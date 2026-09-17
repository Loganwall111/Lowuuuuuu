# 7000.0.0-M — the sky is the still, the storm owns it, and the switches work

**Build #483 — the infinite fall, the dweller, and the ghost whales.** The void has
no floor, it has a door. A fall that reaches the pocket above the barrier
(`y = -2029`) is carried back to **y = -251** — the top of Tier 1, the plan's own
number — with **its velocity, yaw, pitch, fall distance and momentum flag exactly as
they were**: no screen, no reload, no rubber-band. Every wrap bumps a **salt** the
void's generators read, so the second pass is not the first: the sponge's twist is
re-rolled and the rifts move. `void_loop` switches it. The fall tracker is told about
the seam, or a teleport *up* would read as a stop and end the dive.

**The dwellers talk.** `mcsm:void_dweller` — its own entity type, its own attributes,
its own renderer (the lurker's bulbous, tentacled mesh in the void's colours), no
walking goals at all: it glides, weightless, and it notices you. Get close and it says
one line of the deep's own lore; the line rides its synced data the way the cast's
talk gesture always has, so the HUD's overlay needs no packet of its own — it finds
the talking dweller, reads the line and sets it down **letter by letter** in an
amethyst-purple panel that fades in with the first letters and out with the last.
Its voice is the mod's own Ogg set, pitched: the beacon's keys for its clicks, the
whisper for its breath, the vast drone for the song underneath.

**And there are whales in the cavern.** The plan's colossal passive fauna are the
mod's own whale beasts — the class whose whale already swims rather than walks —
kinded, named, and set loose in the luminous tier where there is room for them, two
at most, drifting weightless with the drone that is already their voice.

**Build #482 — the sponge, the rifts and the gel's own fluid.** The void's second
tier is now a **real fractal you fall through**: `McsmVoidSponge` evaluates the Menger
sponge's own membership test per block (three levels, 3-block pores, the classic "two
1s in base 3 and the cell is gone"), eats a little of the solid away so it reads as
sponge, winds it with a twist re-rolled every 64 blocks of fall, and grows a moving
window of it — 24 blocks out, 24 down — around a player as they descend through the
band, on the void's own queue and budget. Behind them it stays: the way down is a
place, not an effect. Its walls are the plan's own gradient, in two new blocks painted
raw for it — **Orange Void Sponge** at the floor of the band, **Pink Void Sponge** at
its top — with the void's lamp set into the pores, and it is switched by `void_sponge`.

**The rifts** (`McsmVoidRifts`) are the plan's Layer 3–4 windows, and they are a
*function of position* rather than an entity: the same 160-block lattice and the same
hash on both sides, so nothing has to be synchronised and nothing can be late. Fall
into one and the frame becomes it — displacement ripples across the window, a parallax
swirl of star arrays, enormous silhouettes going past on the far side, a rim where the
void and the window disagree — while real bubble strings and star-fracture dust are
emitted around the boundary.

**And the gel's pools** are drawn as the plan asks: iridescent bands cycling between
neon teal, deep amethyst and toxic magenta, wave ribs above each surface, and a
full-bright white foam line where a body would cross it. Nothing in the deep places a
block, so the plan's "zero solid collision" is true by construction — and so is the
absence of drowning.

**Build #481 — the multi-layer void.** The void is no longer one place. The dimension
it lives in was already the tallest thing the mod owns; it is now the tallest thing
this engine can express at all — **min_y -2032, height 4064**, a 4064-block world —
and the fall under the gel's surface runs through **five tiers**, each with its own
colour, its own light, its own speed and its own thing to look at:

| tier | band (y) | what it is |
|---|---|---|
| 0 | 56 .. -250 | **the baseline**: empty space under the bedrock line, no filter at all |
| 1 | -251 .. -1100 | **the luminous cavern**: plum `#2E0B36` air and thin spires of cyan, emerald and amber light, with the bedrock ceiling overhead |
| 2 | -1101 .. -1250 | **the sponge**: an orange-to-pink wash of fractal pores, none of it straight |
| 3 | -1251 .. -1550 | **the abyss**: absolute light suppression, `#000000`, and rare ruins that still glow green because nothing here is lit for you |
| 4 | -1551 .. -1800 | **the fracture**: the view ripples — displacement bands and scanlines across the whole frame |
| 5 | -1801 .. -2032 | **the gel horizon**: the void's own fluid, greenish-brown `#1C1F16`, turning over as you sink into it |

