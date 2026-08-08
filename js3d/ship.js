/* ============================================================
   GRAVITON 3D :: ship — player vessel mesh + state
   ============================================================ */
const Ship3D = (() => {
  const S = () => Core3D.get().scene;
  let mesh=null;
  const state={
    position:new BABYLON.Vector3(0,0,0),
    velocity:new BABYLON.Vector3(0,0,0),
    energy:CFG3D.energyMax,
    alive:true,
    boosting:false,
    engineLight:null,
    flame:null,
  };

  function build(){
    const hull = BABYLON.MeshBuilder.CreateBox('hull', { width:2.6, depth:7, height:0.8 }, S());
    const hmat=new BABYLON.StandardMaterial('hullmat', S());
    hmat.diffuseColor=new BABYLON.Color3(0.85,0.95,1);
    hmat.emissiveColor=new BABYLON.Color3(0.2,0.3,0.5);
    hmat.specularColor=new BABYLON.Color3(1,1,1);
    hmat.specularPower=60;
    hull.material=hmat;
    hull.rotation.x=Math.PI/2;

    const nose = BABYLON.MeshBuilder.CreateCone('nose', { diameterTop:0.1, diameterBottom:2.2, height:4 }, S());
    const nmat=new BABYLON.StandardMaterial('nmat', S());
    nmat.diffuseColor=new BABYLON.Color3(0.9,0.3,0.7);
    nmat.emissiveColor=new BABYLON.Color3(0.5,0.1,0.4);
    nmat.specularPower=50;
    nose.material=nmat;
    nose.rotation.x=Math.PI/2;
    nose.position.z=-5.5;

    // cockpit glow
    const cockpit=BABYLON.MeshBuilder.CreateSphere('cockpit', { diameter:0.9 }, S());
    const cmat=new BABYLON.StandardMaterial('cmat', S());
    cmat.emissiveColor=new BABYLON.Color3(0.4,1,1);
    cmat.disableLighting=true;
    cockpit.material=cmat;
    cockpit.position.set(0,0.4,-2);
    cockpit.scaling.set(1,0.7,1.6);

    // engine flame (emissive, no light for cheap glow)
    const flame=BABYLON.MeshBuilder.CreateSphere('flame', { diameter:1.2 }, S());
    const fmat=new BABYLON.StandardMaterial('fmat', S());
    fmat.emissiveColor=new BABYLON.Color3(1,0.5,0.1);
    fmat.disableLighting=true;
    flame.material=fmat;
    flame.position.z=4.2;
    flame.scaling.set(1,1,1.6);

    // engine point light
    const light=new BABYLON.PointLight('engine', new BABYLON.Vector3(0,0,4), S());

    // root group (preserves per-part materials — no merge collapse)
    mesh=new BABYLON.Mesh('shipRoot', S());
    hull.parent=mesh; nose.parent=mesh; cockpit.parent=mesh; flame.parent=mesh;
    light.parent=mesh;
    state.engineLight=light;
    state.flame=flame;
    mesh.isPickable=false;
    return mesh;
  }

  function update(dt, time){
    if(!mesh) return;
    mesh.position.copyFrom(state.position);
    // face movement direction (yaw only)
    const v=state.velocity;
    const sp=Math.hypot(v.x,v.z);
    if(sp>0.5){
      const target=Math.atan2(v.x, v.z);
      const cur=mesh.rotation.y;
      let d=((target-cur+Math.PI*3)%(Math.PI*2))-Math.PI;
      mesh.rotation.y += U3.clamp(d, -4*dt, 4*dt);
    }
    // flame pulse
    const on=(state.boosting || sp>8);
    state.flame.scaling.y = on? (2+2*Math.sin(time*30)) : 1.4;
    state.flame.scaling.x = on? (1.4+0.3*Math.sin(time*40)) : 0.9;
    state.engineLight.intensity = on? 2.0:0.6;
  }

  function reset(pos){
    state.position.copyFrom(pos);
    state.velocity.set(0,0,0);
    state.energy=CFG3D.energyMax;
    state.alive=true;
    if(mesh){ mesh.position.copyFrom(pos); mesh.rotation.y=0; }
  }

  return { build, update, reset, state, mesh:()=>mesh };
})();
