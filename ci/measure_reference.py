#!/usr/bin/env python3
"""measure_reference.py -- read a supplied reference frame and print its colours.

WHY THIS EXISTS
---------------
The build's colours are only as good as the frames they came from, and the
artist's own stills are the ground truth: the three sky sheets ("phase 5
turquoise sky.png", "phase5sky0purple sky.png", "phase6sky 6 witherstorm.png")
were traced into `ci/palette_tables.py` by hand, anchor by anchor. This is the
tool that does the reading for the NEXT batch of frames, so a new halo still or
sky screenshot can be turned into numbers in one command instead of by eye.

WHAT IT PRINTS
--------------
  * the size and the format, so a bad export is obvious straight away;
  * a vertical band table -- the mean colour of the frame's top, three-quarter,
    middle, quarter and bottom rows, which is what a sky column actually is;
  * the dominant opaque colours, binned, with their share of the frame, which is
    what the storm's material actually is;
  * if a second frame is given, the per-band distance between the two, so an
    "is this the same sky?" question has a number for an answer.

Usage:
    python3 ci/measure_reference.py still.png
    python3 ci/measure_reference.py still.png --bands 12
    python3 ci/measure_reference.py trace_a.png trace_b.png

Nothing here writes to the repository: it is a reader, so it is safe to point it
at a file that is still being argued about.
"""
import argparse
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import pngutil  # noqa: E402


def load(path):
    w, h, px = pngutil.read_png(path)
    return w, h, px


def band_table(w, h, px, bands):
    """Mean colour per horizontal band, top to bottom."""
    out = []
    for b in range(bands):
        y0 = h * b // bands
        y1 = max(y0 + 1, h * (b + 1) // bands)
        rs = gs = bs = 0
        n = 0
        for y in range(y0, y1):
            base = y * w
            for x in range(w):
                r, g, bl, a = px[base + x]
                rs += r
                gs += g
                bs += bl
                n += 1
        out.append((rs // n, gs // n, bs // n))
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("frame")
    ap.add_argument("other", nargs="?")
    ap.add_argument("--bands", type=int, default=6)
    ap.add_argument("--bins", type=int, default=6)
    args = ap.parse_args()

    for path in [args.frame] + ([args.other] if args.other else []):
        if not os.path.isfile(path):
            print("[ref] no such frame: %s" % path)
            return 1
        w, h, px = load(path)
        print("[ref] %s -- %dx%d, %d opaque pixels" % (path, w, h,
              sum(1 for p in px if p[3] >= 128)))
        bands = band_table(w, h, px, args.bands)
        for i, (r, g, b) in enumerate(bands):
            print("      band %2d/%d  #%02X%02X%02X   rgb(%3d,%3d,%3d)"
                  % (i + 1, args.bands, r, g, b, r, g, b))
        print("      dominant:")
        for rgb, share in pngutil.dominant(px, w, h, bins=args.bins)[:8]:
            print("        #%02X%02X%02X  %5.2f%%" % (rgb[0], rgb[1], rgb[2], share))

    if args.other:
        w1, h1, a = load(args.frame)
        w2, h2, b = load(args.other)
        if (w1, h1) != (w2, h2):
            print("[ref] different sizes (%dx%d vs %dx%d): band distance not comparable"
                  % (w1, h1, w2, h2))
            return 0
        ba = band_table(w1, h1, a, args.bands)
        bb = band_table(w2, h2, b, args.bands)
        print("[ref] per-band distance %s vs %s:" % (args.frame, args.other))
        worst = 0
        for i, (p, q) in enumerate(zip(ba, bb)):
            d = max(abs(p[k] - q[k]) for k in range(3))
            worst = max(worst, d)
            print("      band %2d: %3d channel%s  (%s vs %s)"
                  % (i + 1, d, "" if d == 1 else "s", "#%02X%02X%02X" % p,
                     "#%02X%02X%02X" % q))
        print("      worst band distance: %d" % worst)
    return 0


if __name__ == "__main__":
    sys.exit(main())
