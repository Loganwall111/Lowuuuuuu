class_name BaseBiome
extends Node3D
## BaseBiome — shared bootstrapping for every ecosystem scene. Concrete biomes
## (Reef/Wetland/Forest/DeepSpace) inherit this and override _build_world() to
## shape their terrain, props and creatures. This keeps "spawn player + apply
## profile + audio + biome signal" in exactly one place.
##
## DEPENDENCIES: GameDirector, EnvironmentProfile, AudioDirector, EventBus,
## PlayerPawn, AgentSwarmManager.

@export var player_scene: PackedScene
@export var biome_id: StringName = &"biome"
@export var terrain_size: float = 180.0
@export var terrain_height: float = 14.0
@export var terrain_seed: int = 7
@export var terrain_low_color: Color = Color(0.1, 0.2, 0.25)
@export var terrain_high_color: Color = Color(0.3, 0.45, 0.4)
@export var fish_scene: PackedScene
@export var fish_count: int = 40

var _seabed: MeshInstance3D


func _ready() -> void:
	_build_world()
	_spawn_player()
	_apply_profile_audio()
	EventBus.biome_entered.emit(biome_id)


## Override in subclasses to add water/plankton/creatures/vegetation.
func _build_world() -> void:
	_build_terrain(terrain_size, terrain_height, terrain_seed, terrain_low_color, terrain_high_color)


func _build_terrain(w: float, h: float, seed: int, low: Color, high: Color) -> void:
	var mesh := TerrainMeshBuilder.build(w, h, h, seed, 0.03, low, high)
	_seabed = MeshInstance3D.new()
	_seabed.name = "Ground"
	var mat := StandardMaterial3D.new()
	mat.albedo_color = low.lerp(high, 0.5)
	mat.roughness = 0.95
	_seabed.material_override = mat
	_seabed.mesh = mesh
	add_child(_seabed)


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


func _spawn_fish(count: int, predator_fraction: float = 0.25) -> void:
	if fish_scene == null:
		fish_scene = load("res://scenes/organisms/PreyCreature.tscn")
	var t := SpawnTableData.new()
	var prey := SpawnTableData.SpawnEntry.new()
	prey.creature_scene = fish_scene
	prey.min_count = int(count * (1.0 - predator_fraction))
	prey.max_count = prey.min_count
	prey.spawn_radius = 80.0
	prey.min_scale = 0.3
	prey.max_scale = 1.0
	var pred := SpawnTableData.SpawnEntry.new()
	pred.creature_scene = fish_scene
	pred.min_count = int(count * predator_fraction)
	pred.max_count = pred.min_count
	pred.spawn_radius = 80.0
	pred.min_scale = 0.5
	pred.max_scale = 1.2
	pred.make_predator = true
	t.entries = [prey, pred]
	var spawned := AgentSwarmManager.spawn_from_table(t, Vector3.ZERO, self, 90.0)
	for node in spawned:
		if node is CreatureAgent:
			(node as CreatureAgent).focus = GameDirector.player


func _apply_profile_audio() -> void:
	if AudioDirector == null:
		return
	var prof := GameDirector.organism_for_new_run.environment_profile if GameDirector.organism_for_new_run != null else null
	if prof != null:
		AudioDirector.set_ambience(prof.ambience_bed, 3.0)
		AudioDirector.set_music(prof.music_bed, 4.0)


func organism_profile() -> EnvironmentProfile:
	return GameDirector.organism_for_new_run.environment_profile if GameDirector.organism_for_new_run != null else null
