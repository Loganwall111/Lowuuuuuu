#!/usr/bin/env python3
"""Generate the mod's built-in Story-Mode world texture overrides.

These live under assets/minecraft/textures/ inside the mod jar, so the world
look ships with the mod itself (users can still override with their own packs):

- colormap/grass.png  : lush Story-Mode green biome gradient
- colormap/foliage.png: matching foliage gradient
- block/grass_block_side.png : dirt with a baked green fringe
- block/short_grass.png      : tintable grayscale blades (biome colored)

Vanilla colormap axes: X = temperature (left cold, right hot),
Y = humidity (top dry, bottom wet).
"""
from __future__ import annotations

import math
import random
import struct
import zlib
from pathlib import Path

ROOT = Path("src/main/resources/assets/minecraft/textures")


def write_png(path: Path, width: int, height: int, rows: list[list[int]]) -> None:
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
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    print(f"wrote {path} ({width}x{height})")


def clamp01(v: float) -> float:
    return max(0.0, min(1.0, v))


def colormap(size: int, tl: tuple, tr: tuple, bl: tuple, br: tuple) -> list[list[int]]:
    """Bilinear temperature/humidity gradient with a gentle luminance ripple."""
    rows: list[list[int]] = []
    for y in range(size):
        fy = y / (size - 1)
        for x in range(size):
            fx = x / (size - 1)
            top = tuple(tl[i] + (tr[i] - tl[i]) * fx for i in range(3))
            bot = tuple(bl[i] + (br[i] - bl[i]) * fx for i in range(3))
            c = tuple(clamp01(top[i] + (bot[i] - top[i]) * fy) for i in range(3))
            ripple = 1.0 + 0.025 * math.sin(fx * 9.0) * math.cos(fy * 7.0)
            rows[-1] if rows else None
            px = [int(clamp01(c[i] * ripple) * 255.0) for i in range(3)] + [255]
            if y == len(rows):
                rows.append([])
            rows[y] = rows[y] + px if isinstance(rows[y], list) else px
            if x == 0 and y > 0 and len(rows) < y + 1:
                pass
    # rebuild cleanly (the append pattern above is only used when rows is fresh)
    out: list[list[int]] = []
    for y in range(size):
        fy = y / (size - 1)
        row: list[int] = []
        for x in range(size):
            fx = x / (size - 1)
            top = tuple(tl[i] + (tr[i] - tl[i]) * fx for i in range(3))
            bot = tuple(bl[i] + (br[i] - bl[i]) * fx for i in range(3))
            c = tuple(clamp01(top[i] + (bot[i] - top[i]) * fy) for i in range(3))
            ripple = 1.0 + 0.025 * math.sin(fx * 9.0) * math.cos(fy * 7.0)
            row += [int(clamp01(c[i] * ripple) * 255.0) for i in range(3)] + [255]
        out.append(row)
    return out


def grass_block_side(size: int = 16) -> list[list[int]]:
    """Dirt base with a baked story-mode green fringe along the top edge."""
    rng = random.Random(1901)
    green = (0.38, 0.72, 0.33)
    dirt = (0.55, 0.40, 0.28)
    rows: list[list[int]] = []
    for y in range(size):
        row: list[int] = []
        for x in range(size):
            n = rng.uniform(-0.07, 0.07)
            if y <= 2 or y == 3 and rng.random() < 0.55:
                c = tuple(clamp01(green[i] * (0.86 + 0.18 * rng.random()) + n * 0.5) for i in range(3))
            elif y == 4 and rng.random() < 0.18:
                c = tuple(clamp01(green[i] * 0.9) for i in range(3))
            else:
                c = tuple(clamp01(dirt[i] * (0.88 + 0.2 * rng.random()) + n) for i in range(3))
            row += [int(clamp01(v) * 255.0) for v in c] + [255]
        rows.append(row)
    return rows


def short_grass(size: int = 16) -> list[list[int]]:
    """Tintable grayscale blades (biome colormap multiplies this)."""
    rng = random.Random(4242)
    grid = [[0] * size for _ in range(size)]
    for blade in range(11):
        x = rng.randint(1, size - 2)
        height = rng.randint(5, 13)
        lean = rng.choice((-1, 0, 0, 1))
        for h in range(height):
            y = size - 1 - h
            x2 = max(0, min(size - 1, x + (lean if h > height // 2 else 0)))
            shade = 150 + int(80 * (h / height))
            grid[y][x2] = max(grid[y][x2], shade)
            if h < height - 2 and rng.random() < 0.3:
                grid[y][max(0, x2 - 1)] = max(grid[y][max(0, x2 - 1)], shade - 40)
    rows: list[list[int]] = []
    for y in range(size):
        row: list[int] = []
        for x in range(size):
            v = grid[y][x]
            row += [v, v, v, 255 if v > 0 else 0]
        rows.append(row)
    return rows


if __name__ == "__main__":
    write_png(ROOT / "colormap" / "grass.png", 256, 256, colormap(256, (0.36, 0.56, 0.28), (0.62, 0.74, 0.28), (0.24, 0.52, 0.36), (0.48, 0.78, 0.34)))
    write_png(ROOT / "colormap" / "foliage.png", 256, 256, colormap(256, (0.38, 0.48, 0.22), (0.60, 0.66, 0.24), (0.26, 0.46, 0.26), (0.50, 0.70, 0.30)))
    write_png(ROOT / "block" / "grass_block_side.png", 16, 16, grass_block_side(16))
    write_png(ROOT / "block" / "short_grass.png", 16, 16, short_grass(16))
