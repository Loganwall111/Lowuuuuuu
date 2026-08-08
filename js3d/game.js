/* ============================================================
   GRAVITON 3D :: game — swarm leader / orchestrator
   Boots Babylon + Havok, runs the state machine, generates the
   world, and coordinates all specialist modules.
   ============================================================ */
const Game3D = (() => {
  let engine=null, scene=null;
  let state='title';
  let sector=0;
  let score=0, shardCount=0;
  let shards=[];        // {mesh, r, collected}
  let gate=null;        // {x,z,r,active,meshes[], imp}
  let pending=null;
  let warpT=0, time=0;
  let gravityBodies=[]; // {x,z,mass,type}
  let worldColliders=[];

  async function boot(){
    const canvas=document.getElementById('game');
    const core=Core3D.init(canvas);
    engine=core.engine; scene=core.scene;

    const HK=await HavokPhysics();
    await Physics3D.init(HK);

    Camera3D.init();
    Ship3D.build();
    Physics3D.setupShip();
    Physics3D.setCallbacks({
      onCrash:()=>crash(),
      onSwallow:()=>swallowed(),
      onShard:()=>{},
    });
    FX3D.makeTrail(Ship3D.mesh());
    UI3D.init();

    // background static ambiance
    window.addEventListener('resize', Core3D.resize);

    UI3D.showTitle();
    UI3D.hideHUD();

    engine.runRenderLoop(loop);
    window.__START3D__=startGame;
  }

  // ---------- sector generation ----------
  function loadSector(s,isStart){
    clearWorld();
    sector=s;
    gravityBodies=[];
    worldColliders=[];
    shards=[];

    const isFinal = s===CFG3D.sectors-1;

    // black hole
    const bh=Bodies3D.makeBlackHole(U3.rand(-120,120), U3.rand(-120,120), U3.rand(10,16));
    bh.mass=bh.radius*bh.radius*2.2;
    gravityBodies.push(bh);

    // planets
    for(let i=0;i<2+Math.floor(s/2);i++){
      const p=Bodies3D.makePlanet(U3.rand(-260,260), U3.rand(-260,260), U3.rand(14,26));
      p.mass=p.radius*p.radius*2.2;
      gravityBodies.push(p);
      const imp=Physics3D.addCollider(p.mesh, true);
      Physics3D.addCollisionCallback(imp, ()=>crash());
      worldColliders.push(imp);
      // star light
      if(Math.random()<0.7) Lighting3D.addStarLight(p.mesh, new BABYLON.Color3(0.4,0.5,1), p.radius);
    }

    // shards
    for(let i=0;i<9+s*2;i++){
      const x=U3.rand(-320,320), z=U3.rand(-320,320);
      const mesh=BABYLON.MeshBuilder.CreateOctahedron('shard', { diameter:2.2 }, scene);
      const mat=new BABYLON.StandardMaterial('shmat'+i, scene);
      const hue=U3.randInt(0,359);
      mat.diffuseColor=BABYLON.Color3.FromHSV(hue,1,0.8);
      mat.emissiveColor=BABYLON.Color3.FromHSV(hue,1,0.7);
      mat.disableLighting=false;
      mat.specularPower=90;
      mesh.material=mat;
      mesh.position.set(x, 0, z);
      mesh.rotation.set(U3.rand(0,6.28), U3.rand(0,6.28), U3.rand(0,6.28));
      mesh.isPickable=false;
      shards.push({ mesh, r:2.2, collected:false, hue });
    }

    // gate
    const gx=U3.rand(380,520)*(Math.random()<0.5?-1:1);
    const gz=U3.rand(380,520)*(Math.random()<0.5?-1:1);
    gate={ x:gx, z:gz, r:isFinal?10:16, active:false, meshes:[], ring1:null };
    buildGate(gx,gz,isFinal);

    // reset ship
    Ship3D.reset(new BABYLON.Vector3(0,0,0));
    Physics3D.resetShip(new BABYLON.Vector3(0,0,0));
    pending=Puzzles3D.random(s);

    if(isFinal) UI3D.msg('THE SINGULARITY AWAITS',3000);
    else UI3D.msg(`SECTOR ${CFG3D.sectorRoman[s]} · ${CFG3D.sectorNames[s]} · reach the jump gate`,3500);
  }

  function buildGate(x,z,r){
    const ring1=BABYLON.MeshBuilder.CreateTorus('g1', { diameter:r*2, thickness:0.8, tessellation:40 }, scene);
    const ring2=ring1.clone('g2');
    ring2.rotation.y=Math.PI/2;
    const mat=new BABYLON.StandardMaterial('gmat', scene);
    mat.emissiveColor=BABYLON.Color3.FromHSV(300,1,0.9);
    mat.disableLighting=true;
    mat.alpha=0.85;
    ring1.material=mat; ring2.material=mat.clone('gmat2');
    ring1.position.set(x,0,z); ring2.position.set(x,0,z);
    ring1.isPickable=false; ring2.isPickable=false;
    gate.meshes=[ring1,ring2];
    gate.ring1=ring1;
    // physics ghost collider for gate (non-solid trigger via distance)
  }

  function clearWorld(){
    Bodies3D.clear();
    for(const sh of shards){ if(sh.mesh) sh.mesh.dispose(); }
    shards=[];
    if(gate){ for(const m of gate.meshes) m.dispose(); }
    gate=null;
    Physics3D.clearWorld();
  }

  // ---------- flow ----------
  function startGame(){
    Audio3D.init();
    UI3D.hideTitle(); UI3D.showHUD();
    score=0; shardCount=0; sector=0;
    loadSector(0,true);
    state='playing';
    UI3D.msg('GRAVITON 3D · BEGIN YOUR ODYSSEY',2600);
  }

  function openGate(){
    state='puzzle';
    pending=Puzzles3D.random(sector);
    Audio3D.sfx.gate();
    UI3D.renderPuzzle(pending,(ok)=>{
      if(ok){
        Audio3D.sfx.good();
        gate.active=true;
        gate.ring1.material.emissiveColor=BABYLON.Color3.FromHSV(160,1,0.95);
        if(gate.meshes[1].material) gate.meshes[1].material.emissiveColor=BABYLON.Color3.FromHSV(160,1,0.95);
        UI3D.hideOverlay();
        state='playing';
        score+=CFG3D.gateScore;
        UI3D.msg('GATE UNLOCKED · FLY THROUGH',2000);
      }
    }, ()=>{
      UI3D.hideOverlay(); state='playing';
      UI3D.msg('the sigil mocks you...',1500);
    });
  }

  function nextSector(){
    if(sector>=CFG3D.sectors-1){ win(); return; }
    loadSector(sector+1,false);
    state='playing';
  }

  function win(){
    state='gameover';
    UI3D.showOverlay('★ THE SINGULARITY ★',
      `<p>You merged with the <b>Primal Light</b> beyond the event horizon.</p>
       <p style="margin-top:10px">Final score: <b style="color:#ffd54f">${score.toLocaleString()}</b></p>
       <p class="mini">${shardCount} prism shards harvested</p>
       <div class="btn-row"><button class="btn" onclick="window.__RESTART3D__()">ODYSSEY AGAIN</button></div>`);
    window.__RESTART3D__=startGame;
    Audio3D.sfx.warp();
  }

  function respawn(msg,penalty){
    const pos=new BABYLON.Vector3(0,0,0);
    Ship3D.reset(pos); Physics3D.resetShip(pos);
    score=Math.max(0,score-penalty);
    UI3D.msg(msg,2200);
    Camera3D.addShake(20);
    FX3D.burst(pos, BABYLON.Color3.FromHSV(U3.randInt(0,360),1,1), 60, 60, 4, 0.8);
    UI3D.dilation(false);
  }
  function crash(){ if(state!=='playing')return; respawn('⚡ HULL BREACH · planetfall',200); }
  function swallowed(){ if(state!=='playing')return; respawn('☍ SWALLOWED BY THE SINGULARITY · reknitted',300); }

  // ---------- loop ----------
  function loop(){
    const dt=Math.min(0.033, 0.016);

    if(state==='title'){
      scene.render();
      return;
    }
    if(state==='paused'||state==='gameover'){
      scene.render();
      return;
    }
    time+=dt;

    if(state==='puzzle'){
      scene.render();
      return;
    }
    if(state==='gatein'){ updateWarp(dt); scene.render(); return; }

    updatePlaying(dt);
    scene.render();
  }

  function updatePlaying(dt){
    const input=Camera3D.input;
    const aim=Camera3D.getAim();

    // abilities
    const abiCtx={
      msg:UI3D.msg,
      onSingularity:(body)=>{ if(gravityBodies.indexOf(body)<0) gravityBodies.push(body); },
      onSingularityEnd:(body)=>{ const i=gravityBodies.indexOf(body); if(i>=0) gravityBodies.splice(i,1); },
    };
    for(const a of CFG3D.abilities){
      if(input['ability'+CFG3D.abilities.indexOf(a)+1]){ Abilities3D.tryActivate(a.id, Ship3D, abiCtx); }
    }
    const ts=Abilities3D.update(dt, abiCtx, Ship3D);

    // physics
    Physics3D.updateShip(input, aim, gravityBodies, dt, ts);
    const sp=Math.hypot(Ship3D.state.velocity.x, Ship3D.state.velocity.z);

    // engine sound / boost
    if(input.boost && input.forward) Audio3D.sfx.boost();

    // energy
    let burn=CFG3D.energyDrainIdle;
    if(input.forward) burn+=CFG3D.energyBurn;
    if(input.boost && input.forward) burn+=CFG3D.energyBoostBurn;
    Ship3D.state.energy=Math.max(0, Ship3D.state.energy-burn*dt);

    // shard collection + magnetize
    const sp3=Ship3D.state.position;
    for(const sh of shards){
      if(sh.collected) continue;
      const d=Math.hypot(sh.mesh.position.x-sp3.x, sh.mesh.position.z-sp3.z);
      if(d<90 && d>1){ // magnetize
        sh.mesh.position.x+=(sp3.x-sh.mesh.position.x)*dt*4;
        sh.mesh.position.z+=(sp3.z-sh.mesh.position.z)*dt*4;
      }
      if(d<12){
        sh.collected=true;
        shardCount++; score+=CFG3D.shardScore;
        Ship3D.state.energy=Math.min(CFG3D.energyMax, Ship3D.state.energy+CFG3D.shardEnergy);
        Audio3D.sfx.shard();
        FX3D.burst(sh.mesh.position, BABYLON.Color3.FromHSV(sh.hue,1,1), 30, 40, 3, 0.6);
        sh.mesh.dispose();
      }
    }
    shards=shards.filter(sh=>!sh.collected);

    // Void Lash: annihilate shards in a forward cone
    if(Abilities3D.isActive('lash')){
      const vel=Ship3D.state.velocity;
      const lsp=Math.hypot(vel.x,vel.y,vel.z);
      const ldir=lsp>0.5? new BABYLON.Vector3(vel.x/lsp,0,vel.z/lsp): new BABYLON.Vector3(1,0,0);
      for(const sh of shards){
        if(sh.collected) continue;
        const rel=new BABYLON.Vector3(sh.mesh.position.x-sp3.x,0,sh.mesh.position.z-sp3.z);
        const d=rel.length();
        if(d<0.01) continue;
        const cos=rel.dot(ldir)/d;
        if(cos>0.85 && d<60){
          sh.collected=true; shardCount++; score+=CFG3D.shardScore;
          Audio3D.sfx.shard();
          FX3D.burst(sh.mesh.position, BABYLON.Color3.FromHSV(280,1,1), 25, 50, 3, 0.5);
          sh.mesh.dispose();
        }
      }
      shards=shards.filter(sh=>!sh.collected);
    }

    // black hole proximity: dilation + swallow
    let dilate=false;
    for(const b of gravityBodies){
      if(b.type==='blackhole'){
        const d=Math.hypot(b.x-sp3.x, b.z-sp3.z);
        if(d < b.radius + CFG3D.horizonExtra){ swallowed(); return; }
        if(d < b.radius*3){ dilate=true; if(Math.random()<dt*2) Audio3D.sfx.dilate(); }
      }
    }
    UI3D.dilation(dilate || ts<1);

    // low energy
    if(Ship3D.state.energy<15 && Math.random()<dt) Audio3D.sfx.warning();
    if(Ship3D.state.energy<=0){ respawn('✹ CORE BLACKOUT · energy exhausted',150); }

    // gate
    const dg=Math.hypot(gate.x-sp3.x, gate.z-sp3.z);
    if(dg < gate.r+14){
      if(!gate.active){ openGate(); return; }
      else { startWarp(); return; }
    }

    // camera
    Camera3D.update(Ship3D, dt);
    Ship3D.update(dt, time);

    // gate pulse
    if(gate.ring1){ const pulse=1+0.04*Math.sin(time*4); gate.ring1.scaling.set(pulse,pulse,pulse); }

    // HUD
    const cds={};
    for(const a of CFG3D.abilities) cds[a.id]=Abilities3D.cooldownOf(a.id);
    UI3D.setHUD({ sector:CFG3D.sectorRoman[sector], energy:Ship3D.state.energy, shards:shardCount, score, abilityCD:cds });
  }

  function startWarp(){
    state='gatein';
    warpT=0;
    Audio3D.sfx.warp();
    FX3D.burst(gate.meshes[0].position, BABYLON.Color3.FromHSV(300,1,1), 80, 80, 5, 1.0);
  }
  function updateWarp(dt){
    warpT+=dt;
    const p=Ship3D.state.position;
    p.x+=(gate.x-p.x)*6*dt;
    p.z+=(gate.z-p.z)*6*dt;
    Physics3D.resetShip(p);
    Ship3D.update(dt,time);
    Camera3D.addShake(14*dt);
    Camera3D.update(Ship3D,dt);
    if(warpT>1.6){
      if(sector>=CFG3D.sectors-1) win();
      else nextSector();
    }
  }

  return { boot };
})();

// auto-boot once the DOM is ready
if(document.readyState==='loading'){
  document.addEventListener('DOMContentLoaded', ()=>Game3D.boot());
}else{
  Game3D.boot();
}
