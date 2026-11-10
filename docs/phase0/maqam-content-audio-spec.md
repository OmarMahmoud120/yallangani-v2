# Maqam Content & Audio Spec

**Phase:** 0 — Discovery  
**Status:** Draft for maqam advisor sign-off  
**Date:** 2026-11-10  
**Tasks:** #2, #3, #11, #18

---

## 1. Purpose

Define the curriculum, tuning, audio deliverables, rights, and metadata required for the recognition-first version of the app. No content ships without advisor sign-off.

The canonical source of truth is a structured content bundle, not prose. This document defines the schema and the production rules; the advisor validates the theory and the musical examples.

---

## 2. Advisor brief (sendable)

### Role

Review and sign off the maqam curriculum and audio examples for an Arabic ear-training app. The advisor is the theory authority; the product team owns the lesson design.

### Scope

- Validate the 9 core maqams, their ajnas, note names, and proposed tuning.
- Resolve the open questions in Section 12, especially Sikah and Huzam.
- Review phrase characteristics and approve each reference recording.
- Provide short Arabic cultural notes for each maqam (100–150 words).
- Give a final written sign-off before content is published.

### Deliverables

1. Annotated tuning table (cents and note names).
2. Nine maqam cards in Arabic: origin/region, mood, characteristic intervals, common genres, one listening suggestion.
3. Phrase notes: what each phrase demonstrates and why it is characteristic.
4. Two review passes: tuning/scales, then phrases/recordings.
5. Final QA sign-off.

### Cadence

| Week | Milestone |
|---|---|
| 1 | Kickoff and tuning table review |
| 2 | Maqam cards and phrase list review |
| 3 | First audio review pass |
| 4 | Final QA and sign-off |

### Rights and credit

- Advisor credited as “Maqam advisor” in the app and on the website.
- Advisor retains authorship of written cards; the app receives a worldwide, perpetual, digital license.
- Original phrases are work-for-hire; the app owns the masters.
- NDA optional before sharing unreleased content.

### Open questions

See Section 12. The advisor may change any proposed value; the spec is a starting point, not a constraint.

### Outreach message

> نعمل على تطبيق لتعليم التعرّف على المقامات وتربية الأذن، ونحتاج مراجعة نظرية وتسجيلية للمقامات التسعة الأساسية: راست، بياتي، حجاز، صبا، كرد، نهاوند، عجم، سيكاه، هزام. المطلوب: مراجعة جدول الدرجات والسنتات، تعريف الجنس واللون المميز لكل مقام، ومراجعة عبارات صوتية قصيرة قبل النشر. هل يناسبك التعاون كمراجع للمحتوى؟

---

## 3. Curriculum scope

### Foundations (first 10 minutes)

1. High vs low.
2. Up vs down.
3. Same vs different.
4. Long vs short.
5. Home note (قرار) and drone.

Then the first win: Hijaz vs Nahawand contrast pair.

### Core nine

| ID | Maqam | Lower jins | Signature interval | Contrast pair | Color token | Story angle |
|---|---|---|---|---|---|---|
| rast | راست | Rast | Neutral third (350¢) | Ajam | Amber `#E2A45C` | The stable home; the sound of the classical ensemble |
| bayati | بياتي | Bayati | Neutral second (150¢) | Kurd | Aqua `#5BC8A6` | Intimate, vocal, close to the chest |
| hijaz | حجاز | Hijaz | Augmented second (300¢) | Nahawand | Terracotta `#C97B5A` | The unmistakable “eastern” color |
| saba | صبا | Saba | Diminished fourth (400¢) | Bayati | Plum `#8E6BAE` | Longing and grief; the “sigh” of the maqam |
| kurd | كرد | Kurd | Minor second (100¢) | Bayati | Teal `#4FA3A5` | Soft, folk-like melancholy |
| nahawand | نهاوند | Nahawand | Minor third (300¢) | Hijaz | Rose `#C77E9B` | The familiar minor; the bridge for new ears |
| ajam | عجم | Ajam | Major third (400¢) | Rast | Gold `#D4AF6A` | Bright, open, celebratory |
| sikah | سيكاه | Sikah | Neutral third on a half-flat tonic | Huzam | Jade `#7FAE6B` | The floating, unresolved color |
| huzam | هزام | Sikah + Hijaz | Raised color over a Sikah home | Sikah | Indigo `#5B7FBF` | Bold, dramatic, “huzam” as a raised call |

