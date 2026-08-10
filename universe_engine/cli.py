"""UniverseEngine command-line interface.

    python -m universe_engine.cli <command> [options]

Commands drive each subsystem and emit artifacts (images, reports) under
./artifacts/ . Run `all` for the full pipeline + artifact set.
"""
from __future__ import annotations

import argparse
import os
import time

ART = "artifacts"


def _ensure_art():
    os.makedirs(ART, exist_ok=True)


def cmd_swarm(args):
    from .swarm.agents import describe
    print(describe())


def cmd_engine(args):
    from . import UniverseEngine, EngineConfig
    eng = UniverseEngine(EngineConfig(enable_background_agents=False))
    eng.initialize()
    for _ in range(5):
        eng.step(1 / 30)
        eng.conductor.synchronize(1 / 30)
    import json
    print(json.dumps(eng.status(), indent=2, default=str))


def cmd_orbit(args):
    from .aerospace.orbit import OrbitalMechanics
    from .core.math_utils import MU_EARTH, R_EARTH
    om = OrbitalMechanics()
    els = {"a": 7_000_000.0, "e": 0.01, "i": 0.05, "raan": 0.0,
           "argp": 0.0, "ta": 0.0, "mu": MU_EARTH, "epoch": 0.0}
    pos0, vel0 = om.elements_to_state(els)
    period = 2 * 3.141592653589793 / om.mean_motion(els["a"], MU_EARTH)
    # propagate one full orbit and confirm we return to start
    els2 = om.propagate_elements(els, period)
    pos1, vel1 = om.elements_to_state(els2)
    import numpy as np
    err = float(np.linalg.norm(np.array(pos1) - np.array(pos0)))
    hoh = om.hohmann_delta_v(R_EARTH + 200_000, R_EARTH + 35_786_000)
    print(f"Orbital period: {period/60:.2f} min")
    print(f"Round-trip position error after 1 period: {err:.3e} m  (should be ~0)")
    print(f"Hohmann LEO->GEO: dv1={hoh['dv1']:.1f} dv2={hoh['dv2']:.1f} "
          f"total={hoh['dv_total']:.1f} m/s, tof={hoh['transfer_time_min']:.1f} min")


def cmd_rocket(args):
    from . import UniverseEngine, EngineConfig
    eng = UniverseEngine(EngineConfig(enable_background_agents=False))
    eng.initialize()
    res = eng.aerospace["rocket"].auto_pilot_to_orbit(
        target_alt_m=400_000, max_t=1200, dt=0.5)
    print(f"Apoapsis: {res['apoapsis_km']:.1f} km  "
          f"Periapsis: {res['periapsis_km']:.1f} km")
    print(f"Final alt: {res['final_alt_km']:.1f} km  "
          f"Final speed: {res['final_speed_mps']:.0f} m/s  "
          f"stage: {res['stage_reached']}")
    stable = res['periapsis_km'] > 100
    print(f"Stable orbit: {stable}  "
          f"({'circular LEO achieved' if stable else 'suborbital'})")


def cmd_blackhole(args):
    _ensure_art()
    from .interstellar.blackhole import BlackHole
    bh = BlackHole(mass_msun=args.mass, spin=args.spin)
    t0 = time.perf_counter()
    img = bh.render(width=args.width, height=args.height,
                    steps=args.steps, disk=not args.no_disk,
                    elevation_deg=args.elev)
    dt = time.perf_counter() - t0
    out = os.path.join(ART, args.out)
    img.save(out)
    print(f"Black hole rendered {img.size} in {dt:.2f}s -> {out}")
    print(bh.describe())


def cmd_render(args):
    _ensure_art()
    import numpy as np
    from .interstellar.renderer import (Renderer, Scene, Sphere, Material,
                                        DirectionalLight, default_space_background)
    scene = Scene(
        spheres=[
            Sphere(np.array([0.0, 0.0, -5.0]), 1.5,
                   Material(color=np.array([0.25, 0.5, 0.9]), reflectivity=0.2)),
            Sphere(np.array([2.4, 0.5, -6.0]), 0.8,
                   Material(color=np.array([0.9, 0.9, 0.95]), reflectivity=0.8,
                            specular=0.6)),
            Sphere(np.array([-2.2, -0.8, -4.5]), 0.7,
                   Material(color=np.array([0.8, 0.4, 0.2]), reflectivity=0.05)),
        ],
        light=DirectionalLight(direction=np.array([0.5, 0.8, 0.3]),
                               color=np.array([1.0, 0.97, 0.9]), intensity=1.3),
        background_fn=default_space_background,
    )
    cam = {"eye": np.array([0.0, 0.0, 0.0]),
           "forward": np.array([0.0, 0.0, -1.0]),
           "up": np.array([0.0, 1.0, 0.0]), "fov": 50.0}
    r = Renderer(width=args.width, height=args.height)
    t0 = time.perf_counter()
    img = r.render(scene, cam, bounces=args.bounces, shadows=True)
    dt = time.perf_counter() - t0
    out = os.path.join(ART, args.out)
    img.save(out)
    print(f"Scene rendered {img.size} bounces={args.bounces} in {dt:.2f}s -> {out}")


