#!/usr/bin/env python3
"""Rebuild site/public/data/maqams.json from the MaqamWorld crawl database.

The JSON is the single content source for the site and the app pack. Crawl fields
(name, pack, notes, notation, tracks) are regenerated from the database; authored
fields are carried over from the existing JSON and must be edited there.
"""

import json
import sqlite3
from datetime import date, datetime
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
PUBLIC = REPO / "site" / "public"
DATA = PUBLIC / "data"
DB = DATA / "maqams.db"
OUT = DATA / "maqams.json"

# Pack assignment from docs/content/maqamworld/README.md; None means unassigned.
PACKS = {
    "rast": 1,
    "hijaz": 1,
    "nahawand": 1,
    "bayati": 2,
    "kurd": 2,
    "saba": 2,
    "ajam": 3,
    "sikah": 3,
    "huzam": 3,
}

# App presentation order for the mapped maqams.
ORDER = {
    "rast": 0,
    "hijaz": 1,
    "nahawand": 2,
    "bayati": 3,
    "kurd": 4,
    "saba": 5,
    "ajam": 6,
    "sikah": 7,
    "huzam": 8,
}

# Authored fields: not present in the crawl, carried over from the existing JSON.
APP_KEYS = ("name_ar", "color", "jins_ar", "signature_ar", "status", "app_audio", "drone")
PACK_KEYS = ("notice", "onboarding", "lessons")


def existing(relative_path: str | None) -> str | None:
    """Return a path relative to site/public/data/, or None when the file is absent."""
    if not relative_path:
        return None
    if not (PUBLIC / relative_path).exists():
        return None
    return relative_path.removeprefix("data/")


def retrieved_date() -> str:
    report = DATA / "json" / "run_report.json"
    if report.exists():
        finished = json.loads(report.read_text(encoding="utf-8")).get("finished_at")
        if finished:
            return datetime.fromisoformat(finished.replace("Z", "+00:00")).date().isoformat()
    return date.today().isoformat()


def main() -> None:
    previous = json.loads(OUT.read_text(encoding="utf-8")) if OUT.exists() else {}
    authored = {item["id"]: item for item in previous.get("maqams", [])}

    con = sqlite3.connect(DB)
    con.row_factory = sqlite3.Row

    pages = con.execute(
        """
        SELECT id, slug, title, url
        FROM pages
        WHERE section = 'maqam' AND slug != 'maqam/_index'
        """
    ).fetchall()

    maqams = []
    for page in pages:
        maqam_id = page["slug"].split("/", 1)[1]

        notation = con.execute(
            """
            SELECT local_path FROM media
            WHERE page_id = ? AND url LIKE '%/note/maqam/%'
            LIMIT 1
            """,
            (page["id"],),
        ).fetchone()

        notes = [
            {"name": row["name"], "frequency": row["frequency"]}
            for row in con.execute(
                "SELECT name, frequency FROM notes WHERE page_id = ? ORDER BY idx",
                (page["id"],),
            )
        ]

        tracks = []
        for row in con.execute(
            """
            SELECT title, artist, credits_json, local_path
            FROM tracks WHERE page_id = ? ORDER BY idx
            """,
            (page["id"],),
        ):
            audio = existing(row["local_path"])
            if not audio:
                continue
            track = {"title": row["title"], "artist": row["artist"], "audio": audio}
            credits = json.loads(row["credits_json"]) if row["credits_json"] else []
            if credits:
                track["credits"] = credits
            tracks.append(track)

        entry = {
            "id": maqam_id,
            "name": page["title"],
            "pack": PACKS.get(maqam_id),
        }
        for key in APP_KEYS:
            value = authored.get(maqam_id, {}).get(key)
            if value is not None:
                entry[key] = value
        entry.update(
            {
                "source_url": page["url"],
                "notation": existing(notation["local_path"] if notation else None),
                "notes": notes,
                "tracks": tracks,
            }
        )
        maqams.append(entry)

    maqams.sort(key=lambda item: (item["pack"] or 99, ORDER.get(item["id"], 99), item["id"]))

    payload = {
        "version": 2,
        "source": {
            "name": "MaqamWorld",
            "url": "https://www.maqamworld.com/",
            "retrieved": retrieved_date(),
        },
        "maqams": maqams,
    }
    for key in PACK_KEYS:
        if key in previous:
            payload[key] = previous[key]

    OUT.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Wrote {OUT} with {len(maqams)} maqams")


if __name__ == "__main__":
    main()
