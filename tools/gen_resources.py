#!/usr/bin/env python3
"""Generates all data .tres resources to match the exported properties defined
in scripts/data/*.gd. Keeps content authoring declarative and editor-free."""
import os

RES = "resources"
ORG = "res://"

def w(path, body):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        f.write(body)

def color(r, g, b, a=1.0):
    return f"Color({r:g}, {g:g}, {b:g}, {a:g})"

def v3(x, y, z):
    return f"Vector3({x:g}, {y:g}, {z:g})"

def psa(*items):
    return "PackedStringArray(" + ", ".join(f'"{i}"' for i in items) + ")"

def env_tres(name, **kw):
    body = f'''[gd_resource type="EnvironmentProfile" script_class="EnvironmentProfile" load_steps=2 format=3 uid="{uid_for(name)}"]

[resource]
sky_top_color = {kw.get("sky_top", color(0.05,0.18,0.32))}
sky_horizon_color = {kw.get("sky_horizon", color(0.4,0.6,0.7))}
sun_color = {kw.get("sun", color(1.0,0.96,0.85))}
sun_energy = {kw.get("sun_e", 1.4):g}
sun_direction = {kw.get("sun_dir", v3(-0.5,-0.85,0.25))}
ambient_color = {kw.get("amb", color(0.3,0.45,0.55))}
ambient_energy = {kw.get("amb_e", 0.5):g}
fog_enabled = {str(kw.get("fog", True)).lower()}
fog_color = {kw.get("fog_color", color(0.1,0.3,0.38))}
fog_density = {kw.get("fog_d", 0.025):g}
fog_light_color = {kw.get("fog_light", color(0.7,0.85,0.9))}
height_fog_start = {kw.get("hf_start", 0.0):g}
height_fog_depth = {kw.get("hf_depth", 50.0):g}
volumetric_fog = {str(kw.get("vfog", True)).lower()}
volumetric_fog_density = {kw.get("vfog_d", 0.06):g}
volumetric_fog_albedo = {kw.get("vfog_alb", color(0.7,0.82,0.88))}
tone_exposure = {kw.get("expo", 1.0):g}
tone_white = {kw.get("white", 1.0):g}
ssao = {kw.get("ssao", 1.0):g}
saturation = {kw.get("sat", 1.1):g}
contrast = {kw.get("con", 1.06):g}
is_underwater = {str(kw.get("underwater", False)).lower()}
water_tint = {kw.get("tint", color(0.06,0.27,0.34))}
fog_density_underwater_scale = {kw.get("uw_scale", 4.0):g}
'''
    w(f"environments/{name}.tres", body)

UIDS = {}
def uid_for(name):
    if name not in UIDS:
        # deterministic pseudo-uid
        h = abs(hash(name)) % 0xFFFFFFFF
        UIDS[name] = f"uid_{h:08x}"
    return UIDS[name]

def ability_tres(name, ability_id, display, desc, speed=1.0, metab=1.0, sense=1.0, ep=1.0, moves=None, unlock_abs=None):
    moves = moves or []
    unlock_abs = unlock_abs or []
    body = f'''[gd_resource type="AbilityData" script_class="AbilityData" load_steps=2 format=3 uid="{uid_for("ab_"+name)}"]

[resource]
ability_id = &"{ability_id}"
display_name = "{display}"
description = "{desc}"
category = &"general"
tags = {psa()}
move_speed_multiplier = {speed:g}
metabolism_multiplier = {metab:g}
sense_radius_multiplier = {sense:g}
damage_multiplier = {ep:g}
evolution_point_multiplier = {ep:g}
unlocks_movement_states = {moves}
unlocks_abilities = {psa(*unlock_abs)}
'''
    w(f"abilities/{name}.tres", body)

def diet_tres(name, edible, forbidden, minr=0.0, maxr=0.6, minmass=0.0005, mult=1.0, auto=False, req=""):
    body = f'''[gd_resource type="DietProfile" script_class="DietProfile" load_steps=2 format=3 uid="{uid_for("diet_"+name)}"]

[resource]
edible_tags = {psa(*edible)}
forbidden_tags = {psa(*forbidden)}
min_mass_ratio = {minr:g}
max_mass_ratio = {maxr:g}
min_mass_for_gain = {minmass:g}
base_energy_multiplier = {mult:g}
consume_radius_padding = {0.25:g}
auto_consume = {str(auto).lower()}
required_ability = &"{req}"
'''
    w(f"diets/{name}.tres", body)

def part(t, size, pos, rot, role=0, eye=False):
    return (f'SubResource("part_{t}_{role}_{"eye" if eye else "no"}")')

