class_name StateWalk
extends MovementState
## StateWalk — surface locomotion on the ground via CharacterBody3D slide.
## Gravity is applied by the controller; this state resolves slope clamping and
## accel/decel, and supports a sprint sub-mode driven by the RUNNING state.

func _init(controller: LocomotionController) -> void:
	super._init(controller)
	id = LocomotionTypes.State.WALKING
	label = "Walking"


func physics_step(delta: float, wish_dir: Vector3, _wish_up: float, sprint: bool) -> void:
	var p: MovementProfile = ctx.active_profile
	var v: Vector3 = ctx.velocity
	v.y -= ProjectSettings.get_setting("physics/3d/default_gravity", 9.8) * p.gravity_scale * delta

	if wish_dir.length_squared() > 0.0001:
		var top := p.effective_max_speed(ctx.body_scale)
		if sprint:
			top *= p.sprint_multiplier
		var desired := wish_dir.normalized() * top
		v.x = move_toward(v.x, desired.x, p.acceleration * delta)
		v.z = move_toward(v.z, desired.z, p.acceleration * delta)
	else:
		v.x = move_toward(v.x, 0.0, p.deceleration * delta)
		v.z = move_toward(v.z, 0.0, p.deceleration * delta)

	ctx.velocity = v
	ctx.body.set_max_slope_angle(p.max_slope)
	ctx.move_and_slide()
	if wish_dir.length_squared() > 0.0001:
		apply_bank(wish_dir, wish_dir.x, p.bank_angle * 0.5, 0.1)
