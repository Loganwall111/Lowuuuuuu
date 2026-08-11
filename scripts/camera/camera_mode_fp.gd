class_name CameraModeFirstPerson
extends RefCounted
## First-person: camera seated at the creature's eye, looking down its nose.
## Used for "I am the animal" intimacy (Subnautica-style). The mesh is hidden
## in this mode so we do not see our own face.

func mode_id() -> int:
	return CameraTypes.Mode.FIRST_PERSON


func _init(rig: CameraRig) -> void:
	_rig = rig


var _rig: CameraRig


func enter() -> void:
	if _rig.mesh_root_hidden_on_fp():
		pass


func update(delta: float, _speed: float) -> void:
	var cam := _rig.camera
	if cam == null or _rig.target == null:
		return
	var yp := _rig.get_yaw_pitch()
	var basis := Basis.from_euler(Vector3(yp.y, yp.x, 0.0))
	var eye := _rig.target.global_position + basis * Vector3(0, _rig.eye_height * _rig._creature_scale, 0.2)
	cam.global_position = eye
	cam.global_transform.basis = basis
