/**
 * OceanWorld — physically-based ocean.
 *
 * Waves: 6-component Gerstner (true trochoidal displacement, not a height
 * offset) evaluated on GPU for the surface and mirrored on CPU so floating
 * bodies ride the exact same surface.
 *
 * Reflections: screen-space ray-marched reflection of the rendered scene
 * (real per-pixel SSR against the depth buffer), blended by Fresnel with a
 * procedural sky. Plus refraction, depth-based absorption (Beer-Lambert),
 * sub-surface scattering on wave crests, and foam from surface curvature.
 */

import { Vector3, Vector2, Matrix } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { PBRMaterial } from '@babylonjs/core/Materials/PBR/pbrMaterial';
import { Effect } from '@babylonjs/core/Materials/effect';
import { RenderTargetTexture } from '@babylonjs/core/Materials/Textures/renderTargetTexture';
import { HemisphericLight } from '@babylonjs/core/Lights/hemisphericLight';
import { DirectionalLight } from '@babylonjs/core/Lights/directionalLight';
import { Constants } from '@babylonjs/core/Engines/constants';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { AbstractMesh } from '@babylonjs/core/Meshes/abstractMesh';
import { GLSL_NOISE } from '../Noise';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';

/* ---- shared Gerstner definition, used by both GLSL and TS ---- */
const WAVES = [
  { dir: [1.0, 0.22], amp: 0.62, len: 26.0, steep: 0.82, speed: 1.00 },
  { dir: [-0.7, 0.71], amp: 0.42, len: 17.0, steep: 0.74, speed: 1.22 },
  { dir: [0.35, -0.94], amp: 0.26, len: 10.5, steep: 0.66, speed: 1.50 },
  { dir: [-0.95, -0.31], amp: 0.16, len: 6.4, steep: 0.58, speed: 1.85 },
  { dir: [0.62, 0.78], amp: 0.09, len: 3.7, steep: 0.5, speed: 2.30 },
  { dir: [-0.28, 0.96], amp: 0.05, len: 2.1, steep: 0.42, speed: 2.90 }
];

const GERSTNER_GLSL = `
struct Wave { vec2 dir; float amp; float len; float steep; float speed; };
const int NW = 6;
uniform Wave waves[NW];
uniform float choppy;
uniform float waveScale;

void gerstner(vec2 p, float t, out vec3 disp, out vec3 nrm){
  disp = vec3(0.0);
  vec3 tang = vec3(1.0, 0.0, 0.0);
  vec3 bino = vec3(0.0, 0.0, 1.0);
  for (int i = 0; i < NW; i++){
    Wave w = waves[i];
    float k = 6.28318530718 / max(w.len, 0.001);
    float c = sqrt(9.81 / k);
    vec2 d = normalize(w.dir);
    float f = k * (dot(d, p) - c * w.speed * t);
    float a = w.amp * waveScale;
    float st = w.steep * choppy / max(k * a * float(NW), 0.001);
    float sf = sin(f), cf = cos(f);
    disp.x += st * a * d.x * cf;
    disp.z += st * a * d.y * cf;
    disp.y += a * sf;
    float wa = k * a;
    tang += vec3(-st * d.x * d.x * wa * sf, d.x * wa * cf, -st * d.x * d.y * wa * sf);
    bino += vec3(-st * d.x * d.y * wa * sf, d.y * wa * cf, -st * d.y * d.y * wa * sf);
  }
  nrm = normalize(cross(bino, tang));
}
`;

const OCEAN_VERT = `
precision highp float;
attribute vec3 position;
attribute vec2 uv;
uniform mat4 world;
uniform mat4 worldViewProjection;
uniform mat4 view;
uniform float time;
${GERSTNER_GLSL}
varying vec3 vWorld;
varying vec3 vNormal;
varying vec2 vUV;
varying float vCrest;
varying vec4 vClip;
void main(void){
  vec3 p = position;
  vec3 disp, nrm;
  gerstner(p.xz, time, disp, nrm);
  p += disp;
  vCrest = disp.y;
  vec4 wp = world * vec4(p, 1.0);
  vWorld = wp.xyz;
  vNormal = nrm;
  vUV = uv;
  vClip = worldViewProjection * vec4(p, 1.0);
  gl_Position = vClip;
}
`;

