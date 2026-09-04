================================================================
 MCSM WITHER STORM  --  1.9.97-26.2-beta-mcsm
 Multi-phase fix: visibility, shadows, glare blob, sky, rim
================================================================

DOWNLOAD (all live on :8765)
  dabywitherstormmod-1.9.97-26.2-beta-mcsm.jar   57,354,216 B
  MCSShaders-shaderpack.zip                          11,663 B
  MCSM_visuals.zip                                1,064,899 B
  MCSM_mod_changes.zip                               51,7xx B
  README.txt / sha256.txt

sha256 (jar):
7eed07ed61074c282d408bcd8cf4d1ce65d66bd9a18d34bcc4ebe0449de891d9

Five defects, five root causes. Every one measured from bytecode
or from your own screenshots -- no guessing this time.

----------------------------------------------------------------
PHASE 1 -- STORM INVISIBLE WITHOUT SHADERS / NO TEETH / NO GLOW
----------------------------------------------------------------
ShaderPackCompat.active() gates SIX systems, every call site
compiled as "ifne <skip>" -- when Iris is loaded the mod turns its
OWN effects off and expects the shader pack to draw them:

    StormSunGlow                 sun glow + ground shadowing
    StormShadowMap               the storm's cast shadow
    StormImpactLights            coloured impact lighting
    StormBloom                   halo / eye-glow bloom
    GlowRenderTypes.emitterMark  turquoise teeth + eye glow
    WitherStormHeadRenderer.shaderGlowGain()

Under the agreed "mod owns the look" architecture the pack no
longer draws any of that, so nothing was left on screen.

Separately FoglessRenderTypes picks the body path with
    useCustom = fogless() || reverseShading()
    useCustom ? bodyCutout(tex)  <-- renders nothing on 26.2
              : MobRenderer.getRenderType(...)  <-- correct

FIX  McsmShaderGatePatch      -> active() returns false
     McsmStormVisibilityPatch -> fogless()/reverseShading() false

ORDER MATTERS. fogless() is
    active && !legacyDistantRenderer && !ShaderPackCompat.active()
so neutralising active() ALONE would switch the broken bodyCutout
path ON. Both mixins are required together.

----------------------------------------------------------------
PHASE 2 -- NO GROUND SHADOWS, FLAT LIGHTING
----------------------------------------------------------------
mcsm_sun_true() computed its angle as (t01*2.0-0.5)*PI -- a
quarter-day phase shift:

    worldTime   my sun.y   correct
          0       -0.88     +0.02   sunrise
       6000       +0.02     +0.92   NOON
      12000       +0.92     +0.02   sunset
      18000       +0.02     -0.88   midnight

At noon the sun sat on the horizon, so every "sunDir.y > 0.05"
gate failed and cloud shadows never ran at any time of day.

FIX  angle is now fract(t01)*2.0*PI. Shadow gate 0.05 -> 0.02.

----------------------------------------------------------------
PHASE 3 -- PURPLE RIM AT THE ZENITH
----------------------------------------------------------------
mcsm_apocalypse_bands() used four hard if/else colour steps. At
u=0.75 bright magenta (0.86,0.10,0.92) cut instantly to near-black
(0.05,0.00,0.12) -- a hard ring. At phase 7.0 that function blends
in at 75% strength, which is why your phase-7.0 shot showed it.

FIX  same four anchors, now joined with smoothstep.

----------------------------------------------------------------
PHASE 4 -- SKY DID NOT MATCH THE REFERENCES
----------------------------------------------------------------
Pixels were sampled from your Story Mode frames this time:

    stop      1.9.70 build        reference
    zenith    (0.60,0.51,0.79)    (0.15,0.10,0.18)
    upper     (0.74,0.64,1.00)    (0.20,0.14,0.23)
    mid       (0.71,0.62,0.96)    (0.24,0.16,0.27)
    horizon   (0.70,0.61,1.00)    (0.30,0.20,0.32)

~2.2x too bright with channels pinned at 1.00 -- saturation 1.34
was CLIPPING them, which destroyed the gradient and produced flat
glowing lavender. Hues were already correct: the reference/build
ratio was a consistent 0.41-0.52 at every stop.

FIX  every mcsm_storm_dome stop rescaled x0.46
     MCSM_SATURATION 1.34 -> 1.06, MCSM_CONTRAST 1.09 -> 1.04

    stop      after fix            reference
    zenith    (0.134,0.063,0.144)  (0.122,0.084,0.145)
    mid       (0.241,0.130,0.211)  (0.220,0.145,0.239)
    horizon   (0.368,0.215,0.307)  (0.320,0.198,0.324)
Max error 0.048 (~12/255), no channel clipping.

----------------------------------------------------------------
PHASE 5 -- THE MISSING BLACK GLARE BLOB
----------------------------------------------------------------
The storm direction could not survive the trip to the shader. The
mod's McsmFogCarrierMixin packs yaw+pitch into FogData.cloudEnd:

    cloudEnd = 1200 + (yaw+180)*2 + (pitch+90)*0.5

That is NOT invertible. The pitch term spans [0,90) while the yaw
term steps by 2, so 45 distinct (yaw,pitch) pairs collide on the
same value -- confirmed by brute force over the angle domain. The
decoder recovered garbage: pitch pinned to -90 (straight down) in
almost every case, yaw off by up to 34 degrees. mcsm_blob() was
drawing the glare BELOW THE WORLD. It was never missing -- it was
being aimed at the ground.

FIX  McsmBlobCarrierPatch re-stamps cloudEnd at TAIL with a
     strictly invertible integer packing:

         cloudEnd = 3000 + yawIdx*181 + pitchIdx
         yawIdx   = round(yaw)   + 180   in [0,360]
         pitchIdx = round(pitch) +  90   in [0,180]

     pitchIdx < 181 guarantees uniqueness. Max 68340, well inside
     float32's exact-integer range. Verified exhaustively:
     65,341 angle pairs, ZERO round-trip mismatches.

     1 degree resolution is far finer than the blob's angular
     radius, so quantisation is invisible.

