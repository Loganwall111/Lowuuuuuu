#!/usr/bin/env python3
"""Generate all procedural art for the Ultimate MCSM Wither Storm pack.

Produces:
  * Per-phase procedural HALO textures (phase 4 / 5 / 5.1 / 5.5)
  * A seamless 6-face title-screen PANORAMA in Story Mode colours
  * A 6-face dynamic SKYBOX (magenta / purple / pink / black)
  * pack.png

Everything is generated with numpy so the horizon wraps perfectly across
the four side faces (no visible seams when the panorama spins).
"""
import os, math
import numpy as np
from PIL import Image, ImageFilter

OUT = "/home/user/UltimateWitherStorm/resourcepack"
WS = f"{OUT}/assets/witherstormmod/textures/entity/wither_storm"
PANO_WS = f"{OUT}/assets/witherstormmod/textures/gui/title/background"
PANO_MC = f"{OUT}/assets/minecraft/textures/gui/title/background"
SKY = f"{OUT}/assets/fabricskyboxes/textures/sky"
SKY_F = f"{OUT}/assets/forgeskyboxes/textures/sky"
for d in (WS, PANO_WS, PANO_MC, SKY, SKY_F):
    os.makedirs(d, exist_ok=True)

rng = np.random.default_rng(20260901)


def save(arr, path):
    Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8)).save(path)


# ----------------------------------------------------------------------------
# 1. PROCEDURAL HALOS
# ----------------------------------------------------------------------------
# Each halo is an RGBA radial field. They are designed to be layered by the
# .jem models: phase 4 = white rim, phase 5 = black blur + purple,
# phase 5.1 = blue aura, phase 5.5 = purple aura wrapped around the blue.
S = 512


def radial(size=S):
    y, x = np.mgrid[0:size, 0:size]
    cx = cy = (size - 1) / 2.0
    r = np.sqrt((x - cx) ** 2 + (y - cy) ** 2) / (size / 2.0)
    a = np.arctan2(y - cy, x - cx)
    return r, a


def turbulence(size=S, octaves=5):
    """Wrapping fractal noise used to make the halos boil instead of being flat."""
    out = np.zeros((size, size), np.float32)
    amp = 1.0
    for o in range(octaves):
        n = 2 ** (o + 2)
        g = rng.random((n, n)).astype(np.float32)
        g = np.vstack([g, g[:1]])
        g = np.hstack([g, g[:, :1]])
        img = Image.fromarray((g * 255).astype(np.uint8)).resize((size, size), Image.BICUBIC)
        out += np.asarray(img, np.float32) / 255.0 * amp
        amp *= 0.5
    out -= out.min()
    return out / max(out.max(), 1e-6)


def ring(r, inner, outer, softness):
    """Smooth annulus mask."""
    band = np.exp(-((r - (inner + outer) / 2) ** 2) / (2 * softness ** 2))
    band[r > outer + softness * 3] = 0
    return band


def make_halo(name, layers, streaks=True, boil=0.35):
    r, a = radial()
    turb = turbulence()
    rgba = np.zeros((S, S, 4), np.float32)
    for (inner, outer, soft, colour, strength) in layers:
        m = ring(r, inner, outer, soft) * strength
        m *= (1.0 - boil) + boil * turb
        if streaks:
            spikes = 0.75 + 0.25 * np.sin(a * 18 + turb * 6.0)
            m *= spikes
        c = np.array(colour, np.float32)
        rgba[..., :3] += c[None, None, :] * m[..., None]
        rgba[..., 3] += m * 255.0
    # keep the very centre clear so the storm body is never washed out
    rgba *= np.clip((r - 0.16) / 0.14, 0, 1)[..., None]
    rgba[..., 3] = np.clip(rgba[..., 3], 0, 255)
    rgba[..., :3] = np.clip(rgba[..., :3], 0, 255)
    img = Image.fromarray(rgba.astype(np.uint8), "RGBA").filter(ImageFilter.GaussianBlur(2.0))
    img.save(f"{WS}/{name}.png")
    return name


