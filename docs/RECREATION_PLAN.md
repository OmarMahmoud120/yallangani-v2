# Yalla Nghanni — Full Recreation Plan

**Document date:** 2026-11-09
**New project:** `/Users/omarmahmoud/Apps/Android/yallangani-v2`
**Legacy reference:** `/Users/omarmahmoud/Apps/Android/yallanganee` (read-only)
**Status:** Phase 0 complete; recognition lessons, bilingual UI, site content streaming, and the
approved ad placements (app open, Home banner, post-Results interstitial, rewarded hint) with UMP
consent implemented and verified locally on the emulator. Real ad unit IDs and store declarations
remain release tasks.

---

## 1. Executive summary

The original **يلا نغني** app was an Arabic song-title guessing game. Its backend
(`osmartapps.com/scratch/YallaNganee.html`), audio pack (`osmartapps.com/audio.zip`), ads, billing,
and Play Games configuration are gone. The old repository cannot reconstruct the level list or
songs: the bundled `db.db` is encrypted, and no archive copy of the API or audio pack exists.

The recreation is therefore a **new build with new, properly licensed content**, distributed under
the **same application ID** so it updates the existing Play listing. Gameplay is now maqam
recognition training, and the app monetizes with the placements the owner approved on 2026-11-10: a
daily app-open ad, a Home banner, an interstitial after every third completed lesson when leaving
Results, and an optional rewarded 50/50 hint inside a lesson. Retention, IAP, and social features
remain deferred. Full placement and consent design: `docs/phase0/ads-plan.md`.

---

## 2. Audit findings (why this plan looks the way it does)

| Item                        | Finding                                                                                                                                                                                |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Original package            | `com.osmartapps.yallanghanee`, version 1.12 (versionCode 12)                                                                                                                           |
| Original stack              | Kotlin/Java, XML layouts, Lottie, Retrofit, Firebase, OneSignal, AdMob, Play Games, anjlab billing                                                                                     |
| Level source                | `POST https://osmartapps.com/scratch/YallaNganee.html` → JSON (`LevelInfo[]`)                                                                                                          |
| Audio source                | `https://osmartapps.com/audio.zip` → downloaded to internal storage                                                                                                                    |
| Server status               | Dead; no snapshots under `web.archive.org` for the API or `audio.zip`                                                                                                                  |
| Bundled `assets/data/db.db` | 34 KB, entropy ~7.995, no SQLite header → AES/SQLCipher-like; **not referenced by any code**; identical in the old v10 AAB                                                             |
| Recoverable artifact        | Deleted `app/release/yallanghanee_V10_01-05 18-release.aab` exists in git commit `3c18100`; recovered and inspected. Contains the same encrypted DB and owned visual assets, no levels |
| Levels stored on device     | Original app used an unencrypted SQLite `Levels.db`, populated from the API                                                                                                            |
| Songs stored on device      | `files/audio/<SongName>.mp3` after the first launch                                                                                                                                    |
| Original content recovery   | Only from an old phone or app-data backup (see Appendix A)                                                                                                                             |

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
4. **Scope:** core gameplay + offline content, plus the approved AdMob placements (app open, Home
   banner, post-Results interstitial every third lesson, rewarded 50/50 hint) with UMP consent.
5. **Agent setup:** `AGENTS.md` plus cross-agent rule files (`CLAUDE.md`, `.cursor/rules`).
6. **Content delivery (2026-11-10):** the public site is the runtime content source. The app ships a
   bundled pack for offline use, then adopts a newer `pack.json` from the site when its
   `content_version` differs. Schema 3 covers all 35 tracked maqams and streams a random site track
   per question; only the Pack 1 phrases and drone are bundled. See
   `docs/phase0/remote-pack.md` and `docs/phase0/site-tracks-plan.md`.

Defaults chosen where the owner did not specify (all changeable before Play upload):

| Setting                         | Value                                                                               |
| ------------------------------- | ----------------------------------------------------------------------------------- |
| App name                        | يلا نغني                                                                            |
| Application ID                  | `com.osmartapps.yallanghanee`                                                       |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36                                                                        |
| Version                         | 2.0.0 (versionCode 13, above the legacy 12)                                         |
| Orientation                     | Portrait                                                                            |
| Language                        | Arabic only, RTL                                                                    |
| Permissions                     | `INTERNET` + `ACCESS_NETWORK_STATE` (pack refresh, track streaming, notation; ads SDK) |
| Ads                             | AdMob test unit IDs by default; real IDs injected at release time                   |
| Target audience                 | 13+ (not child-directed; avoids Families policy requirements)                       |

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

