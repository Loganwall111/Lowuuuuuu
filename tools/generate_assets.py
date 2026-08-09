#!/usr/bin/env python3
"""
DEVOURING STORMS — procedural asset generator.

Synthesizes ALL binary assets for the project from pure mathematics:
  - every texture (PIL), pixel-precise 16px sprite work for blocks/items + entity skins
  - every sound (numpy -> OGG via libsndfile), including per-phase storm music and the
    two Decayed Jukebox records built note-by-note from the trailer's vocal motifs
  - standard Java blockstate/model/item-model JSON scaffolding

Nothing copied, nothing sampled, nothing traced. If you replace any of these files with
licensed art later (e.g. the private Tazo/Watcher skins by the original artist), the
generator will happily not overwrite files you pass --skip-existing for.

Usage:  python3 tools/generate_assets.py [--skip-existing]
Requires: pillow, numpy, soundfile  (pip install pillow numpy soundfile)
"""

from __future__ import annotations

import argparse
import json
import math
import shutil
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw
import soundfile as sf

# ---------------------------------------------------------------- paths

REPO = Path(__file__).resolve().parent.parent
JAVA_RES = REPO / "java-mod" / "src" / "main" / "resources"
JAVA_ASSETS = JAVA_RES / "assets" / "devouring_storms"
BEDROCK_RP = REPO / "bedrock-addon" / "DevouringStormsRP"

SR = 22050  # audio sample rate

rng = np.random.default_rng(20271013)  # the classified date, of course

SKIP = argparse.Namespace(skip=False)


def write_png(img: Image.Image, path: Path) -> None:
    if SKIP.skip and path.exists():
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    print(f"  png  {path.relative_to(REPO)}")


