class_name ObjectiveDirector
extends Node
## ObjectiveDirector — turns the raw evolution/progress signals into a single,
## structured "what should I do next?" the HUD can show. Pure presentation logic:
## it listens to EventBus and re-derives the current objective from the live pawn.
## Keeping it an autoload means any UI (HUD, compass, quest log) can read one
## source of truth instead of each duplicating the evolution math.
##
## DEPENDENCIES: EventBus, GameDirector, PlayerPawn, EvolutionComponent.

signal objective_changed(text: String, target_position: Vector3)

var _current_text: String = ""
var _target: Vector3 = Vector3.ZERO
var _dirty: bool = true


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	EventBus.evolution_stage_changed.connect(func(_s, _p): _dirty = true)
	EventBus.evolution_points_changed.connect(func(_c, _r): _dirty = true)
	EventBus.player_spawned.connect(func(_p): _dirty = true)
	EventBus.mass_changed.connect(func(_m, _c): _dirty = true)


func _process(_delta: float) -> void:
	if not _dirty:
		return
	_dirty = false
	_recompute()


func _recompute() -> void:
	var pawn: PlayerPawn = GameDirector.player as PlayerPawn
	if pawn == null or pawn.evolution == null:
		_current_text = "Explore your world."
		_target = Vector3.ZERO
	else:
		_current_text = pawn.evolution.next_requirement_text()
		_target = _nearest_creature(pawn)
	objective_changed.emit(_current_text, _target)


func _nearest_creature(pawn: PlayerPawn) -> Vector3:
	if SimulationDirector == null:
		return Vector3.ZERO
	var best := Vector3.ZERO
	var best_d := INF
	for agent in SimulationDirector._agents:
		if agent is CreatureAgent and is_instance_valid(agent):
			var d := agent.global_position.distance_to(pawn.global_position)
			if d < best_d:
				best_d = d
				best = agent.global_position
	return best
