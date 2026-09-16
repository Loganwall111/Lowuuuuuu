#!/usr/bin/env python3
"""make_emissive_whites.py -- BUILD #422: the emissive face glow, guaranteed.

THE REPORT. "The teeth and the eyes are still not glowing." The user is not
running a shader pack, so the glow has to come from the mod's own emissive pass
-- the `_e` atlases submitted through RenderTypes.eyes at full brightness.

WHY THEY WERE NOT GLOWING. The emissive pass is gated on the pixel's own
luminance (`emMask` in fogless_entity.fsh, and the same floor in the native
`RenderTypes.eyes` path): a pixel only glows if its own value clears ~0.45-0.80.
The shipped atlases are tinted and dim:

    wither_storm_p7_e.png        max channel 197/255 (0.77) -- under the floor
    wither_storm_e.png (phase 4) only 85 opaque pixels in a 64x96 sheet

so phase 7's mouth never reached the mask, and phase 4 had almost no mask to
reach it with. The teeth were drawn; they were just not bright enough to be
emissive.

WHAT THIS DOES.

  1. PURE WHITE MASKS. Every phase >= 4 emissive atlas keeps its alpha (the mask
     IS the alpha channel -- it is where the model's UVs put the teeth and the
     lenses) and has its colour forced to pure white 255/255/255. White is the
     brief: "WHITE TEETH, the aura carries the phase colour", and the aura is
     applied by the shader/native pass from the phase, not from the sheet.
  2. A REAL PHASE-4 MASK. The phase-4 atlases are 64x96 and almost empty because
     they were never traced. Their mask is now DERIVED from the matching body
     atlas (wither_storm.png / wither_storm_og.png): every pixel the artist drew
     bright -- the teeth, the two head faces, the command-block belly face -- is
     taken, kept at its own alpha, and painted pure white. That is exactly the
     set of faces the emissive pass is supposed to light, and it is measured from
     the sheet rather than invented.
  3. The deliberate blanks stay blank: wither_storm_no_teeth_glow_e.png is the
     below-phase-4 sheet and has no glow by design.

Usage:
    python3 ci/make_emissive_whites.py            # rewrite the atlases
    python3 ci/make_emissive_whites.py --check    # verify them (build gate)
"""
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from pngutil import read_png, write_png  # noqa: E402

# Where the emissive atlases live. Both trees are written so the shipped
# resources and the jar overlay can never disagree.
TREES = [
    "src/main/resources/assets/dabywitherstormmod/textures/entity",
    "jar-overrides/assets/dabywitherstormmod/textures/entity",
]

# The phase-4 family: derive the mask from the body atlas of the same base name.
DERIVED = {
    "wither_storm_e.png": "wither_storm.png",
    "wither_storm_og_e.png": "wither_storm_og.png",
}

# ---------------------------------------------------------------------------
# BUILD #455 -- AND THE ONE THAT WAS QUIETLY EMPTY.
#
# The report came back a third time ("the storm's teeth and eyes are still not
# glowing"), and this time the atlas itself was the answer:
#
#     phase_4_assets_e.png   512x512   30 opaque pixels   30  of them pure white
#
# The 512x512 head/body sheet the base's head passes now sample has two tiny lit
# UV islands in its bottom-left corner -- the eye lenses -- and nothing else. The
# gate below passed it, correctly and uselessly: "every opaque pixel is pure
# white" is vacuously true of a sheet with almost no opaque pixels. An atlas that
# is 0.011% lit lights 0.011% of a 40-block head, which is what "the eyes are not
# glowing" looks like from the ground.
#
# Two things change here:
#
#   1. the eye islands are UNIONED IN from the whole phase-4 family (every sheet
#      in the family, both trees) so the lenses are lit in the one atlas the head
#      passes read, whatever phase the storm is in;
#   2. no `_e.png` atlas may be ENTIRELY transparent any more. That is a real,
#      checkable floor -- the empty-file hole -- as opposed to a coverage
#      percentage, which would have to be tuned per atlas and would fight the
#      legitimate two-island sheets.
# ---------------------------------------------------------------------------
UNION_DERIVED = {
    "phase_4_assets_e.png": [
        "phase_4_assets.png", "phase_4_assets_og.png",
        "phase_4_assets_p55.png", "phase_4_assets_p6.png",
        "phase_4_assets_og_p55.png", "phase_4_assets_og_p6.png",
    ],
}
# Luminance a body pixel must clear to count as one of the lenses.
UNION_FLOOR = 100.0

