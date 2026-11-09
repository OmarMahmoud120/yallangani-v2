# MaqamWorld → يلا نغني Content Acquisition Plan

**Owner:** Content Manager  
**Date:** 2026-11-10  
**Status:** Reviewed draft v2 — corrections applied after codebase and source audit  
**Source:** <https://www.maqamworld.com/> (EN + AR editions)  
**Companion docs:** `maqam-content-audio-spec.md`, `ui-ux-audit.md`, `CONTENT_LICENSES.md`

---

## 0. Review log (2026-11-10)

This plan was reviewed against the current working tree, the content spec, and the live site.
Corrections applied:

| # | Finding | Resolution in this version |
| --- | --- | --- |
| R1 | Onboarding was missing from the plan. The app now requires **5 foundation drills + a first-win item**, with audio, options, answers, and explanations. | Added onboarding to scope, inventory, audio, copy, schema, tasks, and QA. |
| R2 | Current pack is already **schema version 2**. Calling the target “schema v2” was ambiguous. | Target is now **pack schema v3**. |
| R3 | Source counts were overstated: the index lists **43 maqams**, but only **35** have detail pages; **24** jins detail pages were found, not 27. | Corrected in §2 and §5. |
| R4 | A/B comparison currently plays the answer phrase against the other maqam’s **default 12-second clip**. With phrase-level content that compares unlike material. | Added a matched **contrast core** and `contrast_phrase_id` contract in §6.3. |
| R5 | The app has **no drone control**; the UI audit plans one, but it is not implemented. Three drone masters would be orphaned. | Drone is now a conditional decision, not an automatic asset. |
| R6 | The spec contradicts itself on signature registers: §5 counts 3 signature phrases, §6 says every signature phrase exists in two registers. | Added an explicit decision table in §6.1 (15 vs 18 masters per maqam). |
| R7 | `AGENTS.md` says 4 items per lesson; the UI contract says 5; the pack has 4. | Flagged as a required owner decision in §6.5 and §12. |
| R8 | The 100–150-word maqam cards and listening suggestions belong to a Library screen that does not exist yet. | Split copy into **ship-required** and **Library-ready** in §6.6. |
| R9 | `CONTENT_LICENSES.md` is not stale; it was already updated for the maqam pack and onboarding. | Removed the earlier “100-song target” finding; corrected the audit in §8. |
| R10 | The 7 unreferenced maqam files are documented as future content, not silent orphans. | Softened the finding; added a no-orphan/usage-map rule instead of calling them undocumented. |

---

## 1. Executive summary

MaqamWorld is the right **theory and terminology backbone** for the app, but it is not a
drop-in content pack. It offers a large maqam/jins catalogue, Arabic-language pages, notation, and
long audio examples. The app needs a much smaller, curated, offline, rights-cleared pack:

- **5 onboarding drills + 1 first-win item** (the current app requires these).
- **3 starter maqams:** Rast, Hijaz, Nahawand.
- **15–18 phrase masters per maqam**, depending on three decisions in §6.1.
- **3 recognition lessons** at 4 or 5 items each, depending on the owner decision in §6.5.
- **Original Arabic teaching copy**, written for learners.
- **Rights IDs and checksums** for every shipped asset.

The plan separates four asset classes that are easy to conflate:

| Asset class | MaqamWorld role | What ships in the app |
| --- | --- | --- |
| Names, maqam/jins facts, scale degrees | Reference to verify | Structured facts, advisor-approved |
| Editorial prose (history, mood, commentary) | Reference only unless licensed | **New Arabic copy**, written for learners |
| Notation images | Reference only | Redrawn in-app or omitted in v1 |
| Audio examples (taqsim, muwashah, song) | Listening reference for the advisor | **New short phrases**, commissioned and licensed |

**Recommended path:** use MaqamWorld to freeze theory and Arabic terminology, write original app
copy, redraw any notation, and commission **45 maqam masters + 5 onboarding drills** for Rast,
Hijaz, and Nahawand. Runtime `0.75×` supplies the slow variants. Ship behind the existing
recognition loop, then expand to Pack 2 and Pack 3.

**Blocking issue:** the site footer reads “© 2001–2018 MaqamWorld. All rights reserved.” Even if
the app team controls the site, the recordings are performances of named compositions and may
carry separate performer/composer rights. No prose, notation image, or audio file enters the
repository until the rights register in §4 is signed off.

---

## 2. What MaqamWorld actually contains (verified 2026-11-10)

| Section | Volume | Format | Fit for the app |
| --- | --- | --- | --- |
| Maqam index | 43 maqams listed | HTML index | Theory source; needs curation |
| Maqam detail pages | 35 pages | HTML, notation, ajnas, commentary, audio | Theory source; 8 listed maqams have no dedicated page |
| Jins pages | 24 detail pages | HTML, intervals, notation, audio | Theory source for lower/upper jins |
| Iqaa | Rhythm catalogue | HTML + audio | Future feature, not v1 |
| Forms | Catalogue | HTML + audio | Future library content |
| Instruments | Catalogue | HTML + audio | Future library content |
| Languages | EN, AR, DE, EL, ES, FR, IT, PT | Parallel HTML | AR edition is the terminology source |
| Audio examples | Taqsims, muwashahat, songs | MP3, long form | **Reference only**; not lesson phrases |

