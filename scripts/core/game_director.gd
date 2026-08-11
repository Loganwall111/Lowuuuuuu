extends Node
## GameDirector — top-level session orchestrator (AUTOLOAD: "GameDirector").
##
## RESPONSIBILITIES
##  * Owns the species catalogue (data-driven, scanned from res://resources/organisms).
##  * Owns the SessionState for the current run.
##  * Starts / ends runs and asks SceneDirector to stream environments.
##  * Holds the single authoritative reference to the live player pawn.
##  * Owns pause state.
##
## IT DOES NOT
##  * Implement gameplay rules (evolution, feeding, movement) — those live in
##    components on the pawn.
##  * Touch UI directly — it emits through EventBus.
##
## DEPENDENCIES: EventBus, SceneDirector, SaveService, SessionState, OrganismData.

const ORGANISM_DIR := "res://resources/organisms"

## Cached species catalogue: species_id -> OrganismData
var _catalogue: Dictionary = {}
var session: SessionState = null
var player: Node3D = null
var is_paused: bool = false
var space_mode: bool = false
var ship: Node3D = null

## Hand-off fields read by PlayerPawn._ready(). start_new_run() sets them right
## before SceneDirector streams the environment that will spawn the pawn.
var organism_for_new_run: OrganismData = null
var session_for_new_run: SessionState = null

var _playtime_accumulator: float = 0.0


func _ready() -> void:
	# The director must keep ticking while the tree is paused so that pause
	# menus, transitions and audio fades continue to work.
	process_mode = Node.PROCESS_MODE_ALWAYS
	_scan_catalogue()
	set_process(false) # only enabled while a run is live


# ------------------------------------------------------------------ catalogue

## Scans the organism resource folder once at boot. Adding a new species is
## therefore a pure content operation: drop a .tres in resources/organisms/.
func _scan_catalogue() -> void:
	_catalogue.clear()
	var dir := DirAccess.open(ORGANISM_DIR)
	if dir == null:
		push_warning("GameDirector: organism directory missing: %s" % ORGANISM_DIR)
		return
	for file_name in dir.get_files():
		# Exported builds rename .tres to .tres.remap; handle both.
		var clean := file_name.trim_suffix(".remap")
		if not clean.ends_with(".tres"):
			continue
		var path := "%s/%s" % [ORGANISM_DIR, clean]
		var res: Resource = load(path)
		if res is OrganismData:
			var organism := res as OrganismData
			if organism.species_id == &"":
				push_warning("GameDirector: %s has no species_id; skipped." % path)
				continue
			_catalogue[organism.species_id] = organism
		else:
			push_warning("GameDirector: %s is not an OrganismData resource." % path)


## Returns every playable species, sorted by their menu_order field.
func get_playable_species() -> Array[OrganismData]:
	var list: Array[OrganismData] = []
	for key in _catalogue:
		var organism: OrganismData = _catalogue[key]
		if organism.playable:
			list.append(organism)
	list.sort_custom(func(a: OrganismData, b: OrganismData) -> bool:
		return a.menu_order < b.menu_order)
	return list


func get_species(species_id: StringName) -> OrganismData:
	return _catalogue.get(species_id, null)


# ------------------------------------------------------------------- sessions

## Entry point used by the main menu. Builds a fresh SessionState from the
## organism's data and streams in its starting environment.
func start_new_run(organism: OrganismData) -> void:
	if organism == null:
		push_error("GameDirector.start_new_run: null organism.")
		return
	if organism.starting_environment == null:
		push_error("GameDirector: %s has no starting_environment scene." % organism.species_id)
		return

	session = SessionState.new()
	session.organism = organism
	session.species_id = organism.species_id
	session.mass = organism.starting_mass
	session.energy = organism.starting_energy
	session.health = organism.starting_health
	session.unlocked_abilities = organism.starting_abilities.duplicate()

	var root_stage: EvolutionStageData = organism.get_root_stage()
	if root_stage != null:
		session.stage_id = root_stage.stage_id
		session.lineage.append(root_stage.stage_id)
		session.unlocked_movement_states = root_stage.enabled_movement_states.duplicate()

	session.environment_path = organism.starting_environment.resource_path

	organism_for_new_run = organism
	session_for_new_run = session

	EventBus.session_requested.emit(organism)
	SceneDirector.change_scene_async(organism.starting_environment)
	set_process(true)


