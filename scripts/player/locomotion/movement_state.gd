class_name MovementState
extends RefCounted
## MovementState — base for one locomotion behaviour.
##
## States are lightweight RefCounted strategies (not Nodes), so switching costs
## nothing in the scene tree and only ONE script (LocomotionController) runs
## _physics_process. Each state implements enter/exit/physics_step and reads
## everything it needs from its owner controller (`ctx`).
##
## Contract with LocomotionController:
##   * physics_step() must set ctx.velocity and call ctx.move_and_slide().
##   * States never touch gravity wells directly; the controller integrates
##     gravity acceleration BEFORE calling physics_step.

var id: int = LocomotionTypes.State.SWIMMING
var label: String = "Movement"
var ctx: LocomotionController

var _elapsed: float = 0.0


func _init(controller: LocomotionController) -> void:
	ctx = controller


func enter() -> void:
	pass


func exit() -> void:
	pass


func physics_step(_delta: float, _wish_dir: Vector3, _wish_up: float, _sprint: bool) -> void:
	pass


## Roll the body toward a banking angle proportional to turn input, while
## keeping the desired yaw. Allocation-light: builds one target Basis and slerps.
func apply_bank(forward_dir: Vector3, turn_input: float, max_angle: float, smoothness: float) -> void:
	var yaw := Vector2(forward_dir.x, forward_dir.z).angle_to(Vector2(0.0, -1.0))
	var roll := deg_to_rad(-turn_input * max_angle)
	var target := Basis.IDENTITY.rotated(Vector3.UP, yaw).rotated(Vector3.FORWARD, roll)
	ctx.pawn.global_transform.basis = Basis(ctx.pawn.global_transform.basis.get_rotation_quaternion().slerp(target.get_rotation_quaternion(), smoothness))