const OCEAN_FRAG = `
precision highp float;
varying vec3 vWorld;
varying vec3 vNormal;
varying vec2 vUV;
varying float vCrest;
varying vec4 vClip;

uniform vec3  camPos;
uniform vec3  sunDir;
uniform vec3  sunColor;
uniform vec3  deepColor;
uniform vec3  shallowColor;
uniform float time;
uniform float roughness;
uniform float ssrStrength;
uniform float ssrSteps;
uniform float foamAmount;
uniform float sssAmount;
uniform float seabedDepth;

uniform sampler2D sceneTex;   // last frame colour, for SSR
uniform sampler2D depthTex;   // linear depth
uniform mat4 viewProj;
uniform mat4 invView;
uniform vec2 texel;
uniform float camNear;
uniform float camFar;

${GLSL_NOISE}

vec3 skyColor(vec3 d){
  float h = clamp(d.y * 0.5 + 0.5, 0.0, 1.0);
  vec3 zenith = vec3(0.16, 0.34, 0.62);
  vec3 horiz  = vec3(0.62, 0.74, 0.86);
  vec3 c = mix(horiz, zenith, pow(h, 0.85));
  // sun disc + halo
  float sd = max(dot(d, normalize(sunDir)), 0.0);
  c += sunColor * pow(sd, 900.0) * 12.0;
  c += sunColor * pow(sd, 28.0) * 0.32;
  // light cloud banding
  float n = fbm(vec3(d.xz * 3.2 + time * 0.01, d.y * 2.0), 5, 2.2, 0.5) * 0.5 + 0.5;
  c = mix(c, vec3(0.92, 0.95, 1.0), smoothstep(0.55, 0.95, n) * 0.28 * smoothstep(0.0, 0.35, d.y));
  return c;
}

// ---- screen-space ray-traced reflection ----
vec3 ssr(vec3 origin, vec3 dir, out float hit){
  hit = 0.0;
  float stepLen = 0.42;
  vec3 p = origin + dir * 0.15;
  int steps = int(clamp(ssrSteps, 8.0, 64.0));
  for (int i = 0; i < 64; i++){
    if (i >= steps) break;
    p += dir * stepLen;
    vec4 cp = viewProj * vec4(p, 1.0);
    if (cp.w <= 0.0) break;
    vec2 su = (cp.xy / cp.w) * 0.5 + 0.5;
    if (su.x < 0.0 || su.x > 1.0 || su.y < 0.0 || su.y > 1.0) break;
    float sceneD = texture2D(depthTex, su).r;
    float rayD = cp.w;
    float diff = rayD - sceneD * camFar;
    if (diff > 0.02 && diff < stepLen * 5.0){
      // refine
      vec3 back = p - dir * stepLen * 0.5;
      vec4 c2 = viewProj * vec4(back, 1.0);
      vec2 su2 = (c2.xy / c2.w) * 0.5 + 0.5;
      float fade = 1.0
        - smoothstep(0.72, 1.0, abs(su.x * 2.0 - 1.0))
        - smoothstep(0.72, 1.0, abs(su.y * 2.0 - 1.0));
      hit = clamp(fade, 0.0, 1.0);
      return texture2D(sceneTex, mix(su2, su, 0.5)).rgb;
    }
    stepLen *= 1.16;
  }
  return vec3(0.0);
}

void main(void){
  vec3 N = normalize(vNormal);
  // high-frequency detail normal
  float e = 0.55;
  float n1 = fbm(vec3(vWorld.xz * 0.55, time * 0.35), 4, 2.3, 0.55);
  float nx = fbm(vec3((vWorld.xz + vec2(e,0.0)) * 0.55, time * 0.35), 4, 2.3, 0.55);
  float nz = fbm(vec3((vWorld.xz + vec2(0.0,e)) * 0.55, time * 0.35), 4, 2.3, 0.55);
  N = normalize(N + vec3(n1 - nx, 0.0, n1 - nz) * (1.6 * roughness));

  vec3 V = normalize(camPos - vWorld);
  vec3 L = normalize(sunDir);
  vec3 R = reflect(-V, N);

  // Fresnel (Schlick, F0 for water = 0.02)
  float f = 0.02 + 0.98 * pow(1.0 - max(dot(N, V), 0.0), 5.0);

  // --- reflection: ray-traced first, sky as fallback ---
  float hit = 0.0;
  vec3 rt = ssr(vWorld, R, hit);
  vec3 sky = skyColor(R);
  vec3 reflCol = mix(sky, rt, hit * ssrStrength);

  // --- refraction / depth absorption (Beer-Lambert) ---
  float depth = clamp((seabedDepth + vWorld.y) / max(seabedDepth, 0.001), 0.0, 1.0);
  vec3 absorb = exp(-vec3(0.42, 0.14, 0.08) * (1.0 - depth) * 7.0);
  vec3 body = mix(deepColor, shallowColor, depth) * absorb;

  // --- sub-surface scattering through crests ---
  float sss = pow(max(0.0, dot(V, -L)), 3.0) * max(0.0, vCrest) * sssAmount;
  body += vec3(0.10, 0.52, 0.42) * sss * 1.7;

  // --- specular (GGX-ish) ---
  vec3 H = normalize(L + V);
  float a = max(roughness * 0.34, 0.018);
  float ndh = max(dot(N, H), 0.0);
  float d = a * a / (3.14159 * pow(ndh * ndh * (a * a - 1.0) + 1.0, 2.0));
  vec3 spec = sunColor * d * 1.7;

  // --- foam from crest height and surface tilt ---
  // (derivatives are avoided: GLSL ES 1.00 needs an extension for dFdx)
  float tilt = 1.0 - clamp(N.y, 0.0, 1.0);
  float fo = smoothstep(0.55, 1.25, vCrest * 1.2 + tilt * 3.2) * foamAmount;
  float fn = fbm(vec3(vWorld.xz * 1.9, time * 0.6), 4, 2.4, 0.5) * 0.5 + 0.5;
  fo *= smoothstep(0.25, 0.85, fn);

  vec3 col = mix(body, reflCol, f) + spec;
  col = mix(col, vec3(0.97, 0.99, 1.0), clamp(fo, 0.0, 1.0));

  // distance haze
  float dist = length(camPos - vWorld);
  col = mix(col, skyColor(normalize(vWorld - camPos)) * 0.85, smoothstep(90.0, 300.0, dist));

  gl_FragColor = vec4(col, 1.0);
}
`;

