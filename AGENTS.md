# AGENTS.md

## What this project is

**يلا نغني (Yalla Nghanni)** — an Arabic/RTL maqam ear-training app for Android.
The player hears a short phrase, then chooses which maqam it belongs to.

This is a clean-room recreation of the removed 2017 app `com.osmartapps.yallanghanee`. It keeps the
legacy `applicationId` because it updates the **same Play listing**. The old backend and downloaded
song pack are gone; a small content pack ships inside the APK. The legacy app lives at
`../yallanganee` for visual reference only.

The product pivoted from song-title spelling to **“تعلّم اللحن”**: recognition-first lessons with
teaching feedback, slow playback, and A/B comparison. The approved UX contract and findings live in
`docs/phase0/ui-ux-audit.md`.

## Current scope

- Offline maqam pack bundled in `app/src/main/assets/content/` (3 approved starter maqams:
  Rast, Hijaz, Nahawand; target pack: 18 phrases per maqam).
- Home path, three recognition lessons (`4` items each), lesson player, teaching feedback, results.
- Persisted completed lessons (DataStore).
- Pitch-preserved slow mode (`0.75×`), A/B contrast playback, audio-focus and unplug handling.
- **No ads in the learning loop.** AdMob and `AdsController` remain in the codebase but are
  unreferenced by UI and game code. Reintroducing ads is a product decision, not an implementation
  detail.
- **Out of scope:** coin economy, daily rewards, paid hints, onboarding carousel, leaderboards,
  cloud save, push notifications, remote content, accounts, analytics, crash reporting.
- `INTERNET` and `ACCESS_NETWORK_STATE` exist **only** for the retained Ads SDK. Playback, lessons,
  and progress must work fully in airplane mode.

## Stack

- Kotlin 2.2, Jetpack Compose (Material 3), single `:app` module.
- AGP 8.13, Gradle 8.14.3, JDK 17 target (build runs on JDK 21/23 locally).
- `compileSdk`/`targetSdk` 36, `minSdk` 24. `versionCode` 13, `versionName` 2.0.0.
- kotlinx.serialization for the maqam pack, DataStore Preferences for progress.
- MediaPlayer/AudioFocus for phrases; bundled OFL fonts (IBM Plex Sans Arabic + Aref Ruqaa).
- Google Mobile Ads SDK 25.5.0 (retained, unreferenced; app ID via `${admobAppId}`).

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
| `YallaApplication.kt` | Process init; initializes MobileAds (retained but unused by the lesson flow). |
| `model/MaqamContent.kt` | `MaqamPack` / `Maqam` / `RecognitionLesson` / `RecognitionItem` models. |
| `game/RecognitionEngine.kt` | Pure Kotlin recognition session: shuffle options, answer, next, skip. No Android imports. |
| `game/RecognitionViewModel.kt` | Loads pack/progress, exposes UI state, marks completed lessons. |
| `data/MaqamRepository.kt` | Reads `assets/content/maqams.json` and validates audio assets. |
| `data/MaqamPackValidator.kt` | Content rules used by the app and unit tests. |
| `data/ProgressStore.kt` | DataStore-backed completed-lesson set. |
| `data/SettingsStore.kt` | DataStore-backed motion preference. |
| `audio/AudioPlayer.kt` | MediaPlayer sequence playback with pitch-preserved speed, focus, and unplug handling. |
| `audio/AudioFocus.kt` | Transient audio focus and noisy-audio receiver. |
| `ui/theme/Theme.kt` | Night Majlis tokens, per-maqam accents, two-font typography. |
| `ui/HomeScreen.kt` | Continue card, three-maqam path, progress, settings entry. |
| `ui/SettingsScreen.kt` | Motion preference, about/licences, destructive reset. |
| `ui/LessonScreen.kt` | Listen orb, slow mode, A/B comparison, answers, teaching feedback, exit confirm. |
| `ui/ResultsScreen.kt` | Score, pass/fail copy, learned summary, next/retry/home. |
| `ui/YallaApp.kt` | Home/Lesson/Results routing from view-model state. |
| `ui/components/` | Sigils, listen orb, answer cards, progress dots, mastery ring, loading/error. |
| `ads/AdsController.kt` | Retained ad entry point; must not be called from the learning loop. |

