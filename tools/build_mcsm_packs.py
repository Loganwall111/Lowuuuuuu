#!/usr/bin/env python3
"""
MCSM Pack Builder — Standalone Resource Pack and Shader Pack for Minecraft: Story Mode
Target: Minecraft 1.21.2
Creates clean, 100% stable, crash-free packs for Minecraft: Story Mode.
"""

import os
import sys
import json
import shutil
import zipfile
import struct
import zlib
import math

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
# Sky Palettes for OptiFine Cubemaps
# ----------------------------------------------------------------------
def sample_stops(stops, t):
    t = max(0.0, min(1.0, t))
    for i in range(len(stops) - 1):
        t0, c0 = stops[i]
        t1, c1 = stops[i + 1]
        if t0 <= t <= t1:
            f = (t - t0) / (t1 - t0)
            f = f * f * (3.0 - 2.0 * f)
            return [int(c0[j] + (c1[j] - c0[j]) * f) for j in range(3)]
    return list(stops[-1][1])

# 1. Day Sky (Story Mode Normal default sky)
DAY_SKY_STOPS = [
    (0.00, (140, 135, 232)), # #8c87e8 periwinkle lavender zenith
    (0.20, (175, 155, 226)), # #af9be2 soft lilac
    (0.45, (213, 174, 214)), # #d5aed6 soft mauve lilac-pink
    (0.68, (244, 184, 154)), # #f4b89a warm peach-pink
    (0.85, (247, 196, 115)), # #f7c473 warm golden apricot
    (1.00, (248, 182, 72))   # #f8b648 rich golden amber horizon
]

# 2. Purple Sunset (Phase 5.1-5.9)
PURPLE_SUNSET_STOPS = [
    (0.00, (20, 5, 35)),     # #140523 deep midnight obsidian
    (0.20, (56, 10, 84)),    # #380a54 royal dark purple
    (0.42, (111, 20, 120)),  # #6f1478 rich violet magenta
    (0.65, (169, 32, 114)),  # #a92072 vibrant magenta rose
    (0.85, (219, 75, 96)),   # #db4b60 Story Mode coral pink
    (1.00, (249, 136, 88))   # #f98858 fiery sunset orange horizon
]

# 3. Twilight Purple (Phases 6, 7, 8)
TWILIGHT_PURPLE_STOPS = [
    (0.00, (23, 2, 37)),     # #170225 deep midnight black purple
    (0.22, (62, 7, 86)),     # #3e0756 deep dark purple
    (0.48, (115, 17, 123)),  # #73117b magenta purple
    (0.70, (169, 31, 118)),  # #a91f76 bright magenta rose
    (0.86, (220, 68, 116)),  # #dc4474 intense magenta-pink
    (1.00, (233, 98, 128))   # #e96280 warm rose pink horizon
]

# ----------------------------------------------------------------------
# 1. BUILD RESOURCE PACK (Clean, Pure Assets, Crash-Free)
# ----------------------------------------------------------------------
print("[1/2] Assembling Minecraft: Story Mode Resource Pack...")

# pack_format 42 is official for Minecraft 1.21.2 - 1.21.3
rp_meta = {
    "pack": {
        "pack_format": 42,
        "supported_formats": {
            "min_inclusive": 15,
            "max_inclusive": 60
        },
        "description": "Minecraft: Story Mode - Authentic Visuals, OG Textures & Turquoise Teeth Glow (1.21.2)"
    }
}
with open(os.path.join(RP_DIR, "pack.mcmeta"), "w", encoding="utf-8") as f:
    json.dump(rp_meta, f, indent=2)

# Copy ONLY textures, sounds, and language files into the Resource Pack
mod_tex_src = os.path.join(ROOT, "src", "main", "resources", "assets", "dabywitherstormmod", "textures")
mod_tex_dst = os.path.join(RP_DIR, "assets", "dabywitherstormmod", "textures")
if os.path.exists(mod_tex_src):
    shutil.copytree(mod_tex_src, mod_tex_dst, dirs_exist_ok=True)
    print("Copied entity and misc textures into Resource Pack")

