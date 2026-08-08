/* ============================================================================
   CROWN OF THE UNMADE — 3D EDITION
   A full WebGL reality-warping god-arena built with Babylon.js + Havok.
   Assembled by a swarm of 15 sub-agents; sections tagged [AGENT].
   ========================================================================== */
"use strict";

/* Global Babylon handles (set during boot). */
let engine = null, scene = null, camera = null, canvas = null;
let pointer = { x: 0, y: 0 };

/* ------------------------------------------------------------------ [AGENT 1: ARCHITECT] Engine, camera, input, state machine */
const A = (() => {
  const S = {
    playing: false, booting: true, dead: false, paused: false,
    timeScale: 1, targetTimeScale: 1,
    shake: 0, flash: 0, gameTime: 0, frame: 0,
    elapsed: 0,
  };
  const settings = { difficulty: 2, volume: 0.7, sensitivity: 1, fx: "high" };
  return { S, settings };
})();

const GRAVITY_BASE = -9.8;
let gravityY = GRAVITY_BASE;   // flipped by Gravitic Inversion

function createEngine() {
  canvas = document.getElementById("game");
  engine = new BABYLON.Engine(canvas, true, { preserveDrawingBuffer: false, stencil: true });
  engine.adaptToDeviceRatio = true;
  scene = new BABYLON.Scene(engine);
  scene.clearColor = new BABYLON.Color4(0.008, 0.004, 0.02, 1);
  scene.ambientColor = new BABYLON.Color3(0.4, 0.35, 0.6);
  window.addEventListener("resize", () => engine.resize());
}

function createCamera() {
  camera = new BABYLON.ArcRotateCamera("cam", -0.4, 1.15, 46, new BABYLON.Vector3(0, 2, 0), scene);
  camera.attachControl(canvas, true);
  camera.lowerRadiusLimit = 10;
  camera.upperRadiusLimit = 90;
  camera.wheelDeltaPercentage = 0.02;
  camera.panningSensibility = 50;
  camera.inertia = 0.08;
}

/* ------------------------------------------------------------------ [AGENT 8: LIGHTING & RENDERER] lights, glow, materials */
const Lighting = (() => {
  let glowLayer = null;
  function build() {
    const hemi = new BABYLON.HemisphericLight("heli", new BABYLON.Vector3(0, 1, 0.2), scene);
    hemi.intensity = 0.55;
    const key = new BABYLON.DirectionalLight("key", new BABYLON.Vector3(0.4, -1, -0.3), scene);
    key.intensity = 0.7; key.position = new BABYLON.Vector3(20, 30, 10);
    const rim = new BABYLON.DirectionalLight("rim", new BABYLON.Vector3(-0.5, 0.2, 0.6), scene);
    rim.intensity = 0.5; rim.diffuse = new BABYLON.Color3(0.6, 0.3, 1);
    if (A.settings.fx === "high") {
      try {
        glowLayer = new BABYLON.GlowLayer("glow", scene, { mainTextureRatio: 0.5, blurKernelSize: 32 });
        glowLayer.intensity = 0.9;
      } catch (e) { console.warn("glow disabled", e); }
    }
  }
  function setIntensity(v) { if (glowLayer) glowLayer.intensity = v; }
  return { build, setIntensity };
})();

/* ------------------------------------------------------------------ [AGENT 2: COSMOLOGY] procedural skybox + star field */
const Cosmos = (() => {
  let skyMesh = null, starsPS = null;
  function drawStars(ctx, w, h) {
    ctx.fillStyle = "#05020f"; ctx.fillRect(0, 0, w, h);
    const neb = ctx.createRadialGradient(w*0.7, h*0.35, 20, w*0.7, h*0.35, w*0.9);
    neb.addColorStop(0, "rgba(90,40,160,.8)"); neb.addColorStop(0.5, "rgba(30,10,70,.5)"); neb.addColorStop(1, "rgba(0,0,0,0)");
    ctx.fillStyle = neb; ctx.fillRect(0,0,w,h);
    const neb2 = ctx.createRadialGradient(w*0.2, h*0.7, 20, w*0.2, h*0.7, w*0.7);
    neb2.addColorStop(0, "rgba(20,80,120,.6)"); neb2.addColorStop(1, "rgba(0,0,0,0)");
    ctx.fillStyle = neb2; ctx.fillRect(0,0,w,h);
    for (let i = 0; i < 500; i++) {
      const x = Math.random()*w, y = Math.random()*h, r = Math.random()*1.6+0.3;
      ctx.fillStyle = `rgba(220,230,255,${0.3+Math.random()*0.7})`;
      ctx.beginPath(); ctx.arc(x, y, r, 0, 6.283); ctx.fill();
    }
  }
  function build() {
    // skybox from a canvas starfield
    const size = 420;
    const dt = new BABYLON.DynamicTexture("sky", { width: 1024, height: 1024 }, scene, true);
    const ctx = dt.getContext();
    drawStars(ctx, 1024, 1024);
    dt.update();
    skyMesh = BABYLON.MeshBuilder.CreateBox("sky", { size: size }, scene);
    skyMesh.material = new BABYLON.StandardMaterial("skyMat", scene);
    skyMesh.material.diffuseTexture = dt;
    skyMesh.material.disableLighting = true;
    skyMesh.material.backFaceCulling = false;
    skyMesh.infiniteDistance = true;
    skyMesh.isPickable = false;

    // star particle field drifting around
    starsPS = new BABYLON.ParticleSystem("stars", 400, scene);
    starsPS.particleTexture = makeDotTexture();
    starsPS.emitter = new BABYLON.Vector3(0, 0, 0);
    starsPS.minEmitBox = new BABYLON.Vector3(-90, -60, -90);
    starsPS.maxEmitBox = new BABYLON.Vector3(90, 60, 90);
    starsPS.minSize = 0.4; starsPS.maxSize = 1.6;
    starsPS.minLifeTime = 12; starsPS.maxLifeTime = 24;
    starsPS.emitRate = 60;
    starsPS.blendMode = BABYLON.ParticleSystem.BLENDMODE_ADD;
    starsPS.gravity = new BABYLON.Vector3(0, -0.4, 0);
    starsPS.direction1 = new BABYLON.Vector3(-0.5,0,-0.5); starsPS.direction2 = new BABYLON.Vector3(0.5,0,0.5);
    starsPS.color1 = new BABYLON.Color4(0.9,0.9,1,1); starsPS.color2 = new BABYLON.Color4(0.6,0.4,1,1);
    starsPS.start();
  }
  let dotTex = null;
  function makeDotTexture() {
    if (dotTex) return dotTex;
    const c = document.createElement("canvas"); c.width = c.height = 32;
    const g = c.getContext("2d");
    const grad = g.createRadialGradient(16,16,0,16,16,16);
    grad.addColorStop(0,"rgba(255,255,255,1)"); grad.addColorStop(1,"rgba(255,255,255,0)");
    g.fillStyle = grad; g.fillRect(0,0,32,32);
    dotTex = new BABYLON.Texture(c, scene, true);
    return dotTex;
  }
  return { build, makeDotTexture };
})();

