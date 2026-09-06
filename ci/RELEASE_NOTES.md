# Devouring Storms 1.9.143 — mega-phase 7: structures land whole, Sky City goes up

## The segment bug, root-caused and fixed
The base mod builds every schematic through a static queue with a 24,000
blocks/tick budget: towns visibly rise slice by slice (the "spawning in
segments" report), and because the queue is static it survives world
loads - leftovers from the previous world keep placing into the new one
(the "Sky City fragments scattered all over the world" report). Now:
- the queue is cleared whenever the level instance changes (no cross-world
  leftovers, ever);
- the placement budget is raised so each schematic completes in about one
  tick - structures appear whole.

## Sky City altitude
Sky City and its floating sibling sites (Speakeasy, Jungle Fortress,
Mushroom Island) are raised from y~296 to y~4200 - inside the 1000-10,000
order, above the 3500 cloud deck. Jumping off falls you through seven of
the story cloud decks on the way to the ground.

## The cloud sea below
Both sky shaders (core storylook AND the built-in Iris pack) now paint the
same layered decks mirrored into the lower hemisphere: from the ground it
reads as a far cloud sea past the terrain edge; from Sky City altitude it
is the layers streaming past as you fall. Decks seen from above show their
lit tops. Validated: storylook glslang-clean, 30/30 Iris translation units.

Unchanged: 6b warp portals, 6a particle field, 5c Telltale glare, welded
blob, purple face overlay, built-in shader pack DEFAULT ON.

Install: drop the jar in `mods/`. Existing worlds pick the new Sky City
altitude on fresh structure placement; already-placed blocks stay put.