interface Floater {
  mesh: AbstractMesh;
  x: number;
  z: number;
  buoy: number;
  phase: number;
}

export class OceanWorld implements World {
  id = 'ocean';
  name = 'Ocean Worlds';

  private ocean!: Mesh;
  private mat!: ShaderMaterial;
  private seabed!: Mesh;
  private floaters: Floater[] = [];
  private rtt!: RenderTargetTexture;
  private depthRT!: RenderTargetTexture;
  private t = 0;
  private sun!: DirectionalLight;

  private p = {
    waveScale: 1.0,
    choppy: 1.0,
    windSpeed: 1.0,
    roughness: 0.35,
    ssr: 0.9,
    ssrSteps: 40,
    foam: 1.0,
    sss: 1.0,
    depth: 14
  };

  async build(ctx: WorldContext): Promise<void> {
    const scene = ctx.scene;
    scene.clearColor = new Color4(0.62, 0.74, 0.86, 1);

    const hemi = new HemisphericLight('h', new Vector3(0, 1, 0), scene);
    hemi.intensity = 0.55;
    hemi.groundColor = new Color3(0.12, 0.2, 0.26);

    this.sun = new DirectionalLight('sun', new Vector3(-0.45, -0.72, -0.3), scene);
    this.sun.intensity = 2.4;

    // ---- seabed ----
    this.seabed = MeshBuilder.CreateGround('seabed', { width: 400, height: 400, subdivisions: 120 }, scene);
    const pos = this.seabed.getVerticesData('position')!;
    for (let i = 0; i < pos.length; i += 3) {
      const x = pos[i], z = pos[i + 2];
      pos[i + 1] =
        Math.sin(x * 0.045) * Math.cos(z * 0.038) * 3.4 +
        Math.sin(x * 0.13 + 1.7) * Math.cos(z * 0.11) * 1.1 - 14;
    }
    this.seabed.updateVerticesData('position', pos);
    this.seabed.createNormals(true);
    const bedMat = new PBRMaterial('bed', scene);
    bedMat.albedoColor = new Color3(0.34, 0.29, 0.22);
    bedMat.roughness = 0.95;
    bedMat.metallic = 0;
    this.seabed.material = bedMat;

    // ---- floating props ----
    const palette = [
      new Color3(0.90, 0.35, 0.20), new Color3(0.20, 0.85, 0.60),
      new Color3(0.95, 0.78, 0.25), new Color3(0.55, 0.40, 0.95),
      new Color3(0.25, 0.62, 0.95)
    ];
    for (let i = 0; i < 16; i++) {
      const s = 1.1 + Math.random() * 2.2;
      const m = i % 3 === 0
        ? MeshBuilder.CreateBox('f' + i, { size: s }, scene)
        : MeshBuilder.CreateSphere('f' + i, { diameter: s, segments: 24 }, scene);
      const pm = new PBRMaterial('fm' + i, scene);
      pm.albedoColor = palette[i % palette.length];
      pm.roughness = 0.3 + Math.random() * 0.35;
      pm.metallic = i % 4 === 0 ? 0.75 : 0.05;
      m.material = pm;
      const x = (Math.random() - 0.5) * 120;
      const z = (Math.random() - 0.5) * 120;
      m.position.set(x, 0, z);
      this.floaters.push({ mesh: m, x, z, buoy: s * 0.28, phase: Math.random() * 6.28 });
    }

    // ---- reflection source: colour + depth of everything except the water ----
    const eng = scene.getEngine();
    this.rtt = new RenderTargetTexture('sceneRT', { width: 1024, height: 640 }, scene, false);
    this.rtt.renderList = [this.seabed, ...this.floaters.map((f) => f.mesh)];
    scene.customRenderTargets.push(this.rtt);

    this.depthRT = new RenderTargetTexture('depthRT', { width: 1024, height: 640 }, scene, false, true, Constants.TEXTURETYPE_FLOAT);
    this.depthRT.renderList = this.rtt.renderList;
    const depthMat = new ShaderMaterial('dm', scene, {
      vertexSource: `
        precision highp float;
        attribute vec3 position;
        uniform mat4 worldViewProjection;
        varying float vD;
        void main(void){
          vec4 p = worldViewProjection * vec4(position, 1.0);
          vD = p.w;
          gl_Position = p;
        }`,
      fragmentSource: `
        precision highp float;
        varying float vD;
        uniform float camFar;
        void main(void){ gl_FragColor = vec4(vD / camFar, 0.0, 0.0, 1.0); }`
    }, { attributes: ['position'], uniforms: ['worldViewProjection', 'camFar'] });
    this.depthRT.setMaterialForRendering(this.rtt.renderList as AbstractMesh[], depthMat);
    scene.customRenderTargets.push(this.depthRT);
    this.depthRT.onBeforeRenderObservable.add(() => depthMat.setFloat('camFar', ctx.camera.maxZ));

    // ---- ocean surface ----
    Effect.ShadersStore['oceanVertexShader'] = OCEAN_VERT;
    Effect.ShadersStore['oceanFragmentShader'] = OCEAN_FRAG;

    this.mat = new ShaderMaterial('ocean', scene, 'ocean', {
      attributes: ['position', 'uv'],
      uniforms: [
        'world', 'worldViewProjection', 'view', 'time', 'camPos', 'sunDir', 'sunColor',
        'deepColor', 'shallowColor', 'roughness', 'ssrStrength', 'ssrSteps',
        'foamAmount', 'sssAmount', 'seabedDepth', 'viewProj', 'invView',
        'texel', 'camNear', 'camFar', 'choppy', 'waveScale',
        // Struct-array members must be named individually or their uniform
        // locations are never resolved and the waves stay flat.
        ...WAVES.flatMap((_, i) => [
          `waves[${i}].dir`, `waves[${i}].amp`, `waves[${i}].len`,
          `waves[${i}].steep`, `waves[${i}].speed`
        ])
      ],
      samplers: ['sceneTex', 'depthTex']
    });
    this.mat.setTexture('sceneTex', this.rtt);
    this.mat.setTexture('depthTex', this.depthRT);

    WAVES.forEach((w, i) => {
      this.mat.setVector2(`waves[${i}].dir`, new Vector2(w.dir[0], w.dir[1]));
      this.mat.setFloat(`waves[${i}].amp`, w.amp);
      this.mat.setFloat(`waves[${i}].len`, w.len);
      this.mat.setFloat(`waves[${i}].steep`, w.steep);
      this.mat.setFloat(`waves[${i}].speed`, w.speed);
    });

    this.ocean = MeshBuilder.CreateGround('ocean', { width: 400, height: 400, subdivisions: 320 }, scene);
    this.ocean.material = this.mat;
    this.ocean.isPickable = false;

    ctx.setCameraTarget(new Vector3(0, 0, 0), 52);
  }

