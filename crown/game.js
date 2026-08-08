/* ============================================================================
   CROWN OF THE UNMADE
   A reality-warping god-arena. You are the god who unmakes reality.
   Assembled by a swarm of 15 sub-agents, each owning one module.
   Sections tagged [AGENT] below.
   ========================================================================== */
"use strict";

/* ------------------------------------------------------------------ [AGENT 1: ARCHITECT] Core state, loop, orchestrator
   Owns the frame clock, the state machine, the global time-warp that
   every other system samples, and the main update/render dispatch.   */
const A = (() => {
  const S = {
    boot: true, playing: false, dead: false,
    time: 0,          // accumulated unpaused world time
    frame: 0,
    timeScale: 1,     // global temporal warp (TEMPORALIST writes this)
    targetTimeScale: 1,
    shake: 0,
    flash: 0,
    gameTime: 0,      // elapsed in-play seconds
  };
  let last = performance.now();
  const raw = { dt: 0 };

  function tick(now) {
    raw.dt = Math.min((now - last) / 1000, 0.05);
    last = now;
    // ease global time scale back toward 1
    S.timeScale += (S.targetTimeScale - S.timeScale) * Math.min(1, raw.dt * 8);
    if (Math.abs(S.targetTimeScale - S.timeScale) < 0.005) S.timeScale = S.targetTimeScale;
    S.frame++;
    return raw;
  }

  const dispatch = [];
  function update(dt, t) { for (const f of dispatch) f(dt, t); }
  function add(f) { dispatch.push(f); return f; }
  function remove(f) { const i = dispatch.indexOf(f); if (i >= 0) dispatch.splice(i, 1); }

  return { S, tick, update, add, remove };
})();

/* ------------------------------------------------------------------ [AGENT 2: COSMOLOGY] The living nebula world field
   Generates the ambient backdrop: a rotating starfield, drifting dust,
   and a breathing nebula built from layered radial noise.        */
const Cosmos = (() => {
  const stars = [];
  for (let i = 0; i < 260; i++) stars.push({
    x: Math.random() * 2 - 1, y: Math.random() * 2 - 1,
    z: 0.25 + Math.random() * 0.75, tw: Math.random() * Math.PI * 2,
  });
  const dust = [];
  for (let i = 0; i < 90; i++) dust.push({
    x: Math.random(), y: Math.random(), r: 1 + Math.random() * 3,
    h: Math.random() * 40 - 20, s: 0.2 + Math.random() * 0.5, ph: Math.random() * 6,
  });

  function draw(c, w, h, t) {
    // deep space base with a breathing nebula
    const neb = c.createRadialGradient(w/2, h*0.42, 40, w/2, h*0.42, Math.max(w,h)*0.75);
    const pulse = 0.5 + 0.5 * Math.sin(t * 0.35);
    neb.addColorStop(0, `rgba(${70+30*pulse},20,120,${0.5+0.2*pulse})`);
    neb.addColorStop(0.45, `rgba(20,6,50,0.55)`);
    neb.addColorStop(1, `rgba(3,1,10,1)`);
    c.fillStyle = neb; c.fillRect(0, 0, w, h);

    // second drifting nebula cloud
    const cx = w * (0.5 + 0.25 * Math.sin(t * 0.12));
    const cy = h * (0.35 + 0.3 * Math.cos(t * 0.09));
    const cloud = c.createRadialGradient(cx, cy, 10, cx, cy, Math.max(w,h)*0.5);
    cloud.addColorStop(0, `rgba(30,80,140,0.22)`);
    cloud.addColorStop(0.6, `rgba(10,30,80,0.12)`);
    cloud.addColorStop(1, `rgba(0,0,0,0)`);
    c.fillStyle = cloud; c.fillRect(0, 0, w, h);

    // stars
    for (const s of stars) {
      const tw = 0.5 + 0.5 * Math.sin(t * 2 + s.tw);
      const px = (s.x * 0.5 + 0.5) * w, py = (s.y * 0.5 + 0.5) * h;
      c.globalAlpha = tw * 0.8;
      c.fillStyle = s.z > 0.75 ? "#cfe9ff" : "#ffffff";
      const size = s.z * 1.6;
      c.fillRect(px, py, size, size);
    }
    c.globalAlpha = 1;

    // drifting dust motes
    for (const d of dust) {
      const dx = ((d.x + t * 0.005 * d.s) % 1 + 1) % 1 * w;
      const dy = ((d.y - t * 0.002 * d.s) % 1 + 1) % 1 * h;
      const a = 0.25 + 0.25 * Math.sin(t * 1.5 + d.ph);
      c.globalAlpha = Math.max(0, a);
      c.fillStyle = `hsl(${230+d.h},60%,80%)`;
      c.beginPath(); c.arc(dx, dy, d.r, 0, 6.283); c.fill();
    }
    c.globalAlpha = 1;
  }
  return { draw };
})();

/* ------------------------------------------------------------------ [AGENT 3: ENTROPY] Enemy hive & wave genesis
   Defines the enemy species, their behaviours, and the escalating
   wave-birth algorithm that keeps reality collapsing inward.     */
