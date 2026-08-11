class_name EvolutionBranchScreen
extends Control
## EvolutionBranchScreen — the Spore-style branch chooser, rendered as a rich
## full-screen panel. Each option is a card with name, lore, the requirements to
## reach it, the abilities it unlocks, and a live 3D **body preview** (a small
## MeshInstance3D built from the stage's CreatureBodyRecipe). Selecting one
## advances the lineage and morphs the pawn.
##
## Built in code (no external .tscn) so it can be added/removed by EvolutionPanel
## without scene-path coupling.
##
## DEPENDENCIES: EvolutionComponent, CreatureBodyRecipe, ProceduralCreatureFactory,
## EventBus, GameDirector.

@export var option_scene: PackedScene   # a simple "card" Control prefab (or null)

var _pawn: PlayerPawn
var _options: Array = []


func show_branches(options: Array, pawn: PlayerPawn) -> void:
	_pawn = pawn
	_options = options
	_build()
	visible = true


func _build() -> void:
	for child in get_children():
		child.queue_free()

	var bg := ColorRect.new()
	bg.color = Color(0.02, 0.04, 0.07, 0.82)
	bg.set_anchors_preset(Control.PRESET_FULL_RECT)
	bg.mouse_filter = Control.MOUSE_FILTER_STOP
	add_child(bg)

	var vbox := VBoxContainer.new()
	vbox.set_anchors_preset(Control.PRESET_FULL_RECT)
	vbox.offset_left = 60; vbox.offset_top = 50; vbox.offset_right = -60; vbox.offset_bottom = -50
	vbox.add_theme_constant_override("separation", 16)
	add_child(vbox)

	var title := Label.new()
	title.text = "A Branching Path Opens"
	title.add_theme_font_size_override("font_size", 34)
	vbox.add_child(title)

	var grid := GridContainer.new()
	grid.columns = maxi(1, _options.size())
	grid.add_theme_constant_override("hgap", 18)
	grid.add_theme_constant_override("vgap", 18)
	vbox.add_child(grid)

	for stage in _options:
		grid.add_child(_make_card(stage))

	var hint := Label.new()
	hint.text = "Choose with click or Enter. Your form will transform."
	hint.add_theme_font_size_override("font_size", 16)
	vbox.add_child(hint)


func _make_card(stage: EvolutionStageData) -> Control:
	var card := PanelContainer.new()
	card.custom_minimum_size = Vector2(260, 360)
	var vb := VBoxContainer.new()
	vb.add_theme_constant_override("separation", 8)
	card.add_child(vb)

	# Live 3D body preview (sub-viewport) so the player sees the future form.
	var vp := SubViewport.new()
	vp.size = Vector2(240, 150)
	vp.transparent_bg = true
	vp.world_3d = true
	var cam := Camera3D.new()
	cam.position = Vector3(0, 1.2, 4)
	cam.look_at(Vector3.ZERO)
	vp.add_child(cam)
	var root := Node3D.new()
	vp.add_child(root)
	if _pawn != null and _pawn.organism != null and _pawn.organism.body_recipe != null:
		for m in ProceduralCreatureFactory.build(_pawn.organism.body_recipe, stage):
			m.position = Vector3.ZERO
			root.add_child(m)
	root.rotate_y(0.6)
	vb.add_child(vp)

	var nm := Label.new(); nm.text = stage.display_name
	nm.add_theme_font_size_override("font_size", 22); vb.add_child(nm)
	var lore := Label.new(); lore.text = stage.lore; lore.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	vb.add_child(lore)

	var req := "Requires: "
	var parts: Array[String] = []
	if stage.required_mass > 0.0: parts.append("mass %.1f" % stage.required_mass)
	if stage.required_evolution_points > 0.0: parts.append("EP %.0f" % stage.required_evolution_points)
	if stage.required_age_seconds > 0.0: parts.append("age %.0fs" % stage.required_age_seconds)
	req += ", ".join(parts) if not parts.is_empty() else "ready now"
	vb.add_child(_label(req))

	if not stage.unlocked_abilities.is_empty():
		var ab := "Unlocks: "
		var names: Array[String] = []
		for a in stage.unlocked_abilities:
			names.append(a.display_name)
		ab += ", ".join(names)
		vb.add_child(_label(ab))

	var btn := Button.new(); btn.text = "Evolve"
	btn.pressed.connect(_choose.bind(stage))
	vb.add_child(btn)
	return card


func _label(t: String) -> Label:
	var l := Label.new(); l.text = t; l.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	l.add_theme_font_size_override("font_size", 14)
	return l


func _choose(stage: EvolutionStageData) -> void:
	if _pawn != null and _pawn.evolution != null:
		_pawn.evolution.choose_branch(stage)
	visible = false
	if GameDirector != null:
		GameDirector.set_paused(false)
