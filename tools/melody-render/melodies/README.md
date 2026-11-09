# Input melodies

Drop one Standard MIDI File per hook here (`.mid` or `.midi`). The renderer reads every file in
this folder and writes a 30-second robotic clip per file to `../out/`.

- The MIDI should contain a single melodic line, PPQN time division, tempo events optional.
- SMPTE time division and unclosed notes are handled only partially: export with PPQN.
- Files may be Arabic-named; non-ASCII stems fall back to `track-NN` clip names.

Only add MIDI for melodies you composed, or whose composition you are licensed to use. See
[`docs/CONTENT_LICENSES.md`](../../docs/CONTENT_LICENSES.md) before feeding in a commercial song.
