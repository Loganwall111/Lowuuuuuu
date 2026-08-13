/**
 * ShaderRegistry — the imports that make post-processing actually draw.
 *
 * THIS FILE IS THE FIX FOR THE BLACK SCREEN. Please do not "tidy" the
 * imports below: they look unused, and every one of them is load-bearing.
 *
 * Babylon's ES build is tree-shaken. Post-process shaders are not bundled
 * with the classes that use them; each one is registered into the global
 * ShaderStore as a side effect of importing its own module. Importing
 * PostProcess, or even DefaultRenderingPipeline, registers *nothing*:
 *
 *     import '@babylonjs/core/PostProcesses/postProcess';
 *     Object.keys(ShaderStore.ShadersStore).length  // -> 0
 *
 * A post-process whose shaders are absent never becomes ready. It stays in
 * the camera's chain and contributes nothing, so the frame it was meant to
 * present is never presented. The result on screen is total black - at full
 * frame rate, with a healthy scene, every mesh enabled and visible, and a
 * white clear colour. Exactly the report we kept getting: "the canvas is
 * drawing (30 fps, 93 meshes) but every sampled pixel is black".
 *
 * This is invisible to a headless test suite. jsdom's WebGL stub reports
 * every shader compile and program link as successful, so the missing
 * source is never noticed and the pipeline appears perfectly healthy.
 *
 * Every shader below corresponds to something this project switches on:
 * the shared full-screen vertex shader, the pass-through blit, and the
 * bloom / blur / FXAA / tone-mapping / sharpen / chromatic / grain stages
 * of DefaultRenderingPipeline.
 */

// The full-screen quad every single post-process is drawn with. Without
// this one, nothing post-processed can ever appear.
import '@babylonjs/core/Shaders/postprocess.vertex';

// Straight blit, used to copy between render targets.
import '@babylonjs/core/Shaders/pass.fragment';

// Bloom: highlights are extracted, blurred separably, then merged back.
import '@babylonjs/core/Shaders/extractHighlights.fragment';
import '@babylonjs/core/Shaders/kernelBlur.fragment';
import '@babylonjs/core/Shaders/kernelBlur.vertex';
import '@babylonjs/core/Shaders/bloomMerge.fragment';

// Anti-aliasing.
import '@babylonjs/core/Shaders/fxaa.fragment';
import '@babylonjs/core/Shaders/fxaa.vertex';

// Exposure, contrast and the vignette all live in image processing.
import '@babylonjs/core/Shaders/imageProcessing.fragment';

// The remaining stages the settings panel can enable.
import '@babylonjs/core/Shaders/sharpen.fragment';
import '@babylonjs/core/Shaders/chromaticAberration.fragment';
import '@babylonjs/core/Shaders/grain.fragment';

// Depth of field is part of DefaultRenderingPipeline's graph even when the
// effect is disabled, so its shaders must resolve too.
import '@babylonjs/core/Shaders/circleOfConfusion.fragment';
import '@babylonjs/core/Shaders/depthOfField.fragment';

import { ShaderStore } from '@babylonjs/core/Engines/shaderStore';

/** Shaders that must exist before any post-process can draw. */
export const REQUIRED_SHADERS = [
  'postprocessVertexShader',
  'passPixelShader',
  'extractHighlightsPixelShader',
  'kernelBlurPixelShader',
  'kernelBlurVertexShader',
  'bloomMergePixelShader',
  'fxaaPixelShader',
  'fxaaVertexShader',
  'imageProcessingPixelShader',
  'sharpenPixelShader',
  'chromaticAberrationPixelShader',
  'grainPixelShader'
];

/**
 * Which required shaders are missing from the store.
 *
 * Exported so the app can check at boot and say something useful instead of
 * presenting a black rectangle, and so the test suite can assert the
 * imports above have not been removed by a well-meaning cleanup.
 */
export function missingShaders(): string[] {
  const store = ShaderStore.ShadersStore as Record<string, string>;
  return REQUIRED_SHADERS.filter((n) => !store[n]);
}

/** True when every post-process shader this project needs is registered. */
export function postProcessShadersReady(): boolean {
  return missingShaders().length === 0;
}
