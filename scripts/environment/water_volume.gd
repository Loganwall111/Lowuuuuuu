class_name WaterVolume
extends Area3D
## WaterVolume — a body of water. It is the single source of truth for
## "am I underwater?", broadcasting submersion (with depth + surface height) to
## the whole game via EventBus. The player's EnvironmentProbe listens and the
## EnvironmentDirector swaps to an underwater atmosphere profile.
##
## Multiple overlapping volumes are supported: submersion is reference-counted
## in the probe, so leaving one volume while inside another stays "wet".
##
## ATTACH THIS SCRIPT TO:
##   WaterVolume (Area3D)               <-- here
##   └── CollisionShape3D (the water box/scale)
##   └── (optional) a translucent water-surface Mesh + WaterMaterial
##
## DEPENDENCIES: EventBus, EnvironmentProfile, EnvironmentDirector.

@export_group("Surface")
@export var surface_y: float = 0.0
@export var depth_fade: float = 8.0          ## meters until "fully" deep

@export_group("Underwater Look")
@export var underwater_profile: EnvironmentProfile

var _inside: Dictionary = {}   # body: Node3D -> true


func _ready() -> void:
	collision_layer = 64   # WaterVolume
	collision_mask = 2     # only the player matters for submersion
	body_entered.connect(_on_body_entered)
	body_exited.connect(_on_body_exited)


func _on_body_entered(body: Node3D) -> void:
	if body == null or body.collision_layer != 2:
		return
	_inside[body] = true
	EventBus.submersion_changed.emit(true, _depth_of(body))


func _on_body_exited(body: Node3D) -> void:
	if not _inside.has(body):
		return
	_inside.erase(body)
	EventBus.submersion_changed.emit(false, 0.0)


func _depth_of(body: Node3D) -> float:
	return maxf(0.0, surface_y - body.global_position.y)


func _physics_process(_delta: float) -> void:
	# Keep depth fresh for the first tracked body (the player).
	for body in _inside.keys():
		if is_instance_valid(body):
			EventBus.submersion_changed.emit(true, _depth_of(body))
		break
