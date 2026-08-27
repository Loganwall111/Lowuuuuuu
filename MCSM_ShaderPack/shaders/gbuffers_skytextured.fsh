#version 120

precision highp float;
precision highp int;

uniform sampler2D gtexture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord) * color;
    gl_FragColor = col;
}
