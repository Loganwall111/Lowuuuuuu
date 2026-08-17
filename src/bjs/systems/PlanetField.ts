/**
 * PlanetField — the universe's planets as real, reachable spheres.
 *
 * Until now a planet in the universe was only ever a point of light in the
 * starfield: the starfield drew every region at a fixed 2px point size, so
 * flying toward a world never made it grow - it stayed the same speck until
 * you were on top of it. Real space does the opposite: a planet is a disc
 * that swells as you close on it and shrinks to a star as you leave.
 *
 * This renders the planet/ocean/terrain regions as thin instances of one
 * sphere at their true coordinates, limb-darkened like the rest of the sky
 * bodies. Because they are real geometry at real positions, ordinary
 * perspective does the scaling for free: approach and it grows, retreat and
 * it shrinks. The starfield still draws the same bodies as points beyond
 * this field's range, so the two hand over seamlessly rather than doubling
 * up - the near sphere simply occludes its own distant point.
 *
 * Streaming mirrors CelestialRenderer: the instance set is rebuilt only when
 * the camera has moved far enough to change which worlds are in range.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Matrix, Quaternion } from '@babylonjs/core/Maths/math.vector';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import type { Scene } from '@babylonjs/core/scene';
import { registerCelestialShader, CELESTIAL_EFFECT } from './CelestialRenderer';

/** A region that has a solid, walkable surface. */
export interface PlanetFieldSource {
  id: string;
  kind: string;
  position: Vector3;
  surfaceRadius: number;
}

export interface PlanetFieldOptions {
  /** How far out worlds are realised as spheres. */
  range: number;
  /** Rebuild once the camera has moved this far. */
  rebuildAfter: number;
  /** Hard cap on instances, so a dense pocket cannot stall a frame. */
  maxBodies: number;
}

export const DEFAULT_PLANET_FIELD: PlanetFieldOptions = {
  // Real geometry takes over well before a world is visibly a disc, and its
  // transform is refreshed often enough that approach growth is continuous.
  range: 18000,
  rebuildAfter: 18,
  maxBodies: 180
};

/** Base colour per world kind, linear RGB 0..1. */
const KIND_COLOR: Record<string, [number, number, number]> = {
  ocean: [0.28, 0.55, 0.95],
  terrain: [0.80, 0.62, 0.40],
  planet: [0.70, 0.72, 0.80]
};

/** Deterministic 0..1 hash of a string, so one world is always one colour. */
function hash01(s: string): number {
  let h = 2166136261 >>> 0;
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i);
    h = Math.imul(h, 16777619) >>> 0;
  }
  return (h >>> 0) / 4294967296;
}

/**
 * Per-world tint: the kind's base colour with a stable, seeded variation so
 * two ocean worlds are not the same ocean world.
 */
export function planetTint(id: string, kind: string): [number, number, number] {
  const base = KIND_COLOR[kind] ?? [0.7, 0.72, 0.8];
  const r = hash01(id + 'r');
  const g = hash01(id + 'g');
  const b = hash01(id + 'b');
  const vary = (v: number, k: number) =>
    Math.max(0, Math.min(1, v * (0.82 + (k - 0.5) * 0.36)));
  return [vary(base[0], r), vary(base[1], g), vary(base[2], b)];
}

export class PlanetField {
  opts: PlanetFieldOptions;
  private scene: Scene | null = null;
  private mesh: Mesh | null = null;
  private mat: ShaderMaterial | null = null;
  private lastBuild = new Vector3(1e12, 1e12, 1e12);
  private on = true;
  /** Worlds currently realised as spheres. */
  live: PlanetFieldSource[] = [];

  constructor(opts: Partial<PlanetFieldOptions> = {}) {
    this.opts = { ...DEFAULT_PLANET_FIELD, ...opts };
  }

  get count(): number { return this.live.length; }