Every boundary a fall crosses is named once, in chat, in the plan's own words. The
fog, the depth and the art all read the same table, so the world and the picture
cannot drift apart, and the **`Void Rudder`** (`mcsm:void_rudder`, neon-purple fin,
crafted from void shards, cord and a sigil, in the mod's own tab) is the tool that
steers it: it bites only in the void's air, multiplying the tier's own current by
1.6, or by 2.6 when engaged, with sneak as the brake and a glowing trail behind it.
Nothing in it touches damage — the fall is fast, never fatal.

The plan's own numbers (-64 .. -6001, a floor at -10000) are not expressible: this
engine will not place a dimension's `min_y` below -2032. The tiers are scaled into
the depth that exists, the plan's numbers are kept in the code as the record of what
was asked, and the mapping is a function rather than a comment.

**Build #480 — the cast, alive.** "custom NPCs that are able to move around, speak,
make noises and talk". The models shipped in #468 and the walking was always the game's
own goals; what was missing was the middle — a player could stand in the middle of
Beacon Town and the cast would keep staring at the horizon. A cast member now notices
a player within seventeen blocks and **walks over** (on its own navigation, so doors,
stairs and fences work), stops inside nine, **turns to them**, says a line in chat
under their own name from their own dialogue tree, and **speaks it out loud**: the
mouth runs for as long as the line takes and a voice lands syllable by syllable under
it — glass keys for the machine, whisper and giggle fragments for the ones who are not
people, and formant blips pitched to the character for everybody else. All of it from
this mod's own sound set; no vanilla cue is used for a voice. Idle chatter between
cast members is audible now too, and each character leaves a player alone for about
thirteen seconds after a line, so a town is a place with people in it rather than a
wall of text. Switched: `npc_dialogue`.

**Build #479 — the descent, and the deep.** The void is no longer somewhere you are
sent: it is what happens when somebody keeps going down. There is no item, no portal
and no command — the trigger is the world's own floor. Fall past it and the fall is
handed over to the void's air, where the whole drop is real physics, steerable, with
the four stops read off Y as the fall passes through them: **the rate** (the plunge
starts, 3-5 s), **the dark** (the light above goes out, the air stops being air, 10-15 s),
**the gel** (you cross into it, 3-5 s), **the deep** (20-30 s of sinking until the gel
sets you down on something it grew). Those seconds are the player's own, written
against blocks at Minecraft's terminal velocity, so the timings cannot drift.

**No loading screen.** The one frame the game insists on spending getting between
worlds is painted as the deep by the mod itself — no grey plate, no progress bar, no
"Building terrain". And the fall never kills you: the hand-over happens 56 blocks
above the world's own damage line, so the void's damage never sees you, and the deep
catches you seven blocks above the first surface instead of letting you hit it.

**The gel.** Everything the concept image asks for, drawn by the mod in its own HUD
hook — so it works with no shader pack, which is the standing rule for every visual
here: bubbles rising past the camera, bioluminescent specks in three colours, the
bright dark-pink bloom over your head, the violet weight of the gel underneath, and
gigantic soft silhouettes crossing the light above. The deeper you are, the more of
the frame the gel owns and the more of it is awake. And it holds you: **flight inside
the gel**, given back the moment you leave. The bedrock below reads as fog, graded by
depth from the surface's pink down to its own near-black violet.

**Build #477-478 — the black frame, found in a player's own log.** The log showed the
two reports were one bug: the client was not failing to *draw* the menu, it was failing
to *load* it. `EntityRenderers.createEntityRenderers` runs inside the resource reload,
and this mod's mob renderer threw in there — `HumanoidModel` reads a part called `hat`
in its constructor, `ModelPart.getChild` throws when that part is missing, and our
`hat` was an **empty** part, which a baker is free to drop. The reload aborted, the game
logged `removing all selected resourcepacks`, and a pack-less client is a black frame.
Now the hat carries a real (invisible) cube, every part lookup in this mod's bodies
returns its parent instead of throwing, a body is only built on a skeleton that has the
seven parts a humanoid reads — with vanilla's own player mesh as the last fallback —
construction itself is caught model by model, and every layer and renderer registration
stands alone with a named message. The same log's four asset bugs are repaired and are
build failures now: doors naming eight models that did not exist, double slabs with six
unresolved faces each, the crafting table's impossible `axis` property, and the
furnace / lit furnace / jack o'lantern textures that did not resolve.

**Build #478 — the next throw in the same queue.** Which renderer the game builds first
is hash order, so the whale being fixed did not make the reload safe: the cast's
renderer was still built the old way — `new Model(ctx.bakeLayer(LAYER))`, with part
lookups in its constructor — inside that same reload. The cast goes through the same
guarded factory as the mobs now, its layer keeps the definitions its own calls returned
instead of looking them up by name during the bake, and its registration reports a
failure instead of swallowing it.

**Build #476 — real skies, 1:1 with the stills.** The sky's four columns (the three
supplied sheets -- teal, purple, rose -- and the authored ember fall) are read at
**32 stops** now, not six. Six could only ever be the artist's three anchors and a
straight line between them; 32 is one stop per sampled row of the sheet, written from
one table into `sky.fsh`, `position.fsh`, `McsmStormPhase.java` (halo, horizon band,
canopy, hallucinations) and `McsmBackdropPalette.java`, and interpolated by row count
so nothing stretches a short column across the sky. The column also ships **as an
image** -- `stills_<band>.png`, one pixel wide, one row per stop -- in the jar and in
the built-in Story Look pack, beside the sun and moon it already carries, and the
build compares every pixel of those strips against the tables before packing. Drop the
real sheets into `ci/sky_sheets/` and `--apply` re-traces them at the same resolution:
the shipped sky then IS the still, row for row.

