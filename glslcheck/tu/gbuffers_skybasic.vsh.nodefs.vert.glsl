#version 330 core
uniform mat4 gbufferModelViewInverse, gbufferModelView, gbufferProjection, gbufferProjectionInverse;
uniform mat4 modelViewMatrix, projectionMatrix, textureMatrix, modelViewMatrixInverse, gbufferPreviousModelView;
uniform vec3 skyColor, fogColor, sunPosition, moonPosition, shadowLightPosition, cameraPosition, upPosition, previousCameraPosition;
uniform float sunAngle, frameTimeCounter, viewWidth, viewHeight, rainStrength, aspectRatio, far, near, blindness, nightVision, wetness;
uniform int worldTime, worldDay, isEyeInWater, heldBlockLightValue, frameCounter, entityId;
uniform vec4 entityColor;
uniform sampler2D gtexture, lightmap, colortex0, colortex1, colortex2, colortex3, colortex4, depthtex0, depthtex1, noisetex, gaux1, gaux2, tex;
in vec4 mcsm_gl_Vertex; in vec4 mcsm_gl_Color; in vec3 mcsm_gl_Normal;
in vec4 mcsm_gl_MultiTexCoord0; in vec4 mcsm_gl_MultiTexCoord1; in vec4 mcsm_gl_MultiTexCoord2;
uniform mat4 mcsm_TextureMatrix[8];
uniform mat4 mcsm_ModelViewMatrix, mcsm_ProjectionMatrix, mcsm_ModelViewProjectionMatrix, mcsm_NormalMatrix4;
mat3 mcsm_NormalMatrix = mat3(mcsm_NormalMatrix4);
vec4 ftransform() { return projectionMatrix * modelViewMatrix * mcsm_gl_Vertex; }


out vec4 starData;
out vec3 viewPos;

void main() {
    gl_Position = ftransform();
    viewPos = (mcsm_ModelViewMatrix * mcsm_gl_Vertex).xyz;
    // vanilla marks star geometry by having vertex colour; sky dome is untextured
    starData = vec4(mcsm_gl_Color.rgb, float(mcsm_gl_Color.r + mcsm_gl_Color.g + mcsm_gl_Color.b > 0.01));
}
