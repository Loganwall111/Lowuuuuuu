# Batch 17 Visual Phase 1

Date: 2026-08-23
Branch: `arena/01a02fba-lowuuuuuu`

## What changed

This pass started the Batch 17 skybox / atmosphere work using the user's MCSM references as the color target, without introducing raw GLSL.

### Centralized sky palette tuning
- `src/main/java/net/dabicco/witherstormmod/client/StormPalettes.java`
  - Added centralized upper-sky anchors for the purple -> turquoise -> cataclysm transition.
  - Shifted the blend windows to better match the requested timing:
    - purple holds until `4.5`
    - turquoise ramps across `4.5 -> 5.5`
    - cataclysm ramps from `5.8 -> 6.15`
  - Added `skyColor(...)` so the dome color is driven from the same palette hub as fog/clouds.

### Smooth dominant-storm palette ownership
- `src/main/java/net/dabicco/witherstormmod/client/StormSkyDarken.java`
  - Fixed the old "phase multiplied by proximity" behavior, which could incorrectly downgrade a distant phase-5 storm back into an early purple palette.
  - Added a separate `paletteBlend()` ownership factor so phase identity and sky-claim strength are tracked independently.
  - Added `skyR/G/B()` accessors so the sky dome can use the phase palette directly.

### Sky dome now follows the phase palette
- `src/main/java/net/dabicco/witherstormmod/mixin/SkyRendererMixin.java`
  - Swapped the dome darkening blend from the static floor color to the phase-aware sky palette.

### Flat MCSM cloud takeover while storm-active
- `src/main/java/net/dabicco/witherstormmod/client/StormCloudDeck.java`
  - Added `replacesVanillaClouds()`.
  - Gated the flat slab deck to the requested late-growth window (`phase >= 4.5`).
  - Let deck opacity and palette mixing respond to the sky-claim strength.
- `src/main/java/net/dabicco/witherstormmod/mixin/CloudColorMixin.java`
  - Cancels vanilla cloud rendering while the storm cloud deck is actively taking over.
  - Keeps fallback vanilla cloud tinting phase-aware when vanilla clouds still render.

### Supporting atmosphere tweaks
- `src/main/java/net/dabicco/witherstormmod/client/StormStarfield.java`
  - Teal wash now respects palette ownership instead of only raw phase.
- `src/main/java/net/dabicco/witherstormmod/client/StormPresenceFX.java`
  - Moved the black rim-glare emphasis to phase `5+` so it lines up better with the requested sky state.
- `src/main/java/net/dabicco/witherstormmod/DevouringStormsModClient.java`
  - Clears `StormPresenceFX` state on disconnect.

## Still pending

These Batch 17 items are still not finished in this pass:
- separate permanent HALO from one-shot PULSE event logic
- restore / verify the glowing white death dissolve
- update `docs/VIDEO_ACCURACY_STATUS.md` once this is playable
- full renderer-side use of recovered stage/source art remains deferred because the extracted files are not drop-in atlas replacements
- full rename to **Devouring Storms** still pending

## Validation
- No local Gradle build was possible in this sandbox because Java/JDK is unavailable.
- `git diff --check` passed on the edited visual-phase files.
