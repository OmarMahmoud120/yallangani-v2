# MaqamWorld → يلا نغني Content Acquisition Plan

**Owner:** Content Manager  
**Date:** 2026-11-10  
**Status:** Reviewed draft v2 — corrections applied after codebase and source audit  
**Source:** <https://www.maqamworld.com/> (EN + AR editions)  
**Companion docs:** `maqam-content-audio-spec.md`, `ui-ux-audit.md`, `CONTENT_LICENSES.md`

**Decisions locked 2026-11-10:** 5 items per lesson · DroneToggle ships · 15 masters per maqam · all Pack 1 audio commissioned.

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
| R5 | The app has **no drone control**; the UI audit plans one, but it is not implemented. Three drone masters would be orphaned. | **DroneToggle ships**; one drone master per maqam is included. |
| R6 | The spec contradicts itself on signature registers: §5 counts 3 signature phrases, §6 says every signature phrase exists in two registers. | **15 masters per maqam** selected; 3 signatures cover ≥2 registers. |
| R7 | `AGENTS.md` says 4 items per lesson; the UI contract says 5; the pack has 4. | **5 items per lesson** selected; AGENTS must be updated. |
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
- **15 phrase masters per maqam** (45 total).
- **3 recognition lessons** at 5 items each.
- **A shipped DroneToggle** with one loopable drone master per maqam.
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
Hijaz, and Nahawand. Ship a DroneToggle, runtime `0.75×` slow playback, and 5-item lessons. Then
expand to Pack 2 and Pack 3.

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
| 15 phrase masters per maqam | Long audio examples | Commission short, matched phrases |
| A/B contrast per item | Nothing usable | Record a matched contrast core per pair |
| 2–4 s phrases, 4–6 s scales | Songs and taqsims | New recordings with a strict brief |
| Two registers per signature phrase | Various | 3 signatures cover ≥2 registers (15-master set) |
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

Decisions are recorded in **`docs/phase0/rights-triage.md`** (Stage A complete, 2026-11-10). One row
per asset class with decision, basis, status, and evidence. Summary:

- **Green:** names, scale/tuning facts, structural terminology with original definitions, CC0
  synth placeholders, OFL fonts.
- **Amber, reference-only:** site prose, Arabic translations, notation, site audio, the MaqamWorld
  name, library suggestions, advisor contributions.
- **Red:** reusing compositions excerpted in site audio.
- **Green once signed:** commissioned masters, drones, onboarding drills.

Because nothing expressive is copied, Pack 1 needs no MaqamWorld license. The only open shipping
dependencies are our own commission and advisor agreements.

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

**Stage A outcome (2026-11-10):** every expressive class is reference-only, so the gate clears for
Pack 1 by copying nothing. Remaining blockers are the commission agreement and the advisor
agreement, both tracked in `docs/phase0/rights-triage.md` §5.

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
Rast Suznak. Eight index names share another maqam's detail page and are therefore not standalone
pages: Farahfaza, Muhayyar, Rahat al-Arwah, Sazkar, Shadd Araban, Shahnaz, Suzidil, Yakah. They are
related maqams, not aliases — see `docs/content/maqamworld/aliases.csv`. Do not let
the 43-page source pull v1 into a 43-maqam scope.

### 5.2 Inventory files

**Done 2026-11-10:** `docs/content/maqamworld/` contains `maqamworld_inventory.csv` (9 rows),
`aliases.csv`, `terminology_ar.csv`, and a README with the source log. Verified counts: 43 index
names, 35 detail pages, 24 jins names. Facts only; no page snapshots in the repo.

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

1. Log the URL and access date in the source log before using it. Do not keep page copies in the
   repo; the source log is the evidence (`docs/content/maqamworld/README.md`).
2. Record facts, not sentences. Never paste site prose into the pack.
3. One row per app ID; variants stay in `aliases.csv`, not in the shipping pack.
4. Mark every derived row with the triage status; facts are Green, expressive classes are Amber.
5. Cross-check the Arabic name against the Arabic page, not an English transliteration.

---

## 6. Target pack v1

### 6.1 Audio inventory (locked)

**Selected configuration:** 15 masters per maqam, drone included, with three signature phrases
covering at least two registers. Runtime `0.75×` supplies the slow variants. Pack total:
**45 maqam masters + 5 onboarding drills = 50 new assets**. The first win reuses the Hijaz
signature, so it adds no file.

| Item | Count |
| --- | --- |
| Scale up / down / ornamented | 9 (3 × 3) |
| Lower-jins phrases | 9 (3 × 3) |
| Upper-jins phrases | 9 (3 × 3) |
| Signature phrases | 9 (3 × 3, covering ≥2 registers) |
| Cadences | 6 (2 × 3) |
| Drones | 3 (1 × 3) |
| **Maqam masters** | **45** |
| Onboarding drills | 5 |
| First win | reuse Hijaz signature |
| **Total new assets** | **50** |

