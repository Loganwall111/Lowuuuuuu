/**
 * Shell — the application chrome: top bar, HUD, and all floating windows.
 * Every panel is owned by the WindowManager; nothing here ever covers the
 * simulation full-screen or captures input outside its own frame.
 */

import { WindowManager } from './WindowManager';
import { UI_CSS } from './styles';
import type { World, WorldParam } from '../World';
import { POSTFX_PARAMS, DEFAULT_POSTFX } from '../PostFX';
import { CATALOG, CATEGORIES, SCALES, randomObject, type ObjectDef } from '../content/ObjectCatalog';

export type Mode = 'simple' | 'advanced' | 'expert';

export interface WorldEntry {
  id: string;
  name: string;
  glyph: string;
  desc: string;
  tags: string[];
}

export const WORLDS: WorldEntry[] = [
  { id: 'sandbox', name: 'Gravity Sandbox', glyph: '🌌', desc: 'Build systems, launch bodies, watch them collide and merge', tags: ['create', 'gravity', 'n-body', 'orbit', 'collision', 'sandbox'] },
  { id: 'planetary', name: 'Star Systems', glyph: '🪐', desc: 'Procedural planets, moons, rings and atmospheres', tags: ['space', 'planets', 'orbit', 'gravity'] },
  { id: 'ocean', name: 'Ocean Worlds', glyph: '🌊', desc: 'Gerstner fluid with ray-traced reflections', tags: ['water', 'fluid', 'waves', 'reflection'] },
  { id: 'blackhole', name: 'Singularity', glyph: '⚫', desc: 'Ray-marched geodesics and gravitational lensing', tags: ['black hole', 'lensing', 'relativity', 'gravity'] }
];

interface ShellHooks {
  onWorld: (id: string) => void;
  onParam: (key: string, value: number) => void;
  onAction: (key: string) => void;
  onMode: (m: Mode) => void;
  onReset: () => void;
  onPause: (paused: boolean) => void;
  onPostFX: (key: string, value: number) => void;
  onSpawn: (objectId: string, scale: number) => void;
  onUndo: () => string | null;
  onRedo: () => string | null;
  onSaveSnapshot: (label: string) => unknown;
  onLoadSnapshot: (id: string) => boolean;
  listSnapshots: () => { id: string; label: string; time: number }[];
  canUndo: () => boolean;
  canRedo: () => boolean;
}

export class Shell {
  wm: WindowManager;
  mode: Mode = 'simple';
  private hooks: ShellHooks;
  private world: World | null = null;
  private worldId = 'sandbox';
  private paused = false;
  private favs = new Set<string>();
  private fpsHist: number[] = [];
  private postfx: Record<string, number> = { ...DEFAULT_POSTFX } as any;
  private objScale = 1;
  private objCat: string = 'All';

  private topbar!: HTMLDivElement;
  private hud!: HTMLDivElement;
  private boot!: HTMLDivElement;

  constructor(hooks: ShellHooks) {
    this.hooks = hooks;

    const style = document.createElement('style');
    style.textContent = UI_CSS;
    document.head.appendChild(style);

    this.wm = new WindowManager(document.body);
    this.buildBoot();
    this.buildTopbar();
    this.buildHud();
    this.registerWindows();

    this.wm.onChange(() => this.syncTopbar());

    window.addEventListener('keydown', (e) => {
      if ((e.target as HTMLElement)?.tagName === 'INPUT') return;
      const k = e.key.toLowerCase();
      if ((e.ctrlKey || e.metaKey) && k === 'z') {
        e.preventDefault();
        e.shiftKey ? this.hooks.onRedo() : this.hooks.onUndo();
        this.refreshAll();
        return;
      }
      if ((e.ctrlKey || e.metaKey) && k === 'y') {
        e.preventDefault(); this.hooks.onRedo(); this.refreshAll(); return;
      }
      if (k === '1') this.wm.Toggle('controls');
      else if (k === '2') this.wm.Toggle('objects');
      else if (k === '6') this.wm.Toggle('library');
      else if (k === '7') this.wm.Toggle('snapshots');
      else if (k === '3') this.wm.Toggle('telemetry');
      else if (k === '4') this.wm.Toggle('presets');
      else if (k === '5') this.wm.Toggle('graphics');
      else if (k === 'h') this.wm.CloseAll();
      else if (k === ' ') { e.preventDefault(); this.togglePause(); }
      else if (k === 'r') this.hooks.onReset();
    });
  }

