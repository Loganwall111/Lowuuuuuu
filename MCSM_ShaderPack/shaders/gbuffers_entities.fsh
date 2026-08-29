#version 120

// ============================================================================
// MCSM gbuffers_entities.fsh — Story Mode entity lighting + the Wither Storm's
// luminescent turquoise teeth aura (emissive, pulsing) + magenta accents,
// plus the SHINY OG body pass: the near-pure-black obsidian sheets catch a
// soft specular metallic sheen, a purple fresnel rim and a slow reflective
// glint band so the black voxel body reads as glossy MCSM armour instead of
// a flat black void.
// ============================================================================

#define EMISSIVE_TEETH_GLOW // Bright cyan (#00E5FF) bloom on Wither Storm teeth

precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform float frameTimeCounter;
uniform vec3 sunPosition;

varying vec4 color;
varying vec2 texcoord;
varying vec3 normal;
varying vec3 viewPos;

void main() {
    vec4 col = texture2D(gtexture, texcoord);
    col *= color;
    if (col.a < 0.1) {
        discard;
    }

    float isTurquoise = step(0.65, col.g) * step(0.75, col.b) * (1.0 - step(0.40, col.r));
    float isMagenta   = step(0.60, col.r) * step(0.60, col.b) * (1.0 - step(0.50, col.g));

    if (isTurquoise > 0.5) {
        // Turquoise aura on the teeth: emissive core + breathing glow
        float pulse = 0.90 + 0.10 * sin(frameTimeCounter * 4.0);
        float halo = 0.30 + 0.20 * sin(frameTimeCounter * 2.3);
        col.rgb = vec3(0.0, 0.92, 1.0) * (3.5 * pulse + halo);
    } else if (isMagenta > 0.5) {
        float pulse = 0.92 + 0.08 * sin(frameTimeCounter * 3.0);
        col.rgb = vec3(0.85, 0.12, 0.95) * 3.0 * pulse;
    }

    // ---- SHINY OG BODY: pure-black voxel sheets get metallic reflections ----
    float luma = dot(col.rgb, vec3(0.299, 0.587, 0.114));
    float isGlossyBlack = (1.0 - smoothstep(0.02, 0.10, luma)) * step(0.15, col.a);
    if (isGlossyBlack > 0.01) {
        vec3 n = normalize(normal);
        vec3 v = normalize(-viewPos);
        // Key light follows the sun when present, stylized otherwise.
        vec3 key = length(sunPosition) > 0.01 ? normalize(sunPosition)
                                              : normalize(vec3(0.35, 0.85, 0.30));
        vec3 hv = normalize(v + key);
        float ndotl = clamp(dot(n, key), 0.0, 1.0);
        float spec = pow(clamp(dot(n, hv), 0.0, 1.0), 32.0);

        // Slow reflective glint band sweeping across the body (fake env map).
        float band = pow(0.5 + 0.5 * sin(dot(n, vec3(1.0, 0.35, 0.6)) * 9.0
                        + frameTimeCounter * 0.6), 6.0);

        // Base lift: near-black -> deep glossy obsidian purple.
        col.rgb = max(col.rgb, vec3(0.030, 0.020, 0.065));

        vec3 sheenCol = vec3(0.72, 0.64, 1.00); // lavender-silver specular
        col.rgb += sheenCol * spec * (0.35 + 0.30 * ndotl);
        col.rgb += sheenCol * band * 0.16;

        // Purple fresnel rim so the silhouette reads as polished armour.
        float fres = pow(1.0 - clamp(dot(n, v), 0.0, 1.0), 3.0);
        col.rgb += vec3(0.55, 0.34, 0.95) * fres * 0.28;
    }

    gl_FragColor = col;
}
