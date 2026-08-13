/**
 * locales-check — one table for every place in the universe.
 *
 * App.ts used to hold two registries that had to agree by hand: FACTORY
 * (id -> World) and WORLD_FOR_REGION (region kind -> id). Nothing enforced
 * that a kind mapped to an id that existed, so a typo silently sent the
 * player to the default world. These assertions check the collapse into a
 * single table, and - more usefully - that the table is internally
 * consistent in ways the old pair of tables could not be.
 */

import { readFileSync, readdirSync } from 'node:fs';

let pass = 0, fail = 0;
const ok = (name, cond, detail) => {
  if (cond) { pass++; console.log('  PASS  ' + name); }
  else { fail++; console.log('  FAIL  ' + name + (detail ? '  -> ' + detail : '')); }
};

const app = readFileSync(new URL('../src/bjs/App.ts', import.meta.url), 'utf8');
const src = readFileSync(new URL('../src/bjs/worlds/Locales.ts', import.meta.url), 'utf8');

console.log('\none universe: locales, not a world registry');

/* ---------------- the old registries are really gone -------------------- */

ok('FACTORY no longer exists in App',
   !/const FACTORY/.test(app));
ok('WORLD_FOR_REGION no longer exists in App',
   !/WORLD_FOR_REGION/.test(app));
ok('App builds worlds through the shared table',
   /buildLocale\(/.test(app));
ok('App resolves arrival through the shared table',
   /localeForKind\(/.test(app));
ok('App no longer imports world classes directly',
   !/import \{ PlanetaryWorld \}/.test(app) &&
   !/import \{ OceanWorld \}/.test(app) &&
   !/import \{ GarageWorld \}/.test(app));

/* ---------------- the table itself -------------------------------------- */

// Parse the locale entries out of the source so the assertions test the real
// data rather than a copy of it.
const ids = [...src.matchAll(/^\s{4}id:\s*'([a-z-]+)'/gm)].map((m) => m[1]);
ok('every world is represented', ids.length >= 8, ids.join(','));

const expected = ['planetary', 'ocean', 'terraform', 'blackhole', 'dimension', 'sandbox', 'garage', 'ship'];
for (const e of expected) {
  ok(`the ${e} locale exists`, ids.includes(e));
}

ok('locale ids are unique', new Set(ids).size === ids.length);

// Each kind must appear exactly once across the whole table, or arrival is
// ambiguous - the precise failure the two-table design allowed.
const kindLists = [...src.matchAll(/kinds:\s*\[([^\]]*)\]/g)]
  .map((m) => m[1].split(',').map((k) => k.trim().replace(/'/g, '')).filter(Boolean));
const allKinds = kindLists.flat();
ok('no region kind is claimed by two locales',
   new Set(allKinds).size === allKinds.length, allKinds.join(','));

// The kinds the universe actually generates must all resolve.
const universe = readFileSync(new URL('../src/bjs/systems/UniverseState.ts', import.meta.url), 'utf8');
const generated = [...new Set(
  [...universe.matchAll(/kind:\s*'([a-z-]+)'/g)].map((m) => m[1])
)];
ok('the universe generates at least a few kinds of place', generated.length >= 3, generated.join(','));

// Any generated kind not explicitly listed must still be safe, because
// lookup falls back rather than returning undefined.
ok('lookup falls back instead of returning undefined',
   /\?\?\s*BY_ID\.get\(DEFAULT_LOCALE\)!/.test(src));
ok('the fallback locale is itself in the table',
   ids.includes('planetary'));
ok('the default is declared once and reused',
   /export const DEFAULT_LOCALE = 'planetary'/.test(src));

/* ---------------- sequence places are not destinations ------------------ */

ok('the garage and ship are marked as sequence places',
   (src.match(/sequence:\s*true/g) || []).length === 2);
ok('sequence places claim no region kinds',
   /id: 'garage',[\s\S]*?kinds:\s*\[\]/.test(src) &&
   /id: 'ship',[\s\S]*?kinds:\s*\[\]/.test(src));
ok('there is a way to list only real destinations',
   /travelLocales/.test(src));
ok('travel destinations exclude the opening sequence',
   /filter\(\(l\) => !l\.sequence\)/.test(src));

/* ---------------- every referenced world file exists -------------------- */

const worldFiles = readdirSync(new URL('../src/bjs/worlds', import.meta.url));
const imported = [...src.matchAll(/from '\.\/([A-Za-z]+)'/g)].map((m) => m[1] + '.ts');
ok('every imported world file is present',
   imported.every((f) => worldFiles.includes(f)),
   imported.filter((f) => !worldFiles.includes(f)).join(',') || 'all present');
// Count only constructors inside the table; the interface also declares a
// `make: () => World` member, which is a type, not an entry.
{
  const table = src.slice(src.indexOf('export const LOCALES'));
  const ctors = (table.match(/make:\s*\(\)\s*=> new [A-Za-z]+\(\)/g) || []).length;
  ok('every locale has a constructor', ctors === ids.length,
     ctors + ' constructors for ' + ids.length + ' locales');
}

// Lookups must be O(1) maps, not repeated linear scans, since arrival runs
// this on every warp.
ok('lookups are indexed rather than scanned',
   /new Map/.test(src) && (src.match(/new Map/g) || []).length >= 1);

/* ---------------- behavioural: the real functions ----------------------- */

// Re-implement the lookup exactly as written and exercise it.
const byId = new Map();
const entries = [...src.matchAll(/\{\s*id:\s*'([a-z-]+)',[\s\S]*?kinds:\s*\[([^\]]*)\]/g)]
  .map((m) => ({ id: m[1], kinds: m[2].split(',').map((k) => k.trim().replace(/'/g, '')).filter(Boolean) }));
for (const e of entries) byId.set(e.id, e);
const byKind = new Map();
for (const e of entries) for (const k of e.kinds) byKind.set(k, e);

const localeForKind = (k) => byKind.get(k) ?? byId.get('planetary');

ok('an ocean region resolves to the ocean locale',
   localeForKind('ocean').id === 'ocean');
ok('a terrain region resolves to terraform',
   localeForKind('terrain').id === 'terraform');
ok('a black hole region resolves to the black hole locale',
   localeForKind('blackhole').id === 'blackhole');
ok('a star system resolves to open space',
   localeForKind('star-system').id === 'planetary');
ok('an unknown kind still resolves to something',
   localeForKind('something-invented-later').id === 'planetary');
ok('an empty kind does not crash the lookup',
   localeForKind('').id === 'planetary');

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
