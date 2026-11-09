# UI/UX Audit & Plan — تعلّم اللحن (Learn the Melody)

**Phase:** 0 — Discovery
**Status:** Approved 2026-11-10. P0 vertical slice implemented; P1/P2 items remain open.
**Date:** 2026-11-10
**Build audited:** `com.osmartapps.yallanghanee.dev`, commit `f630bd0` + uncommitted `:spike`
**Device:** Samsung SM-S908E (Galaxy S22 Ultra), 1440 × 3088 @ 600 dpi, Android 16 / API 36
**Companion docs:** `maqam-content-audio-spec.md`, `technical-spike.md`, `analytics-taxonomy.md`
**Roadmap tasks touched:** #7 (design system), #8 (music components), #9 (navigation shell),
#14 (onboarding), #15 (home/world), #16 (recognition lessons), #17 (feedback/scaffolds/results),
#20 (profile/settings), #24 (retire letter-tile layer), #25 (accessibility/beta/learning test)

### P0 implementation status (2026-11-10)

| Finding | Status | Where |
|---|---|---|
| F-01 brand/audience mismatch | Implemented | `ui/theme/Theme.kt`, bundled OFL fonts, Night Majlis home |
| F-02 spelling instead of recognition | Implemented | `game/RecognitionEngine.kt`, `game/RecognitionViewModel.kt`, `ui/LessonScreen.kt` |
| F-03 under-instrumented audio | Implemented | `audio/AudioPlayer.kt` (0.75× pitch-preserved, A/B sequence), lesson orb |
| F-04 non-teaching feedback | Implemented | `ui/LessonScreen.kt` feedback panel + results summary |
| F-05 ads inside the learning loop | Implemented | `ui/YallaApp.kt` no longer references `AdsController` |
| §9 component gallery | Implemented (debug builds only) | `ui/gallery/GalleryScreen.kt`, opened from the Home header when `BuildConfig.DEBUG` |
| §14.2 step 3 onboarding + first win | Implemented | `ui/OnboardingScreen.kt`, `onboarding` block in `maqams.json`, `data/ProgressStore.kt` |
| §11 accessibility gate | Verified 2026-11-10 | TalkBack traversal, 200% font scale, 320 dp width, measured contrast (see §11.5) |

