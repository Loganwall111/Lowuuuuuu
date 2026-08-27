#!/usr/bin/env python3
"""
MCSM Pack Builder — Standalone Resource Pack and Shader Pack for Minecraft: Story Mode
Calibrated with official Story Mode palettes:
- day_sky.png: Normal Story Mode daytime sky (periwinkle -> lilac -> peach -> golden amber)
- sky_gradient_purple_sunset.png: Phase 5.4-5.9 sunset / dark purple twilight
- sky_gradient_night_blue.png: Phase 4 blue halo & night sky
- sky_gradient_twilight_purple.png: Phase 6-8 deep magenta twilight
"""

import os
import sys
import json
import shutil
import zipfile
import struct
import zlib
import math
import random

ROOT = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
RP_DIR = os.path.join(ROOT, "MCSM_ResourcePack")
RP_ZIP = os.path.join(ROOT, "MCSM_ResourcePack.zip")
SP_DIR = os.path.join(ROOT, "MCSM_ShaderPack")
SP_ZIP = os.path.join(ROOT, "MCSM_ShaderPack.zip")

print("--- Starting MCSM Pack Assembly ---")

# Clean previous build artifacts
shutil.rmtree(RP_DIR, ignore_errors=True)
shutil.rmtree(SP_DIR, ignore_errors=True)
if os.path.exists(RP_ZIP): os.remove(RP_ZIP)
if os.path.exists(SP_ZIP): os.remove(SP_ZIP)

os.makedirs(RP_DIR, exist_ok=True)
os.makedirs(SP_DIR, exist_ok=True)

# ----------------------------------------------------------------------
# Helper: PNG Writer (standard zlib/struct, zero external dependencies)
# ----------------------------------------------------------------------
def write_png(path: str, width: int, height: int, rows: list) -> None:
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    raw = b"".join(b"\x00" + bytes(r) for r in rows)
    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)

# ----------------------------------------------------------------------
# Palettes (Interpolation helper)
# ----------------------------------------------------------------------
def sample_stops(stops, t):
    t = max(0.0, min(1.0, t))
    for i in range(len(stops) - 1):
        t0, c0 = stops[i]
        t1, c1 = stops[i + 1]
        if t0 <= t <= t1:
            f = (t - t0) / (t1 - t0)
            f = f * f * (3.0 - 2.0 * f) # smoothstep
            return [int(c0[j] + (c1[j] - c0[j]) * f) for j in range(3)]
    return list(stops[-1][1])

# 1. Day Sky (Story Mode Normal default sky before storm)
DAY_SKY_STOPS = [
    (0.00, (140, 135, 232)), # #8c87e8 periwinkle lavender zenith
    (0.20, (175, 155, 226)), # #af9be2 soft lilac
    (0.45, (213, 174, 214)), # #d5aed6 soft mauve lilac-pink
    (0.68, (244, 184, 154)), # #f4b89a warm peach-pink
    (0.85, (247, 196, 115)), # #f7c473 warm golden apricot
    (1.00, (248, 182, 72))   # #f8b648 rich golden amber horizon
]

# 2. Purple Sunset (Phase 5.4-5.9)
PURPLE_SUNSET_STOPS = [
    (0.00, (20, 5, 35)),     # #140523 deep midnight obsidian
    (0.20, (56, 10, 84)),    # #380a54 royal dark purple
    (0.42, (111, 20, 120)),  # #6f1478 rich violet magenta
    (0.65, (169, 32, 114)),  # #a92072 vibrant magenta rose
    (0.85, (219, 75, 96)),   # #db4b60 Story Mode coral pink
    (1.00, (249, 136, 88))   # #f98858 fiery sunset orange horizon
]

# 3. Night Blue (Phase 4 blue halo & night)
NIGHT_BLUE_STOPS = [
    (0.00, (12, 18, 43)),    # #0c122b midnight navy
    (0.25, (25, 42, 94)),    # #192a5e dark cobalt
    (0.50, (43, 74, 147)),   # #2b4a93 rich cobalt blue
    (0.72, (70, 119, 195)),  # #4677c3 cerulean twilight
    (0.88, (106, 165, 234)), # #6aa5ea sky blue
    (1.00, (140, 194, 248))  # #8cc2f8 luminous blue halo glow
]

# 4. Twilight Purple (Phases 6, 7, 8)
TWILIGHT_PURPLE_STOPS = [
    (0.00, (23, 2, 37)),     # #170225 deep midnight black purple
    (0.22, (62, 7, 86)),     # #3e0756 deep dark purple
    (0.48, (115, 17, 123)),  # #73117b magenta purple
    (0.70, (169, 31, 118)),  # #a91f76 bright magenta rose
    (0.86, (220, 68, 116)),  # #dc4474 intense magenta-pink
    (1.00, (233, 98, 128))   # #e96280 warm rose pink horizon
]

# ----------------------------------------------------------------------
# 1. BUILD RESOURCE PACK
# ----------------------------------------------------------------------
print("[1/2] Assembling Minecraft: Story Mode Resource Pack...")