/* ------------------------------------------------------------------ [AGENT 5: PARTICLEFORGE] volumetric bursts & trails */
const Particles = (() => {
  const pool = [];
  function burst(origin, count, color, power, size) {
    const ps = new BABYLON.ParticleSystem("burst"+pool.length, count, scene);
    ps.particleTexture = Cosmos.makeDotTexture();
    ps.emitter = origin.clone();
    ps.minEmitPower = 2; ps.maxEmitPower = 3 + power;
    ps.direction1 = new BABYLON.Vector3(-1,-1,-1); ps.direction2 = new BABYLON.Vector3(1,1,1);
    ps.minLifeTime = 0.4; ps.maxLifeTime = 0.9;
    ps.minSize = 1; ps.maxSize = size || 5;
    ps.emitRate = count * 4;
    ps.blendMode = BABYLON.ParticleSystem.BLENDMODE_ADD;
    ps.color1 = color || new BABYLON.Color4(1,1,1,1);
    ps.color2 = new BABYLON.Color4(1,0.7,0.3,1);
    ps.gravity = new BABYLON.Vector3(0,-3,0);
    ps.disposeOnStop = true;
    ps.start(0.25);
    // hold refs to avoid GC while alive
    if (pool.length > 40) { const dead = pool.shift(); try { dead.dispose(); } catch(e){} }
    pool.push(ps);
    return ps;
  }
  function shockwave(origin, color) {
    // expanding ring mesh
    const ring = BABYLON.MeshBuilder.CreateTorus("sw", { diameter: 1, thickness: 0.4, tessellation: 40 }, scene);
    ring.position = origin.clone();
    ring.rotation.x = Math.PI / 2;
    const mat = new BABYLON.StandardMaterial("swm", scene);
    mat.emissiveColor = color || new BABYLON.Color3(0.6,1,1); mat.disableLighting = true;
    mat.alpha = 0.9; mat.backFaceCulling = false;
    ring.material = mat;
    ring.isPickable = false;
    const grow = (() => {
      let t = 0;
      return (dt) => {
        t += dt; ring.scaling.x = ring.scaling.z = 1 + t * 30;
        mat.alpha = Math.max(0, 0.9 - t * 1.4);
        if (t > 0.8) { ring.dispose(); mat.dispose(); return false; }
        return true;
      };
    })();
    return grow;
  }
  function trail(emitter, color) {
    const ps = new BABYLON.ParticleSystem("trail"+pool.length, 30, scene);
    ps.particleTexture = Cosmos.makeDotTexture();
    ps.emitter = emitter;
    ps.minLifeTime = 0.4; ps.maxLifeTime = 0.8;
    ps.minSize = 1.5; ps.maxSize = 3.5;
    ps.emitRate = 80;
    ps.blendMode = BABYLON.ParticleSystem.BLENDMODE_ADD;
    ps.color1 = color || new BABYLON.Color4(0.5,1,1,1);
    ps.color2 = new BABYLON.Color4(0.5,1,1,0);
    ps.gravity = new BABYLON.Vector3(0,-1,0);
    ps.disposeOnStop = true;
    ps.start();
    pool.push(ps);
    return ps;
  }
  return { burst, shockwave, trail };
})();

/* ------------------------------------------------------------------ [AGENT 3: ENTROPY] enemy 3D assets, spawn, behaviour */
const Entropy = (() => {
  const enemies = [];
  let wave = 0, spawnAcc = 0, budget = 0;
  const SPECIES = {
    drifter: { r: 1.6, hp: 40, speed: 7, color: [1,0.42,0.55], score: 10, dmg: 14, shape: "drifter" },
    shard:   { r: 1.0, hp: 16, speed: 16, color: [0.49,1,1], score: 6, dmg: 9, shape: "shard", wob: true },
    behemoth:{ r: 4.2, hp: 300, speed: 3.6, color: [0.75,0.52,1], score: 55, dmg: 30, shape: "behemoth", splits: 3 },
    wraith:  { r: 1.5, hp: 70, speed: 12, color: [0.62,1,0.82], score: 30, dmg: 18, shape: "wraith", phase: true },
    mine:    { r: 1.3, hp: 20, speed: 0, color: [1,0.82,0.4], score: 5, dmg: 22, shape: "mine", boom: true },
    sunmaw:  { r: 6.0, hp: 900, speed: 2.6, color: [1,0.62,0.36], score: 150, dmg: 26, shape: "sunmaw" },
  };

  function makeMesh(type, r, color, scene3) {
    const c = new BABYLON.Color3(color[0], color[1], color[2]);
    let mesh;
    if (type === "shard") mesh = BABYLON.MeshBuilder.CreatePolyhedron("shard", { type: 1, size: r }, scene3);
    else if (type === "behemoth") {
      mesh = BABYLON.MeshBuilder.CreateIcoSphere("beh", { radius: r, subdivisions: 3 }, scene3);
      for (let i = 0; i < 10; i++) {
        const spike = BABYLON.MeshBuilder.CreateCone("spk", { height: r*0.9, diameter: r*0.16, tessellation: 5 }, scene3);
        const dir = new BABYLON.Vector3(Math.random()-0.5, Math.random()-0.5, Math.random()-0.5).normalize();
        spike.position = dir.scale(r*0.8);
        spike.setParent(mesh);
      }
    }
    else if (type === "wraith") mesh = BABYLON.MeshBuilder.CreateSphere("wra", { diameter: r*2, segments: 12 }, scene3);
    else if (type === "mine") {
      mesh = BABYLON.MeshBuilder.CreateIcoSphere("mine", { radius: r, subdivisions: 2 }, scene3);
      const blink = BABYLON.MeshBuilder.CreateSphere("blk", { diameter: r*0.5 }, scene3);
      blink.position.y = r*0.6; blink.setParent(mesh);
    }
    else if (type === "sunmaw") {
      mesh = BABYLON.MeshBuilder.CreateSphere("sun", { diameter: r*2, segments: 24 }, scene3);
      const mouth = BABYLON.MeshBuilder.CreateTorus("maw", { diameter: r*1.1, thickness: r*0.35, tessellation: 24 }, scene3);
      mouth.rotation.x = Math.PI/2; mouth.position.z = r*0.8; mouth.setParent(mesh);
    }
    else mesh = BABYLON.MeshBuilder.CreateSphere("drif", { diameter: r*2, segments: 10 }, scene3);

    const mat = new BABYLON.StandardMaterial("em"+Math.random(), scene3);
    mat.diffuseColor = c; mat.specularColor = new BABYLON.Color3(0.4,0.4,0.4);
    mat.emissiveColor = c.scale(0.6);
    mat.specularPower = 24;
    mesh.material = mat;
    mesh.isPickable = false;
    return mesh;
  }

  function add(x, y, type) {
    const sp = SPECIES[type];
    const mesh = makeMesh(sp.shape, sp.r, sp.color, scene);
    mesh.position.set(x, sp.r * 0.7, y);
    enemies.push({
      type, mesh, r: sp.r, hp: sp.hp, maxHp: sp.hp, speed: sp.speed,
      color: sp.color, score: sp.score, dmg: sp.dmg,
      vx: 0, vz: 0, dead: false, hit: 0, seed: Math.random()*6.28,
      wob: sp.wob, phase: sp.phase, boom: sp.boom, splits: sp.splits, phaseT: 0,
      life: sp.boom ? 9 : 0,
    });
  }

  function newWave() {
    wave++; spawnAcc = 0;
    budget = (7 + wave*5 + wave*wave*0.6) * A.settings.difficulty;
    UI.center("WAVE " + wave);
    if (wave > 1) Synth.bell();
  }

  function spawnOne() {
    const ang = Math.random()*Math.PI*2, dist = 46 + Math.random()*20;
    const px = Player.x + Math.cos(ang)*dist, pz = Player.z + Math.sin(ang)*dist;
    let type = "drifter"; const roll = Math.random();
    if (wave>=2 && roll<0.3) type="shard";
    if (wave>=3 && roll>0.8 && roll<0.86) type="behemoth";
    if (wave>=4 && roll>0.6 && roll<0.66) type="wraith";
    if (wave>=2 && roll>0.4 && roll<0.44) type="mine";
    if (wave>=6 && roll>0.94) type="sunmaw";
    const counts = {}; for (const e of enemies) counts[e.type]=(counts[e.type]||0)+1;
    if (type==="behemoth" && counts.behemoth>=3) type="drifter";
    if (type==="sunmaw" && counts.sunmaw>=1) type="drifter";
    if (type==="wraith" && counts.wraith>=8) type="drifter";
    add(px, pz, type);
  }

  function update(dt) {
    if (!A.S.playing || A.S.paused) return;
    if (A.S.gameTime > 2) { newWave(); A.S.gameTime = 0; }
    spawnAcc += dt;
    const interval = Math.max(0.18, 0.55 - wave*0.012);
    if (spawnAcc >= interval && budget > 0) { spawnAcc = 0; budget--; spawnOne(); }
  }

  function behaviors(dt) {
    if (!A.S.playing || A.S.paused) return;
    const pl = Player;
    for (const e of enemies) {
      if (e.dead) continue;
      if (e.life > 0) { e.life -= dt; if (e.life<=0) { kill(e); continue; } }
      const dx = pl.x - e.mesh.position.x, dz = pl.z - e.mesh.position.z;
      const d = Math.hypot(dx, dz) || 1;
      if (e.phase) { e.phaseT += dt; if (e.phaseT > 1.6) { e.phaseT = 0; const a = Math.random()*6.28; e.mesh.position.x = pl.x+Math.cos(a)*9; e.mesh.position.z = pl.z+Math.sin(a)*9; } }
      let sp = e.speed;
      if (e.wob) sp *= (1 + Math.sin(A.S.elapsed*6 + e.seed)*1.3);
      if (e.type==="mine") { if (G.dist2D(e.mesh.position, pl) < 9 && pl.alive) explodeMine(e); continue; }
      e.vx += (dx/d)*sp*2*dt; e.vz += (dz/d)*sp*2*dt;
      e.vx *= (1-3*dt); e.vz *= (1-3*dt);
      e.mesh.position.x += e.vx*dt; e.mesh.position.z += e.vz*dt;
      G.gravityPull(e, dt);
      e.mesh.rotation.y += dt*2; e.mesh.rotation.x = Math.sin(A.S.elapsed+e.seed)*0.3;
      // keep on ground plane
      e.mesh.position.y = e.r*0.7 + Math.abs(Math.sin(A.S.elapsed*3+e.seed))*0.4;
      e.hit = Math.max(0, e.hit-dt);
      if (e.hit > 0) e.mesh.material.emissiveColor = new BABYLON.Color3(1,1,1);
      else e.mesh.material.emissiveColor = new BABYLON.Color3(e.color[0],e.color[1],e.color[2]).scale(0.6);
      // bounds
      const R = 58;
      if (Math.abs(e.mesh.position.x) > R || Math.abs(e.mesh.position.z) > R) {
        e.mesh.position.x = Math.max(-R, Math.min(R, e.mesh.position.x));
        e.mesh.position.z = Math.max(-R, Math.min(R, e.mesh.position.z));
      }
    }
    // contact damage
    if (pl.alive) for (const e of enemies) {
      if (e.dead || e.boom) continue;
      if (G.dist2D(e.mesh.position, pl) < e.r + pl.r) {
        const hit = Player.damage(e.dmg, e);
        if (hit) kill(e);
        const dx = pl.x - e.mesh.position.x, dz = pl.z - e.mesh.position.z, d = Math.hypot(dx,dz)||1;
        pl.vx += dx/d*20; pl.vz += dz/d*20;
        if (!pl.alive) return;
      }
    }
    sweep();
  }

  function explodeMine(e) {
    e.hp = 0; Synth.boom(60);
    Particles.burst(e.mesh.position, 30, new BABYLON.Color4(1,0.82,0.4,1), 6, 3);
    A.S.shake = Math.max(A.S.shake, 6);
    if (Player.alive && G.dist2D(e.mesh.position, Player) < 7) Player.damage(e.dmg, e);
    kill(e);
  }

  function kill(e, byPlayer, grant) {
    if (!e || e.dead) return;
    e.dead = true;
    Particles.burst(e.mesh.position, 26, new BABYLON.Color4(e.color[0],e.color[1],e.color[2],1), 6, e.r);
    Synth.hit(e.type==="behemoth"||e.type==="sunmaw");
    if (byPlayer) {
      const pts = Math.round(e.score * Player.comboMult());
      Player.addScore(pts);
      UI.flytext(e.mesh.position, "+"+pts);
      if (e.splits) for (let k=0;k<e.splits;k++) add(e.mesh.position.x+(Math.random()*3-1.5), e.mesh.position.z+(Math.random()*3-1.5), "drifter");
      if (grant) Player.heal(3);
    }
  }

  function sweep() { for (let i=enemies.length-1;i>=0;i--) if (enemies[i].dead) { enemies[i].mesh.dispose(); enemies.splice(i,1); } }
  function clear() { for (const e of enemies) e.mesh.dispose(); enemies.length = 0; }
  function count() { return enemies.length; }
  function list() { return enemies; }
  return { list, add, update, behaviors, kill, clear, count, newWave, SPECIES,
    get wave(){ return wave; }, set wave(v){ wave=v; } };
})();

