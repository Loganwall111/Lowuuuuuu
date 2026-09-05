# MCSM WITHER STORM — SESSION HANDOFF

**Paste this entire file as your first message to the new agent.**

---

## 0. WHO YOU ARE AND WHAT THIS IS

You are continuing a long-running engineering project: a patched Minecraft
**26.2** Fabric mod (`dabywitherstormmod`, "MCSM Wither Storm"), plus an Iris
shader pack and a visuals resource pack. The goal is to make the Wither Storm
and its sky/shadows/effects **render correctly and look identical to the
user's Minecraft: Story Mode reference screenshots.**

The work has run **28 phases**. The current shipped build is **1.9.87**.
Everything below is the accumulated state. Read all of it before acting.

---

## 1. USER'S ENVIRONMENT AND HARD CONSTRAINTS

**Never violate these. They are standing instructions from the user.**

- Minecraft **26.2**, Windows 11, Java 25 (Azul), RTX 5050 laptop.
- Fabric 0.19.5, fabric-api 0.159.0+26.2, Sodium 0.9.1, **Iris 1.11.2+mc26.2**,
  cloth-config 26.2.155, cameraoverhaul. **NO OptiFine.**
- Creative singleplayer, op.
- **Mods folder gets ONLY our jar** — never alongside a plain upstream copy.
- **Verify every artifact with sha256** after writing.
- **Never reuse retired version numbers.** Next version after 1.9.87 is **1.9.87**.
- **Never ship the jar without embedded textures** — keep embedding every
  rebuild, do not "slim" it. Current: 817 png, 109 ogg, 2389 entries.
- **NEVER alter the 24 day/night reference gradients** in `SKY_DAY` /
  `SKY_NIGHT` / `SKY_DUSK` in `core/sky.fsh`. They are byte-matched to the
  user's references ("identical look" contract). I nearly broke this in phase
  25 — see §7.
- Storm-sky progression 5.0→8.0: **5.0 must be turquoise; 5.3+ NO turquoise.**
- Sun/moon hidden during storm sky; ground shadowing; colorful lightmap.
- **Aurora only in the shader pack, never in core ribbons.**
- **The MOD owns all assets**: shaders, shadows, Story Mode clouds, lighting,
  skies, tone. **The shader pack is limited to aurora borealis + colourful
  lighting ONLY.**
- **Do NOT delete/disable the Story Mode clouds** — they are the real ones.
- Coloured lighting is handled by the mod (`storyModeLighting` /
  `StoryModeLightmapMixin`), not by our `lightmap.fsh`.
- Custom GLSL uniforms keep the `witherstorm_` prefix; shaders use `#version 330`.
- Keep the workspace lean (< ~100 MB); build scratch in `/tmp`.
- **Run javac immediately after any Python script that edits Java sources.**
- **Run the GLSL validator immediately after any shader edit.**
- User wants "all links to play" in one shot — file cards for every deliverable.
- Work autonomously in phases; only stop to ask on marked diagnostics.

---

## 2. CURRENT SHIPPED STATE — 1.9.87

Served from `/home/user/delivery/` at `http://localhost:8765/`
(start with: `python3 /home/user/serve.py`, threaded, from that dir).

```
7eed07ed61074c282d408bcd8cf4d1ce65d66bd9a18d34bcc4ebe0449de891d9  dabywitherstormmod-1.9.87-26.2-beta-mcsm.jar   (56,832,812 B)
599c95c357398569d493aafc8f92cb722dffaa278ea4c121ab12a5385031334c  MCSShaders-shaderpack.zip                      (11,663 B)
94f4f405185f5ed14588168c125728d1c177e4cfc34099e592aa9b9d4d0939df  MCSM_visuals.zip                            (1,064,899 B)
dcecd0d5e1d81d1f9eb94476f17e31df4fd23c287908fa33ba29db350306659a  MCSM_mod_changes.zip                           (67,343 B)
9a51a19042aa751d452ef0f512157cd1a650ad8039addbc38e860dff23055555  shadowtest.py                                   (7,154 B)
```

Retired, never reuse: 1.9.68–1.9.82.

---

## 3. WORKSPACE LAYOUT

```
/home/user/delivery/            # served at :8765, all deliverables + README.txt + LOG_GUIDE.txt
/home/user/mcsm-extras/java/    # OUR Java sources (13 files, see §4)
/home/user/mcsm-extras/valcore.py     # core-shader compile harness
/home/user/mcsm-extras/shadowtest.py  # cloud-shadow acceptance test
/home/user/mcsm-core-shaders/   # THE 14 CORE SHADERS — source of truth, edit these
/home/user/shaderpack-v4/shaders/     # Iris pack source (NOTE: no MCSShaders/ level)
/home/user/glslcheck/bin/glslang      # validator binary (chmod +x after every reset!)
/home/user/uploads/             # 44 files: user's reference shots + 12 rendered test frames
/home/user/serve.py             # threaded HTTP server for :8765
```

**The repo the user mentions is cloned and pushed to `main`.** Prior recovery
notes: `raw.githubusercontent.com` returns LFS *pointer* text for LFS-tracked
files; use `media.githubusercontent.com/media/<user>/<repo>/<branch>/<path>`
for real bytes.

---

## 4. OUR JAVA PATCHES (all in `net.mcsm.extras`)

`mcsm_extras.mixins.json` — **`compatibilityLevel: JAVA_25`, `minVersion: 0.8`,
`required: true`, `defaultRequire: 1`.** Package `net.mcsm.extras.mixin` may
contain **ONLY** `@Mixin` classes (26.2 rule) — `McsmDiag` lives one level up
in `net.mcsm.extras` for exactly this reason.

| class | target | what it does |
|---|---|---|
| `McsmShaderGatePatch` | `ShaderPackCompat.active()` HEAD | forces **false** — the single most important patch (§5) |
| `McsmStormVisibilityPatch` | `FoglessRenderTypes.fogless()` + `reverseShading()` HEAD | both **false** — fixes invisible storm body |
| `McsmBlobCarrierPatch` | `FogRenderer.updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V` **TAIL** | rewrites `cloudEnd` with an invertible encoding |
| `McsmGradientTickPatch` | `LevelRenderer.render(...)` HEAD (full descriptor) | drives `StormSkyGradient.update(cameraState.pos)` every frame |
| `McsmDiag` | *(not a mixin)* | `[mcsm]` log output |
| `McsmGuiExtrasRows`, `McsmStormGrabPatch`, `McsmBeaconStormPatch`, `McsmBeaconBlockInitPatch`, `McsmSpiralPatch` | various | pre-existing features, working, leave alone |

---

## 5. THE FIXES, AND WHY EACH ONE EXISTS

### Phase 1 — invisible storm / no teeth / no eye glow
`ShaderPackCompat.active()` gates **6 systems** via `ifne <skip>` — with Iris
loaded, the mod turns its OWN effects off expecting the shader pack to draw
them. Under the "mod owns everything" architecture, nothing drew.

Separately `FoglessRenderTypes` picks the body render path with
`useCustom = fogless() || reverseShading()`, and `useCustom` selects
`bodyCutout(tex)` which **renders nothing on 26.2**.

**ORDER MATTERS:** `fogless() = active && !legacyDistantRenderer &&
!ShaderPackCompat.active()`. Neutralising `active()` **alone** flips
`fogless()` TRUE and re-selects the broken path. **Both mixins are required
together.** Do not remove one.

