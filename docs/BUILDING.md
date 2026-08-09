# Building Devouring Storms

## 0. Assets (all three deliverables)

All textures and audio are **generated**, never redistributed:

```bash
pip install pillow numpy soundfile
python3 tools/generate_assets.py            # regenerate everything
python3 tools/generate_assets.py --skip-existing   # keep hand-made replacements
```

The **structures** (v1.1: Endertown, the Watcher Shrine, the Mainframe Ruin, the Rift
Obelisks) are generated too — one Python geometry source emitted for both editions:

```bash
python3 tools/generate_structures.py
```

Outputs:
- Java: gzipped structure NBT → `java-mod/src/main/resources/data/devouring_storms/structure/*.nbt`
  (placed by the datapack worldgen in `worldgen/{structure,structure_set,template_pool,processor_list}`)
- Bedrock: `bedrock-addon/DevouringStormsBP/scripts/builds_data.js` — the behaviour engine stamps
  the same geometry out block-by-block with `system.runJob` (Bedrock add-ons can't register
  worldgen structures)
- the Endertown Core item icon → `bedrock-addon/DevouringStormsRP/textures/items/`

Outputs land directly in:
- `java-mod/src/main/resources/assets/...`
- `bedrock-addon/DevouringStormsRP/{textures,sounds}/...`

## 1. Java mod (Fabric — Minecraft 26.2)

Requirements: **JDK 25+**, and the checked-in Gradle wrapper (`gradlew` / `gradlew.bat`
+ `gradle/wrapper/gradle-wrapper.jar`, wrapper pins Gradle 9.1 — no local Gradle needed).

```bash
cd java-mod
./gradlew build           # → build/libs/devouring-storms-1.4.0.jar
./gradlew runClient       # dev client with the mod loaded
```

A note on restricted networks (CI/proxy sandboxes): the first build needs outbound HTTPS to
`services.gradle.org` (Gradle distribution), `maven.fabricmc.net` (Loom, Fabric Loader,
Fabric API) and Mojang's piston endpoints (`piston-data|libraries.minecraft.net` for the
game jar + libraries). If any of those are firewalled, the build stops at whichever download
comes first (message like `Remote host terminated the handshake`). Allowlisted networks
build with zero further setup.

Toolchain pinned in `gradle.properties`: Minecraft `26.2` (ships **unobfuscated**, so the
build uses `loom.officialMojangMappings()` — Yarn no longer exists for 26.x), Fabric Loader
`0.19.3`, Fabric API `0.156.0+26.2`.

### Notes on forward-porting
Minecraft renames things every few releases. Everything version-sensitive is either
Fabric API (stable by convention) or fenced into small, commented files:

| Risk area | File | What to check |
|---|---|---|
| Cross-dimension teleport | `util/RiftTravel.java` | `teleportTo` vs `TeleportTransition` signature |
| Cross-dimension entity teleport (same call) | `entity/WatcherEntity.java`, `entity/TazoEntity.java` | same |
| Mob griefing gamerule | `entity/MassgEntity.java` | `GameRules.MOB_GRIEFING` vs `RULE_MOB_GRIEFING` |
| Rift translucent render layer | `DevouringStormsClient.java` | `BlockRenderLayerMap` / `RenderLayer` naming |
| HUD layer registry | `DevouringStormsClient.java` + `client/StormVisuals.java` | `HudElementRegistry` (1.21.2+); older `HudRenderCallback` fallback |
| BlockEntity `build()` | `registry/ModBlockEntities.java` | some versions want `.build(null)` |
| Custom damage loot check | `util/ModLoot.java` | FAPI `LootTableEvents` signature |
| Structure NBT `DataVersion` | `tools/generate_structures.py` | any int is accepted & upgraded; bump if Mojang ever rejects old ones |
| Worldgen JSON format | `data/devouring_storms/worldgen/structure/*` | `jigsaw` type fields (`project_start_to_heightmap`, `max_distance_from_center`) if a future version renames them |
| Location-trigger advancements | `data/devouring_storms/advancement/{endertown,shrine,obelisk}.json` | `conditions.location.structures` predicate shape |
| Loot-container advancement | `advancement/endertown_cache.json` | `player_generates_container_loot` condition key |
| `block_rot` processor | `worldgen/processor_list/mainframe_rot.json` | `integrity` + `rottable_blocks` predicate list |
| Block click handlers | `block/SealedVaultBlock.java`, `block/TerminalBlock.java` | `useItemOn` / `useWithoutItem` signature shape (ItemStack first param) |
| Inventory helpers | `block/SealedVaultBlock.java` | `hasAnyMatching`, `clearOrCountMatchingItems`, `inventoryMenu.getCraftSlots()` |
| Enum exhaustiveness | `storm/MassgPhase.java` (BOWELS) | all `switch (phase)` sites: `bossName`, `StormMusicDirector`, renderer swell (has `default`) |
| Emissive overlay material | `DevouringStormsRP/entity/massg.entity.json` | `entity_emissive_alpha` + the glow render controller if Mojang renames builtin materials |
| Entity-item interaction | `item/SeventhTrumpetItem.java` | `interactLivingEntity(ItemStack, Player, LivingEntity, InteractionHand)` parameter shape |
| Falling-block debris | `entity/MassgEntity.java#trySpawnSegment` | `FallingBlockEntity#fall(Level, BlockPos, BlockState)` + `time` / `disableDrop` fields |
| Damage-source kill path | `entity/MassgEntity.java` (rend) | `damageSources().generic()` + direct `die(...)` semantics (loot drops inside `die`) |
| Husk invulnerability | `entity/MassgEntity.java`, DevouringStormsBP massg `ds_husk` | Java: `isInvulnerableTo` gate; Bedrock: `minecraft:damage_sensor` with `deals_damage: false` |
| Bedrock pocket realms | `main.js#ensurePocket` | End-plane pseudo-dimensions at x=±1000 — a workaround because Bedrock has no custom dimension API |

### The two shader systems
1. **In-game (vanilla):** the mod ships `assets/devouring_storms/post_effect/storm_glitch.json`
   (data-driven post pipeline, 1.21.2+) **and** the in-mod screen-space overlay
   (StormVisuals) produces the same aesthetic with no external shader loader required.
2. **Iris/OptiFine:** `shaders/DevouringStormsShaderPack/` is the full "massive" pipeline
   (two rifts in the sky, storm fog, entity vein-glow, film-grain tearing). It stands alone
   and is the recommended way to experience the storm.

## 2. Bedrock add-on (26.40)

No compilation step — the packs are the build. To install:

```bash
cd bedrock-addon
zip -r DevouringStormsBP.mcpack DevouringStormsBP
zip -r DevouringStormsRP.mcpack DevouringStormsRP
# or bundle both into one .mcaddon (zip of the two directories, renamed)
```

Bedrock has no worldgen API for custom structures, so the Decayed Realm builds
(Endertown & co.) are stamped by the behaviour engine from `scripts/builds_data.js`
(generated — see §0). The town appears automatically the first time anyone enters the
realm; later, the craftable **Endertown Core** (end stone + purpur ring around a Commanded
Star) re-raises the town wherever you use it — handy after MASSG eats it.

.Import in-game: create a world → Behavior Packs → activate **DevouringStormsBP**
(the RP activates automatically via dependency). **No experiments are required** — the
scripting module uses stable `@minecraft/server@2.0.0` APIs only.
Bedrock Realms: upload both packs; enable RP/BP on the realm world.

## 3. Sanity-checking the packs

- Every JSON in the repo is parseable (`tools/generate_assets.py` also regenerates
  model/blockstate JSON — re-running it never overwrites your hand edits to other files).
- Bedrock event parity functions live in `DevouringStormsBP/functions/ds/`:
  `/function ds/summon_massg`, `/function ds/phase_signal` … `/function ds/reset`
  — usable from command blocks like DR's workflow.

## 4. Replacing generated art with licensed art

Tazo/The Watcher official skins are private (creator: *the gaming gamer*). When licensed,
drop the PNGs over `java-mod/.../textures/entity/{tazo,watcher}.png` and
`bedrock-addon/DevouringStormsRP/textures/entity/{tazo,watcher}.png` and run the generator
with `--skip-existing`.
