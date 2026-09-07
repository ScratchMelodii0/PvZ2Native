# Adding a PvZ2 version

Everything version-specific about `libPVZ2.so` lives in exactly one file,
`pvz2native/src/game/symbols.cpp`. Adding a build is adding one entry to the
`kVersions` table there. No other file in the project may contain an address
into the game binary, and nothing else needs to change.

This document is the procedure. Read `include/pvz2native/game/symbols.h`
alongside it -- every field is documented at its declaration, including what
goes wrong when it is missing, which is usually more useful than knowing what
it is.

## 0. Start by just running it

Point `[paths] so` at the new `libPVZ2.so` and launch. The port does most of
this work for you now:

* If a `kVersions` entry's two fingerprints match, you are done -- it is a build
  the project already supports.
* If none match, the loader decodes the `JNINativeMethod` arrays in the binary
  and recovers the lifecycle natives **by name**. When every required one is
  found it boots anyway, under a synthetic version name like `auto:9f3c2a11`,
  and prints a ready-to-paste `kVersions` entry.

So the normal path is: run it, copy the printed entry into `kVersions`, then
fill in and verify the handful of things a scan cannot recover (below).

`[game] auto_version = 0` turns the fallback off if you would rather the port
refuse to start on an unverified build.

### What the scan can and cannot recover

| Recovered from the binary | Must be found by hand |
| --- | --- |
| every `native.*` entry point | `surface_changed_pad` |
| `jni_native.http_transaction_error` | `global.app`, `global.app_driver` |
| `game_app_init_args` (parsed from the JNI signature) | `fn.string_ctor` |
| both fingerprints | `patch.*`, `probe.*`, `input.*` |

The right-hand column is all optional. Every consumer of those fields checks for
0 and turns itself off, so a version entry with only the left-hand column is a
perfectly good first commit -- the game boots, the diagnostics stay quiet, and
the purchase patches simply are not applied.

Run once with `[log] verbose = 1` and the port prints the whole active table,
mapped and unmapped, with each unmapped field named exactly as the symbol table
spells it. That list *is* the to-do list for the version.

## 1. Why the natives need offsets at all

`JNI_OnLoad` registers them with `RegisterNatives`, so unlike the exported
`Java_*` symbols they never appear in the dynamic symbol table and cannot be
looked up by name at load time. They can only be found by reading the
`JNINativeMethod` arrays -- which is exactly what the scanner does, and what you
do by hand in a disassembler when the scanner cannot.

## 2. Finding them by hand (IDA/Ghidra)

Only necessary when the automatic scan misses something.

1. Find `JNI_OnLoad` (it *is* exported) and read the `JNINativeMethod` arrays it
   passes to `RegisterNatives`. Each entry is `{const char *name, const char
   *sig, void *fn}`.
2. There are usually several arrays, not one. 9.6.1 has five, and 4.5.2
   registers only three natives from `JNI_OnLoad` itself with the rest in two
   further arrays. Reach them by following a data cross-reference from a method
   **name string** -- do not try to recognise the functions themselves.
3. **A 12-byte table of pointers is self-similar under a 4-byte shift.** A naive
   pattern scan reports every array three times, with three different apparent
   field orders and three different sets of function addresses. The correct one
   is the one whose *start address* is referenced by the code that passes it to
   `RegisterNatives`. (The built-in scanner defeats this by validating the
   fields: a signature must start with `(`, a name must be an identifier, and
   `fn` must land in the executable segment. Only one alignment survives that.)
4. Some lifecycle natives are one-instruction thunks -- in 9.6.1 two of them are
   a bare `BX LR`, 4 bytes apart. That looks like a misread and is not.

## 3. Fingerprints

Take the first 8 bytes at `native.on_draw_frame` and at
`native.game_app_initialize`, as little-endian `uint64`, with any hex viewer,
and put them in `fingerprint_draw_frame` / `fingerprint_game_app_init`.

They are the entire detection mechanism. If both match, the two most important
offsets in the entry are proven correct against this exact binary; a mismatch is
caught before the port branches into the middle of an unrelated function. Do not
substitute a file-size check -- a re-signed or re-packed APK changes the size and
not the code.

Note the byte order convention: the fingerprints are **words** as a disassembler
prints them (`0xE92D4FF0` = `PUSH {R4-R11,LR}`), while a scanner pattern is
**bytes** in memory order (`F0 4F 2D E9`). Mixing the two is the one easy
mistake here.

## 4. `surface_changed_pad`

How many dummy words precede `(width, height)` in the `onSurfaceChanged` call --
i.e. how many of `r1..r3` the JNI preamble eats before the real arguments start.

Read it off the body of `Native_onSurfaceChanged`: a plain static native reads
`r2`/`r3`, which is `pad = 1` (both 4.5.2 and 9.6.1). 1.6.10 is the odd one at 2.
**Getting this wrong is silent** -- the engine derives its whole projection
matrix from whichever registers it reads, so a shift by one produces a
plausible-looking window with a poisoned transform rather than a crash. The
auto-detected entry guesses 1 and says so; verify it.

## 5. `game_app_init_args`

The object arguments `Native_GameAppInitialize` declares, in order, *not*
counting the leading `(JNIEnv*, jobject thiz)`. Read them straight off the JNI
signature string in its `JNINativeMethod` entry -- never guess. The list genuinely
changed between releases: 1.6/4.5.2 declare eight with an `AndroidFacebookDriver`
third; 9.6.1 declares seven and has no Facebook driver at all. Reusing the old
list on 9.6.1 shifts `cloud`, the `GooglePlay*` objects and `notification` each
down one register, which once made the engine corrupt its own screen size
instead of failing.

The scanner parses this out of the signature for you and prints it.

## 6. The optional fields

Fill these in when you want the feature; skip them and it stays off.

* **`global.app` / `global.app_driver`** -- the `LawnApp` the SexyAppFramework
  constructor stores, and the separate `AndroidAppDriver` pointer the
  surface/frame natives dereference. They are **not** the same object; assuming
  they were cost a long hunt once already.
* **`patch.*`** -- the purchase-broker connectivity calls and the receipt
  verdict. See `game/patches.h` for what each rewrite means. Every site is
  verified against the instruction the table promised before anything is
  written, so a wrong address is reported rather than acted on.
* **`probe.*` / `input.*`** -- the frame-gate and touch diagnostics. Leave them
  at 0 rather than borrowing another build's: 4.5.2 once ran the 9.6.1 probe
  offsets against a 4.5.2 object and printed entirely plausible garbage, which
  is worse than printing nothing.

## 7. Writing the entry

Use **designated initialisers** (`.native = { .on_draw_frame = ... }`) and leave
unmapped fields out entirely rather than writing `0` placeholders. Every field is
an interchangeable `uint32`, so a positional aggregate that omits one shifts
everything after it with nothing for the compiler to complain about, and the
symptom is a branch into the middle of an unrelated function.

Comment what you found and *how*, the way the existing three entries do. The
next person to look at a build has only those notes.

## 8. Verify

1. Boot and check the detection line names your version rather than falling back
   to `auto:`.
2. Run with `[log] verbose = 1` and read the symbol report. Any native whose
   scanned address disagrees with your table is printed as a warning -- that
   means one of the two is wrong for this binary, and it is worth resolving
   before anything else.
3. Reach the menu, resize the window (this re-runs `onSurfaceChanged`, which is
   what exercises `surface_changed_pad`), and touch something.

## Tools you can use from a plugin

Everything the detector uses is available to mods as well, so an unsupported
build can be explored without rebuilding the core -- see `docs/MODDING.md` and
`include/pvz2native/game/scanner.h`.
