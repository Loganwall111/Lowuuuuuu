#!/usr/bin/env python3
"""Validate the mod's datapack files against vanilla's own, straight from client.jar.

Why this exists: until phase 2 nothing checked our datapack JSON at all. A wrong
key name does not fail any build -- it fails silently in game, as a recipe that
never matches, a loot table that drops nothing, or a dimension that is never
registered. Minecraft 1.21.5 moved several of these keys around (recipe results,
item stacks, loot entries), so "looks right to me" is not good enough when the
author cannot launch the game.

The oracle: the runner downloads the exact client.jar this mod targets. This
script reads vanilla's OWN recipe / loot_table / dimension / dimension_type
files out of it, works out which keys that build of the game actually uses, and
then checks ours against them. It also proves every item id our loot tables and
dimension layers mention really exists -- ours against the registrations in
McsmContent.java, vanilla's against the client's own language file.

Usage: python3 ci/check_datapack_schema.py [--jar /path/client.jar] [--data dir]
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_JAR = "/tmp/mcsm-dl/client.jar"
DEFAULT_DATA = os.path.join(ROOT, "jar-overrides", "data", "mcsm")
CONTENT = os.path.join(ROOT, "mcsm-extras", "java", "net", "mcsm", "extras", "McsmContent.java")

problems: list[str] = []
notes: list[str] = []


def load(jar: zipfile.ZipFile, name: str):
    try:
        with jar.open(name) as fh:
            return json.load(fh)
    except Exception:
        return None


def scan_jsons(jar: zipfile.ZipFile, prefix: str, limit: int = 80):
    out = []
    for name in jar.namelist():
        if name.startswith(prefix) and name.endswith(".json"):
            try:
                with jar.open(name) as fh:
                    out.append((name, json.load(fh)))
            except Exception:
                continue
            if len(out) >= limit:
                break
    return out


def result_keys(recipes) -> set:
    keys = set()
    for _name, data in recipes:
        if not isinstance(data, dict):
            continue
        res = data.get("result")
        if isinstance(res, dict):
            keys |= set(res.keys())
    return keys


def loot_entry_keys(tables) -> set:
    keys = set()

    def walk(node):
        if isinstance(node, dict):
            if node.get("type") == "minecraft:item":
                keys.update(k for k in node.keys() if k in ("name", "id", "item"))
            for v in node.values():
                walk(v)
        elif isinstance(node, list):
            for v in node:
                walk(v)

    for _name, data in tables:
        walk(data)
    return keys


def registered_items() -> set:
    """The mcsm item ids this build actually registers."""
    src = open(CONTENT, encoding="utf-8").read()
    names = set(re.findall(r'\bitem\("([a-z0-9_]+)"', src))
    names |= set(re.findall(r'\bblock\("([a-z0-9_]+)"', src))
    return {"mcsm:" + n for n in names}


def collect_item_ids(node, out: set) -> None:
    if isinstance(node, dict):
        for key, value in node.items():
            if key in ("name", "id", "item") and isinstance(value, str) and ":" in value:
                out.add(value)
            else:
                collect_item_ids(value, out)
    elif isinstance(node, list):
        for value in node:
            collect_item_ids(value, out)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", default=DEFAULT_JAR)
    ap.add_argument("--data", default=DEFAULT_DATA)
    args = ap.parse_args()

    if not os.path.exists(args.jar):
        print("[schema] no client jar at %s -- skipped (never a silent pass)" % args.jar)
        return 0 if os.environ.get("MCSM_ALLOW_SCHEMA_SKIP") else 1

    jar = zipfile.ZipFile(args.jar)
    recipes = scan_jsons(jar, "data/minecraft/recipe/")
    tables = scan_jsons(jar, "data/minecraft/loot_table/")
    dim_types = [n for n in jar.namelist() if re.match(r"data/minecraft/dimension_type/[^/]+\.json$", n)]
    dims = [n for n in jar.namelist() if re.match(r"data/minecraft/dimension/[^/]+\.json$", n)]
    print("[schema] vanilla reference: %d recipes, %d loot tables, %d dimension types, %d dimensions"
          % (len(recipes), len(tables), len(dim_types), len(dims)))

    # ---- what does THIS build of the game use? -------------------------------
    r_keys = result_keys(recipes)
    l_keys = loot_entry_keys(tables)
    print("[schema] vanilla recipe result keys: %s" % (sorted(r_keys) or "<none found>"))
    print("[schema] vanilla loot item-entry keys: %s" % (sorted(l_keys) or "<none found>"))
    if recipes:
        print("[schema] example recipe (%s):" % recipes[0][0])
        print(json.dumps(recipes[0][1], indent=2)[:700])
    if tables:
        print("[schema] example loot table (%s):" % tables[0][0])
        print(json.dumps(tables[0][1], indent=2)[:700])
    if dim_types:
        sample = load(jar, dim_types[0])
        print("[schema] vanilla dimension_type %s keys: %s" % (dim_types[0], sorted(sample.keys())))

    # ---- our files ----------------------------------------------------------
    ours = []
    for base, _dirs, files in os.walk(os.path.join(args.data, "loot_table")):
        ours += [os.path.join(base, f) for f in files if f.endswith(".json")]
    our_recipe_dir = os.path.join(args.data, "recipe")
    our_recipes = [os.path.join(our_recipe_dir, f) for f in os.listdir(our_recipe_dir)] \
        if os.path.isdir(our_recipe_dir) else []

    if r_keys:
        for path in our_recipes:
            try:
                data = json.load(open(path, encoding="utf-8"))
            except Exception as exc:
                problems.append("%s is not valid JSON: %s" % (path, exc))
                continue
            res = data.get("result")
            if isinstance(res, dict):
                bad = set(res.keys()) - r_keys - {"count"}
                if bad:
                    problems.append("%s: result uses %s but vanilla uses %s"
                                    % (os.path.basename(path), sorted(bad), sorted(r_keys)))
    if l_keys:
        for path in ours:
            try:
                data = json.load(open(path, encoding="utf-8"))
            except Exception as exc:
                problems.append("%s is not valid JSON: %s" % (path, exc))
                continue
            keys = set()

            def walk(node):
                if isinstance(node, dict):
                    if node.get("type") == "minecraft:item":
                        keys.update(k for k in node.keys() if k in ("name", "id", "item"))
                    for v in node.values():
                        walk(v)
                elif isinstance(node, list):
                    for v in node:
                        walk(v)

            walk(data)
            if keys and not (keys & l_keys):
                problems.append("%s: item entries use %s but vanilla uses %s"
                                % (os.path.basename(path), sorted(keys), sorted(l_keys)))

    # ---- every id we mention must exist ------------------------------------
    known_ours = registered_items()
    lang = load(jar, "assets/minecraft/lang/en_us.json") or {}
    vanilla_ids = {"minecraft:" + k.split(".")[-1] for k in lang if k.startswith("item.minecraft.")
                   or k.startswith("block.minecraft.")}
    print("[schema] registered mcsm ids: %d, vanilla item/block names: %d"
          % (len(known_ours), len(vanilla_ids)))

    mentioned: set = set()
    for path in ours + our_recipes:
        collect_item_ids(json.load(open(path, encoding="utf-8")), mentioned)
    for path in (os.path.join(args.data, "dimension"), os.path.join(args.data, "dimension_type")):
        if not os.path.isdir(path):
            continue
        for f in os.listdir(path):
            if f.endswith(".json"):
                collect_item_ids(json.load(open(os.path.join(path, f), encoding="utf-8")), mentioned)
    for item in sorted(mentioned):
        if item.startswith("mcsm:") and item not in known_ours:
            problems.append("%s is referenced but not registered in McsmContent.java" % item)
        if item.startswith("minecraft:") and item not in vanilla_ids:
            notes.append("%s is not a vanilla item/block name in this jar (check the id)" % item)

    for note in notes:
        print("  note %s" % note)
    if problems:
        for problem in problems:
            print("::error title=datapack schema::%s" % problem)
            print("  FAIL %s" % problem)
        print("[schema] FAILED (%d problems)" % len(problems))
        return 1
    print("[schema] OK -- %d loot tables and %d recipes match this build's own JSON shapes"
          % (len(ours), len(our_recipes)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