  /* ------------------------------- boot ------------------------------- */

  private buildBoot(): void {
    this.boot = document.createElement('div');
    this.boot.className = 'boot';
    this.boot.innerHTML = `
      <div class="boot-in">
        <div class="boot-name">LOW</div>
        <div class="boot-sub">Universe Sandbox</div>
        <div class="boot-bar"><div class="boot-fill" id="bootFill"></div></div>
        <div class="boot-msg" id="bootMsg">initialising</div>
      </div>`;
    document.body.appendChild(this.boot);
  }

  progress(pct: number, msg: string): void {
    const f = document.getElementById('bootFill');
    const m = document.getElementById('bootMsg');
    if (f) f.style.width = pct + '%';
    if (m) m.textContent = msg;
  }

  /** Removes the boot overlay from the DOM entirely. Safe to call repeatedly. */
  hideBoot(): void {
    if (!this.boot) return;
    this.boot.classList.add('gone');
    const el = this.boot;
    setTimeout(() => el.remove(), 500);
    // Hard guarantee: even if the transition never fires, it cannot block input.
    el.style.pointerEvents = 'none';
  }

  /** Surfaces a fatal error instead of leaving the user at a black screen. */
  showBootError(err: unknown): void {
    const msg = (err as any)?.message ?? String(err);
    if (!this.boot || !this.boot.isConnected) {
      // Boot already gone; use a floating, dismissible toast instead.
      const t = document.createElement('div');
      t.className = 'fatal-toast';
      t.innerHTML = `<b>Renderer error</b><div>${msg}</div><button>Dismiss</button>`;
      (t.querySelector('button') as HTMLButtonElement).onclick = () => t.remove();
      document.body.appendChild(t);
      return;
    }
    const inner = this.boot.querySelector('.boot-in') as HTMLElement;
    inner.innerHTML = `
      <div class="boot-name">LOW</div>
      <div class="boot-sub" style="color:#ff6b6b">Startup failed</div>
      <div class="boot-err">${msg}</div>
      <button class="btn pri" id="bootDismiss" style="margin-top:16px;min-width:160px">
        Continue anyway
      </button>`;
    (this.boot.querySelector('#bootDismiss') as HTMLButtonElement).onclick = () => this.hideBoot();
  }

  /* ------------------------------ topbar ------------------------------ */

  private buildTopbar(): void {
    this.topbar = document.createElement('div');
    this.topbar.className = 'topbar';
    this.topbar.innerHTML = `
      <div class="brand">
        <div class="brand-dot"></div>
        <div><div class="brand-name">LOW</div><div class="brand-sub">Universe Sandbox</div></div>
      </div>
      <div class="seg" id="worldSeg"></div>
      <div class="seg" id="modeSeg">
        <button data-m="simple" class="on">Simple</button>
        <button data-m="advanced">Advanced</button>
        <button data-m="expert">Expert</button>
      </div>
      <div class="spacer"></div>
      <button class="iconbtn" id="btnPause" title="Pause / Resume (Space)">⏸</button>
      <button class="iconbtn" id="w-controls" title="Controls (1)">🎛</button>
      <button class="iconbtn" id="w-objects"  title="Objects (2)">🧰</button>
      <button class="iconbtn" id="w-library"  title="World Library (6)">🗂</button>
      <button class="iconbtn" id="w-telemetry" title="Telemetry (3)">📊</button>
      <button class="iconbtn" id="w-presets"  title="Presets (4)">✨</button>
      <button class="iconbtn" id="w-graphics" title="Graphics (5)">🎨</button>
      <button class="iconbtn" id="btnUndo"    title="Undo (Ctrl+Z)">↶</button>
      <button class="iconbtn" id="btnRedo"    title="Redo (Ctrl+Shift+Z)">↷</button>
      <button class="iconbtn" id="w-snapshots" title="Snapshots (7)">📸</button>
      <button class="iconbtn" id="btnReset"   title="Reset layout & sim (R)">↺</button>
    `;
    document.body.appendChild(this.topbar);

    const seg = this.topbar.querySelector('#worldSeg')!;
    WORLDS.forEach((w) => {
      const b = document.createElement('button');
      b.textContent = `${w.glyph} ${w.name}`;
      b.dataset.w = w.id;
      if (w.id === this.worldId) b.classList.add('on');
      b.onclick = () => this.selectWorld(w.id);
      seg.appendChild(b);
    });

    this.topbar.querySelectorAll<HTMLButtonElement>('#modeSeg button').forEach((b) => {
      b.onclick = () => this.setMode(b.dataset.m as Mode);
    });

    (['controls', 'objects', 'library', 'telemetry', 'presets', 'graphics', 'snapshots'] as const).forEach((id) => {
      const btn = this.topbar.querySelector('#w-' + id) as HTMLButtonElement | null;
      if (btn) btn.onclick = () => this.wm.Toggle(id);
    });

    (this.topbar.querySelector('#btnUndo') as HTMLButtonElement).onclick = () => {
      this.hooks.onUndo(); this.refreshAll();
    };
    (this.topbar.querySelector('#btnRedo') as HTMLButtonElement).onclick = () => {
      this.hooks.onRedo(); this.refreshAll();
    };
    (this.topbar.querySelector('#btnPause') as HTMLButtonElement).onclick = () => this.togglePause();
    (this.topbar.querySelector('#btnReset') as HTMLButtonElement).onclick = () => {
      this.wm.Reset();
      this.hooks.onReset();
    };
  }

