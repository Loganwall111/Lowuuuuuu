/**
 * QuantumAnomalySystem — deterministic deep-space "spacetime cathedrals".
 *
 * One anomaly is derived from every 260,000-unit macro sector. Nothing is
 * stored and nothing is streamed from a server: revisiting the same sector
 * reconstructs exactly the same event from its integer coordinates.
 *
 * Geometry exists only inside visual range. Outside it, the system is four
 * numbers of telemetry, so infinite travel has constant memory cost.
 */
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import { hashChunk } from './ChunkedUniverse';
import { toRenderRef } from './RenderOrigin';

export const ANOMALY_SECTOR_SIZE = 260000;
export const ANOMALY_SENSOR_RANGE = 140000;
export const ANOMALY_VISUAL_RANGE = 4200;

export type AnomalyClass = 'CHRONO RIFT' | 'VOID BLOOM' | 'QUANTUM CHOIR';

export interface QuantumAnomalySpec {
  id: string;
  sx: number; sy: number; sz: number;
  center: Vector3;
  radius: number;
  frequency: number;
  klass: AnomalyClass;
  hue: number;
}

export interface QuantumTelemetry {
  detected: boolean;
  visual: boolean;
  id: string;
  klass: AnomalyClass;
  distance: number;
  signal: number;
  phase: number;
}

function unit(h: number): number {
  let x = h >>> 0;
  x ^= x >>> 16; x = Math.imul(x, 0x7feb352d) >>> 0;
  x ^= x >>> 15; x = Math.imul(x, 0x846ca68b) >>> 0;
  x ^= x >>> 16;
  return (x >>> 0) / 4294967296;
}

/** Pure sector generator, safe for arbitrary coordinates. */
export function anomalyAt(x: number, y: number, z: number, seed = 20260813): QuantumAnomalySpec {
  const sane = (v: number) => Number.isFinite(v) ? v : 0;
  const sx = Math.round(sane(x) / ANOMALY_SECTOR_SIZE);
  const sy = Math.round(sane(y) / ANOMALY_SECTOR_SIZE);
  const sz = Math.round(sane(z) / ANOMALY_SECTOR_SIZE);
  const h = hashChunk(sx, sy, sz, seed ^ 0x51a7f00d);
  const span = ANOMALY_SECTOR_SIZE * 0.16;
  let ox = (unit(h ^ 0xa511e9b3) * 2 - 1) * span;
  let oy = (unit(h ^ 0x63d83595) * 2 - 1) * span * 0.55;
  let oz = (unit(h ^ 0x9e3779b9) * 2 - 1) * span;
  // The home sector opens with one composed vista rather than leaving the
  // signature feature hundreds of thousands of units from a new player.
  if (sx === 0 && sy === 0 && sz === 0) { ox = -2600; oy = 1200; oz = 4700; }
  const classes: AnomalyClass[] = ['CHRONO RIFT', 'VOID BLOOM', 'QUANTUM CHOIR'];
  return {
    id: `QA-${(h >>> 0).toString(16).toUpperCase().padStart(8, '0')}`,
    sx, sy, sz,
    center: new Vector3(sx * ANOMALY_SECTOR_SIZE + ox,
      sy * ANOMALY_SECTOR_SIZE + oy,
      sz * ANOMALY_SECTOR_SIZE + oz),
    radius: 520 + unit(h ^ 0x27d4eb2d) * 840,
    frequency: 0.45 + unit(h ^ 0x165667b1) * 1.35,
    klass: classes[h % classes.length],
    hue: unit(h ^ 0x85ebca6b)
  };
}

export class QuantumAnomalySystem {
  private scene: Scene | null = null;
  private seed: number;
  private spec: QuantumAnomalySpec | null = null;
  private rings: Mesh[] = [];
  private mats: StandardMaterial[] = [];
  private core: Mesh | null = null;
  private localCenter = new Vector3();
  private t = 0;
  private last: QuantumTelemetry | null = null;
  private wasDetected = false;
  private detectionPulse = false;

  constructor(seed = 20260813) { this.seed = seed >>> 0; }

  attach(scene: Scene, seed = this.seed): void {
    this.disposeGeometry();
    this.scene = scene;
    this.seed = seed >>> 0;
    this.spec = null;
    this.last = null;
  }

  /** True once when the long-range suit sensor first acquires the event. */
  consumeDetection(): boolean {
    const v = this.detectionPulse;
    this.detectionPulse = false;
    return v;
  }

  telemetry(): QuantumTelemetry | null { return this.last; }

