#!/usr/bin/env python3
"""Write the Android content pack and the public app pack from the site data.

Reads site/public/data/maqams.json and publishes every tracked maqam (Arabic
and English fields, notation, source URL, recordings) plus the authored
onboarding and lessons, with exploration lessons added for maqams no authored
lesson covers.
Writes:
  - app/src/main/assets/content/maqams.json (bundled baseline)
  - site/public/data/pack.json               (runtime updates)
  - site/public/data/app-audio/              (origin for bundled fallback clips)

The site JSON is the only place to edit content; never edit the packs by hand.
"""

import hashlib
import json
import shutil
import sys
from datetime import datetime, timezone
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
SITE_PACK = REPO / "site" / "public" / "data" / "maqams.json"
SITE_DATA = SITE_PACK.parent
SITE_OUT = SITE_DATA / "pack.json"
SITE_AUDIO = SITE_DATA / "app-audio"
APP_CONTENT = REPO / "app" / "src" / "main" / "assets" / "content"
OUT = APP_CONTENT / "maqams.json"
AUDIO = APP_CONTENT / "audio"

SCHEMA_VERSION = 4
MEDIA_BASE = "https://yallanganee-73906702.web.app/data/"
MAQAM_KEYS = (
    "id",
    "name_ar",
    "name_en",
    "color",
    "jins_ar",
    "jins_en",
    "signature_ar",
    "signature_en",
)
TRACK_KEYS = ("title", "artist", "audio")


def die(message: str) -> None:
    sys.exit(f"sync_app_pack: {message}")


def require_audio(name: str, where: str) -> None:
    if not (AUDIO / name).is_file():
        die(f"missing bundled audio {name} ({where})")


def require_site_file(name: str, where: str) -> None:
    if not (SITE_DATA / name).is_file():
        die(f"missing site asset {name} ({where})")