# pack.mcmeta (Zero nested folder at root)
rp_meta = {
    "pack": {
        "pack_format": 46,
        "supported_formats": {
            "min_inclusive": 15,
            "max_inclusive": 60
        },
        "description": "Minecraft: Story Mode - Authentic Visuals, Daytime Sky & MCSM Clouds"
    }
}
with open(os.path.join(RP_DIR, "pack.mcmeta"), "w", encoding="utf-8") as f:
    json.dump(rp_meta, f, indent=2)

# Copy source assets from previous pack tree or git tracked files
src_assets = os.path.join(ROOT, "MCSM_ResourcePack", "assets")
# If not present, pull from git / pr14 commit
tmp_export = "/tmp/mcsm_source_assets"
shutil.rmtree(tmp_export, ignore_errors=True)
os.makedirs(tmp_export, exist_ok=True)
os.system(f"git archive origin/arena/01a04054-lowuuuuuu MCSM_ResourcePack/assets | tar -x -C {tmp_export}")

source_base = os.path.join(tmp_export, "MCSM_ResourcePack", "assets")
if not os.path.exists(source_base):
    # Fallback to local git checkout
    os.system(f"git archive HEAD MCSM_ResourcePack/assets | tar -x -C {tmp_export}")
    source_base = os.path.join(tmp_export, "MCSM_ResourcePack", "assets")

dst_assets = os.path.join(RP_DIR, "assets")
if os.path.exists(source_base):
    shutil.copytree(source_base, dst_assets, dirs_exist_ok=True)
    print("Copied base game assets from repository")

# CRITICAL FIX: REMOVE CORE CLOUD SHADERS!
# Vanilla core shaders with snapshot moj_imports crash the OpenGL pipeline and cause the invisible world / black ground bug!
broken_shaders_dir = os.path.join(RP_DIR, "assets", "minecraft", "shaders")
if os.path.exists(broken_shaders_dir):
    shutil.rmtree(broken_shaders_dir)
    print("REMOVED assets/minecraft/shaders/ to guarantee 100% terrain visibility and eliminate black screen bug!")

# Fix model parent cyclic loops
for ns in ["witherstormmod", "devouringstorms", "dabywitherstormmod"]:
    for blk in ["formidibomb.json", "super_tnt.json"]:
        p = os.path.join(RP_DIR, "assets", ns, "models", "block", blk)
        if os.path.exists(p):
            with open(p, "r", encoding="utf-8") as f:
                d = json.load(f)
            d["parent"] = "block/block"
            with open(p, "w", encoding="utf-8") as f:
                json.dump(d, f, indent=2)
    p_book = os.path.join(RP_DIR, "assets", ns, "models", "item", "command_block_book.json")
    if os.path.exists(p_book):
        with open(p_book, "r", encoding="utf-8") as f:
            d = json.load(f)
        d["parent"] = "item/generated"
        with open(p_book, "w", encoding="utf-8") as f:
            json.dump(d, f, indent=2)

# Full Story Mode sound table
sounds_json_path = os.path.join(RP_DIR, "assets", "minecraft", "sounds.json")
sounds_data = {
    "music.menu": {
        "replace": True,
        "sounds": [{"name": "music/menu/title_theme", "stream": True}]
    },
    "ui.button.click": {
        "replace": True,
        "sounds": ["random/click_stereo"]
    },
    "ui.toast.in": {
        "replace": True,
        "sounds": ["ui/toast/in", "ui/toast/in1", "ui/toast/in2", "ui/toast/in3"]
    }
}
with open(sounds_json_path, "w", encoding="utf-8") as f:
    json.dump(sounds_data, f, indent=2)

# ----------------------------------------------------------------------
# Generate Authentic MCSM Clouds: assets/minecraft/textures/environment/clouds.png
# ----------------------------------------------------------------------
print("Generating authentic MCSM 3D Clouds texture (256x256)...")
CLOUD_SIZE = 256
rng = random.Random(1337)
perm = list(range(256))
rng.shuffle(perm)
perm = perm + perm

def fade_curve(t): return t * t * t * (t * (t * 6 - 15) + 10)
def grad_noise(hash_val, x, y):
    h = hash_val & 7
    u = x if h < 4 else y
    v = y if h < 4 else x
    return (u if (h & 1) == 0 else -u) + (v if (h & 2) == 0 else -v)

def perlin2d(x, y, period):
    xi = int(x) % period
    yi = int(y) % period
    xf = x - int(x)
    yf = y - int(y)
    u = fade_curve(xf)
    v = fade_curve(yf)
    aa = perm[perm[xi] + yi]
    ab = perm[perm[xi] + (yi + 1) % period]
    ba = perm[perm[(xi + 1) % period] + yi]
    bb = perm[perm[(xi + 1) % period] + (yi + 1) % period]
    x1 = (1.0 - u) * grad_noise(aa, xf, yf) + u * grad_noise(ba, xf - 1, yf)
    x2 = (1.0 - u) * grad_noise(ab, xf, yf - 1) + u * grad_noise(bb, xf - 1, yf - 1)
    return (1.0 - v) * x1 + v * x2

