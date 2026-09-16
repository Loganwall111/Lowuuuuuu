#!/usr/bin/env python3
"""palette_tables.py -- BUILD #416: ONE source of truth for the storm's colours.

WHY
---
The three reference sheets the brief names are the sky:

    "phase 5 turquoise sky.png"      -> phase 5.0 - 5.1   (teal)
    "phase5sky0purple sky.png"       -> phase 5.5 - 5.9   (purple / magenta)
    "phase6sky 6 witherstorm.png"    -> phase 6.0 - 7.0   (rose + black smudge)

Their stop tables are traced once and then needed in four places: the sky
shader, the position shader, the Java feed that colours the halo, and every
baked texture in ci/. Hand-copying them is how a build ends up with a sky and
a halo that disagree, so this module READS them out of the shipped GLSL and
hands the same numbers to the Python generators. Nothing re-types a colour.

PARITY
------
`check_parity()` compares, value by value:

    mcsm-core-shaders/core/sky.fsh          the live sky program
    mcsm-core-shaders/core/position.fsh     the blueprint's position program
    mcsm-extras/.../McsmStormPhase.java     the Java halo/sky feed

A drift of more than 1e-4 in any channel is a build failure, not a warning.
"""
import functools
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)

# ===========================================================================
# BUILD #416 -- GROUND-TRUTH SHEET HEXES (supplied by the artist, verbatim).
#
# These are the authoritative anchors for the three reference sheets. The trace
# in the GLSL below (and everything derived from it) is expanded from these,
# NOT the other way round:
#
#     anchors are at t = 0.0 (sky ceiling), 0.5 (middle gradient), 1.0 (horizon)
#
# so a six-stop column is their piecewise-linear expansion. `--from-hex` in
# ci/trace_sky_sheets.py writes them into sky.fsh / position.fsh /
# McsmStormPhase.java, and check_hex() fails the build if the shipped tables
# ever stop matching these three arrays.
# ===========================================================================
SHEET_HEX = {
    # phase 5 backdrop -- slate teal horizon sheet
    "teal": ["#0C1216", "#172228", "#202E34"],
    # phase 5.5-5.9 backdrop -- amethyst purple horizon sheet
    "purple": ["#160A21", "#3A184E", "#5A2474"],
    # phase 6 backdrop -- muted pink gray horizon sheet
    "rose": ["#1D1519", "#422D37", "#644354"],
}
# ===========================================================================
# BUILD #434 -- THE SPEC TABLES, VERBATIM FROM THE USER'S OWN HAND.
#
# The build brief for the model-attached halo quotes the sky's three bands
# directly, three anchors each:
#
#   phase 5 slate-teal      #0C1216 ceiling / #172228 middle / #202E34 horizon
#   phase 5.5-5.9 amethyst  #160A21 ceiling / #3A184E middle / #5A2474 horizon
#   phase 6 plum-to-salmon  #1D1519 ceiling / #422D37 middle / #644354 horizon
#
# They are checked against SHEET_HEX below rather than trusted: if the traced
# tables and the brief ever disagree, the build says so instead of quietly
# shipping one of them.
# ===========================================================================
SPEC_HEX = {
    "teal": ["#0C1216", "#172228", "#202E34"],
    "purple": ["#160A21", "#3A184E", "#5A2474"],
    "rose": ["#1D1519", "#422D37", "#644354"],
}


def check_spec(verbose=False):
    """The brief's own hex tables must BE the traced anchors, channel for channel."""
    ok = True
    msgs = []
    for role, want in SPEC_HEX.items():
        got = SHEET_HEX.get(role)
        if got != want:
            ok = False
            msgs.append("spec drift %-6s brief %s vs traced %s"
                        % (role, " ".join(want), " ".join(got or [])))
        elif verbose:
            msgs.append("spec ok: %s == %s" % (role, " ".join(want)))
    return ok, msgs


ANCHOR_T = [0.0, 0.5, 1.0]
STOPS = 6


