/**
 * GalaxyPointShader — soft volumetric points for the galaxy field.
 *
 * Babylon's PointsCloudSystem builds a StandardMaterial with
 * `pointsCloud = true`, which draws hard square GL_POINTS. At the 14px size
 * the nebula gas needs in order to be visible at all, those squares are
 * plainly visible as chunky digital boxes - exactly the artefact in the
 * reported screenshot. There is no fragment program in that path, so the
 * fix cannot live anywhere but a custom material.
 *
 * Two things happen here that the stock path cannot do:
 *
 *   1. The quad is clipped to a disc and its opacity decays outward, so a
 *      point reads as a soft puff of gas rather than a tile.
 *   2. Point size is carried PER POINT in an attribute rather than being one
 *      material-wide constant, so fine stars and broad gas clouds can share
 *      a single draw call. Distance attenuation then makes the near edge of
 *      a cloud bloom and the far edge tighten, which is what sells depth.
 *
 * The falloff is a true Gaussian rather than a linear ramp. A linear edge
 * leaves a visible circular rim where alpha hits zero - the puffs read as
 * discs instead of fog. exp(-k r^2) never actually reaches zero, so the
 * edge dissolves.
 */

import { Effect } from '@babylonjs/core/Materials/effect';

export const GALAXY_POINT_SHADER = 'galaxyPoint';

export const GALAXY_POINT_VERT = `
precision highp float;
attribute vec3 position;
attribute vec4 color;
attribute float pointSize;

uniform mat4 worldViewProjection;
uniform mat4 world;
uniform vec3 camPos;
/** Global multiplier so the whole field can be tuned from one place. */
uniform float sizeScale;
/** Screen height in pixels, so a point's size is resolution independent. */
uniform float viewportHeight;

varying vec4 vColor;

void main(void) {
  vec4 wp = world * vec4(position, 1.0);
  gl_Position = worldViewProjection * vec4(position, 1.0);

  // Attenuate with distance the way a real projection does. Without this a
  // cloud looks identical whether you are beside it or far from it, which
  // flattens the galaxy into a decal.
  float d = max(length(wp.xyz - camPos), 1.0);
  float sz = pointSize * sizeScale * (viewportHeight / max(d, 1.0));

  // Never let a point collapse to nothing or swallow the screen. The upper
  // clamp is the guard against the original magenta blow-out: a handful of
  // enormous additive quads stacking on one another.
  gl_PointSize = clamp(sz, 1.0, 64.0);

  vColor = color;
}
`;

export const GALAXY_POINT_FRAG = `
precision highp float;

varying vec4 vColor;

/** Overall opacity of the field. */
uniform float gasDensity;

void main(void) {
  // Distance from the centre of the point sprite, 0 at the middle and
  // ~0.707 at the corners of the quad.
  vec2 d = gl_PointCoord - vec2(0.5);
  float r = length(d);

  // Clip the corners away. This alone turns the squares into circles.
  if (r > 0.5) discard;

  // Gaussian decay. Tuned so the centre stays solid and the rim vanishes
  // smoothly rather than terminating on a hard circular edge.
  float g = exp(-r * r * 11.0);

  // Fade the outermost sliver to exactly zero so no rim can survive the
  // Gaussian's long tail.
  float edge = smoothstep(0.5, 0.34, r);

  float a = vColor.a * g * edge * gasDensity;
  if (a < 0.002) discard;

  // Additive blending: premultiply so the colour carries the falloff too,
  // otherwise every point keeps full-intensity colour and only the alpha
  // softens, which brings the hard disc straight back.
  gl_FragColor = vec4(vColor.rgb * a, a);
}
`;

let registered = false;

/** Registers the shader once. Safe to call repeatedly. */
export function registerGalaxyPointShader(): void {
  if (registered) return;
  Effect.ShadersStore[GALAXY_POINT_SHADER + 'VertexShader'] = GALAXY_POINT_VERT;
  Effect.ShadersStore[GALAXY_POINT_SHADER + 'FragmentShader'] = GALAXY_POINT_FRAG;
  registered = true;
}
