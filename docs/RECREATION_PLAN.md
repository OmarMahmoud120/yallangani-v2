# Yalla Nghanni — Full Recreation Plan

**Document date:** 2026-11-09
**New project:** `/Users/omarmahmoud/Apps/Android/yallangani-v2`
**Legacy reference:** `/Users/omarmahmoud/Apps/Android/yallanganee` (read-only)
**Status:** Phase 0 complete; V1 core gameplay, AdMob monetization, and rewarded hints implemented
and verified locally on the emulator.

---

## 1. Executive summary

The original **يلا نغني** app was an Arabic song-title guessing game. Its backend
(`osmartapps.com/scratch/YallaNganee.html`), audio pack (`osmartapps.com/audio.zip`), ads, billing,
and Play Games configuration are gone. The old repository cannot reconstruct the level list or
songs: the bundled `db.db` is encrypted, and no archive copy of the API or audio pack exists.

The recreation is therefore a **new build with new, properly licensed content**, distributed under
the **same application ID** so it updates the existing Play listing. Gameplay is offline-first, but
the app now contains AdMob: an interstitial after every five solved rounds and an opt-in rewarded ad
that reveals one correct letter. Version 1 otherwise focuses on the core game loop; retention, IAP,
and social features remain deferred.

---

## 2. Audit findings (why this plan looks the way it does)

| Item | Finding |
| --- | --- |
| Original package | `com.osmartapps.yallanghanee`, version 1.12 (versionCode 12) |
| Original stack | Kotlin/Java, XML layouts, Lottie, Retrofit, Firebase, OneSignal, AdMob, Play Games, anjlab billing |
| Level source | `POST https://osmartapps.com/scratch/YallaNganee.html` → JSON (`LevelInfo[]`) |
| Audio source | `https://osmartapps.com/audio.zip` → downloaded to internal storage |
| Server status | Dead; no snapshots under `web.archive.org` for the API or `audio.zip` |
| Bundled `assets/data/db.db` | 34 KB, entropy ~7.995, no SQLite header → AES/SQLCipher-like; **not referenced by any code**; identical in the old v10 AAB |
| Recoverable artifact | Deleted `app/release/yallanghanee_V10_01-05 18-release.aab` exists in git commit `3c18100`; recovered and inspected. Contains the same encrypted DB and owned visual assets, no levels |
| Levels stored on device | Original app used an unencrypted SQLite `Levels.db`, populated from the API |
| Songs stored on device | `files/audio/<SongName>.mp3` after the first launch |
| Original content recovery | Only from an old phone or app-data backup (see Appendix A) |

**Implication:** original songs/answers are unavailable from the repo. Shipping the same commercial
recordings would also carry the copyright risk that may have caused the Play removal. New content
is the safer and only complete path.

---

## 3. Decisions (confirmed with owner)

1. **Content:** new royalty-free / commissioned music and newly written levels; target **100 Arabic
   tracks** at launch.
2. **Project:** fresh repo, modern stack, sibling folder; old repo untouched.
3. **Store identity:** keep the legacy application ID `com.osmartapps.yallanghanee` to update the
   existing listing (removal reason: policy + audience targeting).
4. **Scope:** core gameplay + offline content, plus AdMob interstitial every 5 solved rounds and an
   opt-in rewarded ad for a hint.
5. **Agent setup:** `AGENTS.md` plus cross-agent rule files (`CLAUDE.md`, `.cursor/rules`).
6. **Content delivery (2026-11-10):** the public site is the runtime content source. The app ships a
   bundled pack for offline use, then adopts a newer `pack.json` from the site when its
   `content_version` differs. See `docs/phase0/remote-pack.md`.

Defaults chosen where the owner did not specify (all changeable before Play upload):

| Setting | Value |
| --- | --- |
| App name | يلا نغني |
| Application ID | `com.osmartapps.yallanghanee` |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |
| Version | 2.0.0 (versionCode 13, above the legacy 12) |
| Orientation | Portrait |
| Language | Arabic only, RTL |
| Permissions | `INTERNET` + `ACCESS_NETWORK_STATE` (ads only; gameplay and content stay on-device) |
| Ads | AdMob test unit IDs by default; real IDs injected at release time |
| Target audience | 13+ (not child-directed; avoids Families policy requirements) |

---

## 4. Product definition