/* ------------------------------------------------------------------ [AGENT 4: REALITY WEAVER] the abilities */
const Abilities = (() => {
  const defs = [
    { key:"q", name:"SINGULARITY", ico:"◉", cd:15 },
    { key:"e", name:"TEMPORAL RIFT", ico:"⟲", cd:11 },
    { key:"r", name:"FRACTAL ECHO", ico:"❖", cd:12 },
    { key:"g", name:"TRACTOR GRAB", ico:"⌖", cd:6 },
    { key:" ", name:"GRAVINVERS", ico:"⧉", cd:9 },
    { key:"f", name:"VOID LASH", ico:"⌁", cd:7 },
    { key:"c", name:"PRISM PHASE", ico:"◈", cd:6 },
    { key:"x", name:"GENESIS", ico:"☀", cd:32, ult:true },
  ];
  const state = defs.map(d => ({ cd: 0 }));
  const fx = { echoes: [], prismTrail: [], riftActive: false, inversion: 0, grabTarget: null, prismPS: null };
  const S = A.S;

  function canUse(i) { return state[i].cd <= 0 && Player.alive && S.playing; }
  function tryCast(i) {
    if (!canUse(i)) return false;
    state[i].cd = defs[i].cd;
    UI.announce(defs[i].name);
    const fns = [singularity, temporalRift, fractalEcho, tractorGrab, gravInverse, voidLash, prismPhase, genesis];
    fns[i]();
    return true;
  }

  function singularity() {
    const p = Player.aimPoint();
    G.birthBlackHole(p, 16, 6.5);
    Synth.singularity();
    Particles.shockwave(p, new BABYLON.Color3(0.3,1,1));
  }
  function temporalRift() {
    fx.riftActive = true; S.targetTimeScale = 0.3;
    Particles.burst(Player.position(), 26, new BABYLON.Color4(0.5,1,0.8,1), 4, 3);
    Synth.bell();
  }
  function fractalEcho() {
    const offs = [new BABYLON.Vector3(4,0,0), new BABYLON.Vector3(-4,0,0), new BABYLON.Vector3(0,0,4)];
    fx.echoes = offs.map(o => {
      const m = BABYLON.MeshBuilder.CreateSphere("ec", { diameter: 2 }, scene);
      m.position = Player.position().add(o); m.position.y = 1;
      const mat = new BABYLON.StandardMaterial("ecm", scene);
      mat.emissiveColor = new BABYLON.Color3(0.78,0.65,1); mat.disableLighting = true; mat.alpha = 0.5;
      m.material = mat; m.isPickable = false;
      return { p: m.position, mesh: m, mat, t: 5 };
    });
    Synth.echo();
  }
  function tractorGrab() {
    let best = null, bd = 1e9;
    for (const e of Entropy.list()) { if (e.dead) continue; const d = G.dist2D(e.mesh.position, Player); if (d < 26 && d < bd) { bd = d; best = e; } }
    if (best) { fx.grabTarget = best; fx.grabT = 0; Synth.bell(); UI.grab(true); }
    else { UI.announce("NO FOE TO SEIZE"); }
  }
  function gravInverse() {
    fx.inversion = 2.0;
    gravityY = 9.8;
    Player.vy = 18;
    for (const e of Entropy.list()) { e.mesh.position.y += 6; e.vy = 8; }
    A.S.shake = Math.max(A.S.shake, 10);
    Particles.burst(Player.position(), 30, new BABYLON.Color4(0.75,0.52,1,1), 8, 4);
    Synth.bell();
  }
  function voidLash() {
    const aim = Player.aimPoint();
    const from = Player.position(); from.y += 1.2;
    const dir = aim.subtract(from).normalize();
    const reach = 40;
    const end = from.add(dir.scale(reach));
    G.beams.push({ from, end, t: 0.3, color: new BABYLON.Color3(0.75,0.52,1), w: 1.2 });
    for (const e of Entropy.list()) {
      if (e.dead) continue;
      const q = e.mesh.position; q.y += 0.7;
      const t = BABYLON.Vector3.Clamp(BABYLON.Vector3.Dot(q.subtract(from), dir), 0, reach);
      const proj = from.add(dir.scale(t));
      if (proj.subtract(q).length() < e.r + 2) { Entropy.kill(e, true, true); }
    }
    Particles.burst(end, 24, new BABYLON.Color4(0.75,0.52,1,1), 6, 3);
    Synth.lash();
  }
  function prismPhase() {
    fx.prismTrail = [];
    Player.phase = 2.5;
    if (!fx.prismPS) fx.prismPS = Particles.trail(Player.position(), new BABYLON.Color4(1,0.82,1,1));
    Particles.burst(Player.position(), 24, new BABYLON.Color4(1,0.82,1,1), 5, 3);
    Synth.phase();
  }
  function genesis() {
    S.flash = 1; S.shake = Math.max(S.shake, 22);
    Player.overdrive = 4.0; S.targetTimeScale = 0.15;
    Particles.burst(Player.position(), 60, new BABYLON.Color4(1,1,1,1), 12, 5);
    Particles.shockwave(Player.position(), new BABYLON.Color3(1,1,1));
    Synth.genesis();
  }

  function update(dt) {
    for (let i=0;i<state.length;i++) if (state[i].cd>0) state[i].cd = Math.max(0, state[i].cd-dt);
    if (Player.overdrive<=0 && !fx.riftActive) S.targetTimeScale = 1;
    if (fx.riftActive && Player.overdrive<=0) { fx.riftActive = false; S.targetTimeScale = 1; }
    // grav inversion
    if (fx.inversion > 0) {
      fx.inversion -= dt;
      if (fx.inversion <= 0) gravityY = GRAVITY_BASE;
    }
    // prism trail damage
    if (Player.phase>0 && Player.alive) fx.prismTrail.push(Player.position().clone());
    for (let i=fx.prismTrail.length-1;i>=0;i--) {
      for (const e of Entropy.list()) {
        if (e.dead) continue;
        if (G.dist2D(e.mesh.position, fx.prismTrail[i]) < e.r + 2.5) { e.hp -= 30*dt; e.hit = 0.1; if (e.hp<=0) Entropy.kill(e, true, true); }
      }
      if (fx.prismTrail.length > 40) fx.prismTrail.shift();
    }
    // echoes (persistent meshes, fade + dispose)
    for (let i=fx.echoes.length-1;i>=0;i--) {
      const ec = fx.echoes[i];
      ec.t -= dt;
      if (ec.mat) ec.mat.alpha = Math.max(0, ec.t/5)*0.5;
      if (ec.t<=0) { try { ec.mesh.dispose(); ec.mat.dispose(); } catch(e){} fx.echoes.splice(i,1); }
      else if (Player.firing) Bolts.fire(ec.p, Player.aimPoint(), new BABYLON.Color3(0.78,0.65,1), 16);
    }
    // tractor grab update
    if (fx.grabTarget) {
      const tg = fx.grabTarget;
      if (tg.dead) { fx.grabTarget = null; UI.grab(false); return; }
      // pull toward a focal point ahead of player
      const aim = Player.aimPoint(); aim.y = tg.mesh.position.y;
      const toAim = aim.subtract(tg.mesh.position);
      tg.mesh.position.addInPlace(toAim.scale(3*dt));
      UI.grab(true);
      if (tg.mesh.position.subtract(aim).length() < 1.2) { hurl(tg, aim); }
    }
  }
  function hurl(tg, from) {
    const aim = Player.aimPoint();
    const dir = aim.subtract(from).normalize().scale(30);
    tg.vx += dir.x; tg.vz += dir.z; tg.hurt = 2;
    tg.vx *= 0.2; tg.vz *= 0.2; // mostly clear existing velocity
    fx.grabTarget = null; UI.grab(false);
    Synth.lash();
  }
  function drawEchoes() {} // echoes are persistent meshes managed in update()
  return { defs, state, fx, tryCast, update, drawEchoes, useableCds: (i)=>state[i].cd };
})();

