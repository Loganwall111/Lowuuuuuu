/**
 * SkyProbe - a live 360-degree cubemap of the procedural sky.
 *
 * WHAT THIS IS FOR
 *
 * The sky dome and the black-hole raymarcher already share one GLSL
 * function, so a lensed ring shows the true background by construction.
 * This probe is the OTHER half: it hands the same environment to everything
 * that needs a real texture rather than a function call - reflective hulls,
 * station glass, PBR ambient light, and any effect that wants to sample the
 * sky in a direction without re-running the sky maths.
 *
 * So the two mechanisms are not redundant:
 *
 *   shared GLSL  -> the hole bends the sky, at infinite sharpness, because
 *                   it evaluates the sky function per ray. A cubemap would
 *                   cap resolution exactly at the Einstein ring, which is
 *                   where a tiny patch of sky is magnified the most and
 *                   where texel edges would be most obvious.
 *   this cubemap -> everything else gets a genuine samplerCube of the same
 *                   environment, so reflections and ambient light agree
 *                   with the sky the player sees.
 *
 * WHY IT IS CHEAP
 *
 * A cubemap is six renders. Doing that every frame at this scene's budget
 * would be ruinous, so the probe:
 *   - renders ONLY the sky dome (renderList is exactly one mesh), never the
 *     whole scene, so each face is one draw call of a 24-segment sphere;
 *   - refreshes on demand rather than continuously, and only when the sky
 *     has actually changed - a new verse, or a fractal zoom step large
 *     enough to see. A static sky costs nothing at all after the first
 *     frame.
 *
 * FAILURE POLICY
 *
 * A probe is an enhancement. If the device cannot allocate the target, the
 * sky still renders and everything that would have sampled the cubemap
 * falls back to its untextured path. It must never take the frame down.
 */
import { ReflectionProbe } from '@babylonjs/core/Probes/reflectionProbe';
import type { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { BaseTexture } from '@babylonjs/core/Materials/Textures/baseTexture';
import { Vector3 } from '@babylonjs/core/Maths/math.vector';

/**
 * Face resolution.
 *
 * 256 is deliberate. The probe feeds reflections and ambient light, not the
 * lensed ring, and reflection detail is dominated by surface roughness long
 * before it is dominated by cubemap resolution. 256 costs 1.5 MB across six
 * faces; 1024 costs 24 MB for detail that gets blurred away on contact.
 */
export const PROBE_SIZE = 256;

/**
 * How much the fractal zoom must change before the cubemap is stale.
 *
 * The zoom is exponential, so a fixed absolute threshold would refresh
 * constantly when deep and never when shallow. A ratio is scale-free: the
 * sky is re-rendered when the view has magnified by this factor since the
 * last capture, which looks identical at every depth.
 */
export const ZOOM_REFRESH_RATIO = 1.06;

/** A description of the sky, used to decide whether a capture is stale. */
export interface ProbeKey {
  medium: string;
  symmetry: number;
  strangeness: number;
  tint: [number, number, number];
  zoom: number;
}

/**
 * Whether the sky has changed enough since the last capture to be worth
 * re-rendering six faces.
 *
 * Pure, so the refresh policy can be tested without a GPU.
 */
export function probeIsStale(
  last: ProbeKey | null, next: ProbeKey
): boolean {
  if (!last) return true;
  if (last.medium !== next.medium) return true;
  if (last.symmetry !== next.symmetry) return true;
  if (Math.abs(last.strangeness - next.strangeness) > 0.01) return true;
  for (let i = 0; i < 3; i++) {
    if (Math.abs(last.tint[i] - next.tint[i]) > 0.01) return true;
  }
  // Guard against a zero or negative zoom producing a nonsense ratio.
  const a = Math.max(1e-6, last.zoom);
  const b = Math.max(1e-6, next.zoom);
  const ratio = a > b ? a / b : b / a;
  if (!Number.isFinite(ratio)) return true;
  return ratio >= ZOOM_REFRESH_RATIO;
}

export class SkyProbe {
  private scene: Scene | null = null;
  private probe: ReflectionProbe | null = null;
  private lastKey: ProbeKey | null = null;
  private captures = 0;

  /** The live cubemap, or null if the device could not provide one. */
  get cubeTexture(): BaseTexture | null {
    return (this.probe?.cubeTexture as BaseTexture) ?? null;
  }

  /** How many times six faces have actually been rendered. */
  get captureCount(): number { return this.captures; }

  /**
   * Build the probe around a single sky mesh.
   *
   * Only that mesh is in the render list, so a capture cannot accidentally
   * become a full scene render - which is the usual way probes destroy a
   * frame budget.
   */
  attach(scene: Scene, skyMesh: Mesh | null): void {
    this.detach();
    if (!skyMesh) return;
    this.scene = scene;
    try {
      const probe = new ReflectionProbe('skyProbe', PROBE_SIZE, scene);
      // ONLY the sky. Never the scene.
      probe.renderList = [skyMesh];
      // Manual refresh: this.capture() drives it, not the engine.
      probe.refreshRate = 0;
      probe.position = Vector3.Zero();
      this.probe = probe;
      this.lastKey = null;
      // CAPTURE ONCE, IMMEDIATELY.
      // Without this the cubemap stays empty until the sky happens to
      // change, so every reflective surface mirrors black and the ambient
      // light contributes nothing - the scene looks unlit for no visible
      // reason. Measured: 0 captures through boot and 20 frames of a static
      // sky. One capture at attach costs six draw calls, once.
      this.captureNow();
    } catch (e) {
      // A probe is an enhancement; losing it must not lose the sky.
      console.warn('Sky cubemap unavailable:', e);
      this.probe = null;
    }
  }

  /** Render the six faces now, regardless of staleness. */
  private captureNow(): boolean {
    const probe = this.probe;
    if (!probe) return false;
    try {
      (probe.cubeTexture as any).refreshRate = 1;
      (probe.cubeTexture as any).render?.();
      (probe.cubeTexture as any).refreshRate = 0;
    } catch {
      return false;
    }
    this.captures++;
    return true;
  }

  /**
   * Re-render the six faces if the sky has changed.
   *
   * Returns true when a capture actually happened, so callers and tests can
   * see that a static sky is free.
   */
  refresh(key: ProbeKey, force = false): boolean {
    const probe = this.probe;
    if (!probe) return false;
    if (!force && !probeIsStale(this.lastKey, key)) return false;
    // A failed capture leaves the previous cubemap in place, which is a
    // slightly stale sky rather than a black one.
    if (!this.captureNow()) return false;
    this.lastKey = {
      medium: key.medium,
      symmetry: key.symmetry,
      strangeness: key.strangeness,
      tint: [key.tint[0], key.tint[1], key.tint[2]],
      zoom: key.zoom
    };
    return true;
  }

  /** Keep the cubemap centred on the viewer. */
  setCenter(p: Vector3): void {
    if (this.probe) this.probe.position.copyFrom(p);
  }

  stats(): Record<string, string> {
    return {
      'Sky cubemap': this.probe ? PROBE_SIZE + 'px' : 'off',
      'Sky captures': String(this.captures)
    };
  }

  detach(): void {
    try { this.probe?.dispose(); } catch { /* already gone */ }
    this.probe = null;
    this.scene = null;
    this.lastKey = null;
  }

  dispose(): void { this.detach(); }
}
