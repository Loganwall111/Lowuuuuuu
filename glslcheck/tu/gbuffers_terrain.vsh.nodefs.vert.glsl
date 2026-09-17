#version 330 core
uniform mat4 gbufferModelViewInverse, gbufferModelView, gbufferProjection, gbufferProjectionInverse;
uniform mat4 modelViewMatrix, projectionMatrix, textureMatrix, modelViewMatrixInverse, gbufferPreviousModelView;
uniform vec3 skyColor, fogColor, sunPosition, moonPosition, shadowLightPosition, cameraPosition, upPosition, previousCameraPosition;
uniform float frameTimeCounter, viewWidth, viewHeight, rainStrength, aspectRatio, far, near, blindness, nightVision, wetness;
uniform int worldTime, worldDay, isEyeInWater, heldBlockLightValue, frameCounter, entityId;
uniform vec4 entityColor;
uniform sampler2D gtexture, lightmap, colortex0, colortex1, colortex2, colortex3, colortex4, depthtex0, depthtex1, noisetex, gaux1, gaux2, tex;
in vec4 mcsm_gl_Vertex; in vec4 mcsm_gl_Color; in vec3 mcsm_gl_Normal;
in vec4 mcsm_gl_MultiTexCoord0; in vec4 mcsm_gl_MultiTexCoord1; in vec4 mcsm_gl_MultiTexCoord2;
uniform mat4 mcsm_TextureMatrix[8];
uniform mat4 mcsm_ModelViewMatrix, mcsm_ProjectionMatrix, mcsm_ModelViewProjectionMatrix, mcsm_NormalMatrix4;
mat3 mcsm_NormalMatrix = mat3(mcsm_NormalMatrix4);
vec4 ftransform() { return projectionMatrix * modelViewMatrix * mcsm_gl_Vertex; }

/*
 * MCSM v2 — terrain vertex: adds a sun-shadow term.
 * The old composite-stage grading never reached the screen (composite wrote
 * colortex1, final read colortex0), so with this pack on the core pack's
 * terrain.vsh was replaced by a bare pass and ALL shading vanished — that is
 * the "shadows don't render under the shader" bug. Fix: shade here, grade in final.
 */
uniform float sunAngle;
out vec2 texcoord;
out vec2 lmcoord;
out vec4 glcolor;
out vec3 mcsmN;
out float mcsmDay;

void main() {
    gl_Position = ftransform();
    texcoord = (mcsm_TextureMatrix[0] * mcsm_gl_MultiTexCoord0).xy;
    lmcoord  = (mcsm_TextureMatrix[1] * mcsm_gl_MultiTexCoord1).xy;
    glcolor  = mcsm_gl_Color;

    // OptiFine reports sunAngle in degrees, Iris in radians — accept both.
    float ang = sunAngle > 15.0 ? radians(sunAngle) : sunAngle;
    float elev = cos(ang);                       // ~+1 at noon, <0 at night
    mcsmDay = clamp(elev * 2.4, 0.0, 1.0);
    mcsmN = mcsm_gl_Normal;                           // terrain is never rotated
}