/* ------------------------------------------------------------------ [AGENT 6: GRAVITON] black holes, lensing, gravity, beams */
const G = (() => {
  const worldR = 58;
  const blackHoles = [];
  const beams = [];
  let lensPP = null, lensTarget = null, lensStrength = 0;
  const dist = (a,b) => BABYLON.Vector3.Distance(a,b);
  const dist2D = (a,b) => Math.hypot(a.x-b.x, a.z-b.z);

  function registerLensing() {
    if (A.settings.fx !== "high") return;
    try {
      BABYLON.Effect.ShadersStore["lensVertexShader"] =
        "precision highp float;\nattribute vec2 position;\nvarying vec2 vUV;\nvoid main(void){\ngl_Position=vec4(position,0.,1.);\nvUV=position*0.5+0.5;\n}";
      BABYLON.Effect.ShadersStore["lensFragmentShader"] =
        "precision highp float;\nvarying vec2 vUV;\nuniform sampler2D textureSampler;\nuniform vec2 bhPos;\nuniform float strength;\nuniform float aspect;\nvoid main(void){\nvec2 uv=vUV;\nvec2 d=uv-bhPos;\nd.x*=aspect;\nfloat dd=length(d);\nfloat w=strength/(0.25+dd*dd*5.0);\nvec2 n=normalize(d+vec2(0.0001));\nvec2 wuv=uv+n*w*(1.0-smoothstep(0.0,0.75,dd));\ngl_FragColor=texture2D(textureSampler,wuv);\n}";
      lensPP = new BABYLON.PostProcess("lens", "lens", ["bhPos","strength","aspect"], null, 1.0, camera);
      lensPP.onApply = (effect) => {
        const v = new BABYLON.Vector2(0.5,0.5);
        if (lensTarget) {
          const proj = BABYLON.Vector3.Project(lensTarget, BABYLON.Matrix.Identity(), scene.getTransformMatrix(), camera.viewport.toGlobal(engine.getRenderWidth(), engine.getRenderHeight()));
          v.x = proj.x / engine.getRenderWidth(); v.y = proj.y / engine.getRenderHeight();
        }
        effect.setVector2("bhPos", v);
        effect.setFloat("strength", lensStrength);
        effect.setFloat("aspect", engine.getAspectRatio(camera)||1);
      };
    } catch (e) { console.warn("lensing unavailable", e); }
  }

  function birthBlackHole(pos, r, life) {
    const bh = {
      pos: pos.clone(), r, life, t: 0, maxR: r, pull: 26,
      sphere: null, disk: null, ps: null,
    };
    // dark event horizon
    bh.sphere = BABYLON.MeshBuilder.CreateSphere("bh", { diameter: r*2, segments: 20 }, scene);
    bh.sphere.position = pos.clone();
    const sm = new BABYLON.StandardMaterial("bhm", scene);
    sm.emissiveColor = new BABYLON.Color3(0,0,0); sm.diffuseColor = new BABYLON.Color3(0,0,0);
    sm.specularColor = new BABYLON.Color3(0.1,0.1,0.1); sm.disableLighting = true;
    bh.sphere.material = sm; bh.sphere.isPickable = false;
    // glowing accretion disk
    bh.disk = BABYLON.MeshBuilder.CreateTorus("disk", { diameter: r*4, thickness: r*0.6, tessellation: 40 }, scene);
    bh.disk.position = pos.clone(); bh.disk.rotation.x = Math.PI/2.3;
    const dm = new BABYLON.StandardMaterial("diskm", scene);
    dm.emissiveColor = new BABYLON.Color3(0.3,1,1); dm.disableLighting = true;
    bh.disk.material = dm; bh.disk.isPickable = false;
    // accretion particles spiraling in
    bh.ps = new BABYLON.ParticleSystem("bhps", 320, scene);
    bh.ps.particleTexture = Cosmos.makeDotTexture();
    bh.ps.emitter = pos.clone();
    bh.ps.minEmitBox = new BABYLON.Vector3(-1,-1,-1); bh.ps.maxEmitBox = new BABYLON.Vector3(1,1,1);
    bh.ps.minSize = 0.4; bh.ps.maxSize = 2.2;
    bh.ps.minLifeTime = 1.2; bh.ps.maxLifeTime = 2.2;
    bh.ps.emitRate = 220;
    bh.ps.blendMode = BABYLON.ParticleSystem.BLENDMODE_ADD;
    bh.ps.color1 = new BABYLON.Color4(0.3,1,1,1); bh.ps.color2 = new BABYLON.Color4(0.6,0.4,1,1);
    bh.ps.gravity = new BABYLON.Vector3(0,0,0);
    bh.ps.direction1 = new BABYLON.Vector3(-1,-1,-1); bh.ps.direction2 = new BABYLON.Vector3(1,1,1);
    bh.ps.start();
    // aim lensing at it
    lensTarget = pos.clone(); lensStrength = 0.6;
    blackHoles.push(bh);
    Synth.singularity();
  }

  function update(dt) {
    lensStrength = 0;
    for (let i=blackHoles.length-1;i>=0;i--) {
      const bh = blackHoles[i];
      bh.t += dt; bh.pos.y += gravityY*dt*0.4;
      bh.r = Math.min(bh.maxR*1.6, bh.r + dt*bh.maxR*0.4);
      lensTarget = bh.pos; lensStrength = 0.5;
      // pull enemies
      for (const e of Entropy.list()) {
        if (e.dead) continue;
        const dx = bh.pos.x - e.mesh.position.x, dz = bh.pos.z - e.mesh.position.z;
        const d = Math.hypot(dx,dz)||1;
        if (d < bh.r*4) { const f = bh.pull*(1-d/(bh.r*4))*dt; e.vx += dx/d*f*4; e.vz += dz/d*f*4; }
        if (d < bh.r) { e.hp -= 90*dt; e.hit=0.1; if (e.hp<=0) Entropy.kill(e,true,true); }
      }
      // pull player
      if (Player.alive) {
        const dx = bh.pos.x - Player.x, dz = bh.pos.z - Player.z, d = Math.hypot(dx,dz)||1;
        if (d < bh.r*4) { const f = 12*(1-d/(bh.r*4))*dt; Player.vx += dx/d*f*4; Player.vz += dz/d*f*4; }
      }
      bh.disk.rotation.z += dt*4; bh.disk.scaling.x = bh.disk.scaling.y = 1 + Math.sin(A.S.elapsed*8)*0.06;
      bh.sphere.scaling.x = bh.sphere.scaling.y = bh.sphere.scaling.z = 1 + bh.r*0.01;
      if (bh.t >= bh.life) {
        // SUPERNOVA
        blackHoles.splice(i,1);
        Particles.burst(bh.pos, 90, new BABYLON.Color4(0.3,1,1,1), 14, 6);
        Particles.shockwave(bh.pos, new BABYLON.Color3(0.5,1,1));
        A.S.shake = Math.max(A.S.shake, 18);
        Synth.boom(160);
        for (const e of Entropy.list()) if (dist2D(e.mesh.position, bh.pos) < 24) Entropy.kill(e,true,true);
        if (Player.alive && dist2D(Player, bh.pos) < 10) Player.damage(30);
        bh.sphere.dispose(); bh.disk.dispose(); bh.ps.dispose();
        lensStrength = 0;
      }
    }
    for (let i=beams.length-1;i>=0;i--) {
      beams[i].t -= dt;
      if (beams[i].t<=0) { try { beams[i].mesh && beams[i].mesh.dispose(); beams[i].mat && beams[i].mat.dispose(); } catch(e){} beams.splice(i,1); }
    }
  }

  function gravityPull(e, dt) {
    // fall under current gravity
    e.mesh.position.y += gravityY * dt * dt * 6;
    if (e.mesh.position.y < e.r*0.7) e.mesh.position.y = e.r*0.7;
    if (gravityY > 0) { // inverted: pushed up, then damage on flip end
      e.mesh.position.y += 2*dt;
      if (Math.abs(e.vy)>6) { e.hp -= 60; if (e.hp<=0) Entropy.kill(e,true,true); }
      e.vy = Math.max(0, (e.vy||0) - 4*dt);
    }
  }

  function drawBeams() {
    for (const b of beams) {
      if (!b.mesh) {
        const cyl = BABYLON.MeshBuilder.CreateCylinder("beam", { height: b.from.subtract(b.end).length(), diameter: b.w, tessellation: 12 }, scene);
        const mid = b.from.add(b.end).scale(0.5);
        cyl.position = mid;
        cyl.lookAt(b.end);
        cyl.rotation.x += Math.PI/2;
        const mat = new BABYLON.StandardMaterial("beamm", scene);
        mat.emissiveColor = b.color; mat.disableLighting = true; mat.alpha = 1;
        cyl.material = mat; cyl.isPickable = false;
        b.mesh = cyl; b.mat = mat;
      }
      const t = Math.max(0, b.t/0.3);
      if (b.mat) { b.mat.alpha = t; b.mat.emissiveColor = b.color.scale(0.5+t*0.5); }
    }
  }

  return { worldR, blackHoles, beams, dist, dist2D, gravityPull, update, drawBeams, registerLensing, birthBlackHole };
})();

