#!/usr/bin/env python3
"""Generate the Devouring Storms content pack TEXTURES (mandate D.8, phase 1+5).

WHY THIS EXISTS
---------------
Phase 1 shipped the content pack with its models pointing at placeholder
textures borrowed from the base mod and from vanilla -- and NO textures of its
own. That works for the *models*, and it is why the pack built and passed every
gate, but it is not what a player sees: an item whose texture is borrowed
renders as the borrowed texture, and an item with no resolvable model at all
renders as the missing-model cube (the black/magenta glitch block).

So this script draws the pack's own art, in the palette the user supplied: the
stage sheets (wither_storm_stage_a/b.png) are the ground truth for the storm's
material, and their colour family -- near-black navy, ash grey, cold violet --
is what the decayed reality is made of.

  * 30 block textures (16x16): decayed world, abandoned-city materials,
    reality-tear materials, and the three city crates.
  * 19 item textures (16x16): shards, ingots, dusts, cards, orbs, the rift key
    and the five weapons.

Everything is deterministic (fixed seeds), so re-running the script is a no-op
byte for byte and the build gate can hash-check it.

Usage: python3 ci/make_mcsm_textures.py [--check]
       --check verifies every expected texture exists (used by the build).
"""
from __future__ import annotations

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pngutil import read_png, write_png  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT_BLOCK = os.path.join(ROOT, "jar-overrides", "assets", "mcsm", "textures", "block")
OUT_ITEM = os.path.join(ROOT, "jar-overrides", "assets", "mcsm", "textures", "item")
SHEET_A = os.path.join(ROOT, "wither_storm_stage_a.png")
SHEET_B = os.path.join(ROOT, "wither_storm_stage_b.png")

# ---------------------------------------------------------------------------
# The palette: the supplied stage sheets, plus four accents that are already in
# the storm's own colour story (the violet lens, the ember of a burning city,
# the toxic green of phase 7, and the cold cyan of a glitch).
# ---------------------------------------------------------------------------

FALLBACK = [
    (0x00, 0x00, 0x00), (0x01, 0x02, 0x05), (0x02, 0x04, 0x0A), (0x03, 0x05, 0x0C),
    (0x04, 0x07, 0x11), (0x05, 0x09, 0x12), (0x06, 0x0B, 0x18), (0x08, 0x0E, 0x1E),
]


def sheet_mean():
    """The supplied sheets' own colour direction (their mean, normalised)."""
    tot = [0.0, 0.0, 0.0]
    n = 0
    for path in (SHEET_A, SHEET_B):
        if not os.path.isfile(path):
            continue
        try:
            _, _, px = read_png(path)
        except Exception:
            continue
        for c in px:
            tot[0] += c[0]
            tot[1] += c[1]
            tot[2] += c[2]
            n += 1
    if not n:
        return (0.26, 0.45, 1.0)
    peak = max(tot) or 1.0
    return (tot[0] / peak, tot[1] / peak, tot[2] / peak)


def sheet_colours():
    """The distinct colours of the supplied stage sheets, dark -> light."""
    found = {}
    for path in (SHEET_A, SHEET_B):
        if not os.path.isfile(path):
            continue
        try:
            _, _, px = read_png(path)
        except Exception:
            continue
        for c in px:
            found[(c[0], c[1], c[2])] = found.get((c[0], c[1], c[2]), 0) + 1
    if not found:
        return list(FALLBACK)
    ordered = sorted(found.items(), key=lambda kv: sum(kv[0]))
    return [c for c, _ in ordered]


SHEET = sheet_colours()
COLD = sheet_mean()

# THE RAMP. The supplied sheets are near-black atmosphere -- their brightest
# colour is #060B18 -- so a ramp built only from those exact values draws a
# texture pack that is invisible in game (the first pass of this script did
# exactly that, and every block came out black). The ramp below therefore keeps
# the sheets' HUE DIRECTION -- the cold navy the whole stage is lit in -- and
# gives it the RANGE a texture needs, from near-black to a cold highlight.
COLD_RAMP = [(int(255 * (0.10 + 0.90 * (i / 15.0)) ** 1.35 * COLD[0]),
              int(255 * (0.06 + 0.94 * (i / 15.0)) ** 1.55 * COLD[1]),
              int(255 * (0.06 + 0.94 * (i / 15.0)) ** 1.55 * COLD[2]))
             for i in range(16)]