ALSO FIXED: mcsm_clouds_end() and mcsm_rd_start() only guarded the
old 1100..2150 band. With the new carrier those would have leaked
a 3000..68340 value straight through as a cloud distance and
broken cloud fade. Both now guard the new band too.

----------------------------------------------------------------
PHASE 9 -- A SILENT MIXIN FAILURE I SHIPPED IN 1.9.72
----------------------------------------------------------------
Compiling clean does NOT mean a mixin applies. Mixin resolves its
target by name at LOAD time, long after javac is happy.

FogRenderer has TWO methods called updateBuffer:

    public  void updateBuffer(FogData)
    private void updateBuffer(ByteBuffer,int,Vector4f,F,F,F,F,F,F)

McsmBlobCarrierPatch declared    method = "updateBuffer"
which is AMBIGUOUS -- and it also carried require = 0, meaning
mixin would have skipped it SILENTLY. No crash, no log line, no
glare blob, and no way to tell that from the blob maths being
wrong. The phase-5 fix would have looked like it failed.

FIX  full descriptor, matching how the mod's own McsmFogCarrierMixin
     targets the same method:
         method = "updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V"
     and require = 1, so a future mismatch is a HARD startup
     failure instead of a silently missing feature.

Audited every other injection point for the same defect:
    active           1 overload   unambiguous  OK
    fogless          1 overload   unambiguous  OK
    reverseShading   1 overload   unambiguous  OK
    initialize       1 overload   unambiguous  OK
    beginTraveling   1 overload   unambiguous  OK
    tick             1 overload   unambiguous  OK
McsmBlobCarrierPatch was the only broken one.

----------------------------------------------------------------
PHASE 10 -- STATIC INJECTION SIMULATION
----------------------------------------------------------------
Resolved all four injection points against the real bytecode the
way mixin will at runtime:

    McsmShaderGatePatch       active          -> ()Z            MATCH
    McsmStormVisibilityPatch  fogless         -> ()Z            MATCH
    McsmStormVisibilityPatch  reverseShading  -> ()Z            MATCH
    McsmBlobCarrierPatch      updateBuffer    -> (LFogData;)V   MATCH

Also confirmed @At("TAIL") is valid: updateBuffer(FogData) has
exactly one RETURN opcode (offset 83), so the tail injection point
is unambiguous.

And confirmed the carrier ordering: the mod writes cloudEnd at
HEAD, we rewrite it at TAIL, so our invertible encoding is the one
that reaches the shader.

----------------------------------------------------------------
PHASE 11 -- THE ACTUAL REASON THE GLARE BLOB NEVER APPEARED
----------------------------------------------------------------
Phases 5 and 9 fixed two real bugs on the blob path, but neither
was the whole story. A whole-jar bytecode scan for callers of

    StormSkyGradient.update(Vec3)

returns NOTHING. It is dead code. That method is the ONLY writer
of yawDeg, pitchDeg, phase and active. Three classes read those:

    StormSkyGradientMixin   yaw(), color(), fogStampActive()
    McsmFogCarrierMixin     yaw(), pitch(), phase(), fogStampActive()
    McsmBlobCarrierPatch    yaw(), pitch(), phase(), fogStampActive()

...and nobody ever populates them. "active" therefore stays false
for the whole session, fogStampActive() returns false, and BOTH
carriers return at their first guard. mcsm_boss_dir() gets nothing,
returns w=0, and mcsm_blob() is never invoked.

So the chain was broken in three places at once:
    1.9.72  the encoding was mathematically non-invertible
    1.9.73  the injector was ambiguous and set require=0 (silent)
    1.9.74  nothing ever produced a value to encode

FIX  McsmGradientTickPatch drives update() once per frame from
     LevelRenderer.render at HEAD, using CameraRenderState.pos --
     the camera position in world space, exactly what update()
     expects. It runs before the fog carriers later in the frame,
     so the values are fresh.

     update() is cheap and self-contained: one pass over
     ClientDistantStormManager.all() plus two atan2 calls.
     Wrapped in try/catch so a visual helper can never kill a frame.

VERIFIED  LevelRenderer.render has exactly ONE overload; the full
          descriptor matches it exactly; require = 1 so a future
          mapping change is a hard failure, not a silent skip.

----------------------------------------------------------------
PHASE 12 -- ORPHAN AUDIT OF EVERY VISUAL SYSTEM
----------------------------------------------------------------
Phase 11 found StormSkyGradient.update() had no callers. That
raised an obvious question: is anything else orphaned? A full
invocation index was built over all 397 classes (169 distinct
call targets) and every entry point checked:

    StormSkyGradient.update        1   (McsmGradientTickPatch - OURS)
    StormSkyDome.update            1   (StoryModeSkyDomeMixin)
    StormSunGlow.render            1   (LevelRendererBloomMixin)
    StormImpactLights.render       1   (LevelRendererBloomMixin)
    StormBloom.process             1   (LevelRendererBloomMixin)
    StormBloom.beginFrame          1
    StormShadowMap.beginFrame      1   (StormBloom)
    StormShadowMap.build           1   (StormShadow)
    StormShadowMap.captureTerrain  1   (StormShadow)
    StormShadowMap.capture         8
    GlowRenderTypes.emitterMark    5
    GlowRenderTypes.glow           6

CORRECTION TO PHASE 11: StormSkyGradient.update now shows ONE
caller -- and that caller is McsmGradientTickPatch, the class added
in 1.9.74. Before that patch the count was genuinely zero, so the
phase 11 diagnosis holds. Everything ELSE was already wired
correctly. The gradient was the only orphan.

