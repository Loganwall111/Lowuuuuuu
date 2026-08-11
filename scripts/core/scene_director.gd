extends CanvasLayer
## SceneDirector — asynchronous scene streaming with a cinematic transition
## (AUTOLOAD: "SceneDirector").
##
## WHY A CANVASLAYER AUTOLOAD
## The transition veil and loading readout must survive the destruction of the
## outgoing scene and must draw above everything else. A CanvasLayer autoload at
## layer 128 is the cheapest way to guarantee both.
##
## The overlay is built entirely in code — no .tscn dependency — so the loader
## can never fail because of a missing resource.
##
## PERFORMANCE: _process is disabled unless a load is actually in flight.
##
## DEPENDENCIES: EventBus.

signal transition_midpoint ## Screen fully covered; safe to swap scenes.

const FADE_OUT_TIME := 0.45
const FADE_IN_TIME := 0.70
const MIN_VISIBLE_TIME := 0.6 ## Avoid a 1-frame flash on trivially small scenes.

var _veil: ColorRect
var _title: Label
var _hint: Label
var _bar: ProgressBar
var _loading_path: String = ""
var _elapsed: float = 0.0
var _pending_progress: Array = []

var loading_hints: PackedStringArray = [
	"Complexity is only ever borrowed from the environment.",
	"Every eye in the ocean began as a light-sensitive patch of skin.",
	"Mass is memory: what you eat, you become.",
	"Flight was invented four separate times. None of them planned it.",
	"The first nervous systems were nets, not brains.",
]


func _ready() -> void:
	layer = 128
	process_mode = Node.PROCESS_MODE_ALWAYS
	_build_overlay()
	set_process(false)


func _build_overlay() -> void:
	_veil = ColorRect.new()
	_veil.name = "Veil"
	_veil.color = Color(0.016, 0.024, 0.043, 1.0)
	_veil.set_anchors_preset(Control.PRESET_FULL_RECT)
	_veil.mouse_filter = Control.MOUSE_FILTER_IGNORE
	_veil.modulate.a = 0.0
	_veil.visible = false
	add_child(_veil)

	var column := VBoxContainer.new()
	column.set_anchors_preset(Control.PRESET_FULL_RECT)
	column.alignment = BoxContainer.ALIGNMENT_END
	column.add_theme_constant_override("separation", 14)
	column.offset_left = 96.0
	column.offset_right = -96.0
	column.offset_bottom = -84.0
	column.mouse_filter = Control.MOUSE_FILTER_IGNORE
	_veil.add_child(column)

	_title = Label.new()
	_title.text = "ADAPTING"
	_title.add_theme_font_size_override("font_size", 34)
	_title.add_theme_color_override("font_color", Color(0.85, 0.98, 0.96))
	column.add_child(_title)

	_hint = Label.new()
	_hint.add_theme_font_size_override("font_size", 17)
	_hint.add_theme_color_override("font_color", Color(0.55, 0.72, 0.78))
	_hint.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	column.add_child(_hint)

	_bar = ProgressBar.new()
	_bar.custom_minimum_size = Vector2(0, 4)
	_bar.show_percentage = false
	_bar.max_value = 1.0
	_bar.value = 0.0
	column.add_child(_bar)


## Streams a PackedScene that is already referenced by a resource (the common
## case: OrganismData.starting_environment).
func change_scene_async(scene: PackedScene) -> void:
	if scene == null:
		push_error("SceneDirector: null PackedScene.")
		return
	if scene.resource_path.is_empty():
		# Runtime-built scene: nothing to stream, swap immediately.
		await _fade_to_black()
		get_tree().change_scene_to_packed(scene)
		await _fade_from_black()
		return
	change_scene_to_path_async(scene.resource_path)


func change_scene_to_path_async(path: String) -> void:
	if not _loading_path.is_empty():
		return # A load is already running; ignore re-entrancy.
	if not ResourceLoader.exists(path):
		push_error("SceneDirector: scene not found: %s" % path)
		return

	_loading_path = path
	_elapsed = 0.0
	_hint.text = loading_hints[randi() % loading_hints.size()]
	_bar.value = 0.0
	EventBus.scene_load_started.emit(path)

	await _fade_to_black()
	transition_midpoint.emit()

	ResourceLoader.load_threaded_request(path, "PackedScene", true)
	set_process(true)


func _process(delta: float) -> void:
	_elapsed += delta
	_pending_progress.clear()
	var status := ResourceLoader.load_threaded_get_status(_loading_path, _pending_progress)
	var ratio: float = 0.0
	if _pending_progress.size() > 0:
		ratio = float(_pending_progress[0])
	_bar.value = ratio
	EventBus.scene_load_progress.emit(ratio)

	match status:
		ResourceLoader.THREAD_LOAD_IN_PROGRESS:
			return
		ResourceLoader.THREAD_LOAD_LOADED:
			if _elapsed < MIN_VISIBLE_TIME:
				return
			var packed: PackedScene = ResourceLoader.load_threaded_get(_loading_path)
			set_process(false)
			var finished_path := _loading_path
			_loading_path = ""
			get_tree().change_scene_to_packed(packed)
			EventBus.scene_load_finished.emit(finished_path)
			await get_tree().process_frame
			await _fade_from_black()
		_:
			push_error("SceneDirector: failed to load %s" % _loading_path)
			set_process(false)
			_loading_path = ""
			await _fade_from_black()


func _fade_to_black() -> void:
	_veil.visible = true
	var tween := create_tween()
	tween.set_pause_mode(Tween.TWEEN_PAUSE_PROCESS)
	tween.tween_property(_veil, "modulate:a", 1.0, FADE_OUT_TIME)
	await tween.finished


func _fade_from_black() -> void:
	var tween := create_tween()
	tween.set_pause_mode(Tween.TWEEN_PAUSE_PROCESS)
	tween.tween_property(_veil, "modulate:a", 0.0, FADE_IN_TIME)
	await tween.finished
	_veil.visible = false
