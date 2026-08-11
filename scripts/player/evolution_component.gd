class_name EvolutionComponent
extends Node
## EvolutionComponent — mass/age/EP tracking and stage progression.
##
## PROGRESSION MODEL
##   mass (from Growth) + age (from Metabolism) + evolution_points (from
##   eating) are the three currencies. A stage is "available" when ALL of its
##   gates (required_mass, required_evolution_points, required_age_seconds)
##   are met. With one descendant the pawn auto-advances; with several it
##   emits evolution_branch_offered so the UI can ask the player (Spore-style
##   branch choice).
##
## State lives in the SessionState (via GameDirector) so it survives stage
## transitions and saves. This component is the *engine*; the save file is the
## *memory*.
##
## ATTACH THIS SCRIPT TO: PlayerPawn (CharacterBody3D)
##   └── EvolutionComponent   <-- here
##
## DEPENDENCIES: OrganismData, EvolutionStageData, GrowthComponent,
## MetabolismComponent, AbilityData, EventBus.

signal stage_reached(stage: EvolutionStageData)
signal branch_offered(options: Array[EvolutionStageData])

@export_group("Tuning")
@export var evolution_point_per_mass: float = 1.5
@export var auto_advance: bool = true
@export var gate_tolerance: float = 0.001

var organism: OrganismData
var current_stage: EvolutionStageData
var evolution_points: float = 0.0
var unlocked_abilities: Array[AbilityData] = []
var unlocked_movement_states: Array[int] = []

var _growth: GrowthComponent
var _metabolism: MetabolismComponent
var _pending_options: Array[EvolutionStageData] = []


func configure(org: OrganismData, session) -> void:
	organism = org
	_growth = get_parent().get_node_or_null(^"GrowthComponent") as GrowthComponent
	_metabolism = get_parent().get_node_or_null(^"MetabolismComponent") as MetabolismComponent

	var stage_id := session.stage_id if session != null and session.stage_id != &"" else org.get_root_stage().stage_id
	current_stage = org.get_stage(stage_id)
	if current_stage == null:
		current_stage = org.get_root_stage()
	unlocked_movement_states = current_stage.enabled_movement_states.duplicate()

	# Reconcile unlocked abilities from the save (resources cannot be saved,
	# so we re-resolve them from the stage graph each load).
	var saved_ability_ids: Array = session.unlocked_abilities if session != null else []
	_rebuild_unlocked_from_lineage(org, session)

	evolution_points = session.evolution_points if session != null else 0.0
	_apply_stage(current_stage, false)
	EventBus.evolution_points_changed.emit(evolution_points, _required_eps_for_next())
	stage_reached.emit(current_stage)


## Re-derives unlocked ability resources from the lineage + current stage so a
## loaded game does not need to serialise AbilityData resources.
func _rebuild_unlocked_from_lineage(org: OrganismData, session) -> void:
	unlocked_abilities.clear()
	var history: Array = []
	if session != null:
		history = session.lineage
	history.append(current_stage.stage_id)
	for sid in history:
		var st := org.get_stage(sid)
		if st == null:
			continue
		for ab in st.unlocked_abilities:
			if ab != null and not _has_ability(ab.ability_id):
				unlocked_abilities.append(ab)


func _has_ability(id: StringName) -> bool:
	for ab in unlocked_abilities:
		if ab.ability_id == id:
			return true
	return false


## Called by the ConsumptionSensor after a successful meal.
func grant_nutrition(payload: NutritionPayload) -> void:
	if payload == null:
		return
	evolution_points += payload.evolution_points * evolution_point_per_mass
	EventBus.evolution_points_changed.emit(evolution_points, _required_eps_for_next())
	_evaluate()


## Called by MetabolismComponent each tick for age; we only re-evaluate on a
## coarse cadence to avoid per-frame branching checks.
func report_age(age_seconds_value: float) -> void:
	_evaluate_deferred()


func _evaluate_deferred() -> void:
	if _pending_options.is_empty():
		_evaluate()


func _required_eps_for_next() -> float:
	var opts := organism.evolution_tree.get_descendants(current_stage.stage_id) if organism.evolution_tree != null else []
	if opts.is_empty():
		# Terminal: show the current stage requirement as the "next" target.
		return current_stage.required_evolution_points
	var min_req := INF
	for o in opts:
		min_req = minf(min_req, o.required_evolution_points)
	return min_req


func _evaluate() -> void:
	if current_stage == null or organism == null:
		return
	var candidates := organism.evolution_tree.get_descendants(current_stage.stage_id) if organism.evolution_tree != null else []
	if candidates.is_empty():
		return

	var ready: Array[EvolutionStageData] = []
	for candidate in candidates:
		if _stage_ready(candidate):
			ready.append(candidate)

	if ready.is_empty():
		return

	if ready.size() == 1 and auto_advance:
		advance_to(ready[0])
	else:
		_pending_options = ready
		branch_offered.emit(ready)
		EventBus.evolution_branch_offered.emit(ready)


## UI calls this when the player picks a branch.
func choose_branch(stage: EvolutionStageData) -> void:
	if stage in _pending_options or _stage_ready(stage):
		_pending_options.clear()
		advance_to(stage)