const Entropy = (() => {
  const SPECIES = {
    drifter: { r: 15, hp: 34, speed: 70, color: "#ff6b8d", score: 10, dmg: 12 },
    shard:   { r: 9,  hp: 14, speed: 165, color: "#7df9ff", score: 6,  dmg: 8, wob: true },
    behemoth:{ r: 42, hp: 260, speed: 38, color: "#c084ff", score: 55, dmg: 30, splits: 3 },
    wraith:  { r: 13, hp: 60, speed: 130, color: "#9dffd0", score: 30, dmg: 18, phase: true },
    mine:    { r: 12, hp: 20, speed: 0, color: "#ffd166", score: 5, dmg: 22, boom: true },
    sunmaw:  { r: 58, hp: 700, speed: 26, color: "#ff9d5c", score: 150, dmg: 26, maws: true },
  };
  let enemies = [];
  const S = A.S;
  let wave = 0;
  let spawnAcc = 0;
  let budget = 0;

  function add(x, y, type) {
    const sp = SPECIES[type];
    enemies.push({
      type, x, y, vx: 0, vy: 0, dead: false,
      r: sp.r, hp: sp.hp, maxHp: sp.hp, speed: sp.speed,
      color: sp.color, score: sp.score, dmg: sp.dmg,
      wob: sp.wob, phase: sp.phase, boom: sp.boom, splits: sp.splits, maws: sp.maws,
      phaseT: 0, birth: S.time, seed: Math.random() * 6.28, hit: 0,
      life: sp.boom ? 9 : 0,
    });
  }

  function newWave() {
    wave++;
    spawnAcc = 0;
    budget = 8 + wave * 5 + wave * wave * 0.7;
    UI.center(`WAVE ${wave}`);
    if (wave > 1) Synth.bell();
  }

  function update(dt, t) {
    if (!S.playing) return;
    // time-based wave escalation
    if (S.gameTime > 2) { newWave(); S.gameTime = 0; }

    spawnAcc += dt;
    const interval = Math.max(0.18, 0.55 - wave * 0.012);
    if (spawnAcc >= interval && budget > 0) {
      spawnAcc = 0; budget--;
      spawnOne();
    }
    // behaviour update (in the play loop via ENTROPY.update)
  }

  function spawnOne() {
    const ang = Math.random() * Math.PI * 2;
    const dist = 680 + Math.random() * 160;
    const px = G.player.x + Math.cos(ang) * dist;
    const py = G.player.y + Math.sin(ang) * dist;
    const roll = Math.random();
    const p = Player;
    let type = "drifter";
    if (wave >= 2 && roll < 0.3) type = "shard";
    if (wave >= 3 && roll > 0.8 && roll < 0.86) type = "behemoth";
    if (wave >= 4 && roll > 0.6 && roll < 0.66) type = "wraith";
    if (wave >= 2 && roll > 0.4 && roll < 0.44) type = "mine";
    if (wave >= 6 && roll > 0.94) type = "sunmaw";
    // cap extreme species
    const count = { behemoth:0, sunmaw:0, wraith:0 };
    for (const e of enemies) if (count[e.type] !== undefined) count[e.type]++;
    if (type === "behemoth" && count.behemoth >= 3) type = "drifter";
    if (type === "sunmaw" && count.sunmaw >= 1) type = "drifter";
    if (type === "wraith" && count.wraith >= 8) type = "drifter";
    add(px, py, type);
  }

  function behaviors(dt) {
    const pl = Player;
    for (const e of enemies) {
      if (e.dead) continue;
      if (e.life > 0) { e.life -= dt; if (e.life <= 0) { kill(e, false, false); continue; } }
      if (e.type === "mine") { if (G.dist(e, pl) < 130 && pl.alive) explodeMine(e); continue; }
      const dx = pl.x - e.x, dy = pl.y - e.y;
      const d = Math.hypot(dx, dy) || 1;
      if (e.phase) {
        e.phaseT += dt;
        if (e.phaseT > 1.6) { // teleport
          e.phaseT = 0;
          const a = Math.random() * 6.28;
          e.x = pl.x + Math.cos(a) * 120;
          e.y = pl.y + Math.sin(a) * 120;
        }
      }
      let sp = e.speed;
      if (e.wob) { const w = Math.sin(S.time * 6 + e.seed) * 1.4; sp *= (1 + w); }
      if (e.maws && d < 220) { // sunmaw lunges
        sp *= 2.2;
        if (d < 150) sp *= 2;
      }
      // separate soft from neighbours
      for (const o of enemies) {
        if (o === e) continue;
        const ox = e.x - o.x, oy = e.y - o.y;
        const od = Math.hypot(ox, oy);
        const min = e.r + o.r;
        if (od > 0.01 && od < min) { e.x += ox/od * (min-od) * 0.5; e.y += oy/od * (min-od) * 0.5; }
      }
      // flock toward player
      const pull = (e.maws && d < 260) ? 1.6 : 1;
      e.vx = (e.vx + (dx/d) * sp * pull * dt) * 0.9;
      e.vy = (e.vy + (dy/d) * sp * pull * dt) * 0.9;
      e.x += e.vx * dt;
      e.y += e.vy * dt;
      e.hit = Math.max(0, e.hit - dt);
      // gravity fields from black holes pull enemies
      G.gravityPull(e, dt);
      // arena bounds
      const R = G.worldR;
      if (e.x > R || e.x < -R || e.y > R || e.y < -R) {
        const tx = Math.max(-R, Math.min(R, pl.x)), ty = Math.max(-R, Math.min(R, pl.y));
        e.x = Math.max(-R, Math.min(R, e.x));
        e.y = Math.max(-R, Math.min(R, e.y));
        const n = Math.hypot(tx-e.x, ty-e.y)||1;
        e.x += (tx-e.x)/n*4; e.y += (ty-e.y)/n*4;
      }
    }
    // collision vs player
    if (pl.alive) {
      for (const e of enemies) {
        if (e.boom) continue;
        if (G.dist(e, pl) < e.r + pl.r) {
          const dmg = e.dmg;
          if (Player.damage(dmg, e)) {
            kill(e, false, false);
          }
          // knock player back
          const dx = pl.x - e.x, dy = pl.y - e.y, d = Math.hypot(dx, dy)||1;
          pl.vx += dx/d * 260; pl.vy += dy/d * 260;
          if (!pl.alive) return;
        }
      }
    }
  }

  function explodeMine(e) {
    e.hp = 0;
    Synth.boom(60);
    Particles.blast(e.x, e.y, 26, "#ffd166", 40, 300);
    A.S.shake = Math.max(A.S.shake, 6);
    const pl = Player;
    if (pl.alive && G.dist(e, pl) < 70) Player.damage(e.dmg, e);
  }

  function kill(e, byPlayer, grantVoid) {
    if (!e || e.dead) return;
    e.dead = true;
    Particles.blast(e.x, e.y, e.r * 1.2, e.color, 26, 260);
    Synth.hit(e.type === "behemoth" || e.type === "sunmaw");
    if (byPlayer) {
      const pts = e.score * Player.comboMult();
      Player.addScore(pts);
      Particles.text(e.x, e.y, `+${pts}`, e.color);
      if (e.splits) for (let k = 0; k < e.splits; k++) add(e.x + (Math.random()*30-15), e.y + (Math.random()*30-15), "drifter");
      if (grantVoid) Player.heal(3);
    }
    if (e.boom) Particles.blast(e.x, e.y, 30, "#fff", 20, 200);
  }

  // sweep dead enemies at a safe point each frame
  function sweep() {
    for (let i = enemies.length - 1; i >= 0; i--) if (enemies[i].dead) enemies.splice(i, 1);
  }

  function clear() { enemies.length = 0; }
  function count() { return enemies.length; }
  return {
    add, update, behaviors, kill, clear, count, newWave, sweep, SPECIES,
    get wave() { return wave; }, set wave(v) { wave = v; },
    get enemies() { return enemies; },
  };
})();

/* ------------------------------------------------------------------ [AGENT 4: REALITY WEAVER] The seven abilities
   Q/E/R/SPACE/F/C/X — each one bends a different law of reality.  */
