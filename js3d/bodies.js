/* ============================================================
   GRAVITON 3D :: bodies — planets & black holes
   Black holes: event horizon + photon sphere + glowing accretion
   disk + volumetric swirling particle system.
   ============================================================ */
const Bodies3D = (() => {
  const S = () => Core3D.get().scene;
  const bodies=[];
  let particleDot=null;

  function dotTexture(){
    if(particleDot) return particleDot;
    const t=new BABYLON.DynamicTexture('dot', {width:64,height:64}, S(), false);
    const c=t.getContext();
    const g=c.createRadialGradient(32,32,2,32,32,30);
    g.addColorStop(0,'rgba(255,255,255,1)');
    g.addColorStop(0.4,'rgba(255,255,255,0.6)');
    g.addColorStop(1,'rgba(255,255,255,0)');
    c.fillStyle=g; c.fillRect(0,0,64,64);
    t.update();
    particleDot=t;
    return t;
  }

  // ---------- PLANET ----------
  function makePlanet(x,z,radius){
    const hue=U3.randInt(0,359);
    const pos=new BABYLON.Vector3(x,0,z);
    const mesh=BABYLON.MeshBuilder.CreateSphere('planet', { diameter:radius*2, segments:32 }, S());

    const mat=new BABYLON.StandardMaterial('pmat', S());
    mat.diffuseTexture=Core3D.makePlanetTexture(hue);
    mat.specularColor=new BABYLON.Color3(0.2,0.2,0.25);
    mat.specularPower=40;
    mat.emissiveColor=new BABYLON.Color3(0.05,0.04,0.1);
    mesh.material=mat;
    mesh.position.copyFrom(pos);

    // atmosphere
    const atmo=BABYLON.MeshBuilder.CreateSphere('atmo', { diameter:radius*2*1.18, segments:24 }, S());
    const amat=new BABYLON.StandardMaterial('amat', S());
    amat.emissiveColor=new BABYLON.Color3(0.3,0.5,1).scale(0.8);
    amat.alpha=0.16;
    amat.backFaceCulling=false;
    amat.disableLighting=true;
    atmo.material=amat;
    atmo.parent=mesh;
    atmo.isPickable=false;

    const b={ type:'planet', x, z, radius, mesh, hue };
    bodies.push(b);
    return b;
  }

  // ---------- BLACK HOLE ----------
  function makeBlackHole(x,z,radius){
    const hue=U3.randInt(260,320);
    const pos=new BABYLON.Vector3(x,0,z);

    // event horizon (black)
    const horizon=BABYLON.MeshBuilder.CreateSphere('bh_horizon', { diameter:radius*2, segments:48 }, S());
    const hmat=new BABYLON.StandardMaterial('bhmat', S());
    hmat.emissiveColor=new BABYLON.Color3(0,0,0);
    hmat.disableLighting=true;
    hmat.specularColor=new BABYLON.Color3(0,0,0);
    horizon.material=hmat;
    horizon.position.copyFrom(pos);

    // photon sphere (thin glowing shell)
    const photon=BABYLON.MeshBuilder.CreateSphere('bh_photon', { diameter:radius*2*1.35, segments:48 }, S());
    const pmat=new BABYLON.StandardMaterial('phmat', S());
    pmat.emissiveColor=new BABYLON.Color3(1,0.6,0.2).scale(0.9);
    pmat.disableLighting=true;
    pmat.alpha=0.10;
    pmat.backFaceCulling=false;
    photon.material=pmat;
    photon.parent=horizon;

    // accretion disk (flat glowing ring)
    const disk=BABYLON.MeshBuilder.CreateDisc('bh_disk', { radius:radius*2.4, tessellation:48 }, S());
    disk.rotation.x=Math.PI/2*0.92;
    disk.parent=horizon;
    const dmat=new BABYLON.StandardMaterial('dmat', S());
    dmat.diffuseTexture=makeRingTexture(hue);
    dmat.emissiveColor=new BABYLON.Color3(1,0.5,0.25);
    dmat.diffuseColor=new BABYLON.Color3(1,0.5,0.25);
    dmat.backFaceCulling=false;
    dmat.alpha=0.9;
    dmat.useAlphaFromDiffuseTexture=true;
    disk.material=dmat;
    disk.isPickable=false;
    disk.rotation.y=U3.rand(0,6.28);

    // volumetric swirling particle system
    const ps=new BABYLON.ParticleSystem('bh_ps', 900, S());
    ps.particleTexture=dotTexture();
    ps.emitter=horizon;
    ps.minEmitBox=new BABYLON.Vector3(-radius*2.4, -0.5, -radius*2.4);
    ps.maxEmitBox=new BABYLON.Vector3(radius*2.4, 0.5, radius*2.4);
    ps.color1=new BABYLON.Color4(1,0.9,0.6,0.9);
    ps.color2=new BABYLON.Color4(1,0.5,0.25,0.8);
    ps.colorDead=new BABYLON.Color4(0.3,0.1,0.05,0);
    ps.minSize=radius*0.16; ps.maxSize=radius*0.5;
    ps.minLifeTime=0.8; ps.maxLifeTime=2.0;
    ps.emissionRate=160;
    ps.blendMode=BABYLON.ParticleSystem.BLENDMODE_ADD;
    ps.direction1=new BABYLON.Vector3(-1,-0.1,-1);
    ps.direction2=new BABYLON.Vector3(1,0.1,1);
    ps.minEmitPower=radius*3; ps.maxEmitPower=radius*4.5;
    ps.gravity=new BABYLON.Vector3(0,0,0);

    // custom swirl update
    const cx=x, cz=z;
    ps.updateFunction=(particles)=>{
      for(const p of particles){
        const dx=cx-p.position.x, dz=cz-p.position.z;
        const d=Math.sqrt(dx*dx+dz*dz)+0.01;
        // tangential (perpendicular) + inward pull
        const tang=(d<radius*2.2)?1:-1;
        const ax = (-dz/d)*tang*0.55 + (dx/d)*1.2;
        const az = ( dx/d)*tang*0.55 + (dz/d)*1.2;
        p.velocity.x += ax*0.016;
        p.velocity.z += az*0.016;
        p.velocity.y += (-p.velocity.y*0.05)*0.016;
        // tighten orbit as it nears
        if(d>radius*1.4){
          p.velocity.x -= dx/d*0.6*0.016;
          p.velocity.z -= dz/d*0.6*0.016;
        }else{
          p.velocity.x -= dx/d*0.1*0.016;
          p.velocity.z -= dz/d*0.1*0.016;
        }
        p.velocity.x*=0.995; p.velocity.z*=0.995;
      }
    };
    ps.start();

    // slow spin of disk
    S().registerBeforeRender(()=>{ disk.rotation.y+=0.002; });

    const b={ type:'blackhole', x, z, radius, mesh:horizon, ps, hue };
    bodies.push(b);
    return b;
  }

  function makeRingTexture(hue){
    const size=256;
    const t=new BABYLON.DynamicTexture('ring', {width:size,height:size}, S(), false);
    const c=t.getContext();
    c.clearRect(0,0,size,size);
    const g=c.createRadialGradient(size/2,size/2,size*0.22,size/2,size/2,size/2);
    g.addColorStop(0,'rgba(0,0,0,0)');
    g.addColorStop(0.45,`hsla(${hue} 100% 60% 0.0)`);
    g.addColorStop(0.5,`hsla(${hue} 100% 60% 0.9)`);
    g.addColorStop(0.62,`hsla(${(hue+30)%360} 100% 70% 0.7)`);
    g.addColorStop(0.75,`hsla(${(hue+60)%360} 100% 80% 0.3)`);
    g.addColorStop(1,'rgba(0,0,0,0)');
    c.fillStyle=g;
    c.beginPath(); c.arc(size/2,size/2,size/2,0,6.283); c.fill();
    t.update();
    return t;
  }

  function clear(){
    for(const b of bodies){
      if(b.mesh) b.mesh.dispose();
      if(b.ps){ try{b.ps.stop(); b.ps.dispose();}catch(e){} }
    }
    bodies.length=0;
  }
  function list(){ return bodies; }
  function update(dt, time){
    for(const b of bodies){
      if(b.type==='blackhole' && b.ps) b.ps.setParticles();
    }
  }

  return { makePlanet, makeBlackHole, clear, list, update };
})();
