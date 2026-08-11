class_name CurrentVolume
extends Area3D
## CurrentVolume — a region that pushes bodies along a flow direction.
## Used for rivers, ocean currents and updrafts. Like WaterVolume it is
## data-driven and additive: many currents can overlap and sum.
##
## ATTACH THIS SCRIPT TO:
##   CurrentVolume (Area3D)   <-- here
##   └── CollisionShape3D (the flow region)
##
## DEPENDENCIES: GravityAffected-style integration is NOT needed; we just add
## velocity to CharacterBody3D players and RigidBody3D debris inside us.

@export_group("Flow")
@export var flow_direction: Vector3 = Vector3(1, 0, 0)
@export var flow_strength: float = 6.0
@export var turbulence: float = 0.4   ## random wobble
@export var affect_player: bool = true
@export var affect_debris: bool = true

var _bodies: Array[Node3D] = []


func _ready() -> void:
	collision_layer = 256   # Trigger
	collision_mask = 2 | 8  # Player + Debris
	body_entered.connect(_on_enter)
	body_exited.connect(_on_exit)


func _on_enter(body: Node3D) -> void:
	if body == null:
		return
	if body.collision_layer == 2 and affect_player:
		_bodies.append(body)
	elif body.collision_layer == 8 and affect_debris:
		_bodies.append(body)


func _on_exit(body: Node3D) -> void:
	_bodies.erase(body)


func _physics_process(delta: float) -> void:
	if flow_strength <= 0.0 or _bodies.is_empty():
		return
	for body in _bodies:
		if not is_instance_valid(body):
			continue
		var wobble := Vector3(randf_range(-1, 1), randf_range(-1, 1), randf_range(-1, 1)) * turbulence
		var force := (flow_direction + wobble).normalized() * flow_strength
		if body is CharacterBody3D:
			(body as CharacterBody3D).velocity += force * delta
		elif body is RigidBody3D:
			(body as RigidBody3D).apply_central_impulse(force * delta * 0.02)
