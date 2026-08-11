class_name ScatterManager
extends RefCounted
## ScatterManager — places many static instances efficiently with a
## MultiMeshInstance3D (a single draw call for thousands of rocks/coral/grass).
## This is the cheap alternative to "instantiate 2000 MeshInstances", and is the
## backbone of the "hundreds of organisms, large environments" performance goal.
##
## CALLED BY: biome scenes in _ready. Returns the node to add to the tree.

static func scatter_mesh(mesh: Mesh, material: Material, count: int, area: AABB,
		seed: int, min_scale: float = 0.6, max_scale: float = 1.4,
		align_to_normal: bool = false) -> MultiMeshInstance3D:
	var mm := MultiMeshInstance3D.new()
	var multimesh := MultiMesh.new()
	multimesh.mesh = mesh
	multimesh.surface_material_override = material
	multimesh.transform_format = MultiMesh.TRANSFORM_3D
	multimesh.use_custom_data = false
	multimesh.instance_count = count
	mm.multimesh = multimesh

	var rng := RandomNumberGenerator.new()
	rng.seed = seed
	for i in count:
		var px := rng.randf_range(area.position.x, area.position.x + area.size.x)
		var pz := rng.randf_range(area.position.z, area.position.z + area.size.z)
		var py := area.position.y
		var s := rng.randf_range(min_scale, max_scale)
		var rot := rng.randf_range(0.0, TAU)
		var t := Transform3D(Basis.from_euler(Vector3(0, rot, 0)), Vector3(px, py, pz))
		t = t.scaled(Vector3(s, s, s))
		multimesh.set_instance_transform(i, t)
	return mm


## Scatters a PackedScene prefab (e.g. a fish/rock) as real nodes. Use sparingly
## (these are interactive, so they cost more than a MultiMesh). `agent` nodes
## are auto-registered with SimulationDirector by the prefab itself.
static func scatter_scene(scene: PackedScene, count: int, area: AABB, seed: int,
		parent: Node, min_scale: float = 0.5, max_scale: float = 1.5) -> Array[Node]:
	var out: Array[Node] = []
	if scene == null:
		return out
	var rng := RandomNumberGenerator.new()
	rng.seed = seed
	for i in count:
		var inst := scene.instantiate()
		parent.add_child(inst)
		var px := rng.randf_range(area.position.x, area.position.x + area.size.x)
		var pz := rng.randf_range(area.position.z, area.position.z + area.size.z)
		var py := rng.randf_range(area.position.y, area.position.y + area.size.y)
		inst.global_position = Vector3(px, py, pz)
		var s := rng.randf_range(min_scale, max_scale)
		inst.scale = Vector3(s, s, s)
		inst.rotation.y = rng.randf_range(0.0, TAU)
		out.append(inst)
	return out
