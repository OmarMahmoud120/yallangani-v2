# AGENTS.md

## What this project is

**يلا نغني (Yalla Nghanni)** — an Arabic/English maqam ear-training app for Android. Arabic is the
default and the interface switches to English from Settings. The player hears a short phrase, then
chooses which maqam it belongs to.

This is a clean-room recreation of the removed 2017 app `com.osmartapps.yallanghanee`. It keeps the
legacy `applicationId` because it updates the **same Play listing**. The old backend and downloaded
song pack are gone; a small content pack ships inside the APK. The legacy app lives at
`../yallanganee` for visual reference only.

The product pivoted from song-title spelling to **“تعلّم اللحن”**: recognition-first lessons with
teaching feedback, slow playback, and A/B comparison. The approved UX contract and findings live in
`docs/phase0/ui-ux-audit.md`.

## Current scope

- Bundled catalog in `app/src/main/assets/content/` covering all 35 tracked maqams, plus 14
  recognition lessons (three authored Pack 1 lessons, then exploration groups of 2–3 maqams).
- Lesson audio streams a random site track per question (`media_base` + track path). Only the three
  Pack 1 phrases and the drone bed are bundled in the APK.
- Feedback shows the maqam's notation image, the track title/artist, credits, and the MaqamWorld
  source link after every answer.
- Persisted completed lessons, motion preference, and app language (DataStore).
- Bilingual UI: Arabic is the default; Settings switches the whole app to English (LTR), including
  maqam names, lesson titles, onboarding, feedback, and the pack notice.
- Pitch-preserved slow mode (`0.75×`), A/B contrast playback, audio-focus and unplug handling.
- **Ads stay out of active learning.** AdMob shows an app-open ad on cold start (once a day), a
  banner at the bottom of Home, and an interstitial after Results is dismissed on every third
  completed lesson. A rewarded ad grants one 50/50 hint per lesson on questions with three or more
  options. Lessons, feedback, playback, onboarding, and Results themselves show no ads. All ad
  loading is gated on UMP consent.
- **Telemetry.** Firebase Analytics records screen views and the lesson funnel; Crashlytics
  records crashes and errors. Both initialize at first launch, collect no free text or audio, and
  are disclosed in the privacy policy and Data Safety form. Ads remain UMP-gated; telemetry is not.
- **Out of scope:** coin economy, daily rewards, paid hints, onboarding carousel, leaderboards,
  cloud save, push notifications, accounts, Firestore/Auth/Remote Config/Messaging.

## Stack

- Kotlin 2.2, Jetpack Compose (Material 3), single `:app` module.
- AGP 8.13, Gradle 8.14.3, JDK 17 target (build runs on JDK 21/23 locally).
- `compileSdk`/`targetSdk` 36, `minSdk` 24. `versionCode` 13, `versionName` 2.0.0.
- kotlinx.serialization for the maqam pack, DataStore Preferences for progress and language.
- Localized resources without AppCompat: `values/` (Arabic) plus `values-en/`, selected at runtime
  with `createConfigurationContext` and Compose `Local*` providers.
- MediaPlayer/AudioFocus for phrases and HTTPS track streaming; bundled OFL fonts (IBM Plex Sans
  Arabic + Aref Ruqaa).
- Google Mobile Ads SDK 25.5.0 plus User Messaging Platform 4.0.0; app ID via `${admobAppId}`,
  real unit IDs from Gradle properties only in release builds.
- Firebase BOM 34.19.0 with Analytics and Crashlytics, configured by the committed
  `app/google-services.json` (project `yallanganee-73906702`). The BOM stays on 34.x until the
  Kotlin toolchain moves to 2.4; BOM 35 depends on Kotlin 2.4 metadata.

## Commands

```bash
# Debug build
./gradlew :app:assembleDebug

# JVM unit tests (recognition engine, pack validation, bundled content)
./gradlew :app:testDebugUnitTest

# Instrumented UI test (needs a running emulator/device)
./gradlew :app:connectedDebugAndroidTest

# Lint
./gradlew :app:lintDebug

# One test class
./gradlew :app:testDebugUnitTest --tests "com.osmartapps.yallanghanee.game.RecognitionEngineTest"

# Rebuild the site catalog from the crawl database (carries authored fields over)
python3 tools/build_site_game_data.py

# Regenerate the app pack from the canonical site JSON
python3 tools/sync_app_pack.py
```

### Device testing with the Android CLI

The `android` CLI (`~/.local/bin/android`, see `.agents/skills/android-cli`) is the preferred way to
drive an emulator or a physical phone. With one device connected the `--device` flag is optional;
pass it when an emulator is also running.

