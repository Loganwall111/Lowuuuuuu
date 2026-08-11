class_name DebrisBody
extends RigidBody3D
## DebrisBody — a physics prop (rock, husk, asteroid chunk) that is pulled by
## gravity wells via GravityAffected and can be registered with
## SimulationDirector if it needs AI. Demonstrates the "reusable gravity" goal:
## the SAME GravityAffected component bends debris and (later) spaceships.
##
## ATTACH THIS SCRIPT TO:
##   DebrisBody (RigidBody3D)   <-- here
##   └── CollisionShape3D
##   └── MeshInstance3D
##   └── GravityAffected (scripts/physics/gravity_affected.gd)
##
## DEPENDENCIES: GravityAffected, GravityWellRegistry.

@export var is_asteroid: bool = false
@export var spin: float = 0.4

var _affected: GravityAffected


func _ready() -> void:
	_affected = get_node_or_null(^"GravityAffected") as GravityAffected
	if _affected == null:
		_affected = GravityAffected.new()
		add_child(_affected)


func _physics_process(delta: float) -> void:
	# Slow tumble for life; gravity is applied by GravityAffected.
	rotate_object_local(Vector3(0.2, 1.0, 0.1).normalized(), spin * delta)