  private syncTopbar(): void {
    (['controls', 'objects', 'library', 'telemetry', 'presets', 'graphics', 'snapshots'] as const).forEach((id) => {
      const btn = this.topbar.querySelector('#w-' + id);
      btn?.classList.toggle('on', this.wm.IsVisible(id));
    });
  }

  private togglePause(): void {
    this.paused = !this.paused;
    const b = this.topbar.querySelector('#btnPause') as HTMLButtonElement;
    b.textContent = this.paused ? '▶' : '⏸';
    b.classList.toggle('on', this.paused);
    this.hooks.onPause(this.paused);
  }

  setMode(m: Mode): void {
    this.mode = m;
    this.topbar.querySelectorAll<HTMLButtonElement>('#modeSeg button')
      .forEach((b) => b.classList.toggle('on', b.dataset.m === m));
    this.hooks.onMode(m);
    this.wm.refresh('controls');
    this.wm.refresh('telemetry');
  }

  private selectWorld(id: string): void {
    this.worldId = id;
    this.topbar.querySelectorAll<HTMLButtonElement>('#worldSeg button')
      .forEach((b) => b.classList.toggle('on', b.dataset.w === id));
    this.hooks.onWorld(id);
  }

  setWorld(w: World): void {
    this.world = w;
    this.worldId = w.id;
    this.topbar.querySelectorAll<HTMLButtonElement>('#worldSeg button')
      .forEach((b) => b.classList.toggle('on', b.dataset.w === w.id));
    this.wm.refresh('controls');
    this.wm.refresh('telemetry');
    this.wm.refresh('library');
  }

  /* -------------------------------- HUD -------------------------------- */

  private buildHud(): void {
    this.hud = document.createElement('div');
    this.hud.className = 'hud';
    this.hud.innerHTML = `
      <div class="hud-chip"><div class="hud-k">FPS</div><div class="hud-v" id="hFps">–</div></div>
      <div class="hud-chip"><div class="hud-k">Renderer</div><div class="hud-v" id="hBackend" style="font-size:12px">–</div></div>
      <div class="hud-chip"><div class="hud-k">World</div><div class="hud-v" id="hWorld" style="font-size:12px">–</div></div>
    `;
    document.body.appendChild(this.hud);
  }

  /** Called once the main menu is dismissed: reveal the default panel set. */
  onMenuClosed(): void {
    this.wm.Open('controls');
    this.wm.Open('objects');
  }

  setBackend(b: string): void {
    const el = document.getElementById('hBackend');
    if (el) el.textContent = b;
  }

  tickHud(fps: number, worldName: string): void {
    const f = document.getElementById('hFps');
    if (f) f.innerHTML = `${Math.round(fps)} <small>fps</small>`;
    const w = document.getElementById('hWorld');
    if (w) w.textContent = worldName;
    this.fpsHist.push(fps);
    if (this.fpsHist.length > 120) this.fpsHist.shift();
  }

  /* ------------------------------ windows ------------------------------ */