### Advanced backlog (after launch)

جهاركاه، نوى أثر، سوزناك، فرحفزا، شوق أفزا، لامي، راست سوزناك.

---

## 4. Proposed tuning table

**Working method:** 24-TET quarter-tone approximations, expressed as integer cents from the tonic. This is a teaching approximation, not a claim about practiced tuning. The advisor must confirm deviations and may replace any row.

Reference: A4 = 440 Hz for UI and tuning tools. Relative cents are canonical; absolute pitch is a rendering detail.

| Maqam (tonic) | Degree cents (1–8) | Nearest notes | Notes |
|---|---|---|---|
| Rast (C4) | 0, 200, 350, 500, 700, 900, 1050, 1200 | C, D, E½♭, F, G, A, B½♭, C | Neutral third and seventh |
| Bayati (D4) | 0, 150, 300, 500, 700, 800, 1000, 1200 | D, E½♭, F, G, A, B♭, C, D | Neutral second; upper Nahawand on G |
| Hijaz (C4) | 0, 100, 400, 500, 700, 800, 1000, 1200 | C, D♭, E, F, G, A♭, B♭, C | Augmented second between degrees 2–3; equal tonic with Rast and Nahawand for A/B |
| Saba (D4) | 0, 150, 300, 400, 700, 800, 1000, 1200 | D, E½♭, F, G♭, A, B♭, C, D | Diminished fourth; upper jins TBC |
| Kurd (D4) | 0, 100, 300, 500, 700, 800, 1000, 1200 | D, E♭, F, G, A, B♭, C, D | Minor second; upper Nahawand on G |
| Nahawand (C4) | 0, 200, 300, 500, 700, 800, 1000, 1200 | C, D, E♭, F, G, A♭, B♭, C | Familiar minor; upper jins is **Kurd on G** (G–A♭–B♭–C). Farahfaza is a separate entry: Nahawand transposed to G |

**Pack 1 authority:** the signed version of this table lives in
`docs/phase0/pack1-tuning-worksheet.md`. That worksheet settles the Rast, Hijaz, and Nahawand
rows, the signature/register plan, and the drone interval. This section stays the nine-maqam
proposal; if they disagree for Pack 1, the worksheet wins.
| Ajam (C4) | 0, 200, 400, 500, 700, 900, 1100, 1200 | C, D, E, F, G, A, B, C | Major third; upper Ajam on G |
| Sikah (E½♭4) | 0, 150, 350, 550, 650, 850, 1050, 1200 | E½♭, F, G, A, B♭, C, D, E½♭ | **Provisional**; confirm upper jins and exact cents |
| Huzam (E½♭4) | **TBD** | E½♭, F, G, A, B♭, C♯, D, E½♭ (candidate) | **Do not produce until advisor defines the scale** |

### Sign-off rules

- No phrase is produced for a maqam whose row is marked TBD.
- Sikah may be produced only after the advisor confirms the upper jins.
- If the advisor prefers practiced tuning over 24-TET, record the deviation per degree in cents and update the content bundle.
- Keep one tuning reference for all instruments; do not mix sources per phrase.

---

## 5. Phrase inventory

Per maqam, produce:

| Type | Count | Purpose |
|---|---|---|
| Scale ascending (plain) | 1 | First exposure |
| Scale descending (plain) | 1 | Direction and cadence |
| Scale with light ornament | 1 | Realistic listening |
| Lower-jins phrase | 3 | Teach the characteristic jins |
| Upper-jins phrase | 3 | Teach the maqam’s upper color |
| Signature phrase | 3 | The “fingerprint”; cover ≥2 registers |
| Cadence (qafla) | 2 | Teach phrase endings |
| Drone (tonic + fifth) | 1 | Practice bed; loopable |
| **Total per maqam** | **15** | |

**Core nine:** 135 assets.  
**Starter pack (3 maqams):** 45 masters + 5 onboarding drills = **50 assets**; the first win reuses
the Hijaz signature.

Slow variants are produced at runtime (`0.75×`, pitch-preserved), not as files. If the advisor
rejects runtime slow after listening, add 3 slow signatures per maqam (9 total) and re-run QA.

### Starter pack recommendation

**Rast, Hijaz, Nahawand.**