The site has no structured export or public API. Page slugs are stable
(`/en/maqam/hijaz.php`, `/ar/maqam/hijaz.php`), so a one-time snapshot is practical. The copyright
notice ends at 2018, but that does not prove the pages were not edited later; treat the snapshot
date as part of the provenance, not as a stability guarantee.

Eight maqams appear in the index without a dedicated detail page in the current navigation:
Farahfaza, Muhayyar, Rahat al-Arwah, Sazkar, Shadd Araban, Shahnaz, Suzidil, and Yakah. Verify
each before adding it to a future pack.

### Why the audio examples cannot simply be imported

- They are **long-form performances**, not 2–4 second recognition phrases.
- They are **ornamented and metered**, often with instruments and vocals mixed.
- They are **named compositions**, so excerpting them creates composition/sync questions on top
  of master questions.
- Lesson items need controlled contrast: same register, same instrument family, plain versus
  lightly ornamented, matched loudness. Site examples do not provide that control.

Using them would require editing rights, composition sync rights, performer releases, and a
re-mastering pass — more work than commissioning clean phrases, with worse pedagogical fit.

---

## 3. Gap analysis: source vs. app

| App requirement | MaqamWorld provides | Gap to close |
| --- | --- | --- |
| Onboarding: 5 drills + first win | No direct equivalent | Author and record original drills; first win uses a Hijaz signature |
| 9 core maqams, 3 at launch | 43 listed, 35 detail pages | Curate and freeze 9; map variants |
| 15–18 phrase masters per maqam | Long audio examples | Commission short, matched phrases |
| A/B contrast per item | Nothing usable | Record a matched contrast core per pair |
| 2–4 s phrases, 4–6 s scales | Songs and taqsims | New recordings with a strict brief |
| Two registers per signature phrase | Various | Decide 15 vs 18 masters (§6.1) |
| Plain / light ornament levels | Ornamented performances | Record separate takes |
| Arabic learner copy | Academic/editorial prose | Write original prompts, cards, explanations |
| Item-level explanations | None | Author one per item |
| Rights IDs and checksums | None | Add to schema and register |
| Offline, ≤25 MB | Web audio | AAC assets, size budget, CI check |
| Consistent tuning | Notation + regional notes | Advisor-approved cents table |
| Library story + listening suggestion | Commentary + audio | Defer until the Library screen exists |

---

## 4. Rights gate (blocking for shipping, not for research)

### 4.1 Rights matrix

Create one row per asset class before any extraction. Each row needs: rights holder, license
type, territory, term, editing rights, credit requirement, evidence file, and approver.

| Asset class | What we need | Fallback if not cleared |
| --- | --- | --- |
| Maqam names, jins names, scale facts | No permission usually needed for facts; document source | Advisor-written theory sheet |
| MaqamWorld editorial prose (EN/AR) | Written license or assignment from the authors/owner | Write original copy from facts |
| Arabic translations | Same as prose | Our own translation/terminology |
| Notation images | License to reproduce, or redraw | Redraw in Compose/SVG |
| Site audio recordings | Master license + performer releases + editing rights | Commission new recordings |
| Compositions excerpted in site audio | Sync license for each work | Do not excerpt; use original phrases |
| Library listening suggestions | License for any in-app audio | Text-only credit, or omit |
| “MaqamWorld” name/logo | Trademark/credit permission if used in-app | Text-only credit or no mark |
| Advisor cards and review | Written contributor agreement + credit | Work-for-hire or in-house review |
| Onboarding drills | Original recordings; performer releases if commissioned | Keep CC0 synth drills as placeholder |

### 4.2 Evidence to collect

1. A signed statement of who owns the MaqamWorld editorial content, with the chain of title.
2. Performer releases for every musician/vocalist on any reused recording.
3. Composition sync licenses for any excerpted work.
4. Territory/term confirmation: worldwide, perpetual, digital/in-app, promotional.
5. Right to edit, shorten, loop, normalize, and transcode.
6. Credit line wording approved by the rights holder.

Store contracts and license text outside the APK, referenced from
`docs/CONTENT_LICENSES.md`. Do not commit restricted raw page snapshots or audio to a public
repository; commit only derived facts and the rights register.

### 4.3 Decision rule

- **Green:** reuse allowed in the app, worldwide/perpetual, with evidence on file.
- **Amber:** reference only; do not copy or ship.
- **Red:** do not use.

The pack cannot pass acceptance with any Amber/Red asset in `assets/content/`.

**Parallel work:** rights triage can run alongside the source snapshot and fact inventory. The
gate blocks shipping and audio reuse, not reading public pages or writing original copy.

---

## 5. Inventory and mapping

