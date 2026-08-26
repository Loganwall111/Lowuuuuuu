# Wither Storm Mod — Clean Rewrite Plan

We are rewriting the mod from scratch as a clean, modern Fabric mod for
**Minecraft 26.2 / Java 25 / Fabric Loader 0.19.3**. The old decompiled source is
partially broken (missing models, mixins, renderers), so instead of patching it we
design a fresh, coherent architecture. Working pieces are preserved; broken ones are
rewritten cleanly.

## Principle: keep what works, rewrite the rest

**Kept (self-contained & working):**
- `config/` — the whole config system (`WitherStormWorldConfig`, `WitherStormConfigs`,
  client config + sync payloads) works and is independent of the broken parts.
- `ModSounds`, `ModParticles`, `ModPotions`, `ModEffects`, `ModEnchantments`,
  `ModItemGroups`, `ModAdvancements` — registry helpers, self-contained.
- `block/` + `ModBlocks` — withered block set, super TNT, furnace filter.
- All resources under `src/main/resources` (textures, models, sounds, lang, data).

**Rewritten clean (this branch):**
- `DabyWitherStormMod` / `DabyWitherStormModClient` — clean init that only wires the
  things that exist.
- `entity/ModEntityTypes` — clean entity registration (storm, head, skull, cluster,
  severed, tentacle, + the working support entities).
- `entity/WitherStormEntity` — **fresh** AI / phase machine / ability code.
- `entity/WitherStormPhase` — clean phase enum + progression.
- `entity/ai/*` — clean goal classes (move, hunt, flee, absorb).
- `entity/ability/*` — clean ability classes (beam, skull, snatch, roar, absorb, tornado).
- `entity/state/*`, `entity/renderer/*`, `entity/model/ModEntityModelLayers` — modern
  render-state + renderer scaffolding that plugs straight into Blockbench models.
- `ModItems` — cleaned to drop references to the deleted custom item classes.

## Architecture (package map)

```
net.dabicco.witherstormmod
├── DabyWitherStormMod          main entrypoint
├── DabyWitherStormModClient    client entrypoint
├── Mod*                        registries (kept)
├── config/                     kept, working
├── entity/
│   ├── ModEntityTypes
│   ├── WitherStormEntity       fresh core
│   ├── WitherStormPhase        phase enum
│   ├── WitherStormHeadEntity   kept (dep on a small mixin accessor we re-create)
│   ├── ai/                     goals
│   ├── ability/                abilities
│   ├── cluster/                kept
│   ├── state/                  render states (fresh)
│   ├── renderer/               renderers (fresh, plug into Blockbench models)
│   └── model/                  model layers
└── network/                    payloads (kept, reconstructed earlier)
```

## Data flow (how the storm works)

- `WitherStormEntity` holds a `phase` (double 0..6.99) synced via entity data.
- `addSubGrowth(n)` accumulates "consumed" units; when the threshold for the current
  phase is met the storm advances to the next phase (roar + transition).
- Phase thresholds come from `WitherStormWorldConfig` (kept).
- Each tick the storm picks an **intent** (move / hunt / absorb / idle) and runs the
  **abilities** the current phase unlocks (beam at phase 4+, skulls phase 3+,
  snatch phase 5+, absorption always after phase 1).
- The client renders via `WitherStormRenderState` + `WitherStormRenderer`, which pull
  a `WitherStormModel` from `ModEntityModelLayers` — the models the user builds in
  Blockbench.

## How to build

```bash
gradle build
```

See `README.md` and `docs/` for the rest. The models under `entity/model/` are
skeletons that must be replaced with the user's Blockbench exports.
