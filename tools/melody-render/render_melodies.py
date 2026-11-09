#!/usr/bin/env python3
"""Render MIDI melodies as uniform robotic clips for the content pack.

Each Standard MIDI File in the input folder becomes a mono 44.1 kHz clip
with a fixed square/saw timbre, hard quantised grid and no effects, so the
whole pack sounds consistent. Standard library only; MP3 export uses
ffmpeg when it is installed.

The input must be an original melody or one whose composition you have
written permission to use. Rendering does not grant any rights to a song.
"""

from __future__ import annotations

import argparse
import array
import bisect
import csv
import math
import re
import shutil
import subprocess
import sys
import tempfile
import wave
from pathlib import Path

SAMPLE_RATE = 44_100
DEFAULT_SECONDS = 30.0
DEFAULT_WAVE = "square"
DEFAULT_TEMPO_USEC = 500_000  # 120 BPM

HERE = Path(__file__).resolve().parent
DEFAULT_INPUT = HERE / "melodies"
DEFAULT_OUTPUT = HERE / "out"


def read_vlq(data: bytes, pos: int) -> tuple[int, int]:
    value = 0
    while True:
        if pos >= len(data):
            raise ValueError("truncated variable-length quantity")
        byte = data[pos]
        pos += 1
        value = (value << 7) | (byte & 0x7F)
        if byte < 0x80:
            return value, pos


def write_vlq(value: int) -> bytes:
    if value < 0:
        raise ValueError("cannot encode a negative delta")
    parts = [value & 0x7F]
    value >>= 7
    while value:
        parts.append(0x80 | (value & 0x7F))
        value >>= 7
    return bytes(reversed(parts))


def parse_track(track: bytes, notes: list, tempos: list) -> None:
    pos = 0
    tick = 0
    max_tick = 0
    status = 0
    open_notes: dict[tuple[int, int], list[tuple[int, int]]] = {}

    while pos < len(track):
        delta, pos = read_vlq(track, pos)
        tick += delta
        max_tick = max(max_tick, tick)
        if pos >= len(track):
            break

        byte = track[pos]
        if byte & 0x80:
            status = byte
            pos += 1
        elif status == 0:
            raise ValueError("running status without a preceding status byte")

        if status == 0xFF:
            if pos >= len(track):
                break
            meta_type = track[pos]
            pos += 1
            length, pos = read_vlq(track, pos)
            payload = track[pos:pos + length]
            pos += length
            if meta_type == 0x51 and length == 3:
                tempos.append((tick, (payload[0] << 16) | (payload[1] << 8) | payload[2]))
            elif meta_type == 0x2F:
                break
            continue

        if status in (0xF0, 0xF7):
            length, pos = read_vlq(track, pos)
            pos += length
            continue

        kind = status & 0xF0
        channel = status & 0x0F
        if kind in (0xC0, 0xD0):
            pos += 1
            continue
        if kind not in (0x80, 0x90, 0xA0, 0xB0, 0xE0):
            raise ValueError(f"unsupported status byte 0x{status:02x}")

        if pos + 2 > len(track):
            break
        data1, data2 = track[pos], track[pos + 1]
        pos += 2

        if kind == 0x90 and data2 > 0:
            open_notes.setdefault((channel, data1), []).append((tick, data2))
        elif kind in (0x80, 0x90):
            stack = open_notes.get((channel, data1))
            if stack:
                start, velocity = stack.pop(0)
                notes.append((start, data1, tick - start, velocity))

    for (channel, pitch), stack in open_notes.items():
        for start, velocity in stack:
            notes.append((start, pitch, max(0, max_tick - start), velocity))


def read_smf(path: Path) -> tuple[int, list, list]:
    data = path.read_bytes()
    if data[:4] != b"MThd":
        raise ValueError("not a Standard MIDI File")
    header_length = int.from_bytes(data[4:8], "big")
    if header_length < 6 or len(data) < 8 + header_length:
        raise ValueError("truncated MIDI header")

    track_count = int.from_bytes(data[10:12], "big")
    division = int.from_bytes(data[12:14], "big")
    if division & 0x8000:
        raise ValueError("SMPTE time division is not supported; export with PPQN")
    if division == 0:
        raise ValueError("invalid zero time division")

    notes: list = []
    tempos: list = []
    pos = 8 + header_length

    for _ in range(track_count):
        if pos + 8 > len(data):
            break
        if data[pos:pos + 4] != b"MTrk":
            chunk_length = int.from_bytes(data[pos + 4:pos + 8], "big")
            pos += 8 + chunk_length
            continue
        track_length = int.from_bytes(data[pos + 4:pos + 8], "big")
        parse_track(data[pos + 8:pos + 8 + track_length], notes, tempos)
        pos += 8 + track_length

    if not notes:
        raise ValueError("no notes found")
    return division, notes, tempos