### 5.1 Core nine

| App ID | Arabic | MaqamWorld page | Pack | Launch status |
| --- | --- | --- | --- | --- |
| `rast` | راست | `/maqam/rast.php` | 1 | Ship |
| `hijaz` | حجاز | `/maqam/hijaz.php` | 1 | Ship |
| `nahawand` | نهاوند | `/maqam/nahawand.php` | 1 | Ship |
| `bayati` | بياتي | `/maqam/bayati.php` | 2 | After Pack 1 |
| `kurd` | كرد | `/maqam/kurd.php` | 2 | After Pack 1 |
| `saba` | صبا | `/maqam/saba.php` | 2 | After Pack 1 |
| `ajam` | عجم | `/maqam/ajam.php` | 3 | After Pack 2 |
| `sikah` | سيكاه | `/maqam/sikah.php` | 3 | Gated on tuning sign-off |
| `huzam` | هزام | `/maqam/huzam.php` | 3 | Gated on full theory sign-off |

Advanced backlog, after launch: Jiharkah, Nawa Athar, Suznak, Farahfaza, Shawq Afza, Lami,
Rast Suznak. Some backlog names have no dedicated detail page; verify before scoping. Do not let
the 43-page source pull v1 into a 43-maqam scope.

### 5.2 Inventory files

Create `docs/content/maqamworld/` (or a private working folder) with:

**`maqamworld_inventory.csv`**

```csv
app_id,maqamworld_slug,name_ar,name_en,pack,lower_jins,upper_jins,tonic,degrees_cents,source_url,arabic_source_url,notes,rights_status
hijaz,hijaz,حجاز,Hijaz,1,حجاز,نهاوند,D4,"0,100,400,500,700,800,1000,1200",https://www.maqamworld.com/en/maqam/hijaz.php,https://www.maqamworld.com/ar/maqam/hijaz.php,,amber
```

**`aliases.csv`** for variants and synonyms (e.g. Sikah/Huzam spellings, Ajam/Ushayran,
Nahawand/Murassa‘) so Pack 2/3 mapping does not repeat the research.

**`terminology_ar.csv`** for the glossary: maqam, jins, qarar, ghammaz, scale degree, cadence,
neutral interval, ornament. Use the Arabic edition as evidence, then have the advisor approve
the learner-facing wording.

### 5.3 Foundation drills (original, not from MaqamWorld)

The current pack already defines the five drills below; keep them as the content baseline and
replace the synthetic audio if the onboarding audio is commissioned.

| Step ID | Prompt (current) | Skill | Audio shape |
| --- | --- | --- | --- |
| `high_low` | أي النغمتين أعلى؟ | High vs low | Two notes, one clearly higher |
| `up_down` | أي المقطعين يبدأ صاعدًا؟ | Up vs down | Two short phrases, opposite direction |
| `same_different` | هل النغمتان متطابقتان؟ | Same vs different | Two notes, one slightly higher |
| `long_short` | أي النغمتين أطول؟ | Long vs short | Two notes, different durations |
| `home_note` | أي المقطعين ينتهي على نغمة البيت؟ | Home note + drone | Two short phrases over a drone |
| `first_win` | أي مقام تسمع؟ | First recognition win | Hijaz signature vs Nahawand signature |

The drills are relative-listening exercises; they do not need quarter-tone maqam tuning. The first
win does, so it must reuse the approved Hijaz signature phrase and a matched Nahawand contrast.

### 5.4 Extraction rules

1. Snapshot the page and date it before using it as a source.
2. Record facts, not sentences. Never paste site prose into the pack.
3. One row per app ID; variants stay in `aliases.csv`, not in the shipping pack.
4. Mark every derived row Amber until the rights matrix clears it.
5. Cross-check the Arabic name against the Arabic page, not an English transliteration.

---

## 6. Target pack v1

### 6.1 Audio inventory and the three decisions

The 18-phrase spec assumes three slow signature slots. The app already has pitch-preserved
`0.75×` playback, so recommend **runtime slow** instead of separate files. The unique-master count
then depends on two unresolved questions: drone support and signature registers.

| Decision | Masters per maqam | Maqam total | Onboarding | First win | Pack total |
| --- | --- | --- | --- | --- | --- |
| A (recommended): 15/maqam, drone included, 3 signatures across ≥2 registers | 15 | 45 | 5 | reuse Hijaz signature | 50 |
| B: 15/maqam minus drone (no DroneToggle in v1) | 14 | 42 | 5 | reuse | 47 |
| C: 18/maqam, each signature in two registers, drone included | 18 | 54 | 5 | reuse | 59 |

If the advisor rejects runtime slow and wants separate `0.75×` masters, add **3 files per maqam**
(9 total for Pack 1). Do not add them by default.

**15-master breakdown (Decision A):** scale up, scale down, ornamented scale, 3 lower-jins,
3 upper-jins, 3 signature, 2 cadence, 1 drone.  
**18-master breakdown (Decision C):** same, but 6 signature files (each of the 3 signatures in two
registers).  
The spec's §5 and §6 contradict each other here; the advisor must pick A or C in Gate 2.

