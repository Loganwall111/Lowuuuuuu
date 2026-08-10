"""Agent 8 - Stargazer: custom rendering pipeline.

A from-scratch (no Three.js / no web wrappers) ray-tracing pipeline with:
  * ray-sphere intersection,
  * Lambert diffuse + Blinn-Phong specular,
  * sharp mirror reflections (configurable bounce depth),
  * dynamic shadows via shadow rays,
  * a procedural deep-space background.

A `RenderBackend` interface defines the hardware-accelerated contract
(Vulkan-RT / CUDA-RT). The bundled `SoftwareBackend` is a correct vectorized
CPU reference. The hardware backends raise `NotImplementedError` with a clear
message: they require a GPU toolchain (Vulkan SDK / CUDA) not present in this
sandbox, and are the documented drop-in for a production build.
"""
from __future__ import annotations

import math
import numpy as np
from PIL import Image
from dataclasses import dataclass, field
from typing import List, Optional, Callable

from ..core.math_utils import normalize


@dataclass
class Material:
    color: np.ndarray = field(default_factory=lambda: np.array([0.8, 0.8, 0.8]))
    diffuse: float = 0.8
    specular: float = 0.2
    reflectivity: float = 0.0       # 0 = matte, 1 = perfect mirror
    emission: float = 0.0


@dataclass
class Sphere:
    center: np.ndarray
    radius: float
    material: Material


@dataclass
class DirectionalLight:
    direction: np.ndarray       # points from surface toward light (unit)
    color: np.ndarray
    intensity: float


@dataclass
class Scene:
    spheres: List[Sphere]
    light: DirectionalLight
    background_fn: Optional[Callable[[np.ndarray], np.ndarray]] = None


class RenderBackend:
    """Hardware-accelerated ray-tracing backend contract."""
    name = "abstract"

    def render(self, scene: Scene, camera: dict, width: int, height: int,
               bounces: int = 2, shadows: bool = True) -> Image.Image:
        raise NotImplementedError(
            f"{self.name} backend requires a GPU toolchain (Vulkan-RT / CUDA) "
            "not available in this sandbox. Use SoftwareBackend, or install the "
            "toolchain and implement render() against the hardware RT API.")


