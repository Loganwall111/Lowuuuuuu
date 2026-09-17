#version 330

// The storm's shadow, out of a real shadow map, doing two separate jobs.
//
// First pass (StormShadowMap) draws the storm's own geometry -- body, back mass, tentacles, heads
// -- from the sun's point of view into a depth map. This pass then runs over the finished frame:
// every pixel is unprojected back into the world through the scene's depth buffer, projected into
// the sun's clip space, and compared against what the map says was nearest the sun along that
// line. Further away means the storm is in between.
//
// That single test answers two different questions depending on WHERE the pixel is:
//   - on the ground  -> the storm is throwing a shadow across the landscape
//   - on the storm   -> the storm is shading its own body
// They are separate features with separate switches, and neither implies the other.

uniform sampler2D DepthSampler;    // the scene, reversed-Z
uniform sampler2D ShadowSampler;   // the sun's view of the storm, ordinary depth, near = 0
uniform sampler2D GroundSampler;   // the world seen from straight above: its surface height

layout(std140) uniform ShadowConfig {
    mat4 InvViewProj;   // scene clip -> camera-relative world
    mat4 LightViewProj; // camera-relative world -> sun clip
    // x: strength, faded near the horizon and to nothing at night.
    // y: depth bias, in sun-clip units.
    // z: one shadow-map texel, in UV.
    // w: 1 when the ground height map is available.
    vec4 Params;
    mat4 ViewProj;      // camera-relative world -> scene clip, for the sky-access march
    // xyz: direction TO the sun, world space. w: 1 when the storm may shadow ITSELF.
    vec4 SunDir;
    // rgb: what full shadow multiplies the frame by. a: 1 when the storm may shadow the WORLD.
    vec4 ShadowTint;
    mat4 GroundViewProj; // camera-relative world -> the overhead view of the ground
    // x: how hard the storm's own shading is pushed. 0 leaves it exactly as the world's.
    vec4 Contrast;
};

// Half the overhead view's depth range, in blocks. MUST match StormShadowMap.GROUND_SPAN --
// it is what turns a depth difference in that map back into a height in blocks.
const float GROUND_SPAN = 512.0;

// How far above the world's surface a pixel has to be before it counts as the STORM rather than
// as the world. Nothing else is solid this far up at this scale, and the only consequence of
// misjudging it -- the top of a very tall build -- is which switch that pixel obeys. Both produce
// the same shadow, so a misroute is invisible rather than wrong.
// A RAMP, not a line. The lid takes the LOWEST of a small neighbourhood so that isolated floating
// blocks cannot fake a surface -- which means that on a slope it sits well below the real ground,
// and ground pixels read as several blocks "aloft" through no fault of their own. At a hard
// threshold that misclassifies whole hillsides as storm, and since the storm gets a tighter bias
// and a contrast boost, the result is exactly the grid-aligned dark patches that were reported:
// not a shadow, a CLASSIFICATION changing from lid cell to lid cell.
//
// The storm floats far higher than any terrain step, so the two are easy to separate once the
// answer is allowed to be gradual: nothing below LOW is ever treated as storm, everything above
// HIGH fully is, and in between the storm's treatment fades in rather than switching. A misjudged
// pixel now shifts by a fraction of a shade instead of a whole one.
// RAISED, because this is what decides ground from storm and it was deciding wrong.
//
// "Aloft" is measured against the GROUND GRID, which is coarse -- so anywhere the real terrain
// stands well above the sampled surface (a cliff edge, a tower, a tall build) the pixel reads as
// being far above the world and was classed as part of the storm. It then got the storm's own
// self-shadow painted onto it: the model's shading, on the floor.
//
// The storm flies tens of blocks up, so the two are separable with plenty of room; the threshold
// only has to clear the tallest thing the grid can be wrong about.
const float ALOFT_LOW = 28.0;
const float ALOFT_HIGH = 46.0;

// How far toward the sun to look for something already blocking it, in blocks, and in how many
// steps. Only used when there is no ground map to ask instead.
const float SKY_MARCH_DISTANCE = 48.0;
const int SKY_MARCH_STEPS = 16;
const float SKY_MARCH_THICKNESS = 12.0;

