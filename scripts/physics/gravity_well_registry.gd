class_name GravityWellRegistry
extends Node
## GravityWellRegistry — the single accumer of active GravityWell3D fields.
##
## LocomotionController (player), GravityAffected (debris/asteroids/spaceships)
## and any AI call sample_acceleration(world_point, self) ONCE per tick and get
## the summed pull of every well. This keeps "black-hole gravity" out of every
## movement script and in one shared, testable place.
##
## Created on demand by the first GravityWell3D in the scene; never authored by
## hand. Group: "gravity_well_registry".

var _wells: Array[GravityWell3D] = []


func _ready() -> void:
	add_to_group(&"gravity_well_registry")


func register_well(well: GravityWell3D) -> void:
	if well != null and not _wells.has(well):
		_wells.append(well)


func unregister_well(well: GravityWell3D) -> void:
	_wells.erase(well)


## Summed acceleration at a world point from all active wells.
func sample_acceleration(point: Vector3, _requester: Object = null) -> Vector3:
	var total := Vector3.ZERO
	for well in _wells:
		if is_instance_valid(well) and well.active:
			total += well.sample_acceleration(point)
	return total


func well_count() -> int:
	return _wells.size()
