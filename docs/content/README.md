# Top 100 Arabic trending tracks — candidate list (retrieved 2026-11-09)

**Status: rights uncleared.** This is a market-research list for the 100-track pack, not a
license. No track here may be shipped, streamed, ripped, or stem-separated until the rights in
[CONTENT_LICENSES.md](../CONTENT_LICENSES.md) are documented.

| File | Rows | Purpose |
| --- | --- | --- |
| `top-100-arabic-trending-2026-11-09.csv` | 100 | Ranked snapshot of what is charting now (Egypt, Saudi Arabia, UAE, Morocco, pan-Arab) |
| `game-ready-arabic-titles.csv` | 100 | Subset of the same chart pool whose titles already pass the game's Arabic-title rules |

## Method

- **Pan-Arab chart:** Billboard Arabic Hot 100, week of 2026-10-31 (top 25 rows are the only ones
  the public page renders server-side).
- **Streaming charts:** Kworb mirrors of Spotify daily charts for SA, EG, AE, MA (2026-11-07) and
  Apple Music top songs for SA, EG, AE (retrieved 2026-11-09). Morocco Apple Music is not
  published on Kworb.
- **Arabic filter:** keep a song if its title or artist is Arabic-script, or if the artist is a
  known Arabic act. This yields 313 candidates from ~800 chart rows.
- **Dedupe:** same normalized title plus matching artist (with tolerant romanization matching),
  so `Awedak`/`اوعدك`, `Elalb Maat`/`القلب مات`, and `Akhouk Gaybha M El Sefr`/`اخوك جايبها م الصفر`
  merge into one row. Platform and session names (COLORS, Vodafone, Red Bull Sika) are not
  treated as separate artists.
- **Ranking:** best position on any tracked chart; ties broken by number of charts entered.
  Billboard is one of eight charts in the pool, so a regional #1 can outrank Billboard #10.
- Chart positions move daily. Refresh before starting each production batch.

## What the columns mean

- `title_on_chart` — title exactly as the chart published it, usually romanized.
- `arabic_title` — Arabic title, **only** when the chart metadata itself supplied one. Blank
  means "look it up on the official release"; do not guess a transliteration, because a wrong
  title becomes a wrong answer key in the game.
- `title_status` — `chart` or `missing`.
- `letters` — usable Arabic letters after normalization (`أإآٱ→ا`, `ى→ي`, `ة→ه`, diacritics
  removed), i.e. the number of slots the level needs.
- `game_fit` — `ready` (Arabic title, ≤16 letters), `long-arabic` (>16 letters, tray grows to a
  third row or shorten the title), `needs-arabic-title`, `invalid-characters` (digit in the
  official title; rename before use).
- `chart_evidence` — every chart and position the song appeared on in this snapshot.
- `rights_status` — always uncleared at this stage.

## Snapshot totals

- Top 100: 40 `ready`, 57 `needs-arabic-title`, 2 `long-arabic`, 1 `invalid-characters`; 48 artists.
- Full pool: 313 candidates, 128 `ready`, 18 `long-arabic`, 2 `invalid-characters`.
- 25 of the top 100 also appear on the Billboard Arabic Hot 100 (all 25 publicly rendered
  Billboard rows are inside the top 100).

## Rights: what "get the music without the singer voice" actually requires

Every commercial track in these files needs **two** separate clearances:

1. **Master license** from the label or distributor that owns the recording.
2. **Publishing/sync license** from the publisher(s) and songwriters for pairing the composition
   with the game and distributing audio copies.

Consequences for the instrumental + vocal plan:

- An official instrumental or karaoke version is a **different recording** with its own master
  rights. It is not free just because the vocals are missing.
- Running source separation on a commercial master produces a derivative work. It grants no
  rights and is not a shortcut.
- A commissioned cover gives you the new master **only if the contract assigns it**, but the
  composition still needs a sync/mechanical license from its publisher.
- Independent artists who self-distribute sometimes control both master and publishing, which
  makes them the cheapest realistic licensing conversations among these 100.

Recommended path, cheapest first:

1. **Commissioned covers** of the shortlisted songs (one studio, one singer, written assignment;
   sync negotiated per song). Produces matching instrumental and vocal masters.
2. **Licensed karaoke/instrumental catalog** that explicitly permits mobile-game use, then record
   your own vocals on top.
3. **CC0/original compositions** — no third-party rights, but they are not the trending hits. The
   ten synthesized maqam clips remain the shipping fallback until real licenses are signed.

Never: YouTube/Spotify rips, unlicensed karaoke tracks, or AI-separated stems of commercial
masters — including as placeholders.

## Suggested next steps

1. Pick a 10-track first batch from `game-ready-arabic-titles.csv` (short titles first: عفتني،
   مهموم، اوعدك، اريدك، يا ناس).
2. For each, confirm the official Arabic title and that it is a single, then open the licensing
   conversation (label/distributor for the master, publisher for sync).
3. Produce a 10–15 s hook starting on the recognizable phrase, instrumental and vocal as separate
   WAVs; run the audio checklist in [CONTENT_LICENSES.md](../CONTENT_LICENSES.md).
4. Record the rights in the track register before adding the level to `levels.json`.
5. Keep verifying titles for the 58 top-100 rows with no usable Arabic title (57 romanized, 1 with
   a digit); move them into the ready list as they are confirmed.

## Sources

- Billboard Arabic Hot 100 — <https://www.billboard.com/charts/arabic-hot-100/> (week of 2026-10-31)
- Spotify daily charts SA/EG/AE/MA — <https://kworb.net/spotify/country/sa_daily.html> (2026-11-07)
- Apple Music top songs SA/EG/AE — <https://kworb.net/charts/apple_s/sa.html> (retrieved 2026-11-09)
