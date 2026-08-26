#!/usr/bin/env python3
"""Generate the Batch-15 atmosphere textures for DaBy Wither Storm.

Pure stdlib (zlib/struct) PNG codec so it runs anywhere:
- textures/misc/star.png          twinkling 4-point star sprite for the skybox dome
- textures/misc/halo_ring.png     feathered ring used for cataclysm halos AND the black glare ring
- textures/misc/mcsm_cloud.png    soft slab sprite for the orbiting MCSM cloud deck
- textures/entity/wither_storm_og.png    OG skin: obsidian-purple command block tiles + violet sheen
- textures/entity/phase_4_assets_og.png  OG skin: glossy near-black with purple highlights
- textures/entity/devourer_assets_og.png OG skin: same sheen for the devourer/severed form
"""
import struct, zlib, math, os, random

ROOT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src/main/resources/assets/dabywitherstormmod/textures")

# ---------------- PNG codec ----------------

def _paeth(a, b, c):
    p = a + b - c
    pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
    if pa <= pb and pa <= pc:
        return a
    return b if pb <= pc else c

def read_png(path):
    d = open(path, "rb").read()
    assert d[:8] == b"\x89PNG\r\n\x1a\n", path
    off, idat, w, h, ct = 8, bytearray(), 0, 0, 6
    while off < len(d):
        ln = struct.unpack(">I", d[off:off+4])[0]
        typ = d[off+4:off+8]
        if typ == b"IHDR":
            w, h, bd, ct, cm, fm, im = struct.unpack(">IIBBBBB", d[off+8:off+8+13])
            assert bd == 8 and ct == 6 and im == 0, (path, bd, ct, im)
        elif typ == b"IDAT":
            idat += d[off+8:off+8+ln]
        elif typ == b"IEND":
            break
        off += 12 + ln
    raw = zlib.decompress(bytes(idat))
    stride = w * 4
    out = bytearray(w * h * 4)
    prev = bytearray(stride)
    pos = 0
    for y in range(h):
        f = raw[pos]; pos += 1
        line = bytearray(raw[pos:pos+stride]); pos += stride
        if f == 1:
            for i in range(4, stride):
                line[i] = (line[i] + line[i-4]) & 0xFF
        elif f == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 0xFF
        elif f == 3:
            for i in range(stride):
                left = line[i-4] if i >= 4 else 0
                line[i] = (line[i] + ((left + prev[i]) >> 1)) & 0xFF
        elif f == 4:
            for i in range(stride):
                left = line[i-4] if i >= 4 else 0
                ul = prev[i-4] if i >= 4 else 0
                line[i] = (line[i] + _paeth(left, prev[i], ul)) & 0xFF
        out[y*stride:(y+1)*stride] = line
        prev = line
    return w, h, out

def write_png(path, w, h, px):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        raw += px[y*w*4:(y+1)*w*4]
    comp = zlib.compress(bytes(raw), 9)
    def chunk(typ, data):
        return struct.pack(">I", len(data)) + typ + data + struct.pack(">I", zlib.crc32(typ + data) & 0xFFFFFFFF)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)) + chunk(b"IDAT", comp) + chunk(b"IEND", b"")
    open(path, "wb").write(png)
    print("wrote", os.path.relpath(path, ROOT), f"{w}x{h}", len(png), "bytes")

# ---------------- sprites ----------------

def gen_star():
    S = 16
    px = bytearray(S*S*4)
    c = (S-1)/2.0
    for y in range(S):
        for x in range(S):
            dx, dy = x-c, y-c
            d2 = dx*dx + dy*dy
            core = math.exp(-d2/2.2) * 1.2
            spike = math.exp(-(abs(dx)*0.55 + abs(dy)*1.8)) + math.exp(-(abs(dy)*0.55 + abs(dx)*1.8))
            a = min(1.0, core + spike*0.55)
            i = (y*S+x)*4
            px[i:i+4] = bytes((255, 255, 255, int(a*255)))
    write_png(os.path.join(ROOT, "misc/star.png"), S, S, px)

