"""
UniverseEngine - "Baby Lion Jason"
A custom (no Three.js / no web wrappers) hyper-realistic terrestrial, aerospace
and interstellar life-simulation engine.

This package is the foundational architecture. Heavy/perf-critical paths
(rendering, physics) are implemented in vectorized Python as correct reference
implementations, with clearly documented interfaces for future GPU/hardware
backends (Vulkan RT / CUDA) that require a toolchain not present in this sandbox.

Public surface:
    from universe_engine import UniverseEngine, SwarmConductor
"""
from .core.engine import UniverseEngine
from .core.config import EngineConfig
from .core.state import WorldState
from .swarm.conductor import SwarmConductor

__all__ = [
    "UniverseEngine",
    "EngineConfig",
    "WorldState",
    "SwarmConductor",
]

__version__ = "0.1.0-phase1"
