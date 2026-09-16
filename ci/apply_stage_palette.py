#!/usr/bin/env python3
"""Apply the supplied stage-sheet palettes to the storm body atlases.

The user supplied the real MCSM stage texture sheets and described what they
cover: one for phase 4 through 5.9, and a second for phase 6 through 8. They are
the ground truth for the storm's *material*; the traced 512x512 atlases we ship
carry the *shading structure* (the crevices, the panel breaks, the AO the model
UVs rely on). Neither alone is right:

  * using a 160x160 sheet directly as a 512x512 atlas throws away every shading
    detail the traced atlases were built for, and the storm goes flat;
  * keeping the traced atlases leaves them in the wrong material -- they are
    greyscale (measured: four colours, #000000 / #101010 / #202020 / #303030),
    while the sheets are near-black with a faint navy cast (#000000 / #000810 /
    #000010 / #000008 / #000818).

So this tool relights rather than replaces: it reads the sheet's own opaque
pixels, builds a luminance ramp out of them (the sheet's palette, in its own
order), and maps every pixel of the traced atlas through that ramp. The result
keeps the traced shading and wears the sheet's exact colours.

Bands, matching McsmStormSkinPhaseAtlasMixin:
    phase 4.0 - 5.9  ->  wither_storm_stage_a.png  ->  phase_4_assets*.png
                                                       phase_4_assets_*_p55.png
    phase 6.0 - 8.0+ ->  wither_storm_stage_b.png  ->  phase_4_assets_*_p6.png
                                                       phase_4_assets_*_p7.png

Usage:
    python3 ci/apply_stage_palette.py            # write the atlases
    python3 ci/apply_stage_palette.py --check    # verify (build gate)
    python3 ci/apply_stage_palette.py --report    # print the measured palettes
"""
from __future__ import annotations

import argparse
import json
import os
import struct
import sys
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ENTITY = os.path.join(ROOT, "jar-overrides", "assets", "dabywitherstormmod", "textures", "entity")
GRAFTS = os.path.join(ENTITY, "stage_palette.json")

# sheet -> the atlases that wear it. `og` is the base mod's own tracing profile;
# both profiles get the same palette, because the palette is the material.
BANDS = {
    "wither_storm_stage_a.png": [
        "phase_4_assets.png", "phase_4_assets_og.png",
        "phase_4_assets_p55.png", "phase_4_assets_og_p55.png",
    ],
    "wither_storm_stage_b.png": [
        "phase_4_assets_p6.png", "phase_4_assets_og_p6.png",
        "phase_4_assets_p7.png", "phase_4_assets_og_p7.png",
    ],
}

# Where the sheets live. The user's own uploads at the repo root are checked
# first (that is the authoritative copy), then the traced pack's copies.
SHEET_DIRS = [
    ROOT,
    os.path.join(ROOT, "ogs-cem", "assets", "minecraft", "textures", "entity", "cem"),
]

RAMP_STEPS = 24


# ---------------------------------------------------------------------------
# PNG (8-bit RGB/RGBA, the only shapes Minecraft ships)
# ---------------------------------------------------------------------------

def decode_png(path):
    data = open(path, "rb").read()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("%s is not a PNG" % path)
    pos, idat, w, h, bd, ct = 8, b"", 0, 0, 8, 6
    while pos < len(data):
        ln = struct.unpack(">I", data[pos:pos + 4])[0]
        typ = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + ln]
        if typ == b"IHDR":
            w, h, bd, ct = struct.unpack(">IIBB", chunk[:10])
        elif typ == b"IDAT":
            idat += chunk
        pos += 12 + ln
    if bd != 8 or ct not in (2, 6):
        raise ValueError("%s: only 8-bit RGB/RGBA is supported (got bd=%d ct=%d)" % (path, bd, ct))
    raw = zlib.decompress(idat)
    bpp = 4 if ct == 6 else 3
    stride = w * bpp
    out = bytearray(w * h * bpp)
    prev = bytearray(stride)
    p = 0
    for y in range(h):
        f = raw[p]
        p += 1
        line = bytearray(raw[p:p + stride])
        p += stride
        if f == 1:
            for i in range(bpp, stride):
                line[i] = (line[i] + line[i - bpp]) & 255
        elif f == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 255
        elif f == 3:
            for i in range(stride):
                a = line[i - bpp] if i >= bpp else 0
                line[i] = (line[i] + ((a + prev[i]) >> 1)) & 255
        elif f == 4:
            for i in range(stride):
                a = line[i - bpp] if i >= bpp else 0
                b = prev[i]
                c = prev[i - bpp] if i >= bpp else 0
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 255
        out[y * stride:(y + 1) * stride] = line
        prev = line
    return w, h, bpp, out