  update(dt: number, eye: Vector3, renderVisual = true): number {
    if (!this.scene) return 0;
    this.t += Math.max(0, Math.min(.1, Number.isFinite(dt) ? dt : 0));
    const sx = Math.round((Number.isFinite(eye.x) ? eye.x : 0) / ANOMALY_SECTOR_SIZE);
    const sy = Math.round((Number.isFinite(eye.y) ? eye.y : 0) / ANOMALY_SECTOR_SIZE);
    const sz = Math.round((Number.isFinite(eye.z) ? eye.z : 0) / ANOMALY_SECTOR_SIZE);
    if (!this.spec || sx !== this.spec.sx || sy !== this.spec.sy || sz !== this.spec.sz) {
      this.disposeGeometry();
      this.spec = anomalyAt(eye.x, eye.y, eye.z, this.seed);
      this.wasDetected = false;
    }
    const s = this.spec;
    const distance = Vector3.Distance(eye, s.center);
    const signal = Math.max(0, Math.min(1, 1 - distance / ANOMALY_SENSOR_RANGE));
    const detected = signal > 0;
    // Large absolute transforms lose metre-scale precision in WebGL. Keep
    // telemetry procedural at any coordinate, but suppress unstable physical
    // rings when the caller enters translation-free deep-space rendering.
    const visual = renderVisual && distance < ANOMALY_VISUAL_RANGE;
    if (detected && !this.wasDetected) this.detectionPulse = true;
    this.wasDetected = detected;
    if (visual && !this.core) this.buildGeometry(s);
    else if (!visual && this.core) this.disposeGeometry();
    if (visual) this.animate(s, signal, dt);
    const phase = .5 + .5 * Math.sin(this.t * s.frequency);
    if (!this.last) {
      this.last = { detected, visual, id: s.id, klass: s.klass, distance, signal, phase };
    } else {
      this.last.detected = detected; this.last.visual = visual;
      this.last.id = s.id; this.last.klass = s.klass;
      this.last.distance = distance; this.last.signal = signal; this.last.phase = phase;
    }
    return signal;
  }

  private buildGeometry(s: QuantumAnomalySpec): void {
    const scene = this.scene;
    if (!scene) return;
    const cyan = new Color3(0, .94, 1);
    const violet = new Color3(.46, .22, 1);
    toRenderRef(s.center, this.localCenter);
    for (let i = 0; i < 5; i++) {
      const ring = MeshBuilder.CreateTorus('quantumRing' + i, {
        diameter: s.radius * (1 + i * .19), thickness: 2.2 + i * .75,
        tessellation: 96
      }, scene);
      ring.position.copyFrom(this.localCenter);
      ring.rotation.set(i * .47, i * .71, i * .29);
      ring.isPickable = false;
      ring.alwaysSelectAsActiveMesh = true;
      ring.renderingGroupId = 1;
      const mat = new StandardMaterial('quantumRingM' + i, scene);
      mat.diffuseColor = Color3.Black();
      mat.specularColor = Color3.Black();
      mat.emissiveColor = i % 2 ? violet : cyan;
      mat.disableLighting = true;
      mat.alpha = .42 - i * .035;
      mat.alphaMode = 1;
      mat.backFaceCulling = false;
      mat.disableDepthWrite = true;
      ring.material = mat;
      this.rings.push(ring); this.mats.push(mat);
    }
    // The centre is an aperture, never a solid. A polyhedron here used to
    // become the giant faceted black object in the player's screenshot when
    // additive material compilation fell back. A thin photon ring cannot
    // occlude the universe even under a fallback material.
    const core = MeshBuilder.CreateTorus('quantumCore', {
      diameter: s.radius * .28, thickness: 1.4, tessellation: 96
    }, scene);
    core.position.copyFrom(this.localCenter);
    core.rotation.x = Math.PI * .5;
    core.isPickable = false;
    core.alwaysSelectAsActiveMesh = true;
    core.renderingGroupId = 1;
    const cm = new StandardMaterial('quantumCoreM', scene);
    cm.diffuseColor = Color3.Black(); cm.specularColor = Color3.Black();
    cm.emissiveColor = new Color3(.35, .9, 1);
    cm.disableLighting = true; cm.alpha = .58; cm.alphaMode = 1;
    cm.disableDepthWrite = true; cm.backFaceCulling = false;
    core.material = cm;
    this.core = core; this.mats.push(cm);
  }

  private animate(s: QuantumAnomalySpec, signal: number, dt: number): void {
    const step = Math.max(0, Math.min(.1, Number.isFinite(dt) ? dt : 0));
    const pulse = 1 + Math.sin(this.t * s.frequency * 2.4) * (.025 + signal * .035);
    for (let i = 0; i < this.rings.length; i++) {
      const r = this.rings[i];
      const dir = i % 2 ? -1 : 1;
      r.rotation.x += step * .042 * dir * (i + 1);
      r.rotation.y += step * .066 * dir * (6 - i);
      r.rotation.z += step * .03 * (i + 1);
      r.scaling.setAll(pulse + i * .006 * Math.sin(this.t + i));
      const m = this.mats[i];
      if (m) m.alpha = .22 + signal * .28 + .08 * Math.sin(this.t * 2 + i);
    }
    if (this.core) {
      this.core.rotation.x += step * .24; this.core.rotation.y -= step * .36;
      this.core.scaling.setAll(.88 + pulse * .12);
    }
  }

  private disposeGeometry(): void {
    for (const r of this.rings) { try { r.dispose(); } catch { /* gone */ } }
    for (const m of this.mats) { try { m.dispose(); } catch { /* gone */ } }
    try { this.core?.dispose(); } catch { /* gone */ }
    this.rings.length = 0; this.mats.length = 0; this.core = null;
  }

  dispose(): void {
    this.disposeGeometry();
    this.scene = null; this.spec = null; this.last = null;
  }
}
