/**
 * MainMenu — full-screen AAA-style front end.
 * Built entirely in the DOM so it composites over the WebGL canvas with no
 * render cost, and injects its own stylesheet so index.html stays untouched.
 */

export type WorldId = 'planetary' | 'stellar' | 'fluid';

interface MenuEntry {
  id: WorldId;
  title: string;
  tag: string;
  blurb: string;
  glyph: string;
}

const ENTRIES: MenuEntry[] = [
  {
    id: 'planetary',
    title: 'Planetary Scale',
    tag: 'ORBITAL MECHANICS',
    blurb: 'A living world with atmosphere, a tidally locked moon and 1,500 instanced asteroids.',
    glyph: '🌍'
  },
  {
    id: 'stellar',
    title: 'Stellar Scale',
    tag: 'RELATIVISTIC',
    blurb: 'Supermassive singularity with a doppler-beamed accretion disk and photon halo.',
    glyph: '🕳'
  },
  {
    id: 'fluid',
    title: 'Fluid Physics Lab',
    tag: 'GPU HYDRODYNAMICS',
    blurb: 'Gerstner ocean with real-time buoyancy — props ride the true wave surface.',
    glyph: '🌊'
  }
];

const CSS = `
:root{
  --accent:#5fd0ff; --accent-dim:#2b7ea6; --ink:#eaf4ff;
}
#ups-menu{
  position:fixed; inset:0; z-index:9000;
  display:flex; flex-direction:column; align-items:center; justify-content:center;
  font-family:'Segoe UI',system-ui,-apple-system,sans-serif;
  color:var(--ink);
  background:
    radial-gradient(ellipse 120% 80% at 50% -10%, rgba(40,110,170,.35), transparent 60%),
    radial-gradient(ellipse 90% 70% at 50% 110%, rgba(120,40,160,.28), transparent 60%),
    linear-gradient(180deg,#01040a 0%,#040a16 55%,#01040a 100%);
  opacity:1; transition:opacity .55s ease, transform .55s ease;
}
#ups-menu.ups-hidden{ opacity:0; pointer-events:none; transform:scale(1.04); }

/* drifting starfield */
#ups-menu .ups-stars{ position:absolute; inset:0; overflow:hidden; pointer-events:none; }
#ups-menu .ups-stars i{
  position:absolute; width:2px; height:2px; border-radius:50%;
  background:#cfe9ff; opacity:.65;
  animation:ups-drift linear infinite;
}
@keyframes ups-drift{
  from{ transform:translateY(0) } to{ transform:translateY(-120vh) }
}
/* sweeping horizon glow */
#ups-menu .ups-sweep{
  position:absolute; left:50%; top:52%; width:180vw; height:180vw;
  transform:translate(-50%,-50%);
  background:conic-gradient(from 0deg, transparent 0deg, rgba(95,208,255,.07) 22deg, transparent 55deg);
  animation:ups-spin 26s linear infinite; pointer-events:none;
}
@keyframes ups-spin{ to{ transform:translate(-50%,-50%) rotate(360deg) } }

#ups-menu .ups-inner{ position:relative; z-index:2; text-align:center; padding:0 24px; }

.ups-eyebrow{
  font-size:11px; letter-spacing:.62em; text-transform:uppercase;
  color:var(--accent); opacity:.85; margin-bottom:18px; padding-left:.62em;
  animation:ups-fade .8s ease both;
}
.ups-title{
  margin:0; font-weight:200; line-height:.92;
  font-size:clamp(42px,9vw,104px); letter-spacing:.02em;
  text-shadow:0 0 60px rgba(95,208,255,.45);
  animation:ups-fade .9s .1s ease both;
}
.ups-title b{ font-weight:700; display:block;
  background:linear-gradient(92deg,#7fe0ff,#c9a6ff 55%,#ffd9a0);
  -webkit-background-clip:text; background-clip:text; color:transparent;
}
.ups-rule{
  width:min(560px,80vw); height:1px; margin:26px auto 8px;
  background:linear-gradient(90deg,transparent,rgba(95,208,255,.7),transparent);
  animation:ups-fade .9s .2s ease both;
}
.ups-sub{
  font-size:13px; opacity:.55; letter-spacing:.16em; text-transform:uppercase;
  animation:ups-fade .9s .25s ease both;
}

.ups-cards{
  display:flex; gap:18px; margin:42px auto 0; flex-wrap:wrap; justify-content:center;
  animation:ups-fade 1s .35s ease both;
}
.ups-card{
  width:250px; text-align:left; cursor:pointer;
  background:linear-gradient(180deg,rgba(12,22,40,.86),rgba(6,12,24,.72));
  border:1px solid rgba(120,190,255,.16); border-radius:14px;
  padding:20px 20px 18px; backdrop-filter:blur(14px);
  transition:transform .22s cubic-bezier(.2,.8,.3,1), border-color .22s, box-shadow .22s, background .22s;
}
.ups-card:hover{
  transform:translateY(-7px);
  border-color:rgba(95,208,255,.62);
  box-shadow:0 18px 48px rgba(0,0,0,.65), 0 0 0 1px rgba(95,208,255,.18) inset,
             0 0 42px rgba(95,208,255,.14);
  background:linear-gradient(180deg,rgba(18,34,58,.92),rgba(8,16,30,.8));
}
.ups-card:focus-visible{ outline:2px solid var(--accent); outline-offset:3px; }
.ups-glyph{ font-size:30px; line-height:1; margin-bottom:12px; filter:saturate(1.2); }
.ups-tag{
  font-size:9px; letter-spacing:.24em; color:var(--accent);
  opacity:.9; text-transform:uppercase; margin-bottom:7px;
}
.ups-name{ font-size:17px; font-weight:600; margin-bottom:8px; }
.ups-blurb{ font-size:11.5px; line-height:1.55; opacity:.52; }
.ups-go{
  margin-top:14px; font-size:11px; letter-spacing:.14em; color:var(--accent);
  opacity:0; transform:translateX(-6px); transition:opacity .22s, transform .22s;
}
.ups-card:hover .ups-go{ opacity:1; transform:translateX(0); }

.ups-foot{
  position:absolute; bottom:26px; left:0; right:0; z-index:2;
  display:flex; justify-content:space-between; padding:0 30px;
  font-size:10.5px; letter-spacing:.13em; opacity:.35; text-transform:uppercase;
}
.ups-hint{ margin-top:30px; font-size:11px; opacity:.42; letter-spacing:.1em;
  animation:ups-fade 1s .5s ease both; }
@keyframes ups-fade{ from{opacity:0; transform:translateY(14px)} to{opacity:1; transform:none} }

/* ---- in-world HUD additions ---- */
#ups-hud-extra{
  position:fixed; z-index:8000; right:16px; top:16px;
  display:flex; flex-direction:column; gap:8px; align-items:flex-end;
  font-family:'Segoe UI',system-ui,sans-serif; pointer-events:none;
}
#ups-hud-extra.ups-hidden{ display:none; }
.ups-stat{
  background:rgba(6,12,24,.72); border:1px solid rgba(120,190,255,.18);
  border-radius:9px; padding:8px 13px; backdrop-filter:blur(12px);
  font-size:11px; letter-spacing:.07em; color:#cfe6ff; min-width:132px;
  display:flex; justify-content:space-between; gap:14px;
}
.ups-stat b{ color:var(--accent); font-weight:600; }
#ups-menu-btn{
  pointer-events:auto; cursor:pointer;
  background:rgba(6,12,24,.8); border:1px solid rgba(120,190,255,.3);
  color:#cfe6ff; border-radius:9px; padding:9px 15px; font-size:11px;
  letter-spacing:.14em; text-transform:uppercase; transition:all .18s;
}
#ups-menu-btn:hover{ background:rgba(28,60,100,.9); border-color:var(--accent); color:#fff; }
`;

