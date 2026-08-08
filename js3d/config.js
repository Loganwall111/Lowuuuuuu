/* ============================================================
   GRAVITON 3D :: config — constants, palettes, ability specs
   ============================================================ */
const CFG3D = {
  // play plane is the XZ plane (y=0)
  shipMass: 8,
  shipAccel: 900,          // world units/s^2
  shipMaxSpeed: 120,
  boostMul: 3.2,
  drag: 0.985,
  energyMax: 100,
  energyBurn: 9,
  energyBoostBurn: 26,
  energyDrainIdle: 1.2,
  shardEnergy: 20,
  shardScore: 250,
  gateScore: 500,
  collisionRadius: 6,
  horizonExtra: 9,          // swallows ship if within this of event horizon

  // gravitational constant
  gravity: 1.35e5,          // tuned for Havok force units

  sectorNames: ["AURUM VEIL","SAPPHIRE TIDE","EMERALD MADNESS","CRIMSON BLOOM","ORCHID ABYSS","THE SINGULARITY"],
  sectorRoman: ["I","II","III","IV","V","Ω"],
  sectors: 6,

  // abilities (fully functional)
  abilities: [
    { id:'singularity', name:'SINGULARITY', key:'1', cooldown:18, dur:8,
      desc:'Summon a miniature black hole that drags everything around it into a spiraling vortex.' },
    { id:'rift', name:'TEMPORAL RIFT', key:'2', cooldown:20, dur:5,
      desc:'Fold time — the universe crawls while you move at full speed.' },
    { id:'lash', name:'VOID LASH', key:'3', cooldown:6, dur:0.4,
      desc:'Lash out with a void filament that annihilates shards and asteroids on contact.' },
  ],
};

const U3 = {
  clamp:(v,a,b)=>Math.max(a,Math.min(b,v)),
  lerp:(a,b,t)=>a+(b-a)*t,
  rand:(a,b)=>a+Math.random()*(b-a),
  randInt:(a,b)=>Math.floor(U3.rand(a,b+1)),
  shuffle:(arr)=>{const a=arr.slice();for(let i=a.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[a[i],a[j]]=[a[j],a[i]];}return a;},
  pick:(arr)=>arr[Math.floor(Math.random()*arr.length)],
};
