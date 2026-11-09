# AGENTS.md

## What this project is

**يلا نغني (Yalla Nghanni)** — an Arabic/RTL song-title guessing game for Android.
The player hears a short clip, then taps shuffled Arabic letters to spell the song title.

This is a clean-room recreation of the removed 2017 app `com.osmartapps.yallanghanee`. It is an
update to the **same Play listing**, so it keeps the legacy `applicationId`. The old backend and
downloaded audio pack are gone; content ships inside the APK. The legacy app lives at
`../yallanganee` for visual reference only.

## Current scope

- Offline content pack bundled in `app/src/main/assets/content/` (target: **100 Arabic tracks**).
- Home screen, level progression, song playback, letter board, answer validation, win flow.
- Persisted progress and rounds counter (DataStore).
- Monetization (policy-safe, test IDs by default):
  - Interstitial **between rounds, after every 5th solved round**, full-screen only.
  - **Opt-in rewarded ad** for the hint button; granting one correctly placed letter.
  - No banners, no app-open ads, no ads before the first round, no ads during playback.
- **Out of scope:** coin economy, daily rewards, paid hints, in-app purchases, leaderboards,
  cloud save, push notifications, remote content, accounts, analytics, crash reporting.
- `INTERNET` and `ACCESS_NETWORK_STATE` exist **only** for AdMob. Gameplay and progress must work
  fully with airplane mode on; ad calls no-op and the game continues.

## Stack

- Kotlin 2.2, Jetpack Compose (Material 3), single `:app` module.
- AGP 8.13, Gradle 8.14.3, JDK 17 target (build runs on JDK 21/23 locally).
- `compileSdk`/`targetSdk` 36, `minSdk` 24. `versionCode` 13, `versionName` 2.0.0.
- kotlinx.serialization for the content pack, DataStore Preferences for progress.
- MediaPlayer/SoundPool for audio, Lottie for the owned brand animations.
- Google Mobile Ads SDK 25.5.0 (app ID via manifest placeholder `${admobAppId}`).

## Commands

```bash
# Debug build
./gradlew :app:assembleDebug

# JVM unit tests (game engine, Arabic text, content pack validation)
./gradlew :app:testDebugUnitTest

# Instrumented UI test (needs a running emulator/device)
./gradlew :app:connectedDebugAndroidTest

# Lint
./gradlew :app:lintDebug

# One test class
./gradlew :app:testDebugUnitTest --tests "com.osmartapps.yallanghanee.game.GameEngineTest"
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

The wrapper needs `local.properties` with `sdk.dir`. It is gitignored. On this machine:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ...
```

Real AdMob IDs must never be committed. Put them in `~/.gradle/gradle.properties`:

```properties
admob.appId=ca-app-pub-...~...
admob.interstitialUnitId=ca-app-pub-.../...
admob.rewardedUnitId=ca-app-pub-.../...
```

Without overrides the build uses Google's sample test unit IDs, which is what all automated tests
and local development use.

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
| `YallaApplication.kt` | Process init; initializes MobileAds. |
| `model/Content.kt` | `ContentPack` / `Level` serializable models. |
| `game/ArabicText.kt` | Arabic normalization, answer comparison, tray generation. |
| `game/GameEngine.kt` | Pure Kotlin game state, including `revealHint()`. No Android imports. |
| `game/GameViewModel.kt` | Loads content/progress/rounds, exposes UI state, schedules wrong-answer reset. |
| `data/ContentRepository.kt` | Reads and validates `assets/content/levels.json` + audio assets. |
| `data/ContentPackValidator.kt` | Content rules used by the app and unit tests. |
| `data/ProgressStore.kt` | DataStore-backed current level and completed-rounds counter. |
| `ads/AdsController.kt` | Preloads/shows interstitial and rewarded ads; the only ad entry point. |
| `audio/` | MediaPlayer/SoundPool wrappers for song, menu music, and win sound. |
| `ui/` | Compose screens, theme, asset loads. |

## Non-negotiable rules

1. **Arabic text goes through `ArabicText.kt`.** Never compare raw strings. Normalization handles
   hamza variants, ta marbuta, alef maqsura, diacritics, and tatweel. Add a unit test for any new
   normalization rule.
2. **The content pack is the only source of levels.** Never hardcode titles, hints, or audio file
   names in Kotlin. Add levels to `levels.json` and audio to `assets/content/audio/`.
3. **Every audio file must be licensed and documented** in `docs/CONTENT_LICENSES.md`. Do not ship
   commercial recordings or rips. The current pack is synthesized in-repo (CC0) until the licensed
   100-track pack lands.
4. **Keep the game engine pure.** `GameEngine` must stay free of Android imports (ads included) so
   it can be unit tested without Robolectric.
5. **RTL first.** The app is Arabic-only. Verify layouts in RTL and on narrow screens (320 dp).
6. **Strings live in `res/values/strings.xml`.** No user-facing literals in composables.
7. **No new dependencies without a reason.** Prefer platform APIs. Keep the version catalog
   (`gradle/libs.versions.toml`) as the single source of versions.
8. **No secrets in the repo.** `local.properties`, Gradle property overrides, keystores, and
   signing files stay out of git. Test ad IDs are the only ad IDs allowed in source.
9. **Offline gameplay is sacred.** Content, progress, and the full game loop work with no network.
   Ads are additive: a failed load must never block or delay level progression.
10. **Ad placement is fixed.** Interstitial only between rounds after each 5th solved round;
    rewarded only when the player taps the hint button. No banners, no app-open ads, no ads during
    audio playback, never an ad that blocks the first round. Changing placements is a product
    decision, not an implementation detail.
11. **Ads stay behind `AdsController`.** UI code calls it; it must not add billing or analytics.
12. **No legacy infrastructure.** Do not reintroduce the osmartapps.com API, `audio.zip`,
    OneSignal, Firebase, Google Play Games, or anjlab billing.

## Definition of done

A change is done only when:

- `./gradlew :app:assembleDebug` passes.
- `./gradlew :app:testDebugUnitTest` passes.
- `./gradlew :app:lintDebug` has no new errors.
- `./gradlew :app:connectedDebugAndroidTest` passes when the change touches UI, ads, or the flow.
- Content changes pass `ContentPackTest` (unique IDs, valid Arabic titles, existing audio files).
- The feature was exercised on an emulator/device, including RTL and a narrow screen.
- New strings are Arabic and live in `strings.xml`; new audio is licensed and documented.
- No real ad IDs, keystores, or credentials were added to git.

## Content pack format

`app/src/main/assets/content/levels.json`:

```json
{
  "version": 1,
  "question": "ما اسم هذه الأغنية؟",
  "notice": "optional notice shown in docs",
  "levels": [
    { "id": 1, "title": "لحن الحجاز", "audio": "01-alhijaz.mp3" }
  ]
}
```

Rules: unique positive `id`, non-blank Arabic `title` (letters only + spaces, max 16 usable
letters), `audio` is a plain file name inside `content/audio/`. The loader fails fast on violations.

## Reference material

- Recreation plan: `docs/RECREATION_PLAN.md`
- Content licensing: `docs/CONTENT_LICENSES.md`
- Legacy app (read-only reference): `../yallanganee`
