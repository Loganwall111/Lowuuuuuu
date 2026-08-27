#version 120

uniform sampler2D texture;
uniform float frameTimeCounter;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;

    // Emissive boost for glowing MCSM command block, amulet, and storm elements
    float isHotMagenta = step(0.70, col.r) * step(0.70, col.b) * (1.0 - step(0.50, col.g));
    float isCyanGlow   = step(0.70, col.g) * step(0.70, col.b) * (1.0 - step(0.50, col.r));
    float emissive = max(isHotMagenta, isCyanGlow);

    if (emissive > 0.5) {
        float pulse = 0.85 + 0.15 * sin(frameTimeCounter * 3.0);
        col.rgb *= 1.8 * pulse;
    }

    gl_FragColor = col;
}