def tick_converter(division: int, tempos: list):
    merged: dict[int, int] = {}
    for tick, usec in tempos:
        merged[tick] = usec
    merged.setdefault(0, DEFAULT_TEMPO_USEC)
    events = sorted(merged.items())

    starts: list[tuple[int, float, float]] = []
    seconds = 0.0
    for index, (tick, usec) in enumerate(events):
        if index:
            prev_tick, prev_usec = events[index - 1]
            seconds += (tick - prev_tick) * prev_usec / 1e6 / division
        starts.append((tick, seconds, usec / 1e6 / division))

    ticks = [start[0] for start in starts]

    def convert(tick: int) -> float:
        index = max(0, bisect.bisect_right(ticks, tick) - 1)
        base_tick, base_seconds, per_tick = starts[index]
        return base_seconds + (tick - base_tick) * per_tick

    return convert


def render(notes_seconds: list[tuple[float, float, int]], seconds: float, waveform: str) -> list[float]:
    total = int(seconds * SAMPLE_RATE)
    buffer = [0.0] * total
    attack = max(1, int(0.004 * SAMPLE_RATE))
    release = max(1, int(0.05 * SAMPLE_RATE))
    gain = 0.24
    two_pi = 2.0 * math.pi

    for start, duration, pitch in notes_seconds:
        if start >= seconds or duration <= 0.002:
            continue
        begin = int(start * SAMPLE_RATE)
        length = min(int(duration * SAMPLE_RATE), total - begin)
        if length <= 1:
            continue
        freq = 440.0 * (2.0 ** ((pitch - 69) / 12))
        step = freq / SAMPLE_RATE

        for i in range(length):
            phase = (begin + i) * step
            frac = phase - math.floor(phase)
            if waveform == "square":
                sample = 1.0 if frac < 0.5 else -1.0
            elif waveform == "saw":
                sample = 2.0 * frac - 1.0
            elif waveform == "triangle":
                sample = 4.0 * abs(frac - 0.5) - 1.0
            else:
                sample = math.sin(two_pi * frac)

            if i < attack:
                envelope = i / attack
            elif i > length - release:
                envelope = max(0.0, (length - i) / release)
            else:
                envelope = 1.0
            buffer[begin + i] += gain * envelope * sample

    mean = sum(buffer) / len(buffer)
    buffer = [value - mean for value in buffer]
    peak = max(abs(value) for value in buffer) or 1.0
    buffer = [value / peak * 0.85 for value in buffer]

    fade = int(0.03 * SAMPLE_RATE)
    for i in range(fade):
        factor = i / fade
        buffer[i] *= factor
        buffer[-1 - i] *= factor
    return buffer


