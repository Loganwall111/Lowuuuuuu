#version 120

precision highp float;
precision highp int;

uniform long worldTime;
uniform float sunAngle;
uniform vec3 sunPosition;

varying vec4 color;
varying vec2 texcoord;
varying float vLiveTime;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;

    float liveTime = float(worldTime);
    if (liveTime < 0.5) {
        liveTime = mod(sunAngle * 24000.0, 24000.0);
        if (liveTime < 0.5 && length(sunPosition) > 0.01) {
            float sY = normalize(sunPosition).y;
            float sX = normalize(sunPosition).x;
            float a = atan(sY, sX);
            liveTime = mod((0.5 - a / 6.2831853) * 24000.0, 24000.0);
        }
    }
    vLiveTime = liveTime;
}
