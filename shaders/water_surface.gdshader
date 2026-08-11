// water_surface.gdshader — stylised, lightweight water for reefs/wetlands.
// Gentle vertex displacement (no tessellation) + fresnel-tinted fragment so the
// surface reads as a living skin without expensive SSR/planar reflections.
shader_type spatial;
render_mode cull_disabled, blend_mix, depth_draw_opaque;

uniform float time_scale = 0.4;
uniform float wave_height = 0.25;
uniform float wave_freq = 0.35;
uniform vec4 deep_color : source_color = vec4(0.03, 0.18, 0.24, 1.0);
uniform vec4 shallow_color : source_color = vec4(0.12, 0.45, 0.5, 0.85);
uniform float fresnel_power = 2.5;

void vertex() {
	float t = TIME * time_scale;
	vec3 p = VERTEX;
	p.y += sin(p.x * wave_freq + t) * wave_height * 0.5;
	p.y += cos(p.z * wave_freq * 1.3 + t * 0.8) * wave_height * 0.5;
	VERTEX = p;
	NORMAL = normalize(vec3(
		-cos(p.x * wave_freq + t) * wave_freq * wave_height * 0.5,
		1.0,
		sin(p.z * wave_freq * 1.3 + t * 0.8) * wave_freq * wave_height * 0.5 * 1.3));
}

void fragment() {
	vec3 view_dir = normalize(VIEW_MATRIX[3].xyz - WORLD_MATRIX[3].xyz);
	float fres = pow(1.0 - clamp(dot(view_dir, NORMAL), 0.0, 1.0), fresnel_power);
	ALBEDO = mix(deep_color.rgb, shallow_color.rgb, fres);
	ALPHA = mix(0.55, 0.9, fres);
	ROUGHNESS = 0.15;
	METALLIC = 0.0;
	SPECULAR = 0.9;
}