Still open from this plan: the 18-phrase-per-maqam pack, profile/settings (#20), a full TalkBack
and 200% font-scale pass, and the 5-user usability test. The shipped lessons use the three existing
demo phrases and say so in the pack notice; the five onboarding drills and the first-win item are
generated CC0 clips documented in `docs/CONTENT_LICENSES.md`.

---

## 1. Scope and method

This document audits the **current v2 experience** and specifies the **UI/UX of the
recognition-first learning product** — here called **تعلّم اللحن** — that replaces the
letter-tile song game.

Method:

1. Read the shipped UI source (`ui/HomeScreen.kt`, `ui/GameScreen.kt`, `ui/YallaApp.kt`,
   `ui/components/States.kt`, `ui/theme/Theme.kt`).
2. Installed the `.dev` build on the connected S22 Ultra and captured the Home and Game screens
   (`docs/phase0/audit/v2-home-before.png`, `docs/phase0/audit/v2-game-before.png`).
3. Dumped the live accessibility/layout tree with `android layout --flat` to measure targets and
   labels.
4. Cross-checked the planned product against `maqam-content-audio-spec.md` (curriculum, stages,
   scaffolds), the technical spike (audio constraints), and the decision to keep the
   app shell but replace the game layer.
5. Computed WCAG contrast ratios for the current palette and the proposed palette (Appendix A).

**Out of scope for this document:** visual brand illustration, store listing assets, the paywall
design, and hum/studio UI. Those are later phases; this document defines the shell they plug into.

**Not available this session:** the 26-task backlog created in the earlier chat. It lives in that
chat's task list, which is not shared with this workspace. The task mapping in Section 14 is
addressed to that backlog by number; if the backlog cannot be recovered, use Section 14 as the
authoritative UI/UX work breakdown.

---

## 2. Executive verdict

The v2 shell is a competent **song-guessing game**. It is not yet an **ear-training product**.

What survives: the offline Compose shell, RTL-first rendering, edge-to-edge insets, audio focus
handling, large single-CTA interaction, deep-red stage atmosphere, and the loading/error primitives.

What must change: the core loop, the vocabulary, the audio controls, the feedback model, the
progress model, the navigation graph, the type system, and every learning surface. The audit found
**16 issues**, of which **five are P0** — they block the pivot, not just polish it.

The recommended MVP is deliberately small: **onboarding (5 micro-exercises + first win) → home →
3-maqam path → recognition-only lesson player with free scaffolds → teaching feedback → results**,
with **no ads anywhere in the learning loop**.

---

## 3. Current experience audit

### 3.1 Screen inventory

| Screen | Current behaviour | Verdict |
|---|---|---|
| Loading | Centred spinner + «جارٍ تحميل المحتوى…»; no timeout, no skeleton | Keep primitive, restyle |
| Error | «تعذّر تحميل المحتوى» + retry only | Keep, add context + secondary action |
| Home | Curtain + Lottie singer + logo + stage/solved counters + «ابدأ» + footer + reset | Replace layout, keep stage motif |
| Game | Back, stage pill, play orb, «اضغط للاستماع», question, letter slots, hint, tray | Replace with lesson player |
| Wrong state | Red board scale + «حاول مرة أخرى» below the hint; tray disabled briefly | Replace with teaching feedback |
| Win overlay | Full-screen scrim + Lottie + «أحسنت!» + title + «التالي» | Replace with inline feedback + results |
| Onboarding | 4-line modal paragraph; mentions the rewarded ad | Replace with interactive first-run |
| Reset dialog | Clear destructive confirmation | Keep, relocate to settings |

### 3.2 Findings

Severity: **P0** blocks the pivot · **P1** required for the first playable slice · **P2** required
before beta · **P3** polish.

#### F-01 — Brand and audience mismatch (P0)

**Evidence:** Home shows a cartoon singer in a spotlight, a candy-coloured wordmark, and a repeat
Lottie loop (`HomeScreen.kt:97-133`); the win overlay is confetti-style fireworks
(`GameScreen.kt:470-529`); the theme is built entirely from `BrandRed`/`BrandGold`
(`Theme.kt:13-30`). The Phase 0 decision is an adult/general ear-training product with a
**Night Majlis** tone.

**Required:** retire the cartoon hero and candy wordmark from learning surfaces. Keep the deep-red
curtain texture only as an atmospheric background, never as the primary identity. Introduce the
`نغمة | مقام` product line with a typographic wordmark. Calm the celebration from confetti to a
sigil bloom. Keep the listing name **يلا نغني**.

#### F-02 — The loop teaches spelling, not listening (P0)

**Evidence:** the question is data-driven from `levels.json` and still reads
«ما اسم هذه الأغنية؟»; the answer is a letter-tile tray (`GameScreen.kt:414-468`); content is
10 single-phrase tracks named `لحن …`. The planned product is recognition → echo → build →
cadence over 9 maqams with 18 assets each.

**Required:** remove the letter tray from the learning mode entirely. Recognition items are
**2–3 large answer cards**, not a 16-slot spelling board. The maqam schema and lesson items come
from `maqam-content-audio-spec.md`. This also removes a typographic problem: Arabic letters on the
current tray render in isolated forms, which is wrong for teaching Arabic orthography.

#### F-03 — Audio is under-instrumented (P0)

**Evidence:** one 76 dp orb toggles play/pause (`GameScreen.kt:267-314`). There is no position,
duration, replay count, slow mode, loop, or drone. `AudioPlayer.toggle()` has no error channel —
a missing/corrupt asset returns `false` silently (`AudioPlayer.kt:17-42`). No playback state is
surfaced beyond a local `isPlaying` boolean.

**Required:** a real **Listen Orb** with explicit states — idle, loading, playing, paused,
interrupted, error. One-tap replay is unlimited. Slow mode (0.75×, pitch-preserved) and a drone
toggle are available on every recognition item. The audio engine decision is Task 12 (Media3);
the UI contract is fixed here regardless of engine.

#### F-04 — Feedback does not teach (P0)

**Evidence:** a correct answer produces a full-screen Lottie and the title (`GameScreen.kt:470-529`).
A wrong answer produces «حاول مرة أخرى» (`GameScreen.kt:201-208`) and a timed reset; nothing
explains *why*. The content spec requires an `explanation_ar` on every item and an automatic
contrast-pair scaffold after two misses.

**Required:** an inline **feedback sheet** that always states the discriminating interval in
Arabic, plus an **A/B «اسمع الفرق»** playback of the two candidate phrases. Wrong answers reveal
the correct option *after submission* but never before. Learning events are recorded per item.
The full-screen win overlay is reserved for lesson completion.

#### F-05 — Monetization sits inside the learning loop (P0)

**Evidence:** the hint button is a rewarded-ad gate (`YallaApp.kt:86-102`), the interstitial fires
every 5 solved rounds (`YallaApp.kt:73-81`), and the onboarding copy advertises the ad
(`strings.xml` — `onboarding_message`). The Phase 0 decision states **no ads in the learning loop**
and a later Maqam+ subscription.

**Required:** remove all ad affordances from onboarding, lessons, feedback, and results. Hints
become a **free scaffold ladder** (Section 10) because a paywalled hint corrupts the learning
signal. Monetization only at pack boundaries, and only if the owner reconfirms it; the default MVP
ships with no ads in the learning path.

#### F-06 — Progress measures counting, not mastery (P1)

**Evidence:** Home shows «المرحلة 2 من 10» and «تم حل 1 من 10» (`HomeScreen.kt:117-126`); progress is
a single current index in DataStore. The plan requires mastery stages plus Leitner review.

**Required:** progress surfaces become **per-maqam mastery** (4 stages) and a **continue card**
with the next lesson. Home leads with what to do next, not a level counter. Results state what was
learned and when it will be reviewed.

#### F-07 — Navigation is a two-state crossfade (P1)

**Evidence:** `Screen { Home, Game }` with `Crossfade` (`YallaApp.kt:27-105`); only the enum is
saved across process death. No back stack, no deep link, no lesson restoration, no predictive back.

**Required:** a navigation graph with routes `onboarding → home → world → maqam/{id} →
lesson/{lessonId} → results/{lessonId} → library/{maqamId} → settings`. System back follows the
stack. Leaving mid-lesson confirms and restores the current item. Predictive back is enabled.

#### F-08 — Home hierarchy is inverted (P1)

**Evidence:** the hero consumes roughly 45% of the screen; the primary CTA always says «ابدأ» even
mid-progress; reset sits next to the informational footer (`HomeScreen.kt:150-193`); menu music
auto-starts on Home (`HomeScreen.kt:66-68, 284-304`).

**Required:** the first thing on Home is **«تابع الدرس»** with maqam sigil and stage. Destinations
are secondary. Reset moves into settings. No auto-playing music: an ear-training app must not
compete with its own lesson audio or surprise headphone users.

#### F-09 — Touch targets fail on narrow or long content (P1)

**Evidence:** answer-slot size clamps to **18 dp** (`GameScreen.kt:350`) and tray tiles to **32 dp**
(`GameScreen.kt:421-422`). On 411 dp the spike measured 45–48 dp slots and 44 dp tiles; at 320 dp
with a long word the same formula yields 18–32 dp — below the 48 dp minimum. The live tree confirms
slot visuals around 45 dp and tile targets around 44 dp on this device.

**Required:** no interactive target below 48 × 48 dp at any supported width. Recognition uses
full-width answer cards (≥ 64 dp high), which removes the problem by construction. Any remaining
slot/ribbon interaction must reflow rather than shrink.

#### F-10 — Typography and numerals are inconsistent (P1)

**Evidence:** the Kharabeesh face is applied to display/headline/title/label but **not** body
(`Theme.kt:33-54`), so the app mixes a playful display face with the system Arabic font. Counters
use Western digits inside RTL sentences (`home_stage`, `game_stage`), and the game uses «2 / 10»
while Home uses «2 من 10». The font license is explicitly unverified in
`docs/CONTENT_LICENSES.md:118-127`.

**Required:** a two-family system — a calligraphic display face for maqam names/wordmark and a
readable UI face for body and numerals (both OFL). Replace Kharabeesh unless its commercial
embedding license is proven. Use one number format everywhere («الدرس ٢ من ٥») with bidi isolation.
Full spec in Section 8.

#### F-11 — TalkBack and semantics gaps (P1)

**Evidence from the live accessibility tree:** empty slots announce only «خانة فارغة» with no
position; used/removable state is not announced; letter tiles announce «حرف X» without selected/used
state; the win overlay is a plain `Box` with no dialog semantics or focus request, so focus stays
behind it; correct/incorrect changes are not announced.

**Required:** every item exposes position, state, and action; playback state changes are announced
via a polite live region; feedback is announced assertively; results are a semantic list; the
lesson exit is a real dialog. Full spec in Section 11.

#### F-12 — Motion ignores accessibility settings (P2)

**Evidence:** an infinite pulse runs for the entire playback (`GameScreen.kt:268-277`), Lottie
loops on Home, and neither checks the system animation scale or a reduced-motion preference.

**Required:** motion is tied to audio state and stops when audio stops; reduced motion uses
crossfades only. No infinite pulsing decorative animation on a listening screen.

#### F-13 — Audio error and empty states are missing (P1)

**Evidence:** `AudioPlayer` swallows creation failures (`AudioPlayer.kt:32`); the UI has no error,
retry, or skip path; there is no event for a missing asset.

**Required:** an inline audio-error card («تعذّر تشغيل المقطع») with **إعادة المحاولة** and
**تخطّي**. A skipped item is not marked mastered and emits a telemetry event. A missing asset never
blocks the lesson.

#### F-14 — Onboarding is a text dialog (P1)

**Evidence:** `OnboardingDialog` is a single paragraph that explains letter tapping and the
rewarded ad (`HomeScreen.kt:253-281`). There is no first win, no listening, no demonstration.

**Required:** a full-screen, skippable, resumable first run of **5 micro-exercises** (high/low,
up/down, same/different, long/short, home note + drone) followed by the **Hijaz vs Nahawand first
win**. Target under 90 seconds, no ads, one instruction line per step.

#### F-15 — RTL details (P2)

**Evidence:** the back arrow correctly auto-mirrors, but the stage separator differs between
screens, and Latin digits inside Arabic sentences rely on implicit bidi. Long titles wrap into
multiple rows on narrow screens.

**Required:** one separator («من»), explicit bidi isolation for digits and any Latin text, and
layout tests at 320 dp with the display face. No truncation of maqam names.

#### F-16 — Reset prominence and completion state (P3)

**Evidence:** reset is a footer text button adjacent to informational copy
(`HomeScreen.kt:150-193`); completion offers «ابدأ من جديد» inside the win overlay.

**Required:** reset lives in settings, unchanged confirmation. Completion becomes a results screen
with next steps (review, next pack, library) rather than a restart.

### 3.3 What to keep

- The **curtain/stage atmosphere** — reinterpreted as a dark majlis wall with a warm spotlight,
  not a cartoon stage.
- **One dominant CTA per screen** and full-width primary buttons.
- **Offline-first messaging** («تعمل بدون إنترنت») — move to settings/about, not the Home footer.
- **RTL-first and edge-to-edge** plumbing (`MainActivity.kt`).
- **Audio focus and noisy/unplug handling** (`AudioPlayer.kt`, `AudioFocus.kt`).
- **Loading, error, and destructive-confirmation patterns**, restyled.
- The **listen orb** as the seed of the Listen Orb component.
- A **celebration moment** — but calm and educational.

---

## 4. Audience, jobs, and emotional arc

**Audience:** Arabic-speaking adults and curious general listeners, 16+, no music theory assumed.
Sessions are 3–10 minutes, often on a phone with headphones, frequently offline.

**Jobs to be done**

1. «أريد أن أسمع الفرق بين المقامات، لا أن أحفظ أسماءها.»
2. «أريد تمرينًا قصيرًا يشعرني أن أذني تحسّنت اليوم.»
3. «أريد أن أفهم لماذا كانت إجابتي صحيحة أو خاطئة.»
4. «أريد أن أعرف ماذا أسمع بعد ذلك، دون أن أضيع في قوائم.»

**Emotional arc of one lesson**

```
curiosity → focused listening → a small risk (answer) → relief/insight (feedback)
→ growing confidence → quiet pride (results) → pull to continue
```

The current app's arc is *guess → celebrate → repeat*. It produces excitement but no perceived
skill growth. The new arc must make the learner **feel the interval**, not the score.

---

## 5. UX principles for تعلّم اللحن

1. **Ear first, eye second.** Audio is the content; visuals label and support it. Never replace a
   listening task with a visual one.
2. **One decision per screen.** A single question, a small set of large answers, no timers.
3. **Re-listening is free.** Replay is instant and unlimited. Slow and drone are one tap away.
   The learner controls repetition; the app never counts it.
4. **Feedback teaches.** Every answer explains the discriminating interval. «خطأ» alone is a bug.
5. **Scaffolds, never answers.** Drone → home note → jins → contrast pair. The answer is revealed
   only after the learner commits.
6. **Calm, adult, nocturnal.** Warm dark surfaces, gold as attention, sigils for identity. No
   cartoon mascots, casino confetti, or ad interruptions.
7. **Thumb-first and RTL-native.** Primary controls in the lower half; 48 dp minimum; every layout
   built right-to-left first.
8. **Interruption-safe.** A call, headphone unplug, or background switch pauses cleanly and
   restores the exact item.
9. **Progress is mastery.** Show what the ear can now recognize and what will be reviewed — not a
   counter of levels consumed.
10. **Measure learning, not taps.** Instrument per-item accuracy, scaffold use, and first-win
    success; optimize for the learner's gain.

---

## 6. Information architecture and navigation

### 6.1 Route graph

```
Launch
 └─ Splash / content check
     ├─ First run ──► Onboarding (5 steps) ──► First win ──► Home
     └─ Returning ─────────────────────────────────────────► Home
                                                              │
   ┌──────────────────────────────────────────────────────────┤
   │                                                          │
   ▼                                                          ▼
 تعلّم (Home + path)                                     أنا (Profile)
   ├─ Continue card ──► Lesson ──► Feedback ──► Results        ├─ Daily goal
   ├─ Maqam path (3 starter nodes)                             ├─ Sound & haptics
   │    └─ Maqam sheet ──► Lesson                              ├─ Reduced motion
   ├─ Library (المقامات)                                       ├─ About / licenses
   │    └─ Maqam detail ──► Listen / Drone practice            └─ Reset progress
   └─ Settings (from Home top bar) ──► same Profile screen
```

**Recommended bottom navigation: three tabs** — `تعلّم`, `المقامات`, `أنا`. The world map lives
inside `تعلّم` so the MVP does not ship an empty fourth destination. Lessons are full-screen routes
above the tab bar.

### 6.2 Back behaviour

| From | System back |
|---|---|
| Lesson | Confirm if ≥ 1 item answered; otherwise leave silently. Progress per item is saved. |
| Results | To Home, not back into the lesson. |
| Maqam detail / Library | To its parent tab. |
| Home | Exit app (standard). |
| Onboarding | Step back; at step 1, skip to Home. |

Predictive back is enabled for all routes except an active lesson, where a confirmation is
required.

### 6.3 Deep links (later)

`yallanghani://lesson/{id}` and `yallanghani://maqam/{id}` for daily melody and share links.
Not in the MVP.

---

## 7. Screen specifications

Layout rules: 24 dp horizontal page margin (16 dp at < 360 dp), 16 dp vertical rhythm, safe-drawing
insets, portrait, 320–430 dp width, font scale up to 200%.

### S1 — First run / onboarding

**Goal:** produce one real listening win in under 90 seconds.

- Full-screen, no dialog. Progress dots at top (5), «تخطّي» text button.
- Each step: Listen Orb centred, one instruction line, two large answer chips.
- Steps: high/low → up/down → same/different → long/short → home note with drone.
- Step 5 introduces the drone and the home note visually with the Pitch Ribbon.
- Final: **first win** — «حجاز أم نهاوند؟» with a 2.5 s phrase, then a short explanation and
  «ابدأ رحلتك».
- No ads. No wall of text. Audio starts only on tap.
- State is persisted per step; relaunch resumes.

**Acceptance:** a first-time user answers the first win without help; skip is always available;
TalkBack reads each instruction and answer; no target below 48 dp.

### S2 — Home / Majlis

**Goal:** one obvious next action plus quiet orientation.

- Top bar: wordmark (small), settings icon.
- **Continue card** (primary): maqam sigil, «تابع درس حجاز», stage label («تعرّف»), 3-item
  progress, «أكمل». First run reads «ابدأ أول درس».
- Secondary row: **«خريطة المقامات»** (3 of 9 available) and **«المكتبة»**.
- Quiet footer only if needed: no reset, no ad copy. Offline badge in settings/about.
- If all starter maqams are mastered: card becomes «المجموعة التالية قريبًا» with a library link.
- No auto-playing music.

**States:** first run (single CTA), in progress, all mastered, content error.

### S3 — World map (المقامات)

**Goal:** show the three-maqam starter path and mastery without a maze.

- Vertical path (not a horizontal map) for 320 dp fit: Rast → Hijaz → Nahawand, with subtle
  connecting arc.
- Node = sigil + name + mastery ring (0–4) + state (locked/available/in progress/mastered).
- Locked nodes are tappable for a preview card; the CTA explains what unlocks them.
- Next packs (Bayati/Kurd/Saba, Ajam/Sikah/Huzam) shown as «قريبًا»; **Huzam is explicitly
  «قيد المراجعة»** until advisor sign-off.
- Tapping a node opens a **maqam sheet**: name, lower jins, signature interval in Arabic, two-line
  story, «ابدأ» / «تابع».

**Acceptance:** all three starter nodes reachable at 320 dp with one thumb; locked state is
explained, not just disabled; mastery is readable without color.

### S4 — Lesson player (تعلّم اللحن) — core screen

**Goal:** one listening decision with full control over repetition.

```
┌──────────────────────────────────────────────┐
│ ✕            ● ● ○ ○ ○            الدرس ٢ من ٥ │
│                                              │
│                 ( Listen Orb )               │
│              اسمع العبارة · إعادة             │
│                                              │
│            أي مقام تسمع؟                      │
│                                              │
│   ┌──────────────────────────────────────┐   │
│   │              حجاز                     │   │
│   ├──────────────────────────────────────┤   │
│   │             نهاوند                    │   │
│   └──────────────────────────────────────┘   │
│                                              │
│   (↻ إعادة)  (🐢 أبطأ)  (درون)               │
└──────────────────────────────────────────────┘
```

- **Header:** close (X), 5 progress dots, lesson position «الدرس ٢ من ٥». No maqam name visible
  during a maqam-identification item — it is the answer.
- **Listen Orb:** 112 dp (88 dp compact), 132 dp tap area. States: idle, loading, playing,
  paused, interrupted, error. Tap anywhere on the orb replays from the start. A slim
  progress ring shows position (no scrub in the MVP).
- **Prompt:** one line, 24 sp, never longer than two lines.
- **Answers:** 2–3 full-width cards, 64 dp min height, 12 dp gap, single tap submits. Correct
  answer position is shuffled per item, never always first. Selection is immediate; no confirm
  button for 2–3 options.
- **Controls:** replay, slow (0.75×), drone. After a miss, a fourth control appears:
  «اسمع الفرق». Slow and drone states are visible (filled icon + label), not hidden in a menu.
- **Scaffold strip:** after the first miss, the jins label appears; after the second, the Pitch
  Ribbon highlights the characteristic interval and the contrast pair auto-plays once. The answer
  is never revealed by a scaffold.
- **Exit:** X opens a confirm dialog if anything was answered: «إنهاء الدرس؟ سيُحفظ ما أجبت عنه.»
- **Audio error:** inline card replaces the orb caption with retry and skip. Skip does not mark
  mastery.
- **No ads. No timers. No score pressure.**

**Micro-interactions:** orb pulses only while audio plays (and only if motion is allowed); answer
cards press to 0.98 scale; haptic selection on tap; success/error haptics on feedback.

### S5 — Feedback (inline sheet)

**Goal:** explain the difference in under five seconds.

- Bottom sheet, 40% height max, scrim 40%. The orb and controls remain visible/usable above it.
- **Correct:** sigil bloom (calm), «أحسنت», one-line explanation from `explanation_ar`, primary
  «التالي».
- **Incorrect:** «ليس تمامًا», the correct option stated, the same explanation, an automatic
  **A/B «اسمع الفرق»** (1.5 s of each candidate), primary «التالي».
- No full-screen celebration per item. No auto-dismiss.
- Feedback text is announced assertively; focus moves to «التالي».

### S6 — Results

**Goal:** convert five items into perceived skill and a next step.

- Header: maqam sigil + «درس حجاز: التعرّف».
- Accuracy as a simple fraction and a 5-dot item strip (no percentage theatre).
- «ما تعلمته»: two lines tied to the actual misses (e.g. «المسافة الزائدة بين ٢ و٣ هي توقيع حجاز»).
- Next: «الدرس التالي» primary, «إلى الرئيسية» secondary. Streak/daily goal appears from Phase 4.
- Review scheduling is stated when SRS ships: «سنراجع حجاز غدًا».
- Calm animation only; no confetti.

### S7 — Library (المقامات)

- Cards for the 9 maqams; starter three unlocked, others preview.
- Card: sigil, name, colour accent, lower jins, signature interval, 2-line story, mastery ring.
- Detail: story, ascending/descending scale with Pitch Ribbon, three signature phrases with replay,
  one licensed listening suggestion, credit line.
- **Huzam** card: «قيد المراجعة النظرية» and no audio until approved.
- No locked card is a dead end: each explains what it is and when it unlocks.

### S8 — Profile / settings (أنا)

- Daily goal (5/10/15 minutes), sound effects, haptics, **reduced motion** (default: follow
  system), about/licenses (audio + fonts), version.
- **Reset progress** at the bottom, destructive confirm.
- No accounts, no sign-in, no analytics toggle in the MVP (analytics is aggregate and documented in
  `analytics-taxonomy.md`).

### S9 — Loading / error / empty

- Loading: content check only; under 300 ms typically. Warm wordmark, no spinner theatre.
- Content error: «تعذّر تحميل المحتوى» + retry + «إعادة تعيين المحتوى» if validation fails.
- Empty library/pack: explanation + next available action, never a blank screen.

---

## 8. Design system — Night Majlis

### 8.1 Colour tokens

Measured against the Night Majlis background. Full contrast table in Appendix A.

| Token | Value | Use | Contrast on `bg` |
|---|---|---|---|
| `bg` | `#120D0A` | App background | — |
| `surface` | `#1E1612` | Cards, sheets | — |
| `surfaceHigh` | `#2A1F19` | Raised cards, chips | — |
| `outline` | `#7A6353` | 1 dp borders, dividers | 3.44:1 |
| `outlineSoft` | `#4A3A30` | Decorative only | 1.78:1 (not for UI state) |
| `text` | `#F7EAD7` | Primary text | 16.3:1 |
| `textMuted` | `#C9B8A3` | Secondary text | 10.0:1 |
| `gold` | `#D9A94F` | Primary action, active state | 9.0:1 |
| `goldPressed` | `#B8862F` | Pressed action | large/graphic |
| `success` | `#7FB18A` | Correct | 7.9:1 |
| `error` | `#E07A6A` | Incorrect | 6.6:1 |
| `scrim` | `#000000` @ 40% | Feedback sheet | — |

**Per-maqam accents** (from the content spec) are used for sigils, rings, and fills — not body
text. All clear 3:1 as graphics; Saba `#8E6BAE` is 4.47:1 and must use a lightened text variant
`#B49AD4` (7.8:1) when text is needed. Never encode state with colour alone; pair it with a sigil,
icon, or label.

Do **not** reuse the legacy `BrandRed`/`BrandGold` tokens as the learning palette. The curtain
texture may stay as a low-contrast background image, but the red must not dominate the screen.

### 8.2 Typography

| Role | Family | Size / line | Weight |
|---|---|---|---|
| Wordmark / maqam name | Calligraphic Arabic display (OFL) | 34 / 44 | 700 |
| Screen title | Display | 26 / 36 | 700 |
| Section title | UI face | 20 / 30 | 600 |
| Body | UI face (OFL, e.g. IBM Plex Sans Arabic) | 16 / 26 | 400 |
| Button / chip | UI face | 16 / 24 | 600 |
| Label / caption | UI face | 14 / 22 | 500 |
| Micro | UI face | 12 / 18 | 500 |

- Arabic line height is 1.5–1.6×; never tighten.
- Numerals: one format everywhere, wrapped with bidi isolation. Recommended: Western digits with
  `BidiFormatter` for counters, or Arabic-Indic globally — decide once (Section 16, D2).
- **Retire Kharabeesh** unless its commercial embedding license is proven. Ship OFL fonts and
  document them in `docs/CONTENT_LICENSES.md`.

### 8.3 Spacing, shape, elevation

- Spacing scale: 4, 8, 12, 16, 24, 32, 48 dp.
- Radii: 12 dp (chips/inputs), 20 dp (cards), 28 dp (sheets), full (orb/avatars).
- Elevation is tonal (`surface` → `surfaceHigh`), not shadow-heavy. The stage is flat and warm.
- Page margin 24 dp; 16 dp below 360 dp.

### 8.4 Motion

| Token | Duration | Use |
|---|---|---|
| `micro` | 120 ms | press, chip selection |
| `transition` | 220 ms | sheet, route crossfade |
| `reveal` | 360 ms | sigil bloom, results |

- Motion communicates audio state only. No infinite decorative loops on listening screens.
- Reduced motion: 120 ms crossfades, no scale/pulse, no Lottie; honour the system animation scale.
- The Listen Orb pulses only while audio plays; it stops on pause/end/error.

### 8.5 Haptics and sound

- Selection haptic on answer tap; confirm on correct; reject on incorrect. No haptic on audio start.
- No background music anywhere. UI sounds are optional, low-volume, and disabled with sound
  effects. Never play a UI sound over lesson audio.

---

## 9. Music-interaction component inventory

Build these as a gallery screen so design and QA can review states (Task 8).

| Component | States / props | Notes |
|---|---|---|
| `ListenOrb` | idle, loading, playing, paused, interrupted, error; progress 0–1 | 112/88 dp; 132 dp target; replay from start |
| `ReplayButton` | enabled | Label «إعادة»; unlimited |
| `SlowToggle` | off/on, unavailable | 0.75× pitch-preserved; disabled if asset/engine lacks it |
| `DroneToggle` | off/on, loading | Tonic + fifth loop; ducks phrase audio? No — phrase plays over drone at fixed gain |
| `ABCompare` | idle, playingA, playingB | «اسمع الفرق»; 1.5 s each; used in feedback and scaffolds |
| `PitchRibbon` | static, playing (playhead), highlighted interval | Degree dots; labels hidden at < 360 dp |
| `NoteLadder` | planned (echo, R2) | Not in MVP; reserve layout space |
| `AnswerChip` | default, pressed, selected, correct, incorrect, disabled | ≥ 64 dp; full width |
| `ProgressDots` | current index, results per dot | 5 dots; current is a 24 dp pill |
| `FeedbackSheet` | correct, incorrect, scaffold | 40% height; focus to primary action |
| `MaqamSigil` | one per maqam, deterministic seed | Shape + colour; never colour alone |
| `MasteryRing` | 0–4 | Text fallback «٢ من ٤» |
| `MaqamCard` | locked, available, in progress, mastered, review | Library + map |
| `LessonCard` | first, continue, complete | Home |
| `AudioErrorCard` | retry, skip | Inline, never blocks |
| `ConfirmExitDialog` | — | Standard dialog semantics |
| `BottomNav` | 3 tabs, selected | 80 dp + inset; label always visible |

Component rules: no component owns playback; the lesson state holder exposes a single
`LessonAudioState` and all controls are stateless. This keeps previews and tests deterministic.

---

## 10. Feedback and scaffold model

```
first exposure      → drone + home note visible
first recognition   → replay + slow available; jins hidden; Pitch Ribbon hidden
miss #1             → jins label appears; prompt to replay
miss #2             → interval highlighted on Pitch Ribbon; contrast pair auto-plays once
after commit        → correct option + explanation + A/B «اسمع الفرق»
never               → auto-reveal before the learner answers
```

Scaffolds are **free and always available after a miss**; they are not hints that skip the work.
The rewarded-ad hint from v2 is removed. Each scaffold use is logged (Section 15) so the team can
see which scaffolds actually teach.

---

## 11. Accessibility, RTL, and text

### 11.1 Touch and layout

- 48 × 48 dp minimum for every interactive element; primary answer cards ≥ 64 dp.
- No content hidden behind scrolling on the core lesson screen; the lesson fits 320 × 568 dp at
  100% font scale. At 200%, the answer cards scroll while the orb and prompt stay pinned.
- One-handed reach: the orb, answers, and controls all live in the lower 60% of the screen.
- Portrait only for the MVP (matches the manifest); no rotation-specific layouts.

### 11.2 TalkBack semantics

| Element | Label / state |
|---|---|
| Listen Orb | «استمع إلى العبارة» + state «قيد التشغيل / متوقف / تعذّر التشغيل» + action «إعادة» |
| Answer chip | «حجاز، خيار ١ من ٣»; result state after commit |
| Progress dots | «الخطوة ٢ من ٥»; not individually focusable |
| Feedback | Live region assertive: «إجابة صحيحة. …» / «إجابة خاطئة. الإجابة حجاز. …» |
| Audio start/end | Live region polite, but skip if the user initiated the play |
| Mastery ring | «أتقنت درجتين من أربع» |
| Win/results | Real screen, not an overlay that leaves focus behind |

Focus order: header → prompt → orb → answers → controls. After feedback, focus «التالي». In RTL,
the visual order is right-to-left and the traversal follows it automatically.

### 11.3 RTL and bidi

- One separator across the app: «من» (`الدرس ٢ من ٥`).
- Wrap digits/Latin with `BidiFormatter` or use Arabic-Indic digits consistently.
- Test long maqam names («البنجكاه», «النهاوند») at 320 dp and 200% font scale.
- Back/close icons auto-mirror; do not hardcode a direction.
- No reliance on letterforms for identity; sigils are direction-neutral.

### 11.4 Colour, motion, and hearing

- Every state has a non-colour signal (sigil, icon, label, dot pattern).
- Reduced motion follows the system by default and is also exposed in settings.
- The app is auditory by nature; provide textual descriptions of each phrase in feedback and
  library detail so a deaf or hard-of-hearing user can still learn the vocabulary. Do not claim
  the listening skill is accessible without hearing; be explicit in the store listing.

### 11.5 Verification pass (2026-11-10)

Run on the SM-S908E with the debug build, TalkBack enabled for the traversal pass.

- **TalkBack traversal:** Home → «ابدأ أول درس» → lesson → answer → feedback → «التالي» → next
  item completed with keyboard/accessibility focus only (Enter to activate). The feedback panel is
  a polite live region, so the result and explanation are announced without stealing focus; answer
  cards announce «حجاز، خيار ١ من ٢»; progress announces «الدرس ١ من ٤» (lessons) and «الخطوة ١
  من ٥» (onboarding); close, slow, A/B, retry, and skip controls all have Arabic labels.
- **Deviations from §11.2:** the shipped orb label is «اسمع العبارة» (not «استمع إلى العبارة»),
  feedback uses a *polite* (not assertive) live region so it does not interrupt the current
  utterance, and progress dots announce the position on the dot row rather than individually.
- **200% font scale:** onboarding (including feedback and the primary button), Home, lesson, and
  results render without clipping or overlap at 384 dp width. Feedback auto-scrolls to the next
  action, so the CTA stays reachable.
- **320 dp width:** all screens render; the onboarding CTA previously fell below the fold after
  feedback, so `OnboardingScreen` now scrolls to the action when an answer is committed, matching
  `LessonScreen`.
- **Contrast (measured):** text pairs 8.31–16.28:1; primary button 8.95:1. Interactive boundaries:
  idle answer card 3.17:1, correct 5.54:1, incorrect 4.78:1, revealed 3.88:1. Decorative panel
  borders (`NightOutlineSoft`) are below 3:1 against `NightSurfaceHigh`, but those panels are not
  interactive; every interactive boundary passes.
- **Still manual:** spoken TalkBack output was not recorded; the semantic tree, labels, focus
  order, and activation path were verified. A native-speaker listening check of TTS pronunciation
  is recommended before release.

---

## 12. Copy deck (Arabic) and terminology

**Nomenclature**

- App/listing: **يلا نغني**
- Product line: **نغمة | مقام**
- Learning mode: **تعلّم اللحن**
- Lesson unit: **درس** (not «مرحلة»)
- Item: **تمرين** / **سؤال**
- Phrase: **عبارة**; tonic: **القرار**; jins: **جنس**; drone: **درون**; slow: **أبطأ**
- Mastery: **أتقنت** (not «تم الحل»)

**Core strings**

| Key | Arabic |
|---|---|
| `learn_prompt_maqam` | أي مقام تسمع؟ |
| `learn_prompt_phrase` | أي عبارة تنتهي على القرار؟ |
| `learn_listen` | اسمع العبارة |
| `learn_replay` | إعادة |
| `learn_slow` | أبطأ |
| `learn_drone` | درون |
| `learn_compare` | اسمع الفرق |
| `learn_correct` | أحسنت |
| `learn_incorrect` | ليس تمامًا |
| `learn_explanation_prefix` | الفرق في |
| `learn_next` | التالي |
| `learn_exit_title` | إنهاء الدرس؟ |
| `learn_exit_body` | سيُحفظ ما أجبت عنه. |
| `learn_audio_error` | تعذّر تشغيل المقطع |
| `learn_skip` | تخطّي |
| `learn_try_again` | إعادة المحاولة |
| `home_continue` | تابع درسك |
| `home_start_first` | ابدأ أول درس |
| `home_path` | خريطة المقامات |
| `home_library` | المكتبة |
| `results_learned` | ما تعلمته |
| `results_next` | الدرس التالي |
| `results_home` | إلى الرئيسية |
| `maqam_locked` | يُفتح بعد إتقان ما قبله |
| `maqam_review` | قيد المراجعة النظرية |
| `settings_reset` | إعادة التقدم |
| `settings_motion` | تقليل الحركة |
| `settings_about` | عن التطبيق والتراخيص |

**Tone:** short, warm, adult. No exclamation marks except the single success line. No «يا بطل»,
no gambling language, no ad copy. Every explanation names the interval or the note, not just
«صحيح»/«خطأ».

**Remove:** «ما اسم هذه الأغنية؟», «تم حل», «المرحلة», «اضغط للاستماع» (replace with «اسمع
العبارة»), and the onboarding sentence that mentions the ad.

---

## 13. States and edge cases

| Situation | Required behaviour |
|---|---|
| Airplane mode | Full lesson works; no banner; ads (if ever) no-op |
| Audio focus loss (call) | Pause, show «توقف الصوت مؤقتًا», keep item state, offer replay |
| Headphone unplug | Pause, keep state, no auto-resume |
| App backgrounded | Stop audio; on return, show replay; never auto-play |
| Process death in lesson | Restore route + item index; unanswered item restarts |
| Missing/corrupt asset | Inline error, retry, skip (not mastered), telemetry |
| Slow asset absent | Disable slow with label «غير متاح لهذه العبارة»; do not fake it |
| Drone loop gap | Crossfade loop; no click; verified on device |
| TBD maqam (Huzam) | Excluded from paths; library card «قيد المراجعة» |
| Locked maqam | Preview sheet; explains unlock rule |
| First run offline | Same as online; no network check |
| 320 dp / 200% font | No clipped text; answers scroll; orb pinned |
| RTL + Latin digits | Bidi-isolated; no reversed counters |
| TalkBack | All states announced; no focus behind sheets |
| Reduced motion | Crossfades only; no pulsing/Lottie |
| Ad load failure | Never delays or blocks an item (learning loop has no ads) |
| All items skipped | Results says «لم نكمل هذا الدرس» and offers retry |

---

## 14. MVP slice, delivery order, acceptance

### 14.1 MVP scope (first playable slice)

- Onboarding: 5 micro-exercises + first win.
- Home with continue card + two destinations.
- Map with Rast, Hijaz, Nahawand only.
- Lesson player: recognition items only (5 per maqam, 15 total).
- Feedback sheet with explanation + A/B.
- Results.
- Library: three maqam cards + detail.
- Settings stub (motion, sound, reset, about).
- No echo/hum, no studio, no daily melody, no paywall, no ads.

### 14.2 Delivery order

| Step | Deliverable | Tasks |
|---|---|---|
| 1 | Night Majlis tokens, two-font system, component gallery | #7, #8 |
| 2 | Navigation graph + Home + Map + Maqam sheet | #9, #15 |
| 3 | Onboarding + first win | #14 |
| 4 | Lesson state holder + Listen Orb + audio controls + error states | #12, #16 |
| 5 | Feedback, scaffolds, results | #17 |
| 6 | Library + settings | #20 |
| 7 | Accessibility/RTL gate + 5-user usability test | #25 |

Steps 1–5 landed in the 2026-11-10 P0 slice (the world map from step 2 is deferred; Home opens the
three lessons directly). Steps 6–7 remain open.

**Design-first gate:** every step lands as a preview/gallery screen before it is wired to real
content. No production lesson code until the owner approves the corresponding screen spec.

### 14.3 Acceptance criteria

- A first-time user completes onboarding and the first win without assistance in ≤ 90 seconds.
- Five test users can distinguish Hijaz from Nahawand after one session (content spec §13.6).
- Every interactive target is ≥ 48 dp at 320 dp and 200% font scale.
- TalkBack can complete one full lesson: hear prompt, play, answer, hear feedback, advance.
- No ads appear anywhere in onboarding, lesson, feedback, or results.
- Zero network calls or blocking spinners during a lesson.
- Reduced motion removes all pulsing/Lottie and keeps comprehension.
- A call or headphone unplug pauses cleanly; replay restores the item.
- The lesson screen fits 320 × 568 dp with the orb, prompt, answers, and controls visible.
- Contrast ≥ 4.5:1 for text and ≥ 3:1 for UI boundaries in both normal and selected states.

---

## 15. Measurement plan (UI/UX events)

Add to `analytics-taxonomy.md` (Task 5):

| Event | Key properties |
|---|---|
| `onboarding_step_view` / `onboarding_complete` | step, skipped, duration |
| `lesson_start` / `lesson_complete` / `lesson_exit` | maqam, lesson, items, accuracy, duration |
| `item_view` | item id, stage, difficulty |
| `audio_play` / `audio_replay` / `audio_error` | item id, replay count, engine state |
| `slow_toggle` / `drone_toggle` / `ab_compare` | item id, on/off |
| `answer_submit` | item id, option, correct, response time |
| `scaffold_shown` / `scaffold_used` | item id, scaffold type, miss count |
| `feedback_view` | item id, correct, explanation shown |
| `results_view` | accuracy, items, next action |

Guardrails: no audio recording, no identifiers, no per-user profiles in the MVP. The learning KPI
is **first-win success** and **Hijaz vs Nahawand accuracy after one session**, not session length.

---

## 16. Open decisions for the owner

| # | Decision | Recommendation |
|---|---|---|
| D1 | Bottom navigation: 3 or 4 tabs | **3**: `تعلّم` (home + path), `المقامات`, `أنا` |
| D2 | Numerals: Arabic-Indic (٢) or Western (2) | Pick one globally; recommended Arabic-Indic with a tested OFL font |
| D3 | Display font | OFL calligraphic face (Aref Ruqaa/Amiri); retire Kharabeesh unless licensed |
| D4 | Monetization boundary | **No ads in the learning loop**; decide pack unlock vs none for MVP |
| D5 | Celebration tone | Calm sigil bloom; no confetti |
| D6 | Maqam names in UI | Arabic first; English secondary only where space allows (advisor Q4) |
| D7 | Onboarding length | 5 micro-steps, < 90 s, skippable, resumable |
| D8 | Hints | Free scaffold ladder (recommended); not ad-gated, not paid |
| D9 | MVP content | Recognition only; echo/hum deferred to release 2 |
| D10 | Phase 0 docs location | Move to v2 (done in this change); keep v1 docs read-only |

---

## Appendix A — Measured evidence

### A.1 Current palette contrast (passes, but tonally wrong)

| Pair | Ratio |
|---|---|
| Gold `#E6A442` on deep red `#4B0200` | 7.46:1 |
| Cream `#FFF3E0` on deep red | 14.64:1 |
| Cream 70% on deep red | 7.49:1 |
| Error `#FF8A80` on deep red | 7.03:1 |
| Deep red on gold (primary button) | 7.46:1 |

Contrast is **not** the current problem; hierarchy and tone are. The legacy palette paints the
primary CTA, stage pill, hint, and accents the same gold, so nothing stands out.

### A.2 Proposed palette contrast

| Pair | Ratio |
|---|---|
| Text `#F7EAD7` on bg `#120D0A` | 16.28:1 |
| Muted `#C9B8A3` on bg | 9.99:1 |
| Gold `#D9A94F` on bg | 8.95:1 |
| Gold on surface `#1E1612` | 8.26:1 |
| Success `#7FB18A` on bg | 7.87:1 |
| Error `#E07A6A` on bg | 6.58:1 |
| Outline `#7A6353` on bg | 3.44:1 |

Per-maqam accents on bg: Rast 8.92, Bayati 9.43, Hijaz 5.94, Saba 4.47 (graphics only; text
variant `#B49AD4` 7.83), Kurd 6.54, Nahawand 6.33, Ajam 9.32, Sikah 7.51, Huzam 4.81.

### A.3 Measured targets and typography

- Device: 1440 × 3088 px @ 600 dpi = 384 × 823 dp.
- Answer slots: ~45 dp visual / ~48 dp touch on this device; formula permits 18 dp on narrow.
- Tray tiles: ~44 dp on this device; formula permits 32 dp at 320 dp.
- Hint button: ~60 dp high; back icon: 48 dp. Both pass.
- Stage pill text: ~20 dp cap height; home counters ~14–16 dp.
- Home CTA: 328 × 64 dp.
- The Android layout tree shows empty slots with only «خانة فارغة» and used tiles with only
  «حرف X» — no position or state.

### A.4 Evidence files

- `docs/phase0/audit/v2-home-before.png`
- `docs/phase0/audit/v2-game-before.png`
- `docs/phase0/baseline-menu.png` (legacy v1, for the brand comparison)

---

## Appendix B — Why the letter tray must not survive

1. **It teaches orthography, not listening.** The pivot's skill is interval recognition.
2. **Arabic letterforms are context-sensitive.** Isolated tiles contradict Arabic reading.
3. **It creates a narrow-screen failure by construction.** More letters → smaller targets.
4. **It hides the music.** The board occupies the visual centre; the audio is one unlabelled orb.
5. **It makes feedback impossible.** A wrong spelling has no explanatory interval to teach.
6. **It locks the content schema to titles.** The maqam schema is phrases, stages, and ajnas.

The tray, the title slots, the «ما اسم هذه الأغنية؟» prompt, the rewarded hint, and the
level-counting progress are retired together as one unit (Task 24).

---

## Appendix C — Review checklist for the owner

- [ ] Approve the 3-tab IA and the MVP slice (Section 6, 14.1).
- [ ] Approve Night Majlis tokens and the two-font direction (Section 8).
- [ ] Confirm no ads in the learning loop (Section 2, F-05, D4).
- [ ] Approve the free scaffold ladder and removal of the rewarded hint (Section 10, D8).
- [ ] Choose numerals (D2) and display font (D3).
- [ ] Confirm Huzam stays hidden until advisor sign-off (Section 7, S7).
- [ ] Confirm the screen specs are the build contract for Tasks #7–#9, #14–#17, #20, #24, #25.
