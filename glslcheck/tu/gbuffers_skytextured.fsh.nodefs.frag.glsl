#version 330 core
uniform mat4 gbufferModelViewInverse, gbufferModelView, gbufferProjection, gbufferProjectionInverse;
uniform mat4 modelViewMatrix, projectionMatrix, textureMatrix, modelViewMatrixInverse, gbufferPreviousModelView;
uniform vec3 skyColor, fogColor, sunPosition, moonPosition, shadowLightPosition, cameraPosition, upPosition, previousCameraPosition;
uniform float sunAngle, frameTimeCounter, viewWidth, viewHeight, rainStrength, aspectRatio, far, near, blindness, nightVision, wetness;
uniform int worldTime, worldDay, isEyeInWater, heldBlockLightValue, frameCounter, entityId;
uniform vec4 entityColor;
uniform sampler2D lightmap, colortex0, colortex1, colortex2, colortex3, colortex4, depthtex0, depthtex1, noisetex, gaux1, gaux2, tex;
out vec4 mcsm_FragData[1];


// Sun and moon. Kept close to vanilla so the celestial bodies still read,
// with a mild warm push on the sun to match the Story Mode palette.

in vec2 texcoord;
in vec4 glcolor;
uniform sampler2D gtexture;

void main() {
    vec4 c = texture(gtexture, texcoord) * glcolor;
    c.rgb *= vec3(1.04, 0.99, 0.94);
    mcsm_FragData[0] = c;
}
