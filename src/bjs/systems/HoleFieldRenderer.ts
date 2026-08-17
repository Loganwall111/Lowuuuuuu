/**
 * Canonical singularity material bridge.
 *
 * Every open-world black hole now executes the exact vertex/fragment material
 * used by the known-good Singularity locale. One full-screen triangle is
 * shared by the nearest physical hole; only world-space centre, horizon and
 * deterministic profile change. There are no alternate black-hole shaders.
 */
import { Matrix, Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { Effect } from '@babylonjs/core/Materials/effect';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import { holeProfile, type HoleProfile } from './HoleProfiles';
import { rollAnomaly, ANOMALY_COVER, STANDARD_COVER } from './BlackHoleBody';
import { LENS_MODE_ID, LENS_PROFILES } from './LensProfiles';
import { safeAspect } from '../SafeUniforms';
import {
  WORKING_SINGULARITY_FRAG, WORKING_SINGULARITY_VERT
} from '../worlds/BlackHoleWorld';

export interface HoleSpec { id:string; position:Vector3; horizon:number; seed:number; }
export interface HoleFieldOptions { buildWithin:number; releaseBeyond:number; maxLive:number; }
// The canonical material synthesizes a complete lensed sky, so it must only
// engage when the hole is genuinely the focus of approach. The former 320-rs
// radius activated the nearby guaranteed core at the spawn point and replaced
// normal space with its purple interior sky.
export const DEFAULT_HOLEFIELD:HoleFieldOptions={buildWithin:64,releaseBeyond:84,maxLive:1};
export const DISK_INNER=2.6,DISK_OUTER=9.0,QUAD_RADII=11.5;
export function radiiAway(eye:Vector3,h:HoleSpec):number{
 return Vector3.Distance(eye,h.position)/Math.max(h.horizon,1e-6);
}
interface Active { spec:HoleSpec; profile:HoleProfile; anomaly:boolean; }

const UNIFORMS=[
 'camPos','holePos','camInv','fov','aspect','time','rs','spin',
 'lensMode','lensFalloff','ringAmt','ringRadius','lensSymmetry','lensDistortion',
 'lensTwist','lensChroma','lensTint','lensSoftness','insideAmt','exitDir',
 'exitWindow','nestedLens','singularity','fallDir','darkness','voidHue',
 'diskInner','diskOuter','diskTilt','exposure','lensStrength','horizonCover',
 'diskBright','dopplerAmt','diskThickness','diskTemp'
];

export class HoleFieldRenderer{
 opts:HoleFieldOptions; private scene:Scene|null=null; private mesh:Mesh|null=null;
 private mat:ShaderMaterial|null=null; private active:Active|null=null; private t=0;
 private interior={inside:0,exitWindow:1,nestedLens:0,singularity:0,darkness:0,
  exitDir:new Vector3(0,0,-1),fallDir:new Vector3(0,0,1)};
 sky={medium:'stars',symmetry:0,tint:[.06,.10,.22] as [number,number,number],strangeness:0,zoom:1};
 constructor(opts:Partial<HoleFieldOptions>={}){this.opts={...DEFAULT_HOLEFIELD,...opts};}
 attach(scene:Scene):void{
  this.dispose();this.scene=scene;
  Effect.ShadersStore.workingSingularityVertexShader=WORKING_SINGULARITY_VERT;
  Effect.ShadersStore.workingSingularityFragmentShader=WORKING_SINGULARITY_FRAG;
  this.mat=new ShaderMaterial('workingSingularityMaterial',scene,'workingSingularity',{
   attributes:['position','uv'],uniforms:UNIFORMS});
  this.mat.backFaceCulling=false;this.mat.depthFunction=519;
  this.mesh=new Mesh('workingSingularityViewport',scene);
  this.mesh.setVerticesData('position',[-1,-1,0,3,-1,0,-1,3,0],false,3);
  this.mesh.setVerticesData('uv',[0,0,2,0,0,2],false,2);
  this.mesh.setIndices([0,1,2]);this.mesh.material=this.mat;
  this.mesh.infiniteDistance=true;this.mesh.isPickable=false;
  this.mesh.alwaysSelectAsActiveMesh=true;this.mesh.renderingGroupId=3;
  this.mesh.freezeWorldMatrix();this.mesh.setEnabled(false);
 }
 get count():number{return this.active?1:0;}
 has(id:string):boolean{return this.active?.spec.id===id;}
 isLocked(id:string):boolean{return this.active?.spec.id===id;}
 isAnomaly(id:string):boolean{return this.active?.spec.id===id?this.active.anomaly:false;}
 profileOf(id:string):HoleProfile|null{return this.active?.spec.id===id?this.active.profile:null;}
 setSky(next:Partial<typeof this.sky>):void{this.sky={...this.sky,...next} as typeof this.sky;}
 setInterior(state:{inside:number;exitWindow:number;nestedLens:number;singularity:number;darkness:number;
  exitDir:Vector3;fallDir:Vector3}|null):void{
  if(!state){this.interior.inside=0;this.interior.exitWindow=1;this.interior.nestedLens=0;
   this.interior.singularity=0;this.interior.darkness=0;return;}
  this.interior.inside=Math.max(0,Math.min(1,state.inside));
  this.interior.exitWindow=Math.max(0,Math.min(1,state.exitWindow));
  this.interior.nestedLens=Math.max(0,Math.min(1,state.nestedLens));
  this.interior.singularity=Math.max(0,Math.min(1,state.singularity));
  this.interior.darkness=Math.max(0,Math.min(1,state.darkness));
  if(state.exitDir.lengthSquared()>1e-9)this.interior.exitDir.copyFrom(state.exitDir).normalize();
  if(state.fallDir.lengthSquared()>1e-9)this.interior.fallDir.copyFrom(state.fallDir).normalize();
 }
 update(eye:Vector3,holes:readonly HoleSpec[]):void{
  if(!this.scene||!this.mesh||!this.mat)return;
  let nearest:HoleSpec|null=this.interior.inside>0&&holes.length?holes[0]:null,away=Infinity;
  if(!nearest)for(const h of holes){const d=radiiAway(eye,h);if(d<away&&d<=this.opts.buildWithin){nearest=h;away=d;}}
  if(!nearest){this.active=null;this.mesh.setEnabled(false);return;}
  if(this.active?.spec.id!==nearest.id)this.active={spec:nearest,profile:holeProfile(nearest.seed),anomaly:rollAnomaly(nearest.seed)};
  else this.active.spec=nearest;
  const cam=this.scene.activeCamera;if(!cam){this.mesh.setEnabled(false);return;}
  const to=nearest.position.subtract(eye);
  if(this.interior.inside<=0){
   const alignment=Vector3.Dot(to.normalize(),cam.getForwardRay(1).direction);
   if(alignment<.42){this.mesh.setEnabled(false);return;}
  }
  this.mesh.setEnabled(true);this.t+=Math.max(0,this.scene.getEngine().getDeltaTime()/1000);
  cam.computeWorldMatrix();
  this.mat.setVector3('camPos',cam.position);
  this.mat.setVector3('holePos',nearest.position);
  this.mat.setMatrix('camInv',Matrix.Invert(cam.getViewMatrix()));
  this.mat.setFloat('fov',cam.fov||.9);
  const eng=this.scene.getEngine();
  this.mat.setFloat('aspect',safeAspect(eng.getRenderWidth(),eng.getRenderHeight()));
  this.bindCanonical(this.active);
 }
 private bindCanonical(a:Active):void{
  const m=this.mat!,h=Math.max(1e-3,a.spec.horizon),p=a.profile;
  const lens=LENS_PROFILES[p.cls==='exotic'?'prismatic':p.cls==='kerr'?'rippled':'schwarzschild'];
  m.setFloat('time',this.t);m.setFloat('rs',h);m.setFloat('spin',p.spin);
  m.setFloat('lensMode',LENS_MODE_ID[lens.mode]??0);m.setFloat('lensFalloff',lens.falloff);
  m.setFloat('ringAmt',lens.ring);m.setFloat('ringRadius',lens.ringRadius);
  m.setFloat('lensSymmetry',lens.symmetry);m.setFloat('lensDistortion',lens.distortion);
  m.setFloat('lensTwist',lens.twist);m.setFloat('lensChroma',lens.chroma);
  m.setColor3('lensTint',new Color3(...lens.tint));m.setFloat('lensSoftness',lens.softness);
  const i=this.interior;
  m.setFloat('insideAmt',i.inside);m.setVector3('exitDir',i.exitDir);m.setFloat('exitWindow',i.exitWindow);
  m.setFloat('nestedLens',i.nestedLens);m.setFloat('singularity',i.singularity);m.setVector3('fallDir',i.fallDir);
  m.setFloat('darkness',i.darkness);m.setFloat('voidHue',(this.t*.02)%1);
  m.setFloat('diskInner',p.diskInner*h);m.setFloat('diskOuter',Math.max((p.diskInner+.001)*h,p.diskOuter*h));
  m.setFloat('diskTilt',p.diskTilt);m.setFloat('exposure',.72);m.setFloat('lensStrength',1);
  m.setFloat('horizonCover',a.anomaly?ANOMALY_COVER:STANDARD_COVER);
  m.setFloat('diskBright',p.diskBright*.099);m.setFloat('dopplerAmt',p.doppler);
  m.setFloat('diskThickness',Math.max(0,p.diskThickness*h));m.setFloat('diskTemp',Math.max(.01,p.temperature));
 }
 dispose():void{this.mat?.dispose();this.mesh?.dispose();this.mat=null;this.mesh=null;this.active=null;this.scene=null;}
}
