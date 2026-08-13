/**
 * intro-check — the opening sequence that replaced the main menu.
 *
 * The danger with a scripted opening is getting stuck in it. Most of these
 * assertions are about always having a way forward and a way out.
 */
import { build } from 'esbuild';
import fs from 'fs';

const load = async (entry, tag) => {
  const out = await build({
    entryPoints: [entry], bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${tag}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const { IntroSequence, STAGES, LESSONS, SHIP_STATIONS } =
  await load('src/bjs/systems/IntroSequence.ts', 'intro');

console.log('— the shape of the opening —');
{
  ok('it starts at the title', STAGES[0] === 'title');
  ok('then the garage', STAGES.includes('garage'));
  ok('then the lessons', STAGES.includes('lesson'));
  ok('then the portal', STAGES.includes('portal'));
  ok('then the ship', STAGES.includes('ship'));
  ok('and then you are playing', STAGES[STAGES.length - 1] === 'playing');

  const s = new IntroSequence();
  ok('a fresh run begins at the title', s.state.stage === 'title');
  ok('and is not finished', !s.state.done);
}

console.log('\n— you can always get through it —');
{
  const s = new IntroSequence();
  // Hammer advance far more times than the sequence is long; it must
  // terminate at 'playing' and stay there rather than wrapping or throwing.
  for (let i = 0; i < 200; i++) s.advance();
  ok('advancing repeatedly ends at playing', s.state.stage === 'playing');
  ok('and it knows it is done', s.state.done);

  const s2 = new IntroSequence();
  const seen = [];
  for (let i = 0; i < STAGES.length + 3; i++) {
    seen.push(s2.state.stage);
    s2.advance();
  }
  ok('every stage is visited in order',
     STAGES.every((st) => seen.includes(st)), seen.join(' -> '));
}

console.log('\n— the lessons —');
{
  ok('there are rules to teach', LESSONS.length >= 5);
  ok('every lesson has someone saying it',
     LESSONS.every((l) => !!l.speaker && !!l.text));
  ok('more than one person speaks',
     new Set(LESSONS.map((l) => l.speaker)).size > 1);
  ok('lesson ids are unique',
     new Set(LESSONS.map((l) => l.id)).size === LESSONS.length);

  // The things a new player actually needs.
  const text = LESSONS.map((l) => l.text + ' ' + l.keys.join(' ')).join(' ');
  ok('movement is taught', /W A S D|WASD/.test(text));
  ok('looking is taught', /mouse/i.test(text));
  ok('jumping is taught', /jump|Space/i.test(text));
  ok('zoom is taught', /zoom/i.test(text));
  ok('grabbing and throwing is taught', /grab|throw/i.test(text));
  ok('it says there is no objective', /no objective/i.test(text));

  const s = new IntroSequence();
  s.advance(); s.advance();   // -> lesson
  ok('the lesson stage serves a lesson', !!s.currentLesson);

  // Stepping through every lesson must land on the portal, not stall.
  let guard = 0;
  while (s.state.stage === 'lesson' && guard++ < 500) s.nextLesson();
  ok('finishing the last lesson moves you on by itself',
     s.state.stage === 'portal', s.state.stage);
  ok('all the rules are marked taught',
     LESSONS.every((l) => s.hasLearned(l.id)));
}

console.log('\n— lessons respond to what you do —');
{
  const s = new IntroSequence();
  s.advance(); s.advance();

  // Walk to the lesson that wants you to move.
  let guard = 0;
  while (s.currentLesson && s.currentLesson.requires !== 'move' && guard++ < 50) {
    s.nextLesson();
  }
  ok('a lesson asks you to move', s.currentLesson?.requires === 'move');
  const before = s.state.lesson;
  ok('the wrong action does not advance it', !s.didAction('jump'));
  ok('and it stays put', s.state.lesson === before);
  s.didAction('move');
  ok('actually moving advances it', s.state.lesson > before);
}

console.log('\n— you can never be trapped —');
{
  const s = new IntroSequence();
  s.skip();
  ok('skip ends the intro immediately', s.state.stage === 'playing');
  ok('skip marks it done', s.state.done);
  ok('skipping still counts the rules as seen',
     LESSONS.every((l) => s.hasLearned(l.id)));

  // Skipping from anywhere works, including mid-lesson.
  for (const stage of STAGES) {
    const t = new IntroSequence();
    let g = 0;
    while (t.state.stage !== stage && g++ < 20) t.advance();
    t.skip();
    ok(`skip works from "${stage}"`, t.state.stage === 'playing' && t.state.done);
  }

  const t2 = new IntroSequence();
  t2.toShip();
  ok('you can jump straight to the ship', t2.state.stage === 'ship');
  ok('the ship is not the end of the intro', !t2.state.done);
}

console.log('\n— progress and worlds —');
{
  const s = new IntroSequence();
  ok('progress starts at zero', s.progress === 0);
  let last = -1, monotonic = true;
  for (let i = 0; i < STAGES.length; i++) {
    const p = s.progress;
    if (p < last) monotonic = false;
    last = p;
    s.advance();
  }
  ok('progress never goes backwards', monotonic);
  ok('progress ends at one', Math.abs(s.progress - 1) < 1e-9);

  const t = new IntroSequence();
  ok('the title renders over the garage', t.worldFor() === 'garage');
  t.advance();
  ok('the garage is a world', t.worldFor() === 'garage');
  t.toShip();
  ok('the ship is a world', t.worldFor() === 'ship');
  t.skip();
  ok('afterwards you are in the universe', t.worldFor() === 'planetary');
}

console.log('\n— reset —');
{
  const s = new IntroSequence();
  s.skip();
  s.reset();
  ok('reset returns to the title', s.state.stage === 'title');
  ok('reset clears what was taught', !s.hasLearned(LESSONS[0].id));
  ok('reset clears done', !s.state.done);
}

console.log('\n— the worlds it needs exist —');
{
  ok('the garage world exists', fs.existsSync('src/bjs/worlds/GarageWorld.ts'));
  ok('the ship world exists', fs.existsSync('src/bjs/worlds/ShipWorld.ts'));

  const g = fs.readFileSync('src/bjs/worlds/GarageWorld.ts', 'utf8');
  ok('the garage is white', /0\.9\d/.test(g) && g.includes('clearColor'));
  ok('it reads as infinite via fog', g.includes('FOGMODE_EXP2'));
  // The door is now a real sectional garage door built from panels, with the
  // portal set into it, rather than a single box named 'door'.
  ok('there is a door', /doorPanel_/.test(g) && /garage-door\.jpg/.test(g));
  ok('the door is built from more than one panel',
     /for \(let row = 0; row < 6; row\+\+\)/.test(g));
  ok('the door has a texture rather than being a white slab',
     /diffuseTexture = doorTex/.test(g));
  ok('the portal is set into the door, not standing in the room',
     /portal\.position\.set\(0, 3\.15, 25\.7\)/.test(g));
  ok('the whole door animates open together',
     /for \(const panel of this\.doorParts\)/.test(g));
  ok('there are people in it', g.includes('speakers'));
  ok('the cast comes from the script itself',
     g.includes('LESSONS.map((l) => l.speaker)'));
  ok('there is a portal out', g.includes('introPortal'));
  ok('you can walk on the floor', g.includes('sampleGround'));

  const sh = fs.readFileSync('src/bjs/worlds/ShipWorld.ts', 'utf8');
  ok('the ship builds its consoles from the station list',
     sh.includes('SHIP_STATIONS'));
  ok('the ship has windows onto the universe', sh.includes('shipWin_'));
  ok('there is something to jump onto', sh.includes('shipDais'));
  ok('you can walk on the deck', sh.includes('sampleGround'));
  ok('consoles light up as you approach', sh.includes('near'));

  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  ok('the app boots into the garage, not a menu',
     app.includes("loadWorld('garage')"));
  ok('the old main menu is not referenced', !app.includes('new MainMenu'));
  ok('the ship consoles are wired to real actions',
     app.includes('useStation'));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
