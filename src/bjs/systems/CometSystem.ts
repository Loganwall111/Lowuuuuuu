/**
 * CometSystem — comet traffic on real elliptical orbits.
 *
 * Comets exist in the celestial catalog as fixed points, but a comet that
 * never moves is not a comet - it is a rock. A comet is defined by its
 * orbit: an ellipse around a star, with the tail streaming away from the
 * star and growing as the body dives toward it. Watching a comet swing
 * around a sun is one of the iconic sights of space, and it is pure
 * arithmetic, so this module is engine-free below the renderer and can be
 * tested exactly.
 *
 * Orbit model: a Kepler ellipse with the star at one focus, in the orbital
 * plane, then rotated by argument of perihelion, inclination and ascending
 * node. Semi-implicit Kepler solving (a few Newton steps) turns the mean
 * anomaly into position. The tail direction is simply away from the star.
 *
 * The renderer draws heads as instanced limb-darkened spheres (the shared
 * celestial body shader) and tails as a single additive point cloud, both
 * re-written each frame. A handful of comets costs almost nothing.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Matrix, Quaternion } from '@babylonjs/core/Maths/math.vector';
import { Color4 } from '@babylonjs/core/Maths/math.color';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import type { Scene } from '@babylonjs/core/scene';
import { registerCelestialShader, CELESTIAL_EFFECT } from './CelestialRenderer';
import { renderOrigin } from './RenderOrigin';
import { pooledFloat32, releasePoolPrefix } from './RenderResourcePool';

export interface CometSpec {
  id: string;
  /** Semi-major axis, world units. */
  a: number;
  /** Eccentricity. Kept below 0.9 so it never escapes. */
  e: number;
  /** Inclination, radians. */
  inclination: number;
  /** Ascending node, radians. */
  node: number;
  /** Argument of perihelion, radians. */
  argPeri: number;
  /** Orbital period, seconds. */
  period: number;
  /** Phase at t=0, radians. */
  phase: number;
  /** Head tint, linear RGB. */
  tint: [number, number, number];
  /** Head radius, world units. */
  head: number;
}

/** A comet's position and tail at an instant. */
export interface CometState {
  x: number;
  y: number;
  z: number;
  /** Unit direction the tail streams (away from the star). */
  tx: number;
  ty: number;
  tz: number;
  /** Distance from the star, world units. */
  radius: number;
  /** 0..1 how far it is from perihelion (drives tail length/brightness). */
  activity: number;
}

export const COMET_COUNT = 9;
export const TAIL_POINTS = 22;

/** Deterministic 0..1 from a hash seed. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

/** Builds a family of comets from a seed. Same seed, same sky, forever. */
export function cometSpecs(seed: number, count = COMET_COUNT): CometSpec[] {
  const out: CometSpec[] = [];
  for (let i = 0; i < count; i++) {
    const s = (seed ^ Math.imul(i + 1, 2654435761)) >>> 0;
    out.push({
      id: 'comet-' + i,
      a: 260 + hash01(s + 1) * 460,
      e: 0.35 + hash01(s + 2) * 0.5,
      inclination: (hash01(s + 3) - 0.5) * 1.4,
      node: hash01(s + 4) * Math.PI * 2,
      argPeri: hash01(s + 5) * Math.PI * 2,
      period: 220 + hash01(s + 6) * 520,
      phase: hash01(s + 7) * Math.PI * 2,
      tint: [
        0.75 + hash01(s + 8) * 0.25,
        0.82 + hash01(s + 9) * 0.18,
        1.0
      ],
      head: 1.6 + hash01(s + 10) * 1.6
    });
  }
  return out;
}

