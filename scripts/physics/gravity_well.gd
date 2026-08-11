class_name GravityWell3D
extends Node3D
## GravityWell3D — a reusable, data-driven gravitational field.
##
## DESIGN GOALS (from the brief)
##   * Configurable strength, influence radius and falloff.
##   * Stable: force cannot explode near r=0 (softened denominator).
##   * Reusable: affects the player, creatures, debris, asteroids AND
##     spaceships identically via the shared GravityWellRegistry.
##   * Decoupled: it only SAMPLES/ADDS acceleration; it never decides how a body
##     moves. Movement code calls sample_acceleration() and integrates it.
##
## ATTACH THIS SCRIPT TO:
##   AnyNode3D (usually a black hole / planet / star root)
##   └── GravityWell3D   <-- here
##       └── (visual: BlackHole.tscn, planet mesh, etc.)
##
## PERFORMANCE: a well only does work when something asks it for acceleration
## (pull, not push). The registry caches the active set, so N wells cost O(N)
## per sample, not per frame for every body.

signal entered_well(body: Node3D)
signal exited_well(body: Node3D)
signal event_horizon_crossed(body: Node3D)

@export_group("Field")
@export var gravity_strength: float = 120.0
@export var influence_radius: float = 220.0
@export_enum("Inverse Square", "Inverse Linear", "Linear", "Gaussian") var falloff_type: int = 0
@export var max_force: float = 400.0       ## clamp prevents instability
@export var softening: float = 2.5          ## avoids 1/0 at the centre
@export var event_horizon_radius: float = 3.0

@export_group("Behaviour")
@export var active: bool = true
@export var attract: bool = true            ## false => repulsive (solar wind, etc.)

var _registry: Node


func _ready() -> void:
	add_to_group(&"gravity_well")
	_registry = _ensure_registry()
	if _registry != null and _registry.has_method("register_well"):
		_registry.register_well(self)


func _ensure_registry() -> Node:
	var reg := get_tree().get_first_node_in_group(&"gravity_well_registry")
	if reg != null:
		return reg
	reg = Node.new()
	reg.name = "GravityWellRegistry"
	reg.add_to_group(&"gravity_well_registry")
	# The registry must outlive this well's subtree but not pause with it.
	if get_tree().root != null:
		get_tree().root.add_child(reg)
	return reg


## Acceleration (m/s^2) a body at `point` would feel. Adds itself to the
## registry on first use so the registry's sample_acceleration can batch.
func sample_acceleration(point: Vector3) -> Vector3:
	if not active:
		return Vector3.ZERO
	var to_center: Vector3 = global_position - point
	var dist: float = to_center.length()
	if dist > influence_radius:
		return Vector3.ZERO
	var dir: Vector3 = to_center / maxf(dist, 0.0001)
	var mag: float = _falloff(dist)
	mag = minf(mag, max_force)
	return dir * mag * (1.0 if attract else -1.0)


func _falloff(dist: float) -> float:
	var d := maxf(dist, 0.0)
	match falloff_type:
		0:  # Inverse square (softened)
			var r := d + softening
			return gravity_strength / (r * r)
		1:  # Inverse linear
			return gravity_strength / (d + softening)
		2:  # Linear (pull grows with distance up to radius)
			return gravity_strength * (d / maxf(influence_radius, 0.001))
		3:  # Gaussian
			var sigma := influence_radius * 0.4
			return gravity_strength * exp(-(d * d) / (2.0 * sigma * sigma))
	return 0.0


func _exit_tree() -> void:
	if _registry != null and is_instance_valid(_registry):
		_registry.unregister_well(self)
