/* ============================================================
   GRAVITON :: config — cosmic constants, palettes, tuning
   ============================================================ */
const CONFIG = {
  // world
  gravity: 60,          // gravitational constant (pixels^3 / s^2 per mass unit)
  shipAccel: 520,
  shipMaxSpeed: 420,
  boostAccel: 2600,
  drag: 0.995,
  energyMax: 100,
  energyBurn: 11,       // per second while thrusting
  energyBoostBurn: 34,
  energyDrainIdle: 1.4, // passive drain per second
  shardEnergy: 22,
  shardScore: 250,
  lensStrength: 55,     // black-hole light bending
  timeDilationNear: 160,// px from horizon where dilation kicks in
  sectors: 6,

  // palettes (cycled by hue over time)
  palette: {
    cyan:   [79,245,255],
    magenta:[255,61,240],
    violet: [155,91,255],
    gold:   [255,213,79],
    red:    [255,61,110],
    blue:   [64,120,255],
    green:  [79,255,180],
  },
  sectorNames: ["AURUM VEIL","SAPPHIRE TIDE","EMERALD MADNESS","CRIMSON BLOOM","ORCHID ABYSS","THE SINGULARITY"],
  sectorRoman: ["I","II","III","IV","V","Ω"],
};

// cache a hue-rotation helper on Math-free util
const Util = {
  hue(h){ return `hsl(${h%360} 100% 60%)`; },
  lerp(a,b,t){ return a+(b-a)*t; },
  clamp(v,a,b){ return Math.max(a,Math.min(b,v)); },
  rand(a,b){ return a+Math.random()*(b-a); },
  randInt(a,b){ return Math.floor(Util.rand(a,b+1)); },
  dist(x1,y1,x2,y2){ const dx=x2-x1, dy=y2-y1; return Math.hypot(dx,dy); },
  shuffle(arr){ const a=arr.slice(); for(let i=a.length-1;i>0;i--){ const j=Math.floor(Math.random()*(i+1)); [a[i],a[j]]=[a[j],a[i]]; } return a; },
};