# Phase 4 - "omissive" white glow hugging the sides of the storm
make_halo("halo_phase4", [
    (0.34, 0.60, 0.085, (255, 255, 255), 0.85),
    (0.30, 0.44, 0.050, (226, 236, 255), 0.55),
    (0.55, 0.86, 0.150, (150, 170, 220), 0.22),
], boil=0.30)

# Phase 5 - black blur around the sides, bruised with purple
make_halo("halo_phase5", [
    (0.30, 0.52, 0.075, (10, 6, 16), 1.00),
    (0.44, 0.70, 0.110, (96, 26, 168), 0.62),
    (0.62, 0.92, 0.170, (48, 12, 86), 0.34),
    (0.28, 0.38, 0.040, (188, 128, 255), 0.30),
], boil=0.42)

# Phase 5.1 - the blue aura that fades in first
make_halo("halo_phase5_1", [
    (0.32, 0.56, 0.080, (64, 168, 255), 0.80),
    (0.28, 0.40, 0.045, (170, 232, 255), 0.55),
    (0.54, 0.84, 0.150, (24, 84, 190), 0.30),
], boil=0.34)

# Phase 5.5 - purple aura wrapped AROUND the blue aura, larger overall
make_halo("halo_phase5_5", [
    (0.26, 0.44, 0.060, (72, 178, 255), 0.75),   # inner blue core
    (0.40, 0.52, 0.055, (150, 214, 255), 0.45),  # blue falloff
    (0.50, 0.74, 0.105, (168, 46, 236), 0.95),   # purple wrap
    (0.70, 0.96, 0.165, (108, 20, 172), 0.50),   # outer purple bloom
    (0.86, 1.10, 0.190, (44, 6, 78), 0.28),      # dark violet edge
], boil=0.45)

# A soft additive bloom sprite reused for sun-glow / tentacle tips
r, a = radial(256)
glow = np.clip(1.0 - r, 0, 1) ** 2.4
g = np.zeros((256, 256, 4), np.float32)
g[..., 0] = 226; g[..., 1] = 150; g[..., 2] = 255
g[..., 3] = glow * 255
Image.fromarray(g.astype(np.uint8), "RGBA").save(f"{WS}/halo_bloom.png")

# ----------------------------------------------------------------------------
# 2. SHARED SKY GRADIENT  (purple top -> pink bottom, per the trailer)
# ----------------------------------------------------------------------------
TOP = np.array([26, 6, 48], np.float32)       # deep violet-black zenith
MID = np.array([104, 34, 168], np.float32)    # royal purple
LOW = np.array([206, 78, 168], np.float32)    # magenta
HORIZON = np.array([255, 150, 150], np.float32)  # pink horizon
GLOW = np.array([255, 198, 120], np.float32)     # warm sun bloom


def sky_column(h, horizon=0.58):
    """Vertical gradient: violet zenith -> magenta -> pink horizon."""
    t = np.linspace(0, 1, h, dtype=np.float32)
    col = np.zeros((h, 3), np.float32)
    for i, v in enumerate(t):
        if v < horizon * 0.45:
            k = v / max(horizon * 0.45, 1e-6)
            col[i] = TOP * (1 - k) + MID * k
        elif v < horizon:
            k = (v - horizon * 0.45) / max(horizon * 0.55, 1e-6)
            col[i] = MID * (1 - k) + LOW * k
        else:
            k = (v - horizon) / max(1 - horizon, 1e-6)
            col[i] = LOW * (1 - k) + HORIZON * k
    return col


