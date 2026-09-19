#!/usr/bin/env python3
"""The painted skies: one cube per dimension, drawn here, not by the shader pack.

WHY THIS EXISTS. "custom skyboxes" and "the sky is not fully the sky yet" have been
on the list for a long time, and every shader-side answer has the same hole: a
player without the shader pack sees none of it, and a shader pack can only tint
the vanilla sky, not replace it.

So the mod paints its own sky. Six faces per dimension -- four sides and a zenith,
the under-horizon is the sky floor band's job -- as ordinary PNGs in the content
pack, drawn as a camera-anchored cube by
mcsm-extras/java/net/mcsm/extras/client/McsmPaintedSky.java. No dome is involved
anywhere (the dome ban stands): the geometry is a box lid with four walls, and the
texture is a painted panorama.

THE THREE SKIES, and they are three different places, not one palette re-used:

  decayed  the decayed reality: ash-teal air over an ochre band, painted cloud
           bars, faint violet tears in it, and a violet zenith
  adams    the infinite dimension: amethyst air, a warm city glow along the
           horizon, and constellations above it
  void     the void: near-black violet, magenta rift streaks crossing the whole
           sky, a cold green haze low down, and the densest starfield of the three

Usage: python3 ci/make_skybox_textures.py [--check]
"""
from __future__ import annotations

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pngutil import write_png  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT_SKY = os.path.join(ROOT, "jar-overrides", "assets", "mcsm", "textures", "sky")

SIDE_W, SIDE_H = 512, 128
TOP_W, TOP_H = 256, 256
# BUILD #470 -- THE REAL CLOUD DECK. The cube is the air; this is the weather in
# it. One tileable sheet per dimension, drawn as three stacked, drifting layers in
# the world (McsmCloudDeck), because "the sky is not fully the sky yet" is mostly
# about the clouds: a full sky paints them, and the vanilla plane is one flat
# sheet of one colour, the same in every world.
CLOUD_W, CLOUD_H = 128, 128

DIMENSIONS = {
    "decayed": dict(
        seed=11,
        horizon=(0x6E, 0x3E, 0x2A),   # the ochre band the old world ends on
        mid=(0x2C, 0x2A, 0x2E),       # ash grey air
        zenith=(0x2A, 0x1C, 0x4E),    # the violet above it
        glow=(0x8A, 0x5C, 0xFF),
        cloud=0.62, stars=70, rift=0x8A5CFF, rift_p=0.0010,
    ),
    "adams": dict(
        seed=23,
        horizon=(0x5C, 0x2E, 0x86),   # amethyst air
        mid=(0x28, 0x18, 0x44),
        zenith=(0x12, 0x0C, 0x24),
        glow=(0xFF, 0xC2, 0x6E),      # the city's lamps, warm, low down
        cloud=0.34, stars=150, rift=0xC8, rift_p=0.0008,
    ),
    "void": dict(
        seed=37,
        horizon=(0x1E, 0x16, 0x42),   # the void's own violet fog
        mid=(0x10, 0x0A, 0x22),
        zenith=(0x06, 0x04, 0x10),
        glow=(0x7C, 0xFF, 0xB0),      # the cold green haze at the bottom
        cloud=0.10, stars=260, rift=0xFF3CE6, rift_p=0.0016,
    ),
    # BUILD #462 -- the Creator's reach: the one sky in the mod that is already
    # day. Gold at the horizon, pale blue above it, and almost no stars, because
    # nothing is hidden in this place.
    "creator": dict(
        seed=51,
        horizon=(0xFF, 0xF0, 0xC0),   # the gold band the reach ends on
        mid=(0x74, 0x84, 0xC0),       # and blue air above it, dimmer than the band
        zenith=(0x44, 0x52, 0x9A),    # deepening all the way up
        glow=(0xFF, 0xF3, 0xC4),
        cloud=0.44, stars=24, rift=0xFFE9B0, rift_p=0.0006,
    ),
}


class Rand:
    """Deterministic PRNG, the same one the block textures use."""

    def __init__(self, seed):
        self.s = (seed * 2654435761) & 0xFFFFFFFF

    def next(self):
        self.s = (self.s * 1664525 + 1013904223) & 0xFFFFFFFF
        return (self.s >> 8) & 0xFFFFFF

    def frac(self):
        return self.next() / float(0xFFFFFF)

    def chance(self, p):
        return self.frac() < p


def lerp(a, b, t):
    return (int(a[0] + (b[0] - a[0]) * t),
            int(a[1] + (b[1] - a[1]) * t),
            int(a[2] + (b[2] - a[2]) * t))


