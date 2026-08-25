#version 330

// Vertex stage of the storm sky pass: the Telltale-style layered backdrop
// (energy plate / anomaly plate / churning cloud bands / mutation flash bloom)
// drawn camera-locked at "infinite" sky depth inside vanilla's own sky frame
// pass. SkyView carries only the live view rotation (no translation -- the dome
// rides the camera the way the celestial sphere does); ProjMatrix is the frame's
// real projection, bound straight from vanilla's uploaded uniform slice when
// available (or the mod's fallback upload).

in vec3 InPosition;
in vec2 InTexCoords;
in float InColorR;
in float InColorG;
in float InColorB;
in float InColorA;

layout(std140) uniform SkyConfig {
    mat4 SkyView;
};

layout(std140) uniform Projection {
    mat4 ProjMatrix;
};

out vec2 vUv;
out vec4 vColor;

void main() {
    gl_Position = ProjMatrix * (SkyView * vec4(InPosition, 1.0));
    vUv = InTexCoords;
    vColor = vec4(InColorR, InColorG, InColorB, InColorA);
}
