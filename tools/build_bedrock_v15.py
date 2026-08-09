#!/usr/bin/env python3
"""Bedrock v1.5 mirror: eight new entities (+massg_rose variant), three blocks,
two items, four records (sound defs), and the Creator event loop script.

Bedrock cannot define custom dimensions; the planets ship as build templates via
the existing builds_data.js structures placed by main.js commands, documented.
"""

from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "bedrock-addon"
BP = ROOT / "DevouringStormsBP"
RP = ROOT / "DevouringStormsRP"

FMT = "1.21.80"
DS = "ds"

def bp_entity(ident, *, hp, speed, box, dmg=None, family=("monster",), kb=0.0,
              scale=1.0, egg=None, summonable=True, extra=None, flying=False):
    comps = {
        f"{DS}:marker": {},
        "minecraft:type_family": {"family": list(family)},
        "minecraft:health": {"value": hp, "max": hp},
        "minecraft:movement": {"value": speed},
        "minecraft:collision_box": {"width": box[0], "height": box[1]},
        "minecraft:physics": {"has_gravity": not flying, "has_collision": True},
        "minecraft:pushable": {"is_pushable": kb < 1.0, "is_pushable_by_piston": kb < 1.0},
        "minecraft:knockback_resistance": {"value": kb},
        "minecraft:nameable": {},
        "minecraft:follow_range": {"value": 64.0, "max": 64.0},
        "minecraft:behavior.float": {"priority": 0},
        "minecraft:behavior.random_stroll": {"priority": 6, "speed_multiplier": 0.8},
        "minecraft:behavior.look_at_player": {"priority": 7, "look_distance": 10.0},
        "minecraft:behavior.random_look_around": {"priority": 8},
    }
    if scale != 1.0:
        comps["minecraft:scale"] = {"value": scale}
    if dmg is not None:
        comps["minecraft:attack"] = {"damage": dmg}
        comps["minecraft:behavior.melee_attack"] = {"priority": 2, "speed_multiplier": 1.15}
        comps["minecraft:behavior.nearest_attackable_target"] = {
            "priority": 3,
            "within_radius": 24.0,
            "entity_types": [{"filters": {"test": "is_family", "subject": "other", "value": "player"},
                              "max_dist": 24}],
        }
    if flying:
        comps["minecraft:behavior.hover_wander"] = {"priority": 4}
        comps["minecraft:navigation.walk"] = {"can_float": True}
    if extra:
        comps.update(extra)

    entity = {
        "format_version": FMT,
        "minecraft:entity": {
            "description": {"identifier": f"{DS}:{ident}", "is_spawnable": True,
                            "is_summonable": summonable, "is_experimental": False},
            "components": comps,
        },
    }
    if egg:
        entity["minecraft:entity"]["description"]["is_spawnable"] = True
    return entity

def client_entity(ident, base_color, overlay_color, mat="entity_alphatest", glow_mat=None):
    return {
        "format_version": FMT,
        "minecraft:client_entity": {
            "description": {
                "identifier": f"{DS}:{ident}",
                "materials": {"default": mat} | ({"emitter": glow_mat} if glow_mat else {}),
                "textures": {"default": f"textures/entity/{ident}"},
                "geometry": {"default": f"geometry.ds.{ident}"},
                "render_controllers": ["controller.render.ds.basic"],
                "spawn_egg": {"base_color": base_color, "overlay_color": overlay_color},
                "enable_attachables": False,
            }
        },
    }

def geo_model(ident, group, tex_wh, parts):
    """parts: list of dicts {name, parent, pivot, cubes:[o,s], uv:[u,v]}"""
    bones = []
    for p in parts:
        bones.append({
            "name": p["name"],
            "parent": p.get("parent"),
            "pivot": p.get("pivot", [0, 0, 0]),
            "cubes": [{"origin": c[0], "size": c[1], "uv": c[2]} for c in p["cubes"]] if p.get("cubes") else None,
        } | {k: v for k, v in p.items() if k not in ("name", "parent", "pivot", "cubes")})
    for b in bones:
        b.pop("cubes", None) if b.get("cubes") is None else None
        b.pop("parent", None) if b.get("parent") is None else None
    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": f"geometry.{DS}.{ident}",
                "texture_width": tex_wh[0],
                "texture_height": tex_wh[1],
            },
            "bones": bones,
        }],
    }

