# Site tracks, notation, and credits in lessons

Status: implemented and device-verified 2026-11-10. Supersedes the Pack 1-only cut in
`docs/phase0/remote-pack.md`.

## Decision

The site catalog is the app's content source for maqams, recordings, notation, and credits:

- All 35 maqams that have `tracks` and `notation` in `site/public/data/maqams.json` enter the pack.
- Lessons play a random site track for the answer maqam instead of the bundled CC0 phrase.
- After an answer, the feedback shows the maqam notation image, track title/artist/credits, and
  the source (MaqamWorld).
- Recordings stream from the public site origin. No MaqamWorld audio is bundled in the APK.
- The owner directed this on 2026-11-10. The written approval reference is still missing; the
  licensing record moves from "site only" to "site plus site-hosted playback, no APK copies".

## Pack schema (version 3)

> Version 4 keeps this shape and adds required English fields; see
> `docs/phase0/bilingual-plan.md`. The app now rejects version 3 packs.

```json
{
  "version": 3,
  "content_version": "12-hex",
  "published_at": "ISO-8601 UTC",
  "media_base": "https://yallanganee-73906702.web.app/data/",
  "maqams": [
    {
      "id": "bayati",
      "name_ar": "بياتي",
      "color": "#7FA88C",
      "jins_ar": "جنس بياتي",
      "signature_ar": "one-line teaching note",
      "status": "approved",
      "audio": "…bundled fallback, Pack 1 only…",
      "drone": "drone-c4.mp3",
      "notation": "media/maqam/bayati/bayati.png",
      "source_url": "https://www.maqamworld.com/en/maqam/bayati.php",
      "tracks": [
        {
          "title": "…",
          "artist": "…",
          "audio": "media/maqam/bayati/….mp3",
          "credits": ["…"]
        }
      ]
    }
  ],
  "lessons": [],
  "onboarding": {}
}
```

Rules:

- `version` 3; older cached packs are ignored.
- Only maqams with at least one track and a notation image enter the pack.
- `audio_sha256` and the `app-audio` mirror keep only the bundled onboarding/fallback clips.
- `media_base + notation` and `media_base + track.audio` are absolute HTTPS URLs.
- `signature_ar` is required for every packed maqam (drafted, then reviewed).

## Content authoring

- Fill `name_ar`, `color`, `jins_ar`, `signature_ar`, and `status: approved` for all 35 tracked
  maqams directly in `site/public/data/maqams.json`; `build_site_game_data.py` already carries
  these keys.
- Arabic names are the standard maqam names; colors come from the existing Night Majlis palette
  by jins family; signatures name the degree that identifies the maqam.
- The three Pack 1 maqams keep their existing authored text and bundled CC0 phrase as fallback.

## Lessons

- Keep the three authored Pack 1 pair lessons.
- Generate exploration lessons for every remaining tracked maqam: ordered groups of three (last
  group of two), title `أ أم ب أم ج؟`, six items, each maqam answered twice.
- Generated lessons are deterministic, so IDs and progress survive republishing: `explore_<ids>`.
- Every packed maqam appears in at least one lesson option and at least one answer.

## App behavior

- `RecognitionQuestion` carries the selected track (and one contrast track), chosen with the
  session's seeded `Random`; the engine stays Android-free.
- Playback source order: item `audio` override, else a random track URL, else the bundled maqam
  clip. Slow mode and A/B comparison use the same sources.
- `AudioPlayer` accepts HTTPS URLs as well as asset paths; remote sources use `prepareAsync`, and
  duration/position are only read after `onPrepared` (reading them while preparing aborts playback
  with error `-38` on Samsung). Audio focus, unplug handling, and progress updates are unchanged.
- Feedback adds: notation image (downloaded on demand, small PNG), track title and artist,
  credits list, and "المصدر: MaqamWorld". Empty explanations are not shown.
- Home drops the "يعمل بدون إنترنت" badge and the settings offline claim. Onboarding and progress
  remain offline; lessons need a connection to stream tracks and show notation.
- Audio failure keeps the existing retry/skip panel.

## Rules to change

- `AGENTS.md` rule 3: recordings may stream from the site but must not be bundled; rule 8:
  offline covers the shell, onboarding, and progress, not streamed lessons; content pack format
  and architecture map updated for schema v3, `media_base`, tracks, and notation.
- `docs/CONTENT_LICENSES.md`: site-hosted playback scope, no MaqamWorld binaries in the APK,
  written approval still outstanding.
- `docs/phase0/remote-pack.md`: schema v3, media base, track/notation delivery.
- `docs/RECREATION_PLAN.md`: scope and status notes.

## Verification

- Unit: track selection is deterministic under a seed, every question has a playable source,
  validator rejects a packed maqam with tracks but no notation, URL building uses `media_base`.
- Bundled pack test: every packed maqam has Arabic fields, notation, and at least one track with
  title/artist/audio; every lesson references only packed maqams.
- Device: stream a track on Wi-Fi, confirm slow mode and A/B, confirm notation and credits after
  an answer, confirm retry/skip with the network off, 320 dp RTL and TalkBack.
- Deploy `pack.json` and verify live, then reinstall a debug build and confirm adoption.
- Verified on SM-S908E: track streams from the live origin, notation and credits render after an
  answer, answer stops playback. Deployment of the schema-3 `pack.json` still needs the owner.

## Out of scope

- Downloading or bundling MaqamWorld recordings.
- Adding the 8 foundation-only pages with no tracks.
- Editing notation images or re-recording audio.
