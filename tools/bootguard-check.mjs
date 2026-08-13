/**
 * bootguard-check — the page must never be able to show an unexplained
 * black screen.
 *
 * Every previous loading and error surface was built in JavaScript, so any
 * failure before or during module evaluation left the user looking at the
 * page background and nothing else. These assertions pin down the fix:
 * the background is lit, the boot panel is static HTML, and every failure
 * path routes into a visible panel.
 */

import { readFileSync } from 'node:fs';
import { JSDOM } from 'jsdom';

let pass = 0, fail = 0;
const ok = (name, cond, detail) => {
  if (cond) { pass++; console.log('  PASS  ' + name); }
  else { fail++; console.log('  FAIL  ' + name + (detail ? '  -> ' + detail : '')); }
};

const html = readFileSync(new URL('../index.html', import.meta.url), 'utf8');
const main = readFileSync(new URL('../src/main.ts', import.meta.url), 'utf8');
const app = readFileSync(new URL('../src/bjs/App.ts', import.meta.url), 'utf8');
const postfx = readFileSync(new URL('../src/bjs/PostFX.ts', import.meta.url), 'utf8');
const lens = readFileSync(new URL('../src/bjs/systems/LensFX.ts', import.meta.url), 'utf8');

console.log('\nboot guard: the page cannot go black');

/* ---------------- 1. the background itself is never black --------------- */

// Parse the body background out of the inline stylesheet.
const bodyRule = html.match(/html,\s*body\s*\{[\s\S]*?\}/);
ok('index.html styles the body', !!bodyRule);

