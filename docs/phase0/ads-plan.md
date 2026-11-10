# Ads: placements, consent, and real AdMob units

Status: implemented and device-verified 2026-11-10 with Google sample IDs; the release build was
verified with the owner's real IDs stored outside the repo. Remaining external setup: AdMob console
consent messages, consent-form testing, and deploying the updated privacy pages. Superseded
statements in `AGENTS.md`, both privacy pages, the UI/UX audit, and `docs/RECREATION_PLAN.md` were
updated in the same change.

## Decision

Ads return to the app, but stay out of active learning:

- **App open** on cold start, once per day, never during onboarding.
- **Banner** anchored at the bottom of Home only.
- **Interstitial** after the Results screen is dismissed, on every third completed lesson.
- **Rewarded** ad grants one **50/50 hint** per lesson, offered only on questions with three or
  more options (a two-option question has no wrong answer to remove after one elimination it would
  become a forced answer, so the hint is not offered there).
- **Rewarded interstitial** unit is registered in config but unused; the reward path uses the
  rewarded format, which carries the reward callback.

The owner directed these placements on 2026-11-10. This supersedes the "no ads in the learning
loop" rule for Home, the Results exit, and cold start; lessons, feedback, playback, and onboarding
stay ad-free. `AGENTS.md` rule 9, both privacy pages, and the stale Phase 4 section of
`docs/RECREATION_PLAN.md` must be rewritten in the same change.

## AdMob units

App ID and unit IDs live in the AdMob console and are supplied per build from a local, untracked
`~/.gradle/gradle.properties`. The real values are never written into this repository.

| Format | Property | Used |
| --- | --- | --- |
| Banner | `admob.bannerUnitId` | Home bottom |
| Interstitial | `admob.interstitialUnitId` | Results exit, every 3rd lesson |
| Rewarded | `admob.rewardedUnitId` | 50/50 hint |
| App open | `admob.appOpenUnitId` | Cold start, 1/day |
| Rewarded interstitial | `admob.rewardedInterstitialUnitId` | Not shown (no reward signal) |

IDs are never committed. They go in `~/.gradle/gradle.properties` as `admob.appId`,
`admob.bannerUnitId`, `admob.interstitialUnitId`, `admob.rewardedUnitId`,
`admob.rewardedInterstitialUnitId`, `admob.appOpenUnitId`.

**Debug split.** The `.dev` package must never serve real ads. `debug` and the instrumentation
build type force Google's sample IDs for app ID and every unit; `release` reads the Gradle
properties and fails the build if any is missing.

## Consent (UMP)

- Use the User Messaging Platform API. It resolves through `play-services-ads`, but the app declares
  `com.google.android.ump:user-messaging-platform` explicitly (4.0.0) so the consent API is not
  transitive-only.
- Request consent info on every cold start; load and show any ad format only when
  `canRequestAds()` is true and no form is required.
- Settings gains a **Privacy options** row, shown only when `PrivacyOptionsRequirementStatus` is
  `REQUIRED`; it opens the consent form and re-evaluates ad loading afterwards.
- The AdMob console needs a GDPR message for EEA/UK and a US-states message before this can show
  anything; the plan assumes the owner creates both.
- Consent is stored by the SDK, not by DataStore.

## Placement rules

- App open: only when `onboardingComplete == true`, at most once per local calendar day, and not
  when the process is resumed from background (cold start only). Preload on first Home composition,
  show before Home content settles.
- Interstitial: after Results dismisses on Next, Retry, or Home. Never mid-lesson, never on the
  first completed lesson, never alongside the app-open ad. Frequency counter is persisted separately
  from progress so a reset does not immediately re-trigger an ad.
- Banner: Home only, inside the scrolling column's bottom inset so it never covers the last card;
  hidden while Home is loading, erroring, or during onboarding.
- Rewarded hint: a «تلميح / Hint» action on the lesson screen, visible only when the current
  question has three or more options and no hint has been used in this lesson. On reward, one wrong
  option is disabled; the answer is unchanged and the item still counts in Results.
- Playback stops before any full-screen format opens.
- Every format degrades silently: load failure, no fill, or no consent means no ad and the flow
  continues.

## Engine and state changes

- `RecognitionEngine` stays Android-free: `RecognitionSession` carries `hintUsed` and
  `eliminatedId`, with pure `hintAvailable()` and `eliminateWrongOption()`; `AdPolicy` (pure Kotlin)
  decides interstitial and app-open eligibility from persisted counters.