### 6.2 Onboarding contract

The app currently enforces **exactly 5 steps** (`MaqamPackValidator.ONBOARDING_STEPS = 5`), plus a
first-win item. Keep that contract.

Each onboarding step needs:

- `id`, `prompt_ar`, `audio`, at least two `options` with Arabic labels, an `answer` that exists
  in the options, and a non-blank `explanation_ar`.
- One unambiguous correct answer; no trick questions.
- 2–6 second audio, one clear contrast, no maqam names in the prompt.
- A single instrument family across the five drills so the learner hears a consistent voice.

The first win needs a prompt, audio, two maqam IDs, an answer in those IDs, and an explanation.
It must use the approved Hijaz signature phrase; the contrast should be the matched Nahawand
signature, not an arbitrary default clip.

### 6.3 A/B contrast contract

Current behavior: the lesson plays `question.audio`, then `question.contrastAudio`, where
`contrastAudio` is the other maqam’s **default** `audio`. With one 12-second clip per maqam that is
acceptable; with phrase-level content it is not.

Requirements:

- Every lesson item gets a `contrast_phrase_id` (optional in JSON, required by the authoring
  sheet) pointing to a phrase from the other option.
- The contrast phrase must match the item phrase in **instrument, register, phrase type, and
  tempo** so the learner compares maqam color, not arrangement.
- Record a **contrast core** per pair: one matched signature phrase per maqam, same instrument,
  register, BPM, and phrase length. These can be three of the three signature masters, so no extra
  files are needed.
- If a lesson has three options, name the closest contrast explicitly; do not let the engine pick
  the first non-answer.
- A/B loudness must be matched; no gain jump between the two examples.

### 6.4 Audio targets

| Attribute | Target |
| --- | --- |
| Masters | 48 kHz / 24-bit WAV, mono; drone stereo allowed |
| App format | AAC-LC 128–160 kbps mono, 44.1 or 48 kHz |
| Phrase length | 2–4 s; scales 4–6 s; drone 30–60 s loopable |
| Onboarding drills | 2–6 s; first win 3–4 s |
| Tempo | 60–70 BPM practice; 90–100 BPM medium |
| Loudness | −16 LUFS integrated, −1 dBTP, consistent across pack |
| Tuning tolerance | ±5 cents on sustained notes |
| Noise floor | below −60 dBFS; no clipping |
| Registers | Signature phrases cover at least two registers |
| Ornament | Plain for first exposure; light for generalization |
| Pack size | ≤15 MB target, 25 MB hard ceiling |

### 6.5 Lesson contract and the 4-vs-5 decision

Three lessons, one per starter pair:

| Lesson ID | Title (draft) | Options | Items |
| --- | --- | --- | --- |
| `hijaz_nahawand` | حجاز أم نهاوند؟ | hijaz, nahawand | 5 |
| `rast_hijaz` | راست أم حجاز؟ | rast, hijaz | 5 |
| `rast_nahawand` | راست أم نهاوند؟ | rast, nahawand | 5 |

**Conflict:** `AGENTS.md` says 4 items per lesson; `ui-ux-audit.md` says 5 progress dots and
5 items; the current pack has 4. The UI already renders dots from `session.total`, so 5 works
without a layout change. Recommend **5** and update `AGENTS.md`; if the owner freezes 4, the
authoring count becomes 12 items, not 15.

Authoring rules:

- Answer split 3/2 or 2/3 per lesson; no answer three times in a row.
- First item is a plain signature or scale; ornamented/cadence items come later.
- No phrase repeats within a lesson.
- Each item points to a specific phrase and a matched contrast phrase.
- Explanation is ≤2 sentences, names one audible feature, and contrasts the other option.
- Correct-answer position is shuffled at runtime; do not author positions.
- No item may be solvable from the lesson title.

### 6.6 Copy scope: ship-required vs Library-ready

The current UI consumes maqam names, jins labels, signatures, onboarding prompts/explanations, and
lesson explanations. It has no Library detail screen yet. Do not block Pack 1 on copy the app
cannot display.

**Ship-required now**

- 5 onboarding prompts, option labels, and explanations.
- First-win prompt and explanation.
- 3 maqam names, jins labels, and one-sentence signatures.
- 12–15 lesson-item explanations.
- One glossary for consistent Arabic terms.

**Library-ready, defer unless the Library screen ships**

- 100–150-word maqam cards.
- Two-line stories for maqam sheets.
- Listening suggestions.
- Notation redraws.

The advisor's 100–150-word cards remain a useful deliverable, but they are not a Pack 1 release
blocker until the UI consumes them.

### 6.7 Pack schema v3

The current pack is **version 2** and already includes onboarding. Extend it to version 3:

