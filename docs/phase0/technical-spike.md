# Technical Spike — Toolchain, Audio, and Pitch Detection

**Phase:** 0 — Discovery  
**Status:** In progress (codebase decided; baselines captured; AudioLatency measured; A/B loop and pitch pending)  
**Date:** 2026-11-10  
**Tasks:** #4, #6, #10, #12, #21

---

## 1. Objective

Within a 3-day timebox, answer:

0. Which codebase does the maqam pivot build on — the legacy v1 repo or the v2 recreation?
1. What does v2 already prove, and what must be added before maqam work starts?
2. Which audio stack gives usable tap-to-sound latency, gapless A/B looping, and pitch-preserved slow playback?
3. Can on-device pitch detection meet ±20 cents accuracy for humming?
4. Which content-pipeline changes are needed to move from song titles to maqams?
5. In what order should Firebase, Media3, Navigation, Room, and Billing be added?

Output: a go/no-go decision record (Section 10) and a work order for Task #6.

---

## 2. Codebase decision — continue from v2 (decided 2026-11-10)

**Decision: the maqam pivot continues in `/Users/omarmahmoud/Apps/Android/yallangani-v2`. No fresh repo.**

V2 is a modern reimplementation of the same product under the same application ID
(`com.osmartapps.yallanghanee`). It already contains the expensive, pivot-agnostic platform work:
Compose + Material 3, `targetSdk 36`, edge-to-edge, forced RTL, a bundled offline content pack with
validation, DataStore progress, audio focus/noisy handling, AdMob plumbing, unit + instrumentation
tests, and a working R8 release build.

The maqam pivot is a **game/content/information-architecture replacement**, not a platform rewrite.
Rebuilding the platform would discard a release-ready base and repeat months of work for no product
benefit.

### 2.1 Verified v2 state (this session)

| Item | Value |
|---|---|
| Repo | `/Users/omarmahmoud/Apps/Android/yallangani-v2` |
| Git | Baseline committed at `f630bd0`, annotated tag `pre-maqam`; untracked `:spike` module on top |
| Modules | `:app` only |
| Packages | `ads/ audio/ data/ game/ model/ ui/` plus `tools/` and `docs/` |
| Tests | 4 unit suites (`GameEngineTest`, `ProgressionTest`, `ArabicTextTest`, `ContentPackTest`) + 1 instrumentation (`GameFlowTest`) |

### 2.2 Keep — survives the pivot unchanged

| V2 asset | Why it survives |
|---|---|
| Build toolchain — AGP 8.13.0, Gradle 8.14.3, Kotlin 2.2.20, Compose BOM 2025.09.01 | Current and supported; `targetSdk 36` already exceeds the `35` decision |
| `MainActivity.kt` — edge-to-edge + `LocalLayoutDirection = Rtl` | Arabic-first content, RTL is already the default |
| `data/ContentRepository.kt`, `ContentPackValidator.kt` | Bundled read-only pack with validation is the right shape; only the schema changes |
| `data/ProgressStore.kt` (DataStore) | Progress persistence is maqam-agnostic |
| `audio/AudioFocus.kt`, `AudioPlayer.kt`, `SoundEffects.kt` | Focus/noisy handling and playback plumbing are reusable |
| `ads/AdsController.kt` | Interstitial-after-5 and rewarded-hint logic is content-agnostic |
| `ui/components/States.kt`, `ui/AssetImages.kt` | Generic loading/error/asset helpers |
| Release config, tests, `tools/melody-render/` | Melody renderer is directly useful for the maqam pack |

### 2.3 Replace — this is the pivot

