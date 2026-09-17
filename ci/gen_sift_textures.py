#!/usr/bin/env python3
import os, zlib, struct, random
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "jar-overrides", "assets", "mcsm_sift", "textures", "block")
os.makedirs(OUT, exist_ok=True)

BLOCKS_COLORS = {
"blue_grass_block": (60,120,255),
"pink_grass_block": (255,120,180),
"red_grass_block": (200,50,50),
"red_rock": (180,40,40),
"black_water": (10,10,20),
"green_acid": (80,255,100),
"purple_tree_leaves": (150,80,255),
"purple_tree_log": (100,60,120),
"fluor_plant": (120,255,200),
"fungus_tree": (200,180,120),
"red_vines": (200,60,60),
"blue_bun": (100,150,255),
"rainbow_water": (255,100,255),
"cyan_moss": (80,200,200),
"void_lily": (200,100,255),
"gel_crystal": (100,255,255),
"sponge_bloom": (255,180,100),
"rift_glass": (150,100,255),
"prismatic_stone": (180,150,255),
"luminous_vine": (150,255,150),
"fabric_shard": (200,200,255),
"echo_soil": (80,80,100),
"starlit_grass": (100,180,255),
"void_blossom": (255,120,255),
"gel_honey": (255,220,100),
"crystalline_sponge": (180,220,255),
"rift_bloom": (200,100,255),
"displacement_stone": (120,100,150),
"iridescent_leaves": (150,255,200),
"iridescent_log": (120,180,150),
"void_fern": (100,200,120),
"glowing_mushroom": (255,200,100),
"fabric_roots": (150,120,180),
"cosmic_sand": (200,180,255),
"star_dust": (255,255,200),
"void_crystal_cluster": (150,200,255),
"gel_lantern": (255,255,150),
"rift_vein": (180,100,255),
"prismatic_vine": (200,150,255),
"void_berry_bush": (200,80,120),
"echo_crystal": (180,220,255),
"fabric_bloom": (255,150,200),
"cosmic_grass": (120,200,255),
}

def write_png(path, r,g,b):
    # 16x16 solid with slight noise
    w=h=16
    raw = bytearray()
    for y in range(h):
        raw.append(0) # filter type 0
        for x in range(w):
            nr = max(0,min(255, r + random.randint(-15,15)))
            ng = max(0,min(255, g + random.randint(-15,15)))
            nb = max(0,min(255, b + random.randint(-15,15)))
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

for name, (r,g,b) in BLOCKS_COLORS.items():
    write_png(os.path.join(OUT, name+".png"), r,g,b)

# also generate for new void blocks that may be missing textures
VOID_OUT = os.path.join(ROOT, "jar-overrides", "assets", "mcsm", "textures", "block")
os.makedirs(VOID_OUT, exist_ok=True)
VOID_COLORS = {
"void_spire": (60,40,80),
"void_spire_crystal": (120,200,255),
"void_gel": (80,100,120),
"void_gel_bloom": (100,200,180),
"rift_glass_void": (150,120,255),
"fracture_stone": (90,80,100),
"fracture_crystal": (180,150,255),
"abyssal_sand": (50,50,70),
"luminous_fungus": (150,255,150),
"void_vines": (80,120,80),
"gel_horizon_grass": (80,180,120),
"void_blossom_void": (200,120,255),
"sponge_heart": (255,150,100),
"rift_bloom_void": (200,100,255),
"prismatic_void_stone": (150,130,200),
"void_lantern": (255,240,150),
"echo_grass": (100,180,150),
"void_roots": (100,80,120),
"abyssal_crystal": (100,200,255),
"gel_crystal_void": (120,255,220),
"void_moss": (80,150,80),
"rift_soil": (70,60,90),
"fracture_vine": (120,100,150),
"void_berry_bush_void": (200,80,100),
"luminous_sponge": (180,255,180),
"void_crystal_block": (150,220,255),
"gel_lantern_void": (255,255,180),
"rift_crystal_void": (180,120,255),
"prismatic_void_vine": (200,150,255),
"void_shroom": (200,150,100),
}
for name,(r,g,b) in VOID_COLORS.items():
    p = os.path.join(VOID_OUT, name+".png")
    if not os.path.exists(p):
        write_png(p,r,g,b)

print(f"generated {len(BLOCKS_COLORS)} sift + {len(VOID_COLORS)} void textures")