(A first attempt at this census reported 0 callers for everything,
including known-good ones. That was a broken javap @argfile
invocation, not a finding. It was caught by running a control case
whose answer was already known -- worth repeating whenever a scan
returns a suspiciously uniform result.)

Also verified no mixin collision: the mod injects
dabyws$bloomAtLevelEnd at RETURN of LevelRenderer.render while we
inject mcsm$driveStormGradient at HEAD of the same method.
Different injection points, different handler names, and our
HEAD call runs before their RETURN work in the same frame.
All handler names across the jar are unique.

----------------------------------------------------------------
PHASE 13 -- THE SHADOW CHAIN, END TO END
----------------------------------------------------------------
"No shadows" is the longest-standing complaint, so the whole chain
was traced rather than assumed.

StormShadow.render() opens with a guard that skips its entire
474-instruction body. The mod's own diagnostic string names the
cause outright:

    "off: disabled in Effects, strength 0, A SHADER PACK IS
     ACTIVE, or an earlier error switched it off"

StormShadowMap.wanted() requires ALL of:

    1. !failed                                      (runtime)
    2. stormShadow || stormSelfShadow    default ON  OK
    3. stormShadowStrength > 0           default 0.55 OK
    4. !ShaderPackCompat.active()        <-- THE BLOCKER

Condition 4 is exactly what McsmShaderGatePatch forces false, so
with Iris running the shadow map is now permitted to build where
previously it was hard-off.

Config defaults read straight from the bytecode initialiser:
    stormShadow          true
    stormShadowStrength  0.55
    sunGlow              true
    sunGlowStrength      2.2

All four conditions are now satisfiable. If shadows still do not
appear, condition 1 (failed) is the remaining suspect, and
StormShadowMap.status(String) will have logged the reason -- that
string is the single most useful thing to send me next.

----------------------------------------------------------------
PHASE 14 -- LOGGING, SO THE GAME CAN ANSWER INSTEAD OF ME GUESSING
----------------------------------------------------------------
Every defect chased so far shared one investigative problem: from
outside the game, a mixin that silently failed to apply and a
mixin that applied perfectly but whose feature is disabled
downstream look EXACTLY the same. Screenshots cannot tell them
apart. That is why the blob took three separate fixes.

Good news found while looking: the mod is already well
instrumented and prints to stdout, e.g.

    [dabywitherstormmod][shadow] {}
    [dabywitherstormmod] storm shadow map FAILED, shadows off: {}
    [dabywitherstormmod] storm shadow capture FAILED, shadows off: {}
    [dabywitherstormmod] sun glow DISABLED after an error: {}
    [dabywitherstormmod] bloom buffer OK: {}/{} lit pixels ...
    [dabywitherstormmod] no lit pixels over the centre {}x{} ...
    [dabywitherstormmod][perf] %s took %.1f ms

StormShadowMap.status() already dedupes and prints its reason
string. Nobody had been reading it.

What was MISSING was any signal from OUR patches. Added McsmDiag
(outside the mixin package, as 26.2 requires) printing under [mcsm]:

    [mcsm] MCSM extras 1.9.87 active. Patches: ...        (once)
    [mcsm] ShaderPackCompat.active() forced FALSE ...      (once)
    [mcsm] gradient ACTIVE phase=7.00 yaw=-43.0 pitch=12.5 (on change)
    [mcsm] blob carrier cloudEnd=27856 (yawIdx=137 pitchIdx=102)

All rate-limited to changes only, all wrapped so diagnostics can
never break a frame.

This turns the three indistinguishable failure modes -- patch did
not load / feature is gated off / feature drew nothing -- into
three different log lines.

SEE LOG_GUIDE.txt for exactly what to grep and what each line means.

----------------------------------------------------------------
PHASE 15 -- THE BLOB WAS A BRIGHT RING, NOT A BLACK MASS
----------------------------------------------------------------
Phases 5, 9 and 11 fixed the DELIVERY of the storm direction to
the shader. This phase checked what the shader does once it
finally receives a valid one -- and found the blob would have
drawn wrong even with a perfect carrier.

Simulated mcsm_blob() at phase 7.0 against the rescaled dome:

    disc   composited RGB           vs dome
    0.00   (0.239,0.138,0.212)      +0.000
    0.35   (0.905,0.280,0.460)      +0.352   BRIGHTER
    0.50   (0.963,0.284,0.470)      +0.376   BRIGHTER  <- peak
    0.90   (0.490,0.138,0.226)      +0.088   BRIGHTER
    1.00   (0.236,0.067,0.107)      -0.060   barely dark

So it renders a bright red-pink RING with a hole that is only 6%
darker than the sky. The reference frames show a DOMINANT BLACK
MASS with a hot rim. Not the same thing at all.

Cause: the "black core bite" was a SUBTRACTION of
vec3(0.010, 0.006, 0.014) -- about 1.4% of a channel. That was
tuned against the pre-1.9.72 dome, which was 2.2x brighter. When
phase 4 rescaled every dome stop by x0.46 to match your
screenshots, the fixed subtraction was left stranded and became
visually meaningless.

FIX  the core is now MULTIPLICATIVE occlusion, so it scales with
     whatever the dome brightness is:

         occl = 0.93 * pow(disc, 1.5)
         emis = col * (rim * rim) * 0.85
         dome = dome * (1.0 - occl) + emis

     rim*rim tightens the highlight into a hot edge instead of a
     bloom smeared over the whole disc.

Simulated result:

    disc   composited RGB          luminance   vs dome
    0.00   (0.239,0.138,0.212)     0.165       +0.000
    0.50   (0.755,0.241,0.396)     0.362       +0.197  hot rim
    0.85   (0.219,0.076,0.123)     0.110       -0.055
    1.00   (0.017,0.010,0.015)     0.012       -0.153  BLACK CORE