in vec2 texCoord;

out vec4 fragColor;

/** The world's surface depth at a point in the overhead view. */
float surfaceDepth(vec2 groundXy) {
    return texture(GroundSampler, groundXy * 0.5 + 0.5).r;
}

/**
 * Whether this point can see the sun at all, ignoring the storm.
 *
 * Fallback for when the ground height map is unavailable. Screen space, so it only knows about
 * occluders that are actually on screen -- which is why the height map is preferred: a cave
 * ceiling behind the camera is invisible to this and it will happily shadow the floor below it.
 */
bool sunIsBlocked(vec3 world, vec3 sunDir, sampler2D sceneDepth, mat4 viewProj, mat4 invViewProj) {
    for (int i = 1; i <= SKY_MARCH_STEPS; i++) {
        float t = SKY_MARCH_DISTANCE * float(i) / float(SKY_MARCH_STEPS);
        vec3 samplePos = world + sunDir * t;

        vec4 clip = viewProj * vec4(samplePos, 1.0);
        if (clip.w <= 0.0) return false;
        vec3 ndc = clip.xyz / clip.w;
        vec2 uv = ndc.xy * 0.5 + 0.5;
        if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) return false;

        // Reversed-Z: LARGER is NEARER.
        float sceneD = texture(sceneDepth, uv).r;
        if (sceneD <= 0.0) continue;
        if (sceneD > ndc.z) {
            vec4 hit = invViewProj * vec4(ndc.xy, sceneD, 1.0);
            vec3 hitWorld = hit.xyz / hit.w;
            if (distance(hitWorld, samplePos) < SKY_MARCH_THICKNESS) return true;
        }
    }
    return false;
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;

    vec4 clip = vec4(texCoord * 2.0 - 1.0, depth, 1.0);
    vec4 unprojected = InvViewProj * clip;
    vec3 world = unprojected.xyz / unprojected.w;

    // Face normal out of the depth buffer: two neighbouring pixels' world positions span the
    // surface, so their cross product is perpendicular to it. Taken before any branch, because
    // derivatives are undefined if pixels in the same quad take different paths.
    vec3 normal = normalize(cross(dFdx(world), dFdy(world)));
    if (dot(normal, -world) < 0.0) normal = -normal;

    // Reversed-Z: 0.0 is the far plane, so nothing was drawn here -- sky.
    if (depth <= 0.0) { fragColor = vec4(1.0); return; }

    // CHEAPEST REJECTION FIRST, and this is the one that pays for itself.
    //
    // The storm's shadow map covers a box fitted to the storm; on a 4K screen the overwhelming
    // majority of pixels are nowhere near it. Testing that FIRST -- one matrix multiply and a
    // bounds check -- lets all of them skip everything below: the ground map's own projection and
    // texture read, the altitude maths, and the nine-tap filter. This used to run last, so every
    // pixel in the frame paid for the ground lookup before finding out it was never going to be
    // shadowed at all.
    //
    // Outside the map must be LIT rather than clamped to the edge texel, or the shadow smears off
    // in bands wherever the map runs out.
    vec4 lightClip = LightViewProj * vec4(world, 1.0);
    vec3 lightNdc = lightClip.xyz / lightClip.w;
    if (any(lessThan(lightNdc.xy, vec2(-1.0))) || any(greaterThan(lightNdc.xy, vec2(1.0)))
            || lightNdc.z > 1.0) {
        fragColor = vec4(1.0);
        return;
    }
    // FADED AT THE EDGE, NEVER CUT AT IT.
    //
    // Both of this pass's bounds -- the sun's own box here, and the ground grid below -- used to
    // end in a hard return, so wherever either one ran out the shadow stopped along a straight
    // line. Which of the two was doing it depended on the sun and the storm's height, which is why
    // it moved around and was so hard to pin down.
    //
    // Rather than keep chasing which bound is short, both now fade out over their last few percent.
    // A shadow that thins away at its limit reads as distance; one that ends on a straight edge
    // reads as a bug, and no amount of extra reach fixes the edge itself -- it only moves it.
    float boxFade = clamp((1.0 - max(abs(lightNdc.x), abs(lightNdc.y))) / 0.06, 0.0, 1.0);

    // A SURFACE FACING AWAY FROM THE SUN IS ALREADY IN SHADE. The underside of an overhang, a
    // cave roof, the storm's own dark side -- none of them catch sunlight in the first place, so
    // nothing can take any away.
    float facing = dot(normal, SunDir.xyz);
    if (facing <= 0.0) { fragColor = vec4(1.0); return; }

    // HOW HIGH IS THIS PIXEL ABOVE THE WORLD? Two things are decided from that one answer.
    //
    // Below the surface means inside the world -- a cave, a tunnel, a room -- where the sun never
    // reached, so the storm has nothing to take away. Well ABOVE it means the pixel is the storm
    // itself, which is a different feature with a different switch.
    // OUTSIDE THE GROUND GRID, NOTHING IS SHADOWED AT ALL.
    //
    // The grid is finite -- it is built around the storm and stops. Pixels beyond it used to fall
    // through with knowAltitude false, which meant they skipped the cave gate AND were classed as
    // world rather than storm, so the shadow simply behaved differently past an invisible line.
    // That edge is the "odd slightly clipped ground shadow": not a shadow being drawn, a shadow
    // CHANGING at the grid boundary. Anything the grid does not cover is left alone.
    float aloft = 0.0;
    bool knowAltitude = false;
    bool insideGrid = false;
    float gridFade = 1.0;
    if (Params.w > 0.5) {
        vec4 groundClip = GroundViewProj * vec4(world, 1.0);
        vec3 groundNdc = groundClip.xyz / groundClip.w;
        gridFade = clamp((1.0 - max(abs(groundNdc.x), abs(groundNdc.y))) / 0.10, 0.0, 1.0);
        if (all(greaterThanEqual(groundNdc.xy, vec2(-1.0)))
                && all(lessThanEqual(groundNdc.xy, vec2(1.0)))) {
            float surface = surfaceDepth(groundNdc.xy);
            knowAltitude = true;
            insideGrid = true;
            // The overhead view looks DOWN, so depth grows downward: a pixel above the surface
            // reads a smaller z than the surface did. Scaled back into blocks by the view's span.
            aloft = (surface - groundNdc.z) * GROUND_SPAN * 2.0;
            // Six blocks of slack: the lid is a coarse grid, and being under it by half a block
            // is ambiguous where being under it by six is not.
            if (groundNdc.z > surface + 0.0060) { fragColor = vec4(1.0); return; }
        }
    } else if (sunIsBlocked(world, SunDir.xyz, DepthSampler, ViewProj, InvViewProj)) {
        fragColor = vec4(1.0);
        return;
    }

    // TWO INDEPENDENT FEATURES, chosen per pixel. Someone may well want the storm's own folds
    // shaded without a shadow thrown across the landscape, or the other way round, so neither
    // switch is allowed to imply the other.
    // Past the grid there is no way to tell ground from storm, and guessing produces exactly the
    // hard edge this is here to remove. Nothing is drawn out there.
    if (Params.w > 0.5 && !insideGrid) { fragColor = vec4(1.0); return; }

    float stormness = knowAltitude
            ? smoothstep(ALOFT_LOW, ALOFT_HIGH, aloft) : 0.0;
    bool onStorm = stormness > 0.5;
    if ((onStorm ? SunDir.w : ShadowTint.a) < 0.5) { fragColor = vec4(1.0); return; }

    vec2 mapUv = lightNdc.xy * 0.5 + 0.5;
    float texel = Params.z;

    // THE STORM NEEDS A MUCH TIGHTER BIAS THAN THE GROUND DOES.
    //
    // Bias is what stops a surface shadowing itself, and this one is sized for the coarse ground
    // lid. On a body whose folds and tentacles are a couple of blocks apart, that much slack
    // erases every shadow the storm casts on itself before it can be seen -- which is the
    // difference between self-shadowing working and appearing to do nothing at all.
    // Blended by the same ramp, so the bias never jumps between neighbouring pixels either.
    float bias = Params.y * (1.0 + 2.0 * (1.0 - facing)) * mix(1.0, 0.22, stormness);

    // THE PENUMBRA GROWS WITH DISTANCE FROM THE CASTER. A fixed-width edge is the giveaway that
    // something is painted on rather than cast: a real shadow is sharp where it meets the thing
    // casting it and spreads as it travels. The distance is free -- the map already stores how far
    // the storm was from the sun along this ray, and the receiver knows how far IT is.
    float blocker = texture(ShadowSampler, mapUv).r;
    float gap = max(0.0, lightNdc.z - blocker);
    float radius = clamp(1.0 + gap * 900.0, 1.0, 5.0);

    // Rotated per pixel so the taps land differently on neighbouring pixels; a fixed grid at this
    // width shows its own pattern as banding.
    float angle = fract(sin(dot(texCoord, vec2(12.9898, 78.233))) * 43758.5453) * 6.2831853;
    float ca = cos(angle), sa = sin(angle);

    // NINE TAPS OR ONE, and at a high resolution that is the whole cost of this pass.
    //
    // This runs for every pixel on the screen. At 4K that is eight million of them, so nine taps
    // is seventy-five million texture reads a frame -- and NONE of it has anything to do with how
    // much geometry the storm has. Optimising the model cannot touch this; only sampling less can.
    //
    // The taps are what soften the shadow's edge, so dropping to one is a real visual change and
    // not a free win: the edge goes hard and stair-steps along the map's texels. It is offered
    // because on a big screen it is the difference between a shadow you can afford and one you
    // cannot, and that is the player's call rather than ours.
    float shadow = 0.0;
    if (Contrast.y < 0.5) {
        float occluder = texture(ShadowSampler, mapUv).r;
        shadow = (lightNdc.z - bias) > occluder ? 1.0 : 0.0;
    } else {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                vec2 o = vec2(float(x), float(y)) * radius;
                o = vec2(o.x * ca - o.y * sa, o.x * sa + o.y * ca);
                float occluder = texture(ShadowSampler, mapUv + o * texel).r;
                shadow += (lightNdc.z - bias) > occluder ? 1.0 : 0.0;
            }
        }
        shadow /= 9.0;
    }

    // NOT A FLAT WASH. A shadow removes the SUN's share of the light, so how much a surface loses
    // depends on how much it was catching. That gradient is what sets it IN the scene rather than
    // on top of it; the floor stops grazing surfaces being left untouched altogether.
    float lost = 0.25 + 0.75 * facing;
    float amount = shadow * Params.x * lost * boxFade * gridFade;

    // THE STORM IS PUSHED HARDER THAN THE WORLD IS.
    //
    // A shadow pass across terrain has to stay gentle -- it is sitting on top of Minecraft's own
    // lighting and fighting it looks wrong. The storm is a single object filling the sky, and the
    // same gentle treatment leaves it reading flat: its folds and tentacles barely separate.
    //
    // So on the storm, both ends are pulled apart. The shadowed side goes deeper, and -- because
    // this pass is a MULTIPLY -- the lit side can be pushed past 1.0, which brightens rather than
    // merely failing to darken. That is the half that cannot be had by turning the shadow up.
    float gain = 1.0;
    if (stormness > 0.0 && Contrast.x > 0.0) {
        float push = Contrast.x * stormness;
        amount = min(1.0, amount * (1.0 + push));
        // Only where it is BOTH facing the sun and not in shadow -- brightening a shadowed face
        // would flatten the very separation this is meant to create.
        gain = 1.0 + push * facing * (1.0 - shadow);
    }

    // Tinted, not grey: what remains in a real shadow is skylight, cooler and bluer than the
    // sunlight it lost. A neutral multiply is exactly what "just a flat colour" looks like.
    vec3 lit = mix(vec3(1.0), ShadowTint.rgb, amount) * gain;
    fragColor = vec4(lit, 1.0);
}
