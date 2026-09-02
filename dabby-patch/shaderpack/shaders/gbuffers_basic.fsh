#version 330 compatibility

in vec2 texcoord;
in vec4 glcolor;

uniform sampler2D gtexture;
uniform sampler2D lightmap;
uniform float alphaTestRef;

void main() {
    vec4 color = texture(gtexture, texcoord) * glcolor;
    if (color.a < alphaTestRef) discard;
    gl_FragData[0] = color;
}
