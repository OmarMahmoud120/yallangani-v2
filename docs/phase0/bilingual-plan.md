# Bilingual app, content pack, and site

Status: implemented, device-verified, and deployed to `yallanganee-73906702` 2026-11-10. The live
origin serves pack version 4 (`content_version 3a16a8b3bf64`) and `/en/`; the app streams tracks and
notation from it.

## Decision

Arabic stays the default and the product's first language. The app gains an explicit
Arabic/English preference in Settings and an English edition of the static site. English is authored
copy, not machine translation at runtime, and it lives in the same canonical files as the Arabic
content so there is one pipeline.

Scope:

- App UI strings: full English resource set (new `res/values-en/strings.xml`).
- Language switch: Settings radio group (العربية / English), persisted in DataStore, applied
  immediately without an activity restart.
- Content pack: English fields for every packed maqam, authored lesson, onboarding step, first
  win, and the development notice. Schema version 4.
- Site: `/en/` English home and privacy pages, cross-linked from the Arabic pages, with correct
  `lang`, `dir`, Open Graph locale, and `hreflang` alternates.
- Out of scope: additional languages, system-following mode, localized audio, translating track
  titles/artists (proper names stay as authored), RTL-in-English mixing.

## App language model

- `AppLanguage` enum (`ARABIC`, `ENGLISH`) in `model/`, pure Kotlin.
- Preference stored as an int in the existing `SettingsStore` DataStore (`language` key), default
  `ARABIC`. No AppCompat or new dependency.
- `RecognitionViewModel` exposes `language` and `setLanguage`; Settings shows the radio group next
  to Motion and writes through the view model.
- The Compose root wraps content with a localized context built from
  `context.createConfigurationContext(...)` (`ar` or `en`), and provides `LocalContext`,
  `LocalConfiguration`, `LocalResources`, and `LocalLayoutDirection` (RTL for Arabic, LTR for
  English). `MainActivity` stops forcing `LayoutDirection.Rtl`; it remains the Arabic default via
  the provider.
- No activity recreate, so Settings closes in place and every visible screen re-reads strings. The
  language preference is read from the same DataStore emission that gates onboarding, so cold
  starts apply it before content renders.
- Typography stays loaded from assets. `IBM Plex Sans Arabic` covers Latin UI text; the display
  family (`Aref Ruqaa`) is only used for Arabic-led headings — if English display headings render
  poorly or miss glyphs on device, English falls back to the UI family in `nightTypography()`.
- Maqam names stay Arabic inside English sentences only where they are proper nouns and are
  transliterated in the English fields; the answer cards use the English name.

## Content model (schema 4)

Additive English fields, all required by the validator for packs at schema 4:

| Model | New fields |
| --- | --- |
| `MaqamPack` | `notice_en` |
| `Maqam` | `name_en`, `jins_en`, `signature_en` |
| `RecognitionLesson` | `title_en` |
| `RecognitionItem` | `explanation_en` (optional override) |
| `OnboardingStep` | `prompt_en`, `explanation_en` |
| `OnboardingOption` | `label_en` |
| `FirstWin` | `prompt_en`, `explanation_en` |

Authoring source: `site/public/data/maqams.json`. Each tracked maqam gains `name_en` (short
display name, e.g. `Rast`, `‘Ajam ‘Ushayran`), `jins_en` (e.g. `Jins Rast`, `Compound maqam`), and
`signature_en` (editorial translation of the existing Arabic teaching line). The three authored
lessons gain `title_en` (`Hijaz or Nahawand?`, `Rast or Hijaz?`, `Rast or Nahawand?`). Onboarding
prompts, option labels, and explanations get English counterparts. Track metadata is not changed.

Generated exploration lessons get `title_en` built from English names, e.g.
`Bayati or Kurd?` for two and `Saba, ‘Ajam, or Sikah?` for three.

Fallback rules in the app stay defensive: an English accessor falls back to the Arabic field only
for a malformed cached pack; bundled and refreshed packs must pass validation first. Accessors:
`Maqam.name(language)`, `.jins(language)`, `.signature(language)`, `RecognitionLesson.title(language)`,
`MaqamPack.notice(language)`. `RecognitionQuestion.explanation` takes `AppLanguage` and prefers the
item's English override, then the maqam's English signature.

Validation (`MaqamPackValidator`, `PackUpdater`, `sync_app_pack.py`):

