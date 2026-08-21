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

```bash
# 1. Make sure fabric_version + yarn_mappings are set in gradle.properties
# 2. Build
gradle build
# output: build/libs/dabywitherstormmod-1.9.60-26.2-beta.jar
```

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
