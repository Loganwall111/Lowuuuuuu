// Runtime smoke test for the 3D Babylon build using a lightweight API stub.
// Catches JS logic errors, undefined refs, and property mistakes.
"use strict";
const fs = require("fs");
const path = require("path");

class Vector3 {
  constructor(x=0,y=0,z=0){this.x=x;this.y=y;this.z=z;}
  clone(){return new Vector3(this.x,this.y,this.z);} add(v){return new Vector3(this.x+v.x,this.y+v.y,this.z+v.z);}
  subtract(v){return new Vector3(this.x-v.x,this.y-v.y,this.z-v.z);} scale(s){return new Vector3(this.x*s,this.y*s,this.z*s);}
  addInPlace(v){this.x+=v.x;this.y+=v.y;this.z+=v.z;return this;} length(){return Math.hypot(this.x,this.y,this.z);}
  normalize(){const l=this.length()||1;return new Vector3(this.x/l,this.y/l,this.z/l);} set(x,y,z){this.x=x;this.y=y;this.z=z;}
  copyFrom(v){this.x=v.x;this.y=v.y;this.z=v.z;}
  static Up(){return new Vector3(0,1,0);} static Zero(){return new Vector3(0,0,0);}
  static Distance(a,b){return Math.hypot(a.x-b.x,a.y-b.y,a.z-b.z);}
  static Dot(a,b){return a.x*b.x+a.y*b.y+a.z*b.z;}
  static Cross(a,b){return new Vector3(a.y*b.z-a.z*b.y,a.z*b.x-a.x*b.z,a.x*b.y-a.y*b.x);}
  static Lerp(a,b,t){return new Vector3(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t,a.z+(b.z-a.z)*t);}
  static Clamp(v,min,max){return Math.max(min,Math.min(max,v));}
  static Normalize(v){return v.normalize();}
  static Project(v){return new Vector3(600+v.x,400+v.z,0);}
}
const Vector2 = { Zero:()=>({x:0,y:0}) };
const Color3 = { c:0 }; function mkColor3(r=1,g=1,b=1){ return {r,g,b, scale(s){return {r:this.r*s,g:this.g*s,b:this.b*s};}} }
function Color3C(r,g,b){return mkColor3(r,g,b);} Color3C.Black = () => mkColor3(0,0,0); Color3C.White=()=>mkColor3(1,1,1);
Color3C.Blue=()=>mkColor3(0,0,1);
function Color4(r=1,g=1,b=1,a=1){return {r,g,b,a};}
const Matrix = { Identity:()=>({}) };

const rbs = []; // registered beforeRender
let delta = 16;
class Mesh {
  constructor(name,type){ this.name=name; this.position=new Vector3(); this.rotation=new Vector3(); this.scaling=new Vector3(1,1,1);
    this.material=null; this.isPickable=true; this.infiniteDistance=false; this.parent=null; this.disposed=false; this.anim=0; }
  setParent(p){ this.parent=p; }
  dispose(){ this.disposed=true; }
  get isDisposed(){ return this.disposed; }
  lookAt(){} }
