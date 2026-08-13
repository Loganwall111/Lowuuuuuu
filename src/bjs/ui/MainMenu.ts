/**
 * MainMenu — the AAA front-end shown before the sandbox starts.
 *
 * It renders over a live animated starfield canvas (its own lightweight 2D
 * canvas, independent of Babylon) so the menu is never a flat black screen
 * even if the 3D engine is still warming up.
 */

export interface MenuChoice {
  world: string;
  preset?: string;
}

interface MenuEntry {
  id: string;
  title: string;
  sub: string;
  glyph: string;
  accent: string;
}

const ENTRIES: MenuEntry[] = [
  { id: 'sandbox',   title: 'Gravity Sandbox', sub: 'Build systems · throw anything · smash worlds', glyph: '🌌', accent: '#4da3ff' },
  { id: 'planetary', title: 'Star Systems',    sub: 'Procedural planets, moons, rings & atmospheres', glyph: '🪐', accent: '#7c5cff' },
  { id: 'ocean',     title: 'Ocean Worlds',    sub: 'Fluid simulation with ray-traced reflections',   glyph: '🌊', accent: '#31d68a' },
  { id: 'terraform', title: 'Terraform',       sub: 'Carve rivers, flood valleys, erode mountains',   glyph: '⛰', accent: '#8ad14f' },
  { id: 'blackhole', title: 'Singularity',     sub: 'Ray-marched geodesics & gravitational lensing',  glyph: '⚫', accent: '#ffb545' },
  { id: 'dimension', title: 'Beyond the Horizon', sub: 'Infinite dimensions past the event horizon',  glyph: '🌀', accent: '#ff5cc8' }
];

export const MENU_CSS = `
.menu-root{position:fixed;inset:0;z-index:150;display:flex;flex-direction:column;
  background:radial-gradient(ellipse at 50% 0%,#0a1024 0%,#05070d 55%,#020306 100%);
  opacity:1;transition:opacity .5s ease;overflow:hidden}
.menu-root.closing{opacity:0;pointer-events:none}
#menuStars{position:absolute;inset:0;width:100%;height:100%;display:block}
.menu-inner{position:relative;z-index:2;display:flex;flex-direction:column;height:100%;
  padding:0 6vw;justify-content:center}
.menu-title{font-size:clamp(46px,9vw,104px);font-weight:800;letter-spacing:-2px;line-height:.95;
  margin:0 0 6px;background:linear-gradient(120deg,#fff 10%,#8fc4ff 45%,#b39bff 80%);
  -webkit-background-clip:text;background-clip:text;-webkit-text-fill-color:transparent}
.menu-sub{font-size:clamp(11px,1.5vw,15px);color:#7f8aa3;letter-spacing:5px;text-transform:uppercase;
  margin-bottom:38px;font-weight:600}
.menu-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(248px,1fr));gap:14px;max-width:1080px}
.menu-card{position:relative;text-align:left;padding:20px 20px 18px;border-radius:16px;cursor:pointer;
  background:rgba(255,255,255,.038);border:1px solid rgba(255,255,255,.10);color:#e8edf7;
  font-family:inherit;overflow:hidden;transition:transform .16s cubic-bezier(.2,.8,.3,1),
  border-color .16s,background .16s;backdrop-filter:blur(12px)}
.menu-card:hover{transform:translateY(-4px);background:rgba(255,255,255,.085)}
.menu-card::after{content:'';position:absolute;left:0;right:0;bottom:0;height:2.5px;
  background:var(--ac);transform:scaleX(0);transform-origin:left;transition:transform .22s ease}
.menu-card:hover::after{transform:scaleX(1)}
.menu-card-g{font-size:30px;display:block;margin-bottom:12px;line-height:1}
.menu-card-t{font-size:16.5px;font-weight:700;margin-bottom:5px;letter-spacing:.2px}
.menu-card-s{font-size:11.5px;color:#8b95ad;line-height:1.5}
.menu-actions{display:flex;gap:12px;margin-top:34px;flex-wrap:wrap;align-items:center}
.menu-btn{padding:13px 26px;border-radius:12px;border:1px solid rgba(255,255,255,.14);
  background:rgba(255,255,255,.05);color:#e8edf7;font-size:13px;font-weight:600;cursor:pointer;
  font-family:inherit;transition:all .15s}
.menu-btn:hover{background:rgba(255,255,255,.13);transform:translateY(-1px)}
.menu-btn.primary{background:linear-gradient(135deg,#4da3ff,#7c5cff);border-color:transparent;
  box-shadow:0 8px 26px rgba(77,163,255,.34);padding:14px 34px;font-size:14px}
.menu-btn.chaos{border-color:rgba(255,181,69,.45);color:#ffcf85}
.menu-btn.chaos:hover{background:rgba(255,181,69,.16)}
.menu-foot{position:absolute;bottom:22px;left:6vw;right:6vw;display:flex;justify-content:space-between;
  align-items:center;font-size:10.5px;color:#4d566b;letter-spacing:1.4px;text-transform:uppercase;z-index:2}
.menu-badge{padding:3px 9px;border-radius:6px;background:rgba(124,92,255,.16);color:#b9a8ff;
  letter-spacing:1px;font-weight:700}
@media (max-height:620px){
  .menu-sub{margin-bottom:20px}
  .menu-actions{margin-top:20px}
  .menu-card{padding:14px}
}
`;