# ================================================================ THE CAST

ENT = {}

# THE CREATOR — cosmos-sized overseer. Bedrock: giant, slow, unmoved by anything.
ENT["creator"] = dict(
    bp=bp_entity("creator", hp=900, speed=0.18, box=(7.5, 20.0), dmg=30, kb=1.0, scale=8.0,
                 family=("ds_creator", "monster"),
                 extra={"minecraft:damage_sensor": {"triggers": [
                     {"cause": "fall", "deals_damage": False},
                     {"cause": "magic", "deals_damage": True},
                 ]}}),
    rp=client_entity("creator", "#0c0814", "#ff3c3c", glow_mat="entity_emissive"),
    geo=geo_model("creator", "v15", (96, 96), [
        dict(name="robe", pivot=[0, 0, 0], cubes=[([-7, 0, -4], [14, 32, 8], [0, 0])]),
        dict(name="head", parent="robe", pivot=[0, 32, 0], cubes=[([-5, 32, -4], [10, 10, 8], [60, 0])]),
        dict(name="eyes", parent="head", pivot=[0, 36, -4], cubes=[([-4, 34.5, -4.5], [3, 2, 1], [62, 4]),
                                                                  ([1, 34.5, -4.5], [3, 2, 1], [76, 4])]),
        dict(name="crown", parent="head", pivot=[0, 42, 0], cubes=[([-5, 42, -4], [10, 3, 8], [48, 40])]),
        dict(name="arm_l", parent="robe", pivot=[7, 28, 0], cubes=[([7, 10, -2], [4, 20, 4], [0, 46])]),
        dict(name="arm_r", parent="robe", pivot=[-7, 28, 0], cubes=[([-11, 10, -2], [4, 20, 4], [48, 56])]),
    ]),
)

# THE HAND — management's actual hand. Unsummonable (script-driven).
ENT["creator_hand"] = dict(
    bp=bp_entity("creator_hand", hp=480, speed=0.0, box=(6.0, 6.0), dmg=34, kb=1.0,
                 summonable=False, family=("ds_creator", "monster"), flying=True,
                 extra={"minecraft:behavior.random_stroll": {"priority": 4, "speed_multiplier": 0.0}}),
    rp=client_entity("creator_hand", "#0c0814", "#ff3c3c"),
    geo=geo_model("creator_hand", "v15", (64, 64), [
        dict(name="palm", pivot=[0, 0, 0], cubes=[([-7, -3, -3], [14, 6, 7], [0, 0])]),
        dict(name="finger_1", parent="palm", pivot=[-5, 3, 0], cubes=[([-6, 3, -2], [3, 8, 3], [0, 18])]),
        dict(name="finger_2", parent="palm", pivot=[-1.5, 3, 0], cubes=[([-2.5, 3, -2], [3, 9, 3], [12, 18])]),
        dict(name="finger_3", parent="palm", pivot=[1.5, 3, 0], cubes=[([0.5, 3, -2], [3, 8, 3], [24, 18])]),
        dict(name="finger_4", parent="palm", pivot=[5, 3, 0], cubes=[([4, 3, -2], [3, 7, 3], [36, 18])]),
        dict(name="finger_5", parent="palm", pivot=[7, 1, 0], cubes=[([7, -1, -2], [3, 5, 3], [48, 18])]),
    ]),
)

