"""Agent 12 - Optimization Sage (perpetual background loop).

A REAL long-running process. Each cycle it benchmarks a set of hot engine
functions, records timings + memory deltas, and emits optimization
recommendations (e.g. 'blackhole.render grew 1.4x - investigate'). The
conductor (Agent 13) reads its telemetry to tune time budgets.

    python -m universe_engine.swarm.optimization_sage --cycle 45
"""
from __future__ import annotations

import argparse
import gc
import json
import os
import time
import tracemalloc
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def _ensure_log(path: str) -> None:
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)


def _emit(path: str, record: dict) -> None:
    _ensure_log(path)
    with open(path, "a") as f:
        f.write(json.dumps(record) + "\n")


def _bench_orbit() -> dict:
    """Benchmark Kepler propagation - a core hot path."""
    try:
        from ..aerospace.orbit import OrbitalMechanics
        om = OrbitalMechanics()
        els = {"a": 7_000_000.0, "e": 0.01, "i": 0.05, "raan": 0.0,
               "argp": 0.0, "ta": 0.0, "mu": 3.986e14, "epoch": 0.0}
        t0 = time.perf_counter()
        for _ in range(2000):
            els = om.propagate_elements(els, 10.0)
        dt = time.perf_counter() - t0
        return {"bench": "orbit.propagate x2000", "seconds": round(dt, 4),
                "per_call_us": round(dt / 2000 * 1e6, 2)}
    except Exception as e:
        return {"bench": "orbit.propagate", "error": repr(e)}


def _bench_noise() -> dict:
    try:
        import numpy as np
        from ..core.math_utils import fbm_2d
        x = np.linspace(0, 8, 256)
        y = np.linspace(0, 8, 256)
        gx, gy = np.meshgrid(x, y)
        t0 = time.perf_counter()
        fbm_2d(gx, gy, octaves=5)
        dt = time.perf_counter() - t0
        return {"bench": "fbm_2d 256x256", "seconds": round(dt, 4)}
    except Exception as e:
        return {"bench": "fbm_2d", "error": repr(e)}


def _bench_render_small() -> dict:
    try:
        from ..interstellar.blackhole import BlackHole
        bh = BlackHole(mass_msun=4.3e6)
        t0 = time.perf_counter()
        img = bh.render(width=96, height=96, steps=400, disk=True)
        dt = time.perf_counter() - t0
        return {"bench": "blackhole.render 96x96", "seconds": round(dt, 4),
                "pixels": img.size}
    except Exception as e:
        return {"bench": "blackhole.render", "error": repr(e)}


BENCHS = [_bench_orbit, _bench_noise, _bench_render_small]


def cycle(log_path: str, cycle_id: int, baseline: dict) -> dict:
    gc.collect()
    tracemalloc.start()
    t0 = time.time()
    results = []
    for b in BENCHS:
        try:
            results.append(b())
        except Exception as e:
            results.append({"bench": getattr(b, "__name__", "?"), "error": repr(e)})
        gc.collect()
    cur, peak = tracemalloc.get_traced_memory()
    tracemalloc.stop()

    # detect regressions vs baseline
    regressions = []
    for r in results:
        name = r.get("bench", "")
        secs = r.get("seconds")
        if secs is not None and name in baseline:
            ratio = secs / max(baseline[name], 1e-9)
            if ratio > 1.25:
                regressions.append({"bench": name, "ratio": round(ratio, 2),
                                    "msg": f"{name} grew {ratio:.1f}x - investigate"})
            baseline[name] = 0.7 * baseline[name] + 0.3 * secs   # EMA

    record = {
        "agent": 12, "name": "Optimization Sage", "cycle": cycle_id,
        "ts": time.time(), "dur_s": round(time.time() - t0, 3),
        "benchmarks": results,
        "mem_current_mb": round(cur / 1e6, 3),
        "mem_peak_mb": round(peak / 1e6, 3),
        "regressions": regressions,
        "status": "nominal" if not regressions else "regression",
    }
    _emit(log_path, record)
    hb = Path(log_path).with_suffix(".heartbeat")
    hb.write_text(json.dumps({"agent": 12, "ts": time.time(),
                              "cycle": cycle_id, "status": record["status"]}))
    return record


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--cycle", type=float, default=45.0)
    ap.add_argument("--log", default="logs/optimization_sage.jsonl")
    ap.add_argument("--max-cycles", type=int, default=0)
    args = ap.parse_args()
    baseline: dict = {}
    n = 0
    while True:
        n += 1
        try:
            rec = cycle(args.log, n, baseline)
            print(f"[OptSage #{n}] status={rec['status']} "
                  f"peak_mem={rec['mem_peak_mb']}MB regressions={len(rec['regressions'])}",
                  flush=True)
        except Exception as e:
            _emit(args.log, {"agent": 12, "cycle": n, "ts": time.time(),
                             "status": "crash", "error": repr(e)})
            print(f"[OptSage #{n}] CRASH: {e}", flush=True)
        if args.max_cycles and n >= args.max_cycles:
            break
        time.sleep(args.cycle)


if __name__ == "__main__":
    main()
