/**
 * CosmicSkyShader — one procedural sky, sampled by direction.
 *
 * WHY THIS IS A FUNCTION AND NOT A CUBEMAP TEXTURE
 *
 * The obvious way to let a black hole warp the sky is to render the scene
 * into a cubemap and sample it. That means six extra full renders per frame
 * per hole, on a build already running at ~20fps, and it caps the sky at the
 * cube's resolution - so the Einstein ring, which magnifies a tiny patch of
 * sky enormously, would smear into blurry texels exactly where it is most
 * visible.
 *
 * A direction-sampled FUNCTION has neither problem. `cosmicSky(dir)` is
 * infinitely sharp at any magnification, costs nothing to "capture", and is
 * literally the same code in the background dome and inside the hole's
 * raymarcher. The integration is perfect by construction rather than by
 * synchronisation: there is no snapshot that can be stale, no resolution to
 * run out of, and no seam where the two disagree.
 *
 * Mathematically this IS the cubemap - a cube map is just a function from
 * direction to colour. This one is evaluated rather than stored.
 *
 * Every verse gets its own medium through the same entry point, so the hole
 * in the Codeverse warps matrix rain and the hole in the Fractal Core warps
 * Mandelbrot spirals, with no extra plumbing.
 */

/**
 * Shared GLSL. Injected into any shader that needs the sky.
 *
 * Function names are prefixed `sky` to avoid colliding with the noise
 * helpers the hole shader already defines (hash13, noise3, fbm3).
 */
