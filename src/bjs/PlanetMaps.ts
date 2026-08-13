/**
 * Photoreal albedo maps for planet surfaces.
 *
 * The procedural noise stack alone reads as cartoonish up close, so each
 * planet kind gets a real equirectangular texture layered underneath it.
 * The noise is still there as high-frequency break-up, which keeps detail
 * when the camera gets nearer than the texture's resolution.
 *
 * Missing art is not fatal: a body without a map simply falls back to the
 * fully procedural path, so the sim never renders a black or untextured
 * planet.
 */

import type { Scene } from '@babylonjs/core/scene';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import type { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { PlanetKind } from './shaders/PlanetShader';

/** Where each planet kind finds its surface art. */
const MAP_FOR: Partial<Record<PlanetKind, string>> = {
  [PlanetKind.Terran]: '/art/planet-terran.jpg',
  [PlanetKind.Ice]: '/art/planet-ice.jpg',
  [PlanetKind.Gas]: '/art/planet-gas.jpg',
  [PlanetKind.Lava]: '/art/planet-volcanic.jpg',
  [PlanetKind.Desert]: '/art/planet-desert.jpg',
  [PlanetKind.Rocky]: '/art/planet-desert.jpg'
};

/** Cache per scene so twenty planets of one kind share a single upload. */
const cache = new WeakMap<Scene, Map<string, Texture>>();

function get(scene: Scene, url: string): Texture {
  let byUrl = cache.get(scene);
  if (!byUrl) { byUrl = new Map(); cache.set(scene, byUrl); }
  const hit = byUrl.get(url);
  if (hit) return hit;

  const tex = new Texture(url, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
  // Equirectangular maps must wrap horizontally or the seam shows as a line.
  tex.wrapU = Texture.WRAP_ADDRESSMODE;
  tex.wrapV = Texture.CLAMP_ADDRESSMODE;
  tex.anisotropicFilteringLevel = 8;
  byUrl.set(url, tex);
  return tex;
}

/**
 * Binds the right albedo map for `kind` onto `mat`.
 * Returns true if a map was applied.
 */
export function applyPlanetMap(mat: ShaderMaterial, kind: PlanetKind, scene: Scene): boolean {
  const url = MAP_FOR[kind];
  if (!url) {
    // No art for this kind: stay fully procedural rather than sampling a
    // texture that was never bound.
    mat.setFloat('useMap', 0);
    return false;
  }
  try {
    const tex = get(scene, url);
    mat.setTexture('albedoMap', tex);
    mat.setFloat('useMap', 1);

    // If the file is missing or fails to decode, drop straight back to the
    // procedural surface instead of rendering an untextured body.
    tex.onLoadObservable?.addOnce?.(() => { mat.setFloat('useMap', 1); });
    (tex as unknown as { onError?: unknown }).onError = () => { mat.setFloat('useMap', 0); };
    return true;
  } catch {
    mat.setFloat('useMap', 0);
    return false;
  }
}

/** Uniform + sampler names callers must declare on the ShaderMaterial. */
export const PLANET_MAP_UNIFORMS = ['useMap'];
export const PLANET_MAP_SAMPLERS = ['albedoMap'];
