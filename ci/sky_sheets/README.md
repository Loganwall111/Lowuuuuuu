# ci/sky_sheets — which sky images are the authority, and which are history

## The authority is the hex anchors, not these PNGs

The nine hexes supplied with build #416 (ceiling / middle / horizon per band) live
in `ci/palette_tables.py` as `SHEET_HEX`. They are ground truth:

    teal    #0C1216  #172228  #202E34     phase 5.00 - 5.10
    purple  #160A21  #3A184E  #5A2474     phase 5.50 - 5.90
    rose    #1D1519  #422D37  #644354     phase 6.00 - 7.00

`python3 ci/trace_sky_sheets.py --from-hex --check` proves the shipped tables
(`sky.fsh`, `position.fsh`, `McsmStormPhase.java`, the derived constants in
`mcsm_visuals.glsl`) are exactly the six-stop expansion of those anchors, and
`ci/build.sh` runs it on every build. `ci/make_backdrop_sheets.py` bakes the
twelve backdrop sheets from the same tables, so the background layer cannot drift
from the sky it hangs in.

## `superseded/` — the sheets the anchors replaced

The three PNGs in `superseded/` are the washed-out sheets older builds traced:
teal climbing to `#9EAC9F` at the horizon, purple to `#A85CB4`, rose to `#C89CA6`.
Tracing them confirms it — the horizon is bright sage / orchid / dusty pink, not
the deep teal / violet / plum the brief asks for:

| band | traced (zenith → horizon) | shipped now |
|---|---|---|
| teal | `#1E2C2D` → `#8D9687` | `#0C1216` → `#202E34` |
| purple | `#3D1642` → `#A64CB5` | `#160A21` → `#5A2474` |
| rose | `#564153` → `#BE929E` | `#1D1519` → `#644354` |

They are kept as the evidence for what changed, **not** as a reference to trace
from. Do not run `python3 ci/trace_sky_sheets.py --apply` against them: that
write would re-introduce exactly the bright trace the anchors replace. Their
recorded trace is pinned in `SUPERSEDED_TRACE` (in `ci/trace_sky_sheets.py`) so
`--verify` still fails if the folder is ever swapped for different images.

## The resolution of the trace (BUILD #476): 32 rows, not six

The columns the sky interpolates are read at `palette_tables.STOPS` rows -- **32**,
one stop per sampled row of the sheet -- and not the original six. Six stops could
only ever be the artist's three anchors and a straight line between them, which is
an approximation of a sheet; 32 is the sheet's own column at every stop, written
into `sky.fsh`, `position.fsh`, `McsmStormPhase.java` and `McsmBackdropPalette.java`
from the one table, and interpolated by row count so no consumer can stretch a
short column across the sky.

The same column is also shipped **as an image**: `ci/make_still_strips.py` writes a
one-pixel-wide, 32-row strip per band -- top row = zenith, bottom row = horizon --
to both

    jar-overrides/assets/mcsm/textures/sky/stills_<band>.png
    storylook/assets/minecraft/textures/environment/mcsm_stills_<band>.png

and `ci/build.sh` compares every pixel of those strips against the shipped tables
before the jar is packed. That is what makes "1:1 with the stills" checkable: if the
table, the shader and the strip ever disagree, the build fails rather than shipping
a sky that is only approximately the art.

## Dropping the CURRENT sheets in

If the actual current sheets are added to the repository, put them at the top
level of this directory (`ci/sky_sheets/phase 5 turquoise sky.png` and friends).
`find_sheets()` looks there first and ignores `superseded/`, and `--verify` then
compares the shipped tables against those images directly — a stricter check than
the anchor comparison, and the one that would catch an anchor typo.