export class MainMenu {
  private root: HTMLDivElement;
  private raf = 0;
  private onPick: (c: MenuChoice) => void;

  constructor(onPick: (c: MenuChoice) => void) {
    this.onPick = onPick;

    const style = document.createElement('style');
    style.textContent = MENU_CSS;
    document.head.appendChild(style);

    this.root = document.createElement('div');
    this.root.className = 'menu-root';
    this.root.innerHTML = `
      <canvas id="menuStars"></canvas>
      <div class="menu-inner">
        <h1 class="menu-title">UNLIMITED<br/>POSSIBILITIES</h1>
        <div class="menu-sub">Sandbox</div>
        <div class="menu-grid" id="menuGrid"></div>
        <div class="menu-actions">
          <button class="menu-btn primary" id="mQuick">▶  Quick Start</button>
          <button class="menu-btn chaos" id="mChaos">🎲  Random Experiment</button>
          <button class="menu-btn" id="mWeird">✨  Make It Weird</button>
        </div>
      </div>
      <div class="menu-foot">
        <span>Babylon.js 9 · WebGL2</span>
        <span class="menu-badge">Create · Throw · Break · Observe</span>
      </div>`;
    document.body.appendChild(this.root);

    const grid = this.root.querySelector('#menuGrid') as HTMLElement;
    ENTRIES.forEach((e) => {
      const c = document.createElement('button');
      c.className = 'menu-card';
      c.style.setProperty('--ac', e.accent);
      c.innerHTML = `
        <span class="menu-card-g">${e.glyph}</span>
        <div class="menu-card-t">${e.title}</div>
        <div class="menu-card-s">${e.sub}</div>`;
      c.onclick = () => this.pick({ world: e.id });
      grid.appendChild(c);
    });

    (this.root.querySelector('#mQuick') as HTMLButtonElement).onclick =
      () => this.pick({ world: 'sandbox' });
    (this.root.querySelector('#mChaos') as HTMLButtonElement).onclick =
      () => this.pick({ world: 'sandbox', preset: 'chaos' });
    (this.root.querySelector('#mWeird') as HTMLButtonElement).onclick =
      () => this.pick({ world: 'sandbox', preset: 'weird' });

    this.startStars();
  }

  /** Lightweight 2D starfield so the menu is alive without touching Babylon. */
  private startStars(): void {
    const canvas = this.root.querySelector('#menuStars') as HTMLCanvasElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let w = 0, h = 0;
    const stars: { x: number; y: number; z: number; s: number }[] = [];
    const resize = () => {
      w = canvas.width = this.root.clientWidth || window.innerWidth;
      h = canvas.height = this.root.clientHeight || window.innerHeight;
    };
    resize();
    window.addEventListener('resize', resize);
    for (let i = 0; i < 420; i++) {
      stars.push({ x: Math.random(), y: Math.random(), z: Math.random(), s: Math.random() * 1.6 + 0.3 });
    }

    let t = 0;
    const draw = () => {
      t += 0.0016;
      ctx.clearRect(0, 0, w, h);
      for (const st of stars) {
        const drift = (st.x + t * (0.2 + st.z * 0.5)) % 1;
        const x = drift * w;
        const y = st.y * h;
        const tw = 0.55 + Math.sin(t * 40 + st.x * 60) * 0.45;
        ctx.globalAlpha = (0.25 + st.z * 0.75) * tw;
        ctx.fillStyle = st.z > 0.85 ? '#bcd4ff' : '#ffffff';
        ctx.beginPath();
        ctx.arc(x, y, st.s * (0.5 + st.z), 0, 6.283);
        ctx.fill();
      }
      ctx.globalAlpha = 1;
      this.raf = requestAnimationFrame(draw);
    };
    draw();
  }

  private pick(c: MenuChoice): void {
    this.close();
    this.onPick(c);
  }

  /** Fully removes the menu; it can never linger and block the sandbox. */
  close(): void {
    cancelAnimationFrame(this.raf);
    this.root.classList.add('closing');
    this.root.style.pointerEvents = 'none';
    const el = this.root;
    setTimeout(() => el.remove(), 550);
  }

  isOpen(): boolean {
    return this.root.isConnected && !this.root.classList.contains('closing');
  }
}
