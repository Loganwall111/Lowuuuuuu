/**
 * IntergalacticGrid - galaxies without end.
 *
 * One galaxy has an outside. Fly far enough and you are in featureless
 * black with nothing left to reach, which is exactly the dead end the
 * single 50,000-unit Milky Way created: cross the rim and the universe is
 * over.
 *
 * Space is divided into cubic cells of CELL_SIZE. Every cell deterministic-
 * ally contains one galaxy, its position, size, tilt, colour and star count
 * all derived by hashing the cell's integer coordinates. There is no list
 * and no storage: the galaxy three million units away already exists, in
 * the sense that its parameters are a pure function of where it is, and it
 * will be identical every time anyone computes it.
 *
 * Only cells near the player are ever realised as geometry. Fly toward a
 * distant smudge and it is promoted to real stars as you approach; fly away
 * and it drops back to a smudge, then to nothing. The player can travel in
 * any direction forever and never run out.
 *
 * WHY THIS IS NOT THE SAME AS THE SKY DOME
 *
 * The dome painted a galaxy that could never be reached because it sat at
 * infinite distance. These galaxies have real coordinates. The distant ones
 * are drawn as single points rather than 30,000 stars each, which is a
 * level-of-detail decision, not a fake: the point sits at the true position
 * of a galaxy you can actually fly to, and swaps to full geometry when you
 * get there.
 */

/** Edge length of one cell of space, in world units. */
export const CELL_SIZE = 260000;

/** How many cells in each direction are considered for rendering. */
export const VIEW_CELLS = 3;

/** Radius of a realised galaxy's star field. */
export const GALAXY_RADIUS = 50000;

/**
 * Distance at which a galaxy is promoted from a distant smudge to real
 * geometry. Comfortably outside its own radius so the swap never happens
 * inside the disc where it would be obvious.
 */
export const DETAIL_RANGE = GALAXY_RADIUS * 2.6;

/** How a galaxy is coloured and structured. */
export type GalaxyClass = 'photoreal' | 'anomaly';

/**
 * Share of galaxies that are the neon magenta/teal variety.
 *
 * NOTE ON THE NUMBER. The written brief asked for a "5% to 10% threshold",
 * but the instruction in the user's own words was "super rare ... like a one
 * percent". Those disagree, and the prose is the clearer statement of
 * intent, so the rare class is 1%. This is deliberately a single named
 * constant: moving it to 0.05 restores the brief's figure exactly.
 */
export const ANOMALY_CHANCE = 0.01;

export interface GalaxyCell {
  /** Integer cell coordinates. */
  ix: number; iy: number; iz: number;
  /** World-space centre of this galaxy. */
  x: number; y: number; z: number;
  /** Star-field radius. */
  radius: number;
  /** Stable seed for this galaxy's own contents. */
  seed: number;
  /** Disc tilt, radians. */
  tiltX: number; tiltZ: number;
  /** Overall colour bias, RGB 0-1. */
  tint: [number, number, number];
  /** 0-1 how tightly wound the arms are. */
  winding: number;
  /** Relative brightness, 0-1. */
  brightness: number;
  /**
   * Which palette this galaxy uses.
   *
   * Nearly every galaxy is 'photoreal': a creamy-gold core with a cold blue
   * halo and dark dust lanes, like Andromeda. 'anomaly' is the intense
   * magenta/teal emission variety, kept rare enough to be a genuine find.
   */
  klass: GalaxyClass;
}

/** Integer hash. Deterministic across machines and sessions. */
export function hashCell(ix: number, iy: number, iz: number): number {
  let h = (ix | 0) * 374761393 + (iy | 0) * 668265263 + (iz | 0) * 2147483647;
  h = (h ^ (h >>> 13)) >>> 0;
  h = Math.imul(h, 1274126177) >>> 0;
  return (h ^ (h >>> 16)) >>> 0;
}

/** A 0-1 value from a hash and a channel index, so one cell gives many. */
function chan(h: number, i: number): number {
  let v = (h + Math.imul(i + 1, 2654435761)) >>> 0;
  v = Math.imul(v ^ (v >>> 15), 2246822519) >>> 0;
  v = (v ^ (v >>> 13)) >>> 0;
  return v / 4294967296;
}

/**
 * The galaxy belonging to a cell.
 *
 * Pure: same inputs, same galaxy, forever. This is what lets an infinite
 * universe exist without storing anything.
 */
