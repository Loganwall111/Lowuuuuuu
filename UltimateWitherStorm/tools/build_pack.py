#!/usr/bin/env python3
"""Merge every source pack in `stuff/` into one Ultimate MCSM Wither Storm pack.

Merge order (later wins):
  1. Tainted's Accurate Wither Storm Models v3.0.2.2   (all CEM .jem models)
  2. TAW-OG Story Mode Wither Storm Textures v1.0.1    (the OG look)
  3. TAW Plus - Wither Storm Extras v1.0.1             (extra blocks/items)
  4. Story Mode Clouds / Visuals Shader / Atmosphere   (shaders + skies)
  5. Generated art (halos, panorama, skybox)           - already written

Then it post-processes:
  * turquoise emissive glow burned onto the Wither Storm's teeth
  * per-phase halo geometry injected into the CEM models
"""
import os, json, shutil, zipfile, re
from PIL import Image
import numpy as np

DL = "/home/user/dl"
OUT = "/home/user/UltimateWitherStorm/resourcepack"
os.makedirs(OUT, exist_ok=True)

log = []


def unzip(path, dest):
    if not os.path.exists(path):
        log.append(f"  !! missing {os.path.basename(path)}")
        return False
    os.makedirs(dest, exist_ok=True)
    try:
        with zipfile.ZipFile(path) as z:
            z.extractall(dest)
        return True
    except Exception as e:
        log.append(f"  !! bad zip {os.path.basename(path)}: {e}")
        return False


def merge_tree(src, dst, only=None, skip_ext=()):
    """Copy src over dst, later callers win. `only` limits to sub-paths."""
    n = 0
    for root, _, files in os.walk(src):
        for f in files:
            sp = os.path.join(root, f)
            rel = os.path.relpath(sp, src).replace("\\", "/")
            if only and not any(rel.startswith(o) for o in only):
                continue
            if f.lower().endswith(skip_ext):
                continue
            dp = os.path.join(dst, rel)
            os.makedirs(os.path.dirname(dp), exist_ok=True)
            shutil.copy2(sp, dp)
            n += 1
    return n


# ---------------------------------------------------------------- 1. sources
work = f"{DL}/_merge"
shutil.rmtree(work, ignore_errors=True)
os.makedirs(work, exist_ok=True)

SOURCES = [
    ("Tainted's Accurate Wither Storm Models v3.0.2.2 (1).zip", "taw", ["assets/"]),
    ("TAW-OG Story Mode Wither Storm Textures v1.0.1.zip", "og", ["assets/"]),
    ("TAW Plus - Wither Storm Extras v1.0.1.zip", "extras", ["assets/"]),
    ("MCSM_Ultimate_Atmosphere_FIXED.zip", "atmo", ["assets/"]),
    ("StoryMode_Atmosphere.zip", "smatmo", ["assets/"]),
    ("Story_Mode_Visuals_Shader.zip", "smvis", ["assets/"]),
    ("Vanilla_Shader_V2_Beta.zip", "vshader", ["assets/"]),
]
for zname, sub, only in SOURCES:
    d = f"{work}/{sub}"
    if unzip(f"{DL}/{zname}", d):
        c = merge_tree(d, OUT, only=only)
        log.append(f"  + {zname}  ->  {c} files")

# Story Mode Clouds ships per-version folders; take the newest that exists
d = f"{work}/clouds"
if unzip(f"{DL}/Story Mode Clouds.zip", d):
    for v in ("1.21.6", "1.21.5", "1.20.1", ""):
        p = os.path.join(d, v) if v else d
        if os.path.isdir(os.path.join(p, "assets")):
            log.append(f"  + Story Mode Clouds ({v or 'root'}) -> {merge_tree(p, OUT, only=['assets/'])} files")
            break

# Cracker's own OG-ish entity textures as a fallback layer (only fill gaps)
jar = f"{work}/cwsm"
if unzip(f"{DL}/witherstormmod-1.20.1-4.2.1-all.zip", jar):
    inner = os.path.join(jar, "witherstormmod-1.20.1-4.2.1-all.jar")
    if os.path.exists(inner):
        unzip(inner, f"{jar}/x")
        src = f"{jar}/x/assets/witherstormmod/textures"
        added = 0
        for root, _, files in os.walk(src):
            for f in files:
                sp = os.path.join(root, f)
                rel = os.path.relpath(sp, src)
                dp = os.path.join(OUT, "assets/witherstormmod/textures", rel)
                if not os.path.exists(dp):  # never overwrite the OG pack
                    os.makedirs(os.path.dirname(dp), exist_ok=True)
                    shutil.copy2(sp, dp); added += 1
        log.append(f"  + CWSM base textures (gap-fill only) -> {added} files")

