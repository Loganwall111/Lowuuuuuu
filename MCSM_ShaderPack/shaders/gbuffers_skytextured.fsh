#version 120

uniform sampler2D gtexture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord) * color;
    gl_FragColor = col;
}
