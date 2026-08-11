class_name PlayerPawn
extends CharacterBody3D
## PlayerPawn — the living avatar the player controls across ALL stages.
##
## Intentionally thin: it composes components and orchestrates per-frame, but
## owns NO gameplay rule (growth/feeding/evolution/movement/camera are each a
## separate component). This is what lets the SAME pawn become a larva, fish,
## primate and (later) be swapped for a spaceship without rewriting rules.
##
## ATTACH THIS SCRIPT TO:
##   PlayerPawn (CharacterBody3D)            <-- here
##   ├── CollisionShape3D  (capsule; radius/height driven by GrowthComponent)
##   ├── MeshRoot (Node3D)  <-- drop imported .glb here (optional; recipe fills)
##   │   └── (ImportedCreature.glb)  [optional]
##   ├── CameraRig (Node3D)  <-- scripts/camera/camera_rig.gd
##   ├── ConsumptionSensor (Area3D)  <-- scripts/player/consumption_sensor.gd
##   │   └── CollisionShape3D (sphere, interaction radius)
##   ├── EnvironmentProbe (Node)  <-- scripts/player/environment_probe.gd
##   ├── GrowthComponent (Node)   <-- scripts/player/growth_component.gd
##   ├── MetabolismComponent (Node) <-- scripts/player/metabolism_component.gd
##   ├── EvolutionComponent (Node) <-- scripts/player/evolution_component.gd
##   └── LocomotionController (Node) <-- scripts/player/locomotion/locomotion_controller.gd
##
## DEPENDENCIES: all components above + OrganismData + SessionState +
## ProceduralCreatureFactory + EventBus + GameDirector.

@export_group("Placement")
@export var spawn_position: Vector3 = Vector3.ZERO

var organism: OrganismData
var session: SessionState

@onready var mesh_root: Node3D = $MeshRoot
@onready var camera_rig: Node = $CameraRig
@onready var probe: EnvironmentProbe = $EnvironmentProbe
@onready var growth: GrowthComponent = $GrowthComponent
@onready var metabolism: MetabolismComponent = $MetabolismComponent
@onready var evolution: EvolutionComponent = $EvolutionComponent
@onready var sensor: ConsumptionSensor = $ConsumptionSensor
@onready var locomotion: LocomotionController = $LocomotionController

var _configured: bool = false
var _last_scale: float = 1.0
var _speed_fraction: float = 0.0


func _ready() -> void:
	collision_layer = 2   # Player
	collision_mask = 1 | 4 | 6 | 7 | 9   # World, Edible, Water, Gravity, Trigger
	if GameDirector.organism_for_new_run != null:
		configure_organism(GameDirector.organism_for_new_run, GameDirector.session)


func configure_organism(org: OrganismData, run_session: SessionState) -> void:
	if org == null:
		return
	organism = org
	session = run_session  # member; renamed from param to avoid shadowing
	_build_visual(org)
	if growth != null and org.starting_mass > 0.0:
		growth.set_base_scale_from_mass(org.starting_mass, 0.18)
	if metabolism != null:
		metabolism.max_energy = org.starting_energy
		metabolism.energy = org.starting_energy
	if locomotion != null:
		locomotion.configure(org)
	if evolution != null:
		evolution.configure(org, session)
	if probe != null:
		probe.configure(_camera_basis_provider)
	if sensor != null:
		sensor.configure(org, _mass_getter, _ability_checker)
	if camera_rig != null and camera_rig.has_method("set_creature_scale"):
		camera_rig.set_creature_scale(1.0)
	_configured = true
	GameDirector.register_player(self)
	_cinematic_intro()
	var ac := get_node_or_null(^"AbilityController")
	if ac != null and ac.has_method("activate"):
		ac.activate()


func _build_visual(org: OrganismData) -> void:
	if mesh_root == null:
		return
	for child in mesh_root.get_children():
		if child is MeshInstance3D or child is Node3D:
			return  # artist-provided model present -> keep it
	for m in ProceduralCreatureFactory.build(org.body_recipe):
		mesh_root.add_child(m)


func _physics_process(delta: float) -> void:
	if not _configured:
		return
	_handle_actions()
	if locomotion != null and locomotion.active_profile != null:
		var top: float = locomotion.active_profile.effective_max_speed(locomotion.body_scale)
		_speed_fraction = clampf(velocity.length() / maxf(top, 0.001), 0.0, 1.0)
	if metabolism != null:
		metabolism.report_activity(_speed_fraction, locomotion.active_profile if locomotion != null else null)
		if evolution != null:
			evolution.report_age(metabolism.age_seconds)
	if growth != null and abs(growth.current_scale - _last_scale) > 0.012:
		_last_scale = growth.current_scale
		EventBus.scale_changed.emit(growth.current_scale)
		if camera_rig != null and camera_rig.has_method("set_creature_scale"):
			camera_rig.set_creature_scale(growth.current_scale)


func _handle_actions() -> void:
	if Input.is_action_just_pressed(&"consume") and sensor != null:
		sensor.try_consume_nearest()
	if Input.is_action_just_pressed(&"cycle_camera") and camera_rig != null and camera_rig.has_method("cycle_mode"):
		camera_rig.cycle_mode()
	if Input.is_action_just_pressed(&"evolution_panel"):
		EventBus.evolution_branch_offered.emit(_current_branch_options())
	if Input.is_action_just_pressed(&"toggle_debug"):
		EventBus.toast_requested.emit("Debug overlay toggled", &"info")
	if Input.is_action_just_pressed(&"photo_mode"):
		toggle_photo_mode()
	if Input.is_action_just_pressed(&"ascend"):
		if GameDirector.has_method("ascend_to_space"):
			GameDirector.ascend_to_space()


