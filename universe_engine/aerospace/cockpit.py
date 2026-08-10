"""Agent 5 (shared) - Walkable cockpits & interiors for aircraft, shuttles, rockets.

Models a cockpit as a set of interactive, clickable control stations the player
can walk up to and operate. Each control maps to an action callback so the
flight model is genuinely driven from the cockpit (manual flight control).
"""
from __future__ import annotations

from typing import Callable, Dict, List


class Control:
    def __init__(self, name, kind, value, lo=0.0, hi=1.0, on_set: Callable | None = None):
        self.name = name; self.kind = kind       # 'lever' | 'switch' | 'dial'
        self.value = value; self.lo = lo; self.hi = hi
        self.on_set = on_set

    def set(self, v):
        self.value = max(self.lo, min(self.hi, v))
        if self.on_set:
            self.on_set(self.value)
        return self.value


class Cockpit:
    """A walkable cockpit: stations the player approaches to operate controls."""
    def __init__(self, craft_name: str, stations: List[dict]):
        self.craft = craft_name
        self.stations: Dict[str, Dict[str, Control]] = {}
        for st in stations:
            self.stations[st["name"]] = {
                c["name"]: Control(c["name"], c.get("kind", "lever"),
                                   c.get("value", 0.0),
                                   c.get("lo", 0.0), c.get("hi", 1.0),
                                   c.get("on_set"))
                for c in st["controls"]}

    def interact(self, station: str, control: str, value) -> bool:
        st = self.stations.get(station)
        if not st or control not in st:
            return False
        st[control].set(value)
        return True

    def walk_to(self, station: str) -> str:
        return f"Player walked to {station} in {self.craft} cockpit"


class CockpitSystem:
    """Factory + registry of walkable cockpits for every piloted craft."""
    def __init__(self):
        self.cockpits: Dict[str, Cockpit] = {}
        self._build_defaults()

    def _build_defaults(self) -> None:
        self.register("cesna_172", [
            {"name": "pilot_seat", "controls": [
                {"name": "throttle", "value": 0.0},
                {"name": "mixture", "value": 1.0},
                {"name": "yoke_pitch", "value": 0.0, "lo": -1, "hi": 1},
                {"name": "yoke_roll", "value": 0.0, "lo": -1, "hi": 1},
                {"name": "rudder_pedals", "value": 0.0, "lo": -1, "hi": 1}]},
            {"name": "copilot_seat", "controls": [
                {"name": "flaps", "value": 0.0, "lo": 0, "hi": 1}]},
            {"name": "overhead", "controls": [
                {"name": "landing_gear", "kind": "switch", "value": 1},
                {"name": "nav_lights", "kind": "switch", "value": 0}]},
        ])
        self.register("space_shuttle", [
            {"name": "commander_seat", "controls": [
                {"name": "throttle", "value": 0.0},
                {"name": "rot_hand_controller", "value": 0.0, "lo": -1, "hi": 1},
                {"name": "trans_hand_controller", "value": 0.0, "lo": -1, "hi": 1}]},
            {"name": "aft_flight_deck", "controls": [
                {"name": "oms_burn", "kind": "switch", "value": 0},
                {"name": "payload_bay_doors", "kind": "switch", "value": 0}]},
        ])
        self.register("falcon_heavy", [
            {"name": "crew_dragon_seat", "controls": [
                {"name": "throttle", "value": 0.0},
                {"name": "abort_handle", "kind": "switch", "value": 0},
                {"name": "touchscreen_mode", "value": 0.0, "lo": 0, "hi": 2}]},
        ])

    def register(self, craft_name: str, stations: List[dict]) -> None:
        self.cockpits[craft_name] = Cockpit(craft_name, stations)

    def get(self, craft_name: str) -> Cockpit | None:
        return self.cockpits.get(craft_name)