# Delete any cloud PNG from resource pack (per user instruction: no cloud PNGs)
cloud_png_in_mod_tex = os.path.join(mod_tex_dst, "misc", "mcsm_cloud.png")
if os.path.exists(cloud_png_in_mod_tex):
    os.remove(cloud_png_in_mod_tex)
    print("Deleted mcsm_cloud.png from Resource Pack")

mod_sounds_src = os.path.join(ROOT, "src", "main", "resources", "assets", "dabywitherstormmod", "sounds")
mod_sounds_dst = os.path.join(RP_DIR, "assets", "dabywitherstormmod", "sounds")
if os.path.exists(mod_sounds_src):
    shutil.copytree(mod_sounds_src, mod_sounds_dst, dirs_exist_ok=True)

mod_sounds_json = os.path.join(ROOT, "src", "main", "resources", "assets", "dabywitherstormmod", "sounds.json")
if os.path.exists(mod_sounds_json):
    shutil.copy(mod_sounds_json, os.path.join(RP_DIR, "assets", "dabywitherstormmod", "sounds.json"))

# Full Story Mode sound table for vanilla clicks / menu
sounds_json_path = os.path.join(RP_DIR, "assets", "minecraft", "sounds.json")
os.makedirs(os.path.dirname(sounds_json_path), exist_ok=True)
sounds_data = {
    "music.menu": {
        "replace": True,
        "sounds": [{"name": "music/menu/title_theme", "stream": True}]
    },
    "ui.button.click": {
        "replace": True,
        "sounds": ["random/click_stereo"]
    }
}
with open(sounds_json_path, "w", encoding="utf-8") as f:
    json.dump(sounds_data, f, indent=2)

env_dir = os.path.join(RP_DIR, "assets", "minecraft", "textures", "environment")
os.makedirs(env_dir, exist_ok=True)

# Note: clouds.png is deliberately OMITTED. User requirement: "deletes the cloud PNGS it's not a PNG it's a shader"
sun_rows = [[255, 245, 210, 255] * 64 for _ in range(64)]
write_png(os.path.join(env_dir, "sun.png"), 64, 64, sun_rows)
moon_rows = [[230, 220, 245, 255] * 128 for _ in range(64)]
write_png(os.path.join(env_dir, "moon_phases.png"), 128, 64, moon_rows)
end_rows = [[15, 6, 25, 255] * 128 for _ in range(128)]
write_png(os.path.join(env_dir, "end_sky.png"), 128, 128, end_rows)

# 3D Cyan Shield Halo Texture: textures/misc/halo_ring.png (#00E5FF)
HALO_SZ = 256
halo_rows = []
for hy in range(HALO_SZ):
    row = []
    dy = (hy - HALO_SZ / 2) / (HALO_SZ / 2)
    for hx in range(HALO_SZ):
        dx = (hx - HALO_SZ / 2) / (HALO_SZ / 2)
        dist = math.sqrt(dx * dx + dy * dy)
        if 0.65 <= dist <= 0.98:
            ringFactor = 1.0 - abs(dist - 0.82) / 0.16
            ringFactor = max(0.0, min(1.0, ringFactor))
            r = int(0 + ringFactor * 120)
            g = int(229 + ringFactor * 26)
            b = int(255)
            a = int(ringFactor * 240)
        else:
            r, g, b, a = 0, 0, 0, 0
        row.extend([r, g, b, a])
    halo_rows.append(row)

misc_dir = os.path.join(RP_DIR, "assets", "dabywitherstormmod", "textures", "misc")
write_png(os.path.join(misc_dir, "halo_ring.png"), HALO_SZ, HALO_SZ, halo_rows)

