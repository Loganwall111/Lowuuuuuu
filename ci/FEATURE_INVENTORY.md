# MCSM Extras — honest feature inventory

**As of 1.9.112.** What is actually in the released jar, what only half-exists,
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
| Holographic terminal / sidebar HUD ("hotbar still centred") | **NOT BUILT** | no HUD render code exists; repeatedly requested, still queued |
| Pilot-the-storm | **NOT BUILT** | discussed only |
| Custom tentacle-attack visuals beyond the grab cadence | **NOT BUILT** | we trigger THEIR slam; no new attack animations of our own |
| Shadows as our own feature | **NOT BUILT** | only the gate forcing their config field (see §1) |

## 5. DABICCO-SIDE (their mod's code — our jar cannot fix it)

| Feature | Status | Notes |
|---|---|---|
| `/mcsm` command suite (build, tp, list, status) | **DABICCO-SIDE** | the strings "Queued 1 location(s)…", "No location called 'all'. Try /mcsm list." do not exist anywhere in our sources |
| Story-Mode town build queue (queued but nothing spawned after ~9 min) | **DABICCO-SIDE** | their builder; likely needs the town chunks loaded / player near the queued location, or their queue is broken in this world. We can only advise, not patch |
| Look presets (Custom / MCSM OG / Legacy Java / Cinematic / Netflix) | **DABICCO-SIDE (fixed our interference in 1.9.112)** | presets are their screen; the "goes back to normal" wipe was OUR gate re-forcing values after every Extras-panel click — that is fixed now |
| Death Blast crater, Berserk mode | **DABICCO-SIDE** | their Server-tab config; we don't touch it |
| Town NPC population slider | **DABICCO-SIDE** | their world config; towns themselves are world-gen, cannot be retro-generated into an existing world by Force MCSM World |
| Storm phases/growth, spawn animation, tractor beams | **DABICCO-SIDE** | their entity code; we hook around it |

## 6. Infrastructure (ours)

| Feature | Status | Notes |
|---|---|---|
| Single-source build number (./VERSION → BUILD_VERSION) + CI drift gate | **LIVE** | build fails on hardcoded version literals |
| Jar audit (all mixins registered, shaders current) | **LIVE** | CI annotation confirms per release |
| Forced particle delivery (32-block cull bypass) | **LIVE** | the 1.9.109 root-cause fix |
| Shader Pack Gate toggle (hand `ShaderPackCompat.active()` back to the mod) | **LIVE** | 1.9.111; panel column 2 row 13 — the A/B lever for presets |
| Gate value memory (presets survive; explicit re-apply button) | **LIVE (new in 1.9.112)** | unverified in-game until the user tests it |

---

### How to read this against the user's question

"What is actually in the mod vs still code-wise placeholders?" — §1–§3 and §6
are real, shipped and (except where marked PARTIAL) screenshot-proven.
§4 is the honest gap list: the holographic terminal/HUD is the big one and it
does not exist yet. §5 is everything the user has reported broken that lives in
Dabicco's jar (towns queue, `/mcsm tp` names, preset mechanics) — the only one
we ever made worse was the preset wipe, and 1.9.112 removes our foot from it.