func _stage_ready(stage: EvolutionStageData) -> bool:
	var ok := true
	if stage.required_mass > 0.0 and _growth != null:
		# Compare the GROWN body scale (target) against the gate.
		ok = ok and _growth.target_scale >= stage.required_mass
	if stage.required_evolution_points > 0.0:
		ok = ok and evolution_points >= stage.required_evolution_points - gate_tolerance
	if stage.required_age_seconds > 0.0 and _metabolism != null:
		ok = ok and _metabolism.age_seconds >= stage.required_age_seconds
	return ok


func advance_to(stage: EvolutionStageData) -> void:
	if stage == null:
		return
	var previous := current_stage
	current_stage = stage
	_apply_stage(stage, true)
	EventBus.evolution_stage_changed.emit(stage, previous)
	EventBus.toast_requested.emit("Evolved: " + stage.display_name, &"epic")
	if not stage.cinematic_title.is_empty():
		EventBus.cinematic_caption_requested.emit(stage.cinematic_title, stage.cinematic_subtitle, 3.6)
	EventBus.evolution_points_changed.emit(evolution_points, _required_eps_for_next())
	stage_reached.emit(stage)
	# Cascade: a new stage may immediately unlock the next.
	_evaluate_deferred()


## Applies the stat/multiplier/ability unlocks of a stage to the live pawn.
func _apply_stage(stage: EvolutionStageData, announce: bool) -> void:
	if stage == null:
		return
	if _growth != null:
		_growth.set_body_scale_multiplier(stage.body_scale_multiplier)
		if stage.base_movement_profile != null:
			_growth.set_base_scale_from_mass(stage.required_mass, 0.18)
	if _metabolism != null and stage.energy_capacity > 0.0:
		_metabolism.set_max_energy(stage.energy_capacity)

	for ab in stage.unlocked_abilities:
		if ab == null or _has_ability(ab.ability_id):
			continue
		unlocked_abilities.append(ab)
		for st in ab.unlocks_movement_states:
			if st not in unlocked_movement_states:
				unlocked_movement_states.append(st)
				EventBus.movement_capability_unlocked.emit(st)
		EventBus.ability_unlocked.emit(ab)
		if announce:
			EventBus.toast_requested.emit("Ability: " + ab.display_name, &"good")

	for st in stage.enabled_movement_states:
		if st not in unlocked_movement_states:
			unlocked_movement_states.append(st)
			EventBus.movement_capability_unlocked.emit(st)


func has_movement_state(state_id: int) -> bool:
	return state_id in unlocked_movement_states


func has_ability(id: StringName) -> bool:
	return _has_ability(id)


## Aggregate ability modifier multiplier (e.g. 1.1 speed if a swim ability is
## unlocked). Used by the LocomotionController when computing top speed.
func ability_speed_multiplier() -> float:
	var m := 1.0
	for ab in unlocked_abilities:
		m *= ab.move_speed_multiplier
	return m


func ability_metabolism_multiplier() -> float:
	var m := 1.0
	for ab in unlocked_abilities:
		m *= ab.metabolism_multiplier
	return m


func ability_sense_multiplier() -> float:
	var m := 1.0
	for ab in unlocked_abilities:
		m *= ab.sense_radius_multiplier
	return m


# ----------------------------------------------------- presentation hooks

func advance_to(stage: EvolutionStageData) -> void:
	if stage == null:
		return
	var previous := current_stage
	current_stage = stage
	_apply_stage(stage, true)
	EventBus.evolution_stage_changed.emit(stage, previous)
	EventBus.toast_requested.emit("Evolved: " + stage.display_name, &"epic")
	if not stage.cinematic_title.is_empty():
		EventBus.cinematic_caption_requested.emit(stage.cinematic_title, stage.cinematic_subtitle, 3.6)
	EventBus.evolution_points_changed.emit(evolution_points, _required_eps_for_next())
	stage_reached.emit(stage)
	if AudioDirector != null:
		AudioDirector.evolve()
	if GameDirector.player != null and GameDirector.player.has_method("morph_to_stage"):
		GameDirector.player.morph_to_stage(stage)
	_evaluate_deferred()


## Short human-readable summary of what the creature needs to evolve next.
## Used by the HUD objective line.
func next_requirement_text() -> String:
	if organism == null or organism.evolution_tree == null:
		return ""
	var opts := organism.evolution_tree.get_descendants(current_stage.stage_id)
	if opts.is_empty():
		return "Fully evolved - explore and dominate."
	var best: EvolutionStageData = opts[0]
	var best_score := INF
	for o in opts:
		var score := 0.0
		score += maxf(0.0, o.required_mass - (_growth.target_scale if _growth != null else 0.0)) * 10.0
		score += maxf(0.0, o.required_evolution_points - evolution_points)
		if score < best_score:
			best_score = score
			best = o
	var parts: Array[String] = []
	if best.required_mass > 0.0:
		parts.append("mass %.1f" % best.required_mass)
	if best.required_evolution_points > 0.0:
		parts.append("EP %.0f" % best.required_evolution_points)
	if best.required_age_seconds > 0.0:
		parts.append("age %.0fs" % best.required_age_seconds)
	return "Evolve to %s: %s" % [best.display_name, ", ".join(parts)]
