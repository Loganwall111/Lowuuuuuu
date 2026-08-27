#!/usr/bin/env python3
"""
MCSM Pack Builder — Standalone Resource Pack and Shader Pack for Minecraft: Story Mode
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
# Palettes
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
# 1. BUILD RESOURCE PACK (Clean & Crash-Free)
# ----------------------------------------------------------------------
print("[1/2] Assembling Minecraft: Story Mode Resource Pack...")

rp_meta = {
    "pack": {
        "pack_format": 46,
        "supported_formats": {
            "min_inclusive": 15,
            "max_inclusive": 60
        },
        "description": "Minecraft: Story Mode - Authentic Visuals, OG Textures & Turquoise Teeth Glow"
    }
}
with open(os.path.join(RP_DIR, "pack.mcmeta"), "w", encoding="utf-8") as f:
    json.dump(rp_meta, f, indent=2)

# Copy ONLY textures, sounds, and language files into the Resource Pack
# (NEVER copy shaders, post_effects, or blockstates into a resource pack)
mod_tex_src = os.path.join(ROOT, "src", "main", "resources", "assets", "dabywitherstormmod", "textures")
mod_tex_dst = os.path.join(RP_DIR, "assets", "dabywitherstormmod", "textures")
if os.path.exists(mod_tex_src):
    shutil.copytree(mod_tex_src, mod_tex_dst, dirs_exist_ok=True)
    print("Copied entity and misc textures into Resource Pack")

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

# Clouds texture: assets/minecraft/textures/environment/clouds.png
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
        c1 = cloud_fbm(nx * 8.0, ny * 8.0)
        c2 = cloud_fbm((nx + 0.35) * 16.0, (ny + 0.35) * 16.0)
        density = (c1 + 0.4 * c2) * 0.75 + 0.5

        if density > 0.46:
            t = min(1.0, (density - 0.46) / 0.54)
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

# Clean OptiFine Custom Skies: blend=alpha
print("Generating clean OptiFine 3x2 Cubemaps with alpha blend...")
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

    # Sides
    side_slots = [(0, 2), (1, 0), (1, 1), (1, 2)]
    for r_slot, c_slot in side_slots:
        for y in range(tile_size):
            t = y / float(tile_size)
            col = sample_stops(stops, t)
            for x in range(tile_size):
                grid[r_slot * tile_size + y][c_slot * tile_size + x] = col + [255]

    flat_rows = []
    for y in range(h):
        r = []
        for x in range(w):
            r.extend(grid[y][x])
        flat_rows.append(r)
    return flat_rows

# sky1.png: Daytime Sky
write_png(os.path.join(opti_sky_dir, "sky1.png"), 1536, 1024, build_cubemap(DAY_SKY_STOPS))
sky1_properties = """# Minecraft: Story Mode — Official Daytime Sky (day_sky.png)
startFadeIn=5:30
endFadeIn=6:30
startFadeOut=18:00
endFadeOut=19:00
blend=alpha
rotate=false
speed=0.0
axis=0.0 1.0 0.0
"""
with open(os.path.join(opti_sky_dir, "sky1.properties"), "w", encoding="utf-8") as f:
    f.write(sky1_properties)

# sky3.png: Phase 5.1-5.9 Sunset Sky
write_png(os.path.join(opti_sky_dir, "sky3.png"), 1536, 1024, build_cubemap(PURPLE_SUNSET_STOPS))
sky3_properties = """# Minecraft: Story Mode — Phase 5.1-5.9 Purple Sunset Sky
startFadeIn=17:30
endFadeIn=18:30
startFadeOut=21:00
endFadeOut=22:00
blend=alpha
rotate=false
speed=0.0
axis=0.0 1.0 0.0
"""
with open(os.path.join(opti_sky_dir, "sky3.properties"), "w", encoding="utf-8") as f:
    f.write(sky3_properties)

# sky4.png: Phase 6, 7, 8 Twilight Sky
write_png(os.path.join(opti_sky_dir, "sky4.png"), 1536, 1024, build_cubemap(TWILIGHT_PURPLE_STOPS))
sky4_properties = """# Minecraft: Story Mode — Phase 6, 7, 8 Twilight Sky
startFadeIn=20:30
endFadeIn=21:30
startFadeOut=5:00
endFadeOut=6:00
blend=alpha
rotate=false
speed=0.0
axis=0.0 1.0 0.0
"""
with open(os.path.join(opti_sky_dir, "sky4.properties"), "w", encoding="utf-8") as f:
    f.write(sky4_properties)

# Emissive mapping properties for OptiFine / Iris
emissive_props = """# Minecraft: Story Mode Emissive Textures
suffix.emissive=_e
"""
opti_dir = os.path.join(RP_DIR, "assets", "minecraft", "optifine")
with open(os.path.join(opti_dir, "emissive.properties"), "w", encoding="utf-8") as f:
    f.write(emissive_props)

icon_rows = [[140, 135, 232, 255] * 64 for _ in range(64)]
write_png(os.path.join(RP_DIR, "pack.png"), 64, 64, icon_rows)

print("Zipping MCSM_ResourcePack.zip...")
with zipfile.ZipFile(RP_ZIP, "w", zipfile.ZIP_DEFLATED) as z:
    for root_dir, _, files in os.walk(RP_DIR):
        for file in files:
            full_p = os.path.join(root_dir, file)
            rel_p = os.path.relpath(full_p, RP_DIR)
            z.write(full_p, rel_p)
print(f"Created {RP_ZIP} ({os.path.getsize(RP_ZIP)} bytes)")

# ----------------------------------------------------------------------
# 2. BUILD SHADER PACK (100% Compliant GLSL 120 / OptiFine / Iris)
# ----------------------------------------------------------------------
print("\n[2/2] Assembling Minecraft: Story Mode Shader Pack...")
sp_shaders = os.path.join(SP_DIR, "shaders")
os.makedirs(sp_shaders, exist_ok=True)

shaders_properties = """# Minecraft: Story Mode — Atmosphere Shader Configuration
profile.MCSM_DEFAULT=MCSM_DAY_SKY:1,MCSM_ROILING_CLOUDS:1,MCSM_COLOURED_LIGHTING:1,MCSM_SHADOW_TINT:1,MCSM_ATMOSPHERIC_FOG:1,MCSM_CINEMATIC_GRADE:1
profile.MCSM_PERFORMANCE=MCSM_DAY_SKY:1,MCSM_ROILING_CLOUDS:0,MCSM_COLOURED_LIGHTING:1,MCSM_SHADOW_TINT:1,MCSM_ATMOSPHERIC_FOG:1,MCSM_CINEMATIC_GRADE:0

