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
import { ringTexture } from '../Textures';
import { PLANET_SHADER, registerPlanetShader, PlanetKind } from '../shaders/PlanetShader';
import {
  CORONA_VERT, CORONA_FRAG, GLARE_VERT, GLARE_FRAG,
  CORONA_UNIFORMS, GLARE_UNIFORMS, coronaFor
} from '../shaders/SunShader';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';
import { ImpactorSystem, type ImpactTarget } from '../systems/ImpactorSystem';
import { THROWABLES, throwableById } from '../systems/ThrowableSystem';
import { SettlerSystem } from '../systems/SettlerSystem';

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
uniform vec3 planetCenter;
uniform float planetRadius;   // surface radius, world units
uniform float atmoRadius;     // top of the atmosphere, world units

/**
 * Volumetric atmospheric scattering.
 *
 * The previous version of this shader was a fake: a Fresnel rim term with a
 * forward-scatter fudge. It produced a hard-edged shell because the opacity
 * came from the angle of the mesh surface, not from how much gas the view
 * ray actually passed through. Anything with a fixed geometric edge reads as
 * a solid object, which is exactly the "solid geometric aura" problem.
 *
 * This version integrates optical depth along the real view ray, so the limb
 * fades because the air genuinely thins out. Two species are tracked:
 *
 *   Rayleigh - air molecules, scale height 8.0 km. Scatters short
 *              wavelengths far more strongly (the 1/lambda^4 law), which is
 *              why the sky is blue and why sunsets redden.
 *   Mie      - dust and aerosol, scale height 1.2 km. Nearly wavelength
 *              neutral and strongly forward-biased, which is what makes the
 *              gold halo hugging the sun.
 *
 * Scale heights are expressed as a fraction of planet radius so the same
 * shader works for a moon and a gas giant. Earth is 8.0/6371 of its radius.
 */

// Rayleigh coefficients at sea level, per the 1/lambda^4 relationship
// (650/550/440 nm). The ratio is what matters, not the absolute scale.
const vec3 BETA_R = vec3(5.8e-3, 13.5e-3, 33.1e-3);
// Mie is essentially grey - dust does not care much about wavelength.
const vec3 BETA_M = vec3(4.0e-3);

const float H_RAYLEIGH = 8.0 / 6371.0;   // 8.0 km on an Earth-sized world
const float H_MIE      = 1.2 / 6371.0;   // 1.2 km

/** Ray/sphere intersection. Returns (near, far); far < near means a miss. */
vec2 raySphere(vec3 ro, vec3 rd, float rad){
  float b = dot(ro, rd);
  float c = dot(ro, ro) - rad * rad;
  float d = b * b - c;
  if (d < 0.0) return vec2(1.0, -1.0);
  float s = sqrt(d);
  return vec2(-b - s, -b + s);
}

/** Rayleigh phase: gentle, symmetric fore/aft lobes. */
float phaseRayleigh(float mu){
  return 0.0596831 * (1.0 + mu * mu);
}

/**
 * Henyey-Greenstein phase for Mie. g near 0.76 is a standard hazy-atmosphere
 * value and produces the tight forward lobe seen as a halo around the sun.
 */
float phaseMie(float mu, float g){
  float g2 = g * g;
  float denom = 1.0 + g2 - 2.0 * g * mu;
  return (1.0 - g2) / (12.566371 * max(pow(denom, 1.5), 1e-4));
}

