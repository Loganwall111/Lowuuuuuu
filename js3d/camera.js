/* ============================================================
   GRAVITON 3D :: camera — chase cam, pointer-aim, keyboard input
   ============================================================ */
const Camera3D = (() => {
  const S = () => Core3D.get().scene;
  let cam=null;
  let aim={x:0,z:1};
  let shake=0;
  const input={ forward:false, boost:false, ability1:false, ability2:false, ability3:false };

  function init(){
    cam = new BABYLON.UniversalCamera('cam', new BABYLON.Vector3(0,40,-40), S());
    cam.setTarget(new BABYLON.Vector3(0,0,0));
    cam.minZ = 0.5;
    cam.fov = 1.0;
    cam.parent = null;
    bindInput();
  }

  function bindInput(){
    // pointer: compute aim by raycast to y=0 plane
    S().onPointerObservable.add((info)=>{
      if(info.type===BABYLON.PointerEventTypes.POINTERMOVE){
        const r = S().createPickingRay(info.event.offsetX, info.event.offsetY, BABYLON.Matrix.Identity(), cam);
        const plane = new BABYLON.Plane(0,1,0,0);
        const pt = r.intersectsPlane(plane);
        if(pt){ aim.x=pt.x; aim.z=pt.z; }
      }
    });

    window.addEventListener('keydown',(e)=>{
      if(e.code==='KeyW'||e.code==='ArrowUp') input.forward=true;
      if(e.code==='Space'){ input.boost=true; e.preventDefault(); }
      if(e.code==='Digit1') input.ability1=true;
      if(e.code==='Digit2') input.ability2=true;
      if(e.code==='Digit3') input.ability3=true;
    });
    window.addEventListener('keyup',(e)=>{
      if(e.code==='KeyW'||e.code==='ArrowUp') input.forward=false;
      if(e.code==='Space') input.boost=false;
      if(e.code==='Digit1') input.ability1=false;
      if(e.code==='Digit2') input.ability2=false;
      if(e.code==='Digit3') input.ability3=false;
    });
    window.addEventListener('blur',()=>{ input.forward=false; input.boost=false; });
  }

  // follow the ship, look toward ship + velocity, ease in, apply shake
  function update(ship, dt){
    const pos = ship.state.position;
    const back = new BABYLON.Vector3(0, 26, -34);
    const want = pos.add(back);
    cam.position = BABYLON.Vector3.Lerp(cam.position, want, Math.min(1, 5*dt));
    // look at slightly ahead of ship along velocity / aim
    const look = pos.add(ship.state.velocity.scale(0.6)).add(new BABYLON.Vector3(0,6,0));
    cam.setTarget(look);
    // shake
    if(shake>0.2){ cam.position.x += U3.rand(-shake,shake); cam.position.y += U3.rand(-shake,shake); cam.position.z += U3.rand(-shake,shake); }
    shake *= Math.pow(0.001, dt);
  }

  function addShake(v){ shake = Math.min(26, shake+v); }
  function getAim(){ return aim; }

  return { init, update, addShake, getAim, cam, input };
})();
