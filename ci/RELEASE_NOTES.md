# Devouring Storms 1.9.141 — mega-phase 6b: portals glow, and entry warps you through

The bowels mouth (the storm's portal sheet) now behaves like the story:

## Per-portal coloured light
- **Teal** glow + teal dust breathing at the mouth bottom while the plates
  are still closing (phase < 6.9).
- **Magenta** glow + magenta dust once the mouth is open.
- **Gold** glow at the return mouth inside the bowels hallway.
- The glow rides the bottom of the screen and strengthens as you close in,
  so the portal reads as a light source casting onto everything.

## Warp entry instead of a loading screen
- Touching the open mouth no longer snaps you across dimensions: the
  teleport is intercepted and a 1.7 s sequence plays - letterbox bars
  converge, a violet pull-grade washes the frame, your view is dragged
  toward the mouth, the portal travel sound builds, a white flash closes
  it - and only then does the server thread run the ORIGINAL teleport.
- Singleplayer always gets the sequence; a dedicated server without the
  mod keeps the instant teleport as the safe fallback.
- Everything is Throwable-guarded: no portal code, no registry, no server
  - the effect degrades to nothing instead of crashing.

Unchanged: 1.9.140 particle field, 1.9.139 Telltale glare, welded blob,
purple face overlay, built-in Iris pack with the DEFAULT-ON toggle.

Install: drop the jar in `mods/`.
