"""The 13-agent swarm definition.

Each agent is a specialized *role* with a fixed id, responsibility, and the
subsystem it primarily owns. Agents 1-10 are cooperative workers driven by the
conductor (Agent 13). Agents 11 (Bug Hunter) and 12 (Optimization Sage) run as
real perpetual background processes. Agent 13 (Swarm Conductor) owns the
data-pipeline that keeps environment state persistent and in sync.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Callable, Dict, List, Optional


@dataclass(frozen=True)
class AgentSpec:
    id: int
    name: str
    role: str
    subsystem: str
    perpetual: bool = False        # True for the always-on background agents

    @property
    def label(self) -> str:
        return f"Agent {self.id:02d} - {self.name}"


SWARM: List[AgentSpec] = [
    AgentSpec(1,  "Terra Architect",     "Procedural & streaming planetary surface",      "terrestrial"),
    AgentSpec(2,  "Ecologist",           "Ocean & terrestrial wildlife simulation",        "terrestrial"),
    AgentSpec(3,  "Life Director",       "Cities, populations, AI citizens",               "terrestrial"),
    AgentSpec(4,  "Mobility Engineer",   "Walk/drive/sail/fly seamless transitions",       "terrestrial"),
    AgentSpec(5,  "Aerospace Architect", "Cockpits, rockets, ascent & orbit insertion",    "aerospace"),
    AgentSpec(6,  "Orbital Dynamics",    "Keplerian & n-body propagation, station-keeping","aerospace"),
    AgentSpec(7,  "Station Keeper",      "ISS geometry, docking, EVA exploration",         "aerospace"),
    AgentSpec(8,  "Stargazer",           "Custom ray-marched renderer & RT backend iface", "interstellar"),
    AgentSpec(9,  "Spacetime Weaver",    "Gravitational lensing, wormholes, warp vectors", "interstellar"),
    AgentSpec(10, "Xeno Curator",        "Alien biomes, civilizations, social loops",      "interstellar"),
    AgentSpec(11, "Bug Hunter",          "Perpetual fault detection, isolation & repair",  "meta",     True),
    AgentSpec(12, "Optimization Sage",   "Perpetual runtime perf profiling & tuning",      "meta",     True),
    AgentSpec(13, "Swarm Conductor",     "Pipeline sync & environment-state persistence",  "meta",     True),
]

BY_ID: Dict[int, AgentSpec] = {a.id: a for a in SWARM}


def describe() -> str:
    lines = ["UniverseEngine Swarm - 13 Specialized Agents", "=" * 48]
    for a in SWARM:
        flag = " [PERPETUAL]" if a.perpetual else ""
        lines.append(f"{a.label:<28} {a.subsystem:<13}{flag}")
        lines.append(f"    {a.role}")
    return "\n".join(lines)


# A registry of in-process worker callables, one per non-perpetual agent,
# invoked by the conductor each tick. Wired up by the conductor at init.
WorkerFn = Callable[[float], Dict]


def default_worker_registry(engine) -> Dict[int, WorkerFn]:
    """Map agents 1-10 to the subsystem update callables they own."""
    t = engine.terrestrial
    a = engine.aerospace
    i = engine.interstellar
    return {
        1: lambda dt: {"terrain_tiles": _call(t["planet"], "update", dt)},
        2: lambda dt: {"ecology": _call(t["ecology"], "update", dt)},
        3: lambda dt: {"life": _call(t["life"], "update", dt)},
        4: lambda dt: {"vehicles": _call(t["vehicles"], "update", dt)},
        5: lambda dt: {"rocket": _call(a["rocket"], "update", dt)},
        6: lambda dt: {"orbit": _propagate_orbits(engine, dt)},
        7: lambda dt: {"iss": _call(a["iss"], "update", dt)},
        8: lambda dt: {"renderer": _call(i["blackhole"], "update", dt)},
        9: lambda dt: {"warp": _call(i["warp"], "update", dt)},
        10: lambda dt: {"aliens": _call(i["aliens"], "update", dt)},
    }


def _call(obj, name, dt):
    fn = getattr(obj, name, None)
    if callable(fn):
        return fn(dt) or {}
    return {}


def _propagate_orbits(engine, dt):
    om = engine.aerospace["orbit"]
    moved = 0
    for e in engine.state.by_kind("body"):
        if "elements" in e.attributes:
            els = e.attributes["elements"]
            els = om.propagate_elements(els, dt)
            e.attributes["elements"] = els
            pos, vel = om.elements_to_state(els, mu=els.get("mu", 3.986e14))
            e.position = list(pos)
            e.velocity = list(vel)
            moved += 1
    return {"bodies_propagated": moved}
