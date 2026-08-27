#version 120

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;

varying vec4 color;
varying vec2 texcoord;

// Story Mode cloud extrusion factor matching rendertype_clouds.vsh
const float CloudHeight = 2.5;

void main() {
    // Transform from eye space into world space
    vec3 eyePos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    vec3 worldPos = (gbufferModelViewInverse * vec4(eyePos, 1.0)).xyz;

    // Apply the 2.5 vertical extrusion exactly like rendertype_clouds.vsh
    worldPos.y *= CloudHeight;

    // Transform back to clip space
    gl_Position = gl_ProjectionMatrix * (gbufferModelView * vec4(worldPos, 1.0));

    // Flat MCSM story mode cloud coloring: uniform 1.0 brightness for all faces
    color = vec4(gl_Color.rgb * 1.08, gl_Color.a);
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
}
