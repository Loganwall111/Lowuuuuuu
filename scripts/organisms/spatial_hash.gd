class_name SpatialHash
extends RefCounted
## SpatialHash — uniform-grid spatial index for thousands of motes/agents.
## Turns an O(n^2) neighbour search into O(1)-ish bucket lookups, which is what
## lets PlanktonField answer "eat everything near the player" without scanning
## every mote. Used for plankton consumption and could back creature flocking.
##
## Keyed by integer cell coordinates; cell size ~ consumption radius.

var cell_size: float = 4.0
var _buckets: Dictionary = {}


func _hash(pos: Vector3) -> Vector3i:
	return Vector3i(floori(pos.x / cell_size), floori(pos.y / cell_size), floori(pos.z / cell_size))


func insert(id: int, pos: Vector3) -> void:
	var key: Vector3i = _hash(pos)
	var bucket: Array = _buckets.get(key, [])
	bucket.append(id)
	_buckets[key] = bucket


func query_radius(center: Vector3, radius: float) -> Array[int]:
	var result: Array[int] = []
	var r := maxf(1, ceili(radius / cell_size))
	var base: Vector3i = _hash(center)
	for dx in range(-r, r + 1):
		for dy in range(-r, r + 1):
			for dz in range(-r, r + 1):
				var key := Vector3i(base.x + dx, base.y + dy, base.z + dz)
				var bucket: Array = _buckets.get(key, [])
				for id in bucket:
					result.append(id)
	return result


func remove(id: int, pos: Vector3) -> void:
	var key: Vector3i = _hash(pos)
	var bucket: Array = _buckets.get(key, [])
	bucket.erase(id)
	if bucket.is_empty():
		_buckets.erase(key)