**15-master breakdown per maqam:** scale up, scale down, ornamented scale, 3 lower-jins,
3 upper-jins, 3 signature, 2 cadence, 1 drone.

Rejected options: 14/maqam without drones (DroneToggle now ships, so the drone master is required)
and 18/maqam with every signature in two registers (the 15-master set already covers two registers
across the signature group). If the advisor rejects runtime slow after listening, add **3 files per
maqam** (9 total) and re-run QA; do not add them by default.

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

The DroneToggle is in scope. The home-note drill should ship with a drone bed, and the lesson
player must expose a toggle that loops the tonic + fifth drone independently of the phrase.

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

### 6.5 Lesson contract (5 items)

Three lessons, one per starter pair:

| Lesson ID | Title (draft) | Options | Items |
| --- | --- | --- | --- |
| `hijaz_nahawand` | حجاز أم نهاوند؟ | hijaz, nahawand | 5 |
| `rast_hijaz` | راست أم حجاز؟ | rast, hijaz | 5 |
| `rast_nahawand` | راست أم نهاوند؟ | rast, nahawand | 5 |

**Decision:** **5 items per lesson**, 15 items total. `AGENTS.md` still says 4 and must be
updated. The UI already renders dots from `session.total`, so no layout change is needed. The
DroneToggle is not a lesson item and does not count toward the 5.

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
  `rights_id` and an existing file, no undocumented phrases.
- Drone phrases use `type: "drone"` and are looped by the player; the DroneToggle controls them
  independently of item audio.
- Verify `sha256` in CI, not at runtime.
- Keep audio in per-section subfolders (`audio/hijaz/`, `audio/onboarding/`); update the current
  flat path resolution.
- Do not ship the raw MaqamWorld snapshot in the APK.

### 6.8 Usage map and no-undocumented-asset rule

Every shipped master gets a usage-map row naming its consumer:

1. An onboarding step or the first win.
2. A lesson item (as `phrase_id` or `contrast_phrase_id`).
3. The DroneToggle.
4. A declared future lesson in the current pack (`reserved_for_lesson`).

“Zero orphans” means **zero undocumented files**, not zero unused files. A master that is neither
used by v1 nor declared for a planned Pack 1b lesson is archived outside the APK. This replaces the
current “ship 7 unreferenced files for future maqams” practice.

---

## 7. Production pipeline and gates

| Gate | Work | Output | Owner | Exit criteria |
| --- | --- | --- | --- | --- |
| 0. Rights | Rights matrix, contracts, credit line | Signed register | Legal + Content | Every class Green/Amber; no Red in pack |
| 1. Inventory | Snapshot pages, build 43→9 CSV, glossary | `maqamworld_inventory.csv` | Content | 9 core rows reviewed; aliases captured |
| 2. Theory + playback | Tuning worksheet, ajnas, 15-master coverage, A/B contrast | Signed theory record | Advisor + Product | 3 launch maqams approved; locked 5/15/DroneToggle contract confirmed |
| 3. Copy | Onboarding copy, signatures, item explanations | Arabic copy sheet | Content + Advisor | Fits learner voice; no copied prose |
| 4. Audio | Brief, record, edit, master, QA | Maqam masters + 5 drills + cue sheet | Audio producer | Tuning/loudness/noise pass; advisor approves |
| 5. Lessons | Assign phrases and contrasts, balance, validate | Pack JSON | Content | Items valid, no repeats, matched contrasts |
| 6. Integration | Schema v3, DroneToggle, loader, validator, tests | Build + tests | Android | Unit + instrumented tests pass |
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
- The five drills are commissioned with the same producer as the maqam phrases for a consistent
  timbre. The current CC0 synth drills remain dev-only fallbacks and must not be the shipped
  onboarding pack.

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
| No drone control in the current app | DroneToggle is now in scope; drone masters ship with the toggle |
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
- [ ] Home-note drill audio includes a clear drone bed; DroneToggle loops seamlessly; toggle state
      is visible and announced.

### Content QA

- [ ] Rights matrix signed; zero Amber/Red assets in the pack.
- [ ] Advisor signs the 3-maqam tuning table and every phrase.
- [ ] 45 maqam masters + 5 onboarding drills exist; every phrase has metadata and `rights_id`.
- [ ] Three signature phrases per maqam cover at least two registers.
- [ ] Every lesson item has a matched contrast phrase; A/B loudness is even.
- [ ] Runtime `0.75×` passed listening QA on device speaker and headphones.
- [ ] Tuning within ±5 cents; −16 LUFS; −1 dBTP; noise floor < −60 dBFS.
- [ ] No famous-song quotations or uncleared samples.
- [ ] Every item points to an existing phrase; no repeats; balanced answers.
- [ ] Every explanation is ≤2 sentences and names an audible feature.
- [ ] Arabic copy reviewed for RTL, diacritics, and terminology consistency.
- [ ] Every shipped master appears in the usage map; zero undocumented files.
- [ ] Blind test: five target users distinguish Hijaz from Nahawand after one session.

