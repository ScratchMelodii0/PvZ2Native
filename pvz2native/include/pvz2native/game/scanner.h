#ifndef PVZ2NATIVE_GAME_SCANNER_H
#define PVZ2NATIVE_GAME_SCANNER_H

#include <cstdint>
#include <string>
#include <vector>

#include <pvz2native/elf32/elf32_loader.h>

namespace pvz2native {
namespace scan {

/* Finding things inside the loaded guest image, by shape rather than by
 * address.
 *
 * This file is the counterweight to game/symbols.cpp. That table holds concrete
 * addresses and may only ever grow by hand; everything here derives an address
 * from something ABOUT the binary -- a byte pattern, a string literal, a
 * relocated pointer, the layout of a JNINativeMethod array -- and so works on a
 * build nobody has looked at yet. Nothing in this file contains an address of
 * its own, which is what keeps the "only symbols.cpp holds addresses" rule
 * intact: a scan RESULT is an address, a scan is not.
 *
 * Everything here speaks in .so OFFSETS (subtract img->so_base), the same unit
 * GameSymbols uses, so a result can be pasted straight into kVersions.
 *
 * All of it is read-only and side-effect free, so it is equally usable from the
 * detector, from a diagnostic dump, and from a mod that wants to find its own
 * function in a build the core has never heard of.
 */

/* --- byte patterns ---------------------------------------------------------
 *
 * The pattern syntax is IDA's, so a signature copied out of a disassembler
 * works verbatim: space-separated hex bytes, `?` or `??` for a wildcard byte.
 * e.g. "F0 4F 2D E9 ?? ?? 4D E2".
 *
 * Note the byte ORDER is memory order, not the instruction word an ARM
 * disassembler prints: `PUSH {R4-R11,LR}` is the word 0xE92D4FF0 and the bytes
 * "F0 4F 2D E9". The fingerprints in symbols.cpp are words, these are bytes;
 * mixing the two is the one easy mistake here. */

/* First match at or after `from_offset`, or 0 when there is none. Searches the
 * main image only (modules[0]) -- a dependency library is somebody else's
 * binary and its addresses are not what kVersions describes. */
std::uint32_t find_pattern(const pvz2_elf_image_t *img, const char *pattern,
                           std::uint32_t from_offset = 0);

/* Every match. `limit` caps the result so a pattern that is accidentally
 * generic cannot allocate hundreds of megabytes; hitting it is reported by the
 * caller, not here. */
std::vector<std::uint32_t> find_pattern_all(const pvz2_elf_image_t *img, const char *pattern,
                                            std::size_t limit = 64);

/* --- literals and references ---------------------------------------------- */

/* Offset of a NUL-terminated string literal whose contents are exactly `text`.
 * The single most durable anchor there is: engine log messages and JNI class
 * names survive recompilation far better than any code pattern. */
std::uint32_t find_string(const pvz2_elf_image_t *img, const char *text);

/* Every 4-byte-aligned word in the image whose value equals `value`.
 *
 * With `value` a guest ADDRESS (so_base + offset), this is a data cross
 * reference: literal pools, vtable slots, relocated pointers and the entries of
 * a JNINativeMethod array all show up. It is how you get from "I found the
 * string" to "here is the code that uses it". */
std::vector<std::uint32_t> find_word_refs(const pvz2_elf_image_t *img, std::uint32_t value,
                                          std::size_t limit = 64);

/* Decodes the ARM `BL` at `offset` and returns the offset it targets, or 0 if
 * the instruction there is not a BL. Conditional BLs decode too. */
std::uint32_t decode_bl_target(const pvz2_elf_image_t *img, std::uint32_t offset);

/* --- registered natives ----------------------------------------------------
 *
 * The reason this file exists. Every lifecycle entry point the port has to call
 * is registered with RegisterNatives from JNI_OnLoad and therefore appears in
 * NO symbol table -- which is what forces kVersions to hold raw offsets at all.
 * But the JNINativeMethod arrays that RegisterNatives is handed are ordinary
 * relocated data, and each entry is a {const char *name, const char *sig,
 * void *fn} triple whose three fields are individually recognisable. So the
 * natives CAN be recovered from an unknown build, by name, without a
 * disassembler.
 *
 * The known hazard, recorded in symbols.cpp for 9.6.1: a 12-byte table of
 * pointers is self-similar under a 4-byte shift, so a naive scan reports every
 * array three times with three different apparent field orders. The decoder
 * defeats that by validating the FIELDS -- a signature must start with '(' and
 * contain ')', a name must be a plausible JNI identifier, and fn must land in
 * this module's executable range -- which only one of the three alignments can
 * satisfy. */
struct NativeMethod {
    std::string name;      /* e.g. "Native_onDrawFrame"      */
    std::string signature; /* e.g. "(Landroid/...;II)V"      */
    std::uint32_t fn;      /* .so offset of the implementation */
    std::uint32_t entry;   /* .so offset of the JNINativeMethod entry itself */
};

/* Every JNINativeMethod entry in the main image, in address order.
 *
 * Entries are found individually rather than as arrays: an array is just a run
 * of adjacent entries, and treating them separately means a table this decoder
 * has never seen the shape of still yields its members. */
std::vector<NativeMethod> find_registered_natives(const pvz2_elf_image_t *img);

/* The one entry whose name is exactly `name`, or nullptr.
 *
 * Ambiguity is a failure, not a coin toss: if two entries share a name (the
 * same native registered for two classes) the caller is told rather than handed
 * whichever came first. `out_duplicates`, when non-null, receives how many
 * matched. */
const NativeMethod *find_native(const std::vector<NativeMethod> &natives, const char *name,
                                unsigned *out_duplicates = nullptr);

/* --- image identity -------------------------------------------------------- */

/* A cheap, stable digest of the image's executable bytes.
 *
 * Not a cryptographic hash and not meant to be: it exists so an unrecognised
 * build can be given a name to talk about ("auto:9f3c2a11") and so a bug report
 * can say WHICH unknown build without shipping the binary anywhere. Two
 * different builds colliding costs nothing -- the fingerprints in kVersions are
 * what actually gate booting. */
std::uint32_t image_digest(const pvz2_elf_image_t *img);

}  // namespace scan
}  // namespace pvz2native

#endif