**Build #474 — the ceiling.** `McsmStormCanopy`: the storm's own sky, all 24 segments
of the bearing circle, horizon to a real cap at the zenith, drawn in the world (so it
works with no shader pack), colouring every vertex from the phase's own reference
tables. Nothing below phase 4.60, full by 5.10, released at 7.90-8.05, densest over the
mass (0.62) and thinner away (0.24) -- coverage, not a lid and not a fog.

**Build #475 — the hallucinations.** `Reality Glitches + Hallucinations` and
`Hallucination Intensity` had been switches on the settings screen with nothing behind
them since D.8. They now drive torn bands with chroma-split edges, a too-tall figure at
the edge of vision with violet lenses (painted, never spawned), a false sky that never
strikes, and the whispers -- only when MASSG is up, the player's void aging has moved,
or a storm is past its rose phase, and never in creative.

**Build #471-473 — the black frame.** The jar's replaced core shaders are now weighed
against the game's own copies every build; a program whose interface does not match is
disabled rather than shipped (that audit found the `core/position` pair sampling
unbound samplers, which renders black with no error). `/ds menu` reports who is painting
the menu: the mod's render guard, FancyMenu's layout and assets (Git LFS pointers read
as pointers, not content), and the pack, size and mean luminance of
`textures/gui/menu_background.png` and the title panorama.

---

# 7000.0.0-M — Build #416: the dome is gone, the sky actually fades, one traced palette everywhere

This build deletes the "sky with a top on it" and replaces it with a sky that is
painted as a continuous fade, then makes every other storm visual read its
colours from the same three reference sheets. Nothing in this build is a dome,
a cap, a card or a hard-edged oval.

## 1. The sky is a fade again, not a dome

- **New program: `core/sky.vsh` / `core/sky.fsh`.** The sky is drawn from the
  view ray, not from geometry: colour is a function of the ray's elevation, so
  there is no top to hit, no silhouette and no clipped edge -- look up, look
  down, fly through it, and it is still just a gradient. Six stop columns are
  sampled per pixel and cross-faded by phase, then folded into the live fog
  colour at the horizon so no seam can open between sky and ground.
- **The dark disc is off.** `McsmNativeSkyRenderer` no longer asks the platform
  for the dark cap (`shouldRenderDarkDisc = false`); it pins the sky colour to
  the phase's own horizon stop and suppresses the stars from phase 5.0 on. That
  flat disc the screenshots showed as a lid over the world is what this removes.
