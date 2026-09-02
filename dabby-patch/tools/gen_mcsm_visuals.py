#!/usr/bin/env python3
"""
Generate MCSM-accurate atmosphere assets from the user's reference screenshots.

Two families:

  BACKDROPS  - big soft gradient "blobs" that hang BEHIND the Wither Storm and
               follow it. Black core (the storm's own darkness bleeding into
               the sky) fading out through the phase colour to full
               transparency at the rim so they melt into the real sky.
               These are NOT rings. Reference: user screenshots.

  SKIES      - vertical gradient strips for FabricSkyboxes (256x1024, matching
               the pack's existing day/night/sunset format), sampled from the
               Story Mode day / night / evening screenshots.

Colour stops below were measured off the screenshots in /home/user/uploads.
"""
import os
import math
import numpy as np
from PIL import Image, ImageFilter

OUT_BACKDROP = "/home/user/dabby-patch/overlay/assets/dabywitherstormmod/textures/misc"
OUT_SKY = "/home/user/dabby-patch/overlay/assets/fabricskyboxes/textures/sky"
PREVIEW = "/home/user/refimg"
for d in (OUT_BACKDROP, OUT_SKY, PREVIEW):
    os.makedirs(d, exist_ok=True)

S = 1024  # backdrop resolution


def srgb_lerp(c0, c1, t):
    """Blend in linear light so dark->saturated ramps don't go muddy."""
    a = np.array(c0, float) / 255.0
    b = np.array(c1, float) / 255.0
    a = a ** 2.2
    b = b ** 2.2
    m = a + (b - a) * t[..., None]
    return np.clip(m ** (1 / 2.2), 0, 1) * 255.0


def backdrop(stops, alpha_stops, name, grain=0.0, squash=1.0):
    """
    Radial gradient blob.
      stops       : [(radius0..1, (r,g,b)), ...] colour by normalised radius
      alpha_stops : [(radius0..1, alpha0..1), ...]
      squash      : <1 flattens vertically (wider than tall, like the refs)
    """
    y, x = np.mgrid[0:S, 0:S].astype(np.float32)
    cx = cy = (S - 1) / 2.0
    dx = (x - cx) / cx
    dy = (y - cy) / cy / squash
    r = np.sqrt(dx * dx + dy * dy)
    r = np.clip(r, 0, 1)

    rgb = np.zeros((S, S, 3), np.float32)
    for i in range(len(stops) - 1):
        r0, c0 = stops[i]
        r1, c1 = stops[i + 1]
        m = (r >= r0) & (r <= r1)
        if not m.any():
            continue
        t = (r[m] - r0) / max(1e-6, r1 - r0)
        t = t * t * (3 - 2 * t)  # smoothstep
        rgb[m] = srgb_lerp(c0, c1, t)
    rgb[r >= stops[-1][0]] = np.array(stops[-1][1], float)

    a = np.zeros((S, S), np.float32)
    for i in range(len(alpha_stops) - 1):
        r0, a0 = alpha_stops[i]
        r1, a1 = alpha_stops[i + 1]
        m = (r >= r0) & (r <= r1)
        if not m.any():
            continue
        t = (r[m] - r0) / max(1e-6, r1 - r0)
        t = t * t * (3 - 2 * t)
        a[m] = a0 + (a1 - a0) * t
    a[r >= alpha_stops[-1][0]] = alpha_stops[-1][1]
    a[r >= 1.0] = 0.0

    if grain > 0:
        rng = np.random.default_rng(7)
        n = rng.normal(0, grain, (S, S, 1)).astype(np.float32)
        rgb = np.clip(rgb + n * 255.0, 0, 255)

    img = np.dstack([rgb, np.clip(a, 0, 1) * 255.0]).astype(np.uint8)
    im = Image.fromarray(img, "RGBA").filter(ImageFilter.GaussianBlur(S / 180.0))
    p = os.path.join(OUT_BACKDROP, name)
    im.save(p)
    print("  %-34s %s" % (name, im.size))
    return im


print("BACKDROPS (behind the storm, follow it)")

