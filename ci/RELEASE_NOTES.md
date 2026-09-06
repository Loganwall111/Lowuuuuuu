# Devouring Storms: The Point of No Return — 1.9.117

**This release fixes a launch crash introduced in 1.9.114.** If your game
dies at startup with `The specified mixin 'net.dabicco.witherstormmod.mixin.
McsmTownCommandPatch' was not found`, this is your fix: remove the old jar
from `mods/` and install this one.

- Mixin config merge now appends fully-qualified overlay class names. The
  old simple-name style resolved under the base mod's mixin package, which
  crashed at launch (town command patch) or silently applied stale 1.9.100
  classes for the other overlay mixins.
- New hard CI gate: every mixin config entry must resolve to a real class
  in the assembled jar, or the build refuses to publish.
- Includes everything from 1.9.116: Devouring Storms branding (title logo,
  panorama, mod icon) and the Story Look resource pack attached below.