## Called by PlayerPawn once it has finished wiring its components.
func register_player(pawn: Node3D) -> void:
	player = pawn
	SimulationDirector.set_focus(pawn)
	EventBus.player_spawned.emit(pawn)
	if session != null and session.organism != null:
		EventBus.session_started.emit(session.organism)
	# Consume the one-shot hand-off so a later pawn does not re-adopt it.
	organism_for_new_run = null
	session_for_new_run = null


func unregister_player(pawn: Node3D) -> void:
	if player == pawn:
		player = null
		SimulationDirector.set_focus(null)
		EventBus.player_despawned.emit(pawn)


func end_run(reason: StringName = &"quit") -> void:
	set_process(false)
	set_paused(false)
	EventBus.session_ended.emit(reason)
	session = null
	player = null
	SceneDirector.change_scene_to_path_async("res://scenes/menu/MainMenu.tscn")


# ---------------------------------------------------------------------- pause

func set_paused(value: bool) -> void:
	if is_paused == value:
		return
	is_paused = value
	get_tree().paused = value
	Input.mouse_mode = Input.MOUSE_MODE_VISIBLE if value else Input.MOUSE_MODE_CAPTURED
	EventBus.pause_toggled.emit(value)


func toggle_pause() -> void:
	set_paused(not is_paused)


# ----------------------------------------------------------------- book-keeping

func _process(delta: float) -> void:
	if session == null or is_paused:
		return
	# Playtime is accumulated here rather than in a dozen systems that each
	# want to know "how long has this run lasted".
	session.playtime_seconds += delta
	_playtime_accumulator += delta
	if _playtime_accumulator >= 30.0:
		_playtime_accumulator = 0.0
		if player != null:
			session.last_position = player.global_position
		SaveService.autosave(session)


## Loads a previously saved SessionState and streams the player back into their
## biome at their saved stage/mass. The pawn's components read the session on
## configure_organism(), so progression continues seamlessly.
func resume_run(saved: SessionState) -> void:
	if saved == null:
		push_error("GameDirector.resume_run: null session.")
		return
	var org := get_species(saved.species_id)
	if org == null:
		push_error("GameDirector.resume_run: unknown species '%s'." % saved.species_id)
		return
	session = saved
	organism_for_new_run = org
	session_for_new_run = saved
	if saved.environment_path.is_empty():
		saved.environment_path = org.starting_environment.resource_path
	EventBus.session_requested.emit(org)
	if ResourceLoader.exists(saved.environment_path):
		SceneDirector.change_scene_to_path_async(saved.environment_path)
	else:
		SceneDirector.change_scene_async(org.starting_environment)
	set_process(true)


## --- Ascension: biology -> civilisation -> space transition ---------------------
## Streams the player into the deep-space biome piloting a SpaceshipPawn instead
## of a biological creature. The shared camera/HUD contract keeps this additive.
func ascend_to_space() -> void:
	if space_mode:
		return
	space_mode = true
	EventBus.toast_requested.emit("Ascending to the stars...", &"epic")
	EventBus.cinematic_caption_requested.emit("Among the Stars", "Your kind has left the cradle.", 4.0)
	SceneDirector.change_scene_to_path_async("res://scenes/environments/DeepSpaceBiome.tscn")
	set_process(true)

func register_ship(ship_node: Node3D) -> void:
	ship = ship_node
	SimulationDirector.set_focus(ship_node)
	EventBus.player_spawned.emit(ship_node)
