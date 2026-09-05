#version 330 core
uniform mat4 gbufferModelViewInverse, gbufferModelView, gbufferProjection, gbufferProjectionInverse;
uniform mat4 modelViewMatrix, projectionMatrix, textureMatrix, modelViewMatrixInverse, gbufferPreviousModelView;
uniform vec3 skyColor, fogColor, sunPosition, moonPosition, shadowLightPosition, cameraPosition, upPosition, previousCameraPosition;
uniform float sunAngle, frameTimeCounter, viewWidth, viewHeight, rainStrength, aspectRatio, far, near, blindness, nightVision, wetness;
uniform int worldTime, worldDay, isEyeInWater, heldBlockLightValue, frameCounter, entityId;
uniform vec4 entityColor;
uniform sampler2D gtexture, lightmap, colortex0, colortex1, colortex2, colortex3, colortex4, depthtex0, depthtex1, noisetex, gaux1, gaux2, tex;
out vec4 mcsm_FragData[1];

/* MCSM v2: passthrough, lightmap mult dropped to match textured pass. */
in vec4 glcolor;
void main() {
    mcsm_FragData[0] = glcolor;
}