def recipe_tres(name, silhouette, parts, default, accent, eye, rough=0.55, metal=0.0, wet=0.4, sss=0.3):
    subs = []
    idx = 0
    for (t, size, pos, rot, role, is_eye) in parts:
        sid = f"part_{idx}"
        subs.append(f'''[sub_resource type="CreaturePartSpec" id="{sid}"]
shape = {t}
size = {size}
position = {pos}
rotation_deg = {rot}
role = {role}
is_eye = {str(is_eye).lower()}
''')
        idx += 1
    body = f'''[gd_resource type="CreatureBodyRecipe" script_class="CreatureBodyRecipe" load_steps={2+len(subs)} format=3 uid="{uid_for("body_"+name)}"]

'''
    body += "".join(subs)
    body += f'''
[resource]
silhouette = {silhouette}
parts = ['''
    parts_refs = ", ".join('SubResource("part_%d")' % i for i in range(len(parts)))
    body += parts_refs
    body += f''']
default_color = {default}
accent_color = {accent}
eye_color = {eye}
roughness = {rough:g}
metallic = {metal:g}
wetness = {wet:g}
sub_surface = {sss:g}
'''
    w(f"bodies/{name}.tres", body)

def move_tres(name, **kw):
    body = f'''[gd_resource type="MovementProfile" script_class="MovementProfile" load_steps=2 format=3 uid="{uid_for("move_"+name)}"]

[resource]
max_speed = {kw.get("max_speed",8.0):g}
sprint_multiplier = {kw.get("sprint",1.8):g}
vertical_speed_scale = {kw.get("vscale",1.0):g}
acceleration = {kw.get("accel",30.0):g}
deceleration = {kw.get("decel",24.0):g}
drag = {kw.get("drag",1.2):g}
turn_speed = {kw.get("turn",3.0):g}
pitch_speed = {kw.get("pitch",2.2):g}
align_speed = {kw.get("align",6.0):g}
medium_density = {kw.get("medium",1.0):g}
medium_resistance_exponent = {kw.get("mre",1.0):g}
buoyancy = {kw.get("buoy",0.0):g}
gravity_scale = {kw.get("grav",1.0):g}
bank_angle = {kw.get("bank",0.4):g}
undulation = {kw.get("und",0.0):g}
mass_speed_exponent = {kw.get("mse",0.18):g}
fov_offset = {kw.get("fov",0.0):g}
bob_amplitude = {kw.get("bob",0.06):g}
bob_frequency = {kw.get("bobf",2.0):g}
step_height = {kw.get("step",0.35):g}
max_slope = {kw.get("slope",0.78539818):g}
'''
    w(f"movement/{name}.tres", body)

# ---- Environment profiles ----
env_tres("deep_ocean", sky_top=color(0.01,0.06,0.12), sky_horizon=color(0.03,0.15,0.22),
         sun=color(0.6,0.85,0.95), sun_e=0.7, amb=color(0.1,0.25,0.35), amb_e=0.4,
         fog_color=color(0.02,0.12,0.18), fog_d=0.06, vfog_d=0.12, expo=0.95, ssao=0.8,
         sat=1.05, con=1.04, underwater=True, tint=color(0.04,0.2,0.28), uw_scale=5.0)
env_tres("wetland", sky_top=color(0.2,0.4,0.5), sky_horizon=color(0.6,0.7,0.6),
         sun=color(1.0,0.95,0.8), sun_e=1.2, amb=color(0.4,0.5,0.45), amb_e=0.5,
         fog_color=color(0.5,0.6,0.55), fog_d=0.02, vfog_d=0.05, expo=1.0, ssao=1.0,
         sat=1.1, con=1.06, underwater=False, tint=color(0.1,0.3,0.3))
env_tres("forest", sky_top=color(0.15,0.35,0.6), sky_horizon=color(0.7,0.75,0.65),
         sun=color(1.0,0.95,0.85), sun_e=1.5, amb=color(0.35,0.4,0.35), amb_e=0.5,
         fog_color=color(0.55,0.6,0.55), fog_d=0.012, vfog_d=0.03, expo=1.05, ssao=1.1,
         sat=1.12, con=1.07, underwater=False, tint=color(0.1,0.3,0.2))
env_tres("deep_space", sky_top=color(0.0,0.0,0.02), sky_horizon=color(0.02,0.02,0.06),
         sun=color(0.9,0.95,1.0), sun_e=2.0, amb=color(0.1,0.1,0.15), amb_e=0.25,
         fog_color=color(0.0,0.0,0.02), fog_d=0.0, vfog_d=0.0, expo=1.1, ssao=0.0,
         sat=1.0, con=1.1, underwater=False, tint=color(0.0,0.0,0.02))

# ---- Abilities ----
ability_tres("swim_efficient", "swim_efficient", "Efficient Swimming",
    "Streamlined body cuts drag in water.", speed=1.15, metab=0.92, sense=1.05, moves=[0])
ability_tres("crawl", "crawl", "Chitinous Limbs",
    "Hardened limbs allow crawling on substrate.", speed=1.0, moves=[2])
ability_tres("flight", "flight", "Powered Flight",
    "Wings unlock true aerial locomotion.", speed=1.2, sense=1.1, moves=[5])
