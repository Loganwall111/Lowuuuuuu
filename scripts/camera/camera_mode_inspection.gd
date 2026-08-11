class_name CameraModeInspection
extends RefCounted
## Inspection: a slow turntable around the creature for the "inspect your
## evolved form" screen. Auto-rotates; the player can also drag to look.

func mode_id() -> int:
	return CameraTypes.Mode.INSPECTION


func _init(rig: CameraRig) -> void:
	_rig = rig


var _rig: CameraRig
var _angle: float = 0.0


func update(delta: float, _speed: float) -> void:
	var cam := _rig.camera
	if cam == null or _rig.target == null:
		return
	_angle += delta * 0.5
	var radius := 4.0 * (0.5 + 0.5 * _rig._creature_scale)
	var desired := _rig.target.global_position + Vector3(cos(_angle) * radius, 1.5 * _rig._creature_scale, sin(_angle) * radius)
	cam.global_position = cam.global_position.lerp(desired, 1.0 - exp(-5.0 * delta))
	cam.look_at(_rig.target.global_position, Vector3.UP)


func on_mouse_motion(motion: Vector2) -> void:
	_rig.apply_yaw_pitch_from_mouse(motion)