### 4.1 Core loop (V1)

1. Launch → short loading state → **Home** with logo, level number, and **ابدأ**.
2. **Game** shows a play button and the question "ما اسم هذه الأغنية؟".
3. Tapping play streams a 10–15 s clip from the bundled assets.
4. The player taps shuffled Arabic letters; letters fill the title slots in order.
5. Tapping a filled slot returns that letter to the tray.
6. When all slots are filled:
   - Correct → win overlay (Lottie + fireworks), title revealed, **التالي**.
   - Wrong → red board flash, then reset after ~0.8 s.
7. Solved level IDs are persisted; the next launch resumes at the first unsolved level.
8. Solving every level shows a completion state with **ابدأ من جديد**; Home shows the solved count
   and a confirmed reset control.

### 4.2 Non-functional requirements

- Gameplay, content, and progress work fully offline from the bundled or last-known-good pack;
  network is only used to refresh content and to request ads.
- No analytics or tracking beyond what the AdMob SDK collects; no accounts.
- Arabic/RTL by default; usable at 320 dp width.
- Cold start to Home < 1.5 s on a mid-range device.
- Audio starts within 300 ms of tapping play; no clip leaks across levels.
- Progress survives process death and device reboot.
- Text is readable with the display font and accessible to TalkBack.

### 4.3 Explicitly out of scope for V1

Coins, extra hint types (skip/remove), daily reward wheel, banner ads, remove-ads IAP, Play Games
leaderboards, cloud save, push notifications, accounts, social sharing, analytics, crash reporting.
(Interstitial + rewarded hint are now in scope; see §7 Phase 4. Remote content updates are in scope;
see §3 item 6 and `docs/phase0/remote-pack.md`.)

---

## 5. Content strategy

### 5.1 Why content is the critical path

The game is only as good as its pack. Ten placeholder levels prove the loop; a launch needs
roughly **100 levels** with a difficulty curve and legally clean audio. Budget most of the schedule
for this, not for code.

### 5.2 Target pack

| Attribute | Target |
| --- | --- |
| Levels at launch | 100 Arabic tracks (minimum 60) |
| Letters per title | 4–6 early, 7–10 mid, 11–16 late |
| Words per title | 1–3 |
| Clip length | 10–15 s |
| Audio format | MP3/AAC, 44.1 kHz, ~−16 LUFS, ≤ −1 dBTP |
| Genres/themes | Owner choice; keep a mix, avoid ambiguous duplicate titles |
| Answer uniqueness | No two levels with the same normalized answer |
| Hints | Rewarded ad reveals one correct letter; no per-level hint text needed |

### 5.3 Licensing workflow

1. Choose a source model: commission an Arabic music producer, buy a game-compatible library, or
   use CC0/public-domain recordings.
2. Collect written proof that **sync/game use** is allowed, worldwide and perpetual.
3. Record every track in `docs/CONTENT_LICENSES.md` before it enters the repo.
4. Reject tracks with unclear provenance, YouTube rips, or "no copyright" compilations.
5. Get a legal review before the first store release, especially if reusing the original app's
   audio or fonts.

### 5.4 Content pipeline

1. Owner/producer supplies a spreadsheet: title, clip file, optional hint, difficulty.
2. Editor transcribes the title exactly as shipped (spaces included).
3. Script validates: unique IDs, unique normalized answers, supported Arabic characters, ≤ 16
   usable letters, existing audio, duration, loudness.
4. Audio engineer trims and normalizes clips.
5. Import into `assets/content/` + `levels.json`.
6. Run `./gradlew :app:testDebugUnitTest` (content validation is a unit test).
7. Manual playtest each level: recognizable hook, unambiguous title, letters solvable.
8. Version the pack (`version` field + git tag).

### 5.5 Difficulty and fairness

- The tray always contains every answer letter (with duplicates) plus random decoys.
- Arabic normalization is forgiving (hamza, ta marbuta, alef maqsura, diacritics), so slots display
  normalized letters while the win overlay reveals the proper title.
- Avoid titles that differ only by normalization (e.g., same word with/without ta marbuta) because
  they become ambiguous.
- Avoid very short titles with endless valid alternatives; the answer must be the only reasonable
  title for the clip.

---

## 6. Technical architecture

### 6.1 Stack

