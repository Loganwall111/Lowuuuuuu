#!/usr/bin/env python3
"""Generate the Devouring Storms content pack assets (mandate D.8, phase 1).

Writes, into the mod jar's overlay tree:

  jar-overrides/assets/mcsm/blockstates/*.json     -- one per new block
  jar-overrides/assets/mcsm/models/block/*.json    -- cube / pillar / slab /
                                                      stairs / door / trapdoor /
                                                      fence / wall shapes
  jar-overrides/assets/mcsm/models/item/*.json     -- block items + handheld tools
  jar-overrides/assets/mcsm/lang/en_us.json        -- names (merged, not clobbered)
  jar-overrides/data/mcsm/recipe/*.json            -- the crafting set
  jar-overrides/data/mcsm/dimension_type/decayed_reality.json
  jar-overrides/data/mcsm/dimension/decayed_reality.json  -- a REAL dimension

Textures: phase 1 reuses the base mod's own storm textures where they exist
(dabywitherstormmod:block/withered_*, torn_withered_flesh, tainted_obsidian,
command_core_block ...) and vanilla ones elsewhere, so every path is guaranteed
to resolve and a missing file can never turn into a purple-black error block.
Phase 2 replaces the placeholder set with generated storm textures.

Usage: python3 ci/make_mcsm_content_assets.py
"""
from __future__ import annotations

import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "jar-overrides", "assets", "mcsm")
DATA = os.path.join(ROOT, "jar-overrides", "data", "mcsm")

# name -> (shape, texture spec)
#   texture spec: "block/<path>" for a single texture, or a dict of faces
BLOCKS = {
    # ---- decayed world -------------------------------------------------
    "decayed_stone": ("cube", "dabywitherstormmod:block/withered_stone"),
    "decayed_cobblestone": ("cube", "dabywitherstormmod:block/withered_cobblestone"),
    "decayed_stone_bricks": ("cube", "dabywitherstormmod:block/withered_netherbrick"),
    "decayed_dirt": ("cube", "minecraft:block/coarse_dirt"),
    "decayed_surface": ("cube", "minecraft:block/dirt"),
    "decayed_sand": ("cube", "dabywitherstormmod:block/withered_sand"),
    "decayed_log": ("pillar", {"side": "dabywitherstormmod:block/withered_log_side",
                               "end": "dabywitherstormmod:block/withered_log_top"}),
    "decayed_stripped_log": ("pillar", {"side": "dabywitherstormmod:block/stripped_withered_log_side",
                                        "end": "dabywitherstormmod:block/stripped_withered_log_top"}),
    "decayed_planks": ("cube", "dabywitherstormmod:block/withered_planks"),
    "decayed_leaves": ("cube", "minecraft:block/dark_oak_leaves"),
    # ---- abandoned cities ----------------------------------------------
    "city_bricks": ("cube", "minecraft:block/deepslate_tiles"),
    "city_tiles": ("cube", "minecraft:block/polished_deepslate"),
    "rusted_plate": ("cube", "minecraft:block/oxidized_copper"),
    "rebar_grate": ("cube", "minecraft:block/iron_bars"),
    "cracked_road": ("cube", "minecraft:block/cobblestone"),
    "hollow_wall": ("cube", "minecraft:block/bricks"),
    "storm_rib": ("cube", "dabywitherstormmod:block/withered_bone_block"),
    "tendon_block": ("cube", "dabywitherstormmod:block/torn_withered_flesh"),
    "withered_flesh_block": ("cube", "dabywitherstormmod:block/withered_flesh_block"),
    # ---- reality-tear materials ----------------------------------------
    "reality_glass": ("translucent", "minecraft:block/tinted_glass"),
    "glitch_lamp": ("cube", "minecraft:block/sea_lantern"),
    "memory_crystal": ("cube", "minecraft:block/amethyst_block"),
    "void_core": ("cube", "dabywitherstormmod:block/tainted_obsidian"),
    "black_hole_core": ("cube", "minecraft:block/obsidian"),
    "rift_anchor": ("cube", "minecraft:block/netherite_block"),
    # ---- shapes --------------------------------------------------------
    "decayed_slab": ("slab", "dabywitherstormmod:block/withered_stone"),
    "decayed_stairs": ("stairs", "dabywitherstormmod:block/withered_stone"),
    "decayed_wall": ("wall", "dabywitherstormmod:block/withered_cobblestone"),
    "decayed_fence": ("fence", "dabywitherstormmod:block/withered_planks"),
    "city_brick_slab": ("slab", "minecraft:block/deepslate_tiles"),
    "city_brick_stairs": ("stairs", "minecraft:block/deepslate_tiles"),
    # ---- doors ---------------------------------------------------------
    "withered_door": ("door", {"top": "dabywitherstormmod:block/withered_planks",
                               "bottom": "dabywitherstormmod:block/withered_planks"}),
    "withered_trapdoor": ("trapdoor", "dabywitherstormmod:block/withered_planks"),
    "rusted_door": ("door", {"top": "minecraft:block/oxidized_copper",
                             "bottom": "minecraft:block/oxidized_copper"}),
    "rusted_trapdoor": ("trapdoor", "minecraft:block/oxidized_copper"),
}

