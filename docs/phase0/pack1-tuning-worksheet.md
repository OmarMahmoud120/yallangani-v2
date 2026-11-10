# Pack 1 tuning worksheet and advisor sign-off

**Version:** 1.0 — 2026-11-10
**Status:** **UNSIGNED.** Proposal only; no recording may start until §9 is signed.
**Scope:** Rast, Hijaz, Nahawand. Sikah and Huzam stay out of Pack 1.
**Blocks:** #5 (audio commissioning), #4 (learner copy), #6 (schema v3 tuning fields)
**Inputs:** `docs/content/maqamworld/maqamworld_inventory.csv`,
`docs/phase0/maqam-content-audio-spec.md` §4, `docs/phase0/content-acquisition-plan.md` §6

---

## 1. What is already frozen, and what this document decides

Frozen by product (do not reopen here):

- Three maqams, 5 items per lesson, DroneToggle ships, 15 masters per maqam, 50 assets total.
- Runtime `0.75×` pitch-preserved slow mode; no separate slow files.
- A/B contrast uses a matched phrase, not a default clip.

This document decides the theory the audio will be built from: tonic, cents per degree, lower
and upper jins, characteristic interval, signature selection, registers, and drone interval.

**Gate:** the advisor initials each decision row in §6 and signs §9. Until then the values below
are proposals, and the producer must not book studio time.

---

## 2. Method and notation

- Cents are relative to the tonic, integer values, degree 8 = 1200.
- Values are 24-TET quarter-tone approximations used for teaching. They are not a claim about
  practiced intonation. If the advisor prefers practiced tuning, record the per-degree deviation
  and it replaces the table.
- Reference pitch A4 = 440 Hz. Tonic register is a rendering choice; relative cents are canonical.
- Jins shapes used in the arithmetic:
  - Rast `0–200–350–500`
  - Nahawand `0–200–300–500`
  - Hijaz `0–100–400–500`
  - Kurd `0–100–300–500`
  - Bayati `0–150–300–500`
  - Ajam `0–200–400–500`
- "Supports" below means the cents in the proposed scale produce that jins exactly. A site label
  that the arithmetic does not support is flagged, not silently copied.

---

## 3. Proposed tuning rows

### Rast — tonic C4 (MIDI 60)

| Degree | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Cents | 0 | 200 | 350 | 500 | 700 | 900 | 1050 | 1200 |
| Nearest note | C | D | E½♭ | F | G | A | B½♭ | C |
| Step | — | 200 | 150 | 150 | 200 | 200 | 150 | 150 |

Characteristic interval: **neutral third, 350¢** (degree 1→3). Secondary color: neutral seventh.
Lower jins: **Rast** (1–4). Upper jins: **Rast on the 5th** (5–8, shape `0–200–350–500`).

### Hijaz — tonic C4 (MIDI 60)

| Degree | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Cents | 0 | 100 | 400 | 500 | 700 | 800 | 1000 | 1200 |
| Nearest note | C | D♭ | E | F | G | A♭ | B♭ | C |
| Step | — | 100 | 300 | 100 | 200 | 100 | 200 | 200 |

Characteristic interval: **augmented second, 300¢** (degree 2→3). Lower jins: **Hijaz** (1–4).
Upper jins: **Nahawand on the 4th** (4–7, shape `0–200–300–500`).

### Nahawand — tonic C4 (MIDI 60)

| Degree | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Cents | 0 | 200 | 300 | 500 | 700 | 800 | 1000 | 1200 |
| Nearest note | C | D | E♭ | F | G | A♭ | B♭ | C |
| Step | — | 200 | 100 | 200 | 200 | 100 | 200 | 200 |

Characteristic interval: **minor third, 300¢** (degree 1→3). Lower jins: **Nahawand** (1–4).
Upper jins: **Kurd on the 5th** (5–8, shape `0–100–300–500`).

---

## 4. Jins audit — what the arithmetic supports

The page's jins row lists cells used in the maqam's melodic practice, including modulation colors.
Pack 1 fixes the **base scale** and the primary upper jins; secondary cells are theory for later
lessons, not alternate tunings of the base scale. "Not in the base scale" below means it must not
be used to justify a different cents row.

