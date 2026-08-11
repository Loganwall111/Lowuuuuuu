extends Node
## AudioDirector — bus topology, pooled 3D one-shots and ambience crossfades
## (AUTOLOAD: "AudioDirector").
##
## The bus layout is created in code so the project does not depend on a
## default_bus_layout.tres that an artist might overwrite:
##
##     Master
##     ├── Music
##     ├── Ambience   (+ low-pass, muffled underwater)
##     ├── SFX        (+ low-pass, muffled underwater)
##     └── UI
##
## Underwater muffling is a real filter sweep on the Ambience/SFX buses rather
## than a set of duplicate "wet" sound files — one line of code replaces an
## entire second sound bank.
##
## DEPENDENCIES: EventBus (listens to submersion_changed), PoolService is NOT
## used here because AudioStreamPlayer3D needs its own tiny fixed pool.

const VOICE_COUNT := 24 ## Hard cap on simultaneous positional one-shots.
const UNDERWATER_CUTOFF := 900.0
const DRY_CUTOFF := 20000.0

var _voices: Array[AudioStreamPlayer3D] = []
var _voice_cursor: int = 0
var _ambience_a: AudioStreamPlayer
var _ambience_b: AudioStreamPlayer
var _ambience_uses_a: bool = true
var _music: AudioStreamPlayer

var _filters: Array[AudioEffectLowPassFilter] = []


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_ensure_buses()
	_build_players()
	EventBus.submersion_changed.connect(_on_submersion_changed)


func _ensure_buses() -> void:
	for bus_name in ["Music", "Ambience", "SFX", "UI"]:
		if AudioServer.get_bus_index(bus_name) != -1:
			continue
		var idx := AudioServer.bus_count
		AudioServer.add_bus(idx)
		AudioServer.set_bus_name(idx, bus_name)
		AudioServer.set_bus_send(idx, "Master")
	for bus_name in ["Ambience", "SFX"]:
		var idx := AudioServer.get_bus_index(bus_name)
		var filter := AudioEffectLowPassFilter.new()
		filter.cutoff_hz = DRY_CUTOFF
		AudioServer.add_bus_effect(idx, filter)
		_filters.append(filter)


func _build_players() -> void:
	for i in VOICE_COUNT:
		var player := AudioStreamPlayer3D.new()
		player.bus = "SFX"
		player.max_distance = 90.0
		player.unit_size = 4.0
		player.attenuation_model = AudioStreamPlayer3D.ATTENUATION_INVERSE_SQUARE_DISTANCE
		add_child(player)
		_voices.append(player)

	_ambience_a = _make_stream_player("Ambience")
	_ambience_b = _make_stream_player("Ambience")
	_music = _make_stream_player("Music")


func _make_stream_player(bus: String) -> AudioStreamPlayer:
	var player := AudioStreamPlayer.new()
	player.bus = bus
	player.volume_db = -80.0
	add_child(player)
	return player


## Fire-and-forget positional sound. Silently no-ops when `stream` is null so
## gameplay code never needs `if stream != null` guards while audio is still
## being authored.
func play_3d(stream: AudioStream, position: Vector3, volume_db: float = 0.0, pitch_jitter: float = 0.08) -> void:
	if stream == null:
		return
	var player := _voices[_voice_cursor]
	_voice_cursor = (_voice_cursor + 1) % VOICE_COUNT
	player.stream = stream
	player.global_position = position
	player.volume_db = volume_db
	player.pitch_scale = 1.0 + randf_range(-pitch_jitter, pitch_jitter)
	player.play()


func play_ui(stream: AudioStream, volume_db: float = 0.0) -> void:
	if stream == null:
		return
	var player := AudioStreamPlayer.new()
	player.bus = "UI"
	player.stream = stream
	player.volume_db = volume_db
	add_child(player)
	player.finished.connect(player.queue_free)
	player.play()


## Crossfades to a new looping ambience bed. Passing null fades to silence.
func set_ambience(stream: AudioStream, fade_time: float = 2.5, volume_db: float = -8.0) -> void:
	var incoming: AudioStreamPlayer = _ambience_b if _ambience_uses_a else _ambience_a
	var outgoing: AudioStreamPlayer = _ambience_a if _ambience_uses_a else _ambience_b
	_ambience_uses_a = not _ambience_uses_a

	if stream != null:
		incoming.stream = stream
		incoming.volume_db = -80.0
		incoming.play()
		var tween_in := create_tween()
		tween_in.tween_property(incoming, "volume_db", volume_db, fade_time)

	if outgoing.playing:
		var tween_out := create_tween()
		tween_out.tween_property(outgoing, "volume_db", -80.0, fade_time)
		tween_out.tween_callback(outgoing.stop)


func set_music(stream: AudioStream, fade_time: float = 3.0, volume_db: float = -12.0) -> void:
	if stream == null:
		var tween_out := create_tween()
		tween_out.tween_property(_music, "volume_db", -80.0, fade_time)
		tween_out.tween_callback(_music.stop)
		return
	_music.stream = stream
	_music.volume_db = -80.0
	_music.play()
	var tween := create_tween()
	tween.tween_property(_music, "volume_db", volume_db, fade_time)


func _on_submersion_changed(is_submerged: bool, _depth: float) -> void:
	var target := UNDERWATER_CUTOFF if is_submerged else DRY_CUTOFF
	for filter in _filters:
		var tween := create_tween()
		tween.tween_property(filter, "cutoff_hz", target, 0.8)


# ---- Procedural tones (asset-free audio) -----------------------------------
var _tones: Dictionary = {}


func tone(kind: int) -> AudioStream:
	var key := str(kind)
	if _tones.has(key):
		return _tones[key]
	var stream := ProceduralAudioKit.make_tone(kind)
	_tones[key] = stream
	return stream


func eat(pos: Vector3 = Vector3.ZERO) -> void:
	play_3d(tone(ProceduralAudioKit.ToneKind.EAT), pos, -6.0)


func evolve() -> void:
	play_ui(tone(ProceduralAudioKit.ToneKind.EVOLVE), -2.0)


func hurt() -> void:
	play_ui(tone(ProceduralAudioKit.ToneKind.HURT), -1.0)


func click() -> void:
	play_ui(tone(ProceduralAudioKit.ToneKind.CLICK), -8.0)