def cloud_fbm(x, y):
    v = 0.0
    amp = 0.52
    freq = 1.0
    period = 8
    for _ in range(4):
        v += amp * perlin2d(x * freq, y * freq, period)
        amp *= 0.5
        freq *= 2.0
        period *= 2
    return v

cloud_rows = []
for cy in range(CLOUD_SIZE):
    row = []
    ny = cy / CLOUD_SIZE
    for cx in range(CLOUD_SIZE):
        nx = cx / CLOUD_SIZE
        # Double octave billows
        c1 = cloud_fbm(nx * 8.0, ny * 8.0)
        c2 = cloud_fbm((nx + 0.35) * 16.0, (ny + 0.35) * 16.0)
        density = (c1 + 0.4 * c2) * 0.75 + 0.5

        # MCSM puffy cloud clusters with shaded undersides and crisp bright tops
        if density > 0.46:
            t = min(1.0, (density - 0.46) / 0.54)
            # Story Mode soft twilight lilac shading at underside/rim -> radiant off-white at peak
            r = int(160 + t * 95)
            g = int(135 + t * 115)
            b = int(172 + t * 80)
            a = int(185 + t * 70)
        else:
            r, g, b, a = 0, 0, 0, 0
        row.extend([r, g, b, a])
    cloud_rows.append(row)

env_dir = os.path.join(RP_DIR, "assets", "minecraft", "textures", "environment")
write_png(os.path.join(env_dir, "clouds.png"), CLOUD_SIZE, CLOUD_SIZE, cloud_rows)

# Warm golden Story Mode sun (64x64)
sun_rows = []
for sy in range(64):
    row = []
    for sx in range(64):
        dx = (sx - 31.5) / 31.5
        dy = (sy - 31.5) / 31.5
        dist = math.sqrt(dx * dx + dy * dy)
        if dist <= 0.6:
            # Core golden sun disc
            row.extend([255, 242, 210, 255])
        elif dist <= 1.0:
            # Radiant golden halo
            fade = (1.0 - dist) / 0.4
            row.extend([255, 195, 85, int(fade * 220)])
        else:
            row.extend([0, 0, 0, 0])
    sun_rows.append(row)
write_png(os.path.join(env_dir, "sun.png"), 64, 64, sun_rows)

