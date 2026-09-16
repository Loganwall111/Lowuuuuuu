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

## 2. What is LIVE now (phase 1, shipped in this build)

| Piece | Status | Where |
|---|---|---|
| **35 new blocks** — decayed stone / cobble / bricks / dirt / sand / planks / logs / leaves, city bricks + tiles, rusted plate, rebar grate, cracked road, hollow wall, storm rib, tendon, withered flesh, reality glass, glitch lamp, memory crystal, void core, black hole core, rift anchor, slabs, stairs, walls, fences | **LIVE** | `McsmContent` + `jar-overrides/assets/mcsm/` |
| **Doors and trap doors** (withered + rusted, real `DoorBlock`/`TrapDoorBlock`) | **LIVE** | same |
| **19 items**: 6 weapons/tools (Reality Ripper, Withered Blade, Storm Spear, Creator's Judgement, Echo Totem, Tentacle Hook), the Rift Key, and the material tiers (Rift Shard, Void Thread, Decayed Steel, Storm Heart Shard, Glitch Echo, Memory Fragment, Glyph Cell, Creator Fragment, Abyss Orb, City Keycard, Hallucination Dust, Decayed Bone) | **LIVE** | same |
| **Own creative tab** ("Devouring Storms: Decayed Reality") + **16 crafting recipes** | **LIVE** | `McsmContent.registerTab()`, `data/mcsm/recipe/` |
| **A REAL dimension** `mcsm:decayed_reality` — own dimension type (its own sky, fog, ambient light, no skylight, nether-like ceiling rules), own flat terrain **built out of the new blocks**, not a teleport to another corner of the overworld | **LIVE** | `data/mcsm/dimension*`, `McsmReality` |
| **Rift entry**: hold the **Rift Key** + sneak → the rift opens (and closes) — server-side, singleplayer and multiplayer, no key binding to collide with | **LIVE** | `McsmReality.tickServer` + `McsmRiftMixin` |
| **Console chapter IX** — enter the rift from the panel, hand out the starter kit, and switch every new layer (cities, quests, creatures, glitches, black hole, tornadoes) | **LIVE** | `McsmExtrasScreen` |

Phase 1 deliberately uses **existing textures** (the base mod's own storm
textures plus vanilla) so no path can be wrong and nothing renders as an error
block. Phase 2 replaces the placeholder set with generated storm textures.

---

## 3. The phases ahead

### Phase 2 — the abandoned cities (generation, for real)
- Structure generation inside `mcsm:decayed_reality`: ruined city blocks, tower
  shells, collapsed highways, a cratered plaza, a hospital, a radio station.
- Driven by our own site table + the base mod's structure builder
  (`McsmWorldgen.enqueue/layout/tick`), so the worldgen budget that already
  works for the story towns is reused instead of reinvented.
- Loot tables per building type (keys, keycards, glyph cells, weapons).
- Generated **textures** for the new blocks (this is where the placeholder
  vanilla textures go away).

### Phase 3 — creatures and the bosses
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

## 4. Honest notes

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
- Each phase is gated: the checkpoint suite (now 134/134) fails the build if a
  registered block has no blockstate, an item has no model, the dimension
  datapack loses its id match, or the new switches stop being persisted.
