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
uniform float active;
uniform float seed;
uniform float phase;
uniform vec2 resolution;
float h(vec2 p){p=fract(p*vec2(123.34,456.21));p+=dot(p,p+45.32);return fract(p.x*p.y);}
float n(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(h(i),h(i+vec2(1,0)),f.x),mix(h(i+vec2(0,1)),h(i+vec2(1)),f.x),f.y);}
float fbm(vec2 p){float v=0.,a=.5;for(int i=0;i<5;i++){v+=a*n(p);p=p*2.03+17.7;a*=.5;}return v;}
void main(){
 vec4 base=texture2D(textureSampler,vUV);
 if(active<.5){gl_FragColor=vec4(base.rgb,1.);return;}
 float asp=resolution.x/max(1.,resolution.y);vec2 p=(vUV-.5)*vec2(asp,1.);
 float band=exp(-pow((p.y+sin(p.x*2.1+seed*6.28)*.11)/.24,2.));
 float cloud=fbm(p*3.4+seed*31.7);
 vec3 fallback=mix(vec3(.004,.008,.022),vec3(.055,.035,.105),cloud)*(.45+band*1.35);
 fallback+=vec3(.04,.12,.18)*pow(cloud,3.)*band*.7;
 vec2 grid=vUV*vec2(620.,350.);vec2 cell=floor(grid),q=fract(grid)-.5;
 float rnd=h(cell+seed*997.);float star=step(.9915,rnd)*exp(-dot(q,q)*(120.+rnd*260.));
 vec3 st=mix(vec3(.55,.72,1.),vec3(1.,.76,.48),h(cell+13.))*star*(.6+rnd*2.4);
 // Preserve anything the real renderer produced. The fallback only fills
 // pixels that are numerically black, so HUD/world geometry is untouched.
 float lum=max(base.r,max(base.g,base.b));float missing=1.-smoothstep(.002,.018,lum);
 vec3 col=base.rgb+(fallback+st)*missing;
 gl_FragColor=vec4(col,1.);
}`;
let registered=false;
export class SkySafetyPass{
 private pp:PostProcess|null=null;private active=0;private seed=0;private phase=0;
 attach(scene:Scene,camera:Camera):void{this.dispose();if(!registered){Effect.ShadersStore[NAME+'FragmentShader']=FRAG;registered=true;}
  this.pp=new PostProcess(NAME,NAME,['active','seed','phase','resolution'],null,1,camera,Texture.BILINEAR_SAMPLINGMODE,scene.getEngine(),false);
  this.pp.onApply=(e)=>{const g=scene.getEngine();e.setFloat('active',this.active);e.setFloat('seed',this.seed);e.setFloat('phase',this.phase);e.setFloat2('resolution',g.getRenderWidth()||1,g.getRenderHeight()||1);};}
 update(dt:number,x:number,y:number,z:number,allow=true):void{const extreme=Math.max(Math.abs(x),Math.abs(y),Math.abs(z));this.active=allow&&extreme>1e6?1:0;this.seed=((Math.floor(x/260000)^Math.floor(z/260000))>>>0)%997/997;this.phase+=Math.max(0,dt);}
 get enabled():boolean{return this.active>0;}
 dispose():void{this.pp?.dispose();this.pp=null;}
}