# item name -> (kind, texture) ; kind = handheld | flat
ITEMS = {
    "rift_key": ("flat", "minecraft:item/echo_shard"),
    "rift_shard": ("flat", "minecraft:item/amethyst_shard"),
    "void_thread": ("flat", "minecraft:item/string"),
    "decayed_bone": ("flat", "minecraft:item/bone"),
    "decayed_steel_ingot": ("flat", "minecraft:item/iron_ingot"),
    "storm_heart_shard": ("flat", "minecraft:item/heart_of_the_sea"),
    "glitch_echo": ("flat", "minecraft:item/ender_pearl"),
    "memory_fragment": ("flat", "minecraft:item/echo_shard"),
    "hallucination_dust": ("flat", "minecraft:item/gunpowder"),
    "city_keycard": ("flat", "minecraft:item/paper"),
    "glyph_cell": ("flat", "minecraft:item/glowstone_dust"),
    "creator_fragment": ("flat", "minecraft:item/nether_star"),
    "abyss_orb": ("flat", "minecraft:item/ender_eye"),
    "tentacle_hook": ("handheld", "minecraft:item/fishing_rod"),
    "reality_ripper": ("handheld", "minecraft:item/netherite_sword"),
    "withered_blade": ("handheld", "minecraft:item/iron_sword"),
    "storm_spear": ("handheld", "minecraft:item/trident"),
    "creators_judgement": ("handheld", "minecraft:item/golden_sword"),
    "echo_totem": ("flat", "minecraft:item/totem_of_undying"),
}

NAMES = {
    "decayed_stone": "Decayed Stone",
    "decayed_cobblestone": "Decayed Cobblestone",
    "decayed_stone_bricks": "Decayed Stone Bricks",
    "decayed_dirt": "Decayed Dirt",
    "decayed_surface": "Decayed Surface",
    "decayed_sand": "Decayed Sand",
    "decayed_log": "Decayed Log",
    "decayed_stripped_log": "Stripped Decayed Log",
    "decayed_planks": "Decayed Planks",
    "decayed_leaves": "Decayed Leaves",
    "city_bricks": "City Bricks",
    "city_tiles": "City Tiles",
    "rusted_plate": "Rusted Plate",
    "rebar_grate": "Rebar Grate",
    "cracked_road": "Cracked Road",
    "hollow_wall": "Hollow Wall",
    "storm_rib": "Storm Rib",
    "tendon_block": "Tendon Block",
    "withered_flesh_block": "Withered Flesh Block",
    "reality_glass": "Reality Glass",
    "glitch_lamp": "Glitch Lamp",
    "memory_crystal": "Memory Crystal",
    "void_core": "Void Core",
    "black_hole_core": "Black Hole Core",
    "rift_anchor": "Rift Anchor",
    "decayed_slab": "Decayed Slab",
    "decayed_stairs": "Decayed Stairs",
    "decayed_wall": "Decayed Wall",
    "decayed_fence": "Decayed Fence",
    "city_brick_slab": "City Brick Slab",
    "city_brick_stairs": "City Brick Stairs",
    "withered_door": "Withered Door",
    "withered_trapdoor": "Withered Trap Door",
    "rusted_door": "Rusted Door",
    "rusted_trapdoor": "Rusted Trap Door",
    "rift_key": "Rift Key",
    "rift_shard": "Rift Shard",
    "void_thread": "Void Thread",
    "decayed_bone": "Decayed Bone",
    "decayed_steel_ingot": "Decayed Steel Ingot",
    "storm_heart_shard": "Storm Heart Shard",
    "glitch_echo": "Glitch Echo",
    "memory_fragment": "Memory Fragment",
    "hallucination_dust": "Hallucination Dust",
    "city_keycard": "City Keycard",
    "glyph_cell": "Glyph Cell",
    "creator_fragment": "Creator Fragment",
    "abyss_orb": "Abyss Orb",
    "tentacle_hook": "Tentacle Hook",
    "reality_ripper": "Reality Ripper",
    "withered_blade": "Withered Blade",
    "storm_spear": "Storm Spear",
    "creators_judgement": "Creator's Judgement",
    "echo_totem": "Echo Totem",
    "tab": "Devouring Storms: Decayed Reality",
}

