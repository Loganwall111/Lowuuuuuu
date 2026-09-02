#!/usr/bin/env python3
"""Burn the turquoise teeth glow into Dabicco's Wither Storm textures.

Dabicco's mod uses a compact 64x96 Wither-style atlas (three heads: two on the
top row, one at bottom-left) rather than Cracker's 160x160 layout, plus an `_e`
emissive convention already used by `phase_4_assets_e.png`.

Teeth on the Wither skull atlas sit in the lower rows of each face. Rather than
hardcode pixel rectangles, this locates the pale mouth/jaw pixels *inside each
head's face region* so it can never bleed onto the command-block artwork the way
a naive colour scan would.
"""
import os
import numpy as np
from PIL import Image, ImageFilter

REPO = "/home/user/dabby/src/main/resources/assets/dabywitherstormmod/textures/entity"

TURQ = np.array([64, 240, 224], np.float32)
TURQ_HOT = np.array([190, 255, 250], np.float32)

# Wither skull atlas: each head is a 64x32-style block. On the 64x96 sheet the
# three faces occupy these boxes (x0, y0, x1, y1) - derived by inspecting the
# texture: two heads across the top band, the third on the lower-left band.
# Each head's FACE region on the 64x96 sheet. Within a face the eyes sit above
# the mouth, so the mouth is found automatically as the lowest pale band - that
# way it works for all three heads even though they sit at different offsets.
HEAD_BOXES = [
    (0, 0, 32, 22),     # main head, top-left face
    (32, 0, 64, 22),    # side head, top-right face
    (0, 36, 32, 52),    # third head, lower-left face (jaw ends at y=52; below is ribcage)
]


def pale_mask(a):
    """Pale / lavender mouth pixels: brighter than the near-black body."""
    r = a[..., 0].astype(int); g = a[..., 1].astype(int)
    b = a[..., 2].astype(int); al = a[..., 3].astype(int)
    lum = (r + g + b) / 3.0
    return (lum > 95) & (al > 40)


def process(fname, out_emissive=None):
    p = os.path.join(REPO, fname)
    if not os.path.exists(p):
        return None
    a = np.asarray(Image.open(p).convert("RGBA")).copy()
    H, W = a.shape[:2]

    pale = pale_mask(a)
    mask = np.zeros((H, W), bool)
    for (x0, y0, x1, y1) in HEAD_BOXES:
        if y1 > H or x1 > W:
            continue
        sub = pale[y0:y1, x0:x1]
        rows = np.nonzero(sub.any(axis=1))[0]
        if rows.size == 0:
            continue
        # split the pale rows into contiguous bands; eyes are the upper band(s),
        # the mouth/teeth are always the LOWEST band on a Wither skull face
        bands, cur = [], [rows[0]]
        for r in rows[1:]:
            if r == cur[-1] + 1:
                cur.append(r)
            else:
                bands.append(cur); cur = [r]
        bands.append(cur)
        # teeth are a thin strip: never take more than 2 rows, and always the
        # bottom-most ones. Stops ribcage / body detail being mistaken for a jaw.
        mouth = bands[-1][-2:]
        for r in mouth:
            mask[y0 + r, x0:x1] |= sub[r]
    if mask.sum() == 0:
        return None

    # emissive map
    ep = os.path.join(REPO, out_emissive or fname.replace(".png", "_e.png"))
    if os.path.exists(ep):
        em = np.asarray(Image.open(ep).convert("RGBA")).copy()
        if em.shape[:2] != (H, W):
            em = np.zeros((H, W, 4), np.uint8)
    else:
        em = np.zeros((H, W, 4), np.uint8)

    glow = np.zeros((H, W), np.float32)
    glow[mask] = 1.0
    soft = np.asarray(Image.fromarray((glow * 255).astype(np.uint8))
                      .filter(ImageFilter.GaussianBlur(0.8)), np.float32) / 255.0

    sel = soft > 0.05
    col = TURQ[None, None, :] * (1 - soft[..., None] * 0.4) + TURQ_HOT[None, None, :] * (soft[..., None] * 0.4)
    emf = em.astype(np.float32)
    emf[sel, :3] = col[sel]
    emf[sel, 3] = np.maximum(emf[sel, 3], np.clip(soft[sel] * 255, 0, 255))
    Image.fromarray(np.clip(emf, 0, 255).astype(np.uint8), "RGBA").save(ep)

    # tint the diffuse so the teeth read even with shaders off
    af = a.astype(np.float32)
    af[mask, :3] = np.clip(af[mask, :3] * 0.30 + TURQ[None, :] * 0.70, 0, 255)
    Image.fromarray(np.clip(af, 0, 255).astype(np.uint8), "RGBA").save(p)
    return int(mask.sum()), ep


if __name__ == "__main__":
    for f in ("wither_storm.png", "wither_storm_og.png"):
        r = process(f)
        if r:
            n, ep = r
            print(f"  {f}: {n} teeth px -> {os.path.basename(ep)}")
    print("daby teeth glow done")
