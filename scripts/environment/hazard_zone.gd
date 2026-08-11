class_name HazardZone
extends Area3D
## HazardZone — a damaging/energy-draining region (heat vent, acidic pool,
## hypoxia, radiation). Pure data-driven danger: a pawn inside loses
## health/energy per second; the HUD/overlay reflects it via EventBus.
##
## ATTACH THIS SCRIPT TO: Area3D (the hazard volume)
##   └── CollisionShape3D
##
## DEPENDENCIES: EventBus, PlayerPawn/MetabolismComponent.

@export_group("Damage")
@export var damage_per_second: float = 8.0
@export var energy_drain_per_second: float = 4.0
@export var damage_source: StringName = &"hazard"
@export var tick_interval: float = 0.5

@export_group("Look")
@export var tint: Color = Color(1.0, 0.3, 0.1, 0.18)

var _inside: bool = false
var _acc: float = 0.0


func _ready() -> void:
	collision_layer = 0
	collision_mask = 2   # Player
	body_entered.connect(_on_enter)
	body_exited.connect(_on_exit)
	var mat := StandardMaterial3D.new()
	mat.albedo_color = tint
	mat.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
	mat.emission_enabled = true
	mat.emission = tint
	mat.emission_intensity = 0.5
	for child in get_children():
		if child is MeshInstance3D:
			child.material_override = mat


func _on_enter(body: Node3D) -> void:
	if body != null and body.collision_layer == 2:
		_inside = true


func _on_exit(body: Node3D) -> void:
	if body != null and body.collision_layer == 2:
		_inside = false


func _physics_process(delta: float) -> void:
	if not _inside:
		return
	_acc += delta
	if _acc < tick_interval:
		return
	_acc = 0.0
	var pawn := GameDirector.player
	if pawn != null and pawn is PlayerPawn and pawn.metabolism != null:
		if damage_per_second > 0.0:
			pawn.metabolism.damage(damage_per_second * tick_interval, damage_source)
		if energy_drain_per_second > 0.0:
			pawn.metabolism.add_energy(-energy_drain_per_second * tick_interval)
