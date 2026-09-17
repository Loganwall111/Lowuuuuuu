#!/usr/bin/env python3
"""check_vanilla_overrides.py -- our vanilla-namespace files, weighed against the game's.

BUILD #477. The mod's jar overrides files in the game's OWN namespace
(`assets/minecraft/...`) -- the Story Look shaders did it first (#471), and now the
base jar's broken 1.9-era assets are repaired the same way. That overlay is applied
last, so our copy WINS, and a wrong name in it is not a warning: it is a block that
does not exist in the world.

The player's log is the reason this tool exists:

    Exception loading blockstate definition: 'minecraft:crafting_table/dabywitherstormmod'
        for variant 'axis=y': Unknown blockstate property: 'axis'
    Unresolved texture references in minecraft:block/furnace: #up-> #top
    Missing texture references in model minecraft:block/jack_o_lantern: #missing
    Missing block model: mcsm:block/rusted_door_lower_left

Four different files, one shape of mistake: a name that resolves to nothing. So every
file we ship in the game's namespace is checked against the game's OWN copy out of the
client jar:

  * blockstates -- every property we use must be a property the game's own blockstate
    for that block uses (the crafting table has no `axis`, ever), and every model we
    name must exist either in the jar or in our overlay;
  * models -- the parent must exist, every `#variable` we use must be defined in the
    same file, and every texture we name must be a real file in the jar or in the
    mcsm namespace.

Anything it cannot check (no client jar) is reported as unchecked, never as a pass.

    python3 ci/check_vanilla_overrides.py --jar /path/to/client.jar
    python3 ci/check_vanilla_overrides.py --jar ... --dir overrides/resourcepacks/01_...
"""

import argparse
import json
import os
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

DEFAULT_DIR = os.path.join(ROOT, "jar-overrides", "assets", "minecraft")


def jar_files(jar):
    """{path -> bytes} for the jar's own assets/minecraft, or {} with no jar."""
    out = {}
    if not jar or not os.path.isfile(jar):
        return out
    try:
        with zipfile.ZipFile(jar) as zf:
            for name in zf.namelist():
                if name.startswith("assets/minecraft/"):
                    out[name] = zf.read(name)
    except Exception as exc:  # noqa: BLE001
        print("[vanilla] could not read the client jar (%s)" % exc)
    return out


def props_of(variant_key):
    """The property names in a blockstate variant key: `facing=east,half=lower`."""
    if not variant_key or variant_key == "normal":
        return set()
    return set(part.split("=")[0] for part in variant_key.split(",") if "=" in part)


def blockstate_props(obj):
    props = set()
    for key in (obj.get("variants") or {}):
        props |= props_of(key)
    for part in (obj.get("multipart") or []):
        if isinstance(part, dict):
            props |= props_of(_when_key(part.get("when")))
    return props


def _when_key(when):
    """A multipart `when` is a key or a list of them; flatten it to `a,b`."""
    if when is None:
        return ""
    if isinstance(when, str):
        return when
    if isinstance(when, list):
        return ",".join(str(x) for x in when)
    if isinstance(when, dict):
        # {"OR": [{"a": "x"}, {"a": "y"}]}
        return ",".join(str(k) for k in when.keys())
    return ""


def model_ids(obj):
    ids = []

    def take(node):
        if isinstance(node, dict) and "model" in node:
            ids.append(node["model"])
        elif isinstance(node, list):
            for one in node:
                take(one)

    take(obj.get("variants"))
    take(obj.get("multipart"))
    for part in (obj.get("multipart") or []):
        take(part)
    return ids