const Abilities = (() => {
  const S = A.S;
  const defs = [
    { key: "q", name: "SINGULARITY", ico: "◉", cd: 15, ult: false },
    { key: "e", name: "TEMPORAL RIFT", ico: "⟲", cd: 11, ult: false },
    { key: "r", name: "FRACTAL ECHO", ico: "❖", cd: 12, ult: false },
    { key: " ", name: "GRAVINVERS", ico: "⧉", cd: 9, ult: false },
    { key: "f", name: "VOID LASH", ico: "⌁", cd: 7, ult: false },
    { key: "c", name: "PRISM PHASE", ico: "◈", cd: 6, ult: false },
    { key: "x", name: "GENESIS", ico: "☀", cd: 32, ult: true },
  ];
  const state = defs.map(d => ({ cd: 0, dur: 0 }));

  // sub-fields for persistent effects
  const fx = {
    echoes: [],         // FRACTALIST echoes
    prismTrail: [],     // prism phase trail
    riftActive: false,
    inversion: 0,       // remaining grav-inversion time
  };

  function canUse(i) { return state[i].cd <= 0 && state[i].dur <= 0 && Player.alive; }

  function tryCast(i) {
    if (!canUse(i)) return false;
    const d = defs[i];
    state[i].cd = d.cd;
    state[i].dur = d.dur;
    UI.announce(d.name);
    const funcs = [singularity, temporalRift, fractalEcho, gravInverse, voidLash, prismPhase, genesis];
    funcs[i]();
    return true;
  }

  function useableCds(i) { return state[i].cd; }

  /* Q — SINGULARITY: birth a black hole that devours matter, then implodes */
  function singularity() {
    G.blackHoles.push({ x: Player.aimX, y: Player.aimY, r: 14, maxR: 190, t: 0, life: 2.6, pull: 900 });
    Synth.singularity();
    Particles.blast(Player.aimX, Player.aimY, 20, "#6ff", 40, 420);
  }
  /* E — TEMPORAL RIFT: bullet time; enemy time slows to a crawl */
  function temporalRift() {
    fx.riftActive = true;
    A.S.targetTimeScale = 0.28;
    Particles.blast(Player.x, Player.y, 30, "#9dffd0", 40, 260);
  }
  /* R — FRACTAL ECHO: split across timelines; echoes mirror your aim */
  function fractalEcho() {
    const ang = [0.6, Math.PI, -0.6];
    fx.echoes = ang.map(a => ({
      x: Player.x + Math.cos(Player.aimAngle + a) * 46,
      y: Player.y + Math.sin(Player.aimAngle + a) * 46,
      t: 5, angle: a,
    }));
    Synth.echo();
  }
  /* SPACE — GRAVITIC INVERSION: flip gravity, fling everything skyward */
  function gravInverse() {
    fx.inversion = 2.0;
    Player.vy -= 900;
    A.S.shake = Math.max(A.S.shake, 10);
    for (const e of Entropy.enemies) { e.vy -= 520; e.vx += (Math.random()-0.5)*300; }
    Particles.blast(Player.x, Player.y - 20, 24, "#c084ff", 30, 400);
    Synth.bell();
  }
  /* F — VOID LASH: a searing tendril that severs reality in a line */
  function voidLash() {
    const px = Player.x, py = Player.y;
    const ang = Player.aimAngle;
    const reach = 760;
    const hits = new Set();
    for (const e of Entropy.enemies) {
      // distance from segment (player -> player+dir*reach) to enemy
      const ax = px, ay = py, bx = px + Math.cos(ang)*reach, by = py + Math.sin(ang)*reach;
      const dx = bx-ax, dy = by-ay;
      const len2 = dx*dx+dy*dy;
      let tt = ((e.x-ax)*dx + (e.y-ay)*dy) / len2; tt = Math.max(0, Math.min(1, tt));
      const nx = ax + dx*tt, ny = ay + dy*tt;
      const d = Math.hypot(e.x-nx, e.y-ny);
      if (d < e.r + 26) {
        hits.add(e);
        Entropy.kill(e, true, true);
      }
    }
    G.beams.push({ x1:px, y1:py, x2:px+Math.cos(ang)*reach, y2:py+Math.sin(ang)*reach, t:0.28, color:"#c084ff", w:10 });
    Particles.blast(px + Math.cos(ang)*reach, py + Math.sin(ang)*reach, 18, "#c084ff", 30, 380);
    Synth.lash();
    if (hits.size === 0) Player.heal(2);
  }
  /* C — PRISM PHASE: become nowhere; leave a damaging light trail */
  function prismPhase() {
    fx.prismTrail = [];
    Player.phase = 2.0;
    Particles.blast(Player.x, Player.y, 20, "#ffd2ff", 30, 260);
    Synth.phase();
  }
  /* X — GENESIS OVERDRIVE: ultimate. unmake everything. */
  function genesis() {
    A.S.flash = 1;
    A.S.shake = Math.max(A.S.shake, 22);
    Player.overdrive = 4.0;
    A.S.targetTimeScale = 0.12;
    Synth.genesis();
    Particles.blast(Player.x, Player.y, 60, "#fff", 80, 600);
  }

  function update(dt, t) {
    for (let i = 0; i < state.length; i++) {
      if (state[i].cd > 0) state[i].cd = Math.max(0, state[i].cd - dt);
    }
    // overdrive and rift end restoring time
    if (Player.overdrive <= 0 && !fx.riftActive) A.S.targetTimeScale = 1;
    if (fx.riftActive && Player.overdrive <= 0) {
      fx.riftActive = false;
      A.S.targetTimeScale = 1;
    }
    // grav inversion timer
    if (fx.inversion > 0) {
      fx.inversion -= dt;
      for (const e of Entropy.enemies) e.vy -= 220 * dt;
      Player.vy -= 140 * dt;
      if (fx.inversion <= 0) {
        // fall damage
        for (const e of Entropy.enemies) {
          if (Math.abs(e.vy) > 220) { e.hp -= 60; Particles.blast(e.x, e.y, e.r, "#fff", 12, 200); if (e.hp <= 0) Entropy.kill(e, true, true); }
          e.vy = 0;
        }
      }
    }
    // prism trail emission
    if (Player.phase > 0 && Player.alive) {
      fx.prismTrail.push({ x: Player.x, y: Player.y, t: 1.2 });
    }
    for (let i = fx.prismTrail.length - 1; i >= 0; i--) {
      fx.prismTrail[i].t -= dt;
      // trail damages enemies
      for (const e of Entropy.enemies) {
        if (G.dist(e, fx.prismTrail[i]) < e.r + 26) { e.hp -= 30*dt; e.hit = 0.12; if (e.hp <= 0) Entropy.kill(e, true, true); }
      }
      if (fx.prismTrail[i].t <= 0) fx.prismTrail.splice(i, 1);
    }
    // echoes
    for (let i = fx.echoes.length - 1; i >= 0; i--) {
      const ec = fx.echoes[i];
      ec.t -= dt;
      ec.x += Math.cos(Player.aimAngle + ec.angle) * 0;
      if (ec.t <= 0) fx.echoes.splice(i, 1);
    }
  }

  function drawEchoes(c) {
    for (const ec of fx.echoes) {
      c.globalAlpha = Math.min(1, ec.t) * 0.8;
      c.fillStyle = "#c8a6ff";
      c.shadowColor = "#c8a6ff"; c.shadowBlur = 18;
      c.beginPath(); c.arc(ec.x, ec.y, Player.r, 0, 6.283); c.fill();
      c.shadowBlur = 0; c.globalAlpha = 1;
      // echo muzzle when firing
      if (Player.firing) Particles.bolt(ec.x, ec.y, Player.aimAngle, "#c8a6ff", false);
    }
  }

  return { defs, state, fx, tryCast, useableCds, update, drawEchoes };
})();

