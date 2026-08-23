# Batch 17 Visual Phase 2

Date: 2026-08-23
Branch: `arena/01a02fba-lowuuuuuu`

## What changed

This pass moved Batch 17 forward from the sky pass into the storm-body event pass:
- split the late-game permanent HALO from the one-shot command-block PULSE
- restore a renderer-side white collapse/vanish path for the main storm body and severed halves

## HALO / PULSE split

### New one-shot client pulse packet and FX
- `src/main/java/net/dabicco/witherstormmod/network/StormPulsePayload.java`
  - Added a lightweight clientbound payload carrying `(entityId, x, y, z, phase)`.
- `src/main/java/net/dabicco/witherstormmod/client/StormPulseFX.java`
  - Added a transient world-space pulse effect rendered from `LevelRenderEvents.COLLECT_SUBMITS`.
  - Uses the existing storm palette colors and additive glow helpers rather than introducing GLSL.
  - Includes a short command-block power swell and optional deep thump.
  - Respects the existing pulse size/strength config knobs.

### Packet wiring and cleanup
- `src/main/java/net/dabicco/witherstormmod/DevouringStormsMod.java`
  - Registered `StormPulsePayload` in the clientbound payload registry.
- `src/main/java/net/dabicco/witherstormmod/DevouringStormsModClient.java`
  - Registered the client receiver.
  - Registered the pulse renderer on `LevelRenderEvents.COLLECT_SUBMITS`.
  - Clears pulse state on disconnect.

### Authoritative server trigger
- `src/main/java/net/dabicco/witherstormmod/entity/WitherStormEntity.java`
  - Added an authoritative pulse dispatch path.
  - When a storm is formidibombed at late phase and there are at least three nearby late-phase storms within the storm's expanded local area, the storm now sends the one-shot pulse event exactly once.
  - Added NBT persistence for the one-shot latch so reloads do not retrigger it.
- `src/main/java/net/dabicco/witherstormmod/command/DevouringStormsCommand.java`
  - Added `/storm pulse <targets>` for manual in-game triggering while tuning the visuals.

### Permanent halo moved back onto the body
- `src/main/java/net/dabicco/witherstormmod/client/StormPresenceFX.java`
  - Removed the old continuous halo/pulse rendering responsibilities from the atmospheric system.
  - Left only the surrounding black rim glare and ejecta there.
- `src/main/java/net/dabicco/witherstormmod/entity/renderer/WitherStormRenderer.java`
  - Added a phase-driven attached halo pass that rides with the storm body instead of living as a freestanding atmosphere glow.
- `src/main/java/net/dabicco/witherstormmod/config/DevouringStormsClientConfig.java`
  - Updated the related client-config descriptions so the toggles reflect the new split.

## White death dissolve restoration

### Main storm body
- `src/main/java/net/dabicco/witherstormmod/entity/state/WitherStormRenderState.java`
  - Added collapse whiteout/fade state.
- `src/main/java/net/dabicco/witherstormmod/entity/renderer/WitherStormRenderer.java`
  - Extracts collapse whiteout timing from the authoritative collapse clock.
  - Applies whiteout/fade tinting to the storm body pieces.
  - Adds a collapse glow bloom so the body does not just disappear without the requested white relight feeling.

### Severed halves
- `src/main/java/net/dabicco/witherstormmod/entity/state/SeveredWitherStormRenderState.java`
  - Added collapse whiteout/fade state.
- `src/main/java/net/dabicco/witherstormmod/entity/renderer/SeveredWitherStormRenderer.java`
  - Mirrors the severed-body whiteout/fade timing so the split pieces follow the same collapse direction.

## Still pending

Batch 17 is still not fully playable/validated here. Remaining important items:
- verify the exact in-game timing/look of the pulse, attached halo, and white collapse against the MCSM references
- confirm the three-storm proximity trigger radius feels right in play
- finish the remaining resource/rebrand work outside this visual pass
- update `docs/VIDEO_ACCURACY_STATUS.md` only after there is a coherent playable pass

## Validation
- No local Gradle build was possible in this sandbox because Java/JDK is unavailable.
- `git diff --check` passed on the files touched in this phase.
- Runtime/compile status remains unverified in this environment.
