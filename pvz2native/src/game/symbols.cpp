/* The per-version address tables, and the detection that picks one.
 *
 * Adding a release means adding one entry to kVersions below and nothing else:
 * no file outside this one holds an address into libPVZ2.so. To fill an entry,
 * decompile JNI_OnLoad and read its JNINativeMethod tables for the `native`
 * offsets, then take the two fingerprints with any hex viewer.
 *
 * Name every field you set (.native = {...}) and leave the rest out rather than
 * writing a 0 placeholder -- see GameSymbols for why a positional entry here is
 * a silent hazard. What an entry does NOT mention is unmapped, which is exactly
 * what 0 means to every reader of the table.
 */

#include <pvz2native/game/symbols.h>

#include <pvz2native/config.h>
#include <pvz2native/game/scanner.h>

#include <cstddef>
#include <cstdio>
#include <cstring>
#include <string>
#include <vector>

namespace pvz2native {

const char *const kGameAppClass = "com/popcap/SexyAppFramework/AndroidGameApp";

namespace {

/* Every other field value-initialises to 0/nullptr, which is exactly what
 * "nothing is mapped" means -- and stays true when a field is added. */
const GameSymbols kUnknown{.version = "unknown"};

/* Element count of a class-name array, so no entry below has to spell out the
 * sizeof/sizeof and risk dividing by the wrong array. */
template <std::size_t N>
constexpr std::uint32_t kCount(const char *const (&)[N]) {
    return (std::uint32_t)N;
}

/* Native_GameAppInitialize's declared objects for 1.6.10 and 4.5.2, which share
 * the same eight-object signature -- the third being an AndroidFacebookDriver
 * that 9.6.1 no longer has. */
const char *const kGameAppInitArgsLegacy[] = {
    "com/popcap/SexyAppFramework/AndroidSurfaceView",
    "com/popcap/SexyAppFramework/AndroidHttpProxy",
    "com/popcap/SexyAppFramework/AndroidFacebookDriver",
    "com/popcap/SexyAppFramework/cloud/Cloud",
    "com/popcap/SexyAppFramework/GooglePlay/GooglePlayConnect",
    "com/popcap/SexyAppFramework/GooglePlay/GooglePlayAchievements",
    "com/popcap/SexyAppFramework/GooglePlay/GooglePlayLeaderboard",
    "com/popcap/SexyAppFramework/AndroidNotification",
};

/* Native_GameAppInitialize's declared objects for PvZ2 9.6.1, read off the JNI
 * signature in its JNINativeMethod entry:
 *   (Lcom/popcap/SexyAppFramework/AndroidSurfaceView;
 *    Lcom/popcap/SexyAppFramework/AndroidHttpProxy;
 *    Lcom/popcap/SexyAppFramework/cloud/Cloud;
 *    Lcom/popcap/SexyAppFramework/GooglePlay/GooglePlayConnect;
 *    Lcom/popcap/SexyAppFramework/GooglePlay/GooglePlayAchievements;
 *    Lcom/popcap/SexyAppFramework/GooglePlay/GooglePlayLeaderboard;
 *    Lcom/popcap/SexyAppFramework/AndroidNotification;)Z
 * Seven, not the eight of 1.6/4.5.2 -- AndroidFacebookDriver is gone. */
const char *const kGameAppInitArgs961[] = {
    "com/popcap/SexyAppFramework/AndroidSurfaceView",
    "com/popcap/SexyAppFramework/AndroidHttpProxy",
    "com/popcap/SexyAppFramework/cloud/Cloud",
    "com/popcap/SexyAppFramework/GooglePlay/GooglePlayConnect",
    "com/popcap/SexyAppFramework/GooglePlay/GooglePlayAchievements",
    "com/popcap/SexyAppFramework/GooglePlay/GooglePlayLeaderboard",
    "com/popcap/SexyAppFramework/AndroidNotification",
};

const GameSymbols kVersions[] = {
    /* --- 1.6.10 (2013, armeabi-v7a) ------------------------------------- */
    {
        .version = "1.6.10",
        .native = {
            .game_app_initialize                = 0x9eaf98,
            .application_will_finish_launching  = 0x9ebfb8,
            .application_did_finish_launching   = 0x9ec0d8,
            .application_will_become_foreground = 0x9ec0f4,
            .application_did_become_active      = 0x9ec100,
            .on_surface_created                 = 0x9f1878,
            .on_surface_changed                 = 0x9f1914,
            .on_draw_frame                      = 0x9f1944,
            /* No PumpMessageQueue: onDrawFrame drains the queue itself. */
        },
        .surface_changed_pad = 2,
        .global = {
            .app        = 0xd55650, /* dword_D55650 -- LawnApp          */
            .app_driver = 0xdc8fd4, /* dword_DC8FD4 -- AndroidAppDriver */
        },
        .fn = {
            .string_ctor = 0xb75560, /* std::string(const char*, allocator) */
        },
        /* jni_native.http_transaction_error: 1.6 reaches the menu, left untouched.
         * patch: not mapped -- nothing is rewritten, so its store behaves exactly
         * as it always has.
         * probe: frame-gate offsets not located for 1.6. */
        .input = {
            .driver        = 68,
            .scaler        = 696,
            .touch_begin   = 192,
            .touch_end     = 196,
            .touch_stride  = 48,
            .multitouch    = 220,
            .touch_active  = 221,
            .scaler_fields = 176,
            .vt_touch_down = 432,
            .vt_touch_up   = 436,
            .vt_touch_move = 440,
        },
        .fingerprint_draw_frame    = 0xE59F0014E92D4800ull, /* PUSH {R11,LR}; LDR R0,=...        */
        .fingerprint_game_app_init = 0xE24DD094E92D4FF0ull, /* PUSH {R4-R11,LR}; SUB SP,SP,#0x94 */
        .game_app_init_args      = kGameAppInitArgsLegacy,
        .game_app_init_arg_count = kCount(kGameAppInitArgsLegacy),
    },

    /* --- 4.5.2 (2016, armeabi-v7a, 18MB) ---------------------------------
     *
     * Same ARM32 binary shape as 1.6 and the same eight natives with the same
     * JNI signatures, so nothing in runtime/ or engine/ needed to change to
     * reach it -- which was the whole point of splitting this table out.
     *
     * Only three natives are registered from JNI_OnLoad here (GameAppInitialize,
     * GameAppTeardown, getGooglePlayAPIKey); the rest live in two more
     * JNINativeMethod arrays at 0x10A6054 (lifecycle, 11 entries) and 0x10A610C
     * (surface, 3 entries), reached by following a data xref from each method
     * NAME string. That is the reliable way to find them in a stripped build --
     * do not try to recognise the functions themselves.
     *
     * Globals and the diagnostic field offsets are NOT mapped yet: they drive
     * only diagnostics/, which checks for 0 and stays quiet, so the boot does
     * not depend on them. */
    {
        .version = "4.5.2",
        .native = {
            .game_app_initialize                = 0xcc033c,
            .application_will_finish_launching  = 0xcc131c,
            .application_did_finish_launching   = 0xcc1420,
            .application_will_become_foreground = 0xcc142c,
            .application_did_become_active      = 0xcc1430,
            .on_surface_created                 = 0xcc7cf0,
            .on_surface_changed                 = 0xcc7d8c,
            .on_draw_frame                      = 0xcc7e60,
            .pump_message_queue                 = 0xcc7cd0,
        },
        .surface_changed_pad = 1, /* body reads r2/r3 -- plain static native */
        .global = {
            /* LawnApp is not located yet; the driver is the pointer
             * onSurfaceCreated and onDrawFrame dereference (1.6's dword_DC8FD4). */
            .app_driver = 0x117a734,
        },
        .jni_native = {
            /* HttpTransactionError: enqueues a failure via sub_CB9F80. */
            .http_transaction_error = 0xcded8c,
        },
        .patch = {
            /* purchase_online_gate -- the four `BL sub_247084` inside the
             * PurchaseBroker (sub_247084 is "GetNetworkStatus() is 1 or 2").
             * Every one of them gates a purchase step on connectivity and shows
             * [PURCHASE_ERROR_SERVICE_UNAVAILABLE_HEADER] when it fails; the
             * first is the one a player hits by pressing Buy.
             * The SAME helper is called from a dozen non-store places, which is
             * exactly why the call sites are patched and not the helper. */
            .purchase_online_gate = {
                0x72d320, /* in sub_72D264 -- start a single purchase       */
                0x72dc14, /* in sub_72DB98 -- "is the store offline?"        */
                0x72e0d8, /* in sub_72E0C0 -- restore purchases              */
                0x72e38c, /* in sub_72E384 -- any purchase still in flight?  */
            },
            /* The `MOV R1,#1` in sub_737DB8, whose only caller is
             * PurchaseBroker::OnPaymentComplete (sub_72EE30). R1=1 means "POST
             * this receipt to the validation server and wait for $.validation";
             * R1=0 takes sub_737DC0's finish-now branch. */
            .purchase_local_receipt = 0x737db8,
            /* The `MOV R0,#0` at the head of that finish-now branch
             * (loc_737EA4), whose STRB writes the record's "validated" byte
             * before it sets the state to 4=finished. 0 there is what makes the
             * broker (sub_7305D8) show "Unable to contact store" instead of
             * delivering; 1 is what sub_738F3C writes when a real server replies
             * "passed". */
            .purchase_receipt_verdict = 0x737ea4,
        },
        /* probe: the frame-gate field offsets are 9.6.1's; reading a 4.5.2 driver
         * at them printed plausible garbage, which is worse than printing
         * nothing. input: not mapped either. */
        .fingerprint_draw_frame    = 0xE59F1010E59F0010ull, /* LDR R0,[PC,#0x10]; LDR R1,[PC,#0x10] */
        .fingerprint_game_app_init = 0xE24DD084E92D4FF0ull, /* PUSH {R4-R11,LR}; SUB SP,SP,#0x84    */
        .game_app_init_args      = kGameAppInitArgsLegacy,
        .game_app_init_arg_count = kCount(kGameAppInitArgsLegacy),
    },

    /* --- 9.6.1 (PvZ2 Reflourished 1.3.1, armeabi-v7a, 30MB) ---------------
     *
     * The last Reflourished release that ships an armeabi-v7a build at all --
     * 1.4.2 is arm64-only, which this emulator cannot run. Both APKs are
     * versionCode 675, so main.675.com.ea.game.pvz2_rfl.obb pairs with either.
     *
     * First build with real DT_NEEDED dependencies: libc++_shared.so and
     * libNimble.so supply 119 of its 514 imports, so the loader maps them beside
     * libPVZ2.so rather than shimming them (see pvz2_elf_load).
     *
     * The natives were located by decoding the JNINativeMethod arrays whole. A
     * 12-byte table of pointers is self-similar under a 4-byte shift, so a
     * pattern scan reports every array three times with three different
     * apparent field orders and three different sets of function addresses --
     * the correct one is the one whose START ADDRESS is referenced by the code
     * that passes it to RegisterNatives. Three arrays, all {name, sig, fn},
     * separated by NULL words:
     *   0x1d0e960 (3) GameAppInitialize, GameAppTeardown, getGooglePlayAPIKey
     *   0x1d0e988 (1) createNativeApplicationLifecycleObserver
     *   0x1d0e998 (15) the lifecycle family
     *   0x1d0ea80 (6) the surface family + PumpMessageQueue
     *   0x1d10138 (5) the HTTP callbacks
     * Several lifecycle natives are one-instruction thunks -- 0x1277934 and
     * 0x1277938 are a bare `BX LR` -- which is why they sit 4 bytes apart and
     * looks like a misread but is not. */
    {
        .version = "9.6.1",
        .native = {
            .game_app_initialize                = 0x12768c4,
            .application_will_finish_launching  = 0x1277854,
            .application_did_finish_launching   = 0x1277938, /* a bare BX LR */
            .application_will_become_foreground = 0x1277944,
            .application_did_become_active      = 0x1277948,
            .on_surface_created                 = 0x127cc34,
            .on_surface_changed                 = 0x127ccd0,
            .on_draw_frame                      = 0x127cdcc,
            .pump_message_queue                 = 0x127cc24,
            .create_lifecycle_observer          = 0x1277000,
            .notify_surface_change              = 0x1277ad4, /* -> driver+326 */
            .notify_app_running                 = 0x1277ac0, /* -> driver+325 */
            .notify_focus_change                = 0x1277aac, /* -> driver+324 */
        },
        .surface_changed_pad = 1, /* body does MOV R5,R2 / MOV R4,R3 -- plain
                                   * static native, confirmed by disassembly,
                                   * as does Native_onOrientationChanged which
                                   * forwards r2/r3 straight into r0/r1 */
        .global = {
            /* LawnApp: the pointer sub_FC2A74 (the body of
             * Native_applicationWillFinishLaunching) `operator new`s and stores
             * before running the framework. Worth having beyond diagnostics:
             * the AndroidAppDriver lives at *(app + 8) -- SexyAppBase's
             * constructor puts it there (0x10d144c: BL ctor; STR r0,[r4,#8]) --
             * so this global reaches the driver even while the driver's OWN
             * global is still unpublished. */
            .app = 0x1d92a94,
            /* AndroidAppDriver: onDrawFrame and all three Native_Notify* natives
             * reach it through this one GOT slot (LDR r0,=off; LDR r0,[pc,r0]). */
            .app_driver = 0x1d9ce44,
        },
        .jni_native = {
            /* HttpTransactionError, 4th of the 5-entry HTTP array. */
            .http_transaction_error = 0x129eb68,
        },
        /* patch: not mapped -- the purchase-broker rewrites are 4.5.2 addresses
         * and mean nothing here. The store simply behaves as the engine's own
         * offline path dictates until they are found for this build. */
        .probe = {
            /* Byte flags read off onDrawFrame's body (0x12853e0) and
             * HandleApplicationDidBecomeActive (0x1281914). */
            .gate_skip_frame = 316,
            .gate_focus      = 324,
            .gate_running    = 325,
            .gate_surface    = 326,
            /* SexyAppBase's ctor: BL <driver ctor>; STR r0,[r4,#8] at 0x10d144c. */
            .app_driver_field = 8,
            /* From the {offset_to_top, typeinfo} header preceding it, typeinfo
             * RTTI name "N4Sexy16AndroidAppDriverE". */
            .driver_vtable = 0x1d0eb90,
            .publish_slot  = 0x1d0ecdc,
            .publish_thunk = 0x128277c, /* stores `this` into the driver global */
            /* Sexy::AndroidAsyncIOFileDriver, created and stored by SexyAppBase's
             * constructor ~20 lines before it builds the app driver. Named from
             * the RTTI its vtable (0x1d0fddc) points at. */
            .file_driver = 0x1d96db0,
        },
        /* input: not mapped for this build. */
        .fingerprint_draw_frame    = 0xE79F0000E59F0004ull, /* LDR R0,[PC,#4]; LDR R0,[PC,R0]     */
        .fingerprint_game_app_init = 0xE28DB01CE92D4FF0ull, /* PUSH {R4-R11,LR}; ADD R11,SP,#0x1C */
        .game_app_init_args      = kGameAppInitArgs961,
        .game_app_init_arg_count = kCount(kGameAppInitArgs961),
    },
};

/* The table the rest of the port reads. A COPY of the matched kVersions entry
 * rather than a pointer into it, because detection may fill gaps in it from the
 * scan: kVersions itself stays const and stays the human-maintained record of
 * what was read out of a disassembler. */
GameSymbols g_resolved = kUnknown;
const GameSymbols *g_active = &kUnknown;

/* Storage for a table built by scanning, whose strings no static array owns.
 * File-scope and never cleared: g_resolved points into it for the life of the
 * process, which is also the life of the loaded image. */
std::string g_auto_version;
std::vector<std::string> g_auto_arg_storage;
std::vector<const char *> g_auto_arg_ptrs;

/* --- the field map ----------------------------------------------------------
 *
 * Every scalar offset in GameSymbols, by the dotted name the report prints and
 * game_symbols_lookup accepts. It is what lets the report enumerate what a build
 * is missing without a hand-written list that drifts out of date, and what lets
 * a mod ask for "native.on_draw_frame" instead of an address.
 *
 * Adding a field to GameSymbols and forgetting to add it here costs only its
 * line in the report -- nothing reads the table for correctness -- but the
 * report is the thing someone porting a version works from, so a field missing
 * from it is a field nobody knows to fill in. */
struct Field {
    const char *name;
    std::size_t offset;

