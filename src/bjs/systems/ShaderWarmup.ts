/**
 * ShaderWarmup - compile the expensive shaders before they are needed.
 *
 * A ShaderMaterial does not compile when it is created; it compiles the
 * first time something using it is drawn. For a small material that is
 * invisible. For the black-hole raymarcher - 160 RK2 integration steps
 * plus a full procedural sky inlined into the same fragment program - it
 * is the single most expensive compile in the app, and it happens at the
 * exact moment a hole first enters frame. WebGL blocks the calling thread
 * while a program links, so that shows up as a hitch, and any material
 * asked to draw before its program is ready falls back to a default that
 * can render as flat magenta.
 *
 * Compiling during the loading screen costs the same milliseconds, but
 * spends them where there is nothing to interrupt.
 *
 * WHY THIS IS BEST-EFFORT
 *
 * Warmup is an optimisation, never a requirement. Every failure path here
 * resolves rather than rejects: a driver that refuses to precompile should
 * cost a stutter, not a black screen.
 */
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import type { Scene } from '@babylonjs/core/scene';
import type { Material } from '@babylonjs/core/Materials/material';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';

/** Result of a warmup pass, for logging and tests. */
export interface WarmupReport {
  /** Names of materials that reached a ready state. */
  compiled: string[];
  /** Names that did not, with the reason. */
  failed: { name: string; reason: string }[];
  /** Wall-clock milliseconds spent. */
  ms: number;
}

/** How long to wait for a single program to link before moving on. */
export const WARMUP_TIMEOUT_MS = 4000;

/**
 * Force one material to compile against a throwaway mesh.
 *
 * Resolves true if the material reported ready. Never rejects.
 */
export function warmMaterial(
  scene: Scene, material: Material, timeout = WARMUP_TIMEOUT_MS
): Promise<boolean> {
  return new Promise((resolve) => {
    let done = false;
    let probe: Mesh | null = null;
    const finish = (okFlag: boolean) => {
      if (done) return;
      done = true;
      try { probe?.dispose(); } catch { /* already gone */ }
      resolve(okFlag);
    };
    // A driver that never calls back must not hang the loading screen.
    const timer = setTimeout(() => finish(false), Math.max(1, timeout));
    try {
      // A one-triangle mesh is enough to establish the vertex layout the
      // program will be linked against.
      probe = MeshBuilder.CreatePlane('__warmup', { size: 0.01 }, scene);
      probe.isVisible = false;
      probe.isPickable = false;
      probe.material = material;
      material.forceCompilation(probe, () => {
        clearTimeout(timer);
        finish(true);
      }, undefined, (reason: string) => {
        clearTimeout(timer);
        // Record and carry on: a shader that fails to precompile will
        // simply compile later, or fall back, but the app keeps running.
        console.warn('Shader warmup failed for ' + material.name + ':', reason);
        finish(false);
      });
    } catch (e) {
      clearTimeout(timer);
      console.warn('Shader warmup threw for ' + material.name + ':', e);
      finish(false);
    }
  });
}

/**
 * Warm every material in the scene whose name matches one of the given
 * prefixes.
 *
 * Prefix matching rather than a hard-coded list, so a material that gets
 * renamed does not silently stop being warmed.
 */
export async function warmupShaders(
  scene: Scene | null,
  prefixes: string[] = ['holeField', 'm_', 'coronaM', 'glareM', 'cosmicSkyM'],
  timeout = WARMUP_TIMEOUT_MS
): Promise<WarmupReport> {
  const started = Date.now();
  const report: WarmupReport = { compiled: [], failed: [], ms: 0 };
  if (!scene) {
    report.ms = Date.now() - started;
    return report;
  }
  let mats: Material[] = [];
  try {
    mats = (scene.materials ?? []).filter((m) =>
      !!m && typeof m.name === 'string' &&
      prefixes.some((p) => m.name.startsWith(p)));
  } catch {
    mats = [];
  }

  // Warm in parallel, not one after another.
  //
  // Sequential warmup multiplies the timeout by the number of materials:
  // measured on a headless engine that never links, six planet materials
  // at a 4-second timeout each held the loading screen for 24 seconds.
  // The GPU serialises the real work anyway, so issuing them together
  // costs nothing and bounds the total wait at ONE timeout.
  const results = await Promise.all(mats.map(async (m) => {
    try {
      return { name: m.name, okFlag: await warmMaterial(scene, m, timeout) };
    } catch {
      return { name: m.name, okFlag: false };
    }
  }));
  for (const r of results) {
    if (r.okFlag) report.compiled.push(r.name);
    else report.failed.push({ name: r.name, reason: 'not ready' });
  }
  report.ms = Date.now() - started;
  return report;
}
