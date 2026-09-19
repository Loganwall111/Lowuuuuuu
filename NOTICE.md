# Devouring Storms: The Point of No Return — origin & ownership notice

**Devouring Storms: The Point of No Return** is a fork and continuation of
**Dabicco's Wither Storm Mod** (Minecraft 26.2, Fabric).

- The original mod is licensed **MIT, Copyright (c) 2026 Dabicco** — the full
  license text is kept unmodified in [`LICENSE`](LICENSE), as MIT requires.
  MIT expressly grants the rights to use, copy, modify, merge, publish,
  distribute and sublicense this software.
- The fork is made with the original author's stated permission; the author
  has stepped away from the mod. Attribution is preserved here, in LICENSE,
  and in the in-game load banner ("base: Dabicco's Wither Storm Mod").

## What lives where in this tree

| Path | What it is | Generation |
|---|---|---|
| `net/`, `src/main/resources/`, `build.gradle`, `gradle.properties`, `settings.gradle`, `docs/` | The author's clean source + Fabric scaffold, migrated from branch `the-sorsce-` (commit f7c89f2) | **1.9.60** (171 java files; predates the town/structures system) |
| `src-recon/` | CI-recovered source: Vineflower decompilation of every `net/dabicco` class in the pinned 1.9.100 base jar (see `src-recon/RECON_SUMMARY.txt`) | **1.9.100** — matches the jar all releases since are built on |
| `mcsm-extras/` | The Devouring Storms overlay: mixins, shaders, config panel, `/ds` commands — written in this project | 1.9.100 → current |
| `ci/` | Build, deep-scan and source-recovery pipelines | current |
| `jar-overrides/` | Shader/texture overrides applied at assembly | current |

## Build lineage

`1.9.100 base jar` (author's compiled mod, hash-pinned in `ci/build.sh`)
→ overlay releases `mcsm-1.9.100 … 1.9.112`
→ rebrand `ds-1.9.113+` (display name, jar name, tags, banner; mod id
`dabywitherstormmod` retained while any class still comes from the jar)
→ **next:** compile the whole mod from the recovered/unified source, then the
namespace rename to `devouringstorms` becomes possible — that is the point
where the fork is source-complete and the id changes are ours to make.

Existing worlds and configs are tied to the `dabywitherstormmod` id; the
rename will be called out in release notes when it ships.
