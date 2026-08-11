class_name GravityAffected
extends Node
## GravityAffected — applies nearby gravity-well acceleration to ANY body.
##
## Two integration paths, chosen automatically by the owner's physics type:
##   * RigidBody3D        -> _integrate_forces (the correct, stable hook)
##   * CharacterBody3D    -> _physics_process adds to its velocity
##   * Plain Node3D       -> you read get_last_acceleration() and move it yourself
##
## This is what makes debris, asteroids and (later) spaceships bend around a
## black hole with the SAME code the player uses — no per-type gravity hacks.
##
## ATTACH THIS SCRIPT TO:
##   RigidBody3D / CharacterBody3D / Node3D   (the body to be pulled)
##   └── GravityAffected   <-- here
##
## DEPENDENCIES: GravityWellRegistry (scripts/physics/gravity_well_registry.gd).

@export_group("Tuning")
@export var affected: bool = true
@export var mass_scale: float = 1.0
@export var linear_damp_while_in_field: float = 0.0

var last_acceleration: Vector3 = Vector3.ZERO
var _registry: Node


func _ready() -> void:
	_registry = get_tree().get_first_node_in_group(&"gravity_well_registry")
	if get_parent() is RigidBody3D:
		(get_parent() as RigidBody3D).contact_monitor = true


func _physics_process(_delta: float) -> void:
	if not affected or _registry == null:
		return
	if get_parent() is CharacterBody3D:
		var body := get_parent() as CharacterBody3D
		last_acceleration = _registry.sample_acceleration(body.global_position, self)
		body.velocity += last_acceleration * _delta


func _integrate_forces(state: PhysicsDirectBodyState3D) -> void:
	if not affected or _registry == null:
		return
	if not (get_parent() is RigidBody3D):
		return
	last_acceleration = _registry.sample_acceleration(state.transform.origin, self)
	state.linear_velocity += last_acceleration * state.step * mass_scale


## For plain Node3D owners that move themselves (e.g. a scripted comet).
func sample_now() -> Vector3:
	if _registry == null:
		return Vector3.ZERO
	var owner3d := get_parent() as Node3D
	if owner3d == null:
		return Vector3.ZERO
	last_acceleration = _registry.sample_acceleration(owner3d.global_position, self)
	return last_acceleration