# ------------------------------------------------- 2. turquoise glowing teeth
TURQ = np.array([64, 240, 224], np.float32)   # turquoise
TEETH = {}


def is_tooth(px):
    """Bone/ivory coloured and bright -> a tooth."""
    r, g, b, a = px[..., 0], px[..., 1], px[..., 2], px[..., 3]
    bright = (r > 165) & (g > 160) & (b > 120)
    warmish = (np.abs(r.astype(int) - g.astype(int)) < 46) & (r.astype(int) - b.astype(int) > -12)
    return bright & warmish & (a > 40)


def add_teeth_glow(base_rel, emis_rel):
    bp = os.path.join(OUT, base_rel)
    if not os.path.exists(bp):
        return 0
    base = np.asarray(Image.open(bp).convert("RGBA"), np.uint8)
    mask = is_tooth(base)
    if mask.sum() == 0:
        return 0
    ep = os.path.join(OUT, emis_rel)
    if os.path.exists(ep):
        em = np.asarray(Image.open(ep).convert("RGBA"), np.uint8).copy()
        if em.shape[:2] != base.shape[:2]:
            em = np.zeros_like(base)
    else:
        em = np.zeros_like(base)
    em[mask, 0] = TURQ[0]; em[mask, 1] = TURQ[1]; em[mask, 2] = TURQ[2]; em[mask, 3] = 255
    os.makedirs(os.path.dirname(ep), exist_ok=True)
    Image.fromarray(em, "RGBA").save(ep)
    # brighten the teeth on the diffuse map too so they read even without EMF
    b2 = base.copy()
    b2[mask, :3] = np.clip(base[mask, :3].astype(np.float32) * 0.35 + TURQ * 0.65, 0, 255).astype(np.uint8)
    Image.fromarray(b2, "RGBA").save(bp)
    return int(mask.sum())


E = "assets/witherstormmod/textures/entity"
for base, emis in [
    (f"{E}/wither_storm/wither_storm.png", f"{E}/wither_storm/wither_storm_emissive_decal.png"),
    (f"{E}/wither_storm/main_no_tent.png", f"{E}/wither_storm/main_no_tent_emissive.png"),
    (f"{E}/wither_storm_head/wither_storm_head.png", f"{E}/wither_storm_head/wither_storm_head_emissive.png"),
    (f"{E}/wither_storm/wither_storm_invulnerable.png", f"{E}/wither_storm/wither_storm_invulnerable_emissive.png"),
]:
    n = add_teeth_glow(base, emis)
    if n:
        TEETH[os.path.basename(base)] = n
log.append(f"  * turquoise teeth burned into {len(TEETH)} textures: {TEETH}")

# also emit `_e` suffixed copies for OptiFine/ETF emissive convention
ep = os.path.join(OUT, "assets/witherstormmod/optifine/emissive.properties")
os.makedirs(os.path.dirname(ep), exist_ok=True)
open(ep, "w").write("suffix.emissive=_e\n")
for rel in [f"{E}/wither_storm/wither_storm_emissive_decal.png",
            f"{E}/wither_storm_head/wither_storm_head_emissive.png"]:
    p = os.path.join(OUT, rel)
    if os.path.exists(p):
        base_name = rel.replace("_emissive_decal.png", "_e.png").replace("_emissive.png", "_e.png")
        shutil.copy2(p, os.path.join(OUT, base_name))

# --------------------------------------------- 3. inject halos into CEM models
CEM = os.path.join(OUT, "assets/witherstormmod/optifine/cem")
HALOS = {
    "wither_storm_phase4":   ("halo_phase4",   360),
    "wither_storm_phase4_5": ("halo_phase4",   400),
    "wither_storm_phase5":   ("halo_phase5",   470),
    "wither_storm_phase52":  ("halo_phase5_1", 500),
    "wither_storm_phase5_5": ("halo_phase5_5", 620),
    "wither_storm_phase6":   ("halo_phase5_5", 760),
    "wither_storm_phase6_5": ("halo_phase5_5", 900),
    "wither_storm_phase7":   ("halo_phase5_5", 1100),
}


