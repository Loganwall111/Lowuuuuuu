/**
 * ObjectCatalog — the throwable object library.
 *
 * Every entry is a real physics body: mass, radius, material and restitution
 * all feed the same n-body/collision framework, so a rubber duck and a moon
 * are handled by identical code. Nothing here is a special case.
 */

export enum Material {
  Rubber = 'rubber',
  Metal = 'metal',
  Ice = 'ice',
  Stone = 'stone',
  Wood = 'wood',
  Glass = 'glass',
  Water = 'water',
  Gel = 'gel',
  Explosive = 'explosive',
  Alien = 'alien',
  Organic = 'organic',
  Plasma = 'plasma'
}

export interface MaterialProps {
  density: number;      // relative
  restitution: number;  // bounciness 0..1
  heat: number;         // how much impact energy becomes heat
  fracture: number;     // how readily it shatters
  color: [number, number, number];
}

export const MATERIALS: Record<Material, MaterialProps> = {
  [Material.Rubber]:    { density: 0.9,  restitution: 0.92, heat: 0.1, fracture: 0.05, color: [0.9, 0.2, 0.2] },
  [Material.Metal]:     { density: 7.8,  restitution: 0.35, heat: 0.6, fracture: 0.2,  color: [0.7, 0.72, 0.78] },
  [Material.Ice]:       { density: 0.92, restitution: 0.25, heat: 0.9, fracture: 0.85, color: [0.75, 0.88, 0.98] },
  [Material.Stone]:     { density: 2.7,  restitution: 0.15, heat: 0.4, fracture: 0.6,  color: [0.45, 0.43, 0.40] },
  [Material.Wood]:      { density: 0.6,  restitution: 0.3,  heat: 0.5, fracture: 0.5,  color: [0.52, 0.36, 0.20] },
  [Material.Glass]:     { density: 2.5,  restitution: 0.2,  heat: 0.5, fracture: 0.95, color: [0.8, 0.92, 0.95] },
  [Material.Water]:     { density: 1.0,  restitution: 0.05, heat: 0.2, fracture: 1.0,  color: [0.2, 0.5, 0.85] },
  [Material.Gel]:       { density: 1.1,  restitution: 0.75, heat: 0.1, fracture: 0.3,  color: [0.4, 0.9, 0.6] },
  [Material.Explosive]: { density: 1.6,  restitution: 0.1,  heat: 1.0, fracture: 1.0,  color: [1.0, 0.45, 0.1] },
  [Material.Alien]:     { density: 3.3,  restitution: 0.6,  heat: 0.7, fracture: 0.4,  color: [0.5, 1.0, 0.55] },
  [Material.Organic]:   { density: 1.05, restitution: 0.4,  heat: 0.3, fracture: 0.7,  color: [0.85, 0.7, 0.45] },
  [Material.Plasma]:    { density: 0.2,  restitution: 0.0,  heat: 1.0, fracture: 1.0,  color: [1.0, 0.6, 0.2] }
};

/** Primitive used to build the mesh. Kept deliberately small and composable. */
export type Shape =
  | 'sphere' | 'box' | 'capsule' | 'cylinder' | 'cone' | 'torus'
  | 'duck' | 'rocket' | 'ufo' | 'star' | 'ring' | 'blob' | 'creature';

export interface ObjectDef {
  id: string;
  name: string;
  glyph: string;
  category: 'Cosmic' | 'Goofy' | 'Food' | 'Furniture' | 'Vehicles' | 'Alien' | 'Creatures' | 'Tools' | 'Sports';
  shape: Shape;
  /** Base radius in world units before the scale multiplier. */
  radius: number;
  /** Base mass; the sandbox multiplies this by scale^3. */
  mass: number;
  material: Material;
  color?: [number, number, number];
  /** Optional flavour text shown in the object tray. */
  note?: string;
}

