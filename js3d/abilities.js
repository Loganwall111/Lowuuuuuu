/* ============================================================
   GRAVITON 3D :: abilities — Singularity, Temporal Rift, Void Lash
   ============================================================ */
const Abilities3D = (() => {
  const S = () => Core3D.get().scene;
  const defs=CFG3D.abilities;
  const cds={};       // id -> remaining cooldown
  const actives={};   // id -> remaining active time
  let timeScale=1;
  let lashBeam=null;

  for(const d of defs){ cds[d.id]=0; }

  // --- Singularity: temporary mini black hole ---
  let tempBH=null, tempBHTimer=0;
  let lashActive=false;

  // --- Void Lash: beam mesh ---
  function makeBeam(){
    const mesh=BABYLON.MeshBuilder.CreateCylinder('lash', { height:1, diameter:0.3, tessellation:12 }, S());
    const mat=new BABYLON.StandardMaterial('lashmat', S());
    mat.emissiveColor=new BABYLON.Color3(0.7,0.2,1);
    mat.disableLighting=true;
    mesh.material=mat;
    mesh.isVisible=false;
    mesh.isPickable=false;
    return mesh;
  }

  function tryActivate(id, ship, ctx){
    if(cds[id]>0) return false;
    cds[id]=defs.find(d=>d.id===id).cooldown;
    const d=defs.find(x=>x.id===id);
    actives[id]=d.dur;
    Audio3D.sfx.ability();

    if(id==='singularity'){
      // spawn a temporary mini black hole slightly ahead
      const vel=ship.state.velocity;
      const vs=Math.hypot(vel.x,vel.y,vel.z);
      const dir=vs>1? vel.normalize().scale(30): new BABYLON.Vector3(1,0,0).scale(30);
      const ahead=ship.state.position.add(dir);
      tempBH=Bodies3D.makeBlackHole(ahead.x, ahead.z, 7);
      tempBH.mass=tempBH.radius*tempBH.radius*3.5;
      tempBHTimer=d.dur;
      if(ctx && ctx.onSingularity) ctx.onSingularity(tempBH);
      ctx.msg(`✦ SINGULARITY UNLEASHED`, 1500);
      return true;
    }
    if(id==='rift'){
      ctx.msg(`⏳ TEMPORAL RIFT — time bends`, 1200);
      return true;
    }
    if(id==='lash'){
      lashBeam = lashBeam || makeBeam();
      lashActive=true;
      return true;
    }
    return true;
  }

  // returns world timeScale multiplier for this frame
  function update(dt, ctx, ship){
    // cooldowns
    for(const id in cds) cds[id]=Math.max(0, cds[id]-dt);
    // actives
    timeScale=1;
    for(const id in actives){
      actives[id]=Math.max(0, actives[id]-dt);
      if(actives[id]<=0){
        delete actives[id];
        if(id==='lash') lashActive=false;
      }
    }
    if(actives['rift']) timeScale=0.22;

    // singularity expiry
    if(tempBH){
      tempBHTimer-=dt;
      if(tempBHTimer<=0){
        try{ tempBH.mesh.dispose(); if(tempBH.ps){tempBH.ps.stop(); tempBH.ps.dispose();} }catch(e){}
        // remove from bodies list
        const bl=Bodies3D.list();
        const i=bl.indexOf(tempBH); if(i>=0) bl.splice(i,1);
        if(ctx && ctx.onSingularityEnd) ctx.onSingularityEnd(tempBH);
        tempBH=null;
      }
    }

    // lash beam visual
    if(lashBeam){
      if(lashActive){
        lashBeam.isVisible=true;
        const p=ship.state.position;
        const vel=ship.state.velocity;
        const sp=Math.hypot(vel.x,vel.y,vel.z);
        const dir=sp>0.01? vel.scale(1/sp): new BABYLON.Vector3(1,0,0);
        lashBeam.position.copyFrom(p);
        lashBeam.lookAt(p.add(dir.scale(60)));
        lashBeam.scaling.y=60;
      }else{
        lashBeam.isVisible=false;
      }
    }
    return timeScale;
  }

  function cooldownOf(id){ return cds[id]||0; }
  function activeOf(id){ return actives[id]||0; }
  function isActive(id){ return !!activeOf(id); }

  return { tryActivate, update, cooldownOf, activeOf, isActive, getTimeScale:()=>timeScale,
           consumeLash:()=>{ actives['lash']=0; } };
})();