Core is now ~14x darker than the surrounding sky, which is the
black glare body from the references, with the rim preserved.

Blob angular size was checked too and was never the problem:
at phase 7.0 the disc spans ~19 degrees of sky.

NOTE ON COUPLING: this bug was CAUSED by the phase 4 rescale. Any
future change to dome brightness must re-check anything that adds
or subtracts a fixed amount against it. Multiplicative terms are
safe; additive constants are not.

----------------------------------------------------------------
PHASE 16 -- HUNTING THE REST OF THE STRANDED CONSTANTS
----------------------------------------------------------------
Phase 15 found that the x0.46 dome rescale had stranded a fixed
SUBTRACTION. That is a class of bug, not a one-off, so every
additive term touching the dome was measured against the new
brightness (rescaled dome @7.0 = 0.165 luminance):

  LIGHTNING FLASH   dome += A*gate*(0.26+0.5h)*(0.82,0.66,1.0)
      full flash toward zenith added 0.546 luminance
      -> result 0.711, a 4.3x WHITE-OUT of a 0.165 sky
      Every strike buried the storm silhouette. STRANDED.

  SUN HALO          clear-sky path only -- never rescaled. OK.
  HORIZON GLOW      clear-sky path only -- never rescaled. OK.

FIX  lightning amplitude scaled x0.46 to match the dome.
     Peak is now 2.52x the sky instead of 4.3x: a visible strike
     that does not erase the scene.

----------------------------------------------------------------
PHASE 17 -- ANIMATION RATES WERE 20x TOO FAST
----------------------------------------------------------------
While confirming the phase-2 sun fix reaches the shadow code, the
units of mcsm_clock() turned out to be wrong.

GameTime in 26.2 is a normalised 0..1 day fraction. mcsm_clock()
returned gameTime01 * 24000 -- i.e. TICKS. But every consumer was
written in SECONDS, and their own comments say so:

    "one bright blink ... every ~4.3 s"     lightning
    "roar pulse"                            blob throb

Ticks advance at 20 per real second, so the actual behaviour was:

    lightning     floor(clock/4.3)   -> 4.65 STRIKES PER SECOND
    blob roar     sin(clock*3.0)     -> 60 rad/s = 9.5 Hz STROBE
    sky shimmer   sin(clock*0.55)    -> 11 rad/s flicker
    band flicker  fract(clock*0.10)  -> 2 Hz

That is not weather, it is a strobe light -- and a genuine
photosensitivity hazard.

FIX  one MC day = 24000 ticks = 1200 real seconds, so
     mcsm_clock() now returns gameTime01 * 1200.0 (seconds).

     lightning  -> one strike window every 4.3 s   (as documented)
     blob roar  -> 3 rad/s = 0.48 Hz slow throb    (as documented)

Also CONFIRMED here: the phase-2 sun fix is correct at the call
site. mcsm_sun_true(GameTime) is passed the raw 0..1 fraction and
now yields sun.y = +0.965 at noon, -0.962 at midnight, so the
cloud-shadow gate (sunDir.y > 0.02) opens through the whole day.

----------------------------------------------------------------
PHASE 18 -- REGRESSION CHECK ON MY OWN PHASE-17 FIX
----------------------------------------------------------------
Dividing the clock by 20 slowed EVERYTHING, not just the two
things that were too fast. Every consumer was re-measured:

    entity sway    7.00 -> 0.35 rad/s     ok
    entity pulse  18.00 -> 0.90 rad/s     ok
    band flicker   2.00 -> 0.10 cyc/s     ok
    sky shimmer   11.00 -> 0.55 rad/s     ok
    blob roar     60.00 -> 3.00 rad/s     ok (0.48 Hz throb)
    beam rings    40.00 -> 2.00 rad/s     ok
    spiral         1.60 -> 0.08 cyc/s     ok
    cloud drift    0.07 -> 0.0035 uv/s    checked physically

Cloud drift looked "frozen" on a naive Hz test, so it was checked
in world units instead: the noise uv scale is worldPos*0.0042, so
1 uv = 238 blocks, giving 0.92 blocks/s -- about 1.5x vanilla
cloud speed. Correct. BEFORE the phase-17 fix it was 18.3
blocks/s, 31x vanilla: the shadows were tearing across the ground.

No regression. The unit fix improved this too.

----------------------------------------------------------------
PHASE 19 -- CLOUD SHADOWS: ONE REAL BUG, ONE HONEST LIMIT
----------------------------------------------------------------
BUG FIXED -- shadows above the cloud deck.
    t = (192 - worldPos.y) / sunDir.y
    Above y=192 that goes NEGATIVE, which sampled the noise field
    mirrored and painted cloud shadows onto terrain that is ABOVE
    the clouds. Now guarded: dy <= 1.0 returns 1.0 (no shadow).
    Verified at y = 64 / 150 / 191 / 192 / 200 / 320.

LIMITATION STATED PLAINLY -- these shadows are NOT a per-cloud
match to the deck you see.
    The real clouds are vanilla geometry driven by the CloudInfo
    UBO (CloudColor, CloudOffset), and that uniform block is bound
    ONLY to the cloud shader. terrain.fsh cannot read it, so it is
    not possible for the ground shadow to be sampled from the same
    source as the cloud you are looking at.

    What you get instead is a plausible moving cloud-shaped
    occlusion drifting at 1.5x vanilla cloud speed, so it reads as
    the same weather without being pixel-locked to it.

    Making them truly match would need a Java-side mixin to
    re-bind CloudInfo (or mirror CloudOffset into a
    witherstorm_ uniform) for the terrain pass. That is a real
    option if the mismatch is visible in play -- say so and it is
    the next phase.

