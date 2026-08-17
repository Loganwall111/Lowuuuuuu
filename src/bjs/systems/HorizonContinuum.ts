/** Full-screen, continuous event-horizon volume. No geometry trigger or cut. */
import { Effect } from '@babylonjs/core/Materials/effect';
import { PostProcess } from '@babylonjs/core/PostProcesses/postProcess';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import type { Scene } from '@babylonjs/core/scene';

export const HORIZON_CONTINUUM_EFFECT = 'horizonContinuum';
const FRAG = `
precision highp float;
varying vec2 vUV;
uniform sampler2D textureSampler;
uniform float depth;
uniform float lookback;
uniform float phase;
uniform float destinationSeed;
uniform float aspect;

float hash21(vec2 p){
  p=fract(p*vec2(123.34,456.21));p+=dot(p,p+45.32);return fract(p.x*p.y);
}
float noise(vec2 p){
  vec2 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);
  return mix(mix(hash21(i),hash21(i+vec2(1,0)),f.x),
             mix(hash21(i+vec2(0,1)),hash21(i+vec2(1,1)),f.x),f.y);
}
float fbm(vec2 p){float s=0.0,a=.5;for(int i=0;i<5;i++){s+=noise(p)*a;p=p*2.03+17.1;a*=.5;}return s;}

void main(){
  vec4 oldScene=texture2D(textureSampler,vUV);
  if(depth<.0005){gl_FragColor=oldScene;return;}
  vec2 q=vUV-.5;q.x*=aspect;
  float r=length(q),ang=atan(q.y,q.x);

  // The horizon grows from the physical centre into an ink volume.
  float throat=depth*1.38;
  float swallow=smoothstep(.82-throat,.12-throat,r);
  float spiral=fbm(vec2(ang*1.7+phase*.035,log(r+.035)*3.0-phase*.06));
  float fluid=smoothstep(.26,.82,spiral)*smoothstep(.8,.12,r);
  float ink=clamp(swallow*.92+fluid*depth*.42,0.0,1.0);
  vec3 col=mix(oldScene.rgb,vec3(0.0),ink);

  // Looking back keeps a real aperture onto the scene just left.
  float aperture=mix(.04,.34,lookback)*(1.0-depth*.55);
  float backWindow=smoothstep(aperture+.035,aperture,r)*lookback;
  col=mix(col,oldScene.rgb,backWindow);

  // The destination condenses out of darkness only in the latter voyage.
  float arrive=smoothstep(.58,.98,depth);
  vec2 p=q*vec2(2.2,1.4);
  float cloud=fbm(p*2.3+destinationSeed+phase*.002);
  float lane=pow(max(0.0,1.0-abs(p.y+sin(p.x*2.2+destinationSeed)*.17)),4.0);
  vec3 neb=mix(vec3(.05,.16,.32),vec3(.42,.12,.55),cloud)*cloud*lane;
  vec2 cell=floor((vUV+destinationSeed)*vec2(420.0,230.0));
  vec2 fp=fract((vUV+destinationSeed)*vec2(420.0,230.0))-.5;
  float star=step(.992,hash21(cell))*exp(-dot(fp,fp)*180.0);
  vec3 destination=neb*.72+vec3(.68,.82,1.0)*star;
  col+=destination*arrive*(1.0-backWindow);

  // Never flash white: the continuum is light-absorbing by definition.
  col=min(col,vec3(.72));
  gl_FragColor=vec4(col,oldScene.a);
}`;

let registered=false;
function register(){if(registered)return;Effect.ShadersStore[HORIZON_CONTINUUM_EFFECT+'FragmentShader']=FRAG;registered=true;}

export class HorizonContinuum {
  private pp:PostProcess|null=null;
  private depth=0;
  private targetDepth=0;
  private lookback=0;
  private phase=0;
  private seed=0;
  private exiting=false;

  attach(scene:Scene,camera:Camera):void{
    if(this.pp)return;
    register();
    this.pp=new PostProcess(HORIZON_CONTINUUM_EFFECT,HORIZON_CONTINUUM_EFFECT,
      ['depth','lookback','phase','destinationSeed','aspect'],null,1,camera,
      Texture.BILINEAR_SAMPLINGMODE,scene.getEngine(),false);
    this.pp.onApply=(e)=>{
      const eng=scene.getEngine();
      e.setFloat('depth',this.depth);e.setFloat('lookback',this.lookback);
      e.setFloat('phase',this.phase);e.setFloat('destinationSeed',this.seed);
      e.setFloat('aspect',(eng.getRenderWidth()||1)/Math.max(1,eng.getRenderHeight()||1));
    };
  }
  update(dt:number,depth:number,lookback:number,seed:number):void{
    if(!this.exiting)this.targetDepth=Math.max(0,Math.min(1,depth));
    this.lookback=Math.max(0,Math.min(1,lookback));this.seed=(seed%997)/997;
    this.phase+=Math.max(0,dt)*(1+this.targetDepth*2);
    this.depth+=(this.targetDepth-this.depth)*Math.min(1,Math.max(0,dt)*2.8);
  }
  /** Hold opaque over the renderer swap, then reveal the destination. */
  holdForDestination():void{this.exiting=true;this.targetDepth=1;this.depth=1;}
  revealDestination():void{this.exiting=false;this.targetDepth=0;}
  get intensity():number{return this.depth;}
  dispose():void{this.pp?.dispose();this.pp=null;}
}
