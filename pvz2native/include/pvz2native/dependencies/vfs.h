#ifndef PVZ2NATIVE_DEPENDENCIES_VFS_H
#define PVZ2NATIVE_DEPENDENCIES_VFS_H

#include <cstdint>
#include <string>

#include <pvz2native/runtime/guest_runtime.h>

namespace pvz2native {

/* The guest filesystem view.
 *
 * SexyAppFramework's resource layer tags every lookup with a scheme prefix
 * ("ASSET:<path>") and assembles paths with Android separators before they
 * reach libc. On a device the AAssetManager layer consumes all of that; we have
 * no such layer, so translation happens here, in one place, for every libc
 * entry point that takes a path (fopen, open, stat, access, mkdir, unlink).
 *
 * Both the libc modules and the DEX hooks need these paths -- the hooks hand
 * the engine the .obb location it then opens through libc -- so they live here
 * rather than in either one.
 */
namespace vfs {

/* The real expansion file on this machine, and the same file as the engine
 * must SEE it. ResStreamsManager::LoadRSB (sub_867740) only uses a path
 * verbatim when it starts with '/'; anything else it treats as a relative
 * resource name and routes through the Java asset API. On a device the .obb is
 * /storage/.../main.7.<pkg>.obb -- absolute -- so it always takes the verbatim
 * branch. A Windows "E:/..." path does not, hence the leading slash, which
 * translate() strips back off on the way to the host. */
const char *obb_host_path();
const char *obb_guest_path();

/* --- mod overlays ----------------------------------------------------------
 *
 * A folder whose contents stand in for game files. Every path the guest opens
 * is looked for in the mounted overlays, most recently mounted first, before it
 * is resolved against the real game data -- so a mod that ships
 * assets/main.rsb replaces the .obb, and one that ships assets/<something>.dat
 * replaces that file, without either of them touching the original.
 *
 * The match is by FILE NAME, and by the last two path components when the mod
 * ships a folder structure: the guest's paths are Android absolute paths built
 * by the engine from a package name and a version code, so a mod cannot know
 * them ahead of time and matching them literally would make every mod
 * build-specific. So "<overlay>/main.rsb" and "<overlay>/properties/main.rsb"
 * both answer a guest asking for ".../properties/main.rsb", and the second wins
 * over the first when both exist.
 *
 * Mounting is startup-only: the list is read from guest threads on every path
 * translation and is never locked. */
void mount_overlay(const std::string &host_dir, const std::string &mod_name);
unsigned overlay_count();

/* Any path whose basename is "main.rsb" is redirected to the .obb (the engine
 * assembles that name itself from its resource folder), and reaching either
 * form sets rt->rsb_touched. A mounted overlay is consulted first and wins over
 * both. */
std::string translate(GuestRuntime *rt, std::string guest_path);

/* bionic/ARM open(2) flag bits -> host _O_* values. They differ for
 * O_CREAT/O_EXCL/O_TRUNC/O_APPEND; binary mode is always forced. */
int translate_open_flags(std::uint32_t guest_flags);

}  // namespace vfs
}  // namespace pvz2native

#endif