# THE MONSTROSITY — moustached mayor of the glitch lawns.
ENT["monstrosity"] = dict(
    bp=bp_entity("monstrosity", hp=120, speed=0.24, box=(2.2, 3.0), dmg=14,
                 family=("ds_monstrosity", "monster"),
                 extra={"minecraft:behavior.jump_attack": {"priority": 2, "speed_multiplier": 1.1}}),
    rp=client_entity("monstrosity", "#3a124a", "#ff3fc8", glow_mat="entity_emissive"),
    geo=geo_model("monstrosity", "v15", (96, 64), [
        dict(name="body", pivot=[0, 0, 0], cubes=[([-5, 0, -3], [10, 14, 6], [0, 0])]),
        dict(name="head", parent="body", pivot=[0, 14, 0], cubes=[([-4, 14, -3], [8, 8, 6], [32, 20])]),
        dict(name="moustache", parent="head", pivot=[0, 17, -3], cubes=[([-4, 16, -3.5], [8, 2, 1], [34, 24])]),
        dict(name="antenna_l", parent="head", pivot=[3, 22, 0], cubes=[([3, 22, -1], [1, 6, 1], [48, 30])]),
        dict(name="antenna_r", parent="head", pivot=[-3, 22, 0], cubes=[([-4, 22, -1], [1, 6, 1], [40, 40])]),
        dict(name="arm_l", parent="body", pivot=[5, 10, 0], cubes=[([5, 2, -1.5], [3, 9, 3], [0, 21])]),
        dict(name="arm_r", parent="body", pivot=[-5, 10, 0], cubes=[([-8, 2, -1.5], [3, 9, 3], [0, 40])]),
        dict(name="leg_l", parent="body", pivot=[2.5, 2, 0], cubes=[([1, 0, -1.5], [3, 2, 3], [20, 0])]),
        dict(name="leg_r", parent="body", pivot=[-2.5, 2, 0], cubes=[([-4, 0, -1.5], [3, 2, 3], [26, 0])]),
    ]),
)

# THE FORGER — the foundry bell; summons sky_tentacle rains via script.
ENT["forger"] = dict(
    bp=bp_entity("forger", hp=160, speed=0.2, box=(2.6, 5.5), dmg=16, kb=1.0,
                 family=("ds_forger", "monster"), flying=True),
    rp=client_entity("forger", "#141e2e", "#96dcff", glow_mat="entity_emissive"),
    geo=geo_model("forger", "v15", (64, 64), [
        dict(name="bell", pivot=[0, 0, 0], cubes=[([-5, 0, -3], [10, 12, 6], [0, 0])]),
        dict(name="flame", parent="bell", pivot=[0, 6, -3], cubes=[([-2, 5, -3.5], [4, 2, 1], [48, 20])]),
        *([dict(name=f"spout_{i}", pivot=[0, 12, 0], cubes=[([int(-1 + (i % 4) * 1), 12, -1 if i < 4 else 1], [2, 5, 2], [6 + i * 8, 0])]) for i in range(8)]),
    ]),
)

# SKY TENTACLE — it lands; the Forger was right to spit it.
ENT["sky_tentacle"] = dict(
    bp=bp_entity("sky_tentacle", hp=16, speed=0.3, box=(1.2, 3.0), dmg=6,
                 family=("ds_tentacle", "monster")),
    rp=client_entity("sky_tentacle", "#28143e", "#c85aff"),
    geo=geo_model("sky_tentacle", "v15", (64, 64), [
        dict(name="stalk", pivot=[0, 0, 0], cubes=[([-2, 0, -2], [4, 12, 4], [0, 0])]),
        dict(name="tip", parent="stalk", pivot=[0, 12, 0], cubes=[([-2.5, 12, -2.5], [5, 3, 5], [0, 20])]),
    ]),
)

# CART SHOPPER — resumes its errand; the errand ends in you.
ENT["cart_shopper"] = dict(
    bp=bp_entity("cart_shopper", hp=26, speed=0.32, box=(1.4, 1.8), dmg=6,
                 family=("ds_shopper", "monster")),
    rp=client_entity("cart_shopper", "#283034", "#dcdcdc", glow_mat="entity_emissive"),
    geo=geo_model("cart_shopper", "v15", (64, 64), [
        dict(name="body", pivot=[0, 0, 0], cubes=[([-3, 0, -2], [6, 12, 4], [0, 0])]),
        dict(name="head", parent="body", pivot=[0, 12, 0], cubes=[([-3, 12, -2.5], [6, 6, 5], [0, 15])]),
        dict(name="cart", pivot=[0, 4, -6], cubes=[([-5, 0, -10], [10, 5, 8], [0, 30])]),
        dict(name="wheel_l", parent="cart", pivot=[-4, 0, -6], cubes=[([-5, 0, -7], [1, 3, 3], [20, 30])]),
        dict(name="wheel_r", parent="cart", pivot=[4, 0, -6], cubes=[([4, 0, -7], [1, 3, 3], [36, 30])]),
    ]),
)

