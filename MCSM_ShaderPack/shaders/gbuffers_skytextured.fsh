#version 120

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;
    // Warm golden Story Mode sun & celestial bloom
    col.rgb *= vec3(1.10, 1.02, 0.94);
    gl_FragColor = col;
}
