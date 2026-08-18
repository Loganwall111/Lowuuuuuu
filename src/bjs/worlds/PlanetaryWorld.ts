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
import { GLSL_NOISE } from '../Noise';
import { PLANET_SHADER, registerPlanetShader, PlanetKind } from '../shaders/PlanetShader';
import { hashCell } from '../systems/IntergalacticGrid';
import {
  CORONA_VERT, CORONA_FRAG, GLARE_VERT, GLARE_FRAG,
  CORONA_UNIFORMS, GLARE_UNIFORMS, coronaFor
} from '../shaders/SunShader';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';
import { ImpactorSystem, type ImpactTarget } from '../systems/ImpactorSystem';
import { THROWABLES, throwableById } from '../systems/ThrowableSystem';
import { SettlerSystem } from '../systems/SettlerSystem';
import { AsteroidBeltSystem } from '../systems/AsteroidBelts';
import { OrbitTraffic } from '../systems/OrbitTraffic';
import { TleTraffic } from '../systems/TleTraffic';
import { stellarColor, STAR_LIFETIME } from '../systems/StellarLifecycle';
import type { SolidSphere } from '../systems/PlanetLanding';
import { renderOrigin, toRenderRef } from '../systems/RenderOrigin';

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
/** 1 on a living world, 0 on a dead one. Drives the whole character of the
 *  halo: living worlds get a deep, saturated blue with a warm sunset, dead
 *  worlds get a thin, dusty, desaturated grey that reads as harsh. */
uniform float habitable;
/** Overall atmosphere density multiplier. */
uniform float density;
/** 0 = grey haze, 1 = full spectral colour. */
uniform float saturation;
uniform vec3 planetCenter;
uniform float planetRadius;   // surface radius, world units
uniform float atmoRadius;     // top of the atmosphere, world units
/** 1 = draw animated aurora curtains over the magnetic poles. */
uniform float aurora;
/** Slow clock for the aurora drift. */
uniform float time;

${GLSL_NOISE}

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
  float hab = clamp(habitable, 0.0, 1.0);

  // Living worlds scatter blue hard (strong Rayleigh); dead worlds keep a
  // thin, dustier sky where Mie dominates and the colour washes out.
  vec3 br = BETA_R * mix(0.42, 1.0, hab);
  vec3 bm = BETA_M * mix(1.6, 1.0, hab);

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
  // Habitable atmospheres are puffier; dead ones are shallower and crisper.
  float hR = max(H_RAYLEIGH * planetRadius * mix(0.72, 1.0, hab), shell * 0.06);
  float hM = max(H_MIE      * planetRadius * mix(1.25, 1.0, hab), shell * 0.012);

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
    vec3 tau = br * (odR + lodR) + bm * 1.1 * (odM + lodM);
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
  vec3 col = (sumR * br * pR + sumM * bm * pM) * norm * 620.0;

  // Tint toward the per-planet colour without discarding the physics: the
  // scattering decides the shape and the artist decides the hue.
  col = mix(col, col * atmoColor * 1.8, 0.55);

  // Living worlds keep their spectral colour; dead worlds are pulled toward
  // a desaturated grey, which is the "harsher, celestial" look.
  float lum = dot(col, vec3(0.2126, 0.7152, 0.0722));
  col = mix(vec3(lum), col, clamp(saturation, 0.0, 1.0));

  // Opacity is the integrated density, so the limb fades because the gas
  // genuinely runs out. No geometric edge anywhere.
  float dens = (odR + odM * 1.4) * norm;
  float a = 1.0 - exp(-dens * 2.6);
  a *= smoothstep(0.0, 0.06, dens);
  a *= clamp(density, 0.0, 4.0);

  // power stays meaningful as an artistic limb-sharpness control.
  a = pow(clamp(a, 0.0, 1.0), max(power * 0.28, 0.25));

  // ---- aurora over the magnetic poles ----
  // Real aurorae hug the poles and light up on the night side, where the
  // solar wind funnels down the field lines. They read as slow ribbons of
  // green and violet, which is exactly the "alive planet" cue a bare limb
  // glow cannot give. Driven off the same noise field as the rest of the
  // shader so the curtains are one family with the planet, not decals.
  if (aurora > 0.5){
    vec3 p = normalize(vWorld - planetCenter);
    float lat = abs(p.y);
    // Poleward bias, night-side bias.
    float polar = smoothstep(0.55, 0.98, lat);
    float night = smoothstep(0.05, -0.30, dot(p, L));
    // Curtains: noise stretched along longitude so it drapes in bands.
    vec3 q = vec3(p.x * 5.0, p.y * 11.0, p.z * 5.0)
           + vec3(0.0, time * 0.35, time * 0.10);
    float curtain = fbm(q, 4, 2.2, 0.55);
    float ribbons = smoothstep(0.42, 0.95, curtain);
    // Vertical striping, the classic aurora look.
    float vert = sin(p.x * 26.0 + time * 1.15) * 0.5 + 0.5;
    float glow = polar * night * ribbons * (0.30 + 0.70 * vert);
    // Green at the base, violet where the field is strongest.
    vec3 auroraCol = mix(vec3(0.16, 0.95, 0.45), vec3(0.50, 0.25, 1.00), vert);
    col += auroraCol * glow * 0.55;
    a = clamp(a + glow * 0.25, 0.0, 1.0);
  }

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
  /** PlanetKind of this body, for gas-giant / habitable distinctions. */
  type: number;
  moons: { pivot: TransformNode; mesh: Mesh; speed: number }[];
}

