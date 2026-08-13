/**
 * Static shader verification.
 *  1. Parses every GLSL source with a real GLSL parser.
 *  2. Cross-checks declared uniforms against the TypeScript bindings.
 *  3. Checks varying agreement between each vertex/fragment pair.
 *
 * Run: node tools/shader-check.mjs
 */
import { parser } from '@shaderfrog/glsl-parser';
import fs from 'fs';

const read = (f) => fs.readFileSync(f, 'utf8');

function extract(file, names) {
  const src = read(file);
  const out = {};
  for (const n of names) {
    const m = src.match(new RegExp('(?:export )?const\\s+' + n + '\\s*=\\s*`([\\s\\S]*?)`;', 'm'));
    if (m) out[n] = m[1];
  }
  return out;
}

const noise = extract('src/bjs/Noise.ts', ['GLSL_NOISE']).GLSL_NOISE;
const gerstner = extract('src/bjs/worlds/OceanWorld.ts', ['GERSTNER_GLSL'])
  .GERSTNER_GLSL.replace('${GLSL_NOISE}', noise);

const resolve = (s) =>
  s.replace('${GLSL_NOISE}', noise).replace('${GERSTNER_GLSL}', gerstner);

// [sourceFile, vertName, fragName, tsFilesThatBindTheUniforms]
const PAIRS = [
  ['src/bjs/worlds/BlackHoleWorld.ts', 'VERT', 'FRAG', ['src/bjs/worlds/BlackHoleWorld.ts']],
  ['src/bjs/worlds/OceanWorld.ts', 'OCEAN_VERT', 'OCEAN_FRAG', ['src/bjs/worlds/OceanWorld.ts']],
  ['src/bjs/shaders/PlanetShader.ts', 'PLANET_VERT', 'PLANET_FRAG',
   ['src/bjs/worlds/PlanetaryWorld.ts', 'src/bjs/worlds/SandboxWorld.ts', 'src/bjs/PlanetMaps.ts']],
  ['src/bjs/worlds/PlanetaryWorld.ts', 'ATMO_VERT', 'ATMO_FRAG', ['src/bjs/worlds/PlanetaryWorld.ts']],
  ['src/bjs/shaders/PortalShader.ts', 'PORTAL_VERT', 'PORTAL_FRAG', ['src/bjs/systems/PortalSystem.ts']]
];

const uniformsOf = (s) =>
  [...s.matchAll(/^\s*uniform\s+\w+\s+([A-Za-z_]\w*)\s*(\[|;)/gm)].map((m) => m[1]);
const varyingsOf = (s) =>
  [...s.matchAll(/^\s*varying\s+\w+\s+([A-Za-z_]\w*)\s*;/gm)].map((m) => m[1]);

const AUTO = new Set(['world', 'worldViewProjection', 'view', 'projection', 'viewProjection']);

let problems = 0;
const seen = new Set();

for (const [file, vName, fName, tsFiles] of PAIRS) {
  const got = extract(file, [vName, fName]);
  const short = file.split('/').pop();
  const ts = tsFiles.map(read).join('\n');

  for (const [k, raw] of [[vName, got[vName]], [fName, got[fName]]]) {
    if (raw === undefined) { console.log('MISSING', short, k); problems++; continue; }
    const s = resolve(raw);
    if (/\$\{/.test(s)) { console.log('UNRESOLVED TEMPLATE', short, k); problems++; }
    const key = short + ':' + k;
    if (seen.has(key)) continue;
    seen.add(key);
    try {
      parser.parse(s);
      console.log('  parse OK   ', short, k);
    } catch (e) {
      problems++;
      console.log('  parse FAIL ', short, k, '->', String(e.message).split('\n')[0]);
    }
  }

  const vs = resolve(got[vName] ?? '');
  const fsrc = resolve(got[fName] ?? '');

  const vOut = new Set(varyingsOf(vs));
  for (const v of varyingsOf(fsrc)) {
    if (!vOut.has(v)) {
      console.log(`  varying MISMATCH ${short} ${fName}: "${v}" read but never written`);
      problems++;
    }
  }

  for (const u of new Set([...uniformsOf(vs), ...uniformsOf(fsrc)])) {
    if (AUTO.has(u)) continue;
    const q = "['\"`]";
    const bound =
      new RegExp('set\\w+\\(\\s*' + q + u + q).test(ts) ||
      new RegExp(q + u + q).test(ts) ||
      new RegExp('\\.' + u + '\\b').test(ts) ||
      // struct arrays are bound per member, e.g. `waves[${i}].dir`
      new RegExp(u + '\\[\\$\\{\\w+\\}\\]\\.').test(ts) ||
      new RegExp(u + '\\[\\d+\\]\\.').test(ts);
    if (!bound) {
      console.log(`  uniform UNBOUND  ${short}: "${u}" declared but never set`);
      problems++;
    }
  }
}

console.log(problems === 0
  ? '\nAll shaders parse; varyings and uniforms are consistent.'
  : `\n${problems} problem(s) found.`);
process.exit(problems ? 1 : 0);
