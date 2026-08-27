#version 120

uniform sampler2D colortex0;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(colortex0, texcoord);
    gl_FragColor = col;
}