class SoftwareBackend(RenderBackend):
    """Clean, correct, vectorized CPU ray tracer (reference implementation)."""
    name = "software"

    def render(self, scene, camera, width, height, bounces=2, shadows=True):
        spheres = scene.spheres
        light = scene.light
        L = normalize(light.direction)
        eye = camera["eye"]
        forward = normalize(camera["forward"])
        up = normalize(camera["up"])
        right = normalize(np.cross(forward, up))
        fov = math.radians(camera.get("fov", 50.0))
        tan_h = math.tan(fov * 0.5)
        aspect = width / height
        xs = np.linspace(-1, 1, width) * tan_h
        ys = np.linspace(1, -1, height) * tan_h / aspect
        gx, gy = np.meshgrid(xs, ys)
        dirs = forward + gx[..., None] * right + gy[..., None] * up
        dirs = dirs / np.linalg.norm(dirs, axis=-1, keepdims=True)
        N = width * height
        ro = np.broadcast_to(eye, (N, 3)).copy()
        rd = dirs.reshape(N, 3).copy()

        color = np.zeros((N, 3))
        throughput = np.ones((N, 3))
        active = np.ones(N, dtype=bool)

        for bounce in range(bounces + 1):
            t, sid = self._intersect(ro, rd, spheres)
            hit = active & (sid >= 0)
            miss = active & ~hit
            if scene.background_fn is not None:
                color[miss] += throughput[miss] * scene.background_fn(rd[miss])
            if not hit.any():
                break
            hi = np.where(hit)[0]
            P = ro[hi] + rd[hi] * t[hi][:, None]
            sph_idx = sid[hi]
            centers = np.array([spheres[i].center for i in sph_idx])
            radii = np.array([spheres[i].radius for i in sph_idx])
            mats = [spheres[i].material for i in sph_idx]
            mcol = np.array([m.color for m in mats])
            mdiff = np.array([m.diffuse for m in mats])
            mspec = np.array([m.specular for m in mats])
            mrefl = np.array([m.reflectivity for m in mats])
            memit = np.array([m.emission for m in mats])
            Nrm = (P - centers) / radii[:, None]
            Nrm = Nrm / np.linalg.norm(Nrm, axis=1, keepdims=True)

            ndotl = np.clip(Nrm @ L, 0, 1)
            diff = mcol * (mdiff * ndotl)[:, None] * light.color * light.intensity
            H = L[None, :] - rd[hi]
            H = H / np.linalg.norm(H, axis=1, keepdims=True)
            spec = np.clip(np.einsum("ij,ij->i", Nrm, H), 0, 1) ** 48
            shade = diff + (mspec * spec)[:, None] * light.color * light.intensity
            shade += memit[:, None] * mcol
            if shadows:
                in_sh = self._shadow(P + Nrm * 1e-3, L, spheres)
                shade = np.where(in_sh[:, None], shade * 0.2, shade)
            color[hi] += throughput[hi] * shade

            # reflection: keep only reflecting rays active for next bounce
            keep = mrefl > 1e-3
            if bounce < bounces and keep.any():
                ki = hi[keep]
                kd = rd[ki]
                kn = (P[keep] - centers[keep]) / radii[keep, None]
                kn = kn / np.linalg.norm(kn, axis=1, keepdims=True)
                new_d = kd - 2 * np.einsum("ij,ij->i", kd, kn)[:, None] * kn
                new_d = new_d / np.linalg.norm(new_d, axis=1, keepdims=True)
                # rebuild full arrays but only ki carry forward
                new_ro = ro.copy(); new_rd = rd.copy(); new_thr = np.zeros((N, 3))
                new_ro[ki] = P[keep] + kn * 1e-3
                new_rd[ki] = new_d
                new_thr[ki] = throughput[ki] * mrefl[keep, None] * mcol[keep]
                ro, rd, throughput = new_ro, new_rd, new_thr
                active = np.zeros(N, dtype=bool); active[ki] = True
            else:
                active[hi] = False

        img = (np.clip(color, 0, 1) * 255).astype(np.uint8).reshape(height, width, 3)
        return Image.fromarray(img, "RGB")

    def _intersect(self, o, d, spheres):
        N = o.shape[0]
        best_t = np.full(N, np.inf)
        best_i = np.full(N, -1, dtype=int)
        for i, s in enumerate(spheres):
            oc = o - s.center
            b = np.einsum("ij,ij->i", oc, d)
            c = np.einsum("ij,ij->i", oc, oc) - s.radius * s.radius
            disc = b * b - c
            ok = disc > 0
            sq = np.sqrt(np.maximum(disc, 0.0))
            t0 = -b - sq; t1 = -b + sq
            t = np.where((t0 > 1e-4) & ok, t0,
                         np.where((t1 > 1e-4) & ok, t1, np.inf))
            upd = t < best_t
            best_t = np.where(upd, t, best_t)
            best_i = np.where(upd & ok, i, best_i)
        return best_t, best_i

    def _shadow(self, o, L, spheres):
        # L is a single (3,) light direction shared by all shadow rays.
        L = np.asarray(L, dtype=float).reshape(3)
        any_hit = np.zeros(o.shape[0], dtype=bool)
        for s in spheres:
            oc = o - s.center                       # (M,3)
            b = oc @ L                              # (M,)
            c = np.einsum("ij,ij->i", oc, oc) - s.radius * s.radius
            disc = b * b - c
            ok = disc > 0
            sq = np.sqrt(np.maximum(disc, 0.0))
            t = -b - sq
            any_hit |= ok & (t > 1e-3)
        return any_hit


class VulkanRTBackend(RenderBackend):
    name = "vulkan-rt"


class CudaRTBackend(RenderBackend):
    name = "cuda-rt"


def default_space_background(d: np.ndarray) -> np.ndarray:
    """Deep-space gradient + procedural stars from ray directions."""
    d = d / (np.linalg.norm(d, axis=1, keepdims=True) + 1e-12)
    h = np.abs(np.dot(d, np.array([12.9898, 78.233, 37.719]))) * 43758.5453
    h = h - np.floor(h)
    star = (h > 0.992).astype(float)
    up = np.clip(d[:, 1] * 0.5 + 0.5, 0, 1)
    base = np.stack([0.01 + 0.01 * up, 0.012 + 0.012 * up, 0.03 + 0.03 * up], axis=1)
    base[:, 0] += star * 0.9
    base[:, 1] += star * 0.9
    base[:, 2] += star * 1.0
    return np.clip(base, 0, 1)


class Renderer:
    """Public renderer wrapping a backend."""
    def __init__(self, backend: Optional[RenderBackend] = None,
                 width: int = 320, height: int = 240):
        self.backend = backend or SoftwareBackend()
        self.width = width
        self.height = height

    def render(self, scene: Scene, camera: dict, bounces: int = 2,
               shadows: bool = True) -> Image.Image:
        return self.backend.render(scene, camera, self.width, self.height,
                                   bounces, shadows)
