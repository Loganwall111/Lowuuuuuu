/**
 * PauseMenu — the in-game Escape menu.
 *
 * Pressing Escape mid-flight opens a cockpit-styled overlay: Resume,
 * Settings, Performance, Save, Quit & Save, a Dashboard, and a local
 * Leaderboard. It is the game's own pause surface, distinct from the title
 * screen and from the HUD panels, and it pauses the simulation while open.
 *
 * No image files: every surface is border/glass CSS over the live scene.
 */

export interface PauseMenuHooks {
  onResume(): void;
  onSetting(key: string, value: number): void;
  onQuality(name: string): void;
  onSave(): void;
  onQuitSave(): void;
  dashboard(): Record<string, string>;
  leaderboard(): Array<{ rank: number; name: string; score: number; you: boolean }>;
}

export class PauseMenu {
  private el: HTMLDivElement | null = null;
  private open_ = false;

  get isOpen(): boolean { return this.open_; }

  constructor(private hooks: PauseMenuHooks) {}

  mount(parent: HTMLElement = document.body): void {
    if (this.el) return;
    const el = document.createElement('div');
    el.className = 'pause-menu';
    el.innerHTML = `
      <div class="pause-panel">
        <div class="pause-head">PAUSE</div>
        <div class="pause-row">
          <button class="pause-btn pri" data-act="resume">▶ Resume</button>
          <button class="pause-btn" data-act="settings">⚙ Settings</button>
          <button class="pause-btn" data-act="performance">⚡ Performance</button>
        </div>
        <div class="pause-row">
          <button class="pause-btn" data-act="save">💾 Save</button>
          <button class="pause-btn" data-act="quitsave">⏻ Quit &amp; Save</button>
        </div>
        <div class="pause-row">
          <button class="pause-btn" data-act="dashboard">📊 Dashboard</button>
          <button class="pause-btn" data-act="leaderboard">🏆 Leaderboard</button>
        </div>
        <div class="pause-body" id="pauseBody"></div>
      </div>`;
    parent.appendChild(el);
    this.el = el;

    const closeAll = () => { const b = el.querySelector('#pauseBody'); if (b) b.innerHTML = ''; };
    const act = (id: string) => {
      const body = el.querySelector('#pauseBody') as HTMLElement;
      switch (id) {
        case 'resume': this.hooks.onResume(); break;
        case 'settings': body.innerHTML = this.settingsHtml(); this.bindSettings(el, body); break;
        case 'performance': body.innerHTML = this.performanceHtml(); this.bindPerformance(el, body); break;
        case 'save': this.hooks.onSave(); closeAll(); break;
        case 'quitsave': this.hooks.onQuitSave(); break;
        case 'dashboard': body.innerHTML = this.dashboardHtml(this.hooks.dashboard()); break;
        case 'leaderboard': body.innerHTML = this.leaderboardHtml(this.hooks.leaderboard()); break;
      }
    };
    el.querySelectorAll<HTMLElement>('[data-act]').forEach((b) => {
      b.onclick = () => act(b.dataset.act ?? '');
    });
  }

  toggle(): void {
    if (!this.el) return;
    this.open_ = !this.open_;
    this.el.classList.toggle('on', this.open_);
    if (!this.open_) this.el.querySelector('#pauseBody')!.innerHTML = '';
  }

  open(): void { if (!this.open_) this.toggle(); }
  close(): void { if (this.open_) this.toggle(); }

  /* ------------------------------ sections ------------------------------ */

  private settingsHtml(): string {
    return '<div class="pause-grp"><div class="pause-label">Field of View</div>' +
      '<input type="range" data-set="fov" min="0.6" max="1.4" step="0.05" value="0.9">' +
      '</div>' +
      '<div class="pause-grp"><div class="pause-label">Bloom</div>' +
      '<input type="range" data-set="bloom" min="0" max="2" step="0.05" value="1.42">' +
      '</div>' +
      '<div class="pause-grp"><div class="pause-label">Grain</div>' +
      '<input type="range" data-set="grain" min="0" max="10" step="0.5" value="0">' +
      '</div>' +
      '<div class="pause-grp"><div class="pause-label">Chromatic Aberration</div>' +
      '<input type="range" data-set="chromatic" min="0" max="10" step="0.5" value="0">' +
      '</div>' +
      '<div class="pause-grp"><div class="pause-label">HUD Skin</div>' +
      '<div class="pause-row">' +
      '<button class="pause-btn" data-hud="suit">Exosuit</button>' +
      '<button class="pause-btn" data-hud="satellite">Satellite</button>' +
      '<button class="pause-btn" data-hud="legacy">Legacy</button>' +
      '</div></div>';
  }

  private bindSettings(el: HTMLElement, body: HTMLElement): void {
    body.querySelectorAll<HTMLInputElement>('[data-set]').forEach((s) => {
      s.oninput = () => this.hooks.onSetting(s.dataset.set!, parseFloat(s.value));
    });
    body.querySelectorAll<HTMLElement>('[data-hud]').forEach((b) => {
      b.onclick = () => this.hooks.onSetting('hud:' + b.dataset.hud, 0);
    });
  }

  private performanceHtml(): string {
    return '<div class="pause-grp"><div class="pause-label">Quality Preset</div>' +
      '<div class="pause-row">' +
      '<button class="pause-btn" data-q="performance">Low</button>' +
      '<button class="pause-btn on" data-q="high">High</button>' +
      '<button class="pause-btn" data-q="cinematic">Ultra</button>' +
      '</div></div>' +
      '<div class="pause-note">The game also sheds post-effects automatically ' +
      'when the frame rate drops, and restores them on recovery.</div>';
  }

  private bindPerformance(el: HTMLElement, body: HTMLElement): void {
    body.querySelectorAll<HTMLElement>('[data-q]').forEach((b) => {
      b.onclick = () => {
        body.querySelectorAll<HTMLElement>('[data-q]').forEach((x) => x.classList.remove('on'));
        b.classList.add('on');
        this.hooks.onQuality(b.dataset.q ?? 'high');
      };
    });
  }

  private dashboardHtml(stats: Record<string, string>): string {
    const rows = Object.entries(stats)
      .map(([k, v]) => '<div class="pause-stat"><span>' + k + '</span><b>' + v + '</b></div>')
      .join('');
    return '<div class="pause-grp"><div class="pause-label">Dashboard</div>' + rows + '</div>';
  }

  private leaderboardHtml(rows: Array<{ rank: number; name: string; score: number; you: boolean }>): string {
    const out = rows.map((r) =>
      '<div class="pause-stat' + (r.you ? ' you' : '') + '">' +
      '<span>' + r.rank + '. ' + r.name + '</span><b>' + r.score + '</b></div>').join('');
    return '<div class="pause-grp"><div class="pause-label">Leaderboard (local)</div>' + out + '</div>';
  }

  dispose(): void {
    this.el?.remove();
    this.el = null;
    this.open_ = false;
  }
}