const MeshBuilder = {};
["CreateSphere","CreateBox","CreateIcoSphere","CreatePolyhedron","CreateCone","CreateTorus","CreateTorusKnot","CreateGround","CreateDisc","CreateCylinder","CreatePlane"].forEach(m => {
  MeshBuilder[m] = (name,o,sc) => new Mesh(name, m);
});
class Material { constructor(n,sc){ this.name=n; this.emissiveColor=mkColor3(); this.diffuseColor=mkColor3(); this.specularColor=mkColor3(); this.disableLighting=false; this.alpha=1; this.backFaceCulling=true; this.specularPower=0; this.diffuseTexture=null; this.disposed=false; } dispose(){this.disposed=true;} }
const StandardMaterial = Material;
class DynamicTexture { constructor(n,o,sc){ this.w=(o&&o.width)||256; this.h=(o&&o.height)||256; } getContext(){ return canvas2d(); } update(){} }
const ctx2d = { fillStyle:"", strokeStyle:"", globalAlpha:1, fillRect(){}, beginPath(){}, arc(){}, fill(){}, createRadialGradient(){ return { addColorStop(){} }; } };
function canvas2d(){ return ctx2d; }
class ParticleSystem {
  static BLENDMODE_ADD = 2;
  constructor(n,count,sc){ this.emitter=new Vector3(); this.minEmitBox=new Vector3(); this.maxEmitBox=new Vector3(); this.minSize=1; this.maxSize=2; this.minLifeTime=1; this.maxLifeTime=2; this.emitRate=10; this.blendMode=0; this.color1=Color4(); this.color2=Color4(); this.gravity=new Vector3(); this.direction1=new Vector3(); this.direction2=new Vector3(); this.particleTexture=null; this.disposeOnStop=false; this.running=false; this._emit=0; }
  start(delay){ this.running=true; if(!this._disposed) ParticlesStub.addPS(this); }
  dispose(){ this._disposed=true; this.running=false; }
}
class Texture { constructor(src,sc){ this.src=src; } }
class GL {
  constructor(){ this.intensity=1; }
}
class GlowLayer { constructor(n,sc){ return new GL(); } }
const Effect = { ShadersStore: {} };
class Light { constructor(){ this.intensity=1; this.diffuse=mkColor3(); this.specular=mkColor3(); this.position=new Vector3(); } }
const HemisphericLight = Light; const DirectionalLight = Light; const PointLight = Light;
class PostProcess { constructor(n,sh,params,samps,opt,cam){ this.onApply=(e)=>{}; this.setV=new V2Stub(); } }
class V2Stub { setVector2(){ } }
function setVec(){ }
class PhysicsAggregate { constructor(mesh,type,opts){ mesh.pDebris=true; } }
const PhysicsShapeType = { SPHERE:1, BOX:2, CYLINDER:3 };
class HavokPlugin { constructor(a,b){} }
class ArcRotateCamera { constructor(n,a,b,r,t,sc){ this.target=t; this.position=new Vector3(); this.lowerRadiusLimit=0; this.upperRadiusLimit=0; this.wheelDeltaPercentage=0; this.panningSensibility=0; this.inertia=0; this.speed=0; this.viewport={ toGlobal:()=>({x:0,y:0,w:1200,h:800}) }; } attachControl(){} detachControl(){} getTarget(){ return this.target||new Vector3(); } }
class Scene {
  constructor(){ this.clearColor=Color4(); this.ambientColor=mkColor3(); this.physics=null; this.transformMatrix={}; }
  registerBeforeRender(f){ rbs.push(f); }
  createPickingRay(x,y,cam){ return { origin:new Vector3(cam.target?cam.target.x:0,10,cam.target?cam.target.z:0), direction:new Vector3(0,-1,0) }; }
  getTransformMatrix(){ return this.transformMatrix; }
  enablePhysics(g,p){ this.physics={gravity:g,plugin:p}; }
  render(){} }
class Engine { constructor(){ this.adaptToDeviceRatio=false; BABYLON.__engines = BABYLON.__engines||[]; BABYLON.__engines.push(this); this.cb=null; }
  runRenderLoop(cb){ this.cb=cb; } getDeltaTime(){ return delta; } getRenderWidth(){ return 1200; } getRenderHeight(){ return 800; } getAspectRatio(){ return 1.5; } resize(){} }
const ParticlesStub = { ps:[], addPS(p){ this.ps.push(p); if(this.ps.length>60) this.ps.shift(); } };

const BABYLON = {
  Engine, Scene, ArcRotateCamera, Vector3, Vector2, Color3: Color3C, Color4, Matrix,
  MeshBuilder, StandardMaterial, DynamicTexture, ParticleSystem, Texture, GlowLayer,
  Effect, PostProcess, PhysicsAggregate, PhysicsShapeType, HavokPlugin,
  HemisphericLight, DirectionalLight, PointLight,
};

const listeners = {};
const elements = {};
function makeEl(id){
  const el = { id, style:{}, textContent:"", innerHTML:"", _l:{}, classList:{ add(){},remove(){},toggle(){} },
    querySelector: ()=>makeEl("q"), querySelectorAll: ()=>[], addEventListener(t,f){ (el._l[t]=el._l[t]||[]).push(f); },
    getAttribute: ()=>null, setAttribute(){} };
  elements[id]=el; return el;
}
["game","boot","bootsub","menu-main","menu-controls","menu-abilities","menu-settings","menu-pause","menu-death","hud","ct","grabind","sc","cb","wv","reticle"].forEach(makeEl);
const globalDocument = { getElementById:(id)=> elements[id]||makeEl(id), querySelectorAll:()=>[], body:{ appendChild:(el)=>{}, removeChild:()=>{} },
  createElement:()=>({ getContext:()=>canvas2d(), width:256, height:256, style:{}, addEventListener(){}, _l:{}, remove(){}, classList:{add(){},remove(){},toggle(){}}, textContent:"" }) };