export const COSMIC_SKY_GLSL = `
// ---------------------------------------------------------------- helpers
float skyHash(vec3 p){
  p = fract(p * 0.3183099 + vec3(0.71, 0.113, 0.419));
  p *= 17.0;
  return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float skyNoise(vec3 x){
  vec3 i = floor(x);
  vec3 f = fract(x);
  f = f * f * (3.0 - 2.0 * f);
  return mix(
    mix(mix(skyHash(i + vec3(0,0,0)), skyHash(i + vec3(1,0,0)), f.x),
        mix(skyHash(i + vec3(0,1,0)), skyHash(i + vec3(1,1,0)), f.x), f.y),
    mix(mix(skyHash(i + vec3(0,0,1)), skyHash(i + vec3(1,0,1)), f.x),
        mix(skyHash(i + vec3(0,1,1)), skyHash(i + vec3(1,1,1)), f.x), f.y),
    f.z);
}

/** Fractal noise. Detail on many scales is what makes gas look like gas. */
float skyFbm(vec3 p, int octaves){
  float sum = 0.0;
  float amp = 0.5;
  float norm = 0.0;
  for (int i = 0; i < 7; i++){
    if (i >= octaves) break;
    sum += skyNoise(p) * amp;
    norm += amp;
    amp *= 0.5;
    p *= 2.03;                 // not exactly 2, to avoid axis alignment
  }
  return norm > 0.0 ? sum / norm : 0.0;
}

/**
 * Ridged noise - sharp filaments rather than soft blobs.
 * Real nebulae are shock fronts and filaments, not cotton wool.
 */
float skyRidge(vec3 p, int octaves){
  float sum = 0.0;
  float amp = 0.5;
  float norm = 0.0;
  for (int i = 0; i < 6; i++){
    if (i >= octaves) break;
    float n = 1.0 - abs(skyNoise(p) * 2.0 - 1.0);
    sum += n * n * amp;
    norm += amp;
    amp *= 0.5;
    p *= 2.11;
  }
  return norm > 0.0 ? sum / norm : 0.0;
}

// ------------------------------------------------------------------ stars
/**
 * Star field in direction space.
 *
 * Sampled by direction, so a lensed ray and a straight ray that point the
 * same way see the SAME star. That is what makes an Einstein ring show a
 * genuine second image rather than unrelated noise.
 */
vec3 skyStars(vec3 d, float density, float scaleBase){
  vec3 col = vec3(0.0);
  for (int oct = 0; oct < 3; oct++){
    float scale = scaleBase * pow(2.7, float(oct));
    vec3 p = d * scale;
    vec3 cell = floor(p);
    vec3 f = p - cell;
    for (int gx = -1; gx <= 1; gx++){
      for (int gy = -1; gy <= 1; gy++){
        for (int gz = -1; gz <= 1; gz++){
          vec3 g = vec3(float(gx), float(gy), float(gz));
          vec3 id = cell + g;
          float h = skyHash(id);
          if (h < 1.0 - density) continue;
          vec3 j = vec3(skyHash(id + 17.1), skyHash(id + 39.7), skyHash(id + 71.3));
          float dist = length(f - g - j);
          float star = exp(-dist * dist * 260.0);
          float temp = skyHash(id + 5.5);
          // Most stars are cool. An even spread of colour reads as confetti.
          vec3 tint = temp < 0.74
            ? vec3(1.0, 0.70 + temp * 0.32, 0.52 + temp * 0.42)
            : vec3(0.70, 0.83, 1.0);
          col += tint * star * (0.30 + h * 0.70) / (1.0 + float(oct) * 1.5);
        }
      }
    }
  }
  return col;
}

// ------------------------------------------------------------- the galaxy
/**
 * The Milky Way: a bright band of unresolved starlight, threaded with DARK
 * DUST LANES and lit by coloured emission nebulae.
 *
 * The dust is the part that was missing. A real galaxy is not a bright
 * smear - it is a bright smear with black rivers torn through it, because
 * cold dust in the disc absorbs the light of everything behind it. Without
 * that absorption the band reads as a smudge; with it, it reads as the
 * Milky Way.
 */
vec3 skyGalaxy(vec3 d, float bandTilt){
  // Rotate so the galactic plane is not aligned with the world axes.
  float c = cos(bandTilt), s = sin(bandTilt);
  vec3 g = vec3(d.x, d.y * c - d.z * s, d.y * s + d.z * c);

  // Height above the galactic plane, 0 in the band.
  float h = g.y;

  // The band itself: unresolved starlight, brightest toward the core.
  float band = exp(-h * h * 26.0);
  // The core is a direction, not a place - one side of the sky is brighter.
  float core = pow(max(0.0, g.x * 0.5 + 0.5), 3.0);
  float bulge = exp(-(h * h * 60.0 + pow(1.0 - core, 2.0) * 8.0));

  // Unresolved stellar glow.
  float grain = skyFbm(g * 5.5, 5);
  float glow = band * (0.45 + 0.55 * grain) * (0.55 + core * 0.85);
  glow += bulge * 1.5;

  vec3 col = vec3(0.62, 0.66, 0.78) * glow * 0.5;
  // The core runs warmer - older, redder stars.
  col += vec3(0.55, 0.42, 0.26) * bulge * 0.55;

  // ---- dust lanes ----
  // Absorption, not addition. This is why it looks like a galaxy.
  float dust = skyRidge(g * 7.0 + 3.3, 5);
  dust *= exp(-h * h * 42.0);                 // dust hugs the plane
  dust = smoothstep(0.35, 0.95, dust);
  col *= 1.0 - dust * 0.92;

  // ---- emission nebulae ----
  // Clumps of ionised gas sitting in the plane, mostly near the core.
  float neb = skyFbm(g * 3.1 + 19.7, 4);
  neb = smoothstep(0.52, 0.95, neb) * exp(-h * h * 30.0);
  float neb2 = skyFbm(g * 6.3 - 7.1, 4);
  neb2 = smoothstep(0.58, 1.0, neb2) * exp(-h * h * 34.0);

  // Hydrogen-alpha red and oxygen teal, the two colours real nebulae show.
  col += vec3(0.85, 0.18, 0.24) * neb * 0.42 * (0.4 + core);
  col += vec3(0.14, 0.42, 0.62) * neb2 * 0.30;

  // ---- faint high-latitude cirrus ----
  // Keeps the sky away from the poles from being flat black.
  float cirrus = skyFbm(g * 2.2 - 41.0, 4);
  cirrus = smoothstep(0.62, 1.0, cirrus);
  col += vec3(0.10, 0.09, 0.17) * cirrus * 0.5;

  return col;
}

// --------------------------------------------------------- verse mediums
/** Codeverse: cascading columns of green data. */
vec3 skyCode(vec3 d, float t){
  // Columns in the horizontal angle, rows falling in the vertical.
  float ang = atan(d.z, d.x);
  float col = floor(ang * 34.0);
  float speed = 0.35 + skyHash(vec3(col, 1.0, 3.0)) * 1.1;
  float y = d.y * 22.0 + t * speed;
  float row = floor(y);
  float cellId = skyHash(vec3(col, row, 7.0));

  // A glyph is on or off, and the leading character of each run is bright.
  float lit = step(0.55, cellId);
  float head = step(0.965, fract(cellId * 7.7 + t * speed * 0.25));
  float glyph = fract(sin((col * 31.7 + row * 17.3)) * 43758.5453);
  float onOff = step(0.4, glyph);

  float body = lit * onOff * 0.5;
  vec3 c = vec3(0.15, 1.0, 0.35) * body;
  c += vec3(0.75, 1.0, 0.85) * head * onOff;
  // Fade columns with distance from the horizon so it reads as depth.
  return c * (0.35 + 0.65 * exp(-abs(d.y) * 1.6));
}

/** Shapeverses: a rotating wireframe lattice. */
vec3 skyLattice(vec3 d, float symmetry, float t){
  float sym = max(3.0, symmetry);
  // Fold the sky into sym identical wedges - the verse's own symmetry.
  float ang = atan(d.z, d.x) + t * 0.05;
  float wedge = 6.28318 / sym;
  float a = mod(ang, wedge) - wedge * 0.5;
  float rad = length(vec2(length(d.xz), d.y));

  // Snap to a lattice and draw the cell edges.
  vec3 q = vec3(a * 6.0, d.y * 7.0, rad * 5.0);
  vec3 cell = abs(fract(q) - 0.5);
  float edge = 1.0 - smoothstep(0.0, 0.06, min(min(cell.x, cell.y), cell.z));

  // Brighter where lattice lines cross - the vertices of the structure.
  float vert = 1.0 - smoothstep(0.0, 0.12, length(cell));
  return vec3(0.42, 0.66, 0.95) * edge * 0.30 + vec3(0.85, 0.92, 1.0) * vert * 0.55;
}

/**
 * Fractal Core: the Mandelbrot set, escape-time, evaluated per pixel.
 *
 * Z = Z^2 + C on the GPU. The direction is projected onto the complex plane
 * and the zoom factor scales it, so flying deeper genuinely magnifies it rather
 * than scaling a picture of it.
 */
vec3 skyMandelbrot(vec3 d, float zoom, float t){
  // Project the view direction onto a plane in the complex domain.
  vec2 uv = vec2(atan(d.z, d.x) * 0.6, asin(clamp(d.y, -1.0, 1.0)) * 0.9);
  vec2 c = vec2(-0.743643887037151, 0.131825904205330) + uv / max(zoom, 0.001);

  // Iterations must GROW with magnification. Detail in the Mandelbrot is
  // resolved by iteration count, so a fixed budget goes blank at depth:
  // measured on this exact centre, 96 iterations resolves 27 distinct
  // escape times at 10,000x and exactly 1 at 100,000x - a flat void. The
  // budget therefore scales with log2(zoom), and is only paid where it is
  // needed, so the shallow view stays cheap.
  float budget = clamp(96.0 + log2(max(zoom, 1.0)) * 22.0, 96.0, 340.0);

  vec2 z = vec2(0.0);
  float iter = 0.0;
  for (int i = 0; i < 340; i++){
    if (float(i) >= budget) break;
    z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
    if (dot(z, z) > 256.0) break;
    iter += 1.0;
  }

  float MAX_ITER = budget;
  if (iter >= MAX_ITER) return vec3(0.0);      // inside the set: true black

  // Smooth iteration count, so bands are continuous rather than stepped.
  float sm = iter + 1.0 - log2(max(log2(length(z)), 1e-4));
  float v = sm / MAX_ITER;
  vec3 col = 0.5 + 0.5 * cos(6.28318 * (v * 2.4 + vec3(0.0, 0.33, 0.67)) + t * 0.05);
  return col * pow(v, 0.65) * 0.85;
}

/**
 * THE SKY.
 *
 * medium: 0 stars, 1 technology, 2 code, 3 geometry, 4 fractal,
 *         5 string, 6 void.
 */
/*
 * Very distant galaxies: unresolved smudges far outside our own.
 *
 * This is deliberately NOT our Milky Way - that is real geometry now. It
 * exists so the sky between the real stars is not dead black, and it is
 * kept dim and structureless so nothing here can be mistaken for a place
 * you could fly to.
 */
vec3 skyDeepField(vec3 d){
  float n = skyFbm(d * 2.4 + 61.3, 4);
  float clump = smoothstep(0.62, 1.0, n);
  vec3 warm = vec3(0.10, 0.09, 0.13);
  vec3 cool = vec3(0.06, 0.08, 0.14);
  float pick = skyNoise(d * 5.1 - 12.0);
  return mix(cool, warm, pick) * clump * 0.55;
}

vec3 cosmicSky(vec3 dir, float medium, float symmetry, vec3 tint,
               float strangeness, float t, float zoom){
  vec3 d = normalize(dir);
  vec3 col = vec3(0.0);

  if (medium < 0.5){
    // ORDINARY SPACE: NO PAINTED GALAXY.
    //
    // The Milky Way is real geometry now - 30,000 stars and 9,000 gas
    // points at true coordinates out to radius 50,000 (GalaxyField). A
    // painted band here would be a SECOND Milky Way drawn at infinite
    // distance on top of the reachable one: it would never move as you
    // flew toward it, so the real galaxy would visibly slide against a
    // stuck copy of itself. Only the faintest deep-field grain remains,
    // which is unresolved distant galaxies rather than our own.
    col = skyStars(d, 0.16, 42.0) * 1.35;
    col += skyDeepField(d) * 0.5;
  } else if (medium < 1.5){
    // Technology: a cold structural grid over deep field.
    col = skyStars(d, 0.08, 38.0) * 0.7;
    col += skyDeepField(d) * 0.3;
    col += skyLattice(d, 4.0, t) * 0.55;
    col += vec3(0.05, 0.24, 0.26) * 0.5;
  } else if (medium < 2.5){
    col = skyCode(d, t) + skyStars(d, 0.04, 30.0) * 0.25;
  } else if (medium < 3.5){
    col = skyLattice(d, symmetry, t) + skyStars(d, 0.05, 26.0) * 0.4;
  } else if (medium < 4.5){
    col = skyMandelbrot(d, zoom, t) + skyStars(d, 0.03, 24.0) * 0.3;
  } else if (medium < 5.5){
    // String: everything collapsed onto one blazing line.
    float line = exp(-pow(d.y * 34.0, 2.0));
    col = vec3(0.85, 0.86, 0.95) * line * 0.9;
    col += skyStars(d, 0.05, 30.0) * 0.35;
  } else {
    // Void: the infinite cube of stars.
    vec3 q = abs(fract(d * 9.0) - 0.5);
    float grid = 1.0 - smoothstep(0.0, 0.05, min(min(q.x, q.y), q.z));
    col = vec3(0.55, 0.55, 0.62) * grid * 0.35 + skyStars(d, 0.22, 46.0);
  }

  // The verse tint is a floor for the EXOTIC verses only.
  //
  // In ordinary space it must not exist at all. tint defaults to
  // (0.06, 0.10, 0.22), so this line was adding a flat (0.018, 0.030,
  // 0.066) navy over every pixel of the sky - that is the "space is still
  // a bit blue" wash. Intergalactic vacuum has to be genuinely black or
  // the faint galaxies have nothing to be brighter than, and the fog has
  // nothing to glow against. The strange verses still get their floor,
  // because there "empty" is a look rather than a vacuum.
  if (medium >= 0.5) col += tint * 0.30;

  // Strangeness pushes the palette away from anything natural.
  if (strangeness > 0.001){
    vec3 odd = vec3(
      col.r * (1.0 + strangeness * 0.5),
      col.g * (1.0 - strangeness * 0.28),
      col.b * (1.0 + strangeness * 0.75));
    col = mix(col, odd, clamp(strangeness, 0.0, 1.0));
  }

  return max(col, vec3(0.0));
}
`;

