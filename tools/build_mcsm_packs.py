#!/usr/bin/env python3
"""
Build the fixed, standalone Minecraft: Story Mode Resource Pack and Shader Pack.

Deliverables:
1. MCSM_ResourcePack/ and MCSM_ResourcePack.zip
2. MCSM_ShaderPack/ and MCSM_ShaderPack.zip
"""

import os
import sys
import json
import shutil
import zipfile
import subprocess
import struct
import zlib
import math

ROOT = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
PR14_TMP = "/tmp/pr14_pack"

RP_DIR = os.path.join(ROOT, "MCSM_ResourcePack")
RP_ZIP = os.path.join(ROOT, "MCSM_ResourcePack.zip")

SP_DIR = os.path.join(ROOT, "MCSM_ShaderPack")
SP_ZIP = os.path.join(ROOT, "MCSM_ShaderPack.zip")

print("Starting MCSM Pack Assembly...")

# Clean output dirs
shutil.rmtree(RP_DIR, ignore_errors=True)
shutil.rmtree(SP_DIR, ignore_errors=True)
if os.path.exists(RP_ZIP): os.remove(RP_ZIP)
if os.path.exists(SP_ZIP): os.remove(SP_ZIP)

# -------------------------------------------------------------
# 1. BUILD RESOURCE PACK
# -------------------------------------------------------------
print("Building MCSM Resource Pack...")

# pack.mcmeta
rp_meta = {
    "pack": {
        "pack_format": 46,
        "supported_formats": {
            "min_inclusive": 15,
            "max_inclusive": 60
        },
        "description": "Minecraft: Story Mode - Authentic MCSM Visuals, Sky & Atmosphere"
    }
}
os.makedirs(RP_DIR, exist_ok=True)
with open(os.path.join(RP_DIR, "pack.mcmeta"), "w", encoding="utf-8") as f:
    json.dump(rp_meta, f, indent=2)

# Copy pack assets from /tmp/pr14_pack into assets/
# namespaces: minecraft, witherstormmod, cmdblockascension, minecraft-cursor
for ns in ["minecraft", "witherstormmod", "cmdblockascension", "minecraft-cursor"]:
    src_ns = os.path.join(PR14_TMP, ns)
    if os.path.exists(src_ns):
        dst_ns = os.path.join(RP_DIR, "assets", ns)
        shutil.copytree(src_ns, dst_ns)
        print(f"Copied {ns} into assets/{ns}")

