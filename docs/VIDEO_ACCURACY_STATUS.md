# Wither Storm Mod — Video-Accuracy & Completion Status

This tracks every feature from the *Minecraft: Story Mode* Wither Storm (the video) and
whether it exists in the clean rewrite. Legend:

- **✅ Done + wired** — present in code/assets and hooked into the game.
- **🧩 Exists, needs wiring** — the code/asset is there but not fully connected (often
  blocked on missing mixins/models).
- **⚠️ Partial** — implemented but incomplete vs. the video.
- **🔴 Missing / blocked** — not present, or requires a missing class (model/renderer/
  mixin/item) that stops it compiling.
- **⭐ Phase 2 (this batch)** — newly added in the current phase.

---

## Phase 4+ abilities & combat

| Feature | Status | Notes |
|---|---|---|
| Tractor beam pulling blocks/mobs/player | ✅ | `TractorBeamAbility` + `TractorBeamRenderer` |
| Particles sucked up from ground along beam | ✅ | `BeamMoteSpawner.tick` wired in `StormAtmosphere` |
| Super skulls (flaming wither skulls) | ✅ | `SuperSkullAbility` (phase 3+) |
| Block-cluster tear (chunks ripped from ground) | ✅ | `BlockClusterAbility` |
| Orbiting debris chunks | ✅ | `StormDebris` rendered in `DistantStormRenderer` |
| Debris springing up early | ✅ | `StormDebris.submitEarly` |
| Tornado / vortex | ✅ | `TornadoAbility` (swirling debris, throws entities) |
| **Tentacles slam through blocks** | ⭐✅ | `TentacleSlamAbility` (carves terrain, slaps players) |
| Tentacle grabs / picks up players | 🧩 | `GrabTentacleEntity` + `SnatchGrab` exist; renderer needs the missing model |
| Tentacle through portal | 🧩 | `CrossDimensionalEntity` + `NetherScaleEntity`; tentacle-through-portal wired to `tentacleThroughPortal` config |
| Heads chomping victims | ✅ | `WitherStormHeadEntity.chompVictim` |
| Enderman siege (devourer) | ✅ | Entity siege state machine |

## Phases & growth

| Feature | Status | Notes |
|---|---|---|
| Phase 1-3 (commanded → cocoon) | ✅ | `WitherStormPhase` + models (HunchbackGrowth/HugeAssBack) |
| Phase 4 (giant storm) | ✅ | Two textures: `wither_storm.png` (legacy) + `phase_4_assets.png` (glossy purple) |
| Phase 5 (devourer) | ✅ | `devourer_assets.png`, `CollapseAnim` |
| Formidibomb finale (storm crashes to ground) | ✅ | `formidibombed()` + `COLLAPSE_GAME_TIME` + `CollapseAnim` |
| White pulse → pure white → big explosion | ✅ | `StormDeathCinematic` + `StormDeathPayload` + `playDeathCinematic` |
| Purple glass shards on death | ✅ | Particle burst on death |
| Screen glitch during bomb | ✅ | `StormDeathCinematic` RGB-offset glitch |
| **Instant growth (experimental)** | ⭐✅ | Config `instantGrowth` — grow continuously until stopped |
| **Infinite procedural phases** | ⭐✅ | Config `infinitePhases` — grow forever, world consumed |
| Tiny wither storm first-summon animation | ✅ | `WitherStormSummon` + `beginSpawnFreeze` + spawn-anim state |

## World / atmosphere

| Feature | Status | Notes |
|---|---|---|
| Purple-dark sky near storm | ✅ | `StormSkyDarken` wired |
| Purple pulse/aura from far away (final stages) | ⭐✅ | `StormGlowRenderer.submitDistantPulse` |
| Black silhouette + red/purple rim around body | 🧩 | `StormShadow`/`StormShadowMap` need missing mixins |
| White aura | 🧩 | `StormGlowRenderer` exists; full bloom needs mixins |
| Clouds / atmosphere / volumetric clouds | 🔴 | `CloudColorMixin`/bloom need the missing 14 mixins |
| Colored fog per area/biome | ⭐🧩 | Config `biomeFogs` added; renderer wiring needs the fog mixin |
| Boss music within a radius | ✅ | `StormMusic.tick` wired; `bossMusicRange` config |
| Cave rumble | ✅ | `CaveRumble` + `CaveRumbleClient` |

## The Bowels (interior) & finale

| Feature | Status | Notes |
|---|---|---|
| Go inside the hole (enter through mouth) | ✅ | `BowelsMawEntity` + `BowelsGravity.BOWELS` dimension |
| Gravity flips upside-down / sideways | ✅ | `BowelsGravity.axisAt` + `frame` (real gravity-axis rotation) |
| Heart entity (final kill) | ✅ | `BowelsHeartEntity` + `BowelsBoss` |
| Severed tentacles + TNT finale | ✅ | `SeveredTentacleEntity`, `SeveredRope`, `BowelsPedestalEntity` |
| Interior blockbench models | 🧩 | `geo/Wither_Storm_Interior/*.bbmodel` (source files, not yet baked) |

