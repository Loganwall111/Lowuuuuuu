# Devouring Storms — The Decayed Reality

> Build #416 mandate D.8. The user's brief: *"there's only like five or 15 blocks
> in the game ... have new trap doors, doors, items, even some weapons ... the
> abandoned cities ... the black hole, the gigantic tornadoes ... a real
> dimension not the ones they're just teleported somewhere else in the world
> ... the Creator that destroyer of reality ... quests and stuff ... make it
> feel like an absolute 100% triple A game."*

This document is the design + build plan for that expansion, and it is honest
about what is live, what is in progress and what is still ahead. Phases ship one
at a time, each one green in CI before the next starts.

---

## 1. The premise (lore)

The Wither Storm was never a monster. It was **a wound**.

When the Command Block was broken open in the far past, it did not create the
storm — it *tore*. What came through was a mouth, and on the other side of the
mouth is the place the world was before it was finished: **the Decayed
Reality**. Same world, same cities, same people — chewed through, left standing,
left *remembering*. The buildings are still there. Nobody is.

- **The Creator** — the thing that was at the centre of the tear before the
  Command Block moved. It is not the storm. It is the *hand* that fed the storm,
  and now that the world is rotten enough it starts reaching back through:
  gigantic octopus arms of torn space, kilometres long, coming down out of the
  sky through rips in the fabric of reality.
- **The Abandoned Cities** — the story-mode towns, ruined. They are real
  places: they generate in the decayed reality (phase 2), lootable, guarded,
  sometimes inhabited by things that remember being people.
- **Echoes** — the storm does not destroy memories, it *stores* them. Memory
  Fragments, Echo Totems and the Glyph Cells are how a player reads them, which
  is how the quest layer tells its story without a cutscene the engine cannot
  render.
- **The Black Hole** — the end state of a place that has been decayed too long:
  a collapsing singularity that eats the arena around it.
- **Mega-Tornadoes** — the storm's weather, loose in a dimension with nothing
  left to hold it down.

The player's arc: *survive → cross over → read the city → arm yourself from the
tear → face the Creator's arms → seal or eat the hole.*

---

## 2. What is LIVE now (phases 1 + 2, shipped in this build)

