#!/usr/bin/env python3
"""Generate the development drone loop.

One sustained tonic + fifth bed (C3, C4, G4) with no third, so the learner hears
the tonic without the drone giving away the maqam. This is a CC0 placeholder for
the commissioned per-maqam drone masters; it is not a release asset. See
docs/CONTENT_LICENSES.md and docs/phase0/pack1-tuning-worksheet.md §7.
"""

from __future__ import annotations

import math
import struct
import subprocess
import tempfile
import wave
from pathlib import Path

SAMPLE_RATE = 44_100
ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "app" / "src" / "main" / "assets" / "content" / "audio"

DURATION_S = 30.0
CROSSFADE_S = 0.04
TONES = (
    (130.81, 0.16),  # C3 body
    (261.63, 0.22),  # C4 tonic
    (392.00, 0.12),  # G4 fifth
)


def build() -> list[float]:
    total = int(SAMPLE_RATE * DURATION_S)
    buffer = [0.0] * total
    for freq, gain in TONES:
        for i in range(total):
            t = i / SAMPLE_RATE
            swell = gain * (1.0 + 0.03 * math.sin(2 * math.pi * 0.1 * t))
            buffer[i] += swell * math.sin(2 * math.pi * freq * t)

    fade = int(SAMPLE_RATE * CROSSFADE_S)
    tail = buffer[-fade:]
    head = buffer[:fade]
    for i in range(fade):
        weight = i / (fade - 1)
        buffer[i] = head[i] * weight + tail[i] * (1.0 - weight)
    return buffer[:-fade]


def write_wav(path: Path, samples: list[float]) -> None:
    peak = max(abs(value) for value in samples) or 1.0
    scale = 0.70 / peak
    with wave.open(str(path), "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for value in samples:
            frames += struct.pack("<h", int(max(-1.0, min(1.0, value * scale)) * 32767))
        handle.writeframes(bytes(frames))


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        wav_path = Path(tmp) / "drone-c4.wav"
        write_wav(wav_path, build())
        mp3_path = OUT_DIR / "drone-c4.mp3"
        subprocess.run(
            [
                "ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
                "-i", str(wav_path),
                "-codec:a", "libmp3lame", "-b:a", "64k", "-ar", "44100",
                str(mp3_path),
            ],
            check=True,
        )
        print(f"{mp3_path.name}: {mp3_path.stat().st_size // 1024} KB")


if __name__ == "__main__":
    main()