- **The horizon stops come from the sheets**: teal `"phase 5 turquoise sky"` for
  5.0-5.1, `"phase5sky0purple sky"` for 5.5-5.9, `"phase6sky 6 witherstorm"` for
  6.0-7.0, then the storyboard's ember tail to 8.05.

## 2. One traced palette, checked by a tool

- **New `ci/palette_tables.py`** traces the stop tables ONCE and hands them to
  everything: the sky program, the position program, the Java that colours the
  halo, and every baked texture. It also *verifies* them -- a drift of more than
  1e-4 between the GLSL, the Java and the storm-visual constants fails the build
  instead of reaching the player as a sky and a halo that disagree. It caught
  two real drifts the moment it was first run.
- **`mcsm_visuals.glsl`'s storm palette is now derived**, not hand-picked: the
  mass's core/mid/edge/bleed are samples of the traced columns (zenith x0.22,
  t=0.30, t=0.60, t=0.85) and the phase-6 horizon glow is the *orange* stop out
  of the ember trace. `ci/palette_tables.py` re-derives all eleven and fails the
  build if any hex stops matching its source.

## 2b. The trace is a tool, and the clouds come out of it too

- **`ci/trace_sky_sheets.py`** is the "extract it with a tool" half of the brief.
  Give it the three delivered sheets (drop them in `ci/sky_sheets/`, `uploads/`,
  or `/home/user/uploads/`, or pass `--dir`) and it samples each one down its
  centre band into the six-stop column the shaders use, rewrites **every**
  consumer in place -- `sky.fsh`, `position.fsh`, `McsmStormPhase.java`, and the
  derived constants in `mcsm_visuals.glsl` -- then re-runs the parity gate. It
  decodes PNGs itself (all five filter types, no Pillow) and `--self-test`
  round-trips a synthetic sheet through the whole path, so the trace maths is
  verified before it ever touches a real sheet. `--verify` re-traces and FAILS
  if the shipped tables disagree with checked-in sheets (it reports a skip, not
  a pass, when the sheets are absent).
- **The cloud deck is now part of the palette.** The deck used to be tinted with
  hand-picked hexes, which is why it never matched the sky. It is now four
  constants derived the same way as everything else -- the traced column's mid
  row lifted toward white by a per-phase amount -- so the pale puffs under the
  turquoise sky, the dark violet banks under the purple sky and the warm mauve
  under the rose sky all come off the same sheets the sky does, and cross-fade
  on exactly the same phase ramps.

## 3. Backdrops re-baked to those palettes

- The six sky-plane sheets are regenerated from the traced tables -- purple
  canvas, teal and salmon cloud filters, and the three phase decks. The
  phase-6 sheet now carries the **black-smudge mass**: a near-black core ringed
  with purple/magenta, then pink, then the salmon/orange fringe, dissolving into
  the sheet's own column colour so it has no border of its own. The mass outline
  is a noisy field (a broad lobe plus a quantized shred), which is what keeps it
  reading as soot rather than as a starburst.

## 4. The halo, rebuilt on the glowing-teeth shader

- `McsmHaloSkyRenderer` no longer builds a curved cap. It lays **40 camera-facing
  copies of the storm's own additive glow quad** (`core/storm_glow.fsh` -- the
  one shader whose falloff is per pixel) around the storm's head: a real ring of
  thrown light with per-pixel softness, a wider inner hole than the head so the
  mouth stays visible, and a world-space tether to the storm's tracked position,
  so the halo travels with the creature exactly like the backdrop sheets do.
- Its colour is `McsmStormPhase.columnFor(phase, t)` -- the same traced tables
  again, so a halo pixel and the sky behind it are the same colour by
  construction.
- **The teeth glow is phase-exact now.** `core/fogless_entity.fsh` reads the
  phase straight out of the `FogSkyEnd` carrier it already binds (no new
  uniform -- a slot the bind group lacks is a hard Vulkan crash) and picks the
  mouth band from it, keeping the vertex-colour hue only as a fallback. The two
  storm-bound shaders that cannot take that carrier carry an explicit
  `MCSM_PHASE_SOURCE:` annotation naming where their phase comes from.

## 4b. TEETH ARE WHITE; THE AURA CARRIES THE PHASE COLOUR

This is the correction that mattered most, and it is a real behaviour change:

| phase | teeth | aura |
|-------|-------|------|
| 4 | white | bluish `#8CCCFF` |
| 5 | pure white (brightest) | pure white `#FFFFFF` |
| 5.2 - 5.9 | glowing white | bluish `#80C7FF` |
| 6 | white | pure blue `#386BFF` |
| 7 | white | toxic green `#53D977` |
| 8 | pure white | blue `#5994FF` |

Every previous build painted the teeth THEMSELVES cyan / blue / green, which is
why the mouths never matched the reference frames. Now:

- `McsmTeethPhaseTint` carries **two** tracks: the TEETH track (white at every
  storm phase, fed to `eyeColorR/G/B` -> the model's emissive layers) and the
  AURA track, cross-faded on the user's own boundaries (white is reached exactly
  at 5.0 and holds to 5.15; the bluish aura is back by 5.25; blue at 6.0; green
  at 7.0; blue at 8.0).
- `mcsm_visuals.glsl` gained `mcsm_teeth_color()` / `mcsm_aura_color()`, and
  `mcsm_mouth_emissive()` splits them **per pixel by luminance**: a bright tooth
  pixel goes white, the dimmer emissive skirt around it takes the aura -- which
  is exactly what "white teeth with a bluish aura" looks like in the frames.
- `storm_glow.fsh` -- the additive pool drawn AROUND the mouths -- now reads the
  phase from the `FogSkyEnd` carrier (safe there: that pipeline is built from the
  same entity-emissive snippet as `fogless_entity`, which already binds the fog
  block, so the import adds no uniform) and paints the aura at 0.88 strength.
- `McsmStormBlob`'s mouth detail: the dashed teeth are hard white, the inner
  mouth square takes the aura, and the magenta emitter cube is untouched.
- The sky's horizon air is lit by the aura too, so phase 6 / 7 / 8 horizons read
  blue / green / blue.
## 4c. THE EYES KEEP THEIR VIOLET

Answering "is there a purple glow on the eyes too": **there is now, and there
was not before this fix.** `McsmExactGlowTintMixin` overrides
`WitherStormHeadRenderer.eyeTint()` -- the tint the base mod derives from the
beam colour, i.e. the show's violet -- and it was pointing at the TEETH track.
The moment the teeth were corrected to white, the eye lenses and every eye
bloom layer went white with them and the violet glow disappeared.

- New EYE track in `McsmTeethPhaseTint`, separate from the teeth on purpose:
  violet `#9E6BFF` at 4, pale violet at 5, magenta `#D63DFF` from 5.2 to 5.9,
  bluish violet `#7A6BFF` at 6, magenta at 7, blue violet at 8. `eyeTintArgb()`
  now returns THAT, lifted to full brightness the way the base renderer lifts
  the beam colour.
- The magenta pupil / emitter cube is now single-sourced as
  `PUPIL_R/G/B` instead of being typed twice, and our own emitter draws the
  phase's eye glow around it.
- **The emissive snap no longer bleaches eyes.** `mcsm_mouth_emissive()` and
  `fogless_entity.fsh` now skip the teeth-band snap for emitters that are
  already saturated and mid-bright (`eyeLike`) -- that is the eye lens, and its
  violet is the whole point. Teeth are white/grey, so their saturation is
  ~zero and they are always caught by the snap. The 4.0x gain still applies to
  the eye: it is emitted light, only the hue snap is skipped.

`ci/check_phase_uniform.py` verifies all of this (40/40 now) -- including a
symbol check that every `McsmTeethPhaseTint.*` call site resolves to a real
declaration, which is exactly how it caught a missing `PUPIL_*` constant.

- `ci/check_phase_uniform.py` grew twelve checkpoints for this (the checkpoint count is 40/40 now):
  the aura ramp must exist in the include and in each storm-bound program, and
  **no shader may multiply the teeth by a phase colour** any more.

## 5. Wiring, so none of this is inert

- **`core/position.vsh` / `core/position.fsh` are authored files now.** The
  build used to fabricate them by copying `block.*`, so every change made to the
  position pass was silently discarded. The copy is gone; the pipeline fails if
  the authored pair is missing, and the jar audit now **fails the build if
  `position.*` is byte-identical to `block.*`** or if the sky branch
  (`MCSM_SKY_POSITION`) has been lost. The default path stays block-safe, so no
  platform can end up with world geometry painted as sky.
