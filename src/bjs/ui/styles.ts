export const UI_CSS = `
:root{
  --bg:#05070d; --panel:rgba(16,20,30,.82); --panel-solid:#11141d;
  --line:rgba(255,255,255,.10); --line2:rgba(255,255,255,.06);
  --txt:#e8edf7; --dim:#8b95ad; --dim2:#5d6679;
  --acc:#4da3ff; --acc2:#7c5cff; --ok:#31d68a; --warn:#ffb545;
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
  border:1px solid var(--line);border-radius:var(--r);box-shadow:var(--shadow);
  min-width:172px;max-height:calc(100vh - 96px);overflow:hidden;
  /* A panel may never be wide enough to cover the middle of the screen. */
  max-width:min(300px,26vw);
  animation:wmIn .16s cubic-bezier(.2,.8,.3,1);
  font-size:calc(13px * var(--ui-scale));
  background:var(--panel-dyn);
  transition:opacity .35s ease, background .2s ease;
}
/* Panels fade back when you are not using them, so they never hide the sim. */
body[data-idle="1"] .wm-win:not(:hover):not(.wm-pinned){ opacity:var(--idle-alpha); }
body[data-idle="1"] .wm-win:not(:hover):not(.wm-pinned) .wm-body{ pointer-events:none; }
.wm-win:hover{ opacity:1 !important; }
/* Focus mode hides every panel instantly without closing anything. */
body[data-focus="1"] .wm-layer,
body[data-focus="1"] .topbar,
body[data-focus="1"] .wm-dock{ opacity:0; pointer-events:none; }
body[data-focus="1"] .hud{ opacity:.35; }
.wm-win.wm-pinned{ box-shadow:0 0 0 1px var(--acc), var(--shadow); }
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
.wm-body{padding:9px 10px;overflow-y:auto;overflow-x:hidden;flex:1 1 auto;min-height:0;}
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

/* ============ top bar ============ */
.topbar{
  position:fixed;top:0;left:0;right:0;height:56px;z-index:80;display:flex;align-items:center;
  gap:10px;padding:0 14px;pointer-events:none;
}
.topbar > *{pointer-events:auto}
.brand{display:flex;align-items:center;gap:9px;padding:7px 14px 7px 11px;border-radius:12px;
  background:var(--panel);backdrop-filter:blur(20px);border:1px solid var(--line);box-shadow:var(--shadow);}
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

.iconbtn{
  width:38px;height:38px;border-radius:11px;border:1px solid var(--line);background:var(--panel);
  backdrop-filter:blur(20px);color:var(--dim);cursor:pointer;font-size:16px;display:grid;place-items:center;
  box-shadow:var(--shadow);transition:all .13s;font-family:inherit;position:relative;
}
.iconbtn:hover{color:#fff;border-color:var(--acc);transform:translateY(-1px)}
.iconbtn.on{color:#fff;background:linear-gradient(135deg,var(--acc),var(--acc2));border-color:transparent}

/* ============ HUD ============ */
.hud{
  position:fixed;left:14px;bottom:14px;z-index:55;pointer-events:none;
  display:flex;gap:8px;align-items:flex-end;
}
.hud-chip{
  background:var(--panel);backdrop-filter:blur(20px);border:1px solid var(--line);border-radius:11px;
  padding:8px 13px;box-shadow:var(--shadow);
}
.hud-k{font-size:9px;color:var(--dim2);letter-spacing:1.3px;text-transform:uppercase;}
.hud-v{font-size:16px;font-weight:700;font-variant-numeric:tabular-nums;line-height:1.15}
.hud-v small{font-size:10px;color:var(--dim);font-weight:500}

/* ============ controls ============ */
.grp{margin-bottom:16px}
.grp:last-child{margin-bottom:2px}
.grp-h{font-size:9.5px;letter-spacing:1.5px;text-transform:uppercase;color:var(--dim2);
  margin:0 0 9px;display:flex;align-items:center;gap:7px;font-weight:700}
.grp-h::after{content:'';flex:1;height:1px;background:var(--line2)}

.ctl{margin-bottom:13px}
.ctl-top{display:flex;justify-content:space-between;align-items:baseline;margin-bottom:6px;gap:8px}
.ctl-l{font-size:12px;color:var(--txt);font-weight:500}
.ctl-v{font-size:11.5px;color:var(--acc);font-variant-numeric:tabular-nums;font-weight:600;
  background:rgba(77,163,255,.10);padding:1.5px 7px;border-radius:6px;white-space:nowrap}
input[type=range]{
  -webkit-appearance:none;appearance:none;width:100%;height:22px;background:transparent;cursor:pointer;margin:0;display:block;
}
input[type=range]::-webkit-slider-runnable-track{height:5px;border-radius:4px;
  background:linear-gradient(90deg,var(--acc) var(--pct,50%),rgba(255,255,255,.11) var(--pct,50%));}
input[type=range]::-webkit-slider-thumb{-webkit-appearance:none;width:15px;height:15px;border-radius:50%;
  background:#fff;margin-top:-5px;box-shadow:0 2px 7px rgba(0,0,0,.5);border:2.5px solid var(--acc);transition:transform .1s}
input[type=range]::-webkit-slider-thumb:hover{transform:scale(1.16)}
input[type=range]::-moz-range-track{height:5px;border-radius:4px;background:rgba(255,255,255,.11)}
input[type=range]::-moz-range-progress{height:5px;border-radius:4px;background:var(--acc)}
input[type=range]::-moz-range-thumb{width:13px;height:13px;border-radius:50%;background:#fff;border:2.5px solid var(--acc);cursor:pointer}

.btnrow{display:flex;flex-wrap:wrap;gap:7px}
.btn{
  flex:1 1 auto;min-width:84px;padding:9px 12px;border-radius:9px;border:1px solid var(--line);
  background:rgba(255,255,255,.045);color:var(--txt);cursor:pointer;font-size:11.5px;font-weight:600;
  font-family:inherit;transition:all .13s;text-align:center;
}
.btn:hover{background:rgba(255,255,255,.11);border-color:var(--acc);transform:translateY(-1px)}
.btn:active{transform:translateY(0)}
.btn.pri{background:linear-gradient(135deg,var(--acc),var(--acc2));border-color:transparent;color:#fff}
.btn.dgr:hover{background:#e5484d;border-color:#e5484d;color:#fff}

.stat{display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid var(--line2);font-size:12px}
.stat:last-child{border-bottom:0}
.stat-k{color:var(--dim)}
.stat-v{font-variant-numeric:tabular-nums;font-weight:600}

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
.boot{position:fixed;inset:0;z-index:200;display:grid;place-items:center;background:var(--bg);
  transition:opacity .45s ease;}
.boot.gone{opacity:0;pointer-events:none}
.boot-in{text-align:center}
.boot-name{font-size:26px;font-weight:800;letter-spacing:9px;
  background:linear-gradient(135deg,var(--acc),var(--acc2));-webkit-background-clip:text;
  background-clip:text;-webkit-text-fill-color:transparent;margin-bottom:8px}
.boot-sub{font-size:10.5px;color:var(--dim2);letter-spacing:3.5px;text-transform:uppercase;margin-bottom:24px}
.boot-bar{width:230px;height:2.5px;border-radius:3px;background:rgba(255,255,255,.10);overflow:hidden;margin:0 auto}
.boot-fill{height:100%;width:0;border-radius:3px;background:linear-gradient(90deg,var(--acc),var(--acc2));
  transition:width .3s ease}
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
`;
