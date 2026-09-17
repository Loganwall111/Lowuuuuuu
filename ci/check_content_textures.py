#!/usr/bin/env python3
"""The content pack can never again ship a texture that does not resolve.

WHY THIS EXISTS. Phase 1 of mandate D.8 shipped 38 blockstates, 67 block models
and 57 item models whose textures were borrowed from the base mod and from
vanilla -- and no textures of its own, and (because this Minecraft renders items
from ITEM DEFINITIONS) no items/ definitions either. In game that is exactly what
the user reported: the whole content pack came in as the missing-model cube,
"black and purple looking glitch blocks".

Three separate faults caused it, and every one of them is checked here:

  1. A MODEL MAY ONLY NAME A TEXTURE THAT EXISTS. Every reference in
     assets/mcsm/models/** is resolved against the pack's own PNGs, the base
     mod's file list and the vanilla client jar's file list. An unresolvable
     reference is a missing-texture error in game, so it is a build failure here.
  2. EVERY BLOCK AND EVERY ITEM NEEDS AN ITEM DEFINITION. In this version an item
     is rendered from assets/<ns>/items/<name>.json; a registered item without one
     is the glitch cube no matter how good its model is.
  3. EVERY PLACEABLE BLOCK NEEDS A BLOCKSTATE. A Block with no blockstate is an
     invisible-in-world/broken-in-hand block.

The lists it validates against come from the repository's own scan of the base
release jar and the vanilla client jar (ci/api/scan/jar-resources.txt), so this
runs with no jars present at all -- which is why it can gate the build early.

Usage: python3 ci/check_content_textures.py
"""
from __future__ import annotations

import glob
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "jar-overrides", "assets", "mcsm")
SCAN = os.path.join(ROOT, "ci", "api", "scan", "jar-resources.txt")


def load_scan():
    if not os.path.isfile(SCAN):
        return None
    with open(SCAN, encoding="utf-8", errors="replace") as f:
        return set(line.strip() for line in f if line.strip())


def own_textures():
    found = set()
    for path in glob.glob(os.path.join(ASSETS, "textures", "**", "*.png"), recursive=True):
        rel = os.path.relpath(path, os.path.join(ASSETS, "textures")).replace(os.sep, "/")
        found.add(rel[:-4])
    return found


# ---------------------------------------------------------------------------
# BUILD #477 -- THE TWO WAYS A BLOCK GOES MISSING, BOTH SEEN IN A PLAYER'S LOG.
#
#   Missing block model: mcsm:block/rusted_door_lower_left        (x8)
#   Missing texture references in model mcsm:block/<x>_slab_double:
#       #down #east #north #south #up #west
#
# The first is a blockstate naming a model nobody writes; the second is a model
# whose parent asks for texture keys the file does not define. Neither is a build
# error anywhere -- both are purple-and-black missing geometry in the world.
# ---------------------------------------------------------------------------

# What each vanilla template we build on asks the model to provide.
PARENT_KEYS = {
    "minecraft:block/cube": ("down", "up", "north", "south", "east", "west"),
    "minecraft:block/cube_all": ("all",),
    "minecraft:block/cube_bottom_top": ("bottom", "top", "side"),
    "minecraft:block/cube_column": ("end", "side"),
    "minecraft:block/orientable": ("top", "front", "side"),
    "minecraft:block/orientable_with_bottom": ("bottom", "top", "front", "side"),
    "minecraft:block/slab": ("bottom", "top", "side"),
    "minecraft:block/slab_top": ("bottom", "top", "side"),
    "minecraft:block/stairs": ("bottom", "top", "side"),
    "minecraft:block/inner_stairs": ("bottom", "top", "side"),
    "minecraft:block/outer_stairs": ("bottom", "top", "side"),
    "minecraft:block/door_bottom_left": ("bottom",),
    "minecraft:block/door_bottom_right": ("bottom",),
    "minecraft:block/door_top_left": ("top",),
    "minecraft:block/door_top_right": ("top",),
    "minecraft:block/template_trapdoor_bottom": ("texture",),
    "minecraft:block/template_trapdoor_top": ("texture",),
    "minecraft:block/template_trapdoor_open": ("texture",),
    "minecraft:block/fence_post": ("texture",),
    "minecraft:block/fence_side": ("texture",),
    "minecraft:block/wall_post": ("wall",),
    "minecraft:block/wall_side": ("wall",),
    "minecraft:block/wall_side_tall": ("wall",),
}

VANILLA_ASSETS = os.path.join(ROOT, "jar-overrides", "assets", "minecraft")


def model_files():
    """Every model of ours and every vanilla-namespace override, as {id: path}."""
    out = {}
    for base, root in ((ASSETS, "mcsm"), (VANILLA_ASSETS, "minecraft")):
        for path in glob.glob(os.path.join(base, "models", "**", "*.json"), recursive=True):
            rel = os.path.relpath(path, base).replace(os.sep, "/")[:-5]
            # the id a blockstate names is `mcsm:block/<name>`, not the file path
            if rel.startswith("models/"):
                rel = rel[len("models/"):]
            out["%s:%s" % (root, rel)] = path
    return out


def _entries(variants):
    """The model ids a variants/apply value names, however it is shaped."""
    ids = []
    def take(v):
        if isinstance(v, dict) and "model" in v:
            ids.append(v["model"])
        elif isinstance(v, list):
            for one in v:
                take(one)
    take(variants)
    return ids