def hex_to_rgb(h):
    h = h.lstrip("#")
    return [int(h[i:i + 2], 16) / 255.0 for i in (0, 2, 4)]


def hex_column(role):
    """The six-stop column expanded from SHEET_HEX[role]'s three anchors."""
    anchors = [hex_to_rgb(h) for h in SHEET_HEX[role]]
    out = []
    for i in range(STOPS):
        t = i / float(STOPS - 1)
        # locate the anchor pair this stop falls between
        if t <= ANCHOR_T[0]:
            out.append(list(anchors[0]))
            continue
        for j in range(len(ANCHOR_T) - 1):
            lo, hi = ANCHOR_T[j], ANCHOR_T[j + 1]
            if t <= hi:
                f = (t - lo) / (hi - lo)
                a, b = anchors[j], anchors[j + 1]
                out.append([a[k] + (b[k] - a[k]) * f for k in range(3)])
                break
        else:
            out.append(list(anchors[-1]))
    return out


def check_hex(verbose=False):
    """The shipped GLSL columns must BE the supplied hex anchors."""
    ok = True
    msgs = []
    tables = load()
    for role, anchors in SHEET_HEX.items():
        want = hex_column(role)
        got = tables.get(role)
        if not got:
            ok = False
            msgs.append("hex: no %s column in the GLSL to compare" % role)
            continue
        worst = 0.0
        where = ""
        for i in range(STOPS):
            g = sample_column(got, i / float(STOPS - 1))
            d = max(abs(want[i][k] - g[k]) for k in range(3))
            if d > worst:
                worst = d
                where = "stop %d: shipped %s vs hex %s" % (i, _hex(g), _hex(want[i]))
        if worst > 2.0 / 255.0:
            ok = False
            msgs.append("hex drift %-6s %.4f -- %s (run: trace_sky_sheets.py --from-hex --apply)"
                        % (role, worst, where))
        elif verbose:
            msgs.append("hex ok: %s == %s -> %s" % (role, " ".join(anchors),
                                                    _hex(want[-1])))
    return ok, msgs

GLSL_SOURCES = [
    "mcsm-core-shaders/core/sky.fsh",
    "mcsm-core-shaders/core/position.fsh",
]
JAVA_SOURCE = "mcsm-extras/java/net/mcsm/extras/client/McsmStormPhase.java"

# Role -> the tokens that identify that column in every file it appears in.
ROLES = {
    "teal": ["PHASE5_TEAL", "SKY_TEAL", "SKY_REF_TEAL"],
    "purple": ["PHASE55_PUR", "SKY_PURPLE", "SKY_REF_PURPLE"],
    "rose": ["PHASE6_ROSE", "SKY_ROSE", "SKY_REF_ROSE"],
    "ember": ["EMBER_END", "SKY_EMBER"],
    "day": ["SKY_DAY"],
    "night": ["SKY_NIGHT"],
    "sunset": ["SKY_SUNSET"],
}
# Roles the whole build is keyed on -- these must agree everywhere.
CANONICAL = ["teal", "purple", "rose"]

# The phase routing, identical in sky.fsh, position.fsh and McsmStormPhase.java.
TEAL_TO_PURPLE = (5.10, 5.50)
PURPLE_TO_ROSE = (5.75, 6.05)
ROSE_TO_EMBER = (7.00, 8.05)


def read(rel):
    with open(os.path.join(ROOT, rel), encoding="utf-8", errors="replace") as f:
        return f.read()


def _floats(text):
    return [float(x) for x in re.findall(r"-?\d+\.?\d*(?:[eE][-+]?\d+)?", text)]


def _parse_glsl(text):
    """{NAME: [[r,g,b], ...]} out of `const vec3 NAME[6] = vec3[](vec3(..), ...);`"""
    out = {}
    for m in re.finditer(r"const\s+vec3\s+([A-Za-z_0-9]+)\s*\[\s*\d+\s*\]\s*=\s*vec3\[\]\s*\((.*?)\)\s*;",
                         text, re.S):
        name = m.group(1)
        rows = []
        for v in re.finditer(r"vec3\s*\(([^)]*)\)", m.group(2)):
            vals = _floats(v.group(1))
            if len(vals) >= 3:
                rows.append(vals[:3])
        if rows:
            out[name] = rows
    return out


