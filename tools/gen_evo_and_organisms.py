#!/usr/bin/env python3
"""Generate EvolutionTreeData .tres (with inline stage sub-resources) and the
three OrganismData .tres files. References external MovementProfile / Ability /
Diet / EnvironmentProfile resources that already exist in resources/. The
starting_environment / player_scene point at scenes that will be generated next."""
import os

RES = "resources"
def w(path, body):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        f.write(body)

def uid(n):
    h = abs(hash("evo_"+n)) % 0xFFFFFFFF
    return f"uid_{h:08x}"

def psa(*items):
    return "PackedStringArray(" + ", ".join(f'"{i}"' for i in items) + ")"

# stage: id, name, order, lore, bscale, ecap, metab, sense, moves[], ability_paths[], next[]
def tree_tres(name, stages, ext_profiles, ext_abs):
    # ext_profiles/ext_abs: dict mapping id->path for sub_resource refs
    subs = []
    sref = {}
    idx = 0
    for s in stages:
        sid = f"stage_{idx}"
        sref[s[0]] = sid
        prof_path = ext_profiles.get(s[0])
        lines = [f'''[sub_resource type="EvolutionStageData" id="{sid}"]
stage_id = &"{s[0]}"
display_name = "{s[1]}"
display_order = {s[2]}
lore = "{s[3]}"
biological_complexity = {s[9] if len(s) > 9 else 1.0:g}
required_mass = {s[4]:g}
required_evolution_points = {s[5]:g}
required_age_seconds = {s[6]:g}
body_scale_multiplier = {s[7]:g}
energy_capacity = {s[8]:g}
metabolism_rate = 1.0
sense_radius = 6.0
base_movement_profile = ExtResource("{prof_path}")
unlocked_abilities = {psa(*(ext_abs.get(a, "") for a in s[10]))}
enabled_movement_states = {s[11]}
next_stage_ids = {psa(*s[12])}
cinematic_title = "{s[1]}"
cinematic_subtitle = "{s[3]}"
''']
        # filter empty ability paths
        lines[0] = lines[0].replace('unlocked_abilities = PackedStringArray()\n',
                                    'unlocked_abilities = PackedStringArray()\n')
        subs.append(lines[0])
        idx += 1
    # header
    n_ext = len(set(ext_profiles.values())) + len(set(p for p in ext_abs.values()))
    body = f'[gd_resource type="EvolutionTreeData" script_class="EvolutionTreeData" load_steps={2+len(subs)+n_ext} format=3 uid="{uid(name)}"]\n\n'
    seen = {}
    order = []
    for p in list(ext_profiles.values()) + list(ext_abs.values()):
        if p and p not in seen:
            seen[p] = f"1_{len(order)+1}"
            order.append(p)
    for i, p in enumerate(order):
        body += f'[ext_resource type="Resource" path="{p}" id="{i+1}"]\n'
    body += "\n" + "".join(subs)
    body += f'''
[resource]
tree_name = "{name}"
root_stage_id = &"{stages[0][0]}"
stages = ['''
    body += ", ".join(f'SubResource("{sref[s[0]]}")' for s in stages)
    body += "]\n"
    w(f"evolution/{name}.tres", body)

# Organism .tres
def org_tres(name, sp_id, disp, cat, summary, order, env_scene, player_scene,
             start_mass, start_energy, growth, base_move, body_res, tree_res,
             diet_res, env_res, start_abs):
    body = f'''[gd_resource type="OrganismData" script_class="OrganismData" load_steps=8 format=3 uid="{uid("org_"+name)}"]

[ext_resource type="PackedScene" path="{env_scene}" id="1"]
[ext_resource type="PackedScene" path="{player_scene}" id="2"]
[ext_resource type="CreatureBodyRecipe" path="{body_res}" id="3"]
[ext_resource type="EvolutionTreeData" path="{tree_res}" id="4"]
[ext_resource type="DietProfile" path="{diet_res}" id="5"]
[ext_resource type="EnvironmentProfile" path="{env_res}" id="6"]

[resource]
species_id = &"{sp_id}"
display_name = "{disp}"
category = &"{cat}"
summary = "{summary}"
menu_order = {order}
playable = true
accent_color = Color(0.4, 0.85, 0.7, 1)
starting_environment = ExtResource("1")
player_scene = ExtResource("2")
starting_mass = {start_mass:g}
starting_energy = {start_energy:g}
starting_health = 100.0
growth_rate = {growth:g}
base_movement_type = {base_move}
starting_abilities = {psa(*start_abs)}
body_recipe = ExtResource("3")
evolution_tree = ExtResource("4")
diet = ExtResource("5")
environment_profile = ExtResource("6")
'''
    w(f"organisms/{name}.tres", body)

