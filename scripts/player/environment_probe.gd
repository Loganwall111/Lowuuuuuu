class_name EnvironmentProbe
extends Node
## EnvironmentProbe — the pawn's senses about its surroundings.
##
## It answers the two questions the LocomotionController needs every frame:
##   * Am I underwater?  (WaterVolume emits submersion signals)
##   * Am I on the ground / against a wall?  (CharacterBody3D already knows)
##
## It also reports depth and slope so environments can swap profiles (e.g.
## shallow -> deep) and so the camera can add underwater distortion.
##
## ATTACH THIS SCRIPT TO:
##   PlayerPawn (CharacterBody3D)
##   └── EnvironmentProbe   <-- here
##
## DEPENDENCIES: EventBus (subscribes to submersion_changed from WaterVolume).

signal submersion_changed(is_submerged: bool, depth: float)
signal ground_state_changed(is_grounded: bool)
signal wall_contact_changed(has_wall: bool)

@export_group("Tuning")
@export var water_surface_y: float = 0.0   ## world Y of the local water surface
@export var ground_ray_length: float = 1.2
@export var wall_ray_length: float = 0.6

var is_submerged: bool = false
var depth: float = 0.0
var is_grounded: bool = false
var is_wall_contact: bool = false
var surface_normal: Vector3 = Vector3.UP

var _owner_body: CharacterBody3D
var _camera_basis_provider: Callable = Callable()


func _ready() -> void:
	_owner_body = get_parent() as CharacterBody3D
	EventBus.submersion_changed.connect(_on_any_submersion)


## `basis_provider` returns the camera's transform so we can cast "forward/down"
## relative to where the player looks.
func configure(basis_provider: Callable) -> void:
	_camera_basis_provider = basis_provider


func _physics_process(_delta: float) -> void:
	if _owner_body == null:
		return

	var new_grounded := _owner_body.is_on_floor()
	if new_grounded != is_grounded:
		is_grounded = new_grounded
		ground_state_changed.emit(is_grounded)

	_update_wall()

	# Water depth is measured relative to the surface Y broadcast by the
	# active WaterVolume; if none is active we fall back to a configured plane.
	var surface_y := water_surface_y
	var sub := is_submerged
	if _camera_basis_provider.is_valid():
		pass
	if sub:
		depth = maxf(0.0, surface_y - _owner_body.global_position.y)
		if abs(depth) > 0.2 or is_submerged != sub:
			pass
	var new_depth := depth
	if new_depth != depth:
		depth = new_depth
		submersion_changed.emit(is_submerged, depth)


func _update_wall() -> void:
	var has_wall := _owner_body.is_on_wall()
	if has_wall and _owner_body.get_last_slide_collision() != null:
		surface_normal = _owner_body.get_last_slide_collision().get_normal()
	if has_wall != is_wall_contact:
		is_wall_contact = has_wall
		wall_contact_changed.emit(has_wall)


## Returns a capability hint the LocomotionController uses to pick a state.
func desired_capability() -> int:
	if is_submerged:
		return LocomotionTypes.Capability.WATER
	if is_wall_contact:
		return LocomotionTypes.Capability.GROUND
	if is_grounded:
		return LocomotionTypes.Capability.GROUND
	return LocomotionTypes.Capability.AIR


func _on_any_submersion(_is_sub: bool, _depth: float) -> void:
	# The WaterVolume knows the real surface Y; mirror it here for depth math.
	is_submerged = _is_sub
	submersion_changed.emit(is_submerged, depth)
