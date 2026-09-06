# Devouring Storms 1.9.137 — mega-phase 5b: the shader pack lives in the mod

The standing order: the Story Mode shader pack is merged INTO the mod with an
on/off toggle, DEFAULT ON, and the pack owns the clouds instead of reverting
them to vanilla. This build ships exactly that.

## What's new

- **Built-in Iris shader pack (v5)** — `DevouringStorms.zip` now rides inside
  the mod jar. On launch the mod extracts it into your `shaderpacks/` folder
  and selects it in Iris automatically (only if you haven't chosen another
  pack yourself — a pack you picked is never touched). Re-extracted whenever
  the mod updates.
- **Toggle, DEFAULT ON** — MCSM Control Panel → "Built-in Shader Pack". Off
  removes the pack and hands Iris back to (internal) on the next launch.
- **Round-5 Story Look sky inside the pack** — the same sky the mod paints
  without Iris: pastel EnderCon days, warm-violet dawn/dusk, indigo nights,
  layered cloud decks (layer → void gap → layer), and the storm-phase skies —
  pinkish-violet 5.5–5.9, blue horizon rim, purple vault line, darkened roof,
  storm dome shrunken to the sides so the halo hugs the storm's flanks.
- **The pack OWNS the clouds** — new `gbuffers_clouds` program keeps the
  story cloud lighting exactly as the mod renders it; enabling the pack no
  longer reverts clouds to vanilla.
- **Grading defaults ON** — bloom, ACES tonemap, storm purple grade,
  vignette, aurora and colourful lighting all ship enabled (were opt-in in
  v4).
- **Build gates** — every pack program is validated in every toggle
  combination (30 translation units, 0 failures) before the jar is built,
  and the jar audit fails the build if the embedded pack is missing.

## Also in this release line (1.9.136)

- Halo welded to the nearest storm at body scale; purple face overlay at
  5.5+; noon horizon band; teal sunrise-quad remap; sunset key gate;
  purple-lit storm decks.

Install: drop the jar in `mods/` like any other build. With Iris present the
pack installs and selects itself; without Iris the mod's own core-shader
Story Look keeps running as the fallback — the look ships either way.
