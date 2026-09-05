# Devouring Storms: Story Look

A resource pack that recreates the Minecraft Story Mode look from the
reference screenshots: pastel sky gradients, **stacked cloud decks with void
gaps between them**, soft lavender-tinted shadows, and a halo-wrapped sun.

## What each piece does

- `rendertype_sky` — replaces the flat sky box with a pastel gradient keyed
  off vanilla's own day/night colour (dawn pink-lavender, noon cyan, night
  deep blue), then draws **13 cloud decks** at rising heights: two adjacent
  pairs, void gaps between the groups, and a hard ceiling at ~16 000 blocks
  so the stack ends instead of going on forever. Decks accumulate
  front-to-back, so from the ground the lowest deck hides everything above
  it — exactly the "you cannot see the clouds way above" behaviour. Ridge
  noise inside the coverage noise puts clouds inside clouds in each layer.
- `rendertype_terrain` — story lighting: an ambient floor so shadows stay
  soft instead of crushing to black, a cool lavender tint in skylight
  shadow, a gentle saturation lift, and pastel distance haze.
- `rendertype_clouds` — the near vanilla cloud deck, brightened with a cool
  underside lift so it reads against the pastel sky.
- `textures/environment/sun.png` / `moon_phases.png` — square core with a
  wide soft halo (the glowing disc from the night screenshot).

## Honest limits

Core shaders cannot cast real sun shadows on the ground — the soft-shadow
look is a lighting curve, not shadow maps. The stacked decks live on the
sky dome, so they sit behind world geometry (you fly *under* them rather
than through volumetric banks). Both choices match the reference shots at
every angle we tested in shader-space; a true volumetric pass would need a
shader mod (Iris/OptiFine), which this pack deliberately does not require.

## Install

Drop the zip (or this folder) into `.minecraft/resourcepacks/` and enable
"Devouring Storms: Story Look". Works with or without the Devouring Storms
mod; the mod never forces it on you.