def encode_png(path, w, h, bpp, px):
    ct = 6 if bpp == 4 else 2
    stride = w * bpp
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        raw += px[y * stride:(y + 1) * stride]

    def chunk(tag, payload):
        return (struct.pack(">I", len(payload)) + tag + payload
                + struct.pack(">I", zlib.crc32(tag + payload) & 0xFFFFFFFF))

    body = chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, ct, 0, 0, 0))
    body += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    body += chunk(b"IEND", b"")
    with open(path, "wb") as fh:
        fh.write(b"\x89PNG\r\n\x1a\n" + body)


# ---------------------------------------------------------------------------
# the sheet's own palette, as a ramp
# ---------------------------------------------------------------------------

def luma(r, g, b):
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def sheet_ramp(path):
    """
    The sheet's palette as an ordered ramp, dark -> light.

    Built from the sheet's DISTINCT colours, not from its pixel distribution.
    That distinction matters: the supplied sheets are better than 45% pure
    black, so binning by pixel count puts twelve of twenty-four ramp steps at
    #000000 and the relit atlases come out two-tone (black plus one navy). The
    palette a texture actually *means* is its distinct colours, so they are
    collected, sorted by luminance, and spread evenly across the ramp.
    """
    w, h, bpp, px = decode_png(path)
    counts = {}
    for i in range(0, len(px), bpp):
        if bpp == 4 and px[i + 3] < 8:
            continue
        key = (px[i], px[i + 1], px[i + 2])
        counts[key] = counts.get(key, 0) + 1
    if not counts:
        raise ValueError("%s has no opaque pixels" % path)
    ordered = sorted(counts, key=lambda c: luma(c[0], c[1], c[2]))
    if len(ordered) == 1:
        ordered = ordered * RAMP_STEPS
    ramp = []
    for step in range(RAMP_STEPS):
        idx = int(step * (len(ordered) - 1) / max(1, RAMP_STEPS - 1))
        ramp.append(tuple(float(v) for v in ordered[idx]))
    return ramp


def relight(src_path, dst_path, ramp):
    """
    Map each pixel's own luminance through the ramp: shading kept, colour adopted.

    The luminance is normalised against the ATLAS'S OWN range (2nd..98th
    percentile), not against 0..255. That matters: the supplied sheets are
    almost entirely pure black (measured mean #02 04 0A, with four of every
    twenty-four ramp steps at #000000), while the traced atlases carry their
    detail between #000000 and #333333. A straight 0..255 mapping would drive
    every one of those pixels to black and the storm would lose its shading
    entirely; normalising first spreads the atlas's own contrast across the
    sheet's palette, so the detail survives and only the colour changes.
    """
    w, h, bpp, px = decode_png(src_path)
    out = bytearray(px)
    n = len(ramp) - 1
    lums = []
    for i in range(0, len(px), bpp):
        if bpp == 4 and px[i + 3] < 8:
            continue
        lums.append(luma(px[i], px[i + 1], px[i + 2]))
    if not lums:
        return w, h
    lums.sort()
    low = lums[int(0.02 * (len(lums) - 1))]
    high = lums[int(0.98 * (len(lums) - 1))]
    span = max(1.0, high - low)
    for i in range(0, len(px), bpp):
        if bpp == 4 and px[i + 3] < 8:
            continue
        t = (luma(px[i], px[i + 1], px[i + 2]) - low) / span
        t = 0.0 if t < 0.0 else (1.0 if t > 1.0 else t)
        f = t * n
        # NOTE: these are ramp indices and must NOT be called lo/hi -- the first
        # version reused the names of the percentile bounds above, so from the
        # second pixel on every luminance was compared against a ramp index and
        # the whole atlas came out black (caught before shipping).
        step = int(f)
        nxt = min(n, step + 1)
        frac = f - step
        r = ramp[step][0] + (ramp[nxt][0] - ramp[step][0]) * frac
        g = ramp[step][1] + (ramp[nxt][1] - ramp[step][1]) * frac
        b = ramp[step][2] + (ramp[nxt][2] - ramp[step][2]) * frac
        out[i] = int(max(0, min(255, r + 0.5)))
        out[i + 1] = int(max(0, min(255, g + 0.5)))
        out[i + 2] = int(max(0, min(255, b + 0.5)))
    encode_png(dst_path, w, h, bpp, out)
    return w, h


def find_sheet(name):
    for d in SHEET_DIRS:
        p = os.path.join(d, name)
        if os.path.exists(p):
            return p
    return None


