# Dabicco's Wither Storm — Ultimate MCSM Build

Your mod, with the trailer's phase halos and turquoise teeth merged in. **The jar is compiled and ready to play.**

**Target:** Minecraft **26.2** · Fabric loader 0.19.3 · Java 25 · built with Gradle 9.5.1 / Loom 1.17.20

---

## 🎮 Install (start here)

**`dist/dabywitherstormmod-1.9.60-26.2-beta.jar`** → drop into `.minecraft/mods/`

That's it. Needs Fabric API. 370 classes, 135 MB, built from your `arena/01a05ccd-lowuuuuuu` branch with my changes on top.

```
BUILD SUCCESSFUL in 19m 16s
```

### Optional extras (for 1.20.1 + Cracker's mod, *not* for the 26.2 jar)
| File | Where |
|---|---|
| `UltimateMCSM-Schematics.zip` | `config/worldedit/schematics/` — 181 MCSM builds |
| `UltimateMCSM-ResourcePack.zip` | `resourcepacks/` |
| `UltimateMCSM-Datapack.zip` | `<world>/datapacks/` — 9-page guidebook |

---

## ✨ What I added to your mod

### Turquoise glowing teeth
All three heads now burn turquoise at the mouth. This took a few passes to get right — a naive colour scan lit up the eyes and the ribcage too. The final version finds each head's face region, splits the pale pixels into horizontal bands, and takes only the **bottom-most 1–2 rows** — because on a Wither skull the eyes are always above the mouth. Result: **lavender eyes preserved, teeth glowing**, on both `wither_storm.png` and `wither_storm_og.png`, written to your existing `_e` emissive convention.

### `UltimateHalos.java` — the trailer's phase progression
A new client renderer registered next to `StormPresenceFX`, using the same `LevelRenderEvents.COLLECT_SUBMITS` hook, `ClientDistantStormManager` storm list, `GlowRenderTypes` and camera-facing `quad()` construction as your existing code.

| Phase | Look |
|---|---|
| **4** | omissive **white** glow hugging the sides |
| **5** | **black blur** bruised with **purple** |
| **5.1** | a **blue** aura ignites, and persists |
| **5.5** | a **purple** aura wraps **around** the blue, growing with the storm |

Layers cross-fade on smooth ramps so phases hand over automatically instead of popping, and they're submitted back-to-front so bright cores sit over dark blur. Five procedurally-generated 512×512 textures ship in `textures/misc/`.

This *complements* rather than replaces your `StormPresenceFX` — that still owns the atmospheric pulse, the 5.8+ cataclysm halo pair, and the black glare ring.

### 12 new config options
Registered in your existing `KEYS` map, so they inherit save/load, clamping, `DEFAULTS` and preset handling for free. **Client options: 159 → 171.**

New rows appear under **Skybox & Atmosphere** in two sections:
- **Trailer Phase Halos** — master toggle, per-phase toggles (4 / 5 / 5.1 / 5.5), grow-with-storm, brightness, breathing speed, rotation, bloom
- **Turquoise Teeth** — toggle + intensity

Sub-options grey out when their master is off, matching your existing UI convention.

---

## 🔧 Verification I ran

- ✅ Real Gradle build — **BUILD SUCCESSFUL**, jar produced
- ✅ `UltimateHalos.class` + all 5 halo textures + both `_e` emissive maps confirmed inside the jar
- ✅ Zero GUI rows referencing undefined config keys
- ✅ Zero config fields referenced by `UltimateHalos` that aren't declared
- ✅ Teeth output visually inspected across 5 iterations until eyes/ribcage were excluded

---

## 📂 Also in this workspace

- **`schematics/`** — 181 official MCSM builds converted from the 1.5 GB zip in your `stuff` repo (Beacon Town, Order Temple, Sky City, Nether train, MC101→MC205). These are **legacy MCEdit format**, far larger than vanilla's 48³ structure-block limit (the biggest is 531×106×583), so they ship as WorldEdit/Litematica schematics rather than baked-in worldgen.
- **`resourcepack/`** — the 1.20.1 merge (Tainted's 37 CEM models, OG textures, TAW Plus blocks/items, shaders, custom panorama, purple/pink skybox). Built before you shared the repo, so it targets **Cracker's mod on 1.20.1**, not your 26.2 jar. Kept because the panorama and skybox art are reusable.
- **`tools/`** — every generator, re-runnable: `build_visuals.py` (halos/panorama/skybox), `daby_teeth_glow.py` (teeth), `schem_to_nbt.py`, `build_config.py`.

---

## ⚠️ Two honest notes

1. **The 181 schematics are not yet auto-placed in worldgen.** They're converted and bundled, but wiring them into your `structures/` package needs a chunk-generator pass — say the word and I'll do it.
2. **I couldn't run the game.** No GPU or Minecraft client here, so the halos are verified as *compiled and packaged*, not *visually confirmed in-world*. Load it up and tell me if the brightness or fade windows need tuning — those are one-line changes in `UltimateHalos.layersFor`.

---

## 🙏 Credits
- **Dabicco** — Lead Programmer · **Joeyready** — Textures, Blocks, Items & Build
- **Tainted (De4dTainted)**, **DECAYED TEAM**, **VillagerN4** — TAW models (resource pack only)
- **nonamecrackers2 & Nazaru** — Cracker's Wither Storm Mod (resource pack only)
- **Telltale Games** — Minecraft: Story Mode
