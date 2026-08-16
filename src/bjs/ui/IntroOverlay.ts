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
import {
  CURRENT_UPDATE, CURRENT_UPDATE_NAME, countByTag, latestRelease
} from '../content/PatchNotes';

/** Escapes text destined for innerHTML. Patch copy is authored, but this
 *  is the one place player-visible strings become markup, so it is not
 *  left to trust. */
function esc(s: string): string {
  return String(s)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

/** Builds the patch notes panel markup from the structured release data. */
export function renderPatchNotes(): string {
  const r = latestRelease();
  const counts = countByTag(r);
  const summary = [
    counts.new ? counts.new + ' new' : '',
    counts.fixed ? counts.fixed + ' fixed' : '',
    counts.improved ? counts.improved + ' improved' : ''
  ].filter(Boolean).join(' · ');
  const rows = r.entries.map((e) =>
    '<li class="ip-row"><span class="ip-tag ip-' + e.tag + '">'
    + e.tag.toUpperCase() + '</span>'
    + '<div class="ip-text"><b>' + esc(e.title) + '</b>'
    + '<p>' + esc(e.body) + '</p></div></li>').join('');
  return '<div class="ip-head">'
    + '<span class="ip-ver">' + esc(CURRENT_UPDATE) + '</span>'
    + '<span class="ip-name">' + esc(CURRENT_UPDATE_NAME) + '</span>'
    + '<span class="ip-count">' + esc(summary) + '</span></div>'
    + '<p class="ip-tagline">' + esc(r.tagline) + '</p>'
    + '<ul class="ip-list">' + rows + '</ul>';
}

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
/* AAA title treatment over the hero plate: a slow aurora that breathes
   across the Jupiter backdrop and a film vignette that pulls the eye to the
   logotype without ever dimming the planet behind it. Both are blended
   light - the artwork stays exactly as it is underneath. */
.intro-title::before{
  content:''; position:absolute; inset:0; pointer-events:none;
  mix-blend-mode:screen;
  background:
    radial-gradient(120% 90% at 50% -12%,
      rgba(120,170,255,.18) 0%, rgba(120,170,255,0) 55%),
    radial-gradient(90% 70% at 50% 118%,
      rgba(96,64,180,.20) 0%, rgba(96,64,180,0) 62%);
  animation:introAurora 16s ease-in-out infinite alternate;
}
.intro-title::after{
  content:''; position:absolute; inset:0; pointer-events:none;
  background:
    linear-gradient(180deg,
      rgba(2,6,16,.16) 0%, rgba(2,6,16,0) 24%,
      rgba(2,6,16,0) 76%, rgba(2,6,16,.34) 100%),
    radial-gradient(ellipse at center,
      rgba(2,4,12,0) 56%, rgba(2,4,12,.5) 100%);
}
@keyframes introAurora{
  from{ transform:translateY(-1.6%) scale(1.03); opacity:.8; }
  to{ transform:translateY(1.6%) scale(1.07); opacity:1; }
}
/* The logotype's halo breathes, like a beacon rather than a print mark. */
.intro-title h1{
  animation:introGlow 6s ease-in-out infinite alternate;
}
@keyframes introGlow{
  from{ text-shadow:0 0 42px rgba(90,150,255,.26); }
  to{ text-shadow:0 0 86px rgba(110,170,255,.55); }
}
/* The hero plate drifts very slowly behind the text - a living backdrop
   rather than a frozen photograph. */
.intro-title{
  animation:introKenBurns 40s ease-in-out infinite alternate;
}
@keyframes introKenBurns{
  from{ background-position:center 46%, center 46%; }
  to{ background-position:center 54%, center 54%; }
}
/* Cinematic letterbox bars, so the menu reads as a film frame. */
.intro-cinema{
  position:absolute;left:0;right:0;height:9vh;min-height:54px;
  background:linear-gradient(180deg,rgba(2,4,10,.96),rgba(2,4,10,.82));
  z-index:2;pointer-events:none;
}
.intro-cinema.t{top:0;box-shadow:0 1px 0 rgba(140,200,255,.10);}
.intro-cinema.b{bottom:0;box-shadow:0 -1px 0 rgba(140,200,255,.10);}
/* A single scan line that sweeps the title once on load, like a projector
   warming up, then fades out. */
.intro-scan{
  position:absolute;left:0;right:0;height:2px;z-index:3;pointer-events:none;
  background:linear-gradient(90deg,transparent,rgba(160,210,255,.9),transparent);
  box-shadow:0 0 26px rgba(120,180,255,.8);
  animation:introScan 1.8s cubic-bezier(.2,.7,.2,1) forwards;
}
@keyframes introScan{
  0%{top:-2%;opacity:0}
  12%{opacity:1}
  100%{top:104%;opacity:0}
}
/* The logotype assembles itself: rises and snaps its letters into place. */
.intro-logo{
  animation:introReveal .95s cubic-bezier(.2,.8,.2,1) both;
}
@keyframes introReveal{
  from{opacity:0;transform:translateY(14px);letter-spacing:.5em;}
  to{opacity:1;transform:translateY(0);}
}
/* The subtitle reads as a holographic caption, not plain text. */
.intro-sub{
  text-shadow:0 0 18px rgba(110,170,255,.35);
}
.intro-title h1{
  margin:0; font-size:clamp(30px,6.5vw,78px); font-weight:800;
  letter-spacing:.14em; text-align:center; line-height:1.06;
  background:linear-gradient(180deg,#ffffff 0%,#9fc6ff 58%,#4d86e8 100%);
  -webkit-background-clip:text; background-clip:text; color:transparent;
  text-shadow:0 0 62px rgba(90,150,255,.34);
}
/* The logotype reads across on one line. The three words carry different
   weights and sizes so it is a mark rather than a sentence, and it wraps
   only on genuinely narrow screens. */
.intro-logo{
  display:flex; align-items:baseline; justify-content:center;
  gap:.34em; flex-wrap:wrap;
  font-size:clamp(20px,4.6vw,58px);
}
.intro-logo .il-1{ font-weight:300; letter-spacing:.30em; opacity:.92; }
.intro-logo .il-2{ font-weight:900; letter-spacing:.06em; }
.intro-logo .il-3{
  font-weight:500; letter-spacing:.34em; font-size:.46em; opacity:.8;
  align-self:center;
}
/* The release badge, directly under the mark. */
.intro-release{
  display:flex; align-items:center; justify-content:center; gap:10px;
  margin-top:14px;
}
.ir-badge{
  padding:3px 11px 4px; font-size:10px; font-weight:800; letter-spacing:.22em;
  color:#04101f; background:linear-gradient(180deg,#8fd0ff,#3f97f0);
  border-radius:3px; box-shadow:0 0 20px rgba(90,180,255,.5);
}
.ir-name{
  font-size:11px; font-weight:600; letter-spacing:.34em; color:#9fc6ff;
  text-transform:uppercase;
}
.intro-sub{
  margin:0; font-size:clamp(11px,1.5vw,15px); letter-spacing:.42em;
  text-transform:uppercase; color:#9fb2d8; text-align:center;
}
/* The play button is the first thing anyone touches, so it is built like a
   piece of hardware: a bevelled plate with its own light, a sweeping sheen,
   and a bracket frame that charges up on hover. */
.intro-modes{ display:flex; flex-direction:column; gap:14px; margin-top:24px;
  align-items:center; }
/* The doors assemble one after another, bottom to top. */
.intro-modes > *{ animation:introDoor .5s cubic-bezier(.2,.8,.2,1) both; }
.intro-modes > *:nth-child(1){ animation-delay:.35s; }
.intro-modes > *:nth-child(2){ animation-delay:.44s; }
.intro-modes > *:nth-child(3){ animation-delay:.53s; }
.intro-modes > *:nth-child(4){ animation-delay:.62s; }
@keyframes introDoor{
  from{ opacity:0; transform:translateY(16px) scale(.96); }
  to{ opacity:1; transform:translateY(0) scale(1); } }
/* A subtle scanline texture inside every door, so they read as machined
   plates rather than flat gradients. */
.intro-play::after{
  content:''; position:absolute; inset:0; pointer-events:none; opacity:.25;
  background:repeating-linear-gradient(180deg,
    rgba(255,255,255,.06) 0 1px, transparent 1px 4px);
  mix-blend-mode:screen;}
.intro-play{
  position:relative; padding:16px 34px 15px;
  min-width:250px; display:flex; flex-direction:column; gap:5px;
  align-items:center;
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
/* Patch notes sits in the same row so the choices read as one horizontal
   set, but it is deliberately smaller and quieter than the two doors: it
   is something you read, not a way into the game. */
.intro-aux{
  position:relative; padding:11px 22px 10px; align-self:center;
  display:flex; flex-direction:column; gap:3px; align-items:center;
  font-family:inherit; cursor:pointer; color:#b9cde9;
  font-size:12px; font-weight:700; letter-spacing:.2em; text-transform:uppercase;
  background:linear-gradient(180deg,rgba(38,50,76,.72),rgba(18,25,42,.72));
  border:1px solid rgba(140,175,225,.24); border-radius:4px;
  transition:filter .2s ease, transform .2s ease, border-color .2s ease;
}
.intro-aux:hover{
  filter:brightness(1.2); transform:translateY(-2px);
  border-color:rgba(170,205,250,.45); color:#e2eeff;
}
.intro-aux b{ font-size:12px; letter-spacing:.18em; font-weight:800; }
.intro-aux i{ font-size:9.5px; letter-spacing:.05em; font-style:normal;
  text-transform:none; opacity:.68; font-weight:500; }

/* The panel. Scrolls internally so a long release cannot push the buttons
   off the screen. */
.intro-patch{
  margin-top:18px; width:min(760px,86vw); max-height:min(44vh,420px);
  overflow-y:auto; text-align:left;
  padding:16px 18px 18px;
  background:linear-gradient(160deg,rgba(12,20,38,.94),rgba(7,12,24,.9));
  border:1px solid rgba(140,180,240,.22); border-radius:8px;
  box-shadow:0 20px 60px rgba(0,0,0,.6);
}
.ip-head{ display:flex; align-items:center; gap:10px; flex-wrap:wrap;
  padding-bottom:9px; border-bottom:1px solid rgba(150,190,245,.16); }
.ip-ver{ font-size:11px; font-weight:800; letter-spacing:.2em; color:#04101f;
  background:linear-gradient(180deg,#8fd0ff,#3f97f0); padding:3px 9px 4px;
  border-radius:3px; }
.ip-name{ font-size:13px; font-weight:700; letter-spacing:.24em; color:#cfe4ff; }
.ip-count{ margin-left:auto; font-size:10px; letter-spacing:.14em;
  color:#7f9ac4; text-transform:uppercase; }
.ip-tagline{ margin:10px 0 4px; font-size:12px; color:#9fb8de;
  font-style:italic; letter-spacing:.02em; }
.ip-list{ list-style:none; margin:0; padding:0; }
.ip-row{ display:flex; gap:11px; padding:11px 0;
  border-bottom:1px solid rgba(140,180,240,.09); }
.ip-row:last-child{ border-bottom:0; }
.ip-tag{ flex:0 0 auto; align-self:flex-start; margin-top:2px;
  font-size:8.5px; font-weight:800; letter-spacing:.14em; padding:3px 7px;
  border-radius:3px; min-width:52px; text-align:center; }
.ip-new{ color:#04140c; background:linear-gradient(180deg,#69e6a6,#2fbf7c); }
.ip-fixed{ color:#1a0d02; background:linear-gradient(180deg,#ffc978,#f0993a); }
.ip-improved{ color:#04101f; background:linear-gradient(180deg,#8fd0ff,#3f97f0); }
.ip-text b{ display:block; font-size:12.5px; font-weight:700; color:#e6f0ff;
  letter-spacing:.04em; margin-bottom:3px; }
.ip-text p{ margin:0; font-size:11.5px; line-height:1.58; color:#9db4d6;
  letter-spacing:.01em; }

.intro-play b{ font-size:17px; letter-spacing:.24em; font-weight:800; }
/* The one-line explanation of what the mode IS, so the choice can be made
   without having to try both. */
.intro-play i{ font-size:10.5px; letter-spacing:.06em; font-style:normal;
  text-transform:none; opacity:.86; font-weight:600; }
/* Sandbox is the dangerous one and looks it. */
.intro-play-sandbox{
  background:
    linear-gradient(180deg,rgba(255,255,255,.30) 0%,rgba(255,255,255,0) 42%),
    linear-gradient(180deg,#b05cff 0%,#8b3ce0 46%,#5a1aa8 100%);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.55),
    inset 0 -2px 0 rgba(30,0,60,.55),
    0 12px 34px rgba(150,50,230,.48),
    0 0 0 1px rgba(210,160,255,.35);
}
.intro-play-sandbox:hover{
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.7),
    inset 0 -2px 0 rgba(30,0,60,.5),
    0 18px 48px rgba(170,70,255,.62),
    0 0 0 1px rgba(225,190,255,.55),
    0 0 60px rgba(160,70,255,.45);
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

/* ================================================================
   AAA overhaul - the same Jupiter plate, dressed as a launch screen.
   The artwork underneath never changes; these are layered light.
   ================================================================ */

/* A slow starfield drifting behind the planet, so the backdrop is alive. */
.intro-stars{position:absolute;inset:-40% 0 0 0;pointer-events:none;opacity:.55;
  background-image:
    radial-gradient(1px 1px at 12% 22%, rgba(255,255,255,.9) 0, transparent 100%),
    radial-gradient(1px 1px at 71% 9%,  rgba(255,255,255,.7) 0, transparent 100%),
    radial-gradient(1.5px 1.5px at 41% 31%, rgba(255,220,170,.8) 0, transparent 100%),
    radial-gradient(1px 1px at 88% 41%, rgba(255,255,255,.6) 0, transparent 100%),
    radial-gradient(1px 1px at 23% 53%, rgba(255,255,255,.55) 0, transparent 100%),
    radial-gradient(1.5px 1.5px at 57% 61%, rgba(200,225,255,.65) 0, transparent 100%),
    radial-gradient(1px 1px at 5% 70%,  rgba(255,255,255,.5) 0, transparent 100%),
    radial-gradient(1px 1px at 94% 77%, rgba(255,255,255,.5) 0, transparent 100%);
  background-size:760px 760px;
  animation:introStars 300s linear infinite;}
@keyframes introStars{ from{transform:translateY(0)} to{transform:translateY(-760px)} }

/* A faint landing-pad grid rising from the foot of the frame. */
.intro-grid{position:absolute;left:-10%;right:-10%;bottom:-6px;height:32vh;
  pointer-events:none;opacity:.30;
  background-image:
    linear-gradient(rgba(80,180,255,.16) 1px, transparent 1px),
    linear-gradient(90deg, rgba(80,180,255,.16) 1px, transparent 1px);
  background-size:52px 52px;
  transform:perspective(620px) rotateX(63deg);transform-origin:bottom;
  -webkit-mask-image:linear-gradient(180deg,transparent,#000 70%);
  mask-image:linear-gradient(180deg,transparent,#000 70%);
  animation:introGrid 9s linear infinite;}
@keyframes introGrid{ from{background-position:0 0} to{background-position:0 52px} }

/* Top-left identity, like the boot logo of a launch screen. */
.intro-brand{position:absolute;left:28px;top:22px;z-index:4;
  font-size:11px;letter-spacing:.32em;text-transform:uppercase;
  color:rgba(150,215,255,.72);}
.intro-brand b{color:var(--acc,#3fc4ff);font-weight:800;text-shadow:0 0 14px rgba(80,180,255,.6);}

/* A spinning hex emblem above the logotype - the studio mark. */
.intro-emblem{width:66px;height:66px;position:relative;display:grid;place-items:center;
  margin-bottom:-4px;}
.intro-emblem::before{content:'';position:absolute;inset:0;
  clip-path:polygon(50% 0,93% 25%,93% 75%,50% 100%,7% 75%,7% 25%);
  background:linear-gradient(135deg,#9fd6ff 0%,#4fb6ff 55%,#2f7de8 100%);
  box-shadow:0 0 34px rgba(70,160,255,.45);}
.intro-emblem span{position:absolute;inset:4px;
  clip-path:polygon(50% 0,93% 25%,93% 75%,50% 100%,7% 75%,7% 25%);
  background:#0a0e18;animation:introEmblem 26s linear infinite;}
@keyframes introEmblem{ to{transform:rotate(360deg)} }

/* The accent line that sweeps open under the logotype. */
.intro-kicker{display:block;width:min(440px,62vw);height:2px;margin-top:-8px;
  background:linear-gradient(90deg,transparent,var(--acc,#3fc4ff),transparent);
  transform:scaleX(0);transform-origin:center;
  animation:introKicker 1.1s .45s cubic-bezier(.2,.8,.2,1) forwards;}
@keyframes introKicker{ to{transform:scaleX(1)} }

/* Bottom-left control hints, like a cockpit checklist. */
.intro-info{position:absolute;left:28px;bottom:22px;z-index:4;display:flex;gap:16px;
  font-size:10px;letter-spacing:.16em;text-transform:uppercase;
  color:rgba(205,222,255,.42);}
.intro-info-ver{color:rgba(140,210,255,.6);}

/* --- amber recolour: the mark, the badge, the buttons --- */
.intro-title h1{
  background:linear-gradient(180deg,#ffffff 0%,#cfe9ff 46%,#3fc4ff 80%,#3f8bff 100%);
  -webkit-background-clip:text;background-clip:text;color:transparent;
  animation:introGlowAmber 6s ease-in-out infinite alternate;
  text-shadow:0 0 70px rgba(90,180,255,.4);
}
@keyframes introGlowAmber{
  from{ text-shadow:0 0 46px rgba(90,180,255,.30); }
  to{ text-shadow:0 0 96px rgba(70,170,255,.6); }
}
.intro-sub{ color:#bcd9ff; text-shadow:0 0 18px rgba(80,170,255,.4); }
.ir-badge{ color:#04121f; background:linear-gradient(180deg,#9fd6ff,#4fb6ff);
  box-shadow:0 0 22px rgba(80,170,255,.55); }
.ir-name{ color:#9fc6ff; }

.intro-play{
  background:
    linear-gradient(180deg,rgba(255,255,255,.34) 0%,rgba(255,255,255,0) 42%),
    linear-gradient(180deg,#59bfff 0%,#2f8be8 46%,#1a5cc0 100%);
  text-shadow:0 1px 0 rgba(60,20,0,.55), 0 0 24px rgba(120,200,255,.7);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.6),
    inset 0 -2px 0 rgba(90,30,0,.55),
    0 12px 38px rgba(40,130,230,.5),
    0 0 0 1px rgba(120,200,255,.4);
}
.intro-play:hover{
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.75),
    inset 0 -2px 0 rgba(90,30,0,.5),
    0 20px 52px rgba(60,150,255,.65),
    0 0 0 1px rgba(140,210,255,.6),
    0 0 70px rgba(70,160,255,.5);
}
/* Sandbox reads as the hotter, riskier door. */
.intro-play-sandbox{
  background:
    linear-gradient(180deg,rgba(255,255,255,.30) 0%,rgba(255,255,255,0) 42%),
    linear-gradient(180deg,#6f6bff 0%,#5a4ae0 46%,#3a2aa8 100%);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.55),
    inset 0 -2px 0 rgba(70,15,0,.55),
    0 12px 34px rgba(90,70,230,.5),
    0 0 0 1px rgba(150,140,255,.4);
}
.intro-play-sandbox:hover{
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.7),
    inset 0 -2px 0 rgba(70,15,0,.5),
    0 18px 48px rgba(110,90,250,.62),
    0 0 0 1px rgba(170,160,255,.55),
    0 0 60px rgba(110,90,250,.45);
}
.intro-aux{ background:linear-gradient(180deg,rgba(18,30,50,.72),rgba(10,18,34,.72));
  border-color:rgba(110,170,240,.26); color:#bcd9ff; }
.intro-aux:hover{ border-color:rgba(140,200,255,.5); color:#fff; }
.intro-skip{ border-color:rgba(110,170,240,.32); color:#bcd9ff; }

/* ---- title-screen settings panel ---- */
.intro-settings-panel{
  position:relative; width:min(430px,84vw); margin-top:20px; text-align:left;
  padding:16px 18px 18px;
  background:linear-gradient(160deg,rgba(10,18,34,.96),rgba(6,11,22,.92));
  border:1px solid rgba(110,170,240,.24); border-radius:10px;
  box-shadow:0 24px 70px rgba(0,0,0,.65), 0 0 0 1px rgba(80,160,255,.08);
  animation:introDoor .3s cubic-bezier(.2,.8,.2,1);
}
.is-head{ font-size:13px; font-weight:800; letter-spacing:.28em;
  text-transform:uppercase; color:#cfe9ff; padding-bottom:10px;
  border-bottom:1px solid rgba(120,170,240,.16); margin-bottom:12px; }
.is-grp{ margin-bottom:12px; }
.is-label{ font-size:9px; letter-spacing:.22em; text-transform:uppercase;
  color:rgba(150,200,250,.5); margin-bottom:6px; }
.is-row{ display:flex; gap:8px; }
.is-btn{
  flex:1; padding:8px 10px; font-size:11px; font-weight:700; letter-spacing:.08em;
  text-transform:uppercase; cursor:pointer; color:rgba(190,220,255,.7);
  background:linear-gradient(180deg,rgba(255,255,255,.05),rgba(255,255,255,0));
  border:1px solid rgba(110,170,240,.18); border-radius:6px;
  font-family:inherit; transition:all .14s ease;
}
.is-btn:hover{ color:#eaf4ff; border-color:rgba(150,205,255,.5); }
.is-btn.on{
  color:#04121f; background:linear-gradient(180deg,#9fd6ff,#3f8fe8);
  border-color:transparent; box-shadow:0 0 16px rgba(70,160,255,.45);
}
.is-close{
  width:100%; margin-top:4px; padding:9px; font-size:11px; font-weight:700;
  letter-spacing:.22em; text-transform:uppercase; cursor:pointer;
  color:rgba(190,220,255,.75); background:rgba(20,34,56,.7);
  border:1px solid rgba(110,170,240,.22); border-radius:6px; font-family:inherit;
  transition:all .14s ease;
}
.is-close:hover{ color:#fff; border-color:rgba(150,205,255,.5); }

.intro-hide{ display:none !important; }
`;

export interface IntroHooks {
  onPlay(mode: string): void;
  onSkip(): void;
  onAdvance(): void;
  /** Optional: applies a quality preset from the title-screen settings. */
  onSettingsQuality?(name: string): void;
  /** Optional: switches the HUD theme from the title-screen settings. */
  onSettingsHudTheme?(id: string): void;
}

export class IntroOverlay {
  private root: HTMLDivElement;
  private titleCard: HTMLDivElement;
  private patchPanel: HTMLDivElement | null = null;
  private settingsPanel: HTMLDivElement;
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
    // The name reads across, not down. Stacked over three lines it looked
    // like a list of words; on one line with the weight shift between
    // UNLIMITED and POSSIBILITIES it reads as a logotype.
    this.titleCard.innerHTML = `
      <i class="intro-cinema t" aria-hidden="true"></i>
      <i class="intro-cinema b" aria-hidden="true"></i>
      <i class="intro-scan" aria-hidden="true"></i>
      <i class="intro-stars" aria-hidden="true"></i>
      <i class="intro-grid" aria-hidden="true"></i>
      <div class="intro-brand">Unlimited Possibilities <b>Sandbox</b></div>
      <div class="intro-emblem" aria-hidden="true"><span></span></div>
      <p class="intro-sub">A cosmic sandbox</p>
      <h1 class="intro-logo">
        <span class="il-1">UNLIMITED</span>
        <span class="il-2">POSSIBILITIES</span>
        <span class="il-3">SANDBOX</span>
      </h1>
      <i class="intro-kicker" aria-hidden="true"></i>
      <div class="intro-release">
        <span class="ir-badge">${CURRENT_UPDATE}</span>
        <span class="ir-name">${CURRENT_UPDATE_NAME}</span>
      </div>
      <p class="intro-sub">Create · Experiment · Break · Observe</p>
      <div class="intro-info">
        <span>WASD fly · Shift boost · L land · P photomode</span>
        <span class="intro-info-ver">${CURRENT_UPDATE} · ${CURRENT_UPDATE_NAME}</span>
      </div>
    `;
    // Two ways in, not one. Explorer is the universe as a place; Sandbox is
    // the universe as an experiment. Choosing at the title is what stops
    // "sandbox" being a hidden mode nobody finds.
    const modes = document.createElement('div');
    modes.className = 'intro-modes';

    const play = document.createElement('button');
    play.className = 'intro-play';
    play.innerHTML = '<b>🔭 Explore</b><i>Fly anywhere. Fall into a black hole.</i>';
    play.onclick = () => this.hooks.onPlay('explorer');
    modes.appendChild(play);

    const sand = document.createElement('button');
    sand.className = 'intro-play intro-play-sandbox';
    sand.innerHTML = '<b>🌌 Sandbox</b><i>Full physics. Break things.</i>';
    sand.onclick = () => this.hooks.onPlay('sandbox');
    modes.appendChild(sand);

    // Patch notes sits in the same row as the two mode buttons, per the
    // brief that these should read as one horizontal row of choices rather
    // than a vertical stack. It is styled as a secondary action so it does
    // not compete with the two ways into the game.
    // NOT .intro-play. That class means "a way into the game", and there
    // are still exactly two of those - the title must stay two doors, not
    // a menu. Patch notes is a different kind of action and carries its
    // own class, which also keeps it visually subordinate.
    const notes = document.createElement('button');
    notes.className = 'intro-aux intro-play-notes';
    notes.innerHTML = '<b>📖 Patch Notes</b><i>What changed in '
      + CURRENT_UPDATE + '</i>';
    notes.onclick = () => this.togglePatchNotes();
    modes.appendChild(notes);

    // A real settings door: quality and HUD skin, live on the title screen.
    const settings = document.createElement('button');
    settings.className = 'intro-aux intro-settings';
    settings.innerHTML = '<b>⚙ Settings</b><i>Graphics &amp; interface</i>';
    settings.onclick = () => this.toggleSettings();
    modes.appendChild(settings);

    this.titleCard.appendChild(modes);

    // The settings panel, collapsed until asked for.
    this.settingsPanel = document.createElement('div');
    this.settingsPanel.className = 'intro-settings-panel intro-hide';
    this.titleCard.appendChild(this.settingsPanel);

    // The notes panel itself, collapsed until asked for. Built once and
    // shown/hidden rather than rebuilt, so opening it is instant.
    this.patchPanel = document.createElement('div');
    this.patchPanel.className = 'intro-patch intro-hide';
    this.patchPanel.innerHTML = renderPatchNotes();
    this.titleCard.appendChild(this.patchPanel);

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
  /** Shows or hides the patch notes panel. */
  togglePatchNotes(): boolean {
    if (!this.patchPanel) return false;
    const showing = this.patchPanel.classList.toggle('intro-hide');
    return !showing;
  }

  /** Opens (or closes) the title-screen settings panel. */
  toggleSettings(): boolean {
    if (!this.settingsPanel) return false;
    if (this.settingsPanel.classList.contains('intro-hide')) {
      this.buildSettings();
      this.settingsPanel.classList.remove('intro-hide');
      // A settings panel is a different context than the notes; keep only
      // one open at a time.
      this.patchPanel?.classList.add('intro-hide');
      return true;
    }
    this.settingsPanel.classList.add('intro-hide');
    return false;
  }

  /** Builds the settings controls once, wired to the app hooks. */
  private buildSettings(): void {
    const p = this.settingsPanel;
    if (p.childElementCount) return;
    p.innerHTML =
      '<div class="is-head">Settings</div>' +
      '<div class="is-grp">' +
      '<div class="is-label">Quality</div>' +
      '<div class="is-row">' +
      '<button class="is-btn" data-q="low">Low</button>' +
      '<button class="is-btn on" data-q="high">High</button>' +
      '<button class="is-btn" data-q="ultra">Ultra</button>' +
      '</div></div>' +
      '<div class="is-grp">' +
      '<div class="is-label">HUD Skin</div>' +
      '<div class="is-row">' +
      '<button class="is-btn on" data-hud="suit">Exosuit</button>' +
      '<button class="is-btn" data-hud="satellite">Satellite</button>' +
      '<button class="is-btn" data-hud="legacy">Legacy</button>' +
      '</div></div>' +
      '<button class="is-close">Close</button>';
    p.querySelectorAll<HTMLElement>('[data-q]').forEach((b) => {
      b.onclick = () => {
        p.querySelectorAll<HTMLElement>('[data-q]').forEach((x) => x.classList.remove('on'));
        b.classList.add('on');
        this.hooks.onSettingsQuality?.(b.dataset.q ?? 'high');
      };
    });
    p.querySelectorAll<HTMLElement>('[data-hud]').forEach((b) => {
      b.onclick = () => {
        p.querySelectorAll<HTMLElement>('[data-hud]').forEach((x) => x.classList.remove('on'));
        b.classList.add('on');
        this.hooks.onSettingsHudTheme?.(b.dataset.hud ?? 'suit');
      };
    });
    (p.querySelector('.is-close') as HTMLElement).onclick = () =>
      p.classList.add('intro-hide');
  }

  /** True while the notes are on screen. Used by the tests. */
  get patchNotesOpen(): boolean {
    return !!this.patchPanel && !this.patchPanel.classList.contains('intro-hide');
  }

  dispose(): void {
    window.removeEventListener('keydown', this.keyHandler);
    this.root.remove();
  }
}
