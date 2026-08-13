import * as THREE from 'three';

/**
 * SceneBuilder — procedurally generates all visible worlds.
 * Every texture is generated at runtime on a canvas, so there are zero asset
 * downloads and nothing to 404.
 */

function canvasTexture(size: number, draw: (ctx: CanvasRenderingContext2D, s: number) => void): THREE.CanvasTexture {
  const c = document.createElement('canvas');
  c.width = c.height = size;
  const ctx = c.getContext('2d')!;
  draw(ctx, size);
  const t = new THREE.CanvasTexture(c);
  t.anisotropy = 4;
  return t;
}

/** Soft radial glow sprite used for stars, suns and the black-hole halo. */
function glowSprite(color: string, size = 256): THREE.Sprite {
  const tex = canvasTexture(size, (ctx, s) => {
    const g = ctx.createRadialGradient(s / 2, s / 2, 0, s / 2, s / 2, s / 2);
    g.addColorStop(0, color);
    g.addColorStop(0.25, color);
    g.addColorStop(1, 'rgba(0,0,0,0)');
    ctx.fillStyle = g;
    ctx.fillRect(0, 0, s, s);
  });
  const mat = new THREE.SpriteMaterial({
    map: tex,
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  });
  return new THREE.Sprite(mat);
}

/** Value-noise continents for an earth-like world. */
function planetTexture(hue: number): THREE.CanvasTexture {
  return canvasTexture(512, (ctx, s) => {
    ctx.fillStyle = `hsl(${hue}, 70%, 22%)`;
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 900; i++) {
      const x = Math.random() * s;
      const y = Math.random() * s;
      const r = Math.random() * 46 + 8;
      const light = 30 + Math.random() * 34;
      ctx.fillStyle = `hsla(${hue + Math.random() * 40 - 20}, 55%, ${light}%, 0.5)`;
      ctx.beginPath();
      ctx.arc(x, y, r, 0, Math.PI * 2);
      ctx.fill();
    }
    // Polar ice
    const grd = ctx.createLinearGradient(0, 0, 0, s);
    grd.addColorStop(0, 'rgba(255,255,255,0.85)');
    grd.addColorStop(0.16, 'rgba(255,255,255,0)');
    grd.addColorStop(0.84, 'rgba(255,255,255,0)');
    grd.addColorStop(1, 'rgba(255,255,255,0.85)');
    ctx.fillStyle = grd;
    ctx.fillRect(0, 0, s, s);
  });
}

function rockTexture(): THREE.CanvasTexture {
  return canvasTexture(256, (ctx, s) => {
    ctx.fillStyle = '#6b6b73';
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 1400; i++) {
      const g = 60 + Math.random() * 90;
      ctx.fillStyle = `rgba(${g},${g},${g + 6},0.55)`;
      ctx.beginPath();
      ctx.arc(Math.random() * s, Math.random() * s, Math.random() * 9 + 1, 0, Math.PI * 2);
      ctx.fill();
    }
  });
}

/** Fresnel rim-glow shell that reads as an atmosphere. */
function atmosphere(radius: number, color: THREE.Color, power = 3.2): THREE.Mesh {
  const mat = new THREE.ShaderMaterial({
    uniforms: { glowColor: { value: color }, power: { value: power } },
    vertexShader: `
      varying vec3 vNormal;
      varying vec3 vView;
      void main() {
        vNormal = normalize(normalMatrix * normal);
        vec4 mv = modelViewMatrix * vec4(position, 1.0);
        vView = normalize(-mv.xyz);
        gl_Position = projectionMatrix * mv;
      }`,
    fragmentShader: `
      uniform vec3 glowColor;
      uniform float power;
      varying vec3 vNormal;
      varying vec3 vView;
      void main() {
        float rim = pow(1.0 - abs(dot(vNormal, vView)), power);
        gl_FragColor = vec4(glowColor, rim);
      }`,
    transparent: true,
    blending: THREE.AdditiveBlending,
    side: THREE.BackSide,
    depthWrite: false
  });
  return new THREE.Mesh(new THREE.SphereGeometry(radius, 48, 48), mat);
}

export interface WorldHandles {
  group: THREE.Group;
  tick: (t: number, dt: number) => void;
  bodies: number;
}

export class SceneBuilder {
  private shaderClocks: THREE.ShaderMaterial[] = [];