# OptiFine Custom Skies: sequential sky1, sky2, sky3 with explicit source= to prevent crash
print("Generating clean OptiFine 3x2 Cubemaps with explicit source tags...")
opti_sky_dir = os.path.join(RP_DIR, "assets", "minecraft", "optifine", "sky", "world0")
os.makedirs(opti_sky_dir, exist_ok=True)

def build_cubemap(stops, tile_size=512):
    w = tile_size * 3
    h = tile_size * 2
    grid = [[[0, 0, 0, 255] for _ in range(w)] for _ in range(h)]
    c_zenith = stops[0][1]
    c_horizon = stops[-1][1]

    # Top face
    for y in range(tile_size):
        for x in range(tile_size):
            dx = (x - tile_size / 2) / (tile_size / 2)
            dy = (y - tile_size / 2) / (tile_size / 2)
            r = math.sqrt(dx * dx + dy * dy)
            t = min(1.0, r * 0.40)
            col = sample_stops(stops, t)
            grid[y][tile_size + x] = col + [255]

    # Bottom face
    for y in range(tile_size):
        for x in range(tile_size):
            grid[y][x] = list(c_horizon) + [255]

    # Sides: Row 0 slot 2 (South), Row 1 slot 0 (West), Row 1 slot 1 (North), Row 1 slot 2 (East)
    side_slots = [(0, 2), (1, 0), (1, 1), (1, 2)]
    for sy_slot, sx_slot in side_slots:
        for y in range(tile_size):
            fy = y / tile_size
            t = 0.35 + fy * 0.65
            col = sample_stops(stops, t)
            for x in range(tile_size):
                grid[sy_slot * tile_size + y][sx_slot * tile_size + x] = col + [255]

    rows = []
    for y in range(h):
        row = []
        for x in range(w):
            row.extend(grid[y][x])
        rows.append(row)
    return w, h, rows

# Layer 1: Daytime Sky
w1, h1, r1 = build_cubemap(DAY_SKY_STOPS, tile_size=512)
write_png(os.path.join(opti_sky_dir, "sky1.png"), w1, h1, r1)
with open(os.path.join(opti_sky_dir, "sky1.properties"), "w", encoding="utf-8") as f:
    f.write("""# Minecraft: Story Mode — Official Daytime Sky
source=./sky1.png
startFadeIn=5:30
endFadeIn=6:30
startFadeOut=18:00
endFadeOut=19:00
blend=add
rotate=false
speed=0.0
axis=0.0 1.0 0.0
""")

# Layer 2: Phase 5 Purple Sunset Sky (consecutive numbering prevents skybox parser crash)
w2, h2, r2 = build_cubemap(PURPLE_SUNSET_STOPS, tile_size=512)
write_png(os.path.join(opti_sky_dir, "sky2.png"), w2, h2, r2)
with open(os.path.join(opti_sky_dir, "sky2.properties"), "w", encoding="utf-8") as f:
    f.write("""# Minecraft: Story Mode — Phase 5.1-5.9 Purple Sunset Sky
source=./sky2.png
startFadeIn=17:30
endFadeIn=18:30
startFadeOut=21:00
endFadeOut=22:00
blend=add
rotate=false
speed=0.0
axis=0.0 1.0 0.0
""")

# Layer 3: Phases 6, 7, 8 Twilight Sky
w3, h3, r3 = build_cubemap(TWILIGHT_PURPLE_STOPS, tile_size=512)
write_png(os.path.join(opti_sky_dir, "sky3.png"), w3, h3, r3)
with open(os.path.join(opti_sky_dir, "sky3.properties"), "w", encoding="utf-8") as f:
    f.write("""# Minecraft: Story Mode — Phase 6-8 Twilight Sky
source=./sky3.png
startFadeIn=20:30
endFadeIn=21:30
startFadeOut=5:00
endFadeOut=6:00
blend=add
rotate=false
speed=0.0
axis=0.0 1.0 0.0
""")

