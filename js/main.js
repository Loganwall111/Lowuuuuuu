/* ============================================================
   GRAVITON :: main — state machine, world gen, game loop
   ============================================================ */
const Game = (() => {
  let canvas, ctx;
  let state='title';
  let sector=0;
  let bodies=[], shards=[], gate=null, particles=[];
  let spawn={x:0,y:0};
  let cam={cx:0,cy:0,shake:0};
  let hue=0;
  let time=0;
  let score=0, shardCount=0;
  let pending=null; // pending puzzle for current gate
  let warpT=0;
  let gameOverMsg=null;

  const input={forward:false,back:false,left:false,right:false,boost:false,pause:false};
  let showGravity=false;
  let mouseX=innerWidth/2, mouseY=innerHeight/2;

  const ship={x:0,y:0,vx:0,vy:0,ang:0,boosting:false};

  function init(){
    canvas=document.getElementById('game');
    ctx=canvas.getContext('2d');
    Render.init(ctx);
    resize();
    window.addEventListener('resize',resize);
    UI.init();
    bindInput();
    window.__START__=startGame;
    // idle title animation
    requestAnimationFrame(loop);
  }

  function resize(){
    canvas.width=innerWidth; canvas.height=innerHeight;
    Render.resize(innerWidth,innerHeight);
  }

  function bindInput(){
    const map={};
    const down={ArrowUp:'forward',KeyW:'forward',ArrowDown:'back',KeyS:'back',
                ArrowLeft:'left',KeyA:'left',ArrowRight:'right',KeyD:'right'};
    window.addEventListener('keydown',e=>{
      if(e.code==='Space'){ if(state==='playing'){ input.boost=true; e.preventDefault(); } }
      if(e.code==='KeyG' && state==='playing') showGravity=!showGravity;
      if(e.code==='KeyP' && (state==='playing'||state==='paused')) togglePause();
      if(down[e.code]){ input[down[e.code]]=true; e.preventDefault(); }
    });
    window.addEventListener('keyup',e=>{
      if(e.code==='Space') input.boost=false;
      if(down[e.code]) input[down[e.code]]=false;
    });
    window.addEventListener('mousemove',e=>{ mouseX=e.clientX; mouseY=e.clientY; });
    // auto-pause on blur
    window.addEventListener('blur',()=>{ if(state==='playing') togglePause(); });
  }

  function startGame(){
    AudioEngine.init(); AudioEngine.resume();
    UI.hideTitle(); UI.showHUD();
    score=0; shardCount=0; sector=0;
    loadSector(0,true);
    state='playing';
    UI.msg('GRAVITON · BEGIN YOUR ODYSSEY', 2500);
  }

  function togglePause(){
    if(state==='paused'){
      state='playing'; UI.hideOverlay(); UI.msg('');
    } else if(state==='playing'){
      state='paused';
      UI.showOverlay('PAUSED','<p>The void holds its breath.</p><div class="btn-row"><button class="btn" onclick="window.__RESUME__()">RESUME</button></div>');
      window.__RESUME__=()=>togglePause();
    }
  }

  /* ============ WORLD GENERATION ============ */
  function loadSector(s, isStart){
    bodies=[]; shards=[]; particles=[]; gate=null; warpT=0;
    sector=s;
    spawn={x:0,y:0};
    const isFinal = s===CONFIG.sectors-1;

    // bodies: planets + a black hole
    const bh=Physics.makeBody('blackhole', Util.rand(-700,700), Util.rand(-700,700), Util.rand(4000,9000), Util.rand(40,62));
    bodies.push(bh);
    for(let i=0;i<2+Math.floor(s/2);i++){
      const p=Physics.makeBody('planet', Util.rand(-1200,1200), Util.rand(-1200,1200), Util.rand(1200,2600), Util.rand(70,120));
      if(Util.dist(p.x,p.y,spawn.x,spawn.y)<300){ p.x+=400; p.y+=400; }
      bodies.push(p);
    }

    // shards scattered
    for(let i=0;i<9+ s*2;i++){
      shards.push({x:Util.rand(-1400,1400), y:Util.rand(-1400,1400), r:Util.rand(7,11), hue:Util.randInt(0,359), ph:Math.random()*6.28});
    }

    // gate far away
    const gx=Util.rand(1600,2000)*(Math.random()<0.5?-1:1);
    const gy=Util.rand(1600,2000)*(Math.random()<0.5?-1:1);
    gate={x:gx, y:gy, r:isFinal?46:64, active:false, puzzleKind: Math.random()<0.5?'math':'cipher'};

    // reset ship
    ship.x=spawn.x; ship.y=spawn.y; ship.vx=0; ship.vy=0; ship.ang=-Math.atan2(gate.y-ship.y,gate.x-ship.x);
    ship.boosting=false;
    ship.energy=CONFIG.energyMax;
    cam.cx=ship.x; cam.cy=ship.y;

    pending = Puzzles.random(s, gate.puzzleKind);
    gate.active=false;

    if(isFinal) UI.msg('THE SINGULARITY AWAITS', 3000);
    else UI.msg(`SECTOR ${CONFIG.sectorRoman[s]} · ${CONFIG.sectorNames[s]} · reach the jump gate`, 3500);
  }

  function nextSector(){
    if(sector>=CONFIG.sectors-1){ win(); return; }
    loadSector(sector+1,false);
  }

  function win(){
    state='gameover';
    UI.showOverlay('★ THE SINGULARITY ★',
      `<p>You crossed the event horizon and merged with the <b>Primal Light</b>.</p>
       <p style="margin-top:10px">Final score: <b style="color:#ffd54f">${score.toLocaleString()}</b></p>
       <p class="mini">${shardCount} prism shards harvested · ${CONFIG.sectorNames.length} sectors traversed</p>
       <div class="btn-row"><button class="btn" onclick="window.__RESTART__()">ODYSSEY AGAIN</button></div>`);
    window.__RESTART__=startGame;
    AudioEngine.sfx.warp();
  }

  function lose(msg){
    state='gameover';
    gameOverMsg=msg;
    UI.showOverlay('✹ CORE BLACKOUT ✹',
      `<p>${msg}</p>
       <p style="margin-top:8px">Score: <b>${score.toLocaleString()}</b></p>
       <div class="btn-row"><button class="btn" onclick="window.__RESTART__()">REIGNITE</button></div>`);
    window.__RESTART__=startGame;
  }

  /* ============ PUZZLE FLOW ============ */
  function openGate(){
    state='puzzle';
    pending = Puzzles.random(sector, gate.puzzleKind);
    UI.renderPuzzle(pending, (ok)=>{
      if(ok){
        AudioEngine.sfx.good();
        gate.active=true;
        UI.hideOverlay();
        state='playing';
        score+=500;
        UI.msg('GATE UNLOCKED · FLY THROUGH', 2000);
      }
    }, ()=>{
      UI.hideOverlay();
      state='playing';
      UI.msg('the sigil mocks you...', 1500);
    });
  }

  /* ============ GAME LOOP ============ */
  function loop(ts){
    requestAnimationFrame(loop);
    const dt=Math.min(0.033, (ts-time||0.016)/1000);
    time=ts;

    if(state==='title'){
      hue=(hue+dt*8)%360;
      Render.frame(dt, hue, 0, 0);
      Render.vignette(0);
      return;
    }

    if(state==='paused'||state==='puzzle'||state==='gameover'){
      // keep rendering but idle
      hue=(hue+dt*4)%360;
      renderWorld(dt);
      Render.vignette(0);
      return;
    }

    hue=(hue+dt*10)%360;

    if(state==='playing'){
      update(dt);
    } else if(state==='gatein'){
      updateWarp(dt);
    }

    renderWorld(dt);

    if(state==='playing') UI.setHUD({sector:CONFIG.sectorRoman[sector], energy:ship.energy, shards:shardCount, score});
  }

  function update(dt){
    // energy management
    ship.boosting=input.boost;
    let burn = CONFIG.energyDrainIdle;
    if(input.forward) burn += CONFIG.energyBurn;
    if(ship.boosting) burn += CONFIG.energyBoostBurn;
    ship.energy = Math.max(0, ship.energy - burn*dt);

    const out={vx:ship.vx, vy:ship.vy};
    Physics.updateShip(ship, bodies, input, dt, out);

    // steer toward mouse
    const mp=Physics.toWorld(mouseX,mouseY,cam);
    const target=Math.atan2(mp.y-ship.y, mp.x-ship.x);
    Physics.steerShip(ship, target, dt, 7);

    // engine particles
    if(input.forward||ship.boosting){
      const bx=ship.x-Math.cos(ship.ang)*14, by=ship.y-Math.sin(ship.ang)*14;
      Physics.emit(particles, hue, bx,by, -Math.cos(ship.ang)*60,-Math.sin(ship.ang)*60, ship.boosting?4:2, 90, 3, 60);
    }

    // collect shards
    for(let i=shards.length-1;i>=0;i--){
      const s=shards[i];
      if(Util.dist(ship.x,ship.y,s.x,s.y)<s.r+14){
        shards.splice(i,1);
        shardCount++;
        score+=CONFIG.shardScore;
        ship.energy=Math.min(CONFIG.energyMax, ship.energy+CONFIG.shardEnergy);
        AudioEngine.sfx.shard();
        Physics.emit(particles, s.hue, s.x,s.y, 0,0, 18, 160, 4, 80);
      }
    }

    // near bodies: dilation + slingshot bonus
    const near=Physics.nearBody(bodies, ship.x, ship.y);
    let dilate=false;
    if(near.body && near.body.type==='blackhole'){
      const horizonEdge=near.body.r + CONFIG.timeDilationNear;
      if(near.dist<horizonEdge){
        dilate=true;
        // slingshot speed bonus score
        const sp=Math.hypot(ship.vx,ship.vy);
        if(sp>CONFIG.shipMaxSpeed*1.2) score+=Math.floor(sp)*0.1;
      }
      // swallowed?
      if(near.dist < near.body.r+6){
        swallowed();
        return;
      }
    } else if(near.body && near.body.type==='planet'){
      if(near.dist < near.body.r+6){
        crashed();
        return;
      }
    }
    UI.dilation(dilate);
    if(dilate && Math.random()<dt*3) AudioEngine.sfx.dilate();

    // low energy warning
    if(ship.energy<15 && Math.random()<dt*1.5) AudioEngine.sfx.warning();

    // energy zero
    if(ship.energy<=0){ blackout(); return; }

    // gate interaction
    const dg=Util.dist(ship.x,ship.y,gate.x,gate.y);
    if(dg<gate.r+20){
      if(!gate.active){
        openGate();
        AudioEngine.sfx.puzzle();
        return;
      } else {
        // warp!
        state='gatein';
        warpT=0;
        AudioEngine.sfx.warp();
        Physics.emit(particles, hue, ship.x,ship.y,0,0, 40, 300, 5, 90);
        return;
      }
    }

    Physics.camera(ship, cam, innerWidth, innerHeight, dt);
    // keep ship roughly on screen via world bounds
  }

  function updateWarp(dt){
    warpT+=dt;
    // spiral ship into gate
    const dg=Util.dist(ship.x,ship.y,gate.x,gate.y);
    ship.x=Util.lerp(ship.x,gate.x, 4*dt);
    ship.y=Util.lerp(ship.y,gate.y, 4*dt);
    ship.ang+=dt*8;
    if(Math.random()<0.5) Physics.emit(particles, hue, ship.x,ship.y, Util.rand(-200,200),Util.rand(-200,200), 1, 140, 4, 85);
    cam.shake=Math.min(20, cam.shake+dt*30);
    Physics.camera(ship, cam, innerWidth, innerHeight, dt);
    if(warpT>2.2){
      if(sector>=CONFIG.sectors-1){ win(); }
      else { nextSector(); state='playing'; Render.flash(hue, 0.6); }
    }
  }

  function respawn(msg, penalty){
    cam.shake=18;
    ship.x=spawn.x; ship.y=spawn.y; ship.vx=0; ship.vy=0;
    ship.energy=Math.max(20, CONFIG.energyMax*0.5);
    score=Math.max(0, score-penalty);
    UI.msg(msg, 2200);
    AudioEngine.sfx.bad();
    Render.flash(hue, 0.4);
    particles=[];
  }

  function swallowed(){
    respawn('☍ SWALLOWED BY THE SINGULARITY · spacetime reknitted', 300);
    UI.dilation(false);
  }
  function crashed(){
    respawn('⚡ HULL BREACH · planetfall', 200);
  }
  function blackout(){
    respawn('✹ CORE BLACKOUT · energy exhausted', 150);
    UI.dilation(false);
  }

  /* ============ RENDER WORLD ============ */
  function renderWorld(dt){
    const px=(cam.cx-innerWidth/2)*0.15;
    const py=(cam.cy-innerHeight/2)*0.15;
    Render.frame(dt, hue, -px, -py);

    // gravity lines
    if(showGravity){
      for(const b of bodies){
        const a=Math.atan2(b.y-ship.y,b.x-ship.x);
        for(let i=1;i<=4;i++){
          const xx=ship.x+Math.cos(a)*i*80, yy=ship.y+Math.sin(a)*i*80;
          const s=Physics.toScreen(xx,yy,cam);
          const sx=Physics.toScreen(ship.x,ship.y,cam);
          Render.gravityLine(sx.x,sx.y,s.x,s.y, b.hue, 0.5/i);
        }
      }
    }

    // bodies
    for(const b of bodies){
      const s=Physics.toScreen(b.x,b.y,cam);
      if(s.x<-b.r*6||s.x>innerWidth+b.r*6||s.y<-b.r*6||s.y>innerHeight+b.r*6) continue;
      if(b.type==='blackhole') Render.blackHole({...b,x:s.x,y:s.y}, hue, {x:ship.x,y:ship.y});
      else Render.planet({...b,x:s.x,y:s.y}, hue);
    }

    // gate
    {
      const s=Physics.toScreen(gate.x,gate.y,cam);
      if(s.x>-200&&s.x<innerWidth+200&&s.y>-200&&s.y<innerHeight+200){
        Render.gate({...gate,x:s.x,y:s.y}, hue, gate.active);
      }
    }

    // shards
    for(const sh of shards){
      const s=Physics.toScreen(sh.x,sh.y,cam);
      if(s.x<-40||s.x>innerWidth+40||s.y<-40||s.y>innerHeight+40) continue;
      Render.shard({...sh,x:s.x,y:s.y}, hue);
    }

    // ship
    const ss=Physics.toScreen(ship.x,ship.y,cam);
    Render.ship({...ship,x:ss.x,y:ss.y}, hue);

    // particles (world coords, transformed + decayed inside renderer)
    Render.particles(particles, hue, cam);
    // prune dead particles
    for(let i=particles.length-1;i>=0;i--) if(particles[i].life<=0) particles.splice(i,1);

    Render.vignette(0);
  }

  init();
  return { };
})();
