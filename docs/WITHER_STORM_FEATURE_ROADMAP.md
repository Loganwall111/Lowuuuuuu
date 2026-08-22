# Wither Storm — One-to-One Story Mode Replica: Feature & Implementation Roadmap

**Project:** Dabicco's Wither Storm Mod `1.9.60-26.2-beta` (Fabric, Minecraft 26.2, Java 25)
**Goal:** Make the mod's Wither Storm a faithful, feature-complete replica of the *Minecraft: Story Mode* Wither Storm boss (as shown in the reference longplay), including its Story-Mode-accurate textures.

> **Important context.** This repo contains the mod as a **compiled jar** (352 `.class` files, no Java source). This roadmap is written to be *directly executable* by anyone with the mod's **source code** on a machine that can run **Java 25 + Fabric Loader + Gradle (Fabric Loom)**. That environment cannot be provisioned inside this sandbox (no Java, no Gradle, and no network access to Maven Central / Fabric Maven / Mojang), so the *code* steps below are the blueprint for that environment.

---

## How to read this document

- **[✓] Implemented** — already present in the jar (verified from class names + assets). No work needed, or only polish.
- **[+] Missing / gap** — exists in *Minecraft: Story Mode* and/or the video but is not implemented. This is work to add.
- **[~] Partial** — exists but is incomplete relative to Story Mode.

Each section lists the relevant source classes to modify so the work is concrete.

---

## Phase 0 — Get a buildable source project (prerequisite)

The mod must first exist as *source*, not a jar.

| Task | Detail | Status |
|------|--------|--------|
| Decompile the jar | Run a Java decompiler (Vineflower / CFR / Procyon) over `dabywitherstormmod-1.9.60-26.2-beta.zip`, with Minecraft 26.2 + Fabric mappings applied, and commit the recovered `.java` into `src/main/java`. | **[+] Required first step** |
| Restore the build | `gradle.properties`, `settings.gradle`, `build.gradle`, `gradle/wrapper` already reconstructed in this repo. Set the real `fabric_version` for 26.2 and `yarn_mappings` build. | **[✓] scaffold present** |
| Verify it compiles & launches | `./gradlew build` then run a client; confirm the Wither Storm spawns with the stock behaviour before touching anything. | **[+] on build machine** |

> The full class inventory (`src/main/resources`, `docs/`) tells you exactly which source files the decompiler must produce. Nothing below can start until this phase passes.

---

## Phase 1 — Spawn & the command-block "genesis" (Story Mode Episode 1 moment)

The Wither Storm in Story Mode begins as an ordinary Wither that gets fused with a command block, then tears through the sky.

| Story Mode feature | Mod status | Work to do |
|---|---|---|
| Wither → Wither Storm transformation cutscene | `[~]` `WitherStormSummon`, `StormSpawnPlatform` | Add a configurable spawn animation: a normal Wither summons the command block, the player must *place* the command block to trigger phase 1 (mirrors Story Mode). |
| Spawn platform / block pillar the player builds | `[✓]` `StormSpawnPlatform` | Verify placement requirement matches Story Mode (command block on top of a wither-skull-and-soul-sand beacon). |
| Command block core visible in the Wither's ribcage | `[✓]` core rendering (`CommandBlockPowerSound`, core tint mixins) | Polish: make the core glow/beep only when active; add the magenta "command block" texture to the core model. |
| The storm begins (darkening sky, wind, rain) | `[✓]` `StormSkyDarken`, `CloudColorMixin`, `LevelRainShelterMixin` | Optionally add a configurable "first roar" sound + screen shake when it activates. |

**Exit criteria for Phase 1:** on a fresh world, spawning the Wither Storm plays a short activation sequence and the storm visibly begins.

---

## Phase 2 — Growth & evolution phases (the "bigger and bigger" arc)

Story Mode shows the Wither Storm growing through several distinct body stages.