- **Two new hard gates.** `ci/check_phase_uniform.py` proves the phase feed:
  one canonical accessor, no phase uniform declared in any core shader (the
  crash guard), every storm-aware module resolving it, the Iris
  `witherstorm_Phase` uniform declared, and the Java side publishing it. 22/22.
  `ci/palette_tables.py` adds the palette parity gate above. Both run before
  javac.

## 6. Light-adaptive body shading, and the sky's true hexes (build #416, 7000.0.0-M)

### The supplied hexes are now the source of truth

The nine anchors supplied with this build (ceiling / middle / horizon per sheet)
are ingested in `ci/palette_tables.py` as `SHEET_HEX` and expanded to the six-stop
columns the shaders read:

| sheet | ceiling | middle | horizon | expansion |
|---|---|---|---|---|
| `phase 5 turquoise sky.png` | `#0C1216` | `#172228` | `#202E34` | `#0C1216 #10181D #151F24 #19242A #1C292F #202E34` |
| `phase5sky0purple sky.png` | `#160A21` | `#3A184E` | `#5A2474` | `#160A21 #241033 #331545 #401A56 #4D1F65 #5A2474` |
| `phase6sky 6 witherstorm.png` | `#1D1519` | `#422D37` | `#644354` | `#1D1519 #2C1F25 #3B2831 #49313D #563A48 #644354` |

`python3 ci/trace_sky_sheets.py --from-hex --apply` writes them into `sky.fsh`,
`position.fsh`, `McsmStormPhase.java` and the derived constants in
`mcsm_visuals.glsl`; the older trace drifted by **0.4984 / 0.3373 / 0.4212** and is
gone. The twelve backdrop sheets were re-baked from the new columns, and
`ci/REPORT_sky_columns_shipped.png` was regenerated (it had still been showing the
superseded bright sky).

### Two real defects fixed on the way

- **The whole body-shading block was dead code.** `MCSM_VOID_BODY` was defined by
  *no pipeline in the mod* — so the void-black creases, the structural band, the
  glint sheen and the new light-adaptive plates would never have run in game
  (only `glslcheck` ever passed that define). The body cutout pipelines now stamp
  it, and `shimcheck` grew three combinations for the paths that actually run
  (**93/93 -> 147/147**).
- **The table rewriter corrupted its own output.** Two earlier versions of
  `patch()` left a doubled `);` (a GLSL syntax error) and could eat a *second*
  array declaration while looking for the first one's terminator. It is now
  index-based, self-heals duplicated terminators, and refuses to write unless the
  declaration survives exactly once. `--from-hex --check` was also a fake gate —
  it printed a site count that could never fail; it now dry-runs every rewrite and
  exits non-zero on real drift, and `ci/build.sh` enforces it.

### The shading itself

Four tone keys, all read from the ingested atlas or supplied by the brief:

| role | hex | where |
|---|---|---|
| night / navy key | `#0A0E14` | ambient light low (night, cave) |
| twilight blue | `#0D1B2A` | cool light (overcast, blue hour) |
| ash gray | `#242A36` | warm light (day, sunset, lightning) |
| void | `#000000` | crevices, structural joints, deep AO — permanent |

The masks are computed from the **unlit albedo**, captured before
`lightMapColor` multiplies in: keyed off the lit colour, midnight would have
dragged every pixel under the crease floor and crushed the whole hull to void
black. The ambient term is the platform lightmap scaled by the world clock using
the *same sun maths as the sky shader* — the storm head is submitted full-bright,
so the lightmap alone would have read "noon" at midnight, and the hull could never
have taken its night key. Measured on lit faces: midnight `#0E1218` (torch-lit
`#0B0F14`), noon `#1F2733`, sunset `#1F2229`, neutral ambient the exact midpoint
of the two permitted daylight tones, and crevices `#000000` at noon, at midnight
and under torchlight alike. The glint sheen is now light-aware too: invisible on a
black midnight hull, visible where daylight or a flash has lifted the plates.

`python3 ci/make_report_sky_columns.py --check` keeps the report honest, and is a
build gate. `ci/sky_sheets/` is still not in the repository, so the image trace
gate still reports a skip rather than a pass — the anchor gate above carries the
ground truth until the PNGs are checked in.

