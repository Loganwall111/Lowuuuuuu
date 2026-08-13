/**
 * VerseRenderer — draws whichever verse you are standing in.
 *
 * Each verse declares what it is made of (OuterVerses.medium) and this
 * builds from that declaration. Adding a verse is a table entry, not a new
 * renderer, which is the only reason nine of them is maintainable.
 *
 * Everything is one PointsCloudSystem per verse: ten thousand objects for a
 * single draw call. The shapes differ because the *distribution* differs,
 * not because there are different meshes - a square verse is points on a
 * cubic lattice, the Mandelbrot is points sampled from the escape-time set,
 * the edge of reality is points along a single line.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color4 } from '@babylonjs/core/Maths/math.color';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import type { Verse } from './OuterVerses';

/** Deterministic stream, so a verse looks the same every visit. */
function stream(seed: number): () => number {
  let a = (seed >>> 0) || 1;
  return () => {
    a = (a + 0x6D2B79F5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/**
 * Whether a complex point is in the Mandelbrot set, and how fast it escaped.
 *
 * Returns 0 for points inside the set and a normalised escape time outside,
 * which is what gives the fractal its banding. Standard escape-time
 * iteration - the set is defined by z -> z^2 + c staying bounded.
 */
export function mandelbrotEscape(cx: number, cy: number, maxIter = 64): number {
  let x = 0, y = 0, i = 0;
  while (x * x + y * y <= 4 && i < maxIter) {
    const xt = x * x - y * y + cx;
    y = 2 * x * y + cy;
    x = xt;
    i++;
  }
  return i >= maxIter ? 0 : i / maxIter;
}

export interface VersePoint {
  position: Vector3;
  color: Color4;
}

/**
 * Generates the points for a verse.
 *
 * Pure and testable: the shape of each verse can be asserted without a GPU,
 * which matters because "the squareverse is actually square" is exactly the
 * sort of thing that silently regresses.
 */
export function versePoints(verse: Verse, count: number, span: number, seed = 1): VersePoint[] {
  const r = stream(seed + verse.depth);
  const out: VersePoint[] = [];
  const n = Math.max(0, Math.floor(count));
  const [tr, tg, tb] = verse.tint;

  for (let i = 0; i < n; i++) {
    let p: Vector3;
    let bright = 0.5 + r() * 0.5;

    switch (verse.medium) {
      case 'geometry': {
        // A lattice with the verse's symmetry: points snap to a grid and
        // are arranged in rings of `symmetry` around the axis, so a
        // squareverse reads as four-fold and an octagonverse as eight.
        const sym = Math.max(1, verse.symmetry || 4);
        const step = span / 9;
        const ring = Math.floor(r() * 9) + 1;
        const arm = Math.floor(r() * sym);
        const ang = (arm / sym) * Math.PI * 2;
        const rad = ring * step;
        p = new Vector3(
          Math.round(Math.cos(ang) * rad / step) * step,
          Math.round((r() * 2 - 1) * span / step) * step,
          Math.round(Math.sin(ang) * rad / step) * step
        );
        break;
      }
      case 'fractal': {
        // Points sampled from the set itself, so flying "into" it really is
        // flying into the Mandelbrot rather than a picture of it.
        const cx = r() * 3.2 - 2.2;
        const cy = r() * 2.4 - 1.2;
        const esc = mandelbrotEscape(cx, cy, 64);
        // Keep the boundary, which is where all the structure lives.
        if (esc === 0 || esc > 0.55) { continue; }
        p = new Vector3(cx * span * 0.42, (r() * 2 - 1) * span * 0.06, cy * span * 0.42);
        bright = 0.35 + esc * 1.3;
        break;
      }
      case 'string': {
        // The entire universe seen side-on: everything collapsed onto one
        // line, with just enough scatter to show it has thickness.
        const t = r() * 2 - 1;
        p = new Vector3(
          t * span,
          (r() - 0.5) * span * 0.004,
          (r() - 0.5) * span * 0.004
        );
        bright = 0.6 + Math.cos(t * Math.PI * 0.5) * 0.5;
        break;
      }
      case 'code': {
        // Dense vertical runs, like text scrolling in columns.
        const col = Math.floor(r() * 40) - 20;
        const row = Math.floor(r() * 60) - 30;
        p = new Vector3(col * (span / 22), row * (span / 34), (r() * 2 - 1) * span * 0.7);
        bright = r() < 0.14 ? 1.5 : 0.4;
        break;
      }
      case 'technology': {
        // Slabs and towers on a coarse grid: machinery, not stars.
        const g = span / 7;
        p = new Vector3(
          (Math.floor(r() * 15) - 7) * g,
          (Math.floor(r() * 7) - 3) * g * 0.6,
          (Math.floor(r() * 15) - 7) * g
        );
        bright = 0.45 + r() * 0.9;
        break;
      }
      case 'void': {
        // The infinite cube of stars: a hard-edged cubic volume, and it
        // flashes - the strangeness has nowhere left to go.
        const g = span / 11;
        p = new Vector3(
          Math.round((r() * 2 - 1) * 11) * g,
          Math.round((r() * 2 - 1) * 11) * g,
          Math.round((r() * 2 - 1) * 11) * g
        );
        bright = r() < 0.2 ? 2.2 : 0.35;
        break;
      }
      default: {
        // Ordinary space.
        p = new Vector3(
          (r() * 2 - 1) * span,
          (r() * 2 - 1) * span * 0.4,
          (r() * 2 - 1) * span
        );
        break;
      }
    }

    // Strangeness pushes colour away from the tint toward something wrong.
    const s = verse.strangeness;
    out.push({
      position: p,
      color: new Color4(
        Math.min(1, (tr + bright * 0.6) * (1 + s * r() * 0.8)),
        Math.min(1, (tg + bright * 0.55) * (1 - s * 0.25 + r() * s * 0.5)),
        Math.min(1, (tb + bright * 0.7) * (1 + s * r() * 0.4)),
        1
      )
    });
  }
  return out;
}

/** Draws a verse as a single points cloud. */
export class VerseRenderer {
  private scene: Scene | null = null;
  private pcs: PointsCloudSystem | null = null;
  private mesh: Mesh | null = null;
  private builtId: string | null = null;
  private serial = 0;
  /** Points currently drawn. */
  count = 0;

  attach(scene: Scene): void { this.scene = scene; }

  /** Which verse is currently drawn, if any. */
  get current(): string | null { return this.builtId; }

  /**
   * Builds a verse. Skips the work if that verse is already drawn, so this
   * is safe to call every frame.
   */
  show(verse: Verse, span = 2400, seed = 1): boolean {
    if (!this.scene) return false;
    if (this.builtId === verse.id) return false;

    this.clear();
    const pts = versePoints(verse, Math.floor(700 * verse.density), span, seed);
    if (!pts.length) { this.builtId = verse.id; return true; }

    const pcs = new PointsCloudSystem('verse' + (++this.serial), 2, this.scene);
    pcs.addPoints(pts.length, (particle: any, i: number) => {
      particle.position = pts[i].position;
      particle.color = pts[i].color;
    });
    void pcs.buildMeshAsync().then((mesh) => {
      this.mesh = mesh;
      if (!mesh) return;
      mesh.isPickable = false;
      mesh.applyFog = false;
      mesh.alwaysSelectAsActiveMesh = true;
      mesh.renderingGroupId = 0;
      // The sky must never occlude anything. Points were writing opaque
      // depth at their shell radius, so real geometry behind that shell was
      // depth-culled and punched out as black patches - and because the
      // points are rebuilt as the camera moves, the holes swarmed with
      // mouse movement. Depth-write off makes the sky a pure backdrop.
      const m = mesh.material as any;
      if (m) {
        m.disableLighting = true;
        m.pointSize = 3;
        // Depth-write off is the actual fix for the black patches.
        m.disableDepthWrite = true;
        m.forceDepthWrite = false;
        m.needDepthPrePass = false;
      }
    });

    this.pcs = pcs;
    this.count = pts.length;
    this.builtId = verse.id;
    return true;
  }

  clear(): void {
    try { this.mesh?.dispose(); } catch { /* gone */ }
    try { this.pcs?.dispose(); } catch { /* gone */ }
    this.mesh = null;
    this.pcs = null;
    this.count = 0;
    this.builtId = null;
  }

  dispose(): void {
    this.clear();
    this.scene = null;
  }
}
