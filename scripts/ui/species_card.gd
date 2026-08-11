class_name SpeciesCard
extends PanelContainer
## SpeciesCard — one selectable species tile in the main menu. Data-driven: it is
## given an OrganismData and renders the name, category, environment, movement
## and focus text from that resource (plus the organism's accent colour).
##
## ATTACH THIS SCRIPT TO:
##   SpeciesCard (PanelContainer)   <-- here
##   ├── VBox
##   │   ├── Name (Label)
##   │   ├── Category (Label)
##   │   ├── Environment (Label)
##   │   ├── Movement (Label)
##   │   ├── Focus (Label / RichTextLabel)
##   │   └── AccentBar (ColorRect or TextureRect)
##   └── (Mouse/keyboard "pressed" emitted by the card)
##
## The card is fully focusable so the menu works with keyboard/gamepad too.

signal pressed

@export var name_label: NodePath = ^"VBox/Name"
@export var category_label: NodePath = ^"VBox/Category"
@export var environment_label: NodePath = ^"VBox/Environment"
@export var movement_label: NodePath = ^"VBox/Movement"
@export var focus_label: NodePath = ^"VBox/Focus"
@export var accent_bar: NodePath = ^"VBox/AccentBar"

var _species: OrganismData


func _ready() -> void:
	focus_mode = Control.FOCUS_ALL
	mouse_entered.connect(func() -> void: grab_focus())
	gui_input.connect(_on_gui_input)


func setup(species: OrganismData) -> void:
	_species = species
	var n := get_node_or_null(name_label) as Label
	if n != null:
		n.text = species.display_name
	var c := get_node_or_null(category_label) as Label
	if c != null:
		c.text = "Category: " + species.category.capitalize()
	var e := get_node_or_null(environment_label) as Label
	if e != null and species.starting_environment != null:
		e.text = "World: " + species.starting_environment.resource_path.get_file().get_basename()
	var m := get_node_or_null(movement_label) as Label
	if m != null:
		m.text = "Movement: " + LocomotionTypes.name_of(species.base_movement_type)
	var f := get_node_or_null(focus_label) as Label
	if f != null:
		f.text = species.summary
	var bar := get_node_or_null(accent_bar) as Control
	if bar != null and "color" in bar:
		bar.color = species.accent_color


func _on_gui_input(event: InputEvent) -> void:
	if event is InputEventMouseButton and (event as InputEventMouseButton).pressed and (event as InputEventMouseButton).button_index == MOUSE_BUTTON_LEFT:
		pressed.emit()
	elif event is InputEventKey and (event as InputEventKey).pressed and (event as InputEventKey).keycode == KEY_ENTER:
		pressed.emit()
