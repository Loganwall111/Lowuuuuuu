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
import { applyPlanetMap, PLANET_MAP_UNIFORMS, PLANET_MAP_SAMPLERS } from '../PlanetMaps';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { Effect } from '@babylonjs/core/Materials/effect';
import { PointLight } from '@babylonjs/core/Lights/pointLight';
import { TransformNode } from '@babylonjs/core/Meshes/transformNode';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import { DynamicTexture } from '@babylonjs/core/Materials/Textures/dynamicTexture';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import { starfieldTexture, ringTexture } from '../Textures';
import { PLANET_SHADER, registerPlanetShader, PlanetKind } from '../shaders/PlanetShader';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';

/* --------------------------- planet shader --------------------------- */



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

    registerPlanetShader();
    Effect.ShadersStore['atmoVertexShader'] = ATMO_VERT;
    Effect.ShadersStore['atmoFragmentShader'] = ATMO_FRAG;

    // ---- skybox of stars ----
    this.stars = MeshBuilder.CreateSphere('sky', { diameter: 1800, segments: 32, sideOrientation: 1 }, scene);
    const skyMat = new StandardMaterial('skyMat', scene);
    skyMat.emissiveTexture = starfieldTexture(scene);
    skyMat.diffuseColor = Color3.Black();
    skyMat.specularColor = Color3.Black();
    skyMat.backFaceCulling = false;
    skyMat.disableLighting = true;
    this.stars.material = skyMat;
    this.stars.infiniteDistance = true;
    this.stars.isPickable = false;

    // ---- central star ----
    this.star = MeshBuilder.CreateSphere('star', { diameter: 9, segments: 64 }, scene);
    this.starMat = new ShaderMaterial('starM', scene, PLANET_SHADER, {
      attributes: ['position', 'normal', 'uv'],
      uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'time', 'seed',
                 'ptype', 'tintA', 'tintB', 'detail', 'cloudAmt', 'cityLights',
                 'radius', 'isStar', ...PLANET_MAP_UNIFORMS],
      samplers: PLANET_MAP_SAMPLERS
    });
    // Stars are self-luminous and take the isStar path, but the uniform must
    // still be bound or the sampler reads garbage.
    this.starMat.setFloat('useMap', 0);
    this.starMat.setFloat('isStar', 1);
    this.starMat.setFloat('ptype', PlanetKind.Star);
    this.starMat.setFloat('seed', 4.2);
    this.starMat.setFloat('detail', 1.0);
    this.starMat.setFloat('cloudAmt', 0);
    this.starMat.setFloat('cityLights', 0);
    this.starMat.setFloat('radius', 4.5);
    this.starMat.setVector3('sunPos', Vector3.Zero());
    this.starMat.setColor3('tintA', new Color3(1.0, 0.55, 0.12));
    this.starMat.setColor3('tintB', new Color3(1.0, 0.98, 0.86));
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

      const mat = new ShaderMaterial('m_' + cfg.name, scene, PLANET_SHADER, {
        attributes: ['position', 'normal', 'uv'],
        uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'time',
                   'seed', 'ptype', 'tintA', 'tintB', 'detail', 'cloudAmt', 'cityLights', 'radius', 'isStar',
                   ...PLANET_MAP_UNIFORMS],
        samplers: PLANET_MAP_SAMPLERS
      });
      applyPlanetMap(mat, cfg.type as PlanetKind, scene);
      mat.setFloat('seed', i * 3.77 + 1.3);
      mat.setFloat('ptype', cfg.type);
      mat.setColor3('tintA', new Color3(...cfg.a));
      mat.setColor3('tintB', new Color3(...cfg.b));
      mat.setFloat('radius', cfg.r);
      mat.setFloat('isStar', 0);
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
        rm.diffuseTexture = ringTexture(scene);
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
        const mm = new ShaderMaterial('mm', scene, PLANET_SHADER, {
          attributes: ['position', 'normal', 'uv'],
          uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'time',
                     'seed', 'ptype', 'tintA', 'tintB', 'detail', 'cloudAmt', 'cityLights', 'radius', 'isStar',
                     ...PLANET_MAP_UNIFORMS],
          samplers: PLANET_MAP_SAMPLERS
        });
        mm.setFloat('useMap', 0);
        mm.setFloat('seed', i * 9.1 + m * 4.3 + 20.0);
        mm.setFloat('ptype', 0);
        mm.setColor3('tintA', new Color3(0.28, 0.26, 0.25));
        mm.setColor3('tintB', new Color3(0.62, 0.60, 0.57));
        mm.setFloat('radius', mr);
        mm.setFloat('isStar', 0);
        moon.material = mm;
        body.moons.push({ pivot, speed: 0.5 + Math.random() * 0.9 });
        (body as any).moonMats = [...((body as any).moonMats || []), mm];
      }

      this.bodies.push(body);
    });

    ctx.setCameraTarget(Vector3.Zero(), 62);
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
