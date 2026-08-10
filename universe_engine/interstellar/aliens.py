"""Agent 10 - Xeno Curator: alien planets, biomes & civilizations.

Generates alien worlds with unique procedural biomes and civilizations that
have a tech level, government, and a social-interaction loop (trade, diplomacy,
events) that ticks forward. Civilizations can interact (trade routes, war,
alliances) forming a dynamic interstellar society.
"""
from __future__ import annotations

import random
from typing import Dict, List

from ..core.state import Entity
from ..core.math_utils import fbm_2d
import numpy as np


ALIEN_BIOME_POOL = [
    "crystal-forest", "methane-lake", "floating-isles", "lava-plains",
    "bioluminescent-jungle", "ice-veil", "fungus-meadow", "glass-desert",
    "tidal-ocean", "spore-cloud", "magnetic-vortex", "carbon-mesa",
]
GOV_TYPES = ["hive-mind", "council", "ai-collective", "monarchy", "theocracy",
             "corporatocracy", "anarcho-syndicate"]
SPECIES = ["arborials", "aquatides", "skyborne", "lithics", "plasmids",
           "mycelians", "crystallines"]


class AlienCivilization:
    def __init__(self, cid, planet_id, species, gov, tech, rng):
        self.id = cid
        self.planet_id = planet_id
        self.species = species
        self.gov = gov
        self.tech = tech                  # 0..10
        self.population_m = rng.uniform(0.1, 20.0)
        self.relations: Dict[str, str] = {}   # civ_id -> 'ally'|'trade'|'war'|'neutral'
        self.events: List[str] = []
        self.rng = rng

    def step(self, dt: float, others: List["AlienCivilization"]) -> dict:
        # tech & population drift
        self.tech = min(10.0, self.tech + 0.001 * dt * self.population_m)
        self.population_m *= (1 + 0.0002 * dt)
        self.events.clear()
        # social loop: interact with a random neighbor
        if others and self.rng.random() < 0.3 * dt:
            other = self.rng.choice(others)
            rel = self.relations.get(other.id, "neutral")
            if rel == "war":
                if self.tech > other.tech:
                    other.population_m *= 0.999
                    self.events.append(f"victory vs {other.species}")
                else:
                    self.population_m *= 0.999
            elif rel in ("ally", "trade"):
                self.tech = min(10.0, self.tech + 0.01 * dt)
                other.tech = min(10.0, other.tech + 0.01 * dt)
                self.events.append(f"trade with {other.species}")
            else:
                # establish a relation
                new = self.rng.choice(["trade", "ally", "war", "neutral"])
                self.relations[other.id] = new
                other.relations[self.id] = new
                self.events.append(f"diplomacy: {new} with {other.species}")
        return {"tech": self.tech, "pop_m": self.population_m,
                "events": list(self.events)}

    def to_entity(self) -> Entity:
        return Entity(id=self.id, kind="civilization", subsystem="interstellar",
                      position=[0, 0, 0],
                      attributes={"species": self.species, "gov": self.gov,
                                  "tech": self.tech, "pop_m": self.population_m,
                                  "relations": dict(self.relations)})


class AlienCivilizationDB:
    def __init__(self, config, state):
        self.config = config
        self.state = state
        self.rng = random.Random(config.seed ^ 0xD4)
        self.planets: Dict[str, dict] = {}
        self.civs: Dict[str, AlienCivilization] = {}
        self.initialized = False

    def initialize(self, n_planets: int = 6) -> None:
        for i in range(n_planets):
            pid = f"alien_planet:{i}"
            biomes = self.rng.sample(ALIEN_BIOME_POOL, k=self.rng.randint(2, 4))
            self.planets[pid] = {"name": f"Xeno-{i+1}", "biomes": biomes,
                                 "gravity_g": round(self.rng.uniform(0.3, 2.5), 2),
                                 "atm": self.rng.choice(
                                     ["n2-o2", "co2", "ch4", "h2-he", "none"])}
            self.state.add(Entity(id=pid, kind="body", subsystem="interstellar",
                                  position=[self.rng.uniform(-5, 5),
                                            self.rng.uniform(-5, 5),
                                            self.rng.uniform(-5, 5)],
                                  attributes=self.planets[pid]))
            cid = f"civ:{i}"
            self.civs[cid] = AlienCivilization(
                cid, pid, self.rng.choice(SPECIES), self.rng.choice(GOV_TYPES),
                self.rng.uniform(1.0, 7.0), self.rng)
        self.initialized = True

    def update(self, dt: float) -> dict:
        if not self.initialized:
            self.initialize()
        civs = list(self.civs.values())
        total_events = 0
        for c in civs:
            others = [o for o in civs if o is not c]
            r = c.step(dt, others)
            total_events += len(r["events"])
        # sync civs to state
        for c in civs:
            self.state.entities[c.id] = c.to_entity()
        return {"planets": len(self.planets), "civs": len(self.civs),
                "events_this_tick": total_events}

    def planet_biome_map(self, planet_id: str, size: int = 128) -> "Image.Image":
        """Render a 2D biome map for an alien planet using fbm banding."""
        from PIL import Image
        p = self.planets.get(planet_id)
        if not p:
            raise KeyError(planet_id)
        biomes = p["biomes"]
        x = np.linspace(0, 6, size); y = np.linspace(0, 6, size)
        gx, gy = np.meshgrid(x, y)
        field = fbm_2d(gx, gy, octaves=5, seed=self.rng.randint(0, 9999))
        bands = np.clip((field * len(biomes)).astype(int), 0, len(biomes) - 1)
        # simple palette
        palette = np.array([
            [0.3, 0.8, 0.7], [0.1, 0.3, 0.6], [0.6, 0.6, 0.9],
            [0.8, 0.2, 0.1], [0.2, 0.7, 0.3], [0.8, 0.9, 1.0],
            [0.5, 0.4, 0.2], [0.1, 0.5, 0.7], [0.7, 0.5, 0.8],
            [0.9, 0.8, 0.3], [0.2, 0.2, 0.3], [0.4, 0.3, 0.3],
        ])
        col = palette[bands]
        img = (np.clip(col, 0, 1) * 255).astype(np.uint8)
        return Image.fromarray(img, "RGB")
