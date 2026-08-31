#version 120

/*
  Story Mode Visuals - sky is fully procedural (see gbuffers_skybasic.fsh).
  This pass is discarded so no textured skybox can ever show cube edges:
  the vanilla sun/moon/stars textures AND any ForgeSkyboxes/FabricSkyboxes
  skybox textures are hidden and replaced by the seamless procedural dome.
*/

varying vec2 texcoord;

void main() {
    discard;
}
