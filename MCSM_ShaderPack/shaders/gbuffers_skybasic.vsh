#version 120

#define DYNAMIC_SKY // Enable Story Mode Day/Noon/Sunset/Night transitions

precision highp float;
precision highp int;

uniform mat4 gbufferModelViewInverse;
uniform long worldTime;
uniform float sunAngle;
uniform vec3 sunPosition;

varying vec4 color;
varying vec3 worldDir;
varying float vLiveTime;
varying float vSunY;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    vec3 eyePos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    // Unproject to camera-relative world space direction
    worldDir = normalize((gbufferModelViewInverse * vec4(eyePos, 0.0)).xyz);
    
    // Actively compute live game time; prevent Sodium locking at tick 0
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
    vSunY = normalize(sunPosition).y;
}
