/**
 * PortalShader — the see-through face of a player-made wormhole.
 *
 * The portal is not a flat texture. It reuses the same gravitational lensing
 * maths as the black hole: a ray is bent by the throat's mass before being
 * used to sample the destination sky, so you genuinely see the far side
 * distorted by the wormhole's own gravity, and the distortion strengthens
 * toward the rim exactly as it does around a real compact object.
 *
 * Deliberately GLSL ES 1.00, no dFdx/dFdy, matching the rest of the project.
 */

import { GLSL_NOISE } from '../Noise';

export const PORTAL_SHADER = 'lowPortal';

export const PORTAL_VERT = `
precision highp float;
attribute vec3 position;
attribute vec3 normal;
attribute vec2 uv;

uniform mat4 world;
uniform mat4 worldViewProjection;

varying vec3 vWorld;
varying vec3 vNormal;
varying vec2 vUv;

void main(void){
  vec4 wp = world * vec4(position, 1.0);
  vWorld = wp.xyz;
  vNormal = normalize(mat3(world[0].xyz, world[1].xyz, world[2].xyz) * normal);
  vUv = uv;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

export const PORTAL_FRAG = `
precision highp float;

varying vec3 vWorld;
varying vec3 vNormal;
varying vec2 vUv;

uniform vec3  camPos;
uniform float time;
uniform float throatMass;     // bends light near the rim
uniform float lensStrength;   // per-wormhole lensing flavour
uniform float openness;       // 0 = sealed, 1 = fully open
uniform vec3  rimColor;
uniform vec3  destTintA;      // destination sky, warm component
uniform vec3  destTintB;      // destination sky, cool component
uniform float destSeed;       // picks the destination starfield
uniform float exposure;

${GLSL_NOISE}

// ---- the destination sky, sampled in a direction ----
vec3 destinationSky(vec3 dir, float seed){
  vec3 c = vec3(0.0);
  for (int k = 0; k < 3; k++){
    float sc = 70.0 + float(k) * 130.0;
    vec3 p = dir * sc + vec3(seed * 37.1, seed * 11.7, seed * 5.3);
    vec3 ip = floor(p);
    vec3 fp = fract(p);
    for (int i = -1; i <= 1; i++)
    for (int j = -1; j <= 1; j++)
    for (int l = -1; l <= 1; l++){
      vec3 o = vec3(float(i), float(j), float(l));
      vec3 h = hash33(ip + o) * 0.5 + 0.5;
      float d = length(fp - (o + h));
      float bright = pow(max(0.0, 1.0 - d * 2.3), 20.0);
      float t = fract(h.x * 7.3 + seed);
      vec3 tint = mix(destTintA, destTintB, t);
      c += tint * bright * (0.4 + h.z * 0.9);
    }
  }

  // a soft nebula so the far side never reads as empty black
  float n = fbm(dir * 2.1 + vec3(seed * 3.7), 4, 2.0, 0.5);
  vec3 neb = mix(destTintA, destTintB, clamp(n * 1.4, 0.0, 1.0));
  c += neb * smoothstep(0.35, 1.0, n) * 0.5;

  // guarantee a visible floor: the portal must never be a black hole in the UI
  c += mix(destTintA, destTintB, 0.5) * 0.06;
  return c;
}

void main(void){
  // radial coordinate across the portal disc
  vec2 p = vUv * 2.0 - 1.0;
  float r = length(p);

  if (r > 1.0) discard;

  vec3 viewDir = normalize(vWorld - camPos);
  vec3 n = normalize(vNormal);

  // ---- gravitational lensing of the view ray ----
  // Deflection grows toward the rim, matching light passing closer to a mass.
  // alpha = 4GM / (c^2 b), with b the impact parameter, so alpha ~ 1/b.
  float b = max(0.06, 1.0 - r);
  float deflect = (throatMass * 0.42 * lensStrength) / b;
  deflect = clamp(deflect, 0.0, 3.2);

  // bend the ray outward from the portal axis, in the plane of the rim
  vec3 radial = normalize(cross(cross(n, vec3(p.x, p.y, 0.0001)), n) + vec3(1e-5));
  vec3 bent = normalize(viewDir + radial * deflect * 0.6 - n * deflect * 0.25);

  vec3 sky = destinationSky(bent, destSeed);

  // ---- Einstein ring: light piled up at the rim ----
  float ring = smoothstep(0.86, 0.98, r) * (1.0 - smoothstep(0.98, 1.0, r));
  sky += rimColor * ring * 2.6 * lensStrength;

  // ---- the throat itself, swirling ----
  float swirl = fbm(vec3(p * 3.0, time * 0.15), 3, 2.0, 0.5);
  sky *= 0.75 + swirl * 0.6;

  // ---- rim glow so the opening always reads clearly ----
  float rim = smoothstep(0.72, 1.0, r);
  sky = mix(sky, rimColor * 1.6, rim * 0.55);

  // ---- opening animation ----
  float aperture = smoothstep(openness, openness - 0.28, r);
  sky *= aperture;
  sky += rimColor * (1.0 - aperture) * 0.12;

  // ACES-ish tonemap, matching the rest of the renderer
  vec3 c = sky * exposure;
  c = (c * (2.51 * c + 0.03)) / (c * (2.43 * c + 0.59) + 0.14);
  c = pow(clamp(c, 0.0, 1.0), vec3(1.0 / 2.2));

  gl_FragColor = vec4(c, 1.0);
}
`;