```json
{
  "pack_id": "starter-3-v3",
  "version": 3,
  "notice": "…",
  "onboarding": {
    "steps": [
      {
        "id": "high_low",
        "prompt_ar": "أي النغمتين أعلى؟",
        "audio": "onboarding/on-01-high-low.m4a",
        "options": [
          { "id": "first", "label_ar": "الأولى" },
          { "id": "second", "label_ar": "الثانية" }
        ],
        "answer": "second",
        "explanation_ar": "…"
      }
    ],
    "first_win": {
      "prompt_ar": "أي مقام تسمع؟",
      "audio": "hijaz/hijaz_sig_mid_090_oud_v1.m4a",
      "maqam_ids": ["hijaz", "nahawand"],
      "answer": "hijaz",
      "explanation_ar": "…"
    }
  },
  "maqams": [
    {
      "id": "hijaz",
      "name_ar": "حجاز",
      "name_en": "Hijaz",
      "color": "#C97B5A",
      "jins_ar": "جنس حجاز",
      "signature_ar": "…",
      "story_ar": "…",
      "tonic_midi": 62,
      "degrees_cents": [0, 100, 400, 500, 700, 800, 1000, 1200],
      "lower_jins": "hijaz",
      "upper_jins": "nahawand",
      "contrast_with": ["nahawand"],
      "audio": "hijaz/hijaz_sig_mid_090_oud_v1.m4a",
      "status": "approved"
    }
  ],
  "phrases": [
    {
      "id": "hijaz_sig_mid_090_oud_v1",
      "maqam_id": "hijaz",
      "type": "signature",
      "instrument": "oud",
      "register": "mid",
      "bpm": 90,
      "duration_ms": 3200,
      "audio": "hijaz/hijaz_sig_mid_090_oud_v1.m4a",
      "rights_id": "R-2026-014",
      "sha256": "…",
      "loudness_lufs": -16.0,
      "status": "approved"
    }
  ],
  "lessons": [
    {
      "id": "hijaz_nahawand",
      "title_ar": "حجاز أم نهاوند؟",
      "maqam_ids": ["hijaz", "nahawand"],
      "items": [
        {
          "id": "hn_1",
          "phrase_id": "hijaz_sig_mid_090_oud_v1",
          "contrast_phrase_id": "nahawand_sig_mid_090_oud_v1",
          "answer": "hijaz",
          "explanation_ar": "…",
          "difficulty": 2
        }
      ]
    }
  ]
}
```

Engineering implications:

- Keep `Maqam.audio` as a fallback and as the default contrast phrase.
- Resolve `item.phrase_id` to `phrase.audio`; resolve `contrast_phrase_id` for A/B.
- Keep options at lesson level (`maqam_ids`); do not duplicate them per item. The current spec's
  per-item `options` is redundant unless a future lesson mixes option sets.
- Validator additions: unique phrase IDs, phrase→maqam exists, item→phrase exists,
  contrast→phrase exists and belongs to another lesson option, every approved phrase has
  `rights_id` and an existing file, no orphan phrases.
- Verify `sha256` in CI, not at runtime.
- Keep audio in per-section subfolders (`audio/hijaz/`, `audio/onboarding/`); update the current
  flat path resolution.
- Do not ship the raw MaqamWorld snapshot in the APK.

### 6.8 Usage map and no-orphan rule

Every shipped master must be referenced by at least one of:

1. An onboarding step or the first win.
2. A lesson item (as `phrase_id` or `contrast_phrase_id`).
3. A Library surface (once it exists).

Anything else is either removed or explicitly tagged `reserved_for_pack: 2` and excluded from the
release build. This replaces the current “ship 7 unreferenced files for future maqams” practice.

---

## 7. Production pipeline and gates

| Gate | Work | Output | Owner | Exit criteria |
| --- | --- | --- | --- | --- |
| 0. Rights | Rights matrix, contracts, credit line | Signed register | Legal + Content | Every class Green/Amber; no Red in pack |
| 1. Inventory | Snapshot pages, build 43→9 CSV, glossary | `maqamworld_inventory.csv` | Content | 9 core rows reviewed; aliases captured |
| 2. Theory + playback | Tuning worksheet, ajnas, signature-register, drone, item-count, A/B decisions | Signed decision record | Advisor + Product | 3 launch maqams approved; decisions A/B/C and 4/5 fixed |
| 3. Copy | Onboarding copy, signatures, item explanations | Arabic copy sheet | Content + Advisor | Fits learner voice; no copied prose |
| 4. Audio | Brief, record, edit, master, QA | Maqam masters + 5 drills + cue sheet | Audio producer | Tuning/loudness/noise pass; advisor approves |
| 5. Lessons | Assign phrases and contrasts, balance, validate | Pack JSON | Content | Items valid, no repeats, matched contrasts |
| 6. Integration | Schema v3, loader, validator, tests | Build + tests | Android | Unit + instrumented tests pass |
| 7. Release | Device QA, blind test, licenses, tag | Pack v1 | QA + Content | RTL/320 dp/TalkBack/airplane pass |

### 7.1 Audio brief (send to producer)

