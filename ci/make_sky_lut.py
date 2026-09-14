#!/usr/bin/env python3
"""make_sky_lut.py — Build #390 Phase 3: bind the gradient sheets to the sky pass.

WHY A BAKE INSTEAD OF A SAMPLER
-------------------------------
The obvious way to "bind a texture to sky.fsh" would be `uniform sampler2D`.
That is not allowed here: the sky pass runs on Minecraft's own `position`
pipeline, whose bind-group layout has no spare sampler slot. PC/Vulkan validates
that layout, so declaring a sampler the pipeline does not bind is a hard crash on
Vulkan (it is the exact trap the storm_glow.fsh header documents). It would also
break the Story Look resource-pack copy of position.fsh, which is a standalone
file with no include of ours.

So the sheets are still the authored source of truth (drop your own
`image_*.png` in and re-run this script) but they are BAKED into GLSL constant
tables that the sky pass LERPs between. Six stops per row reproduce the linear
sheets exactly; eight stops track arbitrary user art closely. Re-running is
deterministic: same PNGs in, same tables out, reviewable in a diff.

WHAT IT WRITES
--------------
A generated block between the markers

    // >>> MCSM_SKY_SHEETS:BEGIN
    // <<< MCSM_SKY_SHEETS:END

in three places:

  mcsm-core-shaders/include/mcsm_visuals.glsl                  (jar path)
  overrides/resourcepacks/01_Devouring_Storms_Story_Look/.../position.fsh
  overrides/global_packs/required_resources/01_.../position.fsh  (profile path)

Storm table  (8 rows): 4.45 green glare, 5.00 TEAL, 5.20 violet, 5.50 PURPLE,
                       5.90 pink-lavender, 6.00 SALMON, 7.00 dark red, 8.00 near-black.
Vanilla table (4 rows): day BLUE, night LAVENDER, dusk SUNSET SPLIT, void spare.

Usage:
    python3 ci/make_sky_lut.py             # bake from the shipped/replaced sheets
    python3 ci/make_sky_lut.py --check     # fail if the tables are stale
"""
import argparse
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, os.path.join(ROOT, "glslcheck"))
from pngtools import read_png  # noqa: E402  (repo-local dependency-free PNG codec)

BEGIN = "// >>> MCSM_SKY_SHEETS:BEGIN"
END = "// <<< MCSM_SKY_SHEETS:END"
SHEET_W = 8          # stops baked per row
SRC = "src/main/resources/assets/dabywitherstormmod/textures"

STORM_SHEET_SRC = SRC + "/mcsm_atmosphere/sky"
VANILLA_SHEET_SRC = SRC + "/sky"

# Rows whose art IS a shipped sheet file. Everything else in the table is a
# derived row (a phase of the timeline with no dedicated upload yet) and is
# declared here as an explicit stop table.
STORM_ROWS = [
    ("4.45 green glare", None, [(0.00, (0.012, 0.020, 0.016)), (0.35, (0.030, 0.090, 0.062)),
                                (0.70, (0.070, 0.200, 0.130)), (1.00, (0.120, 0.300, 0.180))]),
    ("5.00 TEAL", STORM_SHEET_SRC + "/phase5_teal.png", None),
    ("5.20 violet", None, [(0.00, (0.030, 0.012, 0.058)), (0.35, (0.090, 0.035, 0.130)),
                           (0.70, (0.180, 0.075, 0.220)), (1.00, (0.280, 0.130, 0.300))]),
    ("5.50 PURPLE", STORM_SHEET_SRC + "/phase55.png", None),
    ("5.90 pink-lavender", None, [(0.00, (0.070, 0.030, 0.120)), (0.35, (0.180, 0.085, 0.230)),
                                  (0.70, (0.360, 0.190, 0.360)), (1.00, (0.620, 0.380, 0.500))]),
    ("6.00 SALMON", STORM_SHEET_SRC + "/phase6.png", None),
    ("7.00 dark red", None, [(0.00, (0.018, 0.002, 0.009)), (0.35, (0.092, 0.009, 0.023)),
                             (0.70, (0.212, 0.023, 0.032)), (1.00, (0.320, 0.070, 0.045))]),
    ("8.00 near-black", None, [(0.00, (0.006, 0.002, 0.008)), (0.35, (0.030, 0.008, 0.020)),
                               (0.70, (0.080, 0.014, 0.030)), (1.00, (0.140, 0.030, 0.040))]),
]