def cmd_ecology(args):
    _ensure_art()
    from . import UniverseEngine, EngineConfig
    eng = UniverseEngine(EngineConfig(enable_background_agents=False))
    eng.initialize()
    eco = eng.terrestrial["ecology"]
    rows = []
    t = 0.0
    for _ in range(args.steps):
        r = eco.update(0.5)
        rows.append((t, r["population"], r["individuals_alive"],
                     r["births"], r["deaths"]))
        t += 0.5
    import json
    out = os.path.join(ART, "ecology_report.json")
    with open(out, "w") as f:
        json.dump({"final_population": rows[-1][1],
                   "final_individuals": rows[-1][2],
                   "series": [{"t": r[0], "pop": r[1], "alive": r[2],
                               "births": r[3], "deaths": r[4]} for r in rows]},
                  f, indent=1)
    print(f"Ecology: {len(rows)} ticks. Final aggregate pop={rows[-1][1]:.1f}, "
          f"individuals_alive={rows[-1][2]} -> {out}")


def cmd_life(args):
    from . import UniverseEngine, EngineConfig
    eng = UniverseEngine(EngineConfig(enable_background_agents=False))
    eng.initialize()
    life = eng.terrestrial["life"]
    r = life.update(1.0)
    print(f"Cities: {r['cities']}  Citizens: {r['citizens']}  "
          f"Social events/tick: {r['social_events']}")
    print("Action distribution:", r["actions"])
    print("Sample cities:")
    for cid, c in list(life.cities.items())[:5]:
        print(f"  {c['name']:12} biome={c['biome']:10} pop={c['population']}")


def cmd_warp(args):
    from . import UniverseEngine, EngineConfig
    eng = UniverseEngine(EngineConfig(enable_background_agents=False))
    eng.initialize()
    wn = eng.interstellar["warp"]
    ids = list(wn.systems.keys())
    src = ids[0]; dst = ids[-1]
    res = wn.shortest_path(src, dst)
    print(f"Route {wn.systems[src]['name']} -> {wn.systems[dst]['name']}:")
    if res["ok"]:
        print(f"  hops={res['hops']} time={res['human_time']}")
        print("  path: " + " -> ".join(wn.systems[p]["name"] for p in res["path"]))
    else:
        print("  no route")


def cmd_aliens(args):
    _ensure_art()
    from . import UniverseEngine, EngineConfig
    eng = UniverseEngine(EngineConfig(enable_background_agents=False))
    eng.initialize()
    adb = eng.interstellar["aliens"]
    for _ in range(5):
        adb.update(1.0)
    pid = list(adb.planets.keys())[0]
    out = os.path.join(ART, args.out)
    adb.planet_biome_map(pid, size=160).save(out)
    print(f"Alien worlds: {len(adb.planets)}  Civilizations: {len(adb.civs)}")
    for cid, c in adb.civs.items():
        print(f"  {c.species:12} gov={c.gov:18} tech={c.tech:.1f} "
              f"pop={c.population_m:.1f}M relations={len(c.relations)}")
    print(f"Biome map -> {out}")


def cmd_iss(args):
    from . import UniverseEngine, EngineConfig
    eng = UniverseEngine(EngineConfig(enable_background_agents=False))
    eng.initialize()
    iss = eng.aerospace["iss"]
    iss.dock("port-fwd", "dragon")
    print(iss.board("port-fwd"))
    route = iss.traverse("Unity", "Kibo")
    print("Walkable route Unity -> Kibo:", " -> ".join(route))
    r = iss.update(10.0)
    print(f"Station orbit: a={r['orbit_a_km']:.1f} km e={r['orbit_e']:.4f}")
    print("EVA:", iss.eva())


