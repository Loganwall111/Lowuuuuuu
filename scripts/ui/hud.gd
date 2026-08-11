class_name HUD
extends CanvasLayer
## HUD — the in-game heads-up display. Pure presentation: it only LISTENS to
## EventBus and to a cached pawn reference, never polling gameplay directly
## (cheap, decoupled). Includes a toast line and a stage/mode readout so the
## player always understands their evolutionary state.

@export_group("Paths")
@export var energy_bar: NodePath = ^"Vitals/EnergyBar"
@export var health_bar: NodePath = ^"Vitals/HealthBar"
@export var mass_label: NodePath = ^"Vitals/MassLabel"
@export var evolution_bar: NodePath = ^"Vitals/EvolutionBar"
@export var stage_label: NodePath = ^"Status/StageLabel"
@export var mode_label: NodePath = ^"Status/ModeLabel"
@export var depth_label: NodePath = ^"Status/DepthLabel"
@export var toast_label: NodePath = ^"Toast"

var _pawn: PlayerPawn
var _toast_timer: float = 0.0


func _ready() -> void:
	theme = ThemeFactory.new().get_theme()
	_bind(EventBus.energy_changed, _on_energy)
	_bind(EventBus.health_changed, _on_health)
	_bind(EventBus.mass_changed, _on_mass)
	_bind(EventBus.evolution_points_changed, _on_ep)
	_bind(EventBus.evolution_stage_changed, _on_stage)
	_bind(EventBus.movement_state_changed, _on_mode)
	_bind(EventBus.submersion_changed, _on_submersion)
	_bind(EventBus.toast_requested, _on_toast)
	_bind(EventBus.player_spawned, _on_player_spawned)
	EventBus.cinematic_caption_requested.connect(_on_caption)
	set_process(true)


func _bind(sig: Signal, method: Callable) -> void:
	if not sig.is_connected(method):
		sig.connect(method)


func _on_player_spawned(pawn: Node) -> void:
	_pawn = pawn as PlayerPawn
	if _pawn != null and _pawn.evolution != null and _pawn.evolution.current_stage != null:
		_set_stage(_pawn.evolution.current_stage)


func _on_energy(cur: float, maxv: float) -> void:
	_bar(energy_bar, cur, maxv)


func _on_health(cur: float, maxv: float) -> void:
	_bar(health_bar, cur, maxv)


func _on_mass(cur: float, cap: float) -> void:
	var l := get_node_or_null(mass_label) as Label
	if l != null:
		l.text = "Biomass: %.2f" % cur


func _on_ep(cur: float, req: float) -> void:
	_bar(evolution_bar, cur, maxf(req, 0.001))


func _on_stage(stage: Resource, _prev: Resource) -> void:
	_set_stage(stage as EvolutionStageData)


func _set_stage(stage: EvolutionStageData) -> void:
	var l := get_node_or_null(stage_label) as Label
	if l != null and stage != null:
		l.text = "Stage: " + stage.display_name


func _on_mode(new_state: int, _prev: int) -> void:
	var l := get_node_or_null(mode_label) as Label
	if l != null:
		l.text = "Mode: " + LocomotionTypes.name_of(new_state)


func _on_submersion(is_sub: bool, depth: float) -> void:
	var l := get_node_or_null(depth_label) as Label
	if l != null:
		l.text = ("Depth: %.1f m" % depth) if is_sub else "Surface"


func _on_toast(text: String, kind: StringName) -> void:
	var l := get_node_or_null(toast_label) as Label
	if l == null:
		return
	l.text = text
	match kind:
		&"good": l.modulate = Color(0.5, 0.95, 0.7)
		&"warn": l.modulate = Color(0.98, 0.62, 0.36)
		&"epic": l.modulate = Color(0.75, 0.9, 1.0)
		_: l.modulate = Color(0.86, 0.95, 0.94)
	l.modulate.a = 1.0
	_toast_timer = 3.2


func _on_caption(title: String, subtitle: String, _dur: float) -> void:
	_on_toast(title + " — " + subtitle, &"epic")


func _bar(path: NodePath, cur: float, maxv: float) -> void:
	var b := get_node_or_null(path) as ProgressBar
	if b == null:
		return
	b.max_value = maxf(maxv, 0.001)
	b.value = clampf(cur, 0.0, b.max_value)


func _process(delta: float) -> void:
	if _toast_timer > 0.0:
		_toast_timer -= delta
		var l := get_node_or_null(toast_label) as Label
		if l != null and _toast_timer < 1.0:
			l.modulate.a = clampf(_toast_timer, 0.0, 1.0)
	_update_compass_and_objective(delta)


# ----------------------------------------------------- objective + compass

@export var objective_label: NodePath = ^"Status/ObjectiveLabel"
@export var compass: NodePath = ^"Compass"

var _obj_timer: float = 0.0
var _compass_target: Node3D = null


func _update_objective() -> void:
	var o := get_node_or_null(objective_label) as Label
	if o != null and _pawn != null and _pawn.evolution != null:
		o.text = _pawn.evolution.next_requirement_text()


func _update_compass() -> void:
	var c := get_node_or_null(compass)
	if c == null:
		return
	var arrow := c.get_node_or_null(^"Arrow") if c.has_node("Arrow") else null
	if arrow == null:
		return
	if _obj_target != Vector3.ZERO:
		_compass_target = _obj_marker()
	if _compass_target == null or not is_instance_valid(_compass_target):
		_compass_target = _find_nearest_prey()
	if _compass_target == null:
		arrow.visible = false
		return
	arrow.visible = true
	var to := _compass_target.global_position - _pawn.global_position
	var cam := get_viewport().get_camera_3d()
	if cam == null:
		return
	var fwd := -cam.global_transform.basis.z
	var right := cam.global_transform.basis.x
	var ang := atan2(to.dot(right), to.dot(fwd))
	arrow.rotation = ang


func _find_nearest_prey() -> Node3D:
	if SimulationDirector == null:
		return null
	var best: Node3D = null
	var best_d := INF
	for agent in SimulationDirector._agents:
		if agent is CreatureAgent and is_instance_valid(agent):
			var d := agent.global_position.distance_to(_pawn.global_position)
			if d < best_d:
				best_d = d
				best = agent
	return best


## Drives the objective line + compass each frame; called from the main
## _process so we never define two _process functions.
func _update_compass_and_objective(delta: float) -> void:
	_obj_timer += delta
	if _obj_timer > 0.4:
		_obj_timer = 0.0
		_update_objective()
	if _pawn != null:
		_update_compass()


# ----------------------------------------------------- objective banner

@export var objective_banner: NodePath = ^"ObjectiveBanner"

var _obj_text: String = ""
var _obj_target: Vector3 = Vector3.ZERO


func _bind_objective() -> void:
    if not EventBus.objective_changed.is_connected(_on_objective):
        EventBus.objective_changed.connect(_on_objective)


func _on_objective(text: String, target: Vector3) -> void:
    _obj_text = text
    _obj_target = target
    var b := get_node_or_null(objective_banner) as Label
    if b != null:
        b.text = text


func _obj_marker() -> Node3D:
	for a in SimulationDirector._agents:
		if a is CreatureAgent and is_instance_valid(a):
			if (a as CreatureAgent).global_position.distance_to(_obj_target) < 2.0:
				return a
	return null
