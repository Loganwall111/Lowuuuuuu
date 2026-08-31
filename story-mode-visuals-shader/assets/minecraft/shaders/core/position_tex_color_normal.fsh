#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec2 texCoord0;
in float vertexDistance;
in vec4 vertexColor;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }

    // MCSM Story Mode clouds: round each cloud cell into a soft puff and
    // dissolve its lower part into the atmosphere.
    vec2 c = texCoord0 * 2.0 - 1.0;
    float radial = max(abs(c.x), abs(c.y));
    float puff = smoothstep(1.0, 0.45, radial);
    float bottomFade = mix(0.75, 1.0, smoothstep(0.0, 0.85, texCoord0.y));
    float alpha = color.a * puff * bottomFade;

    // melt into the distance a little harder than terrain does
    float distFade = smoothstep(FogEnd * 0.75, FogEnd, vertexDistance);
    alpha *= 1.0 - 0.4 * distFade;

    fragColor = linear_fog(vec4(color.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
