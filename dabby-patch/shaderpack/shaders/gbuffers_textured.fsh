#version 330 compatibility

in vec2 texcoord;
in vec2 lmcoord;
in vec4 glcolor;

uniform sampler2D gtexture;
uniform sampler2D lightmap;
uniform float alphaTestRef;

void main() {
    vec4 color = texture(gtexture, texcoord) * glcolor;
    if (color.a < alphaTestRef) discard;
    color.rgb *= texture(lightmap, lmcoord).rgb;
    gl_FragData[0] = color;
}
