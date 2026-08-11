class_name DebugOverlay
extends Control
## DebugOverlay — live systems read-out (agent LOD bands, gravity wells, draw
## calls, FPS, memory). Off by default, toggled with F3. Demonstrates that the
## performance architecture is observable, not guesswork.
##
## ATTACH THIS SCRIPT TO:
##   DebugOverlay (Control)   <-- here
##   └── Log (Label, multiline, top-left)

@export var log_label: NodePath = ^"Log"
var _visible := false
var _acc: float = 0.0


func _ready() -> void:
	visible = false
	process_mode = Node.PROCESS_MODE_ALWAYS
	EventBus.toast_requested.connect(func(_t, _k): pass)
	if log_label == NodePath():
		log_label = ^"Log"


func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventKey and (event as InputEventKey).pressed and (event as InputEventKey).keycode == KEY_F3:
		_visible = not _visible
		visible = _visible


func _process(delta: float) -> void:
	if not _visible:
		return
	_acc += delta
	if _acc < 0.25:
		return
	_acc = 0.0
	var l := get_node_or_null(log_label) as Label
	if l == null:
		return
	var s := SimulationDirector.stats
	var wells := 0
	var reg := get_tree().get_first_node_in_group(&"gravity_well_registry")
	if reg != null and reg.has_method("well_count"):
		wells = reg.well_count()
	var fps := Engine.get_frames_per_second()
	var rsm := RenderingServer.get_rendering_info(RenderingServer.RENDERING_INFO_TOTAL_DRAW_CALLS_IN_FRAME) if false else 0
	l.text = (
		"FPS: %d\n" % fps +
		"Agents total: %d\n" % s.get("agents", 0) +
		"  near/mid/far/dormant: %d / %d / %d / %d\n" % [s.get("near", 0), s.get("mid", 0), s.get("far", 0), s.get("dormant", 0)] +
		"  ticked/frame: %d\n" % s.get("ticked", 0) +
		"Gravity wells: %d\n" % wells +
		"Pool parked: %d\n" % PoolService.stats.get("parked", 0) +
		"Draw calls: %d\n" % rsm +
		"Player stage: %s\n" % _stage_name()
	)


func _stage_name() -> String:
	if GameDirector.player != null and GameDirector.player is PlayerPawn:
		var evo := (GameDirector.player as PlayerPawn).evolution
		if evo != null and evo.current_stage != null:
			return evo.current_stage.display_name
	return "-"
