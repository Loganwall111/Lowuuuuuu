"""Agent 5 - Aerospace Architect: rocket ascent & orbit insertion.

A 2-D central-force ascent simulator (proper Newtonian gravity toward Earth's
center, not a flat -y approximation) with realistic staging, an exponential
atmosphere, a gravity-turn pitch program, and a guidance loop that coasts to
apoapsis and circularizes. The player can pilot manually via step(); the
auto-pilot demonstrates a full surface-to-orbit flight and reports the
resulting Keplerian orbit.
"""
from __future__ import annotations

import math

from ..core.state import Entity
from ..core.math_utils import MU_EARTH, R_EARTH, G0
from .orbit import OrbitalMechanics


# Three-stage vehicle (booster / sustainer / OMS) with enough delta-v for LEO.
# Tsiolkovsky budget ~ 13 km/s, comfortably above the ~9.4 km/s LEO cost.
# Upper-stage thrust is sized so horizontal orbital speed builds without the
# vehicle coasting too high before cutoff.
STAGES = [
    {"dry": 4000.0, "prop": 24000.0, "thrust": 1_300_000.0, "isp": 285.0},
    {"dry": 2000.0, "prop": 12000.0, "thrust": 600_000.0,   "isp": 348.0},
    {"dry": 600.0,  "prop": 1800.0,  "thrust": 150_000.0,   "isp": 320.0},
]