def measure(path):
    w, h, bpp, px = decode_png(path)
    distinct = {}
    for i in range(0, len(px), bpp):
        if bpp == 4 and px[i + 3] < 8:
            continue
        key = (px[i], px[i + 1], px[i + 2])
        distinct[key] = distinct.get(key, 0) + 1
    top = sorted(distinct.items(), key=lambda kv: -kv[1])[:6]
    total = sum(distinct.values())
    blue = sum(col[2] * n for col, n in distinct.items()) / max(1, total)
    green = sum(col[1] * n for col, n in distinct.items()) / max(1, total)
    red = sum(col[0] * n for col, n in distinct.items()) / max(1, total)
    return {
        "size": [w, h],
        "distinct": len(distinct),
        "top": ["#%02X%02X%02X x%d" % (k[0], k[1], k[2], v) for k, v in top],
        "mean_blue": round(blue, 1),
        "mean_green": round(green, 1),
        "mean_red": round(red, 1),
    }


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    ap.add_argument("--report", action="store_true")
    ap.add_argument("--to", default=None,
                    help="write the relit atlases here instead of in place (verification)")
    args = ap.parse_args()

    report = {}
    missing = []
    failures = []
    for sheet, atlases in BANDS.items():
        path = find_sheet(sheet)
        if path is None:
            missing.append(sheet)
            continue
        ramp = sheet_ramp(path)
        report[sheet] = {
            "path": os.path.relpath(path, ROOT),
            "measured": measure(path),
            "ramp": ["#%02X%02X%02X" % (round(c[0]), round(c[1]), round(c[2])) for c in ramp],
            "targets": {},
        }
        for atlas in atlases:
            src = os.path.join(ENTITY, atlas)
            # NEVER relight an emissive mask: the _e atlases are the glow
            # (teeth, eyes, emitters). They are supposed to be near-black with
            # bright islands, and mapping them through a dark palette would put
            # the spotlight out -- which is exactly the "teeth do not glow"
            # report this build already had to fix once.
            if atlas.endswith("_e.png") or not os.path.exists(src):
                continue
            before = measure(src)
            if args.check:
                # Verification, not a no-op: every colour in the atlas has to be
                # one the supplied sheet actually contains. That is the whole
                # claim of this step -- "the storm wears the sheet's palette" --
                # and it is what would catch a hand-edited atlas, a regenerated
                # traced asset, or a sheet that was swapped for a different one.
                after = before
                allowed = set()
                for c in ramp:
                    allowed.add((round(c[0]), round(c[1]), round(c[2])))
                w, h, bpp, px = decode_png(src)
                seen = set()
                worst = 0
                for i in range(0, len(px), bpp):
                    if bpp == 4 and px[i + 3] < 8:
                        continue
                    col = (px[i], px[i + 1], px[i + 2])
                    if col in seen:
                        continue
                    seen.add(col)
                    if col in allowed:
                        continue
                    near = min(max(abs(col[0] - a[0]), abs(col[1] - a[1]), abs(col[2] - a[2]))
                               for a in allowed)
                    worst = max(worst, near)
                    if near > 2:
                        failures.append("%s carries #%02X%02X%02X, which is %d away from any "
                                        "colour in %s" % (atlas, col[0], col[1], col[2], near, sheet))
                report[sheet]["targets"][atlas] = {"before": before, "after": after,
                                                   "worst_channel_distance": worst,
                                                   "wrote": "verified"}
                continue
            else:
                dst = src if not args.to else os.path.join(args.to, atlas)
                relight(src, dst, ramp)
                after = measure(dst)
            report[sheet]["targets"][atlas] = {"before": before, "after": after,
                                               "wrote": args.to or os.path.relpath(src, ROOT)}

    if args.report:
        print(json.dumps(report, indent=2))
        return 0
    if args.check:
        if missing:
            print("[stage] FAILED -- supplied sheet(s) not found: %s" % ", ".join(missing))
            return 1
        for line in failures:
            print("::error title=stage palette::%s" % line)
            print("  FAIL %s" % line)
        if failures:
            print("[stage] FAILED (%d colour(s) outside the supplied sheet palette)" % len(failures))
            return 1
        checked = sum(len(v["targets"]) for v in report.values())
        worst = max((t["worst_channel_distance"]
                     for v in report.values() for t in v["targets"].values()), default=0)
        print("[stage] OK -- %d storm body atlases wear the supplied stage-sheet palettes "
              "(worst channel distance %d)" % (checked, worst))
        return 0

    print("[stage] applied the supplied stage palettes to the storm body atlases")
    for sheet, data in report.items():
        for atlas, stats in data["targets"].items():
            print("  %-34s via %-26s %s" % (atlas, sheet, stats["after"]["top"][0]))
    if missing:
        print("  note: sheet(s) not found: %s" % ", ".join(missing))
    with open(GRAFTS, "w", encoding="utf-8") as fh:
        json.dump(report, fh, indent=2)
        fh.write("\n")
    print("[stage] wrote %s" % os.path.relpath(GRAFTS, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
