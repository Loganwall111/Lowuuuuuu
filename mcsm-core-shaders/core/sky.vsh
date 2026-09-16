#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:mcsm_visuals.glsl>

// ============================================================================
//  Devouring Storms - sky.vsh   [BUILD #416 - real fading sky]
//
//  Companion to sky.fsh (REQUIRED pair: the extra varyings must exist on both
//  sides). 26.2 draws the sky with this program (vanilla SkyRenderer's disc),
//  and the disc geometry is IRRELEVANT to the result: the fragment stage paints
//  from the view RAY, so the sky is a continuous direction field with no dome
//  silhouette, no disc rim and no clipped "top" - the failure mode of every
//  previous build, where a quad/dome boundary was visible as a hard edge.
//
//  The sky pass ModelViewMat is rotation-only and Position is the camera-space
//  frustum corner, so Position itself IS the view ray.
// ============================================================================

in vec3 Position;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec3 mcsmCamRay;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);

    // The view ray. Everything the sky paints is a function of this, which is
    // what makes the fade continuous instead of a shape with an edge.
    mcsmCamRay = Position;
}
