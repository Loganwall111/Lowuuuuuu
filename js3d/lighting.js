/* ============================================================
   GRAVITON 3D :: lighting — emissive star lights + flicker
   ============================================================ */
const Lighting3D = (() => {
  const S = () => Core3D.get().scene;
  const lights=[];

  // Attach a flickering point light to a bright body mesh.
  function addStarLight(mesh, color, radius){
    const l = new BABYLON.PointLight('star_'+lights.length, mesh.position, S());
    l.diffuse = color;
    l.specular = color;
    l.intensity = 1.4;
    l.range = radius*6;
    l.falloffType = BABYLON.PointLight.PHYSICAL_FALLOFF;
    lights.push({ l, mesh, color, flick: U3.rand(0,6.28) });
    return l;
  }

  function update(dt, time){
    for(const s of lights){
      s.l.position.copyFrom(s.mesh.position);
      const f = 0.8 + 0.4*Math.sin(time*3+s.flick) + 0.2*Math.sin(time*9+s.flick*2);
      s.l.intensity = U3.clamp(f,0.5,1.7);
    }
  }

  return { addStarLight, update };
})();