def write_json(obj, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n")


def write_ogg(samples: np.ndarray, path: Path) -> None:
    """Mono float32 -> Ogg Vorbis. Gentle peak normalisation."""
    if SKIP.skip and path.exists():
        return
    peak = np.max(np.abs(samples)) or 1.0
    samples = samples / max(peak, 0.98) * 0.9
    path.parent.mkdir(parents=True, exist_ok=True)
    sf.write(str(path), samples.astype(np.float32), SR, format="OGG", subtype="VORBIS")
    print(f"  ogg  {path.relative_to(REPO)}")


# ======================================================================================
#  TEXTURES — helpers
# ======================================================================================

def tex_noise(size: int, base: tuple[int, int, int], jitter: int = 26, alpha: int = 255,
              blank: bool = False) -> Image.Image:
    """Flat-ish noisy base with per-pixel jitter; `blank=True` returns a transparent canvas."""
    noise = rng.integers(-jitter, jitter + 1, size=(size, size, 1))
    if blank:
        noise = noise * 0
    arr = np.clip(np.array(base, dtype=np.int16)[None, None, :] + noise, 0, 255).astype(np.uint8)
    alpha_arr = np.full((size, size, 1), alpha, dtype=np.uint8)
    return Image.fromarray(np.concatenate([arr, alpha_arr], axis=2), "RGBA").copy()


def blotches(img: Image.Image, color: tuple[int, int, int], count: int, size_min=1, size_max=3, alpha=255):
    """Scatter soft darker/lighter blotches — the corruption look."""
    px = img.load()
    w, h = img.size
    for _ in range(count):
        bw = rng.integers(size_min, size_max + 1)
        bh = rng.integers(size_min, size_max + 1)
        x = int(rng.integers(0, max(1, w - bw)))
        y = int(rng.integers(0, max(1, h - bh)))
        shade = int(rng.integers(-30, 30))
        for dx in range(bw):
            for dy in range(bh):
                old = px[(x + dx) % w, (y + dy) % h]
                px[(x + dx) % w, (y + dy) % h] = (
                    int(np.clip(color[0] + shade, 0, 255)),
                    int(np.clip(color[1] + shade, 0, 255)),
                    int(np.clip(color[2] + shade, 0, 255)),
                    alpha,
                )
    return img


def frame(img: Image.Image, color: tuple[int, int, int], width=1):
    dr = ImageDraw.Draw(img)
    w, h = img.size
    for i in range(width):
        dr.rectangle([i, i, w - 1 - i, h - 1 - i], outline=color + (255,))
    return img


def glyph(img: Image.Image, coords, color):
    px = img.load()
    for cx, cy in coords:
        for dx, dy in coords[(cx, cy)] if isinstance(coords, dict) else [(0, 0)]:
            px[min(max(cx + dx, 0), img.size[0] - 1), min(max(cy + dy, 0), img.size[1] - 1)] = color + (255,)
    return img


def disc_texture(bg: tuple, groove: tuple) -> Image.Image:
    img = tex_noise(16, bg, jitter=10)
    dr = ImageDraw.Draw(img)
    dr.ellipse([2, 2, 13, 13], outline=groove + (255,))
    dr.ellipse([4, 4, 11, 11], outline=groove + (255,))
    dr.ellipse([6, 6, 9, 9], fill=(20, 16, 24, 255), outline=groove + (255,))
    return img


def egg_texture(base: tuple, spots: tuple) -> Image.Image:
    """Classic two-tone spawn egg silhouette on transparent bg."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.ellipse([3, 2, 12, 14], fill=base + (255,), outline=(10, 8, 12, 255))
    px = img.load()
    for _ in range(6):
        x = int(rng.integers(4, 11))
        y = int(rng.integers(4, 13))
        if px[x, y][3] > 0:
            dr.ellipse([x, y, min(x + 1, 12), min(y + 1, 14)], fill=spots + (255,))
    return img


# ======================================================================================
#  BLOCK TEXTURES
# ======================================================================================

def block_corrupted_command_block() -> Image.Image:
    img = tex_noise(16, (48, 22, 66), jitter=18)
    blotches(img, (68, 30, 96), 10)
    frame(img, (24, 10, 34))
    # the "command glyph", glitched
    dr = ImageDraw.Draw(img)
    gx, gy = 8, 8
    dr.rectangle([gx - 3, gy - 3, gx + 3, gy + 3], outline=(255, 138, 76, 255))
    dr.point([(gx, gy), (gx - 2, gy + 2), (gx + 2, gy - 2), (gx + 4, gy), (gx - 4, gy)], fill=(255, 60, 220, 255))
    # corruption veins
    for i in range(3):
        x0, y0 = int(rng.integers(0, 12)), int(rng.integers(0, 12))
        dr.line([x0, y0, x0 + 3, y0 + 3], fill=(200, 30, 255, 255))
    return img


def block_terminal(active: bool) -> Image.Image:
    img = tex_noise(16, (18, 16, 24), jitter=12)
    frame(img, (40, 32, 56))
    dr = ImageDraw.Draw(img)
    # screen area
    dr.rectangle([3, 3, 12, 9], fill=(8, 24, 10, 255), outline=(46, 40, 62, 255))
    if active:
        for i in range(5):
            dr.line([4, 4 + i, 11 - (i % 3), 4 + i], fill=(80, 255, 120, 255))
        dr.point([(11, 8), (10, 8)], fill=(255, 60, 255, 255))
    else:
        dr.line([4, 6, 7, 6], fill=(40, 90, 44, 255))
    # keyboard strip
    dr.line([3, 12, 12, 12], fill=(46, 40, 62, 255))
    for x in (4, 7, 10):
        dr.point([(x, 11)], fill=(90, 80, 120, 255))
    return img


def block_mainframe_frame() -> Image.Image:
    img = tex_noise(16, (26, 24, 34), jitter=10)
    frame(img, (56, 48, 76), width=2)
    blotches(img, (34, 30, 48), 6)
    dr = ImageDraw.Draw(img)
    dr.point([(4, 4), (11, 4), (4, 11), (11, 11)], fill=(70, 220, 120, 255))  # status lights
    return img


def block_rift_portal_frames(n_frames=8) -> Image.Image:
    """Animated swirling rift — stacked vertically like vanilla animated textures."""
    img = Image.new("RGBA", (16, 16 * n_frames), (0, 0, 0, 0))
    cx, cy = 7.5, 7.5
    for f in range(n_frames):
        t = f / n_frames * math.tau
        for y in range(16):
            for x in range(16):
                dx, dy = x - cx, y - cy + 16 * f
                r = math.hypot(x - cx, y - cy)
                swirl = math.sin(r * 1.1 - t * 2.0 + math.atan2(dy, dx) * 2.0)
                v = int(120 + 120 * swirl)
                if r < 3.0:
                    color = (255, 120 + f * 8 % 100, 255, 255)
                elif r < 7.5:
                    color = (min(v, 255), 40, min(v + 60, 255), 255)
                else:
                    fade = max(0.0, 1.0 - (r - 7.5) / 3.0)
                    color = (int(60 * fade + 10), int(10 * fade), int(120 * fade + 20), int(200 * fade + 40))
                img.putpixel((x, y + f * 16), color)
    return img


def block_decayed_jukebox(top: bool) -> Image.Image:
    if top:
        img = tex_noise(16, (52, 26, 60), jitter=14)
        dr = ImageDraw.Draw(img)
        dr.rectangle([6, 6, 9, 9], fill=(20, 10, 26, 255))
        frame(img, (30, 14, 36))
    else:
        img = tex_noise(16, (44, 22, 52), jitter=14)
        dr = ImageDraw.Draw(img)
        dr.rectangle([5, 5, 10, 10], outline=(70, 36, 84, 255))
        dr.point([(7, 7), (8, 8), (7, 9), (9, 9)], fill=(90, 180, 220, 255))
        frame(img, (28, 12, 34))
    return img


def block_decayed_soil() -> Image.Image:
    img = tex_noise(16, (56, 38, 60), jitter=22)
    blotches(img, (40, 24, 48), 14)
    blotches(img, (70, 46, 78), 6, 1, 2)
    return img


def block_decayed_stone() -> Image.Image:
    img = tex_noise(16, (66, 60, 78), jitter=16)
    blotches(img, (52, 44, 66), 12, 2, 4)
    blotches(img, (88, 64, 110), 5, 1, 2)
    return img


def block_rot_log(side: bool) -> Image.Image:
    if side:
        img = tex_noise(16, (74, 52, 44), jitter=14)
        px = img.load()
        for x in range(16):  # bark ridges
            if x % 3 == 0:
                for y in range(16):
                    r_, g_, b_, a_ = px[x, y]
                    px[x, y] = (max(0, r_ - 18), max(0, g_ - 14), max(0, b_ - 10), a_)
        blotches(img, (96, 60, 88), 5, 1, 2)
    else:
        img = tex_noise(16, (88, 62, 52), jitter=12)
        dr = ImageDraw.Draw(img)
        for r in range(2, 8, 2):
            dr.ellipse([8 - r, 8 - r, 7 + r, 7 + r], outline=(64, 42, 36, 255))
        dr.ellipse([7, 7, 8, 8], fill=(52, 30, 40, 255))
    return img


def block_decay() -> Image.Image:
    img = tex_noise(16, (86, 30, 120), jitter=30)
    blotches(img, (120, 50, 160), 16, 1, 3)
    dr = ImageDraw.Draw(img)
    dr.point([(int(rng.integers(0, 16)), int(rng.integers(0, 16))) for _ in range(10)], fill=(230, 140, 255, 255))
    return img


# ======================================================================================
#  ITEM TEXTURES
# ======================================================================================

def item_corrupted_blueprints() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.rectangle([3, 1, 12, 14], fill=(226, 216, 190, 255), outline=(120, 100, 80, 255))
    # blueprint lines — the wither storm schematic, glitching out
    for i, ln in enumerate([(5, 4, 10, 4), (5, 6, 11, 6), (5, 8, 9, 8), (6, 10, 11, 10), (6, 12, 10, 12)]):
        x0, y0, x1, y1 = ln
        dr.line([x0, y0, x1, y1], fill=(60, 60, 120, 255))
    dr.line([4, 3, 11, 13], fill=(200, 40, 230, 255))
    dr.line([11, 3, 4, 13], fill=(200, 40, 230, 255))
    dr.point([(8, 2)], fill=(255, 60, 220, 255))
    return img


def item_rift_key() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.rectangle([7, 6, 8, 14], fill=(180, 90, 255, 255))          # shaft
    dr.ellipse([5, 1, 10, 6], outline=(220, 150, 255, 255), fill=(40, 10, 60, 255))
    dr.point([(7, 9), (9, 9), (7, 12), (10, 12)], fill=(230, 200, 255, 255))  # teeth
    dr.point([(7, 3), (8, 4)], fill=(90, 255, 230, 255))           # the rift inside the bow
    return img


def item_formidibomb() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.rectangle([4, 4, 11, 13], fill=(70, 20, 24, 255), outline=(30, 8, 10, 255))
    frame_color = (200, 60, 60, 255)
    dr.line([4, 8, 11, 8], fill=frame_color)
    dr.point([(7, 2), (8, 1)], fill=(220, 200, 60, 255))           # fuse
    dr.point([(8, 1), (9, 0), (8, 2)], fill=(255, 120, 0, 255))    # spark
    return img


def item_watcher_eye() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.ellipse([3, 4, 12, 12], fill=(214, 208, 216, 255), outline=(120, 104, 130, 255))
    dr.ellipse([6, 6, 9, 9], fill=(10, 8, 14, 255))
    dr.point([(6, 6)], fill=(240, 240, 255, 255))
    dr.line([8, 1, 8, 3], fill=(150, 60, 60, 255))
    dr.line([8, 13, 8, 14], fill=(150, 60, 60, 255))
    return img


def item_amulet() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.line([5, 1, 3, 6], fill=(220, 200, 140, 255))
    dr.line([10, 1, 12, 6], fill=(220, 200, 140, 255))
    dr.ellipse([4, 6, 11, 13], fill=(120, 40, 160, 255), outline=(220, 190, 255, 255))
    dr.ellipse([6, 8, 9, 11], fill=(220, 140, 255, 255))
    return img


def item_memory_fragment() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.polygon([(8, 1), (12, 7), (9, 14), (4, 9)], fill=(200, 235, 255, 200), outline=(120, 180, 230, 255))
    dr.point([(7, 4), (8, 8), (6, 10)], fill=(255, 255, 255, 255))
    dr.point([(10, 6)], fill=(90, 240, 255, 255))
    return img


def flesh_texture(base: tuple, vein: tuple) -> Image.Image:
    img = tex_noise(16, base, jitter=20)
    img.paste(img.crop(), (0, 0))
    blotches(img, (min(base[0] + 26, 255), base[1], base[2]), 8)
    dr = ImageDraw.Draw(img)
    dr.line([3, 5, 9, 7, 7, 12], fill=vein + (255,))
    dr.line([11, 3, 8, 8, 12, 12], fill=vein + (255,))
    return img


def item_tendril() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.line([3, 14, 6, 10, 5, 6, 9, 2], fill=(86, 36, 110, 255), width=2)
    dr.line([9, 2, 12, 4], fill=(150, 80, 190, 255), width=1)
    dr.point([(12, 3)], fill=(230, 160, 255, 255))
    return img


def item_storm_dust() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    for _ in range(26):
        x = int(rng.integers(4, 12))
        y = int(rng.integers(6, 14))
        c = 120 + int(rng.integers(0, 120))
        dr.point([(x, y)], fill=(c, c // 2, min(c + 80, 255), 255))
    dr.ellipse([6, 10, 9, 13], fill=(150, 70, 180, 255))
    return img


def star_texture(core: tuple, rim: tuple) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.polygon([(8, 1), (10, 6), (15, 8), (10, 10), (8, 15), (6, 10), (1, 8), (6, 6)],
               fill=rim + (255,))
    dr.ellipse([6, 6, 9, 9], fill=core + (255,))
    dr.point([(7, 7)], fill=(255, 255, 255, 255))
    return img


# ---- THE SEVEN SCHEDULES (ARG vault), payload, sealed vault, Endertown Core ----------------

_DIGIT_FONT = {
    "1": ("010", "110", "010", "010", "111"),
    "2": ("111", "001", "111", "100", "111"),
    "3": ("111", "001", "011", "001", "111"),
    "4": ("101", "101", "111", "001", "001"),
    "5": ("111", "100", "111", "001", "111"),
    "6": ("111", "100", "111", "101", "111"),
    "7": ("111", "001", "010", "010", "010"),
}

def item_schedule(number: int) -> Image.Image:
    """A blood-worn archive slip: pale paper, redacted lines, violet wax seal, its digit."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.rectangle([3, 1, 12, 14], fill=(214, 204, 178, 255))      # paper
    dr.rectangle([3, 1, 12, 2], fill=(168, 148, 116, 255))       # folded top
    dr.rectangle([11, 1, 12, 14], fill=(188, 172, 142, 255))     # edge shade
    for i, y in enumerate((4, 6, 8)):
        w = (7, 5, 6)[i]
        dr.rectangle([4, y, 4 + w, y], fill=(42, 34, 50, 255))   # redacted print
    dr.ellipse([8, 9, 13, 14], fill=(128, 34, 168, 255))         # wax seal
    dr.ellipse([9, 10, 12, 13], fill=(170, 64, 210, 255))
    # the schedule's own digit, stamped into the wax
    glyph = _DIGIT_FONT[str(number)]
    for gy, row in enumerate(glyph):
        for gx, bit in enumerate(row):
            if bit == "1":
                img.putpixel((1 + gx, 9 + gy), (54, 24, 70, 255))
    return img

def item_classified_payload() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.rectangle([2, 1, 13, 14], fill=(24, 18, 32, 255))         # black dossier
    dr.rectangle([2, 1, 13, 3], fill=(44, 30, 58, 255))
    dr.rectangle([4, 6, 12, 8], fill=(224, 64, 96, 255))         # CLASSIFIED band
    dr.line([4, 6, 12, 8], fill=(255, 190, 230, 255))
    dr.ellipse([6, 10, 10, 14], fill=(196, 74, 236, 255))        # payload eye
    dr.point([(8, 13)], fill=(30, 8, 40, 255))
    return img

def block_sealed_vault(opened: bool) -> Image.Image:
    """The ARG vault door-face: gunmetal ribs, a violet keyhole — or its lit-open seam."""
    base = (22, 18, 30) if not opened else (36, 20, 44)
    img = tex_noise(16, base, jitter=8).copy()
    dr = ImageDraw.Draw(img)
    for y in (1, 4, 7, 10, 13):                                   # pressed ribs
        dr.line([0, y, 15, y], fill=(14, 10, 20, 255))
    dr.rectangle([6, 5, 9, 9], fill=(10, 6, 16, 255))             # lock plate
    if opened:
        dr.rectangle([7, 6, 8, 8], fill=(236, 120, 250, 255))     # light seam
        for x, y in ((6, 4), (9, 4), (6, 10), (9, 10), (3, 7), (12, 7)):
            dr.point([(x, y)], fill=(196, 84, 236, 255))
    else:
        dr.rectangle([7, 6, 8, 7], fill=(120, 40, 160, 255))      # cold keyhole
        dr.point([(7, 8), (8, 8)], fill=(120, 40, 160, 255))
    return img

def skin_massg_glow() -> Image.Image:
    """Bedrock emissive overlay for the bowels: burning violet veins on pure transparency."""
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    for _ in range(60):
        x0, y0 = int(rng.integers(0, 122)), int(rng.integers(0, 122))
        x1 = min(127, x0 + int(rng.integers(3, 10)))
        y1 = min(127, max(0, y0 + int(rng.integers(-5, 5))))
        dr.line([x0, y0, x1, y1], fill=(170, 52, 230, 220), width=1)
        dr.line([x0, y0 + 1, x1, y1 + 1], fill=(228, 96, 255, 130), width=1)
    for _ in range(26):                                            # hot cores
        ex, ey = int(rng.integers(4, 123)), int(rng.integers(4, 123))
        dr.point([(ex, ey)], fill=(255, 140, 250, 255))
    return img

def item_storm_killer() -> Image.Image:
    """The Watcher's hoarded blade: a sliver of white command-block fire on a dark hilt."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.line([4, 13, 11, 5], fill=(235, 240, 255, 255), width=2)     # white-hot edge
    dr.line([5, 14, 12, 6], fill=(150, 210, 255, 255), width=1)     # blue rim
    dr.line([3, 12, 10, 4], fill=(255, 120, 240, 255), width=1)     # violet rim
    dr.rectangle([2, 12, 5, 15], fill=(20, 12, 30, 255))            # hilt
    dr.point([(12, 5)], fill=(255, 255, 255, 255))                  # the tip that ends storms
    return img

def item_storm_heart() -> Image.Image:
    """Proof of the rend: a ball of white energy with a violet pulse core."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.ellipse([3, 3, 12, 12], fill=(235, 240, 255, 255))
    dr.ellipse([5, 5, 10, 10], fill=(190, 120, 240, 255))
    dr.rectangle([7, 7, 8, 8], fill=(255, 255, 255, 255))
    dr.point([(4, 8), (11, 8), (8, 4), (8, 11)], fill=(255, 200, 250, 255))
    return img

def item_seventh_trumpet() -> Image.Image:
    """The Seventh Trumpet: a bent end-metal horn, mouthpiece glowing faintly."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    dr.rectangle([2, 10, 9, 12], fill=(196, 178, 152, 255))         # tube
    dr.rectangle([8, 6, 13, 12], fill=(168, 152, 130, 255))         # bend
    dr.rectangle([10, 4, 14, 7], fill=(116, 90, 130, 255))          # flared bell
    dr.point([(12, 5), (13, 5)], fill=(255, 130, 240, 255))         # the sound inside
    dr.rectangle([1, 10, 2, 12], fill=(90, 76, 60, 255))            # mouthpiece
    return img

def item_audio_log(variant: int) -> Image.Image:
    """E.P.A. field tape — three sleeve colours for the three logs."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    sleeve = ((38, 60, 72), (60, 34, 36), (44, 34, 62))[variant]
    dr.rectangle([3, 2, 12, 13], fill=(24, 20, 28, 255))            # cassette
    dr.rectangle([3, 2, 12, 4], fill=sleeve + (255,))               # label band
    dr.ellipse([4, 6, 7, 9], outline=(200, 200, 220, 255))
    dr.ellipse([8, 6, 11, 9], outline=(200, 200, 220, 255))
    dr.rectangle([5, 11, 10, 12], fill=(140, 30, 60, 255))          # exposed tape
    return img

def block_frayed_tear() -> Image.Image:
    """A poorly tear: violet cloth-of-reality with a white seam down the middle."""
    img = tex_noise(16, (44, 10, 64), jitter=12).copy()
    dr = ImageDraw.Draw(img)
    for y in range(16):
        wob = int(2 * math.sin(y * 0.9))
        dr.point([(8 + wob, y)], fill=(245, 240, 255, 255))
        if y % 2 == 0:
            dr.point([(7 + wob, y)], fill=(150, 60, 210, 255))
            dr.point([(9 + wob, y)], fill=(150, 60, 210, 255))
    return img

def skin_preacher() -> Image.Image:
    img = tex_noise(64, (52, 22, 66), jitter=12)                    # violet vestments
    blotches(img, (66, 30, 84), 50, 2, 5)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 0, 32, 6], fill=(226, 214, 238, 255))          # hood pale band
    dr.rectangle([12, 2, 14, 4], fill=(30, 10, 40, 255))            # eyes in the hood
    dr.rectangle([17, 2, 19, 4], fill=(30, 10, 40, 255))
    dr.rectangle([0, 12, 24, 13], fill=(196, 168, 96, 255))         # stole stripe
    return img

