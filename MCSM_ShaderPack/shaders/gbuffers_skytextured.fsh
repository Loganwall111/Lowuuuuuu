#version 120

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;
    // Story Mode warm golden celestial bloom
    col.rgb *= vec3(1.08, 0.98, 0.92);
    gl_FragColor = col;
}