export class MainMenu {
  private root: HTMLDivElement;
  private hud: HTMLDivElement;
  private onSelect: (id: WorldId) => void = () => {};
  private fpsEl!: HTMLElement;
  private bodyEl!: HTMLElement;
  private worldEl!: HTMLElement;

  constructor() {
    const style = document.createElement('style');
    style.textContent = CSS;
    document.head.appendChild(style);

    this.root = document.createElement('div');
    this.root.id = 'ups-menu';
    this.root.innerHTML = this.markup();
    document.body.appendChild(this.root);

    this.hud = document.createElement('div');
    this.hud.id = 'ups-hud-extra';
    this.hud.className = 'ups-hidden';
    this.hud.innerHTML = `
      <div class="ups-stat"><span>WORLD</span><b id="ups-world">—</b></div>
      <div class="ups-stat"><span>BODIES</span><b id="ups-bodies">0</b></div>
      <div class="ups-stat"><span>FPS</span><b id="ups-fps">60</b></div>
      <button id="ups-menu-btn">☰ Menu</button>`;
    document.body.appendChild(this.hud);

    this.fpsEl = this.hud.querySelector('#ups-fps')!;
    this.bodyEl = this.hud.querySelector('#ups-bodies')!;
    this.worldEl = this.hud.querySelector('#ups-world')!;

    this.spawnStars();
    this.wire();
  }

