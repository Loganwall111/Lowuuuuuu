# Sift-Core

A standalone Fabric 1.20.1 sandbox for building an original, near one-to-one **The Sift** experience before it is merged into the larger dimensions project.

The visual language is based on the public reference trailer supplied for this project: an empty abyss, green-black fog, colorful liquid layers, animated spacetime rifts, and fast falling motion. This workspace uses new procedural code and shaders rather than extracting game assets or footage.

## Current vertical slice

### Phase 1 — seamless fall handshake

- `EntityMixin` watches server-side players in the Overworld below `Y < -64`.
- `SiftTransfer` captures position, yaw, pitch, head/body rotation, fall distance, gravity state, and the complete velocity vector before moving the player to `mcsm:the_sift`.
- The captured state is restored after `moveToWorld`, including `velocityModified`, so the player keeps tumbling instead of being placed at a dimension spawn point.
- The client mixin suppresses the vanilla `DownloadingTerrainScreen` only for a Sift respawn packet. The required network respawn packet is still sent; this removes the cutscene/loading interruption without pretending a protocol hop does not occur.
- `sky.fsh` receives `Depth`, `FallSpeed`, and `GameTime` uniforms. Depth is driven by the player's falling Y position and is clamped so the void-to-fog curve stays stable.

### Phase 2 — rifts and cosmic window

- The dimension is an air-only flat generator: `layers: []`, `features: false`, and the `minecraft:the_void` biome.
- `SiftRiftEntity` is a non-collidable, non-attackable visual entity. A small server spawner keeps a moving field of rifts around players.
- `SiftRiftRenderer` draws a camera-facing procedural portal using `rift.fsh`. The fragment pass contains layered wave displacement, a cosmic interior, star points, and an emissive broken rim.

### Phase 3 — fluid pass

- `SiftFluidRenderer` adds purely visual, collision-free horizontal fluid sheets around the player. No fluid blocks are registered, so a player can fall through every layer.
- `final.fsh` supplies animated refraction, teal/amethyst/magenta iridescence, and proximity-based white intersection foam. The current foam is a deterministic screen-space approximation; a later pass can feed a real depth/normal buffer when a post-processing backend is selected.

### Phase 4 — build and validation

- This directory is a standalone Gradle/Loom project targeting Java 17, Minecraft 1.20.1, and Fabric API.
- Run the lightweight offline checks from the repository root with `python3 sift-core/tools/validate.py`.
- On a machine with Java 17 and network access, run `gradle build` from this directory. The sandbox image used by the coding agent does not include a JDK or Gradle, so the validation script is run here instead of claiming a compiled jar that was not produced.

## Test commands

After launching a dev client/server with the dimension data loaded:

```text
/sift              Toggle between the Overworld and The Sift
/sift enter        Enter The Sift at a safe viewing height
/sift fall         Place the player at Y -63 with downward velocity; the next server tick exercises the handshake
/sift return       Return to the Overworld at Y 96
```

The dimension key is intentionally `mcsm:the_sift` so the sandbox can be merged into the future Minecraft Dimensions Forged namespace without changing datapack references.