export interface PlanetCfg {
  name: string; r: number; orbit: number; speed: number; type: number;
  a: [number, number, number]; b: [number, number, number];
  atmo?: [number, number, number]; clouds: number; lights: number; moons: number; ring?: boolean;
  /** The one world in the system with people on it. */
  inhabited?: boolean;
}

/**
 * The classes of world a system can produce.
 *
 * Each entry is a recipe rather than a fixed planet: radius, orbital
 * position, colour and cloud cover are all drawn from ranges, so two lava
 * worlds in different galaxies are recognisably the same KIND of place
 * without being the same object.
 */
const PLANET_CLASSES: {
  kind: string;
  type: PlanetKind;
  /** Visual radius range. */
  rMin: number; rMax: number;
  /** Surface colour endpoints, each with a per-channel jitter range. */
  a: [number, number, number]; b: [number, number, number];
  atmo?: [number, number, number];
  cloudMin: number; cloudMax: number;
  /** Chance this class carries a ring system. */
  ringChance: number;
  moonMin: number; moonMax: number;
  /** Relative likelihood of being picked. */
  weight: number;
}[] = [
  // Gas giants: big, banded, usually ringed. The showpiece worlds.
  { kind: 'gas-giant', type: PlanetKind.Gas, rMin: 2.1, rMax: 3.4,
    a: [0.72,0.58,0.40], b: [0.90,0.80,0.62],
    cloudMin: 0, cloudMax: 0, ringChance: 0.72, moonMin: 2, moonMax: 4, weight: 1.15 },
  // Ice giants: the cold blue variety of the same family.
  { kind: 'ice-giant', type: PlanetKind.Gas, rMin: 1.8, rMax: 2.7,
    a: [0.32,0.48,0.62], b: [0.62,0.78,0.88],
    cloudMin: 0, cloudMax: 0, ringChance: 0.55, moonMin: 1, moonMax: 3, weight: 0.85 },
  // Volcanic worlds: glowing magma fissures, no atmosphere worth the name.
  { kind: 'volcanic', type: PlanetKind.Lava, rMin: 0.55, rMax: 1.05,
    a: [0.35,0.12,0.08], b: [0.72,0.34,0.16],
    cloudMin: 0, cloudMax: 0.12, ringChance: 0.04, moonMin: 0, moonMax: 1, weight: 1.0 },
  // Lush terra worlds: oceans, weather, and the only ones that get cities.
  { kind: 'terra', type: PlanetKind.Terran, rMin: 0.95, rMax: 1.35,
    a: [0.20,0.40,0.15], b: [0.50,0.45,0.30], atmo: [0.25,0.50,1.00],
    cloudMin: 0.55, cloudMax: 0.95, ringChance: 0.05, moonMin: 1, moonMax: 2, weight: 0.9 },
  // Frozen worlds: bright, high albedo, thin hazy atmosphere.
  { kind: 'ice', type: PlanetKind.Ice, rMin: 0.75, rMax: 1.25,
    a: [0.66,0.78,0.88], b: [0.88,0.94,0.99], atmo: [0.60,0.80,1.00],
    cloudMin: 0.18, cloudMax: 0.45, ringChance: 0.10, moonMin: 0, moonMax: 2, weight: 1.0 },
  // Deserts and bare rock: the common, unglamorous majority.
  { kind: 'desert', type: PlanetKind.Desert, rMin: 0.70, rMax: 1.15,
    a: [0.72,0.52,0.28], b: [0.92,0.78,0.50], atmo: [0.85,0.60,0.35],
    cloudMin: 0.10, cloudMax: 0.35, ringChance: 0.05, moonMin: 0, moonMax: 1, weight: 1.0 },
  { kind: 'rocky', type: PlanetKind.Rocky, rMin: 0.55, rMax: 1.00,
    a: [0.42,0.26,0.18], b: [0.66,0.44,0.30],
    cloudMin: 0, cloudMax: 0.08, ringChance: 0.03, moonMin: 0, moonMax: 2, weight: 1.1 }
];