----------------------------------------------------------------
PHASE 20 -- AUDIT OF MY OWN GLOBAL active() OVERRIDE
----------------------------------------------------------------
McsmShaderGatePatch forces ShaderPackCompat.active() to false for
the WHOLE GAME. Polarity had only ever been verified on 3 of its
11 call sites. If any path needed true, that override broke it.

All 11 sites disassembled and classified:

  ifne (skip-when-shaders)  -> forcing false ENABLES the feature
      StormSunGlow.render            sun glow + ground shadowing
      StormShadowMap (wanted)        shadow map may build
      StormImpactLights.render       coloured impact lighting
      StormBloom.process             halo / eye-glow bloom
      StormBloom.wantsEntityTarget   bloom entity target
      WitherStormHeadRenderer        shaderGlowGain
      FoglessRenderTypes x3          (handled separately)

  ifeq (INVERTED - opposite behaviour)  <-- the risk
      GlowRenderTypes.emitterMark
          active TRUE  -> RenderTypes.eyes()  = vanilla plain glow
          active FALSE -> MARK_TYPES          = the mod's CUSTOM
                                                emitter mark
      StormDebris (stored to a local, combined with another flag)

RESULT: the inverted sites also want FALSE. emitterMark is where
the turquoise teeth live, and false is what selects the mod's own
mark type instead of the vanilla eyes fallback. The override is
correct at every one of the 11 sites, including the two that
branch the other way.

CONFIG PRECONDITIONS (read from <clinit>, last write wins):
    stormShadow          true      stormSelfShadow     true
    stormShadowStrength  0.55      sunGlow             true
    sunGlowStrength      2.2       bloomStrength       2.0
    bloomMaskToStorm     true      turquoiseTeeth      true
    cataclysmHalos       true      headEyeGlow         true
    blackGlare           true      stormBackdrop       true
    storyModeLighting    true      impactLight         true

All 14 enabled. Agrees with the independent phase-13 reading of
stormShadow / stormShadowStrength.

METHOD NOTE: the first two attempts at this table reported every
boolean as "0.5", then as "false". Both were parser bugs -- field
DECLARATIONS default to false and the real values are assigned
later in <clinit>, so only the LAST write counts. Caught by
cross-checking against a value already known from phase 13. A
scan that disagrees with a known-good fact is a broken scan, not
a discovery.

----------------------------------------------------------------
PHASE 21 -- CLOUD SHADOWS NOW MATCH THE REAL CLOUDS
----------------------------------------------------------------
Phase 19 called this impossible because the CloudInfo UBO is bound
only to the cloud shader. That conclusion was WRONG -- it stopped
at the UBO instead of reading CloudRenderer itself.

Vanilla 26.2 clouds are NOT procedural. CloudRenderer loads
textures/environment/clouds.png (256x256) and steps it on a fixed
grid. Constants read straight from its bytecode:

    CELL_SIZE_IN_BLOCKS = 12.0
    TICKS_PER_CELL      = 400
    BLOCKS_PER_SECOND   = 0.6        (12 / (400/20) = 0.6, cross-checks)
    scroll               +X only; Z is a fixed +3.96
    1 texel = 1 cell = 12 blocks, wrapping every 3072 blocks

The old shadow used smooth noise on an arbitrary 238-block scale
drifting on BOTH axes at 1.5x speed. It could never line up.

Now matched against vanilla:

    cell size     12.0 blocks     MATCH
    scroll speed  0.6 blocks/s    MATCH
    scroll axis   +X only         MATCH
    Z offset      +3.96           MATCH
    coverage      27.6%           MATCH (measured from clouds.png:
                                  18103 of 65536 texels opaque)

Only the per-cell occupancy is still procedural, because
clouds.png is not bound to the terrain pass -- Sampler0 there is
the block atlas, and adding a second sampler needs Java-side
pipeline work. So the shadows move with the real deck on the real
grid at the real speed; individual cells will not always agree.
That is a far smaller gap than before, and it is stated rather
than glossed.

BUG I INTRODUCED AND CAUGHT: the first version wrote hit.z on a
vec2, which broke 11 of 15 shader units (entity, clouds, sky,
terrain all failed to compile). The validator caught it
immediately -- this is exactly why every shader edit is compiled
before shipping. Fixed to hit.y; back to 15/15.

----------------------------------------------------------------
PHASE 22 -- CROSS-ARTIFACT CONFLICT AUDIT
----------------------------------------------------------------
Three artifacts ship assets. If the resource pack overrode the
jar's core shaders, every shader fix in phases 2-21 would be dead
on arrival and nothing would explain why. Checked properly:

  CORE SHADER OVERLAP (jar vs MCSM_visuals):  0 files
      jar ships 14 core shaders, visuals ships 0.
      The v8 removal of the 11 conflicting core shaders held.
      The mod owns the shaders, as agreed.

  ASSET OVERLAP (jar vs MCSM_visuals): 764 identical paths
      763 byte-identical
        1 differs -- pack.mcmeta (expected, it is the descriptor)
      Files in visuals that are NOT in the jar: 1 (README.md)

  => MCSM_visuals.zip is now fully REDUNDANT. Every texture,
     model, CEM and mcmeta it carries is already embedded in the
     jar, byte for byte. Installing it changes nothing.

  Keep it if you want the textures usable WITHOUT the mod (e.g.
  on a vanilla client). Otherwise the jar alone is sufficient.
  It is not harmful -- but do not let an OLD copy linger in
  resourcepacks/, because a stale pack WOULD shadow the jar.

