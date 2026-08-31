# Game files go here

PvZ2Native does **not** include or download Plants vs. Zombies 2.

For the easiest setup, put exactly **two files** in this folder:

```text
game/
├── your-own-copy.apk
└── matching-file.obb
```

Then return to the `PvZ2Native` folder and run:

```bash
./compile-macos.sh
```

On macOS Apple Silicon the script extracts the required ARM32
`libPVZ2.so` from the APK automatically and copies the matching OBB into the
build folder.

You do **not** need to open the APK or extract `libPVZ2.so` yourself.

Advanced users may instead provide:

```text
game/
├── libPVZ2.so
└── matching-file.obb
```

Only use files from your own legally obtained copy of the game. Game files are
ignored by Git and must never be committed to this repository.