- Lessons, content, and progress load from the bundled or last-known-good pack without a network.
  Site tracks and notation images need a connection; the network is also used to refresh content
  and to request ads.
- Firebase Analytics and Crashlytics collect anonymous usage and crash data; no accounts and no
  first-party server. Ads stay gated on UMP consent.
- Arabic/RTL by default; usable at 320 dp width.
- Cold start to Home < 1.5 s on a mid-range device.
- Audio starts within 300 ms of tapping play; no clip leaks across levels.
- Progress survives process death and device reboot.
- Text is readable with the display font and accessible to TalkBack.

### 4.3 Explicitly out of scope for V1

Coins, extra hint types (skip/remove), daily reward wheel, banner ads, remove-ads IAP, Play Games
leaderboards, cloud save, push notifications, accounts, social sharing, Firestore, Firebase Auth,
Remote Config, and Messaging.
(Interstitial + rewarded hint are now in scope; see §7 Phase 4. Remote content updates are in scope;
see §3 item 6 and `docs/phase0/remote-pack.md`.)

---

## 5. Content strategy

### 5.1 Why content is the critical path

The game is only as good as its pack. Ten placeholder levels prove the loop; a launch needs
roughly **100 levels** with a difficulty curve and legally clean audio. Budget most of the schedule
for this, not for code.

### 5.2 Target pack

| Attribute         | Target                                                                |
| ----------------- | --------------------------------------------------------------------- |
| Levels at launch  | 100 Arabic tracks (minimum 60)                                        |
| Letters per title | 4–6 early, 7–10 mid, 11–16 late                                       |
| Words per title   | 1–3                                                                   |
| Clip length       | 10–15 s                                                               |
| Audio format      | MP3/AAC, 44.1 kHz, ~−16 LUFS, ≤ −1 dBTP                               |
| Genres/themes     | Owner choice; keep a mix, avoid ambiguous duplicate titles            |
| Answer uniqueness | No two levels with the same normalized answer                         |
| Hints             | Rewarded ad reveals one correct letter; no per-level hint text needed |

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
- Google Mobile Ads SDK 25.5.0 (app open, banner, interstitial, rewarded) and User Messaging
  Platform 4.0.0 for consent.

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

- `INTERNET` and `ACCESS_NETWORK_STATE` are used only for lesson audio/notation, pack refresh, the
  ads SDK, and Firebase telemetry; there is no first-party server or account.
- AdMob collects device/advertising identifiers, IP, and ad interactions. Firebase Analytics and
  Crashlytics collect a Firebase installation ID, app/device data, approximate location, usage
  events, and crash diagnostics; they start at first launch and are never sent audio or free text.
  The Data Safety form must declare "Device or other IDs", "App info and performance", and
  "App activity" as collected (and shared with Google where applicable), with the privacy policy
  linked in Play Console.
- UMP consent gates SDK initialization and every ad load; the Settings screen exposes the privacy
  options form whenever the provider requires it (`ConsentManager`, `settings_privacy_*`).
- Ads use Google's sample unit IDs in source control and debug builds. Release builds read
  `admob.appId`, `admob.bannerUnitId`, `admob.interstitialUnitId`, `admob.rewardedUnitId`,
  `admob.rewardedInterstitialUnitId`, and `admob.appOpenUnitId` from Gradle properties; a release
  build fails if any is missing. Real IDs are never committed.
- Local ad-frequency counters (lessons since the last interstitial, UTC day of the last app-open
  ad) live in DataStore next to progress and survive a progress reset.
- `local.properties` and keystores are gitignored; signing keys are backed up offline.

### 6.5 Remote content delivery

The site publishes `data/pack.json`, generated from `site/public/data/maqams.json` by
`tools/sync_app_pack.py`. The app keeps a bundled pack as the baseline, adopts a newer pack when
`content_version` differs, and verifies bundled clip digests before use. Schema 3 packs carry
`media_base`, per-maqam notation and tracks, and exploration lessons; lesson audio streams the site
track with `MediaPlayer` and feedback shows notation, credits, and the source link. Full design,
limits, and test plan: `docs/phase0/remote-pack.md`.

### 6.6 Current implementation status