const SYS_SYLL_A = ['Cin', 'Vas', 'Terr', 'Rho', 'Oph', 'Kel', 'Sil', 'Mor',
  'Aur', 'Tha', 'Ven', 'Xan', 'Bel', 'Nyx', 'Cor', 'Zel', 'Ith', 'Dra', 'Sol',
  'Ery', 'Pha', 'Qel', 'Tyr', 'Ume'];
const SYS_SYLL_B = ['dara', 'ara', 'apor', 'gar', 'ion', 'vara', 'ex', 'ata',
  'elis', 'ros', 'une', 'thus', 'mir', 'aque', 'vus', 'yn', 'oda', 'ander',
  'is', 'ophe', 'ux', 'een'];

/** Proper Roman numerals, so worlds read as Kelvara IV rather than Kel L4. */
const ROMAN = ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X'];

/**
 * Builds a star system's planets from the coordinates of the 260,000-unit
 * galaxy cell it sits in.
 *
 * The point is that nothing here is authored. Feed it a different cell and
 * you get a different system - different count, different classes, different
 * colours, different rings - but feed it the SAME cell and you get exactly
 * the same system back, forever, with nothing stored. That determinism is
 * what lets an infinite universe have specific places in it.
 */
export function planetsForCell(ix: number, iy: number, iz: number): PlanetCfg[] {
  const h = hashCell(ix, iy, iz);
  // One hash, many independent streams. Reusing the same hash directly for
  // several decisions correlates them - every ringed planet would also be
  // the largest, and so on - so each draw takes its own channel.
  let c = 0;
  const nx = () => {
    let v = (h + Math.imul(++c, 2654435761)) >>> 0;
    v = Math.imul(v ^ (v >>> 15), 2246822519) >>> 0;
    v = (v ^ (v >>> 13)) >>> 0;
    return v / 4294967296;
  };
  const range = (lo: number, hi: number) => lo + nx() * (hi - lo);

  const total = PLANET_CLASSES.reduce((s, k) => s + k.weight, 0);
  const pick = () => {
    let t = nx() * total;
    for (const k of PLANET_CLASSES) { t -= k.weight; if (t <= 0) return k; }
    return PLANET_CLASSES[PLANET_CLASSES.length - 1];
  };

  const count = 5 + Math.floor(nx() * 4);          // 5..8 worlds
  const name = SYS_SYLL_A[Math.floor(nx() * SYS_SYLL_A.length)] +
               SYS_SYLL_B[Math.floor(nx() * SYS_SYLL_B.length)];

  // Every system gets exactly one habitable world, placed somewhere in the
  // middle of the run where a temperate orbit belongs. Left purely to the
  // weighted roll, most systems come out with none - the origin cell rolled
  // six worlds and not one of them was terra - which would mean no
  // settlements, no city lights, and nowhere to land and meet anyone.
  const habitableAt = 1 + Math.floor(nx() * Math.max(1, count - 2));

  const out: PlanetCfg[] = [];
  let orbit = 13 + nx() * 6;
  for (let i = 0; i < count; i++) {
    const k = i === habitableAt
      ? PLANET_CLASSES.find((q) => q.kind === 'terra')!
      : pick();
    // Orbits widen outward roughly geometrically, like a real system, so the
    // inner worlds are crowded and the outer ones are lonely.
    orbit += 7 + orbit * range(0.24, 0.46);
    const jitter = (m: number): number => Math.min(1, Math.max(0, m + (nx() - 0.5) * 0.12));
    const a = k.a.map(jitter) as [number, number, number];
    const b = k.b.map(jitter) as [number, number, number];
    out.push({
      name: name + ' ' + (ROMAN[i] ?? String(i + 1)),
      r: range(k.rMin, k.rMax),
      orbit,
      // Kepler: further out is slower. This is why the inner worlds visibly
      // race and the gas giants barely creep.
      speed: 0.62 * Math.pow(14 / orbit, 1.5),
      type: k.type,
      a, b,
      atmo: k.atmo,
      clouds: range(k.cloudMin, k.cloudMax),
      // Only the ONE inhabited world lights up at night. Keying this on the
      // class instead let a second, randomly-rolled terra world glow with
      // city lights while having no settlers on it.
      lights: i === habitableAt ? range(0.7, 1.3) : 0,
      moons: Math.floor(range(k.moonMin, k.moonMax + 0.999)),
      ring: nx() < k.ringChance,
      inhabited: i === habitableAt
    });
  }
  return out;
}

