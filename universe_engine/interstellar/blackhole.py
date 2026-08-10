"""Agent 9 - Spacetime Weaver: black holes & gravitational lensing.

A custom, vectorized Schwarzschild geodesic ray tracer (NO Three.js / no web
wrappers). For every pixel it integrates the photon orbit equation

        d^2u/dphi^2 + u = (3/2) r_s u^2 ,    u = 1/r ,   r_s = 2GM/c^2

with RK4, in each ray's own orbital plane (justified by spherical symmetry),
while reconstructing 3D positions to test intersection with the accretion
disk plane. This reproduces the Interstellar cinematic aesthetic:
  * the black photon-ring shadow,
  * the lensed accretion disk arcing over and under the hole,
  * relativistic Doppler beaming (one side brighter),
  * the secondary image from photons wrapping the photon sphere.

A Kerr (spinning) variant is provided as a documented first-order
approximation (frame-dragging asymmetry) - full Kerr geodesic integration is
flagged as the path for the future hardware backend.
"""
from __future__ import annotations

import math
import numpy as np
from PIL import Image

from ..core.math_utils import G, C, M_SUN


class BlackHole:
    def __init__(self, mass_msun: float = 4.3e6, spin: float = 0.0):
        self.M = mass_msun * M_SUN
        self.spin = float(np.clip(spin, 0.0, 0.998))   # a/M
        # Schwarzschild radius (geometric units r_s = 2GM/c^2)
        self.r_s = 2.0 * G * self.M / (C * C)

    # ------------------------------------------------------------------
    def render(self, width: int = 256, height: int = 192, steps: int = 1500,
               disk: bool = True, camera_dist_rs: float = 40.0,
               elevation_deg: float = 12.0, fov_deg: float = 32.0,
               disk_in_rs: float = 3.0, disk_out_rs: float = 12.0,
               bg_starfield: bool = True) -> Image.Image:
        rs = self.r_s
        D = camera_dist_rs * rs
        r_in = disk_in_rs * rs
        r_out = disk_out_rs * rs
        escape_R = 400.0 * rs

        # Camera. Disk = xz-plane (normal +y). Camera above plane at elevation.
        elev = math.radians(elevation_deg)
        cam = np.array([0.0, D * math.sin(elev), D * math.cos(elev)])
        e1 = cam / np.linalg.norm(cam)            # BH->camera unit (same for all)

        forward = -cam / np.linalg.norm(cam)
        world_up = np.array([0.0, 1.0, 0.0])
        right = np.cross(forward, world_up); right /= np.linalg.norm(right)
        up = np.cross(right, forward)

        # Pixel grid -> ray directions
        aspect = width / height
        tan_h = math.tan(math.radians(fov_deg) * 0.5)
        xs = np.linspace(-1, 1, width) * tan_h
        ys = np.linspace(1, -1, height) * tan_h * aspect
        gx, gy = np.meshgrid(xs, ys)              # (H, W)
        dirs = (forward[None, None, :]
                + gx[..., None] * right[None, None, :]
                + gy[..., None] * up[None, None, :])
        dirs = dirs / np.linalg.norm(dirs, axis=-1, keepdims=True)
        N = width * height
        v = dirs.reshape(N, 3)

        cam_vec = np.broadcast_to(cam, (N, 3))
        n = np.cross(cam_vec, v)                  # (N,3) angular momentum dir
        b = np.linalg.norm(n, axis=1)             # impact parameter
        n_hat = np.zeros_like(n)
        nz = b > 1e-9
        n_hat[nz] = n[nz] / b[nz, None]
        e2 = np.cross(n_hat, e1)                  # in-plane perpendicular basis
        # radial rays (b~0) -> straight into the hole
        radial = ~nz

        u0 = 1.0 / D
        # initial du/dphi = +sqrt(1/b^2 - u0^2 + r_s u0^3)
        disc = 1.0 / (b ** 2 + 1e-30) - u0 ** 2 + rs * u0 ** 3
        disc = np.maximum(disc, 0.0)
        w = np.sqrt(disc)
        w[radial] = 1.0 / (0.01 * rs)             # force rapid capture for radial
        u = np.full(N, u0)

        alive = np.ones(N, dtype=bool)
        color = np.zeros((N, 3))
        phi = np.zeros(N)
        dphi = 0.01

        # 3D position for disk test
        def pos3d(u_arr, phi_arr):
            r = 1.0 / np.maximum(u_arr, 1e-12)
            cp = np.cos(phi_arr)[:, None] * e1[None, :]
            sp = np.sin(phi_arr)[:, None] * e2
            return r[:, None] * (cp + sp)

        prev_y = pos3d(u, phi)[:, 1]
        hit_disk = np.zeros(N, dtype=bool)

        for _ in range(steps):
            if not alive.any():
                break
            # RK4 on (u, w=du/dphi):  u' = w ;  w' = -u + 1.5*rs*u^2
            idx = alive
            ui = u[idx]; wi = w[idx]
            k1u, k1w = wi, -ui + 1.5 * rs * ui * ui
            k2u, k2w = wi + 0.5*dphi*k1w, -(ui+0.5*dphi*k1u) + 1.5*rs*(ui+0.5*dphi*k1u)**2
            k3u, k3w = wi + 0.5*dphi*k2w, -(ui+0.5*dphi*k2u) + 1.5*rs*(ui+0.5*dphi*k2u)**2
            k4u, k4w = wi + dphi*k3w, -(ui+dphi*k3u) + 1.5*rs*(ui+dphi*k3u)**2
            u_new = ui + dphi/6*(k1u + 2*k2u + 2*k3u + k4u)
            w_new = wi + dphi/6*(k1w + 2*k2w + 2*k3w + k4w)
            phi_new = phi[idx] + dphi
            u[idx] = u_new; w[idx] = w_new; phi[idx] = phi_new

            r_cur = 1.0 / np.maximum(u, 1e-12)
            # capture
            cap = idx & (r_cur < rs * 1.001)
            color[cap] = [0.0, 0.0, 0.0]
            alive[cap] = False
            # escape
            esc = idx & (r_cur > escape_R)
            if bg_starfield:
                color[esc] = self._starfield(v[esc])
            else:
                color[esc] = [0.01, 0.01, 0.03]
            alive[esc] = False

            # disk plane crossing (y sign change) within annulus
            cur_y = pos3d(u, phi)[:, 1]
            still = alive
            cross = still & (np.signbit(prev_y) != np.signbit(cur_y))
            in_band = (r_cur > r_in) & (r_cur < r_out)
            cross = cross & in_band
            if cross.any():
                # interpolate r at the plane crossing (linear in y)
                y0 = prev_y[cross]; y1 = cur_y[cross]
                t = np.clip(-y0 / (y1 - y0 + 1e-30), 0.0, 1.0)
                u_cross = u[cross] + t * (u[cross] - (u[cross] - w[cross]*dphi))
                # simpler: r at crossing ~ r_cur (fine for shading)
                r_cross = r_cur[cross]
                color[cross] = self._disk_color(r_cross, pos3d(u, phi)[cross], v[cross],
                                                r_in, r_out)
                hit_disk[cross] = True
                alive[cross] = False
            prev_y = cur_y

        # any still-alive rays -> treat as escaped to starfield
        if bg_starfield:
            color[alive] = self._starfield(v[alive])

        img = (np.clip(color, 0.0, 1.0) * 255).astype(np.uint8).reshape(height, width, 3)
        return Image.fromarray(img, "RGB")

    # ------------------------------------------------------------------
    def _disk_color(self, r, pos, view_dir, r_in, r_out):
        """Procedural accretion-disk shading: temperature by radius + Doppler
        beaming from Keplerian rotation in the disk (xz) plane."""
        rs = self.r_s
        t = np.clip((r - r_in) / (r_out - r_in), 0.0, 1.0)   # 0 inner(hot) .. 1 outer
        # blackbody-ish color ramp: white-blue -> yellow -> orange -> red
        hot = np.array([0.85, 0.92, 1.0])
        mid = np.array([1.0, 0.85, 0.45])
        cool = np.array([1.0, 0.35, 0.12])
        col = np.where(t[:, None] < 0.5,
                       hot + (mid - hot) * (t[:, None] * 2),
                       mid + (cool - mid) * ((t[:, None] - 0.5) * 2))
        # brightness falls off with radius and is boosted near inner edge
        bright = (1.0 - 0.6 * t) * (1.0 / (0.4 + 2.0 * t))
        # Keplerian orbital speed (Newtonian approx, in units of c)
        v_orb = np.sqrt(rs / (2.0 * np.maximum(r, r_in)))      # sqrt(GM/r)/c since rs=2GM/c^2
        v_orb = np.clip(v_orb, 0.0, 0.6)
        # direction of motion = tangent in disk plane (rotate radial by 90deg about y)
        rad = pos.copy()
        rad[:, 1] = 0.0
        rn = np.linalg.norm(rad, axis=1, keepdims=True) + 1e-12
        rad = rad / rn
        motion = np.cross(np.array([0.0, 1.0, 0.0]), rad)      # CCW from +y
        # line of sight from disk point to camera ~ -view_dir
        los = -view_dir / (np.linalg.norm(view_dir, axis=1, keepdims=True) + 1e-12)
        beta_dot = np.sum(motion * los, axis=1)
        # relativistic beaming factor ~ (1/(1-beta.n))^3
        beam = 1.0 / np.clip(1.0 - v_orb * beta_dot, 0.2, 5.0) ** 3
        beam = np.clip(beam, 0.3, 4.0)
        col = col * bright[:, None] * beam[:, None]
        # spin asymmetry (Kerr first-order proxy): shift brightness by spin
        if self.spin > 0:
            col[:, 0] *= (1.0 + 0.25 * self.spin * np.sign(beta_dot))
        return np.clip(col, 0.0, 4.0)

    def _starfield(self, dirs):
        """Sparse procedural stars on a deep-space gradient from ray dirs."""
        d = dirs / (np.linalg.norm(dirs, axis=1, keepdims=True) + 1e-12)
        # hash direction to a pseudo-random in [0,1)
        h = (np.abs(np.dot(d, np.array([12.9898, 78.233, 37.719]))) * 43758.5453)
        h = h - np.floor(h)
        h2 = (np.abs(np.dot(d, np.array([4.1, 91.3, 17.7]))) * 12345.6789)
        h2 = h2 - np.floor(h2)
        star = (h > 0.985).astype(float) * (0.6 + 0.4 * h2)
        base = np.array([0.02, 0.025, 0.05])
        col = np.tile(base, (d.shape[0], 1))
        col[:, 0] += star * 0.9
        col[:, 1] += star * 0.9
        col[:, 2] += star * 1.0
        return np.clip(col, 0.0, 1.0)

    # ------------------------------------------------------------------
    def update(self, dt: float) -> dict:
        # black holes are static on sim timescales
        return {"mass_msun": self.M / M_SUN, "r_s_m": self.r_s}

    def describe(self) -> dict:
        return {"mass_msun": self.M / M_SUN,
                "schwarzschild_radius_m": self.r_s,
                "isco_radius_m": 3.0 * self.r_s,
                "photon_sphere_m": 1.5 * self.r_s,
                "spin": self.spin}