- `RecognitionViewModel` exposes `grantHint()` and `hintUnavailable()`; the lesson reads hint
  visibility from the engine session.
- `ProgressStore` gains `AdCounters` (`completedSinceAd`, `lastAppOpenEpochDay`) as a DataStore
  flow, independent of lesson completion and preserved by progress reset.
- `AdsController` grows banner, app-open, rewarded, and preload entry points plus consent-gated
  initialization; it stays out of `RecognitionEngine`. `YallaApplication.kt` was deleted because
  MobileAds now initializes only after consent.

## Files

- `app/build.gradle.kts`, `gradle/libs.versions.toml` (adds `user-messaging-platform`),
  `AndroidManifest.xml` (AdMob app ID stays; the custom `Application` entry is removed).
- `ads/AdsController.kt`, new `ads/AdPolicy.kt`, new `ads/ConsentManager.kt`, new
  `ui/components/AdBanner.kt`, `MainActivity.kt`.
- `game/RecognitionEngine.kt`, `game/RecognitionViewModel.kt`, `data/ProgressStore.kt`.
- `ui/YallaApp.kt`, `ui/HomeScreen.kt`, `ui/LessonScreen.kt`, `ui/SettingsScreen.kt`,
  `ui/ResultsScreen.kt` (no ad UI inside Results itself).
- `res/values/strings.xml`, `res/values-en/strings.xml`.
- `site/public/privacy.html`, `site/public/en/privacy.html` (ads active, consent, opt-out).
- `AGENTS.md`, `docs/RECREATION_PLAN.md` Phase 4, this file.
- Tests: `AdPolicyTest`, `RecognitionEngineTest`, `BundledMaqamPackTest` unaffected.

## Verification

- `./gradlew :app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`,
  `:app:connectedDebugAndroidTest`.
- AdPolicy unit tests: first lesson never shows interstitial, every third lesson does, reset does
  not re-trigger, app open once per day, onboarding blocks app open.
- Engine tests: hint eliminates exactly one wrong option, is unavailable on two-option questions,
  cannot be used twice, and does not change the correct answer.
- Device with test IDs: banner on Home, app-open on second cold start same day is suppressed,
  interstitial on the third completed lesson only after Results exits, rewarded hint shows ad then
  disables one wrong option, audio stops before full-screen ads, airplane mode shows no ads and no
  blocking dialogs.
- Both languages and 320 dp: banner does not clip Home content; hint and privacy-options strings
  exist in both files.
- Consent: with a test device in the EEA debug override, the form appears, declining leaves the app
  functional with no ad requests, Privacy options can reopen it.

## Implementation status (2026-11-10)

- Code, strings, build split, and policy tests are in place; `assembleDebug`, `testDebugUnitTest`,
  `lintDebug`, and `connectedDebugAndroidTest` (Galaxy S22) pass.
- `assembleRelease` passes with the real IDs supplied by the owner and stored only in
  `~/.gradle/gradle.properties`. The release BuildConfig carries the real units; the debug build
  still carries Google's sample units.
- Device pass with sample IDs (emulator + Galaxy S22): app-open ad on the first cold start of the
  day after onboarding and suppressed on the next cold start; Home banner only at the end of the
  Home scroll; no interstitial leaving Results after lessons 1 and 2; interstitial before lesson 4
  after the third completion; rewarded hint removes exactly one wrong option only after the reward
  callback, cannot be used again in that lesson, and stays inert if tapped early; offline cold start
  shows no ads, keeps lessons usable, and reports the hint as unavailable; Arabic and English at
  320 dp with the banner and hint controls intact.
- Consent: SDK requests consent on launch, waits for `canRequestAds`, and exposes Settings →
  Privacy options only when required. The AdMob console still needs the GDPR message, the US-states
  message, and a registered test device before the form can be exercised end to end.
- The updated privacy pages are written but not deployed; deployment needs the owner's go-ahead.

## Risks

- Banner on Home can reduce lesson starts; measure by watching Play vitals and retention, not
  first-party analytics (none by design).
- A rewarded hint weakens the ear-training signal; it is opt-in and limited to one per lesson.
- Real IDs in a debug `.dev` install would produce invalid traffic, hence the build-type split.
- AdMob policy requires the privacy policy to describe consent withdrawal; the pages must change
  before any real-ID release.