  private registerWindows(): void {
    this.wm.register({
      id: 'controls', title: 'Controls', glyph: '🎛',
      x: 0, y: 0.10, width: 310, open: false,
      render: (b) => this.renderControls(b)
    });

    this.wm.register({
      id: 'library', title: 'World Library', glyph: '🗂',
      x: 0, y: 0.55, width: 340,
      render: (b) => this.renderLibrary(b)
    });

    this.wm.register({
      id: 'telemetry', title: 'Telemetry', glyph: '📊',
      x: 1, y: 0.10, width: 280,
      render: (b) => this.renderTelemetry(b)
    });

    this.wm.register({
      id: 'presets', title: 'Presets & Experiments', glyph: '✨',
      x: 1, y: 0.56, width: 290,
      render: (b) => this.renderPresets(b)
    });

    this.wm.register({
      id: 'objects', title: 'Objects', glyph: '🧰',
      x: 1, y: 0.10, width: 330, height: 520,
      render: (b) => this.renderObjects(b)
    });

    this.wm.register({
      id: 'snapshots', title: 'Snapshots & History', glyph: '📸',
      x: 0.5, y: 0.12, width: 310,
      render: (b) => this.renderSnapshots(b)
    });

    this.wm.register({
      id: 'graphics', title: 'Graphics', glyph: '🎨',
      x: 1, y: 0.33, width: 285,
      render: (b) => this.renderGraphics(b)
    });

    this.wm.register({
      id: 'help', title: 'Shortcuts', glyph: '⌨',
      x: 0.5, y: 0.62, width: 300,
      render: (b) => {
        b.innerHTML = `
          <div class="note">Every panel floats. Drag by the title bar, resize from the corner,
          minimize to the dock, or press <span class="kbd">Esc</span> to close the top one.
          The simulation always stays interactive behind them.</div>
          <div class="grp"><div class="grp-h">Keys</div>
            ${[['1', 'Controls'], ['2', 'World Library'], ['3', 'Telemetry'], ['4', 'Presets'],
               ['Space', 'Pause / resume'], ['R', 'Reset'], ['H', 'Hide all panels'], ['Esc', 'Close top panel']]
              .map(([k, d]) => `<div class="stat"><span class="stat-k">${d}</span><span class="kbd">${k}</span></div>`).join('')}
          </div>
          <div class="grp"><div class="grp-h">Camera</div>
            <div class="stat"><span class="stat-k">Orbit</span><span class="stat-v">Left drag</span></div>
            <div class="stat"><span class="stat-k">Pan</span><span class="stat-v">Right drag</span></div>
            <div class="stat"><span class="stat-k">Zoom</span><span class="stat-v">Wheel</span></div>
          </div>`;
      }
    });
  }

  /* ---- controls: progressive disclosure ---- */

  private renderControls(b: HTMLElement): void {
    if (!this.world) { b.innerHTML = '<div class="note">Loading world…</div>'; return; }

    const params = this.world.getParams();
    const limit = this.mode === 'simple' ? 4 : this.mode === 'advanced' ? 7 : params.length;
    const shown = params.slice(0, limit);

    const g = document.createElement('div');
    g.className = 'grp';
    g.innerHTML = `<div class="grp-h">${this.world.name}
      <span class="badge">${this.mode}</span></div>`;
    b.appendChild(g);

    if (this.mode === 'simple') {
      const n = document.createElement('div');
      n.className = 'note';
      n.textContent = 'Simple mode shows the essentials. Switch to Advanced or Expert for the full parameter set.';
      g.appendChild(n);
    }

    shown.forEach((p) => g.appendChild(this.slider(p)));

    const actions = this.world.getActions?.() ?? [];
    if (actions.length) {
      const ag = document.createElement('div');
      ag.className = 'grp';
      ag.innerHTML = '<div class="grp-h">Actions</div>';
      const row = document.createElement('div');
      row.className = 'btnrow';
      actions.forEach((a) => {
        const btn = document.createElement('button');
        btn.className = 'btn';
        btn.textContent = `${a.glyph ?? ''} ${a.label}`;
        btn.onclick = () => this.hooks.onAction(a.key);
        row.appendChild(btn);
      });
      ag.appendChild(row);
      b.appendChild(ag);
    }

    const sys = document.createElement('div');
    sys.className = 'grp';
    sys.innerHTML = '<div class="grp-h">Simulation</div>';
    const row = document.createElement('div');
    row.className = 'btnrow';
    const rb = document.createElement('button');
    rb.className = 'btn dgr';
    rb.textContent = '↺ Reset World';
    rb.onclick = () => this.hooks.onReset();
    const pb = document.createElement('button');
    pb.className = 'btn';
    pb.textContent = this.paused ? '▶ Resume' : '⏸ Pause';
    pb.onclick = () => { this.togglePause(); this.wm.refresh('controls'); };
    row.append(pb, rb);
    sys.appendChild(row);
    b.appendChild(sys);
  }

