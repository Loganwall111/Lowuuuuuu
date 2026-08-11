class_name PauseMenu
extends Control
## PauseMenu — Esc-to-pause overlay with a real cursor, resume / save / quit.
## Reads nothing from gameplay; it only drives GameDirector + SaveService and
## releases the mouse so the player can click.
##
## ATTACH THIS SCRIPT TO:
##   PauseMenu (Control)   <-- here
##   ├── Panel (PanelContainer)
##   │   └── VBox
##   │       ├── Title (Label)
##   │       ├── ResumeButton (Button)
##   │       ├── SaveButton (Button)
##   │       └── QuitButton (Button)

@export var resume_button: NodePath = ^"Panel/VBox/ResumeButton"
@export var save_button: NodePath = ^"Panel/VBox/SaveButton"
@export var quit_button: NodePath = ^"Panel/VBox/QuitButton"

func _ready() -> void:
	theme = ThemeFactory.new().get_theme()
	visible = false
	process_mode = Node.PROCESS_MODE_ALWAYS
	var rb := get_node_or_null(resume_button) as Button
	if rb != null:
		rb.pressed.connect(_resume)
	var sb := get_node_or_null(save_button) as Button
	if sb != null:
		sb.pressed.connect(_save)
	var qb := get_node_or_null(quit_button) as Button
	if qb != null:
		qb.pressed.connect(_quit)
	EventBus.pause_toggled.connect(_on_pause)


func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventKey and (event as InputEventKey).pressed and (event as InputEventKey).keycode == KEY_ESCAPE:
		GameDirector.toggle_pause()


func _on_pause(is_paused: bool) -> void:
	visible = is_paused


func _resume() -> void:
	GameDirector.set_paused(false)


func _save() -> void:
	if GameDirector.session != null:
		SaveService.save_slot(1, GameDirector.session)
		EventBus.toast_requested.emit("Saved.", &"good")


func _quit() -> void:
	GameDirector.end_run(&"quit")
