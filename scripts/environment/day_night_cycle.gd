class_name DayNightCycle
extends Node
## DayNightCycle — rotates the biome's sun and cross-fades its mood through
## dawn/day/dusk/night. Pure presentation driven by a normalized time-of-day;
## gameplay reads time_of_day via EventBus if it needs "is it night?".
##
## ATTACH THIS SCRIPT TO:
##   BiomeScene (Node3D)   <-- here (sibling of EnvironmentDirector)
##
## DEPENDENCIES: EnvironmentDirector (optional; it calls set_profile), EventBus.

@export_group("Sun")
@export var sun: DirectionalLight3D
@export var day_duration_seconds: float = 240.0
@export var start_time_of_day: float = 0.3

@export_group("Profiles")
@export var dawn_profile: EnvironmentProfile
@export var day_profile: EnvironmentProfile
@export var dusk_profile: EnvironmentProfile
@export var night_profile: EnvironmentProfile

var _time: float = 0.3
var _director: EnvironmentDirector


func _ready() -> void:
	_director = get_parent().get_node_or_null(^"EnvironmentDirector") as EnvironmentDirector
	_time = start_time_of_day
	_apply_phase(_time, true)
	EventBus.day_phase_changed.emit(_phase_name(_time), _time)


func _process(delta: float) -> void:
	_time = fmod(_time + delta / day_duration_seconds, 1.0)
	_apply_phase(_time, false)
	EventBus.day_phase_changed.emit(_phase_name(_time), _time)


func _phase_name(t: float) -> StringName:
	if t < 0.25:
		return &"dawn"
	if t < 0.55:
		return &"day"
	if t < 0.75:
		return &"dusk"
	return &"night"


func _apply_phase(t: float, instant: bool) -> void:
	var profile := day_profile
	if t < 0.22:
		profile = dawn_profile
	elif t < 0.55:
		profile = day_profile
	elif t < 0.78:
		profile = dusk_profile
	else:
		profile = night_profile
	if _director != null and profile != null:
		if instant:
			_director._apply_instant(profile)
		else:
			_director.set_profile(profile)
	if sun != null:
		# Sun arcs across the sky; t=0 sunrise (east), t=0.5 noon, t=1 midnight.
		var angle := t * TAU - PI * 0.5
		sun.rotation = Vector3(deg_to_rad(40.0), angle, 0.0)
		sun.light_energy = lerpf(0.15, 1.4, sin(t * PI))
