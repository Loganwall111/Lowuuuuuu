class_name BiomeForest
extends BaseBiome
## BiomeForest — terrestrial ecosystem (primate path). A gentle heightfield
## ground, scattered trees (MultiMesh) and ambient fauna. Demonstrates that the
## SAME player pawn walks/climbs here using the exact same movement code.
##
## ATTACH THIS SCRIPT TO: ForestBiome (Node3D)  (ForestBiome.tscn)

@export var tree_count: int = 260
@export var tree_mesh: Mesh

func _ready() -> void:
	biome_id = &"forest"
	_build_world()


func _build_world() -> void:
	_build_terrain(200.0, 22.0, 31, Color(0.2, 0.28, 0.18), Color(0.35, 0.4, 0.25))
	if tree_mesh != null:
		var mat := StandardMaterial3D.new()
		mat.albedo_color = Color(0.15, 0.3, 0.18)
		mat.roughness = 0.9
		var area := AABB(Vector3(-90, 0, -90), Vector3(180, 0, 180))
		add_child(ScatterManager.scatter_mesh(tree_mesh, mat, tree_count, area, 41, 0.8, 2.4))
	_spawn_fish(fish_count)  # ambient ground creatures reuse the agent
