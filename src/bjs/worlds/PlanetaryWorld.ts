/**
 * PlanetaryWorld — procedurally shaded star system.
 *
 * Every planet gets a unique surface generated entirely in the fragment
 * shader from a seeded FBM/ridged-noise stack: continents, biome banding by
 * latitude and altitude, mountain ridges, polar caps, ocean specular masks,
 * cloud layers with their own advection, city lights on the night side, gas
 * giant zonal bands with storm vortices, and rings. Plus a Rayleigh-style
 * atmospheric rim on every body that has an atmosphere.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { Effect } from '@babylonjs/core/Materials/effect';
import { PointLight } from '@babylonjs/core/Lights/pointLight';
import { TransformNode } from '@babylonjs/core/Meshes/transformNode';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import { DynamicTexture } from '@babylonjs/core/Materials/Textures/dynamicTexture';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import { GLSL_NOISE } from '../Noise';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';

/* --------------------------- planet shader --------------------------- */

const PLANET_VERT = `
precision highp float;
attribute vec3 position;
attribute vec3 normal;
attribute vec2 uv;
uniform mat4 world;
uniform mat4 worldViewProjection;
varying vec3 vPos;
varying vec3 vNrm;
varying vec3 vWorld;
varying vec2 vUV;
void main(void){
  vPos = position;
  vNrm = normal;
  vWorld = (world * vec4(position, 1.0)).xyz;
  vUV = uv;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

const PLANET_FRAG = `
precision highp float;
varying vec3 vPos;
varying vec3 vNrm;
varying vec3 vWorld;
varying vec2 vUV;

uniform vec3  camPos;
uniform vec3  sunPos;
uniform float time;
uniform float seed;
uniform float ptype;      // 0 rocky, 1 terran, 2 ice, 3 gas, 4 lava, 5 desert
uniform vec3  tintA;
uniform vec3  tintB;
uniform float detail;
uniform float cloudAmt;
uniform float cityLights;
uniform float radius;

${GLSL_NOISE}

