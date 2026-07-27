/* config.ini reader and self-seeder -- see include/pvz2native/config.h.
 *
 * A tiny, dependency-free INI parser: `[section]` headers, `key = value` lines,
 * `;`/`#` comments. Unknown sections and keys are ignored on purpose so a config
 * written for a newer build does not break an older one. Booleans accept 1/0,
 * true/false, yes/no, on/off. The whole file is optional; with no file every
 * switch takes the default recorded in kSettings below.
 *
 * kSettings is the single source of truth. The parser, the built-in defaults and
 * the config.ini written on first run are all generated from that one table,
 * which is the point: they used to be three hand-maintained lists and all three
 * had drifted apart --
 *
 *   - `save_dir` was documented in config.h as a [paths] key and had a
 *     resolve-relative branch in load(), but no parser entry at all, so it was
 *     unsettable and that branch was dead code;
 *   - the seeded file advertised `main.7.com.ea.game.pvz2_na.obb` while the code
 *     defaulted to `main.147.com.ea.game.pvz2_row.obb`, so a player who
 *     uncommented the documented line broke their own boot;
 *   - config.h promised fps_limit defaulted to 120 while it was actually 60;
 *   - `[log] input` was parsed but appeared in no generated file, so the switch
 *     existed and nobody could find it.
 *
 * Adding a switch is now one row.
 */

#include <pvz2native/config.h>

#include <cctype>
#include <cstddef>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <string>

namespace {

/* The single instance. Zero-initialised => every flag off and both paths empty,
 * which is the correct "no config loaded yet" state for anything that reads
 * pvz2_config() before pvz2_config_load() runs. */
pvz2_config_t g_config{};

/* Default resource file names, resolved under <exe_dir>/lib. kSettings quotes
 * these in the seeded file's documentation, so the two cannot disagree. */
constexpr char kDefaultSoName[] = "libPVZ2.so";
constexpr char kDefaultObbName[] = "main.147.com.ea.game.pvz2_row.obb";

enum class Kind {
    kBool,    /* 1/0, true/false, yes/no, on/off; anything else reads as off */
    kUInt,    /* strtoul, base 0 */
    kInt,     /* strtol, base 0 */
    kString,  /* copied verbatim, trimmed */
    kLower,   /* copied lowercased, trimmed */
    kPath,    /* copied verbatim; resolved against the exe folder after parsing */
    kNetwork, /* none/mobile/wifi -> 0/1/2 */
    kFps,     /* like kInt, but a value with no digits keeps the default */
};

struct Setting {
    const char *section;
    const char *key;
    const char *alias; /* accepted spelling that is not advertised, or nullptr */
    Kind kind;
    std::size_t offset;   /* into pvz2_config_t */
    std::size_t capacity; /* char-array fields only */

    /* Comment block written above this key in a seeded config.ini, one line per
     * '\n'. Written with a leading "; ". */
    const char *comment;

    /* The value written into a seeded config.ini AND applied as the built-in
     * default before the file is parsed -- so those two can never disagree.
     *
     * nullptr means the key is written commented out and is NOT given a default
     * here: either the zero-initialised value is already correct (video width and
     * height, where 0 means "derive from mode"), or the default depends on the
     * executable's folder and finish_defaults() computes it (the three paths, and
     * user_locale). */
    const char *seed;