  /** CPU mirror of the vertex shader, so props sit exactly on the surface. */
  private surface(x: number, z: number, t: number): { y: number; nx: number; nz: number } {
    let dx = 0, dy = 0, dz = 0;
    let tx = 1, ty = 0, tz = 0;
    let bx = 0, by = 0, bz = 1;
    const scale = this.p.waveScale;
    for (const w of WAVES) {
      const k = (Math.PI * 2) / w.len;
      const c = Math.sqrt(9.81 / k);
      const len = Math.hypot(w.dir[0], w.dir[1]);
      const dxn = w.dir[0] / len, dzn = w.dir[1] / len;
      // `t` already carries windSpeed (see update()), so it is not applied twice here.
      const f = k * (dxn * x + dzn * z - c * w.speed * t);
      const a = w.amp * scale;
      const st = (w.steep * this.p.choppy) / Math.max(k * a * WAVES.length, 0.001);
      const sf = Math.sin(f), cf = Math.cos(f);
      dx += st * a * dxn * cf;
      dz += st * a * dzn * cf;
      dy += a * sf;
      const wa = k * a;
      tx += -st * dxn * dxn * wa * sf; ty += dxn * wa * cf; tz += -st * dxn * dzn * wa * sf;
      bx += -st * dxn * dzn * wa * sf; by += dzn * wa * cf; bz += -st * dzn * dzn * wa * sf;
    }
    // normal = cross(bino, tang)
    const nx = by * tz - bz * ty;
    const nz = bx * ty - by * tx;
    return { y: dy, nx, nz };
  }