# A cooler, flatter ash axis for stone, wood and roads: the same cold family,
# desaturated, so a wall does not read as the same material as a rift.
ASH = (0.74, 0.80, 0.94)
ASH_RAMP = [(int(255 * (0.16 + 0.84 * (i / 15.0)) ** 1.25 * ASH[0]),
             int(255 * (0.08 + 0.84 * (i / 15.0)) ** 1.5 * ASH[1]),
             int(255 * (0.08 + 0.84 * (i / 15.0)) ** 1.5 * ASH[2]))
            for i in range(16)]


_RAMP = None    # set by in_ash()/in_cold(); None means the cold axis


def shade(step, tint=None, lift=0.0, ramp=None):
    """A colour from the pack's ramp, optionally tinted and lifted.

    step: 0.0 = darkest, 1.0 = the cold highlight. The hue is the supplied
    sheets' own colour direction (see COLD/COLD_RAMP above)."""
    s = max(0.0, min(1.0, step))
    table = ramp if ramp is not None else (_RAMP or COLD_RAMP)
    i = int(round(s * (len(table) - 1)))
    r, g, b = table[i]
    if tint:
        r = r * tint[0]
        g = g * tint[1]
        b = b * tint[2]
    if lift:
        r = r + lift
        g = g + lift
        b = b + lift
    return (int(max(0, min(255, r))), int(max(0, min(255, g))), int(max(0, min(255, b))))


def ash(step, tint=None, lift=0.0):
    return shade(step, tint=tint, lift=lift, ramp=ASH_RAMP)


def _on_axis(table, fn):
    """Draw one texture on a given ramp (ash for stone/wood, cold for the rest)."""
    global _RAMP
    previous = _RAMP
    _RAMP = table
    try:
        return fn()
    finally:
        _RAMP = previous


def on_ash(fn):
    return lambda: _on_axis(ASH_RAMP, fn)


def on_cold(fn):
    return lambda: _on_axis(COLD_RAMP, fn)


# accents: the storm's lens violet, rust, phase-7 toxic green, glitch cyan
VIOLET = (0x6A, 0x3C, 0xFF)
VIOLET_DIM = (0x2A, 0x18, 0x76)
RUST = (0x6B, 0x40, 0x28)
RUST_LIT = (0x9A, 0x63, 0x3C)
TOXIC = (0x4C, 0xC8, 0x3A)
GLITCH = (0x3C, 0xE6, 0xFF)
BLOOD = (0x5A, 0x10, 0x18)
WHITE = (0xE8, 0xF2, 0xFF)


class Rand:
    """Deterministic small PRNG (no dependency on the stdlib's sequence)."""

    def __init__(self, seed):
        self.s = (seed * 2654435761) & 0xFFFFFFFF

    def next(self):
        self.s = (self.s * 1664525 + 1013904223) & 0xFFFFFFFF
        return (self.s >> 8) & 0xFFFFFF

    def frac(self):
        return self.next() / float(0xFFFFFF)

    def between(self, lo, hi):
        return lo + (hi - lo) * self.frac()

    def int(self, lo, hi):
        return lo + int(self.frac() * (hi - lo + 1))

    def chance(self, p):
        return self.frac() < p


def _auto(fn):
    """Drawing helpers mutate the pixel list in place; this makes them return it."""
    def wrapper(px, *args, **kwargs):
        fn(px, *args, **kwargs)
        return px
    wrapper.__name__ = fn.__name__
    return wrapper


def blank(alpha=255):
    return [(0, 0, 0, 0)] * (16 * 16) if alpha == 0 else [(0, 0, 0, alpha)] * (16 * 16)