- Rast grounds the neutral third and the tonal center.
- Hijaz vs Nahawand is the clearest, fastest first win.
- Pack 2: Bayati, Kurd, Saba.
- Pack 3: Ajam, Sikah, Huzam.

This supersedes the earlier “Rast, Bayati, Hijaz” plan; Task #11 is updated to match.

---

## 6. Instruments, registers, and performance

### Instruments

- **Oud** — primary reference; clear attack, good for pitch.
- **Ney** — breathy color; use for variety after the first exposure.
- **Qanun** — bright, precise; good for scale demonstrations.
- **Voice (“aah”)** — the bridge to humming; include one reference per maqam.
- **Riq / frame drum** — optional and on a separate bus; never in the first pitch-identification item.

### Registers

- Low: tonic around C3–D3.
- Middle: tonic around C4.
- High: tonic around C5.
- The signature group must cover at least two registers for generalization.

Pack 1 implements this as a **signature group**: one matched mid-register signature per maqam (the
A/B contrast core) plus one low and one high signature, so the group covers three registers. A/B is
guaranteed only on the matched core; alternate-register items are recognition-only. See
`docs/phase0/pack1-tuning-worksheet.md` §5.

### Tempo and length

- Slow practice: 60–70 BPM.
- Medium: 90–100 BPM.
- Phrase length: 2–4 seconds; scale: 4–6 seconds.
- Drone: 30–60 seconds, seamless loop.

### Ornamentation levels

- **Plain:** no vibrato, no grace notes; used for first exposure and all pitch tasks.
- **Light:** one slide or grace note; used for recognition generalization.
- **Medium:** tahrir/ornamented; used only after the learner is accurate on plain versions.

Do not use heavy vibrato on reference scales; it confuses both the learner and the pitch detector.

---

## 7. Recording and mastering

### Masters

- 48 kHz / 24-bit WAV.
- Mono for phrases; stereo for drone if it adds width.
- Dry close mic plus a short room; no reverb tail longer than 300 ms.
- Noise floor below −60 dBFS; peaks below −3 dBFS.
- Trim silence; 10 ms fade in/out; preserve the natural attack for onset sync.

### App assets

- AAC-LC 128–160 kbps mono at 44.1 or 48 kHz; or Opus 96 kbps if the playback stack supports it.
- Target ≤25 MB for a three-maqam pack.
- Loudness: −16 LUFS integrated, −1 dBTP; consistent across the pack.
- Do not normalize each phrase independently in a way that changes musical dynamics; apply one consistent gain policy.

### File naming

```
maqam_{id}_{type}_{register}_{bpm}_{instrument}_{version}.m4a
```

Examples:

```
hijaz_sig_mid_090_oud_v1.m4a
rast_scale_up_mid_070_qanun_v1.m4a
nahawand_cadence_low_060_voice_v1.m4a
```

### Rights metadata

Every asset has a `rights_id` pointing to a signed release. No asset ships without it.

---

## 8. Metadata schema

### Maqam

```json
{
  "id": "hijaz",
  "name_ar": "حجاز",
  "name_en": "Hijaz",
  "family": "hijaz",
  "tonic_midi": 62,
  "degrees_cents": [0, 100, 400, 500, 700, 800, 1000, 1200],
  "lower_jins": "hijaz",
  "upper_jins": "nahawand",
  "contrast_with": ["nahawand"],
  "color": "#C97B5A",
  "sigil_seed": 1042,
  "story_ar": "…",
  "status": "approved"
}
```

### Phrase

```json
{
  "id": "hijaz_sig_mid_090_oud_v1",
  "maqam_id": "hijaz",
  "type": "signature",
  "instrument": "oud",
  "register": "mid",
  "bpm": 90,
  "duration_ms": 3200,
  "notes": [
    { "degree": 1, "cents": 0, "duration_ms": 500, "velocity": 0.8, "ornament": null },
    { "degree": 2, "cents": 100, "duration_ms": 400, "velocity": 0.8, "ornament": "slide" },
    { "degree": 3, "cents": 400, "duration_ms": 600, "velocity": 0.9, "ornament": null },
    { "degree": 1, "cents": 0, "duration_ms": 800, "velocity": 0.8, "ornament": null }
  ],
  "audio": {
    "path": "audio/hijaz/hijaz_sig_mid_090_oud_v1.m4a",
    "sha256": "…",
    "rights_id": "R-2026-014",
    "loudness_lufs": -16.0
  },
  "tags": ["recognizable", "plain"],
  "status": "approved"
}
```

