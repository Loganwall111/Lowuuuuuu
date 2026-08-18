/** Coordinate-invariant fallback sky for Float32-hostile deep coordinates. */
import { Effect } from '@babylonjs/core/Materials/effect';
import { PostProcess } from '@babylonjs/core/PostProcesses/postProcess';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import type { Scene } from '@babylonjs/core/scene';

const NAME='extremeSkySafety';
const FRAG=`
precision highp float;
varying vec2 vUV;
uniform sampler2D textureSampler;
uniform float u_skyEnabled;
uniform float seed;
uniform vec2 resolution;
float h(vec2 p){p=fract(p*vec2(123.34,456.21));p+=dot(p,p+45.32);return fract(p.x*p.y);}
float n(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(h(i),h(i+vec2(1,0)),f.x),mix(h(i+vec2(0,1)),h(i+vec2(1)),f.x),f.y);}
float fbm(vec2 p){float v=0.,a=.5;for(int i=0;i<5;i++){v+=a*n(p);p=p*2.03+17.7;a*=.5;}return v;}
void main(){
 vec4 base=texture2D(textureSampler,vUV);
 if(u_skyEnabled<.5){gl_FragColor=vec4(base.rgb,1.);return;}
 float asp=resolution.x/max(1.,resolution.y);vec2 p=(vUV-.5)*vec2(asp,1.);
 // RIGID SKY DOME: the galactic band is a stationary matrix pass. The old
 // sine-wave wobble displaced the band (and with it the whole background
 // starfield) every frame, which read as a heartbeat bounce; there is no
 // time-based transform on these coordinates, so stars hold perfectly still.
 float band=exp(-pow(p.y/.24,2.));
 float cloud=fbm(p*3.4+seed*31.7);
 vec3 fallback=mix(vec3(.001,.003,.010),vec3(.014,.010,.035),cloud)*(.24+band*.58);
 fallback+=vec3(.008,.026,.045)*pow(cloud,4.)*band*.42;
 vec2 grid=vUV*vec2(620.,350.);vec2 cell=floor(grid),q=fract(grid)-.5;
 float rnd=h(cell+seed*997.);float star=step(.9915,rnd)*exp(-dot(q,q)*(120.+rnd*260.));
 vec3 st=mix(vec3(.55,.72,1.),vec3(1.,.76,.48),h(cell+13.))*star*(.6+rnd*2.4);
 float lum=max(base.r,max(base.g,base.b));float missing=1.-smoothstep(.002,.018,lum);
 gl_FragColor=vec4(base.rgb+(fallback+st)*missing,1.);
}`;
let registered=false;
export class SkySafetyPass{
 private pp:PostProcess|null=null;private scene:Scene|null=null;private camera:Camera|null=null;
 private active=0;private seed=0;private errorObserver:any=null;private failed=false;
 /** Store dependencies only. Compilation is lazy and never touches startup. */
 attach(scene:Scene,camera:Camera):void{this.dispose();this.scene=scene;this.camera=camera;}
 private ensure():void{
  const scene=this.scene,camera=this.camera;if(this.pp||this.failed||!scene||!camera)return;
  try{
   if(!registered){Effect.ShadersStore[NAME+'FragmentShader']=FRAG;registered=true;}
   const engine=scene.getEngine();
   this.errorObserver=engine.onEffectErrorObservable.add(({effect,errors})=>{
    let source='';try{const fx=effect as any;source=fx.getFragmentShaderSource?.()??fx._fragmentSourceCode??String(fx.name??'');}catch{}
    if(!source.includes('u_skyEnabled'))return;
    this.failed=true;this.active=0;
    console.warn('Extreme sky safety shader disabled; base scene preserved:',errors);
    // Removing a failed post-process from the camera chain is the fail-open
    // behavior. Leaving it attached is what blacked the entire game.
    setTimeout(()=>{this.pp?.dispose();this.pp=null;},0);
   });
   this.pp=new PostProcess(NAME,NAME,['u_skyEnabled','seed','resolution'],null,1,camera,Texture.BILINEAR_SAMPLINGMODE,engine,false);
   this.pp.onApply=(e)=>{const g=scene.getEngine();e.setFloat('u_skyEnabled',this.active);e.setFloat('seed',this.seed);e.setFloat2('resolution',g.getRenderWidth()||1,g.getRenderHeight()||1);};
  }catch(e){this.failed=true;this.active=0;console.warn('Extreme sky safety unavailable; base scene preserved:',e);this.pp?.dispose();this.pp=null;}
 }
 update(dt:number,x:number,y:number,z:number,allow=true):void{
  const extreme=Math.max(Math.abs(x),Math.abs(y),Math.abs(z));this.active=allow&&extreme>1e6&&!this.failed?1:0;
  this.seed=((Math.floor(x/260000)^Math.floor(z/260000))>>>0)%997/997;
  if(this.active)this.ensure();
 }
 get enabled():boolean{return this.active>0&&!this.failed;}
 dispose():void{
  if(this.errorObserver&&this.scene){try{this.scene.getEngine().onEffectErrorObservable.remove(this.errorObserver);}catch{}}
  this.errorObserver=null;this.pp?.dispose();this.pp=null;this.scene=null;this.camera=null;this.failed=false;this.active=0;
 }
}
