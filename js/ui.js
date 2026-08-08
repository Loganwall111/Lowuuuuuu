/* ============================================================
   GRAVITON :: ui — HUD, overlays, puzzle forms
   ============================================================ */
const UI = (() => {
  const $=id=>document.getElementById(id);
  const hud=$('hud'), title=$('title'), overlay=$('overlay');
  let activeOnSubmit=null;

  function init(){
    $('btn-start').addEventListener('click', ()=> window.__START__ && window.__START__());
  }

  function setHUD({sector, energy, shards, score}){
    $('hud-sector').textContent=sector;
    $('hud-shards').textContent=shards;
    $('hud-score').textContent=score.toLocaleString();
    const fill=$('energy-fill');
    fill.style.width=(Util.clamp(energy,0,CONFIG.energyMax)/CONFIG.energyMax*100)+'%';
    if(energy/CONFIG.energyMax<0.25) fill.style.background='linear-gradient(90deg,#ff3d6e,#ffd54f)';
    else fill.style.background='linear-gradient(90deg,#ff3df0,#4ff5ff)';
  }
  function showHUD(){ hud.classList.remove('hidden'); }
  function hideHUD(){ hud.classList.add('hidden'); }
  function showTitle(){ title.classList.remove('hidden'); }
  function hideTitle(){ title.classList.add('hidden'); }

  function msg(text, ms){
    $('hud-msg').textContent=text||'';
    if(ms && window.__MSG_TIMER__) clearTimeout(window.__MSG_TIMER__);
    if(ms) window.__MSG_TIMER__=setTimeout(()=>{ $('hud-msg').textContent=''; }, ms);
  }

  function showOverlay(titleText, contentHTML){
    $('overlay-title').textContent=titleText;
    $('overlay-content').innerHTML=contentHTML;
    overlay.classList.remove('hidden');
    activeOnSubmit=null;
    const first=overlay.querySelector('input'); if(first) setTimeout(()=>first.focus(),50);
  }
  function hideOverlay(){ overlay.classList.add('hidden'); activeOnSubmit=null; }
  function isOverlayOpen(){ return !overlay.classList.contains('hidden'); }

  /* ---------- puzzle form ---------- */
  function renderPuzzle(puzzle, onSubmit, onGiveUp){
    let html='', extra='';
    let input='<input class="puzzle-input" autocomplete="off" spellcheck="false" />';
    if(puzzle.type==='math'){
      html=`<p class="prompt">${puzzle.prompt}</p>
            <p class="puzzle-glyphs" style="letter-spacing:4px;font-size:26px;">${puzzle.display}</p>
            <div>${input}</div>`;
    } else {
      const tableHTML = (puzzle.table||[]).map(t=>`<div class="cipher-table"><span class="k">${t.k} →</span><span class="v">${t.v}</span></div>`).join('');
      extra = `<p class="mini">glyphs: ${puzzle.glyph}</p>`;
      html=`<p class="prompt">${puzzle.prompt}</p>
            <p class="puzzle-glyphs">${puzzle.display}</p>
            ${tableHTML}
            <p class="mini">${puzzle.hint||''}</p>
            <div>${input}</div>`;
    }
    html+=`<div class="feedback"></div>
           <div class="btn-row">
             <button class="btn" id="pz-submit">✦ SUBMIT ✦</button>
             <button class="btn alt" id="pz-giveup">abandon</button>
           </div>`;
    showOverlay('JUMP GATE · SIGIL LOCKED', html+extra);
    const fb=overlay.querySelector('.feedback');
    const inp=overlay.querySelector('input');
    const submit=()=>{
      const val=inp.value.trim();
      if(!val){ fb.textContent='enter something...'; fb.className='feedback bad'; return; }
      if(puzzle.type==='math'){
        const n=parseFloat(val.replace(/,/g,''));
        if(Math.abs(n-puzzle.answer)<0.001){ onSubmit(true); }
        else { fb.textContent='✗ that field rejects your answer'; fb.className='feedback bad'; AudioEngine.sfx.bad(); }
      } else {
        if(val.toUpperCase()===puzzle.word){ onSubmit(true); }
        else { fb.textContent='✗ not the right resonance'; fb.className='feedback bad'; AudioEngine.sfx.bad(); }
      }
    };
    $('pz-submit').addEventListener('click', submit);
    inp.addEventListener('keydown', e=>{ if(e.key==='Enter') submit(); });
    $('pz-giveup').addEventListener('click', ()=> onGiveUp && onGiveUp());
  }

  function dilation(on){
    $('time-dilation').classList.toggle('hidden', !on);
  }

  return { init, setHUD, showHUD, hideHUD, showTitle, hideTitle, msg,
           showOverlay, hideOverlay, isOverlayOpen, renderPuzzle, dilation };
})();
