/** Full Babylon.js engine bootstrap: WebGPU first, WebGL2 fail-safe. */
import { Engine } from '@babylonjs/core/Engines/engine';
import type { AbstractEngine } from '@babylonjs/core/Engines/abstractEngine';

import '@babylonjs/core/Materials/standardMaterial';
import '@babylonjs/core/Rendering/depthRendererSceneComponent';
import '@babylonjs/core/Culling/ray';
import './ShaderRegistry';

export interface EngineBoot { engine: AbstractEngine; backend: string; webgpu: boolean; }

async function tryWebGPU(canvas:HTMLCanvasElement):Promise<EngineBoot|null>{
  // WebGPU remains available as an explicit Next Generation preview while
  // the hand-written GLSL material library is certified one pipeline at a
  // time. Shipping it unconditionally caused invalid D3D bind groups and a
  // black frame on otherwise capable Windows GPUs.
  let requested=false;
  try{requested=new URLSearchParams(location.search).get('webgpu')==='1'||localStorage.getItem('low-webgpu-optin')==='1';}catch{}
  if(!requested)return null;
  try{if(sessionStorage.getItem('low-force-webgl')==='1')return null;}catch{}
  if(!(globalThis.navigator as any)?.gpu)return null;
  try{
    const {WebGPUEngine}=await import('@babylonjs/core/Engines/webgpuEngine');
    const supported=await Promise.race([
      WebGPUEngine.IsSupportedAsync,
      new Promise<boolean>((resolve)=>setTimeout(()=>resolve(false),900))
    ]);
    if(!supported)return null;
    const create=WebGPUEngine.CreateAsync(canvas,{
      antialias:true,adaptToDeviceRatio:false,powerPreference:'high-performance',
      enableAllFeatures:false,enableGPUDebugMarkers:false
    });
    const result=await Promise.race([
      create.then((engine)=>({engine,timedOut:false as const})),
      new Promise<{engine:null;timedOut:true}>((resolve)=>
        setTimeout(()=>resolve({engine:null,timedOut:true}),3500))
    ]);
    if(result.timedOut||!result.engine){void create.then((late)=>late.dispose()).catch(()=>{});return null;}
    result.engine.setHardwareScalingLevel(1);
    return{engine:result.engine as AbstractEngine,backend:'WebGPU · Full Babylon.js',webgpu:true};
  }catch(e){console.warn('WebGPU initialization degraded to WebGL2:',e);return null;}
}

export async function createEngine(canvas: HTMLCanvasElement): Promise<EngineBoot> {
  const modern=await tryWebGPU(canvas);if(modern)return modern;
  const engine = new Engine(canvas, true, {
    preserveDrawingBuffer: false,
    stencil: true,
    powerPreference: 'high-performance',
    antialias: true,
    disableWebGL2Support: false,
    failIfMajorPerformanceCaveat: false
  });
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
  return { engine:engine as AbstractEngine,backend:label,webgpu:false };
}
