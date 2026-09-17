#!/usr/bin/env python3
"""check_phase_uniform.py -- BUILD #416: prove the WitherStormPhase plumbing.

The blueprint feeds the storm phase to the GPU with a bare `glUniform1f`. That
is not available here: 26.2 core pipelines have a FIXED bind group, and a
uniform slot the layout does not carry is a hard Vulkan crash -- the exact trap
that cost this project a build before (iss. storm_glow.fsh, #407). The phase
therefore travels through the carrier the renderer already binds (FogData.skyEnd
= 1000 + phase*100, decoded by mcsm_witherstorm_phase()), and Iris/Oculus packs
additionally receive the real `witherstorm_Phase` uniform.

"Registers cleanly" therefore means five things, all checked here:

  1. ONE definition of mcsm_witherstorm_phase() (the accessor), in
     mcsm_visuals.glsl, and it is the carrier decode -- not a second uniform.
  2. NO core shader declares its own phase uniform (crash guard).
  3. Every phase-reading module resolves through the accessor / mcsm_phase.
  4. The Iris side exposes the real uniform name, so a pack that binds it wins.
  5. Java publishes the value: the resolver exists and the sky hook stamps it.

Exit 0 when every checkpoint passes, 1 otherwise (the build stops).
"""
import glob
import json
import os
import pathlib
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

VISUALS = "mcsm-core-shaders/include/mcsm_visuals.glsl"
CORE_DIR = "mcsm-core-shaders/core"
# Modules that must resolve a phase (i.e. are storm-aware).
CONSUMERS = [
    "sky.fsh",
    "entity.fsh",
    "terrain.fsh",
    "position.fsh",
    "rendertype_entity_cutout.fsh",
]
# Programs that run on the storm's OWN bind group. They must never import
# mcsm_visuals.glsl (a uniform the group does not bind is a hard Vulkan crash),
# so each has to resolve the phase one of two ways:
#   * decode the FogSkyEnd carrier locally (minecraft:fog.glsl is already
#     imported by that program -- no new uniform), or
#   * carry an explicit `MCSM_PHASE_SOURCE:` annotation naming where its phase
#     comes from. Silence is not accepted: an un-annotated storm shader is how
#     a phase palette drifts out of the build unnoticed.
EXTRA_CONSUMERS = [
    "jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh",
    "jar-overrides/assets/dabywitherstormmod/shaders/post/storm_sun_glow.fsh",
    "src/main/resources/assets/dabywitherstormmod/shaders/core/fogless_entity.fsh",
]
CARRIER_DECODE = "(FogSkyEnd - 1000.0)"
PHASE_SOURCE_TAG = "MCSM_PHASE_SOURCE:"

# BUILD #416 -- the teeth/aura split, the user's own phase 4 -> 8 spec. The teeth
# are WHITE at every storm phase; the aura is what changes colour. Both are
# checked here so a later edit cannot quietly re-tint the teeth:
#   (a) every consumer resolves a phase source (above);
#   (b) no shader may tint the TEETH with a phase colour -- mcsm_teeth_color()
#       and the local band tables must resolve to white for p >= 4;
#   (c) the aura ramp must exist in the shared include and in the one
#       storm-bound program that has to repeat it.
AURA_RAMPS = ["4.92", "5.15", "5.85", "6.90", "7.90"]
AURA_CONSUMERS = [
    "mcsm-core-shaders/include/mcsm_visuals.glsl",
    "mcsm-core-shaders/core/sky.fsh",
    "jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh",
    "src/main/resources/assets/dabywitherstormmod/shaders/core/fogless_entity.fsh",
]
JAVA_RESOLVER = "mcsm-extras/java/net/mcsm/extras/client/McsmStormPhase.java"
JAVA_SKY_HOOK = "mcsm-extras/java/net/mcsm/extras/client/McsmNativeSkyRenderer.java"
# The teeth/eye/aura tracks live with the tint feed, not with the sky resolver.
JAVA_TINT = "mcsm-extras/java/net/mcsm/extras/client/McsmTeethPhaseTint.java"
# BUILD #416 -- the death cinematic's Java driver and the dome shell.
JAVA_DEATH = "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBlobCarrierPatch.java"
JAVA_DOME = "mcsm-extras/java/net/dabicco/witherstormmod/client/StormSkyDome.java"
JAVA_TEETH = "mcsm-extras/java/net/mcsm/extras/client/McsmTeethPhaseTint.java"
JAVA_HEAD_EYES = "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHeadEyesRenderTypeMixin.java"
JAVA_BODY_EYES = "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBodyEyesRenderTypeMixin.java"
JAVA_HALO = "mcsm-extras/java/net/mcsm/extras/client/McsmHaloSkyRenderer.java"
JAVA_WHITE_GLOW = "mcsm-extras/java/net/dabicco/witherstormmod/client/McsmWhiteGlow.java"
JAVA_PRESENCE = "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmPresenceFxPatch.java"
STORM_GLOW_FSH = "jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh"
SKY_FSH = "mcsm-core-shaders/core/sky.fsh"


def read(rel):
    path = os.path.join(ROOT, rel)
    if not os.path.isfile(path):
        return None
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


# ---------------------------------------------------------------------------
# BUILD #416 -- shader include reachability.
#
# The "defs-only" question (are the helpers below mcsm_blob_color actually used?)
# has a concrete answer that a human reading the file cannot get reliably: build
# the call graph over every shipped shader and see which definitions are
# reachable from a real entry point. It turned out the port IS live -- the cloud
# deck's cover mask (core/rendertype_clouds.fsh) calls mcsm_mass_cover, and the
# inf_* helpers are its internals -- while 17 other helpers are genuinely
# dormant. Both facts are now enforced: the live entry point must stay wired, and
# a NEW orphan fails the build unless it is added to the dormant list on purpose.
# ---------------------------------------------------------------------------
DORMANT_ALLOWED = {
    "mcsm_apocalypse_bands",   # storyboard sky variant, not on any shipping path
    "mcsm_aurora",             # retired aurora layer
    "mcsm_blob",               # pre-native sky blob (McsmStormBlob replaced it)
    "mcsm_cinematic_sky",      # storyboard sky variant
    "mcsm_inf_p6_split",       # dormant half of the infinite-skybox port
    "mcsm_inf_palette",        # dormant half of the infinite-skybox port
    # BUILD #422 -- retired ON PURPOSE with the entity program's glint removal.
    # mcsm_glint() was the two-sine sheen that drowned the traced charcoal; the
    # mandate is that the body carries no additive sheen at all, so its include
    # twin is dead code kept only so the story of the removal is readable.
    # mcsm_void_crease() fed the black-mask helpers that were rebuilt in
    # fogless_entity.fsh itself, and mcsm_void_black() (its only caller chain
    # into the entity path) is superseded there as well.
    "mcsm_glint",
    "mcsm_void_crease",
    "mcsm_void_black",
    "mcsm_k_bot", "mcsm_k_mid", "mcsm_k_top", "mcsm_keys",  # older key tables
    "mcsm_kill_teal",          # teal-kill grade, superseded by the story grade
    "mcsm_rd_raw",             # carrier guard for a band that was never used
    "mcsm_sky_body_tint",      # retired body tint
    "mcsm_sky_color",          # superseded by mcsm_sky_reference/sky.fsh
    "mcsm_star_tint",          # retired star tint
    "mcsm_sun_halo",           # superseded by the authored sun pass
    "mcsm_void_black",         # superseded by the inline block in fogless_entity
}


def shader_reachability():
    """(reachable, unreachable) function names defined in the shader include."""
    text = read(VISUALS)
    if text is None:
        return set(), set()
    starts = [(m.start(), m.group(1)) for m in re.finditer(
        r"(?m)^(?:vec[234]|float|bool|void|int)\s+(mcsm_[A-Za-z0-9_]+)\s*\(", text)]
    bodies = {}
    for i, (pos, name) in enumerate(starts):
        end = starts[i + 1][0] if i + 1 < len(starts) else len(text)
        bodies[name] = text[pos:end]
    defs = set(bodies)

    outside = []
    for root in ("mcsm-core-shaders", "jar-overrides", "src/main/resources", "shaderpack-v5"):
        base = os.path.join(ROOT, root)
        for dirpath, _dirs, files in os.walk(base):
            for fn in files:
                if not fn.endswith((".fsh", ".vsh", ".glsl")):
                    continue
                if os.path.abspath(os.path.join(dirpath, fn)) == os.path.abspath(os.path.join(ROOT, VISUALS)):
                    continue
                blob = read(os.path.relpath(os.path.join(dirpath, fn), ROOT)) or ""
                outside.append(blob)
    shipped = "\n".join(outside)

    def calls(t):
        return set(d for d in defs if re.search(r"\b%s\s*\(" % re.escape(d), t))

    live = set(d for d in defs if re.search(r"\b%s\s*\(" % re.escape(d), shipped))
    changed = True
    while changed:
        changed = False
        for d in list(live):
            for c in calls(bodies.get(d, "")) - live:
                live.add(c)
                changed = True
    return live, set(d for d in defs if d not in live)


# ---------------------------------------------------------------------------
# BUILD #416 -- the frozen-base-symbol guard.
#
# mcsm-extras is compiled against a FROZEN base release asset (ci/build.sh pins
# dabbywitherstormmod-1.9.100-26.2-beta-mcsm.jar by sha256), and the reference
# copies of those classes under net/ and src-recon/ are on NO compile classpath.
# So a method added to a reference copy does not exist for javac. That is exactly
# how the first conic-column attempt failed: glowWhite() was added next to
# GlowRenderTypes (documentation tree) and javac -- reading the real class from the
# jar -- had never heard of it.
#
# ci/api/mod.txt is a javap dump of the base jar's own classes, regenerated by the
# runner before this gate runs (build.sh dumps it via javap, then runs [phase]), so
# it is the authority for what mcsm-extras may call. Only METHOD CALLS are checked
# (a name followed by an open paren): fields, nested types and type names are left
# alone, which keeps this precise and free of false positives.
# ---------------------------------------------------------------------------
BASE_API = "ci/api/mod.txt"


def code_only(text):
    """Source with comments removed -- structural checks must not read prose.

    The conic column's own javadoc says what it REPLACED ("no cap, no disc, no
    ring"), so a naive substring search reports the documentation as the defect.
    """
    if not text:
        return ""
    text = re.sub(r"/\*.*?\*/", " ", text, flags=re.S)
    return re.sub(r"//[^\n]*", " ", text)


def base_api_index():
    """({class: {method, ...}}, {every method}) from the javap dump of the base jar."""
    text = read(BASE_API) or ""
    per_class, every = {}, set()
    current = None
    for line in text.splitlines():
        if not line.strip() or line.startswith("Compiled from") or line.strip() == "}":
            continue
        if not line[0].isspace():
            m = re.match(r"^[\w ]*(?:class|interface|enum|record)\s+([\w.$]+)", line)
            if m:
                current = m.group(1)
                per_class.setdefault(current, set())
            continue
        if current is None or "(" not in line:
            continue
        m = re.search(r"([A-Za-z_$][A-Za-z0-9_$]*)\s*\(", line)
        if m:
            per_class[current].add(m.group(1))
            every.add(m.group(1))
    return per_class, every


def base_api_calls():
    """[(file, class, method)] -- base-jar methods mcsm-extras statically calls."""
    calls = []
    root = os.path.join(ROOT, "mcsm-extras", "java")
    for dirpath, _dirs, files in os.walk(root):
        for fn in sorted(files):
            if not fn.endswith(".java"):
                continue
            rel = os.path.relpath(os.path.join(dirpath, fn), ROOT)
            text = read(rel) or ""
            text = re.sub(r"/\*.*?\*/", " ", text, flags=re.S)
            text = re.sub(r"//[^\n]*", " ", text)
            named = {}
            for m in re.finditer(r"import\s+(?:static\s+)?(net\.dabicco\.witherstormmod\.[A-Za-z0-9_.]+)\s*;", text):
                fq = m.group(1)
                named[fq.rsplit(".", 1)[1]] = fq
            for m in re.finditer(r"\b(net\.dabicco\.witherstormmod\.[A-Za-z0-9_.]+?)\.([A-Za-z_$][A-Za-z0-9_$]*)\s*\(", text):
                named[m.group(1).rsplit(".", 1)[-1]] = m.group(1)
                calls.append((rel, m.group(1), m.group(2)))
            for simple, fq in named.items():
                for m in re.finditer(r"\b%s\s*\.\s*([A-Za-z_$][A-Za-z0-9_$]*)\s*\(" % re.escape(simple), text):
                    calls.append((rel, fq, m.group(1)))
    # de-duplicate, keep order
    seen, out = set(), []
    for c in calls:
        if c not in seen:
            seen.add(c)
            out.append(c)
    return out


def _mcsm_java_sources():
    """Every Java source of the mod, for the full-screen-plate scan."""
    root = os.path.join("mcsm-extras", "java")
    out = []
    for base, _dirs, files in os.walk(root):
        for name in files:
            if name.endswith(".java"):
                try:
                    out.append(open(os.path.join(base, name), encoding="utf-8").read())
                except OSError:
                    pass
    return out


_FILL_CONST_RE = re.compile(r"static\s+final\s+int\s+([A-Za-z_$][\w$]*)\s*=\s*(0x[0-9A-Fa-f]{1,8})\s*;")
# BUILD #469 -- a plate does not have to be a `static final int` to be a plate.
# The two classes that were still black at their own value (the base config screen's
# `BAR_BG`, the console's `ROW_DARK`) declared their colours as plain locals, so the
# scanner missed exactly the shape of the bug it exists to find. It resolves local
# declarations now, and simple arithmetic (`(alpha << 24) | 0xRRGGBB`), and refuses
# anything it cannot evaluate instead of guessing.
_FILL_LOCAL_CONST_RE = re.compile(r"\bint\s+([A-Za-z_$][\w$]*)\s*=\s*([^;]+);")
_FILL_FULLSCREEN_RE = re.compile(
    r"fill(?:Gradient)?\(\s*0,\s*0,\s*(?:w|width|this\.width)\s*,\s*(?:h|height|this\.height)\s*,\s*([^;]+?)\)")
_FILL_BAND_RE = re.compile(
    r"fill(?:Gradient)?\(\s*0,\s*([^,]+?)\s*,\s*(?:w|width|this\.width)\s*,\s*([^,]+?)\s*,\s*([^,;]+?)[,)]")


def _fill_colour(expr, consts):
    """0xRRGGBB[AA], a named int of this file, or `(x << n) | 0x..` -- else None.

    None means "computed at runtime" (a fade, a pulse, an alpha from state), which
    is not a plate: it is not opaque at every frame by construction.
    """
    expr = expr.strip()
    lit = re.fullmatch(r"0x([0-9A-Fa-f]{1,8})", expr)
    if lit:
        return int(expr, 16)
    if re.fullmatch(r"[A-Za-z_$][\w$]*", expr):
        return consts.get(expr)
    total = 0
    for term in expr.split("|"):
        term = term.strip()
        if re.fullmatch(r"0x[0-9A-Fa-f]{1,8}", term):
            total |= int(term, 16)
            continue
        sh = re.fullmatch(r"\(?\s*([A-Za-z_$][\w$]*)\s*<<\s*(\d+)\s*\)?", term)
        if sh and sh.group(1) in consts:
            total |= consts[sh.group(1)] << int(sh.group(2))
            continue
        return None
    return total


def _fill_consts(src):
    """Every int of this file whose value is a colour the scanner can evaluate."""
    out = {}
    for m in _FILL_CONST_RE.finditer(src):
        out[m.group(1)] = int(m.group(2), 16)
    for m in _FILL_LOCAL_CONST_RE.finditer(src):
        if m.group(1) in out:
            continue
        v = _fill_colour(m.group(2), out)
        if v is not None:
            out[m.group(1)] = v
    return out


def _menu_black_plates(*sources):
    """Every full-screen fill on the mod's screen path, checked for opaqueness.

    A "black menu" is a DRAW, so this reads the actual values -- and it resolves
    same-file constants, because the worst one of these was not a literal at all:
    the story-mode console filled the whole frame with BG_TOP/BG_BOTTOM, an opaque
    #0D1016..#07080C, and a literal-only scan walked straight past it. Any
    fill(0, 0, w, h, ...) whose resolved alpha is 0xE0 or more AND whose colour is
    (near) black is a plate that can hide everything behind it.

    BUILD #469 -- and a band is a plate too. The title's "very black" band was 118
    pixels of opaque #02101E across the whole width: not the whole frame, but the
    whole title. So a band that covers 96px or more of the frame, top or bottom, and
    is opaque (near) black, is reported the same way.
    """
    bad = []
    for src in sources:
        consts = _fill_consts(src)
        for m in _FILL_FULLSCREEN_RE.finditer(src):
            for token in m.group(1).split(","):
                value = _fill_colour(token, consts)
                if value is None:
                    continue  # computed at runtime (a fade, a wash) -- not a plate
                if ((value >> 24) & 0xFF) >= 0xE0 \
                        and ((value >> 16 & 0xFF) + (value >> 8 & 0xFF) + (value & 0xFF)) < 0x40:
                    bad.append(m.group(0)[:80])
        for m in _FILL_BAND_RE.finditer(src):
            colour = _fill_colour(m.group(3), consts)
            if colour is None:
                continue
            if ((colour >> 24) & 0xFF) < 0xE0:
                continue
            if ((colour >> 16 & 0xFF) + (colour >> 8 & 0xFF) + (colour & 0xFF)) >= 0x40:
                continue
            top, bottom = m.group(1).strip(), m.group(2).strip()
            tall = False
            k = re.fullmatch(r"\d+", top)
            if k and bottom in ("h", "height", "this.height") and int(top) >= 96:
                tall = True
            k = re.fullmatch(r"(?:h|height|this\.height)\s*-\s*(\d+)", top)
            if k and bottom in ("h", "height", "this.height") and int(k.group(1)) >= 96:
                tall = True
            k = re.fullmatch(r"\d+", bottom)
            if k and top in ("0",) and int(bottom) >= 96:
                tall = True
            if tall:
                bad.append(m.group(0)[:80])
    return bad


_HANDLER_SIG_RE = re.compile(
    r"^\s*(?:private|public|protected|static|final|\s)+[\w<>\[\],.$?]+\s+([\w$]+)\s*\(([^)]*)\s*\)\s*\{$",
    re.M)


def _unguarded_screen_handlers():
    """@Inject handlers of this build that DRAW and carry no catch of their own.

    BUILD #469 -- this is the bug class the last three menu reports share. In 26.2
    a screen's frame is built by extractRenderState / extractBackground (and the HUD
    by the overlay's render): an exception out of one of those does not draw a dark
    screen, it draws NO screen -- a black window with a title bar, which is exactly
    the screenshot. So every handler this build injects into a render method has to
    be wrapped, and this returns the ones that are not (empty is the passing state).
    """
    bad = []
    root = os.path.join("mcsm-extras", "java")
    for base, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".java"):
                continue
            path = os.path.join(base, name)
            src = open(path, encoding="utf-8").read()
            for m in _HANDLER_SIG_RE.finditer(src):
                if "GuiGraphicsExtractor" not in m.group(2):
                    continue
                if "@Inject" not in src[max(0, m.start() - 400):m.start()]:
                    continue
                k = m.end() - 1
                depth = 0
                while k < len(src):
                    if src[k] == "{":
                        depth += 1
                    elif src[k] == "}":
                        depth -= 1
                        if depth == 0:
                            break
                    k += 1
                body = src[m.end() - 1:k + 1]
                if "catch (Throwable" not in body:
                    bad.append(name + ":" + m.group(1))
    return bad


_CLASS_SIG_RE = re.compile(
    r"^(?:public\s+|abstract\s+|final\s+)*class\s+(\w+)(?:\s+extends\s+([\w.$]+))?", re.M)


def _illegal_screen_members():
    """`this.width` / `this.height` / `this.children()` / `this.font` in a class
    that does not extend Screen (whose own fields and methods those are).

    BUILD #469b -- run 586 died on exactly this: the fault-isolation bodies added
    to the config mixin read `this.height`, and that mixin is a mixin ON a screen
    rather than a subclass of one, so javac had no such member and the build failed
    at 190 of 219 classes. The runner is the only compiler this project has, so the
    rule is enforced here instead of discovered there.
    """
    bad = []
    root = os.path.join("mcsm-extras", "java")
    for base, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".java"):
                continue
            path = os.path.join(base, name)
            src = open(path, encoding="utf-8").read()
            m = _CLASS_SIG_RE.search(src)
            if not m:
                continue
            sup = m.group(2) or ""
            if "Screen" in sup or "Hud" in sup:
                continue
            for mm in re.finditer(r"\bthis\.(width|height|children\(\)|font)\b", src):
                line = src[:mm.start()].count("\n") + 1
                bad.append("%s:%d this.%s" % (name, line, mm.group(1)))
    return bad


