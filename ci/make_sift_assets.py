#!/usr/bin/env python3
import json, os
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "jar-overrides", "assets", "mcsm_sift")
DATA = os.path.join(ROOT, "jar-overrides", "data", "mcsm_sift")

BLOCKS = [
"blue_grass_block","pink_grass_block","red_grass_block","red_rock","black_water","green_acid",
"purple_tree_leaves","purple_tree_log","fluor_plant","fungus_tree","red_vines","blue_bun","rainbow_water",
"cyan_moss","void_lily","gel_crystal","sponge_bloom","rift_glass","prismatic_stone","luminous_vine",
"fabric_shard","echo_soil","starlit_grass","void_blossom","gel_honey","crystalline_sponge","rift_bloom",
"displacement_stone","iridescent_leaves","iridescent_log","void_fern","glowing_mushroom","fabric_roots",
"cosmic_sand","star_dust","void_crystal_cluster","gel_lantern","rift_vein","prismatic_vine","void_berry_bush",
"echo_crystal","fabric_bloom","cosmic_grass"
]

def write(path,obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path,"w",encoding="utf-8") as fh:
        json.dump(obj,fh,indent=2,sort_keys=True)
        fh.write("\n")

for name in BLOCKS:
    # blockstate
    write(os.path.join(ASSETS,"blockstates",name+".json"), {"variants":{"": {"model": f"mcsm_sift:block/{name}"}}})
    # model block
    write(os.path.join(ASSETS,"models","block",name+".json"), {"parent":"minecraft:block/cube_all","textures":{"all": f"mcsm_sift:block/{name}"}})
    # item model
    write(os.path.join(ASSETS,"models","item",name+".json"), {"parent": f"mcsm_sift:block/{name}"})
    # item definition
    write(os.path.join(ASSETS,"items",name+".json"), {"model":{"type":"minecraft:model","model": f"mcsm_sift:block/{name}"}})
    # loot table self drop
    write(os.path.join(DATA,"loot_table","blocks",name+".json"), {"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"mcsm_sift:{name}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})

print(f"sift assets: {len(BLOCKS)} blocks")
