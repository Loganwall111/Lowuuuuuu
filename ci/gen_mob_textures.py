#!/usr/bin/env python3
import os, zlib, struct, random
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "jar-overrides", "assets", "mcsm", "textures", "entity")
os.makedirs(OUT, exist_ok=True)

# also for mcsm_sift namespace
OUT_SIFT = os.path.join(ROOT, "jar-overrides", "assets", "mcsm_sift", "textures", "entity")
os.makedirs(OUT_SIFT, exist_ok=True)
os.makedirs(os.path.join(OUT_SIFT, "void_whale"), exist_ok=True)

def write_png(path, r,g,b, w=64,h=64):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            # pattern
            nr = max(0,min(255, r + random.randint(-20,20) + (x%8)*2))
            ng = max(0,min(255, g + random.randint(-20,20) + (y%8)*2))
            nb = max(0,min(255, b + random.randint(-20,20)))
            raw.extend([nr,ng,nb,255])
    comp = zlib.compress(bytes(raw))
    def chunk(ct, data):
        c = struct.pack(">I", len(data)) + ct + data
        crc = struct.pack(">I", zlib.crc32(ct + data) & 0xffffffff)
        return c + crc
    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    png = sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', comp) + chunk(b'IEND', b'')
    with open(path,"wb") as f:
        f.write(png)

MOBS = {
"void_whale": (80,200,220),
"void_whale/void_whale": (80,200,220),
"void_whale/void_whale_glow": (150,255,255),
"colossal_octopus": (255,120,150),
"jokest_creature": (120,180,255),
"void_dweller": (200,150,255),
"void_dweller_glow": (255,200,255),
"dweller": (200,150,255),
}

for name,(r,g,b) in MOBS.items():
    if "/" in name:
        base, sub = name.split("/",1)
        dirpath = os.path.join(ROOT, "jar-overrides", "assets", "mcsm", "textures", "entity", base)
        os.makedirs(dirpath, exist_ok=True)
        p = os.path.join(dirpath, sub+".png")
    else:
        p = os.path.join(OUT, name+".png")
    if not os.path.exists(p):
        write_png(p,r,g,b)
    # also sift namespace
    if "/" in name:
        base, sub = name.split("/",1)
        dirpath = os.path.join(ROOT, "jar-overrides", "assets", "mcsm_sift", "textures", "entity", base)
        os.makedirs(dirpath, exist_ok=True)
        p2 = os.path.join(dirpath, sub+".png")
    else:
        p2 = os.path.join(OUT_SIFT, name+".png")
    if not os.path.exists(p2):
        write_png(p2,r,g,b)

# Ensure whale_monster exists in sift too
for src in ["whale_monster","voidwalker","void_lurker"]:
    src_path = os.path.join(OUT, src+".png")
    dst_path = os.path.join(OUT_SIFT, src+".png")
    if os.path.exists(src_path) and not os.path.exists(dst_path):
        import shutil
        shutil.copy(src_path, dst_path)

print("mob textures generated")
