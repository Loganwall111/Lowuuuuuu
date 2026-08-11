class_name SpawnTableData
extends Resource
## SpawnTableData — weighted table of creatures for a biome.
## Read by a biome's spawner (e.g. BiomeReef) during the loading screen / in
## waves, so each ecosystem has its own population mix with no code changes.

@export var entries: Array[SpawnEntry] = []

class SpawnEntry extends Resource:
	@export var creature_scene: PackedScene
	@export var weight: float = 1.0
	@export var min_count: int = 1
	@export var max_count: int = 6
	@export var min_scale: float = 0.2
	@export var make_predator: bool = false
	@export var max_scale: float = 1.2
	@export var spawn_radius: float = 80.0
	@export var min_depth: float = -40.0
	@export var max_depth: float = 0.0

func pick_entry() -> SpawnEntry:
	if entries.is_empty():
		return null
	var total := 0.0
	for entry in entries:
		total += entry.weight
	var roll := randf() * total
	for entry in entries:
		roll -= entry.weight
		if roll <= 0.0:
			return entry
	return entries[0]
