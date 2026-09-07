/* The mod loader -- see include/pvz2native/mods/mod_host.h and docs/MODDING.md.
 *
 * Three things happen here and they are deliberately independent: mounting a
 * pack's assets/ folder over the game data, applying the guest-code patches its
 * mod.ini declares, and loading its native plugin. A pack may do any or all of
 * them, and a failure in one does not stop the others -- a mod whose plugin
 * refuses to load still gets its textures replaced.
 *
 * Nothing here contains an address. A mod.ini's `at` is data supplied by the
 * mod, checked against `expect` before it is written, exactly as game/patches
 * checks the addresses in symbols.cpp; `symbol` resolves through the version
 * table instead. That is the whole reason mod.ini exists rather than telling
 * people to edit symbols.cpp: their addresses are theirs, and stay out of the
 * project's one address file.
 */

#include <pvz2native/mods/mod_host.h>

#include <pvz2native/config.h>
#include <pvz2native/dependencies/vfs.h>
#include <pvz2native/game/hooks.h>
#include <pvz2native/game/scanner.h>
#include <pvz2native/game/symbols.h>
#include <pvz2native/mods/mod_api.h>

#include <algorithm>
#include <cctype>
#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <deque>
#include <filesystem>
#include <fstream>
#include <string>
#include <vector>

#if defined(_WIN32)
#include <windows.h>
#else
#include <dlfcn.h>
#endif