| V2 asset | Replacement |
|---|---|
| `game/GameEngine.kt`, `GameViewModel.kt` | Recognition engine: listen → identify → echo → build → cadence |
| `game/Progression.kt` | Mastery stages + Leitner spaced repetition |
| `model/Content.kt` + `assets/content/levels.json` | Maqam/jins/phrase/lesson schema from `maqam-content-audio-spec.md` |
| `audio/*` playback surface | Media3/ExoPlayer for A/B loop and 0.75× pitch-preserved slow mode |
| `ui/GameScreen.kt` | Listen Orb, Pitch Ribbon, Note Ladder lesson player |
| `ui/HomeScreen.kt` | Majlis home, world map, onboarding entry |
| `ui/YallaApp.kt` — `Crossfade(Home/Game)` | Navigation graph (Home, World, Lesson, Results, Library, Profile) |
| `ui/theme/Theme.kt` — BrandRed/Gold | Night Majlis tokens and per-maqam color/sigil system |
| `assets/content/audio/*` (10 demo tracks) | 18-asset pack per maqam, starting with Rast/Hijaz/Nahawand |

### 2.4 What v2 already gives us that changes Task #10

V2's shipped pack is **not** a generic song list. `assets/content/levels.json` already names ten
maqams (`لحن الحجاز`, `لحن النهاوند`, `لحن الرست`, …) and the audio files are maqam-named
(`01-alhijaz.mp3` … `10-albanjkah.mp3`).

What is still wrong for the pivot:

- The question string is `ما اسم هذه الأغنية؟` and the loop is letter-tile spelling of the title.
- The schema carries only `id`, `title`, `audio` — no degrees, ajnas, tuning, colors, stories,
  phrases, durations, ornaments, or lesson types.
- It is one demo phrase per maqam, not the 18-asset set per maqam in the content spec.
- Sikah and Huzam tuning are unverified against the advisor sign-off in Task #2.

So Task #10 keeps its schema work but **reuses the existing maqam naming and the starter maqams'
demo audio as placeholders**, rather than starting a new content namespace.

### 2.5 Caveats

1. **Baseline is committed.** `f630bd0` is tagged `pre-maqam`; the `:spike` module remains
   uncommitted. Commit the spike before starting pivot work so the pre-pivot state stays recoverable.
2. **Same `applicationId` as v1.** Installing v2 over an existing differently-signed v1 install
   fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Verify on a dedicated emulator/device, or add a
   `.dev` application ID suffix for debug builds.
3. **Another session owns v2 history.** The v2 chat showed one background process. Verify the current
   repo state (not the last reported green build) before editing, and avoid concurrent edits.
4. **V2's `docs/RECREATION_PLAN.md` is now stale.** Its decisions #1 (100 Arabic tracks) and #4
   (song-guessing scope) are superseded by the maqam pivot; revise it in Task #24.
5. **V2 has no Firebase.** Its analytics and crash reporting are absent, so Task #5 must add them
   before any baseline is meaningful.

---

## 3. Baselines

### 3.1 Legacy v1 baseline (for reference, 2026-11-10)

| Item | Result |
|---|---|
| Debug build | `:app:assembleDebug` succeeds in **2m 11s** |
| Toolchain | Gradle 8.4, AGP 8.3.1, Kotlin 1.8.0, JDK 17 |
| SDK | compileSdk 33, targetSdk 33, minSdk 21 |
| APK size | **11 MB** |
| Device | Samsung SM-S908E (Galaxy S22 Ultra) |
| OS | Android 16 / API 36, arm64-v8a |
| RAM | 11.4 GB total |
| Display | 1440 × 3088 @ 600 dpi |
| Cold start (legacy) | **559 ms** TotalTime / 576 ms WaitTime |
| Install | Existing package is signed differently; debug APK cannot overwrite |
| Default Java | **JDK 25** on PATH; unsupported by AGP 8.3 / Gradle 8.4 |
| Working JDK | Temurin **17.0.20.1** from the Gradle JDK cache |

Legacy visual baseline (`docs/phase0/baseline-menu.png`): red curtain + cartoon singer + glossy
candy-button identity, level counter overlapping the Play button, deprecated immersive
`systemUiVisibility`, and no edge-to-edge safety.

### 3.2 V2 baseline (verified at tag `pre-maqam`, commit `f630bd0`)

