class_name EnvironmentProfile
extends Resource
## EnvironmentProfile — declarative "mood" of a biome: sky, sun, fog, grading,
## audio. Applied to the live WorldEnvironment + DirectionalLight by
## EnvironmentDirector, with smooth interpolation between profiles as the
## creature moves between water/air/space.
##
## Two entry points:
##   apply(env, sun)        — full, instant structural setup (used on load).
##   gather()/lerp_values()/apply_gathered() — smooth scalar/colour blend.

@export_group("Sky & Sun")
@export var sky_top_color: Color = Color(0.05, 0.18, 0.32)
@export var sky_horizon_color: Color = Color(0.4, 0.6, 0.7)
@export var sun_color: Color = Color(1.0, 0.96, 0.85)
@export var sun_energy: float = 1.4
@export var sun_direction: Vector3 = Vector3(-0.5, -0.85, 0.25)
@export var ambient_color: Color = Color(0.3, 0.45, 0.55)
@export var ambient_energy: float = 0.5

@export_group("Atmosphere / Fog")
@export var fog_enabled: bool = true
@export var fog_color: Color = Color(0.1, 0.3, 0.38)
@export var fog_density: float = 0.025
@export var fog_light_color: Color = Color(0.7, 0.85, 0.9)
@export var height_fog_start: float = 0.0
@export var height_fog_depth: float = 50.0

@export_group("Volumetric")
@export var volumetric_fog: bool = true
@export var volumetric_fog_density: float = 0.06
@export var volumetric_fog_albedo: Color = Color(0.7, 0.82, 0.88)

@export_group("Grading")
@export var tone_exposure: float = 1.0
@export var tone_white: float = 1.0
@export var ssao: float = 1.0
@export var saturation: float = 1.1
@export var contrast: float = 1.06

@export_group("Audio")
@export var ambience_bed: AudioStream
@export var music_bed: AudioStream

@export_group("Water / Medium")
@export var is_underwater: bool = false
@export var water_tint: Color = Color(0.06, 0.27, 0.34)
@export var fog_density_underwater_scale: float = 4.0

## Keys that participate in cross-biome interpolation.
const KEYS := [&"fog_density", &"vol_fog_density", &"exposure", &"ambient_energy",
	&"ssao", &"saturation", &"contrast", &"sky_top", &"sky_horizon", &"sun_color",
	&"ambient_color", &"fog_color", &"fog_light_color", &"water_tint"]


func apply(env: Environment, sun: DirectionalLight3D = null) -> void:
	if env == null:
		return
	if env.sky == null:
		env.sky = GradientSky.new()
	if env.sky is GradientSky:
		var gs := env.sky as GradientSky
		gs.top_color = sky_top_color
		gs.horizon_color = sky_horizon_color
	env.background_mode = Environment.BG_SKY
	env.ambient_light_source = Environment.AMBIENT_SOURCE_SKY
	env.ambient_light_color = ambient_color
	env.ambient_light_energy = ambient_energy
	env.fog_enabled = fog_enabled
	env.fog_light_color = fog_light_color
	env.fog_density = fog_density
	env.fog_height_density = height_fog_depth
	env.fog_height_min = height_fog_start
	env.volumetric_fog_enabled = volumetric_fog
	env.volumetric_fog_density = volumetric_fog_density
	env.volumetric_fog_albedo = volumetric_fog_albedo
	env.volumetric_fog_height_fog_intensity = 1.0
	env.tone_mapping_mode = Environment.TONE_MAPPING_ACES
	env.tone_mapping_exposure = tone_exposure
	env.tone_mapping_white = tone_white
	env.ssao_enabled = ssao > 0.0
	env.ssao_intensity = ssao
	env.adjustment_enabled = true
	env.adjustment_saturation = saturation
	env.adjustment_contrast = contrast
	if sun != null:
		sun.light_color = sun_color
		sun.light_energy = sun_energy
		sun.direction = sun_direction


func gather() -> Dictionary:
	return {
		&"fog_density": fog_density,
		&"vol_fog_density": volumetric_fog_density,
		&"exposure": tone_exposure,
		&"ambient_energy": ambient_energy,
		&"ssao": ssao,
		&"saturation": saturation,
		&"contrast": contrast,
		&"sky_top": sky_top_color,
		&"sky_horizon": sky_horizon_color,
		&"sun_color": sun_color,
		&"ambient_color": ambient_color,
		&"fog_color": fog_color,
		&"fog_light_color": fog_light_color,
		&"water_tint": water_tint,
	}


static func lerp_values(a: Dictionary, b: Dictionary, t: float) -> Dictionary:
	var out: Dictionary = {}
	for key in b:
		var from = a.get(key, b[key])
		if from is Color and b[key] is Color:
			out[key] = (from as Color).lerp(b[key] as Color, t)
		elif from is float and b[key] is float:
			out[key] = lerpf(from, b[key], t)
		else:
			out[key] = b[key]
	return out


func apply_gathered(env: Environment, sun: DirectionalLight3D, values: Dictionary) -> void:
	if env == null:
		return
	env.fog_density = values.get(&"fog_density", env.fog_density)
	env.volumetric_fog_density = values.get(&"vol_fog_density", env.volumetric_fog_density)
	env.tone_mapping_exposure = values.get(&"exposure", env.tone_mapping_exposure)
	env.ambient_light_energy = values.get(&"ambient_energy", env.ambient_light_energy)
	env.ssao_enabled = values.get(&"ssao", 1.0) > 0.0
	env.ssao_intensity = values.get(&"ssao", env.ssao_intensity)
	env.adjustment_saturation = values.get(&"saturation", env.adjustment_saturation)
	env.adjustment_contrast = values.get(&"contrast", env.adjustment_contrast)
	if env.sky is GradientSky:
		var gs := env.sky as GradientSky
		gs.top_color = values.get(&"sky_top", gs.top_color)
		gs.horizon_color = values.get(&"sky_horizon", gs.horizon_color)
	if sun != null:
		sun.light_color = values.get(&"sun_color", sun.light_color)
	env.ambient_light_color = values.get(&"ambient_color", env.ambient_light_color)
	env.fog_color = values.get(&"fog_color", env.fog_color)
	env.fog_light_color = values.get(&"fog_light_color", env.fog_light_color)
