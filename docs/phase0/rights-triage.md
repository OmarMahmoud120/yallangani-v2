# Rights triage — MaqamWorld → يلا نغني

**Date:** 2026-11-10
**Owner:** content manager
**Status:** Stage A complete (decisions below). Stage B open (commission + advisor paperwork).
**Companion documents:** `docs/phase0/content-acquisition-plan.md` §4, `docs/CONTENT_LICENSES.md`

> **Site-only note (2026-11-10):** The owner reports approval to publish the MaqamWorld crawl on
> the public website under `site/public/data/`. That exception covers the site only; the app/APK rules in
> §3 and §6 are unchanged, and the written approval still needs to be filed. See
> `docs/CONTENT_LICENSES.md`, "Site data".

---

## 1. Scope

MaqamWorld (`https://www.maqamworld.com/`, © 2001–2018, all rights reserved) is a **theory and
terminology source**. Pack 1 takes facts and structure from it and nothing expressive.

| We take | We do not take |
| --- | --- |
| Maqam and jins names, Arabic spellings, aliases | Page prose (EN or AR) |
| Tuning values in cents, scale degrees, ajnas membership | Arabic translations of that prose |
| Structural concepts: jins, ghammaz, sayr, qafla, registers | Notation images or PDFs |
| Which works are associated with a maqam (facts) | Site audio recordings |
| Page URLs and access dates for our own records | The compositions those recordings perform |

**Consequence:** no MaqamWorld expressive asset enters the APK, so Pack 1 needs **no license from
MaqamWorld**. The remaining rights work is our own commissioning and advisor paperwork.

---

## 2. Status legend

| Status | Meaning | May ship? |
| --- | --- | --- |
| **Green** | Cleared or never needed; evidence or rationale on file | Yes |
| **Amber** | Reference only; usable to inform original work, not to copy | Yes, if nothing copied |
| **Red** | Must not be used in any form | No |

A class marked Amber is safe only while the derived work is independently written or recorded. If
any sentence, image, or audio sample is reproduced, the class becomes Red until a signed license
exists.

---

## 3. Asset-class decisions (Stage A)

| # | Asset class | Decision | Basis | What ships | Evidence required | Status |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Maqam names, jins names, aliases, scale facts, cents | Take facts only | Names and measurements are facts, not protected expression; the selection/arrangement of a page is | `id`, `name_ar`, `jins_ar`, tuning table, aliases | Source log (URL + access date + fact extracted) kept in `docs/`; no page copies | **Green** |
| 2 | Structural terminology (jins, ghammaz, sayr, qafla, register names) | Use the terms, write our own definitions | Individual terms are not owned; definition sentences are | Short original definitions in `strings.xml` / pack JSON | Reviewer check that no definition matches a site sentence | **Green** (conditional on original wording) |
| 3 | MaqamWorld editorial prose (EN) | Reference only | © All rights reserved; no license on file | Original copy from task #4 | None (nothing copied); source log only | **Amber** |
| 4 | Arabic translations of site pages | Reference only | Translation is a derivative work of the prose | Our own terminology glossary, written in-house | None; translator confirms independent drafting | **Amber** |
| 5 | Notation images | Do not use | © All rights reserved | No notation in Pack 1; redraw in Compose/SVG only if a later screen needs it | Redraw must be original, not traced | **Amber** |
| 6 | Site audio recordings | Reference only (locked) | Performance + master rights; not licensable for us today | Commissioned masters only | None; no site file ever enters the repo | **Amber** |
| 7 | Compositions excerpted in site audio | Do not excerpt | Sync/mechanical rights per work; many are in-copyright | Original phrases composed for the app | Composer assignment inside the commission | **Red** to reuse |
| 8 | Library listening suggestions (named works/performers) | Defer to Library; text-only facts if used | Naming a work as an example is factual; linking or hosting it is not | Pack 1 ships no Library cards | Legal read before any performer/work name appears in-app | **Amber** |
| 9 | "MaqamWorld" name and logo | Default omit; no logo | Trademark and attribution expectations | Nothing in Pack 1 | Approved credit wording only if the name is used | **Amber** |
| 10 | Advisor contributions and review | Contract before credit | Contributor holds copyright until assigned | Tuning table and phrase review | Signed contributor agreement + agreed credit | **Amber** |
| 11 | Commissioned masters, drones, onboarding drills | Commission with full assignment | Our own recordings once signed | All 50 Pack 1 assets | Signed commission + performer release (see §5) | **Green** once signed |
| 12 | Existing CC0 synth placeholders | Keep as dev fallback | Generated in-repo, CC0 | Debug builds only | Already documented in `CONTENT_LICENSES.md` | **Green** |
| 13 | Bundled fonts | Keep | OFL 1.1, license texts already shipped | IBM Plex Sans Arabic, Aref Ruqaa | Existing OFL files | **Green** |