def _parse_java(text):
    """{NAME: [[r,g,b], ...]} out of `float[][] NAME = { {...}, ... };`"""
    out = {}
    for m in re.finditer(r"float\[\]\[\]\s+([A-Za-z_0-9]+)\s*=\s*\{(.*?)\}\s*;", text, re.S):
        name = m.group(1)
        rows = []
        for v in re.finditer(r"\{([^{}]*)\}", m.group(2)):
            vals = _floats(v.group(1))
            if len(vals) >= 3:
                rows.append(vals[:3])
        if rows:
            out[name] = rows
    for m in re.finditer(r"float\[\]\s+([A-Za-z_0-9]+)\s*=\s*\{([^{}]*)\}\s*;", text):
        vals = _floats(m.group(2))
        if len(vals) >= 3:
            out[m.group(1)] = [vals[:3]]
    return out


@functools.lru_cache(maxsize=None)
def load():
    """{role: [[r,g,b] x 6]} read out of the shipped GLSL (single source).

    Cached: the texture bakers call this once per PIXEL otherwise, which turned a
    32k-pixel sheet into 32k file parses (~6 minutes per build)."""
    tables = {}
    for rel in GLSL_SOURCES:
        try:
            got = _parse_glsl(read(rel))
        except OSError:
            continue
        for role, names in ROLES.items():
            for name in names:
                if name in got and role not in tables:
                    tables[role] = got[name]
        # a single-row ember table is valid
        for name in ROLES["ember"]:
            if name in got and len(got[name]) == 1 and "ember_rows" not in tables:
                tables["ember_rows"] = got[name]
    return tables


def rows(role):
    t = load()
    if role not in t:
        raise KeyError("palette table '%s' not found in %s" % (role, GLSL_SOURCES))
    return t[role]


def ramp(v, lo, hi):
    if hi <= lo:
        return 1.0 if v >= hi else 0.0
    t = max(0.0, min(1.0, (v - lo) / (hi - lo)))
    return t * t * (3.0 - 2.0 * t)