- Kotlin 2.2.20, Jetpack Compose, Material 3, single `:app` module.
- AGP 8.13.0, Gradle 8.14.3, JDK 17 bytecode target.
- kotlinx.serialization for content, DataStore Preferences for progress.
- Platform `MediaPlayer`/`SoundPool` for audio; Lottie Compose for brand animations.
- Google Mobile Ads SDK 25.5.0 for interstitial and rewarded formats.

No database, DI framework, or navigation library is needed at this scale. Adding them now would be
architecture theater.

### 6.2 Data flow

```
assets/content/levels.json
        │  ContentRepository.load() + validate
        ▼
   ContentPack ──► GameEngine (pure state machine)
                        │
                        ▼
                  GameViewModel (StateFlow)
                        │
                        ▼
              Compose Home/Game screens
                        │
              ProgressStore <── DataStore
```

### 6.3 Key design rules

- `GameEngine` is pure Kotlin and fully unit tested. All Android concerns live outside it.
- The content pack is the single source of truth; no hardcoded levels.
- Arabic comparison always goes through `ArabicText.kt`.
- UI state is a sealed `Loading / Error / Ready` type; engine state is immutable.
- Progress stores solved level IDs and the rounds counter; resume starts at the first unsolved level,
  and the reset action clears both.

### 6.4 Privacy and security

- `INTERNET` is used only by the AdMob SDK; no first-party server, accounts, or analytics.
- AdMob collects device/advertising identifiers, IP, and ad interactions. The Data Safety form must
  declare "Device or other IDs" and "App info and performance" as collected/shared for advertising,
  with the privacy policy linked in Play Console and the UMP consent flow added for EEA/UK users
  before launch.
- Ads use Google's sample unit IDs in source control. Real app/unit IDs come from Gradle properties
  (`admob.appId`, `admob.interstitialUnitId`, `admob.rewardedUnitId`) and are never committed.
- `local.properties` and keystores are gitignored; signing keys are backed up offline.

### 6.5 Remote content delivery

The site publishes `data/pack.json`, generated from `site/public/data/maqams.json` by
`tools/sync_app_pack.py`. The app keeps a bundled pack as the offline baseline, adopts a newer pack
when `content_version` differs, and verifies clip digests before use. Full design, limits, and test
plan: `docs/phase0/remote-pack.md`.

### 6.6 Current implementation status

| Area | Status |
| --- | --- |
| Gradle/Compose scaffold | Done |
| Content pack (3 Pack 1 phrases) | Done |
| Site catalog + generated app pack | Done |
| Runtime pack updates from the site | Pending (§6.5) |
| Game engine, Arabic normalization, tests | Done |
| Home + game + win overlay | Done |
| Progress persistence (solved IDs), rounds counter, reset dialog | Done |
| AdMob interstitial (every 5 rounds) + rewarded hint | Done (test unit IDs) |
| Agent rules (`AGENTS.md`, `CLAUDE.md`, Cursor rule) | Done |
| Lint/build verification | See Phase 0 acceptance |
| 100 licensed Arabic tracks, audio focus handling, accessibility pass | Pending |
| Real AdMob IDs, UMP consent, privacy policy | Pending |
| Play Console re-upload | Pending |

---

## 7. Roadmap

Timeline assumes one developer working part-time; content production can run in parallel.

### Phase 0 — Foundation (complete, 2026-11-09)

- New repo, modern stack, version catalog, RTL theme, reuse of owned visual assets.
- Core gameplay, persistence, sample content, unit tests.
- Agent rules and recreation plan.

**Verified 2026-11-09:** `assembleDebug`, `lintDebug`, and `testDebugUnitTest` pass. The
instrumented `GameFlowTest` runs on an API 36 emulator and plays a level end-to-end (start,
audio toggle, solve, win, next stage). An R8 release build was signed with the debug key,
installed, and confirmed to load the serialized content pack and play audio. Home and game
screens were checked at 412 dp and 320 dp widths. Monetization was also exercised on the emulator:
the rewarded hint showed a Google test ad, reported "Reward granted", and filled one correct
letter; with the rounds counter seeded to 4, solving one round and tapping next brought the
interstitial `AdActivity` to the foreground; in airplane mode the hint showed the Arabic
"ad unavailable" toast while the level stayed playable. The same rewarded ad was confirmed in the
R8 release build.

### Phase 1 — Launch content pack (2–4 weeks)

