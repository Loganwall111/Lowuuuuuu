/* ============================================================
   GRAVITON 3D :: core — engine, scene, environment, shared assets
   ============================================================ */
const Core3D = (() => {
  let engine=null, scene=null, canvas=null, glowLayer=null, haLocked=false;

  function init(canvasEl){
    canvas=canvasEl;
    engine = new BABYLON.Engine(canvas, true, { stencil:true, preserveDrawingBuffer:true });
    scene = new BABYLON.Scene(engine);
    scene.clearColor = new BABYLON.Color4(0.01,0.003,0.03,1);
    scene.fogMode = BABYLON.Scene.FOGMODE_EXP2;
    scene.fogDensity = 0.0016;
    scene.fogColor = new BABYLON.Color3(0.01,0.003,0.03);
    buildEnvironment();
    return { engine, scene, canvas };
  }

  function buildEnvironment(){
    // skybox starfield (procedural)
    const sky = BABYLON.MeshBuilder.CreateSphere('sky', { diameter: 6000, segments: 24 }, scene);
    const starTex = makeStarTexture(2048);
    const skyMat = new BABYLON.StandardMaterial('skyMat', scene);
    skyMat.emissiveTexture = starTex;
    skyMat.backFaceCulling = false;
    skyMat.disableLighting = true;
    sky.material = skyMat;
    sky.infiniteDistance = true;
    sky.isPickable = false;

    // hemisphere fill light
    const hemi = new BABYLON.HemisphericLight('hemi', new BABYLON.Vector3(0,1,0), scene);
    hemi.intensity = 0.5;
    hemi.diffuse = new BABYLON.Color3(0.35,0.4,0.9);
    hemi.groundColor = new BABYLON.Color3(0.1,0.03,0.2);

    // accent point lights
    const accent = new BABYLON.PointLight('accent', new BABYLON.Vector3(0,20,0), scene);
    accent.diffuse = new BABYLON.Color3(1,0.3,0.9);
    accent.specular = new BABYLON.Color3(1,1,1);
    accent.intensity = 0.4;

    // glow bloom
    glowLayer = new BABYLON.GlowLayer('glow', scene, {
      mainTextureSamples:2, blurKernelSize:64, intensity:1.0,
    });
    glowLayer.intensity = 1.0;
  }

  // procedural star texture for skybox
  function makeStarTexture(size){
    const tex = new BABYLON.DynamicTexture('stars', { width:size, height:size }, scene, false);
    const ctx = tex.getContext();
    ctx.fillStyle = '#000003';
    ctx.fillRect(0,0,size,size);
    const n = 2600;
    for(let i=0;i<n;i++){
      const x=Math.random()*size, y=Math.random()*size;
      const r=Math.random();
      const col = Math.random()<0.12
        ? (Math.random()<0.5?'#ff9de0':'#9dccff')
        : (Math.random()<0.7?'#ffffff':'#cfd8ff');
      ctx.globalAlpha = U3.rand(0.25,1);
      ctx.fillStyle=col;
      ctx.beginPath();
      ctx.arc(x,y, r*1.8+0.2, 0, 6.283);
      ctx.fill();
      if(Math.random()<0.06){ // cross flare
        ctx.strokeStyle=col; ctx.globalAlpha=0.5; ctx.lineWidth=1;
        ctx.beginPath(); ctx.moveTo(x-4,y); ctx.lineTo(x+4,y); ctx.moveTo(x,y-4); ctx.lineTo(x,y+4); ctx.stroke();
      }
    }
    ctx.globalAlpha=1;
    tex.update();
    tex.coordinatesMode = BABYLON.Texture.SKYBOX_MODE;
    return tex;
  }

  // procedural planet surface texture
  function makePlanetTexture(hue){
    const size=512;
    const tex = new BABYLON.DynamicTexture('planet', {width:size,height:size}, scene, false);
    const ctx = tex.getContext();
    const base = `hsl(${hue} 45% ${U3.rand(30,40)}%)`;
    ctx.fillStyle=base; ctx.fillRect(0,0,size,size);
    const bands=8;
    for(let i=0;i<bands;i++){
      ctx.fillStyle=`hsla(${(hue+U3.randInt(-40,40)+360)%360} 55% ${U3.rand(38,55)}% 0.4)`;
      const y=U3.rand(0,size);
      ctx.beginPath();
      ctx.ellipse(size/2, y, size*0.6, U3.rand(6,40), 0, 0, 6.283);
      ctx.fill();
    }
    // craters / noise dots
    for(let i=0;i<180;i++){
      ctx.fillStyle=`hsla(${hue} 40% ${U3.rand(20,48)}% 0.35)`;
      ctx.beginPath();
      ctx.arc(U3.rand(0,size),U3.rand(0,size),U3.rand(1,9),0,6.283);
      ctx.fill();
    }
    tex.update();
    tex.wrapU = BABYLON.Texture.WRAP_ADDRESSMODE;
    tex.wrapV = BABYLON.Texture.WRAP_ADDRESSMODE;
    return tex;
  }

  function resize(){ if(engine) engine.resize(); }

  function get(){ return { engine, scene, glowLayer }; }
  function isLocked(){ return haLocked; }

  return { init, resize, get, makePlanetTexture, lock:()=>{haLocked=true;} };
})();
