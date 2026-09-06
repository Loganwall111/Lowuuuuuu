# Devouring Storms 1.9.140 — mega-phase 6a: the particle field from the frames

The four particle reads the reference frames show, now drawn by the storm
renderer (stateless - every position is a hash of its index plus time, so
nothing is stored, synced or spawned through the particle API):

- **Black cubes** peeling off the silhouette edge and drifting out/down -
  the signature debris read of every storm close-up.
- **Sparkle dots riding down inside the beam cones**, fading as they fall,
  spread widening toward the ground like the frames.
- **Faint motes orbiting the whole storm** - the "subtle particles
  everywhere" layer, half purple half pale white.
- **Mist puffs clinging to the storm's base**, very low alpha violet-grey.

All of it is batched into exactly two extra draws (one translucent, one
additive) on the nearest storm only, gated from phase ~4 and fading with
distance, so it costs nothing at range and never touches the particle
engine's API surface.

Unchanged: the 1.9.139 Telltale-construction glare (gradient backdrop +
emissive mouth details), welded blob, purple face overlay, built-in Iris
shader pack with the DEFAULT-ON toggle.

Install: drop the jar in `mods/`.
