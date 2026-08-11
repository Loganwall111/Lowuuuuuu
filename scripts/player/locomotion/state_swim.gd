class_name StateSwim
extends MovementState
## StateSwim — organic, medium-resisted 3D movement for water/microscopic life.
##
## The feel goal is "a living thing pushing through fluid", not "a flying
## camera": acceleration is strong along input, drag is quadratic (so fast
## motion bleeds off naturally), buoyancy gently opposes gravity, and a subtle
## body bank/undulation sells the creature. Gravity wells are integrated by the
## controller and felt here as a net acceleration on `ctx.velocity`.

func _init(controller: LocomotionController) -> void:
	super._init(controller)
	id = LocomotionTypes.State.SWIMMING
	label = "Swimming"


func enter() -> void:
	ctx.body.velocity = ctx.body.velocity * 0.6


func physics_step(delta: float, wish_dir: Vector3, wish_up: float, _sprint: bool) -> void:
	var p: MovementProfile = ctx.active_profile
	var v: Vector3 = ctx.velocity

	# Net "up" from buoyancy + gravity.
	var gravity := ProjectSettings.get_setting("physics/3d/default_gravity", 9.8) * p.gravity_scale
	var buoyancy_accel := gravity * (p.buoyancy - 1.0)   # buoyancy>1 => floats up
	v.y += buoyancy_accel * delta

	# Vertical swimming input, slightly damped when fighting buoyancy.
	var vert := wish_up
	if wish_up > 0.0:
		vert *= 1.0
	v.y += vert * p.acceleration * 0.9 * delta

	# Horizontal acceleration from input (camera-relative).
	if wish_dir.length_squared() > 0.0001:
		var desired := wish_dir.normalized() * p.effective_max_speed(ctx.body_scale)
		var accel := p.acceleration
		v = v.move_toward(desired, accel * delta)
	else:
		# Drag: stronger in denser medium, quadratic for a fluid feel.
		var speed := v.length()
		if speed > 0.0001:
			var drag_coeff := p.drag * p.medium_density
			var decel := drag_coeff * speed * speed
			v = v.move_toward(Vector3.ZERO, decel * delta)

	# Hard clamp on vertical so buoyancy + input never run away.
	v.y = clampf(v.y, -p.effective_max_speed(ctx.body_scale), p.effective_max_speed(ctx.body_scale) * 0.9)

	ctx.velocity = v
	ctx.move_and_slide()

	# Creature-like bank + slow undulation.
	var turn := wish_dir.x
	if wish_dir.length_squared() > 0.0001:
		apply_bank(wish_dir, turn, p.bank_angle, 0.12)
	_elapsed += delta
	if p.undulation > 0.0:
		var wag := sin(_elapsed * 6.0) * p.undulation * 0.15
		ctx.pawn.rotate_object_local(Vector3.Z, wag * delta)