### The uploaded sheets are the superseded set — checked, not assumed

`ci/sky_sheets/` was empty until the three sky PNGs landed in the repository root
(URL-encoded names, one commit). Tracing them was worth doing: they are the
**washed-out sheets these anchors replace**, not the new ones — teal climbs to
`#8D9687` at the horizon, purple to `#A64CB5`, rose to `#BE929E`, i.e. the exact
bright trace the brief rejects. So `--apply` against them would have undone the
ingestion; they were moved to `ci/sky_sheets/superseded/` with their measured
trace pinned in `SUPERSEDED_TRACE`, alongside a README that says which folder is
the authority. `ci/sky_sheets/superseded/` is now verified on every build ("the
superseded set is still the superseded set"), while the shipped tables are still
proven against the anchors — the gate reports what it checked instead of skipping
silently. When the real current sheets are added to the top level of
`ci/sky_sheets/`, `--verify` compares the shipped tables against those images
directly, which is stricter and will catch an anchor typo.

## 7. The white conic light column, cloud blobs, and the death finale (build #416, 7000.0.0-M)

### The halo is an independent, body-tethered light column

The white halo is now completely separate from the purple backdrop, as required.
The backdrop sheets stay flat 2D sky accents on the deep skybox layer; the white
atmosphere is its own render pass, tethered to the storm's own coordinates:

- **The flat bottom circle is gone.** The white under-halo oval (a horizontal disc
  pinned to the lower chassis layers, drawn only from phase 5.82 up, which read as
  a sticker and never followed the body) no longer draws, and neither does the old
  ring: no cap, no disc, no ring geometry remains in the pass.
- **The column.** `McsmHaloSkyRenderer` now builds a stack of 26 overlapped,
  camera-facing additive bands through the teeth/eye glow shader, so the falloff is
  computed per PIXEL and the column has no polygonal edge. It runs from inside the
  lowest core blocks up past the top tentacle attachments, and its base alpha is
  zero — the cone is open at the bottom by construction.
- **White-locked.** A new `MCSM_GLOW_WHITE` branch, `pipeline/storm_glow_white` and
  `GlowRenderTypes.glowWhite()` pin the pool to white. The aura around the teeth
  and eyes keeps carrying the phase colour; the atmosphere can never pick it up.
- **The growth weld.** Radius, height AND the cone's spread angle come from one
  shared size model (`McsmStormPhase.growth/bodyRadius/bodyHeight/scaleMultiplier/
  columnHalfAngleDeg`) instead of private per-pass curves. The multiplier reads the
  live entity state — the phase ramp plus the heads currently attached — so a
  severed head pulls the column in rather than leaving it inflated around air.
- **Soft cloud blobs.** `ci/make_cloud_blobs.py` re-bakes the six phase glare
  assets with a domain-warped silhouette and a long dissolve inside the texture
  (border alpha is exactly 0, so there is no rim left to see), with the palette
  ramp reversed so the light comes from the middle instead of the black-cored ring.
  At draw time each layer is five offset, rotated, anisotropically squashed lobes
  that roll slowly with the world clock — a rigid circular card cannot be read
  anywhere in the result.

### The death cinematic had never rendered — and now it does, cool-white

`mcsm_death` and its whole stack (direction warp, white cracks, implosion, supernova
rings, flash) sat in `mcsm_visuals.glsl` with **no caller**, exactly like the
void-body block in `fogless_entity.fsh` before it: the Java driver stamped the
1906..2906 carrier and nothing ever read it, so the finale was invisible in every
build. `core/sky.fsh` now reads the band, warps its sampling direction, and adds the
stack — and the palette is the **cool-white** finish: cracks `#E0F0FF`, the implosion
whitening to `#E6F2FF`, and the six expanding rings walking a cold blue-to-white ramp
instead of the old rainbow. The finale also holds the late-storm sky while the band
owns the phase carrier, so it cannot snap to a calm sky mid-flash.

`ci/check_phase_uniform.py` went **40 -> 60 checkpoints**, including the ones that keep
this honest: the death driver must stamp and release the band, the sky pass must call
every part of the stack, the finale must be cool-white, the no-dome shell must stay
inert (no geometry submission, zero strengths) — and a shader-include reachability
analysis that fails on any NEW unreachable helper.

### The "defs-only" question, answered with a call graph

The block below `mcsm_blob_color` is **not** dead: `core/rendertype_clouds.fsh` calls
`mcsm_mass_cover`, and the `mcsm_inf_*` helpers are its internals — 43 of the include's
60 functions are reachable from shipped shaders. The other 17 are dormant by design and
are now an explicit allowlist: the port's live entry point must stay wired, and a new
orphan fails the build instead of accumulating quietly.

### The white column's material had to move — and a guard so that class of mistake cannot recur

The first attempt at the conic column added `glowWhite()` next to `GlowRenderTypes`, in
the reference copy of the base mod's source. That copy is on **no compile classpath**:
CI compiles `mcsm-extras/java` only and assembles the rest of the jar from the FROZEN
base release asset (`dabywitherstormmod-1.9.100-26.2-beta-mcsm.jar`, pinned by sha256 in
`ci/build.sh`). javac therefore resolved `GlowRenderTypes` from the jar, found no
`glowWhite`, and the build stopped with `cannot find symbol` — a one-line definition in a
documentation tree that looked exactly like the real thing.

The white pipeline now lives in `net/dabicco/witherstormmod/client/McsmWhiteGlow.java`,
inside `mcsm-extras` — a class that is genuinely compiled and genuinely overwrites into
that package at assembly time. Same shader, same additive blend, same 4x gain as the aura
pool; only the `MCSM_GLOW_WHITE` define separates them, so the atmosphere is white while
the aura around the teeth and eyes keeps the phase colour.

Two guards came out of it:

- `ci/build.sh` now dumps the API of **every** base class mcsm-extras calls into (24
  classes, up from 6). `ci/api/mod.txt` is the frozen jar's own `javap` output, so it is
  the authority for what exists rather than a remembered signature.
- `ci/check_phase_uniform.py` gained the frozen-base-symbol guard: every method mcsm-extras
  calls on a base-jar class must be declared somewhere in that dump, or the build fails with
  the file and the symbol named. Fields, nested types and type names are deliberately not
  checked, classes javap cannot load are reported rather than failed, and classes we compile
  ourselves are excluded — so it stays precise and cannot false-fail.

Checkpoint count: 40 -> 76.

## 8. Phase 3 locked: the aura is welded, the blobs are organic, and the counters say so

The layer-separated aura engine is finished and, more importantly, *falsifiable* from
here on. `ci/check_phase_uniform.py` is at **94/94 checkpoints** (it was 40 before this
build) and the shim parity gate is at **165/165**; both run before javac, so a regression
stops the build instead of reaching a release.

- **The flat bottom circle is gone and stays gone.** The white under-halo disc that hung on
  the lower chassis layers no longer draws, and the column is built from overlapping
  camera-facing bands with a zero-alpha base, so it is open at the bottom by construction.
  A checkpoint fails the build if ring/disc/cap geometry reappears in the pass.
- **Radius, height and spread angle all come from one model.** The column reads
  `McsmStormPhase.bodyRadius / bodyHeight / scaleMultiplier / columnHalfAngleDeg` and feeds
  the multiplier the LIVE attached-head count, so a severed head pulls the light in rather
  than leaving it inflated around empty air. A checkpoint fails the build if the renderer
  grows a private body-radius curve again.
- **White is structural, not incidental.** The column draws through `McsmWhiteGlow`, a
  compiled class in `mcsm-extras` that builds the same shader with `MCSM_GLOW_WHITE`. The
  aura path beside it in `storm_glow.fsh` is verified untouched, so the teeth keep taking
  the phase colour while the atmosphere never can.
- **The glare sheets are cloud blobs.** Six phase layers, five drifting lobes each, baked
  from `ci/make_cloud_blobs.py` with a warped silhouette and a bright core, so no rigid
  circular card survives anywhere in the effect.
- **The frozen-base-symbol guard** (new) verifies all 51 base-jar calls mcsm-extras makes
  against `ci/api/mod.txt`, the javap dump of the base release asset — the class of mistake
  that cost this build a red run cannot recur.
- **The death cinematic finally renders**, cool-white, and the no-dome rule plus the
  shader-include reachability map are enforced checkpoints.
