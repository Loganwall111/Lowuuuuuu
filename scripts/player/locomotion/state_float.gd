class_name StateFloat
extends MovementState
## StateFloat — buoyant hovering at/near the surface or in still water.
## Used as the "resting" water behaviour when there is no directional input but
## the creature should not sink. Adds a gentle bob so it reads as alive.

func _init(controller: LocomotionController) -> void:
	super._init(controller)
	id = LocomotionTypes.State.FLOATING
	label = "Floating"


func physics_step(delta: float, wish_dir: Vector3, wish_up: float, sprint: bool) -> void:
	var p: MovementProfile = ctx.active_profile
	var v: Vector3 = ctx.velocity
	var gravity := ProjectSettings.get_setting("physics/3d/default_gravity", 9.8) * p.gravity_scale
	# Strong buoyancy keeps it at neutral: net slight upward, capped low.
	v.y += gravity * (p.buoyancy - 1.05) * delta
	v.y = clampf(v.y, -1.5, 1.5)
	if wish_dir.length_squared() > 0.0001:
		var desired := wish_dir.normalized() * p.effective_max_speed(ctx.body_scale)
		v = v.move_toward(desired, p.acceleration * 0.8 * delta)
	else:
		v.x = move_toward(v.x, 0.0, p.drag * delta)
		v.z = move_toward(v.z, 0.0, p.drag * delta)
	_elapsed += delta
	v.y += sin(_elapsed * 2.2) * 0.12 * delta * 10.0  # soft bob
	ctx.velocity = v
	ctx.move_and_slide()
