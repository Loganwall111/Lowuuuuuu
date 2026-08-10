"""Agent 6 - Orbital Dynamics: Keplerian & n-body propagation.

All math is in SI units and physically correct. This module is a hot path
benchmarked by Agent 12 and exercised by Agent 12's regression tests.

Conventions:
  elements = {a, e, i, raan, argp, ta, mu, epoch}
    a    semi-major axis (m)
    e    eccentricity
    i    inclination (rad)
    raan right ascension of ascending node (rad)
    argp argument of periapsis (rad)
    ta   true anomaly (rad)
    mu   gravitational parameter of central body (m^3/s^2)
"""
from __future__ import annotations

import math
import numpy as np

from ..core.math_utils import MU_EARTH, vec3, normalize, cross


class OrbitalMechanics:
    # ---- Kepler's equation: M = E - e sin E ----------------------------
    @staticmethod
    def solve_kepler(M: float, e: float, tol: float = 1e-10,
                     max_iter: int = 64) -> float:
        M = math.fmod(M + math.pi, 2 * math.pi) - math.pi   # wrap to [-pi, pi]
        E = M if e < 0.8 else math.pi
        for _ in range(max_iter):
            f = E - e * math.sin(E) - M
            fp = 1 - e * math.cos(E)
            dE = f / fp
            E -= dE
            if abs(dE) < tol:
                break
        return E

    @staticmethod
    def mean_motion(a: float, mu: float) -> float:
        return math.sqrt(mu / a ** 3)

    # ---- elements <-> state --------------------------------------------
    @staticmethod
    def elements_to_state(els: dict, mu: float | None = None) -> tuple:
        mu = els.get("mu", mu or MU_EARTH)
        a, e = els["a"], els["e"]
        i = els["i"]; raan = els["raan"]; argp = els["argp"]; ta = els["ta"]
        p = a * (1 - e * e)
        r_pqw = np.array([p * math.cos(ta) / (1 + e * math.cos(ta)),
                          p * math.sin(ta) / (1 + e * math.cos(ta)),
                          0.0])
        # velocity in perifocal frame: h = sqrt(mu*p)
        h = math.sqrt(mu * p)
        v_pqw = np.array([-mu / h * math.sin(ta),
                          mu / h * (e + math.cos(ta)),
                          0.0])
        R = OrbitalMechanics._rot_pqw_to_inertial(raan, i, argp)
        return R @ r_pqw, R @ v_pqw

    @staticmethod
    def state_to_elements(r, v, mu: float = MU_EARTH) -> dict:
        r = np.asarray(r, dtype=float); v = np.asarray(v, dtype=float)
        rmag = float(np.linalg.norm(r)); vmag = float(np.linalg.norm(v))
        h = cross(r, v); hmag = float(np.linalg.norm(h))
        n = cross(vec3(0, 0, 1), h); nmag = float(np.linalg.norm(n))
        e_vec = (cross(v, h) / mu) - (r / rmag)
        e = float(np.linalg.norm(e_vec))
        energy = vmag * vmag / 2 - mu / rmag
        a = -mu / (2 * energy) if abs(energy) > 1e-12 else rmag
        # guard the degenerate radial (h ~ 0) case, e.g. v=0 at launch
        if hmag < 1e-9:
            i = 0.0
        else:
            i = math.acos(clamp2(h[2] / hmag, -1, 1))
        if nmag > 1e-12:
            raan = math.acos(clamp2(n[0] / nmag, -1, 1))
            if n[1] < 0: raan = 2 * math.pi - raan
        else:
            raan = 0.0
        if nmag > 1e-12 and e > 1e-12:
            argp = math.acos(clamp2(float(np.dot(n, e_vec)) / (nmag * e), -1, 1))
            if e_vec[2] < 0: argp = 2 * math.pi - argp
        else:
            argp = 0.0
        if e > 1e-12:
            ta = math.acos(clamp2(float(np.dot(e_vec, r)) / (e * rmag), -1, 1))
            if float(np.dot(r, v)) < 0: ta = 2 * math.pi - ta
        elif nmag > 1e-12:
            # circular, inclined: true anomaly from argument of latitude
            arglat = math.acos(clamp2(float(np.dot(n, r)) / (nmag * rmag), -1, 1))
            if float(np.dot(n, v)) < 0: arglat = 2 * math.pi - arglat
            ta = arglat - argp
        else:
            # circular AND equatorial: all angles undefined; use position
            # angle in the xy-plane as the true anomaly directly.
            ta = math.atan2(r[1], r[0]) - argp
        return {"a": a, "e": e, "i": i, "raan": raan, "argp": argp,
                "ta": ta, "mu": mu, "epoch": 0.0}

    @staticmethod
    def propagate_elements(els: dict, dt: float) -> dict:
        """Advance the orbit by dt using Kepler's equation (analytic, exact for
        two-body). Returns new elements with updated true anomaly."""
        mu = els.get("mu", MU_EARTH)
        a, e = els["a"], els["e"]
        n = OrbitalMechanics.mean_motion(a, mu)
        # current mean anomaly from current true anomaly
        ta = els["ta"]
        E0 = OrbitalMechanics._true_to_eccentric(ta, e)
        M0 = E0 - e * math.sin(E0)
        M = M0 + n * dt
        E = OrbitalMechanics.solve_kepler(M, e)
        ta_new = OrbitalMechanics._eccentric_to_true(E, e)
        out = dict(els)
        out["ta"] = ta_new
        out["epoch"] = els.get("epoch", 0.0) + dt
        return out

    # ---- n-body integrator (velocity Verlet) ---------------------------
    @staticmethod
    def n_body_step(positions: np.ndarray, velocities: np.ndarray,
                    masses: np.ndarray, dt: float,
                    accelerations: np.ndarray | None = None) -> tuple:
        """One symplectic velocity-Verlet step for N bodies.
        positions: (N,3), velocities: (N,3), masses: (N,)."""
        if accelerations is None:
            accelerations = OrbitalMechanics._accel(positions, masses)
        positions = positions + velocities * dt + 0.5 * accelerations * dt * dt
        a_new = OrbitalMechanics._accel(positions, masses)
        velocities = velocities + 0.5 * (accelerations + a_new) * dt
        return positions, velocities, a_new

    @staticmethod
    def _accel(positions: np.ndarray, masses: np.ndarray) -> np.ndarray:
        from ..core.math_utils import G
        N = positions.shape[0]
        a = np.zeros_like(positions)
        for i in range(N):
            for j in range(N):
                if i == j: continue
                r = positions[j] - positions[i]
                d = float(np.linalg.norm(r))
                if d < 1e-6: continue
                a[i] += G * masses[j] * r / (d ** 3)
        return a

    # ---- helpers --------------------------------------------------------
    @staticmethod
    def _true_to_eccentric(ta: float, e: float) -> float:
        return math.atan2(math.sqrt(1 - e * e) * math.sin(ta),
                          e + math.cos(ta))

    @staticmethod
    def _eccentric_to_true(E: float, e: float) -> float:
        return math.atan2(math.sqrt(1 - e * e) * math.sin(E),
                          math.cos(E) - e)

    @staticmethod
    def _rot_pqw_to_inertial(raan, i, argp) -> np.ndarray:
        c, s = math.cos, math.sin
        R1 = np.array([[c(raan), -s(raan), 0], [s(raan), c(raan), 0], [0, 0, 1]])
        R2 = np.array([[1, 0, 0], [0, c(i), -s(i)], [0, s(i), c(i)]])
        R3 = np.array([[c(argp), -s(argp), 0], [s(argp), c(argp), 0], [0, 0, 1]])
        return R1 @ R2 @ R3

    # ---- mission helpers ------------------------------------------------
    @staticmethod
    def circular_orbit_velocity(r: float, mu: float = MU_EARTH) -> float:
        return math.sqrt(mu / r)

    @staticmethod
    def hohmann_delta_v(r1: float, r2: float, mu: float = MU_EARTH) -> dict:
        """Delta-v for a Hohmann transfer between two circular orbits."""
        v1 = math.sqrt(mu / r1); v2 = math.sqrt(mu / r2)
        vt1 = math.sqrt(mu * (2 / r1 - 2 / (r1 + r2)))
        vt2 = math.sqrt(mu * (2 / r2 - 2 / (r1 + r2)))
        dv1 = vt1 - v1; dv2 = v2 - vt2
        tof = math.pi * math.sqrt((r1 + r2) ** 3 / (8 * mu))
        return {"dv1": dv1, "dv2": dv2, "dv_total": dv1 + dv2,
                "transfer_time_s": tof,
                "transfer_time_min": tof / 60.0}


def clamp2(x, lo, hi):
    return max(lo, min(hi, x))
