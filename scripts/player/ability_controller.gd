class_name AbilityController
extends Node
## AbilityController — turns unlocked potential into ACTIVE, input-driven
## abilities. Dash (burst of speed, costs energy, cooldown) and Sonar (reveals
## nearby prey via EventBus) demonstrate the pattern; more abilities slot in
## here the same way.
##
## ATTACH THIS SCRIPT TO: PlayerPawn (CharacterBody3D) -> AbilityController
## DEPENDENCIES: PlayerPawn, MetabolismComponent, EventBus, CameraRig.

@export_group("Dash")
@export var dash_impulse: float = 16.0
@export var dash_energy: float = 8.0
@export var dash_cooldown: float = 1.1

@export_group("Sonar")
@export var sonar_cooldown: float = 6.0
@export var sonar_radius: float = 60.0

var _dash_cd: float = 0.0
var _sonar_cd: float = 0.0
var pawn: PlayerPawn


func _ready() -> void:
	pawn = get_parent() as PlayerPawn
	set_process(false)  # only run while a run is live


func activate() -> void:
	set_process(true)


func _physics_process(delta: float) -> void:
	_dash_cd = maxf(0.0, _dash_cd - delta)
	_sonar_cd = maxf(0.0, _sonar_cd - delta)
	if pawn == null or not is_instance_valid(pawn):
		return
	if Input.is_action_just_pressed(&"dash") and _dash_cd <= 0.0:
		_try_dash()
	if Input.is_action_just_pressed(&"sonar") and _sonar_cd <= 0.0:
		_sonar()


func _try_dash() -> void:
	if pawn.metabolism == null or pawn.metabolism.energy < dash_energy:
		EventBus.toast_requested.emit("Not enough energy to dash", &"warn")
		return
	pawn.metabolism.add_energy(-dash_energy)
	var fwd := -pawn.global_transform.basis.z
	pawn.velocity += fwd * dash_impulse
	_dash_cd = dash_cooldown
	EventBus.toast_requested.emit("Dash", &"good")
	EventBus.camera_shake_requested.emit(0.22, 0.2)
	AudioDirector.play_ui(AudioDirector.tone("whoosh"), -4.0)


func _sonar() -> void:
	_sonar_cd = sonar_cooldown
	EventBus.toast_requested.emit("Sonar ping", &"info")
	if is_instance_valid(pawn):
		EventBus.sonar_ping.emit(pawn.global_position, sonar_radius)
	EventBus.camera_shake_requested.emit(0.12, 0.25)
	AudioDirector.play_ui(AudioDirector.tone("ping"), -2.0)
