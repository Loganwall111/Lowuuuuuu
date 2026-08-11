class_name EvolutionPanel
extends Control
## EvolutionPanel — thin host for the data-driven EvolutionBranchScreen. When the
## EvolutionComponent reports multiple ready stages, this shows the Spore-style
## chooser (body preview + lore + requirements) built by EvolutionBranchScreen.
##
## ATTACH THIS SCRIPT TO: EvolutionPanel (Control)  (scenes/ui/EvolutionPanel.tscn)
## DEPENDENCIES: EvolutionBranchScreen, EventBus, GameDirector.

@export var branch_screen_scene: PackedScene

var _screen: Control


func _ready() -> void:
	theme = ThemeFactory.new().get_theme()
	visible = false
	process_mode = Node.PROCESS_MODE_ALWAYS
	EventBus.evolution_branch_offered.connect(_show)
	EventBus.evolution_stage_changed.connect(func(_s, _p): hide())
	EventBus.player_spawned.connect(func(p): _pawn = p as PlayerPawn)
	var cb := get_node_or_null(^"CloseButton") as Button
	if cb != null:
		cb.pressed.connect(_close)


var _pawn: PlayerPawn


func _show(options: Array) -> void:
	if options.is_empty():
		return
	_build_screen()
	if _screen != null and _screen.has_method("show_branches"):
		_screen.show_branches(options, _pawn)
	visible = true
	if GameDirector != null:
		GameDirector.set_paused(true)


func _build_screen() -> void:
	if _screen != null:
		return
	if branch_screen_scene == null:
		branch_screen_scene = load("res://scripts/ui/evolution_branch_screen.gd")
	# EvolutionBranchScreen is a script, not a scene: instance the class.
	var script := load("res://scripts/ui/evolution_branch_screen.gd")
	_screen = Control.new()
	_screen.set_script(script)
	_screen.name = "BranchScreen"
	_screen.mouse_filter = Control.MOUSE_FILTER_STOP
	add_child(_screen)


func _close() -> void:
	visible = false
	if GameDirector != null:
		GameDirector.set_paused(false)
