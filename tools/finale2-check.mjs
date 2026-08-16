/**
 * finale2-check — the absolute-last interface polish.
 *
 * Pins the closing batch of requests:
 *   - the menu: Explore/Sandbox at the top, a single Create World at the
 *     bottom, patch notes moved to the launch row, and a blue line through
 *     the title,
 *   - the boot-to-menu: staged "loading / transmitting / device open" lines
 *     and a "SYSTEMS CALIBRATED" flare,
 *   - the command center: a horizontal bottom strip with two blue rails and
 *     an outer line, buttons centred,
 *   - no config panels auto-open on entering the game,
 *   - the Subnautica voice tune and the ambient-score echo.
 *
 * Run: node tools/finale2-check.mjs
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (rel) => {
  const p = path.join(root, rel);
  return fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '';
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const intro = read('src/bjs/ui/IntroOverlay.ts');
const shell = read('src/bjs/ui/Shell.ts');
const css = read('src/bjs/ui/styles.ts');
const omni = read('src/bjs/ui/OmniBoot.ts');
const music = read('src/bjs/systems/SpaceMusic.ts');

console.log('\n— the menu is two doors on top, one Create World at the bottom —');
{
  ok('there are still exactly two intro-play doors',
    (intro.match(/className = 'intro-play/g) ?? []).length === 2);
  ok('Create World is the single bottom action', intro.includes('Create World'));
  ok('patch notes moved out of the mode menu', !/modes\.appendChild\(notes\)/.test(intro));
  ok('a blue line runs through the middle of the title',
    intro.includes('intro-centerline') && /\.intro-centerline/.test(intro));
}

console.log('\n— the boot-to-menu is staged, with a mech animation —');
{
  for (const line of ['LOADING SYSTEM', 'TRANSMITTING DEVICE', 'DEVICE OPEN']) {
    ok('boot line "' + line + '"', shell.includes(line));
  }
  ok('the systems-calibrated flare exists', shell.includes('SYSTEMS CALIBRATED'));
  ok('the boot stages light up as thresholds cross', /data-step=/.test(shell));
  ok('the mech helm animates', /\.boot-helm/.test(css) && /cmdSpin/.test(css));
}

console.log('\n— the command center is a horizontal bottom rail —');
{
  ok('the buttons run in a single centred row',
    /\.cmd-grid\{[^}]*display:flex/.test(css) && /justify-content:center/.test(css));
  ok('two blue lines frame the strip',
    /\.cmd-center\{[\s\S]{0,420}border-top:1px solid/.test(css) &&
    /\.cmd-center\{[\s\S]{0,420}border-bottom:1px solid/.test(css));
  ok('an outer glow line sits outside the rails',
    /\.cmd-center::before/.test(css));
}

console.log('\n— no config menus open on entering the game —');
{
  ok('onMenuClosed no longer auto-opens panels',
    !/wm\.Open\('controls'\)/.test(shell) && !/wm\.Open\('objects'\)/.test(shell));
}

console.log('\n— the Subnautica voice and score —');
{
  ok('the voice is tuned low and measured', /u\.pitch = 0\.9/.test(omni) && /u\.rate = 0\.82/.test(omni));
  ok('the voice prefers calm female registers', /samantha|zira|aria|jenny|karen|veena|moira/i.test(omni));
  ok('the score sits deep (E2)', music.includes('rootHz: 82.4'));
  ok('the score has a vast echo tail', music.includes('createDelay') && music.includes('createBiquadFilter'));
  ok('notes are soft sine pads', /'sine'/.test(music));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
