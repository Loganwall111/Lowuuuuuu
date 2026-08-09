#version 120

/*
 * Rotting ground: subtle purple-bias grade + depth darkening,
 * layered under the standard OptiFine fog behaviour.
 */

uniform sampler2D texture;
uniform float rainStrength;

varying vec4 intColor;
varying vec2 texcoord;
varying vec3 viewPos;
varying float fogDepth;

void main() {
    vec4 col = texture2D(texture, texcoord) * intColor;

    // bruised grade
    col.rgb *= vec3(0.96, 0.86, 1.05);

    // depth darkening — geometry drowns earlier than vanilla likes
    float dist = length(viewPos);
    float shade = exp(-dist * 0.0022 * (1.0 + rainStrength));
    col.rgb *= mix(1.0, shade, 0.30);

    gl_FragColor = col;
}
