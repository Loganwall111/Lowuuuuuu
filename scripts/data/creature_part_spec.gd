class_name CreaturePartSpec
extends Resource
## CreaturePartSpec — one primitive in a procedural placeholder creature.
## Used by CreatureBodyRecipe to build a silhouette at runtime so the game is
## fully playable before an artist imports a .glb. When a real model is dropped
## into MeshRoot, the recipe is ignored for silhouette but its material palette
## (creature_body_recipe.gd) still drives tints and PBR response.

enum Shape { SPHERE, CAPSULE, BOX, CYLINDER }
enum Role { BODY, ACCENT, EYE }

@export var shape: int = Shape.CAPSULE
@export var size: Vector3 = Vector3(1.0, 1.0, 1.0)
@export var position: Vector3 = Vector3.ZERO
@export var rotation_deg: Vector3 = Vector3.ZERO
@export var role: int = Role.BODY
@export var is_eye: bool = false