def file_digest(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            digest.update(chunk)
    return digest.hexdigest()


def pack_digest(pack: dict) -> str:
    canonical = json.dumps(pack, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    return hashlib.sha256(canonical.encode("utf-8")).hexdigest()[:12]


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def tracked_maqams(site: dict) -> list[dict]:
    return [item for item in site["maqams"] if item.get("tracks") and item.get("notation")]


def build_maqam(item: dict) -> dict:
    missing = [key for key in MAQAM_KEYS if not item.get(key)]
    if missing:
        die(f"{item['id']} is missing authored fields: {', '.join(missing)}")
    if item.get("status") != "approved":
        die(f"{item['id']} is not approved")
    require_site_file(item["notation"], item["id"])

    tracks = []
    for track in item["tracks"]:
        missing = [key for key in TRACK_KEYS if not track.get(key)]
        if missing:
            die(f"{item['id']} track is missing {', '.join(missing)}")
        require_site_file(track["audio"], f"{item['id']} track")
        entry = {
            "title": track["title"],
            "artist": track["artist"],
            "audio": track["audio"],
        }
        if track.get("credits"):
            entry["credits"] = list(track["credits"])
        tracks.append(entry)

    maqam = {
        "id": item["id"],
        "name_ar": item["name_ar"],
        "name_en": item["name_en"],
        "color": item["color"],
        "jins_ar": item["jins_ar"],
        "jins_en": item["jins_en"],
        "signature_ar": item["signature_ar"],
        "signature_en": item["signature_en"],
        "status": item["status"],
        "notation": item["notation"],
        "tracks": tracks,
    }
    if item.get("source_url"):
        maqam["source_url"] = item["source_url"]
    if item.get("app_audio"):
        require_audio(item["app_audio"], item["id"])
        maqam["audio"] = item["app_audio"]
    if item.get("drone"):
        require_audio(item["drone"], item["id"])
        maqam["drone"] = item["drone"]
    return maqam


def exploration_lessons(maqams: list[dict], covered: set[str]) -> list[dict]:
    remaining = [maqam for maqam in maqams if maqam["id"] not in covered]
    lessons = []
    for start in range(0, len(remaining), 3):
        group = remaining[start:start + 3]
        ids = [maqam["id"] for maqam in group]
        items = [
            {
                "id": f"{'_'.join(ids)}_{index + 1}",
                "answer": ids[index % len(ids)],
            }
            for index in range(len(ids) * 2)
        ]
        names_en = [maqam["name_en"] for maqam in group]
        if len(names_en) == 2:
            title_en = f"{names_en[0]} or {names_en[1]}?"
        else:
            title_en = f"{names_en[0]}, {names_en[1]}, or {names_en[2]}?"
        lessons.append(
            {
                "id": f"explore_{'_'.join(ids)}",
                "title_ar": " أم ".join(maqam["name_ar"] for maqam in group) + "؟",
                "title_en": title_en,
                "maqam_ids": ids,
                "items": items,
            }
        )
    return lessons


def main() -> None:
    site = json.loads(SITE_PACK.read_text(encoding="utf-8"))

    maqams = [build_maqam(item) for item in tracked_maqams(site)]
    if len(maqams) < 2:
        die(f"need at least two tracked maqams, found {len(maqams)}")

    maqam_ids = {maqam["id"] for maqam in maqams}

    for key in ("onboarding", "lessons"):
        if key not in site:
            die(f"{key} block missing from {SITE_PACK.name}")

    onboarding = site["onboarding"]
    for step in onboarding["steps"]:
        for key in ("prompt_ar", "prompt_en", "explanation_ar", "explanation_en"):
            if not step.get(key):
                die(f"onboarding step {step['id']} is missing {key}")
        for option in step["options"]:
            if not option.get("label_ar") or not option.get("label_en"):
                die(f"onboarding step {step['id']} option {option['id']} is missing a label")
        require_audio(step["audio"], step["id"])
    first_win = onboarding["first_win"]
    for key in ("prompt_ar", "prompt_en", "explanation_ar", "explanation_en"):
        if not first_win.get(key):
            die(f"first win is missing {key}")
    require_audio(first_win["audio"], "first_win")

    authored = site["lessons"]
    for lesson in authored:
        if not lesson.get("title_ar") or not lesson.get("title_en"):
            die(f"{lesson['id']} is missing a lesson title")
        unknown = [mid for mid in lesson["maqam_ids"] if mid not in maqam_ids]
        if unknown:
            die(f"{lesson['id']} offers maqams without tracks: {', '.join(unknown)}")
        for item in lesson["items"]:
            if item["answer"] not in lesson["maqam_ids"]:
                die(f"{lesson['id']}/{item['id']} answer outside the lesson options")
            if item.get("audio"):
                require_audio(item["audio"], f"{lesson['id']}/{item['id']}")

    covered = {mid for lesson in authored for mid in lesson["maqam_ids"]}
    lessons = authored + exploration_lessons(maqams, covered)
    covered = {mid for lesson in lessons for mid in lesson["maqam_ids"]}
    uncovered = sorted(maqam_ids - covered)
    if uncovered:
        die(f"no lesson covers: {', '.join(uncovered)}")

    clip_names = {maqam["audio"] for maqam in maqams if maqam.get("audio")}
    clip_names.update(maqam.get("drone") for maqam in maqams if maqam.get("drone"))
    clip_names.update(step["audio"] for step in onboarding["steps"])
    clip_names.add(onboarding["first_win"]["audio"])
    clip_names.update(
        item["audio"] for lesson in lessons for item in lesson["items"] if item.get("audio")
    )

    base = {"version": SCHEMA_VERSION, "media_base": MEDIA_BASE}
    if site.get("notice"):
        base["notice"] = site["notice"]
    if site.get("notice_en"):
        base["notice_en"] = site["notice_en"]
    base.update(
        {
            "maqams": maqams,
            "onboarding": onboarding,
            "lessons": lessons,
            "audio_sha256": {name: file_digest(AUDIO / name) for name in sorted(clip_names)},
        }
    )

    pack = {"version": SCHEMA_VERSION, "content_version": pack_digest(base)}
    previous = json.loads(SITE_OUT.read_text(encoding="utf-8")) if SITE_OUT.exists() else {}
    if previous.get("content_version") == pack["content_version"] and previous.get("published_at"):
        pack["published_at"] = previous["published_at"]
    else:
        pack["published_at"] = datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")
    pack.update({key: value for key, value in base.items() if key != "version"})

    for path in (OUT, SITE_OUT):
        write_json(path, pack)

    SITE_AUDIO.mkdir(parents=True, exist_ok=True)
    for stale in SITE_AUDIO.iterdir():
        if stale.is_file() and stale.name not in clip_names:
            stale.unlink()
    for name in sorted(clip_names):
        shutil.copyfile(AUDIO / name, SITE_AUDIO / name)

    print(
        f"Wrote {OUT.relative_to(REPO)}, {SITE_OUT.relative_to(REPO)} "
        f"({len(maqams)} maqams, {len(lessons)} lessons, "
        f"{sum(len(maqam['tracks']) for maqam in maqams)} tracks, "
        f"{len(clip_names)} clips, version {pack['content_version']})"
    )


if __name__ == "__main__":
    main()