- **Instrumentation:** oud primary. Ney for one take per maqam, qanun for scale clarity, voice
  “aah” only after advisor approval. No percussion in the first recognition item.
- **Registers:** low (C3–D3), middle (C4), high (C5).
- **Phrasing:** original melodic phrases; no quotations of known songs; no samples.
- **Contrast core:** for each pair, record a matched signature phrase per maqam with the same
  instrument, register, BPM, and approximate length. This is the A/B reference.
- **Performance:** plain takes with no heavy vibrato; separate light-ornament takes; preserve the
  natural attack; trim silence with 10 ms fades.
- **Delivery:** 48/24 WAV masters, stems archived, one cue sheet row per file, signed release per
  performer, `rights_id` assigned before delivery.
- **Naming:** `{maqam}_{type}_{register}_{bpm}_{instrument}_v1.wav`; app file `.m4a`.

### 7.2 Onboarding brief

- Five drills: high/low, up/down, same/different, long/short, home note over drone.
- Two options per drill; one unambiguous answer; one instrument family.
- 2–6 s each; home-note drill may run to 6–8 s to establish the drone.
- Original exercises; no maqam names in the prompt; no notation required.
- Recommendation: commission the five drills with the same producer as the maqam phrases for a
  consistent timbre. If budget is tight, the current CC0 synth drills can ship as placeholders,
  but they must be clearly labeled in the license doc and replaced before the pack is marketed as
  a finished listening course.

### 7.3 Copy brief

- **Voice:** Modern Standard Arabic, warm and precise, not academic. One idea per sentence.
- **Onboarding:** ask one listening question, not a theory question. Explanation names what to
  listen for.
- **Signature (1 sentence):** the single audible feature a beginner should hear.
- **Item explanation (≤2 sentences):** name the feature, state the answer, contrast the other
  option. No shaming, no score talk.
- **Terminology:** one glossary; use the same Arabic term for jins, qarar, ghammaz, and cadence
  everywhere.
- **Draft examples:**
  - حجاز: «المسافة الزائدة بين الدرجتين الثانية والثالثة هي توقيع الحجاز.»
  - نهاوند: «الدرجة الثالثة صغيرة؛ هذا هو المقام الصغير المألوف للأذن.»
  - راست: «الدرجة الثالثة محايدة بين الكبير والصغير؛ هذا هو قرار الراست.»

---

## 8. Current pack audit (why we cannot launch on it)

| Finding | Impact |
| --- | --- |
| 3 maqams, 3 lessons × **4 items**, 5 onboarding steps | Onboarding is implemented; item count conflicts with the 5-item UI contract |
| Pack is **version 2** | New schema must be version 3; do not call it v2 |
| 3 maqam clips + 5 onboarding clips are referenced; 7 maqam clips are unreferenced | Unreferenced audio bloats the APK; a usage map is needed |
| Maqam clips are 12 s, same contour, equal-tempered semitone tables | Rast is a major scale; Bayati/Saba/Sikah/Huzam lose their characteristic intervals; weak recognition training |
| Onboarding drills are synthesized relative exercises | Acceptable as placeholders; commission or keep CC0 explicitly |
| One default audio per maqam; A/B compares unlike material | Contrast must be matched by type, instrument, and register |
| No drone control in the app | Drone masters would be orphaned unless DroneToggle ships |
| No phrase-level schema, rights IDs, checksums, or contrast IDs | Cannot pass the content gate |
| `CONTENT_LICENSES.md` documents all 15 files and the 18-phrase target | Not stale; it does still use 8–15 s phrase language, which conflicts with the 2–4 s spec |

**Decision:** treat the current synth pack as a development fixture only. Remove or exclude the
unreferenced files from the release build, and replace launch audio with licensed masters. Do not
“improve” the synth generator for release; recognition training needs performed, tuned phrases and
matched contrasts.

---

## 9. QA and acceptance

### Onboarding QA

- [ ] Exactly 5 steps; unique IDs; prompts and explanations non-blank.
- [ ] Each step has at least two options, a valid answer, and existing audio.
- [ ] First win uses an approved Hijaz signature and a matched Nahawand contrast.
- [ ] Onboarding audio is 2–6 s, clear on a phone speaker, and consistent in timbre.
- [ ] No maqam name appears in a drill prompt before the first win.

### Content QA

- [ ] Rights matrix signed; zero Amber/Red assets in the pack.
- [ ] Advisor signs the 3-maqam tuning table and every phrase.
- [ ] 15/18 masters exist per the Gate 2 decision; every phrase has metadata and `rights_id`.
- [ ] Signature phrases cover at least two registers (or two registers each, per decision C).
- [ ] Every lesson item has a matched contrast phrase; A/B loudness is even.
- [ ] Runtime `0.75×` passed listening QA on device speaker and headphones.
- [ ] Tuning within ±5 cents; −16 LUFS; −1 dBTP; noise floor < −60 dBFS.
- [ ] No famous-song quotations or uncleared samples.
- [ ] Every item points to an existing phrase; no repeats; balanced answers.
- [ ] Every explanation is ≤2 sentences and names an audible feature.
- [ ] Arabic copy reviewed for RTL, diacritics, and terminology consistency.
- [ ] Every shipped master appears in the usage map; zero orphans.
- [ ] Blind test: five target users distinguish Hijaz from Nahawand after one session.

