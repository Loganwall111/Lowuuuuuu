/* ---------------------------------------------------------------------------
 * Ocean Worlds — Gerstner wave field (JS mirror of the GPU shader), buoyancy,
 * currents, whirlpools and verlet cloth.
 * Global: window.PHYS  (requires shared/util.js)
 * ------------------------------------------------------------------------- */
(function () {
  const { mulberry32, clamp, lerp } = UTILS;

  /* Gerstner wave set — MUST match the formulas in water/glsl.js WATER_VERTEX */
  class WaveSet {
    constructor() {
      this.N = 0;
      this.a = new Float32Array(64);  // dirx, dirz, k, amp
      this.b = new Float32Array(64);  // speed c, phase0, -, -
      this.chop = 0.5;
      this.ampSum = 0.0001;
      this.dirAngle = 0;
    }
    build(p) {
      const rng = mulberry32(p.seed || 1);
      const N = clamp(Math.round(p.count), 1, 16);
      this.N = N;
      this.chop = p.chop;
      this.dirAngle = p.dir || 0;
      let ampSum = 0;
      for (let i = 0; i < N; i++) {
        const ang = (p.dir || 0) + (rng() * 2 - 1) * 0.85;
        const L = p.len * (0.45 + rng() * 1.25);
        const k = (2 * Math.PI) / L;
        const amp = p.amp * clamp(L / (p.len * 1.7), 0.35, 1.0) * (0.55 + 0.45 * rng());
        const c = Math.sqrt(9.81 / k) * p.speed;
        this.a[i * 4] = Math.cos(ang);
        this.a[i * 4 + 1] = Math.sin(ang);
        this.a[i * 4 + 2] = k;
        this.a[i * 4 + 3] = amp;
        this.b[i * 4] = c;
        this.b[i * 4 + 1] = rng() * Math.PI * 2;
        this.b[i * 4 + 2] = 0;
        this.b[i * 4 + 3] = 0;
        ampSum += amp;
      }
      this.ampSum = Math.max(ampSum, 0.0001);
    }
    height(x, z, t) {
      let h = 0;
      for (let i = 0; i < this.N; i++) {
        const k = this.a[i * 4 + 2], amp = this.a[i * 4 + 3];
        const ph = k * (this.a[i * 4] * x + this.a[i * 4 + 1] * z) - k * this.b[i * 4] * t + this.b[i * 4 + 1];
        h += amp * Math.sin(ph);
      }
      return h;
    }
    normal(x, z, t, out) {
      const e = 0.9;
      const hL = this.height(x - e, z, t), hR = this.height(x + e, z, t);
      const hD = this.height(x, z - e, t), hU = this.height(x, z + e, t);
      out.set(hL - hR, 2 * e, hD - hU).normalize();
      return out;
    }
  }

  /* Whirlpool surface dip — MUST match the vertex shader funnel term */
  function whirlDip(x, z, whirls) {
    if (!whirls) return 0;
    let d = 0;
    for (const w of whirls) {
      const dx = x - w.x, dz = z - w.z;
      d += w.strength * Math.exp(-(dx * dx + dz * dz) / (w.radius * w.radius));
    }
    return d;
  }

  /* ------------------------------------------------ floater archetypes -- */
  const KINDS = {
    crate:  { label: "Crate",          r: 1.05, draft: 0.85, mass: 1.0,  stability: 3.2, drag: 1.9, buoy: 1.0 },
    ball:   { label: "Ball",           r: 0.85, draft: 0.55, mass: 0.55, stability: 0.9, drag: 1.4, buoy: 1.0 },
    barrel: { label: "Barrel",         r: 0.75, draft: 0.95, mass: 0.8,  stability: 2.6, drag: 1.7, buoy: 1.0 },
    boat:   { label: "S.S. Baby",      r: 1.9,  draft: 0.55, mass: 1.6,  stability: 8.0, drag: 1.2, buoy: 1.0 },
    berg:   { label: "Iceberg",        r: 2.6,  draft: 2.4,  mass: 9.0,  stability: 1.1, drag: 2.6, buoy: 1.0 },
    stone:  { label: "Stone (sinks!)", r: 0.8,  draft: 0.6,  mass: 3.2,  stability: 0.5, drag: 3.0, buoy: 0.16 },
    plank:  { label: "Plank",          r: 1.3,  draft: 0.2,  mass: 0.5,  stability: 6.0, drag: 1.6, buoy: 1.0 },
    duck:   { label: "Rubber Duck",    r: 0.7,  draft: 0.42, mass: 0.3,  stability: 14,  drag: 1.5, buoy: 1.1 },
    buoy:   { label: "Anchored Buoy",  r: 0.9,  draft: 0.8,  mass: 0.9,  stability: 5.0, drag: 2.0, buoy: 1.0, anchored: true },
    banner: { label: "Banner Raft",    r: 1.5,  draft: 0.4,  mass: 1.2,  stability: 9.0, drag: 1.4, buoy: 1.0 }
  };

  /* ------------------------------------------------------------ cloth --- */
  class Cloth {
    constructor(scene, getPin) {
      this.scene = scene;
      this.getPin = getPin;          // (i, outV3) world position of top-edge pin i
      this.W = 10; this.H = 12;
      this.restX = 0.17; this.restY = 0.185;
      const W = this.W, H = this.H;
      this.pts = [];
      for (let j = 0; j < H; j++) {
        for (let i = 0; i < W; i++) {
          this.pts.push({ p: new BABYLON.Vector3(), pp: new BABYLON.Vector3(), pinned: j === 0 });
        }
      }
      this._pinnedInit = false;
      const positions = new Float32Array(W * H * 3);
      const colors = new Float32Array(W * H * 4);
      const uvs = new Float32Array(W * H * 2);
      const indices = [];
      for (let j = 0; j < H; j++) {
        for (let i = 0; i < W; i++) {
          const k = j * W + i;
          uvs[k * 2] = i / (W - 1); uvs[k * 2 + 1] = j / (H - 1);
          const stripe = Math.floor(i / 2) % 2 === 0;
          colors[k * 4] = stripe ? 0.75 : 0.95; colors[k * 4 + 1] = stripe ? 0.16 : 0.9; colors[k * 4 + 2] = stripe ? 0.14 : 0.85; colors[k * 4 + 3] = 1;
          if (i < W - 1 && j < H - 1) {
            const a = k, b = k + 1, c = k + W, d = k + W + 1;
            indices.push(a, c, b, b, c, d);
          }
        }
      }
      const mesh = new BABYLON.Mesh("cloth", scene);
      const vd = new BABYLON.VertexData();
      vd.positions = positions; vd.indices = indices; vd.uvs = uvs;
      vd.normals = new Float32Array(W * H * 3);
      vd.applyToMesh(mesh, true);
      mesh.setVerticesData(BABYLON.VertexBuffer.ColorKind, colors);
      mesh.useVertexColors = true;
      this.mesh = mesh;
      const m = new BABYLON.StandardMaterial("clothMat", scene);
      m.diffuseColor = new BABYLON.Color3(1, 1, 1);
      m.specularColor = new BABYLON.Color3(0.05, 0.05, 0.05);
      m.emissiveColor = new BABYLON.Color3(0.10, 0.03, 0.03);
      m.backFaceCulling = false;
      try { m.useVertexColor = true; } catch (e) {}
      mesh.material = m;
      this.indices = indices;
      this.t = 0;
    }
    step(dt, windX, windZ, windPow, waterFn) {
      this.t += dt;
      dt = Math.min(dt, 0.033);
      const W = this.W, H = this.H;
      const damp = 0.985;
      const g = -7.5;
      // pins
      const pinV = this._pinV || (this._pinV = new BABYLON.Vector3());
      for (let i = 0; i < W; i++) {
        const pt = this.pts[i];
        this.getPin(i, pinV);
        if (!this._pinnedInit) { pt.pp.copyFrom(pinV); }
        pt.p.copyFrom(pinV);
        pt.pp.copyFrom(pinV);
      }
      if (!this._pinnedInit) {
        // lay the cloth hanging below the pins on first run
        for (let j = 1; j < H; j++) {
          for (let i = 0; i < W; i++) {
            const pt = this.pts[j * W + i];
            pt.p.copyFrom(this.pts[i].p);
            pt.p.y -= j * this.restY;
            pt.pp.copyFrom(pt.p);
          }
        }
        this._pinnedInit = true;
      }
      // verlet integrate
      for (let j = 1; j < H; j++) {
        for (let i = 0; i < W; i++) {
          const pt = this.pts[j * W + i];
          const gust = 0.55 + 0.45 * Math.sin(this.t * 2.3 + pt.p.x * 0.6 + pt.p.z * 0.5) + 0.25 * Math.sin(this.t * 5.1 + pt.p.y);
          const fx = windX * gust * windPow;
          const fz = windZ * gust * windPow;
          const vx = (pt.p.x - pt.pp.x) * damp;
          const vy = (pt.p.y - pt.pp.y) * damp;
          const vz = (pt.p.z - pt.pp.z) * damp;
          pt.pp.copyFrom(pt.p);
          pt.p.x += vx + fx * dt * dt * 9.0;
          pt.p.y += vy + g * dt * dt;
          pt.p.z += vz + fz * dt * dt * 9.0;
        }
      }
      // constraints
      const rx = this.restX, ry = this.restY;
      for (let iter = 0; iter < 4; iter++) {
        for (let j = 0; j < H; j++) {
          for (let i = 0; i < W; i++) {
            const k = j * W + i;
            if (i < W - 1) this._link(k, k + 1, rx);
            if (j < H - 1) this._link(k, k + W, ry);
          }
        }
      }
      // water collision
      if (waterFn) {
        for (let j = 1; j < H; j++) {
          for (let i = 0; i < W; i++) {
            const pt = this.pts[j * W + i];
            const h = waterFn(pt.p.x, pt.p.z) + 0.04;
            if (pt.p.y < h) { pt.p.y = h; pt.pp.y = Math.min(pt.pp.y, h); }
          }
        }
      }
      // write mesh
      const pos = this.mesh.getVerticesData(BABYLON.VertexBuffer.PositionKind);
      for (let k = 0; k < this.pts.length; k++) {
        const p = this.pts[k].p;
        pos[k * 3] = p.x; pos[k * 3 + 1] = p.y; pos[k * 3 + 2] = p.z;
      }
      this.mesh.updateVerticesData(BABYLON.VertexBuffer.PositionKind, pos);
      const nor = this._nor || (this._nor = new Float32Array(this.pts.length * 3));
      BABYLON.VertexData.ComputeNormals(pos, this.indices, nor);
      this.mesh.updateVerticesData(BABYLON.VertexBuffer.NormalKind, nor);
    }
    _link(ka, kb, rest) {
      const A = this.pts[ka], B = this.pts[kb];
      let dx = B.p.x - A.p.x, dy = B.p.y - A.p.y, dz = B.p.z - A.p.z;
      const d = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (d < 1e-6) return;
      const diff = (d - rest) / d;
      const wA = A.pinned ? 0 : 1, wB = B.pinned ? 0 : 1;
      const wSum = wA + wB;
      if (wSum === 0) return;
      const fA = wA / wSum, fB = wB / wSum;
      A.p.x += dx * diff * fA; A.p.y += dy * diff * fA; A.p.z += dz * diff * fA;
      B.p.x -= dx * diff * fB; B.p.y -= dy * diff * fB; B.p.z -= dz * diff * fB;
    }
    dispose() { this.mesh.dispose(); }
  }

  /* ---------------------------------------------------------- floaters -- */
  class Floaters {
    constructor(scene, shadowGen) {
      this.scene = scene;
      this.shadow = shadowGen;
      this.list = [];
      this.rings = [];
      this.mats = null;
      this._buildMaterials();
      this._buildSplash();
      this._tmpN = new BABYLON.Vector3();
      this._tmpQ = new BABYLON.Quaternion();
      this._up = new BABYLON.Vector3();
      this._axis = new BABYLON.Vector3();
      this._vAxis = new BABYLON.Vector3();
      this.allowFx = true;
      this.splashScale = 1;
    }
    _buildMaterials() {
      const mk = (name, hex, spec = 0.25) => {
        const m = new BABYLON.StandardMaterial(name, this.scene);
        const c = UTILS.hexToRgb(hex);
        m.diffuseColor = new BABYLON.Color3(c.r, c.g, c.b);
        m.specularColor = new BABYLON.Color3(spec, spec, spec);
        m.specularPower = 48;
        return m;
      };
      this.mats = {
        crate: mk("mCrate", "#9a7443", 0.12),
        ball: mk("mBall", "#e1533f", 0.5),
        barrel: mk("mBarrel", "#3f6d93", 0.35),
        boat: mk("mBoat", "#d9d2c0", 0.3),
        boatTrim: mk("mBoatTrim", "#7c2e2e", 0.3),
        berg: mk("mBerg", "#e6eff6", 0.6),
        flag: mk("mFlag", "#ffd76a", 0.1),
        stone: mk("mStone", "#4c4a48", 0.05),
        plank: mk("mPlank", "#a8875a", 0.15),
        duck: mk("mDuck", "#ffd23f", 0.45),
        duckBeak: mk("mBeak", "#ff8c2e", 0.4),
        buoy: mk("mBuoyR", "#d8322a", 0.4),
        buoyW: mk("mBuoyW", "#efe8dd", 0.4)
      };
    }
    _buildSplash() {
      const s = this.scene;
      const texSize = 64;
      const dtex = new BABYLON.DynamicTexture("splashTex", texSize, s, true);
      const c = dtex.getContext();
      const grd = c.createRadialGradient(texSize / 2, texSize / 2, 1, texSize / 2, texSize / 2, texSize / 2);
      grd.addColorStop(0, "rgba(255,255,255,1)");
      grd.addColorStop(0.55, "rgba(220,240,255,0.55)");
      grd.addColorStop(1, "rgba(200,230,255,0)");
      c.fillStyle = grd; c.fillRect(0, 0, texSize, texSize); dtex.update();
      dtex.hasAlpha = true;
      const ps = new BABYLON.ParticleSystem("splashes", 3000, s);
      ps.particleTexture = dtex;
      ps.emitter = new BABYLON.Vector3(0, -100, 0);
      ps.minEmitBox = new BABYLON.Vector3(-0.4, 0, -0.4);
      ps.maxEmitBox = new BABYLON.Vector3(0.4, 0, 0.4);
      ps.direction1 = new BABYLON.Vector3(-2.2, 2.5, -2.2);
      ps.direction2 = new BABYLON.Vector3(2.2, 7.5, 2.2);
      ps.minLifeTime = 0.35; ps.maxLifeTime = 0.9;
      ps.minSize = 0.10; ps.maxSize = 0.55;
      ps.emitRate = 0;
      ps.gravity = new BABYLON.Vector3(0, -13, 0);
      ps.color1 = new BABYLON.Color4(0.9, 0.97, 1, 0.85);
      ps.color2 = new BABYLON.Color4(0.7, 0.88, 1, 0.6);
      ps.colorDead = new BABYLON.Color4(0.8, 0.9, 1, 0);
      ps.blendMode = BABYLON.ParticleSystem.BLENDMODE_STANDARD;
      ps.start();
      this.splash = ps;
      const ring = BABYLON.MeshBuilder.CreateTorus("ringProto", { diameter: 1.6, thickness: 0.12, tessellation: 48 }, s);
      const rm = new BABYLON.StandardMaterial("ringMat", s);
      rm.emissiveColor = new BABYLON.Color3(0.75, 0.88, 0.95);
      rm.alpha = 0.55; rm.disableLighting = true;
      ring.material = rm; ring.isVisible = false; ring.isPickable = false;
      this.ringProto = ring;
    }
    ringOnly(p, scale) {
      if (!this.allowFx) return;
      const ring = this.ringProto.clone("ring" + (Math.random() * 1e6 | 0));
      ring.material = this.ringProto.material.clone("ringMat" + (Math.random() * 1e6 | 0));
      ring.isVisible = true;
      ring.position.copyFromFloats(p.x, p.y + 0.06, p.z);
      ring.scaling.setAll(scale || 1);
      this.rings.push({ mesh: ring, t: 0, scale: scale || 1 });
    }
    _splashBurst(p, strength) {
      if (!this.allowFx) return;
      this.splash.emitter.copyFromFloats(p.x, p.y + 0.15, p.z);
      this.splash.manualEmitCount = Math.min(90, (24 + strength * 16) * this.splashScale) | 0;
      this.ringOnly(p, this.splashScale);
    }
    _meshFor(kind) {
      const s = this.scene;
      let node;
      if (kind === "crate") {
        node = BABYLON.MeshBuilder.CreateBox("crate", { size: 1.7 }, s);
        node.material = this.mats.crate;
      } else if (kind === "ball") {
        node = BABYLON.MeshBuilder.CreateSphere("ball", { diameter: 1.7, segments: 14 }, s);
        node.material = this.mats.ball;
      } else if (kind === "barrel") {
        node = BABYLON.MeshBuilder.CreateCylinder("barrel", { height: 1.9, diameter: 1.15, tessellation: 18 }, s);
        node.material = this.mats.barrel;
      } else if (kind === "berg") {
        node = BABYLON.MeshBuilder.CreateBox("berg", { width: 4.4, height: 3.4, depth: 3.6 }, s);
        node.rotationQuaternion = BABYLON.Quaternion.FromEulerAngles(0.35, Math.random() * 6.28, 0.22);
        node.material = this.mats.berg;
      } else if (kind === "stone") {
        node = BABYLON.MeshBuilder.CreateBox("stone", { size: 1.2 }, s);
        node.rotationQuaternion = BABYLON.Quaternion.FromEulerAngles(0.5, 0.7, 0.3);
        node.material = this.mats.stone;
      } else if (kind === "plank") {
        node = BABYLON.MeshBuilder.CreateBox("plank", { width: 2.5, height: 0.22, depth: 0.65 }, s);
        node.material = this.mats.plank;
      } else if (kind === "duck") {
        node = BABYLON.MeshBuilder.CreateSphere("duckBody", { diameter: 1.0, segments: 12 }, s);
        node.scaling = new BABYLON.Vector3(1.15, 0.85, 0.85);
        node.material = this.mats.duck;
        const head = BABYLON.MeshBuilder.CreateSphere("duckHead", { diameter: 0.55, segments: 10 }, s);
        head.position = new BABYLON.Vector3(0.42, 0.55, 0);
        head.material = this.mats.duck; head.parent = node;
        const beak = BABYLON.MeshBuilder.CreateCylinder("duckBeak", { height: 0.26, diameterTop: 0.02, diameterBottom: 0.24, tessellation: 8 }, s);
        beak.rotationQuaternion = BABYLON.Quaternion.FromEulerAngles(0, 0, -Math.PI / 2);
        beak.position = new BABYLON.Vector3(0.72, 0.52, 0);
        beak.material = this.mats.duckBeak; beak.parent = node;
      } else if (kind === "buoy") {
        node = BABYLON.MeshBuilder.CreateSphere("buoy", { diameter: 1.5, segments: 12 }, s);
        node.material = this.mats.buoy;
        const cap = BABYLON.MeshBuilder.CreateCylinder("buoyCap", { height: 0.6, diameterTop: 0.25, diameterBottom: 0.6, tessellation: 10 }, s);
        cap.position = new BABYLON.Vector3(0, 0.85, 0);
        cap.material = this.mats.buoyW; cap.parent = node;
      } else if (kind === "banner") {
        node = BABYLON.MeshBuilder.CreateBox("raft", { width: 1.9, height: 0.42, depth: 1.15 }, s);
        node.material = this.mats.plank;
        const pole = BABYLON.MeshBuilder.CreateCylinder("pole", { height: 3.0, diameter: 0.09 }, s);
        pole.position = new BABYLON.Vector3(-0.7, 1.6, 0);
        pole.material = this.mats.boatTrim; pole.parent = node;
        const arm = BABYLON.MeshBuilder.CreateCylinder("arm", { height: 1.7, diameter: 0.07 }, s);
        arm.rotationQuaternion = BABYLON.Quaternion.FromEulerAngles(0, 0, Math.PI / 2);
        arm.position = new BABYLON.Vector3(0.15, 3.0, 0);
        arm.material = this.mats.boatTrim; arm.parent = node;
      } else { // boat
        node = BABYLON.MeshBuilder.CreateBox("boat", { width: 3.0, height: 0.85, depth: 1.35 }, s);
        node.material = this.mats.boat;
        const nose = BABYLON.MeshBuilder.CreateCylinder("boatNose", { height: 1.35, diameterTop: 0, diameterBottom: 1.3, tessellation: 3 }, s);
        nose.rotationQuaternion = BABYLON.Quaternion.FromEulerAngles(Math.PI / 2, 0, Math.PI / 6);
        nose.scaling = new BABYLON.Vector3(1, 1, 0.62);
        nose.position = new BABYLON.Vector3(1.9, 0, 0);
        nose.material = this.mats.boat;
        nose.parent = node;
        const mast = BABYLON.MeshBuilder.CreateCylinder("mast", { height: 2.4, diameter: 0.09 }, s);
        mast.position = new BABYLON.Vector3(-0.1, 1.5, 0);
        mast.material = this.mats.boatTrim; mast.parent = node;
        const flag = BABYLON.MeshBuilder.CreatePlane("flag", { width: 0.95, height: 0.55 }, s);
        flag.position = new BABYLON.Vector3(0.38, 2.35, 0);
        flag.material = this.mats.flag; flag.parent = node;
        node._flag = flag;
      }
      if (!node.rotationQuaternion) node.rotationQuaternion = BABYLON.Quaternion.Identity();
      return node;
    }
    spawn(kind, pos, opts) {
      opts = opts || {};
      if (this.list.length >= 60) {
        const old = this.list.shift();
        if (old.cloth) old.cloth.dispose();
        if (old.rope) old.rope.dispose();
        old.node.dispose();
      }
      const def = KINDS[kind] || KINDS.crate;
      const node = this._meshFor(kind);
      node.position.copyFrom(pos);
      node.position.y += opts.dropHeight !== undefined ? opts.dropHeight : (4 + Math.random() * 3);
      if (this.shadow) this.shadow.addShadowCaster(node);
      const body = {
        node, kind, def,
        vel: new BABYLON.Vector3((Math.random() - 0.5) * 2, 0, (Math.random() - 0.5) * 2),
        angVel: new BABYLON.Vector3((Math.random() - 0.5), (Math.random() - 0.5) * 0.4, (Math.random() - 0.5)),
        prevAbove: true,
        mass: def.mass,
        held: false,
        holdPos: new BABYLON.Vector3(),
        anchor: def.anchored ? { x: pos.x, z: pos.z } : null,
        rope: null,
        cloth: null
      };
      if (opts.vel) body.vel.copyFrom(opts.vel);
      // tag every sub-mesh so picking can find the owning body
      node._floatBody = body;
      node.getChildMeshes(false).forEach(ch => { ch._floatBody = body; });
      if (def.anchored) {
        const anchors = [];
        for (let i = 0; i <= 7; i++) anchors.push(new BABYLON.Vector3(pos.x, pos.y, pos.z));
        body.rope = BABYLON.MeshBuilder.CreateLines("rope", { points: anchors, updatable: true }, this.scene);
        body.rope.color = new BABYLON.Color3(0.25, 0.22, 0.18);
      }
      if (kind === "banner") {
        const pinV = new BABYLON.Vector3();
        body.cloth = new Cloth(this.scene, (i, out) => {
          const lx = -0.55 + (i / 9) * 1.5;
          out.copyFromFloats(lx, 3.0, 0);
          BABYLON.Vector3.TransformCoordinatesToRef(out, node.getWorldMatrix(), out);
        });
      }
      this.list.push(body);
      return body;
    }
    clear() {
      this.list.forEach(b => {
        if (b.cloth) b.cloth.dispose();
        if (b.rope) b.rope.dispose();
        b.node.dispose(false, true);
      });
      this.list.length = 0;
    }
    releaseAll() { this.list.forEach(b => { b.held = false; }); }
    step(dt, waves, t, opts) {
      const g = opts.gravity;
      const hAt = opts.heightAt;
      const wind = opts.wind;
      const current = opts.current || { x: 0, y: 0 };
      const sea = opts.seaLevel || 0;
      const whirls = opts.whirls || [];
      for (const b of this.list) {
        const p = b.node.position;
        if (b.held) {
          // kinematic drag — velocity captured by the grabber for throws
          const k = Math.min(1, dt * 12);
          p.x += (b.holdPos.x - p.x) * k;
          p.y += (b.holdPos.y - p.y) * k;
          p.z += (b.holdPos.z - p.z) * k;
          b.prevAbove = false;
          continue;
        }
        const h = sea + waves.height(p.x, p.z, t) - whirlDip(p.x, p.z, whirls);
        const sub = clamp((h - (p.y - b.def.draft)) / (b.def.draft * 2), 0, 1);
        const wasAbove = b.prevAbove;
        b.vel.y += (sub * g * opts.buoyancy * b.def.buoy * (1.0 + clamp((h - p.y) / (b.def.draft * 2), 0, 1)) - g) * dt / Math.sqrt(b.mass);
        const linD = (0.04 + sub * b.def.drag) * dt;
        b.vel.x -= b.vel.x * Math.min(1, linD);
        b.vel.y -= b.vel.y * Math.min(1, linD * 1.2);
        b.vel.z -= b.vel.z * Math.min(1, linD);
        if (sub > 0.05) {
          // current + wind push (flowing water option)
          const wx = current.x + wind.x * 0.22;
          const wz = current.y + wind.y * 0.22;
          b.vel.x += wx * sub * dt;
          b.vel.z += wz * sub * dt;
        }
        if (b.anchor) {
          const k = 5.0;
          b.vel.x += (b.anchor.x - p.x) * k * dt * 0.5;
          b.vel.z += (b.anchor.z - p.z) * k * dt * 0.5;
        }
        // whirlpool suction + swirl
        for (const w of whirls) {
          const dx = w.x - p.x, dz = w.z - p.z;
          const d = Math.sqrt(dx * dx + dz * dz);
          if (d < w.radius * 4 && d > 0.01) {
            const nx = dx / d, nz = dz / d;
            const pull = w.strength * 3.2 / (d * 0.4 + 0.6);
            b.vel.x += nx * pull * dt;
            b.vel.z += nz * pull * dt;
            // tangential swirl
            b.vel.x += -nz * w.strength * 5.5 * dt / (d * 0.25 + 0.7);
            b.vel.z += nx * w.strength * 5.5 * dt / (d * 0.25 + 0.7);
            if (d < w.radius * 0.5) {
              b.vel.y -= w.strength * 5.0 * dt;
              b.angVel.y += w.strength * 6.0 * dt;
            }
          }
        }
        // swallowed by a whirlpool → resurface somewhere far away
        for (const w of whirls) {
          const dx = p.x - w.x, dz = p.z - w.z;
          if (dx * dx + dz * dz < w.radius * w.radius * 0.16 && p.y < (sea - w.strength * 0.6 - 1.0)) {
            const a = Math.random() * Math.PI * 2, r = 60 + Math.random() * 80;
            p.set(Math.cos(a) * r, sea + 9, Math.sin(a) * r);
            b.vel.set(0, 0, 0);
            this._splashBurst(new BABYLON.Vector3(p.x, sea + 0.3, p.z), 4);
            break;
          }
        }
        const nowAbove = (p.y - b.def.draft) > h;
        if (!nowAbove && wasAbove && b.vel.y < -2.2) {
          this._splashBurst(new BABYLON.Vector3(p.x, h + 0.2, p.z), -b.vel.y);
        }
        b.prevAbove = nowAbove;
        waves.normal(p.x, p.z, t, this._tmpN);
        const q = b.node.rotationQuaternion;
        this._up.copyFromFloats(0, 1, 0).applyRotationQuaternionToRef(q, this._up);
        BABYLON.Vector3.CrossToRef(this._up, this._tmpN, this._axis);
        b.angVel.addInPlace(this._axis.scale(b.def.stability * (0.25 + sub) * dt));
        b.angVel.scaleInPlace(Math.exp(-(1.4 + sub * 1.6) * dt));
        const av = b.angVel.length();
        if (av > 1e-5) {
          this._vAxis.copyFrom(b.angVel).scaleInPlace(1 / av);
          BABYLON.Quaternion.RotationAxisToRef(this._vAxis, av * dt, this._tmpQ);
          this._tmpQ.multiplyToRef(q, q);
          b.node.rotationQuaternion = q;
        }
        p.x += b.vel.x * dt; p.y += b.vel.y * dt; p.z += b.vel.z * dt;
        if (hAt) {
          const th = hAt(p.x, p.z);
          if (p.y - b.def.draft < th) {
            p.y = th + b.def.draft * 0.55;
            b.vel.y = Math.max(b.vel.y, 0);
            b.vel.x *= 0.94; b.vel.z *= 0.94;
          }
        }
        const r2 = p.x * p.x + p.z * p.z;
        if (r2 > 260 * 260) {
          const r = Math.sqrt(r2);
          b.vel.x -= (p.x / r) * 10 * dt;
          b.vel.z -= (p.z / r) * 10 * dt;
        }
        if (p.y < -40) { p.y = sea + 20; b.vel.set(0, 0, 0); }
        if (b.node._flag) b.node._flag.rotation.y = Math.sin(t * 6 + p.x) * 0.35;
        // buoy rope visual
        if (b.rope && b.anchor) {
          const bottom = hAt ? hAt(b.anchor.x, b.anchor.z) : -10;
          const pts = [];
          for (let i = 0; i <= 7; i++) {
            const k = i / 7;
            const ax = lerp(p.x, b.anchor.x, k), az = lerp(p.z, b.anchor.z, k);
            const ay = lerp(p.y - 0.7, bottom, k) - Math.sin(k * Math.PI) * 0.8;
            pts.push(new BABYLON.Vector3(ax, ay, az));
          }
          BABYLON.MeshBuilder.CreateLines("rope", { points: pts, instance: b.rope }, this.scene);
        }
      }
      // pairwise soft separation
      const L = this.list;
      for (let i = 0; i < L.length; i++) {
        for (let j = i + 1; j < L.length; j++) {
          const A = L[i], B = L[j];
          const dx = B.node.position.x - A.node.position.x;
          const dz = B.node.position.z - A.node.position.z;
          const rr = A.def.r + B.def.r;
          const d2 = dx * dx + dz * dz;
          if (d2 < rr * rr && d2 > 1e-4) {
            const d = Math.sqrt(d2);
            const push = (rr - d) / d * 0.5;
            if (!A.held) { A.node.position.x -= dx * push; A.node.position.z -= dz * push; }
            if (!B.held) { B.node.position.x += dx * push; B.node.position.z += dz * push; }
            const ix = dx / d * push * 2, iz = dz / d * push * 2;
            A.vel.x -= ix * 0.2; A.vel.z -= iz * 0.2;
            B.vel.x += ix * 0.2; B.vel.z += iz * 0.2;
          }
        }
      }
      // rings animation
      for (let i = this.rings.length - 1; i >= 0; i--) {
        const ring = this.rings[i];
        ring.t += dt;
        const k = ring.t / 1.15;
        if (k >= 1) {
          if (ring.mesh.material) ring.mesh.material.dispose();
          ring.mesh.dispose();
          this.rings.splice(i, 1);
          continue;
        }
        const sc = (1 + k * 10) * ring.scale;
        ring.mesh.scaling.set(sc, 1 + k * 0.5, sc);
        ring.mesh.material.alpha = 0.55 * (1 - k) / Math.sqrt(ring.scale);
      }
    }
  }

  window.PHYS = { WaveSet, Floaters, Cloth, KINDS, whirlDip };
})();
