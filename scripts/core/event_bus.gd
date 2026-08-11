extends Node
## EventBus — global, dependency-free signal hub (AUTOLOAD: "EventBus").
##
## ARCHITECTURE NOTE
## The EventBus exists so that gameplay systems never need direct references to
## one another. A consumption sensor does not know the HUD exists; it emits
## [signal prey_consumed] and any number of listeners (HUD, audio, VFX,
## analytics, achievements) react.
##
## RULES FOR THIS FILE
##  1. Signals only. No state, no logic, no node lookups.
##  2. Every signal is typed and documented.
##  3. High-frequency data (per-frame velocity, per-frame position) must NEVER
##     travel through here. Use direct references or polling for those; the bus
##     is for *events*, not for streams.
##
## DEPENDENCIES: none (this must remain the leaf of the dependency graph).

# --- Session / flow -----------------------------------------------------------

## Emitted after GameDirector has committed a species choice and before the
## environment scene begins loading. Payload: the chosen OrganismData.
signal session_requested(organism: Resource)
## Emitted once the gameplay scene is live and the player pawn exists.
signal session_started(organism: Resource)
## Emitted when returning to the menu or when the run terminates.
## `reason` is one of: "quit", "death", "ascension".
signal session_ended(reason: StringName)
## Emitted whenever the tree pause state is toggled by the pause menu.
signal pause_toggled(is_paused: bool)

# --- Scene streaming ----------------------------------------------------------

signal scene_load_started(path: String)
signal scene_load_progress(ratio: float)
signal scene_load_finished(path: String)

# --- Player lifecycle ---------------------------------------------------------

## The player pawn finished initialising and is registered with GameDirector.
signal player_spawned(pawn: Node3D)
signal player_despawned(pawn: Node3D)

# --- Vitals (emitted at low frequency; see MetabolismComponent) ---------------

signal energy_changed(current: float, maximum: float)
signal health_changed(current: float, maximum: float)
signal mass_changed(current: float, capacity: float)
signal age_changed(seconds: float)
## Fired when the organism's rendered size changes meaningfully (>1%).
signal scale_changed(scale_factor: float)

# --- Evolution ----------------------------------------------------------------

signal evolution_points_changed(current: float, required: float)
signal evolution_stage_changed(stage: Resource, previous: Resource)
## More than one descendant stage is available: the UI must ask the player.
signal evolution_branch_offered(options: Array)
signal ability_unlocked(ability: Resource)
signal movement_capability_unlocked(state_id: int)

# --- Feeding / combat ---------------------------------------------------------

## payload keys: mass, energy, evolution_points, tags, position, source_name
signal prey_consumed(payload: Dictionary)
signal consumption_rejected(reason: StringName, target_name: String)
signal player_damaged(amount: float, source_name: String)

# --- Locomotion & camera ------------------------------------------------------

signal movement_state_changed(new_state: int, previous_state: int)
signal camera_mode_changed(mode_id: int)
## Any system may request a shake; the CameraRig is the only listener.
signal camera_shake_requested(strength: float, duration: float)

# --- Environment --------------------------------------------------------------

signal environment_profile_requested(profile: Resource, blend_time: float)
signal submersion_changed(is_submerged: bool, depth: float)
signal biome_entered(biome_id: StringName)
signal day_phase_changed(phase: StringName, time_of_day: float)
signal gravity_well_entered(well: Node3D)
signal gravity_well_exited(well: Node3D)
signal event_horizon_crossed(body: Node3D, well: Node3D)

# --- Presentation / UI --------------------------------------------------------

## Short-lived on-screen message. `kind`: "info" | "good" | "warn" | "epic"
signal toast_requested(text: String, kind: StringName)
## Full-screen cinematic caption used for stage transitions.
signal cinematic_caption_requested(title: String, subtitle: String, duration: float)
signal objective_changed(text: String)


## A sonar/ping ability broadcast: position + radius. The compass/HUD can use
## it to briefly reveal nearby prey.
signal sonar_ping(position: Vector3, radius: float)


## Objective/quest guidance: text + a world position to point the compass at.
signal objective_changed(text: String, target_position: Vector3)
