/* ---------------------------------------------------------------------------
 * Ocean Worlds — terrain generation, height texture, planet presets
 * Global: window.WORLD  (requires shared/util.js)
 * ------------------------------------------------------------------------- */
(function () {
  const { mulberry32, clamp, lerp, smooth } = UTILS;

  /* Deterministic 2D value noise */
  function makeNoise2D(seed) {
    const rng = mulberry32(seed);
    const perm = new Uint8Array(512);
    const grad = new Float32Array(256);
    const tmp = [...Array(256).keys()];
    for (let i = 255; i > 0; i--) { const j = (rng() * (i + 1)) | 0; [tmp[i], tmp[j]] = [tmp[j], tmp[i]]; }
    for (let i = 0; i < 512; i++) perm[i] = tmp[i & 255];
    for (let i = 0; i < 256; i++) grad[i] = rng();
    function h(xi, yi) { return grad[perm[(perm[xi & 255] + yi) & 255]]; }
    function noi2(x, y) {
      const xi = Math.floor(x), yi = Math.floor(y);
      const xf = x - xi, yf = y - yi;
      const u = xf * xf * (3 - 2 * xf), v = yf * yf * (3 - 2 * yf);
      return lerp(lerp(h(xi, yi), h(xi + 1, yi), u), lerp(h(xi, yi + 1), h(xi + 1, yi + 1), u), v);
    }
    function fbm(x, y, oct) {
      let a = 0.5, r = 0, fx = x, fy = y;
      for (let i = 0; i < oct; i++) { r += a * noi2(fx, fy); fx = fx * 2.03 + 13.7; fy = fy * 2.03 + 5.1; a *= 0.5; }
      return r; // ~0..1
    }
    return { noi2, fbm };
  }

  const SIZE = 600;          // terrain extent (m)
  const SUB = 176;           // grid subdivisions
  const HMIN = -22, HMAX = 20;

  function heightAt(cfg, noise, x, z) {
    const d = Math.sqrt(x * x + z * z) / cfg.radius;
    let h;
    const base = noise.fbm(x * 0.009 + 31.7, z * 0.009 + 11.3, 5);
    const det = noise.fbm(x * 0.045 + 7.7, z * 0.045 + 3.1, 4);
    if (cfg.ring) {
      const band = Math.exp(-Math.pow((d - 0.72) * 3.2, 2));
      h = cfg.height * band * (0.55 + 0.7 * base) + cfg.height * 0.12 * base;
      h -= (1 - band) * cfg.floorDepth * 0.4;
    } else {
      const fall = clamp(1 - Math.pow(d, 2.4), 0, 1);
      h = (base * 0.8 + det * 0.2) * cfg.height * fall + cfg.height * 0.10 * fall;
    }
    h -= cfg.floorDepth * clamp(1 - 1 / Math.max(d, 0.35), 0, 1);
    return h;
  }

  function paletteFor(cfg) { return cfg.palette; }

  function buildTerrain(scene, cfg, noise) {
    const g = BABYLON.MeshBuilder.CreateGround("island", { width: SIZE, height: SIZE, subdivisions: SUB, updatable: true }, scene);
    const pos = g.getVerticesData(BABYLON.VertexBuffer.PositionKind);
    const nor = new Float32Array(pos.length);
    const colors = new Float32Array((pos.length / 3) * 4);
    const P = paletteFor(cfg);
    const cFloor = UTILS.hexToRgb(P.floor), cSand = UTILS.hexToRgb(P.sand),
          cGrass = UTILS.hexToRgb(P.grass), cRock = UTILS.hexToRgb(P.rock), cSnow = UTILS.hexToRgb(P.snow);
    const jitter = (n, x, z) => (noise.noi2(x * 0.13 + 99, z * 0.13 + 51) - 0.5) * n;
    for (let i = 0; i < pos.length; i += 3) {
      const x = pos[i], z = pos[i + 2];
      const h = heightAt(cfg, noise, x, z);
      pos[i + 1] = h;
    }
    g.updateVerticesData(BABYLON.VertexBuffer.PositionKind, pos);
    const idx = g.getIndices();
    BABYLON.VertexData.ComputeNormals(pos, idx, nor);
    g.updateVerticesData(BABYLON.VertexBuffer.NormalKind, nor);
    for (let i = 0, vi = 0; i < pos.length; i += 3, vi += 4) {
      const x = pos[i], h = pos[i + 1], z = pos[i + 2];
      const nx = nor[i], ny = nor[i + 1], nz = nor[i + 2];
      const slope = 1 - ny; // 0 flat
      let r, gg, b;
      const sand = smooth(1.6, 0.4, h);
      const snow = smooth(cfg.height * 0.62, cfg.height * 0.9, h + jitter(3, x, z));
      const rocky = smooth(0.28, 0.45, slope + jitter(0.12, x, z));
      r = cGrass.r; gg = cGrass.g; b = cGrass.b;
      r = lerp(r, cSand.r, sand); gg = lerp(gg, cSand.g, sand); b = lerp(b, cSand.b, sand);
      r = lerp(r, cRock.r, rocky); gg = lerp(gg, cRock.g, rocky); b = lerp(b, cRock.b, rocky);
      r = lerp(r, cSnow.r, snow); gg = lerp(gg, cSnow.g, snow); b = lerp(b, cSnow.b, snow);
      const und = smooth(0.4, -2.0, h); // underwater → sea floor tint
      r = lerp(r, cFloor.r, und); gg = lerp(gg, cFloor.g, und); b = lerp(b, cFloor.b, und);
      const sh = 0.92 + jitter(0.16, x * 3.1, z * 3.1);
      colors[vi] = clamp(r * sh, 0, 1); colors[vi + 1] = clamp(gg * sh, 0, 1); colors[vi + 2] = clamp(b * sh, 0, 1); colors[vi + 3] = 1;
    }
    g.setVerticesData(BABYLON.VertexBuffer.ColorKind, colors);
    g.useVertexColors = true;
    return g;
  }

  function buildHeightTexture(scene, cfg, noise) {
    const S = 256;
    const data = new Uint8Array(S * S * 4);
    for (let py = 0; py < S; py++) {
      for (let px = 0; px < S; px++) {
        const x = (px / (S - 1) - 0.5) * SIZE;
        const z = (py / (S - 1) - 0.5) * SIZE;   // row py == v == z (RawTexture: no flip)
        const h = heightAt(cfg, noise, x, z);
        const v = clamp((h - HMIN) / (HMAX - HMIN), 0, 1) * 255;
        const o = (py * S + px) * 4;
        data[o] = v; data[o + 1] = v; data[o + 2] = v; data[o + 3] = 255;
      }
    }
    const tex = BABYLON.RawTexture.CreateRGBATexture(data, S, S, scene, true, false, BABYLON.Texture.BILINEAR_SAMPLINGMODE);
    tex.wrapU = BABYLON.Texture.CLAMP_ADDRESSMODE;
    tex.wrapV = BABYLON.Texture.CLAMP_ADDRESSMODE;
    return tex;
  }

  /* ----------------------------------------------------- planet presets -- */
  const PLANETS = [
    {
      id: "terran", name: "🌍 Terran Ocean", time: 10.6, seed: 7,
      sky: { horizon: "#a9cbe6", zenith: "#2f6db2", sunTint: "#fff1d8", cloud: 0.30, cloudScale: 1.0, aurora: 0, star: 1.0, fog: 0.0012, fogColor: "#a7c4de", planet: null },
      water: { deep: "#07304a", shallow: "#1ba5ac", foam: "#eef7ff", clarity: 0.14, foam: 1.0, detail: 0.55, refl: 1.0, glow: 0, glowColor: "#39f0ff", lava: false },
      ocean: { amp: 1.0, len: 46, chop: 0.5, speed: 1.0, count: 12, dir: 0.8 },
      terrain: { radius: 92, height: 12, floorDepth: 15, seed: 7, ring: false,
        palette: { floor: "#3a4a52", sand: "#cbb489", grass: "#4f7c3c", rock: "#6d6a63", snow: "#eef2f5" } }
    },
    {
      id: "arctic", name: "🧊 Arctic Reach", time: 8.2, seed: 21, spawnBergs: true,
      sky: { horizon: "#c3d5e2", zenith: "#5d83a8", sunTint: "#ffdcae", cloud: 0.34, cloudScale: 0.8, aurora: 0.55, star: 1.1, fog: 0.0021, fogColor: "#b9cdda", planet: null },
      water: { deep: "#0a2331", shallow: "#3f8d99", foam: "#f4fbff", clarity: 0.20, foam: 1.15, detail: 0.5, refl: 1.05, glow: 0, glowColor: "#39f0ff", lava: false },
      ocean: { amp: 0.8, len: 34, chop: 0.45, speed: 0.8, count: 11, dir: 2.4 },
      terrain: { radius: 100, height: 15, floorDepth: 14, seed: 21, ring: false,
        palette: { floor: "#44545e", sand: "#d9e2e8", grass: "#8fa5a8", rock: "#77848c", snow: "#ffffff" } }
    },
    {
      id: "oasis", name: "🏜️ Desert Oasis", time: 12.4, seed: 33,
      sky: { horizon: "#e8d7ae", zenith: "#3f7fc0", sunTint: "#fff6e2", cloud: 0.12, cloudScale: 1.2, aurora: 0, star: 0.9, fog: 0.0016, fogColor: "#dcc9a2", planet: null },
      water: { deep: "#0b4b53", shallow: "#39d2c0", foam: "#f4fffa", clarity: 0.10, foam: 0.8, detail: 0.6, refl: 1.0, glow: 0, glowColor: "#39f0ff", lava: false },
      ocean: { amp: 0.45, len: 22, chop: 0.4, speed: 1.1, count: 10, dir: 1.6 },
      terrain: { radius: 52, height: 5.5, floorDepth: 10, seed: 33, ring: false,
        palette: { floor: "#5f5a44", sand: "#e3cd96", grass: "#b7a35f", rock: "#8d7f5e", snow: "#efe6c8" } }
    },
    {
      id: "volcanic", name: "🌋 Pyrrhos Reach", time: 17.3, seed: 47,
      sky: { horizon: "#4a2c28", zenith: "#211a26", sunTint: "#ff9a55", cloud: 0.66, cloudScale: 0.7, aurora: 0, star: 0.8, fog: 0.003, fogColor: "#3a2320", planet: null },
      water: { deep: "#1a0c08", shallow: "#5c2110", foam: "#ffd9b0", clarity: 0.2, foam: 0.6, detail: 0.8, refl: 0.7, glow: 0, glowColor: "#ff6a22", lava: true },
      ocean: { amp: 1.4, len: 40, chop: 0.6, speed: 0.55, count: 12, dir: 3.6 },
      terrain: { radius: 80, height: 14, floorDepth: 12, seed: 47, ring: false,
        palette: { floor: "#241a18", sand: "#4a3a34", grass: "#33231f", rock: "#1f1a1c", snow: "#5c2a1a" } }
    },
    {
      id: "biolume", name: "✨ Noctis Biolume", time: 23.6, seed: 58,
      sky: { horizon: "#0c1830", zenith: "#070b1e", sunTint: "#b8c8ff", cloud: 0.10, cloudScale: 1.0, aurora: 0.85, star: 1.5, fog: 0.0016, fogColor: "#0a1226", planet: null },
      water: { deep: "#041224", shallow: "#0a3550", foam: "#bfefff", clarity: 0.12, foam: 0.9, detail: 0.6, refl: 0.9, glow: 1.4, glowColor: "#2fe8ff", lava: false },
      ocean: { amp: 0.9, len: 36, chop: 0.5, speed: 0.9, count: 12, dir: 0.2 },
      terrain: { radius: 85, height: 10, floorDepth: 13, seed: 58, ring: false,
        palette: { floor: "#0c1626", sand: "#223448", grass: "#14283a", rock: "#1a2333", snow: "#274a63" } }
    },
    {
      id: "storm", name: "🪐 Kharon Storm", time: 18.8, seed: 66,
      sky: { horizon: "#5d6a72", zenith: "#232c36", sunTint: "#cfd8e8", cloud: 0.92, cloudScale: 0.55, aurora: 0.15, star: 0.6, fog: 0.0026, fogColor: "#4c585f",
        planet: { dir: [0.42, 0.38, -0.55], size: 0.42, a: "#c9ae84", b: "#7c6750" } },
      water: { deep: "#0e2126", shallow: "#2a5a60", foam: "#e8f2f4", clarity: 0.22, foam: 1.5, detail: 0.75, refl: 0.9, glow: 0, glowColor: "#39f0ff", lava: false },
      ocean: { amp: 2.3, len: 72, chop: 0.85, speed: 1.25, count: 14, dir: 5.0 },
      terrain: { radius: 70, height: 9, floorDepth: 14, seed: 66, ring: false,
        palette: { floor: "#2c383a", sand: "#5d6a66", grass: "#37474b", rock: "#414a50", snow: "#77848a" } }
    },
    {
      id: "atoll", name: "🏝️ Emerald Atoll", time: 15.8, seed: 77,
      sky: { horizon: "#c8e4e2", zenith: "#3f8ab8", sunTint: "#fff4dd", cloud: 0.26, cloudScale: 0.9, aurora: 0, star: 1.0, fog: 0.0014, fogColor: "#b7d8d6", planet: null },
      water: { deep: "#075a63", shallow: "#4fe0c8", foam: "#f2fffb", clarity: 0.09, foam: 1.1, detail: 0.55, refl: 1.0, glow: 0, glowColor: "#39f0ff", lava: false },
      ocean: { amp: 0.6, len: 30, chop: 0.4, speed: 1.0, count: 11, dir: 4.2 },
      terrain: { radius: 110, height: 5, floorDepth: 16, seed: 77, ring: true,
        palette: { floor: "#1a4a50", sand: "#e8ddb0", grass: "#3f7c46", rock: "#5d6a5e", snow: "#eef2e8" } }
    }
  ];

  window.WORLD = { makeNoise2D, heightAt, buildTerrain, buildHeightTexture, PLANETS, SIZE, HMIN, HMAX };
})();
