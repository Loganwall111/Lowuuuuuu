class_name StateFly
extends MovementState
## StateFly — powered flight (insect / avian). Same fluid model as swimming but
## tuned for air: lower medium density, no buoyancy, and steep bank on turns so
## banking reads clearly against the sky.

func _init(controller: LocomotionController) -> void:
	super._init(controller)
	id = LocomotionTypes.State.FLYING
	label = "Flying"


func physics_step(delta: float, wish_dir: Vector3, wish_up: float, sprint: bool) -> void:
	var p: MovementProfile = ctx.active_profile
	var v: Vector3 = ctx.velocity
	var top := p.effective_max_speed(ctx.body_scale) * (p.sprint_multiplier if sprint else 1.0)

	if wish_dir.length_squared() > 0.0001:
		var desired := wish_dir.normalized() * top
		v = v.move_toward(desired, p.acceleration * delta)
	else:
		v.x = move_toward(v.x, 0.0, p.drag * delta)
		v.z = move_toward(v.z, 0.0, p.drag * delta)
		v.y = move_toward(v.y, 0.0, p.drag * delta)

	v.y += wish_up * p.acceleration * 0.8 * delta
	ctx.velocity = v
	ctx.move_and_slide()
	if wish_dir.length_squared() > 0.0001:
		apply_bank(wish_dir, wish_dir.x, p.bank_angle * 1.8, 0.18)