/** The system at the origin cell, which is where the planetary world opens. */
/**
 * The home system.
 *
 * Generated from the origin cell like every other system, then its
 * habitable world is replaced with a proper Earth: deep blue oceans, green
 * and tan continents, heavy white cloud. The player should recognise home
 * the moment they see it, rather than it being one more procedural terra.
 */
const PLANETS: PlanetCfg[] = (() => {
  const gen = planetsForCell(0, 0, 0);
  const i = gen.findIndex((q) => q.inhabited);
  if (i >= 0) {
    gen[i] = {
      ...gen[i],
      name: 'Earth',
      r: 1.15,
      type: PlanetKind.Terran,
      // Ocean blue against continental green; the shader mixes between
      // them by landmass, so these two are what give Earth its colour.
      a: [0.06, 0.22, 0.52],
      b: [0.24, 0.44, 0.18],
      atmo: [0.35, 0.58, 1.00],
      clouds: 0.85,
      lights: 1.0,
      moons: 1
    };
  }
  return gen;
})();

/** The habitable world is the yardstick for mass, whatever it is called. */
const EARTH_VISUAL_R = (PLANETS.find((q) => q.inhabited) ?? PLANETS[0]).r;
/** ...and the one with people on it. */
const INHABITED = (PLANETS.find((q) => q.inhabited) ?? PLANETS[0]).name;
const INHABITED_SEED = 40917;

/**
 * Size of the sun's glare billboard, world units.
 *
 * Module-level because the build pass creates the quad at this size and
 * the update pass has to scale it back down relative to it.
 */
export const GLARE_SIZE = 4.5 * 5.4 * 4.2;

export class PlanetaryWorld implements World {
  id = 'planetary';
  name = 'Star Systems';
  /** Configured glare intensity, before distance easing. */
  private glareIntensity = 1;

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
  private belts = new AsteroidBeltSystem();
  /** Earth's orbital neighbourhood: the ISS, satellites, Hubble, Webb, Apollo. */
  private orbitTraffic = new OrbitTraffic();
  /** The ten-thousand-object TLE satellite cloud around Earth. */
  private tleTraffic = new TleTraffic();
  private t = 0;
  private systemCenter = new Vector3();
  private logicalSystemCenter = new Vector3();
  private localPlanets: PlanetCfg[] = PLANETS;
  private inhabitedName = INHABITED;
  private referenceRadius = EARTH_VISUAL_R;

  private p = { timeScale: 1.0, detail: 1.0, clouds: 1.0, lights: 1.0, exposure: 1.0, orbitSpeed: 1.0 };