Phase 20 verified polarity at **all 11 call sites** of `active()`. Nine use
`ifne` (false enables them). **Two use `ifeq` and invert** —
`GlowRenderTypes.emitterMark` is the important one:
`active TRUE → RenderTypes.eyes()` (vanilla plain glow),
`active FALSE → MARK_TYPES` (the mod's custom emitter mark = **turquoise
teeth**). False is correct at all 11.

### Phase 2 — no shadows at all
`mcsm_sun_true()` computed `(t01*2.0-0.5)*PI` — a **quarter-day phase shift**.
At noon the sun sat on the horizon (`y=+0.02`), so every `sunDir.y > 0.05`
gate failed and cloud shadows never ran at any time of day.
Fixed to `fract(t01)*2.0*PI`; gate lowered to `0.02`. Verified: `sun.y` is
now `+0.965` at noon, `−0.962` at midnight.

### Phase 3 — purple rim at zenith
Four hard `if/else` colour steps; at `u=0.75` magenta cut instantly to
near-black. Now `smoothstep(0.58, 0.94, u)`.

### Phase 4 / 25 — sky colour
Every `mcsm_storm_dome` stop rescaled **×0.46**; `MCSM_SATURATION` 1.34→**1.06**,
`MCSM_CONTRAST` 1.09→**1.04**. (1.34 was *clipping* channels and destroying the
gradient.) Phase 25 then retargeted the **7.0 row** directly from a real
rendered frame to the measured reference profile:
`zenith (0.130,0.076,0.120)`, `mid (0.184,0.116,0.184)`, `horizon (0.373,0.215,0.398)`.

### Phase 5 / 9 / 11 / 15 — the black glare blob (FOUR separate bugs)
1. **Non-invertible encoding.** The mod's `McsmFogCarrierMixin` packs
   `cloudEnd = 1200 + (yaw+180)*2 + (pitch+90)*0.5`. The pitch term spans
   [0,90) while yaw steps by 2 → **45 pairs collide**. Decoder recovered
   pitch ≈ −90° (straight down): the blob was drawn *below the world*.
   **Fix:** our TAIL patch rewrites it as
   `cloudEnd = 3000 + yawIdx*181 + pitchIdx` (yawIdx 0..360, pitchIdx 0..180).
   `pitchIdx < 181` guarantees uniqueness; max 68340 is exact in float32.
   **Verified exhaustively: 65,341 pairs, zero mismatches.**
   Decoder is `mcsm_boss_dir()` in `include/mcsm_visuals.glsl`, band
   `v >= 2999.0 && v <= 68341.0`.
2. **Ambiguous injector.** `FogRenderer` has TWO `updateBuffer` overloads. We
   declared a bare name **and `require = 0`** — Mixin would have skipped it
   **silently**. Now the full descriptor + `require = 1`.
3. **Dead-code producer.** `StormSkyGradient.update(Vec3)` is the ONLY writer
   of `yawDeg/pitchDeg/phase/active`, and **nothing in the jar called it**.
   `active` stayed false forever → both carriers bailed at their first guard.
   `McsmGradientTickPatch` now drives it per-frame.
4. **Wrong compositing.** The "black core bite" was a *subtraction* of
   `vec3(0.010,0.006,0.014)` — ~1.4% of a channel — tuned against the old
   2.2×-brighter dome. It rendered as a bright red-pink RING. Now
   multiplicative: `occl = 0.93*pow(disc,1.5)`, `emis = col*(rim*rim)*0.85`,
   composite `dome*(1.0-occl) + emis`. Core is ~14× darker than sky.

**Also guarded:** `mcsm_clouds_end()` and `mcsm_rd_start()` only recognised the
old 1100–2150 band; they now also guard 2999–68341 or a carrier value would
leak through as a literal cloud distance.

### Phase 16 / 28 — STRANDED ADDITIVE CONSTANTS (a recurring bug class)
The ×0.46 dome rescale left behind every term that *added* a fixed amount:
- **blob core bite** (phase 15) — fixed, now multiplicative
- **lightning flash** (phase 16) — added 0.546 lum to a 0.165 sky, a **4.3×
  white-out** on every strike. Scaled ×0.46.
- **sun halo** (phase 28) — added 0.423 lum to 0.091, a **4.7× blow-out that
  clipped 90% of the sky**. Scaled ×0.46.
- sun halo & horizon glow on the *clear-sky* path were checked and are fine
  (that path was never rescaled).

**RULE: multiplicative terms survive a rescale; additive constants do not.**
If you change dome brightness again, re-check every `+=` against it.

### Phase 17 — animation ran 20× too fast
`mcsm_clock()` returned `gameTime01 * 24000` (**ticks**) but every consumer was
written in **seconds** (their comments say "every ~4.3 s", "roar pulse").
Ticks advance 20/sec → lightning fired **4.65 strikes/second**, blob pulsed at
**9.5 Hz**. A genuine photosensitivity hazard. Now `gameTime01 * 1200.0`.
Phase 18 re-checked all 9 consumers for the opposite regression; all fine.

### Phase 19 / 21 — cloud shadows
- **Bug:** `t = (192 - worldPos.y)/sunDir.y` goes negative above y=192, which
  sampled the noise **mirrored** and painted shadows on terrain *above* the
  clouds. Guarded: `if (dy <= 1.0) return 1.0;`
- **Phase 19 wrongly declared per-cloud matching impossible** (stopped at the
  `CloudInfo` UBO). Phase 21 read `CloudRenderer` instead: vanilla clouds are
  **not procedural** — they load `textures/environment/clouds.png` (256×256)
  on a fixed grid. Constants from bytecode:
  `CELL_SIZE_IN_BLOCKS 12.0`, `TICKS_PER_CELL 400`, `BLOCKS_PER_SECOND 0.6`,
  **scroll is +X only**, Z is a fixed **+3.96**, coverage measured **27.6%**
  (18,103/65,536 opaque texels).
  Shadows now match all of that. **Remaining gap:** per-cell occupancy is still
  procedural because `clouds.png` is not bound to the terrain pass (`Sampler0`
  is the block atlas). Closing it needs Java-side pipeline work to bind a
  second sampler — **a legitimate next task if the user reports mismatch.**

### Phase 24 — mixin config could have been refused at load
Ours declared **no** `compatibilityLevel` while the mod declares `JAVA_25`, and
our classes were `--release 21` (major 65) vs theirs (major 69). Mixin falls
back conservatively and can refuse — **silently**. Now `JAVA_25` + all classes
compiled `--release 25`, verified major version 69.

### Phase 26 — the sky is "intermittent" BY DESIGN (not a bug)
`StormSkyGradient.update()` only selects a storm at **`phase >= 4.5` AND
distance <= 1400**. Below that, nothing sets `active`, the carrier never stamps
`fogSkyEnd`, `mcsm_phase()` returns 0, and the shader correctly takes the
vanilla branch. **Frame 100455 shows a huge storm under a plain blue sky and
that is CORRECT** for a sub-4.5 storm. Do **not** "fix" this by widening the
gate — turquoise at 5.0 is a specified requirement.
`McsmDiag.skyReason()` now states the reason in the log.

---

## 6. DIAGNOSTICS — THE MOST IMPORTANT TOOL

The mod **already** logs to stdout (nobody had been reading it):
```
[dabywitherstormmod][shadow] <reason>
[dabywitherstormmod] storm shadow map FAILED, shadows off: {}
[dabywitherstormmod] storm shadow capture FAILED, shadows off: {}
[dabywitherstormmod] sun glow DISABLED after an error: {}
[dabywitherstormmod] bloom buffer OK: {}/{} lit pixels ...
[dabywitherstormmod] no lit pixels over the centre {}x{} ...
[dabywitherstormmod][perf] %s took %.1f ms
```
`StormShadowMap.status()` dedupes and prints its own reason string. The known
one is literally: *"off: disabled in Effects, strength 0, **a shader pack is
active**, or an earlier error switched it off"*.

