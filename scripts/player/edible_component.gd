class_name EdibleComponent
extends Node
## EdibleComponent — attached to ANY consumable (prey creature, food mote,
## plant, carrion). It advertises what the actor yields and how it dies.
##
## The ConsumptionSensor never touches an entity's internals; it calls
## `try_consume(consumer)` which returns null if the entity refuses (e.g. it is
## protected, or already dead), otherwise a NutritionPayload.
##
## GROWTH SCALING: every edible carries a `mass`. The player's DietProfile uses
## it to decide whether the prey is small enough to eat. This is what makes a
## tiny larva unable to swallow a whale — and able to, later.
##
## ATTACH THIS SCRIPT TO:
##   AnyNode (the root of a prey/plant/mote scene)
##   └── Node3D / Area3D / RigidBody3D  (whatever the prefab is)
##
## Required child (optional but recommended): a node used as the "death
## anchor" so the dies() effect spawns at the right place. If omitted, the
## component's own global transform is used.

signal consumed(consumer: Node)

@export_group("Yield")
@export var provided_mass: float = 0.05
@export var provided_energy: float = 4.0
@export var provided_evolution_points: float = 1.0
@export var tags: PackedStringArray = [&"plankton"]
@export var source_name: String = "organic matter"

@export_group("Lifecycle")
@export var return_to_pool: bool = true     ## if pooled, release() instead of free()
@export var fade_out_seconds: float = 0.25
@export var spawn_burst: PackedScene = null  ## optional bite/VFX prefab

var _alive: bool = true


func try_consume(consumer: Node) -> NutritionPayload:
	if not _alive:
		return null
	_alive = false
	var payload := NutritionPayload.new()
	payload.mass = provided_mass
	payload.energy = provided_energy
	payload.evolution_points = provided_evolution_points
	payload.tags = tags
	payload.source_name = source_name

	if spawn_burst != null:
		var burst := spawn_burst.instantiate()
		burst.global_transform = get_visual_global_transform()
		if burst is Node3D and get_tree().current_scene != null:
			get_tree().current_scene.add_child(burst)

	consumed.emit(consumer)
	_die()
	return payload


func is_alive() -> bool:
	return _alive


## Force-removes the entity (used by AI death, despawns, cleanup).
func kill() -> void:
	if not _alive:
		return
	_alive = false
	_die()


func _die() -> void:
	var owner_node := get_parent()
	if owner_node == null:
		return
	if return_to_pool and PoolService != null:
		# Visual fade then release back to the pool for reuse.
		_fade_then_release(owner_node)
	else:
		owner_node.queue_free()


func _fade_then_release(node: Node) -> void:
	if not (node is Node3D):
		PoolService.release(node)
		return
	var spatial := node as Node3D
	var tween := spatial.create_tween()
	tween.tween_property(spatial, "scale", Vector3.ZERO, fade_out_seconds).set_ease(Tween.EASE_IN)
	tween.tween_callback(PoolService.release.bind(node))


func get_visual_global_transform() -> Transform3D:
	return get_parent().global_transform if get_parent() != null else global_transform