/* ------------------------------------------------------------------ [AGENT 5: PARTICLEFORGE] Particle systems & shockwaves  */
const Particles = (() => {
  let parts = [];
  const S = A.S;
  const texts = [];

  function spawn(p) { parts.push(p); if (parts.length > 1400) parts.splice(0, parts.length - 1400); }

  function blast(x, y, r, color, n, speed) {
    for (let i = 0; i < n; i++) {
      const a = Math.random() * 6.28, sp = speed * (0.3 + Math.random() * 0.9);
      spawn({ x, y, vx: Math.cos(a)*sp, vy: Math.sin(a)*sp, r: r * (0.4 + Math.random()*0.8), life: 0.5 + Math.random()*0.6, max: 1.1, color, add: true, glow: true });
    }
  }

  function bolt(x, y, ang, color, live) {
    const sp = 620;
    spawn({ x, y: y, vx: Math.cos(ang)*sp, vy: Math.sin(ang)*sp, r: 3.5, life: 0.22, max: 0.22, color, add: true, glow: true, bolt: true, live });
  }

  function trail(x, y, color) {
    spawn({ x, y, vx: (Math.random()-0.5)*30, vy: (Math.random()-0.5)*30, r: 3, life: 0.5, max: 0.5, color, add: true, glow: true });
  }

  function text(x, y, str, color) {
    texts.push({ x, y, str, color, t: 1 });
  }

  function ring(x, y, r, color) {
    spawn({ x, y, r: r, vx:0, vy:0, life: 0.5, max: 0.5, color, ring: true, add: true });
  }

  function update(dt, t) {
    for (let i = parts.length - 1; i >= 0; i--) {
      const p = parts[i];
      p.life -= dt;
      if (p.life <= 0) { parts.splice(i, 1); continue; }
      if (!p.ring) { p.x += p.vx*dt; p.y += p.vy*dt; p.vx *= (1 - 2*dt); p.vy *= (1 - 2*dt); }
      else p.r += 640*dt;
      if (p.glow) ParticlesF.trail(p.x, p.y, p.color);
    }
    for (let i = texts.length - 1; i >= 0; i--) { texts[i].t -= dt; texts[i].y -= 40*dt; if (texts[i].t <= 0) texts.splice(i,1); }
  }

  function draw(c) {
    for (const p of parts) {
      const a = Math.max(0, p.life / p.max);
      c.globalAlpha = a;
      if (p.ring) {
        c.strokeStyle = p.color; c.lineWidth = 4*a;
        c.shadowColor = p.color; c.shadowBlur = 16;
        c.beginPath(); c.arc(p.x, p.y, p.r, 0, 6.283); c.stroke();
        c.shadowBlur = 0;
      } else if (p.bolt) {
        c.strokeStyle = p.color; c.lineWidth = 2.5*a; c.shadowColor = p.color; c.shadowBlur = 14;
        const tx = p.x - p.vx*0.03, ty = p.y - p.vy*0.03;
        c.beginPath(); c.moveTo(p.x, p.y); c.lineTo(tx, ty); c.stroke(); c.shadowBlur = 0;
      } else {
        c.fillStyle = p.color; c.shadowColor = p.color; c.shadowBlur = p.glow ? 12 : 0;
        c.beginPath(); c.arc(p.x, p.y, p.r * a, 0, 6.283); c.fill(); c.shadowBlur = 0;
      }
    }
    c.globalAlpha = 1;
  }

  function drawTexts(c) {
    for (const tx of texts) {
      c.globalAlpha = Math.max(0, tx.t);
      c.font = "bold 15px system-ui"; c.fillStyle = tx.color; c.textAlign = "center";
      c.shadowColor = "#000"; c.shadowBlur = 6;
      c.fillText(tx.str, tx.x, tx.y); c.shadowBlur = 0;
    }
    c.globalAlpha = 1;
  }
  return { spawn, blast, bolt, trail, text, ring, update, draw, drawTexts };
})();
// helper alias used above
const ParticlesF = Particles;

/* ------------------------------------------------------------------ [AGENT 6: GRAVITON] Physics — black holes & fields       */
const G = (() => {
  const worldR = 1400;
  const blackHoles = [];
  const beams = [];
  const S = A.S;
  const player = { x:0, y:0 };
  const dist = (a, b) => Math.hypot(a.x - b.x, a.y - b.y);

  function gravityPull(e, dt) {
    for (const bh of blackHoles) {
      const dx = bh.x - e.x, dy = bh.y - e.y;
      const d = Math.hypot(dx, dy) || 1;
      if (d < bh.maxR) {
        const f = bh.pull * (1 - d / bh.maxR) * dt;
        e.vx += (dx/d) * f; e.vy += (dy/d) * f;
        // damage inside
        if (d < bh.r) { e.hp -= 90*dt; e.hit = 0.1; if (e.hp <= 0) Entropy.kill(e, true, true); }
        Particles.trail(e.x, e.y, "#6ff");
      }
    }
  }

  function update(dt) {
    for (let i = blackHoles.length - 1; i >= 0; i--) {
      const bh = blackHoles[i];
      bh.t += dt;
      bh.r = Math.min(bh.maxR * 0.5, 14 + bh.t * 40);
      // pull player slightly
      const dx = bh.x - player.x, dy = bh.y - player.y, d = Math.hypot(dx,dy)||1;
      if (d < bh.maxR) { const f = 220*(1-d/bh.maxR)*dt; player.vx += dx/d*f; player.vy += dy/d*f; }
      if (bh.t >= bh.life) {
        blackHoles.splice(i, 1);
        // SUPERNOVA: enormous blast
        const bx = bh.x, by = bh.y;
        Particles.blast(bx, by, 34, "#6ff", 90, 620);
        Particles.ring(bx, by, 10, "#9dffd0");
        A.S.shake = Math.max(A.S.shake, 18);
        Synth.boom(160);
        for (const e of Entropy.enemies) if (dist(e, bh) < 300) Entropy.kill(e, true, true);
        if (Player.alive && dist(Player, bh) < 120) Player.damage(30, null);
      }
    }
    for (let i = beams.length - 1; i >= 0; i--) { beams[i].t -= dt; if (beams[i].t <= 0) beams.splice(i, 1); }
  }

  function drawBlackHoles(c) {
    for (const bh of blackHoles) {
      // accretion ring
      const a = 0.5 + 0.5 * Math.sin(S.time * 10 + bh.t * 4);
      c.strokeStyle = "#6ff"; c.globalAlpha = 0.7; c.lineWidth = 3;
      c.shadowColor = "#6ff"; c.shadowBlur = 22;
      c.beginPath(); c.arc(bh.x, bh.y, bh.r * 0.7, 0, 6.283); c.stroke();
      c.strokeStyle = "#c8a6ff"; c.lineWidth = 2; c.globalAlpha = 0.4;
      c.beginPath(); c.arc(bh.x, bh.y, bh.r * 0.9 + a*6, 0, 6.283); c.stroke();
      c.shadowBlur = 0;
      // event horizon
      const g = c.createRadialGradient(bh.x, bh.y, 1, bh.x, bh.y, bh.r);
      g.addColorStop(0, "rgba(0,0,0,1)"); g.addColorStop(0.8, "rgba(0,10,30,0.9)"); g.addColorStop(1, "rgba(60,255,255,0.3)");
      c.fillStyle = g; c.globalAlpha = 1;
      c.beginPath(); c.arc(bh.x, bh.y, bh.r, 0, 6.283); c.fill();
    }
  }

  function drawBeams(c) {
    for (const b of beams) {
      c.globalAlpha = Math.max(0, b.t / 0.28);
      c.strokeStyle = b.color; c.lineWidth = b.w; c.shadowColor = b.color; c.shadowBlur = 26;
      c.beginPath(); c.moveTo(b.x1, b.y1); c.lineTo(b.x2, b.y2); c.stroke();
      c.lineWidth = b.w*0.4; c.strokeStyle = "#fff";
      c.beginPath(); c.moveTo(b.x1, b.y1); c.lineTo(b.x2, b.y2); c.stroke();
      c.shadowBlur = 0; c.globalAlpha = 1;
    }
  }

  function drawBounds(c) {
    c.strokeStyle = "rgba(160,120,255,0.18)"; c.lineWidth = 2;
    c.strokeRect(-worldR, -worldR, worldR*2, worldR*2);
  }
  return { worldR, blackHoles, beams, dist, gravityPull, update, drawBlackHoles, drawBeams, drawBounds, player };
})();

/* ------------------------------------------------------------------ [AGENT 7: TEMPORALIST] bullet-time, rewind ghosts, time warps
   Renders time-rift ghosts of enemies when the rift is active, and
   owns the global timeScale.                                        */