We add, under `[mcsm]`:
```
[mcsm] MCSM extras 1.9.87 active. Patches: ...          (once)
[mcsm] ShaderPackCompat.active() forced FALSE ...       (once)
[mcsm] gradient ACTIVE phase=7.00 yaw=-43.0 pitch=12.5  (on change)
[mcsm] blob carrier cloudEnd=27856 (yawIdx=137 pitchIdx=102)
[mcsm] storm sky ON (phase 7.00)  /  storm sky OFF -- phase 4.10 is below 4.5 ...
```

**If the `[mcsm]` banner is ABSENT, the mixins did not apply and nothing
downstream matters.** This is the first thing to check in any user log.

`StormShadowMap.wanted()` requires ALL of:
1. `!failed` (runtime), 2. `stormShadow || stormSelfShadow`,
3. `stormShadowStrength > 0`, 4. `!ShaderPackCompat.active()` ← our patch.
Config defaults confirmed from `<clinit>` (last write wins):
`stormShadow=true`, `stormShadowStrength=0.55`, `sunGlow=true`,
`sunGlowStrength=2.2`, `bloomStrength=2.0`, `turquoiseTeeth=true`,
`cataclysmHalos=true`, `headEyeGlow=true`, `blackGlare=true`,
`stormBackdrop=true`, `storyModeLighting=true`, `impactLight=true`,
`bloomMaskToStorm=true`, `stormSelfShadow=true`. **All 14 enabled.**

---

## 7. MEASUREMENT DISCIPLINE — READ THIS, IT COST REAL TIME

Four separate times a **measuring tool** produced a confident wrong answer:

1. **Phase 12** — a caller census reported 0 callers for *everything* including
   known-good ones. Broken `javap @argfile`. Caught by a control case.
2. **Phase 20** — a config parser reported every boolean as `0.5`, then as
   `false`. Field *declarations* default to false; the real values are assigned
   later in `<clinit>`, so **only the last write counts.**
3. **Phase 27** — a naive min/max ratio said "cloud shadows present" on a frame
   that has none. min/max is the statistic outliers corrupt most.
4. **Phase 28** — a hue measurement on a **clipped** patch reported a too-blue
   sky as "correct magenta", and nearly caused a correct fix to be reverted.
   **A clipped pixel carries no hue information — reject `max(c) >= 235` first.**

**Rules:**
- A scan that disagrees with a known-good fact is a **broken scan**, not a discovery.
- Always run a control case whose answer you already know.
- Prefer **structural** tests (run lengths, histogram modality) over single ratios.
- Reject saturated pixels before any colour comparison.
- **Static checks are not visual verification.** 21/21 and 15/15 audits passed
  while four defects were plainly visible on screen.

---

## 8. BUILD / VALIDATE / SHIP RECIPE

```bash
# toolchain (/tmp is wiped between turns — recreate every time)
mkdir -p /tmp/jdkx /tmp/dl && cd /tmp/dl
curl -sL -o jdk.tgz "https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse" && tar xzf jdk.tgz -C /tmp/jdkx
curl -sL -o client.jar "https://piston-data.mojang.com/v1/objects/2dc72797acbc1b63fc16a11c4ac393605f453754/client.jar"   # 39,193,383 B
curl -sL -o mixin.jar    "https://repo1.maven.org/maven2/net/fabricmc/sponge-mixin/0.15.4+mixin.0.8.7/sponge-mixin-0.15.4+mixin.0.8.7.jar"
curl -sL -o jspecify.jar "https://repo1.maven.org/maven2/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar"
curl -sL -o fastutil.jar "https://repo1.maven.org/maven2/it/unimi/dsi/fastutil/8.5.15/fastutil-8.5.15.jar"
curl -sL -o dfu.jar      "https://libraries.minecraft.net/com/mojang/datafixerupper/8.0.16/datafixerupper-8.0.16.jar"
curl -sL -o joml.jar     "https://libraries.minecraft.net/org/joml/joml/1.10.8/joml-1.10.8.jar"

# GLSL validate (ALWAYS after any shader edit)
chmod +x /home/user/glslcheck/bin/glslang
rm -rf /tmp/vc && mkdir -p /tmp/vc/assets/minecraft
cp -r /home/user/mcsm-core-shaders /tmp/vc/assets/minecraft/shaders
python3 /home/user/mcsm-extras/valcore.py /tmp/vc/assets/minecraft/shaders /tmp/dl/client.jar   # expect 15/15

# compile (ALWAYS --release 25)
J=/tmp/jdkx/jdk-25.0.4.1+1
CP="/tmp/dl/client.jar:/home/user/delivery/<current>.jar:/tmp/dl/mixin.jar:/tmp/dl/jspecify.jar:/tmp/dl/fastutil.jar:/tmp/dl/dfu.jar:/tmp/dl/joml.jar"
cd /home/user/mcsm-extras/java
$J/bin/javac -nowarn --release 25 -proc:none -cp "$CP" -d /tmp/build $(find . -name "*.java")   # expect 13 classes

# assemble
rm -rf /tmp/fx && mkdir -p /tmp/fx/cls && cd /tmp/fx/cls
unzip -o -q /home/user/delivery/<current>.jar
cp -r /home/user/mcsm-core-shaders/* assets/minecraft/shaders/
cp -r /tmp/build/* .
# bump version in fabric.mod.json AND the McsmDiag banner string
zip -q -r -X /tmp/<new>.jar . -x '.*'

# ship
cd /home/user/delivery && cp /tmp/<new>.jar . && rm -f <old>.jar
sha256sum *.jar *.zip shadowtest.py > sha256.txt
python3 /home/user/serve.py &     # threaded; from delivery/
# then curl every file and byte-compare against on-disk
```