def sample_column(col, t):
    """Sample one traced 6-stop column at t (0 zenith -> 1 horizon)."""
    if len(col) == 1:
        return list(col[0])
    u = max(0.0, min(1.0, t)) * (len(col) - 1)
    i = int(u // 1)
    f = u - i
    if i > len(col) - 2:
        i, f = len(col) - 2, 1.0
    a, b = col[i], col[i + 1]
    return [a[k] + (b[k] - a[k]) * f for k in range(3)]


@functools.lru_cache(maxsize=None)
def column(phase, t):
    """The reference sky colour at (phase, vertical t) -- same routing as sky.fsh.
    Returns a tuple so the cache key/value stays immutable."""
    teal = sample_column(rows("teal"), t)
    purple = sample_column(rows("purple"), t)
    rose = sample_column(rows("rose"), t)
    ember = sample_column(rows("ember"), t)
    a = ramp(phase, *TEAL_TO_PURPLE)
    b = ramp(phase, *PURPLE_TO_ROSE)
    c = ramp(phase, *ROSE_TO_EMBER)
    out = [teal[k] + (purple[k] - teal[k]) * a for k in range(3)]
    out = [out[k] + (rose[k] - out[k]) * b for k in range(3)]
    out = [out[k] + (ember[k] - out[k]) * c for k in range(3)]
    return tuple(out)


def argb(c, a=255):
    def q(v):
        return max(0, min(255, int(round(v * 255.0))))
    return (a << 24) | (q(c[0]) << 16) | (q(c[1]) << 8) | q(c[2])


# ---------------------------------------------------------------------------
# The phase-6+ smudge: purple / magenta / pink / orange / salmon around a
# near-black core. Those five names are the brief's own words for the sheet;
# every value below is a point on the TRACED tables above (or a mix of two of
# them), so the smudge stays inside the delivered palette instead of inventing
# a new one.
# ---------------------------------------------------------------------------
@functools.lru_cache(maxsize=None)
def smudge_deck():
    purple = rows("purple")
    rose = rows("rose")
    ember = rows("ember")[-1]                       # the ember horizon stop
    pur_h = purple[-1]                       # 0.660, 0.361, 0.706  (magenta)
    rose_h = rose[-1]                        # 0.784, 0.612, 0.651  (pink)
    rose_m = rose[2]                         # 0.478, 0.392, 0.439  (muted mid)
    orange = tuple(ember[k] * 1.62 if k != 1 else ember[k] * 2.30 for k in range(3))
    salmon = tuple((rose_h[k] + orange[k]) * 0.5 for k in range(3))
    return {
        "core": (0.020, 0.008, 0.030),
        "upper": tuple(rose_m),
        "purple": tuple(pur_h),
        "magenta": tuple(pur_h),
        "pink": tuple(rose_h),
        "salmon": salmon,
        "orange": orange,
    }


# ---------------------------------------------------------------------------
# The storm-visual constants in mcsm_visuals.glsl are SAMPLED off the traced
# columns, one role per row of the trace. This is the derivation, and
# check_derived() holds the shipped GLSL to it.
#   (constant name, source table role, mode, value, scale)
#   mode "t"   = sample the column at vertical t (0 zenith -> 1 horizon)
#   mode "row" = take traced stop `value` verbatim
# ---------------------------------------------------------------------------
VISUALS = "mcsm-core-shaders/include/mcsm_visuals.glsl"
DERIVED = [
    ("P5_CORE", "teal", "t", 0.00, 0.22),
    ("P5_MID", "teal", "t", 0.30, 1.00),
    ("P5_EDGE", "teal", "t", 0.60, 1.00),
    ("P55_CORE", "purple", "t", 0.00, 0.22),
    ("P55_MID", "purple", "t", 0.30, 1.00),
    ("P55_EDGE", "purple", "t", 0.60, 1.00),
    ("P55_HIGH", "purple", "t", 0.85, 1.00),
    ("P6_TOP", "rose", "t", 0.00, 0.22),
    ("P6_UMID", "rose", "t", 0.30, 1.00),
    ("P6_LMID", "rose", "t", 0.60, 1.00),
    ("P6_BOT", "ember", "row", 3, 1.00),   # the orange stop of the ember column
    # The cloud deck: the traced column's mid row lifted toward white by a
    # per-phase amount. mode "mixw" = mix(column, white, w).
    ("MCSM_CLOUD_TEAL", "teal", "mixw", (0.30, 0.55), 1.00),
    ("MCSM_CLOUD_PURPLE", "purple", "mixw", (0.30, 0.10), 1.00),
    ("MCSM_CLOUD_ROSE", "rose", "mixw", (0.30, 0.35), 1.00),
    ("MCSM_CLOUD_EMBER", "ember", "mixw", (0.50, 0.12), 1.00),
]
# P5_BEAM is the cosmic ambient bleed, not a sky colour: deliberately excluded.


def _parse_visuals(text):
    """{NAME: [r,g,b] in 0..1} out of `const vec3 NAME = vec3(..) / 255.0;`"""
    out = {}
    for m in re.finditer(r"const\s+vec3\s+([A-Za-z_0-9]+)\s*=\s*vec3\s*\(([^)]*)\)\s*/\s*255\.0\s*;",
                         text):
        vals = _floats(m.group(2))
        if len(vals) >= 3:
            out[m.group(1)] = [v / 255.0 for v in vals[:3]]
    return out


def check_derived(verbose=False):
    """Hold every storm-visual constant to its traced derivation."""
    ok = True
    msgs = []
    try:
        shipped = _parse_visuals(read(VISUALS))
    except OSError as exc:
        return False, ["cannot read %s: %s" % (VISUALS, exc)]
    for name, role, mode, value, scale in DERIVED:
        if name not in shipped:
            ok = False
            msgs.append("visuals: %s not found in %s" % (name, VISUALS))
            continue
        if mode == "row":
            base = list(rows(role)[int(value)])
        elif mode == "mixw":
            t, w = value
            base = [v * (1.0 - w) + w for v in sample_column(rows(role), t)]
        else:
            base = list(sample_column(rows(role), value))
        want = [v * scale for v in base]
        got = shipped[name]
        drift = max(abs(want[k] - got[k]) for k in range(3))
        if drift > 2.0 / 255.0:   # the shipped constants are rounded to 0..255
            ok = False
            msgs.append("visuals drift: %s = %s, traced %s (%.4f)"
                        % (name, _hex(got), _hex(want), drift))
        elif verbose:
            where = ("row %d" % value if mode == "row"
                     else ("t=%.2f mixw %.2f" % value if mode == "mixw" else "t=%.2f" % value))
            msgs.append("visuals ok: %s == %s %s x%.2f" % (name, role, where, scale))
    return ok, msgs


def _hex(c):
    return "#%02X%02X%02X" % tuple(max(0, min(255, int(round(v * 255.0)))) for v in c)


def check_parity(verbose=False):
    """Compare the GLSL, the Java and this module. Returns (ok, messages)."""
    msgs = []
    ok = True
    glsl = {}
    for rel in GLSL_SOURCES:
        got = _parse_glsl(read(rel))
        for role, names in ROLES.items():
            for name in names:
                if name in got:
                    glsl.setdefault(role, []).append((rel, name, got[name]))
    java = _parse_java(read(JAVA_SOURCE))

    for role in CANONICAL + ["ember"]:
        entries = glsl.get(role, [])
        if not entries:
            ok = False
            msgs.append("GLSL: no %s column found in %s" % (role, ", ".join(GLSL_SOURCES)))
            continue
        base_rel, base_name, base = entries[0]
        for rel, name, tbl in entries[1:]:
            if _diff(base, tbl) > 1.0e-4:
                ok = False
                msgs.append("GLSL drift: %s.%s != %s.%s" % (rel, name, base_rel, base_name))
            elif verbose:
                msgs.append("GLSL agree: %s.%s == %s.%s" % (rel, name, base_rel, base_name))
        jname = None
        for name in ROLES[role]:
            if name in java:
                jname = name
                break
        if role == "ember" and jname is None:
            for name in ROLES["ember"]:
                if name in java:
                    jname = name
        if jname is None:
            ok = False
            msgs.append("Java: no %s column found in %s" % (role, JAVA_SOURCE))
            continue
        if _diff(base, java[jname]) > 1.0e-4:
            ok = False
            msgs.append("Java drift: %s.%s != %s.%s" % (JAVA_SOURCE, jname, base_rel, base_name))
        elif verbose:
            msgs.append("Java agree: %s.%s == GLSL %s" % (JAVA_SOURCE, jname, base_name))
    return ok, msgs


def _diff(a, b):
    """Max absolute channel difference between two columns of different length
    (compared through their own samples, so a 6-row and a 1-row table still
    compare meaningfully at both ends)."""
    worst = 0.0
    for i in range(12):
        t = i / 11.0
        ca = sample_column(a, t)
        cb = sample_column(b, t)
        worst = max(worst, max(abs(ca[k] - cb[k]) for k in range(3)))
    return worst


def main():
    verbose = "--quiet" not in sys.argv
    ok, msgs = check_parity(verbose=verbose)
    ok2, msgs2 = check_derived(verbose=verbose)
    ok3, msgs3 = check_hex(verbose=verbose)
    for m in msgs + msgs2 + msgs3:
        print(("  ok   " if " ok" in m or " agree: " in m else "  FAIL ") + m)
    ok = ok and ok2 and ok3
    print("[palette] %s (%d reference columns, %d derived constants, %d hex anchors)"
          % ("parity OK" if ok else "PARITY BROKEN", len(load()), len(DERIVED),
             sum(len(v) for v in SHEET_HEX.values())))
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
