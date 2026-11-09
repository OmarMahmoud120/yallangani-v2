# Analytics and Baseline Spec

**Phase:** 0 — Discovery  
**Status:** Draft  
**Date:** 2026-11-10  
**Tasks:** #5, #19, #23, #25

---

## 1. Purpose

Define the event taxonomy, dashboards, privacy rules, and baseline plan for the pivot. The goal is to measure activation, learning, retention, monetization, and quality without collecting personal data or raw audio.

The current app has Firebase Analytics and Crashlytics, but all custom-event calls are commented out. The legacy baseline is therefore mostly install/crash data; custom instrumentation is required in Task #5.

---

## 2. Naming and limits

- `snake_case`, `object_action`: `lesson_started`, `answer_submitted`.
- Maximum 40 characters per event name.
- Maximum 25 parameters per event.
- String values ≤100 characters.
- No free text, no names, no email, no precise location, no microphone audio.
- Global parameters: `session_id`, `app_version`, `content_version`, `locale`, `device_tier`, `platform`.
- Domain parameters: `maqam_id`, `lesson_id`, `item_id`, `stage`, `mode`, `difficulty`.

---

## 3. Event taxonomy

### 3.1 App and session

| Event | Trigger | Parameters |
|---|---|---|
| `app_open` | App process start | `cold_start_ms`, `source` |
| `session_start` | Foreground session | `session_id` |
| `session_end` | Background session end | `session_id`, `duration_s` |
| `screen_view` | Screen becomes visible | `screen_name`, `mode` |
| `feature_flag_seen` | New/legacy experience selected | `flag`, `variant` |

### 3.2 Onboarding

| Event | Trigger | Parameters |
|---|---|---|
| `onboarding_start` | First onboarding item shown | `variant` |
| `goal_selected` | Goal chosen | `goal` (`learn_maqam`, `train_ear`, `sing`) |
| `first_item_started` | First listen item starts | `maqam_id`, `item_type` |
| `first_item_completed` | First answer submitted | `correct`, `latency_ms` |
| `onboarding_completed` | Last onboarding step | `duration_s`, `items_completed` |
| `mic_prompt_shown` | Mic primer shown | `context` |
| `mic_permission_result` | Permission result | `result` (`granted`, `denied`, `denied_permanent`) |

### 3.3 Learning loop

| Event | Trigger | Parameters |
|---|---|---|
| `lesson_started` | Lesson route opens | `lesson_id`, `maqam_id`, `stage`, `mode` |
| `item_started` | Item rendered/played | `item_id`, `type`, `difficulty`, `maqam_id` |
| `answer_submitted` | Answer confirmed | `item_id`, `type`, `correct`, `latency_ms`, `attempt`, `maqam_id` |
| `replay_used` | Replay pressed | `item_id`, `count`, `slow_mode` |
| `scaffold_opened` | Scaffold used | `scaffold_id`, `item_id`, `stage` |
| `feedback_shown` | Feedback displayed | `correct`, `explanation_id`, `auto_scaffold` |
| `lesson_completed` | Last item finished | `lesson_id`, `accuracy`, `duration_s`, `items`, `maqam_id` |
| `lesson_abandoned` | Exit before completion | `lesson_id`, `last_item_id`, `elapsed_s` |

### 3.4 Mastery and review

| Event | Trigger | Parameters |
|---|---|---|
| `mastery_stage_up` | Stage advances | `maqam_id`, `from_stage`, `to_stage` |
| `review_scheduled` | Item enters the queue | `item_id`, `due_in_days`, `box` |
| `review_completed` | Review answered | `item_id`, `correct`, `box` |
| `streak_updated` | Daily completion | `days`, `is_new` |
| `daily_completed` | Daily melody finished | `score`, `items` |
| `challenge_completed` | Weekly challenge finished | `challenge_id`, `score` |
| `share_sent` | Share sheet completed | `surface` (`maqam_card`, `challenge`, `studio`) |

### 3.5 Hum practice (release 2)

| Event | Trigger | Parameters |
|---|---|---|
| `hum_started` | Mic practice opens | `item_id`, `maqam_id` |
| `hum_attempt` | Scored attempt | `item_id`, `cents_error`, `confidence`, `octave_error` |
| `hum_completed` | Practice item ends | `item_id`, `attempts`, `best_cents_error` |
| `mic_denied` | Permission denied | `context`, `permanent` |

