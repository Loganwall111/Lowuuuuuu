# Missing source classes — status & what still needs re-decompiling

This file tracks the source classes the decompiled `src/main/java` is missing.
A first audit found 52 classes via `import` statements; a second, deeper audit
found **more** classes that are referenced only by **same-package** name (so no
`import` line exists), most importantly the model classes. Several have now been
reconstructed. The rest fall into two groups: those that can be reconstructed
from their consumers, and those that **cannot be reproduced without the original
bytecode/source**.

## ✅ Reconstructed (committed)

| Class | Package | Confidence | Source of truth |
|---|---|---|---|
| `StormRemovedPacket` | network | High | `SigeonNetwork`, `WitherStormEntity`, `StormSkyDarken` |
| `WitherStormPositionPacket` (+`HeadData`,`SeveredData`) | network | High | `SigeonNetwork`, `ClientDistantStormManager`, `DistantStormRenderer`, `WitherStormEntity` |
| `TentaclePathPayload` | network | High | `SigeonNetwork` |
| `CaveRumblePayload` | network | High | `CaveRumble`, `BowelsBoss`, `CaveRumbleClient` |
| `CommandBlockPowerPayload` | network | High | `BowelsHeartEntity`, client handler |
| `FormidibombFlashPayload` | network | High | `FormidibombEntity`, `FormidibombFlash` |
| `SpawnStructurePayload` | network | High | `DabyWitherStormMod`, client handler |
| `WitherSicknessPayload` | network | High | `WitherSickness`, `ClientSicknessManager` |
| `WitheredCastPayload` | network | High | `ClientWitheredManager` |
| `ActionButtonPayload` | network | High | `BowelsActionKeys`, `ActionButtons` |
| `WitherStormRenderState` | entity.state | High | `SnatchGrab`, `TentaclePhysics`, `StormModelPreview`, `DistantStormRenderer`, models |
| `WitherStormHeadRenderState` | entity.state | High | `DistantStormRenderer`, `BowelsMawRenderer` |
| `SeveredWitherStormRenderState` | entity.state | High | `StormModelPreview`, `DistantStormRenderer` |
| `WitherStormClusterRenderState` | entity.state | High | `WitherStormClusterRenderer` |
| `BlackHoleRenderState` | entity.state | High | `BlackHoleRenderer` |
| `DarkenedMovingBlockRenderState` | entity.state | High | `WitherStormClusterRenderer` |
| `ModMenus` | menu | Medium | Fabric `MenuTypeRegistry` / `FabricMenuTypeBuilder` API |
| `FurnaceFilterMenu` | menu | Medium | `FurnaceFilterBlockEntity.createMenu`, `FurnaceFilterScreen` |
| `NetherScaleManager` | nether | Medium | `DabyWSCommand.trigger`, `NetherScaleEntity` |

All reconstructed files follow the mod's existing patterns (e.g. the
`RegistryFriendlyByteBuf` + `StreamCodec` payload pattern from
`ClusterBlocksPayload`/`SyncWitherStormConfigPayload`) and match the exact field
names/accessors the present code calls.

## ⛔ Cannot be reconstructed without the original source/bytecode

These are the real blockers. They cannot be written correctly from usage sites:

### 1. The model classes — `entity.model` (11 files)
These are **BlockBench-generated** geometry: thousands of lines of `addBox(...)`
cube definitions. They are data, not logic, and cannot be recreated by hand.
Referenced from `ModEntityModelLayers.registerModelLayers()`:

- `SuperSkull`
- `WitherCommandBlock`
- `WitherStormP4`
- `WitherStormDevourer`
- `WitherStormTentaclesDevourer`
- `StormCoverModel`
- `WitherStormHead` (also needs `createGlowLayer`, `createEyeGlowLayer`, `upperJaw()`)
- `WitherStormGrowth5`
- `WitherStormTentacles5`
- `Tentacle`
- `SeveredWitherStorm`

### 2. The mixin accessors/invokers — `mixin` (14 files)
These target **unmodified Minecraft classes** and need the exact obfuscated
field/method mapping names for MC 26.2. Guessing them will fail at runtime even if
they compile. (Requires the real mappings / original source.)

`CubePolygonsAccessor`, `FireworkRocketEntityAccessor`, `GameRendererAccessor`,
`ItemTintSourcesAccessor`, `LevelRendererTargetsAccessor`, `LivingEntitySwimAccessor`,
`ModelPartAccessor`, `ModelPartCubesAccessor`, `RangeSelectItemModelPropertiesAccessor`,
`RenderPipelinesAccessor`, `RenderTypeInvoker`, `SelectItemModelPropertiesAccessor`,
`SoundBufferAccessor`, `WitherBossAccessor`.

### 3. Depends on the models above, so also blocked until they're restored
- **renderer** (11): `WitherStormRenderer`, `WitherStormHeadRenderer`, `SeveredWitherStormRenderer`,
  `CrossDimensionalRenderer`, `FormidibombRenderer`, `GrabTentacleRenderer`, `GrappledTntRenderer`,
  `NetherScaleRenderer`, `SuperSkullRenderer`, `SuperTntRenderer`, `WitheredBlockRenderer`
- **entity.withered** (2): `WitheredMobs`, `WitheredBlockEntity`
- **item** (4): `FormidibombItem`, `RocketRetrieverItem`, `RetrieverContents`, `RetrieverTooltip`

## Recommended next step

Re-decompile the original jar and restore **at minimum** the 11 model classes and
14 mixins above (they are the hard blockers). Once those are back, the renderer /
item / withered classes can be reconstructed from their now-present consumers, or
re-decompiled alongside. The 19 reconstructed classes here can then be validated
against the real sources and corrected if any field name differs.
