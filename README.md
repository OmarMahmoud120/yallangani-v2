# Yalla Nghanni 2

A clean recreation of the Arabic song-guessing game **يلا نغني**, shipped as an update to the
original Play listing (`com.osmartapps.yallanghanee`).
The player hears a short clip, then taps shuffled Arabic letters to spell the song title.

- Kotlin 2.2 + Jetpack Compose (Material 3), single `:app` module
- `compileSdk`/`targetSdk` 36, `minSdk` 24, `versionCode` 13 / `versionName` 2.0.0
- Gameplay, content, and progress are fully offline
- AdMob, policy-safe: interstitial between rounds after every 5th solved round, opt-in rewarded ad
  for the hint button. No banners, no app-open ads
- Bundled sample content pack (synthesized maqam melodies, CC0); target pack is 100 licensed Arabic
  tracks

## Build

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  ./gradlew :app:assembleDebug
```

`local.properties` must point at your Android SDK (`sdk.dir=...`).

## Test

```bash
# JVM unit tests
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  ./gradlew :app:testDebugUnitTest

# UI flow test (requires a running emulator/device)
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  ./gradlew :app:connectedDebugAndroidTest
```

## AdMob configuration

Debug builds and tests use Google's sample ad unit IDs. Real IDs are never committed; put them in
`~/.gradle/gradle.properties`:

```properties
admob.appId=ca-app-pub-...~...
admob.interstitialUnitId=ca-app-pub-.../...
admob.rewardedUnitId=ca-app-pub-.../...
```

The app ID is injected into the manifest through the `${admobAppId}` placeholder; unit IDs become
`BuildConfig` fields consumed by `ads/AdsController.kt`.

## Project layout

```
app/src/main/java/com/osmartapps/yallanghanee/
  YallaApplication.kt  MobileAds initialization
  game/     pure Kotlin engine, Arabic text rules, ViewModel
  data/     content loading/validation, progress persistence
  ads/      interstitial and rewarded ad controller
  audio/    MediaPlayer/SoundPool wrappers
  ui/       Compose screens and theme
  model/    content pack models
app/src/main/assets/content/
  levels.json
  audio/*.mp3
docs/       recreation plan, content licensing
tools/      sample audio generator, melody-render MIDI batch renderer
```

## Content

The bundled pack is placeholder content: ten original synthesized melodies in different maqam
scales. It is safe to ship but must be replaced with 100 licensed Arabic tracks before launch.
See [docs/CONTENT_LICENSES.md](docs/CONTENT_LICENSES.md) and
[docs/RECREATION_PLAN.md](docs/RECREATION_PLAN.md).

## Legacy reference

The original 2017 app is kept read-only at `../yallanganee`. Its backend is intentionally not
reused: the level API and audio server are dead, the bundled database is encrypted, and the old SDK
integrations (OneSignal, Firebase, Play Games, billing) are out of scope. AdMob is implemented
fresh against the current SDK, not ported from the legacy code.

## Agent setup

Project rules for coding agents live in [AGENTS.md](AGENTS.md), with Claude and Cursor entry
points in `CLAUDE.md` and `.cursor/rules/project.mdc`.
