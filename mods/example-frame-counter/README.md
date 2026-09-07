# Example mod

A complete mod pack, for copying. See [`docs/MODDING.md`](../../docs/MODDING.md)
for the reference.

* `mod.ini` — metadata, and one `[patch]` that is deliberately inert so you can
  see what a refused patch looks like in the log.
* `frame_counter.c` — a native plugin that hooks `Native_onDrawFrame`, calls the
  original, and logs a count every 600 frames. Build instructions are in its
  header comment.
* an `assets/` folder would go here too: any file in it stands in for the game
  file of the same name, with no code at all.

The plugin is not built by this project's CMake — it is a mod, built separately,
exactly as somebody else's would be.
