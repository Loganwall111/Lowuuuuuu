/**
 * IntroOverlay — the thin DOM layer over the opening sequence.
 *
 * There is no menu screen any more, so this draws very little: the title
 * card, the subtitles the instructors speak, and the prompt to step through
 * the portal. Everything else is in the world itself.
 *
 * The sim renders live behind all of it, so there is never a black screen.
 */

import type { IntroSequence, Lesson } from '../systems/IntroSequence';

export const INTRO_CSS = `
.intro-root{
  position:fixed; inset:0; z-index:60; pointer-events:none;
  font-family:'Inter',system-ui,-apple-system,'Segoe UI',sans-serif;
  color:#eef3ff;
}
.intro-root.on{ pointer-events:auto; }

/* ---- title card ---- */
/* The title card must never read as a black screen. It is a vignette over
   the live sim, not a curtain: the middle stays clear so you can see the
   garage behind it, and the darkest corner is still visibly blue rather
   than near-black. */
.intro-title{
  position:absolute; inset:0; display:flex; flex-direction:column;
  align-items:center; justify-content:center; gap:26px;
  /* The hero plate sits behind the title so there is always a real image
     behind the Play button. Previously this was a bare gradient over the
     canvas, so if the canvas was dark the whole card - and the area around
     the button - read as solid black. The gradient is now layered ON TOP of
     the artwork purely to keep the text legible. */
  background-image:
    radial-gradient(ellipse at 50% 45%,
      rgba(10,16,34,.30) 0%, rgba(7,12,26,.58) 55%, rgba(5,9,20,.78) 100%),
    url('/art/menu-hero.jpg');
  background-size:cover, cover;
  background-position:center center, center center;
  background-repeat:no-repeat, no-repeat;
  /* Fallback colour if the image ever fails to load: a lit blue, never black. */
  background-color:#101a36;
  transition:opacity .6s ease;
}
.intro-title h1{
  margin:0; font-size:clamp(30px,6.5vw,78px); font-weight:800;
  letter-spacing:.14em; text-align:center; line-height:1.06;
  background:linear-gradient(180deg,#ffffff 0%,#9fc6ff 58%,#4d86e8 100%);
  -webkit-background-clip:text; background-clip:text; color:transparent;
  text-shadow:0 0 62px rgba(90,150,255,.34);
}
.intro-sub{
  margin:0; font-size:clamp(11px,1.5vw,15px); letter-spacing:.42em;
  text-transform:uppercase; color:#9fb2d8; text-align:center;
}
/* The play button is the first thing anyone touches, so it is built like a
   piece of hardware: a bevelled plate with its own light, a sweeping sheen,
   and a bracket frame that charges up on hover. */
.intro-play{
  position:relative; margin-top:18px; padding:18px 74px;
  font-size:17px; font-weight:800; letter-spacing:.28em;
  text-transform:uppercase; cursor:pointer; color:#eaf6ff; border:0;
  background:
    linear-gradient(180deg,rgba(255,255,255,.30) 0%,rgba(255,255,255,0) 42%),
    linear-gradient(180deg,#4aa8ff 0%,#2f7ce8 46%,#1552b4 100%);
  clip-path:polygon(18px 0,100% 0,100% calc(100% - 18px),
                    calc(100% - 18px) 100%,0 100%,0 18px);
  text-shadow:0 1px 0 rgba(0,0,0,.45), 0 0 22px rgba(150,210,255,.65);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.55),
    inset 0 -2px 0 rgba(0,20,60,.55),
    0 12px 34px rgba(30,110,230,.48),
    0 0 0 1px rgba(140,200,255,.35);
  transition:transform .16s cubic-bezier(.2,.9,.3,1.4),
             box-shadow .2s ease, filter .2s ease, letter-spacing .2s ease;
  overflow:hidden; isolation:isolate;
}
/* A specular sweep that crosses the plate on hover. */
.intro-play::before{
  content:''; position:absolute; inset:0; z-index:-1;
  background:linear-gradient(115deg,
    transparent 0%, transparent 38%,
    rgba(255,255,255,.42) 50%, transparent 62%, transparent 100%);
  transform:translateX(-130%);
  transition:transform .55s cubic-bezier(.25,.8,.3,1);
}
.intro-play:hover::before{ transform:translateX(130%); }
.intro-play:hover{
  transform:translateY(-3px) scale(1.02); letter-spacing:.32em;
  filter:brightness(1.08) saturate(1.1);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.7),
    inset 0 -2px 0 rgba(0,20,60,.5),
    0 18px 48px rgba(60,150,255,.62),
    0 0 0 1px rgba(180,225,255,.55),
    0 0 60px rgba(70,160,255,.45);
}
.intro-play:active{ transform:translateY(0) scale(.99); filter:brightness(.95); }
.intro-play:focus-visible{ outline:2px solid #bfe0ff; outline-offset:4px; }
.intro-skip{
  position:absolute; right:22px; bottom:20px; padding:8px 20px;
  font-size:12px; letter-spacing:.16em; text-transform:uppercase;
  cursor:pointer; color:#c3d3ee; background:rgba(16,24,44,.7);
  border:1px solid rgba(130,170,230,.3);
  clip-path:polygon(9px 0,100% 0,100% calc(100% - 9px),
                    calc(100% - 9px) 100%,0 100%,0 9px);
}
.intro-skip:hover{ color:#fff; border-color:rgba(150,200,255,.6); }

/* ---- dialogue ---- */
.intro-talk{
  position:absolute; left:50%; bottom:9%; transform:translateX(-50%);
  width:min(680px,86vw); padding:15px 20px 16px;
  background:linear-gradient(180deg,rgba(12,19,36,.93),rgba(8,13,26,.95));
  border:1px solid rgba(120,165,230,.32);
  clip-path:polygon(15px 0,100% 0,100% calc(100% - 15px),
                    calc(100% - 15px) 100%,0 100%,0 15px);
  box-shadow:0 16px 50px rgba(0,0,0,.55);
}
.intro-who{
  font-size:11px; letter-spacing:.26em; text-transform:uppercase;
  color:#63b4ff; margin-bottom:7px; font-weight:700;
}
.intro-line{ font-size:15.5px; line-height:1.55; color:#e9f0ff; }
.intro-keys{ display:flex; gap:7px; margin-top:11px; flex-wrap:wrap; }
.intro-key{
  padding:4px 11px; font-size:11.5px; font-weight:700; letter-spacing:.08em;
  color:#cfe4ff; background:rgba(40,70,120,.55);
  border:1px solid rgba(120,175,245,.45); border-radius:4px;
}
.intro-next{
  margin-top:12px; font-size:11px; letter-spacing:.2em; text-transform:uppercase;
  color:#8fa8ce; display:flex; justify-content:space-between; align-items:center;
}
.intro-next b{ color:#63b4ff; }
.intro-bar{
  position:absolute; left:0; bottom:0; height:2px;
  background:linear-gradient(90deg,#3ea0ff,#9fe0ff); transition:width .3s ease;
}

/* ---- centre prompt ---- */
.intro-prompt{
  position:absolute; left:50%; top:56%; transform:translateX(-50%);
  padding:10px 26px; font-size:13px; letter-spacing:.18em;
  text-transform:uppercase; color:#dbe8ff;
  background:rgba(10,18,36,.72); border:1px solid rgba(120,170,240,.4);
  clip-path:polygon(11px 0,100% 0,100% calc(100% - 11px),
                    calc(100% - 11px) 100%,0 100%,0 11px);
  animation:introPulse 1.7s ease-in-out infinite;
}
@keyframes introPulse{ 0%,100%{opacity:.68} 50%{opacity:1} }
.intro-hide{ display:none !important; }
`;