  /** Deep-space backdrop shared by every world. */
  buildStarfield(count = 12000): THREE.Points {
    const geo = new THREE.BufferGeometry();
    const pos = new Float32Array(count * 3);
    const col = new Float32Array(count * 3);
    const c = new THREE.Color();
    for (let i = 0; i < count; i++) {
      // Distribute on a shell so stars never sit inside the play space.
      const r = 420 + Math.random() * 380;
      const th = Math.random() * Math.PI * 2;
      const ph = Math.acos(2 * Math.random() - 1);
      pos[i * 3] = r * Math.sin(ph) * Math.cos(th);
      pos[i * 3 + 1] = r * Math.cos(ph);
      pos[i * 3 + 2] = r * Math.sin(ph) * Math.sin(th);
      // Stellar classification tint
      const h = Math.random();
      c.setHSL(h < 0.75 ? 0.58 : 0.08, 0.35 + Math.random() * 0.4, 0.72 + Math.random() * 0.28);
      col[i * 3] = c.r; col[i * 3 + 1] = c.g; col[i * 3 + 2] = c.b;
    }
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
    geo.setAttribute('color', new THREE.BufferAttribute(col, 3));
    const mat = new THREE.PointsMaterial({
      size: 2.1,
      sizeAttenuation: true,
      vertexColors: true,
      transparent: true,
      opacity: 0.95,
      depthWrite: false,
      blending: THREE.AdditiveBlending
    });
    return new THREE.Points(geo, mat);
  }

  /** PLANETARY — an earth-like world, atmosphere, moon and orbiting debris. */
  buildPlanetary(): WorldHandles {
    const group = new THREE.Group();

    const planet = new THREE.Mesh(
      new THREE.SphereGeometry(5, 96, 96),
      new THREE.MeshStandardMaterial({ map: planetTexture(205), roughness: 0.85, metalness: 0.05 })
    );
    planet.castShadow = planet.receiveShadow = true;
    group.add(planet);
    group.add(atmosphere(5.42, new THREE.Color(0x4ea8ff), 3.0));

    // Moon on an inclined orbit
    const moonPivot = new THREE.Group();
    moonPivot.rotation.z = 0.32;
    const moon = new THREE.Mesh(
      new THREE.SphereGeometry(1.15, 48, 48),
      new THREE.MeshStandardMaterial({ map: rockTexture(), roughness: 1 })
    );
    moon.position.set(11.5, 0, 0);
    moon.castShadow = moon.receiveShadow = true;
    moonPivot.add(moon);
    group.add(moonPivot);

    // Debris belt via instancing — thousands of rocks, one draw call
    const N = 1500;
    const belt = new THREE.InstancedMesh(
      new THREE.IcosahedronGeometry(0.11, 0),
      new THREE.MeshStandardMaterial({ color: 0x9a9aa6, roughness: 1, flatShading: true }),
      N
    );
    const m = new THREE.Matrix4();
    const q = new THREE.Quaternion();
    const scl = new THREE.Vector3();
    const beltData: { a: number; r: number; y: number; s: number; spin: number }[] = [];
    for (let i = 0; i < N; i++) {
      const a = Math.random() * Math.PI * 2;
      const r = 16 + Math.random() * 5;
      const y = (Math.random() - 0.5) * 1.1;
      const s = 0.4 + Math.random() * 1.5;
      beltData.push({ a, r, y, s, spin: Math.random() * 2 });
      scl.setScalar(s);
      m.compose(new THREE.Vector3(Math.cos(a) * r, y, Math.sin(a) * r), q, scl);
      belt.setMatrixAt(i, m);
    }
    group.add(belt);

    // Distant sun
    const sun = glowSprite('rgba(255,238,190,0.95)', 512);
    sun.scale.setScalar(90);
    sun.position.set(180, 70, -150);
    group.add(sun);

    let beltAngle = 0;
    const tick = (t: number, dt: number) => {
      planet.rotation.y += dt * 0.06;
      moonPivot.rotation.y += dt * 0.22;
      moon.rotation.y += dt * 0.1;
      beltAngle += dt * 0.05;
      for (let i = 0; i < N; i++) {
        const d = beltData[i];
        const a = d.a + beltAngle * (18 / d.r);
        scl.setScalar(d.s * 0.11 / 0.11);
        q.setFromAxisAngle(new THREE.Vector3(0, 1, 0), a * d.spin);
        m.compose(new THREE.Vector3(Math.cos(a) * d.r, d.y, Math.sin(a) * d.r), q, scl);
        belt.setMatrixAt(i, m);
      }
      belt.instanceMatrix.needsUpdate = true;
    };

    return { group, tick, bodies: N + 3 };
  }

