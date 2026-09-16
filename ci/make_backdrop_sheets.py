#!/usr/bin/env python3
"""make_backdrop_sheets.py -- BUILD #416 backdrop sheets, baked from the traced tables.

WHAT THESE ARE
--------------
Six flat RGBA sheets painted on the mod's background sky-plane sticker stack (the
2D background sky layer -- never world-space geometry, never a dome, never a cap):

    backdrop_sheet_phase5_teal.png     phase 5.00-5.50   teal column + cloud deck
    backdrop_sheet_phase55_violet.png  phase 5.50-5.95   purple column + cloud deck
    backdrop_sheet_phase6_plum.png     phase 5.95+       rose column + THE BLACK SMUDGE
    backdrop_sheet_purple_canvas.png   phase 4.70+       opaque base canvas
    backdrop_sheet_teal_filter.png     phase 4.70-5.75   transparent teal filter
    backdrop_sheet_salmon_filter.png   phase 5.60+       transparent salmon filter

COLOUR PROVENANCE -- the point of build #416
--------------------------------------------
Every colour is read at bake time out of `ci/palette_tables.py`, which reads the
ship-shape stop tables straight out of the GLSL the jar installs. So the sheets
cannot drift from the sky they hang in:

    teal   "phase 5 turquoise sky.png"      -> 5.0 - 5.1
    purple "phase5sky0purple sky.png"       -> 5.5 - 5.9
    rose   "phase6sky 6 witherstorm.png"    -> 6.0 - 7.0
    ember  the storyboard's phase 7-8 tail  -> 7.0 - 8.05

The phase-6 sheet carries the big black-smudge mass the brief describes
(purple / magenta / pink / orange / salmon around a near-black core). Its rim
ramp is built from the SAME traced stops, so the smudge is a shape drawn in the
delivered palette rather than a second, hand-mixed one.

SHAPES are unchanged from the sheets these replace: a vertical column gradient,
a stretched-noise cloud deck for the filters, and a jagged elliptical mass for
the smudge. Nothing here samples an image or needs Pillow -- the PNG is written
by hand (filter-0 rows, RGBA8) so a diff is reviewable and a rerun is
byte-identical.

Usage:
    python3 ci/make_backdrop_sheets.py            # write the sheets
    python3 ci/make_backdrop_sheets.py --dry-run  # print the plan, write nothing
"""
import argparse
import functools
import math
import os
import struct
import sys
import zlib

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import palette_tables as pal  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)

# Matches the sheets these replace exactly (256x128 RGBA8, filter-0 rows).
WIDTH = 256
HEIGHT = 128

MISC_DIR = "assets/dabywitherstormmod/textures/misc"
ROOTS = ["src/main/resources", "jar-overrides"]

# The phase each sheet is baked for. The purple canvas is a base layer and uses
# the purple sheet's own middle row, so "canvas" and "violet sheet" agree.
PHASE_TEAL = 5.05
PHASE_PURPLE = 5.65
PHASE_ROSE = 6.20


def lerp(a, b, t):
    return a + (b - a) * t


def _hash2(x, y):
    """Deterministic 0..1 hash -- no RNG state, so the sheets are reproducible
    on every machine and every rerun (the whole point of a baked sheet)."""
    n = math.sin(x * 127.1 + y * 311.7) * 43758.5453
    return n - math.floor(n)


def _noise2(x, y):
    """Bilinear value noise."""
    xi, yi = math.floor(x), math.floor(y)
    xf, yf = x - xi, y - yi
    u = xf * xf * (3.0 - 2.0 * xf)
    v = yf * yf * (3.0 - 2.0 * yf)
    a = _hash2(xi, yi)
    b = _hash2(xi + 1, yi)
    c = _hash2(xi, yi + 1)
    d = _hash2(xi + 1, yi + 1)
    return lerp(lerp(a, b, u), lerp(c, d, u), v)


def _fbm(x, y, octaves=4):
    total = 0.0
    amp = 0.5
    freq = 1.0
    for _ in range(octaves):
        total += amp * _noise2(x * freq, y * freq)
        freq *= 2.03
        amp *= 0.5
    return total