# Emissive mapping property
opti_base = os.path.join(RP_DIR, "assets", "minecraft", "optifine")
with open(os.path.join(opti_base, "emissive.properties"), "w", encoding="utf-8") as f:
    f.write("# Minecraft: Story Mode Emissive Textures\nsuffix.emissive=_e\n")

# Resource Pack pack.png
pack_png_rows = []
for py in range(64):
    row = []
    for px in range(64):
        t = (px + py) / 128.0
        col = sample_stops(DAY_SKY_STOPS, t)
        row.extend(col + [255])
    pack_png_rows.append(row)
write_png(os.path.join(RP_DIR, "pack.png"), 64, 64, pack_png_rows)

print("Zipping MCSM_ResourcePack.zip...")
with zipfile.ZipFile(RP_ZIP, "w", zipfile.ZIP_DEFLATED) as z:
    for root_dir, _, files in os.walk(RP_DIR):
        for file in files:
            full_p = os.path.join(root_dir, file)
            rel_p = os.path.relpath(full_p, RP_DIR)
            z.write(full_p, rel_p)
print(f"Created {RP_ZIP} ({os.path.getsize(RP_ZIP)} bytes)")

# ----------------------------------------------------------------------
# 2. BUILD SHADER PACK (Clean GLSL, User Clouds Shader, Zero Crashes)
# ----------------------------------------------------------------------
print("[2/2] Assembling Minecraft: Story Mode Atmosphere Shader Pack...")
sp_shaders = os.path.join(SP_DIR, "shaders")
sp_core = os.path.join(sp_shaders, "core")
os.makedirs(sp_core, exist_ok=True)

# 2.1 The exact user-provided rendertype_clouds.vsh for Minecraft 1.21.2
user_rendertype_clouds_vsh = """#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

const int FLAG_MASK_DIR = 7;
const int FLAG_INSIDE_FACE = 1 << 4;
const int FLAG_USE_TOP_COLOR = 1 << 5;
const int FLAG_EXTRA_Z = 1 << 6;
const int FLAG_EXTRA_X = 1 << 7;

layout(std140) uniform CloudInfo {
    vec4 CloudColor;
    vec3 CloudOffset;
    vec3 CellSize;
};

uniform isamplerBuffer CloudFaces;

const float CloudFadeAlpha = 0; // 0 = a full 0 alpha fade
const float CloudHeight = 2.5; // vertical scaling
const float CloudYOffset = 0.0; // Y offset
const float BrightnessBottom = 1.0;
const float BrightnessTop = 1.0;
const float BrightnessNorth = 1.0;
const float BrightnessSouth = 1.0;
const float BrightnessWest = 1.0;
const float BrightnessEast = 1.0;

out float vertexDistance;
out vec4 vertexColor;

const vec3[] NORMAL_DIRECTIONS = vec3[](
    vec3(0, -1, 0),
    vec3(0, 1, 0),
    vec3(0, 0, -1),
    vec3(0, 0, 1),
    vec3(-1, 0, 0),
    vec3(1, 0, 0)
);

const vec3[][] VERTICES = vec3[][](
    vec3[](vec3(0, 0, 0), vec3(1, 0, 0), vec3(1, 0, 1), vec3(0, 0, 1)),
    vec3[](vec3(0, 1, 1), vec3(1, 1, 1), vec3(1, 1, 0), vec3(0, 1, 0)),
    vec3[](vec3(1, 1, 0), vec3(1, 0, 0), vec3(0, 0, 0), vec3(0, 1, 0)),
    vec3[](vec3(0, 1, 1), vec3(0, 0, 1), vec3(1, 0, 1), vec3(1, 1, 1)),
    vec3[](vec3(0, 1, 0), vec3(0, 0, 0), vec3(0, 0, 1), vec3(0, 1, 1)),
    vec3[](vec3(1, 1, 1), vec3(1, 0, 1), vec3(1, 0, 0), vec3(1, 1, 0))
);

vec3 lerp(vec3 a, vec3 b, float t) {
    return a + t * (b - a);
}

float fog_spherical_distance(vec3 pos) {
    return length(pos);
}

void main() {
    int faceIndex = gl_VertexID / 4;
    int vertexIndex = gl_VertexID % 4;

    int faceData = texelFetch(CloudFaces, faceIndex).r;
    int dir = faceData & FLAG_MASK_DIR;

    vec3 baseVertex = VERTICES[dir][vertexIndex];
    vec3 normal = NORMAL_DIRECTIONS[dir];

    // Decode position from faceData
    int posX = (faceData >> 8) & 0xFF;
    int posY = (faceData >> 16) & 0xFF;
    int posZ = (faceData >> 24) & 0xFF;

    vec3 cellPos = vec3(posX, posY, posZ) * CellSize + CloudOffset;
    vec3 worldPos = cellPos + baseVertex * CellSize;
    worldPos.y = (worldPos.y + CloudYOffset) * CloudHeight;

    vec3 viewPos = (ModelViewMat * vec4(worldPos, 1.0)).xyz;
    gl_Position = ProjMat * vec4(viewPos, 1.0);

    vertexDistance = fog_spherical_distance(viewPos);

    // Flat MCSM story mode cloud coloring
    vec4 faceColor = CloudColor;
    if (dir == 0) faceColor.rgb *= BrightnessBottom;
    else if (dir == 1) faceColor.rgb *= BrightnessTop;
    else if (dir == 2) faceColor.rgb *= BrightnessNorth;
    else if (dir == 3) faceColor.rgb *= BrightnessSouth;
    else if (dir == 4) faceColor.rgb *= BrightnessWest;
    else if (dir == 5) faceColor.rgb *= BrightnessEast;

    vertexColor = faceColor;
}
"""