SHADER PACK SCOPE (your rule: aurora + colourful lighting ONLY):
  gbuffers_skybasic  storyModeSky()/biomeTint() exist but are
                     behind SKY_STORY_MODE, which defaults to 0 in
                     the source AND false in shaders.properties.
                     The live path is: c = skyColor (Iris/the mod)
                     then c += aurora(). Sky NOT taken over.
  gbuffers_terrain   COLORED_LIGHT only.
  gbuffers_textured  passthrough (clouds untouched -- your rule).
  gbuffers_basic     passthrough.
  final              BLOOM 0, TONEMAP 0, VIBRANCE 1.00 (neutral).

  Verified: no sky, no clouds, no lightmap, no biome skies, no
  atmosphere are active. The constraint holds.

----------------------------------------------------------------
PHASE 24 -- MIXIN CONFIG COULD HAVE BEEN REFUSED AT LOAD
----------------------------------------------------------------
Both mixin configs are registered in fabric.mod.json and all 82
listed classes exist (73 theirs + 9 ours). But a real mismatch:

    dabywitherstormmod.mixins.json   compatibilityLevel JAVA_25
    mcsm_extras.mixins.json          compatibilityLevel UNSET

and our classes were compiled --release 21 (major version 65)
while the mod's are Java 25 (major version 69).

With no declared compatibilityLevel, Mixin falls back to a
conservative default. Injecting Java 21 handlers into Java 25
targets in that state is exactly the kind of thing that gets
refused at load -- and a refused config is SILENT unless you are
reading the log for it.

FIX  mcsm_extras.mixins.json now declares
         "compatibilityLevel": "JAVA_25"
         "minVersion": "0.8"
     and every class is recompiled --release 25, verified at
     major version 69, matching the mod's own classes exactly.

This is the same failure family as phase 9 (ambiguous descriptor,
require=0): the patch looks correct, compiles clean, and simply
never applies. Worth stating that the ONLY way to be sure it did
apply is the [mcsm] banner in the log.

----------------------------------------------------------------
PHASE 25 -- FIRST MEASUREMENT AGAINST A REAL RENDERED FRAME
----------------------------------------------------------------
Twelve screenshots dated 2026-09-03 were sitting in uploads/ and I
had not looked at them. They are actual rendered frames -- the one
input I had been saying I lacked for 24 phases. That was my error:
the evidence was already there.

WHAT THEY SHOW

  131242 (1:12 PM) -- the storm RENDERS. Body, tractor beams,
  ejecta, boss bar all present. So the phase-1 visibility fix
  works. But the sky is measurably wrong:

      metric                  reference   rendered
      B/R at zenith             0.92        1.72     too BLUE
      horizon/zenith luminance  2.89x       1.34x    too FLAT

  Average brightness was close, which is why earlier eyeball
  checks passed. The SHAPE and HUE were wrong.

  FIX  the 7.0 dome row is now the measured reference profile
       verbatim: zenith (0.130,0.076,0.120), mid (0.184,0.116,
       0.184), horizon (0.373,0.215,0.398).
       New B/R 0.92 / 1.07 and ratio 2.89x -- exact match.

  130558 (1:05 PM) -- boss bar present, but the sky is the plain
  VANILLA DAY sky and no storm dome at all.

  I nearly "fixed" this by darkening SKY_DAY. That would have
  VIOLATED your standing rule never to touch the 24 byte-matched
  reference gradients. Checked first: the screen values match the
  SKY_DAY table almost exactly, so the day sky is rendering
  correctly AS SPECIFIED. The table is not the bug.

  The real fault is that a storm was active and mcsm_phase() still
  returned 0, so the shader took the vanilla branch. mcsm_phase()
  reads fogSkyEnd in 1395..1855, which is written by the carrier
  that StormSkyGradient.update() gates -- and update() was dead
  code until phase 11. Both screenshots predate 1.9.74, so this
  specific frame should already be fixed. Worth re-testing.

LESSON: measure the frame, not the code. Average brightness hid a
2x error in gradient shape for four phases.

----------------------------------------------------------------
PHASE 26 -- ALL TWELVE FRAMES MEASURED. THE SKY IS INTERMITTENT.
----------------------------------------------------------------
Classified the sky in every 2026-09-03 screenshot:

    frame    R     G     B    B/R   verdict
    100455  0.208 0.345 0.631  3.04  VANILLA sky, no dome
    100047  0.607 0.523 0.842  1.39  VANILLA sky, no dome
    100514  0.281 0.461 0.608  2.17  VANILLA sky, no dome
    130558  0.545 0.529 0.769  1.41  VANILLA sky, no dome
    131150  0.472 0.322 0.425  0.90  storm dome, magenta CORRECT
    131242  0.122 0.073 0.211  1.74  storm dome, too blue (fixed 1.9.81)
    (six others too dark to classify)

Only 2 of 12 frames have a storm dome. Frame 100455 is the
decisive one: a storm at CLOSE range with tentacles, tractor beams
and debris all drawing -- under a plain vanilla blue sky. So the
entity path and the sky path are independent, and the entity path
already works.

ROOT CAUSE -- and it is NOT a bug.

StormSkyGradient.update() only selects a storm when
    phase >= 4.5   AND   distance <= 1400 blocks
Below phase 4.5 nothing sets active, the carrier never stamps
fogSkyEnd, mcsm_phase() returns 0, and the shader takes the
vanilla branch. That is CORRECT: the dome table starts at 4.5 and
turquoise is specified at 5.0. Widening the gate would break the
agreed storm-sky progression.

So frame 100455 is correct behaviour for a sub-4.5 storm.

THE REAL DEFECT was diagnostic, not visual: from a screenshot,
"phase is below threshold" and "the patch is broken" look
identical. That ambiguity cost several phases of work.

FIX  McsmDiag.skyReason() now states it outright:
     [mcsm] storm sky ON (phase 7.00)
     [mcsm] storm sky OFF -- phase 4.10 is below the 4.5 threshold.
            Vanilla sky here is CORRECT; the dome starts at 4.5
            and turquoise at 5.0.
     [mcsm] storm sky OFF -- no storm within 1400 blocks.

