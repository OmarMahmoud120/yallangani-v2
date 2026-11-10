#!/usr/bin/env python3
"""Write the Android content pack and the public app pack from the site data.

Reads site/public/data/maqams.json, keeps approved Pack 1 maqams plus the
authored onboarding/lessons blocks, and writes:
  - app/src/main/assets/content/maqams.json (bundled baseline)
  - site/public/data/pack.json               (runtime updates)
  - site/public/data/app-audio/              (download origin for clips)

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
SITE_OUT = REPO / "site" / "public" / "data" / "pack.json"
SITE_AUDIO = REPO / "site" / "public" / "data" / "app-audio"
APP_CONTENT = REPO / "app" / "src" / "main" / "assets" / "content"
OUT = APP_CONTENT / "maqams.json"
AUDIO = APP_CONTENT / "audio"

MAQAM_KEYS = ("id", "name_ar", "color", "jins_ar", "signature_ar", "app_audio")


def die(message: str) -> None:
    sys.exit(f"sync_app_pack: {message}")


def require_audio(name: str, where: str) -> None:
    if not (AUDIO / name).is_file():
        die(f"missing bundled audio {name} ({where})")


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


def main() -> None:
    site = json.loads(SITE_PACK.read_text(encoding="utf-8"))

    maqams = []
    for item in site["maqams"]:
        if item.get("pack") != 1 or item.get("status") != "approved":
            continue
        missing = [key for key in MAQAM_KEYS if not item.get(key)]
        if missing:
            die(f"{item['id']} is missing authored fields: {', '.join(missing)}")
        require_audio(item["app_audio"], item["id"])
        maqam = {
            "id": item["id"],
            "name_ar": item["name_ar"],
            "color": item["color"],
            "jins_ar": item["jins_ar"],
            "signature_ar": item["signature_ar"],
            "audio": item["app_audio"],
            "status": item["status"],
        }
        if item.get("drone"):
            require_audio(item["drone"], item["id"])
            maqam["drone"] = item["drone"]
        maqams.append(maqam)

    if len(maqams) < 2:
        die(f"need at least two approved Pack 1 maqams, found {len(maqams)}")

    maqam_ids = {maqam["id"] for maqam in maqams}

    for key in ("onboarding", "lessons"):
        if key not in site:
            die(f"{key} block missing from {SITE_PACK.name}")

    onboarding = site["onboarding"]
    for step in onboarding["steps"]:
        require_audio(step["audio"], step["id"])
    require_audio(onboarding["first_win"]["audio"], "first_win")

    for lesson in site["lessons"]:
        unknown = [mid for mid in lesson["maqam_ids"] if mid not in maqam_ids]
        if unknown:
            die(f"{lesson['id']} offers non-approved maqams: {', '.join(unknown)}")
        for item in lesson["items"]:
            if item["answer"] not in lesson["maqam_ids"]:
                die(f"{lesson['id']}/{item['id']} answer outside the lesson options")
            if item.get("audio"):
                require_audio(item["audio"], f"{lesson['id']}/{item['id']}")

    clip_names = {maqam["audio"] for maqam in maqams}
    clip_names.update(maqam.get("drone") for maqam in maqams if maqam.get("drone"))
    clip_names.update(step["audio"] for step in onboarding["steps"])
    clip_names.add(onboarding["first_win"]["audio"])
    clip_names.update(
        item["audio"] for lesson in site["lessons"] for item in lesson["items"] if item.get("audio")
    )

    base = {"version": site["version"]}
    if site.get("notice"):
        base["notice"] = site["notice"]
    base.update(
        {
            "maqams": maqams,
            "onboarding": onboarding,
            "lessons": site["lessons"],
            "audio_sha256": {name: file_digest(AUDIO / name) for name in sorted(clip_names)},
        }
    )

    pack = {"version": site["version"], "content_version": pack_digest(base)}
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
        f"({len(maqams)} maqams, {len(clip_names)} clips, version {pack['content_version']})"
    )


if __name__ == "__main__":
    main()
