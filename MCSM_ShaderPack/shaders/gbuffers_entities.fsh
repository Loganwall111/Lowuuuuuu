#version 120

uniform sampler2D gtexture;
uniform float frameTimeCounter;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord) * color;
    if (col.a < 0.1) {
        discard;
    }

    // Turquoise / cyan glow on top of teeth (#00E5FF)
    float isTurquoise = step(0.70, col.g) * step(0.80, col.b) * (1.0 - step(0.40, col.r));
    float isHotMagenta = step(0.68, col.r) * step(0.68, col.b) * (1.0 - step(0.50, col.g));
    float isCyanGlow   = step(0.68, col.g) * step(0.68, col.b) * (1.0 - step(0.50, col.r));
    float isAmuletGold = step(0.80, col.r) * step(0.70, col.g) * (1.0 - step(0.40, col.b));
    float emissive = max(max(max(isTurquoise, isHotMagenta), isCyanGlow), isAmuletGold);

    if (emissive > 0.5) {
        float pulse = 0.88 + 0.12 * sin(frameTimeCounter * 3.5);
        if (isTurquoise > 0.5) {
            col.rgb = mix(col.rgb, vec3(0.0, 0.90, 1.0), 0.75) * 2.10 * pulse;
        } else {
            col.rgb *= 1.85 * pulse;
        }
    }

    gl_FragColor = col;
}
