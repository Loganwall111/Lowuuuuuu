# Dabicco's Wither Storm Mod — source-project workspace

This repository holds **Dabicco's Wither Storm Mod** (the *Minecraft: Story Mode*–inspired Wither Storm boss) as a source-ready Fabric mod project.

**Original jar:** `dabywitherstormmod-1.9.60-26.2-beta.zip` (Fabric, Minecraft 26.2, Java 25)
**License:** MIT — see `LICENSE` (© 2026 Dabicco). Modification and redistribution are permitted with attribution.

---

## What's in here

```
src/main/resources/       Extracted mod assets (textures, models, blockstates, lang, sounds, data)
src/main/java/            Empty skeleton — the decompiled source goes here (Phase 0)
build.gradle              Fabric Loom build script (reconstructed from the jar's manifest)
settings.gradle           Gradle / Fabric plugin repos
gradle.properties         Version pins (minecraft 26.2, loader 0.19.3, loom 1.17.19)
docs/                     Feature roadmap for the one-to-one Story Mode replica
mod/                      Full raw extraction of the jar (gitignored; reference only)
dabywitherstormmod-*.zip  The original compiled jar as provided
```

## Important — read this first

The provided zip is a **compiled** mod: it contains `.class` bytecode, **not Java source**. You cannot edit it in place, and it cannot be rebuilt in a sandbox without a Java 25 + Fabric + Gradle environment or network access to the Fabric Maven / Maven Central / Mojang.

To actually modify and extend the mod you must first **decompile the jar into `src/main/java`** (Vineflower / CFR / Procyon, mapped with Yarn/Mojmap for 26.2), then build with:

```bash
gradle build
```

Set the correct `fabric_version` and `yarn_mappings` in `gradle.properties` first.

## Building the mod (on a machine with Java 25 + network)

```bash
# 1. Decompile the jar into src/main/java
# 2. Fix fabric_version in gradle.properties
# 3. Build
gradle build
# output: build/libs/dabywitherstormmod-1.9.60-26.2-beta.jar
```

## Replicating the Story Mode Wither Storm

See **[docs/WITHER_STORM_FEATURE_ROADMAP.md](docs/WITHER_STORM_FEATURE_ROADMAP.md)** for the full phased plan: spawn/command-block genesis → growth phases → destruction & tornado → the weapon set (Rocket Retriever / Formidibomb / Super TNT) → the "Bowels" interior finale → Story Mode texture pack → config/polish.

## Credits

- **Dabicco** — Lead Programmer
- **Joeyready** — Textures, Blocks, Items & Build