  private slider(p: WorldParam, onChange?: (k: string, v: number) => void): HTMLElement {
    const wrap = document.createElement('div');
    wrap.className = 'ctl';
    const fmt = (v: number) => (p.step >= 1 ? String(Math.round(v)) : v.toFixed(2)) + (p.unit ? ' ' + p.unit : '');

    wrap.innerHTML = `
      <div class="ctl-top"><span class="ctl-l">${p.label}</span><span class="ctl-v">${fmt(p.value)}</span></div>
    `;
    const input = document.createElement('input');
    input.type = 'range';
    input.min = String(p.min); input.max = String(p.max);
    input.step = String(p.step); input.value = String(p.value);
    const pct = ((p.value - p.min) / (p.max - p.min)) * 100;
    input.style.setProperty('--pct', pct + '%');

    const vEl = wrap.querySelector('.ctl-v') as HTMLElement;
    input.oninput = () => {
      const v = parseFloat(input.value);
      vEl.textContent = fmt(v);
      input.style.setProperty('--pct', ((v - p.min) / (p.max - p.min)) * 100 + '%');
      (onChange ?? this.hooks.onParam)(p.key, v);
    };
    wrap.appendChild(input);
    return wrap;
  }

  /* ---- library: search + cards + favourites ---- */

  private renderLibrary(b: HTMLElement): void {
    const s = document.createElement('input');
    s.className = 'search';
    s.placeholder = 'Search worlds, phenomena, tags…';
    b.appendChild(s);

    const grid = document.createElement('div');
    grid.className = 'cards';
    b.appendChild(grid);

    const draw = (q: string) => {
      grid.innerHTML = '';
      const ql = q.toLowerCase().trim();
      const list = WORLDS.filter((w) =>
        !ql || w.name.toLowerCase().includes(ql) || w.desc.toLowerCase().includes(ql) ||
        w.tags.some((t) => t.includes(ql)));
      const sorted = [...list].sort((a, c) =>
        Number(this.favs.has(c.id)) - Number(this.favs.has(a.id)));

      if (!sorted.length) {
        grid.innerHTML = '<div class="note" style="grid-column:1/-1">No matches.</div>';
        return;
      }
      sorted.forEach((w) => {
        const c = document.createElement('button');
        c.className = 'card' + (w.id === this.worldId ? ' on' : '');
        c.innerHTML = `
          <span class="card-g">${w.glyph}</span>
          <div class="card-t">${w.name}</div>
          <div class="card-d">${w.desc}</div>
          <button class="fav ${this.favs.has(w.id) ? 'on' : ''}">★</button>`;
        c.onclick = () => { this.selectWorld(w.id); draw(s.value); };
        const fav = c.querySelector('.fav') as HTMLButtonElement;
        fav.onclick = (e) => {
          e.stopPropagation();
          this.favs.has(w.id) ? this.favs.delete(w.id) : this.favs.add(w.id);
          draw(s.value);
        };
        grid.appendChild(c);
      });
    };
    s.oninput = () => draw(s.value);
    draw('');
  }

  /* ---- telemetry ---- */

  private renderTelemetry(b: HTMLElement): void {
    const g = document.createElement('div');
    g.className = 'grp';
    g.innerHTML = '<div class="grp-h">Performance</div>';
    const canvas = document.createElement('canvas');
    canvas.className = 'graph';
    canvas.width = 260; canvas.height = 52;
    g.appendChild(canvas);
    b.appendChild(g);

    const draw = () => {
      if (!this.wm.IsVisible('telemetry')) return;
      const c = canvas.getContext('2d')!;
      c.clearRect(0, 0, canvas.width, canvas.height);
      const h = this.fpsHist;
      if (h.length > 1) {
        c.strokeStyle = '#4da3ff'; c.lineWidth = 1.6; c.beginPath();
        h.forEach((v, i) => {
          const x = (i / (h.length - 1)) * canvas.width;
          const y = canvas.height - Math.min(v / 120, 1) * canvas.height;
          i ? c.lineTo(x, y) : c.moveTo(x, y);
        });
        c.stroke();
        const grad = c.createLinearGradient(0, 0, 0, canvas.height);
        grad.addColorStop(0, 'rgba(77,163,255,.32)');
        grad.addColorStop(1, 'rgba(77,163,255,0)');
        c.lineTo(canvas.width, canvas.height); c.lineTo(0, canvas.height);
        c.fillStyle = grad; c.fill();
      }
      requestAnimationFrame(draw);
    };
    draw();

    if (this.world) {
      const sg = document.createElement('div');
      sg.className = 'grp';
      sg.innerHTML = '<div class="grp-h">Simulation State</div>';
      const stats = this.world.getStats();
      Object.entries(stats).forEach(([k, v]) => {
        const r = document.createElement('div');
        r.className = 'stat';
        r.innerHTML = `<span class="stat-k">${k}</span><span class="stat-v">${v}</span>`;
        sg.appendChild(r);
      });
      b.appendChild(sg);
    }

    if (this.mode === 'expert') {
      const eg = document.createElement('div');
      eg.className = 'grp';
      eg.innerHTML = '<div class="grp-h">Engine</div>';
      const bk = document.getElementById('hBackend')?.textContent ?? '–';
      [['Backend', bk], ['Library', 'Babylon.js 9.20'], ['Shading', 'Custom GLSL'],
       ['Tonemap', 'ACES filmic']].forEach(([k, v]) => {
        const r = document.createElement('div');
        r.className = 'stat';
        r.innerHTML = `<span class="stat-k">${k}</span><span class="stat-v">${v}</span>`;
        eg.appendChild(r);
      });
      b.appendChild(eg);
    }
  }

