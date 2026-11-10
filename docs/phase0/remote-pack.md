# Remote content pack (runtime site updates)

Status: approved and shipped 2026-11-10. Schema 3 extended the same day with site tracks,
notation, and a 35-maqam catalog; see `docs/phase0/site-tracks-plan.md`.

## Decision

The public site becomes the runtime content source. The app ships a bundled pack for offline use,
checks the site for a newer pack, and rebuilds its lessons from whatever pack it ends up holding.

This changed two earlier contracts:

- `AGENTS.md` rule 8 (“offline gameplay is sacred”) now reads: the app always has a usable
  last-known-good pack, and the network refreshes content and streams lesson audio.
- `docs/RECREATION_PLAN.md` §4.3 drops “remote content updates” from the out-of-scope list.

## Pack format

The site publishes two files, both generated from `site/public/data/maqams.json`:

| File | Consumer | Shape |
| --- | --- | --- |
| `data/maqams.json` | site, humans | catalog: every maqam, tracks, notes, notation |
| `data/pack.json` | app | app schema: approved Pack 1 maqams, onboarding, lessons |

`pack.json` is written by `tools/sync_app_pack.py`, the same file that writes
`app/src/main/assets/content/maqams.json`. One generator, no hand-edited copies.

Additions to the app pack:

- `content_version`: 12-hex digest of the pack content (maqams, onboarding, lessons). Equal content
  gives an equal value; any edit changes it. The app compares this value, not ordering.
- `version` is the schema version (`3`). Bump it only on a breaking shape change; the app rejects
  unknown schema versions instead of guessing.
- `media_base`: absolute HTTPS base (`https://yallanganee-73906702.web.app/data/`) for every relative
  media path in the pack.
- Per maqam: `notation` (PNG path), `source_url` (MaqamWorld page), and `tracks[]`
  (`title`, `artist`, `audio`, `credits`). Lesson audio resolves to a random track per question.
- `audio_sha256`: map of *bundled* clip file name to digest. Site tracks are not downloaded by the
  app, so they have no digest entry.

Audio:

- Lesson questions stream the site track MP3 directly through `MediaPlayer`; no track is copied into
  the APK or app cache.
- `site/public/data/app-audio/<file>` still mirrors the nine Pack 1 clips (phrases, drills, drone);
  it is the download origin when a pack references one that is not bundled.
- Notation images are fetched from the site on answer and kept in a small in-memory LRU cache.

## App behaviour

1. Read the bundled pack. The app is usable immediately and offline, unchanged.
2. Read `filesDir/pack/pack.json` if present and valid. Use it when its `content_version` differs
   from the bundled one.
3. In the background, fetch `https://yallanganee-73906702.web.app/data/pack.json` (HTTPS only, 5 s
   connect/read timeout, `HttpURLConnection`, no new dependency).
4. If the fetch succeeds, the schema matches, `content_version` differs, and every referenced clip
   resolves, adopt the pack: write it to `filesDir/pack/pack.json`, refresh the UI state.
5. If anything fails, keep the current pack and log nothing user-facing. Lesson start must never
   wait on the network.

Refresh triggers: cold start, and returning to Home when the last check is older than 6 hours.
No periodic background work, no WorkManager.

### Audio resolution

A question resolves its source in this order:

1. Item-level bundled `audio` override.
2. `<media_base><track.audio>` for the track picked at random from the answer maqam.
3. The maqam's bundled `audio` phrase (Pack 1 only).

`MediaPlayer` streams HTTPS sources with `prepareAsync`; `duration`/`position` are only read after
`onPrepared` because reading them while preparing aborts playback with error `-38` on Samsung.
A bundled clip named in the active pack still follows bundled asset → verified cache → remote
`data/app-audio` download.

### Limits and safety

- Pack body ≤ 512 KB; one bundled clip ≤ 5 MB; referenced cached clips ≤ 50 MB, otherwise the pack
  is rejected and the previous one stays. Streamed tracks are not downloaded or size-capped by the
  app; `MediaPlayer` reads them progressively.
- Reject non-200, non-HTTPS, and bodies over the caps before parsing.
- Verify `audio_sha256` before a downloaded clip is used.
- A rejected or partial pack never replaces a working one; writes are temp-file then rename.
- Unreferenced cached clips are deleted after a pack is adopted.
- Hosting serves `data/pack.json` and `data/app-audio/**` with `Cache-Control: public, max-age=300`
  so a redeploy is visible within five minutes without pinning a stale pack in the CDN.
- No analytics, no identifiers, no request logging.

## Consequences

- Content edits reach installed apps without a Play release, which is the owner's goal.
- Content and lessons survive offline from the bundled or cached pack, but site tracks and notation
  need a network connection. The old “works without internet” copy was removed on 2026-11-10.
- Progress keys are lesson IDs. Removing a lesson leaves its completion flag orphaned but harmless;
  adding lessons does not reset progress.
- The pack is fetched at launch: one small HTTPS request. It is not ads and carries no identifiers.
- `INTERNET` is already declared. It is now required for pack refresh, track streaming, and notation
  images, not only for the retained ads SDK.

## Test plan

Unit:

- digest stability: same pack, same `content_version`; one changed lesson, different value.
- cache read/write, rejection of a tampered clip, rejection of an oversized body.
- fallback order: bundled → cache → remote, and failure paths.

Instrumented:

- fake server serves a newer pack; Home shows the new lesson set without a restart.
- airplane mode after first sync keeps the cached pack.
- corrupted cache falls back to the bundled pack.

Manual:

- deploy `pack.json`, change one lesson, redeploy, relaunch the app, confirm the change.
- 320 dp RTL and TalkBack pass on the updated lesson list.