VANILLA_ROWS = [
    ("day PERIWINKLE", VANILLA_SHEET_SRC + "/day.png", None),
    ("night MIDNIGHT BLUE", VANILLA_SHEET_SRC + "/night.png", None),
    ("dusk SUNSET SPLIT", VANILLA_SHEET_SRC + "/sunset.png", None),
    ("void spare", None, [(0.00, (0.010, 0.008, 0.022)), (0.35, (0.030, 0.024, 0.060)),
                          (0.70, (0.070, 0.055, 0.120)), (1.00, (0.130, 0.100, 0.200))]),
]

TARGETS = [
    ("mcsm-core-shaders/include/mcsm_visuals.glsl", "       "),
    ("overrides/resourcepacks/01_Devouring_Storms_Story_Look/assets/minecraft/shaders/core/position.fsh", " "),
    ("overrides/global_packs/required_resources/01_Devouring_Storms_Story_Look/assets/minecraft/shaders/core/position.fsh", " "),
]


def rows_to_stops(rows, count):
    """Rows are stored ZENITH (v=0) first -- the same order as mcsm_sky6's
    arguments. `count` stops are sampled per row; index 0 = zenith."""
    out = []
    for name, path, table in rows:
        if path:
            p = os.path.join(ROOT, path)
            w, h, px = read_png(p)
            stops = []
            for i in range(count):
                t = i / float(count - 1)
                y = min(h - 1, int(round(t * (h - 1))))
                o = (y * w) * 4
                stops.append((px[o] / 255.0, px[o + 1] / 255.0, px[o + 2] / 255.0))
            out.append((name, stops))
        else:
            stops = []
            for i in range(count):
                t = i / float(count - 1)
                stops.append(sample_table(table, t))
            out.append((name, stops))
    return out


def sample_table(table, t):
    if t <= table[0][0]:
        return table[0][1]
    for (t0, c0), (t1, c1) in zip(table, table[1:]):
        if t <= t1:
            u = 0.0 if t1 <= t0 else (t - t0) / (t1 - t0)
            return tuple(a + (b - a) * u for a, b in zip(c0, c1))
    return table[-1][1]


def emit_array(name, rows, count):
    lines = ["const vec3 %s[%d] = vec3[%d](" % (name, len(rows) * count, len(rows) * count)]
    flat = []
    for _, stops in rows:
        flat.extend(stops)
    for i, c in enumerate(flat):
        tail = "," if i < len(flat) - 1 else ");"
        lines.append("    vec3(%.4f, %.4f, %.4f)%s" % (c[0], c[1], c[2], tail))
    return "\n".join(lines)