  private markup(): string {
    const cards = ENTRIES.map(
      (e) => `
      <div class="ups-card" tabindex="0" role="button" data-world="${e.id}">
        <div class="ups-glyph">${e.glyph}</div>
        <div class="ups-tag">${e.tag}</div>
        <div class="ups-name">${e.title}</div>
        <div class="ups-blurb">${e.blurb}</div>
        <div class="ups-go">ENTER →</div>
      </div>`
    ).join('');

    return `
      <div class="ups-sweep"></div>
      <div class="ups-stars"></div>
      <div class="ups-inner">
        <div class="ups-eyebrow">Realtime Simulation Suite</div>
        <h1 class="ups-title">Unlimited<b>Possibilities</b></h1>
        <div class="ups-rule"></div>
        <div class="ups-sub">Sandbox · Three.js Engine</div>
        <div class="ups-cards">${cards}</div>
        <div class="ups-hint">Select a world to begin · Drag to orbit · Scroll to zoom · ESC for menu</div>
      </div>
      <div class="ups-foot"><span>Build 1.0.0</span><span>WebGL2 · 60 FPS Target</span></div>`;
  }

  private spawnStars(): void {
    const host = this.root.querySelector('.ups-stars')!;
    for (let i = 0; i < 90; i++) {
      const s = document.createElement('i');
      s.style.left = Math.random() * 100 + '%';
      s.style.top = 100 + Math.random() * 60 + '%';
      const dur = 14 + Math.random() * 26;
      s.style.animationDuration = dur + 's';
      s.style.animationDelay = '-' + Math.random() * dur + 's';
      const sz = Math.random() < 0.15 ? 3 : Math.random() < 0.5 ? 2 : 1;
      s.style.width = s.style.height = sz + 'px';
      s.style.opacity = String(0.25 + Math.random() * 0.6);
      host.appendChild(s);
    }
  }

  private wire(): void {
    this.root.querySelectorAll<HTMLElement>('.ups-card').forEach((card) => {
      const go = () => {
        const id = card.dataset.world as WorldId;
        this.hide();
        this.onSelect(id);
      };
      card.addEventListener('click', go);
      card.addEventListener('keydown', (e) => {
        if ((e as KeyboardEvent).key === 'Enter' || (e as KeyboardEvent).key === ' ') {
          e.preventDefault();
          go();
        }
      });
    });

    this.hud.querySelector('#ups-menu-btn')!.addEventListener('click', () => this.show());

    window.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') this.isOpen() ? this.hide() : this.show();
    });
  }

  onWorldSelected(cb: (id: WorldId) => void): void {
    this.onSelect = cb;
  }

  isOpen(): boolean {
    return !this.root.classList.contains('ups-hidden');
  }

  show(): void {
    this.root.classList.remove('ups-hidden');
    this.hud.classList.add('ups-hidden');
  }

  hide(): void {
    this.root.classList.add('ups-hidden');
    this.hud.classList.remove('ups-hidden');
  }

  setStats(fps: number, bodies: number, world: string): void {
    this.fpsEl.textContent = String(Math.round(fps));
    this.bodyEl.textContent = bodies.toLocaleString();
    this.worldEl.textContent = world;
  }
}
