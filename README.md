# 🌟 Official Releases & Quick Downloads (Minecraft 1.21.2 & 26.2)

> **Every official Minecraft: Story Mode build is published as a real GitHub Release
> asset on the [Releases page](https://github.com/Loganwall111/Lowuuuuuu/releases/tag/v1.9.60-26.2-mcsm).**
> The `MCSM Integrated Release Build` workflow (`.github/workflows/mcsm-release.yml`)
> recompiles the mod JAR from the latest branch sources, rebuilds both packs, **renames
> the JAR on every build** (`-r<build-number>` so no launcher can reuse a cached stale
> jar), and force-uploads everything over that release. Never download from raw branch
> links again — they were the source of the stale-visuals problem.

| Deliverable | Description | Permanent Download Link | Target Location |
| :--- | :--- | :--- | :--- |
| **`MCSM_ResourcePack.zip`** | Authentic Story Mode textures, sounds, the original custom time-of-day skyboxes in `assets/minecraft/optifine/sky/world0/` (lavender→orange day sky + purple sunset + twilight night), 8 blocky cloud sheets, 32-bit RGBA items, and modern split-range `pack.mcmeta`. | [📥 Release asset](https://github.com/Loganwall111/Lowuuuuuu/releases/download/v1.9.60-26.2-mcsm/MCSM_ResourcePack.zip) or [branch mirror](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a048fa-lowuuuuuu/MCSM_ResourcePack.zip) | `.minecraft/resourcepacks/` *(Do NOT unzip)* |
| **`MCSM_ShaderPack.zip`** | Atmosphere shaderpack for Iris/OptiFine: `clouds=fast` routing, 8 cloud samplers, 2.5x extruded cloud slabs, dynamic `uniform long worldTime` sky dome, and the re-aligned seam-free cloud UV mapping (Protocol 5 below). | [📥 Release asset](https://github.com/Loganwall111/Lowuuuuuu/releases/download/v1.9.60-26.2-mcsm/MCSM_ShaderPack.zip) or [branch mirror](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a048fa-lowuuuuuu/MCSM_ShaderPack.zip) | `.minecraft/shaderpacks/` *(Do NOT unzip)* |
| **`dabywitherstormmod-…jar`** | The Fabric mod, compiled fresh from the latest master on every release. Bundles the storm atmosphere backdrop (`StormAtmospherePost` purple-phase overlays), all `shaders/post` storm filters + `post_effect` definitions, the sky/cloud mixins, and the custom skyboxes — the JAR itself carries the OG visuals. Current name: `dabywitherstormmod-1.9.61-26.2-beta-r{N}.jar` (see release page for the latest N). | [📥 Release page](https://github.com/Loganwall111/Lowuuuuuu/releases/tag/v1.9.60-26.2-mcsm) | `.minecraft/mods/` |
| **Bundle: Resource Pack + Mod** | One-file combo of `MCSM_ResourcePack.zip` + the renamed mod JAR. | [📥 Release asset](https://github.com/Loganwall111/Lowuuuuuu/releases/download/v1.9.60-26.2-mcsm/MCSM_ResourcePack_and_Mod.zip) | split per instructions |
| **Bundle: Shader Pack + Mod** | One-file combo of `MCSM_ShaderPack.zip` + the renamed mod JAR. | [📥 Release asset](https://github.com/Loganwall111/Lowuuuuuu/releases/download/v1.9.60-26.2-mcsm/MCSM_ShaderPack_and_Mod.zip) | split per instructions |

### 🚀 Quick Setup
1. **Mod**: put the newest `dabywitherstormmod-…-r<N>.jar` into `.minecraft/mods/`, **deleting any 1.9.60 jar** still sitting there.
2. **Resource Pack**: enable `MCSM_ResourcePack` (Options → Resource Packs). Conflicting cloud packs off.
3. **Shader Pack**: select `MCSM_ShaderPack` (Video Settings → Shader Packs) alongside or instead of another shader.
4. Verify in-game: version reads **1.9.61-26.2-beta** — if it shows 1.9.60 you still have the stale jar.

---

# Dabicco's Wither Storm Mod — clean rewrite

This repository is the **clean rewrite** of Dabicco's Wither Storm Mod (the
*Minecraft: Story Mode*–inspired Wither Storm boss). The original decompiled source
was partially broken (missing models, mixins, renderers), so instead of patching it we
rebuild it as a fresh, modern Fabric mod.

**Original jar:** `dabywitherstormmod-1.9.60-26.2-beta.zip` (Fabric, Minecraft 26.2, Java 25)
**License:** MIT — see `LICENSE` (© 2026 Dabicco).

---

## What's in here

```
src/main/resources/       Mod assets (textures, models, blockstates, lang, sounds, data)
src/main/java/            Fresh Java source
├── DabyWitherStormMod    clean server entrypoint
├── DabyWitherStormModClient  clean client entrypoint
├── Mod*                  kept, working registries (config, sounds, blocks, items...)
└── entity/
    ├── WitherStormEntity  fresh phase-driven boss core
    ├── WitherStormPhase   phase enum + growth requirements
    ├── ai/                fresh AI goals (hunt, absorb)
    ├── ability/           fresh ability framework + super skull, tractor beam
    ├── renderer/          fresh renderer (plugs into Blockbench models)
    └── model/             model layer registry
build.gradle              Fabric Loom build script (from the jar's manifest)
docs/                     rewrite plan + feature roadmap
mod/                      Full raw extraction of the jar (gitignored; reference only)
```

## Building (on a machine with Java 25 + network access to Fabric/Maven)

The repo ships with the **Gradle wrapper** (`gradlew`, `gradlew.bat`,
`gradle/wrapper/gradle-wrapper.properties`). Only the wrapper **jar** needs fetching
once (it is a ~43 KB binary and can't be committed as text). Two ways:

```bash
# A) One-shot script (recommended) — downloads gradle-wrapper.jar:
bash tools/setup-gradle-wrapper.sh

# B) If you already have any Gradle 8+/9+ installed:
gradle wrapper --gradle-version 9.5.1
```

Then:

```bash
# 1. Make sure fabric_version + yarn_mappings are set in gradle.properties
# 2. Build
./gradlew build            # Linux / macOS
# gradlew.bat build        # Windows
# output: build/libs/dabywitherstormmod-1.9.60-26.2-beta.jar
```

> `gradle-wrapper.properties` pins **Gradle 9.5.1** (matches the original jar's
> `Fabric-Gradle-Version`). If Fabric Loom 1.17.19 needs a newer Gradle, bump the
> version there and re-run the setup script.

## Recovering the "missing" classes from the original jar

See **`tools/restore-missing-classes.sh`** — it decompiles
`dabywitherstormmod-1.9.60-26.2-beta.zip` and drops the 129 missing `.java` files back
into `src/main/java`, unblocking the mixins, models, renderers and items the clean
rewrite needs to compile fully.

## The rewrite

See **[docs/REWRITE_PLAN.md](docs/REWRITE_PLAN.md)** for the architecture and data
flow, and **[docs/WITHER_STORM_FEATURE_ROADMAP.md](docs/WITHER_STORM_FEATURE_ROADMAP.md)**
for the feature list.

The Wither Storm is a phase-driven state machine: it absorbs blocks/items/mobs to grow,
and each phase unlocks new abilities. Models are built in **Blockbench** and dropped
into `entity/model/`; the renderer and layer registry are already wired to consume them.

## Credits

- **Dabicco** — Lead Programmer
- **Joeyready** — Textures, Blocks, Items & Build
