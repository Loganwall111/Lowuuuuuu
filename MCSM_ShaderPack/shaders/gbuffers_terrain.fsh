#version 120

uniform sampler2D texture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(texture, texcoord) * color;
    // Subtle Story Mode ambient grade: rich greens, harmonious lighting
    col.rgb *= vec3(1.01, 0.99, 1.03);
    gl_FragColor = col;
}
