#!/usr/bin/env python3
"""
MCSM Pack Builder — Standalone Resource Pack and Shader Pack for Minecraft: Story Mode
Target: Minecraft 1.21.2 & 26.2 (Fabric / Iris / Sodium)
Builds crash-free, authentic Story Mode resource pack and shader pack.
"""

import os
import sys
import json
import shutil
import zipfile
import struct
import zlib
import math
import base64
import io
from PIL import Image

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
# 1. BUILD RESOURCE PACK (Pure Assets, Required Schema, Crash-Free)
# ----------------------------------------------------------------------
print("[1/2] Assembling Minecraft: Story Mode Resource Pack...")

# Exact required pack.mcmeta schema
rp_meta = {
    "pack": {
        "pack_format": 46,
        "supported_formats": {
            "min_format": 42,
            "max_format": 50
        },
        "description": "Minecraft: Story Mode Authentic Visuals"
    }
}
with open(os.path.join(RP_DIR, "pack.mcmeta"), "w", encoding="utf-8") as f:
    json.dump(rp_meta, f, indent=2)

# Copy entity and misc textures from mod resources into Resource Pack
mod_tex_src = os.path.join(ROOT, "src", "main", "resources", "assets", "dabywitherstormmod", "textures")
mod_tex_dst = os.path.join(RP_DIR, "assets", "dabywitherstormmod", "textures")
if os.path.exists(mod_tex_src):
    shutil.copytree(mod_tex_src, mod_tex_dst, dirs_exist_ok=True)
    print("Copied entity and misc textures into Resource Pack")

# Ensure mcsm_cloud.png is removed from mod textures (no conflicts)
cloud_png_in_mod_tex = os.path.join(mod_tex_dst, "misc", "mcsm_cloud.png")
if os.path.exists(cloud_png_in_mod_tex):
    os.remove(cloud_png_in_mod_tex)

# Copy sounds and sounds.json
mod_sounds_src = os.path.join(ROOT, "src", "main", "resources", "assets", "dabywitherstormmod", "sounds")
mod_sounds_dst = os.path.join(RP_DIR, "assets", "dabywitherstormmod", "sounds")
if os.path.exists(mod_sounds_src):
    shutil.copytree(mod_sounds_src, mod_sounds_dst, dirs_exist_ok=True)

mod_sounds_json = os.path.join(ROOT, "src", "main", "resources", "assets", "dabywitherstormmod", "sounds.json")
if os.path.exists(mod_sounds_json):
    shutil.copy(mod_sounds_json, os.path.join(RP_DIR, "assets", "dabywitherstormmod", "sounds.json"))

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

# Environment textures (sun, moon, clouds)
env_dir = os.path.join(RP_DIR, "assets", "minecraft", "textures", "environment")
os.makedirs(env_dir, exist_ok=True)

sun_rows = [[255, 245, 210, 255] * 64 for _ in range(64)]
write_png(os.path.join(env_dir, "sun.png"), 64, 64, sun_rows)
moon_rows = [[230, 220, 245, 255] * 128 for _ in range(64)]
write_png(os.path.join(env_dir, "moon_phases.png"), 128, 64, moon_rows)
end_rows = [[15, 6, 25, 255] * 128 for _ in range(128)]
write_png(os.path.join(env_dir, "end_sky.png"), 128, 128, end_rows)

