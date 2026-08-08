/* ============================================================
   GRAVITON 3D :: physics — Havok-driven ship, gravity wells,
   thrust, collisions
   ============================================================ */
const Physics3D = (() => {
  const S = () => Core3D.get().scene;
  let plugin=null;
  let shipImp=null;
  let callbacks={ onCrash:null, onSwallow:null, onShard:null };
  const planetImps=[];

  async function init(HK){
    plugin=new BABYLON.HavokPlugin(true, HK);
    S().enablePhysics(new BABYLON.Vector3(0,0,0), plugin);
  }

  function setupShip(){
    const mesh=Ship3D.mesh();
    shipImp=new BABYLON.PhysicsImpostor(mesh, BABYLON.PhysicsImpostor.SphereImpostor,
      { mass:CFG3D.shipMass, restitution:0.2, friction:0.1 }, S());
    shipImp.setLinearDamping(0.3);
    shipImp.setAngularDamping(0.9);
    // constrain rotation (spacecraft shouldn't tumble from collisions)
    shipImp.setMass(CFG3D.shipMass);
  }

  function addCollider(mesh, isStatic){
    const imp=new BABYLON.PhysicsImpostor(mesh, BABYLON.PhysicsImpostor.SphereImpostor,
      { mass: isStatic?0:1, restitution:0.1, friction:0.4 }, S());
    planetImps.push(imp);
    return imp;
  }

  function addCollisionCallback(imp, fn){
    if(!shipImp||!imp) return;
    shipImp.registerOnPhysicsCollide(imp, ()=> fn());
  }

  function clearWorld(){
    for(const imp of planetImps){ try{ imp.dispose(); }catch(e){} }
    planetImps.length=0;
  }

  // One simulation step for the ship.
  // input, aim (world point), bodies (gravity sources), dt
  function updateShip(input, aim, bodies, dt, timeScale){
    if(!shipImp) return;
    const mesh=Ship3D.mesh();
    const pos=mesh.position;

    // aim direction (XZ plane)
    let dx=aim.x-pos.x, dz=aim.z-pos.z;
    const ad=Math.hypot(dx,dz)||1;
    dx/=ad; dz/=ad;

    // thrust force
    const force=new BABYLON.Vector3(0,0,0);
    const boosting=input.boost && input.forward;
    Ship3D.state.boosting=boosting;
    if(input.forward){
      let a=CFG3D.shipAccel*(boosting?CFG3D.boostMul:1)*CFG3D.shipMass;
      force.x+=dx*a; force.z+=dz*a;
    }

    // gravity from bodies (scaled by timeScale: Temporal Rift loosens gravity's grip)
    const gm=CFG3D.gravity*timeScale;
    for(const b of bodies){
      const bdx=b.x-pos.x, bdz=b.z-pos.z;
      const d2=bdx*bdx+bdz*bdz+4;
      const d=Math.sqrt(d2);
      const f=gm*b.mass/d2;
      force.x+=bdx/d*f*CFG3D.shipMass;
      force.z+=bdz/d*f*CFG3D.shipMass;
      // black hole pulls harder / drags
      if(b.type==='blackhole'){
        force.x+=bdx/d*f*CFG3D.shipMass*0.5;
        force.z+=bdz/d*f*CFG3D.shipMass*0.5;
      }
    }

    shipImp.applyForce(force, shipImp.getCenterOfMass());

    // clamp speed + keep on plane
    const v=shipImp.getLinearVelocity();
    const sp=Math.hypot(v.x,v.y,v.z);
    const max=CFG3D.shipMaxSpeed*(boosting?CFG3D.boostMul*0.5:1);
    let nv=v;
    if(sp>max){ nv=v.scale(max/sp); }
    nv.y=0;
    shipImp.setLinearVelocity(nv);

    // keep y on plane after solver
    mesh.position.y=0;
    Ship3D.state.position.copyFrom(mesh.position);
    Ship3D.state.velocity.copyFrom(nv);
  }

  function getVelocity(){ return shipImp? shipImp.getLinearVelocity() : new BABYLON.Vector3(); }
  function resetShip(pos){
    if(!shipImp) return;
    shipImp.setLinearVelocity(new BABYLON.Vector3(0,0,0));
    shipImp.setAngularVelocity(new BABYLON.Vector3(0,0,0));
    shipImp.setPosition(pos.clone());
    Ship3D.state.position.copyFrom(pos);
    Ship3D.state.velocity.set(0,0,0);
  }
  function setCallbacks(cb){ Object.assign(callbacks,cb); }

  return { init, setupShip, addCollider, addCollisionCallback, clearWorld,
           updateShip, getVelocity, resetShip, setCallbacks };
})();