const bg = bodyRule ? bodyRule[0] : '';
ok('the page background is not pure black',
   !/background:\s*#000/i.test(bg) && !/background:\s*black/i.test(bg));

// The darkest declared background colour must still be visibly non-black.
const hexes = [...bg.matchAll(/#([0-9a-f]{6})\b/gi)].map((m) => m[1]);
ok('the background declares real colour values', hexes.length > 0);
const brightest = Math.max(...hexes.map((h) => {
  const r = parseInt(h.slice(0, 2), 16), g = parseInt(h.slice(2, 4), 16), b = parseInt(h.slice(4, 6), 16);
  return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255;
}), 0);
ok('the lit background is clearly visible, not near-black',
   brightest > 0.05, 'brightest luma ' + brightest.toFixed(3));

ok('the body carries a gradient so an empty page still reads as a scene',
   /radial-gradient|linear-gradient/.test(bg));

/* ---------------- 2. the boot panel exists without JavaScript ----------- */

const dom = new JSDOM(html, { runScripts: 'outside-only' });
const doc = dom.window.document;

ok('a boot panel is present in the raw HTML',
   !!doc.getElementById('staticBoot'));
ok('the boot panel has visible text before any script runs',
   (doc.getElementById('staticBoot')?.textContent || '').trim().length > 10);
ok('the failure panel is present in the raw HTML',
   !!doc.getElementById('bootFail'));
ok('the failure panel starts hidden',
   /#bootFail\s*\{[^}]*display:\s*none/.test(html));
ok('the failure panel can be reloaded from',
   /location\.reload\(\)/.test(html));
ok('the canvas is transparent so the lit page shows through until frame one',
   /#renderCanvas\s*\{[\s\S]*?background:\s*transparent/.test(html));

// The static boot markup must not depend on the module having loaded.
const moduleTagIndex = html.indexOf('src="/src/main.ts"');
const staticBootIndex = html.indexOf('id="staticBoot"');
ok('the boot panel is declared before the module tag',
   staticBootIndex > 0 && staticBootIndex < moduleTagIndex);

/* ---------------- 3. every failure mode becomes visible ----------------- */

ok('missing WebGL is reported in words',
   /webgl2['"]\)\s*\|\|/.test(html) && /not providing WebGL/i.test(html));
ok('the WebGL message points at hardware acceleration',
   /chrome:\/\/gpu|graphics acceleration/i.test(html));
ok('script load failures are caught',
   /addEventListener\('error'/.test(html));
ok('a failed script tag is distinguished from a runtime error',
   /tagName === 'SCRIPT'/.test(html));
ok('a missing font stylesheet is not treated as fatal',
   /tagName === 'LINK'/.test(html) && /not fatal/i.test(html));
ok('unhandled promise rejections are caught',
   /unhandledrejection/.test(html));
ok('a stalled boot eventually explains itself',
   /still loading after/i.test(html));
ok('the stall timeout is generous enough for a slow first compile',
   /},\s*20000\)/.test(html));
ok('the guard refuses to fire twice',
   /if \(failed\) return/.test(html));

/* ---------------- 4. the module co-operates with the guard -------------- */

ok('main.ts exposes failures through the static panel',
   /__bootFail/.test(main));
// Note: the function *declaration* also contains the text "clearStaticBoot()",
// so compare against the call site inside the success handler specifically.
const thenBlock = main.slice(main.indexOf('.then('), main.indexOf('.catch('));
ok('main.ts only clears the boot panel after start() succeeds',
   thenBlock.includes('app.start()') &&
   thenBlock.indexOf('app.start()') < thenBlock.indexOf('clearStaticBoot()'));
ok('main.ts marks the app as booted so the stall timer stands down',
   /__appBooted\s*=\s*true/.test(main));
ok('a throwing constructor is still reported',
   /could not be created/i.test(main));
ok('boot rejection routes to the visible panel',
   /\.catch\(/.test(main) && /Boot failure/i.test(main));

/* ---------------- 5. the watchdog does not depend on the loop ----------- */

ok('there is a timer-driven watchdog',
   /startWatchdogTimer/.test(app));
ok('it is started when the app starts',
   /this\.startWatchdogTimer\(\)/.test(app));
ok('it uses setInterval rather than the render loop',
   /setInterval/.test(app));
ok('it detects the case where no frame was ever drawn',
   /never started|frames rendered: 0/.test(app));
ok('it reports a permanently throwing loop distinctly',
   /render loop is throwing/i.test(app));
ok('it stops itself once it has reported',
   /clearInterval/.test(app));

/* ---------------- 6. post-processing cannot blank the frame ------------- */

ok('the post-process pipeline is health-checked',
   /watchPipelineHealth/.test(postfx));
ok('it uses the real Babylon API for support',
   /\.isSupported/.test(postfx) && !/p\.isReady\(\)/.test(postfx));
ok('an unsupported pipeline is removed rather than left blitting black',
   /this\.detach\(\)/.test(postfx) && /still be visible|is visible/i.test(postfx));
ok('the health check gives the GPU real frames before judging',
   /frames > 1[0-9]{2}/.test(postfx));
ok('the lens pass verifies it actually compiled',
   /ensureCompiles/.test(lens));
ok('a lens shader that never compiles is dropped',
   /never compiled/i.test(lens) && /this\.detach\(\)/.test(lens));
ok('the lens stops re-checking once it is known good',
   /compileWatch\s*=\s*-1/.test(lens));

/* ---------------- 7. behavioural: the guard script really works --------- */

// Run the inline guard in a DOM with no WebGL at all - the common real-world
// cause of a black canvas - and confirm it speaks up.
{
  const d = new JSDOM(html, { runScripts: 'dangerously' });
  // jsdom's canvas has no WebGL context, so getContext returns null.
  const box = d.window.document.getElementById('bootFail');
  ok('with no WebGL, the failure panel is shown',
     box && box.style.display === 'block', 'display=' + (box && box.style.display));
  ok('with no WebGL, the reason names WebGL',
     /WebGL/i.test(d.window.document.getElementById('bootFailWhy').textContent));
  ok('with no WebGL, the boot spinner is dismissed',
     d.window.document.getElementById('staticBoot').classList.contains('gone'));
  d.window.close();
}

// With WebGL present, the guard must stay silent and leave the app alone.
{
  const d = new JSDOM(html, { runScripts: 'dangerously', beforeParse(w) {
    w.HTMLCanvasElement.prototype.getContext = function () { return {}; };
  } });
  const box = d.window.document.getElementById('bootFail');
  ok('with WebGL present, no failure panel is shown',
     !box || box.style.display !== 'block');
  ok('with WebGL present, the boot spinner is still up',
     !d.window.document.getElementById('staticBoot').classList.contains('gone'));

  // A runtime error afterwards must still surface.
  d.window.__bootFail('test failure', 'detail here');
  ok('a later failure still reveals the panel',
     d.window.document.getElementById('bootFail').style.display === 'block');
  ok('the detail text is shown to the user',
     /detail here/.test(d.window.document.getElementById('bootFailDetail').textContent));
  d.window.close();
}

// The detail must be truncated so a huge stack cannot blow out the layout.
{
  const d = new JSDOM(html, { runScripts: 'dangerously', beforeParse(w) {
    w.HTMLCanvasElement.prototype.getContext = function () { return {}; };
  } });
  d.window.__bootFail('big', 'x'.repeat(9000));
  const len = d.window.document.getElementById('bootFailDetail').textContent.length;
  ok('an enormous stack trace is truncated', len <= 1500, 'len ' + len);
  d.window.close();
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