export interface IntroHooks {
  onPlay(): void;
  onSkip(): void;
  onAdvance(): void;
}

export class IntroOverlay {
  private root: HTMLDivElement;
  private titleCard: HTMLDivElement;
  private talk: HTMLDivElement;
  private prompt: HTMLDivElement;
  private seq: IntroSequence;
  private hooks: IntroHooks;
  private keyHandler: (e: KeyboardEvent) => void;

  constructor(seq: IntroSequence, hooks: IntroHooks) {
    this.seq = seq;
    this.hooks = hooks;

    if (!document.getElementById('intro-css')) {
      const st = document.createElement('style');
      st.id = 'intro-css';
      st.textContent = INTRO_CSS;
      document.head.appendChild(st);
    }

    this.root = document.createElement('div');
    this.root.className = 'intro-root on';

    // ---- title ----
    this.titleCard = document.createElement('div');
    this.titleCard.className = 'intro-title';
    this.titleCard.innerHTML = `
      <p class="intro-sub">A cosmic sandbox</p>
      <h1>UNLIMITED<br>POSSIBILITIES<br>SANDBOX</h1>
      <p class="intro-sub">Create · Experiment · Break · Observe</p>
    `;
    const play = document.createElement('button');
    play.className = 'intro-play';
    play.textContent = 'Play';
    play.onclick = () => this.hooks.onPlay();
    this.titleCard.appendChild(play);

    const skip = document.createElement('button');
    skip.className = 'intro-skip';
    skip.textContent = 'Skip intro';
    skip.onclick = () => this.hooks.onSkip();
    this.titleCard.appendChild(skip);

    // ---- dialogue ----
    this.talk = document.createElement('div');
    this.talk.className = 'intro-talk intro-hide';

    // ---- prompt ----
    this.prompt = document.createElement('div');
    this.prompt.className = 'intro-prompt intro-hide';

    this.root.append(this.titleCard, this.talk, this.prompt);
    document.body.appendChild(this.root);

    // Enter/Space advances dialogue; Escape always escapes the intro. Being
    // stuck in a tutorial with no way out is unforgivable.
    this.keyHandler = (e: KeyboardEvent) => {
      const st = this.seq.state;
      if (st.done) return;
      if (e.key === 'Escape') { this.hooks.onSkip(); return; }
      if ((e.key === 'Enter' || e.key === ' ') && st.stage !== 'title') {
        this.hooks.onAdvance();
      }
    };
    window.addEventListener('keydown', this.keyHandler);

    this.render();
    this.seq.onChange(() => this.render());
  }