  update(dt: number, ctx: WorldContext): void {
    this.t += dt * this.p.windSpeed;
    const cam = ctx.camera;
    const scene = ctx.scene;

    this.mat.setFloat('time', this.t);
    this.mat.setVector3('camPos', cam.position);
    this.mat.setVector3('sunDir', this.sun.direction.scale(-1));
    this.mat.setColor3('sunColor', new Color3(1.0, 0.95, 0.86));
    this.mat.setColor3('deepColor', new Color3(0.008, 0.06, 0.11));
    this.mat.setColor3('shallowColor', new Color3(0.10, 0.44, 0.52));
    this.mat.setFloat('roughness', this.p.roughness);
    this.mat.setFloat('ssrStrength', this.p.ssr);
    this.mat.setFloat('ssrSteps', this.p.ssrSteps);
    this.mat.setFloat('foamAmount', this.p.foam);
    this.mat.setFloat('sssAmount', this.p.sss);
    this.mat.setFloat('seabedDepth', this.p.depth);
    this.mat.setFloat('choppy', this.p.choppy);
    this.mat.setFloat('waveScale', this.p.waveScale);
    this.mat.setFloat('camNear', cam.minZ);
    this.mat.setFloat('camFar', cam.maxZ);
    this.mat.setMatrix('viewProj', scene.getTransformMatrix());
    this.mat.setMatrix('invView', Matrix.Invert(cam.getViewMatrix()));
    this.mat.setVector2('texel', new Vector2(1 / 1024, 1 / 640));

    // buoyancy — props ride the true trochoidal surface with tilt
    for (const f of this.floaters) {
      const s = this.surface(f.x, f.z, this.t);
      f.mesh.position.y = s.y + f.buoy;
      f.mesh.rotation.x = Math.atan(s.nz) * 0.8;
      f.mesh.rotation.z = -Math.atan(s.nx) * 0.8;
      f.mesh.rotation.y += 0.12 * (1 / 60);
    }
  }

