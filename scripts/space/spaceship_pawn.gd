class_name SpaceshipPawn
extends CharacterBody3D
## SpaceshipPawn — the playable craft for the future "Civilisation -> Space" stage.
## Reuses the SAME architecture as PlayerPawn so biology->machines is additive:
## a CameraRig, GravityWellRegistry pull (same code as debris/asteroids) and the
## camera/HUD contract (get_camera_basis / get_speed_fraction). Navigable prototype
## with thrust, pitch/yaw/roll and gravity-well interaction; combat/docking/FTL slot
## into the same hooks later.
##
## ATTACH THIS SCRIPT TO: SpaceshipPawn (CharacterBody3D)
##   ├── CollisionShape3D
##   ├── MeshRoot (Node3D) -> ship model
##   ├── CameraRig (scripts/camera/camera_rig.gd)
##   └── ThrusterVFX (GPUParticles3D) [optional]

@export var thrust: float = 60.0
@export var boost_multiplier: float = 2.2
@export var turn_rate: float = 1.6
@export var max_speed: float = 220.0
@export var hull: float = 200.0

@onready var camera_rig: Node = $CameraRig
@onready var mesh_root: Node3D = $MeshRoot

var velocity: Vector3 = Vector3.ZERO
var _hull: float = 200.0
var _dead: bool = false

func _ready() -> void:
	collision_layer = 2
	collision_mask = 1 | 7 | 8
	_hull = hull
	if mesh_root != null and mesh_root.get_child_count() == 0:
		_build_fallback_hull()
	if camera_rig != null and camera_rig.has_method("bind_target"):
		camera_rig.bind_target(self)
	set_process(true)

func _build_fallback_hull() -> void:
	var body := MeshInstance3D.new()
	var cone := CylinderMesh.new()
	cone.top_radius = 0.2
	cone.bottom_radius = 1.2
	cone.height = 3.0
	body.mesh = cone
	body.rotation_degrees.x = 90
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.7, 0.75, 0.8)
	mat.metallic = 0.8
	mat.roughness = 0.3
	body.material_override = mat
	mesh_root.add_child(body)

func _physics_process(delta: float) -> void:
	if _dead:
		return
	var forward := -global_transform.basis.z
	if Input.is_action_pressed(&"sprint"):
		forward *= boost_multiplier
	_throttle = Input.get_axis(&"move_down", &"move_up")
	velocity += forward * _throttle * thrust * delta
	if Input.is_action_pressed(&"move_left"):
		rotate_object_local(Vector3.UP, turn_rate * delta)
	if Input.is_action_pressed(&"move_right"):
		rotate_object_local(Vector3.UP, -turn_rate * delta)
	if Input.is_action_pressed(&"consume"):
		rotate_object_local(Vector3.RIGHT, turn_rate * 0.7 * delta)
	if Input.is_action_pressed(&"interact"):
		rotate_object_local(Vector3.RIGHT, -turn_rate * 0.7 * delta)
	var well := get_tree().get_first_node_in_group(&"gravity_well_registry")
	if well != null and well.has_method("sample_acceleration"):
		velocity += well.sample_acceleration(global_position, self) * delta
	velocity = velocity.limit_length(max_speed)
	move_and_slide()

func get_camera_basis() -> Basis:
	if camera_rig != null and camera_rig.has_method("get_camera_basis"):
		return camera_rig.get_camera_basis()
	return global_transform.basis

func get_speed_fraction() -> float:
	return clampf(velocity.length() / max_speed, 0.0, 1.0)

func take_damage(amount: float) -> void:
	_hull -= amount
	if _hull <= 0.0 and not _dead:
		_dead = true
		EventBus.toast_requested.emit("Hull breached", &"warn")
		EventBus.player_damaged.emit(amount, "hull")
