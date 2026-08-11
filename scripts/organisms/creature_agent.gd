class_name CreatureAgent
extends Node3D
## CreatureAgent — lightweight AI swimmer/flyer for the background ecosystem.
## Now with PREDATOR behaviour: predators chase the player (focus) within
## detect range and bite when close; they flee if the player is far larger.
## Prey (non-predators) keep the original wander+flee. Deer/creatures register
## with SimulationDirector and tick at a distance-appropriate cadence.
##
## ATTACH THIS SCRIPT TO: PreyCreature (Node3D)
##   ├── MeshInstance3D
##   └── EdibleComponent

@export_group("Behaviour")
@export var max_speed: float = 3.0
@export var wander_strength: float = 1.2
@export var separation_radius: float = 2.2
@export var flee_radius: float = 12.0
@export var flee_speed: float = 6.0
@export var bounds_center: Vector3 = Vector3.ZERO
@export var bounds_radius: float = 80.0

@export_group("Predator")
@export var is_predator: bool = false
@export var detect_range: float = 26.0
@export var attack_range: float = 2.4
@export var attack_damage: float = 12.0
@export var attack_cooldown: float = 1.3

@export_group("Look")
@export var bank_amount: float = 0.4

var velocity: Vector3 = Vector3.ZERO
var focus: Node3D
var _wander_dir: Vector3 = Vector3.FORWARD
var _rng: RandomNumberGenerator
var _lod: int = 0
var _atk_timer: float = 0.0


func _ready() -> void:
	_rng = RandomNumberGenerator.new()
	_rng.seed = (get_instance_id() ^ 0x9e3779b9) as int
	velocity = Vector3(_rng.randf_range(-1, 1), 0, _rng.randf_range(-1, 1)) * max_speed
	if SimulationDirector != null:
		SimulationDirector.register_agent(self, true)


func on_simulation_lod_changed(lod: int) -> void:
	_lod = lod


func simulation_tick(delta: float, lod: int) -> void:
	if lod == 3:
		return
	_atk_timer = maxf(0.0, _atk_timer - delta)
	var steer := Vector3.ZERO

	if is_predator and is_instance_valid(focus) and focus is PlayerPawn:
		var to_focus: Vector3 = focus.global_position - global_position
		var d: float = to_focus.length()
		var player_mass: float = _pawn_mass(focus)
		var bigger := player_mass > (get_meta("mass", 1.0) * 1.5)
		if d < detect_range and not bigger:
			# Chase.
			steer += to_focus.normalized() * flee_speed
			if d < attack_range and _atk_timer <= 0.0:
				_atk_timer = attack_cooldown
				if focus.has_method("take_damage"):
					focus.take_damage(attack_damage)
				EventBus.player_damaged.emit(attack_damage, "predator")
		elif d < flee_radius:
			steer += -to_focus.normalized() * flee_speed
	else:
		_wander_dir = (_wander_dir + Vector3(_rng.randf_range(-1, 1), _rng.randf_range(-0.4, 0.4), _rng.randf_range(-1, 1)) * wander_strength * delta).normalized()
		steer += _wander_dir * max_speed
		if is_instance_valid(focus):
			var to_focus2: Vector3 = focus.global_position - global_position
			var d2: float = to_focus2.length()
			if d2 < flee_radius and d2 > 0.001:
				steer += -to_focus2.normalized() * flee_speed * (1.0 - d2 / flee_radius)

	var to_center: Vector3 = bounds_center - global_position
	if to_center.length() > bounds_radius:
		steer += to_center.normalized() * max_speed * 1.5

	# Schooling / flocking: neighbours pull toward a coherent swarm.
	var neighbours := SimulationDirector.get_neighbours(self) if SimulationDirector != null else []
	if neighbours.size() > 0:
		var coh := SteeringKit.cohesion(global_position, neighbours)
		var ali := SteeringKit.alignment(_neighbour_velocities(neighbours))
		var sep := SteeringKit.separation(global_position, neighbours, separation_radius)
		steer += coh * 1.2 + ali * 1.0 + sep * 2.0

	velocity = velocity.lerp(steer, 1.0 - exp(-3.0 * delta))
	if velocity.length() > flee_speed:
		velocity = velocity.normalized() * flee_speed
	global_position += velocity * delta

	if velocity.length_squared() > 0.01 and (lod <= 1 or (_rng.randi() % 4 == 0)):
		look_at(global_position + velocity, Vector3.UP)
		rotate_object_local(Vector3.Z, -velocity.x * bank_amount * 0.02)


func _pawn_mass(pawn: Node) -> float:
	if pawn is PlayerPawn and pawn.session != null:
		return pawn.session.mass
	return 1.0


func _exit_tree() -> void:
	if SimulationDirector != null:
		SimulationDirector.unregister_agent(self)


func get_visual_global_transform() -> Transform3D:
	return global_transform

func _neighbour_velocities(neighbours: Array) -> Array:
	var out: Array = []
	for n in neighbours:
		if n is CreatureAgent:
			out.append((n as CreatureAgent).velocity)
	return out
