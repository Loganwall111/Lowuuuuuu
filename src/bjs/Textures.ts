/**
 * Procedurally baked textures. Nothing is loaded from disk, so there is no
 * asset that can 404 and no network dependency at runtime.
 */

import { DynamicTexture } from '@babylonjs/core/Materials/Textures/dynamicTexture';
import type { Scene } from '@babylonjs/core/scene';

export function starfieldTexture(scene: Scene, size = 2048): DynamicTexture {
  const dt = new DynamicTexture('starfield', { width: size, height: size / 2 }, scene, false);
  const c = dt.getContext() as unknown as CanvasRenderingContext2D;
  c.fillStyle = '#000308';
  c.fillRect(0, 0, size, size / 2);

  // deep-field nebula wash
  for (let i = 0; i < 26; i++) {
    const x = Math.random() * size;
    const y = (Math.random() * size) / 2;
    const r = 90 + Math.random() * 320;
    const g = c.createRadialGradient(x, y, 0, x, y, r);
    const hue = Math.random() < 0.5 ? '120,60,200' : '30,90,190';
    g.addColorStop(0, `rgba(${hue},0.16)`);
    g.addColorStop(1, 'rgba(0,0,0,0)');
    c.fillStyle = g;
    c.fillRect(x - r, y - r, r * 2, r * 2);
  }

  // stars with plausible colour classes
  for (let i = 0; i < 9000; i++) {
    const x = Math.random() * size;
    const y = (Math.random() * size) / 2;
    const b = Math.pow(Math.random(), 3.2);
    const r = b * 1.7 + 0.25;
    const t = Math.random();
    c.fillStyle =
      t < 0.72 ? `rgba(255,255,255,${0.25 + b})`
      : t < 0.88 ? `rgba(180,205,255,${0.25 + b})`
      : `rgba(255,205,160,${0.25 + b})`;
    c.beginPath();
    c.arc(x, y, r, 0, 6.284);
    c.fill();
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