| Area                                                                 | Status                 |
| -------------------------------------------------------------------- | ---------------------- |
| Gradle/Compose scaffold                                              | Done                   |
| Content pack (35 maqams; 3 bundled phrases + drone)                   | Done                   |
| Site catalog + generated app pack (schema 4, Arabic+English)         | Done                   |
| English site edition (`/en/`) and in-app language switch             | Done                   |
| Runtime pack updates from the site                                   | Done (§6.5)            |
| Site track streaming, notation, credits                              | Done                   |
| Game engine, Arabic normalization, tests                             | Done                   |
| Home + game + win overlay                                            | Done                   |
| Progress persistence (solved IDs), rounds counter, reset dialog      | Done                   |
| AdMob app open, Home banner, post-Results interstitial, rewarded hint | Done (test unit IDs)   |
| UMP consent gate + Settings → Privacy options                        | Done                   |
| Agent rules (`AGENTS.md`, `CLAUDE.md`, Cursor rule)                  | Done                   |
| Lint/build verification                                              | See Phase 0 acceptance |
| 100 licensed Arabic tracks, audio focus handling, accessibility pass | Pending                |
| Real AdMob IDs configured locally; AdMob consent messages pending     | Pending (console)      |
| Play Console re-upload                                               | Pending                |

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
screens were checked at 412 dp and 320 dp widths. (The monetization checks from this phase were
superseded by the 2026-11-10 placements in Phase 4.)

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

### Phase 4 — Monetization (placements implemented 2026-11-10)

The owner approved all four placements on 2026-11-10; this supersedes the earlier "no ads in the
learning loop" rule. Full unit table, placement rules, and risks: `docs/phase0/ads-plan.md`.

- **App open:** shown on a cold start once the Home screen is visible, onboarding is complete, and
  consent allows ads. At most one attempt per process and one per UTC day; the attempt is recorded
  when shown, so a failed load does not retry in the same day.
- **Home banner:** adaptive banner in the Home footer only. It never appears on lesson, feedback,
  or results screens.
- **Interstitial:** shown when the learner leaves Results after a newly completed lesson, every
  third lesson and never for the first. The Results screen itself is ad-free; the ad opens only on
  Next, Retry, or Home, after the dismissal callback returns.
- **Rewarded 50/50 hint:** the learner taps the hint button during a lesson to remove one wrong
  option. One hint per lesson, only on questions with three or more options, and the elimination is
  applied only after the reward callback. If no ad is available or consent is declined, the lesson
  stays playable and the UI reports that the hint is unavailable.
- **Rewarded interstitial:** unit ID is configured in the catalog but no UI uses it.
- **Consent:** UMP updates consent on launch, shows the form when required, and gates SDK
  initialization plus every load. Settings → Privacy options opens the UMP form when the provider
  requires it.
- **IDs:** Google's sample unit IDs in source and debug builds. Release builds read Gradle
  properties and `checkReleaseAdIds` fails the build when any real ID is missing.
- Deferred: remove-ads IAP and any placement inside active learning.

**Acceptance:** ads degrade gracefully offline, the rewarded hint is granted only after the reward
callback, no ad interrupts listening, answering, feedback, or the Results screen itself, and Data
Safety and Ads declarations match the shipped placements.

**Verified (2026-11-10):** `assembleDebug`, `testDebugUnitTest` (AdPolicy and hint engine tests),
`lintDebug`, and `connectedDebugAndroidTest` on a Galaxy S22 pass; `assembleRelease` passes with the
owner's real IDs stored outside the repo. On device with sample IDs: app open once per day, banner
only at the end of Home, no interstitial after lessons 1–2, interstitial after the third completed
lesson when leaving Results, rewarded hint removes one wrong option only after the reward callback,
early dismissal leaves the question intact, offline shows no ads and reports the hint as
unavailable, and Arabic/English hold at 320 dp. Consent messages, consent-form testing, and the
privacy-page deploy remain external setup.

### Phase 5 — Social (deferred)

- Play Games Services leaderboard (new app ID).
- Optional cloud save via Saved Games.

**Acceptance:** sign-in is optional, progress works unchanged when signed out, privacy policy
updated.

### Phase 6 — Play launch (1–2 weeks, overlapping Phase 2)

- Reactivate the existing listing with application ID `com.osmartapps.yallanghanee`; if Play has
  permanently deleted it, publish under a new ID instead and update this plan.
- Store listing in Arabic, screenshots, feature graphic, short/full description.
- Content rating questionnaire (Everyone / PEGI 3, ads present), Data Safety (AdMob, Analytics,
  and Crashlytics data), ads declaration (yes), privacy policy URL.
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

