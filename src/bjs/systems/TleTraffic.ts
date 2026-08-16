/**
 * TleTraffic — the ten-thousand-object satellite cloud, live.
 *
 * The catalogue is real TLE data; this puts it in the sky. Every bird is
 * propagated from its own epoch to the real clock and drawn as one additive
 * point cloud around the home world, so Earth wears the shell of satellites
 * it actually has - thousands of LEO comms birds, the GPS/GLONASS/Galileo/
 * BeiDou rings, the geostationary belt, and a faint debris halo. Because the
 * orbits are in real kilometres and the home world has a real radius, the
 * shells fall out at the correct relative altitudes.
 *
 * One draw call for the whole cloud. Positions live in a persistent buffer
 * (no per-frame allocation), and the cloud is drawn additively with depth
 * write off, so it can only ever add light, never occlude or be occluded.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color4 } from '@babylonjs/core/Maths/math.color';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import {
  tlePositionKm, minutesSinceEpoch, type TleRecord
} from './Tle';
import { buildCatalog, POPULATIONS } from './TleCatalog';

/** Earth's visual radius in the home system, in world units. */
const EARTH_VISUAL_RADIUS = 1.15;
/** 1 world unit in kilometres, from the Earth visual/real radius ratio. */
export const KM_PER_UNIT = 6371 / EARTH_VISUAL_RADIUS;

export class TleTraffic {
  private scene: Scene | null = null;
  private records: TleRecord[] = [];
  private pcs: PointsCloudSystem | null = null;
  private mesh: Mesh | null = null;
  private positions: Float32Array | null = null;
  private colors: Float32Array | null = null;
  private built = false;
  /** Seconds between full re-propagations; 10k birds at 60fps is 600k
   *  sqrt/atan2 a second, which is real frame cost for no visual gain. */
  private acc = 0;

  get count(): number { return this.records.length; }

  attach(scene: Scene): void {
    this.scene = scene;
  }

  /** Builds the cloud from the deterministic catalogue. */
  async build(seed = 0x7e1e): Promise<void> {
    const scene = this.scene;
    if (!scene || this.built) return;
    this.records = buildCatalog(seed);

    // One tint per population, so the shell colours come from the regime.
    const tintOf: Record<string, [number, number, number]> = {};
    for (const p of POPULATIONS) tintOf[p.name] = p.tint;

    const pcs = new PointsCloudSystem('tleTraffic', 1.1, scene);
    pcs.addPoints(this.records.length, (p: any, i: number) => {
      p.position = new Vector3(0, -1e5, 0);
      const t = tintOf[this.records[i].name.replace(/-\d+$/, '')] ?? [0.6, 0.6, 0.65];
      p.color = new Color4(t[0], t[1], t[2], 0.6);
    });

    const mesh = await pcs.buildMeshAsync();
    if (mesh) {
      mesh.renderingGroupId = 0;
      mesh.isPickable = false;
      mesh.applyFog = false;
      mesh.alwaysSelectAsActiveMesh = true;
      const m = mesh.material as any;
      if (m) {
        m.disableLighting = true;
        m.disableDepthWrite = true;
        m.forceDepthWrite = false;
        m.alpha = 0.999;
        m.alphaMode = 1;      // additive
        m.backFaceCulling = false;
      }
    }
    this.pcs = pcs;
    this.mesh = mesh;
    this.positions = new Float32Array(this.records.length * 3);
    this.colors = new Float32Array(this.records.length * 4);
    this.built = true;
  }

  /**
   * Propagates every bird to the real clock and writes the cloud around the
   * home world's centre. A satellite's km position is scaled down to world
   * units, so the geostationary belt sits ~7 units out and LEO hugs the
   * planet, exactly as it should.
   *
   * Re-propagation is throttled: orbital motion at these scales is far
   * slower than a frame, so a ~2 Hz update is indistinguishable from a
   * per-frame one and costs a fraction of the CPU.
   */
  update(center: Vector3, dt = 0): void {
    const mesh = this.mesh;
    const pos = this.positions;
    const col = this.colors;
    if (!mesh || !pos || !col || !this.records.length) return;

    this.acc += Number.isFinite(dt) ? Math.max(0, dt) : 0;
    if (this.acc < 0.5) return;
    this.acc = 0;

    const now = new Date();
    const scratch: [number, number, number] = [0, 0, 0];
    for (let i = 0; i < this.records.length; i++) {
      const r = this.records[i];
      const mins = minutesSinceEpoch(r, now);
      tlePositionKm(r, mins, scratch);
      pos[i * 3] = center.x + scratch[0] / KM_PER_UNIT;
      pos[i * 3 + 1] = center.y + scratch[1] / KM_PER_UNIT;
      pos[i * 3 + 2] = center.z + scratch[2] / KM_PER_UNIT;
    }

    try {
      mesh.updateVerticesData('position', pos, false, false);
      mesh.refreshBoundingInfo();
    } catch { /* disposed mid-frame */ }
  }

  stats(): Record<string, string> {
    return { 'TLE satellites': this.built ? String(this.records.length) : 'off' };
  }

  dispose(): void {
    try { this.pcs?.dispose(); } catch { /* gone */ }
    try { this.mesh?.dispose(); } catch { /* gone */ }
    this.pcs = null;
    this.mesh = null;
    this.positions = null;
    this.colors = null;
    this.records = [];
    this.built = false;
    this.scene = null;
  }
}
