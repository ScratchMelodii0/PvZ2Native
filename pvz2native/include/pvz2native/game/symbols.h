#ifndef PVZ2NATIVE_GAME_SYMBOLS_H
#define PVZ2NATIVE_GAME_SYMBOLS_H

#include <cstdint>

#include <pvz2native/elf32/elf32_loader.h>

namespace pvz2native {

/* Everything about the game binary that is not the same in every build.
 *
 * The harness itself -- the JIT, the libc/GLES/JNI shims, the heap, the audio
 * bridge -- is version-agnostic: it implements Android, and Android does not
 * change between two PvZ2 releases. What DOES change is every address inside
 * libPVZ2.so, and those used to be hex literals inlined at their use sites, so
 * supporting a second release meant editing the middle of the boot logic. They
 * all live here now, and adding a version is adding one entry to kVersions.
 *
 * The lifecycle natives are the reason raw offsets are unavoidable at all:
 * JNI_OnLoad registers them with RegisterNatives, so unlike the Java_* exports
 * they never appear in the dynamic symbol table and cannot be looked up by name.
 *
 * 0 means "not mapped for this version". Callers must treat it as such:
 * `natives` are required and a missing one is a hard error, while a missing
 * diagnostic offset just turns that diagnostic off.
 *
 * kVersions fills this in with DESIGNATED initialisers (.native = {...}), and a
 * new entry must too. Every field here is an interchangeable uint32 offset, so a
 * positional aggregate that omits one block -- or a member added in the middle of
 * one -- shifts everything after it with nothing for the compiler to complain
 * about; the symptom is a branch into the middle of an unrelated function. Naming
 * each field makes an omitted one simply absent, and absent means 0, which is
 * already the contract above. */
struct GameSymbols {
    const char *version;

    /* Registered natives -- offsets into the .so, called by run_at_offset. */
    struct {
        std::uint32_t game_app_initialize;
        std::uint32_t application_will_finish_launching;
        std::uint32_t application_did_finish_launching;
        std::uint32_t application_will_become_foreground;
        std::uint32_t application_did_become_active;
        std::uint32_t on_surface_created;
        std::uint32_t on_surface_changed;
        std::uint32_t on_draw_frame;

        /* PumpMessageQueue: drains the lifecycle event queue and dispatches it.
         *
         * 0 when the version has no such native. 1.6 does not: it drains the
         * queue at the top of onDrawFrame itself. 4.5.2 split the two apart, and
         * its onDrawFrame ONLY draws -- so failing to call this leaves every
         * applicationDidBecomeActive/WillBecomeForeground sitting in the queue
         * forever, the driver stuck in its paused state, and every frame taking
         * a fixed-cost branch that updates nothing. The window stays black with
         * no error anywhere; the giveaway is a frame that costs the same ticks
         * every time and makes zero imports. */
        std::uint32_t pump_message_queue;

        /* Native_createNativeApplicationLifecycleObserver: new in 9.6.1, 0 for
         * 1.6 and 4.5.2. It is registered in a JNINativeMethod array of its own,
         * so the Java side calls it explicitly during startup, and this build
         * moved work there -- its Native_applicationDidFinishLaunching is now a
         * bare `BX LR`, where 4.5.2's did something. Called right after
         * GameAppInitialize, which is where Java constructs the observer.
         *
         * If a 9.6.1 boot misbehaves in the lifecycle, this speculative call is
         * the first thing to try removing: the ORDER is inferred from the name
         * and from what the natives around it became, not read off a trace. */
        std::uint32_t create_lifecycle_observer;

        /* Native_NotifySurfaceChange / NotifyAppRunning / NotifyFocusChange,
         * each `(Z)V`. New in 9.6.1; 0 for 1.6 and 4.5.2, which have no such
         * natives. On Android the activity calls them from its surface and
         * focus callbacks -- so this port, which IS that activity, must too.
         *
         * They are not optional, and skipping them fails SILENTLY. Each writes
         * one byte into the app driver (surface -> +326, running -> +325,
         * focus -> +324, all zero from the driver's constructor).
         * HandleApplicationDidBecomeActive -- the handler PumpMessageQueue
         * dispatches for applicationDidBecomeActive -- opens with
         *
         *     if (surface == 0) return;
         *     if (running == 0 && focus == 0) return;
         *
         * and only past that guard does it clear the driver's "skip the frame"
         * byte at +316. onDrawFrame tests exactly that byte and, when set, takes
         * a branch that updates and draws nothing. So without these three the
         * engine boots perfectly, drains its queue, and then runs a fixed-cost
         * empty frame forever: measured as `0 imports, 0 jni, 1052 ticks`,
         * identical every frame, with a black window and no error anywhere.
         * The giveaway in the log is a MISSING line -- "HandleApplicationWill-
         * BecomeForeground" appears, "HandleApplicationDidBecomeActive" does
         * not, because that debug print sits after the guard. */
        std::uint32_t notify_surface_change;
        std::uint32_t notify_app_running;
        std::uint32_t notify_focus_change;
    } native;

