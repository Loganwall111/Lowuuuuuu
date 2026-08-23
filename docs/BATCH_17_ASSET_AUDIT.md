# Batch 17 Asset Audit

Generated during the Batch 17 texture recovery pass.

## What was recovered

- Source of truth scanned: `src/main/resources/assets/devouringstorms/geo/**/*.bbmodel`
- Repro script: `tools/extract_bbmodel_textures.py`
- Destination: `src/main/resources/assets/devouringstorms/textures/entity/`
- Result on this branch: **117 PNG files present**, representing **94 unique embedded texture names** recovered from the Stage A-D and related `.bbmodel` archives.

### Notable stage/body textures now staged in the runtime resources tree

These are the key skin files the user asked to recover from inside the Blockbench models:

- `skM0_witherstormStageA.png`
- `skM0_witherstormStageB.png`
- `skM0_witherstormStageCRibs.png`
- `skM0_witherstormStageDbloodA.png`
- `skM0_witherstormStageDbloodB.png`
- `skM0_witherstormStageDbloodC.png`
- `devour_wither_storm_night.png`
- `fx_haloWitherstormStageB.png`
- `obj_blockCommandA.png`
- `obj_blockCommandALights.png`
- `obj_blockCommandDamageA.png`
- `obj_blockCommandDamageB.png`
- `tile_sandWitherstorm.png`
- `tile_sandWitherstormInterior.png`
- `tile_witherBloodA.png`
- `tile_witherstormVortexABackdrop.png`

### Where the embedded textures were found most heavily

Approximate unique embedded texture-name counts by `.bbmodel` folder:

- `Blocks/` → 41
- `Misc/` → 51
- `Stage_A/` → 12
- `Stage_B/` → 16
- `Stage_C/` → 15
- `Stage_D/` → 24
- `Traced_shading_Textures/` → 56
- `Wither_Storm_Deadass/` → 10
- `Wither_Storm_Interior/` → 53

## Important implementation note

The extraction pass successfully restored the original embedded PNG payloads, but the active Java-rendered storm models still expect the following atlas sizes:

- `WitherCommandBlock` / early body: **64x96**
- `WitherStormDevourer`: **256x256**
- `WitherStormP4`: **512x512**

Most of the recovered embedded stage textures are much smaller tile-like sources (for example `skM0_witherstormStageB.png` is `16x16`, `skM0_witherstormStageA.png` is `4x4`). That means they are now **preserved and available**, but they are **not drop-in replacements** for the current pre-Batch-18 Java model atlases.

In other words:

- Batch 17 asset recovery is done.
- Direct runtime swap-in for the current `StormSkins` atlas files is still unresolved.
- Proper one-to-one use of these recovered sources likely belongs to the deferred Stage A-D model port, or to a future atlas rebuild that repacks the recovered tiles into the sizes the current Java models expect.

## Block texture audit

### What is definitely present and referenced

Referenced `assets/.../textures/block/` entries currently resolve for the runtime JSON/model set, including:

- `formidibomb`
- `mushroom_withered`
- `stripped_withered_log_side`
- `stripped_withered_log_top`
- `stripped_withered_planks`
- `super_tnt`
- `torn_withered_flesh`
- `withered_bedrock`
- `withered_cobblestone`
- `withered_flesh_block`
- `withered_log_side`
- `withered_log_top`
- `withered_netherbrick`
- `withered_planks`
- `withered_sand`
- `withered_stone`

Resource scan status at this checkpoint: **no missing block texture references were detected in the shipped asset JSON/model set**.

### What was *not* found

No standalone grass-themed block texture asset was found under the current mod resources, and no JSON/model reference to a custom grass texture was found either. If a missing grass texture is expected, it is not presently checked into this repository under `src/main/resources/assets/devouringstorms/`.

## Cleanup caveat

The Blockbench archives also contain novelty/placeholder/joke-named embedded files. The extractor intentionally preserved almost all of them so that no original bytes were lost, but that means the recovered folder includes non-production names alongside real assets.

Examples include:

- `astolfo.png`
- `go ahead pee.png`
- `another placeholder.png`
- `this one is accurate.png`

Those should be treated as **archive artifacts**, not proof that they belong in the final live renderer path.

## Windows compatibility note

Two recovered archive filenames originally contained `:` and were renamed in the committed resource tree for Windows/git compatibility:

- `1:1 flesh.png` → `1_1_flesh.png`
- `this is 1:1 too.png` → `this_is_1_1_too.png`

`tools/extract_bbmodel_textures.py` now applies those same safe renames during extraction so the files do not reappear with invalid Windows path characters on future reruns.
