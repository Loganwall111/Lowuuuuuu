# Devouring Storms — clean rewrite

This repository is the **clean rewrite** of **Devouring Storms**, a modernized Fabric rebuild of the original *Minecraft: Story Mode* Wither Storm mod work. The old decompiled source was partially broken (missing models, mixins, renderers), so instead of patching it in place, this branch rebuilds the project as a coherent source-first mod.

**Original jar source:** `dabywitherstormmod-1.9.60-26.2-beta.zip` (Fabric, Minecraft 26.2, Java 25)
**Current mod id / artifact:** `devouringstorms`
**License:** MIT — see `LICENSE` (© 2026 Dabicco).

---

## What's in here

```
src/main/resources/       Mod assets (textures, models, blockstates, lang, sounds, data)
src/main/java/            Fresh Java source
├── DevouringStormsMod        clean server entrypoint
├── DevouringStormsModClient  clean client entrypoint
├── Mod*                      kept, working registries (config, sounds, blocks, items...)
└── entity/
    ├── WitherStormEntity     fresh phase-driven boss core
    ├── WitherStormPhase      phase enum + growth requirements
    ├── ai/                   fresh AI goals (hunt, absorb)
    ├── ability/              fresh ability framework + super skull, tractor beam
    ├── renderer/             renderer pipeline wired to Blockbench-driven models
    └── model/                model layer registry
build.gradle                  Fabric Loom build script
docs/                         rewrite plan + feature roadmap + validation notes
tools/                        asset recovery / restore helpers
```

## Building

Build on a machine with **Java 25** and network access to Fabric/Maven.

The repo ships with the **Gradle wrapper scripts** (`gradlew`, `gradlew.bat`) and
`gradle/wrapper/gradle-wrapper.properties`. If `gradle-wrapper.jar` is missing, fetch it once:

```bash
# A) One-shot setup script
bash tools/setup-gradle-wrapper.sh

# B) Or, if you already have Gradle 8+/9+
gradle wrapper --gradle-version 9.5.1
```

Then build:

```bash
./gradlew build            # Linux / macOS
# gradlew.bat build        # Windows
# output: build/libs/devouringstorms-1.9.60-26.2-beta.jar
```

> `gradle-wrapper.properties` pins **Gradle 9.5.1**. If Fabric Loom needs a newer Gradle,
> bump it there and rerun the wrapper setup.

## Recovering the missing classes from the original jar

See **`tools/restore-missing-classes.sh`** — it decompiles
`dabywitherstormmod-1.9.60-26.2-beta.zip`, rewrites the recovered sources into the
current `net.dabicco.devouringstorms` package, and drops the missing `.java` files back
into `src/main/java`.

## Project docs

- **[docs/REWRITE_PLAN.md](docs/REWRITE_PLAN.md)** — architecture and package layout
- **[docs/WITHER_STORM_FEATURE_ROADMAP.md](docs/WITHER_STORM_FEATURE_ROADMAP.md)** — long-form feature roadmap
- **[docs/VIDEO_ACCURACY_STATUS.md](docs/VIDEO_ACCURACY_STATUS.md)** — current implementation / validation status against the MCSM references
- **[docs/BATCH_17_ASSET_AUDIT.md](docs/BATCH_17_ASSET_AUDIT.md)** — recovered texture inventory from the Blockbench archives

## Credits

- **Dabicco** — Lead Programmer
- **Joeyready** — Textures, Blocks, Items & Build