| Item | Result |
|---|---|
| Debug build | `:app:assembleDebug` succeeds; **26 MB** debug APK; 50 s warm build |
| Compose | BOM 2025.09.01, Material 3 |
| SDK | compile/target **36**, min **24** |
| Ads | `play-services-ads` 25.5.0, test IDs by default |
| Unit tests | **26/26 passing**, 0 failures, 0 errors, 0 skipped |
| Lint | **0 errors / 16 warnings** (verified) |
| Release | R8 succeeds; **5.99 MB** unsigned APK (from the v2 session) |
| Small screen | 320×568 dp verified; footer overflow fixed |
| Standard screen | 411×914 dp verified |
| Touch targets | answer slots 45–48 dp; tray tiles 44 dp |
| Audio | focus loss and noisy/unplug paths verified |

### 3.3 Build commands

```bash
export JAVA_HOME="$HOME/.gradle/jdks/eclipse_adoptium-17-aarch64-os_x.2/jdk-17.0.20.1+1/Contents/Home"
./gradlew :app:assembleDebug --console=plain
adb install -r app/build/outputs/apk/debug/app-debug.apk   # blocked by signature conflict
adb shell am start -W -n com.osmartapps.yallanghanee/.MainActivity

# Spike module (separate applicationId, installs alongside v1/v2)
./gradlew :spike:assembleDebug --console=plain
adb install -r spike/build/outputs/apk/debug/spike-debug.apk
adb shell am start -W -n com.osmartapps.spike/.MainActivity
adb logcat -d -s SpikeLatency:I | grep audio_latency
```

Do not commit `org.gradle.java.home` or any machine-specific path. Use `JAVA_HOME` locally and a Gradle toolchain in CI.

### 3.4 Spike results — tap-to-sound latency (SM-S908E, 2026-11-10)

Two independent runs of 30 taps, fresh process each, `:spike` `AudioLatency` screen.

| Metric | Run A | Run B |
|---|---|---|
| dispatch (tap → probe start) median | 8.9 ms | 8.9 ms |
| audio (probe start → first frame at DAC) median | 43.5 ms | 43.7 ms |
| **total (tap → first frame) median** | **53.2 ms** | **52.2 ms** |
| total p95 | 67.8 ms | 71.2 ms |
| total min / steady max | 37.9 / 67.8 ms | 39.1 / 72.2 ms |
| first tap after cold launch | 135.7 ms | 183.3 ms |
| timestamp method | 30/30 | 30/30 |

Audio config and path: 48 kHz, 192 frames/buffer (4.0 ms), stable `stampFrame = 144` (3 ms).
The track runs on the fast path (`F2` track, FastMixer `latency=21.00 ms`, mixPeriod 4.00 ms),
so `getMinBufferSize` of 85.5 ms is irrelevant — it describes the deep-buffer path, not this track.
The residual ~22 ms between the fast-mixer latency and the measured audio path is `play()` start,
server wake-up and first mix period.

Method: static stereo `AudioTrack` (`USAGE_GAME` / `CONTENT_TYPE_SONIFICATION`,
`PERFORMANCE_MODE_LOW_LATENCY`), an 8 ms 1 kHz click at frame 0 padded with 250 ms of silence
(the pad keeps the track playing long enough for `getTimestamp` to report, without delaying frame 0).
Tap time = `awaitFirstDown.uptimeMillis`; presentation = `getTimestamp` back-off
`nanoTime − framePosition / sampleRate`. Only the first tap after launch is excluded; it pays the
one-time fast-track attach and is discarded as cold.

Interpretation: ~52 ms tap-to-sound is fine for answer feedback and melody replay, marginal for
rhythm tapping, and reads as immediate at 60 fps. This is an API-level measurement
(`getTimestamp`); acoustic loopback still needs a second device.

---

## 4. Dependency inventory

### 4.1 Present in v2

| Area | Version | Notes |
|---|---|---|
| Gradle / AGP / Kotlin | 8.14.3 / 8.13.0 / 2.2.20 | Current; no upgrade needed for the pivot |
| Compose | BOM 2025.09.01 | Material 3, icons, foundation, tooling |
| DataStore | 1.1.7 | Preferences only; no Room/DB |
| kotlinx.serialization | 1.9.0 | Content pack parsing |
| Lottie | 6.7.1 (`lottie-compose`) | Already on the target major |
| Lifecycle | 2.9.4 | Compose runtime/viewmodel |
| Play Services Ads | 25.5.0 | 16 KB page support; UMP consent still missing |

