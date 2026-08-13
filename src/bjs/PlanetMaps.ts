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
const MAP_FOR: Partial<Record<PlanetKind, string[]>> = {
  // Several maps per kind so neighbouring planets of the same type do not
  // look like copies of each other.
  [PlanetKind.Terran]: [
    '/art/planet-terran.jpg',
    '/art/planet-eyeball.jpg',
    '/art/planet-ocean.jpg'
  ],
  [PlanetKind.Ice]: ['/art/planet-ice.jpg'],
  [PlanetKind.Gas]: ['/art/planet-gas.jpg', '/art/planet-storm.jpg'],
  [PlanetKind.Lava]: ['/art/planet-volcanic.jpg'],
  [PlanetKind.Desert]: ['/art/planet-desert.jpg'],
  [PlanetKind.Rocky]: ['/art/planet-desert.jpg']
};

/** Picks one of a kind's maps deterministically from the body's seed. */
function variantFor(kind: PlanetKind, seed: number): string | undefined {
  const list = MAP_FOR[kind];
  if (!list || !list.length) return undefined;
  let h = Math.imul((seed >>> 0) ^ 0x632be59b, 0x85ebca6b) >>> 0;
  h ^= h >>> 13;
  return list[h % list.length];
}

/**
 * Rare oddities.
 *
 * Most planets are ordinary. Every so often the universe hands you something
 * that makes no sense, and finding one should feel like a genuine event -
 * so these are deliberately uncommon rather than a menu you pick from.
 */
export interface ExoticSurface {
  id: string;
  name: string;
  url: string;
  /** Relative weight within the exotic roll. */
  weight: number;
  /** Some oddities aren't spherical. */
  shape?: 'cube';
  blurb: string;
}

export const EXOTICS: ExoticSurface[] = [
  {
    id: 'candy', name: 'Confection World', url: '/art/planet-candy.jpg', weight: 3,
    blurb: 'Spun sugar continents over syrup seas. Nobody can explain it.'
  },
  {
    id: 'fungal', name: 'Mycelial World', url: '/art/planet-fungal.jpg', weight: 3,
    blurb: 'A single organism covering a planet, glowing in the dark.'
  },
  {
    id: 'circuit', name: 'Machine World', url: '/art/planet-circuit.jpg', weight: 2,
    blurb: 'Surface-wide circuitry. Still powered. Still running something.'
  },
  {
    id: 'checker', name: 'Tessellated World', url: '/art/planet-checker.jpg', weight: 1,
    shape: 'cube',
    blurb: 'Perfectly cubic, perfectly tiled. Physics disagrees; it persists.'
  },
  {
    id: 'crystal', name: 'Crystalline World', url: '/art/planet-crystal.jpg', weight: 2,
    blurb: 'A planet-sized geode. The whole crust refracts.'
  },
  {
    id: 'iris', name: 'Iris World', url: '/art/planet-iris.jpg', weight: 2,
    blurb: 'Concentric living rings. It appears to be growing.'
  }
];

/** Odds that any given planet turns out to be one of the oddities. */
export const EXOTIC_CHANCE = 0.01;

/**
 * Rolls for an exotic surface using the planet's own seed, so a given world
 * is always the same thing every time you visit it.
 *
 * @param chance override the 1% default (0 disables, 1 forces)
 */
export function rollExotic(seed: number, chance = EXOTIC_CHANCE): ExoticSurface | null {
  // Deterministic hash of the seed -> [0,1)
  let h = Math.imul(seed >>> 0 || 1, 0x9e3779b1) >>> 0;
  h ^= h >>> 15; h = Math.imul(h, 0x85ebca6b) >>> 0;
  h ^= h >>> 13;
  const roll = (h >>> 0) / 4294967296;
  if (roll >= Math.max(0, Math.min(1, chance))) return null;

  // Second, independent draw picks which oddity.
  let g = Math.imul(h ^ 0x27d4eb2f, 0xc2b2ae35) >>> 0;
  g ^= g >>> 16;
  const total = EXOTICS.reduce((n, e) => n + e.weight, 0);
  let pick = ((g >>> 0) / 4294967296) * total;
  for (const e of EXOTICS) {
    pick -= e.weight;
    if (pick <= 0) return e;
  }
  return EXOTICS[0];
}

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
export function applyPlanetMap(
  mat: ShaderMaterial,
  kind: PlanetKind,
  scene: Scene,
  /** Pass the body's seed to let it roll for a rare exotic surface. */
  seed?: number,
  chance = EXOTIC_CHANCE
): boolean {
  const exotic = seed === undefined ? null : rollExotic(seed, chance);
  const url = exotic ? exotic.url : variantFor(kind, seed ?? 0);

  // How deep this world's oceans run. Drives per-channel light absorption in
  // the shader, so an ocean world reads as genuinely deep rather than blue.
  const depthFor: Partial<Record<PlanetKind, number>> = {
    [PlanetKind.Terran]: 0.85,
    [PlanetKind.Ice]: 0.45,
    [PlanetKind.Desert]: 0.18,
    [PlanetKind.Rocky]: 0.10
  };
  mat.setFloat('oceanDepth', depthFor[kind] ?? 0.5);
  // Neutral default. Bound here so that no call site can leave it unset -
  // an unbound float reads as 0 and would render the planet pure black.
  mat.setFloat('exposure', 1);
  if (!url) {
    // No art for this kind: stay fully procedural rather than sampling a
    // texture that was never bound.
    mat.setFloat('useMap', 0);
    return false;
  }
  try {
    if (exotic) lastExotic.set(mat, exotic);
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

/** Which oddity (if any) a given material ended up with. */
const lastExotic = new WeakMap<ShaderMaterial, ExoticSurface>();

/** Returns the exotic surface applied to `mat`, if it rolled one. */
export function exoticOf(mat: ShaderMaterial): ExoticSurface | null {
  return lastExotic.get(mat) ?? null;
}

/** Uniform + sampler names callers must declare on the ShaderMaterial. */
// 'exposure' rides along here because every planet-shader call site already
// spreads this list, so adding it in one place binds it everywhere and no
// site can forget it and read an unbound uniform.
export const PLANET_MAP_UNIFORMS = ['useMap', 'oceanDepth', 'exposure'];
export const PLANET_MAP_SAMPLERS = ['albedoMap'];
