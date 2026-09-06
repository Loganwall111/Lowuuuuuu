# Devouring Storms 1.9.139 — mega-phase 5c: the glare, rebuilt the way Telltale made it

The reference frames exposed the original construction of the Wither Storm
glare, so the old hard-ring glare is deleted and replaced with exactly what
the frames show.

## What the frames exposed

- The wide aura is a **plain soft gradient quad hung behind the silhouette**
  (purple at 5.5+, blue at phase 4-5, teal in the green phase) - not a bloom
  pass, not a ring hugging the body. Trees and buildings occlude it because
  terrain draws after the sky layer.
- Up close, the "glow" is **flat emissive squares**: a cyan-white inner-mouth
  square, a **U-arc of tiny white dashed teeth** (zigzagged), and one small
  **magenta cube** floating above each of the three beam mouths. Their
  softness comes from distance alone.

## What changed

- Old ring glare removed (texture, draw call and build step).
- New `storm_glare.png` (soft radial gradient) drawn first, behind the body,
  on the nearest storm; its scale still rides the **Glare Size** slider
  (default 0.58).
- New `storm_white.png` emissive primitive; the three mouths draw over the
  body: inner-mouth square + 7 dashed teeth + magenta cube each, from
  phase 4 up, fading with distance and hidden when too small to read.
- Everything else from 1.9.136-1.9.138 is unchanged: welded blob, purple
  face overlay 5.5+, built-in Iris shader pack with the DEFAULT-ON toggle.

Install: drop the jar in `mods/`. Panel → Glare Size adjusts the aura width;
the mouths need no configuration.
