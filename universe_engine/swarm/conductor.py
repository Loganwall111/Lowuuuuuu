"""Agent 13 - Swarm Conductor.

Owns the data pipeline that keeps environment state persistent and in sync
across all 13 agents, and supervises the two perpetual background processes
(Agent 11 Bug Hunter, Agent 12 Optimization Sage) as real OS subprocesses.
"""
from __future__ import annotations

import json
import os
import subprocess
import sys
import threading
import time
from pathlib import Path
from typing import Dict, List, Optional

from .agents import SWARM, BY_ID, describe, default_worker_registry
from .message_bus import MessageBus


class SwarmConductor:
    def __init__(self, config, state, engine):
        self.config = config
        self.state = state
        self.engine = engine
        self.bus = MessageBus()
        self.workers = default_worker_registry(engine)
        self._procs: Dict[int, subprocess.Popen] = {}
        self._lock = threading.RLock()
        self._tick_count = 0
        os.makedirs(self.config.log_dir, exist_ok=True)

    # ---- perpetual agent supervision ------------------------------------
    def launch_background_agents(self) -> None:
        """Spawn Agents 11 & 12 as detached perpetual loops."""
        with self._lock:
            if self._procs:
                return
            py = sys.executable
            root = str(Path(__file__).resolve().parents[2])
            specs = [
                (11, [py, "-m", "universe_engine.swarm.bug_hunter",
                      "--cycle", "30",
                      "--log", os.path.join(self.config.log_dir, "bug_hunter.jsonl")]),
                (12, [py, "-m", "universe_engine.swarm.optimization_sage",
                      "--cycle", "45",
                      "--log", os.path.join(self.config.log_dir, "optimization_sage.jsonl")]),
            ]
            for aid, cmd in specs:
                logf = open(os.path.join(self.config.log_dir, f"agent{aid:02d}.stdout"), "a")
                p = subprocess.Popen(cmd, cwd=root, stdout=logf, stderr=subprocess.STDOUT,
                                     start_new_session=True)
                self._procs[aid] = p
                self.bus.publish(13, aid, "agent.spawn", {"pid": p.pid})
                print(f"[Conductor] launched {BY_ID[aid].label} pid={p.pid}", flush=True)

    def stop_background_agents(self) -> None:
        with self._lock:
            for aid, p in self._procs.items():
                try:
                    p.terminate()
                    try:
                        p.wait(timeout=5)
                    except subprocess.TimeoutExpired:
                        p.kill()
                    self.bus.publish(13, aid, "agent.stop", {"pid": p.pid})
                except Exception as e:
                    self.bus.publish(13, aid, "agent.stop.error", {"err": repr(e)})
            self._procs.clear()

    def _read_heartbeat(self, aid: int) -> Optional[dict]:
        name = "bug_hunter" if aid == 11 else "optimization_sage"
        path = Path(self.config.log_dir) / f"{name}.heartbeat"
        if not path.exists():
            return None
        try:
            return json.loads(path.read_text())
        except Exception:
            return None

    # ---- pipeline synchronization ---------------------------------------
    def synchronize(self, dt: float) -> None:
        """Called once per engine frame: persist state, emit pipeline messages,
        and record agent liveness. (Subsystem stepping is done by engine.step.)
        """
        self._tick_count += 1
        # Persist the shared environment state - the single source of truth.
        self.state.last_sync = {
            "conductor_tick": self._tick_count,
            "ts": time.time(),
            "sim_time": self.state.sim_time,
            "bus_total": self.bus.total_published,
        }
        self.state.save(self.config.state_path)

        # Heartbeat the workers so the bus reflects a living pipeline.
        if self._tick_count % 4 == 0:
            for aid in range(1, 11):
                spec = BY_ID[aid]
                self.bus.publish(13, aid, "tick", {
                    "sim_time": self.state.sim_time,
                    "owner": spec.role,
                })

        # Sample background-agent liveness into state metrics.
        hb11 = self._read_heartbeat(11)
        hb12 = self._read_heartbeat(12)
        self.state.metrics["agent11_alive"] = 1.0 if hb11 else 0.0
        self.state.metrics["agent12_alive"] = 1.0 if hb12 else 0.0
        if hb11:
            self.state.metrics["agent11_last_cycle"] = float(hb11.get("cycle", 0))
        if hb12:
            self.state.metrics["agent12_last_cycle"] = float(hb12.get("cycle", 0))

    # ---- introspection --------------------------------------------------
    def status(self) -> dict:
        with self._lock:
            procs = {}
            for aid, p in self._procs.items():
                procs[aid] = {"pid": p.pid, "alive": p.poll() is None}
        return {
            "swarm_size": len(SWARM),
            "conductor_ticks": self._tick_count,
            "background_agents": procs,
            "bus_total_published": self.bus.total_published,
            "bus_recent": self.bus.snapshot(8),
            "roster": [{"id": a.id, "name": a.name, "perpetual": a.perpetual}
                       for a in SWARM],
        }

    def roster(self) -> str:
        return describe()