void main(void){
  vec3 ro = camPos - planetCenter;
  vec3 rd = normalize(vWorld - camPos);
  vec3 L  = normalize(sunPos - planetCenter);

  float Ra = max(atmoRadius, planetRadius * 1.001);
  vec2 hit = raySphere(ro, rd, Ra);
  if (hit.y < hit.x) { gl_FragColor = vec4(0.0); return; }

  // Start at the atmosphere, stop at the surface if the ray hits it.
  float tNear = max(hit.x, 0.0);
  float tFar  = hit.y;
  vec2 ground = raySphere(ro, rd, planetRadius);
  if (ground.y >= ground.x && ground.x > 0.0) tFar = min(tFar, ground.x);
  if (tFar <= tNear) { gl_FragColor = vec4(0.0); return; }

  // Thickness of the shell, used to normalise the scale heights.
  float shell = max(Ra - planetRadius, 1e-5);
  float hR = max(H_RAYLEIGH * planetRadius, shell * 0.06);
  float hM = max(H_MIE      * planetRadius, shell * 0.012);

  // ---- primary raymarch ----
  // Four samples, per the brief. Few samples are enough because density is
  // smooth and exponential; the cost is in the light march, not this one.
  const int STEPS = 4;
  float seg = (tFar - tNear) / float(STEPS);

  vec3 sumR = vec3(0.0);
  vec3 sumM = vec3(0.0);
  float odR = 0.0;   // accumulated optical depth toward the viewer
  float odM = 0.0;

  for (int i = 0; i < STEPS; i++){
    float t = tNear + seg * (float(i) + 0.5);
    vec3 pos = ro + rd * t;
    float alt = max(length(pos) - planetRadius, 0.0);

    float dR = exp(-alt / hR) * seg;
    float dM = exp(-alt / hM) * seg;
    odR += dR;
    odM += dM;

    // ---- light march: how much sun reaches this sample ----
    // Two samples toward the star is enough to get the terminator reddening
    // right without a nested loop blowing the frame budget.
    vec2 lh = raySphere(pos, L, Ra);
    float lodR = 0.0;
    float lodM = 0.0;
    if (lh.y > 0.0){
      float lseg = lh.y / 2.0;
      for (int j = 0; j < 2; j++){
        vec3 lp = pos + L * (lseg * (float(j) + 0.5));
        float la = max(length(lp) - planetRadius, 0.0);
        lodR += exp(-la / hR) * lseg;
        lodM += exp(-la / hM) * lseg;
      }
    }

    // Beer-Lambert both ways: sun to sample, then sample to eye.
    vec3 tau = BETA_R * (odR + lodR) + BETA_M * 1.1 * (odM + lodM);
    vec3 att = exp(-tau * (1.0 / max(shell, 1e-5)) * 12.0);

    // Shadowed samples contribute nothing - this is what carves the
    // terminator instead of lighting the whole shell uniformly.
    float lit = smoothstep(-0.35, 0.15, dot(normalize(pos), L));

    sumR += dR * att * lit;
    sumM += dM * att * lit;
  }

  float mu = dot(rd, L);
  float pR = phaseRayleigh(mu);
  float pM = phaseMie(mu, 0.76);

  float norm = 1.0 / max(shell, 1e-5);
  vec3 col = (sumR * BETA_R * pR + sumM * BETA_M * pM) * norm * 620.0;

  // Tint toward the per-planet colour without discarding the physics: the
  // scattering decides the shape and the artist decides the hue.
  col = mix(col, col * atmoColor * 1.8, 0.55);

  // Opacity is the integrated density, so the limb fades because the gas
  // genuinely runs out. No geometric edge anywhere.
  float dens = (odR + odM * 1.4) * norm;
  float a = 1.0 - exp(-dens * 2.6);
  a *= smoothstep(0.0, 0.06, dens);

  // power stays meaningful as an artistic limb-sharpness control.
  a = pow(clamp(a, 0.0, 1.0), max(power * 0.28, 0.25));

  col = col / (col + vec3(1.0));            // keep the halo from clipping
  gl_FragColor = vec4(col, clamp(a, 0.0, 1.0));
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
  /** Visual radius, kept so impact mass can be derived from real size. */
  visualR: number;
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

/** Terrapor is the Earth-like world, so it is the yardstick for mass. */
const EARTH_VISUAL_R = 1.15;
/** ...and the one with people on it. */
const INHABITED = 'Terrapor';
const INHABITED_SEED = 40917;

export class PlanetaryWorld implements World {
  id = 'planetary';
  name = 'Star Systems';

  private bodies: Body[] = [];
  /** Things thrown at the planets, and what they did when they arrived. */
  private impactor = new ImpactorSystem();
  /** What the next throw will be. */
  private armed = 'asteroid';
  private lastImpactNote = '';
  /** The people who live on Terrapor, and what they make of you. */
  private settlers = new SettlerSystem();
  private settlerAnchor = new Vector3(0, 0, 0);
  private star!: Mesh;
  private starMat!: ShaderMaterial;
  private light!: PointLight;
  private stars!: Mesh;
  private t = 0;

  private p = { timeScale: 1.0, detail: 1.0, clouds: 1.0, lights: 1.0, exposure: 1.0, orbitSpeed: 1.0 };

  async build(ctx: WorldContext): Promise<void> {
    const scene = ctx.scene;
    scene.clearColor = new Color4(0.002, 0.004, 0.012, 1);

    // Throwing things is core to this place, so the impactor is attached
    // for the lifetime of the world rather than spun up on first use.
    this.settlers.attach(scene);

    this.impactor.attach(scene, (e) => {
      this.lastImpactNote =
        e.projectile.spec.name + ' → ' + e.target.id + ': ' +
        e.result.megatons.toExponential(1) + ' Mt, ' + e.result.description;

      // The people living there notice. How much depends on what you did,
      // taken from the same outcome the physics produced rather than a
      // separate table that could disagree with it.
      const SEVERITY: Record<string, number> = {
        bounce: 0, crater: 0.05, regional: 0.4,
        extinction: 1.2, 'crust-loss': 2, shattered: 2
      };
      this.settlers.witnessed(SEVERITY[e.result.outcome] ?? 0);
    });

    registerPlanetShader();
    Effect.ShadersStore['atmoVertexShader'] = ATMO_VERT;
    Effect.ShadersStore['atmoFragmentShader'] = ATMO_FRAG;

    // ---- no skybox ----
    // There is deliberately no sky object here. Any finite mesh wrapped around
    // the camera shows its own triangle silhouettes against the star volume,
    // which is what the hard-edged wedges were: not UV seams, but the
    // icosphere's own faces. Space is rendered by the three real point-cloud
    // shells in LayeredSky, which App rebuilds for every world.

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
    this.starMat.setFloat('oceanDepth', 0);
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

    // ---- corona ----
    // Previously this was the planetary atmosphere shader pointed at the
    // star, which gives a smooth halo and reads as a plain ball. A real
    // corona is turbulent: streamers, prominences, and a faint outer glow
    // that extends for several radii.
    const look = coronaFor('yellow');
    const STAR_R = 4.5;
    const SHELL_R = STAR_R * 5.4;

    const corona = MeshBuilder.CreateSphere('corona',
      { diameter: SHELL_R * 2, segments: 64 }, scene);
    const cm = new ShaderMaterial('coronaM', scene, {
      vertexSource: CORONA_VERT, fragmentSource: CORONA_FRAG
    }, {
      attributes: ['position', 'normal'],
      uniforms: CORONA_UNIFORMS,
      needAlphaBlending: true
    });
    cm.setVector3('camPos', Vector3.Zero());
    cm.setVector3('starCenter', Vector3.Zero());
    cm.setFloat('time', 0);
    cm.setFloat('starRadius', STAR_R);
    cm.setFloat('shellRadius', SHELL_R);
    cm.setColor3('hotColor', new Color3(look.hot[0], look.hot[1], look.hot[2]));
    cm.setColor3('midColor', new Color3(look.mid[0], look.mid[1], look.mid[2]));
    cm.setColor3('coolColor', new Color3(look.cool[0], look.cool[1], look.cool[2]));
    cm.setFloat('intensity', look.intensity);
    cm.setFloat('turbulence', look.turbulence);
    cm.setFloat('streamers', look.streamers);
    cm.setFloat('prominence', look.prominence);
    // Additive so the aura only ever adds light - it can never paint a
    // black shell over the sky if a value goes out of range.
    cm.alphaMode = 1;
    cm.backFaceCulling = false;
    cm.disableDepthWrite = true;
    corona.material = cm;
    corona.isPickable = false;
    corona.renderingGroupId = 0;
    (this as any)._coronaMat = cm;

    // ---- glare ----
    // A camera-facing disc in front of the star for the raw searing core
    // and its diffraction spikes.
    const glare = MeshBuilder.CreatePlane('sunGlare',
      { size: SHELL_R * 4.2 }, scene);
    const gm = new ShaderMaterial('glareM', scene, {
      vertexSource: GLARE_VERT, fragmentSource: GLARE_FRAG
    }, {
      attributes: ['position', 'uv'],
      uniforms: GLARE_UNIFORMS,
      needAlphaBlending: true
    });
    gm.setFloat('time', 0);
    gm.setColor3('glareColor',
      new Color3(look.glare[0], look.glare[1], look.glare[2]));
    gm.setFloat('intensity', look.intensity * 0.85);
    gm.setFloat('spikes', 0.75);
    gm.alphaMode = 1;
    gm.backFaceCulling = false;
    gm.disableDepthWrite = true;
    glare.material = gm;
    glare.isPickable = false;
    glare.billboardMode = 7;   // always faces the camera
    (this as any)._glareMat = gm;
    (this as any)._glare = glare;

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
      applyPlanetMap(mat, cfg.type as PlanetKind, scene, Math.floor(i * 2654435761 + 101));
      mat.setFloat('seed', i * 3.77 + 1.3);
      mat.setFloat('ptype', cfg.type);
      mat.setColor3('tintA', new Color3(...cfg.a));
      mat.setColor3('tintB', new Color3(...cfg.b));
      mat.setFloat('radius', cfg.r);
      mat.setFloat('isStar', 0);
      mesh.material = mat;

      const body: Body = {
        root, mesh, mat,
        orbitR: cfg.orbit, visualR: cfg.r, orbitSpeed: cfg.speed,
        angle: Math.random() * Math.PI * 2,
        spin: 0.15 + Math.random() * 0.35,
        name: cfg.name, moons: []
      };

      if (cfg.atmo) {
        const atmo = MeshBuilder.CreateSphere('a_' + cfg.name, { diameter: cfg.r * 2.16, segments: 48 }, scene);
        atmo.parent = root;
        const am = new ShaderMaterial('am_' + cfg.name, scene, 'atmo', {
          attributes: ['position', 'normal'],
          uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos',
                     'atmoColor', 'power',
                     'planetCenter', 'planetRadius', 'atmoRadius'],
          needAlphaBlending: true
        });
        am.setColor3('atmoColor', new Color3(...cfg.atmo));
        am.setFloat('power', 3.0);
        // The volumetric march needs the real geometry of the shell it is
        // integrating through. The mesh diameter is cfg.r * 2.16, so the
        // atmosphere tops out at 1.08 planet radii.
        am.setFloat('planetRadius', cfg.r);
        am.setFloat('atmoRadius', cfg.r * 1.08);
        am.setVector3('planetCenter', Vector3.Zero());
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
        mm.setFloat('oceanDepth', 0);
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
    // The corona boils and its streamers drift outward, so it must be fed
    // time as well as the camera.
    const cmat = (this as any)._coronaMat as ShaderMaterial;
    if (cmat) {
      cmat.setVector3('camPos', cp);
      cmat.setFloat('time', this.t);
    }
    const gmat = (this as any)._glareMat as ShaderMaterial;
    if (gmat) gmat.setFloat('time', this.t);

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
      // The Exposure slider existed in the options panel but was never sent
      // to the shader, so dragging it did nothing at all.
      b.mat.setFloat('exposure', this.p.exposure);

      if (b.atmoMat) {
        b.atmoMat.setVector3('camPos', cp);
        b.atmoMat.setVector3('sunPos', Vector3.Zero());
        // The planet orbits, so the centre the raymarch integrates around
        // moves every frame. A stale centre would tear the atmosphere off
        // the planet exactly the way a stale camera tore the accretion disk
        // off its horizon.
        b.atmoMat.setVector3('planetCenter', b.mesh.getAbsolutePosition());
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

    // Projectiles fly under the gravity of every planet at once, so a throw
    // can be slung around one world into another.
    this.impactor.update(dt * Math.max(0.05, this.p.timeScale), this.targets());

    // The inhabited world carries its people with it as it orbits, so they
    // stay on the ground rather than being left behind in empty space.
    const home = this.bodies.find((x) => x.name === INHABITED);
    if (home) {
      if (!this.settlers.settlers.length) {
        this.settlers.populate(
          INHABITED_SEED, home.mesh.getAbsolutePosition(),
          home.mesh.getBoundingInfo().boundingSphere.radiusWorld, 6);
        this.settlerAnchor = home.mesh.getAbsolutePosition().clone();
      } else {
        const now = home.mesh.getAbsolutePosition();
        const drift = now.subtract(this.settlerAnchor);
        if (drift.lengthSquared() > 1e-9) {
          for (const st of this.settlers.settlers) {
            st.position.addInPlace(drift);
            st.mesh?.position.copyFrom(st.position);
          }
          this.settlerAnchor.copyFrom(now);
        }
      }
    }
    this.settlers.update(dt);
  }

  /** The planets, described the way the impact maths needs them. */
  private targets(): ImpactTarget[] {
    return this.bodies.map((b) => ({
      id: b.name,
      position: b.mesh.getAbsolutePosition(),
      radius: b.mesh.getBoundingInfo().boundingSphere.radiusWorld,
      // Mass and physical radius are derived from the planet's own visual
      // size against an Earth-sized reference, at constant density: a body
      // twice Earth's radius masses eight times as much and has roughly
      // thirty times the binding energy. So the gas giants genuinely shrug
      // off what shatters the little rocky worlds, rather than every planet
      // being equally destructible.
      mass: 5.97e24 * Math.pow(b.visualR / EARTH_VISUAL_R, 3),
      physicalRadius: 6.371e6 * (b.visualR / EARTH_VISUAL_R)
    }));
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
    return [
      ...this.bodies.map((b) => ({ key: 'goto:' + b.name, label: b.name, glyph: '🪐' })),
      // Arm something, then throw it. Two clicks from arriving to watching a
      // moon hit a planet.
      ...THROWABLES.map((t) => ({
        key: 'arm:' + t.id,
        label: (this.armed === t.id ? '● ' : '') + t.name,
        glyph: t.glyph
      })),
      { key: 'throw', label: 'Throw at nearest', glyph: '🎯' },
      { key: 'clear-throws', label: 'Clear projectiles', glyph: '🧹' },
      { key: 'talk', label: 'Hail the inhabitants', glyph: '💬' }
    ];
  }

  runAction(key: string, ctx: WorldContext): void {
    if (key.startsWith('arm:')) {
      this.armed = key.slice(4);
      return;
    }
    if (key === 'clear-throws') { this.impactor.clear(); return; }
    if (key === 'talk') {
      const said = this.settlers.talkTo(ctx.camera.position, 1e9);
      this.lastImpactNote = said ?? 'Nobody within earshot.';
      return;
    }
    if (key === 'throw') {
      // Thrown from the camera toward whatever you are looking at, so aim
      // is yours and a bad throw genuinely misses.
      const from = ctx.camera.position.clone();
      const dir = ctx.camera.getTarget().subtract(from);
      this.impactor.throwAt(this.armed, from, dir, 55);
      return;
    }
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
      ...this.impactor.stats(),
      ...this.settlers.stats(),
      'Armed': throwableById(this.armed)?.name ?? '—',
      'Last event': this.lastImpactNote || '—',
      'Planets': String(this.bodies.length),
      'Moons': String(this.bodies.reduce((s, b) => s + b.moons.length, 0)),
      'Surfaces': 'Procedural FBM',
      'Star': 'G-type, granulated'
    };
  }

  dispose(): void {
    this.impactor.dispose();
    this.settlers.dispose();
    this.bodies.forEach((b) => { b.root.dispose(false, true); b.mat.dispose(); });
    this.bodies = [];
    this.star?.dispose();
    this.starMat?.dispose();
    this.stars?.dispose();
    this.light?.dispose();
  }
}