# --- Phase 4.5 -> 5.0 : dark turquoise/green haze, black blur in the centre.
# Sampled from "Screenshot 2026-08-23 144558.png": #172426 #1D2D2F #243535
# #313D38 #37453E - a desaturated teal-green that lifts away from the core.
backdrop(
    [(0.00, (0, 0, 0)), (0.20, (6, 20, 21)), (0.42, (22, 52, 50)),
     (0.66, (45, 76, 68)), (0.85, (55, 88, 78)), (1.00, (60, 94, 84))],
    [(0.00, 0.98), (0.30, 0.92), (0.58, 0.70), (0.80, 0.34), (1.00, 0.0)],
    "backdrop_turquoise.png", grain=0.0, squash=0.80)

# --- Phase 5.1+ : the purple sky. Sampled from "Screenshot 2026-08-25
# 073325.png" (#312F55 -> #514672) and the closer/darker "072359.png"
# (#09060F core -> #381D52 -> #653469).
backdrop(
    [(0.00, (0, 0, 0)), (0.18, (9, 6, 15)), (0.36, (39, 18, 64)),
     (0.58, (69, 26, 94)), (0.80, (81, 70, 114)), (1.00, (92, 80, 128))],
    [(0.00, 0.99), (0.28, 0.94), (0.56, 0.74), (0.80, 0.38), (1.00, 0.0)],
    "backdrop_purple.png", grain=0.0, squash=0.82)

# --- Phase 5.5+ : purple wrapping outward into magenta/pink.
# From "145046.png" (pink horizon) + nether pinks in "145348.png"
# (#5F1148 -> #9C1A8A).
backdrop(
    [(0.00, (0, 0, 0)), (0.16, (12, 4, 20)), (0.34, (52, 16, 74)),
     (0.54, (99, 26, 122)), (0.74, (150, 34, 130)), (0.90, (196, 78, 152)),
     (1.00, (214, 110, 168))],
    [(0.00, 0.99), (0.26, 0.95), (0.54, 0.78), (0.80, 0.40), (1.00, 0.0)],
    "backdrop_purple_pink.png", grain=0.0, squash=0.84)

# --- Pure black blur: the storm's own darkness smeared across the sky.
# Used underneath every phase so the centre always reads black.
backdrop(
    [(0.00, (0, 0, 0)), (0.55, (0, 0, 0)), (1.00, (0, 0, 0))],
    [(0.00, 1.0), (0.24, 0.96), (0.50, 0.66), (0.76, 0.26), (1.00, 0.0)],
    "backdrop_black.png", squash=0.86)

# --- Orange/ember backdrop (the sunset-lit devastation shot, 145217.png:
# #925F52 -> #F19267 with a violet cap).
backdrop(
    [(0.00, (0, 0, 0)), (0.18, (26, 10, 26)), (0.38, (92, 30, 74)),
     (0.58, (156, 62, 62)), (0.78, (214, 108, 62)), (1.00, (241, 146, 103))],
    [(0.00, 0.98), (0.28, 0.92), (0.56, 0.72), (0.80, 0.36), (1.00, 0.0)],
    "backdrop_ember.png", grain=0.0, squash=0.80)


# --- Phase 4 halo: the blue radial glow the user supplied directly.
# Measured off "ChatGPT Image Sep 1 2026 03_58_20 PM.png":
#   r=0.00 #446EAE   r=0.20 #31548C   r=0.40 #142952
#   r=0.60 #040D23   r=0.85 #000001   r=1.00 black
# and the brighter companion "1787659149e708 (1).png" core #688EF5.
# Perfectly round (squash 1.0), no grain, falls to pure transparent black.
backdrop(
    [(0.00, (104, 142, 245)), (0.10, (68, 110, 174)), (0.22, (49, 84, 140)),
     (0.40, (20, 41, 82)), (0.60, (4, 13, 35)), (0.80, (1, 3, 10)),
     (1.00, (0, 0, 0))],
    [(0.00, 1.0), (0.14, 0.94), (0.34, 0.68), (0.56, 0.34), (0.78, 0.10),
     (1.00, 0.0)],
    "backdrop_phase4_blue.png", grain=0.0, squash=1.0)


