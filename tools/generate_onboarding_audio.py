#!/usr/bin/env python3
"""Generate the onboarding listening exercises.

Five short, original synthesised drills (CC0) that teach the vocabulary the
maqam lessons depend on: high/low, ascending/descending, same/different,
long/short, and the home note over a drone. Like the maqam phrases, these are
placeholders until a licensed recording pack exists (see
docs/CONTENT_LICENSES.md).
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

NOTES = {
    "B3": 246.94,
    "C4": 261.63,
    "D4": 293.66,
    "E4": 329.63,
    "G4": 392.00,
}


def add_tone(
    buffer: list[float],
    start: float,
    duration: float,
    freq: float,
    gain: float = 0.32,
) -> None:
    start_index = int(start * SAMPLE_RATE)
    length = int(duration * SAMPLE_RATE)
    attack = int(0.02 * SAMPLE_RATE)
    release = int(min(0.14 * SAMPLE_RATE, length * 0.4))
    two_pi = 2.0 * math.pi
    phase = 0.0
    for i in range(length):
        pos = start_index + i
        if pos >= len(buffer):
            break
        t = i / SAMPLE_RATE
        vibrato = 1.0 + 0.0035 * math.sin(two_pi * 5.2 * t)
        phase += two_pi * freq * vibrato / SAMPLE_RATE
        sample = math.sin(phase) + 0.25 * math.sin(2.0 * phase) + 0.08 * math.sin(3.0 * phase)
        if i < attack:
            envelope = i / attack
        elif i > length - release:
            envelope = max(0.0, (length - i) / release)
        else:
            envelope = 1.0
        buffer[pos] += gain * envelope * sample


def add_drone(buffer: list[float], start: float, duration: float, freq: float) -> None:
    start_index = int(start * SAMPLE_RATE)
    length = int(duration * SAMPLE_RATE)
    fade = int(0.25 * SAMPLE_RATE)
    two_pi = 2.0 * math.pi
    for i in range(length):
        pos = start_index + i
        if pos >= len(buffer):
            break
        envelope = min(1.0, i / fade, (length - i) / fade)
        sample = math.sin(two_pi * freq * i / SAMPLE_RATE) + 0.2 * math.sin(
            2 * two_pi * freq * i / SAMPLE_RATE
        )
        buffer[pos] += 0.10 * envelope * sample


def silence(seconds: float) -> list[float]:
    return [0.0] * int(seconds * SAMPLE_RATE)


def high_low() -> list[float]:
    buffer = silence(2.2)
    add_tone(buffer, 0.0, 0.85, NOTES["C4"])
    add_tone(buffer, 1.15, 0.85, NOTES["G4"])
    return buffer


def up_down() -> list[float]:
    buffer = silence(3.1)
    for i, name in enumerate(["C4", "D4", "E4", "G4"]):
        add_tone(buffer, i * 0.34, 0.30, NOTES[name])
    for i, name in enumerate(["G4", "E4", "D4", "C4"]):
        add_tone(buffer, 1.75 + i * 0.34, 0.30, NOTES[name])
    return buffer


def same_different() -> list[float]:
    buffer = silence(2.1)
    add_tone(buffer, 0.0, 0.82, NOTES["D4"])
    add_tone(buffer, 1.10, 0.82, NOTES["E4"])
    return buffer


def long_short() -> list[float]:
    buffer = silence(2.7)
    add_tone(buffer, 0.0, 1.55, NOTES["C4"])
    add_tone(buffer, 1.95, 0.35, NOTES["E4"])
    return buffer


def home_note() -> list[float]:
    buffer = silence(6.2)
    add_drone(buffer, 0.0, 6.0, NOTES["C4"])
    for i, name in enumerate(["E4", "D4", "B3"]):
        add_tone(buffer, 0.6 + i * 0.42, 0.40, NOTES[name])
    for i, name in enumerate(["E4", "D4", "C4"]):
        add_tone(buffer, 3.4 + i * 0.42, 0.40, NOTES[name])
    return buffer


CLIPS = [
    ("on-01-high-low", high_low),
    ("on-02-up-down", up_down),
    ("on-03-same-different", same_different),
    ("on-04-long-short", long_short),
    ("on-05-home-note", home_note),
]


def write_wav(path: Path, samples: list[float]) -> None:
    peak = max(abs(value) for value in samples) or 1.0
    scale = 0.82 / peak
    fade = int(0.05 * SAMPLE_RATE)
    with wave.open(str(path), "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for i, value in enumerate(samples):
            if i < fade:
                value *= i / fade
            elif i > len(samples) - fade:
                value *= max(0.0, (len(samples) - i) / fade)
            frames += struct.pack("<h", int(max(-1.0, min(1.0, value * scale)) * 32767))
        handle.writeframes(bytes(frames))


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        for name, build in CLIPS:
            wav_path = Path(tmp) / f"{name}.wav"
            write_wav(wav_path, build())
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