- Commission/select 100 licensed tracks; clear rights.
- Produce levels with a difficulty curve; QA each clip.
- Add a content import/validation script (CSV/JSON → pack) if the volume justifies it.
- Legal review of music, fonts, menu music, and fireworks sound.
- Replace the sample pack; keep it available as a test fixture.

**Acceptance:** 100 levels, every track documented, content tests green, no duplicate normalized
answers, manual playthrough of the full pack, no ambiguous hooks.

### Phase 2 — Gameplay polish and release engineering (1–2 weeks)

- Audio focus and "becoming noisy" handling (calls, other apps, headset unplug).
- Onboarding hint on first launch (reset control is implemented).
- Accessibility: 48 dp touch targets, TalkBack labels, contrast check.
- Device matrix testing (small phone, large phone, tablet, API 24/31/34/36).
- Release build, R8 verification, signing key + Play App Signing.
- Add CI (build + tests + lint) if the repo moves to a remote host.
- Crash-free verification via Play Console vitals after testing.

**Verified 2026-11-09:** `testDebugUnitTest` (26 tests), `lintDebug` (0 errors, 16 warnings),
`assembleRelease`, and `lintVitalRelease` pass. Audio focus was confirmed in `dumpsys audio` and
logcat; a simulated incoming call (`gsm call`) paused the song and the play icon returned after
cancelling it, and the `AUDIO_BECOMING_NOISY` receiver is registered while playing. The onboarding
dialog shows once, dismisses, and stays dismissed across cold restarts. Answer slots measure 45–48 dp
and tray tiles 44 dp at 411 dp width; eight tray columns cannot reach 48 dp under ~412 dp width, so
the tray caps at 44 dp and shrinks to 32 dp on 320 dp screens. A 320×568 dp pass verified the
compact home layout (centered footer plus visible reset button), the game board/tray fit, and the
reset dialog, while the 411 dp home keeps the footer and reset control on one row. A physical
Galaxy S22 (SM-S908E, Android 16/API 36, 384 dp) run confirmed the first-run onboarding, home and
game layouts in RTL, playback with `GAIN_TRANSIENT` audio focus, a complete solve → next-level
flow, and progress persistence after a cold restart; the debug build installed as
`com.osmartapps.yallanghanee.dev` alongside the untouched legacy app. Not yet done: API 24/31/34 or
tablet runs, TalkBack walkthrough, upload keystore, and CI.

**Acceptance:** no audio leaks, no ANRs/crashes in the matrix, release AAB installs and plays
offline, TalkBack can operate the board.

### Phase 3 — Retention features (deferred)

- Coin economy, four hint types (skip 120, remove 60, reveal 90, hint 20 as in the original).
- Daily reward wheel and free-coin timer.
- Optional: streak, level packs.

**Acceptance:** hint usage cannot corrupt progress; economy tested; no pay-to-win dead ends.

### Phase 4 — Monetization (interstitial + rewarded implemented, 2026-11-09)

- AdMob interstitial after every **5th solved round**, shown between levels, never mid-round and
  never on the first round.
- Opt-in **rewarded ad for a hint**: the player taps «تلميح», watches the ad, and one correct
  letter is placed in the next empty slot (consuming the matching tray letter).
- No banners in V1. Real app/unit IDs come from Gradle properties; test IDs are the committed
  default. UMP consent for EEA/UK is still required before launch.
- Deferred: remove-ads IAP, turn-based interstitial caps beyond the 5-round rule.

**Acceptance:** ads degrade gracefully offline, the hint is granted only after the reward callback,
the interstitial never interrupts active play, Data Safety and Ads declarations updated.

**Verified 2026-11-09 (test unit IDs):** rewarded ad → reward callback → one letter placed;
interstitial shown after the 5th solved round; offline hint shows the fallback toast and the game
continues; playback stops before a rewarded ad opens. Real IDs, UMP consent, and store
declarations remain pending.

### Phase 5 — Social (deferred)

- Play Games Services leaderboard (new app ID).
- Optional cloud save via Saved Games.

**Acceptance:** sign-in is optional, progress works unchanged when signed out, privacy policy
updated.

### Phase 6 — Play launch (1–2 weeks, overlapping Phase 2)

- Reactivate the existing listing with application ID `com.osmartapps.yallanghanee`; if Play has
  permanently deleted it, publish under a new ID instead and update this plan.
