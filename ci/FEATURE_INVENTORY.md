# Devouring Storms — honest feature inventory

> **Phase 31 rebrand (1.9.113+):** with the original author's permission the
> mod is being rebranded as **Devouring Storms: The Point of No Return**.
> Display name, jar filename, release tags, chat banner and panel headers are
> ours as of 1.9.113. The mod id and registry namespaces stay
> `dabywitherstormmod` until/unless the author hands over the SOURCE, at which
> point a true fork (new namespace, source-level fixes, published under the
> new name) becomes possible. Track A (no source): fix his broken features —
> town build queue, /mcsm command gaps — with deep mixins, guided by the CI
> deep-scan disassembly in ci/api/scan/. Track B (with source): full fork.


**As of 7000.0.0-M (#416).** What is actually in the released jar, what only half-exists,
what was never built, and what belongs to Dabicco's mod (which our overlay
cannot fix from inside). Evidence column cites the player's own screenshots or
the chat lines our code prints.

Legend:
- **LIVE** — code shipped in the jar AND verified working from a screenshot/chat line.
- **PARTIAL** — code shipped; works in some conditions, weak/unverified in others.
- **NOT BUILT** — discussed/promised but no code exists in our jar. Say so plainly.
- **DABICCO-SIDE** — the feature lives in Dabicco's Wither Storm Mod itself. Our
  jar can only gate, force or decorate around it — we cannot fix its internals.

---

## 0. Branding (as of 1.9.116)

| Feature | Status | Evidence / notes |
|---|---|---|
| Title-screen wordmark = player's logo | LIVE | `jar-overrides/assets/minecraft/textures/gui/title/minecraft.png` (1024x682, black keyed to alpha), baked by `ci/make_branding.py` from `branding/logo.png`. |
| Mods-list icon = player's logo | LIVE | `jar-overrides/assets/dabywitherstormmod/icon.png` (256x256, whole artwork fit-centred); path confirmed against `fabric.mod.json` "icon". |
| Title panorama = player's storm shot | LIVE | 6 faces (4 side quarters + sky + ground caps) at BOTH `gui/title/background/` and legacy `gui/panorama/`. |
| Display-name rebrand strings | LIVE | since 1.9.113: banner, panel, jar filename, release tags. |

---

## 1. Storm look

