class_name MainMenuController
extends Control
## MainMenuController — the polished, cinematic desktop main menu.
##
## It asks GameDirector for the data-driven species list (so adding a 4th
## species needs NO menu code) and builds a card per species from the
## SpeciesCard prefab. Selecting a card calls start_run(), which hands the
## chosen OrganismData to GameDirector and lets SceneDirector fade into the
## correct starting environment.
##
## ATTACH THIS SCRIPT TO:
##   MainMenu (Control)                  <-- here
##   ├── Background (ColorRect / SubViewport / animated creature)
##   ├── Title (Label)
##   ├── Subtitle (Label)
##   ├── CardContainer (HBoxContainer)   (cards are injected here)
##   ├── SettingsButton (Button)
##   └── QuitButton (Button)
##
## DEPENDENCIES: GameDirector, SpeciesCard, ThemeFactory, EventBus, SceneDirector.

@export_group("References")
@export var card_container: NodePath = ^"CardContainer"
@export var title_label: NodePath = ^"Title"
@export var subtitle_label: NodePath = ^"Subtitle"
@export var card_scene: PackedScene

@export_group("Tuning")
@export var auto_focus_first_card: bool = true

var _cards: Array = []


func _ready() -> void:
	theme = ThemeFactory.new().get_theme()
	var title := get_node_or_null(title_label) as Label
	if title != null:
		title.text = "AEON"
	var sub := get_node_or_null(subtitle_label) as Label
	if sub != null:
		sub.text = "Begin as a spark of life. End among the stars."
	_build_cards()
	_ready_continue()


func _build_cards() -> void:
	var container := get_node_or_null(card_container)
	if container == null or card_scene == null:
		push_error("MainMenuController: missing CardContainer or card_scene.")
		return
	for species in GameDirector.get_playable_species():
		var card := card_scene.instantiate()
		container.add_child(card)
		card.setup(species)
		card.pressed.connect(_on_card_pressed.bind(species))
		_cards.append(card)
	if auto_focus_first_card and not _cards.is_empty():
		(_cards[0] as Control).grab_focus()


func _on_card_pressed(species: OrganismData) -> void:
	# Optional: a cinematic line before the fade.
	EventBus.cinematic_caption_requested.emit("Becoming " + species.display_name, species.summary, 2.4)
	start_run(species)


## Hands the species to the core and lets SceneDirector stream the biome.
func start_run(species: OrganismData) -> void:
	EventBus.toast_requested.emit("Entering the " + species.category.capitalize() + " path", &"info")
	GameDirector.start_new_run(species)


func _on_settings_pressed() -> void:
	EventBus.toast_requested.emit("Settings — wire to your options panel", &"info")


func _on_quit_pressed() -> void:
	get_tree().quit()


func _ready_continue() -> void:
	# Called by the scene after _ready; wires the optional Continue button.
	var cont := get_node_or_null(^"ContinueButton")
	if cont == null:
		return
	if not SaveService.has_slot(1):
		cont.visible = false
		return
	cont.pressed.connect(_on_continue_pressed)


func _on_continue_pressed() -> void:
	var session := SaveService.load_slot(1)
	if session != null:
		GameDirector.resume_run(session)
	else:
		EventBus.toast_requested.emit("No save found.", &"warn")
