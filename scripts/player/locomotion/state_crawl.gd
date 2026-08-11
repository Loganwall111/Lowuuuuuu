class_name StateCrawl
extends MovementState
## StateCrawl — low, slow ground movement (larva / insect on substrate).
## Like walking but with very low top speed and strong friction, so it reads as
## "clambering" rather than "running".

func _init(controller: LocomotionController) -> void:
	super._init(controller)
	id = LocomotionTypes.State.CRAWLING
	label = "Crawling"


func physics_step(delta: float, wish_dir: Vector3, _wish_up: float, _sprint: bool) -> void:
	var p: MovementProfile = ctx.active_profile
	var v: Vector3 = ctx.velocity
	v.y -= ProjectSettings.get_setting("physics/3d/default_gravity", 9.8) * p.gravity_scale * delta
	if wish_dir.length_squared() > 0.0001:
		var desired := wish_dir.normalized() * p.effective_max_speed(ctx.body_scale)
		v.x = move_toward(v.x, desired.x, p.acceleration * 0.7 * delta)
		v.z = move_toward(v.z, desired.z, p.acceleration * 0.7 * delta)
	else:
		v.x = move_toward(v.x, 0.0, p.deceleration * 1.6 * delta)
		v.z = move_toward(v.z, 0.0, p.deceleration * 1.6 * delta)
	ctx.velocity = v
	ctx.body.set_max_slope_angle(p.max_slope)
	ctx.move_and_slide()
