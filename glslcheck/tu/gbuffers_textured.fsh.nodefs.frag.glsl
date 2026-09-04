#version 330 core
uniform mat4 gbufferModelViewInverse, gbufferModelView, gbufferProjection, gbufferProjectionInverse;
uniform mat4 modelViewMatrix, projectionMatrix, textureMatrix, modelViewMatrixInverse, gbufferPreviousModelView;
uniform vec3 skyColor, fogColor, sunPosition, moonPosition, shadowLightPosition, cameraPosition, upPosition, previousCameraPosition;
uniform float sunAngle, frameTimeCounter, viewWidth, viewHeight, rainStrength, aspectRatio, far, near, blindness, nightVision, wetness;
uniform int worldTime, worldDay, isEyeInWater, heldBlockLightValue, frameCounter, entityId;
uniform vec4 entityColor;
uniform sampler2D lightmap, colortex0, colortex1, colortex2, colortex3, colortex4, depthtex0, depthtex1, noisetex, gaux1, gaux2, tex;
out vec4 mcsm_FragData[1];

/* MCSM v2: passthrough on purpose. No lightmap multiply — clouds and
   particles keep the exact core-pack lighting (user: shader must not
   take the clouds). */
in vec2 texcoord;
in vec2 lmcoord;
in vec4 glcolor;
uniform sampler2D gtexture;
void main() {
    vec4 color = texture(gtexture, texcoord) * glcolor;
    if (color.a <= 0.0) discard;   // MCSM v3: OptiFine-only alpha-ref uniform removed; constant test keeps this program compiling under Iris
    mcsm_FragData[0] = color;
}