def cmd_start_swarm(args):
    from . import UniverseEngine, EngineConfig
    cfg = EngineConfig(enable_background_agents=True)
    eng = UniverseEngine(cfg)
    eng.initialize()
    eng.run_background_swarm()
    print("Background swarm launched (Agents 11 & 12).")
    print("Heartbeats will appear under logs/. Status:")
    import time as _t, json
    _t.sleep(6)
    print(json.dumps(eng.conductor.status(), indent=2, default=str))
    print("\n(Leave running to keep perpetual agents active. "
          "Stop with: python -m universe_engine.cli stop-swarm)")
    # keep the foreground alive briefly so the user can see heartbeats
    eng.conductor.synchronize(1.0)
    if args.watch:
        for _ in range(args.watch):
            _t.sleep(5)
            eng.conductor.synchronize(1.0)
            print(json.dumps(eng.conductor.status()["background_agents"], default=str))
        eng.stop_background_swarm()


def cmd_stop_swarm(args):
    from . import UniverseEngine, EngineConfig
    eng = UniverseEngine(EngineConfig(enable_background_agents=True))
    eng.conductor.stop_background_agents()
    print("Background swarm stop signal sent.")


def cmd_all(args):
    for name, fn in [
        ("swarm", cmd_swarm), ("orbit", cmd_orbit), ("rocket", cmd_rocket),
        ("blackhole", cmd_blackhole), ("render", cmd_render),
        ("ecology", cmd_ecology), ("life", cmd_life), ("warp", cmd_warp),
        ("aliens", cmd_aliens), ("iss", cmd_iss),
    ]:
        print(f"\n===== {name.upper()} =====")
        try:
            fn(argparse.Namespace(**{k: getattr(args, k) for k in vars(args)
                                     if k not in ("func",)}))
        except Exception as e:
            print(f"[{name}] failed: {e!r}")


def build_parser():
    ap = argparse.ArgumentParser(prog="universe_engine")
    sub = ap.add_subparsers(dest="cmd", required=True)

    sub.add_parser("swarm").set_defaults(func=cmd_swarm)
    sub.add_parser("engine").set_defaults(func=cmd_engine)
    sub.add_parser("orbit").set_defaults(func=cmd_orbit)
    sub.add_parser("rocket").set_defaults(func=cmd_rocket)

    p = sub.add_parser("blackhole")
    p.add_argument("--width", type=int, default=320)
    p.add_argument("--height", type=int, default=200)
    p.add_argument("--steps", type=int, default=1400)
    p.add_argument("--mass", type=float, default=4.3e6)
    p.add_argument("--spin", type=float, default=0.0)
    p.add_argument("--elev", type=float, default=12.0)
    p.add_argument("--no-disk", action="store_true")
    p.add_argument("--out", default="blackhole.png")
    p.set_defaults(func=cmd_blackhole)

    p = sub.add_parser("render")
    p.add_argument("--width", type=int, default=360)
    p.add_argument("--height", type=int, default=240)
    p.add_argument("--bounces", type=int, default=2)
    p.add_argument("--out", default="scene.png")
    p.set_defaults(func=cmd_render)

    p = sub.add_parser("ecology")
    p.add_argument("--steps", type=int, default=40)
    p.set_defaults(func=cmd_ecology)

    sub.add_parser("life").set_defaults(func=cmd_life)
    sub.add_parser("warp").set_defaults(func=cmd_warp)

    p = sub.add_parser("aliens")
    p.add_argument("--out", default="alien_biome.png")
    p.set_defaults(func=cmd_aliens)
    sub.add_parser("iss").set_defaults(func=cmd_iss)

    p = sub.add_parser("start-swarm")
    p.add_argument("--watch", type=int, default=0)
    p.set_defaults(func=cmd_start_swarm)
    sub.add_parser("stop-swarm").set_defaults(func=cmd_stop_swarm)

    p = sub.add_parser("all")
    for k, v in [("width", 320), ("height", 200), ("steps", 1400),
                 ("mass", 4.3e6), ("spin", 0.0), ("elev", 12.0),
                 ("no_disk", False), ("out", "blackhole.png"),
                 ("bounces", 2), ("out_aliens", "alien_biome.png"),
                 ("out_scene", "scene.png"), ("n_ecology", 40), ("watch", 0)]:
        p.add_argument(f"--{k.replace('_', '-')}", dest=k, default=v)
    p.set_defaults(func=cmd_all)
    return ap


def main():
    ap = build_parser()
    args = ap.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