def gen_halo_ring():
    S = 64
    px = bytearray(S*S*4)
    c = (S-1)/2.0
    rng = random.Random(41)
    for y in range(S):
        for x in range(S):
            dx, dy = x-c, y-c
            r = math.sqrt(dx*dx+dy*dy) / (S/2.0)
            band = (r - 0.62) / 0.10
            ring = math.exp(-band*band*2.0)
            inner = max(0.0, 1.0 - r*0.9) * 0.22
            a = min(1.0, ring + inner)
            i = (y*S+x)*4
            px[i:i+4] = bytes((255, 255, 255, int(a*255)))
    write_png(os.path.join(ROOT, "misc/halo_ring.png"), S, S, px)

def gen_mcsm_cloud():
    S = 64
    px = bytearray(S*S*4)
    c = (S-1)/2.0
    rng = random.Random(7)
    noise = [[rng.random() for _ in range(S//8+2)] for _ in range(S//8+2)]
    def bilin(nx, ny):
        x0, y0 = int(nx), int(ny)
        fx, fy = nx-x0, ny-y0
        a = noise[y0][x0]*(1-fx)+noise[y0][x0+1]*fx
        b = noise[y0+1][x0]*(1-fx)+noise[y0+1][x0+1]*fx
        return a*(1-fy)+b*fy
    for y in range(S):
        for x in range(S):
            nx = (x-c)/(S/2.0)
            ny = (y-c)/(S/2.0)
            d = math.sqrt(nx*nx + (ny*2.1)**2)      # flattened: wide horizontal slab
            edge = 1.0/(1.0+math.exp((d-0.75)*9.0)) # smooth band edge
            n = 0.72 + 0.28*bilin(x/8.0, y/8.0)
            a = min(1.0, edge*n)
            shade = 0.82 + 0.18*(1.0-d)
            i = (y*S+x)*4
            px[i:i+4] = bytes((int(255*shade), int(255*shade), int(255*shade), int(a*255)))
    write_png(os.path.join(ROOT, "misc/mcsm_cloud.png"), S, S, px)

# ---------------- OG skins ----------------

def og_wither_storm():
    src = os.path.join(ROOT, "entity/wither_storm.png")
    w, h, px = read_png(src)
    out = bytearray(px)
    for i in range(0, len(out), 4):
        r, g, b, a = out[i], out[i+1], out[i+2], out[i+3]
        if a == 0:
            continue
        if r > 110 and r > g*1.35 and b < 95:  # vanilla orange command-block tile
            lum = r/255.0
            obs = 10 + lum*26; pur = 4 + lum*18; glo = 30 + lum*(150 if lum > 0.72 else 88)
            out[i] = int(min(255, obs + (lum-0.5)*30))
            out[i+1] = int(min(255, pur))
            out[i+2] = int(min(255, glo))
        else:  # body: deepen blacks, violet lift on brighter flesh
            lum = (0.30*r + 0.59*g + 0.11*b)/255.0
            out[i]   = int(min(255, r*0.94 + lum*22))
            out[i+1] = int(min(255, g*0.90))
            out[i+2] = int(max(0, min(255, b*0.96*1.02 + lum*40)))
    write_png(os.path.join(ROOT, "entity/wither_storm_og.png"), w, h, out)

def og_big(src_name, out_name, contrast, purple_light):
    w, h, px = read_png(os.path.join(ROOT, "entity/"+src_name))
    out = bytearray(px)
    for i in range(0, len(out), 4):
        r, g, b, a = out[i], out[i+1], out[i+2], out[i+3]
        if a == 0:
            continue
        lum = (0.30*r + 0.59*g + 0.11*b)/255.0
        c = lum ** contrast                      # S-curve-ish: sink mids, keep deep blacks
        lift = c*purple_light
        out[i]   = int(min(255, r*(0.9+0.1*c) + lift*0.55))
        out[i+1] = int(min(255, g*(0.86+0.08*c)))
        out[i+2] = int(min(255, b*(0.95+0.25*c) + lift))
    write_png(os.path.join(ROOT, "entity/"+out_name), w, h, out)

if __name__ == "__main__":
    gen_star()
    gen_halo_ring()
    gen_mcsm_cloud()
    og_wither_storm()
    og_big("phase_4_assets.png", "phase_4_assets_og.png", 1.18, 46)
    og_big("devourer_assets.png", "devourer_assets_og.png", 1.15, 52)
    print("all textures generated")
