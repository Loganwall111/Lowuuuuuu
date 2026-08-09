# DEVOURING STORMS — Concept Analysis & Implementation Map

Analysis of the DEVOURING STORMS series material (official trailer, series reveal, and the
MASSG / Decayed Reality body of work it builds on). Every identified concept is listed with its
implementation in the **Java mod** (`devouring_storms`) and the **Bedrock add-on** (`ds:`).

> **Lore anchors used:** "The Mainframe has been breached. The portal is open." · "The system was
> never stable. The Wither Storm blueprints are corrupted. MASSG IS WAKING UP." · "Avoid the silent
> gaze of The Watcher." · "Anna isn't real. This world is an illusion." · "You must destroy the
> storm before it's too late." · "Then the plague came." · "When two rifts open in the sky, light
> flows, but not enough." · "A quarantined world trapped within an endless cycle of destruction,
> rebirth, and corruption." · "I died 2 years after." · "We have been changed forever." ·
> "Rest, my boy. Dream of the ones that came before / waiting for the ships to carry us home."

---

## 1. MASSG — Massive Abomination Sundering Storm Genesis

The ultimate anomaly. A corrupted-blueprint Wither-Storm-class entity that evolves, feeds, and
refuses to die.

| Concept | Java mod | Bedrock add-on |
|---|---|---|
| The storm itself | `MassgEntity` (boss, no-gravity drift, custom model) | `ds:massg` entity (runtime: wither → boss bar + hostility) |
| Phase ladder | `MassgPhase` enum: **SLEEPING** (dormant, "MASSG IS WAKING UP") → **SIGNAL** → **HUNGER** → **DEVOURER** → **SUNDERER** → **GENESIS** (final, world-eater) | component groups + events: `ds:sleeping … ds:genesis` |
| Devouring (tractor beam, eats mobs/blocks, grows) | `DevourPullGoal` + block absorption + `growth` meter driving phase-ups | script-driven pull (`ds:pull` interval logic) + scoreboard growth |
| Devolve on damage + **critical music** | health-threshold devolution shock, spawns Withered Symbionts, `critical` flag flips client music | health events flip `ds:critical` group; script swaps music |
| **Play dead / rebirth** ("blueprints corrupted → it wakes again") | on HP→0: 30 s play-dead, invulnerable; revives at GENESIS unless Formidibombed | `ds:play_dead` event; `true_dead` only via Formidibomb |
| Sundering (splits off living fragments) | SUNDERER phase spawns `SeveredStormEntity` fragments | script spawns `ds:severed_storm` in Sunderer phase |
| Debris vortex + storm lightning (GENESIS) | vortex damage ring, purple lightning strikes | script vortex: ring pull + `summon lightning_bolt` |
| Formidibomb (Story Mode's F-bomb) | `FormidiBombItem` → `FormidiBombEntity` projectile; only true-kill path | `ds:formidibomb` throwable; `projectileHit` script ends the storm |

## 2. The Decayed Reality

| Concept | Java mod | Bedrock add-on |
|---|---|---|
| Quarantined dimension | Custom dimension `devouring_storms:decayed_reality` (datapack dimension+biomes, permanent dead sky) | Quarantine platform in the End dimension (already a rotting void sky) driven by scripts + fog |
| Endless death/reincarnation cycle | `ServerPlayerEvents.AFTER_RESPAWN`: dying inside pulls you back in — *"THE CYCLE CONTINUES."* | `entityDie`/`playerSpawn` script: respawn back on the platform |
| Two rifts in the sky | client sky overlay: two glowing rifts, "light flows, but not enough" (world stays dim) | `ds:fog_decayed_reality` + dark ambience |
| Plague ("then the plague came") | `DecayBlock` spreads, converts terrain, inflicts **Decay** status (custom damage type) | `ds:decay_block` conversion ticks + wither-style effect via script |
| Rotting landscape blocks | Decayed Soil / Decayed Stone / Rot Log / Decay Block | same block palette in the BP |

## 3. The Mainframe — *"breached; the portal is open"*

| Concept | Java mod | Bedrock add-on |
|---|---|---|
| Mainframe structure & terminal | `TerminalBlock` + Mainframe Frame multiblock detection; activation opens the Rift Portal, plays boot/transmission sequence | `ds:terminal` + `ds:mainframe_frame`; script-based pattern check (interact) |
| Classified transmissions / 2027 countdown | Terminal lore broadcasts; advancement **CLASSIFIED UNTIL 2027** for the hidden message | same broadcast lines via `onInteract` script |
| Rift travel | `RiftPortalBlock` (collision teleport) | interacting portal frame teleports to the platform |

## 4. Characters & encounters

| Concept | Java mod | Bedrock add-on |
|---|---|---|
| **The Watcher** (silent gaze; avoid it) | `WatcherEntity`: spawns behind you in darkness, stares; sustained direct gaze → screen corruption + it vanishes (whisper+heartbeat audio) | `ds:watcher` + stalker script loop |
| **Tazo** (you survive *alongside* Tazo) | `TazoEntity` companion: found waiting in the Decayed Reality, tamed with a **Memory Fragment**, follows/fights, speaks trailer lines | `ds:tazo` companion w/ follow goal + chat lines |
| **Anna** ("Anna isn't real. This world is an illusion.") | `AnnaApparitionEntity`: glitching apparition; approaching or staring dissolves it into glitch particles and drops a Memory Fragment | `ds:anna_apparition` + dissolve script |
| **Withered Symbiont** (mutated thrall) | `WitheredSymbiontEntity` — MASSG spawns them when devolving | `ds:withered_symbiont` |

## 5. The corrupted blueprints & progression

1. Explore deepslate structures — **Corrupted Blueprints** appear in Ancient City / Stronghold loot
   ("deep beneath the broken code").
2. Craft the **Corrupted Command Block** (nether star + wither skulls + crying obsidian).
3. Place it, use the Blueprints on it — **the Signal begins; MASSG wakes** (SLEEPING phase).
4. Survive the phases; **destroy the storm before it's too late** — but it outgrows the world.
5. It plays dead. Only the **Formidibomb** ends it. Reward: **Commanded Star** + **Withered Nether Star**.

| Item/Block | Purpose |
|---|---|
| Corrupted Blueprints | summon ritual key / lore |
| Rift Key | completes a Mainframe → opens the portal |
| Formidibomb | the only true kill |
| Watcher Eye, Amulet of Decay, Tendril, Storm Flesh, Decayed Flesh/Bone, Storm Dust | drops/crafting/relics |
| Commanded Star, Withered Nether Star | victory trophies |
| Memory Fragment | Anna's gift; tames Tazo; lore lines |
| Discs: *We Have Been Changed*, *Ships to Carry Us Home* | the trailer's song as Decayed-Jukebox tracks |

## 6. Presentation (analog-horror "shaders")

| Concept | Java | Bedrock |
|---|---|---|
| Built-in shader pack | `shaders/DevouringStormsShaderPack` — Iris/OptiFine GLSL: storm grade, purple decay fog, screen corruption/glitch pass, rift glow, vignette, film grain | n/a (RenderDragon) → replaced by **fog files**, particles, weather cues |
| Vanilla built-in effect | `post_effect` storm-glitch post shader + HUD corruption overlay driven by `StormSync` packets | `/fog` push/pop when near MASSG |
| Phase music + critical switch | client storm-music director (AWAKENING → DEVOUR → SUNDER → GENESIS → CRITICAL) | script `playsound` music cue switches |
| Glitch particles | custom `ds_glitch` particle (Anna, Watcher, Terminal) | `ds:glitch`, `ds:devour_pull`, `ds:rift` particles |

## 7. Parity with Decayed Reality conventions (tribute)

- Entity family `main_wither_storm` is kept on `ds:massg`, and events accept
  `ds:phase_…` / `ds:devour_phase…` / `ds:devolve` names, so server owners used to DR's
  `/event entity @e[family=main_wither_storm] devour_phase1` workflows feel at home.
- Functions shipped: `ds/summon_massg`, `ds/phase_devourer`, `ds/phase_sunderer`,
  `ds/devolve`, `ds/formidibomb_end`, `ds/reset`.

## 8. Endertown & the ruined realm (v1.1)

The DR V2 wiki describes Endertown with its hundreds of banners as the last refuge inside the
quarantine — the modern Devouring Storms trailers keep that imagery (banner towns, dead machines,
scars in the sky). v1.1 builds the whole district into both editions from **one procedural geometry
source** (`tools/generate_structures.py`): Java gets gzipped structure NBT placed by datapack
worldgen; Bedrock (no worldgen API for custom structures) gets script-stamped parity builds.

| Concept | Java | Bedrock |
|---|---|---|
| Endertown, the banner-town | `worldgen/structure/endertown` (jigsaw, single 14k-block template, beard_thin, `spacing 30` in Decayed Wastes) | auto-stamped east of the breach platform on first realm entry |
| ~90 hand-patterned banners | block-entity NBT banners (rhombus/border sigils, Watcher's eye poles) | script limitation (no block-entity NBT) → purple/magenta wool tapestries in the same spots |
| Memorial spire + plaza cache | chest NBT → `loot_table/chests/endertown_plaza` | script chest fill (`BlockInventoryComponent`), same loot table shape |
| Six houses, tall house, Relay Hall, watchtower, stalls, rot-trees | in the town template | in the town build |
| Relay Hall Terminal | `devouring_storms:terminal` block | `ds:terminal` block |
| Watcher Shrine | `worldgen/structure/watcher_shrine` + shrine chest (`watcher_eye` likely) | stamped build + script fill |
| Mainframe Ruin | `worldgen/structure/mainframe_ruin` + `mainframe_rot` block-rot processor; silent Corrupted Command Block pedestal; `corrupted_blueprints` rediscovery chance | stamped build + script fill with same blueprint chance |
| Rift Obelisks | `worldgen/structure/rift_obelisk`; a live `rift_portal` block at the foot = return network | stamped build incl. `ds:rift_portal_block` (same travel script) |
| Rebuild after devouring | n/a (worldgen structures persist) | craftable **Endertown Core** re-raises the town anywhere in the realm |
| Advancements | *Welcome to Endertown*, *Rift Archaeologist*, *He Watches Back*, *The Rifts Remember* | n/a (no Bedrock advancement API) → chat cues via Tazo's town lines |

**Chest loot tables** exist for both editions with matching flavour: `endertown_house`
(supplies + rumour), `endertown_plaza` (the town heart — discs, echo shards, a whisper of a
Commanded Star), `watcher_shrine` (His relics), `mainframe_ruin` (machine salvage + blueprint chance).

## 9. Phase 5.5, the story-mode sky, the vault, and the plague (v1.2)

| Concept | Java | Bedrock |
|---|---|---|
| **The Bowels (phase 5.5)** | `MassgPhase.BOWELS` between SUNDERER and GENESIS: the storm swells (scale 3.0), splits open — new **bowels cube** on the model rendered **emissive fullbright**, hot-magenta pulse tint, witch/reverse-portal glow-bleed particles, devolve-sting + rebirth sting on entry, "THE BOWELS ARE EXPOSED" broadcast | `ds:bowels` event + `ds_bowels` group (scale 2.2), PHASES array insert so growth flows SUNDERER → BOWELS → GENESIS; same broadcast + sounds |
| **Purple glow veins** (MCSM look) | code-tinted emissive layer on the bowels part | always-on **emissive overlay** (`entity_emissive_alpha`) — new `massg_glow.png` vein texture on core+maw bones via a second render controller |
| **MCSM clouds & far thunder** | Iris pack: `gbuffers_skybasic` now boils a rotating **storm-cloud bank** (fbm domain-swirl) with violet underlit bellies; `composite` adds slow **far-thunder pulses** (procedural, weather-independent) | fog files already choke the air; new **`ds:sky_flash`** additive particles + distant roar audio on a sky-flash tick while phase ≥ DEVOURER |
| **EPA / ARG Vault + 7 Schedules** | `sealed_vault` block (OPEN state, two textures) in Endertown's Relay Hall; holds-all-seven use opens it: `M.A.S.S.G.O.O.S` transmission, payload item + commanded star + disc + echo shards + diamonds; hidden challenge advancement (inventory_changed payload); vault-chest loot pools inject schedules into plaza/ruin/shrine caches | same block w/ `ds:open` permutation; script fills plaza/ruin/shrine chests with schedules I–III, anna drop IV, Tazo gift V, MASSG loot VI, severed VII; opening uses inventory scan, consumes 7, transmission + payload title |
| **Infection (corruption mobs)** | `InfectionTicker` (FAPI world tick): MASSG seeds DECAY into hostiles within 40 (phase ≥ HUNGER); soaked monsters convert to **Withered Symbionts** (22% / 35% in the realm). Decay block already spreads terrain + applies the effect | `infectionTick` script: exposure = realm air ∨ storm proximity ∨ standing in decay → convert monster → symbiont; `decaySpreadTick` creeps rot through terrain one block per pulse |

The seven schedule trail (both editions): **I** Endertown plaza cache · **II** Mainframe Ruin ·
**III** Watcher Shrine · **IV** Anna's dissolve (50%) · **V** Tazo's bond-gift (once per companion) ·
**VI** MASSG's true-death loot · **VII** Severed Storm drops.

## 10. The Husk, the Multiverse, and the town that kept praying (v1.3)

| Concept | Java | Bedrock |
|---|---|---|
| **Phase-5.5 rupture cinematic** | `ruptureTick`: SPLIT (smoke/witch + falling decay-block segments) → POUR (streaming violet) → RISE (reeled back up) → SHOCKWAVE ring; entry sting + "And the purple pours on." | `runBowelsCinematic` runTimeout chain: glitch/devour_pull split stages, pour/rise particle columns, 3-ring sky_flash shockwave |
| **The Husk (zombie form)** | `MassgPhase.HUSK`: Genesis downed without a formidibomb → falls grounded, ashen lavender tint, **command block keeps it intact** (`isInvulnerableTo` hard-gate), goals stopped | `ds:husk` event/group (scale 2.2, `damage_sensor` deals_damage=false on all causes), same ashen nameTag + fall broadcast |
| **Storm Killer & the rend** | Watcher loot 25%; `mobInteract` on husk (hole opens in pulses, 120/260 ticks) ×3 strikes → 110-tick `rendTick` white/violet rings → true death + **Storm Heart** drop; hidden advancement *REND THE HEART* | Watcher loot 25%; `playerInteractWithEntity` w/ same hole-window → `rendFinale` chain → remove + storm heart + star/dust/tendril drops |
| **The fog ladder (5 tints)** | StormVisuals `PHASE_FOG` wash: signal teal-blue, hunger/devourer bruised dark blue, sunderer violet, bowels dark-purple + pink undertone, genesis near-black red-rim, husk ash | five new RP fog files (`fog_signal_teal/devourer_blue/sunderer_violet/bowels_pink/genesis_black`), swapped on `setPhase` via fog push/pop |
| **Infinite growth** | Genesis: SCALE +0.08 / 30s up to 6.5, "THE STORM STILL GROWS." | `ds_overgrowth` counter, regen + same whisper |
| **Debris rings** | orbiting ash/reverse-portal ring, radius grows with phase | covered by bowels/shockwave + sky flashes |
| **Watcher paranoia fx** | GAZED effect → heartbeat vignette, blind-frame flashes, cyan/magenta afterimage strips, denser glitch | proximity watcher fog (`fog_watcher`) push/pop + heartbeat |
| **The Multiverse** | new dims **the_fray** + **echo_fields** (dimension + dimension_type jsons), `frayed_tear` block cycles Decayed→Fray→Echo (RiftTravel, dwell+cooldown), advancement *Between Worlds* | pocket realms on the End plane (x=±1000 platforms via `ensurePocket`), tear at each pad cycles the ring with flavor names |
| **The Seventh Trumpet** | `SeventhTrumpetItem#interactLivingEntity`: advances the storm one phase (husk refuses), recipe end_rod+tendril+echo_shard | same interact branch via `playerInteractWithEntity`, same recipe |
| **E.P.A. audio logs ×3** | `AudioLogItem`: use → full E.P.A. field-log text; found in endertown house / ruin / plaza chests | same via itemUse; same chest sources |
| **Endertown NPCs** | `PreacherEntity` (sermons + listener blessing, one at the plaza from the town template — template **entities** in the NBT) + `TownsfolkEntity` (chatter), robes/broom models, spawn eggs, natural realm spawns | `ds:preacher`/`ds:townsfolk` entities×RP/geometry, spawned with the town build (dedup-safe), `preacherTick` sermons + regeneration blessing |

## 11. The Multiverse slate (v1.4)

| Concept | Java | Bedrock |
|---|---|---|
| **Tractor beams (3 heads, MCSM)** | `MassgEntity.tractorBeamTick`: from DEVOURER up, three sockets ring the crown; each head lifts the nearest warm thing toward the maw (suck + fall-damage wipe + glitch filaments + END_ROD). Renderer: three translucent **beam planes** baked pre-faded into the 128-px atlas at UV (104,104), sweep-driven | `tractorBeamTick`: applyImpulse lift toward the storm, glitch filaments at the capture midpoint, chat whisper every 40 ticks. Toggle `tractor_beams` |
| **Earthquakes** | SUNDERER+ every 12–25s: roar pitch 0.32, radial shockwave `push()` on players within 320, LARGE_SMOKE rings ×3 + ASH column, "still feeding" broadcast | `quakeTick`: `camerashake add @s 0.55 1.4 positional`, impulse lift + explosion-dust ring; Toggle `earthquakes` |
| **The belly (stomach interior)** | `ModDimensions.BELLY` (flat void dim) + `StomachChamber`: flying into open bowels (≤2.5 of mouth, BOWELS/GENESIS) teleports you to (0,32,0) chamber — decay_rock walls, decay veins, `corrupted_command_block` on an obsidian dais; 3 Storm-Killer bites (`entityHitBlock`-style use) → core breaks → storm falls as HUSK, you’re exhaled back out | `bellyTick`/`ensureBelly`: chamber stamped at (0,250,1000) in the realm; `entityHitBlock` after-event on the command block w/ per-player hit count; 3rd hit despawns the storm, spawns the husk (reusing the v1.3 husk event) and `dopEject`s you. Toggle `belly` |
| **Void Maw (gravity that eats)** | `VoidMawEntity`: photon-ring particles, slow brownian drift, 26-block pull sphere, kills at 2.2 → **SCALE attribute grows per meal** (max 2.4) | `voidMawTick`: endrod photon ring, impulse pull scaled by `ds_maw_mass`, magic damage at the throat. Toggle `void_maw` + spawn_rules in the End (weight 2, because a maw on every hill would be too many maws) |
| **Storm Mites** | `StormMiteEntity` (fast, 8hp, swarms), decayed_wastes/rot_forest spawner entries (weight 30/22, packs 2–4) | `ds:storm_mite` BP/RP + End spawn_rules weight 30, herds 2–4 |
| **The Taken** | `TheTakenEntity` (24hp, dmg 6, 0.24 speed, knockback resist); **deeper conversion**: realm mobs now rise as Taken instead of symbionts; DECAY-soaked **villagers** convert too (35%×0.35 outer chance) with "a door that will never open again" whisper | `ds:the_taken` BP/RP + villager pass inside `infectionTick` (mirror-chance 5%/2%), spawn_rules weight 14 |
| **Travis & Tonya? (the trapped)** | `Multiverse.ensurePocket` spawns them with each outpost; Travis trades a **Memory Fragment** for his spare **audio_log_2** (`mobInteract`, once per Travis) | pocket spawns in `ensurePocket` + `playerInteractWithEntity` trade mirror; both speak from `TRAVIS_LINES` / `TONYA_LINES` |
| **EAOIN first-line** | Terminals answer ~1/9 uses with an EAOIN line before the menu (idle-terminal AI voice) | (terminal chat lines already exist server-side) |
| **Title cards** | `StormVisuals.tickTitleCard`: phase-change detection → 110-tick card: black band, letterspaced phase name (double-spaced), glitch line, sub-lines per phase; SLEEPING silent; BOWELS gets the "stomach" line; HUSK gets the finale hint | `titleCardForPhase` in `setPhase`: `onScreenDisplay.setTitle` letterspaced + subtitle + ghosted echo update. Toggle `title_cards` (`storm_title_cards` on Java) |
| **Watcher Camp / Rot Cathedral** | two new NBT structure templates from the generator (cult tents + lens rig + watcher/taken; rot-log nave + preacher + taken + reliquary), jigsaw worldgen in quarantine biomes, loot tables + schedules V & VI | both stamped into the realm by the script builder from the regenerated `builds_data.js` tables |
| **Config everything** | `config/devouring-storms.properties`, hot-reloaded: overlay_intensity, fog_ladder, storm_title_cards, watcher_paranoia, bowels_cinematic, debris_rings, earthquakes, infection, infinite_growth, void_maw, stomach_interior | `world.dynamic properties` + `/scriptevent ds:cfg <key> <1|0>` (`list` shows all) |
| **Gravitational lensing (shaders)** | Iris `final.fsh`: precessing maw warps sampling around screen position (Einstein-ring pullback + photon-sphere dark bite), RGB-split + grain + vignette sliders; `gbuffers_skybasic.fsh`: **maw disc in the dome** with photon rim + churn/cover/rift-glow/darkness sliders — nine `DS_*` defines in `shaders.properties` | (Bedrock's pipeline is fog/particles — already covered by the fog ladder) |