FACES = ("north", "east", "south", "west", "up", "down")


def write(path: str, obj) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(obj, fh, indent=2, sort_keys=True)
        fh.write("\n")


def texture_map(spec) -> dict:
    if isinstance(spec, str):
        return {face: spec for face in FACES}
    out = {}
    for face in FACES:
        if face in spec:
            out[face] = spec[face]
        elif "all" in spec:
            out[face] = spec["all"]
        else:
            out[face] = spec.get("side", spec.get("top"))
    return out


def emit_cube(name, spec, translucent=False):
    tex = texture_map(spec)
    model = {"parent": "minecraft:block/cube", "textures": {"particle": tex["north"], **tex}}
    if translucent:
        model["render_type"] = "minecraft:translucent"
    write(os.path.join(ASSETS, "models", "block", name + ".json"), model)
    write(os.path.join(ASSETS, "blockstates", name + ".json"),
          {"variants": {"": {"model": f"mcsm:block/{name}"}}})


def emit_pillar(name, spec):
    write(os.path.join(ASSETS, "models", "block", name + ".json"),
          {"parent": "minecraft:block/cube_column",
           "textures": {"end": spec["end"], "side": spec["side"]}})
    write(os.path.join(ASSETS, "blockstates", name + ".json"),
          {"variants": {"axis=y": {"model": f"mcsm:block/{name}"},
                        "axis=z": {"model": f"mcsm:block/{name}", "x": 90},
                        "axis=x": {"model": f"mcsm:block/{name}", "x": 90, "y": 90}}})


def emit_slab(name, spec):
    tex = texture_map(spec)
    for suffix, parent in (("", "minecraft:block/slab"),
                           ("_top", "minecraft:block/slab_top"),
                           ("_double", "minecraft:block/cube")):
        write(os.path.join(ASSETS, "models", "block", f"{name}{suffix}.json"),
              {"parent": parent,
               "textures": {"bottom": tex["down"], "top": tex["up"],
                            "side": tex["north"], "particle": tex["north"]}})
    write(os.path.join(ASSETS, "blockstates", name + ".json"),
          {"variants": {
              "type=bottom": {"model": f"mcsm:block/{name}"},
              "type=top": {"model": f"mcsm:block/{name}_top"},
              "type=double": {"model": f"mcsm:block/{name}_double"}}})


STAIR_ROT = {
    # (shape, facing) -> (model suffix, y rotation)
    ("straight", "east"): ("", 0), ("straight", "south"): ("", 90),
    ("straight", "west"): ("", 180), ("straight", "north"): ("", 270),
    ("outer_right", "east"): ("_outer", 0), ("outer_right", "south"): ("_outer", 90),
    ("outer_right", "west"): ("_outer", 180), ("outer_right", "north"): ("_outer", 270),
    ("outer_left", "east"): ("_outer", 270), ("outer_left", "south"): ("_outer", 0),
    ("outer_left", "west"): ("_outer", 90), ("outer_left", "north"): ("_outer", 180),
    ("inner_right", "east"): ("_inner", 0), ("inner_right", "south"): ("_inner", 90),
    ("inner_right", "west"): ("_inner", 180), ("inner_right", "north"): ("_inner", 270),
    ("inner_left", "east"): ("_inner", 270), ("inner_left", "south"): ("_inner", 0),
    ("inner_left", "west"): ("_inner", 90), ("inner_left", "north"): ("_inner", 180),
}