  /** Re-renders every open panel (after undo/redo changes world state). */
  refreshAll(): void {
    ['controls', 'telemetry', 'snapshots', 'objects'].forEach((id) => this.wm.refresh(id));
  }

  /* ---- snapshots & history ---- */

  private renderSnapshots(b: HTMLElement): void {
    const n = document.createElement('div');
    n.className = 'note';
    n.textContent = 'Experiments are reversible. Undo any action, or save a state you want to return to.';
    b.appendChild(n);

    const hg = document.createElement('div');
    hg.className = 'grp';
    hg.innerHTML = '<div class="grp-h">History</div>';
    const hrow = document.createElement('div');
    hrow.className = 'btnrow';
    const ub = document.createElement('button');
    ub.className = 'btn';
    ub.textContent = '↶ Undo';
    ub.disabled = !this.hooks.canUndo();
    ub.style.opacity = ub.disabled ? '0.45' : '1';
    ub.onclick = () => { this.hooks.onUndo(); this.refreshAll(); };
    const rb = document.createElement('button');
    rb.className = 'btn';
    rb.textContent = '↷ Redo';
    rb.disabled = !this.hooks.canRedo();
    rb.style.opacity = rb.disabled ? '0.45' : '1';
    rb.onclick = () => { this.hooks.onRedo(); this.refreshAll(); };
    hrow.append(ub, rb);
    hg.appendChild(hrow);
    b.appendChild(hg);

    const sg = document.createElement('div');
    sg.className = 'grp';
    sg.innerHTML = '<div class="grp-h">Saved States</div>';
    const saveRow = document.createElement('div');
    saveRow.className = 'btnrow';
    const sb2 = document.createElement('button');
    sb2.className = 'btn pri';
    sb2.textContent = '📸 Save Current State';
    sb2.onclick = () => {
      this.hooks.onSaveSnapshot('Snapshot ' + new Date().toLocaleTimeString());
      this.wm.refresh('snapshots');
    };
    saveRow.appendChild(sb2);
    sg.appendChild(saveRow);

    const list = this.hooks.listSnapshots();
    if (!list.length) {
      const e = document.createElement('div');
      e.className = 'note';
      e.style.marginTop = '10px';
      e.textContent = 'No saved states yet.';
      sg.appendChild(e);
    } else {
      list.forEach((snap) => {
        const row = document.createElement('div');
        row.className = 'stat';
        row.style.cursor = 'pointer';
        row.innerHTML = `<span class="stat-k">${snap.label}</span>`;
        const load = document.createElement('button');
        load.className = 'btn';
        load.style.cssText = 'min-width:auto;padding:3px 10px;font-size:10.5px';
        load.textContent = 'Load';
        load.onclick = () => { this.hooks.onLoadSnapshot(snap.id); this.refreshAll(); };
        row.appendChild(load);
        sg.appendChild(row);
      });
    }
    b.appendChild(sg);
  }

  /* ---- objects tray ---- */

