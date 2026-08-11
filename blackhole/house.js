/* ---------------------------------------------------------------------------
 * Singularity Vault — "The Hollow House" dimension
 * A full Babylon scene on its own canvas/engine: a lone house in a foggy void,
 * telephone poles sagging into nothing, one impossible white door.
 * Global: window.createHouseDimension(canvas, { onDoor })
 * ------------------------------------------------------------------------- */
(function () {
  window.createHouseDimension = function (canvas, hooks) {
    const engine = new BABYLON.Engine(canvas, true, { antialias: true, powerPreference: "high-performance" });
    const scene = new BABYLON.Scene(engine);
    scene.clearColor = BABYLON.Color4.FromHexString("#04060cff");
    scene.fogMode = BABYLON.Scene.FOGMODE_EXP2;
    scene.fogDensity = 0.023;
    scene.fogColor = BABYLON.Color3.FromHexString("#04060c");

    const camera = new BABYLON.UniversalCamera("hcam", new BABYLON.Vector3(0, 1.7, 44), scene);
    camera.setTarget(new BABYLON.Vector3(0.6, 2.0, 3));
    camera.minZ = 0.1;
    camera.maxZ = 1600;
    camera.speed = 0.11;
    camera.inertia = 0.8;
    camera.keysUp = [87, 38];
    camera.keysDown = [83, 40];
    camera.keysLeft = [65, 37];
    camera.keysRight = [68, 39];
    camera.attachControl(canvas, true);

    const hemi = new BABYLON.HemisphericLight("hhemi", new BABYLON.Vector3(0.2, 1, 0.1), scene);
    hemi.intensity = 0.16;
    hemi.diffuse = BABYLON.Color3.FromHexString("#33405e");
    hemi.groundColor = BABYLON.Color3.FromHexString("#0a0c10");
    const moon = new BABYLON.DirectionalLight("moon", new BABYLON.Vector3(0.45, -0.8, 0.55), scene);
    moon.intensity = 0.22;
    moon.diffuse = BABYLON.Color3.FromHexString("#8fa2cc");

    /* ---- ground: gently rippling void plain ---- */
    const ground = BABYLON.MeshBuilder.CreateGround("ground", { width: 900, height: 900, subdivisions: 48, updatable: true }, scene);
    {
      const pos = ground.getVerticesData(BABYLON.VertexBuffer.PositionKind);
      for (let i = 0; i < pos.length; i += 3) {
        const x = pos[i], z = pos[i + 2];
        const d = Math.sqrt(x * x + z * z);
        const flat = Math.min(1, Math.max(0, (d - 14) / 30));
        pos[i + 1] = (Math.sin(x * 0.05) * Math.cos(z * 0.045) * 2.2 + Math.sin(x * 0.013 + 2.0) * 3.0) * flat - 0.02;
      }
      ground.updateVerticesData(BABYLON.VertexBuffer.PositionKind, pos);
      const normals = [];
      BABYLON.VertexData.ComputeNormals(pos, ground.getIndices(), normals);
      ground.updateVerticesData(BABYLON.VertexBuffer.NormalKind, normals);
    }
    const gmat = new BABYLON.StandardMaterial("gmat", scene);
    gmat.diffuseColor = BABYLON.Color3.FromHexString("#131a13");
    gmat.specularColor = new BABYLON.Color3(0, 0, 0);
    ground.material = gmat;

    /* ---- helper for standard dark materials ---- */
    const mkMat = (name, hex, spec = 0.04) => {
      const m = new BABYLON.StandardMaterial(name, scene);
      m.diffuseColor = BABYLON.Color3.FromHexString(hex);
      m.specularColor = new BABYLON.Color3(spec, spec, spec);
      return m;
    };
    const mkGlow = (name, hex, mult) => {
      const m = new BABYLON.StandardMaterial(name, scene);
      m.emissiveColor = BABYLON.Color3.FromHexString(hex).scale(mult);
      m.disableLighting = true;
      return m;
    };

    /* ---- the house ---- */
    const wood = mkMat("wood", "#33291f");
    const roofM = mkMat("roof", "#1e1a17");
    const trim = mkMat("trim", "#14100d");
    const house = new BABYLON.TransformNode("house", scene);
    const walls = BABYLON.MeshBuilder.CreateBox("walls", { width: 7.4, height: 3.6, depth: 6.4 }, scene);
    walls.position.y = 1.8; walls.material = wood; walls.parent = house;
    const roofL = BABYLON.MeshBuilder.CreateBox("roofL", { width: 7.9, height: 0.22, depth: 4.3 }, scene);
    roofL.position.set(0, 4.42, 1.48); roofL.rotation.x = 0.63; roofL.material = roofM; roofL.parent = house;
    const roofR = roofL.clone("roofR");
    roofR.position.set(0, 4.42, -1.48); roofR.rotation.x = -0.63; roofR.material = roofM; roofR.parent = house;
    const ridge = BABYLON.MeshBuilder.CreateBox("ridge", { width: 8.1, height: 0.24, depth: 0.5 }, scene);
    ridge.position.set(0, 5.35, 0); ridge.material = trim; ridge.parent = house;
    const chimney = BABYLON.MeshBuilder.CreateBox("chimney", { width: 0.9, height: 2.2, depth: 0.9 }, scene);
    chimney.position.set(2.2, 5.2, -1.4); chimney.material = trim; chimney.parent = house;
    // porch
    const porch = BABYLON.MeshBuilder.CreateBox("porch", { width: 5.4, height: 0.28, depth: 2.3 }, scene);
    porch.position.set(0, 0.14, 4.1); porch.material = trim; porch.parent = house;
    const postL = BABYLON.MeshBuilder.CreateCylinder("postL", { height: 2.5, diameter: 0.16 }, scene);
    postL.position.set(-2.45, 1.35, 5.05); postL.material = wood; postL.parent = house;
    const postR = postL.clone("postR"); postR.position.x = 2.45; postR.parent = house;
    const porchRoof = BABYLON.MeshBuilder.CreateBox("porchRoof", { width: 5.6, height: 0.16, depth: 2.5 }, scene);
    porchRoof.position.set(0, 2.72, 4.15); porchRoof.rotation.x = 0.12; porchRoof.material = roofM; porchRoof.parent = house;
    // the DOOR — impossibly bright
    const doorFrame = BABYLON.MeshBuilder.CreateBox("doorFrame", { width: 1.5, height: 2.7, depth: 0.18 }, scene);
    doorFrame.position.set(0.6, 1.4, 3.22); doorFrame.material = trim; doorFrame.parent = house;
    const door = BABYLON.MeshBuilder.CreatePlane("door", { width: 1.16, height: 2.4 }, scene);
    door.position.set(0.6, 1.36, 3.33); door.material = mkGlow("doorGlow", "#dcecff", 2.6); door.parent = house;
    const doorLight = new BABYLON.PointLight("doorLight", new BABYLON.Vector3(0.6, 1.9, 4.2), scene);
    doorLight.diffuse = BABYLON.Color3.FromHexString("#cfe4ff");
    doorLight.intensity = 0.85; doorLight.range = 11;
    // warm windows
    const winGlow = mkGlow("winGlow", "#ffb45c", 2.0);
    const win1 = BABYLON.MeshBuilder.CreatePlane("win1", { width: 1.05, height: 0.95 }, scene);
    win1.position.set(-2.1, 1.95, 3.22); win1.material = winGlow; win1.parent = house;
    const win2 = win1.clone("win2"); win2.position.x = 2.6; win2.parent = house;
    const winBar = mkMat("winBar", "#0e0b09");
    [win1, win2].forEach((w, i) => {
      const bx = BABYLON.MeshBuilder.CreateBox("wb" + i, { width: 0.07, height: 0.95, depth: 0.03 }, scene);
      bx.position.copyFrom(w.position); bx.position.z += 0.02; bx.material = winBar; bx.parent = house;
      const by = BABYLON.MeshBuilder.CreateBox("wby" + i, { width: 1.05, height: 0.07, depth: 0.03 }, scene);
      by.position.copyFrom(bx.position); by.material = winBar; by.parent = house;
    });
    const amber = new BABYLON.PointLight("amber", new BABYLON.Vector3(-0.2, 2.0, 4.6), scene);
    amber.diffuse = BABYLON.Color3.FromHexString("#ffb45c");
    amber.intensity = 0.75; amber.range = 15;
    // porch lamp
    const lampCord = BABYLON.MeshBuilder.CreateCylinder("cord", { height: 0.5, diameter: 0.03 }, scene);
    lampCord.position.set(1.9, 2.5, 4.1); lampCord.material = trim; lampCord.parent = house;
    const lamp = BABYLON.MeshBuilder.CreateSphere("lamp", { diameter: 0.22, segments: 8 }, scene);
    lamp.position.set(1.9, 2.2, 4.1); lamp.material = mkGlow("lampGlow", "#ffd9a0", 2.2); lamp.parent = house;
    const lampLight = new BABYLON.PointLight("lampLight", new BABYLON.Vector3(1.9, 2.2, 4.15), scene);
    lampLight.diffuse = BABYLON.Color3.FromHexString("#ffd9a0");
    lampLight.intensity = 0.55; lampLight.range = 9;

    /* ---- dirt path ---- */
    const path = BABYLON.MeshBuilder.CreateBox("path", { width: 1.9, height: 0.06, depth: 40 }, scene);
    path.position.set(0.4, 0.03, 24.5);
    path.material = mkMat("pathMat", "#0d0b09");

    /* ---- telephone poles + sagging wires ---- */
    const poleMat = mkMat("poleMat", "#0f0d0b");
    const poleTops = [];
    for (let i = 0; i < 7; i++) {
      const z = 36 - i * 15;
      const pole = BABYLON.MeshBuilder.CreateCylinder("pole" + i, { height: 7.2, diameter: 0.22 }, scene);
      pole.position.set(7.5, 3.6, z);
      pole.material = poleMat;
      const arm = BABYLON.MeshBuilder.CreateBox("arm" + i, { width: 2.0, height: 0.14, depth: 0.14 }, scene);
      arm.position.set(7.5, 6.6, z);
      arm.material = poleMat;
      poleTops.push(new BABYLON.Vector3(7.5, 6.62, z));
    }
    for (let i = 0; i < poleTops.length - 1; i++) {
      for (let w = 0; w < 2; w++) {
        const A = poleTops[i].clone(); A.x += w === 0 ? -0.9 : 0.9;
        const B = poleTops[i + 1].clone(); B.x += w === 0 ? -0.9 : 0.9;
        const pts = [];
        for (let s = 0; s <= 8; s++) {
          const t = s / 8;
          const p = BABYLON.Vector3.Lerp(A, B, t);
          p.y -= Math.sin(t * Math.PI) * 1.15;
          pts.push(p);
        }
        const wire = BABYLON.MeshBuilder.CreateLines("wire", { points: pts }, scene);
        wire.color = new BABYLON.Color3(0.02, 0.02, 0.03);
      }
    }

    /* ---- dead forest ---- */
    const trunkProto = BABYLON.MeshBuilder.CreateCylinder("trunkP", { height: 3.4, diameterTop: 0.14, diameterBottom: 0.3 }, scene);
    trunkProto.material = mkMat("trunkM", "#0c0a09");
    trunkProto.position.y = -500;
    const crownProto = BABYLON.MeshBuilder.CreateCylinder("crownP", { height: 4.6, diameterTop: 0, diameterBottom: 2.6, tessellation: 7 }, scene);
    crownProto.material = mkMat("crownM", "#0b1210");
    crownProto.position.y = -500;
    const rng = UTILS.mulberry32(1337);
    for (let i = 0; i < 60; i++) {
      const a = rng() * Math.PI * 2;
      const r = 42 + rng() * 250;
      const x = Math.cos(a) * r, z = Math.sin(a) * r;
      if (Math.abs(x) < 10 && z > -6) continue; // keep the approach corridor clear
      const s = 0.7 + rng() * 1.5;
      const ti = trunkProto.createInstance("ti" + i);
      ti.position.set(x, 1.7 * s, z);
      ti.scaling.setAll(s);
      const ci = crownProto.createInstance("ci" + i);
      ci.position.set(x, (3.4 + 2.3) * s - 1.0, z);
      ci.scaling.setAll(s);
      ci.rotation.y = rng() * 6.28;
    }

    /* ---- sky dome ---- */
    const sky = BABYLON.MeshBuilder.CreateSphere("hsky", { diameter: 1200, segments: 10, sideOrientation: BABYLON.Mesh.DOUBLESIDE }, scene);
    sky.infiniteDistance = true;
    sky.isPickable = false;
    sky.applyFog = false;
    const skyMat = new BABYLON.ShaderMaterial("hskyMat", scene, { vertex: "hsky", fragment: "hsky" }, {
      attributes: ["position"], uniforms: ["worldViewProjection", "uTime"]
    });
    skyMat.backFaceCulling = false;
    skyMat.disableDepthWrite = true;
    sky.material = skyMat;

    /* ---- chimney smoke ---- */
    const smokeTex = new BABYLON.DynamicTexture("smokeTex", 64, scene, true);
    {
      const c = smokeTex.getContext();
      const g = c.createRadialGradient(32, 32, 2, 32, 32, 32);
      g.addColorStop(0, "rgba(160,160,170,0.5)");
      g.addColorStop(1, "rgba(160,160,170,0)");
      c.fillStyle = g; c.fillRect(0, 0, 64, 64);
      smokeTex.update(); smokeTex.hasAlpha = true;
    }
    const smoke = new BABYLON.ParticleSystem("smoke", 220, scene);
    smoke.particleTexture = smokeTex;
    smoke.emitter = new BABYLON.Vector3(2.2, 6.4, -1.4);
    smoke.minEmitBox = new BABYLON.Vector3(-0.2, 0, -0.2);
    smoke.maxEmitBox = new BABYLON.Vector3(0.2, 0.2, 0.2);
    smoke.direction1 = new BABYLON.Vector3(-0.2, 1.2, -0.15);
    smoke.direction2 = new BABYLON.Vector3(0.35, 1.9, 0.2);
    smoke.minLifeTime = 2.5; smoke.maxLifeTime = 5.5;
    smoke.minSize = 0.5; smoke.maxSize = 1.4;
    smoke.addSizeGradient(0, 0.4); smoke.addSizeGradient(1, 2.3);
    smoke.addAlphaGradient(0, 0.0); smoke.addAlphaGradient(0.25, 0.35); smoke.addAlphaGradient(1, 0.0);
    smoke.emitRate = 14;
    smoke.color1 = new BABYLON.Color4(0.35, 0.35, 0.4, 0.3);
    smoke.color2 = new BABYLON.Color4(0.3, 0.3, 0.36, 0.25);
    smoke.blendMode = BABYLON.ParticleSystem.BLENDMODE_STANDARD;
    smoke.start();

    /* ---- film pipeline ---- */
    const pipeline = new BABYLON.DefaultRenderingPipeline("hrp", true, scene, [camera]);
    pipeline.samples = 4;
    pipeline.fxaaEnabled = true;
    pipeline.bloomEnabled = true;
    pipeline.bloomThreshold = 0.55;
    pipeline.bloomWeight = 0.75;
    pipeline.imageProcessing.toneMappingEnabled = true;
    pipeline.imageProcessing.toneMappingType = BABYLON.ImageProcessingConfiguration.TONEMAPPING_ACES;
    pipeline.imageProcessing.exposure = 1.35;
    pipeline.imageProcessing.vignetteEnabled = true;
    pipeline.imageProcessing.vignetteWeight = 2.6;
    pipeline.grainEnabled = true;
    pipeline.grain.intensity = 9;
    pipeline.grain.animated = true;

    /* ---- runtime state ---- */
    let running = false;
    let autoWalk = false;
    let t = 0;
    let doorArmed = true;
    let bobPhase = 0;
    const doorPos = new BABYLON.Vector3(0.6, 1.5, 3.5);
    const autoTarget = new BABYLON.Vector3(0.6, 1.7, 4.6);
    const prevPos = camera.position.clone();

    scene.registerBeforeRender(() => {
      const dt = Math.min(engine.getDeltaTime() / 1000, 0.05);
      t += dt;
      skyMat.setFloat("uTime", t);
      amber.intensity = 0.72 + 0.16 * Math.sin(t * 12.7) + 0.08 * Math.sin(t * 31.4 + 1.7);
      lampLight.intensity = 0.52 + 0.06 * Math.sin(t * 9.1 + 0.6);
      doorLight.intensity = 0.82 + 0.12 * Math.sin(t * 0.9) + 0.04 * Math.sin(t * 5.3);
      if (autoWalk) {
        const to = autoTarget.subtract(camera.position); to.y = 0;
        const d = to.length();
        if (d > 0.4) {
          const step = Math.min(d * 1.1, 2.6) * dt;
          to.normalize();
          camera.position.x += to.x * step + Math.sin(t * 1.7) * 0.05 * dt;
          camera.position.z += to.z * step;
          bobPhase += step * 2.6;
        }
        camera.setTarget(BABYLON.Vector3.Lerp(camera.getTarget(), new BABYLON.Vector3(0.6, 1.6, 3.3), Math.min(1, dt * 1.5)));
      } else {
        const v = BABYLON.Vector3.Distance(camera.position, prevPos) / Math.max(dt, 1e-4);
        bobPhase += Math.min(v, 6) * dt * 2.2;
      }
      prevPos.copyFrom(camera.position);
      camera.position.y = 1.7 + Math.sin(bobPhase) * 0.05;
      // bounds
      camera.position.x = UTILS.clamp(camera.position.x, -260, 260);
      camera.position.z = UTILS.clamp(camera.position.z, -260, 290);
      // door proximity portal
      if (doorArmed) {
        const dx = camera.position.x - doorPos.x;
        const dz = camera.position.z - doorPos.z;
        if (dx * dx + dz * dz < 2.1 * 2.1 && camera.position.z > 2.4) {
          doorArmed = false;
          if (hooks && hooks.onDoor) hooks.onDoor();
        }
      }
    });

    return {
      engine, scene, camera,
      isRunning: () => running,
      start(auto) {
        autoWalk = !!auto;
        if (!running) { running = true; engine.runRenderLoop(() => scene.render()); }
      },
      stop() {
        if (running) { engine.stopRenderLoop(); running = false; }
      },
      reset() {
        camera.position.set(0, 1.7, 44);
        camera.setTarget(new BABYLON.Vector3(0.6, 2.0, 3));
        doorArmed = true;
        prevPos.copyFrom(camera.position);
      },
      armDoor() { doorArmed = true; },
      setAutoWalk(v) { autoWalk = !!v; }
    };
  };
})();