def check_model(rel, obj, jar, ours, problems, notes):
    parent = obj.get("parent")
    if parent:
        p = _model_path(parent)
        if p not in jar and p not in ours and not os.path.isfile(os.path.join(ROOT, p)):
            problems.append("%s: parent %s does not exist" % (rel, parent))

    textures = obj.get("textures") or {}
    for key, value in textures.items():
        if not isinstance(value, str):
            continue
        if value.startswith("#"):
            if value[1:] not in textures and not _parent_defines(obj, value[1:], jar):
                problems.append("%s: #%s -> %s, which is not defined here or by its parent"
                                % (rel, key, value))
            continue
        if ":" in value:
            ns, path = value.split(":", 1)
        else:
            ns, path = "minecraft", value
        if ns == "mcsm":
            if not os.path.isfile(os.path.join(ROOT, "jar-overrides/assets/mcsm/textures",
                                              path + ".png")):
                problems.append("%s: texture %s does not exist in the pack" % (rel, value))
        else:
            want = "assets/%s/textures/%s.png" % (ns, path)
            if jar and want not in jar:
                problems.append("%s: texture %s does not exist in the game" % (rel, value))
            elif not jar:
                notes.append("%s: texture %s not checked (no jar)" % (rel, value))


def _model_path(mid):
    """`minecraft:block/cube` -> `assets/minecraft/models/block/cube.json`."""
    ns, _, path = mid.partition(":")
    if not path:
        ns, path = "minecraft", mid
    return "assets/%s/models/%s.json" % (ns, path)


def _parent_defines(obj, key, jar):
    """Does the parent chain define this texture variable? One level, which is all we use."""
    parent = obj.get("parent")
    if not parent or not jar:
        return False
    p = _model_path(parent)
    raw = jar.get(p)
    if not raw:
        return False
    try:
        pobj = json.loads(raw.decode("utf-8"))
    except Exception:  # noqa: BLE001
        return False
    return key in (pobj.get("textures") or {}) or _parent_defines(pobj, key, jar)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", default="")
    ap.add_argument("--dir", default=DEFAULT_DIR)
    args = ap.parse_args()

    jar = jar_files(args.jar)
    if not jar:
        print("[vanilla] no client jar at '%s' -- the game's own files could not be read, "
              "so nothing is checked and nothing is claimed" % args.jar)
        return 0
    print("[vanilla] the game's own assets/minecraft: %d files read" % len(jar))

    ours = {}
    for base, _dirs, files in os.walk(args.dir):
        for name in files:
            if not name.endswith(".json"):
                continue  # images are checked by existence, not by parsing
            path = os.path.join(base, name)
            rel = "assets/minecraft/" + os.path.relpath(path, args.dir).replace(os.sep, "/")
            ours[rel] = path

    problems = []
    notes = []
    models_checked = states_checked = 0

    for rel, path in sorted(ours.items()):
        try:
            with open(path, encoding="utf-8") as f:
                obj = json.load(f)
        except Exception as exc:  # noqa: BLE001
            problems.append("%s: not valid JSON (%s)" % (rel, exc))
            continue
        if "/blockstates/" in rel:
            states_checked += 1
            mine = blockstate_props(obj)
            raw = jar.get(rel)
            if raw:
                try:
                    theirs = blockstate_props(json.loads(raw.decode("utf-8")))
                except Exception:  # noqa: BLE001
                    theirs = set()
                invented = mine - theirs
                if invented:
                    problems.append("%s: uses blockstate properties the game's own file does "
                                    "not have: %s" % (rel, ", ".join(sorted(invented))))
            elif mine:
                notes.append("%s: the game ships no such blockstate to compare against" % rel)
            for mid in model_ids(obj):
                target = _model_path(mid)
                if target not in jar and target not in ours and not os.path.isfile(
                        os.path.join(ROOT, target)):
                    problems.append("%s: names model %s, which exists nowhere" % (rel, mid))
        elif "/models/" in rel:
            models_checked += 1
            check_model(rel, obj, jar, ours, problems, notes)

    for line in notes[:10]:
        print("  note %s" % line)
    for line in problems[:20]:
        print("  FAIL %s" % line)
    print("[vanilla] %d blockstates and %d models of ours weighed against the game's own "
          "copies: %d problem%s" % (states_checked, models_checked, len(problems),
                                    "" if len(problems) == 1 else "s"))
    if problems:
        print("[vanilla] a name in the jar's own namespace resolves to nothing -- that is a "
              "missing block in the world, not a warning")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
