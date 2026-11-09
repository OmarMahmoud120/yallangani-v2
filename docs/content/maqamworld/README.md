# MaqamWorld inventory and 9-maqam mapping

**Retrieved:** 2026-11-10
**Status:** facts recorded; tuning, copy, and audio are still open (tasks #3–#7)

This folder holds **facts only**. No site prose, translation, notation image, or audio is copied
here. Rights basis: `docs/phase0/rights-triage.md` §3 (classes 1–2 Green, 3–10 reference-only).

| File | Purpose |
| --- | --- |
| `maqamworld_inventory.csv` | One row per core maqam: names, ajnas, page URLs, app color, tuning status |
| `aliases.csv` | Alternate names, shared detail pages, near-name traps, transliteration variants |
| `terminology_ar.csv` | Arabic glossary candidates; definitions are drafts for advisor approval |

Raw page snapshots are deliberately **not** kept in this repository. The source log below records
what was consulted and when. If an audit needs a private archive, store it outside the repo.

---

## 1. Source log

All pages fetched 2026-11-10.

| Page | URL |
| --- | --- |
| Maqam index (43 names) | `https://www.maqamworld.com/en/maqam.php` |
| Jins index (24 names) | `https://www.maqamworld.com/en/jins.php` |
| Rast EN / AR | `https://www.maqamworld.com/en/maqam/rast.php` · `/ar/maqam/rast.php` |
| Hijaz EN / AR | `https://www.maqamworld.com/en/maqam/hijaz.php` · `/ar/maqam/hijaz.php` |
| Nahawand EN / AR | `https://www.maqamworld.com/en/maqam/nahawand.php` · `/ar/maqam/nahawand.php` |
| Bayati EN / AR | `https://www.maqamworld.com/en/maqam/bayati.php` · `/ar/maqam/bayati.php` |
| Kurd EN / AR | `https://www.maqamworld.com/en/maqam/kurd.php` · `/ar/maqam/kurd.php` |
| Saba EN / AR | `https://www.maqamworld.com/en/maqam/saba.php` · `/ar/maqam/saba.php` |
| Ajam EN / AR | `https://www.maqamworld.com/en/maqam/ajam.php` · `/ar/maqam/ajam.php` |
| Sikah EN / AR | `https://www.maqamworld.com/en/maqam/sikah.php` · `/ar/maqam/sikah.php` |
| Huzam EN / AR | `https://www.maqamworld.com/en/maqam/huzam.php` · `/ar/maqam/huzam.php` |

Site footer: © 2001–2018 MaqamWorld. Treated as all rights reserved.

### Counts verified today

- **43** maqam names on the index.
- **35** unique detail pages. Eight names share another maqam's page:
  Farahfaza, Muhayyar, Rahat al-Arwah, Sazkar, Shadd Araban, Shahnaz, Suzidil, Yakah.
- **24** jins names, each with its own detail page. Jins pages are not inventoried yet; only the
  names used by the core nine appear in `maqamworld_inventory.csv`.

---

## 2. Core nine mapping

Pack 1 is frozen at the first three rows. Do not add a fourth maqam to Pack 1.

| App ID | Arabic (app) | Page | Pack | Gate |
| --- | --- | --- | --- | --- |
| `rast` | راست | `rast.php` | 1 | Commission |
| `hijaz` | حجاز | `hijaz.php` | 1 | Commission |
| `nahawand` | نهاوند | `nahawand.php` | 1 | Commission |
| `bayati` | بياتي | `bayati.php` | 2 | After Pack 1 |
| `kurd` | كرد | `kurd.php` | 2 | After Pack 1 |
| `saba` | صبا | `saba.php` | 2 | After Pack 1 |
| `ajam` | عجم | `ajam.php` | 3 | After Pack 2 |
| `sikah` | سيكاه | `sikah.php` | 3 | Tuning sign-off |
| `huzam` | هزام | `huzam.php` | 3 | Full theory sign-off |

App IDs match `maqamColor()` in `ui/theme/Theme.kt`, so a lesson can ship without a theme change.
`app_color_hex` in the CSV duplicates those values for authoring convenience; the theme remains the
runtime source of truth.

The seven legacy synth files use an `al-` prefix (`03-alkurd.mp3`, `04-alsaba.mp3`,
`05-albayati.mp3`, `07-alsika.mp3`, `08-alajam.mp3`, `09-alhuzam.mp3`, `10-albanjkah.mp3`). They map
to `kurd`, `saba`, `bayati`, `sikah`, `ajam`, `huzam`, and a Banjkah entry that is **not** in the
core nine. Per plan §13.9 these files are unreferenced and must be removed or excluded before
release; do not treat the filenames as app IDs.

---

## 3. What the inventory does and does not contain

Recorded because it is a fact on the page:

- Arabic and English names, including the page's diacritics, plus a plain app-facing spelling.
- Jins names and where they start, exactly as the page lists them (see caveat below).
- Stated tonic (`huzam`: E, or B when performed as Rahat al-Arwah). Pages that do not state one
  are marked `not stated on page` rather than guessed.
- Page URLs and access date.

Not recorded, on purpose:

- **Cents.** MaqamWorld shows notation, not cent values. Tuning belongs to task #3 and the advisor
  worksheet; `degrees_cents` stays empty until signed.
- **Prose, translations, notation images, audio.** Reference-only per the rights triage.
- **Example titles** from the pages. They are facts, but Pack 1 has no Library screen, so listing
  performers and works would invite unlicensed linking. Revisit if the Library ships.
- **Jins pages.** Pack 1 needs the three lower ajnas (Rast, Hijaz, Nahawand). Their page facts are
  enough for now; a full jins inventory waits for Pack 2.

### Caveat on jins order

The English and Arabic editions sometimes list ajnas in a different order (Rast and Bayati). The
CSV records both readings in `notes` and names the starting degree only where the page states it.
Do not treat list order as theory. Task #3 resolves the lower/upper jins for each maqam with the
advisor before any phrase is recorded.

---

## 4. Column and status vocabulary

`rights_status`:

| Value | Meaning |
| --- | --- |
| `green-facts` | Facts only; no expressive content copied. Cleared by `rights-triage.md` class 1. |
| `amber` | Would require a license; should not appear in this folder |

`aliases.csv` — `relation`:

| Value | Meaning |
| --- | --- |
| `alternate_name` | Same maqam under a second name on the same page |
| `distinct_maqam` | Different maqam sharing a page, or a near name with its own page. **Never merge.** |
| `transliteration` | Latin spelling variant; search aid only |

`advisor_status` in `terminology_ar.csv` is `draft` until the advisor approves the learner-facing
wording in task #4.

---

## 5. Refresh procedure

1. Fetch the EN and AR page for the maqam; update `accessed` and only the facts that changed.
2. Diff against the CSV. Never paste prose to explain a change; put the new fact in `notes`.
3. If a new name appears on the index, add it to `aliases.csv` with its relation before mapping it.
4. Re-check the index counts (43 / 35 / 8) when the site changes; if they move, update this README.
5. Run no build step: the app does not read this folder. It is an authoring source for tasks #3–#7.

---

## 6. Outputs this unblocks

| Task | Uses |
| --- | --- |
| #3 tuning/playback | `lower_jins`, `upper_jins`, `tonic`, `notes` |
| #4 Arabic copy | `name_ar_app`, `terminology_ar.csv` |
| #6 schema v3 | `app_id`, pack assignment, page URLs for traceability |
| #7 lessons | Option pairs drawn from Pack 1 only (Rast, Hijaz, Nahawand) |
