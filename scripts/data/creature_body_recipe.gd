class_name CreatureBodyRecipe
extends Resource
## CreatureBodyRecipe — procedural placeholder model spec.
## Lets the game run with zero imported assets. ProceduralCreatureFactory reads
## `silhouette` + `parts` to build a MeshRoot, then the recipe's palette drives
## PBR materials (wetness, subsurface, metallic) so even placeholders look
## "biological". Drop a real .glb into MeshRoot and the recipe is bypassed.

enum Silhouette { CAPSULE, FISH, QUADRUPED, AVIAN, SPHERE }

@export var silhouette: int = Silhouette.CAPSULE
@export var parts: Array[CreaturePartSpec] = []

@export_group("Material Palette")
@export var default_color: Color = Color(0.45, 0.68, 0.6)
@export var accent_color: Color = Color(0.9, 0.4, 0.3)
@export var eye_color: Color = Color(0.9, 0.95, 1.0)
@export var roughness: float = 0.55
@export var metallic: float = 0.0
@export var wetness: float = 0.4        ## strength of specular sheen
@export var sub_surface: float = 0.3    ## fake SSS via emission-ish rim

## Builds an Array of part dicts (shape, size, position, rotation, color, eye)
## for ProceduralCreatureFactory. Keeping the colour resolution here means a
## designer can retint a creature without touching the factory.
func resolve_parts() -> Array:
	var out: Array = []
	for spec in parts:
		var col := default_color
		if spec.role == CreaturePartSpec.Role.ACCENT:
			col = accent_color
		elif spec.role == CreaturePartSpec.Role.EYE:
			col = eye_color
		out.append({
			"shape": spec.shape,
			"size": spec.size,
			"position": spec.position,
			"rotation": spec.rotation_deg,
			"color": col,
			"eye": spec.is_eye,
		})
	return out
