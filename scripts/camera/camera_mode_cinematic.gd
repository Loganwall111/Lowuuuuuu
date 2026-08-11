class_name CameraModeCinematic
extends RefCounted
## Cinematic: a slow, automatic orbit used for stage transitions, title cards
## and "inspect the world" beats. Mouse look is ignored; the camera frames the
## pawn dramatically.

func mode_id() -> int:
	return CameraTypes.Mode.CINEMATIC


func _init(rig: CameraRig) -> void:
	_rig = rig


var _rig: CameraRig
var _angle: float = 0.0


func update(delta: float, _speed: float) -> void:
	var cam := _rig.camera
	if cam == null or _rig.target == null:
		return
	_angle += delta * 0.35
	var radius := _rig.third_person_distance * 1.6 * (0.6 + 0.4 * _rig._creature_scale)
	var height := _rig.eye_height * 2.0 * _rig._creature_scale
	var desired := _rig.target.global_position + Vector3(cos(_angle) * radius, height, sin(_angle) * radius)
	cam.global_position = cam.global_position.lerp(desired, 1.0 - exp(-4.0 * delta))
	cam.look_at(_rig.target.global_position + Vector3(0, height * 0.4, 0), Vector3.UP)
