"""Agent 3 - Life Director: cities, populations, and AI citizens.

Generates settlements on habitable land, a population of AI citizens modeled
as utility-driven agents with needs (hunger, rest, social, work) and a
behavior selector. Citizens can meet and socialize, forming the social
interaction loop. Designed to scale: the aggregate population is simulated
always; individual citizens are spawned around the player's region.
"""
from __future__ import annotations

import random
from typing import Dict, List

from ..core.state import Entity
from .planet import PlanetEarth


CITY_NAMES = ["Aurora", "Marintide", "Verdania", "Solhaven", "Kestrel",
              "Alderac", "Borealis", "Tashar", "Lume", "Cinderfall",
              "Oakhollow", "Miramar", "Drakemoor", "Sablewood"]


class Citizen:
    """A utility-based AI citizen."""
    NEEDS = ["hunger", "rest", "social", "work"]

    def __init__(self, cid, city_id, rng):
        self.id = cid
        self.city_id = city_id
        self.rng = rng
        self.needs = {n: rng.uniform(0.2, 0.6) for n in self.NEEDS}
        self.action = "idle"
        self.socializing_with = None

    def step(self, dt: float, citizens_by_city: Dict[str, List["Citizen"]]):
        # needs drift up over time (0..1)
        for n in self.NEEDS:
            self.needs[n] = min(1.0, self.needs[n] + 0.05 * dt)
        # pick the most urgent need and act
        top = max(self.NEEDS, key=lambda n: self.needs[n])
        if top == "social":
            peers = citizens_by_city.get(self.city_id, [])
            peers = [p for p in peers if p is not self and p.action != "sleeping"]
            if peers:
                other = self.rng.choice(peers)
                # both gain social and a bit of rest; a social loop
                self.needs["social"] = max(0.0, self.needs["social"] - 0.4)
                other.needs["social"] = max(0.0, other.needs["social"] - 0.4)
                self.socializing_with = other.id
                other.socializing_with = self.id
                self.action = "socializing"
            else:
                self.action = "wandering"
        elif top == "hunger":
            self.needs["hunger"] = max(0.0, self.needs["hunger"] - 0.6)
            self.action = "eating"
        elif top == "rest":
            self.needs["rest"] = max(0.0, self.needs["rest"] - 0.7)
            self.action = "sleeping"
        else:  # work
            self.needs["work"] = max(0.0, self.needs["work"] - 0.5)
            self.needs["hunger"] = min(1.0, self.needs["hunger"] + 0.1 * dt)
            self.action = "working"
        self.socializing_with = None if top != "social" else self.socializing_with

    def to_entity(self) -> Entity:
        return Entity(
            id=self.id, kind="citizen", subsystem="terrestrial",
            position=[0.0, 0.0, 0.0],
            attributes={"city": self.city_id, "action": self.action,
                        "needs": dict(self.needs),
                        "socializing_with": self.socializing_with})


class LifeSimulation:
    def __init__(self, config, state):
        self.config = config
        self.state = state
        self.rng = random.Random(config.seed ^ 0xB3)
        self.cities: Dict[str, dict] = {}
        self.citizens: Dict[str, Citizen] = {}
        self._cid = 0
        self.initialized = False

    def initialize(self, n_cities: int = 8, citizens_per_city: int = 12) -> None:
        # place cities on habitable land
        placed = 0
        tries = 0
        while placed < n_cities and tries < 400:
            tries += 1
            lat = self.rng.uniform(-60, 65)
            lon = self.rng.uniform(-180, 180)
            elev = PlanetEarth.__new__(PlanetEarth)  # cheap static classify call
            # use a lightweight biome check via a temp instance
            tmp = PlanetEarth(self.config, self.state)
            info = tmp.elevation_at(lat, lon)
            biome = info["biome"]
            if biome in ("ocean", "coast", "ice", "snow", "desert", "mountain"):
                continue
            name = CITY_NAMES[placed % len(CITY_NAMES)]
            pop = self.rng.randint(50_000, 4_000_000)
            city_id = f"city:{name}:{placed}"
            self.cities[city_id] = {"name": name, "lat": lat, "lon": lon,
                                    "biome": biome, "population": pop}
            self.state.add(Entity(id=city_id, kind="city", subsystem="terrestrial",
                                  position=[lat, lon, 0.0],
                                  attributes={"name": name, "biome": biome,
                                              "population": pop}))
            for _ in range(citizens_per_city):
                self._cid += 1
                cid = f"citizen:{self._cid}"
                self.citizens[cid] = Citizen(cid, city_id, self.rng)
            placed += 1
        self.initialized = True

    def update(self, dt: float) -> dict:
        if not self.initialized:
            self.initialize()
        by_city: Dict[str, List[Citizen]] = {}
        for c in self.citizens.values():
            by_city.setdefault(c.city_id, []).append(c)
        social_events = 0
        for c in self.citizens.values():
            before = c.action
            c.step(dt, by_city)
            if c.action == "socializing" and before != "socializing":
                social_events += 1
        # sync a sample of citizens into state (cap to keep it bounded)
        sample = list(self.citizens.values())[:64]
        for c in sample:
            self.state.entities[c.id] = c.to_entity()
        actions = {}
        for c in self.citizens.values():
            actions[c.action] = actions.get(c.action, 0) + 1
        return {"cities": len(self.cities),
                "citizens": len(self.citizens),
                "social_events": social_events,
                "actions": actions}