# ----------------------------------------------------------------------------
# SKIES - 256x1024 vertical strips, top of image = zenith, bottom = horizon,
# matching the existing fabricskyboxes day/night/sunset textures.
# ----------------------------------------------------------------------------
def sky(stops, name):
    W, H = 256, 1024
    ys = np.linspace(0, 1, H, dtype=np.float32)
    col = np.zeros((H, 3), np.float32)
    for i in range(len(stops) - 1):
        p0, c0 = stops[i]
        p1, c1 = stops[i + 1]
        m = (ys >= p0) & (ys <= p1)
        t = (ys[m] - p0) / max(1e-6, p1 - p0)
        t = t * t * (3 - 2 * t)
        col[m] = srgb_lerp(c0, c1, t)
    col[ys >= stops[-1][0]] = np.array(stops[-1][1], float)
    img = np.repeat(col[:, None, :], W, axis=1).astype(np.uint8)
    p = os.path.join(OUT_SKY, name)
    Image.fromarray(img, "RGB").save(p)
    print("  %-34s (%d, %d)" % (name, W, H))
    return img


print("\nSKIES (Story Mode time-of-day, 256x1024)")

# DAY - the lavender Story Mode sky. Measured #867FF1 / #958BFA at zenith
# easing to #C2A5FC / #CCAAFB near the horizon (Booth 5 + farm screenshots).
sky([(0.00, (108, 100, 200)), (0.16, (134, 127, 241)), (0.38, (149, 139, 250)),
     (0.62, (170, 150, 251)), (0.82, (194, 165, 252)), (1.00, (208, 176, 253))],
    "day.png")

# NIGHT - deep indigo zenith into the vivid blue horizon band.
# Measured #10114A -> #222892 -> #394BD2 -> #4A67EC.
sky([(0.00, (13, 14, 60)), (0.18, (16, 17, 74)), (0.40, (26, 30, 120)),
     (0.62, (34, 40, 146)), (0.80, (57, 75, 210)), (0.93, (74, 103, 236)),
     (1.00, (86, 122, 244))],
    "night.png")

# EVENING - violet cap over the ember horizon (#925F52 -> #F19267) with the
# magenta band the refs show just above the terrain line.
sky([(0.00, (52, 34, 78)), (0.16, (86, 44, 96)), (0.34, (134, 62, 96)),
     (0.52, (176, 88, 88)), (0.70, (206, 118, 92)), (0.86, (231, 140, 103)),
     (1.00, (241, 150, 110))],
    "sunset.png")


# ----------------------------------------------------------------------------
# Preview sheet
# ----------------------------------------------------------------------------
names = ["backdrop_phase4_blue.png", "backdrop_black.png", "backdrop_turquoise.png",
         "backdrop_purple.png", "backdrop_purple_pink.png", "backdrop_ember.png"]
labels = ["phase 4 blue", "black blur", "phase 4.5 turquoise", "phase 5.1 purple",
          "phase 5.5 purple+pink", "ember / sunset"]
tile = 240
sheet = Image.new("RGB", (tile * len(names), tile + tile), (18, 18, 24))
for i, n in enumerate(names):
    im = Image.open(os.path.join(OUT_BACKDROP, n)).resize((tile, tile))
    # composite over a mid sky so the alpha falloff is visible
    bg = Image.new("RGBA", (tile, tile), (86, 78, 140, 255))
    bg.alpha_composite(im)
    sheet.paste(bg.convert("RGB"), (i * tile, 0))
for i, n in enumerate(["day.png", "night.png", "sunset.png"]):
    im = Image.open(os.path.join(OUT_SKY, n)).resize((tile, tile))
    sheet.paste(im, (i * tile, tile))
sheet.save(os.path.join(PREVIEW, "mcsm_atmosphere_preview.png"))
print("\npreview -> /home/user/refimg/mcsm_atmosphere_preview.png")
print("  top row:", ", ".join(labels))
print("  bottom row: day, night, sunset")