export const CATALOG: ObjectDef[] = [
  /* ------------------------------ Cosmic ------------------------------ */
  { id: 'asteroid',    name: 'Asteroid',        glyph: '☄',  category: 'Cosmic', shape: 'blob',   radius: 1.2, mass: 3,    material: Material.Stone },
  { id: 'comet',       name: 'Comet',           glyph: '💫', category: 'Cosmic', shape: 'blob',   radius: 1.0, mass: 2,    material: Material.Ice },
  { id: 'moon',        name: 'Moon',            glyph: '🌕', category: 'Cosmic', shape: 'sphere', radius: 3.5, mass: 90,   material: Material.Stone },
  { id: 'planet',      name: 'Planet',          glyph: '🪐', category: 'Cosmic', shape: 'sphere', radius: 6,   mass: 400,  material: Material.Stone },
  { id: 'gasgiant',    name: 'Gas Giant',       glyph: '🟠', category: 'Cosmic', shape: 'sphere', radius: 10,  mass: 900,  material: Material.Gel },
  { id: 'star',        name: 'Star',            glyph: '⭐', category: 'Cosmic', shape: 'star',   radius: 8,   mass: 2000, material: Material.Plasma },
  { id: 'neutron',     name: 'Neutron Star',    glyph: '⚪', category: 'Cosmic', shape: 'sphere', radius: 0.8, mass: 3000, material: Material.Metal, note: 'Absurdly dense' },
  { id: 'blackhole',   name: 'Black Hole',      glyph: '⚫', category: 'Cosmic', shape: 'sphere', radius: 1.5, mass: 6000, material: Material.Plasma, note: 'Eats everything' },
  { id: 'whitedwarf',  name: 'White Dwarf',     glyph: '🔘', category: 'Cosmic', shape: 'sphere', radius: 1.4, mass: 1400, material: Material.Plasma },
  { id: 'meteor',      name: 'Meteor Shower',   glyph: '🌠', category: 'Cosmic', shape: 'blob',   radius: 0.5, mass: 0.6,  material: Material.Stone },
  { id: 'icechunk',    name: 'Ice Chunk',       glyph: '🧊', category: 'Cosmic', shape: 'box',    radius: 1.4, mass: 1.5,  material: Material.Ice },
  { id: 'debris',      name: 'Space Debris',    glyph: '🛰', category: 'Cosmic', shape: 'box',    radius: 0.7, mass: 0.9,  material: Material.Metal },

  /* ------------------------------- Goofy ------------------------------- */
  { id: 'duck',        name: 'Giant Rubber Duck', glyph: '🦆', category: 'Goofy', shape: 'duck',   radius: 2.2, mass: 4,  material: Material.Rubber, color: [1.0, 0.85, 0.1], note: 'The classic' },
  { id: 'bowling',     name: 'Bowling Ball',      glyph: '🎳', category: 'Sports', shape: 'sphere', radius: 1.6, mass: 30, material: Material.Stone, color: [0.1, 0.05, 0.2] },
  { id: 'beachball',   name: 'Beach Ball',        glyph: '🏖', category: 'Sports', shape: 'sphere', radius: 2.4, mass: 0.4, material: Material.Rubber, color: [1.0, 0.35, 0.4] },
  { id: 'piano',       name: 'Grand Piano',       glyph: '🎹', category: 'Furniture', shape: 'box', radius: 2.6, mass: 22, material: Material.Wood, color: [0.06, 0.06, 0.08] },
  { id: 'toilet',      name: 'Toilet',            glyph: '🚽', category: 'Furniture', shape: 'box', radius: 1.4, mass: 8,  material: Material.Glass, color: [0.96, 0.96, 0.98] },
  { id: 'fridge',      name: 'Refrigerator',      glyph: '🧊', category: 'Furniture', shape: 'box', radius: 2.0, mass: 14, material: Material.Metal, color: [0.85, 0.86, 0.88] },
  { id: 'sofa',        name: 'Sofa',              glyph: '🛋', category: 'Furniture', shape: 'box', radius: 2.4, mass: 10, material: Material.Wood, color: [0.35, 0.45, 0.6] },
  { id: 'cone',        name: 'Traffic Cone',      glyph: '🚧', category: 'Goofy', shape: 'cone',   radius: 1.1, mass: 1,  material: Material.Rubber, color: [1.0, 0.4, 0.05] },
  { id: 'chicken',     name: 'Rubber Chicken',    glyph: '🐔', category: 'Goofy', shape: 'creature', radius: 1.5, mass: 0.6, material: Material.Rubber, color: [1.0, 0.9, 0.3] },
  { id: 'cart',        name: 'Shopping Cart',     glyph: '🛒', category: 'Goofy', shape: 'box',    radius: 1.6, mass: 3,  material: Material.Metal, color: [0.7, 0.72, 0.75] },
  { id: 'brick',       name: 'Giant Brick',       glyph: '🧱', category: 'Goofy', shape: 'box',    radius: 1.8, mass: 18, material: Material.Stone, color: [0.65, 0.3, 0.22] },
  { id: 'snowball',    name: 'Giant Snowball',    glyph: '⛄', category: 'Goofy', shape: 'sphere', radius: 3.0, mass: 9,  material: Material.Ice, color: [0.95, 0.97, 1.0] },
  { id: 'toycar',      name: 'Toy Car',           glyph: '🚗', category: 'Vehicles', shape: 'box', radius: 1.2, mass: 1.4, material: Material.Rubber, color: [0.9, 0.15, 0.15] },
  { id: 'dice',        name: 'Giant Dice',        glyph: '🎲', category: 'Goofy', shape: 'box',    radius: 1.8, mass: 5,  material: Material.Rubber, color: [0.98, 0.98, 0.98] },
  { id: 'anvil',       name: 'Anvil',             glyph: '🔨', category: 'Tools', shape: 'box',    radius: 1.5, mass: 60, material: Material.Metal, color: [0.25, 0.26, 0.3], note: 'Cartoon physics approved' },
  { id: 'hammer',      name: 'Giant Hammer',      glyph: '🔨', category: 'Tools', shape: 'capsule', radius: 2.2, mass: 40, material: Material.Metal, color: [0.5, 0.4, 0.3] },
  { id: 'spoon',       name: 'Giant Spoon',       glyph: '🥄', category: 'Tools', shape: 'capsule', radius: 2.4, mass: 6, material: Material.Metal, color: [0.8, 0.82, 0.86] },
  { id: 'fork',        name: 'Giant Fork',        glyph: '🍴', category: 'Tools', shape: 'capsule', radius: 2.4, mass: 6, material: Material.Metal, color: [0.8, 0.82, 0.86] },
  { id: 'dumbbell',    name: 'Dumbbell',          glyph: '🏋', category: 'Sports', shape: 'capsule', radius: 1.4, mass: 45, material: Material.Metal, color: [0.2, 0.2, 0.24] },
  { id: 'bouncy',      name: 'Bouncy Ball',       glyph: '🔴', category: 'Sports', shape: 'sphere', radius: 1.0, mass: 0.5, material: Material.Rubber, color: [1.0, 0.2, 0.5], note: 'Restitution 0.92' },
  { id: 'bowlingpin',  name: 'Bowling Pin',       glyph: '🎳', category: 'Sports', shape: 'capsule', radius: 1.2, mass: 2, material: Material.Wood, color: [0.97, 0.97, 0.95] },
  { id: 'basketball',  name: 'Basketball',        glyph: '🏀', category: 'Sports', shape: 'sphere', radius: 1.3, mass: 0.9, material: Material.Rubber, color: [0.9, 0.45, 0.1] },
  { id: 'soccerball',  name: 'Soccer Ball',       glyph: '⚽', category: 'Sports', shape: 'sphere', radius: 1.3, mass: 0.8, material: Material.Rubber, color: [0.95, 0.95, 0.95] },
  { id: 'tire',        name: 'Giant Tire',        glyph: '🛞', category: 'Vehicles', shape: 'torus', radius: 2.0, mass: 12, material: Material.Rubber, color: [0.1, 0.1, 0.11] },
  { id: 'barrel',      name: 'Barrel',            glyph: '🛢', category: 'Goofy', shape: 'cylinder', radius: 1.5, mass: 7, material: Material.Metal, color: [0.3, 0.5, 0.3] },
  { id: 'trashcan',    name: 'Trash Can',         glyph: '🗑', category: 'Furniture', shape: 'cylinder', radius: 1.3, mass: 3, material: Material.Metal, color: [0.4, 0.42, 0.45] },
  { id: 'lamp',        name: 'Street Lamp',       glyph: '💡', category: 'Furniture', shape: 'capsule', radius: 2.2, mass: 5, material: Material.Metal, color: [0.35, 0.36, 0.4] },
  { id: 'mattress',    name: 'Mattress',          glyph: '🛏', category: 'Furniture', shape: 'box',    radius: 2.4, mass: 4, material: Material.Gel, color: [0.9, 0.9, 0.95] },
  { id: 'bathtub',     name: 'Bathtub',           glyph: '🛁', category: 'Furniture', shape: 'box',    radius: 2.0, mass: 9, material: Material.Glass, color: [0.95, 0.96, 0.98] },
  { id: 'washer',      name: 'Washing Machine',   glyph: '🌀', category: 'Furniture', shape: 'box',    radius: 1.7, mass: 13, material: Material.Metal, color: [0.88, 0.89, 0.92] },
  { id: 'tv',          name: 'Television',        glyph: '📺', category: 'Furniture', shape: 'box',    radius: 1.8, mass: 6, material: Material.Glass, color: [0.12, 0.12, 0.14] },
  { id: 'boot',        name: 'Giant Boot',        glyph: '🥾', category: 'Goofy', shape: 'box',    radius: 1.9, mass: 5, material: Material.Wood, color: [0.4, 0.25, 0.15] },
  { id: 'umbrella',    name: 'Umbrella',          glyph: '☂', category: 'Goofy', shape: 'cone',   radius: 1.8, mass: 1, material: Material.Rubber, color: [0.2, 0.3, 0.8] },
  { id: 'balloon',     name: 'Balloon',           glyph: '🎈', category: 'Goofy', shape: 'sphere', radius: 2.0, mass: 0.1, material: Material.Rubber, color: [1.0, 0.2, 0.3] },
  { id: 'magnet',      name: 'Giant Magnet',      glyph: '🧲', category: 'Tools', shape: 'torus',  radius: 1.7, mass: 20, material: Material.Metal, color: [0.85, 0.15, 0.15] },
  { id: 'gear',        name: 'Giant Gear',        glyph: '⚙', category: 'Tools', shape: 'torus',  radius: 1.9, mass: 25, material: Material.Metal, color: [0.5, 0.52, 0.56] },
  { id: 'key',         name: 'Giant Key',         glyph: '🔑', category: 'Tools', shape: 'capsule', radius: 1.6, mass: 4, material: Material.Metal, color: [0.85, 0.7, 0.25] },
  { id: 'bell',        name: 'Giant Bell',        glyph: '🔔', category: 'Tools', shape: 'cone',   radius: 1.8, mass: 30, material: Material.Metal, color: [0.8, 0.65, 0.2] },

  /* -------------------------------- Food -------------------------------- */
  { id: 'banana',      name: 'Banana',            glyph: '🍌', category: 'Food', shape: 'capsule', radius: 1.8, mass: 2, material: Material.Organic, color: [0.98, 0.85, 0.2] },
  { id: 'orange',      name: 'Orange',            glyph: '🍊', category: 'Food', shape: 'sphere',  radius: 1.4, mass: 1.5, material: Material.Organic, color: [1.0, 0.55, 0.05] },
  { id: 'watermelon',  name: 'Watermelon',        glyph: '🍉', category: 'Food', shape: 'sphere',  radius: 2.0, mass: 5, material: Material.Organic, color: [0.15, 0.55, 0.2] },
  { id: 'pizza',       name: 'Pizza',             glyph: '🍕', category: 'Food', shape: 'cylinder', radius: 2.2, mass: 1.2, material: Material.Organic, color: [0.9, 0.65, 0.25] },
  { id: 'donut',       name: 'Donut',             glyph: '🍩', category: 'Food', shape: 'torus',   radius: 1.7, mass: 0.8, material: Material.Organic, color: [0.85, 0.5, 0.65] },
  { id: 'burger',      name: 'Burger',            glyph: '🍔', category: 'Food', shape: 'cylinder', radius: 1.6, mass: 1.4, material: Material.Organic, color: [0.75, 0.45, 0.2] },
  { id: 'egg',         name: 'Giant Egg',         glyph: '🥚', category: 'Food', shape: 'sphere',  radius: 1.7, mass: 2.5, material: Material.Glass, color: [0.98, 0.95, 0.88] },
  { id: 'cheese',      name: 'Cheese Wheel',      glyph: '🧀', category: 'Food', shape: 'cylinder', radius: 1.8, mass: 4, material: Material.Organic, color: [0.98, 0.8, 0.25] },
  { id: 'icecream',    name: 'Ice Cream',         glyph: '🍦', category: 'Food', shape: 'cone',    radius: 1.6, mass: 1, material: Material.Ice, color: [0.98, 0.9, 0.8] },
  { id: 'pineapple',   name: 'Pineapple',         glyph: '🍍', category: 'Food', shape: 'capsule', radius: 1.7, mass: 2.5, material: Material.Organic, color: [0.9, 0.75, 0.2] },
  { id: 'carrot',      name: 'Giant Carrot',      glyph: '🥕', category: 'Food', shape: 'cone',    radius: 1.6, mass: 1.8, material: Material.Organic, color: [0.95, 0.5, 0.1] },
  { id: 'apple',       name: 'Apple',             glyph: '🍎', category: 'Food', shape: 'sphere',  radius: 1.3, mass: 1.2, material: Material.Organic, color: [0.85, 0.12, 0.12] },
  { id: 'taco',        name: 'Taco',              glyph: '🌮', category: 'Food', shape: 'capsule', radius: 1.5, mass: 1, material: Material.Organic, color: [0.9, 0.7, 0.3] },
  { id: 'sushi',       name: 'Sushi',             glyph: '🍣', category: 'Food', shape: 'cylinder', radius: 1.2, mass: 0.9, material: Material.Organic, color: [0.95, 0.9, 0.85] },
  { id: 'cake',        name: 'Birthday Cake',     glyph: '🎂', category: 'Food', shape: 'cylinder', radius: 1.9, mass: 3, material: Material.Organic, color: [0.95, 0.8, 0.85] },
  { id: 'pretzel',     name: 'Pretzel',           glyph: '🥨', category: 'Food', shape: 'torus',   radius: 1.6, mass: 1.1, material: Material.Organic, color: [0.7, 0.45, 0.15] },

  /* ------------------------------ Vehicles ------------------------------ */
  { id: 'rocket',      name: 'Rocket',            glyph: '🚀', category: 'Vehicles', shape: 'rocket', radius: 2.4, mass: 8, material: Material.Metal, color: [0.92, 0.93, 0.96] },
  { id: 'satellite',   name: 'Satellite',         glyph: '📡', category: 'Vehicles', shape: 'box',    radius: 1.5, mass: 3, material: Material.Metal, color: [0.75, 0.72, 0.4] },
  { id: 'spacestation',name: 'Space Station',     glyph: '🛰', category: 'Vehicles', shape: 'ring',   radius: 5.0, mass: 40, material: Material.Metal, color: [0.8, 0.82, 0.85] },
  { id: 'school_bus',  name: 'School Bus',        glyph: '🚌', category: 'Vehicles', shape: 'box',    radius: 2.6, mass: 16, material: Material.Metal, color: [0.98, 0.78, 0.1] },
  { id: 'train',       name: 'Train Car',         glyph: '🚃', category: 'Vehicles', shape: 'box',    radius: 3.0, mass: 30, material: Material.Metal, color: [0.3, 0.4, 0.55] },
  { id: 'boat',        name: 'Boat',              glyph: '⛵', category: 'Vehicles', shape: 'capsule', radius: 2.4, mass: 9, material: Material.Wood, color: [0.85, 0.86, 0.9] },
  { id: 'submarine',   name: 'Submarine',         glyph: '🤿', category: 'Vehicles', shape: 'capsule', radius: 2.8, mass: 26, material: Material.Metal, color: [0.25, 0.35, 0.3] },
  { id: 'plane',       name: 'Airplane',          glyph: '✈', category: 'Vehicles', shape: 'rocket', radius: 2.8, mass: 12, material: Material.Metal, color: [0.9, 0.92, 0.95] },
  { id: 'tractor',     name: 'Tractor',           glyph: '🚜', category: 'Vehicles', shape: 'box',    radius: 1.9, mass: 14, material: Material.Metal, color: [0.2, 0.6, 0.25] },

  /* -------------------------------- Alien -------------------------------- */
  { id: 'ufo',         name: 'UFO',               glyph: '🛸', category: 'Alien', shape: 'ufo',    radius: 2.6, mass: 6,  material: Material.Alien, color: [0.6, 0.95, 0.7], note: 'Occasionally hostile' },
  { id: 'mothership',  name: 'Alien Mothership',  glyph: '👽', category: 'Alien', shape: 'ufo',    radius: 7.0, mass: 120, material: Material.Alien, color: [0.4, 0.9, 0.6] },
  { id: 'monolith',    name: 'Monolith',          glyph: '⬛', category: 'Alien', shape: 'box',    radius: 3.0, mass: 200, material: Material.Alien, color: [0.03, 0.03, 0.05] },
  { id: 'crystal',     name: 'Alien Crystal',     glyph: '💎', category: 'Alien', shape: 'cone',   radius: 2.0, mass: 15, material: Material.Glass, color: [0.5, 0.9, 1.0] },
  { id: 'dyson',       name: 'Dyson Fragment',    glyph: '🔆', category: 'Alien', shape: 'ring',   radius: 8.0, mass: 300, material: Material.Alien, color: [0.9, 0.8, 0.4] },
  { id: 'probe',       name: 'Alien Probe',       glyph: '🔮', category: 'Alien', shape: 'sphere', radius: 1.2, mass: 3, material: Material.Alien, color: [0.7, 0.4, 1.0] },
  { id: 'portal',      name: 'Portal Ring',       glyph: '🌀', category: 'Alien', shape: 'torus',  radius: 3.4, mass: 50, material: Material.Plasma, color: [0.6, 0.3, 1.0] },
  { id: 'obelisk',     name: 'Obelisk',           glyph: '🗿', category: 'Alien', shape: 'cone',   radius: 2.6, mass: 80, material: Material.Stone, color: [0.4, 0.38, 0.35] },

  /* ------------------------------ Creatures ------------------------------ */
  { id: 'blob',        name: 'Space Blob',        glyph: '🟢', category: 'Creatures', shape: 'blob',     radius: 2.0, mass: 3, material: Material.Gel, color: [0.35, 0.9, 0.45] },
  { id: 'worm',        name: 'Giant Space Worm',  glyph: '🪱', category: 'Creatures', shape: 'capsule',  radius: 3.2, mass: 18, material: Material.Organic, color: [0.75, 0.35, 0.45] },
  { id: 'penguin',     name: 'Giant Penguin',     glyph: '🐧', category: 'Creatures', shape: 'creature', radius: 1.8, mass: 4, material: Material.Organic, color: [0.1, 0.1, 0.14] },
  { id: 'fish',        name: 'Giant Fish',        glyph: '🐟', category: 'Creatures', shape: 'capsule',  radius: 2.2, mass: 6, material: Material.Organic, color: [0.3, 0.6, 0.85] },
  { id: 'whale',       name: 'Space Whale',       glyph: '🐋', category: 'Creatures', shape: 'capsule',  radius: 4.5, mass: 40, material: Material.Organic, color: [0.25, 0.4, 0.6] },
  { id: 'octopus',     name: 'Giant Octopus',     glyph: '🐙', category: 'Creatures', shape: 'blob',     radius: 2.6, mass: 9, material: Material.Organic, color: [0.8, 0.3, 0.5] },
  { id: 'cat',         name: 'Giant Cat',         glyph: '🐈', category: 'Creatures', shape: 'creature', radius: 2.0, mass: 5, material: Material.Organic, color: [0.6, 0.5, 0.35] },
  { id: 'snail',       name: 'Cosmic Snail',      glyph: '🐌', category: 'Creatures', shape: 'blob',     radius: 2.2, mass: 7, material: Material.Organic, color: [0.65, 0.6, 0.35] },
  { id: 'dino',        name: 'Dinosaur',          glyph: '🦖', category: 'Creatures', shape: 'creature', radius: 3.0, mass: 25, material: Material.Organic, color: [0.35, 0.55, 0.3] },
  { id: 'turtle',      name: 'World Turtle',      glyph: '🐢', category: 'Creatures', shape: 'creature', radius: 5.0, mass: 150, material: Material.Organic, color: [0.3, 0.5, 0.35], note: 'Carries worlds' },
  { id: 'bee',         name: 'Giant Bee',         glyph: '🐝', category: 'Creatures', shape: 'capsule',  radius: 1.5, mass: 1.2, material: Material.Organic, color: [0.95, 0.8, 0.15] },
  { id: 'crab',        name: 'Giant Crab',        glyph: '🦀', category: 'Creatures', shape: 'creature', radius: 2.2, mass: 8, material: Material.Organic, color: [0.9, 0.3, 0.2] },
  { id: 'cactus',      name: 'Giant Cactus',      glyph: '🌵', category: 'Creatures', shape: 'capsule',  radius: 2.4, mass: 6, material: Material.Organic, color: [0.25, 0.55, 0.3] },
  { id: 'mushroom',    name: 'Giant Mushroom',    glyph: '🍄', category: 'Creatures', shape: 'cone',     radius: 2.2, mass: 4, material: Material.Organic, color: [0.85, 0.25, 0.25] },
  { id: 'tree',        name: 'Giant Tree',        glyph: '🌳', category: 'Creatures', shape: 'capsule',  radius: 3.0, mass: 14, material: Material.Wood, color: [0.3, 0.5, 0.25] }
];

export const CATEGORIES = [...new Set(CATALOG.map((o) => o.category))];

export function findObject(id: string): ObjectDef | undefined {
  return CATALOG.find((o) => o.id === id);
}

export function randomObject(): ObjectDef {
  return CATALOG[Math.floor(Math.random() * CATALOG.length)];
}

/** Scale presets exposed in the UI. */
export const SCALES: { label: string; value: number }[] = [
  { label: '1×', value: 1 },
  { label: '2×', value: 2 },
  { label: '5×', value: 5 },
  { label: '10×', value: 10 },
  { label: '100×', value: 100 },
  { label: 'Planetary', value: 400 }
];
