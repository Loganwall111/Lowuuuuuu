#version 330

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>
#moj_import <minecraft:globals.glsl>

// ============================================================================
//  Devouring Storms - rendertype_entity_cutout.vsh   [BUILD #416]
//
//  Blueprint component 1 (entity cutout / emissive), ported to 26.2.
//
//  Two things in the blueprint could not be pasted verbatim, and both are
//  stated here instead of being hidden:
//
//   * 26.2 renamed the program. The entity cutout + emissive pass is
//     `entity.vsh` / `entity.fsh`, and cutout/translucent/emissive are DEFINE
//     variants of that one pair (PER_FACE_LIGHTING, EMISSIVE, ALPHA_CUTOUT,
//     NO_OVERLAY, DISSOLVE). The LIVE path for the mod is therefore
//     mcsm-core-shaders/core/entity.* -- which already carries the blueprint's
//     luminance gate. This file ships beside it as the blueprint-named module
//     so anything that asks for the classic name still resolves.
//   * `#version 150` plus bare `ModelViewMat` / `ProjMat` declarations are the
//     pre-1.21 interface. Re-declaring those against a 26.2 core pipeline is
//     exactly what produced the uniform-redefinition compile break (#407), so
//     this module uses the 26.2 includes. `lmCoords = UV2 / 256.0` is likewise
//     replaced by the 26.2 lightmap sampler + helper: the same quantity, but it
//     cannot go wrong if the lightmap atlas is ever resized.
// ============================================================================

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;   // overlay atlas
uniform sampler2D Sampler2;   // lightmap atlas

out float sphericalVertexDistance;
out float cylindricalVertexDistance;

#ifdef PER_FACE_LIGHTING
out vec4 vertexPerFaceColorBack;
out vec4 vertexPerFaceColorFront;
#else
out vec4 vertexColor;
#endif

#ifndef EMISSIVE
out vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
out vec4 overlayColor;
#endif

out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);

#ifdef PER_FACE_LIGHTING
    vec2 light = minecraft_compute_light(Light0_Direction, Light1_Direction, Normal);
    vertexPerFaceColorBack  = minecraft_mix_light_separate(-light, Color);
    vertexPerFaceColorFront = minecraft_mix_light_separate(light, Color);
#elif defined(NO_CARDINAL_LIGHTING)
    vertexColor = Color;
#else
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
#endif

#ifndef EMISSIVE
    lightMapColor = sample_lightmap(Sampler2, UV2);
#endif

#ifndef NO_OVERLAY
    overlayColor = texelFetch(Sampler1, UV1, 0);
#endif

    texCoord0 = UV0;

#ifdef APPLY_TEXTURE_MATRIX
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
#endif
}
