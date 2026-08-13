/**
 * SunShader — the aura around a star.
 *
 * The old corona was the planetary atmosphere shader pointed at the sun: a
 * smooth rim falloff, which reads as a plain orange ball with a halo. A real
 * corona is not smooth. It is turbulent plasma with structure that streams
 * outward, loops of prominence arcing off the limb, and a bright, thin
 * chromosphere right at the surface fading into a huge faint outer glow.
 *
 * This is three cooperating pieces on additive shells, all driven from one
 * fragment shader so they stay consistent:
 *
 *   - `CORONA_FRAG`  the turbulent aura, on a large additive shell
 *   - `GLARE_FRAG`   a camera-facing bloom disc for the raw glare
 *
 * GLSL ES 1.00 only: no dFdx/dFdy, no dynamic array indexing.
 */

/** Shared noise. Cheap 3-D value noise plus FBM and a ridged variant. */
const NOISE = `
float hash13(vec3 p){
  p = fract(p * 0.3183099 + vec3(0.1, 0.2, 0.3));
  p *= 17.0;
  return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float vnoise(vec3 p){
  vec3 i = floor(p);
  vec3 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float n000 = hash13(i + vec3(0.0, 0.0, 0.0));
  float n100 = hash13(i + vec3(1.0, 0.0, 0.0));
  float n010 = hash13(i + vec3(0.0, 1.0, 0.0));
  float n110 = hash13(i + vec3(1.0, 1.0, 0.0));
  float n001 = hash13(i + vec3(0.0, 0.0, 1.0));
  float n101 = hash13(i + vec3(1.0, 0.0, 1.0));
  float n011 = hash13(i + vec3(0.0, 1.0, 1.0));
  float n111 = hash13(i + vec3(1.0, 1.0, 1.0));
  float nx00 = mix(n000, n100, f.x);
  float nx10 = mix(n010, n110, f.x);
  float nx01 = mix(n001, n101, f.x);
  float nx11 = mix(n011, n111, f.x);
  return mix(mix(nx00, nx10, f.y), mix(nx01, nx11, f.y), f.z);
}

float fbm(vec3 p){
  float a = 0.5;
  float s = 0.0;
  for (int i = 0; i < 5; i++){
    s += vnoise(p) * a;
    p *= 2.03;
    a *= 0.5;
  }
  return s;
}

/* Ridged noise gives the filament structure real coronae have. */
float ridged(vec3 p){
  float a = 0.5;
  float s = 0.0;
  for (int i = 0; i < 5; i++){
    float n = 1.0 - abs(vnoise(p) * 2.0 - 1.0);
    s += n * n * a;
    p *= 2.11;
    a *= 0.5;
  }
  return s;
}
`;

export const CORONA_VERT = `
precision highp float;
attribute vec3 position;
attribute vec3 normal;
uniform mat4 world;
uniform mat4 worldViewProjection;
varying vec3 vNrm;
varying vec3 vWorld;
varying vec3 vLocal;
void main(void){
  vLocal = position;
  vNrm = normalize(mat3(world) * normal);
  vWorld = (world * vec4(position, 1.0)).xyz;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

/**
 * The aura itself. Rendered additively on a shell several radii wide, so
 * the star is surrounded by streaming plasma rather than a hard edge.
 */
export const CORONA_FRAG = `
precision highp float;
varying vec3 vNrm;
varying vec3 vWorld;
varying vec3 vLocal;

uniform vec3 camPos;
uniform vec3 starCenter;
uniform float time;
uniform float starRadius;    // surface radius in world units
uniform float shellRadius;   // radius of this shell
uniform vec3 hotColor;       // chromosphere colour, near-white
uniform vec3 midColor;       // main corona colour
uniform vec3 coolColor;      // far streamers
uniform float intensity;
uniform float turbulence;
uniform float streamers;     // how pronounced the radial filaments are
uniform float prominence;    // strength of the looping arcs at the limb

${NOISE}

