class_name LocomotionController
extends Node
## LocomotionController — the single _physics_process driver of player movement.
##
## ARCHITECTURE (why a controller + RefCounted states, not one big script):
##   * Only this node ticks physics, so switching movement mode is free.
##   * Each behaviour lives in its own file (state_*.gd) and is selected from a
##     registry keyed by LocomotionTypes.State. Adding BURROWING = adding one
##     file + one registry entry; nothing else changes.
##   * Gravity wells are integrated HERE, once, from a global registry, so every
##     movement state (and later spaceships) feels the same pull with zero
##     per-state gravity code.
##
## ATTACH THIS SCRIPT TO:
##   PlayerPawn (CharacterBody3D)
##   └── LocomotionController   <-- here
##
## DEPENDENCIES: MovementState + state_*, EnvironmentProbe, CameraRig,
## GrowthComponent, GravityWellRegistry (scripts/physics/gravity_well.gd),
## MovementProfile, EventBus, LocomotionTypes.

signal state_changed(new_state: int, previous_state: int)

@export_group("Tuning")
@export var input_deadzone: float = 0.13
@export var sense_radius_base: float = 6.0
@export var collision_shake_threshold: float = 6.0

var pawn: CharacterBody3D
var body: CharacterBody3D
var probe: EnvironmentProbe
var camera_rig: Node
var growth: GrowthComponent

var velocity: Vector3 = Vector3.ZERO
var active_profile: MovementProfile
var active_state: MovementState
var current_state_id: int = -1
var body_scale: float = 1.0

var _well_registry: Node
var _gravity_accel: Vector3 = Vector3.ZERO
var _state_factory: Dictionary = {}


func _ready() -> void:
	pawn = get_parent() as CharacterBody3D
	body = pawn
	probe = pawn.get_node_or_null(^"EnvironmentProbe") as EnvironmentProbe
	camera_rig = pawn.get_node_or_null(^"CameraRig")
	growth = pawn.get_node_or_null(^"GrowthComponent") as GrowthComponent
	_well_registry = get_node_or_null("/root/GravityWellRegistry") if false else _find_registry()

	_register_state(StateSwim.new(self))
	_register_state(StateFloat.new(self))
	_register_state(StateCrawl.new(self))
	_register_state(StateWalk.new(self))
	_register_state(StateRun.new(self))
	_register_state(StateFly.new(self))
	_register_state(StateClimb.new(self))
	_register_state(StateSpaceFlight.new(self))

	# Begin in whatever the organism's base mode is; the probe will refine it.
	var start := LocomotionTypes.State.SWIMMING
	if growth != null:
		body_scale = growth.current_scale
	_enter_state(start)


func _find_registry() -> Node:
	# GravityWellRegistry is created by the first GravityWell3D in the scene.
	return get_tree().get_first_node_in_group(&"gravity_well_registry")


func configure(organism: OrganismData) -> void:
	if organism != null and organism.get_root_stage() != null:
		active_profile = organism.get_root_stage().base_movement_profile
	if active_profile == null:
		active_profile = MovementProfile.new()


func _register_state(state: MovementState) -> void:
	_state_factory[state.id] = state


## --- Public surface used by MovementState ---------------------------------

var camera_basis: Basis:
	get:
		if camera_rig != null and camera_rig.has_method("get_camera_basis"):
			return camera_rig.get_camera_basis()
		return pawn.global_transform.basis


func move_and_slide() -> void:
	body.velocity = velocity
	body.move_and_slide()
	velocity = body.velocity
	if body.get_slide_collision_count() > 0:
		var impact := velocity.length()
		if impact > collision_shake_threshold:
			EventBus.camera_shake_requested.emit(clampf(impact * 0.01, 0.0, 0.6), 0.25)


func set_max_slope_angle(rad: float) -> void:
	body.floor_max_angle = rad


## --- Main tick ---------------------------------------------------------------

