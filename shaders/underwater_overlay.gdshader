// underwater_overlay.gdshader — full-screen post-ish tint + drifting caustics
// for the "you are underwater" feeling. Applied to a ColorRect drawn above the
// 3D world (CanvasLayer) so we never pay for a second Environment during a dive.
shader_type canvas_item;

uniform vec4 tint : source_color = vec4(0.03, 0.22, 0.3, 0.45);
uniform float caustic_speed = 0.6;
uniform float caustic_scale = 9.0;
uniform float caustic_strength = 0.12;

float caustic(vec2 uv, float t) {
	float v = 0.0;
	v += sin(uv.x * caustic_scale + t);
	v += sin(uv.y * caustic_scale * 1.3 - t * 0.8);
	v += sin((uv.x + uv.y) * caustic_scale * 0.7 + t * 1.2);
	return v / 3.0;
}

void fragment() {
	vec2 uv = SCREEN_UV;
	float c = caustic(uv, TIME * caustic_speed) * 0.5 + 0.5;
	c = pow(c, 3.0) * caustic_strength;
	vec4 col = tint;
	col.rgb += vec3(c);
	// Vignette to focus the eye toward the centre.
	float d = distance(uv, vec2(0.5));
	col.a *= smoothstep(0.85, 0.2, d);
	COLOR = col;
}
