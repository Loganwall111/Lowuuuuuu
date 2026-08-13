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
  // Now that the pipeline genuinely runs in HDR (its shaders were never
  // registered before, so none of this was actually reaching the screen),
  // the grade can be pushed properly: a wide soft bloom for star glare and
  // lit planet limbs, with enough exposure headroom for highlights to
  // bloom rather than clip.
  bloom: 1.25,
  bloomThreshold: 0.30,
  // Neutral. Every world shader already tonemaps and gamma-encodes its own
  // output (see the note at the top of this file), so the pipeline must not
  // grade on top of that. An exposure of 1.32 here was multiplying an
  // already-displayable image and then ACES-tonemapping it a second time,
  // which lifted dark tones by up to 190% and made planets glare white.
  exposure: 1.0,
  contrast: 1.05,
  vignette: 0.20,
  grain: 0.6,
  sharpen: 0.42,
  chromatic: 1.0,
  fxaa: 1,
  bloomKernel: 112,
  bloomScale: 0.85
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
  /** Whether the pipeline is running in floating-point mode. */
  hdr = false;

  /** Rebuilt per world load, because world switching disposes scene resources. */
  attach(scene: Scene, camera: Camera): void {
    this.detach();
    try {
      // HDR here means "render the whole frame into a floating-point
      // texture". That is the right choice for bloom, but it is only valid
      // if the GPU can actually render to and filter half-float textures.
      // Where it cannot, Babylon still builds the pipeline and still reports
      // a healthy scene - it just resolves to an entirely black image, at
      // full frame rate, with every mesh present. That is precisely the
      // black screen that has been reported: drawing, 93 meshes, 46 fps,
      // mean luminance 0.0000.
      //
      // So HDR is now conditional on the capability actually being there.
      const caps = scene.getEngine().getCaps();
      const hdr = !!(caps.textureHalfFloatRender && caps.textureHalfFloatLinearFiltering) ||
                  !!(caps.textureFloatRender && caps.textureFloatLinearFiltering);
      if (!hdr) {
        console.warn('This GPU cannot render to float textures - using an LDR pipeline so the frame is not black.');
      }
      this.hdr = hdr;
      this.pipeline = new DefaultRenderingPipeline('postfx', hdr, scene, [camera]);
      this.apply();
      this.watchPipelineHealth(scene);
    } catch (e) {
      console.warn('Post-processing unavailable, rendering without it:', e);
      this.pipeline = null;
    }
  }

  /**
   * The entire frame is blitted through this pipeline. If one of its shaders
   * will not compile on the user's GPU, the result is not "no bloom" - it is
   * a black screen. jsdom compiles every shader successfully, so this class
   * of failure is invisible to the test suite and has to be caught live.
   *
   * If the pipeline is still not ready after ~3 seconds of real frames, tear
   * it down. An unfiltered image is infinitely better than no image.
   */
  private watchPipelineHealth(scene: Scene): void {
    let frames = 0;
    const observer = scene.onAfterRenderObservable.add(() => {
      const p = this.pipeline;
      if (!p) { scene.onAfterRenderObservable.remove(observer); return; }
      // isSupported is the pipeline's own verdict on whether its effects
      // could be built for this engine.
      let ready = true;
      try { ready = p.isSupported; } catch { ready = false; }
      if (ready) { scene.onAfterRenderObservable.remove(observer); return; }
      if (++frames > 180) {
        console.warn('Post-processing pipeline never became ready on this GPU - removing it so the scene is visible.');
        this.detach();
        scene.onAfterRenderObservable.remove(observer);
      }
    });
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
      // ACES filmic tone mapping. This is the single biggest contributor to
      // a scene looking photographed rather than rendered: it rolls bright
      // values off into white instead of clipping them, so a star's core and
      // a planet's lit edge keep their shape. Only safe to enable now that
      // the pipeline really is HDR - in LDR it would crush the image.
      // DOUBLE TONE MAPPING WAS THE PLANET BRIGHTNESS BUG.
      // PlanetShader, PortalShader and BlackHoleWorld all end with the ACES
      // curve followed by pow(1/2.2) - they emit finished, gamma-encoded
      // colour. Running ACES again here re-tonemapped an already-tonemapped
      // image: measured inflation was +190% at 0.05 luminance and +107% at
      // 0.10, so shadows and midtones were pushed toward white and every
      // planet read as blown out.
      //
      // The scene's own shaders are the single source of truth for tone
      // mapping, so the pipeline stays photometrically neutral.
      ip.toneMappingEnabled = false;
      ip.toneMappingType = ImageProcessingConfiguration.TONEMAPPING_ACES;
      ip.exposure = s.exposure;
      ip.contrast = s.contrast;
      // Vignette darkens toward the frame edge. In MULTIPLY mode the weight
      // is a direct multiplier, so the old `weight * 4` (1.68 by default)
      // drove the edges to black and, combined with a bogus 1.2 rad
      // vignetteCameraFov, could swallow most of the picture. Keep it as a
      // subtle edge falloff that can never black out the view.
      ip.vignetteEnabled = s.vignette > 0.001;
      ip.vignetteWeight = Math.min(1.5, Math.max(0, s.vignette));
      ip.vignetteStretch = 0;
      ip.vignetteBlendMode = ImageProcessingConfiguration.VIGNETTEMODE_MULTIPLY;
    });
  }
}
