/* Guest path translation -- see vfs.h for why this layer exists at all. */

#include <pvz2native/dependencies/vfs.h>

#include <pvz2native/config.h>

#include <cctype>
#include <cstdio>
#include <fcntl.h>
#include <filesystem>
#include <string>
#include <vector>

#if defined(_WIN32)
#include <io.h>
#endif

namespace pvz2native {
namespace vfs {

/* Real on-disk resources (doc section 9.22). The game's own native loader
 * (ResStreamsManager/ResourceManager: sub_2BE120 -> sub_867740/sub_864950)
 * opens "main.rsb" via libc fopen/fread and decodes the outer "1bsr" RSB plus
 * the inner "pgsr"/RTON formats ITSELF -- so none of those formats are
 * reimplemented here. It gets real file I/O over the real .obb, which literally
 * IS a "1bsr" archive (magic confirmed). */
/* Both come from config.ini (see pvz2_config_load): the host path is used
 * verbatim, and the guest path is the same with a leading '/' so
 * ResStreamsManager::LoadRSB takes its verbatim branch (see vfs.h). The guest
 * form is cached on first use -- the config path is fixed for the run. */
const char *obb_host_path() { return pvz2_config()->obb_path; }

const char *obb_guest_path() {
    static const std::string guest = std::string("/") + pvz2_config()->obb_path;
    return guest.c_str();
}

namespace {

/* One mounted overlay. Startup-only, appended to and never erased -- see the
 * header for why it needs no lock. */
struct Overlay {
    std::string dir;
    std::string mod;
};
std::vector<Overlay> g_overlays;

/* The last `components` path components of `p`, e.g. "properties/main.rsb". */
std::string tail_of(const std::string &p, unsigned components) {
    std::size_t at = p.size();
    for (unsigned i = 0; i < components; ++i) {
        const std::size_t slash = p.find_last_of('/', at == 0 ? 0 : at - 1);
        if (slash == std::string::npos || at == 0) return p;
        at = slash;
    }
    return p.substr(at + 1);
}

/* An overlay file standing in for `p`, or an empty string.
 *
 * More specific wins: the two-component form is tried before the bare file
 * name, so a mod that ships properties/main.rsb is not answered by its own
 * unrelated top-level main.rsb. Between overlays, the most recently mounted
 * wins -- last mod loaded is the one on top, which is the convention every mod
 * manager uses. */
std::string overlay_for(const std::string &p) {
    if (g_overlays.empty()) return {};
    const std::string two = tail_of(p, 2);
    const std::string one = tail_of(p, 1);
    if (one.empty()) return {};
    for (std::size_t i = g_overlays.size(); i-- > 0;) {
        for (const std::string &rel : {two, one}) {
            if (rel.empty() || rel == p) continue; /* p itself: not a relative name */
            std::string cand = g_overlays[i].dir + "/" + rel;
            std::error_code ec;
            if (std::filesystem::is_regular_file(cand, ec)) {
                std::printf("pvz2: [mod] %s overrides %s with %s\n", g_overlays[i].mod.c_str(),
                            one.c_str(), cand.c_str());
                std::fflush(stdout);
                return cand;
            }
        }
    }
    return {};
}

}  // namespace

void mount_overlay(const std::string &host_dir, const std::string &mod_name) {
    std::error_code ec;
    if (!std::filesystem::is_directory(host_dir, ec)) return;
    g_overlays.push_back({host_dir, mod_name});
}

unsigned overlay_count() { return (unsigned)g_overlays.size(); }

std::string translate(GuestRuntime *rt, std::string p) {
    /* SexyAppFramework's VFS tags every resource lookup with a scheme prefix
     * before it reaches libc; on Android the AAssetManager layer consumes it,
     * so the bytes libc sees never carry it. Stripping it here is what unblocked
     * the RSB load: the engine logged "Loading main RSB from the path <obb>"
     * and then asked for "ASSET:<obb>", which no host open could satisfy. */
    if (p.compare(0, kAssetSchemeLen, kAssetScheme) == 0) p.erase(0, kAssetSchemeLen);
    for (char &c : p) if (c == '\\') c = '/';
    /* Undo the leading slash obb_guest_path() carries to look absolute to the
     * engine: "/E:/..." is not a path the host can open. */
    if (p.size() >= 3 && p[0] == '/' && std::isalpha((unsigned char)p[1]) && p[2] == ':') {
        p.erase(0, 1);
    }

    /* Save data lands in a folder the ENGINE names: "No_Backup/" is a string
     * literal inside libPVZ2.so (on iOS/Android it marks data excluded from
     * cloud backup, which means nothing on a PC). Renaming it here rather than
     * patching the .so keeps this independent of the game build -- and this is
     * the layer every path already passes through on its way to libc, so it
     * covers the mkdir that creates it as well as the reads and writes. */
    static const char kEngineDataDir[] = "No_Backup";
    static const char kHostDataDir[] = "userdata";
    for (std::size_t at = p.find(kEngineDataDir); at != std::string::npos;
         at = p.find(kEngineDataDir, at + sizeof(kHostDataDir) - 1)) {
        p.replace(at, sizeof(kEngineDataDir) - 1, kHostDataDir);
    }

    std::size_t slash = p.find_last_of('/');
    std::string base = (slash == std::string::npos) ? p : p.substr(slash + 1);
    for (char &c : base) c = (char)std::tolower((unsigned char)c);

    /* A mod's copy of this file, if any, before the real game data. Checked
     * after the path is fully normalised so an overlay matches what the engine
     * actually asked for, and before the .obb redirect so a mod CAN replace the
     * main archive -- which is the single most useful thing a mod pack can do. */
    const std::string overridden = overlay_for(p);
    if (!overridden.empty()) {
        if (rt != nullptr && (base == "main.rsb" ||
                              (base.size() > 4 && base.compare(base.size() - 4, 4, ".obb") == 0))) {
            rt->rsb_touched = true;
        }
        return overridden;
    }

    if (base == "main.rsb") {
        if (rt != nullptr) rt->rsb_touched = true;
        return std::string(obb_host_path());
    }
    /* The engine may also open the .obb by the full path it got from
     * FrameworkInfo_SysGetMainExpansionFilePath -- that counts as reaching the
     * RSB load too. */
    if (base.size() > 4 && base.compare(base.size() - 4, 4, ".obb") == 0) {
        if (rt != nullptr) rt->rsb_touched = true;
    }
    return p;
}

int translate_open_flags(std::uint32_t g) {
    int h = (int)(g & 3); /* O_RDONLY/O_WRONLY/O_RDWR share values 0/1/2 */
#if defined(_WIN32)
    if (g & 0x40)  h |= _O_CREAT;
    if (g & 0x80)  h |= _O_EXCL;
    if (g & 0x200) h |= _O_TRUNC;
    if (g & 0x400) h |= _O_APPEND;
    h |= _O_BINARY;
#else
    if (g & 0x40)  h |= O_CREAT;
    if (g & 0x80)  h |= O_EXCL;
    if (g & 0x200) h |= O_TRUNC;
    if (g & 0x400) h |= O_APPEND;
#endif
    return h;
}

}  // namespace vfs
}  // namespace pvz2native
