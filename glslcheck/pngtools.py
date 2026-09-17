#!/usr/bin/env python3
"""pngtools.py — minimal PNG decode/encode + the 1.9.96 devourer alpha lift.

No Pillow in this sandbox, so this is a dependency-free RGBA PNG codec
(8-bit, non-interlaced, filters 0-4 on decode; filter 0 + zlib on encode).

Used by phase 29 (MCSM 1.9.96) for the user's "the black part is a bit
see-through, make it a little more opaque, not too much":
the devourer atlas carries ~7.7% of texels at alpha 1..120 (soft wisp fringe)
which renders the densest body mass translucent. We lift every semi-opaque
texel to the 200..253 band:
    a' = 200 + round(a * 53 / 255)      for 1 <= a <= 249
    a' = a                              for a == 0 (holes stay holes)
    a' = a                              for a >= 250
so edges keep their shape but no longer read as fog. ~0.78 minimum alpha.

Usage:
    python3 pngtools.py lift <in.png> <out.png>
    python3 pngtools.py stats <in.png>
"""
import struct, sys, zlib


def read_png(path):
    """Returns (w, h, channels, bytearray of RGBA rows). ctype must be 6 (RGBA8)."""
    d = open(path, 'rb').read()
    assert d[:8] == b'\x89PNG\r\n\x1a\n', 'not a png: ' + path
    pos = 8
    idat = b''
    w = h = dep = ct = None
    while pos < len(d):
        ln, = struct.unpack('>I', d[pos:pos + 4])
        typ = d[pos + 4:pos + 8]
        data = d[pos + 8:pos + 8 + ln]
        pos += 12 + ln
        if typ == b'IHDR':
            w, h, dep, ct = struct.unpack('>IIBB', data[:10])
        elif typ == b'IDAT':
            idat += data
        elif typ == b'IEND':
            break
    assert dep == 8 and ct == 6, 'only RGBA8 supported, got depth=%s ctype=%s in %s' % (dep, ct, path)
    raw = zlib.decompress(idat)
    ch = 4
    stride = w * ch
    out = bytearray()
    prev = bytearray(stride)
    i = 0
    for _y in range(h):
        f = raw[i]
        i += 1
        line = bytearray(raw[i:i + stride])
        i += stride
        if f == 1:
            for x in range(ch, stride):
                line[x] = (line[x] + line[x - ch]) & 255
        elif f == 2:
            for x in range(stride):
                line[x] = (line[x] + prev[x]) & 255
        elif f == 3:
            for x in range(stride):
                line[x] = (line[x] + ((line[x - ch] if x >= ch else 0) + prev[x]) // 2) & 255
        elif f == 4:
            for x in range(stride):
                a = line[x - ch] if x >= ch else 0
                b = prev[x]
                c = prev[x - ch] if x >= ch else 0
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[x] = (line[x] + pr) & 255
        elif f != 0:
            raise ValueError('bad filter %d in %s' % (f, path))
        out += line
        prev = line
    return w, h, out


def write_png(path, w, h, px):
    """Writes RGBA8 PNG, filter 0 rows, single IDAT."""
    def chunk(typ, data):
        c = struct.pack('>I', len(data)) + typ + data
        c += struct.pack('>I', zlib.crc32(typ + data) & 0xffffffff)
        return c
    raw = bytearray()
    stride = w * 4
    for y in range(h):
        raw.append(0)
        raw += px[y * stride:(y + 1) * stride]
    ihdr = struct.pack('>IIBB', w, h, 8, 6) + b'\x00\x00\x00'
    blob = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', ihdr) \
        + chunk(b'IDAT', zlib.compress(bytes(raw), 9)) \
        + chunk(b'IEND', b'')
    open(path, 'wb').write(blob)


def lift_alpha(w, h, px):
    n = 0
    for i in range(3, w * h * 4, 4):
        a = px[i]
        if 1 <= a <= 249:
            px[i] = 200 + round(a * 53 / 255)
            n += 1
    return n


def main():
    cmd = sys.argv[1]
    if cmd == 'stats':
        w, h, px = read_png(sys.argv[2])
        buckets = {'a=0': 0, '1..120': 0, '121..249': 0, '250..255': 0}
        for i in range(3, w * h * 4, 4):
            a = px[i]
            buckets['a=0' if a == 0 else '1..120' if a <= 120 else '121..249' if a <= 249 else '250..255'] += 1
        print(sys.argv[2], w, 'x', h, buckets)
    elif cmd == 'lift':
        w, h, px = read_png(sys.argv[2])
        n = lift_alpha(w, h, px)
        write_png(sys.argv[3], w, h, px)
        print('%s -> %s : lifted %d semi-transparent texels to the 200..253 band' % (sys.argv[2], sys.argv[3], n))
    else:
        raise SystemExit('unknown command ' + cmd)


if __name__ == '__main__':
    main()
