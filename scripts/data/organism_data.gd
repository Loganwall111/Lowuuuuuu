class_name OrganismData
extends Resource
## OrganismData — the top-level, designer-authored species definition.
## This single resource wires together body, diet, evolution tree, starting
## environment and starting scene. Adding a new species = adding one of these
## (Editor: create a new .tres), no core code changes.

@export_group("Identity")
@export var species_id: StringName
@export var display_name: String
@export var category: StringName = &"general"   ## &"insect" | &"marine" | &"mammal"
@export_multiline var summary: String
@export var menu_order: int = 100
@export var playable: bool = true
@export var accent_color: Color = Color(0.4, 0.85, 0.7)

@export_group("Start State")
@export var starting_environment: PackedScene
@export var player_scene: PackedScene
@export var starting_mass: float = 0.5
@export var starting_energy: float = 100.0
@export var starting_health: float = 100.0
@export var growth_rate: float = 1.0
@export var base_movement_type: int = 0        ## LocomotionTypes.State.SWIMMING
@export var starting_abilities: Array[StringName] = []

@export_group("Systems")
@export var body_recipe: CreatureBodyRecipe
@export var evolution_tree: EvolutionTreeData
@export var diet: DietProfile
@export var environment_profile: EnvironmentProfile

func get_root_stage() -> EvolutionStageData:
	if evolution_tree == null:
		return null
	return evolution_tree.get_root()

func get_stage(stage_id: StringName) -> EvolutionStageData:
	if evolution_tree == null:
		return null
	return evolution_tree.get_stage(stage_id)

func _to_string() -> String:
	return "Organism:%s" % display_name