| Maqam | Page labels (EN / AR) | Supported by these cents | Not in the base scale | Consequence |
| --- | --- | --- | --- | --- |
| Rast | Rast, Upper Rast, Nahawand | Rast on 1; Rast on 5 (upper) | No Nahawand cell: on F the cell is `0–200–400–550`; on A it is Bayati `0–150–300–500` | Pack 1 uses lower Rast + upper Rast. The Nahawand mention is a sayr color (lowered 6th) or a variant; not a Pack 1 scale. |
| Hijaz | Hijaz; Nahawand **or** Rast at the 4th | Hijaz on 1; Nahawand on 4 (`0–200–300–500` with 500,700,800,1000) | Rast on 4 would need 900/1050, but the scale has 800/1000 | Pack 1 uses Nahawand on 4. The Rast cell is a modulation color, scoped later if wanted. |
| Nahawand | Nahawand; Hijaz **or** Kurd at the 5th | Nahawand on 1; Kurd on 5 (`0–100–300–500`) | Hijaz on 5 needs degree 6 = 1100, but the scale has 800 | Pack 1 uses Kurd on 5. The Hijaz cell is a modulation color, not the base scale. Farahfaza is a separate entry: the site defines it as Nahawand transposed to a G tonic, same scale structure. |

The spec's old note "upper Nahawand on G" for Nahawand is arithmetically impossible (G–A♭–B♭–C is
Kurd). It is corrected here; §4 of the spec is updated to match.

---

## 5. Signature, register, and contrast plan

### 15 masters per maqam

| Slot | Count | Register | Instrument (proposal) | BPM |
| --- | --- | --- | --- | --- |
| Scale up | 1 | mid | qanun | 70 |
| Scale down | 1 | mid | qanun | 70 |
| Ornamented scale (light) | 1 | mid | ney | 70 |
| Lower-jins phrases | 3 | low, mid, high | oud | 90 |
| Upper-jins phrases | 3 | low, mid, high | oud | 90 |
| Signature phrases | 3 | mid, low, high | oud | 90 |
| Cadences | 2 | mid, low | oud | 90 |
| Drone (tonic + fifth) | 1 | mid | oud | loop |
| **Total** | **15** | | | |

### Signature group

- `sig_core_mid` — the A/B contrast core. One per maqam, same instrument (oud), same BPM (90),
  same phrase length (3–4 s), same register.
- `sig_alt_low` — same maqam, lower register, different phrase shape.
- `sig_alt_high` — same maqam, higher register, different phrase shape.

Three signatures × three maqams cover **three** registers, exceeding the "at least two" rule.
The first win uses `hijaz_sig_core_mid` against `nahawand_sig_core_mid`, so it adds no file.

### A/B rule that keeps the 15-master budget honest

- A/B comparison is guaranteed **only for the matched mid contrast core**.
- Items that use `sig_alt_low` or `sig_alt_high` are recognition-only (no A/B), because no
  matched contrast exists at that register within 15 masters.
- If the advisor wants A/B in all registers, the pack grows by 4 files (one matched contrast per
  extra register per pair). That is a scope change, not a default.

### Contrast core requirements (producer checklist)

- Same instrument, register, BPM, approximate length, and ornament level.
- Same phrase type: both are signature phrases, not "signature vs cadence".
- Loudness matched at −16 LUFS; no gain jump when A/B plays.
- No phrase quotes a known melody (rights triage class 7 is Red).

---

## 6. Decision register — advisor initials required

| # | Decision | Proposal | Why | Advisor | Date |
| --- | --- | --- | --- | --- | --- |
| D1 | Rast third jins (page lists Nahawand) | Use lower Rast + upper Rast on the 5th; ignore the Nahawand label for Pack 1 | No Nahawand cell exists in the proposed cents | ☐ | |
| D2 | Hijaz upper jins | Nahawand on the 4th | Matches the plain scale; Rast-upper needs degrees 6–7 raised | ☐ | |
| D3 | Nahawand upper jins | Kurd on the 5th; Farahfaza tracked separately (Nahawand on G) | Hijaz-upper raises degree 6 to 1100; it is a modulation color, not this scale | ☐ | |
| D4 | Tonic registers | **All three at C4 (MIDI 60)** | Equal tonics make A/B compare color, not pitch height; matches the current synth root and sits mid-range. Alternative: all at D4 for Hijaz's familiar key, or the spec's mixed C4/C4/D4 (not recommended: height becomes a cue) | ☐ | |
| D5 | Tuning system | 24-TET values above; practiced deviations recorded per degree if preferred | Simple, testable, matches the app's current synth | ☐ | |
| D6 | Regional practice | Neutral synthesis leaning Egyptian/Levantine, stated in the advisor card | Avoids claiming one tradition's intonation | ☐ | |
| D7 | Note naming in UI | Arabic degree names (درجة ١…) with Western note names in advisor material only | Learner-facing app is Arabic-only | ☐ | |
| D8 | Beginner ornament | Plain for scale and signature; light only for the ornamented scale | Keeps pitch tasks unambiguous | ☐ | |
| D9 | Signature brief | Approve three shapes per maqam; the mid core must be the most characteristic; final phrases are approved at the audio review | Drives recognition and the first win | ☐ | |
| D10 | Drone interval | Tonic + fifth, sustained, no pulse | Matches the home-note drill and keeps the tonic audible | ☐ | |
| D11 | Instruments | Oud for all recognition material, qanun for plain scales, ney for the ornamented scale | One color for answers, brighter timbre for scale demos | ☐ | |
| D12 | Vocal reference | Not in Pack 1 (no slot in 15); revisit in Pack 1b | Would need a 16th master per maqam | ☐ | |

