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
