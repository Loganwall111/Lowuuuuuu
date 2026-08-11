class_name SteeringKit
extends RefCounted
## SteeringKit — small, allocation-light boid helpers shared by every creature
## AI. Kept as static functions so any agent (fish, insect, future herd animal)
## gets consistent, tunable behaviour without copy-paste.
##
## All functions take a position + a list of neighbours and return a steering
## Vector3 (already scaled by the relevant weight if you pass it). The caller
## integrates the result into velocity.

## Pushes away from nearby neighbours so the swarm does not collapse to a point.
static func separation(pos: Vector3, neighbours: Array, radius: float) -> Vector3:
	var steer := Vector3.ZERO
	var n := 0
	for other in neighbours:
		if other == null:
			continue
		var d := pos.distance_to(other.global_position)
		if d > 0.0001 and d < radius:
			steer += (pos - other.global_position).normalized() / d
			n += 1
	if n > 0:
		steer /= n
	return steer


## Cohesion: drift toward the local centre of mass.
static func cohesion(pos: Vector3, neighbours: Array) -> Vector3:
	if neighbours.is_empty():
		return Vector3.ZERO
	var centre := Vector3.ZERO
	for other in neighbours:
		if other != null:
			centre += other.global_position
	centre /= neighbours.size()
	return (centre - pos)


## Alignment: match the average heading of neighbours.
static func alignment(velocities: Array) -> Vector3:
	if velocities.is_empty():
		return Vector3.ZERO
	var avg := Vector3.ZERO
	for v in velocities:
		avg += v
	return avg / velocities.size()


## Seek a target point.
static func seek(pos: Vector3, target: Vector3, weight: float = 1.0) -> Vector3:
	return (target - pos).normalized() * weight


## Flee from a point (used for predator avoidance).
static func flee(pos: Vector3, threat: Vector3, radius: float) -> Vector3:
	var d := pos.distance_to(threat)
	if d > radius or d < 0.0001:
		return Vector3.ZERO
	return (pos - threat).normalized() * (1.0 - d / radius)