def skin_townsfolk() -> Image.Image:
    img = tex_noise(64, (40, 56, 52), jitter=14)                    # worn teal-grey work clothes
    blotches(img, (52, 68, 62), 50, 2, 5)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 0, 32, 7], fill=(214, 188, 158, 255))          # face band
    dr.rectangle([11, 3, 13, 5], fill=(36, 30, 34, 255))            # tired eyes
    dr.rectangle([18, 3, 20, 5], fill=(36, 30, 34, 255))
    dr.rectangle([0, 6, 32, 7], fill=(86, 62, 44, 255))             # hairline
    return img

def skin_storm_mite() -> Image.Image:
    img = tex_noise(64, (58, 15, 92), jitter=18)                 # shed storm-tissue
    blotches(img, (96, 38, 150), 60, 2, 5)
    blotches(img, (28, 8, 44), 40, 2, 4)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 11, 12, 15], fill=(176, 96, 255, 255))      # feeler/sheen band
    dr.point([(2, 12), (6, 12), (9, 13)], fill=(255, 255, 255, 255))  # mite glares
    return img


def skin_the_taken() -> Image.Image:
    img = tex_noise(64, (31, 10, 51), jitter=16)                 # decay-soaked flesh
    blotches(img, (46, 18, 70), 80, 3, 7)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 15, 28, 21], fill=(86, 62, 44, 255))        # what's left of the villager robe
    dr.rectangle([0, 15, 28, 16], fill=(120, 190, 110, 255))     # emeraldsmear
    # the eyes remember the plague glow (head band, emissive pass)
    dr.rectangle([2, 16, 6, 18], fill=(63, 215, 200, 255))
    dr.rectangle([10, 16, 14, 18], fill=(63, 215, 200, 255))
    return img


def skin_travis() -> Image.Image:
    img = tex_noise(64, (28, 42, 72), jitter=12)                 # navy field coat
    blotches(img, (38, 56, 90), 50, 2, 5)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 18, 28, 25], fill=(212, 186, 160, 255))     # face band
    dr.rectangle([28, 18, 42, 25], fill=(66, 44, 28, 255))       # hair band (uv 28,18)
    dr.rectangle([4, 20, 7, 22], fill=(26, 30, 44, 255))         # tired eyes
    dr.rectangle([12, 20, 15, 22], fill=(26, 30, 44, 255))
    dr.rectangle([52, 30, 56, 34], fill=(150, 220, 255, 255))    # recorder, still running
    return img


def skin_tonya() -> Image.Image:
    img = tex_noise(64, (188, 210, 232), jitter=15)              # echo-pale
    blotches(img, (220, 236, 250), 50, 2, 5)
    dr = ImageDraw.Draw(img)
    for _ in range(30):                                          # dropout grain — barely a person
        x, y = int(rng.integers(0, 62)), int(rng.integers(0, 62))
        dr.point([(x, y)], fill=(10, 20, 40, 120))
    dr.rectangle([0, 17, 24, 22], fill=(240, 244, 252, 255))     # face band
    dr.point([(5, 19), (13, 19)], fill=(40, 70, 120, 255))       # eyes, out of phase
    return img


def skin_creator() -> Image.Image:
    """96x96: void-silk robe, a face-less head, and two red eyes that outlast blinks."""
    img = Image.new("RGBA", (96, 96), (0, 0, 0, 255))
    dr = ImageDraw.Draw(img)
    # robe + arms: deep indigo voidcloth with pinprick stars (rows 0-56)
    for y in range(0, 58):
        for x in range(0, 96, 2):
            base = 10 + (x * 7 + y * 13) % 14
            dr.point([(x, y), (x + 1, y)], fill=(base, base - 6 if base > 6 else base, base + 8, 255))
            if (x * 31 + y * 17) % 97 == 0:
                dr.point([(x, y)], fill=(220, 225, 255, 255))   # a star noticed you
    # head band (uv 60,0): the absence of a face
    dr.rectangle([60, 0, 95, 15], fill=(16, 12, 24, 255))
    # THE EYES (uv 60,0 chip, emissive layer): two perfect red lenses
    dr.rectangle([62, 4, 68, 7], fill=(255, 40, 40, 255))
    dr.rectangle([76, 4, 82, 7], fill=(255, 40, 40, 255))
    dr.point([(64, 5), (78, 5)], fill=(255, 180, 180, 255))     # iris glint
    # crown chip (uv 48,40) + arm rims: navy-gold circuitry of authority
    dr.rectangle([48, 40, 63, 46], fill=(30, 24, 48, 255))
    for x in range(48, 64, 3):
        dr.point([(x, 41), (x, 44)], fill=(230, 190, 90, 255))
    return img


def skin_creator_hand() -> Image.Image:
    """64x64: the closed hand that politely ends arguments; dark glove, lit knuckles."""
    img = tex_noise(64, (14, 10, 22), jitter=10)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 30, 63, 33], fill=(36, 28, 56, 255))       # wrist band (uv 0,30)
    for x in range(8, 26, 6):                                    # knuckle lights (palm face)
        dr.point([(x, 3), (x + 1, 3)], fill=(255, 60, 60, 255))
    return img


def skin_monstrosity() -> Image.Image:
    """96x64: a moustache that covers the horror; pastel static spilling at the edges."""
    img = tex_noise(96, (58, 28, 78), jitter=18)
    dr = ImageDraw.Draw(img)
    dr.rectangle([32, 20, 63, 28], fill=(40, 24, 52, 255))      # head zone (uv 32,20)
    dr.rectangle([34, 24, 61, 26], fill=(20, 12, 28, 255))      # the moustache. do not look under it
    dr.point([(35, 24), (59, 24), (47, 25)], fill=(220, 60, 170, 255))  # wax tips
    # antenna tips (uv 48,30 / 40,40): colourful broadcast LEDs
    dr.rectangle([48, 30, 51, 31], fill=(63, 215, 200, 255))
    dr.rectangle([40, 40, 43, 41], fill=(255, 63, 200, 255))
    for y in (44, 48, 52):                                       # static spill rows
        for x in range(0, 96, 3):
            c = [(255, 63, 200), (63, 215, 200), (240, 240, 245)][(x + y) % 3]
            dr.point([(x, y)], fill=(*c, 255))
    return img


def skin_forger() -> Image.Image:
    """64x64: a foundry-bell body; eight spout rims; one furnace eye."""
    img = tex_noise(64, (26, 18, 40), jitter=12)
    dr = ImageDraw.Draw(img)
    dr.rectangle([48, 20, 61, 24], fill=(255, 140, 60, 255))    # furnace flame chip (uv 48,20)
    dr.point([(50, 21), (52, 22), (55, 22), (58, 21)], fill=(255, 230, 140, 255))
    for x in range(6, 58, 8):                                    # spout rim lights
        dr.point([(x, 0), (x + 1, 0)], fill=(150, 220, 255, 255))
    return img


