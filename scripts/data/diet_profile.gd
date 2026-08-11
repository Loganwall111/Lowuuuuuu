class_name DietProfile
extends Resource
## DietProfile — declarative "can I eat this?" rules for the ConsumptionSystem.
## Engineered so the actual eating logic never hard-codes a species; it only
## asks this resource.

@export_group("Tags")
@export var edible_tags: PackedStringArray = [&"plankton", &"plant", &"carrion"]
@export var forbidden_tags: PackedStringArray = [&"toxic", &"rock", &"inorganic"]

@export_group("Size Rules")
## Consumer must be at least this many times the prey's mass to bother.
@export var min_mass_ratio: float = 0.0
## Prey must be at most this many times the consumer's mass (no eating whales).
@export var max_mass_ratio: float = 0.6
@export var min_mass_for_gain: float = 0.0005

@export_group("Yield")
@export var base_energy_multiplier: float = 1.0
@export var consume_radius_padding: float = 0.25
@export var auto_consume: bool = false
@export var required_ability: StringName = &""

## Pure validation of mass + tags. The caller still verifies that the consumer
## actually possesses `required_ability` before trusting a true result.
func can_eat(consumer_mass: float, prey_mass: float, prey_tags: PackedStringArray) -> bool:
	if prey_mass < min_mass_for_gain:
		return false
	if max_mass_ratio > 0.0 and prey_mass > consumer_mass * max_mass_ratio:
		return false
	if min_mass_ratio > 0.0 and prey_mass < consumer_mass * min_mass_ratio:
		return false
	if forbidden_tags.size() > 0:
		for tag in prey_tags:
			if tag in forbidden_tags:
				return false
	if edible_tags.size() > 0:
		var hit := false
		for tag in prey_tags:
			if tag in edible_tags:
				hit = true
				break
		if not hit:
			return false
	return true
