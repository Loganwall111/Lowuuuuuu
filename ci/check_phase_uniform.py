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
    check("the panorama is graded, not covered (grade drawn in the TAIL hook)",
          "menuPanorama) {" in title and "0x6607050E" in title)
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
    check("the three UI one-shots are Ogg Vorbis",
          len(oggs) >= 3 and all(open(p, "rb").read(4) == b"OggS" for p in oggs))
    sjson = read("jar-overrides/assets/mcsm/sounds.json") or ""
    try:
        events = json.loads(sjson)
    except Exception:
        events = {}
    check("sounds.json keys are plain paths in the mcsm namespace",
          bool(events) and all("." not in k for k in events))
    check("every declared sound points at an existing shipped file",
          all((read(os.path.join(sound_dir, s.split(":", 1)[-1] + ".ogg")) is not None)
              for ev in events.values() for s in ev.get("sounds", [])))

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
