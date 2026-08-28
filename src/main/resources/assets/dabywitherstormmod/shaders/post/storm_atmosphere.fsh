#version 150

// ============================================================================
// storm_atmosphere.fsh — full-screen cinematic purple->magenta fog gradient
// ============================================================================
// A true post-processing pass (not a physical material block or sphere around
// the storm): it gently re-grades the finished frame with a smooth
// purple-to-dark-magenta atmospheric fog that hugs the horizon behind the
// Wither Storm, matching the Minecraft: Story Mode cataclysm look.

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform AtmosphereConfig {
    vec4 Params;        // x: horizon band height, y: top strength, z: bottom strength, w: unused
    vec4 TopColor;      // zenith purple
    vec4 HorizonColor;  // magenta horizon
    vec4 BottomColor;   // dark purple ground haze
};

uniform sampler2D In;

void main() {
    vec4 scene = texture(In, texCoord);

    // Vertical gradient: dark purple ground haze -> magenta horizon ->
    // purple zenith (the "lavender at the top, orange at the bottom" MCSM
    // sky is handled by the sky programs; this pass adds the storm gloom).
    float h = clamp(texCoord.y, 0.0, 1.0);
    vec3 grad = mix(BottomColor.rgb, HorizonColor.rgb, smoothstep(0.0, Params.x, h));
    grad = mix(grad, TopColor.rgb, smoothstep(Params.x, 1.0, h));

    // Strength is stronger near the horizon, weaker overhead.
    float amt = mix(Params.z, Params.y, h);
    vec3 outCol = mix(scene.rgb, grad, amt * 0.55);

    fragColor = vec4(outCol, scene.a);
}
