/**
 * PostFX — the shared post-processing chain applied to every world.
 *
 * The world shaders already tonemap and gamma-correct their own output, so
 * the pipeline's tone mapping stays off by default; enabling it here would
 * double-process the image. Bloom, FXAA, grain, vignette and chromatic
 * aberration all operate happily on that LDR result.
 */

import { DefaultRenderingPipeline } from '@babylonjs/core/PostProcesses/RenderPipeline/Pipelines/defaultRenderingPipeline';
import { ImageProcessingConfiguration } from '@babylonjs/core/Materials/imageProcessingConfiguration';
import '@babylonjs/core/PostProcesses/RenderPipeline/postProcessRenderPipelineManagerSceneComponent';
import type { Scene } from '@babylonjs/core/scene';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import type { WorldParam } from './World';

export interface PostFXSettings {
  bloom: number;
  bloomThreshold: number;
  exposure: number;
  contrast: number;
  vignette: number;
  grain: number;
  sharpen: number;
  chromatic: number;
  fxaa: number;
}

export const DEFAULT_POSTFX: PostFXSettings = {
  bloom: 0.55,
  bloomThreshold: 0.62,
  exposure: 1.0,
  contrast: 1.06,
  vignette: 0.35,
  grain: 3.0,
  sharpen: 0.25,
  chromatic: 2.0,
  fxaa: 1
};

export const POSTFX_PARAMS: WorldParam[] = [
  { key: 'bloom', label: 'Bloom', min: 0, max: 2, step: 0.05, value: DEFAULT_POSTFX.bloom },
  { key: 'bloomThreshold', label: 'Bloom Threshold', min: 0, max: 1, step: 0.02, value: DEFAULT_POSTFX.bloomThreshold },
  { key: 'exposure', label: 'Exposure', min: 0.2, max: 2.5, step: 0.02, value: DEFAULT_POSTFX.exposure },
  { key: 'contrast', label: 'Contrast', min: 0.5, max: 2, step: 0.02, value: DEFAULT_POSTFX.contrast },
  { key: 'vignette', label: 'Vignette', min: 0, max: 1.5, step: 0.05, value: DEFAULT_POSTFX.vignette },
  { key: 'grain', label: 'Film Grain', min: 0, max: 20, step: 0.5, value: DEFAULT_POSTFX.grain },
  { key: 'sharpen', label: 'Sharpen', min: 0, max: 1, step: 0.02, value: DEFAULT_POSTFX.sharpen },
  { key: 'chromatic', label: 'Chromatic Aberration', min: 0, max: 20, step: 0.5, value: DEFAULT_POSTFX.chromatic },
  { key: 'fxaa', label: 'Anti-aliasing (FXAA)', min: 0, max: 1, step: 1, value: DEFAULT_POSTFX.fxaa }
];

export class PostFX {
  settings: PostFXSettings = { ...DEFAULT_POSTFX };
  private pipeline: DefaultRenderingPipeline | null = null;

  /** Rebuilt per world load, because world switching disposes scene resources. */
  attach(scene: Scene, camera: Camera): void {
    this.detach();
    try {
      this.pipeline = new DefaultRenderingPipeline('postfx', true, scene, [camera]);
      this.apply();
    } catch (e) {
      console.warn('Post-processing unavailable, rendering without it:', e);
      this.pipeline = null;
    }
  }

  detach(): void {
    this.pipeline?.dispose();
    this.pipeline = null;
  }

  set(key: string, value: number): void {
    (this.settings as any)[key] = value;
    this.apply();
  }

  private apply(): void {
    const p = this.pipeline;
    if (!p) return;
    const s = this.settings;

    p.bloomEnabled = s.bloom > 0.001;
    if (p.bloomEnabled) {
      p.bloomWeight = s.bloom;
      p.bloomThreshold = s.bloomThreshold;
      p.bloomKernel = 64;
      p.bloomScale = 0.5;
    }

    p.fxaaEnabled = s.fxaa > 0.5;

    p.sharpenEnabled = s.sharpen > 0.001;
    if (p.sharpenEnabled) {
      p.sharpen.edgeAmount = s.sharpen;
      p.sharpen.colorAmount = 1.0;
    }

    p.chromaticAberrationEnabled = s.chromatic > 0.001;
    if (p.chromaticAberrationEnabled) {
      p.chromaticAberration.aberrationAmount = s.chromatic;
      p.chromaticAberration.radialIntensity = 0.7;
    }

    p.grainEnabled = s.grain > 0.001;
    if (p.grainEnabled) {
      p.grain.intensity = s.grain;
      p.grain.animated = true;
    }

    p.imageProcessingEnabled = true;
    const ip = p.imageProcessing;
    if (ip) {
      // Worlds tonemap in-shader; doing it again here would crush highlights.
      ip.toneMappingEnabled = false;
      ip.exposure = s.exposure;
      ip.contrast = s.contrast;
      ip.vignetteEnabled = s.vignette > 0.001;
      ip.vignetteWeight = s.vignette * 4;
      ip.vignetteStretch = 0.4;
      ip.vignetteCameraFov = 1.2;
      ip.vignetteBlendMode = ImageProcessingConfiguration.VIGNETTEMODE_MULTIPLY;
    }
  }
}
