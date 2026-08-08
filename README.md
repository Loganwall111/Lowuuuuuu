# CLONE // MULTI-VOLUME AGENT SWARM

> A Roblox colony-sim remake of the "multi-volume agent swarm" concept (originally built in Babylon.js).
> **35 humanoid clones · 7 named volume sectors · one hive mind.**

![genre](https://img.shields.io/badge/type-roblox%20place-00A2FF) ![agents](https://img.shields.io/badge/agents-35-8a5cff) ![volumes](https://img.shields.io/badge/volumes-7-00dcc8)

Built from scratch as a complete Roblox game: a living swarm of **35 uniquely named clones** in **7 different roles**
(SCOUT · GATHERER · BUILDER · MEDIC · DEFENDER · ENGINEER · COORDINATOR) that autonomously run a base spread across
**7 volume sectors** (VAULT-1, ARC-2 … ARC-7). They mine crystal nodes, haul ore to the depot, raise walls and
turret towers, repair each other, upgrade the depot, explore and secure every sector — and fight off waves of
**Null Tick** hostiles together, using flocking physics and a shared task blackboard.

The whole world — sector floors, holographic signs, neon volume borders, depot, antenna, turret pads, gates,
crystal nodes, every clone and hostile — is **built at runtime by Luau scripts**. Nothing needs to be placed by hand.

---

## 🚀 Quick start

### Option A — no plugins (recommended)
1. Clone/download this repository.
2. Open **`CLONE_MultiVolumeAgentSwarm.rbxlx`** in Roblox Studio
   (`File → Open from file…`, or just double-click it).
3. Hit **Play**. The swarm boots itself: world → structures → 35 clones → hive feed.

### Option B — Rojo workflow
1. Install the [Rojo](https://rojo.space/) Studio plugin (v7.x).
2. Open `default.project.json` via Rojo → serve.
3. Play. Edit anything under `src/` and it hot-reloads.

> The `.rbxlx` place file is generated from the same `src/` tree —
> run `python3 tools/generate_rbxlx.py` to regenerate it after editing scripts.

---

## 🕹️ Commands (type in chat)

| Command | Effect |
|---|---|
| `/wave` | Trigger the next Null Tick wave immediately |
| `/reform` | Recall the whole swarm to VAULT-1 |
| `/report` | Print sector + swarm status into the hive feed |
| `/heal` | Restore every clone to full HP |
| `/god` | Toggle god mode |
| `/swarm <1-35>` | Set the announced swarm size (all 35 still run) |
| `/help` | Show the list in-game |

**Waves also auto-start ~45s after the previous one is repelled.**

---

## 🤖 The 35 agents

| Role | Count | Prefix | Color | Does |
|---|---|---|---|---|
| **Scout** `S` | 5 | SCO-01…05 | cyan | Reveals sectors, roams volumes, marks nodes |
| **Gatherer** `G` | 8 | GAT-01…08 | green | Mines crystal nodes, hauls ore to depot |
| **Builder** `B` | 5 | BLD-01…05 | orange | Raises perimeter walls & turret pads |
| **Medic** `M` | 4 | MED-01…04 | pink | Repairs clones and structures in the field |
| **Defender** `D` | 5 | DEF-01…05 | red | Intercepts Null Ticks, guards the ring |
| **Engineer** `E` | 4 | ENG-01…04 | yellow | Upgrades depot/antenna, helps build turrets |
| **Coordinator** `C` | 4 | CRD-01…04 | violet | Hive-mind units: rebalance sectors, boost nearby clones |

Every clone has a unique name (Echo, Rust, Faber, Salve, Blitz, Cog, Oracle…) and a bio. Click any clone in-game
to inspect it — name, role, HP bar, current sector and position.

## 🗺️ The 7 volumes

```
VAULT-1  Command Core   (home base: depot, antenna, turret pads, spawn)
ARC-2    Crystal Ridge   ·   ARC-3  North Reaches   ·   ARC-4  Ashen Field
ARC-5    Static Drift    ·   ARC-6  Ember Hollow    ·   ARC-7  Mist Basin
```

Each volume has a glowing wireframe border, corner pillars, an overhead holographic sign, its own floor slab,
and a live agent count in the HUD.

---

## 🏗️ Architecture

```
src/
├── ReplicatedStorage/Swarm/          # shared modules
│   ├── Shared.lua                    # roles, 35 unit names, volumes, constants
│   ├── RoleData.lua                  # per-role materials / trim colors
│   ├── CloneModel.lua                # R6 clone factory (cosmetics, tags, HP gui)
│   └── CrystalModel.lua              # crystal node factory + HP bars
├── ServerScriptService/Swarm/        # server brain
│   ├── GameController.server.lua     # bootstraps remotes + swarm controller
│   ├── SwarmController.lua           # the hive mind: agents, tasks, combat, waves
│   ├── SwarmLib.lua                  # world builder, flocking, navigation, damage
│   └── Commands.lua                  # chat command parsing
└── StarterPlayer/StarterPlayerScripts/SwarmClient/
    └── ClientController.client.lua   # HUD: stats, hive feed, volumes, click-inspect
```

See **DESIGN.md** for the full system breakdown (task blackboard, economy, waves, flocking, extension guide).

---

*Inspired by the Babylon.js multi-volume agent swarm demo — reimagined as a playable Roblox colony sim.*
