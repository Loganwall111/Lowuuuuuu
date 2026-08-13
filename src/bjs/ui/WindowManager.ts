/**
 * WindowManager — the single authority over every panel, widget, editor,
 * menu, overlay and dialog in the application.
 *
 * Design rules enforced here (these are the P0 defect fixes):
 *  - No window is ever modal. The simulation is always visible and always
 *    receives input outside window chrome.
 *  - Windows are pointer-events:none at the container level, so the canvas
 *    beneath is never blocked by an invisible full-screen layer.
 *  - Close() fully removes the element from the layout AND clears state, so
 *    nothing lingers after a visual close.
 *  - Every window is draggable, minimizable, maximizable and resettable.
 *  - Escape closes the top-most window; it can never trap the user.
 */

export type WindowId = string;

export interface WindowSpec {
  id: WindowId;
  title: string;
  glyph?: string;
  /** Initial position as a fraction of viewport, 0..1 */
  x?: number;
  y?: number;
  width?: number;
  height?: number;
  /** Windows marked transient close when another opens in the same slot. */
  group?: string;
  resizable?: boolean;
  open?: boolean;
  render: (body: HTMLElement) => void;
}

interface WindowState {
  spec: WindowSpec;
  el: HTMLDivElement;
  body: HTMLDivElement;
  open: boolean;
  minimized: boolean;
  maximized: boolean;
  z: number;
  home: { x: number; y: number; w: number; h: number };
  last: { x: number; y: number; w: number; h: number };
}

export class WindowManager {
  private layer: HTMLDivElement;
  private dock: HTMLDivElement;
  private windows = new Map<WindowId, WindowState>();
  private zCounter = 100;
  private listeners: Array<() => void> = [];

