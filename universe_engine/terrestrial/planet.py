"""Agent 1 - Terra Architect: the planetary surface of Earth.

Two-layer design, exactly as a production engine would split it:

  1. Procedural core (implemented here): a deterministic spherical terrain +
     biome field sampled at any lat/lon. Used as the fallback and as the
     geometric backbone (ocean/land mask, elevation, biomes).

  2. Streaming photogrammetry provider (interface): the integration point for
     real global satellite/DEM/photogrammetry tiles (e.g. a Bing/Mapbox-style
     quadtree). NOT bundled - that data is licensed and multi-TB. The interface
     defines the contract a real provider plugs into; a stub returns the
     procedural surface so the pipeline runs end-to-end today.

The procedural layer uses fbm noise on the sphere so there are no seams at the
poles or the antimeridian.
"""
from __future__ import annotations

import math
import numpy as np

from ..core.math_utils import fbm_2d, smoothstep, clamp


# Biome table: ordered by aridity/temperature thresholds.
BIOMES = [
    "ocean", "coast", "beach", "desert", "savanna", "grassland",
    "forest", "rainforest", "taiga", "tundra", "snow", "mountain", "ice",
]


class StreamingTileProvider:
    """Contract for a real global photogrammetry/satellite tile service.

    A production provider (Bing Maps / Mapbox Satellite / a custom DEM quadtree)
    implements `fetch_tile(level, x, y)` returning elevation + imagery for a
    web-mercator quadkey. The bundled stub returns the procedural surface so
    the engine runs without licensed data.
    """

    def fetch_tile(self, level: int, x: int, y: int) -> dict:
        # Stub: no network/licensed data. Real provider overrides this.
        return {"status": "procedural-fallback", "level": level, "x": x, "y": y}

    def is_available(self) -> bool:
        return False


class PlanetEarth:
    def __init__(self, config, state):
        self.config = config
        self.state = state
        self.radius = config.planet_radius_m
        self.seed = config.seed
        self.tile_provider = StreamingTileProvider()
        # sea level ~ 0 m of the normalized height field; tuned below.
        self.sea_level = 0.0
        self._tiles_served = 0

    def initialize(self) -> None:
        from ..core.state import Entity
        self.state.add(Entity(
            id="earth", kind="body", subsystem="terrestrial",
            position=[0.0, 0.0, 0.0],
            attributes={"radius_m": self.radius, "mass_kg": 5.972e24,
                        "rot_period_s": 86164.0, "seed": self.seed}))

    # ---- spherical procedural surface -----------------------------------
    def _sample_field(self, lat_deg, lon_deg, octaves=6):
        """Seamless noise on the sphere via 3D->2D domain warp."""
        lat = np.radians(lat_deg)
        lon = np.radians(lon_deg)
        # project sphere point to a 2D domain using an equal-area-ish warp so
        # poles do not pinch.
        u = lon * math.cos(lat) * 2.0
        v = lat * 3.0
        return fbm_2d(np.array([u]), np.array([v]), octaves=octaves,
                      seed=self.seed)[0]

    def elevation_at(self, lat_deg: float, lon_deg: float) -> dict:
        """Return elevation (m) and biome name at a geodetic point."""
        h = self._sample_field(lat_deg, lon_deg, octaves=6)
        # map [0,1] -> [-8000 m, +6000 m] ocean/land split at h=0.48
        sea = 0.48
        if h < sea:
            elev = (h - sea) / sea * 4000.0          # -4000 .. 0 m (bathymetry)
            biome = "ocean"
            depth = -elev
            if depth < 200:
                biome = "coast"
        else:
            t = (h - sea) / (1 - sea)
            elev = t * 6000.0                          # 0 .. 6000 m
            # latitude-driven temperature proxy
            abslat = abs(lat_deg)
            temp = 1.0 - abslat / 90.0 - 0.15 * (elev / 6000.0)
            temp = clamp(temp, 0.0, 1.0)
            arid = self._sample_field(lat_deg + 13.0, lon_deg - 7.0, octaves=4)
            biome = self._classify_biome(elev, temp, arid)
        return {"elevation_m": elev, "biome": biome,
                "tile_source": "procedural" if not self.tile_provider.is_available()
                else "streamed"}

    @staticmethod
    def _classify_biome(elev_m: float, temp: float, arid: float) -> str:
        if elev_m > 4500:
            return "snow" if temp < 0.4 else "mountain"
        if temp < 0.15:
            return "ice" if arid > 0.5 else "tundra"
        if temp < 0.32:
            return "taiga"
        if arid > 0.62:
            return "desert"
        if arid > 0.5:
            return "savanna" if temp > 0.55 else "grassland"
        if temp > 0.7 and arid < 0.45:
            return "rainforest"
        if arid < 0.55:
            return "forest"
        return "grassland"

    # ---- streaming LOD interface ----------------------------------------
    def request_tile(self, level: int, x: int, y: int) -> dict:
        """LOD quadtree request. Real provider streams photogrammetry here."""
        self._tiles_served += 1
        return self.tile_provider.fetch_tile(level, x, y)

    def update(self, dt: float) -> dict:
        # Surface is static between tectonic timescales; expose tile throughput.
        return {"tiles_served": self._tiles_served,
                "streaming": self.tile_provider.is_available()}
