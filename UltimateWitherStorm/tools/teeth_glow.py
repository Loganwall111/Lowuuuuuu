#!/usr/bin/env python3
"""Burn a turquoise emissive glow onto the Wither Storm's TEETH.

Rather than guessing by colour (which wrongly caught the command-block art),
this reads the real CEM models, finds every box belonging to a teeth/fang
part, and converts its `textureOffset` + box size into the exact UV rectangle
that box occupies on the 160x160 atlas. Only those pixels are recoloured.
"""
import os, json, glob, re, math
import numpy as np
from PIL import Image, ImageFilter

PACK = "/home/user/UltimateWitherStorm/resourcepack"
CEM = f"{PACK}/assets/witherstormmod/optifine/cem"
E = f"{PACK}/assets/witherstormmod/textures/entity"

TURQ = np.array([64, 240, 224], np.float32)     # core turquoise
TURQ_HOT = np.array([186, 255, 248], np.float32)  # hot centre

TEETH_RE = re.compile(r"teeth|tooth|fang", re.I)


def box_uv_rect(off, size):
    """Vanilla/OptiFine cube unwrap -> bounding rect on the atlas."""
    u, v = float(off[0]), float(off[1])
    w, h, d = (float(size[3]), float(size[4]), float(size[5]))
    return (u, v, u + 2 * d + 2 * w, v + d + h)


def collect(model, out, inherited=None):
    nid = str(model.get("id") or model.get("part") or "")
    is_teeth = bool(TEETH_RE.search(nid)) or bool(inherited)
    for b in (model.get("boxes") or []):
        if not is_teeth:
            continue
        off = b.get("textureOffset")
        co = b.get("coordinates")
        if off and co and len(co) >= 6:
            out.append(box_uv_rect(off, co))
        # explicit per-face UVs
        for key in ("uvNorth", "uvSouth", "uvEast", "uvWest", "uvUp", "uvDown"):
            q = b.get(key)
            if q and len(q) == 4:
                x0, y0, x1, y1 = q
                out.append((min(x0, x1), min(y0, y1), max(x0, x1), max(y0, y1)))
    for s in (model.get("submodels") or []):
        collect(s, out, inherited=is_teeth)


rects, sizes = [], set()
for f in sorted(glob.glob(f"{CEM}/*.jem")):
    try:
        d = json.load(open(f, encoding="utf-8-sig"))
    except Exception:
        continue
    ts = d.get("textureSize") or [160, 160]
    sizes.add(tuple(ts))
    got = []
    for m in d.get("models", []):
        collect(m, got)
    for r in got:
        rects.append((r, tuple(ts)))

print(f"teeth boxes found: {len(rects)}  atlas sizes seen: {sizes}")


def build_mask(W, H):
    m = np.zeros((H, W), bool)
    for (x0, y0, x1, y1), (tw, th) in rects:
        sx, sy = W / float(tw), H / float(th)
        a, b = int(math.floor(x0 * sx)), int(math.floor(y0 * sy))
        c, d2 = int(math.ceil(x1 * sx)), int(math.ceil(y1 * sy))
        a, b = max(a, 0), max(b, 0)
        c, d2 = min(c, W), min(d2, H)
        if c > a and d2 > b:
            m[b:d2, a:c] = True
    return m


def apply(base_rel, emis_rel):
    bp = f"{PACK}/{base_rel}"
    if not os.path.exists(bp):
        return None
    base = np.asarray(Image.open(bp).convert("RGBA")).copy()
    H, W = base.shape[:2]
    mask = build_mask(W, H)
    # only touch pixels that are actually opaque tooth material
    mask &= base[..., 3] > 20
    if mask.sum() == 0:
        return None

    # ---- emissive map: turquoise where the teeth are
    ep = f"{PACK}/{emis_rel}"
    if os.path.exists(ep):
        em = np.asarray(Image.open(ep).convert("RGBA")).copy()
        if em.shape[:2] != (H, W):
            em = np.zeros((H, W, 4), np.uint8)
    else:
        em = np.zeros((H, W, 4), np.uint8)

    glow = np.zeros((H, W), np.float32)
    glow[mask] = 1.0
    soft = np.asarray(Image.fromarray((glow * 255).astype(np.uint8)).filter(
        ImageFilter.GaussianBlur(1.2)), np.float32) / 255.0

    col = TURQ[None, None, :] * (1 - soft[..., None] * 0.35) + TURQ_HOT[None, None, :] * (soft[..., None] * 0.35)
    em_f = em.astype(np.float32)
    sel = soft > 0.04
    em_f[sel, :3] = col[sel]
    em_f[sel, 3] = np.maximum(em_f[sel, 3], np.clip(soft[sel] * 255 * 1.15, 0, 255))
    Image.fromarray(np.clip(em_f, 0, 255).astype(np.uint8), "RGBA").save(ep)

    # ---- diffuse: tint the teeth turquoise so they read without a shader too
    b2 = base.astype(np.float32)
    b2[mask, :3] = np.clip(b2[mask, :3] * 0.28 + TURQ[None, :] * 0.72, 0, 255)
    Image.fromarray(np.clip(b2, 0, 255).astype(np.uint8), "RGBA").save(bp)
    return int(mask.sum())


TARGETS = [
    (f"assets/witherstormmod/textures/entity/wither_storm/wither_storm.png",
     f"assets/witherstormmod/textures/entity/wither_storm/wither_storm_emissive_decal.png"),
    (f"assets/witherstormmod/textures/entity/wither_storm/main_no_tent.png",
     f"assets/witherstormmod/textures/entity/wither_storm/main_no_tent_emissive.png"),
    (f"assets/witherstormmod/textures/entity/wither_storm_head/wither_storm_head.png",
     f"assets/witherstormmod/textures/entity/wither_storm_head/wither_storm_head_emissive.png"),
    (f"assets/witherstormmod/textures/entity/wither_storm/wither_storm_invulnerable.png",
     f"assets/witherstormmod/textures/entity/wither_storm/wither_storm_invulnerable_emissive.png"),
    (f"assets/witherstormmod/textures/entity/wither_storm/wither_storm_exploding.png",
     f"assets/witherstormmod/textures/entity/wither_storm/wither_storm_exploding_emissive.png"),
]

for b, e in TARGETS:
    n = apply(b, e)
    if n:
        print(f"  teeth glow -> {os.path.basename(b)}: {n} px")

# OptiFine `_e` convention duplicates
for rel in [f"assets/witherstormmod/textures/entity/wither_storm/wither_storm_emissive_decal.png",
            f"assets/witherstormmod/textures/entity/wither_storm_head/wither_storm_head_emissive.png"]:
    p = f"{PACK}/{rel}"
    if os.path.exists(p):
        Image.open(p).save(p.replace("_emissive_decal.png", "_e.png").replace("_emissive.png", "_e.png"))
print("teeth glow done")
