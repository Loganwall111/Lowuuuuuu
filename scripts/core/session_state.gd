class_name SessionState
extends RefCounted
## SessionState — the serialisable snapshot of a single playthrough.
##
## Held by GameDirector. Deliberately a plain RefCounted (not a Node, not an
## autoload) so it can be copied, diffed, serialised and unit-tested without
## touching the scene tree.
##
## DEPENDENCIES: OrganismData (scripts/data/organism_data.gd)

var species_id: StringName = &""
var organism: OrganismData = null

var stage_id: StringName = &""
var unlocked_abilities: Array[StringName] = []
var unlocked_movement_states: Array[int] = []
## Every stage_id the player has passed through, in order. Drives the
## "evolutionary history" panel and prevents re-entering a consumed branch.
var lineage: Array[StringName] = []

var mass: float = 0.0
var energy: float = 0.0
var health: float = 100.0
var evolution_points: float = 0.0
var age_seconds: float = 0.0

var playtime_seconds: float = 0.0
var creatures_consumed: int = 0
var biomass_consumed: float = 0.0
var distance_travelled: float = 0.0

var last_position: Vector3 = Vector3.ZERO
var environment_path: String = ""


func to_dictionary() -> Dictionary:
	return {
		"version": 1,
		"species_id": String(species_id),
		"stage_id": String(stage_id),
		"unlocked_abilities": _string_array(unlocked_abilities),
		"unlocked_movement_states": unlocked_movement_states.duplicate(),
		"lineage": _string_array(lineage),
		"mass": mass,
		"energy": energy,
		"health": health,
		"evolution_points": evolution_points,
		"age_seconds": age_seconds,
		"playtime_seconds": playtime_seconds,
		"creatures_consumed": creatures_consumed,
		"biomass_consumed": biomass_consumed,
		"distance_travelled": distance_travelled,
		"last_position": [last_position.x, last_position.y, last_position.z],
		"environment_path": environment_path,
	}


static func from_dictionary(data: Dictionary) -> SessionState:
	var state := SessionState.new()
	state.species_id = StringName(String(data.get("species_id", "")))
	state.stage_id = StringName(String(data.get("stage_id", "")))
	for a in data.get("unlocked_abilities", []):
		state.unlocked_abilities.append(StringName(String(a)))
	for m in data.get("unlocked_movement_states", []):
		state.unlocked_movement_states.append(int(m))
	for l in data.get("lineage", []):
		state.lineage.append(StringName(String(l)))
	state.mass = float(data.get("mass", 0.0))
	state.energy = float(data.get("energy", 0.0))
	state.health = float(data.get("health", 100.0))
	state.evolution_points = float(data.get("evolution_points", 0.0))
	state.age_seconds = float(data.get("age_seconds", 0.0))
	state.playtime_seconds = float(data.get("playtime_seconds", 0.0))
	state.creatures_consumed = int(data.get("creatures_consumed", 0))
	state.biomass_consumed = float(data.get("biomass_consumed", 0.0))
	state.distance_travelled = float(data.get("distance_travelled", 0.0))
	var pos: Array = data.get("last_position", [0.0, 0.0, 0.0])
	if pos.size() == 3:
		state.last_position = Vector3(float(pos[0]), float(pos[1]), float(pos[2]))
	state.environment_path = String(data.get("environment_path", ""))
	return state


static func _string_array(source: Array) -> Array:
	var out: Array = []
	for v in source:
		out.append(String(v))
	return out
