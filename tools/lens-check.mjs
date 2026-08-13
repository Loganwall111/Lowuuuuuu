/**
 * LensProfiles verification — each black hole must bend light its own way,
 * some must have no photon ring at all, and a fully custom profile must
 * never be able to produce a broken or invisible image.
 * Run: node tools/lens-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/LensProfiles.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/lens-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const {
  LENS_PROFILES, LENS_ORDER, LENS_MODE_ID, LENS_FIELDS, ALIEN_MODES,
  cloneProfile, sanitizeProfile, randomAlienProfile, describeProfile
} = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const all = LENS_ORDER.map((m) => LENS_PROFILES[m]);

console.log('\n— the catalogue —');
{
  ok(`many lens types exist (${all.length})`, all.length >= 10);
  ok('every ordered mode resolves', all.every(Boolean));
  ok('each has identity and a blurb',
     all.every((p) => p.name && p.glyph && p.blurb));
  ok('modes are unique', new Set(all.map((p) => p.mode)).size === all.length);
  ok('names are unique', new Set(all.map((p) => p.name)).size === all.length);
  ok('every mode has a numeric id for the shader',
     all.every((p) => Number.isInteger(LENS_MODE_ID[p.mode])));
  ok('the numeric ids are unique',
     new Set(Object.values(LENS_MODE_ID)).size === Object.keys(LENS_MODE_ID).length);
}

console.log('\n— NOT every hole has a photon ring —');
{
  const ringless = all.filter((p) => p.ring === 0);
  ok(`some lenses have no photon ring at all (${ringless.map((p) => p.name).join(', ')})`,
     ringless.length >= 2);
  ok('the Ringless profile really has none', LENS_PROFILES.ringless.ring === 0);
  ok('the flat profile has none either', LENS_PROFILES.flat.ring === 0);
  const withRing = all.filter((p) => p.ring > 0);
  ok(`and plenty still do (${withRing.length})`, withRing.length >= 6);
  ok('ring radius is zero when there is no ring',
     ringless.every((p) => p.ringRadius === 0));
}

console.log('\n— the lenses are genuinely different from each other —');
{
  const sig = (p) => [p.strength, p.falloff, p.ring, p.ringRadius, p.symmetry,
                      p.distortion, p.twist, p.chroma].join('|');
  ok('no two profiles share the same parameters',
     new Set(all.map(sig)).size === all.length);
  ok('deflection strength varies',
     new Set(all.map((p) => p.strength)).size >= 6);
  ok('falloff varies, so some bend gently and some violently',
     new Set(all.map((p) => p.falloff)).size >= 4);
  ok('some lenses are radially symmetric and some are folded',
     all.some((p) => p.symmetry === 0) && all.some((p) => p.symmetry > 0));
  ok('one lens bends light the wrong way', all.some((p) => p.strength < 0));
  ok('one lens does not bend light at all',
     all.some((p) => p.strength === 0));
  ok('some lenses split light chromatically',
     all.some((p) => p.chroma > 0) && all.some((p) => p.chroma === 0));
  ok('some lenses twist the image', all.some((p) => Math.abs(p.twist) > 0.1));
}

console.log('\n— alien lenses —');
{
  ok(`there are several alien modes (${ALIEN_MODES.length})`, ALIEN_MODES.length >= 5);
  ok('every alien mode is a real profile',
     ALIEN_MODES.every((m) => !!LENS_PROFILES[m]));
  const aliens = ALIEN_MODES.map((m) => LENS_PROFILES[m]);
  // "alien" means visibly unlike Schwarzschild by SOME mechanism: angular
  // distortion, twist, chromatic splitting, folding, or reversed deflection.
  const ref = LENS_PROFILES.schwarzschild;
  ok('every alien lens differs from the textbook one by some mechanism',
     aliens.every((p) => p.distortion > 0 || Math.abs(p.twist) > 0 ||
                         p.chroma > 0 || p.symmetry > 0 ||
                         Math.sign(p.strength) !== Math.sign(ref.strength)),
     aliens.filter((p) => !(p.distortion > 0 || Math.abs(p.twist) > 0 ||
                            p.chroma > 0 || p.symmetry > 0 ||
                            Math.sign(p.strength) !== Math.sign(ref.strength)))
           .map((p) => p.name).join(', '));
  ok('the repulsive lens is alien by bending light backwards',
     LENS_PROFILES.inverted.strength < 0);
  ok('the hexagonal lens has six-fold symmetry', LENS_PROFILES.hexagonal.symmetry === 6);
  ok('the kaleidoscope repeats the sky', LENS_PROFILES.kaleidoscope.symmetry >= 4);
}

console.log('\n— fully customizable: every field is editable and bounded —');
{
  ok(`every tunable field is exposed (${LENS_FIELDS.length})`, LENS_FIELDS.length >= 8);
  ok('each field has a sane range',
     LENS_FIELDS.every((f) => f.max > f.min && f.step > 0));
  ok('each field exists on a real profile',
     LENS_FIELDS.every((f) => f.key in LENS_PROFILES.schwarzschild));

  // editing one profile must not corrupt the shared catalogue
  const c = cloneProfile(LENS_PROFILES.schwarzschild);
  c.strength = 99;
  c.tint[0] = 0;
  ok('cloning gives an independent copy',
     LENS_PROFILES.schwarzschild.strength === 1.0 &&
     LENS_PROFILES.schwarzschild.tint[0] === 1);
}

console.log('\n— a custom profile can never break the renderer —');
{
  const evil = {
    ...cloneProfile(LENS_PROFILES.schwarzschild),
    strength: 1e9, falloff: -50, ring: -20, ringRadius: 1e6,
    symmetry: 9999, distortion: NaN, twist: Infinity, chroma: -3,
    softness: 1e9, tint: [-5, NaN, 40]
  };
  const s = sanitizeProfile(evil);
  ok('absurd values are clamped into range',
     LENS_FIELDS.every((f) => s[f.key] >= f.min && s[f.key] <= f.max),
     JSON.stringify(LENS_FIELDS.filter((f) => s[f.key] < f.min || s[f.key] > f.max)));
  ok('NaN is replaced with a usable number',
     Number.isFinite(s.distortion) && Number.isFinite(s.twist));
  ok('the tint stays a valid colour',
     s.tint.every((v) => Number.isFinite(v) && v >= 0 && v <= 1), JSON.stringify(s.tint));
  ok('a fully black tint is rejected (lensed light must stay visible)',
     s.tint[0] + s.tint[1] + s.tint[2] > 0.15, JSON.stringify(s.tint));

  const blacked = sanitizeProfile({ ...cloneProfile(LENS_PROFILES.soft), tint: [0, 0, 0] });
  ok('an all-black tint is substituted, never rendered',
     blacked.tint[0] + blacked.tint[1] + blacked.tint[2] > 0.15);

  // every stock profile must already be valid
  const dirty = all.filter((p) => {
    const q = sanitizeProfile(p);
    return LENS_FIELDS.some((f) => Math.abs(q[f.key] - p[f.key]) > 1e-9);
  });
  ok('every built-in profile is already within range', dirty.length === 0,
     dirty.map((p) => p.name).join(', '));
}

console.log('\n— the random alien generator —');
{
  let bad = [];
  const seen = new Set();
  let seed = 1;
  const rand = () => {
    seed = (seed * 1103515245 + 12345) % 2147483648;
    return seed / 2147483648;
  };
  for (let i = 0; i < 400; i++) {
    const p = randomAlienProfile(rand);
    seen.add([p.strength.toFixed(2), p.symmetry, p.twist.toFixed(2)].join('|'));
    if (LENS_FIELDS.some((f) => !Number.isFinite(p[f.key]) || p[f.key] < f.min || p[f.key] > f.max)) {
      bad.push(p.name + ' out of range');
    }
    if (!p.tint.every((v) => v >= 0 && v <= 1)) bad.push(p.name + ' bad tint');
    if (p.tint[0] + p.tint[1] + p.tint[2] < 0.15) bad.push(p.name + ' black tint');
  }
  ok('400 random alien lenses are all valid', bad.length === 0, bad.slice(0, 3).join(' | '));
  ok(`the generator produces real variety (${seen.size} distinct)`, seen.size > 200);
}

console.log('\n— descriptions for the UI —');
{
  for (const p of all) {
    const d = describeProfile(p);
    if (!d.Lens || !d.Mode || !d.Strength) {
      ok('every profile describes cleanly', false, p.mode);
      break;
    }
  }
  ok('every profile describes cleanly', true);
  ok('a ringless lens says so in words',
     describeProfile(LENS_PROFILES.ringless)['Photon ring'] === 'none');
  ok('a ringed lens reports its strength',
     describeProfile(LENS_PROFILES.schwarzschild)['Photon ring'] !== 'none');
  ok('a folded lens reports its symmetry',
     /6-fold/.test(describeProfile(LENS_PROFILES.hexagonal).Symmetry));
  ok('a radial lens says radial',
     describeProfile(LENS_PROFILES.schwarzschild).Symmetry === 'radial');
}

console.log('\n— lensing works everywhere, not just in one world —');
{
  // The regression: the warp lived inside BlackHoleWorld's raymarcher, so
  // you only saw it if you switched to that world. In one continuous
  // universe it has to bend whatever is on screen.
  const lfx = fs.readFileSync('src/bjs/systems/LensFX.ts', 'utf8');
  ok('there is a universal lensing pass', lfx.includes('universalLens'));
  ok('it is a screen-space post-process', lfx.includes('PostProcess'));
  ok('deflection falls off with distance', lfx.includes('pow(clamp(holeR / rr'));
  ok('it honours alien lens shapes',
     lfx.includes('symmetry') && lfx.includes('distortion') && lfx.includes('twist'));
  ok('ringless holes are supported', lfx.includes('ringAmt > 0.001'));
  // This used to pin the literal expression `mix(col, tint * 0.05, inside)`,
  // which is precisely the line that produced the reported black screen: it
  // replaces the frame with a flat wash, and because the shadow radius was
  // unbounded it applied to every pixel once you got close. The assertion
  // name was right; what it checked was not. Now it checks the two
  // properties that actually keep the screen alive.
  ok('the horizon shadow can never cover the whole frame',
     lfx.includes('min(holeR, 0.42)'));
  ok('even inside the horizon some lensed light survives',
     /mix\(col, col \* 0\.0\d+ \+ tint/.test(lfx));
  ok('it switches itself off when idle', lfx.includes('active < 0.5'));
  ok('a failed post-process cannot stop rendering', lfx.includes('Gravitational lensing unavailable'));

  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  ok('the app owns a universal lens', app.includes('new LensFX'));
  ok('it is attached on every world load', app.includes('this.lensfx.attach('));
  ok('it tracks the nearest hole each frame', app.includes('this.lensfx.track('));
  ok('it clears when no hole is near', app.includes('this.lensfx.clear()'));
  ok('lensing state is reported in telemetry', app.includes('this.lensfx.stats()'));

  // It must not be gated on being in the blackhole world.
  const trackIdx = app.indexOf('this.lensfx.track(');
  const slice = app.slice(Math.max(0, trackIdx - 900), trackIdx);
  ok('lensing is not gated on the blackhole world',
     !/currentId\s*===\s*'blackhole'/.test(slice));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
