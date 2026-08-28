#version 150

// ============================================================================
// storm_atmosphere.fsh — Destroyer-stage cinematic fog (Phases 5.1 - 5.9)
// ============================================================================
// Full-screen pink/magenta atmospheric overlay, triggered chronologically by
// StormAtmosphere.process() exactly during the Destroyer stage:
//  - soft pink (#D81B60) -> magenta -> void-violet (#4A148C) horizon gradient
//  - custom coloured light-map tint (pink key light, magenta fill)
//  - environmental flares: two soft lens blooms hugging the horizon
//  - high-contrast dark shadow silhouette occlusion at ground level
// Never a physical shell: it re-grades the finished frame in post.

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform AtmosphereConfig {
    vec4 Params;        // x: horizon band height, y: top strength, z: bottom strength, w: occlusion
    vec4 TopColor;      // void-violet zenith
    vec4 HorizonColor;  // pink-magenta horizon
    vec4 BottomColor;   // deep rose ground haze
    vec4 Flare1Color;   // warm pink flare tint
    vec4 Flare1Pos;     // x,y: screen position, z: radius, w: intensity
    vec4 Flare2Color;   // magenta flare tint
    vec4 Flare2Pos;     // x,y: screen position, z: radius, w: intensity
};

uniform sampler2D In;

void main() {
    vec4 scene = texture(In, texCoord);

    // Vertical gradient: deep rose ground haze -> pink-magenta horizon ->
    // void-violet zenith.
    float h = clamp(texCoord.y, 0.0, 1.0);
    vec3 grad = mix(BottomColor.rgb, HorizonColor.rgb, smoothstep(0.0, Params.x, h));
    grad = mix(grad, TopColor.rgb, smoothstep(Params.x, 1.0, h));

    // Strength is stronger near the horizon, weaker overhead.
    float amt = mix(Params.z, Params.y, h);
    vec3 outCol = mix(scene.rgb, grad, amt * 0.55);

    // Custom coloured light-map tint: pink key light, magenta fill.
    vec3 lightTint = mix(vec3(1.04, 0.90, 1.02), vec3(1.06, 0.82, 0.98), h);
    outCol *= lightTint * 0.985;

    // Environmental flares: soft additive blooms drifting with the screen.
    float d1 = distance(texCoord, Flare1Pos.xy);
    float flare1 = Flare1Pos.w * smoothstep(Flare1Pos.z, 0.0, d1);
    float d2 = distance(texCoord, Flare2Pos.xy);
    float flare2 = Flare2Pos.w * smoothstep(Flare2Pos.z, 0.0, d2);
    outCol += Flare1Color.rgb * flare1 * 0.35;
    outCol += Flare2Color.rgb * flare2 * 0.28;

    // High-contrast dark shadow silhouette occlusion toward the ground.
    float luma = dot(outCol, vec3(0.299, 0.587, 0.114));
    outCol = mix(outCol, outCol * vec3(0.55, 0.42, 0.62), Params.w * (1.0 - smoothstep(0.05, 0.55, luma)));

    fragColor = vec4(outCol, scene.a);
}
