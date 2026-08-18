/** Coordinate-invariant fallback sky for Float32-hostile deep coordinates. */
import { Effect } from '@babylonjs/core/Materials/effect';
import { PostProcess } from '@babylonjs/core/PostProcesses/postProcess';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import { Matrix } from '@babylonjs/core/Maths/math.vector';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import type { Scene } from '@babylonjs/core/scene';

const NAME = 'extremeSkySafety';
const FRAG = `
precision highp float;
varying vec2 vUV;
uniform sampler2D textureSampler;
uniform float u_skyEnabled;
uniform float seed;
uniform vec2 resolution;
uniform mat4 viewInverse;
uniform float fov;
uniform float aspect;

float h3(vec3 p){p=fract(p*vec3(123.34,456.21,789.12));p+=dot(p,p+45.32);return fract(p.x*p.y*p.z);}
float n3(vec3 p){vec3 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);
 return mix(mix(mix(h3(i+vec3(0,0,0)),h3(i+vec3(1,0,0)),f.x),
                mix(h3(i+vec3(0,1,0)),h3(i+vec3(1,1,0)),f.x),f.y),
            mix(mix(h3(i+vec3(0,0,1)),h3(i+vec3(1,0,1)),f.x),
                mix(h3(i+vec3(0,1,1)),h3(i+vec3(1,1,1)),f.x),f.y),f.z);}
float fbm3(vec3 p){float v=0.,a=.5;for(int i=0;i<5;i++){v+=a*n3(p);p=p*2.03+17.7;a*=.5;}return v;}

void main(){
 vec4 base = texture2D(textureSampler, vUV);
 if(u_skyEnabled < .5){gl_FragColor = vec4(base.rgb, 1.); return;}

 // Reconstruct world-space ray direction from inverse view matrix.
 // Independent stationary 3D skybox dome in absolute world space.
 vec2 ndc = (vUV - 0.5) * 2.0;
 float tanFov = tan(max(0.1, fov) * 0.5);
 vec3 rayCam = normalize(vec3(ndc.x * aspect * tanFov, ndc.y * tanFov, 1.0));
 vec3 dir = normalize((viewInverse * vec4(rayCam, 0.0)).xyz);

 // RIGID SKY DOME: the galactic band and stars are anchored to absolute 3D world space.
 float band = exp(-pow(dir.y / 0.24, 2.0));
 float cloud = fbm3(dir * 3.4 + seed * 31.7);
 vec3 fallback = mix(vec3(.001, .003, .010), vec3(.014, .010, .035), cloud) * (.24 + band * .58);
 fallback += vec3(.008, .026, .045) * pow(cloud, 4.0) * band * .42;

 vec3 pStar = dir * 180.0;
 vec3 cell = floor(pStar), q = fract(pStar) - 0.5;
 float rnd = h3(cell + seed * 997.0);
 float star = step(0.985, rnd) * exp(-dot(q, q) * (120.0 + rnd * 260.0));
 vec3 st = mix(vec3(.55, .72, 1.), vec3(1., .76, .48), h3(cell + 13.0)) * star * (.6 + rnd * 2.4);

 float lum = max(base.r, max(base.g, base.b));
 float missing = 1.0 - smoothstep(.002, .018, lum);
 gl_FragColor = vec4(base.rgb + (fallback + st) * missing, 1.0);
}`;

let registered = false;
export class SkySafetyPass {
 private pp: PostProcess | null = null;
 private scene: Scene | null = null;
 private camera: Camera | null = null;
 private active = 0;
 private seed = 0;
 private errorObserver: any = null;
 private failed = false;

 attach(scene: Scene, camera: Camera): void {
  this.dispose();
  this.scene = scene;
  this.camera = camera;
 }

 private ensure(): void {
  const scene = this.scene, camera = this.camera;
  if (this.pp || this.failed || !scene || !camera) return;
  try {
   if (!registered) {
    Effect.ShadersStore[NAME + 'FragmentShader'] = FRAG;
    registered = true;
   }
   const engine = scene.getEngine();
   this.errorObserver = engine.onEffectErrorObservable.add(({ effect, errors }) => {
    let source = '';
    try {
     const fx = effect as any;
     source = fx.getFragmentShaderSource?.() ?? fx._fragmentSourceCode ?? String(fx.name ?? '');
    } catch {}
    if (!source.includes('u_skyEnabled')) return;
    this.failed = true;
    this.active = 0;
    console.warn('Extreme sky safety shader disabled; base scene preserved:', errors);
    setTimeout(() => { this.pp?.dispose(); this.pp = null; }, 0);
   });
   this.pp = new PostProcess(
    NAME, NAME,
    ['u_skyEnabled', 'seed', 'resolution', 'viewInverse', 'fov', 'aspect'],
    null, 1, camera, Texture.BILINEAR_SAMPLINGMODE, engine, false
   );
   this.pp.onApply = (e) => {
    const g = scene.getEngine();
    const w = g.getRenderWidth() || 1;
    const h = g.getRenderHeight() || 1;
    e.setFloat('u_skyEnabled', this.active);
    e.setFloat('seed', this.seed);
    e.setFloat2('resolution', w, h);
    e.setFloat('aspect', w / Math.max(1, h));
    e.setFloat('fov', camera.fov ?? 0.9);
    const inv = Matrix.Invert(camera.getViewMatrix());
    e.setMatrix('viewInverse', inv);
   };
  } catch (e) {
   this.failed = true;
   this.active = 0;
   console.warn('Extreme sky safety unavailable; base scene preserved:', e);
   this.pp?.dispose();
   this.pp = null;
  }
 }

 update(dt: number, x: number, y: number, z: number, allow = true): void {
  const extreme = Math.max(Math.abs(x), Math.abs(y), Math.abs(z));
  this.active = allow && extreme > 1e6 && !this.failed ? 1 : 0;
  this.seed = ((Math.floor(x / 260000) ^ Math.floor(z / 260000)) >>> 0) % 997 / 997;
  if(this.active)this.ensure();
 }

 get enabled(): boolean { return this.active > 0 && !this.failed; }

 dispose(): void {
  if (this.errorObserver && this.scene) {
   try { this.scene.getEngine().onEffectErrorObservable.remove(this.errorObserver); } catch {}
  }
  this.errorObserver = null;
  this.pp?.dispose();
  this.pp = null;
  this.scene = null;
  this.camera = null;
  this.failed = false;
  this.active = 0;
 }
}
