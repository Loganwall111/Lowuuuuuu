class_name ThemeFactory
extends RefCounted
## ThemeFactory — builds a single cohesive, "premium" UI Theme at runtime so the
## menu/HUD never depend on a hand-authored .tres (and so designers can re-skin
## the whole game from one place). Loaded lazily and cached.
##
## Palette: deep abyssal navy + bioluminescent teal + warm accent. Designed to
## feel like a living, mysterious world rather than a debug UI.

const BG := Color(0.027, 0.039, 0.063)
const PANEL := Color(0.055, 0.09, 0.13, 0.82)
const TEAL := Color(0.36, 0.95, 0.82)
const TEAL_DIM := Color(0.18, 0.45, 0.45)
const TEXT := Color(0.86, 0.95, 0.94)
const TEXT_DIM := Color(0.55, 0.7, 0.74)
const WARN := Color(0.98, 0.62, 0.36)
const EPIC := Color(0.7, 0.88, 1.0)

var _cached: Theme = null


func get_theme() -> Theme:
	if _cached != null:
		return _cached
	var t := Theme.new()
	t.default_font_size = 18
	_style_panel(t)
	_style_button(t)
	_style_label(t)
	_style_progress(t)
	_cached = t
	return t


func _style_panel(t: Theme) -> void:
	var sb := StyleBoxFlat.new()
	sb.bg_color = PANEL
	sb.border_color = TEAL_DIM
	sb.set_border_width_all(1)
	sb.corner_radius_top_left = 14
	sb.corner_radius_top_right = 14
	sb.corner_radius_bottom_left = 14
	sb.corner_radius_bottom_right = 14
	sb.content_margin_left = 18
	sb.content_margin_right = 18
	sb.content_margin_top = 14
	sb.content_margin_bottom = 14
	t.set_stylebox("panel", "PanelContainer", sb)
	t.set_stylebox("panel", "Panel", sb)


func _style_button(t: Theme) -> void:
	var normal := StyleBoxFlat.new()
	normal.bg_color = Color(0.06, 0.12, 0.16, 0.9)
	normal.border_color = TEAL_DIM
	normal.set_border_width_all(1)
	normal.corner_radius_all = 10
	normal.content_margin_left = 20
	normal.content_margin_right = 20
	normal.content_margin_top = 12
	normal.content_margin_bottom = 12
	var hover := normal.duplicate() as StyleBoxFlat
	hover.bg_color = Color(0.1, 0.2, 0.26, 0.95)
	hover.border_color = TEAL
	t.set_stylebox("normal", "Button", normal)
	t.set_stylebox("hover", "Button", hover)
	t.set_color("font_color", "Button", TEXT)
	t.set_color("font_hover_color", "Button", TEAL)
	t.set_color("font_pressed_color", "Button", TEAL)
	t.set_font_size("font_size", "Button", 20)


func _style_label(t: Theme) -> void:
	t.set_color("font_color", "Label", TEXT)
	t.set_color("font_color", "LineEdit", TEXT)


func _style_progress(t: Theme) -> void:
	var bg := StyleBoxFlat.new()
	bg.bg_color = Color(0.03, 0.06, 0.09)
	bg.corner_radius_all = 6
	var fill := StyleBoxFlat.new()
	fill.bg_color = TEAL
	fill.corner_radius_all = 6
	t.set_stylebox("background", "ProgressBar", bg)
	t.set_stylebox("fill", "ProgressBar", fill)
	t.set_color("font_color", "ProgressBar", TEXT)
