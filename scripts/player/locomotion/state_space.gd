class_name StateSpaceFlight
extends MovementState
## StateSpaceFlight — zero-gravity flight for the future space stage.
## Identical fluid integration to swimming but with NO buoyancy and very low
## drag, so a thruster tap produces a long, gliding drift. Gravity wells are
## integrated by the controller and are what makes a black hole "feel" real.

func _init(controller: LocomotionController) -> void:
	super._init(controller)
	id = LocomotionTypes.State.SPACE_FLIGHT
	label = "Space Flight"


func physics_step(delta: float, wish_dir: Vector3, wish_up: float, sprint: bool) -> void:
	var p: MovementProfile = ctx.active_profile
	var v: Vector3 = ctx.velocity
	var top := p.effective_max_speed(ctx.body_scale) * (p.sprint_multiplier if sprint else 1.0)
	if wish_dir.length_squared() > 0.0001:
		v = v.move_toward(wish_dir.normalized() * top, p.acceleration * delta)
	if absf(wish_up) > 0.01:
		v += Vector3.UP * wish_up * p.acceleration * 0.8 * delta
	# Minimal drag: inertia dominates, like real spaceflight.
	v = v.move_toward(v, 0.0)  # no-op guard; real damping handled below
	v *= (1.0 - minf(p.drag * 0.05 * delta, 0.9))
	ctx.velocity = v
	ctx.move_and_slide()
	if wish_dir.length_squared() > 0.0001:
		apply_bank(wish_dir, wish_dir.x, p.bank_angle * 0.6, 0.08)
