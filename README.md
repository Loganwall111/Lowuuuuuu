# 🌟 Official Releases & Quick Downloads (Minecraft 1.21.2 & 26.2)

> **All official Minecraft: Story Mode packs and mod builds are published in the [GitHub Releases Page](https://github.com/Loganwall111/Lowuuuuuu/releases/tag/v1.9.60-26.2-mcsm)!**

| Deliverable | Description | Direct Download | Install Location |
| :--- | :--- | :---: | :--- |
| **MCSM Shader Pack** | Iris/OptiFine shaderpack with 8 Story Mode cloud presets, `clouds=fast` pipeline routing in `shaders.properties`, identical `precision highp float;` headers, and turquoise teeth glow. | [📥 **Download `MCSM_ShaderPack.zip`**](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a04054-lowuuuuuu/MCSM_ShaderPack.zip) | `.minecraft/shaderpacks/` *(Do NOT unzip)* |
| **MCSM Resource Pack** | Authentic Story Mode textures, sounds, OptiFine skies, and modern 26.2 JSON metadata schema (`supported_formats`, `min_format`, `max_format`). | [📥 **Download `MCSM_ResourcePack.zip`**](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a04054-lowuuuuuu/MCSM_ResourcePack.zip) | `.minecraft/resourcepacks/` *(Do NOT unzip)* |
| **Wither Storm Mod JAR** | Official 1.21.2 / 26.2 Fabric mod JAR with crash-free post-chain shaders, 3D spherical shield halo, and unified `/devouringstorms` command. | [📥 **Download Mod JAR (CI #33123018070)**](https://github.com/Loganwall111/Lowuuuuuu/actions/runs/33123018070) | `.minecraft/mods/` |

### 🚀 Quick Setup Instructions
1. **Shaderpack**: Put `MCSM_ShaderPack.zip` in `.minecraft/shaderpacks/`. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
2. **Resource Pack**: Put `MCSM_ResourcePack.zip` in `.minecraft/resourcepacks/`. In Minecraft: Options -> Resource Packs -> enable **MCSM_ResourcePack** (ensure conflicting cloud packs like `Story Mode Clouds.zip` are disabled).
3. **Mod**: Put the mod JAR in `.minecraft/mods/`.

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
