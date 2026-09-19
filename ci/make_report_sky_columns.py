#!/usr/bin/env python3
"""make_report_sky_columns.py -- regenerate ci/REPORT_sky_columns_shipped.png.

WHY THIS EXISTS
---------------
`ci/REPORT_sky_columns_shipped.png` is the picture of what the jar actually ships
for the three storm sky bands. It went stale the moment the supplied hex anchors
were ingested (build #416): it still showed the bright teal / violet / plum trace
that those anchors replaced, so anyone opening it to check the colours was being
shown the superseded sky. There was no generator in ci/ to re-make it, which is
how it went stale at all -- so this bakes it from the same source of truth the
shaders read, and a rerun is byte-identical.

WHAT IT DRAWS
-------------
One vertical column per supplied sheet, each the six-stop expansion of that
sheet's three anchors (ceiling at the top, horizon at the bottom), with the
anchor positions marked:

    teal    phase 5.00 - 5.10   #0C1216 / #172228 / #202E34
    purple  phase 5.50 - 5.90   #160A21 / #3A184E / #5A2474
    rose    phase 6.00 - 7.00   #1D1519 / #422D37 / #644354

Between the columns sit the expanded stops as labelled swatches, so the report
shows the gradient AND the numbers a reviewer would want to check it against.

    python3 ci/make_report_sky_columns.py            # write the report
    python3 ci/make_report_sky_columns.py --dry-run  # print the plan only
"""
import argparse
import os
import struct
import sys
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import palette_tables as pal  # noqa: E402

ROOT = os.path.dirname(HERE)
OUT = "ci/REPORT_sky_columns_shipped.png"

# left to right, matching the brief's three supplied sheets
COLUMNS = [
    ("teal", "phase 5.0-5.1", "phase 5 turquoise sky.png"),
    ("purple", "phase 5.5-5.9", "phase5sky0purple sky.png"),
    ("rose", "phase 6.0-7.0", "phase6sky 6 witherstorm.png"),
]

COL_W, COL_H = 92, 300       # one gradient column
GAP = 26
PAD = 18
SWATCH_H = 26
LABEL_ROW_H = 12


def _px(strip, x, y, w, h, rgb, a=255):
    for yy in range(max(0, y), min(h, y + 1)):
        for xx in range(max(0, x), min(w, x + 1)):
            i = (yy * w + xx) * 4
            strip[i] = max(0, min(255, int(rgb[0])))
            strip[i + 1] = max(0, min(255, int(rgb[1])))
            strip[i + 2] = max(0, min(255, int(rgb[2])))
            strip[i + 3] = a


def _fill(strip, w, h, x0, y0, x1, y1, rgb, a=255):
    for yy in range(max(0, y0), min(h, y1)):
        for xx in range(max(0, x0), min(w, x1)):
            _px(strip, xx, yy, w, h, rgb, a)


def _checker(strip, w, h, x0, y0, x1, y1, cell=8):
    """A light/dark checker so translucent-looking swatches never read as solid."""
    for yy in range(max(0, y0), min(h, y1)):
        for xx in range(max(0, x0), min(w, x1)):
            dark = ((xx - x0) // cell + (yy - y0) // cell) % 2 == 0
            v = 58 if dark else 78
            _px(strip, xx, yy, w, h, (v, v, v))


def build_strip():
    n = len(COLUMNS)
    width = PAD * 2 + n * COL_W + (n - 1) * GAP
    height = PAD * 2 + LABEL_ROW_H + COL_H + 10 + LABEL_ROW_H + SWATCH_H
    strip = bytearray(width * height * 4)
    _fill(strip, width, height, 0, 0, width, height, (12, 12, 16), 255)

    for i, (role, label, sheet) in enumerate(COLUMNS):
        x0 = PAD + i * (COL_W + GAP)
        col = pal.hex_column(role)
        # the gradient: stop 0 (ceiling) at the top, stop 5 (horizon) at the bottom
        for y in range(COL_H):
            t = y / float(COL_H - 1)
            c = pal.sample_column(col, t)
            rgb = tuple(round(v * 255.0) for v in c)
            _fill(strip, width, height, x0, PAD + LABEL_ROW_H + y,
                  x0 + COL_W, PAD + LABEL_ROW_H + y + 1, rgb)

        # the three supplied anchors, marked as ticks on the column edge
        for t in pal.ANCHOR_T:
            yy = PAD + LABEL_ROW_H + int(round(t * (COL_H - 1)))
            _fill(strip, width, height, x0 - 4, yy, x0, yy + 1, (235, 235, 240))
            _fill(strip, width, height, x0 + COL_W, yy,
                  x0 + COL_W + 4, yy + 1, (235, 235, 240))

        # the six expanded stops as swatches under the column
        sy = PAD + LABEL_ROW_H + COL_H + 10
        _checker(strip, width, height, x0, sy, x0 + COL_W, sy + SWATCH_H)
        seg = COL_W / float(pal.STOPS)
        for s in range(pal.STOPS):
            c = col[s]
            rgb = tuple(round(v * 255.0) for v in c)
            _fill(strip, width, height, x0 + int(s * seg), sy,
                  x0 + int((s + 1) * seg), sy + SWATCH_H, rgb)

    return strip, width, height


def _png_bytes(strip, w, h):
    raw = bytearray()
    for y in range(h):
        raw.append(0)  # filter 0
        raw += strip[y * w * 4:(y + 1) * w * 4]

    def chunk(typ, data):
        c = struct.pack(">I", len(data)) + typ + data
        return c + struct.pack(">I", zlib.crc32(typ + data) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBB", w, h, 8, 6) + b"\x00\x00\x00"
    blob = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr)
            + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b""))
    return blob


def write_png(path, strip, w, h):
    blob = _png_bytes(strip, w, h)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(blob)
    return len(blob)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--check", action="store_true",
                    help="rebuild in memory and fail if the committed report is stale")
    args = ap.parse_args()

    for role, label, sheet in COLUMNS:
        col = pal.hex_column(role)
        print("[column] %-7s %-13s %-28s %s -> %s" % (
            role, label, sheet, pal._hex(col[0]), pal._hex(col[-1])))
        print("         stops: %s" % " ".join(pal._hex(c) for c in col))

    if args.dry_run:
        print("[plan] would write %s (%d columns)" % (OUT, len(COLUMNS)))
        return 0

    strip, w, h = build_strip()
    if args.check:
        # this report went stale once because nothing compared it to the tables;
        # --check is what stops that happening a second time
        path = os.path.join(ROOT, OUT)
        if not os.path.exists(path):
            print("[report] FAIL -- %s is missing (run without --check)" % OUT)
            return 1
        with open(path, "rb") as f:
            on_disk = f.read()
        if on_disk != _png_bytes(strip, w, h):
            print("[report] FAIL -- %s is stale; rerun 'python3 ci/make_report_sky_columns.py'" % OUT)
            return 1
        print("[report] OK -- %s matches the supplied-anchor columns" % OUT)
        return 0
    size = write_png(os.path.join(ROOT, OUT), strip, w, h)
    print("[done] %s  %dx%d  %d B -- colours read from ci/palette_tables.py" % (OUT, w, h, size))
    return 0


if __name__ == "__main__":
    sys.exit(main())
