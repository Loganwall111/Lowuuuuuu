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
  /** Blur width of the bloom, in pixels. Wide = soft cinematic glare. */
  bloomKernel: number;
  /** Resolution the bloom is computed at. Higher = smoother falloff. */
  bloomScale: number;
}

export const DEFAULT_POSTFX: PostFXSettings = {
  // A cinematic default. Bloom is wide and soft rather than a tight halo,
  // which is what sells a star's glare and a planet's lit limb.
  bloom: 0.95,
  bloomThreshold: 0.42,
  exposure: 1.18,
  contrast: 1.14,
  vignette: 0.42,
  grain: 2.0,
  sharpen: 0.35,
  chromatic: 3.0,
  fxaa: 1,
  bloomKernel: 96,
  bloomScale: 0.75
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
  { key: 'bloomKernel', label: 'Glare Width', min: 8, max: 192, step: 4, value: DEFAULT_POSTFX.bloomKernel },
  { key: 'bloomScale', label: 'Glare Quality', min: 0.25, max: 1, step: 0.05, value: DEFAULT_POSTFX.bloomScale },
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
    // Each stage is guarded: a driver quirk in one effect must not disable
    // the entire pipeline (FXAA in particular can throw on odd driver info).
    const guard = (name: string, fn: () => void) => {
      try { fn(); } catch (e) { console.warn(`post-fx "${name}" unavailable:`, e); }
    };

    guard('bloom', () => {
      p.bloomEnabled = s.bloom > 0.001;
      if (p.bloomEnabled) {
        p.bloomWeight = s.bloom;
        p.bloomThreshold = s.bloomThreshold;
        p.bloomKernel = s.bloomKernel;
        p.bloomScale = s.bloomScale;
      }
    });

    guard('fxaa', () => { p.fxaaEnabled = s.fxaa > 0.5; });

    guard('sharpen', () => {
      p.sharpenEnabled = s.sharpen > 0.001;
      if (p.sharpenEnabled) {
        p.sharpen.edgeAmount = s.sharpen;
        p.sharpen.colorAmount = 1.0;
      }
    });

    guard('chromatic', () => {
      p.chromaticAberrationEnabled = s.chromatic > 0.001;
      if (p.chromaticAberrationEnabled) {
        p.chromaticAberration.aberrationAmount = s.chromatic;
        p.chromaticAberration.radialIntensity = 0.7;
      }
    });

    guard('grain', () => {
      p.grainEnabled = s.grain > 0.001;
      if (p.grainEnabled) {
        p.grain.intensity = s.grain;
        p.grain.animated = true;
      }
    });

    p.imageProcessingEnabled = true;
    const ip = p.imageProcessing;
    if (ip) guard('imageProcessing', () => {
      // Worlds tonemap in-shader; doing it again here would crush highlights.
      ip.toneMappingEnabled = false;
      ip.exposure = s.exposure;
      ip.contrast = s.contrast;
      ip.vignetteEnabled = s.vignette > 0.001;
      ip.vignetteWeight = s.vignette * 4;
      ip.vignetteStretch = 0.4;
      ip.vignetteCameraFov = 1.2;
      ip.vignetteBlendMode = ImageProcessingConfiguration.VIGNETTEMODE_MULTIPLY;
    });
  }
}