### Lesson item

```json
{
  "id": "hijaz_recognize_001",
  "maqam_id": "hijaz",
  "stage": "recognize",
  "type": "maqam_match",
  "phrase_id": "hijaz_sig_mid_090_oud_v1",
  "options": ["hijaz", "nahawand", "kurd"],
  "answer": "hijaz",
  "explanation_ar": "الفرق في النغمة الثالثة: حجاز فيها مسافة زائدة.",
  "scaffolds": ["replay", "slow", "drone", "home_note", "jins", "contrast"],
  "difficulty": 2,
  "status": "approved"
}
```

### Validation rules

- Every `answer` exists in `options`.
- Every `phrase_id` exists and its audio checksum matches.
- Every maqam referenced is `approved`.
- `degrees_cents` is strictly increasing and ends at 1200.
- `cents` values are integers.
- No lesson ships with a `TBD` maqam.

---

## 9. Lesson mapping

| Stage | Content | Example item |
|---|---|---|
| Discover | Scale, two signature phrases, story | Listen and tap the home note |
| Recognize | Contrast pairs, jins identification | Hijaz or Nahawand? |
| Reproduce | Echo 3–5 notes; hum (R2) | Tap back the phrase |
| Apply | New register, light ornament, cadence | Which maqam is this ornamented phrase? |
| Create | Studio phrase on the scale | Record a 4-note phrase |

### Scaffold defaults

- First exposure: drone + home note visible.
- First recognition: replay and slow available; jins hidden.
- After two misses: auto-highlight the characteristic interval and replay the contrast pair.
- Never auto-reveal the answer.

---

## 10. QA checklist

- [ ] Advisor signs off the tuning table.
- [ ] Advisor signs off every maqam card.
- [ ] Every asset is tuned within the agreed tolerance (suggested ±5 cents for sustained notes).
- [ ] Onsets are trimmed and consistent.
- [ ] Loudness is consistent at −16 LUFS.
- [ ] Noise floor < −60 dBFS; no clipping.
- [ ] The signature group covers at least two registers.
- [ ] Slow versions are exactly 0.75× and pitch-preserved.
- [ ] Metadata validates against the schema.
- [ ] Rights file exists for every asset.
- [ ] Audio plays on API 24 and API 35 devices.
- [ ] Arabic text renders correctly in RTL.
- [ ] No famous-song quotations or uncleared samples.

---

## 11. Rights checklist

- [ ] Signed commission/work-for-hire agreement.
- [ ] Signed performer release for every musician and vocalist.
- [ ] Sample clearance if any sample is used (prefer none).
- [ ] Worldwide, perpetual, digital/online, in-app, promotional rights.
- [ ] Right to edit, shorten, loop, and normalize.
- [ ] Stems delivered and archived.
- [ ] Advisor credit and content license.
- [ ] No GPL-licensed audio or pitch-detection code in a closed-source app.
- [ ] Existing `audio.zip` is not reused unless every track has documented rights.
- [ ] Rights IDs mapped in the content bundle.

---

## 12. Open questions for the advisor

1. Exact cents and upper ajnas for Sikah and Huzam.
2. Which regional practice should the app teach: Egyptian, Levantine, or a neutral synthesis?
3. Reference pitch and whether to use 24-TET or practiced tuning.
4. Note naming: Arabic names, Western names, or both in the UI?
5. Ornamentation level for beginners: plain only, or light from the start?
6. Which three signature phrases are most characteristic and least ambiguous per maqam?
7. How to avoid misleading examples for Saba and Huzam, where small tuning differences change the color.
8. Should every item include a drone, or only the first exposure and practice?
9. Is a vocal reference (“aah”) appropriate for all maqams, or only some?
10. Which listening examples (public-domain or licensed) can be recommended in the library?

---

## 13. Starter pack acceptance

The three-maqam starter pack is accepted when:

1. Rast, Hijaz, and Nahawand are fully produced (45 masters + 5 onboarding drills = 50 assets).
2. The advisor signs off theory, cards, and audio.
3. Rights files are complete.
4. The pack is ≤25 MB and plays offline.
5. Metadata validates and every lesson answer is present.
6. Five test users can distinguish Hijaz from Nahawand after one session.
