# Minecraft: Story Mode Official Visuals & Packs (1.21.2 & 26.2)

Complete authentic visual recreation of **Minecraft: Story Mode** by Telltale Games and Mojang Studios, engineered for modern Minecraft **1.21.2** and **26.2** (Fabric / Iris / Sodium / OptiFine).

---

## 📦 Deliverables & 1-Click Direct Downloads

| Package | Direct Download Link | Target Directory | Description |
| :--- | :--- | :--- | :--- |
| **MCSM Shader Pack** | [MCSM_ShaderPack.zip](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a04054-lowuuuuuu/MCSM_ShaderPack.zip) | `.minecraft/shaderpacks/` | Standalone atmosphere shaderpack: 2.5x chunky 3D extruded clouds, active Iris Shader Options, dynamic sky dome, colored lighting & shadows, and luminescent turquoise teeth bloom. |
| **MCSM Resource Pack** | [MCSM_ResourcePack.zip](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a04054-lowuuuuuu/MCSM_ResourcePack.zip) | `.minecraft/resourcepacks/` | Standalone authentic visual resourcepack: original author custom textures, 4-point time-of-day custom skyboxes, 8 Story Mode cloud sheets, 32-bit RGBA items, and sounds. |
| **Wither Storm Mod** | [dabywitherstormmod-1.9.60-26.2-beta.jar](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a04054-lowuuuuuu/dabywitherstormmod-1.9.60-26.2-beta.jar) | `.minecraft/mods/` | Fully functional mod JAR for 1.21.2 & 26.2 with boss-anchored Phase 5.1+ cataclysm halo, 32-bit RGBA item transparency masking, and split-range pack metadata. |

---

## 🛠️ Complete 4-Protocol Visual Pipeline Repair

### Protocol 1: Shader Mappings & Disappeared Skybox Restoration
* **Custom Time-of-Day Skyboxes Restored**: Reinstated authentic 1536×1024 author skyboxes in `assets/minecraft/optifine/sky/world0/` (`sky1.png` - `sky4.png`).
* **Complete 4-Point Fade Specifications**: Fixed OptiFine custom sky properties with all 4 required fade times (`startFadeIn`, `endFadeIn`, `startFadeOut`, `endFadeOut`) and `blend=alpha` for smooth cross-fading across Day, Noon, Sunset, and Twilight.
* **Active live game time uniform (`worldTime`) Sampling**: Both `gbuffers_skybasic.fsh` and `gbuffers_skytextured.fsh` actively sample `worldTime` (and vertex-interpolated `vLiveTime`).
* **Sodium Tick 0 Anti-Freeze Protection**: If Sodium / Iris locks `worldTime` at tick 0 or delays initialization, shaders dynamically fall back to computing live celestial time from `sunAngle` and `sunPosition` coordinates, preventing frozen dawn/night loops.
* **World Space Background Definitions**: `gbuffers_skybasic.vsh` unprojects view vectors using `mat3(gbufferModelViewInverse)` into true camera-relative world direction (`worldDir.y`), guaranteeing the sky dome stays fixed to the world horizon rather than pitching with player head movements.
* **Iris / OptiFine Shader Options**: Root and shader `shaders.properties` configure `clouds=fast`, `customTexture.cloudTex0` through `customTexture.cloudTex7` mapping to `shaders/textures/clouds/cloud0.png` .. `cloud7.png`, and define interactive menus for all options. Standalone `block.properties` ensures Iris immediately ungrays the "Shader Options..." button.

### Protocol 2: Rebuilt Extruded 3D Story Mode Clouds
* **Explicit Texture Samplers**: `gbuffers_clouds.fsh` and `rendertype_clouds.fsh` declare all 8 texture samplers (`uniform sampler2D cloudTex0;` through `uniform sampler2D cloudTex7;`), eliminating empty texture register bugs and preventing fallback to flat unshaded geometry.
* **2.5x Chunky Mesh Extrusion**: `gbuffers_clouds.vsh` and `rendertype_clouds.vsh` unproject coordinates to world space and scale mesh geometry height by 2.5x (`worldPos.y *= 2.5`) for thick, boxy Minecraft: Story Mode cloud slabs.
* **Universal GPU Stability**: Unified `precision highp float; precision highp int;` headers declared identically across all vertex and fragment files to stop driver compiler errors.
* **Dynamic 3D Shading**: 3-tier directional lighting across top faces (warm sunlight highlight), side faces (ambient contrast), and bottom faces (soft lavender shadow tint).

### Protocol 3: Purged Corrupted Metadata Text Leaks
* **Clean Localization**: Cleaned `lang/en_us.lang`, `lang/en_US.lang`, `shaders/lang/en_us.lang`, and `shaders/lang/en_US.lang`.
* **Zero Leaked Markdown or URLs**: Deleted all leaked compilation outputs, download URLs, markdown `#` headers, and table syntax from UI and language files.
* **Standard Key Mappings**: Clean localization mappings for options menus, items, hotbars, blocks, subtitles, and death messages.

### Protocol 4: Held Item Transparency & Mod ZIP Schema
* **Transparency Masking Registered**: Cleaned and converted all item textures in `assets/dabywitherstormmod/textures/item/` (`grapple.png`, `command_circuit.png`, `super_tnt.png`, `formidibomb.png`, `rocket_retriever.png`, etc.) to 32-bit RGBA with zeroed-out transparent channels (`(0, 0, 0, 0)`).
* **Dual-Sampler Hand Shaders**: `gbuffers_hand.fsh` and `gbuffers_hand_water.fsh` declare both `texture` and `gtexture` samplers with explicit alpha discard (`if (col.a < 0.1) discard;`), preventing held items from rendering as solid black boxes over the viewport.
* **Modern Split Range Schema**: Declared in `pack.mcmeta` across resource pack, shader pack, and mod JAR:
  ```json
  {
    "pack": {
      "pack_format": 46,
      "supported_formats": {
        "min_format": 42,
        "max_format": 50
      },
      "description": "Minecraft: Story Mode Authentic Visual Pack"
    }
  }
  ```
* **Flat Archive Layout**: Zero nested wrapper folders in all packages.

---

## 🎮 Recommended In-Game Settings

1. **Video Settings -> Quality -> Custom Sky**: `ON`
2. **Video Settings -> Quality -> Sky / Sun & Moon**: `ON`
3. **Video Settings -> Shader Packs -> MCSM_ShaderPack -> Shader Options**:
   - **Story Mode Clouds**: `ON`
   - **Cloud Thickness**: `ON (2.5x Extrusion)`
   - **Dynamic Skybox**: `ON`
   - **Story Mode Lighting**: `ON`
   - **Wither Storm Teeth Glow**: `ON`
