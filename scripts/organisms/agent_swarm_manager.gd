class_name AgentSwarmManager
extends Node
## AgentSwarmManager — population bookkeeping + spawn helper for ambient
## creatures. Biomes call spawn_from_table() during load; the actual per-agent
## simulation is owned by SimulationDirector (distance-banded ticks). This node
## only manages *how many* and *where*.
##
## DEPENDENCIES: SpawnTableData, CreatureAgent (implicit), SimulationDirector.

var population: int = 0
var _by_species: Dictionary = {}


func spawn_from_table(table: SpawnTableData, center: Vector3, parent: Node,
		bounds_radius: float = 80.0) -> Array[Node]:
	var spawned: Array[Node] = []
	if table == null or parent == null:
		return spawned
	for entry in table.entries:
		if entry.creature_scene == null:
			continue
		var count := randi_range(entry.min_count, entry.max_count)
		for i in count:
			var inst := entry.creature_scene.instantiate()
			parent.add_child(inst)
			var ang := randf() * TAU
			var rad := sqrt(randf()) * entry.spawn_radius
			var y := randf_range(entry.min_depth, entry.max_depth)
			inst.global_position = center + Vector3(cos(ang) * rad, y, sin(ang) * rad)
			var s := randf_range(entry.min_scale, entry.max_scale)
			inst.scale = Vector3(s, s, s)
			if inst is CreatureAgent:
			(inst as CreatureAgent).is_predator = entry.make_predator
			(inst as CreatureAgent).focus = GameDirector.player
				(inst as CreatureAgent).focus = GameDirector.player
				(inst as CreatureAgent).bounds_center = center
				(inst as CreatureAgent).bounds_radius = bounds_radius
			spawned.append(inst)
	population += spawned.size()
	return spawned


func note_despawn() -> void:
	population = maxi(0, population - 1)
