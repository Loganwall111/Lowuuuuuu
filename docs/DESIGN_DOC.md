# AEON — Evolution Simulator · AAA Godot 4 Technical Design Document

> **Engine:** Godot 4.4+ (Forward+). **Language:** GDScript. **Target:** native PC build.
> **Scope of this deliverable:** a *production-grade foundation* — modular architecture, data-driven
> species, a working aquatic/terrestrial/space-ready framework, and real, compiling GDScript.

This document is the architecture blueprint plus the implementation that already exists in this
repository. Every script named below is present under `res://scripts/...` and parses cleanly;
every scene under `res://scenes/...` passes a structural validator (all ext_resource paths,
sub_resources and node parents resolve).

---

## 1. DESIGN PHILOSOPHY

The project is engineered as if it were the foundation of a large commercial title:

* **Modular** — systems are managers, components, resources, controllers and scenes, never one
  mega-script.
* **Data-driven** — species, stages, diets, bodies, environments and movement tuning live in
  `Resource` files; adding a species is a content task, not a code change.
* **Decoupled** — gameplay talks through a global `EventBus` (signals only) and via small
  interfaces (`get_camera_basis()`, `simulation_tick()`), never by holding direct references to
  unrelated systems.
* **Scalable** — hundreds of creatures and thousands of food motes are cheap because most
  simulation is *distance-banded* and most food is *not physics*.
* **Cinematic** — every biome owns a `WorldEnvironment` + sun + smooth profile blending; the
  camera supports FP/TP/Cinematic/Free/Inspect/Space with speed-FOV and trauma shake.

The long arc (microscopic → spacefaring) is supported by reusing the same *movement, camera,
input, energy, progression and gravity* primitives across biological and mechanical bodies.

---

## 2. PROJECT ARCHITECTURE

### 2.1 Folder structure

```
res://
├── project.godot                # engine config, input actions, layers, autoloads, renderer
├── icon.svg
├── scenes/
│   ├── main/Main.tscn           # bootstrap: owns the menu, reacts to session flow
│   ├── menu/MainMenu.tscn       # data-driven species menu (3 cards today)
│   ├── player/PlayerPawn.tscn  # the living avatar (CharacterBody3D + components)
│   ├── organisms/PreyCreature.tscn
│   ├── environments/           # AquaticReef, WetlandBiome, ForestBiome, DeepSpaceBiome
│   ├── space/BlackHole.tscn, DebrisBody.tscn
│   └── ui/                     # HUD, EvolutionPanel, PauseMenu, DebugOverlay, SpeciesCard
├── scripts/
│   ├── core/        # EventBus, GameDirector, SceneDirector, SimulationDirector, PoolService,
│   │               # AudioDirector, SettingsService, SaveService, SessionState, MainBootstrap
│   ├── data/        # OrganismData, EvolutionTreeData, EvolutionStageData, AbilityData,
│   │               # DietProfile, MovementProfile, EnvironmentProfile, CreatureBodyRecipe, ...
│   ├── player/      # PlayerPawn, Growth, Metabolism, Evolution, ConsumptionSensor,
│   │               # EdibleComponent, EnvironmentProbe, ProceduralCreatureFactory
│   │   └── locomotion/  # LocomotionController + one file per movement state
│   ├── camera/      # CameraRig + one file per camera mode + CameraShake
│   ├── physics/     # GravityWell3D, GravityWellRegistry, GravityAffected
│   ├── environment/ # EnvironmentDirector, WaterVolume, CurrentVolume, DayNightCycle,
│   │               # TerrainMeshBuilder, ScatterManager, BaseBiome + 4 biomes
│   ├── organisms/   # CreatureAgent, PlanktonField (MultiMesh), SpatialHash
│   ├── ai/          # SteeringKit (boid helpers)
│   ├── space/       # SpaceshipPawn (stub), OrbitalBodyFactory
│   └── ui/          # MainMenuController, SpeciesCard, HUD, EvolutionPanel, PauseMenu, DebugOverlay, ThemeFactory
├── resources/       # generated .tres: organisms/, evolution/, environments/, abilities/, diets/, bodies/, movement/
├── assets/          # drop imported .glb/.png/.wav here (optional — recipes work without them)
├── shaders/         # water_surface, underwater_overlay, organic_skin
└── docs/            # this document
```