## Structures, towns & portals

| Feature | Status | Notes |
|---|---|---|
| Storm tours & levels towns/villages | ⭐✅ | `structureHunt` AI + `storm_targets` tag (villages, outposts, mansions...) |
| Nether-portal punching (drag players back) | ⭐🧩 | `CrossDimensionalEntity` + `portalHunt` config; needs `NetherScaleManager` |
| Far Lands chaos maze (experimental) | ⭐🔴 | Config added; terrain-warp generator not yet built |
| Endertown / Beacon Town / named places | 🔴 | Not implemented (next phase) |
| Colored biome areas (green fog, bluish-turquoise) | ⭐🧩 | `biomeFogs` config; biome fog renderer needs fog mixin |

## Config menu

| Feature | Status | Notes |
|---|---|---|
| Deep config (many options) | ✅ | 80+ keys |
| **Menu revamp with all Phase-2 features** | ⭐✅ | New sections: Instant Growth, Structure Hunt, Portals, Tentacle Slams, Boss Music, Atmosphere, Far Lands Maze, Biome Fogs |

---

## What's blocking the 🔴/🧩 items

Almost everything left is blocked by **missing classes** that stop a clean compile:
the **14 mixin accessors** (needed for bloom/clouds/fog/shadow/sun-glow), the
**model classes** + **entity renderers** for the head/tentacle/severed, and the
**item classes** (Rocket Retriever / Formidibomb). Until those are restored (or
rebuilt), the storm itself works but the polish layers (clouds, volumetric fog,
full bloom, precise shadow silhouette) cannot be enabled.

## Honest accuracy vs. the video

- **Core boss fight (phases, beams, skulls, absorption, tornado, tentacle slam,
  debris, death cinematic): ~80% implemented.**
- **Bowels interior + gravity flip + finale: ~70%** (code exists; renderers/models needed).
- **Atmosphere/clouds/volumetric fog/skybox: ~25%** (blocked on mixins).
- **Towns/structures/portals/named places/Far Lands maze: ~20%** (structure-hunt AI done;
  the named biomes/places and maze generation are the next phase).

As the missing classes come back and the next phases land, the storm will visually and
mechanically approach the video. The foundation (phase machine, ability framework,
config, structure-hunt, death cinematic) is in place.

---

## HOW TO RESTORE THE MISSING CLASSES (recently updated)

Every missing class still exists as a `.class` file inside the **original jar**,
which has been restored to the repo root:

    dabywitherstormmod-1.9.60-26.2-beta.zip

**Steps (run on a machine with Java 25 + internet, not the Arena sandbox):**

```bash
# 1. Make sure the jar is present (it is committed):
git checkout 4287fad -- dabywitherstormmod-1.9.60-26.2-beta.zip

# 2. Decompile the jar and copy the missing classes into the source tree:
bash tools/restore-missing-classes.sh

# 3. Build; paste any compile errors and I'll fix them:
gradle build
```

This restores all 14 mixins, the 11 model classes, the 11 entity renderers, the
entity/state + withered + item + menu + nether + network classes. That unblocks the
glossy clouds / volumetric fog / bloom / shadow silhouette / skybox, the
head/tentacle/severed rendering, and the Rocket Retriever / Formidibomb items.

---

## BUILD FIX LOG

- **`compileJava` — 6 errors (SeveredRope.java)**: Vineflower decompile mangling
  (`import [Lnet.minecraft...Vec3;;` and `(Vec3;)` cast). Fixed.
- **`compileJava` — 100 errors ("package net.minecraft.client does not exist")**:
  root cause was `loom { splitEnvironmentSourceSets() }` in build.gradle. This mod keeps
  all client code in `src/main/java` (main source set), which needs the client Minecraft
  classes. Split source sets removed them from the main classpath. Fixed by removing
  `splitEnvironmentSourceSets()`.

- **`compileJava` — `missing return statement` (SeveredWitherStorm.java:14923, then latent in WitherStormDevourer.java + WitherStormP4.java)**: Vineflower ran out of memory and left 3 very large model `createBodyLayer()` methods as bytecode comments only. Reconstructed all three from the bytecode dumps with a JVM stack/local-slot interpreter (`tools/reconstruct_models_generic.py`). Verified the generated part trees match each class's constructor `getChild()` exactly (67/67, 302/302, 3/3 parts) and box/pose counts match the bytecode. Added the missing geom imports. **Green build reached at commit `04f1e48`.**

- **Full clean build — GREEN (first cloud build)**: The GitHub Actions workflow
  (`.github/workflows/build.yml`) now builds on every push. Its first full clean
  compile hit a `java.lang.StackOverflowError` inside javac's `TransTypes`
  generic-translation pass. Root cause: the huge single-chain `createBodyLayer()`
  model expressions (thousands of chained `addBox()` calls) make javac recurse
  deeper than the default JVM thread stack. Fixed by forking the Java compiler
  with `-Xss64m` in `build.gradle`. Confirmed `conclusion: success` on commit
  `d40b808`; a mod jar is uploaded as the `dabywitherstormmod` artifact on every run.