## Non-negotiable rules

1. **The content pack is the only source of maqams and lessons.** Never hardcode names, signatures,
   explanations, or audio file names in Kotlin. Add content to `maqams.json` and audio to
   `assets/content/audio/`.
2. **Keep the recognition engine pure.** `RecognitionEngine` must stay free of Android imports (ads
   included) so it can be unit tested without Robolectric.
3. **Every audio file must be licensed and documented** in `docs/CONTENT_LICENSES.md`. Do not ship
   commercial recordings or rips. The current phrases are synthesized in-repo (CC0) and marked in
   the pack notice; replace them before release.
4. **RTL first.** The app is Arabic-only. Verify layouts in RTL, at 320 dp width, and with TalkBack.
   Answer cards and progress must announce position and state.
5. **Strings live in `res/values/strings.xml`.** No user-facing literals in composables.
6. **No new dependencies without a reason.** Prefer platform APIs. Keep the version catalog
   (`gradle/libs.versions.toml`) as the single source of versions.
7. **No secrets in the repo.** `local.properties`, Gradle property overrides, keystores, and
   signing files stay out of git. Test ad IDs are the only ad IDs allowed in source.
8. **Offline gameplay is sacred.** Content, audio, lessons, and progress work with no network.
9. **No ads in the learning loop.** No interstitials, rewarded hints, banners, or app-open ads
   during onboarding, lessons, feedback, results, or playback. `AdsController` must remain
   unreferenced by `ui/` and `game/`. Changing this is a product decision.
10. **Feedback must teach.** After every answer show the correct maqam and its signature
    explanation; never return a bare score.
11. **No legacy infrastructure.** Do not reintroduce the osmartapps.com API, `audio.zip`,
    OneSignal, Firebase, Google Play Games, or anjlab billing.

## Definition of done

A change is done only when:

- `./gradlew :app:assembleDebug` passes.
- `./gradlew :app:testDebugUnitTest` passes.
- `./gradlew :app:lintDebug` has no new errors.
- `./gradlew :app:connectedDebugAndroidTest` passes when the change touches UI or the flow.
- Content changes pass `BundledMaqamPackTest` (unique IDs, valid colors, existing audio, valid
  answers).
- The feature was exercised on a device, including RTL and a 320 dp-wide screen.
- New strings are Arabic and live in `strings.xml`; new audio is licensed and documented.
- No real ad IDs, keystores, or credentials were added to git.

## Content pack format

`app/src/main/assets/content/maqams.json`:

```json
{
  "version": 1,
  "notice": "development pack notice",
  "maqams": [
    {
      "id": "hijaz",
      "name_ar": "حجاز",
      "color": "#C97B5A",
      "jins_ar": "جنس حجاز",
      "signature_ar": "one-sentence teaching explanation",
      "audio": "01-alhijaz.mp3",
      "status": "approved"
    }
  ],
  "lessons": [
    {
      "id": "hijaz_nahawand",
      "title_ar": "حجاز أم نهاوند؟",
      "maqam_ids": ["hijaz", "nahawand"],
      "items": [
        { "id": "hn_1", "answer": "hijaz", "explanation_ar": "optional override" }
      ]
    }
  ]
}
```

Rules: unique maqam and lesson ids; `status` must be `approved`; colors are `#RRGGBB`; every lesson
offers at least two distinct known maqams; every item answer is one of that lesson’s options; audio
is a plain file name inside `content/audio/`. The loader fails fast on violations.

## Reference material

- UX audit and screen contract: `docs/phase0/ui-ux-audit.md`
- Technical spike: `docs/phase0/technical-spike.md`
- Maqam content/audio spec: `docs/phase0/maqam-content-audio-spec.md`
- Analytics taxonomy: `docs/phase0/analytics-taxonomy.md`
- Content licensing: `docs/CONTENT_LICENSES.md`
- Recreation plan: `docs/RECREATION_PLAN.md`
- Legacy app (read-only reference): `../yallanganee`