FUNCS = """
// Self-contained on purpose: this block is also baked into the standalone Story
// Look `position.fsh`, which has no include of mcsm_visuals.glsl.
float mcsm_sheet_ramp(float v, float lo, float hi) {
    if (hi <= lo) return v >= hi ? 1.0 : 0.0;
    float t = clamp((v - lo) / (hi - lo), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

// ---- baked sheet tables, %(storm_rows)d storm rows / %(vanilla_rows)d vanilla rows,
// %(cols)d stops per row (index 0 = zenith, last = horizon). -----------------
#define MCSM_SHEET_COLS %(cols)d

vec3 mcsm_sheet_grad(const vec3 c[MCSM_SHEET_COLS], float up) {
    // up: 0 horizon .. 1 zenith (the same convention the sky arrays use), so the
    // table is read from the zenith end downwards.
    float t = clamp(1.0 - up, 0.0, 1.0) * float(MCSM_SHEET_COLS - 1);
    int i = int(floor(t));
    float f = t - floor(t);
    if (i >= MCSM_SHEET_COLS - 1) return c[MCSM_SHEET_COLS - 1];
    return mix(c[i], c[i + 1], f);
}

vec3 mcsm_sheet_row_storm(int row, float up) {
    vec3 c[MCSM_SHEET_COLS];
    for (int i = 0; i < MCSM_SHEET_COLS; i++) c[i] = MCSM_SHEET_STORM[row * MCSM_SHEET_COLS + i];
    return mcsm_sheet_grad(c, up);
}

vec3 mcsm_sheet_row_vanilla(int row, float up) {
    vec3 c[MCSM_SHEET_COLS];
    for (int i = 0; i < MCSM_SHEET_COLS; i++) c[i] = MCSM_SHEET_VANILLA[row * MCSM_SHEET_COLS + i];
    return mcsm_sheet_grad(c, up);
}

// Phase -> fractional row in the storm table. The knots are the phases the
// sheets are keyed to, so crossing a knot IS the cross-fade.
float mcsm_sheet_storm_row(float p) {
    if (p < 5.00) return 0.0 + mcsm_sheet_ramp(p, 4.45, 5.00);
    if (p < 5.20) return 1.0 + mcsm_sheet_ramp(p, 5.00, 5.20);
    if (p < 5.50) return 2.0 + mcsm_sheet_ramp(p, 5.20, 5.50);
    if (p < 5.90) return 3.0 + mcsm_sheet_ramp(p, 5.50, 5.90);
    if (p < 6.00) return 4.0 + mcsm_sheet_ramp(p, 5.90, 6.00);
    if (p < 7.00) return 5.0 + mcsm_sheet_ramp(p, 6.00, 7.00);
    return 6.0 + mcsm_sheet_ramp(p, 7.00, 8.00);
}

// The storm backdrop the build is specified around: teal at phase 5, purple
// through 5.5-5.9, salmon from 6.0. Adjacent rows are LERPed, so the sky folds
// from one sheet into the next as the storm grows instead of snapping.
vec3 mcsm_sheet_storm(float up, float p) {
    float v = mcsm_sheet_storm_row(p);
    int a = int(floor(v));
    int b = a + 1;
    if (b > %(storm_max)d) b = %(storm_max)d;
    float f = v - floor(v);
    return mix(mcsm_sheet_row_storm(a, up), mcsm_sheet_row_storm(b, up), f);
}

// Vanilla cycle: row 0 blue (day), 1 lavender (night), 2 sunset split (dusk).
// The weights vanilla already computes pick the position in that row triangle.
vec3 mcsm_sheet_vanilla(float up, float dayW, float nightW, float duskW) {
    float v = (0.0 * dayW + 1.0 * nightW + 2.0 * duskW) / max(dayW + nightW + duskW, 0.0001);
    int a = int(floor(v));
    int b = a + 1;
    if (a > 1) a = 1;
    if (b > 2) b = 2;
    float f = clamp(v - floor(v), 0.0, 1.0);
    return mix(mcsm_sheet_row_vanilla(a, up), mcsm_sheet_row_vanilla(b, up), f);
}

// The folding blend itself: one LERP, driven by w (0 = untouched sky,
// 1 = the sheet is the sky).
vec3 mcsm_sheet_fold(vec3 base, vec3 sheet, float w) {
    return mix(base, sheet, clamp(w, 0.0, 1.0));
}

// ---- storm-approach carrier -------------------------------------------------
// The 1395..1855 slot carries PHASE only (a base-mod writer owns the integer
// part). McsmBlobCarrierPatch re-stamps the same slot at TAIL with
//     skyEnd = 1000 + phase*100 + 0.008 * approach        (approach 0..1)
// which is +0..0.008 on a value that is exact in float32 to ~1e-4, so the
// fraction survives the round trip. A legacy writer leaves the fraction at 0,
// which decodes to approach 0 -- the sheets then fold on phase alone.
float mcsm_approach(float fogSkyEnd) {
    if (!(fogSkyEnd > 1395.0 && fogSkyEnd < 1855.0)) return 0.0;
    float raw = (fogSkyEnd - 1000.0) * 100.0;
    return clamp((raw - floor(raw)) / 0.8, 0.0, 1.0);
}

// Storm fold weight: the teal sheet IS the phase-5 sky, so this reaches 1.0 by
// 5.02 and stays there -- later phases cross-fade between ROWS instead of
// changing the weight. A closing storm pulls the fold in early.
float mcsm_sheet_w_storm(float p, float approach) {
    return clamp(mcsm_sheet_ramp(p, 4.45, 5.02) * (1.0 + 0.25 * approach), 0.0, 1.0);
}

// Vanilla fold weight: the three overworld sheets ARE the calm sky. Raise
// MCSM_SHEET_VANILLA_BASE to 1.0 for "sheets only" -- the sampled
// SKY_DAY/NIGHT/DUSK arrays then no longer show through.
#define MCSM_SHEET_VANILLA_BASE 0.65
float mcsm_sheet_w_vanilla(float approach) {
    return clamp(MCSM_SHEET_VANILLA_BASE + (1.0 - MCSM_SHEET_VANILLA_BASE) * approach,
                 0.0, 1.0);
}
"""