const Temporalist = (() => {
  // enemy ghost history sampled while rift is active
  let history = [];
  const S = A.S;

  function sample() {
    if (S.targetTimeScale < 0.5) {
      history.push(Entropy.enemies.map(e => ({ x: e.x, y: e.y, color: e.color, r: e.r })));
      if (history.length > 40) history.shift();
    }
  }

  function draw(c) {
    if (S.targetTimeScale >= 0.5) { history.length = 0; return; }
    for (let i = 0; i < history.length; i++) {
      const frame = history[i];
      const a = (i / history.length) * 0.35;
      c.globalAlpha = a;
      for (const e of frame) {
        c.fillStyle = "#9dffd0";
        c.beginPath(); c.arc(e.x, e.y, e.r * 0.7, 0, 6.283); c.fill();
      }
    }
    c.globalAlpha = 1;
    // cyan time-flare
    const g = c.createRadialGradient(Player.x, Player.y, 20, Player.x, Player.y, 260);
    g.addColorStop(0, "rgba(60,255,200,0.14)"); g.addColorStop(1, "rgba(60,255,200,0)");
    c.fillStyle = g; c.fillRect(Player.x-280, Player.y-280, 560, 560);
  }

  return { sample, draw };
})();

/* ------------------------------------------------------------------ [AGENT 9: FRACTALIST] recursion & duplication hooks
   Mostly folded into Abilities.fx.echoes; this agent owns the bolt
   recursion (overdrive cascade) and echo firing.                 */
const Fractalist = (() => {
  const overBoltAcc = { t: 0 };
  function update(dt) {
    if (Player.overdrive > 0) {
      overBoltAcc.t -= dt;
      if (overBoltAcc.t <= 0) {
        overBoltAcc.t = 0.08;
        // cascade of starfire in all directions
        for (let k = 0; k < 6; k++) {
          const a = Math.random() * 6.28;
          const ex = Player.x + Math.cos(a) * 60, ey = Player.y + Math.sin(a) * 60;
          Bolts.fire(ex, ey, a, "#ffd2ff", { dmg: 40, speed: 800 });
        }
      }
    }
    // echo firing
    for (const ec of Abilities.fx.echoes) {
      if (Player.firing) Bolts.fire(ec.x, ec.y, Player.aimAngle, "#c8a6ff", { dmg: 18, speed: 640 });
    }
  }
  return { update };
})();

/* ------------------------------------------------------------------ [GRAVITON/PARTICLEFORGE] Bolts — real damaging projectiles */
const Bolts = (() => {
  const bolts = [];
  const S = A.S;
  const BOLT_DMG = 26;

  function fire(x, y, ang, color, opts) {
    opts = opts || {};
    bolts.push({
      x, y,
      vx: Math.cos(ang) * (opts.speed || 660),
      vy: Math.sin(ang) * (opts.speed || 660),
      r: 4, dmg: opts.dmg || BOLT_DMG,
      life: opts.life || 0.9, color: color || "#8ff",
      pierce: opts.pierce || 0, dead: false,
    });
  }

  function update(dt) {
    for (const b of bolts) {
      if (b.dead) continue;
      b.x += b.vx * dt; b.y += b.vy * dt;
      b.life -= dt;
      Particles.trail(b.x, b.y, b.color);
      if (b.life <= 0) { b.dead = true; continue; }
      // world bounds
      if (Math.abs(b.x) > G.worldR || Math.abs(b.y) > G.worldR) { b.dead = true; continue; }
      // hit enemies
      for (const e of Entropy.enemies) {
        if (e.dead) continue;
        if (G.dist(b, e) < b.r + e.r) {
          e.hp -= b.dmg; e.hit = 0.1;
          Particles.blast(b.x, b.y, 8, b.color, 6, 160);
          if (e.hp <= 0) Entropy.kill(e, true, true);
          if (b.pierce > 0) { b.pierce--; continue; }
          b.dead = true; break;
        }
      }
    }
    for (let i = bolts.length - 1; i >= 0; i--) if (bolts[i].dead) bolts.splice(i, 1);
  }

  function draw(c) {
    for (const b of bolts) {
      if (b.dead) continue;
      c.strokeStyle = b.color; c.lineWidth = 3; c.shadowColor = b.color; c.shadowBlur = 14;
      const tx = b.x - b.vx * 0.03, ty = b.y - b.vy * 0.03;
      c.beginPath(); c.moveTo(b.x, b.y); c.lineTo(tx, ty); c.stroke();
      c.lineWidth = 1.5; c.strokeStyle = "#fff";
      c.beginPath(); c.moveTo(b.x, b.y); c.lineTo(tx, ty); c.stroke();
      c.shadowBlur = 0;
    }
  }
  return { fire, update, draw };
})();

/* ------------------------------------------------------------------ [AGENT 8-merged + GRAVITON] The Player
   Movement, vitals, firing, phase, overdrive, combos.           */
const Player = {
  x: 0, y: 0, vx: 0, vy: 0, r: 15,
  maxHp: 120, hp: 120,
  maxReality: 100, reality: 100,
  aimX: 0, aimY: 0, aimAngle: 0,
  firing: false, alive: true,
  phase: 0, overdrive: 0,
  fireAcc: 0, score: 0, combo: 0, comboT: 0, kills: 0,
  fireRate: 0.16,

  comboMult() { return 1 + Math.min(9, Math.floor(this.combo / 8)) * 0.5; },

  addScore(p) { this.score += Math.round(p); this.combo++; this.comboT = 2.5; this.kills++; },

  heal(n) { this.hp = Math.min(this.maxHp, this.hp + n); },

  damage(dmg, src) {
    if (!this.alive || this.phase > 0 || A.S.targetTimeScale < 0.3) return false;
    this.hp -= dmg;
    A.S.shake = Math.max(A.S.shake, 8);
    Synth.ouch();
    Particles.blast(this.x, this.y, 16, "#ff4d6d", 14, 240);
    if (this.hp <= 0) { this.alive = false; A.S.playing = false; UI.die(); }
    return true;
  },

  update(dt, t) {
    if (!this.alive) return;
    this.firing = Input.mouse.down;
    // movement
    const k = Input.keys;
    let mx = (k["d"]?1:0) - (k["a"]?1:0);
    let my = (k["s"]?1:0) - (k["w"]?1:0);
    const len = Math.hypot(mx, my) || 1;
    const speed = this.phase > 0 ? 360 : 250;
    this.vx += (mx/len) * speed * 4 * dt;
    this.vy += (my/len) * speed * 4 * dt;
    this.vx *= (1 - 10*dt); this.vy *= (1 - 10*dt);
    // overdrive magnet
    if (this.overdrive > 0) { this.overdrive -= dt; }
    this.phase = Math.max(0, this.phase - dt);
    this.x += this.vx * dt; this.y += this.vy * dt;
    // clamp to world
    const R = G.worldR - 20;
    this.x = Math.max(-R, Math.min(R, this.x));
    this.y = Math.max(-R, Math.min(R, this.y));
    // aim
    const worldAim = Input.toWorld(Input.mouse.x, Input.mouse.y);
    this.aimX = Input.mouse.x; this.aimY = Input.mouse.y;
    this.aimAngle = Math.atan2(worldAim.y - this.y, worldAim.x - this.x);
    // reality regen
    this.reality = Math.min(this.maxReality, this.reality + dt * 6);
    // combo decay
    if (this.comboT > 0) { this.comboT -= dt; if (this.comboT <= 0) this.combo = 0; }
    // firing
    if (this.firing) {
      this.fireAcc -= dt;
      const rate = this.fireRate / (this.overdrive > 0 ? 6 : (Abilities.fx.riftActive ? 2.5 : (Abilities.fx.echoes.length ? 1.6 : 1)));
      if (this.fireAcc <= 0) {
        this.fireAcc = rate;
        const boltColor = this.overdrive > 0 ? "#ffd2ff" : "#8ff";
        Bolts.fire(this.x + Math.cos(this.aimAngle)*20, this.y + Math.sin(this.aimAngle)*20, this.aimAngle, boltColor,
          { dmg: this.overdrive > 0 ? 60 : 26, speed: this.overdrive > 0 ? 900 : 660, pierce: this.overdrive > 0 ? 3 : 0 });
        Synth.shoot(this.overdrive > 0);
      }
    }
    // trail
    Particles.trail(this.x, this.y, this.phase > 0 ? "#ffd2ff" : "#8ff");
    // keep graviton player ref
    G.player.x = this.x; G.player.y = this.y;
  },

  render(c) {
    c.save();
    c.translate(this.x, this.y);
    c.rotate(this.aimAngle);
    // aura
    const aura = c.createRadialGradient(0,0,4,0,0,40);
    aura.addColorStop(0, this.overdrive>0 ? "rgba(255,255,255,.7)" : "rgba(140,160,255,.35)");
    aura.addColorStop(1, "rgba(140,160,255,0)");
    c.fillStyle = aura; c.beginPath(); c.arc(0,0,40,0,6.283); c.fill();
    // body
    c.shadowColor = "#8ff"; c.shadowBlur = 18;
    c.fillStyle = this.phase > 0 ? "rgba(255,210,255,0.5)" : "#cfd8ff";
    c.beginPath();
    c.moveTo(24, 0); c.lineTo(-14, -13); c.lineTo(-8, 0); c.lineTo(-14, 13); c.closePath();
    c.fill();
    c.shadowBlur = 0;
    // core
    c.fillStyle = "#fff";
    c.beginPath(); c.arc(4, 0, 5, 0, 6.283); c.fill();
    c.restore();
  }
};

