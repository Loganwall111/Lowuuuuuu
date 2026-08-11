class_name MetabolismComponent
extends Node
## MetabolismComponent — energy drain, health, starvation and lifespan.
##
## PERFORMANCE: this runs on a coarse TIMER (default 0.5s), not _process, so a
## hundred small creatures do not each pay a per-frame cost. The UI is updated
## only when values change meaningfully, and the EventBus carries those low
## frequency updates. Fast feedback (damage flashes) is handled by callers
## emitting player_damaged directly.
##
## ATTACH THIS SCRIPT TO: PlayerPawn (CharacterBody3D)
##   └── MetabolismComponent   <-- here

signal energy_changed(current: float, maximum: float)
signal health_changed(current: float, maximum: float)
signal died

@export_group("Vitals")
@export var max_energy: float = 100.0
@export var max_health: float = 100.0
@export var start_energy: float = 100.0
@export var start_health: float = 100.0

@export_group("Tuning")
@export var base_drain_per_second: float = 0.6
@export var movement_drain_scale: float = 0.4   ## extra drain while moving fast
@export var starvation_damage_per_tick: float = 2.0
@export var tick_interval: float = 0.5
@export var age_scaling: float = 0.0008          ## vitality slowly declines with age

var energy: float = 100.0
var health: float = 100.0
var age_seconds: float = 0.0
var _speed_fraction: float = 0.0
var _metabolism_multiplier: float = 1.0
var _tick_accumulator: float = 0.0
var _alive: bool = true


func _ready() -> void:
	energy = start_energy
	health = start_health
	energy_changed.emit(energy, max_energy)
	health_changed.emit(health, max_health)


## Called by the pawn each physics frame with how fast the creature is going
## relative to its top speed (0..1) and the active movement profile so the
## metabolism can be tuned per mode without this script knowing the mode.
func report_activity(speed_fraction: float, profile: MovementProfile) -> void:
	_speed_fraction = clampf(speed_fraction, 0.0, 1.0)
	if profile != null:
		_metabolism_multiplier = profile.drag * 0.2 + 0.8


## Called by EvolutionComponent when a stage changes energy capacity.
func set_max_energy(value: float) -> void:
	max_energy = maxf(1.0, value)
	energy_changed.emit(energy, max_energy)


func add_energy(amount: float) -> void:
	energy = clampf(energy + amount, 0.0, max_energy)
	energy_changed.emit(energy, max_energy)


func damage(amount: float, source_name: String = "environment") -> void:
	if not _alive:
		return
	health = clampf(health - amount, 0.0, max_health)
	health_changed.emit(health, max_health)
	EventBus.player_damaged.emit(amount, source_name)
	if health <= 0.0:
		_alive = false
		died.emit()
		EventBus.session_ended.emit(&"death")


func _physics_process(delta: float) -> void:
	if not _alive:
		return
	age_seconds += delta
	_tick_accumulator += delta
	if _tick_accumulator < tick_interval:
		return
	var dt := _tick_accumulator
	_tick_accumulator = 0.0

	var drain := (base_drain_per_second + _speed_fraction * movement_drain_scale) * _metabolism_multiplier
	drain *= (1.0 + age_seconds * age_scaling)
	energy -= drain * dt

	if energy <= 0.0:
		energy = 0.0
		health -= starvation_damage_per_tick * dt
		health_changed.emit(health, max_health)
		if health <= 0.0:
			_alive = false
			died.emit()
			EventBus.session_ended.emit(&"death")
			return

	energy_changed.emit(energy, max_energy)