  private renderObjects(b: HTMLElement): void {
    const n = document.createElement('div');
    n.className = 'note';
    n.innerHTML = `Click any object to launch it at the world. <b>${CATALOG.length}</b> objects available.`;
    b.appendChild(n);

    // scale selector
    const sg = document.createElement('div');
    sg.className = 'grp';
    sg.innerHTML = '<div class="grp-h">Scale</div>';
    const srow = document.createElement('div');
    srow.className = 'btnrow';
    SCALES.forEach((sc) => {
      const btn = document.createElement('button');
      btn.className = 'btn' + (this.objScale === sc.value ? ' pri' : '');
      btn.textContent = sc.label;
      btn.onclick = () => { this.objScale = sc.value; this.wm.refresh('objects'); };
      srow.appendChild(btn);
    });
    sg.appendChild(srow);
    b.appendChild(sg);

    // quick actions
    const qg = document.createElement('div');
    qg.className = 'grp';
    const qrow = document.createElement('div');
    qrow.className = 'btnrow';
    const randBtn = document.createElement('button');
    randBtn.className = 'btn pri';
    randBtn.textContent = '🎲 Random Object';
    randBtn.onclick = () => this.hooks.onSpawn(randomObject().id, this.objScale);
    qrow.appendChild(randBtn);
    qg.appendChild(qrow);
    b.appendChild(qg);

    // search + category filter
    const search = document.createElement('input');
    search.className = 'search';
    search.placeholder = `Search ${CATALOG.length} objects…`;
    b.appendChild(search);

    const tabs = document.createElement('div');
    tabs.className = 'tabs';
    tabs.style.flexWrap = 'wrap';
    b.appendChild(tabs);

    const grid = document.createElement('div');
    grid.className = 'cards';
    b.appendChild(grid);

    const drawTabs = () => {
      tabs.innerHTML = '';
      ['All', ...CATEGORIES].forEach((cat) => {
        const t = document.createElement('button');
        t.className = 'tab' + (this.objCat === cat ? ' on' : '');
        t.textContent = cat;
        t.onclick = () => { this.objCat = cat; drawTabs(); draw(search.value); };
        tabs.appendChild(t);
      });
    };

    const draw = (q: string) => {
      grid.innerHTML = '';
      const ql = q.toLowerCase().trim();
      const list = CATALOG.filter((o) => {
        const catOk = this.objCat === 'All' || o.category === this.objCat;
        const qOk = !ql || o.name.toLowerCase().includes(ql) ||
                    o.category.toLowerCase().includes(ql) ||
                    o.material.toLowerCase().includes(ql);
        return catOk && qOk;
      });
      if (!list.length) {
        grid.innerHTML = '<div class="note" style="grid-column:1/-1">No objects match.</div>';
        return;
      }
      list.forEach((o) => {
        const c = document.createElement('button');
        c.className = 'card';
        c.title = `${o.name} · ${o.material} · mass ${o.mass}`;
        c.innerHTML = `
          <span class="card-g">${o.glyph}</span>
          <div class="card-t">${o.name}</div>
          <div class="card-d">${o.note ?? o.material}</div>`;
        c.onclick = () => this.hooks.onSpawn(o.id, this.objScale);
        grid.appendChild(c);
      });
    };
    search.oninput = () => draw(search.value);
    drawTabs();
    draw('');
  }

  /* ---- graphics ---- */

  private renderGraphics(b: HTMLElement): void {
    const n = document.createElement('div');
    n.className = 'note';
    n.textContent = 'Post-processing applies to every world. Lower these if the framerate drops.';
    b.appendChild(n);

    const g = document.createElement('div');
    g.className = 'grp';
    g.innerHTML = '<div class="grp-h">Image</div>';
    const shown = this.mode === 'simple'
      ? POSTFX_PARAMS.filter((p) => ['bloom', 'exposure', 'vignette'].includes(p.key))
      : this.mode === 'advanced'
        ? POSTFX_PARAMS.filter((p) => p.key !== 'bloomThreshold' && p.key !== 'chromatic')
        : POSTFX_PARAMS;

    shown.forEach((p) => {
      const cur = { ...p, value: this.postfx[p.key] ?? p.value };
      g.appendChild(this.slider(cur, (k, v) => {
        this.postfx[k] = v;
        this.hooks.onPostFX(k, v);
      }));
    });
    b.appendChild(g);

    const pg = document.createElement('div');
    pg.className = 'grp';
    pg.innerHTML = '<div class="grp-h">Looks</div>';
    const row = document.createElement('div');
    row.className = 'btnrow';
    const looks: [string, Partial<Record<string, number>>][] = [
      ['Clean', { bloom: 0.2, grain: 0, chromatic: 0, vignette: 0.15, contrast: 1.0 }],
      ['Cinematic', { bloom: 0.75, grain: 4, chromatic: 3, vignette: 0.5, contrast: 1.12 }],
      ['Telescope', { bloom: 1.3, grain: 9, chromatic: 6, vignette: 0.8, contrast: 1.2 }],
      ['Flat', { bloom: 0, grain: 0, chromatic: 0, vignette: 0, contrast: 1.0, exposure: 1.0 }]
    ];
    looks.forEach(([label, vals]) => {
      const btn = document.createElement('button');
      btn.className = 'btn';
      btn.textContent = label;
      btn.onclick = () => {
        Object.entries(vals).forEach(([k, v]) => {
          this.postfx[k] = v as number;
          this.hooks.onPostFX(k, v as number);
        });
        this.wm.refresh('graphics');
      };
      row.appendChild(btn);
    });
    pg.appendChild(row);
    b.appendChild(pg);
  }

