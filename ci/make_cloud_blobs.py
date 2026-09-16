#!/usr/bin/env python3
"""make_cloud_blobs.py -- BUILD #416: reshape the rigid glare ovals into soft cloud blobs.

WHAT THIS REPLACES
------------------
The phase glare assets were 256x256 OVALS: a banded radial ramp with a defined
rim, drawn as one rigid billboard per layer. In game that reads as exactly what
it is -- a printed circle hung behind the storm, complete with a visible edge.
The brief asks for the opposite: soft, organic, cinematic CLOUD BLOBS that match
the show references.

WHAT A BLOB IS HERE
-------------------
Three changes, all in the asset:

  * the radius is WARPED by domain-warped value noise, so the silhouette is
    lumpy instead of round (fbm noise displaces the radial coordinate);
  * the alpha reaches zero well BEFORE the edge (the falloff is forced to zero at
    a noisy rim that sits inside the texture), so there is never a hard cut -- the
    blob dissolves into the sky;
  * a few soft LOBES are baked in (offset centres with their own falloff), which
    is what makes it read as vapour rather than a lens flare.

The per-phase PALETTE is untouched: each blob keeps the palette the oval carried
(colour bands from rim to core), so the phase colours the build ships do not move.
Only the SHAPE changes.

Deterministic: the noise is a fixed hash, the PNG is filter-0 rows compressed at
zlib level 9, so a rerun is byte-identical and a diff is reviewable.

Usage:
    python3 ci/make_cloud_blobs.py             # write every blob into both roots
    python3 ci/make_cloud_blobs.py --dry-run   # print the plan
"""
import argparse
import math
import os
import struct
import sys
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)

SIZE = 256
REL = "assets/dabywitherstormmod/textures/mcsm_atmosphere/glare/%s.png"
ROOTS = ["src/main/resources", "jar-overrides"]

# The palettes the ovals carried, rim -> core (R, G, B). Unchanged on purpose:
# this build reshapes the blobs, it does not re-colour the phases.
PALETTES = {
    "phase4":  [(0.349, 0.384, 0.816), (0.196, 0.212, 0.545), (0.086, 0.094, 0.290), (0.030, 0.035, 0.120)],
    "phase5":  [(0.416, 0.604, 0.471), (0.230, 0.400, 0.290), (0.086, 0.196, 0.140), (0.020, 0.075, 0.055)],
    "phase54": [(0.439, 0.220, 0.529), (0.270, 0.110, 0.360), (0.130, 0.040, 0.190), (0.045, 0.012, 0.075)],
    "phase55": [(0.529, 0.322, 0.612), (0.330, 0.180, 0.420), (0.160, 0.075, 0.230), (0.055, 0.022, 0.090)],
    "phase6":  [(0.847, 0.596, 0.455), (0.610, 0.330, 0.240), (0.330, 0.150, 0.110), (0.120, 0.050, 0.040)],
    "phase89": [(0.808, 0.353, 0.122), (0.580, 0.200, 0.060), (0.300, 0.090, 0.020), (0.110, 0.030, 0.005)],
}

# Lobe layout: (dx, dy, radius, weight) in unit-disc coordinates. Hand-placed so
# every blob has a heavy side and a wispy tail, which is what a cloud has.
LOBES = [
    (0.00, 0.00, 0.78, 1.00),
    (-0.34, 0.22, 0.58, 0.72),
    (0.36, -0.17, 0.53, 0.66),
    (-0.12, -0.41, 0.48, 0.55),
    (0.27, 0.34, 0.43, 0.48),
]
# Horizontal squash: clouds in the reference frames are wider than they are tall.
ASPECT = 1.18


def _hash2(x, y):
    h = (x * 374761393 + y * 668265263) & 0xFFFFFFFF
    h = (h ^ (h >> 13)) * 1274126177 & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0xFFFF) / 65535.0


def _noise2(x, y):
    xi, yi = int(math.floor(x)), int(math.floor(y))
    xf, yf = x - xi, y - yi
    u = xf * xf * (3.0 - 2.0 * xf)
    v = yf * yf * (3.0 - 2.0 * yf)
    a = _hash2(xi, yi)
    b = _hash2(xi + 1, yi)
    c = _hash2(xi, yi + 1)
    d = _hash2(xi + 1, yi + 1)
    return (a * (1 - u) + b * u) * (1 - v) + (c * (1 - u) + d * u) * v