### Firebase telemetry

- [x] Add `app/google-services.json` for project `yallanganee-73906702`, including the
      `com.osmartapps.yallanghanee.dev` debug entry. Present in the working tree 2026-11-10;
      commit it with the release branch.
- [x] Keep Firebase BOM on 34.x until the Kotlin toolchain reaches 2.4; BOM 35 requires Kotlin 2.4.
      Pinned 34.19.0 (Analytics 23.2.0) on 2026-11-10.
- [x] Verify `screen_view`, `lesson_started`, `answer_submitted`, `lesson_completed`,
      `onboarding_completed`, `setting_changed`, and `rewarded_completed` arrive in Analytics.
      Verified 2026-11-10 on the signed release build: FA/FA-SVC debug logs showed every event
      during an Arabic onboarding, two lessons, and the switch to English; the batch upload
      returned HTTP 204.
- [x] Verify a test crash appears in Crashlytics after relaunch. Verified 2026-11-10 on the
      debug-signed `.dev` build sharing the same Firebase app: a main-thread exception was
      persisted by the handler and uploaded after relaunch (HTTP 200 to
      `crashlyticsreports-pa.googleapis.com`). The release `uploadCrashlyticsMappingFileRelease`
      task also succeeds. No NDK crash component ships because the app has no native code of its
      own; native media/WebView crashes are not collected.
- [x] Disclose Firebase data in both privacy pages.
- [ ] Declare Analytics and Crashlytics categories in the Play Data Safety form.
- [ ] Re-check ad fill before release: the test device logged `Ad failed to load : 3` (NO_FILL)
      for every real AdMob unit, so no banner, app-open, interstitial, or rewarded ad rendered.

### Technical

- [x] `targetSdk = 36`; test on API 36. Verified 2026-11-10 on the API 36 emulator and the
      API 36 phone.
- [x] 16 KB page-size compatibility. Verified 2026-11-10: the two bundled libraries
      (`libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`) have
      `p_align = 16384` on all four ABIs and `zipalign -c -P 16` passes.
- [x] Release build with R8; verify serialization models are kept. Verified 2026-11-10: a
      debug-key-signed release APK cold-starts offline in 0.9 s, loads the pack, and renders
      onboarding and Home.
- [x] Signed release build exercised end to end on the API 36 phone in Arabic and English:
      onboarding, streamed lesson audio and notation, feedback, Results, exit confirmation,
      Settings, language switch, 320 dp layout in both directions, and progress/language
      persistence across force-stop. Verified 2026-11-10 with a temporary release-test key; the
      Play upload key and Play App Signing remain owner actions.
- [x] Test offline install and first launch with no network; ads must fail silently. Verified
      2026-11-10: onboarding, Home, and Settings work; UMP and ad requests log only.
