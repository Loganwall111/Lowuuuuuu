#version 120

precision highp float;
precision highp int;

uniform sampler2D colortex0;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(colortex0, texcoord);
    gl_FragColor = col;
}