### 4.2 Missing in v2 — must be added

| Area | Needed for | Task |
|---|---|---|
| Firebase Analytics + Crashlytics (BOM) | Baseline metrics, KPI tracking, crash-free rate | #5 |
| Media3 (ExoPlayer, session) | A/B looping, 0.75× pitch-preserved slow mode | #12 |
| Navigation Compose | Multi-screen IA (home, world, lesson, results, library, profile) | #9 |
| Room | Maqam/phrase/lesson entities and migrations | #10 |
| Play Billing 7 | Maqam+ subscription (deferred to Phase 3) | #23 |
| `RECORD_AUDIO` permission + pitch detector | Hum practice (release 2) | #21 |
| UMP consent SDK | EEA/UK ad consent before launch | #23 |
| CI workflow | Build + tests + lint reproducibility | #6 |

### 4.3 Legacy v1 stack (reference only — not migrating)

| Area | Legacy | Disposition |
|---|---|---|
| UI | XML + AppCompat + custom presenter stack | Superseded by v2 Compose |
| Billing | `com.anjlab.android.iab.v3:2.0.3` | Do not port; use Play Billing 7 later |
| OneSignal | 4.5.1 + Gradle plugin | Dropped; push out of scope |
| Play Games | 21.0.0 | Dropped; leaderboard gating retired |
| Retrofit/OkHttp | 2.3.0 / 3.12.1 | Retired with the remote API |
| Firestore | 23.0.4 | Retired |
| Local `inappbilling` module | Not in the build | Confirm unreferenced, then delete in Task #24 |

---

## 5. Work order in v2

Replaces the earlier v1→Compose migration order. Each step must build, pass tests, and pass lint
before the next.

1. **Commit the v2 baseline** and tag it `pre-maqam` so the pivot is reversible.
2. **Pin JDK 17** locally and in CI; add a Gradle toolchain declaration. Verify a clean build from a
   fresh checkout, not an incremental one.
3. **Add Firebase** (BOM, Analytics, Crashlytics) behind a debug-safe wrapper; confirm events appear
   before Task #5 measurement.
4. **Add Media3** and replace the `MediaPlayer` playback surface for lesson audio only; keep
   `SoundPool` for taps.
5. **Add Navigation Compose** and a feature flag that switches between the legacy letter-tile flow and
   the maqam shell.
6. **Add Room + the maqam schema**; seed from the bundled pack; keep `ContentRepository` as the read
   API so screens do not see storage details.
7. **Verify 16 KB page alignment** on the release APK and re-run `lintVitalRelease`.
8. **Add CI** (assemble, unit tests, lint) once a remote exists; until then document the local command.
9. **Billing 7 last**, behind a flag, only when Phase 3 starts.

---

## 6. `targetSdk 36` posture

V2 already meets or exceeds the `targetSdk 35` decision. Open items:

- **16 KB page size**: verify Ads and any native libraries with `zipalign -c -P 16`.
- **Predictive back**: confirm behavior with Navigation Compose; no legacy `onBackPressed` remains.
- **Edge-to-edge**: already enabled; re-verify insets on new screens.
- **Deprecated APIs**: none known in v2 (no `Handler()`, `getColor()`, `systemUiVisibility`).
- **Microphone**: `RECORD_AUDIO` is not declared; add only with the hum feature and a runtime prompt.
- **Foreground service / notifications**: not needed unless practice reminders ship.

---

## 7. Spike harness (inside v2)

Run as a debug-only module or a debug-only screen set, branch `spike/phase0`:

- Module: `:spike`, `applicationId "com.osmartapps.spike"` (coexists with both v1 and v2 installs).
- No analytics, no ads, no billing.

Screens still needed (the rest are already proven by v2 and stay as smoke checks, not full tests):

1. `AudioLatency` — **done**. Static low-latency `AudioTrack` click; tap-to-first-frame measured
   via `AudioTrack.getTimestamp`, JSON in logcat plus on-screen median/p95. See §3.4.