| Piece | Status | Where |
|---|---|---|
| **35 new blocks** — decayed stone / cobble / bricks / dirt / sand / planks / logs / leaves, city bricks + tiles, rusted plate, rebar grate, cracked road, hollow wall, storm rib, tendon, withered flesh, reality glass, glitch lamp, memory crystal, void core, black hole core, rift anchor, slabs, stairs, walls, fences | **LIVE** | `McsmContent` + `jar-overrides/assets/mcsm/` |
| **Doors and trap doors** (withered + rusted, real `DoorBlock`/`TrapDoorBlock`) | **LIVE** | same |
| **19 items**: 6 weapons/tools (Reality Ripper, Withered Blade, Storm Spear, Creator's Judgement, Echo Totem, Tentacle Hook), the Rift Key, and the material tiers (Rift Shard, Void Thread, Decayed Steel, Storm Heart Shard, Glitch Echo, Memory Fragment, Glyph Cell, Creator Fragment, Abyss Orb, City Keycard, Hallucination Dust, Decayed Bone) | **LIVE** | same |
| **Own creative tab** ("Devouring Storms: Decayed Reality") on the **vanilla** `CreativeModeTab.builder` -- no Fabric module, every block and item reachable in the creative inventory -- + **16 crafting recipes** | **LIVE** | `McsmContent.registerTab()`, `data/mcsm/recipe/` |
| **A REAL dimension** `mcsm:decayed_reality` — own dimension type (its own sky, fog, ambient light, no skylight, nether-like ceiling rules), own flat terrain **built out of the new blocks**, not a teleport to another corner of the overworld | **LIVE** | `data/mcsm/dimension*`, `McsmReality` |
| **Rift entry**: hold the **Rift Key** + sneak → the rift opens (and closes) — server-side, singleplayer and multiplayer, no key binding to collide with | **LIVE** | `McsmReality.tickServer` + `McsmRiftMixin` |
| **Console chapter IX** — enter the rift from the panel, hand out the starter kit, and switch every new layer (cities, quests, creatures, glitches, black hole, tornadoes) | **LIVE** | `McsmExtrasScreen` |
| **Abandoned cities that actually generate** — a district every 256 blocks (62% of regions), each one a plaza with a rift monument, nine plots in a 3x3 grid of streets, six building archetypes (tower shell, warehouse, half-collapsed house, hospital, radio mast, crater), street furniture and a ruined highway. Deterministic: the same region always holds the same district | **LIVE** | `McsmCities` |
| **Built without stalling anything** — a district is planned once and written 1200 blocks per server tick, only when a player is within 352 blocks. A district rises over about a second of play and an unreachable corner of the dimension costs nothing | **LIVE** | `McsmCities.OPS_PER_TICK` |
| **A district is a place, not an event** — its rift monument is part of the world, so a reloaded server does not rebuild what is standing, and a monument that was mined out is rebuilt rather than lost | **LIVE** | `McsmCities.planCity` |
| **Crate loot** — `city_crate`, `supply_crate` and `vault_crate` scattered through every archetype, dropping the new weapons, materials and glyph cells through vanilla block loot tables | **LIVE** | `data/mcsm/loot_table/blocks/` |
| **Directions to the ruins** — arriving in the rift tells you how far the nearest district is and which way, from pure arithmetic on the same hash the generator uses | **LIVE** | `McsmCities.guidance` |
| **The datapack is validated against the game itself** — vanilla's own recipe / loot table / dimension files are read out of the client jar the runner downloads, and the build fails if our JSON uses a different key shape or names an id that is not registered | **LIVE** | `ci/check_datapack_schema.py` |

Phase 1 deliberately uses **existing textures** (the base mod's own storm
textures plus vanilla) so no path can be wrong and nothing renders as an error
block. Phase 2 replaces the placeholder set with generated storm textures.

---

## 3. The phases ahead

### Phase 2 — the abandoned cities (generation, for real) — **DONE**
- Districts generate inside `mcsm:decayed_reality` as described above, with six
  building archetypes, street furniture, a highway, and crate loot.
- Still to do inside this phase: generated textures for the city blocks (they
  currently reuse the storm/vanilla placeholder set), interiors that read as
  *rooms* (beds, desks, signage), and a city map entry in the HUD terminal.

### Phase 3 — creatures and the bosses (NEXT)
- Reality creatures: Withered survivors, Echo husks, Glitch crawlers,
  Rift hounds, the City Warden; spawned and staged by the dimension, not the
  overworld.
- The boss ladder: **the City Warden** → **the Torn Colossus** → **the Creator's
  Hand** (the octopus arms: kilometres-long segmented limbs that come down
  through sky rips, break the terrain and must be severed at the joint).
- Creature behaviour is layered on vanilla entity types with our own AI/staging
  mixins (`require = 0`), which is the only approach that is safe against a
  frozen base jar.

### Phase 4 — the reality itself
- Reality-glitch pass: torn frames, duplicated silhouettes, false storms,
  whispering chat, inventory flicker — intensity driven by the console slider.
- Mega-tornadoes that sweep the decayed reality.
- The Black Hole event: a singularity that spawns, drags blocks and mobs in,
  grows, and collapses.

### Phase 5 — the AAA layer (quests, lore, polish)
- A chapter log: objectives, waypoints, read-out in the console and the HUD
  terminal.
- Dialogue in the Story Mode style the mod already ships ("[Take Ellegaard's
  armor]" style choice buttons), now driven by our own quest state.
- The camera/audio polish pass: hit stops, shake, stingers, adaptive music
  hooks, and the cinematic framing for every boss beat.

---

## 4. How the build is checked (this is what makes the phases affordable)

Everything here is written on a machine with **no JDK and no route to Mojang**, so
the GitHub runner is the compiler. Two things were added in phase 1 to make that
loop usable instead of blind:

* **the evidence channel** -- the build now pushes its own run log, javac log,
  class list and the vanilla API dump to `ci-out/run-<N>/` on the session branch.
  The first two D.8 runs went red and said nothing at all (runner logs live on a
  blob host this machine cannot reach); the third one published
  `javac-full.log`, which is how the protected-constructor and missing-Fabric-
  module errors were found and fixed in one pass instead of guessed at.
* **the vanilla API oracle** -- every run dumps `javap` for the classes the next
  phase needs (blocks, items, entities, goals, attributes) plus a probe of which
  Fabric modules are on the compile classpath, into
  `ci-out/run-<N>/vanilla-api.txt`. That is where `BlockSetType.IRON`, the
  protected `DoorBlock`/`TrapDoorBlock`/`StairBlock` constructors and the public
  `CreativeModeTab.builder(Row, int)` were settled -- before writing the code,
  not after a red run.
* **135 checkpoints** now gate the build, including nine for this content pack:
  every registered block must have a blockstate, every item a model and a
  display name, the dimension datapack ids must match the Java key, the new
  gameplay switches must persist, and the tab must not name the Fabric type that
  cannot compile.

## 5. Honest notes

- The "decayed reality", the ghost whale and the glitch hallucinations were
  **never** in this mod before this build (see `FEATURE_INVENTORY.md` §5b) —
  this is new work, not a restoration, and the whale is not part of it: it was
  never designed anywhere in the repo, so it would have to be invented from
  scratch if wanted.
- Everything registered here only touches the **built-in registries at mod
  init** and the **base mod's own public API** (or reflection), because
  `mcsm-extras` compiles against the frozen released jar. That constraint is
  why phase 3 reuses vanilla entity types instead of shipping new model
  geometry.
- Each phase is gated: the checkpoint suite (now **135/135**) fails the build if a
  registered block has no blockstate, an item has no model, the dimension
  datapack loses its id match, or the new switches stop being persisted.
- Phase 1 was written blind and repaid in three red runs; the last two are the
  contract for the rest: **dump the API first, then write the code.**