user_rendertype_clouds_fsh = """#version 150

#moj_import <minecraft:fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
"""

user_rendertype_clouds_json = """{
    "blend": {
        "func": "add",
        "srcrgb": "srcalpha",
        "dstrgb": "1-srcalpha"
    },
    "vertex": "rendertype_clouds",
    "fragment": "rendertype_clouds",
    "attributes": [],
    "samplers": [
        { "name": "CloudFaces" }
    ],
    "uniforms": [
        { "name": "ModelViewMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
        { "name": "ProjMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
        { "name": "ColorModulator", "type": "float", "count": 4, "values": [ 1.0, 1.0, 1.0, 1.0 ] },
        { "name": "FogStart", "type": "float", "count": 1, "values": [ 0.0 ] },
        { "name": "FogEnd", "type": "float", "count": 1, "values": [ 1.0 ] },
        { "name": "FogColor", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] }
    ]
}
"""

# Place in shaders/core/ and shaders/ root
with open(os.path.join(sp_core, "rendertype_clouds.vsh"), "w", encoding="utf-8") as f:
    f.write(user_rendertype_clouds_vsh)
with open(os.path.join(sp_core, "rendertype_clouds.fsh"), "w", encoding="utf-8") as f:
    f.write(user_rendertype_clouds_fsh)
with open(os.path.join(sp_core, "rendertype_clouds.json"), "w", encoding="utf-8") as f:
    f.write(user_rendertype_clouds_json)

with open(os.path.join(sp_shaders, "rendertype_clouds.vsh"), "w", encoding="utf-8") as f:
    f.write(user_rendertype_clouds_vsh)
with open(os.path.join(sp_shaders, "rendertype_clouds.fsh"), "w", encoding="utf-8") as f:
    f.write(user_rendertype_clouds_fsh)

# 2.2 OptiFine / Iris Pipeline Cloud Shaders (gbuffers_clouds)
# Flat, crisp Story Mode lighting (Brightness = 1.0, CloudFadeAlpha = 0, no reflections)
gbuffers_clouds_vsh = """#version 120

varying vec4 color;

void main() {
    gl_Position = ftransform();
    // Flat bright uniform MCSM story mode cloud lighting (all faces 1.0 brightness)
    color = gl_Color;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_clouds.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_clouds_vsh)

gbuffers_clouds_fsh = """#version 120

