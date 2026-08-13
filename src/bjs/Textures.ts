/**
 * Procedurally baked textures. Nothing is loaded from disk, so there is no
 * asset that can 404 and no network dependency at runtime.
 */

import { DynamicTexture } from '@babylonjs/core/Materials/Textures/dynamicTexture';
import type { Scene } from '@babylonjs/core/scene';

export function starfieldTexture(scene: Scene, size = 4096): DynamicTexture {
  const dt = new DynamicTexture('starfield', { width: size, height: size / 2 }, scene, false);
  const c = dt.getContext() as unknown as CanvasRenderingContext2D;
  const W = size, H = size / 2;

  c.fillStyle = '#010206';
  c.fillRect(0, 0, W, H);

  // ---------- deep-field nebulosity ----------
  // Layered, irregular clouds rather than tidy circles. Real nebulae are
  // filamentary, so each blob is drawn several times at offsets to break up
  // the perfect radial falloff that reads as "flat board with stars".
  const PALETTES = [
    ['140,70,220', '80,40,160'],   // violet
    ['30,110,220', '20,60,150'],   // blue
    ['220,80,120', '150,40,90'],   // magenta
    ['40,180,190', '20,90,110'],   // teal
    ['230,140,60', '150,70,30']    // amber
  ];

  for (let i = 0; i < 46; i++) {
    const pal = PALETTES[Math.floor(Math.random() * PALETTES.length)];
    const cx = Math.random() * W;
    const cy = Math.random() * H;
    const baseR = 120 + Math.random() * 620;

    // several overlapping lobes make an irregular, cloud-like shape
    const lobes = 3 + Math.floor(Math.random() * 5);
    for (let k = 0; k < lobes; k++) {
      const ox = cx + (Math.random() - 0.5) * baseR * 1.3;
      const oy = cy + (Math.random() - 0.5) * baseR * 0.9;
      const r = baseR * (0.35 + Math.random() * 0.75);
      const g = c.createRadialGradient(ox, oy, 0, ox, oy, r);
      const hue = Math.random() < 0.55 ? pal[0] : pal[1];
      const a = 0.05 + Math.random() * 0.13;
      g.addColorStop(0, `rgba(${hue},${a})`);
      g.addColorStop(0.45, `rgba(${hue},${a * 0.45})`);
      g.addColorStop(1, 'rgba(0,0,0,0)');
      c.fillStyle = g;
      c.fillRect(ox - r, oy - r, r * 2, r * 2);
    }
  }

  // ---------- dark dust lanes ----------
  // Nebulae are as much about what blocks light as what emits it.
  c.globalCompositeOperation = 'destination-out';
  for (let i = 0; i < 90; i++) {
    const x = Math.random() * W;
    const y = Math.random() * H;
    const r = 40 + Math.random() * 260;
    const g = c.createRadialGradient(x, y, 0, x, y, r);
    g.addColorStop(0, `rgba(0,0,0,${0.18 + Math.random() * 0.35})`);
    g.addColorStop(1, 'rgba(0,0,0,0)');
    c.fillStyle = g;
    c.fillRect(x - r, y - r, r * 2, r * 2);
  }
  c.globalCompositeOperation = 'source-over';

  // ---------- the galactic band ----------
  // A dense, slightly tilted river of stars. This single feature does more
  // than anything else to stop the sky looking like uniform noise.
  const bandY = H * (0.40 + Math.random() * 0.20);
  const tilt = (Math.random() - 0.5) * H * 0.20;
  for (let i = 0; i < 34000; i++) {
    const t = Math.random();
    const x = t * W;
    // gaussian-ish spread, tight to the band centre
    const spread = (Math.random() + Math.random() + Math.random() - 1.5) * H * 0.085;
    const y = bandY + tilt * (t - 0.5) * 2 + spread
            + Math.sin(t * 9.0) * H * 0.02;
    if (y < 0 || y >= H) continue;
    const b = Math.pow(Math.random(), 2.6);
    const a = 0.05 + b * 0.5;
    const col = Math.random();
    c.fillStyle =
      col < 0.70 ? `rgba(255,250,240,${a})`
      : col < 0.88 ? `rgba(190,212,255,${a})`
      : `rgba(255,206,164,${a})`;
    c.fillRect(x, y, 1, 1);
  }

  // a soft glow along the band, as if unresolved stars blend together
  const bg = c.createLinearGradient(0, bandY - H * 0.16, 0, bandY + H * 0.16);
  bg.addColorStop(0, 'rgba(0,0,0,0)');
  bg.addColorStop(0.5, 'rgba(150,175,225,0.075)');
  bg.addColorStop(1, 'rgba(0,0,0,0)');
  c.fillStyle = bg;
  c.fillRect(0, bandY - H * 0.16, W, H * 0.32);

  // ---------- field stars ----------
  for (let i = 0; i < 26000; i++) {
    const x = Math.random() * W;
    const y = Math.random() * H;
    const b = Math.pow(Math.random(), 3.4);
    const r = b * 1.9 + 0.22;
    const t = Math.random();
    const a = 0.18 + b;
    c.fillStyle =
      t < 0.68 ? `rgba(255,255,255,${a})`
      : t < 0.84 ? `rgba(176,203,255,${a})`
      : t < 0.95 ? `rgba(255,208,158,${a})`
      : `rgba(255,150,120,${a})`;
    c.beginPath();
    c.arc(x, y, r, 0, 6.284);
    c.fill();
  }

  // ---------- bright named stars, with glare ----------
  // A handful of genuinely bright stars with halos and diffraction spikes.
  // Without these the sky has no focal points and reads as static.
  for (let i = 0; i < 130; i++) {
    const x = Math.random() * W;
    const y = Math.random() * H;
    const mag = 0.55 + Math.random() * 0.45;
    const t = Math.random();
    const tint = t < 0.6 ? '255,255,255'
      : t < 0.8 ? '178,206,255'
      : '255,205,160';

    // halo
    const hr = 8 + mag * 26;
    const hg = c.createRadialGradient(x, y, 0, x, y, hr);
    hg.addColorStop(0, `rgba(${tint},${0.55 * mag})`);
    hg.addColorStop(0.25, `rgba(${tint},${0.16 * mag})`);
    hg.addColorStop(1, 'rgba(0,0,0,0)');
    c.fillStyle = hg;
    c.fillRect(x - hr, y - hr, hr * 2, hr * 2);

    // diffraction spikes
    const sl = hr * (1.4 + Math.random() * 1.1);
    c.strokeStyle = `rgba(${tint},${0.22 * mag})`;
    c.lineWidth = 1;
    c.beginPath();
    c.moveTo(x - sl, y); c.lineTo(x + sl, y);
    c.moveTo(x, y - sl * 0.55); c.lineTo(x, y + sl * 0.55);
    c.stroke();

    // core
    c.fillStyle = `rgba(255,255,255,${0.85 * mag})`;
    c.beginPath();
    c.arc(x, y, 1.0 + mag * 1.3, 0, 6.284);
    c.fill();
  }

  // ---------- distant galaxies ----------
  for (let i = 0; i < 22; i++) {
    const x = Math.random() * W;
    const y = Math.random() * H;
    const r = 5 + Math.random() * 20;
    const g = c.createRadialGradient(x, y, 0, x, y, r);
    const warm = Math.random() < 0.5;
    g.addColorStop(0, warm ? 'rgba(255,235,205,0.42)' : 'rgba(210,225,255,0.38)');
    g.addColorStop(0.5, warm ? 'rgba(220,180,140,0.13)' : 'rgba(150,180,240,0.12)');
    g.addColorStop(1, 'rgba(0,0,0,0)');
    c.save();
    c.translate(x, y);
    c.rotate(Math.random() * 6.283);
    c.scale(1, 0.32 + Math.random() * 0.4);   // ellipse, seen at an angle
    c.fillStyle = g;
    c.beginPath();
    c.arc(0, 0, r, 0, 6.284);
    c.fill();
    c.restore();
  }

  dt.update();
  return dt;
}

export function ringTexture(scene: Scene, size = 1024): DynamicTexture {
  const dt = new DynamicTexture('ring', { width: size, height: size }, scene, false);
  const c = dt.getContext() as unknown as CanvasRenderingContext2D;
  c.clearRect(0, 0, size, size);
  const cx = size / 2;

  for (let i = 0; i < 620; i++) {
    const t = Math.random();
    const rad = cx * (0.42 + t * 0.56);
    const gap = Math.sin(t * 34) * 0.5 + 0.5;      // Cassini-style divisions
    const a = (0.05 + Math.random() * 0.4) * gap * (1 - Math.abs(t - 0.5) * 0.7);
    const g = 200 + Math.random() * 45;
    c.strokeStyle = `rgba(${g},${g - 22},${g - 55},${a})`;
    c.lineWidth = 0.7 + Math.random() * 2.6;
    c.beginPath();
    c.arc(cx, cx, rad, 0, 6.284);
    c.stroke();
  }

  dt.update();
  dt.hasAlpha = true;
  return dt;
}
