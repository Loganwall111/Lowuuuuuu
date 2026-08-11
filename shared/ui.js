/* ---------------------------------------------------------------------------
 * XUI — tiny dependency-free control panel library (dark glass style)
 * Used by both apps: Ocean Worlds and Singularity Vault.
 * Global: window.XUI
 * ------------------------------------------------------------------------- */
(function () {
  const css = `
  :root { --xui-accent: #7fd4ff; }
  .xui-panel {
    position: fixed; top: 10px; right: 10px; width: 318px; max-height: calc(100vh - 20px);
    display: flex; flex-direction: column; z-index: 50;
    font: 12px/1.45 "Segoe UI", system-ui, -apple-system, sans-serif;
    color: #dfe7ef; background: rgba(10, 14, 22, 0.82);
    border: 1px solid rgba(140, 190, 255, 0.16); border-radius: 12px;
    backdrop-filter: blur(14px); -webkit-backdrop-filter: blur(14px);
    box-shadow: 0 12px 40px rgba(0,0,0,.55), inset 0 1px 0 rgba(255,255,255,.05);
    overflow: hidden; transition: transform .25s ease, opacity .25s ease;
  }
  .xui-panel.hidden { transform: translateX(112%); opacity: 0; pointer-events: none; }
  .xui-head {
    display: flex; align-items: center; gap: 8px; padding: 10px 12px;
    background: linear-gradient(180deg, rgba(255,255,255,.06), rgba(255,255,255,0));
    border-bottom: 1px solid rgba(140,190,255,.12); cursor: default; user-select: none;
  }
  .xui-head .dot { width: 9px; height: 9px; border-radius: 50%;
    background: var(--xui-accent); box-shadow: 0 0 10px var(--xui-accent); flex: none; }
  .xui-head h1 { font-size: 12.5px; margin: 0; font-weight: 600; letter-spacing: .4px; flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .xui-head button {
    background: rgba(255,255,255,.06); color: #cfe0f0; border: 1px solid rgba(255,255,255,.1);
    border-radius: 6px; width: 22px; height: 22px; cursor: pointer; font-size: 12px; line-height: 1; padding: 0;
  }
  .xui-head button:hover { background: rgba(255,255,255,.14); }
  .xui-body { overflow-y: auto; padding: 6px 10px 10px; scrollbar-width: thin; scrollbar-color: rgba(140,190,255,.25) transparent; }
  .xui-body::-webkit-scrollbar { width: 6px; }
  .xui-body::-webkit-scrollbar-thumb { background: rgba(140,190,255,.22); border-radius: 3px; }
  .xui-folder { margin-top: 6px; border: 1px solid rgba(140,190,255,.10); border-radius: 9px; overflow: hidden; background: rgba(255,255,255,.025); }
  .xui-folder > .xui-fhead { display: flex; align-items: center; padding: 7px 9px; cursor: pointer; user-select: none;
    background: rgba(255,255,255,.035); font-weight: 600; letter-spacing: .3px; color: #cfe4ff; }
  .xui-folder > .xui-fhead .car { margin-right: 7px; font-size: 9px; opacity: .8; transition: transform .15s; }
  .xui-folder.closed > .xui-fhead .car { transform: rotate(-90deg); }
  .xui-folder > .xui-fbody { padding: 4px 9px 8px; }
  .xui-folder.closed > .xui-fbody { display: none; }
  .xui-row { margin: 7px 0; }
  .xui-row .xui-lab { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 3px; }
  .xui-row .xui-lab .val { color: var(--xui-accent); font-variant-numeric: tabular-nums; font-weight: 600; font-size: 11.5px; }
  .xui-row input[type=range] { width: 100%; accent-color: var(--xui-accent); height: 18px; margin: 0; cursor: pointer; }
  .xui-row select, .xui-row input[type=number] {
    width: 100%; background: rgba(5,8,14,.85); color: #e6eefb; border: 1px solid rgba(140,190,255,.18);
    border-radius: 6px; padding: 5px 7px; font: inherit; outline: none;
  }
  .xui-row input[type=color] { width: 100%; height: 24px; border: 1px solid rgba(140,190,255,.2); border-radius: 6px; background: transparent; padding: 1px 2px; cursor: pointer; }
  .xui-toggle-row { display: flex; align-items: center; justify-content: space-between; margin: 7px 0; cursor: pointer; user-select: none; }
  .xui-switch { position: relative; width: 34px; height: 18px; border-radius: 9px; background: rgba(255,255,255,.12); transition: background .18s; flex: none; }
  .xui-switch::after { content: ""; position: absolute; left: 2px; top: 2px; width: 14px; height: 14px; border-radius: 50%;
    background: #b9c7d6; transition: transform .18s, background .18s; }
  .xui-toggle-row.on .xui-switch { background: var(--xui-accent); }
  .xui-toggle-row.on .xui-switch::after { transform: translateX(16px); background: #08131f; }
  .xui-btn { display: inline-block; width: 100%; margin: 4px 0; padding: 7px 8px; text-align: center; border-radius: 8px;
    border: 1px solid rgba(140,190,255,.25); background: rgba(120,180,255,.08); color: #dfeeff; cursor: pointer;
    font: 600 12px/1 inherit; letter-spacing: .3px; transition: background .15s, box-shadow .15s; }
  .xui-btn:hover { background: rgba(120,180,255,.18); box-shadow: 0 0 14px rgba(127,212,255,.15); }
  .xui-btn.accent { background: color-mix(in srgb, var(--xui-accent) 22%, transparent); border-color: var(--xui-accent); }
  .xui-btn.danger { background: rgba(255,90,90,.12); border-color: rgba(255,120,120,.4); color: #ffd9d9; }
  .xui-btnrow { display: flex; gap: 6px; } .xui-btnrow .xui-btn { flex: 1; }
  .xui-read { display: flex; justify-content: space-between; margin: 6px 0; }
  .xui-read .v { color: var(--xui-accent); font-family: ui-monospace, Consolas, monospace; font-size: 11px; text-align: right; word-break: break-all; max-width: 62%; }
  .xui-note { font-size: 10.5px; color: #93a6bd; margin: 5px 0 3px; line-height: 1.5; }
  .xui-foot { padding: 6px 12px; border-top: 1px solid rgba(140,190,255,.12); font-size: 10.5px; color: #9fb2c8;
    display: flex; justify-content: space-between; background: rgba(0,0,0,.25); font-variant-numeric: tabular-nums; }
  .xui-collapse-tab {
    position: fixed; top: 10px; right: 10px; z-index: 49; width: 34px; height: 34px; border-radius: 10px;
    background: rgba(10,14,22,.8); border: 1px solid rgba(140,190,255,.25); color: var(--xui-accent);
    font-size: 16px; cursor: pointer; display: none; align-items: center; justify-content: center;
    backdrop-filter: blur(10px);
  }
  .xui-collapse-tab.show { display: flex; }
  .xui-sep { height: 1px; background: rgba(140,190,255,.10); margin: 8px -9px 4px; }
  `;
  const st = document.createElement("style");
  st.textContent = css;
  document.head.appendChild(st);

  function el(tag, cls, parent, text) {
    const e = document.createElement(tag);
    if (cls) e.className = cls;
    if (text !== undefined) e.textContent = text;
    if (parent) parent.appendChild(e);
    return e;
  }
  function fmt(v, step) {
    if (step >= 1) return String(Math.round(v));
    const dec = Math.min(4, Math.max(0, -Math.floor(Math.log10(step))));
    return Number(v).toFixed(dec);
  }

  function attachAdders(host, container, panel) {
    host.slider = function (label, min, max, step, value, onChange) {
      const row = el("div", "xui-row", container);
      const lab = el("div", "xui-lab", row);
      el("span", null, lab, label);
      const val = el("span", "val", lab, fmt(value, step));
      const input = el("input", null, row);
      input.type = "range"; input.min = min; input.max = max; input.step = step; input.value = value;
      input.addEventListener("input", () => {
        const v = parseFloat(input.value);
        val.textContent = fmt(v, step);
        onChange(v);
      });
      return { set(v) { input.value = v; val.textContent = fmt(v, step); }, row };
    };
    host.select = function (label, options, value, onChange) {
      const row = el("div", "xui-row", container);
      const lab = el("div", "xui-lab", row); el("span", null, lab, label);
      const sel = el("select", null, row);
      options.forEach(o => {
        const op = document.createElement("option");
        op.value = (o && typeof o === "object") ? o.value : o;
        op.textContent = (o && typeof o === "object") ? o.label : o;
        sel.appendChild(op);
      });
      sel.value = value;
      sel.addEventListener("change", () => onChange(sel.value));
      return { set(v) { sel.value = v; }, row, el: sel };
    };
    host.color = function (label, value, onChange) {
      const row = el("div", "xui-row", container);
      const lab = el("div", "xui-lab", row); el("span", null, lab, label);
      const input = el("input", null, row);
      input.type = "color"; input.value = value;
      input.addEventListener("input", () => onChange(input.value));
      return { set(v) { input.value = v; }, row };
    };
    host.toggle = function (label, value, onChange) {
      const row = el("div", "xui-toggle-row" + (value ? " on" : ""), container);
      el("span", null, row, label);
      el("span", "xui-switch", row);
      const flip = () => {
        const on = !row.classList.contains("on");
        row.classList.toggle("on", on);
        onChange(on);
      };
      row.addEventListener("click", flip);
      return { set(v) { row.classList.toggle("on", !!v); }, row };
    };
    host.button = function (label, onClick, kind) {
      const b = el("button", "xui-btn" + (kind ? " " + kind : ""), container, label);
      b.addEventListener("click", onClick);
      return { el: b };
    };
    host.buttonRow = function (defs) {
      const wrap = el("div", "xui-btnrow", container);
      defs.forEach(d => {
        const b = el("button", "xui-btn" + (d.kind ? " " + d.kind : ""), wrap, d.label);
        b.addEventListener("click", d.onClick);
      });
      return wrap;
    };
    host.read = function (label, initial) {
      const row = el("div", "xui-read", container);
      el("span", null, row, label);
      const v = el("span", "v", row, initial !== undefined ? String(initial) : "—");
      return { set(x) { v.textContent = String(x); }, row };
    };
    host.number = function (label, value, onChange, step = 1) {
      const row = el("div", "xui-row", container);
      const lab = el("div", "xui-lab", row); el("span", null, lab, label);
      const input = el("input", null, row);
      input.type = "number"; input.value = value; input.step = step;
      input.addEventListener("change", () => { const v = parseFloat(input.value); if (isFinite(v)) onChange(v); });
      return { set(v) { input.value = v; }, row };
    };
    host.note = function (html) { el("div", "xui-note", container).innerHTML = html; };
    host.sep = function () { el("div", "xui-sep", container); };
    return host;
  }

  function createPanel(title, opts) {
    opts = opts || {};
    if (opts.accent) document.documentElement.style.setProperty("--xui-accent", opts.accent);
    const root = el("div", "xui-panel");
    const head = el("div", "xui-head", root);
    el("span", "dot", head);
    el("h1", null, head, title);
    const bMin = el("button", null, head, "–"); bMin.title = "Collapse panel";
    const bHide = el("button", null, head, "×"); bHide.title = "Hide panel (press H to bring it back)";
    const body = el("div", "xui-body", root);
    const foot = el("div", "xui-foot", root);
    const fL = el("span", null, foot, opts.footer || "");
    const fR = el("span", null, foot, "");
    document.body.appendChild(root);
    const tab = el("button", "xui-collapse-tab", null, "☰");
    document.body.appendChild(tab);

    bMin.addEventListener("click", () => {
      const hidden = body.style.display === "none";
      body.style.display = hidden ? "" : "none";
      foot.style.display = hidden ? "" : "none";
    });
    function hidePanel() { root.classList.add("hidden"); tab.classList.add("show"); }
    function showPanel() { root.classList.remove("hidden"); tab.classList.remove("show"); }
    bHide.addEventListener("click", hidePanel);
    tab.addEventListener("click", showPanel);
    window.addEventListener("keydown", (e) => {
      if (e.key.toLowerCase() === "h" && !e.repeat && !/input|select|textarea/i.test(document.activeElement.tagName)) {
        root.classList.contains("hidden") ? showPanel() : hidePanel();
      }
    });

    const panel = {
      root, body, foot,
      setFooterLeft(s) { fL.textContent = s; },
      setFooterRight(s) { fR.textContent = s; },
      folder(name, open) {
        const f = el("div", "xui-folder" + (open === false ? " closed" : ""), body);
        const fh = el("div", "xui-fhead", f);
        el("span", "car", fh, "▼");
        el("span", null, fh, name);
        const fb = el("div", "xui-fbody", f);
        fh.addEventListener("click", () => f.classList.toggle("closed"));
        return attachAdders({}, fb, panel);
      },
      toggle() { root.classList.contains("hidden") ? showPanel() : hidePanel(); }
    };
    attachAdders(panel, body, panel);
    return panel;
  }

  window.XUI = { createPanel };
})();
