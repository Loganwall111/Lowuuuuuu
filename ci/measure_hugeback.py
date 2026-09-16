#!/usr/bin/env python3
"""measure_hugeback.py -- BUILD #423: why the phase-5.5 upper back looks detached.

THE REPORT. "I also noticed the big problem phase 5.5 has an upper back
disattached from the main body." The only geometry that exists in the 5.4-5.8
window and nowhere else is the huge back, drawn from HugeAssBackModel by
WitherStormRenderer.submitGrowth5 (HugeAssBackModel is also the mirror copy drawn
under the same transform).

WHAT THIS MEASURES. The model's own bounding box, read out of the model source:
every bone's PartPose offset accumulated down the bone tree -- the same chain
LayerDefinition.create() walks -- and every .addBox(...) corner taken with it.
The number that matters is the CENTRE of that box, because the renderer enlarges
the huge back by scaling 1.72x about the model ORIGIN, and scaling a body whose
centre sits `d` from the pivot moves it by (1.72 - 1) * d.

Measured here: the huge back's centre is about 11.7 blocks BELOW its own origin,
so the enlargement throws it 8.4 model blocks -- and the storm's -4 body scale
carries that to about 34 WORLD blocks, roughly seven of them sideways. That is
the gap the user is looking at, and it is why McsmHugeBackAttachMixin /
McsmHugeBackPoseMixin re-scale the model about its own centre instead of the
origin.

Usage:
    python3 ci/measure_hugeback.py            # print the measurement
    python3 ci/measure_hugeback.py --check    # gate: the geometry still matches
"""
import argparse
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL = os.path.join(ROOT, "src-recon/net/dabicco/witherstormmod/entity/model",
                     "HugeAssBackModel.java")

# The measured centre (model blocks). A model edit that moves the huge back by
# more than a block should be looked at by a human, not silently accepted: the
# pose correction compensates the CURRENT geometry and would then be off.
EXPECT_CENTRE = (0.7, -11.7, 2.4)
CENTRE_TOLERANCE = 1.5
# Below this the correction would be pointless and the report would not be
# explained by geometry -- i.e. the fix would be aimed at the wrong thing.
MIN_WORLD_DISPLACEMENT = 20.0
# The storm draws the growth-5 family with this base scale (submitGrowth5), and
# enlarges the huge back about the origin by this factor.
BODY_SCALE = 4.0
HUGE_SCALE = 1.72

OFFSET = re.compile(r"PartPose\.(?:offset|offsetAndRotation)\(\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?")
BOX = re.compile(r"\.addBox\(\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?\s*,\s*(-?[\d.]+)F?")
FUNC = re.compile(r"private static PartDefinition (make_\w+)\s*\(PartDefinition (\w+)\)\s*\{")
CHAIN = re.compile(r"PartDefinition (\w+) = (make_\w+)\((\w+)\);")


def bodies(src):
    """Every make_* body, brace-matched (never by indentation)."""
    out = {}
    for m in FUNC.finditer(src):
        i = m.end() - 1
        depth = 0
        while i < len(src):
            ch = src[i]
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        out[m.group(1)] = src[m.end(): i]
    return out


def measure(path):
    src = open(path).read()
    funcs = bodies(src)
    layer = re.search(r"public static LayerDefinition createBodyLayer\(\)\s*\{(.*?)\n   \}", src, re.S)
    if not layer or not funcs:
        return None
    # The bone chain, in order, as createBodyLayer builds it: a bone's parent is
    # whichever function produced the variable passed to it.
    var2fn, children = {}, {}
    for var, fn, parent in CHAIN.findall(layer.group(1)):
        children.setdefault(var2fn.get(parent), []).append(fn)
        var2fn[var] = fn

    lo, hi = [1e18] * 3, [-1e18] * 3
    counted = [0]

    def walk(fn, acc):
        off = OFFSET.search(funcs[fn])
        if off:
            acc = tuple(acc[i] + float(off.group(i + 1)) for i in range(3))
        for m in BOX.finditer(funcs[fn]):
            x, y, z, dx, dy, dz = (float(g) for g in m.groups())
            counted[0] += 1
            for i, (a, b) in enumerate(((x, x + dx), (y, y + dy), (z, z + dz))):
                lo[i] = min(lo[i], acc[i] + a)
                hi[i] = max(hi[i], acc[i] + b)
        for child in children.get(fn, []):
            walk(child, acc)

    for root in children.get(None, []):
        walk(root, (0.0, 0.0, 0.0))
    if counted[0] == 0:
        return None
    return lo, hi, counted[0], len(funcs)


def main(argv):
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args(argv)

    if not os.path.isfile(MODEL):
        print("[hugeback] model source not found at %s -- skipped (never a silent pass)"
              % os.path.relpath(MODEL, ROOT))
        return 0

    measured = measure(MODEL)
    if measured is None:
        print("[hugeback] %s does not parse as a bone chain any more -- the measurement is out of "
              "step with the model" % os.path.relpath(MODEL, ROOT))
        return 1 if args.check else 0

    lo, hi, boxes, bones = measured
    blocks = lambda v: v / 16.0
    centre = [(lo[i] + hi[i]) / 32.0 for i in range(3)]
    # How far the 1.72x enlargement about the origin moves the model, in model
    # blocks, and then through the storm's -4 body scale into world blocks.
    model_move = [(HUGE_SCALE - 1.0) * c for c in centre]
    world_move = [model_move[0] * -BODY_SCALE, model_move[1] * -BODY_SCALE, model_move[2] * BODY_SCALE]
    biggest = max(abs(v) for v in world_move)

    print("[hugeback] %s: %d bones, %d boxes" % (os.path.basename(MODEL), bones, boxes))
    print("[hugeback]   bounds (model blocks): min=%s max=%s"
          % ([round(blocks(v), 1) for v in lo], [round(blocks(v), 1) for v in hi]))
    print("[hugeback]   centre (model blocks): %s" % [round(c, 1) for c in centre])
    print("[hugeback]   1.72x about the ORIGIN moves it: model=%s world=%s blocks"
          % ([round(v, 1) for v in model_move], [round(v, 1) for v in world_move]))

    problems = []
    for i, axis in enumerate("xyz"):
        if abs(centre[i] - EXPECT_CENTRE[i]) > CENTRE_TOLERANCE:
            problems.append("centre %s is %.1f, expected %.1f (+-%.1f): the huge back's geometry "
                            "moved, so the pose correction in McsmHugeBackPoseMixin needs "
                            "re-deriving" % (axis, centre[i], EXPECT_CENTRE[i], CENTRE_TOLERANCE))
    if biggest < MIN_WORLD_DISPLACEMENT:
        problems.append("largest displacement is only %.1f world blocks; the detached-upper-back "
                        "report would no longer be explained by the pivot" % biggest)

    if problems:
        for p in problems:
            print("[hugeback] FAIL %s" % p)
        return 1
    print("[hugeback] OK -- the phase-5.5 upper back is flung %.0f world blocks off its body when "
          "the 1.72x enlargement is taken about the model origin; #423 scales it about its own "
          "centre instead" % biggest)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