def write_wav(path: Path, samples: list[float]) -> None:
    pcm = array.array("h", (int(max(-1.0, min(1.0, value)) * 32767) for value in samples))
    if sys.byteorder == "big":
        pcm.byteswap()
    with wave.open(str(path), "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(SAMPLE_RATE)
        handle.writeframes(pcm.tobytes())


def slugify(stem: str, index: int) -> str:
    slug = re.sub(r"[^a-z0-9]+", "-", stem.lower()).strip("-")
    return slug or f"track-{index:02d}"


def write_demo_midi(path: Path) -> None:
    """Original hijaz phrase in D, 100 BPM, 16 bars — no third-party melody."""
    division = 480
    tempo_usec = 600_000  # 100 BPM
    melody = [
        (62, 0, 1), (63, 1, 0.5), (66, 1.5, 0.5), (67, 2, 1), (69, 3, 1),
        (70, 4, 0.5), (69, 4.5, 0.5), (67, 5, 1), (66, 6, 1), (64, 7, 1),
        (62, 8, 1.5), (66, 9.5, 0.5), (69, 10, 1), (70, 11, 1),
        (69, 12, 0.5), (67, 12.5, 1.5), (66, 14, 1), (62, 15, 1),
        (66, 16, 1), (67, 17, 1), (69, 18, 1), (70, 19, 1),
        (72, 20, 1.5), (70, 21.5, 0.5), (69, 22, 1), (67, 23, 1),
        (66, 24, 1), (69, 25, 1), (70, 26, 2),
        (69, 28, 1), (67, 29, 1), (66, 30, 1), (64, 31, 1),
        (62, 32, 2), (69, 36, 1), (70, 37, 1), (72, 38, 1), (74, 39, 1),
        (72, 40, 1), (70, 41, 1), (69, 42, 1), (67, 43, 1),
        (66, 44, 1), (67, 45, 1), (69, 46, 1), (70, 47, 1),
        (69, 48, 2), (67, 50, 1), (66, 51, 1),
        (62, 52, 3), (63, 56, 1), (66, 57, 1), (67, 58, 1), (62, 59, 2),
    ]

    events = []
    for pitch, start, duration in melody:
        events.append((int(start * division), 1, pitch))
        events.append((int((start + duration) * division), 0, pitch))
    events.sort(key=lambda item: (item[0], item[1]))

    track = bytearray(b"\x00\xff\x51\x03" + tempo_usec.to_bytes(3, "big"))
    last_tick = 0
    for tick, is_on, pitch in events:
        track += write_vlq(tick - last_tick)
        track += bytes([0x90 if is_on else 0x80, pitch, 96 if is_on else 0])
        last_tick = tick
    track += b"\x00\xff\x2f\x00"

    header = (
        b"MThd" + (6).to_bytes(4, "big") + (0).to_bytes(2, "big")
        + (1).to_bytes(2, "big") + division.to_bytes(2, "big")
    )
    path.write_bytes(header + b"MTrk" + len(track).to_bytes(4, "big") + bytes(track))


def initial_bpm(tempos: list) -> float:
    if not tempos:
        return 60_000_000 / DEFAULT_TEMPO_USEC
    tempo = sorted(tempos)[0][1]
    return 60_000_000 / tempo


def export_mp3(wav_path: Path) -> Path | None:
    if not shutil.which("ffmpeg"):
        return None
    mp3_path = wav_path.with_suffix(".mp3")
    subprocess.run(
        [
            "ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
            "-i", str(wav_path),
            "-codec:a", "libmp3lame", "-b:a", "128k", "-ar", "44100",
            str(mp3_path),
        ],
        check=True,
    )
    return mp3_path


def render_file(
    midi_path: Path,
    out_dir: Path,
    seconds: float,
    waveform: str,
    transpose: int,
    mp3: bool,
) -> dict:
    division, raw_notes, tempos = read_smf(midi_path)
    convert = tick_converter(division, tempos)
    notes_seconds = []
    for start_tick, pitch, duration_ticks, _velocity in raw_notes:
        start = convert(start_tick)
        end = convert(start_tick + duration_ticks)
        notes_seconds.append((start, end - start, pitch + transpose))
    notes_seconds.sort()

    samples = render(notes_seconds, seconds, waveform)
    wav_path = out_dir / f"{midi_path.stem}.wav"
    write_wav(wav_path, samples)

    if mp3:
        export_mp3(wav_path)

    used = sum(1 for start, duration, _pitch in notes_seconds if start < seconds)
    return {
        "source": midi_path.name,
        "output": wav_path.name,
        "notes": used,
        "duration_s": f"{len(samples) / SAMPLE_RATE:.1f}",
        "bpm": f"{initial_bpm(tempos):.0f}",
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Render MIDI hooks as uniform robotic clips.")
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT, help="folder of .mid/.midi files")
    parser.add_argument("--out", type=Path, default=DEFAULT_OUTPUT, help="output folder")
    parser.add_argument("--seconds", type=float, default=DEFAULT_SECONDS, help="clip length (default 30)")
    parser.add_argument("--wave", choices=("square", "saw", "triangle", "sine"), default=DEFAULT_WAVE)
    parser.add_argument("--transpose", type=int, default=0, help="semitones, applied before rendering")
    parser.add_argument("--no-mp3", action="store_true", help="keep WAV only")
    parser.add_argument("--demo", action="store_true", help="render the built-in original demo melody")
    args = parser.parse_args()

    if args.seconds <= 0:
        parser.error("--seconds must be positive")

    args.out.mkdir(parents=True, exist_ok=True)
    mp3 = not args.no_mp3
    if mp3 and not shutil.which("ffmpeg"):
        print("ffmpeg not found; writing WAV only", file=sys.stderr)
        mp3 = False

    if args.demo:
        midi_path = args.out / "demo-melody.mid"
        write_demo_midi(midi_path)
        sources = [midi_path]
    else:
        sources = sorted(
            path for path in args.input.glob("*")
            if path.suffix.lower() in (".mid", ".midi")
        )
        if not sources:
            print(f"no .mid/.midi files in {args.input}", file=sys.stderr)
            return 1

    manifest = []
    failures = 0
    for index, midi_path in enumerate(sources, start=1):
        stem = slugify(midi_path.stem, index)
        staged = args.out / f"{stem}.mid"
        created = midi_path != staged
        if created:
            staged.write_bytes(midi_path.read_bytes())
        try:
            row = render_file(staged, args.out, args.seconds, args.wave, args.transpose, mp3)
        except (ValueError, OSError) as error:
            print(f"{midi_path.name}: {error}", file=sys.stderr)
            failures += 1
            if created:
                staged.unlink(missing_ok=True)
            continue
        if created:
            staged.unlink()
        manifest.append(row)
        size = (args.out / row["output"]).stat().st_size // 1024
        print(f"{row['output']}: {row['notes']} notes, {row['duration_s']}s, {size} KB")

    with (args.out / "manifest.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=["source", "output", "notes", "duration_s", "bpm"])
        writer.writeheader()
        writer.writerows(manifest)

    print(f"wrote {len(manifest)} clip(s) to {args.out}")
    if failures:
        print(f"{failures} file(s) failed", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
