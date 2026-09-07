# Modding PvZ2Native

A mod is a folder. Drop it in `mods/` next to the executable and it loads on the
next launch.

```
mods/
  10-my-mod/
    mod.ini              metadata, and guest-code patches
    assets/              files that stand in for game files
      main.rsb
    my_mod.dll           a native plugin (.so on Linux, .dylib on macOS)
```

All three parts are optional and independent. A pack with only an `assets/`
folder needs no code and no addresses; a pack with only a `mod.ini` needs no
compiler. Mods load in **name order**, so a numeric prefix gives you a
predictable load order.

Two config switches, both in `config.ini`:

```ini
[mods]
dir = mods      ; where packs live (default <exe folder>/mods)
plugins = 1     ; load native plugin libraries
```

`plugins = 0` still applies asset overrides and `mod.ini` patches. It only
refuses to execute anybody's code -- **a native plugin is host code with no
sandbox of any kind and can do anything this program can.** Install plugins from
people you trust.

## 1. Asset overrides

Anything in a pack's `assets/` folder stands in for the game file of the same
name. The engine's paths are Android absolute paths it builds from a package
name and a version code, so matching them literally would make every mod
build-specific. Instead the match is by **file name**, and by the **last two path
components** when a mod ships a folder structure:

| The engine opens | Answered by |
| --- | --- |
| `/storage/.../properties/main.rsb` | `assets/main.rsb`, or `assets/properties/main.rsb` |

The two-component form wins when both exist, and among packs the
**last-loaded wins**. Replacing `main.rsb` replaces the whole `.obb`, which is
the single most useful thing a pack can do.

Each override is announced in the log the first time it is used, so a mod that
silently does nothing is easy to spot.

## 2. `mod.ini` patches

For rewriting guest instructions without writing any code.

```ini
[mod]
name = No purchase confirmation
author = you
; anything else under [mod] is documentation and is ignored

; One [patch] section per instruction. Repeat it as many times as you like.
[patch]
comment = skip the confirm dialog        ; how this patch is named in the log
game    = 4.5.2                          ; only apply on this build ("*" or omit = any)
at      = 0x72d320                       ; .so offset, as a disassembler shows it
expect  = 0xEB000123                     ; refuse unless this is what is there
write   = 0xE3A00001                     ; MOV R0, #1

[patch]
symbol  = native.on_draw_frame           ; resolve through the version table instead of `at`
plus    = 4                              ; ...and offset from it
expect  = 0xE1A00000
write   = nop                            ; `nop` and `ret` are the two spellings understood
```

* `at` is a **.so offset** -- what IDA/Ghidra show, not a runtime address.
* `symbol` names a field of the active version table (`native.on_draw_frame`,
  `global.app_driver`, ...). Prefer it: the core resolves it per build, so a mod
  written against a name keeps working on a release that came out afterwards.
  Run once with `[log] verbose = 1` to see every name the loaded build maps.
* `expect` is not optional in spirit. Without it, an address that belongs to a
  different build is written over in silence; with it, the mod is skipped with a
  line saying exactly what was there instead. Omitting it (or writing 0) skips
  the check, which is exactly as dangerous as it sounds.
* `game` is compared against the detected version string, including `auto:...`
  names, so a patch can be scoped to one build.

Patches apply before any guest code runs. That is not a style choice: the JIT
caches translated blocks, so a rewrite after the fact changes nothing.

## 3. Native plugins

For anything the two above cannot express: running your own C++ when the engine
calls a function, and letting it carry on afterwards.

A plugin is a shared library exporting one function. The whole interface is
`include/pvz2native/mods/mod_api.h` -- a plain C table, versioned and
append-only, so a plugin built today keeps loading against later releases of the
port and needs no other header from this source tree.