void main(void){
  vec3 dir = normalize(vLocal);
  vec3 V = normalize(camPos - vWorld);
  vec3 n = normalize(vNrm);

  // Distance of this shell point from the star centre, in star radii.
  float r = length(vLocal) / max(starRadius, 0.0001);

  // ---- radial falloff -------------------------------------------------
  // Corona brightness drops steeply but never truly ends, so the star
  // keeps a faint halo far out instead of a visible shell boundary.
  float near = 1.0 / (1.0 + pow(max(r - 1.0, 0.0) * 2.4, 2.0));
  float far  = 0.30 / (1.0 + pow(max(r - 1.0, 0.0) * 0.62, 3.0));
  float radial = near + far;

  // ---- turbulent plasma ----------------------------------------------
  // Two noise fields advected outward at different rates: the corona
  // boils, and the structure drifts away from the surface.
  vec3 q = dir * (2.6 + turbulence * 2.0);
  float t = time * 0.06;
  float boil = fbm(q * 1.7 + vec3(0.0, t * 1.4, 0.0));
  float drift = fbm(q * 0.9 - dir * t * 2.2 + 11.0);

  // ---- radial streamers ----------------------------------------------
  // Ridged noise sampled almost entirely by direction makes long spokes
  // that persist as they travel out, which is what sells "corona" over
  // "fog". They stretch with distance.
  float spoke = ridged(dir * (3.4 + streamers * 3.0) + vec3(0.0, t * 0.5, 0.0));
  spoke = pow(clamp(spoke, 0.0, 1.0), 1.6);
  float spokeFade = 1.0 / (1.0 + max(r - 1.0, 0.0) * 0.8);
  float filament = spoke * streamers * spokeFade;

  // ---- prominences ----------------------------------------------------
  // Bright loops hugging the limb: a band of noise confined to just above
  // the surface, so arcs appear to leap off the edge and fall back.
  float limb = exp(-pow((r - 1.14) * 6.0, 2.0));
  float arc = ridged(dir * 6.5 + vec3(t * 2.0, 0.0, t * 1.3));
  float arcs = pow(clamp(arc, 0.0, 1.0), 3.0) * limb * prominence;

  // ---- combine --------------------------------------------------------
  float density = radial * (0.55 + boil * 0.85 + drift * 0.5);
  density += filament * radial * 1.3;
  density += arcs * 1.6;

  // Hotter nearer the surface: white -> orange -> deep red at the fringe.
  float heat = clamp(1.0 - (r - 1.0) * 0.55, 0.0, 1.0);
  vec3 col = mix(coolColor, midColor, clamp(heat * 1.5, 0.0, 1.0));
  col = mix(col, hotColor, pow(heat, 3.0));
  col += hotColor * arcs * 0.8;

  // Limb brightening: grazing views look through more plasma.
  float graze = 1.0 - abs(dot(n, V));
  density *= 0.55 + graze * 1.25;

  float a = clamp(density * intensity, 0.0, 1.0);

  // Additive: never darkens what is behind it, so it cannot make a
  // black shell if the maths goes out of range.
  gl_FragColor = vec4(col * a, a);
}
`;

/**
 * A camera-facing disc of raw glare sitting in front of the star, giving the
 * eye-searing core and diffraction the corona alone cannot provide.
 */
export const GLARE_FRAG = `
precision highp float;
varying vec2 vUV;
uniform float time;
uniform vec3 glareColor;
uniform float intensity;
uniform float spikes;

void main(void){
  vec2 p = vUV * 2.0 - 1.0;
  float r = length(p);
  if (r > 1.0) { gl_FragColor = vec4(0.0); return; }

  // Core plus a wide soft skirt.
  float core = exp(-r * r * 34.0);
  float skirt = exp(-r * 3.1) * 0.42;

  // Diffraction spikes, slowly rotating so it never looks like a decal.
  float ang = atan(p.y, p.x) + time * 0.02;
  float star4 = pow(abs(cos(ang * 2.0)), 22.0);
  float star6 = pow(abs(cos(ang * 3.0 + 0.7)), 30.0);
  float spike = (star4 + star6 * 0.6) * exp(-r * 4.4) * spikes;

  float a = clamp((core + skirt + spike) * intensity, 0.0, 1.0);
  gl_FragColor = vec4(glareColor * a, a);
}
`;

export const GLARE_VERT = `
precision highp float;
attribute vec3 position;
attribute vec2 uv;
uniform mat4 worldViewProjection;
varying vec2 vUV;
void main(void){
  vUV = uv;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

/** Uniform names, kept beside the source so the two cannot drift apart. */
export const CORONA_UNIFORMS = [
  'world', 'worldViewProjection', 'camPos', 'starCenter', 'time',
  'starRadius', 'shellRadius', 'hotColor', 'midColor', 'coolColor',
  'intensity', 'turbulence', 'streamers', 'prominence'
];

export const GLARE_UNIFORMS = [
  'world', 'worldViewProjection', 'time', 'glareColor', 'intensity', 'spikes'
];

/** Spectral class presets, so different stars have different auras. */
export interface CoronaLook {
  hot: [number, number, number];
  mid: [number, number, number];
  cool: [number, number, number];
  glare: [number, number, number];
  intensity: number;
  turbulence: number;
  streamers: number;
  prominence: number;
}

export const CORONA_PRESETS: Record<string, CoronaLook> = {
  // A sun-like G star: white core, orange corona, red fringe.
  yellow: {
    hot: [1.0, 0.97, 0.88], mid: [1.0, 0.58, 0.18], cool: [0.62, 0.13, 0.03],
    glare: [1.0, 0.9, 0.72], intensity: 1.0, turbulence: 1.0,
    streamers: 0.85, prominence: 0.9
  },
  blue: {
    hot: [0.94, 0.98, 1.0], mid: [0.44, 0.66, 1.0], cool: [0.12, 0.20, 0.66],
    glare: [0.82, 0.90, 1.0], intensity: 1.25, turbulence: 1.35,
    streamers: 1.1, prominence: 0.55
  },
  red: {
    hot: [1.0, 0.72, 0.44], mid: [0.92, 0.30, 0.10], cool: [0.34, 0.05, 0.02],
    glare: [1.0, 0.56, 0.30], intensity: 0.78, turbulence: 0.7,
    streamers: 0.55, prominence: 1.3
  },
  white: {
    hot: [1.0, 1.0, 1.0], mid: [0.86, 0.88, 0.95], cool: [0.34, 0.40, 0.58],
    glare: [1.0, 1.0, 1.0], intensity: 1.1, turbulence: 1.0,
    streamers: 0.9, prominence: 0.7
  }
};

export function coronaFor(name: string): CoronaLook {
  return CORONA_PRESETS[name] ?? CORONA_PRESETS.yellow;
}
