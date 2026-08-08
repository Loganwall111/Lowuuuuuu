/* ============================================================
   GRAVITON :: audio — procedural WebAudio ambient + SFX
   ============================================================ */
const AudioEngine = (() => {
  let ctx=null, master=null, musicGain=null, sfxGain=null, started=false;
  let drone=[], padTimer=null, arpT=0, arpStep=0;

  const scale = [0,2,4,7,9]; // pentatonic-ish

  function init(){
    if(ctx) return;
    ctx = new (window.AudioContext||window.webkitAudioContext)();
    master = ctx.createGain(); master.gain.value=0.7; master.connect(ctx.destination);
    musicGain = ctx.createGain(); musicGain.gain.value=0.0; musicGain.connect(master);
    sfxGain = ctx.createGain(); sfxGain.gain.value=0.5; sfxGain.connect(master);
    started=true;
    startDrone();
    fadeIn(musicGain, 0.6, 3);
    padTimer = setInterval(schedulePad, 600);
    scheduleArp();
  }
  function resume(){ if(ctx&&ctx.state==='suspended') ctx.resume(); }

  function now(){ return ctx.currentTime; }

  function startDrone(){
    const base=55; // A1
    const freqs=[base, base*1.5, base*2, base*3];
    freqs.forEach((f,i)=>{
      const o=ctx.createOscillator(), g=ctx.createGain();
      o.type='sine'; o.frequency.value=f; o.detune.value=(i-1.5)*7;
      g.gain.value=i===0?0.12:0.06;
      o.connect(g); g.connect(musicGain);
      o.start(); drone.push({o,g});
    });
    // slow movement on the drone
    setInterval(()=>{ drone.forEach((d,j)=>{ try{ d.o.frequency.linearRampToValueAtTime((55*[1,1.5,2,3][j])*(Math.random()<0.15?1.5:1), now()+2); }catch(e){} }); }, 8000);
  }

  function schedulePad(){
    if(!ctx) return;
    const root=110;
    const note = scale[Math.floor(Math.random()*scale.length)] + 24;
    const f = root * Math.pow(2, note/12);
    const o=ctx.createOscillator(), g=ctx.createGain();
    o.type='sine'; o.frequency.value=f;
    const t=now();
    g.gain.setValueAtTime(0,t);
    g.gain.linearRampToValueAtTime(0.05, t+2);
    g.gain.exponentialRampToValueAtTime(0.0001, t+7);
    o.connect(g); g.connect(musicGain);
    o.start(t); o.stop(t+7.5);
  }

  function scheduleArp(){
    if(!ctx) return;
    arpT = now();
    const bpm=110, beat=60/bpm;
    const playStep=()=>{
      const idx = [0,2,4,7,9,12,9,7][arpStep%8] + 12;
      const f = 220 * Math.pow(2, idx/12);
      const o=ctx.createOscillator(), g=ctx.createGain();
      o.type='triangle'; o.frequency.value=f; o.detune.value=(Math.random()-0.5)*12;
      const t=now();
      g.gain.setValueAtTime(0,t);
      g.gain.linearRampToValueAtTime(0.045, t+0.01);
      g.gain.exponentialRampToValueAtTime(0.0001, t+0.5);
      o.connect(g); g.connect(musicGain);
      o.start(t); o.stop(t+0.6);
      arpStep++;
    };
    const loop=()=>{ if(!ctx) return; playStep(); setTimeout(loop, (beat/2)*1000); };
    loop();
  }

  function fadeIn(g,val,sec){ g.gain.linearRampToValueAtTime(val, now()+sec); }

  /* ---------- SFX ---------- */
  function tone({freq=440,type='sine',dur=0.3,vol=0.3,slide=0,at}){
    if(!ctx) return;
    const t=at||now();
    const o=ctx.createOscillator(), g=ctx.createGain();
    o.type=type; o.frequency.setValueAtTime(freq,t);
    if(slide) o.frequency.exponentialRampToValueAtTime(Math.max(1,freq+slide), t+dur);
    g.gain.setValueAtTime(0,t);
    g.gain.linearRampToValueAtTime(vol, t+0.01);
    g.gain.exponentialRampToValueAtTime(0.0001, t+dur);
    o.connect(g); g.connect(sfxGain);
    o.start(t); o.stop(t+dur+0.02);
  }
  function noise({dur=0.3,vol=0.2,filter=1200,slide=0}){
    if(!ctx) return;
    const n=ctx.createBufferSource(); n.buffer=noiseBuf();
    const f=ctx.createBiquadFilter(); f.type='lowpass'; f.frequency.value=filter;
    const g=ctx.createGain(); const t=now();
    g.gain.setValueAtTime(vol,t);
    g.gain.exponentialRampToValueAtTime(0.0001, t+dur);
    n.connect(f); f.connect(g); g.connect(sfxGain);
    n.start(t); n.stop(t+dur+0.02);
  }
  let _nbuf=null;
  function noiseBuf(){ if(_nbuf) return _nbuf; const l=ctx.sampleRate; _nbuf=ctx.createBuffer(1,l,ctx.sampleRate); const d=_nbuf.getChannelData(0); for(let i=0;i<l;i++) d[i]=Math.random()*2-1; return _nbuf; }

  return {
    init, resume, isOn:()=>!!ctx,
    sfx:{
      shard(){ tone({freq:880,type:'sine',dur:0.25,vol:0.25,slide:500}); tone({freq:1320,dur:0.2,vol:0.15,at:now()+0.05}); },
      boost(){ noise({dur:0.5,vol:0.25,filter:2200,slide:1800}); tone({freq:180,type:'sawtooth',dur:0.4,vol:0.15,slide:-120}); },
      puzzle(){ tone({freq:520,type:'triangle',dur:0.35,vol:0.25,slide:320}); },
      good(){ tone({freq:660,dur:0.18,vol:0.25}); tone({freq:990,dur:0.18,vol:0.25,at:now()+0.12}); tone({freq:1320,dur:0.3,vol:0.22,at:now()+0.24}); },
      bad(){ tone({freq:180,type:'sawtooth',dur:0.3,vol:0.2,slide:-80}); },
      warp(){ noise({dur:1.2,vol:0.4,filter:300,slide:3000}); tone({freq:80,type:'sine',dur:1.2,vol:0.3,slide:600}); },
      dilate(){ tone({freq:300,type:'sine',dur:0.6,vol:0.18,slide:-200}); },
      warning(){ tone({freq:900,type:'square',dur:0.12,vol:0.12}); setTimeout(()=>tone({freq:700,type:'square',dur:0.12,vol:0.12}),160); },
    },
  };
})();