### Engineering QA

- [ ] `./gradlew :app:assembleDebug` passes.
- [ ] `./gradlew :app:testDebugUnitTest` passes, including the extended pack validator.
- [ ] `./gradlew :app:lintDebug` has no new errors.
- [ ] `./gradlew :app:connectedDebugAndroidTest` passes for the lesson flow.
- [ ] Device QA: RTL, 320 dp width, TalkBack, airplane mode, audio focus/unplug.
- [ ] Physical test device: check for the Avast overlay before debugging a missing package; use the
      Samsung TalkBack component for accessibility passes.
- [ ] Pack ≤25 MB; cold content load is fast; no network dependency.
- [ ] DroneToggle ships; drone loops seamlessly at fixed gain; drone file is in the usage map.

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
| 1 | Rights triage, snapshot, 43→9 CSV | Brief + quotes | DroneToggle + schema v3 design | Rights triage + inventory |
| 2 | Theory worksheet, onboarding copy | Studio booked | DroneToggle implementation | Theory + playback freeze |
| 3 | Maqam cards, item drafts | Record maqam + onboarding | Import/validator | Copy draft |
| 4 | Lesson/contrast assembly | Edit/master/QA | Loader + validator + drone QA | Audio approved |
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
| Advisor rejects the 15-master coverage | Medium | Late re-record | Advisor signs the 15-master inventory before booking |
| A/B compares unlike phrases | High | Bad teaching | Matched contrast core; `contrast_phrase_id`; QA listen |
| Drone loop clicks or masks the phrase | Medium | Poor lesson audio | Seamless loop, fixed gain, phone-speaker QA |
| 5-item lesson overflows at 320 dp | Low | Layout regression | 320 dp + TalkBack QA |
| Onboarding drills sound disconnected from maqam audio | Medium | Jarring first session | Same producer and instrument family |
| Phrases are too similar to distinguish | Medium | Bad lessons | Blind tests; contrast pairs; matched registers/instruments |
| Scope expands to 43 maqams | High | Slips launch | Freeze Pack 1 at 3; backlog everything else |
| Arabic terminology drifts | Medium | Confusing copy | Single glossary; advisor-approved terms |
| Studio/advisor delay | Medium | Slips launch | Book early; keep CC0 synth as dev-only fallback, never release |
| Pack exceeds size budget | Low | Rework | AAC mono, usage map, CI size check |

---

## 12. Decisions locked and defaults

**Locked 2026-11-10**

| Decision | Outcome |
| --- | --- |
| Items per lesson | **5**; 15 items total |
| Drone | **DroneToggle ships**; one loopable drone master per maqam (3 total) |
| Signature registers | **15 masters per maqam**; 3 signatures cover ≥2 registers |
| Audio source | **Commission all Pack 1 audio**; no site audio reuse; 45 maqam masters + 5 onboarding drills |
| Onboarding audio | **Included in the commission**; CC0 synth only as a dev fallback |

**Defaults pending advisor/owner**

| Decision | Default |
| --- | --- |
| Slow variants | Runtime `0.75×`; add 9 separate files only if the advisor rejects after listening |
| Library copy | Defer 100–150-word cards and listening suggestions until the Library screen ships |
| Lesson order | Hijaz/Nahawand first win, then Rast/Hijaz, then Rast/Nahawand |

---

## 13. Immediate next actions

1. **Rights triage:** done, 2026-11-10 — see `docs/phase0/rights-triage.md`. All expressive classes
   are reference-only; only our commission and advisor agreements remain open.
2. **Snapshot:** save the maqam/jins pages (EN + AR) with dates and URLs outside the APK. Record
   facts and URLs only; do not commit page copies.
3. **Inventory:** fill the 9 core rows; add aliases; note the 8 listed maqams without detail pages.
4. **Playback contract:** locked at 5 items, DroneToggle, 15 masters/maqam, commissioned audio.
   Confirm the matched A/B contrast design and hand the DroneToggle spec to engineering.
5. **Advisor kickoff:** approve the 3-maqam tuning table, 15-master inventory, phrase list, and
   contrast core.
6. **Audio:** send the brief; book two to three studio days for 45 maqam masters, 3 drones, and
   5 onboarding drills.
7. **Copy:** draft ship-required onboarding and lesson copy; defer Library cards.
8. **Schema:** agree pack v3 and the import/validate scripts before the first audio delivery.
9. **Usage map:** remove or exclude the 7 unreferenced files; require zero undocumented assets at
   release.
10. **DroneToggle:** implement the toggle and loop playback in parallel with content production.
11. **Schedule:** set gate reviews for Weeks 1, 2, 4, and 6; do not record before the advisor signs
    the tuning table.

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
- [ ] One loopable drone (DroneToggle ships).
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