```c
#include <pvz2native/mods/mod_api.h>

static const Pvz2ModApi *g;

/* Runs instead of the engine's onDrawFrame; calls the original, then counts. */
static void on_frame(Pvz2ModCall *call, const Pvz2ModHook *hook, void *user) {
    unsigned *frames = (unsigned *)user;
    uint32_t result = g->call_original(call, hook, NULL, 0);
    if ((++*frames % 600) == 0) g->log("%u frames", *frames);
    g->set_result(call, result);
}

static unsigned g_frames;

PVZ2_MOD_EXPORT int pvz2_mod_init(const Pvz2ModApi *api) {
    if (api->api_version < PVZ2_MOD_API_VERSION) return 1;  /* host too old */
    g = api;
    g->log("running on game build %s", api->game_version);

    uint32_t draw = g->symbol("native.on_draw_frame");
    if (draw == 0) return 1;                                /* not this build */
    g->hook(draw, "frame counter", on_frame, &g_frames);
    return 0;
}
```

Build it as a shared library with `pvz2native/include` on the include path and
**no** link against the port -- the API arrives as an argument, so a plugin
resolves nothing at load time:

```sh
g++ -std=c++17 -shared -fPIC -I<pvz2native>/include my_mod.cpp -o my_mod.so
```

### What the API gives you

* **Finding things.** `symbol("native.on_draw_frame")` resolves through the
  version table. `native("Native_onDrawFrame")` reads this binary's own
  `JNINativeMethod` arrays, reaching entry points the table does not name at
  all. `find_pattern("F0 4F 2D E9 ?? ?? 4D E2")` takes an IDA-style signature,
  `find_string` finds a literal, and `word_ref` finds what references a value --
  between them, enough to locate a function on a build nobody has mapped.
* **Hooks.** `hook(offset, name, fn, user)` replaces a guest function with your
  callback. Inside it, `arg`/`set_arg` read and rewrite the arguments,
  `call_original` runs the function you replaced and gives you its result, and
  `set_result` decides what the engine's caller sees. A handler that does not
  call the original replaces the function outright.
* **Patches.** `patch_word(offset, expect, value)`, with the same
  check-before-write rule as `mod.ini`.
* **Guest memory.** `read32`/`write32`/`read8`/`write8`/`read_cstr`, all
  bounds-checked, all taking guest **addresses** (`offset + api->so_base`).
* **`config()`** so a mod can read the same `config.ini` switches the core does.

### Rules

* **Install hooks and patches from `pvz2_mod_init`, never later.** The JIT
  caches translated blocks, so a later rewrite is silently ineffective, and the
  import handler table is sized once and read by guest threads without a lock.
* **Return non-zero from `pvz2_mod_init` to decline.** A mod that finds itself
  on a game build it does not support should decline rather than install
  half-working hooks; the host logs it and carries on with the other mods.
* **One hook per function.** The second would relocate an instruction that is
  already a branch to the first, so it is refused with a line naming the mod
  that got there first. Co-operate by calling the original from inside the
  existing hook, not by stacking.
* **Some functions cannot be hooked.** Only a function whose first instruction
  is position-independent can be -- a `PUSH`, `SUB SP`, `MOV` or `ADD Rd, SP`
  prologue, which is nearly all of them. A PC-relative first instruction is
  refused loudly; hook a caller, or patch the instruction directly.

## How it works

`install` copies the target's first instruction into a private *island* below
the loaded image, appends a branch back into the function's body, and replaces
that first instruction with a branch to an SVC stub bound to your handler. So a
call to the hooked function traps to the host with the guest's registers exactly
as it was entered, and calling the island runs the original in full. See
`include/pvz2native/game/hooks.h`.

Asset overrides ride the path-translation layer every libc file call already
passes through (`include/pvz2native/dependencies/vfs.h`), so one check covers
`fopen`, `open`, `stat` and everything else at once.

## Where to look next

| Question | File |
| --- | --- |
| the plugin interface | `include/pvz2native/mods/mod_api.h` |
| when mods load, and why then | `include/pvz2native/mods/mod_host.h` |
| what can and cannot be hooked | `include/pvz2native/game/hooks.h` |
| finding things in the binary | `include/pvz2native/game/scanner.h` |
| what every symbol name means | `include/pvz2native/game/symbols.h` |
| supporting a new game build | `docs/ADDING_A_VERSION.md` |
