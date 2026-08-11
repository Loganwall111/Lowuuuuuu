extends Node
## PoolService — generic PackedScene instance pool (AUTOLOAD: "PoolService").
##
## WHY
## Feeding spawns and consumes hundreds of small entities per minute (food
## motes, bite VFX, bubble bursts). instantiate()/queue_free() churn causes
## frame spikes from allocation and from Godot rebuilding the node's internal
## state. Pooling reuses the instance and only resets what matters.
##
## CONTRACT (both optional)
##     func _on_pool_acquired() -> void   # re-arm timers, reset visuals
##     func _on_pool_released() -> void   # stop timers, clear references
##
## Pooled nodes are removed from the active scene while parked, so they cost no
## processing, no physics and no draw calls.
##
## DEPENDENCIES: none.

const DEFAULT_MAX_PER_SCENE := 512

var _pools: Dictionary = {}      # scene_path -> Array[Node]
var _origin: Dictionary = {}     # node -> scene_path
var _parked: Node = null

var stats: Dictionary = {"acquired": 0, "reused": 0, "released": 0, "parked": 0}


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_parked = Node.new()
	_parked.name = "ParkedInstances"
	add_child(_parked)


## Instantiates `count` copies up front, typically during a loading screen.
func prewarm(scene: PackedScene, count: int) -> void:
	if scene == null:
		return
	var key := _key_for(scene)
	var pool: Array = _pools.get(key, [])
	for _i in count:
		if pool.size() >= DEFAULT_MAX_PER_SCENE:
			break
		var node: Node = scene.instantiate()
		_origin[node] = key
		_parked.add_child(node)
		_deactivate(node)
		pool.append(node)
	_pools[key] = pool
	stats["parked"] = _count_parked()


## Returns a live instance parented to `parent` (or to the pool root if null).
func acquire(scene: PackedScene, parent: Node = null) -> Node:
	if scene == null:
		return null
	var key := _key_for(scene)
	var pool: Array = _pools.get(key, [])
	var node: Node = null
	while node == null and not pool.is_empty():
		node = pool.pop_back()
		if not is_instance_valid(node):
			node = null
	if node == null:
		node = scene.instantiate()
		_origin[node] = key
		stats["acquired"] += 1
	else:
		stats["reused"] += 1
	_pools[key] = pool

	if node.get_parent() != null:
		node.get_parent().remove_child(node)
	var target: Node = parent if parent != null else _parked
	target.add_child(node)
	_activate(node)
	if node.has_method("_on_pool_acquired"):
		node.call("_on_pool_acquired")
	return node


## Parks a node for later reuse. Safe to call on non-pooled nodes (they are
## simply freed).
func release(node: Node) -> void:
	if not is_instance_valid(node):
		return
	if node.has_method("_on_pool_released"):
		node.call("_on_pool_released")
	if not _origin.has(node):
		node.queue_free()
		return
	var key: String = _origin[node]
	var pool: Array = _pools.get(key, [])
	if pool.size() >= DEFAULT_MAX_PER_SCENE:
		_origin.erase(node)
		node.queue_free()
		return
	if node.get_parent() != null:
		node.get_parent().remove_child(node)
	_parked.add_child(node)
	_deactivate(node)
	pool.append(node)
	_pools[key] = pool
	stats["released"] += 1
	stats["parked"] = _count_parked()


## Frees every parked instance. Call between levels to release memory.
func flush() -> void:
	for key in _pools:
		for node in _pools[key]:
			if is_instance_valid(node):
				_origin.erase(node)
				node.queue_free()
	_pools.clear()
	stats["parked"] = 0


func _activate(node: Node) -> void:
	node.process_mode = Node.PROCESS_MODE_INHERIT
	if node is Node3D:
		(node as Node3D).visible = true


func _deactivate(node: Node) -> void:
	node.process_mode = Node.PROCESS_MODE_DISABLED
	if node is Node3D:
		(node as Node3D).visible = false


func _key_for(scene: PackedScene) -> String:
	return scene.resource_path if not scene.resource_path.is_empty() else str(scene.get_instance_id())


func _count_parked() -> int:
	var total := 0
	for key in _pools:
		total += (_pools[key] as Array).size()
	return total