    /* How many dummy words precede (width, height) in the onSurfaceChanged
     * call -- i.e. how many of r1..r3 the JNI preamble eats before the real
     * arguments start.
     *
     * A per-version value because the two builds genuinely differ, and getting
     * it wrong is silent: the engine reads whichever registers it expects and
     * derives its whole projection matrix from them, so a shift by one register
     * produces a plausible-looking window with a poisoned transform rather than
     * a crash. 4.5.2's is the textbook static-native shape -- its body reads r2
     * and r3 -- and was read off the decompilation, not assumed. */
    std::uint32_t surface_changed_pad;

    /* Engine globals, as offsets into the .so. `app` is the LawnApp the
     * SexyAppFramework constructor stores (dword_D55650 in 1.6); `app_driver`
     * is the separate AndroidAppDriver pointer the surface/frame natives
     * actually dereference (dword_DC8FD4) -- they are NOT the same object, and
     * assuming they were cost a long hunt once already. */
    struct {
        std::uint32_t app;
        std::uint32_t app_driver;
    } global;

    /* Guest functions the harness calls directly rather than through a native. */
    struct {
        /* libstdc++ `string(const char*, const allocator&)`, for handing the
         * engine a real std::string argument. */
        std::uint32_t string_ctor;
    } fn;

    /* Engine natives the PORT calls back INTO -- the reverse of `native`. These
     * are the C++ implementations behind Java `native` methods, registered by
     * RegisterNatives, that a Java class fires as a completion callback. This
     * port IS that Java class (see dex/hooks/), so it has to invoke them itself.
     *
     * `http_transaction_error` is AndroidHttpTransaction's error callback. It
     * takes (JNIEnv*, jobject thiz, jlong nativeTransaction) and merely ENQUEUES
     * a failure message onto the same ring buffer PumpMessageQueue drains -- so
     * it is cheap and safe to call. Without it, every HTTP request the engine
     * starts hangs forever instead of failing: on a device the Java side times
     * out after 30s and fires this; here Start() does nothing, so the transaction
     * never completes and the loading screen that waits on it never finishes.
     * 0 when not mapped (1.6, which already reaches the menu, is left untouched). */
    struct {
        std::uint32_t http_transaction_error;
    } jni_native;

    /* Instructions the port rewrites in the loaded image -- see game/patches.h,
     * which explains why one exists at all. Addresses only; the rewriting and
     * its verification live there. */
    struct {
        /* Every call to the engine's "is the device online?" helper made from
         * INSIDE the purchase broker, as offsets into the .so. Each is a 4-byte
         * ARM `BL` that is rewritten to `MOV R0, #1`, which is what makes an
         * emulated store usable on a port with no network. 0 ends the list, and
         * an all-zero list simply patches nothing. */
        std::uint32_t purchase_online_gate[8];

        /* The receipt verdict, which takes two instructions because the engine
         * only ever reaches a "purchase finished" state through its validation
         * server. Both are `MOV Rd, #imm` and 0 means not mapped.
         *
         * A completed payment is put on the broker's pending list, and the broker
         * hands the item over only once that record reads (finished, validated).
         * Only the server sets validated: the HTTP reply's "$.validation" must
         * say "passed". This port has no network, so left alone the purchase
         * hangs on "Purchasing Stuff..." forever waiting for that reply.
         *
         *   local_receipt  -- the `MOV R1, #1` in the thunk whose only caller is
         *                     OnPaymentComplete, meaning "POST the receipt and
         *                     wait". Rewritten to `MOV R1, #0`: finish now.
         *   receipt_verdict-- the `MOV R0, #0` in that finish-now branch, which
         *                     writes "not validated". Rewritten to `MOV R0, #1`.
         * Together they are exactly the outcome of a server answering "passed",
         * without the round trip. Patching only the first ends at the engine's
         * "Unable to contact store" dialog, which is that branch being honest. */
        std::uint32_t purchase_local_receipt;
        std::uint32_t purchase_receipt_verdict;
    } patch;

