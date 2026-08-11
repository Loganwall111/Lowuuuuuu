class_name CameraRig
extends Node
## CameraRig — cinematic, multi-mode camera controller.
##
## WHY A RIG + RefCounted MODES (not one big node)
## The camera must support First Person, Third Person, Cinematic, Free, Inspect
## and (future) Space Flight without rewriting the pawn. Each mode is a small
## strategy object (camera_mode_*.gd) implementing enter/exit/update; the rig
## only owns the shared Camera3D, mouse-look state and transitions. Adding a
## mode = one new file + one registry line.
##
## The rig is decoupled from the pawn: it reads `target` (a Node3D) and asks it
## for a basis via get_camera_basis(); the pawn satisfies that contract. This
## is the same contract the LocomotionController uses, so one pawn interface
## drives both movement and camera.
##
## ATTACH THIS SCRIPT TO:
##   PlayerPawn (CharacterBody3D)
##   └── CameraRig (Node3D)   <-- here
##       └── Camera3D         (the actual viewport camera)
##       └── SpringArm3D (optional, used by third-person for collision)
##           └── (the Camera3D can live under the SpringArm for auto-collision)
##
## DEPENDENCIES: CameraMode* (scripts/camera/camera_mode_*.gd), CameraTypes,
## SettingsService, EventBus.

@export_group("Look")
@export var mouse_sensitivity: float = 0.0022
@export var invert_y: bool = false
@export var pitch_min_deg: float = -80.0
@export var pitch_max_deg: float = 80.0
@export var position_smoothing: float = 12.0

@export_group("Framing")
@export var third_person_distance: float = 6.0
@export var eye_height: float = 1.3
@export var head_bob_amplitude: float = 0.04
@export var head_bob_frequency: float = 1.8
@export var fov_base: float = 74.0
@export var fov_speed_gain: float = 12.0

@onready var camera: Camera3D = $Camera3D

var target: Node3D
var current_mode_id: int = CameraTypes.Mode.THIRD_PERSON
var _mode: RefCounted
var _yaw: float = 0.0
var _pitch: float = 0.0
var _creature_scale: float = 1.0
var _fov: float = 74.0
var _modes: Dictionary = {}
var _shaker: CameraShake
var _bob_phase: float = 0.0
var _capture_mouse: bool = true


func _ready() -> void:
	_shaker = CameraShake.new()
	_register_mode(CameraModeFirstPerson.new(self))
	_register_mode(CameraModeThirdPerson.new(self))
	_register_mode(CameraModeCinematic.new(self))
	_register_mode(CameraModeFree.new(self))
	_register_mode(CameraModeInspection.new(self))
	_register_mode(CameraModeSpaceFlight.new(self))
	_fov = fov_base
	if camera != null:
		camera.fov = _fov
	EventBus.camera_shake_requested.connect(add_shake)
	set_process(true)


func _register_mode(mode: RefCounted) -> void:
	if mode.has_method("mode_id"):
		_modes[mode.mode_id()] = mode


## The pawn calls this once it exists (it usually is the parent).
func bind_target(node: Node3D) -> void:
	target = node


func set_creature_scale(scale_value: float) -> void:
	_creature_scale = maxf(0.05, scale_value)


## Contract used by LocomotionController and the pawn for camera-relative input.
func get_camera_basis() -> Basis:
	if camera != null:
		return camera.global_transform.basis
	return Basis.IDENTITY


func cycle_mode() -> void:
	var order: Array[int] = CameraTypes.cycle_order()
	var idx := order.find(current_mode_id)
	idx = (idx + 1) % order.size()
	set_mode(order[idx])


func set_mode(id: int) -> void:
	if not _modes.has(id):
		return
	if _mode != null and _mode.has_method("exit"):
		_mode.exit()
	current_mode_id = id
	_mode = _modes[id]
	if _mode != null and _mode.has_method("enter"):
		_mode.enter()
	_apply_mesh_visibility(id)
	EventBus.camera_mode_changed.emit(id)


## Hides the creature's own mesh in first person so we don't see our face.
func _apply_mesh_visibility(id: int) -> void:
	if target == null:
		return
	var mr := target.get_node_or_null(^"MeshRoot")
	if mr != null:
		mr.visible = (id != CameraTypes.Mode.FIRST_PERSON)


## Used by CameraModeFirstPerson.enter() as a guard hook (kept for symmetry).
func mesh_root_hidden_on_fp() -> bool:
	return current_mode_id == CameraTypes.Mode.FIRST_PERSON


func _unhandled_input(event: InputEvent) -> void:
	if not _capture_mouse or not Input.mouse_mode == Input.MOUSE_MODE_CAPTURED:
		# Allow look only while captured (gameplay). UI uses the mouse freely.
		if not Input.mouse_mode == Input.MOUSE_MODE_CAPTURED:
			return
	if event is InputEventMouseMotion and _mode != null and _mode.has_method("on_mouse_motion"):
		_mode.on_mouse_motion(event.relative)


func _process(delta: float) -> void:
	if camera == null or target == null:
		return
	if _mode != null and _mode.has_method("update"):
		var speed_fraction: float = 0.0
		if target.has_method("get_speed_fraction"):
			speed_fraction = target.get_speed_fraction()
		_mode.update(delta, speed_fraction)

	# Speed-based FOV (cinematic sense of velocity).
	var target_fov := fov_base + fov_speed_gain * speed_fraction
	_fov = lerpf(_fov, target_fov, 1.0 - exp(-6.0 * delta))
	camera.fov = _fov + _shaker.get_offset().y

	# Apply shake offset to the camera's local position each frame.
	var shake := _shaker.get_offset()
	camera.position += Vector3(shake.x, 0.0, 0.0)

	_shaker.update(delta)


## Helpers shared by the modes ------------------------------------------------

func apply_yaw_pitch_from_mouse(motion: Vector2) -> void:
	var sens := mouse_sensitivity * SettingsService.mouse_sensitivity * 1000.0
	_yaw -= motion.x * sens
	_pitch -= motion.y * sens * (1.0 if not invert_y else -1.0)
	_pitch = clampf(_pitch, deg_to_rad(pitch_min_deg), deg_to_rad(pitch_max_deg))


func get_yaw_pitch() -> Vector2:
	return Vector2(_yaw, _pitch)


func set_yaw_pitch(yaw: float, pitch: float) -> void:
	_yaw = yaw
	_pitch = pitch


func add_shake(strength: float, duration: float) -> void:
	_shaker.add_trauma(strength, duration)


func _enter_tree() -> void:
	# Ensure the pawn binds itself as the target.
	if get_parent() is Node3D:
		target = get_parent() as Node3D