### Engineering QA

- [ ] `./gradlew :app:assembleDebug` passes.
- [ ] `./gradlew :app:testDebugUnitTest` passes, including the extended pack validator.
- [ ] `./gradlew :app:lintDebug` has no new errors.
- [ ] `./gradlew :app:connectedDebugAndroidTest` passes for the lesson flow.
- [ ] Device QA: RTL, 320 dp width, TalkBack, airplane mode, audio focus/unplug.
- [ ] Pack ≤25 MB; cold content load is fast; no network dependency.
- [ ] Drone behavior matches the Gate 2 decision; no orphan drone files.

### Release QA

- [ ] `docs/CONTENT_LICENSES.md` updated with one row per master and all onboarding clips.
- [ ] `docs/phase0/maqam-content-audio-spec.md` amended for runtime slow, signature registers,
      and 2–4 s phrases.
- [ ] `AGENTS.md` reconciled with the final item count and master count.
- [ ] Store listing and in-app credits match the approved credit line.
- [ ] Pack version bumped and tagged; previous pack archived.

---

## 10. Timeline and RACI

Assumes rights triage and studio booking start immediately; full legal clearance may run longer.

| Week | Content | Audio | Engineering | Gate |
| --- | --- | --- | --- | --- |
| 1 | Rights triage, snapshot, 43→9 CSV | Brief + quotes | Playback decisions | Rights triage + inventory |
| 2 | Theory worksheet, onboarding copy | Studio booked | Schema v3 design | Theory + playback freeze |
| 3 | Maqam cards, item drafts | Record maqam + onboarding | Import/validator | Copy draft |
| 4 | Lesson/contrast assembly | Edit/master/QA | Loader + validator | Audio approved |
| 5 | Advisor review, fixes | Re-takes if needed | Tests + device QA | Content QA |
| 6 | Licenses, credits, release notes | Final masters | Release candidate | Launch |

**RACI**

| Role | Responsibility |
| --- | --- |
| Owner | Approve scope, budget, contracts; final release sign-off |
| Content Manager | Plan, inventory, copy, lessons, rights register, acceptance |
| Maqam advisor | Theory/tuning, phrase selection, Arabic copy review, final content sign-off |
| Audio producer | Record, edit, master, deliver stems and releases |
| Android engineer | Schema, loader, validator, playback integration, tests |
| Legal | Rights review, contract terms, credit wording |
| QA | Device, RTL, accessibility, offline verification, blind test |

---

## 11. Risks

| Risk | Likelihood | Impact | Mitigation |
| --- | --- | --- | --- |
| MaqamWorld rights are not cleared for app use | High | Blocks reuse | Rights gate; fall back to facts + original copy + commissioned audio |
| Site audio has performer/composition encumbrances | High | Blocks audio reuse | Do not reuse; commission original phrases |
| Advisor disagrees with site theory or regional practice | Medium | Rework | Freeze tuning before recording; advisor signs the worksheet |
| Signature-register decision is deferred | Medium | Wrong asset count | Decide A/B/C at Gate 2, before booking |
| A/B compares unlike phrases | High | Bad teaching | Matched contrast core; `contrast_phrase_id`; QA listen |
| Drone UI never ships | Medium | Orphan assets | Defer drone masters or add DroneToggle in the same release |
| Item count stays ambiguous | Medium | Rework lessons | Owner decision at Gate 2; update AGENTS |
| Phrases are too similar to distinguish | Medium | Bad lessons | Blind tests; contrast pairs; matched registers/instruments |
| Scope expands to 43 maqams | High | Slips launch | Freeze Pack 1 at 3; backlog everything else |
| Arabic terminology drifts | Medium | Confusing copy | Single glossary; advisor-approved terms |
| Studio/advisor delay | Medium | Slips launch | Book early; keep CC0 synth as dev-only fallback, never release |
| Pack exceeds size budget | Low | Rework | AAC mono, usage map, CI size check |

---

## 12. Decisions required before Gate 2

| Decision | Options | Recommendation |
| --- | --- | --- |
| Items per lesson | 4 (AGENTS) vs 5 (UI contract) | **5**, update AGENTS; 15 items |
| Drone in v1 | Ship DroneToggle + 3 drones vs defer drones | **Ship** if the toggle is in scope; otherwise defer and drop 3 files |
| Signature registers | 3 signatures across ≥2 registers (15) vs each in 2 registers (18) | **15** for launch; add registers in Pack 1b |
| Slow variants | Runtime `0.75×` vs separate masters | **Runtime**, unless advisor rejects after listening |
| Audio source | Reuse cleared site audio vs commission | **Commission**; site audio is long-form and encumbered |
| Onboarding audio | Commission 5 drills vs keep CC0 synth | **Commission** with the same producer; CC0 only as dev fallback |
| Library copy | Write 100–150-word cards now vs defer | **Defer** until the Library screen ships |