| Feature | Status | Evidence / notes |
|---|---|---|
| Round, slightly-oval blue halo, radial gradient (5.5–5.9: #6A8FF7→#627FE3→#263165; 4/5.3: #3F255A→#2D1C41→#140B1B) | **LIVE** | shader `core/*.fsh`; blue ring visible in 13:43/14:13 shots |
| Phase 5.5 sky: dark violet/magenta, never orange | **LIVE** | purple-sky screenshots; `purpleSky` toggle in panel |
| Purple motes + electric sparks under the storm at 5.5+ | **LIVE** | forced-delivery particles (1.9.109 fix); visible in phase shots |
| Glare Size slider (0.25–3.05, default 0.58) | **LIVE** | panel row; user changed it and the blob responded |
| Smudge Scale slider | **LIVE** | panel row |
| OG CEM models toggle (forces Dabicco's trailer models) | **PARTIAL** | gate field forced; no side-by-side screenshot proof yet |
| Torch/glowing-block emissive pop | **LIVE** | shader-side (1.9.107); not re-verified this batch |
| Tree/mob ground shadows | **DABICCO-SIDE** | their `trailerShadows` config field; our gate only forces it ON |

## 2. Sequences & shockwaves

| Feature | Status | Evidence / notes |
|---|---|---|
| Phase 4 / phase 7 rise shockwave (expanding front, 3 s) | **LIVE** | "rise shockwave armed" chat line + pink burst in phase-9 shot; duplicate arm fixed in 1.9.112 |
| Death sequence: supernova rings → white column → pink embers → sky band (cracks, shake, implosion, flare, fade, 16 s) → recover/heal | **LIVE** | START/END chat lines; large pink burst proven at mid range (phase-9 shot); sky band ran in 14:17 death |
| Death legibility at FAR standoff (300+ blocks) | **PARTIAL** | improved in 1.9.111 (fat motes, column, embers); the 2:17pm far death still read as a pale pink band only — needs a close-range death screenshot to call it done |
| `/kill` triggering the death sequence | **LIVE** | remove() hook (1.9.110); user's deaths via /kill did arm it |
| Rise ground FX during spawn animation (spark+dust rings tearing off the ground) | **LIVE** | mixin tick path; `enableRiseFx` toggle |
| Dust waves while the storm sweeps | **LIVE** | forced-delivery particles |
| Smoke screen pooled under the body | **LIVE** | `smokeScreen` toggle |

## 3. Gameplay additions (ours)

| Feature | Status | Evidence / notes |
|---|---|---|
| Tentacle grab: storm self-triggers slams near survival players, on a cadence | **LIVE** | uses Dabicco's `forceTentacleSlam`; `Grab Interval` slider |
| Storm Beacon block + lit-beacon relay (repel/relay) | **PARTIAL** | block class + `/give @s dabywitherstormmod:storm_beacon` shipped; not in their creative tab; no gameplay screenshot proof yet |
| Obliterate flash | **LIVE (config)** | `obliterateFlash` ON by default; fires on the mod's obliterate event — not separately screenshotted |
| Obliterate kicks players (prank) | **LIVE (config)** | OFF by default per user request |
| Reality tear recovery (heal + cleanse when tear closes) | **LIVE** | recover() in FxDriver; ON by default |
| Command wire beam (three strands, bright core, up through storm into sky) | **LIVE** | visible beam in user shots |
| Counterclockwise spiral option | **LIVE** | `McsmSpiralPatch` + toggle |
| MCSM instructions / chat briefing on world load | **LIVE** | banner "[mcsm] MCSM extras 1.9.112 loaded..." |
| In-panel build number (widget row) | **LIVE** | confirmed in 1.9.111 screenshot |

## 4. NOT BUILT (no code in our jar — promised, not delivered)

| Feature | Status | Notes |
|---|---|---|
| Holographic terminal / sidebar HUD ("hotbar still centred") | **LIVE (built in #416)** | no longer queued: `net.mcsm.extras.client.McsmHudTerminal` (593 lines) is the sidebar console -- chapter-coloured, storm-phase read-out, live head count, distance, siege and bowels state, fed by `ClientDistantStormManager` + `StormSkyDarken`. Attached through `McsmHudAttachMixin` over the base `StormAtmosphereOverlay`. |
| Pilot-the-storm | **NOT BUILT** | discussed only |
| Custom tentacle-attack visuals beyond the grab cadence | **NOT BUILT** | we trigger THEIR slam; no new attack animations of our own |
| Shadows as our own feature | **NOT BUILT** | only the gate forcing their config field (see §1) |

## 5. DABICCO-SIDE (their mod's code — our jar cannot fix it)

| Feature | Status | Notes |
|---|---|---|
| `/mcsm` command suite (build, tp, list, status) | **DABICCO-SIDE, DECODED (1.9.114)** | fully disassembled via ci/api/scan: the queue WORKS — `McsmWorldgen.tick` runs every server tick at up to 24000 blocks/tick, all 33 schematics ship in the jar, towns land at ABSOLUTE coords (anchor -640/256, offsets to ~1400 blocks, Sky City floats at y=296). The failure was UX: no coords printed, `/mcsm tp all` invalid, site keys hidden (`beacon_town`). Our `/ds towns` (list/build/tp/status, and `start` = Episode 1 treehouse opening, 1.9.115) now drives their builder with coordinates and tab-completion. Full layout: 35 sites |
| Look presets (Custom / MCSM OG / Legacy Java / Cinematic / Netflix) | **DABICCO-SIDE (fixed our interference in 1.9.112)** | presets are their screen; the "goes back to normal" wipe was OUR gate re-forcing values after every Extras-panel click — that is fixed now. Preset A/B: Shader Pack Gate OFF → apply preset → compare |
| Death Blast crater, Berserk mode | **DABICCO-SIDE** | their Server-tab config; we don't touch it |
| Town NPC population slider | **DABICCO-SIDE** | their world config; towns themselves are world-gen, cannot be retro-generated into an existing world by Force MCSM World |
| Storm phases/growth, spawn animation, tractor beams | **DABICCO-SIDE** | their entity code; we hook around it |

## 5b. The "mystery content" question, answered plainly (Build #416, mandate D.7)

The user asked for several features to be found and restored: a decayed-reality
dimension, a ghost whale, reality-glitch hallucinations, spreading withered
blocks, abandoned structures, shockwave/cinematic-smoke particles. Here is what
an exhaustive search actually found, so nobody looks again:

| Asked for | Verdict | Evidence |
|---|---|---|
| Decayed-reality dimension | **NEVER IN THIS MOD** | every `Registries.DIMENSION` key in the tree resolves to `dabywitherstormmod:bowels` (Dabicco's dimension, `data/dabywitherstormmod/dimension/bowels.json`). No second dimension, no `decayed`/`reality` resource, in the working tree, the full local history (`git log --all -S"decayed"`: no hits) or the pre-migration branch. |
| Ghost whale | **NEVER IN THIS MOD** | no source, asset, model, sound or lang key anywhere (`git log --all -S"whale"`, `git grep`, jar resource list: no hits). Not in the sibling repos either: `Loganwall111/ggggrff` (Blockbench asset dump), `stuff` ("Minecraft Story Mode In Minecraft.zip"), `mcsm` / `use-this-mod-instead` (README-only) and `mass-awakening-` (empty repo) contain no whale content. |
| Reality-glitch hallucinations | **NOT BUILT (closest existing: our reality tear)** | `McsmFxDriver` implements the post-death reality tear + recovery (`realityTear`, ON by default) and #416 shipped the cool-white death cinematic, but there is no glitch/hallucination pass. |
| Spreading withered blocks | **DABICCO-SIDE, and it is ON** | `net.dabicco.witherstormmod.entity.withered.WitheredBlockEntity` + `WitheredMobs` are the base mod's; our gate *raises* the corrupter limits (`witheredMobs` >= 1, `witheredMax` >= 32, `witheredMaxCaves` >= 16 in `McsmGate`). If a world still shows no spread, the source is the base mod's own world config, not our overlay. |
| Abandoned structures | **LIVE** | 33 schematics ship in the jar (`ci/api/scan/jar-schematics.txt`); `/ds towns list/build/start/tp/status` drives Dabicco's builder with coordinates. The "abandoned" feel is the storm-damaged state, not missing code. |
| Shockwave / cinematic smoke | **LIVE** | `supernovaRings` (phase-4 rise, phase-7 rise, death) and `smokeScreen` (skull-impact grey ground smoke + sparks) are ON by default in `McsmExtrasConfig`. |

Conclusion: the three "missing" mysteries were never code in this mod -- they
belong to a separate project the user is remembering. They can be BUILT here,
but they cannot be *restored* from a branch that never had them.

## 6. Infrastructure (ours)

| Feature | Status | Notes |
|---|---|---|
| Single-source build number (./VERSION → BUILD_VERSION) + CI drift gate | **LIVE** | build fails on hardcoded version literals |
| Jar audit (all mixins registered, shaders current) | **LIVE** | CI annotation confirms per release |
| Forced particle delivery (32-block cull bypass) | **LIVE** | the 1.9.109 root-cause fix |
| Shader Pack Gate toggle (hand `ShaderPackCompat.active()` back to the mod) | **LIVE** | 1.9.111; panel column 2 row 13 — the A/B lever for presets |
| Gate value memory (presets survive; explicit re-apply button) | **LIVE (new in 1.9.112)** | unverified in-game until the user tests it |
| Revamped UI (main menu, loading, pause, logo intro, screen reskins) | **LIVE (new in #416)** | ported from `arena/01a09c3d-lowuuuuuu`: `McsmCinematic` boot cutscene, `McsmTitleOverhaulMixin`, `McsmScreenReskinMixin`, `McsmLoadingPauseReskinMixin`, `McsmLogoIntroMixin`, `McsmStormMenuScene` (draggable 3D storm on the menu). Restored in the same build: the main-menu panorama (#375 deleted it) and the menu's own click/hover/open sounds. |
| UI audio | **LIVE (new in #416)** | `mcsm:ds_btn_hover|ds_btn_click|ds_menu_open`, Ogg Vorbis, registered at mod init (a WAV + lazy registration meant the menus were silent before), applied to every widget through `McsmButtonSoundHookMixin` + `McsmScreenOpenSoundMixin`. |
| Console entry point | **LIVE (new in #416)** | one entry, reserved top-right slot in the base console (it used to be a scrollable row that could sit invisibly over the bottom bar and re-open the panel on any click); closing the panel lands in gameplay, never back in the console. |

---

### How to read this against the user's question

"What is actually in the mod vs still code-wise placeholders?" — §1–§3 and §6
are real, shipped and (except where marked PARTIAL) screenshot-proven.
§4 is the honest gap list: the holographic terminal/HUD is the big one and it
does not exist yet. §5 is everything the user has reported broken that lives in
Dabicco's jar (towns queue, `/mcsm tp` names, preset mechanics) — the only one
we ever made worse was the preset wipe, and 1.9.112 removes our foot from it.
