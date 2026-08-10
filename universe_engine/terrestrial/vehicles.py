"""Agent 4 - Mobility Engineer: seamless locomotion transitions.

A single player avatar with a locomotion state machine that transitions
smoothly between on-foot, driving, sailing, and flying globally. The mode is
chosen from the avatar's altitude + the underlying biome/terrain, so a player
can walk to a car, drive to a harbour, sail across an ocean, then fly to orbit
without a loading screen - the core 'seamless universe' requirement.
"""
from __future__ import annotations

from ..core.state import Entity
from .planet import PlanetEarth


class VehicleSystem:
    MODES = ("on_foot", "driving", "sailing", "flying", "orbiting")

    def __init__(self, config, state):
        self.config = config
        self.state = state
        self.planet = PlanetEarth(config, state)
        self.initialized = False

    def initialize(self) -> None:
        self.state.add(Entity(
            id="player", kind="vehicle", subsystem="terrestrial",
            position=[37.7749, -122.4194, 5.0],      # lat, lon, alt_m
            velocity=[0.0, 0.0, 0.0],
            attributes={"mode": "on_foot", "heading_deg": 0.0,
                        "speed_mps": 0.0}))
        self.initialized = True

    @property
    def player(self) -> Entity:
        return self.state.get("player")

    def _terrain_mode(self, lat, lon, alt_m) -> str:
        if alt_m > 100_000:
            return "orbiting"
        if alt_m > 600:
            return "flying"
        info = self.planet.elevation_at(lat, lon)
        biome = info["biome"]
        ground = info["elevation_m"]
        if alt_m < ground + 1 and biome in ("ocean", "coast"):
            return "sailing"
        return "driving" if alt_m < ground + 3 else "on_foot"

    def transition(self) -> str:
        p = self.player
        if p is None:
            return "on_foot"
        lat, lon, alt = p.position
        new = self._terrain_mode(lat, lon, alt)
        old = p.attributes.get("mode", "on_foot")
        if new != old:
            p.attributes["mode"] = new
        return new

    def move(self, dlat: float, dlon: float, dalt: float, dt: float) -> str:
        """Apply a movement intent and resolve the resulting locomotion mode."""
        p = self.player
        if p is None:
            return "on_foot"
        # speed caps per mode (m/s approximated in lat/lon/alt space)
        caps = {"on_foot": 1.4, "driving": 35.0, "sailing": 12.0,
                "flying": 250.0, "orbiting": 7800.0}
        mode = p.attributes.get("mode", "on_foot")
        # allow intent to change altitude (e.g. take off / dive)
        p.position[2] += dalt * dt
        # horizontal intent scaled by mode cap
        scale = caps.get(mode, 1.4) * dt
        p.position[0] += dlat * scale * 1e-4
        p.position[1] += dlon * scale * 1e-4
        p.attributes["speed_mps"] = scale / max(dt, 1e-6)
        return self.transition()

    def update(self, dt: float) -> dict:
        if not self.initialized:
            self.initialize()
        mode = self.transition()
        return {"player_mode": mode,
                "position": list(self.player.position)}