namespace pvz2native {
namespace mods {
namespace {

namespace fs = std::filesystem;

pvz2_elf_image_t *g_img = nullptr;
GuestRuntime *g_rt = nullptr;
unsigned g_loaded = 0;

/* Whose log line is this? Set while a mod's pvz2_mod_init runs and while one of
 * its hook handlers is being dispatched, so api->log names the right mod
 * without the plugin having to pass a handle it never asked for. A hook that
 * logs from a guest thread the harness did not dispatch will be attributed to
 * whatever ran last, which is a cosmetic inaccuracy in a diagnostic line and
 * not worth a lock on every log call. */
const char *g_current_mod = "mod";

/* --- guest memory, bounds-checked ------------------------------------------ */

bool addr_ok(std::uint32_t addr, std::uint32_t size) {
    return g_img != nullptr && g_img->mem != nullptr &&
           (std::uint64_t)addr + size <= g_img->mem_size;
}

std::uint32_t api_read32(std::uint32_t addr) {
    if (!addr_ok(addr, 4)) return 0;
    std::uint32_t v = 0;
    std::memcpy(&v, g_img->mem + addr, 4);
    return v;
}

void api_write32(std::uint32_t addr, std::uint32_t v) {
    if (!addr_ok(addr, 4)) return;
    std::memcpy(g_img->mem + addr, &v, 4);
}

std::uint32_t api_read8(std::uint32_t addr) {
    return addr_ok(addr, 1) ? g_img->mem[addr] : 0;
}

void api_write8(std::uint32_t addr, std::uint32_t v) {
    if (addr_ok(addr, 1)) g_img->mem[addr] = (std::uint8_t)v;
}

std::uint32_t api_read_cstr(std::uint32_t addr, char *out, std::uint32_t max) {
    if (out == nullptr || max == 0) return 0;
    out[0] = '\0';
    std::uint32_t n = 0;
    while (n + 1 < max && addr_ok(addr + n, 1) && g_img->mem[addr + n] != 0) {
        out[n] = (char)g_img->mem[addr + n];
        ++n;
    }
    out[n] = '\0';
    return n;
}

/* --- finding things -------------------------------------------------------- */

std::uint32_t api_symbol(const char *name) { return game_symbols_lookup(name); }

std::uint32_t api_native(const char *jni_name) {
    /* Re-scanned per call rather than cached: a mod asks for a handful of these
     * during init and never again, and a cache would have to be invalidated by
     * nothing in particular. */
    const std::vector<scan::NativeMethod> natives = scan::find_registered_natives(g_img);
    const scan::NativeMethod *m = scan::find_native(natives, jni_name);
    return m != nullptr ? m->fn : 0;
}

std::uint32_t api_find_pattern(const char *pat) { return scan::find_pattern(g_img, pat); }
std::uint32_t api_find_string(const char *s) { return scan::find_string(g_img, s); }

std::uint32_t api_word_ref(std::uint32_t value) {
    const std::vector<std::uint32_t> refs = scan::find_word_refs(g_img, value, 1);
    return refs.empty() ? 0 : refs[0];
}

void api_log(const char *fmt, ...) {
    std::printf("pvz2: [mod:%s] ", g_current_mod);
    std::va_list ap;
    va_start(ap, fmt);
    std::vprintf(fmt, ap);
    va_end(ap);
    std::printf("\n");
    std::fflush(stdout);
}

const pvz2_config_t *api_config() { return pvz2_config(); }

/* --- changing what the guest does ------------------------------------------ */

/* A plugin's callback plus its user pointer, kept alive for the process. A
 * deque so the addresses the hook layer holds stay valid as more are appended. */
struct PluginHook {
    Pvz2ModHookFn fn;
    void *user;
    const char *mod;      /* owned by the ModPack; outlives the process */
    std::string name;
};
std::deque<PluginHook> g_plugin_hooks;

/* The single host-side handler behind every plugin hook. Hook::user says which
 * plugin callback this is -- which is what that field exists for, and why the
 * mod layer needs no per-hook thunk of its own. */
void plugin_hook_dispatch(GuestCall &c, const hooks::Hook &h) {
    auto *ph = static_cast<PluginHook *>(h.user);
    if (ph == nullptr || ph->fn == nullptr) return;
    const char *saved = g_current_mod;
    g_current_mod = ph->mod;
    ph->fn(reinterpret_cast<Pvz2ModCall *>(&c), reinterpret_cast<const Pvz2ModHook *>(&h),
           ph->user);
    g_current_mod = saved;
}

int api_hook(std::uint32_t offset, const char *name, Pvz2ModHookFn fn, void *user) {
    if (fn == nullptr) return 0;
    g_plugin_hooks.push_back({fn, user, g_current_mod, name != nullptr ? name : "mod hook"});
    PluginHook &ph = g_plugin_hooks.back();
    return hooks::install(g_img, offset, ph.name.c_str(), &plugin_hook_dispatch, &ph) ? 1 : 0;
}

/* Rewrites one instruction after checking what is there. Shared by the plugin
 * API and by mod.ini's [patch] sections, so the two cannot disagree about what
 * a refused patch means. */
bool patch_word(std::uint32_t offset, std::uint32_t expect, std::uint32_t value,
                const char *what) {
    if (g_img == nullptr || offset == 0 || offset + 4 > g_img->so_span) {
        std::printf("pvz2: [mod:%s] %s: 0x%x is not inside the game image -- not patched\n",
                    g_current_mod, what, offset);
        return false;
    }
    const std::uint32_t addr = g_img->so_base + offset;
    const std::uint32_t found = api_read32(addr);
    if (expect != 0 && found != expect) {
        std::printf("pvz2: [mod:%s] %s: 0x%x holds 0x%08x, not the expected 0x%08x -- not patched; "
                    "this address belongs to a different build of the game\n",
                    g_current_mod, what, offset, found, expect);
        return false;
    }
    api_write32(addr, value);
    std::printf("pvz2: [mod:%s] %s: 0x%x 0x%08x -> 0x%08x\n", g_current_mod, what, offset, found,
                value);
    return true;
}

int api_patch_word(std::uint32_t offset, std::uint32_t expect, std::uint32_t value) {
    return patch_word(offset, expect, value, "patch") ? 1 : 0;
}

/* --- inside a hook handler -------------------------------------------------- */

GuestCall &call_of(Pvz2ModCall *c) { return *reinterpret_cast<GuestCall *>(c); }

std::uint32_t api_arg(Pvz2ModCall *c, int i) { return c == nullptr ? 0 : call_of(c).arg(i); }

void api_set_arg(Pvz2ModCall *c, int i, std::uint32_t v) {
    if (c == nullptr) return;
    if (i < 0 || i > 3) {
        /* Arguments past the fourth live on the caller's stack, and rewriting
         * one there would change what the CALLER sees on return as well, which
         * is not what "set an argument" means anywhere else. Refused rather
         * than half-implemented. */
        api_log("set_arg(%d): only r0-r3 can be rewritten; stack arguments are the caller's", i);
        return;
    }
    call_of(c).regs[i] = v;
}

void api_set_result(Pvz2ModCall *c, std::uint32_t v) {
    if (c != nullptr) call_of(c).set_result(v);
}

std::uint32_t api_call_original(Pvz2ModCall *c, const Pvz2ModHook *h, const std::uint32_t *args,
                                int nargs) {
    if (c == nullptr || h == nullptr) return 0;
    const auto &hook = *reinterpret_cast<const hooks::Hook *>(h);
    if (args == nullptr || nargs <= 0) return hook.call_original(call_of(c));
    return hook.call_original(call_of(c), args, nargs);
}

std::uint32_t api_call_guest(Pvz2ModCall *c, std::uint32_t addr, const std::uint32_t *args,
                             int nargs) {
    if (c == nullptr || addr == 0) return 0;
    return call_of(c).call_guest(addr, args, nargs);
}

/* --- one mod pack ----------------------------------------------------------- */

struct ModPack {
    std::string name; /* the folder name, and what the log calls it */
    fs::path dir;
    Pvz2ModApi api{};
    /* Storage the api table points into, so the strings it hands the plugin
     * outlive this function. A deque of packs keeps them stable. */
    std::string dir_str;
};
std::deque<ModPack> g_packs;

/* --- mod.ini ---------------------------------------------------------------
 *
 * Same shape as config.ini -- [section], key = value, ; or # comments -- but
 * with repeated [patch] sections, each one instruction. Deliberately its own
 * tiny parser rather than a JSON dependency: the whole grammar is five keys,
 * and a mod author already knows what an .ini is. */

/* A number as a mod writes it: decimal, or hex with 0x. */
bool parse_u32(const std::string &s, std::uint32_t &out) {
    if (s.empty()) return false;
    char *end = nullptr;
    const unsigned long v = std::strtoul(s.c_str(), &end, 0);
    if (end == s.c_str() || *end != '\0') return false;
    out = (std::uint32_t)v;
    return true;
}

/* An instruction word: a literal, or one of the two mnemonics worth having --
 * they are the ones a mod reaches for constantly and the ones most often
 * mistyped as a raw word. */
bool parse_word(const std::string &s, std::uint32_t &out) {
    if (s == "nop") {  /* MOV R0, R0 -- the traditional ARM NOP, valid on ARMv5+ */
        out = 0xE1A00000u;
        return true;
    }
    if (s == "ret") {  /* BX LR */
        out = 0xE12FFF1Eu;
        return true;
    }
    return parse_u32(s, out);
}

std::string trim(std::string s) {
    std::size_t b = 0, e = s.size();
    while (b < e && std::isspace((unsigned char)s[b]) != 0) ++b;
    while (e > b && std::isspace((unsigned char)s[e - 1]) != 0) --e;
    return s.substr(b, e - b);
}

/* One [patch] record, accumulated line by line and applied when the section
 * ends. Held whole rather than applied key-by-key because `expect` may follow
 * `write` in the file and the check has to happen first. */
struct PatchRecord {
    bool active = false;
    std::string game;    /* "" or "*" = every build */
    std::string symbol;  /* a dotted symbol name, as an alternative to `at` */
    std::uint32_t at = 0;
    std::uint32_t plus = 0;
    std::uint32_t expect = 0;
    bool has_write = false;
    std::uint32_t write = 0;
    std::string comment;
};

void apply_patch(const PatchRecord &p, const std::string &mod) {
    if (!p.active) return;
    const char *what = p.comment.empty() ? "patch" : p.comment.c_str();

    if (!p.game.empty() && p.game != "*" && p.game != sym().version) {
        std::printf("pvz2: [mod:%s] %s: declared for game %s, this is %s -- skipped\n",
                    mod.c_str(), what, p.game.c_str(), sym().version);
        return;
    }
    std::uint32_t offset = p.at;
    if (!p.symbol.empty()) {
        offset = game_symbols_lookup(p.symbol.c_str());
        if (offset == 0) {
            std::printf("pvz2: [mod:%s] %s: symbol '%s' is not mapped for %s -- skipped\n",
                        mod.c_str(), what, p.symbol.c_str(), sym().version);
            return;
        }
    }
    if (offset == 0 || !p.has_write) {
        std::printf("pvz2: [mod:%s] %s: needs both an address (at = / symbol =) and write = -- "
                    "skipped\n", mod.c_str(), what);
        return;
    }
    patch_word(offset + p.plus, p.expect, p.write, what);
}

/* Reads mod.ini, applying each [patch] as its section closes. Returns the
 * mod's declared display name, or an empty string. */
std::string read_mod_ini(const fs::path &path, const std::string &folder) {
    std::ifstream in(path);
    if (!in) return {};

    std::string display;
    std::string section;
    PatchRecord patch;
    std::string line;
    unsigned lineno = 0;

    auto close_section = [&] {
        apply_patch(patch, folder);
        patch = PatchRecord{};
    };

    while (std::getline(in, line)) {
        ++lineno;
        line = trim(line);
        if (line.empty() || line[0] == ';' || line[0] == '#') continue;
        if (line.front() == '[' && line.back() == ']') {
            close_section();
            section = line.substr(1, line.size() - 2);
            if (section == "patch") patch.active = true;
            continue;
        }
        const std::size_t eq = line.find('=');
        if (eq == std::string::npos) {
            std::printf("pvz2: [mod:%s] mod.ini:%u: '%s' is not a key = value line -- ignored\n",
                        folder.c_str(), lineno, line.c_str());
            continue;
        }
        const std::string key = trim(line.substr(0, eq));
        const std::string value = trim(line.substr(eq + 1));

        if (section == "mod") {
            if (key == "name") display = value;
            /* Everything else under [mod] -- author, version, description -- is
             * documentation for whoever opens the file, and is ignored here on
             * purpose so a mod can carry notes without this warning about them. */
            continue;
        }
        if (section != "patch") continue;

        bool ok = true;
        if (key == "game") patch.game = value;
        else if (key == "symbol") patch.symbol = value;
        else if (key == "comment") patch.comment = value;
        else if (key == "at") ok = parse_u32(value, patch.at);
        else if (key == "plus") ok = parse_u32(value, patch.plus);
        else if (key == "expect") ok = parse_word(value, patch.expect);
        else if (key == "write") { ok = parse_word(value, patch.write); patch.has_write = ok; }
        else {
            std::printf("pvz2: [mod:%s] mod.ini:%u: unknown key '%s' in [patch] -- ignored\n",
                        folder.c_str(), lineno, key.c_str());
            continue;
        }
        if (!ok) {
            std::printf("pvz2: [mod:%s] mod.ini:%u: '%s' is not a number this understands (use "
                        "0x... , a decimal, 'nop' or 'ret') -- this patch is skipped\n",
                        folder.c_str(), lineno, value.c_str());
            patch.active = false;
        }
    }
    close_section();
    return display;
}

/* --- native plugins --------------------------------------------------------- */

bool is_plugin_file(const fs::path &p) {
    const std::string ext = p.extension().string();
#if defined(_WIN32)
    return ext == ".dll";
#elif defined(__APPLE__)
    return ext == ".dylib" || ext == ".so";
#else
    return ext == ".so";
#endif
}

void *open_library(const fs::path &p) {
#if defined(_WIN32)
    return (void *)LoadLibraryA(p.string().c_str());
#else
    return dlopen(p.string().c_str(), RTLD_NOW | RTLD_LOCAL);
#endif
}

void *find_init(void *lib) {
#if defined(_WIN32)
    return (void *)GetProcAddress((HMODULE)lib, "pvz2_mod_init");
#else
    return dlsym(lib, "pvz2_mod_init");
#endif
}

std::string library_error() {
#if defined(_WIN32)
    return "error " + std::to_string((unsigned long)GetLastError());
#else
    const char *e = dlerror();
    return e != nullptr ? e : "unknown error";
#endif
}

/* Fills a pack's API table. One table per mod rather than one shared one,
 * because mod_dir differs -- and because a plugin is allowed to keep the
 * pointer for the life of the process. */
void fill_api(ModPack &pack) {
    Pvz2ModApi &a = pack.api;
    a.api_version = PVZ2_MOD_API_VERSION;
    a.game_version = sym().version;
    a.mod_dir = pack.dir_str.c_str();
    a.log = &api_log;
    a.symbol = &api_symbol;
    a.native = &api_native;
    a.find_pattern = &api_find_pattern;
    a.find_string = &api_find_string;
    a.word_ref = &api_word_ref;
    a.so_base = g_img != nullptr ? g_img->so_base : 0;
    a.hook = &api_hook;
    a.patch_word = &api_patch_word;
    a.read32 = &api_read32;
    a.write32 = &api_write32;
    a.read8 = &api_read8;
    a.write8 = &api_write8;
    a.read_cstr = &api_read_cstr;
    a.arg = &api_arg;
    a.set_arg = &api_set_arg;
    a.set_result = &api_set_result;
    a.call_original = &api_call_original;
    a.call_guest = &api_call_guest;
    a.config = &api_config;
}

void load_plugins(ModPack &pack) {
    std::error_code ec;
    for (const fs::directory_entry &e : fs::directory_iterator(pack.dir, ec)) {
        if (!e.is_regular_file(ec) || !is_plugin_file(e.path())) continue;

        void *lib = open_library(e.path());
        if (lib == nullptr) {
            std::printf("pvz2: [mod:%s] plugin %s did not load: %s\n", pack.name.c_str(),
                        e.path().filename().string().c_str(), library_error().c_str());
            continue;
        }
        void *init = find_init(lib);
        if (init == nullptr) {
            /* Not necessarily a mistake: a mod may ship a support library its
             * real plugin links against. Said once, quietly, rather than
             * treated as a failure. */
            std::printf("pvz2: [mod:%s] %s exports no pvz2_mod_init -- not a plugin, ignored\n",
                        pack.name.c_str(), e.path().filename().string().c_str());
            continue;
        }
        const int rc = ((Pvz2ModInitFn)init)(&pack.api);
        if (rc != 0) {
            std::printf("pvz2: [mod:%s] plugin %s declined to run on %s (returned %d)\n",
                        pack.name.c_str(), e.path().filename().string().c_str(), sym().version, rc);
            continue;
        }
        std::printf("pvz2: [mod:%s] plugin %s initialised\n", pack.name.c_str(),
                    e.path().filename().string().c_str());
    }
}

void load_one(const fs::path &dir) {
    g_packs.push_back(ModPack{});
    ModPack &pack = g_packs.back();
    pack.name = dir.filename().string();
    pack.dir = dir;
    pack.dir_str = dir.string();
    g_current_mod = pack.name.c_str();

    std::string display;
    std::error_code ec;
    const fs::path ini = dir / "mod.ini";
    if (fs::is_regular_file(ini, ec)) display = read_mod_ini(ini, pack.name);

    const fs::path assets = dir / "assets";
    if (fs::is_directory(assets, ec)) {
        vfs::mount_overlay(assets.string(), pack.name);
    }

    fill_api(pack);
    if (pvz2_config()->mod_plugins != 0) load_plugins(pack);

    std::printf("pvz2: [mod] loaded '%s'%s%s\n", pack.name.c_str(),
                display.empty() ? "" : " -- ", display.c_str());
    ++g_loaded;
}

}  // namespace

void load_all(pvz2_elf_image_t *img, GuestRuntime *rt) {
    g_img = img;
    g_rt = rt;

    const char *root = pvz2_config()->mods_dir;
    if (root == nullptr || root[0] == '\0') return;

    std::error_code ec;
    if (!fs::is_directory(root, ec)) return;

    /* Name order, so a load order exists and can be steered with a numeric
     * prefix -- directory_iterator's own order is whatever the filesystem
     * happens to give, which is not something a mod pack can rely on. */
    std::vector<fs::path> dirs;
    for (const fs::directory_entry &e : fs::directory_iterator(root, ec)) {
        if (e.is_directory(ec)) dirs.push_back(e.path());
    }
    std::sort(dirs.begin(), dirs.end());

    for (const fs::path &d : dirs) load_one(d);
    g_current_mod = "mod";

    if (g_loaded != 0) {
        std::printf("pvz2: [mod] %u mod(s) loaded, %u asset overlay(s), %u guest hook(s)\n",
                    g_loaded, vfs::overlay_count(), hooks::installed_count());
        std::fflush(stdout);
    }
}

unsigned loaded_count() { return g_loaded; }

}  // namespace mods
}  // namespace pvz2native
