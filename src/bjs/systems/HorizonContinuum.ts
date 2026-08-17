/**
 * Non-Euclidean horizon volume.
 *
 * This is the interior half of the unified singularity architecture. Exterior
 * lensing hands directly to this opaque post-process in the same scene; no
 * world promotion, loading veil, coordinate reset, geometry card or blend
 * discontinuity participates in capture.
 */
import { Effect } from '@babylonjs/core/Materials/effect';
import { PostProcess } from '@babylonjs/core/PostProcesses/postProcess';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import type { Scene } from '@babylonjs/core/scene';

export const HORIZON_CONTINUUM_EFFECT='horizonContinuum';
const FRAG=`
precision highp float;
varying vec2 vUV;
uniform sampler2D textureSampler;
uniform float depth;
uniform float lookback;
uniform float phase;
uniform float destinationSeed;
uniform vec2 u_resolution;
float h(vec2 p){p=fract(p*vec2(127.1,311.7));p+=dot(p,p+19.19);return fract(p.x*p.y);}
float n(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(h(i),h(i+vec2(1,0)),f.x),mix(h(i+vec2(0,1)),h(i+vec2(1)),f.x),f.y);}
float fbm(vec2 p){float v=0.,a=.5;for(int i=0;i<6;i++){v+=a*n(p);p=p*2.07+13.7;a*=.5;}return v;}
void main(){
 vec3 exterior=texture2D(textureSampler,vUV).rgb;
 if(depth<.00001){gl_FragColor=vec4(exterior,1.);return;}
 vec2 q=(gl_FragCoord.xy*2.-u_resolution.xy)/max(1.,min(u_resolution.x,u_resolution.y));
 float r=length(q),a=atan(q.y,q.x);

 // Exponential optical depth: capture swallows every widescreen pixel rather
 // than enlarging a black decal. At the committed entry depth transmission
 // is effectively zero and no exterior layer can leak through.
 float optical=1.-exp(-max(0.,depth)*72.);
 vec3 col=exterior*(1.-optical);

 // Sub-black tidal structure gives steering feedback without turning the
 // horizon into a glowing tunnel. Values remain beneath three percent.
 float flow=fbm(vec2(a*2.4+phase*.018,log(r+.025)*2.8-phase*.026));
 float filament=smoothstep(.56,.86,flow)*(1.-smoothstep(.35,1.75,r));
 col+=vec3(.003,.009,.014)*filament*(1.-depth*.72);

 // The physical shadow expands through the complete minimum-axis viewport.
 // It is an overwrite, not transparency or additive blending.
 float swallowRadius=mix(.14,1.82,smoothstep(0.,.14,depth));
 float shadow=1.-smoothstep(swallowRadius,swallowRadius+.055,r);
 col*=1.-shadow;

 // The universe behind is visible only while the camera is truly aligned to
 // the recorded exit direction. This is a soft causal aperture, never a
 // whole-screen restoration of the exterior renderer. The aperture is large
 // enough to reacquire while steering, with a second gravitationally warped
 // image around it so the old universe remains spatially legible.
 float ap=.42*(1.-depth*.52);
 float back=(1.-smoothstep(ap,ap+.075,r))*lookback;
 float backHalo=smoothstep(ap*.72,ap,r)*(1.-smoothstep(ap+.055,ap+.30,r))*lookback;
 vec2 backUv=.5+(vUV-.5)*(1.+backHalo*.9);
 vec3 bentExterior=texture2D(textureSampler,clamp(backUv,vec2(.001),vec2(.999))).rgb;
 col=mix(col,bentExterior,backHalo*.68);
 col=mix(col,exterior,back);

 // A destination condenses continuously near voyage completion while the
 // opaque volume remains the carrier. There is no blink or intermediate sky.
 float arrive=smoothstep(.70,.995,depth);
 vec2 p=q*vec2(2.15,1.25);
 float cloud=fbm(p*2.5+destinationSeed*31.+phase*.0015);
 float lane=pow(max(0.,1.-abs(p.y+sin(p.x*2.1+destinationSeed*9.)*.16)),5.);
 vec3 neb=mix(vec3(.015,.07,.18),vec3(.20,.035,.29),cloud)*cloud*lane;
 vec2 cell=floor((vUV+destinationSeed)*vec2(480.,270.));
 vec2 fp=fract((vUV+destinationSeed)*vec2(480.,270.))-.5;
 float star=step(.994,h(cell))*exp(-dot(fp,fp)*220.);
 col+=(neb+vec3(.55,.72,1.)*star)*arrive*(1.-back);
 col=min(col,vec3(.68));
 gl_FragColor=vec4(col,1.0);
}`;
let registered=false;
function register(){if(registered)return;Effect.ShadersStore[HORIZON_CONTINUUM_EFFECT+'FragmentShader']=FRAG;registered=true;}

export class HorizonContinuum{
 private pp:PostProcess|null=null; private depth=0; private target=0;
 private lookback=0; private phase=0; private seed=0; private exiting=false;
 attach(scene:Scene,camera:Camera):void{
  if(this.pp)return;register();
  this.pp=new PostProcess(HORIZON_CONTINUUM_EFFECT,HORIZON_CONTINUUM_EFFECT,
   ['depth','lookback','phase','destinationSeed','u_resolution'],null,1,camera,
   Texture.BILINEAR_SAMPLINGMODE,scene.getEngine(),false);
  this.pp.onApply=(e)=>{const g=scene.getEngine();e.setFloat('depth',this.depth);
   e.setFloat('lookback',this.lookback);e.setFloat('phase',this.phase);
   e.setFloat('destinationSeed',this.seed);e.setFloat2('u_resolution',g.getRenderWidth()||1,g.getRenderHeight()||1);};
 }
 update(dt:number,depth:number,lookback:number,seed:number):void{
  if(!this.exiting)this.target=Math.max(0,Math.min(1,depth));
  this.lookback=Math.max(0,Math.min(1,lookback));this.seed=(seed%997)/997;
  this.phase+=Math.max(0,dt)*(1+this.target*2.);
  // Entry is immediate; destination reveal may ease without exposing a frame.
  if(this.target>0&&this.depth<=0)this.depth=this.target;
  else this.depth+=(this.target-this.depth)*Math.min(1,Math.max(0,dt)*3.5);
 }
 holdForDestination():void{this.exiting=true;this.target=1;this.depth=1;}
 revealDestination():void{this.exiting=false;this.target=0;}
 get intensity():number{return this.depth;}
 dispose():void{this.pp?.dispose();this.pp=null;}
}
