#version 120

#define DYNAMIC_SKY // Enable Story Mode Day/Noon/Sunset/Night transitions

precision highp float;
precision highp int;

uniform int worldTime;
uniform vec3 sunPosition;

varying vec4 color;
varying vec3 viewPos;
varying float vWorldTime;
varying float vSunY;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    // Explicitly reference worldTime uniform so sky does not freeze
    vWorldTime = float(worldTime);
    vSunY = normalize(sunPosition).y;
}