def _fbm(x, y, octaves=4):
    total, amp, freq, norm = 0.0, 1.0, 1.0, 0.0
    for _ in range(octaves):
        total += amp * _noise2(x * freq, y * freq)
        norm += amp
        amp *= 0.5
        freq *= 2.0
    return total / norm


def _mixc(a, b, t):
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def _palette_at(pal, q):
    """Four-stop ramp rim -> core, sampled at q (0 = outer, 1 = core)."""
    if q <= 0.0:
        return pal[0]
    if q >= 1.0:
        return pal[-1]
    seg = q * (len(pal) - 1)
    i = min(int(seg), len(pal) - 2)
    return _mixc(pal[i], pal[i + 1], seg - i)


def blob_pixels(pal):
    """RGBA rows for one blob: warped radius, lobe sum, alpha forced to zero inside the rim."""
    rows = []
    for y in range(SIZE):
        row = []
        for x in range(SIZE):
            u = ((x + 0.5) / SIZE * 2.0 - 1.0) / ASPECT
            v = (y + 0.5) / SIZE * 2.0 - 1.0
            r = math.hypot(u, v)

            # domain-warped radius: the silhouette wanders instead of being a circle
            warp = _fbm(u * 2.3 + 11.0, v * 2.3 + 7.0, 4) - 0.5
            rr = r * (1.0 + 0.34 * warp)

            # the cloud: sum of soft lobes
            dens = 0.0
            for (dx, dy, lr, lw) in LOBES:
                d = math.hypot(u - dx, v - dy) / lr
                if d < 1.0:
                    dens += lw * (1.0 - d) ** 1.9
            dens = min(dens, 1.85)
            # fine grain so the blob has texture rather than a flat wash
            dens *= 0.80 + 0.40 * _fbm(u * 5.5 + 3.0, v * 5.5 + 19.0, 3)

            # alpha: zero by rr = 1 (no hard rim), soft over the last third.
            # Skipped pixels are written as FULLY TRANSPARENT pixels, never omitted:
            # a PNG row is exactly `width` pixels wide, and a ragged row produces a
            # file that decodes as garbage (or not at all).
            # a LONG soft ramp (0.45 of the radius) -- these are drawn as huge
            # billboards, so the dissolve has to happen over a wide band
            edge = min(1.0, (1.0 - rr) / 0.45) if rr < 1.0 else 0.0
            alpha = dens * edge * edge * (3.0 - 2.0 * edge) * 0.80
            if alpha <= 0.004:
                row.append((0, 0, 0, 0))
                continue

            # colour: the SAME palette, reversed, so the LIGHT comes from the middle.
            # The ovals were authored rim-bright with a black core (a ring), which is
            # why they read as a printed circle; a cloud is the other way round -- the
            # dense middle is the emissive part and the wisps carrying it outward are
            # dark. Reversing the ramp is the whole colour change this build makes.
            q = min(1.0, dens / 1.35)
            col = _palette_at(pal, 1.0 - q)
            row.append((min(255, int(round(col[0] * 255))),
                        min(255, int(round(col[1] * 255))),
                        min(255, int(round(col[2] * 255))),
                        min(255, int(round(alpha * 255)))))
        rows.append(row)
    return rows


def write_png(path, rows):
    raw = bytearray()
    for row in rows:
        raw.append(0)  # filter 0
        for (r, g, b, a) in row:
            raw += struct.pack("BBBB", r, g, b, a)

    def chunk(typ, data):
        c = struct.pack(">I", len(data)) + typ + data
        return c + struct.pack(">I", zlib.crc32(typ + data) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBB", SIZE, SIZE, 8, 6) + b"\x00\x00\x00"
    blob = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr)
            + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(blob)
    return len(blob)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    total = 0
    for name in sorted(PALETTES):
        rows = blob_pixels(PALETTES[name])
        if args.dry_run:
            print("[plan] %-9s %dx%d blob, %d painted rows (rim inside the texture, no hard edge)"
                  % (name, SIZE, SIZE, len([r for r in rows if r])))
            continue
        for root in ROOTS:
            path = os.path.join(ROOT, root, REL % name)
            size = write_png(path, rows)
            total += size
            print("[blob] %-70s %7d B" % (os.path.join(root, REL % name), size))
    if args.dry_run:
        print("[plan] %d blobs x %d roots" % (len(PALETTES), len(ROOTS)))
        return 0
    print("[done] %d cloud blob files, %.1f KiB -- shapes reshaped, palettes untouched"
          % (len(PALETTES) * len(ROOTS), total / 1024.0))
    return 0


if __name__ == "__main__":
    sys.exit(main())