Never log raw audio, pitch traces, or recordings.

### 3.6 Studio (release 2)

| Event | Trigger | Parameters |
|---|---|---|
| `studio_opened` | Studio opens | `source` |
| `take_recorded` | Recording saved | `maqam_id`, `instrument`, `duration_s` |
| `take_saved` | Take persisted | `slot_count`, `is_free_slot` |
| `take_shared` | Export/share | `instrument`, `surface` |

### 3.7 Monetization

| Event | Trigger | Parameters |
|---|---|---|
| `paywall_viewed` | Paywall shown | `source`, `sku_group` |
| `purchase_started` | Billing flow starts | `sku`, `price_micros`, `currency` |
| `purchase_completed` | Purchase success | `sku`, `price_micros`, `currency` |
| `purchase_failed` | Purchase failure | `sku`, `error_code` |
| `restore_completed` | Restore finishes | `sku_count`, `success` |
| `ad_impression` | Ad shown | `format`, `placement`, `session_id` |
| `rewarded_completed` | Reward granted | `placement`, `reward_type` |

### 3.8 Settings and errors

| Event | Trigger | Parameters |
|---|---|---|
| `setting_changed` | Setting toggled | `key`, `value` |
| `audio_error` | Playback failure | `code`, `asset_id`, `engine` |
| `content_error` | Pack/schema failure | `pack_id`, `code` |
| `data_deleted` | User deletes data | `scope` |

Crashes and ANRs stay in Crashlytics; do not duplicate them as analytics events.

---

## 4. Baseline plan

### 4.1 Legacy instrumentation (before the UX changes)

Add events to the current build:

- `app_open`, `session_start`, `session_end`
- `level_started`, `level_completed`, `level_failed`
- `hint_opened`, `hint_purchased` (legacy coins)
- `daily_coin_opened`, `daily_coin_claimed`
- `ad_impression`, `interstitial_shown`
- `purchase_started`, `purchase_completed`, `purchase_failed`
- `remove_ads_purchased`

Capture 7 consecutive days before changing the experience.

### 4.2 Baseline snapshot (device measurement)

| Metric | Baseline |
|---|---|
| Cold start (SM-S908E, API 36) | 559 ms |
| APK size (debug) | 11 MB |
| Crash-free rate | To be measured |
| ANR rate | To be measured |
| Sessions/day | To be measured |
| Level completion | To be measured |

### 4.3 Dashboards

1. **Activation funnel:** install → app_open → onboarding_start → first_item_completed → onboarding_completed.
2. **Engagement:** DAU/WAU, sessions/day, items/session, lesson completion.
3. **Learning:** accuracy by maqam/stage, pre/post gain, mastery distribution, review completion, cents error (R2).
4. **Retention:** D1/D7/D30, streak distribution, daily completion.
5. **Monetization:** paywall conversion, ARPPU, ad impressions/session, rewarded opt-in.
6. **Quality:** crash-free, ANR, audio_error rate, content_error rate, cold start.

Use Firebase + BigQuery export. Keep a debug screen for event validation and a QA checklist per release.

---

## 5. KPI targets

| Metric | Target |
|---|---|
| First lesson completed (activation) | 70% |
| D1 retention | 30% |
| D7 retention | 12% |
| D30 retention | 6% |
| Lesson completion | 75% |
| Learning gain (5-item pre/post) | +20 points |
| Hum opt-in (release 2) | 30% |
| Mastered-maqam share rate | 5% |
| Premium conversion | 1.5–3% |
| Crash-free | 99.5% |

Targets are provisional and will be revised after the 7-day baseline.

---

## 6. Privacy and compliance

- No PII, no free text, no raw audio, no precise location.
- Microphone audio is processed on-device and never uploaded; analytics only receives `cents_error` and `confidence`.
- IP anonymization on; data retention 14 months.
- Consent flow for analytics where required; honor deletion requests.
- Play Data Safety form must match the actual collection.
- Audience is adult/general; COPPA does not apply, but the app remains family-safe.

---

## 7. Instrumentation checklist

- [ ] Event constants and parameter validation.
- [ ] Global parameters wired.
- [ ] Debug event screen.
- [ ] QA plan for each event.
- [ ] Firebase + BigQuery dashboards.
- [ ] 7-day legacy baseline captured.
- [ ] Privacy review signed off.
- [ ] Data Safety form updated.
- [ ] Deletion path tested.