- [x] Verify the merged manifest declares only the expected entries: `INTERNET`,
      `ACCESS_NETWORK_STATE`, `com.google.android.gms.permission.AD_ID`, the three
      `ACCESS_ADSERVICES_*` permissions, `WAKE_LOCK`, `FOREGROUND_SERVICE`, the app-private
      `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, and the AdMob
      `com.google.android.gms.ads.APPLICATION_ID` metadata. All extras are library-merged from
      the ads and UMP SDK. Verified 2026-11-10.
- [x] Icon, adaptive icon, and splash behavior look correct on Android 12+. Replaced the legacy
      kids-game artwork on 2026-11-10 with an adaptive icon (night background, gold open ring,
      waveform bars, cream tonic dot) plus matching legacy PNGs and a monochrome layer; the
      Android 12 splash now shows the mark on the night background with no white plate. Store
      masters at `docs/store/ic_launcher_512.png` and `docs/store/ic_launcher_1024.png`.
- [x] Feature graphic and listing screenshots produced 2026-11-10. Feature graphic
      `docs/store/feature_graphic_1024x500.png` (1024×500, no alpha) uses the tuning-ring mark with
      the Aref Ruqaa wordmark and Plex tagline on the Night Majlis palette. Four framed phone
      screenshots `docs/store/screenshots/{ar,en}-{lesson,feedback}.jpg` (1080×2160, 2:1, opaque
      JPEG) captured from the device build. Tablet screenshots remain optional.

### Store listing

- [ ] Arabic title, short description, full description.
- [x] 512×512 icon, 1024×500 feature graphic, and four phone screenshots are ready in
      `docs/store/`; upload them when the listing copy is set.
- [ ] Category: Music or Trivia; tags; contact email.
- [ ] Privacy policy URL (host a simple Arabic/English page).
- [ ] Content rating questionnaire (music/lyrics, no user-generated content).
- [ ] Data Safety: declare device/ad IDs and ad interactions collected and shared for advertising.
- [ ] Ads declaration: yes (app open, banner, interstitial, rewarded).
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

| Dimension     | Cases                                                                                    |
| ------------- | ---------------------------------------------------------------------------------------- |
| Android       | 24, 28, 31, 34, 36                                                                       |
| Screens       | 320 dp, 360 dp, 411 dp, tablet                                                           |
| Locale        | Arabic; system English but app forced RTL                                                |
| Audio         | speaker, headphones, Bluetooth, call interruption, headset unplug                        |
| Lifecycle     | background/foreground, lock screen, process death, low memory                            |
| Network       | airplane mode, no SIM, Wi-Fi off; ad load failure paths                                  |
| Ads           | consent decline, app-open daily cap, 3rd-lesson interstitial, hint without reward, early reward dismissal, banner only on Home |
| Accessibility | TalkBack, large font, display size, contrast                                             |
| Content       | every level solvable, no duplicate answers, no clipped hooks                             |

### Regression hotspots

- Arabic normalization edge cases (hamza, ta marbuta, alef maqsura).
- Tray generation for titles with repeated letters.
- Audio player state after rapid play/pause or level change.
- Resume index after gaps in solved IDs, completing the final level, and reset.
- Layout overflow on 320 dp devices.
- Rounds counter double-incrementing or firing the interstitial on the wrong level.

---

## 10. Risks and mitigations

| Risk                                    | Impact                             | Mitigation                                                                       |
| --------------------------------------- | ---------------------------------- | -------------------------------------------------------------------------------- |
| Music licensing / copyright             | App removal, legal claims          | CC0/commissioned content, written licenses, legal review, no commercial rips     |
| Unknown reason for original removal     | Repeat removal                     | Check old Play Console notices before launch; fix the root cause                 |
| Content production slips                | Delayed launch                     | Start licensing now; ship 60 levels if needed; keep sample pack as fallback demo |
| Font/menu audio rights unverified       | Takedown request                   | Verify licenses or replace with OFL/CC0 assets                                   |
| Ambiguous Arabic answers                | Frustrating gameplay               | Editorial review; ban normalization-duplicate answers                            |
| Audio focus bugs                        | Bad UX                             | Implement AudioFocus + noisy handling in Phase 2                                 |
| Small-screen overflow                   | Unplayable on cheap devices        | Adaptive slot/tile sizing; test at 320 dp                                        |
| Removed listing cannot be reactivated   | No update path under the legacy ID | Confirm listing state in Play Console early; have a new-ID fallback ready        |
| Ad policy violation (placement/consent) | Removal or ad-serving limits       | Ads only on Home, cold start, Results exit, and opt-in rewarded; UMP consent gate    |
| New-account testing requirements        | Delayed production access          | Plan the 12-tester/14-day closed test early                                      |
| Telemetry starts before EEA consent     | GDPR complaint or removal         | Owner chose always-on; add analytics purposes to the UMP message before EEA launch |

---

## 11. Decisions still needed from the owner

Resolved: application ID (`com.osmartapps.yallanghanee`), content volume (100 tracks), monetization
(app open, Home banner, post-Results interstitial every third lesson, rewarded 50/50 hint), removal
reason (policy and targeting), and real AdMob app/unit IDs (2026-11-10). Real IDs live in
`~/.gradle/gradle.properties`, never in the repository; the AdMob console still needs GDPR/US-states
consent messages before release.

Still open:

1. **Branding** — keep the name "يلا نغني" and logo, or rebrand to distance from the removed listing?
2. **Content source** — commission tracks, buy a game-compatible library, or use CC0 recordings?
3. **Old device** — is there a phone/backup where the original app ran? Decide whether to recover the
   original levels/songs for reference (not necessarily for shipping).
4. **Exact removal notice** — retrieve the policy/targeting email so the fix can be documented in
   the release notes or appeal.
5. **Play account type** — personal or organization? This affects testing requirements.
6. **Privacy policy hosting** — where will it live, and who writes the Arabic/English text?
7. **Font licensing** — can you provide the Kharabeesh font license, or should we switch to OFL?

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
