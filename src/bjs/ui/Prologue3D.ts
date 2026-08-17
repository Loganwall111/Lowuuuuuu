/** Dedicated real-time WebGL prologue scene used only for a new universe. */
import { Engine } from '@babylonjs/core/Engines/engine';
import { Scene } from '@babylonjs/core/scene';
import { FreeCamera } from '@babylonjs/core/Cameras/freeCamera';
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { TransformNode } from '@babylonjs/core/Meshes/transformNode';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import { PBRMaterial } from '@babylonjs/core/Materials/PBR/pbrMaterial';
import { HemisphericLight } from '@babylonjs/core/Lights/hemisphericLight';
import { DirectionalLight } from '@babylonjs/core/Lights/directionalLight';
import { GlowLayer } from '@babylonjs/core/Layers/glowLayer';

export class Prologue3D {
 private engine:Engine|null=null;private scene:Scene|null=null;private camera:FreeCamera|null=null;
 private canvas:HTMLCanvasElement|null=null;private errorObserver:any=null;private startupTimer=0;private rendered=false;
 private startAt=0;private pilot:TransformNode|null=null;private rocket:TransformNode|null=null;
 private ground:Mesh|null=null;private matrix:Mesh[]=[];private rings:Mesh[]=[];private hands:Mesh[]=[];
 start(canvas:HTMLCanvasElement):boolean{
  try{
   this.canvas=canvas;this.rendered=false;
   this.engine=new Engine(canvas,true,{alpha:true,stencil:true,preserveDrawingBuffer:false});
   this.errorObserver=this.engine.onEffectErrorObservable.add(({errors})=>this.degrade(errors));
   const scene=this.scene=new Scene(this.engine);scene.clearColor=new Color4(.018,.035,.075,1);
   scene.onAfterRenderObservable.addOnce(()=>{this.rendered=true;clearTimeout(this.startupTimer);});
   scene.fogMode=Scene.FOGMODE_EXP2;scene.fogDensity=.008;scene.fogColor=new Color3(.06,.1,.18);
   const cam=this.camera=new FreeCamera('prologueCamera',new Vector3(0,5,-18),scene);cam.setTarget(new Vector3(0,3,12));cam.fov=.86;
   const hemi=new HemisphericLight('proHemi',new Vector3(.2,1,-.2),scene);hemi.intensity=.72;hemi.diffuse=new Color3(.52,.7,1);
   const sun=new DirectionalLight('proSun',new Vector3(-.35,-1,.45),scene);sun.position=new Vector3(30,50,-25);sun.intensity=4.2;sun.diffuse=new Color3(1,.72,.43);
   const glow=new GlowLayer('proGlow',scene,{blurKernelSize:48});glow.intensity=.75;
   this.buildTerrain(scene);this.buildPilot(scene);this.buildRocket(scene);this.buildMatrix(scene);this.buildWormhole(scene);this.buildHands(scene);
   this.startAt=performance.now();this.engine.runRenderLoop(()=>this.render());
   this.startupTimer=window.setTimeout(()=>{if(!this.rendered)this.degrade('no 3D frame within 1.5s');},1500);
   addEventListener('resize',this.resize,{passive:true});return true;
  }catch(e){console.warn('3D prologue degraded to CSS cinematic:',e);this.dispose();return false;}
 }
 private mat(name:string,c:Color3,metal=.05,rough=.72,emissive=Color3.Black()):PBRMaterial{const m=new PBRMaterial(name,this.scene!);m.albedoColor=c;m.metallic=metal;m.roughness=rough;m.emissiveColor=emissive;return m;}
 private buildTerrain(s:Scene):void{
  const g=this.ground=MeshBuilder.CreateGround('futureEarth',{width:180,height:220,subdivisions:64},s);const p=g.getVerticesData('position');
  if(p)for(let i=0;i<p.length;i+=3){const x=p[i],z=p[i+2];p[i+1]=Math.sin(x*.17)*1.8+Math.sin(z*.11+x*.04)*2.4+Math.sin((x-z)*.37)*.35;}if(p){g.updateVerticesData('position',p);g.createNormals(true);}
  g.material=this.mat('earthAlive',new Color3(.12,.24,.12),0,.94);
  for(let i=0;i<80;i++){const rock=MeshBuilder.CreatePolyhedron('earthRock',{type:1,size:.5+Math.random()*2},s);rock.position.set((Math.random()-.5)*90,1+Math.random()*3,5+Math.random()*130);rock.scaling.y=.4+Math.random()*1.8;rock.material=this.mat('rock'+i,new Color3(.12+Math.random()*.12,.1,.08),0,.96);}
 }
 private buildPilot(s:Scene):void{const root=this.pilot=new TransformNode('lastPilot',s);root.position.set(-12,1,-2);const suit=this.mat('pilotSuit',new Color3(.025,.05,.075),.72,.28,new Color3(0,.04,.065));
  const torso=MeshBuilder.CreateCapsule('pilotTorso',{radius:.7,height:2.8,tessellation:20},s);torso.parent=root;torso.position.y=1.7;torso.material=suit;
  const helmet=MeshBuilder.CreateSphere('pilotHelmet',{diameter:1.45,segments:24},s);helmet.parent=root;helmet.position.y=3.25;helmet.material=this.mat('helmet',new Color3(.035,.12,.18),.82,.12,new Color3(0,.12,.18));
  for(const x of [-.48,.48]){const leg=MeshBuilder.CreateCapsule('pilotLeg',{radius:.23,height:2.1,tessellation:12},s);leg.parent=root;leg.position.set(x,.25,0);leg.material=suit;}}
 private buildRocket(s:Scene):void{const r=this.rocket=new TransformNode('arkRocket',s);r.position.set(12,2,22);const hull=this.mat('rocketHull',new Color3(.72,.78,.86),.68,.2);
  const body=MeshBuilder.CreateCylinder('rocketBody',{height:12,diameter:3,tessellation:32},s);body.parent=r;body.position.y=6;body.material=hull;
  const nose=MeshBuilder.CreateCylinder('rocketNose',{height:4,diameterTop:0,diameterBottom:3,tessellation:32},s);nose.parent=r;nose.position.y=14;nose.material=hull;
  const engine=MeshBuilder.CreateCylinder('rocketEngine',{height:2,diameterTop:2.5,diameterBottom:.9,tessellation:24},s);engine.parent=r;engine.position.y=-1;engine.material=this.mat('engineFire',new Color3(.1,.3,.4),.1,.2,new Color3(.2,.8,1));}
 private buildMatrix(s:Scene):void{const mat=this.mat('matrixCode',new Color3(0,.08,.01),.1,.3,new Color3(0,1,.18));for(let i=0;i<96;i++){const b=MeshBuilder.CreateBox('matrixGlyph',{width:.08+Math.random()*.16,height:1+Math.random()*5,depth:.08},s);b.position.set((Math.random()-.5)*46,20+Math.random()*45,-2+Math.random()*55);b.material=mat;b.setEnabled(false);this.matrix.push(b);}}
 private buildWormhole(s:Scene):void{const mat=this.mat('wormholeEnergy',new Color3(.02,.08,.2),.25,.16,new Color3(.12,.45,1));for(let i=0;i<28;i++){const t=MeshBuilder.CreateTorus('wormholeRing',{diameter:9+i*.72,thickness:.18+i*.015,tessellation:64},s);t.position.set(0,4,20+i*4);t.rotation.x=Math.PI/2;t.material=mat;t.setEnabled(false);this.rings.push(t);}}
 private buildHands(s:Scene):void{const mat=this.mat('pixelHands',new Color3(.06,.35,.5),.55,.2,new Color3(0,.4,.7));for(const side of [-1,1])for(let i=0;i<70;i++){const b=MeshBuilder.CreateBox('handVoxel',{size:.17+Math.random()*.15},s);const row=Math.floor(i/10),col=i%10;b.position.set(side*(3.5-col*.16),-1.7+row*.16,4+Math.sin(col*.5)*.22);b.material=mat;b.setEnabled(false);(b.metadata={side,index:i});this.hands.push(b);}}
 private render=()=>{if(!this.scene||!this.camera)return;const t=(performance.now()-this.startAt)/1000;
  if(t<4)this.phaseEarth(t);else if(t<7)this.phaseMatrix(t-4);else if(t<10.5)this.phaseWormhole(t-7);else if(t<13.3)this.phaseHands(t-10.5);else{this.scene.clearColor=new Color4(1,1,1,1);this.setGroup(this.hands,false);}
  this.scene.render();};
 private phaseEarth(t:number):void{if(this.pilot){this.pilot.position.x=-12+Math.min(1,t/2.4)*19;this.pilot.rotation.y=Math.sin(t*7)*.08;}if(this.rocket&&t>2.1)this.rocket.position.y=2+Math.pow((t-2.1)/1.9,2)*130;if(this.camera)this.camera.position.x=Math.sin(t*.22)*1.2;}
 private phaseMatrix(t:number):void{if(this.ground){const m=this.ground.material as PBRMaterial;m.albedoColor=Color3.Lerp(new Color3(.12,.24,.12),new Color3(.16,.055,.025),Math.min(1,t*.55));m.emissiveColor=new Color3(0,.015*t,0);}for(let i=0;i<this.matrix.length;i++){const b=this.matrix[i];b.setEnabled(true);b.position.y-=.7+(i%7)*.05;if(b.position.y<-2)b.position.y=45;}if(this.camera){this.camera.position.set(0,10,-22+t*3);this.camera.setTarget(new Vector3(0,1,30));}}
 private phaseWormhole(t:number):void{this.ground?.setEnabled(false);this.pilot?.setEnabled(false);this.rocket?.setEnabled(false);this.setGroup(this.matrix,false);this.setGroup(this.rings,true);for(let i=0;i<this.rings.length;i++){const r=this.rings[i];r.rotation.z+=.008*(i%2?1:-1);r.scaling.setAll(.7+Math.sin(t*2+i)*.04);}if(this.camera){this.camera.position.set(0,4,12+t*24);this.camera.setTarget(new Vector3(0,4,120));}}
 private phaseHands(t:number):void{this.setGroup(this.rings,false);if(this.camera){this.camera.position.set(0,0,0);this.camera.setTarget(new Vector3(0,0,5));}const amount=Math.min(1,t/2.25);for(const b of this.hands){const md=b.metadata as {side:number,index:number};const on=md.index/70<amount;b.setEnabled(on);if(on)b.position.x+=-md.side*.006*(1+t*1.2);}}
 private setGroup(a:Mesh[],on:boolean):void{for(const m of a)m.setEnabled(on);}
 private degrade(reason:string):void{console.warn('3D prologue failed open to cinematic fallback:',reason);if(this.canvas)this.canvas.style.display='none';setTimeout(()=>this.dispose(false),0);}
 private resize=()=>this.engine?.resize();
 dispose(clearCanvas=true):void{removeEventListener('resize',this.resize);clearTimeout(this.startupTimer);
  if(this.errorObserver&&this.engine){try{this.engine.onEffectErrorObservable.remove(this.errorObserver);}catch{}}
  this.errorObserver=null;try{this.engine?.stopRenderLoop();this.scene?.dispose();this.engine?.dispose();}catch{}
  this.engine=null;this.scene=null;if(clearCanvas)this.canvas=null;}
}
