extends Node
## MainBootstrap — the project root. Owns the menu and reacts to session flow
## so the player only ever sees the menu when not playing, and the mouse is
## captured during gameplay (released for the menu). It does NOT hard-code any
## species — it just instantiates the data-driven MainMenu.
##
## DEPENDENCIES: EventBus, GameDirector, SceneDirector, MainMenu.

@export var menu_scene: PackedScene

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	if menu_scene == null:
		menu_scene = load("res://scenes/menu/MainMenu.tscn")
	if menu_scene != null and $MenuLayer/Menu.get_child_count() == 0:
		var menu := menu_scene.instantiate()
		$MenuLayer/Menu.add_child(menu)
	Input.mouse_mode = Input.MOUSE_MODE_VISIBLE
	EventBus.session_requested.connect(_on_session_requested)
	EventBus.session_ended.connect(_on_session_ended)


func _on_session_requested(_organism: Resource) -> void:
	# Gameplay starting: capture the mouse for look/steer.
	Input.mouse_mode = Input.MOUSE_MODE_CAPTURED
	if $MenuLayer/Menu.get_child_count() > 0:
		for c in $MenuLayer/Menu.get_children():
			c.queue_free()


func _on_session_ended(_reason: StringName) -> void:
	# Returning to menu: free the cursor and rebuild the menu.
	Input.mouse_mode = Input.MOUSE_MODE_VISIBLE
	if $MenuLayer/Menu.get_child_count() == 0 and menu_scene != null:
		$MenuLayer/Menu.add_child(menu_scene.instantiate())