def build_block():
    storm = rows_to_stops(STORM_ROWS, SHEET_W)
    vanilla = rows_to_stops(VANILLA_ROWS, SHEET_W)
    body = [
        BEGIN,
        "// GENERATED by ci/make_sky_lut.py from the gradient sheets in",
        "//   %s" % STORM_SHEET_SRC,
        "//   %s" % VANILLA_SHEET_SRC,
        "// Replace those PNGs (your image_*.png maps to the same targets -- see",
        "// ci/SKY_SHEETS.md) and re-run the script; never edit this block by hand.",
        "// Storm rows: " + " | ".join(n for n, _ in storm),
        "// Vanilla rows: " + " | ".join(n for n, _ in vanilla),
        emit_array("MCSM_SHEET_STORM", storm, SHEET_W) + "\n",
        emit_array("MCSM_SHEET_VANILLA", vanilla, SHEET_W) + "\n",
        FUNCS % {"storm_rows": len(storm), "vanilla_rows": len(vanilla),
                 "cols": SHEET_W, "storm_max": len(storm) - 1},
        END,
    ]
    return "\n".join(body)


def patch(path, block):
    full = os.path.join(ROOT, path)
    with open(full) as f:
        text = f.read()
    if BEGIN not in text or END not in text:
        return False, "no markers"
    head, rest = text.split(BEGIN, 1)
    _, tail = rest.split(END, 1)
    new = head + block + tail
    if new == text:
        return True, "unchanged"
    with open(full, "w") as f:
        f.write(new)
    return True, "updated"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true",
                    help="exit 1 if any target's block is stale (CI gate)")
    args = ap.parse_args()
    block = build_block()
    bad = 0
    for path, _pad in TARGETS:
        ok, state = patch(path, block) if not args.check else check(path, block)
        print("[lut] %-88s %s" % (path if len(path) < 88 else "..." + path[-85:], state))
        if state in ("no markers", "STALE"):
            bad = 1
    return bad


def check(path, block):
    full = os.path.join(ROOT, path)
    with open(full) as f:
        text = f.read()
    if BEGIN not in text or END not in text:
        return False, "no markers"
    head, rest = text.split(BEGIN, 1)
    _, tail = rest.split(END, 1)
    return True, ("ok" if head + block + tail == text else "STALE")


if __name__ == "__main__":
    sys.exit(main())
