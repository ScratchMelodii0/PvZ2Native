#ifndef PVZ2NATIVE_MODS_MOD_API_H
#define PVZ2NATIVE_MODS_MOD_API_H

#include <stddef.h>
#include <stdint.h>

#include <pvz2native/config.h>

#ifdef __cplusplus
extern "C" {
#endif

/* The interface a native plugin sees -- and the ONLY thing it may rely on.
 *
 * A plugin is an ordinary shared library (.dll / .so / .dylib) dropped in a mod
 * folder. The host loads it, looks up `pvz2_mod_init`, and hands it this table.
 * Everything a mod can do goes through the table rather than through the port's
 * C++ headers, so a plugin built against one release of PvZ2Native keeps working
 * against the next: the table is C, versioned, and append-only.
 *
 * It is also why a mod does not need a copy of this source tree to build --
 * this one header is self-contained apart from config.h.
 *
 * A plugin is host code with no sandbox: it can do anything this process can.
 * That is the point (a mod that could not touch guest memory could not mod
 * anything), and it is why [mods] plugins exists as a separate switch from the
 * asset and mod.ini paths, which cannot.
 */

/* Bumped when an EXISTING entry changes meaning or disappears. Appending to the
 * end of Pvz2ModApi does not bump it: a plugin built against an older, shorter
 * table never reads the new fields, and one built against a newer table is
 * refused by the check below. Compare it, do not assume it. */
#define PVZ2_MOD_API_VERSION 1

/* --- what a hook handler is given ------------------------------------------ */

/* One in-flight call into a hooked guest function. Opaque: its shape is the
 * harness's business, and the accessors below are the whole of what a plugin
 * may do with it. */
typedef struct Pvz2ModCall Pvz2ModCall;

/* One installed hook, as the handler sees it. Opaque for the same reason;
 * pass it back to call_original. */
typedef struct Pvz2ModHook Pvz2ModHook;

/* A hook handler. Registers arrive exactly as the guest function was entered,
 * and whatever result the handler leaves is what the engine's caller receives.
 * A handler that does not call the original REPLACES the function. */
typedef void (*Pvz2ModHookFn)(Pvz2ModCall *call, const Pvz2ModHook *hook, void *user);

typedef struct Pvz2ModApi {
    /* PVZ2_MOD_API_VERSION as the HOST was built with. A plugin must refuse to
     * run when this is smaller than the version it was built against -- the
     * host is older than the plugin and the fields it wants are not there. */
    uint32_t api_version;

    /* The detected game build, e.g. "4.5.2", "9.6.1", or "auto:9f3c2a11" for a
     * build recovered by scanning. A mod carrying build-specific addresses MUST
     * check this; one written against symbols and patterns need not. */
    const char *game_version;

    /* This mod's own folder, absolute. Where to read a mod's config or data
     * files from -- the process's working directory is not it. */
    const char *mod_dir;

    /* --- diagnostics ------------------------------------------------------ */

    /* printf-style, prefixed with the mod's name and interleaved safely with
     * the harness's own output. */
    void (*log)(const char *fmt, ...);

    /* --- finding things in the guest binary -------------------------------
     *
     * All offsets are .so offsets -- relative to the start of libPVZ2.so, the
     * same unit a disassembler shows and the symbol table stores. 0 always
     * means "not found"/"not mapped", never offset zero. */

    /* A field of the active symbol table by its dotted name, e.g.
     * "native.on_draw_frame" or "global.app_driver". THE way to name a guest
     * function portably: the core resolves it per version, so a mod written
     * against a name still works on a build released afterwards. */
    uint32_t (*symbol)(const char *dotted_name);

    /* A JNI native by the name it is registered under, e.g.
     * "Native_onDrawFrame", read out of this binary's JNINativeMethod arrays.
     * Reaches natives the symbol table does not name at all. */
    uint32_t (*native)(const char *jni_name);

    /* An IDA-style byte signature ("F0 4F 2D E9 ?? ?? 4D E2"), and the offset
     * of a NUL-terminated string literal. Together with word_ref below, these
     * are how a mod finds a function nobody has added to the table -- find the
     * log string, find the code that references it. */
    uint32_t (*find_pattern)(const char *ida_pattern);
    uint32_t (*find_string)(const char *text);
    /* The first 4-byte-aligned word in the image equal to `value`; with a guest
     * address as the value, a data cross-reference. */
    uint32_t (*word_ref)(uint32_t value);

    /* so_base: add it to an offset to get a guest address, subtract it to go
     * back. The memory accessors below take ADDRESSES, the hook and patch
     * entry points take OFFSETS, and this is the one conversion between them. */
    uint32_t so_base;

    /* --- changing what the guest does ------------------------------------- */

    /* Runs `fn` in place of the function at `offset`. `name` labels it in the
     * log; `user` is handed back to every invocation. Returns 0 and says why
     * on refusal -- see game/hooks.h for what cannot be hooked.
     *
     * Startup only, from pvz2_mod_init. Later is silently ineffective: the JIT
     * caches translated code, so a function that has already run keeps running
     * its old form. */
    int (*hook)(uint32_t offset, const char *name, Pvz2ModHookFn fn, void *user);

    /* Rewrites one 4-byte instruction, refusing unless the word already there
     * is `expect` -- so an address that belongs to a different build is
     * reported instead of corrupting this one. Pass expect == 0 to skip that
     * check, which is exactly as dangerous as it sounds. Startup only, for the
     * same reason as hook. */
    int (*patch_word)(uint32_t offset, uint32_t expect, uint32_t value);

    /* --- guest memory, by ADDRESS ------------------------------------------
     *
     * Bounds-checked: a read out of range returns 0 and a write is dropped, so
     * a mod cannot corrupt the host through these. Safe from a hook handler;
     * from anywhere else, remember guest threads are running. */
    uint32_t (*read32)(uint32_t guest_addr);
    void (*write32)(uint32_t guest_addr, uint32_t value);
    uint32_t (*read8)(uint32_t guest_addr);
    void (*write8)(uint32_t guest_addr, uint32_t value);
    /* Copies at most `max` bytes of a NUL-terminated guest string into `out`,
     * always NUL-terminating. Returns the number of bytes written. */
    uint32_t (*read_cstr)(uint32_t guest_addr, char *out, uint32_t max);

    /* --- inside a hook handler --------------------------------------------- */

    /* Argument i (0..3 are r0..r3; 4 and up come off the guest stack), and the
     * value the caller will receive. */
    uint32_t (*arg)(Pvz2ModCall *call, int index);
    void (*set_arg)(Pvz2ModCall *call, int index, uint32_t value);
    void (*set_result)(Pvz2ModCall *call, uint32_t value);

    /* Runs the function this hook replaced and returns its result. `args` may
     * be NULL, which re-uses the four argument registers as they arrived --
     * what an observing hook wants. A function taking arguments on the stack
     * must pass them explicitly: the original runs on a stack of its own. */
    uint32_t (*call_original)(Pvz2ModCall *call, const Pvz2ModHook *hook, const uint32_t *args,
                              int nargs);

    /* Calls any guest function, by ADDRESS, with an AAPCS argument list.
     * Only from inside a hook handler -- it needs an executing environment. */
    uint32_t (*call_guest)(Pvz2ModCall *call, uint32_t guest_addr, const uint32_t *args, int nargs);

    /* --- the rest of the host ---------------------------------------------- */

    /* The loaded config.ini, so a mod can read the same switches the core does
     * rather than inventing its own. */
    const pvz2_config_t *(*config)(void);
} Pvz2ModApi;

/* What a plugin must export.
 *
 * Called once, during startup, before any guest code runs -- which is the only
 * time hooks and patches can be installed. Return 0 to accept, non-zero to
 * decline (a plugin that finds itself on a game build it does not support
 * should decline; the host logs it and carries on with the other mods).
 *
 * `api` is valid for the life of the process.
 *
 * The minimum plugin:
 *
 *     #include <pvz2native/mods/mod_api.h>
 *     static const Pvz2ModApi *g;
 *     PVZ2_MOD_EXPORT int pvz2_mod_init(const Pvz2ModApi *api) {
 *         if (api->api_version < PVZ2_MOD_API_VERSION) return 1;
 *         g = api;
 *         g->log("hello from %s", api->game_version);
 *         return 0;
 *     }
 */
typedef int (*Pvz2ModInitFn)(const Pvz2ModApi *api);

#if defined(_WIN32)
#define PVZ2_MOD_EXPORT __declspec(dllexport)
#else
#define PVZ2_MOD_EXPORT __attribute__((visibility("default")))
#endif

#ifdef __cplusplus
}  /* extern "C" */
#endif

#endif