def wrapped_noise(width, height, scale, seed_off=0):
    """Noise that tiles horizontally - essential so the panorama has no seam."""
    r2 = np.random.default_rng(1234 + seed_off)
    g = r2.random((height, scale)).astype(np.float32)
    g = np.hstack([g, g[:, :1]])
    img = Image.fromarray((g * 255).astype(np.uint8)).resize((width + 1, height), Image.BICUBIC)
    return np.asarray(img, np.float32)[:, :width] / 255.0


def skyline(width, base, amp, scale, seed_off):
    """A wrapping silhouette height profile."""
    prof = np.zeros(width, np.float32)
    amp_i, sc = amp, scale
    for o in range(4):
        prof += (wrapped_noise(width, 1, sc, seed_off + o)[0] - 0.5) * amp_i
        amp_i *= 0.5; sc *= 2
    return base + prof


# ----------------------------------------------------------------------------
# 3. PANORAMA  (6 faces, 1024px, seamless across 0..3)
# ----------------------------------------------------------------------------
P = 1024
STRIP = P * 4  # the four side faces unrolled

grad = sky_column(P, horizon=0.60)
strip = np.repeat(grad[:, None, :], STRIP, axis=1)

# sun bloom low on the horizon (face 2-ish) -> gives the warm pink/orange pocket
yy, xx = np.mgrid[0:P, 0:STRIP]
sun_x, sun_y = STRIP * 0.62, P * 0.585
d = np.sqrt(((xx - sun_x) / (STRIP * 0.16)) ** 2 + ((yy - sun_y) / (P * 0.30)) ** 2)
bloom = np.clip(1 - d, 0, 1) ** 2.0
strip += GLOW[None, None, :] * bloom[..., None] * 0.85

# cloud banding
cl = wrapped_noise(STRIP, P, 96, 7)
band = np.clip((cl - 0.52) * 3.2, 0, 1) * np.clip(1 - abs(np.linspace(-1, 1, P))[:, None] * 1.4, 0, 1)
strip += (np.array([255, 190, 235], np.float32)[None, None, :] - strip) * (band[..., None] * 0.30)

# --- storm is stamped BEFORE the terrain so the ground occludes its tentacles
r3 = np.random.default_rng(99)

STORM_CX, STORM_CY, STORM_S = int(STRIP * 0.13), int(P * 0.30), 205

# purple god-rays fanning out from the storm (behind everything)
for i in range(18):
    a0 = -math.pi * 0.5 + (i - 9) * 0.068
    for s_i in range(900):
        px = int(STORM_CX + math.cos(a0) * s_i * 1.6)
        py = int(STORM_CY + math.sin(a0) * s_i * 1.1)
        w = int(2 + s_i * 0.03)
        if 0 <= py < P - w and 0 <= px < STRIP - w:
            strip[py:py + w, px:px + w] += np.array([120, 40, 190], np.float32) * 0.05

def blob(canvas, cx, cy, rx, ry, colour, n=1400, power=0.5):
    """Soft cloud of blocky chunks - the Wither Storm's mass."""
    H, W = canvas.shape[:2]
    for _ in range(n):
        ang = r3.random() * math.pi * 2
        rad = r3.random() ** power
        px = int(cx + math.cos(ang) * rad * rx)
        py = int(cy + math.sin(ang) * rad * ry)
        s = int(max(3, 16 * (1 - rad)))
        if 0 <= py < H - s and 0 <= px < W - s:
            canvas[py:py + s, px:px + s] = colour


def head(canvas, hx, hy, hw, colour):
    """A Wither skull with turquoise teeth + magenta eyes."""
    H, W = canvas.shape[:2]
    if not (0 <= hy < H - hw and 0 <= hx < W - hw):
        return
    canvas[hy:hy + hw, hx:hx + hw] = colour
    # jaw slightly wider
    jy = hy + int(hw * 0.66)
    canvas[jy:jy + int(hw * 0.34), hx - int(hw * 0.07):hx + int(hw * 1.07)] = colour
    # turquoise glowing teeth
    ty = hy + int(hw * 0.60); th = max(2, int(hw * 0.10))
    for i in range(5):
        tx = hx + int(hw * (0.12 + i * 0.17))
        canvas[ty:ty + th, tx:tx + max(2, int(hw * 0.10))] = np.array([64, 240, 224], np.float32)
    # magenta eyes
    ey = hy + int(hw * 0.30)
    for ex in (hx + int(hw * 0.16), hx + int(hw * 0.60)):
        canvas[ey:ey + max(2, int(hw * 0.13)), ex:ex + max(2, int(hw * 0.20))] = np.array([255, 60, 235], np.float32)


