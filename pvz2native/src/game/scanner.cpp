/* Image scanning -- see include/pvz2native/game/scanner.h for what and why. */

#include <pvz2native/game/scanner.h>

#include <cctype>
#include <cstring>

namespace pvz2native {
namespace scan {
namespace {

/* The main image's byte range, as a host pointer plus a length. Every scan here
 * is over modules[0] only: a DT_NEEDED library is a different binary with its
 * own addresses, and an offset into it would mean nothing to kVersions. */
struct Span {
    const std::uint8_t *base = nullptr;
    std::uint32_t size = 0;
};

Span image_span(const pvz2_elf_image_t *img) {
    Span s;
    if (img == nullptr || img->mem == nullptr) return s;
    if ((std::uint64_t)img->so_base + img->so_span > img->mem_size) return s;
    s.base = img->mem + img->so_base;
    s.size = img->so_span;
    return s;
}

/* True when `offset` names `size` readable bytes inside the main image. */
bool in_image(const pvz2_elf_image_t *img, std::uint32_t offset, std::uint32_t size) {
    const Span s = image_span(img);
    return s.base != nullptr && offset < s.size && size <= s.size - offset;
}

/* The R+X range, unbiased -- where code and string literals live. Used to
 * reject a JNINativeMethod candidate whose "function pointer" points at data,
 * which is most of what a shifted read produces. 0-sized (an image with no such
 * segment recorded) disables the check rather than rejecting everything. */
bool in_text(const pvz2_elf_image_t *img, std::uint32_t offset) {
    if (img->text_size == 0) return in_image(img, offset, 4);
    return offset >= img->text_vaddr && offset < img->text_vaddr + img->text_size;
}

/* One parsed pattern byte: a value, or "anything". */
struct PatByte {
    std::uint8_t value;
    bool wild;
};

/* Parses IDA-style "F0 4F ?? E9". Returns an empty vector on anything it does
 * not understand, which every caller treats as "no match" -- a malformed
 * pattern is a bug in the caller, and matching a mis-parsed prefix of it would
 * hide that bug behind a plausible address. */
std::vector<PatByte> parse_pattern(const char *pattern) {
    std::vector<PatByte> out;
    if (pattern == nullptr) return out;
    for (const char *p = pattern; *p != '\0';) {
        if (std::isspace((unsigned char)*p) != 0) { ++p; continue; }
        if (*p == '?') {
            ++p;
            if (*p == '?') ++p;
            out.push_back({0, true});
            continue;
        }
        int hi = -1, lo = -1;
        auto nyb = [](char c) -> int {
            if (c >= '0' && c <= '9') return c - '0';
            if (c >= 'a' && c <= 'f') return c - 'a' + 10;
            if (c >= 'A' && c <= 'F') return c - 'A' + 10;
            return -1;
        };
        hi = nyb(*p);
        if (hi < 0) return {};
        ++p;
        lo = nyb(*p);
        if (lo < 0) return {};
        ++p;
        out.push_back({(std::uint8_t)((hi << 4) | lo), false});
    }
    return out;
}

bool match_at(const Span &s, std::uint32_t at, const std::vector<PatByte> &pat) {
    for (std::size_t i = 0; i < pat.size(); ++i) {
        if (!pat[i].wild && s.base[at + i] != pat[i].value) return false;
    }
    return true;
}

/* A JNI method name: an identifier, possibly with the '$' inner classes use.
 * Deliberately strict -- this is the check that makes a shifted read fail. */
bool plausible_native_name(const char *s, std::size_t max) {
    std::size_t n = 0;
    if (!(std::isalpha((unsigned char)s[0]) != 0 || s[0] == '_')) return false;
    while (n < max && s[n] != '\0') {
        const char c = s[n];
        if (std::isalnum((unsigned char)c) == 0 && c != '_' && c != '$') return false;
        ++n;
    }
    return n >= 2 && n < max;
}

/* A JNI signature: "(args)ret". Cheap, and no shifted read survives it. */
bool plausible_signature(const char *s, std::size_t max) {
    if (s[0] != '(') return false;
    std::size_t n = 0;
    while (n < max && s[n] != '\0') {
        if (s[n] == ')') return n + 1 < max && s[n + 1] != '\0';
        ++n;
    }
    return false;
}

/* A guest address turned back into a main-image offset, or 0 if it does not
 * point into the main image at all. */
std::uint32_t to_offset(const pvz2_elf_image_t *img, std::uint32_t addr) {
    if (addr < img->so_base) return 0;
    const std::uint32_t off = addr - img->so_base;
    return off < img->so_span ? off : 0;
}

std::uint32_t read32(const Span &s, std::uint32_t at) {
    std::uint32_t v = 0;
    std::memcpy(&v, s.base + at, 4);
    return v;
}

}  // namespace

std::uint32_t find_pattern(const pvz2_elf_image_t *img, const char *pattern,
                           std::uint32_t from_offset) {
    const std::vector<std::uint32_t> hits = [&] {
        const Span s = image_span(img);
        const std::vector<PatByte> pat = parse_pattern(pattern);
        std::vector<std::uint32_t> out;
        if (s.base == nullptr || pat.empty() || pat.size() > s.size) return out;
        for (std::uint32_t at = from_offset; at + pat.size() <= s.size; ++at) {
            if (match_at(s, at, pat)) {
                out.push_back(at);
                break;
            }
        }
        return out;
    }();
    return hits.empty() ? 0 : hits[0];
}

std::vector<std::uint32_t> find_pattern_all(const pvz2_elf_image_t *img, const char *pattern,
                                            std::size_t limit) {
    std::vector<std::uint32_t> out;
    const Span s = image_span(img);
    const std::vector<PatByte> pat = parse_pattern(pattern);
    if (s.base == nullptr || pat.empty() || pat.size() > s.size) return out;
    for (std::uint32_t at = 0; at + pat.size() <= s.size && out.size() < limit; ++at) {
        if (match_at(s, at, pat)) out.push_back(at);
    }
    return out;
}

std::uint32_t find_string(const pvz2_elf_image_t *img, const char *text) {
    const Span s = image_span(img);
    if (s.base == nullptr || text == nullptr) return 0;
    const std::size_t n = std::strlen(text);
    if (n == 0 || n + 1 > s.size) return 0;
    for (std::uint32_t at = 0; at + n + 1 <= s.size; ++at) {
        if (s.base[at] == (std::uint8_t)text[0] && std::memcmp(s.base + at, text, n) == 0 &&
            s.base[at + n] == 0) {
            return at;
        }
    }
    return 0;
}

std::vector<std::uint32_t> find_word_refs(const pvz2_elf_image_t *img, std::uint32_t value,
                                          std::size_t limit) {
    std::vector<std::uint32_t> out;
    const Span s = image_span(img);
    if (s.base == nullptr || s.size < 4) return out;
    for (std::uint32_t at = 0; at + 4 <= s.size && out.size() < limit; at += 4) {
        if (read32(s, at) == value) out.push_back(at);
    }
    return out;
}

std::uint32_t decode_bl_target(const pvz2_elf_image_t *img, std::uint32_t offset) {
    if (!in_image(img, offset, 4)) return 0;
    const Span s = image_span(img);
    const std::uint32_t word = read32(s, offset);
    /* cond 1011 imm24, condition masked off so a conditional BL decodes too.
     * BLX (immediate) is 0xFA/0xFB with cond==0b1111 and is deliberately NOT
     * accepted: it switches to Thumb, and a Thumb target is not something the
     * callers of this can use as an ARM offset. */
    if ((word & 0x0F000000u) != 0x0B000000u) return 0;
    if ((word >> 28) == 0xFu) return 0;
    std::int32_t imm = (std::int32_t)(word << 8) >> 6; /* sign-extend imm24, then *4 */
    /* The ARM PC reads as the instruction address + 8. */
    const std::int64_t target = (std::int64_t)offset + 8 + imm;
    if (target < 0 || (std::uint64_t)target >= img->so_span) return 0;
    return (std::uint32_t)target;
}

std::vector<NativeMethod> find_registered_natives(const pvz2_elf_image_t *img) {
    std::vector<NativeMethod> out;
    const Span s = image_span(img);
    if (s.base == nullptr || s.size < 12) return out;

    for (std::uint32_t at = 0; at + 12 <= s.size; at += 4) {
        const std::uint32_t name_off = to_offset(img, read32(s, at));
        if (name_off == 0) continue;
        const std::uint32_t sig_off = to_offset(img, read32(s, at + 4));
        if (sig_off == 0) continue;
        const std::uint32_t fn_addr = read32(s, at + 8);

        /* The function pointer. RegisterNatives is given the ARM entry point
         * directly, so the low bit is a Thumb marker rather than part of the
         * address; both engine builds are A32 throughout, but masking it costs
         * nothing and keeps a Thumb-built future release readable. */
        const std::uint32_t fn_off = to_offset(img, fn_addr & ~1u);
        if (fn_off == 0 || !in_text(img, fn_off)) continue;

        const char *name = (const char *)s.base + name_off;
        const char *sig = (const char *)s.base + sig_off;
        if (!plausible_native_name(name, s.size - name_off)) continue;
        if (!plausible_signature(sig, s.size - sig_off)) continue;

        NativeMethod m;
        m.name = name;
        m.signature = sig;
        m.fn = fn_off;
        m.entry = at;
        out.push_back(std::move(m));
    }
    return out;
}

const NativeMethod *find_native(const std::vector<NativeMethod> &natives, const char *name,
                                unsigned *out_duplicates) {
    const NativeMethod *found = nullptr;
    unsigned count = 0;
    for (const NativeMethod &m : natives) {
        if (m.name != name) continue;
        /* The same native registered twice for the same function is not an
         * ambiguity -- some builds list a method in two class tables. Only a
         * name resolving to two different ADDRESSES is. */
        if (found != nullptr && found->fn == m.fn) continue;
        if (found == nullptr) found = &m;
        ++count;
    }
    if (out_duplicates != nullptr) *out_duplicates = count;
    return count == 1 ? found : nullptr;
}

std::uint32_t image_digest(const pvz2_elf_image_t *img) {
    const Span s = image_span(img);
    if (s.base == nullptr) return 0;
    /* FNV-1a over a sample of the image rather than all of it: 30 MB per launch
     * buys nothing here (see the header -- this names a build, it does not
     * verify one) and a stride keeps it under a millisecond. The size is folded
     * in so two builds that happen to agree at every sampled byte still differ. */
    std::uint32_t h = 2166136261u ^ s.size;
    constexpr std::uint32_t kStride = 251; /* prime, so it does not align with any structure */
    for (std::uint32_t at = 0; at < s.size; at += kStride) {
        h ^= s.base[at];
        h *= 16777619u;
    }
    return h;
}

}  // namespace scan
}  // namespace pvz2native