option.MCSM_DAY_SKY=true
option.MCSM_ROILING_CLOUDS=true
option.MCSM_COLOURED_LIGHTING=true
option.MCSM_SHADOW_TINT=true
option.MCSM_ATMOSPHERIC_FOG=true
option.MCSM_CINEMATIC_GRADE=true
"""
with open(os.path.join(sp_shaders, "shaders.properties"), "w", encoding="utf-8") as f:
    f.write(shaders_properties)

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

gbuffers_skybasic_fsh = """#version 120

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

vec3 getStoryModeDaySky(float elev) {
    vec3 cZenith   = vec3(0.549, 0.529, 0.910); // #8c87e8 soft periwinkle lavender
    vec3 cLilac    = vec3(0.686, 0.608, 0.886); // #af9be2 soft lilac
    vec3 cMauve    = vec3(0.835, 0.682, 0.839); // #d5aed6 pastel mauve-pink
    vec3 cPeach    = vec3(0.957, 0.722, 0.604); // #f4b89a warm peach
    vec3 cApricot  = vec3(0.969, 0.769, 0.451); // #f7c473 golden apricot
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
    vec3 dirV = normalize(viewPos);
    vec3 dir = normalize(mat3(gbufferModelViewInverse) * dirV);
    float elev = dir.y;

    #if MCSM_DAY_SKY
    vec3 skyCol = getStoryModeDaySky(elev);

    float horizBand = exp(-pow(max(elev, 0.0) * 10.0, 2.0));
    skyCol += vec3(0.98, 0.76, 0.45) * horizBand * 0.22;

    #if MCSM_ROILING_CLOUDS
    if (elev > 0.04) {
        vec2 cp = dir.xz / max(elev + 0.18, 0.08);
        float time = frameTimeCounter * 0.018;

        float c = cloudFbm(cp * 0.85 + vec2(time * 0.4, time * 0.15));
        float detail = cloudFbm(cp * 1.8 - vec2(time * 0.6, time * 0.2));
        float density = c + detail * 0.35;

        float cloudMask = smoothstep(0.48, 0.76, density) * clamp(elev * 3.0, 0.0, 1.0);

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

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;
    col.rgb *= vec3(1.10, 1.02, 0.94);
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skytextured.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skytextured_fsh)

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

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;

#ifndef MCSM_COLOURED_LIGHTING
#define MCSM_COLOURED_LIGHTING 1
#endif
#ifndef MCSM_SHADOW_TINT
#define MCSM_SHADOW_TINT 1
#endif

void main() {
    vec4 tex = texture2D(texture, texcoord) * color;
    if (tex.a < 0.1) {
        discard;
    }

    #if MCSM_COLOURED_LIGHTING
    float blockLight = clamp((lmcoord.x - 0.03) * 1.05, 0.0, 1.0);
    float skyLight   = clamp((lmcoord.y - 0.03) * 1.05, 0.0, 1.0);

    vec3 sunLightColor = vec3(1.08, 1.00, 0.92);
    vec3 shadowAmbientColor = vec3(0.68, 0.58, 0.82);
    vec3 torchColor = vec3(1.15, 0.74, 0.40);

    vec3 skyLightTerm = mix(shadowAmbientColor * 0.75, sunLightColor, pow(skyLight, 1.3));
    vec3 blockLightTerm = torchColor * pow(blockLight, 1.4) * 1.35;

    vec3 ambient = skyLightTerm + blockLightTerm;
    tex.rgb *= ambient;

    #if MCSM_SHADOW_TINT
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

uniform sampler2D texture;
uniform float frameTimeCounter;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;
    if (col.a < 0.1) {
        discard;
    }

    // Vibrant Turquoise Teeth Glow (#00E5FF)
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
uniform sampler2D depthtex0;
uniform float near;
uniform float far;
uniform vec3 fogColor;
uniform float rainStrength;

varying vec2 texcoord;

#ifndef MCSM_ATMOSPHERIC_FOG
#define MCSM_ATMOSPHERIC_FOG 1
#endif

float linearizeDepth(float z) {
    float n = max(near, 0.1);
    float f = max(far, 16.0);
    return (2.0 * n) / (f + n - z * (f - n));
}

void main() {
    vec3 col = texture2D(colortex0, texcoord).rgb;
    float depth = texture2D(depthtex0, texcoord).x;

    #if MCSM_ATMOSPHERIC_FOG
    if (depth < 0.9999) {
        float dist = linearizeDepth(depth);
        float fogFactor = 1.0 - exp(-dist * 0.005 * (1.0 + rainStrength * 0.8));
        fogFactor = clamp(fogFactor, 0.0, 0.82);

        vec3 storyModeFog = vec3(0.95, 0.74, 0.62) * (0.85 + 0.15 * fogColor);
        col = mix(col, storyModeFog, fogFactor);
    }
    #endif

    gl_FragColor = vec4(col, 1.0);
}
"""
with open(os.path.join(sp_shaders, "composite.fsh"), "w", encoding="utf-8") as f:
    f.write(composite_fsh)

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

#ifndef MCSM_CINEMATIC_GRADE
#define MCSM_CINEMATIC_GRADE 1
#endif

void main() {
    vec2 uv = texcoord;
    vec3 col = texture2D(colortex0, uv).rgb;

    #if MCSM_CINEMATIC_GRADE
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(lum), col, 1.18);
    col = pow(col, vec3(0.96, 0.95, 0.98));
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
- **Procedural Shader Clouds**: Billowing storm cumulus clouds underlit by warm amber/peach horizon light (handled entirely by shader, no solid objects/textures).
- **Coloured Lighting & Ground Shadows**: Warm golden sun illumination, cool atmospheric lavender/purple bounce tint in shadows, and rich firelight block lighting.
- **Teeth Turquoise Glow**: Electric turquoise glow (#00E5FF) on the Wither Storm teeth.
- **Atmospheric Golden-Peach Distance Fog**: Smoothly blends distant terrain into the horizon.
- **Cinematic Story Mode Grading**: Vibrant Telltale color curve.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine.
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