P = {
 "mosquito_larva": "res://resources/movement/swim.tres",
 "mosquito_pupa": "res://resources/movement/float.tres",
 "mosquito_adult": "res://resources/movement/fly.tres",
 "fish_larva": "res://resources/movement/swim.tres",
 "fish_juvenile": "res://resources/movement/swim.tres",
 "fish_predator": "res://resources/movement/swim.tres",
 "fish_reef": "res://resources/movement/swim.tres",
 "primate_infant": "res://resources/movement/crawl.tres",
 "primate_juvenile": "res://resources/movement/walk.tres",
 "primate_ape": "res://resources/movement/walk.tres",
 "primate_tool": "res://resources/movement/run.tres",
 "primate_hunter": "res://resources/movement/run.tres",
}
A = {
 "swim_efficient": "res://resources/abilities/swim_efficient.tres",
 "crawl": "res://resources/abilities/crawl.tres",
 "flight": "res://resources/abilities/flight.tres",
 "tool_use": "res://resources/abilities/tool_use.tres",
 "predator": "res://resources/abilities/predator.tres",
}

# Mosquito
tree_tres("mosquito_tree", [
 ("mosquito_larva","Larval Wriggler",0,"A blind wriggler in the shallows.",0.5,0.0,0.0,0.5,100.0,1.0,[], [0], ["mosquito_pupa"]),
 ("mosquito_pupa","Pupa",1,"Suspended, transforming.",3.0,5.0,20.0,1.0,140.0,1.3,[], [1], ["mosquito_adult"]),
 ("mosquito_adult","Adult Mosquito",2,"Wings unlock the air.",12.0,20.0,60.0,1.6,180.0,1.8,["swim_efficient","flight"], [0,5], []),
], P, A)

# Fish
tree_tres("fish_tree", [
 ("fish_larva","Larva",0,"A tiny planktivore.",0.5,0.0,0.0,0.4,100.0,1.0,[], [0], ["fish_juvenile"]),
 ("fish_juvenile","Juvenile",1,"Faster, bolder.",4.0,6.0,15.0,0.9,160.0,1.2,["swim_efficient"], [0], ["fish_predator","fish_reef"]),
 ("fish_predator","Apex Predator",2,"Hunter of the reef.",20.0,40.0,120.0,1.8,260.0,1.6,["predator"], [0], []),
 ("fish_reef","Reef Dweller",2,"Master of the coral maze.",20.0,40.0,120.0,1.5,220.0,1.4,[], [0], []),
], P, A)

# Primate
tree_tres("primate_tree", [
 ("primate_infant","Infant",0,"Helpless at birth.",0.8,0.0,0.0,0.5,120.0,1.0,[], [2], ["primate_juvenile"]),
 ("primate_juvenile","Juvenile",1,"Learning to walk and climb.",6.0,10.0,30.0,1.0,180.0,1.2,["crawl"], [2,6], ["primate_ape"]),
 ("primate_ape","Ape",2,"Tool-using forager.",30.0,50.0,180.0,1.6,240.0,1.5,["tool_use"], [3,4], ["primate_tool","primate_hunter"]),
 ("primate_tool","Tool User",3,"Civilisation begins.",120.0,120.0,600.0,2.4,320.0,1.8,["tool_use"], [3,4], []),
 ("primate_hunter","Hunter",3,"Apex terrestrial predator.",120.0,120.0,600.0,2.2,320.0,1.7,["predator","tool_use"], [3,4], []),
], P, A)

org_tres("mosquito","mosquito","Mosquito","insect",
    "Begin as a wriggler; metamorphose into a flier.", 300,
    "res://scenes/environments/AquaticReef.tscn", "res://scenes/player/PlayerPawn.tscn",
    0.5, 100.0, 1.0, 0,
    "res://resources/bodies/mosquito.tres", "res://resources/evolution/mosquito_tree.tres",
    "res://resources/diets/filter_feeder.tres", "res://resources/environments/wetland.tres", [])

org_tres("fish","fish","Fish","marine",
    "A reef fish: predation, exploration, branching fates.", 200,
    "res://scenes/environments/AquaticReef.tscn", "res://scenes/player/PlayerPawn.tscn",
    0.6, 110.0, 1.0, 0,
    "res://resources/bodies/fish.tres", "res://resources/evolution/fish_tree.tres",
    "res://resources/diets/predator.tres", "res://resources/environments/deep_ocean.tres", ["swim_efficient"])

org_tres("primate","primate","Primate","mammal",
    "From infant to intelligence, social evolution and technology.", 100,
    "res://scenes/environments/ForestBiome.tscn", "res://scenes/player/PlayerPawn.tscn",
    0.8, 120.0, 1.0, 2,
    "res://resources/bodies/primate.tres", "res://resources/evolution/primate_tree.tres",
    "res://resources/diets/herbivore.tres", "res://resources/environments/forest.tres", [])

print("Evo + organism generation complete.")