/* ------------------------------------------------------------------ [AGENT 9: FRACTALIST] recursion & echo cascade */
const Fractalist = (() => {
  let acc = 0;
  function update(dt) {
    if (Player.overdrive > 0) {
      acc -= dt;
      if (acc <= 0) {
        acc = 0.07;
        for (let k=0;k<6;k++) {
          const a = Math.random()*Math.PI*2;
          const from = Player.position().add(new BABYLON.Vector3(Math.cos(a)*6, 0, Math.sin(a)*6));
          const to = Player.position().add(new BABYLON.Vector3(Math.cos(a)*40, 0, Math.sin(a)*40));
          Bolts.fire(from, to, new BABYLON.Color3(1,0.82,1), 45);
        }
      }
    }
    // echo firing handled in drawEchoes when Player.firing
  }
  return { update };
})();

/* ------------------------------------------------------------------ [GRAVITON/PARTICLEFORGE] Bolts — damaging projectiles */
const Bolts = (() => {
  const bolts = [];
  const BOLT_DMG = 26;
  function fire(from, to, color, dmg) {
    bolts.push({ pos: from.clone(), dir: to.subtract(from).normalize(), speed: 46, dmg: dmg||BOLT_DMG, color, life: 1.4, mesh: null });
  }
  function update(dt) {
    for (const b of bolts) {
      if (!b.mesh) {
        b.mesh = BABYLON.MeshBuilder.CreateSphere("bolt", { diameter: 0.5 }, scene);
        const m = new BABYLON.StandardMaterial("boltm", scene);
        m.emissiveColor = b.color; m.disableLighting = true;
        b.mesh.material = m; b.mesh.isPickable = false;
        b.trailPS = Particles.trail(b.mesh.position, new BABYLON.Color4(b.color.r,b.color.g,b.color.b,1));
      }
      b.pos.addInPlace(b.dir.scale(b.speed*dt));
      b.mesh.position.copyFrom(b.pos);
      b.life -= dt;
      if (b.life<=0 || Math.abs(b.pos.x)>G.worldR || Math.abs(b.pos.z)>G.worldR) { b.dead = true; continue; }
      for (const e of Entropy.list()) {
        if (e.dead) continue;
        if (G.dist2D(b.pos, e.mesh.position) < e.r + 0.6) {
          e.hp -= b.dmg; e.hit = 0.1;
          Particles.burst(b.pos, 8, new BABYLON.Color4(b.color.r,b.color.g,b.color.b,1), 4, 1.5);
          if (e.hp<=0) Entropy.kill(e,true,true);
          b.dead = true; break;
        }
      }
    }
    for (let i=bolts.length-1;i>=0;i--) if (bolts[i].dead) { try { bolts[i].mesh.dispose(); bolts[i].trailPS.dispose(); } catch(e){} bolts.splice(i,1); }
  }
  function draw() {}
  return { fire, update, draw };
})();

