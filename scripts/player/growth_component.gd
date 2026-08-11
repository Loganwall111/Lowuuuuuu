class_name GrowthComponent
extends Node
## GrowthComponent — turns mass into a smoothly interpolated body scale.
##
## DESIGN: growth is PHYSICAL, not a number flip. We lerp the pawn's
## global_scale toward `target_scale` every frame, so a creature visibly
## swells as it eats. The collision shape radius tracks the scale at a
## fraction (physics should not snap abruptly), while the visual mesh uses the
## full, smooth scale.
##
## The component does NOT pick body_scale_multiplier; EvolutionComponent sets
## it when a stage is reached. Growth only owns the smoothing math.
##
## ATTACH THIS SCRIPT TO:
##   PlayerPawn (CharacterBody3D)
##   └── GrowthComponent   <-- here
##
## Required: a CollisionShape3D node (capsule) referenced via the inspector so
## the physics radius can grow in step with the visible body.

signal growth_updated(current_scale: float, target_scale: float)

@export_group("Tuning")
@export var growth_speed: float = 0.9
@export var collision_radius_scale: float = 0.62   ## physics scales slower than visuals
@export var min_scale: float = 0.04

@export_group("Node References")
@export var collision_shape: CollisionShape3D

var base_scale: float = 1.0
var body_scale_multiplier: float = 1.0
var target_scale: float = 1.0
var current_scale: float = 1.0
var _base_capsule_radius: float = 0.5
var _base_capsule_height: float = 1.0

var _owner_body: Node3D


func _ready() -> void:
	_owner_body = get_parent() as Node3D
	if _owner_body == null:
		push_error("GrowthComponent must be a child of a Node3D.")
		return
	if collision_shape != null and collision_shape.shape is CapsuleShape3D:
		var cap := collision_shape.shape as CapsuleShape3D
		_base_capsule_radius = cap.radius
		_base_capsule_height = cap.height
	current_scale = maxf(min_scale, base_scale)
	_owner_body.global_scale = Vector3.ONE * current_scale
	_apply_collision(current_scale)


## Called by EvolutionComponent when a stage changes the body multiplier.
func set_body_scale_multiplier(multiplier: float) -> void:
	body_scale_multiplier = maxf(0.0, multiplier)
	_recompute_target()


## Called by Metabolism/Growth-feeding when mass changes.
func set_base_scale_from_mass(mass: float, mass_to_scale: float) -> void:
	base_scale = maxf(min_scale, pow(maxf(mass, 0.0001), mass_to_scale))
	_recompute_target()


func _recompute_target() -> void:
	target_scale = maxf(min_scale, base_scale * body_scale_multiplier)


func _physics_process(delta: float) -> void:
	if _owner_body == null:
		return
	if abs(current_scale - target_scale) < 0.0005:
		if current_scale != target_scale:
			current_scale = target_scale
			_owner_body.global_scale = Vector3.ONE * current_scale
			_apply_collision(current_scale)
			growth_updated.emit(current_scale, target_scale)
		return
	# Critically-damped-ish exponential approach: frame-rate independent.
	var t := 1.0 - exp(-growth_speed * delta)
	current_scale = lerpf(current_scale, target_scale, t)
	_owner_body.global_scale = Vector3.ONE * current_scale
	_apply_collision(current_scale)
	growth_updated.emit(current_scale, target_scale)


func _apply_collision(scale_value: float) -> void:
	if collision_shape == null or not (collision_shape.shape is CapsuleShape3D):
		return
	var cap := collision_shape.shape as CapsuleShape3D
	var phys := clampf(scale_value * collision_radius_scale, 0.05, 50.0)
	cap.radius = _base_capsule_radius * phys / maxf(current_scale, 0.0001)
	cap.height = _base_capsule_height * scale_value