    /* Shown after "; <key> = " when seed is nullptr: an example, not a default. */
    const char *example;
};

/* Sections are emitted into a seeded file in the order they first appear here. */
const Setting kSettings[] = {
    /* The `so` and `obb` examples are generated from kDefaultSoName/kDefaultObbName
     * -- see example_for() -- so the documented name is always the real one. */
    {"paths", "so", nullptr, Kind::kPath, offsetof(pvz2_config_t, so_path), sizeof(g_config.so_path),
     "Where to load the game data from. Leave commented for <exe folder>/lib/<name>.\n"
     "A relative path is resolved against the folder holding the .exe.",
     nullptr, nullptr},
    {"paths", "obb", nullptr, Kind::kPath, offsetof(pvz2_config_t, obb_path),
     sizeof(g_config.obb_path), nullptr, nullptr, nullptr},
    {"paths", "save", nullptr, Kind::kPath, offsetof(pvz2_config_t, save_dir),
     sizeof(g_config.save_dir),
     "Where persisted settings go (see persist_saves). Default <exe folder>/save.", nullptr,
     "save"},

    {"log", "verbose", nullptr, Kind::kBool, offsetof(pvz2_config_t, verbose), 0,
     "Blow-by-blow boot log: one banner per .init_array entry and lifecycle call.", "0", nullptr},
    {"log", "trace", nullptr, Kind::kBool, offsetof(pvz2_config_t, trace), 0,
     "Per-import-call trace. Extremely loud (~57 MB of stdout per run).", "0", nullptr},
    {"log", "pc_sample", nullptr, Kind::kBool, offsetof(pvz2_config_t, pc_sample), 0,
     "[pc-sample]/[HOT] instruction sampling and hot-frame slicing. This is the\n"
     "source of the \"pc=0x.... lr=0x....\" spam.",
     "0", nullptr},
    {"log", "input", nullptr, Kind::kBool, offsetof(pvz2_config_t, input), 0,
     "Per-event touch/key logging, plus a frame heartbeat with heap and audio\n"
     "health. This is the switch to turn on for a freeze or a dead click: if the\n"
     "heartbeat stops, the engine hung inside onDrawFrame; if it keeps ticking,\n"
     "the event arrived and the engine chose to ignore it.",
     "0", nullptr},

    {"runtime", "no_page_table", nullptr, Kind::kBool, offsetof(pvz2_config_t, no_page_table), 0,
     "Route guest memory through the slow callback path (needed by watchpoints).", "0", nullptr},
    {"runtime", "heap_quarantine", nullptr, Kind::kUInt, offsetof(pvz2_config_t, heap_quarantine), 0,
     "Hold the N most-recently-freed heap blocks out of the reuse pool (0 = off).", "0", nullptr},

    {"gl", "debug_clear", nullptr, Kind::kBool, offsetof(pvz2_config_t, gl_debug_clear), 0,
     "Force a loud clear colour instead of the engine's opaque black.", "0", nullptr},
    {"gl", "no_viewport_fix", nullptr, Kind::kBool, offsetof(pvz2_config_t, gl_no_viewport_fix), 0,
     "Disable the zero-size viewport substitution.", "0", nullptr},
    {"gl", "flat_fragment", nullptr, Kind::kBool, offsetof(pvz2_config_t, gl_flat_fragment), 0,
     "Replace every fragment shader body with a constant colour (bisection tool).", "0", nullptr},
    {"gl", "strict", nullptr, Kind::kBool, offsetof(pvz2_config_t, gl_strict), 0,
     "Query GL state on every suspicious call and report mismatches.", "0", nullptr},

    {"video", "mode", nullptr, Kind::kLower, offsetof(pvz2_config_t, video_mode),
     sizeof(g_config.video_mode),
     "Launch resolution. The window is always resizable and the engine re-fits\n"
     "on resize, so this is only the STARTING size.\n"
     "  mode = auto   -> match the display's aspect at a moderate base height\n"
     "                   (light on the GPU; 4:3/16:10/16:9 each get right shape)\n"
     "  mode = native -> match the display's full resolution (sharpest, heaviest)\n"
     "An explicit width AND height (both > 0) override mode and are used as-is.",
     "auto", nullptr},
    {"video", "width", nullptr, Kind::kInt, offsetof(pvz2_config_t, video_width), 0, nullptr,
     nullptr, "0"},
    {"video", "height", nullptr, Kind::kInt, offsetof(pvz2_config_t, video_height), 0, nullptr,
     nullptr, "0"},
    {"video", "fullscreen", nullptr, Kind::kBool, offsetof(pvz2_config_t, video_fullscreen), 0,
     "Start borderless-fullscreen (F11 toggles it at runtime either way).", "0", nullptr},
    {"video", "fps_limit", "fpslimit", Kind::kFps, offsetof(pvz2_config_t, fps_limit), 0,
     "Frame-rate cap, in FPS. The simulation runs off the wall clock, so a lower\n"
     "cap does not slow the game down -- it only stops the loop from redrawing as\n"
     "fast as the CPU allows and pinning a core. Vsync stays on, so what reaches\n"
     "the screen is whichever is lower, this or the monitor's refresh rate.\n"
     "  0 = no limit at all (vsync off too: tearing, one core saturated).",
     "60", nullptr},

    {"game", "user_locale", nullptr, Kind::kString, offsetof(pvz2_config_t, user_locale),
     sizeof(g_config.user_locale),
     "Locale the engine is told the device uses, as <lang>_<REGION>. Steers\n"
     "currency, store region and which localized text loads; an unavailable\n"
     "language falls back to English. The country code is taken from the part\n"
     "after '_'. Leave commented for en_US.",
     nullptr, "en_US"},
    {"game", "emulate_iap", nullptr, Kind::kBool, offsetof(pvz2_config_t, emulate_iap), 0,
     "Emulated in-app purchases: on = the store is available and every purchase\n"
     "the game requests completes for free; off = store reports not supported.",
     "1", nullptr},
    {"game", "persist_saves", nullptr, Kind::kBool, offsetof(pvz2_config_t, persist_saves), 0,
     "Persist the game's saved settings (age gate, options) to the save folder\n"
     "so they survive a restart. Off makes every launch a clean first run.",
     "1", nullptr},
    {"game", "network", nullptr, Kind::kNetwork, offsetof(pvz2_config_t, network_status), 0,
     "What the game is told about network connectivity: none, mobile or wifi.\n"
     "Leave it at none. The port has no network stack, and telling the engine it\n"
     "is online makes the loading screen wait forever for downloads that can\n"
     "never arrive. The store does NOT need this -- it is emulated locally.",
     "none", nullptr},
};

/* --- small helpers --------------------------------------------------------- */

std::string trim(const std::string &s) {
    std::size_t a = 0, b = s.size();
    while (a < b && std::isspace((unsigned char)s[a])) ++a;
    while (b > a && std::isspace((unsigned char)s[b - 1])) --b;
    return s.substr(a, b - a);
}

std::string lower(std::string s) {
    for (char &c : s) c = (char)std::tolower((unsigned char)c);
    return s;
}

int parse_bool(const std::string &v) {
    const std::string t = lower(trim(v));
    if (t == "1" || t == "true" || t == "yes" || t == "on") return 1;
    return 0; /* anything else, including empty/garbage, reads as off */
}

void *field(const Setting &s) { return (char *)&g_config + s.offset; }

/* Writes one already-trimmed value into its config field, per the row's kind. */
void store(const Setting &s, const std::string &raw) {
    switch (s.kind) {
        case Kind::kBool:
            *(int *)field(s) = parse_bool(raw);
            break;
        case Kind::kUInt:
            *(unsigned *)field(s) = (unsigned)std::strtoul(raw.c_str(), nullptr, 0);
            break;
        case Kind::kInt:
            *(int *)field(s) = (int)std::strtol(raw.c_str(), nullptr, 0);
            break;
        case Kind::kString:
        case Kind::kPath: {
            const std::string v = trim(raw);
            if (v.empty()) return; /* an empty value is "unset", not "clear it" */
            std::snprintf((char *)field(s), s.capacity, "%s", v.c_str());
            break;
        }
        case Kind::kLower: {
            const std::string v = lower(trim(raw));
            if (v.empty()) return;
            std::snprintf((char *)field(s), s.capacity, "%s", v.c_str());
            break;
        }
        case Kind::kNetwork: {
            /* Named rather than numeric because the numbers are Android's
             * (AndroidHttpProxy.GetNetworkStatus), not something a player should
             * have to know. An unrecognised word keeps the default. */
            const std::string t = lower(trim(raw));
            if (t == "wifi" || t == "on" || t == "1" || t == "true" || t == "yes")
                *(int *)field(s) = 2;
            else if (t == "mobile" || t == "cell")
                *(int *)field(s) = 1;
            else if (t == "none" || t == "off" || t == "0" || t == "false" || t == "no")
                *(int *)field(s) = 0;
            break;
        }
        case Kind::kFps: {
            const std::string t = trim(raw);
            char *end = nullptr;
            const long v = std::strtol(t.c_str(), &end, 10);
            /* A value with no digits at all (typo, empty) keeps the default rather
             * than reading as 0 and silently uncapping the loop; a real number
             * <= 0 is the documented "no limit". */
            if (end != t.c_str()) *(int *)field(s) = v > 0 ? (int)v : 0;
            break;
        }
    }
}

void apply(const std::string &section, const std::string &key, const std::string &val) {
    for (const Setting &s : kSettings) {
        if (section != s.section) continue;
        if (key != s.key && (s.alias == nullptr || key != s.alias)) continue;
        store(s, val);
        return;
    }
    /* Unknown (section, key) pairs are silently ignored -- see the header. */
}

/* Parses an already-open INI stream into g_config. */
void parse(FILE *f) {
    std::string section;
    char line[1024];
    while (std::fgets(line, sizeof(line), f) != nullptr) {
        std::string s = trim(line);
        if (s.empty() || s[0] == ';' || s[0] == '#') continue;
        if (s.front() == '[' && s.back() == ']') {
            section = lower(trim(s.substr(1, s.size() - 2)));
            continue;
        }
        const std::size_t eq = s.find('=');
        if (eq == std::string::npos) continue;
        apply(section, lower(trim(s.substr(0, eq))), s.substr(eq + 1));
    }
}

/* --- the seeded file ------------------------------------------------------- */

void write_comment(FILE *f, const char *text) {
    fputs("; ", f);
    for (const char *p = text; *p != '\0'; ++p) {
        std::fputc(*p, f);
        if (*p == '\n') fputs("; ", f);
    }
    std::fputc('\n', f);
}

/* The commented-out example value for a row that has no literal default.
 *
 * The two resource paths derive theirs from the same constants the loader falls
 * back to, keyed on the field rather than the key name. They used to be separate
 * string literals in the seeded text, and the advertised .obb name did not match
 * the one the code actually looked for. */
std::string example_for(const Setting &s) {
    if (s.offset == offsetof(pvz2_config_t, so_path)) return std::string("lib/") + kDefaultSoName;
    if (s.offset == offsetof(pvz2_config_t, obb_path)) return std::string("lib/") + kDefaultObbName;
    return s.example != nullptr ? s.example : "";
}

/* Generated from kSettings, so it documents exactly what the parser accepts and
 * exactly the values it defaults to. Best-effort: failure (read-only folder) is
 * fine, since the in-memory defaults are the same ones this file would state. */
void seed_default(const char *ini_path) {
    FILE *f = std::fopen(ini_path, "wb");
    if (f == nullptr) return;

    fputs("; PvZ2Native configuration.\n"
          ";\n"
          "; Written automatically on first run, with every switch at its default --\n"
          "; deleting the file changes nothing about how the game behaves. Set a value\n"
          "; to change it; booleans accept 1/0, true/false, yes/no, on/off. This is the\n"
          "; single source of truth -- the old PVZ2_* environment variables are gone.\n",
          f);

    const char *current = nullptr;
    for (const Setting &s : kSettings) {
        if (current == nullptr || std::strcmp(current, s.section) != 0) {
            current = s.section;
            std::fprintf(f, "\n[%s]\n", s.section);
        }
        if (s.comment != nullptr) write_comment(f, s.comment);
        if (s.seed != nullptr) {
            std::fprintf(f, "%s = %s\n", s.key, s.seed);
        } else {
            std::fprintf(f, "; %s = %s\n", s.key, example_for(s).c_str());
        }
    }
    std::fclose(f);
}

/* --- path resolution ------------------------------------------------------- */

void set_path(char *dst, std::size_t cap, const std::string &v) {
    std::snprintf(dst, cap, "%s", v.c_str());
}

/* base_dir with a guaranteed trailing separator (SDL_GetBasePath supplies one,
 * but be defensive), or "" when there is no base. */
std::string base_with_sep(const char *base_dir) {
    std::string base = (base_dir != nullptr) ? base_dir : "";
    if (!base.empty() && base.back() != '/' && base.back() != '\\') base.push_back('/');
    return base;
}

/* A drive-qualified ("C:/..."), UNC, or root-relative path is used verbatim;
 * anything else is treated as relative to the executable's folder, so a
 * config.ini can say `so = lib/libPVZ2.so` and still work from any CWD. */
bool is_absolute(const char *p) {
    if (p == nullptr || p[0] == '\0') return false;
    if (p[0] == '/' || p[0] == '\\') return true;
    return std::isalpha((unsigned char)p[0]) && p[1] == ':';
}

/* Gives a path field its final, concrete value: the caller's `fallback` when the
 * file set nothing, otherwise the file's value resolved against the exe folder. */
void finish_path(char *dst, std::size_t cap, const char *base_dir, const std::string &fallback) {
    if (dst[0] == '\0') {
        set_path(dst, cap, fallback);
    } else if (!is_absolute(dst)) {
        set_path(dst, cap, base_with_sep(base_dir) + dst);
    }
}

/* The defaults that cannot be literals in kSettings because they depend on where
 * the executable lives. Runs after parsing, so a value from the file wins. */
void finish_defaults(const char *base_dir) {
    const std::string lib = base_with_sep(base_dir) + "lib/";
    finish_path(g_config.so_path, sizeof(g_config.so_path), base_dir, lib + kDefaultSoName);
    finish_path(g_config.obb_path, sizeof(g_config.obb_path), base_dir, lib + kDefaultObbName);
    finish_path(g_config.save_dir, sizeof(g_config.save_dir), base_dir,
                base_with_sep(base_dir) + "save");

    if (g_config.user_locale[0] == '\0')
        std::snprintf(g_config.user_locale, sizeof(g_config.user_locale), "en_US");
}

}  // namespace

extern "C" void pvz2_config_load(const char *ini_path, const char *base_dir) {
    g_config = pvz2_config_t{}; /* reset to all-zero before (re)reading */

    /* Every non-zero default, taken from the same strings the seeded file shows.
     * This must precede parsing so an explicit entry in the file can still
     * override it, and so a config.ini written by an OLDER build -- which has no
     * line for a newer key at all -- gets the intended default rather than a zero
     * that means something else entirely (fps_limit 0 = uncapped, network 0 =
     * offline, which used to disable the store). */
    for (const Setting &s : kSettings) {
        if (s.seed != nullptr) store(s, s.seed);
    }

    if (ini_path != nullptr) {
        FILE *f = std::fopen(ini_path, "rb");
        if (f == nullptr) { /* first run: write the documented default */
            seed_default(ini_path);
            f = std::fopen(ini_path, "rb");
        }
        if (f != nullptr) {
            parse(f);
            std::fclose(f);
        }
    }

    finish_defaults(base_dir);
}

extern "C" const pvz2_config_t *pvz2_config(void) { return &g_config; }