func _physics_process(delta: float) -> void:
	if pawn == null or active_profile == null:
		return
	if growth != null:
		body_scale = growth.current_scale

	_integrate_gravity_wells(delta)

	var desired := _choose_state()
	if desired != current_state_id:
		_enter_state(desired)

	var wish := _build_wish()
	var sprint := Input.is_action_pressed(&"sprint")
	active_state.physics_step(delta, wish.direction, wish.up, sprint)


func _integrate_gravity_wells(delta: float) -> void:
	_gravity_accel = Vector3.ZERO
	if _well_registry == null or not _well_registry.has_method("sample_acceleration"):
		return
	_gravity_accel = _well_registry.sample_acceleration(pawn.global_position, self)
	if _gravity_accel != Vector3.ZERO:
		velocity += _gravity_accel * delta


func _build_wish() -> Dictionary:
	var forward := Vector3.ZERO
	var right := Vector3.ZERO
	var fwd := camera_basis.z
	var rgt := camera_basis.x
	forward = Vector3(fwd.x, 0, fwd.z)
	right = Vector3(rgt.x, 0, rgt.z)
	if forward.length_squared() > 0.0001:
		forward = forward.normalized()
	if right.length_squared() > 0.0001:
		right = right.normalized()

	var ix := Input.get_axis(&"move_left", &"move_right")
	var iz := Input.get_axis(&"move_back", &"move_forward")  # forward positive
	var iy := Input.get_axis(&"move_down", &"move_up")
	if absf(ix) < input_deadzone:
		ix = 0.0
	if absf(iz) < input_deadzone:
		iz = 0.0
	if absf(iy) < input_deadzone:
		iy = 0.0

	var dir := forward * iz + right * ix
	return {"direction": dir, "up": iy}


## Picks the best unlocked state for the current environment + abilities.
func _choose_state() -> int:
	var unlocked: Array = []
	if GrowthComponent != null:
		pass
	var evo := pawn.get_node_or_null(^"EvolutionComponent") as EvolutionComponent
	var has := func(s: int) -> bool:
		return evo != null and evo.has_movement_state(s)
	var base := LocomotionTypes.State.SWIMMING
	if probe != null and probe.is_submerged:
		if has.call(LocomotionTypes.State.SWIMMING): return LocomotionTypes.State.SWIMMING
		if has.call(LocomotionTypes.State.FLOATING): return LocomotionTypes.State.FLOATING
		return LocomotionTypes.State.SWIMMING
	if probe != null and probe.is_wall_contact and has.call(LocomotionTypes.State.CLIMBING):
		return LocomotionTypes.State.CLIMBING
	if probe != null and probe.is_grounded:
		var sprint := Input.is_action_pressed(&"sprint")
		if sprint and has.call(LocomotionTypes.State.RUNNING): return LocomotionTypes.State.RUNNING
		if has.call(LocomotionTypes.State.WALKING): return LocomotionTypes.State.WALKING
		if has.call(LocomotionTypes.State.CRAWLING): return LocomotionTypes.State.CRAWLING
		if has.call(LocomotionTypes.State.FLOATING): return LocomotionTypes.State.FLOATING
		return LocomotionTypes.State.SWIMMING
	# Airborne: prefer flight, else fall back to swim-like freedom.
	if has.call(LocomotionTypes.State.FLYING): return LocomotionTypes.State.FLYING
	if has.call(LocomotionTypes.State.SPACE_FLIGHT): return LocomotionTypes.State.SPACE_FLIGHT
	if has.call(LocomotionTypes.State.SWIMMING): return LocomotionTypes.State.SWIMMING
	return base


func _enter_state(id: int) -> void:
	if not _state_factory.has(id):
		return
	if active_state != null:
		active_state.exit()
	var previous := current_state_id
	current_state_id = id
	active_state = _state_factory[id]
	active_state.enter()
	if previous != id:
		state_changed.emit(id, previous)
		EventBus.movement_state_changed.emit(id, previous)


func get_state_label() -> String:
	return LocomotionTypes.name_of(current_state_id)