  /** STELLAR — a black hole with a hot accretion disk and photon halo. */
  buildStellar(): WorldHandles {
    const group = new THREE.Group();

    // Event horizon: pure black, unlit
    const horizon = new THREE.Mesh(
      new THREE.SphereGeometry(3.1, 64, 64),
      new THREE.MeshBasicMaterial({ color: 0x000000 })
    );
    group.add(horizon);

    // Photon ring
    const halo = glowSprite('rgba(255,176,92,0.9)', 512);
    halo.scale.setScalar(15.5);
    group.add(halo);

    // Accretion disk — doppler-shifted, turbulent
    const diskMat = new THREE.ShaderMaterial({
      uniforms: { time: { value: 0 } },
      vertexShader: `
        varying vec2 vUv;
        varying vec3 vPos;
        void main() {
          vUv = uv; vPos = position;
          gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
        }`,
      fragmentShader: `
        uniform float time;
        varying vec2 vUv;
        varying vec3 vPos;
        float hash(vec2 p){ return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
        float noise(vec2 p){
          vec2 i = floor(p), f = fract(p);
          vec2 u = f*f*(3.0-2.0*f);
          return mix(mix(hash(i), hash(i+vec2(1,0)), u.x),
                     mix(hash(i+vec2(0,1)), hash(i+vec2(1,1)), u.x), u.y);
        }
        void main() {
          float r = length(vPos.xy);
          float ang = atan(vPos.y, vPos.x);
          // swirl
          float sw = ang * 3.0 + time * 1.6 - r * 0.55;
          float n = noise(vec2(sw * 1.6, r * 1.4)) * 0.55
                  + noise(vec2(sw * 4.2, r * 3.1)) * 0.3;
          // radial falloff between inner and outer edge
          float inner = smoothstep(3.4, 5.0, r);
          float outer = 1.0 - smoothstep(9.0, 14.5, r);
          float body = inner * outer;
          // hotter toward the middle
          float heat = 1.0 - smoothstep(4.0, 13.0, r);
          vec3 hot  = vec3(1.0, 0.95, 0.82);
          vec3 mid  = vec3(1.0, 0.55, 0.16);
          vec3 cold = vec3(0.65, 0.12, 0.02);
          vec3 col = mix(cold, mid, heat);
          col = mix(col, hot, pow(heat, 3.0));
          // relativistic beaming: one side brighter
          float beam = 0.55 + 0.75 * smoothstep(-1.0, 1.0, sin(ang + 1.2));
          float a = body * (0.32 + n) * beam;
          gl_FragColor = vec4(col * (0.85 + n * 1.5) * beam, clamp(a, 0.0, 1.0));
        }`,
      transparent: true,
      side: THREE.DoubleSide,
      blending: THREE.AdditiveBlending,
      depthWrite: false
    });
    const disk = new THREE.Mesh(new THREE.RingGeometry(3.4, 14.5, 160, 1), diskMat);
    disk.rotation.x = -Math.PI / 2.32;
    group.add(disk);
    this.shaderClocks.push(diskMat);

    // Infalling companion star
    const compPivot = new THREE.Group();
    const comp = new THREE.Mesh(
      new THREE.SphereGeometry(0.85, 32, 32),
      new THREE.MeshBasicMaterial({ color: 0xfff0c0 })
    );
    comp.position.set(22, 1.5, 0);
    const compGlow = glowSprite('rgba(255,235,180,0.9)', 256);
    compGlow.scale.setScalar(7);
    comp.add(compGlow);
    compPivot.add(comp);
    group.add(compPivot);

    const tick = (t: number, dt: number) => {
      diskMat.uniforms.time.value = t;
      disk.rotation.z += dt * 0.05;
      compPivot.rotation.y += dt * 0.16;
      halo.scale.setScalar(15.5 + Math.sin(t * 1.7) * 0.35);
    };

    return { group, tick, bodies: 4 };
  }

