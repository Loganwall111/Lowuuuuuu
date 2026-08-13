/**
 * PlanetShader — the shared procedural body shader.
 *
 * One fragment program covers every class of body in the project. The whole
 * surface is synthesised from an FBM / ridged-noise stack, so there are no
 * textures to author or download and each body is unique from its seed.
 */

import { Effect } from '@babylonjs/core/Materials/effect';
import { GLSL_NOISE } from '../Noise';

export const PLANET_SHADER = 'lowPlanet';

export enum PlanetKind {
  Rocky = 0,
  Terran = 1,
  Ice = 2,
  Gas = 3,
  Lava = 4,
  Desert = 5,
  Star = 6
}

export const PLANET_VERT = `
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
  vNrm = normalize(mat3(world[0].xyz, world[1].xyz, world[2].xyz) * normal);
  vWorld = (world * vec4(position, 1.0)).xyz;
  vUV = uv;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

export const PLANET_FRAG = `
precision highp float;
varying vec3 vPos;
varying vec3 vNrm;
varying vec3 vWorld;
varying vec2 vUV;

uniform vec3  camPos;
uniform vec3  sunPos;
uniform float time;
uniform float seed;
uniform float ptype;
uniform vec3  tintA;
uniform vec3  tintB;
uniform float detail;
uniform float cloudAmt;
uniform float cityLights;
uniform float exposure;   // artistic stop, 1.0 = neutral
uniform float radius;
uniform float isStar;
/* Optional photoreal albedo map. Procedural noise alone reads as cartoonish
   at close range; a real texture carries the detail and the noise stack is
   demoted to high-frequency break-up on top of it. */
uniform sampler2D albedoMap;
uniform float useMap;
/** How deep this planet's oceans run, 0..1. Drives light absorption. */
uniform float oceanDepth;

${GLSL_NOISE}

