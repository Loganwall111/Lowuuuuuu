# Creature Models - Why Each Model Is What It Is

This document explains the 9+ mob models in Devouring Storms V2 revamp and why they wear the bodies they wear.

## Summary Table

| Entity ID | Display Name | Model Class | Scale (S) | Texture | Why This Body |
|-----------|--------------|-------------|-----------|---------|---------------|
| `mcsm:mas` | Massg / Mas | `MassgModel` | 6.0x | `massg.png` | Black warden needs: hunched posture, heavy brow, 2 long horns swept back, coat to ground, red eye lenses on own part so body stays black while eyes glow |
| `mcsm:creator` | The Creator | `CreatorModel` | 8.0x | `creator.png` | Colossal god needs: crowned head, 3 spinning halos, white-hot eyes on own part, 2 thin arms 200 units long coming down out of sky, barely moves |
| `mcsm:voidwalker` | Voidwalker | `VoidwalkerModel` | 1.0x | `voidwalker.png` | Thin zombie-like: hollow torso, jaw that hangs open on own part, one arm up, violet slits, tatter strip |
| `mcsm:void_lurker` | Void Lurker (mini-boss) | `VoidLurkerModel` | 2.4x | `void_lurker.png` | The avoid boss: bulbous body, maw that opens downward, 6 tentacles each with lit tip that whips a beat behind, reach/pull/swallow behavior |
| `mcsm:whale_monster` | Whale Monster (legacy) | `VoidLurkerModel` reused | 2.4x | `whale_monster.png` | Same mesh as lurker because bulbous + maw + 6 tentacles IS a whale monster already - saves unique model while distinct texture |
| `mcsm:void_whale` | Void Whale Colossal | `VoidWhaleColossalModel` | 4.0x | `void_whale.png` + `void_whale/void_whale.png` | V2 epic: 12 segments each animated with sine wave, tail sweeps, fins flap, head glow that sings - leviathan that needs segmentation, not single body |
| `mcsm:void_dweller` | Void Dweller (talker) | `VoidLurkerModel` reused | 2.4x | `voidwalker.png` / `void_dweller.png` | Lives in luminous gel, talks via synced data line, wears lurker mesh in void colors because fluid body + trailing limbs reads as gel dweller; non-hostile attributes |
| `mcsm:drifter` | Drifter (decayed reality) | `DrifterModel` | 1.0x | `drifter.png` | Decayed reality's own: hooded slab over skull, ash where face should be, 2 violet slits, tatters hanging off hip, loping low |
| `mcsm:keeper` | Keeper (infinite dim) | `KeeperModel` | 1.0x | `keeper.png` | Infinite dimension's own: tall, brimmed hat (14x14 flat ring), lantern box in hand + lamp inside on own part that swings a beat late, walks like it has all time |
| `mcsm:colossal_octopus` + `mcsm_sift:colossal_octopus` | Colossal Octopus | `ColossalOctopusModel` | 3.5x | `colossal_octopus.png` (both namespaces) | 8 tentacles radial, beak under head, mantle on back, each tentacle+tip on own part with phase offset 0.785 rad, rainbow chromatophores via texture |
| `mcsm:jokest_creature` + `mcsm_sift:jokest_creature` | Jokest Creature | `JokestCreatureModel` | 1.2x | `jokest_creature.png` | Bouncing trickster: grin box under head, ears as two boxes, belly, bounce = abs(sin)*0.35, head y offset -bounce*4, leaves joke particles |

## Why Reuse Happens (Whale Monster + Dweller sharing Lurker mesh)

**Question: why does whale_monster and void_dweller use void_lurker model?**

1. **Whale Monster**: Original design doc said "void mini-boss (avoid) whose tentacles reach, pull, swallow" + "whale monster I talked about". A bulbous body + maw + 6 tentacles satisfies BOTH descriptions - it's already a whale. Creating a separate whale mesh that is also bulbous+tentacled would be duplicate geometry. So WhaleRenderer reuses `VoidLurkerModel` with different texture `whale_monster.png` and different scale/shadow.

2. **Void Dweller**: Lives in gel, talks, doesn't fight. The lurker mesh (bulbous fluid body + trailing limbs) reads as something that lives in fluid. It needed to be visible and not invisible (entity type with no renderer = invisible mob walking at you). Reusing lurker mesh with voidwalker colors + glow overlay was fastest safe path, then V2 adds dedicated `void_dweller.png`.