def sky_pixel(spec, x, y, w, h, rng):
    """One painted pixel: a vertical gradient, a horizon glow, clouds, stars."""
    v = 1.0 - (y / float(max(1, h - 1)))          # 0 at the top, 1 at the horizon
    if v < 0.45:
        c = lerp(spec["zenith"], spec["mid"], v / 0.45)
    else:
        c = lerp(spec["mid"], spec["horizon"], (v - 0.45) / 0.55)
    # grain: no sky in the mod is a flat fill
    n = (rng.frac() - 0.5) * 14.0
    c = (max(0, min(255, c[0] + n)), max(0, min(255, c[1] + n)), max(0, min(255, c[2] + n)))
    # the horizon haze: it starts low, deepens into the last fifth, and varies
    # along the way round, so the ring is air rather than a painted stripe
    if v > 0.72:
        t = (v - 0.72) / 0.28
        t = t * t
        wobble = 0.78 + 0.22 * ((x % 97) / 97.0)
        c = lerp(c, spec["glow"], t * 0.55 * wobble)
    # painted cloud bars, only where the dimension has air
    if spec["cloud"] > 0.0 and 0.25 < v < 0.92:
        band = ((x * 0.045 + y * 0.30) % 6.0) / 6.0
        if band < spec["cloud"] * 0.5:
            lit = lerp(c, (0xFF, 0xFF, 0xFF), 0.12 * spec["cloud"])
            c = lit
    return c, v


def paint_sides(spec, w=SIDE_W, h=SIDE_H):
    rng = Rand(spec["seed"])
    px = []
    for y in range(h):
        for x in range(w):
            c, v = sky_pixel(spec, x, y, w, h, rng)
            px.append((c[0], c[1], c[2], 255))
    # stars above the clouds
    for _ in range(spec["stars"]):
        x = rng.int(0, w - 1) if hasattr(rng, "int") else int(rng.frac() * w)
        y = int(rng.frac() * h * 0.55)
        b = 150 + int(rng.frac() * 105)
        px[y * w + x] = (b, b, min(255, b + 20), 255)
    # rifts: short bright streaks, the tears in reality
    for _ in range(int(spec["rift_p"] * w * h)):
        x = int(rng.frac() * w)
        y = int(rng.frac() * h * 0.8)
        length = 4 + int(rng.frac() * 10)
        r, g, b = (spec["rift"] >> 16) & 255, (spec["rift"] >> 8) & 255, spec["rift"] & 255
        if spec["rift"] == 0xC8:
            r = g = b = 0xC8
        for i in range(length):
            yy = y + i
            if yy >= h:
                break
            f = 1.0 - i / float(length)
            c = lerp((px[yy * w + x][0], px[yy * w + x][1], px[yy * w + x][2]), (r, g, b), f * 0.85)
            px[yy * w + x] = (c[0], c[1], c[2], 255)
    return px


def paint_top(spec, w=TOP_W, h=TOP_H):
    """The zenith: the same air, seen from underneath, with the stars concentrated."""
    rng = Rand(spec["seed"] + 5)
    px = []
    for y in range(h):
        for x in range(w):
            # radial: the middle of the lid is the deepest part of the sky
            dx = (x / float(w)) * 2.0 - 1.0
            dy = (y / float(h)) * 2.0 - 1.0
            r = min(1.0, (dx * dx + dy * dy) ** 0.5)
            c = lerp(spec["zenith"], spec["mid"], r * 0.85)
            n = (rng.frac() - 0.5) * 10.0
            c = (max(0, min(255, c[0] + n)), max(0, min(255, c[1] + n)), max(0, min(255, c[2] + n)))
            px.append((c[0], c[1], c[2], 255))
    for _ in range(spec["stars"] * 2):
        x = int(rng.frac() * w)
        y = int(rng.frac() * h)
        b = 160 + int(rng.frac() * 95)
        px[y * w + x] = (b, b, min(255, b + 24), 255)
    return px


def _cloud_field(seed, cells, w, h):
    """Tileable value noise: a coarse grid, smoothed, wrapping at the edges.

    Wrapping is the whole point -- the deck scrolls its UVs, so a seam is a line
    of hard edge crawling across the sky.
    """
    rng = Rand(seed)
    grid = [[rng.frac() for _ in range(cells)] for _ in range(cells)]
    out = []
    for y in range(h):
        fy = y * cells / float(h)
        y0 = int(fy) % cells
        y1 = (y0 + 1) % cells
        ty = fy - int(fy)
        sy = ty * ty * (3.0 - 2.0 * ty)
        row = []
        for x in range(w):
            fx = x * cells / float(w)
            x0 = int(fx) % cells
            x1 = (x0 + 1) % cells
            tx = fx - int(fx)
            sx = tx * tx * (3.0 - 2.0 * tx)
            a = grid[y0][x0] * (1.0 - sx) + grid[y0][x1] * sx
            b = grid[y1][x0] * (1.0 - sx) + grid[y1][x1] * sx
            row.append(a * (1.0 - sy) + b * sy)
        out.append(row)
    return out


