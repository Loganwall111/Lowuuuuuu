export const UI_CSS = `
:root{
  --bg:#05070d; --panel:rgba(16,20,30,.82); --panel-solid:#11141d;
  --line:rgba(255,255,255,.10); --line2:rgba(255,255,255,.06);
  --txt:#e8edf7; --dim:#8b95ad; --dim2:#5d6679;
  --acc:#00f0ff; --acc2:#2f6bff; --ok:#3ddb8f; --warn:#ff5a45;
  --r:6px; --r2:4px;
  --shadow:0 14px 38px rgba(0,0,0,.5), 0 2px 6px rgba(0,0,0,.35);
  /* UI density + see-through: the sim must always stay readable behind panels */
  --ui-scale:1;
  --panel-alpha:.80;
  --idle-alpha:.30;
  --panel-dyn:rgba(16,20,30,var(--panel-alpha));
}
/* --- density presets: desktop games use tighter UI than web pages --- */
body[data-density="compact"]{ --r:8px; --r2:6px; --ui-scale:.80; }
body[data-density="tiny"]   { --r:7px; --r2:5px; --ui-scale:.66; }
*{box-sizing:border-box}
html,body{margin:0;padding:0;height:100%;overflow:hidden;background:var(--bg);}
body{font:14px/1.45 'Inter',-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:var(--txt);
  -webkit-font-smoothing:antialiased;}
#renderCanvas{position:fixed;inset:0;width:100%;height:100%;display:block;outline:none;touch-action:none;z-index:0;}

/* ============ window layer: never blocks the canvas ============ */
.wm-layer{position:fixed;inset:0;pointer-events:none;z-index:60;}
.wm-win{
  position:absolute;pointer-events:auto;display:none;flex-direction:column;
  background:var(--panel);backdrop-filter:blur(22px) saturate(140%);
  -webkit-backdrop-filter:blur(22px) saturate(140%);
  border:1px solid color-mix(in srgb,var(--acc) 26%,var(--line));
  border-radius:var(--r);box-shadow:var(--shadow);
  min-width:172px;max-height:calc(100vh - 96px);overflow:hidden;
  /* A panel may never be wide enough to cover the middle of the screen. */
  max-width:min(300px,26vw);
  animation:wmIn .16s cubic-bezier(.2,.8,.3,1);
  font-size:calc(13px * var(--ui-scale));
  background:var(--panel-dyn);
  transition:opacity .35s ease, background .2s ease;
}
/* The neon pylon chamfer: a lit left edge and a glass inner glow, so the
   panels read as cockpit hardware rather than web-page boxes. */
.wm-win::before{content:'';position:absolute;left:0;top:9px;bottom:9px;width:2px;
  background:linear-gradient(180deg,var(--acc),transparent);opacity:.85;pointer-events:none;}
.wm-win::after{content:'';position:absolute;inset:0;pointer-events:none;
  box-shadow:inset 0 0 22px color-mix(in srgb,var(--acc) 7%,transparent);}
/* Panels fade back when you are not using them, so they never hide the sim. */
body[data-idle="1"] .wm-win:not(:hover):not(.wm-pinned){ opacity:var(--idle-alpha); }
body[data-idle="1"] .wm-win:not(:hover):not(.wm-pinned) .wm-body{ pointer-events:none; }
.wm-win:hover{ opacity:1 !important; }
/* Focus mode hides every panel instantly without closing anything. */
body[data-focus="1"] .wm-layer,
body[data-focus="1"] .topbar,
body[data-focus="1"] .wm-dock{ opacity:0; pointer-events:none; }
body[data-focus="1"] .hud{ opacity:.35; }
/* Photomode drops every layer of UI for a clean, screenshot-ready frame. */
body[data-photo="1"] .topbar,
body[data-photo="1"] .wm-layer,
body[data-photo="1"] .hud,
body[data-photo="1"] .fhud,
body[data-photo="1"] .visor-console,
body[data-photo="1"] .sonar-cursor{ display:none !important; }
body[data-cinematic="1"] .visor-console{ display:none !important; }
.wm-win.wm-pinned{ box-shadow:0 0 0 1px var(--acc), var(--shadow); }
.objsearch{display:flex;gap:6px;align-items:center;}
.objsearch input{flex:1;min-width:0;padding:8px 10px;font-size:12px;color:var(--txt);
  background:rgba(10,16,28,.7);border:1px solid color-mix(in srgb,var(--acc) 24%,transparent);
  border-radius:6px;font-family:inherit;letter-spacing:.06em;text-transform:uppercase;}
.objsearch input::placeholder{color:color-mix(in srgb,var(--acc) 45%,transparent);}
.objsearch input:focus{outline:none;border-color:var(--acc);
  box-shadow:0 0 12px color-mix(in srgb,var(--acc) 35%,transparent);}
.objsearch .btn{flex:0 0 auto;min-width:34px;padding:8px;}

.wm-b.on{ background:var(--acc); color:#04121f; }
@keyframes wmIn{from{opacity:0;transform:translateY(-6px) scale(.985)}to{opacity:1;transform:none}}
.wm-win.wm-dragging{transition:none;box-shadow:0 26px 70px rgba(0,0,0,.7);}
.wm-bar{
  display:flex;align-items:center;gap:6px;padding:6px 6px 6px 10px;cursor:grab;
  background:linear-gradient(180deg,rgba(255,255,255,.055),rgba(255,255,255,0));
  border-bottom:1px solid var(--line2);user-select:none;flex:0 0 auto;
}
.wm-bar:active{cursor:grabbing}
.wm-grip{width:3px;height:15px;border-radius:2px;background:linear-gradient(var(--acc),var(--acc2));flex:0 0 auto;}
.wm-title{flex:1;font-size:calc(12px * var(--ui-scale));font-weight:600;letter-spacing:.3px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}
.wm-btns{display:flex;gap:3px;flex:0 0 auto;}
.wm-b{
  width:21px;height:21px;border:0;border-radius:6px;background:rgba(255,255,255,.05);
  color:var(--dim);cursor:pointer;font-size:14px;line-height:1;display:grid;place-items:center;
  transition:background .12s,color .12s;font-family:inherit;
}
.wm-b:hover{background:rgba(255,255,255,.14);color:#fff}
.wm-x:hover{background:#e5484d;color:#fff}
.wm-body{padding:5px 6px;overflow-y:auto;overflow-x:hidden;flex:1 1 auto;min-height:0;}
.wm-body::-webkit-scrollbar{width:9px}
.wm-body::-webkit-scrollbar-thumb{background:rgba(255,255,255,.14);border-radius:9px;border:2px solid transparent;background-clip:content-box}
.wm-resize{position:absolute;right:0;bottom:0;width:16px;height:16px;cursor:nwse-resize;
  background:linear-gradient(135deg,transparent 50%,rgba(255,255,255,.22) 50%);border-radius:0 0 var(--r) 0;}

.wm-dock{position:fixed;left:50%;transform:translateX(-50%);bottom:14px;display:none;gap:8px;z-index:70;pointer-events:auto;}
.wm-dockbtn{
  background:var(--panel);backdrop-filter:blur(18px);border:1px solid var(--line);color:var(--txt);
  padding:7px 14px;border-radius:10px;font-size:12px;cursor:pointer;box-shadow:var(--shadow);font-family:inherit;
}
.wm-dockbtn:hover{border-color:var(--acc);color:#fff}

/* ============ command center ============ */
.topbar{
  position:fixed;inset:0;z-index:80;display:flex;align-items:flex-end;
  justify-content:center;padding:0 8px 12px;pointer-events:none;
  transition:opacity .5s ease;
}
.topbar > *{pointer-events:auto}
/* The command center: every toggle, shrunk and run in a single horizontal
   strip along the very bottom of the visor, framed by two blue lines and
   an outer glow line - a console rail, not a toolbar. */
.cmd-center{
  position:relative;display:flex;flex-direction:column;align-items:center;gap:8px;
  width:min(1120px,96vw);padding:9px 20px 10px;pointer-events:auto;
  background:linear-gradient(180deg,rgba(10,16,28,.68),rgba(6,10,18,.42));
  backdrop-filter:blur(18px);-webkit-backdrop-filter:blur(18px);
  border-top:1px solid color-mix(in srgb,var(--acc) 55%,transparent);
  border-bottom:1px solid color-mix(in srgb,var(--acc) 55%,transparent);
  box-shadow:
    0 18px 50px rgba(0,0,0,.5),
    0 0 0 1px color-mix(in srgb,var(--acc) 18%,transparent);
}
/* A second, softer blue line just outside the two hard rails. */
.cmd-center::before{content:'';position:absolute;left:0;right:0;top:-4px;height:1px;
  background:linear-gradient(90deg,transparent,color-mix(in srgb,var(--acc) 40%,transparent),transparent);
  pointer-events:none;}
.cmd-brand{display:flex;align-items:center;gap:8px;
  padding:3px 12px 3px 8px;background:rgba(14,22,38,.5);
  border:1px solid rgba(140,190,255,.18);border-radius:8px;}
.cmd-brand .brand-name{font-size:10px;letter-spacing:.26em;font-weight:700;}
.cmd-brand .brand-sub{font-size:7.5px;letter-spacing:.2em;text-transform:uppercase;color:var(--dim2);}
.cmd-modes{display:flex;gap:5px;}
.cmd-modes .modesw-b{padding:2px 9px;font-size:11px;border-radius:6px;}
/* The toggle row: one line, every control in reach, centred. */
.cmd-grid{display:flex;flex-wrap:nowrap;gap:5px;justify-content:center;}
.cmd-grid .iconbtn{width:26px;height:26px;font-size:12px;}
/* Corner brackets around the strip, so it reads as machined armour. */
.cmd-ring{display:none;}
.cmd-mark{position:absolute;width:11px;height:11px;pointer-events:none;
  border-color:var(--acc);border-style:solid;}
.cmd-mark.tl{top:-3px;left:-3px;border-width:1.5px 0 0 1.5px;}
.cmd-mark.tr{top:-3px;right:-3px;border-width:1.5px 1.5px 0 0;}
.cmd-mark.bl{bottom:-3px;left:-3px;border-width:0 0 1.5px 1.5px;}
.cmd-mark.br{bottom:-3px;right:-3px;border-width:0 1.5px 1.5px 0;}

.brand{display:flex;align-items:center;gap:9px;padding:6px 15px 6px 11px;
  background:linear-gradient(180deg,rgba(16,24,40,.9),rgba(10,15,26,.9));
  backdrop-filter:blur(20px);border:1px solid rgba(140,190,255,.22);box-shadow:var(--shadow);
  clip-path:polygon(9px 0,100% 0,100% calc(100% - 9px),calc(100% - 9px) 100%,0 100%,0 9px);}
.brand-dot{width:9px;height:9px;border-radius:50%;background:linear-gradient(135deg,var(--acc),var(--acc2));
  box-shadow:0 0 14px var(--acc);}
.brand-name{font-weight:700;font-size:14px;letter-spacing:2.4px;}
.brand-sub{font-size:9.5px;color:var(--dim2);letter-spacing:1.6px;text-transform:uppercase;margin-top:-2px}

.seg{display:flex;background:var(--panel);backdrop-filter:blur(20px);border:1px solid var(--line);
  border-radius:11px;padding:3px;gap:2px;box-shadow:var(--shadow);}
.seg button{
  border:0;background:transparent;color:var(--dim);padding:7px 13px;border-radius:8px;cursor:pointer;
  font-size:12px;font-weight:600;font-family:inherit;letter-spacing:.2px;transition:all .13s;white-space:nowrap;
}
.seg button:hover{color:var(--txt);background:rgba(255,255,255,.06)}
.seg button.on{background:linear-gradient(135deg,var(--acc),var(--acc2));color:#fff;box-shadow:0 3px 12px rgba(77,163,255,.34)}
.spacer{flex:1}

/* Marks a panel section as belonging to Sandbox mode. */
.grp-tag{font-style:normal;font-size:8px;letter-spacing:1.4px;
  text-transform:uppercase;padding:2px 6px;margin-left:7px;vertical-align:1px;
  background:linear-gradient(135deg,#a45cff,#ff7a4d);color:#fff;
  border-radius:2px;font-weight:800;}
/* A section that exists but is not available in this mode. */
.grp-locked{opacity:.92;}

/* ---- Explorer / Sandbox switch ---- */
/* The single most consequential control in the app, so it is a labelled
   switch in the top bar rather than another anonymous icon: which mode you
   are in decides whether the universe can tear your ship apart. */
.modesw{display:flex;align-items:stretch;margin-right:4px;
  border:1px solid rgba(140,190,255,.22);
  background:linear-gradient(180deg,rgba(16,24,40,.86),rgba(10,15,26,.86));
  backdrop-filter:blur(20px);box-shadow:var(--shadow);
  clip-path:polygon(7px 0,100% 0,100% calc(100% - 7px),calc(100% - 7px) 100%,0 100%,0 7px);}
.modesw-b{border:0;background:transparent;color:var(--dim);cursor:pointer;
  font-family:inherit;font-size:11px;font-weight:600;letter-spacing:.6px;
  padding:0 13px;height:34px;white-space:nowrap;
  transition:color .13s, background .13s, box-shadow .13s;}
.modesw-b:hover{color:var(--txt);background:rgba(255,255,255,.06);}
.modesw-b.on{color:#fff;background:linear-gradient(135deg,var(--acc),var(--acc2));
  box-shadow:0 3px 14px rgba(77,163,255,.36);}
/* sandbox reads as the dangerous one */
.modesw-b[data-gm="sandbox"].on{
  background:linear-gradient(135deg,#a45cff,#ff7a4d);
  box-shadow:0 3px 14px rgba(190,90,255,.40);}

.iconbtn{
  width:34px;height:34px;border:1px solid rgba(140,190,255,.20);
  background:linear-gradient(180deg,rgba(16,24,40,.86),rgba(10,15,26,.86));
  backdrop-filter:blur(20px);color:var(--dim);cursor:pointer;font-size:15px;
  display:grid;place-items:center;box-shadow:var(--shadow);font-family:inherit;position:relative;
  clip-path:polygon(6px 0,100% 0,100% calc(100% - 6px),calc(100% - 6px) 100%,0 100%,0 6px);
  transition:color .13s, border-color .13s, box-shadow .13s, transform .13s;
}
.iconbtn:hover{color:#fff;border-color:var(--acc);transform:translateY(-1px);
  box-shadow:0 0 0 1px color-mix(in srgb,var(--acc) 34%,transparent),
             0 5px 18px color-mix(in srgb,var(--acc) 30%,transparent);}
.iconbtn.on{color:#fff;background:linear-gradient(135deg,var(--acc),var(--acc2));border-color:transparent;
  box-shadow:0 4px 18px color-mix(in srgb,var(--acc) 48%,transparent);}

/* ============ HUD ============ */
.hud{
  position:fixed;left:14px;bottom:14px;z-index:55;pointer-events:none;
  display:flex;gap:8px;align-items:flex-end;
}
.hud-chip{
  position:relative;
  background:linear-gradient(180deg,rgba(16,24,40,.88),rgba(9,14,24,.88));
  backdrop-filter:blur(20px);border:1px solid rgba(140,190,255,.20);
  padding:7px 13px;box-shadow:var(--shadow);min-width:74px;
  clip-path:polygon(8px 0,100% 0,100% calc(100% - 8px),calc(100% - 8px) 100%,0 100%,0 8px);
}
/* accent edge marking live telemetry */
.hud-chip::before{content:'';position:absolute;left:0;top:6px;bottom:6px;width:2px;
  background:linear-gradient(180deg,var(--acc),transparent);opacity:.9}
.hud-flight .hud-v{color:#dff0ff;text-shadow:0 0 12px color-mix(in srgb,var(--acc) 55%,transparent)}
.hud-k{font-size:9px;color:var(--dim2);letter-spacing:1.3px;text-transform:uppercase;}
.hud-v{font-size:16px;font-weight:700;font-variant-numeric:tabular-nums;line-height:1.15}
.hud-v small{font-size:10px;color:var(--dim);font-weight:500}

/* ============ flight HUD ============ */
/* Angled, thin-ruled and monospaced: instrument panel, not web page. The
   canvas must stay readable through all of it, so nothing here is opaque
   and nothing takes pointer events. */
.fhud{position:fixed;inset:0;z-index:54;pointer-events:none;
  font-family:'JetBrains Mono',ui-monospace,'SF Mono',Menlo,monospace;}
/* The canopy glass: a curved-window tint and a faint phosphor scanline
   raster, blended over the scene so the whole cockpit reads as one lit
   surface. Pure overlay - no pointer events, never opaque. */
.fhud-canopy{position:absolute;inset:0;pointer-events:none;overflow:hidden;
  mix-blend-mode:screen;
  background:
    radial-gradient(130% 100% at 50% 42%,
      rgba(24,46,88,0) 0%, rgba(10,20,44,.10) 62%, rgba(4,10,26,.30) 100%),
    linear-gradient(180deg,
      rgba(96,156,255,.05) 0%, rgba(96,156,255,0) 34%,
      rgba(0,0,0,0) 66%, rgba(140,90,255,.05) 100%);
  box-shadow:inset 0 0 180px rgba(30,60,120,.22);}
.fhud-canopy::after{content:'';position:absolute;inset:0;
  background:repeating-linear-gradient(180deg,
    rgba(150,200,255,.028) 0px, rgba(150,200,255,.028) 1px,
    transparent 1px, transparent 4px);
  animation:fhCanopy 9s linear infinite;}
@keyframes fhCanopy{
  from{transform:translateY(0);}
  to{transform:translateY(4px);}}
/* Warp rips the canopy brighter - the glass itself reacts to the drive. */
.fhud.warping .fhud-canopy{
  box-shadow:inset 0 0 220px rgba(90,120,255,.30),
             inset 0 -40px 120px rgba(150,110,255,.16);}
/* Opening a traversable fold reroutes violet energy through the helmet
   perimeter, making the mechanic legible without adding another box. */
.fhud.portal-open .fv-side,.fhud.portal-open .fv-pylon{
  filter:hue-rotate(58deg) brightness(1.35);
  animation:fhPortalRail 1.25s ease-in-out infinite alternate;}
.fhud.portal-crossworld .fv-top,.fhud.portal-crossworld .fv-bottom{
  background:linear-gradient(90deg,transparent,#7657ff,#00f0ff,#7657ff,transparent);}
.fhud.portal-open .fh-arc{stroke:#9d7cff;filter:drop-shadow(0 0 4px #7657ff);}
@keyframes fhPortalRail{from{opacity:.55}to{opacity:1;box-shadow:0 0 24px rgba(118,87,255,.8)}}

/* ---- holographic instrument look ---- */
/* Panels are glass plates lit from the edge, like a projected interface.
   The accent edge brightens and the text carries a faint chromatic fringe. */
.fh-block{
  box-shadow:
    0 8px 26px rgba(0,0,0,.46),
    0 0 0 1px rgba(110,175,255,.10),
    inset 0 0 22px rgba(80,150,255,.06);
}
.fh-block::before{
  content:'';position:absolute;left:1px;right:1px;top:0;height:1px;
  background:linear-gradient(90deg,
    color-mix(in srgb,var(--acc) 65%,transparent),transparent 70%);
  opacity:.8;pointer-events:none;
}
.fh-big{background:linear-gradient(180deg,#f2f8ff,#b9d4ff 70%,#7fb2f5);
  -webkit-background-clip:text;background-clip:text;color:transparent;}
.fh-accent{background:linear-gradient(90deg,#cfe8ff,#6fd0ff 45%,#9fd0ff);
  -webkit-background-clip:text;background-clip:text;color:transparent;
  filter:drop-shadow(0 0 8px rgba(90,190,255,.45));}
.fh-tgt{color:#eaf4ff;text-shadow:0 0 12px rgba(120,190,255,.28);}
/* A faint holographic flicker across every panel, like a projected feed. */
.fh-block::after{animation:fhFlicker 7s linear infinite;}
@keyframes fhFlicker{
  0%,100%{opacity:.85} 8%{opacity:.5} 9%{opacity:.85}
  46%{opacity:.8} 48%{opacity:.45} 50%{opacity:.85}}
/* The reticle reads as a targeting computer: a slowly rotating outer ring. */
.fhud-reticle svg{animation:fhRetSpin 24s linear infinite;}
@keyframes fhRetSpin{to{transform:rotate(360deg);}}

/* ---- bottom telemetry ticker ---- */
.fhud-status{position:absolute;left:50%;bottom:12px;transform:translateX(-50%);
  display:flex;align-items:center;gap:14px;max-width:min(92vw,860px);
  padding:5px 16px 6px;font-size:9px;letter-spacing:.22em;text-transform:uppercase;
  color:rgba(170,205,245,.82);
  background:linear-gradient(180deg,rgba(8,13,24,.62),rgba(6,10,18,.42));
  backdrop-filter:blur(10px);-webkit-backdrop-filter:blur(10px);
  border:1px solid rgba(120,180,255,.14);
  clip-path:polygon(9px 0,100% 0,100% calc(100% - 9px),calc(100% - 9px) 100%,0 100%,0 9px);}
.fh-st-seg{white-space:nowrap;}
.fh-st-seg:first-child{color:#7fd0ff;text-shadow:0 0 10px rgba(90,180,255,.5);}
.fh-st-right{margin-left:auto;font-weight:700;color:#cfe6ff;}
/* A little centre notch above the ticker, like a HUD alignment mark. */
.fhud-status::before{content:'';position:absolute;left:50%;top:-3px;
  width:22px;height:3px;transform:translateX(-50%);
  background:linear-gradient(90deg,transparent,var(--acc),transparent);}

/* ---- flight director: pitch ladder + heading tape ---- */
/* The centre instruments. Drawn faint so the scene stays readable through
   them, but always present - a pilot's eye never has to leave the middle of
   the screen to know which way the ship points. */
.fhud-director{position:absolute;left:50%;top:50%;width:0;height:0;pointer-events:none;}
.fh-hdg-tape{position:absolute;left:50%;top:-64px;width:620px;height:20px;
  transform:translateX(-50%);overflow:hidden;
  -webkit-mask-image:linear-gradient(90deg,transparent,#000 12%,#000 88%,transparent);
  mask-image:linear-gradient(90deg,transparent,#000 12%,#000 88%,transparent);}
.fh-hdg-inner{position:absolute;top:0;left:0;height:20px;width:0;
  will-change:transform;transition:transform .08s linear;}
.fh-hdg-tick{position:absolute;top:0;width:0;height:14px;
  border-left:1px solid rgba(90,190,255,.30);}
.fh-hdg-tick.lbl{border-left-color:rgba(120,205,255,.55);height:16px;}
.fh-hdg-tick i{position:absolute;left:4px;top:1px;font-style:normal;
  font-size:8px;letter-spacing:.06em;color:rgba(150,215,255,.66);white-space:nowrap;}
.fh-pitch{position:absolute;left:50%;top:50%;width:150px;height:220px;
  transform:translate(-50%,-50%);overflow:hidden;
  -webkit-mask-image:linear-gradient(180deg,transparent,#000 14%,#000 86%,transparent);
  mask-image:linear-gradient(180deg,transparent,#000 14%,#000 86%,transparent);}
.fh-pitch-inner{position:absolute;top:0;left:0;width:150px;height:0;
  will-change:transform;transition:transform .08s linear;}
.fh-ladder-line{position:absolute;left:0;width:150px;height:1px;
  background:linear-gradient(90deg,transparent,rgba(90,190,255,.34) 24%,
    rgba(90,190,255,.34) 76%,transparent);}
.fh-ladder-line span{position:absolute;right:2px;top:-4px;font-size:8px;
  color:rgba(120,205,255,.5);letter-spacing:.04em;}
.fh-ladder-line.zero{height:2px;
  background:linear-gradient(90deg,transparent,var(--acc) 12%,var(--acc) 88%,transparent);
  box-shadow:0 0 10px color-mix(in srgb,var(--acc) 55%,transparent);}
.fh-horizon{position:absolute;left:50%;top:50%;width:190px;height:2px;
  transform:translate(-50%,-50%);
  background:linear-gradient(90deg,transparent,rgba(90,200,255,.4),transparent);}
.fh-fd{position:absolute;left:50%;top:50%;width:0;height:0;
  transform:translate(-50%,-50%);
  border-left:7px solid transparent;border-right:7px solid transparent;
  border-bottom:10px solid rgba(90,200,255,.65);filter:drop-shadow(0 0 5px rgba(70,180,255,.4));}

/* ---- exosuit (powered armor) framing ---- */
/* The helmet visor: an angular mask at the screen edges that reads as
   looking out through an armored faceplate. Pure overlay, pointer-events
   none, never occludes the scene - it frames it. */
.fhud-suit .fhud-visor{display:block;position:absolute;inset:0;pointer-events:none;
  background:
    linear-gradient(90deg,
      rgba(8,10,16,.55) 0%, rgba(8,10,16,0) 7%,
      rgba(8,10,16,0) 93%, rgba(8,10,16,.55) 100%),
    linear-gradient(180deg,
      rgba(8,10,16,.6) 0%, rgba(8,10,16,0) 9%,
      rgba(8,10,16,0) 91%, rgba(8,10,16,.6) 100%);}
.fv-top,.fv-bottom{position:absolute;left:50%;transform:translateX(-50%);
  width:min(70vw,860px);height:3px;
  background:linear-gradient(90deg,transparent,var(--acc),transparent);
  box-shadow:0 0 14px color-mix(in srgb,var(--acc) 55%,transparent);}
.fv-top{top:16px;}.fv-bottom{bottom:16px;}
.fv-side{position:absolute;top:50%;transform:translateY(-50%);
  width:3px;height:min(46vh,420px);
  background:linear-gradient(180deg,transparent,var(--acc),transparent);
  box-shadow:0 0 14px color-mix(in srgb,var(--acc) 55%,transparent);}
.fv-side.l{left:16px;}.fv-side.r{right:16px;}
.fv-pylon{position:absolute;top:50%;width:14px;height:120px;transform:translateY(-50%);
  border:1px solid rgba(80,180,255,.22);
  background:linear-gradient(180deg,rgba(80,170,255,.08),rgba(60,140,255,.02));}
.fv-pylon.l{left:6px;clip-path:polygon(0 0,100% 8%,100% 92%,0 100%);}
.fv-pylon.r{right:6px;clip-path:polygon(0 8%,100% 0,100% 100%,0 92%);}

/* The suit vitals: a reactor core ring on the left, armor + life support
   stacked under it. Only the exosuit theme shows them. */
.fhud-suit .fhud-vitals{display:flex;position:absolute;left:92px;top:50%;
  transform:translateY(-50%);flex-direction:column;gap:16px;align-items:center;}
.fh-reactor{position:relative;width:78px;height:78px;border-radius:50%;
  display:grid;place-items:center;
  background:rgba(10,14,22,.55);border:1px solid rgba(80,180,255,.24);
  box-shadow:0 0 30px rgba(60,150,255,.12), inset 0 0 18px rgba(0,0,0,.5);}
.fh-reactor i{position:absolute;inset:7px;border-radius:50%;
  background:conic-gradient(var(--acc) 0deg, rgba(255,255,255,.06) 0deg);
  -webkit-mask:radial-gradient(circle,transparent 58%,#000 60%);
  mask:radial-gradient(circle,transparent 58%,#000 60%);}
.fh-reactor::after{content:'';position:absolute;width:10px;height:10px;border-radius:50%;
  background:var(--acc);box-shadow:0 0 12px var(--acc);}
.fh-reactor span{position:absolute;bottom:-16px;font-size:8px;letter-spacing:.28em;
  color:rgba(140,205,255,.66);text-transform:uppercase;}
.fh-suit-block{width:150px;padding:8px 12px 9px;
  background:linear-gradient(135deg,rgba(14,16,24,.72),rgba(8,10,16,.55));
  backdrop-filter:blur(12px);-webkit-backdrop-filter:blur(12px);
  border:1px solid rgba(80,180,255,.18);
  clip-path:polygon(10px 0,100% 0,100% calc(100% - 10px),calc(100% - 10px) 100%,0 100%,0 10px);}
.fh-bar-armor i{background:linear-gradient(90deg,#4fb6ff,#bfe4ff);}
.fh-suit-rows{display:flex;gap:10px;margin-top:7px;justify-content:space-between;
  font-size:8.5px;letter-spacing:.1em;color:rgba(140,205,255,.55);text-transform:uppercase;}
.fh-suit-rows b{color:#cfe9ff;font-weight:700;letter-spacing:.02em;}
/* The suit still wears the satellite corner brackets, in the amber accent. */
.fhud-suit .fh-corner{border-color:color-mix(in srgb,var(--acc) 45%,transparent);}
.fhud-suit .fh-scan{background:linear-gradient(180deg,transparent,color-mix(in srgb,var(--acc) 12%,transparent),transparent);}

/* ---- the scrolling hex diagnostic matrix ---- */
/* Two vertical machine streams down the visor edges: the suit's own data,
   present only in the exosuit theme and never occluding the view. */
.fhud-hex{display:none;}
.fhud-suit .fhud-hex{display:block;position:absolute;inset:0;pointer-events:none;
  overflow:hidden;}
.fh-hex{position:absolute;top:0;bottom:0;width:13px;overflow:hidden;
  font-style:normal;font-size:8px;line-height:1.9;letter-spacing:0;
  font-family:'JetBrains Mono',ui-monospace,Menlo,monospace;
  color:color-mix(in srgb,var(--acc) 55%,transparent);
  text-shadow:0 0 6px color-mix(in srgb,var(--acc) 45%,transparent);
  writing-mode:vertical-rl;white-space:nowrap;
  -webkit-mask-image:linear-gradient(180deg,transparent,#000 14%,#000 86%,transparent);
  mask-image:linear-gradient(180deg,transparent,#000 14%,#000 86%,transparent);}
.fh-hex.l{left:20px;animation:fhHexScroll 22s linear infinite;}
.fh-hex.r{right:20px;animation:fhHexScroll 29s linear infinite reverse;}
@keyframes fhHexScroll{
  from{transform:translateY(0);}
  to{transform:translateY(-50%);}}

.fhud-left{position:absolute;left:92px;bottom:88px;display:flex;
  flex-direction:column;gap:10px;align-items:flex-start;}
.fhud-right{position:absolute;right:92px;bottom:88px;display:flex;
  flex-direction:column;gap:10px;align-items:stretch;min-width:212px;}

.fh-block{position:relative;padding:8px 14px 9px;
  background:linear-gradient(135deg,rgba(10,16,28,.72),rgba(6,10,18,.58));
  backdrop-filter:blur(14px) saturate(130%);
  -webkit-backdrop-filter:blur(14px) saturate(130%);
  border:1px solid rgba(120,175,255,.16);
  clip-path:polygon(10px 0,100% 0,100% calc(100% - 10px),calc(100% - 10px) 100%,0 100%,0 10px);
  box-shadow:0 8px 26px rgba(0,0,0,.42);}
/* live-telemetry edge */
.fh-block::after{content:'';position:absolute;left:0;top:9px;bottom:9px;width:2px;
  background:linear-gradient(180deg,var(--acc),transparent);opacity:.85;}

.fh-label{font-size:8.5px;letter-spacing:2px;text-transform:uppercase;
  color:var(--dim2);margin-bottom:3px;}
.fh-label b{color:var(--acc);font-weight:700;letter-spacing:1px;}
.fh-coords{display:grid;grid-template-columns:auto auto;gap:1px 8px;align-items:baseline;}
.fh-ax{font-size:9px;color:var(--dim2);letter-spacing:1px;}
.fh-num{font-size:14px;font-weight:600;color:#cfe4ff;font-variant-numeric:tabular-nums;}
.fh-row{display:flex;align-items:baseline;gap:9px;}
.fh-big{font-size:21px;font-weight:700;line-height:1.1;color:#e6f1ff;
  font-variant-numeric:tabular-nums;letter-spacing:.5px;}
.fh-accent{color:#dff0ff;text-shadow:0 0 16px color-mix(in srgb,var(--acc) 60%,transparent);}
.fh-cmp{font-size:12px;font-weight:700;color:var(--acc);letter-spacing:1.5px;}
.fh-sub{font-size:9.5px;color:var(--dim);letter-spacing:.8px;}
.fh-sub b{color:#b9cde8;font-weight:600;}
.fh-tgt{font-size:13px;font-weight:600;color:#e2ecfa;letter-spacing:.4px;}

.fh-bar{height:3px;margin-top:5px;background:rgba(140,180,240,.13);overflow:hidden;}
.fh-bar i{display:block;height:100%;background:linear-gradient(90deg,var(--acc),#9fd0ff);
  box-shadow:0 0 10px color-mix(in srgb,var(--acc) 70%,transparent);
  transition:width .09s linear;}
.fh-bar-warp i{background:linear-gradient(90deg,var(--acc2),#c9b3ff);
  box-shadow:0 0 12px color-mix(in srgb,var(--acc2) 75%,transparent);}
/* at warp the whole panel picks up the drive colour */
.fhud.warping .fh-warp{border-color:color-mix(in srgb,var(--acc2) 55%,transparent);}
.fhud.warping .fh-warp::after{background:linear-gradient(180deg,var(--acc2),transparent);}

/* ---- descent: only on screen while falling through a horizon ---- */
/* Centred at the top rather than in the corners: while you are inside a
   black hole this is the only instrument that matters, and the corners are
   where the eye is not. */
/* Pushed below the gear row + its shift notice, which now own the top
   centre; a descent readout overlapping the shifter would hide both. */
.fhud-descent{position:absolute;left:50%;top:122px;transform:translateX(-50%);
  min-width:340px;text-align:center;display:none;}
.fhud-descent.on{display:block;}
.fh-desc-phase{font-size:12px;letter-spacing:3px;text-transform:uppercase;
  color:#ffd9a8;text-shadow:0 0 18px rgba(255,170,80,.55);font-weight:700;}
.fh-desc-sub{font-size:10px;letter-spacing:1.2px;color:var(--dim);margin-top:3px;}
/* The neon horizon warning, telemetry-style, while inside a hole. */
.fh-desc-warn{font-size:8.5px;letter-spacing:.14em;color:var(--acc);
  text-shadow:0 0 10px color-mix(in srgb,var(--acc) 60%,transparent);
  margin-top:5px;text-transform:uppercase;}
.fh-desc-warn:empty{display:none;}
.fh-desc-bar{display:none;height:4px;margin-top:7px;background:rgba(255,190,120,.14);
  overflow:hidden;}
.fh-desc-bar i{display:block;height:100%;
  background:linear-gradient(90deg,#4fb6ff,#bfe4ff);
  box-shadow:0 0 14px rgba(255,150,60,.75);transition:width .12s linear;}
/* the way back, shown as its own closing aperture */
.fh-desc-exit{font-size:9.5px;letter-spacing:1px;color:#8fa8ce;margin-top:5px;}
.fh-desc-exit b{color:#cfe4ff;font-weight:600;}
/* the singularity warning takes the panel over when the dot is in reach */
.fhud-descent.singular .fh-desc-phase{color:#fff;text-shadow:0 0 26px #fff;}
.fhud-descent.singular .fh-desc-bar i{background:linear-gradient(90deg,#fff,#dfefff);}

.fhud-reticle{position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);
  opacity:.5;}
.fh-ring{fill:none;stroke:rgba(150,200,255,.34);stroke-width:1;}
.fh-dot{fill:var(--acc);}
.fh-tick{stroke:rgba(160,205,255,.55);stroke-width:1.4;}
.fh-arc{fill:none;stroke:color-mix(in srgb,var(--acc) 60%,transparent);stroke-width:1.2;
  stroke-dasharray:3 6;}
/* ================= sonar cursor =================
   A tracking reticle instead of an arrow. Built from borders and
   transforms, no image files. */
body.sonar-on,body.sonar-on *{cursor:none !important;}
/* Except real text entry, where losing the caret is genuinely disorienting. */
body.sonar-on input[type="text"],body.sonar-on input[type="number"],
body.sonar-on textarea{cursor:text !important;}

.sonar-cursor{position:fixed;left:0;top:0;width:34px;height:34px;
  pointer-events:none;z-index:100000;will-change:transform;}
.sonar-cursor span{position:absolute;left:50%;top:50%;
  transform:translate(-50%,-50%);}
.sc-ring{width:18px;height:18px;border-radius:50%;
  border:1px solid color-mix(in srgb,var(--acc) 90%,transparent);
  box-shadow:0 0 10px color-mix(in srgb,var(--acc) 55%,transparent),
             0 0 0 1px color-mix(in srgb,var(--acc) 22%,transparent);}
/* The core is a diamond, not a dot - the lock-on look. */
.sc-dot{width:5px;height:5px;border-radius:0;background:var(--acc);
  transform:translate(-50%,-50%) rotate(45deg);
  box-shadow:0 0 7px var(--acc),0 0 14px color-mix(in srgb,var(--acc) 60%,transparent);}
/* The sweep: a conic wedge rotating inside the ring, like a radar trace. */
.sc-sweep{width:18px;height:18px;border-radius:50%;
  background:conic-gradient(from 0deg,
    color-mix(in srgb,var(--acc) 50%,transparent) 0deg,
    transparent 54deg,transparent 360deg);
  animation:scSweep 2.2s linear infinite;opacity:.85;}
@keyframes scSweep{to{transform:translate(-50%,-50%) rotate(360deg);}}
/* Four tracking ticks outside the ring, with tiny corner heads. */
.sc-tick{width:1px;height:5px;background:color-mix(in srgb,var(--acc) 90%,transparent);
  box-shadow:0 0 5px color-mix(in srgb,var(--acc) 60%,transparent);}
.sc-tick.n{transform:translate(-50%,-50%) translateY(-13px);}
.sc-tick.s{transform:translate(-50%,-50%) translateY(13px);}
.sc-tick.w{width:5px;height:1px;transform:translate(-50%,-50%) translateX(-13px);}
.sc-tick.e{width:5px;height:1px;transform:translate(-50%,-50%) translateX(13px);}
/* Click ping: expands and fades once, then waits for the next click. */
.sc-ping{width:18px;height:18px;border-radius:50%;
  border:1px solid var(--acc);opacity:0;}
.sc-ping.go{animation:scPing .5s ease-out 1;}
@keyframes scPing{
  0%{opacity:.9;transform:translate(-50%,-50%) scale(.55);}
  100%{opacity:0;transform:translate(-50%,-50%) scale(2.6);}
}
/* State variants. Colour carries the meaning, geometry stays constant so
   the cursor never appears to jump size as you move over things. */
.sonar-cursor[data-state="target"] .sc-ring{border-color:var(--warn);
  box-shadow:0 0 10px color-mix(in srgb,var(--warn) 55%,transparent);}
.sonar-cursor[data-state="target"] .sc-dot{background:var(--warn);}
.sonar-cursor[data-state="grab"] .sc-ring{border-color:var(--ok);}
.sonar-cursor[data-state="grab"] .sc-dot{background:var(--ok);}
/* Zoom: the ring opens up, which is the one case where a size change is
   the actual information being conveyed. */
.sonar-cursor[data-state="zoom"] .sc-ring{width:26px;height:26px;
  border-style:dashed;}

@media (prefers-reduced-motion: reduce){
  .sc-sweep{animation:none;opacity:.4;}
}

/* ================= satellite HUD theme =================
   The conceit: you are watching a downlink from a tracking satellite, not
   looking through a canopy. Everything here is frame furniture around the
   edges of the feed - it must never encroach on the middle of the screen,
   which is the picture itself. */

/* Frame + uplink are in the DOM for both themes; the theme class decides
   whether they render, so switching is a class swap not a rebuild. */
.fhud-frame,.fhud-uplink,.fhud-visor,.fhud-vitals{display:none;}
.fhud-satellite .fhud-frame,.fhud-suit .fhud-frame{display:block;position:absolute;inset:0;
  pointer-events:none;}
.fhud-satellite .fhud-uplink{display:flex;}

/* Corner brackets: the classic "this is a tracked feed" tell. Drawn with
   borders rather than images, per the no-image-files rule. */
.fh-corner{position:absolute;width:26px;height:26px;
  border-color:color-mix(in srgb,var(--acc) 46%,transparent);border-style:solid;
  border-width:0;opacity:.75;}
.fh-corner.tl{left:14px;top:14px;border-left-width:2px;border-top-width:2px;
  border-top-left-radius:4px;}
.fh-corner.tr{right:14px;top:14px;border-right-width:2px;border-top-width:2px;
  border-top-right-radius:4px;}
.fh-corner.bl{left:14px;bottom:14px;border-left-width:2px;border-bottom-width:2px;
  border-bottom-left-radius:4px;}
.fh-corner.br{right:14px;bottom:14px;border-right-width:2px;border-bottom-width:2px;
  border-bottom-right-radius:4px;}

/* The scan sweep. A slow vertical pass, the one piece of ambient motion
   the satellite theme allows - it reads as a sensor refreshing. */
.fh-scan{position:absolute;left:14px;right:14px;height:78px;top:0;
  background:linear-gradient(180deg,transparent,
    color-mix(in srgb,var(--acc) 9%,transparent) 55%,transparent);
  opacity:.5;animation:fhSweep 7.5s linear infinite;}
@keyframes fhSweep{
  0%{transform:translateY(-80px);opacity:0;}
  8%{opacity:.5;}
  92%{opacity:.5;}
  100%{transform:translateY(100vh);opacity:0;}
}

/* Uplink strip: identifier, signal bars, tracking state. Top-left, tucked
   under the corner bracket so it reads as part of the frame. */
.fhud-uplink{position:absolute;left:52px;top:16px;align-items:center;gap:9px;
  padding:4px 11px 5px;pointer-events:none;
  font-family:'JetBrains Mono',ui-monospace,monospace;
  font-size:9.5px;letter-spacing:.15em;text-transform:uppercase;
  color:rgba(175,205,240,.8);
  background:linear-gradient(135deg,rgba(9,15,26,.72),rgba(5,9,16,.55));
  backdrop-filter:blur(12px) saturate(130%);
  border:1px solid var(--line);border-radius:999px;}
.fh-up-dot{width:6px;height:6px;border-radius:50%;background:var(--ok);
  box-shadow:0 0 9px var(--ok);}
.fh-up-id{color:rgba(200,225,255,.92);font-weight:600;}
.fh-up-sig{color:var(--acc);letter-spacing:.05em;}
.fh-up-mode{color:rgba(160,190,225,.6);}

/* In the satellite theme the instrument blocks get a harder, more
   technical edge than the rounded legacy chips. */
.fhud-satellite .fh-block{border-radius:3px;
  border-left:2px solid color-mix(in srgb,var(--acc) 50%,transparent);}
.fhud-satellite .fh-label{color:rgba(150,190,235,.72);}

/* Legacy is the original look: no frame, no sweep, no uplink. */
.fhud-legacy .fhud-frame,.fhud-legacy .fhud-uplink{display:none;}

/* ---- velocity gearbox, top centre ---- */
/* Sits above the descent readout and clear of the reticle, because it is
   the one instrument you look at while deciding where to point the ship. */
.fhud-gears{position:absolute;left:50%;top:14px;transform:translateX(-50%);
  display:flex;flex-direction:column;align-items:center;gap:5px;
  padding:7px 10px 8px;pointer-events:auto;
  background:linear-gradient(135deg,rgba(10,16,28,.74),rgba(6,10,18,.60));
  backdrop-filter:blur(16px) saturate(135%);
  border:1px solid var(--line);border-radius:11px;
  box-shadow:0 8px 30px rgba(0,0,0,.5);}
.fh-gear-label{font-size:8.5px;letter-spacing:.20em;text-transform:uppercase;
  color:rgba(170,200,235,.62);}
.fh-gear-set{display:flex;align-items:stretch;gap:0;}
.fh-gear-sep{width:1px;margin:5px 5px;background:var(--line2);}
.fh-gear{display:flex;flex-direction:column;align-items:center;gap:1px;
  min-width:82px;padding:5px 11px 6px;cursor:pointer;
  background:transparent;border:1px solid transparent;border-radius:7px;
  font-family:inherit;color:rgba(190,212,240,.60);
  transition:background .13s ease,color .13s ease,border-color .13s ease;}
.fh-gear:hover{background:rgba(120,170,255,.10);color:rgba(220,235,255,.9);}
.fh-gear-key{font-size:8px;letter-spacing:.14em;opacity:.55;}
.fh-gear-name{font-size:11.5px;font-weight:600;letter-spacing:.11em;}
.fh-gear-mul{font-size:8.5px;letter-spacing:.06em;opacity:.62;}
/* The engaged gear is the only lit thing in the row, so the current
   velocity regime is readable in peripheral vision. */
.fh-gear.on{color:#eaf4ff;
  background:linear-gradient(180deg,
    color-mix(in srgb,var(--acc) 26%,transparent),
    color-mix(in srgb,var(--acc) 9%,transparent));
  border-color:color-mix(in srgb,var(--acc) 52%,transparent);
  box-shadow:0 0 16px color-mix(in srgb,var(--acc) 28%,transparent),
    inset 0 1px 0 rgba(255,255,255,.13);}
.fh-gear.on .fh-gear-key,.fh-gear.on .fh-gear-mul{opacity:.85;}
.fh-gear.on .fh-gear-name{text-shadow:0 0 12px color-mix(in srgb,var(--acc) 70%,transparent);}
/* shift telemetry: green, brief, directly under the shifter */
.fhud-notice{position:absolute;left:50%;top:76px;transform:translateX(-50%) translateY(-5px);
  padding:4px 12px 5px;white-space:nowrap;pointer-events:none;
  font-family:'JetBrains Mono',ui-monospace,monospace;
  font-size:10.5px;letter-spacing:.09em;text-transform:uppercase;
  color:var(--ok);text-shadow:0 0 14px color-mix(in srgb,var(--ok) 60%,transparent);
  background:linear-gradient(135deg,rgba(8,20,14,.80),rgba(5,12,9,.62));
  border:1px solid color-mix(in srgb,var(--ok) 34%,transparent);border-radius:7px;
  opacity:0;transition:opacity .16s ease,transform .16s ease;}
.fhud-notice.on{opacity:1;transform:translateX(-50%) translateY(0);}

/* Macro-sector anomaly sensor: a narrow holographic blade projected from
   the visor crown. It only exists when the procedural field is in range. */
.fhud-anomaly{position:absolute;left:50%;top:112px;width:min(360px,54vw);
  transform:translateX(-50%) perspective(500px) rotateX(-8deg) scaleY(.2);
  padding:8px 18px 9px;text-align:center;opacity:0;
  background:linear-gradient(90deg,transparent,rgba(10,20,48,.72) 18%,rgba(18,12,54,.76) 82%,transparent);
  border-top:1px solid rgba(0,240,255,.28);border-bottom:1px solid rgba(118,87,255,.3);
  clip-path:polygon(8% 0,92% 0,100% 50%,92% 100%,8% 100%,0 50%);
  transition:opacity .4s ease,transform .55s cubic-bezier(.2,1.4,.3,1);}
.fhud-anomaly.on{opacity:.86;transform:translateX(-50%) perspective(500px) rotateX(0) scaleY(1);
  animation:fhQaGlitch 7s steps(1) infinite;}
.fhud-anomaly.visual{opacity:1;filter:drop-shadow(0 0 14px rgba(118,87,255,.52));}
.fhud.descending .fhud-anomaly{opacity:0;transform:translateX(-50%) scaleY(.2);}
.fhud-anomaly span,.fhud-anomaly b,.fhud-anomaly small{display:block;text-transform:uppercase;}
.fh-qa-k{font-size:7px;letter-spacing:.34em;color:rgba(130,210,255,.52);}
.fhud-anomaly b{font-size:12px;letter-spacing:.24em;color:#e9e4ff;margin:3px 0;
  text-shadow:-1px 0 #7657ff,1px 0 #00f0ff,0 0 16px rgba(118,87,255,.7);}
#fhQaId{font-size:7px;letter-spacing:.18em;color:rgba(180,205,255,.48);}
.fh-qa-wave{height:2px;margin:6px auto 4px;width:82%;background:rgba(255,255,255,.08);overflow:hidden;}
.fh-qa-wave i{display:block;height:100%;background:linear-gradient(90deg,#00f0ff,#7657ff,#fff);
  box-shadow:0 0 10px #7657ff;transition:width .25s ease;}
.fhud-anomaly small{font-size:7.5px;letter-spacing:.12em;color:#a9c8ec;}
@keyframes fhQaGlitch{0%,93%,100%{translate:0 0}94%{translate:2px 0}95%{translate:-1px 0}}

/* focus mode dims instruments but never hides them */
body[data-focus="1"] .fhud{opacity:.42;}

/* ============ controls ============ */
.grp{margin-bottom:16px}
.grp:last-child{margin-bottom:2px}
.grp-h{font-size:calc(9px * var(--ui-scale));letter-spacing:1.7px;text-transform:uppercase;
  color:#8fb6e8;margin:0 0 8px;display:flex;align-items:center;gap:7px;font-weight:700}
.grp-h::before{content:'';width:2px;height:10px;background:var(--acc);
  box-shadow:0 0 8px var(--acc);flex:none}
.grp-h::after{content:'';flex:1;height:1px;
  background:linear-gradient(90deg,color-mix(in srgb,var(--acc) 40%,transparent),transparent)}

/* ------------------------------ sliders ------------------------------
   Instrument panel rather than a web form: each control is a recessed
   cell with a machined track, a tick rule and a numeric readout that
   looks like a gauge. */
/* Instrument rows, not stacked cards.
   Label, value and track share one line, which roughly halves the height of
   every control and lets a panel show twice as many without growing. */
.ctl{
  display:grid;
  grid-template-columns:minmax(58px, 1fr) auto;
  grid-template-areas:'label value' 'track track';
  align-items:center;
  column-gap:6px; row-gap:1px;
  margin:0 0 3px;padding:3px 6px 4px;border-radius:var(--r2);
  background:linear-gradient(180deg,rgba(255,255,255,.045),rgba(255,255,255,.014));
  border:1px solid rgba(255,255,255,.07);
  box-shadow:inset 0 1px 0 rgba(255,255,255,.05);
  transition:border-color .14s, background .14s;}
/* Wide panels put the track inline for a true single-line instrument. */
@media (min-width:0px){
  .wm-win[data-wide="1"] .ctl{
    grid-template-columns:minmax(56px,88px) 1fr auto;
    grid-template-areas:'label track value';
    row-gap:0;}
}
.ctl:hover{border-color:rgba(120,180,255,.30);
  background:linear-gradient(180deg,rgba(120,180,255,.07),rgba(255,255,255,.02));}
.ctl:focus-within{border-color:var(--acc);
  box-shadow:inset 0 1px 0 rgba(255,255,255,.06),
             0 0 0 1px color-mix(in srgb,var(--acc) 32%,transparent);}

/* ctl-top is now a passthrough: its children are placed by the grid above. */
.ctl-top{display:contents}
.ctl-l{grid-area:label;font-size:calc(9.5px * var(--ui-scale));color:var(--txt);font-weight:600;
  letter-spacing:.3px;text-transform:uppercase;opacity:.86;
  white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}
.ctl-v{
  grid-area:value;
  font-size:calc(9.5px * var(--ui-scale));color:var(--acc);
  font-variant-numeric:tabular-nums;font-weight:700;letter-spacing:.2px;
  background:linear-gradient(180deg,rgba(77,163,255,.20),rgba(77,163,255,.07));
  border:1px solid rgba(77,163,255,.30);
  padding:0 5px;border-radius:4px;white-space:nowrap;
  min-width:40px;text-align:right;
  text-shadow:0 0 10px color-mix(in srgb,var(--acc) 55%,transparent);}

/* Tick rule under the track: reads as a calibrated instrument. */
.ctl-track{grid-area:track;position:relative;padding-top:0}
.ctl-track::after{
  content:'';position:absolute;left:2px;right:2px;bottom:1px;height:2px;
  pointer-events:none;opacity:.30;
  background:repeating-linear-gradient(90deg,
    rgba(255,255,255,.55) 0 1px, transparent 1px 10%);}

input[type=range]{
  -webkit-appearance:none;appearance:none;width:100%;height:13px;
  background:transparent;cursor:pointer;margin:0;display:block;position:relative;z-index:1;}

/* --- WebKit ---
   Squared off rather than rounded. Rounded pill sliders read as a web form;
   instrument panels use hard edges, a machined vertical thumb and a lit
   track, which is what makes the UI feel like hardware. */
input[type=range]::-webkit-slider-runnable-track{
  height:5px;border-radius:1px;
  background:
    linear-gradient(90deg,
      color-mix(in srgb,var(--acc) 92%,#fff) 0%,
      var(--acc) var(--pct,50%),
      rgba(255,255,255,.07) var(--pct,50%)),
    repeating-linear-gradient(90deg,
      rgba(255,255,255,.16) 0 1px, transparent 1px 12.5%);
  box-shadow:inset 0 1px 2px rgba(0,0,0,.6),
             0 0 14px color-mix(in srgb,var(--acc) 30%,transparent);}
input[type=range]::-webkit-slider-thumb{
  -webkit-appearance:none;width:7px;height:16px;border-radius:1px;
  margin-top:-5.5px;cursor:grab;
  background:linear-gradient(180deg,#ffffff 0%,#cfe2fb 42%,#7ea8dc 100%);
  border:1px solid color-mix(in srgb,var(--acc) 85%,#fff);
  box-shadow:0 1px 6px rgba(0,0,0,.75),
             0 0 12px color-mix(in srgb,var(--acc) 75%,transparent),
             inset 0 0 0 1px rgba(255,255,255,.4);
  transition:transform .11s ease, box-shadow .11s ease;}
input[type=range]::-webkit-slider-thumb:hover{transform:scaleY(1.18);
  box-shadow:0 1px 7px rgba(0,0,0,.8),0 0 18px color-mix(in srgb,var(--acc) 95%,transparent);}
input[type=range]:active::-webkit-slider-thumb{transform:scaleY(1.05);cursor:grabbing}

/* --- Firefox --- */
input[type=range]::-moz-range-track{height:5px;border-radius:1px;
  background:rgba(255,255,255,.07);box-shadow:inset 0 1px 2px rgba(0,0,0,.6);}
input[type=range]::-moz-range-progress{height:5px;border-radius:1px;background:var(--acc);
  box-shadow:0 0 14px color-mix(in srgb,var(--acc) 45%,transparent);}
input[type=range]::-moz-range-thumb{width:7px;height:16px;border-radius:1px;
  background:linear-gradient(180deg,#ffffff 0%,#cfe2fb 42%,#7ea8dc 100%);
  border:1px solid var(--acc);cursor:grab;
  box-shadow:0 1px 6px rgba(0,0,0,.75);}

.btnrow{display:flex;flex-wrap:wrap;gap:7px}
/* Angled corner + sweep highlight: reads as hardware, not a web page. */
.btn{
  position:relative;overflow:hidden;
  flex:1 1 auto;min-width:80px;padding:8px 12px;
  border:1px solid rgba(140,190,255,.20);color:var(--txt);cursor:pointer;
  font-size:calc(11px * var(--ui-scale));font-weight:600;font-family:inherit;
  letter-spacing:.5px;text-transform:uppercase;text-align:center;
  background:linear-gradient(180deg,rgba(120,175,255,.10),rgba(255,255,255,.028));
  clip-path:polygon(7px 0,100% 0,100% calc(100% - 7px),calc(100% - 7px) 100%,0 100%,0 7px);
  transition:background .14s, border-color .14s, color .14s, box-shadow .14s;
}
.btn::after{
  /* light sweep on hover */
  content:'';position:absolute;inset:0;pointer-events:none;
  background:linear-gradient(115deg,transparent 30%,rgba(180,220,255,.30) 50%,transparent 70%);
  transform:translateX(-130%);transition:transform .5s ease;}
.btn:hover{
  color:#fff;border-color:var(--acc);
  background:linear-gradient(180deg,rgba(120,175,255,.24),rgba(120,175,255,.07));
  box-shadow:0 0 0 1px color-mix(in srgb,var(--acc) 30%,transparent),
             0 4px 18px color-mix(in srgb,var(--acc) 26%,transparent);}
.btn:hover::after{transform:translateX(130%)}
.btn:active{background:linear-gradient(180deg,rgba(120,175,255,.34),rgba(120,175,255,.12))}
.btn.pri{
  background:linear-gradient(135deg,var(--acc),var(--acc2));border-color:transparent;color:#fff;
  box-shadow:0 4px 20px color-mix(in srgb,var(--acc) 45%,transparent);}
.btn.dgr:hover{background:linear-gradient(180deg,#e5484d,#a3282c);border-color:#e5484d;color:#fff;
  box-shadow:0 4px 18px rgba(229,72,77,.4)}

.stat{display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid var(--line2);font-size:12px}
.stat:last-child{border-bottom:0}
.stat-k{color:var(--dim)}
.stat-v{font-variant-numeric:tabular-nums;font-weight:600}

/* Search field: how you reach any control now that tiers are gone. */
.searchrow{position:relative;display:flex;align-items:center;margin:2px 0 8px;}
.searchin{
  width:100%;padding:6px 26px 6px 26px;border-radius:var(--r2);
  border:1px solid var(--line);background:rgba(8,12,22,.72);color:var(--fg);
  font:inherit;font-size:calc(11.5px * var(--ui-scale));outline:none;
  -webkit-appearance:none;appearance:none;}
.searchin::placeholder{color:var(--dim);opacity:.85}
.searchin:focus{border-color:var(--accent);
  box-shadow:0 0 0 2px color-mix(in srgb,var(--accent) 26%,transparent);}
.searchrow::before{
  content:'⌕';position:absolute;left:8px;color:var(--dim);
  font-size:calc(13px * var(--ui-scale));pointer-events:none;line-height:1;}
.searchx{
  position:absolute;right:5px;width:17px;height:17px;padding:0;line-height:1;
  border:0;border-radius:5px;cursor:pointer;
  background:rgba(255,255,255,.10);color:var(--fg);font-size:13px;}
.searchx:hover{background:var(--danger,#c0392b);color:#fff}

.numin{width:100%;margin-top:5px;padding:5px 8px;border-radius:7px;border:1px solid var(--line);
  background:rgba(0,0,0,.34);color:var(--txt);font:inherit;font-size:11px;font-variant-numeric:tabular-nums;}
.numin:focus{outline:none;border-color:var(--acc);}
.search{width:100%;padding:9px 12px;border-radius:9px;border:1px solid var(--line);
  background:rgba(0,0,0,.30);color:var(--txt);font-size:12.5px;font-family:inherit;margin-bottom:11px;outline:none}
.search:focus{border-color:var(--acc);box-shadow:0 0 0 3px rgba(77,163,255,.14)}
.search::placeholder{color:var(--dim2)}

.cards{display:grid;grid-template-columns:1fr 1fr;gap:9px}
.card{
  padding:13px 11px;border-radius:11px;border:1px solid var(--line);background:rgba(255,255,255,.035);
  cursor:pointer;transition:all .14s;text-align:left;color:var(--txt);font-family:inherit;position:relative;overflow:hidden;
}
.card:hover{background:rgba(255,255,255,.09);border-color:var(--acc);transform:translateY(-2px);
  box-shadow:0 10px 26px rgba(0,0,0,.4)}
.card.on{border-color:var(--acc);background:linear-gradient(135deg,rgba(77,163,255,.17),rgba(124,92,255,.10))}
.card-g{font-size:21px;margin-bottom:6px;display:block;line-height:1}
.card-t{font-size:12.5px;font-weight:700;margin-bottom:2px}
.card-d{font-size:10.5px;color:var(--dim);line-height:1.35}

.fav{position:absolute;top:7px;right:8px;font-size:12px;opacity:.28;cursor:pointer;transition:opacity .12s;
  background:none;border:0;padding:2px;color:var(--warn)}
.fav:hover{opacity:.8}
.fav.on{opacity:1}

.tabs{display:flex;gap:2px;margin-bottom:12px;background:rgba(0,0,0,.26);padding:3px;border-radius:9px}
.tab{flex:1;padding:6px 8px;border:0;border-radius:7px;background:transparent;color:var(--dim);
  cursor:pointer;font-size:11px;font-weight:600;font-family:inherit;transition:all .12s}
.tab:hover{color:var(--txt)}
.tab.on{background:rgba(255,255,255,.10);color:#fff}

.note{font-size:11px;color:var(--dim);line-height:1.55;padding:9px 11px;border-radius:9px;
  background:rgba(77,163,255,.07);border-left:2px solid var(--acc);margin-bottom:12px}

.graph{width:100%;height:52px;display:block;border-radius:8px;background:rgba(0,0,0,.3);margin-top:4px}

.kbd{display:inline-block;padding:1px 6px;border-radius:5px;background:rgba(255,255,255,.10);
  border:1px solid var(--line);font-size:10px;font-family:ui-monospace,Menlo,monospace;color:var(--txt)}

.badge{font-size:9px;padding:2px 6px;border-radius:5px;background:rgba(124,92,255,.22);
  color:#c3b4ff;letter-spacing:.6px;font-weight:700;text-transform:uppercase}

/* ============ boot / loading ============ */
.boot{position:fixed;inset:0;z-index:200;display:grid;place-items:center;
  background:radial-gradient(ellipse at 50% 40%,rgba(10,22,44,.9),rgba(3,7,16,.98));
  transition:opacity .45s ease;}
.boot.gone{opacity:0;pointer-events:none}
.boot-in{text-align:center}
/* The mech's own boot: a glowing suit helmet with a rotating scan line. */
.boot-helm{position:relative;width:64px;height:64px;margin:0 auto 18px;
  border-radius:50%;border:1.5px solid color-mix(in srgb,var(--acc) 55%,transparent);
  box-shadow:0 0 26px color-mix(in srgb,var(--acc) 35%,transparent),
    inset 0 0 18px color-mix(in srgb,var(--acc) 20%,transparent);}
.boot-helm i{position:absolute;inset:7px;border-radius:50%;
  border:1px dashed color-mix(in srgb,var(--acc) 45%,transparent);
  animation:cmdSpin 12s linear infinite;}
.boot-helm span{position:absolute;left:50%;top:6px;width:2px;height:52px;
  transform:translateX(-50%);background:linear-gradient(180deg,var(--acc),transparent);
  animation:cmdSpin 3.4s linear infinite;}
.boot-name{font-size:26px;font-weight:800;letter-spacing:9px;
  background:linear-gradient(135deg,var(--acc),var(--acc2));-webkit-background-clip:text;
  background-clip:text;-webkit-text-fill-color:transparent;margin-bottom:8px}
.boot-sub{font-size:10.5px;color:var(--dim2);letter-spacing:3.5px;text-transform:uppercase;margin-bottom:22px}
/* The staged boot lines: each lights up as its threshold is crossed. */
.boot-seq{display:flex;flex-direction:column;gap:5px;margin-bottom:18px;
  font-family:'JetBrains Mono',ui-monospace,Menlo,monospace;}
.boot-line{font-size:10px;letter-spacing:.3em;text-transform:uppercase;
  color:rgba(90,120,160,.35);transition:color .3s ease, text-shadow .3s ease;}
.boot-line.on{color:var(--acc);text-shadow:0 0 10px color-mix(in srgb,var(--acc) 55%,transparent);}
.boot-bar{width:230px;height:2.5px;border-radius:3px;background:rgba(255,255,255,.10);overflow:hidden;margin:0 auto}
.boot-fill{height:100%;width:0;border-radius:3px;background:linear-gradient(90deg,var(--acc),var(--acc2));
  transition:width .3s ease}
.boot-cal{margin-top:16px;font-size:12px;font-weight:800;letter-spacing:.4em;text-transform:uppercase;
  color:transparent;opacity:0;transform:scale(.94);
  transition:opacity .4s ease, transform .4s cubic-bezier(.2,.9,.3,1.4);
  background:linear-gradient(180deg,#fff,var(--acc));-webkit-background-clip:text;background-clip:text;}
.boot-cal.on{opacity:1;transform:scale(1);}
.boot-msg{font-size:10.5px;color:var(--dim);margin-top:12px;height:14px;letter-spacing:.5px}
.boot-err{font-size:11.5px;color:#ffb4b4;max-width:460px;margin:14px auto 0;line-height:1.6;
  background:rgba(229,72,77,.10);border:1px solid rgba(229,72,77,.35);border-radius:10px;padding:11px 14px;
  font-family:ui-monospace,Menlo,monospace;text-align:left;word-break:break-word}
/* Non-blocking status message, bottom-left, never over the middle. */
.ui-toast{position:fixed;left:14px;bottom:14px;z-index:120;padding:9px 14px;border-radius:10px;
  background:rgba(12,16,24,.92);border:1px solid var(--line);color:var(--txt);font-size:12px;
  pointer-events:none;opacity:0;transform:translateY(8px);transition:opacity .2s,transform .2s;
  max-width:min(420px,44vw);backdrop-filter:blur(14px);}
.ui-toast.show{opacity:1;transform:none;}
.fatal-toast{position:fixed;right:16px;bottom:16px;z-index:300;max-width:420px;padding:13px 15px;
  border-radius:12px;background:var(--panel);backdrop-filter:blur(20px);border:1px solid rgba(229,72,77,.5);
  box-shadow:var(--shadow);color:var(--txt);font-size:12px;line-height:1.5}
.fatal-toast b{color:#ff8a8a;display:block;margin-bottom:5px}
.fatal-toast button{margin-top:10px;padding:6px 12px;border-radius:8px;border:1px solid var(--line);
  background:rgba(255,255,255,.06);color:var(--txt);cursor:pointer;font-family:inherit;font-size:11px}
.fatal-toast button:hover{background:rgba(255,255,255,.14)}

@media (max-width:820px){
  .cards{grid-template-columns:1fr}
  .brand-sub{display:none}
  .seg button{padding:7px 9px;font-size:11px}
}


/* =====================================================================
   SPACE-ENGINE INSTRUMENT LAYER
   ---------------------------------------------------------------------
   A professional simulator UI is not a set of boxes with rounded corners.
   Three things carry the look, and each is done here with no extra markup
   so nothing in the app has to change:

     1. Cut corners, not radii. Angled clips read as machined panels.
     2. Corner brackets. Drawn with a single conic-gradient border image,
        so they cost nothing and cannot desynchronise from the panel size.
     3. A faint scanline wash, which is what stops a flat translucent panel
        from looking like a web modal.
   ===================================================================== */

/* --- windows: cut corners + engraved bracket --- */
.wm-win{
  border-radius:0;
  clip-path:polygon(
    12px 0, 100% 0,
    100% calc(100% - 12px), calc(100% - 12px) 100%,
    0 100%, 0 12px);
  border:1px solid rgba(125,180,255,.20);
  background:
    linear-gradient(180deg, rgba(18,26,44,.88), rgba(8,12,22,.82));
}
/* the bright hairline along the top edge: catches the eye like lit metal */
.wm-win::before{
  content:'';position:absolute;left:12px;right:0;top:0;height:1px;
  background:linear-gradient(90deg,
    transparent, color-mix(in srgb,var(--acc) 85%,transparent) 22%,
    color-mix(in srgb,var(--acc) 85%,transparent) 78%, transparent);
  opacity:.75;pointer-events:none;z-index:2;}
/* scanline wash */
.wm-win .wm-body{
  background-image:repeating-linear-gradient(180deg,
    rgba(140,190,255,.028) 0 1px, transparent 1px 3px);
}

.wm-bar{
  background:linear-gradient(180deg,rgba(90,150,230,.16),rgba(255,255,255,0));
  border-bottom:1px solid rgba(125,180,255,.16);
}
.wm-title{
  text-transform:uppercase;letter-spacing:1.4px;
  font-family:'JetBrains Mono',ui-monospace,'SF Mono',Menlo,monospace;
  font-size:calc(10.5px * var(--ui-scale));
  color:#cfe2ff;
}
.wm-grip{
  width:2px;height:13px;border-radius:0;
  background:linear-gradient(180deg,var(--acc),var(--acc2));
  box-shadow:0 0 8px color-mix(in srgb,var(--acc) 80%,transparent);}

/* --- section headings inside panels --- */
.wm-body h4, .sec-title{
  font-family:'JetBrains Mono',ui-monospace,monospace;
  font-size:9px;letter-spacing:2.2px;text-transform:uppercase;
  color:var(--dim2);
  display:flex;align-items:center;gap:8px;margin:12px 0 7px;}
.wm-body h4::after, .sec-title::after{
  content:'';flex:1;height:1px;
  background:linear-gradient(90deg,rgba(125,180,255,.28),transparent);}

/* --- buttons: angled, with a lit leading edge --- */
.btn, .wm-b{
  border-radius:0;
  clip-path:polygon(5px 0,100% 0,100% calc(100% - 5px),calc(100% - 5px) 100%,0 100%,0 5px);
  font-family:'JetBrains Mono',ui-monospace,monospace;
  letter-spacing:.9px;text-transform:uppercase;font-size:10px;
}
.btn.on, .wm-b.on{
  box-shadow:inset 0 0 0 1px color-mix(in srgb,var(--acc) 70%,transparent),
             0 0 14px color-mix(in srgb,var(--acc) 45%,transparent);}

/* --- HUD: heavier, with corner brackets on every instrument block --- */
.fh-block{
  border:1px solid rgba(120,175,255,.20);
  background:
    linear-gradient(135deg,rgba(10,18,34,.80),rgba(5,9,17,.66));
}
/* bracket ticks at the two square corners */
.fh-block::before{
  content:'';position:absolute;right:0;top:0;width:9px;height:9px;
  border-top:1px solid color-mix(in srgb,var(--acc) 70%,transparent);
  border-right:1px solid color-mix(in srgb,var(--acc) 70%,transparent);
  pointer-events:none;}
.fh-label{
  font-size:8px;letter-spacing:2.4px;color:#6f8ab0;}
.fh-num, .fh-big{
  text-shadow:0 0 12px color-mix(in srgb,var(--acc) 35%,transparent);}
/* Coordinates read like an instrument: fixed width, so digits never jitter */
.fh-coords{grid-template-columns:auto minmax(72px,auto);}
.fh-num{letter-spacing:.6px;}

/* the reticle gets a slow breathing pulse so the frame never feels frozen */
@keyframes fhPulse{0%,100%{opacity:.44}50%{opacity:.60}}
.fhud-reticle{animation:fhPulse 4.5s ease-in-out infinite;}

/* ---- the cockpit visor console ---- */
/* The world's live parameters, melted into the armour itself: a frosted
   glass row of pylon sliders across the top edge, and Reset/Pause as
   physical nodes along the bottom. Overlay only - never replaces panels. */
.visor-console{position:fixed;inset:0;z-index:56;pointer-events:none;}
.visor-console.hidden{display:none;}
.vc-top{position:absolute;left:50%;top:10px;transform:translateX(-50%);
  display:flex;gap:14px;align-items:flex-end;
  padding:8px 18px 9px;
  background:linear-gradient(180deg,rgba(10,16,28,.66),rgba(6,10,18,.40));
  backdrop-filter:blur(15px);-webkit-backdrop-filter:blur(15px);
  border:1px solid color-mix(in srgb,var(--acc) 22%,transparent);
  border-radius:10px;pointer-events:auto;
  clip-path:polygon(12px 0,100% 0,100% calc(100% - 12px),calc(100% - 12px) 100%,0 100%,0 12px);}
.vc-ctl{display:flex;flex-direction:column;align-items:center;gap:3px;min-width:86px;}
.vc-l{font-size:8px;letter-spacing:.14em;text-transform:uppercase;
  color:color-mix(in srgb,var(--acc) 70%,transparent);}
.vc-v{font-size:9px;color:rgba(210,235,255,.8);font-variant-numeric:tabular-nums;}
.vc-ctl input[type="range"]{width:84px;height:2px;appearance:none;background:
  linear-gradient(90deg,var(--acc) var(--pct,50%),rgba(255,255,255,.10) var(--pct,50%));
  border-radius:2px;outline:none;cursor:pointer;}
.vc-ctl input[type="range"]::-webkit-slider-thumb{appearance:none;width:10px;height:10px;
  border-radius:50%;background:var(--acc);box-shadow:0 0 8px var(--acc);cursor:pointer;}
.vc-bottom{position:absolute;left:50%;bottom:16px;transform:translateX(-50%);
  display:flex;gap:8px;pointer-events:auto;}
.vc-node{padding:7px 16px;font-size:10px;font-weight:700;letter-spacing:.16em;
  text-transform:uppercase;color:#cfe9ff;cursor:pointer;font-family:inherit;
  background:linear-gradient(180deg,rgba(14,22,36,.62),rgba(8,12,22,.42));
  backdrop-filter:blur(15px);-webkit-backdrop-filter:blur(15px);
  border:1px solid color-mix(in srgb,var(--acc) 30%,transparent);border-radius:8px;
  transition:all .13s ease;}
.vc-node:hover{color:#fff;border-color:var(--acc);
  box-shadow:0 0 16px color-mix(in srgb,var(--acc) 45%,transparent);}

/* Visor integration pass: controls sit directly on the cyan perimeter rails,
   not inside webpage panels. Top owns orbital/time controls, side pylons own
   world parameters, and the lower brace owns reset/pause. */
.vc-top{top:15px;padding:0 42px 3px;gap:22px;background:transparent;
  backdrop-filter:none;-webkit-backdrop-filter:none;border:0;border-bottom:1px solid #00f0ff;
  border-radius:0;clip-path:none;box-shadow:0 5px 14px rgba(0,240,255,.12);}
.vc-rail-cap{position:absolute;left:50%;top:11px;transform:translateX(-50%);
  font-size:7px;letter-spacing:.3em;color:rgba(0,240,255,.42);white-space:nowrap;}
.vc-top .vc-ctl{transform:translateY(16px);padding:2px 8px 4px;
  background:linear-gradient(180deg,rgba(2,12,24,.78),rgba(2,12,24,.18));}
.vc-side{position:absolute;top:50%;transform:translateY(-50%);display:flex;
  flex-direction:column;gap:30px;pointer-events:auto;}
.vc-side::before{content:'';position:absolute;top:-42px;bottom:-42px;width:1px;
  background:linear-gradient(transparent,#00f0ff 18%,#00f0ff 82%,transparent);
  box-shadow:0 0 11px rgba(0,240,255,.55);}
.vc-left{left:11px}.vc-right{right:11px}
.vc-left::before{left:0}.vc-right::before{right:0}
.vc-side .vc-ctl{min-width:80px;padding:5px 7px;background:rgba(2,12,24,.28);}
.vc-left .vc-ctl{align-items:flex-start}.vc-right .vc-ctl{align-items:flex-end}
.vc-ctl input[type="range"]{height:1px;background:linear-gradient(90deg,#00f0ff 0 50%,rgba(0,240,255,.16) 50%);
  box-shadow:0 0 5px rgba(0,240,255,.34);}
.vc-ctl input[type="range"]::-webkit-slider-thumb{width:7px;height:13px;border-radius:0;
  clip-path:polygon(50% 0,100% 30%,100% 70%,50% 100%,0 70%,0 30%);background:#d8fcff;}
.vc-bottom{bottom:15px;padding:0 54px;border-top:1px solid #00f0ff;
  box-shadow:0 -5px 14px rgba(0,240,255,.12);}
.vc-node{transform:translateY(-13px);padding:5px 13px;background:rgba(2,12,24,.45);
  backdrop-filter:none;-webkit-backdrop-filter:none;border:0;border-bottom:1px solid rgba(0,240,255,.45);
  border-radius:0;clip-path:polygon(7px 0,100% 0,100% calc(100% - 7px),calc(100% - 7px) 100%,0 100%,0 7px);}
@media(max-width:760px){.vc-side{display:none}.vc-top{max-width:86vw}.vc-top .vc-ctl:nth-of-type(n+3){display:none}}

/* ---- the in-game pause menu (Escape) ---- */
.pause-menu{position:fixed;inset:0;z-index:200;display:none;place-items:center;
  background:radial-gradient(ellipse at center,rgba(4,8,16,.55),rgba(2,4,10,.82));
  pointer-events:none;}
.pause-menu.on{display:grid;pointer-events:auto;}
.pause-panel{width:min(420px,88vw);max-height:84vh;overflow-y:auto;
  padding:22px 24px 24px;
  background:linear-gradient(165deg,rgba(12,20,38,.94),rgba(6,11,22,.9));
  backdrop-filter:blur(18px);-webkit-backdrop-filter:blur(18px);
  border:1px solid color-mix(in srgb,var(--acc) 26%,transparent);border-radius:14px;
  box-shadow:0 26px 80px rgba(0,0,0,.7), 0 0 0 1px color-mix(in srgb,var(--acc) 10%,transparent);
  animation:wmIn .16s cubic-bezier(.2,.8,.3,1);}
.pause-head{font-size:15px;font-weight:800;letter-spacing:.4em;text-transform:uppercase;
  text-align:center;color:var(--acc);text-shadow:0 0 18px color-mix(in srgb,var(--acc) 55%,transparent);
  margin-bottom:16px;padding-bottom:12px;border-bottom:1px solid color-mix(in srgb,var(--acc) 18%,transparent);}
.pause-row{display:flex;gap:8px;margin-bottom:10px;flex-wrap:wrap;}
.pause-btn{flex:1;min-width:0;padding:9px 10px;font-size:11px;font-weight:700;
  letter-spacing:.1em;text-transform:uppercase;color:rgba(200,225,255,.8);cursor:pointer;
  background:linear-gradient(180deg,rgba(255,255,255,.05),rgba(255,255,255,0));
  border:1px solid color-mix(in srgb,var(--acc) 22%,transparent);border-radius:8px;font-family:inherit;
  transition:all .13s ease;}
.pause-btn:hover{color:#fff;border-color:var(--acc);
  box-shadow:0 0 14px color-mix(in srgb,var(--acc) 35%,transparent);}
.pause-btn.pri{background:linear-gradient(180deg,#00cfff 0%,#1f8be8 100%);
  color:#04121f;border-color:transparent;}
.pause-btn.on{background:color-mix(in srgb,var(--acc) 22%,transparent);color:#fff;}
.pause-body{margin-top:6px;}
.pause-grp{margin-bottom:14px;}
.pause-label{font-size:9px;letter-spacing:.22em;text-transform:uppercase;
  color:color-mix(in srgb,var(--acc) 70%,transparent);margin-bottom:7px;}
.pause-grp input[type="range"]{width:100%;height:3px;appearance:none;
  background:linear-gradient(90deg,var(--acc) 50%,rgba(255,255,255,.10) 50%);border-radius:2px;}
.pause-grp input[type="range"]::-webkit-slider-thumb{appearance:none;width:12px;height:12px;
  border-radius:50%;background:var(--acc);box-shadow:0 0 8px var(--acc);cursor:pointer;}
.pause-stat{display:flex;justify-content:space-between;gap:10px;padding:5px 2px;
  font-size:11px;color:rgba(190,215,245,.75);border-bottom:1px solid rgba(120,170,240,.08);}
.pause-stat b{color:#eaf4ff;font-variant-numeric:tabular-nums;}
.pause-stat.you{color:var(--acc);}
.pause-stat.you b{color:var(--acc);text-shadow:0 0 8px color-mix(in srgb,var(--acc) 55%,transparent);}
.pause-note{font-size:10px;color:var(--dim);line-height:1.5;padding:8px 10px;
  border-left:2px solid var(--acc);background:color-mix(in srgb,var(--acc) 6%,transparent);border-radius:6px;}

/* ---- dedicated five-stage LoadingScreenManager scene ---- */
.omni-boot{position:fixed;inset:0;z-index:9999;overflow:hidden;pointer-events:auto;
  --boot-cyan:#00f0ff;--boot-violet:#7657ff;
  background:
    radial-gradient(ellipse at 50% 46%,rgba(10,32,70,.84),rgba(2,7,18,.97) 68%),
    #030713;color:#dff8ff;opacity:1;transition:opacity .56s ease;
  font-family:'JetBrains Mono',ui-monospace,Menlo,monospace;isolation:isolate;}
.omni-boot.fading{opacity:0;pointer-events:none;}
.omni-canvas,.omni-frost{position:absolute;inset:0;width:100%;height:100%;}
.omni-canvas{z-index:2;filter:drop-shadow(0 0 6px rgba(0,240,255,.48));}
.omni-frost{z-index:3;mix-blend-mode:screen;opacity:.9;}
.omni-warp{position:absolute;inset:-18%;z-index:1;overflow:hidden;
  background:
    radial-gradient(circle at 50% 50%,rgba(180,230,255,.22) 0 1%,transparent 18%),
    repeating-conic-gradient(from 0deg at 50% 50%,rgba(0,240,255,.055) 0deg .7deg,transparent .8deg 5deg);
  transform:scale(1.35);filter:blur(22px) saturate(170%);
  animation:omniDefrost 6.15s cubic-bezier(.17,.72,.12,1) forwards;}
.omni-warp i{position:absolute;inset:0;background:
  repeating-radial-gradient(circle at center,transparent 0 20px,rgba(115,83,255,.055) 22px 23px);
  animation:omniWarpPulse .22s linear infinite;}
@keyframes omniDefrost{
  0%,25%{filter:blur(26px) saturate(190%);transform:scale(1.5);opacity:.62}
  55%{filter:blur(12px) saturate(170%);transform:scale(1.18);opacity:.92}
  100%{filter:blur(0) saturate(115%);transform:scale(1);opacity:.22}}
@keyframes omniWarpPulse{to{transform:scale(1.028);opacity:.5}}
.omni-grid{position:absolute;inset:0;z-index:0;opacity:.22;
  background-image:linear-gradient(rgba(0,240,255,.1) 1px,transparent 1px),
    linear-gradient(90deg,rgba(0,240,255,.1) 1px,transparent 1px);
  background-size:46px 46px;transform:perspective(560px) rotateX(58deg);
  transform-origin:bottom;mask-image:linear-gradient(transparent,#000 76%);
  animation:omniGrid 1.5s linear infinite;}
@keyframes omniGrid{to{background-position:0 46px}}
.omni-vignette{position:absolute;inset:0;z-index:5;pointer-events:none;
  box-shadow:inset 0 0 180px rgba(0,0,0,.92),inset 0 0 0 1px rgba(0,240,255,.1);
  background:repeating-linear-gradient(180deg,transparent 0 3px,rgba(0,0,0,.09) 3px 4px);}
.omni-chrome{position:absolute;z-index:8;left:25px;right:25px;top:20px;
  display:flex;justify-content:space-between;padding:7px 11px;
  border-top:1px solid rgba(0,240,255,.38);font-size:8px;letter-spacing:.3em;
  color:rgba(155,220,255,.6);text-transform:uppercase;}
.omni-chrome::before,.omni-chrome::after{content:'';position:absolute;top:-1px;width:54px;height:6px;
  border-top:2px solid var(--boot-cyan);filter:drop-shadow(0 0 5px var(--boot-cyan));}
.omni-chrome::before{left:0;border-left:1px solid var(--boot-cyan)}
.omni-chrome::after{right:0;border-right:1px solid var(--boot-cyan)}
.omni-stage{position:absolute;inset:0;z-index:7;display:grid;place-items:center;pointer-events:none;}
.omni-matrix{display:flex;flex-direction:column;align-items:center;gap:8px;
  transition:opacity .3s ease,transform .45s cubic-bezier(.2,.8,.2,1);}
.omni-matrix small{font-size:8px;letter-spacing:.34em;color:rgba(170,215,255,.45);margin-bottom:9px;}
.omni-line{font-size:clamp(9px,1vw,13px);letter-spacing:.36em;text-transform:uppercase;
  color:var(--boot-cyan);text-shadow:0 0 10px rgba(0,240,255,.75);}
.omni-line.dim{font-size:9px;color:rgba(150,205,255,.58);letter-spacing:.25em;}
.omni-line.accent{color:#bbaeff;text-shadow:0 0 16px rgba(118,87,255,.9);}
.omni-count{font-size:clamp(24px,4vw,48px);font-weight:200;letter-spacing:.5em;
  color:#f2fbff;text-shadow:0 0 24px rgba(0,240,255,.62);}
.omni-bar{position:relative;width:min(520px,72vw);height:4px;margin-top:10px;
  background:rgba(120,170,220,.12);border-left:1px solid var(--boot-cyan);
  border-right:1px solid var(--boot-violet);box-shadow:0 0 0 1px rgba(0,240,255,.08);}
.omni-bar i{display:block;height:100%;width:0;transition:width .08s linear;
  background:linear-gradient(90deg,var(--boot-cyan),#3c9cff 56%,var(--boot-violet));
  box-shadow:0 0 14px rgba(0,240,255,.9),0 0 32px rgba(118,87,255,.55);}
.omni-bar b{position:absolute;right:0;top:11px;font-size:8px;letter-spacing:.2em;
  color:rgba(190,225,255,.66);font-weight:500;}
.omni-telemetry{position:absolute;left:50%;top:50%;width:min(760px,84vw);height:min(230px,36vh);
  transform:translate(-50%,-50%);display:flex;opacity:0;transition:opacity .35s ease;}
.omni-bracket{width:36px;border-top:1px solid rgba(0,240,255,.45);
  border-bottom:1px solid rgba(0,240,255,.45);}
.omni-bracket.l{border-left:2px solid var(--boot-cyan)}
.omni-bracket.r{border-right:2px solid var(--boot-cyan)}
.omni-ticker{flex:1;overflow:hidden;white-space:pre-line;padding:24px 30px;
  font-size:clamp(9px,1.15vw,13px);line-height:1.9;letter-spacing:.24em;text-align:center;
  color:rgba(205,235,255,.86);text-transform:uppercase;text-shadow:0 0 9px rgba(0,240,255,.5);
  background:linear-gradient(90deg,transparent,rgba(3,15,35,.36),transparent);}
.omni-ticker::after{content:'_';color:var(--boot-cyan);animation:omniCursor .48s steps(1) infinite;}
@keyframes omniCursor{50%{opacity:0}}
.omni-engage{position:absolute;left:50%;top:50%;transform:translate(-50%,-50%) scale(.7);
  display:flex;flex-direction:column;gap:12px;align-items:center;white-space:nowrap;
  font-size:clamp(20px,4vw,52px);font-weight:900;letter-spacing:.32em;text-transform:uppercase;
  color:transparent;opacity:0;background:linear-gradient(180deg,#fff,var(--boot-cyan) 58%,#547cff);
  -webkit-background-clip:text;background-clip:text;filter:drop-shadow(0 0 24px rgba(0,240,255,.8));
  transition:opacity .18s ease,transform .55s cubic-bezier(.14,1.5,.4,1);}
.omni-engage small{font-size:8px;letter-spacing:.42em;color:#9eefff;-webkit-text-fill-color:#9eefff;}
.omni-boot.engage .omni-engage{opacity:1;transform:translate(-50%,-50%) scale(1);}
.stage-shatter .omni-matrix,.stage-defrost .omni-matrix{opacity:0;transform:scale(1.35);}
.stage-vitals .omni-matrix,.stage-engage .omni-matrix{opacity:0;transform:scale(.7);}
.stage-vitals .omni-telemetry{opacity:1;}
.stage-engage .omni-telemetry{opacity:0;}
.stage-engage .omni-vignette{animation:omniFlare .8s ease-out both;}
@keyframes omniFlare{0%{background:rgba(180,240,255,.9)}100%{background:transparent}}
.omni-skip{position:absolute;z-index:10;right:25px;bottom:20px;padding:7px 10px;
  color:rgba(165,210,245,.4);background:transparent;border:0;border-bottom:1px solid rgba(0,240,255,.2);
  font:8px/1 'JetBrains Mono',monospace;letter-spacing:.2em;cursor:pointer;pointer-events:auto;}
.omni-skip:hover{color:#dffaff;border-color:var(--boot-cyan)}
@media (prefers-reduced-motion:reduce){.omni-warp,.omni-grid,.omni-warp i{animation:none}.omni-boot{transition:none}}

/* --- the suit rails --- */
/* No full-width bar any more: the rails are transparent docks over the
   scene, so the galaxy stays readable behind every button. */
.topbar{
  background:transparent;
}
/* The rails stay off the title screen and fade in once the player enters. */
.topbar.hidden{ opacity:0; pointer-events:none; }
/* Cinematic mode (H): the rails and every panel drop away, leaving only the
   armor visor metrics over a clean view of the universe. */
body[data-cinematic="1"] .topbar,
body[data-cinematic="1"] .wm-layer{ display:none !important; }

/* Helmet power-on choreography. This is a construction sequence, not a
   generic fade: an energy scan traverses the glass, perimeter rails draw
   themselves, pylon nodes strike, then instrument plates resolve in order. */
.fhud.fhud-powering::after{content:'';position:absolute;z-index:20;left:-15%;top:0;
  width:12%;height:100%;pointer-events:none;opacity:0;
  background:linear-gradient(90deg,transparent,rgba(140,245,255,.3),#e8ffff,rgba(0,240,255,.22),transparent);
  filter:blur(9px);mix-blend-mode:screen;animation:fhPowerSweep 1.8s .28s cubic-bezier(.22,.72,.2,1) both;}
.fhud.fhud-powering .fv-top,.fhud.fhud-powering .fv-bottom{
  animation:fhRailBoot 1.15s cubic-bezier(.15,.8,.2,1) both;}
.fhud.fhud-powering .fv-side,.fhud.fhud-powering .fv-pylon{
  animation:fhSideBoot .9s .24s cubic-bezier(.15,.8,.2,1) both;}
.fhud.fhud-powering .fhud-left,.fhud.fhud-powering .fhud-right,
.fhud.fhud-powering .fhud-vitals,.fhud.fhud-powering .fhud-gears,
.fhud.fhud-powering .fhud-status{animation:fhTelemetryBoot .72s .72s ease-out both;}
.fhud.fhud-powering .fhud-director,.fhud.fhud-powering .fhud-reticle{
  animation:fhDirectorLock .8s 1.45s cubic-bezier(.15,1.5,.3,1) both;}
.fhud.fhud-powering .fh-block{animation:fhPlateResolve .64s 1.05s steps(9,end) both;}
.fhud.fhud-powering .fhud-left .fh-block:nth-child(2){animation-delay:1.2s}
.fhud.fhud-powering .fhud-right .fh-block:nth-child(1){animation-delay:1.12s}
.fhud.fhud-powering .fhud-right .fh-block:nth-child(2){animation-delay:1.26s}
.fhud.fhud-powering .fhud-right .fh-block:nth-child(3){animation-delay:1.4s}
@keyframes fhPowerSweep{0%{left:-15%;opacity:0}15%{opacity:1}72%{opacity:.75}100%{left:112%;opacity:0}}
@keyframes fhPlateResolve{0%{opacity:0;clip-path:inset(49% 0 49% 0);filter:brightness(4)}55%{opacity:1;clip-path:inset(42% 0 42% 0)}100%{opacity:1;clip-path:inset(0);filter:brightness(1)}}
@keyframes fhRailBoot{from{transform:translateX(-50%) scaleX(0);filter:brightness(4)}to{transform:translateX(-50%) scaleX(1);filter:brightness(1)}}
@keyframes fhSideBoot{from{transform:translateY(-50%) scaleY(0);opacity:0}to{transform:translateY(-50%) scaleY(1);opacity:1}}
@keyframes fhTelemetryBoot{0%{opacity:0;filter:blur(8px)}65%{opacity:1;filter:blur(0);text-shadow:0 0 16px #00f0ff}100%{opacity:1;filter:blur(0)}}
@keyframes fhDirectorLock{0%{opacity:0;filter:blur(5px);scale:1.7}100%{opacity:1;filter:blur(0);scale:1}}

/* Reduced motion: the pulse is decorative, so drop it on request. */
@media (prefers-reduced-motion: reduce){
  .fhud-reticle{animation:none;}
  /* The sweep is ambient motion with no informational content, so it is
     the first thing to go when motion is unwelcome. */
  .fh-scan{animation:none;opacity:0;}
}

`;
