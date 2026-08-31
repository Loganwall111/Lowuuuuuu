#version 120
/* DRAWBUFFERS:0 */

/*
  Story Mode Visuals - the sky is fully procedural (see gbuffers_skybasic.fsh).
  This pass discards every textured sky element so no skybox texture can ever
  draw cube edges: the seamless dome fully replaces the vanilla sky.
*/

varying vec2 texcoord;

void main() {
    discard;
}
