class_name ProceduralCreatureFactory
extends RefCounted
## ProceduralCreatureFactory — builds a placeholder creature from a
## CreatureBodyRecipe so the project is fully playable with ZERO imported art.
## When a real .glb is dropped into a pawn's MeshRoot, the pawn uses that model
## instead and this factory is bypassed. The recipe's material palette still
## informs the look (wetness -> specular sheen, sub_surface -> emissive rim).
##
## ATTACH / CALL: called by PlayerPawn and PreyCreature via
##   ProceduralCreatureFactory.build(recipe, stage=null) -> Array[MeshInstance3D]
## The caller parents the returned meshes under its MeshRoot.
##
## DEPENDENCIES: CreatureBodyRecipe, CreaturePartSpec, EvolutionStageData.

const EYE_EMISSIVE := Color(0.9, 0.97, 1.0)
const SHELL_SPECULAR := 0.9


static func build(recipe: CreatureBodyRecipe, stage = null) -> Array[MeshInstance3D]:
	var meshes: Array[MeshInstance3D] = []
	if recipe == null:
		meshes.append(_make_default_blob())
		return meshes
	var parts := recipe.resolve_parts()
	if parts.is_empty():
		meshes.append(_make_default_blob())
		return meshes
	# Stage-driven visual transformation: a silhouette override reshapes the
	# body so evolving visibly changes the creature, not just its size.
	var silhouette_override := -1
	if stage != null and "silhouette_override" in stage and stage.silhouette_override >= 0:
		silhouette_override = stage.silhouette_override
		# Elongate the primary body part to read as a new form.
		if parts.size() > 0:
			parts[0]["size"] = Vector3(parts[0]["size"].x * 1.2, parts[0]["size"].y * 1.4, parts[0]["size"].z * 1.2)
	for part in parts:
		meshes.append(_build_part(part, recipe))
	return meshes


static func _build_part(part: Dictionary, recipe: CreatureBodyRecipe) -> MeshInstance3D:
	var mi := MeshInstance3D.new()
	mi.name = "Part"
	mi.mesh = _geometry_for(part["shape"], part["size"])
	mi.position = part["position"]
	mi.rotation_degrees = part["rotation"]
	var mat := StandardMaterial3D.new()
	var col: Color = part["color"]
	mat.albedo_color = col
	mat.roughness = recipe.roughness
	mat.metallic = recipe.metallic
	if part["eye"]:
		mat.roughness = 0.05
		mat.metallic = 0.0
		mat.specular_intensity = 1.0
		mat.emission_enabled = true
		mat.emission = EYE_EMISSIVE
		mat.emission_intensity = 0.6
	else:
		mat.specular_intensity = clampf(recipe.wetness, 0.0, 1.0)
		mat.emission_enabled = recipe.sub_surface > 0.01
		mat.emission = col * 0.5
		mat.emission_intensity = recipe.sub_surface * 0.35
	mi.material_override = mat
	return mi


static func _geometry_for(shape: int, size: Vector3) -> Mesh:
	match shape:
		CreaturePartSpec.Shape.SPHERE:
			var s := SphereMesh.new()
			s.radius = maxf(size.x, 0.05) * 0.5
			s.height = size.y
			return s
		CreaturePartSpec.Shape.CAPSULE:
			return _styled_capsule(size)
		CreaturePartSpec.Shape.BOX:
			var b := BoxMesh.new()
			b.size = size
			return b
		CreaturePartSpec.Shape.CYLINDER:
			var cyl := CylinderMesh.new()
			cyl.radius = maxf(size.x, size.y) * 0.5
			cyl.height = size.z
			return cyl
	var fb := SphereMesh.new()
	fb.radius = 0.5
	return fb


static func _styled_capsule(size: Vector3) -> CapsuleMesh:
	var c := CapsuleMesh.new()
	c.radius = maxf(size.x, size.y) * 0.5
	c.height = size.z + c.radius * 2.0
	return c


static func _make_default_blob() -> MeshInstance3D:
	var mi := MeshInstance3D.new()
	mi.name = "Blob"
	var s := SphereMesh.new()
	s.radius = 0.5
	s.height = 1.0
	mi.mesh = s
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.5, 0.7, 0.6)
	mi.material_override = mat
	return mi