  /* ---- presets ---- */

  private renderPresets(b: HTMLElement): void {
    const n = document.createElement('div');
    n.className = 'note';
    n.textContent = 'One-click states. Create, modify, experiment, observe — then reset and try something else.';
    b.appendChild(n);

    const g = document.createElement('div');
    g.className = 'grp';
    g.innerHTML = '<div class="grp-h">Quick States</div>';
    const row = document.createElement('div');
    row.className = 'btnrow';

    const presets: Record<string, [string, Record<string, number>][]> = {
      ocean: [
        ['🪞 Glass', { waveScale: 0.08, choppy: 0.2, windSpeed: 0.25, foam: 0.05, ssr: 1 }],
        ['🌊 Swell', { waveScale: 1.1, choppy: 0.9, windSpeed: 1.0, foam: 0.8 }],
        ['🌩 Tempest', { waveScale: 2.6, choppy: 1.8, windSpeed: 2.6, foam: 1.9 }],
        ['🔬 Max Quality', { ssr: 1, ssrSteps: 64, roughness: 0.5, sss: 1.6 }]
      ],
      blackhole: [
        ['🕳 Stellar Mass', { mass: 0.6, diskInner: 2.6, diskOuter: 12, diskBright: 1.1 }],
        ['🌀 Supermassive', { mass: 2.8, diskInner: 6, diskOuter: 30, diskBright: 1.6 }],
        ['📐 Edge-On', { diskTilt: 0.02, doppler: 1.8, exposure: 1.3 }],
        ['🔭 Max Lensing', { lens: 2.0, mass: 2.0, exposure: 1.4 }]
      ],
      sandbox: [
        ['🪐 Solar System', { gravity: 1, timeScale: 1, spawnMass: 1 }],
        ['💫 Heavy Gravity', { gravity: 2.6, timeScale: 1.2 }],
        ['🐌 Slow Motion', { timeScale: 0.15, trails: 1, trailLength: 300 }],
        ['🌠 Long Trails', { trails: 1, trailLength: 380 }]
      ],
      planetary: [
        ['🌍 Realtime', { timeScale: 1, orbitSpeed: 1, detail: 1 }],
        ['⏩ Fast Orbits', { timeScale: 3.5, orbitSpeed: 3 }],
        ['🔍 Ultra Detail', { detail: 2.5, clouds: 1.2, lights: 2 }],
        ['🌑 Night Side', { lights: 3, clouds: 0.4, exposure: 1.5 }]
      ]
    };

    (presets[this.worldId] ?? []).forEach(([label, vals]) => {
      const btn = document.createElement('button');
      btn.className = 'btn';
      btn.textContent = label;
      btn.onclick = () => {
        Object.entries(vals).forEach(([k, v]) => this.hooks.onParam(k, v));
        this.wm.refresh('controls');
      };
      row.appendChild(btn);
    });
    g.appendChild(row);
    b.appendChild(g);

    const lg = document.createElement('div');
    lg.className = 'grp';
    lg.innerHTML = '<div class="grp-h">Layout</div>';
    const lr = document.createElement('div');
    lr.className = 'btnrow';
    [['Reset Panels', () => this.wm.Reset()],
     ['Hide All', () => this.wm.CloseAll()],
     ['Shortcuts', () => this.wm.Open('help')]].forEach(([l, fn]) => {
      const btn = document.createElement('button');
      btn.className = 'btn';
      btn.textContent = l as string;
      btn.onclick = fn as () => void;
      lr.appendChild(btn);
    });
    lg.appendChild(lr);
    b.appendChild(lg);
  }
}
