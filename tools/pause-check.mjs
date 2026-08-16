/**
 * pause-check — the in-game Escape menu, local leaderboard, and save flow.
 *
 * The final-polish pause surface is pinned here:
 *
 *   - the leaderboard is a deterministic, sorted table that slots the player
 *     in by a real score, best first,
 *   - the player score rises with distance, discoveries, milestones and
 *     challenges,
 *   - the pause menu carries Settings / Performance / Save / Quit & Save /
 *     Dashboard / Leaderboard,
 *   - Escape toggles it only once the intro is done,
 *   - manual save and autosave are both present.
 *
 * Run: node tools/pause-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (rel) => {
  const p = path.join(root, rel);
  return fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '';
};

const load = async (entry) => {
  const out = await build({
    entryPoints: ['src/bjs/systems/' + entry],
    bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${entry.replace(/\W/g, '_')}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const L = await load('Leaderboard.ts');
console.log('\n— the local leaderboard slots the player by a real score —');
{
  const rows = L.leaderboard(7, { distance: 100000, discoveries: 5, milestones: 2, challenges: 1 });
  ok('the leaderboard returns ten rows', rows.length === 10);
  ok('it is sorted best first', rows.every((r, i) => i === 0 || rows[i - 1].score >= r.score));
  ok('the player is in the table', rows.some((r) => r.you));
  ok('the same seed gives the same table',
    JSON.stringify(rows) === JSON.stringify(L.leaderboard(7, { distance: 100000, discoveries: 5, milestones: 2, challenges: 1 })));

  ok('score rises with distance',
    L.playerScore({ distance: 1e6, discoveries: 0, milestones: 0, challenges: 0 }) >
    L.playerScore({ distance: 10, discoveries: 0, milestones: 0, challenges: 0 }));
  ok('score rises with discoveries',
    L.playerScore({ distance: 0, discoveries: 20, milestones: 0, challenges: 0 }) >
    L.playerScore({ distance: 0, discoveries: 0, milestones: 0, challenges: 0 }));
  ok('score rises with milestones and challenges',
    L.playerScore({ distance: 0, discoveries: 0, milestones: 5, challenges: 4 }) >
    L.playerScore({ distance: 0, discoveries: 0, milestones: 0, challenges: 0 }));
  ok('garbage metrics never produce NaN',
    Number.isFinite(L.playerScore({ distance: NaN, discoveries: -5, milestones: 0, challenges: 0 })));
}

console.log('\n— the pause menu carries every surface —');
{
  const menu = read('src/bjs/ui/PauseMenu.ts');
  for (const s of ['Settings', 'Performance', 'Save', 'Quit &amp; Save', 'Dashboard', 'Leaderboard', 'Resume']) {
    ok('the menu has "' + s + '"', menu.includes(s));
  }
}

console.log('\n— Escape, save and autosave are wired —');
{
  const app = read('src/bjs/App.ts');
  ok('Escape toggles the pause menu once the intro is done',
    app.includes("e.key === 'Escape'") && app.includes('pauseMenu.toggle()') &&
    app.includes('this.intro.state.done'));
  ok('the pause menu is mounted at boot', app.includes('this.pauseMenu.mount()'));
  ok('a manual save exists', app.includes('saveNow()'));
  ok('autosave is still running', app.includes('this.saves.tick('));
  const css = read('src/bjs/ui/styles.ts');
  ok('the pause menu is styled as cockpit glass', /\.pause-menu/.test(css) && /\.pause-panel/.test(css));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