/** Numeric medium ids, shared by TypeScript and GLSL. */
export const SKY_MEDIUM: Record<string, number> = {
  stars: 0,
  technology: 1,
  code: 2,
  geometry: 3,
  fractal: 4,
  string: 5,
  void: 6
};

/** Maps a verse medium name to the id the shader branches on. */
export function mediumId(medium: string): number {
  const v = SKY_MEDIUM[medium];
  return typeof v === 'number' ? v : 0;
}

// ----------------------------------------------------------- the sky dome

export const SKY_VERT = `
precision highp float;
attribute vec3 position;
uniform mat4 worldViewProjection;
varying vec3 vDir;
void main(void){
  vDir = position;
  // The dome is drawn at the far plane with depth writing off, so its own
  // radius never matters and it can never occlude anything.
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

export const SKY_FRAG = `
precision highp float;
varying vec3 vDir;

uniform float medium;
uniform float symmetry;
uniform vec3  tint;
uniform float strangeness;
uniform float time;
uniform float zoom;
uniform float exposure;

${COSMIC_SKY_GLSL}

void main(void){
  vec3 col = cosmicSky(vDir, medium, symmetry, tint, strangeness, time, zoom);
  col *= exposure;

  // Tone map so bright nebular cores do not clip to flat white.
  col = (col * (2.51 * col + 0.03)) / (col * (2.43 * col + 0.59) + 0.14);
  col = pow(clamp(col, 0.0, 1.0), vec3(1.0 / 2.2));

  gl_FragColor = vec4(col, 1.0);
}
`;

export const COSMIC_SKY_SHADER = 'cosmicSky';