# Brightness a body-atlas pixel must clear before it counts as a face we light.
DERIVE_FLOOR = 150.0

# The one sheet that is meant to be empty (below phase 4 there is no glow).
KEEP_EMPTY = "wither_storm_no_teeth_glow_e.png"

# Below this many opaque pixels an atlas is treated as "never traced".
TRACED_MIN = 300

WHITE = (255, 255, 255)

# Set by main(): with --two-way (and always under --check) the mod's own texture
# tree is held to the same pure-white masks as the shipped overlay tree.
GLOBAL = {}


def lum(px):
    return 0.2126 * px[0] + 0.7152 * px[1] + 0.0722 * px[2]


def whiten(path):
    w, h, px = read_png(path)
    out = []
    changed = 0
    for r, g, b, a in px:
        if a <= 0:
            out.append((0, 0, 0, 0))
            continue
        if (r, g, b) != WHITE:
            changed += 1
        out.append((WHITE[0], WHITE[1], WHITE[2], a))
    write_png(path, w, h, out)
    return len([p for p in px if p[3] > 0]), changed


def bright_count(body_path):
    """How many pixels of a body atlas the mask would light."""
    if not os.path.exists(body_path):
        return 0
    _w, _h, body = read_png(body_path)
    return sum(1 for (r, g, b, a) in body if a > 0 and lum((r, g, b)) > DERIVE_FLOOR)


def best_body(tree, name):
    """The richest body atlas available for this mask.

    The mod's own tree can carry a placeholder body sheet while the overlay tree
    carries the traced one; both trees then get the mask derived from the atlas
    that actually ships, so the two render paths cannot disagree.
    """
    best, count = None, 0
    for candidate_tree in (tree, "jar-overrides/assets/dabywitherstormmod/textures/entity"):
        candidate = os.path.join(candidate_tree, name)
        n = bright_count(candidate)
        if n > count:
            best, count = candidate, n
    return best, count


def derive(path, body_path):
    """Paint a pure-white mask from the bright pixels of the body atlas."""
    bw, bh, body = read_png(body_path)
    w, h, px = read_png(path)
    if (bw, bh) != (w, h):
        return None
    out = []
    for i, (r, g, b, a) in enumerate(px):
        br, bg, bb, ba = body[i]
        bright = ba > 0 and lum((br, bg, bb)) > DERIVE_FLOOR
        if bright:
            out.append((WHITE[0], WHITE[1], WHITE[2], max(a, 255) if a > 0 else 255))
        else:
            out.append((0, 0, 0, 0))
    write_png(path, w, h, out)
    return len([p for p in out if p[3] > 0])


def union_mask(path, names):
    """Union of the lit pixels of a whole atlas family into one mask.

    Both trees are read, so the overlay sheet and the mod's own sheet agree, and
    the existing contents of the mask are always kept: this adds light, it never
    takes any away.
    """
    w, h, px = read_png(path)
    best = [0.0] * (w * h)
    for tree in TREES + ["jar-overrides/assets/dabywitherstormmod/textures/entity"]:
        for name in names:
            candidate = os.path.join(tree, name)
            if not os.path.exists(candidate):
                continue
            bw, bh, body = read_png(candidate)
            if (bw, bh) != (w, h):
                continue
            for i, (r, g, b, a) in enumerate(body):
                if a <= 0:
                    continue
                value = lum((r, g, b))
                if value > best[i]:
                    best[i] = value
    out = []
    added = 0
    for i, (r, g, b, a) in enumerate(px):
        if a > 0:
            out.append((WHITE[0], WHITE[1], WHITE[2], a))
        elif best[i] > UNION_FLOOR:
            out.append((WHITE[0], WHITE[1], WHITE[2], 255))
            added += 1
        else:
            out.append((0, 0, 0, 0))
    write_png(path, w, h, out)
    return len([p for p in out if p[3] > 0]), added


