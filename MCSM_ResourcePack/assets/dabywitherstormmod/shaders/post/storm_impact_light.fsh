#version 330

// A REAL point light at the end of each tractor beam, shaded per pixel.
//
// This exists because Minecraft's own lighting cannot do what is wanted here. Block light is one
// integer per cubic metre, so a light source can only ever sit ON the grid: the pool is built out
// of block-sized steps and it teleports a whole block sideways the moment the beam crosses a
// boundary. Spreading the source over several blocks and weighting them helps the jumping, but the
// steps are still there, because the steps ARE the data structure. There is no arrangement of
// light blocks that produces a smooth pool.
//
// So this does not use them. Every pixel of the frame is unprojected back into the world through
// the depth buffer, its surface normal is recovered from the depth gradient, and the light is
// evaluated against that -- a genuine diffuse point light with a continuous falloff, positioned at
// the beam's exact fractional impact point. It moves by fractions of a pixel because it is
// computed per pixel, not looked up from a grid, and it wraps over slopes, stairs and walls
// because it is shading the real geometry rather than filling in cells around it.

uniform sampler2D DepthSampler;

layout(std140) uniform LightConfig {
    // inverse(projection * viewRotation). Takes a clip-space point back to CAMERA-RELATIVE world
    // space, which is the space the world is drawn in -- and, conveniently, the one where a float
    // still has plenty of precision however far from the origin the player has walked.
    mat4 InvViewProj;
    vec4 Params;        // x: how many of the arrays below are in use
    vec4 LightPos[8];   // xyz: camera-relative position, w: outer radius in blocks
    vec4 LightColor[8]; // rgb: colour, a: brightness
    vec4 LightShape[8]; // x: core distance in blocks -- where the light is half strength
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float depth = texture(DepthSampler, texCoord).r;

    // Reversed-Z: 0.0 is the far plane, which here means nothing was drawn -- sky. The early
    // return for it happens AFTER the derivatives below, not here; taking it now would leave dFdx
    // undefined for the terrain pixels sharing the same quad.

    // Clip -> camera-relative world. texCoord is 0..1 and clip xy is -1..1, but clip Z is already
    // 0..1 because the game renders with a zero-to-one depth range, so only xy is remapped.
    vec4 clip = vec4(texCoord * 2.0 - 1.0, depth, 1.0);
    vec4 unprojected = InvViewProj * clip;
    vec3 world = unprojected.xyz / unprojected.w;

    // The surface normal, straight out of the depth buffer. Two neighbouring pixels' world
    // positions span the surface, so their cross product is perpendicular to it. Block faces are
    // flat, so this recovers the exact face normal -- no G-buffer, no vertex data, nothing to keep
    // in sync with how the world happens to be drawn.
    vec3 normal = normalize(cross(dFdx(world), dFdy(world)));
    // Which way round the cross product comes out depends on the winding, which flips with the
    // face. The camera sits at the origin in this space, so -world points at the eye, and a
    // visible face must face it.
    if (dot(normal, -world) < 0.0) normal = -normal;

    if (depth <= 0.0) { fragColor = vec4(0.0, 0.0, 0.0, 1.0); return; }

    vec3 accumulated = vec3(0.0);
    int count = int(Params.x);

    for (int i = 0; i < count; i++) {
        vec3 toLight = LightPos[i].xyz - world;
        float radius = LightPos[i].w;
        float distance = length(toLight);
        if (distance >= radius) continue;

        // Falloff: an inverse square for the shape, windowed so it reaches exactly zero at the
        // radius. Both halves matter. A true inverse square never reaches zero, so cutting it off
        // at the radius leaves a visible ring where the light stops; a plain window with no
        // inverse-square term gives a flat disc with no bright core, which reads as painted-on fog
        // rather than as something illuminating the ground. Together: a hot centre, a long soft
        // tail, and zero value AND zero slope at the rim, so there is no edge anywhere.
        //
        // The inverse square is measured in BLOCKS against the core distance, NOT as a fraction of
        // the outer radius. That distinction is the whole reason "make it bigger" and "make it
        // brighter" are separate controls here: scaled by the radius, widening the light also
        // raises its value everywhere inside the old one, so asking for a bigger pool silently
        // gets you a brighter one too. Anchored in blocks, the core keeps the brightness it had
        // and a larger radius only extends the tail -- which is what growing a light should mean.
        float f = distance / radius;
        float window = (1.0 - f) * (1.0 - f);
        float core = distance / max(LightShape[i].x, 0.001);
        float falloff = window / (1.0 + core * core);

        // Diffuse term. Without it this is a fog ball that brightens walls and floors equally and
        // reads as an overlay; with it, a floor directly under the impact takes the full light and
        // a wall beside it catches a glancing amount, which is what makes it look like a light in
        // the world rather than a sprite drawn over it.
        float lambert = max(dot(normal, toLight / max(distance, 1.0e-4)), 0.0);
        // Not quite to zero: surfaces edge-on to the light would otherwise go abruptly dark
        // against their neighbours, and real light bounces. Kept small, because there is no shadow
        // pass here -- this term is also what leaks through a wall into a room behind it, and at
        // this size that is a faint wash rather than a light in the wrong place.
        lambert = 0.12 + 0.88 * lambert;

        accumulated += LightColor[i].rgb * (LightColor[i].a * falloff * lambert);
    }

    fragColor = vec4(accumulated, 1.0);
}
