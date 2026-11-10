# Remote content pack (runtime site updates)

Status: proposed 2026-11-10, awaiting owner review.

## Decision

The public site becomes the runtime content source. The app ships a bundled pack for offline use,
checks the site for a newer pack, and rebuilds its lessons from whatever pack it ends up holding.

This changes two earlier contracts and both must be amended when the work is approved:

- `AGENTS.md` rule 8 (“offline gameplay is sacred”) becomes “offline gameplay is guaranteed from the
  bundled or last-known-good pack; the network only refreshes content”.
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
- `version` stays the schema version (`2`). Bump it only on a breaking shape change; the app rejects
  unknown schema versions instead of guessing.
- `audio_sha256`: map of clip file name to digest. Required for clips hosted on the site.

Audio:

- `site/public/data/app-audio/<file>` mirrors the Pack 1 clips. It is the download origin for new
  clips and is generated from the app assets by the sync step.
- Site recordings (`data/media/...`) are still never shipped or downloaded into the app.

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

For a clip named in the active pack:

1. Bundled asset (`assets/content/audio/`) wins.
2. `filesDir/pack/audio/<name>` if the digest matches `audio_sha256`.
3. Otherwise download from `<base>/data/app-audio/<name>`, verify the digest, then play.

A download failure falls back to the bundled clip for that maqam when one exists, and otherwise
drops that maqam from the lesson options (`MaqamPackValidator` already rejects unknown answers).

### Limits and safety

- Pack body ≤ 512 KB; one clip ≤ 5 MB; referenced cached clips ≤ 50 MB, otherwise the pack is
  rejected and the previous one stays.
- Reject non-200, non-HTTPS, and bodies over the caps before parsing.
- Verify `audio_sha256` before a downloaded clip is used.
- A rejected or partial pack never replaces a working one; writes are temp-file then rename.
- Unreferenced cached clips are deleted after a pack is adopted.
- Hosting serves `data/pack.json` and `data/app-audio/**` with `Cache-Control: public, max-age=300`
  so a redeploy is visible within five minutes without pinning a stale pack in the CDN.
- No analytics, no identifiers, no request logging.

## Consequences

- Content edits reach installed apps without a Play release, which is the owner's goal.
- The app still passes the airplane-mode check: bundled pack on first run, cached pack afterwards.
- Progress keys are lesson IDs. Removing a lesson leaves its completion flag orphaned but harmless;
  adding lessons does not reset progress.
- The pack is fetched at launch: one small HTTPS request. It is not ads and carries no identifiers.
- `INTERNET` is already declared, so no manifest change. If ads are ever removed, the permission is
  still required by this feature.

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
