class_name CameraModeSpaceFlight
extends RefCounted
## Space-flight camera: a wide chase cam for the future space stage. Pulls back
## with speed and keeps the ship framed against the void.

func mode_id() -> int:
	return CameraTypes.Mode.SPACE_FLIGHT


func _init(rig: CameraRig) -> void:
	_rig = rig


var _rig: CameraRig


func update(delta: float, speed: float) -> void:
	var cam := _rig.camera
	if cam == null or _rig.target == null:
		return
	var yp := _rig.get_yaw_pitch()
	var basis := Basis.from_euler(Vector3(yp.y, yp.x, 0.0))
	var dist := (_rig.third_person_distance + speed * 6.0) * (0.6 + 0.4 * _rig._creature_scale)
	var desired := _rig.target.global_position + basis * Vector3(0, 2.0, dist)
	cam.global_position = cam.global_position.lerp(desired, 1.0 - exp(-6.0 * delta))
	cam.look_at(_rig.target.global_position, Vector3.UP)
