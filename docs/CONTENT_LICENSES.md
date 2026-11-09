# Content licenses

## Current pack: `sample-maqamat-v1`

- Location: `app/src/main/assets/content/`
- Levels: 10
- Audio: 10 original instrumental clips, 12 seconds each
- Generator: `tools/generate_sample_audio.py`
- License: **CC0 1.0 Universal (public domain dedication)**
- Third-party recordings: none

Each clip is synthesized from a maqam scale by the generator script in this repository. The
melodies are original note sequences, not transcriptions of existing songs. The level titles name
the maqam family (e.g., "لحن الحجاز") and are not copyrighted works.

| File | Title | Maqam | Duration | Source | License |
| --- | --- | --- | --- | --- | --- |
| `01-alhijaz.mp3` | لحن الحجاز | Hijaz | 12s | synthesized in-repo | CC0 |
| `02-alnahawand.mp3` | لحن النهاوند | Nahawand | 12s | synthesized in-repo | CC0 |
| `03-alkurd.mp3` | لحن الكرد | Kurd | 12s | synthesized in-repo | CC0 |
| `04-alsaba.mp3` | لحن الصبا | Saba | 12s | synthesized in-repo | CC0 |
| `05-albayati.mp3` | لحن البياتي | Bayati | 12s | synthesized in-repo | CC0 |
| `06-alrast.mp3` | لحن الرست | Rast | 12s | synthesized in-repo | CC0 |
| `07-alsika.mp3` | لحن السيكا | Sika | 12s | synthesized in-repo | CC0 |
| `08-alajam.mp3` | لحن العجم | Ajam | 12s | synthesized in-repo | CC0 |
| `09-alhuzam.mp3` | لحن الهزام | Huzam | 12s | synthesized in-repo | CC0 |
| `10-albanjkah.mp3` | لحن البنجكاه | Banjkah | 12s | synthesized in-repo | CC0 |

Regenerate with:

```bash
python3 tools/generate_sample_audio.py
```

## Launch pack target: 100 Arabic tracks

V1 launches with **100 Arabic tracks**. Until that pack is licensed and documented, the ten
synthesized maqam clips above are the only content allowed in shipping builds.

The current market shortlist lives in [`content/`](content/README.md):
`top-100-arabic-trending-2026-11-09.csv` (what is charting) and `game-ready-arabic-titles.csv`
(titles that already pass the Arabic-title rules). **Both are research only — every row still
needs the master and sync clearances described below.**

### Sourcing options (pick one per batch, record which)

1. **Commissioned recordings** — pay a performer/arranger for an original instrumental rendition of
   an original melody, with a written assignment of rights covering mobile game distribution.
2. **Game-compatible royalty-free library** — purchase a license that explicitly permits use in
   mobile games/apps (not video-only), worldwide, perpetual, with no per-download reporting.
3. **CC0 / public-domain recordings** — verify that both the composition and the recording are
   free of restrictions and that no Content ID system claims them.

Commercial Arabic recordings, YouTube rips, and "no copyright music" compilations of commercial
songs are never allowed, even as placeholders.

### Per-batch pipeline

1. Pre-screen the license for mobile-game use, territory, term, and Content ID claims.
2. Store the license text/contract in the evidence archive and add a row to the register below.
3. Process a 10–15 s hook (see the audio checklist), name it `NN-slug.mp3` in lowercase.
   Original melodies written as MIDI can be batch-rendered into uniform clips with
   [`tools/melody-render/`](../tools/melody-render/README.md).
4. Add the level to `levels.json` with a unique `id` and a distinct Arabic title.
5. Run `./gradlew :app:testDebugUnitTest` — `ContentPackTest` must pass with all audio present.
6. Play every new level on a phone speaker before merging.

### Track register (one row per shipped track)

| Level ID | Title | Audio file | Rights holder | License | Evidence ref | Territory/term | Mobile-game use confirmed |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | لحن الحجاز | `01-alhijaz.mp3` | in-repo generator | CC0 1.0 | `tools/generate_sample_audio.py` | worldwide/perpetual | n/a — original synthesis |

For the 100-track pack, extend this table (or link a per-batch annex kept with the license
evidence) so every shipped level has a row before release.

### Editorial rules for 100 tracks

- Arabic titles only, letters and spaces, at most 16 usable letters.
- No two levels may normalize to the same answer (see `ArabicText`).
- Titles must be solvable from the clip; avoid ambiguous transliterations.
- Split the pack into recognizable difficulty waves (early levels short and famous-in-genre
  melodies; later levels longer).
- Keep a 10-track CC0 fallback so a licensing failure on one batch never blocks a release.

## Replacing the pack with real songs

Do not upload a replacement track until all of the following are recorded here:

1. **Track name and level ID** it belongs to.
2. **Rights holder** and **license type** (original work, CC0, purchased royalty-free, commission
   with written assignment, etc.).
3. **License text or contract reference** (file path or URL).
4. **Territory and term** (worldwide/perpetual preferred).
5. **Proof that game/sync use is allowed** — many "royalty-free music" licenses cover videos but
   not mobile games. Check explicitly.
6. **Source file** and the processed version (trim, fade, loudness normalization).

### Legal guardrails

- Song **titles** are generally not protected by copyright, but **recordings and compositions**
  are. A re-recorded cover still needs a mechanical/sync license for the composition.
- Do not use commercial Arabic song clips, YouTube rips, or "no copyright" compilations of
  commercial music.
- Do not transcribe a famous melody for a placeholder track. The current synthesized melodies are
  original note sequences.
- Keep license evidence for every track, even after launch.

### Audio processing checklist

- 10–15 second hook, starting at a recognizable phrase.
- Trim silence; 150–300 ms fade-in and fade-out.
- 44.1 kHz, mono or stereo, MP3 96–128 kbps (or AAC), filename lowercase with no spaces.
- Loudness approximately −16 LUFS integrated, true peak ≤ −1 dBTP.
- Verify the clip is clear on a phone speaker, not only headphones.
- Keep the original lossless source outside the APK, backed up with the license.

## Fonts and visual assets

The project reuses visual assets from the original 2017 app: background, logo, Kharabeesh display
font, Lottie animations, menu music, and fireworks sound. These are the owner's assets, but the
**font licenses must be verified before a store release**:

- `Kharabeesh-Font.ttf` — confirm the license permits embedding in a commercial Android app.
- If a font license cannot be proven, replace it with an open font (e.g., an OFL Arabic display
  font) and update `ui/theme/Theme.kt`.
- The menu music and fireworks sound are from the original app; confirm they were originally
  licensed for distribution. If not, replace them with CC0 assets.

Record the outcome in this file before launch.
