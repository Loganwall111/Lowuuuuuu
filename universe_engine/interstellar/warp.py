"""Agent 9 (shared) - Warp & wormhole travel vectors.

  * WarpDrive  : Alcubierre-style warp bubble. Effective superluminal speed
                 = warp_factor * c; travel time = distance / (warp * c). Energy
                 requirement scales as a proxy of the Alcubierre negative-energy
                 budget (~warp^3). No FTL physics is claimed - it is a sim
                 mechanic with physically-motivated scaling.
  * Wormhole   : Morris-Thorne traversable wormhole (proxy). Connects two
                 endpoints through a throat of radius r_throat; traversal is
                 near-instant with a time-dilation factor based on throat
                 curvature.
  * WarpNetwork: a graph of star systems linked by warp routes and wormholes;
                 Dijkstra finds the shortest travel-time path.
"""
from __future__ import annotations

import math
import heapq
import random
from typing import Dict, List, Tuple

from ..core.math_utils import C, AU
from ..core.state import Entity


class WarpDrive:
    def __init__(self, max_warp: float = 9.0):
        self.max_warp = max_warp

    def travel_time(self, distance_m: float, warp: float) -> dict:
        warp = min(warp, self.max_warp)
        v = warp * C
        t = distance_m / v if v > 0 else float("inf")
        # energy proxy (dimensionless) ~ warp^3 * (throat mass scale)
        energy = warp ** 3
        return {"warp": warp, "effective_speed_mps": v, "time_s": t,
                "time_days": t / 86400.0, "energy_proxy": energy}

    def humanize(self, t_s: float) -> str:
        if t_s < 60: return f"{t_s:.1f}s"
        if t_s < 3600: return f"{t_s/60:.1f}min"
        if t_s < 86400: return f"{t_s/3600:.1f}h"
        if t_s < 31557600: return f"{t_s/86400:.1f}d"
        return f"{t_s/31557600:.2f}y"


class Wormhole:
    def __init__(self, wid: str, a: Tuple[str, float], b: Tuple[str, float],
                 throat_radius_m: float = 1_000.0):
        self.id = wid
        self.a = a       # (system_id, distance_from_star_m)
        self.b = b
        self.r_throat = throat_radius_m

    def traverse(self, from_system: str) -> dict:
        if from_system == self.a[0]:
            to = self.b
        elif from_system == self.b[0]:
            to = self.a
        else:
            return {"ok": False, "msg": "not at a wormhole mouth"}
        # time-dilation factor from throat curvature (proxy)
        factor = 1.0 + 1e-6 / max(self.r_throat, 1.0)
        return {"ok": True, "to_system": to[0],
                "to_offset_m": to[1], "transit_s": factor,
                "throat_radius_m": self.r_throat}


class WarpNetwork:
    def __init__(self, config, state):
        self.config = config
        self.state = state
        self.rng = random.Random(config.seed ^ 0xC9)
        self.systems: Dict[str, dict] = {}     # id -> {pos3d, name, star_type}
        self.warp_routes: List[Tuple[str, str, float]] = []  # (a,b, warp_factor)
        self.wormholes: List[Wormhole] = []
        self.drive = WarpDrive()
        self.initialized = False

    def initialize(self) -> None:
        # seed a handful of star systems in a volume
        names = ["Sol", "Proxima", "Sirius", "Vega", "Altair", "Arcturus",
                 "TauCeti", "EpsilonEridani", "Trappist", "Wolf359"]
        for i, name in enumerate(names):
            pos = [self.rng.uniform(-30, 30) * 3.086e16,    # ~parsec-ish ly in m
                   self.rng.uniform(-30, 30) * 3.086e16,
                   self.rng.uniform(-30, 30) * 3.086e16]
            sid = f"sys:{name}"
            self.systems[sid] = {"name": name, "pos": pos,
                                 "star_type": self.rng.choice(
                                     ["G", "K", "M", "F", "A"])}
            self.state.add(Entity(id=sid, kind="body", subsystem="interstellar",
                                  position=pos, velocity=[0, 0, 0],
                                  attributes={"name": name, "star_type":
                                              self.systems[sid]["star_type"]}))
        ids = list(self.systems.keys())
        # warp routes between near neighbors
        for i in range(len(ids)):
            for j in range(i + 1, len(ids)):
                if self.rng.random() < 0.4:
                    self.warp_routes.append((ids[i], ids[j], self.rng.uniform(2, 8)))
        # a couple of wormholes for shortcut jumps
        self.wormholes.append(Wormhole("wh0", (ids[0], 0.0), (ids[5], 0.0)))
        self.wormholes.append(Wormhole("wh1", (ids[2], 0.0), (ids[8], 0.0)))
        self.initialized = True

    def _distance(self, a: str, b: str) -> float:
        import numpy as np
        pa = np.array(self.systems[a]["pos"]); pb = np.array(self.systems[b]["pos"])
        return float(np.linalg.norm(pa - pb))

    def shortest_path(self, src: str, dst: str) -> dict:
        """Dijkstra over warp routes + wormhole jumps, minimizing travel time."""
        if not self.initialized:
            self.initialize()
        import numpy as np
        adj: Dict[str, List[Tuple[str, float]]] = {s: [] for s in self.systems}
        for a, b, w in self.warp_routes:
            d = self._distance(a, b)
            t = self.drive.travel_time(d, w)["time_s"]
            adj[a].append((b, t)); adj[b].append((a, t))
        for wh in self.wormholes:
            adj[wh.a[0]].append((wh.b[0], wh.traverse(wh.a[0])["transit_s"]))
            adj[wh.b[0]].append((wh.a[0], wh.traverse(wh.b[0])["transit_s"]))
        dist = {s: math.inf for s in self.systems}; dist[src] = 0.0
        prev: Dict[str, str | None] = {s: None for s in self.systems}
        pq = [(0.0, src)]
        while pq:
            d, u = heapq.heappop(pq)
            if d > dist[u]: continue
            for v, w in adj[u]:
                nd = d + w
                if nd < dist[v]:
                    dist[v] = nd; prev[v] = u; heapq.heappush(pq, (nd, v))
        if dist[dst] == math.inf:
            return {"ok": False, "msg": "no route"}
        # reconstruct
        path = []; cur = dst
        while cur is not None:
            path.append(cur); cur = prev[cur]
        path.reverse()
        return {"ok": True, "from": src, "to": dst, "hops": len(path) - 1,
                "travel_time_s": dist[dst],
                "human_time": self.drive.humanize(dist[dst]),
                "path": path}

    def update(self, dt: float) -> dict:
        if not self.initialized:
            self.initialize()
        return {"systems": len(self.systems),
                "warp_routes": len(self.warp_routes),
                "wormholes": len(self.wormholes)}