const globalWindow = { addEventListener(t,f){ (listeners[t]=listeners[t]||[]).push(f); }, innerWidth:1200, innerHeight:800 };
let raf = null; const globalRaf = (cb)=>{ raf=cb; };
let intervals=[]; const globalSetInterval=(fn)=>{ intervals.push(fn); return intervals.length; };
const globalClearInterval=()=>{};
const globalSetTimeout=(fn)=>0;
global.document = globalDocument; global.window = globalWindow;
global.requestAnimationFrame = globalRaf; global.setInterval = globalSetInterval;
global.clearInterval = globalClearInterval; global.setTimeout = globalSetTimeout;
global.BABYLON = BABYLON;

const errors = [];
process.on("uncaughtException", (e)=>{ errors.push(e.stack); });

const src = fs.readFileSync(path.join(__dirname,"game.js"),"utf8");
try {
  (new Function("window","document","requestAnimationFrame","setInterval","clearInterval","setTimeout","BABYLON","HavokPhysics",
    src))(globalWindow, globalDocument, globalRaf, globalSetInterval, globalClearInterval, globalSetTimeout, BABYLON, async()=>({}));
} catch(e){ errors.push("LOAD: "+e.stack); }

// fire load (boot is async)
(async ()=>{
  try {
    if (listeners.load) for (const fn of listeners.load) await fn();
  } catch(e){ errors.push("LOAD-HANDLER: "+e.stack); }

  // start game via clicking START
  try {
    if (elements["menu-main"]) { }
    // find the data-action button click handlers are attached to querySelectorAll results,
    // which our stub returns empty. Instead call startGame indirectly: it's not exported.
    // Simulate by triggering panel button via document click. We can't reach startGame.
    // Instead drive frames via engine.runRenderLoop cb.
  } catch(e){ errors.push("menu: "+e.stack); }

  // Drive gameplay via exposed handle
  try {
    const G = globalWindow.__GAME;
    const eng = BABYLON.__engines && BABYLON.__engines[0];
    if (!G || !eng) throw new Error("no game handle/engine");
    G.startGame();
    // helper to fire keys
    const kd = (k) => { if(listeners.keydown) for(const fn of listeners.keydown) fn({ key:k, preventDefault(){} }); };
    const ku = (k) => { if(listeners.keyup) for(const fn of listeners.keyup) fn({ key:k }); };
    const pm = (x,y) => { if(listeners.pointermove) for(const fn of listeners.pointermove) fn({ clientX:x, clientY:y }); };
    const pd = (btn) => { if(listeners.pointerdown) for(const fn of listeners.pointerdown) fn({ button:btn }); };
    const pu = (btn) => { if(listeners.pointerup) for(const fn of listeners.pointerup) fn({ button:btn }); };
    for (let f=0; f<900; f++) {
      if (f%40===5) kd("q");
      if (f%40===12) kd("e");
      if (f%40===20) kd("r");
      if (f%40===28) kd("g");
      if (f%70===33) kd(" ");
      if (f%80===8) kd("f");
      if (f%60===16) kd("c");
      if (f%120===20) kd("x");
      if (f%30===0) kd("w");
      if (f%30===0) kd("d");
      if (f%30===15) { ku("w"); ku("d"); }
      pm(600+Math.sin(f*0.1)*200, 400+Math.cos(f*0.13)*150);
      pd(0); if (f%50===20) pu(0);
      eng.cb();
    }
    // trigger pause + resume
    kd("escape"); eng.cb();
    kd("escape"); eng.cb();
    // simulate death path
    G.Player.hp = 1; G.Player.damage(50); eng.cb();
    // retry
    G.startGame(); eng.cb();
  } catch(e){ errors.push("LOOP: "+e.stack); }

  if (errors.length){ console.log("ERRORS ("+errors.length+"):"); errors.forEach(e=>console.log(e+"\n")); process.exit(1); }
  console.log("OK: booted Babylon 3D scene without load errors.");
})();