| Phase | Mod status | Work to do |
|---|---|---|
| Phase 1 — Wither with command core | `[✓]` `WitherStormModel` | Already a "Commanded Wither". Verify it flies and droops. |
| Phase 2–3 — mass grows, develops a torn-flesh body | `[✓]` `WitherStormModel` + `WitherVeinLayer`, `wither_veins.png` | Confirm the growth modifiers (`/witherstormmod consumedEntities`) advance stages. |
| Phase 4 — giant head, huge mouth, starts absorbing terrain | `[✓]` `WitherStormP4`, `phase_4_assets.png` / `phase_4_assets_e.png` | Polish model scale and jaw animation. |
| Phase 5 — multiple heads & massive tentacles | `[✓]` `WitherStormTentacles5`, `WitherStormHead` entity | Ensure all heads chomp independently (see `death.attack…chomp`). |
| Phase 7 / Devourer — final giant form | `[✓]` `WitherStormTentaclesDevourer`, `devourer_assets.png` | This is the "final boss" mass — verify it only appears at max consumed-entity count. |
| Smooth automatic transitions between phases | `[~]` | Ensure phase-up happens *automatically* as entities/blocks are consumed, with a transition effect (flash, grow, roar). |

**Exit criteria for Phase 2:** leaving the Wither Storm alone lets it visibly grow through all phases on its own, ending at the Devourer.

---

## Phase 3 — Destruction & world interaction (the "flattens your world" claim)

| Story Mode feature | Mod status | Work to do |
|---|---|---|
| Absorbing blocks (torn chunks of terrain) | `[✓]` `ClusterBlocksPayload`, `WitheredBlockEntity`, `DarkenedMovingBlockRenderState` | Add debris that gets sucked into the body in a spiral (debris tornado). |
| Block cluster orbiting the storm | `[✓]` `ClusterMesh`, `StormDebris`, `WitherStormClusterRenderState` | Polish spiral/rise physics. |
| Tractor beam pulling mobs/blocks/player | `[✓]` `TractorBeamRenderer`, `tractor_beam.png` | Verify beam can grab players and mobs, not just blocks. |
| Absorbing mobs → turns into Withered mobs | `[✓]` `WitheredMobs`, `WitheredBlockEntity` | Ensure villagers/houses get absorbed into the mass and respawn as withered variants. |
| Wither sickness effect on nearby players | `[✓]` `WitherSicknessPayload`, `ClientSicknessManager` | Confirm progressive wither debuff while near the storm. |
| Tornado / storm vortex with debris + rain | `[+]` `CaveRumble`, rain mixins present but no dedicated tornado core | **Add** a rotating vortex entity (dust/debris column) that moves with the storm and damages/throws players. This is the most visible Story Mode signature that still needs a dedicated implementation. |
| Destructive ground beam that carves a path | `[~]` beam renderers present (`BeamGroundLoopSound`, `BeamMoteSpawner`) | Confirm the storm carves a continuous swath of destroyed terrain as it travels. |

**Exit criteria for Phase 3:** the storm visibly consumes blocks, mobs and the player, leaving a carved wasteland and a spinning debris tornado.

---

## Phase 4 — The weapon set (Rocket Retriever / Formidibomb / Super TNT)

In Story Mode the player counters the storm with a rocket that flies into the command block, and a giant bomb.

| Story Mode feature | Mod status | Work to do |
|---|---|---|
| Rocket Retriever (aims into the core) | `[✓]` `RocketRetrieverItem`, `RetrieverContents`, `RetrieverTooltip`, rocket/TNT count models | Verify multi-stage counter (rockets then TNT) and that it targets the command block. |
| Formidibomb (giant TNT that flattens mountains) | `[✓]` `FormidibombItem`, `FormidibombRenderer`, `FormidibombBlast`, `FormidibombFlash` | Confirm the "flattens mountains" radius and that it can damage the core. |
| Super TNT (bigger TNT block/entity) | `[✓]` `SuperTntBlock`, `SuperTntRenderer` | Polish blast radius / chain reactions. |
| Command block must be broken via the retriever | `[✓]` core health + `WitherStormPositionPacket` | Ensure the core only becomes "breakable" at the right phase. |

**Exit criteria for Phase 4:** the full weapon loop works — build retriever → fire into core → break command block → the storm temporarily destabilizes.

---

## Phase 5 — Inside the Wither Storm (the "Bowels" boss arena)