@functools.lru_cache(maxsize=None)
def cloud_cover(x, y):
    """Horizontal cloud banding: stretched noise so the filter reads as cloud
    decks drifting across the canvas instead of a flat wash."""
    n1 = _fbm(x / WIDTH * 3.2, y / HEIGHT * 1.6)
    n2 = _fbm(x / WIDTH * 7.5 + 11.0, y / HEIGHT * 3.1 + 5.0)
    streak = 0.62 * n1 + 0.38 * n2
    return 0.42 + 0.58 * streak


@functools.lru_cache(maxsize=None)
def smudge(x, y):
    """The phase-6 mass: (inside, rim) for one pixel.

    inside  1 at the dead centre of the mass, 0 outside its jagged boundary.
    rim     1 exactly on the boundary ring, falling off inward and outward --
    this is where purple -> magenta -> pink -> orange -> salmon is painted.
    """
    u = (x + 0.5) / WIDTH
    v = (y + 0.5) / HEIGHT
    # the mass sits high and slightly right of centre, like the reference pane
    dx = (u - 0.54) / 0.46
    dy = (v - 0.40) / 0.40
    q = math.sqrt(dx * dx + dy * dy)
    # The boundary is a NOISY FIELD, not a function of the angle: an angular
    # perturbation is what turns a smudge into a starburst of spokes. Two noise
    # octaves -- a broad lobe plus a fine shred -- give an irregular mass whose
    # border tears the way soot does, and the fine term is quantized so the
    # border keeps the blocky, recovered-from-the-sheets edge.
    lobe = _fbm(u * 2.6 + 3.1, v * 2.2 - 1.7)
    shred = _fbm(u * 9.0 + 11.0, v * 7.0 - 6.0)
    shred = math.floor(shred * 7.0) / 7.0
    boundary = 0.74 + (lobe - 0.5) * 0.34 + (shred - 0.5) * 0.20
    inside = 1.0 - _smoothstep(boundary - 0.30, boundary + 0.10, q)
    d = (q - boundary) / 0.17
    rim = math.exp(-d * d)
    return inside, rim


@functools.lru_cache(maxsize=None)
def smudge_q(x, y):
    """The elliptical radius the smear ramp is read against (same frame as
    smudge(): high, slightly right of centre, wider than it is tall)."""
    u = (x + 0.5) / WIDTH
    v = (y + 0.5) / HEIGHT
    dx = (u - 0.54) / 0.46
    dy = (v - 0.40) / 0.40
    return math.sqrt(dx * dx + dy * dy)


def _smoothstep(lo, hi, v):
    if hi <= lo:
        return 1.0 if v >= hi else 0.0
    t = max(0.0, min(1.0, (v - lo) / (hi - lo)))
    return t * t * (3.0 - 2.0 * t)


def smudge_colour(deck, rim, inside, column_colour, q):
    """The mass, painted from the traced palette: near-black core, then the
    purple/magenta band, then pink, then the salmon/orange fringe, and finally
    the sheet's own column colour outside the boundary -- so the smudge has no
    edge of its own, it dissolves into the sky it hangs in."""
    m = deck["magenta"]
    pk = deck["pink"]
    sa = deck["salmon"]
    og = deck["orange"]
    c_in = deck["core"]
    c_mag = [lerp(m[k], pk[k], 0.25) for k in range(3)]
    c_sal = [lerp(sa[k], og[k], 0.45) for k in range(3)]
    smear = [lerp(c_in[k], c_mag[k], _smoothstep(0.0, 0.62, q)) for k in range(3)]
    smear = [lerp(smear[k], c_sal[k], _smoothstep(0.55, 1.06, q)) for k in range(3)]
    weight = max(inside, rim * 0.92)
    return [lerp(column_colour[k], smear[k], weight) for k in range(3)]


