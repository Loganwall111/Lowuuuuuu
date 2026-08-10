"""UniverseEngine - top-level orchestrator.

Wires the four simulation modules (terrestrial, aerospace, interstellar) and
the 13-agent swarm onto a shared, persistent WorldState. This is the object a
game loop / host application drives.
"""
from __future__ import annotations

import time
from typing import Optional

from .config import EngineConfig
from .state import WorldState
from ..swarm.conductor import SwarmConductor


class UniverseEngine:
    def __init__(self, config: Optional[EngineConfig] = None,
                 state: Optional[WorldState] = None):
        self.config = config or EngineConfig()
        self.state = state or WorldState.load(self.config.state_path)

        # Subsystems are imported lazily to keep import cost low and avoid
        # pulling numpy into pure-management code paths.
        from ..terrestrial.planet import PlanetEarth
        from ..terrestrial.ecology import EcologySim
        from ..terrestrial.life_sim import LifeSimulation
        from ..terrestrial.vehicles import VehicleSystem
        from ..aerospace.orbit import OrbitalMechanics
        from ..aerospace.rocket import RocketSim
        from ..aerospace.iss import ISSEnvironment
        from ..aerospace.cockpit import CockpitSystem
        from ..interstellar.blackhole import BlackHole
        from ..interstellar.warp import WarpNetwork
        from ..interstellar.aliens import AlienCivilizationDB

        self.terrestrial = {
            "planet": PlanetEarth(self.config, self.state),
            "ecology": EcologySim(self.config, self.state),
            "life": LifeSimulation(self.config, self.state),
            "vehicles": VehicleSystem(self.config, self.state),
        }
        self.aerospace = {
            "orbit": OrbitalMechanics(),
            "rocket": RocketSim(self.config, self.state),
            "iss": ISSEnvironment(self.config, self.state),
            "cockpit": CockpitSystem(),
        }
        self.interstellar = {
            "blackhole": BlackHole(),
            "warp": WarpNetwork(self.config, self.state),
            "aliens": AlienCivilizationDB(self.config, self.state),
        }

        # The swarm conductor (Agent 13) coordinates all 13 agents and owns
        # the persistent-state sync pipeline.
        self.conductor = SwarmConductor(self.config, self.state, self)

        self._last_real = time.time()
        self.running = False

    # ---- lifecycle ------------------------------------------------------
    def initialize(self) -> None:
        """Seed the world with starter content from each subsystem."""
        for sub in (self.terrestrial, self.aerospace, self.interstellar):
            for mod in sub.values():
                init = getattr(mod, "initialize", None)
                if callable(init):
                    init()
        self.state.snapshot_metrics()
        self.state.save(self.config.state_path)

    def step(self, dt_real: Optional[float] = None) -> None:
        """Advance the simulation by one frame."""
        now = time.time()
        if dt_real is None:
            dt_real = now - self._last_real
        self._last_real = now
        dt_real = min(dt_real, self.config.max_simulation_dt)
        dt_sim = dt_real * self.config.time_scale

        # substep physics for stability
        n = max(1, self.config.physics_substeps)
        h = dt_sim / n
        for _ in range(n):
            for mod in self.terrestrial.values():
                upd = getattr(mod, "update", None)
                if callable(upd):
                    upd(h)
            for mod in self.aerospace.values():
                upd = getattr(mod, "update", None)
                if callable(upd):
                    upd(h)
            for mod in self.interstellar.values():
                upd = getattr(mod, "update", None)
                if callable(upd):
                    upd(h)
            self.state.sim_time += h

        self.state.snapshot_metrics()

    def run_background_swarm(self) -> None:
        """Launch Agents 11 & 12 as real perpetual background loops."""
        if self.config.enable_background_agents:
            self.conductor.launch_background_agents()

    def stop_background_swarm(self) -> None:
        self.conductor.stop_background_agents()

    # ---- introspection --------------------------------------------------
    def status(self) -> dict:
        return {
            "version": "0.1.0-phase1",
            "sim_time_s": self.state.sim_time,
            "entities": len(self.state.entities),
            "metrics": self.state.metrics,
            "swarm": self.conductor.status(),
        }