    /* What diagnostics/guest_probe reads to explain a frame that draws nothing.
     * All optional: zeroed out, dump_frame_gate reports what it can and skips
     * the rest. These were 9.6.1 literals inside guest_probe.cpp, which put .so
     * addresses outside this file -- the one thing symbols.cpp promises not to
     * allow -- and which the probe then applied to whatever build was loaded:
     * 4.5.2 has a non-zero app_driver global, so it ran and printed 9.6.1's
     * field offsets against a 4.5.2 object. Plausible numbers, all meaningless. */
    struct {
        /* Byte flags in the AndroidAppDriver that gate whether a frame draws.
         * Read off onDrawFrame's body and HandleApplicationDidBecomeActive. */
        std::uint32_t gate_skip_frame;
        std::uint32_t gate_focus;
        std::uint32_t gate_running;
        std::uint32_t gate_surface;

        /* Where the driver sits inside the app object: SexyAppBase's constructor
         * stores it there, so *(app + this) finds the driver even while the
         * driver's own global is still unpublished -- which is exactly the state
         * the probe exists to explain. */
        std::uint32_t app_driver_field;

        /* Sexy::AndroidAppDriver's vtable, the slot holding the thunk that
         * publishes `this` into the driver global, and that thunk itself.
         *
         * The vtable base is derived by hand from the {offset_to_top, typeinfo}
         * header, and a plausible-but-wrong base makes every slot index wrong
         * while still looking right -- so the probe verifies it against the live
         * object's vptr instead of trusting the arithmetic. That check is only
         * possible because all three are given together. */
        std::uint32_t driver_vtable;
        std::uint32_t publish_slot;
        std::uint32_t publish_thunk;

        /* The Sexy::AndroidAsyncIOFileDriver global, and THE bisector for a boot
         * that never publishes the app driver.
         *
         * SexyAppBase's constructor creates this object and stores it here, then
         * -- a few instructions later, in the same constructor -- creates the
         * AndroidAppDriver at app + app_driver_field. So the two together say
         * exactly how far that constructor got, which is the one thing a black
         * first frame does not otherwise reveal:
         *
         *   this 0, driver 0 -- the constructor died BEFORE either, i.e. inside
         *                       the base-class chain above SexyAppBase;
         *   this != 0, drv 0 -- it died between the two, a span of ~20 lines;
         *   both != 0        -- the constructor finished, so the failure is
         *                       downstream of it and nothing in the app-
         *                       construction tree is to blame.
         *
         * Worth reading even when everything works: the framework's own
         * "is there a resources.xml?" probe, which runs immediately after the
         * constructor returns, dereferences this global with NO null check. A
         * constructor that died leaves it 0 and that probe then walks a vtable
         * of zeroes -- so a failure anywhere above reappears as the executed
         * void, several frames from its cause. */
        std::uint32_t file_driver;
    } probe;

    /* Field offsets for the touch diagnostic -- see diagnostics/input_probe.
     * All optional: zeroed out, the probe simply reports nothing. */
    struct {
        std::uint32_t driver;        /* app_driver value -> driver          */
        std::uint32_t scaler;        /* driver -> coordinate scaler         */
        std::uint32_t touch_begin;   /* active-touch vector begin           */
        std::uint32_t touch_end;     /* active-touch vector end             */
        std::uint32_t touch_stride;  /* bytes per entry in that vector      */
        std::uint32_t multitouch;    /* byte flag: multitouch enabled       */
        std::uint32_t touch_active;  /* byte flag: touch dispatch active    */
        std::uint32_t scaler_fields; /* first of 8 int32s in the scaler     */
        std::uint32_t vt_touch_down; /* driver vtable slots, for naming the */
        std::uint32_t vt_touch_up;   /* concrete handlers in the log        */
        std::uint32_t vt_touch_move;
    } input;