vec3 shade(vec3 p, out float rough, out float spec){
  vec3 sp = p * (1.6 + detail * 2.2) + seed * 37.0;
  float lat = abs(p.y);
  rough = 0.9; spec = 0.0;

  if (ptype < 0.5){
    // ---- rocky / cratered ----
    float base = fbm(sp * 1.4, 6, 2.15, 0.52) * 0.5 + 0.5;
    float cr = ridged(sp * 3.1, 5, 2.3, 0.5);
    float craters = smoothstep(0.62, 0.95, cr);
    vec3 c = mix(tintA * 0.55, tintB, base);
    c = mix(c, c * 0.45, craters * 0.7);
    c += vec3(0.06) * fbm(sp * 12.0, 4, 2.4, 0.5);
    return c;
  } else if (ptype < 1.5){
    // ---- terran: continents, biomes, mountains, ice caps ----
    float cont = fbm(sp * 0.85, 7, 2.05, 0.55) * 0.5 + 0.5;
    cont = pow(cont, 1.25);
    float sea = 0.50;
    float land = smoothstep(sea - 0.015, sea + 0.02, cont);
    float alt = clamp((cont - sea) / 0.4, 0.0, 1.0);
    float mtn = ridged(sp * 2.6, 6, 2.25, 0.5);
    alt = clamp(alt + mtn * 0.42 * land, 0.0, 1.0);

    vec3 ocean = mix(vec3(0.01,0.05,0.16), vec3(0.03,0.22,0.38), smoothstep(0.30, 0.50, cont));
    vec3 shore = vec3(0.72, 0.66, 0.42);
    vec3 grass = mix(vec3(0.10,0.30,0.10), vec3(0.20,0.42,0.14), fbm(sp * 5.0, 4, 2.3, 0.5) * 0.5 + 0.5);
    vec3 arid  = vec3(0.56, 0.44, 0.24);
    vec3 rock  = vec3(0.40, 0.36, 0.33);
    vec3 snow  = vec3(0.93, 0.95, 0.98);

    // latitude biome
    float trop = 1.0 - smoothstep(0.15, 0.72, lat);
    vec3 lc = mix(arid, grass, trop);
    lc = mix(lc, shore, smoothstep(0.06, 0.0, alt));
    lc = mix(lc, rock, smoothstep(0.34, 0.62, alt));
    lc = mix(lc, snow, smoothstep(0.6, 0.85, alt));
    // polar caps
    float pole = smoothstep(0.74, 0.90, lat + fbm(sp * 4.0, 3, 2.2, 0.5) * 0.09);
    lc = mix(lc, snow, pole);

    vec3 c = mix(ocean, lc, land);
    rough = mix(0.06, 0.95, land);
    spec = (1.0 - land) * 0.9;
    return c;
  } else if (ptype < 2.5){
    // ---- ice world ----
    float f = fbm(sp * 1.9, 6, 2.2, 0.55) * 0.5 + 0.5;
    float cracks = ridged(sp * 5.5, 5, 2.4, 0.52);
    vec3 c = mix(tintA, tintB, f);
    c = mix(c, vec3(0.35, 0.55, 0.72), smoothstep(0.70, 0.95, cracks) * 0.8);
    rough = 0.22; spec = 0.6;
    return c;
  } else if (ptype < 3.5){
    // ---- gas giant: zonal bands + storms ----
    float band = p.y * 7.5 + fbm(vec3(sp.x * 0.7, sp.y * 3.4, sp.z * 0.7 + time * 0.02), 5, 2.2, 0.55) * 2.6;
    float b = sin(band) * 0.5 + 0.5;
    vec3 c = mix(tintA, tintB, b);
    // turbulent shear
    float turb = fbm(vec3(sp.x * 2.2 + time * 0.05, sp.y * 8.0, sp.z * 2.2), 5, 2.4, 0.5) * 0.5 + 0.5;
    c = mix(c, c * 1.28, turb * 0.5);
    // great storm
    vec3 sc = normalize(vec3(0.62, -0.28, 0.44));
    float sd = distance(normalize(p), sc);
    float storm = smoothstep(0.30, 0.05, sd);
    float swirl = fbm(vec3(p.xz * 9.0 + time * 0.12, p.y * 9.0), 5, 2.3, 0.5) * 0.5 + 0.5;
    c = mix(c, mix(vec3(0.78,0.28,0.16), vec3(0.95,0.62,0.38), swirl), storm * 0.85);
    rough = 0.85;
    return c;
  } else if (ptype < 4.5){
    // ---- lava world ----
    float f = fbm(sp * 2.4 + vec3(0.0, time * 0.05, 0.0), 6, 2.25, 0.52) * 0.5 + 0.5;
    float crust = smoothstep(0.42, 0.72, f);
    vec3 magma = mix(vec3(1.0, 0.85, 0.25), vec3(0.95, 0.22, 0.03), f);
    vec3 rock = mix(vec3(0.09,0.06,0.06), vec3(0.20,0.16,0.15), fbm(sp * 7.0, 4, 2.3, 0.5) * 0.5 + 0.5);
    vec3 c = mix(magma * 2.4, rock, crust);
    rough = 0.9;
    return c;
  }
  // ---- desert ----
  float dunes = fbm(vec3(sp.x * 3.0, sp.y * 9.0, sp.z * 3.0), 6, 2.2, 0.55) * 0.5 + 0.5;
  float can = ridged(sp * 3.4, 5, 2.3, 0.5);
  vec3 c = mix(tintA, tintB, dunes);
  c = mix(c, c * 0.55, smoothstep(0.74, 0.96, can) * 0.75);
  rough = 0.95;
  return c;
}

