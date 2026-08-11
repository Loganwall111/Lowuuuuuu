class_name StateRun
extends StateWalk
## StateRun — a sprinting variant of walking. Implemented as a subclass so the
## walking physics is shared (DRY) and only the speed multiplier differs. The
## LocomotionController selects RUNNING over WALKING when the sprint action is
## held and the organism has unlocked running.

func _init(controller: LocomotionController) -> void:
	super._init(controller)
	id = LocomotionTypes.State.RUNNING
	label = "Running"


func physics_step(delta: float, wish_dir: Vector3, wish_up: float, _sprint: bool) -> void:
	# Force sprint on for the shared walk implementation.
	super.physics_step(delta, wish_dir, wish_up, true)
