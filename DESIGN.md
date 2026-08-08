# Design — CLONE // MULTI-VOLUME AGENT SWARM

This document explains how the game works internally and how to extend it.

## 1. Core loop

1. `GameController.server.lua` starts, creates the remotes, and boots `SwarmController`.
2. `SwarmController:start()`:
   - registers physics collision groups,
   - builds the world (`SwarmLib.buildWorld`) — 7 sector floors, neon volume borders, signs, lighting, spawn pad,
   - builds base structures (depot, antenna, 4 turret pads, 4 gate walls),
   - seeds 8 crystal nodes in sectors ARC-2…ARC-7,
   - spawns all 35 clones via the shared `CloneModel` factory,
   - starts a `RunService.Heartbeat` loop that drives everything.
3. Each heartbeat:
   - respawns dead clones, updates sector membership (every 0.5 s),
   - runs every clone's role brain,
   - runs Null Tick (hostile) AI + turret AI,
   - processes mining/repair ticks, the wave director, and stats broadcasts to clients.

## 2. The hive mind: task blackboard

Tasks live in `SwarmController.Tasks` (a simple queue with `claimedBy` ownership):

| Task | Created by | Consumed by |
|---|---|---|
| `MINE` | `refreshTasks` for every un-depleted node | Gatherers |
| `BUILD` | `refreshTasks` for un-built wall/turret slots | Builders (+ Engineers fallback) |
| `HAUL` | `depleteNode` (assigned straight to the miner) | the miner |
| `REPAIR` | Medics scan for injured clones/structures (not queued) | Medics |
| `UPGRADE` | Engineers when stock ≥ cost (not queued) | Engineers |
| `FIGHT` | any clone that senses a hostile in range | that clone |

Claim scoring favors same-sector tasks and nearby tasks, so the swarm spreads across volumes instead of
clumping on one node. Stale/complete tasks are pruned every refresh; dead or interrupted agents release
their claims and build slots.

## 3. Role behaviors

- **Scout** — walks to unexplored sectors; on arrival marks `sec.explored = true` and feeds the hive. Then roams.
- **Gatherer** — claims `MINE`, walks to the node, mines (heartbeat tick applies damage + spark FX), and on
  depletion `depleteNode` pays crystals, beams the ore home, and hands the miner a `HAUL` task to the depot.
- **Builder** — claims `BUILD`, walks to the slot, shows a holographic scaffold while `progress` builds up,
  then spawns a wall or turret (deducting stock).
- **Medic** — scans clones (≤ 85% HP) then structures (≤ 90% HP), walks over, and heals 14 HP per repair tick.
- **Defender** — senses hostiles at 65 studs (others at 30), engages (ranged at 26 studs), else patrols the ring.
- **Engineer** — upgrades the Depot (max lvl 4, raises `Level` = payout multiplier) or Antenna when enough
  crystals are stockpiled; otherwise helps Builders.
- **Coordinator** — travels between sector cores, pings the ground with expanding rings, boosts clone speed
  within 14 studs, and redirects the swarm toward empty unexplored sectors.

Flocking (separation/cohesion/alignment) is applied in `SwarmLib.moveToward`, plus a cheap raycast detour
that steers around walls.

## 4. Combat & waves

- **Null Ticks**: dark humanoids with red eyes, spawned by `startWave`. Count = `4 + wave*3`, HP = `55 * (1 + (wave-1)*0.55) + 10`.
- They pick the nearest clone (or structure if far away), chase, and slash every 1 s. Kills drop 4 crystals.
- **Turrets** (built by Engineers on pads): 50-stud range, 12 dmg/0.6 s with laser FX.
- Wave clears → +`10 + wave*5` crystals, next wave auto-starts after 45 s. `/wave` forces it.

## 5. Economy

- Start stock: 60 crystals.
- Node payout: `12 + Level*3`. Kill scrap: 4.
- Costs: wall 30, turret 60, depot/antenna upgrade `100 + level*50`.
- Depot level raises mining payout; structures gain HP per level.

## 6. Respawn

Dead clones respawn at the VAULT-1 spawn pad after 7 s with full HP, keeping role, unit name, and index.
The HUD's swarm-online counter and per-sector counts reflect it.

## 7. Networking / HUD

- `StatsBroadcast` (~2.5 Hz): crystals, level, wave, swarm alive/total, hostiles, turrets, per-volume counts.
- `TaskBroadcast` (sparse): unclaimed tasks (kind + volume) shown in the right panel.
- `FeedBroadcast`: last events, color-coded by system, rendered in the hive feed.
- Click-to-inspect is fully client-side: clones expose `Role`, `CloneId`, `Hp`, `MaxHp`, `DisplayName` attributes.

## 8. Extending

- **Add a role**: add it to `Shared.Roles` (+ `RoleOrder`, `UnitNames`, `RoleData`), give it a branch in
  `SwarmController:autonomous`, and bump counts — the spawner and HUD pick it up automatically.
- **Add a volume**: append to `Shared.Volumes`; the world builder, signs, borders and HUD handle the rest.
- **Add a command**: add a branch in `Commands.handle` + a `SwarmController` method.
- **Tune balance**: all numbers live near the top of the relevant functions in `SwarmController.lua` /
  `SwarmLib.lua` / `Shared.lua`.

## 9. File map

| File | Responsibility |
|---|---|
| `default.project.json` | Rojo project definition |
| `CLONE_MultiVolumeAgentSwarm.rbxlx` | Standalone place file (regenerate with `tools/generate_rbxlx.py`) |
| `tools/generate_rbxlx.py` | Converts `src/` → `.rbxlx` |
| `src/ReplicatedStorage/Swarm/Shared.lua` | Roles, unit names, volumes, shared helpers |
| `src/ReplicatedStorage/Swarm/CloneModel.lua` | Clone body/tag/HP factory |
| `src/ReplicatedStorage/Swarm/CrystalModel.lua` | Crystal node factory |
| `src/ServerScriptService/Swarm/SwarmController.lua` | Everything the hive does |
| `src/ServerScriptService/Swarm/SwarmLib.lua` | World, flocking, nav, damage |
| `src/StarterPlayer/…/ClientController.client.lua` | HUD + click inspect |