def emit_stairs(name, spec):
    tex = texture_map(spec)
    textures = {"bottom": tex["down"], "top": tex["up"], "side": tex["north"]}
    for suffix, parent in (("", "minecraft:block/stairs"),
                           ("_inner", "minecraft:block/inner_stairs"),
                           ("_outer", "minecraft:block/outer_stairs"),
                           ("_top", "minecraft:block/stairs"),
                           ("_top_inner", "minecraft:block/inner_stairs"),
                           ("_top_outer", "minecraft:block/outer_stairs")):
        write(os.path.join(ASSETS, "models", "block", f"{name}{suffix}.json"),
              {"parent": parent, "textures": textures})
    variants = {}
    for half in ("bottom", "top"):
        prefix = "" if half == "bottom" else "_top"
        for shape in ("straight", "outer_right", "outer_left", "inner_right", "inner_left"):
            suffix, rot = STAIR_ROT[(shape, "east")]  # base facing
            for facing in ("east", "south", "west", "north"):
                _, extra = STAIR_ROT[(shape, facing)]
                entry = {"model": f"mcsm:block/{name}{prefix}{suffix}"}
                total = (rot + extra) % 360
                if total:
                    entry["y"] = total
                    entry["uvlock"] = True
                variants[f"facing={facing},half={half},shape={shape}"] = entry
    write(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": variants})


def emit_fence(name, spec):
    tex = texture_map(spec)["north"]
    model = {"textures": {"texture": tex}}
    for suffix, parent in (("_post", "minecraft:block/fence_post"),
                           ("_side", "minecraft:block/fence_side"),
                           ("_inventory", "minecraft:block/fence_inventory")):
        write(os.path.join(ASSETS, "models", "block", f"{name}{suffix}.json"),
              {"parent": parent, **model})
    multipart = [{"apply": {"model": f"mcsm:block/{name}_post"}}] + [
        {"when": {"north": "true"}, "apply": {"model": f"mcsm:block/{name}_side", "uvlock": True}},
        {"when": {"east": "true"}, "apply": {"model": f"mcsm:block/{name}_side", "y": 90, "uvlock": True}},
        {"when": {"south": "true"}, "apply": {"model": f"mcsm:block/{name}_side", "y": 180, "uvlock": True}},
        {"when": {"west": "true"}, "apply": {"model": f"mcsm:block/{name}_side", "y": 270, "uvlock": True}},
    ]
    write(os.path.join(ASSETS, "blockstates", name + ".json"), {"multipart": multipart})


def emit_wall(name, spec):
    tex = texture_map(spec)["north"]
    t = {"wall": tex}
    for suffix, parent in (("_post", "minecraft:block/template_wall_post"),
                           ("_side", "minecraft:block/template_wall_side"),
                           ("_side_tall", "minecraft:block/template_wall_side_tall"),
                           ("_inventory", "minecraft:block/wall_inventory")):
        write(os.path.join(ASSETS, "models", "block", f"{name}{suffix}.json"),
              {"parent": parent, "textures": t})
    multipart = [{"apply": {"model": f"mcsm:block/{name}_post"}}]
    for direction, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for state, suffix in (("low", "_side"), ("tall", "_side_tall")):
            entry = {"model": f"mcsm:block/{name}{suffix}", "uvlock": True}
            if rot:
                entry["y"] = rot
            multipart.append({"when": {direction: state}, "apply": entry})
    write(os.path.join(ASSETS, "blockstates", name + ".json"), {"multipart": multipart})


def emit_door(name, spec):
    for suffix, parent in (("_bottom_left", "minecraft:block/door_bottom_left"),
                           ("_bottom_right", "minecraft:block/door_bottom_right"),
                           ("_top_left", "minecraft:block/door_top_left"),
                           ("_top_right", "minecraft:block/door_top_right")):
        write(os.path.join(ASSETS, "models", "block", f"{name}{suffix}.json"),
              {"parent": parent, "textures": {"top": spec["top"], "bottom": spec["bottom"]}})
    variants = {}
    for facing, rot in (("east", 0), ("south", 90), ("west", 180), ("north", 270)):
        for hinge in ("left", "right"):
            for open_state in ("false", "true"):
                for half in ("lower", "upper"):
                    entry = {"model": f"mcsm:block/{name}_{half}_{hinge}"}
                    if rot:
                        entry["y"] = rot
                    variants[f"facing={facing},half={half},hinge={hinge},open={open_state}"] = entry
    write(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": variants})


def emit_trapdoor(name, spec):
    tex = texture_map(spec)["north"]
    for suffix, parent in (("_bottom", "minecraft:block/template_trapdoor_bottom"),
                           ("_top", "minecraft:block/template_trapdoor_top"),
                           ("_open", "minecraft:block/template_trapdoor_open")):
        write(os.path.join(ASSETS, "models", "block", f"{name}{suffix}.json"),
              {"parent": parent, "textures": {"texture": tex}})
    variants = {}
    for facing, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for open_state in ("false", "true"):
            for half, suffix in (("top", "_top"), ("bottom", "_bottom")):
                model = f"mcsm:block/{name}{'_open' if open_state == 'true' else suffix}"
                entry = {"model": model}
                if rot:
                    entry["y"] = rot
                variants[f"facing={facing},half={half},open={open_state}"] = entry
    write(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": variants})


EMITTERS = {"cube": emit_cube, "translucent": lambda n, s: emit_cube(n, s, True),
            "pillar": emit_pillar, "slab": emit_slab, "stairs": emit_stairs,
            "fence": emit_fence, "wall": emit_wall, "door": emit_door,
            "trapdoor": emit_trapdoor}


def emit_dimension():
    """A REAL dimension: own dimension type, own flat terrain of our blocks."""
    write(os.path.join(DATA, "dimension_type", "decayed_reality.json"), {
        "ambient_light": 0.05,
        "attributes": {
            "minecraft:gameplay/bed_rule": {"can_set_spawn": "never",
                                            "can_sleep": "never", "explodes": True},
            "minecraft:gameplay/can_start_raid": False,
            "minecraft:gameplay/fast_lava": False,
            "minecraft:gameplay/piglins_zombify": False,
            "minecraft:gameplay/respawn_anchor_works": False,
            "minecraft:gameplay/sky_light_level": 0.0,
            "minecraft:gameplay/snow_golem_melts": False,
            "minecraft:gameplay/water_evaporates": False,
            "minecraft:visual/ambient_light_color": "#140A1E",
            "minecraft:visual/fog_start_distance": 2.0,
            "minecraft:visual/fog_end_distance": 64.0,
            "minecraft:visual/sky_light_color": "#5A2A7A",
            "minecraft:visual/sky_light_factor": 0.0,
        },
        "cardinal_light": "nether",
        "coordinate_scale": 1.0,
        "has_ceiling": False,
        "has_ender_dragon_fight": False,
        "has_fixed_time": True,
        "has_skylight": False,
        "height": 384,
        "infiniburn": "#minecraft:infiniburn_overworld",
        "logical_height": 384,
        "min_y": -64,
        "monster_spawn_block_light_limit": 0,
        "monster_spawn_light_level": 0,
        "skybox": "none",
        "timelines": "#minecraft:in_nether",
    })
    write(os.path.join(DATA, "dimension", "decayed_reality.json"), {
        "type": "mcsm:decayed_reality",
        "generator": {
            "type": "minecraft:flat",
            "settings": {
                "layers": [
                    {"block": "mcsm:void_core", "height": 1},
                    {"block": "mcsm:decayed_stone", "height": 38},
                    {"block": "mcsm:cracked_road", "height": 6},
                    {"block": "mcsm:decayed_surface", "height": 2},
                    {"block": "mcsm:decayed_planks", "height": 3},
                ],
                "biome": "minecraft:plains",
                "lakes": False,
                "features": False,
                "structure_overrides": [],
            },
        },
    })


RECIPES = {
    "decayed_planks_from_log": (["A"], {"A": "mcsm:decayed_log"}, "mcsm:decayed_planks", 4),
    "city_brick_slab": (["AAA"], {"A": "mcsm:city_bricks"}, "mcsm:city_brick_slab", 6),
    "city_brick_stairs": (["A  ", "AA ", "AAA"], {"A": "mcsm:city_bricks"},
                          "mcsm:city_brick_stairs", 4),
    "decayed_slab": (["AAA"], {"A": "mcsm:decayed_stone"}, "mcsm:decayed_slab", 6),
    "decayed_stairs": (["A  ", "AA ", "AAA"], {"A": "mcsm:decayed_stone"},
                       "mcsm:decayed_stairs", 4),
    "rift_key": ([" S ", "SRS", " S "], {"S": "mcsm:rift_shard", "R": "mcsm:glyph_cell"},
                 "mcsm:rift_key", 1),
    "reality_ripper": ([" D ", " D ", " S "], {"D": "mcsm:decayed_steel_ingot",
                                               "S": "mcsm:storm_heart_shard"},
                       "mcsm:reality_ripper", 1),
    "rift_shard": (["GG", "GG"], {"G": "mcsm:glitch_echo"}, "mcsm:rift_shard", 2),
    "reality_glass": (["RG", "GR"], {"R": "mcsm:rift_shard", "G": "minecraft:glass"},
                      "mcsm:reality_glass", 4),
    "glitch_lamp": (["RG", "GR"], {"R": "mcsm:rift_shard", "G": "minecraft:glowstone"},
                    "mcsm:glitch_lamp", 2),
    "withered_door": (["AA", "AA", "AA"], {"A": "mcsm:decayed_planks"},
                      "mcsm:withered_door", 3),
    "withered_trapdoor": (["AAA", "AAA"], {"A": "mcsm:decayed_planks"},
                          "mcsm:withered_trapdoor", 2),
    "decayed_fence": (["ABA", "ABA"], {"A": "mcsm:decayed_planks", "B": "minecraft:stick"},
                      "mcsm:decayed_fence", 3),
    "decayed_wall": (["AAA", "AAA"], {"A": "mcsm:decayed_cobblestone"},
                     "mcsm:decayed_wall", 6),
    "decayed_steel_ingot": (["BB", "BB"], {"B": "mcsm:decayed_bone"},
                            "mcsm:decayed_steel_ingot", 1),
    "echo_totem": (["GFG", "FTF", "GFG"], {"G": "mcsm:glyph_cell", "F": "mcsm:memory_fragment",
                                           "T": "minecraft:totem_of_undying"},
                   "mcsm:echo_totem", 1),
}


def emit_recipes():
    for name, (pattern, key, result, count) in RECIPES.items():
        shaped = len({len(row) for row in pattern}) == 1 if len(pattern) > 1 else True
        if shaped and all(len(row) == len(pattern[0]) for row in pattern):
            write(os.path.join(DATA, "recipe", name + ".json"), {
                "type": "minecraft:crafting_shaped",
                "pattern": pattern,
                "key": {k: {"item": v} for k, v in key.items()},
                "result": {"id": result, "count": count},
            })
        else:
            write(os.path.join(DATA, "recipe", name + ".json"), {
                "type": "minecraft:crafting_shapeless",
                "ingredients": [{"item": v} for v in key.values()],
                "result": {"id": result, "count": count},
            })


def emit_lang():
    path = os.path.join(ASSETS, "lang", "en_us.json")
    existing = {}
    if os.path.exists(path):
        with open(path, encoding="utf-8") as fh:
            existing = json.load(fh)
    for name, pretty in NAMES.items():
        if name == "tab":
            existing["itemGroup.mcsm.content"] = pretty
        else:
            existing[f"block.mcsm.{name}"] = pretty
            existing[f"item.mcsm.{name}"] = pretty
    write(path, existing)


def main() -> int:
    for name, (shape, spec) in BLOCKS.items():
        EMITTERS[shape](name, spec)
    for name, (kind, tex) in ITEMS.items():
        parent = "minecraft:item/handheld" if kind == "handheld" else "minecraft:item/generated"
        write(os.path.join(ASSETS, "models", "item", name + ".json"),
              {"parent": parent, "textures": {"layer0": tex}})
    # block items that are a full cube reuse the block model, shapes get their
    # own inventory model so the item icon matches what is placed
    for name, (shape, _spec) in BLOCKS.items():
        if shape in ("cube", "translucent", "pillar"):
            parent = f"mcsm:block/{name}"
        elif shape == "slab":
            parent = f"mcsm:block/{name}"
        elif shape == "stairs":
            parent = f"mcsm:block/{name}"
        elif shape == "fence":
            parent = f"mcsm:block/{name}_inventory"
        elif shape == "wall":
            parent = f"mcsm:block/{name}_inventory"
        elif shape == "trapdoor":
            parent = f"mcsm:block/{name}_bottom"
        else:
            parent = f"mcsm:block/{name}_bottom_left"
        write(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": parent})
    emit_dimension()
    emit_recipes()
    emit_lang()

    # validate: every file we just wrote must be parseable JSON
    bad = []
    for base in (ASSETS, DATA):
        for root, _dirs, files in os.walk(base):
            for f in files:
                if not f.endswith(".json"):
                    continue
                p = os.path.join(root, f)
                try:
                    with open(p, encoding="utf-8") as fh:
                        json.load(fh)
                except Exception as exc:  # pragma: no cover - tooling only
                    bad.append("%s: %s" % (os.path.relpath(p, ROOT), exc))
    if bad:
        print("\n".join(bad))
        return 1
    counts = {}
    for base, label in ((ASSETS, "assets"), (DATA, "data")):
        n = sum(len(files) for _r, _d, files in os.walk(base))
        counts[label] = n
    print("mcsm content pack: %d blocks, %d items, %d asset files, %d data files"
          % (len(BLOCKS), len(BLOCKS) + len(ITEMS), counts["assets"], counts["data"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
