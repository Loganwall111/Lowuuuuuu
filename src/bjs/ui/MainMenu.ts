/**
 * MainMenu — the AAA front-end.
 *
 * Deliberately NOT a grid of world tiles. There is one universe, so the menu
 * offers actions (continue, new, sandbox, customise) rather than levels to
 * pick between. Everything that configures the simulation lives in-game.
 *
 * Layout follows the reference: a slim top nav, a vertical action list on the
 * left over a full-bleed hero, a news panel on the right, and a status strip
 * along the bottom. The starfield canvas keeps it from ever being flat black.
 */

export interface MenuChoice {
  world: string;
  preset?: string;
  action?: 'continue' | 'new' | 'sandbox' | 'customize' | 'settings';
}

interface MenuAction {
  id: string;
  label: string;
  choice: MenuChoice;
  primary?: boolean;
  hint: string;
}

const ACTIONS: MenuAction[] = [
  { id: 'mContinue', label: 'CONTINUE', primary: true,
    choice: { world: 'planetary', action: 'continue' },
    hint: 'Resume where you left off' },
  { id: 'mNew', label: 'NEW UNIVERSE',
    choice: { world: 'planetary', action: 'new' },
    hint: 'Generate a fresh universe from a new seed' },
  { id: 'mSandbox', label: 'SANDBOX MODE',
    choice: { world: 'sandbox', action: 'sandbox' },
    hint: 'Full creative control from the first second' },
  { id: 'mChaos', label: 'RANDOM EXPERIMENT',
    choice: { world: 'sandbox', preset: 'chaos' },
    hint: 'Roll the dice and see what happens' },
  { id: 'mWeird', label: 'MAKE IT WEIRD',
    choice: { world: 'sandbox', preset: 'weird' },
    hint: 'Alien physics, alien lensing, alien everything' },
  { id: 'mCustomize', label: 'CUSTOMIZATION',
    choice: { world: 'planetary', action: 'customize' },
    hint: 'Open the universe generator settings' }
];

/** Top navigation. Purely a mode hint; all of it lands in the one universe. */
const NAV = [
  { id: 'navPlay', label: 'PLAY', glyph: '▲' },
  { id: 'navCreate', label: 'CREATE', glyph: '◈' },
  { id: 'navExplore', label: 'EXPLORE', glyph: '⬡' },
  { id: 'navControl', label: 'CONTROL', glyph: '⬢' }
];

