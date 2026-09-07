#ifndef PVZ2NATIVE_MODS_MOD_HOST_H
#define PVZ2NATIVE_MODS_MOD_HOST_H

#include <pvz2native/elf32/elf32_loader.h>
#include <pvz2native/runtime/guest_runtime.h>

namespace pvz2native {
namespace mods {

/* Discovering and loading mod packs. See docs/MODDING.md for the folder layout
 * and mod.ini reference; this header is only about WHEN it happens.
 *
 * A mod pack is one immediate subfolder of [mods] dir, with three optional
 * parts, each independent of the others:
 *
 *   assets/       files that stand in for game files (dependencies/vfs.h)
 *   mod.ini       metadata, and guest-code patches that are checked before
 *                 they are written and skipped with a reason if they do not fit
 *   *.dll/.so/.dylib exporting pvz2_mod_init -- a native plugin (mods/mod_api.h)
 *
 * The first needs nothing from the game build. The second and third are why
 * load_all runs where it does: patches and hooks may only be installed after
 * the version is known and before a single guest instruction has executed,
 * because dynarmic caches translated blocks and would keep running the code as
 * it was. That is a window of exactly one call in pvz2_session_start, which is
 * where this is called from.
 */

/* Loads every mod pack, in name order so a load order is predictable and can be
 * steered with a numeric prefix ("10-", "20-"). Later mods override earlier
 * ones for assets; for hooks and patches, first to claim a function wins and
 * the loser is told why.
 *
 * Never fails: a broken mod is reported and skipped. A mod folder that does not
 * exist is not an error either -- most installs have none. */
void load_all(pvz2_elf_image_t *img, GuestRuntime *rt);

/* How many packs loaded, for the boot summary. */
unsigned loaded_count();

}  // namespace mods
}  // namespace pvz2native

#endif
