/**
 * Locales — one table describing every place in the universe.
 *
 * App.ts used to carry two parallel registries: FACTORY, mapping an id to a
 * World constructor, and WORLD_FOR_REGION, mapping a region kind to one of
 * those ids. Keeping them in step was manual, they disagreed silently when
 * they drifted, and together they made the app read like a menu of separate
 * worlds rather than one continuous universe.
 *
 * There is now a single list. Each entry says what a place is, what kinds of
 * region resolve to it, and how to build it. A locale is not a "level" you
 * pick - it is what you find when you fly somewhere, which is why the only
 * public lookups are "what is at this kind of place" and "build it".
 */

import type { World } from '../World';
import { PlanetaryWorld } from './PlanetaryWorld';
import { OceanWorld } from './OceanWorld';
import { BlackHoleWorld } from './BlackHoleWorld';
import { SandboxWorld } from './SandboxWorld';
import { TerraformWorld } from './TerraformWorld';
import { DimensionWorld } from './DimensionWorld';
import { GarageWorld } from './GarageWorld';
import { ShipWorld } from './ShipWorld';

export interface Locale {
  /** Stable id, used by saves and by the search navigator. */
  id: string;
  /** Human name. */
  name: string;
  /** Region kinds that resolve to this locale when you arrive at one. */
  kinds: string[];
  /** Builds the world. */
  make: () => World;
  /**
   * True for places that are part of the opening sequence rather than the
   * universe proper, so they never appear as travel destinations.
   */
  sequence?: boolean;
}

/**
 * Every place, in one list. `kinds` is what ties a locale to the universe:
 * fly to an ocean region and you arrive in the ocean locale, because that is
 * what an ocean region *is*.
 */
export const LOCALES: Locale[] = [
  {
    id: 'planetary',
    name: 'Star System',
    // The default. Anything that is a place in open space resolves here.
    kinds: ['star-system', 'planet', 'nebula', 'galaxy', 'deep-space'],
    make: () => new PlanetaryWorld()
  },
  {
    id: 'ocean',
    name: 'Ocean World',
    kinds: ['ocean'],
    make: () => new OceanWorld()
  },
  {
    id: 'terraform',
    name: 'Terraform',
    kinds: ['terrain'],
    make: () => new TerraformWorld()
  },
  {
    id: 'blackhole',
    name: 'Black Hole',
    kinds: ['blackhole'],
    make: () => new BlackHoleWorld()
  },
  {
    id: 'dimension',
    name: 'Dimension',
    kinds: ['dimension'],
    make: () => new DimensionWorld()
  },
  {
    id: 'sandbox',
    name: 'Sandbox',
    kinds: ['sandbox'],
    make: () => new SandboxWorld()
  },
  {
    id: 'garage',
    name: 'Garage',
    kinds: [],
    make: () => new GarageWorld(),
    sequence: true
  },
  {
    id: 'ship',
    name: 'Ship',
    kinds: [],
    make: () => new ShipWorld(),
    sequence: true
  }
];

/** The locale every unknown place falls back to. */
export const DEFAULT_LOCALE = 'planetary';

const BY_ID = new Map<string, Locale>(LOCALES.map((l) => [l.id, l]));

const BY_KIND = new Map<string, Locale>();
for (const l of LOCALES) for (const k of l.kinds) BY_KIND.set(k, l);

/** Looks a locale up by id, falling back to the default rather than failing. */
export function localeById(id: string): Locale {
  return BY_ID.get(id) ?? BY_ID.get(DEFAULT_LOCALE)!;
}

/**
 * What you find when you arrive at a region of this kind. Unknown kinds are
 * open space, which is the honest answer for a universe that generates
 * places procedurally.
 */
export function localeForKind(kind: string): Locale {
  return BY_KIND.get(kind) ?? BY_ID.get(DEFAULT_LOCALE)!;
}

/** Builds the world for a locale id. Never throws for an unknown id. */
export function buildLocale(id: string): World {
  return localeById(id).make();
}

/** Places you can actually travel to, excluding the opening sequence. */
export function travelLocales(): Locale[] {
  return LOCALES.filter((l) => !l.sequence);
}