  async build(ctx: WorldContext): Promise<void> {
    const scene = ctx.scene;
    this.logicalSystemCenter.copyFrom(ctx.focus?.position ?? Vector3.Zero());
    toRenderRef(this.logicalSystemCenter,this.systemCenter);
    if (ctx.focus) {
      const cell = 260000;
      this.localPlanets = planetsForCell(
        Math.floor(ctx.focus.position.x / cell),
        Math.floor(ctx.focus.position.y / cell),
        Math.floor(ctx.focus.position.z / cell));
    } else this.localPlanets = PLANETS;
    const inhabited = this.localPlanets.find((p) => p.inhabited) ?? this.localPlanets[0];
    this.inhabitedName = inhabited?.name ?? INHABITED;
    this.referenceRadius = inhabited?.r ?? EARTH_VISUAL_R;
    // INK-BLACK VACUUM. All the colour in ordinary space comes from the
    // galaxy fog volume, never from the clear colour - lifting this to fake
    // a "space blue" washes the whole frame and buries the faint stars.
    scene.clearColor = new Color4(0, 0, 0, 1);

    // Throwing things is core to this place, so the impactor is attached
    // for the lifetime of the world rather than spun up on first use.
    this.settlers.attach(scene);
    this.belts.attach(scene);
    this.orbitTraffic.attach(scene);
    this.orbitTraffic.build();
    this.tleTraffic.attach(scene);
    void this.tleTraffic.build();

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
    this.star.position.copyFrom(this.systemCenter);
    this.starMat = new ShaderMaterial('starM', scene, PLANET_SHADER, {
      attributes: ['position', 'normal', 'uv'],
      uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'time', 'seed',
                 'ptype', 'tintA', 'tintB', 'detail', 'cloudAmt', 'cityLights',
                 'radius', 'isStar', 'displace', 'displaceScale', ...PLANET_MAP_UNIFORMS],
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
    // The photosphere is granulated in the fragment, not displaced in the
    // vertex: a star's surface relief would read as a lumpy disc.
    this.starMat.setFloat('displace', 0);
    this.starMat.setFloat('displaceScale', 0);
    this.starMat.setVector3('sunPos', this.systemCenter);
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
    corona.position.copyFrom(this.systemCenter);
    cm.setVector3('starCenter', this.systemCenter);
    corona.isPickable = false;
    corona.renderingGroupId = 0;
    (this as any)._coronaMat = cm;

    // ---- glare ----
    // A camera-facing disc in front of the star for the raw searing core
    // and its diffraction spikes.
    const glare = MeshBuilder.CreatePlane('sunGlare',
      { size: GLARE_SIZE }, scene);
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
    this.glareIntensity = look.intensity * 0.85;
    gm.setFloat('intensity', this.glareIntensity);
    gm.setFloat('spikes', 0.75);
    gm.alphaMode = 1;
    gm.backFaceCulling = false;
    gm.disableDepthWrite = true;
    glare.material = gm;
    glare.position.copyFrom(this.systemCenter);
    glare.isPickable = false;
    glare.billboardMode = 7;   // always faces the camera
    (this as any)._glareMat = gm;
    (this as any)._glare = glare;

    // ---- asteroid belts ----
    // One main belt in the gap between the inner worlds and the first gas
    // giant, which is where a real belt forms: close enough for debris to
    // survive, far enough that the giant's resonances stopped a planet
    // from ever accreting there.
    {
      // Find the widest gap between consecutive orbits and put the belt
      // there. Keying off "the first gas giant" was wrong: the generator
      // is free to put a giant in the innermost slot, in which case the
      // supposed inner worlds all orbit OUTSIDE it and the gap inverts to
      // nothing, which is why no belt was being created at all.
      const sorted = [...this.localPlanets].sort((p1, p2) => p1.orbit - p2.orbit);
      let lastInner = sorted.length ? sorted[0].orbit : 40;
      let gapOuter = lastInner * 1.8;
      let widest = 0;
      for (let i = 1; i < sorted.length; i++) {
        const gap = sorted[i].orbit - sorted[i - 1].orbit;
        if (gap > widest) {
          widest = gap;
          lastInner = sorted[i - 1].orbit;
          gapOuter = sorted[i].orbit;
        }
      }
      if (gapOuter > lastInner * 1.15) {
        this.belts.add({
          centre: this.systemCenter.clone(),
          inner: lastInner * 1.12,
          outer: gapOuter * 0.86,
          count: 1100,
          thickness: 0.9,
          // Tuned so the belt's period reads as motion without spinning
          // like a fan; the SHAPE of the curve is what matters.
          mu: 260,
          seed: 0x5eed1
        });
      }
    }

    this.light = new PointLight('sunLight', this.systemCenter.clone(), scene);
    this.light.intensity = 2.2;
    this.light.range = 900;

    // ---- planets ----
    this.localPlanets.forEach((cfg, i) => {
      const root = new TransformNode('root_' + cfg.name, scene);
      // Earth gets the highest tessellation: it is the world players look at
      // longest, and the extra vertices are what let its real terrain relief
      // and cloud deck resolve instead of reading as a smooth cartoon ball.
      const segments = cfg.inhabited ? 160 : 96;
      const mesh = MeshBuilder.CreateSphere(cfg.name, { diameter: cfg.r * 2, segments }, scene);
      mesh.parent = root;

      const mat = new ShaderMaterial('m_' + cfg.name, scene, PLANET_SHADER, {
        attributes: ['position', 'normal', 'uv'],
        uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'time',
                   'seed', 'ptype', 'tintA', 'tintB', 'detail', 'cloudAmt', 'cityLights', 'radius', 'isStar',
                   'habitable', 'displace', 'displaceScale', ...PLANET_MAP_UNIFORMS],
        samplers: PLANET_MAP_SAMPLERS
      });
      applyPlanetMap(mat, cfg.type as PlanetKind, scene, Math.floor(i * 2654435761 + 101));
      mat.setFloat('seed', i * 3.77 + 1.3);
      mat.setFloat('ptype', cfg.type);
      mat.setColor3('tintA', new Color3(...cfg.a));
      mat.setColor3('tintB', new Color3(...cfg.b));
      mat.setFloat('radius', cfg.r);
      mat.setFloat('isStar', 0);
      // Only the inhabited world gets the lush atmospheric limb; every other
      // body keeps the thin, desaturated, "dead rock" haze.
      mat.setFloat('habitable', cfg.inhabited ? 1 : 0);
      // Real terrain relief, scaled to the body's own radius. Gas giants
      // stay smooth: their surface is a fluid band system, not rock.
      const displaceScale = cfg.type === PlanetKind.Gas
        ? 0 : cfg.r * (cfg.type === PlanetKind.Rocky ? 0.05
            : cfg.type === PlanetKind.Lava ? 0.04 : 0.06);
      mat.setFloat('displace', cfg.type === PlanetKind.Gas ? 0 : 1);
      mat.setFloat('displaceScale', displaceScale);
      mesh.material = mat;

      const body: Body = {
        root, mesh, mat,
        orbitR: cfg.orbit, visualR: cfg.r, orbitSpeed: cfg.speed,
        angle: Math.random() * Math.PI * 2,
        spin: 0.15 + Math.random() * 0.35,
        name: cfg.name, type: cfg.type, moons: []
      };

      if (cfg.atmo) {
        const atmo = MeshBuilder.CreateSphere('a_' + cfg.name, { diameter: cfg.r * 2.16, segments: 48 }, scene);
        atmo.parent = root;
        const am = new ShaderMaterial('am_' + cfg.name, scene, 'atmo', {
          attributes: ['position', 'normal'],
          uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos',
                     'atmoColor', 'power', 'habitable', 'density', 'saturation',
                     'aurora', 'time',
                     'planetCenter', 'planetRadius', 'atmoRadius'],
          needAlphaBlending: true
        });
        am.setColor3('atmoColor', new Color3(...cfg.atmo));
        // Habitable worlds get a soft, glowing halo; dead worlds get a
        // tighter, harsher limb. Everything else keys off the same flag so
        // the two looks can never drift apart.
        am.setFloat('power', cfg.inhabited ? 2.4 : 3.6);
        am.setFloat('habitable', cfg.inhabited ? 1 : 0);
        am.setFloat('density', cfg.inhabited ? 1.0 : 0.55);
        am.setFloat('saturation', cfg.inhabited ? 1.0 : 0.45);
        // Aurorae need a magnetic field: habitable and icy worlds get them,
        // airless rocks do not.
        am.setFloat('aurora', cfg.inhabited || cfg.type === PlanetKind.Ice ? 1 : 0);
        am.setFloat('time', 0);
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
      // Saturn's rings span about 2.3 planetary radii and are the first
      // thing anyone looks for; the old 2.4x disc read as a faint hoop you
      // had to squint at. A wider, brighter disc makes a ringed world read
      // as a ringed world from a distance instead of a slightly fuzzy ball.
      if (cfg.ring) {
        const ring = MeshBuilder.CreateDisc('r_' + cfg.name, { radius: cfg.r * 3.4, tessellation: 160 }, scene);
        ring.parent = root;
        ring.rotation.x = Math.PI / 2;
        ring.rotation.z = 0.24;
        const rm = new StandardMaterial('rm_' + cfg.name, scene);
        rm.diffuseTexture = ringTexture(scene);
        rm.opacityTexture = rm.diffuseTexture;
        rm.emissiveColor = new Color3(0.5, 0.42, 0.33);
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
                     'habitable', 'displace', 'displaceScale', ...PLANET_MAP_UNIFORMS],
          samplers: PLANET_MAP_SAMPLERS
        });
        mm.setFloat('useMap', 0);
        mm.setFloat('oceanDepth', 0);
        mm.setFloat('habitable', 0);
        mm.setFloat('seed', i * 9.1 + m * 4.3 + 20.0);
        mm.setFloat('ptype', 0);
        mm.setColor3('tintA', new Color3(0.28, 0.26, 0.25));
        mm.setColor3('tintB', new Color3(0.62, 0.60, 0.57));
        mm.setFloat('radius', mr);
        mm.setFloat('isStar', 0);
        // Moons are lumpy, airless rock: real relief, a touch more relative
        // to their small radius so it reads from close range.
        mm.setFloat('displace', 1);
        mm.setFloat('displaceScale', mr * 0.06);
        moon.material = mm;
        body.moons.push({ pivot, mesh: moon, speed: 0.5 + Math.random() * 0.9 });
        (body as any).moonMats = [...((body as any).moonMats || []), mm];
      }