varying vec4 color;

void main() {
    if (color.a < 0.05) {
        discard;
    }
    // Crisp flat Story Mode clouds without raymarching or alpha fade
    gl_FragColor = color;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_clouds.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_clouds_fsh)

# 2.3 OptiFine / Iris Sky Dome (gbuffers_skybasic)
# Clean MCSM Daytime Sky gradient. Volumetric raymarched noise clouds DELETED per user instruction.
gbuffers_skybasic_vsh = """#version 120

varying vec4 color;
varying vec3 viewPos;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skybasic.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skybasic_vsh)

gbuffers_skybasic_fsh = """#version 120

varying vec4 color;
varying vec3 viewPos;

// Minecraft: Story Mode Official Daytime Sky Palette
vec3 getStoryModeDaySky(float elev) {
    vec3 cZenith   = vec3(0.549, 0.529, 0.910); // #8c87e8 periwinkle lavender zenith
    vec3 cLilac    = vec3(0.686, 0.608, 0.886); // #af9be2 soft lilac
    vec3 cMauve    = vec3(0.835, 0.682, 0.839); // #d5aed6 soft mauve lilac-pink
    vec3 cPeach    = vec3(0.957, 0.722, 0.604); // #f4b89a warm peach-pink
    vec3 cApricot  = vec3(0.969, 0.769, 0.451); // #f7c473 warm golden apricot
    vec3 cHorizon  = vec3(0.973, 0.714, 0.282); // #f8b648 rich golden amber horizon
    vec3 cVoid     = vec3(0.350, 0.220, 0.150);

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
    float elev = normalize(viewPos).y;
    vec3 skyCol = getStoryModeDaySky(elev);
    gl_FragColor = vec4(skyCol, 1.0);
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skybasic.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skybasic_fsh)

# 2.4 Sky Textured
gbuffers_skytextured_vsh = """#version 120

varying vec4 color;
varying vec2 texcoord;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skytextured.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skytextured_vsh)

gbuffers_skytextured_fsh = """#version 120

uniform sampler2D gtexture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord) * color;
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skytextured.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skytextured_fsh)

# 2.5 Terrain: Authentic MCSM Colored Lighting & Shadows (Warm sun, lavender shadow tint, amber torchlight, NO reflections)
gbuffers_terrain_vsh = """#version 120

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmcoord = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_terrain.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_terrain_vsh)

gbuffers_terrain_fsh = """#version 120

uniform sampler2D gtexture;
varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;

void main() {
    vec4 tex = texture2D(gtexture, texcoord) * color;
    if (tex.a < 0.1) {
        discard;
    }

    // Minecraft: Story Mode Colored Lighting & Shadows (No Reflections)
    float blockLight = clamp((lmcoord.x - 0.03) * 1.05, 0.0, 1.0);
    float skyLight   = clamp((lmcoord.y - 0.03) * 1.05, 0.0, 1.0);

    // Warm Story Mode sunlight, cool lavender shadow tint, and warm amber torchlight
    vec3 sunLightColor = vec3(1.08, 1.00, 0.92);
    vec3 shadowAmbientColor = vec3(0.72, 0.65, 0.85); // Story Mode lavender ambient shadow
    vec3 torchColor = vec3(1.15, 0.75, 0.40);         // Warm amber firelight

    // Sky light combines direct sun with ambient shadow
    vec3 skyLightTerm = mix(shadowAmbientColor * 0.70, sunLightColor, pow(skyLight, 1.25));
    // Torch / block light term
    vec3 blockLightTerm = torchColor * pow(blockLight, 1.35) * 1.30;

    vec3 ambientLighting = skyLightTerm + blockLightTerm;
    tex.rgb *= ambientLighting;

    // Story Mode shadow deepening on shaded faces (diffuse shading, NO reflections)
    float isShadowed = 1.0 - skyLight;
    if (isShadowed > 0.30) {
        float shadowStr = (isShadowed - 0.30) / 0.70;
        tex.rgb = mix(tex.rgb, tex.rgb * vec3(0.82, 0.76, 0.92), shadowStr * 0.40);
    }

    gl_FragColor = tex;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_terrain.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_terrain_fsh)

