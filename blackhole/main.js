/* ---------------------------------------------------------------------------
 * Singularity Vault — black hole customizer + endless-dimension journey
 * Babylon.js / WebGL2. Main render is a single full-screen ray-marched pass.
 * ------------------------------------------------------------------------- */
(function () {
  const { clamp, lerp, hexToRgb, mulberry32 } = UTILS;

  const c3cache = new Map();
  function C3(hex) {
    let c = c3cache.get(hex);
    if (!c) { c = BABYLON.Color3.FromHexString(hex); c3cache.set(hex, c); }
    return c;
  }
  function hexLerp(a, b, t) {
    const A = hexToRgb(a), B = hexToRgb(b);
    const r = Math.round(lerp(A.r, B.r, t) * 255), g = Math.round(lerp(A.g, B.g, t) * 255), bl = Math.round(lerp(A.b, B.b, t) * 255);
    return "#" + ((1 << 24) | (r << 16) | (g << 8) | bl).toString(16).slice(1);
  }

  const DIM_NAMES = ["Event Horizon", "The Bloodstream", "The Hollow House", "The Kaleidoscope"];
  const DIM_SUBS = [
    "You orbit a wound in spacetime.",
    "You are inside something alive.",
    "A house in the middle of nowhere. The door is open.",
    "Reality stops taking itself seriously."
  ];
  const NEXT_DIM = [1, 2, 3, 0];

  const DEFAULTS = {
    rs: 1.0, spin: 0.9, lens: 1.0, diskInner: 2.9, diskOuter: 12, temp: 1.15, bright: 2.2, beaming: 0.9,
    tiltX: 14, tiltZ: 3, swirl: 1.0, ring: 1.2, star: 1.0, nebula: 0.55, band: 1.0,
    tintA: "#241a3f", tintB: "#c8722a", ringCol: "#ffe9c4", diskTint: "#ffffff", nebScale: 1.0,
    exposure: 1.12, contrast: 1.06, bloom: 0.75, ca: 2.5, grain: 10, vignette: 0.5,
    fractK: 1.9, fractCS: 0.9, fractGlow: 1.0
  };
  const PRESET_KEYS = Object.keys(DEFAULTS);

  let P = Object.assign({}, DEFAULTS);
  const TUNE = {
    fov: 58, baseFov: 58, steps: 250, dtScale: 0.42, resScale: 1.0,
    orbitSpeed: 0, sensitivity: 1.0, journeySpeed: 1.0, audio: false, volume: 0.45,
    camMode: "orbit", flySpeed: 16
  };

  /* ------------------------------------------------- spacetime control --- */
  const holePos = new BABYLON.Vector3(0, 0, 0);       // where the hole lives (world units)
  const orbitCenter = new BABYLON.Vector3(0, 0, 0);   // what the orbit camera looks at
  const look = { yaw: 0, pitch: 0, tYaw: 0, tPitch: 0 };
  const free = { pos: new BABYLON.Vector3(16, 5, -16), yaw: 0.75, pitch: -0.16 };
  const keys = new Set();

  /* ---------------------------------------------------------- summons ---- */
  const SUM = {
    star: { on: false, size: 18, color: "#fff2d8", mesh: null, light: null },
    planets: [],
    planetScratch: { type: "gas", orbit: 24, size: 2.4, speed: 0.12 },
    belt: { on: false, radius: 30, node: null, mesh: null },
    comet: { on: false, speed: 0.32, mesh: null, ps: null, ang: 2.2 },
    twinWorld: new BABYLON.Vector3(20, 0, 0)
  };
  const twin = { on: false, dist: 18, speed: 0.22, rs: 0.7, ang: 1.2 };

  function diskBasis(outN, outT1, outT2) {
    const tx = P.tiltX * Math.PI / 180, tz = P.tiltZ * Math.PI / 180;
    outN.set(Math.sin(tz) * Math.cos(tx), Math.cos(tz) * Math.cos(tx), Math.sin(tx));
    const ref = Math.abs(outN.z) < 0.94 ? BABYLON.Vector3.Forward() : BABYLON.Vector3.Right();
    BABYLON.Vector3.CrossToRef(outN, ref, outT1);
    outT1.normalize();
    BABYLON.Vector3.CrossToRef(outN, outT1, outT2);
  }
  const _dN = new BABYLON.Vector3(), _dT1 = new BABYLON.Vector3(), _dT2 = new BABYLON.Vector3();

  function paintPlanetTexture(type, seed) {
    const rng = mulberry32(seed);
    const dtex = new BABYLON.DynamicTexture("ptex" + seed, { width: 512, height: 256 }, scene, true);
    const c = dtex.getContext();
    if (type === "gas") {
      const pals = [["#c8a06a", "#8a6238", "#e8d0a0"], ["#7a9ac8", "#4a628a", "#b8d0e8"], ["#b8787a", "#8a4a4a", "#e8b8a0"]];
      const pal = pals[(seed % pals.length)];
      for (let y = 0; y < 256; y++) {
        const band = Math.sin(y * 0.11 + seed) * 0.5 + Math.sin(y * 0.031 + 1.7 + seed) * 0.5;
        const t = clamp(0.5 + band * 0.55 + (rng() - 0.5) * 0.08, 0, 1);
        c.fillStyle = hexLerp(t > 0.5 ? pal[0] : pal[1], pal[2], Math.abs(band) * 0.7);
        c.fillRect(0, y, 512, 1);
      }
      // a great spot storm
      c.fillStyle = "rgba(255,210,170,0.5)";
      c.beginPath(); c.ellipse(300 + (seed % 90), 130 + (seed % 40), 44, 20, 0.2, 0, 6.29); c.fill();
      c.fillStyle = "rgba(150,70,40,0.65)";
      c.beginPath(); c.ellipse(300 + (seed % 90), 130 + (seed % 40), 30, 13, 0.2, 0, 6.29); c.fill();
    } else {
      c.fillStyle = "#6a5646"; c.fillRect(0, 0, 512, 256);
      for (let i = 0; i < 900; i++) {
        const lum = 0.35 + rng() * 0.5;
        const r = 2 + rng() * 14;
        c.fillStyle = `rgba(${90 + lum * 90 | 0},${70 + lum * 70 | 0},${55 + lum * 55 | 0},0.55)`;
        c.beginPath(); c.arc(rng() * 512, rng() * 256, r, 0, 6.29); c.fill();
      }
      // craters
      for (let i = 0; i < 46; i++) {
        const x = rng() * 512, y = 30 + rng() * 196, r = 2 + rng() * 9;
        c.fillStyle = "rgba(20,14,10,0.5)";
        c.beginPath(); c.arc(x, y, r, 0, 6.29); c.fill();
        c.strokeStyle = "rgba(200,180,150,0.35)"; c.lineWidth = 1.5;
        c.beginPath(); c.arc(x, y, r + 1.5, 0, 6.29); c.stroke();
      }
      c.fillStyle = "rgba(235,240,250,0.9)";
      c.fillRect(0, 0, 512, 16); c.fillRect(0, 240, 512, 16);
    }
    dtex.update();
    return dtex;
  }

  function ensureStar() {
    const s = SUM.star;
    if (s.mesh) return;
    s.mesh = BABYLON.MeshBuilder.CreateSphere("star", { diameter: 2, segments: 16 }, scene);
    const m = new BABYLON.StandardMaterial("starMat", scene);
    m.disableLighting = true;
    s.mesh.material = m;
    s.mesh.isPickable = false;
    s.light = new BABYLON.DirectionalLight("starLight", new BABYLON.Vector3(0.3, -0.4, 0.8), scene);
    s.light.intensity = 1.15;
  }
  function addPlanet() {
    const scr = SUM.planetScratch;
    if (SUM.planets.length >= 4) { setObjective("Max 4 planets — remove one first."); return; }
    ensureStar();
    SUM.star.on = true;
    if (handles.starOn) handles.starOn.set(true);
    const idx = SUM.planets.length;
    const mesh = BABYLON.MeshBuilder.CreateSphere("planet" + idx, { diameter: 2, segments: 22 }, scene);
    const mat = new BABYLON.StandardMaterial("planetMat" + idx, scene);
    mat.diffuseTexture = paintPlanetTexture(scr.type, idx * 13 + 5);
    mat.specularColor = new BABYLON.Color3(0.03, 0.03, 0.03);
    mesh.material = mat;
    mesh.isPickable = false;
    mesh.scaling.setAll(scr.size);
    const pl = { mesh, orbit: scr.orbit, size: scr.size, speed: scr.speed, ang: Math.random() * 6.28, plane: Math.random() < 0.75 ? 1 : 0 };
    SUM.planets.push(pl);
    if (handles.planetCount) handles.planetCount.set(SUM.planets.length + " orbiting");
  }
  function removePlanet() {
    const pl = SUM.planets.pop();
    if (pl) pl.mesh.dispose();
    if (handles.planetCount) handles.planetCount.set(SUM.planets.length ? SUM.planets.length + " orbiting" : "—");
  }
  function buildBelt() {
    if (SUM.belt.node) { SUM.belt.node.dispose(); SUM.belt.node = null; }
    const node = new BABYLON.TransformNode("belt", scene);
    const src = BABYLON.MeshBuilder.CreateSphere("beltRock", { diameter: 1, segments: 4 }, scene);
    const rm = new BABYLON.StandardMaterial("beltMat", scene);
    rm.diffuseColor = new BABYLON.Color3(0.36, 0.33, 0.30);
    rm.specularColor = new BABYLON.Color3(0.02, 0.02, 0.02);
    src.material = rm; src.parent = node; src.isPickable = false;
    src.alwaysSelectAsActiveMesh = true;
    src.useVertexColors = false;
    const N = 340;
    const data = new Float32Array(N * 16);
    const rng = mulberry32(4242);
    const q = new BABYLON.Quaternion();
    for (let i = 0; i < N; i++) {
      const a = rng() * Math.PI * 2;
      const r = SUM.belt.radius * (0.82 + rng() * 0.85);
      const s = 0.12 + rng() * 0.6;
      BABYLON.Quaternion.RotationYawPitchRollToRef(rng() * 6.28, rng() * 6.28, 0, q);
      BABYLON.Matrix.ComposeToRef(
        new BABYLON.Vector3(s, s * (0.7 + rng() * 0.6), s), q,
        new BABYLON.Vector3(Math.cos(a) * r, (rng() - 0.5) * 2.2, Math.sin(a) * r), _beltM);
      _beltM.copyToArray(data, i * 16);
    }
    src.thinInstanceSetBuffer("matrix", data, 16, true);
    SUM.belt.node = node; SUM.belt.mesh = src;
  }
  const _beltM = new BABYLON.Matrix();
  let _sprayTex = null;
  function sprayTex() {
    if (_sprayTex) return _sprayTex;
    const t = new BABYLON.DynamicTexture("spray", 64, scene, true);
    const c = t.getContext();
    const g = c.createRadialGradient(32, 32, 1, 32, 32, 32);
    g.addColorStop(0, "rgba(255,255,255,1)");
    g.addColorStop(0.6, "rgba(200,225,255,0.5)");
    g.addColorStop(1, "rgba(180,215,255,0)");
    c.fillStyle = g; c.fillRect(0, 0, 64, 64); t.update();
    t.hasAlpha = true;
    _sprayTex = t;
    return t;
  }
  function ensureComet() {
    const s = SUM.comet;
    if (s.mesh) return;
    s.mesh = BABYLON.MeshBuilder.CreateSphere("comet", { diameter: 1.1, segments: 10 }, scene);
    const m = new BABYLON.StandardMaterial("cometMat", scene);
    m.emissiveColor = new BABYLON.Color3(0.55, 0.7, 0.85);
    m.diffuseColor = new BABYLON.Color3(0.2, 0.26, 0.33);
    s.mesh.material = m; s.mesh.isPickable = false;
    const ps = new BABYLON.ParticleSystem("cometTail", 900, scene);
    ps.particleTexture = sprayTex();
    ps.emitter = s.mesh;
    ps.minEmitBox = new BABYLON.Vector3(0, 0, 0);
    ps.maxEmitBox = new BABYLON.Vector3(0, 0, 0);
    ps.direction1 = new BABYLON.Vector3(-1, 0, 0);
    ps.direction2 = new BABYLON.Vector3(-1, 0, 0);
    ps.minEmitPower = 5; ps.maxEmitPower = 9;
    ps.minLifeTime = 0.9; ps.maxLifeTime = 2.0;
    ps.minSize = 0.35; ps.maxSize = 1.5;
    ps.emitRate = 260;
    ps.color1 = new BABYLON.Color4(0.65, 0.8, 1.0, 0.55);
    ps.color2 = new BABYLON.Color4(0.5, 0.7, 1.0, 0.4);
    ps.colorDead = new BABYLON.Color4(0.4, 0.6, 1.0, 0);
    ps.blendMode = BABYLON.ParticleSystem.BLENDMODE_ADD;
    s.ps = ps;
  }

  let engine, scene, camera, pp, pipeline, panel;
  let houseDim = null;
  let houseVisible = false;
  let simTime = 0;
  const handles = {};
  const flashEl = () => document.getElementById("flash");

  /* ------------------------------------------------------- orbit state -- */
  const orbit = { theta: -0.8, phi: 1.22, radius: 11, tTheta: -0.8, tPhi: 1.22, tRadius: 11, external: null };
  const steer = { yaw: 0, pitch: 0, tYaw: 0, tPitch: 0 };
  const driver = {
    pos: new BABYLON.Vector3(0, 0, -10),
    fwd: new BABYLON.Vector3(0, 0, 1),
    right: new BABYLON.Vector3(1, 0, 0),
    up: new BABYLON.Vector3(0, 1, 0)
  };
  const bloodCam = { z: 0 };
  const fractCam = { t: 0 };

  function bloodCenterJS(z) {
    return {
      x: Math.sin(z * 0.21) * 2.2 + Math.sin(z * 0.070) * 3.0,
      y: Math.cos(z * 0.17) * 2.0 + Math.cos(z * 0.052) * 2.4
    };
  }

  /* ---------------------------------------------------------- journey --- */
  const J = {
    active: false, dim: 0, phase: "idle", t: 0,
    warp: 0, nextDim: 1, startR: 11, lerp: null
  };

  function shaderModeFor(dim) { return dim === 0 ? 0 : dim === 1 ? 1 : 2; }

  function banner(i, extra) {
    const elT = document.getElementById("hudTitle");
    const elS = document.getElementById("hudSub");
    elT.textContent = (extra ? extra.title : DIM_NAMES[i]);
    elS.textContent = (extra ? extra.sub : DIM_SUBS[i]);
    const hud = document.getElementById("hud");
    hud.classList.remove("show");
    void hud.offsetWidth;
    hud.classList.add("show");
  }
  function setObjective(s) { document.getElementById("objective").textContent = s || ""; }
  function setDepth(s) {
    const d = document.getElementById("depth");
    d.style.display = s ? "block" : "none";
    if (s) d.textContent = s;
  }
  function flashTo(v, ms, then) {
    const f = flashEl();
    f.style.transition = "opacity " + ms + "ms ease";
    f.style.opacity = v;
    if (then) setTimeout(then, ms + 30);
  }
  function easeInOut(t) { return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2; }

  function startJourney() {
    if (J.phase !== "idle") return;
    // whatever the camera was doing, descend in controlled orbit
    if (TUNE.camMode !== "orbit") { setCamMode("orbit"); }
    const d = BABYLON.Vector3.Distance(driver.pos, holePos);
    orbit.tRadius = clamp(d, P.rs * 2.3, 60);
    orbit.radius = d;
    J.active = true; J.t = 0; J.startR = orbit.tRadius; J.phase = "approach";
    banner(0, { title: "Descent initiated", sub: "Gravitational shear rising. Hold on to something." });
    setObjective("Approaching the event horizon…  (⤓ Dive Deeper to skip ahead)");
    AudioSys.setDimension(0);
  }
  function setCamMode(m) {
    TUNE.camMode = m;
    if (handles.camMode) handles.camMode.set(m);
    if (m === "fly") {
      free.pos.copyFrom(driver.pos);
      free.yaw = Math.atan2(driver.fwd.x, driver.fwd.z);
      free.pitch = Math.asin(clamp(driver.fwd.y, -1, 1));
    }
    if (m === "look") { look.tYaw = look.tPitch = look.yaw = look.pitch = 0; }
  }
  function warpTo(next) {
    J.nextDim = next;
    J.t = 0; J.phase = "warpOut";
    setDepth(null);
  }
  function warpSpeed() { return 1.05 * TUNE.journeySpeed; }

  function performSwitch() {
    J.dim = J.nextDim;
    if (J.dim === 2) {
      // crossfade into the external house engine behind a white flash
      flashTo(1, 320, () => {
        document.getElementById("renderCanvas").style.display = "none";
        const hc = document.getElementById("houseCanvas");
        hc.style.display = "block";
        houseVisible = true;
        houseDim.reset();
        houseDim.start(true);
        houseDim.engine.resize();
        J.warp = 0; J.phase = "house";
        banner(2);
        setObjective("Walk the path. WASD + drag, or let it carry you. The door is the way out.");
        flashTo(0, 700);
      });
      J.phase = "switching";
      return;
    }
    // shader-side destination
    if (J.dim === 0) {
      // a NEW singularity every loop — the journey is endless
      J.seed = (J.seed * 1664525 + 1013904223) >>> 0;
      const forged = BHPRESETS.forge(J.seed);
      applyParams(forged.params, 0, true);
      if (handles.forgedName) handles.forgedName.set(forged.label);
      if (handles.seedEl) handles.seedEl.input.value = J.seed;
      orbit.tRadius = forged.params.rs * 11;
      orbit.radius = orbit.tRadius * 1.35;
      orbit.external = null;
      TUNE.fov = TUNE.baseFov;
      banner(0, { title: "A new singularity", sub: forged.label + " — forged at the end of the tunnel." });
      setObjective("The descent never ends. (⏹ Return to orbit to escape)");
      J.warp = 1;
      J.t = 0; J.phase = "warpInThenIdle";
      AudioSys.setDimension(0);
    } else if (J.dim === 1) {
      bloodCam.z = 0;
      steer.yaw = steer.pitch = steer.tYaw = steer.tPitch = 0;
      banner(1);
      setObjective("Go with the flow. Drag to look around. (⤓ Dive Deeper to go further)");
      J.phase = "warpIn";
      AudioSys.setDimension(1);
    } else {
      fractCam.t = 0;
      banner(3);
      setObjective("There is no floor here. (⤓ Dive Deeper to find the next door)");
      J.phase = "warpIn";
      AudioSys.setDimension(3);
    }
    J.t = 0;
  }

  function leaveHouse() {
    flashTo(1, 380, () => {
      houseDim.stop();
      document.getElementById("houseCanvas").style.display = "none";
      document.getElementById("renderCanvas").style.display = "block";
      houseVisible = false;
      engine.resize();
      J.dim = 3; J.warp = 1; J.t = 0; J.phase = "warpIn";
      banner(3);
      setObjective("There is no floor here. (⤓ Dive Deeper to find the next door)");
      AudioSys.setDimension(3);
      flashTo(0, 900);
    });
    J.phase = "switching";
  }

  function abortJourney() {
    flashTo(1, 300, () => {
      if (houseVisible) {
        houseDim.stop();
        document.getElementById("houseCanvas").style.display = "none";
        document.getElementById("renderCanvas").style.display = "block";
        houseVisible = false;
        engine.resize();
      }
      J.active = false; J.phase = "idle"; J.dim = 0; J.warp = 0;
      orbit.tRadius = Math.max(orbit.tRadius, P.rs * 9);
      orbit.external = null;
      TUNE.fov = TUNE.baseFov;
      banner(0, { title: "Back in orbit", sub: "The void lets you go. For now." });
      setObjective("Orbit the anomaly, or dive again.");
      setDepth(null);
      AudioSys.setDimension(0);
      flashTo(0, 700);
    });
  }

  function journeyTick(dt) {
    switch (J.phase) {
      case "idle":
        orbit.external = null;
        if (TUNE.fov !== TUNE.baseFov) TUNE.fov += (TUNE.baseFov - TUNE.fov) * dt * 3;
        break;
      case "approach": {
        J.t += dt * TUNE.journeySpeed / 13.0;
        const e = easeInOut(clamp(J.t, 0, 1));
        orbit.external = lerp(J.startR, P.rs * 2.06, e);
        TUNE.fov = TUNE.baseFov + e * 14;
        setDepth("radial distance  r = " + (orbit.external / P.rs).toFixed(2) + "  R_s" + (e > 0.85 ? "   ⚠ photon sphere" : ""));
        if (J.t >= 1) warpTo(1);
        break;
      }
      case "warpOut":
        J.t += dt / warpSpeed();
        J.warp = easeInOut(clamp(J.t, 0, 1));
        if (J.t >= 1) { J.warp = 1; performSwitch(); }
        break;
      case "warpIn":
        J.t += dt / (1.35 * TUNE.journeySpeed);
        J.warp = 1 - easeInOut(clamp(J.t, 0, 1));
        if (J.t >= 1) { J.warp = 0; J.phase = "travel"; J.t = 0; }
        break;
      case "warpInThenIdle":
        J.t += dt / 1.6;
        J.warp = 1 - easeInOut(clamp(J.t, 0, 1));
        if (J.t >= 1) { J.warp = 0; J.phase = J.active ? "orbitPause" : "idle"; J.t = 0; }
        break;
      case "orbitPause":
        J.t += dt * TUNE.journeySpeed;
        if (J.t > 3.5 && J.active) {
          J.startR = orbit.radius;
          J.t = 0; J.phase = "approach";
          banner(0, { title: "Descent re-initiated", sub: "The pull is automatic now. ⏹ to escape." });
        } else if (!J.active) {
          J.phase = "idle";
        }
        break;
      case "travel": {
        J.t += dt;
        const dur = (J.dim === 1 ? 17 : 21) / TUNE.journeySpeed;
        if (J.dim === 1) {
          setDepth("depth into the vessel  " + bloodCam.z.toFixed(1) + " m");
        } else {
          setDepth("void traversal  " + (fractCam.t * 3).toFixed(1) + " kly");
        }
        if (J.t >= dur) warpTo(NEXT_DIM[J.dim]);
        break;
      }
      case "switching":
      case "house":
        break;
    }
  }

  const _v1 = new BABYLON.Vector3(), _v2 = new BABYLON.Vector3();
  let _q1 = new BABYLON.Quaternion(), _q2 = new BABYLON.Quaternion();
  const _f1 = new BABYLON.Vector3();
  const _twinRel = new BABYLON.Vector3();
  const _camQ = new BABYLON.Quaternion();

  /* Rough gravitational eclipse fade: a summoned object sitting behind the
   * apparent shadow disk of the hole fades out (its light is bent around). */
  function occFade(pos, margin) {
    _v1.copyFrom(pos).subtractInPlace(camera.position);
    const d1 = _v1.length();
    _v2.copyFrom(holePos).subtractInPlace(camera.position);
    const d2 = _v2.length();
    if (d1 <= d2) return 1;
    const angHole = Math.asin(clamp(P.rs * 2.5 / d2, 0, 1)) * (margin || 1);
    const dot = BABYLON.Vector3.Dot(_v1.normalize(), _v2.normalize());
    const ang = Math.acos(clamp(dot, -1, 1));
    if (ang >= angHole * 1.35) return 1;
    if (ang <= angHole) return 0;
    return (ang - angHole) / (angHole * 0.35);
  }

  function summonsTick(dt) {
    const vis = (J.dim === 0) && !houseVisible;
    // companion star
    const s = SUM.star;
    if (s.mesh) {
      const on = vis && s.on;
      s.mesh.setEnabled(on);
      s.light.setEnabled(on);
      if (on) {
        s.mesh.position.set(0, 0, 0).addInPlace(holePos);
        _v1.set(0.55, 0.34, -0.76).normalize().scaleInPlace(260);
        s.mesh.position.addInPlace(_v1);
        s.mesh.scaling.setAll(s.size);
        const fade = occFade(s.mesh.position, 3.5);
        s.mesh.material.emissiveColor = C3(s.color).scale(Math.max(fade, 0.001));
        s.mesh.material.alpha = Math.max(fade, 0.001);
        s.light.intensity = 1.15 * (0.25 + 0.75 * fade);
        s.light.diffuse = C3(s.color);
        _v2.copyFrom(holePos).subtractInPlace(s.mesh.position).normalize();
        s.light.direction.copyFrom(_v2);
        s.light.position = s.mesh.position;
      }
    }
    // planets — most ride the disk plane, some polar
    diskBasis(_dN, _dT1, _dT2);
    for (const pl of SUM.planets) {
      pl.mesh.setEnabled(vis);
      if (!vis) continue;
      pl.ang += pl.speed * dt;
      pl.mesh.rotation.y += dt * 0.35;
      const ca = Math.cos(pl.ang) * pl.orbit, sa = Math.sin(pl.ang) * pl.orbit;
      if (pl.plane === 1) {
        pl.mesh.position.set(
          holePos.x + _dT1.x * ca + _dT2.x * sa,
          holePos.y + _dT1.y * ca + _dT2.y * sa,
          holePos.z + _dT1.z * ca + _dT2.z * sa);
      } else {
        pl.mesh.position.set(
          holePos.x + Math.cos(pl.ang) * pl.orbit,
          holePos.y + Math.sin(pl.ang) * pl.orbit * 0.45,
          holePos.z + Math.sin(pl.ang) * pl.orbit);
      }
      pl.mesh.material.alpha = Math.max(occFade(pl.mesh.position), 0.001);
    }
    // asteroid belt
    if (SUM.belt.node) {
      const on = vis && SUM.belt.on;
      SUM.belt.node.setEnabled(on);
      if (on) {
        SUM.belt.node.position.copyFrom(holePos);
        SUM.belt.node.rotation.y += dt * 0.045;
      }
    }
    // comet with a streaming tail
    const cm = SUM.comet;
    if (cm.mesh) {
      const on = vis && cm.on;
      cm.mesh.setEnabled(on);
      if (on) {
        if (!cm.ps.isStarted()) cm.ps.start();
        cm.ang += cm.speed * dt;
        const r = 46 * (1 + 0.32 * Math.sin(cm.ang * 0.5));
        cm.mesh.position.set(
          holePos.x + Math.cos(cm.ang) * r,
          holePos.y + Math.sin(cm.ang * 0.8) * 7,
          holePos.z + Math.sin(cm.ang) * r);
        _v1.copyFrom(cm.mesh.position).subtractInPlace(holePos).normalize();
        _v2.set(_v1.x * 8 - Math.sin(cm.ang) * 3.0, _v1.y * 8 + 0.6, _v1.z * 8 + Math.cos(cm.ang) * 3.0);
        cm.ps.direction1.copyFrom(_v2);
        cm.ps.direction2.copyFrom(_v2).scaleInPlace(1.15);
        cm.mesh.material.alpha = Math.max(occFade(cm.mesh.position), 0.001);
      } else if (cm.ps.isStarted()) cm.ps.stop();
    }
    // twin singularity orbits in the disk plane (shader consumes twinWorld)
    if (twin.on) {
      twin.ang += twin.speed * dt;
      const ca = Math.cos(twin.ang) * twin.dist, sa = Math.sin(twin.ang) * twin.dist;
      SUM.twinWorld.set(
        holePos.x + _dT1.x * ca + _dT2.x * sa,
        holePos.y + _dT1.y * ca + _dT2.y * sa,
        holePos.z + _dT1.z * ca + _dT2.z * sa);
    }
  }

  function driverTick(dt) {
    // smooth orbit toward targets
    const damp = 1 - Math.exp(-dt * 7);
    if (orbit.external === null) {
      orbit.radius += (orbit.tRadius - orbit.radius) * damp;
      orbit.theta += (orbit.tTheta - orbit.theta) * damp;
      orbit.phi += (clamp(orbit.tPhi, 0.06, 3.08) - orbit.phi) * damp;
    } else {
      orbit.radius += (orbit.external - orbit.radius) * (1 - Math.exp(-dt * 3.5));
      orbit.theta += (orbit.tTheta - orbit.theta) * damp * 0.4;
      orbit.phi += (clamp(orbit.tPhi, 0.06, 3.08) - orbit.phi) * damp * 0.4;
    }
    if (TUNE.orbitSpeed !== 0 && J.phase === "idle") orbit.tTheta += TUNE.orbitSpeed * dt * 0.017453;

    if (J.dim === 0) {
      const freeFly = (TUNE.camMode === "fly") && J.phase === "idle";
      if (freeFly) {
        // WASD free flight
        const cpf = Math.cos(free.pitch);
        driver.fwd.set(Math.sin(free.yaw) * cpf, Math.sin(free.pitch), Math.cos(free.yaw) * cpf).normalize();
        driver.right = BABYLON.Vector3.Cross(driver.fwd, BABYLON.Axis.Y).normalize();
        driver.up = BABYLON.Vector3.Cross(driver.right, driver.fwd);
        const spd = TUNE.flySpeed * (keys.has("shift") ? 2.6 : 1) * dt;
        if (keys.has("w")) free.pos.addInPlace(driver.fwd.scale(spd));
        if (keys.has("s")) free.pos.addInPlace(driver.fwd.scale(-spd));
        if (keys.has("a")) free.pos.addInPlace(driver.right.scale(spd));
        if (keys.has("d")) free.pos.addInPlace(driver.right.scale(-spd));
        if (keys.has("e") || keys.has(" ")) free.pos.y += spd;
        if (keys.has("q") || keys.has("control")) free.pos.y -= spd;
        driver.pos.copyFrom(free.pos);
        // fly straight into the maw → contact dive starts the journey
        const dsq = BABYLON.Vector3.DistanceSquared(free.pos, holePos);
        if (dsq < P.rs * 2.05 * P.rs * 2.05) startJourney();
      } else {
        // orbit (or look / hole-pan): camera circles orbitCenter; the hole may be elsewhere
        const r = orbit.radius;
        const sp = Math.sin(orbit.phi), cp = Math.cos(orbit.phi);
        driver.pos.set(
          orbitCenter.x + r * sp * Math.cos(orbit.theta),
          orbitCenter.y + r * cp,
          orbitCenter.z + r * sp * Math.sin(orbit.theta));
        driver.fwd.copyFrom(orbitCenter).subtractInPlace(driver.pos).normalize();
        let upRef = Math.abs(driver.fwd.y) > 0.985 ? new BABYLON.Vector3(0, 0, 1) : new BABYLON.Vector3(0, 1, 0);
        driver.right = BABYLON.Vector3.Cross(driver.fwd, upRef).normalize();
        driver.up = BABYLON.Vector3.Cross(driver.right, driver.fwd);
        if (TUNE.camMode === "look") {
          look.yaw += (look.tYaw - look.yaw) * (1 - Math.exp(-dt * 6));
          look.pitch += (look.tPitch - look.pitch) * (1 - Math.exp(-dt * 6));
          _q1 = BABYLON.Quaternion.RotationAxis(driver.up, -look.yaw);
          driver.fwd.rotateByQuaternionToRef(_q1, _f1);
          driver.right.rotateByQuaternionToRef(_q1, driver.right);
          _q2 = BABYLON.Quaternion.RotationAxis(driver.right, look.pitch);
          _f1.rotateByQuaternionToRef(_q2, driver.fwd);
          driver.fwd.normalize();
          driver.right = BABYLON.Vector3.Cross(driver.fwd, upRef).normalize();
          driver.up = BABYLON.Vector3.Cross(driver.right, driver.fwd);
        }
        if (TUNE.camMode === "hole" && J.phase === "idle") {
          const dsq = BABYLON.Vector3.DistanceSquared(driver.pos, holePos);
          if (dsq < P.rs * 2.05 * P.rs * 2.05) startJourney();
        }
        if (J.phase === "approach") {
          // pull the hole back to the orbit focus while we descend
          _v1.copyFrom(orbitCenter).subtractInPlace(holePos).scaleInPlace(Math.min(1, dt * 0.55));
          holePos.addInPlace(_v1);
        }
      }
    } else if (J.dim === 1) {
      const speed = 6.5 * TUNE.journeySpeed;
      bloodCam.z += speed * dt;
      steer.yaw += (steer.tYaw - steer.yaw) * (1 - Math.exp(-dt * 4));
      steer.pitch += (steer.tPitch - steer.pitch) * (1 - Math.exp(-dt * 4));
      const c = bloodCenterJS(bloodCam.z);
      driver.pos.set(c.x, c.y, bloodCam.z);
      const c2 = bloodCenterJS(bloodCam.z + 7);
      driver.fwd.set(c2.x - c.x + steer.yaw * 6.0, c2.y - c.y + steer.pitch * 4.5, 7).normalize();
      driver.right = BABYLON.Vector3.Cross(driver.fwd, BABYLON.Axis.Y).normalize();
      driver.up = BABYLON.Vector3.Cross(driver.right, driver.fwd);
    } else if (J.dim === 3) {
      fractCam.t += dt;
      driver.pos.set(Math.sin(fractCam.t * 0.21) * 1.3, Math.cos(fractCam.t * 0.17) * 1.3, fractCam.t * 2.6);
      driver.fwd.set(0, 0, 1);
      driver.right.set(1, 0, 0);
      driver.up.set(0, 1, 0);
    }
  }

  /* ----------------------------------------------------- param lerp ----- */
  function applyParams(target, dur) {
    const full = Object.assign({}, DEFAULTS, target);
    if (!dur || dur <= 0) {
      J.lerp = null;
      for (const k of PRESET_KEYS) P[k] = full[k];
      refreshHandles();
      return;
    }
    J.lerp = { from: {}, to: full, t: 0, dur };
    for (const k of PRESET_KEYS) J.lerp.from[k] = P[k];
    refreshHandles(full);
  }
  function lerpTick(dt) {
    if (!J.lerp) return;
    const L = J.lerp;
    L.t += dt / L.dur;
    const t = clamp(L.t, 0, 1);
    const e = t * t * (3 - 2 * t);
    for (const k of PRESET_KEYS) {
      const a = L.from[k], b = L.to[k];
      if (typeof a === "number" && typeof b === "number") P[k] = lerp(a, b, e);
      else P[k] = hexLerp(a, b, e);
    }
    if (L.t >= 1) J.lerp = null;
  }

  function refreshHandles(from) {
    const src = from || P;
    for (const k of PRESET_KEYS) if (handles[k]) handles[k].set(src[k]);
  }

  /* ------------------------------------------------------------ audio --- */
  const AudioSys = {
    ctx: null, master: null, o1: null, o2: null, sub: null, shimmer: null, shimmerG: null,
    noiseG: null, heartG: null, heartOsc: null, on: false, volume: TUNE.volume, dim: 0,
    ensure() {
      if (this.ctx) return;
      const AC = window.AudioContext || window.webkitAudioContext;
      if (!AC) return;
      const ctx = this.ctx = new AC();
      this.master = ctx.createGain(); this.master.gain.value = 0; this.master.connect(ctx.destination);
      const mk = (type, f, g) => {
        const o = ctx.createOscillator(); o.type = type; o.frequency.value = f;
        const gg = ctx.createGain(); gg.gain.value = g;
        o.connect(gg); gg.connect(this.master); o.start();
        return { o, g: gg };
      };
      const a = mk("sine", 55, 0.20); this.o1 = a.o; this.o1g = a.g;
      const b = mk("triangle", 82.41, 0.07); this.o2 = b.o;
      const c = mk("sine", 32, 0.22); this.sub = c.o;
      const s = mk("sine", 523.25, 0.0); this.shimmer = s.o; this.shimmerG = s.g;
      const lfo = mk("sine", 0.09, 0.0);
      const lfoG = ctx.createGain(); lfoG.gain.value = 0.035;
      lfo.o.connect(lfoG); lfoG.connect(this.shimmerG.gain);
      // brown noise bed
      const len = ctx.sampleRate * 3;
      const buf = ctx.createBuffer(1, len, ctx.sampleRate);
      const ch = buf.getChannelData(0);
      let last = 0;
      for (let i = 0; i < len; i++) {
        const w = Math.random() * 2 - 1;
        last = (last + 0.02 * w) / 1.02;
        ch[i] = last * 3.2;
      }
      const src = ctx.createBufferSource(); src.buffer = buf; src.loop = true;
      const lp = ctx.createBiquadFilter(); lp.type = "lowpass"; lp.frequency.value = 240;
      this.noiseG = ctx.createGain(); this.noiseG.gain.value = 0.16;
      src.connect(lp); lp.connect(this.noiseG); this.noiseG.connect(this.master); src.start();
      // heartbeat
      const h = mk("sine", 47, 0.0); this.heartOsc = h.o; this.heartG = h.g;
    },
    setPower(on) {
      this.on = on;
      if (on) {
        this.ensure();
        if (!this.ctx) return;
        this.ctx.resume();
        this.master.gain.setTargetAtTime(this.volume, this.ctx.currentTime, 0.4);
        this.setDimension(this.dim);
      } else if (this.ctx) {
        this.master.gain.setTargetAtTime(0, this.ctx.currentTime, 0.25);
      }
    },
    setVolume(v) {
      this.volume = v;
      if (this.on && this.ctx) this.master.gain.setTargetAtTime(v, this.ctx.currentTime, 0.1);
    },
    setDimension(d) {
      this.dim = d;
      if (!this.ctx || !this.on) return;
      const t = this.ctx.currentTime;
      const base = [55, 41.2, 30, 66][d] || 55;
      this.o1.frequency.setTargetAtTime(base, t, 0.8);
      this.o2.frequency.setTargetAtTime(base * 1.498, t, 0.8);
      this.sub.frequency.setTargetAtTime(base * 0.55, t, 0.8);
      this.shimmerG.gain.setTargetAtTime(d === 3 ? 0.05 : 0.012, t, 0.8);
      this.noiseG.gain.setTargetAtTime(d === 2 ? 0.26 : 0.12, t, 0.8);
      if (d === 1) this.heartG.gain.setTargetAtTime(0.4, t, 0.5);
      else this.heartG.gain.setTargetAtTime(0, t, 0.5);
    },
    tickHeart(v) {
      if (this.on && this.dim === 1 && this.heartG) {
        this.heartOsc.frequency.setValueAtTime(47 + v * 6, this.ctx.currentTime);
      }
    }
  };

  /* ------------------------------------------------------------- boot --- */
  window.addEventListener("DOMContentLoaded", init);

  function fatal(msg) {
    const d = document.getElementById("error");
    d.style.display = "flex";
    d.querySelector("p").textContent = msg;
  }

  function init() {
    if (!window.BABYLON) { fatal("Could not load Babylon.js from the CDN. Check your connection and reload."); return; }
    BH_GLSL.install();
    const canvas = document.getElementById("renderCanvas");
    engine = new BABYLON.Engine(canvas, true, { antialias: true, stencil: false, powerPreference: "high-performance" });
    if (engine.webGLVersion < 2) document.getElementById("gl1warn").style.display = "block";
    scene = new BABYLON.Scene(engine);
    scene.clearColor = new BABYLON.Color4(0, 0, 0, 0); // transparent so summoned meshes layer under the raymarch
    camera = new BABYLON.FreeCamera("shaderCam", new BABYLON.Vector3(0, 0, -10), scene);
    camera.minZ = 0.08;
    camera.maxZ = 4000;
    scene.activeCamera = camera;

    const UNIFORMS = ["uRes", "uTime", "uCamPos", "uCamRight", "uCamUp", "uCamFwd", "uFovTan", "uAspect",
      "uModeA", "uMix", "uWarp", "uRS", "uSpin", "uLensStr", "uDtScale", "uSteps",
      "uDiskInner", "uDiskOuter", "uDiskTemp", "uDiskBright", "uBeaming", "uSwirlK",
      "uDiskN", "uDiskT1", "uDiskT2", "uRingBoost", "uStarDensity", "uNebulaAmt", "uBandAmt",
      "uTintA", "uTintB", "uPulse", "uFractK", "uFractC", "uFractGlow",
      "uHolePos", "uTwinOn", "uTwinPos", "uRS2", "uRingCol", "uDiskTint", "uNebScale"];

    pp = new BABYLON.PostProcess("singularity", "singularity", UNIFORMS, [], 1.0, camera, BABYLON.Texture.BILINEAR_SAMPLINGMODE, engine);
    pp.autoClear = true;
    const _diskN = new BABYLON.Vector3();
    const _diskT1 = new BABYLON.Vector3();
    const _diskT2 = new BABYLON.Vector3();
    const applyUniforms = (effect) => {
      const w = engine.getRenderWidth() || 1, h = engine.getRenderHeight() || 1;
      effect.setVector2("uRes", new BABYLON.Vector2(w, h));
      effect.setFloat("uAspect", w / h);
      effect.setFloat("uTime", simTime);
      effect.setVector3("uCamPos", driver.pos);
      effect.setVector3("uCamRight", driver.right);
      effect.setVector3("uCamUp", driver.up);
      effect.setVector3("uCamFwd", driver.fwd);
      effect.setFloat("uFovTan", Math.tan((TUNE.fov * Math.PI / 180) / 2));
      effect.setFloat("uModeA", shaderModeFor(J.dim));
      effect.setFloat("uMix", 0);
      effect.setFloat("uWarp", J.warp);
      effect.setFloat("uRS", P.rs);
      effect.setFloat("uSpin", P.spin * (J.phase === "approach" ? 1 + clamp(J.t, 0, 1) * 0.5 : 1));
      effect.setFloat("uLensStr", P.lens);
      effect.setFloat("uDtScale", TUNE.dtScale);
      effect.setInt("uSteps", TUNE.steps | 0);
      effect.setFloat("uDiskInner", P.diskInner * P.rs);
      effect.setFloat("uDiskOuter", P.diskOuter * P.rs);
      effect.setFloat("uDiskTemp", P.temp);
      effect.setFloat("uDiskBright", P.bright);
      effect.setFloat("uBeaming", P.beaming);
      effect.setFloat("uSwirlK", P.swirl);
      // disk frame from tilts
      const tx = P.tiltX * Math.PI / 180, tz = P.tiltZ * Math.PI / 180;
      _diskN.set(Math.sin(tz) * Math.cos(tx), Math.cos(tz) * Math.cos(tx), Math.sin(tx));
      let ref = Math.abs(_diskN.z) < 0.94 ? BABYLON.Vector3.Forward() : BABYLON.Vector3.Right();
      BABYLON.Vector3.CrossToRef(_diskN, ref, _diskT1).normalize();
      BABYLON.Vector3.CrossToRef(_diskN, _diskT1, _diskT2);
      effect.setVector3("uDiskN", _diskN);
      effect.setVector3("uDiskT1", _diskT1);
      effect.setVector3("uDiskT2", _diskT2);
      effect.setFloat("uRingBoost", P.ring);
      effect.setFloat("uStarDensity", P.star);
      effect.setFloat("uNebulaAmt", P.nebula);
      effect.setFloat("uBandAmt", P.band);
      effect.setColor3("uTintA", C3(P.tintA));
      effect.setColor3("uTintB", C3(P.tintB));
      const hb = Math.pow(Math.max(0, Math.sin(simTime * 1.5)), 6) + 0.72;
      effect.setFloat("uPulse", hb);
      AudioSys.tickHeart(hb);
      effect.setFloat("uFractK", P.fractK);
      effect.setVector3("uFractC", new BABYLON.Vector3(P.fractCS, P.fractCS * 0.9, P.fractCS * 1.12));
      effect.setFloat("uFractGlow", P.fractGlow);
      // spacetime control
      effect.setVector3("uHolePos", holePos);
      effect.setFloat("uTwinOn", twin.on ? 1 : 0);
      _twinRel.copyFrom(SUM.twinWorld).subtractInPlace(holePos);
      effect.setVector3("uTwinPos", _twinRel);
      effect.setFloat("uRS2", twin.rs);
      effect.setColor3("uRingCol", C3(P.ringCol));
      effect.setColor3("uDiskTint", C3(P.diskTint));
      effect.setFloat("uNebScale", P.nebScale);
    };
    pp.onApply = applyUniforms;
    if (pp.onApplyObservable) pp.onApplyObservable.add(applyUniforms);

    pipeline = new BABYLON.DefaultRenderingPipeline("rp", true, scene, [camera]);
    pipeline.samples = 4;
    pipeline.fxaaEnabled = true;
    pipeline.bloomEnabled = true;
    pipeline.bloomThreshold = 0.72;
    pipeline.chromaticAberrationEnabled = true;
    pipeline.grainEnabled = true;
    pipeline.grain.animated = true;
    pipeline.imageProcessing.toneMappingEnabled = true;
    pipeline.imageProcessing.toneMappingType = BABYLON.ImageProcessingConfiguration.TONEMAPPING_ACES;

    // start with Gargantua
    const garg = BHPRESETS.CURATED[0];
    P = Object.assign({}, DEFAULTS, garg.params);
    J.seed = 13371337;

    // URL hash seed support: #seed=1234
    const m = /[#&]seed=(\d+)/.exec(location.hash || "");
    if (m) {
      const f = BHPRESETS.forge(parseInt(m[1], 10) >>> 0);
      P = Object.assign({}, DEFAULTS, f.params);
      J.seed = f.seed;
      setTimeout(() => banner(0, { title: "Seeded singularity", sub: f.label }), 900);
    }

    houseDim = createHouseDimension(document.getElementById("houseCanvas"), { onDoor: () => { if (J.phase === "house") leaveHouse(); } });

    buildPanel();
    refreshHandles();
    wireInput(canvas);
    banner(0);
    setObjective("Drag to orbit · scroll to zoom · customize everything · then dive in.");
    document.getElementById("loading").style.display = "none";

    engine.runRenderLoop(() => {
      const dt = Math.min(engine.getDeltaTime() / 1000, 0.05);
      simTime += dt;
      journeyTick(dt);
      lerpTick(dt);
      driverTick(dt);
      summonsTick(dt);
      pushPipeline();
      // align the real 3D scene camera with the shader driver basis
      camera.position.copyFrom(driver.pos);
      BABYLON.Quaternion.FromLookDirectionLHToRef(driver.fwd, driver.up, _camQ);
      camera.rotationQuaternion = _camQ;
      camera.fov = TUNE.fov * Math.PI / 180;
      if (!houseVisible) scene.render();
    });
    window.addEventListener("resize", () => { engine.resize(); if (houseDim) houseDim.engine.resize(); });

    let fc = 0;
    setInterval(() => {
      panel.setFooterRight(engine.getFps().toFixed(0) + " fps");
    }, 800);
  }

  function pushPipeline() {
    pipeline.imageProcessing.exposure = P.exposure;
    pipeline.imageProcessing.contrast = P.contrast;
    pipeline.bloomWeight = P.bloom;
    pipeline.chromaticAberrationEnabled = P.ca > 0.01;
    if (pipeline.chromaticAberration) {
      pipeline.chromaticAberration.aberrationAmount = P.ca * 10;
      pipeline.chromaticAberration.radialIntensity = Math.max(0.9, P.ca * 0.5);
    }
    pipeline.grainEnabled = P.grain > 0.01;
    pipeline.grain.intensity = P.grain;
    pipeline.imageProcessing.vignetteEnabled = P.vignette > 0.01;
    pipeline.imageProcessing.vignetteWeight = P.vignette * 3.0;
  }

  /* ------------------------------------------------------------ input --- */
  function wireInput(canvas) {
    let dragging = false, lx = 0, ly = 0;
    canvas.addEventListener("pointerdown", (e) => {
      dragging = true; lx = e.clientX; ly = e.clientY;
      canvas.setPointerCapture(e.pointerId);
    });
    canvas.addEventListener("pointermove", (e) => {
      if (!dragging) return;
      const dx = (e.clientX - lx) * 0.005 * TUNE.sensitivity;
      const dy = (e.clientY - ly) * 0.005 * TUNE.sensitivity;
      lx = e.clientX; ly = e.clientY;
      if (J.dim === 0 && J.phase === "idle") {
        if (TUNE.camMode === "fly") {
          free.yaw += dx;
          free.pitch = clamp(free.pitch + dy, -1.45, 1.45);
        } else if (TUNE.camMode === "look") {
          look.tYaw = clamp(look.tYaw + dx, -2.7, 2.7);
          look.tPitch = clamp(look.tPitch - dy, -1.4, 1.4);
        } else if (TUNE.camMode === "hole") {
          // drag the black hole itself through space
          const k = orbit.radius * 1.05;
          holePos.addInPlace(driver.right.scale(dx * k));
          holePos.addInPlace(driver.up.scale(-dy * k));
          const dmin = P.rs * 2.3, dmax = 150;
          const dd = BABYLON.Vector3.Distance(holePos, driver.pos);
          if (dd < dmin) {
            _v1.copyFrom(holePos).subtractInPlace(driver.pos).normalize().scaleInPlace(dmin - dd);
            holePos.addInPlace(_v1);
          }
        } else {
          orbit.tTheta += dx;
          orbit.tPhi = clamp(orbit.tPhi + dy, 0.06, 3.08);
        }
      } else if (J.dim === 1 && (J.phase === "travel" || J.phase === "warpIn")) {
        steer.tYaw = clamp(steer.tYaw - dx * 0.5, -0.65, 0.65);
        steer.tPitch = clamp(steer.tPitch - dy * 0.5, -0.5, 0.5);
      } else if (J.dim === 0) {
        orbit.tTheta += dx * 0.4;
        orbit.tPhi = clamp(orbit.tPhi + dy * 0.4, 0.06, 3.08);
      }
    });
    const stop = (e) => { dragging = false; };
    canvas.addEventListener("pointerup", stop);
    canvas.addEventListener("pointercancel", stop);
    canvas.addEventListener("wheel", (e) => {
      e.preventDefault();
      if (J.dim !== 0 || J.phase === "approach" || J.phase === "switching") return;
      if (TUNE.camMode === "fly") {
        const cpf = Math.cos(free.pitch);
        _v1.set(Math.sin(free.yaw) * cpf, Math.sin(free.pitch), Math.cos(free.yaw) * cpf);
        free.pos.addInPlace(_v1.scaleInPlace(-e.deltaY * 0.025 * TUNE.flySpeed * 0.12));
        const dd = BABYLON.Vector3.Distance(free.pos, holePos);
        if (dd < P.rs * 2.2) {
          _v1.copyFrom(free.pos).subtractInPlace(holePos).normalize().scaleInPlace(P.rs * 2.2 - dd);
          free.pos.addInPlace(_v1);
        }
      } else if (TUNE.camMode === "hole") {
        // dolly the hole toward / away from the camera
        _v1.copyFrom(driver.fwd).scaleInPlace(-e.deltaY * 0.004 * orbit.radius);
        holePos.addInPlace(_v1);
        const dd = BABYLON.Vector3.Distance(holePos, driver.pos);
        if (dd < P.rs * 2.3) {
          _v1.copyFrom(holePos).subtractInPlace(driver.pos).normalize().scaleInPlace(P.rs * 2.3 - dd);
          holePos.addInPlace(_v1);
        }
      } else {
        orbit.tRadius = clamp(orbit.tRadius * (1 + e.deltaY * 0.0011), P.rs * 2.3, 42);
      }
    }, { passive: false });
    window.addEventListener("keydown", (e) => {
      if (/input|select|textarea/i.test(document.activeElement.tagName)) return;
      keys.add(e.key.toLowerCase());
    });
    window.addEventListener("keyup", (e) => keys.delete(e.key.toLowerCase()));
    window.addEventListener("blur", () => keys.clear());
  }

  /* -------------------------------------------------------------- GUI --- */
  function buildPanel() {
    panel = XUI.createPanel("🕳 Singularity Vault", { accent: "#b78bff", footer: "Babylon.js · WebGL2 · geodesic raymarch" });

    const fPresets = panel.folder("🎛 Preset Vault — 4,294,967,296 configurations", true);
    fPresets.select("Curated presets", BHPRESETS.CURATED.map(p => ({ value: p.id, label: p.label })), "gargantua", (v) => {
      const p = BHPRESETS.CURATED.find(c => c.id === v);
      if (p) { applyParams(p.params, 1.5); handles.forgedName.set(p.label); }
    });
    fPresets.note("Every 32-bit seed grows a unique singularity — mass, spin, disk, sky, palette, film grain. Nothing repeats.");
    handles.seed = fPresets.number("Seed", J ? J.seed : 1337, (v) => {});
    const seedBtnRow = fPresets.buttonRow([
      { label: "⚒ Forge from seed", onClick: () => {
        const raw = handles.seedEl.input.value;
        const s = (parseInt(raw, 10) || 0) >>> 0;
        const f = BHPRESETS.forge(s);
        J.seed = s;
        applyParams(f.params, 1.8);
        handles.forgedName.set(f.label);
        location.hash = "seed=" + s;
      } },
      { label: "🎲 Random", onClick: () => {
        const s = (Math.random() * 4294967295) >>> 0;
        handles.seedEl.input.value = s;
        const f = BHPRESETS.forge(s);
        J.seed = s;
        applyParams(f.params, 1.8);
        handles.forgedName.set(f.label);
        location.hash = "seed=" + s;
      } }
    ]);
    handles.forgedName = fPresets.read("Forged identity", "—");
    fPresets.buttonRow([
      { label: "💾 Save snapshot", onClick: saveSnapshot },
      { label: "📂 Load snapshot", onClick: loadSnapshot },
      { label: "🗑", onClick: deleteSnapshot, kind: "danger" }
    ]);
    handles.snapSel = fPresets.select("Your snapshots", ["—"], "—", () => {});

    const fBH = panel.folder("⚫ The Singularity", true);
    handles.rs = fBH.slider("Mass (R_s scale)", 0.5, 2.2, 0.01, P.rs, v => { P.rs = v; });
    handles.spin = fBH.slider("Spin (frame dragging)", 0, 0.99, 0.01, P.spin, v => { P.spin = v; });
    handles.lens = fBH.slider("Lensing strength", 0.3, 2.0, 0.01, P.lens, v => { P.lens = v; });
    handles.ring = fBH.slider("Photon ring glow", 0, 2.5, 0.02, P.ring, v => { P.ring = v; });
    handles.ringCol = fBH.color("Photon ring tint", P.ringCol, v => { P.ringCol = v; });
    fBH.button("🎯 Recenter black hole (to camera focus)", () => { holePos.copyFrom(orbitCenter); });
    fBH.note("The hole can wander off-centre — orbit the void and drag the hole wherever you like (see Camera mode).");

    const fDisk = panel.folder("🔥 Accretion Disk", true);
    handles.diskInner = fDisk.slider("Inner edge (R_s)", 1.6, 6, 0.05, P.diskInner, v => { P.diskInner = v; });
    handles.diskOuter = fDisk.slider("Outer edge (R_s)", 5, 22, 0.1, P.diskOuter, v => { P.diskOuter = v; });
    handles.temp = fDisk.slider("Temperature", 0.15, 3.5, 0.01, P.temp, v => { P.temp = v; });
    handles.bright = fDisk.slider("Brightness", 0.2, 6, 0.05, P.bright, v => { P.bright = v; });
    handles.beaming = fDisk.slider("Relativistic beaming", 0, 1, 0.01, P.beaming, v => { P.beaming = v; });
    handles.swirl = fDisk.slider("Orbital speed", 0, 3, 0.02, P.swirl, v => { P.swirl = v; });
    handles.diskTint = fDisk.color("Disk tint (hue shift)", P.diskTint, v => { P.diskTint = v; });
    handles.tiltX = fDisk.slider("Tilt X", -70, 70, 1, P.tiltX, v => { P.tiltX = v; });
    handles.tiltZ = fDisk.slider("Tilt Z", -40, 40, 1, P.tiltZ, v => { P.tiltZ = v; });

    const fTwin = panel.folder("🕳🕳 Twin Singularity", false);
    fTwin.note("A second black hole, orbiting in the disk plane. Its gravity bends light too — watch the sky warp where two monsters pass.");
    fTwin.toggle("Summon the twin", false, v => { twin.on = v; });
    fTwin.slider("Separation (R_s, ~)", 8, 48, 1, twin.dist, v => { twin.dist = v; });
    fTwin.slider("Twin orbit speed", 0, 0.8, 0.01, twin.speed, v => { twin.speed = v; });
    fTwin.slider("Twin mass (R_s)", 0.3, 1.6, 0.05, twin.rs, v => { twin.rs = v; });
    fTwin.button("Twin beside the main hole", () => { twin.ang = 0; });

    const fSum = panel.folder("🪐 Summon Worlds", true);
    fSum.note("Real 3D objects orbiting your black hole — planets, a sun, an asteroid belt, a comet. They're lit, they move, and the lensing bends the sky behind them.");
    handles.starOn = fSum.toggle("Companion star", SUM.star.on, v => { if (v) ensureStar(); SUM.star.on = v; });
    fSum.color("Star color", SUM.star.color, v => { SUM.star.color = v; });
    fSum.slider("Star size", 6, 40, 1, SUM.star.size, v => { SUM.star.size = v; });
    fSum.sep();
    fSum.select("Planet type", [{ value: "gas", label: "Gas giant (banded)" }, { value: "rocky", label: "Rocky world (cratered)" }], SUM.planetScratch.type, v => { SUM.planetScratch.type = v; });
    fSum.slider("Orbit radius", 12, 70, 1, SUM.planetScratch.orbit, v => { SUM.planetScratch.orbit = v; });
    fSum.slider("Planet size", 0.8, 6, 0.1, SUM.planetScratch.size, v => { SUM.planetScratch.size = v; });
    fSum.slider("Orbit speed", 0, 0.6, 0.01, SUM.planetScratch.speed, v => { SUM.planetScratch.speed = v; });
    fSum.buttonRow([
      { label: "＋ Summon planet", onClick: addPlanet },
      { label: "－ Remove last", kind: "danger", onClick: removePlanet }
    ]);
    handles.planetCount = fSum.read("Planets", "—");
    fSum.sep();
    fSum.toggle("Asteroid belt (340 rocks)", SUM.belt.on, v => { SUM.belt.on = v; if (v && !SUM.belt.node) buildBelt(); });
    fSum.slider("Belt radius", 16, 60, 1, SUM.belt.radius, v => {
      SUM.belt.radius = v;
      if (SUM.belt.node && SUM.belt.on) buildBelt();
    });
    fSum.toggle("Comet with tail", SUM.comet.on, v => { if (v) ensureComet(); SUM.comet.on = v; });
    fSum.slider("Comet speed", 0.05, 1.2, 0.01, SUM.comet.speed, v => { SUM.comet.speed = v; });

    const fSky = panel.folder("🌌 Deep Field", false);
    handles.star = fSky.slider("Star density", 0, 2.5, 0.02, P.star, v => { P.star = v; });
    handles.nebula = fSky.slider("Nebula", 0, 2, 0.02, P.nebula, v => { P.nebula = v; });
    handles.band = fSky.slider("Galactic band", 0, 2, 0.02, P.band, v => { P.band = v; });
    handles.tintA = fSky.color("Nebula tint A", P.tintA, v => { P.tintA = v; });
    handles.tintB = fSky.color("Nebula tint B", P.tintB, v => { P.tintB = v; });
    handles.nebScale = fSky.slider("Nebula texture scale", 0.3, 3, 0.02, P.nebScale, v => { P.nebScale = v; });

    const fFilm = panel.folder("🎞 Film & Optics", false);
    handles.exposure = fFilm.slider("Exposure", 0.4, 2.5, 0.01, P.exposure, v => { P.exposure = v; });
    handles.contrast = fFilm.slider("Contrast", 0.6, 1.8, 0.01, P.contrast, v => { P.contrast = v; });
    handles.bloom = fFilm.slider("Bloom", 0, 2, 0.02, P.bloom, v => { P.bloom = v; });
    handles.ca = fFilm.slider("Chromatic aberration", 0, 4, 0.05, P.ca, v => { P.ca = v; });
    handles.grain = fFilm.slider("Film grain", 0, 30, 0.5, P.grain, v => { P.grain = v; });
    handles.vignette = fFilm.slider("Vignette", 0, 1, 0.02, P.vignette, v => { P.vignette = v; });

    const fVoid = panel.folder("🌀 Kaleidoscope Void", false);
    handles.fractK = fVoid.slider("Fractal constant", 1.2, 2.6, 0.01, P.fractK, v => { P.fractK = v; });
    handles.fractCS = fVoid.slider("Fold offset", 0.4, 1.6, 0.01, P.fractCS, v => { P.fractCS = v; });
    handles.fractGlow = fVoid.slider("Glow", 0.1, 2.5, 0.02, P.fractGlow, v => { P.fractGlow = v; });

    const fJourney = panel.folder("🚀 Journey — the endless descent", true);
    handles.jStatus = fJourney.read("Status", "in orbit");
    fJourney.slider("Journey speed", 0.4, 2.5, 0.05, TUNE.journeySpeed, v => { TUNE.journeySpeed = v; updateStatus(); });
    fJourney.button("🕳 ENTER THE BLACK HOLE", () => startJourney(), "accent");
    fJourney.buttonRow([
      { label: "⤓ Dive deeper", onClick: () => {
        if (J.phase === "approach" || J.phase === "travel") warpTo(NEXT_DIM[J.dim]);
        else if (J.phase === "house") leaveHouse();
      } },
      { label: "⏭ Skip dimension", onClick: () => {
        if (J.phase === "approach" || J.phase === "travel") warpTo(NEXT_DIM[J.dim]);
        else if (J.phase === "house") leaveHouse();
      } }
    ]);
    fJourney.button("⏹ Return to orbit", () => { if (J.phase !== "idle") abortJourney(); }, "danger");
    fJourney.note("Black hole → Bloodstream → Hollow House → Kaleidoscope → a brand-new forged singularity. It never ends.");

    const fCam = panel.folder("🎥 Camera & Movement", true);
    handles.camMode = fCam.select("Movement mode", [
      { value: "orbit", label: "🛰 Orbit — drag to circle, scroll to zoom" },
      { value: "look", label: "👁 Look — orbit plus drag to look around freely" },
      { value: "fly", label: "🚀 Fly — WASD + drag, scroll = dolly (dive in to start the journey!)" },
      { value: "hole", label: "🕳 Move the hole — drag repositions it, scroll dollies it" }
    ], TUNE.camMode, v => setCamMode(v));
    fCam.slider("Fly speed", 3, 60, 1, TUNE.flySpeed, v => { TUNE.flySpeed = v; });
    fCam.note("In <b>Fly</b> mode, touch the event horizon (fly in close) to begin the descent automatically.");
    fCam.slider("Field of view", 35, 100, 1, TUNE.baseFov, v => { TUNE.baseFov = v; if (J.phase === "idle") TUNE.fov = v; });
    fCam.slider("Auto-orbit (deg/s)", 0, 25, 0.5, TUNE.orbitSpeed, v => { TUNE.orbitSpeed = v; });
    fCam.slider("Drag sensitivity", 0.2, 2.5, 0.05, TUNE.sensitivity, v => { TUNE.sensitivity = v; });
    fCam.buttonRow([
      { label: "Reset view", onClick: () => {
        orbit.tTheta = -0.8; orbit.tPhi = 1.22; orbit.tRadius = P.rs * 11;
        holePos.copyFrom(orbitCenter); look.tYaw = look.tPitch = 0;
      } },
      { label: "Photon ring zoom", onClick: () => { if (J.phase === "idle") orbit.tRadius = P.rs * 2.35; } }
    ]);

    const fQ = panel.folder("⚙ Quality", false);
    fQ.slider("Integration steps", 90, 420, 10, TUNE.steps, v => { TUNE.steps = v; });
    fQ.slider("Step scale", 0.2, 0.7, 0.01, TUNE.dtScale, v => { TUNE.dtScale = v; });
    fQ.slider("Render scale", 0.5, 1.25, 0.05, TUNE.resScale, v => {
      TUNE.resScale = v;
      engine.setHardwareScalingLevel(1 / v);
    });

    const fAudio = panel.folder("🔊 Ambience", false);
    fAudio.toggle("Ambient drone", TUNE.audio, v => { TUNE.audio = v; AudioSys.setPower(v); });
    fAudio.slider("Volume", 0, 1, 0.01, TUNE.volume, v => { TUNE.volume = v; AudioSys.setVolume(v); });

    function updateStatus() {
      const phaseTxt = {
        idle: "in orbit", approach: "descending…", warpOut: "crossing the veil…", warpIn: "arriving…",
        travel: "travelling…", house: "walking…", switching: "…", warpInThenIdle: "rematerializing…",
        orbitPause: "circling a new anomaly…"
      }[J.phase] || J.phase;
      handles.jStatus.set(DIM_NAMES[J.dim] + " · " + phaseTxt);
    }
    setInterval(updateStatus, 400);
  }

  /* -------------------------------------------------------- snapshots --- */
  const SNAP_KEY = "singularity_snapshots_v1";
  function readSnaps() {
    try { return JSON.parse(localStorage.getItem(SNAP_KEY) || "[]"); } catch (e) { return []; }
  }
  function refreshSnapSelect() {
    const snaps = readSnaps();
    const sel = handles.snapSel.el;
    sel.innerHTML = "";
    const d = document.createElement("option"); d.value = "—"; d.textContent = snaps.length ? "choose…" : "—"; sel.appendChild(d);
    snaps.forEach(s => {
      const o = document.createElement("option");
      o.value = s.name; o.textContent = s.name;
      sel.appendChild(o);
    });
  }
  function saveSnapshot() {
    const name = (prompt("Name this singularity:", "My black hole") || "").trim();
    if (!name) return;
    const snaps = readSnaps().filter(s => s.name !== name);
    const data = {};
    for (const k of PRESET_KEYS) data[k] = P[k];
    snaps.push({ name, params: data });
    try { localStorage.setItem(SNAP_KEY, JSON.stringify(snaps)); } catch (e) { /* storage full */ }
    refreshSnapSelect();
  }
  function loadSnapshot() {
    const name = handles.snapSel.el.value;
    const s = readSnaps().find(x => x.name === name);
    if (s) applyParams(s.params, 1.5);
  }
  function deleteSnapshot() {
    const name = handles.snapSel.el.value;
    try { localStorage.setItem(SNAP_KEY, JSON.stringify(readSnaps().filter(x => x.name !== name))); } catch (e) {}
    refreshSnapSelect();
  }

  window.addEventListener("DOMContentLoaded", () => {
    // after panel exists, seed input element needs wiring (created in buildPanel)
    const iv = setInterval(() => {
      if (handles.seed) {
        handles.seedEl = { input: handles.seed.row.querySelector("input") };
        clearInterval(iv);
        refreshSnapSelect();
      }
    }, 100);
  });
})();