vec3 surface(vec3 p, out float rough, out float specMask){
  vec3 sp = p * (1.6 + detail * 2.2) + seed * 37.0;
  float lat = abs(p.y);
  rough = 0.9; specMask = 0.0;

  if (ptype < 0.5){
    // rocky / cratered
    float base = fbm(sp * 1.4, 6, 2.15, 0.52) * 0.5 + 0.5;
    float cr = ridged(sp * 3.1, 5, 2.3, 0.5);
    float craters = smoothstep(0.62, 0.95, cr);
    vec3 c = mix(tintA * 0.55, tintB, base);
    c = mix(c, c * 0.45, craters * 0.7);
    c += vec3(0.06) * fbm(sp * 12.0, 4, 2.4, 0.5);
    return c;
  } else if (ptype < 1.5){
    // terran: continents, biomes, mountains, caps
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

    float trop = 1.0 - smoothstep(0.15, 0.72, lat);
    vec3 lc = mix(arid, grass, trop);
    lc = mix(lc, shore, smoothstep(0.06, 0.0, alt));
    lc = mix(lc, rock, smoothstep(0.34, 0.62, alt));
    lc = mix(lc, snow, smoothstep(0.6, 0.85, alt));
    float pole = smoothstep(0.74, 0.90, lat + fbm(sp * 4.0, 3, 2.2, 0.5) * 0.09);
    lc = mix(lc, snow, pole);

    vec3 c = mix(ocean, lc, land);
    rough = mix(0.06, 0.95, land);
    specMask = (1.0 - land) * 0.9;
    return c;
  } else if (ptype < 2.5){
    // ice
    float f = fbm(sp * 1.9, 6, 2.2, 0.55) * 0.5 + 0.5;
    float cracks = ridged(sp * 5.5, 5, 2.4, 0.52);
    vec3 c = mix(tintA, tintB, f);
    c = mix(c, vec3(0.35, 0.55, 0.72), smoothstep(0.70, 0.95, cracks) * 0.8);
    rough = 0.22; specMask = 0.6;
    return c;
  } else if (ptype < 3.5){
    // gas giant
    float band = p.y * 7.5 + fbm(vec3(sp.x * 0.7, sp.y * 3.4, sp.z * 0.7 + time * 0.02), 5, 2.2, 0.55) * 2.6;
    float b = sin(band) * 0.5 + 0.5;
    vec3 c = mix(tintA, tintB, b);
    float turb = fbm(vec3(sp.x * 2.2 + time * 0.05, sp.y * 8.0, sp.z * 2.2), 5, 2.4, 0.5) * 0.5 + 0.5;
    c = mix(c, c * 1.28, turb * 0.5);
    vec3 sc = normalize(vec3(0.62, -0.28, 0.44));
    float sd = distance(normalize(p), sc);
    float storm = smoothstep(0.30, 0.05, sd);
    float swirl = fbm(vec3(p.xz * 9.0 + time * 0.12, p.y * 9.0), 5, 2.3, 0.5) * 0.5 + 0.5;
    c = mix(c, mix(vec3(0.78,0.28,0.16), vec3(0.95,0.62,0.38), swirl), storm * 0.85);
    rough = 0.85;
    return c;
  } else if (ptype < 4.5){
    // lava
    float f = fbm(sp * 2.4 + vec3(0.0, time * 0.05, 0.0), 6, 2.25, 0.52) * 0.5 + 0.5;
    float crust = smoothstep(0.42, 0.72, f);
    vec3 magma = mix(vec3(1.0, 0.85, 0.25), vec3(0.95, 0.22, 0.03), f);
    vec3 rk = mix(vec3(0.09,0.06,0.06), vec3(0.20,0.16,0.15), fbm(sp * 7.0, 4, 2.3, 0.5) * 0.5 + 0.5);
    vec3 c = mix(magma * 2.4, rk, crust);
    rough = 0.9;
    return c;
  } else if (ptype < 5.5){
    // desert
    float dunes = fbm(vec3(sp.x * 3.0, sp.y * 9.0, sp.z * 3.0), 6, 2.2, 0.55) * 0.5 + 0.5;
    float can = ridged(sp * 3.4, 5, 2.3, 0.5);
    vec3 c = mix(tintA, tintB, dunes);
    c = mix(c, c * 0.55, smoothstep(0.74, 0.96, can) * 0.75);
    rough = 0.95;
    return c;
  }
  // star: granulated photosphere
  float g = fbm(p * 6.0 + vec3(0.0, time * 0.09, 0.0), 6, 2.3, 0.55) * 0.5 + 0.5;
  float g2 = fbm(p * 16.0 - time * 0.16, 5, 2.4, 0.5) * 0.5 + 0.5;
  float cells = pow(g, 1.5) * 0.75 + g2 * 0.45;
  rough = 1.0;
  return mix(tintA, tintB, cells);
}

