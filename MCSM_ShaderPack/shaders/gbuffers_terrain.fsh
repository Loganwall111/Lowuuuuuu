#version 120

uniform sampler2D gtexture;
varying vec4 color;
varying vec2 texcoord;

void main() {
    vec4 tex = texture2D(gtexture, texcoord) * color;
    if (tex.a < 0.1) {
        discard;
    }
    gl_FragColor = tex;
}