If the advisor changes any value, edit §3, initial the change inline, and update the pack schema
source in #6. Do not keep two versions of the tuning table.

---

## 7. Drone specification

- One loopable master per maqam: **tonic + fifth**, sustained, no rhythm, no modulation.
- 30–60 s seamless loop; loop point on a zero crossing with matched room tone.
- Mixed 6–9 dB under the phrase; the app applies fixed gain, not per-item ducking.
- Must not mask the characteristic degree: no added third in the drone.
- Same instrument family as the signatures (oud proposal) so the bed does not change color.
- DroneToggle state is visible/announced (engineering task #11); the audio contract is this file.

---

## 8. Acceptance checks for the tuning itself

Run after the advisor signs, before the first phrase is approved:

- [ ] Every degree is within ±5 cents of the signed row on sustained notes.
- [ ] The neutral third of Rast measures 350¢, not 300 or 400.
- [ ] Hijaz degrees 2→3 measure 300¢; degrees 1→2 measure 100¢.
- [ ] Nahawand degree 6 is 800¢ (A♭), not 900¢; degree 7 is 1000¢.
- [ ] Signature contrast pairs match in instrument, register, BPM, and length.
- [ ] Drone loop has no click and no audible seam over five repetitions.
- [ ] No master exceeds the 2–4 s phrase / 30–60 s drone targets.

---

## 9. Sign-off

**Advisor statement (Arabic):**

> أقرّ بأن جدول الدرجات والسنتات والأجناس للمقامات راست وحجاز ونهاوند في هذا المستند يمثل
> الصيغة التي أوافق على تسجيلها للتطبيق، وأن ثلاث عبارات التوقيع لكل مقام تغطي ثلاث طبقات،
> وأن عبارة التوقيع الوسطى لكل مقام تصلح للمقارنة الثنائية. وأتحمل مراجعة أي تغيير لاحق في
> هذه القيم قبل النشر.

**Advisor statement (English):**

> I approve the degree/cents table, the lower and upper ajnas, and the signature/register plan for
> Rast, Hijaz, and Nahawand as the version to be recorded for the app. I will review any later
> change to these values before release.

| Field | Value |
| --- | --- |
| Advisor name | |
| Role / affiliation | |
| Signature | |
| Date | |
| Version signed | 1.0 |
| Scope | Rast, Hijaz, Nahawand only |

Signed copy is stored outside the repository with the contributor agreement
(`docs/phase0/rights-triage.md` §5). Only the date and version are recorded here.

---

## 10. Outreach message (send with this document)

> أهلاً،
>
> نعمل على تطبيق «يلا نغني» لتدريب الأذن على المقامات. نحتاج مراجعة قصيرة لثلاثة مقامات فقط:
> راست وحجاز ونهاوند. المستند المرفق يحتوي جدول الدرجات بالسنتات، والجنس السفلي والعلوي لكل
> مقام، وثلاث عبارات توقيع بثلاث طبقات، واثنتا عشرة نقطة قرار معلّمة باقتراحنا.
>
> المطلوب: تصحيح أي قيمة، ثم توقيع قسم الإقرار في نهاية المستند. لن نبدأ التسجيل قبل التوقيع.
>
> شكراً.

Suggested deadline: 5 working days. If the advisor is unavailable, the pack does not record;
there is no "provisional ship" path.

---

## 11. Handoff after signature

| Task | Change |
| --- | --- |
| #5 audio | Booking opens; 45 masters + 5 drills; this file is the tuning authority |
| #6 schema | `degrees_cents`, `lower_jins`, `upper_jins`, `tonic_midi`, `contrast` fields frozen from §3 |
| #4 copy | Signature explanations name the interval from §3, not generic praise |
| #7 lessons | Option pairs use the mid contrast core; alt-register items are recognition-only |
| #9 release | Verify shipped audio matches the signed rows within ±5 cents |
