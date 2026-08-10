"""Engine configuration."""
from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class EngineConfig:
    # Rendering
    render_width: int = 480
    render_height: int = 320
    # "software" = vectorized CPU reference renderer (this sandbox).
    # "vulkan-rt" / "cuda-rt" = hardware-accelerated backends (interface only;
    # require a GPU toolchain not present here). See interstellar/renderer.py.
    render_backend: str = "software"

    # Simulation
    physics_substeps: int = 4
    time_scale: float = 1.0          # sim seconds per real second
    max_simulation_dt: float = 1.0 / 30.0

    # World
    seed: int = 1337
    planet_radius_m: float = 6_371_000.0

    # Persistence
    state_path: str = "universe_state.json"

    # Swarm
    swarm_tick_hz: float = 2.0       # conductor synchronization rate
    enable_background_agents: bool = True

    # Telemetry / logs
    log_dir: str = "logs"
    telemetry_path: str = "logs/telemetry.jsonl"