# E.P.A. RESEARCHER — writes reports on impossibility with legitimate stationery.
ENT["researcher"] = dict(
    bp=bp_entity("researcher", hp=24, speed=0.25, box=(0.6, 1.9), dmg=None,
                 family=("ds_researcher", "ambient")),
    rp=client_entity("researcher", "#e6e6eb", "#285a28", glow_mat="entity_emissive"),
    geo=geo_model("researcher", "v15", (64, 64), [
        dict(name="body", pivot=[0, 0, 0], cubes=[([-3, 0, -2], [6, 12, 4], [0, 0])]),
        dict(name="head", parent="body", pivot=[0, 12, 0], cubes=[([-3, 12, -2.5], [6, 6, 5], [0, 15])]),
        dict(name="glasses", parent="head", pivot=[0, 15, -2.5], cubes=[([-3, 14.5, -3], [6, 1, 1], [32, 18])]),
        dict(name="clipboard", parent="body", pivot=[4, 8, 0], cubes=[([4, 4, -1.5], [1, 5, 3], [40, 30])]),
        dict(name="badge", parent="body", pivot=[-2, 10, -2], cubes=[([-2.5, 9.5, -2.5], [1, 1, 1], [52, 30])]),
    ]),
)

# THE EARTH EATER — the god that eats planets, on a cosmological diet of one planet.
ENT["earth_eater"] = dict(
    bp=bp_entity("earth_eater", hp=1500, speed=0.06, box=(9.0, 12.0), dmg=45, kb=1.0, scale=6.0,
                 family=("ds_earth_eater", "monster"), flying=True),
    rp=client_entity("earth_eater", "#060508", "#3fd7c8", glow_mat="entity_emissive"),
    geo=geo_model("earth_eater", "v15", (96, 64), [
        dict(name="body", pivot=[0, 0, 0], cubes=[([-8, 0, -5], [16, 10, 10], [0, 0])]),
        dict(name="jaw_top", parent="body", pivot=[0, 6, -5], cubes=[([-7, 6, -6], [14, 3, 7], [0, 22])]),
        dict(name="jaw_bottom", parent="body", pivot=[0, 0, -5], cubes=[([-7, 0, -6], [14, 2, 7], [48, 22])]),
        dict(name="throat", parent="body", pivot=[0, 3, -4], cubes=[([-4, 2, -5], [8, 4, 2], [0, 36])]),
    ]),
)

# THE ROSE VARIANT OF MASSG — the pink apocalypse, same hunger in a blushing body:
# re-register the massg shell as massg_rose with its own texture + name.
ENT["massg_rose"] = dict(
    bp=bp_entity("massg_rose", hp=700, speed=0.4, box=(12, 16), dmg=22, kb=1.0,
                 family=("ds_massg", "monster"), flying=True),
    rp=client_entity("massg_rose", "#5c1430", "#ff6fc0", glow_mat="entity_emissive"),
    geo=None,  # reuses geometry.ds.massg; patch in client def below
)

# ------------------------------------------------------------ write files

def write_json(path: Path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)

for ident, entry in ENT.items():
    write_json(BP / "entities" / f"{ident}.json", entry["bp"])
    client = entry["rp"]
    if ident == "massg_rose":
        client["minecraft:client_entity"]["description"]["geometry"]["default"] = "geometry.ds.massg"
        client["minecraft:client_entity"]["description"]["textures"]["default"] = "textures/entity/massg_rose"
    write_json(RP / "entity" / f"{ident}.entity.json", client)
    if entry["geo"] is not None:
        write_json(RP / "models" / "entity" / f"{ident}.geo.json", entry["geo"])
    print(f"entity ds:{ident}")

