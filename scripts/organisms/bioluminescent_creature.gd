class_name BioluminescentCreature
extends CreatureAgent
## BioluminescentCreature — a CreatureAgent variant that glows: its material gets a
## strong emissive pulse and it carries a small PointLight3D, so a school of them
## lights the abyss (Subnautica-style). Used in deep/reef biomes for atmosphere
## and to demonstrate per-creature dynamic lighting without an expensive global
## light count.
##
## ATTACH THIS SCRIPT TO: PreyCreature variant (Node3D) with a MeshInstance3D.

@export var glow_color: Color = Color(0.3, 0.9, 1.0)
@export var glow_intensity: float = 1.6
@export var pulse_speed: float = 2.0

var _light: PointLight3D
var _t: float = 0.0


func _ready() -> void:
	super._ready()
	_add_glow()
	# Bioluminescent creatures are usually non-predatory drifters.
	is_predator = false


func _add_glow() -> void:
	var mesh := get_node_or_null(^"MeshInstance3D") as MeshInstance3D
	if mesh != null and mesh.material_override == null:
		var mat := StandardMaterial3D.new()
		mat.albedo_color = glow_color
		mat.emission_enabled = true
		mat.emission = glow_color
		mat.emission_intensity = glow_intensity
		mat.roughness = 0.4
		mesh.material_override = mat
	_light = PointLight3D.new()
	_light.light_color = glow_color
	_light.light_energy = glow_intensity * 2.0
	_light.omni_range = 8.0
	_light.position = Vector3(0, 0.4, 0)
	add_child(_light)


func _process(delta: float) -> void:
	_t += delta
	if _light != null:
		_light.light_energy = glow_intensity * 2.0 * (0.6 + 0.4 * sin(_t * pulse_speed))
