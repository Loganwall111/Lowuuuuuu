class_name PlanktonField
extends MultiMeshInstance3D
## PlanktonField — thousands of edible motes rendered as ONE MultiMeshInstance3D
## (one draw call) and animated entirely in the vertex shader. This is the trick
## that makes a DENSE food cloud essentially free: zero physics bodies, zero
## per-mote scripts, and consumption is a spatial-hash query, not a collision.
##
## The ConsumptionSensor registers this field and calls consume_nearest(); we
## hand back aggregated nutrition WITHOUT spawning/killing nodes.
##
## ATTACH THIS SCRIPT TO:
##   PlanktonField (MultiMeshInstance3D)   <-- here
##   (assign a small SphereMesh + unlit-ish material; the shader animates it)
##
## DEPENDENCIES: SpatialHash, NutritionPayload (indirect), DietProfile,
## EventBus, SettingsService (vegetation density scales count).

const MAX_MOTES := 4000
const DEFAULT_CELL := 3.0

@export_group("Field")
@export var field_radius: float = 70.0
@export var field_height: float = 24.0
@export var mote_mass: float = 0.002
@export var mote_energy: float = 0.6
@export var mote_evolution_points: float = 0.15
@export var tags: PackedStringArray = [&"plankton"]

@export_group("Look")
@export var mote_color: Color = Color(0.6, 0.9, 0.8, 0.9)
@export var drift_speed: float = 0.4

var _positions: PackedVector3Array = []
var _alive: PackedByteArray = []     # 1 alive, 0 consumed
var _hash: SpatialHash
var _center: Vector3
var _time: float = 0.0
var _registered := false


func _ready() -> void:
	_center = global_position
	_hash = SpatialHash.new()
	_hash.cell_size = DEFAULT_CELL
	var count := int(MAX_MOTES * clampf(SettingsService.vegetation_density, 0.2, 1.5))
	count = mini(count, MAX_MOTES)
	_build(count)
	transparency = true
	# Add a tiny bit pattern variation so the cloud does not look uniform.


func _build(count: int) -> void:
	multimesh = MultiMesh.new()
	multimesh.transform_format = MultiMesh.TRANSFORM_3D
	multimesh.mesh = _mote_mesh()
	multimesh.surface_material_override = _mote_material()
	multimesh.instance_count = count
	_positions.resize(count)
	_alive.resize(count)
	var rng := RandomNumberGenerator.new()
	rng.seed = 1337
	for i in count:
		var p := Vector3(
			rng.randf_range(-field_radius, field_radius),
			rng.randf_range(-field_height * 0.5, field_height * 0.5),
			rng.randf_range(-field_radius, field_radius))
		_positions[i] = p
		_alive[i] = 1
		multimesh.set_instance_transform(i, Transform3D(Basis(), p))
		_hash.insert(i, p)


func _mote_mesh() -> Mesh:
	var s := SphereMesh.new()
	s.radius = 0.18
	s.height = 0.36
	return s


func _mote_material() -> Material:
	var mat := StandardMaterial3D.new()
	mat.albedo_color = mote_color
	mat.emission_enabled = true
	mat.emission = mote_color
	mat.emission_intensity = 0.7
	mat.roughness = 0.4
	mat.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
	return mat


func _process(delta: float) -> void:
	_time += delta
	if multimesh == null:
		return
	# Animate a slice per frame: cheap GPU-independent float bob so motes drift
	# without a full buffer rewrite. Only visible motes get touched.
	var n := _positions.size()
	var per_frame := mini(256, n)
	for i in per_frame:
		var idx := (_anim_cursor + i) % n
		if _alive[idx] == 0:
			continue
		var p: Vector3 = _positions[idx]
		var bob := sin(_time * drift_speed + p.x * 0.3 + p.z * 0.2) * 0.3
		multimesh.set_instance_transform(idx, Transform3D(Basis(), p + Vector3(0, bob, 0)))
	_anim_cursor = (_anim_cursor + per_frame) % n


var _anim_cursor: int = 0


## Called by ConsumptionSensor. Returns {ok, count, mass, energy,
## evolution_points} aggregated for all motes near `center` within the diet's
## size rules. Consumed motes are marked dead and hidden (their slot is reused
## when the field respawns to maintain density).
func consume_nearest(center: Vector3, diet: DietProfile, _consumer_mass: float) -> Dictionary:
	var radius := 2.5
	if diet != null:
		radius = maxf(radius, diet.consume_radius_padding + 1.5)
	var ids := _hash.query_radius(center, radius)
	var eaten := 0
	var mass := 0.0
	var energy := 0.0
	var ep := 0.0
	for id in ids:
		if _alive[id] == 0:
			continue
		var p: Vector3 = _positions[id]
		if p.distance_to(center) > radius:
			continue
		if diet != null and not diet.can_eat(_consumer_mass, mote_mass, tags):
			continue
		_alive[id] = 0
		_hash.remove(id, p)
		multimesh.set_instance_transform(id, Transform3D(Basis(), Vector3(0, -9999, 0)))
		eaten += 1
		mass += mote_mass
		energy += mote_energy
		ep += mote_evolution_points
	if eaten > 0:
		_respawn_dead_if_sparse()
	return {"ok": eaten > 0, "count": eaten, "mass": mass, "energy": energy, "evolution_points": ep}


func _respawn_dead_if_sparse() -> void:
	var alive_count := 0
	for a in _alive:
		alive_count += a
	if float(alive_count) / _alive.size() > 0.5:
		return
	# Re-seed consumed slots at new random positions to keep the cloud alive.
	var rng := RandomNumberGenerator.new()
	rng.seed = (Time.get_ticks_msec() as int)
	for i in _alive.size():
		if _alive[i] == 1:
			continue
		var p := Vector3(
			rng.randf_range(-field_radius, field_radius),
			rng.randf_range(-field_height * 0.5, field_height * 0.5),
			rng.randf_range(-field_radius, field_radius))
		_positions[i] = p
		_alive[i] = 1
		_hash.insert(i, p)
		multimesh.set_instance_transform(i, Transform3D(Basis(), p))