/* ------------------------------------------------------------------ [AGENT 10: SYNTHESIZER] procedural WebAudio score & sfx   */
const Synth = (() => {
  let ctx = null, master = null, musicOn = false, seqTimer = null;
  function ensure() {
    if (ctx) return;
    const AC = window.AudioContext || window.webkitAudioContext;
    if (!AC) return;
    ctx = new AC();
    master = ctx.createGain(); master.gain.value = 0.7; master.connect(ctx.destination);
    startMusic();
  }
  function env(freq, dur, type, vol, slideTo, delay) {
    if (!ctx) return;
    const t = ctx.currentTime + (delay||0);
    const o = ctx.createOscillator(), g = ctx.createGain();
    o.type = type; o.frequency.setValueAtTime(freq, t);
    if (slideTo) o.frequency.exponentialRampToValueAtTime(slideTo, t + dur);
    g.gain.setValueAtTime(0.0001, t);
    g.gain.exponentialRampToValueAtTime(vol, t + 0.01);
    g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    o.connect(g); g.connect(master); o.start(t); o.stop(t + dur + 0.02);
  }
  function noise(dur, vol, hp, delay) {
    if (!ctx) return;
    const t = ctx.currentTime + (delay||0);
    const len = Math.floor(ctx.sampleRate * dur);
    const buf = ctx.createBuffer(1, len, ctx.sampleRate);
    const d = buf.getChannelData(0);
    for (let i=0;i<len;i++) d[i] = Math.random()*2-1;
    const src = ctx.createBufferSource(); src.buffer = buf;
    const f = ctx.createBiquadFilter(); f.type = "highpass"; f.frequency.value = hp;
    const g = ctx.createGain();
    g.gain.setValueAtTime(vol, t);
    g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    src.connect(f); f.connect(g); g.connect(master); src.start(t); src.stop(t+dur);
  }
  // ---- SFX ----
  const shoot = (big) => env(big ? 900 : 620, 0.08, "square", 0.05, big?200:380);
  const hit = (big) => { if(big) noise(0.3,0.5,120); else noise(0.12,0.25,400); env(big?90:160, 0.2, "sawtooth", 0.2, 40); };
  const ouch = () => { env(300,0.2,"sawtooth",0.3,80); noise(0.2,0.3,200); };
  const bell = () => [523,659,784,1046].forEach((f,i)=>env(f,0.6,"sine",0.18, null, i*0.06));
  const boom = (f) => { env(f||120, 0.7, "sine", 0.5, 30); noise(0.5,0.5,60); };
  const singularity = () => { env(80,1.2,"sine",0.4,24); noise(0.8,0.3,80); };
  const echo = () => [880,1320].forEach((f,i)=>env(f,0.4,"triangle",0.2,null,i*0.09));
  const lash = () => { env(1400,0.15,"sawtooth",0.3,200); noise(0.15,0.3,1000); };
  const phase = () => env(500,0.3,"sine",0.25,1000);
  const genesis = () => { env(60,1.8,"sawtooth",0.5,30); noise(1.2,0.5,80); [220,330,440,660].forEach((f,i)=>env(f,1.4,"sine",0.15,null,i*0.15)); };

  // ---- Music ----
  const SCALE = [0,2,4,7,9]; const ROOT = 55;
  function note(step, dur, oct) {
    const idx = step % SCALE.length, octOff = Math.floor(step/SCALE.length);
    const f = ROOT * Math.pow(2, (SCALE[idx] + 12*octOff + (oct||0)*12)/12);
    env(f, dur*0.9, "triangle", 0.12);
    env(f*0.5, dur*0.9, "sine", 0.14);
  }
  function drum(step) {
    if (step % 2 === 0) { noise(0.12,0.16,400); env(110,0.2,"sine",0.3,40); }
    if (step % 4 === 2) noise(0.05,0.06,6000);
  }
  function startMusic() {
    if (musicOn || !ctx) return;
    musicOn = true;
    let step = 0;
    seqTimer = setInterval(() => {
      if (!A.S.playing) return;
      const beat = A.S.targetTimeScale < 0.5 ? 0.42 : 0.16; // tempo warps with time!
      note(step * 3, beat * 2, 2);
      if (step % 2 === 0) note(step * 5 + 1, beat*2, 1);
      drum(step);
      if (A.S.targetTimeScale < 0.5) bellDrip();
      step++;
    }, 150);
  }
  function bellDrip() { if (Math.random() < 0.3) env(1500 + Math.random()*800, 0.3, "sine", 0.05, null, Math.random()*0.2); }
  function start() { ensure(); if (ctx && ctx.state === "suspended") ctx.resume(); }

  return { ensure, start, shoot, hit, ouch, bell, boom, singularity, echo, lash, phase, genesis };
})();

