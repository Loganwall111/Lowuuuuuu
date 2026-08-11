class_name CameraTypes
extends RefCounted
## CameraTypes — shared integer identifiers for camera modes.
## Kept separate from LocomotionTypes so the camera and movement systems can
## evolve independently. The CameraRig maps these IDs to RefCounted camera
## "mode" strategies (see scripts/camera/camera_mode_*.gd).

enum Mode {
	FIRST_PERSON,
	THIRD_PERSON,
	CINEMATIC,
	FREE,
	INSPECTION,
	SPACE_FLIGHT,
}

static func name_of(mode: int) -> StringName:
	match mode:
		Mode.FIRST_PERSON: return &"First Person"
		Mode.THIRD_PERSON: return &"Third Person"
		Mode.CINEMATIC: return &"Cinematic"
		Mode.FREE: return &"Free Camera"
		Mode.INSPECTION: return &"Inspect"
		Mode.SPACE_FLIGHT: return &"Flight"
		_: return &"Unknown"

static func cycle_order() -> Array[int]:
	return [Mode.THIRD_PERSON, Mode.FIRST_PERSON, Mode.FREE, Mode.CINEMATIC]
