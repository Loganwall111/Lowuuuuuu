/* ============================================================
   GRAVITON 3D :: ui — HUD, abilities, puzzles, overlays
   ============================================================ */
const UI3D = (() => {
  const $=id=>document.getElementById(id);
  let submitCb=null;

  function init(){
    $('btn-start').addEventListener('click', ()=> window.__START3D__ && window.__START3D__());
    $('btn-solve').addEventListener('click', submit);
    $('btn-abandon').addEventListener('click', ()=> { if(window.__ABANDON__) window.__ABANDON__(); });
  }

  function showTitle(){ $('title').classList.remove('hidden'); }
  function hideTitle(){ $('title').classList.add('hidden'); }
  function showHUD(){ $('hud').classList.remove('hidden'); }
  function hideHUD(){ $('hud').classList.add('hidden'); }

  function setHUD(o){
    $('hud-sector').textContent=o.sector;
    $('hud-shards').textContent=o.shards;
    $('hud-score').textContent=o.score.toLocaleString();
    const e=U3.clamp(o.energy,0,CFG3D.energyMax)/CFG3D.energyMax*100;
    const fill=$('energy-fill');
    fill.style.width=e+'%';
    fill.style.background= e<25? 'linear-gradient(90deg,#ff3d6e,#ffd54f)':'linear-gradient(90deg,#ff3df0,#4ff5ff)';
    // ability cooldowns
    CFG3D.abilities.forEach((a,idx)=>{
      const cd=o.abilityCD[a.id]||0;
      const el=$('ab-'+idx);
      const pct = a.cooldown>0 ? U3.clamp(cd/a.cooldown,0,1) : 0;
      const cover=el.querySelector('.cd-cover');
      if(cover){ cover.style.height=(pct*100)+'%'; cover.style.background= pct>0? 'rgba(0,0,10,0.75)':'transparent'; }
      const key=el.querySelector('.cd-key');
      if(key) key.textContent= (cd>0? cd.toFixed(1): a.key);
    });
  }

  function msg(text,ms){
    $('hud-msg').textContent=text||'';
    if(window.__MSG3D__) clearTimeout(window.__MSG3D__);
    if(ms) window.__MSG3D__=setTimeout(()=>{ $('hud-msg').textContent=''; }, ms);
  }

  function showOverlay(title, content){
    $('overlay-title').textContent=title;
    $('overlay-content').innerHTML=content;
    $('overlay').classList.remove('hidden');
    const inp=$('overlay').querySelector('input'); if(inp) setTimeout(()=>inp.focus(),50);
  }
  function hideOverlay(){ $('overlay').classList.add('hidden'); }
  function isOpen(){ return !$('overlay').classList.contains('hidden'); }

  function renderPuzzle(p, onOk, onAbandon){
    window.__ABANDON__=onAbandon;
    submitCb=null;
    let inner='';
    if(p.type==='math'){
      inner=`<p class="prompt">Unlock the jump gate · solve the equation</p>
             <p class="puzzle-glyphs" style="font-size:28px">${p.display}</p>
             <div><input class="puzzle-input" autocomplete="off" spellcheck="false" /></div>`;
    } else {
      const tbl=(p.table||[]).map(t=>`<div class="cipher-table"><span class="k">${t.k} →</span><span class="v">${t.v}</span></div>`).join('');
      inner=`<p class="prompt">Decode the Sigil of the Ancients</p>
             <p class="puzzle-glyphs">${p.glyph}</p>${tbl}
             <p class="mini">${p.hint||''}</p>
             <div><input class="puzzle-input" autocomplete="off" spellcheck="false" /></div>`;
    }
    inner+=`<div class="feedback"></div>
            <div class="btn-row"><button class="btn" id="btn-solve">✦ SUBMIT ✦</button>
            <button class="btn alt" id="btn-abandon">abandon</button></div>`;
    showOverlay('JUMP GATE · SIGIL LOCKED', inner);
    const inp=$('overlay').querySelector('input');
    const fb=$('overlay').querySelector('.feedback');
    submitCb=()=>{
      const val=inp.value.trim();
      if(!val){ fb.textContent='enter something...'; fb.className='feedback bad'; return; }
      if(p.type==='math'){
        const n=parseFloat(val.replace(/,/g,''));
        if(Math.abs(n-p.answer)<0.001){ onOk(); }
        else { fb.textContent='✗ the field rejects your answer'; fb.className='feedback bad'; Audio3D.sfx.bad(); }
      } else {
        if(val.toUpperCase()===p.word){ onOk(); }
        else { fb.textContent='✗ not the right resonance'; fb.className='feedback bad'; Audio3D.sfx.bad(); }
      }
    };
    inp.addEventListener('keydown',e=>{ if(e.key==='Enter') submit(); });
  }
  function submit(){ if(submitCb) submitCb(); }

  function dilation(on){ $('time-dilation').classList.toggle('hidden',!on); }

  return { init, showTitle, hideTitle, showHUD, hideHUD, setHUD, msg,
           showOverlay, hideOverlay, isOpen, renderPuzzle, dilation };
})();