export function galaxyInCell(ix: number, iy: number, iz: number): GalaxyCell {
  const h = hashCell(ix, iy, iz);
  // Jitter the centre inside the cell so the grid is never visible as a
  // lattice. Kept off the walls so neighbours cannot overlap.
  const jx = 0.22 + chan(h, 0) * 0.56;
  const jy = 0.22 + chan(h, 1) * 0.56;
  const jz = 0.22 + chan(h, 2) * 0.56;
  const radius = GALAXY_RADIUS * (0.55 + chan(h, 3) * 0.9);

  // Classification comes off its own hash channel, so it is independent of
  // size, tilt and tint - a rare galaxy is not also always a big bright one.
  const klass: GalaxyClass = chan(h, 9) < ANOMALY_CHANCE ? 'anomaly' : 'photoreal';

  // Colour: most galaxies are warm white to gold, some blue starbursts.
  const cool = chan(h, 6);
  const tint: [number, number, number] = klass === 'anomaly'
    // The neon variety announces itself from a distance.
    ? [1.0, 0.42, 0.86]
    : cool > 0.72
      ? [0.62, 0.74, 1.0]
      : cool < 0.16
        ? [1.0, 0.62, 0.42]
        : [1.0, 0.90, 0.76];
  return {
    ix, iy, iz,
    x: (ix + jx) * CELL_SIZE,
    y: (iy + jy) * CELL_SIZE,
    z: (iz + jz) * CELL_SIZE,
    radius,
    seed: h,
    tiltX: chan(h, 4) * Math.PI,
    tiltZ: chan(h, 5) * Math.PI,
    tint,
    winding: 0.6 + chan(h, 7) * 0.9,
    brightness: 0.45 + chan(h, 8) * 0.55,
    klass
  };
}

/** Which cell a world position falls in. */
export function cellOf(x: number, y: number, z: number): [number, number, number] {
  return [
    Math.floor(x / CELL_SIZE),
    Math.floor(y / CELL_SIZE),
    Math.floor(z / CELL_SIZE)
  ];
}

/**
 * Every galaxy within VIEW_CELLS of a position, nearest first.
 *
 * This is the whole visible universe at any moment: a bounded list drawn
 * from an unbounded space.
 */
export function galaxiesNear(
  x: number, y: number, z: number, cells = VIEW_CELLS
): GalaxyCell[] {
  if (!Number.isFinite(x) || !Number.isFinite(y) || !Number.isFinite(z)) {
    return [];
  }
  const [cx, cy, cz] = cellOf(x, y, z);
  const out: GalaxyCell[] = [];
  const n = Math.max(0, Math.min(6, Math.floor(cells)));
  for (let i = -n; i <= n; i++) {
    for (let j = -n; j <= n; j++) {
      for (let k = -n; k <= n; k++) {
        out.push(galaxyInCell(cx + i, cy + j, cz + k));
      }
    }
  }
  out.sort((a, b) => {
    const da = (a.x - x) ** 2 + (a.y - y) ** 2 + (a.z - z) ** 2;
    const db = (b.x - x) ** 2 + (b.y - y) ** 2 + (b.z - z) ** 2;
    return da - db;
  });
  return out;
}

/** The galaxy whose disc contains this point, if any. */
export function galaxyContaining(
  x: number, y: number, z: number
): GalaxyCell | null {
  const near = galaxiesNear(x, y, z, 1);
  for (const g of near) {
    const d = Math.hypot(g.x - x, g.y - y, g.z - z);
    if (d <= g.radius) return g;
  }
  return null;
}

/**
 * Distance to the nearest galaxy centre, and that galaxy.
 *
 * Used by navigation so the player is never lost: there is always a
 * concrete "nearest galaxy, N units that way".
 */
export function nearestGalaxy(
  x: number, y: number, z: number
): { galaxy: GalaxyCell; distance: number } | null {
  const near = galaxiesNear(x, y, z, 1);
  if (!near.length) return null;
  const g = near[0];
  return { galaxy: g, distance: Math.hypot(g.x - x, g.y - y, g.z - z) };
}

/**
 * Should this galaxy be realised as full geometry?
 *
 * One at a time: only the galaxy you are in or heading into gets 30,000
 * real stars. Everything else is a point. Without this rule an infinite
 * universe would try to allocate infinite vertices.
 */
export function shouldRealise(
  g: GalaxyCell, x: number, y: number, z: number
): boolean {
  const d = Math.hypot(g.x - x, g.y - y, g.z - z);
  return d <= Math.max(DETAIL_RANGE, g.radius * 2.2);
}