Verified the packet path is healthy on the way in:
WitherStormEntity sends WitherStormPositionPacket every 2 ticks on
the server, which drives ClientDistantStormManager.update(). The
only filter is the phase/distance test above.

----------------------------------------------------------------
PHASE 27 -- CLOUD SHADOWS TESTED AGAINST A REAL FRAME
----------------------------------------------------------------
Frame 100455 is an ideal test: full daylight, clouds overhead, a
large area of flat open sand. Three tests were run on the ground
luminance, and they DISAGREED -- which is the interesting part.

  test 1  naive min/max ratio    0.681 vs 0.70 predicted   POSITIVE
  test 2  dark-run length        1.40 cells, 30/35 isolated NEGATIVE
  test 3  histogram shape        unimodal, single peak 0.80 NEGATIVE

Test 1 was a FALSE POSITIVE. It compares the single darkest patch
to the single brightest -- precisely the statistic outliers
corrupt most. It would have let me report "cloud shadows working"
on a frame that has none.

Tests 2 and 3 look at STRUCTURE instead:
  - vanilla cloud cells are 12 blocks across, so a real shadow
    covers several adjacent samples. Isolated single dark cells
    are per-block texture noise.
  - shadows create TWO populations, lit and shadowed, so the
    histogram should be bimodal with modes ~0.70 apart. One hump
    with a left tail is one lit population with dark block edges.

VERDICT: no cloud shadows in frame 100455.

That is EXPECTED, not a failure. The frame is timestamped 10:04,
and the phase-2 sun-vector fix -- the thing that makes sunDir.y
positive at midday and therefore lets the shadow gate open at all
-- shipped later the same day. This frame cannot show them. It is
evidence from BEFORE the fix, not evidence against it.

DELIVERED: shadowtest.py, so this is repeatable rather than a
one-off argument.

    python3 shadowtest.py <screenshot.png>

Point it at a daylight frame with open flat ground. It filters out
tractor beams, water and foliage by hue, then requires BOTH
structural tests to pass before it will say shadows are present.

While building it I had to fix the tool twice: a coarse sampling
grid first said NO, a finer grid said YES, and the peak detector
was loose enough to find three "peaks" on one smooth hump. Both
corrected; it now agrees with itself at either resolution and
reports NO for the night control. A measurement tool gets the same
scrutiny as the code it measures.

----------------------------------------------------------------
PHASE 28 -- A CONTRADICTION THAT ALMOST REVERSED A GOOD FIX
----------------------------------------------------------------
Phase 26 classified two frames 92 seconds apart as:
    131150  B/R 0.90  "storm dome, magenta CORRECT"
    131242  B/R 1.74  "storm dome, too blue"
Same storm, same phase (boss bar 94% in both). They cannot both be
right, and if 131150 were right then the phase-25 retarget was
wrong and should be reverted.

Measured the frames properly instead of trusting the summary:

    131150 mid sky  R = 0.993, 90% OF PIXELS CLIPPED
    131242 mid sky  R = 0.127, 0% clipped

Phase 26's "correct magenta" was CLIPPING. R cannot exceed 1.0, so
on a blown-out patch B/R is dragged toward 1.0 regardless of the
true hue. The measurement was an artifact of saturation.

Re-measured rejecting every pixel with a channel >= 235:
    131150  (0.712,0.486,0.630)  B/R 0.89   G/R 0.68
    131242  (0.150,0.102,0.229)  B/R 1.52   G/R 0.68

G/R IDENTICAL in both, B/R wildly different -> a term added to R
and G but not B. The difference normalises to R:G:B =
1.00 : 0.68 : 0.71, which is a warm sun glow. 131150 looks AT the
sun; 131242 looks away.

So the dome hue is the one with no halo on it -- frame 131242,
B/R 1.52 against a reference 0.92. The dome WAS too blue and the
phase-25 retarget was correct. Not reverted.

NEW BUG FOUND -- the fourth stranded constant.
The measured halo contribution is luminance 0.423 sitting on a
dome of 0.091: a 4.7x blow-out, which is why 90% of that sky
clipped. Same trap as phase 15 (blob core bite) and phase 16
(lightning): a term calibrated against the old 2.2x-brighter dome
and left behind by the rescale.

FIX  mcsm_sun_halo scaled x0.46, matching the dome.
     Peak now ~2.4x the sky instead of 4.7x, and it no longer
     clips -- so the hue survives where the sun is.

LESSON: a clipped pixel carries no hue information. Any colour
measurement must reject saturated samples first, or it will
confidently report the wrong answer -- as it did here, and nearly
cost a correct fix.

----------------------------------------------------------------
PHASE 30 -- THE HALO NOW CHANGES COLOUR WITH THE PHASE
----------------------------------------------------------------
1.9.84 hard-coded the halo/bubble mass to teal. That is correct at
5.0 and WRONG everywhere else. Your spec:

    5.0          turquoise
    5.1 - 5.2    dark purple
    5.25 - 5.9   magenta / purple / pink / black
    6.0          orange, magenta, purple, pink, black and red
    7.0          same family as 6

Cross-checked against the reference frames (trimmed radial means
at r = 0.20 of screen height, measured from the storm centre):

    144558  turquoise ref  (0.018,0.038,0.040)  G-R +0.020  B-R +0.022
    072359  phase ~7       (0.025,0.013,0.045)  G-R -0.012  B-R +0.020
    073325  phase ~7       (0.035,0.036,0.086)  G-R +0.001  B-R +0.050
    145046  late/orange    (0.136,0.071,0.239)  G-R -0.066  B-R +0.103

Only the turquoise reference has G-R POSITIVE. Every late-phase
frame is negative -- red above green -- which is the signature of
purple/magenta. So green has to fall away right after 5.0 and
never come back, which is also your "5.3+ no turquoise" rule.

