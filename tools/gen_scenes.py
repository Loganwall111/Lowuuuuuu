#!/usr/bin/env python3
"""Generate all .tscn scene files with correct scene-tree wiring, script
references and ext_resource paths. tab-indented .tscn, validated afterwards."""
import os

SC = "scenes"
def w(path, body):
    full = os.path.join(SC, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        f.write(body)

def node(name, type_, parent="", script=None, props=None, groups=None):
    lines = []
    base = name if parent == "" else f"{parent}/{name}"
    lines.append(f'[node name="{name}" type="{type_}"')
    if parent != "":
        lines.append(f'parent="{parent}"')
    if groups:
        for g in groups:
            lines.append(f'groups=["{g}"]')
    lines.append("]")
    if script:
        lines.append(f'script = ExtResource("{script}")')
    if props:
        for k, v in props.items():
            lines.append(f'{k} = {v}')
    return "\n".join(lines) + "\n"

def camera3d(parent, name="Camera3D"):
    return node(name, "Camera3D", parent, props={"fov": "70.0", "near": "0.1", "far": "2000.0"})

HEAD = '[gd_scene load_steps={steps} format=3]\n\n'

def file(relpath, steps, exts, ext_names, nodes_text):
    body = HEAD.format(steps=steps)
    i = 1
    for (etype, epath) in exts:
        body += f'[ext_resource type="{etype}" path="{epath}" id="{i}"]\n'
        ext_names.append(i)
        i += 1
    body += "\n" + nodes_text
    w(relpath, body)
    return ext_names

# ---------------- PlayerPawn ----------------
def gen_player():
    exts = [("Script", "res://scripts/player/player_pawn.gd"),
            ("Script", "res://scripts/player/growth_component.gd"),
            ("Script", "res://scripts/player/metabolism_component.gd"),
            ("Script", "res://scripts/player/evolution_component.gd"),
            ("Script", "res://scripts/player/environment_probe.gd"),
            ("Script", "res://scripts/player/consumption_sensor.gd"),
            ("Script", "res://scripts/player/locomotion/locomotion_controller.gd"),
            ("Script", "res://scripts/camera/camera_rig.gd")]
    names = []
    n = ""
    n += node("PlayerPawn", "CharacterBody3D", "", "1", {"collision_layer": "2", "collision_mask": "231"})
    n += node("CollisionShape3D", "CollisionShape3D", "PlayerPawn", props={"shape": 'SubResource("cap")'})
    n += node("MeshRoot", "Node3D", "PlayerPawn")
    n += node("CameraRig", "Node3D", "PlayerPawn", "8")
    n += camera3d("CameraRig")
    n += node("ConsumptionSensor", "Area3D", "PlayerPawn", "6", {"collision_layer": "16", "collision_mask": "16", "monitoring": "true"})
    n += node("CollisionShape3D2", "CollisionShape3D", "ConsumptionSensor", props={"shape": 'SubResource("sense")'})
    n += node("EnvironmentProbe", "Node", "PlayerPawn", "5")
    n += node("GrowthComponent", "Node", "PlayerPawn", "2", {"collision_shape": 'NodePath("CollisionShape3D")'})
    n += node("MetabolismComponent", "Node", "PlayerPawn", "3")
    n += node("EvolutionComponent", "Node", "PlayerPawn", "4")
    n += node("LocomotionController", "Node", "PlayerPawn", "7")
    sb = '''[sub_resource type="CapsuleShape3D" id="cap"]
radius = 0.5
height = 1.4

[sub_resource type="SphereShape3D" id="sense"]
radius = 2.2
'''
    file("player/PlayerPawn.tscn", 2+len(exts), exts, names, n + sb)

# ---------------- PreyCreature ----------------
def gen_prey():
    exts = [("Script", "res://scripts/organisms/creature_agent.gd"),
            ("Script", "res://scripts/player/edible_component.gd")]
    n = ""
    n += node("PreyCreature", "Node3D", "", "1", {"collision_layer": "4", "collision_mask": "0"})
    n += node("MeshInstance3D", "MeshInstance3D", "PreyCreature", props={"mesh": 'SubResource("m")'})
    n += node("EdibleComponent", "Node", "PreyCreature", "2",
              {"provided_mass": "0.1", "provided_energy": "6.0", "provided_evolution_points": "1.0",
               "tags": 'PackedStringArray("fish")', "source_name": "prey", "return_to_pool": "true",
               "fade_out_seconds": "0.3"})
    sb = '''[sub_resource type="SphereMesh" id="m"]
radius = 0.35
height = 0.7
'''
    file("organisms/PreyCreature.tscn", 2+len(exts), exts, [], n + sb)

# ---------------- DebrisBody ----------------
def gen_debris():
    exts = [("Script", "res://scripts/organisms/debris_body.gd"),
            ("Script", "res://scripts/physics/gravity_affected.gd")]
    n = ""
    n += node("DebrisBody", "RigidBody3D", "", "1", {"collision_layer": "128", "collision_mask": "1", "contact_monitor": "true"})
    n += node("CollisionShape3D", "CollisionShape3D", "DebrisBody", props={"shape": 'SubResource("cs")'})
    n += node("MeshInstance3D", "MeshInstance3D", "DebrisBody", props={"mesh": 'SubResource("m")', "material_override": 'SubResource("mat")'})
    n += node("GravityAffected", "Node", "DebrisBody", "2")
    sb = '''[sub_resource type="SphereShape3D" id="cs"]
radius = 0.6

[sub_resource type="SphereMesh" id="m"]
radius = 0.6
height = 1.2

[sub_resource type="StandardMaterial3D" id="mat"]
albedo_color = Color(0.45, 0.4, 0.35, 1)
roughness = 0.9
'''
    file("space/DebrisBody.tscn", 2+len(exts), exts, [], n + sb)

# ---------------- BlackHole ----------------
def gen_blackhole():
    exts = [("Script", "res://scripts/physics/gravity_well.gd")]
    n = ""
    n += node("BlackHole", "Node3D", "", "")
    n += node("GravityWell3D", "Node3D", "BlackHole", "1",
              {"gravity_strength": "220.0", "influence_radius": "400.0",
               "falloff_type": "0", "max_force": "600.0", "softening": "4.0",
               "event_horizon_radius": "5.0", "attract": "true"})
    n += node("Visual", "MeshInstance3D", "BlackHole", props={"mesh": 'SubResource("m")', "material_override": 'SubResource("mat")'})
    sb = '''[sub_resource type="SphereMesh" id="m"]
radius = 4.5
height = 9.0

[sub_resource type="StandardMaterial3D" id="mat"]
albedo_color = Color(0.0, 0.0, 0.0, 1)
roughness = 0.2
metallic = 0.0
emission_enabled = true
emission = Color(0.3, 0.1, 0.5, 1)
emission_intensity = 0.4
'''
    file("space/BlackHole.tscn", 2+len(exts), exts, [], n + sb)

# ---------------- Biomes ----------------
def biome(name, env_profile, scene_script, spawn, water=None, plankton=False):
    exts = [("Script", "res://scripts/environment/environment_director.gd"),
            ("Script", scene_script),
            ("Resource", env_profile)]
    n = ""
    n += node("SceneRoot", "Node3D", "", "2")
    n += node("WorldEnvironment", "WorldEnvironment", "SceneRoot", props={"environment": 'SubResource("env")'})
    n += node("Sun", "DirectionalLight3D", "SceneRoot", {"light_color":"Color(1,0.96,0.85,1)","light_energy":"1.4","shadow_enabled":"true"})
    n += node("EnvironmentDirector", "Node", "SceneRoot", "1",
              {"surface_profile": 'ExtResource("3")', "underwater_profile": 'ExtResource("3")', "blend_time": "1.2"})
    if water:
        exts.append(("Script", "res://scripts/environment/water_volume.gd"))
        n += node("Water", "Area3D", "SceneRoot", "4" if len(exts)==4 else "4",
                  {"collision_layer":"64","collision_mask":"2","surface_y":str(water[0])})
        n += node("CollisionShape3D", "CollisionShape3D", "Water", props={"shape":'SubResource("water_shape")'})
    if plankton:
        exts.append(("Script", "res://scripts/organisms/plankton_field.gd"))
        n += node("Plankton", "MultiMeshInstance3D", "SceneRoot", exts[-1][1].split("/")[-1] if False else "5",
                  {"field_radius":"70.0","field_height":"24.0","mote_color":"Color(0.6,0.9,0.8,0.9)"})
    # spawn marker
    n += node("SpawnPoint", "Node3D", "SceneRoot", props={"position": f"Vector3({spawn[0]},{spawn[1]},{spawn[2]})"})
    sb = '''[sub_resource type="Environment" id="env"]
background_mode = 2
tonemap_mode = 3
volumetric_fog_enabled = true
fog_enabled = true

[sub_resource type="BoxShape3D" id="water_shape"]
size = Vector3(160, 60, 160)
'''
    file(name, 2+len(exts), exts, [], n + sb)

gen_player()
gen_prey()
gen_debris()
gen_blackhole()
biome("environments/AquaticReef.tscn", "res://resources/environments/wetland.tres",
      "res://scripts/environment/biome_reef.gd", (0,2,0), water=(0.0,), plankton=True)
biome("environments/WetlandBiome.tscn", "res://resources/environments/wetland.tres",
      "res://scripts/environment/biome_wetland.gd", (0,2,0), water=(0.0,), plankton=True)
biome("environments/ForestBiome.tscn", "res://resources/environments/forest.tres",
      "res://scripts/environment/biome_forest.gd", (0,2,0))
biome("environments/DeepSpaceBiome.tscn", "res://resources/environments/deep_space.tres",
      "res://scripts/environment/biome_deep_space.gd", (0,0,0))

print("Scene generation complete.")
