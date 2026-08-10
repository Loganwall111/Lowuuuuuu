"""Agent 7 - Station Keeper: the International Space Station.

A walkable, dockable, explorable station. Models the real ISS module layout
(nodes, labs, docking ports) as a graph the player can traverse on an EVA or
IVA path, plus docking-port geometry for capsule rendezvous.
"""
from __future__ import annotations

from ..core.state import Entity
from .orbit import OrbitalMechanics


# A simplified but faithful ISS module graph. Each node: name, role, neighbors.
# Keys are short stable ids; neighbors reference those same ids.
ISS_MODULES = {
    "Unity":      {"role": "node",    "neighbors": ["Harmony", "Zarya", "Quest"]},
    "Harmony":    {"role": "node",    "neighbors": ["Unity", "Destiny", "Kibo", "Columbus"]},
    "Zarya":      {"role": "module",  "neighbors": ["Unity", "Zvezda"]},
    "Zvezda":     {"role": "module",  "neighbors": ["Zarya"]},
    "Destiny":    {"role": "lab",     "neighbors": ["Harmony"]},
    "Columbus":   {"role": "lab",     "neighbors": ["Harmony"]},
    "Kibo":       {"role": "lab",     "neighbors": ["Harmony"]},
    "Quest":      {"role": "airlock", "neighbors": ["Unity"]},
}

DOCKING_PORTS = [
    {"id": "port-fwd",  "node": "Harmony", "craft": None},
    {"id": "port-aft",  "node": "Zarya",   "craft": None},
    {"id": "port-zen",  "node": "Unity",   "craft": None},
]


class ISSEnvironment:
    def __init__(self, config, state):
        self.config = config
        self.state = state
        self.initialized = False

    def initialize(self, altitude_m: float = 408_000.0) -> None:
        from ..core.math_utils import R_EARTH
        self.state.add(Entity(
            id="iss", kind="station", subsystem="aerospace",
            position=[0.0, R_EARTH + altitude_m, 0.0],
            velocity=[OrbitalMechanics.circular_orbit_velocity(R_EARTH + altitude_m), 0.0, 0.0],
            attributes={"modules": list(ISS_MODULES.keys()),
                        "docking_ports": [p["id"] for p in DOCKING_PORTS],
                        "crew": 7, "altitude_km": altitude_m / 1000.0}))
        self.initialized = True

    @property
    def station(self) -> Entity:
        return self.state.get("iss")

    def dock(self, port_id: str, craft_id: str) -> bool:
        for p in DOCKING_PORTS:
            if p["id"] == port_id and p["craft"] is None:
                p["craft"] = craft_id
                return True
        return False

    def undock(self, craft_id: str) -> bool:
        for p in DOCKING_PORTS:
            if p["craft"] == craft_id:
                p["craft"] = None
                return True
        return False

    def board(self, port_id: str) -> str:
        """Transition a docked player from capsule into the station interior."""
        port = next((p for p in DOCKING_PORTS if p["id"] == port_id), None)
        if port and port["craft"]:
            return f"Entered ISS at {port['node']} via {port_id}"
        return f"Nothing docked at {port_id}"

    def traverse(self, frm: str, to: str) -> list:
        """BFS path through the module graph (walkable interior route)."""
        from collections import deque
        if frm not in ISS_MODULES or to not in ISS_MODULES:
            return []
        q = deque([[frm]]); seen = {frm}
        while q:
            path = q.popleft()
            if path[-1] == to:
                return path
            for nb in ISS_MODULES[path[-1]]["neighbors"]:
                if nb not in seen:
                    seen.add(nb); q.append(path + [nb])
        return []

    def eva(self, from_node: str = "Quest") -> dict:
        """Begin an extravehicular activity from the airlock."""
        return {"eva": True, "egress": from_node,
                "suit": "EMU", "oxygen_min": 8.5}

    def update(self, dt: float) -> dict:
        if not self.initialized:
            self.initialize()
        s = self.station
        # simple two-body propagation of the station's orbit
        from .orbit import OrbitalMechanics
        from ..core.math_utils import vec3
        pos = vec3(*s.position); vel = vec3(*s.velocity)
        els = OrbitalMechanics.state_to_elements(pos, vel)
        els = OrbitalMechanics.propagate_elements(els, dt)
        p, v = OrbitalMechanics.elements_to_state(els)
        s.position = list(p); s.velocity = list(v)
        return {"orbit_a_km": els["a"] / 1000.0, "orbit_e": els["e"]}