func _current_branch_options() -> Array:
	if evolution == null or organism == null or organism.evolution_tree == null:
		return []
	return organism.evolution_tree.get_descendants(evolution.current_stage.stage_id)


func _on_consumed(payload: NutritionPayload, _target: Node) -> void:
	if payload == null:
		return
	if growth != null:
		var new_mass := _current_mass() + payload.mass
		growth.set_base_scale_from_mass(new_mass, 0.18)
		EventBus.mass_changed.emit(new_mass, _mass_capacity())
		if session != null:
			session.mass = new_mass
	if metabolism != null:
		metabolism.add_energy(payload.energy)
	if evolution != null:
		evolution.grant_nutrition(payload)
	if AudioDirector != null:
		AudioDirector.eat(global_position)
	EventBus.prey_consumed.emit({
		"mass": payload.mass, "energy": payload.energy,
		"evolution_points": payload.evolution_points,
		"tags": payload.tags, "position": global_position,
		"source_name": payload.source_name,
	})
	EventBus.camera_shake_requested.emit(0.12, 0.18)
	if session != null:
		session.creatures_consumed += 1
		session.biomass_consumed += payload.mass


func _current_mass() -> float:
	return session.mass if session != null else (organism.starting_mass if organism != null else 1.0)


func _mass_capacity() -> float:
	if evolution != null and evolution.current_stage != null:
		return evolution.current_stage.required_mass * 4.0
	return 10.0


func _mass_getter() -> float:
	return maxf(0.01, _current_mass())


func _ability_checker() -> Callable:
	return func(id: StringName) -> bool:
		return evolution != null and evolution.has_ability(id)


func _camera_basis_provider() -> Basis:
	if camera_rig != null and camera_rig.has_method("get_camera_basis"):
		return camera_rig.get_camera_basis()
	return global_transform.basis


## Used by the CameraRig to drive speed-based FOV.
func get_speed_fraction() -> float:
	return _speed_fraction


# ----------------------------------------------------- combat / damage

func take_damage(amount: float) -> void:
	if metabolism != null:
		metabolism.damage(amount, "predator")
	EventBus.camera_shake_requested.emit(0.3, 0.25)
	if AudioDirector != null:
		AudioDirector.hurt()


# ----------------------------------------------------- evolution morph

## Rebuilds the visible body from the (possibly stage-overridden) recipe and
## plays a cinematic flash + brief slow-motion so evolving FEELS like a
## transformation, not a number flip.
func morph_to_stage(stage: EvolutionStageData) -> void:
	if mesh_root == null:
		_on_morph_flash()
		return
	for child in mesh_root.get_children():
		child.queue_free()
	var recipe := organism.body_recipe
	if recipe != null:
		for m in ProceduralCreatureFactory.build(recipe, stage):
			mesh_root.add_child(m)
	_on_morph_flash()


func _on_morph_flash() -> void:
	var layer := CanvasLayer.new()
	layer.layer = 9
	add_child(layer)
	var flash := ColorRect.new()
	flash.color = Color(0.9, 0.95, 1.0, 0.0)
	flash.set_anchors_preset(Control.PRESET_FULL_RECT)
	flash.mouse_filter = Control.MOUSE_FILTER_IGNORE
	layer.add_child(flash)
	var tw := create_tween()
	tw.tween_property(flash, "color:a", 0.7, 0.12)
	tw.tween_property(flash, "color:a", 0.0, 0.5)
	tw.tween_callback(layer.queue_free)
	Engine.time_scale = 0.35
	get_tree().create_timer(0.5).timeout.connect(func() -> void: Engine.time_scale = 1.0)


# ----------------------------------------------------- cinematic spawn intro

func _cinematic_intro() -> void:
	if camera_rig != null and camera_rig.has_method("set_mode"):
		camera_rig.set_mode(CameraTypes.Mode.CINEMATIC)
		get_tree().create_timer(3.2).timeout.connect(func() -> void:
			if is_instance_valid(self) and camera_rig != null and camera_rig.has_method("set_mode"):
				camera_rig.set_mode(CameraTypes.Mode.THIRD_PERSON))
	EventBus.cinematic_caption_requested.emit("You are " + organism.display_name, organism.summary, 3.0)


# ----------------------------------------------------- photo mode

## Pause-friendly "inspect the creature" mode: hides gameplay UI (via EventBus)
## and switches the camera to a slow orbit. Press again to exit.
func toggle_photo_mode() -> void:
	if camera_rig == null or not camera_rig.has_method("set_mode"):
		return
	var inspecting := camera_rig.current_mode_id == CameraTypes.Mode.INSPECTION
	if inspecting:
		GameDirector.set_paused(false)
		camera_rig.set_mode(CameraTypes.Mode.THIRD_PERSON)
		EventBus.toast_requested.emit("Photo mode off", &"info")
	else:
		GameDirector.set_paused(true)
		camera_rig.set_mode(CameraTypes.Mode.INSPECTION)
		EventBus.toast_requested.emit("Photo mode — drag to orbit", &"info")
