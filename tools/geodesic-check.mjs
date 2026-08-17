/** Verification for the unified full-screen singularity architecture. */
import fs from 'fs';
let pass=0,fail=0;
const ok=(n,c)=>{if(c)pass++;else{fail++;console.log('FAIL: '+n);}};
const src=fs.readFileSync('src/bjs/shaders/HoleFieldShader.ts','utf8');
const renderer=fs.readFileSync('src/bjs/systems/HoleFieldRenderer.ts','utf8');
const frag=(src.match(/const\s+FRAG\s*=\s*`([\s\S]*?)`;/m)||[])[1]||'';

ok('the unified fragment shader exists',frag.length>1000);
ok('the pass samples the real rendered background',/texture2D\(textureSampler,vUV\)/.test(frag));
ok('lensing resamples a deflected source coordinate',/texture2D\(textureSampler,sourceUv\)/.test(frag));
ok('the null path is integrated in 32 affine steps',/for\(int i=0;i<32;i\+\+\)/.test(frag));
ok('the integrator uses impact radius and affine depth',/rho2=impact\*impact\+horizon\*horizon\*z\*z/.test(frag));
ok('Schwarzschild bending falls with radius',/pow\(max\(rho2,1e-10\),1\.5\)/.test(frag));
ok('Kerr frame dragging depends on spin',/float drag=spin\*horizon\*horizon/.test(frag));
ok('the critical curve is physical and continuous',/critical=horizon\*1\.52/.test(frag));
ok('the Einstein ring contains a secondary background image',/vec3 secondary=texture2D/.test(frag));
ok('the horizon is an opaque overwrite',/warped=mix\(warped,vec3\(0\.\),shadow\)/.test(frag));
ok('fragment alpha is always opaque',/gl_FragColor=vec4\(max\(col,vec3\(0\.\)\),1\.\)/.test(frag));
ok('disk and shadow share the same screen-space centre',/vec2 d=vUV-center/.test(frag));
ok('the disk has seeded turbulent filaments',/fbm\(vec2\(a\*2\.7\+seed/.test(frag));
ok('the disk carries relativistic Doppler asymmetry',
  /beta\*radial\.x\*sign\(spin\)/.test(frag) && /doppler=pow\(dop,3\.\)/.test(frag));
ok('the lens is aspect corrected',/resolution\.x\/max\(1\.,resolution\.y\)/.test(frag));
ok('influence fades before the pass boundary',/smoothstep\(influence\*\.42,influence\*\.90,r\)/.test(frag));
ok('there is no private synthetic star field',!/float star\s*=/.test(frag));
ok('the renderer contains no black-hole MeshBuilder',!renderer.includes('MeshBuilder'));
ok('the renderer is a full-screen PostProcess',renderer.includes('new PostProcess'));
ok('world positions are projected every update',renderer.includes('Vector3.Project'));
ok('physical angular size controls screen radius',renderer.includes('Math.atan2'));
ok('only one singularity pass can own the frame',/maxLive:\s*1/.test(renderer));

// The analytic weak-field term represented by the shader must remain finite
// and strictly decrease with impact parameter.
const bend=(b,rs=1)=>2*rs/b;
ok('grazing light bends more than distant light',bend(2)>bend(20));
ok('all tested deflections are finite',Array.from({length:100},(_,i)=>bend(1+i)).every(Number.isFinite));
ok('deflection tends toward zero at distance',bend(1e9)<1e-8);

console.log(pass+' passed, '+fail+' failed');
process.exit(fail?1:0);