    /* First 8 bytes at native.on_draw_frame and native.game_app_initialize.
     * Detection compares these rather than trusting a file size: if they match,
     * the two most important offsets in the table are proven right on this exact
     * binary, and a mismatch is caught before we branch into the middle of some
     * unrelated function. */
    std::uint64_t fingerprint_draw_frame;
    std::uint64_t fingerprint_game_app_init;

    /* The object arguments Native_GameAppInitialize declares, in order, NOT
     * counting the leading (JNIEnv*, jobject thiz): thiz is always
     * AndroidGameApp and the caller supplies it.
     *
     * Per-version because the list genuinely changed. 1.6 and 4.5.2 declare
     * eight and pass an AndroidFacebookDriver third; 9.6.1 declares seven and
     * has no Facebook driver at all. Reusing the old list on 9.6.1 would shift
     * cloud, GooglePlay* and notification each down one register -- and this is
     * the exact mistake that once made the engine call
     * Graphics_GetScreenSizeInPixels on the wrong fake object, corrupting the
     * screen size instead of failing. Read it off the JNI signature string in
     * the JNINativeMethod table, never guessed.
     *
     * Every version must fill this in. It used to be optional, with nullptr
     * meaning "the historical eight" and engine/lifecycle.cpp holding that list
     * as a fallback -- which put per-version data outside this file, against the
     * rule at the top of symbols.cpp, and duplicated seven of the eight names. */
    const char *const *game_app_init_args;
    std::uint32_t game_app_init_arg_count;
};

/* thiz, the leading jobject every build's Native_GameAppInitialize takes and
 * which therefore is not in the per-version lists above. */
extern const char *const kGameAppClass;

/* Identifies the loaded image and prepares the active table.
 *
 * Three outcomes, in the order they are tried:
 *
 *   1. A kVersions entry whose two fingerprints both match. This is the only
 *      outcome that proves an entry's addresses belong to THIS binary, so it
 *      always wins, and everything the entry maps is used verbatim.
 *
 *   2. Nothing matches, but the JNINativeMethod arrays in the image yield every
 *      REQUIRED native by name (see game/scanner.h). The addresses then come
 *      from the binary itself rather than from a guess about which release it
 *      is, so the boot is allowed to proceed under a synthetic version name
 *      ("auto:9f3c2a11"), and a paste-ready kVersions skeleton is printed. Only
 *      the natives, the GameAppInitialize argument list and the fingerprints can
 *      be recovered this way; globals, patches and diagnostics stay unmapped,
 *      which each of their consumers already treats as "off".
 *      [game] auto_version = 0 turns this off and restores the old behaviour.
 *
 *   3. Neither. Returns false, having logged every candidate it tried and what
 *      it found instead -- the report someone adding a version needs. Booting
 *      anyway would mean calling whatever happens to sit at another build's
 *      offsets, so the caller must refuse to start.
 *
 * On outcome 1 the scan still runs, in a purely advisory role: a native the
 * entry leaves at 0 is filled in from it, and one whose address DISAGREES with
 * the table is reported. A disagreement means the entry is describing a
 * different build than the fingerprints admitted to, which is worth hearing
 * about immediately rather than as a crash later. */
bool game_symbols_detect(const pvz2_elf_image_t *img);

/* Prints every field of the active table, mapped and unmapped alike.
 *
 * The unmapped half is the useful half: it is exactly the list of work left to
 * do for this build, and each line names the field the way game_symbols_lookup
 * and the mod API spell it. Called after detection when [log] verbose is on. */
void game_symbols_report();

/* One field of the active table, by the dotted name the report prints
 * ("native.on_draw_frame", "global.app_driver", "fn.string_ctor", ...).
 *
 * This is how a mod names a guest function without naming an address: the core
 * resolves it per version, and a mod written against "native.on_draw_frame"
 * keeps working on a build that came out afterwards. Returns 0 for an unknown
 * name and for a field this version does not map -- the same 0 that means
 * "unmapped" everywhere else, and callers must treat it as such.
 *
 * Only the scalar offset fields are addressable; the argument lists and the
 * fingerprints are not, having no meaning as an address. */
std::uint32_t game_symbols_lookup(const char *dotted_name);

/* The detected table. Before a successful detect it is all zeroes with
 * version "unknown", so a stray read cannot hand out a plausible-looking
 * address. */
const GameSymbols &sym();

}  // namespace pvz2native

#endif
