# Minecraft: Story Mode — Fixed Resource Pack & Shader Pack

This package delivers two standalone, fully-fixed, production-ready deliverables that authentically recreate the visual aesthetic of **Minecraft: Story Mode (MCSM)**:

1. 📦 **`MCSM_ResourcePack.zip`** (and folder `MCSM_ResourcePack/`)
2. 🔮 **`MCSM_ShaderPack.zip`** (and folder `MCSM_ShaderPack/`)

---

## 🌟 What Was Broken & How It Was Fixed

### 1. The Resource Pack (`MCSM_ResourcePack.zip`)

| Issue in Previous Version | Root Cause | Fix Applied |
|---|---|---|
| **Minecraft didn't recognize the pack** | Missing `pack.mcmeta` at root | Created modern `pack.mcmeta` supporting formats 15 through 60 (1.20 to 1.21.4+). |
| **Assets failed to load** | Namespaces (`minecraft/`, `witherstormmod/`, etc.) were sitting in the pack root instead of under `assets/` | Structured all assets inside standard `assets/<namespace>/` hierarchy. |
| **Game crash: Cyclic model reference** | `formidibomb.json` had `"parent": "formidibomb"`, and `command_block_book.json` had `"parent": "command_block_book"` | Replaced circular self-references with standard `"parent": "block/block"` and `"item/generated"`. |
| **Super TNT model conflict** | `super_tnt.json` declared `"parent": "minecraft:block/cube_bottom_top"` while specifying custom Blockbench elements | Changed parent to `"block/block"`, preserving the full 3D Blockbench model. |
| **Missing Sound Registrations** | `click_stereo.ogg` and `toast/in*.ogg` were in files but not defined in `sounds.json` | Wired `music.menu` (Title Theme), `ui.button.click` (MCSM UI clicks), and `ui.toast.in` (Toast notifications). |
| **Cloud Shader Glitches** | `rendertype_clouds.vsh` calculated `finalA = baseA * (0.8 - fade)`, producing negative alpha and black box artifacts | Clamped alpha to `[0.0, 1.0]`, normalized slab height, and restored Story Mode flat directional face shading. |
| **Cross-Mod Incompatibility** | Models/textures were only under `witherstormmod` | Added mirrors for `devouringstorms` and `dabywitherstormmod` so the pack works regardless of mod namespace. |
| **Custom Sky Support** | No custom sky config in previous pack | Added dual OptiFine custom sky (`optifine/sky/world0/`) and FabricSkyBoxes format (`fabricskyboxes/sky/`) using the authentic pink twilight plate. |
| **OptiFine Emissive Glow** | Glowing textures (`*_e.png`) were unconfigured | Added `optifine/emissive.properties` (`suffix.emissive=_e`) for glowing Command Blocks, Amulets, and Formidibombs. |

---

### 2. The Shader Pack (`MCSM_ShaderPack.zip`)

| Issue in Previous Version | Root Cause | Fix Applied |
|---|---|---|
| **No Pink Sky (Near-Black Sky)** | Old `gbuffers_skybasic.fsh` blended sky down to `(0.05, 0.02, 0.09)` (near pitch-black) | Implemented the authentic **Minecraft: Story Mode Pink Twilight sky gradient** calibrated directly to `sky_only_no_clouds.png`. |
| **Harsh Screen Distortion & VHS Lines** | `final.fsh` forced VHS tracking bands, tracking noise, and heavy chromatic aberration by default | Disabled intrusive glitching by default; added a clean cinematic color-grade with warm saturation and soft tone curves. |
| **Drab Grey Vanilla Distance Fog** | `composite.fsh` used generic grey fog | Implemented atmospheric **Rose-Coral Distance Fog** (`vec3(0.85, 0.52, 0.56)`), seamlessly dissolving terrain into the pink horizon. |
| **Static / Boring Sky** | Old shader had flat rifts | Added procedural roiling storm clouds underlit by the magenta/pink horizon glow, plus twinkling stars in the high indigo dome. |
| **Missing Skytexturing** | Celestial bodies lacked warm Story Mode bloom | Added `gbuffers_skytextured` to give the sun and moon warm golden radiance. |

---

## 🎨 Authentic Story Mode Sky Palette

The shader's dynamic sky dome accurately evaluates:

```
Zenith (Top):          #100930  RGB( 16,   9,  48)  Deep Midnight Indigo
Upper Sky:             #230f4f  RGB( 35,  15,  79)  Deep Nocturnal Purple
Mid-Upper:             #441c6a  RGB( 68,  28, 106)  Rich Violet Purple
Mid-Elevation:         #6a3175  RGB(106,  49, 117)  Royal Magenta Purple
Lower Sky:             #974a80  RGB(151,  74, 128)  Vibrant Story Mode Pink
Near Horizon:          #c5728e  RGB(197, 114, 142)  Warm Rose Pink
Horizon Band:          #ec9891  RGB(236, 152, 145)  Soft Coral Pink
Sunset / Low Horizon:  #fdc38c  RGB(253, 195, 140)  Luminous Peach Horizon Glow
Under-Horizon Base:    #0c0618  RGB( 12,   6,  24)  Smooth Dark Void Transition
```

---

## 📥 How to Install

### Step 1: Install the Resource Pack
1. Take `MCSM_ResourcePack.zip`.
2. Move or copy it into your Minecraft `.minecraft/resourcepacks/` folder.
3. In Minecraft: **Options → Resource Packs...** → move **MCSM_ResourcePack** to the top of the Selected list → **Done**.

### Step 2: Install the Shader Pack
1. Make sure you have **Iris + Sodium** (Fabric) or **OptiFine** installed.
2. Take `MCSM_ShaderPack.zip`.
3. Move or copy it into your `.minecraft/shaderpacks/` folder.
4. In Minecraft: **Options → Video Settings → Shader Packs...** → select **MCSM_ShaderPack** → **Apply**.

---

## 📂 Deliverable Files in Workspace

- `MCSM_ResourcePack.zip` (14.1 MB) — Ready-to-use Resource Pack archive
- `MCSM_ResourcePack/` — Uncompressed Resource Pack folder
- `MCSM_ShaderPack.zip` (7.4 KB) — Ready-to-use Shader Pack archive
- `MCSM_ShaderPack/` — Uncompressed Shader Pack folder
- `tools/build_mcsm_packs.py` — Automated build script to re-pack at any time