  /** Redraws for the current stage. */
  render(): void {
    const st = this.seq.state;
    const show = (el: HTMLElement, on: boolean) =>
      el.classList.toggle('intro-hide', !on);

    show(this.titleCard, st.stage === 'title');

    const lesson = this.seq.currentLesson;
    show(this.talk, st.stage === 'lesson' && !!lesson);
    if (lesson) this.renderLesson(lesson);

    const promptFor: Record<string, string> = {
      garage: 'Walk to the door  ·  W A S D',
      portal: 'Step through the portal  ·  Enter',
      ship: 'Walk to a console to use it'
    };
    const text = promptFor[st.stage];
    show(this.prompt, !!text && !st.done);
    if (text) this.prompt.textContent = text;

    // Once you are playing the overlay must not eat clicks.
    this.root.classList.toggle('on', !st.done);
    if (st.done) this.root.classList.add('intro-hide');
  }

  private renderLesson(l: Lesson): void {
    const keys = l.keys.map((k) => `<span class="intro-key">${k}</span>`).join('');
    const pct = Math.round(this.seq.progress * 100);
    this.talk.innerHTML = `
      <div class="intro-who">${l.speaker}</div>
      <div class="intro-line">${l.text}</div>
      ${keys ? `<div class="intro-keys">${keys}</div>` : ''}
      <div class="intro-next">
        <span>${l.requires === 'none' ? 'Press <b>Enter</b>' : 'Try it'}</span>
        <span>Esc to skip</span>
      </div>
      <div class="intro-bar" style="width:${pct}%"></div>
    `;
  }

  /** Takes the overlay down for good. */
  dispose(): void {
    window.removeEventListener('keydown', this.keyHandler);
    this.root.remove();
  }
}