- Store listing in Arabic, screenshots, feature graphic, short/full description.
- Content rating questionnaire (Everyone / PEGI 3, ads present), Data Safety (AdMob data),
  ads declaration (yes), privacy policy URL.
- Declare target audience **13+**; do not opt in to Designed for Families (the old removal cited
  targeting/policy issues).
- Closed testing (new personal accounts: currently 12 testers for 14 days — verify current policy).
- Staged rollout 10% → 50% → 100%; monitor Android vitals and reviews.
- Attach the policy fix summary (audience, ads placement, content rights) to the release notes or
  appeal, and confirm the exact old violation text before submitting.

**Acceptance:** production release live, no policy strikes, crash-free rate > 99.5%.

---

## 8. Play Console launch checklist

### Identity and signing

- [ ] Keep application ID `com.osmartapps.yallanghanee`; confirm the listing still exists in Play
      Console (a removed app that was permanently deleted cannot be revived under the same ID).
- [ ] Generate a new upload keystore; enroll in Play App Signing.
- [ ] Back up the keystore and passwords offline; never commit them.
- [ ] versionCode 13 (above the legacy 12), versionName 2.0.0.

### Technical

- [ ] `targetSdk = 36`; test on API 36.
- [ ] 16 KB page-size compatibility (no native libraries currently; keep it that way or verify).
- [x] Release build with R8; verify serialization models are kept.
- [ ] Test offline install and first launch with no network; ads must fail silently.
- [ ] Verify the merged manifest contains only `INTERNET`, `ACCESS_NETWORK_STATE`, and the AdMob
      `com.google.android.gms.ads.APPLICATION_ID` metadata.
- [ ] Icon, adaptive icon, and splash behavior look correct on Android 12+.

### Store listing

- [ ] Arabic title, short description, full description.
- [ ] 512×512 icon, 1024×500 feature graphic, phone screenshots (at least 2–4).
- [ ] Category: Music or Trivia; tags; contact email.
- [ ] Privacy policy URL (host a simple Arabic/English page).
- [ ] Content rating questionnaire (music/lyrics, no user-generated content).
- [ ] Data Safety: declare device/ad IDs and ad interactions collected and shared for advertising.
- [ ] Ads declaration: yes (interstitial + rewarded).
- [ ] Target audience 13+; not designed for families; ads content rating set to general audiences.
- [ ] Closed testing track with required testers; collect feedback.
- [ ] Staged rollout; pause criteria defined.

### Legal and policy

- [ ] Music licenses documented and reviewed.
- [ ] Font licenses verified or fonts replaced.
- [ ] Brand assets ownership confirmed (logo, background, Lottie, menu music, fireworks).
- [ ] Old removal reason investigated (Play Console email / policy notices).
- [ ] No impersonation of the removed app's listing.

---

## 9. QA and test plan

### Automated

- JVM unit tests: Arabic normalization, tray generation, correct/wrong flow, slot removal, level
  advancement, completion, restart, content pack validity, audio presence.
- Compose UI tests: home → play → solve → next (`GameFlowTest` on the emulator); hint reveal.
- Lint in CI.

### Manual matrix

| Dimension | Cases |
| --- | --- |
| Android | 24, 28, 31, 34, 36 |
| Screens | 320 dp, 360 dp, 411 dp, tablet |
| Locale | Arabic; system English but app forced RTL |
| Audio | speaker, headphones, Bluetooth, call interruption, headset unplug |
| Lifecycle | background/foreground, lock screen, process death, low memory |
| Network | airplane mode, no SIM, Wi-Fi off; ad load failure paths |
| Ads | hint without reward, early rewarded dismissal, 5th-round interstitial, no first-round ad |
| Accessibility | TalkBack, large font, display size, contrast |
| Content | every level solvable, no duplicate answers, no clipped hooks |

### Regression hotspots

- Arabic normalization edge cases (hamza, ta marbuta, alef maqsura).
- Tray generation for titles with repeated letters.
- Audio player state after rapid play/pause or level change.
- Resume index after gaps in solved IDs, completing the final level, and reset.
- Layout overflow on 320 dp devices.
- Rounds counter double-incrementing or firing the interstitial on the wrong level.

---