# Story Mode Cloud texture in environment/clouds.png
c_rows = []
for y in range(64):
    c_row = []
    for x in range(64):
        bx = (x // 8) % 2
        by = (y // 8) % 2
        checker = 0.95 if (bx ^ by) else 1.05
        r = int(min(255, 250 * checker))
        g = int(min(255, 252 * checker))
        b = int(min(255, 255 * checker))
        a = 240
        c_row.extend([r, g, b, a])
    c_rows.append(c_row)
write_png(os.path.join(env_dir, "clouds.png"), 64, 64, c_rows)

# Boss halo ring texture
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

# OptiFine emissives properties
opti_base = os.path.join(RP_DIR, "assets", "minecraft", "optifine")
os.makedirs(opti_base, exist_ok=True)
with open(os.path.join(opti_base, "emissive.properties"), "w", encoding="utf-8") as f:
    f.write("# Minecraft: Story Mode Emissive Textures\nsuffix.emissive=_e\n")

# Story Mode Command Block & Grass Textures
block_tex_dir = os.path.join(RP_DIR, "assets", "minecraft", "textures", "block")
os.makedirs(block_tex_dir, exist_ok=True)

# Authentic Story Mode Command Block base64 texture
cb_b64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAjRJREFUOE91kk9oE0EUxr+1rdQGoRJIsl3ZgthAoVStUGmp3lQMVtioVCOegmKhVw8VQdRo79JrrSgRLxpQiNBDCbp4sdo/BGlToWSz68Y2IblElhpdebOZTVfowMC8efv93jf7njD36IYNAIausR25Gsd8KomAJLO9p/6b0jAMHYZhIHr5GtKpJEKSDFGSIRCAiylptbYzAYeEgmJTrFwEGnkOEWbHzzIAiWlxAIf4g5JTmcS0GgA6EkRIjPbaXKwX8vi713HA18InFYODJ9y4pbWNnUWpG6aRh5B5OGarKznETg9D+1lC4NI9D8AsbHhicX871tLP4e85gtz7Fw6AviDISH+YATZXv7miP759HkBbcQ3l9WWUv6/Af7i/CeCQ6IMkA2TfvkHfhSjUJ7cxNDmNXGoGYSUOAlBlEtNyHfwP4GV3c8DzwtzULWcOGn0eGk+wXC01A58Sh/X0GRC7Aty5CzxOQM+8hmYWIYshyF0iGICL6W+3HB/1vNmyLE9MABKqXxYZRJidOG/zPuuG7gKqUxPonJxuOnj5ijnhgO4uEcl3aQgJ5ZjNh2QngJfdzQEB8j9MCJn7Y7a6lEPs3DA0s4TAmZsorS+7tj98XvA8IVSvYKD3ELtLf/zqAFgHlnIYORpGpe5Motvn7SqLs7986OuoQZYOwtyqYHF1A5GTA00Ah5xSrnv6HMSW62C+2olIzwFWmcQEch3QV1qxjO1q2R0SutsJyNY6sFkwXDHl/wEvhyiSMeAO8wAAAABJRU5ErkJggg=="
cb_img = Image.open(io.BytesIO(base64.b64decode(cb_b64))).convert("RGBA")

# Save authentic command block variants
cb_img.save(os.path.join(block_tex_dir, "command_block_front.png"))
cb_img.save(os.path.join(block_tex_dir, "command_block_back.png"))
cb_img.save(os.path.join(block_tex_dir, "command_block_side.png"))
cb_img.save(os.path.join(block_tex_dir, "command_block_conditional.png"))

# Repeating command block (tinted purple)
cb_rep = cb_img.copy()
for px in range(16):
    for py in range(16):
        r, g, b, a = cb_rep.getpixel((px, py))
        if a > 0:
            cb_rep.putpixel((px, py), (int(r * 0.7), int(g * 0.4), int(min(255, b * 1.3)), a))
cb_rep.save(os.path.join(block_tex_dir, "repeating_command_block_front.png"))
cb_rep.save(os.path.join(block_tex_dir, "repeating_command_block_back.png"))
cb_rep.save(os.path.join(block_tex_dir, "repeating_command_block_side.png"))
cb_rep.save(os.path.join(block_tex_dir, "repeating_command_block_conditional.png"))

# Chain command block (tinted teal/cyan)
cb_chain = cb_img.copy()
for px in range(16):
    for py in range(16):
        r, g, b, a = cb_chain.getpixel((px, py))
        if a > 0:
            cb_chain.putpixel((px, py), (int(r * 0.3), int(min(255, g * 1.2)), int(min(255, b * 1.1)), a))
cb_chain.save(os.path.join(block_tex_dir, "chain_command_block_front.png"))
cb_chain.save(os.path.join(block_tex_dir, "chain_command_block_back.png"))
cb_chain.save(os.path.join(block_tex_dir, "chain_command_block_side.png"))
cb_chain.save(os.path.join(block_tex_dir, "chain_command_block_conditional.png"))

# Authentic Story Mode Grass Textures (Vibrant lush green & warm stylized dirt)
grass_top_rows = []
for y in range(16):
    row = []
    for x in range(16):
        noise = 0.92 if ((x + y) % 3 == 0) else (1.08 if ((x * y) % 5 == 0) else 1.0)
        r = int(min(255, 110 * noise))
        g = int(min(255, 192 * noise))
        b = int(min(255, 62 * noise))
        row.extend([r, g, b, 255])
    grass_top_rows.append(row)
write_png(os.path.join(block_tex_dir, "grass_block_top.png"), 16, 16, grass_top_rows)

grass_side_rows = []
for y in range(16):
    row = []
    for x in range(16):
        noise = 0.94 if ((x + y) % 4 == 0) else 1.04
        if y < 4:
            # Lush green grass rim hanging over side
            r = int(min(255, 105 * noise))
            g = int(min(255, 185 * noise))
            b = int(min(255, 58 * noise))
        elif y == 4 and ((x % 3) != 0):
            r = int(min(255, 98 * noise))
            g = int(min(255, 175 * noise))
            b = int(min(255, 52 * noise))
        else:
            # Warm stylized Story Mode dirt
            r = int(min(255, 134 * noise))
            g = int(min(255, 96 * noise))
            b = int(min(255, 67 * noise))
        row.extend([r, g, b, 255])
    grass_side_rows.append(row)
write_png(os.path.join(block_tex_dir, "grass_block_side.png"), 16, 16, grass_side_rows)
write_png(os.path.join(block_tex_dir, "grass_block_side_overlay.png"), 16, 16, grass_top_rows)
print("Added authentic Story Mode command block and grass textures")

# Modern Minecraft 1.21.2 2.5x Extrusion Cloud Vertex Shader in Resource Pack core
core_shader_dir = os.path.join(RP_DIR, "assets", "minecraft", "shaders", "core")
os.makedirs(core_shader_dir, exist_ok=True)
core_cloud_vsh = """#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

const int FLAG_MASK_DIR      = 7;
const int FLAG_INSIDE_FACE   = 1 << 4;
const int FLAG_USE_TOP_COLOR = 1 << 5;
const int FLAG_EXTRA_Z       = 1 << 6;
const int FLAG_EXTRA_X       = 1 << 7;

layout(std140) uniform CloudInfo {
    vec4 CloudColor;
    vec3 CloudOffset;
    vec3 CellSize;
};

uniform isamplerBuffer CloudFaces;

const float CloudFadeAlpha   = 0;
const float CloudHeight      = 2.5;   // 2.5x Story Mode vertical scaling
const float CloudYOffset     = 0.0;
const float BrightnessBottom = 1.0;
const float BrightnessTop    = 1.0;
const float BrightnessNorth  = 1.0;
const float BrightnessSouth  = 1.0;
const float BrightnessWest   = 1.0;
const float BrightnessEast   = 1.0;

out float vertexDistance;
out vec4 vertexColor;

const vec3[] vertices = vec3[](
    vec3(1,0,0),vec3(1,0,1),vec3(0,0,1),vec3(0,0,0),
    vec3(0,1,0),vec3(0,1,1),vec3(1,1,1),vec3(1,1,0),
    vec3(0,0,0),vec3(0,1,0),vec3(1,1,0),vec3(1,0,0),
    vec3(1,0,1),vec3(1,1,1),vec3(0,1,1),vec3(0,0,1),
    vec3(0,0,1),vec3(0,1,1),vec3(0,1,0),vec3(0,0,0),
    vec3(1,0,0),vec3(1,1,0),vec3(1,1,1),vec3(1,0,1)
);

float lerp(float d, float e, float f) {
    return e + d * (f - e);
}

void main() {
    int quadVertex = gl_VertexID % 4;
    int index = (gl_VertexID / 4) * 3;

    int cellX = texelFetch(CloudFaces, index).r;
    int cellZ = texelFetch(CloudFaces, index + 1).r;
    int dirAndFlags = texelFetch(CloudFaces, index + 2).r;
    int direction = dirAndFlags & FLAG_MASK_DIR;
    bool isInsideFace = (dirAndFlags & FLAG_INSIDE_FACE) != 0;
    bool useTopColor = (dirAndFlags & FLAG_USE_TOP_COLOR) != 0;

    cellX = (cellX << 1) | ((dirAndFlags & FLAG_EXTRA_X) >> 7);
    cellZ = (cellZ << 1) | ((dirAndFlags & FLAG_EXTRA_Z) >> 6);

    vec3 faceVertex = vertices[(direction * 4) + (isInsideFace ? 3 - quadVertex : quadVertex)];

    vec3 scaledVertex = faceVertex * CellSize;
    scaledVertex.y *= CloudHeight;
    vec3 pos = scaledVertex + (vec3(cellX, 0, cellZ) * CellSize) + CloudOffset + vec3(0, CloudYOffset, 0);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexDistance = fog_spherical_distance(pos);

    float brightness = 1.0;
    if (useTopColor || direction == 1) brightness = BrightnessTop;
    else if (direction == 0) brightness = BrightnessBottom;
    else if (direction == 2) brightness = BrightnessNorth;
    else if (direction == 3) brightness = BrightnessSouth;
    else if (direction == 4) brightness = BrightnessWest;
    else if (direction == 5) brightness = BrightnessEast;

    vec3 rgb = vec3(brightness);
    float baseA = CloudColor.a;
    float vertexY = pos.y - CloudOffset.y;
    float normalizedY = clamp(vertexY / CloudHeight, 0.0, 1.0);
    float dir = clamp(CloudOffset.y / CloudHeight, -1.0, 1.0);
    float fadeBelow = lerp(normalizedY, 1.0, CloudFadeAlpha);
    float fadeAbove = lerp(1.0 - normalizedY, 1.0, CloudFadeAlpha);
    float mixFactor = (dir + 1.0) * 0.5;
    float fade = mix(fadeBelow, fadeAbove, mixFactor);
    float finalA = baseA * (0.8 - fade);

    vertexColor = vec4(rgb, finalA) * CloudColor;
}
"""
with open(os.path.join(core_shader_dir, "rendertype_clouds.vsh"), "w", encoding="utf-8") as f:
    f.write(core_cloud_vsh)

# Resource pack icon
pack_png_rows = []
for py in range(64):
    row = []
    for px in range(64):
        t = (px + py) / 128.0
        r = int(140 + t * 108)
        g = int(135 + t * 47)
        b = int(232 - t * 160)
        row.extend([r, g, b, 255])
    pack_png_rows.append(row)
write_png(os.path.join(RP_DIR, "pack.png"), 64, 64, pack_png_rows)

# ----------------------------------------------------------------------
# 2. BUILD SHADER PACK (8 Presets, 2.5x Extrusion, Dynamic Sky, No Black Bands)
# ----------------------------------------------------------------------
print("[2/2] Assembling Minecraft: Story Mode Atmosphere Shader Pack...")
sp_shaders = os.path.join(SP_DIR, "shaders")
sp_core = os.path.join(sp_shaders, "core")
os.makedirs(sp_core, exist_ok=True)

# Generate local shader pack textures for all 8 presets in shaders/textures/clouds/ and textures/clouds/
cloud_tex_dir1 = os.path.join(sp_shaders, "textures", "clouds")
cloud_tex_dir2 = os.path.join(SP_DIR, "textures", "clouds")
os.makedirs(cloud_tex_dir1, exist_ok=True)
os.makedirs(cloud_tex_dir2, exist_ok=True)

PRESET_TEXTURE_COLORS = [
    (250, 252, 255, 240), # 0: Day
    (250, 185, 125, 230), # 1: Sunset
    (95, 105, 155, 220),  # 2: Night
    (58, 48, 68, 245),    # 3: Storm Gathering
    (32, 22, 54, 250),    # 4: Awakening (#00E5FF Cyan Rim)
    (216, 27, 96, 252),   # 5: Cataclysm (#D81B60 Magenta)
    (255, 109, 0, 255),   # 6: Volcanic Horizon
    (224, 176, 255, 235)  # 7: Twilight Purple / Flash
]

for idx, (cr, cg, cb, ca) in enumerate(PRESET_TEXTURE_COLORS):
    p_rows = []
    for y in range(64):
        p_row = []
        for x in range(64):
            bx = (x // 8) % 2
            by = (y // 8) % 2
            checker = 0.92 if (bx ^ by) else 1.05
            r = int(min(255, cr * checker))
            g = int(min(255, cg * checker))
            b = int(min(255, cb * checker))
            a = ca
            p_row.extend([r, g, b, a])
        p_rows.append(p_row)
    write_png(os.path.join(cloud_tex_dir1, f"cloud{idx}.png"), 64, 64, p_rows)
    write_png(os.path.join(cloud_tex_dir2, f"cloud{idx}.png"), 64, 64, p_rows)

print("Generated 8 local Story Mode cloud preset textures")

# 2.1 The Rewritten 8-Preset Cloud Vertex Shader (gbuffers_clouds.vsh)
gbuffers_clouds_vsh = """#version 120

// Identical high precision header to eliminate GPU compiler crashes
precision highp float;
precision highp int;

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform float frameTimeCounter;

varying vec4 vColor;
varying vec2 vTexCoord;
varying vec3 vWorldPos;
varying vec3 vNormal;
varying float vFogFactor;

// Global Story Mode Cloud Extrusion (2.5x vertical scaling)
const float CloudHeight = 2.5;

void main() {
    // 1. Transform vertex to camera-relative world coordinates
    vec3 eyePos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    vec3 worldPos = (gbufferModelViewInverse * vec4(eyePos, 1.0)).xyz;
    vec3 normal = gl_Normal;

    // 2. Vertically scale cloud geometry bounds by 2.5x for Story Mode chunk layout thickness
    float localExtrusion = 4.0 * (CloudHeight - 1.0); // 6.0 blocks expansion
    if (normal.y > 0.3) {
        worldPos.y += localExtrusion;
    } else if (abs(normal.y) < 0.3) {
        int q = int(mod(float(gl_VertexID), 4.0));
        if (q == 1 || q == 2) {
            worldPos.y += localExtrusion;
        }
    }

    // 3. Project back to clip space
    gl_Position = gl_ProjectionMatrix * (gbufferModelView * vec4(worldPos, 1.0));

    // 4. Pass varying attributes cleanly to fragment shader
    vTexCoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vNormal = normalize(gl_NormalMatrix * gl_Normal);
    vColor = gl_Color;
    vWorldPos = worldPos;
    vFogFactor = clamp((length(eyePos) - 160.0) / 180.0, 0.0, 1.0);
}
"""
with open(os.path.join(sp_shaders, "gbuffers_clouds.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_clouds_vsh)

# 2.2 Cloud Fragment Shader (gbuffers_clouds.fsh)
gbuffers_clouds_fsh = """#version 120

// Identical high precision header to eliminate GPU compiler crashes
precision highp float;
precision highp int;

// Local shader pack texture samplers for all 8 Story Mode cloud presets
uniform sampler2D gtexture;
uniform sampler2D cloudTex0; // 0: Overworld Day
uniform sampler2D cloudTex1; // 1: Sunset / Golden Hour
uniform sampler2D cloudTex2; // 2: Deep Night / Moonlight
uniform sampler2D cloudTex3; // 3: Storm Gathering
uniform sampler2D cloudTex4; // 4: Wither Awakening (Cyan Rim)
uniform sampler2D cloudTex5; // 5: Cataclysm (Pink-Magenta Anamorphic)
uniform sampler2D cloudTex6; // 6: Volcanic Horizon Mask
uniform sampler2D cloudTex7; // 7: Twilight Purple / Flash

uniform float frameTimeCounter;

varying vec4 vColor;
varying vec2 vTexCoord;
varying vec3 vWorldPos;
varying vec3 vNormal;
varying float vFogFactor;

struct CloudPreset {
    vec4 baseColor;
    vec3 highlightColor;
    vec3 shadowColor;
    vec2 speed;
    float extrusion;
    float weight;
};

void main() {
    CloudPreset presets[8];

    // Preset 0: Overworld Day (MCSM Normal / Default)
    presets[0].baseColor      = vec4(1.00, 1.00, 1.00, 0.90);
    presets[0].highlightColor = vec3(1.05, 1.02, 0.98);
    presets[0].shadowColor    = vec3(0.90, 0.88, 0.96);
    presets[0].speed          = vec2(1.0, 0.2) * 0.0006;
    presets[0].extrusion      = 2.5;
    presets[0].weight         = 0.35;

    // Preset 1: Sunset / Golden Hour (Warm Coral & Amber)
    presets[1].baseColor      = vec4(0.98, 0.68, 0.45, 0.88);
    presets[1].highlightColor = vec3(1.00, 0.84, 0.55);
    presets[1].shadowColor    = vec3(0.85, 0.42, 0.48);
    presets[1].speed          = vec2(0.8, 0.6) * 0.0008;
    presets[1].extrusion      = 2.8;
    presets[1].weight         = 0.20;

    // Preset 2: Deep Night / Moonlight (Silver & Periwinkle Indigo)
    presets[2].baseColor      = vec4(0.35, 0.38, 0.58, 0.82);
    presets[2].highlightColor = vec3(0.55, 0.62, 0.88);
    presets[2].shadowColor    = vec3(0.18, 0.16, 0.32);
    presets[2].speed          = vec2(0.5, -0.7) * 0.0005;
    presets[2].extrusion      = 2.4;
    presets[2].weight         = 0.15;

    // Preset 3: Storm Formative (Bruised Charcoal Overcast)
    presets[3].baseColor      = vec4(0.22, 0.18, 0.26, 0.92);
    presets[3].highlightColor = vec3(0.38, 0.30, 0.45);
    presets[3].shadowColor    = vec3(0.10, 0.08, 0.14);
    presets[3].speed          = vec2(1.8, 1.2) * 0.0012;
    presets[3].extrusion      = 3.0;
    presets[3].weight         = 0.12;

    // Preset 4: Awakening (Obsidian Purple with #00E5FF Cyan Rim Glow)
    presets[4].baseColor      = vec4(0.12, 0.08, 0.20, 0.95);
    presets[4].highlightColor = vec3(0.00, 0.90, 1.00);
    presets[4].shadowColor    = vec3(0.05, 0.02, 0.08);
    presets[4].speed          = vec2(-1.5, 2.0) * 0.0016;
    presets[4].extrusion      = 3.2;
    presets[4].weight         = 0.10;

    // Preset 5: Cataclysm Core (Pink-Magenta #D81B60 & Void-Violet #4A148C)
    presets[5].baseColor      = vec4(0.35, 0.05, 0.25, 0.98);
    presets[5].highlightColor = vec3(0.85, 0.11, 0.38);
    presets[5].shadowColor    = vec3(0.29, 0.08, 0.55);
    presets[5].speed          = vec2(2.5, -1.8) * 0.0020;
    presets[5].extrusion      = 3.6;
    presets[5].weight         = 0.08;

    // Preset 6: Volcanic Horizon Mask (Fire-Orange #FF6D00 & Blood-Red #D50000)
    presets[6].baseColor      = vec4(0.70, 0.15, 0.02, 1.00);
    presets[6].highlightColor = vec3(1.00, 0.43, 0.00);
    presets[6].shadowColor    = vec3(0.84, 0.00, 0.00);
    presets[6].speed          = vec2(-3.0, -2.5) * 0.0025;
    presets[6].extrusion      = 4.0;
    presets[6].weight         = 0.06;

    // Preset 7: Twilight Purple / End Flash (Twilight #E0B0FF & Flash Pulse)
    presets[7].baseColor      = vec4(0.88, 0.69, 1.00, 0.90);
    presets[7].highlightColor = vec3(0.98, 0.90, 1.00);
    presets[7].shadowColor    = vec3(0.45, 0.25, 0.65);
    presets[7].speed          = vec2(0.4, 0.4) * 0.0006;
    presets[7].extrusion      = 2.6;
    presets[7].weight         = 0.06;

    vec4 accumulatedColor = vec4(0.0);
    float totalWeight = 0.0;

    float isTop = clamp(vNormal.y, 0.0, 1.0);
    float isBottom = clamp(-vNormal.y, 0.0, 1.0);
    float isSide = clamp(1.0 - abs(vNormal.y), 0.0, 1.0);

    for (int i = 0; i < 8; i++) {
        vec2 uvOffset = presets[i].speed * frameTimeCounter;
        vec2 sampledUV = vTexCoord + uvOffset;

        vec4 sampledTex = vec4(1.0);
        if (i == 0) sampledTex = texture2D(cloudTex0, sampledUV);
        else if (i == 1) sampledTex = texture2D(cloudTex1, sampledUV);
        else if (i == 2) sampledTex = texture2D(cloudTex2, sampledUV);
        else if (i == 3) sampledTex = texture2D(cloudTex3, sampledUV);
        else if (i == 4) sampledTex = texture2D(cloudTex4, sampledUV);
        else if (i == 5) sampledTex = texture2D(cloudTex5, sampledUV);
        else if (i == 6) sampledTex = texture2D(cloudTex6, sampledUV);
        else if (i == 7) sampledTex = texture2D(cloudTex7, sampledUV);

        if (sampledTex.a < 0.01) {
            sampledTex = texture2D(gtexture, sampledUV);
            if (sampledTex.a < 0.01) {
                sampledTex = vec4(1.0);
            }
        }

        vec3 faceTint = mix(presets[i].shadowColor, presets[i].highlightColor, isTop * 0.70 + isSide * 0.40);
        if (isBottom > 0.5) {
            faceTint = presets[i].shadowColor;
        }

        vec4 presetFinal = vec4(presets[i].baseColor.rgb * faceTint * sampledTex.rgb, presets[i].baseColor.a * sampledTex.a);
        accumulatedColor += presetFinal * presets[i].weight;
        totalWeight += presets[i].weight;
    }

    if (totalWeight > 0.0) {
        accumulatedColor /= totalWeight;
    }

    if (accumulatedColor.a < 0.08) {
        discard;
    }

    accumulatedColor.rgb *= vColor.rgb;
    accumulatedColor.rgb = mix(accumulatedColor.rgb, vec3(0.68, 0.60, 0.88), vFogFactor * 0.45);
    gl_FragColor = accumulatedColor;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_clouds.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_clouds_fsh)

# Sync with core and rendertype variants
with open(os.path.join(sp_shaders, "rendertype_clouds.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_clouds_vsh)
with open(os.path.join(sp_shaders, "rendertype_clouds.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_clouds_fsh)
with open(os.path.join(sp_core, "rendertype_clouds.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_clouds_vsh)
with open(os.path.join(sp_core, "rendertype_clouds.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_clouds_fsh)

# 2.3 OptiFine / Iris Sky Dome (gbuffers_skybasic) - DYNAMIC TIME OF DAY BLEND
gbuffers_skybasic_vsh = """#version 120

precision highp float;
precision highp int;

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

precision highp float;
precision highp int;

uniform int worldTime;
uniform vec3 sunPosition;
uniform vec3 upPosition;

varying vec4 color;
varying vec3 viewPos;

// 1. Day Sky: Periwinkle Lavender -> Golden Amber
vec3 getDaySky(float h) {
    vec3 cZenith  = vec3(0.549, 0.529, 0.910); // #8c87e8
    vec3 cLilac   = vec3(0.686, 0.608, 0.886); // #af9be2
    vec3 cMauve   = vec3(0.835, 0.682, 0.839); // #d5aed6
    vec3 cPeach   = vec3(0.957, 0.722, 0.604); // #f4b89a
    vec3 cApricot = vec3(0.969, 0.769, 0.451); // #f7c473
    vec3 cHorizon = vec3(0.973, 0.714, 0.282); // #f8b648
    if (h < 0.08) return mix(cHorizon, cApricot, h / 0.08);
    if (h < 0.22) return mix(cApricot, cPeach, (h - 0.08) / 0.14);
    if (h < 0.45) return mix(cPeach, cMauve, (h - 0.22) / 0.23);
    if (h < 0.72) return mix(cMauve, cLilac, (h - 0.45) / 0.27);
    return mix(cLilac, cZenith, (h - 0.72) / 0.28);
}

// 2. Noon Sky: Vivid Bright Story Mode Azure
vec3 getNoonSky(float h) {
    vec3 cZenith  = vec3(0.368, 0.549, 0.949);
    vec3 cMid     = vec3(0.529, 0.765, 0.980);
    vec3 cHorizon = vec3(0.882, 0.894, 0.941);
    if (h < 0.35) return mix(cHorizon, cMid, h / 0.35);
    return mix(cMid, cZenith, (h - 0.35) / 0.65);
}

// 3. Sunset / Twilight Sky: Royal Violet -> Vivid Magenta -> Fiery Coral -> Orange
vec3 getSunsetSky(float h) {
    vec3 cZenith  = vec3(0.220, 0.039, 0.329); // #380a54 Royal dark violet
    vec3 cMagenta = vec3(0.486, 0.082, 0.408); // #7c1568 Rich magenta
    vec3 cRose    = vec3(0.663, 0.125, 0.447); // #a92072 Vibrant rose
    vec3 cCoral   = vec3(0.941, 0.314, 0.282); // #f05048 Fiery coral
    vec3 cHorizon = vec3(0.976, 0.533, 0.157); // #f98828 Fiery sunset orange
    if (h < 0.10) return mix(cHorizon, cCoral, h / 0.10);
    if (h < 0.30) return mix(cCoral, cRose, (h - 0.10) / 0.20);
    if (h < 0.60) return mix(cRose, cMagenta, (h - 0.30) / 0.30);
    return mix(cMagenta, cZenith, (h - 0.60) / 0.40);
}

// 4. Night Sky: Deep Obsidian Midnight -> Dark Violet -> Deep Indigo Horizon
vec3 getNightSky(float h) {
    vec3 cZenith  = vec3(0.063, 0.016, 0.110); // #10041c Deep obsidian midnight
    vec3 cMid     = vec3(0.098, 0.039, 0.176); // #190a2d Dark royal purple
    vec3 cHorizon = vec3(0.157, 0.110, 0.294); // #281c4b Deep twilight indigo
    if (h < 0.40) return mix(cHorizon, cMid, h / 0.40);
    return mix(cMid, cZenith, (h - 0.40) / 0.60);
}

void main() {
    vec3 nView = normalize(viewPos);
    // Smoothly extend horizon color below the horizon to eliminate dark bands completely
    float h = clamp(nView.y, 0.0, 1.0);

    float sunY = normalize(sunPosition).y;

    vec3 dayCol    = getDaySky(h);
    vec3 noonCol   = getNoonSky(h);
    vec3 sunsetCol = getSunsetSky(h);
    vec3 nightCol  = getNightSky(h);

    float noonWeight = clamp(sunY * 1.5 - 0.5, 0.0, 1.0);
    vec3 fullDayCol = mix(dayCol, noonCol, noonWeight);

    float sunsetWeight = clamp(1.0 - abs(sunY - 0.05) / 0.25, 0.0, 1.0);
    sunsetWeight = smoothstep(0.0, 1.0, sunsetWeight);

    float nightWeight = clamp((-sunY - 0.05) / 0.25, 0.0, 1.0);
    nightWeight = smoothstep(0.0, 1.0, nightWeight);

    float dayWeight = clamp((sunY - 0.10) / 0.25, 0.0, 1.0);
    dayWeight = smoothstep(0.0, 1.0, dayWeight);

    vec3 finalCol = fullDayCol * dayWeight + sunsetCol * sunsetWeight + nightCol * nightWeight;
    float totalW = dayWeight + sunsetWeight + nightWeight;
    if (totalW > 0.001) {
        finalCol /= totalW;
    } else {
        finalCol = fullDayCol;
    }

    finalCol *= max(color.rgb, vec3(0.35));
    gl_FragColor = vec4(finalCol, 1.0);
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skybasic.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skybasic_fsh)

# 2.4 Sky Textured (Sun, Moon, Starfields)
gbuffers_skytextured_vsh = """#version 120

precision highp float;
precision highp int;

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

precision highp float;
precision highp int;

uniform sampler2D gtexture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord) * color;
    if (col.a < 0.01) {
        discard;
    }
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skytextured.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skytextured_fsh)

# 2.5 Dedicated Hand Shader (gbuffers_hand.vsh and gbuffers_hand.fsh) - Eliminates Solid Black Hand Items
gbuffers_hand_vsh = """#version 120

precision highp float;
precision highp int;

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
with open(os.path.join(sp_shaders, "gbuffers_hand.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_hand_vsh)

gbuffers_hand_fsh = """#version 120

precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform sampler2D lightmap;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord);
    if (col.a < 0.1) {
        discard;
    }
    vec4 lm = texture2D(lightmap, lmcoord);
    vec3 light = max(lm.rgb, vec3(0.55));
    col.rgb *= color.rgb * light;
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_hand.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_hand_fsh)

# 2.6 Terrain: Authentic MCSM Colored Lighting & Shadows (No Reflections)
gbuffers_terrain_vsh = """#version 120

precision highp float;
precision highp int;

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

precision highp float;
precision highp int;

uniform sampler2D gtexture;
varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;

void main() {
    vec4 tex = texture2D(gtexture, texcoord) * color;
    if (tex.a < 0.1) {
        discard;
    }

    float blockLight = clamp((lmcoord.x - 0.03) * 1.05, 0.0, 1.0);
    float skyLight   = clamp((lmcoord.y - 0.03) * 1.05, 0.0, 1.0);

    vec3 sunLightColor = vec3(1.08, 1.00, 0.92);
    vec3 shadowAmbientColor = vec3(0.72, 0.65, 0.85);
    vec3 torchColor = vec3(1.15, 0.75, 0.40);

    vec3 skyLightTerm = mix(shadowAmbientColor * 0.70, sunLightColor, pow(skyLight, 1.25));
    vec3 blockLightTerm = torchColor * pow(blockLight, 1.35) * 1.30;

    vec3 ambientLighting = skyLightTerm + blockLightTerm;
    tex.rgb *= ambientLighting;

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

# 2.7 Entities: Luminescent Turquoise Teeth Glow (#00E5FF) and Magenta Eye Bloom
gbuffers_entities_vsh = """#version 120

precision highp float;
precision highp int;

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

precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform float frameTimeCounter;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord) * color;
    if (col.a < 0.1) {
        discard;
    }

    float isTurquoise = step(0.65, col.g) * step(0.75, col.b) * (1.0 - step(0.40, col.r));
    float isMagenta   = step(0.60, col.r) * step(0.60, col.b) * (1.0 - step(0.50, col.g));

    if (isTurquoise > 0.5) {
        float pulse = 0.90 + 0.10 * sin(frameTimeCounter * 4.0);
        col.rgb = vec3(0.0, 0.92, 1.0) * 3.5 * pulse;
    } else if (isMagenta > 0.5) {
        float pulse = 0.92 + 0.08 * sin(frameTimeCounter * 3.0);
        col.rgb = vec3(0.85, 0.12, 0.95) * 3.0 * pulse;
    }

    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_entities.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_entities_fsh)

# 2.8 Composite and Final Passes
composite_vsh = """#version 120

precision highp float;
precision highp int;

varying vec2 texcoord;

void main() {
    gl_Position = ftransform();
    texcoord = gl_MultiTexCoord0.xy;
}
"""
with open(os.path.join(sp_shaders, "composite.vsh"), "w", encoding="utf-8") as f:
    f.write(composite_vsh)

composite_fsh = """#version 120

precision highp float;
precision highp int;

uniform sampler2D colortex0;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(colortex0, texcoord);
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "composite.fsh"), "w", encoding="utf-8") as f:
    f.write(composite_fsh)

with open(os.path.join(sp_shaders, "final.vsh"), "w", encoding="utf-8") as f:
    f.write(composite_vsh)
with open(os.path.join(sp_shaders, "final.fsh"), "w", encoding="utf-8") as f:
    f.write(composite_fsh)

# 2.9 shaders.properties: Required schema with clouds=fast and customTexture.cloudTex0..7
shaders_properties = """# Minecraft: Story Mode — Shaderpack Configuration & Pipeline Routing
clouds=fast
customTexture.cloudTex0=shaders/textures/clouds/cloud0.png
customTexture.cloudTex1=shaders/textures/clouds/cloud1.png
customTexture.cloudTex2=shaders/textures/clouds/cloud2.png
customTexture.cloudTex3=shaders/textures/clouds/cloud3.png
customTexture.cloudTex4=shaders/textures/clouds/cloud4.png
customTexture.cloudTex5=shaders/textures/clouds/cloud5.png
customTexture.cloudTex6=shaders/textures/clouds/cloud6.png
customTexture.cloudTex7=shaders/textures/clouds/cloud7.png
"""
with open(os.path.join(SP_DIR, "shaders.properties"), "w", encoding="utf-8") as f:
    f.write(shaders_properties)
with open(os.path.join(sp_shaders, "shaders.properties"), "w", encoding="utf-8") as f:
    f.write(shaders_properties)

# Shaderpack README
sp_readme = """# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack (1.21.2 / 26.2)
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Features
- **Pipeline Cloud Routing**: `shaders.properties` with `clouds=fast` explicitly instructs Iris to intercept the cloud rendering loop and route geometry directly through `gbuffers_clouds`.
- **8 Story Mode Cloud Presets**: All 8 authentic cloud presets (Day, Sunset, Night, Storm Gathering, Awakening Cyan Rim, Cataclysm Magenta, Volcanic Horizon, Twilight Purple) forced to render globally without external map dependencies.
- **Identical Precision Headers**: Both `.vsh` and `.fsh` use `precision highp float; precision highp int;` to prevent GPU compiler crashes on load.
- **Dynamic Story Mode Sky Dome**: Smooth Day, Noon, Sunset, and Night transitions with zero void horizon black bands.
- **Story Mode Colored Lighting & Shadows**: Warm golden sunlight, lavender shadow tint, amber torchlight, NO reflections.
- **Teeth Turquoise Glow**: Vibrant cyan/turquoise glow (#00E5FF) pulsing on the Wither Storm teeth.
- **Hand Item Lighting**: Dedicated gbuffers_hand shaders ensuring items never render solid black.

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

print("Zipping MCSM_ResourcePack.zip...")
with zipfile.ZipFile(RP_ZIP, "w", zipfile.ZIP_DEFLATED) as z:
    for root_dir, _, files in os.walk(RP_DIR):
        for file in files:
            full_p = os.path.join(root_dir, file)
            rel_p = os.path.relpath(full_p, RP_DIR)
            z.write(full_p, rel_p)
print(f"Created {RP_ZIP} ({os.path.getsize(RP_ZIP)} bytes)")

print("\n--- MCSM PACK ASSEMBLY COMPLETE ---")
print(f"Resource Pack: {RP_ZIP} ({len(os.listdir(RP_DIR))} root entries)")
print(f"Shader Pack:   {SP_ZIP} ({len(os.listdir(SP_DIR))} root entries)")