def put(px, x, y, c, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        px[y * 16 + x] = (c[0], c[1], c[2], a)


def get(px, x, y):
    if 0 <= x < 16 and 0 <= y < 16:
        return px[y * 16 + x]
    return (0, 0, 0, 0)


# ---------------------------------------------------------------------------
# Surface generators
# ---------------------------------------------------------------------------

@_auto
def speckle(px, seed, base=(0.10, 0.42), accent=None, accent_p=0.0, alpha=255):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            c = shade(rng.between(base[0], base[1]))
            if accent and rng.chance(accent_p):
                c = accent
            put(px, x, y, c, alpha)


@_auto
def stone(px, seed, base=(0.12, 0.46), veins=True):
    """Rubble-stone: noisy body with a few darker seams."""
    speckle(px, seed, base)
    rng = Rand(seed + 7)
    for _ in range(5):
        x, y = rng.int(0, 15), rng.int(0, 15)
        for _ in range(rng.int(3, 7)):
            put(px, x, y, shade(base[0] * 0.55))
            x += rng.int(-1, 1)
            y += rng.int(0, 1)
    if veins:
        for _ in range(2):
            x = rng.int(0, 15)
            for y in range(16):
                put(px, x, y, shade(base[0] * 0.35))
                x = (x + rng.int(-1, 1)) % 16


@_auto
def cobble(px, seed):
    rng = Rand(seed)
    for gy in range(4):
        for gx in range(4):
            tone = rng.between(0.10, 0.40)
            for y in range(gy * 4, gy * 4 + 4):
                for x in range(gx * 4, gx * 4 + 4):
                    edge = (x in (gx * 4, gx * 4 + 3)) or (y in (gy * 4, gy * 4 + 3))
                    put(px, x, y, shade(tone * (0.45 if edge else 1.0)))


@_auto
def bricks(px, seed, rows=8, cols=4, tone=(0.14, 0.40), mortar=0.28):
    rng = Rand(seed)
    brick_h = 16 // rows
    for y in range(16):
        row = y // brick_h
        offset = (row % 2) * (16 // cols // 2)
        for x in range(16):
            xx = (x + offset) % 16
            if y % brick_h == 0 or xx % (16 // cols) == 0:
                put(px, x, y, shade(tone[0] * mortar * 0.5))
            else:
                put(px, x, y, shade(rng.between(*tone)))


@_auto
def planks(px, seed, tone=(0.16, 0.44)):
    rng = Rand(seed)
    for y in range(16):
        band = y // 4
        base = rng.between(*tone)
        for x in range(16):
            c = shade(base * (0.7 if y % 4 == 0 else 1.0))
            if y % 4 == 3:
                c = shade(base * 0.5)
            put(px, x, y, c)
    for i, y in enumerate((3, 7, 11, 15)):
        knot = rng.int(2, 13)
        put(px, knot, y, shade(tone[0] * 0.4))
        put(px, (knot + 1) % 16, y, shade(tone[0] * 0.4))


@_auto
def log_side(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            v = 0.14 + 0.30 * abs(((x * 5 + y * 3) % 7) / 7.0)
            put(px, x, y, shade(v + rng.between(-0.04, 0.04)))
    for x in (2, 6, 10, 14):
        for y in range(16):
            put(px, x, y, shade(0.06))


@_auto
def log_top(px, seed, rings=True):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            v = 0.10 + 0.06 * (int(d) % 3) if rings else rng.between(0.10, 0.34)
            put(px, x, y, shade(v + rng.between(-0.02, 0.02)))


@_auto
def leaves(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            if rng.chance(0.16):
                put(px, x, y, (0, 0, 0), 0)
            else:
                put(px, x, y, shade(rng.between(0.10, 0.42)))


@_auto
def metal(px, seed, tint=RUST, tone=(0.24, 0.52)):
    """Riveted plate."""
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            v = rng.between(*tone)
            c = shade(v)
            if rng.chance(0.06):
                c = tint
            put(px, x, y, c)
    for (rx, ry) in ((1, 1), (14, 1), (1, 14), (14, 14), (7, 7), (8, 8)):
        put(px, rx, ry, shade(0.62))
        put(px, rx + 1, ry, shade(0.34))
        put(px, rx, ry + 1, shade(0.34))
    for x in range(16):
        put(px, x, 7, shade(0.05))
    for y in range(16):
        put(px, 7, y, shade(0.05))


@_auto
def grate(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            if x % 5 == 0 or y % 5 == 0:
                put(px, x, y, shade(rng.between(0.30, 0.52)))
            else:
                put(px, x, y, (0, 0, 0), 0)


@_auto
def road(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            put(px, x, y, shade(rng.between(0.06, 0.20)))
    for _ in range(4):
        x = rng.int(0, 15)
        y = 0
        while y < 16:
            put(px, x, y, shade(0.30))
            x = max(0, min(15, x + rng.int(-1, 1)))
            y += 1
    for x in range(1, 10):
        put(px, x, 0, shade(0.70))
        put(px, x, 1, shade(0.55))


@_auto
def hollow(px, seed):
    bricks(px, seed)
    rng = Rand(seed + 3)
    cx, cy = 7, 8
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2) * 0.7 + (y - cy) ** 2
            if d < 9.0:
                if d < 3.5:
                    put(px, x, y, (0, 0, 0), 0)
                else:
                    put(px, x, y, shade(rng.between(0.05, 0.16)))


@_auto
def bone(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            c = shade(rng.between(0.42, 0.80))
            put(px, x, y, c)
    for y in range(16):
        put(px, 7, y, shade(0.30))
        put(px, 8, y, shade(0.34))
    for y in (3, 8, 13):
        for x in range(16):
            put(px, x, y, shade(0.26))


@_auto
def flesh(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            v = rng.between(0.10, 0.34)
            c = shade(v, tint=(1.35, 0.62, 0.85))
            if rng.chance(0.10):
                c = BLOOD
            put(px, x, y, c)
    for _ in range(3):
        x, y = rng.int(0, 15), rng.int(0, 15)
        for _ in range(6):
            put(px, x, y, VIOLET_DIM)
            x = (x + rng.int(-1, 1)) % 16
            y = (y + rng.int(-1, 1)) % 16


@_auto
def glass(px, seed, alpha=150):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            c = shade(rng.between(0.40, 0.62)) if edge else shade(0.26)
            a = 235 if edge else alpha
            put(px, x, y, c, a)
            if not edge and rng.chance(0.05):
                put(px, x, y, VIOLET, 210)


@_auto
def lamp(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d > 6.5:
                put(px, x, y, shade(0.10))
            elif d > 4.5:
                put(px, x, y, GLITCH if rng.chance(0.4) else VIOLET)
            elif d > 2.5:
                put(px, x, y, VIOLET)
            else:
                put(px, x, y, WHITE)


@_auto
def crystal(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            facet = ((x * 3 + y * 5) // 4) % 3
            base = 0.20 + 0.12 * facet
            c = shade(base, tint=(1.1, 0.95, 1.5))
            if rng.chance(0.05):
                c = VIOLET
            put(px, x, y, c)
    for x in range(16):
        put(px, x, (x * 2) % 16, WHITE if x % 4 == 0 else shade(0.55))


@_auto
def void_core(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            put(px, x, y, shade(rng.between(0.03, 0.14)))
    for _ in range(5):
        x, y = rng.int(0, 15), rng.int(0, 15)
        for _ in range(rng.int(4, 10)):
            put(px, x, y, VIOLET_DIM if rng.chance(0.7) else VIOLET)
            x = (x + rng.int(-1, 1)) % 16
            y = (y + rng.int(-1, 1)) % 16


@_auto
def black_hole(px, seed):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if d < 3.2:
                put(px, x, y, (0, 0, 0))
            elif d < 5.0:
                put(px, x, y, VIOLET_DIM if rng.chance(0.5) else (0x10, 0x08, 0x2A))
            elif d < 6.4:
                put(px, x, y, shade(0.30) if rng.chance(0.6) else VIOLET)
            else:
                put(px, x, y, shade(rng.between(0.04, 0.16)))


@_auto
def anchor(px, seed):
    metal(px, seed, tint=VIOLET, tone=(0.16, 0.34))
    for y in range(4, 12):
        for x in range(4, 12):
            put(px, x, y, VIOLET if (x + y) % 3 else WHITE)


@_auto
def crate(px, seed, tint=None, band=None):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            put(px, x, y, shade(rng.between(0.18, 0.42)))
    for i in range(16):
        put(px, i, 0, shade(0.55))
        put(px, i, 15, shade(0.55))
        put(px, 0, i, shade(0.55))
        put(px, 15, i, shade(0.55))
    for i in range(1, 15):
        put(px, i, 7, shade(0.30))
        put(px, 7, i, shade(0.30))
    if band:
        for i in range(2, 14):
            put(px, i, 3, band)
            put(px, i, 12, band)
    if tint:
        for i in range(5, 11):
            put(px, i, 9, tint)


@_auto
def door_texture(px, seed, tone):
    """A plank door face: stiles, a rail and a small violet glyph window."""
    planks(px, seed, tone)
    for y in range(16):
        put(px, 1, y, shade(tone[0] * 0.6))
        put(px, 14, y, shade(tone[0] * 0.6))
    for x in range(2, 14):
        put(px, x, 2, shade(tone[0] * 0.7))
        put(px, x, 13, shade(tone[0] * 0.7))
    for y in range(5, 9):
        for x in range(5, 11):
            put(px, x, y, VIOLET_DIM if (x + y) % 2 else VIOLET)


# ---------------------------------------------------------------------------
# Item art
# ---------------------------------------------------------------------------

@_auto
def item_flat(px, body, core=None, dust=False, seed=1):
    """A small floating object on a transparent background."""
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            if dust:
                if rng.chance(0.22):
                    put(px, x, y, body if rng.chance(0.6) else (core or body))
                continue
            # an egg/blob silhouette
            d = ((x - 7.5) / 4.2) ** 2 + ((y - 8.0) / 5.2) ** 2
            if d < 1.0:
                edge = d > 0.72
                c = body if not edge else shade(0.30)
                if core and ((x - 7) ** 2 + (y - 7) ** 2) < 3.2:
                    c = core
                put(px, x, y, c)
            elif rng.chance(0.05) and d < 1.6:
                put(px, x, y, body, 170)


@_auto
def item_shard(px, seed, body, core):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            # a diamond shard
            if abs(x - 7.5) / 4.5 + abs(y - 8.0) / 6.0 < 1.0:
                c = core if abs(x - 7.5) / 2.2 + abs(y - 6.0) / 3.0 < 0.7 else body
                put(px, x, y, c)
    for _ in range(3):
        x, y = rng.int(5, 10), rng.int(6, 11)
        put(px, x, y, WHITE)


@_auto
def item_ingot(px, seed, body):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            if 3 <= x <= 12 and 5 <= y <= 10:
                bevel = (x in (3, 12)) or (y in (5, 10))
                put(px, x, y, shade(0.34) if bevel else body)
    for x in range(5, 11):
        put(px, x, 7, shade(0.72))
    if rng.chance(1.0):
        put(px, 6, 6, WHITE)


@_auto
def item_dust(px, seed, body, core=None):
    rng = Rand(seed)
    for y in range(16):
        for x in range(16):
            if (x - 7.5) ** 2 + (y - 8) ** 2 < 34 and rng.chance(0.55):
                put(px, x, y, body if rng.chance(0.7) else (core or body))
            elif rng.chance(0.06):
                put(px, x, y, core or body, 150)


@_auto
def item_card(px, seed, body, core):
    for y in range(16):
        for x in range(16):
            if 3 <= x <= 12 and 3 <= y <= 12:
                edge = x in (3, 12) or y in (3, 12)
                put(px, x, y, shade(0.38) if edge else body)
    for y in range(6, 10):
        for x in range(5, 11):
            put(px, x, y, core if (x + y) % 3 else shade(0.66))


@_auto
def item_orb(px, seed, body, core):
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if d < 6.0:
                if d < 2.2:
                    put(px, x, y, WHITE)
                elif d < 4.0:
                    put(px, x, y, core)
                else:
                    put(px, x, y, body if ((x + y) % 4) else core)
            elif d < 6.8:
                put(px, x, y, body, 130)


@_auto
def item_sword(px, seed, blade, guard, hilt):
    """A true 16x16 sword along the anti-diagonal, like a vanilla item."""
    for i in range(4, 13):
        put(px, i, 15 - i, blade)
        put(px, i + 1, 15 - i, shade(0.82))
        put(px, i, 14 - i, blade)
        put(px, i + 1, 14 - i, shade(0.46))
        put(px, i - 1, 15 - i, shade(0.40))
    for (x, y) in ((3, 11), (4, 12), (11, 3), (12, 4)):
        put(px, x, y, blade)
    # crossguard
    put(px, 3, 13, guard); put(px, 4, 13, guard); put(px, 5, 13, guard)
    put(px, 2, 12, guard); put(px, 3, 12, guard)
    # grip + pommel
    for i in range(3):
        put(px, 2 + i, 14 - i, hilt)
    put(px, 1, 15, guard)
    put(px, 2, 15, hilt)


@_auto
def item_spear(px, seed, shaft, tip):
    for i in range(2, 15):
        put(px, 15 - i, i, shaft)
    for (x, y) in ((13, 1), (14, 0), (12, 2), (13, 0), (14, 1), (12, 1), (13, 2)):
        put(px, x, y, tip)
    put(px, 11, 3, tip)
    put(px, 15, 2, tip)


@_auto
def item_hook(px, seed, metal_c, rope):
    for i in range(4):
        put(px, 3 + i, 12 - i, rope)
    for (x, y) in ((7, 7), (8, 7), (9, 8), (9, 9), (8, 10), (7, 10), (6, 9), (6, 8), (7, 6), (8, 6)):
        put(px, x, y, metal_c)
    put(px, 9, 6, metal_c); put(px, 5, 7, metal_c)


@_auto
def item_totem(px, seed, body, core):
    for y in range(16):
        for x in range(16):
            if 4 <= x <= 11 and 2 <= y <= 14:
                edge = x in (4, 11) or y in (2, 14)
                put(px, x, y, shade(0.30) if edge else body)
    for x in range(6, 10):
        put(px, x, 5, core)
        put(px, x, 10, core)
    put(px, 7, 7, core); put(px, 8, 8, core); put(px, 7, 8, core); put(px, 8, 7, core)


# ---------------------------------------------------------------------------
# The catalogue
# ---------------------------------------------------------------------------

def block_textures():
    return {
        "decayed_stone": on_ash(lambda: stone(blank(), 101, (0.12, 0.46))),
        "decayed_cobblestone": on_ash(lambda: cobble(blank(), 102)),
        "decayed_stone_bricks": on_ash(lambda: bricks(blank(), 103, rows=8, cols=4, tone=(0.12, 0.38))),
        "decayed_dirt": on_ash(lambda: speckle(blank(), 104, (0.08, 0.26), accent=shade(0.05), accent_p=0.10)),
        "decayed_surface": on_ash(lambda: speckle(blank(), 105, (0.12, 0.34), accent=VIOLET_DIM, accent_p=0.04)),
        "decayed_sand": on_ash(lambda: speckle(blank(), 106, (0.18, 0.42))),
        "decayed_log": on_ash(lambda: log_side(blank(), 107)),
        "decayed_log_top": on_ash(lambda: log_top(blank(), 108)),
        "decayed_stripped_log": on_ash(lambda: log_side(blank(), 109)),
        "decayed_stripped_log_top": on_ash(lambda: log_top(blank(), 110, rings=False)),
        "decayed_planks": on_ash(lambda: planks(blank(), 111)),
        "decayed_leaves": lambda: leaves(blank(), 112),
        "city_bricks": on_ash(lambda: bricks(blank(), 113, rows=4, cols=2, tone=(0.16, 0.46))),
        "city_tiles": on_ash(lambda: bricks(blank(), 114, rows=16, cols=16, tone=(0.10, 0.30), mortar=0.6)),
        "rusted_plate": on_ash(lambda: metal(blank(), 115)),
        "rebar_grate": on_ash(lambda: grate(blank(), 116)),
        "cracked_road": on_ash(lambda: road(blank(), 117)),
        "hollow_wall": on_ash(lambda: hollow(blank(), 118)),
        "storm_rib": lambda: bone(blank(), 119),
        "tendon_block": lambda: flesh(blank(), 120),
        "withered_flesh_block": lambda: flesh(blank(), 121),
        "reality_glass": lambda: glass(blank(), 122),
        "glitch_lamp": lambda: lamp(blank(), 123),
        "memory_crystal": lambda: crystal(blank(), 124),
        "void_core": lambda: void_core(blank(), 125),
        "black_hole_core": lambda: black_hole(blank(), 126),
        "rift_anchor": lambda: anchor(blank(), 127),
        "city_crate": on_ash(lambda: crate(blank(), 128)),
        "supply_crate": on_ash(lambda: crate(blank(), 129, tint=RUST_LIT, band=RUST)),
        "vault_crate": on_ash(lambda: crate(blank(), 130, tint=VIOLET, band=VIOLET_DIM)),
        # doors read as planks / plate with a violet glyph
        "withered_door_top": on_ash(lambda: door_texture(blank(), 131, (0.16, 0.44))),
        "withered_door_bottom": on_ash(lambda: door_texture(blank(), 132, (0.16, 0.44))),
        "withered_trapdoor": on_ash(lambda: metal(blank(), 133, tint=VIOLET_DIM, tone=(0.18, 0.40))),
        "rusted_door_top": on_ash(lambda: door_texture(blank(), 134, (0.20, 0.46))),
        "rusted_door_bottom": on_ash(lambda: door_texture(blank(), 135, (0.20, 0.46))),
        "rusted_trapdoor": on_ash(lambda: metal(blank(), 136, tint=RUST_LIT, tone=(0.22, 0.48))),
        "decayed_fence_inventory": on_ash(lambda: planks(blank(), 137)),
        "decayed_wall_inventory": on_ash(lambda: cobble(blank(), 138)),
    }


def item_textures():
    return {
        "rift_key": lambda: item_card(blank(0), 201, VIOLET_DIM, VIOLET),
        "rift_shard": lambda: item_shard(blank(0), 202, VIOLET_DIM, VIOLET),
        "void_thread": lambda: item_flat(blank(0), shade(0.55), VIOLET, dust=False, seed=203),
        "decayed_bone": lambda: item_flat(blank(0), shade(0.72), shade(0.45), seed=204),
        "decayed_steel_ingot": lambda: item_ingot(blank(0), 205, shade(0.52)),
        "storm_heart_shard": lambda: item_shard(blank(0), 206, shade(0.42, tint=(1.4, 0.7, 0.9)), BLOOD),
        "glitch_echo": lambda: item_orb(blank(0), 207, GLITCH, VIOLET),
        "memory_fragment": lambda: item_shard(blank(0), 208, shade(0.30, tint=(1.1, 0.9, 1.5)), WHITE),
        "hallucination_dust": lambda: item_dust(blank(0), 209, VIOLET, GLITCH),
        "city_keycard": lambda: item_card(blank(0), 210, shade(0.30), GLITCH),
        "glyph_cell": lambda: item_flat(blank(0), VIOLET_DIM, GLITCH, seed=211),
        "creator_fragment": lambda: item_shard(blank(0), 212, VIOLET, WHITE),
        "abyss_orb": lambda: item_orb(blank(0), 213, VIOLET_DIM, VIOLET),
        "tentacle_hook": lambda: item_hook(blank(0), 214, shade(0.46, tint=(1.2, 0.8, 1.2)), shade(0.30)),
        "reality_ripper": lambda: item_sword(blank(0), 215, VIOLET, shade(0.40), shade(0.20)),
        "withered_blade": lambda: item_sword(blank(0), 216, shade(0.66), shade(0.34), shade(0.16)),
        "storm_spear": lambda: item_spear(blank(0), 217, shade(0.38), GLITCH),
        "creators_judgement": lambda: item_sword(blank(0), 218, GLITCH, VIOLET, shade(0.18)),
        "echo_totem": lambda: item_totem(blank(0), 219, shade(0.26), VIOLET),
        "guide_book": lambda: item_card(blank(0), 220, shade(0.34), WHITE),
        "antenna": lambda: item_hook(blank(0), 221, GLITCH, shade(0.30)),
    }


# ---------------------------------------------------------------------------
# Emission
# ---------------------------------------------------------------------------

def emit(check_only=False):
    os.makedirs(OUT_BLOCK, exist_ok=True)
    os.makedirs(OUT_ITEM, exist_ok=True)
    written = missing = 0
    for name, maker in sorted(block_textures().items()):
        path = os.path.join(OUT_BLOCK, name + ".png")
        if check_only:
            if not os.path.isfile(path):
                print("missing block texture:", name)
                missing += 1
            continue
        write_png(path, 16, 16, maker())
        written += 1
    for name, maker in sorted(item_textures().items()):
        path = os.path.join(OUT_ITEM, name + ".png")
        if check_only:
            if not os.path.isfile(path):
                print("missing item texture:", name)
                missing += 1
            continue
        write_png(path, 16, 16, maker())
        written += 1
    if check_only:
        if missing:
            print("[textures] %d MISSING" % missing)
            return 1
        print("[textures] all %d content-pack textures present"
              % (len(block_textures()) + len(item_textures())))
        return 0
    print("[textures] wrote %d block + %d item textures from the supplied stage palette"
          % (len(block_textures()), len(item_textures())))
    return 0


def main():
    return emit(check_only="--check" in sys.argv[1:])


if __name__ == "__main__":
    sys.exit(main())