/* ------------------------------------------------------------------ [GRAVITON + Player] the Unmade */
const Player = {
  x:0, z:0, vx:0, vz:0, vy:0, r:1.4,
  maxHp:120, hp:120, reality:100, maxReality:100,
  alive:true, phase:0, overdrive:0, firing:false, fireAcc:0, fireRate:0.13,
  score:0, combo:0, comboT:0, kills:0, mesh:null, auraPS:null, handMat:null,
  position() { return new BABYLON.Vector3(this.x, 1.4, this.z); },
  aimPoint() {
    const r = scene.createPickingRay(pointer.x, pointer.y, camera);
    const o = r.origin, d = r.direction;
    const t = (0.9 - o.y) / (d.y||0.0001);
    if (!isFinite(t) || t < 0) return o.add(d.scale(20));
    return o.add(d.scale(Math.max(0,t)));
  },
  comboMult() { return 1 + Math.min(9, Math.floor(this.combo/8))*0.5; },
  addScore(p){ this.score += p; this.combo++; this.comboT=2.5; this.kills++; },
  heal(n){ this.hp = Math.min(this.maxHp, this.hp+n); },
  damage(dmg, src){
    if (!this.alive || this.phase>0 || A.S.targetTimeScale<0.3) return false;
    this.hp -= dmg; A.S.shake = Math.max(A.S.shake, 8);
    Synth.ouch(); Particles.burst(this.position(), 14, new BABYLON.Color4(1,0.3,0.42,1), 4, 2);
    if (this.hp<=0){ this.alive=false; A.S.playing=false; UI.die(); }
    return true;
  },
  build() {
    // a glowing crown ring
    this.mesh = BABYLON.MeshBuilder.CreateTorus("crown", { diameter: 2.4, thickness: 0.5, tessellation: 20 }, scene);
    this.mesh.rotation.x = Math.PI/2;
    this.handMat = new BABYLON.StandardMaterial("crownm", scene);
    this.handMat.emissiveColor = new BABYLON.Color3(0.5,0.7,1); this.handMat.disableLighting = true;
    this.handMat.specularColor = new BABYLON.Color3(0.8,0.8,1);
    this.mesh.material = this.handMat; this.mesh.isPickable = false;
    // inner core
    const core = BABYLON.MeshBuilder.CreateSphere("core", { diameter: 1.1 }, scene);
    const cm = new BABYLON.StandardMaterial("corem", scene);
    cm.emissiveColor = new BABYLON.Color3(1,1,1); cm.disableLighting = true;
    core.material = cm; core.setParent(this.mesh); core.position.y = 0.9; core.rotation.x = -Math.PI/2;
    this.auraPS = Particles.trail(this.mesh.position, new BABYLON.Color4(0.5,0.7,1,1));
    this.auraPS.emitRate = 40;
    // hoversphere for physics debris collision (visual)
  },
  update(dt) {
    if (!this.alive || A.S.paused) return;
    this.firing = (pointerDown && !pointerGrab);
    const k = Input.keys;
    let mx = (k.d?1:0)-(k.a?1:0), mz = (k.s?1:0)-(k.w?1:0);
    // camera-relative movement
    const fwd = camera.getTarget().subtract(camera.position); fwd.y = 0; fwd.normalize();
    const right = BABYLON.Vector3.Cross(fwd, BABYLON.Vector3.Up()).normalize();
    const acc = right.scale(mx).add(fwd.scale(-mz)).normalize().scale(this.phase>0?26:18);
    this.vx += acc.x*dt; this.vz += acc.z*dt;
    this.vx *= (1-8*dt); this.vz *= (1-8*dt);
    this.overdrive = Math.max(0, this.overdrive - dt);
    this.phase = Math.max(0, this.phase - dt);
    this.x += this.vx*dt; this.z += this.vz*dt;
    const R = G.worldR - 2;
    this.x = Math.max(-R, Math.min(R, this.x)); this.z = Math.max(-R, Math.min(R, this.z));
    this.reality = Math.min(this.maxReality, this.reality + dt*6);
    if (this.comboT>0){ this.comboT-=dt; if(this.comboT<=0) this.combo=0; }
    // firing
    if (this.firing) {
      this.fireAcc -= dt;
      const rate = this.fireRate / (this.overdrive>0?6:(Abilities.fx.riftActive?2.5:(Abilities.fx.echoes.length?1.6:1)));
      if (this.fireAcc<=0) {
        this.fireAcc = rate;
        const from = this.position().add(new BABYLON.Vector3(0,0.8,0));
        Bolts.fire(from, this.aimPoint(), this.overdrive>0?new BABYLON.Color3(1,0.82,1):new BABYLON.Color3(0.5,1,1), this.overdrive>0?60:26);
        Synth.shoot(this.overdrive>0);
      }
    }
    // hover bob + orientation
    this.mesh.position.set(this.x, 1.4 + Math.sin(A.S.elapsed*2)*0.15, this.z);
    this.mesh.rotation.y += dt*1.5;
    this.auraPS.emitter = this.mesh.position;
    // prism phase tint
    this.handMat.emissiveColor = this.phase>0 ? new BABYLON.Color3(1,0.82,1) : (this.overdrive>0?new BABYLON.Color3(1,1,1):new BABYLON.Color3(0.5,0.7,1));
    // camera target follows player
    camera.target = BABYLON.Vector3.Lerp(camera.target, new BABYLON.Vector3(this.x,1,this.z), Math.min(1, dt*3));
  }
};

/* ------------------------------------------------------------------ [AGENT 7: TEMPORALIST] bullet-time + rewind ghosts */
const Temporalist = (() => {
  let history = [];
  let ghostPS = null;
  function sample() {
    if (A.S.targetTimeScale < 0.5) {
      if (!ghostPS) ghostPS = Particles.trail(new BABYLON.Vector3(0,0,0), new BABYLON.Color4(0.5,1,0.8,1));
      for (const e of Entropy.list()) { if (e.dead) continue; Particles.burst(e.mesh.position, 2, new BABYLON.Color4(0.5,1,0.8,0.6), 1, 1); }
    } else if (ghostPS) { ghostPS.dispose(); ghostPS = null; }
  }
  return { sample };
})();

/* ------------------------------------------------------------------ [AGENT 10: SYNTHESIZER] procedural WebAudio */
const Synth = (() => {
  let ctx = null, master = null, musicOn = false;
  function ensure(){
    if (ctx) return;
    const AC = window.AudioContext || window.webkitAudioContext; if (!AC) return;
    ctx = new AC();
    master = ctx.createGain(); master.gain.value = A.settings.volume; master.connect(ctx.destination);
    startMusic();
  }
  function env(f, dur, type, vol, slide, delay){
    if (!ctx) return;
    const t = ctx.currentTime + (delay||0);
    const o = ctx.createOscillator(), g = ctx.createGain();
    o.type = type; o.frequency.setValueAtTime(f,t);
    if (slide) o.frequency.exponentialRampToValueAtTime(slide, t+dur);
    g.gain.setValueAtTime(0.0001,t); g.gain.exponentialRampToValueAtTime(vol,t+0.01); g.gain.exponentialRampToValueAtTime(0.0001,t+dur);
    o.connect(g); g.connect(master); o.start(t); o.stop(t+dur+0.02);
  }
  function noise(dur,vol,hp){
    if (!ctx) return; const t = ctx.currentTime;
    const len = Math.floor(ctx.sampleRate*dur); const buf = ctx.createBuffer(1,len,ctx.sampleRate);
    const d = buf.getChannelData(0); for(let i=0;i<len;i++) d[i]=Math.random()*2-1;
    const src = ctx.createBufferSource(); src.buffer = buf;
    const f = ctx.createBiquadFilter(); f.type="highpass"; f.frequency.value=hp;
    const g = ctx.createGain(); g.gain.setValueAtTime(vol,t); g.gain.exponentialRampToValueAtTime(0.0001,t+dur);
    src.connect(f); f.connect(g); g.connect(master); src.start(t); src.stop(t+dur);
  }
  const shoot = (big)=>env(big?900:620,0.08,"square",0.05,big?200:380);
  const hit = (big)=>{ if(big) noise(0.3,0.5,120); else noise(0.12,0.25,400); env(big?90:160,0.2,"sawtooth",0.2,40); };
  const ouch = ()=>{ env(300,0.2,"sawtooth",0.3,80); noise(0.2,0.3,200); };
  const bell = ()=>[523,659,784,1046].forEach((f,i)=>env(f,0.6,"sine",0.18,null,i*0.06));
  const boom = (f)=>{ env(f||120,0.7,"sine",0.5,30); noise(0.5,0.5,60); };
  const singularity = ()=>{ env(80,1.2,"sine",0.4,24); noise(0.8,0.3,80); };
  const echo = ()=>[880,1320].forEach((f,i)=>env(f,0.4,"triangle",0.2,null,i*0.09));
  const lash = ()=>{ env(1400,0.15,"sawtooth",0.3,200); noise(0.15,0.3,1000); };
  const phase = ()=>env(500,0.3,"sine",0.25,1000);
  const genesis = ()=>{ env(60,1.8,"sawtooth",0.5,30); noise(1.2,0.5,80); [220,330,440,660].forEach((f,i)=>env(f,1.4,"sine",0.15,null,i*0.15)); };
  const SCALE=[0,2,4,7,9], ROOT=55;
  function note(step,dur,oct){ const idx=step%SCALE.length, oo=Math.floor(step/SCALE.length); const f=ROOT*Math.pow(2,(SCALE[idx]+12*oo+(oct||0)*12)/12); env(f,dur*0.9,"triangle",0.12); env(f*0.5,dur*0.9,"sine",0.14); }
  function drum(step){ if(step%2===0){noise(0.12,0.16,400);env(110,0.2,"sine",0.3,40);} if(step%4===2) noise(0.05,0.06,6000); }
  function startMusic(){
    if (musicOn || !ctx) return; musicOn = true; let step=0;
    setInterval(()=>{ if(!A.S.playing||A.S.paused) return; const beat = A.S.targetTimeScale<0.5?0.42:0.16; note(step*3,beat*2,2); if(step%2===0) note(step*5+1,beat*2,1); drum(step); if(A.S.targetTimeScale<0.5 && Math.random()<0.3) env(1500+Math.random()*800,0.3,"sine",0.05,null,Math.random()*0.2); step++; },150);
  }
  function start(){ ensure(); if(ctx && ctx.state==="suspended") ctx.resume(); }
  function setVol(v){ if(master) master.gain.value=v; }
  return { start, setVol, shoot, hit, ouch, bell, boom, singularity, echo, lash, phase, genesis };
})();