This is Story Mode's signature finale: the player enters the Wither Storm's interior and destroys its heart.

| Feature | Mod status | Work to do |
|---|---|---|
| Interior biome ("Bowels") with hallways, gravity flipping, portals | `[✓]` `Bowels*` (Body, Hallway, BackHall, EndRoom, Gravity, Portal, Frame, Mantle, Route, Trace, Flip, Entry) | Verify gravity-flip rooms and looping geometry work. |
| Heart entity (final destroyable core) | `[✓]` `BowelsHeartEntity`, `BowelsHeartRenderer` | Confirm the heart has health and is the actual kill condition. |
| Maw entrance (enter the storm through its mouth) | `[✓]` `BowelsMawEntity`, `BowelsMawRenderer` | Verify the player can enter/exit through the giant mouth. |
| TNT → heart damage mechanic | `[✓]` `SeveredRope`, `SeveredTentacleEntity`, `TentaclePathPayload` | This is the "severed tentacles + TNT" finale. Confirm the loop. |
| Final cinematic defeat + storm collapse | `[+]` `BowelsFinale` exists but confirm | **Add** an end cinematic: command block shatters, storm falls from the sky, world is left as a crater. |

**Exit criteria for Phase 5:** the full Story Mode finale is playable — enter the mouth, traverse the Bowels, blow up the heart, watch the storm fall.

---

## Phase 6 — Presentation & "Story Mode texture pack" fidelity

| Feature | Mod status | Work to do |
|---|---|---|
| Story-Mode-accurate textures (dark violet/black flesh, glowing magenta core, jagged teeth) | `[~]` textures exist (`wither_storm.png`, `phase_4_assets*.png`, `devourer_assets.png`) | Re-skin each body-atlas texture to match the Story Mode colour/look *while preserving the exact UV layout* so the existing code-defined models don't break. Ship as a resource pack layered above the mod. |
| Cinematic camera / HUD / action buttons (TNT prompt) | `[✓]` `ActionButtons`, `BowelsHud`, `BowelsView`, `BowelsMusic` | Verify the on-screen "place TNT" prompt appears at the heart. |
| Boss health bar | `[✓]` `BossEventMixin` | Confirm a proper boss bar shows phases. |
| Dynamic storm audio / music that swells | `[✓]` `StormMusic`, `StormAmbienceSound`, `StormLoopSounds`, `StormTornadoSound`, `BowelsMusic` | Polish volume/phase transitions. |
| Cinematic bloom / shadow / fog for the storm | `[✓]` `StormBloom*`, `StormShadow*`, `StormGlowRenderer`, `StormSceneDepth` | Confirm depth-of-field style presentation during the finale. |

**Exit criteria for Phase 6:** the mod *looks* like Story Mode — dark, glowing, cinematic — and the texture pack applies cleanly over the existing models.

---

## Phase 7 — Config, polish & regression

| Task | Status | Work to do |
|---|---|---|
| `WitherStormConfigScreen` full options | `[✓]` | Add a toggle for the new tornado + cinematic effects. |
| Multiplayer sync | `[✓]` `SigeonNetwork`, all `*Payload`, `ClientDistantStormManager` | Re-test storm position/health sync across 2+ clients. |
| Performance (distant storm culling) | `[✓]` `DistantStormRenderer`, `GpuBufferPool`, `StormProfiler` | Verify distant-render toggle. |
| Full regression | `[+]` | Playtest all phases start-to-finish; fix regressions. |

---

## Immediate action items for the person/agent with build access

1. Decompile the jar into `src/main/java` (Phase 0) — **nothing else can proceed without source**.
2. Rebuild + launch the stock mod to confirm parity.
3. Implement, in order: **tornado core (Phase 3)** → **phase-transition cinematics (Phase 2)** → **final defeat cinematic (Phase 5)** → **Story Mode texture pack (Phase 6)**.
4. Then run the polish pass in Phase 7.

*Asset inventory and the full class catalogue are under `src/main/resources/assets/dabywitherstormmod/` and the extracted `mod/` tree.*

---

## FEATURE DELIVERY LOG (cloud build green at every entry)