### Classes that are already settled

- **1, 2, 12, 13** need no external permission. Class 2 stays Green only if every definition is
  written from scratch; a reviewer compares our copy against the site before release.
- **3–10** are reference-only. The safe path is to copy nothing, which removes the need to negotiate.
- **11** is the only class that can still block shipping, and it is cleared by our own contracts.

---

## 4. What this unblocks

| Task | Effect |
| --- | --- |
| #2 inventory and 9-maqam mapping | Can proceed now; record facts + source log, no page snapshots in git |
| #4 Arabic copy | Must be independently written; no translation of site sentences |
| #5 commissioned audio | Only source of ship audio; contract terms in §5 |
| #7 lessons | Phrases are original; no transcription of named works |
| #9 release | Confirm zero MaqamWorld-derived files in APK; add credit only if approved |

The shipping gate is now: **no MaqamWorld file in the APK** plus **signed commission paperwork**.
No reply from MaqamWorld is required for Pack 1.

---

## 5. Stage B evidence checklist (open)

| Evidence | Why it matters | Owner | State |
| --- | --- | --- | --- |
| Commission agreement: assignment of copyright in recordings and compositions, worldwide, perpetual, all digital/app distribution, right to edit/shorten/loop/normalize/transcode | Covers all 50 Pack 1 assets | content manager | **Open — blocks #5** |
| Performer release for every musician and vocalist | Prevents later claims from session players | content manager | **Open — blocks #5** |
| Composer warranty that phrases are original and do not quote a named work | Protects against sync claims | content manager | **Open — blocks #5** |
| No Content ID / no collecting-society registration of the delivered masters | Prevents false claims on our own uploads | content manager | **Open — blocks #5** |
| Advisor contributor agreement + credit wording | Allows using the advisor's name and review | content manager | **Open — blocks #9 credit** |
| MaqamWorld credit line (only if the name is used) | Trademark-safe attribution | content manager | **Conditional — default is to omit** |
| Source log: URL, access date, fact extracted | Shows facts were used, not prose | content manager | **Open — starts with #2** |

Store signed documents outside the APK. Commit only the register row and, if needed, a redacted
reference. Never commit page snapshots, notation, audio, or contracts to this repository.

---

## 6. Red lines

1. No page snapshots, prose, notation images, audio, or PDFs in the repo or the APK.
2. No transcription of a site recording; no "melody inspired by" a named work.
3. No commercial recordings, YouTube rips, or "no copyright" compilations, even as placeholders.
4. No MaqamWorld logo, and no implication of endorsement or partnership.
5. No performer, composer, or ensemble named in-app without a factual basis and a rights check.
6. No advisor name or quote before a signed contributor agreement.

---

## 7. Outbound request (optional, only if we later want prose or the name)

> Hello,
>
> I maintain يلا نغني, an Arabic maqam ear-training app for Android. We are building original
> lessons and recordings and would like to cite MaqamWorld as a terminology reference.
>
> Could you confirm: (1) who owns the editorial content on the site; (2) whether you would license
> any prose or notation images for use in a mobile app, and on what terms; (3) whether you are
> willing to be credited as a terminology reference, and the exact credit line you prefer.
>
> To be clear, we are not requesting the site's audio recordings. Thank you.

Record the reply verbatim with its date. A "no" changes nothing: the default plan already copies
nothing.

---

## 8. Sign-off

| Item | Approver | Date | State |
| --- | --- | --- | --- |
| Stage A decisions (§3) | content manager | 2026-11-10 | Approved |
| Original-copy check (classes 2–4) | content manager + reviewer | — | Pending before #7 |
| Commission paperwork (class 11) | content manager | — | Pending before #5 |
| Credit wording, if any (class 9) | content manager | — | Pending before #9 |