export const MENU_CSS = `
.menu-root{position:fixed;inset:0;z-index:150;display:block;
  background:radial-gradient(ellipse at 62% 32%,#0d1a33 0%,#060b16 48%,#010307 100%);
  opacity:1;transition:opacity .55s ease;overflow:hidden;
  font-feature-settings:'tnum' 1;}
.menu-root.closing{opacity:0;pointer-events:none}
#menuStars{position:absolute;inset:0;width:100%;height:100%;display:block;z-index:0;opacity:.55}

/* Cinematic hero plate. A rendered starfield alone reads as flat and a bit
   cartoonish; a real photographic plate underneath is what sells the
   AAA look. Slow drift keeps it alive without distracting. */
.menu-plate{position:absolute;inset:-3% -3% -3% -3%;z-index:0;
  background-image:url('/art/menu-hero.jpg');
  background-size:cover;background-position:62% 42%;
  animation:heroDrift 46s ease-in-out infinite alternate;
  will-change:transform;}
@keyframes heroDrift{
  from{transform:scale(1.03) translate3d(0,0,0)}
  to{transform:scale(1.09) translate3d(-1.4%,-1%,0)}}
/* Graded so UI text always has contrast over it. */
.menu-grade{position:absolute;inset:0;z-index:1;pointer-events:none;
  background:
    linear-gradient(90deg,rgba(3,6,14,.94) 0%,rgba(3,6,14,.72) 34%,rgba(3,6,14,.16) 62%,rgba(3,6,14,.42) 100%),
    linear-gradient(0deg,rgba(2,4,10,.92) 0%,transparent 38%);}

/* atmospheric depth over the starfield */
.menu-glow{position:absolute;inset:0;z-index:1;pointer-events:none;
  background:
    radial-gradient(ellipse 60% 45% at 68% 26%,rgba(90,160,255,.20),transparent 70%),
    radial-gradient(ellipse 40% 30% at 30% 78%,rgba(60,110,220,.13),transparent 70%),
    radial-gradient(ellipse 90% 60% at 50% 110%,rgba(3,6,14,.92),transparent 70%);}
.menu-vig{position:absolute;inset:0;z-index:1;pointer-events:none;
  background:radial-gradient(ellipse 78% 78% at 50% 46%,transparent 42%,rgba(0,0,0,.72) 100%);}
.menu-scan{position:absolute;inset:0;z-index:1;pointer-events:none;opacity:.05;
  background:repeating-linear-gradient(0deg,rgba(255,255,255,.9) 0 1px,transparent 1px 3px);}

/* ---------- top bar ---------- */
.menu-top{position:absolute;top:0;left:0;right:0;height:78px;z-index:4;
  display:flex;align-items:center;gap:26px;padding:0 22px;
  background:linear-gradient(180deg,rgba(4,8,16,.90),rgba(4,8,16,0));}
.menu-brand{display:flex;align-items:center;gap:11px;min-width:210px}
.menu-mark{width:38px;height:38px;border-radius:9px;flex:none;
  background:linear-gradient(145deg,#3b8cff,#7c5cff);
  display:grid;place-items:center;font-weight:900;font-size:15px;color:#fff;
  box-shadow:0 0 22px rgba(70,140,255,.55);letter-spacing:-1px}
.menu-brandtext{line-height:1.05}
.menu-brandtext b{display:block;font-size:15.5px;letter-spacing:2.6px;color:#eaf2ff;font-weight:800}
.menu-brandtext span{display:block;font-size:8.5px;letter-spacing:4.2px;color:#61789e}
.menu-brandtext span{color:#61789e}

.menu-nav{display:flex;align-items:center;gap:4px;margin:0 auto}
.menu-navbtn{position:relative;background:none;border:0;cursor:pointer;
  padding:8px 20px 10px;color:#7c8ba6;font-size:10.5px;font-weight:700;
  letter-spacing:2.2px;display:flex;flex-direction:column;align-items:center;gap:5px;
  transition:color .18s}
.menu-navbtn i{font-style:normal;font-size:15px;opacity:.85}
.menu-navbtn:hover{color:#cfe0ff}
.menu-navbtn.on{color:#fff}
.menu-navbtn.on::after{content:'';position:absolute;left:14px;right:14px;bottom:0;height:2px;
  background:linear-gradient(90deg,transparent,#4da3ff,transparent)}

.menu-user{display:flex;align-items:center;gap:11px;
  background:rgba(10,18,32,.72);border:1px solid rgba(90,140,220,.20);
  border-radius:10px;padding:7px 12px}
.menu-av{width:34px;height:34px;border-radius:7px;flex:none;
  background:linear-gradient(150deg,#20304e,#0d1424);
  border:1px solid rgba(120,170,255,.28);display:grid;place-items:center;font-size:16px}
.menu-uinfo{min-width:132px}
.menu-uinfo b{display:block;font-size:12px;color:#eaf2ff;letter-spacing:.3px}
.menu-uinfo em{font-style:normal;display:block;font-size:9px;color:#6d82a4;letter-spacing:1.1px}
.menu-xp{height:3px;border-radius:2px;background:rgba(255,255,255,.10);margin-top:5px;overflow:hidden}
.menu-xp i{display:block;height:100%;width:78%;border-radius:2px;
  background:linear-gradient(90deg,#2f7ddb,#63b3ff)}

/* ---------- hero ---------- */
.menu-hero{position:absolute;left:0;top:78px;bottom:52px;width:min(430px,34vw);z-index:3;
  display:flex;flex-direction:column;justify-content:center;padding-left:26px;
  padding-right:14px}
.menu-title{font-size:clamp(30px,3.5vw,50px);font-weight:900;letter-spacing:-1.4px;
  line-height:.92;margin:0;color:#fff;text-transform:uppercase;
  text-shadow:0 4px 40px rgba(70,140,255,.4)}
.menu-sub{font-size:clamp(8.5px,.85vw,10.5px);color:#5f9fe0;letter-spacing:5.5px;
  text-transform:uppercase;margin:9px 0 12px;font-weight:600}
.menu-blurb{font-size:12px;color:#93a6c4;line-height:1.62;max-width:330px;margin:0 0 20px}

.menu-list{display:flex;flex-direction:column;gap:5px;max-width:330px}
/* Angled hardware-style plates with a scanning sheen and a live accent bar. */
.menu-item{position:relative;display:flex;align-items:center;justify-content:space-between;
  padding:12px 16px 12px 20px;border:1px solid rgba(95,140,205,.18);cursor:pointer;
  background:linear-gradient(100deg,rgba(11,20,36,.80),rgba(9,16,29,.52));
  color:#c3d3ea;font-size:11.5px;font-weight:700;overflow:hidden;
  letter-spacing:2.1px;text-align:left;font-family:inherit;
  backdrop-filter:blur(9px);
  clip-path:polygon(10px 0,100% 0,100% calc(100% - 10px),calc(100% - 10px) 100%,0 100%,0 10px);
  transition:background .17s ease,color .17s ease,border-color .17s ease,
             transform .17s ease,box-shadow .17s ease;}
/* leading accent bar lights up on hover */
.menu-item::before{content:'';position:absolute;left:0;top:0;bottom:0;width:3px;
  background:linear-gradient(180deg,transparent,rgba(120,180,255,.55),transparent);
  opacity:.45;transition:opacity .17s ease, box-shadow .17s ease;}
.menu-item:hover::before{opacity:1;box-shadow:0 0 14px rgba(120,180,255,.9)}
.menu-item::after{content:'›';font-size:16px;opacity:.5;font-weight:400;
  transition:transform .17s ease, opacity .17s ease}
.menu-item:hover{color:#fff;transform:translateX(5px);
  background:linear-gradient(100deg,rgba(28,58,104,.90),rgba(16,32,60,.62));
  border-color:rgba(120,180,255,.50);
  box-shadow:0 6px 26px rgba(30,90,190,.30),
             inset 0 0 24px rgba(90,160,255,.10);}
.menu-item:hover::after{transform:translateX(3px);opacity:.95}
.menu-item.primary{color:#fff;border-color:rgba(150,200,255,.55);
  background:linear-gradient(100deg,#1d63c4,#2f86e8);
  box-shadow:0 6px 30px rgba(40,120,230,.48);}
.menu-item.primary::before{opacity:1;background:linear-gradient(180deg,#bfe0ff,#63a8ff,#bfe0ff);
  box-shadow:0 0 16px rgba(150,205,255,.95)}
.menu-item.primary:hover{background:linear-gradient(100deg,#2775db,#3d97f7);
  box-shadow:0 8px 36px rgba(60,150,250,.58)}

/* ---------- news ---------- */
.menu-news{position:absolute;right:22px;bottom:74px;width:min(320px,26vw);z-index:3;
  background:rgba(8,14,26,.86);border:1px solid rgba(95,140,205,.20);border-radius:5px;
  backdrop-filter:blur(14px);overflow:hidden}
.menu-news-h{display:flex;align-items:center;justify-content:space-between;
  padding:9px 13px;border-bottom:1px solid rgba(95,140,205,.16);
  font-size:9.5px;letter-spacing:2.4px;color:#7d92b4;font-weight:700}
.menu-news-h button{background:none;border:0;color:#61789e;cursor:pointer;font-size:13px;
  color:#66799a;line-height:1;padding:0}
.menu-news-b{padding:13px}
.menu-news-b h4{margin:0 0 7px;font-size:12.5px;color:#eaf2ff;letter-spacing:.7px;font-weight:800}
.menu-news-b p{margin:0 0 11px;font-size:10.5px;color:#8698b6;line-height:1.6}
.menu-news-b button{background:rgba(40,110,210,.20);border:1px solid rgba(110,170,255,.36);
  color:#a8ceff;font-size:9.5px;letter-spacing:1.7px;padding:6px 13px;border-radius:3px;
  cursor:pointer;font-weight:700;font-family:inherit;transition:all .15s}
.menu-news-b button:hover{background:rgba(50,130,240,.34);color:#fff}

/* ---------- bottom strip ---------- */
.menu-foot{position:absolute;left:0;right:0;bottom:0;height:52px;z-index:4;
  display:flex;align-items:center;gap:0;padding:0 22px;
  background:linear-gradient(0deg,rgba(4,8,16,.94),rgba(4,8,16,0));
  border-top:1px solid rgba(95,140,205,.10)}
.menu-cell{display:flex;flex-direction:column;gap:2px;padding:0 20px;
  border-right:1px solid rgba(95,140,205,.12)}
.menu-cell:first-child{padding-left:0}
.menu-cell:last-child{border-right:0}
.menu-cell em{font-style:normal;font-size:8px;letter-spacing:2.2px;color:#5b6f90;font-weight:700}
.menu-cell b{font-size:11.5px;color:#cfe0f7;letter-spacing:.4px;font-weight:600}
.menu-badge{margin-left:auto;padding:5px 12px;border-radius:3px;
  background:rgba(60,120,220,.14);border:1px solid rgba(110,170,255,.24);
  color:#8fbcf5;font-size:9px;letter-spacing:2.2px;font-weight:700}

@media (max-width:900px){
  .menu-hero{width:auto;right:0;padding-right:26px}
  .menu-news{display:none}
  .menu-nav{display:none}
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
      <div class="menu-plate"></div>
      <canvas id="menuStars"></canvas>
      <div class="menu-grade"></div>
      <div class="menu-glow"></div>
      <div class="menu-scan"></div>
      <div class="menu-vig"></div>

      <div class="menu-top">
        <div class="menu-brand">
          <div class="menu-mark">UP</div>
          <div class="menu-brandtext">
            <b>UNLIMITED</b><span>POSSIBILITIES</span>
          </div>
        </div>
        <nav class="menu-nav" id="menuNav"></nav>
        <div class="menu-user">
          <div class="menu-av">👨‍🚀</div>
          <div class="menu-uinfo">
            <b>Commander</b>
            <em>ONE UNIVERSE · NO LIMITS</em>
            <div class="menu-xp"><i></i></div>
          </div>
        </div>
      </div>

      <div class="menu-hero">
        <h1 class="menu-title">UNLIMITED<br/>POSSIBILITIES</h1>
        <div class="menu-sub">Sandbox · Space · Engine</div>
        <p class="menu-blurb">A single continuous universe with no loading between
          places. Create it, fly it, break it, and rewrite the rules while you watch.</p>
        <div class="menu-list" id="menuList"></div>
      </div>

      <div class="menu-news">
        <div class="menu-news-h"><span>LATEST</span><button id="mNewsX">✕</button></div>
        <div class="menu-news-b">
          <h4>One Universe Update</h4>
          <p>Everything now lives in one continuous space. Free flight, movable
             black holes, twelve gravitational lens types, and look-back views
             from inside the event horizon.</p>
          <button id="mNewsGo">VIEW DETAILS</button>
        </div>
      </div>

      <div class="menu-foot">
        <div class="menu-cell"><em>ENGINE</em><b>Babylon.js 9 · WebGL2</b></div>
        <div class="menu-cell"><em>MODE</em><b>Continuous Universe</b></div>
        <div class="menu-cell"><em>LOOP</em><b>Create · Break · Observe</b></div>
        <span class="menu-badge">READY</span>
      </div>`;
    document.body.appendChild(this.root);

    // ---- top nav ----
    const nav = this.root.querySelector('#menuNav') as HTMLElement;
    NAV.forEach((n, i) => {
      const b = document.createElement('button');
      b.className = 'menu-navbtn' + (i === 0 ? ' on' : '');
      b.id = n.id;
      b.innerHTML = `<i>${n.glyph}</i>${n.label}`;
      b.onclick = () => {
        nav.querySelectorAll('.menu-navbtn').forEach((x) => x.classList.remove('on'));
        b.classList.add('on');
      };
      nav.appendChild(b);
    });

    // ---- action list ----
    const list = this.root.querySelector('#menuList') as HTMLElement;
    ACTIONS.forEach((a) => {
      const btn = document.createElement('button');
      btn.className = 'menu-item' + (a.primary ? ' primary' : '');
      btn.id = a.id;
      btn.dataset.action = a.id;
      btn.title = a.hint;
      btn.append(a.label);
      btn.onclick = () => this.pick(a.choice);
      list.appendChild(btn);
    });

    const x = this.root.querySelector('#mNewsX') as HTMLButtonElement | null;
    const news = this.root.querySelector('.menu-news') as HTMLElement | null;
    if (x && news) x.onclick = () => { news.style.display = 'none'; };
    const go = this.root.querySelector('#mNewsGo') as HTMLButtonElement | null;
    if (go) go.onclick = () => this.pick({ world: 'planetary', action: 'new' });

    this.startStars();
  }

  /** Lightweight 2D starfield, so the menu is never a flat black screen. */
  private startStars(): void {
    const cv = this.root.querySelector('#menuStars') as HTMLCanvasElement;
    const ctx = cv.getContext('2d');
    if (!ctx) return;

    let w = 0, h = 0;
    const stars: Array<{ x: number; y: number; z: number; r: number }> = [];
    const resize = () => {
      w = cv.width = Math.max(1, window.innerWidth);
      h = cv.height = Math.max(1, window.innerHeight);
    };
    resize();
    window.addEventListener('resize', resize);

    for (let i = 0; i < 340; i++) {
      stars.push({
        x: Math.random(), y: Math.random(),
        z: 0.2 + Math.random() * 0.8,
        r: Math.random() * 1.5 + 0.25
      });
    }

    let t = 0;
    const draw = () => {
      t += 0.0016;
      ctx.clearRect(0, 0, w, h);

      // a soft nebula wash so the backdrop reads as space, not a black rect
      const g = ctx.createRadialGradient(w * 0.68, h * 0.3, 0, w * 0.68, h * 0.3, w * 0.6);
      g.addColorStop(0, 'rgba(58,110,200,0.16)');
      g.addColorStop(0.5, 'rgba(40,70,150,0.06)');
      g.addColorStop(1, 'rgba(0,0,0,0)');
      ctx.fillStyle = g;
      ctx.fillRect(0, 0, w, h);

      for (const s of stars) {
        const tw = 0.55 + 0.45 * Math.sin(t * 40 * s.z + s.x * 90);
        const px = ((s.x + t * 0.02 * s.z) % 1) * w;
        const py = s.y * h;
        ctx.globalAlpha = Math.min(1, tw * s.z);
        ctx.fillStyle = s.z > 0.72 ? '#dce9ff' : '#8fb4e8';
        ctx.beginPath();
        ctx.arc(px, py, s.r * s.z, 0, 6.283);
        ctx.fill();
      }
      ctx.globalAlpha = 1;
      this.raf = window.requestAnimationFrame(draw);
    };
    draw();
  }

  private pick(c: MenuChoice): void {
    this.root.classList.add('closing');
    this.onPick(c);
    window.setTimeout(() => this.destroy(), 560);
  }

  destroy(): void {
    if (this.raf && typeof window.cancelAnimationFrame === 'function') {
      window.cancelAnimationFrame(this.raf);
    }
    this.raf = 0;
    this.raf = 0;
    this.root.remove();
  }
}
