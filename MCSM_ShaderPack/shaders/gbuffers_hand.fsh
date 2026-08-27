#version 120

precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform sampler2D lightmap;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;

void main() {
    vec4 col = texture2D(gtexture, texcoord);
    if (col.a < 0.1) {
        discard;
    }
    vec4 lm = texture2D(lightmap, lmcoord);
    vec3 light = max(lm.rgb, vec3(0.55));
    col.rgb *= color.rgb * light;
    gl_FragColor = col;
}