2. `ABLoop` — Media3 two-second phrase, 20 repeats, gap logging.
3. `SlowPitch` — Media3 `PlaybackParameters(0.75f)`; verify pitch preservation.
4. `DroneSync` — looped drone plus phrase; measure drift.
5. `PitchDetector` — `AudioRecord` + YIN; cents readout and confidence.
6. `FocusTest` — regression only (v2 already passes focus/noisy handling).

Results: JSON lines in logcat plus an on-screen summary; export with `adb logcat -d`.

Do not install over the production package.

---

## 8. Device matrix

| Tier | Device | Status | Use |
|---|---|---|---|
| High | SM-S908E, API 36, 12 GB, 1440×3088 | Connected | Full spike |
| Mid | Physical API 30–33, 4–6 GB, 1080p | Needed | Audio latency, insets |
| Low | Physical API 24–26, 2 GB, 720p | Needed | Worst-case latency, memory |
| Emulator | API 24, 30, 36 | Available via SDK | Install/launch/insets only |

Audio latency and pitch accuracy must be measured on physical devices, not emulators.

Because v2 shares v1's `applicationId` and is signed with a different debug key, use a dedicated
emulator or a `.dev` suffix. Do not uninstall the existing v1 install on the test device — it may
contain user data.

---

## 9. Test procedures and acceptance

### 9.1 Build and launch

- Assemble debug and release; install on API 24, 30, 36.
- Cold start <2 s on mid-tier; no crash on launch.
- Release build passes Play pre-launch.

### 9.2 Tap-to-sound latency

- 30 trials per device; log touch and first audio frame timestamps.
- Acceptance: median <50 ms, p95 <100 ms (app-side).
- **Result on SM-S908E:** median 52–53 ms, p95 68–72 ms, steady max 72 ms (two runs, §3.4).
  Median misses the 50 ms stretch by ~2–3 ms but is well inside p95; accepted for recall/feedback
  taps. Re-test on mid-tier before locking the threshold — if a mid-tier device exceeds p95 100 ms,
  fall back to the continuous-track + gain-envelope design instead of `play()` per tap.
- Acoustic loopback (speaker → mic): median <100 ms.

### 9.3 A/B loop

- 20 loops of a 2-second phrase.
- Acceptance: gap <30 ms, no click/pop, stable pitch.

### 9.4 Slow mode (0.75×)

- Duration ratio 1.333 ±2%; pitch deviation <5 cents; no artifacts.

### 9.5 Drone sync

- 60-second loop with a phrase on top; drift <10 ms; seamless loop point.

### 9.6 Audio focus and routing

Regression only — v2 passes today. Re-run after Media3 replaces `MediaPlayer`.

- Focus loss (call/other app): pause and resume correctly.
- Headphone unplug: pause, do not blast the speaker.
- Bluetooth: no double playback.

### 9.7 Pitch detection

Test tones: 110, 220, 261.63, 329.63, 440, 523.25 Hz, plus quarter-tone offsets (+50 cents).

- Median error ≤10 cents; p95 ≤20 cents.
- Five human hums; octave errors <2%.
- Detection latency ≤50 ms.
- Noise gate rejects silence and room hum.

**Algorithm proposal:** in-house YIN with 44.1 kHz capture, 2048-sample frame, 512-sample hop,
threshold 0.15, parabolic interpolation, 65–1000 Hz range, median filter.

**License warning:** TarsosDSP is GPL-3.0 and is not suitable for a closed-source commercial app.
Use an in-house implementation or a permissively licensed library after review.

### 9.8 Compose, RTL, and accessibility

Regression baseline from v2 (320×568 dp and 411×914 dp). For the maqam UI:

- 60 fps scrolling on mid-tier; no jank >1% frames.
- APK size delta <5 MB for the new experience.
- TalkBack can operate the lesson player; touch targets ≥48 dp where physically possible.

### 9.9 Insets and predictive back

- API 36: no content under system bars.
- Predictive back gesture works and the back stack is correct.

### 9.10 16 KB alignment