/* ------------------------------------------------------------------ [AGENT 12: HUDMIND] menus & HUD */
const UI = (() => {
  const panels = {};
  function $(id){ return document.getElementById(id); }
  function init() {
    ["menu-main","menu-controls","menu-abilities","menu-settings","menu-pause","menu-death"].forEach(id => panels[id] = $(id));
    const btns = document.querySelectorAll("[data-action]");
    btns.forEach(b => b.addEventListener("click", () => {
      const a = b.getAttribute("data-action");
      if (a==="start") startGame();
      else if (a==="resume") togglePause();
      else if (a==="retry") startGame();
      else if (a==="main") gotoMain();
      else if (a==="controls") show("menu-controls");
      else if (a==="abilities") show("menu-abilities");
      else if (a==="settings") show("menu-settings");
    }));
    $("set-vol").addEventListener("input", e => { A.settings.volume = parseFloat(e.target.value); Synth.setVol(A.settings.volume); });
    $("set-sens").addEventListener("input", e => { A.settings.sensitivity = parseFloat(e.target.value); if(camera){ camera.wheelDeltaPercentage = 0.02*A.settings.sensitivity; camera.angularSensibilityX = 800/A.settings.sensitivity; camera.angularSensibilityY = 800/A.settings.sensitivity; } });
    $("set-diff").addEventListener("change", e => A.settings.difficulty = parseInt(e.target.value));
    $("set-fx").addEventListener("change", e => A.settings.fx = e.target.value);
  }
  function hideAll(){ Object.values(panels).forEach(p => p.classList.add("hidden")); }
  function show(name){ hideAll(); panels[name].classList.remove("hidden"); }
  function gotoMain(){ hideAll(); show("menu-main"); if (A.S.playing) A.S.playing = false; $("hud").classList.add("hidden"); }
  function buildHUD(){
    const hud = $("hud");
    const abWrap = hud.querySelector(".abilities");
    abWrap.innerHTML = Abilities.defs.map((d,i)=>`<div class="ab ${d.ult?'ult':''}"><span class="k">${d.key.toUpperCase()}</span><span class="ico">${d.ico}</span><span class="nm">${d.name}</span><div class="cd" style="display:none"></div></div>`).join("");
  }
  function center(str){
    const el = $("ct"); el.textContent = str; el.classList.remove("show"); void el.offsetWidth; el.classList.add("show");
  }
  const announce = center;
  function flytext(pos, str){
    const el = document.createElement("div"); el.className="dmg"; el.textContent=str;
    document.body.appendChild(el);
    const proj = BABYLON.Vector3.Project(pos, BABYLON.Matrix.Identity(), scene.getTransformMatrix(), camera.viewport.toGlobal(engine.getRenderWidth(), engine.getRenderHeight()));
    el.style.left = (proj.x - 20) + "px"; el.style.top = (proj.y - 20) + "px";
    setTimeout(()=>el.remove(), 900);
  }
  function grab(active){ const g = $("grabind"); g.style.opacity = active?1:0; }
  function die(){
    const stats = $("menu-death").querySelector(".dead-stats");
    stats.innerHTML = `<div><b>${Player.score.toLocaleString()}</b>REALITY SEVERED</div><div><b>${Entropy.wave}</b>WAVES SURVIVED</div><div><b>${Player.kills}</b>SHARDS UNMADE</div><div><b>${Math.floor(A.S.elapsed)}s</b>BORROWED</div>`;
    show("menu-death"); Synth.boom(70); Synth.ouch();
  }
  function tickHUD(){
    const hp = $("hud").querySelector(".hp i"), re = $("hud").querySelector(".reality i");
    if(hp) hp.style.width = (Player.hp/Player.maxHp*100)+"%";
    if(re) re.style.width = (Player.reality/Player.maxReality*100)+"%";
    $("sc").textContent = Player.score.toLocaleString();
    $("cb").textContent = Player.combo>1?`COMBO ×${Player.comboMult().toFixed(1)} (${Player.combo})`:"";
    $("wv").textContent = Entropy.wave;
    const abEls = $("hud").querySelectorAll(".ab");
    abEls.forEach((el,i)=>{ const st=Abilities.state[i]; const cd=el.querySelector(".cd"); if(st.cd>0){cd.style.display="flex";cd.textContent=Math.ceil(st.cd);} else cd.style.display="none"; el.classList.toggle("active", (i===7&&Player.overdrive>0)||(Abilities.fx.riftActive&&i===1)||(Abilities.fx.grabTarget&&i===3)); });
  }
  return { init, buildHUD, center, announce, flytext, die, grab, tickHUD, gotoMain, hideAll, show };
})();

/* ------------------------------------------------------------------ [AGENT 13: NARRATIVE] lore boot */
const Narrative = (() => {
  const lines = ["forging reality…","binding the wound…","awakening the crown…"];
  let i = 0;
  function run(){
    const boot = document.getElementById("boot"); const sub = document.getElementById("bootsub");
    const print = ()=>{ if(i>=lines.length){ boot.classList.add("hide"); setTimeout(()=>boot.style.display="none", 900); return; } sub.textContent=lines[i++]; setTimeout(print, 900); };
    print();
  }
  return { run };
})();

/* ------------------------------------------------------------------ [AGENT 14: BALANCER] */
const Balance = (() => {
  function adjust() {
    // difficulty already scales budget in Entropy
    return 1 + (Entropy.wave-1)*0.06;
  }
  return { adjust };
})();

/* ------------------------------------------------------------------ [AGENT 15: POLISHER] shake, flash, juice */
const Polish = (() => {
  function update(dt){
    A.S.shake = Math.max(0, A.S.shake - dt*40);
    A.S.flash = Math.max(0, A.S.flash - dt*1.6);
    // camera shake
    if (A.S.shake > 0) {
      camera.target = camera.target.add(new BABYLON.Vector3((Math.random()-0.5)*A.S.shake*0.06, 0, (Math.random()-0.5)*A.S.shake*0.06));
    }
  }
  return { update };
})();

/* ------------------------------------------------------------------ INPUT */
const Input = (() => {
  const keys = {};
  function bind(){
    window.addEventListener("keydown", e => {
      const k = e.key.toLowerCase();
      if (k===" ") e.preventDefault();
      if (k==="escape") { togglePause(); return; }
      if (keys[k]) return;
      keys[k] = true;
      const idx = Abilities.defs.findIndex(d => d.key===e.key.toLowerCase());
      if (idx>=0) Abilities.tryCast(idx);
    });
    window.addEventListener("keyup", e => keys[e.key.toLowerCase()]=false);
    canvas.addEventListener("pointermove", e => { pointer.x=e.clientX; pointer.y=e.clientY; });
    canvas.addEventListener("pointerdown", e => { if (e.button===0) pointerDown = true; if (e.button===2) pointerGrab = true; });
    canvas.addEventListener("pointerup", e => { if (e.button===0) pointerDown = false; if (e.button===2) pointerGrab = false; });
    canvas.addEventListener("contextmenu", e => e.preventDefault());
  }
  return { keys, bind };
})();
let pointerDown = false, pointerGrab = false;

/* ------------------------------------------------------------------ WORLD BUILD & BOOT */
const debris = [];  // physics-ready asteroid meshes, aggregated after Havok loads

