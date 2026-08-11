class_name BiomeDeepSpace
extends BaseBiome
## BiomeDeepSpace — the future "space" stage testbed. No terrain: a starfield, a
## BlackHole (reusable GravityWell3D) plus planets/asteroids built by
## OrbitalBodyFactory (each also a gravity well), and a swirling accretion disc.
## When the player ASCENDS, a SpaceshipPawn is spawned instead of the creature,
## proving the same camera/HUD/gravity primitives scale cell -> cosmos.
##
## ATTACH THIS SCRIPT TO: DeepSpaceBiome (Node3D)  (DeepSpaceBiome.tscn)

@export var asteroid_scene: PackedScene
@export var black_hole_scene: PackedScene
@export var spaceship_scene: PackedScene
@export var asteroid_count: int = 40
@export var planet_count: int = 4

var _swirl: MeshInstance3D

func _ready() -> void:
	biome_id = &"deep_space"
	_build_world()

func _build_world() -> void:
	_add_starfield()
	if black_hole_scene == null:
		black_hole_scene = load("res://scenes/space/BlackHole.tscn")
	if black_hole_scene != null:
		var bh := black_hole_scene.instantiate()
		bh.global_position = Vector3(0, 0, -300)
		add_child(bh)
		_add_swirl(bh)
	_spawn_planets()
	_spawn_asteroids()
	if GameDirector.space_mode and spaceship_scene != null:
		_spawn_ship()

func _add_swirl(black_hole: Node3D) -> void:
	_swirl = MeshInstance3D.new()
	var torus := TorusMesh.new()
	torus.inner_radius = 6.0
	torus.outer_radius = 9.0
	torus.height = 0.6
	_swirl.mesh = torus
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.9, 0.6, 0.3)
	mat.emission_enabled = true
	mat.emission = Color(1.0, 0.5, 0.2)
	mat.emission_intensity = 1.2
	mat.roughness = 0.4
	_swirl.material_override = mat
	_swirl.position = black_hole.position
	add_child(_swirl)

func _add_starfield() -> void:
	var mm := MultiMeshInstance3D.new()
	mm.name = "Starfield"
	var m := SphereMesh.new()
	m.radius = 0.5
	m.height = 1.0
	mm.multimesh = MultiMesh.new()
	mm.multimesh.mesh = m
	mm.multimesh.transform_format = MultiMesh.TRANSFORM_3D
	mm.multimesh.instance_count = 1200
	var rng := RandomNumberGenerator.new()
	rng.seed = 99
	for i in 1200:
		var dir := Vector3(rng.randf_range(-1, 1), rng.randf_range(-1, 1), rng.randf_range(-1, 1)).normalized()
		var p := dir * rng.randf_range(400.0, 900.0)
		mm.multimesh.set_instance_transform(i, Transform3D(Basis(), p))
	add_child(mm)

func _spawn_planets() -> void:
	if asteroid_scene == null:
		asteroid_scene = load("res://scenes/space/DebrisBody.tscn")
	for i in planet_count:
		var radius := 14.0 + i * 6.0
		var strength := 80.0 + i * 40.0
		var planet := OrbitalBodyFactory.make_planet(radius, strength, Color(0.3, 0.4 + i * 0.1, 0.7))
		var ang := float(i) / planet_count * TAU
		planet.global_position = Vector3(cos(ang) * (120.0 + i * 40.0), rng_off(i), sin(ang) * (120.0 + i * 40.0))
		add_child(planet)

func rng_off(i: int) -> float:
	return (i % 2) * 30.0 - 15.0

func _spawn_asteroids() -> void:
	if asteroid_scene == null:
		asteroid_scene = load("res://scenes/space/DebrisBody.tscn")
	var rng := RandomNumberGenerator.new()
	rng.seed = 123
	for i in asteroid_count:
		var a := asteroid_scene.instantiate()
		var ang := rng.randf_range(0, TAU)
		var rad := rng.randf_range(40.0, 260.0)
		a.global_position = Vector3(cos(ang) * rad, rng.randf_range(-40, 40), sin(ang) * rad)
		add_child(a)

func _spawn_ship() -> void:
	var ship := spaceship_scene.instantiate()
	add_child(ship)
	ship.global_position = Vector3(0, 0, 60)
	if ship is Node3D:
		GameDirector.register_ship(ship)
		var rig := ship.get_node_or_null(^"CameraRig")
		if rig != null and rig.has_method("bind_target"):
			rig.bind_target(ship)

func _process(delta: float) -> void:
	if _swirl != null:
		_swirl.rotate_y(delta * 0.6)