void main(void){
  vec3 n = normalize(vNrm);
  vec3 p = normalize(vPos);
  float rough, specMask;
  vec3 albedo = shade(p, rough, specMask);

  // normal perturbation from the same field (bump without a texture)
  float e = 0.012;
  float h0 = fbm(p * (5.0 + detail * 6.0) + seed * 37.0, 5, 2.2, 0.5);
  float hx = fbm((p + vec3(e,0,0)) * (5.0 + detail * 6.0) + seed * 37.0, 5, 2.2, 0.5);
  float hy = fbm((p + vec3(0,e,0)) * (5.0 + detail * 6.0) + seed * 37.0, 5, 2.2, 0.5);
  float hz = fbm((p + vec3(0,0,e)) * (5.0 + detail * 6.0) + seed * 37.0, 5, 2.2, 0.5);
  vec3 grad = vec3(hx - h0, hy - h0, hz - h0) / e;
  n = normalize(n - (grad - dot(grad, n) * n) * 0.010 * (1.0 - specMask));

  vec3 L = normalize(sunPos - vWorld);
  vec3 V = normalize(camPos - vWorld);
  float ndl = dot(n, L);
  float lam = max(ndl, 0.0);
  // soft terminator
  float day = smoothstep(-0.12, 0.22, ndl);

  vec3 col = albedo * (lam * 1.25 + 0.035);

  // ocean / ice specular
  if (specMask > 0.01){
    vec3 H = normalize(L + V);
    float a = max(rough * rough, 0.004);
    float ndh = max(dot(n, H), 0.0);
    float d = a * a / (3.14159 * pow(ndh * ndh * (a * a - 1.0) + 1.0, 2.0));
    col += vec3(1.0, 0.97, 0.9) * d * specMask * 1.6 * lam;
  }

  // ---- clouds ----
  if (cloudAmt > 0.01 && ptype > 0.5 && ptype < 3.5){
    vec3 cp = p * 3.1 + vec3(time * 0.012, 0.0, time * 0.006) + seed * 11.0;
    float cl = fbm(cp, 6, 2.3, 0.55) * 0.5 + 0.5;
    float cl2 = fbm(cp * 2.4 - time * 0.02, 5, 2.4, 0.5) * 0.5 + 0.5;
    float cover = smoothstep(0.52, 0.80, cl * 0.65 + cl2 * 0.45) * cloudAmt;
    vec3 cloudCol = vec3(1.0) * (lam * 1.15 + 0.05);
    col = mix(col, cloudCol, clamp(cover, 0.0, 0.92));
  }

  // ---- city lights on the night side ----
  if (cityLights > 0.01){
    float cont = fbm(p * (1.6 + detail * 2.2) * 0.85 + seed * 37.0, 7, 2.05, 0.55) * 0.5 + 0.5;
    float land = smoothstep(0.50, 0.53, pow(cont, 1.25));
    float grid = fbm(p * 42.0 + seed * 5.0, 4, 2.5, 0.5) * 0.5 + 0.5;
    float lights = smoothstep(0.68, 0.92, grid) * land * (1.0 - day) * cityLights;
    col += vec3(1.0, 0.82, 0.48) * lights * 1.7;
  }

  // ---- atmospheric rim (Rayleigh-ish) ----
  float rim = pow(1.0 - max(dot(n, V), 0.0), 3.0);
  col += mix(vec3(0.18,0.42,0.95), vec3(0.95,0.55,0.30), 1.0 - day) * rim * 0.5 * day;

  // filmic
  col = (col * (2.51 * col + 0.03)) / (col * (2.43 * col + 0.59) + 0.14);
  col = pow(clamp(col, 0.0, 1.0), vec3(1.0 / 2.2));
  gl_FragColor = vec4(col, 1.0);
}
`;

/* --------------------------- atmosphere shell --------------------------- */

const ATMO_FRAG = `
precision highp float;
varying vec3 vNrm;
varying vec3 vWorld;
uniform vec3 camPos;
uniform vec3 sunPos;
uniform vec3 atmoColor;
uniform float power;
void main(void){
  vec3 n = normalize(vNrm);
  vec3 V = normalize(camPos - vWorld);
  vec3 L = normalize(sunPos - vWorld);
  float rim = pow(1.0 - max(dot(n, V), 0.0), power);
  float lit = pow(max(dot(n, L), 0.0), 0.6);
  float a = rim * (0.25 + lit * 1.15);
  // forward scattering glow near the limb toward the sun
  float fs = pow(max(dot(V, -L), 0.0), 6.0) * 0.5;
  gl_FragColor = vec4(atmoColor * (1.0 + fs), clamp(a, 0.0, 1.0));
}
`;

const ATMO_VERT = `
precision highp float;
attribute vec3 position;
attribute vec3 normal;
uniform mat4 world;
uniform mat4 worldViewProjection;
varying vec3 vNrm;
varying vec3 vWorld;
void main(void){
  vNrm = normalize(mat3(world[0].xyz, world[1].xyz, world[2].xyz) * normal);
  vWorld = (world * vec4(position, 1.0)).xyz;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

/* --------------------------- star surface --------------------------- */

const STAR_FRAG = `
precision highp float;
varying vec3 vPos;
varying vec3 vNrm;
varying vec3 vWorld;
uniform vec3 camPos;
uniform float time;
uniform vec3 hot;
uniform vec3 cool;
${GLSL_NOISE}
void main(void){
  vec3 p = normalize(vPos);
  float g = fbm(p * 6.0 + vec3(0.0, time * 0.09, 0.0), 6, 2.3, 0.55) * 0.5 + 0.5;
  float g2 = fbm(p * 16.0 - time * 0.16, 5, 2.4, 0.5) * 0.5 + 0.5;
  float cells = pow(g, 1.5) * 0.75 + g2 * 0.45;
  vec3 col = mix(cool, hot, cells);
  // limb darkening
  vec3 V = normalize(camPos - vWorld);
  float limb = pow(max(dot(normalize(vNrm), V), 0.0), 0.45);
  col *= 0.55 + limb * 0.75;
  col *= 2.4;
  col = (col * (2.51 * col + 0.03)) / (col * (2.43 * col + 0.59) + 0.14);
  gl_FragColor = vec4(pow(clamp(col, 0.0, 1.0), vec3(1.0/2.2)), 1.0);
}
`;

interface Body {
  root: TransformNode;
  mesh: Mesh;
  mat: ShaderMaterial;
  atmo?: Mesh;
  atmoMat?: ShaderMaterial;
  orbitR: number;
  orbitSpeed: number;
  angle: number;
  spin: number;
  name: string;
  moons: { pivot: TransformNode; speed: number }[];
}

const PLANETS: {
  name: string; r: number; orbit: number; speed: number; type: number;
  a: [number, number, number]; b: [number, number, number];
  atmo?: [number, number, number]; clouds: number; lights: number; moons: number; ring?: boolean;
}[] = [
  { name: 'Cinder',   r: 0.62, orbit: 14,  speed: 0.62, type: 4, a: [0.35,0.12,0.08], b: [0.6,0.3,0.2], clouds: 0, lights: 0, moons: 0 },
  { name: 'Vasara',   r: 0.95, orbit: 21,  speed: 0.44, type: 5, a: [0.72,0.52,0.28], b: [0.92,0.78,0.5], atmo: [0.85,0.6,0.35], clouds: 0.25, lights: 0, moons: 0 },
  { name: 'Terrapor', r: 1.15, orbit: 30,  speed: 0.33, type: 1, a: [0.2,0.4,0.15], b: [0.5,0.45,0.3], atmo: [0.25,0.5,1.0], clouds: 0.75, lights: 1.0, moons: 1 },
  { name: 'Rhogar',   r: 0.86, orbit: 40,  speed: 0.25, type: 0, a: [0.42,0.26,0.18], b: [0.66,0.44,0.3], clouds: 0, lights: 0, moons: 2 },
  { name: 'Ophion',   r: 2.9,  orbit: 58,  speed: 0.15, type: 3, a: [0.72,0.58,0.40], b: [0.90,0.80,0.62], clouds: 0, lights: 0, moons: 3, ring: true },
  { name: 'Kelvara',  r: 2.3,  orbit: 78,  speed: 0.10, type: 3, a: [0.32,0.48,0.62], b: [0.62,0.78,0.88], clouds: 0, lights: 0, moons: 2, ring: true },
  { name: 'Silex',    r: 1.05, orbit: 96,  speed: 0.07, type: 2, a: [0.66,0.78,0.88], b: [0.88,0.94,0.99], atmo: [0.6,0.8,1.0], clouds: 0.3, lights: 0, moons: 1 }
];

export class PlanetaryWorld implements World {
  id = 'planetary';
  name = 'Star Systems';

  private bodies: Body[] = [];
  private star!: Mesh;
  private starMat!: ShaderMaterial;
  private light!: PointLight;
  private stars!: Mesh;
  private t = 0;

  private p = { timeScale: 1.0, detail: 1.0, clouds: 1.0, lights: 1.0, exposure: 1.0, orbitSpeed: 1.0 };

  async build(ctx: WorldContext): Promise<void> {
    const scene = ctx.scene;
    scene.clearColor = new Color4(0.002, 0.004, 0.012, 1);

    Effect.ShadersStore['planetVertexShader'] = PLANET_VERT;
    Effect.ShadersStore['planetFragmentShader'] = PLANET_FRAG;
    Effect.ShadersStore['atmoVertexShader'] = ATMO_VERT;
    Effect.ShadersStore['atmoFragmentShader'] = ATMO_FRAG;
    Effect.ShadersStore['starVertexShader'] = PLANET_VERT;
    Effect.ShadersStore['starFragmentShader'] = STAR_FRAG;

    // ---- skybox of stars ----
    this.stars = MeshBuilder.CreateSphere('sky', { diameter: 1800, segments: 32, sideOrientation: 1 }, scene);
    const skyMat = new StandardMaterial('skyMat', scene);
    skyMat.emissiveTexture = this.starfieldTexture(scene);
    skyMat.diffuseColor = Color3.Black();
    skyMat.specularColor = Color3.Black();
    skyMat.backFaceCulling = false;
    skyMat.disableLighting = true;
    this.stars.material = skyMat;
    this.stars.infiniteDistance = true;
    this.stars.isPickable = false;

    // ---- central star ----
    this.star = MeshBuilder.CreateSphere('star', { diameter: 9, segments: 64 }, scene);
    this.starMat = new ShaderMaterial('starM', scene, 'star', {
      attributes: ['position', 'normal', 'uv'],
      uniforms: ['world', 'worldViewProjection', 'camPos', 'time', 'hot', 'cool']
    });
    this.starMat.setColor3('hot', new Color3(1.0, 0.98, 0.86));
    this.starMat.setColor3('cool', new Color3(1.0, 0.55, 0.12));
    this.star.material = this.starMat;

    const corona = MeshBuilder.CreateSphere('corona', { diameter: 13.5, segments: 48 }, scene);
    const cm = new ShaderMaterial('coronaM', scene, {
      vertexSource: ATMO_VERT, fragmentSource: ATMO_FRAG
    }, {
      attributes: ['position', 'normal'],
      uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'atmoColor', 'power'],
      needAlphaBlending: true
    });
    cm.setColor3('atmoColor', new Color3(1.0, 0.62, 0.22));
    cm.setFloat('power', 2.2);
    cm.setVector3('sunPos', Vector3.Zero());
    cm.alpha = 0.9;
    cm.backFaceCulling = false;
    corona.material = cm;
    corona.isPickable = false;
    (this as any)._coronaMat = cm;

    this.light = new PointLight('sunLight', Vector3.Zero(), scene);
    this.light.intensity = 2.2;
    this.light.range = 900;

    // ---- planets ----
    PLANETS.forEach((cfg, i) => {
      const root = new TransformNode('root_' + cfg.name, scene);
      const mesh = MeshBuilder.CreateSphere(cfg.name, { diameter: cfg.r * 2, segments: 96 }, scene);
      mesh.parent = root;

      const mat = new ShaderMaterial('m_' + cfg.name, scene, 'planet', {
        attributes: ['position', 'normal', 'uv'],
        uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'time',
                   'seed', 'ptype', 'tintA', 'tintB', 'detail', 'cloudAmt', 'cityLights', 'radius']
      });
      mat.setFloat('seed', i * 3.77 + 1.3);
      mat.setFloat('ptype', cfg.type);
      mat.setColor3('tintA', new Color3(...cfg.a));
      mat.setColor3('tintB', new Color3(...cfg.b));
      mat.setFloat('radius', cfg.r);
      mesh.material = mat;

      const body: Body = {
        root, mesh, mat,
        orbitR: cfg.orbit, orbitSpeed: cfg.speed,
        angle: Math.random() * Math.PI * 2,
        spin: 0.15 + Math.random() * 0.35,
        name: cfg.name, moons: []
      };

      if (cfg.atmo) {
        const atmo = MeshBuilder.CreateSphere('a_' + cfg.name, { diameter: cfg.r * 2.16, segments: 48 }, scene);
        atmo.parent = root;
        const am = new ShaderMaterial('am_' + cfg.name, scene, 'atmo', {
          attributes: ['position', 'normal'],
          uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'atmoColor', 'power'],
          needAlphaBlending: true
        });
        am.setColor3('atmoColor', new Color3(...cfg.atmo));
        am.setFloat('power', 3.0);
        am.backFaceCulling = false;
        atmo.material = am;
        atmo.isPickable = false;
        body.atmo = atmo;
        body.atmoMat = am;
      }

      // ---- rings ----
      if (cfg.ring) {
        const ring = MeshBuilder.CreateDisc('r_' + cfg.name, { radius: cfg.r * 2.4, tessellation: 128 }, scene);
        ring.parent = root;
        ring.rotation.x = Math.PI / 2;
        ring.rotation.z = 0.24;
        const rm = new StandardMaterial('rm_' + cfg.name, scene);
        rm.diffuseTexture = this.ringTexture(scene, cfg.r);
        rm.opacityTexture = rm.diffuseTexture;
        rm.emissiveColor = new Color3(0.35, 0.30, 0.24);
        rm.specularColor = Color3.Black();
        rm.backFaceCulling = false;
        (rm.diffuseTexture as Texture).hasAlpha = true;
        ring.material = rm;
        ring.isPickable = false;
      }

      // ---- moons ----
      for (let m = 0; m < cfg.moons; m++) {
        const pivot = new TransformNode('mp', scene);
        pivot.parent = root;
        pivot.rotation.x = (Math.random() - 0.5) * 0.7;
        const mr = cfg.r * (0.14 + Math.random() * 0.16);
        const moon = MeshBuilder.CreateSphere('moon', { diameter: mr * 2, segments: 40 }, scene);
        moon.parent = pivot;
        moon.position.x = cfg.r * (2.1 + m * 0.85);
        const mm = new ShaderMaterial('mm', scene, 'planet', {
          attributes: ['position', 'normal', 'uv'],
          uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'time',
                     'seed', 'ptype', 'tintA', 'tintB', 'detail', 'cloudAmt', 'cityLights', 'radius']
        });
        mm.setFloat('seed', i * 9.1 + m * 4.3 + 20.0);
        mm.setFloat('ptype', 0);
        mm.setColor3('tintA', new Color3(0.28, 0.26, 0.25));
        mm.setColor3('tintB', new Color3(0.62, 0.60, 0.57));
        mm.setFloat('radius', mr);
        moon.material = mm;
        body.moons.push({ pivot, speed: 0.5 + Math.random() * 0.9 });
        (body as any).moonMats = [...((body as any).moonMats || []), mm];
      }

      this.bodies.push(body);
    });

    ctx.setCameraTarget(Vector3.Zero(), 62);
  }

  private starfieldTexture(scene: any): DynamicTexture {
    const size = 2048;
    const dt = new DynamicTexture('stars', { width: size, height: size / 2 }, scene, false);
    const c = dt.getContext() as CanvasRenderingContext2D;
    c.fillStyle = '#000308';
    c.fillRect(0, 0, size, size / 2);
    // nebula wash
    for (let i = 0; i < 26; i++) {
      const x = Math.random() * size, y = Math.random() * size / 2;
      const r = 90 + Math.random() * 320;
      const g = c.createRadialGradient(x, y, 0, x, y, r);
      const hue = Math.random() < 0.5 ? '120,60,200' : '30,90,190';
      g.addColorStop(0, `rgba(${hue},0.16)`);
      g.addColorStop(1, 'rgba(0,0,0,0)');
      c.fillStyle = g;
      c.fillRect(x - r, y - r, r * 2, r * 2);
    }
    for (let i = 0; i < 9000; i++) {
      const x = Math.random() * size, y = Math.random() * size / 2;
      const b = Math.pow(Math.random(), 3.2);
      const r = b * 1.7 + 0.25;
      const t = Math.random();
      const col = t < 0.72
        ? `rgba(255,255,255,${0.25 + b})`
        : t < 0.88 ? `rgba(180,205,255,${0.25 + b})` : `rgba(255,205,160,${0.25 + b})`;
      c.fillStyle = col;
      c.beginPath(); c.arc(x, y, r, 0, 6.284); c.fill();
    }
    dt.update();
    return dt;
  }

  private ringTexture(scene: any, r: number): DynamicTexture {
    const w = 1024;
    const dt = new DynamicTexture('ring', { width: w, height: w }, scene, false);
    const c = dt.getContext() as CanvasRenderingContext2D;
    c.clearRect(0, 0, w, w);
    const cx = w / 2, cy = w / 2;
    for (let i = 0; i < 620; i++) {
      const t = Math.random();
      const rad = cx * (0.42 + t * 0.56);
      // Cassini-style gaps
      const gap = Math.sin(t * 34) * 0.5 + 0.5;
      const a = (0.05 + Math.random() * 0.4) * gap * (1 - Math.abs(t - 0.5) * 0.7);
      const g = 200 + Math.random() * 45;
      c.strokeStyle = `rgba(${g},${g - 22},${g - 55},${a})`;
      c.lineWidth = 0.7 + Math.random() * 2.6;
      c.beginPath(); c.arc(cx, cy, rad, 0, 6.284); c.stroke();
    }
    dt.update();
    dt.hasAlpha = true;
    return dt;
  }

  update(dt: number, ctx: WorldContext): void {
    this.t += dt * this.p.timeScale;
    const cam = ctx.camera;
    const cp = cam.position;

    this.starMat.setVector3('camPos', cp);
    this.starMat.setFloat('time', this.t);
    this.star.rotation.y += dt * 0.02;
    const cmat = (this as any)._coronaMat as ShaderMaterial;
    if (cmat) cmat.setVector3('camPos', cp);

    for (const b of this.bodies) {
      b.angle += dt * b.orbitSpeed * 0.12 * this.p.timeScale * this.p.orbitSpeed;
      b.root.position.set(Math.cos(b.angle) * b.orbitR, 0, Math.sin(b.angle) * b.orbitR);
      b.mesh.rotation.y += dt * b.spin * this.p.timeScale;

      b.mat.setVector3('camPos', cp);
      b.mat.setVector3('sunPos', Vector3.Zero());
      b.mat.setFloat('time', this.t);
      b.mat.setFloat('detail', this.p.detail);
      b.mat.setFloat('cloudAmt', this.p.clouds);
      b.mat.setFloat('cityLights', b.name === 'Terrapor' ? this.p.lights : 0);

      if (b.atmoMat) {
        b.atmoMat.setVector3('camPos', cp);
        b.atmoMat.setVector3('sunPos', Vector3.Zero());
      }
      for (const mm of ((b as any).moonMats || []) as ShaderMaterial[]) {
        mm.setVector3('camPos', cp);
        mm.setVector3('sunPos', Vector3.Zero());
        mm.setFloat('time', this.t);
        mm.setFloat('detail', this.p.detail);
        mm.setFloat('cloudAmt', 0);
        mm.setFloat('cityLights', 0);
      }
      for (const m of b.moons) m.pivot.rotation.y += dt * m.speed * this.p.timeScale;
    }
  }

  getParams(): WorldParam[] {
    return [
      { key: 'timeScale', label: 'Time Scale', min: 0, max: 6, step: 0.05, value: this.p.timeScale, unit: '×' },
      { key: 'orbitSpeed', label: 'Orbital Rate', min: 0, max: 4, step: 0.05, value: this.p.orbitSpeed, unit: '×' },
      { key: 'detail', label: 'Surface Detail', min: 0.2, max: 2.5, step: 0.05, value: this.p.detail },
      { key: 'clouds', label: 'Cloud Cover', min: 0, max: 1.6, step: 0.05, value: this.p.clouds },
      { key: 'lights', label: 'City Lights', min: 0, max: 3, step: 0.05, value: this.p.lights },
      { key: 'exposure', label: 'Exposure', min: 0.3, max: 2.5, step: 0.05, value: this.p.exposure }
    ];
  }

  getActions(): WorldAction[] {
    return this.bodies.map((b) => ({ key: 'goto:' + b.name, label: b.name, glyph: '🪐' }));
  }

  runAction(key: string, ctx: WorldContext): void {
    if (key.startsWith('goto:')) {
      const n = key.slice(5);
      const b = this.bodies.find((x) => x.name === n);
      if (b) {
        const r = (b.mesh.getBoundingInfo().boundingSphere.radius) * 5.5;
        ctx.camera.setTarget(b.root.position.clone());
        ctx.camera.radius = Math.max(r, 5);
      }
    }
  }

  setParam(key: string, value: number): void {
    (this.p as any)[key] = value;
  }

  getStats(): Record<string, string> {
    return {
      'Planets': String(this.bodies.length),
      'Moons': String(this.bodies.reduce((s, b) => s + b.moons.length, 0)),
      'Surfaces': 'Procedural FBM',
      'Star': 'G-type, granulated'
    };
  }

  dispose(): void {
    this.bodies.forEach((b) => { b.root.dispose(false, true); b.mat.dispose(); });
    this.bodies = [];
    this.star?.dispose();
    this.starMat?.dispose();
    this.stars?.dispose();
    this.light?.dispose();
  }
}
