/**
 * ObjectSearch — the SpaceEngine-style object search.
 *
 * Typing "BLACK HOLE", "EARTH" or "SUN" into the cockpit and hitting enter
 * should do one thing: put you beside that object. This is the pure half of
 * that - keyword parsing and target resolution over the universe's regions -
 * so the matching can be tested without a scene and reused by any panel or
 * HUD. The app owns the actual warp.
 */

export interface Searchable {
  id: string;
  name: string;
  kind: string;
  x: number;
  y: number;
  z: number;
}

export interface SearchTarget {
  /** 'region' warps to a real region; 'point' warps to raw coordinates. */
  kind: 'region' | 'point';
  id: string;
  name: string;
  x: number;
  y: number;
  z: number;
}

/** Normalises a query into lowercase keyword tokens. */
export function tokens(query: string): string[] {
  return String(query ?? '')
    .toLowerCase()
    .split(/[^a-z0-9]+/)
    .filter((t) => t.length > 0);
}

/** One token matches a region's name or kind. */
function tokenMatchesRegion(t: string, r: Searchable): boolean {
  if (!t) return false;
  const name = r.name.toLowerCase();
  const kind = r.kind.toLowerCase();
  if (name.includes(t)) return true;
  if (kind.includes(t)) return true;
  // Common synonyms, so natural language works.
  const alias: Record<string, string[]> = {
    'blackhole': ['black', 'hole', 'singularity'],
    'starsystem': ['star', 'system', 'sun'],
    'nebula': ['cloud'],
    'galaxy': ['galactic'],
    'planet': ['world'],
    'ocean': ['sea', 'water'],
    'terrain': ['mountain', 'rocky']
  };
  const expanded = alias[kind] ?? [];
  return expanded.some((a) => t === a || a.includes(t));
}

/** True when the query names the home system's sun or world. */
export function isHomeQuery(q: string[]): boolean {
  return q.length > 0 && (
    q.includes('sun') || q.includes('home') || q.includes('earth') ||
    q.includes('solar') || q.includes('terra')
  );
}

/**
 * Resolves a query to a target.
 *
 * "black hole" finds the nearest black hole to the player; "earth"/"sun"
 * resolves to the home system at the origin. Any other query fuzzy-matches
 * region names and kinds, falling back to null when nothing matches.
 */
export function resolveSearch(
  query: string,
  regions: Searchable[],
  eye: { x: number; y: number; z: number }
): SearchTarget | null {
  const q = tokens(query);
  if (!q.length) return null;

  if (isHomeQuery(q)) {
    return { kind: 'point', id: 'home', name: 'Home System', x: 0, y: 0, z: 0 };
  }

  // A black hole names a specific body: the nearest one wins.
  if (q.some((t) => t === 'blackhole' || t === 'black' || t === 'hole')) {
    const holes = regions.filter((r) => r.kind === 'blackhole');
    if (holes.length) {
      holes.sort((a, b) =>
        (Math.hypot(a.x - eye.x, a.y - eye.y, a.z - eye.z) -
         Math.hypot(b.x - eye.x, b.y - eye.y, b.z - eye.z)));
      const h = holes[0];
      return { kind: 'region', id: h.id, name: h.name, x: h.x, y: h.y, z: h.z };
    }
    return null;
  }

  // Fuzzy match: a region whose name or kind absorbs every query token.
  let best: SearchTarget | null = null;
  let bestScore = -1;
  for (const r of regions) {
    let score = 0;
    for (const t of q) {
      if (tokenMatchesRegion(t, r)) score++;
    }
    if (score > bestScore) {
      bestScore = score;
      best = { kind: 'region', id: r.id, name: r.name, x: r.x, y: r.y, z: r.z };
    }
  }
  return bestScore > 0 ? best : null;
}
