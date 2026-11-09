#!/usr/bin/env python3
"""Generate the placeholder content pack audio.

Every track is an original synthesised melody built from a maqam scale.
No third-party recordings are used, so the pack can ship under CC0.
Replace these files with properly licensed recordings when real content
is available (see docs/CONTENT_LICENSES.md).
"""

from __future__ import annotations

import math
import random
import struct
import subprocess
import tempfile
import wave
from pathlib import Path

SAMPLE_RATE = 44_100
ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "app" / "src" / "main" / "assets" / "content" / "audio"

# Semitone offsets for common maqam families (equal-tempered approximation).
MAQAMAT = {
    "alhijaz": [0, 1, 4, 5, 7, 8, 11],
    "alnahawand": [0, 2, 3, 5, 7, 8, 10],
    "alkurd": [0, 1, 3, 5, 7, 8, 10],
    "alsaba": [0, 1, 3, 4, 6, 8, 10],
    "albayati": [0, 1, 3, 5, 7, 8, 10],
    "alrast": [0, 2, 4, 5, 7, 9, 11],
    "alsika": [0, 1, 4, 5, 7, 8, 10],
    "alajam": [0, 2, 4, 5, 7, 9, 11],
    "alhuzam": [0, 1, 3, 4, 6, 8, 10],
    "albanjkah": [0, 2, 3, 6, 7, 9, 11],
}

TRACKS = [
    ("01-alhijaz", "alhijaz", 1),
    ("02-alnahawand", "alnahawand", 2),
    ("03-alkurd", "alkurd", 3),
    ("04-alsaba", "alsaba", 4),
    ("05-albayati", "albayati", 5),
    ("06-alrast", "alrast", 6),
    ("07-alsika", "alsika", 7),
    ("08-alajam", "alajam", 8),
    ("09-alhuzam", "alhuzam", 9),
    ("10-albanjkah", "albanjkah", 10),
]


def note_hz(root_hz: float, scale: list[int], degree: int) -> float:
    octave, index = divmod(degree, len(scale))
    return root_hz * (2 ** ((scale[index] + 12 * octave) / 12))


def add_tone(buffer: list[float], start: int, duration: int, freq: float, gain: float) -> None:
    attack = int(0.02 * SAMPLE_RATE)
    release = int(0.12 * SAMPLE_RATE)
    two_pi = 2.0 * math.pi
    phase = 0.0
    for i in range(duration):
        pos = start + i
        if pos >= len(buffer):
            break
        t = i / SAMPLE_RATE
        vibrato = 1.0 + 0.0035 * math.sin(two_pi * 5.2 * t)
        phase += two_pi * freq * vibrato / SAMPLE_RATE
        # Sine plus a soft second harmonic for a plucked-string timbre.
        sample = math.sin(phase) + 0.28 * math.sin(2.0 * phase) + 0.1 * math.sin(3.0 * phase)
        if i < attack:
            envelope = i / attack
        elif i > duration - release:
            envelope = max(0.0, (duration - i) / release)
        else:
            envelope = 1.0
        buffer[pos] += gain * envelope * sample


def make_melody(track_index: int, scale: list[int]) -> list[float]:
    rng = random.Random(1000 + track_index)
    seconds = 12
    total = seconds * SAMPLE_RATE
    buffer = [0.0] * total
    root_hz = 261.63 * (2 ** (track_index % 3) / 12)  # C4 with a small key lift

    step = int(0.34 * SAMPLE_RATE)
    contour = [0, 1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1, 0, 1,
               2, 3, 4, 5, 6, 7, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1,
               0, 2, 4, 6, 7, 6, 4, 2]
    for i, degree in enumerate(contour):
        if i and i % 5 == 0 and rng.random() < 0.35:
            continue  # occasional breath
        freq = note_hz(root_hz, scale, degree)
        duration = step if i % 4 else int(step * 1.45)
        add_tone(buffer, i * step, duration, freq, 0.30)

        if i % 4 == 0:
            add_tone(buffer, i * step, int(step * 3.2), root_hz / 2, 0.10)
        if i % 8 == 6:
            add_tone(buffer, i * step + step // 2, step, note_hz(root_hz, scale, degree + 2), 0.14)

    # Gentle fades so loops and transitions are click-free.
    fade = int(0.4 * SAMPLE_RATE)
    for i in range(fade):
        buffer[i] *= i / fade
        buffer[-1 - i] *= i / fade

    peak = max(abs(value) for value in buffer) or 1.0
    return [value / peak * 0.82 for value in buffer]


def write_wav(path: Path, samples: list[float]) -> None:
    with wave.open(str(path), "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(SAMPLE_RATE)
        frames = b"".join(struct.pack("<h", int(max(-1.0, min(1.0, s)) * 32767)) for s in samples)
        handle.writeframes(frames)


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        for track_index, (name, maqam, _level) in enumerate(TRACKS, start=1):
            samples = make_melody(track_index, MAQAMAT[maqam])
            wav_path = Path(tmp) / f"{name}.wav"
            write_wav(wav_path, samples)
            mp3_path = OUT_DIR / f"{name}.mp3"
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