void main(void){
  vec3 n = normalize(vNrm);
  vec3 p = normalize(vPos);
  vec3 V = normalize(camPos - vWorld);
  float rough, specMask;
  vec3 albedo = surface(p, rough, specMask);

  if (useMap > 0.5){
    // Equirectangular lookup from the sphere normal.
    vec2 uvm = vec2(atan(p.z, p.x) / 6.2831853 + 0.5, acos(clamp(p.y, -1.0, 1.0)) / 3.14159265);
    vec3 mapped = texture2D(albedoMap, uvm).rgb;
    // Keep a little procedural grain so the surface still has detail when
    // the camera gets closer than the texture's resolution.
    float grain = dot(albedo, vec3(0.333)) - 0.5;
    albedo = clamp(mapped * (1.0 + grain * 0.28), 0.0, 1.0);
  }

  // ---- self-luminous star path ----
  if (isStar > 0.5){
    float limb = pow(max(dot(n, V), 0.0), 0.45);
    vec3 c = albedo * (0.55 + limb * 0.75) * 2.4;
    c = (c * (2.51 * c + 0.03)) / (c * (2.43 * c + 0.59) + 0.14);
    gl_FragColor = vec4(pow(clamp(c, 0.0, 1.0), vec3(1.0 / 2.2)), 1.0);
    return;
  }

  // bump from the same noise field
  float e = 0.012;
  float sc = 5.0 + detail * 6.0;
  float h0 = fbm(p * sc + seed * 37.0, 5, 2.2, 0.5);
  float hx = fbm((p + vec3(e,0,0)) * sc + seed * 37.0, 5, 2.2, 0.5);
  float hy = fbm((p + vec3(0,e,0)) * sc + seed * 37.0, 5, 2.2, 0.5);
  float hz = fbm((p + vec3(0,0,e)) * sc + seed * 37.0, 5, 2.2, 0.5);
  vec3 grad = vec3(hx - h0, hy - h0, hz - h0) / e;
  n = normalize(n - (grad - dot(grad, n) * n) * 0.010 * (1.0 - specMask));

  vec3 L = normalize(sunPos - vWorld);
  float ndl = dot(n, L);
  float lam = max(ndl, 0.0);
  float day = smoothstep(-0.12, 0.22, ndl);

  // ---- direct light with a soft terminator ----
  // A hard N.L edge looks like CG. Real planets have a wide, warm terminator
  // because the star is a disc, not a point, so wrap the diffuse slightly.
  float wrap = 0.22;
  float diff = clamp((ndl + wrap) / (1.0 + wrap), 0.0, 1.0);
  // warm the light as it grazes, like sunset through more atmosphere
  vec3 sunCol = mix(vec3(1.0, 0.62, 0.34), vec3(1.0, 0.97, 0.92),
                    smoothstep(0.0, 0.42, ndl));

  // 1.35 was a deliberate overdrive that only looked right because the
  // post pipeline was tonemapping a second time and crushing it back down.
  // With the pipeline neutral, the physically sane 1.0 is correct.
  vec3 col = albedo * sunCol * (diff * 1.0 + 0.03);

  // ---- ambient sky bounce, so the night side is never dead black ----
  float upness = n.y * 0.5 + 0.5;
  col += albedo * mix(vec3(0.012, 0.016, 0.030), vec3(0.030, 0.038, 0.062), upness);

  if (specMask > 0.01){
    // ---------------------------- deep water ----------------------------
    // Real oceans are not a blue surface with a highlight. Light that enters
    // water is absorbed with depth, and red goes first - which is why shallow
    // water is turquoise and deep water is nearly black-blue. Doing the
    // absorption properly is most of what sells an ocean.

    // Depth proxy: how far below the shoreline this point sits.
    float shore = smoothstep(0.0, 0.16, specMask);
    float depth = specMask * oceanDepth;

    // Beer-Lambert absorption per channel: red dies fastest, blue survives.
    vec3 absorb = exp(-depth * vec3(2.35, 0.62, 0.22));
    vec3 shallow = vec3(0.16, 0.62, 0.68);   // sunlit turquoise
    vec3 abyss   = vec3(0.004, 0.020, 0.055); // trench
    vec3 waterCol = mix(abyss, shallow, absorb.b * 0.85 + absorb.g * 0.15);

    // Subsurface glow where light scatters back out of shallow water.
    float scatter = pow(1.0 - clamp(depth, 0.0, 1.0), 2.2) * shore;
    waterCol += vec3(0.05, 0.24, 0.26) * scatter * lam;

    col = mix(col, waterCol * (0.25 + lam * 1.15), specMask);

    // Fresnel: water turns mirror-like at grazing angles.
    float f0 = 0.02;
    float fres = f0 + (1.0 - f0) * pow(1.0 - max(dot(n, V), 0.0), 5.0);

    // Sky reflection on the water rather than a flat blue.
    vec3 skyRefl = mix(vec3(0.05, 0.11, 0.24), vec3(0.35, 0.52, 0.78), max(n.y, 0.0));
    col = mix(col, skyRefl, fres * specMask * 0.75);

    // Sun glitter: many small wave facets, so the highlight is a broken
    // shimmering path rather than one round dot.
    vec3 H = normalize(L + V);
    float a = max(rough * rough, 0.004);
    float ndh = max(dot(n, H), 0.0);
    float d = a * a / (3.14159 * pow(ndh * ndh * (a * a - 1.0) + 1.0, 2.0));

    float ripple = fbm(p * 260.0 + vec3(time * 0.35, time * 0.21, 0.0), 3, 2.4, 0.55);
    float glint = pow(max(ndh, 0.0), 90.0) * (0.55 + 0.45 * ripple);

    col += vec3(1.0, 0.97, 0.9) * (d * 1.1 + glint * 2.2) * specMask * lam * (0.35 + fres);
  }

  if (cloudAmt > 0.01 && ptype > 0.5 && ptype < 3.5){
    vec3 cp = p * 3.1 + vec3(time * 0.012, 0.0, time * 0.006) + seed * 11.0;
    float cl  = fbm(cp, 6, 2.3, 0.55) * 0.5 + 0.5;
    float cl2 = fbm(cp * 2.4 - time * 0.02, 5, 2.4, 0.5) * 0.5 + 0.5;
    float cover = smoothstep(0.52, 0.80, cl * 0.65 + cl2 * 0.45) * cloudAmt;

    // Cloud tops are lit from above and their undersides are shadowed. Sample
    // the field slightly toward the sun to get a cheap self-shadow, which is
    // what gives cloud decks depth instead of looking like a painted decal.
    float above = fbm(cp + L * 0.16, 5, 2.3, 0.55) * 0.5 + 0.5;
    float selfShadow = clamp(1.0 - max(above - cl, 0.0) * 2.6, 0.35, 1.0);

    // forward scattering: clouds glow where they are between you and the star
    float vdl = max(dot(V, -L), 0.0);
    float silver = pow(vdl, 7.0) * 0.85;

    vec3 cloudLit = vec3(1.0) * (diff * 1.25 * selfShadow + 0.04)
                  * sunCol + vec3(1.0, 0.96, 0.9) * silver * diff;
    col = mix(col, cloudLit, clamp(cover, 0.0, 0.94));

    // clouds cast a faint shadow on the surface at grazing light
    col *= 1.0 - cover * 0.18 * (1.0 - diff);
  }

  if (cityLights > 0.01){
    float cont = fbm(p * (1.6 + detail * 2.2) * 0.85 + seed * 37.0, 7, 2.05, 0.55) * 0.5 + 0.5;
    float land = smoothstep(0.50, 0.53, pow(cont, 1.25));
    float grid = fbm(p * 42.0 + seed * 5.0, 4, 2.5, 0.5) * 0.5 + 0.5;
    float lights = smoothstep(0.68, 0.92, grid) * land * (1.0 - day) * cityLights;
    col += vec3(1.0, 0.82, 0.48) * lights * 1.7;
  }

  // ---- atmospheric limb ----
  // Rayleigh scattering is strongly wavelength dependent, so the limb goes
  // blue while the sunset edge goes orange. Two separate powers keep the
  // thin bright rim distinct from the broad haze.
  float fres = 1.0 - max(dot(n, V), 0.0);
  float haze = pow(fres, 2.2);
  float limb = pow(fres, 6.0);

  vec3 rayleigh = vec3(0.16, 0.38, 0.92);
  vec3 sunset   = vec3(1.00, 0.48, 0.20);
  // the sunset colour appears where the light grazes the surface
  float graze = smoothstep(0.35, -0.05, ndl) * smoothstep(-0.35, 0.05, ndl);
  vec3 atmoCol = mix(rayleigh, sunset, clamp(graze * 1.4, 0.0, 1.0));

  col += atmoCol * haze * 0.34 * (day * 0.85 + 0.15);
  col += atmoCol * limb * 0.85 * day;

  // ---- sun glare on the lit limb ----
  // Where the star is almost behind the planet, light spills around the edge.
  float behind = pow(max(dot(V, -L), 0.0), 3.5);
  col += sunCol * behind * limb * 2.2;

  // a faint bluish terminator glow, the atmosphere still lit after sunset
  col += rayleigh * pow(fres, 3.0) * smoothstep(0.30, 0.0, abs(ndl)) * 0.30;

  // Exposure is applied in linear space, before the tone curve - that is
  // the whole point of a filmic curve. Applying it afterwards (as the post
  // pipeline used to) just scales an already-displayable image and blows
  // out the highlights.
  col *= max(exposure, 0.0);
  col = (col * (2.51 * col + 0.03)) / (col * (2.43 * col + 0.59) + 0.14);
  gl_FragColor = vec4(pow(clamp(col, 0.0, 1.0), vec3(1.0 / 2.2)), 1.0);
}
`;

let registered = false;

export function registerPlanetShader(): void {
  if (registered) return;
  Effect.ShadersStore[PLANET_SHADER + 'VertexShader'] = PLANET_VERT;
  Effect.ShadersStore[PLANET_SHADER + 'FragmentShader'] = PLANET_FRAG;
  registered = true;
}
