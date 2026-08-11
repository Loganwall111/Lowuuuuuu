class_name StateClimb
extends MovementState
## StateClimb — vertical/overhead locomotion on a wall or ceiling. Activated by
## the controller when the EnvironmentProbe reports wall contact and the
## creature has unlocked CLIMBING. Gravity is cancelled and movement is
## projected onto the surface plane using the last collision normal.

func _init(controller: LocomotionController) -> void:
	super._init(controller)
	id = LocomotionTypes.State.CLIMBING
	label = "Climbing"


func physics_step(delta: float, wish_dir: Vector3, wish_up: float, _sprint: bool) -> void:
	var p: MovementProfile = ctx.active_profile
	var v: Vector3 = ctx.velocity
	# Cancel gravity while adhered.
	v.y = move_toward(v.y, 0.0, ProjectSettings.get_setting("physics/3d/default_gravity", 9.8) * delta)

	var normal: Vector3 = ctx.probe.surface_normal if ctx.probe != null else Vector3.UP
	# Build a tangent basis on the wall.
	var up := -normal
	var right := normal.cross(Vector3.UP)
	if right.length_squared() < 0.001:
		right = normal.cross(Vector3.RIGHT)
	right = right.normalized()
	var fwd := right.cross(normal).normalized()

	var move := fwd * wish_dir.z + right * wish_dir.x + up * wish_up
	if move.length_squared() > 0.0001:
		var desired := move.normalized() * p.effective_max_speed(ctx.body_scale) * 0.7
		v = v.move_toward(desired, p.acceleration * delta)
	else:
		v = v.move_toward(Vector3.ZERO, p.deceleration * delta)
	ctx.velocity = v
	ctx.move_and_slide()