class RocketSim:
    def __init__(self, config, state):
        self.config = config
        self.state = state
        self.initialized = False

    def initialize(self) -> None:
        prop = [s["prop"] for s in STAGES]
        self.state.add(Entity(
            id="rocket:arrow", kind="vehicle", subsystem="aerospace",
            # 2D Cartesian state relative to Earth's center; start at equator.
            position=[0.0, R_EARTH, 0.0],
            velocity=[0.0, 0.0, 0.0],
            attributes={"throttle": 1.0, "pitch_deg": 90.0, "stage_idx": 0,
                        "prop_remaining": prop, "stages": STAGES,
                        "cd": 0.3, "area_m2": 12.0}))
        self.initialized = True

    @property
    def rocket(self) -> Entity:
        return self.state.get("rocket:arrow")

    @staticmethod
    def _air_density(alt: float) -> float:
        return 1.225 * math.exp(-max(alt, 0.0) / 8500.0)

    def _mass(self) -> float:
        a = self.rocket.attributes
        idx = a["stage_idx"]
        m = 0.0
        for i in range(idx, len(STAGES)):
            m += STAGES[i]["dry"] + a["prop_remaining"][i]
        return m

    def step(self, dt: float, throttle: float, pitch_deg: float) -> dict:
        r = self.rocket
        if r is None:
            return {}
        a = r.attributes
        idx = a["stage_idx"]
        if idx >= len(STAGES):
            throttle = 0.0
            stage = {"thrust": 0.0, "isp": 1.0}
        else:
            stage = STAGES[idx]
        mass = self._mass()
        x, y = r.position[0], r.position[1]
        vx, vy = r.velocity[0], r.velocity[1]
        rmag = math.hypot(x, y) or 1.0
        alt = rmag - R_EARTH
        # local vertical (up) and prograde-horizontal basis
        upx, upy = x / rmag, y / rmag
        hx, hy = upy, -upx                      # +90deg rotation of up
        pitch = math.radians(pitch_deg)         # pitch from horizontal
        thrust = stage["thrust"] * throttle
        fx = thrust * (math.sin(pitch) * upx + math.cos(pitch) * hx)
        fy = thrust * (math.sin(pitch) * upy + math.cos(pitch) * hy)
        # central gravity (already an acceleration, m/s^2 - NOT divided by mass)
        g_over_r = MU_EARTH / (rmag * rmag * rmag)
        gx = -g_over_r * x
        gy = -g_over_r * y
        # drag (a force, N)
        vmag = math.hypot(vx, vy)
        rho = self._air_density(alt)
        drag = 0.5 * rho * vmag * vmag * a["cd"] * a["area_m2"]
        if vmag > 0:
            dx = -drag * vx / vmag
            dy = -drag * vy / vmag
        else:
            dx = dy = 0.0
        # thrust (force) + drag (force) are divided by mass; gravity is not.
        ax = (fx + dx) / mass + gx
        ay = (fy + dy) / mass + gy
        vx += ax * dt; vy += ay * dt
        x += vx * dt; y += vy * dt
        r.velocity[0], r.velocity[1] = vx, vy
        r.position[0], r.position[1] = x, y
        # consume propellant; stage when empty
        if throttle > 0 and idx < len(STAGES):
            mdot = thrust / (stage["isp"] * G0) if stage["isp"] > 0 else 0
            rem = a["prop_remaining"]
            rem[idx] = max(0.0, rem[idx] - mdot * dt)
            if rem[idx] <= 0 and idx < len(STAGES) - 1:
                a["stage_idx"] = idx + 1
        a["throttle"] = throttle; a["pitch_deg"] = pitch_deg
        return {"altitude_km": alt / 1000.0, "speed_mps": vmag,
                "mass_kg": mass, "stage": a["stage_idx"] + 1}

    def _apses(self) -> tuple:
        els = OrbitalMechanics.state_to_elements(
            [self.rocket.position[0], self.rocket.position[1], 0.0],
            [self.rocket.velocity[0], self.rocket.velocity[1], 0.0],
            mu=MU_EARTH)
        apo = els["a"] * (1 + els["e"]) - R_EARTH
        peri = els["a"] * (1 - els["e"]) - R_EARTH
        return apo, peri, els

    @staticmethod
    def _pitch_program(alt: float) -> float:
        """Gravity-turn pitch (degrees from horizontal). Aggressive early
        pitch-over keeps vertical speed low so the vehicle builds horizontal
        orbital speed without coasting too high; near-horizontal by ~80 km."""
        if alt < 1_500:
            return 90.0
        if alt < 12_000:
            return 90.0 - 55.0 * (alt - 1_500) / 10_500      # 90 -> 35 by 12 km
        if alt < 45_000:
            return 35.0 - 30.0 * (alt - 12_000) / 33_000     # 35 -> 5 by 45 km
        if alt < 80_000:
            return 5.0 - 5.0 * (alt - 45_000) / 35_000       # 5 -> 0 by 80 km
        return 0.0

    def auto_pilot_to_orbit(self, target_alt_m: float = 400_000.0,
                            max_t: float = 4000.0, dt: float = 0.5) -> dict:
        """Guided surface-to-orbit ascent to a circular orbit.

        Three phases:
          1. Gravity-turn pitch program to ~80 km, then a PD altitude hold at
             ~130 km that burns horizontally to build orbital speed, cutting
             when apoapsis reaches the target (leaving a transfer orbit with a
             low but positive-ish periapsis).
          2. Ballistic coast to apoapsis (length from Kepler's equation).
          3. Impulsive prograde circularization burn at apoapsis
             (dv = v_circ - v_apo). Instantaneous, so apoapsis does not run
             away the way a long finite burn would.
        Returns the resulting Keplerian orbit.
        """
        if not self.initialized:
            self.initialize()
        hold_alt = 130_000.0

        # ---- phase 1: ascent + altitude hold ----
        t = 0.0
        while t < max_t:
            x, y = self.rocket.position[0], self.rocket.position[1]
            rmag = math.hypot(x, y)
            alt = rmag - R_EARTH
            apo, _, _ = self._apses()
            idx = self.rocket.attributes["stage_idx"]
            prop_left = sum(self.rocket.attributes["prop_remaining"][idx:])
            if prop_left <= 0:
                break
            if alt < 80_000 and apo < target_alt_m:
                self.step(dt, 1.0, self._pitch_program(alt))
            elif apo < target_alt_m:
                vr = (x * self.rocket.velocity[0] +
                      y * self.rocket.velocity[1]) / max(rmag, 1.0)
                pitch = max(-15.0, min(15.0,
                            -0.015 * vr - 0.0002 * (alt - hold_alt)))
                self.step(dt, 1.0, pitch)
            else:
                break       # apoapsis reached target -> cutoff
            t += dt

        # ---- phases 2 & 3: analytic jump to apoapsis + impulsive circ ----
        # The coast is done analytically (set true anomaly = pi, convert
        # elements -> state) rather than by numerically integrating under
        # gravity. Plain Euler leaks orbital energy over a long coast; the
        # analytic jump is exact and instantaneous, then the impulsive
        # prograde burn (dv = v_circ - v_apo) circularizes at apoapsis.
        _, _, els = self._apses()
        els_apo = dict(els)
        els_apo["ta"] = math.pi                    # apoapsis
        pos_apo, vel_apo = OrbitalMechanics.elements_to_state(els_apo, mu=MU_EARTH)
        self.rocket.position = [float(pos_apo[0]), float(pos_apo[1]), 0.0]
        self.rocket.velocity = [float(vel_apo[0]), float(vel_apo[1]), 0.0]
        r_apo = math.hypot(pos_apo[0], pos_apo[1])
        v_apo = math.hypot(vel_apo[0], vel_apo[1])
        v_circ = OrbitalMechanics.circular_orbit_velocity(r_apo, MU_EARTH)
        if 0 < v_apo < v_circ:                      # prograde impulsive dv
            scale = v_circ / v_apo
            self.rocket.velocity[0] = vel_apo[0] * scale
            self.rocket.velocity[1] = vel_apo[1] * scale

        self.step(dt, 0.0, 0.0)                   # settle one ballistic step
        apo, peri, els = self._apses()
        return {"elements": els, "apoapsis_km": apo / 1000.0,
                "periapsis_km": peri / 1000.0,
                "final_alt_km": (math.hypot(*self.rocket.position[:2]) - R_EARTH) / 1000.0,
                "final_speed_mps": math.hypot(*self.rocket.velocity[:2]),
                "stage_reached": self.rocket.attributes["stage_idx"] + 1}

    def update(self, dt: float) -> dict:
        if not self.initialized:
            self.initialize()
        return {"stage": self.rocket.attributes["stage_idx"] + 1}