---

## 13. Immediate next actions

1. **Rights triage:** decide reuse vs reference-only for each asset class; open the matrix. Full
   legal clearance can run in parallel.
2. **Snapshot:** save the maqam/jins pages (EN + AR) with dates and URLs outside the APK.
3. **Inventory:** fill the 9 core rows; add aliases; note the 8 listed maqams without detail pages.
4. **Playback contract:** freeze item count, drone support, signature registers, and A/B contrast.
5. **Advisor kickoff:** approve the 3-maqam tuning table, phrase list, and contrast core.
6. **Audio:** send the brief; book two to three studio days for maqam masters + 5 onboarding drills.
7. **Copy:** draft ship-required onboarding and lesson copy; defer Library cards.
8. **Schema:** agree pack v3 and the import/validate scripts before the first audio delivery.
9. **Usage map:** remove or exclude the 7 unreferenced files; require zero orphans at release.
10. **Schedule:** set gate reviews for Weeks 1, 2, 4, and 6; do not record before Gate 2.

---

## Appendix A — Source field map

| App field | MaqamWorld source | Handling |
| --- | --- | --- |
| `name_ar`, `name_en` | Page title / index | Fact; verify Arabic edition |
| `lower_jins`, `upper_jins` | Maqam page ajnas | Fact; advisor confirms |
| `degrees_cents` | Notation + advisor | Manual worksheet, not OCR |
| `story_ar` | Page commentary | Facts only; write original; Library-only |
| `signature_ar` | Page commentary + advisor | Original one-sentence copy |
| Notation | Scale image | Redraw or omit; do not ship image |
| Audio examples | Page audio | Reference only unless fully cleared |
| Iqaa/forms/instruments | Separate sections | Future backlog, not v1 |
| Onboarding drills | Not on the site | Original exercises; current pack is the baseline |

## Appendix B — File and metadata templates

```
assets/content/
  maqams.json
  audio/
    onboarding/
      on-01-high-low.m4a
      on-02-up-down.m4a
      on-03-same-different.m4a
      on-04-long-short.m4a
      on-05-home-note.m4a
    rast/
      rast_scale_up_mid_070_qanun_v1.m4a
      rast_sig_mid_090_oud_v1.m4a
      rast_drone_mid_060_oud_v1.m4a
    hijaz/
    nahawand/
```

`phrase_register.csv`

```csv
phrase_id,maqam_id,type,instrument,register,bpm,duration_ms,audio_file,rights_id,sha256,loudness_lufs,performer,notes,status
hijaz_sig_mid_090_oud_v1,hijaz,signature,oud,mid,90,3200,hijaz/hijaz_sig_mid_090_oud_v1.m4a,R-2026-014,,−16.0,,,approved
```

`lesson_items.csv`

```csv
item_id,lesson_id,phrase_id,contrast_phrase_id,answer,explanation_ar,difficulty,status
hn_1,hijaz_nahawand,hijaz_sig_mid_090_oud_v1,nahawand_sig_mid_090_oud_v1,hijaz,"…",2,approved
```

`onboarding.csv`

```csv
step_id,prompt_ar,audio,options_ar,answer,explanation_ar,status
high_low,أي النغمتين أعلى؟,onboarding/on-01-high-low.m4a,"الأولى|الثانية",second,"…",approved
```

## Appendix C — Starter pack checklist (per maqam)

- [ ] Name, jins labels, signature sentence.
- [ ] Tuning row signed; tonic and cents fixed.
- [ ] Scale up, scale down, ornamented scale.
- [ ] Three lower-jins phrases, three upper-jins phrases.
- [ ] Three signature phrases, plus matched contrast signature.
- [ ] Two cadences.
- [ ] One loopable drone (if DroneToggle ships).
- [ ] Lesson items assigned where the maqam is an option.
- [ ] Every file has rights ID, checksum, duration, loudness.
- [ ] Every file appears in the usage map.
- [ ] Advisor listened to every file and signed the cue sheet.

## Appendix D — Proposed tuning table for the launch three

Starting values from the content spec; the advisor must confirm or replace them before recording.

| Maqam (tonic) | Degree cents (1–8) | Lower jins | Upper jins | Note |
| --- | --- | --- | --- | --- |
| Rast (C4) | 0, 200, 350, 500, 700, 900, 1050, 1200 | Rast | TBC by advisor | Neutral third and seventh |
| Hijaz (D4) | 0, 100, 400, 500, 700, 800, 1000, 1200 | Hijaz | Nahawand (spec example) | Augmented second between degrees 2–3 |
| Nahawand (C4) | 0, 200, 300, 500, 700, 800, 1000, 1200 | Nahawand | TBC by advisor | Familiar minor; upper jins unresolved |

Sikah and Huzam remain out of Pack 1 until the advisor resolves their tuning and upper ajnas.
