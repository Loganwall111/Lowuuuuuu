#version 330

// A yellow bloom around the sun, and ONLY while the storm's gloom is over the sky.
//
// Not a sprite pinned to the sun and not a change to the sun itself. Every SKY pixel is turned
// back into a view ray and asked how near the sun it points; near ones get warmth added. That is
// why it settles into the fog instead of sitting on top of it -- it is being added to the same
// pixels the fog already coloured, across a wide falloff, so the two blend as one gradient rather
// than as a disc laid over a background.
//
// Tied to the gloom on purpose. Under a clear sky the vanilla sun is already right and does not
// want a halo; it is the violet murk a late-phase storm drags over everything that makes a sun
// look wrong without one, so the whole effect rides the same factor that darkens the sky and
// vanishes with it.

uniform sampler2D DepthSampler;

layout(std140) uniform SunGlowConfig {
    mat4 InvViewProj;  // scene clip -> camera-relative world, for the view ray
    vec4 SunDir;       // xyz: direction TO the sun. w: overall strength, already faded
    vec4 GlowColor;    // rgb: the warmth added. a: how tightly it hugs the sun
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    // Reversed-Z: anything other than 0 means solid geometry was drawn here. The glow is
    // atmospheric, so it belongs to the sky and must not wash over terrain in front of it.
    if (depth > 0.0) { fragColor = vec4(0.0, 0.0, 0.0, 1.0); return; }

    // The view ray for this pixel. Unprojected at the far plane, which for a sky pixel IS the
    // direction it is looking -- no camera position needed, because the world is drawn
    // camera-relative and the origin is already the eye.
    vec4 clip = vec4(texCoord * 2.0 - 1.0, 1.0, 1.0);
    vec4 unprojected = InvViewProj * clip;
    vec3 ray = normalize(unprojected.xyz / unprojected.w);

    float toward = max(0.0, dot(ray, SunDir.xyz));

    // TWO falloffs summed, not one. A single exponent gives either a hard little disc or a wash
    // over half the sky; a tight core for the sun itself plus a wide, weak halo is what real haze
    // around a light looks like, and the wide term is the part that actually knits into the fog.
    float core = pow(toward, GlowColor.a);
    // MCSM 1.9.96 -- "shrink the glare, it's way too big": the wide halo term is
    // what read as a giant orange wash. Exponent 4 -> 7 shrinks its reach
    // (pow(0.5,7)=0.008 vs pow(0.5,4)=0.0625 -- an ~8x smaller half-width) and
    // 0.35 -> 0.18 dims what remains.
    float halo = pow(toward, 7.0) * 0.18;

    // MCSM 1.9.96 -- "the one in phase 5.5 is really orange ... it should be dark
    // red pink purple magenta ... and a tinsy blue". The Java side feeds a warm
    // yellow GlowColor for the sun; re-hue here, luminance preserved, so the
    // strength config still works exactly as before -- only the chroma changes.
    vec3 mcsmWarm = GlowColor.rgb;
    float mcsmLum = max(dot(mcsmWarm, vec3(0.2126, 0.7152, 0.0722)), 0.0005);
    const vec3 MCSM_MAGENTA = vec3(0.52, 0.13, 0.44);
    const vec3 MCSM_BLUENOTE = vec3(0.60, 0.16, 0.78);
    vec3 mcsmHue = mix(MCSM_MAGENTA, MCSM_BLUENOTE, 0.22);  // mostly magenta, a tinsy blue
    vec3 mcsmGlowRGB = mcsmHue * (mcsmLum / dot(mcsmHue, vec3(0.2126, 0.7152, 0.0722)));

    fragColor = vec4(mcsmGlowRGB * ((core + halo) * SunDir.w), 1.0);
}