# massg_rose texture = pink-shifted massg texture
from PIL import Image
src = RP / "textures" / "entity" / "massg.png"
if src.exists():
    im = Image.open(src).convert("RGBA")
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            # hue-swing violet -> rose: boost red, dip blue a tinge
            nr = min(255, int(r * 1.25 + 30))
            ng = int(g * 0.55)
            nb = min(255, int(b * 0.85))
            px[x, y] = (nr, ng, nb, a)
    dst = RP / "textures" / "entity" / "massg_rose.png"
    dst.parent.mkdir(parents=True, exist_ok=True)
    im.save(dst)
    print("texture entity/massg_rose.png")

# ------------------------------------------------------------ blocks

def block_def(name, sound="stone", destroy=3.0):
    return {
        "format_version": "1.21.80",
        "minecraft:block": {
            "description": {"identifier": f"ds:{name}"},
            "components": {
                "minecraft:geometry": "geometry.full_block",
                "minecraft:material_instances": {"*": {"texture": name}},
                "minecraft:destructible_by_mining": {"seconds_to_destroy": destroy},
                "minecraft:destructible_by_explosion": True,
                "minecraft:map_color": "#ff3fc8" if "glitch" in name else "#3a3a42",
                "step_on_sound": None,
            } | ({}),
        },
    }
for name, snd in (("glitch_block", "stone"), ("vhs_jukebox", "metal"), ("crate_block", "wood")):
    d = block_def(name, snd, 2.0)
    d["minecraft:block"]["components"].pop("step_on_sound", None)
    write_json(BP / "blocks" / f"{name}.json", d)
    print(f"block ds:{name}")

# RP block textures + terrain
tex_rg = "textures/block"
blocks_dir = RP / "textures" / "blocks"
blocks_dir.mkdir(parents=True, exist_ok=True)

# block atlas entries (terrain_texture.json)
tt_path = RP / "textures" / "terrain_texture.json"
terrain = json.load(open(tt_path)) if tt_path.exists() else {
    "num_mip_levels": 4, "padding": 8,
    "resource_pack_name": "DevouringStormsRP",
    "texture_name": "atlas.terrain",
    "texture_data": {},
}
for name in ("glitch_block", "vhs_jukebox", "crate_block"):
    terrain["texture_data"][name] = {"textures": f"textures/blocks/{name}"}
write_json(tt_path, terrain)
print("terrain_texture.json updated")

# item model: DS items get geo? Bedrock: items.json attachable items w/ texture
write_json(BP / "items" / "broken_record.json", {
    "format_version": "1.21.80",
    "minecraft:item": {
        "description": {"identifier": "ds:broken_record", "menu_category": {"category": "items"}},
        "components": {
            "minecraft:icon": {"textures": {"default": "broken_record"}},
            "minecraft:max_stack_size": 1,
        },
    },
})
write_json(BP / "items" / "rocket_key.json", {
    "format_version": "1.21.80",
    "minecraft:item": {
        "description": {"identifier": "ds:rocket_key", "menu_category": {"category": "items"}},
        "components": {
            "minecraft:icon": {"textures": {"default": "rocket_key"}},
            "minecraft:max_stack_size": 1,
            "minecraft:rarity": "rare",
        },
    },
})
print("items: broken_record, rocket_key")

it_path = RP / "textures" / "item_texture.json"
items_tex = json.load(open(it_path)) if it_path.exists() else {
    "resource_pack_name": "DevouringStormsRP",
    "texture_name": "atlas.items",
    "texture_data": {},
}
for name in ("broken_record", "rocket_key"):
    items_tex["texture_data"][name] = {"textures": f"textures/items/{name}"}
write_json(it_path, items_tex)
print("item_texture.json updated")

# ------------------------------------------------------------ sounds
snd_path = RP / "sounds" / "sound_definitions.json"
sounds = json.load(open(snd_path)) if snd_path.exists() else {"format_version": "1.14.0", "sound_definitions": {}}
for name in ("signal_tape", "eaoin", "countdown", "quarantine"):
    sounds["sound_definitions"][f"record.{name}"] = {
        "category": "records",
        "max_distance": 100.0,
        "sounds": [{"name": f"sounds/ds/{name}", "load_on_low_memory": True}],
    }
write_json(snd_path, sounds)
print("sound_definitions: 4 more records")

print("bedrock v1.5 mirror complete.")