/* ------------------------------------------------------------------ [AGENT 11: UMBRA] post-fx, vignette, chromatic, shake     */
const Umbra = (() => {
  let grainCanvas = null;
  function ensureGrain(w, h) {
    if (grainCanvas && grainCanvas.width === w && grainCanvas.height === h) return;
    grainCanvas = document.createElement("canvas");
    grainCanvas.width = w; grainCanvas.height = h;
    const g = grainCanvas.getContext("2d");
    const img = g.createImageData(w, h);
    for (let i=0;i<img.data.length;i+=4){ const v=Math.random()*255|0; img.data[i]=img.data[i+1]=img.data[i+2]=v; img.data[i+3]=40; }
    g.putImageData(img,0,0);
  }
  function post(c, w, h) {
    const S = A.S;
    // vignette
    const v = c.createRadialGradient(w/2,h/2,Math.min(w,h)*0.35, w/2,h/2,Math.max(w,h)*0.72);
    v.addColorStop(0,"rgba(0,0,0,0)"); v.addColorStop(1,`rgba(0,0,0,${0.35+S.shake*0.01})`);
    c.fillStyle = v; c.fillRect(0,0,w,h);
    // chromatic aberration edges
    if (S.flash > 0.02) {
      c.globalCompositeOperation = "lighter";
      c.globalAlpha = S.flash;
      c.fillStyle = "#fff"; c.fillRect(0,0,w,h);
      c.globalAlpha = 1; c.globalCompositeOperation = "source-over";
    }
    // grain
    ensureGrain(Math.floor(w/4), Math.floor(h/4));
    c.globalAlpha = 0.06;
    c.drawImage(grainCanvas, 0, 0, w, h);
    c.globalAlpha = 1;
  }
  return { post };
})();

/* ------------------------------------------------------------------ [AGENT 12: HUDMIND] interface, ability deck, vitals        */
const UI = (() => {
  const hud = document.getElementById("hud");
  const menu = document.getElementById("menu");
  const dead = document.getElementById("dead");
  let announceT = null;
  let centerEl = null;

  function buildHUD() {
    hud.innerHTML = `
      <div class="hud-top">
        <div class="vitals">
          <div class="name">THE UNMADE</div>
          <div class="bar hp"><i></i></div>
          <div class="bar reality"><i></i></div>
        </div>
        <div class="hud-right">
          <div class="wave">WAVE <span id="wv">0</span></div>
          <div class="score" id="sc">0</div>
          <div class="combo" id="cb"></div>
        </div>
      </div>
      <div class="abilities"></div>
      <div class="vignette"></div>
      <div class="center-text" id="ct"></div>`;
    const abWrap = hud.querySelector(".abilities");
    abWrap.innerHTML = Abilities.defs.map((d,i) =>
      `<div class="ab ${d.ult?'ult':''}" data-i="${i}">
         <span class="k">${d.key.toUpperCase()}</span>
         <span class="ico">${d.ico}</span>
         <span class="nm">${d.name}</span>
         <div class="cd" style="display:none"></div>
       </div>`).join("");
  }

  function center(str) {
    const el = hud.querySelector("#ct");
    el.textContent = str;
    el.classList.remove("show"); void el.offsetWidth; el.classList.add("show");
  }

  function announce(name) { center(name); }

  function die() {
    const stats = dead.querySelector(".dead-stats");
    stats.innerHTML = `<div><b>${Player.score.toLocaleString()}</b>REALITY SEVERED</div>
      <div><b>${Entropy.wave}</b>WAVES SURVIVED</div>
      <div><b>${Player.kills}</b>SHARDS UNMADE</div>
      <div><b>${Math.floor(A.S.gameTime + (Entropy.wave)* 8)}s</b>SECONDS BORROWED</div>`;
    dead.classList.remove("hidden");
    Synth.boom(70); Synth.ouch();
  }

  function tickHUD() {
    const hp = hud.querySelector(".hp i");
    const re = hud.querySelector(".reality i");
    const sc = hud.querySelector("#sc");
    const cb = hud.querySelector("#cb");
    const wv = hud.querySelector("#wv");
    if (hp) { hp.style.width = (Player.hp/Player.maxHp*100)+"%"; }
    if (re) { re.style.width = (Player.reality/Player.maxReality*100)+"%"; }
    if (sc) sc.textContent = Player.score.toLocaleString();
    if (cb) cb.textContent = Player.combo > 1 ? `COMBO ×${Player.comboMult().toFixed(1)}  (${Player.combo})` : "";
    if (wv) wv.textContent = Entropy.wave;
    const abEls = hud.querySelectorAll(".ab");
    abEls.forEach((el, i) => {
      const st = Abilities.state[i];
      const cd = el.querySelector(".cd");
      if (st.cd > 0) { cd.style.display = "flex"; cd.textContent = Math.ceil(st.cd); }
      else { cd.style.display = "none"; }
      el.classList.toggle("active", st.dur > 0 || (i===6 && Player.overdrive>0));
    });
  }

  function showMenu() { menu.classList.remove("hidden"); }
  function hideMenu() { menu.classList.add("hidden"); }
  function hideDead() { dead.classList.add("hidden"); }

  return { buildHUD, center, announce, die, tickHUD, showMenu, hideMenu, hideDead };
})();

/* ------------------------------------------------------------------ [AGENT 13: NARRATIVE] title, lore, awakening                */
const Narrative = (() => {
  const boot = document.getElementById("boot");
  const lines = [
    "before light, there was a wound.",
    "the wound dreamed, and the dream opened its eye.",
    "you are that eye. you are the crown of the unmaking.",
    "hold the shards of the dying star at bay.",
    "do not let the wound close.",
  ];
  let i = 0;
  function startBoot() {
    boot.textContent = "";
    const print = () => {
      if (i >= lines.length) { boot.classList.add("hide"); setTimeout(()=>{ boot.style.display="none"; }, 600); return; }
      boot.textContent = lines[i]; i++;
      setTimeout(print, 1400);
    };
    print();
  }
  return { startBoot };
})();

/* ------------------------------------------------------------------ [AGENT 14: BALANCER] tuning knobs & difficulty scaling      */
const Balance = (() => {
  function adjust() {
    // scale enemy HP slightly with wave
    const mult = 1 + (Entropy.wave - 1) * 0.06;
    // ENTROPY.SPECIES hp is base; we don't mutate live but difficulty via spawn budget already scales.
    return mult;
  }
  return { adjust };
})();

/* ------------------------------------------------------------------ [AGENT 15: POLISHER] game-feel & final integration           */
const Polish = (() => {
  let lastShot = 0;
  function update(dt) {
    const S = A.S;
    S.shake = Math.max(0, S.shake - dt * 40);
    S.flash = Math.max(0, S.flash - dt * 1.6);
  }
  return { update };
})();

/* ------------------------------------------------------------------ INPUT */
const Input = (() => {
  const keys = {};
  const mouse = { x: innerWidth/2, y: innerHeight/2, down: false };
  const canvas = document.getElementById("game");
  const cam = { x: 0, y: 0 };

  function toWorld(sx, sy) {
    return { x: cam.x + (sx - innerWidth/2), y: cam.y + (sy - innerHeight/2) };
  }
  function bind() {
    window.addEventListener("keydown", e => {
      const k = e.key.toLowerCase();
      if (k === " ") e.preventDefault();
      if (keys[k]) return;
      keys[k] = true;
      // abilities
      const idx = Abilities.defs.findIndex(d => d.key === e.key.toLowerCase());
      if (idx >= 0) Abilities.tryCast(idx);
    });
    window.addEventListener("keyup", e => { keys[e.key.toLowerCase()] = false; });
    window.addEventListener("mousemove", e => { mouse.x = e.clientX; mouse.y = e.clientY; });
    window.addEventListener("mousedown", e => { if (e.button === 0) mouse.down = true; });
    window.addEventListener("mouseup", e => { if (e.button === 0) mouse.down = false; });
    canvas.addEventListener("contextmenu", e => e.preventDefault());
  }
  return { keys, mouse, toWorld, bind, cam };
})();

