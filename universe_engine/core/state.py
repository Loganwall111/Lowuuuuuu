"""Persistent environment state.

Agent 13 (Swarm Conductor) serializes this to disk every tick so that every
agent in the swarm shares one consistent view of the universe. It is the
single source of truth for environment-state persistence across the pipeline.
"""
from __future__ import annotations

import json
import os
import time
from dataclasses import dataclass, field, asdict
from typing import Any, Dict, List


@dataclass
class Entity:
    """A generic simulation entity (creature, vehicle, citizen, planet, ...)."""
    id: str
    kind: str                      # 'creature' | 'vehicle' | 'citizen' | 'body' | 'station'
    subsystem: str                 # 'terrestrial' | 'aerospace' | 'interstellar'
    position: List[float] = field(default_factory=lambda: [0.0, 0.0, 0.0])
    velocity: List[float] = field(default_factory=lambda: [0.0, 0.0, 0.0])
    attributes: Dict[str, Any] = field(default_factory=dict)

    def to_dict(self) -> Dict[str, Any]:
        return asdict(self)


@dataclass
class WorldState:
    """The whole universe, serializable to JSON."""
    sim_time: float = 0.0                # seconds of simulated time elapsed
    real_time_started: float = field(default_factory=time.time)
    entities: Dict[str, Entity] = field(default_factory=dict)
    metrics: Dict[str, float] = field(default_factory=dict)
    # Last writer / conductor tick provenance for debugging the swarm pipeline.
    last_sync: Dict[str, Any] = field(default_factory=dict)

    # ---- entity helpers -------------------------------------------------
    def add(self, e: Entity) -> None:
        self.entities[e.id] = e

    def get(self, eid: str) -> Entity | None:
        return self.entities.get(eid)

    def by_kind(self, kind: str) -> List[Entity]:
        return [e for e in self.entities.values() if e.kind == kind]

    def by_subsystem(self, subsystem: str) -> List[Entity]:
        return [e for e in self.entities.values() if e.subsystem == subsystem]

    # ---- persistence ----------------------------------------------------
    def to_dict(self) -> Dict[str, Any]:
        return {
            "sim_time": self.sim_time,
            "real_time_started": self.real_time_started,
            "entities": {k: v.to_dict() for k, v in self.entities.items()},
            "metrics": self.metrics,
            "last_sync": self.last_sync,
        }

    @classmethod
    def from_dict(cls, d: Dict[str, Any]) -> "WorldState":
        ents = {}
        for k, v in (d.get("entities") or {}).items():
            ents[k] = Entity(**v)
        return cls(
            sim_time=d.get("sim_time", 0.0),
            real_time_started=d.get("real_time_started", time.time()),
            entities=ents,
            metrics=d.get("metrics", {}),
            last_sync=d.get("last_sync", {}),
        )

    def save(self, path: str) -> None:
        os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
        # unique tmp name so concurrent writers (multiple agents / processes)
        # do not clobber each other's temp file before os.replace runs.
        import os as _os, time as _time
        tmp = f"{path}.tmp.{_os.getpid()}.{int(_time.time()*1e6)}"
        with open(tmp, "w") as f:
            json.dump(self.to_dict(), f, indent=1)
        _os.replace(tmp, path)   # atomic rename

    @classmethod
    def load(cls, path: str) -> "WorldState":
        if not os.path.exists(path):
            return cls()
        with open(path) as f:
            return cls.from_dict(json.load(f))

    def snapshot_metrics(self) -> Dict[str, float]:
        n = len(self.entities)
        self.metrics = {
            "entity_count": float(n),
            "sim_time_s": self.sim_time,
            "entities_terrestrial": float(sum(
                1 for e in self.entities.values() if e.subsystem == "terrestrial")),
            "entities_aerospace": float(sum(
                1 for e in self.entities.values() if e.subsystem == "aerospace")),
            "entities_interstellar": float(sum(
                1 for e in self.entities.values() if e.subsystem == "interstellar")),
        }
        return self.metrics
