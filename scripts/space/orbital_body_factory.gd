class_name OrbitalBodyFactory
extends RefCounted
## OrbitalBodyFactory — builds space set-dressing (asteroids, planets, stations)
## that all respond to gravity wells through GravityAffected. This is the
## "reuse the same physics for debris, asteroids and spaceships" proof point.
##
## CALLED BY: BiomeDeepSpace / a planet-builder tool. Returns a configured node.

static func make_asteroid(radius: float, is_asteroid: bool) -> Node3D:
	var body := preload("res://scenes/space/DebrisBody.tscn").instantiate() if ResourceLoader.exists("res://scenes/space/DebrisBody.tscn") else null
	if body == null:
		body = Node3D.new()
	var mesh := MeshInstance3D.new()
	var sph := SphereMesh.new()
	sph.radius = radius
	sph.height = radius * 2.0
	mesh.mesh = sph
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.5, 0.45, 0.4)
	mat.roughness = 0.9
	mesh.material_override = mat
	body.add_child(mesh)

	var col := CollisionShape3D.new()
	var cshape := SphereShape3D.new()
	cshape.radius = radius
	col.shape = cshape
	body.add_child(col)

	if body is DebrisBody:
		(body as DebrisBody).is_asteroid = is_asteroid
	return body


## A gravitational body (planet/star) is just a big mesh + a GravityWell3D.
static func make_planet(radius: float, gravity_strength: float, color: Color) -> Node3D:
	var root := Node3D.new()
	var mesh := MeshInstance3D.new()
	var sph := SphereMesh.new()
	sph.radius = radius
	sph.height = radius * 2.0
	mesh.mesh = sph
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	mat.emission_enabled = true
	mat.emission = color * 0.2
	mesh.material_override = mat
	root.add_child(mesh)

	var well := GravityWell3D.new()
	well.gravity_strength = gravity_strength
	well.influence_radius = radius * 40.0
	well.event_horizon_radius = radius * 1.05
	root.add_child(well)
	return root