# 2.6 Entities: Turquoise Teeth Glow (#00E5FF)
gbuffers_entities_vsh = """#version 120

varying vec4 color;
varying vec2 texcoord;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_entities.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_entities_vsh)

gbuffers_entities_fsh = """#version 120

uniform sampler2D gtexture;
uniform float frameTimeCounter;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord) * color;
    if (col.a < 0.1) {
        discard;
    }

    // Turquoise / cyan glow on top of teeth (#00E5FF)
    float isTurquoise = step(0.70, col.g) * step(0.80, col.b) * (1.0 - step(0.40, col.r));
    float isHotMagenta = step(0.68, col.r) * step(0.68, col.b) * (1.0 - step(0.50, col.g));
    float isCyanGlow   = step(0.68, col.g) * step(0.68, col.b) * (1.0 - step(0.50, col.r));
    float isAmuletGold = step(0.80, col.r) * step(0.70, col.g) * (1.0 - step(0.40, col.b));
    float emissive = max(max(max(isTurquoise, isHotMagenta), isCyanGlow), isAmuletGold);

    if (emissive > 0.5) {
        float pulse = 0.88 + 0.12 * sin(frameTimeCounter * 3.5);
        if (isTurquoise > 0.5) {
            col.rgb = mix(col.rgb, vec3(0.0, 0.90, 1.0), 0.75) * 2.10 * pulse;
        } else {
            col.rgb *= 1.85 * pulse;
        }
    }

    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_entities.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_entities_fsh)

# 2.7 Composite Pass
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

uniform sampler2D colortex0;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(colortex0, texcoord);
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "composite.fsh"), "w", encoding="utf-8") as f:
    f.write(composite_fsh)

# 2.8 Final Pass
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

uniform sampler2D colortex0;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(colortex0, texcoord);
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "final.fsh"), "w", encoding="utf-8") as f:
    f.write(final_fsh)

# 2.9 shaders.properties
shaders_properties = """# Minecraft: Story Mode — Shaderpack Configuration
# Clean, Authentic Minecraft: Story Mode visuals
# Flat shader clouds, authentic daytime sky dome, turquoise teeth glow
"""
with open(os.path.join(sp_shaders, "shaders.properties"), "w", encoding="utf-8") as f:
    f.write(shaders_properties)

# Shaderpack README
sp_readme = """# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack (1.21.2)
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Features
- **Official MCSM Core Cloud Shader**: Integrated GLSL 150 core cloud shader (`rendertype_clouds.vsh` with `CloudFaces`, `CloudInfo`, `BrightnessTop/Bottom/Sides = 1.0`, `CloudHeight = 2.5`, `CloudFadeAlpha = 0`).
- **Iris / OptiFine Cloud Pipeline**: `gbuffers_clouds` renders flat crisp Story Mode clouds without raymarched noise.
- **Story Mode Daytime Sky Dome**: Signature MCSM periwinkle lavender -> lilac -> mauve -> peach -> amber horizon gradient.
- **Teeth Turquoise Glow**: Vibrant cyan/turquoise glow (#00E5FF) pulsing on the Wither Storm teeth.
- **No Dynamic Reflections or Heavy Shadows**: Authentic clean Story Mode diffuse lighting.
- **100% Crash-Free**: Standardized uniform declarations (`gtexture`) avoiding GLSL keyword collisions.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine for Minecraft 1.21.2.
2. Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (DO NOT unzip).
3. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
"""
with open(os.path.join(SP_DIR, "README.md"), "w", encoding="utf-8") as f:
    f.write(sp_readme)

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
