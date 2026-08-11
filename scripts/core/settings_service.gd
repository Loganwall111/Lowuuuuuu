extends Node
## SettingsService — persisted user configuration + defensive input bootstrap
## (AUTOLOAD: "SettingsService").
##
## Two jobs:
##  1. Load/save user://settings.cfg and apply graphics/audio/gameplay values.
##  2. Guarantee that every gameplay input action exists, even if project.godot
##     was regenerated or an action was deleted by accident. Gameplay code can
##     therefore call Input.is_action_pressed(&"move_forward") without guards.
##
## DEPENDENCIES: none.

signal settings_changed(section: StringName)

const CONFIG_PATH := "user://settings.cfg"

## action_name -> [ {type="key", code=KEY_*} | {type="mouse", index=MOUSE_BUTTON_*} ]
const DEFAULT_ACTIONS := {
	&"move_forward": [{"type": "key", "code": KEY_W}],
	&"move_back": [{"type": "key", "code": KEY_S}],
	&"move_left": [{"type": "key", "code": KEY_A}],
	&"move_right": [{"type": "key", "code": KEY_D}],
	&"move_up": [{"type": "key", "code": KEY_SPACE}],
	&"move_down": [{"type": "key", "code": KEY_CTRL}],
	&"sprint": [{"type": "key", "code": KEY_SHIFT}],
	&"consume": [{"type": "mouse", "index": MOUSE_BUTTON_LEFT}],
	&"interact": [{"type": "key", "code": KEY_E}],
	&"cycle_camera": [{"type": "key", "code": KEY_V}],
	&"evolution_panel": [{"type": "key", "code": KEY_TAB}],
	&"pause_menu": [{"type": "key", "code": KEY_ESCAPE}],
	&"toggle_debug": [{"type": "key", "code": KEY_F3}],
	&"quick_save": [{"type": "key", "code": KEY_F5}],
	&"dash": [{"type": "key", "code": KEY_Q}],
	&"sonar": [{"type": "key", "code": KEY_R}],
    &"photo_mode": [{"type": "key", "code": KEY_P}],
}

# --- Gameplay -----------------------------------------------------------------
var mouse_sensitivity: float = 0.0022
var invert_y: bool = false
var base_fov: float = 74.0
var camera_smoothing: float = 1.0
var head_bob_enabled: bool = true

# --- Graphics -----------------------------------------------------------------
var volumetric_fog_enabled: bool = true
var ssao_enabled: bool = true
var ssr_enabled: bool = true
var shadow_quality: int = 2   # 0 low, 1 medium, 2 high
var render_scale: float = 1.0
var vegetation_density: float = 1.0

# --- Audio (linear 0..1) -------------------------------------------------------
var volume_master: float = 0.9
var volume_music: float = 0.7
var volume_sfx: float = 0.9
var volume_ambience: float = 0.85


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	ensure_input_actions()
	load_settings()
	apply_all()


## Adds any missing action / event so the game is never un-playable.
func ensure_input_actions() -> void:
	for action_name in DEFAULT_ACTIONS:
		if not InputMap.has_action(action_name):
			InputMap.add_action(action_name)
		if InputMap.action_get_events(action_name).is_empty():
			for spec in DEFAULT_ACTIONS[action_name]:
				InputMap.action_add_event(action_name, _make_event(spec))


func _make_event(spec: Dictionary) -> InputEvent:
	if spec.get("type", "key") == "mouse":
		var mouse := InputEventMouseButton.new()
		mouse.button_index = int(spec["index"])
		return mouse
	var key := InputEventKey.new()
	key.physical_keycode = int(spec["code"])
	return key


func load_settings() -> void:
	var config := ConfigFile.new()
	if config.load(CONFIG_PATH) != OK:
		return
	mouse_sensitivity = config.get_value("gameplay", "mouse_sensitivity", mouse_sensitivity)
	invert_y = config.get_value("gameplay", "invert_y", invert_y)
	base_fov = config.get_value("gameplay", "base_fov", base_fov)
	camera_smoothing = config.get_value("gameplay", "camera_smoothing", camera_smoothing)
	head_bob_enabled = config.get_value("gameplay", "head_bob", head_bob_enabled)

	volumetric_fog_enabled = config.get_value("graphics", "volumetric_fog", volumetric_fog_enabled)
	ssao_enabled = config.get_value("graphics", "ssao", ssao_enabled)
	ssr_enabled = config.get_value("graphics", "ssr", ssr_enabled)
	shadow_quality = config.get_value("graphics", "shadow_quality", shadow_quality)
	render_scale = config.get_value("graphics", "render_scale", render_scale)
	vegetation_density = config.get_value("graphics", "vegetation_density", vegetation_density)

	volume_master = config.get_value("audio", "master", volume_master)
	volume_music = config.get_value("audio", "music", volume_music)
	volume_sfx = config.get_value("audio", "sfx", volume_sfx)
	volume_ambience = config.get_value("audio", "ambience", volume_ambience)


func save_settings() -> void:
	var config := ConfigFile.new()
	config.set_value("gameplay", "mouse_sensitivity", mouse_sensitivity)
	config.set_value("gameplay", "invert_y", invert_y)
	config.set_value("gameplay", "base_fov", base_fov)
	config.set_value("gameplay", "camera_smoothing", camera_smoothing)
	config.set_value("gameplay", "head_bob", head_bob_enabled)
	config.set_value("graphics", "volumetric_fog", volumetric_fog_enabled)
	config.set_value("graphics", "ssao", ssao_enabled)
	config.set_value("graphics", "ssr", ssr_enabled)
	config.set_value("graphics", "shadow_quality", shadow_quality)
	config.set_value("graphics", "render_scale", render_scale)
	config.set_value("graphics", "vegetation_density", vegetation_density)
	config.set_value("audio", "master", volume_master)
	config.set_value("audio", "music", volume_music)
	config.set_value("audio", "sfx", volume_sfx)
	config.set_value("audio", "ambience", volume_ambience)
	config.save(CONFIG_PATH)


func apply_all() -> void:
	apply_audio()
	apply_graphics()
	settings_changed.emit(&"all")


func apply_audio() -> void:
	_set_bus_volume("Master", volume_master)
	_set_bus_volume("Music", volume_music)
	_set_bus_volume("SFX", volume_sfx)
	_set_bus_volume("Ambience", volume_ambience)


func _set_bus_volume(bus_name: String, linear: float) -> void:
	var idx := AudioServer.get_bus_index(bus_name)
	if idx == -1:
		return
	AudioServer.set_bus_volume_db(idx, linear_to_db(clampf(linear, 0.0001, 1.0)))


## Graphics settings that live on the Viewport / RenderingServer rather than on
## the Environment resource. Environment-side toggles (fog, SSAO, SSR) are read
## by EnvironmentDirector when it applies a profile.
func apply_graphics() -> void:
	var viewport := get_viewport()
	if viewport == null:
		return
	viewport.scaling_3d_scale = clampf(render_scale, 0.5, 2.0)
	match shadow_quality:
		0:
			RenderingServer.directional_shadow_atlas_set_size(2048, true)
			viewport.positional_shadow_atlas_size = 2048
		1:
			RenderingServer.directional_shadow_atlas_set_size(4096, true)
			viewport.positional_shadow_atlas_size = 4096
		_:
			RenderingServer.directional_shadow_atlas_set_size(8192, true)
			viewport.positional_shadow_atlas_size = 8192
	settings_changed.emit(&"graphics")