def halo_model(tex, size):
    """A camera-facing flat plane behind the storm carrying the halo texture."""
    h = size / 2.0
    return {
        "part": "halo", "id": f"halo_{tex}",
        "texture": f"witherstormmod:textures/entity/wither_storm/{tex}.png",
        "textureSize": [512, 512],
        "invertAxis": "xy",
        "translate": [0, 0, 0],
        "boxes": [{
            "coordinates": [-h, -h, 6, size, size, 0],
            "uvNorth": [0, 0, 512, 512],
            "uvSouth": [512, 0, 0, 512],
        }],
    }


injected = []
if os.path.isdir(CEM):
    for name, (tex, size) in HALOS.items():
        p = os.path.join(CEM, f"{name}.jem")
        if not os.path.exists(p):
            continue
        try:
            d = json.load(open(p, encoding="utf-8-sig"))
        except Exception as e:
            log.append(f"  !! {name}.jem unreadable: {e}"); continue
        d.setdefault("models", [])
        d["models"] = [m for m in d["models"] if m.get("part") != "halo"]
        d["models"].append(halo_model(tex, size))
        json.dump(d, open(p, "w"), separators=(",", ":"))
        injected.append(name)
log.append(f"  * halo geometry injected into {len(injected)} models: {injected}")

# ------------------------------------------------------- 4. skybox definitions
def fsb(ns, name, tex_prefix, fade, priority, fog):
    return {
        "schemaVersion": 2, "type": "square-textured",
        "blend": {"type": "alpha", "horizonBlend": True},
        "textures": {k: f"{ns}:textures/sky/{tex_prefix}_{k}.png"
                     for k in ("north", "south", "east", "west", "top", "bottom")},
        "properties": {
            "priority": priority,
            "fade": fade,
            "transitionInDuration": 40, "transitionOutDuration": 40,
            "changeFog": True,
            "fogColors": {"red": fog[0], "green": fog[1], "blue": fog[2], "alpha": 0.92},
            "rotation": {"static": [0, 0, 0], "axis": [0, 0, 0],
                         "rotationSpeedY": 0.35},
            "shouldRotate": True,
        },
        "conditions": {"dimensions": ["minecraft:overworld"]},
        "decorations": {"showSun": True, "showMoon": True, "showStars": True},
    }


FADES = {
    "day":    {"alwaysOn": False, "startFadeIn": 22500, "endFadeIn": 500,   "startFadeOut": 11000, "endFadeOut": 13000},
    "sunset": {"alwaysOn": False, "startFadeIn": 11500, "endFadeIn": 13200, "startFadeOut": 13800, "endFadeOut": 15000},
    "night":  {"alwaysOn": False, "startFadeIn": 13500, "endFadeIn": 15500, "startFadeOut": 22000, "endFadeOut": 23900},
}
FOG = {"day": (0.44, 0.16, 0.62), "sunset": (0.86, 0.36, 0.60), "night": (0.10, 0.03, 0.20)}

for ns, folder in (("fabricskyboxes", "sky"), ("forgeskyboxes", "sky")):
    for i, (when, fade) in enumerate(FADES.items()):
        d = os.path.join(OUT, f"assets/{ns}/{folder}")
        os.makedirs(d, exist_ok=True)
        json.dump(fsb(ns, when, "storm", fade, 30 + i, FOG[when]),
                  open(os.path.join(d, f"storm_{when}.json"), "w"), indent=2)
log.append("  * skybox JSON written for FabricSkyBoxes + ForgeSkyBoxes (rotating, fog-tinted)")

# ------------------------------------------------------------- 5. pack.mcmeta
json.dump({
    "pack": {
        "pack_format": 15,
        "supported_formats": [15, 34],
        "description": "\u00a75Ultimate MCSM Wither Storm \u00a7d\u2014 \u00a7fTAW + OG + Halos + Storm Sky",
    }
}, open(os.path.join(OUT, "pack.mcmeta"), "w"), indent=2)

# tidy junk that came out of the source packs
for junk in ("README.txt", "PACK_FIX_NOTES.txt", "credits.txt"):
    pass  # keep credits - see CREDITS.md

shutil.rmtree(work, ignore_errors=True)

print("\n".join(log))
n = sum(len(f) for _, _, f in os.walk(OUT))
print(f"\nRESOURCE PACK: {n} files")
