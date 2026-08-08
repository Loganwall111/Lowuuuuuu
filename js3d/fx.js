/* ============================================================
   GRAVITON 3D :: fx — volumetric particle bursts, trails, warp
   ============================================================ */
const FX3D = (() => {
  const S = () => Core3D.get().scene;
  let dot=null;

  function dotTexture(){
    if(dot) return dot;
    const t=new BABYLON.DynamicTexture('fxdot', {width:64,height:64}, S(), false);
    const c=t.getContext();
    const g=c.createRadialGradient(32,32,2,32,32,30);
    g.addColorStop(0,'rgba(255,255,255,1)');
    g.addColorStop(1,'rgba(255,255,255,0)');
    c.fillStyle=g; c.fillRect(0,0,64,64);
    t.update(); dot=t; return t;
  }

  // one-shot burst
  function burst(position, color, count, power, size, dur){
    const ps=new BABYLON.ParticleSystem('burst'+Math.random(), count, S());
    ps.particleTexture=dotTexture();
    ps.emitter=position.clone();
    ps.minEmitBox=new BABYLON.Vector3(-0.5,-0.5,-0.5);
    ps.maxEmitBox=new BABYLON.Vector3(0.5,0.5,0.5);
    ps.color1=new BABYLON.Color4(color.r,color.g,color.b,1);
    ps.color2=new BABYLON.Color4(color.r,color.g,color.b,0.8);
    ps.colorDead=new BABYLON.Color4(color.r,color.g,color.b,0);
    ps.minSize=size*0.5; ps.maxSize=size;
    ps.minLifeTime=dur; ps.maxLifeTime=dur*1.5;
    ps.emitRate=count;
    ps.manualEmitCount=count;
    ps.blendMode=BABYLON.ParticleSystem.BLENDMODE_ADD;
    ps.direction1=new BABYLON.Vector3(-power,-power,-power);
    ps.direction2=new BABYLON.Vector3(power,power,power);
    ps.minEmitPower=0.5; ps.maxEmitPower=power;
    ps.start();
    setTimeout(()=>{ try{ ps.stop(); ps.dispose(); }catch(e){} }, dur*2000);
    return ps;
  }

  // engine trail attached to ship
  let trail=null;
  function makeTrail(shipMesh){
    const ps=new BABYLON.ParticleSystem('trail', 700, S());
    ps.particleTexture=dotTexture();
    ps.emitter=shipMesh;
    ps.minEmitBox=new BABYLON.Vector3(-0.4,0,3.5);
    ps.maxEmitBox=new BABYLON.Vector3(0.4,0.4,3.8);
    ps.color1=new BABYLON.Color4(1,0.6,0.2,0.9);
    ps.color2=new BABYLON.Color4(0.4,1,1,0.7);
    ps.colorDead=new BABYLON.Color4(0.2,0.4,1,0);
    ps.minSize=0.3; ps.maxSize=0.9;
    ps.minLifeTime=0.4; ps.maxLifeTime=0.9;
    ps.emitRate=120;
    ps.blendMode=BABYLON.ParticleSystem.BLENDMODE_ADD;
    ps.direction1=new BABYLON.Vector3(0,0,1);
    ps.direction2=new BABYLON.Vector3(0,0,1);
    ps.minEmitPower=1; ps.maxEmitPower=3;
    ps.start();
    trail=ps;
    return ps;
  }
  function trailOn(){ if(trail) trail.start(); }
  function trailOff(){ if(trail) trail.stop(); }

  // warp ring around gate
  function ring(mesh, color, dur){
    // pulse scaling on the given mesh
    let t=0;
    const maxScale=3;
    const orig=mesh.scaling.clone();
    const h=S().registerBeforeRender(()=>{
      t+=0.016;
      const s=1+(maxScale-1)*(t/(dur));
      mesh.scaling.set(s,s,s);
      mesh.alpha= U3.clamp(1 - t/dur, 0, 1);
      if(t>dur){ S().unregisterBeforeRender(h); mesh.dispose(); }
    });
  }

  function clearTrail(){ if(trail){ try{trail.dispose();}catch(e){} trail=null; } }

  return { burst, makeTrail, trailOn, trailOff, clearTrail, ring };
})();