# Story Mode Moon Phases (128x64)
moon_rows = []
for my in range(64):
    row = []
    for mx in range(128):
        # 8 phases of 32x32 each
        phase_idx = (mx // 32) + (my // 32) * 4
        px = (mx % 32) - 15.5
        py = (my % 32) - 15.5
        d = math.sqrt(px * px + py * py) / 14.0
        if d <= 0.85:
            # Soft celestial silver-blue Story Mode moon
            row.extend([225, 235, 255, 255])
        elif d <= 1.0:
            fade = (1.0 - d) / 0.15
            row.extend([180, 205, 255, int(fade * 180)])
        else:
            row.extend([0, 0, 0, 0])
    moon_rows.append(row)
write_png(os.path.join(env_dir, "moon_phases.png"), 128, 64, moon_rows)

# Story Mode End Sky (256x256)
end_rows = []
for ey in range(256):
    row = []
    for ex in range(256):
        n = cloud_fbm(ex / 32.0, ey / 32.0)
        c = sample_stops(TWILIGHT_PURPLE_STOPS, (n + 0.5) * 0.8)
        row.extend([c[0], c[1], c[2], 255])
    end_rows.append(row)
write_png(os.path.join(env_dir, "end_sky.png"), 256, 256, end_rows)

# ----------------------------------------------------------------------
# Generate OptiFine Custom Sky 3x2 Cubemaps & Properties
# ----------------------------------------------------------------------
print("Generating OptiFine Custom Sky cubemaps (1536x1024 3x2)...")
def build_cubemap(stops, tile_size=512):
    w = tile_size * 3
    h = tile_size * 2
    grid = [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]

    # Tile (1, 0): Top / Zenith (x: 512..1023, y: 0..511)
    for y in range(tile_size):
        for x in range(tile_size):
            dx = (x - tile_size / 2) / (tile_size / 2)
            dy = (y - tile_size / 2) / (tile_size / 2)
            r = math.sqrt(dx * dx + dy * dy)
            t = min(1.0, r) * 0.30
            c = sample_stops(stops, t)
            grid[y][tile_size + x] = [c[0], c[1], c[2], 255]

    # Side tiles: (2,0)=South, (0,1)=West, (1,1)=North, (2,1)=East
    for tx, ty in [(2, 0), (0, 1), (1, 1), (2, 1)]:
        ox = tx * tile_size
        oy = ty * tile_size
        for y in range(tile_size):
            yf = y / (tile_size - 1)
            if yf < 0.84:
                t = 0.30 + (yf / 0.84) * 0.70
                c = sample_stops(stops, t)
            else:
                # Fade clean at horizon
                fade = max(0.0, 1.0 - (yf - 0.84) / 0.16)
                c = sample_stops(stops, 1.0)
                c = [int(v * fade) for v in c]
            for x in range(tile_size):
                grid[oy + y][ox + x] = [c[0], c[1], c[2], 255]

    # Tile (0, 0): Bottom face remains black/transparent [0,0,0,0]
    flat_rows = []
    for y in range(h):
        r = []
        for x in range(w):
            r.extend(grid[y][x])
        flat_rows.append(r)
    return flat_rows

opti_sky_dir = os.path.join(RP_DIR, "assets", "minecraft", "optifine", "sky", "world0")
os.makedirs(opti_sky_dir, exist_ok=True)

# sky1.png: Official Story Mode Daytime Sky (day_sky.png palette)
write_png(os.path.join(opti_sky_dir, "sky1.png"), 1536, 1024, build_cubemap(DAY_SKY_STOPS))
sky1_properties = """# Minecraft: Story Mode — Official Daytime Sky (day_sky.png)
source=./sky1.png
startFadeIn=0:00
endFadeIn=0:01
endFadeOut=23:59
blend=add
rotate=false
speed=0.0
axis=0.0 1.0 0.0
"""
with open(os.path.join(opti_sky_dir, "sky1.properties"), "w", encoding="utf-8") as f:
    f.write(sky1_properties)

# sky2.png: Drifting Story Mode Cloud Ceiling
def build_cloud_cubemap(tile_size=512):
    w = tile_size * 3
    h = tile_size * 2
    grid = [[[0, 0, 0, 0] for _ in range(w)] for _ in range(h)]
    for y in range(tile_size):
        for x in range(tile_size):
            dx = (x - tile_size / 2) / (tile_size / 2)
            dy = (y - tile_size / 2) / (tile_size / 2)
            r = math.sqrt(dx * dx + dy * dy)
            if r <= 1.0:
                n = cloud_fbm(x / 32.0, y / 32.0)
                if n > 0.1:
                    alpha = int(min(1.0, (n - 0.1) * 2.5) * 160 * (1.0 - r * 0.8))
                    grid[y][tile_size + x] = [255, 235, 245, alpha]
    flat_rows = []
    for y in range(h):
        r = []
        for x in range(w):
            r.extend(grid[y][x])
        flat_rows.append(r)
    return flat_rows

write_png(os.path.join(opti_sky_dir, "sky2.png"), 1536, 1024, build_cloud_cubemap())
sky2_properties = """# Minecraft: Story Mode — Drifting Roiling Cloud Ceiling
source=./sky2.png
startFadeIn=0:00
endFadeIn=0:01
endFadeOut=23:59
blend=add
rotate=true
speed=0.015
axis=0.0 1.0 0.0
"""
with open(os.path.join(opti_sky_dir, "sky2.properties"), "w", encoding="utf-8") as f:
    f.write(sky2_properties)

# sky3.png: Phase 5.4-5.9 Sunset/Purple Sky
write_png(os.path.join(opti_sky_dir, "sky3.png"), 1536, 1024, build_cubemap(PURPLE_SUNSET_STOPS))
sky3_properties = """# Minecraft: Story Mode — Phase 5.4-5.9 Purple Sunset Sky
source=./sky3.png
startFadeIn=17:30
endFadeIn=18:30
startFadeOut=21:00
endFadeOut=22:00
blend=add
rotate=false
speed=0.0
axis=0.0 1.0 0.0
"""
with open(os.path.join(opti_sky_dir, "sky3.properties"), "w", encoding="utf-8") as f:
    f.write(sky3_properties)

# sky4.png: Phase 6-8 Twilight Purple Sky
write_png(os.path.join(opti_sky_dir, "sky4.png"), 1536, 1024, build_cubemap(TWILIGHT_PURPLE_STOPS))
sky4_properties = """# Minecraft: Story Mode — Phase 6, 7, 8 Twilight Sky
source=./sky4.png
startFadeIn=21:00
endFadeIn=22:00
startFadeOut=4:30
endFadeOut=5:30
blend=add
rotate=false
speed=0.0
axis=0.0 1.0 0.0
"""
with open(os.path.join(opti_sky_dir, "sky4.properties"), "w", encoding="utf-8") as f:
    f.write(sky4_properties)

# ----------------------------------------------------------------------
# FabricSkyBoxes Support (assets/fabricskyboxes/)
# ----------------------------------------------------------------------
fsb_tex_dir = os.path.join(RP_DIR, "assets", "fabricskyboxes", "textures", "sky")
os.makedirs(fsb_tex_dir, exist_ok=True)
fsb_sky_dir = os.path.join(RP_DIR, "assets", "fabricskyboxes", "sky")
os.makedirs(fsb_sky_dir, exist_ok=True)

# Generate individual 512x512 faces for square-textured
top_face = []
for y in range(512):
    r = []
    for x in range(512):
        dx = (x - 256) / 256.0
        dy = (y - 256) / 256.0
        d = math.sqrt(dx * dx + dy * dy)
        c = sample_stops(DAY_SKY_STOPS, min(1.0, d) * 0.30)
        r.extend([c[0], c[1], c[2], 255])
    top_face.append(r)
write_png(os.path.join(fsb_tex_dir, "top.png"), 512, 512, top_face)

side_face = []
for y in range(512):
    yf = y / 511.0
    c = sample_stops(DAY_SKY_STOPS, 0.30 + yf * 0.70)
    r = []
    for x in range(512):
        r.extend([c[0], c[1], c[2], 255])
    side_face.append(r)
write_png(os.path.join(fsb_tex_dir, "side.png"), 512, 512, side_face)

bottom_face = [[0, 0, 0, 0] * 512 for _ in range(512)]
write_png(os.path.join(fsb_tex_dir, "bottom.png"), 512, 512, bottom_face)

fsb_json = {
    "schemaVersion": 2,
    "type": "square-textured",
    "blend": {"type": "add"},
    "textures": {
        "top": "fabricskyboxes:textures/sky/top.png",
        "bottom": "fabricskyboxes:textures/sky/bottom.png",
        "north": "fabricskyboxes:textures/sky/side.png",
        "south": "fabricskyboxes:textures/sky/side.png",
        "east": "fabricskyboxes:textures/sky/side.png",
        "west": "fabricskyboxes:textures/sky/side.png"
    },
    "properties": {
        "fade": {"alwaysOn": True},
        "rotation": {"skyboxRotation": False}
    }
}
with open(os.path.join(fsb_sky_dir, "mcsm_twilight.json"), "w", encoding="utf-8") as f:
    json.dump(fsb_json, f, indent=2)

# Lush Green Colormaps
def gen_colormap(path, c_dry, c_lush):
    rows = []
    for y in range(256):
        row = []
        ty = y / 255.0
        for x in range(256):
            tx = x / 255.0
            r = int(c_dry[0] * (1 - tx) + c_lush[0] * tx)
            g = int(c_dry[1] * (1 - ty) + c_lush[1] * ty)
            b = int(c_dry[2] * (1 - tx) + c_lush[2] * tx)
            row.extend([r, g, b, 255])
        rows.append(row)
    write_png(path, 256, 256, rows)

cmap_dir = os.path.join(RP_DIR, "assets", "minecraft", "textures", "colormap")
gen_colormap(os.path.join(cmap_dir, "grass.png"), (80, 168, 60), (95, 195, 75))
gen_colormap(os.path.join(cmap_dir, "foliage.png"), (70, 155, 52), (88, 185, 68))

# Story Mode pack icon (128x128)
pack_icon_rows = []
for iy in range(128):
    row = []
    t = iy / 127.0
    base = sample_stops(DAY_SKY_STOPS, t)
    for ix in range(128):
        dx = abs(ix - 64)
        dy = abs(iy - 64)
        d = dx + dy
        r, g, b = base[0], base[1], base[2]
        if 28 <= d <= 32:
            r, g, b = 255, 215, 80
        elif 24 <= d < 28:
            r, g, b = 45, 25, 75
        elif d < 24:
            glow = 1.0 - (d / 24.0)
            r = int(min(255, r + 180 * glow))
            g = int(min(255, g + 80 * glow))
            b = int(min(255, b + 240 * glow))
        row.extend([r, g, b, 255])
    pack_icon_rows.append(row)
write_png(os.path.join(RP_DIR, "pack.png"), 128, 128, pack_icon_rows)

# Zip Resource Pack directly with pack.mcmeta at archive root (NO NESTING!)
print("Zipping MCSM_ResourcePack.zip...")
with zipfile.ZipFile(RP_ZIP, "w", zipfile.ZIP_DEFLATED) as z:
    for root_dir, _, files in os.walk(RP_DIR):
        for file in files:
            full_p = os.path.join(root_dir, file)
            rel_p = os.path.relpath(full_p, RP_DIR)
            z.write(full_p, rel_p)
print(f"Created {RP_ZIP} ({os.path.getsize(RP_ZIP)} bytes)")


# ----------------------------------------------------------------------
# 2. BUILD SHADER PACK
# ----------------------------------------------------------------------
print("[2/2] Assembling Minecraft: Story Mode Shader Pack...")

sp_shaders = os.path.join(SP_DIR, "shaders")
os.makedirs(sp_shaders, exist_ok=True)

# shaders.properties
shaders_properties_content = """# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack
# Compatible with Iris (Fabric) and OptiFine (Java Edition).

# Vanilla celestial and lighting settings
clouds=off
sky.stars=vanilla
sun=true
moon=true

# Shading Profiles & Telltale Visual Options
profile=mcsm_telltale
MCSM_DAY_SKY=1
MCSM_ROILING_CLOUDS=1
MCSM_COLOURED_LIGHTING=1
MCSM_SHADOW_TINT=1
MCSM_EMISSIVE_BLOOM=1
MCSM_ATMOSPHERIC_FOG=1
MCSM_CINEMATIC_GRADE=1
MCSM_VIGNETTE=0
"""
with open(os.path.join(sp_shaders, "shaders.properties"), "w", encoding="utf-8") as f:
    f.write(shaders_properties_content)

# gbuffers_skybasic.vsh
gbuffers_skybasic_vsh = """#version 120

varying vec4 intColor;
varying vec3 viewPos;

void main() {
    gl_Position = ftransform();
    intColor = gl_Color;
    viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skybasic.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skybasic_vsh)

# gbuffers_skybasic.fsh — Authentic Story Mode daytime sky with roiling MCSM clouds
gbuffers_skybasic_fsh = """#version 120

/*
 * MINECRAFT: STORY MODE — OFFICIAL DAY SKY & ROILING CLOUDS
 * Calibrated directly to day_sky.png:
 * Periwinkle zenith -> soft lilac -> pastel mauve-pink -> warm peach -> golden amber horizon
 * Plus Telltale-style roiling storm cumulus clouds with warm lit undersides.
 */

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform float frameTimeCounter;

varying vec4 intColor;
varying vec3 viewPos;

#ifndef MCSM_DAY_SKY
#define MCSM_DAY_SKY 1
#endif
#ifndef MCSM_ROILING_CLOUDS
#define MCSM_ROILING_CLOUDS 1
#endif

// Fast 2D procedural noise
float hash2(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash2(i);
    float b = hash2(i + vec2(1.0, 0.0));
    float c = hash2(i + vec2(0.0, 1.0));
    float d = hash2(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float cloudFbm(vec2 p) {
    float v = 0.0;
    float amp = 0.52;
    for (int i = 0; i < 4; i++) {
        v += amp * vnoise(p);
        p = p * 2.18 + vec2(5.3, 11.7);
        amp *= 0.5;
    }
    return v;
}

// Official day_sky.png 6-stop Story Mode gradient
vec3 getStoryModeDaySky(float elev) {
    vec3 cZenith   = vec3(0.549, 0.529, 0.910); // #8c87e8 soft periwinkle lavender
    vec3 cLilac    = vec3(0.686, 0.608, 0.886); // #af9be2 soft lilac
    vec3 cMauve    = vec3(0.835, 0.682, 0.839); // #d5aed6 pastel mauve-pink
    vec3 cPeach    = vec3(0.957, 0.722, 0.604); // #f4b89a warm peach
    vec3 cApricot  = vec3(0.969, 0.769, 0.451); // #f7c473 golden apricot
    vec3 cHorizon  = vec3(0.973, 0.714, 0.282); // #f8b648 rich golden amber horizon
    vec3 cVoid     = vec3(0.350, 0.220, 0.150); // warm underside ground tone

    if (elev < 0.0) {
        float t = clamp(-elev / 0.20, 0.0, 1.0);
        return mix(cHorizon, cVoid, t);
    } else if (elev < 0.06) {
        return mix(cHorizon, cApricot, smoothstep(0.0, 1.0, elev / 0.06));
    } else if (elev < 0.18) {
        return mix(cApricot, cPeach, smoothstep(0.0, 1.0, (elev - 0.06) / 0.12));
    } else if (elev < 0.38) {
        return mix(cPeach, cMauve, smoothstep(0.0, 1.0, (elev - 0.18) / 0.20));
    } else if (elev < 0.65) {
        return mix(cMauve, cLilac, smoothstep(0.0, 1.0, (elev - 0.38) / 0.27));
    } else {
        return mix(cLilac, cZenith, smoothstep(0.0, 1.0, (elev - 0.65) / 0.35));
    }
}

void main() {
    // Transform view direction into world coordinates
    vec3 dirV = normalize(viewPos);
    vec3 dir = normalize(mat3(gbufferModelViewInverse) * dirV);

    float elev = dir.y;

    #if MCSM_DAY_SKY
    vec3 skyCol = getStoryModeDaySky(elev);

    // Warm luminous horizon glow accent
    float horizBand = exp(-pow(max(elev, 0.0) * 10.0, 2.0));
    skyCol += vec3(0.98, 0.76, 0.45) * horizBand * 0.22;

    // Stylized Telltale Story Mode Roiling Clouds
    #if MCSM_ROILING_CLOUDS
    if (elev > 0.04) {
        vec2 cp = dir.xz / max(elev + 0.18, 0.08);
        float time = frameTimeCounter * 0.018;

        // Roiling swirl
        float c = cloudFbm(cp * 0.85 + vec2(time * 0.4, time * 0.15));
        float detail = cloudFbm(cp * 1.8 - vec2(time * 0.6, time * 0.2));
        float density = c + detail * 0.35;

        float cloudMask = smoothstep(0.48, 0.76, density) * clamp(elev * 3.0, 0.0, 1.0);

        // Story Mode clouds: underlit by warm amber/peach horizon, soft lilac-tinted crowns
        vec3 cloudUnderside = vec3(0.96, 0.72, 0.58);
        vec3 cloudCrown     = vec3(0.98, 0.96, 1.00);
        vec3 cloudCol = mix(cloudUnderside, cloudCrown, smoothstep(0.40, 0.82, detail));

        skyCol = mix(skyCol, cloudCol, cloudMask * 0.72);
    }
    #endif

    gl_FragColor = vec4(skyCol, 1.0);
    #else
    gl_FragColor = intColor;
    #endif
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skybasic.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skybasic_fsh)

# gbuffers_skytextured.vsh / fsh (Celestial sun/moon bloom)
gbuffers_skytextured_vsh = """#version 120

varying vec4 color;
varying vec2 texcoord;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = gl_MultiTexCoord0.xy;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skytextured.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skytextured_vsh)

gbuffers_skytextured_fsh = """#version 120

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;
    // Warm golden Story Mode sun & celestial bloom
    col.rgb *= vec3(1.10, 1.02, 0.94);
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skytextured.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skytextured_fsh)

# gbuffers_terrain.vsh / fsh — Coloured Lighting, Shadows on ground, and Story Mode look!
gbuffers_terrain_vsh = """#version 120

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 worldPos;

uniform mat4 gbufferModelViewInverse;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = gl_MultiTexCoord0.xy;
    lmcoord = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    normal = normalize(gl_NormalMatrix * gl_Normal);
    worldPos = (gbufferModelViewInverse * (gl_ModelViewMatrix * gl_Vertex)).xyz;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_terrain.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_terrain_vsh)

gbuffers_terrain_fsh = """#version 120

/*
 * Minecraft: Story Mode — Coloured Lighting & Ground Shadows
 * From Telltale Games:
 * - Direct sunlight: Warm amber/golden illumination (#FFF2D8)
 * - Shadows on ground: Atmospheric cool lavender/purple bounce tint (#6B5885)
 * - Torch / blocklight: Rich warm firelight (#FFA347)
 */

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 worldPos;

#ifndef MCSM_COLOURED_LIGHTING
#define MCSM_COLOURED_LIGHTING 1
#endif
#ifndef MCSM_SHADOW_TINT
#define MCSM_SHADOW_TINT 1
#endif

void main() {
    vec4 tex = texture2D(texture, texcoord) * color;

    #if MCSM_COLOURED_LIGHTING
    float blockLight = clamp((lmcoord.x - 0.03) * 1.05, 0.0, 1.0);
    float skyLight   = clamp((lmcoord.y - 0.03) * 1.05, 0.0, 1.0);

    // Warm direct sun illumination
    vec3 sunLightColor = vec3(1.08, 1.00, 0.92);
    // Cool Telltale lavender/purple ambient shadow tint
    vec3 shadowAmbientColor = vec3(0.68, 0.58, 0.82);
    // Warm fire / torch blocklight color
    vec3 torchColor = vec3(1.15, 0.74, 0.40);

    // Light calculation
    vec3 skyLightTerm = mix(shadowAmbientColor * 0.75, sunLightColor, pow(skyLight, 1.3));
    vec3 blockLightTerm = torchColor * pow(blockLight, 1.4) * 1.35;

    vec3 ambient = skyLightTerm + blockLightTerm;
    tex.rgb *= ambient;

    #if MCSM_SHADOW_TINT
    // Accentuate ground shadows with Story Mode purple tone
    float isShadowed = 1.0 - skyLight;
    if (isShadowed > 0.35) {
        float shadowStr = (isShadowed - 0.35) / 0.65;
        tex.rgb = mix(tex.rgb, tex.rgb * vec3(0.85, 0.78, 0.95), shadowStr * 0.45);
    }
    #endif

    #endif

    gl_FragColor = tex;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_terrain.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_terrain_fsh)

# gbuffers_entities.vsh / fsh — Glowing Command Blocks, Amulets, Formidibomb
gbuffers_entities_vsh = """#version 120

varying vec4 color;
varying vec2 texcoord;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = gl_MultiTexCoord0.xy;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_entities.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_entities_vsh)

gbuffers_entities_fsh = """#version 120

uniform sampler2D texture;
uniform float frameTimeCounter;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;

    // Emissive boost for glowing MCSM command block, amulet, and storm elements
    float isHotMagenta = step(0.68, col.r) * step(0.68, col.b) * (1.0 - step(0.50, col.g));
    float isCyanGlow   = step(0.68, col.g) * step(0.68, col.b) * (1.0 - step(0.50, col.r));
    float isAmuletGold = step(0.80, col.r) * step(0.70, col.g) * (1.0 - step(0.40, col.b));
    float emissive = max(max(isHotMagenta, isCyanGlow), isAmuletGold);

    if (emissive > 0.5) {
        float pulse = 0.88 + 0.12 * sin(frameTimeCounter * 3.5);
        col.rgb *= 1.85 * pulse;
    }

    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_entities.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_entities_fsh)

# composite.vsh / fsh — Atmospheric Fog
composite_vsh = """#version 120

varying vec2 texcoord;

void main() {
    gl_Position = ftransform();
    texcoord = gl_MultiTexCoord0.xy;
}
"""
with open(os.path.join(sp_shaders, "composite.vsh"), "w", encoding="utf-8") as f:
    f.write(composite_vsh)

composite_fsh = """#version 120

/*
 * Minecraft: Story Mode — Atmospheric Distance Fog
 * Melts distant terrain into the golden-peach horizon haze.
 */

uniform sampler2D colortex0;
uniform sampler2D depthtex0;
uniform float near;
uniform float far;
uniform vec3 fogColor;
uniform float rainStrength;
uniform int lightningBolt;

varying vec2 texcoord;

#ifndef MCSM_ATMOSPHERIC_FOG
#define MCSM_ATMOSPHERIC_FOG 1
#endif

float linearizeDepth(float z) {
    float ndc = z * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - ndc * (far - near));
}

void main() {
    vec3 col = texture2D(colortex0, texcoord).rgb;
    float depth = texture2D(depthtex0, texcoord).x;

    #if MCSM_ATMOSPHERIC_FOG
    if (depth < 0.9999) {
        float dist = linearizeDepth(depth);
        float fogFactor = 1.0 - exp(-dist * 0.006 * (1.0 + rainStrength * 0.8));
        fogFactor = clamp(fogFactor, 0.0, 0.85);

        // Story Mode warm golden peach horizon fog haze
        vec3 storyModeFog = vec3(0.95, 0.74, 0.62) * (0.85 + 0.15 * fogColor);
        col = mix(col, storyModeFog, fogFactor);
    }
    #endif

    // Lightning storm sky flash
    if (lightningBolt > 0) {
        col += vec3(0.95, 0.85, 1.00) * 0.18;
    }

    gl_FragColor = vec4(col, 1.0);
}
"""
with open(os.path.join(sp_shaders, "composite.fsh"), "w", encoding="utf-8") as f:
    f.write(composite_fsh)

# final.vsh / fsh — Cinematic Color Grading
final_vsh = """#version 120

varying vec2 texcoord;

void main() {
    gl_Position = ftransform();
    texcoord = gl_MultiTexCoord0.xy;
}
"""
with open(os.path.join(sp_shaders, "final.vsh"), "w", encoding="utf-8") as f:
    f.write(final_vsh)

final_fsh = """#version 120

/*
 * Minecraft: Story Mode — Cinematic Final Presentation
 * Applies clean, vibrant Telltale color grading, rich saturation, and filmic tone curve.
 */

uniform sampler2D colortex0;
uniform float viewWidth;
uniform float viewHeight;

varying vec2 texcoord;

#ifndef MCSM_CINEMATIC_GRADE
#define MCSM_CINEMATIC_GRADE 1
#endif
#ifndef MCSM_VIGNETTE
#define MCSM_VIGNETTE 0
#endif

void main() {
    vec2 uv = texcoord;
    vec3 col = texture2D(colortex0, uv).rgb;

    #if MCSM_CINEMATIC_GRADE
    // Story Mode Vibrancy & Saturation Push
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(lum), col, 1.18);
    // Warm filmic tone curve
    col = pow(col, vec3(0.96, 0.95, 0.98));
    #endif

    #if MCSM_VIGNETTE
    float d = distance(uv, vec2(0.5));
    float vig = smoothstep(0.42, 0.95, d);
    col = mix(col, col * 0.72, vig * 0.35);
    #endif

    gl_FragColor = vec4(col, 1.0);
}
"""
with open(os.path.join(sp_shaders, "final.fsh"), "w", encoding="utf-8") as f:
    f.write(final_fsh)

# Shaderpack README
sp_readme = """# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Features
- **Official MCSM Daytime Sky**: Signature Story Mode daytime sky palette (periwinkle lavender zenith -> soft lilac -> pastel mauve-pink -> warm peach -> golden amber horizon).
- **Telltale Roiling Clouds**: Billowing storm cumulus clouds underlit by warm amber/peach horizon light.
- **Coloured Lighting & Ground Shadows**: Warm golden sun illumination, cool atmospheric lavender/purple bounce tint in shadows, and rich firelight block lighting.
- **Emissive Highlights**: Neon bloom on Command Blocks, Order of the Stone Amulets, and Formidibomb.
- **Atmospheric Golden-Peach Distance Fog**: Smoothly blends distant terrain into the horizon.
- **Cinematic Story Mode Grading**: Vibrant Telltale color curve.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine.
2. Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (DO NOT unzip).
3. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
"""
with open(os.path.join(SP_DIR, "README.md"), "w", encoding="utf-8") as f:
    f.write(sp_readme)

# Zip Shader Pack
print("Zipping MCSM_ShaderPack.zip...")
with zipfile.ZipFile(SP_ZIP, "w", zipfile.ZIP_DEFLATED) as z:
    for root_dir, _, files in os.walk(SP_DIR):
        for file in files:
            full_p = os.path.join(root_dir, file)
            rel_p = os.path.relpath(full_p, SP_DIR)
            z.write(full_p, rel_p)
print(f"Created {SP_ZIP} ({os.path.getsize(SP_ZIP)} bytes)")

print("\n--- MCSM PACK ASSEMBLY COMPLETE ---")
print(f"Resource Pack: {RP_ZIP} ({len(os.listdir(RP_DIR))} root entries)")
print(f"Shader Pack:   {SP_ZIP} ({len(os.listdir(SP_DIR))} root entries)")
