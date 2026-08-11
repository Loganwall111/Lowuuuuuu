class_name CameraModeFree
extends RefCounted
## Free camera: orbital spectator view, mouse-drag to rotate, no input
## constraints. Handy for debugging environments and for "beauty shots".

func mode_id() -> int:
	return CameraTypes.Mode.FREE


func _init(rig: CameraRig) -> void:
	_rig = rig


var _rig: CameraRig
var _dist: float = 10.0


func update(delta: float, _speed: float) -> void:
	var cam := _rig.camera
	if cam == null or _rig.target == null:
		return
	var yp := _rig.get_yaw_pitch()
	var basis := Basis.from_euler(Vector3(yp.y, yp.x, 0.0))
	var desired := _rig.target.global_position + basis * Vector3(0, 2.0, _dist)
	cam.global_position = cam.global_position.lerp(desired, 1.0 - exp(-8.0 * delta))
	cam.look_at(_rig.target.global_position, Vector3.UP)


func on_mouse_motion(motion: Vector2) -> void:
	_rig.apply_yaw_pitch_from_mouse(motion)