3. **Sift namespace duplicates**: `mcsm_sift:colossal_octopus` and `mcsm_sift:jokest_creature` exist so datapack spawns (`mcsm_sift` dimension) find entities. They point to same `McsmBeast` class + same model, but sift textures. This is why `McsmEntities` registers both `mcsm:colossal_octopus` AND `mcsm_sift:colossal_octopus` - same beast, two IDs.

## Guarded Factories - Why They Exist

Every model goes through `McsmMobModels.guarded()` + `humanoidRoot()`:

- `humanoidRoot()` bakes mod layer, checks `complete()` (7 parts humanoid needs: head, hat, body, left_arm, right_arm, left_leg, right_leg). If incomplete, falls back to vanilla `ModelLayers.PLAYER` skeleton.
- `guarded()` tries to build on mod skeleton, if fails tries spareRoot (player), if fails returns null but NEVER throws into resource reload.
- Why? Player log: `NoSuchElementException: Can't find part hat` during `EntityRenderers.createEntityRenderers` aborted whole resource reload -> black screen, no fonts, no models. Now missing part animates as parent.

The `hat` part carries 0.01 unit cube (sub-pixel, invisible) so baker cannot drop it as empty.

## Animation Philosophy Per Creature

- **Massg**: hunches (head forward 0.16 + sin), shoulders roll inward, horns follow head *0.4 + 0.22, coat sways opposite body.
- **Creator**: barely moves, halos do moving: yRot = t*(0.01+i*0.004), sky arms sway 0.018 rad slow, out of step 0.7 rad.
- **Voidwalker**: one arm up -1.9 rad, other hangs 0.55, head down 0.28, jaw works abs(sin)*0.3 independent clock.
- **VoidLurker**: tentacles phase = t*0.06 + i*1.05, reach sin(phase)*0.55, tips lag 0.6 rad behind, maw opens abs(sin)*0.45.
- **VoidWhaleColossal**: 12 segments, phase = t*0.08 + i*0.5, yRot sin*0.22, tail = sin(t*0.08+segments*0.5)*0.35, fins zRot sin*0.3.
- **ColossalOctopus**: 8 tentacles radial angle i/8*2PI, phase t*0.07+i*0.785, yRot = radial + sin*0.15, xRot reach + cos*0.2.
- **Jokest**: bounce = abs(sin(t*0.18))*0.35, head.y = -bounce*4, body.y = -bounce*2, legs sin*0.4 - bounce.
- **Drifter**: lopes low, head down 0.20, body 0.13+sin*0.04, rags lag sin*0.22.
- **Keeper**: slow, rightArm 0.75+sin*0.06, brim yRot sin*0.012*0.22, lantern = -rightArm + sin*0.10 lag.

## Textures - Where They Live

- Main jar: `jar-overrides/assets/mcsm/textures/entity/` (massg, creator, voidwalker, void_lurker, void_whale, whale_monster, drifter, keeper, colossal_octopus, jokest_creature, void_dweller, dweller)
- Sift pack: `jar-overrides/assets/mcsm_sift/textures/entity/` (same set, sift variants, merged into main jar)
- Both namespaces ensure `mcsm:void_whale` and `mcsm_sift:colossal_octopus` find textures without extra resourcepack.

## V2 Changes (This Branch)

- Added 3 new models: `ColossalOctopusModel` (8 tentacles), `JokestCreatureModel` (bounce+grin), `VoidWhaleColossalModel` (12 segments)
- Added 3 new layers: OCTOPUS_LAYER, JOKEST_LAYER, VOID_WHALE_LAYER
- Added 3 new renderers: OctopusRenderer, JokestRenderer, VoidWhaleColossalRenderer (Sodium-safe SubmitNodeCollector/GlowRenderTypes)
- Updated phase gate to check >=12 renderers, >=9 layers, guards for new models
- Fixed FlyingMob->PathfinderMob for 1.21 compatibility
- Textures already existed from `ci/make_mcsm_textures.py`, now properly wired

All images in this folder are AI concept art for what each mob SHOULD look like, matching their model descriptions.