# Fix model parent references in witherstormmod
# 1) formidibomb.json
formidibomb_path = os.path.join(RP_DIR, "assets", "witherstormmod", "models", "block", "formidibomb.json")
if os.path.exists(formidibomb_path):
    with open(formidibomb_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    data["parent"] = "block/block"
    with open(formidibomb_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print("Fixed formidibomb.json parent reference")

# 2) super_tnt.json
super_tnt_path = os.path.join(RP_DIR, "assets", "witherstormmod", "models", "block", "super_tnt.json")
if os.path.exists(super_tnt_path):
    with open(super_tnt_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    data["parent"] = "block/block"
    with open(super_tnt_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print("Fixed super_tnt.json parent reference")

# 3) command_block_book.json
book_path = os.path.join(RP_DIR, "assets", "witherstormmod", "models", "item", "command_block_book.json")
if os.path.exists(book_path):
    with open(book_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    data["parent"] = "item/generated"
    with open(book_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print("Fixed command_block_book.json parent reference")

# Wire all sounds in sounds.json
sounds_json_path = os.path.join(RP_DIR, "assets", "minecraft", "sounds.json")
sounds_data = {
    "music.menu": {
        "replace": True,
        "sounds": [
            {
                "name": "music/menu/title_theme",
                "stream": True
            }
        ]
    },
    "ui.button.click": {
        "replace": True,
        "sounds": [
            "random/click_stereo"
        ]
    },
    "ui.toast.in": {
        "replace": True,
        "sounds": [
            "ui/toast/in",
            "ui/toast/in1",
            "ui/toast/in2",
            "ui/toast/in3"
        ]
    }
}
with open(sounds_json_path, "w", encoding="utf-8") as f:
    json.dump(sounds_data, f, indent=2)
print("Updated assets/minecraft/sounds.json with full MCSM sound table")

# Add core cloud shaders to assets/minecraft/shaders/core/
cloud_core_dir = os.path.join(RP_DIR, "assets", "minecraft", "shaders", "core")
os.makedirs(cloud_core_dir, exist_ok=True)

cloud_vsh_content = """#version 150

// Authentic Minecraft: Story Mode cloud vertex shader
// Flat directional shading + smooth underside atmospheric fade (non-negative)

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

// Story Mode Shading Knobs
const float CloudHeight      = 1.0;   // Clean slab geometry
const float CloudYOffset     = 0.0;
const float BrightnessBottom = 0.62;  // Softly shaded underside
const float BrightnessTop    = 1.00;  // Full crisp illuminated tops
const float BrightnessNorth  = 0.80;  // Gentle directional side lighting
const float BrightnessSouth  = 0.80;
const float BrightnessWest   = 0.88;
const float BrightnessEast   = 0.88;
const float BottomAlpha      = 0.70;  // Underside translucency floor

out float vertexDistance;
out vec4 vertexColor;

const vec3[] vertices = vec3[](
    vec3(1,0,0),vec3(1,0,1),vec3(0,0,1),vec3(0,0,0),   // Bottom
    vec3(0,1,0),vec3(0,1,1),vec3(1,1,1),vec3(1,1,0),   // Top
    vec3(0,0,0),vec3(0,1,0),vec3(1,1,0),vec3(1,0,0),   // North
    vec3(1,0,1),vec3(1,1,1),vec3(0,1,1),vec3(0,0,1),   // South
    vec3(0,0,1),vec3(0,1,1),vec3(0,1,0),vec3(0,0,0),   // West
    vec3(1,0,0),vec3(1,1,0),vec3(1,1,1),vec3(1,0,1)    // East
);

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

    float slabHeight = max(CellSize.y * CloudHeight, 0.001);
    float normalizedY = clamp(faceVertex.y, 0.0, 1.0);
    float alpha = mix(BottomAlpha, 1.0, normalizedY);

    // Apply color and clamp alpha to valid range [0, 1]
    vertexColor = vec4(vec3(brightness) * CloudColor.rgb, clamp(alpha * CloudColor.a, 0.0, 1.0));
}
"""

cloud_fsh_content = """#version 150

#moj_import <minecraft:fog.glsl>

in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    color.a *= clamp(1.0 - linear_fog_value(vertexDistance, 0.0, FogCloudsEnd), 0.0, 1.0);
    fragColor = color;
}
"""

cloud_json_content = """{
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
        { "name": "CloudInfo", "type": "float", "count": 12, "values": [ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ] },
        { "name": "FogStart", "type": "float", "count": 1, "values": [ 0.0 ] },
        { "name": "FogEnd", "type": "float", "count": 1, "values": [ 1.0 ] },
        { "name": "FogShape", "type": "int", "count": 1, "values": [ 0 ] }
    ]
}
"""

with open(os.path.join(cloud_core_dir, "rendertype_clouds.vsh"), "w", encoding="utf-8") as f:
    f.write(cloud_vsh_content)
with open(os.path.join(cloud_core_dir, "rendertype_clouds.fsh"), "w", encoding="utf-8") as f:
    f.write(cloud_fsh_content)
with open(os.path.join(cloud_core_dir, "rendertype_clouds.json"), "w", encoding="utf-8") as f:
    f.write(cloud_json_content)
print("Wrote fixed core cloud shaders into assets/minecraft/shaders/core/")

# Add OptiFine Custom Sky in assets/minecraft/optifine/sky/world0/
opti_sky_dir = os.path.join(RP_DIR, "assets", "minecraft", "optifine", "sky", "world0")
os.makedirs(opti_sky_dir, exist_ok=True)

# Copy the pink sky image to sky1.png
sky_img_src = os.path.join(ROOT, "sky_only_no_clouds.png")
sky_img_dst = os.path.join(opti_sky_dir, "sky1.png")
shutil.copyfile(sky_img_src, sky_img_dst)

# sky1.properties for OptiFine custom sky
sky1_props = """# Minecraft: Story Mode Authentic Pink Twilight Sky
source=sky1.png
startFadeIn=0:00
endFadeIn=4:00
endFadeOut=19:00
startFadeOut=24:00
blend=replace
rotate=true
speed=0.15
axis=0.0 0.0 1.0
"""
with open(os.path.join(opti_sky_dir, "sky1.properties"), "w", encoding="utf-8") as f:
    f.write(sky1_props)

# FabricSkyBoxes format for Fabric users
fsb_dir = os.path.join(RP_DIR, "assets", "fabricskyboxes", "sky")
os.makedirs(fsb_dir, exist_ok=True)
fsb_tex_dir = os.path.join(RP_DIR, "assets", "fabricskyboxes", "textures", "sky")
os.makedirs(fsb_tex_dir, exist_ok=True)
shutil.copyfile(sky_img_src, os.path.join(fsb_tex_dir, "mcsm_sky.png"))

fsb_json = {
    "schemaVersion": 2,
    "type": "single-sprite-square",
    "blend": "add",
    "texture": "fabricskyboxes:textures/sky/mcsm_sky.png",
    "properties": {
        "rotation": {
            "axis": [0.0, 1.0, 0.0],
            "speed": 0.1
        },
        "fade": {
            "alwaysOn": True
        }
    }
}
with open(os.path.join(fsb_dir, "mcsm_twilight.json"), "w", encoding="utf-8") as f:
    json.dump(fsb_json, f, indent=2)
print("Configured OptiFine and FabricSkyBoxes custom sky entries")

# Colormaps (Lush green Story Mode world)
def write_png(path: str, width: int, height: int, rows: list) -> None:
    raw = b"".join(b"\x00" + bytes(r) for r in rows)
    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))

def clamp01(v: float) -> float:
    return max(0.0, min(1.0, v))

def colormap(size: int, tl: tuple, tr: tuple, bl: tuple, br: tuple) -> list:
    out = []
    for y in range(size):
        fy = y / (size - 1)
        row = []
        for x in range(size):
            fx = x / (size - 1)
            top = tuple(tl[i] + (tr[i] - tl[i]) * fx for i in range(3))
            bot = tuple(bl[i] + (br[i] - bl[i]) * fx for i in range(3))
            c = tuple(clamp01(top[i] + (bot[i] - top[i]) * fy) for i in range(3))
            ripple = 1.0 + 0.02 * math.sin(fx * 9.0) * math.cos(fy * 7.0)
            row += [int(clamp01(c[i] * ripple) * 255.0) for i in range(3)] + [255]
        out.append(row)
    return out

colormap_dir = os.path.join(RP_DIR, "assets", "minecraft", "textures", "colormap")
write_png(os.path.join(colormap_dir, "grass.png"), 256, 256, colormap(256, (0.36, 0.58, 0.28), (0.62, 0.74, 0.28), (0.24, 0.52, 0.36), (0.48, 0.78, 0.34)))
write_png(os.path.join(colormap_dir, "foliage.png"), 256, 256, colormap(256, (0.38, 0.48, 0.22), (0.60, 0.66, 0.24), (0.26, 0.46, 0.26), (0.50, 0.70, 0.30)))
print("Generated lush Story Mode grass and foliage colormaps")

# Duplicate witherstormmod into devouringstorms and dabywitherstormmod for full mod compat
for alias_ns in ["devouringstorms", "dabywitherstormmod"]:
    dst = os.path.join(RP_DIR, "assets", alias_ns)
    if not os.path.exists(dst):
        shutil.copytree(os.path.join(RP_DIR, "assets", "witherstormmod"), dst)
        print(f"Mirrored witherstormmod assets into assets/{alias_ns} for complete multi-mod compatibility")

# Generate pack.png (Icon with pink sky and gold crosshair/badge)
def gen_pack_icon(path: str, size: int = 128) -> None:
    rows = []
    # Gradient matching sky_only_no_clouds.png
    stops = [
        (0.0,  (16, 9, 48)),
        (0.2,  (35, 15, 79)),
        (0.45, (68, 28, 106)),
        (0.7,  (151, 74, 128)),
        (0.88, (197, 114, 142)),
        (0.96, (236, 152, 145)),
        (1.0,  (253, 195, 140))
    ]
    def get_color(t):
        for i in range(len(stops) - 1):
            t0, c0 = stops[i]
            t1, c1 = stops[i+1]
            if t0 <= t <= t1:
                f = (t - t0) / (t1 - t0)
                return [int(c0[j] + (c1[j] - c0[j]) * f) for j in range(3)]
        return list(stops[-1][1])

    cx, cy = size / 2, size / 2
    for y in range(size):
        row = []
        t = y / (size - 1)
        base = get_color(t)
        for x in range(size):
            r, g, b = base[0], base[1], base[2]
            # Draw a subtle diamond amulet / command block motif in the center
            dx = abs(x - cx)
            dy = abs(y - cy)
            dist_d = dx + dy
            # Outer gold border
            if 28 <= dist_d <= 32:
                r, g, b = 255, 215, 80
            elif 24 <= dist_d < 28:
                r, g, b = 40, 20, 70
            elif dist_d < 24:
                # Glowing center
                glow = 1.0 - (dist_d / 24.0)
                r = int(min(255, r + 200 * glow))
                g = int(min(255, g + 80 * glow))
                b = int(min(255, b + 240 * glow))
            row += [r, g, b, 255]
        rows.append(row)
    write_png(path, size, size, rows)

gen_pack_icon(os.path.join(RP_DIR, "pack.png"), 128)
print("Generated pack.png icon")

# Zip the Resource Pack
print("Zipping MCSM_ResourcePack.zip...")
with zipfile.ZipFile(RP_ZIP, "w", zipfile.ZIP_DEFLATED) as z:
    for root_dir, _, files in os.walk(RP_DIR):
        for file in files:
            full_p = os.path.join(root_dir, file)
            rel_p = os.path.relpath(full_p, RP_DIR)
            z.write(full_p, rel_p)
print(f"Created {RP_ZIP} ({os.path.getsize(RP_ZIP)} bytes)")


# -------------------------------------------------------------
# 2. BUILD SHADER PACK
# -------------------------------------------------------------
print("\nBuilding MCSM Shader Pack...")
sp_shaders = os.path.join(SP_DIR, "shaders")
os.makedirs(sp_shaders, exist_ok=True)

# shaders.properties
shaders_props_content = """# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack
# Compatible with Iris (Fabric) and OptiFine (Java Edition).

# Sun, moon, and vanilla celestial alignment
clouds=off
sky.stars=vanilla
sun=true
moon=true

shadow.enabled=false
particles.before=false

# Visual Configuration
profile=mcsm_story_mode
MCSM_PINK_SKY=1
MCSM_HORIZON_GLOW=1
MCSM_ROILING_CLOUDS=1
MCSM_STORM_FOG=1
MCSM_CINEMATIC_GRADE=1
MCSM_VIGNETTE=0
MCSM_GRAIN=0
MCSM_RETRO_VHS=0
"""
with open(os.path.join(sp_shaders, "shaders.properties"), "w", encoding="utf-8") as f:
    f.write(shaders_props_content)

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

# gbuffers_skybasic.fsh — THE MCSM PINK TWILIGHT SKY!
gbuffers_skybasic_fsh = """#version 120

/*
 * MINECRAFT: STORY MODE — AUTHENTIC PINK TWILIGHT SKY & ATMOSPHERE
 * Recreates the iconic Story Mode sky gradient (deep navy -> rich purple ->
 * vibrant rose pink -> warm glowing peach horizon) with roiling storm clouds.
 */

uniform mat4 gbufferModelView;
uniform float frameTimeCounter;

varying vec4 intColor;
varying vec3 viewPos;

#ifndef MCSM_PINK_SKY
#define MCSM_PINK_SKY 1
#endif
#ifndef MCSM_HORIZON_GLOW
#define MCSM_HORIZON_GLOW 1
#endif
#ifndef MCSM_ROILING_CLOUDS
#define MCSM_ROILING_CLOUDS 1
#endif

// Fast 2D hash and noise
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += amp * vnoise(p);
        p = p * 2.15 + vec2(11.3, 7.1);
        amp *= 0.5;
    }
    return v;
}

// Authentic Minecraft: Story Mode Sky Gradient (calibrated to sky_only_no_clouds.png)
vec3 getStoryModeSky(float elev) {
    vec3 cVoid    = vec3(0.047, 0.024, 0.094); // #0c0618 under-horizon void
    vec3 cPeach   = vec3(0.992, 0.765, 0.549); // #fdc38c warm peach horizon
    vec3 cCoral   = vec3(0.925, 0.596, 0.569); // #ec9891 soft coral pink
    vec3 cRose    = vec3(0.773, 0.447, 0.557); // #c5728e rich rose pink
    vec3 cPink    = vec3(0.592, 0.290, 0.502); // #974a80 vibrant magenta pink
    vec3 cMagenta = vec3(0.416, 0.192, 0.459); // #6a3175 royal magenta purple
    vec3 cViolet  = vec3(0.267, 0.110, 0.416); // #441c6a rich violet purple
    vec3 cPurple  = vec3(0.137, 0.059, 0.310); // #230f4f deep purple
    vec3 cIndigo  = vec3(0.063, 0.035, 0.188); // #100930 deep midnight indigo zenith

    if (elev < 0.0) {
        float t = clamp(-elev / 0.25, 0.0, 1.0);
        return mix(cPeach, cVoid, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.08) {
        float t = elev / 0.08;
        return mix(cPeach, cCoral, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.18) {
        float t = (elev - 0.08) / 0.10;
        return mix(cCoral, cRose, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.30) {
        float t = (elev - 0.18) / 0.12;
        return mix(cRose, cPink, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.42) {
        float t = (elev - 0.30) / 0.12;
        return mix(cPink, cMagenta, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.58) {
        float t = (elev - 0.42) / 0.16;
        return mix(cMagenta, cViolet, smoothstep(0.0, 1.0, t));
    } else if (elev < 0.75) {
        float t = (elev - 0.58) / 0.17;
        return mix(cViolet, cPurple, smoothstep(0.0, 1.0, t));
    } else {
        float t = clamp((elev - 0.75) / 0.25, 0.0, 1.0);
        return mix(cPurple, cIndigo, smoothstep(0.0, 1.0, t));
    }
}

void main() {
    // Un-rotate view vector to world space unit vector
    vec3 dirV = normalize(viewPos);
    vec3 dir = normalize(transpose(mat3(gbufferModelView)) * dirV);

    float elev = dir.y;

    #if MCSM_PINK_SKY
    vec3 skyCol = getStoryModeSky(elev);

    // Warm luminous horizon glow accent
    #if MCSM_HORIZON_GLOW
    float horizBand = exp(-pow(max(elev, 0.0) * 8.0, 2.0));
    skyCol += vec3(0.99, 0.75, 0.52) * horizBand * 0.28;
    #endif

    // Roiling Minecraft Story Mode storm clouds
    #if MCSM_ROILING_CLOUDS
    if (elev > 0.05) {
        vec2 cp = dir.xz / max(elev + 0.22, 0.1);
        float time = frameTimeCounter * 0.025;
        float ang = atan(cp.y, cp.x) + time * 0.4;
        float rad = length(cp);
        vec2 swirlPos = vec2(cos(ang), sin(ang)) * rad;

        float c = fbm(swirlPos * 1.1 + vec2(time * 0.5, time * 0.3));
        float cloudMask = smoothstep(0.42, 0.78, c) * clamp(elev * 2.2, 0.0, 1.0);

        // Story Mode clouds: underlit by pink/magenta horizon, darker crowns
        vec3 cloudBase = vec3(0.12, 0.05, 0.20);
        vec3 cloudRim  = vec3(0.85, 0.45, 0.68);
        float under = fbm(swirlPos * 2.2 - vec2(time * 0.7, 0.0));
        vec3 cloudCol = mix(cloudBase, cloudRim, smoothstep(0.35, 0.80, under) * 0.75);

        skyCol = mix(skyCol, cloudCol, cloudMask * 0.65);
    }
    #endif

    // Subtle twinkling stars in the high indigo dome
    if (elev > 0.45) {
        vec2 starPos = dir.xz / (elev + 0.2);
        float s = hash(floor(starPos * 180.0));
        if (s > 0.991) {
            float twinkle = 0.5 + 0.5 * sin(frameTimeCounter * 3.5 + s * 6.28);
            skyCol += vec3(0.9, 0.85, 1.0) * twinkle * (elev - 0.45) * 1.4;
        }
    }

    gl_FragColor = vec4(skyCol, 1.0);
    #else
    gl_FragColor = intColor;
    #endif
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skybasic.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skybasic_fsh)

# gbuffers_skytextured.vsh / fsh (Sun & Moon)
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
    // Story Mode warm golden celestial bloom
    col.rgb *= vec3(1.08, 0.98, 0.92);
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_skytextured.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_skytextured_fsh)

# gbuffers_terrain.vsh / fsh
gbuffers_terrain_vsh = """#version 120

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = gl_MultiTexCoord0.xy;
    lmcoord = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    normal = gl_NormalMatrix * gl_Normal;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_terrain.vsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_terrain_vsh)

gbuffers_terrain_fsh = """#version 120

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;
    // Subtle Story Mode ambient grade: rich greens, harmonious lighting
    col.rgb *= vec3(1.01, 0.99, 1.03);
    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_terrain.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_terrain_fsh)

# gbuffers_entities.vsh / fsh
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
    float isHotMagenta = step(0.70, col.r) * step(0.70, col.b) * (1.0 - step(0.50, col.g));
    float isCyanGlow   = step(0.70, col.g) * step(0.70, col.b) * (1.0 - step(0.50, col.r));
    float emissive = max(isHotMagenta, isCyanGlow);

    if (emissive > 0.5) {
        float pulse = 0.85 + 0.15 * sin(frameTimeCounter * 3.0);
        col.rgb *= 1.8 * pulse;
    }

    gl_FragColor = col;
}
"""
with open(os.path.join(sp_shaders, "gbuffers_entities.fsh"), "w", encoding="utf-8") as f:
    f.write(gbuffers_entities_fsh)

# composite.vsh / fsh (Atmospheric Pink Fog!)
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
 * Story Mode Atmospheric Distance Fog
 * Melts terrain smoothly into the pink/coral horizon haze instead of drab grey.
 */

uniform sampler2D colortex0;
uniform sampler2D depthtex0;
uniform float near;
uniform float far;
uniform vec3 fogColor;
uniform float rainStrength;
uniform int lightningBolt;
uniform float frameTimeCounter;

varying vec2 texcoord;

#ifndef MCSM_STORM_FOG
#define MCSM_STORM_FOG 1
#endif

float linearizeDepth(float z) {
    float ndc = z * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - ndc * (far - near));
}

void main() {
    vec3 col = texture2D(colortex0, texcoord).rgb;
    float depth = texture2D(depthtex0, texcoord).x;

    #if MCSM_STORM_FOG
    if (depth < 0.9999) {
        float dist = linearizeDepth(depth);
        float fogFactor = 1.0 - exp(-dist * 0.007 * (1.0 + rainStrength * 0.8));
        fogFactor = clamp(fogFactor, 0.0, 0.88);

        // Story Mode Rose-Coral Atmospheric Fog
        vec3 storyModeFog = vec3(0.85, 0.48, 0.58) * (0.85 + 0.15 * fogColor);
        col = mix(col, storyModeFog, fogFactor);
    }
    #endif

    // Lightning storm sky flash
    if (lightningBolt > 0) {
        col += vec3(0.95, 0.70, 0.90) * 0.15;
    }

    gl_FragColor = vec4(col, 1.0);
}
"""
with open(os.path.join(sp_shaders, "composite.fsh"), "w", encoding="utf-8") as f:
    f.write(composite_fsh)

# final.vsh / fsh (Cinematic Color Grading)
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
 * Applies clean, vibrant Telltale color grading, optional subtle vignette,
 * and frames the world cleanly without screen tears or noise.
 */

uniform sampler2D colortex0;
uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;

varying vec2 texcoord;

#ifndef MCSM_CINEMATIC_GRADE
#define MCSM_CINEMATIC_GRADE 1
#endif
#ifndef MCSM_VIGNETTE
#define MCSM_VIGNETTE 0
#endif
#ifndef MCSM_GRAIN
#define MCSM_GRAIN 0
#endif
#ifndef MCSM_RETRO_VHS
#define MCSM_RETRO_VHS 0
#endif

float hash(float x) {
    return fract(sin(x * 127.1 + 311.7) * 43758.5453);
}

void main() {
    vec2 uv = texcoord;
    vec3 col = texture2D(colortex0, uv).rgb;

    #if MCSM_CINEMATIC_GRADE
    // Story Mode Vibrancy & Saturation Push
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(lum), col, 1.15); // Enhanced color pop
    // Warm tone curve
    col = pow(col, vec3(0.96, 0.94, 0.98));
    #endif

    #if MCSM_VIGNETTE
    float d = distance(uv, vec2(0.5));
    float vig = smoothstep(0.40, 0.92, d);
    col = mix(col, col * 0.70, vig * 0.40);
    #endif

    #if MCSM_GRAIN
    float noise = (hash(uv.x * 913.0 + uv.y * 719.0 + frameTimeCounter * 60.0) - 0.5) * 0.025;
    col += noise;
    #endif

    #if MCSM_RETRO_VHS
    float scan = 0.98 + 0.02 * sin(uv.y * viewHeight * 3.14159);
    col *= scan;
    #endif

    gl_FragColor = vec4(col, 1.0);
}
"""
with open(os.path.join(sp_shaders, "final.fsh"), "w", encoding="utf-8") as f:
    f.write(final_fsh)

# Shader README
sp_readme = """# MINECRAFT: STORY MODE — Official Shaderpack
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Features
- **Authentic MCSM Pink Twilight Sky**: Full dynamic sky dome matching the signature
  Story Mode twilight palette (midnight indigo zenith -> royal purple -> vibrant rose pink -> warm peach sunset glow).
- **Roiling Storm Cloud Bank**: Organic, roiling cloud ceiling underlit by the twilight glow.
- **Atmospheric Pink Distance Fog**: Smoothly dissolves distant terrain into the rose-coral horizon haze.
- **Celestial Story Mode Bloom**: Golden sun and moon glow.
- **Emissive Highlights**: Enhances glowing items (Command Blocks, Amulets, Formidibomb, and Wither Storm cores).
- **Cinematic Color Grading**: Rich Story Mode color vibrancy.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine for your Minecraft version.
2. Copy `MCSM_ShaderPack.zip` into your `.minecraft/shaderpacks/` directory.
3. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
"""
with open(os.path.join(SP_DIR, "README.md"), "w", encoding="utf-8") as f:
    f.write(sp_readme)

print("Wrote all shader files into MCSM_ShaderPack/shaders/")

# Zip the Shader Pack
print("Zipping MCSM_ShaderPack.zip...")
with zipfile.ZipFile(SP_ZIP, "w", zipfile.ZIP_DEFLATED) as z:
    for root_dir, _, files in os.walk(SP_DIR):
        for file in files:
            full_p = os.path.join(root_dir, file)
            rel_p = os.path.relpath(full_p, SP_DIR)
            z.write(full_p, rel_p)
print(f"Created {SP_ZIP} ({os.path.getsize(SP_ZIP)} bytes)")

print("\n--- MCSM ASSEMBLE COMPLETE ---")
print(f"Resource Pack: {RP_ZIP} ({len(os.listdir(RP_DIR))} root entries)")
print(f"Shader Pack:   {SP_ZIP} ({len(os.listdir(SP_DIR))} root entries)")
