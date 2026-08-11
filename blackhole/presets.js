/* ---------------------------------------------------------------------------
 * Singularity Vault — curated presets + deterministic seeded "forge"
 * (any 32-bit seed → a fully unique black hole: 4,294,967,296 combinations)
 * Global: window.BHPRESETS
 * ------------------------------------------------------------------------- */
(function () {
  const { mulberry32 } = UTILS;

  /* Parameter schema (keys shared with main.js P object):
   * rs, spin, lens, diskInner, diskOuter, temp, bright, beaming, tiltX, tiltZ,
   * swirl, ring, star, nebula, band, tintA, tintB, exposure, bloom, ca, grain,
   * vignette, fractK, fractGlow */

  const CURATED = [
    { id: "gargantua", label: "Gargantua — Interstellar homage",
      params: { rs: 1.0, spin: 0.92, lens: 1.0, diskInner: 2.9, diskOuter: 12.5, temp: 1.15, bright: 2.3, beaming: 0.92,
        tiltX: 14, tiltZ: 3, swirl: 1.0, ring: 1.2, star: 1.0, nebula: 0.55, band: 1.05,
        tintA: "#241a3f", tintB: "#c8722a", exposure: 1.12, bloom: 0.75, ca: 2.5, grain: 10, vignette: 0.5 } },
    { id: "m87", label: "M87* — Event Horizon Telescope",
      params: { rs: 1.15, spin: 0.7, lens: 1.0, diskInner: 3.4, diskOuter: 10, temp: 0.85, bright: 1.9, beaming: 1.0,
        tiltX: 20, tiltZ: -8, swirl: 0.7, ring: 1.0, star: 0.7, nebula: 0.4, band: 0.7,
        tintA: "#1a1420", tintB: "#d98d3a", exposure: 1.15, bloom: 0.85, ca: 3, grain: 14, vignette: 0.55 } },
    { id: "sgra", label: "Sagittarius A* — Our Core",
      params: { rs: 0.9, spin: 0.6, lens: 1.0, diskInner: 3.2, diskOuter: 8.5, temp: 0.7, bright: 1.5, beaming: 0.8,
        tiltX: -30, tiltZ: 12, swirl: 0.55, ring: 0.9, star: 1.35, nebula: 0.8, band: 1.2,
        tintA: "#141c30", tintB: "#b06a3a", exposure: 1.1, bloom: 0.7, ca: 2, grain: 12, vignette: 0.5 } },
    { id: "cygnus", label: "Cygnus X-1 — Stellar Feast",
      params: { rs: 0.8, spin: 0.95, lens: 1.05, diskInner: 2.35, diskOuter: 9, temp: 1.9, bright: 3.2, beaming: 1.0,
        tiltX: 8, tiltZ: -4, swirl: 1.6, ring: 1.3, star: 0.9, nebula: 0.6, band: 0.8,
        tintA: "#0f1c30", tintB: "#4a7ae0", exposure: 1.05, bloom: 0.9, ca: 2, grain: 8, vignette: 0.45 } },
    { id: "ton618", label: "TON 618 — Ultramassive Quasar",
      params: { rs: 1.6, spin: 0.8, lens: 1.0, diskInner: 3.0, diskOuter: 17, temp: 1.6, bright: 4.2, beaming: 0.95,
        tiltX: 4, tiltZ: 0, swirl: 0.9, ring: 1.4, star: 0.55, nebula: 0.35, band: 0.5,
        tintA: "#101426", tintB: "#d9b45a", exposure: 0.95, bloom: 1.1, ca: 3, grain: 10, vignette: 0.5 } },
    { id: "starfield", label: "The Eye — Starfield drift",
      params: { rs: 1.0, spin: 0.85, lens: 1.1, diskInner: 2.7, diskOuter: 13, temp: 2.3, bright: 2.0, beaming: 0.75,
        tiltX: 24, tiltZ: 10, swirl: 1.2, ring: 1.5, star: 1.2, nebula: 0.9, band: 0.9,
        tintA: "#0c2030", tintB: "#3fd8c8", exposure: 1.08, bloom: 0.85, ca: 2, grain: 8, vignette: 0.42 } },
    { id: "quasar", label: "3C 273 — Beacon Quasar",
      params: { rs: 1.3, spin: 0.99, lens: 1.0, diskInner: 2.5, diskOuter: 15, temp: 2.6, bright: 3.8, beaming: 1.0,
        tiltX: 2, tiltZ: -2, swirl: 1.4, ring: 1.2, star: 0.6, nebula: 0.4, band: 0.6,
        tintA: "#141030", tintB: "#7a5ae0", exposure: 1.0, bloom: 1.2, ca: 3.5, grain: 9, vignette: 0.5 } },
    { id: "phoenix", label: "Phoenix — Ember Crown",
      params: { rs: 1.05, spin: 0.5, lens: 1.0, diskInner: 3.1, diskOuter: 11, temp: 1.0, bright: 2.6, beaming: 0.7,
        tiltX: -14, tiltZ: 6, swirl: 0.8, ring: 1.0, star: 0.8, nebula: 1.1, band: 0.7,
        tintA: "#2a0f14", tintB: "#e0452a", exposure: 1.12, bloom: 1.0, ca: 3, grain: 11, vignette: 0.55 } },
    { id: "obsidian", label: "Obsidian Halo — Thin Whisper",
      params: { rs: 1.0, spin: 0.4, lens: 1.15, diskInner: 4.2, diskOuter: 7.5, temp: 0.55, bright: 1.1, beaming: 0.55,
        tiltX: 35, tiltZ: -14, swirl: 0.4, ring: 1.7, star: 1.5, nebula: 0.7, band: 1.1,
        tintA: "#171226", tintB: "#8a6ae0", exposure: 1.15, bloom: 0.6, ca: 2, grain: 14, vignette: 0.6 } },
    { id: "aurorabloom", label: "Aurora Bloom — Verdant Veil",
      params: { rs: 0.95, spin: 0.75, lens: 1.0, diskInner: 2.8, diskOuter: 12, temp: 1.4, bright: 2.1, beaming: 0.85,
        tiltX: 18, tiltZ: -10, swirl: 1.1, ring: 1.1, star: 1.1, nebula: 1.3, band: 1.0,
        tintA: "#0f2418", tintB: "#3fd87a", exposure: 1.1, bloom: 0.9, ca: 2.2, grain: 9, vignette: 0.45 } },
    { id: "frostbloom", label: "Frostbloom — Dying Light",
      params: { rs: 0.85, spin: 0.3, lens: 0.95, diskInner: 3.6, diskOuter: 9.5, temp: 1.1, bright: 1.4, beaming: 0.6,
        tiltX: -42, tiltZ: 20, swirl: 0.5, ring: 0.8, star: 1.6, nebula: 1.0, band: 1.3,
        tintA: "#101c2c", tintB: "#6ab4e0", exposure: 1.18, bloom: 0.7, ca: 2, grain: 12, vignette: 0.5 } },
    { id: "bloodmoon", label: "Sanguine Maw — Blood Orbit",
      params: { rs: 1.1, spin: 0.88, lens: 1.05, diskInner: 2.6, diskOuter: 10.5, temp: 0.75, bright: 2.4, beaming: 0.9,
        tiltX: 10, tiltZ: -18, swirl: 1.3, ring: 1.2, star: 0.9, nebula: 0.7, band: 0.8,
        tintA: "#200c12", tintB: "#e02a3f", exposure: 1.1, bloom: 0.95, ca: 3, grain: 10, vignette: 0.55 } },
    { id: "voidwalker", label: "Voidwalker — Almost Nothing",
      params: { rs: 1.0, spin: 0.5, lens: 1.2, diskInner: 3.0, diskOuter: 6, temp: 0.35, bright: 0.55, beaming: 0.4,
        tiltX: 60, tiltZ: 8, swirl: 0.25, ring: 2.0, star: 1.8, nebula: 0.5, band: 1.4,
        tintA: "#0c0f18", tintB: "#5a6480", exposure: 1.2, bloom: 0.5, ca: 1.5, grain: 16, vignette: 0.65 } },
    { id: "halo", label: "Coronae — Twin Halo",
      params: { rs: 0.9, spin: 0.65, lens: 1.1, diskInner: 2.45, diskOuter: 14, temp: 1.8, bright: 2.8, beaming: 1.0,
        tiltX: 0, tiltZ: 0, swirl: 1.5, ring: 1.6, star: 1.0, nebula: 0.8, band: 0.9,
        tintA: "#1c1428", tintB: "#e0b45a", exposure: 1.05, bloom: 1.05, ca: 2.5, grain: 8, vignette: 0.45 } },
    { id: "kraken", label: "Kraken — Abyssal Coil",
      params: { rs: 1.2, spin: 0.55, lens: 1.05, diskInner: 3.0, diskOuter: 13, temp: 0.8, bright: 1.8, beaming: 0.65,
        tiltX: -22, tiltZ: 15, swirl: 0.65, ring: 0.9, star: 0.5, nebula: 1.7, band: 1.4,
        tintA: "#04121c", tintB: "#1a7a6a", ringCol: "#7ae0c8", diskTint: "#9fd8ff", nebScale: 1.8,
        exposure: 1.08, bloom: 0.85, ca: 2.2, grain: 9, vignette: 0.55 } },
    { id: "forgefire", label: "Forgefire — Molten Anvil",
      params: { rs: 1.05, spin: 0.9, lens: 1.0, diskInner: 2.35, diskOuter: 9.5, temp: 3.1, bright: 4.6, beaming: 0.9,
        tiltX: 12, tiltZ: -6, swirl: 1.9, ring: 1.4, star: 0.6, nebula: 0.35, band: 0.5,
        tintA: "#1c0d08", tintB: "#ff5a2a", ringCol: "#ffd9a0", diskTint: "#ffd0b0", nebScale: 0.8,
        exposure: 1.02, bloom: 1.25, ca: 3, grain: 8, vignette: 0.5 } },
    { id: "sapphire", label: "Sapphire Choir — Ice Aria",
      params: { rs: 0.95, spin: 0.4, lens: 1.05, diskInner: 3.3, diskOuter: 11, temp: 0.55, bright: 1.35, beaming: 0.5,
        tiltX: 28, tiltZ: -12, swirl: 0.45, ring: 1.5, star: 1.7, nebula: 1.15, band: 1.1,
        tintA: "#0a1426", tintB: "#4a8ae0", ringCol: "#a0d0ff", diskTint: "#b0e0ff", nebScale: 1.3,
        exposure: 1.15, bloom: 0.95, ca: 1.8, grain: 10, vignette: 0.5 } },
    { id: "whisper", label: "The Whisper — Pale Void",
      params: { rs: 0.75, spin: 0.2, lens: 1.3, diskInner: 4.5, diskOuter: 7, temp: 0.4, bright: 0.5, beaming: 0.3,
        tiltX: 48, tiltZ: 22, swirl: 0.2, ring: 2.3, star: 2.1, nebula: 0.25, band: 1.5,
        tintA: "#0b0d12", tintB: "#6a7488", ringCol: "#e8ecff", diskTint: "#c8d0e0", nebScale: 2.4,
        exposure: 1.25, bloom: 0.55, ca: 1.2, grain: 18, vignette: 0.7 } },
    { id: "carnival", label: "Carnival — Spectral Riot",
      params: { rs: 1.1, spin: 0.98, lens: 1.0, diskInner: 2.3, diskOuter: 16, temp: 2.9, bright: 3.4, beaming: 1.0,
        tiltX: 6, tiltZ: 30, swirl: 2.2, ring: 1.35, star: 1.3, nebula: 1.5, band: 1.3,
        tintA: "#180a24", tintB: "#e040c0", ringCol: "#ff9ae8", diskTint: "#ff70d0", nebScale: 1.6,
        exposure: 1.12, bloom: 1.35, ca: 4, grain: 6, vignette: 0.45 } },
    { id: "emberveil", label: "Ember Veil — Smouldering Shroud",
      params: { rs: 1.3, spin: 0.7, lens: 0.95, diskInner: 3.4, diskOuter: 19, temp: 1.0, bright: 2.0, beaming: 0.75,
        tiltX: -8, tiltZ: 4, swirl: 0.65, ring: 0.8, star: 0.45, nebula: 1.9, band: 0.6,
        tintA: "#140b06", tintB: "#a04a20", ringCol: "#ffb080", diskTint: "#e0b090", nebScale: 2.1,
        exposure: 1.05, bloom: 1.0, ca: 2.5, grain: 12, vignette: 0.55 } },
    { id: "needle", label: "The Needle — Razor Horizon",
      params: { rs: 0.85, spin: 0.99, lens: 1.15, diskInner: 2.2, diskOuter: 8, temp: 2.2, bright: 3.0, beaming: 1.0,
        tiltX: 2, tiltZ: 0, swirl: 2.6, ring: 1.9, star: 0.9, nebula: 0.5, band: 0.7,
        tintA: "#0c1018", tintB: "#90b0e0", ringCol: "#ffffff", diskTint: "#e8f0ff", nebScale: 1.0,
        exposure: 1.1, bloom: 0.9, ca: 3.5, grain: 7, vignette: 0.45 } },
    { id: "garden", label: "Verdant Garden — Living Orbit",
      params: { rs: 1.0, spin: 0.5, lens: 1.0, diskInner: 3.0, diskOuter: 12, temp: 0.9, bright: 1.7, beaming: 0.6,
        tiltX: 16, tiltZ: -20, swirl: 0.8, ring: 1.1, star: 1.4, nebula: 1.6, band: 1.2,
        tintA: "#0a180e", tintB: "#4ad070", ringCol: "#c0ffd0", diskTint: "#b0e8c0", nebScale: 1.4,
        exposure: 1.12, bloom: 0.8, ca: 2, grain: 9, vignette: 0.48 } },
    { id: "sovereign", label: "Sovereign — Gold Eternal",
      params: { rs: 1.5, spin: 0.85, lens: 1.0, diskInner: 2.8, diskOuter: 15, temp: 1.35, bright: 3.6, beaming: 0.95,
        tiltX: 10, tiltZ: -2, swirl: 1.1, ring: 1.45, star: 0.8, nebula: 0.7, band: 0.85,
        tintA: "#181208", tintB: "#e0a830", ringCol: "#ffe8a0", diskTint: "#ffe0a8", nebScale: 0.9,
        exposure: 1.08, bloom: 1.1, ca: 2.8, grain: 8, vignette: 0.5 } },
    { id: "mourning", label: "Mourning Star — Violet Requiem",
      params: { rs: 1.05, spin: 0.6, lens: 1.1, diskInner: 3.6, diskOuter: 10, temp: 0.65, bright: 1.2, beaming: 0.55,
        tiltX: -35, tiltZ: 10, swirl: 0.5, ring: 1.75, star: 1.9, nebula: 1.35, band: 0.9,
        tintA: "#100a1c", tintB: "#7a50c8", ringCol: "#c0a0ff", diskTint: "#b898e8", nebScale: 1.7,
        exposure: 1.18, bloom: 1.05, ca: 2.2, grain: 13, vignette: 0.6 } }
  ];

  const FORGE_PREFIX = ["Vanta", "Helios", "Nyx", "Charybdis", "Ouro", "Kron", "Aster", "Umbra", "Pyre", "Nadir",
    "Zethe", "Moro", "Calix", "Draec", "Ony", "Vesper", "Than", "Ilios", "Ravu", "Solm"];
  const FORGE_SUFFIX = ["Maw", "Crown", "Heart", "Eye", "Throat", "Bloom", "Womb", "Forge", "Choir", "Veil",
    "Pyre", "Gate", "Cradle", "Halo", "Maw", "Depths", "Chalice", "Storm", "Spindle", "Engine"];
  const ROMAN = ["I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI"];
  const TINT_PAIRS = [
    ["#241a3f", "#c8722a"], ["#0f1c30", "#4a7ae0"], ["#0c2030", "#3fd8c8"], ["#2a0f14", "#e0452a"],
    ["#171226", "#8a6ae0"], ["#0f2418", "#3fd87a"], ["#101c2c", "#6ab4e0"], ["#200c12", "#e02a3f"],
    ["#101426", "#d9b45a"], ["#141030", "#7a5ae0"], ["#1c1428", "#e0b45a"], ["#0c0f18", "#5a6480"]
  ];

  function forgeName(seed) {
    const rng = mulberry32(seed ^ 0x9e3779b9);
    const a = FORGE_PREFIX[(rng() * FORGE_PREFIX.length) | 0];
    const b = FORGE_SUFFIX[(rng() * FORGE_SUFFIX.length) | 0];
    const n = ROMAN[(rng() * ROMAN.length) | 0];
    return `${a}${b === "Eye" && a.endsWith("e") ? "" : ""} ${b} ${n}`;
  }

  /* Deterministic unique black hole from any unsigned 32-bit seed */
  function forge(seed) {
    const rng = mulberry32(seed >>> 0);
    const pair = TINT_PAIRS[(rng() * TINT_PAIRS.length) | 0];
    const p = {
      rs: 0.7 + rng() * 0.9,
      spin: 0.15 + rng() * 0.84,
      lens: 0.85 + rng() * 0.4,
      diskInner: 2.3 + rng() * 2.1,
      diskOuter: 6.5 + rng() * 11,
      temp: 0.3 + rng() * 2.4,
      bright: 1.4 + rng() * 3.4,
      beaming: 0.35 + rng() * 0.65,
      tiltX: (rng() * 2 - 1) * 62,
      tiltZ: (rng() * 2 - 1) * 30,
      swirl: 0.25 + rng() * 1.8,
      ring: 0.6 + rng() * 1.3,
      star: 0.4 + rng() * 1.6,
      nebula: 0.2 + rng() * 1.2,
      band: 0.3 + rng() * 1.3,
      tintA: pair[0], tintB: pair[1],
      exposure: 0.95 + rng() * 0.3,
      bloom: 0.5 + rng() * 0.8,
      ca: 1 + rng() * 3,
      grain: 6 + rng() * 12,
      vignette: 0.35 + rng() * 0.35
    };
    if (p.diskOuter < p.diskInner + 2.2) p.diskOuter = p.diskInner + 2.2 + rng() * 3;
    return { id: "forge-" + seed, label: forgeName(seed) + "  ·  #" + seed, seed: seed >>> 0, params: p };
  }

  window.BHPRESETS = { CURATED, forge, forgeName };
})();
