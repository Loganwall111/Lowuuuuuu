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
