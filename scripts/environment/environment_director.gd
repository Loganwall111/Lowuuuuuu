class_name EnvironmentDirector
extends Node
## EnvironmentDirector — owns the live WorldEnvironment + sun and applies
## EnvironmentProfiles with smooth cross-fades. Every biome scene contains one
## (its "art director"); it is NOT an autoload.
##
## It also handles the signature AAA touch: submersion swaps the whole mood to
## an underwater profile and tints the screen via a lightweight ColorRect
## overlay (so we never pay for a second full Environment during a dive).
##
## ATTACH THIS SCRIPT TO:
##   BiomeScene (Node3D)
##   └── EnvironmentDirector (Node)   <-- here
##       └── WorldEnvironment (Node)  (env.resource assigned)
##       └── Sun (DirectionalLight3D)
##       └── UnderwaterOverlay (ColorRect) [optional; created in code]
##
## DEPENDENCIES: EnvironmentProfile, EventBus.

@export_group("Profiles")
@export var surface_profile: EnvironmentProfile
@export var underwater_profile: EnvironmentProfile

@export_group("Tuning")
@export var blend_time: float = 1.2
@export var underwater_tint_strength: float = 0.45

var world_env: WorldEnvironment
var sun: DirectionalLight3D
var _overlay: ColorRect
var _current: Dictionary = {}
var _from: Dictionary = {}
var _to: Dictionary = {}
var _blend_t: float = 1.0
var _is_underwater: bool = false


func _ready() -> void:
	world_env = get_parent().get_node_or_null(^"WorldEnvironment") as WorldEnvironment
	sun = get_parent().get_node_or_null(^"Sun") as DirectionalLight3D
	_build_overlay()
	if surface_profile != null:
		_apply_instant(surface_profile)
	EventBus.submersion_changed.connect(_on_submersion)


func _build_overlay() -> void:
	_overlay = ColorRect.new()
	_overlay.name = "UnderwaterOverlay"
	_overlay.color = Color(0.03, 0.22, 0.3, 0.0)
	_overlay.set_anchors_preset(Control.PRESET_FULL_RECT)
	_overlay.mouse_filter = Control.MOUSE_FILTER_IGNORE
	_overlay.material = preload_underwater_material()
	_overlay.visible = true
	# Draw above the 3D world but below UI: a CanvasLayer on top of the scene.
	var layer := CanvasLayer.new()
	layer.layer = 5
	layer.add_child(_overlay)
	if get_parent() is Node:
		get_parent().add_child(layer)


func preload_underwater_material() -> CanvasItemMaterial:
	var mat := CanvasItemMaterial.new()
	mat.light_mode = CanvasItemMaterial.LIGHT_MODE_UNSHADED
	return mat


func _apply_instant(profile: EnvironmentProfile) -> void:
	if world_env != null and world_env.environment != null:
		profile.apply(world_env.environment, sun)
	_current = profile.gather()
	_blend_t = 1.0


func set_profile(profile: EnvironmentProfile, time: float = -1.0) -> void:
	if profile == null or world_env == null or world_env.environment == null:
		return
	_from = _current.duplicate()
	_to = profile.gather()
	_blend_t = 0.0
	_blend_duration = maxf(0.01, time if time > 0.0 else blend_time)


var _blend_duration: float = 1.2


func _on_submersion(is_submerged: bool, _depth: float) -> void:
	if is_submerged == _is_underwater:
		return
	_is_underwater = is_submerged
	if is_submerged and underwater_profile != null:
		set_profile(underwater_profile)
		_fade_overlay(underwater_tint_strength)
		EventBus.environment_profile_requested.emit(underwater_profile, blend_time)
	elif not is_submerged and surface_profile != null:
		set_profile(surface_profile)
		_fade_overlay(0.0)
		EventBus.environment_profile_requested.emit(surface_profile, blend_time)


func _fade_overlay(alpha: float) -> void:
	if _overlay == null:
		return
	var tw := create_tween()
	tw.tween_property(_overlay, "color:a", alpha, blend_time)


func _process(delta: float) -> void:
	if _blend_t >= 1.0:
		return
	_blend_t = minf(1.0, _blend_t + delta / _blend_duration)
	var eased := clampf(_blend_t, 0.0, 1.0)
	eased = eased * eased * (3.0 - 2.0 * eased)  # smoothstep
	_current = EnvironmentProfile.lerp_values(_from, _to, eased)
	if world_env != null:
		world_env.environment.apply_gathered(world_env.environment, sun, _current)
