/**
 * Unified screen-space Schwarzschild/Kerr lens.
 *
 * This pass owns every open-universe black-hole pixel. It replaces the old
 * camera-facing plane/card architecture: there is no mesh edge, billboard,
 * translucent bubble, or second black disc to drift away from the lens.
 */
export const HOLE_FIELD_SHADER = 'unifiedSingularity';

// Retained for standalone shader verification; Babylon's PostProcess supplies
// its equivalent full-screen vertex stage at runtime.
export const VERT = `
precision highp float;
attribute vec2 position;
varying vec2 vUV;
void main(){vUV=position*.5+.5;gl_Position=vec4(position,0.,1.);}
`;

export const FRAG = `
precision highp float;
varying vec2 vUV;
uniform sampler2D textureSampler;
uniform vec2 center;
uniform vec2 resolution;
uniform float horizon;
uniform float time;
uniform float u_holeEnabled;
uniform float spin;
uniform float diskInner;
uniform float diskOuter;
uniform float diskTilt;
uniform float diskBright;
uniform float temperature;
uniform float seed;
// Full procedural lens vocabulary—every authored and generated variant.
uniform float lensMode;
uniform float lensStrength;
uniform float lensFalloff;
uniform float ringAmount;
uniform float ringRadius;
uniform float lensSymmetry;
uniform float lensDistortion;
uniform float lensTwist;
uniform float lensChroma;
uniform vec3 lensTint;
uniform float lensSoftness;

float hash21(vec2 p){
  p=fract(p*vec2(123.34,456.21));
  p+=dot(p,p+45.32);
  return fract(p.x*p.y);
}
float noise(vec2 p){
  vec2 i=floor(p),f=fract(p); f=f*f*(3.0-2.0*f);
  return mix(mix(hash21(i),hash21(i+vec2(1.,0.)),f.x),
             mix(hash21(i+vec2(0.,1.)),hash21(i+vec2(1.,1.)),f.x),f.y);
}
float fbm(vec2 p){
  float n=0.,a=.5;
  for(int i=0;i<5;i++){n+=noise(p)*a;p=p*2.03+17.7;a*=.5;}
  return n;
}
vec3 tonemapACES(vec3 x){
  return (x*(2.51*x+.03))/(x*(2.43*x+.59)+.14);
}

void main(){
  vec4 scene=texture2D(textureSampler,vUV);
  if(u_holeEnabled<.5 || horizon<.000001){gl_FragColor=vec4(scene.rgb,1.);return;}

  float aspect=resolution.x/max(1.,resolution.y);
  vec2 d=vUV-center;
  d.x*=aspect;
  float r=length(d);
  float influence=horizon*11.5;
  if(r>influence){gl_FragColor=vec4(scene.rgb,1.);return;}

  vec2 radial=d/max(r,1e-7);
  float theta=atan(radial.y,radial.x);
  vec2 lensRadial=radial;
  // Shattered and kaleidoscope lenses fold the source direction into
  // deterministic wedges; the physical hole remains anchored in world space.
  if(lensMode>5.5&&lensMode<7.5&&lensSymmetry>1.){
    float sector=6.2831853/lensSymmetry;
    float local=mod(theta+sector*.5,sector)-sector*.5;
    if(lensMode>6.5)local=abs(local);
    else local=floor((theta+sector*.5)/sector)*sector;
    lensRadial=vec2(cos(local),sin(local));
  }
  float angularShape=lensSymmetry>1.
    ? 1.+cos(theta*lensSymmetry)*lensDistortion*.14 : 1.;
  float effectiveR=max(r*angularShape,1e-7);
  // All bending reaches exactly zero before the effect boundary. Without
  // this field taper the differently sampled/graded interior ended at a
  // circular seam—the translucent bubble reported around every hole.
  float lensFade=1.-smoothstep(influence*.42,influence*.90,r);

  // Integrate the weak-field null-geodesic deflection in 32 affine steps.
  // The accumulated 2Rs/b term is the Schwarzschild Einstein bend; frame
  // dragging adds the signed Kerr term rather than rotating a texture card.
  float impact=max(effectiveR,horizon*.82);
  float deflect=0.;
  for(int i=0;i<32;i++){
    float s=(float(i)+.5)/32.;
    float z=(s*2.-1.)*7.;
    float rho2=impact*impact+horizon*horizon*z*z;
    deflect+=(2.*horizon*horizon*impact/pow(max(rho2,1e-10),1.5))/32.;
  }
  float falloffShape=pow(max(horizon/max(effectiveR,horizon*.25),.02),lensFalloff-1.);
  deflect*=horizon*7.2*lensFade*lensStrength*falloffShape;
  if(lensMode>8.5&&lensMode<9.5)deflect*=1.+sin(effectiveR/max(horizon,.0001)*5.)*.32*lensDistortion;
  float drag=(spin*.16+lensTwist*.12)*horizon*horizon/
    max(effectiveR*effectiveR,horizon*horizon)*lensFade;
  float cs=cos(drag),sn=sin(drag);
  vec2 dragged=vec2(lensRadial.x*cs-lensRadial.y*sn,lensRadial.x*sn+lensRadial.y*cs);
  vec2 sourceD=dragged*(r+deflect);
  vec2 sourceUv=center+vec2(sourceD.x/aspect,sourceD.y);
  sourceUv=clamp(sourceUv,vec2(.001),vec2(.999));
  vec3 lensed=texture2D(textureSampler,sourceUv).rgb;
  if(lensChroma>.001){
    vec2 chromaOff=vec2(dragged.x/aspect,dragged.y)*horizon*.075*lensChroma*lensFade;
    lensed.r=texture2D(textureSampler,clamp(sourceUv+chromaOff,vec2(.001),vec2(.999))).r;
    lensed.b=texture2D(textureSampler,clamp(sourceUv-chromaOff,vec2(.001),vec2(.999))).b;
  }
  lensed*=mix(vec3(1.),lensTint,.22*clamp(abs(lensStrength),0.,2.));

  // A second geodesic image condenses at the critical curve, forming a
  // continuous Einstein ring from real background light rather than glow.
  float critical=horizon*max(1.05,ringRadius);
  float ringWidth=max(horizon*mix(.055,.19,clamp(lensSoftness,0.,1.)),.0012);
  float einstein=exp(-pow((effectiveR-critical)/ringWidth,2.))*ringAmount;
  float mirroredR=critical+abs(r-critical)*2.4;
  vec2 mirrorUv=center+vec2(radial.x*mirroredR/aspect,radial.y*mirroredR);
  vec3 secondary=texture2D(textureSampler,clamp(mirrorUv,vec2(.001),vec2(.999))).rgb;
  lensed=mix(lensed,secondary,einstein*.82);

  // A higher-order photon image wraps the opposite hemisphere back around
  // the shadow. This is narrow and dimmer than the primary Einstein image,
  // matching the repeated images produced by near-critical null geodesics.
  float photonCritical=horizon*1.30;
  float photonRing=exp(-pow((effectiveR-photonCritical)/max(horizon*.055,.0007),2.))*ringAmount;
  float tertiaryR=critical+abs(r-photonCritical)*4.8;
  vec2 tertiaryUv=center-vec2(radial.x*tertiaryR/aspect,radial.y*tertiaryR);
  vec3 tertiary=texture2D(textureSampler,clamp(tertiaryUv,vec2(.001),vec2(.999))).rgb;
  lensed=mix(lensed,tertiary,photonRing*.58);

  // Relativistic accretion volume. It is evaluated in the same pass and
  // shares the exact same centre, so disk, shadow and lens cannot separate.
  float a=atan(d.y,d.x)+time*(.025+.045*spin);
  float tilt=max(.13,abs(sin(diskTilt)));
  float elliptical=length(vec2(d.x,d.y/tilt));
  float din=horizon*diskInner;
  float dout=horizon*diskOuter;
  float outerRamp=1.-smoothstep(dout-horizon,dout,elliptical);
  float radialBand=smoothstep(din,din+horizon*.65,elliptical)
    *outerRamp * outerRamp;
  float turbulence=fbm(vec2(a*2.7+seed*19.,elliptical/max(horizon,.00001)*.44-time*.08));
  float filaments=.32+.68*smoothstep(.28,.82,turbulence);
  float beam=pow(max(0.,1.-abs(d.y)/(horizon*(.35+tilt*.7))),3.2);
  float beta=clamp(abs(spin)*sqrt(horizon/(2.*max(elliptical,horizon*1.5))),0.,.58);
  float dop=1./max(.2,1.-beta*radial.x*sign(spin));
  dop=clamp(dop,.2,1.8);
  float doppler=pow(dop,3.);
  vec3 cool=vec3(.95,.20,.035), hot=vec3(1.,.88,.56);
  vec3 gas=mix(cool,hot,clamp(temperature,0.,1.));
  gas*=radialBand*beam*filaments*diskBright*(.18+.34*doppler);
  // Tone-map emitted gas only. Re-tonemapping the already graded background
  // inside a circular coverage mask was the visible grey/purple bubble.
  gas=tonemapACES(min(gas,vec3(2.4)));

  // Exactly opaque event horizon. Ordered edge masks are defined on every
  // GLSL implementation and alpha is always one.
  float shadowRadius=horizon*1.08;
  float shadowEdge=max(horizon*(.035+lensSoftness*.28),.0008);
  float shadow=1.-smoothstep(shadowRadius,shadowRadius+shadowEdge,effectiveR);
  vec3 warped=mix(scene.rgb,lensed,lensFade)+gas;
  warped=mix(warped,vec3(0.),shadow);
  float coverage=max(shadow,max(lensFade,radialBand));
  vec3 col=mix(scene.rgb,warped,clamp(coverage,0.,1.));
  gl_FragColor=vec4(max(col,vec3(0.)),1.);
}`;

export const HOLE_FIELD_FRAG=FRAG;

export function registerHoleFieldShader(store: Record<string,string>): void {
  store[HOLE_FIELD_SHADER+'FragmentShader']=FRAG;
}
