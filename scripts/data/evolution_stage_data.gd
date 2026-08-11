class_name EvolutionStageData
extends Resource
## EvolutionStageData — one node of a branching evolution tree.
## Branching is expressed as `next_stage_ids`: an empty array means a terminal
## stage; multiple entries mean the player is offered a choice.

@export_group("Identity")
@export var stage_id: StringName
@export var display_name: String
@export var display_order: int = 0
@export_multiline var lore: String
@export var biological_complexity: float = 1.0

@export_group("Progression Gates")
@export var required_mass: float = 1.0
@export var required_evolution_points: float = 0.0
@export var required_age_seconds: float = 0.0

@export_group("Body & Stats")
@export var silhouette_override: int = -1  ## >=0 reshapes the procedural body on evolution

@export var body_scale_multiplier: float = 1.0
@export var energy_capacity: float = 100.0
@export var metabolism_rate: float = 1.0
@export var sense_radius: float = 6.0
@export var base_movement_profile: MovementProfile

@export_group("Unlocks")
@export var unlocked_abilities: Array[AbilityData] = []
@export var enabled_movement_states: Array[int] = []  ## LocomotionTypes.State

@export_group("Branching")
@export var next_stage_ids: Array[StringName] = []

@export_group("Presentation")
@export var cinematic_title: String = "Evolution"
@export var cinematic_subtitle: String = ""
