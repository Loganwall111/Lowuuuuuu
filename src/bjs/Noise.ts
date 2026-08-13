/**
 * Noise — GLSL noise/FBM source injected into custom shaders, plus a CPU
 * mirror used for procedural texture baking and terrain displacement.
 */

export const GLSL_NOISE = `
vec3 hash33(vec3 p){
  p = vec3(dot(p, vec3(127.1, 311.7, 74.7)),
           dot(p, vec3(269.5, 183.3, 246.1)),
           dot(p, vec3(113.5, 271.9, 124.6)));
  return fract(sin(p) * 43758.5453123) * 2.0 - 1.0;
}
float snoise3(vec3 p){
  vec3 i = floor(p); vec3 f = fract(p);
  vec3 u = f * f * (3.0 - 2.0 * f);
  return mix(mix(mix(dot(hash33(i + vec3(0,0,0)), f - vec3(0,0,0)),
                     dot(hash33(i + vec3(1,0,0)), f - vec3(1,0,0)), u.x),
                 mix(dot(hash33(i + vec3(0,1,0)), f - vec3(0,1,0)),
                     dot(hash33(i + vec3(1,1,0)), f - vec3(1,1,0)), u.x), u.y),
             mix(mix(dot(hash33(i + vec3(0,0,1)), f - vec3(0,0,1)),
                     dot(hash33(i + vec3(1,0,1)), f - vec3(1,0,1)), u.x),
                 mix(dot(hash33(i + vec3(0,1,1)), f - vec3(0,1,1)),
                     dot(hash33(i + vec3(1,1,1)), f - vec3(1,1,1)), u.x), u.y), u.z);
}
float fbm(vec3 p, int oct, float lac, float gain){
  float a = 0.5, s = 0.0, n = 0.0;
  for (int i = 0; i < 8; i++){
    if (i >= oct) break;
    s += a * snoise3(p);
    n += a; a *= gain; p *= lac;
  }
  return s / max(n, 0.0001);
}
float ridged(vec3 p, int oct, float lac, float gain){
  float a = 0.5, s = 0.0, n = 0.0;
  for (int i = 0; i < 8; i++){
    if (i >= oct) break;
    float v = 1.0 - abs(snoise3(p));
    v *= v;
    s += a * v; n += a; a *= gain; p *= lac;
  }
  return s / max(n, 0.0001);
}
`;

/* ---------- CPU mirror ---------- */

function hash3(x: number, y: number, z: number): number {
  const s = Math.sin(x * 127.1 + y * 311.7 + z * 74.7) * 43758.5453123;
  return (s - Math.floor(s)) * 2 - 1;
}

function lerp(a: number, b: number, t: number) { return a + (b - a) * t; }

export function valueNoise3(x: number, y: number, z: number): number {
  const ix = Math.floor(x), iy = Math.floor(y), iz = Math.floor(z);
  const fx = x - ix, fy = y - iy, fz = z - iz;
  const ux = fx * fx * (3 - 2 * fx);
  const uy = fy * fy * (3 - 2 * fy);
  const uz = fz * fz * (3 - 2 * fz);
  const c = (dx: number, dy: number, dz: number) => hash3(ix + dx, iy + dy, iz + dz);
  return lerp(
    lerp(lerp(c(0,0,0), c(1,0,0), ux), lerp(c(0,1,0), c(1,1,0), ux), uy),
    lerp(lerp(c(0,0,1), c(1,0,1), ux), lerp(c(0,1,1), c(1,1,1), ux), uy),
    uz
  );
}

export function fbmCPU(x: number, y: number, z: number, oct = 5, lac = 2.0, gain = 0.5): number {
  let a = 0.5, s = 0, n = 0;
  for (let i = 0; i < oct; i++) {
    s += a * valueNoise3(x, y, z);
    n += a; a *= gain; x *= lac; y *= lac; z *= lac;
  }
  return s / (n || 1);
}

export function ridgedCPU(x: number, y: number, z: number, oct = 5, lac = 2.0, gain = 0.5): number {
  let a = 0.5, s = 0, n = 0;
  for (let i = 0; i < oct; i++) {
    let v = 1 - Math.abs(valueNoise3(x, y, z));
    v *= v;
    s += a * v; n += a; a *= gain; x *= lac; y *= lac; z *= lac;
  }
  return s / (n || 1);
}
