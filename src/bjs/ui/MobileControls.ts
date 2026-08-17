/** iPad-only cockpit controls. Desktop markup is never mounted. */
export function isIPadDevice(nav: Navigator = navigator): boolean {
  return /iPad/i.test(nav.userAgent) ||
    (nav.platform === 'MacIntel' && (nav.maxTouchPoints ?? 0) > 1);
}

export interface MobileControlHooks {
  key(key: string, down: boolean): void;
  look(dx: number, dy: number): void;
  throttle(direction: 1 | -1): void;
  action(name: 'land' | 'portal' | 'photo'): void;
}

const CSS = `
.mobile-cockpit{position:fixed;inset:0;z-index:180;pointer-events:none;
  font-family:'JetBrains Mono',ui-monospace,monospace;touch-action:none;user-select:none}
.mobile-cockpit button,.mobile-look{pointer-events:auto;-webkit-tap-highlight-color:transparent}
.mobile-stick{position:absolute;left:max(22px,env(safe-area-inset-left));bottom:max(28px,env(safe-area-inset-bottom));
  width:174px;height:174px;display:grid;grid-template:repeat(3,1fr)/repeat(3,1fr);gap:6px}
.mobile-stick button,.mobile-actions button,.mobile-throttle button{border:1px solid rgba(0,240,255,.42);
  color:#cfffff;background:rgba(3,14,28,.58);backdrop-filter:blur(10px);border-radius:12px;
  font:700 17px/1 inherit;text-shadow:0 0 9px #00f0ff;box-shadow:inset 0 0 16px rgba(0,240,255,.08)}
.mobile-stick button.on,.mobile-actions button:active,.mobile-throttle button:active{background:rgba(0,240,255,.28);transform:scale(.94)}
.mobile-stick .f{grid-area:1/2}.mobile-stick .l{grid-area:2/1}.mobile-stick .b{grid-area:3/2}.mobile-stick .r{grid-area:2/3}
.mobile-stick .u{grid-area:1/3}.mobile-stick .d{grid-area:3/3;font-size:12px}
.mobile-look{position:absolute;right:max(18px,env(safe-area-inset-right));bottom:max(24px,env(safe-area-inset-bottom));
  width:min(38vw,360px);height:min(38vh,280px);border:1px solid rgba(118,87,255,.28);border-radius:28px;
  background:radial-gradient(circle,rgba(118,87,255,.08),rgba(3,10,25,.18));box-shadow:inset 0 0 35px rgba(0,240,255,.07)}
.mobile-look::after{content:'DRAG TO LOOK';position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);
  font-size:9px;letter-spacing:.25em;color:rgba(160,220,255,.32);white-space:nowrap}
.mobile-actions{position:absolute;right:max(28px,env(safe-area-inset-right));top:max(86px,env(safe-area-inset-top));display:flex;gap:8px}
.mobile-actions button{padding:11px 14px;font-size:10px;letter-spacing:.1em}
.mobile-throttle{position:absolute;left:max(26px,env(safe-area-inset-left));top:44%;display:flex;flex-direction:column;gap:8px}
.mobile-throttle button{width:46px;height:42px}.mobile-throttle small{font-size:7px;color:#86dbe8;text-align:center;letter-spacing:.1em}
@media (orientation:portrait){.mobile-look{width:44vw;height:30vh}.mobile-stick{width:156px;height:156px}}
body:not([data-playing="1"]) .mobile-cockpit,body[data-photo="1"] .mobile-cockpit{display:none}
`;

export class MobileControls {
  private root: HTMLDivElement | null = null;
  private hooks: MobileControlHooks;
  private held = new Set<string>();

  constructor(hooks: MobileControlHooks) { this.hooks = hooks; }

  mount(): boolean {
    if (!isIPadDevice() || this.root) return false;
    document.body.dataset.inputMode = 'ipad';
    const style = document.createElement('style'); style.id = 'mobile-controls-css'; style.textContent = CSS;
    document.head.appendChild(style);
    const root = document.createElement('div'); root.className = 'mobile-cockpit';
    root.innerHTML = `<div class="mobile-stick">
      <button class="f" data-key="w">▲</button><button class="l" data-key="a">◀</button>
      <button class="r" data-key="d">▶</button><button class="b" data-key="s">▼</button>
      <button class="u" data-key=" ">↥</button><button class="d" data-key="control">DESC</button></div>
      <div class="mobile-throttle"><small>THR</small><button data-thr="1">＋</button><button data-thr="-1">－</button></div>
      <div class="mobile-actions"><button data-action="land">LAND</button><button data-action="portal">PORTAL</button><button data-action="photo">PHOTO</button></div>
      <div class="mobile-look" aria-label="Touch and drag to look"></div>`;
    document.body.appendChild(root); this.root = root;
    root.querySelectorAll<HTMLButtonElement>('[data-key]').forEach((b) => {
      const key = b.dataset.key!;
      const down = (e: PointerEvent) => { e.preventDefault(); b.setPointerCapture(e.pointerId); this.held.add(key); b.classList.add('on'); this.hooks.key(key, true); };
      const up = (e: PointerEvent) => { e.preventDefault(); this.held.delete(key); b.classList.remove('on'); this.hooks.key(key, false); };
      b.addEventListener('pointerdown', down); b.addEventListener('pointerup', up); b.addEventListener('pointercancel', up);
    });
    root.querySelectorAll<HTMLButtonElement>('[data-thr]').forEach((b) => b.onclick = () => this.hooks.throttle(Number(b.dataset.thr) > 0 ? 1 : -1));
    root.querySelectorAll<HTMLButtonElement>('[data-action]').forEach((b) => b.onclick = () => this.hooks.action(b.dataset.action as any));
    const look = root.querySelector<HTMLElement>('.mobile-look')!;
    let id = -1, x = 0, y = 0;
    look.onpointerdown = (e) => { e.preventDefault(); id = e.pointerId; x = e.clientX; y = e.clientY; look.setPointerCapture(id); };
    look.onpointermove = (e) => { if (e.pointerId !== id) return; const dx=e.clientX-x, dy=e.clientY-y; x=e.clientX; y=e.clientY; this.hooks.look(dx,dy); };
    look.onpointerup = look.onpointercancel = (e) => { if (e.pointerId === id) id = -1; };
    return true;
  }

  dispose(): void {
    for (const key of this.held) this.hooks.key(key, false);
    this.held.clear(); this.root?.remove(); this.root = null;
    document.getElementById('mobile-controls-css')?.remove();
    delete document.body.dataset.inputMode;
  }
}
