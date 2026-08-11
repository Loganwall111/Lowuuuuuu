/* ---------------------------------------------------------------------------
 * Ocean Worlds — realtime water physics sandbox (Babylon.js / WebGL2)
 * Gerstner ocean + buoyancy + currents + whirlpools + rain + cloth + drag&drop
 * ------------------------------------------------------------------------- */
(function () {
  const { clamp, lerp } = UTILS;
  let engine, scene, camera, flyCam, sun, hemi, shadowGen, pipeline;
  let oceanMat, skyMat, oceanMesh, skyMesh, terrainMesh, heightTex, heightSampler;
  let waves, floaters, noise;
  let P, panel, canvas;
  let simTime = 0, skyTime = 0;
  const handles = {};

  const c3cache = new Map();
  function C3(hex) {
    let c = c3cache.get(hex);
    if (!c) { c = BABYLON.Color3.FromHexString(hex); c3cache.set(hex, c); }
    return c;
  }

  function fatal(msg) {
    const d = document.getElementById("error");
    d.style.display = "flex";
    d.querySelector("p").textContent = msg;
  }

  window.addEventListener("DOMContentLoaded", init);

  /* ------------------------------------------------------- whirlpools -- */
  const whirls = [];       // {x, z, strength, radius, mesh}
  let whirlArmed = false;
  const whirlDefaults = { strength: 1.6, radius: 7 };
  function addWhirl(x, z, s, r) {
    if (whirls.length >= 4) {
      const old = whirls.shift();
      if (old.mesh) old.mesh.dispose();
    }
    const w = { x, z, strength: s !== undefined ? s : whirlDefaults.strength, radius: r !== undefined ? r : whirlDefaults.radius };
    const mesh = BABYLON.MeshBuilder.CreateCylinder("whirlHole", { height: 0.16, diameterTop: w.radius * 0.62, diameterBottom: w.radius * 0.8, tessellation: 32 }, scene);
    const m = new BABYLON.StandardMaterial("whirlMat", scene);
    m.diffuseColor = new BABYLON.Color3(0.005, 0.012, 0.02);
    m.emissiveColor = new BABYLON.Color3(0.01, 0.02, 0.045);
    m.specularColor = new BABYLON.Color3(0, 0, 0);
    mesh.material = m;
    mesh.position.set(x, 0, z);
    mesh.isPickable = false;
    w.mesh = mesh;
    whirls.push(w);
    return w;
  }
  function clearWhirls() {
    whirls.forEach(w => { if (w.mesh) w.mesh.dispose(); });
    whirls.length = 0;
  }
  function whirlUniformArray() {
    const out = new Float32Array(16);
    whirls.forEach((w, i) => {
      out[i * 4] = w.x; out[i * 4 + 1] = w.z; out[i * 4 + 2] = w.strength; out[i * 4 + 3] = w.radius;
    });
    return out;
  }

  /* ------------------------------------------------------------- audio -- */
  const Ambience = {
    ctx: null, master: null, oceanG: null, rainG: null, on: false, rain: 0,
    ensure() {
      if (this.ctx) return;
      const AC = window.AudioContext || window.webkitAudioContext;
      if (!AC) return;
      const ctx = this.ctx = new AC();
      this.master = ctx.createGain(); this.master.gain.value = 0; this.master.connect(ctx.destination);
      const mkNoise = () => {
        const len = ctx.sampleRate * 3;
        const buf = ctx.createBuffer(1, len, ctx.sampleRate);
        const ch = buf.getChannelData(0);
        let last = 0;
        for (let i = 0; i < len; i++) { const w = Math.random() * 2 - 1; last = (last + 0.02 * w) / 1.02; ch[i] = last * 3.0; }
        const src = ctx.createBufferSource(); src.buffer = buf; src.loop = true;
        return src;
      };
      // ocean bed
      {
        const src = mkNoise();
        const lp = ctx.createBiquadFilter(); lp.type = "lowpass"; lp.frequency.value = 320;
        this.oceanG = ctx.createGain(); this.oceanG.gain.value = 0.5;
        const lfo = ctx.createOscillator(); lfo.frequency.value = 0.13;
        const lg = ctx.createGain(); lg.gain.value = 0.16;
        lfo.connect(lg); lg.connect(this.oceanG.gain); lfo.start();
        src.connect(lp); lp.connect(this.oceanG); this.oceanG.connect(this.master); src.start();
      }
      // rain hiss
      {
        const src = mkNoise();
        const bp = ctx.createBiquadFilter(); bp.type = "bandpass"; bp.frequency.value = 2600; bp.Q.value = 0.6;
        this.rainG = ctx.createGain(); this.rainG.gain.value = 0;
        src.connect(bp); bp.connect(this.rainG); this.rainG.connect(this.master); src.start();
      }
    },
    setPower(on) {
      this.on = on;
      if (on) { this.ensure(); if (!this.ctx) return; this.ctx.resume(); this.master.gain.setTargetAtTime(0.55, this.ctx.currentTime, 0.4); }
      else if (this.ctx) this.master.gain.setTargetAtTime(0, this.ctx.currentTime, 0.25);
    },
    setRain(v) {
      this.rain = v;
      if (this.ctx && this.on) this.rainG.gain.setTargetAtTime(v * 0.5, this.ctx.currentTime, 0.3);
    },
    thunder(delay) {
      if (!this.ctx || !this.on) return;
      const ctx = this.ctx;
      const t0 = ctx.currentTime + delay;
      const dur = 2.6;
      const buf = ctx.createBuffer(1, ctx.sampleRate * dur, ctx.sampleRate);
      const ch = buf.getChannelData(0);
      let last = 0;
      for (let i = 0; i < ch.length; i++) {
        const w = Math.random() * 2 - 1;
        last = (last + 0.03 * w) / 1.03;
        const env = Math.exp(-3.2 * i / ch.length) * Math.min(1, i / (ctx.sampleRate * 0.06));
        ch[i] = last * 3.5 * env;
      }
      const src = ctx.createBufferSource(); src.buffer = buf;
      const lp = ctx.createBiquadFilter(); lp.type = "lowpass";
      lp.frequency.setValueAtTime(420, t0);
      lp.frequency.exponentialRampToValueAtTime(65, t0 + dur);
      const gg = ctx.createGain(); gg.gain.setValueAtTime(0.9, t0);
      src.connect(lp); lp.connect(gg); gg.connect(this.master);
      src.start(t0);
    }
  };

  /* --------------------------------------------------------- lightning -- */
  const storm = { flash: 0, nextAt: 8 };

  /* --------------------------------------------------------------- rain -- */
  let rainPS = null;
  function buildRain() {
    const tex = new BABYLON.DynamicTexture("rainTex", { width: 16, height: 64 }, scene, true);
    const c = tex.getContext();
    const g = c.createLinearGradient(0, 0, 0, 64);
    g.addColorStop(0, "rgba(200,225,255,0)");
    g.addColorStop(0.5, "rgba(210,235,255,0.85)");
    g.addColorStop(1, "rgba(200,225,255,0)");
    c.fillStyle = g; c.fillRect(6, 0, 4, 64);
    tex.update(); tex.hasAlpha = true;
    rainPS = new BABYLON.ParticleSystem("rain", 5000, scene);
    rainPS.particleTexture = tex;
    rainPS.emitter = new BABYLON.Vector3(0, 0, 0);
    rainPS.minEmitBox = new BABYLON.Vector3(-38, 16, -38);
    rainPS.maxEmitBox = new BABYLON.Vector3(38, 24, 38);
    rainPS.minLifeTime = 1.1; rainPS.maxLifeTime = 1.6;
    rainPS.minSize = 0.5; rainPS.maxSize = 1.0;
    rainPS.minEmitPower = 24; rainPS.maxEmitPower = 30;
    rainPS.updateSpeed = 0.012;
    rainPS.direction1 = new BABYLON.Vector3(-1.5, -1, -1.5);
    rainPS.direction2 = new BABYLON.Vector3(1.5, -1, 1.5);
    rainPS.color1 = new BABYLON.Color4(0.75, 0.85, 1, 0.32);
    rainPS.color2 = new BABYLON.Color4(0.8, 0.9, 1, 0.4);
    rainPS.colorDead = new BABYLON.Color4(0.8, 0.9, 1, 0);
    rainPS.blendMode = BABYLON.ParticleSystem.BLENDMODE_STANDARD;
  }

  /* --------------------------------------------------------------- init -- */
  function init() {
    if (!window.BABYLON) {
      fatal("Could not load Babylon.js from the CDN. Check your internet connection and reload.");
      return;
    }
    WATER_GLSL.install();
    canvas = document.getElementById("renderCanvas");
    engine = new BABYLON.Engine(canvas, true, {
      antialias: true, stencil: false, powerPreference: "high-performance", doNotHandleContextLost: false
    });
    if (engine.webGLVersion < 2) document.getElementById("gl1warn").style.display = "block";
    scene = new BABYLON.Scene(engine);
    scene.clearColor = new BABYLON.Color4(0.5, 0.65, 0.8, 1);
    scene.fogMode = BABYLON.Scene.FOGMODE_EXP2;

    camera = new BABYLON.ArcRotateCamera("cam", -1.35, 1.12, 115, new BABYLON.Vector3(0, 4, 0), scene);
    camera.attachControl(canvas, true);
    camera.panningSensibility = 0;
    camera.wheelDeltaPercentage = 0.02;
    camera.lowerRadiusLimit = 26;
    camera.upperRadiusLimit = 320;
    camera.upperBetaLimit = 1.535;
    camera.lowerBetaLimit = 0.12;
    camera.minZ = 0.5;
    camera.maxZ = 5000;

    hemi = new BABYLON.HemisphericLight("hemi", new BABYLON.Vector3(0, 1, 0), scene);
    hemi.groundColor = new BABYLON.Color3(0.16, 0.2, 0.28);
    hemi.intensity = 0.6;

    sun = new BABYLON.DirectionalLight("sun", new BABYLON.Vector3(0, -1, 0.3), scene);
    sun.position = new BABYLON.Vector3(0, 400, -150);
    shadowGen = new BABYLON.ShadowGenerator(2048, sun);
    shadowGen.useBlurExponentialShadowMap = true;
    shadowGen.blurKernel = 24;
    shadowGen.autoCalcDepthBounds = true;
    shadowGen.setDarkness(0.45);

    buildSkyAndOcean();
    buildRain();

    waves = new PHYS.WaveSet();
    floaters = new PHYS.Floaters(scene, shadowGen);

    pipeline = new BABYLON.DefaultRenderingPipeline("rp", true, scene, [camera]);
    pipeline.samples = 4;
    pipeline.fxaaEnabled = true;
    pipeline.bloomEnabled = true;
    pipeline.imageProcessing.toneMappingEnabled = true;
    pipeline.imageProcessing.toneMappingType = BABYLON.ImageProcessingConfiguration.TONEMAPPING_ACES;

    // ---- live parameters ----
    P = {
      planetId: "terran",
      time: 10.6, dayCycle: false, cycleSpeed: 0.18,
      ocean: { amp: 1.0, len: 46, chop: 0.5, speed: 1.0, count: 12, dir: 0.8 },
      flow: { strength: 0, dir: 1.2 },
      water: { deep: "#07304a", shallow: "#1ba5ac", foam: "#eef7ff", clarity: 0.14, foamAmt: 1.0, detail: 0.55, refl: 1.0, glow: 0, glowColor: "#39f0ff", lava: 0 },
      sky: { horizon: "#a9cbe6", zenith: "#2f6db2", sunTint: "#fff1d8", cloud: 0.30, cloudScale: 1.0, aurora: 0, star: 1.0, fog: 0.0012, fogColor: "#a7c4de", planetDir: [0.4, 0.4, -0.6], planetSize: 0, planetA: "#c9ae84", planetB: "#7c6750" },
      gravity: 9.81, buoyancy: 1.06, splashes: true, splashScale: 1.0,
      seaLevel: 0, floodRate: 0, drainRate: 0,
      rain: 0, thunder: false, ambience: false,
      paused: false, slowmo: 1.0,
      exposure: 1.12, bloom: 0.5, vignette: 0.55, fxaa: true, glitter: 1.0,
      renderScale: 1.0, shadows: true, spawnKind: "crate", autoOrbit: false, fov: 0.9,
      camMode: "orbit", flySpeed: 26
    };

    buildPanel();
    applyPlanet("terran", true);
    wirePointer();
    wireKeyboard();

    window.addEventListener("wheel", (e) => {
      if (grab) { grab.planeY = clamp(grab.planeY + e.deltaY * 0.012, -3, 26); e.preventDefault(); }
      else if (P.camMode === "fly" && flyCam && document.activeElement !== canvas) { /* handled below */ }
    }, { passive: false });
    canvas.addEventListener("wheel", (e) => {
      if (P.camMode === "fly" && flyCam) {
        const fwd = flyCam.getDirection(BABYLON.Vector3.Forward());
        flyCam.position.addInPlace(fwd.scale(-e.deltaY * 0.02 * (P.flySpeed * 0.12)));
        e.preventDefault();
      }
    }, { passive: false });

    scene.registerBeforeRender(tick);

    engine.runRenderLoop(() => scene.render());
    window.addEventListener("resize", () => engine.resize());

    let fpsTick = 0;
    scene.registerAfterRender(() => {
      if (++fpsTick % 30 === 0 && panel) {
        panel.setFooterRight(engine.getFps().toFixed(0) + " fps · " + floaters.list.length + " floaters · " + whirls.length + " whirlpools");
      }
    });

    document.getElementById("loading").style.display = "none";
  }

  /* ------------------------------------------------------- frame tick --- */
  let rippleTimer = 0;
  function tick() {
    const rawDt = Math.min(engine.getDeltaTime() / 1000, 0.05);
    skyTime += rawDt;
    const dt = rawDt * P.slowmo;
    if (!P.paused) simTime += dt;
    if (P.dayCycle) {
      P.time = (P.time + rawDt * P.cycleSpeed) % 24;
      if (handles.time) handles.time.set(P.time);
    }
    // flood & drain
    if (P.floodRate > 0 || P.drainRate > 0) {
      P.seaLevel = clamp(P.seaLevel + (P.floodRate - P.drainRate) * rawDt, -8, 10);
      if (handles.seaLevel) handles.seaLevel.set(P.seaLevel);
    }
    if (oceanMesh.position.y !== P.seaLevel) oceanMesh.position.y = P.seaLevel;
    // thunder scheduler
    if (P.thunder && P.rain > 0.15) {
      storm.nextAt -= rawDt;
      if (storm.nextAt <= 0) {
        storm.nextAt = 4 + Math.random() * 9;
        storm.flash = 1;
        Ambience.thunder(0.4 + Math.random() * 1.4);
      }
    }
    storm.flash = Math.max(0, storm.flash - rawDt * 2.4);
    updateSun();
    flyTick(rawDt);
    // rain follows the camera
    const cam = scene.activeCamera;
    if (P.rain > 0.02) {
      if (!rainPS.isStarted()) rainPS.start();
      const fwd = cam.getForwardRay ? cam.getForwardRay(12).direction : BABYLON.Vector3.Forward();
      rainPS.emitter = new BABYLON.Vector3(cam.globalPosition.x + fwd.x * 14, cam.globalPosition.y + 6, cam.globalPosition.z + fwd.z * 14);
      rainPS.emitRate = P.rain * 1500;
      // ripples
      rippleTimer -= rawDt;
      if (rippleTimer <= 0) {
        rippleTimer = 0.14 / Math.max(P.rain, 0.1);
        const a = Math.random() * Math.PI * 2, r = Math.random() * 42;
        const tx = cam.globalPosition.x + fwd.x * 20 + Math.cos(a) * r;
        const tz = cam.globalPosition.z + fwd.z * 20 + Math.sin(a) * r;
        if (!heightSampler || heightSampler(tx, tz) < P.seaLevel) {
          const y = P.seaLevel + waves.height(tx, tz, simTime) - PHYS.whirlDip(tx, tz, whirls);
          floaters.ringOnly(new BABYLON.Vector3(tx, y + 0.05, tz), 0.35 + Math.random() * 0.45);
        }
      }
    } else if (rainPS.isStarted()) {
      rainPS.stop();
    }
    Ambience.setRain(P.rain);
    // whirlpool drain discs ride the surface
    for (const w of whirls) {
      if (w.mesh) w.mesh.position.y = P.seaLevel - w.strength * 0.52 + waves.height(w.x, w.z, simTime) * 0.2;
    }
    // physics
    floaters.allowFx = P.splashes;
    floaters.splashScale = P.splashScale;
    const windX = Math.cos(waves.dirAngle) * P.ocean.speed * 0.4;
    const windZ = Math.sin(waves.dirAngle) * P.ocean.speed * 0.4;
    floaters.step(dt, waves, simTime, {
      gravity: P.gravity, buoyancy: P.buoyancy, seaLevel: P.seaLevel,
      heightAt: heightSampler,
      wind: { x: windX, y: windZ },
      current: { x: Math.cos(P.flow.dir) * P.flow.strength, y: Math.sin(P.flow.dir) * P.flow.strength },
      whirls
    });
    // cloth
    const waterFn = (x, z) => P.seaLevel + waves.height(x, z, simTime) - PHYS.whirlDip(x, z, whirls);
    for (const b of floaters.list) {
      if (b.cloth) b.cloth.step(dt, windX * 2.6, windZ * 2.6, 0.8 + P.ocean.speed * 0.4, waterFn);
    }
    pushUniforms();
  }

  function flyTick(dt) {
    if (P.camMode !== "fly" || !flyCam || !scene.activeCamera || scene.activeCamera !== flyCam) return;
    const spd = P.flySpeed * (keys.has("shift") ? 3 : 1) * dt;
    const mv = new BABYLON.Vector3(0, 0, 0);
    if (keys.has("w")) mv.addInPlace(flyCam.getDirection(BABYLON.Vector3.Forward()));
    if (keys.has("s")) mv.addInPlace(flyCam.getDirection(BABYLON.Vector3.Backward()));
    if (keys.has("a")) mv.addInPlace(flyCam.getDirection(BABYLON.Vector3.Left()));
    if (keys.has("d")) mv.addInPlace(flyCam.getDirection(BABYLON.Vector3.Right()));
    if (keys.has("e") || keys.has(" ")) mv.y += 1;
    if (keys.has("q") || keys.has("control")) mv.y -= 1;
    if (mv.lengthSquared() > 0) flyCam.position.addInPlace(mv.normalize().scale(spd));
  }

  function setCamMode(mode) {
    P.camMode = mode;
    if (mode === "fly") {
      if (!flyCam) {
        flyCam = new BABYLON.FreeCamera("fly", camera.globalPosition.clone(), scene);
        flyCam.minZ = 0.3; flyCam.maxZ = 5000;
        flyCam.angularSensibility = 2600;
        flyCam.keysUp = []; flyCam.keysDown = []; flyCam.keysLeft = []; flyCam.keysRight = [];
        flyCam.inertia = 0.4;
      }
      flyCam.position.copyFrom(camera.globalPosition);
      flyCam.setTarget(camera.target.clone());
      camera.detachControl();
      scene.activeCamera = flyCam;
      pipeline.cameras = [flyCam];
      flyCam.attachControl(canvas, true);
    } else {
      if (flyCam) flyCam.detachControl();
      scene.activeCamera = camera;
      pipeline.cameras = [camera];
      camera.attachControl(canvas, true);
    }
  }

  /* ------------------------------------------------ sky + ocean build --- */
  const SKY_UNIFORM_NAMES = ["uSunDir", "uSunTint", "uHorizonTint", "uZenithTint", "uCloudCover", "uCloudScale",
    "uSkyT", "uWindSky", "uStarBoost", "uAurora", "uPlanetDir", "uPlanetSize", "uPlanetA", "uPlanetB"];

  function buildSkyAndOcean() {
    skyMesh = BABYLON.MeshBuilder.CreateSphere("sky", { diameter: 3200, segments: 16, sideOrientation: BABYLON.Mesh.DOUBLESIDE }, scene);
    skyMesh.infiniteDistance = true;
    skyMesh.isPickable = false;
    skyMesh.applyFog = false;
    skyMat = new BABYLON.ShaderMaterial("skyMat", scene, { vertex: "sky", fragment: "sky" }, {
      attributes: ["position"],
      uniforms: ["worldViewProjection"].concat(SKY_UNIFORM_NAMES)
    });
    skyMat.backFaceCulling = false;
    skyMat.disableDepthWrite = true;
    skyMesh.material = skyMat;
    skyMesh.renderingGroupId = 0;

    oceanMesh = BABYLON.MeshBuilder.CreateGround("ocean", { width: 1500, height: 1500, subdivisions: 256 }, scene);
    oceanMesh.alwaysSelectAsActiveMesh = true;
    oceanMesh.applyFog = false;
    oceanMat = new BABYLON.ShaderMaterial("oceanMat", scene, { vertex: "ocean", fragment: "ocean" }, {
      attributes: ["position"],
      uniforms: ["worldViewProjection", "uTime", "uWaveData", "uWaveData2", "uWaveCount", "uChop", "uAmpSum",
        "uCamPos", "uDeepColor", "uShallowColor", "uFoamColor", "uClarity", "uFoamAmt", "uSeaLevel",
        "uMapHalf", "uHMin", "uHScale", "uLava", "uGlow", "uGlowColor", "uFogDensity", "uFogColor",
        "uDetailK", "uReflGain", "uGlitter", "uWindWater", "uFlow", "uWhirl"].concat(SKY_UNIFORM_NAMES),
      samplers: ["tHeight"]
    });
    oceanMat.backFaceCulling = false;
    oceanMat.alpha = 0.999;
    oceanMat.alphaMode = BABYLON.Engine.ALPHA_COMBINE;
    oceanMat.disableDepthWrite = true;
    oceanMesh.material = oceanMat;
  }

  function pushSkyUniforms(mat) {
    mat.setVector3("uSunDir", sunDirVec());
    mat.setColor3("uSunTint", C3(P.sky.sunTint));
    mat.setColor3("uHorizonTint", C3(P.sky.horizon));
    mat.setColor3("uZenithTint", C3(P.sky.zenith));
    mat.setFloat("uCloudCover", P.sky.cloud);
    mat.setFloat("uCloudScale", P.sky.cloudScale);
    mat.setFloat("uSkyT", skyTime);
    mat.setVector2("uWindSky", new BABYLON.Vector2(Math.cos(waves.dirAngle), Math.sin(waves.dirAngle)));
    mat.setFloat("uStarBoost", P.sky.star);
    mat.setFloat("uAurora", P.sky.aurora);
    mat.setVector3("uPlanetDir", BABYLON.Vector3.FromArray(P.sky.planetDir).normalize());
    mat.setFloat("uPlanetSize", P.sky.planetSize);
    mat.setColor3("uPlanetA", C3(P.sky.planetA));
    mat.setColor3("uPlanetB", C3(P.sky.planetB));
  }

  function pushUniforms() {
    const w = P.water;
    pushSkyUniforms(oceanMat);
    pushSkyUniforms(skyMat);
    oceanMat.setFloat("uTime", simTime);
    oceanMat.setVector3("uCamPos", scene.activeCamera.globalPosition);
    oceanMat.setColor3("uDeepColor", C3(w.deep));
    oceanMat.setColor3("uShallowColor", C3(w.shallow));
    oceanMat.setColor3("uFoamColor", C3(w.foam));
    oceanMat.setFloat("uClarity", w.clarity);
    oceanMat.setFloat("uFoamAmt", w.foamAmt);
    oceanMat.setFloat("uSeaLevel", P.seaLevel);
    oceanMat.setFloat("uMapHalf", WORLD.SIZE / 2);
    oceanMat.setFloat("uHMin", WORLD.HMIN);
    oceanMat.setFloat("uHScale", WORLD.HMAX - WORLD.HMIN);
    oceanMat.setFloat("uLava", w.lava);
    oceanMat.setFloat("uGlow", w.glow);
    oceanMat.setColor3("uGlowColor", C3(w.glowColor));
    oceanMat.setFloat("uFogDensity", P.sky.fog * 3.0);
    oceanMat.setColor3("uFogColor", C3(P.sky.fogColor));
    oceanMat.setFloat("uDetailK", w.detail);
    oceanMat.setFloat("uReflGain", w.refl);
    oceanMat.setFloat("uGlitter", P.glitter);
    oceanMat.setVector2("uWindWater", new BABYLON.Vector2(Math.cos(waves.dirAngle), Math.sin(waves.dirAngle)));
    oceanMat.setVector2("uFlow", new BABYLON.Vector2(
      Math.cos(P.flow.dir) * P.flow.strength * 0.9,
      Math.sin(P.flow.dir) * P.flow.strength * 0.9));
    oceanMat.setArray("uWhirl", whirlUniformArray());
    // film
    pipeline.imageProcessing.exposure = P.exposure;
    pipeline.imageProcessing.contrast = 1.05;
    pipeline.bloomWeight = P.bloom * 0.8;
    pipeline.bloomThreshold = 0.82;
    pipeline.fxaaEnabled = P.fxaa;
    pipeline.imageProcessing.vignetteEnabled = P.vignette > 0.001;
    pipeline.imageProcessing.vignetteWeight = P.vignette * 2.2;
    scene.fogDensity = P.sky.fog;
    scene.fogColor = C3(P.sky.fogColor);
    scene.activeCamera.fov = P.camMode === "fly" ? P.fov : P.fov;
    camera.fov = P.fov;
    if (P.camMode === "orbit") {
      if (P.autoOrbit && !camera.useAutoRotationBehavior) camera.useAutoRotationBehavior = true;
      if (!P.autoOrbit && camera.useAutoRotationBehavior) camera.useAutoRotationBehavior = false;
    }
  }

  let _sunDir = new BABYLON.Vector3(0, 1, 0);
  function sunDirVec() { return _sunDir; }
  function updateSun() {
    const t = P.time;
    const az = (t / 24) * Math.PI * 2 + 0.9;
    const el = Math.sin(((t - 6) / 12) * Math.PI) * 1.02;
    const ce = Math.cos(el);
    _sunDir.copyFromFloats(Math.cos(az) * ce, Math.sin(el), Math.sin(az) * ce).normalize();
    sun.direction = _sunDir.scale(-1);
    sun.position = _sunDir.scale(420);
    const dayAmt = UTILS.smooth(-0.06, 0.24, _sunDir.y);
    const flash = storm.flash * storm.flash;
    sun.intensity = clamp(_sunDir.y * 1.6 + 0.05, 0, 1.25) + flash * 3.5;
    hemi.intensity = 0.18 + 0.5 * Math.max(_sunDir.y, 0) + 0.06 * (1 - dayAmt) * P.sky.star * 0.4 + flash * 1.6;
    hemi.diffuse = BABYLON.Color3.Lerp(new BABYLON.Color3(0.25, 0.3, 0.45), C3(P.sky.zenith), 0.55 + 0.45 * dayAmt);
    const clear = BABYLON.Color3.Lerp(new BABYLON.Color3(0.012, 0.02, 0.05), C3(P.sky.horizon), dayAmt);
    scene.clearColor = new BABYLON.Color4(
      Math.min(1, clear.r + flash * 0.8), Math.min(1, clear.g + flash * 0.85), Math.min(1, clear.b + flash), 1);
    if (shadowGen) shadowGen.setDarkness(lerp(0.85, 0.4, dayAmt));
  }

  /* ------------------------------------------------------- planets ------ */
  function rebuildWaves() {
    waves.build({ count: P.ocean.count, amp: P.ocean.amp, len: P.ocean.len, chop: P.ocean.chop, speed: P.ocean.speed, dir: P.ocean.dir, seed: (WORLD.PLANETS.find(p => p.id === P.planetId) || {}).seed || 5 });
    oceanMat.setArray("uWaveData", waves.a);
    oceanMat.setArray("uWaveData2", waves.b);
    oceanMat.setInt("uWaveCount", waves.N);
    oceanMat.setFloat("uChop", waves.chop);
    oceanMat.setFloat("uAmpSum", waves.ampSum);
  }

  function applyPlanet(id, first) {
    const planet = WORLD.PLANETS.find(p => p.id === id) || WORLD.PLANETS[0];
    P.planetId = planet.id;
    noise = WORLD.makeNoise2D(planet.seed);
    P.sky.horizon = planet.sky.horizon; P.sky.zenith = planet.sky.zenith; P.sky.sunTint = planet.sky.sunTint;
    P.sky.cloud = planet.sky.cloud; P.sky.cloudScale = planet.sky.cloudScale;
    P.sky.aurora = planet.sky.aurora; P.sky.star = planet.sky.star;
    P.sky.fog = planet.sky.fog; P.sky.fogColor = planet.sky.fogColor;
    if (planet.sky.planet) {
      P.sky.planetDir = planet.sky.planet.dir; P.sky.planetSize = planet.sky.planet.size;
      P.sky.planetA = planet.sky.planet.a; P.sky.planetB = planet.sky.planet.b;
    } else {
      P.sky.planetSize = 0;
    }
    P.water.deep = planet.water.deep; P.water.shallow = planet.water.shallow; P.water.foam = planet.water.foam;
    P.water.clarity = planet.water.clarity; P.water.foamAmt = planet.water.foam; P.water.detail = planet.water.detail;
    P.water.refl = planet.water.refl; P.water.glow = planet.water.glow; P.water.glowColor = planet.water.glowColor;
    P.water.lava = planet.water.lava ? 1 : 0;
    P.ocean.amp = planet.ocean.amp; P.ocean.len = planet.ocean.len; P.ocean.chop = planet.ocean.chop;
    P.ocean.speed = planet.ocean.speed; P.ocean.count = planet.ocean.count; P.ocean.dir = planet.ocean.dir;
    P.time = planet.time;
    if (terrainMesh) terrainMesh.dispose();
    if (heightTex) heightTex.dispose();
    terrainMesh = WORLD.buildTerrain(scene, planet.terrain, noise);
    const terrMat = new BABYLON.StandardMaterial("terrMat", scene);
    terrMat.diffuseColor = new BABYLON.Color3(1, 1, 1);
    terrMat.specularColor = new BABYLON.Color3(0.03, 0.03, 0.03);
    try { terrMat.useVertexColor = true; } catch (e) { }
    terrainMesh.material = terrMat;
    terrainMesh.receiveShadows = true;
    heightTex = WORLD.buildHeightTexture(scene, planet.terrain, noise);
    oceanMat.setTexture("tHeight", heightTex);
    heightSampler = (x, z) => WORLD.heightAt(planet.terrain, noise, x, z);
    rebuildWaves();
    clearWhirls();
    floaters.clear();
    const spawnSafe = (kind, r, a) => {
      const x = Math.cos(a) * r, z = Math.sin(a) * r;
      floaters.spawn(kind, new BABYLON.Vector3(x, 6, z));
    };
    spawnSafe("crate", 46, 0.4); spawnSafe("crate", 52, 2.3); spawnSafe("ball", 40, 1.4);
    spawnSafe("barrel", 58, 3.6); spawnSafe("boat", 70, 5.2);
    if (planet.spawnBergs) for (let i = 0; i < 5; i++) spawnSafe("berg", 90 + i * 22, i * 1.35);
    refreshPanel();
  }

  function refreshPanel() {
    const h = handles;
    if (!h.planet) return;
    h.planet.set(P.planetId);
    h.time.set(P.time);
    h.amp.set(P.ocean.amp); h.len.set(P.ocean.len); h.chop.set(P.ocean.chop); h.speed.set(P.ocean.speed);
    h.count.set(P.ocean.count); h.dir.set(P.ocean.dir);
    h.flowS.set(P.flow.strength); h.flowD.set(P.flow.dir);
    h.deep.set(P.water.deep); h.shallow.set(P.water.shallow); h.foamC.set(P.water.foam);
    h.clarity.set(P.water.clarity); h.foamAmt.set(P.water.foamAmt); h.detail.set(P.water.detail);
    h.refl.set(P.water.refl); h.glow.set(P.water.glow); h.lava.set(!!P.water.lava);
    h.horizon.set(P.sky.horizon); h.zenith.set(P.sky.zenith); h.sunTint.set(P.sky.sunTint);
    h.cloud.set(P.sky.cloud); h.cloudScale.set(P.sky.cloudScale); h.aurora.set(P.sky.aurora);
    h.star.set(P.sky.star); h.fog.set(P.sky.fog);
    if (h.planetSize) h.planetSize.set(P.sky.planetSize);
    h.seaLevel.set(P.seaLevel);
    h.rain.set(P.rain);
    h.thunder.set(P.thunder);
  }

  /* ------------------------------------------------------- scenarios ---- */
  function applyScenario(name) {
    const set = (o) => { Object.assign(P.ocean, o); rebuildWaves(); };
    switch (name) {
      case "zen":
        set({ amp: 0.3, len: 20, chop: 0.3, speed: 0.7, count: 9 });
        P.sky.cloud = 0.15; P.rain = 0; P.thunder = false; P.floodRate = 0; P.drainRate = 0; P.flow.strength = 0;
        clearWhirls();
        break;
      case "storm":
        set({ amp: 2.4, len: 78, chop: 0.85, speed: 1.4, count: 15 });
        P.sky.cloud = 0.95; P.rain = 1; P.thunder = true; P.flow.strength = 0.8; P.time = 18.4;
        break;
      case "flood":
        set({ amp: 1.2, len: 40, chop: 0.5, speed: 1.0, count: 12 });
        P.rain = 0.9; P.sky.cloud = 0.8; P.floodRate = 0.3; P.drainRate = 0; P.thunder = false;
        break;
      case "drain":
        P.drainRate = 0.35; P.floodRate = 0; P.rain = 0;
        addWhirl(0, 0, 2.4, 8);
        break;
      case "whirlpools":
        clearWhirls();
        addWhirl(-35, -20, 1.8, 7);
        addWhirl(40, 15, 2.2, 9);
        addWhirl(5, 60, 1.4, 6);
        set({ amp: 1.0, len: 40, chop: 0.5, speed: 1.0 });
        break;
      case "regatta":
        floaters.clear();
        ["boat", "boat", "boat", "duck", "duck", "duck", "buoy", "buoy", "ball", "crate"].forEach((k, i) => {
          const a = i * 0.63, r = 40 + (i % 5) * 14;
          floaters.spawn(k, new BABYLON.Vector3(Math.cos(a) * r, 8, Math.sin(a) * r));
        });
        set({ amp: 0.7, len: 30, chop: 0.45, speed: 1.0 });
        break;
    }
    refreshPanel();
  }

  /* ------------------------------------------------------ drag & drop --- */
  let grab = null;
  const keys = new Set();
  function wireKeyboard() {
    window.addEventListener("keydown", (e) => {
      if (/input|select|textarea/i.test(document.activeElement.tagName)) return;
      keys.add(e.key.toLowerCase());
    });
    window.addEventListener("keyup", (e) => keys.delete(e.key.toLowerCase()));
    window.addEventListener("blur", () => keys.clear());
  }
  function rayPlaneY(x, y, planeY, out) {
    const ray = scene.createPickingRay(x, y, BABYLON.Matrix.Identity(), scene.activeCamera);
    const dy = ray.direction.y;
    if (Math.abs(dy) < 1e-6) return false;
    const t = (planeY - ray.origin.y) / dy;
    if (t < 0) return false;
    out.copyFrom(ray.origin).addInPlace(ray.direction.scale(t));
    return true;
  }
  function wirePointer() {
    scene.onPointerObservable.add((pi) => {
      if (pi.type === BABYLON.PointerEventTypes.POINTERDOWN) {
        const ev = pi.event;
        if (ev.button !== 0) return;
        // whirlpool placement mode
        if (whirlArmed) {
          const pick = scene.pick(ev.clientX, ev.clientY, (m) => m === oceanMesh || m === terrainMesh);
          if (pick && pick.hit && pick.pickedMesh === oceanMesh) {
            addWhirl(pick.pickedPoint.x, pick.pickedPoint.z);
            if (handles.whirlCount) handles.whirlCount.set(whirls.length + " active");
          }
          whirlArmed = false;
          if (handles.whirlBtn) handles.whirlBtn.el.textContent = "🌀 Arm whirlpool placement (click water)";
          return;
        }
        // grab a floater?
        const fpick = scene.pick(ev.clientX, ev.clientY, (m) => !!m._floatBody);
        if (fpick && fpick.hit && fpick.pickedMesh && fpick.pickedMesh._floatBody) {
          const body = fpick.pickedMesh._floatBody;
          grab = { body, planeY: body.node.position.y, vel: new BABYLON.Vector3(), lastPos: body.node.position.clone(), lastT: performance.now() };
          body.held = true;
          body.holdPos.copyFrom(body.node.position);
          if (P.camMode === "orbit") { camera.detachControl(); grab.cam = 0; }
          else { if (flyCam) flyCam.detachControl(); grab.cam = 1; }
          return;
        }
        // otherwise spawn on the ocean
        const pick = scene.pick(ev.clientX, ev.clientY, (m) => m === oceanMesh || m === terrainMesh);
        if (pick && pick.hit && pick.pickedMesh === oceanMesh && pick.pickedPoint) {
          const pt = pick.pickedPoint.clone();
          pt.y += 6;
          floaters.spawn(P.spawnKind, pt);
        }
      } else if (pi.type === BABYLON.PointerEventTypes.POINTERMOVE) {
        if (grab) {
          const ev = pi.event;
          const pt = new BABYLON.Vector3();
          if (rayPlaneY(ev.clientX, ev.clientY, grab.planeY, pt)) {
            const now = performance.now();
            const dt = Math.max((now - grab.lastT) / 1000, 1e-3);
            grab.vel.copyFrom(pt).subtractInPlace(grab.lastPos).scaleInPlace(1 / dt);
            grab.vel.x = clamp(grab.vel.x, -60, 60); grab.vel.y = clamp(grab.vel.y, -60, 60); grab.vel.z = clamp(grab.vel.z, -60, 60);
            grab.lastPos.copyFrom(pt);
            grab.lastT = now;
            grab.body.holdPos.copyFrom(pt);
          }
        }
      } else if (pi.type === BABYLON.PointerEventTypes.POINTERUP) {
        if (grab) {
          grab.body.held = false;
          grab.body.vel.copyFrom(grab.vel).scaleInPlace(0.32);
          if (grab.cam === 0) camera.attachControl(canvas, true);
          else if (grab.cam === 1 && flyCam) flyCam.attachControl(canvas, true);
          grab = null;
        }
      }
    });
  }

  /* ------------------------------------------------------------- GUI ---- */
  function buildPanel() {
    panel = XUI.createPanel("🌊 Ocean Worlds — Simulation Control", { accent: "#7fd4ff", footer: "Babylon.js · WebGL2 · Gerstner ocean" });

    const fPlanet = panel.folder("🪐 Planet & Scenarios", true);
    handles.planet = fPlanet.select("World preset", WORLD.PLANETS.map(p => ({ value: p.id, label: p.name })), P.planetId, v => applyPlanet(v));
    fPlanet.note("One-click scenarios:");
    fPlanet.buttonRow([
      { label: "🧘 Zen pond", onClick: () => applyScenario("zen") },
      { label: "⛈ Perfect storm", onClick: () => applyScenario("storm") },
      { label: "🌊 Flash flood", onClick: () => applyScenario("flood") }
    ]);
    fPlanet.buttonRow([
      { label: "🕳 Drain the sea", onClick: () => applyScenario("drain") },
      { label: "🌀 Whirlpool bay", onClick: () => applyScenario("whirlpools") },
      { label: "⛵ Regatta", onClick: () => applyScenario("regatta") }
    ]);
    handles.time = fPlanet.slider("Time of day", 0, 24, 0.05, P.time, v => { P.time = v; P.dayCycle = false; handles.dayCycle.set(false); });
    handles.dayCycle = fPlanet.toggle("Day/night cycle", P.dayCycle, v => { P.dayCycle = v; });
    fPlanet.slider("Cycle speed (h/s)", 0.02, 1.5, 0.01, P.cycleSpeed, v => { P.cycleSpeed = v; });

    const fOcean = panel.folder("🌊 Ocean Simulation", true);
    handles.amp = fOcean.slider("Wave height", 0, 3.2, 0.05, P.ocean.amp, v => { P.ocean.amp = v; rebuildWaves(); });
    handles.len = fOcean.slider("Wavelength", 8, 110, 1, P.ocean.len, v => { P.ocean.len = v; rebuildWaves(); });
    handles.chop = fOcean.slider("Choppiness", 0, 1, 0.02, P.ocean.chop, v => { P.ocean.chop = v; rebuildWaves(); });
    handles.speed = fOcean.slider("Wave speed", 0.05, 3, 0.05, P.ocean.speed, v => { P.ocean.speed = v; rebuildWaves(); });
    handles.count = fOcean.slider("Wave components", 3, 16, 1, P.ocean.count, v => { P.ocean.count = v; rebuildWaves(); });
    handles.dir = fOcean.slider("Swell direction", 0, 6.28, 0.02, P.ocean.dir, v => { P.ocean.dir = v; rebuildWaves(); });
    fOcean.sep();
    fOcean.note("Flowing water — a real current that carries floaters and advects foam & ripples.");
    handles.flowS = fOcean.slider("Current strength", 0, 4, 0.05, P.flow.strength, v => { P.flow.strength = v; });
    handles.flowD = fOcean.slider("Current direction", 0, 6.28, 0.02, P.flow.dir, v => { P.flow.dir = v; });

    const fWater = panel.folder("💧 Water Look", false);
    handles.deep = fWater.color("Deep color", P.water.deep, v => { P.water.deep = v; });
    handles.shallow = fWater.color("Shallow color", P.water.shallow, v => { P.water.shallow = v; });
    handles.foamC = fWater.color("Foam color", P.water.foam, v => { P.water.foam = v; });
    handles.clarity = fWater.slider("Clarity / absorption", 0.02, 0.5, 0.01, P.water.clarity, v => { P.water.clarity = v; });
    handles.foamAmt = fWater.slider("Foam amount", 0, 2.5, 0.05, P.water.foamAmt, v => { P.water.foamAmt = v; });
    handles.detail = fWater.slider("Ripple detail", 0, 1.6, 0.05, P.water.detail, v => { P.water.detail = v; });
    handles.refl = fWater.slider("Reflection gain", 0, 2, 0.05, P.water.refl, v => { P.water.refl = v; });
    fWater.slider("Sun glitter", 0, 2.5, 0.05, P.glitter, v => { P.glitter = v; });
    handles.glow = fWater.slider("Bioluminescence", 0, 3, 0.05, P.water.glow, v => { P.water.glow = v; });
    handles.lava = fWater.toggle("Molten lava mode", !!P.water.lava, v => { P.water.lava = v ? 1 : 0; });

    const fLevel = panel.folder("🌡 Sea Level — Flood & Drain", true);
    handles.seaLevel = fLevel.slider("Sea level", -8, 10, 0.05, P.seaLevel, v => { P.seaLevel = v; });
    fLevel.slider("Flood rate (auto-rise)", 0, 1, 0.01, P.floodRate, v => { P.floodRate = v; });
    fLevel.slider("Drain rate (auto-fall)", 0, 1, 0.01, P.drainRate, v => { P.drainRate = v; });
    fLevel.note("Flood drowns the island; draining strands everything on the seabed. Whirlpools pull floaters under and make them resurface elsewhere.");

    const fWhirl = panel.folder("🌀 Whirlpools", false);
    handles.whirlBtn = fWhirl.button("🌀 Arm whirlpool placement (click water)", () => {
      whirlArmed = !whirlArmed;
      handles.whirlBtn.el.textContent = whirlArmed ? "…now click anywhere on the ocean" : "🌀 Arm whirlpool placement (click water)";
    });
    fWhirl.buttonRow([
      { label: "Random whirlpool", onClick: () => {
        const a = Math.random() * Math.PI * 2, r = 30 + Math.random() * 70;
        addWhirl(Math.cos(a) * r, Math.sin(a) * r);
        if (handles.whirlCount) handles.whirlCount.set(whirls.length + " active");
      } },
      { label: "Clear all", kind: "danger", onClick: () => { clearWhirls(); if (handles.whirlCount) handles.whirlCount.set("0 active"); } }
    ]);
    fWhirl.slider("Strength", 0.4, 3.5, 0.05, whirlDefaults.strength, v => { whirlDefaults.strength = v; });
    fWhirl.slider("Radius", 3, 14, 0.5, whirlDefaults.radius, v => { whirlDefaults.radius = v; });
    handles.whirlCount = fWhirl.read("Whirlpools", "0 active");

    const fSky = panel.folder("🌤️ Atmosphere & Sky", false);
    handles.horizon = fSky.color("Horizon", P.sky.horizon, v => { P.sky.horizon = v; });
    handles.zenith = fSky.color("Zenith", P.sky.zenith, v => { P.sky.zenith = v; });
    handles.sunTint = fSky.color("Sun tint", P.sky.sunTint, v => { P.sky.sunTint = v; });
    handles.cloud = fSky.slider("Cloud cover", 0, 1, 0.02, P.sky.cloud, v => { P.sky.cloud = v; });
    handles.cloudScale = fSky.slider("Cloud scale", 0.3, 2.2, 0.05, P.sky.cloudScale, v => { P.sky.cloudScale = v; });
    handles.aurora = fSky.slider("Aurora", 0, 1.5, 0.05, P.sky.aurora, v => { P.sky.aurora = v; });
    handles.star = fSky.slider("Star brightness", 0, 2, 0.05, P.sky.star, v => { P.sky.star = v; });
    handles.fog = fSky.slider("Fog density", 0, 0.006, 0.0001, P.sky.fog, v => { P.sky.fog = v; });
    handles.planetSize = fSky.slider("Gas giant size (0 = off)", 0, 0.8, 0.01, P.sky.planetSize, v => { P.sky.planetSize = v; });
    fSky.color("Gas giant band A", P.sky.planetA, v => { P.sky.planetA = v; });
    fSky.color("Gas giant band B", P.sky.planetB, v => { P.sky.planetB = v; });

    const fWeather = panel.folder("🌧 Weather & Time", false);
    handles.rain = fWeather.slider("Rain", 0, 1, 0.02, P.rain, v => { P.rain = v; });
    handles.thunder = fWeather.toggle("Thunder & lightning", P.thunder, v => { P.thunder = v; });
    fWeather.toggle("Ocean ambience (audio)", P.ambience, v => { P.ambience = v; Ambience.setPower(v); });
    fWeather.sep();
    fWeather.toggle("Freeze waves (pause time)", P.paused, v => { P.paused = v; });
    fWeather.slider("Slow motion", 0.1, 1, 0.05, P.slowmo, v => { P.slowmo = v; });

    const fPhys = panel.folder("🧲 Physics Playground", true);
    fPhys.note("<b>Drag any floater with the mouse</b> — move it, place it, flick to throw it (scroll adjusts height). Click empty water to spawn.");
    fPhys.select("Click-to-spawn", Object.keys(PHYS.KINDS).map(k => ({ value: k, label: PHYS.KINDS[k].label })), P.spawnKind, v => { P.spawnKind = v; });
    fPhys.buttonRow([
      { label: "＋ Crate", onClick: () => randSpawn("crate") },
      { label: "＋ Ball", onClick: () => randSpawn("ball") },
      { label: "＋ Boat", onClick: () => randSpawn("boat") }
    ]);
    fPhys.buttonRow([
      { label: "＋ Duck 🦆", onClick: () => randSpawn("duck") },
      { label: "＋ Stone", onClick: () => randSpawn("stone") },
      { label: "＋ Plank", onClick: () => randSpawn("plank") }
    ]);
    fPhys.buttonRow([
      { label: "＋ Buoy", onClick: () => randSpawn("buoy") },
      { label: "🚩 Banner (cloth)", onClick: () => randSpawn("banner") },
      { label: "🧊 Iceberg", onClick: () => randSpawn("berg") }
    ]);
    fPhys.buttonRow([
      { label: "＋ Barrel", onClick: () => randSpawn("barrel") },
      { label: "🗑 Clear all", kind: "danger", onClick: () => floaters.clear() }
    ]);
    fPhys.slider("Gravity", 2, 20, 0.1, P.gravity, v => { P.gravity = v; });
    fPhys.slider("Buoyancy", 0.6, 1.7, 0.02, P.buoyancy, v => { P.buoyancy = v; });
    fPhys.toggle("Splash effects", P.splashes, v => { P.splashes = v; });
    fPhys.slider("Splash size", 0.3, 2.5, 0.05, P.splashScale, v => { P.splashScale = v; });

    const fFilm = panel.folder("🎥 Camera, Film & Performance", false);
    fFilm.select("Camera mode", [{ value: "orbit", label: "Orbit" }, { value: "fly", label: "Free fly (WASD + drag)" }], P.camMode, v => setCamMode(v));
    fFilm.slider("Fly speed", 6, 90, 1, P.flySpeed, v => { P.flySpeed = v; });
    fFilm.slider("Exposure", 0.3, 3, 0.02, P.exposure, v => { P.exposure = v; });
    fFilm.slider("Bloom", 0, 2, 0.05, P.bloom, v => { P.bloom = v; });
    fFilm.slider("Vignette", 0, 1, 0.05, P.vignette, v => { P.vignette = v; });
    fFilm.toggle("FXAA", P.fxaa, v => { P.fxaa = v; });
    fFilm.slider("Field of view", 0.5, 1.5, 0.02, P.fov, v => { P.fov = v; });
    fFilm.toggle("Cinematic auto-orbit", P.autoOrbit, v => { P.autoOrbit = v; });
    fFilm.slider("Render scale", 0.45, 1.25, 0.05, P.renderScale, v => {
      P.renderScale = v;
      engine.setHardwareScalingLevel(1 / v);
    });
    fFilm.toggle("Shadows", P.shadows, v => {
      P.shadows = v;
      if (!v) { scene.removeLight(sun); } else if (!scene.lights.includes(sun)) { scene.addLight(sun); }
    });
  }

  function randSpawn(kind) {
    for (let i = 0; i < 12; i++) {
      const a = Math.random() * Math.PI * 2;
      const r = 34 + Math.random() * 90;
      const x = Math.cos(a) * r, z = Math.sin(a) * r;
      if (!heightSampler || heightSampler(x, z) < P.seaLevel - 2.5) {
        floaters.spawn(kind, new BABYLON.Vector3(x, P.seaLevel + 8, z));
        return;
      }
    }
    floaters.spawn(kind, new BABYLON.Vector3(0, P.seaLevel + 8, 130));
  }
})();
