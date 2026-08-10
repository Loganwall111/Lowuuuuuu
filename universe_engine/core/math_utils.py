"""Core math primitives: physical constants, vectors, quaternions, units.

Intentionally dependency-light. Uses numpy only where vectorization is a real
win (renderer, physics integrator). Engine-wide constants live here so every
module shares one source of truth.
"""
from __future__ import annotations

import math
from dataclasses import dataclass

import numpy as np

# ---------------------------------------------------------------------------
# Physical constants (SI)
# ---------------------------------------------------------------------------
G = 6.67430e-11            # gravitational constant   m^3 kg^-1 s^-2
C = 299_792_458.0          # speed of light           m s^-1
C2 = C * C                 # c^2
M_SUN = 1.98892e30         # kg
M_EARTH = 5.972e24         # kg
R_EARTH = 6_371_000.0      # m (mean radius)
MU_EARTH = G * M_EARTH     # standard gravitational parameter (Earth) m^3 s^-2
MU_SUN = G * M_SUN
AU = 1.495978707e11        # m
DAY = 86_400.0             # s
HOUR = 3_600.0
G0 = 9.80665               # standard gravity m s^-2

# ---------------------------------------------------------------------------
# Vectors (lightweight helpers; the renderer/integrator use numpy directly)
# ---------------------------------------------------------------------------

def vec3(x: float, y: float, z: float) -> np.ndarray:
    return np.array([x, y, z], dtype=np.float64)


def normalize(v: np.ndarray) -> np.ndarray:
    n = float(np.linalg.norm(v))
    if n == 0.0:
        return v.copy()
    return v / n


def dot(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(a, b))


def cross(a: np.ndarray, b: np.ndarray) -> np.ndarray:
    return np.cross(a, b)


# ---------------------------------------------------------------------------
# Quaternions - for aircraft / spacecraft attitude. Convention: (w, x, y, z),
# Hamilton product, rotates vectors via v' = q * (0,v) * q^-1.
# ---------------------------------------------------------------------------

@dataclass(frozen=True)
class Quat:
    w: float
    x: float
    y: float
    z: float

    @staticmethod
    def identity() -> "Quat":
        return Quat(1.0, 0.0, 0.0, 0.0)

    @staticmethod
    def from_axis_angle(axis: np.ndarray, angle: float) -> "Quat":
        a = normalize(axis)
        h = angle * 0.5
        s = math.sin(h)
        return Quat(math.cos(h), a[0] * s, a[1] * s, a[2] * s)

    def __mul__(self, other: "Quat") -> "Quat":
        w1, x1, y1, z1 = self.w, self.x, self.y, self.z
        w2, x2, y2, z2 = other.w, other.x, other.y, other.z
        return Quat(
            w1 * w2 - x1 * x2 - y1 * y2 - z1 * z2,
            w1 * x2 + x1 * w2 + y1 * z2 - z1 * y2,
            w1 * y2 - x1 * z2 + y1 * w2 + z1 * x2,
            w1 * z2 + x1 * y2 - y1 * x2 + z1 * w2,
        )

    def conjugate(self) -> "Quat":
        return Quat(self.w, -self.x, -self.y, -self.z)

    def normalize(self) -> "Quat":
        n = math.sqrt(self.w ** 2 + self.x ** 2 + self.y ** 2 + self.z ** 2)
        if n == 0:
            return Quat.identity()
        return Quat(self.w / n, self.x / n, self.y / n, self.z / n)

    def rotate(self, v: np.ndarray) -> np.ndarray:
        qv = Quat(0.0, v[0], v[1], v[2])
        r = self * qv * self.conjugate()
        return vec3(r.x, r.y, r.z)

    def to_matrix(self) -> np.ndarray:
        w, x, y, z = self.w, self.x, self.y, self.z
        return np.array([
            [1 - 2 * (y * y + z * z), 2 * (x * y - z * w),     2 * (x * z + y * w)],
            [2 * (x * y + z * w),     1 - 2 * (x * x + z * z), 2 * (y * z - x * w)],
            [2 * (x * z - y * w),     2 * (y * z + x * w),     1 - 2 * (x * x + y * y)],
        ], dtype=np.float64)


def lerp(a: float, b: float, t: float) -> float:
    return a + (b - a) * t


def clamp(x: float, lo: float, hi: float) -> float:
    return max(lo, min(hi, x))


def smoothstep(edge0: float, edge1: float, x: float) -> float:
    t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0)
    return t * t * (3 - 2 * t)


# ---------------------------------------------------------------------------
# Procedural noise - value/gradient noise used by terrain, clouds, biomes.
# Deterministic, seeded, numpy-vectorizable over a coordinate grid.
# ---------------------------------------------------------------------------

def _hash_grid(ix: np.ndarray, iy: np.ndarray, seed: int) -> np.ndarray:
    """Deterministic hash -> [0,1) for integer grids."""
    h = (ix * 374761393 + iy * 668265263 + seed * 1274126177) & 0xFFFFFFFF
    h = (h ^ (h >> 13)) * 1274126177 & 0xFFFFFFFF
    return (h & 0xFFFFFFFF) / 4294967295.0


def value_noise_2d(x: np.ndarray, y: np.ndarray, seed: int = 0) -> np.ndarray:
    """Smoothed value noise sampled at float coords x,y (numpy arrays)."""
    x0 = np.floor(x).astype(np.int64)
    y0 = np.floor(y).astype(np.int64)
    fx = x - x0
    fy = y - y0
    sx = fx * fx * (3 - 2 * fx)
    sy = fy * fy * (3 - 2 * fy)
    v00 = _hash_grid(x0, y0, seed)
    v10 = _hash_grid(x0 + 1, y0, seed)
    v01 = _hash_grid(x0, y0 + 1, seed)
    v11 = _hash_grid(x0 + 1, y0 + 1, seed)
    a = v00 + (v10 - v00) * sx
    b = v01 + (v11 - v01) * sx
    return a + (b - a) * sy


def fbm_2d(x: np.ndarray, y: np.ndarray, octaves: int = 5, seed: int = 0,
           lacunarity: float = 2.0, gain: float = 0.5) -> np.ndarray:
    """Fractal Brownian motion (summed octaves of value noise) -> ~[0,1]."""
    amp = 0.5
    freq = 1.0
    total = np.zeros_like(x, dtype=np.float64)
    norm = 0.0
    for o in range(octaves):
        total += amp * value_noise_2d(x * freq, y * freq, seed + o * 17)
        norm += amp
        amp *= gain
        freq *= lacunarity
    return total / norm
