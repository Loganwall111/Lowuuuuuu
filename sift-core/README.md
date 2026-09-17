# Sift-Core

A standalone Fabric 1.20.1 sandbox for building an original, near one-to-one **The Sift** experience before it is merged into the larger dimensions project.

The visual language is based on the public reference trailer supplied for this project: an empty abyss, green-black fog, colorful liquid layers, animated spacetime rifts, and fast falling motion. This workspace uses new procedural code and shaders rather than extracting game assets or footage.

## Important version note

The current sandbox and CI artifact target **Minecraft 1.20.1**, not 1.20.2. The manifest is intentionally pinned to `1.20.1` so an incompatible launcher profile fails clearly instead of producing a misleading blank render. Use a Fabric 1.20.1 instance with the matching Fabric API when testing `sift-core`.

If the game window is completely black, first remove the Sift-Core jar from the 1.20.2 instance and launch the 1.20.1 profile. The Sift renderer is only active inside `mcsm:the_sift`; a black title/menu screen is not normal behavior.

## Current vertical slice

### Phase 1 — seamless fall handshake

- `EntityMixin` watches server-side players in the Overworld below `Y < -64`.
- `SiftTransfer` captures position, yaw, pitch, head/body rotation, fall distance, gravity state, and the complete velocity vector before moving the player to `mcsm:the_sift`.
- The captured state is restored after `moveToWorld`, including `velocityModified`, so the player keeps tumbling instead of being placed at a dimension spawn point.
- The server synchronously warms a 3x3 destination chunk window before sending the respawn packet; The Sift uses an empty flat generator, so this is a small deterministic preparation rather than a terrain-generation wait.
- The client mixin suppresses the vanilla `DownloadingTerrainScreen` only for a Sift respawn packet and explicitly clears any already-visible copy. The required network respawn packet is still sent; this removes the cutscene/loading interruption without pretending a protocol hop does not occur.
- `sky.fsh` receives `Depth`, `FallSpeed`, and `GameTime` uniforms. Depth is driven by the player's falling Y position and is clamped so the void-to-fog curve stays stable. The sky now layers a readable green-black-to-violet vertical gradient, drifting aurora curtains, falling light shafts, and depth-scaled star dust.

### Phase 2 — rifts and cosmic window

- The dimension uses a sparse flat generator with custom `mcsm:siftstone` and `mcsm:sift_moss` floor layers, `features: false`, and the `minecraft:the_void` biome.
- `SiftTerrainSpawner` adds deterministic floating shelves made from Siftstone, Sift Moss, and Rift Crystal as the player falls. The open routes between shelves preserve the original free-fall character.
- `SiftRiftEntity` is a non-collidable, non-attackable visual entity. A small server spawner keeps an authored, deterministic twelve-slot formation around players; the pattern changes only after the formation cycle ages out.
- `SiftEncounterController` drives a synchronized swell, snap, and settle beat across each formation on the same 800-tick cadence. It only updates visual rift intensity, so the choreography never creates a collision surface or interrupts the fall.
- `SiftRiftRenderer` draws a camera-facing procedural portal using `rift.fsh`. The fragment pass contains layered wave displacement, a cosmic interior, star points, an emissive broken rim, and two atmospheric envelope passes: a low-pressure haze plus animated colored filaments that bleed beyond the aperture.
- `/sift status` reports dimension, position, velocity, and all preserved rotations so the fall handshake can be verified without guessing from the camera.
- A procedural `Sift Drifter` is the first ambient mob: it is a real living entity with health and a server-authoritative free-flight impulse, but no collision or hostile AI yet.

### Phase 3 — fluid pass

- `SiftGroundRenderer` adds a distant procedural abyss floor above the generated custom-block floor. It is visual shader geometry only, while the actual terrain remains sparse enough for players to fall through the open route.
- `SiftFluidRenderer` adds purely visual, collision-free horizontal fluid sheets around the player. No fluid blocks are registered, so a player can fall through every layer.
- `final.fsh` supplies animated refraction, teal/amethyst/magenta iridescence, flowing caustic bands, liquid glints, and proximity-based white intersection foam. Its flow direction is driven by the same deterministic field used by server-side current physics. The current foam is a screen-space approximation; a later pass can feed a real depth/normal buffer when a post-processing backend is selected.

### Phase 4 — build and validation

- This directory is a standalone Gradle/Loom project targeting Java 17, Minecraft 1.20.1, and Fabric API. It includes its own Gradle 8.7 wrapper (`./gradlew`) so it can be built without relying on the parent project.
- Run the lightweight offline checks from the repository root with `python3 sift-core/tools/validate.py`.
- On a machine with Java 17 and network access, run `./gradlew build` from this directory. The sandbox image used by the coding agent does not include a JDK or Gradle, so the validation script is run here; GitHub Actions is the authoritative compiled build for this checkout.

## Test commands

After launching a dev client/server with the dimension data loaded:

```text
/sift              Toggle between the Overworld and The Sift
/sift enter        Enter The Sift at a safe viewing height
/sift fall         Place the player at Y -63 with downward velocity; the next server tick exercises the handshake
/sift return       Return to the Overworld at Y 96
/sift status       Print dimension, position, velocity, and rotation state
```

The dimension key is intentionally `mcsm:the_sift` so the sandbox can be merged into the future Minecraft Dimensions Forged namespace without changing datapack references.
