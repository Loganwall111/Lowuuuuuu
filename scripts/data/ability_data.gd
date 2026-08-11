class_name AbilityData
extends Resource
## AbilityData — a single unlockable capability (e.g. "Venom", "Echolocation",
## "Tool Use"). Pure data + modifiers; behaviour lives in the systems that read
## the modifiers (EvolutionComponent applies stats, LocomotionController reads
## unlocks_movement_states, ConsumptionSensor reads required_ability).

@export_group("Identity")
@export var ability_id: StringName
@export var display_name: String
@export_multiline var description: String
@export var icon: Texture2D
@export var category: StringName = &"general"
@export var tags: PackedStringArray

@export_group("Modifiers")
@export var move_speed_multiplier: float = 1.0
@export var metabolism_multiplier: float = 1.0
@export var sense_radius_multiplier: float = 1.0
@export var damage_multiplier: float = 1.0
@export var evolution_point_multiplier: float = 1.0

@export_group("Unlocks")
@export var unlocks_movement_states: Array[int] = []  ## LocomotionTypes.State
@export var unlocks_abilities: Array[StringName] = []
