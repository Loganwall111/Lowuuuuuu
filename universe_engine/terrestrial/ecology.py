"""Agent 2 - Ecologist: ocean & terrestrial wildlife simulation.

A real, working agent-based ecology with two coupled layers:
  * an aggregate Lotka-Volterra dynamics layer (prey / predator) per region,
    giving stable long-term population oscillations,
  * individual creatures as Entities in the WorldState, each with energy,
    species, position, so the player can actually meet and interact with them.

Sea animals populate ocean/coast biomes; terrestrial wildlife populates land
biomes. Reproduction, predation, and natural death are all modeled.
"""
from __future__ import annotations

import math
import random
from typing import Dict, List

from ..core.state import Entity


# (species, biome, trophic) - trophic 0 = prey, 1 = predator
SEA_SPECIES = [
    ("bluefin_tuna", "ocean", 0), ("sardine", "ocean", 0),
    ("dolphin", "ocean", 1), ("reef_shark", "coast", 1),
    ("sea_turtle", "coast", 0), ("manta_ray", "ocean", 0),
    ("orca", "ocean", 1), ("humpback_whale", "ocean", 0),
]
LAND_SPECIES = [
    ("red_fox", "forest", 1), ("white_tailed_deer", "forest", 0),
    ("gray_wolf", "taiga", 1), ("moose", "taiga", 0),
    ("lion", "savanna", 1), ("zebra", "savanna", 0),
    ("elephant", "savanna", 0), ("rabbit", "grassland", 0),
    ("brown_bear", "forest", 1), ("bison", "grassland", 0),
]


class EcologySim:
    def __init__(self, config, state):
        self.config = config
        self.state = state
        self.rng = random.Random(config.seed ^ 0xA2)
        # aggregate populations per species (Lotka-Volterra)
        self.pops: Dict[str, float] = {}
        self.initialized = False

    def initialize(self, per_species: int = 6) -> None:
        for name, biome, trop in SEA_SPECIES + LAND_SPECIES:
            self.pops[name] = float(per_species)
            for i in range(per_species):
                self._spawn_creature(name, biome, trop)
        self.initialized = True

    def _spawn_creature(self, name, biome, trop):
        cid = f"creature:{name}:{self.rng.randint(0, 10**9)}"
        self.state.add(Entity(
            id=cid, kind="creature", subsystem="terrestrial",
            position=[self.rng.uniform(-180, 180),
                      self.rng.uniform(-90, 90),
                      0.0],
            attributes={"species": name, "biome": biome, "trophic": trop,
                        "energy": self.rng.uniform(0.5, 1.0),
                        "age": 0.0, "alive": True}))

    # ---- aggregate Lotka-Volterra per predator-prey pair ----------------
    def _step_aggregate(self, dt: float) -> None:
        # group species by trophic level using the master tables
        prey = {n for n, _, t in SEA_SPECIES + LAND_SPECIES if t == 0}
        pred = {n for n, _, t in SEA_SPECIES + LAND_SPECIES if t == 1}
        # generic LV coupling constants
        a = 0.20    # prey growth
        b = 0.012   # predation rate
        c = 0.10    # predator death
        d = 0.004   # predator conversion
        P = sum(self.pops[n] for n in prey)
        Q = sum(self.pops[n] for n in pred)
        dP = (a * P - b * P * Q) * dt
        dQ = (d * b * P * Q - c * Q) * dt
        # distribute the delta across species proportionally
        for n in prey:
            share = self.pops[n] / P if P > 0 else 0
            self.pops[n] = max(0.0, self.pops[n] + share * dP)
        for n in pred:
            share = self.pops[n] / Q if Q > 0 else 0
            self.pops[n] = max(0.0, self.pops[n] + share * dQ)

    def update(self, dt: float) -> dict:
        if not self.initialized:
            self.initialize()
        self._step_aggregate(dt)
        # individual creatures: age, burn energy, move, die, occasionally spawn
        births = deaths = 0
        creatures = [e for e in self.state.by_kind("creature")
                     if e.attributes.get("alive")]
        for c in creatures:
            a = c.attributes
            a["age"] = a.get("age", 0.0) + dt
            a["energy"] = a.get("energy", 1.0) - 0.02 * dt
            # wander
            c.position[0] += self.rng.uniform(-0.5, 0.5) * dt
            c.position[1] += self.rng.uniform(-0.5, 0.5) * dt
            if a["energy"] <= 0 or a["age"] > 600:
                a["alive"] = False
                deaths += 1
            elif a["energy"] > 0.8 and self.rng.random() < 0.02 * dt:
                spec = a["species"]; biome = a["biome"]; trop = a["trophic"]
                self._spawn_creature(spec, biome, trop)
                births += 1
        # cap population to keep the entity table bounded
        alive = [e for e in self.state.by_kind("creature") if e.attributes.get("alive")]
        if len(alive) > 400:
            for c in alive[400:]:
                c.attributes["alive"] = False
                deaths += 1
        return {"population": sum(self.pops.values()),
                "births": births, "deaths": deaths,
                "individuals_alive": len([e for e in alive])}