FIX  new mcsm_halo_color(p) with nine anchors:

    5.00  (0.026,0.082,0.088)  turquoise
    5.10  (0.038,0.020,0.062)  dark purple
    5.25  (0.075,0.022,0.090)  magenta-purple
    5.60  (0.105,0.028,0.108)  magenta / pink
    5.90  (0.120,0.032,0.118)  pink-magenta
    6.00  (0.140,0.052,0.110)  orange enters (R climbs, G lifts)
    6.50  (0.150,0.058,0.100)  orange + magenta + red
    7.00  (0.155,0.060,0.098)  same family as 6
    8.00  (0.150,0.040,0.070)  blood red drift

Verified progression:
    5.00 TURQUOISE   5.10 dark purple   5.20 purple
    5.30 magenta     5.90 magenta/pink  6.00 orange enters
    6.50 orange-red  7.00 orange-red    8.00 blood
Turquoise is present ONLY at 5.00-5.02 and is gone by 5.30.

----------------------------------------------------------------
PHASE 6 -- TRUNCATED DOWNLOADS (infrastructure, not the mod)
----------------------------------------------------------------
Downloads of the jar were arriving at ~53.5 MB instead of 56.8 MB
and failing to open as a zip. The server was NOT at fault: /tmp is
a 993 MB tmpfs and the build scratch (JDK 303M, deps 197M, spare
jars 165M) had filled it to 100%. Every "curl -o /tmp/..." wrote a
short file and reported success. Content-Length was correct the
whole time.

FIX  cleared the scratch (295 MB free), moved verification
     downloads off tmpfs, and confirmed a full-size, hash-exact,
     zip-valid transfer of all six files.

The delivered jar was never damaged -- on-disk sha256 stayed
7eed07ed61074c282d408bcd8cf4d1ce65d66bd9a18d34bcc4ebe0449de891d9
throughout.

----------------------------------------------------------------
PHASE 7 -- "BRING BACK ALL THE ORIGINAL STUFF" AUDIT
----------------------------------------------------------------
Confirmed nothing was lost across the rebuilds:

    textures (.png)      817
    models / json        561
    CEM (.jem/.jpm)       52
    classes              396
    sounds (.ogg)        109
    core shaders          14
    lang                   2

Every original mod class is still present and untouched --
StormSkyDome, StormSunGlow, StormShadowMap, StormBloom,
GlowRenderTypes, ShaderPackCompat, FoglessRenderTypes,
McsmFogCarrierMixin, StoryModeSkyDomeMixin, and the mod's own
dabywitherstormmod.mixins.json. Our patches OVERLAY behaviour at
runtime; they delete nothing.

----------------------------------------------------------------
PHASE 8 -- SERVER HARDENING
----------------------------------------------------------------
The preview server is now threaded (the stock single-threaded
http.server crashed on concurrent 56 MB transfers) and advertises
Accept-Ranges so an interrupted download can be resumed instead of
leaving a corrupt jar. Cache-Control: no-store prevents a stale
build being served after a rebuild.

----------------------------------------------------------------
VERIFICATION -- AND ITS LIMITS
----------------------------------------------------------------
  javac                     13/13 classes, no errors
  mixin target resolution   5/5 resolve uniquely (phases 10-11)
  @At(TAIL) validity        1 RETURN opcode, injectable
  core shader units         15/15 compiled
  carrier round-trip        65,341/65,341 exact
  jar integrity             OK, 2387 entries, 817 textures
  all three mixins embedded confirmed
  all six files over HTTP   full size + hash-exact + zip-valid
  original content audit    nothing lost (see phase 7)

READ THIS: apart from the carrier round-trip (real arithmetic on
real values), these are STATIC checks. They prove the code
compiles and the files are present. They do NOT prove anything
appears on screen. In 1.9.70 a 21/21 audit passed while four
defects were plainly visible. You are the only renderer here.

The colour work is measured against your images. The blob fix is
proven arithmetic. The Java gate fix is sound bytecode reasoning
that compiles clean -- but none of it has been run in a game.

----------------------------------------------------------------
INSTALL
----------------------------------------------------------------
  1. Remove ALL older dabywitherstormmod jars from mods/.
     Only this jar -- never alongside a plain upstream copy.
  2. Drop the 1.9.96 jar in mods/.
  3. MCSM_visuals.zip  -> resourcepacks/, enable, move to TOP.
  4. MCSShaders zip    -> shaderpacks/ (optional; aurora +
     colourful lighting only).
  5. The 1.9.70 bodyCutout workaround is NO LONGER NEEDED.
     Reverse Shading / Legacy Distant Renderer can be set to
     whatever you like -- the mixin overrides both.

Fabric 0.19.5 / fabric-api 0.159.0+26.2 / Sodium 0.9.1 /
Iris 1.11.2+mc26.2 / cloth-config 26.2.155. No OptiFine.

----------------------------------------------------------------
CHECK IN THIS ORDER
----------------------------------------------------------------
  a) Shaders OFF -- is the storm body visible?        (phase 1)
  b) Turquoise teeth and eye glow present?            (phase 1)
  c) Midday -- do clouds cast moving ground shadows?  (phase 2)
  d) Phase 7.0 -- is the zenith ring gone?            (phase 3)
  e) Phase 7.0 -- dark and moody, not glowing lavender? (phase 4)
  f) Is the black glare blob attached to the storm?
     (phases 5 + 9 + 11 -- three separate bugs on one path)

If (a)/(b) still fail, the gate is elsewhere -- send one shot with
shaders OFF. If (f) still fails, the mod may not be updating
StormSkyGradient.yaw()/pitch() at all, which is the next thing to
instrument. If (e) is too dark in ORDINARY DAYTIME play, that is
one constant and a five-minute change -- I tuned to your storm
references, which are dusk scenes.