def bake(kind, phase, x, y):
    """One pixel of one sheet -> (r, g, b, a) in 0..255.

    `kind` selects the SHAPE; the COLOUR always comes from palette_tables, so a
    phase change is a data change and never a re-mix by hand.
    """
    t = (y + 0.5) / HEIGHT            # 0 zenith -> 1 horizon
    col = pal.column(phase, t)
    rgb = [max(0.0, min(1.0, v)) for v in col]

    if kind == "canvas":
        # opaque base canvas: the purple sheet's own column, full alpha
        return (rgb[0], rgb[1], rgb[2], 255.0)

    if kind == "filter":
        cover = cloud_cover(x + 0.5, y + 0.5)
        # transparent at the zenith, near-opaque at the horizon: the canvas
        # shows through the gaps, which is what makes it a FILTER
        a = 248.0 * (0.10 + 0.90 * t ** 1.35) * lerp(1.0, cover, 0.72)
        return (rgb[0], rgb[1], rgb[2], a)

    if kind == "deck":
        # the smoke sheets: a soft vertical deck with cloud shaping
        cover = cloud_cover(x + 0.5, y + 0.5)
        a = 232.0 * (0.18 + 0.82 * t) * lerp(0.55, 1.0, cover)
        return (rgb[0], rgb[1], rgb[2], a)

    if kind == "smudge":
        deck = pal.smudge_deck()
        inside, rim = smudge(x, y)
        q = smudge_q(x, y)
        rgb = smudge_colour(deck, rim, inside, rgb, q)
        # the mass is opaque in the middle and dissolved at its edge, so it
        # reads as a smudge on the sky rather than a sticker with a border
        cover = cloud_cover(x + 0.5, y + 0.5)
        a = 255.0 * max(inside * 0.96, rim * 0.70)
        a *= lerp(0.88, 1.0, cover)
        return (rgb[0], rgb[1], rgb[2], a)

    raise ValueError("unknown sheet kind: %s" % kind)


# (path, kind, phase) -- the six sheets the jar audit looks for, unchanged names.
SHEETS = [
    (MISC_DIR + "/backdrop_sheet_purple_canvas.png", "canvas", PHASE_PURPLE),
    (MISC_DIR + "/backdrop_sheet_teal_filter.png", "filter", PHASE_TEAL),
    (MISC_DIR + "/backdrop_sheet_salmon_filter.png", "filter", PHASE_ROSE),
    (MISC_DIR + "/backdrop_sheet_phase5_teal.png", "deck", PHASE_TEAL),
    (MISC_DIR + "/backdrop_sheet_phase55_violet.png", "deck", PHASE_PURPLE),
    (MISC_DIR + "/backdrop_sheet_phase6_plum.png", "smudge", PHASE_ROSE),
]


def write_sheet(path, kind, phase):
    """Write an RGBA8 PNG (filter-0 rows -- the repo's pngtools convention, so
    the file stays readable by every tool in ci/ without a Pillow dependency)."""
    raw = bytearray()
    for y in range(HEIGHT):
        raw.append(0)  # filter type 0 for this row
        for x in range(WIDTH):
            r, g, b, a = bake(kind, phase, x, y)
            raw += struct.pack(
                "BBBB",
                max(0, min(255, int(round(r * 255.0)))),
                max(0, min(255, int(round(g * 255.0)))),
                max(0, min(255, int(round(b * 255.0)))),
                max(0, min(255, int(round(a)))),
            )

    def chunk(typ, data):
        c = struct.pack(">I", len(data)) + typ + data
        return c + struct.pack(">I", zlib.crc32(typ + data) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBB", WIDTH, HEIGHT, 8, 6) + b"\x00\x00\x00"
    blob = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        + chunk(b"IEND", b"")
    )
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(blob)
    return len(blob)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    total = 0
    written = 0
    for rel, kind, phase in SHEETS:
        for root in ROOTS:
            path = os.path.join(ROOT, root, rel)
            if args.dry_run:
                c = pal.column(phase, 1.0)
                print("[plan] %-70s %s phase %.2f horizon #%02X%02X%02X"
                      % (os.path.relpath(path, ROOT), kind, phase,
                         int(round(c[0] * 255)), int(round(c[1] * 255)), int(round(c[2] * 255))))
                continue
            size = write_sheet(path, kind, phase)
            total += size
            written += 1
            print("[sheet] %-70s %-7s %7d B" % (os.path.relpath(path, ROOT), kind, size))
    if args.dry_run:
        print("[plan] %d files (6 sheets x %d roots)" % (len(SHEETS) * len(ROOTS), len(ROOTS)))
        return 0
    print("[done] %d sheet files, %.1f KiB total -- colours read from the traced tables"
          % (written, total / 1024.0))
    return 0


if __name__ == "__main__":
    sys.exit(main())
