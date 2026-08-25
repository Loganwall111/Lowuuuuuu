#version 330

// Vertex stage of the storm sky pass: the Telltale-style layered backdrop
// (energy plate / anomaly plate / churning cloud bands / mutation flash bloom)
// drawn camera-locked at "infinite" sky depth inside vanilla's own sky frame
// pass. The CPU composes projection x live view rotation into SkyViewProj, so
// the geometry never translates with the world -- exactly like the sun/moon.

in vec3 InPosition;
in vec2 InTexCoords;
in float InColorR;
in float InColorG;
in float InColorB;
in float InColorA;

layout(std140) uniform SkyConfig {
    // world-axis sky space -> clip space. No translation component: the dome
    // rides the camera the way the celestial sphere does.
    mat4 SkyViewProj;
};

out vec2 vUv;
out vec4 vColor;

void main() {
    gl_Position = SkyViewProj * vec4(InPosition, 1.0);
    vUv = InTexCoords;
    vColor = vec4(InColorR, InColorG, InColorB, InColorA);
}
