# DEVOURING STORMS — Shaderpack

Built-in horror presentation for **Java Edition** via [Iris](https://irisshaders.dev) (recommended)
or OptiFine. Atmosphere-first profile: no shadow pipeline, maximum dread.

## What it does

- **Two rifts in the sky** — fixed breaches burn violet on the horizon (`gbuffers_skybasic`)
- **Bruised decay grade** — greens poisoned, purples fed, midtones crushed (`final`)
- **Storm fog** — depth fog pulled toward storm-violet, thicker in rain (`composite`)
- **Reality tearing** — row-slice tears, RGB split, scanlines, film grain (`final`)
- **Storm-lit entities** — bright magenta texels on MASSG/decay textures glow and flicker
  (`gbuffers_entities`)
- **Lightning flashes** — composite flash on storm strikes

## Install

1. Install **Iris + Sodium** (Fabric) for your Minecraft version.
2. Zip this folder so that `shaders/` is at the zip root (it already is):

   ```
   cd shaders
   zip -r DevouringStormsShaderPack.zip DevouringStormsShaderPack
   ```

3. Drop the zip into your Minecraft `shaderpacks/` folder.
4. Video Settings → Shader Packs → **DevouringStormsShaderPack**.

Pairs with the Devouring Storms Fabric mod — but the pack stands alone for any world
that deserves worse weather.