**`/tmp` is a 993 MB tmpfs.** It has hit 100% twice and **silently truncated
writes** (a 53.5 MB "jar" that wasn't a valid zip, and a failed `zip`). Check
`df -h /tmp` and clean before big writes. Also: never verify a download by
writing into a full `/tmp`.

**The stock `python3 -m http.server` crashes on concurrent 56 MB transfers.**
Use `/home/user/serve.py` (threaded, `Accept-Ranges`, `no-store`).

---

## 9. VERIFIED-GOOD FACTS (don't re-derive these)

- Cross-artifact audit: **0** core-shader overlap between jar and visuals pack.
  `MCSM_visuals.zip` is now **fully redundant** — 763/764 files byte-identical
  to the jar, only `pack.mcmeta` differs. Harmless, but a *stale* copy in
  `resourcepacks/` WOULD shadow the jar.
- Shader pack scope verified clean: `SKY_STORY_MODE=0` in source *and* false in
  `shaders.properties`, so `storyModeSky()`/`biomeTint()` are dead code. Live
  path is `c = skyColor; c += aurora()`. Terrain does `COLORED_LIGHT` only;
  textured/basic are passthrough; BLOOM 0, TONEMAP 0, VIBRANCE 1.00.
- All 82 mixin classes (73 theirs + 9 ours) exist and are registered.
- Every visual system has a caller (phase 12 census, 169 distinct call targets).
  `StormSkyGradient.update` is called **only** by our `McsmGradientTickPatch`.
- No mixin collision: mod injects `dabyws$bloomAtLevelEnd` at RETURN of
  `LevelRenderer.render`; we inject `mcsm$driveStormGradient` at HEAD. All
  handler names across the jar are unique.
- `WitherStormEntity` sends `WitherStormPositionPacket` every 2 ticks
  server-side → drives `ClientDistantStormManager`.
- **Upstream 1.9.60 jar is unobtainable** (404 both branches).
- `repo1.maven.org` 404s on `com/mojang/datafixerupper` — use
  `libraries.minecraft.net`.
- glslang cannot compile `#version 330 compatibility` builtins; `validate.py`
  shims them. GLSL has **no hoisting** — the `mcsm_ramp` forward declaration
  near line 40 of `mcsm_visuals.glsl` must survive any edit.
- Pillow IS available.

---

## 10. RULED OUT — DO NOT RETRY

- Shader pack, texture atlas, CEM, OptiFine-detection compat class,
  `emissive.properties`, tawmesh meshes, Legacy Distant Renderer alone.
- **The `witherstorm_*` uniform path is DEAD** — 0 classes reference
  `witherstorm_BossPos` / `witherstorm_Phase` / `witherstorm_GameTime`. The
  `FogData` carrier is the only channel.
- Do **not** write a smarter decoder for the old `cloudEnd` encoding — proven
  non-invertible.
- Do **not** neutralise `ShaderPackCompat.active()` on its own (see §5 phase 1).
- Flat vivid blue night sky = **not a defect**, it's the storm's own baked sky.
- The purple rim was **not** a dome-pole/UV bug — don't revisit geometry.
- `mcsm_story_grade` with SAT 1.34 was **actively harmful** (clipped channels).
  The references are dark and moody with vivid *accents*, not globally vivid.
- Substring presence checks are fooled by explanatory comments — **strip
  comment lines before asserting a formula was removed.** (Bit us twice.)

---

## 11. WHAT IS STILL OPEN

Acceptance criteria status:
- ✅ no IllegalClassLoadError
- ✅ storm body renders (**proven** by frames 100455 / 131242 / 131056)
- ✅ one owner per visual system (mod owns everything; pack is aurora + light)
- ✅ :8765 + README + LOG_GUIDE + sha256
- ❓ **halos + turquoise teeth + eye glow** — gate fixed, never visually confirmed
- ❓ **ground/cloud shadows** — all four `wanted()` conditions now satisfiable,
  never visually confirmed. `shadowtest.py` exists to settle it.
- ❓ **sky matches references** — 7.0 row retargeted from a real frame in phase
  25; sun halo declipped in phase 28. Needs a fresh frame.
- ❓ **glare blob** — four bugs fixed across phases 5/9/11/15, never seen working.

**The single most valuable next input** is a fresh test from the user on
1.9.87: a daylight screenshot over open flat ground, a late-phase (>5.0)
screenshot, and the `[mcsm]` + `dabywitherstormmod` lines from
`.minecraft/logs/latest.log`. Run the screenshot through
`python3 shadowtest.py <file>`.

**Known candidate next tasks** (in rough priority order):
1. Diagnose from the user's log/frames — everything above is unverified in-game.
2. Bind `clouds.png` to the terrain pass so cloud shadows match per-cell
   (phase 21's remaining gap). Needs Java pipeline work.
3. Hunt for a **fifth** stranded additive constant if a fresh frame shows any
   blown-out region.
4. Consider dropping `MCSM_visuals.zip` from the deliverables as redundant
   (user's call — it's useful standalone on a vanilla client).

---

## 12. TONE AND WORKING STYLE THE USER EXPECTS

- Work in **phases**, autonomously, continuing until done. The user repeatedly
  says "keep continuing until it's done."
- **Be honest about what is and isn't verified.** Say plainly when a claim is
  static analysis vs. an observed frame. The user is the only renderer.
- **Own mistakes explicitly.** Several bugs in this project were *introduced by
  earlier fixes* (the ×0.46 rescale stranded four constants; a `vec2.z` typo
  broke 11 of 15 shader units). Flag them, don't bury them.
- Ship a versioned jar + refreshed README + sha256 + working :8765 every phase
  that changes an artifact.
- Present the README at the end so the user sees it.

---

**End of handoff. The new agent should read §5, §7 and §11 most carefully.**

---

## 12. PHASE 29 (2026-09-04) — build **1.9.97-26.2-beta-mcsm**

### Environment reality this session
- **No network** (curl to Mojang/Adoptium/Maven all fail: exit 35/empty) and
  **no local JDK/javac, no Pillow, no git-lfs**. The LFS-filtered `.py` files in
  the repo are pointer text in the working tree (valcore/validate/shadowtest).
- The user-linked current build is on branch `heress`:
  `dabywitherstormmod-1.9.95-26.2-beta-mcsm.jar` (sha256 git-object, real bytes,
  57,363,400 B). **New truth baseline.** It already contains everything from
  1.9.88–1.9.95 (CEM engine, McsmTeethTexturePatch, McsmTeethGlowPatch,
  McsmSilhouetteWrapPatch, McsmStormBackdropPatch, McsmShadowTrackPatch,...).
  Its embedded `sky.fsh` + `include/mcsm_visuals.glsl` DIFFER from main's
  copies — main was stale. **Repo copies have been synced FROM the jar first,
  then edited.**
- Java edits were impossible to compile-verify → **no Java changed this phase**
  (standing rule: javac after any Java edit). The glare-size slider and the
  extras-tab scroll fix are wired shader-side and queued for the toolchain
  phase (see backlog §13).

### What changed in 1.9.96 (all shader-side, all validated 42/42 by the new
### `glslcheck/shimcheck.py` offline glslang harness — shims are compile-only):
1. **Glare blob sized & centred on the storm** (`include/mcsm_visuals.glsl`):
   extent `mix(24,36°) * mcsmSize`, **default mcsmSize = 1.125** after the
   user corrected the brief in-review ("not smaller, a tiny bit bigger than
   1.9.95"; the "way too big" offender was the post sun halo, shrunk there). Carrier band **FogRenderDistanceStart 9001..9299** =
   size×10 ready for the Java slider (range 0.1..3.0 → up to ~3x the old
   maximum). `mcsm_rd_start()` guards the band so fog never reads it.
   **ANTIPODE FIX**: carrier decode result negated — frames 194701/195146 show
   the mass mirrored both axes (boss→camera vector). This is also the
   "clipping through the other side of the storm" fix. If a future frame shows
   it opposite again, flip that one negation back first.
2. **Glare more opaque**: occl heart 0.93→0.965, skirt 0.78→0.85, cap
   0.95→0.97 ("a little more opaque, not too much").
3. **5.5–5.9 palette**: halo 5.65–5.90 → (0.095,0.030,0.170) deep purple with
   a tinsy blue; 5.48–5.60 pink-magenta nudged; `sky.fsh` 5.5 stop pinker
   (measured Sep-3 stills kept as anchor, hue nudged pinkward per user) +
   **new 5.7–5.9 "dark pink end" stop** so the range no longer holds one flat
   colour. 5.0 turquoise / 7.0 measured row / SKY_DAY-NIGHT-DUSK arrays all
   untouched.
4. **Silhouette glow = blue** (`dabywitherstormmod:shaders/core/storm_glow.fsh`,
   now tracked in `jar-overrides/`): luminance-preserving re-hue of the vertex
   colour to blue (0.20,0.45,0.95); **white core washout 0.22→0.08** — that was
   what blew the turquoise teeth marks out to white ("teeth still not
   turquoise"). Teeth marks themselves already ARE turquoise in the *_e.png
   textures (measured: rgb(71,240,225)); they now read through.
5. **Phase-5.5 sun glare no longer orange + smaller**
   (`dabywitherstormmod:shaders/post/storm_sun_glow.fsh`): luminance-preserving
   re-hue yellow→dark-red/magenta with 22% blue note; wide halo pow 4→7 and
   ×0.35→×0.18 (~8x smaller half-width).
6. **Aurora borealis in the mod**: `mcsm_aurora()` in the include, called from
   the clear-sky path of `core/sky.fsh`, night-gated + cold-biome-biased via
   fog colour. Additive on top of the finished sky; reference gradients
   untouched. Subtle (0.06). Shader-pack aurora still exists independently —
   running both shows both (they're independent toggles).
7. **World grade**: MCSM_SATURATION 1.06→**1.14**, MCSM_CONTRAST 1.04→**1.08**
   ("more contrast and vivid"; kept far below the clipping 1.34 disaster).
8. **Devourer body opacity**: `pngtools.py lift` — devourer_assets pngs'
   5,080 semi-transparent texels (alpha 1..120) lifted to 200..253 (holes kept
   0; "a little more opaque, not too much").
9. fabric.mod.json version → 1.9.96-26.2-beta-mcsm. Jar: 2450 entries,
   57,354,186 B, sha256 **acbbe5f6df314351dc81a48b3a82ba1104cdbd9b16e8c08fd1fc1fac7dcef6fe**
   (1.9.97 — rebuilt in-phase after the glare-size ruling; 1.9.96 never shipped),
   zip-tested clean. delivery/ README header updated; sha256.txt regenerated.
   NOTE: the `[mcsm]` in-log banner still prints the 1.9.95 string (it is a
   compiled class; no Java rebuild this phase) — cosmetic only.

### Build-from recipe this session (no network, no JDK)
```
git fetch origin heress
git show FETCH_HEAD:dabywitherstormmod-1.9.95-26.2-beta-mcsm.jar > /tmp/dl/mod95.jar
unzip to /tmp/fx/cls; overlay repo files (mcsm-core-shaders/* +
jar-overrides/*); bump fabric.mod.json version; zip -q -r -X.
Validate GLSL first: python3 glslcheck/shimcheck.py mcsm-core-shaders \
  jar-overrides/.../storm_glow.fsh jar-overrides/.../storm_sun_glow.fsh  # 42/42
```

## 13. PHASE 30+ BACKLOG (needs network for JDK + client.jar, or the gradle
    source build on branch heress — its MIT licence allows editing with
    attribution; roadmap at docs/WITHER_STORM_FEATURE_ROADMAP.md)
User-ordered, 2026-09-04 message:
- **Extras-tab sliders**: "clicked it, just went plus and minus, nothing to
  scroll" — rows appended past the scroll viewport; fix scroll-bounds refresh
  after McsmGuiExtrasRows adds; add **Glare Size slider** (carrier band 9001..
  9299 already decoded shader-side), aurora/smoke/supernova toggles.
- **Config-screen overhaul**: MCSM-accurate layout, "Save & Quit" slot,
  Episode-One button ("The Order of the Stone"), intro (camera drifts down
  through clouds to the character), three white side bars toggle (default on).
- **Silhouette placement**: colour is now shader-blue; "all the way around the
  sides" needs quad placement in a Java patch (extend McsmSilhouetteWrapPatch).
- **Tentacle attacks**: grab/sway/slam players, smash buildings; earthquakes,
  ground splits scaling with phase.
- **Supernova ring at phase-up** (colour wave, giant transparent blue→purple
  rings that fell trees/blocks and blast the player; default ON, config-gated).
- **Smoke screen**: skull impacts → grey ground smoke + yellow electric sparks
  + crackle noise (default ON, config-gated).
- **Phase 5.5+ purple lightning from the sky** + **purple floating motes** all
  over the sky after phase 5 (config-gated).
- **Dust waves** trailing the storm's swoops against blocks (config-gated).
- **Ambient block-lift particles** (tiny block-textured debris lifting near the
  storm even off-beam) + **phase-5 black smoke drift** (TNT-smoke style, black).
- **Command block**: 3D command block model, purple side palette like the
  reference, cycling colours, dented 3-dot texture animating, night glow like
  RGB; shift+click the core in-world → big control menu (summon, every
  ability), with vanilla command-block UI as the other option.
- **Gameplay layout**: inventory to the top-left (MCSM-like), look-down
  first-person body (arms/legs), place-items-on-crafting-table interaction.
- **Storm rain-phase tractor beams pull players onto debris clusters the
  player can stand on** (all phases).
- **Ground shadows not moving**: get the user's latest.log, read the
  [dabywitherstormmod][shadow] / [mcsm] lines per LOG_GUIDE.txt before editing
  anything (McsmShadowTrackPatch exists in 1.9.95).
- **Porch/emissive story-lighting** on story-relevant blocks (Iris pack
  block-id work or mod lightmap), more atmospheric night towns.

### Phase 30 additions (user, 2026-09-04 second message) — append to backlog §13
- **Death cinematic**: while dying the storm cracks open — WHITE cracks spread
  over the body — then it falls to the ground in SEGMENTS (breaks into chunks
  as it drops). Layered with the command-block shatter.
- **Obliterate flash**: the command block erases mobs/entities "out of
  existence" with a single flash — including players (knockout-style); an
  optional prank "kick player" variant. Must be config-gated; delete default
  ON, kick default OFF (grief-safe), both configurable.
- **Command Wire** (new block, never in MCSM): wires into the command block
  like redstone; with a lever attached it opens a **gigantic holographic
  terminal window** — summon/dismiss the storm, choose who it follows
  (Ivor's "programmed to follow the amulet" made playable), orders, ride-a-mob
  option, summon button. Alternative summon: command block on the ground with
  a lever on EACH side in a circular ring — click a lever → instant summon.
- **Shockwave/supernova timing clarified**: the expanding ring blast happens
  on entering **phase 4**, again at **phase 7**, and once more **when killed**
  — three triggers, all config-gated.

### Phase-30 mechanics note
171 decompiled mod sources exist on branch `heress` (clean Vineflower-style).
Strategy stays ADDITIVE: new features as mcsm-extras classes + mixins +
(allowed) our own Fabric entrypoint registered in fabric.mod.json — no edits
to decompiled net.dabicco sources unless a feature truly can't hook around.
ci/build.sh + .github/workflows/build-mcsm.yml now compile every push on
GitHub Actions (JDK 25), run shimcheck, and publish a release when VERSION is
new — "the GitHub compiler" the user asked for.

---

## 14. PHASE 30a (2026-09-04) — build **1.9.98-26.2-beta-mcsm**

Shader-side engine work, dormant-safe (no Java landed in this jar — sources
are committed and compile via ci/build.ps1 or CI; nothing uncompiled ships):

- **Wide aim carrier.** `cloudEnd = (3000 + yawIdx*181 + pitchIdx) * 16 +
  sizeIdx` (band 47000..1093455, exact < 2^24). Shader decodes BOTH bands;
  legacy keeps aiming, wide adds the glare-size nibble. Guards updated in
  `mcsm_clouds_end()`; `mcsm_rd_start()` 9001-band guard retired-but-kept as
  future-proofing. Default size when no writer: **1.18** ("a tiny bit bigger
  than 1.9.95" — user's final ruling; the 1.9.96 half-size passed review).
- **Death-sequence engine** (`mcsm_death*` family in mcsm_visuals.glsl +
  branch in sky.fsh main): distortion wobble -> white crack web -> shaking
  layered implosion to a white-hot point with in-rushing pink/white motes ->
  whole-sky flash at dt=0.55 -> six-ring supernova (purple, pink, blue,
  orange, green, yellow) -> low settling dust -> 0.95..1.0 ease-out so the
  return to normal sky never snaps. Carrier: FogSkyEnd band 1906..2906 maps to
  dt 0.06..1.06. **Dormant until the phase-31 driver stamps it.**
- **Java written, awaiting first CI/local compile into a jar**
  (`mcsm-extras/java`, compiled by ci/build.ps1 / Actions): config adds
  glareSize, auroraEnabled, deathCinematic, supernovaRings, smokeScreen,
  purpleSky, dustWaves, realityTear (=ON per user), obliterateFlash (ON),
  obliterateKick (OFF, grief-safe); `McsmBlobCarrierPatch` stamps the wide
  carrier + size nibble; `McsmGuiExtrasRows` adds all toggle/slider rows and
  nudges zero-arg scroll/relayout/refresh methods (extras-tab scroll fix
  attempt — if the screen's real hook differs, it silently no-ops; check next
  user log).

### Jar record
sha256 fa33e307bf644a562e0a5ace53a704bc6fe9b78ab6fa42dfaefc1793a2d9fd06 —
57,356,874 B, 2450 entries, zip-tested; GLSL 42/42. 1.9.96 and 1.9.97 existed
only locally inside this phase (never uploaded); current public build: 1.9.98.

### Phase 30b/31 next (Java-first): death driver mixin on the storm entity
(stamp 1906..2906 during dying, ring-radius block/player damage, segment
drop), reality-tear block + corruption spread + splash-heal cure, command-wire
block + holographic terminal, obliterate flash action, tentacle attacks,
extras-tab scroll verification from user log, inventory/HUD moves.

### Phase 30a-2 (2026-09-04, after user's config screenshot)
- **Extras-tab bug ROOT-CAUSED** (Screenshot 2026-09-04 145751): their screen
  folds rows by section (`collapsed` set, `tabKeys`/`masterKeys`) and
  `repositionRows()` ran at init BEFORE our TAIL-injected rows → header showed
  [-] with an empty body. Also: jar-era GUI class had extra self fields
  (skipCurrentSection/skipCurrentMaster) we don't reproduce. New design: ONE
  header row + ONE button row ("Open the MCSM Control Panel") that opens our
  own `net.mcsm.extras.client.McsmExtrasScreen` (vanilla Screen; CycleButton +
  AbstractSliderButton two-column layout; conservative API so it survives 26.2
  render refactor). Then exact-name `repositionRows()` is invoked. `rebuild()`
  would drop ours — NEVER call it.
- **API-parity rule for CI rebuilds (important):** repo Java now mirrors the
  jar's 1.9.95-era config fields (`ogCemModels Z` default false, `smudgeScale
  D` default 0.5) — other pre-compiled jar classes read these, so removing or
  renaming them would NoSuchFieldError at runtime. Verified via constant-pool
  scan (classfile parser snippet in this session's notes).
- **New backlog items (user, same message):**
  - "Pilot the storm yourself" — ride/controllable-storm option (follows the
    Command Wire terminal; you'll steer it from the holographic panel).
  - Ground shadows for TREES and MOBS too (not just cloud/storm shadows) —
    renderer-level shadow-map expansion; StormShadowMap currently covers the
    storm only. Big engine task; phase 31+.
  - Torches & colored lighting ambience — pack side: MCSShaders COLORED_LIGHT
    is default-on; user's working knob today is Iris → shader pack Lighting →
    COLORED_LIGHT_AMT. A richer per-block emission pass is queued with the
    Story Mode "porch lighting" item.

### Phase 30b (2026-09-04, after "halo accuracy comparison" screenshot)
Reference frame: `uploads/Screenshot 2026-09-04 182220.png` (user's side-by-side:
LEFT = our 1.9.98 render, RIGHT = the MCSM original). The image cannot be viewed
as pixels in this sandbox, so it was MEASURED instead (pure-python PNG decode,
luminance/coverage scans). Numbers that drove the retune:
  * reference top rows: #06030b .. #0c0911  (lum 0.015-0.02) -- a near-black
    slab that spans the FULL frame width; dark coverage 1.00 at y=65..156.
  * our 1.9.98: sky #5d3e62 / #894889, dark coverage 0.00 above the blob --
    the mass was a compact 273px disc in a 950px frame, and its core read
    0.06-0.17 lum (4-10x too bright).
Shape: the reference is NOT round. Dark span narrows monotonically downward
(full width at the top -> ~68% at mid-frame -> gone at the ground) with the
arms running a long way down both sides = a stretched heart, V tip at the
bottom. Implemented as the classic heart implicit
    f(x,y) = (x^2+y^2-1)^3 - x^2 y^3   (interior f<0)
in a gnomonic dome-plane frame around the boss direction, stretched
1.15x wide / 1.38x tall with an extra 1.25x BELOW the centre (the V runs far
down the sides) and 0.72x above it (0.85 cropped the lobes out of a 70deg
frame, which read as a plain wedge -- the notch and lobes have to be visible
for it to read as a heart). Boundary radius by 5-step bisection along the ray
(the heart is star-shaped about the origin).
Look: black heart now reaches 0.82 of the radius (was 0.55), occlusion ceiling
0.99 and the top slab pushed to 0.995 (measured interior #060209, lum 0.013 vs
the reference's 0.015). Rim/skirt emission is killed toward the top (x0.28 /
x0.22) so all colour traces the V and the underside -- every bright pixel in
the reference sits on the lower/outer edge.
Colour (5.5-5.9): reference glows sample #3e1256 / #321772 / #472fbe -> hue
(0.44, 0.20, 1.0) at unit luminance. Old key normalised to (0.56, 0.18, 1.0)
= too pink. New key vec3(0.082, 0.030, 0.185). Never orange.
Clouds: `rendertype_clouds.vsh` now exports `mcsmCloudRay` and the .fsh
reconstructs the world ray (transpose(mat3(ModelViewMat)) * normalize(ray),
same trick as sky.fsh) to apply `mcsm_heart_cover()` -- the deck now vanishes
wherever the mass is opaque, which is the "you can't even see the clouds at the
very top of the storm" note. Gated to phase 5.10-5.90 and to an active carrier.
Validation: shimcheck 40/40. Preview: delivery/preview_halo_heart_1.9.99.png
(left = old disc, right = new heart, same scene + a cloud deck).
Release: 1.9.99 is a SHADER-ONLY overlay onto the 1.9.98 jar (no javac in this
sandbox): the 4 touched shader entries + fabric.mod.json version were replaced
in place, 2450 entries preserved, sha256 regenerated. The Java half (extras
config-screen fix, McsmExtrasScreen) still needs build.ps1 / GitHub Actions.

#### Phase 30b-2 — the reference was MEASURED, then FITTED (same day)
Two measurement mistakes in the first pass, both caught and corrected:
1. The right-hand screenshot is PILLARBOXED: ~60px of #202020 matte on each
   side. Early "dark" readings included the matte. Content box is actually
   x[62..894] y[6..516] of the panel (832x510).
2. The first preview used a hand-guessed dome. Replaced with the REAL
   mcsm_storm_dome()/mcsm_col()/mcsm_story_grade() maths lifted from the
   shipped shaders, so the preview is now what the game actually draws.

Fit method: per-row "dark coverage" profile of the reference (fraction of the
row below luminance 0.045, 12 rows) vs the same profile computed from our
render in the reference's framing (camera pitched up 13.4deg -- derived from
the horizon sitting at 0.67 of the frame -- storm head at +29.5deg). Grid
search over the heart stretch triple.
   row y_frac:  0.04  0.12  0.21  0.29  0.38  0.46  0.54  0.62  0.71
   reference :  0.81  0.98  1.00  0.89  0.80  0.63  0.80  0.66  0.41
   1.9.98    :  0.22  0.29  0.33  0.33  0.31  0.25  0.04  0.00  0.00   err 0.580
   1.9.99    :  0.94  0.85  0.77  0.70  0.64  0.58  0.51  0.44   --    err 0.200
Final stretch: width 1.50 (was 1.15), height 1.45 (was 1.38), lower half 1.70
(was 1.25), upper half 0.72 unchanged. Darkening the 5.5 dome zenith was
tested and REJECTED: it moved the error 0.138 -> 0.137, i.e. nothing, and it
would have broken the standing "5.5 sky stays pinkish/purplish" rule. The
residual error is the reference's own storm body + attachments (the
non-monotonic 0.63 -> 0.80 bump at row 0.54), which a sky shader cannot draw.
Artifact: delivery/preview_halo_vs_reference_1.9.99.png -- LEFT is the user's
reference crop, RIGHT is our render, same framing, same size.
1.9.99 jar re-rolled with the fitted numbers: sha256
058da7fe3a5b10c317b80f6b5b4f844bdfbd56c0118f733607b284dd1ad05656.

#### Phase 30b-3 — the COLOUR fit (same reference, third measurement pass)
The shape fit was done; the colour gap was still open, so the reference was
measured per-cell (8x6 grid, upper sky) against our render, comparing BOTH
luminance and hue normalised to unit luminance.
Finding: our 5.5 dome was too bright AND too red. Sampled frame-edge sky cells:
    cell   reference                     1.9.96 (old dome)          1.9.99 (fitted)
    r2c0   #260e3e lum .089 h1.68:.62:2.75   #591d53 .179 h1.95:.64:1.82   #351241 .113 h1.84:.62:2.25
    r2c7   #2f134a lum .113 h1.62:.66:2.56   #4c1549 .143 h2.09:.58:2.00   #240835 .067 h2.09:.47:3.08
    r3c0   #320c4e lum .097 h2.01:.48:3.14   #531748 .154 h2.11:.59:1.83   #20072c .059 h2.14:.47:2.94
Blue/red at the frame edges was 1.8-2.0 where the reference reads 2.6-3.1.
Sanity check that proved the dark top is SKY, not the creature: local contrast
(mean |px - 3px neighbour|) is 0.0012-0.0021 in the reference's dark upper
bands vs 0.0181 on the ground -- a textured creature surface would be noisy,
a sky gradient is flat. So the dome really did need to be darker.
Grid search on (zenith, mid, horizon brightness) x blue boost, 32 sky cells:
    A 1.9.96 as-is            dLum 0.0324  dHue 0.4529
    B fitted extreme (mid .25) dLum 0.0282  dHue 0.3829
    C moderate    (mid .40)    dLum 0.0279  dHue 0.4072
    D darker mid  (mid .30)    dLum 0.0280  dHue 0.3959
Chose D with an extra-dark zenith (the zenith barely affects the score because
the mass covers it in this framing, but it is what you see when you look up):
    5.5 dome stops  zenith (0.067,0.022,0.134)  mid (0.099,0.032,0.150)
                    horizon (0.505,0.205,0.580)
    luminance 0.084/0.174/0.303 -> 0.040/0.054/0.298
The MID stop was the culprit: it alone drives elevations 10-45deg and was 3.2x
brighter than the reference there. The horizon stop keeps its brightness and
only goes blue/red 0.78 -> 1.15, so the low band stays pink-dominant and the
standing "5.5 sky pinkish/purplish" rule survives. 5.7-5.9 keeps a milder 1.3x
blue so the sky does not snap back to pink at phase 5.7.
Verified in sky.fsh (line ~93); REVERT restores the documented "was" values.
shimcheck 40/40. Jar re-rolled with the WHOLE mcsm-core-shaders tree this time
(core/ + include/) so a future roll cannot drift from source: sha256
36ead130096f2f5955c5dd26ffca6dc82d570ec80e3d03ef695e36f5611f21a1.

### Phase 30c (2026-09-04) — MCSM 1.9.100: the mass is a MAP PIN, not a heart
User correction after seeing 1.9.99: "the shape actually is not a heart I was
wrong. The exact shape ... a minimalist black outline of a map pin icon ... The
top of the shape is flattened into a completely straight, horizontal line with
rounded corners that curve smoothly down into a single sharp point at the
bottom." Also confirmed the reference's near-black upper area is SKY (not the
creature), so the 1.9.99 dome retune stays.
Implementation (mcsm_visuals.glsl): the heart implicit is gone. The silhouette
is now a half-width profile w(y) in the dome plane, y up, origin on the storm:
    y in [H-r, H]  :  w = (W - r) + sqrt(r^2 - (y-(H-r))^2)   rounded corners
    y in [-D, H-r] :  w = W * pow((y+D)/(H-r+D), k)           k<1 sweeps the
                      shoulders out before converging on one cusp
Top edge = straight horizontal segment |x| <= W-r (75% of width at r = 0.25W);
w -> 0 at y = -D gives the single sharp point. Exactly one cusp, no notch.
Boundary radius by 6-step bisection (the pin is star-shaped about the origin).
Constants (grid-fitted against the reference's per-row dark coverage):
    W 1.95   H 0.78   D 2.80   r 0.49 (=0.25W)   k 0.68
    W = 2.00 scored 0.081 vs 0.093 but left only ~27% of the frame as sky at
    mid-height; 1.95 keeps ~30%, so the storm stays readable.
Result, mean |error| over 9 sky rows:
    1.9.98 disc 0.580  ->  1.9.99 heart 0.200  ->  1.9.100 pin 0.076
    row     0.04  0.11  0.18  0.25  0.32  0.39  0.46  0.54  0.61
    ref     0.81  0.98  1.00  0.89  0.80  0.63  0.80  0.66  0.41
    1.9.100 1.00  1.00  0.96  0.89  0.80  0.75  0.68  0.63  0.56
Renamed mcsm_heart_field/mcsm_heart_cover -> mcsm_mass_field/mcsm_mass_cover
(cloud pass updated to match). shimcheck 40/40.
Artifact: delivery/preview_halo_vs_reference_1.9.100.png (reference | ours).
Release 1.9.100 built from the clean 1.9.98 base (not the re-rolled 1.9.99) with
the full mcsm-core-shaders tree overlaid: 2450 entries, sha256
03cbed79a8c16d419eef69b6cb36cbb56b95ff081ebfc97b15289d8d50d6f1dd.

### Phase 30d (2026-09-04) — "I can't see any of the changes" (DELIVERY bug, not code)
User report: no config-menu overhaul, inventory still at the bottom, no
corruption, no smoke, teeth unchanged, halo still CIRCULAR, no command text /
holographic wire / 3D command block, no shockwave on death (storm dies
instantly), no aura, no rift, MCSM structures and instructions don't render --
only cloud shadows work. No resource packs enabled, shaders OFF.
Two independent causes, both proven:
1. DELIVERY: the user is running the RELEASED 1.9.98 jar. 1.9.99/1.9.100 were
   never released -- `gh release create` fails at the asset upload step from
   this sandbox (EOF on both a 68 MB jar and a 37 KB zip; `git push` works,
   uploads.github.com does not). So every shader change made after 1.9.98 has
   never reached the game. Proof that it is the old jar: the halo is still a
   perfect circle, which is exactly the pre-1.9.99 code.
2. JAVA NEVER COMPILED: config-screen fix + McsmExtrasScreen, inventory/HUD
   move, smoke screen, reality tear, command wire, corruption spread, death
   shockwave/supernova, MCSM structures/instructions are all .java sources in
   mcsm-extras/java. There is no JDK in this sandbox (no javac/java), so none
   of them exist in ANY jar yet. Cloud shadows work because they are pure
   shader (terrain.fsh), which is why the user sees exactly one MCSM feature.
Not the cause (all checked and cleared): jar is structurally valid (zip test
OK, fabric.mod.json + dabywitherstormmod.mixins.json + mcsm_extras.mixins.json
present, 38 net/mcsm/extras classes, 14 shader entries); file NAME is
irrelevant to Fabric (mod id comes from fabric.mod.json); no resource pack
shadowing; with shaders OFF the jar's core shaders DO run.
NEW DELIVERY MECHANISM (works around the blocked release uploads):
delivery/MCSM_shaders_1.9.100.zip -- 37 KB, pack.mcmeta + the 14 core shader
entries. Drop in .minecraft/resourcepacks, enable, move to TOP: a resource
pack overrides the copies baked into the jar, so shader-only changes can ship
without touching the jar. pack.mcmeta reuses the project's own pack_format 15
(the value in the mod jar), so Minecraft may flag it red/incompatible --
enabling it anyway is harmless. Remove it after running ci/build.ps1, which
bakes the same files into the jar.

### Phase 30e (2026-09-04) — the GitHub Actions build: status + honest inventory
The user installed ci/workflows/build-mcsm.yml as .github/workflows/build-mcsm.yml
(I cannot: pushes are rejected with "refusing to allow a GitHub App to create or
update workflow ... without `workflows` permission", and `gh workflow run`
returns 403 "Resource not accessible by integration"). Consequences:
  * ONLY the user's pushes/clicks start a build. My pushes do NOT trigger runs.
    Verified: branch head 9fcce7f produced no run.
  * I cannot read runner logs (results-receiver.actions.githubusercontent.com
    is unreachable from the sandbox); `gh run view --log-failed` EOFs.
  * I cannot create GitHub releases at all (uploads.github.com EOFs on both a
    68 MB jar and a 37 KB zip), but the workflow publishes them from the runner.
First run (33930633043) failed at "Build the jar" (bash ci/build.sh, exit 1) in
~20 s. Hardening applied since:
  1. `set -x` full trace so a red run names the exact command.
  2. client.jar resolved from the LIVE version manifest
     (piston-meta.mojang.com) instead of the pinned object hash -- a stale hash
     404s and kills the build in seconds, which matches the observed timing.
     Falls back to the pinned hash; fallback path tested locally (clean exit).
  3. javac failure is now SURVIVABLE: the jar is still assembled from the old
     classes + current shaders, the first 60 lines of javac output go to
     out/JAVAC_FAILED.txt, and a ::error:: annotation reddens the run. Before,
     one bad java file meant the user got no jar at all.
MIXIN REGISTRY (checked): the jar's mcsm_extras.mixins.json lists 13 mixins but
only 6 have sources in mcsm-extras/java. Seven exist ONLY as compiled .class
files in the jar: McsmCemModelPatch, McsmFoglessEyesPatch, McsmShadowTrackPatch,
McsmSilhouetteWrapPatch, McsmStormBackdropPatch, McsmTeethGlowPatch,
McsmTeethTexturePatch. build.sh overlays and never deletes, so they keep
working -- but they cannot be edited until the sources are recovered.
HONEST INVENTORY (1125 lines of Java, 14 files) -- what the build WILL deliver:
  config-screen fix + McsmExtrasScreen control panel, storm grab/rise fx,
  beacon storm + beacon block, spiral, visibility, shader gate (Iris), blob
  carrier + gradient tick, storm relay fx, diagnostics.
  NOT WRITTEN AT ALL (backlog, not "waiting for a compile" -- do not promise
  these from a build): smoke screen, reality tear + corruption spread, command
  wire / holographic terminal, death shockwave + supernova DRIVER (the shader
  side exists but nothing stamps its carrier), obliterate flash action,
  inventory/HUD move to the side, MCSM structures, MCSM instructions.

### Phase 30f (2026-09-04) — the features were written; the GATES were shut
Two new tools replaced the missing compiler:
  glslcheck/whocalls.py  -- whole-jar invoke scan: is this method ever called?
  glslcheck/apicheck.py  -- verifies every net.dabicco.* symbol our java uses
                            against the shipped jar (public? non-final? exists?)
                            so a typo/bad type is caught here, not on the runner.
  (both sit on glslcheck/whocalls.py's minimal .class parser; no JDK here)

KEY FINDING: the "missing" features already exist and are already wired.
  die()             -> deathBlast(ServerLevel)          (death shockwave)
  addSubGrowth(..)  -> phaseUpShockwave(ServerLevel)    (phase shockwave)
  aiStep(..)        -> tickAmbientBuildingTear(..)      (reality tear/corruption)
DabyWSClientConfig exposes 335 public static non-final fields holding the look
(trailerShadows = ground shadows for terrain+mobs, stormProximityVignette = the
smoke screen, cloudDeckLayer, customSkyboxes, purpleLightningSparks, sunGlow,
blackGlare, stormShadowTerrain...). WitherStormWorldConfig is reached via
WitherStormConfigs.get(Level) and all its interesting fields are public
non-final (buildingDestruction, buildingTearRadius/Interval,
groundShockwaveParticles, structureRaid*, structureTearClusters,
witherSickness, witheredMobs/Max, caveRumble).
So the job was to open gates, not to reimplement.

NEW FILES
  net/mcsm/extras/McsmGate.java      -- forces the MCSM look on (client) and the
    world config on (server), ONCE per session via a static latch. Booleans are
    only ever forced ON; numbers are only RAISED to a floor (never lowered), so
    a player who already turned something up keeps their value. Blanket catch:
    a renamed field after a mod update costs a visual, never a crash.
    World gate is server-only: in single player both sides share one JVM and the
    client's synced copy would be overwritten by the next sync packet.
  net/mcsm/extras/McsmFxDriver.java  -- the visible staging, all vanilla
    particles so nothing desyncs: phase-4/7 rise shockwaves (dust ring + smoke),
    death supernova (6 coloured rings via DustParticleOptions, purple/pink/blue/
    orange/green/yellow, then FLASH + sparks), recovery on death (heal nearby
    players + totem particles = "the tear closes"), purple motes at phase 5.5+,
    dust waves while sweeping, smoke pool under the body.
HOOKS (no new mixin, so no mixin-json registration needed):
  McsmGradientTickPatch (client, per frame)  -> McsmGate.openClient()
  McsmStormGrabPatch    (server, per storm)  -> McsmGate.openWorld(level)
                                              + McsmFxDriver.tick(self, level, gt)
NEW CONFIG: forceMcsmLook, forceMcsmWorld (both default true, in
  config/mcsm_storm_extras.properties).
Gotcha found by apicheck: DabyWSClientConfig.useNewFormidibomb is FINAL -- it
  cannot be assigned. Removed. Re-run apicheck after every java edit.