  constructor(root: HTMLElement = document.body) {
    this.layer = document.createElement('div');
    this.layer.className = 'wm-layer';
    root.appendChild(this.layer);

    this.dock = document.createElement('div');
    this.dock.className = 'wm-dock';
    root.appendChild(this.dock);

    // Escape closes the top-most open window — input is never trapped.
    window.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        const top = this.topMost();
        if (top) { this.Close(top); e.preventDefault(); }
      }
    });

    window.addEventListener('resize', () => this.clampAll());
  }

  /* ------------------------------- registry ------------------------------- */

  register(spec: WindowSpec): void {
    if (this.windows.has(spec.id)) this.destroy(spec.id);

    const el = document.createElement('div');
    el.className = 'wm-win';
    el.dataset.wid = spec.id;

    const w = spec.width ?? 340;
    const h = spec.height ?? 420;
    const x = spec.x !== undefined ? spec.x * (window.innerWidth - w) : 24;
    const y = spec.y !== undefined ? spec.y * (window.innerHeight - h) : 84;

    el.style.width = w + 'px';
    if (spec.height) el.style.height = h + 'px';
    el.style.left = Math.max(8, x) + 'px';
    el.style.top = Math.max(8, y) + 'px';

    el.innerHTML = `
      <header class="wm-bar">
        <span class="wm-grip"></span>
        <span class="wm-title">${spec.glyph ? spec.glyph + ' ' : ''}${spec.title}</span>
        <div class="wm-btns">
          <button class="wm-b" data-act="min"   title="Minimize" aria-label="Minimize">–</button>
          <button class="wm-b" data-act="max"   title="Maximize" aria-label="Maximize">□</button>
          <button class="wm-b wm-x" data-act="close" title="Close" aria-label="Close">×</button>
        </div>
      </header>
      <div class="wm-body"></div>
      ${spec.resizable !== false ? '<div class="wm-resize"></div>' : ''}
    `;

    const body = el.querySelector('.wm-body') as HTMLDivElement;

    const st: WindowState = {
      spec, el, body,
      open: false, minimized: false, maximized: false,
      z: 0,
      home: { x: Math.max(8, x), y: Math.max(8, y), w, h },
      last: { x: Math.max(8, x), y: Math.max(8, y), w, h }
    };
    this.windows.set(spec.id, st);

    // --- title-bar buttons. Bound with capture so nothing can swallow them. ---
    el.querySelectorAll<HTMLButtonElement>('.wm-b').forEach((btn) => {
      const act = btn.dataset.act!;
      const handler = (ev: Event) => {
        ev.preventDefault();
        ev.stopPropagation();
        if (act === 'close') this.Close(spec.id);
        else if (act === 'min') this.Minimize(spec.id);
        else if (act === 'max') this.Maximize(spec.id);
      };
      btn.addEventListener('pointerdown', (e) => e.stopPropagation());
      btn.addEventListener('click', handler);
    });

    el.addEventListener('pointerdown', () => this.BringToFront(spec.id));
    this.makeDraggable(st);
    if (spec.resizable !== false) this.makeResizable(st);

    this.layer.appendChild(el);
    el.style.display = 'none';

    if (spec.open) this.Open(spec.id);
  }

  /* -------------------------------- API -------------------------------- */

  Open(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st) return;
    if (st.spec.group) {
      for (const [oid, o] of this.windows) {
        if (oid !== id && o.spec.group === st.spec.group && o.open) this.Close(oid);
      }
    }
    st.open = true;
    st.minimized = false;
    st.el.style.display = 'flex';
    st.el.classList.remove('wm-minimized');
    st.body.innerHTML = '';
    st.spec.render(st.body);
    this.BringToFront(id);
    this.clamp(st);
    this.syncDock();
    this.emit();
  }

  Close(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st) return;
    st.open = false;
    st.minimized = false;
    st.maximized = false;
    st.el.classList.remove('wm-max', 'wm-minimized');
    st.el.style.display = 'none';
    // Fully clear content so no overlay or handler can linger after close.
    st.body.innerHTML = '';
    this.syncDock();
    this.emit();
  }

  Toggle(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st) return;
    if (st.open && !st.minimized) this.Close(id);
    else this.Open(id);
  }

  Minimize(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st || !st.open) return;
    st.minimized = true;
    st.el.style.display = 'none';
    this.syncDock();
    this.emit();
  }

  Maximize(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st) return;
    if (!st.open) this.Open(id);
    if (st.maximized) {
      st.maximized = false;
      st.el.classList.remove('wm-max');
      st.el.style.left = st.last.x + 'px';
      st.el.style.top = st.last.y + 'px';
      st.el.style.width = st.last.w + 'px';
      st.el.style.height = st.last.h + 'px';
    } else {
      st.last = {
        x: st.el.offsetLeft, y: st.el.offsetTop,
        w: st.el.offsetWidth, h: st.el.offsetHeight
      };
      st.maximized = true;
      st.el.classList.add('wm-max');
      // Maximize still leaves the simulation visible: 46% width, never full-screen.
      st.el.style.left = '12px';
      st.el.style.top = '76px';
      st.el.style.width = Math.min(560, window.innerWidth * 0.46) + 'px';
      st.el.style.height = (window.innerHeight - 100) + 'px';
    }
    this.emit();
  }

  BringToFront(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st) return;
    st.z = ++this.zCounter;
    st.el.style.zIndex = String(st.z);
  }

  SendToBack(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st) return;
    let min = this.zCounter;
    for (const o of this.windows.values()) min = Math.min(min, o.z || this.zCounter);
    st.z = min - 1;
    st.el.style.zIndex = String(st.z);
  }

  Reset(id?: WindowId): void {
    const doReset = (st: WindowState) => {
      st.el.classList.remove('wm-max', 'wm-minimized');
      st.maximized = false;
      st.minimized = false;
      st.el.style.left = st.home.x + 'px';
      st.el.style.top = st.home.y + 'px';
      st.el.style.width = st.home.w + 'px';
      if (st.spec.height) st.el.style.height = st.home.h + 'px';
      else st.el.style.height = '';
      st.last = { ...st.home };
    };
    if (id) {
      const st = this.windows.get(id);
      if (st) doReset(st);
    } else {
      for (const st of this.windows.values()) { doReset(st); this.Close(st.spec.id); }
    }
    this.syncDock();
    this.emit();
  }

  IsOpen(id: WindowId): boolean {
    return !!this.windows.get(id)?.open;
  }

  IsVisible(id: WindowId): boolean {
    const st = this.windows.get(id);
    return !!st && st.open && !st.minimized;
  }

  /* ------------------------------ extras ------------------------------ */

  CloseAll(): void {
    for (const id of this.windows.keys()) this.Close(id);
  }

  /** Re-run the render callback of an open window (live data refresh). */
  refresh(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st || !st.open || st.minimized) return;
    st.body.innerHTML = '';
    st.spec.render(st.body);
  }

  destroy(id: WindowId): void {
    const st = this.windows.get(id);
    if (!st) return;
    st.el.remove();
    this.windows.delete(id);
    this.syncDock();
  }

  list(): { id: string; title: string; glyph?: string; open: boolean; minimized: boolean }[] {
    return [...this.windows.values()].map((s) => ({
      id: s.spec.id, title: s.spec.title, glyph: s.spec.glyph,
      open: s.open, minimized: s.minimized
    }));
  }

  onChange(cb: () => void): void { this.listeners.push(cb); }

  private emit(): void { this.listeners.forEach((l) => l()); }

  private topMost(): WindowId | null {
    let best: WindowState | null = null;
    for (const st of this.windows.values()) {
      if (st.open && !st.minimized && (!best || st.z > best.z)) best = st;
    }
    return best ? best.spec.id : null;
  }

  /* ------------------------------ behaviour ------------------------------ */

  private makeDraggable(st: WindowState): void {
    const bar = st.el.querySelector('.wm-bar') as HTMLElement;
    let sx = 0, sy = 0, ox = 0, oy = 0, dragging = false;

    bar.addEventListener('pointerdown', (e: PointerEvent) => {
      if ((e.target as HTMLElement).closest('.wm-b')) return;
      dragging = true;
      sx = e.clientX; sy = e.clientY;
      ox = st.el.offsetLeft; oy = st.el.offsetTop;
      bar.setPointerCapture(e.pointerId);
      st.el.classList.add('wm-dragging');
    });
    bar.addEventListener('pointermove', (e: PointerEvent) => {
      if (!dragging) return;
      st.el.style.left = (ox + e.clientX - sx) + 'px';
      st.el.style.top = (oy + e.clientY - sy) + 'px';
    });
    const end = (e: PointerEvent) => {
      if (!dragging) return;
      dragging = false;
      try { bar.releasePointerCapture(e.pointerId); } catch {}
      st.el.classList.remove('wm-dragging');
      this.clamp(st);
    };
    bar.addEventListener('pointerup', end);
    bar.addEventListener('pointercancel', end);
    bar.addEventListener('dblclick', () => this.Maximize(st.spec.id));
  }

  private makeResizable(st: WindowState): void {
    const grip = st.el.querySelector('.wm-resize') as HTMLElement;
    if (!grip) return;
    let sx = 0, sy = 0, ow = 0, oh = 0, active = false;
    grip.addEventListener('pointerdown', (e: PointerEvent) => {
      active = true;
      sx = e.clientX; sy = e.clientY;
      ow = st.el.offsetWidth; oh = st.el.offsetHeight;
      grip.setPointerCapture(e.pointerId);
      e.stopPropagation();
    });
    grip.addEventListener('pointermove', (e: PointerEvent) => {
      if (!active) return;
      st.el.style.width = Math.max(240, ow + e.clientX - sx) + 'px';
      st.el.style.height = Math.max(160, oh + e.clientY - sy) + 'px';
    });
    const end = (e: PointerEvent) => {
      if (!active) return;
      active = false;
      try { grip.releasePointerCapture(e.pointerId); } catch {}
    };
    grip.addEventListener('pointerup', end);
    grip.addEventListener('pointercancel', end);
  }

  private clamp(st: WindowState): void {
    const w = st.el.offsetWidth, h = st.el.offsetHeight;
    let x = st.el.offsetLeft, y = st.el.offsetTop;
    x = Math.min(Math.max(-w + 90, x), window.innerWidth - 90);
    y = Math.min(Math.max(0, y), window.innerHeight - 44);
    st.el.style.left = x + 'px';
    st.el.style.top = y + 'px';
  }

  private clampAll(): void {
    for (const st of this.windows.values()) if (st.open) this.clamp(st);
  }

  /** Minimized windows live in a slim dock so they can always be restored. */
  private syncDock(): void {
    this.dock.innerHTML = '';
    const mins = [...this.windows.values()].filter((s) => s.open && s.minimized);
    this.dock.style.display = mins.length ? 'flex' : 'none';
    for (const st of mins) {
      const b = document.createElement('button');
      b.className = 'wm-dockbtn';
      b.textContent = `${st.spec.glyph ?? ''} ${st.spec.title}`;
      b.onclick = () => this.Open(st.spec.id);
      this.dock.appendChild(b);
    }
  }
}
