#!/usr/bin/env python3
"""
Generate the Wither Storm command block textures.

The shipped `super_command_block.png` and `command_core_block.png` were the
purple/orange MISSING TEXTURE checkerboard -- the real art was never in the
repo, which is why the block looked wrong in game.

Reference: uploads/Screenshot 2026-08-23 144829.png (Story Mode command block).
Sampled medians from that frame, then brightness-corrected back to unlit
values (Minecraft shades the top face ~1.0, sides ~0.8):

    outer rust    (105, 64, 44)  lit  -> ~(131, 80, 55) unlit
    inner panel   (131,109, 95)  lit  -> ~(164,136,119) unlit
    dot highlight (223,192,166)

Every face uses the same texture (`cube_all`), so the 3x3 dot grid appears on
all six sides, as the user asked.
"""

import os
import numpy as np
from PIL import Image

REPO = "/var/tmp/build/dabsrc/src/main/resources/assets/dabywitherstormmod/textures/block"

# unlit base colours
RUST_DARK = (96, 56, 38)
RUST      = (131, 80, 55)
RUST_LITE = (156, 100, 70)
PANEL     = (164, 136, 119)
PANEL_DK  = (132, 106, 92)
FRAME     = (110, 68, 46)

# the little indicator lights
DOT_RED   = (196, 74, 74)
DOT_PINK  = (214, 122, 128)
DOT_OLIVE = (150, 156, 82)
DOT_WARM  = (216, 158, 96)


def noise_field(rng, size, base, spread):
    """Blocky per-pixel colour variation so the metal reads as pixel art."""
    img = np.zeros((size, size, 3), np.float32)
    img[:, :] = base
    n = rng.integers(-spread, spread + 1, (size, size, 1))
    return np.clip(img + n, 0, 255)


def build(seed, variant):
    size = 16
    rng = np.random.default_rng(seed)

    a = noise_field(rng, size, RUST, 14)

    # scattered darker/lighter flecks for the weathered copper look
    for _ in range(26):
        x, y = rng.integers(0, size, 2)
        a[y, x] = RUST_DARK if rng.random() < 0.55 else RUST_LITE

    # inset panel: 10x10 centred, with a 1px frame
    p0, p1 = 3, 13
    a[p0:p1, p0:p1] = noise_field(rng, p1 - p0, PANEL, 8)
    # frame edge
    a[p0 - 1, p0 - 1:p1 + 1] = FRAME
    a[p1, p0 - 1:p1 + 1] = FRAME
    a[p0 - 1:p1 + 1, p0 - 1] = FRAME
    a[p0 - 1:p1 + 1, p1] = FRAME
    # subtle shading inside the panel
    a[p1 - 1, p0:p1] = PANEL_DK
    a[p0:p1, p1 - 1] = PANEL_DK

    # 3x3 grid of indicator dots, evenly spaced inside the panel
    if variant == "super":
        palette = [DOT_RED, DOT_PINK, DOT_OLIVE,
                   DOT_PINK, DOT_WARM, DOT_RED,
                   DOT_RED, DOT_OLIVE, DOT_PINK]
    else:
        palette = [DOT_PINK, DOT_RED, DOT_PINK,
                   DOT_RED, DOT_OLIVE, DOT_RED,
                   DOT_PINK, DOT_RED, DOT_WARM]

    xs = [4, 7, 10]
    ys = [4, 7, 10]
    i = 0
    for gy in ys:
        for gx in xs:
            col = palette[i]
            i += 1
            a[gy, gx] = col
            a[gy + 1, gx] = tuple(int(c * 0.72) for c in col)   # 2px tall dot
    return Image.fromarray(a.astype(np.uint8), "RGB").convert("RGBA")


def main():
    if not os.path.isdir(REPO):
        raise SystemExit("texture dir not found: " + REPO)
    for name, seed, variant in (
        ("super_command_block.png", 20260902, "super"),
        ("command_core_block.png", 77315, "core"),
    ):
        img = build(seed, variant)
        out = os.path.join(REPO, name)
        img.save(out)
        print(f"  wrote {name} {img.size}")
    print("command block textures done")


if __name__ == "__main__":
    main()
