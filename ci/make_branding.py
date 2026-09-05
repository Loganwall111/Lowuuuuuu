#!/usr/bin/env python3
"""Devouring Storms -- branding baker (pure-python PNG, no PIL in sandbox).

Turns the two brand assets the player supplied into jar overrides:

  branding/logo.png        -> assets/minecraft/textures/gui/title/minecraft.png
                              (the title-screen wordmark: logo, black keyed
                              out to alpha so the panorama shows through)
                           -> assets/dabywitherstormmod/icon.png
                              (mods-list icon: centre square crop, 256x256)
  branding/panorama.png    -> assets/minecraft/textures/gui/title/background/
                              panorama_0..5.png  (and the legacy
                              textures/gui/panorama/ path as insurance)
                              sides = four horizontal quarters of the shot so
                              the 360-degree pan reconstructs the picture with
                              a single wrap seam; face 4 = sky cap, 5 = ground.

Usage: python3 ci/make_branding.py
Exits 0 with a notice when the inputs are absent, so it can sit in the
pipeline before the assets arrive.
"""
import os
import struct
import sys
import zlib

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LOGO_CANDIDATES = [
    "branding/logo.png",
    "uploads/ChatGPT Image Sep 5, 2026, 03_06_13 PM.png",
]
PANO_CANDIDATES = [
    "branding/panorama.png",
    "uploads/Screenshot 2026-09-05 035802.png",
]


# ---------------------------------------------------------------- png codec
def read_png(path):
    d = open(path, "rb").read()
    assert d[:8] == b"\x89PNG\r\n\x1a\n", "not a png: " + path
    pos = 8
    idat = b""
    w = h = depth = ctype = interlace = None
    plte = None
    while pos < len(d):
        ln = struct.unpack_from(">I", d, pos)[0]
        tag = d[pos + 4:pos + 8]
        body = d[pos + 8:pos + 8 + ln]
        if tag == b"IHDR":
            w, h, depth, ctype, _c, _f, interlace = struct.unpack(">IIBBBBB", body)
        elif tag == b"IDAT":
            idat += body
        elif tag == b"PLTE":
            plte = body
        elif tag == b"IEND":
            break
        pos += 12 + ln
    assert depth == 8 and interlace == 0, f"unsupported png flavour in {path}"
    raw = zlib.decompress(idat)
    ch = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[ctype]
    stride = w * ch
    out = bytearray(h * stride)
    prev = bytearray(stride)
    p = 0
    for y in range(h):
        f = raw[p]; p += 1
        line = bytearray(raw[p:p + stride]); p += stride
        if f == 1:
            for i in range(ch, stride):
                line[i] = (line[i] + line[i - ch]) & 255
        elif f == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 255
        elif f == 3:
            for i in range(stride):
                a = line[i - ch] if i >= ch else 0
                line[i] = (line[i] + ((a + prev[i]) >> 1)) & 255
        elif f == 4:
            for i in range(stride):
                a = line[i - ch] if i >= ch else 0
                b = prev[i]
                c = prev[i - ch] if i >= ch else 0
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 255
        out[y * stride:(y + 1) * stride] = line
        prev = line
    # normalise to RGBA
    px = []
    for i in range(0, len(out), ch):
        if ctype == 6:
            px.append((out[i], out[i + 1], out[i + 2], out[i + 3]))
        elif ctype == 2:
            px.append((out[i], out[i + 1], out[i + 2], 255))
        elif ctype == 0:
            px.append((out[i], out[i], out[i], 255))
        elif ctype == 4:
            px.append((out[i], out[i], out[i], out[i + 1]))
        else:  # palette
            j = out[i] * 3
            px.append((plte[j], plte[j + 1], plte[j + 2], 255))
    return w, h, px


def write_png(path, w, h, px):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        row = px[y * w:(y + 1) * w]
        for r, g, b, a in row:
            raw += bytes((r, g, b, a))
    comp = zlib.compress(bytes(raw), 9)

    def chunk(tag, body):
        c = struct.pack(">I", len(body)) + tag + body
        return c + struct.pack(">I", zlib.crc32(tag + body) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", comp)
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "wb").write(png)


# ------------------------------------------------------------------ helpers
def scale(px, w, h, nw, nh):
    out = []
    for y in range(nh):
        sy = min(h - 1, y * h // nh)
        base = sy * w
        for x in range(nw):
            out.append(px[base + min(w - 1, x * w // nw)])
    return out


def crop(px, w, h, x0, y0, cw, chh):
    out = []
    for y in range(y0, y0 + chh):
        base = y * w
        out.extend(px[base + x0:base + x0 + cw])
    return out


def key_black(px):
    """The logo is neon on pure black: key black to alpha so the title
    panorama shows through instead of a black rectangle."""
    out = []
    for r, g, b, a in px:
        m = max(r, g, b)
        if m < 20:
            out.append((r, g, b, 0))
        elif m < 70:
            out.append((r, g, b, (m - 20) * 255 // 50))
        else:
            out.append((r, g, b, a))
    return out


def main():
    logo = next((c for c in LOGO_CANDIDATES if os.path.isfile(os.path.join(REPO, c))), None)
    pano = next((c for c in PANO_CANDIDATES if os.path.isfile(os.path.join(REPO, c))), None)
    if not logo and not pano:
        print("[branding] no inputs in branding/ or uploads/ -- nothing to bake yet")
        return 0
    over = os.path.join(REPO, "jar-overrides")

    if logo:
        w, h, px = read_png(os.path.join(REPO, logo))
        print(f"[branding] logo {w}x{h}")
        # title wordmark: 1024 wide, black keyed out
        nw = 1024
        nh = max(1, h * nw // w)
        write_png(os.path.join(over, "assets/minecraft/textures/gui/title/minecraft.png"),
                  nw, nh, key_black(scale(px, w, h, nw, nh)))
        # mods-list icon: centre square, 256
        side = min(w, h)
        cx, cy = (w - side) // 2, (h - side) // 2
        write_png(os.path.join(over, "assets/dabywitherstormmod/icon.png"),
                  256, 256, key_black(scale(crop(px, w, h, cx, cy, side, side), side, side, 256, 256)))
        print("[branding] wrote title logo + mod icon")

    if pano:
        w, h, px = read_png(os.path.join(REPO, pano))
        print(f"[branding] panorama source {w}x{h}")
        faces = []
        for q in range(4):                       # sides: four horizontal quarters
            qw = w // 4
            faces.append(scale(crop(px, w, h, q * qw, 0, qw, h), qw, h, 512, 512))
        band = max(64, h // 4)
        faces.append(scale(crop(px, w, h, 0, 0, w, band), w, band, 512, 512))          # sky cap
        faces.append(scale(crop(px, w, h, 0, h - band, w, band), w, band, 512, 512))   # ground
        for i, face in enumerate(faces):
            for base in ("assets/minecraft/textures/gui/title/background",
                         "assets/minecraft/textures/gui/panorama"):
                write_png(os.path.join(over, base, f"panorama_{i}.png"), 512, 512, face)
        print("[branding] wrote 6 panorama faces to both known title paths")
    return 0


if __name__ == "__main__":
    sys.exit(main())