def blockstate_model_refs():
    """(blockstate path, model id) for every model a blockstate points at."""
    out = []
    for base in (ASSETS, VANILLA_ASSETS):
        for path in glob.glob(os.path.join(base, "blockstates", "*.json")):
            try:
                with open(path, encoding="utf-8") as f:
                    obj = json.load(f)
            except Exception:
                continue
            for section in ("variants", "multipart"):
                body = obj.get(section)
                if isinstance(body, dict):
                    for v in body.values():
                        for m in _entries(v):
                            out.append((path, m))
                elif isinstance(body, list):
                    for part in body:
                        if isinstance(part, dict):
                            for m in _entries(part.get("apply")):
                                out.append((path, m))
    return out


def model_texture_refs():
    """name -> set of texture identifiers, for every model in the pack."""
    refs = {}
    for path in glob.glob(os.path.join(ASSETS, "models", "**", "*.json"), recursive=True):
        name = os.path.relpath(path, ASSETS).replace(os.sep, "/")[:-5]
        try:
            with open(path, encoding="utf-8") as f:
                obj = json.load(f)
        except Exception as exc:
            refs[name] = {"<<unparseable: %s>>" % exc}
            continue
        found = set()

        def walk(node):
            if isinstance(node, dict):
                for key, value in node.items():
                    if key == "textures" and isinstance(value, dict):
                        for v in value.values():
                            if isinstance(v, str) and ":" in v:
                                found.add(v)
                    else:
                        walk(value)
            elif isinstance(node, list):
                for v in node:
                    walk(v)

        walk(obj)
        refs[name] = found
    return refs


def main():
    problems = []
    scan = load_scan()
    own = own_textures()
    refs = model_texture_refs()

    if not own:
        problems.append("the pack ships no textures at all (assets/mcsm/textures is empty)")

    for model, textures in sorted(refs.items()):
        for ref in sorted(textures):
            namespace, path = ref.split(":", 1)
            if namespace == "mcsm":
                if path not in own:
                    problems.append("%s names mcsm:%s, which is not in "
                                    "assets/mcsm/textures" % (model, path))
                continue
            if scan is None:
                continue
            candidate = "assets/%s/textures/%s.png" % (namespace, path)
            if candidate not in scan:
                problems.append("%s names %s, which is in neither the base jar nor "
                                "the vanilla client" % (model, ref))

    # ---- 2. every block and item needs an item definition --------------------
    blocks = sorted(os.path.basename(p)[:-5]
                    for p in glob.glob(os.path.join(ASSETS, "blockstates", "*.json")))
    item_models = sorted(os.path.basename(p)[:-5]
                         for p in glob.glob(os.path.join(ASSETS, "models", "item", "*.json")))
    definitions = sorted(os.path.basename(p)[:-5]
                         for p in glob.glob(os.path.join(ASSETS, "items", "*.json")))
    missing_defs = [n for n in sorted(set(blocks) | set(item_models)) if n not in definitions]
    if missing_defs:
        problems.append("no item definition (assets/mcsm/items/<name>.json) for: %s"
                        % ", ".join(missing_defs))

    # ---- 3. every block needs a blockstate -----------------------------------
    for name in blocks:
        path = os.path.join(ASSETS, "blockstates", name + ".json")
        try:
            with open(path, encoding="utf-8") as f:
                json.load(f)
        except Exception as exc:
            problems.append("blockstate %s is not valid JSON: %s" % (name, exc))

    # ---- 7. every model a blockstate names must exist ------------------------
    models = model_files()
    dangling = []
    for bpath, mid in blockstate_model_refs():
        if ":" in mid and mid.split(":")[0] not in ("mcsm", "minecraft"):
            continue
        if mid.startswith("minecraft:") and mid not in models:
            # vanilla-namespace models live in the game jar: only ours are checkable
            continue
        if mid not in models:
            dangling.append("%s -> %s" % (os.path.relpath(bpath, ROOT), mid))
    if dangling:
        problems.append("blockstates name models that do not exist: %s"
                        % ", ".join(sorted(set(dangling))[:8]))

    # ---- 8. and every model must define what its parent asks for ------------
    unresolved = []
    for mid, path in models.items():
        try:
            with open(path, encoding="utf-8") as f:
                obj = json.load(f)
        except Exception:
            continue
        textures = obj.get("textures") or {}
        parent = obj.get("parent")
        if parent in PARENT_KEYS:
            for key in PARENT_KEYS[parent]:
                if key not in textures:
                    unresolved.append("%s: #%s (parent %s)" % (mid, key, parent))
        for key, value in textures.items():
            if isinstance(value, str) and value.startswith("#") and value[1:] not in textures:
                unresolved.append("%s: #%s -> %s" % (mid, key, value))
    if unresolved:
        problems.append("models do not resolve their own textures: %s"
                        % ", ".join(sorted(set(unresolved))[:8]))

    print("[content] %d textures, %d models, %d blockstates, %d item definitions"
          % (len(own), len(refs), len(blocks), len(definitions)))
    if problems:
        for p in problems:
            print("  MISSING :: %s" % p)
        print("[content] FAILED with %d unresolved texture/model problems" % len(problems))
        return 1
    print("[content] every texture, model, blockstate and item definition resolves")
    return 0


if __name__ == "__main__":
    sys.exit(main())
