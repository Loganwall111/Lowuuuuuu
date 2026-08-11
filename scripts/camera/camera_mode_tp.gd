class_name CameraModeThirdPerson
extends RefCounted
## Third-person: a spring-arm camera that trails behind the pawn and collides
## with the world. The pawn's mesh stays visible (satisfies the "premium
## creature presentation" brief).

func mode_id() -> int:
	return CameraTypes.Mode.THIRD_PERSON


func _init(rig: CameraRig) -> void:
	_rig = rig


var _rig: CameraRig


func update(delta: float, _speed: float) -> void:
	var cam := _rig.camera
	if cam == null or _rig.target == null:
		return
	var yp := _rig.get_yaw_pitch()
	var basis := Basis.from_euler(Vector3(yp.y, yp.x, 0.0))
	var dist := _rig.third_person_distance * (0.6 + 0.4 * _rig._creature_scale)
	var back := -basis.z * dist
	var target_world := _rig.target.global_position + Vector3(0, _rig.eye_height * _rig._creature_scale, 0)
	var desired := target_world + back
	var t := 1.0 - exp(-_rig.position_smoothing * delta)
	cam.global_position = cam.global_position.lerp(desired, t)
	cam.look_at(target_world, Vector3.UP)

	# Subtle head-bob while moving.
	if _speed > 0.05:
		_rig._bob_phase += delta * _rig.head_bob_frequency * (1.0 + _speed)
		cam.position.y += sin(_rig._bob_phase) * _rig.head_bob_amplitude * _speed
