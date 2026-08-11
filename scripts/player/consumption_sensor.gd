class_name ConsumptionSensor
extends Area3D
## ConsumptionSensor — the player's "mouth". Detects edibles, validates them
## against the organism's DietProfile, and triggers consumption.
##
## WHY AREA3D
## Detection is a spatial query, and Area3D gives us exactly that via
## get_overlapping_areas()/bodies() with no per-target _process. We combine it
## with a fast array scan + distance sort so the player eats the NEAREST valid
## prey, not an arbitrary one.
##
## Plankton fields (thousands of motes) are NOT physics bodies; they register
## themselves with this sensor via register_plankton_field() and are consumed
## through a single spatial query, keeping a dense food cloud essentially free.
##
## ATTACH THIS SCRIPT TO:
##   PlayerPawn (CharacterBody3D)
##   └── ConsumptionSensor (Area3D)   <-- here
##       └── CollisionShape3D (sphere, radius ~ interaction range)
##
## The sensor must be on the PlayerSensor collision layer so edibles placed on
## the Edible layer can overlap it.

signal consumed(payload: NutritionPayload, target: Node)

@export_group("Tuning")
@export var consume_angle_deg: float = 55.0   ## only eat what's roughly ahead when aimed
@export var allow_omnidirectional: bool = true
@export var max_targets_per_scan: int = 24

var _owner_pawn: Node3D
var _diet: DietProfile
var _consumer_mass_getter: Callable = Callable()   ## () -> float
var _ability_checker: Callable = Callable()         ## (StringName) -> bool
var _plankton_fields: Array = []
var _auto_mode: bool = false


func _ready() -> void:
	_owner_pawn = get_parent() as Node3D
	area_entered.connect(_on_area_entered)
	body_entered.connect(_on_body_entered)
	_auto_mode = _diet.auto_consume if _diet != null else false


## Called by the pawn once OrganismData is resolved.
func configure(organism: OrganismData, mass_getter: Callable, ability_checker: Callable) -> void:
	_diet = organism.diet
	_consumer_mass_getter = mass_getter
	_ability_checker = ability_checker
	_auto_mode = _diet != null and _diet.auto_consume


## PlanktonField calls this on spawn so the sensor can consume motes through a
## cheap spatial query instead of physics.
func register_plankton_field(field: Node) -> void:
	if field != null and not _plankton_fields.has(field):
		_plankton_fields.append(field)


func unregister_plankton_field(field: Node) -> void:
	_plankton_fields.erase(field)


## Called by the pawn when the player presses "consume" (manual feeding) and
## continuously when the diet is set to auto_consume.
func try_consume_nearest() -> bool:
	var best_target: Node = null
	var best_score := INF
	var best_payload: NutritionPayload = null

	# 1) Physics-based edibles (prey, plants, carrion).
	var candidates: Array = get_overlapping_areas()
	for area in get_overlapping_bodies():
		candidates.append(area)
	for candidate in candidates:
		var edible := _find_edible(candidate)
		if edible == null:
			continue
		var payload := _validate(edible)
		if payload == null:
			continue
		var score := _owner_pawn.global_position.distance_squared_to(edible.global_position)
		if score < best_score:
			best_score = score
			best_target = edible
			best_payload = payload

	# 2) Plankton fields (no physics bodies).
	if best_target == null:
		for field in _plankton_fields:
			if field == null or not field.has_method("consume_nearest"):
				continue
			var result: Dictionary = field.consume_nearest(_owner_pawn.global_position, _diet, _consumer_mass())
			if result.is_empty() or not result.get("ok", false):
				continue
			var motes := int(result.get("count", 0))
			if motes <= 0:
				continue
			var payload := NutritionPayload.new()
			payload.mass = result.get("mass", 0.0)
			payload.energy = result.get("energy", 0.0)
			payload.evolution_points = result.get("evolution_points", 0.0)
			payload.tags = PackedStringArray([&"plankton"])
			payload.source_name = "plankton"
			_on_consumed(payload, _owner_pawn)
			return true

	if best_target != null and best_payload != null:
		EventBus.prey_consumed.emit(_payload_to_dict(best_payload, best_target))
		_on_consumed(best_payload, best_target)
		return true
	return false


func _validate(edible: EdibleComponent) -> NutritionPayload:
	if not edible.is_alive():
		return null
	var mass := _consumer_mass()
	if _diet == null:
		return null
	if _diet.required_ability != &"" and _ability_checker.is_valid():
		if not _ability_checker.call(_diet.required_ability):
			EventBus.consumption_rejected.emit(&"needs_ability", edible.source_name)
			return null
	if not _diet.can_eat(mass, edible.provided_mass, edible.tags):
		EventBus.consumption_rejected.emit(&"too_big", edible.source_name)
		return null
	if not allow_omnidirectional and not _is_ahead(edible.global_position):
		return null
	return edible.try_consume(_owner_pawn)


func _on_consumed(payload: NutritionPayload, target: Node) -> void:
	consumed.emit(payload, target)


func _is_ahead(world_pos: Vector3) -> bool:
	var to_target := (world_pos - _owner_pawn.global_position).normalized()
	var facing := -_owner_pawn.global_transform.basis.z
	return facing.dot(to_target) >= cos(deg_to_rad(consume_angle_deg))


func _find_edible(node: Node) -> EdibleComponent:
	if node == null:
		return null
	var found := node.get_node_or_null(^".")  # self
	var edible := node as EdibleComponent
	if edible != null:
		return edible
	return node.find_child("EdibleComponent", true, false) as EdibleComponent


func _consumer_mass() -> float:
	if _consumer_mass_getter.is_valid():
		return _consumer_mass_getter.call()
	return 1.0


func _payload_to_dict(payload: NutritionPayload, target: Node) -> Dictionary:
	return {
		"mass": payload.mass,
		"energy": payload.energy,
		"evolution_points": payload.evolution_points,
		"tags": payload.tags,
		"position": target.global_position,
		"source_name": payload.source_name,
	}


func _on_area_entered(_area: Area3D) -> void:
	if _auto_mode:
		try_consume_nearest()


func _on_body_entered(_body: Node) -> void:
	if _auto_mode:
		try_consume_nearest()