## 10. Risks and mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Music licensing / copyright | App removal, legal claims | CC0/commissioned content, written licenses, legal review, no commercial rips |
| Unknown reason for original removal | Repeat removal | Check old Play Console notices before launch; fix the root cause |
| Content production slips | Delayed launch | Start licensing now; ship 60 levels if needed; keep sample pack as fallback demo |
| Font/menu audio rights unverified | Takedown request | Verify licenses or replace with OFL/CC0 assets |
| Ambiguous Arabic answers | Frustrating gameplay | Editorial review; ban normalization-duplicate answers |
| Audio focus bugs | Bad UX | Implement AudioFocus + noisy handling in Phase 2 |
| Small-screen overflow | Unplayable on cheap devices | Adaptive slot/tile sizing; test at 320 dp |
| Removed listing cannot be reactivated | No update path under the legacy ID | Confirm listing state in Play Console early; have a new-ID fallback ready |
| Ad policy violation (placement/consent) | Removal or ad-serving limits | No mid-round ads, 5-round cap, opt-in rewarded only, UMP consent before launch |
| New-account testing requirements | Delayed production access | Plan the 12-tester/14-day closed test early |
| No first-party analytics by design | Less visibility | Rely on Play Console vitals, AdMob metrics, and store reviews |

---

## 11. Decisions still needed from the owner

Resolved: application ID (`com.osmartapps.yallanghanee`), content volume (100 tracks), monetization
(interstitial every 5 rounds + rewarded hint), removal reason (policy and targeting).

Still open:

1. **Branding** — keep the name "يلا نغني" and logo, or rebrand to distance from the removed listing?
2. **Content source** — commission tracks, buy a game-compatible library, or use CC0 recordings?
3. **Old device** — is there a phone/backup where the original app ran? Decide whether to recover the
   original levels/songs for reference (not necessarily for shipping).
4. **Exact removal notice** — retrieve the policy/targeting email so the fix can be documented in
   the release notes or appeal.
5. **Play account type** — personal or organization? This affects testing requirements.
6. **Privacy policy hosting** — where will it live, and who writes the Arabic/English text?
7. **AdMob account** — confirm the account and app are still active; provide real app/unit IDs
   through Gradle properties (never commit them).
10. **Font licensing** — can you provide the Kharabeesh font license, or should we switch to OFL?

---

## Appendix A — Recovering original content from an old device

Only relevant if the owner wants the original levels/songs as reference. It does **not** make the
old commercial recordings safe to ship.

1. **Old phone with the app still installed and data intact** (best case):
   - The levels are in `/data/data/com.osmartapps.yallanghanee/databases/Levels.db` (unencrypted).
   - The songs are in `/data/data/com.osmartapps.yallanghanee/files/audio/`.
   - Without root, the release app's private data is not directly readable. Options:
     - Rooted device or emulator with root: `adb pull` the two paths.
     - An OEM backup tool or a third-party backup app that can back up app data.
     - `adb backup -f yalla.ab -noapk com.osmartapps.yallanghanee` may work on older Android
       versions (Android 12+ generally restricts adb backup). The app has `allowBackup="true"`.
   - A Google cloud backup may contain the data on an older device; restoring to a test device and
     extracting it is possible with root.
2. Convert `Levels.db` to JSON with a small script (`SELECT * FROM LevelInfo`).
3. Treat the recovered **songs** as reference only. Reusing them requires the original licenses.

---

## Appendix B — Content schema

```json
{
  "version": 1,
  "question": "ما اسم هذه الأغنية؟",
  "notice": "optional",
  "levels": [
    {
      "id": 1,
      "title": "لحن الحجاز",
      "audio": "01-alhijaz.mp3"
    }
  ]
}
```

Validation rules enforced by `ContentPackValidator` and `ContentPackTest`:

- `version >= 1`, non-blank `question`, non-empty `levels`.
- Positive, unique `id`.
- `title` non-blank, Arabic letters and spaces only, at least one usable letter, ≤ 16 letters.
- `audio` is a plain file name (no path separators or `..`), and the file exists in
  `assets/content/audio/` and is non-empty.

---

## Appendix C — Working commands

```bash
cd /Users/omarmahmoud/Apps/Android/yallangani-v2

JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  ./gradlew :app:assembleDebug

JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  ./gradlew :app:testDebugUnitTest

JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  ./gradlew :app:lintDebug
```
