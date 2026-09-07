/* Guest function hooks -- see include/pvz2native/game/hooks.h for the design. */

#include <pvz2native/game/hooks.h>

#include <pvz2native/game/symbols.h>
#include <pvz2native/runtime/guest_memmap.h>

#include <array>
#include <cstdio>
#include <cstring>
#include <utility>
#include <vector>

namespace pvz2native {
namespace hooks {
namespace {

namespace memmap = runtime::memmap;

struct Installed {
    Hook hook;
    HookFn fn = nullptr;
};

/* Every installed hook, indexed by island slot. Written only during startup
 * (see the header) and read from guest threads afterwards, which is why it is
 * a fixed-size array rather than something that reallocates: the reads happen
 * without a lock and a growing vector would move the elements under them. */
std::array<Installed, memmap::kHookIslandMax> g_hooks{};
unsigned g_count = 0;

std::uint32_t read_word(const pvz2_elf_image_t *img, std::uint32_t offset) {
    std::uint32_t w = 0;
    std::memcpy(&w, &img->mem[img->so_base + offset], 4);
    return w;
}

void write_word(pvz2_elf_image_t *img, std::uint32_t addr, std::uint32_t word) {
    std::memcpy(&img->mem[addr], &word, 4);
}

/* ARM `B <dest>`, as executed at `at`. The PC an ARM branch computes from reads
 * as the instruction's own address + 8. Returns 0 when the destination is out
 * of the +-32MB an imm24 can express, which every caller reports rather than
 * encoding a branch to the wrong place. */
std::uint32_t encode_b(std::uint32_t at, std::uint32_t dest) {
    const std::int64_t delta = (std::int64_t)dest - ((std::int64_t)at + 8);
    if ((delta & 3) != 0) return 0;
    if (delta > 33554428 || delta < -33554432) return 0;
    return 0xEA000000u | (std::uint32_t)((delta >> 2) & 0x00FFFFFFu);
}

/* May this instruction be executed from a different address than the one it was
 * assembled at? See the header for why the answer has to be conservative.
 *
 * Everything below is an A32 (never Thumb) encoding with the condition field
 * masked off, so a conditional form of an accepted instruction is accepted too. */
bool relocatable(std::uint32_t w) {
    if ((w >> 28) == 0xFu) return false; /* unconditional space: BLX imm, PLD, the SIMD block */

    const std::uint32_t rn = (w >> 16) & 0xF;
    const std::uint32_t rd = (w >> 12) & 0xF;
    const std::uint32_t rm = w & 0xF;

    /* STMDB SP!, {...} -- i.e. PUSH. Register-list only; it touches no PC and
     * no literal. A list CONTAINING pc (bit 15) is still fine to relocate, but
     * a store of pc records this instruction's address, so refuse it. */
    if ((w & 0x0FFF0000u) == 0x092D0000u) return (w & 0x8000u) == 0;

    /* Data processing, immediate operand: SUB SP,SP,#n / ADD R11,SP,#n /
     * MOV Rd,#n / CMP Rn,#n. Refused when either register is PC: `ADD Rd, PC,
     * #n` is how a function forms an address of itself. */
    if ((w & 0x0E000000u) == 0x02000000u) return rn != 15 && rd != 15;

    /* Data processing, register operand shifted by an immediate: MOV Rd,Rm /
     * ADD Rd,Rn,Rm. The register-shifted-register form (bit 4 set) is excluded
     * by the mask, and PC in any slot is refused for the same reason. */
    if ((w & 0x0E000010u) == 0x00000000u) return rn != 15 && rd != 15 && rm != 15;

    return false;
}

void dispatch(unsigned slot, GuestCall &c) {
    if (slot >= g_hooks.size()) return;
    const Installed &h = g_hooks[slot];
    if (h.fn == nullptr) return;
    h.fn(c, h.hook);
}

/* One distinct host function per island slot, so the SVC handler -- which is
 * handed nothing but the call itself -- still knows which hook it belongs to.
 * A shared handler cannot: the trampoline index it was reached through is not
 * part of GuestCall, and adding it there would change an interface every
 * dependency module implements for the sake of this one caller. */
template <std::size_t N>
void thunk(GuestCall &c) {
    dispatch((unsigned)N, c);
}

template <std::size_t... Ns>
constexpr std::array<ImportHandler, sizeof...(Ns)> make_thunks(std::index_sequence<Ns...>) {
    return {{&thunk<Ns>...}};
}

const auto kThunks = make_thunks(std::make_index_sequence<memmap::kHookIslandMax>{});

}  // namespace

std::uint32_t Hook::call_original(GuestCall &c) const {
    /* No explicit list: re-use the four argument registers as they arrived,
     * which is what an observing hook wants. A function taking arguments on the
     * stack needs the explicit overload -- the original runs on a nested stack
     * of its own, so the caller's stack arguments are not where it will look. */
    const std::uint32_t args[4] = {c.regs[0], c.regs[1], c.regs[2], c.regs[3]};
    return call_original(c, args, 4);
}

std::uint32_t Hook::call_original(GuestCall &c, const std::uint32_t *args, int nargs) const {
    if (island == 0) return 0;
    return c.call_guest(island, args, nargs);
}

bool install(pvz2_elf_image_t *img, std::uint32_t offset, const char *name, HookFn fn,
             void *user) {
    if (img == nullptr || fn == nullptr) return false;
    const char *label = name != nullptr ? name : "<unnamed>";

    if (offset == 0 || (std::uint64_t)img->so_base + offset + 4 > img->mem_size ||
        offset >= img->so_span) {
        std::printf("pvz2: [hook] %s: 0x%x is not inside the loaded image -- not hooked\n", label,
                    offset);
        return false;
    }
    if ((offset & 3) != 0) {
        std::printf("pvz2: [hook] %s: 0x%x is not 4-byte aligned, so it is not the start of an ARM "
                    "function -- not hooked\n", label, offset);
        return false;
    }
    for (unsigned i = 0; i < g_count; ++i) {
        if (g_hooks[i].hook.target == offset) {
            std::printf("pvz2: [hook] %s: 0x%x is already hooked by '%s' -- not hooked again; "
                        "chain from inside that handler instead\n",
                        label, offset, g_hooks[i].hook.name);
            return false;
        }
    }
    if (g_count >= memmap::kHookIslandMax) {
        std::printf("pvz2: [hook] %s: all %u hook slots are in use -- raise kHookIslandMax in "
                    "runtime/guest_memmap.h\n", label, (unsigned)memmap::kHookIslandMax);
        return false;
    }

    const std::uint32_t first = read_word(img, offset);
    if (!relocatable(first)) {
        std::printf("pvz2: [hook] %s: the first instruction at 0x%x is 0x%08x, which cannot be "
                    "moved (it is PC-relative, a branch, or writes PC) -- not hooked. Hook a "
                    "caller, or patch the instruction directly (game/patches.h)\n",
                    label, offset, first);
        return false;
    }

    const unsigned slot = g_count;
    const std::uint32_t island = memmap::kHookIslandBase + slot * memmap::kHookIslandStride;
    const std::uint32_t target_addr = img->so_base + offset;

    /* The island: the original instruction, then straight back into the body.
     * Both encodings are checked before ANYTHING is written -- a half-built
     * island reached through a rewritten entry would run one instruction and
     * fall into whatever follows it. */
    const std::uint32_t back = encode_b(island + 4, target_addr + 4);
    if (back == 0) {
        std::printf("pvz2: [hook] %s: 0x%x is too far from the hook islands to branch back "
                    "(more than 32MB) -- not hooked\n", label, offset);
        return false;
    }

    const std::uint32_t stub = make_guest_callback(label, kThunks[slot]);
    if (stub == 0) {
        std::printf("pvz2: [hook] %s: no SVC trampoline left -- raise PVZ2_TRAMPOLINE_MAX in "
                    "elf32/elf32_loader.h\n", label);
        return false;
    }
    const std::uint32_t entry = encode_b(target_addr, stub);
    if (entry == 0) {
        std::printf("pvz2: [hook] %s: 0x%x is too far from the trampoline table to branch to it "
                    "-- not hooked\n", label, offset);
        return false;
    }

    write_word(img, island, first);
    write_word(img, island + 4, back);
    write_word(img, target_addr, entry);

    g_hooks[slot].hook = Hook{offset, island, stub, label, user};
    g_hooks[slot].fn = fn;
    ++g_count;

    std::printf("pvz2: [hook] %s at 0x%x -> host (original reachable at 0x%08x)\n", label, offset,
                island);
    return true;
}

bool install_symbol(pvz2_elf_image_t *img, const char *dotted_symbol, HookFn fn,
                    void *user) {
    const std::uint32_t offset = game_symbols_lookup(dotted_symbol);
    if (offset == 0) {
        std::printf("pvz2: [hook] '%s' is not mapped for this build -- nothing hooked\n",
                    dotted_symbol != nullptr ? dotted_symbol : "(null)");
        return false;
    }
    return install(img, offset, dotted_symbol, fn, user);
}

unsigned installed_count() { return g_count; }

}  // namespace hooks
}  // namespace pvz2native