def paint_clouds(spec, w=CLOUD_W, h=CLOUD_H):
    """One dimension's cloud sheet: dense where `cloud` is high, thin where it is not.

    RGB is the world's own light caught in the cloud (its glow pulled most of the
    way to white); ALPHA is the cloud itself, so the same sheet reads as a solid
    overcast in the decayed reality and as torn wisps in the void. Tileable in both
    axes, and never opaque: a deck is weather, not a lid.
    """
    coarse = _cloud_field(spec["seed"] * 7 + 1, 8, w, h)
    fine = _cloud_field(spec["seed"] * 13 + 5, 24, w, h)
    density = float(spec["cloud"])          # 0.10 .. 0.62
    glow = spec["glow"]
    # clouds catch the world's light: the glow, pulled toward white
    cr = int(glow[0] + (255 - glow[0]) * 0.6)
    cg = int(glow[1] + (255 - glow[1]) * 0.6)
    cb = int(glow[2] + (255 - glow[2]) * 0.6)
    cut = 0.62 - density * 0.45             # the denser the world's cloud, the lower the cut
    px = []
    for y in range(h):
        for x in range(w):
            f = coarse[y][x] * 0.68 + fine[y][x] * 0.32
            d = (f - cut) / max(0.08, 1.0 - cut)
            if d <= 0.0:
                px.append((cr, cg, cb, 0))
                continue
            if d > 1.0:
                d = 1.0
            alpha = int(40 + d * 175)       # 40..215: present, still air, never a lid
            # the thick cores go whiter, the edges keep the world's colour
            t = min(1.0, d * 1.35)
            px.append((int(cr + (255 - cr) * t),
                       int(cg + (255 - cg) * t),
                       int(cb + (255 - cb) * t),
                       alpha))
    return px

def sky_files():
    return {
        "decayed_sides": (SIDE_W, SIDE_H, lambda: paint_sides(DIMENSIONS["decayed"])),
        "decayed_top": (TOP_W, TOP_H, lambda: paint_top(DIMENSIONS["decayed"])),
        "adams_sides": (SIDE_W, SIDE_H, lambda: paint_sides(DIMENSIONS["adams"])),
        "adams_top": (TOP_W, TOP_H, lambda: paint_top(DIMENSIONS["adams"])),
        "void_sides": (SIDE_W, SIDE_H, lambda: paint_sides(DIMENSIONS["void"])),
        "void_top": (TOP_W, TOP_H, lambda: paint_top(DIMENSIONS["void"])),
        "creator_sides": (SIDE_W, SIDE_H, lambda: paint_sides(DIMENSIONS["creator"])),
        "creator_top": (TOP_W, TOP_H, lambda: paint_top(DIMENSIONS["creator"])),
        # BUILD #470 -- and the weather in that air: the cloud deck, one sheet per
        # dimension (the same seeds as its cube, so its sky is one place).
        "clouds_decayed": (CLOUD_W, CLOUD_H, lambda: paint_clouds(DIMENSIONS["decayed"])),
        "clouds_adams": (CLOUD_W, CLOUD_H, lambda: paint_clouds(DIMENSIONS["adams"])),
        "clouds_void": (CLOUD_W, CLOUD_H, lambda: paint_clouds(DIMENSIONS["void"])),
        "clouds_creator": (CLOUD_W, CLOUD_H, lambda: paint_clouds(DIMENSIONS["creator"])),
    }


def emit(check_only=False):
    os.makedirs(OUT_SKY, exist_ok=True)
    missing = 0
    written = 0
    for name, (w, h, maker) in sorted(sky_files().items()):
        path = os.path.join(OUT_SKY, name + ".png")
        if check_only:
            if not os.path.isfile(path):
                print("missing sky face:", name)
                missing += 1
            continue
        write_png(path, w, h, maker())
        written += 1
    if check_only:
        if missing:
            print("[sky] %d MISSING" % missing)
            return 1
        print("[sky] all %d painted sky faces present (%d dimensions: sides + zenith "
              "+ cloud deck)" % (len(sky_files()), len(DIMENSIONS)))
        return 0
    print("[sky] wrote %d painted sky faces for %d dimensions (%dx%d sides, %dx%d zenith, "
          "%dx%d cloud deck)" % (written, len(DIMENSIONS), SIDE_W, SIDE_H, TOP_W, TOP_H,
                                 CLOUD_W, CLOUD_H))
    return 0


def main():
    return emit(check_only="--check" in sys.argv[1:])


if __name__ == "__main__":
    sys.exit(main())