def skin_sky_tentacle() -> Image.Image:
    """64x64: descending stalk flesh with a curved tip and four lit landing cups."""
    img = tex_noise(64, (40, 20, 66), jitter=14)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 20, 31, 23], fill=(70, 30, 110, 255))      # tip band (uv 0,20)
    for x in range(4, 28, 6):                                    # cups
        dr.point([(x, 21)], fill=(200, 90, 255, 255))
    return img


def skin_cart_shopper() -> Image.Image:
    """64x64: mall-grey raincoat, two eyes on red-shift, and a faithful cart."""
    img = tex_noise(64, (44, 52, 56), jitter=10)
    dr = ImageDraw.Draw(img)
    dr.rectangle([28, 38, 43, 41], fill=(230, 40, 40, 255))     # head chip (uv 28,38) emissive
    dr.point([(30, 39), (32, 39), (38, 39), (40, 39)], fill=(10, 6, 8, 255))  # pupils, bored
    dr.rectangle([0, 30, 63, 33], fill=(150, 150, 158, 255))    # cart lattice (uv 0,30)
    dr.rectangle([20, 30, 23, 32], fill=(210, 210, 218, 255))   # basket glint
    return img


def skin_researcher() -> Image.Image:
    """64x64: EPA lab coat, green badge, glasses catching a terminal's light forever."""
    img = tex_noise(64, (225, 226, 232), jitter=8)
    dr = ImageDraw.Draw(img)
    dr.rectangle([32, 18, 47, 21], fill=(60, 70, 90, 255))      # head chip (uv 32,18)
    dr.point([(34, 19), (37, 19), (41, 19), (44, 19)], fill=(180, 240, 255, 255))  # glasses
    dr.rectangle([52, 30, 55, 33], fill=(40, 200, 90, 255))     # badge
    dr.rectangle([40, 30, 44, 33], fill=(200, 200, 205, 255))   # clipboard
    return img


def skin_earth_eater() -> Image.Image:
    """96x64: horizon-scale body of tectonic black; the emissive throat of mass."""
    img = Image.new("RGBA", (96, 64), (6, 5, 10, 255))
    dr = ImageDraw.Draw(img)
    for y in range(0, 36):
        for x in range(0, 96, 3):                                # continental plates
            if (x * 11 + y * 7) % 23 == 0:
                dr.point([(x, y), (x + 1, y)], fill=(24, 20, 30, 255))
    for x in range(0, 96, 2):                                    # magma seams
        if x % 14 == 0:
            dr.point([(x, 12), (x, 22)], fill=(200, 60, 20, 255))
    dr.rectangle([0, 36, 47, 39], fill=(180, 40, 20, 255))      # throat band (uv 0,36) emissive
    dr.rectangle([4, 37, 43, 38], fill=(255, 90, 40, 255))      # inner glow
    return img


def skin_void_maw() -> Image.Image:
    img = tex_noise(64, (4, 3, 8), jitter=4)                     # the quiet
    dr = ImageDraw.Draw(img)
    # lensed-starlight bands for the halo quads (uv rows 30/34/38)
    for band, base in ((30, 235), (34, 205), (38, 180)):
        for x in range(0, 40):
            glint = base if (x * 7 + band) % 5 else 255
            dr.point([(x, band), (x, band + 2)], fill=(glint, int(glint * 0.82), 255, 255))
    dr.rectangle([40, 0, 45, 2], fill=(240, 220, 255, 255))      # emissive core chip
    return img


def block_glitch() -> Image.Image:
    """The Monstrosity's trail: pastel channel-static with honest-to-blackness dropout bands."""
    img = tex_noise(16, (58, 18, 74), jitter=26)
    px = img.load()
    for y in range(16):
        for x in range(16):
            if (x + y * 3) % 7 == 0:
                px[x, y] = (255, 63, 200, 255)
            elif (x * 5 + y) % 11 == 0:
                px[x, y] = (63, 215, 200, 255)
            elif (x + y) % 13 == 0:
                px[x, y] = (10, 8, 14, 255)
    return img


def block_vhs_jukebox() -> Image.Image:
    """The vcr-face: slot, counter window, and one lit red eye that believes in you."""
    img = tex_noise(16, (18, 16, 22), jitter=8, blank=True)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 0, 15, 15], fill=(24, 20, 28, 255))
    dr.rectangle([1, 1, 14, 3], fill=(48, 42, 56, 255))          # brushed aluminium crown
    dr.rectangle([3, 5, 12, 6], fill=(10, 8, 12, 255))           # the slot
    dr.rectangle([3, 5, 12, 5], fill=(140, 120, 150, 255))       # slot glint
    dr.rectangle([11, 2, 12, 2], fill=(255, 70, 60, 255))        # REC dot
    dr.rectangle([2, 9, 8, 12], fill=(16, 26, 40, 255))          # counter window
    dr.point([(3, 11), (5, 10), (7, 11)], fill=(120, 220, 255, 255))  # digits, counting
    dr.rectangle([10, 10, 13, 13], fill=(52, 46, 60, 255))       # eject+track buttons
    return img


def block_crate() -> Image.Image:
    """Shipping crate: battered printed timber, a sticker that meant something once."""
    img = tex_noise(16, (96, 72, 44), jitter=14)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 0, 15, 15], outline=(60, 44, 26, 255))      # frame
    dr.line([0, 5, 15, 5], fill=(70, 52, 30, 255))               # planks
    dr.line([0, 10, 15, 10], fill=(70, 52, 30, 255))
    dr.rectangle([9, 1, 14, 4], fill=(210, 200, 170, 255))       # the sticker
    dr.point([(10, 3), (12, 2)], fill=(180, 40, 40, 255))        # stamp
    return img


def item_rocket_key() -> Image.Image:
    """A tiny ship on a keyring: sky going the wrong way, on purpose."""
    img = tex_noise(16, (20, 22, 34), jitter=10, blank=True)
    dr = ImageDraw.Draw(img)
    dr.polygon([(8, 1), (11, 7), (11, 12), (5, 12), (5, 7)], fill=(212, 218, 232, 255))      # hull
    dr.polygon([(8, 1), (10, 5), (6, 5)], fill=(255, 63, 60, 255))                            # nose
    dr.rectangle([6, 7, 9, 9], fill=(63, 215, 230, 255))                                       # window
    dr.polygon([(5, 12), (3, 15), (5, 15)], fill=(200, 70, 90, 255))                           # fins
    dr.polygon([(11, 12), (13, 15), (11, 15)], fill=(200, 70, 90, 255))
    dr.rectangle([7, 12, 8, 13], fill=(255, 190, 80, 255))                                     # a little fire
    dr.point([(2, 3), (13, 2), (14, 9)], fill=(240, 240, 250, 255))                            # stars
    return img


