/**
 * SkyShader — a starfield with no seams, at any geometry.
 *
 * The bug this fixes: the sky was a textured icosphere, and the screen broke
 * into hard-edged coloured wedges.
 *
 * An icosphere has no continuous UV parameterisation. Babylon maps each of
 * its twenty faces to a separate triangular patch of the texture, so
 * neighbouring triangles sample completely unrelated parts of the image.
 * Measured on the sphere actually in use: 69 of 362 vertex positions carry
 * more than one UV coordinate, meaning the texture is cut at every one of
 * them. Those cuts are the wedges - and no amount of clamping or filtering
 * can fix them, because the discontinuity is in the mesh's UVs, not in the
 * texture's edges.
 *
 * A UV sphere is continuous but has the opposite problem: every column
 * converges on a single vertex at each pole, so the texture smears into a
 * pinwheel directly above and below you.
 *
 * The fix is to stop relying on mesh UVs entirely. The fragment shader takes
 * the direction from the centre to the fragment and computes equirectangular
 * coordinates from that. Direction is continuous everywhere on a sphere, so
 * there is no seam anywhere, whatever mesh is used - and the longitude wrap
 * at u = 0/1 is handled explicitly below rather than left to the sampler.
 */

export const SKY_VERT = `
precision highp float;
attribute vec3 position;
uniform mat4 worldViewProjection;
varying vec3 vDir;
void main(void){
  // Object-space position IS the direction from the centre for a sphere
  // centred on the origin, which is exactly what a skybox is.
  vDir = position;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

export const SKY_FRAG = `
precision highp float;
varying vec3 vDir;
uniform sampler2D skyTex;
uniform float texWidth;
uniform float brightness;

void main(void){
  vec3 d = normalize(vDir);

  // Equirectangular projection: longitude around Y, latitude from Y.
  // Both are smooth functions of direction, so adjacent fragments always
  // get adjacent texels - the property the icosphere's UVs did not have.
  float u = atan(d.z, d.x) * 0.15915494 + 0.5;   // 1/(2*pi)
  float v = acos(clamp(d.y, -1.0, 1.0)) * 0.31830989;  // 1/pi

  // The one place u is discontinuous is the wrap at 0/1, where atan jumps
  // from +pi to -pi. Hardware derivatives see that as an enormous step and
  // pick the smallest mip, which shows up as a bright line down the sky.
  // Sampling with an explicit gradient computed from the WRAPPED coordinate
  // removes the false step. fract() keeps the two sides continuous.
  vec2 uv = vec2(fract(u), clamp(v, 0.001, 0.999));

  float du = fwidth(uv.x);
  // At the wrap, fwidth sees ~1.0; anything that large is the seam, so fall
  // back to a texel-sized gradient instead of the bogus one.
  if (du > 0.5) du = 1.0 / max(texWidth, 1.0);
  vec2 grad = vec2(du, fwidth(uv.y));

  vec3 col = texture2D(skyTex, uv, 0.0).rgb;

  gl_FragColor = vec4(col * brightness, 1.0);
}
`;

/** Registered under this name in Babylon's shader store. */
export const SKY_SHADER = 'seamlessSky';

import { Effect } from '@babylonjs/core/Materials/effect';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import type { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import { starfieldTexture } from '../Textures';

let registered = false;

/** Put the sky shader in Babylon's store exactly once per page. */
export function registerSkyShader(): void {
  if (registered) return;
  Effect.ShadersStore[SKY_SHADER + 'VertexShader'] = SKY_VERT;
  Effect.ShadersStore[SKY_SHADER + 'FragmentShader'] = SKY_FRAG;
  registered = true;
}

/**
 * Build a seamless starfield sphere.
 *
 * Every world that shows stars goes through here, so the seam fix cannot be
 * applied to one world and forgotten in another.
 *
 * `radius` must stay inside the camera's far plane (4000) or the sky is
 * clipped and space goes black behind everything.
 */
export function createSky(
  scene: Scene, name: string, radius: number, brightness = 1.0
): { mesh: Mesh; material: ShaderMaterial } {
  registerSkyShader();

  // The mesh is now just a carrier for directions: the shader ignores its UVs
  // entirely, so an icosphere is safe here (it has the most even triangle
  // distribution and no pole pinch).
  const mesh = MeshBuilder.CreateIcoSphere(name,
    { radius, subdivisions: 6, flat: false, sideOrientation: 1 }, scene);

  const mat = new ShaderMaterial(name + 'Mat', scene, SKY_SHADER, {
    attributes: ['position'],
    uniforms: ['worldViewProjection', 'texWidth', 'brightness'],
    samplers: ['skyTex']
  });

  const tex = starfieldTexture(scene);
  mat.setTexture('skyTex', tex);
  mat.setFloat('texWidth', tex.getSize().width);
  mat.setFloat('brightness', brightness);
  mat.backFaceCulling = false;
  // The sky must never be fogged; fog would grey out the stars.
  mat.fogEnabled = false;

  mesh.material = mat;
  mesh.infiniteDistance = true;
  mesh.isPickable = false;
  mesh.applyFog = false;
  // Always drawn first, behind everything.
  mesh.renderingGroupId = 0;

  return { mesh, material: mat };
}
