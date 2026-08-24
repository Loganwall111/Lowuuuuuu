#!/usr/bin/env python3
"""Generate the soft gradient sprite textures used by the storm halo passes.

Writes plain RGBA PNGs (no external deps):
- halo_gradient.png : radial falloff blob, the volumetric glow sprite
- halo_band.png     : soft horizontal band with vertical gaussian falloff,
                      used for the layered phase-6 halo bands and horizon glow
"""
from __future__ import annotations

import math
import struct
import zlib
from pathlib import Path

OUT = Path("src/main/resources/assets/devouringstorms/textures/misc")


def write_png(path: Path, width: int, height: int, rows: list[list[int]]) -> None:
    """rows: per-scanline flat RGBA byte lists (len == width*4)."""
    assert len(rows) == height and all(len(r) == width * 4 for r in rows)
    raw = b"".join(b"\x00" + bytes(r) for r in rows)

    def chunk(tag: bytes, data: bytes) -> bytes:
        return (
            struct.pack(">I", len(data))
            + tag
            + data
            + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
        )

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    path.write_bytes(png)
    print(f"wrote {path} ({width}x{height}, {len(png)} bytes)")


def smoothstep(t: float) -> float:
    return t * t * (3.0 - 2.0 * t)


def radial(size: int = 128) -> list[list[int]]:
    """Soft radial gradient: bright core, smooth falloff to zero at the rim."""
    rows: list[list[int]] = []
    c = (size - 1) / 2.0
    for y in range(size):
        row: list[int] = []
        for x in range(size):
            dx = (x - c) / c
            dy = (y - c) / c
            r = math.sqrt(dx * dx + dy * dy)
            a = max(0.0, 1.0 - r)
            a = smoothstep(a) ** 0.75  # smooth decay, keeps a readable core
            row += [255, 255, 255, int(round(a * 255.0))]
        rows.append(row)
    return rows


def band(width: int = 128, height: int = 64) -> list[list[int]]:
    """Horizontal light band: vertical gaussian, soft horizontal edges."""
    rows: list[list[int]] = []
    for y in range(height):
        vy = (y - (height - 1) / 2.0) / (height / 2.0)
        gauss = math.exp(-((vy / 0.46) ** 2))
        row: list[int] = []
        for x in range(width):
            vx = abs((x - (width - 1) / 2.0) / ((width - 1) / 2.0))
            edge = smoothstep(max(0.0, 1.0 - vx))
            row += [255, 255, 255, int(round(min(1.0, gauss * edge) * 255.0))]
        rows.append(row)
    return rows


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    write_png(OUT / "halo_gradient.png", 128, 128, radial(128))
    write_png(OUT / "halo_band.png", 128, 64, band(128, 64))
