#version 150

// Story Look terrain lighting: shadows stay soft and cool-lavender instead
// of crushing to black, colors get a gentle saturation lift, and distance
// melts into a pastel haze matched to the horizon.
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 lightCoord;

out vec4 fragColor;

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.5) discard;

    vec2 lm = clamp(lightCoord, 0.0, 1.0);
    // soft ambient floor: story-mode shadows never go fully dark
    vec2 soft = mix(vec2(0.32, 0.36), vec2(1.0), smoothstep(vec2(0.0), vec2(1.0), lm));
    vec4 light = texture(Sampler2, soft);

    vec3 col = tex.rgb * vertexColor.rgb * light.rgb;

    // cool lavender tint in skylight shadow, warm-neutral in full light
    col = mix(col * vec3(0.86, 0.84, 1.03), col, smoothstep(0.15, 0.60, lm.y));

    // gentle saturation lift (the bright story-mode palette)
    float luma = dot(col, vec3(0.3333));
    col = mix(vec3(luma), col, 1.12);

    // pastel distance haze
    float depth = gl_FragCoord.z / gl_FragCoord.w;
    float fog = clamp((depth - FogStart) / max(FogEnd - FogStart, 0.001), 0.0, 1.0);
    vec3 pastel = FogColor.rgb * 1.15 + vec3(0.040, 0.030, 0.050);
    col = mix(col, pastel, fog * 0.9);

    fragColor = vec4(col, tex.a * vertexColor.a);
}