BODY = np.array([13, 7, 22], np.float32)
# main mass
blob(strip, STORM_CX, STORM_CY, STORM_S * 1.7, STORM_S * 1.05, BODY, n=3000, power=0.55)
# the three heads, sunk INTO the mass so they read as attached
for hx_f, hy_f, hs in ((-0.78, 0.06, 0.30), (0.02, -0.20, 0.40), (0.80, 0.10, 0.29)):
    hw = int(STORM_S * hs)
    head(strip, int(STORM_CX + hx_f * STORM_S * 1.25) - hw // 2,
         int(STORM_CY + hy_f * STORM_S) - hw // 2, hw, BODY)
    # collar of mass welding each head to the body
    blob(strip, int(STORM_CX + hx_f * STORM_S * 1.25), int(STORM_CY + hy_f * STORM_S + hw * 0.30),
         hw * 0.85, hw * 0.55, BODY, n=320, power=0.6)
# tentacles curling out and DOWN, tapering
for t in range(11):
    ang0 = math.pi * (0.06 + 0.88 * t / 10.0)
    L = STORM_S * (1.4 + r3.random() * 1.5)
    px = STORM_CX + math.cos(ang0) * STORM_S * 1.25
    py = STORM_CY + STORM_S * 0.35
    vx, vy = math.cos(ang0) * 1.9, 0.55
    for s_i in range(int(L)):
        vx += (r3.random() - 0.5) * 0.16
        vy += 0.022
        px += vx; py += vy
        th = int(max(2, STORM_S * 0.075 * (1 - s_i / L)))
        ix, iy = int(px), int(py)
        if 0 <= iy < P - th and 0 <= ix < STRIP - th:
            strip[iy:iy + th, ix:ix + th] = BODY

# distant hills + tainted skyline
hz = int(P * 0.60)
far = skyline(STRIP, hz - 26, 30, 8, 11)
near = skyline(STRIP, hz + 6, 46, 14, 23)
col_far = np.array([58, 26, 84], np.float32)
col_near = np.array([22, 10, 34], np.float32)
for x in range(STRIP):
    strip[int(far[x]):, x] = col_far
    strip[int(near[x]):, x] = col_near

# blocky Story-Mode style buildings along the near ridge

x = 0
while x < STRIP:
    w = int(r3.integers(26, 90)); h = int(r3.integers(30, 150))
    if r3.random() < 0.55:
        top = int(near[x % STRIP]) - h
        strip[max(top, 0):int(near[x % STRIP]), x:min(x + w, STRIP)] = np.array([14, 7, 24], np.float32)
        # a few lit windows
        for _ in range(int(r3.integers(0, 5))):
            wy = int(r3.integers(max(top, 0) + 4, max(top + 8, int(near[x % STRIP]) - 4)))
            wx = int(r3.integers(x + 3, x + max(w - 4, 4)))
            if wy + 3 < P and wx + 3 < STRIP:
                strip[wy:wy + 3, wx:wx + 3] = np.array([255, 196, 96], np.float32)
    x += w + int(r3.integers(10, 60))

strip = np.clip(strip, 0, 255)

# slice into the four side faces  (MC order: 0=N 1=E 2=S 3=W)
for i in range(4):
    face = strip[:, i * P:(i + 1) * P]
    save(face, f"{PANO_WS}/panorama_{i}.png")

# face 4 = up  (violet void + stars)
up = np.zeros((P, P, 3), np.float32)
yy, xx = np.mgrid[0:P, 0:P]
rr = np.sqrt(((xx - P / 2) / (P / 2)) ** 2 + ((yy - P / 2) / (P / 2)) ** 2)
up[:] = TOP[None, None, :] + (MID - TOP)[None, None, :] * np.clip(rr, 0, 1)[..., None] * 0.85
for _ in range(900):
    sx, sy = int(rng.integers(0, P)), int(rng.integers(0, P))
    b = rng.random() * 205 + 50
    up[sy:sy + 2, sx:sx + 2] = np.array([b, b * 0.86, b], np.float32)
save(up, f"{PANO_WS}/panorama_4.png")

# face 5 = down (dark tainted ground)
dn = np.zeros((P, P, 3), np.float32)
gn = wrapped_noise(P, P, 64, 31)
dn[:] = np.array([16, 8, 24], np.float32)[None, None, :] + gn[..., None] * 26
save(dn, f"{PANO_WS}/panorama_5.png")

# mirror the panorama into vanilla's namespace so it also replaces the main menu
for i in range(6):
    Image.open(f"{PANO_WS}/panorama_{i}.png").save(f"{PANO_MC}/panorama_{i}.png")

# ----------------------------------------------------------------------------
# 4. DYNAMIC SKYBOX  (magenta / purple / pink / dark black)
# ----------------------------------------------------------------------------
K = 1024


def sky_face(kind):
    img = np.zeros((K, K, 3), np.float32)
    if kind in ("north", "south", "east", "west"):
        g = sky_column(K, horizon=0.62)
        img[:] = np.repeat(g[:, None, :], K, axis=1)
        n = wrapped_noise(K, K, 72, {"north": 1, "south": 2, "east": 3, "west": 4}[kind])
        veil = np.clip((n - 0.48) * 2.6, 0, 1)
        img += (np.array([255, 120, 220], np.float32)[None, None, :] - img) * (veil[..., None] * 0.26)
        fade = np.clip(np.linspace(0, 1, K) * 1.5, 0, 1)[:, None, None]
        img = img * (0.35 + 0.65 * fade)
    elif kind == "top":
        yy, xx = np.mgrid[0:K, 0:K]
        rr = np.clip(np.sqrt(((xx - K / 2) / (K / 2)) ** 2 + ((yy - K / 2) / (K / 2)) ** 2), 0, 1)
        img[:] = np.array([8, 2, 16], np.float32)[None, None, :] * (1 - rr[..., None]) + \
                 np.array([88, 24, 150], np.float32)[None, None, :] * rr[..., None]
        for _ in range(700):
            sx, sy = int(rng.integers(0, K)), int(rng.integers(0, K))
            b = rng.random() * 200 + 55
            img[sy:sy + 2, sx:sx + 2] = np.array([b, b * 0.8, b], np.float32)
    else:  # bottom -> the pink underside the trailer shows
        g = np.linspace(0, 1, K, dtype=np.float32)[:, None, None]
        img[:] = np.array([232, 96, 168], np.float32)[None, None, :] * (1 - g) + \
                 np.array([255, 176, 150], np.float32)[None, None, :] * g
        n = wrapped_noise(K, K, 48, 9)
        img += (n[..., None] - 0.5) * 26
    return np.clip(img, 0, 255)


for kind in ("north", "south", "east", "west", "top", "bottom"):
    a = sky_face(kind)
    save(a, f"{SKY}/storm_{kind}.png")
    save(a, f"{SKY_F}/storm_{kind}.png")

# pack.png - use the storm face of the panorama as the pack icon
ic = Image.open(f"{PANO_WS}/panorama_0.png").resize((128, 128), Image.LANCZOS)
ic.save(f"{OUT}/pack.png")

print("visuals: halos + panorama(6) + skybox(6) + pack.png OK")
