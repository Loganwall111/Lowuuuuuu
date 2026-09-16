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
JAVA_RESOLVER = "mcsm-extras/java/net/mcsm/extras/client/McsmStormPhase.java"
JAVA_SKY_HOOK = "mcsm-extras/java/net/mcsm/extras/client/McsmNativeSkyRenderer.java"


def read(rel):
    path = os.path.join(ROOT, rel)
    if not os.path.isfile(path):
        return None
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


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

    for c in checks:
        if c not in [f.split(" --")[0] for f in fails]:
            print("  ok   WitherStormPhase :: %s" % c)
    for f in fails:
        print("  FAIL WitherStormPhase :: %s" % f)
    print("[phase] %d/%d WitherStormPhase checkpoints pass" % (len(checks) - len(fails), len(checks)))
    return 1 if fails else 0


if __name__ == "__main__":
    sys.exit(main())
