/**
 * shaderstore-check — the actual cause of the black screen.
 *
 * Babylon's ES build is tree-shaken, and post-process shaders are registered
 * into the global ShaderStore purely as a side effect of importing their own
 * module. Importing PostProcess, or DefaultRenderingPipeline, registers
 * nothing. A post-process with no shader never becomes ready, contributes
 * nothing to the frame, and the screen is black at full frame rate with a
 * perfectly healthy scene behind it.
 *
 * The headless suite could never catch this: jsdom's WebGL stub reports
 * every compile and link as successful. So this check does not mock
 * anything - it imports the real modules and inspects the real store.
 */

import { build } from 'esbuild';
import { readFileSync, writeFileSync, mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

let pass = 0, fail = 0;
const ok = (name, cond, detail) => {
  if (cond) { pass++; console.log('  PASS  ' + name); }
  else { fail++; console.log('  FAIL  ' + name + (detail ? '  -> ' + detail : '')); }
};

console.log('\nshader store: post-processing must have its shaders');

const dir = mkdtempSync(join(tmpdir(), 'ss-'));
const bundleAndRun = async (tsSource, name) => {
  const entry = join('tools', '_ss_' + name + '.ts');
  writeFileSync(entry, tsSource);
  try {
    const out = await build({
      entryPoints: [entry], bundle: true, format: 'esm',
      write: false, logLevel: 'error', platform: 'browser'
    });
    const f = join(dir, name + '-' + Date.now() + '.mjs');
    writeFileSync(f, out.outputFiles[0].text);
    return await import(f);
  } finally {
    rmSync(entry, { force: true });
  }
};

/* ------- 1. the failure this bug was: imports alone register nothing ---- */

{
  const M = await bundleAndRun(`
    import '@babylonjs/core/PostProcesses/postProcess';
    import '@babylonjs/core/Materials/effect';
    import '@babylonjs/core/PostProcesses/RenderPipeline/Pipelines/defaultRenderingPipeline';
    import { ShaderStore } from '@babylonjs/core/Engines/shaderStore';
    export const count = Object.keys(ShaderStore.ShadersStore).length;
    export const hasVertex = !!(ShaderStore.ShadersStore as any)['postprocessVertexShader'];
  `, 'bare');

  // This is the regression itself, asserted as fact: importing the classes
  // does NOT bring their shaders. If Babylon ever changes this, the test
  // will tell us the workaround is no longer needed.
  ok('importing PostProcess and the pipeline registers no shaders by itself',
     M.count === 0, 'registered ' + M.count);
  ok('the shared post-process vertex shader is absent without an explicit import',
     M.hasVertex === false);
}

/* ------- 2. the registry fixes it ---------------------------------------- */

{
  const M = await bundleAndRun(`
    import { missingShaders, postProcessShadersReady, REQUIRED_SHADERS } from '../src/bjs/ShaderRegistry';
    import { ShaderStore } from '@babylonjs/core/Engines/shaderStore';
    export const missing = missingShaders();
    export const ready = postProcessShadersReady();
    export const required = REQUIRED_SHADERS;
    export const total = Object.keys(ShaderStore.ShadersStore).length;
    export const vertexSource = (ShaderStore.ShadersStore as any)['postprocessVertexShader'] || '';
  `, 'registry');

  ok('the registry leaves nothing missing', M.missing.length === 0, M.missing.join(','));
  ok('the registry reports itself ready', M.ready === true);
  ok('it registers a substantial set of shaders', M.total >= 12, 'total ' + M.total);
  ok('every required shader is named', M.required.length >= 12);
  ok('the vertex shader has real GLSL in it',
     /gl_Position/.test(M.vertexSource), M.vertexSource.slice(0, 40));
  ok('the vertex shader declares the varying the fragment stages read',
     /vUV/.test(M.vertexSource));
}

/* ------- 3. the app's own import graph is fixed, not just the registry --- */

{
  const M = await bundleAndRun(`
    import '../src/bjs/Engine';
    import { ShaderStore } from '@babylonjs/core/Engines/shaderStore';
    const need = ['postprocessVertexShader','passPixelShader','fxaaPixelShader',
      'bloomMergePixelShader','imageProcessingPixelShader','kernelBlurVertexShader',
      'extractHighlightsPixelShader','grainPixelShader','sharpenPixelShader',
      'chromaticAberrationPixelShader','kernelBlurPixelShader'];
    export const missing = need.filter(n => !(ShaderStore.ShadersStore as any)[n]);
    export const total = Object.keys(ShaderStore.ShadersStore).length;
  `, 'engine');

  // This is the assertion that actually protects the user: booting the
  // engine must be enough.
  ok('booting the engine registers every post-process shader',
     M.missing.length === 0, 'missing ' + M.missing.join(','));
  ok('the store is populated through the real import graph',
     M.total >= 12, 'total ' + M.total);
}

/* ------- 4. the lens pass specifically --------------------------------- */

{
  const M = await bundleAndRun(`
    import '../src/bjs/systems/LensFX';
    import { ShaderStore } from '@babylonjs/core/Engines/shaderStore';
    export const hasVertex = !!(ShaderStore.ShadersStore as any)['postprocessVertexShader'];
  `, 'lens');

  // The lens is attached in every world, including the white garage, so if
  // it cannot draw then nothing can.
  ok('importing LensFX alone brings the vertex shader it needs',
     M.hasVertex === true);
}

/* ------- 5. the source keeps the imports that make it work -------------- */

const registry = readFileSync(new URL('../src/bjs/ShaderRegistry.ts', import.meta.url), 'utf8');
const engine = readFileSync(new URL('../src/bjs/Engine.ts', import.meta.url), 'utf8');
const lens = readFileSync(new URL('../src/bjs/systems/LensFX.ts', import.meta.url), 'utf8');
const app = readFileSync(new URL('../src/bjs/App.ts', import.meta.url), 'utf8');

ok('the engine pulls in the registry', /import '\.\/ShaderRegistry'/.test(engine));
ok('LensFX imports its own vertex shader defensively',
   /Shaders\/postprocess\.vertex/.test(lens));
ok('the registry explains why the imports must not be removed',
   /load-bearing/i.test(registry) && /black/i.test(registry));
ok('the bloom chain is registered',
   /extractHighlights/.test(registry) && /kernelBlur/.test(registry) && /bloomMerge/.test(registry));
ok('image processing is registered so exposure and vignette work',
   /imageProcessing\.fragment/.test(registry));
ok('anti-aliasing is registered', /fxaa\.fragment/.test(registry) && /fxaa\.vertex/.test(registry));

/* ------- 6. the app refuses to attach a pipeline that cannot draw ------- */

ok('the app checks for missing shaders at boot', /missingShaders\(\)/.test(app));
ok('post-processing is skipped rather than drawn black',
   /absent\.length/.test(app));
ok('the failure names the missing shaders', /Missing: ' \+ absent\.join/.test(app));
ok('attachment happens only when the shaders are present',
   /} else \{[\s\S]*?this\.postfx\.attach/.test(app));

rmSync(dir, { recursive: true, force: true });
console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