def item_endertown_core() -> Image.Image:
    """16x16 Endertown Core: the town's memorial spire between two banner poles."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    OBS = (18, 10, 28, 255); OBS_L = (34, 20, 52, 255)
    CRY = (150, 66, 196, 255); CRY_L = (203, 128, 236, 255)
    PUR = (110, 40, 172, 255); MAG = (208, 84, 224, 255); GLD = (235, 186, 98, 255)
    for x in range(2, 14):
        px[x, 13] = OBS_L
    for y in range(3, 13):
        px[7, y] = OBS; px[8, y] = OBS
    px[7, 2] = CRY; px[8, 2] = CRY_L
    px[7, 6] = CRY; px[8, 6] = CRY_L
    for y in range(7, 13):
        px[4, y] = OBS_L; px[11, y] = OBS_L
    for y in range(7, 11):
        px[3, y] = PUR if y % 2 == 0 else MAG
        px[12, y] = MAG if y % 2 == 0 else PUR
    px[3, 11] = MAG; px[12, 11] = MAG
    px[6, 4] = GLD; px[9, 4] = GLD
    return img

# ======================================================================================
#  ENTITY SKINS — full-field noisy palettes (all cubes sample texOffs(0,0) regions,
#  so skins are designed as atmospheric fields, not precise UV maps)
# ======================================================================================

def skin_massg() -> Image.Image:
    img = tex_noise(128, (30, 16, 44), jitter=20)
    blotches(img, (46, 22, 66), 240, 3, 10)
    blotches(img, (16, 8, 24), 200, 2, 8)
    px = img.load()
    # burning violet veins
    dr = ImageDraw.Draw(img)
    for _ in range(16):
        x0, y0 = int(rng.integers(0, 120)), int(rng.integers(0, 120))
        dr.line([x0, y0, x0 + int(rng.integers(4, 12)), y0 + int(rng.integers(-6, 6))],
                fill=(150, 40, 200, 255), width=1)
    # a few dead-white eyes staring out of the storm
    for _ in range(5):
        ex, ey = int(rng.integers(10, 118)), int(rng.integers(10, 118))
        dr.rectangle([ex, ey, ex + 2, ey + 1], fill=(235, 220, 255, 255))
        px[ex + 1, ey] = (255, 80, 240, 255)
    # TRACTOR BEAM strip at UV (104,104): violet filaments pre-faded so the part renders
    # translucent. 8 wide x 24 tall, alpha falls toward the tip (bottom of the strip).
    for sy in range(104, 128):
        t = (sy - 104) / 23.0
        for sx in range(104, 112):
            alpha = int(max(0, min(255, 190 * (1.0 - t) + 18)))
            core = 255 if sx in (105, 106) else int(150 + 60 * (1.0 - t))
            px[sx, sy] = (core, int(70 + 40 * (1.0 - t)), 255, alpha)
    return img


def skin_watcher() -> Image.Image:
    img = tex_noise(64, (12, 10, 18), jitter=10)
    blotches(img, (20, 16, 30), 60, 2, 6)
    dr = ImageDraw.Draw(img)
    # the gaze band (uv region 0,46 -> rendered emissive by renderer)
    dr.rectangle([0, 46, 6, 47], fill=(255, 255, 255, 255))
    dr.rectangle([1, 46, 5, 47], fill=(140, 220, 255, 255))
    return img


def skin_tazo() -> Image.Image:
    img = tex_noise(64, (36, 84, 96), jitter=16)              # storm-torn teal cloak
    blotches(img, (24, 60, 72), 90, 2, 6)
    dr = ImageDraw.Draw(img)
    dr.rectangle([0, 0, 32, 8], fill=(200, 168, 146, 255))    # skin tone band for head cubes
    dr.rectangle([4, 3, 6, 5], fill=(30, 30, 40, 255))        # eyes
    dr.rectangle([10, 3, 12, 5], fill=(30, 30, 40, 255))
    blotches(img, (52, 110, 120), 30, 1, 3)
    return img


def skin_anna() -> Image.Image:
    img = tex_noise(64, (216, 208, 224), jitter=14)           # pale, overexposed
    blotches(img, (188, 178, 202), 80, 2, 5)
    dr = ImageDraw.Draw(img)
    for _ in range(24):                                       # dropout speckles — file corruption
        x, y = int(rng.integers(0, 62)), int(rng.integers(0, 62))
        dr.rectangle([x, y, x + 1, y + 1], fill=(0, 0, 0, 0))
    dr.rectangle([0, 0, 32, 7], fill=(236, 226, 238, 255))    # face
    dr.point([(5, 3), (11, 3)], fill=(70, 60, 90, 255))       # closed eyes — or not eyes at all
    return img


def skin_symbiont() -> Image.Image:
    img = tex_noise(64, (74, 52, 82), jitter=24)
    blotches(img, (54, 70, 48), 90, 2, 6)                     # sickly green rot
    blotches(img, (44, 30, 56), 60, 3, 7)
    dr = ImageDraw.Draw(img)
    for _ in range(8):
        x, y = int(rng.integers(2, 60)), int(rng.integers(6, 60))
        dr.line([x, y, x + int(rng.integers(3, 8)), y + int(rng.integers(-2, 4))],
                fill=(160, 90, 200, 255))
    dr.rectangle([0, 0, 32, 8], fill=(96, 76, 104, 255))      # face
    dr.rectangle([4, 2, 7, 4], fill=(230, 200, 255, 255))     # storm-lit eyes
    dr.rectangle([10, 2, 13, 4], fill=(230, 200, 255, 255))
    return img


# ======================================================================================
#  AUDIO — synthesis helpers
# ======================================================================================

def env_adsr(n: int, a: float, d: float, s: float, r: float, total: float) -> np.ndarray:
    """Simple ADSR envelope with segment lengths in fractions of total."""
    t = np.linspace(0, total, n, endpoint=False)
    e = np.ones(n) * s
    an, dn, rn = int(a * n), int(d * n), int(r * n)
    if an: e[:an] = np.linspace(0, 1, an)
    if dn: e[an:an + dn] = np.linspace(1, s, dn)
    if rn: e[n - rn:] *= np.linspace(1, 0, rn)
    return t, e


def lowpass(x: np.ndarray, alpha: float) -> np.ndarray:
    """One-pole lowpass."""
    y = np.empty_like(x)
    acc = 0.0
    for i in range(len(x)):
        acc += alpha * (x[i] - acc)
        y[i] = acc
    return y


def bandpass_noise(n: int, center: float, width: int, sample_rate=SR) -> np.ndarray:
    noise = rng.standard_normal(n)
    spectrum = np.fft.rfft(noise)
    freqs = np.fft.rfftfreq(n, 1 / sample_rate)
    mask = np.exp(-0.5 * ((freqs - center) / (width / 2 + 1)) ** 2)
    return np.fft.irfft(spectrum * mask, n)


def echo(x: np.ndarray, delay_s: float, feedback: float, mix: float) -> np.ndarray:
    d = int(delay_s * SR)
    out = x.copy()
    taps = 6
    for i in range(1, taps):
        idx = np.arange(len(x)) - d * i
        valid = idx >= 0
        out[valid] += x[idx[valid]] * (feedback ** i)
    return (1 - mix) * x + mix * out


def sine(f, t): return np.sin(2 * np.pi * f * t)


def saw(f, t): return 2 * ((f * t) % 1.0) - 1.0


def note_freq(midi: int) -> float:
    return 440.0 * 2 ** ((midi - 69) / 12)


def drone(chord: list[float], dur: float, detune=0.35, pulse_hz=0.0, bright=0.5) -> np.ndarray:
    """Detuned saw stack through a one-pole lowpass. The voice of the storm."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    out = np.zeros(n)
    for f in chord:
        for dt in (-detune, 0.0, detune):
            out += saw(f * (1 + dt / 100), t) * (1 / (3 * len(chord)))
    out = lowpass(out, 0.05 + 0.2 * bright)
    lfo_amt = 0.18 + 0.12 * bright
    out *= 1 - lfo_amt + lfo_amt * sine(0.13 + 0.07 * bright, t)
    if pulse_hz > 0:
        sub = (sine(48, t) * np.clip(sine(pulse_hz, t), 0, 1) ** 3) * 0.9
        out = out * 0.8 + sub * 0.55
    t2, e = env_adsr(n, 0.06, 0.001, 1.0, 0.25, dur)
    return out * e * 0.7