    /* The name the native is registered under in the JNINativeMethod arrays,
     * and any alternative spellings a release has used. nullptr for a field
     * that is not a registered native (globals, patch sites, probe offsets),
     * which is exactly the set the scan cannot recover.
     *
     * Recorded here rather than in a second table so a field can never have a
     * JNI name in one place and not the other. */
    const char *jni_names[3];

    /* A native without which the port cannot run at all. A build missing one of
     * these is not supportable by scanning alone, and detection says so instead
     * of booting into a call through 0. */
    bool required;
};

#define PVZ2_FIELD(path) #path, offsetof(GameSymbols, path)

const Field kFields[] = {
    /* The natives. Every name here was read off a JNINativeMethod table in a
     * shipped build, never guessed -- see the per-version notes above. */
    {PVZ2_FIELD(native.game_app_initialize), {"Native_GameAppInitialize"}, true},
    {PVZ2_FIELD(native.application_will_finish_launching),
     {"Native_applicationWillFinishLaunching"}, true},
    {PVZ2_FIELD(native.application_did_finish_launching),
     {"Native_applicationDidFinishLaunching"}, false},
    {PVZ2_FIELD(native.application_will_become_foreground),
     {"Native_applicationWillBecomeForeground"}, false},
    {PVZ2_FIELD(native.application_did_become_active), {"Native_applicationDidBecomeActive"},
     false},
    {PVZ2_FIELD(native.on_surface_created), {"Native_onSurfaceCreated"}, true},
    {PVZ2_FIELD(native.on_surface_changed), {"Native_onSurfaceChanged"}, true},
    {PVZ2_FIELD(native.on_draw_frame), {"Native_onDrawFrame"}, true},
    {PVZ2_FIELD(native.pump_message_queue), {"Native_PumpMessageQueue"}, false},
    {PVZ2_FIELD(native.create_lifecycle_observer),
     {"Native_createNativeApplicationLifecycleObserver",
      "Native_CreateNativeApplicationLifecycleObserver"},
     false},
    {PVZ2_FIELD(native.notify_surface_change), {"Native_NotifySurfaceChange"}, false},
    {PVZ2_FIELD(native.notify_app_running), {"Native_NotifyAppRunning"}, false},
    {PVZ2_FIELD(native.notify_focus_change), {"Native_NotifyFocusChange"}, false},

    {PVZ2_FIELD(surface_changed_pad), {nullptr}, false},

    {PVZ2_FIELD(global.app), {nullptr}, false},
    {PVZ2_FIELD(global.app_driver), {nullptr}, false},

    {PVZ2_FIELD(fn.string_ctor), {nullptr}, false},

    /* A native the PORT calls, so it is in the tables like any other and the
     * scan finds it -- the direction of the call makes no difference to how it
     * is registered. */
    {PVZ2_FIELD(jni_native.http_transaction_error), {"Native_HttpTransactionError"}, false},

    {PVZ2_FIELD(patch.purchase_local_receipt), {nullptr}, false},
    {PVZ2_FIELD(patch.purchase_receipt_verdict), {nullptr}, false},

    {PVZ2_FIELD(probe.gate_skip_frame), {nullptr}, false},
    {PVZ2_FIELD(probe.gate_focus), {nullptr}, false},
    {PVZ2_FIELD(probe.gate_running), {nullptr}, false},
    {PVZ2_FIELD(probe.gate_surface), {nullptr}, false},
    {PVZ2_FIELD(probe.app_driver_field), {nullptr}, false},
    {PVZ2_FIELD(probe.driver_vtable), {nullptr}, false},
    {PVZ2_FIELD(probe.publish_slot), {nullptr}, false},
    {PVZ2_FIELD(probe.publish_thunk), {nullptr}, false},
    {PVZ2_FIELD(probe.file_driver), {nullptr}, false},

    {PVZ2_FIELD(input.driver), {nullptr}, false},
    {PVZ2_FIELD(input.scaler), {nullptr}, false},
    {PVZ2_FIELD(input.touch_begin), {nullptr}, false},
    {PVZ2_FIELD(input.touch_end), {nullptr}, false},
    {PVZ2_FIELD(input.touch_stride), {nullptr}, false},
    {PVZ2_FIELD(input.multitouch), {nullptr}, false},
    {PVZ2_FIELD(input.touch_active), {nullptr}, false},
    {PVZ2_FIELD(input.scaler_fields), {nullptr}, false},
    {PVZ2_FIELD(input.vt_touch_down), {nullptr}, false},
    {PVZ2_FIELD(input.vt_touch_up), {nullptr}, false},
    {PVZ2_FIELD(input.vt_touch_move), {nullptr}, false},
};

#undef PVZ2_FIELD

std::uint32_t &field_of(GameSymbols &t, const Field &f) {
    return *(std::uint32_t *)((std::uint8_t *)&t + f.offset);
}

std::uint32_t field_of(const GameSymbols &t, const Field &f) {
    return *(const std::uint32_t *)((const std::uint8_t *)&t + f.offset);
}

/* The scanned entry for a field, by any of its accepted spellings. */
const scan::NativeMethod *scanned_for(const std::vector<scan::NativeMethod> &natives,
                                      const Field &f) {
    for (const char *name : f.jni_names) {
        if (name == nullptr) break;
        unsigned dupes = 0;
        const scan::NativeMethod *m = scan::find_native(natives, name, &dupes);
        if (m != nullptr) return m;
        if (dupes > 1) {
            std::printf("pvz2: [version] '%s' is registered at %u different addresses -- ignoring "
                        "it; %s must be filled in by hand\n", name, dupes, f.name);
        }
    }
    return nullptr;
}

/* Reads the 8 bytes at a .so offset as a little-endian u64, or 0 if the offset
 * lies outside the loaded image. */
std::uint64_t fingerprint_at(const pvz2_elf_image_t *img, std::uint32_t offset) {
    const std::uint64_t addr = (std::uint64_t)img->so_base + offset;
    if (offset == 0 || addr + 8 > img->mem_size) return 0;
    std::uint64_t v = 0;
    std::memcpy(&v, &img->mem[addr], 8);
    return v;
}

bool matches(const pvz2_elf_image_t *img, const GameSymbols &v) {
    return fingerprint_at(img, v.native.on_draw_frame) == v.fingerprint_draw_frame &&
           fingerprint_at(img, v.native.game_app_initialize) == v.fingerprint_game_app_init;
}

/* Splits a JNI signature's argument list into the object class names
 * Native_GameAppInitialize declares, in order -- the same list kVersions spells
 * out by hand as game_app_init_args.
 *
 * Only object arguments are collected, because that list names the fake Java
 * objects engine/lifecycle.cpp has to fabricate; a primitive argument would
 * have no object to stand for. A signature carrying one is therefore refused
 * outright rather than silently producing a list that is right in content and
 * wrong in REGISTER POSITION -- which is precisely the failure the field's
 * comment in symbols.h describes.
 *
 * The leading (JNIEnv*, jobject thiz) is not in the signature at all: JNI
 * signatures describe the Java-visible parameters only. */
bool parse_object_args(const std::string &sig, std::vector<std::string> &out) {
    const std::size_t close = sig.find(')');
    if (sig.empty() || sig[0] != '(' || close == std::string::npos) return false;
    for (std::size_t i = 1; i < close;) {
        if (sig[i] != 'L') return false; /* a primitive or an array: see above */
        const std::size_t semi = sig.find(';', i);
        if (semi == std::string::npos || semi > close) return false;
        out.push_back(sig.substr(i + 1, semi - i - 1));
        i = semi + 1;
    }
    return true;
}

/* Fills g_resolved's argument list from a scanned GameAppInitialize signature.
 * Returns false -- and leaves the list alone -- on a signature this cannot
 * safely read, so a build with an unusual one is reported rather than booted
 * with the wrong objects in the wrong registers. */
bool adopt_scanned_args(const scan::NativeMethod &init) {
    std::vector<std::string> names;
    if (!parse_object_args(init.signature, names) || names.empty()) {
        std::printf("pvz2: [version] Native_GameAppInitialize's signature '%s' is not a plain list "
                    "of objects -- game_app_init_args must be filled in by hand\n",
                    init.signature.c_str());
        return false;
    }
    g_auto_arg_storage = std::move(names);
    g_auto_arg_ptrs.clear();
    for (const std::string &n : g_auto_arg_storage) g_auto_arg_ptrs.push_back(n.c_str());
    g_resolved.game_app_init_args = g_auto_arg_ptrs.data();
    g_resolved.game_app_init_arg_count = (std::uint32_t)g_auto_arg_ptrs.size();
    return true;
}

/* Cross-checks the matched entry against the scan, and fills in any native it
 * leaves at 0. Advisory only: the entry has already proven itself against the
 * fingerprints, so a disagreement is reported and the TABLE wins. */
void reconcile_with_scan(const std::vector<scan::NativeMethod> &natives) {
    unsigned filled = 0, disagreed = 0;
    for (const Field &f : kFields) {
        if (f.jni_names[0] == nullptr) continue;
        const scan::NativeMethod *m = scanned_for(natives, f);
        if (m == nullptr) continue;
        std::uint32_t &slot = field_of(g_resolved, f);
        if (slot == 0) {
            slot = m->fn;
            ++filled;
            std::printf("pvz2: [version] %s was unmapped; the registered-natives scan found %s at "
                        "0x%x -- using it\n", f.name, m->name.c_str(), m->fn);
        } else if (slot != m->fn) {
            ++disagreed;
            std::printf("pvz2: [version] %s says 0x%x but %s is registered at 0x%x -- keeping the "
                        "table's value; one of the two is wrong for this binary\n",
                        f.name, slot, m->name.c_str(), m->fn);
        }
    }
    if (filled != 0 || disagreed != 0) std::fflush(stdout);
}

/* Prints an entry someone can paste into kVersions and edit, rather than making
 * them transcribe 13 addresses out of the log by hand. This is the whole payoff
 * of the scan: adding a version becomes reviewing a generated entry instead of
 * producing one. */
void print_paste_ready_entry(const pvz2_elf_image_t *img,
                             const std::vector<scan::NativeMethod> &natives) {
    std::printf("pvz2: [version] --- paste into kVersions in src/game/symbols.cpp and edit ---\n");
    std::printf("    {\n        .version = \"%s\",\n        .native = {\n", g_resolved.version);
    for (const Field &f : kFields) {
        if (f.jni_names[0] == nullptr) continue;
        const std::uint32_t v = field_of(g_resolved, f);
        if (v == 0) continue;
        /* The dotted name minus its "native."/"jni_native." prefix, so the line
         * is already in designated-initialiser form for the right sub-struct. */
        const char *dot = std::strchr(f.name, '.');
        if (std::strncmp(f.name, "native.", 7) != 0) continue;
        std::printf("            .%-34s = 0x%x,\n", dot + 1, v);
    }
    std::printf("        },\n        .surface_changed_pad = %u, /* VERIFY: read off "
                "Native_onSurfaceChanged's body */\n", g_resolved.surface_changed_pad);
    for (const Field &f : kFields) {
        if (std::strncmp(f.name, "jni_native.", 11) != 0) continue;
        const std::uint32_t v = field_of(g_resolved, f);
        if (v == 0) continue;
        std::printf("        .jni_native = { .%s = 0x%x },\n", std::strchr(f.name, '.') + 1, v);
    }
    std::printf("        .fingerprint_draw_frame    = 0x%016llXull,\n",
                (unsigned long long)fingerprint_at(img, g_resolved.native.on_draw_frame));
    std::printf("        .fingerprint_game_app_init = 0x%016llXull,\n",
                (unsigned long long)fingerprint_at(img, g_resolved.native.game_app_initialize));
    if (!g_auto_arg_storage.empty()) {
        std::printf("        /* Native_GameAppInitialize declares:\n");
        for (const std::string &n : g_auto_arg_storage) std::printf("         *   %s\n", n.c_str());
        std::printf("         * Add that array above and reference it here. */\n");
    }
    std::printf("        .game_app_init_args      = kGameAppInitArgs,\n"
                "        .game_app_init_arg_count = kCount(kGameAppInitArgs),\n    },\n");
    std::printf("pvz2: [version] --- end (%u JNINativeMethod entries were decoded in total) ---\n",
                (unsigned)natives.size());
    std::fflush(stdout);
}

/* Builds a table for a build kVersions has never seen, from the image alone.
 * Returns false when a REQUIRED native cannot be recovered -- at which point
 * nothing about this binary is known and booting would be a guess. */
bool resolve_by_scan(const pvz2_elf_image_t *img,
                     const std::vector<scan::NativeMethod> &natives) {
    g_resolved = kUnknown;

    char name[32];
    std::snprintf(name, sizeof(name), "auto:%08x", scan::image_digest(img));
    g_auto_version = name;
    g_resolved.version = g_auto_version.c_str();

    /* Both builds that carry a PumpMessageQueue are also the ones whose
     * onSurfaceChanged is a plain static native (pad 1); 1.6, the only build
     * with pad 2, has no such native. That correlation is not a law, so the
     * value is stated as a guess in the generated entry and in the log rather
     * than presented as read. */
    g_resolved.surface_changed_pad = 1;

    unsigned missing = 0;
    for (const Field &f : kFields) {
        if (f.jni_names[0] == nullptr) continue;
        const scan::NativeMethod *m = scanned_for(natives, f);
        if (m == nullptr) {
            if (f.required) {
                std::printf("pvz2: [version] required native %s (%s) is not in any "
                            "JNINativeMethod array in this image\n",
                            f.name, f.jni_names[0]);
                ++missing;
            }
            continue;
        }
        field_of(g_resolved, f) = m->fn;
    }
    if (missing != 0) return false;

    const scan::NativeMethod *init = scanned_for(natives, kFields[0]);
    if (init == nullptr || !adopt_scanned_args(*init)) return false;

    g_resolved.fingerprint_draw_frame = fingerprint_at(img, g_resolved.native.on_draw_frame);
    g_resolved.fingerprint_game_app_init =
        fingerprint_at(img, g_resolved.native.game_app_initialize);
    return true;
}

}  // namespace

const GameSymbols &sym() { return *g_active; }

std::uint32_t game_symbols_lookup(const char *dotted_name) {
    if (dotted_name == nullptr) return 0;
    for (const Field &f : kFields) {
        if (std::strcmp(f.name, dotted_name) == 0) return field_of(*g_active, f);
    }
    return 0;
}

void game_symbols_report() {
    std::printf("pvz2: [version] active table '%s':\n", g_active->version);
    unsigned unmapped = 0;
    for (const Field &f : kFields) {
        const std::uint32_t v = field_of(*g_active, f);
        if (v == 0) {
            ++unmapped;
            continue;
        }
        std::printf("pvz2: [version]   %-42s 0x%x\n", f.name, v);
    }
    if (unmapped == 0) {
        std::printf("pvz2: [version]   every field is mapped\n");
        std::fflush(stdout);
        return;
    }
    std::printf("pvz2: [version]   %u field(s) unmapped -- each is off, not broken:\n", unmapped);
    for (const Field &f : kFields) {
        if (field_of(*g_active, f) != 0) continue;
        std::printf("pvz2: [version]     %s%s\n", f.name,
                    f.jni_names[0] != nullptr ? " (a registered native; the scan did not find it)"
                                              : "");
    }
    std::fflush(stdout);
}

bool game_symbols_detect(const pvz2_elf_image_t *img) {
    if (img == nullptr) return false;

    /* Decoded once and shared by both paths below: on a 30 MB image this is a
     * single linear pass, and it is the input to the cross-check as well as to
     * the fallback. */
    const std::vector<scan::NativeMethod> natives = scan::find_registered_natives(img);

    for (const GameSymbols &v : kVersions) {
        if (!matches(img, v)) continue;
        g_resolved = v;
        g_active = &g_resolved;
        std::printf("pvz2: [version] libPVZ2.so identified as %s (onDrawFrame at 0x%x, verified)\n",
                    v.version, v.native.on_draw_frame);
        reconcile_with_scan(natives);
        if (pvz2_config()->verbose != 0) game_symbols_report();
        return true;
    }

    /* Not a version we know. Say exactly what was expected and what is
     * actually there, because that difference is the whole of the work needed
     * to add the build. */
    std::printf("pvz2: [version] this libPVZ2.so matches no known build (%u MB image, %u imports, "
                "%u registered natives)\n",
                (unsigned)(img->so_span >> 20), img->trampoline_count, (unsigned)natives.size());
    for (const GameSymbols &v : kVersions) {
        std::printf("pvz2: [version]   tried %-8s: onDrawFrame 0x%x expected %016llx, found %016llx\n",
                    v.version, v.native.on_draw_frame,
                    (unsigned long long)v.fingerprint_draw_frame,
                    (unsigned long long)fingerprint_at(img, v.native.on_draw_frame));
    }

    if (pvz2_config()->auto_version == 0) {
        std::printf("pvz2: [version] [game] auto_version is off, so this build is not run from a "
                    "scan; add an entry to kVersions in src/game/symbols.cpp\n");
        return false;
    }

    if (!resolve_by_scan(img, natives)) {
        std::printf("pvz2: [version] and its registered natives could not be recovered either -- "
                    "add an entry to kVersions in src/game/symbols.cpp to support it "
                    "(see docs/ADDING_A_VERSION.md)\n");
        return false;
    }

    g_active = &g_resolved;
    std::printf("pvz2: [version] running it anyway as '%s': every native below was read out of "
                "THIS binary's JNINativeMethod arrays, not guessed from a release number. "
                "Globals, guest-code patches and the frame/touch diagnostics stay unmapped -- "
                "each of those is off, not broken.\n", g_resolved.version);
    game_symbols_report();
    print_paste_ready_entry(img, natives);
    return true;
}

}  // namespace pvz2native