/* ------------------------------------------------------------------ RENDER ORCHESTRATOR (part of ARCHITECT/RENDERER)            */
const canvas = document.getElementById("game");
const ctx = canvas.getContext("2d");
let W = 0, H = 0, DPR = 1;

function resize() {
  DPR = Math.min(window.devicePixelRatio || 1, 2);
  W = innerWidth; H = innerHeight;
  canvas.width = W * DPR; canvas.height = H * DPR;
  canvas.style.width = W + "px"; canvas.style.height = H + "px";
  ctx.setTransform(DPR, 0, 0, DPR, 0, 0);
}
window.addEventListener("resize", resize);

function render() {
  const S = A.S;
  ctx.save();
  // camera
  const shakeX = (Math.random()-0.5) * S.shake;
  const shakeY = (Math.random()-0.5) * S.shake;
  const camX = Player.x + shakeX, camY = Player.y + shakeY;
  Input.cam.x = camX; Input.cam.y = camY;
  ctx.translate(W/2 - camX, H/2 - camY);

  // background (screen-space, drawn in world coords origin)
  ctx.save();
  ctx.translate(camX - W/2, camY - H/2);
  Cosmos.draw(ctx, W, H, S.time);
  ctx.restore();

  G.drawBounds(ctx);

  // gravity field subtle grid / pulse lines at edges during danger
  if (Entropy.count() > 0) {
    ctx.strokeStyle = `rgba(255,80,120,${0.06 + Math.min(0.15, Entropy.count()*0.004)})`;
    ctx.lineWidth = 2;
    ctx.strokeRect(Player.x - W*0.6, Player.y - H*0.6, W*1.2, H*1.2);
  }

  // temporal ghosts behind
  Temporalist.draw(ctx);
  // beams behind entities
  G.drawBeams(ctx);
  // black holes behind enemies
  G.drawBlackHoles(ctx);

  // prism trail
  for (const p of Abilities.fx.prismTrail) {
    ctx.globalAlpha = p.t * 0.6;
    ctx.fillStyle = "#ffd2ff"; ctx.shadowColor = "#ffd2ff"; ctx.shadowBlur = 16;
    ctx.beginPath(); ctx.arc(p.x, p.y, 16, 0, 6.283); ctx.fill();
    ctx.shadowBlur = 0;
  }
  ctx.globalAlpha = 1;

  // enemies
  for (const e of Entropy.enemies) {
    const bob = Math.sin(S.time * 4 + e.seed) * 2;
    ctx.save(); ctx.translate(e.x, e.y + bob);
    if (e.hit > 0) { ctx.globalAlpha = 0.7; ctx.fillStyle = "#fff"; }
    else {
      const grad = ctx.createRadialGradient(-e.r*0.3,-e.r*0.3,2, 0,0,e.r);
      grad.addColorStop(0, "#fff"); grad.addColorStop(0.35, e.color); grad.addColorStop(1, "#1a0530");
      ctx.fillStyle = grad;
    }
    ctx.shadowColor = e.color; ctx.shadowBlur = e.hit > 0 ? 6 : 14;
    ctx.beginPath();
    if (e.type === "shard" || e.type === "wraith") {
      ctx.moveTo(e.r, 0); ctx.lineTo(-e.r*0.6, e.r*0.7); ctx.lineTo(-e.r*0.6, -e.r*0.7); ctx.closePath();
    } else if (e.type === "behemoth" || e.type === "sunmaw") {
      ctx.arc(0,0,e.r,0,6.283);
    } else {
      ctx.arc(0,0,e.r,0,6.283);
    }
    ctx.fill();
    // eye for living ones
    if (e.type !== "mine") {
      ctx.shadowBlur = 0; ctx.fillStyle = "#fff";
      const dx = Player.x - e.x, dy = Player.y - e.y, d = Math.hypot(dx,dy)||1;
      ctx.beginPath(); ctx.arc((dx/d)*e.r*0.35, (dy/d)*e.r*0.35, e.r*0.22, 0, 6.283); ctx.fill();
    }
    // hp bar for big
    if (e.type === "behemoth" || e.type === "sunmaw") {
      ctx.fillStyle = "rgba(0,0,0,.5)"; ctx.fillRect(-e.r, -e.r-10, e.r*2, 4);
      ctx.fillStyle = e.color; ctx.fillRect(-e.r, -e.r-10, e.r*2*(e.hp/e.maxHp), 4);
    }
    ctx.restore();
  }

  // echoes
  Abilities.drawEchoes(ctx);

  // player
  Player.render(ctx);

  // particles
  Particles.draw(ctx);
  G.drawBeams(ctx); // bright beams on top
  Bolts.draw(ctx);  // live projectiles above everything in-world

  ctx.restore();

  // screen-space particles/texts
  Particles.drawTexts(ctx);
  Umbra.post(ctx, W, H);

  A.S.time += A.S.gameDt; // (set below each frame)
}

/* ------------------------------------------------------------------ MAIN LOOP  (ARCHITECT)                                      */
function main(now) {
  const raw = A.tick(now);
  const dt = raw.dt * A.S.timeScale;
  A.S.gameDt = dt;
  A.S.gameTime += dt;
  if (A.S.playing) {
    // sub-systems ordered: temporal sample -> balancer -> entropy spawn -> behaviors -> particles -> abilities -> player
    Temporalist.sample();
    Balance.adjust();
    Entropy.update(dt, A.S.time);
    Player.update(dt, A.S.time);
    Entropy.behaviors(dt);
    Entropy.sweep();
    Bolts.update(dt);
    Abilities.update(dt, A.S.time);
    Fractalist.update(dt);
    G.update(dt);
    Particles.update(dt, A.S.time);
    Polish.update(dt);
    UI.tickHUD();
  } else {
    Polish.update(dt);
  }
  render();
  requestAnimationFrame(main);
}

/* ------------------------------------------------------------------ BOOT & EVENTS (NARRATIVE + ARCHITECT)                       */
window.addEventListener("load", () => {
  resize();
  Input.bind();
  UI.buildHUD();
  Narrative.startBoot();
  requestAnimationFrame(main);
});

document.getElementById("begin").addEventListener("click", () => {
  Synth.start();
  startGame();
});
document.getElementById("again").addEventListener("click", () => {
  Synth.start();
  startGame();
});

function startGame() {
  Entropy.clear();
  Entropy.wave = 0;
  G.blackHoles.length = 0;
  G.beams.length = 0;
  Abilities.fx.echoes = [];
  Abilities.fx.prismTrail = [];
  Abilities.fx.inversion = 0;
  Abilities.fx.riftActive = false;
  for (const st of Abilities.state) { st.cd = 0; st.dur = 0; }
  Player.x = 0; Player.y = 0; Player.vx = 0; Player.vy = 0;
  Player.hp = Player.maxHp; Player.reality = Player.maxReality;
  Player.alive = true; Player.phase = 0; Player.overdrive = 0;
  Player.score = 0; Player.combo = 0; Player.kills = 0;
  A.S.timeScale = 1; A.S.targetTimeScale = 1;
  A.S.shake = 0; A.S.flash = 0; A.S.gameTime = 0;
  A.S.playing = true; A.S.dead = false;
  Entropy.newWave();
  UI.hideMenu(); UI.hideDead();
}