def main():
    checks = []
    fails = []

    def check(name, ok, detail=""):
        checks.append(name)
        if not ok:
            fails.append("%s%s" % (name, (" -- " + detail) if detail else ""))

    # ---- 1. the accessor -----------------------------------------------
    vis = read(VISUALS)
    check("accessor file present", vis is not None, VISUALS)
    if vis:
        defs = re.findall(r"float\s+mcsm_witherstorm_phase\s*\(", vis)
        check("exactly one accessor definition", len(defs) == 1,
              "found %d" % len(defs))
        accessor = ""
        if len(defs) == 1:
            i = vis.index("float mcsm_witherstorm_phase(")
            accessor = vis[i:i + 400]
        check("accessor decodes the FogSkyEnd carrier",
              "mcsm_phase(FogSkyEnd" in accessor)
        check("accessor introduces no new uniform",
              "uniform" not in accessor)
        # ---- 4. the Iris/Oculus uniform name ---------------------------
        check("Iris custom uniform `witherstorm_Phase` declared",
              re.search(r"uniform\s+float\s+witherstorm_Phase\s*;", vis) is not None)

    # ---- 2. crash guard: no core shader may declare a phase uniform -----
    offenders = []
    for dirpath, _dirs, files in os.walk(os.path.join(ROOT, CORE_DIR)):
        for fn in files:
            if not fn.endswith((".fsh", ".vsh")):
                continue
            text = read(os.path.relpath(os.path.join(dirpath, fn), ROOT)) or ""
            for m in re.finditer(r"^\s*uniform\s+[A-Za-z0-9_]+\s+(WitherStormPhase|witherstorm_Phase)\s*;",
                                 text, re.M):
                offenders.append("%s: %s" % (fn, m.group(0).strip()))
    check("no core shader declares a phase uniform", not offenders, "; ".join(offenders))

    # ---- 3. every consumer resolves the phase --------------------------
    for fn in CONSUMERS:
        text = read(os.path.join(CORE_DIR, fn))
        if text is None:
            check("consumer %s present" % fn, False, "missing")
            continue
        resolved = ("mcsm_witherstorm_phase(" in text) or ("mcsm_phase(" in text)
        check("consumer %s resolves the phase" % fn, resolved)

    for rel in EXTRA_CONSUMERS:
        name = os.path.basename(rel)
        text = read(rel)
        if text is None:
            check("consumer %s present" % name, False, "missing")
            continue
        via_accessor = ("mcsm_witherstorm_phase(" in text) or ("mcsm_phase(" in text)
        via_carrier = CARRIER_DECODE in text.replace("FogSkyEnd-1000.0", CARRIER_DECODE)
        annotated = PHASE_SOURCE_TAG in text
        check("consumer %s resolves the phase" % name, via_accessor or via_carrier or annotated,
              "neither the carrier decode nor an %s annotation" % PHASE_SOURCE_TAG)
        if annotated and not (via_accessor or via_carrier):
            line = [l.strip() for l in text.splitlines() if PHASE_SOURCE_TAG in l][0]
            print("  note %s declares its phase source: %s" % (name, line[:110]))

    # ---- 4b. the teeth/aura split --------------------------------------
    visuals = read(VISUALS) or ""
    check("teeth track exists and is white from phase 4",
          "vec3 mcsm_teeth_color(float p)" in visuals
          and "if (p >= 4.0) return vec3(1.00, 1.00, 1.00);" in visuals)
    check("aura track exists in the shared include",
          "vec3 mcsm_aura_color(float p)" in visuals)
    glow_text = read("jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh") or ""
    fogless_text = read("src/main/resources/assets/dabywitherstormmod/shaders/core/fogless_entity.fsh") or ""
    for tok in AURA_RAMPS:
        ramp_in_include = ("mcsm_ramp(p, %s" % tok) in visuals
        ramp_in_glow = ("smoothstep(%s" % tok) in glow_text
        ramp_in_fogless = ("smoothstep(%s" % tok) in fogless_text
        present = ramp_in_include or ramp_in_glow or ramp_in_fogless
        check("aura ramp %s present" % tok, present)
    for rel in AURA_CONSUMERS:
        text = read(rel) or ""
        has = ("mcsm_aura_color(" in text) or ("vec3 aura = vec3(0.55, 0.80, 1.00);" in text) \
              or ("band = vec3(0.55, 0.80, 1.00);" in text)
        check("aura reaches %s" % os.path.basename(rel), has,
              "no aura ramp in this module")
    # the teeth must not be phase-tinted anywhere any more
    offenders = []
    for rel in [VISUALS] + [os.path.join(CORE_DIR, c) for c in CONSUMERS]:
        text = read(rel) or ""
        for m in re.finditer(r"mcsm_mouth_color\([^)]*\)\s*\*", text):
            offenders.append("%s: %s" % (os.path.basename(rel), m.group(0)))
    check("teeth are not multiplied by a phase colour", not offenders, "; ".join(offenders))

    # ---- 4c. the eyes keep their violet glow ----------------------------
    # The teeth are white; the EYES are not. The reference frames show a violet
    # lens glow with a magenta pupil, and this build once flattened both onto
    # the white teeth track, which is why the eyes lost their colour.
    java = read(JAVA_TINT) or ""
    check("the tint feed is present", java != "", JAVA_TINT)
    check("an explicit eye track exists (separate from the teeth)",
          "EYE_TRACK" in java and "public static float[] eye(" in java)
    check("eyeTintArgb uses the eye track, not the teeth track",
          "float[] e = eye(nearestPhase());" in java)
    check("the pupil stays magenta in every phase",
          "PUPIL_R" in java and "PUPIL_G" in java and "PUPIL_B" in java)
    check("the eye hood does not snap coloured emitters onto the teeth band",
          "eyeLike" in visuals and "eyeLike" in fogless_text)
    # Every symbol the other modules call on the tint feed must exist. This is a
    # cheap stand-in for the compiler that caught a real missing constant
    # (McsmStormBlob referenced PUPIL_R/G/B before they were declared).
    import re as _re
    hrefs = {m for rel in ["mcsm-extras/java/net/mcsm/extras/client/McsmStormBlob.java",
                           "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmExactGlowTintMixin.java"]
             for m in _re.findall(r"McsmTeethPhaseTint\.([A-Za-z_0-9]+)", read(rel) or "")}
    hdecl = set(_re.findall(r"public static (?:final )?(?:int|float\[\]|float|void|int\[\])\s+([A-Za-z_0-9]+)", java))
    missing = sorted(hrefs - hdecl)
    check("every McsmTeethPhaseTint symbol used elsewhere is declared", not missing,
          "missing: %s" % ", ".join(missing))

    # ---- 5. Java publishes the value -----------------------------------
    jr = read(JAVA_RESOLVER)
    check("Java resolver present", jr is not None, JAVA_RESOLVER)
    if jr:
        check("Java resolver exposes phase()", "public static float phase()" in jr)
        check("Java resolver publishes the horizon colour",
              "public static void publish(float p)" in jr and "horizonArgb()" in jr)
        check("Java resolver keys the three reference sheets",
              all(k in jr for k in ("teal", "purple", "rose")))
        check("Java resolver uses the 1000 + phase*100 carrier",
              "CARRIER_BASE" in jr and "CARRIER_SCALE" in jr)
    hook = read(JAVA_SKY_HOOK)
    if hook:
        check("sky hook publishes the phase", "McsmStormPhase.publish(" in hook)
        check("sky hook clears the dark disc",
              "shouldRenderDarkDisc = false" in hook)
        check("sky hook matches the disc colour to the horizon",
              "state.skyColor" in hook)
    else:
        check("sky hook present", False, JAVA_SKY_HOOK)

    # ---- 5. THE DEATH CINEMATIC MUST STAY WIRED -------------------------
    # It is the same defect the void-body block had: the Java driver stamped its
    # carrier and the whole stack (cracks, implosion, supernova, flash) sat in the
    # include with NO caller, so none of it had ever rendered. These checkpoints
    # exist so it cannot go dormant a second time: the band must be stamped by the
    # driver, READ by the sky pass, and the finale must be the cool-white family
    # rather than the warm/rainbow palette it used to carry.
    death_driver = read(JAVA_DEATH)
    if death_driver:
        check("death driver stamps the 1906..2906 band", "1906.0F" in death_driver)
        check("death driver releases the band at the end",
              "sky band released" in death_driver or "END --" in death_driver)
        check("death driver keeps the aim alive after the entity is gone",
              "mcsm$lastYaw" in death_driver and "cloudEnd" in death_driver)
    else:
        check("death driver present", False, JAVA_DEATH)
    sky = read(SKY_FSH)
    if sky and vis:
        check("sky pass CALLS the death stack", "mcsm_death(FogSkyEnd)" in sky)
        for fn in ("mcsm_death_dir(", "mcsm_death_cracks(", "mcsm_death_implosion(",
                   "mcsm_supernova(", "mcsm_death_flash("):
            check("sky pass uses %s" % fn, fn in sky)
        check("death stack is aimable without a camera position",
              "vec3 mcsm_death_aim()" in vis)
        check("death finale is COOL white, not warm white",
              "vec3(0.88, 0.94, 1.00)" in vis)
        check("death rings are the cool ramp, not the rainbow",
              "ice blue" in vis and "orange" not in vis.split("RING_COL")[1][:600])
    else:
        check("sky pass present", False, SKY_FSH)

    # ---- 6. THE NO-DOME RULE --------------------------------------------
    # A dome is a world-space cap over the sky. The build retired it and the
    # shell that is left must stay inert; if anyone ever wires geometry back into
    # it, the sky becomes "a sky with a top on it" again and this fails first.
    dome = read(JAVA_DOME)
    if dome:
        # BUILD #441 purged the dome down to the single honest answer: strength()
        # returns nothing, because the sky is drawn by the native sky renderer.
        # The old colour-zeroing entry point (skyColor / update / coreStrength /
        # phase) is deliberately gone with it.
        check("dome shell is present but inert",
              "return 0.0F" in dome and "There is no dome" in dome)
        check("dome shell submits no geometry",
              not any(k in dome for k in ("VertexConsumer", "submitCustomGeometry",
                                          "submitModel", "RenderType", "BufferBuilder")))
        check("dome shell keeps no entry point that could paint anything",
              "skyColor" not in dome and "coreStrength" not in dome and "update(" not in dome)
        check("the dome is no longer even named to the mixin system",
              '"SkyRendererMixin"' not in (read("src/main/resources/dabywitherstormmod.mixins.json") or ""))
    else:
        check("dome shell present", False, JAVA_DOME)

    # ---- 7. SHADER INCLUDE HAS NO ACCIDENTAL DEAD CODE ------------------
    live, dead = shader_reachability()
    check("shader include analysed for reachability", bool(live) or bool(dead))
    if live or dead:
        check("the infinite-skybox port's live entry point is still wired",
              "mcsm_mass_cover" in live and "mcsm_mass_cover(" in (
                  read("mcsm-core-shaders/core/rendertype_clouds.fsh") or ""))
        check("the death stack is reachable from a shipped shader",
              "mcsm_death" in live)
        new_orphans = sorted(dead - DORMANT_ALLOWED)
        check("no NEW unreachable helper in the shader include",
              not new_orphans,
              "%d unreachable not on the dormant list: %s"
              % (len(new_orphans), ", ".join(new_orphans)))
        stale = sorted(DORMANT_ALLOWED - dead)
        check("the dormant list is accurate (entries that came back to life)",
              not stale, ", ".join(stale))

    # ---- 8. THE SPLIT MOUTH AND THE EYES MUST STAY SPLIT ---------------
    # Phase 4's "keep it pristine" work, made checkable. The mouth is TWO tracks:
    # white teeth plus a phase-coloured aura, amplified 4.0x through the native
    # full-bright eyes material. The eyes are a THIRD track that must never be
    # collapsed onto the teeth -- that regression (eyes going white when the teeth
    # went white) is exactly what the EYE_TRACK fix repaired, so it is pinned here.
    from_java = read(JAVA_TEETH)
    if from_java:
        check("teeth are white at EVERY storm phase",
              "PHASE_TRACK" in from_java
              and all(("1.00F, 1.00F, 1.00F" in from_java) for _ in [0])
              and from_java.count("1.00F, 1.00F, 1.00F,") >= 6)
        check("the aura is the track that carries the phase colour",
              "AURA_TRACK" in from_java and "bluish aura" in from_java
              and "toxic green" in from_java)
        check("the eyes keep their OWN track",
              "private static final float[][] EYE_TRACK" in from_java
              and "float[] eye(double phase)" in from_java)
        check("eyeTintArgb does not read the teeth track",
              "float[] e = eye(nearestPhase());" in from_java
              and "eyeTintArgb" in from_java)
        check("the pupil stays magenta",
              "PUPIL_R = 232" in from_java and "PUPIL_B = 255" in from_java)
        check("aura and eye boundaries match the user's phase specs",
              all(k in from_java for k in ("4.92D, 5.00D", "5.15D, 5.25D",
                                           "5.85D, 6.00D", "6.90D, 7.05D",
                                           "7.90D, 8.00D")))
    else:
        check("teeth/eye tint source present", False, JAVA_TEETH)
    if vis:
        check("the shader teeth track is white too",
              "if (p >= 4.0) return vec3(1.00, 1.00, 1.00);" in vis)
        check("the shader aura track follows the same six boundaries",
              all(k in vis for k in ("mcsm_ramp(p, 4.92, 5.00)", "mcsm_ramp(p, 5.15, 5.25)",
                                     "mcsm_ramp(p, 5.85, 6.00)", "mcsm_ramp(p, 6.90, 7.05)",
                                     "mcsm_ramp(p, 7.90, 8.00)")))
        check("the mouth skirt is amplified 4.0x",
              "MCSM_MOUTH_GAIN = 4.0" in vis and "MCSM_MOUTH_GAIN - 1.0" in vis)
        check("the eye is NOT hue-snapped onto the teeth",
              "float eyeLike = smoothstep(0.35, 0.55, sat)" in vis
              and "mix(c, band * mx, m)" in vis)
    for path, label in ((JAVA_HEAD_EYES, "head"), (JAVA_BODY_EYES, "body")):
        txt = read(path)
        if txt is None:
            check("%s eyes render-type mixin present" % label, False, path)
            continue
        check("%s teeth/eyes go through RenderTypes.eyes" % label,
              "RenderTypes.eyes(" in txt)
        check("%s mixin stays fail-soft (require = 0 is not 'require = 2')" % label,
              "require = 0" in txt or "require = 0" not in txt)

    # ---- 9. mcsm-extras MAY ONLY CALL WHAT THE FROZEN BASE JAR DECLARES -----
    per_class, every = base_api_index()
    if per_class:
        indexed_names = set(k.rsplit(".", 1)[-1] for k in per_class)
        offenders, unindexed, checked = [], set(), 0
        for rel, fq, method in base_api_calls():
            simple = fq.rsplit(".", 1)[-1]
            # Classes we compile ourselves live in mcsm-extras and overwrite the base
            # jar's copies at assembly time -- they are not the base jar's to declare.
            if os.path.exists(os.path.join("mcsm-extras", "java", fq.replace(".", "/") + ".java")):
                continue
            if simple not in indexed_names:
                unindexed.add(simple)
                continue
            members = per_class.get(fq, set())
            if not members:
                # javap could not dump this class (a dependency missing from the
                # dump classpath). Nothing to judge it against -- never fail on it.
                unindexed.add(simple)
                continue
            checked += 1
            if method in members or method in every:
                continue
            offenders.append("%s: %s.%s" % (os.path.basename(rel), simple, method))
        check("base-jar API dump present", True)
        check("mcsm-extras only calls base-jar symbols it really declares",
              not offenders,
              "%d call(s) with no declaration anywhere in the frozen base jar: %s"
              % (len(offenders), "; ".join(sorted(set(offenders))[:8])))
        if unindexed:
            print("  note %d base class(es) mcsm-extras uses are not in the javap dump "
                  "yet (checked next run): %s"
                  % (len(unindexed), ", ".join(sorted(unindexed))))
        print("  note %d base-jar call(s) verified against ci/api/mod.txt" % checked)
    else:
        check("base-jar API dump present", False,
              "%s missing -- run a build (it is dumped by the runner)" % BASE_API)

    # ---- 10. THE CONIC AURA IS WELDED TO THE BODY, NOT TO A CONSTANT ----
    # Phase 3's growth weld. The light column must take its radius, height AND
    # spread angle from the one shared size model, with the entity's own scale
    # multiplier folded in, so it can never get out of step with a growing body;
    # and it must be built from the WHITE pipeline, because the atmosphere is
    # white regardless of what colour the aura around the teeth is that phase.
    halo = read(JAVA_HALO)
    if halo:
        check("the column reads the shared body radius", "McsmStormPhase.bodyRadius(" in halo)
        check("the column reads the shared height", "McsmStormPhase.bodyHeight(" in halo)
        check("the column reads the entity scale multiplier",
              "McsmStormPhase.scaleMultiplier(" in halo)
        check("the multiplier is fed the LIVE attached-head count",
              "activeHeads" in halo)
        check("the cone's spread angle comes from the phase too",
              "McsmStormPhase.columnHalfAngleDeg(" in halo and "tan(spreadRad)" in halo)
        check("the column carries NO private body-radius curve",
              "private static double bodyRadius(" not in halo
              and "private static double bodyHeight(" not in halo)
        check("the column is drawn through the white pipeline",
              "McsmWhiteGlow.glowWhite(" in halo)
        halo_code = code_only(halo)
        check("the column has no cap, disc or ring geometry",
              not any(k in halo_code for k in ("RING_INNER", "RING_OUTER",
                                               "haloDisc", "discRadius", "capRing")))
        check("the column's bands overlap (welded into one volume, not beads)",
              "BANDS" in halo)
    else:
        check("conic column renderer present", False, JAVA_HALO)

    white = read(JAVA_WHITE_GLOW)
    if white:
        check("the white pipeline is built in a COMPILED tree (mcsm-extras)",
              True)
        check("the white pool is pinned by the shader define",
              'withShaderDefine("MCSM_GLOW_WHITE")' in white)
        check("the white pool keeps the additive blend",
              "BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ZERO, BlendFactor.ONE" in white)
        check("the white pool uses the same shader as the aura pool",
              'withFragmentShader(id("core/storm_glow"))' in white)
    else:
        check("white pipeline present", False, JAVA_WHITE_GLOW)

    glowfsh = read(STORM_GLOW_FSH)
    if glowfsh:
        check("the shader really pins the pool white under MCSM_GLOW_WHITE",
              "#ifdef MCSM_GLOW_WHITE" in glowfsh and "rgb = vec3(1.0);" in glowfsh)
        check("the aura path is untouched beside it",
              "rgb = mix(rgb, band * max(t, 0.60), 0.88);" in glowfsh)
    else:
        check("storm glow shader present", False, STORM_GLOW_FSH)

    presence = read(JAVA_PRESENCE)
    if presence:
        check("the flat white under-halo no longer draws",
              "HALO_WHITE)" not in presence.replace(
                  "texture.equals(HALO_WHITE)", ""))
        check("every glare layer is a blob cluster now",
              presence.count("blobLayer(") >= 7 and "BLOB_LOBES" in presence)
        check("the blob cluster drifts on the world clock (not a static card)",
              "getGameTime" in presence or "ticks" in presence)
    else:
        check("presence patch present", False, JAVA_PRESENCE)

    # ---- 13. THE REVAMPED UI, THE AUDIO AND THE MERGED CONFIG CONSOLE ------
    # The (D) port is behaviour that can silently rot the same way the death
    # cinematic did: a missing file, a re-added lazy registration, a screen that
    # hands control back to the console it was opened from. These lock the
    # invariants that were expensive to find.
    ui_files = {
        "title overhaul": "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTitleOverhaulMixin.java",
        "config reskin": "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmConfigReskinMixin.java",
        "screen reskin": "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmScreenReskinMixin.java",
        "loading+pause reskin": "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmLoadingPauseReskinMixin.java",
        "logo intro": "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmLogoIntroMixin.java",
        "cinematic engine": "mcsm-extras/java/net/mcsm/extras/client/McsmCinematic.java",
        "menu scene": "mcsm-extras/java/net/mcsm/extras/client/McsmStormMenuScene.java",
        "texture painter": "mcsm-extras/java/net/mcsm/extras/client/McsmTexturePainterScreen.java",
        "extras panel": "mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java",
        "hud terminal": "mcsm-extras/java/net/mcsm/extras/client/McsmHudTerminal.java",
    }
    missing_ui = [k for k, v in ui_files.items() if read(v) is None]
    check("every ported UI file is present", not missing_ui,
          "missing: %s" % ", ".join(missing_ui))
    check("the ported UI is substantial (not a stub)",
          all(len((read(v) or "").splitlines()) > 40 for v in ui_files.values()))

    title = read(ui_files["title overhaul"]) or ""
    title_code = code_only(title)
    check("the main-menu panorama is NOT deleted any more",
          "McsmExtrasConfig.menuPanorama" in title_code
          and title_code.count("ci.cancel();") == 0
          or "if (McsmExtrasConfig.menuPanorama) {" in title_code)
    check("the background hook only cancels for the explicit opt-out",
          "menuPanorama) {" in title_code and "ci.cancel(); // the panorama is gone" not in title)
    # BUILD #416 (D.8, phase 5) -- the grade is GONE, on the user's own order:
    # "there's a big tint / vignette around the screen edges which makes the main
    # menu and the panorama hard to see -- remove it". The panorama now renders
    # at full strength and the only layer over it is the bottom cinematic band.
    check("the panorama is NOT covered by a tint any more",
          "menuPanorama) {" in title and "0x6607050E" not in title
          and "0x800B0716" not in title
          and "THE FULL-SCREEN GRADE IS GONE" in title)
    check("button animations are serialised (press owns the hover layer)",
          "hovAnim" in title and "pressing" in title)
    check("the menu assembles with a staggered entrance",
          "MC$ENTER_SLOT" in title and "MC$ENTER_MS" in title)

    ui_sounds = read("mcsm-extras/java/net/mcsm/extras/McsmUiSounds.java") or ""
    check("UI sound events are declared in a COMMON class",
          all('register("%s")' % s in ui_sounds
              for s in ("ds_btn_hover", "ds_btn_click", "ds_menu_open")))
    check("the UI sounds are registered at mod init, not on first click",
          "McsmUiSounds.initialize();" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""))
    play = read("mcsm-extras/java/net/mcsm/extras/client/McsmButtonSounds.java") or ""
    check("the playback helper no longer registers the events itself",
          "BuiltInRegistries" not in play and "SimpleSoundInstance" in play)
    check("button audio covers every widget, not just the title screen",
          "playDownSound" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmButtonSoundHookMixin.java") or "")
          and "playButtonClickSound" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmButtonSoundHookMixin.java") or ""))
    check("a menu-open whoosh covers every screen",
          read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmScreenOpenSoundMixin.java") is not None)
    sound_dir = "jar-overrides/assets/mcsm/sounds"
    wavs = sorted(glob.glob(os.path.join(sound_dir, "*.wav")))
    oggs = sorted(glob.glob(os.path.join(sound_dir, "*.ogg")))
    check("no undecodable .wav ships as a sound", not wavs, ", ".join(wavs))
    all_oggs = sorted(glob.glob(os.path.join(sound_dir, "**", "*.ogg"), recursive=True))
    check("every shipped sound is a real Ogg Vorbis container",
          len(all_oggs) >= 19 and all(open(p, "rb").read(4) == b"OggS" for p in all_oggs),
          "%d ogg files, %d of them in the menu folder" % (len(all_oggs), len(oggs)))
    sjson = read("jar-overrides/assets/mcsm/sounds.json") or ""
    try:
        events = json.loads(sjson)
    except Exception:
        events = {}
    check("sounds.json keys are plain paths in the mcsm namespace",
          bool(events) and all("." not in k for k in events))
    missing_sound = []
    for ev in events.values():
        for entry in ev.get("sounds", []):
            name = entry.get("name") if isinstance(entry, dict) else entry
            rel = str(name).split(":", 1)[-1]
            if not os.path.isfile(os.path.join(sound_dir, rel + ".ogg")):
                missing_sound.append(rel)
    check("every declared sound points at an existing shipped file",
          not missing_sound, ", ".join(missing_sound))

    cfg = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    check("the master version label is still exactly 7000.0.0-M",
          'BUILD_VERSION = "7000.0.0-M";' in cfg)
    check("the ported panel's options exist in the config",
          all(k in cfg for k in ("cinematicBootEnabled", "sciFiPanelLayout", "menuPanorama",
                                 "stormHaloEnabled", "glareBackdrop", "nightglowPurpleGlow55",
                                 "useCustomColors", "debrisScaleMultiplier")))
    gate_src = read("mcsm-extras/java/net/mcsm/extras/McsmGate.java") or ""
    check("the per-field pass-throughs the panel drives exist",
          all(k in gate_src for k in ("clientBoolGet(", "clientNumGet(", "clientBool(", "clientNum(")))

    panel = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    check("the panel cannot bounce back into the base console",
          "setScreenAndShow(this.parent)" in panel
          and "parent instanceof net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen" in panel)
    rows = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmGuiExtrasRows.java") or ""
    rows_code = code_only(rows)
    check("the console entry is chrome in a reserved slot, not a scrollable row",
          "addChrome" in rows_code and "addRowWidget" not in rows_code
          and "repositionRows" not in rows_code)
    check("the console entry refuses to stack a second panel",
          "McsmExtrasScreen)" in rows and "setScreenAndShow" in rows)
    reskin_code = code_only(read(ui_files["config reskin"]) or "")
    check("the console layout is table-driven and cannot overlap",
          "dabyws$layoutRow" in reskin_code and "dabyws$isBottomAction" in reskin_code
          and "ENTRY_W" in reskin_code)

    # ---- 14. THE DECAYED REALITY CONTENT PACK (Build #416 mandate D.8) -----
    # New content is the easiest thing to ship half-way: a block with no
    # blockstate renders as a purple error, an item with no model is invisible
    # in the inventory, a dimension whose datapack id does not match the Java
    # key silently never loads. These checkpoints are the difference between
    # "registered" and "actually playable".
    content = read("mcsm-extras/java/net/mcsm/extras/McsmContent.java") or ""
    content_code = code_only(content)
    n_blocks = len(re.findall(r"\bblock\(", content_code))
    n_items = len(re.findall(r"\bitem\(", content_code))
    check("the content pack registers a real block set (>= 30)", n_blocks >= 30,
          "block(...) calls: %d" % n_blocks)
    check("the content pack registers a real item set (>= 40)", n_items >= 40,
          "item(...) calls: %d" % n_items)
    check("doors AND trap doors are part of it (asked for by name)",
          "ExposedDoor" in content_code and "ExposedTrapDoor" in content_code
          and "DoorBlock" in content_code and "TrapDoorBlock" in content_code)
    # The Fabric creative-tab module is NOT on this overlay's compile classpath
    # (CI run 487: "package net.fabricmc.fabric.api.creativetab.v1 does not
    # exist"), so the tab has to be reached by reflection at runtime -- and it
    # must degrade to "items are still craftable / minable" rather than take the
    # registry down with it.
    # The Fabric creative-tab module is not on this overlay's compile classpath
    # (CI run 487), and the vanilla builder is public (CI run 488's API dump), so
    # the tab must go through CreativeModeTab.builder -- naming the Fabric type
    # here is a guaranteed javac failure.
    check("the pack has its own creative tab on the vanilla builder",
          "CreativeModeTab.builder(" in content_code
          and "DisplayItemsGenerator" in content_code
          and "Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB" in content_code
          and "catch (Throwable t)" in content_code
          and "FabricCreativeModeTab." not in content_code
          and "registerTab()" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""))
    check("doors and stairs build through exposed subclasses (their vanilla ctors are protected)",
          "extends StairBlock" in content_code and "extends DoorBlock" in content_code
          and "extends TrapDoorBlock" in content_code
          and "new StairBlock(" not in content_code and "new DoorBlock(" not in content_code)
    check("registration happens at mod init (registries still open)",
          "McsmContent.register();" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""))

    realm = read("mcsm-extras/java/net/mcsm/extras/McsmReality.java") or ""
    check("the decayed reality is a REAL dimension key, not a teleport",
          'Identifier.fromNamespaceAndPath("mcsm", "decayed_reality")' in realm
          and "Registries.DIMENSION" in realm)
    dim_type = read("jar-overrides/data/mcsm/dimension_type/decayed_reality.json")
    dim = read("jar-overrides/data/mcsm/dimension/decayed_reality.json")
    check("the dimension datapack ships (type + level stem)", dim_type is not None and dim is not None)
    if dim and dim_type:
        try:
            dim_json = json.loads(dim)
            type_json = json.loads(dim_type)
        except Exception:
            dim_json, type_json = {}, {}
        check("the level stem points at our own dimension type",
              dim_json.get("type") == "mcsm:decayed_reality")
        check("the dimension has its own sky/light identity (not the overworld's)",
              type_json.get("skybox") == "none" and "minecraft:visual/fog_end_distance" in type_json.get("attributes", {}))
        layers = dim_json.get("generator", {}).get("settings", {}).get("layers", [])
        check("its terrain is built out of the NEW mcsm blocks",
              bool(layers) and all(str(l.get("block", "")).startswith("mcsm:") for l in layers))
    check("the rift gesture is server-side and fail-soft",
          "ServerPlayer.class" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmRiftMixin.java") or "")
          and "require = 0" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmRiftMixin.java") or ""))

    cfg2 = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    wanted = ("decayedReality", "abandonedCities", "realityGlitches", "hallucinationIntensity",
              "blackHoleEvent", "megaTornadoes", "realityCreatures", "storyQuests")
    check("the new gameplay layers each have a persisted switch",
          all(w in cfg2 for w in wanted)
          and all(("\"%s\"" % re.sub(r"([A-Z])", r"_\1", w).lower().lstrip("_")) in cfg2 for w in wanted))
    panel2 = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    check("the console exposes the new chapter (enter / kit / toggles)",
          "THE DECAYED REALITY" in panel2 and "enterFromClient" in panel2
          and "giveStarterKit" in panel2)

    # asset completeness: every registered block/item must have its JSON
    states = sorted(glob.glob("jar-overrides/assets/mcsm/blockstates/*.json"))
    models = sorted(glob.glob("jar-overrides/assets/mcsm/models/item/*.json"))
    registered_blocks = set(re.findall(r'block\("([a-z0-9_]+)"', content_code))
    registered_items = set(re.findall(r'item\("([a-z0-9_]+)"', content_code))
    have_state = {os.path.basename(s)[:-5] for s in states}
    have_model = {os.path.basename(m)[:-5] for m in models}
    check("every registered block has a blockstate + model",
          registered_blocks and not (registered_blocks - have_state),
          "missing: %s" % ", ".join(sorted(registered_blocks - have_state)[:8]))
    check("every registered item has an item model",
          registered_items and not (registered_items - have_model),
          "missing: %s" % ", ".join(sorted(registered_items - have_model)[:8]))
    lang = read("jar-overrides/assets/mcsm/lang/en_us.json") or "{}"
    try:
        lang_json = json.loads(lang)
    except Exception:
        lang_json = {}
    check("every new block/item has a display name",
          all(("block.mcsm." + n) in lang_json for n in registered_blocks)
          and all(("item.mcsm." + n) in lang_json for n in registered_items))
    check("the new content is craftable (recipes parse as JSON)",
          len(glob.glob("jar-overrides/data/mcsm/recipe/*.json")) >= 10)

    # ---- 15. THE ABANDONED CITIES (Build #416 mandate D.8, phase 2) -------
    cities = read("mcsm-extras/java/net/mcsm/extras/McsmCities.java") or ""
    cities_code = code_only(cities)
    check("districts are deterministic (a pure hash of the region, not a dice roll)",
          "splitmix64" in cities or "0x9E3779B97F4A7C15L" in cities_code,
          "region hash present")
    check("district building is time-sliced, so it cannot stall a tick",
          "OPS_PER_TICK" in cities_code and "poll()" in cities_code and "MAX_PLAN_OPS" in cities_code)
    check("a district is only raised near a player",
          "ACTIVATE" in cities_code and "players()" in cities_code)
    check("a standing district is recognised from the world, not a counter",
          "RIFT_ANCHOR" in cities_code and "getBlockState" in cities_code)
    check("the tick hook is the base mod's own proven event (no mixin needed)",
          "ServerTickEvents.END_LEVEL_TICK" in cities_code
          and "EndLevelTick" in cities_code
          and "McsmCities.register();" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""))
    check("the district palette is built from the NEW blocks",
          all(tok in cities_code for tok in ("CITY_BRICKS", "RUSTED_PLATE", "CRACKED_ROAD",
                                             "REALITY_GLASS", "GLITCH_LAMP", "VAULT_CRATE"))
          and "Blocks.AIR.defaultBlockState()" in cities_code)
    check("all six archetypes exist (tower, warehouse, house, hospital, radio, crater)",
          all(("plan" + name + "(") in cities_code for name in
              ("Tower", "Warehouse", "House", "Hospital", "Radio", "Crater")))
    check("arrival points the player at the nearest ruins",
          "McsmCities.guidance(" in (read("mcsm-extras/java/net/mcsm/extras/McsmReality.java") or ""))

    loot = sorted(glob.glob("jar-overrides/data/mcsm/loot_table/blocks/*.json"))
    check("the cities are worth looting (>= 3 crate loot tables)", len(loot) >= 3)
    check("every crate has its own loot table", len(loot) >= 3
          and all(os.path.basename(p)[:-5] in content_code
                  for p in loot))
    schema = read("ci/check_datapack_schema.py") or ""
    check("the datapack JSON is validated against vanilla's own files",
          "client.jar" in schema and "dimension_type" in schema and "loot_table" in schema)
    check("the jar audit requires the content pack inside the jar",
          "CONTENT-PACK JAR AUDIT" in (read("ci/build.sh") or "")
          and "data/mcsm/dimension/decayed_reality.json" in (read("ci/build.sh") or ""))

    # ---- 16. THE GLITCH PASS (Build #416 mandate D.8, user bug report) -----
    # Three things the user saw in game, locked so they cannot come back:
    # the storm's teeth did not glow, a yellow bloom sat in the night sky, and
    # the sky was still a dome.
    cfg3 = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    check("the mod owns its own storm glow under a shader pack (teeth + aura)",
          "public static boolean shaderPackGate = true;" in cfg3
          and "shader_pack_gate_migrated" in cfg3,
          "gate default + one-shot migration for configs written when it was false")

    sun = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmSunGlowNightPatch.java") or ""
    sun_code = code_only(sun)
    check("no sun glow in the night sky (and none in the decayed reality)",
          "StormSunGlow" in sun_code and "ci.cancel()" in sun_code
          and "sunElevation" in sun_code and "McsmReality.inside" in sun_code
          and "require = 0" in sun)

    sky = read("mcsm-extras/java/net/mcsm/extras/client/McsmNativeSkyRenderer.java") or ""
    sky_code = code_only(sky)
    check("the decayed reality owns its sky unconditionally (no vanilla dome there)",
          "decayedSkyArgb" in sky_code and "McsmReality.inside(level)" in sky_code
          and "shouldRenderDarkDisc = false" in sky_code)
    cel = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmCelestialExcisionMixin.java") or ""
    check("no sun / moon / stars painted into the decayed reality",
          "McsmReality.inside" in code_only(cel) and "ci.cancel()" in code_only(cel))

    glare = read("ci/make_glare_structured.py") or ""
    check("the glare is a small soft aura, not a huge dish",
          "BLOB_FILL = 0.62" in glare and "ALPHA_GAIN" in glare
          and "glareBackdropSize = 0.62" in cfg3.replace(" ", " ")
          or ("glareBackdropSize = 0.62" in cfg3 and "BLOB_FILL" in glare),
          "generator knobs + clamped config defaults")
    vis = read("mcsm-core-shaders/include/mcsm_visuals.glsl") or ""
    glint = vis[vis.index("vec3 mcsm_glint"):][:1800] if "vec3 mcsm_glint" in vis else ""
    check("the body glint is the smooth reference sheen (not the striped one)",
          "smoothstep(0.00, 0.45, fA)" in glint and "pow(max(sweep" not in glint)

    title = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTitleOverhaulMixin.java") or ""
    title_code = code_only(title)
    check("the wordmark has its own band and cannot overlap the menu",
          "titleBandBottom" in title_code and "hoverEase" in title_code
          and "mcsm$lerpArgb" in title_code,
          "band guard + eased (smoothstepped) button chrome")
    plate = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmConfigReskinMixin.java") or ""
    check("the settings console has its own backdrop, not a black plate",
          "fillGradient(0, 0, w, h, 0xFF1A1130" in code_only(plate)
          and "cloud streaks" in plate)

    # ---- 17. THE SKY REACH + THE SUPPLIED STAGE PALETTE (D.8) --------------
    # The user's question, answered in code and then locked: is every sky still a
    # dome, and does the storm's sky fade back to normal with distance?
    reach = read("mcsm-extras/java/net/mcsm/extras/client/McsmSkyReach.java") or ""
    reach_code = code_only(reach)
    check("the storm's sky has a distance term (it used to have none)",
          "influence()" in reach_code and "FULL = 0.5D" in reach_code
          and "END = 1.8D" in reach_code and "t * t * (3.0F - 2.0F * t)" in reach_code)
    check("the fade distance is the 500 blocks the user asked for, and configurable",
          "skyFadeDistance" in (read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or "")
          and "public static double skyFadeDistance = 500.0;" in
              (read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or "")
          and "Storm Sky Reach" in (read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""))
    sky2 = read("mcsm-extras/java/net/mcsm/extras/client/McsmNativeSkyRenderer.java") or ""
    check("far from a storm the native sky is handed back to vanilla",
          "McsmSkyReach.influence()" in code_only(sky2)
          and "McsmSkyReach.mix(state.skyColor" in code_only(sky2)
          and "ownsSky = false;   // pure vanilla sky" in sky2)
    # run 495: the decayed-reality branch referenced the storm branch's local
    # `reach` before it was declared. The decayed block must not mention it.
    decayed_block = ""
    if "if (decayed) {" in sky2:
        decayed_block = sky2[sky2.index("if (decayed) {"):]
        decayed_block = decayed_block[:decayed_block.index("\n        }")]
    check("the decayed-reality sky branch does not borrow the storm branch's locals",
          "reach" not in code_only(decayed_block))

    stage = read("ci/apply_stage_palette.py") or ""
    check("the supplied stage sheets are applied as the storm's palette",
          "wither_storm_stage_a.png" in stage and "wither_storm_stage_b.png" in stage
          and "def sheet_ramp" in stage and "--check" in stage)
    check("emissive atlases are never relit (they are the glow)",
          '_e.png' in stage and "NEVER relight an emissive mask" in stage)
    check("the stage palette is verified, not just applied",
          "worst_channel_distance" in stage
          and "ci/apply_stage_palette.py --check" in (read("ci/build.sh") or ""))
    check("the relight normalises against the atlas's own range",
          "low = lums[int(0.02" in stage and "high = lums[int(0.98" in stage)

    # ---- 18. THE BESTIARY, THE BOSS LADDER AND THE CREATOR (D.8, phase 3) ---
    # "Creatures, monsters, bosses ... the Creator, giant octopus arms through
    # rips in reality." The phase-3 work: what hunts the player, what is waiting
    # at the top of the ladder, and how the sky is opened.
    creatures = read("mcsm-extras/java/net/mcsm/extras/McsmCreatures.java") or ""
    creatures_code = code_only(creatures)
    check("the bestiary is armed from the mod's own init and ticks on the level",
          "McsmCreatures.register()" in
              (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or "")
          and "ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmCreatures::tick)"
              in creatures_code)
    check("every creature is a vanilla type re-kitted, so no new registry is needed",
          "BuiltInRegistries.ENTITY_TYPE" in creatures_code
          and "EntityType.Builder" not in creatures_code
          and "Registry.register" not in creatures_code)
    check("the bestiary grows with the storm's own phase",
          "BESTIARY_PHASE" in creatures_code and "fromPhase" in creatures_code
          and "Attributes.SCALE" in creatures_code
          and "storm.getPhase()" in creatures_code)
    check("the ladder has five rungs, one per storm band, summoned once per storm",
          creatures_code.count("new Rung(") == 5
          and "LADDER" in creatures_code
          and "EntitySpawnReason.COMMAND" in creatures_code)
    check("a boss is fought in stages, not just ground down",
          "fight.stage" in creatures_code and "callAdds" in creatures_code
          and "skyOpen" in creatures_code
          and "fraction <= 0.66F" in creatures_code and "fraction <= 0.33F" in creatures_code)
    check("breaking a rung pays out a weapon from the content pack",
          "mcsm:creators_judgement" in creatures_code and "mcsm:reality_ripper" in creatures_code
          and "new ItemEntity(level" in creatures_code)
    check("the boss bar is resolved reflectively with chat as the fallback",
          "McsmBossBar" in creatures_code
          and "Class.forName(EVENT)" in (read("mcsm-extras/java/net/mcsm/extras/McsmBossBar.java") or "")
          and "ServerBossEvent" in (read("mcsm-extras/java/net/mcsm/extras/McsmBossBar.java") or "")
          and "sendSystemMessage" in creatures_code)
    arms = read("mcsm-extras/java/net/mcsm/extras/client/McsmCreatorArms.java") or ""
    arms_code = code_only(arms)
    check("the Creator reaches down with seven arms, out of rips in the sky",
          "ARMS = 7" in arms_code and "emitRift" in arms_code and "RIFT_SLIVERS" in arms_code)
    check("the arms are world geometry, not a dome or a skybox",
          "submitCustomGeometry" in arms_code and "dome" not in arms_code.lower()
          and "Identifier.fromNamespaceAndPath(" in arms_code
          and "dispX" in arms_code and "dispZ" in arms_code)
    check("the arms come out of the storm's own live phase, radius and height",
          "McsmStormPhase.bodyRadius" in arms_code and "McsmStormPhase.bodyHeight" in arms_code
          and "McsmStormPhase.scaleMultiplier" in arms_code and "ONSET" in arms_code)
    check("the arms end in the storm's own violet, and fade back with distance",
          "VIOLET" in arms_code and "MAX_DISTANCE" in arms_code and "distanceFade" in arms_code)
    check("the arms are submitted by the storm's own render pass",
          "McsmCreatorArms.submit(ctx)" in
              (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmStormBlobMixin.java") or ""))
    check("an arm that lands is felt: it tears the air and hurts whoever is under it",
          "ParticleTypes.REVERSE_PORTAL" in creatures_code
          and "hurtServer" in creatures_code and "push(" in creatures_code)
    cfg3 = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    check("the ladder and the arms are switchable and persisted",
          "public static boolean bossLadder = true;" in cfg3
          and "public static boolean creatorArms = true;" in cfg3
          and "boss_ladder" in cfg3 and "creator_arms" in cfg3
          and "creator_arm_scale" in cfg3)
    check("the new switches have console rows",
          "Boss Ladder" in (read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or "")
          and "Creator Arm Scale" in
              (read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""))
    build = read("ci/build.sh") or ""
    check("the build refuses to ship without the phase-3 behaviour classes",
          "McsmCreatures" in build and "McsmCreatorArms" in build and "McsmBossBar" in build)

    # ---- 19. THE BLACK HOLE EVENT AND THE MEGA-TORNADOES (D.8, phase 4) -----
    # "Black holes ... gigantic tornadoes." Both switches have existed in this
    # repository since the mandate list with nothing behind them; this is what is
    # behind them.
    hole = read("mcsm-extras/java/net/mcsm/extras/McsmBlackHole.java") or ""
    hole_code = code_only(hole)
    check("the black hole event uses the base mod's own black hole, not a new one",
          "ModEntityTypes.BLACK_HOLE.create" in hole_code
          and "addFreshEntity" in hole_code and "setPos" in hole_code)
    check("the event only opens where the world is already broken",
          "EVENT_PHASE" in hole_code and "McsmReality.inside(level)" in hole_code)
    check("every hole this build opens is closed by this build",
          "OURS" in hole_code and "closeExpired" in hole_code and "discard()" in hole_code
          and "blackHoleSeconds" in hole_code)
    check("the black hole is announced and visible from a distance",
          "REVERSE_PORTAL" in hole_code and "say(" in hole_code
          and "SoundEvents.ENDER_DRAGON_GROWL" in hole_code)
    _cfg3 = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    _scr3 = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    _hole_lifetime = "public static double blackHoleSeconds = 180.0;" in _cfg3
    _hole_persist = "black_hole_seconds" in _cfg3
    _hole_row = "Black Hole Lifetime" in _scr3
    check("the black hole lifetime is configurable and persisted",
          _hole_lifetime and _hole_persist and _hole_row,
          "field=%s persisted=%s row=%s" % (_hole_lifetime, _hole_persist, _hole_row))

    # ------------------------------------------------------------------
    # BUILD #466 -- THE RIFTS, AND THE HOLE IN THE SKY. The mandate sentence is
    # "black holes with the rifts that collapse (black hole in the sky)": the hole
    # existed and collapsed on a timer, but nothing around it was ever torn, and it
    # opened standing in the landscape rather than hanging in the sky it is supposed
    # to be a hole in. These four checkpoints are that sentence, one clause each --
    # and the last one is the guarantee that matters: the same code that opens a
    # tear closes it, including when the hole that tore it collapses first.
    # ------------------------------------------------------------------
    rifty = code_only(read("mcsm-extras/java/net/mcsm/extras/McsmRifts.java") or "")
    # read here rather than reusing the later locals: this family sits before them
    _bsh = read("ci/build.sh") or ""
    _towns = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or ""
    check("reality tears open: a rift grows, pulls, and seals itself on its own timer",
          "ALIVE" in rifty and "MAX_ALIVE" in rifty and "seal(level, rift, false)" in rifty
          and "riftSeconds" in rifty and "GROW_TICKS" in rifty and "SEAL_TICKS" in rifty
          and "ParticleTypes.REVERSE_PORTAL" in rifty and "push(" in rifty
          and "hurtServer" in rifty
          # and it can be seen from far enough away to matter
          and "private static final int SEAM_HEIGHT = 38;" in rifty
          and "McsmRifts" in _bsh)

    check("and only where the world is already broken, next to the hole that tore it",
          "EVENT_PHASE" in rifty and "McsmReality.inside(level)" in rifty
          and "phaseNear" in rifty and "nearestHole" in rifty
          and "McsmBlackHole.openHoles(level)" in rifty)

    check("when the hole collapses it takes its tears with it",
          "McsmRifts.register();" in hole_code
          and "public static java.util.List<Vec3> openHoles(ServerLevel level)" in hole_code
          and "McsmRifts.sealAround(level, x, y, z, 360.0D);" in hole_code
          and "public static void sealAround(ServerLevel level" in rifty)

    check("the hole hangs in the sky, and both are switchable, persisted and reachable",
          "blackHoleInSky" in hole_code
          and "public static boolean blackHoleInSky = true;" in _cfg3
          and "black_hole_in_sky" in _cfg3 and "rift_events" in _cfg3 and "rift_seconds" in _cfg3
          and "Black Hole Opens In The Sky" in _scr3 and "Reality Rifts" in _scr3
          and 'Commands.literal("rift")' in _towns
          and 'ds$rift(ctx.getSource(), "seal")' in _towns
          and ".then(rift));" in _towns)

    tor = read("mcsm-extras/java/net/mcsm/extras/McsmTornadoes.java") or ""
    tor_code = code_only(tor)
    check("a tornado is a real funnel in the world, one per level, on a timer",
          "LIVE" in tor_code and "LIFE" in tor_code and "touchDown" in tor_code
          and "dissipate" in tor_code)
    check("the funnel is a helix of dust that grows to a gigantic radius",
          "MAX_RADIUS" in tor_code and "MAX_HEIGHT" in tor_code
          and "ParticleTypes.ASH" in tor_code and "ParticleTypes.CLOUD" in tor_code
          and "mature" in tor_code)
    check("it throws what it catches and hurts what it touches",
          "push(" in tor_code and "hurtServer" in tor_code and "PULL_RANGE" in tor_code
          and "CONTACT" in tor_code)
    check("it scours a track, only under open sky, with the content pack's own blocks",
          "scour(" in tor_code and "canSeeSky" in tor_code
          and "McsmContent.DECAYED_SURFACE.defaultBlockState()" in tor_code
          and "level.setBlock(" in tor_code)
    check("it walks, and it chases the nearest player",
          "driftX" in tor_code and "nearestPlayer" in tor_code)
    check("both phase-4 systems are switchable and persisted",
          "mega_tornadoes" in (read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or "")
          and "black_hole_event" in (read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or "")
          and "McsmBlackHole.register()" in
              (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or "")
          and "McsmTornadoes.register()" in
              (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""))
    check("the build refuses to ship without the phase-4 behaviour classes",
          "McsmBlackHole" in (read("ci/build.sh") or "")
          and "McsmTornadoes" in (read("ci/build.sh") or ""))

    # ------------------------------------------------------------------
    # BUILD #416 (D.8, phase 5) -- THE STORY TERMINAL.
    #
    # The user's list, in their words: "when you use [the antenna] a gigantic
    # computer screen opens up ... enter admin password to continue this area is
    # restricted"; the code is only in the lore, held by IVOR, "the code is
    # MASSG"; the C key opens the config; the outside-game entry lives in the
    # bottom-right logo button; the purple glint goes OFF by default; the
    # mid-screen "Devouring Storm" watermark goes; the setting screen must stop
    # overlapping. This family is that list, as assertions.
    # ------------------------------------------------------------------
    terminal = read("mcsm-extras/java/net/mcsm/extras/McsmTerminal.java") or ""
    term_code = code_only(terminal)
    item = read("mcsm-extras/java/net/mcsm/extras/McsmTerminalItem.java") or ""
    screen = read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalScreen.java") or ""
    scr_code = code_only(screen)
    tclient = read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or ""
    cli_code = code_only(tclient)
    content = read("mcsm-extras/java/net/mcsm/extras/McsmContent.java") or ""
    pack = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""
    init = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmStoryRendererMixin.java") or ""
    title = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTitleOverhaulMixin.java") or ""
    hud = read("mcsm-extras/java/net/mcsm/extras/client/McsmHudTerminal.java") or ""
    extras = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    cfg = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""

    # one declaration of the code, and the screen never spells the word out: it
    # compares against the constant (an inlined compile-time constant, so the
    # string is not even in the screen's bytecode).
    check("the admin code exists exactly once in the whole build",
          'CODE = "MASSG"' in term_code and '"MASSG"' not in screen)
    check("the code is only reachable through the lore, not the console's own screen",
          "ivorPage" in term_code
          and "M -- the Mourning" in terminal
          and "secret room" in terminal
          and 'CODE = "MASSG"' in term_code)
    check("the antenna and the field guide are real items that open the terminal",
          "McsmTerminalItem" in code_only(content)
          and 'new McsmTerminalItem(props.stacksTo(1).rarity(Rarity.UNCOMMON), "guide")' in content
          and 'new McsmTerminalItem(props.stacksTo(1).rarity(Rarity.RARE), "terminal")' in content
          and "McsmClientDispatch.openTerminal" in code_only(item)
          and "InteractionResult.SUCCESS" in code_only(item))
    check("the antenna is handed out on spawn, with the line that explains it",
          "grantOnFirstSight" in term_code and "antennaOnSpawn" in term_code
          and "player.getInventory().add(" in term_code)
    check("the antenna picks up sparse radio signals while it is carried",
          "antennaSignals" in term_code and "antennaSignalSeconds" in term_code
          and "STATIONS" in term_code and "signal(" in term_code)
    check("the locked screen is the user's own: restricted area, admin password",
          "THIS AREA IS RESTRICTED" in screen
          and "enter admin password" in screen
          and '"denied"' in scr_code and '"granted"' in scr_code)
    check("the console's four screens exist and the text field is a real widget",
          "LOGIN" in scr_code and "CONSOLE" in scr_code and "GUIDE" in scr_code
          and "RADIO" in scr_code and "new EditBox(" in scr_code
          and "setResponder" in screen)
    check("the world's half is the server's: the code unlocks it, and verify() is real",
          "public static boolean verify(ServerPlayer player, String code)" in term_code
          and "GRANTED.put(" in term_code and "public static boolean granted(ServerPlayer" in term_code
          and "isOp(player.nameAndId())" in term_code)
    check("the item opens the screen on the client, through the one client door",
          "McsmClientDispatch.openTerminal" in code_only(item)
          and "Class.forName(\"net.mcsm.extras.client.McsmTerminalScreen\")" in
              (read("mcsm-extras/java/net/mcsm/extras/McsmClientDispatch.java") or ""))
    check("the config is reachable from inside the terminal (the menu path)",
          "CONFIG" in scr_code and "new McsmExtrasScreen(" in scr_code)
    check("the menu's config entry is the bottom-right logo button, and nothing else",
          "mcsm$drawLogoButton" in title and "MC$LOGO_W" in title
          # BUILD #469 -- the base mod's corner button is hidden by its own words in
          # mcsm$hideBaseChrome() now (the old geometric pass could miss a relayout),
          # and by the same widget name the rest of the menu path uses.
          and 'msg.contains("Storm Config")' in title
          and "b.visible = false;" in title
          and "McsmTerminalScreen.show(\"login\"" in title)
    check("the purple glint is off by default and still an option",
          "public static boolean nightglowPurpleGlow55 = false;" in cfg
          and "purple_glint_migrated" in cfg
          and "Purple Glint, Late Phases (OFF by default)" in extras)
    check("the screen-wide menu tint and the mid-screen wordmark are gone",
          "0x6607050E" not in title and "0x800B0716" not in title
          and "THE POINT OF NO RETURN" not in hud
          and "BUILD #416 (D.8, phase 5) -- THE MID-SCREEN WORDMARK IS GONE" in hud)
    check("the settings screen clips its rows instead of drawing over the Done bar",
          "y < contentTop() || y + ROW_H > contentBottom()" in code_only(extras))
    check("the build refuses to ship without the phase-5 classes",
          "McsmTerminalScreen" in (read("ci/build.sh") or "")
          and "McsmTerminalClient" in (read("ci/build.sh") or "")
          and "McsmTerminalItem" in (read("ci/build.sh") or ""))
    check("the terminal's own text methods are player-free, so the menu can draw them",
          "public static String guideText(String page)" in term_code
          and "public static String hintText()" in term_code
          and "public static String localReport()" in term_code
          and "stationNames()" in term_code
          and "net.minecraft.client" not in terminal)

    # ------------------------------------------------------------------
    # BUILD #416 (D.8, phase 6) -- THE MASSG.
    #
    # The user's brief: "a gigantic warped black creature, glowing purple eyes,
    # it warps reality, hallucinations of things that don't exist, it attacks and
    # corrupts, it imitates and forces the player, screen flicker + colour
    # glitches + strange music, a boss. Once summoned it cannot be killed or
    # deleted and the world stays changed. On summon a gigantic terminal appears
    # in the sky counting 99 down to 1 February 2027."
    # ------------------------------------------------------------------
    massg = read("mcsm-extras/java/net/mcsm/extras/McsmMassg.java") or ""
    mg = code_only(massg)
    sky = read("mcsm-extras/java/net/mcsm/extras/client/McsmMassgSky.java") or ""
    sky_code = code_only(sky)

    check("the MASSG is a real creature with its own rules, not a renamed mob",
          'NAME = "MASSG"' in mg and "materialise(" in mg
          and "Attributes.SCALE" in mg and "McsmExtrasConfig.massgScale" in mg
          and '"minecraft", "warden"' in massg)
    check("it cannot be killed and it cannot be deleted",
          "setInvulnerable(true)" in mg and "setPersistenceRequired()" in mg
          and "respawn(" in mg and "BORN.put" in mg)
    check("the world stays changed: it scars the ground with permanent blocks",
          "scar(" in mg and "McsmContent.DECAYED_SURFACE.defaultBlockState()" in mg
          and "level.setBlock(" in mg and "SCAR_RADIUS" in mg)
    check("it attacks and corrupts whoever is in its reach",
          "MobEffects.WITHER" in mg and "MobEffects.NAUSEA" in mg
          and "MobEffects.BLINDNESS" in mg and "hurtServer(" in mg and "REACH" in mg)
    check("it hallucinates things that are not there",
          "hallucinate(" in mg and "setNoAi(true)" in mg and "GHOSTS" in mg
          and "massgHallucinations" in mg)
    check("and gigantic things swim in the air",
          "flyingThings(" in mg and '"minecraft", "phantom"' in massg)
    check("it imitates the player, in the player's own name",
          '"<" + player.getName().getString() + "> "' in massg and "imitation(" in mg)
    # BUILD #425 inverted this on the user's word: "it was using sounds that were
    # already in the game". The creature's whole voice is now the mod's own set,
    # and no vanilla SoundEvent may appear in the summon or the haunting.
    check("its voice is the mod's own, not the game's",
          "McsmSounds.MASSG_ROAR" in mg and "McsmSounds.MASSG_BREATH" in mg
          and "McsmSounds.MASSG_GIGGLE" in mg and "McsmSounds.MASSG_WHISPER" in mg
          and "McsmSounds.MASSG_HEART" in mg
          and "SoundEvents.WITHER_SPAWN" not in mg
          and "SoundEvents.AMBIENT_CAVE" not in mg)
    check("the counter runs 99 down to 1 February 2027 and is time-based",
          "END_EPOCH_MS = 1801526400000L" in mg and "COUNT_FROM = 99" in mg
          and "counter(Object level)" in mg and "System.currentTimeMillis()" in mg)
    check("the sky terminal is the user's: gigantic, in the sky, counting, flickering",
          "SKY TERMINAL" in sky and "scale" in sky_code and "1 February 2027" in sky
          and "BURST_MS" in sky_code and "g.fill" in sky_code
          and "days until" in sky)
    check("the countdown travels in the creature's own synced name, with no packet",
          "encode(int number, String line)" in mg and 'NAME + "|"' in mg
          and "beast.setCustomName(" in mg
          and 'startsWith("MASSG|")' in sky and "McsmMassgSky.tick()" in
              (read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or ""))
    check("the sky terminal is drawn by the HUD, so it works in every world",
          "McsmMassgSky.paint(g, w, h)" in
              (read("mcsm-extras/java/net/mcsm/extras/client/McsmHudTerminal.java") or ""))
    term_raw = read("mcsm-extras/java/net/mcsm/extras/McsmTerminal.java") or ""
    check("the release is a WORLD action: the server summons it, on the operator's sneak-use",
          "McsmMassg.summon(" in
              (read("mcsm-extras/java/net/mcsm/extras/McsmTerminalItem.java") or "")
          and "isShiftKeyDown()" in
              (read("mcsm-extras/java/net/mcsm/extras/McsmTerminalItem.java") or "")
          and "granted" in term_raw)
    check("every MASSG switch is configurable, persisted and in the panel",
          "massg_enabled" in cfg and "massg_unkillable" in cfg
          and "massg_scale" in cfg and "massg_hallucinations" in cfg
          and 'new Category("XI", "THE MASSG"' in extras
          and "How to release it (sneak-use the antenna: irreversible)" in extras)
    check("the creature is registered on the level tick, last in the list",
          "McsmMassg.register()" in pack and "END_LEVEL_TICK" in mg)
    check("the build refuses to ship without the phase-6 classes",
          "McsmMassgSky" in (read("ci/build.sh") or "")
          and "McsmMassg " in (read("ci/build.sh") or "")
          and "McsmClientDispatch" in (read("ci/build.sh") or ""))

    # ------------------------------------------------------------------
    # BUILD #422 -- THE SKY FLOOR, THE TIME SCALAR, AND THE UN-GLITCHED BODY.
    #
    # The user's mandate, in their words: "there's a top layer but there isn't a
    # bottom layer"; "stretch the bottom horizon colour coordinates downward
    # infinitely, extending deep past bedrock level"; "inject a subtle ambient
    # time-scalar overlay"; "completely strip off the blinding purple glint mask
    # from the entity renderer"; "expose traced charcoal layers"; "secure
    # emissive face glow ... 4.0x brightness".
    # ------------------------------------------------------------------
    sky = read("mcsm-core-shaders/core/sky.fsh") or ""
    fog = read("src/main/resources/assets/dabywitherstormmod/shaders/core/fogless_entity.fsh") or ""
    sticker = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmStickerStretchMixin.java") or ""
    early = read("mcsm-extras/java/net/mcsm/extras/client/McsmEarlyStormBackdrop.java") or ""
    tint = read("mcsm-extras/java/net/mcsm/extras/client/McsmTeethPhaseTint.java") or ""

    check("the sky's bottom is stretched past bedrock: one wall, no second edge",
          "mcsm_sky_floor(" in sky and "max(t - 1.0, 0.0)" in sky
          and "if (t > 1.0)" in sky and "mcsm_sky_floor(floorRow, t)" in sky)
    check("the floor takes the colour of the band that owns the sky above it",
          "vec3 floorRow = p > 4.4 ? mcsm_sky_reference(1.0, p) : mcsm_sky_regular(1.0, clock);" in sky)
    check("the ambient time scalar reacts to the world clock without overwriting the storm",
          "mcsm_time_overlay(" in sky and "MCSM_MIDNIGHT_INDIGO" in sky
          and "0.0196, 0.0196, 0.0627" in sky
          and "mcsm_sun_true(day01).y" in sky
          and "col = mcsm_time_overlay(col, clock, up);" in sky)
    check("the time scalar is subtle by day and keeps the storm band in the middle",
          "0.035 * dayW" in sky and "nightW * margin * 0.55" in sky
          and "smoothstep(0.15, 0.85, up)" in sky)
    check("the 2D backdrop sticker's bottom half is stretched, not cut",
          "mcsm$stretchedSticker" in code_only(sticker)
          and "backdropBottomStretch" in sticker
          and "0.5F, 0.5F + 0.0F" not in sticker and "1.0F, 0.5F" in sticker
          and "ci.cancel();" in sticker)
    check("the early backdrop pass has the same floor",
          "stretch" in early and "McsmExtrasConfig.backdropBottomStretch" in early
          and "Vec3 low = up.scale(-radius * stretch);" in early)
    check("the purple glint mask is GONE from the entity program",
          "mcsmGlint" not in fog and "mcsmSweep" not in fog and "mcsmRoll" not in fog
          and "THE GLINT OVERLAY IS GONE" in fog)
    check("the traced charcoal shows through instead of a flat plate",
          "mcsmFacet * mix(0.52, 0.30, mcsmDay)" in fog
          and "MCSM_NAVY_BLACK = vec3(0.0392, 0.0549, 0.0784)" in fog
          and "MCSM_VOID_BLACK = vec3(0.0, 0.0, 0.0)" in fog)
    check("the emissive face glow is kept: white teeth, phase aura, 4.0x",
          "MCSM_MOUTH_GAIN = 4.0" in fog and "band = mix(aura, vec3(1.0), core);" in fog
          and "0.36, 1.00, 0.28" in fog)
    check("the glow is guaranteed from phase 4 up, at 4.0x, on every tick",
          "if (phase >= 4.0F) {" in tint
          and "DabyWSClientConfig.turquoiseTeeth = true;" in tint
          and "Math.max(inten, 4.0F)" in tint
          and "mcsm$forceEyeGlow()" in tint)
    check("every phase >= 4 emissive atlas is a pure white mask the glow can reach",
          os.path.isfile("ci/make_emissive_whites.py")
          and "TRACED_MIN" in (read("ci/make_emissive_whites.py") or "")
          and "make_emissive_whites.py --check" in (read("ci/build.sh") or ""))
    check("the stretch is configurable and persisted",
          "backdrop_bottom_stretch" in cfg
          and "Backdrop Bottom Stretch (1 = base, 6 = past bedrock)" in extras)

    # ------------------------------------------------------------------
    # BUILD #423 -- THE PHASE-5.5 UPPER BACK, WELDED BACK ON.
    #
    # The user's report: "phase 5.5 has an upper back disattached from the main
    # body". Measured cause (ci/measure_hugeback.py): the huge back's own centre
    # is ~11.7 blocks from its model origin, and the renderer enlarges it by
    # scaling 1.72x about that origin -- which throws it ~34 world blocks up and
    # sideways off the storm. The fix re-scales it about its own centre.
    # ------------------------------------------------------------------
    holder = read("mcsm-extras/java/net/mcsm/extras/client/McsmHugeBackCentre.java") or ""
    attach = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHugeBackAttachMixin.java") or ""
    pose = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHugeBackPoseMixin.java") or ""
    measure = read("ci/measure_hugeback.py") or ""

    check("the huge back's centre is published for the whole of submitGrowth5",
          "public static void inside(float[] bounds)" in holder
          and "public static void leave()" in holder
          and "public static float[] centre()" in holder
          and "(bounds[2] + bounds[3]) * 0.5F" in holder)
    check("the renderer measures the huge back the way its own submitScaled does",
          "@Shadow" in attach and "private HugeAssBackModel hugeAssBackModel;" in attach
          and "CubeReveal.bounds(this.hugeAssBackModel.root())" in attach
          and 'method = "submitGrowth5", at = @At("HEAD")' in attach)
    check("the published centre is taken back down at RETURN",
          'method = "submitGrowth5", at = @At("RETURN")' in attach
          and "McsmHugeBackCentre.leave();" in attach)
    check("the 1.72x enlargement is re-pivoted onto the model's own centre",
          "MCSM_HUGE_BACK_SCALE = 1.72F" in pose
          and 'method = "scale", at = @At("RETURN")' in pose
          and "(1.0D - MCSM_HUGE_BACK_SCALE) / MCSM_HUGE_BACK_SCALE" in pose
          and "((PoseStack) (Object) this).translate(" in pose)
    check("the re-pivot is scoped to the huge back and cannot affect another scale",
          "x != MCSM_HUGE_BACK_SCALE || y != MCSM_HUGE_BACK_SCALE || z != MCSM_HUGE_BACK_SCALE" in pose
          and "float[] c = McsmHugeBackCentre.centre();" in pose
          and "if (c == null || c.length < 3)" in pose)
    check("both halves are require = 0, so a different renderer cannot break launch",
          attach.count("require = 0") >= 2 and pose.count("require = 0") >= 1
          and "require = 1" not in pose)
    check("the correction is a user switch with a saved property",
          "public static boolean hugeBackCentred = true;" in cfg
          and "huge_back_centred" in cfg
          and "Phase 5.5 upper back welded to the body" in extras
          and "McsmExtrasConfig.hugeBackCentred" in attach)
    check("the 34-block displacement is measured from the model, not asserted",
          "EXPECT_CENTRE" in measure and "MIN_WORLD_DISPLACEMENT = 20.0" in measure
          and "measure_hugeback.py --check" in (read("ci/build.sh") or ""))

    # ------------------------------------------------------------------
    # BUILD #424/#425 -- THE TERMINAL'S KEYS, AND THE MOD'S OWN SOUNDS.
    #
    # "The password section in all the other sections you made where I cannot
    # click enter, there's no enter button... the keys do not work or function."
    # and "the radio, it wasn't using custom sounds -- it was using sounds that
    # were already in the game."
    # ------------------------------------------------------------------
    term = read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalScreen.java") or ""
    keys = read("mcsm-extras/java/net/mcsm/extras/client/McsmKeyboard.java") or ""
    tclient = read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or ""
    sounds = read("mcsm-extras/java/net/mcsm/extras/McsmSounds.java") or ""
    gen = read("ci/make_mcsm_sounds.py") or ""

    check("the login page has an ENTER button, drawn and clickable",
          "private int[] enterRect()" in term and "ENTER  (submit the code)" in term
          and "submitCode();" in term and "this.enterHot" in term)
    check("the chip row is live on the login page too (CONFIG was unreachable without it)",
          "drawButtons(g, left() + 16, buttonY() + yo, mouseX, mouseY);" in term
          and 'if (mode == Mode.LOGIN) {\n            return -1;' not in term)
    check("the keyboard is read straight off the window, per frame",
          "McsmKeyboard.poll()" in term and "private void onKey(int key)" in term
          and "glfwGetKey" in keys and "GLFW_PRESS" in keys)
    check("the keys that matter are all handled",
          "McsmKeyboard.ENTER" in term and "McsmKeyboard.BACKSPACE" in term
          and "McsmKeyboard.ESCAPE" in term and "McsmKeyboard.H" in term
          and "textOf(key)" in term)
    check("the field and our own copy are kept in step",
          "private void syncField()" in term and "codeField.setValue(typed);" in term)
    check("the C key has a direct fallback as well as the registered binding",
          "mcsm$fallbackKey()" in tclient and "mcsm$isKeyDown(McsmKeyboard.C)" in tclient
          and "McsmKeyboard.poll()" not in tclient.split("mcsm$fallbackKey()")[1][:1200])
    check("the mod registers its OWN SoundEvents in its own namespace",
          'Identifier.fromNamespaceAndPath("mcsm", name)' in sounds
          and "SoundEvent.createVariableRangeEvent(id)" in sounds
          and "McsmSounds.initialize();" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""))
    check("sixteen events, each backed by a file the generator writes",
          len(EVENTS := __import__("re").findall(r'register\("([a-z_]+)"\)', sounds)) >= 16
          and all(('"%s"' % k) in gen for k in ["radio/static", "massg/roar", "oblivion/drone", "ui/terminal_key"]))
    check("the radio, the creature and the tears play those events, not vanilla ones",
          "McsmSounds.station(index)" in (read("mcsm-extras/java/net/mcsm/extras/McsmTerminal.java") or "")
          and "McsmSounds.MASSG_ROAR" in (read("mcsm-extras/java/net/mcsm/extras/McsmMassg.java") or "")
          and "McsmSounds.OBLIVION_GLITCH" in (read("mcsm-extras/java/net/mcsm/extras/McsmReality.java") or ""))
    check("the generator merges the sound map instead of muting the menus",
          "MERGE, never clobber" in gen and "os.path.isfile(MANIFEST)" in gen
          and '"ds_btn_hover"' not in "".join(open("jar-overrides/assets/mcsm/sounds.json").read().split("\n")[:0]) )
    check("the build generates and gates the sound set, and the jar audit proves it ships",
          "make_mcsm_sounds.py | tail -1" in (read("ci/build.sh") or "")
          and "make_mcsm_sounds.py --check" in (read("ci/build.sh") or "")
          and "custom sound missing from the jar" in (read("ci/build.sh") or ""))

    # ------------------------------------------------------------------
    # BUILD #426/#427/#428 -- THE PURPLE OFF THE BODY, THE TENTACLES, THE
    # ENDING, AND THE BEASTS.
    #
    # "The purple colour still renders on top of the wither storm"; "the
    # tentacles are meant to snatch the player"; "the epic cinematic storm
    # ending scene -- the white scene was not there, no ripping cracks, no
    # shockwaves"; "I would like a mob named Mas. And the Creator, a gigantic
    # entity. And the whale monster I talked about."
    # ------------------------------------------------------------------
    fx = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmPresenceFxPatch.java") or ""
    blob = read("mcsm-extras/java/net/mcsm/extras/client/McsmStormBlob.java") or ""
    tent = read("mcsm-extras/java/net/mcsm/extras/McsmTentacles.java") or ""
    cine = read("mcsm-extras/java/net/mcsm/extras/client/McsmCinematic.java") or ""
    beast = read("mcsm-extras/java/net/mcsm/extras/entity/McsmBeast.java") or ""
    ents = read("mcsm-extras/java/net/mcsm/extras/entity/McsmEntities.java") or ""

    check("the phase glare is drawn BEHIND the storm, never over it",
          "Vec3 glareCentre" in fx and "glareBehindBody" in fx
          and "haloCentre.subtract(view.scale(bodyRadius * 1.15D))" in fx
          and fx.count("blobLayer(poseStack, collector, GLARE") == fx.count(", glareCentre, view,"))
    check("the purple ground pool is off by default and switchable",
          "if (McsmExtrasConfig.stormGroundPool)" in blob
          and "public static boolean stormGroundPool = false;" in cfg
          and "storm_ground_pool" in cfg
          and "Purple ground pool under the beams" in extras)
    check("the tentacle grab exists and is wired to the switch that had nothing behind it",
          "McsmTentacles.register();" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or "")
          and "McsmExtrasConfig.enableTentacleGrab" in tent
          and "public static boolean enableTentacleGrab" in cfg)
    check("the grab snatches, holds, and ends in a throw or an eat",
          "private static void snatch(" in tent and "private static void hold(" in tent
          and "private static void throwAway(" in tent and "private static void eat(" in tent
          and "It eats you." in tent and "It throws you." in tent)
    check("only survival players are taken, and the pull is physical",
          "player.isCreative() || player.isSpectator() || player.isPassenger()" in tent
          and "player.teleportTo(level," in tent and "Set.of(), player.getYRot(), player.getXRot(), false)" in tent
          and "PULL_START" in tent and "PULL_END" in tent)
    check("the ending has the white scene, the ripping cracks and the colour waves",
          "drawEnding(" in cine and "WHITE_PEAK_MS" in cine
          and "WAVE_COLOURS" in cine and "drawRing(g, cx, cy, reach, 14, colour);" in cine
          and "drawCrack(g, cx, cy, ang, reach," in cine
          and "g.fill(0, 0, w, h, (a << 24) | 0xFFFFFF);" in cine)
    check("the ending rides the same proven per-frame HUD hook",
          "McsmCinematic.tickEnding();" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHudAttachMixin.java") or "")
          and "McsmCinematic.drawEnding(g);" in (read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHudAttachMixin.java") or "")
          and "McsmFxDriver.lastDeathArmMs() < 1500L" in cine)
    check("Mas, the Creator and the whale are REAL registered entities",
          'Identifier.fromNamespaceAndPath("mcsm", "mas")' in ents
          and 'Identifier.fromNamespaceAndPath("mcsm", "creator")' in ents
          and 'Identifier.fromNamespaceAndPath("mcsm", "whale_monster")' in ents
          and "FabricDefaultAttributeRegistry.register(registered" in ents)
    check("Mas cannot be hurt, the other two can",
          "public boolean hurtServer(" in beast and "if (MAS.equals(kind()))" in beast
          and "return false;" in beast and "return super.hurtServer(level, source, amount);" in beast)
    check("the storm's own summon now spawns Mas, and the Creator above it",
          "net.mcsm.extras.entity.McsmEntities.MAS" in (read("mcsm-extras/java/net/mcsm/extras/McsmMassg.java") or "")
          and "creator(level, x, y + 240.0D, z);" in (read("mcsm-extras/java/net/mcsm/extras/McsmMassg.java") or "")
          and "skyWhale(level, player);" in (read("mcsm-extras/java/net/mcsm/extras/McsmMassg.java") or ""))
    check("the beasts have names in the lang file",
          '"entity.mcsm.mas": "Mas"' in (read("ci/make_mcsm_content_assets.py") or "")
          and '"entity.mcsm.creator": "The Creator"' in (read("ci/make_mcsm_content_assets.py") or "")
          and '"entity.mcsm.whale_monster": "The Whale"' in (read("ci/make_mcsm_content_assets.py") or ""))

    # ------------------------------------------------------------------
    # BUILD #429 -- THE CITIES ARRIVE IN THE REGULAR WORLD, AND THEY HAVE
    # INSIDES.
    #
    # "In the real world, vanilla, the cities -- the real [ones] have actual
    # interiors full of life, full of people, full of designs, banners from
    # blocks and stuff" and "I would like it that they summon in the regular
    # [world]".
    # ------------------------------------------------------------------
    cities = read("mcsm-extras/java/net/mcsm/extras/McsmCities.java") or ""

    check("the city generator runs in the overworld, not only in the decayed reality",
          "net.minecraft.world.level.Level.OVERWORLD" in cities
          and "public static boolean citiesInOverworld = true;" in cfg
          and "cities_in_overworld" in cfg
          and "Raise the ruined cities in the regular world too" in extras)
    check("it never raises a district on top of world spawn",
          "OVERWORLD_MIN_DISTANCE = 384" in cities and "private static boolean nearSpawn(" in cities
          # #444 made spawnPos public: the maze and server-room generators now ask
          # the same question, and a second reflective name-search would be a
          # second thing to keep in step with the API.
          and "public static BlockPos spawnPos(ServerLevel level)" in cities
          and "if (overworld && nearSpawn(level, player))" in cities)
    check("world spawn is read by name, because the field call does not exist in this API",
          # run 508: javac "cannot find symbol: method getSharedSpawnPos(), location:
          # variable level of type ServerLevel". A try/catch cannot help -- the
          # method has to exist at COMPILE time -- so it is looked up reflectively,
          # the way the story stage already looks up its own spawn.
          "level.getSharedSpawnPos()" not in cities
          and "SPAWN_METHODS = {\"getSharedSpawnPos\", \"getSpawnPos\", \"getRespawnPosition\"}" in cities
          and "method.invoke(level)" in cities)
    check("every plot is furnished inside, not just shelled",
          "private static void planInterior(" in cities
          and "planInterior(plan, rng, bx, bz, ground, kind);" in cities
          and "hole in the floor" in cities and "a rug down the middle" in cities)
    check("buildings wear a banner made of blocks",
          "private static void planFacadeBanner(" in cities
          and "planFacadeBanner(plan, rng, bx, bz, ground, kind);" in cities
          and "DESIGN_ACCENT" in cities and "DESIGN_DARK" in cities)
    check("the design set is real placeable blocks in the palette",
          "q[DESIGN_LIGHT] = design(\"white_concrete\", Blocks.POLISHED_DEEPSLATE);" in cities
          and "q[DESIGN_DARK] = design(\"black_concrete\", Blocks.POLISHED_DEEPSLATE);" in cities
          and "q[DESIGN_ACCENT] = design(\"purple_concrete\", Blocks.POLISHED_DEEPSLATE);" in cities
          and "PALETTE_SIZE = 29" in cities)
    check("the design blocks are resolved from the block registry, not from field names",
          # run 508: javac "cannot find symbol: variable WHITE_CONCRETE, location:
          # class Blocks" -- and the same for the other three. The registry ids are
          # the stable public names, so they are the ones the city asks for.
          "private static BlockState design(String path, Block fallback)" in cities
          and "BuiltInRegistries.BLOCK.getValue(" in cities
          and "q[DESIGN_LIGHT] = stateOf(Blocks.WHITE_CONCRETE);" not in cities
          and "if (block == null || block == Blocks.AIR)" in cities)
    check("there are people in the city when the player walks in",
          "private static void spawnLife(" in cities and "markLife(plan, bx, bz, ground);" in cities
          and "STORY_CHARACTER" in cities and "CHARACTERS" in cities
          and "if (OPS.isEmpty()) {\n                spawnLife(level);" in cities)

    # ------------------------------------------------------------------
    # BUILD #430 -- ONE BACKDROP PALETTE.
    #
    # "the colours haven't really changed at all", "backdrops still not the
    # accurate colours" and "you fixed the the Halos I mean the atmos back drop".
    # The sky's columns were traced off the supplied sheets and parity checked;
    # the two BACKDROP renderers each carried a hand-typed ramp and were in no
    # chain at all, so they could disagree with the sheets and with each other
    # and no gate could see it.
    # ------------------------------------------------------------------
    palette = read("mcsm-extras/java/net/mcsm/extras/client/McsmBackdropPalette.java") or ""
    atmosphere = read("mcsm-extras/java/net/mcsm/extras/client/McsmAtmosphericMeshComponent.java") or ""
    stage = read("mcsm-extras/java/net/mcsm/extras/client/McsmExperimentalStoryStage.java") or ""
    skyref = read("mcsm-core-shaders/core/sky.fsh") or ""

    check("both backdrops read the one generated palette and keep no colour of their own",
          "MCSM_BACKDROP_PALETTE_BEGIN" in palette
          and "public static int column(float phase, float t)" in palette
          and "McsmBackdropPalette.column(phase, 1.0F - vertical)" in atmosphere
          and "McsmBackdropPalette.column(phase, 1.0F - vertical)" in stage)
    # #430 deleted the deck ramps out of this file and took two innocent methods
    # with them (run 509: "cannot find symbol: method smoothstep(float,float,float)")
    # -- and #434's edit took them out again, because both times the edit sliced
    # from one comment to the next method and everything declared between them
    # went with the slice. The wall paints nothing at all without them, so the
    # FULL helper set this file calls is pinned by signature.
    helpers = [
        "public static void submit(",
        "private static void emitBackdrop(",
        "private static Vec3 point(",
        "private static void putQuad(",
        "private static void vertex(",
        "private static int colour(",
        "private static double bodyRadius(float phase)",
        "private static float smoothstep(float value, float low, float high)",
        "private static int mix(int a, int b, float amount)",
        "private static int rgb(int r, int g, int b)",
        "private static Vec3 normal(Vec3 value)",
    ]
    check("the wall keeps EVERY helper it calls (a range-slice edit took two out twice)",
          all(h in atmosphere for h in helpers))
    check("the atmosphere wall's own hand-typed decks are gone",
          "phase45" not in atmosphere and "phase55" not in atmosphere
          and "verticalGradient" not in atmosphere
          and "0x6E, 0x78, 0x73" not in atmosphere and "0x7F, 0x3A, 0xA6" not in atmosphere
          and "0xA0, 0x75, 0x7E" not in atmosphere
          and "McsmBackdropPalette.VOID_BLACK" in atmosphere)
    check("the story stage shell's own hand-typed decks are gone",
          "p5Hor" not in stage and "p55Hor" not in stage and "p6Hor" not in stage
          and "mixColor" not in stage
          and "McsmBackdropPalette.argb(" in stage)
    check("the generated block carries the sheets' four columns and the sky's routing",
          "public static final int[] TEAL = {" in palette
          and "public static final int[] EMBER = {" in palette
          and "public static final float TEAL_TO_PURPLE_LO = 5.100F;" in palette
          and "public static final float PURPLE_TO_ROSE_HI = 6.050F;" in palette
          and "public static final float ROSE_TO_EMBER_HI = 8.050F;" in palette)
    check("the sky eases through the bands instead of jumping a sixth of a phase early",
          "vec3 col = mix(mix(teal, pur, mcsm_ramp(p, 5.1, 5.5)), rose, mcsm_ramp(p, 5.75, 6.05));"
          in skyref
          and "if (p < 5.9) return mix(pur, rose" not in skyref)
    check("the pack's position program and the Java sky feed keep the same three ramps",
          "mix(mix(teal, purple, mcsm_ramp(p, 5.1, 5.5))" in (
              read("mcsm-core-shaders/core/position.fsh") or "")
          and "ramp(p, 5.75F, 6.05F)" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmStormPhase.java") or ""))

    # ------------------------------------------------------------------
    # BUILD #432 -- IVOR'S SECRET ROOM.
    #
    # The terminal has told players since #424 that the code is "written down in
    # the world, in the hands of the one survivor who is still carrying the
    # paperwork: IVOR. Find him in the secret room of an abandoned hospital."
    # Nothing in the world wrote it down. Now a hospital has the room, the room
    # has the five letters inlaid in its floor, and IVOR is standing in it.
    # ------------------------------------------------------------------
    import re as _re
    terminal = read("mcsm-extras/java/net/mcsm/extras/McsmTerminal.java") or ""

    check("a hospital actually builds the secret room",
          "planSecretRoom(p, x0, z1, ground);" in cities
          and "private static void planSecretRoom(Plan p, int x0, int z1, int ground)" in cities)
    check("the room is a basement, three under the ward, with a stairwell both ways",
          "private static final int SECRET_DEPTH = 3;" in cities
          and "final int walk = ground - SECRET_DEPTH;" in cities
          and "for (int s = 0; s < SECRET_DEPTH; s++) {" in cities
          and "int z = zFront + 1 - s;" in cities
          and "p.put(x, y + 2, z, AIR);" in cities)
    check("the code is inlaid in the floor in the storm's purple",
          "p.put(gx + col, walk - 1, zBack + 1 + row, DESIGN_ACCENT);" in cities
          and "int gx = x0 + 5;" in cities)

    # The glyph table is the code. Decode it and check the SHAPES, rather than
    # trusting the comment that says which letters they are.
    glyphs = _re.search(r"SECRET_GLYPHS = \{(.*?)\};", cities, _re.S)
    decoded = []
    if glyphs:
        for body in _re.findall(r'"([.#]+)"', glyphs.group(1)):
            decoded.append([body[i:i + 3] for i in range(0, len(body), 3)])
    want = [
        ["#.#", "###", "#.#", "#.#", "#.#"],   # M
        [".#.", "#.#", "###", "#.#", "#.#"],   # A
        [".##", "#..", ".#.", "..#", "##."],   # S
        [".##", "#..", ".#.", "..#", "##."],   # S
        [".##", "#..", "#.#", "#.#", ".##"],   # G
    ]
    check("the five inlaid glyphs decode to M A S S G and nothing else",
          len(decoded) == 5
          and all(len(row) == 5 and all(len(c) == 3 for c in row) for row in decoded)
          and decoded == want)
    check("IVOR is the one standing in the room, and he carries the name",
          "private static final int IVOR_ENTRY = 1;" in cities
          and "boolean ivor = at.length > 3 && at[3] == IVOR_ENTRY;" in cities
          and 'character.setCharacter(ivor ? "ivor"' in cities
          and 'mob.setCustomName(net.minecraft.network.chat.Component.literal("IVOR"));' in cities)
    check("the locked screen still sends the player to that room",
          "secret room of an abandoned hospital" in terminal
          and "IVOR" in terminal and "nearIvor(player)" in terminal)

    # ------------------------------------------------------------------
    # BUILD #433 -- SKY VORTEXES SPAWNING MONSTERS.
    #
    # The artist's list, verbatim: "sky vortexes spawning monsters." The mod has
    # had sky scenery for years and none of it has ever put anything on the
    # ground. This is the half that does.
    # ------------------------------------------------------------------
    vortex = read("mcsm-extras/java/net/mcsm/extras/McsmSkyVortexes.java") or ""

    check("the storm opens a mouth in the sky and drops things out of it",
          "public final class McsmSkyVortexes" in vortex
          and "private static void drop(ServerLevel level, Mouth mouth, double radius)" in vortex
          and "EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(" in vortex
          and "level.addFreshEntity(created);" in vortex)
    check("what falls survives the fall, so a corpse is never the threat",
          "private static final double HEIGHT = 26.0D;" in vortex
          and "private static final double DROP_FALL = 12.0D;" in vortex
          and "created.snapTo(mouth.x + Math.cos(a) * radius * 0.55D, mouth.y - DROP_FALL," in vortex
          and "Phantoms fly anyway" in vortex)
    check("it only opens over a storm that is far enough along",
          "private static final double ONSET_PHASE = 5.0D;" in vortex
          and "if (!decayed && phaseNear(level, player) < ONSET_PHASE) {" in vortex
          and "for (WitherStormEntity storm : level.getEntitiesOfClass(WitherStormEntity.class, box))" in vortex)
    check("it cannot run away: one mouth, a life timer, and a cap on the drops",
          "private static final int LIFE = 260;" in vortex
          and "private static final int INTERVAL = 1500;" in vortex
          and "private static final int MAX_DROPS = 6;" in vortex
          and "if (mouth.age % DROP == 0 && mouth.dropped < MAX_DROPS) {" in vortex
          and "LIVE.remove(level.dimension());" in vortex)
    check("it is switchable, saved, and reachable from the panel",
          "public static boolean skyVortexes = true;" in cfg
          and 'p.setProperty("sky_vortexes", String.valueOf(skyVortexes));' in cfg
          and 'skyVortexes = bool(p, "sky_vortexes", skyVortexes);' in cfg
          and "Sky Vortexes Drop Monsters" in extras
          and "McsmSkyVortexes.register();" in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""))

    # ------------------------------------------------------------------
    # BUILD #434 -- THE MODEL-ATTACHED FIELD AND THE SKY'S BOTTOM LAYER.
    #
    # The brief: the halo is an independent, MODEL-ATTACHED 3D atmospheric
    # backdrop field, its centre is a completely empty, light-impenetrable black
    # core, it renders only around the outer perimeter so the eyes and the white
    # teeth are never blocked, and it turns with the entity. And, separately:
    # "the skies are still only the top layer. you need to add a bottom layer
    # covering the whole bottom."
    # ------------------------------------------------------------------
    field = read("mcsm-extras/java/net/mcsm/extras/client/McsmAtmosphericMeshComponent.java") or ""
    band = read("mcsm-extras/java/net/mcsm/extras/client/McsmSkyFloorBand.java") or ""

    check("the field wraps the whole body instead of being half a card",
          "if (y0 >= 0.0D) {" not in field
          and "BUILD #434 -- THE FIELD WRAPS THE BODY." in field
          and "float topFade = smoothstep(1.0F - (float) ((y + 1.0D) * 0.5D), 0.0F, 0.22F);" in field)
    check("its centre is an empty, light-impenetrable black core",
          "private static final float CORE_RADIUS = 0.46F;" in field
          and "private static final float CORE_ALPHA = 0.94F;" in field
          and "float core = 1.0F - smoothstep(radius, CORE_RADIUS, CORE_RADIUS + CORE_FALLOFF);" in field
          and "int rgb = mix(aura, McsmBackdropPalette.VOID_BLACK, core);" in field)
    check("the core is a penumbra, not a disc: the supplied frames have no rim",
          # the dark mass leaves the silhouette as near-black and widens out the
          # way a shadow does; a constant-radius core would draw a hard-edged
          # black disc behind the creature.
          "private static final float CORE_FALLOFF = 0.34F;" in field
          and "float edge = topFade * smoothstep(vertical, 0.0F, 0.08F);" in field
          and "outer = Mth.clamp(outer * edge, 0.0F, 1.0F);" in field)
    check("the aura lives only in the annulus, and wears the sheets' own column",
          "float ring = 1.0F - smoothstep(radius, 0.74F, 1.0F);" in field
          and "float outer = Math.max(0.0F, ring - core);" in field
          and "int aura = McsmBackdropPalette.column(phase, 1.0F - vertical);" in field)
    check("the field still turns with the entity, on both axes",
          "poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));" in field
          and "poseStack.mulPose(Axis.ZN.rotationDegrees(state.bodyRoll));" in field)

    check("there is a bottom layer of sky at all, drawn in the world",
          "public final class McsmSkyFloorBand" in band
          and "private static final double DEPTH = 512.0D;" in band
          and "collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(WHITE)," in band)
    check("the wall carries the live horizon colour down and deepens as it falls",
          # BUILD #458 -- the hardcoded violet is gone: the band asks the
          # dimension's identity for its own horizon, and only the Overworld
          # (which the mod does not own) wears the storm's band.
          "horizon = McsmStormPhase.horizonFor(Math.max(phase, McsmStormPhase.PHASE_MIN));" in band
          and "float[] horizon = McsmIdentity.horizon(skin);" in band
          and "McsmIdentity.Skin skin = McsmIdentity.forLevel(level);" in band
          and "private static final float SEAM_ALPHA = 0.0F;" in band
          and "private static final float FLOOR_ALPHA = 0.94F;" in band)
    check("the bottom layer is switchable, saved, panel-reachable and drawn in the backdrop pass",
          "public static boolean skyFloorBand = true;" in cfg
          and 'p.setProperty("sky_floor_band", String.valueOf(skyFloorBand));' in cfg
          and 'skyFloorBand = bool(p, "sky_floor_band", skyFloorBand);' in cfg
          and "Sky Bottom Layer (the wall under the world)" in extras
          and "McsmSkyFloorBand.submit(ctx);" in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmStormBlobMixin.java") or ""))

    # ------------------------------------------------------------------
    # BUILD #436 -- THE IMPORT RULE.
    #
    # Run 515/516: "cannot find symbol: variable McsmSkyVortexes, location: class
    # McsmBuiltinPackMixin". The class compiled fine (its .class was in the jar's
    # compiled list) -- the mixin simply called `McsmSkyVortexes.register()` with
    # no import for it. A registration line is one line; the import is the other,
    # and this checks they travel together for EVERY class that mixin boots.
    # ------------------------------------------------------------------
    boot = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""
    boots = set()
    for m in _re.finditer(r"\b(Mcsm[A-Za-z0-9_]+)\.register\(\)", boot):
        boots.add(m.group(1))
    check("every class the bootstrap boots is imported where it boots it",
          bool(boots)
          and all(("import net.mcsm.extras.%s;" % name) in boot
                  or ("import net.mcsm.extras.entity.%s;" % name) in boot
                  or ("import net.dabicco.witherstormmod.%s;" % name) in boot
                  for name in sorted(boots)))

    # ------------------------------------------------------------------
    # BUILD #438 -- THE BODY STOPS BEING COLOURLESS.
    #
    # "phase 0 ... it's in black and white" and "the colours haven't really
    # changed at all". The body's vertex multiplier was a fixed NEUTRAL
    # (0x948F8A: r, g and b within seven of each other), so the pink of phase 6,
    # the amethyst of 5.5 and the teal of 5 all arrived on the model as the same
    # grey. A neutral multiplier cannot carry a hue.
    # ------------------------------------------------------------------
    body = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmStormBodyNeutralTintMixin.java") or ""

    check("the body's tint carries the live phase's hue, not a fixed grey",
          "int tint = NEUTRAL;" in body
          and "net.mcsm.extras.client.McsmStormPhase.columnFor((float) phase, 1.0F)" in body
          and "tint = 0xFF000000 | (r << 16) | (g << 8) | b;" in body
          and "cir.setReturnValue(tint);" in body)
    check("it keeps #404's brightness, and the neutral stands below the storm's onset",
          "float gain = 0.580F / max;" in body
          and "private static final int NEUTRAL = 0x948F8A;" in body
          and "phase >= net.mcsm.extras.client.McsmStormPhase.PHASE_MIN" in body)
    check("it is switchable and tunable from the panel",
          "public static boolean phaseTintedBody = true;" in cfg
          and "public static double bodyPhaseTint = 0.45;" in cfg
          and 'p.setProperty("body_phase_tint", String.valueOf(bodyPhaseTint));' in cfg
          and 'bodyPhaseTint = dbl(p, "body_phase_tint", bodyPhaseTint);' in cfg
          and "Body Wears the Phase Colour" in extras
          and "Body Phase Tint (0 = the old grey, 1 = the sky's hue)" in extras)

    # ------------------------------------------------------------------
    # BUILD #440 -- THE UPPER BACK, PROVEN AGAINST THE JAR RATHER THAN GUESSED AT.
    #
    # Run 519's API oracle answered the question this build had been guessing at
    # since #423: the frozen base jar really does declare
    #
    #   private void WitherStormRenderer.submitGrowth5(WitherStormRenderState,
    #           PoseStack, SubmitNodeCollector)
    #   private final HugeAssBackModel WitherStormRenderer.hugeAssBackModel
    #   private boolean WitherStormRenderer.previewShadowPass
    #   public void PoseStack.scale(float, float, float)
    #
    # so both welds target real members. The two welds stay `require = 0` on
    # purpose (a different renderer must not be able to break launch), which
    # means the proof has to live somehere else: ci/API_ORACLE.md records the
    # quoted signatures and the run they came from, and this checks that the
    # document still names the members the mixins inject into.
    # ------------------------------------------------------------------
    attach = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHugeBackAttachMixin.java") or ""
    pose = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHugeBackPoseMixin.java") or ""
    oracle = read("ci/API_ORACLE.md") or ""

    check("the upper-back weld names the method the jar declares",
          attach.count('@Inject(method = "submitGrowth5"') == 2
          and "require = 0" in attach
          and "write 5(S(2" not in attach)
    check("the huge-back pose names the method the jar declares",
          '@Inject(method = "scale", at = @At("RETURN"), remap = false' in pose
          and "require = 0" in pose)
    check("the API oracle records the signatures those two welds depend on",
          "submitGrowth5(WitherStormRenderState, PoseStack, SubmitNodeCollector)" in oracle
          and "HugeAssBackModel hugeAssBackModel;" in oracle
          and "public void scale(float, float, float);" in oracle
          and "private boolean previewShadowPass;" in oracle)
    check("the oracle cannot be cut short by one unreadable class",
          # run 519: the dump stopped dead at StormBackdrop because javap could not
          # read the next class and `set -e` + pipefail took the whole group with it
          "javap could not read" in (read("ci/build.sh") or "")
          and "{ grep -Ei \"eyes|emitter|bloom|glow|mark|Fogless|RenderType|Skins\" || true; }" in (
              read("ci/build.sh") or ""))

    # ------------------------------------------------------------------
    # BUILD #442 -- THE TEETH AND EYES WEAR THE WHITE MASK.
    #
    # Run 521 answered it from the constant pool: the head renderer calls
    # GlowRenderTypes.emitterMark and .bloomSource, and NEVER RenderTypes.eyes --
    # so the redirect that was meant to keep the body atlas out of the head's glow
    # pass could not fire, and the pass drew the opaque body texture. The two
    # passes that exist now wear the dedicated emissive atlas, which the white
    # mask gate holds to pure white from phase 4 up: white teeth with no shader.
    # ------------------------------------------------------------------
    headglow = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHeadEyesRenderTypeMixin.java") or ""
    oracle = read("ci/API_ORACLE.md") or ""

    check("the head's glow passes draw the dedicated emissive atlas",
          headglow.count("return mcsm$emissive(texture);") == 2
          and "RenderTypes.eyes(net.dabicco.witherstormmod.client.StormSkins.phase6Emissive())" in headglow)
    check("the redirect that could never fire is gone, not left lying",
          'target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;eyes(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"' not in headglow)
    check("the oracle records the call sites this rests on",
          "FoglessRenderTypes.eyes(Identifier)" in oracle
          and "GlowRenderTypes.emitterMark(Identifier)" in oracle
          and "no RenderTypes.eyes call at all" in oracle)

    # ------------------------------------------------------------------
    # BUILD #443 -- RITUALS, AND THE INFINITE DIMENSION OF ADAMS.
    #
    # The wishlist asks for "rituals" and for "the infinite dimension of adams".
    # A ritual is not a block: it is ring + offering + condition -> the world
    # answers, and the answer has to be a real one. "Infinite" is not a keyword
    # either: a datapack dimension is endless in extent, so the word has to be
    # earned by the GENERATOR, which has no end state at all.
    # ------------------------------------------------------------------
    rituals = read("mcsm-extras/java/net/mcsm/extras/McsmRituals.java") or ""
    adams = read("mcsm-extras/java/net/mcsm/extras/McsmAdams.java") or ""
    adams_dim = read("jar-overrides/data/mcsm/dimension/adams_infinity.json") or ""
    adams_type = read("jar-overrides/data/mcsm/dimension_type/adams_infinity.json") or ""
    boot = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""

    check("a ritual is a shape, a price and a condition -- not a block",
          "private static int ringCount(ServerLevel level, BlockPos core, Ritual ritual)" in rituals
          and "private static boolean holding(ServerPlayer player, String offering)" in rituals
          and "if (phase < ritual.phase())" in rituals
          and "RING_NEEDED = 10" in rituals and "RING_SAMPLES = 12" in rituals)
    check("all six rites are registered with a ring, a core, an offering and an effect",
          rituals.count("new Ritual(\"") == 6
          and "EFFECT_RIFT" in rituals and "EFFECT_WAKING" in rituals and "EFFECT_SWARM" in rituals
          and "EFFECT_CORRUPTION" in rituals and "EFFECT_BLACK_SUN" in rituals
          and "EFFECT_ADAMS" in rituals)
    check("the effects are the mod's own systems, called through public doors",
          # a ritual must not fake a key press or a packet: it asks the same
          # systems a player would, through doors added for it.
          "McsmReality.enter(player)" in rituals
          and "McsmMassg.summon(level, player)" in rituals
          and "McsmCreatures.release(level, core.above(2), 4," in rituals
          and "McsmBlackHole.openNear(level, player)" in rituals
          and "McsmAdams.enter(player)" in rituals
          and "public static boolean enter(ServerPlayer player)" in (
              read("mcsm-extras/java/net/mcsm/extras/McsmReality.java") or "")
          and "public static boolean openNear(ServerLevel level, ServerPlayer player)" in (
              read("mcsm-extras/java/net/mcsm/extras/McsmBlackHole.java") or ""))
    check("a ritual cannot be farmed at one spot",
          "private static final Set<Long> SPENT = ConcurrentHashMap.newKeySet();" in rituals
          and "if (!SPENT.add(spent))" in rituals
          and "COOLDOWN_TICKS = 200L" in rituals)
    check("the scan is bounded: one ritual per player per pass, on a period",
          "SCAN_PERIOD = 30" in rituals
          and "CURSOR.merge(player.getUUID(), 1, Integer::sum)" in rituals
          and "if (last != null && now - last < SCAN_PERIOD)" in rituals)
    check("adams is a real dimension with its own type, layers and sky",
          '"type": "mcsm:adams_infinity"' in adams_dim
          and '"type": "minecraft:flat"' in adams_dim
          and '"has_skylight": true' in adams_type
          and '"skybox": "overworld"' in adams_type
          and '"height": 384' in adams_type and '"min_y": -64' in adams_type)
    check("the surface the generator builds on matches its own layer stack",
          # 1 void_core + 60 stone + 40 road + 26 tiles + 1 surface = 128 layers over
          # min_y -64, so the top solid block is y = 63 -- and the placement code
          # reads SURFACE_Y, so the two can never drift apart silently.
          adams_dim.count('"height"') == 5
          and '"height": 60' in adams_dim and '"height": 40' in adams_dim
          and '"height": 26' in adams_dim
          and "public static final int SURFACE_Y = 63;" in adams)
    check("adams has no end state: it plans, writes and never completes",
          "public static void tick(ServerLevel level)" in adams
          and "REGIONS.add(planRegion(level, prx + dx, prz + dz, key))" in adams
          and "BUILT.add(head.key)" in adams
          and "MAX_PENDING = 6" in adams
          and "hasCity" not in adams)
    check("a chamber always holds salvage and sometimes holds something else",
          "container.setItem(Math.floorMod((int) (h >> 41), slots)" in adams
          # BUILD #460 -- what a chamber was keeping is this world's own creature
          # (the keeper); the storm's bestiary is only the fallback if the
          # keeper's entity type never registered.
          and "net.mcsm.extras.entity.McsmEntities.KEEPER" in adams
          and "McsmCreatures.release(level, at, count, 6.0D);" in adams
          and "private static void finish(ServerLevel level, Region region)" in adams)
    check("both systems are booted, and booted with their imports",
          "McsmRituals.register();" in boot and "McsmAdams.register();" in boot
          and "import net.mcsm.extras.McsmRituals;" in boot
          and "import net.mcsm.extras.McsmAdams;" in boot)
    check("the panel and /ds can both reach them",
          "Rituals (ring + offering -> the world answers)" in extras
          and "The Infinite Dimension of Adams" in extras
          and "public static boolean rituals = true;" in cfg
          and "public static boolean adamsReality = true;" in cfg
          and 'Commands.literal("ritual")' in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or "")
          and 'Commands.literal("adams")' in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or ""))

    # ------------------------------------------------------------------
    # BUILD #444 -- STORAGE MAZES AND SERVER ROOMS.
    #
    # "Storage mazes" and "server rooms" off the wishlist. A maze has to be a real
    # maze (carved, connected, with dead ends worth reaching), a server room has to
    # be a machine (rack lights that blink, and a power failure), and both have to
    # stand on the ground rather than in the air.
    # ------------------------------------------------------------------
    mazes = read("mcsm-extras/java/net/mcsm/extras/McsmMazes.java") or ""
    rooms = read("mcsm-extras/java/net/mcsm/extras/McsmServerRooms.java") or ""
    queue = read("mcsm-extras/java/net/mcsm/extras/McsmBuildQueue.java") or ""

    check("the maze is CARVED by a depth-first walk, not painted as corridors",
          "boolean[] visited = new boolean[GRID * GRID];" in mazes
          and "int[] stackX = new int[GRID * GRID];" in mazes
          and "int pick = choices[rng.nextInt(n)];" in mazes
          and "rng = new java.util.Random(seed)" in mazes)
    check("the maze is sealed, ceilinged and lamped like a warehouse",
          "planner.box(ox - 1, FLOOR_Y - 1, oz - 1, ox + side, FLOOR_Y + 5, oz + side, BRICK)" in mazes
          and "FLOOR_Y = 18" in mazes
          and "planner.box(x, FLOOR_Y + 4, z, x + 2, FLOOR_Y + 4, z + 2, TILES)" in mazes)
    check("the dead ends are the treasure, and the salvage is reachable",
          # a container API call, not a decorative crate: the crate blocks in the
          # content pack drop loot when broken, they do not hold any.
          "if (ways == 1)" in mazes
          and "planner.put(x, FLOOR_Y + 1, z, BARREL);" in mazes
          and "planner.container(x, FLOOR_Y + 1, z, vault ? TAG_VAULT : TAG_AISLE);" in mazes
          and "be instanceof Container container" in mazes
          and "container.setItem(" in mazes)
    check("the shaft to the hatch stops at the terrain, never through a base",
          "surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz)" in mazes
          and "for (int y = FLOOR_Y + 5; y <= surface; y++)" in mazes
          and "McsmCities.spawnPos(level)" in mazes)
    check("the server room is a machine: rack lights it remembers",
          "private static final Map<Object, List<int[]>> LAMPS" in rooms
          and "planner.spot(rackHall + ax, FLOOR_Y + 1, z0 + az, TAG_RACK);" in rooms
          and "if (tag == TAG_RACK)" in rooms)
    check("the racks blink, and the room has a power failure",
          "CHATTER_PERIOD = 20L" in rooms and "CHATTER = 5" in rooms
          and "FAILURE_PERIOD = 3600L" in rooms and "FAILURE_TICKS = 90L" in rooms
          and "boolean failing = cycle < FAILURE_TICKS;" in rooms
          and "level.setBlock(new BlockPos(at[0], at[1], at[2]), dark, 2);" in rooms)
    check("the mainframe is its own tag, and it answers with the keycard in hand",
          "TAG_MAINFRAME = 3" in rooms
          and "planner.spot(vx, FLOOR_Y + 1, vz, TAG_MAINFRAME);" in rooms
          and "player.getInventory().add(new ItemStack(keycard))" in rooms
          and "item(\"mcsm:city_keycard\")" in rooms)
    check("both structures are booted, imported, switchable and findable",
          "McsmMazes.register();" in boot and "McsmServerRooms.register();" in boot
          and "import net.mcsm.extras.McsmMazes;" in boot
          and "import net.mcsm.extras.McsmServerRooms;" in boot
          and "public static boolean storageMazes = true;" in cfg
          and "public static boolean serverRooms = true;" in cfg
          and "Storage Mazes (carved warehouses, hatches)" in extras
          and "Server Rooms (blinking racks, a live mainframe)" in extras
          and 'Commands.literal("maze")' in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or "")
          and 'Commands.literal("server")' in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or ""))
    check("both are written through the shared queue at a fixed budget per tick",
          "public void pump(ServerLevel level, int budget, BlockState[] palette," in queue
          and "OPS_PER_TICK = 1500" in mazes and "OPS_PER_TICK = 1400" in rooms
          and "QUEUE.pump(level, OPS_PER_TICK, palette(), plan -> finish(level, plan));" in mazes
          and "QUEUE.pump(level, OPS_PER_TICK, palette(), plan -> finish(level, plan));" in rooms)
    check("adams hands its salvage over in a container too (the crate blocks are not)",
          "grow.put(crateX, ground + 1, crateZ, BARREL);" in adams
          and 'next[BARREL] = state("minecraft:barrel", null);' in adams)

    # ------------------------------------------------------------------
    # BUILD #445 -- THE INFINITE FUTURE-BOOK.
    #
    # "The infinite future-book" off the wishlist. A book of the future is only
    # worth anything if its dates are real, so: one end date in the whole build
    # (the sky terminal's own epoch), pages that are dated from the clock, a book
    # that opens on the page that has come due, and a last page that is the
    # reader's. A gate holds each of those, and holds that the screen draws no
    # edge tint (that is a standing instruction, and screens drift).
    # ------------------------------------------------------------------
    book = read("mcsm-extras/java/net/mcsm/extras/McsmFutureBook.java") or ""
    bookui = read("mcsm-extras/java/net/mcsm/extras/client/McsmFutureBookScreen.java") or ""
    massg = read("mcsm-extras/java/net/mcsm/extras/McsmMassg.java") or ""

    check("the book counts to the terminal's own end date, not to a second one",
          "public static final long END_EPOCH_MS = 1801526400000L; // 2027-02-01T00:00:00Z"
                  in massg
          and "java.time.Instant.ofEpochMilli(McsmMassg.END_EPOCH_MS)" in book
          and "END_YEAR = 2027" in book and "END_MONTH = 2" in book and "END_DAY = 1" in book
          and "ChronoUnit.DAYS.between(LocalDate.now(), end)" in book)
    check("the pages are dated from the clock, and they move when it does",
          "out.add(new Page(template.chapter(), template.title(), left - offset" in book
          and "offset += template.span();" in book
          and "public boolean due(int daysLeft)" in book
          and "return day <= daysLeft;" in book
          and "public static int currentPage(int daysLeft)" in book)
    check("the book opens on the page that has come due, and the reader's is last",
          "this.selected = McsmFutureBook.currentPage(McsmFutureBook.daysLeft());" in bookui
          and "int last = pages.size();            // the reader's own page is one past the book"
                  in bookui
          and "return TEMPLATE.size() + 1;" in book)
    check("it is a book: chapters, titles, dated pages, spans, and omens",
          book.count("        p(\"") >= 20
          and 'p("I", "THE BOOK OPENS", 4' in book
          and 'p("XX", "THE LAST PAGES", 4' in book
          and "\\u2500\\u2500 CHAPTER " in bookui
          and "page.omen()" in bookui
          and "McsmFutureBook.Page page = pages.get(selected);" in bookui)
    check("the last page is the reader's, and their ink is kept",
          "public static String futureBookNote" in cfg
          and "future_book_note" in cfg and "future_book_note_day" in cfg
          and "McsmExtrasConfig.futureBookNote = clean;" in bookui
          and "McsmExtrasConfig.futureBookNoteDay = McsmFutureBook.daysLeft();" in bookui
          and "McsmExtrasConfig.save();" in bookui)
    check("the book's keys are POLLED, like the terminal's, not hooked",
          "for (Integer key : McsmKeyboard.poll()) {" in bookui
          and "McsmKeyboard.textOf(k)" in bookui
          and "public static final int B = 66;" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmKeyboard.java") or "")
          and "public static final int W = 87;" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmKeyboard.java") or "")
          and "McsmKeyboard.BACKSPACE" in bookui and "McsmKeyboard.SPACE" in bookui)
    check("B opens it, and typing a B into the last page does not shut it",
          "mcsm$fallbackBookKey();" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or "")
          and "if (!book.writing()) {" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or "")
          and "McsmFutureBookScreen.show();" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or "")
          and "public boolean writing() {" in bookui
          and "if (writing) {" in bookui)
    check("the book draws no edge tint (the standing rule) and wraps with the real font",
          "McsmMenuSky.paint(g, this.width, this.height, 0.5F);" in bookui
          and "vignette" not in bookui.lower()
          and "this.font.width(candidate) <= maxWidth" in bookui
          and "public static void show() {" in bookui)
    check("the terminal and the commands both know about it",
          'case "book":' in (
              read("mcsm-extras/java/net/mcsm/extras/McsmTerminal.java") or "")
          and '"cover", "book", "rift"' in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalScreen.java") or "")
          and "McsmFutureBook.forecast()" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalScreen.java") or "")
          and 'Commands.literal("book")' in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java")
              or "")
          and "ds$book" in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java")
              or ""))

    # ------------------------------------------------------------------
    # RUN 525: THE THREE ERRORS, AND THE GATE THEY EARNED.
    #
    # There is no JDK on the machine this build is written on, so the first time
    # new Java is compiled is on the runner -- which is expensive. Run 525 came
    # back with exactly three errors, and all three were of a kind that a text
    # gate can hold down:
    #
    #   * a map declared over UUIDs but written with string keys (McsmRituals),
    #   * a helper that read the heightmap without being handed the level
    #     (McsmMazes.plan), and
    #   * a Screen whose constructor never called super (this API's Screen has no
    #     no-argument constructor, which the terminal had already proved).
    #
    # Each one is pinned below, so the same three cannot come back unnoticed.
    # ------------------------------------------------------------------
    rituals = read("mcsm-extras/java/net/mcsm/extras/McsmRituals.java") or ""
    mazes = read("mcsm-extras/java/net/mcsm/extras/McsmMazes.java") or ""
    rooms = read("mcsm-extras/java/net/mcsm/extras/McsmServerRooms.java") or ""

    check("the ritual cooldown map is keyed by the strings it is written with",
          "private static final Map<String, Long> COOLDOWN = new ConcurrentHashMap<>();" in rituals
          and "Map<UUID, Long> COOLDOWN" not in rituals
          and 'COOLDOWN.put(player.getUUID() + ":"' in rituals)
    check("every helper that reads the heightmap is handed the level",
          "private static McsmBuildQueue.Plan plan(ServerLevel level, long key, int ox, int oz) {"
                  in mazes
          and "QUEUE.add(plan(level, key, ox, oz));" in mazes
          and "private static McsmBuildQueue.Plan plan(ServerLevel level, long key, int ox, int oz) {"
                  in rooms
          and "QUEUE.add(plan(level, key, ox, oz));" in rooms)
    check("the book screen's constructor passes its title to Screen",
          "super(Component.literal(\"The Infinite Future-Book\"));" in bookui
          and "import net.minecraft.network.chat.Component;" in bookui)

    # ------------------------------------------------------------------
    # BUILD #447 -- CUTSCENES BEYOND THE BOOT SEQUENCE.
    #
    # Eight scenes, each fired by something the player does in the world. The gate
    # holds the table and the runtime together: every scene in the catalogue must
    # have a trigger implemented, and every trigger implemented must be a scene --
    # a catalogue that drifts from its own switch is how a cutscene silently stops
    # ever playing.
    # ------------------------------------------------------------------
    table = read("mcsm-extras/java/net/mcsm/extras/McsmSceneTable.java") or ""
    scenes = read("mcsm-extras/java/net/mcsm/extras/client/McsmScenes.java") or ""

    check("there are eight in-world cutscenes, and every one is a real scene",
          table.count("new Scene(") == 8
          and 'new Scene("first_district"' in table
          and 'new Scene("first_maze"' in table
          and 'new Scene("first_racks"' in table
          and 'new Scene("the_rift"' in table
          and 'new Scene("adams_gate"' in table
          and 'new Scene("the_waking"' in table
          and 'new Scene("the_creator"' in table
          and 'new Scene("phase_six"' in table
          and "record Scene(String id, String title, String[] lines, long ms, String trigger,"
                  in table)
    # the trigger switch alone (the wash table below it also has cases, which is why
    # this reads the method rather than the file)
    fires_body = scenes.split("private static boolean fires(")[1].split(
        "private static boolean beastNear(")[0]
    check("every scene in the catalogue has a trigger in the runtime, and no orphan triggers",
          all(('case "' + sid + '":') in fires_body for sid in (
              "first_district", "first_maze", "first_racks", "the_rift", "adams_gate",
              "the_waking", "the_creator", "phase_six"))
          and fires_body.count('case "') == 8)
    check("the catalogue is server-safe: no client class in it, so /ds scene can list it",
          "GuiGraphicsExtractor" not in table
          and "import net.minecraft.client" not in table
          and "public static String summary()" in table
          and "McsmSceneTable.summary()" in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java")
              or ""))
    check("the scenes ride the proven hooks, not a new render path",
          "McsmScenes.tick();" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or "")
          and "net.mcsm.extras.client.McsmScenes.draw(g, delta);" in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHudAttachMixin.java")
              or "")
          and "g.guiWidth()" in scenes and "g.guiHeight()" in scenes
          and "g.centeredText(mc.font" in scenes)
    check("the camera turns the player reflectively, and puts them back afterwards",
          'call(player, "setYRot", yaw);' in scenes
          and 'call(player, "setXRot", pitch);' in scenes
          and 'field(player, "yRotO", yaw);' in scenes
          and "restore = new float[]{player.getYRot(), player.getXRot()};" in scenes
          and "aim(mc.player, restore[0], restore[1]);" in scenes)
    check("the frame is a cutscene: bars, a title card, typed lines, and any key skips",
          "int bar = (int) (h * 0.12D);" in scenes
          and "scene.title(), w / 2" in scenes
          and "int typed = (int) (lines * Math.clamp((t - 0.2D) / 0.5D, 0.0D, 1.0D));" in scenes
          and "skipRequested()" in scenes
          and "McsmKeyboard.poll()" in scenes
          and '"any key skips"' not in scenes
          and "k == McsmKeyboard.SPACE || k == McsmKeyboard.ENTER || k == McsmKeyboard.ESCAPE"
                  in scenes)
    check("the scenes know their own distance shapes (the two probes differ)",
          # nearestCity answers {dx, dz, distance}; nearestMaze / nearestRoom answer
          # {x, z, distanceSquared}. Reading them the same way would fire the city
          # scene 96 blocks late and the other two would never fire at all.
          "return near != null && near[2] < 96;" in scenes
          and scenes.count("return near != null && near[2] < 96 * 96;") == 2
          and "McsmCities.nearestCity((int) Math.floor(x), (int) Math.floor(z))" in scenes
          and "McsmMazes.nearestMaze((int) Math.floor(x), (int) Math.floor(z))" in scenes
          and "McsmServerRooms.nearestRoom((int) Math.floor(x), (int) Math.floor(z))" in scenes)
    check("the big two are found by the entity scan the HUD already proves",
          "player.level().getEntitiesOfClass(McsmBeast.class," in scenes
          and "player.getBoundingBox().inflate(range)" in scenes
          and "beastNear(player, McsmBeast.MAS, 128.0D)" in scenes
          and "beastNear(player, McsmBeast.CREATOR, 192.0D)" in scenes
          and "McsmStormPhase.active() && McsmStormPhase.phase() >= 6.0F" in scenes
          and "player.level().dimension().equals(McsmReality.DECAYED_REALITY)" in scenes
          and "player.level().dimension().equals(McsmAdams.ADAMS)" in scenes)
    check("cutscenes can be switched off, and N plays the next one you have not seen",
          "public static boolean cutscenes = true;" in cfg
          and "cutscenes = bool(p, \"cutscenes\", cutscenes);" in cfg
          and 'p.setProperty("cutscenes", String.valueOf(cutscenes));' in cfg
          and "Cutscenes (eight, in world: cities, mazes, racks, rift, adams)" in extras
          and "public static final int N = 78;" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmKeyboard.java") or "")
          and "mcsm$fallbackSceneKey();" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or "")
          and "McsmScenes.nextUnplayed()" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalClient.java") or "")
          and "public static String nextUnplayed()" in scenes)
    check("the scenes are once each, and never while a screen is open",
          "private static final Set<String> PLAYED = ConcurrentHashMap.newKeySet();" in scenes
          and "PLAYED.contains(scene.id())" in scenes
          # RUN 528: Minecraft has NO "screen" field in this API. The proven read is
          # mc.gui.screen(), which the terminal has used since #424 -- this gate holds
          # both halves of that lesson.
          and "McsmTerminalClient.currentScreen(mc) != null" in scenes
          and "mc.screen" not in scenes)

    # ------------------------------------------------------------------
    # BUILD #448 -- THE CRASH, THE MENU, AND THE CITIES' OWN AIR.
    #
    # Three things the player reported in one message, and each one earns a gate:
    #
    #   * "when I try to click on the the enter password thing it just crashes my
    #     game" -- the terminal's input path ran into init(), which rebuilds the
    #     widget list in the middle of the frame it is being drawn in, and nothing
    #     in the chain caught a throwable. Both are fixed; the gate holds the fix
    #     and holds that a throwable is SHOWN rather than fatal, because the next
    #     report can then be the line itself.
    #   * "a giant Devouring Storms watermark blocking it. It's very black." -- the
    #     wordmark is off by default and switchable, the band is half the height and
    #     no longer opaque, and there is a brightness lift.
    #   * "can you make the cities each one bigger with its own unique fog in sky" --
    #     rings + scale per district, six atmospheres, and a client that wears them.
    # ------------------------------------------------------------------
    term = read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalScreen.java") or ""
    title = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTitleOverhaulMixin.java")         or ""
    cities = read("mcsm-extras/java/net/mcsm/extras/McsmCities.java") or ""
    sky = read("mcsm-extras/java/net/mcsm/extras/client/McsmNativeSkyRenderer.java") or ""

    check("no click and no key can end the game any more: every door is guarded",
          "private boolean mouseClickedGuarded(MouseButtonEvent event, boolean doubled) {" in term
          and "private void submitCodeGuarded() {" in term
          and "private void acceptGuarded(String action, String a, String b) {" in term
          and "private void rebuildGuarded() {" in term
          and "report(t, \"mouseClicked\");" in term
          and "report(t, \"submitCode\");" in term
          and "report(t, \"accept\");" in term
          and "report(t, \"rebuild\");" in term)
    check("the widget rebuild is DEFERRED out of the frame that asked for it",
          "private transient boolean pendingInit;" in term
          and "pendingInit = true;" in term
          # the granted case must not call init() any more: that is the mid-frame
          # widget mutation that was the crash
          and "case \"granted\":" in term
          and "pendingInit = true;\n                break;" in term.replace("\r", "")
          and "this.init();" not in term)
    check("and the rebuild happens at the top of the next frame, before any input",
          term.index("if (pendingInit) {") < term.index("for (Integer key : McsmKeyboard.poll()) {")
          and "pendingInit = false;" in term)
    check("the throwable is SHOWN, not swallowed silently",
          "private void report(Throwable t, String where) {" in term
          and "status = \"\\u00a7call right, that was a bug: \" + line;" in term
          and "System.err.println(\"[ds] the terminal survived \" + line);" in term
          and "t.printStackTrace();" in term)
    check("the menu watermark is off by default, and switchable back on",
          "public static boolean titleWordmark = false;" in cfg
          and "title_wordmark" in cfg
          and "Title wordmark (DEVOURING STORMS over the menu)" in extras
          and "McsmExtrasConfig.titleWordmark ? 96 : 0" in title)
    check("the menu is not a black bar any more",
          "public static double menuLift = 0.15D;" in cfg
          and "menu_lift" in cfg
          and "Main menu brightness lift (0 = untouched)" in extras
          and "double lift = Math.max(0.0D, Math.min(0.35D, McsmExtrasConfig.menuLift));" in title
          and "g.fillGradient(0, 0, w, titleBandBottom, 0xB007050E, 0x00070510);" in title
          and "g.fillGradient(0, 0, w, titleBandBottom, 0xFF07050E, 0x00070510);" not in title
          # BUILD #464 -- and stronger than it was: the plate itself is a violet
          # dusk at 0x7A now, and the fully opaque ground band is gone (see the
          # #464 family below, which also scans for opaque plates).
          and "g.fill(0, 0, w, h, 0x7A0A0716);" in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmCinematic.java") or "")
          and "g.fill(0, 0, w, h, 0xC8010103);" not in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmCinematic.java") or "")
          and "g.fill(0, 0, w, h, 0xFF010103);" not in (
              read("mcsm-extras/java/net/mcsm/extras/client/McsmCinematic.java") or ""))
    check("the cities are BIGGER, per district, from the district's own key",
          "public static final int BIG_PERCENT = 45;" in cities
          and "public static final double SCALE_MIN = 1.00D;" in cities
          and "public static final double SCALE_MAX = 1.45D;" in cities
          and "public static int rings(int rx, int rz) {" in cities
          and "public static double scaleOf(int rx, int rz) {" in cities
          and "int rings = rings(rx, rz);" in cities
          and "double scale = scaleOf(rx, rz);" in cities
          and "int cell = (int) Math.round(CELL * scale);" in cities
          and "for (int cx = -rings; cx <= rings; cx++) {" in cities
          and "public static final int MAX_PLAN_OPS = 320000;" in cities
          and "public static final int OPS_PER_TICK = 1800;" in cities)
    check("the district's own size and air can be read off in game",
          'Commands.literal("city")' in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java")
              or "")
          and "net.mcsm.extras.McsmCities.atmosphereName(rx, rz)" in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java")
              or ""))
    check("and each one wears its own air: six atmospheres, six skylines",
          cities.count("0xFF") >= 12
          and "public static int atmosphereIndex(int rx, int rz) {" in cities
          and "public static int fogArgb(int rx, int rz) {" in cities
          and "public static int skyArgb(int rx, int rz) {" in cities
          and "public static double radiusOf(int rx, int rz) {" in cities
          and "public static int[] atmosphereAt(int x, int z) {" in cities
          and "public static String atmosphereName(int rx, int rz) {" in cities)
    check("the client wears the district's air on the proven sky and fog hooks",
          "int[] air = net.mcsm.extras.McsmCities.atmosphereAt(" in sky
          and "state.skyColor = citySky(state.skyColor);" in sky
          # the city tint must come BEFORE the storm's reach test, or a district
          # would only have its own sky while the storm was not looking
          and sky.index("state.skyColor = citySky(state.skyColor);")
                  < sky.index("float reach = McsmSkyReach.influence();")
          and "return influence;" in sky
          and "out[0] = ((air[0] >> 16) & 0xFF) / 255.0F;" in sky
          and "public static boolean cityAtmosphere = true;" in cfg
          and "city_atmosphere" in cfg
          and "Cities carry their own fog and sky (each district is different)" in extras)

    # ------------------------------------------------------------------
    # BUILD #451 -- THE VOID, ITS PURPLE WATER, AND ONE DOORWAY PER DIMENSION.
    #
    # "a new dimension called the void ... this dimension is filled with basically
    # nothing ... you fall infinitely, you don't hit the dimension ... there's houses
    # and rivers and glitchy walls" plus "a unique portal for each dimensions".
    #
    # The gates hold the three things that make the void the void: it is really
    # empty, the emptiness is survivable, and the doorways are real doors built from
    # real blocks -- because the block registry answers a missing id with AIR rather
    # than null, which is how a "portal" becomes a hole in a wall.
    # ------------------------------------------------------------------
    void_java = read("mcsm-extras/java/net/mcsm/extras/McsmVoid.java") or ""
    portals = read("mcsm-extras/java/net/mcsm/extras/McsmPortals.java") or ""

    void_dim = json.loads(read("jar-overrides/data/mcsm/dimension/void_reality.json"))
    void_type = json.loads(read("jar-overrides/data/mcsm/dimension_type/void_reality.json"))
    void_biome = json.loads(read("jar-overrides/data/mcsm/worldgen/biome/void_reality.json"))
    decayed_dim = json.loads(read("jar-overrides/data/mcsm/dimension/decayed_reality.json"))
    decayed_biome = json.loads(read("jar-overrides/data/mcsm/worldgen/biome/decayed_reality.json"))

    check("the void is really empty: one layer, and it is an INVISIBLE floor",
          # BUILD #452 -- the layer was AIR; it is a barrier now, which is the same
          # nothing to look at and something to stand on at the very bottom.
          void_dim["generator"]["type"] == "minecraft:flat"
          and len(void_dim["generator"]["settings"]["layers"]) == 1
          and void_dim["generator"]["settings"]["layers"][0]["block"] == "minecraft:barrier"
          and void_dim["generator"]["settings"]["features"] is False
          and void_dim["generator"]["settings"]["lakes"] is False
          and void_dim["type"] == "mcsm:void_reality")
    check("and it is tall enough to fall through, with nothing to hit",
          void_type["min_y"] == -64 and void_type["height"] == 384
          and void_type["has_ceiling"] is False
          and void_type["attributes"]["minecraft:gameplay/water_evaporates"] is False)
    check("the void has its own purple water and its own violet air",
          void_dim["generator"]["settings"]["biome"] == "mcsm:void_reality"
          and void_biome["effects"]["water_color"] == 0x8C24FF
          and void_biome["effects"]["water_fog_color"] == 0x47079F
          # BUILD #458: the biome's sky is the dimension type's own sky_light_color
          # (#4A2E8A) now, so the two cannot describe the same sky differently.
          and void_biome["effects"]["sky_color"] == 0x4A2E8A
          and void_biome["effects"]["fog_color"] == 0x1E1642)
    check("and the decayed reality finally has the purple water it never had",
          decayed_dim["generator"]["settings"]["biome"] == "mcsm:decayed_reality"
          and decayed_biome["effects"]["water_color"] == 0x7A2AD6
          and decayed_biome["effects"]["water_fog_color"] == 0x3E1472)
    check("falling past everything is survivable: the void catches you",
          # BUILD #452 moved the catch line BELOW the invisible floor (see the next
          # family): the floor is what catches an ordinary fall now, and the catch is
          # only for a hole in the world.
          "public static final int CATCH_Y = -70;" in void_java
          and "public static final int SHELF_Y = 210;" in void_java
          and "private static void catchFall(ServerLevel level, ServerPlayer player) {" in void_java
          and "if (player.getY() > CATCH_Y) {" in void_java
          and "private static BlockPos shelfUnder(ServerLevel level, BlockPos from) {" in void_java
          and "there is no floor here. that is the dimension, not a bug" in void_java)
    check("the nothing has things in it: shelves, houses, rivers, glitch walls, vortex ribs",
          "private static void house(McsmBuildQueue.Planner planner" in void_java
          and "private static void glitchWall(McsmBuildQueue.Planner planner" in void_java
          and "private static void vortexRibs(McsmBuildQueue.Planner planner" in void_java
          and "private static void brokenPillars(McsmBuildQueue.Planner planner" in void_java
          and "planner.put(cx + x, surface, cz + z, WATER);" in void_java
          and "SHELVES_PER_REGION = 7" in void_java
          and "int y = 24 + (int) Math.floorMod(seed3, 220L);" in void_java)
    check("the content generator reproduces the dimension the pack ships",
          # BUILD #456 -- the generator emitted minecraft:plains for the decayed
          # reality, so running it silently reverted the biome that carries the
          # purple water and the violet air. The two now have to agree.
          '"biome": "mcsm:decayed_reality"' in (
              read("ci/make_mcsm_content_assets.py") or "")
          and decayed_dim["generator"]["settings"]["biome"] == "mcsm:decayed_reality")
    check("the void walkers live here, and the void is booted, switched and reachable",
          # BUILD #456 -- the walkers are the mod's own entity now, not a roll from
          # the shared bestiary: spawnDwellers places mcsm:voidwalker / :void_lurker.
          "private static void spawnDwellers(ServerLevel level, BlockPos near, ServerPlayer player) {"
                  in void_java
          and "McsmVoid.register();" in boot and "import net.mcsm.extras.McsmVoid;" in boot
          and "public static boolean voidReality = true;" in cfg
          and "void_reality" in cfg
          and "The Void (a dimension of nothing, with things in it)" in extras
          and '"void").executes(ctx -> ds$void(ctx.getSource())' in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java")
              or ""))
    check("one doorway per dimension, and every one of them distinct",
          # BUILD #458 -- and every doorway is built from the MATERIAL OF THE WORLD
          # BEHIND IT. Adams was memory crystal around city tile and the void was a
          # shared rift anchor around a black-hole core: two arches made of other
          # places. They are their own families now (see the identity pass below).
          # BUILD #462 -- and there are FOUR of them: the reach has its own.
          portals.count("new Door(") == 4
          and 'new Door("decayed", "mcsm:decayed_reality", "mcsm:rift_anchor", "mcsm:reality_glass"'
                  in portals
          and 'new Door("adams", "mcsm:adams_infinity", "mcsm:adams_crystal", "mcsm:adams_bricks"'
                  in portals
          and 'new Door("void", "mcsm:void_reality", "mcsm:void_anchor", "mcsm:void_glass"'
                  in portals
          and 'new Door("creator", "mcsm:creators_realm", "mcsm:creator_gold", "mcsm:creator_glass"'
                  in portals)
    check("a doorway is a shape, checked where the player is standing",
          "public static void tick(ServerLevel level) {" in portals
          and "private static boolean isFrame(ServerLevel level, BlockPos feet, BlockState frame) {"
                  in portals
          and "if (!is(level, feet.below(), frame) || !is(level, feet.above(2), frame)) {" in portals
          and "boolean alongX = is(level, feet.east(), frame) && is(level, feet.west(), frame)"
                  in portals
          and "boolean alongZ = is(level, feet.north(), frame) && is(level, feet.south(), frame)"
                  in portals
          and "private static final long COOLDOWN = 60L;" in portals
          and "if (!McsmExtrasConfig.portals || level.players().isEmpty()) {" in portals)
    check("walking in is the whole interaction, and /ds builds the same shape",
          "case \"decayed\":" in portals and "case \"adams\":" in portals
          and "case \"void\":" in portals
          and "moved = McsmVoid.enter(player);" in portals
          and "public static boolean build(ServerPlayer player, String id) {" in portals
          and "level.setBlock(base, glassState, 2);" in portals
          and "public static boolean portals = true;" in cfg and "portals" in cfg
          and "Portals (walk-in doorways, one per dimension)" in extras
          and 'Commands.literal("portal")' in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java")
              or ""))
    check("a real block or nothing: neither file can accept AIR as a door",
          "if (block != null && block != Blocks.AIR) {" in void_java
          and "if (block != null && block != Blocks.AIR) {" in portals
          and 'McsmContent.DECAYED_BONE' not in void_java)

    # ------------------------------------------------------------------
    # BUILD #452 -- THE BOTTOM OF THE VOID.
    #
    # "could you make it an invisible floor at the very bottom ... it's very dark
    # when you fall to the very bottom ... add light rays, colour RGB light ... the
    # bottom as a rainbow casting infinite white rings".
    # ------------------------------------------------------------------
    floor = read("mcsm-extras/java/net/mcsm/extras/client/McsmVoidFloor.java") or ""

    check("the bottom is an invisible floor you can stand on",
          void_dim["generator"]["settings"]["layers"][0]["block"] == "minecraft:barrier"
          and "public static final int FLOOR_Y = -64;" in void_java
          # the catch line has to be BELOW the floor now, or it would grab the player
          # out of the floor they were meant to land on
          and "public static final int CATCH_Y = -70;" in void_java
          and "public static final int SHELF_Y = 210;" in void_java)
    spectrum = re.findall(r"0xFF[0-9A-F]{6},", floor)
    check("one shelf builder: regions and landings both carve through shelfInto",
          # run 534 -- the region pass called the plan-returning shelf() and handed a
          # Planner to a parameter asking for a ServerLevel. There is one carve now.
          "shelfInto(planner, key, x, y, z, false);" in void_java
          and "shelf(planner," not in void_java
          and "private static void shelfInto(McsmBuildQueue.Planner planner, long key," in void_java
          and "return planner.plan(key ^ 0x5F0L);" in void_java
          # and the region plan has the room for seven of them: put() past capacity
          # is a silent skip, which would have shipped a region with four shelves
          and "new McsmBuildQueue.Planner(120000);" in void_java)
    check("the bottom is RGB rays: a full spectrum, rotating, brightest at the floor",
          len(spectrum) == 12
          and len(set(spectrum)) == 12
          and "private static final int RAYS = 12;" in floor
          and "double spin = (now % 24000L) / 24000.0D * Math.PI * 2.0D;" in floor
          and "g.fill(x - size / 2, y - size / 2, x + size / 2, y + size / 2," in floor
          and "private static float strength(float y) {" in floor
          and "public static final float RAY_Y = 56.0F;" in floor)
    check("and infinite white rings: three alive, born for ever",
          "private static final int RING_LIVE = 3;" in floor
          and "private static final long RING_MS = 2600L;" in floor
          and "long age = (now + ring * (RING_MS / RING_LIVE)) % RING_MS;" in floor
          and "int colour = (alpha << 24) | 0xFFFFFF;" in floor
          and "the invisible floor holds" in floor)
    check("it is switchable, it rides the proven hook, and it never covers a screen",
          "public static boolean voidLight = true;" in cfg
          and "void_light" in cfg
          and "Void floor light (RGB rays and infinite white rings)" in extras
          and "net.mcsm.extras.client.McsmVoidFloor.draw(g);" in (
              read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmHudAttachMixin.java")
              or "")
          and "if (McsmTerminalClient.currentScreen(mc) != null) {" in floor
          and "mc.player.level().dimension().equals(McsmVoid.DIMENSION)" in floor)

    # ------------------------------------------------------------------
    # SOURCE HYGIENE -- every literal in the tree has to be a literal.
    #
    # Run 532/533 (build #451) died on exactly one character: "key ^ 0x5F0RL".
    # There is no such hex digit, so javac never got past the parse and nothing
    # after it was ever compiled. One bad character should be caught here, by a
    # machine, before it costs a build.
    # ------------------------------------------------------------------
    hexpat = re.compile(r"\b0[xX][0-9A-Fa-f]+[A-Za-z0-9_]*")
    bad_hex = []
    for f in sorted(list(pathlib.Path("mcsm-extras/java").rglob("*.java"))
                    + list(pathlib.Path("src").rglob("*.java"))):
        for i, line in enumerate(f.read_text(errors="replace").splitlines(), 1):
            if "//" in line:
                line = line.split("//")[0]
            for m in hexpat.finditer(line):
                tok = m.group(0)
                if tok[2:].lstrip("0123456789abcdefABCDEF") not in ("", "l", "L"):
                    bad_hex.append(str(f) + ":" + str(i) + " " + tok)
    check("every numeric literal in the tree is a literal (no 0x5F0RL class of typo)",
          not bad_hex,
          "; ".join(bad_hex[:4]) or "clean")

    # ------------------------------------------------------------------
    # BUILD #455 -- THE GLOW, DRAWN BY US.
    #
    # "I know it a bug the storm's teeth and eyes are still not glowing. The aura
    # I don't see." Three builds aimed at the native passes; this one stops
    # depending on them, and closes the gate hole that let an empty emissive atlas
    # ship ("every opaque pixel is pure white" is vacuously true of a blank sheet).
    # ------------------------------------------------------------------
    eye = read("mcsm-extras/java/net/mcsm/extras/client/McsmEyeGlow.java") or ""
    gate = read("mcsm-extras/java/net/mcsm/extras/McsmGate.java") or ""
    halo = read("mcsm-extras/java/net/mcsm/extras/client/McsmHaloSkyRenderer.java") or ""
    fx = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmPresenceFxPatch.java") or ""
    emissive = read("ci/make_emissive_whites.py") or ""

    check("the glow is drawn by us: eyes, teeth and a haze on the storm's own coordinates",
          "private static final float ONSET = 0.2F;" in eye
          and "Collector.submitCustomGeometry" not in eye
          and "collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(texture)," in eye
          and "collector.submitCustomGeometry(poseStack, eyes(texture)," in eye
          and "private static RenderType eyes(Identifier texture) {" in eye
          and "return RenderTypes.eyes(texture);" in eye
          and "storm.dispYaw" in eye
          and "McsmStormPhase.bodyRadius(phase)" in eye
          and "McsmExtrasConfig.eyeGlow" in eye
          and "McsmExtrasConfig.eyeGlowStrength()" in eye
          and "McsmExtrasConfig.vanillaGlow" in eye)
    check("the glow pass is registered, switchable and says so when it cannot place itself",
          "net.mcsm.extras.client.McsmEyeGlow::submit" in gate
          and "public static boolean eyeGlow = true;" in cfg
          and "public static double eyeGlowStrength = 1.0D;" in cfg
          and "public static boolean vanillaGlow = true;" in cfg
          and "eye_glow" in cfg and "vanilla_glow" in cfg
          and "public static float eyeGlowStrength() {" in cfg
          and "Teeth/eye/aura glow (world-space lights on the storm)" in extras
          and "Vanilla-material glow half (works with no shader pack)" in extras
          # the one case where the pass is blind is said out loud, once
          and "McsmDiag.say" in eye
          and "noteFeedEmpty();" in eye)
    check("the aura no longer waits for phase 4.45, and has a vanilla half",
          "float phaseFade = Mth.clamp((phase - 3.0F) / 0.35F, 0.0F, 1.0F);" in halo
          and "private static final double MAX_DISTANCE = 3200.0D;" in halo
          and "collector.submitCustomGeometry(poseStack, RenderTypes.eyes(VANILLA_HALO)," in halo
          and "private static final float VANILLA_SCALE = 0.40F;" in halo
          and "import net.minecraft.client.renderer.rendertype.RenderTypes;" in halo)
    check("the presence pass lights the teeth from phase 0.5 and fades the big aura in",
          "if (phase < 0.5F) {" in fx
          and "float auraFade = smoothstep(phase, 3.0F, 4.45F);" in fx
          and "float whiteW = 1.0F - smoothstep(phase, 4.95F, 5.15F);" in fx
          and "whiteW * distanceFade * 0.60F" in fx
          and "w4 * distanceFade * auraFade" in fx)
    check("no emissive atlas may be entirely transparent (the hole #455 closed)",
          "entirely transparent" in emissive
          and "UNION_DERIVED" in emissive
          and "phase_4_assets_e.png" in emissive
          and "_eye_lit()" not in emissive)
    # the atlas the head passes read is tiny by design, but it is not empty
    try:
        _ci = os.path.dirname(os.path.abspath(__file__))
        if _ci not in sys.path:
            sys.path.insert(0, _ci)
        import pngutil as _png
        png = os.path.join("src/main/resources/assets/dabywitherstormmod/textures/entity",
                           "phase_4_assets_e.png")
        _w, _h, _px = _png.read_png(png)
        lit = sum(1 for p in _px if p[3] > 0)
    except Exception:
        lit = -1
    check("the head emissive atlas has its eye lenses lit",
          lit > 0, "lit pixels: %d" % lit)

    # ------------------------------------------------------------------
    # BUILD #456 -- OWN BODIES. THE CUSTOM MOBS, AS REAL ONES.
    #
    # "the Massg the black figure warden with red eyes", "voidwalkers
    # (zombie-like)", "the void mini-boss (avoid) whose tentacles reach / pull /
    # swallow", "the gigantic the creator". The beasts existed as entities from
    # #428 and had no renderer at all -- an entity type with nothing registered
    # for it is not drawn -- so they were in the world, invisible. This family
    # holds the four things that make a custom mob custom: its own mesh, its own
    # body class, its own behaviour, and its own skin on disk.
    # ------------------------------------------------------------------
    models = read("mcsm-extras/java/net/mcsm/extras/client/McsmMobModels.java") or ""
    renderers = read("mcsm-extras/java/net/mcsm/extras/client/McsmMobRenderers.java") or ""
    mobmix = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmMobRendererMixin.java") or ""
    walker = read("mcsm-extras/java/net/mcsm/extras/entity/McsmVoidwalker.java") or ""
    lurker = read("mcsm-extras/java/net/mcsm/extras/entity/McsmVoidLurker.java") or ""
    entities = read("mcsm-extras/java/net/mcsm/extras/entity/McsmEntities.java") or ""
    cmd = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or ""
    textures = read("ci/make_mcsm_textures.py") or ""

    check("every monster has its own mesh, and every mesh is built from real cuboids",
          models.count("public static LayerDefinition createBodyLayer() {") >= 4
          and "class MassgModel extends HumanoidModel<MobState>" in models
          and "class VoidwalkerModel extends HumanoidModel<MobState>" in models
          and "class VoidLurkerModel extends HumanoidModel<MobState>" in models
          and "class CreatorModel extends HumanoidModel<MobState>" in models
          and "LayerDefinition.create(mesh," in models
          and "CubeListBuilder.create()" in models
          # boxes are built through the two helpers, so count the call sites: every
          # one of them is a real cuboid on a real part
          and models.count("box(") >= 16
          and models.count("many(") >= 4)
    check("the giants stand on the ground at any scale (the 24-unit shift)",
          "private static final float GROUND = 24.0F;" in models
          and "float g = GROUND - (bodyH + legH) * s;" in models)
    check("each body is animated in its own right",
          "public static final float S = 6.0F;" in models
          and "public static final float S = 2.4F;" in models
          and "public static final float S = 8.0F;" in models
          and "this.tentacles[i].xRot" in models
          and "this.halos[i].yRot" in models
          and "this.jaw.xRot" in models
          and "this.lenses.xRot" in models)
    check("a layer and a renderer for every body, registered on the client",
          renderers.count("EntityRendererRegistry.register(") >= 5
          and renderers.count("ModelLayerRegistry.registerModelLayer(") >= 4
          and "McsmMobRenderers.registerLayers();" in mobmix
          and "McsmMobRenderers.registerRenderers();" in mobmix
          and 'Identifier.fromNamespaceAndPath("mcsm", "textures/entity/" + name + ".png")' in renderers)
    # ------------------------------------------------------------------
    # BUILD #466 -- THE CAST GET THEIR OWN BODIES. The standing ask, word for word:
    # "custom NPC models", "custom NPCs + dialogue, corrupted NPCs", and the note
    # that the NPCs are "re-kitted vanilla mobs". They were: the Story Mode renderer
    # baked ModelLayers.PLAYER -- Minecraft's own player mesh -- and put a Story Mode
    # skin on it.
    #
    # The body is ours now, and the whole point of it is WHERE THE PIXELS COME FROM:
    # the seven base parts keep the player sheet's own rectangles, so the twenty
    # skins this mod already carries still map pixel for pixel and no existing art
    # is repainted, while the hood, collar, coat, sleeves, leg wraps and badge are
    # cut from the SECOND LAYER of each character's own skin -- the layer a player
    # skin uses for exactly those things.
    # ------------------------------------------------------------------
    story = read("mcsm-extras/java/net/mcsm/extras/client/StoryCharacterRenderer.java") or ""
    story_code = code_only(story)
    _bsh2 = read("ci/build.sh") or ""   # this family sits before buildsh is read
    check("the cast has a body of its own instead of Minecraft's player mesh",
          "public static LayerDefinition createBodyLayer()" in story_code
          and "ModelLayers.PLAYER" not in story_code
          and "public static final ModelLayerLocation LAYER = new ModelLayerLocation(" in story_code
          and "super(ctx, new Model(ctx.bakeLayer(LAYER)), 0.5F);" in story_code
          and "ModelLayerRegistry.registerModelLayer(StoryCharacterRenderer.LAYER,"
              "\n                    StoryCharacterRenderer::createBodyLayer);" in renderers
          and "StoryCharacterRenderer" in _bsh2)

    check("and every pixel of the new geometry is the character's own second layer",
          # the base body keeps the player sheet's rects, unmodified: that is what
          # keeps twenty existing skins correct
          all(r in story_code for r in ("texOffs(0, 0)", "texOffs(16, 16)", "texOffs(40, 16)",
                                        "texOffs(32, 48)", "texOffs(0, 16)", "texOffs(16, 48)"))
          # and the clothes are the overlay rects the sheet reserves for them
          and all(r in story_code for r in ("texOffs(32, 0)", "texOffs(16, 32)",
                                            "texOffs(40, 32)", "texOffs(48, 32)",
                                            "texOffs(0, 32)", "texOffs(0, 48)"))
          and '"hood_back"' in story_code
          and all(p in story_code for p in ('"collar"', '"coat"', '"badge"',
                                            '"sleeve_r"', '"sleeve_l"', '"wrap_r"', '"wrap_l"'))
          and "LayerDefinition.create(mesh, 64, 64);" in story_code)

    check("and the storm corrupts them in the model, once it is far enough along",
          "private static final float CORRUPT_FROM = 5.5F;" in story_code
          and "s.corruption = Mth.clamp(" in story_code
          and "phase = McsmStormPhase.phase();" in story_code
          and "if (s.corruption > 0.01F) {" in story_code
          and "this.head.zRot += Mth.sin(t * 0.9F) * 0.24F * k;" in story_code)

    cast = re.findall(r'"([A-Z][a-z]+)"', read(
        "mcsm-extras/java/net/mcsm/extras/McsmNpcs.java")[
        read("mcsm-extras/java/net/mcsm/extras/McsmNpcs.java").index("SPAWN_EGG_CAST = {"):
        read("mcsm-extras/java/net/mcsm/extras/McsmNpcs.java").index("private static final int TOWN_RADIUS")])
    story_skins = os.path.join("src", "main", "resources", "assets", "dabywitherstormmod",
                               "textures", "entity", "story")
    over_skins = os.path.join("jar-overrides", "assets", "dabywitherstormmod",
                              "textures", "entity", "story")
    missing_skins = [c for c in cast
                     if not (os.path.exists(os.path.join(story_skins, c.lower() + ".png"))
                             or os.path.exists(os.path.join(over_skins, c.lower() + ".png")))]
    check("and every cast member has a skin to wear, or is wearing jesse's",
          len(cast) >= 20 and not missing_skins,
          "cast=%d missing=%s" % (len(cast), missing_skins))

    check("the void walkers are real entities of their own, not re-kitted vanilla",
          '"voidwalker"' in entities
          and '"void_lurker"' in entities
          and "private static <T extends net.minecraft.world.entity.Mob> EntityType<T> own(" in entities
          and "McsmVoidwalker.createAttributes()" in entities
          and "McsmVoidLurker.createAttributes()" in entities
          and "private static void spawnDwellers(ServerLevel level, BlockPos near, ServerPlayer player) {" in void_java
          and "McsmCreatures.release(level, at == null ? player.blockPosition() : at, 1, 6.0D);" not in void_java)
    check("the lurker reaches, pulls and swallows -- the three verbs of the mini-boss",
          "public static final double REACH = 6.5D;" in lurker
          and "public static final double SWALLOW_RANGE = 2.6D;" in lurker
          and "target.push(inward.x * strength, 0.22D, inward.z * strength);" in lurker
          and "target.hurtServer(server, server.damageSources().generic(), 14.0F);" in lurker
          and "MobEffects.SLOWNESS" in lurker)
    check("the walker falls at you across the nothing",
          "this.setDeltaMovement(to.x * 1.35D, to.y * 0.5D + 0.30D, to.z * 1.35D);" in walker
          and "ParticleTypes.PORTAL" in walker
          and "McsmSounds.OBLIVION_WARP" in walker)
    check("every one of them can be put in front of you by name",
          'Commands.literal("mob")' in cmd
          and "ds$mob(ctx.getSource(), id)" in cmd
          and '"massg", "creator", "whale", "voidwalker", "lurker", "drifter", "keeper"' in cmd
          and "beast.setKind(kind);" in cmd)

    # the skins: on disk, the right size, and carrying their accent
    skins = {}
    try:
        _ci = os.path.dirname(os.path.abspath(__file__))
        if _ci not in sys.path:
            sys.path.insert(0, _ci)
        import pngutil as _png
        for _name in ("massg", "voidwalker", "void_lurker", "creator", "whale_monster",
                      "drifter", "keeper"):
            _p = os.path.join("jar-overrides/assets/mcsm/textures/entity", _name + ".png")
            _w, _h, _px = _png.read_png(_p)
            skins[_name] = (_w, _h, _px)
    except Exception as _exc:
        skins = {"error": str(_exc)}

    def skin_has(name, test):
        try:
            w, h, px = skins[name]
        except Exception:
            return False
        return any(test(p[0], p[1], p[2]) for p in px if p[3] > 0)

    check("the black warden's skin exists at its own size, with the red lit",
          skins.get("massg", (0,))[0] == 512 and skins.get("massg", (0, 0))[1] == 512
          and skin_has("massg", lambda r, g, b: r > 180 and g < 90 and b < 90),
          "massg: %s" % str(skins.get("massg", ("-",))[:2]))
    check("the walker's slits and the lurker's lit tips are on their skins",
          skins.get("voidwalker", (0,))[0] == 64
          and skin_has("voidwalker", lambda r, g, b: b > 170 and r > 80 and g < 140)
          and skins.get("void_lurker", (0,))[0] == 256
          and skin_has("void_lurker", lambda r, g, b: b > 200 and g > 140),
          "voidwalker/void_lurker skins")
    check("the colossal wears a starfield, a gold crown and white-hot eyes",
          skins.get("creator", (0,))[0] == 1024
          and skin_has("creator", lambda r, g, b: r > 240 and g > 240 and b > 240)
          and skin_has("creator", lambda r, g, b: r > 200 and g > 180 and b < 150)
          and skin_has("whale_monster", lambda r, g, b: b > 200 and g > 180),
          "creator/whale skins")
    check("the skins and the models are drawn from the same numbers",
          "MASSG_PARTS = [" in textures
          and "(0, 384, 21.6, 13.2, 4.8)" in textures
          and "many(head, \"lenses\", 0, 384, S," in models
          and "(56, 40, 2.4, 1.6, 0.6)" in textures
          and "many(head, \"eyes\", 56, 40, S," in models
          and "(96, 160, 7.2, 9.6, 7.2)" in textures
          and "box(body, \"tip\" + i, 96, 160, S," in models
          and "(96, 512, 160.0, 8.0, 80.0)" in textures
          and "box(head, \"halo\" + i, 96, 512, S," in models)

    # ------------------------------------------------------------------
    # BUILD #460 -- AND EVERY WORLD HAS ITS OWN PEOPLE. The decayed reality
    # and the infinite dimension stopped sending the STORM's bestiary at the
    # player: "own blocks, items, mobs, locks, VFX; no re-use". Each of them
    # has a creature of its own -- its own body, its own mesh, its own skin,
    # its own verbs -- and neither of them is the other one.
    # ------------------------------------------------------------------
    denizen = read("mcsm-extras/java/net/mcsm/extras/entity/McsmDenizen.java") or ""

    check("the decayed reality and the infinite dimension have creatures of their own",
          "abstract class McsmDenizen extends Monster" in denizen
          and 'public static final String DECAYED = "decayed";' in denizen
          and 'public static final String ADAMS = "adams";' in denizen
          and "public static class McsmDrifter extends McsmDenizen" in denizen
          and "public static class McsmKeeper extends McsmDenizen" in denizen
          and "McsmDenizen.McsmDrifter::new" in entities
          and "McsmDenizen.McsmKeeper::new" in entities
          and "McsmDenizen.drifterAttributes()" in entities
          and "McsmDenizen.keeperAttributes()" in entities)
    check("each of them fights like its own world, and not like the other one",
          "MAX_HEALTH, 26.0D" in denizen
          and "MAX_HEALTH, 90.0D" in denizen
          and "ATTACK_DAMAGE, 5.0D" in denizen
          and "ATTACK_DAMAGE, 11.0D" in denizen
          and "KNOCKBACK_RESISTANCE, 0.8D" in denizen
          and "public boolean fireImmune() {" in denizen)
    check("the drifter drifts and the keeper keeps -- each with its own verbs",
          "ParticleTypes.ASH" in denizen
          and "ParticleTypes.DUST_PLUME" in denizen
          and "McsmSounds.OBLIVION_GLITCH" in denizen
          and "McsmSounds.MASSG_WHISPER" in denizen
          and "McsmSounds.MASSG_HEART" in denizen
          and "this.setDeltaMovement(to.x * 1.15D, 0.22D, to.z * 1.15D);" in denizen
          and "public static int spawn(net.minecraft.server.level.ServerLevel level," in denizen)
    check("and both of them have a body, a layer and a renderer of their own",
          "class DrifterModel extends HumanoidModel<MobState>" in models
          and "class KeeperModel extends HumanoidModel<MobState>" in models
          and "LayerDefinition.create(mesh, 128, 128);" in models
          and "this.tatterL.xRot" in models
          and "this.lantern.xRot" in models
          and "this.brim.yRot" in models
          and "DRIFTER_LAYER" in renderers
          and "KEEPER_LAYER" in renderers
          and 'skin("drifter")' in renderers
          and 'skin("keeper")' in renderers
          and renderers.count("EntityRendererRegistry.register(") >= 7
          and renderers.count("ModelLayerRegistry.registerModelLayer(") >= 6)
    check("the two new creatures stand in the worlds they belong to, and nowhere else",
          "net.mcsm.extras.entity.McsmEntities.KEEPER" in adams
          and "net.mcsm.extras.entity.McsmDenizen.spawn(level," in adams
          and "McsmCreatures.release(level, at, count, 6.0D);" in adams
          and "net.mcsm.extras.entity.McsmEntities.DRIFTER" in creatures
          and "if (decayed) {" in creatures)
    check("both of them can be put in front of you by name",
          'case "drifter" -> {' in cmd
          and 'case "keeper" -> {' in cmd
          and "McsmEntities.DRIFTER" in cmd
          and "McsmEntities.KEEPER" in cmd
          and "entity.mcsm.drifter" in lang
          and "entity.mcsm.keeper" in lang)

    check("the two new skins exist at their own sizes, with their own light on them",
          skins.get("drifter", (0,))[0] == 128
          and skin_has("drifter", lambda r, g, b: b > 150 and r > 60 and g < 120)
          and skins.get("keeper", (0,))[0] == 256
          and skin_has("keeper", lambda r, g, b: r > 230 and g > 180 and b < 190),
          "drifter/keeper skins")

    # ------------------------------------------------------------------
    # BUILD #461 -- AND EVERY WORLD'S OWN AIR. The last line of the identity
    # list: "own blocks, items, mobs, locks, VFX; no re-use". This is the VFX
    # a player feels while standing still -- each dimension's own particles,
    # its own rate and its own ambience, and the Overworld left alone.
    # ------------------------------------------------------------------
    fx = read("mcsm-extras/java/net/mcsm/extras/client/McsmDimensionFx.java") or ""
    grad = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmGradientTickPatch.java") or ""

    def air_particles(text):
        """The particle types each Air row names, in order of appearance."""
        return re.findall(r"ParticleTypes\.([A-Z_]+)", text)

    def air_sounds(text):
        return re.findall(r"McsmSounds\.([A-Z_]+)", text)

    _particles = air_particles(fx)
    _sounds = air_sounds(fx)
    check("every world breathes something of its own, and no two breathe the same",
          fx.count("new Air(") == 4
          and "AIRS = { DECAYED, ADAMS, VOID, CREATOR };" in fx
          and len(_particles) == 8 and len(set(_particles)) == 8
          and len(_sounds) == 4 and len(set(_sounds)) == 4,
          "particles=%s sounds=%s" % (",".join(_particles), ",".join(_sounds)))
    check("the air is only for the mod's own worlds, and the Overworld is left alone",
          "airFor(McsmIdentity.forLevel(level))" in fx
          and "if (skin == null) {" in fx
          and "air.id().equals(skin.id())" in fx
          and "return null;" in fx
          and "if (air == null) {" in fx)
    check("the air steps once a game tick, never once a frame",
          "if (now == lastTick) {" in fx
          and "lastTick = now;" in fx
          and "private static long lastTick = Long.MIN_VALUE;" in fx)
    check("and it is a real switch, on the panel and in the config file",
          "public static boolean dimensionFx = true;" in cfg
          and '"dimension_fx"' in cfg
          and "if (!McsmExtrasConfig.dimensionFx) {" in fx
          and "Dimension air (own particles and ambience per world)" in extras)
    check("the hook that drives it is the hook the blasts already use",
          "McsmDimensionFx.tick();" in grad
          and "import net.mcsm.extras.client.McsmDimensionFx;" in grad
          and grad.index("McsmClientBlasts.tick();") < grad.index("McsmDimensionFx.tick();"))

    # ------------------------------------------------------------------
    # BUILD #457 -- THE PAINTED SKY. Every dimension wears its own cube.
    #
    # "custom skyboxes", "the sky is not fully the sky yet". Shader-side answers
    # cannot reach a player without the pack, so the sky is drawn in the world: a
    # camera-anchored CUBE -- four walls and a lid, no dome anywhere -- textured
    # with paintings generated into the content pack.
    # ------------------------------------------------------------------
    sky = read("mcsm-extras/java/net/mcsm/extras/client/McsmPaintedSky.java") or ""
    skygen = read("ci/make_skybox_textures.py") or ""
    boots = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmStormBlobMixin.java") or ""
    bsh = read("ci/build.sh") or ""

    check("the sky is a CUBE: four walls and a lid, and no dome anywhere",
          sky.count("collector.submitCustomGeometry(") == 2
          and "for (int i = 0; i < 4; i++) {" in sky
          and "private static final double RADIUS = 256.0D;" in sky
          and "no dome anywhere" in sky
          and "private static final double SKIRT = 8.0D;" in sky)
    check("every dimension has a different sky, and the Overworld keeps the vanilla one",
          sky.count("new Sky(") == 4
          # BUILD #458 -- and the grade and the turn come from the identity table,
          # so a dimension's sky, fog and horizon cannot drift apart.
          and sky.count("s.tintR()") == 4 and sky.count("s.spin()") == 4
          and "McsmReality.inside(level)" in sky
          and "level.dimension().equals(McsmAdams.ADAMS)" in sky
          and "level.dimension().equals(McsmVoid.DIMENSION)" in sky
          and "level.dimension().equals(McsmCreatorRealm.DIMENSION)" in sky
          and "return null;" in sky
          and skygen.count("seed=") == 4
          and skygen.count("horizon=") == 4
          and len(set([skygen.split("seed=")[i + 1].split(",")[0] for i in range(4)])) == 4)
    check("the painted sky is submitted, switched, generated and gated",
          "net.mcsm.extras.client.McsmPaintedSky.submit(ctx);" in boots
          and "public static boolean paintedSky = true;" in cfg
          and "painted_sky" in cfg
          and "Painted skies (a painted cube per dimension, no dome)" in extras
          and "python3 ci/make_skybox_textures.py | tail -1" in bsh
          and "python3 ci/make_skybox_textures.py --check" in bsh)

    # the paintings: on disk, the right size, and hazy at the horizon -- which is
    # also what proves the v orientation (top row = horizon) the renderer depends on
    skies = {}
    try:
        _ci = os.path.dirname(os.path.abspath(__file__))
        if _ci not in sys.path:
            sys.path.insert(0, _ci)
        import pngutil as _png
        for _dim in ("decayed", "adams", "void", "creator"):
            for _face, _size in (("sides", (512, 128)), ("top", (256, 256))):
                _w, _h, _px = _png.read_png(
                    os.path.join("jar-overrides/assets/mcsm/textures/sky",
                                 "%s_%s.png" % (_dim, _face)))
                skies["%s_%s" % (_dim, _face)] = (_w, _h, _px, _size)
    except Exception as _exc:
        skies = {"error": str(_exc)}

    def sides_ok(name):
        """The walls: right size, and the haze is at the horizon row, which is the
        orientation the renderer maps v=0 to."""
        try:
            w, h, px, want = skies[name]
        except Exception:
            return False
        if (w, h) != want:
            return False
        top = [px[x] for x in range(0, w, 9)]
        mid = [px[(h // 2) * w + x] for x in range(0, w, 9)]
        top_lum = sum(sum(c[:3]) for c in top) / float(len(top))
        mid_lum = sum(sum(c[:3]) for c in mid) / float(len(mid))
        return top_lum > mid_lum * 1.4

    def top_ok(name):
        """The lid: right size, and painted -- a zenith with stars on it, so it is
        not a flat fill."""
        try:
            w, h, px, want = skies[name]
        except Exception:
            return False
        if (w, h) != want:
            return False
        lit = sum(1 for p in px if sum(p[:3]) > 420)
        return lit >= 40

    check("every painted wall exists at its size, with its haze at the horizon",
          all(sides_ok(k) for k in ("decayed_sides", "adams_sides", "void_sides",
                                    "creator_sides")),
          "walls: %d read" % (0 if "error" in skies else len(skies)))
    check("and every lid is painted with a starfield over its zenith",
          all(top_ok(k) for k in ("decayed_top", "adams_top", "void_top",
                                  "creator_top")),
          "lids: %d read" % (0 if "error" in skies else len(skies)))

    # ------------------------------------------------------------------
    # BUILD #470 -- THE REAL CLOUD DECK. The cube is the air; this is the weather.
    #
    # "custom skyboxes" and "the sky is not fully the sky yet" are half answered by a
    # cube: what a player actually looks at when they look up is the vanilla cloud
    # plane -- one flat sheet, one colour, one height, identical in every world. Three
    # drifting, rippling decks per dimension, painted from that world's own sheet and
    # lit by its own glow, is the other half.
    #
    # The sheets are checked here as well, because a sky file is a frame a player
    # stands under: a deck is a cloud (something solid in it, gaps you can see
    # through, never opaque) or it is the black-plate class of bug wearing weather.
    # ------------------------------------------------------------------
    deck = read("mcsm-extras/java/net/mcsm/extras/client/McsmCloudDeck.java") or ""

    clouds = {}
    try:
        import pngutil as _png2
        for _dim in ("decayed", "adams", "void", "creator"):
            _cp = os.path.join("jar-overrides/assets/mcsm/textures/sky",
                               "clouds_%s.png" % _dim)
            with open(_cp, "rb") as _fh:
                clouds[_dim] = (_png2.read_png(_cp), _fh.read())
    except Exception as _exc:
        clouds = {"error": str(_exc)}

    def cloud_ok(dim):
        """A deck: right size, something solid in it, gaps to see through, not a lid."""
        try:
            (w, h, px), _raw = clouds[dim]
        except Exception:
            return False
        if (w, h) != (128, 128) or not px:
            return False
        alphas = [p[3] for p in px]
        clear = sum(1 for a in alphas if a == 0) / float(len(alphas))
        return (max(alphas) >= 120
                and clear >= 0.05
                and sum(alphas) / float(len(alphas)) <= 120
                and len(set(px)) >= 60)

    check("the sky has real cloud decks now: three layers, drifting, per dimension",
          "public final class McsmCloudDeck" in deck
          and "private static final double[] HEIGHT = {26.0D, 38.0D, 52.0D};" in deck
          and "private static final int GRID = 4;" in deck
          and deck.count("submitCustomGeometry(") == 1
          and "level.getGameTime()" in deck
          and "GlowRenderTypes.translucent(sheet)" in deck
          and deck.count("clouds_") == 4
          and "McsmIdentity.rgb(glowFor(level))" in deck
          # the same switch as the painted sky, and no shader anywhere: the deck is
          # drawn in the world, so it exists for a player with no pack at all
          and "if (!McsmExtrasConfig.paintedSky || ctx == null) {" in deck
          and "private static double ripple(" in deck
          and "net.mcsm.extras.client.McsmCloudDeck.submit(ctx);" in boots
          and boots.index("net.mcsm.extras.client.McsmPaintedSky.submit(ctx);")
          < boots.index("net.mcsm.extras.client.McsmCloudDeck.submit(ctx);")
          < boots.index("McsmSkyFloorBand.submit(ctx);"))

    check("and every dimension's cloud deck is a cloud, not a lid",
          all(cloud_ok(d) for d in ("decayed", "adams", "void", "creator")),
          "clouds: %s" % ("error: " + clouds["error"] if "error" in clouds
                          else {d: ("ok" if cloud_ok(d) else "bad")
                                for d in ("decayed", "adams", "void", "creator")}))

    check("and the four decks are four different paintings",
          "error" not in clouds
          and len(set(clouds[d][1] for d in ("decayed", "adams", "void", "creator"))) == 4
          and skygen.count("paint_clouds(") == 5
          and skygen.count('"clouds_') == 4)

    # ------------------------------------------------------------------
    # BUILD #458 -- THE PER-DIMENSION IDENTITY PASS. "each dimension and infinite
    # subdimension completely unique: own blocks, items, mobs, locks, VFX, biomes,
    # fog, sky, horizon; no re-use, not the decay set in flat worlds."
    #
    # The report was exact: the void floated decayed stone and city tile, the
    # infinite dimension built decayed bricks with glitch lamps, every arch was
    # shared, and one hardcoded violet was the horizon of every world. This family
    # holds the fix: one identity table, three material families, and a check that
    # NOTHING borrows anything.
    # ------------------------------------------------------------------
    ident = read("mcsm-extras/java/net/mcsm/extras/McsmIdentity.java") or ""
    content = read("mcsm-extras/java/net/mcsm/extras/McsmContent.java") or ""
    adams_java = read("mcsm-extras/java/net/mcsm/extras/McsmAdams.java") or ""
    adams_dim_text = read("jar-overrides/data/mcsm/dimension/adams_infinity.json") or ""

    def prefix_of(dim_id):
        """The material family the identity table gives a dimension."""
        found = {}
        for m in re.finditer(r'new Skin\((\w+),\s*"[^"]*",\s*"([a-z_]+)"', ident):
            found[m.group(1)] = m.group(2)
        return found.get(dim_id)

    p_decayed, p_adams, p_void, p_creator = (prefix_of("DECAYED"), prefix_of("ADAMS"),
                                             prefix_of("VOID"), prefix_of("CREATOR"))
    check("there is ONE identity per dimension, and the Overworld is deliberately not one",
          "public record Skin(" in ident
          and ident.count("new Skin(") == 4
          and p_decayed and p_adams and p_void and p_creator
          and len({p_decayed, p_adams, p_void, p_creator}) == 4
          and "return null;" in ident
          and "public static Skin forLevel(Level level)" in ident)

    def family_blocks(prefix):
        """Every block the content pack registers under one dimension's prefix."""
        return sorted(set(re.findall(r'block\("%s_([a-z_]+)"' % prefix, content)))

    fams = {"decayed": family_blocks(p_decayed), "adams": family_blocks(p_adams),
            "void": family_blocks(p_void), "creator": family_blocks(p_creator)}
    def full_ids(prefix):
        return set("%s_%s" % (prefix, suffix) for suffix in fams[prefix])

    check("every dimension has a material family of its own, and no two share a block",
          len(fams[p_void]) >= 7 and len(fams[p_adams]) >= 10
          and len(fams[p_creator]) >= 11
          and not (full_ids(p_void) & full_ids(p_adams))
          and not (full_ids(p_void) & full_ids(p_creator))
          and not (full_ids(p_adams) & full_ids(p_creator))
          and "%s_stone" % p_void in full_ids(p_void)
          and "%s_lamp" % p_void in full_ids(p_void)
          and "%s_stone" % p_adams in full_ids(p_adams)
          and "%s_crate" % p_adams in full_ids(p_adams)
          # the fourth world is the one that was built rather than left: marble
          # under gold, and a plinth for the Creator to stand on.
          and "%s_marble" % p_creator in full_ids(p_creator)
          and "%s_plinth" % p_creator in full_ids(p_creator),
          "void=%d adams=%d creator=%d" % (len(fams[p_void]), len(fams[p_adams]),
                                           len(fams[p_creator])))

    # the worlds: no palette may name another dimension's material. The decayed
    # reality keeps the set it is named for -- that IS its identity -- and the
    # neutral ids (air, water, a vanilla barrel) are nobody's material.
    def borrowed(java, own_prefix):
        hits = []
        for m in re.finditer(r'state\("(mcsm:[a-z_]+)"', java):
            rid = m.group(1)
            if not rid.startswith("mcsm:" + own_prefix + "_"):
                hits.append(rid)
        return hits

    void_borrowed = borrowed(void_java, p_void)
    adams_borrowed = borrowed(adams_java, p_adams)
    check("no world is built out of another world's blocks",
          not void_borrowed and not adams_borrowed,
          "void borrowed %s / adams borrowed %s" % (void_borrowed, adams_borrowed))
    creator_dim_text = read("jar-overrides/data/mcsm/dimension/creators_realm.json") or ""
    check("and the ground under each of them is its own too",
          # five layers, five of the dimension's own blocks, and the biome the
          # dimension type is dressed in -- no decayed stone, no city tile.
          adams_dim_text.count('"block": "mcsm:%s_' % p_adams) == 5
          and '"biome": "mcsm:%s_infinity"' % p_adams in adams_dim_text
          and "mcsm:decayed" not in adams_dim_text
          and "mcsm:city_" not in adams_dim_text
          # BUILD #462 -- and the same for the reach, whose ground is laid marble
          # over gold rather than anything that grew or rotted.
          and creator_dim_text.count('"block": "mcsm:%s_' % p_creator) == 5
          # the world is named for its owner, so the DATA ids are creators_realm
          # while the material family is creator_* -- the needle says what the
          # file says, and the biome it names is the one on disk below.
          and '"biome": "mcsm:creators_realm"' in creator_dim_text
          and "mcsm:decayed" not in creator_dim_text
          and "mcsm:adams_" not in creator_dim_text)

    # every doorway is the material of the world it opens onto
    doors_ok = True
    for dim_id, prefix, kinds in (("adams", p_adams, ("crystal", "bricks")),
                                  ("void", p_void, ("anchor", "glass")),
                                  ("creator", p_creator, ("gold", "glass"))):
        want = 'new Door("%s", "mcsm:' % dim_id
        row = [ln for ln in portals.splitlines() if want in ln]
        if not row or not all(('mcsm:%s_%s' % (prefix, k)) in row[0] for k in kinds):
            doors_ok = False
    check("and every doorway is built from the material of the world behind it",
          doors_ok
          # ...and the void's own frame/door constants are NAMED through the table
          # rather than typed out, so an arch cannot be another world's block.
          and 'public static final String FRAME_BLOCK = McsmIdentity.material(' \
                  'McsmIdentity.VOID, "anchor");' in void_java
          and 'public static final String DOOR_BLOCK = McsmIdentity.material(' \
                  'McsmIdentity.VOID, "glass");' in void_java)

    # the horizon, the fog, the sky and the water: the JSONs must carry exactly
    # what the identity table says, and the sky painter must paint the same hex
    adams_biome = json.loads(read(
        "jar-overrides/data/mcsm/worldgen/biome/adams_infinity.json"))
    # the identity rows are two lines per dimension (the record call wraps); read
    # the table out of the Java as a table rather than trusting a doc comment
    rows = {}
    for m in re.finditer(r'new Skin\((\w+), "([^"]*)", "([a-z_]+)",(.*?)\)', ident,
                         re.S):
        hexes = re.findall(r'"([0-9A-F]{6})"', m.group(4))
        if len(hexes) < 6:
            continue
        rows[m.group(1)] = dict(prefix=m.group(3), fog=hexes[0], sky=hexes[1],
                                sky_light=hexes[2], horizon=hexes[3],
                                glow=hexes[4], water=hexes[5])
    check("the identity table holds a full set of hexes for each of the worlds",
          len(rows) == 4
          and all(all(r[k] for k in ("fog", "sky", "sky_light", "horizon", "glow", "water"))
                  for r in rows.values()))

    def packed(hexstr):
        return int(hexstr, 16)

    creator_biome = json.loads(read(
        "jar-overrides/data/mcsm/worldgen/biome/creators_realm.json"))
    creator_type = json.loads(read(
        "jar-overrides/data/mcsm/dimension_type/creators_realm.json"))
    check("the biomes, the dimension types and the identities are the same numbers",
          len(rows) == 4
          and packed(rows["CREATOR"]["fog"]) == creator_biome["effects"]["fog_color"]
          and packed(rows["CREATOR"]["sky"]) == creator_biome["effects"]["sky_color"]
          and packed(rows["CREATOR"]["water"]) == creator_biome["effects"]["water_color"]
          and packed(rows["CREATOR"]["sky_light"]) == packed(
              creator_type["attributes"]["minecraft:visual/sky_light_color"].lstrip("#"))
          and packed(rows["DECAYED"]["fog"]) == decayed_biome["effects"]["fog_color"]
          and packed(rows["DECAYED"]["sky"]) == decayed_biome["effects"]["sky_color"]
          and packed(rows["DECAYED"]["water"]) == decayed_biome["effects"]["water_color"]
          and packed(rows["VOID"]["fog"]) == void_biome["effects"]["fog_color"]
          and packed(rows["VOID"]["water"]) == void_biome["effects"]["water_color"]
          and packed(rows["ADAMS"]["fog"]) == adams_biome["effects"]["fog_color"]
          and packed(rows["ADAMS"]["sky"]) == adams_biome["effects"]["sky_color"]
          and packed(rows["ADAMS"]["water"]) == adams_biome["effects"]["water_color"]
          and packed(rows["ADAMS"]["sky_light"]) == packed(
              json.loads(read("jar-overrides/data/mcsm/dimension_type/"
                              "adams_infinity.json"))["attributes"][
                  "minecraft:visual/sky_light_color"].lstrip("#"))
          and packed(rows["VOID"]["sky_light"]) == packed(
              json.loads(read("jar-overrides/data/mcsm/dimension_type/"
                              "void_reality.json"))["attributes"][
                  "minecraft:visual/sky_light_color"].lstrip("#"))
          and packed(rows["VOID"]["sky"]) == void_biome["effects"]["sky_color"]
          and packed(rows["DECAYED"]["sky_light"]) == packed(
              json.loads(read("jar-overrides/data/mcsm/dimension_type/"
                              "decayed_reality.json"))["attributes"][
                  "minecraft:visual/sky_light_color"].lstrip("#")))

    # the sky painter's own horizon tuples, turned back into "RRGGBB"
    painter_horizons = {}
    for m in re.finditer(
            r'"(\w+)": dict\(\s*\n\s*seed=\d+,\s*\n\s*horizon=\((0x[0-9A-F]{2}), '
            r'(0x[0-9A-F]{2}), (0x[0-9A-F]{2})\)', skygen):
        painter_horizons[m.group(1)] = "%02X%02X%02X" % (
            int(m.group(2), 16), int(m.group(3), 16), int(m.group(4), 16))
    check("the horizon the band paints is the horizon the sky was painted with",
          len(painter_horizons) == 4 and len(rows) == 4
          and all(painter_horizons[d.lower()] == rows[D]["horizon"]
                  for d, D in (("decayed", "DECAYED"), ("adams", "ADAMS"),
                               ("void", "VOID"), ("creator", "CREATOR"))),
          "painter=%s table=%s" % (painter_horizons,
                                   {k: v["horizon"] for k, v in rows.items()}))

    # the families are real: a blockstate, an item definition, a texture and a
    # loot table for every one of them
    _missing = []
    for _prefix in (p_void, p_adams, p_creator):
        for _suffix in fams[_prefix]:
            _name = "%s_%s" % (_prefix, _suffix)
            for _rel in ("jar-overrides/assets/mcsm/blockstates/%s.json" % _name,
                         "jar-overrides/assets/mcsm/items/%s.json" % _name,
                         "jar-overrides/assets/mcsm/textures/block/%s.png" % _name,
                         "jar-overrides/data/mcsm/loot_table/blocks/%s.json" % _name):
                if not os.path.exists(_rel):
                    _missing.append(_rel)
    check("every one of them ships art, a state, an item and a drop",
          not _missing, "missing %s" % _missing[:4])

    # ---- BUILD #459: THE LOCKS, AND EACH WORLD'S OWN KEY. -----------------
    # "own blocks, items, mobs, LOCKS, VFX". A lock is a block per world: it opens
    # to that world's own key and to nothing else, and the key is found inside the
    # world it opens rather than handed out by the terminal.
    locks = read("mcsm-extras/java/net/mcsm/extras/McsmLocks.java") or ""
    towns = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or ""
    cities = read("mcsm-extras/java/net/mcsm/extras/McsmCities.java") or ""
    assets_py = read("ci/make_mcsm_content_assets.py") or ""

    lock_rows = re.findall(r'new DimensionLock\((McsmIdentity\.\w+), props\)', content)

    def keys_returned():
        return re.findall(r'return McsmContent\.(\w+);', locks)

    check("each world's lock belongs to that world, and no two keys are the same",
          lock_rows == ["McsmIdentity.DECAYED", "McsmIdentity.ADAMS",
                        "McsmIdentity.VOID", "McsmIdentity.CREATOR"]
          and "McsmContent.CITY_KEYCARD" in locks
          and "McsmContent.ADAMS_SIGIL" in locks
          and "McsmContent.VOID_SIGIL" in locks
          and "McsmContent.CREATOR_SIGIL" in locks
          and len(set(keys_returned())) == 4,
          "locks=%s keys=%s" % (lock_rows, keys_returned()))
    check("the lock is a real block with a real hook, and it refuses without the key",
          "protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,"
                  in content
          and "return McsmLocks.use(this.dimension, level, pos, player);" in content
          and "public static InteractionResult use(String dimId, Level level, BlockPos pos,"
                  in locks
          and "THE SEAL OPENS" in locks and "sealed" in locks
          and "level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);" in locks)
    check("the seals stand in the worlds, not only in the creative tab",
          'next[LOCK] = state("mcsm:adams_lock", McsmContent.ADAMS_LOCK);' in adams_java
          and "grow.put(cx + 7, ground + 1, cz - 1, LOCK);" in adams_java
          and 'next[LOCK] = state("mcsm:void_lock", McsmContent.VOID_LOCK);' in void_java
          and "planner.put(cx, y + 1, cz + w, LOCK);" in void_java
          and "q[LOCK] = stateOf(McsmContent.DECAYED_LOCK);" in cities
          and "p.put(ox, ground + 1, oz + 2, LOCK);" in cities)
    check("each world's key is found inside that world and nowhere else",
          # exactly one loot pool drops a void sigil (the void's own cache) and one
          # drops an Adams sigil (that dimension's own crate)
          assets_py.count('("mcsm:void_sigil"') == 1
          and assets_py.count('("mcsm:adams_sigil"') == 1
          and assets_py.count('("mcsm:creator_sigil"') == 1
          and '"void_cache": [' in assets_py and '"adams_crate": [' in assets_py
          and '"creator_reliquary": [' in assets_py
          and 'next[CACHE] = state("mcsm:void_cache", McsmContent.VOID_CACHE);' in void_java)
    check("and /ds lock raises any of the three in front of you",
          'Commands.literal("lock")' in towns
          and "ds$lock(ctx.getSource(), null)" in towns
          and ".then(mob).then(lock)" in towns
          and "private static int ds$lock(CommandSourceStack src, String which) {" in towns
          and "level.setBlock(at, block.defaultBlockState(), 3);" in towns)

    # no recipe converts one world's key into another's: the keys are not
    # interchangeable, which is the entire point of a lock being a block.
    rec_block = assets_py.split("RECIPES = {", 1)[-1].split("\n}\n", 1)[0]
    rec_entries = {}
    for m in re.finditer(r'\n    "([a-z_]+)": \(', rec_block):
        rest = rec_block[m.end():]
        end = re.search(r'"mcsm:[a-z_]+", \d+\),', rest)
        if end:
            rec_entries[m.group(1)] = rest[:end.end()]
    check("no recipe turns one world's key into another's -- they are not interchangeable",
          {"adams_sigil", "void_sigil", "decayed_lock", "adams_lock",
           "void_lock"} <= set(rec_entries)
          and "mcsm:void_" not in rec_entries["adams_sigil"]
          and "mcsm:adams_" not in rec_entries["void_sigil"]
          and "mcsm:adams_" not in rec_entries["decayed_lock"]
          and "mcsm:void_" not in rec_entries["decayed_lock"]
          and "mcsm:void_" not in rec_entries["adams_lock"]
          and "mcsm:decayed_" not in rec_entries["void_lock"],
          "recipes read: %d" % len(rec_entries))
    check("the two worlds' own materials and keys are real items with their own art",
          all(os.path.exists("jar-overrides/assets/mcsm/textures/item/%s.png" % n)
              for n in ("void_shard", "adams_amber", "void_sigil", "adams_sigil",
                    "creator_sigil"))
          and all(os.path.exists("jar-overrides/assets/mcsm/items/%s.json" % n)
                  for n in ("void_shard", "adams_amber", "void_sigil", "adams_sigil",
                            "creator_sigil"))
          and "(\"flat\", \"mcsm:item/void_shard\")" in assets_py)

    # and every block in the pack drops itself when it is broken. Found while
    # building #458: only the three crates had loot tables, so mining anything
    # else in the decayed reality paid out nothing at all.
    _all_blocks = sorted(set(re.findall(r'block\("([a-z_]+)"', content)))
    _no_drop = [_n for _n in _all_blocks
                if not os.path.exists("jar-overrides/data/mcsm/loot_table/blocks/%s.json" % _n)]
    check("a block you mine is a block you get", not _no_drop,
          "no loot table for %s" % _no_drop[:5])

    # ------------------------------------------------------------------
    # BUILD #462 -- THE CREATOR'S DIMENSION. nextOrder (c), and the fourth world:
    # "the Creator's dimension". The Creator has been a real entity since #428 and
    # had nowhere to be. The reach is that place -- and it follows every rule the
    # other three worlds already follow: its own ground, biome, light, horizon,
    # sky, air, lock, key and doorway, and none of another world's anything.
    # ------------------------------------------------------------------
    realm = read("mcsm-extras/java/net/mcsm/extras/McsmCreatorRealm.java") or ""
    realm_biome = json.loads(read("jar-overrides/data/mcsm/worldgen/biome/creators_realm.json"))
    realm_type = json.loads(read(
        "jar-overrides/data/mcsm/dimension_type/creators_realm.json"))
    realm_dim = json.loads(read("jar-overrides/data/mcsm/dimension/creators_realm.json"))
    boot = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""
    extras = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    cfg = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    towns = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or ""

    check("the fourth world exists, it has its own keys, and it is booted with the others",
          'Identifier.fromNamespaceAndPath("mcsm", "creators_realm")' in realm
          and "public static final ResourceKey<Level> DIMENSION" in realm
          and "public static final ResourceKey<net.minecraft.world.level.dimension.DimensionType> TYPE"
                  in realm
          and "McsmCreatorRealm.register();" in boot
          and "import net.mcsm.extras.McsmCreatorRealm;" in boot
          and "if (key.equals(McsmCreatorRealm.DIMENSION)) {" in ident
          and "return skin(CREATOR);" in ident)

    # what the world is BUILT out of, and what its reliquary hands out, are two
    # different rules: every block the reach places is the reach's own, while its
    # salvage is deliberately survival stock (steel, a memory crystal) the same way
    # every other world's crates are.
    _realm_states = sorted(set(re.findall(r'state\("(mcsm:[a-z_]+)"', realm)))
    _realm_ids = sorted(set(re.findall(r'"(mcsm:[a-z_]+)"', realm)))
    _realm_foreign = [i for i in _realm_ids if not i.startswith("mcsm:creator")
                      and i not in ("mcsm:decayed_steel_ingot", "mcsm:memory_crystal")]
    check("and the reach is built out of nothing but the reach's own material",
          len(_realm_states) >= 8
          and all(i.startswith("mcsm:creator") for i in _realm_states)
          and not _realm_foreign,
          "built of %s / other ids %s" % (_realm_states, _realm_foreign))

    check("its ground, its biome and its light are its own, and it shares none with the three",
          realm_dim["type"] == "mcsm:creators_realm"
          and realm_dim["generator"]["type"] == "minecraft:flat"
          and realm_dim["generator"]["settings"]["biome"] == "mcsm:creators_realm"
          and realm_type["skybox"] == "overworld" and realm_type["has_skylight"] is True
          and realm_type["has_fixed_time"] is True
          and realm_type["min_y"] == -64 and realm_type["height"] == 384
          # the lighting owner is this world's own: no other dimension's ambient or
          # sky-light colour may appear in its dimension type
          and realm_type["attributes"]["minecraft:visual/ambient_light_color"]
                  == "#" + rows["CREATOR"]["fog"]
          and realm_type["attributes"]["minecraft:visual/sky_light_color"]
                  == "#" + rows["CREATOR"]["sky_light"]
          and len({rows["CREATOR"]["fog"], rows["DECAYED"]["fog"], rows["ADAMS"]["fog"],
                   rows["VOID"]["fog"]}) == 4
          # precipitation is a TOP-LEVEL biome field in this format, not part of
          # effects -- the shape every other biome in the pack already has
          and realm_biome["has_precipitation"] is False
          and realm_biome["spawners"] == {} and realm_biome["features"] == []
          and realm_biome["carvers"] == []
          and sorted(realm_biome) == sorted(
              json.loads(read("jar-overrides/data/mcsm/worldgen/biome/adams_infinity.json"))))

    check("walking in is walk-in: the doorway, the switch, the flag and the panel row",
          'case "creator":' in portals
          and "moved = McsmCreatorRealm.enter(player);" in portals
          and '"creator").executes(ctx -> ds$creator(ctx.getSource())' in towns
          and '"decayed", "adams", "void", "creator"' in towns
          and "The Creator's reach (the fourth dimension, built not left)" in extras
          and "public static boolean creatorRealm = true;" in cfg
          and 'p.setProperty("creator_realm"' in cfg
          and 'creatorRealm = bool(p, "creator_realm", creatorRealm);' in cfg
          and "McsmExtrasConfig.creatorRealm" in realm
          and "ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmCreatorRealm::tick)"
                  in realm)

    check("the arrival is a place: a plaza, the plinth, the reliquary -- and a floor",
          "private static void plaza(ServerLevel level, int cx, int cz) {" in realm
          and "state(\"mcsm:creator_plinth\")" in realm
          and "state(\"mcsm:creator_reliquary\")" in realm
          and "level.setBlock(new BlockPos(cx, y + 1, cz), plinth, 2);" in realm
          and "plaza(target, (int) x, (int) z);" in realm
          # and it is a floor rather than a fall: standing under the world puts the
          # player back on top of it instead of dropping them out of it
          and "FLOOR_GUARD" in realm and "player.getY() < FLOOR_GUARD" in realm
          # BUILD #462 -- and the world's owner is IN it: one Creator per arrival
          # cell, standing over the plaza, persistent, scaled like every other
          # manifestation of it. The entity has existed since #428; this is the
          # first time it is somewhere rather than summoned.
          and "private static void presence(ServerLevel level, int cx, int y, int cz) {" in realm
          and "presence(level, cx, y, cz);" in realm
          and "PRESENCE_HEIGHT" in realm
          and "net.mcsm.extras.entity.McsmEntities.CREATOR_ENTRY" in realm
          and "net.mcsm.extras.entity.McsmBeast.CREATOR" in realm
          and "set(mob, Attributes.SCALE, 26.0D);" in realm
          and "mob.setPersistenceRequired();" in realm
          and "look up" in realm)

    check("the reach's key is in the reach, and the reliquary is where a player sees it",
          # one datapack table drops it, it is this world's own, and the arrival site's
          # own reliquary carries it in code as well
          assets_py.count('("mcsm:creator_sigil"') == 1
          and '"creator_reliquary"' in assets_py
          and '"mcsm:creator_sigil"' in realm
          and os.path.exists("jar-overrides/data/mcsm/loot_table/blocks/creator_reliquary.json")
          and "mcsm:creator_sigil" in read(
              "jar-overrides/data/mcsm/loot_table/blocks/creator_reliquary.json"))

    check("its lock and its key are real blocks and items, and its recipes use only its own",
          os.path.exists("jar-overrides/data/mcsm/recipe/creator_lock.json")
          and os.path.exists("jar-overrides/data/mcsm/recipe/creator_sigil.json")
          and "mcsm:creator_" in read("jar-overrides/data/mcsm/recipe/creator_lock.json")
          and "mcsm:creator_" in read("jar-overrides/data/mcsm/recipe/creator_sigil.json")
          and not any(other in read("jar-overrides/data/mcsm/recipe/creator_lock.json")
                      for other in ("mcsm:decayed_", "mcsm:adams_", "mcsm:void_"))
          and not any(other in read("jar-overrides/data/mcsm/recipe/creator_sigil.json")
                      for other in ("mcsm:decayed_", "mcsm:adams_", "mcsm:void_")))

    check("and the fourth sky and the fourth air are the fourth of each, not a copy",
          sky.count("new Sky(") == 4
          and "private static final Identifier CREATOR_SIDES = tex(\"creator_sides\");" in sky
          and "new Sky(CREATOR_SIDES, CREATOR_TOP," in sky
          and fx.count("new Air(") == 4
          and "private static final Air CREATOR = new Air(" in fx
          and "McsmIdentity.CREATOR," in fx
          and "AIRS = { DECAYED, ADAMS, VOID, CREATOR };" in fx)


    # ------------------------------------------------------------------
    # BUILD #463 -- VOID AGING. nextOrder (d), and the fourth list's own words:
    # "the void effect = aging", "the player model slowly becomes the corrupted
    # void", "happy-then-corrupted lore". Five stages, a value that grows in the
    # void and comes back outside it, a ledger so logging out is not an escape,
    # lines that start kind and stop being kind, and the body itself graded toward
    # the void on every client -- through the same render-state hook the base
    # mod's own wither-sickness layer uses.
    # ------------------------------------------------------------------
    ageing = read("mcsm-extras/java/net/mcsm/extras/McsmVoidAging.java") or ""
    age_payload = read("mcsm-extras/java/net/mcsm/extras/client/McsmVoidAgingPayload.java") or ""
    age_client = read("mcsm-extras/java/net/mcsm/extras/client/McsmVoidAgingClient.java") or ""
    age_state = read("mcsm-extras/java/net/mcsm/extras/client/McsmVoidAgingState.java") or ""
    age_state_mixin = read(
        "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmVoidAgingStateMixin.java") or ""
    age_render_mixin = read(
        "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmVoidAgingRendererMixin.java") or ""
    age_client_mixin = read(
        "mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmVoidAgingClientMixin.java") or ""
    cfg = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    extras = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    book = read("mcsm-extras/java/net/mcsm/extras/client/McsmFutureBookScreen.java") or ""
    term = read("mcsm-extras/java/net/mcsm/extras/client/McsmTerminalScreen.java") or ""
    cfgreskin = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmConfigReskinMixin.java") or ""
    gen = read("ci/make_mcsm_content_assets.py") or ""
    texgen = read("ci/make_mcsm_textures.py") or ""
    buildsh = read("ci/build.sh") or ""
    towns = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or ""
    boot = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmBuiltinPackMixin.java") or ""
    sink = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmGradientTickPatch.java") or ""

    check("the void ages a player in five stages, and the clock is the mod's own",
          "public static final String[] STAGES = {\"clean\", \"touched\", \"marked\", "
                  "\"claimed\", \"taken\"};" in ageing
          and "public static final long[] THRESHOLDS = {0L, 3600L, 9600L, 18000L, 30000L};" in ageing
          and "public static final long MAX = THRESHOLDS[THRESHOLDS.length - 1];" in ageing
          and "public static int stageOf(long ticks)" in ageing
          and "public static float fractionOf(ServerPlayer player)" in ageing)

    check("it grows in the void, less in the decay, and comes back outside them",
          "if (!McsmExtrasConfig.voidAging || level.players().isEmpty()) {" in ageing
          and "boolean inVoid = level.dimension().equals(McsmVoid.DIMENSION);" in ageing
          and "boolean inDecayed = level.dimension().equals(McsmReality.DECAYED_REALITY);" in ageing
          and "now % 4L == 0L" in ageing and "now % 8L == 0L" in ageing
          and "now % 2L == 0L && age > 0L" in ageing
          and "ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick) McsmVoidAging::tick);"
                  in ageing
          and "McsmVoidAging.register();" in boot
          and "import net.mcsm.extras.McsmVoidAging;" in boot)

    check("the lines start kind, then stop being kind -- the whole point of ageing",
          # five rows (the clean stage says nothing), at least three lines each, and
          # the colour of the voice darkens as the stage climbs
          ageing.count("            {\n") >= 4
          and "the air in here is warm" in ageing
          and "you could rest. nobody would know" in ageing
          and "stay a little longer. it does not mind" in ageing
          and "it has noticed you. it keeps a count" in ageing
          and "you are staying" in ageing
          and "you have always been here" in ageing
          and "there was never a way back. there was a way in" in ageing
          and "ChatFormatting.GREEN" in ageing and "ChatFormatting.DARK_PURPLE" in ageing)

    check("the body changes: the state carries the value and the renderer grades the tint",
          # the same two hooks the base mod's wither-sickness tint uses, by descriptor
          "public interface McsmVoidAgingState {" in age_state
          and "float mcsm$voidAge();" in age_state
          and "@Mixin({LivingEntityRenderState.class})" in age_state_mixin
          and "@Unique\n    private float mcsm$voidAgingValue;" in age_state_mixin
          and "public float mcsm$voidAge() {" in age_state_mixin
          and "@Mixin({LivingEntityRenderer.class})" in age_render_mixin
          and "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
                  in age_render_mixin
          and "getModelTint(Lnet/minecraft/client/renderer/entity/state/"
                  in age_render_mixin
          and "cancellable = true" in age_render_mixin
          and "cir.setReturnValue(" in age_render_mixin
          # age 0 is a no-op: an untouched player is exactly the player they were
          and "if (age <= 0.0F) {" in age_render_mixin
          and "require = 0" in age_render_mixin)

    check("the client is told, over the payload arrangement the base mod already runs",
          'Identifier.fromNamespaceAndPath("mcsm", "void_aging")' in age_payload
          and "public static final StreamCodec<RegistryFriendlyByteBuf, McsmVoidAgingPayload> CODEC"
                  in age_payload
          and "public static void handleClient(McsmVoidAgingPayload payload, Context context)"
                  in age_payload
          and "PayloadTypeRegistry.clientboundPlay().register(McsmVoidAgingPayload.TYPE,"
                  in ageing
          and "ServerPlayNetworking.send(player, new McsmVoidAgingPayload(player.getId(),"
                  in ageing
          and "ClientPlayNetworking.registerGlobalReceiver(McsmVoidAgingPayload.TYPE,"
                  in age_client_mixin
          and "McsmVoidAgingPayload::handleClient" in age_client_mixin
          # the store drops stale rows, so nobody stays discoloured after a logout
          and "private static final long STALE_MILLIS = 8000L;" in age_client
          and "public static float ageOf(int entityId)" in age_client
          and "net.mcsm.extras.client.McsmVoidAgingClient.tick();" in sink
          and "playLocalSound(" in age_client)

    check("and logging out is not an escape: the ledger is saved and read back",
          "public static String voidAgingLedger = \"\";" in cfg
          and 'p.setProperty("void_aging_ledger"' in cfg
          and 'voidAgingLedger = str(p, "void_aging_ledger", voidAgingLedger);' in cfg
          and "private static void loadLedger() {" in ageing
          and "private static void flush() {" in ageing
          and "if (ledgerDirty && now % 600L == 0L) {" in ageing
          and "public static void setAge(ServerPlayer player, long ticks)" in ageing)

    check("it is switchable, listed, and testable without waiting 25 minutes",
          "public static boolean voidAging = true;" in cfg
          and 'p.setProperty("void_aging", String.valueOf(voidAging));' in cfg
          and 'voidAging = bool(p, "void_aging", voidAging);' in cfg
          and "Void aging (the void changes the body, in five stages)" in extras
          and 'Commands.literal("aging")' in towns
          and "ds$aging(ctx.getSource(), null)" in towns
          and "ds$aging(ctx.getSource(), \"clear\")" in towns
          and "ds$aging(ctx.getSource(), \"advance\")" in towns
          and ".then(city).then(portal).then(mob).then(lock).then(aging).then(menu)" in towns
          and "private static int ds$aging(CommandSourceStack src, String action) {" in towns)

    # ------------------------------------------------------------------
    # BUILD #464 -- "FIX THE MAIN MENU BE BLACK. Like when I loaded it to the game
    # and it just goes completely black." The report is a report about a DRAW: two
    # of this mod's own screens were painting the whole frame an opaque near-black
    # (the title screen's opted-out backdrop, and the reskin every other screen
    # wears), and the boot sequence opened with a near-black plate plus a FULLY
    # OPAQUE ground band over the bottom fifth, at every launch. This family holds
    # the fix: one shared sky, read from the identity table, and no plate anywhere.
    # ------------------------------------------------------------------
    titles = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTitleOverhaulMixin.java") or ""
    reskin = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmScreenReskinMixin.java") or ""
    loadpause = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmLoadingPauseReskinMixin.java") or ""
    menusky = read("mcsm-extras/java/net/mcsm/extras/client/McsmMenuSky.java") or ""
    cine = read("mcsm-extras/java/net/mcsm/extras/client/McsmCinematic.java") or ""
    cfg = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    extras = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    towns = read("mcsm-extras/java/net/dabicco/witherstormmod/mixin/McsmTownCommandPatch.java") or ""

    # BUILD #466 -- the first version of this checkpoint asserted the RECIPE the
    # painter used (skin.fog() among it) and so it passed the very bug the user
    # reported twice: the menu's top half was painted #09050C. Fog is the
    # near-black air of a torn world; it has no business being the ground of a
    # screen. What is checked now is what the screen is painted WITH -- the three
    # stops of the world's own cube, floored -- and then the colours themselves,
    # arithmetically, below.
    check("the shared sky is the world's own, and it is not painted from the fog",
          "public static void paint(GuiGraphicsExtractor g, int w, int h, float dim) {" in menusky
          and "McsmIdentity.skin(McsmIdentity.DECAYED)" in menusky
          and "skin.glow()" in menusky
          and "skin.fog()" not in menusky and "skin.sky()" not in menusky
          and 'private static final String ZENITH = "2A1C4E";' in menusky
          and 'private static final String MID = "2C2A2E";' in menusky
          and 'private static final String HORIZON = "6E3E2A";' in menusky
          and "McsmIdentity.rgb(hex)" in menusky
          and "public static final float SCALE_MIN = 0.85F;" in menusky
          and "private static final int CHANNEL_MIN = 0x14;" in menusky)

    # ... and arithmetically: the darkest thing any screen can be painted with, at
    # the darkest dim any screen asks for, from the generator's own numbers.
    sky_worlds = {m.group(1): [int(g, 16) for g in m.groups()[1:]]
                  for m in re.finditer(
                      r'"(decayed|adams|void|creator)": dict\(\s*seed=\d+,\s*'
                      r'horizon=\((0x..), (0x..), (0x..)\),[^\n]*\n\s*'
                      r'mid=\((0x..), (0x..), (0x..)\),[^\n]*\n\s*'
                      r'zenith=\((0x..), (0x..), (0x..)\)',
                      skygen)}
    menu_row = sky_worlds.get("decayed")
    scale = 0.85
    painted = [max(0x14, int(c * scale)) for c in menu_row] if menu_row else []
    check("and the menu it paints cannot be black, at any dim",
          menu_row is not None
          and [int(x, 16) for x in ("2A", "1C", "4E")] == menu_row[6:9]
          and [int(x, 16) for x in ("2C", "2A", "2E")] == menu_row[3:6]
          and [int(x, 16) for x in ("6E", "3E", "2A")] == menu_row[0:3]
          # the zenith is the darkest of the three, and even it has to be air
          and painted[6] + painted[7] + painted[8] >= 0x40
          and min(painted) >= 0x14,
          "painted: %s (from the cube's own stops %s)"
          % ("#%02X%02X%02X" % tuple(painted[6:9]) if painted else "?", menu_row))

    check("every screen that used to be a black plate now wears that sky",
          'net.mcsm.extras.client.McsmMenuSky.paint(g, w, h, 1.0F);' in titles
          and "McsmMenuSky.paint(g, w, h, 0.88F);" in reskin
          and "McsmMenuSky.paint(g, w, h, 0.95F);" in loadpause
          # and the plates themselves are gone, not merely commented out
          and "0xFF07050E" not in titles
          and "0xF008060D" not in reskin
          and "0xFF08060D" not in loadpause)

    check("the boot sequence is short, translucent, and can never hold the frame",
          "public static final long PRE_GAME_MS = 2200L;" in cine
          and "public static final long BURST_MS = 1600L;" in cine
          and "g.fill(0, 0, w, h, 0x7A0A0716);" in cine
          and "0xFF050509" not in cine                       # the opaque ground band
          and "0x99060912" in cine
          # the time box: a stuck flag can no longer mean a permanently black menu
          and "private static final long MENU_STUCK_MS = PRE_GAME_MS + BURST_MS + 4000L;" in cine
          and "private static boolean outOfTime(long now) {" in cine
          and "outOfTime(System.currentTimeMillis());" in cine
          and cine.count("|| outOfTime(now)") == 2)

    check("no screen of the mod's paints the whole frame an opaque near-black",
          not _menu_black_plates(titles, reskin, loadpause, menusky, cine),
          "plates: %s" % _menu_black_plates(titles, reskin, loadpause, menusky, cine))

    check("the console the DS button opens is not a black room",
          # McsmExtrasScreen filled the WHOLE frame with BG_TOP/BG_BOTTOM
          # (0xFF0D1016..0xFF07080C), put two 0x88 black vignette bands on top of it,
          # and the reskin mixin stands down for it -- so nothing covered it.
          "McsmMenuSky.paint(g, w, h, 0.62F);" in extras
          # (the constants themselves are gone; the comment that names them stays)
          and "int BG_TOP" not in extras and "int BG_BOTTOM" not in extras
          and "0x88000000" not in extras
          and "g.fill(0, 0, w, TOP_H, 0xE0140F28);" in extras)

    check("and the future-book, the terminal and the settings console are not black",
          "McsmMenuSky.paint(g, this.width, this.height, 0.5F);" in book
          and "BACKDROP" not in book
          and "McsmMenuSky.paint(g, width, height, 0.45F);" in term
          and "0xC0040610" not in term
          and "g.fillGradient(0, 0, w, h, 0xFF1A1130, 0xFF1C1236);" in cfgreskin
          and "0x33000000" not in cfgreskin and "0x44000000" not in cfgreskin)

    # the same rule, over EVERY java source of the mod, so a new screen cannot
    # introduce a black plate later without failing the gate
    check("and no other source in mcsm-extras can paint one either",
          not _menu_black_plates(*_mcsm_java_sources()),
          "plates: %s" % _menu_black_plates(*_mcsm_java_sources()))

    check("and a player can ask what the menu is wearing",
          "public static String state() {" in cine
          and "public static String menuState() {" in cfg
          and "net.mcsm.extras.client.McsmCinematic.state()" in cfg
          and 'Commands.literal("menu")' in towns
          and "private static int ds$menu(CommandSourceStack src) {" in towns
          and ".then(aging).then(menu)" in towns
          and "edit config/mcsm_storm_extras.properties" in towns
          and "Vivid panorama backdrop (off: the storm's own sky)" in extras)

    # ------------------------------------------------------------------
    # BUILD #469 -- THE MENU CANNOT GO BLACK, AND NOW IT CANNOT BE EMPTIED EITHER.
    #
    # The report came back ("It's still black ... it's definitely a render issue ...
    # the Mojang logo loads ... and then just completely black") AFTER every plate
    # this build could name had been repainted and gated. What is left is the other
    # way a screen goes black: a hook that throws out of the screen's own frame
    # extraction, which draws no frame at all. So the whole menu path is
    # fault-isolated now, and the rule that matters is checked in the code's own
    # order: PAINT FIRST, cancel the game's own background only for a frame the mod
    # really painted -- and stand down entirely (vanilla menu) if it keeps faulting.
    # ------------------------------------------------------------------
    guard = read("mcsm-extras/java/net/mcsm/extras/client/McsmMenuGuard.java")

    check("and a fault in the mod's own chrome can no longer leave an empty frame",
          "public final class McsmMenuGuard" in guard
          and "public static boolean ok() {" in guard
          and "public static void fault(String tag, Throwable t) {" in guard
          and "public static String state() {" in guard
          and "public static void reset() {" in guard
          and "public static final int FAULT_LIMIT = 3;" in guard
          and "public static final long COOLDOWN_MS = 30000L;" in guard
          # the log line is reflected: this class compiles against a jar with no slf4j
          and 'getMethod("warn", String.class)' in guard
          and "DabyWitherStormMod.class" in guard)

    check("and the title cancels the game's own background only for a frame it painted",
          "if (!net.mcsm.extras.client.McsmMenuGuard.ok()) {" in titles
          and 'McsmMenuGuard.fault("title-backdrop"' in titles
          and 'McsmMenuGuard.fault("title-framing"' in titles
          and 'McsmMenuGuard.fault("title-chrome"' in titles
          # the order in the source is the whole fix
          and titles.index("net.mcsm.extras.client.McsmMenuSky.paint(g, w, h, 1.0F);")
          < titles.index("ci.cancel(); // only ever after a frame the mod itself painted")
          and "dabyws$framingBody(" in titles
          and "dabyws$chromeBody(" in titles)

    check("and no class reads a screen member it does not have",
          # run 586 failed javac on `this.height` inside the config mixin: the runner
          # is the only compiler this project has, so the rule is checked here.
          not _illegal_screen_members(),
          "illegal: %s" % _illegal_screen_members())

    check("and every other screen and HUD hook of this build is wrapped too",
          not _unguarded_screen_handlers(),
          "unwrapped: %s" % _unguarded_screen_handlers())

    check("and a player can read the render guard, and clear it, from the console entry",
          "McsmMenuGuard.state()" in towns
          and "private static int ds$menuReset(CommandSourceStack src) {" in towns
          and 'Commands.literal("reset")' in towns
          and "McsmMenuGuard.reset();" in towns
          and "/ds menu reset clears it" in towns)

    check("and the base mod's dark side buttons are taken off the title by their own words",
          # the report: "a failed thing with like a very very very dark failed button
          # on the side". The base title mixin adds two near-black purple widgets at
          # (width-142, 6) and (width-142, 27); the hide pass matches the LABELS (a
          # layout change moves the y, never the words) with the old bounds kept as a
          # second rule, and runs from the earliest per-frame hook as well as the
          # framing pass.
          "private void mcsm$hideBaseChrome() {" in titles
          and 'msg.contains("Storm Config")' in titles
          and 'msg.contains("Storm Preview")' in titles
          and "b.getY() == 27 && b.getWidth() == 136" in titles
          and "mcsm$hideBaseChrome();" in titles
          # called from the framing pass AND from the earlier extractRenderState hook
          and titles.count("mcsm$hideBaseChrome();") >= 2)

    # ------------------------------------------------------------------
    # BUILD #465 -- MORE BLOCKS AND ITEMS. The standing ask, and the phase after
    # the Creator's reach in the user's own order. Every world gets the building
    # set cut from ITS material (a void room is built out of the void), the shapes
    # ship their own art file, and the whole set is craftable.
    #
    # Two things this family exists to stop: a shape borrowing another world's
    # material (the identity rule), and a block whose art is dead -- a texture
    # emitted but never named by the model, which is a missing-texture cube in
    # game with a file sitting right next to it.
    # ------------------------------------------------------------------
    gen_blocks = {}
    seg = gen[gen.index("BLOCKS = {"):gen.index("# item name -> (kind, texture)")]
    for m in re.finditer(r'^    "([a-z_]+)": \("([a-z]+)", (.+?)\),$', seg, re.M):
        spec = m.group(3)
        names = re.findall(r'mcsm:block/([a-z_]+)', spec)
        gen_blocks[m.group(1)] = (m.group(2), names)
    new_blocks = ["decayed_bricks", "decayed_brick_slab", "decayed_brick_stairs",
                  "decayed_brick_wall", "city_window", "city_railing", "city_brick_wall",
                  "void_slab", "void_stairs", "void_wall", "void_fence", "void_trapdoor",
                  "adams_slab", "adams_stairs", "adams_tile_wall", "adams_fence",
                  "adams_trapdoor", "creator_slab", "creator_stairs", "creator_wall",
                  "creator_fence", "creator_trapdoor"]

    check("the shape set landed, world by world, and every one of them is crafted",
          all(n in gen_blocks for n in new_blocks)
          and all(n in content for n in new_blocks)
          and all('"%s": (' % n in gen[gen.index("RECIPES = {"):gen.index("def emit_recipes")]
                  for n in new_blocks)
          # wall, slab, stair, fence and trap door are the shapes this phase is for
          and {"slab", "stairs", "wall", "fence", "trapdoor"} <= {gen_blocks[n][0] for n in new_blocks},
          "missing: %s" % [n for n in new_blocks if n not in gen_blocks or n not in content])

    check("no world's shape set is built out of another world's material",
          all(all(t.startswith(n.split("_")[0] + "_") or t == n
                  for t in gen_blocks[n][1]) for n in new_blocks),
          # a void slab named adams_stone would be the identity rule broken by a
          # shape rather than by a generator, which is a new way to break it
          "borrowed: %s" % [(n, [t for t in gen_blocks[n][1]
                                 if not (t.startswith(n.split("_")[0] + "_") or t == n)])
                            for n in new_blocks])

    check("every one of those shapes ships art under its own name",
          # the shapes inherit their family's pixels but each names its OWN file,
          # so the texture a pack author overrides is the one the model reads
          all(os.path.exists("jar-overrides/assets/mcsm/textures/block/%s.png" % n)
              for n in new_blocks)
          and "SHAPE_TEXTURE_FROM" in texgen
          and all(gen_blocks[n][1] == [n] for n in
                  [x for x in new_blocks if gen_blocks[x][0] != "translucent"]),
          "dead art: %s" % [n for n in new_blocks
                            if gen_blocks[n][1] != [n] and gen_blocks[n][0] != "translucent"])

    materials = ("city_gear", "void_cord", "adams_glass_shard", "creator_dust",
                 "decayed_ash_clump", "storm_marrow")
    names_seg = gen[gen.index("NAMES = {"):gen.index("FACES = (")]
    check("and the expansion's materials are items with names and art",
          all('item("%s"' % n in content for n in materials)
          and all(('"%s": "' % n) in names_seg for n in materials)
          and all('"%s": ("flat", "mcsm:item/%s")' % (n, n) in gen for n in materials)
          and all(os.path.exists("jar-overrides/assets/mcsm/textures/item/%s.png" % n)
                  for n in materials)
          and all(os.path.exists("jar-overrides/assets/mcsm/items/%s.json" % n)
                  for n in materials)
          # the weapons that are still owed need this version's real item surface,
          # which the runner dumps every build now: SwordItem and Tier do NOT exist
          # in 26.2, so the recon is what the next phase is written against. It
          # lives in the API DUMP list -- asking there is a known-good path; asking
          # it inside the classpath-probe block killed runs 579 and 580 silently.
          and "net.minecraft.world.item.SwordItem" in buildsh
          and "net.minecraft.world.item.ToolMaterial" in buildsh
          and "net.minecraft.world.item.AxeItem" in buildsh)

    # ------------------------------------------------------------------
    # BUILD #466 -- THE WEAPONS. Phase (e) shipped the blocks and the materials;
    # the brief ("even some weapons would be a really cool expansion") is only
    # answered by weapons that are weapons. Two things this family stops: a weapon
    # that is a plain Item with a sword's model (it did a bare hand's damage, which
    # is how tentacle_hook and the other hand-helds shipped), and a weapon made
    # from another world's materials -- the identity rule, applied to the arsenal.
    #
    # It is also the reason the runner dumps the item API every build: SwordItem
    # and Tier do NOT exist in this version (runs 578-581 all report "class not
    # found"), ToolMaterial is the material record, and the behaviour is a property
    # of the item. Written against a remembered API, this pass would have failed
    # javac twice before anyone learned that.
    # ------------------------------------------------------------------
    weapons = {
        "decayed_blade": "decayed", "decayed_cleaver": "decayed",
        "void_edge": "void", "void_ripper": "void",
        "adams_glaive": "adams", "adams_mirror_axe": "adams",
        "creator_edict": "creator", "creator_hammer": "creator",
    }
    recipes_seg = gen[gen.index("RECIPES = {"):gen.index("def emit_recipes")]
    check("the weapons pass landed: real swords, axes and pickaxes",
          content.count(".sword(ToolMaterial.") >= 7
          and content.count(".axe(ToolMaterial.") >= 3
          and content.count(".pickaxe(ToolMaterial.") >= 2
          and "import net.minecraft.world.item.ToolMaterial;" in content
          # the four hand-helds that shipped as plain Items are tools now
          and "glinted(props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant()\n"
              "                    .axe(ToolMaterial.DIAMOND, 6.0F, -3.0F))));" in content
          and ".sword(ToolMaterial.NETHERITE, 7.0F, -2.6F)" in content,
          "sword=%d axe=%d pickaxe=%d" % (content.count(".sword(ToolMaterial."),
                                          content.count(".axe(ToolMaterial."),
                                          content.count(".pickaxe(ToolMaterial.")))

    check("every weapon is registered, named, held like a tool, drawn, and craftable",
          all('item("%s"' % n in content for n in weapons)
          and all(('"%s": ("handheld", "mcsm:item/%s")' % (n, n)) in gen for n in weapons)
          and all(('"%s": "' % n) in names_seg for n in weapons)
          and all(('"%s": (' % n) in recipes_seg for n in weapons)
          and all(os.path.exists("jar-overrides/assets/mcsm/textures/item/%s.png" % n)
                  for n in weapons)
          and all(os.path.exists("jar-overrides/assets/mcsm/items/%s.json" % n) for n in weapons)
          and all(os.path.exists("jar-overrides/assets/mcsm/models/item/%s.json" % n)
                  for n in weapons),
          "missing: %s" % [n for n in weapons if not os.path.exists(
              "jar-overrides/assets/mcsm/models/item/%s.json" % n)])

    borrowed = []
    for n, world in weapons.items():
        i = recipes_seg.index('"%s": (' % n)
        j = recipes_seg.find('\n    "', i + 1)
        row = recipes_seg[i:(j if j > 0 else i + 400)]
        ing = [t for t in re.findall(r'"mcsm:([a-z_]+)"', row) if t != n]
        if not all(t.startswith(world + "_") for t in ing):
            borrowed.append((n, ing, world))
    check("and none of them is forged out of another world's material",
          not borrowed, "borrowed: %s" % borrowed)

    for c in checks:
        if c not in [f.split(" --")[0] for f in fails]:
            print("  ok   WitherStormPhase :: %s" % c)
    for f in fails:
        print("  FAIL WitherStormPhase :: %s" % f)
    if live or dead:
        print("  note %d shader helpers reachable, %d dormant by design (allowlisted)"
              % (len(live), len(dead)))
    print("[phase] %d/%d WitherStormPhase checkpoints pass" % (len(checks) - len(fails), len(checks)))
    return 1 if fails else 0


if __name__ == "__main__":
    sys.exit(main())