- every packed maqam has non-blank `name_en`, `jins_en`, `signature_en`;
- every lesson has non-blank `title_en`;
- every onboarding step/first win has non-blank English prompt and explanation, and every option a
  non-blank English label;
- schema constant moves to 4; a v3 cached or remote pack is rejected and the bundled v4 pack is the
  baseline until refresh succeeds.

## Site

- New `site/public/en/index.html` (`lang="en" dir="ltr"`) and `site/public/en/privacy.html`.
  Assets are referenced as `../assets/...`; internal anchors stay on the English page; the Play
  link is unchanged.
- Arabic pages gain an `English` link in the header and footer; English pages gain `العربية`.
- Metadata: English title/description, `og:locale en_US`, `hreflang` alternates on both editions,
  and `x-default` pointing at the Arabic home.
- English copy mirrors the current sections (hero, why cards, how steps, first three maqams,
  download, footer) and the full privacy policy; maqam cards use the authored English names,
  jins, and signatures.
- CSS is already mostly logical-property based; add only the small amount needed for the language
  link and LTR header, then verify both directions at 320 px and desktop.
- Firebase Hosting serves `/en/` as a directory index; no rewrite or redirect is needed. The
  hosting deploy stays gated on a separate explicit owner go-ahead.

## Files

App:

- `model/MaqamContent.kt`, new `model/AppLanguage.kt`
- `data/SettingsStore.kt`, `data/MaqamPackValidator.kt`, `data/PackUpdater.kt`
- `game/RecognitionViewModel.kt`, `game/RecognitionEngine.kt`
- `MainActivity.kt`, `ui/YallaApp.kt`, `ui/theme/Theme.kt`
- `ui/HomeScreen.kt`, `ui/LessonScreen.kt`, `ui/OnboardingScreen.kt`, `ui/ResultsScreen.kt`,
  `ui/SettingsScreen.kt`, `ui/gallery/GalleryScreen.kt`, `ui/components/LessonParts.kt`,
  `ui/components/Music.kt`
- `res/values/strings.xml` (add language labels), new `res/values-en/strings.xml`
- tests: `BundledMaqamPackTest`, `MaqamPackValidatorTest`, `RecognitionEngineTest`,
  `PackUpdaterTest`, `PackClipsTest` fixtures; add English coverage.

Content and tooling:

- `site/public/data/maqams.json`
- `tools/sync_app_pack.py` (require/carry English fields, generated English titles, schema 4)
- regenerated `app/src/main/assets/content/maqams.json` and `site/public/data/pack.json`

Site:

- `site/public/index.html`, `site/public/privacy.html`
- new `site/public/en/index.html`, `site/public/en/privacy.html`
- `site/public/assets/css/site.css` (only if LTR/language link needs it)

Docs and rules:

- `AGENTS.md` (bilingual content rule, RTL-first wording, pack schema, architecture note)
- `.cursor/rules/project.mdc` if it restates the string/RTL rules
- `docs/phase0/bilingual-plan.md` (this file), `docs/phase0/site-tracks-plan.md` schema note,
  `docs/RECREATION_PLAN.md`

## Verification

- `./gradlew :app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`,
  `:app:connectedDebugAndroidTest`.
- `python3 tools/sync_app_pack.py` produces identical Arabic content, complete English fields, a
  new `content_version`, and schema 4.
- Device, Arabic: home/lesson/feedback/results/settings unchanged; stored preference still Arabic
  after relaunch.
- Device, English: switch in Settings; verify every screen and dialog is English, layout direction
  is LTR, text does not clip at 320 dp, feedback still shows notation/credits/source, playback,
  slow mode, compare, drone, progress, reset, and TalkBack labels.
- Switch back to Arabic in the same session; verify RTL and copy restore without restart.
- Site: load `/` and `/en/` in a browser, follow every nav/footer/language link, check privacy
  pages, 320 px width, and that `pack.json` still parses with schema 4.
- Deploy is a separate step after review and an explicit go-ahead; then reinstall and confirm the
  v4 pack is adopted from the live origin.

## Risks

- English signature copy is newly authored editorial text; it should be reviewed for musical
  accuracy alongside the Arabic lines.
- `Aref Ruqaa` Latin rendering needs a device check; the fallback avoids shipping a new font.
- Schema 4 strands v3 clients on their bundled pack (Arabic only). That is acceptable: the old
  app cannot render English fields anyway.