  getParams(): WorldParam[] {
    return [
      { key: 'waveScale', label: 'Wave Height', min: 0, max: 3, step: 0.05, value: this.p.waveScale, unit: 'm' },
      { key: 'choppy', label: 'Choppiness', min: 0, max: 2, step: 0.05, value: this.p.choppy },
      { key: 'windSpeed', label: 'Wind Speed', min: 0, max: 3, step: 0.05, value: this.p.windSpeed },
      { key: 'roughness', label: 'Surface Detail', min: 0.02, max: 1, step: 0.02, value: this.p.roughness },
      { key: 'ssr', label: 'Ray-Traced Reflection', min: 0, max: 1, step: 0.02, value: this.p.ssr },
      { key: 'ssrSteps', label: 'Reflection Quality', min: 8, max: 64, step: 1, value: this.p.ssrSteps, unit: 'steps' },
      { key: 'foam', label: 'Foam', min: 0, max: 2, step: 0.05, value: this.p.foam },
      { key: 'sss', label: 'Subsurface Scatter', min: 0, max: 3, step: 0.05, value: this.p.sss },
      { key: 'depth', label: 'Water Depth', min: 4, max: 40, step: 0.5, value: this.p.depth, unit: 'm' }
    ];
  }

  getActions(): WorldAction[] {
    return [
      { key: 'calm', label: 'Dead Calm', glyph: '🪞' },
      { key: 'storm', label: 'Storm', glyph: '🌩' },
      { key: 'drop', label: 'Drop Object', glyph: '🎯' }
    ];
  }

  runAction(key: string, ctx: WorldContext): void {
    if (key === 'calm') {
      this.p.waveScale = 0.12; this.p.choppy = 0.25; this.p.windSpeed = 0.3; this.p.foam = 0.1;
    } else if (key === 'storm') {
      this.p.waveScale = 2.4; this.p.choppy = 1.7; this.p.windSpeed = 2.3; this.p.foam = 1.8;
    } else if (key === 'drop') {
      const scene = ctx.scene;
      const s = 1.2 + Math.random() * 2.4;
      const m = MeshBuilder.CreateSphere('drop' + Date.now(), { diameter: s, segments: 24 }, scene);
      const pm = new PBRMaterial('dm' + Date.now(), scene);
      pm.albedoColor = new Color3(Math.random(), Math.random() * 0.8 + 0.2, Math.random());
      pm.roughness = 0.28; pm.metallic = 0.4;
      m.material = pm;
      const x = (Math.random() - 0.5) * 100;
      const z = (Math.random() - 0.5) * 100;
      m.position.set(x, 14, z);
      this.floaters.push({ mesh: m, x, z, buoy: s * 0.28, phase: 0 });
      this.rtt.renderList!.push(m);
      this.depthRT.renderList!.push(m);
    }
  }

  setParam(key: string, value: number): void {
    (this.p as any)[key] = value;
  }

  getStats(): Record<string, string> {
    return {
      'Wave model': 'Gerstner ×6',
      'Reflections': this.p.ssr > 0.05 ? `SSR ${Math.round(this.p.ssrSteps)} steps` : 'Sky only',
      'Floating bodies': String(this.floaters.length),
      'Surface verts': '102k'
    };
  }

  dispose(): void {
    this.floaters.forEach((f) => f.mesh.dispose());
    this.floaters = [];
    this.ocean?.dispose();
    this.seabed?.dispose();
    this.mat?.dispose();
    this.rtt?.dispose();
    this.depthRT?.dispose();
  }
}