def growl(dur: float, base=52.0, chaos=0.6) -> np.ndarray:
    """FM growl/roar."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    mod = sine(base * (1.5 + chaos * rng.random(1)[0]), t)
    carrier = sine(base * (0.98 + 0.04 * rng.standard_normal(len(t)) * 0.1), t + 0.9 * (mod / base))
    noise_part = bandpass_noise(n, 320, 500) * 0.6
    _, e = env_adsr(n, 0.04, 0.2, 0.7, 0.45, dur)
    pitch_drop = np.maximum(0.3, 1 - t / dur * 0.7)
    return (carrier * pitch_drop + noise_part) * e * 0.8


def thump(dur=0.5, f0=95.0, f1=38.0) -> np.ndarray:
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = f1 + (f0 - f1) * np.exp(-t * 14)
    phase = np.cumsum(f) / SR
    e = np.exp(-t * 10)
    return sine(1.0, phase) * e


def beep(f: float, dur: float, kind="sine", decay=24.0) -> np.ndarray:
    n = int(dur * SR)
    t = np.arange(n) / SR
    osc = sine(f, t) if kind == "sine" else saw(f, t)
    return osc * np.exp(-t * decay)


def beep_seq(freqs: list[float], step: float, kind="sine", tail=0.0) -> np.ndarray:
    parts = [beep(f, step + tail, kind) for f in freqs]
    total = int((len(freqs) * step + tail) * SR)
    out = np.zeros(total)
    for i, p in enumerate(parts):
        start = int(i * step * SR)
        out[start:start + len(p)] += p[:max(0, total - start)]
    return out


def static_bed(dur: float, vol=0.15, lofi=8) -> np.ndarray:
    n = int(dur * SR)
    x = rng.standard_normal(n)
    x = np.round(x * lofi) / lofi  # bit-crushed
    return x * vol


def chiptune(notes: list[tuple[int, float]], bpm: float, voice="pluck", vol=0.5) -> np.ndarray:
    """notes = [(midi, beats)] monophonic melody line."""
    out = []
    beat = 60.0 / bpm
    for midi, beats in notes:
        dur = max(0.06, beats * beat)
        n = int(dur * SR)
        t = np.arange(n) / SR
        f = note_freq(midi)
        if midi <= 0:
            out.append(np.zeros(n))
            continue
        if voice == "pluck":
            e = np.exp(-t * 7)
            w = (sine(f, t) + 0.5 * sine(2 * f, t) + 0.25 * sine(3 * f, t)) * e
        elif voice == "lullaby":
            e = np.clip(1 - t / dur, 0, 1) ** 0.7
            vib = 1 + 0.004 * sine(5.2, t)
            w = (sine(f * vib, t) + 0.3 * sine(2 * f * vib, t)) * e
        elif voice == "bell":
            e = np.exp(-t * 4)
            w = (sine(f, t) + 0.6 * sine(2.01 * f, t) + 0.3 * sine(3.98 * f, t)) * e
        else:
            w = sine(f, t)
        out.append(w)
    return np.concatenate(out) * vol


def padmix(*arrays: np.ndarray) -> np.ndarray:
    """Sum arrays of different lengths by zero-padding to the longest."""
    n = max(len(a) for a in arrays)
    out = np.zeros(n)
    for a in arrays:
        out[:len(a)] += a
    return out


def loopable(x: np.ndarray, fade=0.25) -> np.ndarray:
    """Crossfade tail into head so the file loops seamlessly."""
    n = int(fade * len(x))
    if n <= 4:
        return x
    head, tail = x[:-n], x[-n:]
    ramp = np.linspace(0, 1, n)
    blended = head[:n] * (1 - ramp) + tail * ramp
    return np.concatenate([blended, head[n:]])


# ======================================================================================
#  SOUND BANK
# ======================================================================================

def build_sounds() -> dict[str, np.ndarray]:
    S: dict[str, np.ndarray] = {}

    # ---- ambience ------------------------------------------------------------------
    S["ambient/decayed_loop"] = loopable(
        drone([55.0, 55.5, 82.4], 20.0, detune=0.8, bright=0.25)
        + bandpass_noise(int(20 * SR), 400, 300) * 0.05, 0.15)
    S["ambient/rift_hum"] = loopable(
        (sine(110, np.arange(int(6 * SR)) / SR) + sine(111.3, np.arange(int(6 * SR)) / SR)) * 0.25
        + bandpass_noise(int(6 * SR), 900, 200) * 0.04, 0.3)

    # ---- MASSG ---------------------------------------------------------------------
    boot = echo(beep_seq([220.0, 277.2, 329.6, 220.0, 415.3], 0.22, "sine", 0.25), 0.18, 0.3, 0.4)
    S["massg/awakening"] = np.concatenate([static_bed(0.4, 0.1), boot * 0.5, growl(2.2, 46, 0.8)])
    S["massg/roar"] = growl(2.4, 48.0, 0.7)
    S["massg/devour"] = echo(growl(1.1, 40.0, 1.0) + bandpass_noise(int(1.1 * SR), 500, 340) * 0.4, 0.08, 0.4, 0.3)
    S["massg/pull_loop"] = loopable(
        drone([42.0, 63.0], 6.0, detune=0.4, pulse_hz=2.0, bright=0.35)
        + bandpass_noise(int(6 * SR), 700, 150) * 0.1, 0.3)
    S["massg/devolve_sting"] = np.concatenate([
        beep_seq([880.0, 622.2, 1108.7, 466.2], 0.1, "saw", 0.05) * 0.4,
        growl(1.6, 38.0, 1.2)])
    S["massg/play_dead"] = np.concatenate([growl(2.0, 36.0, 0.4),
                                           bandpass_noise(int(1.5 * SR), 200, 120) * np.linspace(0.6, 0, int(1.5 * SR))])
    S["massg/rebirth"] = np.concatenate([thump(0.4, 60, 24), growl(2.6, 55.0, 0.9)])
    S["massg/true_death"] = np.concatenate([
        growl(3.0, 34.0, 0.5),
        echo(beep_seq([523.3, 392.0, 329.6, 261.6], 0.4), 0.3, 0.5, 0.5) * 0.35])

    # ---- phase music ----------------------------------------------------------------
    # SIGNAL: two tones, unsettlingly simple — "the signal grows when observed"
    S["music/signal"] = loopable(drone([110.0, 116.5], 26.0, detune=0.3, bright=0.2), 0.2)
    # HUNGER: minor drone + heartbeat of sub pulses
    S["music/hunger"] = loopable(drone([73.4, 87.3, 110.0], 28.0, pulse_hz=1.4, bright=0.4), 0.2)
    # DEVOURER: adds a grinding mid band
    dev = drone([65.4, 98.0], 30.0, pulse_hz=2.0, bright=0.55)
    S["music/devourer"] = loopable(dev + bandpass_noise(len(dev), 520, 130) * 0.1, 0.2)
    # SUNDERER: stacked tritones — the sound of physically coming apart
    S["music/sunderer"] = loopable(drone([69.3, 98.0, 138.6], 30.0, pulse_hz=2.6, bright=0.65), 0.2)
    # GENESIS: the choir of broken code (cluster chord + shimmer)
    gen = drone([49.0, 51.9, 58.3, 98.0, 103.8], 32.0, pulse_hz=3.2, bright=0.8)
    shimmer = bandpass_noise(len(gen), 2400, 400) * 0.05
    S["music/genesis"] = loopable(echo((gen + shimmer) * 0.9, 0.24, 0.35, 0.25), 0.2)
    # CRITICAL: fast pulse under dissonant siren thirds — "the music turns critical"
    crit = drone([62.2, 78.0, 92.5], 18.0, pulse_hz=5.6, bright=0.9)
    siren = beep_seq([932.3, 987.8, 932.3, 987.8] * 5, 0.45, "sine", 0.1) * 0.16
    crit = crit[:len(siren)] + siren[:len(crit)]
    S["music/critical"] = loopable(crit, 0.15)

    # ---- The Watcher -----------------------------------------------------------------
    hb = np.concatenate([thump(0.5, 90, 34), np.zeros(int(0.12 * SR)),
                         thump(0.4, 70, 30), np.zeros(int(0.7 * SR))])
    S["watcher/heartbeat"] = np.concatenate([hb, hb]) * 0.9
    wh = bandpass_noise(int(1.8 * SR), 1200, 900)
    f = np.linspace(0, 1, len(wh))
    syll = (np.clip(sine(6.5, f * 1.8 * math.pi), 0, 1) ** 2) * 0.8
    S["watcher/whisper"] = echo(wh * syll, 0.13, 0.45, 0.35)
    S["watcher/vanish"] = np.concatenate([bandpass_noise(int(0.8 * SR), 1800, 2400) * np.linspace(0.7, 0, int(0.8 * SR)), beep(2200, 0.5, decay=3.0) * 0.15])

    # ---- Anna ------------------------------------------------------------------------
    gig = chiptune([(76, 0.5), (74, 0.5), (71, 1.0), (68, 0.5), (64, 1.0)], 132, "lullaby", 0.4)
    S["anna/giggle"] = echo(gig, 0.14, 0.5, 0.4)

    # ---- glitch ----------------------------------------------------------------------
    for idx, name in enumerate(("glitch", "glitch2")):
        n = int(0.7 * SR)
        x = beep_seq([8800 / (i + 2 + idx) for i in range(14)], 0.03, "saw", 0.0) * 0.3
        y = static_bed(0.7, 0.22, lofi=6)
        z = np.zeros(n)
        z[:len(x)] = x[:n]
        # random reversals — corrupted playback
        for seg in range(4):
            a = int(rng.integers(0, n - 4000))
            z[a:a + 4000] = z[a:a + 4000][::-1]
        S[name] = (z + y) * 0.8

    # ---- terminal / rift ---------------------------------------------------------------
    S["terminal/boot"] = np.concatenate([static_bed(0.2, 0.12),
                                         beep_seq([440.0, 554.4, 659.3], 0.16, "sine", 0.3) * 0.5])
    # radio static + morse-like beep pattern
    tr_beeps = beep_seq([1200.0, 1500.0, 900.0, 1350.0, 750.0], 0.12, "sine", 0.05) * 0.25
    n_tr = int(0.9 * SR)
    tr = static_bed(0.9, 0.1)
    tr[:min(n_tr, len(tr_beeps))] += tr_beeps[:min(n_tr, len(tr_beeps))]
    S["terminal/transmission"] = tr
    ro_n = int(1.6 * SR)
    ro = bandpass_noise(ro_n, 500, 700) * np.linspace(0.1, 0.9, ro_n)
    ro_tone = beep(196.0, 1.2, "sine", 2.0) * 0.3
    ro[:len(ro_tone)] += ro_tone[:ro_n]
    S["rift/open"] = echo(ro, 0.2, 0.5, 0.4)

    # ---- records (the trailer's two sung motifs, as Decayed Jukebox tracks) -------------
    # "We Have Been Changed" — a changed choir, e-minor lament
    mel = ([(64, 1), (67, 1), (71, 2), (69, 1), (67, 1), (66, 2), (64, 2),
            (62, 1), (64, 1), (67, 2), (64, 1), (62, 1), (59, 4),
            (64, 1), (64, 1), (66, 1), (67, 1), (71, 2), (74, 2), (71, 2), (67, 4)] * 2)
    bass = ([(40, 2), (0, 2)] * 22)
    track_a = padmix(chiptune(mel, 96, "lullaby", 0.5), echo(chiptune(bass, 96, "pluck", 0.3), 0.12, 0.3, 0.2))
    track_a = echo(track_a, 0.3, 0.35, 0.25)
    S["record/we_have_been_changed"] = loopable(track_a, 0.05)

    # "Ships to Carry Us Home" — a lullaby waltz over a faraway sea
    mel_b = ([(60, 1), (64, 1), (67, 2), (67, 1), (69, 2), (67, 3),
              (64, 1), (67, 1), (72, 2), (72, 1), (74, 2), (72, 3),
              (67, 1), (69, 1), (72, 2), (71, 1), (69, 2), (64, 3), (60, 3)] * 2)
    bass_b = ([(36, 1.5), (0, 1.5)] * 26)
    track_b = padmix(chiptune(mel_b, 112, "bell", 0.4), chiptune(bass_b, 112, "pluck", 0.25))
    sea = bandpass_noise(len(track_b), 320, 260) * 0.06
    track_b = echo((track_b + sea), 0.28, 0.4, 0.3)
    S["record/ships_to_carry_us_home"] = loopable(track_b, 0.05)

    # ---- v1.5 records: the channel's soundtrack suite ----------------------------------
    # "The Signal (tape rip)" — the counting song: cold b-minor ostinato under a two-tone
    # lighthouse call, like somebody made a metronome out of weather.
    mel_s = ([(59, 1), (62, 1), (66, 2), (62, 1), (66, 2), (69, 1), (66, 1), (62, 2),
              (59, 1), (62, 1), (66, 1), (69, 1), (74, 2), (71, 2), (66, 4)] * 2)
    bass_s = ([(35, 2), (0, 1), (35, 1), (0, 4)] * 9)
    tape_hiss = bandpass_noise(int(20 * SR), 3400, 900) * 0.045
    track_s = padmix(chiptune(mel_s, 84, "pluck", 0.45), chiptune(bass_s, 84, "pluck", 0.3),
                     tape_hiss * np.linspace(0.4, 1.0, int(20 * SR)) ** 2)
    S["record/signal_tape"] = loopable(echo(track_s, 0.22, 0.4, 0.3), 0.05)

    # "EAOIN, Sing" — the AI learns a choir: four ascending whole tones that never resolve,
    # sung by a pad that keeps correcting its own pitch upwards by exactly half a breatĥ.
    mel_e = ([(57, 2), (59, 2), (61, 2), (63, 2), (66, 4),
              (66, 2), (63, 2), (61, 2), (59, 2), (57, 4)] * 2)
    choir_pad = drone([220.0, 277.2, 329.6], 26.0, detune=0.45, bright=0.5)
    track_e = padmix(chiptune(mel_e, 74, "lullaby", 0.5), choir_pad * 0.35,
                     echo(chiptune(mel_e, 74, "bell", 0.2), 0.33, 0.5, 0.45))
    S["record/eaoin"] = loopable(echo(track_e, 0.34, 0.42, 0.35), 0.05)

    # "Countdown" — 10 major fourths, each one floor lower, each one louder, then one
    # note too many because something out there just ticked instead of tocked.
    mel_c = ([(48, 1), (55, 1), (47, 1), (54, 1), (46, 1), (53, 1),
              (45, 1), (52, 1), (44, 1), (51, 1), (43, 1), (50, 2), (43, 6)] )
    ticks = np.zeros(int(24 * SR)); pos = 0
    while pos + int(0.5 * SR) < len(ticks):
        ticks[pos:pos + int(0.03 * SR)] += beep(1200, 0.03, "sine", 6.0) * 0.25
        pos += int(0.5 * SR)
    track_c = padmix(chiptune(mel_c, 60, "pluck", 0.5),
                     drone([36.7, 73.4], 24.0, pulse_hz=0.5, bright=0.4) * 0.6,
                     ticks)
    S["record/countdown"] = loopable(echo(track_c, 0.2, 0.32, 0.22), 0.05)

    # "Outside The Quarantine" — wistful slow waltz in the minor that tips major once,
    # realised by a broadcast transmitter that has opinions about melancholy.
    mel_q = ([(57, 1), (60, 1), (64, 2), (65, 3), (64, 1), (62, 2), (60, 3),
              (57, 1), (59, 1), (60, 2), (62, 3), (60, 1), (59, 2), (57, 3)] * 2)
    waltz_bass = ([(33, 1), (45, 1), (52, 1)] * 18)
    sweep = bandpass_noise(int(22 * SR), 900, 1600) * (0.5 + 0.5 * np.sin(np.linspace(0, 8 * math.pi, int(22 * SR)))) * 0.05
    track_q = padmix(chiptune(mel_q, 92, "bell", 0.45), chiptune(waltz_bass, 92, "pluck", 0.28), sweep)
    S["record/quarantine"] = loopable(echo(track_q, 0.3, 0.4, 0.3), 0.05)

    return S


# ======================================================================================
#  JAVA JSON scaffolding (blockstates, block models, item models)
# ======================================================================================

NS = "devouring_storms"
BLOCKS_CUBE_ALL = [
    "corrupted_command_block", "mainframe_frame", "decayed_soil", "decayed_stone", "decay_block",
    "glitch_block", "vhs_jukebox", "crate_block",
]
ITEM_NAMES = [
    "corrupted_blueprints", "rift_key", "formidibomb", "watcher_eye", "amulet_of_decay",
    "memory_fragment", "storm_flesh", "decayed_flesh", "decayed_bone", "tendril", "storm_dust",
    "commanded_star", "withered_nether_star", "music_disc_changed", "music_disc_ships",
    "music_disc_signal_tape", "music_disc_eaoin", "music_disc_countdown", "music_disc_quarantine",
    "broken_record", "rocket_key",
    "endertown_core", "classified_payload", "storm_killer", "storm_heart", "seventh_trumpet",
    "audio_log_1", "audio_log_2", "audio_log_3",
    "schedule_1", "schedule_2", "schedule_3", "schedule_4", "schedule_5", "schedule_6", "schedule_7",
]
EGG_NAMES = [
    "massg_spawn_egg", "watcher_spawn_egg", "tazo_spawn_egg", "anna_spawn_egg",
    "severed_storm_spawn_egg", "withered_symbiont_spawn_egg",
    "preacher_spawn_egg", "townsfolk_spawn_egg",
    "storm_mite_spawn_egg", "the_taken_spawn_egg", "travis_spawn_egg",
    "tonya_spawn_egg", "void_maw_spawn_egg",
    "creator_spawn_egg", "monstrosity_spawn_egg", "forger_spawn_egg",
    "cart_shopper_spawn_egg", "researcher_spawn_egg", "earth_eater_spawn_egg",
]
BLOCK_ITEMS = [
    "corrupted_command_block", "terminal", "mainframe_frame", "rift_portal",
    "decayed_jukebox", "decayed_soil", "decayed_stone", "rot_log", "decay_block", "sealed_vault",
    "frayed_tear", "glitch_block", "vhs_jukebox", "crate_block",
]


def write_java_scaffolding() -> None:
    bsd = JAVA_ASSETS / "blockstates"
    bmd = JAVA_ASSETS / "models" / "block"
    imd = JAVA_ASSETS / "models" / "item"
    itd = JAVA_ASSETS / "items"

    def blockstate(name, model):
        write_json({"variants": {"": {"model": f"{NS}:block/{model}"}}}, bsd / f"{name}.json")

    for name in BLOCKS_CUBE_ALL:
        blockstate(name, name)
        write_json({"parent": "minecraft:block/cube_all",
                    "textures": {"all": f"{NS}:block/{name}"}}, bmd / f"{name}.json")

    # rot log — axis variants
    write_json({"variants": {
        "axis=y": {"model": f"{NS}:block/rot_log"},
        "axis=x": {"model": f"{NS}:block/rot_log", "x": 90},
        "axis=z": {"model": f"{NS}:block/rot_log", "z": 90},
    }}, bsd / "rot_log.json")
    write_json({"parent": "minecraft:block/cube_column",
                "textures": {"end": f"{NS}:block/rot_log_top", "side": f"{NS}:block/rot_log"}},
               bmd / "rot_log.json")

    # terminal — inactive/active variants
    write_json({"variants": {
        "active=false": {"model": f"{NS}:block/terminal"},
        "active=true": {"model": f"{NS}:block/terminal_active"},
    }}, bsd / "terminal.json")
    write_json({"parent": "minecraft:block/cube_all",
                "textures": {"all": f"{NS}:block/terminal"}}, bmd / "terminal.json")
    write_json({"parent": "minecraft:block/cube_all",
                "textures": {"all": f"{NS}:block/terminal_active"}}, bmd / "terminal_active.json")

    # rift portal
    blockstate("rift_portal", "rift_portal")
    write_json({"parent": "minecraft:block/cube_all",
                "textures": {"all": f"{NS}:block/rift_portal"}}, bmd / "rift_portal.json")

    # decayed jukebox — vanilla jukebox parent with our textures
    write_json({"variants": {
        "has_record=false": {"model": f"{NS}:block/decayed_jukebox"},
        "has_record=true": {"model": f"{NS}:block/decayed_jukebox"},
    }}, bsd / "decayed_jukebox.json")
    write_json({"parent": "minecraft:block/cube_column",
                "textures": {"end": f"{NS}:block/decayed_jukebox_top", "side": f"{NS}:block/decayed_jukebox_side"}},
               bmd / "decayed_jukebox.json")

    # item models (legacy models/ + modern items/ defs)
    for name in ITEM_NAMES + EGG_NAMES:
        write_json({"parent": "minecraft:item/generated",
                    "textures": {"layer0": f"{NS}:item/{name}"}}, imd / f"{name}.json")
        write_json({"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}},
                   itd / f"{name}.json")
    for name in BLOCK_ITEMS:
        write_json({"parent": f"{NS}:block/{name}"}, imd / f"{name}.json")
        write_json({"model": {"type": "minecraft:model", "model": f"{NS}:block/{name}"}},
                   itd / f"{name}.json")

    # particle definition
    write_json({"textures": [f"{NS}:glitch"]}, JAVA_ASSETS / "particles" / "glitch.json")

    # animated texture mcmeta
    write_json({"animation": {"frametime": 2, "interpolate": False}},
               JAVA_ASSETS / "textures" / "block" / "rift_portal.png.mcmeta")


# ======================================================================================
#  MAIN
# ======================================================================================

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--skip-existing", action="store_true", help="don't overwrite existing png/ogg files")
    args = parser.parse_args()
    SKIP.skip = args.skip_existing

    print("== Devouring Storms asset generation ==")

    # ---------- Java block textures ----------
    jt = JAVA_ASSETS / "textures"
    write_png(block_corrupted_command_block(), jt / "block" / "corrupted_command_block.png")
    write_png(block_terminal(False), jt / "block" / "terminal.png")
    write_png(block_terminal(True), jt / "block" / "terminal_active.png")
    write_png(block_mainframe_frame(), jt / "block" / "mainframe_frame.png")
    write_png(block_rift_portal_frames(), jt / "block" / "rift_portal.png")
    write_png(block_decayed_jukebox(True), jt / "block" / "decayed_jukebox_top.png")
    write_png(block_decayed_jukebox(False), jt / "block" / "decayed_jukebox_side.png")
    write_png(block_decayed_soil(), jt / "block" / "decayed_soil.png")
    write_png(block_decayed_stone(), jt / "block" / "decayed_stone.png")
    write_png(block_rot_log(True), jt / "block" / "rot_log.png")
    write_png(block_rot_log(False), jt / "block" / "rot_log_top.png")
    write_png(block_decay(), jt / "block" / "decay_block.png")
    write_png(block_glitch(), jt / "block" / "glitch_block.png")
    write_png(block_vhs_jukebox(), jt / "block" / "vhs_jukebox.png")
    write_png(block_crate(), jt / "block" / "crate_block.png")
    write_png(block_sealed_vault(False), jt / "block" / "sealed_vault.png")
    write_png(block_sealed_vault(True), jt / "block" / "sealed_vault_open.png")
    write_png(block_frayed_tear(), jt / "block" / "frayed_tear.png")

    # ---------- Java item textures ----------
    write_png(item_corrupted_blueprints(), jt / "item" / "corrupted_blueprints.png")
    write_png(item_rift_key(), jt / "item" / "rift_key.png")
    write_png(item_formidibomb(), jt / "item" / "formidibomb.png")
    write_png(item_watcher_eye(), jt / "item" / "watcher_eye.png")
    write_png(item_amulet(), jt / "item" / "amulet_of_decay.png")
    write_png(item_memory_fragment(), jt / "item" / "memory_fragment.png")
    write_png(flesh_texture((120, 50, 70), (70, 20, 40)), jt / "item" / "storm_flesh.png")
    write_png(flesh_texture((86, 62, 44), (50, 32, 22)), jt / "item" / "decayed_flesh.png")
    write_png(flesh_texture((150, 146, 138), (90, 80, 74)), jt / "item" / "decayed_bone.png")
    write_png(item_tendril(), jt / "item" / "tendril.png")
    write_png(item_storm_dust(), jt / "item" / "storm_dust.png")
    write_png(star_texture((255, 220, 120), (180, 60, 220)), jt / "item" / "commanded_star.png")
    write_png(star_texture((200, 200, 210), (90, 90, 110)), jt / "item" / "withered_nether_star.png")
    write_png(disc_texture((30, 18, 40), (140, 90, 200)), jt / "item" / "music_disc_changed.png")
    write_png(disc_texture((20, 26, 44), (90, 160, 220)), jt / "item" / "music_disc_ships.png")
    write_png(disc_texture((16, 16, 20), (220, 220, 225)), jt / "item" / "music_disc_signal_tape.png")
    write_png(disc_texture((24, 10, 30), (150, 220, 255)), jt / "item" / "music_disc_eaoin.png")
    write_png(disc_texture((34, 8, 8), (255, 120, 60)), jt / "item" / "music_disc_countdown.png")
    write_png(disc_texture((10, 26, 22), (120, 255, 170)), jt / "item" / "music_disc_quarantine.png")
    write_png(disc_texture((8, 6, 8), (10, 8, 10)), jt / "item" / "broken_record.png")
    write_png(item_rocket_key(), jt / "item" / "rocket_key.png")
    write_png(item_endertown_core(), jt / "item" / "endertown_core.png")
    write_png(item_classified_payload(), jt / "item" / "classified_payload.png")
    write_png(item_storm_killer(), jt / "item" / "storm_killer.png")
    write_png(item_storm_heart(), jt / "item" / "storm_heart.png")
    write_png(item_seventh_trumpet(), jt / "item" / "seventh_trumpet.png")
    for log in range(3):
        write_png(item_audio_log(log), jt / "item" / f"audio_log_{log + 1}.png")
    for sched in range(1, 8):
        write_png(item_schedule(sched), jt / "item" / f"schedule_{sched}.png")
    eggs = {
        "massg_spawn_egg": ((60, 24, 84), (220, 100, 255)),
        "watcher_spawn_egg": ((10, 8, 14), (200, 230, 255)),
        "tazo_spawn_egg": ((30, 90, 100), (180, 240, 250)),
        "anna_spawn_egg": ((220, 212, 228), (120, 90, 150)),
        "severed_storm_spawn_egg": ((40, 18, 60), (160, 60, 220)),
        "withered_symbiont_spawn_egg": ((70, 60, 40), (140, 190, 90)),
        "preacher_spawn_egg": ((52, 22, 66), (226, 214, 238)),
        "townsfolk_spawn_egg": ((40, 56, 52), (214, 188, 158)),
        "storm_mite_spawn_egg": ((58, 15, 92), (176, 96, 255)),
        "the_taken_spawn_egg": ((31, 10, 51), (63, 215, 200)),
        "travis_spawn_egg": ((28, 42, 72), (150, 220, 255)),
        "tonya_spawn_egg": ((188, 210, 232), (240, 244, 252)),
        "void_maw_spawn_egg": ((4, 3, 8), (240, 220, 255)),
        "creator_spawn_egg": ((12, 8, 20), (255, 60, 60)),
        "monstrosity_spawn_egg": ((58, 18, 74), (255, 63, 200)),
        "forger_spawn_egg": ((20, 14, 30), (150, 220, 255)),
        "cart_shopper_spawn_egg": ((40, 48, 52), (220, 220, 220)),
        "researcher_spawn_egg": ((230, 230, 235), (40, 90, 40)),
        "earth_eater_spawn_egg": ((4, 3, 8), (63, 215, 200)),
    }
    for name, (base, spots) in eggs.items():
        write_png(egg_texture(base, spots), jt / "item" / f"{name}.png")

    # ---------- entity skins ----------
    write_png(skin_massg(), jt / "entity" / "massg.png")
    write_png(skin_watcher(), jt / "entity" / "watcher.png")
    write_png(skin_tazo(), jt / "entity" / "tazo.png")
    write_png(skin_anna(), jt / "entity" / "anna.png")
    write_png(skin_symbiont(), jt / "entity" / "symbiont.png")
    write_png(skin_preacher(), jt / "entity" / "preacher.png")
    write_png(skin_townsfolk(), jt / "entity" / "townsfolk.png")
    write_png(skin_storm_mite(), jt / "entity" / "storm_mite.png")
    write_png(skin_the_taken(), jt / "entity" / "the_taken.png")
    write_png(skin_travis(), jt / "entity" / "travis.png")
    write_png(skin_tonya(), jt / "entity" / "tonya.png")
    write_png(skin_void_maw(), jt / "entity" / "void_maw.png")
    write_png(skin_creator(), jt / "entity" / "creator.png")
    write_png(skin_creator_hand(), jt / "entity" / "creator_hand.png")
    write_png(skin_monstrosity(), jt / "entity" / "monstrosity.png")
    write_png(skin_forger(), jt / "entity" / "forger.png")
    write_png(skin_sky_tentacle(), jt / "entity" / "sky_tentacle.png")
    write_png(skin_cart_shopper(), jt / "entity" / "cart_shopper.png")
    write_png(skin_researcher(), jt / "entity" / "researcher.png")
    write_png(skin_earth_eater(), jt / "entity" / "earth_eater.png")

    # ---------- particle + environment + icon ----------
    glitch = tex_noise(16, (180, 40, 220), jitter=60)
    blotches(glitch, (0, 240, 255), 6, 1, 3)
    write_png(glitch, jt / "particle" / "glitch.png")

    rift = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    rp = rift.load()
    for y in range(128):
        for x in range(128):
            dx, dy = x - 64, y - 64
            r = math.hypot(dx, dy) / 64
            if r > 1:
                continue
            swirl = 0.5 + 0.5 * math.sin(r * 18 - math.atan2(dy, dx) * 4)
            a = int(255 * max(0.0, 1.0 - r) * (0.5 + 0.5 * swirl))
            rp[x, y] = (int(200 + 55 * (1 - r)), int(90 + 60 * swirl), 255, a)
    write_png(rift, jt / "environment" / "rift.png")

    icon = Image.new("RGBA", (128, 128), (12, 6, 20, 255))
    dr = ImageDraw.Draw(icon)
    dr.ellipse([16, 16, 112, 112], outline=(120, 40, 160, 255), width=4)
    dr.ellipse([40, 40, 88, 88], fill=(60, 20, 90, 255), outline=(220, 100, 255, 255), width=3)
    dr.ellipse([56, 56, 72, 72], fill=(255, 120, 240, 255))
    for _ in range(90):
        x, y = int(rng.integers(4, 124)), int(rng.integers(4, 124))
        dr.point([(x, y)], fill=(int(rng.integers(80, 200)), 40, int(rng.integers(120, 255)), 255))
    write_png(icon, JAVA_ASSETS / "icon.png")

    # ---------- sounds ----------
    print("== synthesizing sound bank ==")
    sounds = build_sounds()
    for name, data in sounds.items():
        write_ogg(data, JAVA_ASSETS / "sounds" / f"{name}.ogg")

    # ---------- java json scaffolding ----------
    write_java_scaffolding()

    # ==================================================================================
    #  BEDROCK RP — textures reuse the same generators; sounds are byte-copies
    # ==================================================================================
    print("== bedrock resource pack ==")
    bt = BEDROCK_RP / "textures"

    block_map = {
        "corrupted_command_block": block_corrupted_command_block(),
        "terminal": block_terminal(True),
        "mainframe_frame": block_mainframe_frame(),
        "rift_portal": block_rift_portal_frames(1).crop((0, 0, 16, 16)),
        "decayed_jukebox_top": block_decayed_jukebox(True),
        "decayed_jukebox_side": block_decayed_jukebox(False),
        "decayed_soil": block_decayed_soil(),
        "decayed_stone": block_decayed_stone(),
        "rot_log": block_rot_log(True),
        "rot_log_top": block_rot_log(False),
        "decay_block": block_decay(),
        "sealed_vault": block_sealed_vault(False),
        "sealed_vault_open": block_sealed_vault(True),
        "glitch_block": block_glitch(),
        "vhs_jukebox": block_vhs_jukebox(),
        "crate_block": block_crate(),
        "frayed_tear": block_frayed_tear(),
    }
    # the Decayed Reality palette bleeds into the End itself
    block_map["end_stone"] = block_decayed_stone()
    for name, img in block_map.items():
        write_png(img, bt / "blocks" / f"{name}.png")

    for name in ITEM_NAMES:
        src = jt / "item" / f"{name}.png"
        write_png(Image.open(src), bt / "items" / f"{name}.png")

    for name in ("massg", "watcher", "tazo", "anna", "symbiont", "preacher", "townsfolk",
                 "storm_mite", "the_taken", "travis", "tonya", "void_maw"):
        write_png(Image.open(jt / "entity" / f"{name}.png"), bt / "entity" / f"{name}.png")
    write_png(Image.open(jt / "entity" / "massg.png"), bt / "entity" / "severed.png")
    write_png(skin_massg_glow(), bt / "entity" / "massg_glow.png")
    write_png(Image.open(jt / "particle" / "glitch.png"), bt / "particle" / "glitch.png")

    # pack icon (256px, bedrock loves bigger icons)
    write_png(icon.resize((256, 256), Image.NEAREST), BEDROCK_RP / "pack_icon.png")
    write_png(icon.resize((256, 256), Image.NEAREST), REPO / "bedrock-addon" / "DevouringStormsBP" / "pack_icon.png")

    # terrain/item short-name registries
    write_json({"resource_pack_name": "devouring_storms",
                "texture_name": "atlas.terrain",
                "padding": 8, "num_mip_levels": 4,
                "texture_data": {f"ds_{name}": {"textures": f"textures/blocks/{name}"}
                                  for name in block_map}},
               BEDROCK_RP / "textures" / "terrain_texture.json")
    write_json({"resource_pack_name": "devouring_storms",
                "texture_name": "atlas.items",
                "texture_data": {f"ds_{name}": {"textures": f"textures/items/{name}"}
                                  for name in ITEM_NAMES}},
               BEDROCK_RP / "textures" / "item_texture.json")

    # sounds: copy oggs into RP/sounds/ds/
    for name in sounds:
        dst = BEDROCK_RP / "sounds" / "ds" / (name.split("/")[-1] + ".ogg")
        if not (SKIP.skip and dst.exists()):
            src = JAVA_ASSETS / "sounds" / f"{name}.ogg"
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(src, dst)
            print(f"  ogg  {dst.relative_to(REPO)}")

    print("Done. The storm is ready to be packed.")


if __name__ == "__main__":
    main()