function buildWorld() {
  Lighting.build();
  Cosmos.build();
  // arena floor
  const floor = BABYLON.MeshBuilder.CreateDisc("floor", { radius: G.worldR, tessellation: 64 }, scene);
  floor.rotation.x = Math.PI/2;
  const fm = new BABYLON.StandardMaterial("floorm", scene);
  fm.diffuseColor = new BABYLON.Color3(0.05,0.03,0.12);
  fm.specularColor = new BABYLON.Color3(0.1,0.1,0.2);
  fm.emissiveColor = new BABYLON.Color3(0.02,0.01,0.05);
  floor.material = fm; floor.isPickable = false;
  // glow ring boundary
  const ring = BABYLON.MeshBuilder.CreateTorus("arena", { diameter: G.worldR*2, thickness: 0.5, tessellation: 64 }, scene);
  ring.rotation.x = Math.PI/2; ring.position.y = 0.25;
  const rm = new BABYLON.StandardMaterial("arenaRing", scene);
  rm.emissiveColor = new BABYLON.Color3(0.5,0.3,1); rm.disableLighting = true;
  ring.material = rm; ring.isPickable = false;

  // decorative asteroid debris (meshes only; aggregates added after Havok enables)
  for (let i=0;i<12;i++) {
    const ast = BABYLON.MeshBuilder.CreateIcoSphere("ast"+i, { radius: 0.4+Math.random()*0.8, subdivisions: 2 }, scene);
    ast.position = new BABYLON.Vector3((Math.random()-0.5)*80, 5+Math.random()*10, (Math.random()-0.5)*80);
    const am = new BABYLON.StandardMaterial("astm"+i, scene);
    const g = 0.2+Math.random()*0.3; am.diffuseColor = new BABYLON.Color3(g,g,g+0.1);
    am.specularColor = new BABYLON.Color3(0.2,0.2,0.2);
    ast.material = am; ast.isPickable = false;
    ast.rotation.x = Math.random()*6; ast.rotation.y = Math.random()*6;
    ast.physicsDebris = true;
    debris.push(ast);
  }
  Player.build();
  G.registerLensing();
}

// Called only after the Havok plugin is enabled, so aggregates are valid.
function enableDebrisPhysics() {
  try {
    if (typeof BABYLON.PhysicsAggregate === "undefined") return;
    const floor = BABYLON.MeshBuilder.CreateGround("phfloor", 200, 200, 1, scene);
    floor.position.y = -0.5; floor.isPickable = false;
    new BABYLON.PhysicsAggregate(floor, BABYLON.PhysicsShapeType.BOX, { mass: 0 }, scene);
    for (const ast of debris) {
      new BABYLON.PhysicsAggregate(ast, BABYLON.PhysicsShapeType.SPHERE, { mass: 1, friction: 0.6, restitution: 0.3 }, scene);
    }
    G.physicsReady = true;
  } catch(e) { console.warn("physics aggregates disabled", e); }
}

/* ------------------------------------------------------------------ MAIN LOOP */
function startLoop() {
  engine.runRenderLoop(() => {
    const dt = Math.min(engine.getDeltaTime()/1000, 0.05);
    A.S.timeScale += (A.S.targetTimeScale - A.S.timeScale) * Math.min(1, dt*8);
    if (Math.abs(A.S.targetTimeScale-A.S.timeScale) < 0.005) A.S.timeScale = A.S.targetTimeScale;
    const sdt = dt * A.S.timeScale;
    A.S.elapsed += sdt; A.S.frame++;
    A.S.gameDt = sdt;
    if (A.S.playing && !A.S.paused) {
      A.S.gameTime += sdt;
      Temporalist.sample();
      Balance.adjust();
      Entropy.update(sdt);
      Player.update(sdt);
      Entropy.behaviors(sdt);
      Bolts.update(sdt);
      Abilities.update(sdt);
      Fractalist.update(sdt);
      G.update(sdt);
      G.drawBeams();
      Polish.update(dt);
      UI.tickHUD();
    } else if (A.S.playing && A.S.paused) {
      // still render
    }
    scene.render();
  });
}

/* ------------------------------------------------------------------ GAME CONTROL */
function startGame() {
  A.S.playing = true; A.S.dead = false; A.S.paused = false;
  A.S.timeScale = 1; A.S.targetTimeScale = 1;
  A.S.shake = 0; A.S.flash = 0; A.S.gameTime = 0;
  gravityY = GRAVITY_BASE;
  Entropy.clear(); Entropy.wave = 0;
  for (const bh of G.blackHoles) { try{bh.sphere.dispose();bh.disk.dispose();bh.ps.dispose();}catch(e){} }
  G.blackHoles.length = 0; G.beams.length = 0;
  Abilities.fx.echoes = []; Abilities.fx.prismTrail = [];
  Abilities.fx.riftActive = false; Abilities.fx.inversion = 0; Abilities.fx.grabTarget = null;
  for (const st of Abilities.state) st.cd = 0;
  Player.x = 0; Player.z = 0; Player.vx = 0; Player.vz = 0;
  Player.hp = Player.maxHp; Player.reality = Player.maxReality;
  Player.alive = true; Player.phase = 0; Player.overdrive = 0;
  Player.score = 0; Player.combo = 0; Player.kills = 0;
  Entropy.newWave();
  UI.hideAll();
  document.getElementById("hud").classList.remove("hidden");
  Synth.start();
}

function togglePause() {
  if (!A.S.playing) return;
  A.S.paused = !A.S.paused;
  const p = document.getElementById("menu-pause");
  p.classList.toggle("hidden", !A.S.paused);
  camera.detachControl(canvas);
  camera.attachControl(canvas, true);
}

/* ------------------------------------------------------------------ BOOT & DIAGNOSTICS */
const Boot = (() => {
  function showError(msg) {
    const err = document.getElementById("booterr");
    const actions = err && err.nextElementSibling;
    if (err) { err.textContent = msg; err.style.display = "block"; }
    if (actions) actions.style.display = "flex";
    const sub = document.getElementById("bootsub");
    if (sub) sub.textContent = "REALITY FAILED TO FORGE";
  }
  function hideError() {
    const err = document.getElementById("booterr");
    const actions = err && err.nextElementSibling;
    if (err) err.style.display = "none";
    if (actions) actions.style.display = "none";
  }
  return { showError, hideError };
})();

function bootFailed(msg) {
  console.error("CROWN boot failed:", msg);
  Boot.showError(msg);
  try { engine && engine.dispose(); } catch(e) {}
}

async function boot() {
  Boot.hideError();
  // verify Babylon CDN loaded
  if (typeof BABYLON === "undefined") {
    bootFailed("Babylon.js did not load from the CDN.\n\n" +
      "Your browser or the preview network is blocking cdn.babylonjs.com.\n" +
      "Make sure you have internet access, then press RETRY.");
    return;
  }
  try {
    createEngine();
  } catch (e) {
    bootFailed("WebGL could not be created: " + (e.message || e) + "\n\n" +
      "This preview environment may not support WebGL. Try a desktop browser with hardware acceleration.");
    return;
  }
  try {
    createCamera();
    buildWorld();
    Input.bind();
    UI.init();
    UI.buildHUD();
  } catch (e) {
    bootFailed("Scene setup error: " + (e && e.stack || e));
    return;
  }
  // Havok async (optional — game runs even without physics)
  try {
    if (typeof HavokPhysics !== "undefined") {
      const havok = await HavokPhysics();
      if (scene) scene.enablePhysics(new BABYLON.Vector3(0, gravityY, 0), new BABYLON.HavokPlugin(true, havok));
      enableDebrisPhysics();
    } else {
      enableDebrisPhysics(); // no havok; still try aggregates (they may no-op safely)
    }
  } catch(e) { console.warn("havok", e); }
  Narrative.run();
  startLoop();
  UI.show("menu-main");
}

// surface any uncaught runtime error on the boot screen so it never hangs silently
window.addEventListener("error", (ev) => {
  if (A.S.playing) return; // only surface pre-game crashes
  Boot.showError("Runtime error: " + (ev.error ? (ev.error.stack || ev.error.message) : ev.message));
});
window.addEventListener("unhandledrejection", (ev) => {
  if (A.S.playing) return;
  const r = ev.reason;
  Boot.showError("Async error: " + (r && (r.stack || r.message) || r));
});

window.addEventListener("load", () => { boot(); });
document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("boot-retry");
  if (btn) btn.addEventListener("click", () => location.reload());
});

/* Exposed for automated harness testing only. */
window.__GAME = { startGame, togglePause, Player, Abilities, A };