ability_tres("tool_use", "tool_use", "Tool Use",
    "Manipulators enable tool use and intelligence.", sense=1.2, ep=1.3)
ability_tres("predator", "predator", "Predatory Instinct",
    "Heightened senses and aggression.", speed=1.1, sense=1.3, ep=1.2)

# ---- Diets ----
diet_tres("filter_feeder", ["plankton","plant","carrion"], ["toxic","rock"], maxr=0.5, auto=True)
diet_tres("predator", ["plankton","plant","carrion","fish"], ["toxic","rock"], maxr=0.6)
diet_tres("herbivore", ["plant","plankton"], ["toxic","rock","fish"], maxr=0.7)

# ---- Body recipes ----
recipe_tres("mosquito",
    silhouette=1,  # FISH-like elongated for larva; adult uses wings
    parts=[
        (1, v3(0.6,1.4,0.6), v3(0,0,0), v3(0,0,0), 0, False),      # capsule body
        (3, v3(1.2,0.1,1.2), v3(0,0.4,-0.6), v3(0,0,0), 1, False), # wing-ish cylinder (accent)
        (0, v3(0.25,0.25,0.25), v3(0.15,0.3,0.7), v3(0,0,0), 2, True),  # eye
        (0, v3(0.25,0.25,0.25), v3(-0.15,0.3,0.7), v3(0,0,0), 2, True), # eye
    ],
    default=color(0.3,0.35,0.4), accent=color(0.8,0.85,0.9), eye=color(0.9,0.95,1.0),
    rough=0.4, metal=0.0, wet=0.6, sss=0.2)
recipe_tres("fish",
    silhouette=1,
    parts=[
        (1, v3(0.8,1.8,0.8), v3(0,0,0), v3(0,0,0), 0, False),       # body capsule
        (2, v3(0.1,0.9,0.6), v3(0,0,0.9), v3(0,0,0), 1, False),     # tail box
        (0, v3(0.2,0.2,0.2), v3(0.2,0.2,-0.6), v3(0,0,0), 2, True), # eye
        (0, v3(0.2,0.2,0.2), v3(-0.2,0.2,-0.6), v3(0,0,0), 2, True),# eye
    ],
    default=color(0.2,0.5,0.6), accent=color(0.9,0.7,0.3), eye=color(0.95,0.98,1.0),
    rough=0.3, metal=0.0, wet=0.8, sss=0.35)
recipe_tres("primate",
    silhouette=2,  # QUADRUPED-ish (we build via parts)
    parts=[
        (1, v3(0.5,1.0,0.5), v3(0,0.8,0), v3(0,0,0), 0, False),     # torso capsule
        (0, v3(0.35,0.35,0.35), v3(0,1.4,-0.1), v3(0,0,0), 0, False),# head sphere
        (3, v3(0.15,0.9,0.15), v3(0.2,0.4,0), v3(0,0,0), 0, False), # limb cylinder
        (3, v3(0.15,0.9,0.15), v3(-0.2,0.4,0), v3(0,0,0), 0, False),# limb
        (3, v3(0.15,0.9,0.15), v3(0.2,0.4,0.3), v3(0,0,0), 0, False),# limb
        (3, v3(0.15,0.9,0.15), v3(-0.2,0.4,0.3), v3(0,0,0), 0, False),# limb
        (0, v3(0.12,0.12,0.12), v3(0.1,1.5,-0.1), v3(0,0,0), 2, True),# eye
        (0, v3(0.12,0.12,0.12), v3(-0.1,1.5,-0.1), v3(0,0,0), 2, True),# eye
    ],
    default=color(0.55,0.45,0.35), accent=color(0.35,0.28,0.22), eye=color(0.9,0.95,1.0),
    rough=0.7, metal=0.0, wet=0.2, sss=0.4)

# ---- Movement profiles ----
move_tres("swim", max_speed=7.0, accel=26.0, drag=1.1, buoyancy=1.02, medium=1.2, bank=0.5, und=0.25, grab=1.0)
move_tres("float", max_speed=3.0, accel=14.0, drag=1.6, buoyancy=1.08, medium=1.2, bank=0.2)
move_tres("crawl", max_speed=2.2, accel=18.0, decel=22.0, drag=3.0, buoyancy=1.0, medium=1.0, bank=0.15, step=0.25)
move_tres("walk", max_speed=5.0, accel=28.0, decel=26.0, drag=1.5, buoyancy=1.0, medium=1.0, bank=0.2, step=0.4)
move_tres("run", max_speed=9.5, accel=34.0, decel=20.0, drag=1.2, buoyancy=1.0, medium=1.0, sprint=1.9, bank=0.35, step=0.5)
move_tres("fly", max_speed=12.0, accel=32.0, drag=0.6, buoyancy=1.0, medium=0.35, bank=0.8, fov=4.0)
move_tres("space", max_speed=120.0, accel=40.0, drag=0.05, buoyancy=1.0, medium=0.0, bank=0.4, fov=8.0)

print("Resource generation complete.")
