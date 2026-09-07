#ifndef PVZ2NATIVE_GAME_HOOKS_H
#define PVZ2NATIVE_GAME_HOOKS_H

#include <cstdint>

#include <pvz2native/dependencies/dependency.h>
#include <pvz2native/elf32/elf32_loader.h>

namespace pvz2native {
namespace hooks {

/* Running host code in place of -- or around -- a guest function.
 *
 * game/patches.h already rewrites individual instructions, and that is the
 * right tool for "make this one branch go the other way". It cannot express
 * "run some C++ when the engine calls this, then let the engine carry on",
 * which is what a mod needs and what the port itself increasingly wants: the
 * alternative is a second copy of the engine's logic in the harness.
 *
 * The mechanism is the one every detour library uses, made simple by ARM32's
 * fixed 4-byte instructions and by the SVC trampolines this runtime already
 * has for imports:
 *
 *   1. The target's FIRST instruction is copied into an "island" -- a private
 *      block of guest memory below the image (memmap::kHookIslandBase) --
 *      followed by a branch back to the target's second instruction. Calling
 *      the island therefore runs the original function in full.
 *   2. That first instruction is replaced, in the loaded image, with a branch
 *      to a freshly minted SVC stub bound to the hook's handler.
 *
 * So a call to the hooked function traps to the host with the guest's registers
 * exactly as the function was entered, and the handler may read or rewrite the
 * arguments, call the original through the island (Hook::call_original), use or
 * discard its result, and set the value the caller receives.
 *
 * --- what this deliberately refuses ----------------------------------------
 *
 * Only a first instruction that is POSITION-INDEPENDENT can be relocated into
 * an island. `PUSH {r4-r11,lr}`, `SUB SP, SP, #imm`, `MOV Rd, Rm` and
 * `ADD R11, SP, #imm` -- between them the opening instruction of essentially
 * every function in these builds, and of all three natives whose prologues the
 * fingerprints in symbols.cpp record -- are fine. A PC-relative load, a branch
 * or anything writing PC is not: moved a megabyte away it would read or jump
 * somewhere else entirely, and the corruption would surface far from here. Such
 * a target is REFUSED, loudly, rather than hooked approximately.
 *
 * --- when hooks may be installed --------------------------------------------
 *
 * Startup only, before the first guest instruction runs. Two hard reasons, both
 * of them silent failures rather than crashes if ignored: dynarmic caches
 * translated basic blocks, so rewriting an instruction in a function that has
 * already executed changes nothing; and the import handler table is sized once
 * and then read by guest threads without a lock, so minting a stub later is a
 * data race on the buffer itself.
 */

/* One installed hook. The handler is given this so it can reach the original.
 *
 * Passed by const reference and safe to keep: hooks live for the process. */
struct Hook {
    std::uint32_t target;   /* .so offset of the hooked function      */
    std::uint32_t island;   /* guest address to call for the original */
    std::uint32_t stub;     /* guest address of the SVC stub          */
    const char *name;       /* for logging; owned by the caller       */

    /* Whatever the installer passed. The handler is a plain function pointer,
     * so this is how one handler serves many hooks -- the mod loader binds a
     * single dispatcher and puts the plugin's own callback here, instead of
     * needing a distinct host function per hook. Never dereferenced by this
     * layer; the installer owns whatever it points at and must outlive the
     * process. */
    void *user;

    /* Runs the original function with the given AAPCS argument list and returns
     * r0. Reentrant with respect to the hook itself -- the island holds a COPY
     * of the prologue and branches past the rewritten instruction, so the
     * handler is not re-entered.
     *
     * `c` must be the GuestCall the handler was given: the call runs on the
     * environment that is already executing. Passing no arguments at all
     * re-uses the registers as they arrived, which is what a hook that only
     * observes wants. */
    std::uint32_t call_original(GuestCall &c) const;
    std::uint32_t call_original(GuestCall &c, const std::uint32_t *args, int nargs) const;
};

/* What a hook handler is. `c` carries the guest registers and memory exactly as
 * the function was entered; whatever r0 holds when the handler returns is what
 * the engine's caller sees. */
using HookFn = void (*)(GuestCall &c, const Hook &hook);

/* Hooks the function at `offset` (a .so offset, as everything in GameSymbols
 * is). `name` labels it in the log and in traces, and is not copied -- pass a
 * literal or something that outlives the process.
 *
 * Returns false, having said exactly why, when the offset is outside the image,
 * when the first instruction cannot be relocated, when the trampoline or island
 * tables are full, or when that function is already hooked. Chaining two
 * handlers onto one function is not supported: the second would relocate an
 * instruction that is already a branch to the first, and the result would be a
 * loop. A mod that needs to co-operate with another mod's hook should call the
 * original from within it. */
bool install(pvz2_elf_image_t *img, std::uint32_t offset, const char *name, HookFn fn,
             void *user = nullptr);

/* Convenience over game_symbols_lookup: hooks whatever "native.on_draw_frame"
 * resolves to on the build that is actually loaded.
 *
 * This is what a mod should use. An unmapped or unknown name is a no-op that
 * says so and returns false -- a build whose table does not name that function
 * is not one this hook can be installed on, and refusing is better than hooking
 * offset 0. */
bool install_symbol(pvz2_elf_image_t *img, const char *dotted_symbol, HookFn fn,
                    void *user = nullptr);

/* How many hooks are installed, for the boot summary. */
unsigned installed_count();

}  // namespace hooks
}  // namespace pvz2native

#endif