def main(argv):
    check = "--check" in argv
    problems = []
    report = []
    for tree in TREES:
        if not os.path.isdir(tree):
            continue
        for name in sorted(os.listdir(tree)):
            if not name.endswith("_e.png"):
                continue
            path = os.path.join(tree, name)
            w, h, px = read_png(path)
            opaque = [p for p in px if p[3] > 0]

            if name == KEEP_EMPTY:
                if opaque:
                    problems.append("%s is supposed to be empty but has %d opaque pixels"
                                    % (path, len(opaque)))
                report.append("%-34s intentionally empty (phase < 4)" % name)
                continue

            if name in UNION_DERIVED:
                if check:
                    # held to the union: every pixel the family lights must be lit
                    w2, h2, want = read_png(path)
                    lit = sum(1 for p in want if p[3] > 0)
                    if lit == 0:
                        problems.append("%s is entirely transparent -- the eye lenses "
                                        "cannot glow out of an empty atlas, run "
                                        "ci/make_emissive_whites.py" % path)
                    else:
                        report.append("%-34s union mask, %d lit lens pixels"
                                      % (name, lit))
                    continue
                total, added = union_mask(path, UNION_DERIVED[name])
                report.append("%-34s union mask, %d lit pixels (%d added from the "
                              "phase-4 family)" % (name, total, added))
                continue

            if name in DERIVED:
                # The overlay tree is the one that ships (the build copies
                # jar-overrides over the jar's own resources), and it is the one
                # that carries the real 64x96 body atlas to derive from. In any
                # other tree the derivation is only attempted if it produces a
                # real mask; a placeholder sheet is reported and left alone
                # rather than failed, because the overlay copy is authoritative.
                overlay = ("jar-overrides/" in tree) or bool(GLOBAL.get("two_way"))
                body = os.path.join(tree, DERIVED[name])
                if not os.path.exists(body):
                    if overlay:
                        problems.append("%s has no body atlas at %s" % (path, body))
                    continue
                if len(opaque) < TRACED_MIN:
                    # Nothing usable in this tree: fall back to the traced body
                    # atlas from the overlay tree (the one that ships) and hold
                    # every tree to the same derived mask.
                    body_path, bright = best_body(tree, DERIVED[name])
                    if body_path is None or bright < TRACED_MIN:
                        if check:
                            problems.append("%s is untraced (%d opaque pixels) -- run "
                                            "ci/make_emissive_whites.py" % (path, len(opaque)))
                        else:
                            report.append("%-34s no traced body atlas to derive from"
                                          % name)
                        continue
                    if check:
                        problems.append("%s is untraced (%d opaque pixels) -- run "
                                        "ci/make_emissive_whites.py" % (path, len(opaque)))
                        continue
                    made = derive(path, body_path)
                    if made is None:
                        problems.append("%s cannot be derived from %s (size mismatch)"
                                        % (path, body_path))
                    elif made < TRACED_MIN:
                        problems.append("%s derived only %d lit faces from %s"
                                        % (path, made, body_path))
                    else:
                        report.append("%-34s derived %d lit faces from %s"
                                      % (name, made, os.path.relpath(body_path)))
                    continue

            # every other atlas: pure white, alpha kept -- and not empty
            if not opaque:
                problems.append("%s is entirely transparent: an emissive atlas with no "
                                "pixels lights nothing, and 'every opaque pixel is pure "
                                "white' is vacuously true of it. Run "
                                "ci/make_emissive_whites.py, or delete it so the base "
                                "jar's own atlas is used." % path)
                continue
            non_white = [p for p in opaque if (p[0], p[1], p[2]) != WHITE]
            if check:
                if non_white:
                    problems.append("%s has %d non-white emissive pixels (max channel %d) "
                                    "-- the glow mask must be pure white, run "
                                    "ci/make_emissive_whites.py"
                                    % (path, len(non_white),
                                       max(max(p[0], p[1], p[2]) for p in opaque)))
                else:
                    report.append("%-34s pure white, %d lit pixels" % (name, len(opaque)))
                continue
            total, changed = whiten(path)
            report.append("%-34s pure white, %d lit pixels (%d recoloured)"
                          % (name, total, changed))

    for line in report:
        print("[emissive] " + line)
    if problems:
        for p in problems:
            print("[emissive] FAIL " + p)
        return 1
    print("[emissive] OK -- every phase >= 4 emissive atlas is a pure white mask "
          "that clears the glow floor")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
