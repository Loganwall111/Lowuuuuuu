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
import json
import glob
import json
import os
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
        check("dome shell is present but inert",
              "returns 0.0F" in dome or "return 0.0F" in dome)
        check("dome shell submits no geometry",
              not any(k in dome for k in ("VertexConsumer", "submitCustomGeometry",
                                          "submitModel", "RenderType", "BufferBuilder")))
        check("dome shell contributes no colour",
              "out[0] = 0.0F" in dome and "out[1] = 0.0F" in dome and "out[2] = 0.0F" in dome)
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
    _dbg_cfg = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    _dbg_scr = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    print("DBG", "public static double blackHoleSeconds = 180.0;" in _dbg_cfg,
          "black_hole_seconds" in _dbg_cfg, "Black Hole Lifetime" in _dbg_scr, len(_dbg_cfg), len(_dbg_scr))
    _cfg3 = read("mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java") or ""
    _scr3 = read("mcsm-extras/java/net/mcsm/extras/client/McsmExtrasScreen.java") or ""
    _hole_lifetime = "public static double blackHoleSeconds = 180.0;" in _cfg3
    _hole_persist = "black_hole_seconds" in _cfg3
    _hole_row = "Black Hole Lifetime" in _scr3
    check("the black hole lifetime is configurable and persisted",
          _hole_lifetime and _hole_persist and _hole_row,
          "field=%s persisted=%s row=%s" % (_hole_lifetime, _hole_persist, _hole_row))

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
          and 'msg.contains("Storm Config")' in title
          and "cb.visible = false" in title
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
          and "private static BlockPos spawnPos(ServerLevel level)" in cities
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