- **Batch 1 — Instant/Infinite Growth + Tentacle Slams** (`88b9e53`, green): new world-config
  toggles `instantGrowth` + `instantGrowthRate`, `infinitePhases` + `phaseCeiling`, and
  `tentacleSlam` + `tentacleSlamInterval` + `tentacleSlamRadius`. Instant growth multiplies
  every bit of eaten growth (`WitherStormEntity.addSubGrowth`); infinite phases lifts the
  phase ceiling (`growthCeiling()`); tentacle slams carve a crater with `carveSphere`,
  damage + fling nearby living entities, and run every phase via the server `aiStep` branch.
- **Batch 2 — `/storm` control command** (`dea3576`, green): clean, separate command tree —
  `/storm grow <targets> <amount>`, `/storm slam <targets>`, `/storm phase <targets> <phase>`.
  Adds `WitherStormEntity.forceTentacleSlam()`. Kept out of the giant single-line `dabyws`
  chain to avoid breaking it.

Remaining planned batches (each pushed and auto-built by Actions):
  Atmosphere tuning (storm fog / purple aura, boss-music range is already configurable as
  `stormMusicRange`), far-lands haze, biome-tinted fogs, story-structure raids + portal
  raids, death cinematic, Story Mode building mechanics, and textures/assets (require
  externally-produced artwork).
- **Batch 3 — Storm Proximity Fog** (`d9c8b5d`, green): client-config `stormFog` +
  `stormFogStrength` (default off). New `client/StormFog` computes a fog multiplier from
  distance to the nearest storm (real entity or distant-storm data); `FogRendererMixin`
  multiplies fog start/end by it, so the purple haze closes in as you approach.
- **Batch 4 — Structure Raids** (`b650588`, green): world-config `structureRaid` +
  `structureRaidInterval` + `structureRaidRadius`. While the storm tours built structures
  (Structures targeting mode) it actively levels its current target, caving chunks out of
  it on a timer via `carveSphere`. Runs in the server `aiStep` branch.
- **Batch 5 — Far-Lands Haze + `/storm roar/status`** (`9ea6c4b` fixed by `d9e0fdf`, green): client
  config `farLandsHaze`/`farLandsDistance`/`farLandsStrength`; new `client/FarLandsHaze` closes
  the fog in as you travel from world origin. FogRendererMixin now combines tower gloom, storm
  proximity fog and far-lands haze. Also `/storm roar <targets>` and `/storm status <targets>`.
  (Batch 5's first push failed on a wrong `camera.getPosition()` call; fixed to `camera.position()`.)
- **Batch 6 — Phase-up Shockwave + `/storm consume`** (`9a83ff2`, green): every integer phase-up
  emits a damaging/flinging shockwave (bigger in Devourer form); `consumeBlocks(server, radius)`
  carves a sphere into storm growth; `/storm consume <targets> <radius>`.
- **Batch 7 — `/storm preset`** (`221eefc`, green): one-shot presets `instant`, `raid`,
  `apocalypse` that flip the new feature toggles in one command and sync clients.
- **Batch 8 — Storm Death Blast** (`a3ece34`, green): world-config `deathBlast`/`deathBlastRadius`.
  On destruction the storm detonates, carving a huge crater, playing the Formidibomb explosion +
  explosion particle, and flinging/damaging everything in a radius via a `die(DamageSource)` override.
- **Batch 9 — `/storm spawn <phase>`** (`18ee8da`, green): spawn a fresh storm at a chosen phase.
- **Batch 10 — `/storm heal|kill`** (`ca76f73`, fixed `0e52832`, green): heal restores health; kill
  destroys via `die(damageSources().genericKill())` so the death blast fires. (First push used
  `LivingEntity.kill()` which doesn't exist in this MC; fixed.)
- **Batch 11 — Devourer Berserk Rage** (`a530b3e`, green): config `berserk`/`berserkHealth`/
  `berserkSlamInterval` — below a health fraction the Devourer slams far more often.
- **Batch 12 — Config-screen sections** (`7a92cd2`, green): new server config sections surface all
  the new growth/slam/raid/death/berserk toggles in the in-game Wither Storm config screen.
