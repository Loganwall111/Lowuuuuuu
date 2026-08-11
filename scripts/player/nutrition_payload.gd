class_name NutritionPayload
extends RefCounted
## NutritionPayload — the result of a successful consumption, passed from the
## eaten entity to the consumer's Growth/Metabolism/Evolution components.
## A RefCounted (not a Resource) because it is transient and never saved.

var mass: float = 0.0
var energy: float = 0.0
var evolution_points: float = 0.0
var tags: PackedStringArray = []
var source_name: String = "prey"

func _to_string() -> String:
	return "Nutrition{%.3f mass, %.1f ep, %s}" % [mass, evolution_points, source_name]