```bash
adb devices -l

# Install + launch the debug build (debug package is com.osmartapps.yallanghanee.dev)
android run --device=R5CT920A4EJ --apks=app/build/outputs/apk/debug/app-debug.apk

# UI tree as JSON, with text/content-desc, bounds, and center coordinates
android layout --device=R5CT920A4EJ --flat

# PNG screenshot; `--annotate` labels elements that text search cannot find
android screen capture --device=R5CT920A4EJ -o /tmp/screen.png

# Official Android docs lookup
android docs search "audio focus"
```

Tap elements with `adb shell input tap <x> <y>` using the `center` from `android layout`. Always
look at a screenshot before acting on it. Verify audio focus with
`adb shell dumpsys audio | grep -A 4 "Audio Focus stack"`.

Narrow-screen check (320 dp):

```bash
adb shell wm size 640x1280
adb shell wm density 320
# ... verify, then restore
adb shell wm size reset
adb shell wm density reset
```

The wrapper needs `local.properties` with `sdk.dir`. It is gitignored. On this machine:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ...
```

Real AdMob IDs must never be committed. If ads are reintroduced, put ids in
`~/.gradle/gradle.properties`:

```properties
admob.appId=ca-app-pub-...~...
admob.interstitialUnitId=ca-app-pub-.../...
admob.rewardedUnitId=ca-app-pub-.../...
```

Without overrides the build uses Google's sample test unit IDs.

## Agent skills

Project skills live in `.agents/skills/`:

- `android-cli/` — Google's Android CLI guide. Use it for device and emulator work: `android run`,
  `android install`, `android layout` (JSON UI tree with bounds and centers), `android screen`
  (PNG capture, `--annotate` for unlabeled elements), `android input`, `android emulator`, and
  `android docs`. Prefer `layout` for element discovery; use screenshots to confirm rendering.
- `caveman/` and `ultracave/` — terse response-voice skills (auto-on; `/caveman`,
  `/caveman ultra`). They change chat style only. Code, comments, docs, commit messages, and tests
  always stay in normal prose.

The debug build installs as `com.osmartapps.yallanghanee.dev` (`applicationIdSuffix = ".dev"`) so it
can coexist with the legacy Play install on a physical phone. Release builds keep the real ID.

## Architecture map

| Path | Responsibility |
| --- | --- |
| `model/MaqamContent.kt` | `MaqamPack` / `Maqam` / `RecognitionLesson` / `RecognitionItem` models and language-aware accessors. |
| `model/AppLanguage.kt` | `ARABIC` (default, RTL) and `ENGLISH` (LTR) with their locale codes. |
| `game/RecognitionEngine.kt` | Pure Kotlin recognition session: shuffle options, answer, next, skip, 50/50 hint state. No Android imports. |
| `game/RecognitionViewModel.kt` | Loads pack/progress, exposes UI state, marks completed lessons, tracks ad counters, triggers pack refresh on home. |
| `data/MaqamRepository.kt` | Bundled pack load, cached baseline selection, remote refresh delegation. |
| `data/PackUpdater.kt` | Version check, digest-verified clip downloads, validation, cache write, prune. |
| `data/PackCache.kt` | Atomic cache of `pack.json` and clips under `filesDir/pack`. |
| `data/RemotePackSource.kt` | HTTPS-only, no-redirect, size-capped pack fetch. |
| `data/PackClips.kt` | Referenced clip names, safe-name check, SHA-256 helpers. |
| `data/MaqamPackValidator.kt` | Content rules used by the app and unit tests. |
| `data/ProgressStore.kt` | DataStore-backed completed-lesson set and ad counters. |
| `data/SettingsStore.kt` | DataStore-backed motion and language preferences. |
| `audio/AudioPlayer.kt` | MediaPlayer playback for assets and HTTPS tracks (async prepare), pitch-preserved speed, focus, unplug handling. |
| `audio/AudioFocus.kt` | Transient audio focus and noisy-audio receiver. |
| `ui/LocalizedContent.kt` | Runtime locale wrapper: localized `Context`/`Configuration`/`Resources`, layout direction, `LocalAppLanguage`. |
| `ui/theme/Theme.kt` | Night Majlis tokens, per-maqam accents, two-font typography (Latin display falls back to IBM Plex). |
| `ui/HomeScreen.kt` | Continue card, three-maqam path, progress, settings entry, banner footer. |
| `ui/SettingsScreen.kt` | Language and motion preferences, UMP privacy options, about/licences, destructive reset. |
| `ui/LessonScreen.kt` | Listen orb, slow mode, A/B comparison, rewarded hint, answers, teaching feedback, exit confirm. |
| `ui/ResultsScreen.kt` | Score, pass/fail copy, learned summary, next/retry/home. |
| `ui/YallaApp.kt` | Routing from view-model state; consent, app-open, post-Results interstitial, language wrapper. |
| `ui/components/` | Sigils, listen orb, answer cards, progress dots, mastery ring, banner, loading/error. |
| `ads/AdsController.kt` | Loads and shows app-open, interstitial, and rewarded ads; banner lives in `ui/components/AdBanner.kt`. |
| `ads/ConsentManager.kt` | UMP consent update, form display, privacy options entry point. |
| `ads/AdPolicy.kt` | Pure rules for interstitial frequency and daily app-open cap. |
| `telemetry/Telemetry.kt` | Firebase Analytics event helpers; initialized in `MainActivity`. |
| `app/google-services.json` | Firebase client config for project `yallanganee-73906702`, including the `.dev` debug package entry. |

## Non-negotiable rules

1. **The content pack is the only source of maqams and lessons.** Never hardcode names, signatures,
   explanations, notation, credits, track titles, or audio file names in Kotlin. Edit
   `site/public/data/maqams.json`, run `python3 tools/sync_app_pack.py` to write the app pack, and
   add bundled audio to `assets/content/audio/`.
2. **Keep the recognition engine pure.** `RecognitionEngine` must stay free of Android imports (ads
   included) so it can be unit tested without Robolectric.
3. **Every bundled audio file must be documented** in `docs/CONTENT_LICENSES.md`. Do not ship
   commercial recordings or rips. The current bundled phrases are synthesized in-repo (CC0) and
   marked in the pack notice; replace them before release. Site tracks are streamed at runtime, not
   shipped, and carry their title/artist/credits from the pack. The MaqamWorld crawl has no filed
   written approval reference; treat hosted playback as an open rights risk.
4. **Bilingual, RTL first.** Arabic is the default; English is a full translation, not a fallback.
   Verify both languages in RTL/LTR at 320 dp width and with TalkBack. Answer cards and progress
   must announce position and state in the active language.
5. **Strings live in `res/values/strings.xml` and `res/values-en/strings.xml`.** Both files carry
   the same keys; no user-facing literals in composables. Content copy lives in the pack's Arabic
   and English fields, never in Kotlin.
6. **No new dependencies without a reason.** Prefer platform APIs. Keep the version catalog
   (`gradle/libs.versions.toml`) as the single source of versions.
7. **No secrets in the repo.** `local.properties`, Gradle property overrides, keystores, and
   signing files stay out of git. Test ad IDs are the only ad IDs allowed in source.
   `app/google-services.json` is committed on purpose: it is restricted client configuration, not
   a credential, and the build needs it. Never add service-account keys or `.env` files.
8. **A usable pack always exists.** The bundled or last-known-good pack must load, validate, and
   run lessons offline; remote refresh fails silently back to it. Lesson audio and notation for
   site maqams need a network connection, and the UI must not claim otherwise.
9. **Ads follow the approved placements.** App open on cold start (once a day, never during
   onboarding), banner on Home only, interstitial after Results is dismissed every third completed
   lesson, and a rewarded 50/50 hint on lessons with three or more options. Never show an ad during
   playback, an unanswered question, feedback, Results, or onboarding. Load ads only when UMP
   consent allows it, and keep `RecognitionEngine` free of ad code.
10. **Feedback must teach.** After every answer show the correct maqam and its signature
    explanation; never return a bare score.
11. **No legacy infrastructure.** Do not reintroduce the osmartapps.com API, `audio.zip`,
    OneSignal, Firestore, Firebase Auth, Remote Config, Messaging, Google Play Games, or anjlab
    billing. Firebase Analytics and Crashlytics are the only Firebase SDKs in the app. The static
    Firebase Hosting site remains the content origin for `pack.json` and `app-audio/`, reachable
    over plain HTTPS without a Firebase client dependency.
12. **Telemetry stays anonymous and sparse.** Send ids, counts, and booleans only through
    `telemetry/Telemetry.kt`: no names, email, free text, audio, precise location, or maqam
    explanation text. Keep event names in `docs/phase0/analytics-taxonomy.md`; update the privacy
    pages and Data Safety form whenever the collected data changes.

## Definition of done

A change is done only when:

- `./gradlew :app:assembleDebug` passes.
- `./gradlew :app:testDebugUnitTest` passes.
- `./gradlew :app:lintDebug` has no new errors.
- `./gradlew :app:connectedDebugAndroidTest` passes when the change touches UI or the flow.
- Content changes pass `BundledMaqamPackTest` (unique IDs, valid colors, notation, tracks, and
  Arabic+English copy for every maqam, existing bundled audio, valid answers, every maqam covered
  by a lesson).
- The feature was exercised on a device in both languages, including RTL/LTR and a 320 dp-wide
  screen; the language choice survives relaunch.
- New strings are translated in both `strings.xml` files; new audio is licensed and documented.
- Telemetry changes keep `docs/phase0/analytics-taxonomy.md`, both privacy pages, and the Data
  Safety declaration in sync.
- No real ad IDs, keystores, or credentials were added to git.

## Content pack format

`site/public/data/maqams.json` is canonical: it carries the site catalog (all maqams and their
tracks) plus the authored Arabic and English fields, onboarding, and lessons. The app copy at
`app/src/main/assets/content/maqams.json` is generated for every tracked maqam by
`tools/sync_app_pack.py`; never edit the app copy by hand.

App pack shape (`app/src/main/assets/content/maqams.json`):

```json
{
  "version": 4,
  "content_version": "12-hex digest",
  "media_base": "https://yallanganee-73906702.web.app/data/",
  "notice": "development pack notice",
  "notice_en": "development pack notice in English",
  "maqams": [
    {
      "id": "hijaz",
      "name_ar": "حجاز",
      "name_en": "Hijaz",
      "color": "#C97B5A",
      "jins_ar": "جنس حجاز",
      "jins_en": "Jins Hijaz",
      "signature_ar": "one-sentence teaching explanation",
      "signature_en": "one-sentence teaching explanation in English",
      "status": "approved",
      "notation": "media/maqam/hijaz/hijaz.png",
      "source_url": "https://www.maqamworld.com/en/maqam/hijaz.php",
      "tracks": [
        {
          "title": "Muwashah ...",
          "artist": "Al Turath Ensemble",
          "audio": "media/maqam/hijaz/track.mp3",
          "credits": ["Music by ..."]
        }
      ],
      "audio": "01-alhijaz.mp3"
    }
  ],
  "onboarding": {
    "steps": [
      {
        "id": "step",
        "prompt_ar": "سؤال",
        "prompt_en": "Question",
        "audio": "on-1.mp3",
        "options": [{ "id": "first", "label_ar": "الأولى", "label_en": "First" }],
        "answer": "first",
        "explanation_ar": "شرح",
        "explanation_en": "Explanation"
      }
    ],
    "first_win": {
      "prompt_ar": "أي مقام؟",
      "prompt_en": "Which maqam?",
      "audio": "on-win.mp3",
      "maqam_ids": ["hijaz", "nahawand"],
      "answer": "hijaz",
      "explanation_ar": "شرح",
      "explanation_en": "Explanation"
    }
  },
  "lessons": [
    {
      "id": "hijaz_nahawand",
      "title_ar": "حجاز أم نهاوند؟",
      "title_en": "Hijaz or Nahawand?",
      "maqam_ids": ["hijaz", "nahawand"],
      "items": [
        {
          "id": "hn_1",
          "answer": "hijaz",
          "explanation_ar": "optional override",
          "explanation_en": "optional override in English"
        }
      ]
    }
  ]
}
```

Rules: unique maqam and lesson ids; `status` must be `approved`; colors are `#RRGGBB`; every maqam
has `notation` and at least one track with title, artist, and audio; every lesson offers at least two
distinct known maqams; every item answer is one of that lesson’s options; every maqam is covered by
a lesson. English copy (`name_en`, `jins_en`, `signature_en`, onboarding `prompt_en`/`label_en`/
`explanation_en`, lesson `title_en`) is required by the validator. Bundled `audio`/`drone` are plain
file names inside `content/audio/`; tracks and notation are relative paths under `media_base`. The
loader fails fast on violations.

## Reference material

- UX audit and screen contract: `docs/phase0/ui-ux-audit.md`
- Bilingual app/content/site plan: `docs/phase0/bilingual-plan.md`
- Technical spike: `docs/phase0/technical-spike.md`
- Site tracks and streaming plan: `docs/phase0/site-tracks-plan.md`
- Maqam content/audio spec: `docs/phase0/maqam-content-audio-spec.md`
- Analytics taxonomy: `docs/phase0/analytics-taxonomy.md`
- Content licensing: `docs/CONTENT_LICENSES.md`
- Recreation plan: `docs/RECREATION_PLAN.md`
- Legacy app (read-only reference): `../yallanganee`
