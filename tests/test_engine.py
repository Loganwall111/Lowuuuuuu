"""UniverseEngine test suite - exercised perpetually by Agent 11 (Bug Hunter)."""
import os
import math
import numpy as np

from universe_engine.swarm.agents import SWARM, BY_ID
from universe_engine.aerospace.orbit import OrbitalMechanics
from universe_engine.core.math_utils import MU_EARTH, R_EARTH
from universe_engine.core.state import WorldState, Entity


def test_swarm_has_exactly_13_agents():
    assert len(SWARM) == 13
    ids = [a.id for a in SWARM]
    assert ids == list(range(1, 14))
    perpetual = [a for a in SWARM if a.perpetual]
    assert {a.id for a in perpetual} == {11, 12, 13}


def test_kepler_solver_basic():
    om = OrbitalMechanics()
    assert abs(om.solve_kepler(0.0, 0.0)) < 1e-9
    # for e=0, E == M
    assert abs(om.solve_kepler(0.7, 0.0) - 0.7) < 1e-9


def test_orbit_round_trip_one_period():
    om = OrbitalMechanics()
    els = {"a": 7_000_000.0, "e": 0.05, "i": 0.1, "raan": 0.3,
           "argp": 0.4, "ta": 0.0, "mu": MU_EARTH, "epoch": 0.0}
    pos0, vel0 = om.elements_to_state(els)
    T = 2 * math.pi / om.mean_motion(els["a"], MU_EARTH)
    els2 = om.propagate_elements(els, T)
    pos1, vel1 = om.elements_to_state(els2)
    assert np.linalg.norm(np.array(pos1) - np.array(pos0)) < 1.0      # < 1 m
    assert abs(els2["ta"] - 2 * math.pi) < 1e-6 or abs(els2["ta"]) < 1e-6


def test_state_to_elements_round_trip():
    om = OrbitalMechanics()
    els = {"a": 42_000_000.0, "e": 0.3, "i": 0.6, "raan": 1.0,
           "argp": 0.8, "ta": 0.5, "mu": MU_EARTH, "epoch": 0.0}
    pos, vel = om.elements_to_state(els)
    back = om.state_to_elements(pos, vel, mu=MU_EARTH)
    assert abs(back["a"] - els["a"]) / els["a"] < 1e-6
    assert abs(back["e"] - els["e"]) < 1e-6
    assert abs(back["i"] - els["i"]) < 1e-6


def test_hohmann_delta_v_positive():
    om = OrbitalMechanics()
    h = om.hohmann_delta_v(R_EARTH + 200_000, R_EARTH + 35_786_000)
    assert h["dv1"] > 0 and h["dv2"] > 0
    assert 2.3e3 < h["dv_total"] < 4.5e3            # LEO->GEO ~3.9 km/s


def test_world_state_persistence(tmp_path):
    p = str(tmp_path / "state.json")
    s = WorldState()
    s.add(Entity(id="x", kind="body", subsystem="terrestrial",
                 position=[1, 2, 3], attributes={"k": "v"}))
    s.sim_time = 12.5
    s.save(p)
    s2 = WorldState.load(p)
    assert s2.sim_time == 12.5
    assert s2.get("x").attributes["k"] == "v"
    assert s2.get("x").position == [1, 2, 3]


def test_planet_elevation_returns_biome():
    from universe_engine.terrestrial.planet import PlanetEarth
    from universe_engine.core.config import EngineConfig
    pe = PlanetEarth(EngineConfig(), WorldState())
    info = pe.elevation_at(0.0, 0.0)
    assert "elevation_m" in info and "biome" in info
    assert info["biome"] in universe_biomes()


def universe_biomes():
    from universe_engine.terrestrial.planet import BIOMES
    return BIOMES


def test_ecology_runs_and_populates():
    from universe_engine.terrestrial.ecology import EcologySim
    from universe_engine.core.config import EngineConfig
    eco = EcologySim(EngineConfig(), WorldState())
    r = eco.update(0.5)
    assert r["population"] > 0
    assert r["individuals_alive"] > 0


def test_iss_traverse_and_dock():
    from universe_engine.aerospace.iss import ISSEnvironment
    from universe_engine.core.config import EngineConfig
    iss = ISSEnvironment(EngineConfig(), WorldState())
    iss.initialize()
    assert iss.dock("port-fwd", "dragon") is True
    assert "Harmony" in iss.board("port-fwd")
    route = iss.traverse("Unity", "Kibo")
    assert route[0] == "Unity" and route[-1] == "Kibo"


def test_warp_shortest_path():
    from universe_engine.interstellar.warp import WarpNetwork
    from universe_engine.core.config import EngineConfig
    wn = WarpNetwork(EngineConfig(), WorldState())
    wn.initialize()
    ids = list(wn.systems.keys())
    res = wn.shortest_path(ids[0], ids[-1])
    assert res["ok"] is True
    assert res["from"] == ids[0] and res["to"] == ids[-1]


def test_blackhole_renders_image():
    from universe_engine.interstellar.blackhole import BlackHole
    bh = BlackHole(mass_msun=4.3e6)
    img = bh.render(width=48, height=32, steps=300, disk=True)
    arr = np.array(img)
    assert arr.shape == (32, 48, 3)
    # there must be a dark photon-ring region (shadow) and bright disk pixels
    assert arr.min() < 20                      # shadow / space
    assert arr.max() > 40                      # disk / stars


def test_renderer_renders_image():
    import numpy as np
    from universe_engine.interstellar.renderer import (
        Renderer, Scene, Sphere, Material, DirectionalLight,
        default_space_background)
    scene = Scene(
        spheres=[Sphere(np.array([0, 0, -3]), 1.0,
                        Material(color=np.array([0.9, 0.2, 0.2]), reflectivity=0.3))],
        light=DirectionalLight(np.array([0.5, 0.8, 0.3]),
                               np.array([1, 1, 1]), 1.2),
        background_fn=default_space_background)
    cam = {"eye": np.array([0, 0, 0]),
           "forward": np.array([0, 0, -1]),
           "up": np.array([0, 1, 0]), "fov": 50}
    r = Renderer(width=40, height=30)
    img = r.render(scene, cam, bounces=1, shadows=True)
    arr = np.array(img)
    assert arr.shape == (30, 40, 3)


def test_bug_hunter_one_cycle(tmp_path):
    from universe_engine.swarm.bug_hunter import cycle
    rec = cycle(str(tmp_path / "bh.jsonl"), 1, do_tests=False)
    assert rec["status"] in ("nominal", "issues")
    assert "tests" in rec


def test_optimization_sage_one_cycle(tmp_path):
    from universe_engine.swarm.optimization_sage import cycle
    rec = cycle(str(tmp_path / "os.jsonl"), 1, {})
    assert rec["status"] in ("nominal", "regression")
    assert rec["mem_peak_mb"] >= 0


def test_cockpit_walkable_interact():
    from universe_engine.aerospace.cockpit import CockpitSystem
    cs = CockpitSystem()
    cp = cs.get("cesna_172")
    assert cp is not None
    assert cp.interact("pilot_seat", "throttle", 0.8) is True
    assert abs(cp.stations["pilot_seat"]["throttle"].value - 0.8) < 1e-9
    assert "pilot_seat" in cp.walk_to("pilot_seat")
