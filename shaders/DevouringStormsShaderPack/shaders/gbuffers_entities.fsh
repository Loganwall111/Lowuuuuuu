#version 120

/*
 * Entities under the storm: grade toward bruised tones, and let the storm's
 * own pixels BURN — any bright-magenta texel (MASSG's veins and eyes, Decay veins)
 * gets an emissive push, so the corrupted parts of an entity carry light.
 */

uniform sampler2D texture;
uniform float frameTimeCounter;

varying vec4 intColor;
varying vec2 texcoord;
varying vec3 viewPos;

void main() {
    vec4 col = texture2D(texture, texcoord) * intColor;

    col.rgb *= vec3(0.95, 0.85, 1.04);

    // emissive push for storm-lit texels (hot magenta family)
    float hot = step(0.72, col.r) * step(0.72, col.b) * (1.0 - step(0.55, col.g));
    float flicker = 0.75 + 0.25 * sin(frameTimeCounter * 2.6);
    col.rgb = mix(col.rgb, col.rgb * 2.1 * flicker, hot);

    // light depth darkening shared with terrain feel
    float dist = length(viewPos);
    col.rgb *= exp(-dist * 0.0008);

    gl_FragColor = col;
}