  attach(scene: Scene): void {
    if (this.mesh) return;
    this.scene = scene;
    registerCelestialShader();

    const m = MeshBuilder.CreateSphere('planetFieldBody', {
      diameter: 2, segments: 16
    }, scene);
    // Reuses the shared limb-darkened body shader: the edge is dimmer than
    // the centre and a faint corona rides the rim, so a planet reads as a
    // solid sphere of rock/gas rather than a flat painted disc.
    const mat = new ShaderMaterial(CELESTIAL_EFFECT, scene, CELESTIAL_EFFECT, {
      attributes: ['position', 'normal', 'bodyColor'],
      uniforms: ['world', 'viewProjection', 'eyePos'],
      needAlphaBlending: false
    });
    mat.backFaceCulling = true;
    mat.fogEnabled = false;

    m.material = mat;
    m.isPickable = false;
    m.alwaysSelectAsActiveMesh = true;
    m.renderingGroupId = 0;
    m.setEnabled(this.on);

    this.mesh = m;
    this.mat = mat;
  }

  setEnabled(v: boolean): void {
    this.on = v;
    if (this.mesh) this.mesh.setEnabled(v);
  }

  /** Worlds worth realising from the region list. */
  static candidates(
    regions: readonly { id: string; kind: string; position: Vector3; surfaceRadius?: number }[],
    eye: Vector3, range: number
  ): PlanetFieldSource[] {
    const out: PlanetFieldSource[] = [];
    for (const r of regions) {
      if (r.kind !== 'planet' && r.kind !== 'ocean' && r.kind !== 'terrain') continue;
      const sr = r.surfaceRadius ?? 0;
      if (!(sr > 0)) continue;
      const d = Vector3.Distance(r.position, eye) - sr;
      if (d <= range) {
        out.push({ id: r.id, kind: r.kind, position: r.position, surfaceRadius: sr });
      }
    }
    return out;
  }

  /**
   * Rebuilds the instance set if the camera has moved far enough. Returns
   * true when a rebuild actually happened.
   */
  update(regions: readonly {
    id: string; kind: string; position: Vector3; surfaceRadius?: number;
    orbitParentId?: string;
  }[], eye: Vector3): boolean {
    if (!this.mesh || !this.on) return false;
    this.mat?.setVector3('eyePos', eye);
    // Orbiting worlds move even while the viewer is still. Their instance
    // matrices therefore refresh continuously; static fields retain the
    // distance throttle.
    const liveOrbits = regions.some((r) => !!r.orbitParentId);
    if (!liveOrbits && Vector3.Distance(eye, this.lastBuild) < this.opts.rebuildAfter) {
      return false;
    }
    this.lastBuild.copyFrom(eye);

    let found = PlanetField.candidates(regions, eye, this.opts.range);
    if (found.length > this.opts.maxBodies) {
      found = found
        .map((s) => ({ s, d: Vector3.Distance(s.position, eye) - s.surfaceRadius }))
        .sort((a, b) => a.d - b.d)
        .slice(0, this.opts.maxBodies)
        .map((p) => p.s);
    }
    this.live = found;

    const n = found.length;
    if (n === 0) {
      this.mesh.thinInstanceCount = 0;
      return true;
    }

    const matrices = new Float32Array(n * 16);
    const colors = new Float32Array(n * 4);
    const q = Quaternion.Identity();
    const scale = new Vector3(1, 1, 1);
    const pos = new Vector3();
    const tmp = Matrix.Identity();

    for (let i = 0; i < n; i++) {
      const s = found[i];
      scale.set(s.surfaceRadius, s.surfaceRadius, s.surfaceRadius);
      pos.copyFrom(s.position);
      Matrix.ComposeToRef(scale, q, pos, tmp);
      tmp.copyToArray(matrices, i * 16);

      const tint = planetTint(s.id, s.kind);
      // A gentle per-world brightness variation in the alpha channel, which
      // the shared shader multiplies against the limb-darkened body.
      const lum = 0.34 + hash01(s.id + 'lum') * 0.22;
      colors[i * 4 + 0] = tint[0];
      colors[i * 4 + 1] = tint[1];
      colors[i * 4 + 2] = tint[2];
      colors[i * 4 + 3] = lum;
    }

    this.mesh.thinInstanceSetBuffer('matrix', matrices, 16, true);
    this.mesh.thinInstanceSetBuffer('bodyColor', colors, 4, true);
    return true;
  }

  stats(): Record<string, string> {
    return { 'Planet bodies': this.on ? String(this.live.length) : 'off' };
  }

  dispose(): void {
    this.mesh?.dispose();
    this.mat?.dispose();
    this.mesh = null;
    this.mat = null;
    this.live = [];
    this.scene = null;
  }
}