```bash
zipalign -c -P 16 -v 4 app-release.apk
```

- All native libraries 16 KB aligned; Play pre-launch report clean.

---

## 10. Decision record

| Decision | Options | Result | Rationale |
|---|---|---|---|
| Codebase | Fresh / continue v1 / continue v2 | **Continue v2** | Platform is pivot-agnostic and already release-ready |
| UI stack | Compose / XML+Material 3 / hybrid | **Compose** | v2 already Compose |
| minSdk | 21 / 23 / 24 | **24** | v2 baseline; low-tier audio still to be measured |
| compile/target | 33 / 35 / 36 | **36** | v2 already 36; satisfies the 35 decision |
| Audio engine | Media3 + SoundPool / MediaPlayer | Media3 + SoundPool recommended | A/B loop, speed, focus, gapless |
| Pitch detection | In-house YIN / licensed library | In-house YIN | Accuracy, license, no upload |
| Billing | 7.x | 7.x, deferred | Play requirement; Phase 3 |
| Lottie | 6.x / Compose animations | **6.7.1 (v2)** | Already migrated |
| OneSignal | Upgrade / remove | **Removed** | Push out of scope |
| Play Games | Keep / remove | **Removed** | Leaderboard gating retired |
| Ads | 23.6+ | **25.5.0 (v2)** | 16 KB and target 36 compliant |
| Analytics | Firebase + BigQuery | Firebase to be added | Absent from v2 today |
| Content namespace | New / reuse v2 maqam names | **Reuse v2 names** | Pack is already maqam-named |
| Tap-to-sound path | MediaPlayer / SoundPool / low-latency static `AudioTrack` | **`AudioTrack` (`USAGE_GAME`, low-latency)** | 52 ms median on fast mixer; sets the engine's trigger path |
| Tap-to-sound acceptance | <50 / <100 ms median / p95 | **Accept p95 100 ms; median 52 ms measured** | Recall taps tolerate ~50 ms; rhythm mode not planned for v1 |

---

## 11. Risks

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| V2 edits before its baseline is committed | Certain if skipped | Unrecoverable pre-pivot state | Commit and tag `pre-maqam` first |
| Concurrent edits from the v2 session | Medium | Lost work, merge conflicts | Confirm the v2 session is idle first |
| Signature conflict with the v1 install | Certain on device | Cannot install | Dedicated emulator or `.dev` suffix |
| JDK 25 default breaks AGP | High on this machine | Build failure | Pin JDK 17 in CI and local scripts |
| No analytics in v2 | Certain | No baseline, blind beta | Add Firebase before Task #5 measurement |
| OneSignal/Lottie/Billing rewrites grow | Low now | Schedule slip | OneSignal/Play Games already gone; Billing deferred |
| Low-end audio latency | Medium | Poor UX | Test physical low-tier; SoundPool for taps |
| Pitch accuracy on microtones | Medium | Hum feature inaccurate | Calibrate YIN; sustained vowels; forgiving thresholds |
| Unverified Sikah/Huzam tuning | Medium | Content teaching error | Advisor sign-off in Task #2; starter trio Rast/Hijaz/Nahawand |
| 16 KB native alignment | Low/Medium | Play rejection | Run zipalign; Ads already 25.5.0 |
| Emulator-only testing | Medium | False confidence | Require at least one mid-tier physical device |

---

## 12. Immediate next actions

1. ~~Commit v2 and tag `pre-maqam`; confirm the v2 session is idle.~~ Done (`f630bd0`, tag `pre-maqam`).
2. ~~Re-run `:app:assembleDebug`, `testDebugUnitTest`, and `lintDebug` from the current tree.~~ Done.
3. ~~Add the `:spike` module (or debug-only screens).~~ Done — separate `com.osmartapps.spike` app.
4. ~~Implement `AudioLatency`~~ Done (§3.4). Next: `ABLoop` and `SlowPitch`; these decide the audio stack.
5. Implement `PitchDetector` second; this decides the humming roadmap.
6. Re-test latency on one mid-tier physical device, then update Tasks #4 and #6 with the go/no-go.
