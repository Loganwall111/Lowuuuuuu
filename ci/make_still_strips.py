#!/usr/bin/env python3
"""make_still_strips.py -- BUILD #476: the stills, shipped as data, 1:1.

The sky's colours are read at 32 stops now (palette_tables.STOPS) instead of six, so
the column the shaders interpolate is the reference image's own column rather than
three anchors and a straight line between them. This tool writes that column out as
an IMAGE -- one pixel row per stop, top row = zenith, bottom row = horizon -- and
puts it in both places the sky lives:

    jar-overrides/assets/mcsm/textures/sky/stills_<role>.png     (the jar's own copy)
    storylook/assets/minecraft/textures/environment/mcsm_stills_<role>.png
                                                (the built-in Story Look pack's copy,
                                                 beside the sun and moon it already
                                                 ships 1:1 with the stills)

Why an image: it makes the claim checkable. "The sky is the still" is a sentence; a
1-pixel-wide, 32-pixel-tall file whose every pixel IS one stop of the traced column,
compared against the shader's own array and the Java feed every build, is a fact. If
the tables ever drift from the sheet, one of the three copies stops matching and the
build says so.

    python3 ci/make_still_strips.py            # write the six files
    python3 ci/make_still_strips.py --check    # report drift only, exit 1 on mismatch
"""
import argparse
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)
import palette_tables as pal  # noqa: E402
import pngutil  # noqa: E402

ROLES = ("teal", "purple", "rose")

DESTINATIONS = [
    ("jar-overrides/assets/mcsm/textures/sky", "stills_%s.png"),
    ("storylook/assets/minecraft/textures/environment", "mcsm_stills_%s.png"),
]


def column_pixels(role):
    """The traced column as opaque pixels: [(r,g,b,255), ...], zenith first."""
    out = []
    for stop in pal.rows(role):
        out.append(tuple(max(0, min(255, int(round(v * 255.0)))) for v in stop[:3]) + (255,))
    return out


def write(role, path, px):
    pngutil.write_png(os.path.join(ROOT, path), 1, len(px), list(px))


def read(path):
    return pngutil.read_png(os.path.join(ROOT, path))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()

    ok = True
    for role in ROLES:
        try:
            px = column_pixels(role)
        except KeyError as exc:
            print("[stills] %s: no traced column (%s)" % (role, exc))
            ok = False
            continue
        print("[stills] %-7s %d stops: %s ... %s"
              % (role, len(px), pal._hex([c / 255.0 for c in px[0][:3]]),
                 pal._hex([c / 255.0 for c in px[-1][:3]])))
        for base, pattern in DESTINATIONS:
            path = os.path.join(base, pattern % role)
            if args.check:
                try:
                    w, h, got = read(path)
                except Exception as exc:  # noqa: BLE001
                    print("  ! %s: %s (run: python3 ci/make_still_strips.py)" % (path, exc))
                    ok = False
                    continue
                if (w, h) != (1, len(px)):
                    print("  ! %s: %dx%d, expected 1x%d" % (path, w, h, len(px)))
                    ok = False
                    continue
                worst = 0
                for i, want in enumerate(px):
                    worst = max(worst, max(abs(got[i][k] - want[k]) for k in range(3)))
                if worst > 1:
                    print("  ! %s: drifts from the traced column by %d/255" % (path, worst))
                    ok = False
                else:
                    print("  ok %s" % path)
            else:
                write(role, path, px)
                print("  wrote %s" % path)

    if args.check:
        print("[stills] %s" % ("the shipped strips ARE the traced columns, 1:1"
                               if ok else "the shipped strips do not match the tables"))
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
