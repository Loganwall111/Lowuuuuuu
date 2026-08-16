/**
 * Engine — Babylon.js 9 boot layer.
 *
 * NOTE ON BACKEND: every world in this project is shaded with hand-written
 * GLSL (ray-marched geodesics, Gerstner surfaces, procedural planet shaders).
 * WebGPU cannot consume GLSL without shipping the twgsl transpiler, and the
 * struct-array + dFdx usage here does not survive that path cleanly. WebGL2
 * is therefore the deliberate target — it supports every feature these
 * shaders need (derivatives, float render targets, 320-iteration loops).
 */

import { Engine } from '@babylonjs/core/Engines/engine';
import type { AbstractEngine } from '@babylonjs/core/Engines/abstractEngine';

import '@babylonjs/core/Materials/standardMaterial';
import '@babylonjs/core/Rendering/depthRendererSceneComponent';
// Ray is a side-effect import in Babylon's tree-shaken build. Anything that
// picks - scene.pick, createPickingRay, camera.getForwardRay - throws without
// it, and a throw inside the render loop leaves a permanently black canvas.
import '@babylonjs/core/Culling/ray';
// Registers every post-process shader. Post-processing draws nothing at all
// without these, which presents as a completely black screen; see
// ShaderRegistry.ts for the full explanation.
import './ShaderRegistry';

export interface EngineBoot {
  engine: AbstractEngine;
  backend: string;
}

export async function createEngine(canvas: HTMLCanvasElement): Promise<EngineBoot> {
  const engine = new Engine(canvas, true, {
    preserveDrawingBuffer: true,
    stencil: true,
    powerPreference: 'high-performance',
    antialias: true,
    disableWebGL2Support: false,
    failIfMajorPerformanceCaveat: false
  });

  // Start at native CSS resolution. The previous inverse-DPR value silently
  // supersampled the full raymarched universe at 1.5-2x during launch, which
  // is why the menu was smooth but gameplay collapsed to 2 fps.
  engine.setHardwareScalingLevel(1);

  const gl = engine._gl as WebGL2RenderingContext | undefined;
  const ver = engine.webGLVersion === 2 ? 'WebGL2' : 'WebGL1';
  let label = ver;
  try {
    const dbg = gl?.getExtension('WEBGL_debug_renderer_info');
    if (dbg && gl) {
      const r = gl.getParameter(dbg.UNMASKED_RENDERER_WEBGL) as string;
      if (r) label = `${ver} · ${r.replace(/\s*\(.*?\)\s*/g, ' ').trim().slice(0, 22)}`;
    }
  } catch { /* renderer string is optional */ }

  return { engine: engine as unknown as AbstractEngine, backend: label };
}
