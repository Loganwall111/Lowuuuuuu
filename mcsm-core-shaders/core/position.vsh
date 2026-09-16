#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:chunksection.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

// ============================================================================
//  Devouring Storms - position.vsh   [BUILD #416]
//
//  AUTHORED MODULE. Until this build `position.fsh`/`position.vsh` did not
//  exist in the tree at all: ci/build.sh created them with
//      cp block.fsh position.fsh ; cp block.vsh position.vsh
//  a blind fallback copy. That is the "generic fallback render path" this build
//  removes -- the module below is the real program and the build now ASSERTS it
//  exists instead of fabricating it.
//
//  What `position` is in 26.2, stated plainly: the fixed-function position pass
//  the renderer still asks for, shading exactly like a block. It is NOT the sky
//  pass -- SkyRenderer draws the sky through `sky.vsh` / `sky.fsh`, which is
//  where this build put the fading gradient. Mapping the blueprint's sky
//  gradient onto `position` would tint world geometry, so the gradient lives in
//  sky.* and this module keeps the block-safe path (with MCSM_SKY_POSITION
//  available for a platform whose `position` really is a sky pass; see
//  position.fsh).
// ============================================================================

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec3 mcsmWorldPos;

void main() {
    // Identical transform contract to terrain/block: the chunk offset and the
    // camera offset are part of the 26.2 vertex contract for world geometry.
    vec3 pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);

    vertexColor = Color * sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
#ifdef APPLY_TEXTURE_MATRIX
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
#endif

    mcsmWorldPos = pos;
}