      this.bodies.push(body);
    });

    ctx.setCameraTarget(this.logicalSystemCenter, 62);
  }

  update(dt: number, ctx: WorldContext): void {
    toRenderRef(this.logicalSystemCenter,this.systemCenter);
    this.t += dt * this.p.timeScale;
    const cam = ctx.camera;
    const cp = cam.position;

    this.starMat.setVector3('camPos', cp);
    this.starMat.setFloat('time', this.t);
    this.star.rotation.y += dt * 0.02;
    // The home star ages on the long clock: main sequence, a slow swell
    // into a red giant, then a collapse into whatever its mass leaves
    // behind. Colour and size drift with the phase, so the sky of the home
    // system is a place that changes rather than a fixed photograph.
    {
      const life = stellarColor(7, this.t);
      this.starMat.setColor3('tintA', new Color3(...life.tintA));
      this.starMat.setColor3('tintB', new Color3(...life.tintB));
      this.star.scaling.setAll(Math.max(0.02, life.size));
    }
    // The corona boils and its streamers drift outward, so it must be fed
    // time as well as the camera.
    const cmat = (this as any)._coronaMat as ShaderMaterial;
    if (cmat) {
      cmat.setVector3('camPos', cp);
      cmat.setFloat('time', this.t);
    }
    const gmat = (this as any)._glareMat as ShaderMaterial;
    if (gmat) gmat.setFloat('time', this.t);

    // ---- keep the glare from swallowing the screen ----
    // The glare is a fixed 102-unit billboard, so its ANGULAR size grows
    // without limit as you approach: measured 54 degrees at 100 units and
    // 119 degrees at 30. That is the white-out that buries everything else
    // in the frame and leaves nearby bodies as black silhouettes.
    //
    // A real star does the opposite - it stays a small brilliant disc and
    // it is the BLOOM that grows. So the billboard is scaled to hold a
    // constant apparent size once you are inside the range where it would
    // otherwise take over, and its intensity is eased down to match.
    const glare = (this as any)._glare as Mesh | undefined;
    if (glare && gmat) {
      const dist = Vector3.Distance(cp, glare.getAbsolutePosition());
      // Half-angle we are willing to let the glare occupy.
      const MAX_HALF_ANGLE = 0.28;          // ~32 degrees across
      const baseHalf = GLARE_SIZE * 0.5;
      const allowedHalf = Math.tan(MAX_HALF_ANGLE) * Math.max(dist, 1);
      const k = Math.max(0.06, Math.min(1, allowedHalf / baseHalf));
      glare.scaling.set(k, k, 1);
      // Fade with the same curve, so closing on a star brightens the scene
      // smoothly instead of clipping every pixel to white.
      gmat.setFloat('intensity', this.glareIntensity * (0.35 + 0.65 * k));
    }

    for (const b of this.bodies) {
      b.angle += dt * b.orbitSpeed * 0.12 * this.p.timeScale * this.p.orbitSpeed;
      b.root.position.set(
        this.systemCenter.x + Math.cos(b.angle) * b.orbitR,
        this.systemCenter.y,
        this.systemCenter.z + Math.sin(b.angle) * b.orbitR);
      b.mesh.rotation.y += dt * b.spin * this.p.timeScale;

      b.mat.setVector3('camPos', cp);
      b.mat.setVector3('sunPos', this.systemCenter);
      b.mat.setFloat('time', this.t);
      // Distance-adaptive detail: as the camera closes on a world, the noise
      // octaves deepen so up-close terrain resolves into real relief instead
      // of a flat, hollow-looking ghost mesh. The boost is smooth, so there
      // is no pop as you cross the threshold.
      {
        const d = Vector3.Distance(cp, b.mesh.getAbsolutePosition());
        const k = Math.max(0, Math.min(1, 1 - (d - b.visualR) / Math.max(b.visualR * 2.4, 1)));
        // Earth resolves an extra octave of detail up close: its terrain is
        // the one the player inspects, so it is allowed to cost more.
        const homeBoost = b.name === this.inhabitedName ? 1.35 : 1;
        const boost = (1 + k * 1.6) * homeBoost;
        b.mat.setFloat('detail', this.p.detail * boost);
      }
      b.mat.setFloat('cloudAmt', this.p.clouds);
      // Keyed to the system's inhabited world, whichever one that turned out
      // to be. Hard-coding the old literal name here meant the night side of
      // every planet stayed dark once the roster became procedural.
      b.mat.setFloat('cityLights', b.name === this.inhabitedName ? this.p.lights : 0);
      // The Exposure slider existed in the options panel but was never sent
      // to the shader, so dragging it did nothing at all.
      b.mat.setFloat('exposure', this.p.exposure);

      if (b.atmoMat) {
        b.atmoMat.setVector3('camPos', cp);
        b.atmoMat.setVector3('sunPos', this.systemCenter);
        // The aurora curtains drift on the same clock as the weather.
        b.atmoMat.setFloat('time', this.t);
        // The planet orbits, so the centre the raymarch integrates around
        // moves every frame. A stale centre would tear the atmosphere off
        // the planet exactly the way a stale camera tore the accretion disk
        // off its horizon.
        b.atmoMat.setVector3('planetCenter', b.mesh.getAbsolutePosition());
      }
      for (const mm of ((b as any).moonMats || []) as ShaderMaterial[]) {
        mm.setVector3('camPos', cp);
        mm.setVector3('sunPos', this.systemCenter);
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
    this.belts.update(dt * Math.max(0.05, this.p.timeScale));

    // The inhabited world carries its people with it as it orbits, so they
    // stay on the ground rather than being left behind in empty space.
    const home = this.bodies.find((x) => x.name === this.inhabitedName);
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
    // Earth's orbital neighbourhood - the ISS, the satellites, Apollo - rides
    // along with the inhabited world as it circles the star, and the full
    // ten-thousand-object TLE cloud wears Earth like its real shell.
    if (home) {
      const homePos = home.mesh.getAbsolutePosition();
      this.orbitTraffic.update(dt * Math.max(0.05, this.p.timeScale), homePos);
      this.tleTraffic.update(homePos, dt);
    }
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
      mass: 5.97e24 * Math.pow(b.visualR / this.referenceRadius, 3),
      physicalRadius: 6.371e6 * (b.visualR / this.referenceRadius)
    }));
  }

  /**
   * The solid bodies flight can collide with and land on: the sun, every
   * planet and every moon, at their current world positions. App resolves the
   * player against these so a planet stops them instead of swallowing them,
   * and the ground probe walks on them.
   */
  collisionBodies(): SolidSphere[] {
    const out: SolidSphere[] = [];
    const origin=renderOrigin();
    const starPos = this.star ? this.star.getAbsolutePosition() : Vector3.Zero();
    out.push({
      id:'the sun',x:starPos.x+origin.x,y:starPos.y+origin.y,z:starPos.z+origin.z,
      radius: 4.5, mass: 120000, habitable: false
    });
    for (const b of this.bodies) {
      const p = b.mesh.getAbsolutePosition();
      out.push({
        id:b.name,x:p.x+origin.x,y:p.y+origin.y,z:p.z+origin.z,
        radius: b.visualR,
        mass: 60 + 400 * Math.pow(b.visualR / this.referenceRadius, 3),
        habitable: b.name === this.inhabitedName,
        // Gas giants are dived, not landed: the landing key reroutes.
        gas: b.type === PlanetKind.Gas,
        // Earth's own sky: a deep blue you can stand under.
        sky: b.name === this.inhabitedName ? [0.08, 0.17, 0.34] : undefined
      });
      for (const m of b.moons) {
        const mp = m.mesh.getAbsolutePosition();
        out.push({
          id:b.name+' moon',x:mp.x+origin.x,y:mp.y+origin.y,z:mp.z+origin.z,
          radius: b.visualR * 0.2, mass: 1, habitable: false
        });
      }
    }
    // The Apollo command module is a place you can land on and walk around.
    for(const s of this.orbitTraffic.solids())out.push({...s,x:s.x+origin.x,y:s.y+origin.y,z:s.z+origin.z});
    return out;
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
    this.belts.detach();
    this.orbitTraffic.dispose();
    this.tleTraffic.dispose();
    this.bodies.forEach((b) => { b.root.dispose(false, true); b.mat.dispose(); });
    this.bodies = [];
    this.star?.dispose();
    this.starMat?.dispose();
    this.stars?.dispose();
    this.light?.dispose();
  }
}