**Why this structure (vs. the brief's proposal):** the brief suggested `creatures/` and
`organisms/` as separate roots; I merged creature *logic* under `organisms/` and kept `player/`
for the avatar, because both share the same `CreatureAgent` steering and `EdibleComponent`
contract. I added `physics/` and `space/` as first-class roots so the gravity/reusability and the
future space stage are explicitly supported, not buried.

### 2.2 Autoloads (global services)

| Autoload | Class | Responsibility |
|---|---|---|
| `EventBus` | `event_bus.gd` | Global signal hub (signals only — no state, no logic). |
| `SettingsService` | `settings_service.gd` | Graphics/audio/input config + defensive input-action bootstrap. |
| `PoolService` | `pool_service.gd` | Generic `PackedScene` instance pool (no alloc/free churn). |
| `SimulationDirector` | `simulation_director.gd` | Distance-banded, time-sliced agent ticks (the perf backbone). |
| `AudioDirector` | `audio_director.gd` | Bus layout, pooled 3D one-shots, ambience crossfade, underwater muffling. |
| `SceneDirector` | `scene_director.gd` | Async threaded scene streaming with a cinematic fade veil. |
| `SaveService` | `save_service.gd` | JSON slot persistence of `SessionState`. |
| `GameDirector` | `game_director.gd` | Species catalogue, session ownership, player reference, pause. |

**Communication model:** gameplay code *emits* on `EventBus`; listeners *react*. Example:
`ConsumptionSensor` never imports `HUD` — it emits `prey_consumed`, and `HUD` (listening) updates.
This is why HUD/VFX/audio can all observe the same event without coupling.

### 2.3 Core data flow

```
User picks card (MainMenu)
   └─> GameDirector.start_new_run(organism)
          ├─ builds SessionState (mass/energy/stage/abilities/lineage)
          ├─ sets organism_for_new_run + session (hand-off fields)
          └─> SceneDirector.change_scene_async(organism.starting_environment)
                 └─> Biome._ready() spawns PlayerPawn at SpawnPoint
                        └─> PlayerPawn.configure_organism(...)  ── registers with GameDirector
                              ├─ Growth / Metabolism / Evolution / Locomotion / Camera / Sensor
                              └─ EventBus.session_started  ──> HUD binds pawn, Audio sets bed
```

### 2.4 Performance strategy (the "hundreds of organisms" requirement)

1. **`SimulationDirector`** registers every agent and ticks it at a cadence from distance to the
   focus (the player): near = every physics tick, mid = every 4th, far = every 16th, dormant =
   never + hidden. Because `delta_since_last_tick` is passed in, motion stays correct at any band.
   Band re-classification is itself amortised (`BUDGET_PER_FRAME` agents/frame).
2. **`PlanktonField`** renders thousands of edible motes as a *single `MultiMeshInstance3D`* with a
   bobbing vertex animation — zero physics bodies, zero per-mote scripts. Consumption is a
   `SpatialHash` radius query, not a collision sweep.
3. **`PoolService`** recycles bitemarks/bursts/creatures; no per-event `instantiate/queue_free`.
4. **`CameraRig`/`LocomotionController`** are the *only* per-frame scripts on the pawn; all movement
   states are lightweight `RefCounted` strategies.
5. **`EnvironmentDirector`** blends profiles with a tweened scalar/colour lerp (no second full
   `Environment` instantiated during a dive — the underwater look is a `ColorRect` overlay).

A `DebugOverlay` (F3) exposes live LOD bands, agent counts and gravity-well count so the
architecture is observable, not guesswork.

---

## 3. MAIN MENU (PART 2)

**`MainMenuController`** (`scripts/ui/main_menu_controller.gd`) is fully data-driven:

```gdscript
func _ready() -> void:
    theme = ThemeFactory.new().get_theme()
    _build_cards()

func _build_cards() -> void:
    for species in GameDirector.get_playable_species():   # scanned from resources/organisms
        var card := card_scene.instantiate()
        card.setup(species)                                # renders name/category/world/movement
        card.pressed.connect(_on_card_pressed.bind(species))
        _cards.append(card)

func start_run(species: OrganismData) -> void:
    EventBus.cinematic_caption_requested.emit("Becoming " + species.display_name, species.summary, 2.4)
    GameDirector.start_new_run(species)                   # streams the correct biome
```

**Scene wiring (`MainMenu.tscn`):**

```
MainMenu (Control)  ── script: main_menu_controller.gd
├── Background (ColorRect)
├── VBox
│   ├── Title (Label)            ── title_label
│   ├── Subtitle (Label)         ── subtitle_label
│   ├── CardContainer (HBox)     ── card_container   (cards injected here)
│   ├── SettingsButton (Button)
│   └── QuitButton (Button)
```

**Required setup**
* The three species (`mosquito`, `fish`, `primate`) already exist as `resources/organisms/*.tres`.
* `card_scene` points at `SpeciesCard.tscn`; each card reads `OrganismData` fields only.
* Adding a 4th species = drop a new `OrganismData` .tres; the menu picks it up automatically
  (sorted by `menu_order`). **No code change.**
* Imported creature models go under `assets/models/`; the pawn drops them into `MeshRoot` and
  `ProceduralCreatureFactory` yields (it prefers a present model over a generated placeholder).

---

## 4. PLAYER CONTROLLER (PART 3)

**Why `CharacterBody3D`?** Evolution needs bespoke, organic motion (buoyancy, drag, banking,
undulation, gravity-well pull) that is awkward to express with `RigidBody3D` without fighting the
solver, yet heavier than a raw `Node3D` (we want real collision with terrain/walls).
`CharacterBody3D` + `move_and_slide()` gives deterministic, controllable sliding collision while we
own the entire velocity integration — ideal for a creature that *becomes* many things.

`PlayerPawn` (`scripts/player/player_pawn.gd`) is intentionally **thin**: it composes components and
orchestrates the per-frame loop, but owns no rule.

```
PlayerPawn (CharacterBody3D)        ── player_pawn.gd
├── CollisionShape3D  (capsule; radius/height driven by GrowthComponent)
├── MeshRoot (Node3D)   ── imported .glb OR procedural recipe
├── CameraRig (Node3D)  ── camera_rig.gd  (owns the Camera3D)
├── ConsumptionSensor (Area3D)  ── consumption_sensor.gd
├── EnvironmentProbe (Node)    ── environment_probe.gd
├── GrowthComponent (Node)     ── growth_component.gd
├── MetabolismComponent (Node) ── metabolism_component.gd
├── EvolutionComponent (Node)  ── evolution_component.gd
└── LocomotionController (Node) ── locomotion_controller.gd
```

Per-frame (`_physics_process`):
1. read actions (`consume`/`cycle_camera`/`evolution_panel`),
2. report speed fraction to `MetabolismComponent`,
3. `EvolutionComponent.report_age(...)`,
4. emit `scale_changed` when the body grew,
5. on `consume`, call `sensor.try_consume_nearest()` → on success, grow + feed + evolve + shake.

**Movement is a state machine**, not one big function. `LocomotionController` (`locomotion_controller.gd`):
* holds `velocity` and the active `MovementProfile`,
* integrates **gravity wells** once per tick from the shared `GravityWellRegistry`,
* picks the best *unlocked* state from `EnvironmentProbe` (water/ground/wall/air) + input,
* calls `active_state.physics_step(delta, wish_dir, wish_up, sprint)`.

Each state is a `RefCounted` (`state_*.gd`): `Swim` (organic medium-resisted, buoyant, banked),
`Float`, `Crawl`, `Walk`, `Run` (Walk + sprint), `Fly`, `Climb` (wall-tangent), `SpaceFlight`
(zero-g drift). **Adding a state = one file + one `_register_state()` line.**

The component exposes a stable contract so the camera and movement share one pawn interface:
`camera_basis` (camera-relative), `get_speed_fraction()`, `set_mode()` on the rig.

---

## 5. EVOLUTION SYSTEM (PART 4)

**Foundation (`EvolutionComponent` + `EvolutionTreeData` + `EvolutionStageData` + `OrganismData`):**

* A stage carries `required_mass`, `required_evolution_points`, `required_age_seconds`,
  `body_scale_multiplier`, `energy_capacity`, `base_movement_profile`, `unlocked_abilities`,
  `enabled_movement_states`, and `next_stage_ids`.
* `next_stage_ids` is an **array** → branching is native (Predator / Reef Dweller; Tool User /
  Hunter). A stage with one ready descendant auto-advances; several emit
  `evolution_branch_offered` → `EvolutionPanel` lets the player choose (Spore-style).
* `EvolutionComponent` tracks `evolution_points` (from eating × a per-organism multiplier),
  `age` (from metabolism) and `mass` (from growth). When all gates open it advances, applies the
  new body scale / energy capacity / abilities / movement unlocks, and emits `evolution_stage_changed`
  + a cinematic caption.

`SessionState` (serialisable `RefCounted`) holds the lineage + unlocked abilities/movement so the
run survives stage transitions and saves, while the *engine* of progression lives in the component.

```gdscript
# resources/evolution/fish_tree.tres (excerpt — flat array + id index = O(1) lookup)
stages = [ SubResource("stage_0"), SubResource("stage_1"), SubResource("stage_2"), SubResource("stage_3") ]
# stage_1.next_stage_ids = PackedStringArray("fish_predator", "fish_reef")
```

---

## 6. CONSUMPTION SYSTEM (PART 5)

`ConsumptionSensor` (`consumption_sensor.gd`) turns collisions into meals:

```
Area3D overlap ─> for each edible:
    DietProfile.can_eat(consumer_mass, prey_mass, prey_tags)?   # size + tag rules
    required_ability present?  (e.g. "predator")
    ahead-of-camera? (optional)
        └─> EdibleComponent.try_consume(pawn) -> NutritionPayload
                ├─ remove/hide the target (pooled)
                ├─ emit consumed(payload, target)
                └─ return {mass, energy, evolution_points, tags}
PlayerPawn._on_consumed(payload):
    growth.add_mass -> smooth scale lerp
    metabolism.add_energy
    evolution.grant_nutrition(payload)   # -> may trigger stage advance
    EventBus.prey_consumed + camera shake + audio
```

`EdibleComponent` is attached to **any** consumable (prey, plant, plankton via the field). Growth
is *physical*: `GrowthComponent` lerps `global_scale` toward `target_scale` each frame
(`current = lerp(current, target, 1 - exp(-growth_speed*dt))`), so a creature visibly swells as it
eats while the collision capsule tracks at a fraction (no physics pop).

---

## 7. ENVIRONMENT (PART 6)

Every biome scene contains an **`EnvironmentDirector`** that owns the live `WorldEnvironment` and
sun and applies `EnvironmentProfile`s with smooth cross-fades. Submersion (`WaterVolume`) swaps the
whole mood to an `underwater_profile` and tints the screen via a lightweight `ColorRect`
(`underwater_overlay.gdshader`) — we never pay for a second `Environment` mid-dive.

**Recommended node hierarchy (biome):**
```
BiomeRoot (Node3D)              ── biome_*.gd (extends BaseBiome)
├── WorldEnvironment             ── environment = (Environment)
├── Sun (DirectionalLight3D)    ── shadow casting, rim via second OmniLight optional
├── EnvironmentDirector (Node)  ── surface_profile / underwater_profile
├── Water (Area3D)              ── water_volume.gd  (surface_y, depth_fade)
├── Plankton (MultiMeshInstance3D) ── plankton_field.gd  (thousands of motes, 1 draw call)
├── SpawnPoint (Node3D)
└── (scattered coral/rock/trees via ScatterManager MultiMesh)
```

**Visual profiles** (`resources/environments/*.tres`) cover Deep Ocean, Wetland, Forest and Deep
Space with distinct sky/sun/fog/volumetric/tonemap/grading. `TerrainMeshBuilder` makes a heightmap
ground from `FastNoiseLite`; `ScatterManager` places rocks/coral/trees as a single `MultiMesh`
(thousands of instances, one draw call). `DayNightCycle` arcs the sun and cross-fades dawn→day→
dusk→night for terrestrial biomes.

Because `EnvironmentDirector` and the profiles are data, a designer can add a *Cave* or *Alien
Planet* profile + a biome scene with no engine changes.

---

## 8. BLACK HOLE / GRAVITY WELL (PART 7)

`GravityWell3D` (`scripts/physics/gravity_well.gd`) is a reusable, configurable field:

```gdscript
@export var gravity_strength: float = 120.0
@export var influence_radius: float = 220.0
@export_enum("Inverse Square","Inverse Linear","Linear","Gaussian") var falloff_type: int = 0
@export var max_force: float = 400.0      # clamp prevents instability
@export var softening: float = 2.5        # no 1/0 at r=0  (stable falloff)
@export var event_horizon_radius: float = 3.0

func sample_acceleration(point: Vector3) -> Vector3:
    var to_center := global_position - point
    var dist := to_center.length()
    if dist > influence_radius: return Vector3.ZERO
    var dir := to_center / maxf(dist, 0.0001)
    var mag := minf(_falloff(dist), max_force)        # stable, never infinite
    return dir * mag * (1.0 if attract else -1.0)
```

A single **`GravityWellRegistry`** accumulates active wells. The **player** (`LocomotionController`),
**debris/asteroids** (`GravityAffected` on `RigidBody3D`), and future **spaceships** all call
`registry.sample_acceleration(point)` once per tick and integrate the *same* pull. This satisfies the
brief: "reusable by players, creatures, particles, debris, asteroids, spaceships" with zero
per-type gravity code. Event-horizon crossing can emit `event_horizon_crossed` for scripting.

---

## 9. FUTURE SPACE SYSTEM (PART 8)

The architecture is already compatible:

* **`SpaceshipPawn`** (`scripts/space/spaceship_pawn.gd`) reuses `CameraRig`, `EventBus` vitals and
  the `SPACE_FLIGHT` locomotion state; it samples `GravityWellRegistry` exactly like the creature.
* **`OrbitalBodyFactory`** builds planets/asteroids — a planet is literally a mesh + a
  `GravityWell3D`. `DebrisBody` already proves asteroids bend around a black hole.
* The **same progression/energy/inventory/AI/input** contracts apply: a ship is "a pawn with a
  different MovementProfile and no GrowthComponent", slotted into `LocomotionController` + `CameraRig`.
* `DeepSpaceBiome` already exists as a testbed (starfield `MultiMesh`, `BlackHole`, drifting
  `DebrisBody` asteroids) so the space path is demonstrable today.

No biological controller is forced to "handle everything" — the space stage adds its own pawn and
reuses the shared primitives.

---

## 10. DATA-DRIVEN DESIGN (PART 14)

Everything is a `Resource` or `PackedScene`. `OrganismData` is the top-level, designer-authored
species definition:

```gdscript
class_name OrganismData
extends Resource
@export var species_id: StringName
@export var starting_environment: PackedScene     # which biome
@export var player_scene: PackedScene
@export var body_recipe: CreatureBodyRecipe
@export var evolution_tree: EvolutionTreeData
@export var diet: DietProfile
@export var environment_profile: EnvironmentProfile
# ... mass/growth/abilities/category/summary
```

A designer creates a new organism in the Inspector from this template; `GameDirector` scans
`resources/organisms/` at boot. The `resources/` tree already contains `mosquito`, `fish` and
`primate` (each with bodies, diets, movement profiles, abilities and branching evolution trees).

---

## 11. CODE QUALITY & CONVENTIONS (PART 18)

Every script: Godot 4 syntax, `class_name` where useful, `@export` for tunables, typed variables,
no deprecated 3.x APIs, signals for decoupling, clear comments stating *attach this script to:
Node → child*. Dependencies are stated at the top of each file. Giant monoliths are deliberately
avoided (e.g. locomotion is 9 files, camera is 8, environment is 10).

---

## 12. HOW TO RUN / VERIFY

1. Open `project.godot` in **Godot 4.4+**.
2. (Optional) drop art into `assets/`. Without it, `ProceduralCreatureFactory` generates creatures
   and `TerrainMeshBuilder`/`ScatterManager` generate worlds.
3. F5 → `Main` boots the menu → pick **Mosquito / Fish / Primate** → the correct biome streams in.
4. **Controls:** WASD move, mouse look, Space/Ctrl up-down, Shift sprint, LMB feed/consume, V cycle
   camera, Tab evolution panel, Esc pause, F3 debug overlay.
5. The build is validated by: GDScript parse (all 79 scripts) and a scene/resource structural
   validator (all ext_resource paths, sub_resources and node parents resolve). Open it in the editor
   to confirm engine-level resource wiring; the architecture is editor-ready.

---

## 13. IMMEDIATE NEXT STEPS (recommended, not yet built)

* Real imported `.glb` creatures + normal/roughness/subsurface maps (drop into `MeshRoot`).
* `BiomeWetland`/`BiomeForest` richer prop sets (reeds, grass `MultiMesh`, weather).
* `SpaceshipPawn` combat/docking/FTL hooks; `BlackHole` accretion VFX.
* Audio beds + one-shots wired into `AudioDirector`.
* `EvolutionPanel` richer branching UX (requirements preview, lore cards).

---

## 14. UPDATE LOG — "Living Ecosystem & Survival" (big feature drop)

Built on top of the foundation without breaking any existing system. All new code
is modular and validated.

### 14.1 Predator AI + combat
* `CreatureAgent` (`organisms/creature_agent.gd`) gained predator behaviour: predators **detect** the player (focus), **chase** within `detect_range`, and **bite** when in `attack_range` (cooldown-gated), calling `PlayerPawn.take_damage()` → `Metabolism.damage()` and emitting `player_damaged`. They **flee** if the player is far larger. Biomes now spawn a predator fraction (`make_predator` on `SpawnTableData.SpawnEntry`, applied by `AgentSwarmManager`).
* `PlayerPawn.take_damage()` added; camera shake + `AudioDirector.hurt()` on hit.

### 14.2 Active abilities (Dash / Sonar)
* New `player/ability_controller.gd` component: **Dash** (Q, energy cost + cooldown, velocity impulse) and **Sonar** (R, reveals nearest prey via `EventBus.sonar_ping`). Pattern is extensible — more abilities slot in identically. Input actions `dash`/`sonar` added to `project.godot` + `SettingsService`.

### 14.3 Evolution = visible transformation
* `PlayerPawn.morph_to_stage()` rebuilds the procedural body from the (optionally stage-overridden) recipe, with a white **flash** + brief **slow-motion** (`Engine.time_scale`) so evolving *feels* like a metamorphosis. `EvolutionStageData` gained `silhouette_override`.
* `EvolutionComponent.advance_to()` now morphs the pawn and plays the evolve sting.

### 14.4 Procedural audio (zero asset files)
* `core/procedural_audio_kit.gd` synthesises PCM tones into `AudioStreamWAV` at runtime (eat/evolve/hurt/click/ping/whoosh). `AudioDirector` gained `eat/evolve/hurt/click` wired from consumption, evolution, damage and UI. The game now has sound with **no imported audio**.

### 14.5 Objective + compass HUD
* `ui/hud.gd` shows a live **objective line** (`EvolutionComponent.next_requirement_text()` → "Evolve → X: mass/EP/age") and a **compass arrow** that points to the nearest creature (scans `SimulationDirector`).

### 14.6 Save / Continue
* `GameDirector.resume_run(session)` streams the player back into their saved biome at the saved stage/mass. `MainMenuController` gained a **Continue** button (visible only if a slot-1 save exists), backed by `SaveService`.

### 14.7 Cinematic spawn intro
* On spawn, `PlayerPawn` enters **Cinematic** camera mode for ~3s with a caption, then returns to third-person.

### New/changed files (this drop)
`player/ability_controller.gd`, `core/procedural_audio_kit.gd`, + edits to
`creature_agent`, `player_pawn`, `evolution_component`, `audio_director`,
`event_bus`, `game_director`, `settings_service`, `hud`, `main_menu_controller`,
`procedural_creature_factory`, `base_biome`, `biome_reef`, `spawn_table_data`,
`agent_swarm_manager`, `evolution_stage_data`, `project.godot`, and the
`PlayerPawn`/`HUD`/`MainMenu` scenes.