  /** FLUID LAB — Gerstner ocean over a seabed, with buoyant props. */
  buildFluid(): WorldHandles {
    const group = new THREE.Group();

    const waterMat = new THREE.ShaderMaterial({
      uniforms: {
        time: { value: 0 },
        deep: { value: new THREE.Color(0x03263f) },
        shallow: { value: new THREE.Color(0x2ea6c7) },
        sunDir: { value: new THREE.Vector3(0.5, 0.8, 0.3).normalize() }
      },
      vertexShader: `
        uniform float time;
        varying vec3 vNormal;
        varying vec3 vView;
        varying float vHeight;
        // Sum of Gerstner-style sines
        float wave(vec2 p, vec2 dir, float freq, float speed, float amp) {
          return sin(dot(p, dir) * freq + time * speed) * amp;
        }
        float height(vec2 p) {
          float h = 0.0;
          h += wave(p, normalize(vec2(1.0, 0.35)), 0.30, 1.10, 1.05);
          h += wave(p, normalize(vec2(-0.6, 1.0)), 0.46, 1.55, 0.62);
          h += wave(p, normalize(vec2(0.4, -0.9)), 0.92, 2.10, 0.28);
          h += wave(p, normalize(vec2(-1.0, -0.3)), 1.70, 2.80, 0.13);
          return h;
        }
        void main() {
          vec3 pos = position;
          float h = height(pos.xy);
          pos.z += h;
          vHeight = h;
          // finite-difference normal
          float e = 0.6;
          float hx = height(pos.xy + vec2(e, 0.0));
          float hy = height(pos.xy + vec2(0.0, e));
          vec3 n = normalize(vec3(h - hx, h - hy, e));
          vNormal = normalize(normalMatrix * n);
          vec4 mv = modelViewMatrix * vec4(pos, 1.0);
          vView = normalize(-mv.xyz);
          gl_Position = projectionMatrix * mv;
        }`,
      fragmentShader: `
        uniform vec3 deep;
        uniform vec3 shallow;
        uniform vec3 sunDir;
        varying vec3 vNormal;
        varying vec3 vView;
        varying float vHeight;
        void main() {
          float fres = pow(1.0 - max(dot(vNormal, vView), 0.0), 2.4);
          vec3 base = mix(deep, shallow, clamp(vHeight * 0.42 + 0.5, 0.0, 1.0));
          // specular glint
          vec3 h = normalize(sunDir + vView);
          float spec = pow(max(dot(vNormal, h), 0.0), 90.0);
          // foam on crests
          float foam = smoothstep(1.35, 2.0, vHeight);
          vec3 col = base + fres * 0.5 + spec * 1.6 + foam * 0.6;
          gl_FragColor = vec4(col, 0.93);
        }`,
      transparent: true,
      side: THREE.DoubleSide
    });
    const ocean = new THREE.Mesh(new THREE.PlaneGeometry(150, 150, 220, 220), waterMat);
    ocean.rotation.x = -Math.PI / 2;
    group.add(ocean);
    this.shaderClocks.push(waterMat);

    // Seabed
    const bedGeo = new THREE.PlaneGeometry(150, 150, 90, 90);
    const bp = bedGeo.attributes.position;
    for (let i = 0; i < bp.count; i++) {
      const x = bp.getX(i), y = bp.getY(i);
      bp.setZ(i, Math.sin(x * 0.07) * Math.cos(y * 0.06) * 3.2 + Math.random() * 0.5);
    }
    bedGeo.computeVertexNormals();
    const bed = new THREE.Mesh(bedGeo, new THREE.MeshStandardMaterial({ color: 0x2c2a26, roughness: 1 }));
    bed.rotation.x = -Math.PI / 2;
    bed.position.y = -9;
    bed.receiveShadow = true;
    group.add(bed);

    // Buoyant props
    const props: { mesh: THREE.Mesh; phase: number; x: number; z: number }[] = [];
    const palette = [0xe8623c, 0x3ce89a, 0xf2c94c, 0x9b6cf0, 0x4ea8ff];
    for (let i = 0; i < 14; i++) {
      const s = 0.8 + Math.random() * 1.5;
      const mesh = new THREE.Mesh(
        i % 3 === 0
          ? new THREE.BoxGeometry(s, s, s)
          : new THREE.SphereGeometry(s * 0.6, 28, 28),
        new THREE.MeshStandardMaterial({
          color: palette[i % palette.length],
          roughness: 0.35,
          metalness: 0.15
        })
      );
      const x = (Math.random() - 0.5) * 60;
      const z = (Math.random() - 0.5) * 60;
      mesh.position.set(x, 0, z);
      mesh.castShadow = true;
      props.push({ mesh, phase: Math.random() * 6.28, x, z });
      group.add(mesh);
    }

    // Match the vertex shader so props ride the actual surface
    const sampleHeight = (x: number, z: number, t: number) => {
      const w = (dx: number, dy: number, f: number, sp: number, a: number) =>
        Math.sin((x * dx + z * dy) * f + t * sp) * a;
      let h = 0;
      h += w(0.944, 0.330, 0.30, 1.10, 1.05);
      h += w(-0.514, 0.857, 0.46, 1.55, 0.62);
      h += w(0.406, -0.914, 0.92, 2.10, 0.28);
      h += w(-0.958, -0.287, 1.70, 2.80, 0.13);
      return h;
    };

    const tick = (t: number, _dt: number) => {
      waterMat.uniforms.time.value = t;
      for (const p of props) {
        const h = sampleHeight(p.x, p.z, t);
        p.mesh.position.y = h + 0.35;
        p.mesh.rotation.x = Math.sin(t * 1.2 + p.phase) * 0.16;
        p.mesh.rotation.z = Math.cos(t * 0.9 + p.phase) * 0.16;
      }
    };

    return { group, tick, bodies: props.length + 2 };
  }
}
