class_name BiomeReef
extends Node3D
## BiomeReef — a procedural aquatic reef. Shows the "no imported assets needed"
## path: a FastNoiseLite seabed, MultiMesh coral/rock, a PlanktonField for
## thousands of free food motes, a WaterVolume for submersion, ambient fish via
## AgentSwarmManager, and the player pawn spawned at the SpawnPoint.
##
## ATTACH THIS SCRIPT TO:
##   AquaticReef (Node3D)   <-- here
##   ├── WorldEnvironment / Sun / EnvironmentDirector / Water / Plankton /
##   │   SpawnPoint  (created by AquaticReef.tscn)
##
## DEPENDENCIES: TerrainMeshBuilder, ScatterManager, WaterVolume, PlanktonField,
## EnvironmentDirector, GameDirector, AgentSwarmManager, CreatureAgent.

@export_group("Population")
@export var fish_count: int = 60
@export var coral_count: int = 220
@export var rock_count: int = 80

@export_group("References")
@export var player_scene: PackedScene
@export var fish_scene: PackedScene
@export var coral_mesh: Mesh
@export var rock_mesh: Mesh

var _seabed: MeshInstance3D


func _ready() -> void:
	_build_terrain()
	_scatter_static()
	_spawn_player()
	_spawn_creatures()
	_apply_audio()
	EventBus.biome_entered.emit(&"reef")


func _build_terrain() -> void:
	var mesh := TerrainMeshBuilder.build(180.0, 180.0, 14.0, 7, 0.03,
		Color(0.05, 0.12, 0.16), Color(0.2, 0.35, 0.4))
	_seabed = MeshInstance3D.new()
	_seabed.name = "Seabed"
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.08, 0.18, 0.22)
	mat.roughness = 0.95
	_seabed.material_override = mat
	_seabed.mesh = mesh
	add_child(_seabed)


func _scatter_static() -> void:
	var coral_mat := StandardMaterial3D.new()
	coral_mat.albedo_color = Color(0.9, 0.5, 0.4)
	coral_mat.emission_enabled = true
	coral_mat.emission = Color(0.4, 0.1, 0.2)
	coral_mat.emission_intensity = 0.3
	var rock_mat := StandardMaterial3D.new()
	rock_mat.albedo_color = Color(0.25, 0.3, 0.34)
	rock_mat.roughness = 1.0
	var area := AABB(Vector3(-80, -6, -80), Vector3(160, 0, 160))
	if coral_mesh != null:
		add_child(ScatterManager.scatter_mesh(coral_mesh, coral_mat, coral_count, area, 11, 0.6, 2.2))
	if rock_mesh != null:
		add_child(ScatterManager.scatter_mesh(rock_mesh, rock_mat, rock_count, area, 23, 0.5, 2.0))


func _spawn_player() -> void:
	if player_scene == null:
		player_scene = load("res://scenes/player/PlayerPawn.tscn")
	if player_scene == null or GameDirector.organism_for_new_run == null:
		return
	var pawn: Node3D = player_scene.instantiate()
	add_child(pawn)
	var sp := get_node_or_null(^"SpawnPoint")
	pawn.global_position = sp.global_position if sp != null else Vector3.ZERO
	if pawn is PlayerPawn:
		(pawn as PlayerPawn).configure_organism(GameDirector.organism_for_new_run, GameDirector.session_for_new_run)
		var rig := pawn.get_node_or_null(^"CameraRig")
		if rig != null and rig.has_method("bind_target"):
			rig.bind_target(pawn)


func _spawn_creatures() -> void:
	if fish_scene == null:
		fish_scene = load("res://scenes/organisms/PreyCreature.tscn")
	var center := Vector3.ZERO
	var spawned := AgentSwarmManager.spawn_from_table(_table(), center, self, 90.0)
	for node in spawned:
		if node is CreatureAgent:
			(node as CreatureAgent).focus = GameDirector.player


func _table() -> SpawnTableData:
	var t := SpawnTableData.new()
	var prey := SpawnTableData.SpawnEntry.new()
	prey.creature_scene = fish_scene
	prey.weight = 1.0
	prey.min_count = int(fish_count * 0.75)
	prey.max_count = prey.min_count
	prey.min_scale = 0.3
	prey.max_scale = 1.0
	prey.spawn_radius = 90.0
	prey.min_depth = -10.0
	prey.max_depth = 6.0
	var pred := SpawnTableData.SpawnEntry.new()
	pred.creature_scene = fish_scene
	pred.weight = 0.5
	pred.min_count = int(fish_count * 0.25)
	pred.max_count = pred.min_count
	pred.min_scale = 0.6
	pred.max_scale = 1.3
	pred.spawn_radius = 90.0
	pred.min_depth = -10.0
	pred.max_depth = 6.0
	pred.make_predator = true
	t.entries = [prey, pred]
	return t


func _apply_audio() -> void:
	if AudioDirector != null and organism_profile() != null:
		AudioDirector.set_ambience(organism_profile().ambience_bed, 3.0)
		AudioDirector.set_music(organism_profile().music_bed, 4.0)


func organism_profile() -> EnvironmentProfile:
	return GameDirector.organism_for_new_run.environment_profile if GameDirector.organism_for_new_run != null else null