/** Solves Kepler's equation for the eccentric anomaly. */
export function eccentricAnomaly(M: number, e: number): number {
  let E = M;
  for (let i = 0; i < 6; i++) {
    E = E - (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
  }
  return E;
}

/** Position and tail of a comet at time t, in star-centred coordinates. */
export function cometState(spec: CometSpec, t: number): CometState {
  const M = (2 * Math.PI * t) / Math.max(spec.period, 1) + spec.phase;
  const E = eccentricAnomaly(M, spec.e);

  // In the orbital plane (star at focus, +x toward perihelion).
  const xp = spec.a * (Math.cos(E) - spec.e);
  const yp = spec.a * Math.sqrt(Math.max(0, 1 - spec.e * spec.e)) * Math.sin(E);

  // Rotate into 3D: perihelion direction, then inclination, then node.
  const cw = Math.cos(spec.argPeri), sw = Math.sin(spec.argPeri);
  const x1 = xp * cw - yp * sw;
  const y1 = xp * sw + yp * cw;
  const ci = Math.cos(spec.inclination), si = Math.sin(spec.inclination);
  const y2 = y1 * ci;
  const z2 = y1 * si;
  const cn = Math.cos(spec.node), sn = Math.sin(spec.node);
  const x = x1 * cn - y2 * sn;
  const y = x1 * sn + y2 * cn;
  const z = z2;

  const r = Math.sqrt(x * x + y * y + z * z);
  // Perihelion distance vs apohelion: activity is 1 at closest approach.
  const peri = spec.a * (1 - spec.e);
  const apo = spec.a * (1 + spec.e);
  const activity = Math.max(0, Math.min(1, (apo - r) / Math.max(apo - peri, 1e-6)));

  // Tail streams away from the star.
  let tx = 0, ty = 0, tz = 1;
  if (r > 1e-6) {
    tx = x / r; ty = y / r; tz = z / r;
  }
  return { x, y, z, tx, ty, tz, radius: r, activity };
}

export class CometRenderer {
  private scene: Scene | null = null;
  private headMesh: Mesh | null = null;
  private headMat: ShaderMaterial | null = null;
  private tailPcs: PointsCloudSystem | null = null;
  private tailMesh: Mesh | null = null;
  private specs: CometSpec[] = [];
  private focus = new Vector3(0, 0, 0);
  private localEye = new Vector3();
  private t = 0;
  private on = true;
  private built = false;
  private generation = 0;

  get count(): number { return this.specs.length; }

  attach(scene: Scene): void {
    this.scene = scene;
  }

  async build(seed = 0x5eedc0de): Promise<void> {
    const scene = this.scene;
    if (!scene || this.built) return;
    const generation = ++this.generation;
    this.specs = cometSpecs(seed);

    try {
      registerCelestialShader();
      // Heads: one instanced sphere with the limb-darkened body shader.
      const head = MeshBuilder.CreateSphere('cometHead', { diameter: 2, segments: 12 }, scene);
      const hm = new ShaderMaterial(CELESTIAL_EFFECT, scene, CELESTIAL_EFFECT, {
        attributes: ['position', 'normal', 'bodyColor'],
        uniforms: ['world', 'viewProjection', 'eyePos'],
        needAlphaBlending: false
      });
      hm.backFaceCulling = true;
      hm.fogEnabled = false;
      head.material = hm;
      head.isPickable = false;
      head.alwaysSelectAsActiveMesh = true;
      head.metadata={...(head.metadata??{}),floatingOriginManaged:true};
      head.renderingGroupId = 0;
      this.headMesh = head;
      this.headMat = hm;

      // Tails: one additive point cloud, rewritten each frame.
      const pcs = new PointsCloudSystem('cometTail', 1.6, scene);
      pcs.addPoints(this.specs.length * TAIL_POINTS, (p: any) => {
        p.position = new Vector3(0, -1e4, 0);
        p.color = new Color4(0, 0, 0, 0);
      });
      const tail = await pcs.buildMeshAsync();
      if (generation !== this.generation || scene !== this.scene) {
        try { tail?.dispose(); pcs.dispose(); head.dispose(); hm.dispose(); } catch { /* superseded */ }
        return;
      }
      if (tail) {
        tail.renderingGroupId = 0;
        tail.isPickable = false;
        tail.applyFog = false;
        tail.alwaysSelectAsActiveMesh = true;
        tail.metadata={...(tail.metadata??{}),floatingOriginManaged:true};
        const m = tail.material as any;
        if (m) {
          m.disableLighting = true;
          m.disableDepthWrite = true;
          m.forceDepthWrite = false;
          m.alpha = 0.999;
          m.alphaMode = 1; // additive
          m.backFaceCulling = false;
        }
      }
      this.tailPcs = pcs;
      this.tailMesh = tail;
      this.built = true;
    } catch (e) {
      console.warn('Comet traffic unavailable:', e);
    this.built = false;
    releasePoolPrefix('comets:');
  }
}

  setEnabled(v: boolean): void {
    this.on = v;
    this.headMesh?.setEnabled(v);
    this.tailMesh?.setEnabled(v);
  }

  /**
   * The comet nearest a point, or null when none are built or in range.
   * Exposed so the gravity tractor can find a comet to steer.
   */
  nearestTo(
    pos: Vector3, maxDist = Infinity
  ): { id: string; spec: CometSpec; distance: number; x: number; y: number; z: number } | null {
    if (!this.built) return null;
    let best: { id: string; spec: CometSpec; distance: number; x: number; y: number; z: number } | null = null;
    for (const spec of this.specs) {
      const st = cometState(spec, this.t);
      const wx = this.focus.x + st.x;
      const wy = this.focus.y + st.y;
      const wz = this.focus.z + st.z;
      const d = Math.hypot(wx - pos.x, wy - pos.y, wz - pos.z);
      if (d <= maxDist && (!best || d < best.distance)) {
        best = { id: spec.id, spec, distance: d, x: wx, y: wy, z: wz };
      }
    }
    return best;
  }

  /**
   * Bends a comet's orbit by a phase offset - the gravity tractor.
   *
   * The offset is applied to the orbital phase, so the comet stays on a
   * valid ellipse while its position along it changes, which is exactly what
   * a gentle gravitational nudge does.
   */
  deflect(id: string, dPhase: number): boolean {
    const spec = this.specs.find((s) => s.id === id);
    if (!spec || !Number.isFinite(dPhase)) return false;
    spec.phase = (spec.phase + dPhase) % (Math.PI * 2);
    return true;
  }

  /** Points the comet family at a star and advances the clock. */
  update(dt: number, focus: Vector3, eye: Vector3): void {
    if (!this.built || !this.scene) return;
    const step = Number.isFinite(dt) ? Math.min(0.1, Math.max(0, dt)) : 0;
    this.t += step;
    this.focus.copyFrom(focus);

    const n = this.specs.length;
    const matrices = pooledFloat32('comets:matrices',n*16);
    const colors = pooledFloat32('comets:colors',n*4);
    const q = Quaternion.Identity();
    const scale = new Vector3(1, 1, 1);
    const pos = new Vector3();
    const tmp = Matrix.Identity();

    const origin=renderOrigin();
    for (let i = 0; i < n; i++) {
      const st = cometState(this.specs[i], this.t);
      scale.set(this.specs[i].head, this.specs[i].head, this.specs[i].head);
      pos.set(focus.x+st.x-origin.x,focus.y+st.y-origin.y,focus.z+st.z-origin.z);
      Matrix.ComposeToRef(scale, q, pos, tmp);
      tmp.copyToArray(matrices, i * 16);
      const t = this.specs[i].tint;
      const bright = 0.35 + st.activity * 0.65;
      colors[i * 4] = t[0];
      colors[i * 4 + 1] = t[1];
      colors[i * 4 + 2] = t[2];
      colors[i * 4 + 3] = bright;
    }

    try {
      this.headMesh?.thinInstanceSetBuffer('matrix', matrices, 16, true);
      this.headMesh?.thinInstanceSetBuffer('bodyColor', colors, 4, true);
      this.localEye.set(eye.x-origin.x,eye.y-origin.y,eye.z-origin.z);
      this.headMat?.setVector3('eyePos', this.localEye);
    } catch { /* disposed mid-frame */ }

    this.writeTails(eye);
  }

  /** Scatters tail points behind each comet, fading with distance. */
  private writeTails(eye: Vector3): void {
    const mesh = this.tailMesh;
    if (!mesh) return;
    const data = mesh.getVerticesData('position');
    const col = mesh.getVerticesData('color');
    if (!data || !col) return;

    const f = this.focus;
    const origin=renderOrigin();
    for (let i = 0; i < this.specs.length; i++) {
      const st = cometState(this.specs[i], this.t);
      const hx = f.x + st.x, hy = f.y + st.y, hz = f.z + st.z;
      const len = 26 + st.activity * 90;
      const tint = this.specs[i].tint;
      for (let k = 0; k < TAIL_POINTS; k++) {
        const idx = i * TAIL_POINTS + k;
        // Points spread from the coma backward, with slight spread.
        const u = (k + 0.5) / TAIL_POINTS;
        const jitter = ((k * 7919) % 100) / 100 - 0.5;
        const spread = u * 9;
        const px = hx + st.tx * u * len + jitter * spread * 0.3;
        const py = hy + st.ty * u * len + jitter * spread;
        const pz = hz + st.tz * u * len + jitter * spread * 0.3;
        data[idx * 3] = px-origin.x;
        data[idx * 3 + 1] = py-origin.y;
        data[idx * 3 + 2] = pz-origin.z;
        const fade = Math.pow(1 - u, 1.6) * (0.35 + st.activity * 0.65);
        col[idx * 4] = tint[0];
        col[idx * 4 + 1] = tint[1];
        col[idx * 4 + 2] = 1.0;
        col[idx * 4 + 3] = Math.min(0.5, fade * 0.5);
      }
    }

    try {
      mesh.updateVerticesData('position', data, false, false);
      mesh.updateVerticesData('color', col, false, false);
      mesh.refreshBoundingInfo();
    } catch { /* disposed mid-frame */ }
  }

  stats(): Record<string, string> {
    return { 'Comets': this.on ? String(this.specs.length) : 'off' };
  }

  dispose(): void {
    this.generation++;
    try { this.headMesh?.dispose(); } catch { /* gone */ }
    try { this.headMat?.dispose(); } catch { /* gone */ }
    try { this.tailPcs?.dispose(); } catch { /* gone */ }
    try { this.tailMesh?.dispose(); } catch { /* gone */ }
    this.headMesh = null;
    this.headMat = null;
    this.tailPcs = null;
    this.tailMesh = null;
    this.built = false;
    this.scene = null;
  }
}
