// organic_skin.gdshader — cheap "wet creature" material response. Adds a
// view-dependent sheen (wetness), a faint same-hue subsurface glow in shadow,
// and a rim term so silhouettes pop against fog. Drive it from instance/pipeline
// colour for variety across one MultiMesh.
shader_type spatial;

uniform vec4 albedo : source_color = vec4(0.45, 0.68, 0.6, 1.0);
uniform float roughness : hint_range(0.0, 1.0) = 0.55;
uniform float wetness : hint_range(0.0, 1.0) = 0.4;
uniform float subsurface : hint_range(0.0, 1.0) = 0.3;
uniform vec3 rim_color : source_color = vec3(0.5, 0.9, 0.8);

void fragment() {
	ALBEDO = albedo.rgb;
	ROUGHNESS = roughness;
	METALLIC = 0.0;
	SPECULAR = 0.5 + wetness * 0.5;
	// Fake SSS: emissive same-hue glow so tissue never goes pure black.
	EMISSION = albedo.rgb * subsurface * 0.25;
	// Rim light for that cinematic creature-pop.
	float rim = pow(1.0 - clamp(dot(normalize(VIEW_MATRIX[3].xyz - WORLD_MATRIX[3].xyz), NORMAL), 0.0, 1.0), 3.0);
	EMISSION += rim_color * rim * 0.25 * wetness;
}
