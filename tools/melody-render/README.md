# melody-render

Batch renderer that turns melody MIDI files into uniform robotic clips for the content pack.
Pure Python standard library; MP3 export uses `ffmpeg` if it is on the PATH.

Every clip is: mono, 44.1 kHz, 16-bit WAV, default 30 s, one square/saw lead, fixed velocity,
hard grid, no reverb or effects, peak-normalised to −1.4 dBFS with 30 ms fades.

## Rights first

A rendered clip is a **recording of a composition**. If the MIDI is a transcription of a
commercial song, this renderer produces an unlicensed derivative work no matter how robotic it
sounds. Only feed it:

- melodies you composed yourself (CC0 pack), or
- compositions you hold a sync/mechanical license for.

Read [`docs/CONTENT_LICENSES.md`](../../docs/CONTENT_LICENSES.md) before adding anything to
shipping assets.

## Usage

```bash
# Render every .mid in melodies/ to out/
python3 tools/melody-render/render_melodies.py

# Prove the pipeline works without any input file (writes out/demo-melody.*)
python3 tools/melody-render/render_melodies.py --demo

# Options
python3 tools/melody-render/render_melodies.py \
  --input path/to/midi --out path/to/output \
  --seconds 30 --wave saw --transpose -2 --no-mp3
```

`--wave` is `square` (default), `saw`, `triangle`, or `sine`. `--transpose` shifts by semitones
before rendering.

## Input

One melody per Standard MIDI File in `melodies/`:

- PPQN time division; tempo events are honoured, including tempo changes.
- All tracks are merged, so type 0 and type 1 files both work.
- Overlapping notes are mixed; chords are fine but reduce headroom.
- SMPTE time division is rejected with a clear error.

## Output

```
out/
├── 01-my-hook.wav
├── 01-my-hook.mp3      # when ffmpeg is installed
└── manifest.csv        # source, output, notes used, duration, initial BPM
```

Clip names are slugified from the MIDI filename (`My Hook.mid` → `my-hook.wav`); non-ASCII names
fall back to `track-NN`. Output is gitignored — copy approved clips into
`app/src/main/assets/content/audio/` only after the track has a row in the license register.

## Getting MIDI from audio

Transcribe with [Basic Pitch](https://basicpitch.spotify.com) or Melodyne, then fix the notes in
MuseScore or a DAW. Do this only for compositions you are licensed to use; the transcription tool
does not grant rights, and stem separation of a commercial master is not a shortcut.

## Troubleshooting

- **`SMPTE time division is not supported`** — re-export the MIDI with PPQN.
- **`no notes found`** — the file has no note-on events (drums-only, or a truncated export).
- **MP3 missing** — install `ffmpeg` (`brew install ffmpeg`); WAV output is unaffected.
- **Clip too quiet/loud** — every clip is peak-normalised, so perceived loudness varies with note
  density. Adjust `gain` in `render()` for a batch, not per file.
