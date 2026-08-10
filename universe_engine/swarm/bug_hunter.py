"""Agent 11 - Bug Hunter (perpetual background loop).

A REAL long-running process. Each cycle it:
  * discovers the engine's test suite,
  * runs it via python -m pytest (falls back to unittest),
  * runs a lightweight AST/static scan for obvious smells,
  * appends structured findings to a JSONL telemetry log.

It writes a heartbeat + latest findings so the conductor (Agent 13) can observe
it is alive and what it found. Designed to be launched as a detached process:

    python -m universe_engine.swarm.bug_hunter --cycle 30
"""
from __future__ import annotations

import argparse
import ast
import json
import os
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def _ensure_log(path: str) -> None:
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)


def _emit(path: str, record: dict) -> None:
    _ensure_log(path)
    with open(path, "a") as f:
        f.write(json.dumps(record) + "\n")


def run_tests() -> dict:
    """Run the test suite; return a result summary.

    We deselect the two meta-tests that themselves invoke the Bug Hunter /
    Optimization Sage cycles, so the perpetual loop never recurses into itself.
    """
    tests_dir = ROOT / "tests"
    if not tests_dir.exists():
        return {"ok": True, "ran": 0, "failures": 0, "note": "no tests dir yet"}
    py = sys.executable
    deselect = [
        "--deselect", "tests/test_engine.py::test_bug_hunter_one_cycle",
        "--deselect", "tests/test_engine.py::test_optimization_sage_one_cycle",
    ]
    try:
        proc = subprocess.run(
            [py, "-m", "pytest", str(tests_dir), "-q", "--tb=line",
             "-p", "no:cacheprovider", *deselect],
            capture_output=True, text=True, timeout=120, cwd=str(ROOT))
        out = proc.stdout + proc.stderr
        ran = fail = err = 0
        for line in out.splitlines():
            if "passed" in line or "failed" in line or "error" in line:
                # e.g. "3 passed in 0.12s" or "1 failed, 2 passed"
                for tok in line.split():
                    if tok.isdigit():
                        pass
        # crude parse
        import re
        m = re.search(r"(\d+) passed", out)
        if m: ran += int(m.group(1))
        m = re.search(r"(\d+) failed", out)
        if m: fail += int(m.group(1)); ran += int(m.group(1))
        m = re.search(r"(\d+) error", out)
        if m: err += int(m.group(1)); ran += int(m.group(1))
        return {"ok": proc.returncode == 0, "ran": ran, "failures": fail,
                "errors": err, "returncode": proc.returncode,
                "tail": out[-400:]}
    except subprocess.TimeoutExpired:
        return {"ok": False, "ran": 0, "failures": 0, "errors": 0,
                "note": "test run timed out"}
    except Exception as e:
        # pytest not installed -> fall back to unittest
        proc = subprocess.run(
            [py, "-m", "unittest", "discover", "-s", str(tests_dir), "-q"],
            capture_output=True, text=True, timeout=120, cwd=str(ROOT))
        out = proc.stdout + proc.stderr
        import re
        ran = len(re.findall(r"\b(ok|FAIL|ERROR)\b", out))
        fail = out.count("FAIL") + out.count("ERROR")
        return {"ok": proc.returncode == 0, "ran": ran, "failures": fail,
                "errors": 0, "returncode": proc.returncode,
                "tail": out[-400:], "note": "unittest fallback"}


def static_scan() -> list:
    """Walk the package AST and flag obvious smells. Returns findings list."""
    findings = []
    pkg = ROOT / "universe_engine"
    for py in pkg.rglob("*.py"):
        try:
            tree = ast.parse(py.read_text())
        except SyntaxError as e:
            findings.append({"file": str(py.relative_to(ROOT)),
                             "severity": "critical",
                             "msg": f"SyntaxError: {e}"})
            continue
        for node in ast.walk(tree):
            # bare except
            if isinstance(node, ast.ExceptHandler) and node.type is None:
                findings.append({"file": str(py.relative_to(ROOT)),
                                 "severity": "warn",
                                 "msg": "bare except: swallows all errors",
                                 "line": node.lineno})
            # except Exception without re-raise in a 1-line body is fine, but
            # `except: pass` is a smell.
            if (isinstance(node, ast.ExceptHandler) and node.type is None
                    and isinstance(node.body, list) and node.body
                    and isinstance(node.body[-1], ast.Pass)):
                findings.append({"file": str(py.relative_to(ROOT)),
                                 "severity": "high",
                                 "msg": "bare except: pass - silent failure",
                                 "line": node.lineno})
    return findings


def cycle(log_path: str, cycle_id: int, do_tests: bool = True) -> dict:
    t0 = time.time()
    tests = run_tests() if do_tests else {
        "ok": True, "ran": 0, "failures": 0, "errors": 0,
        "note": "skipped (invoked from within test suite)"}
    scan = static_scan()
    record = {
        "agent": 11, "name": "Bug Hunter", "cycle": cycle_id,
        "ts": time.time(), "dur_s": round(time.time() - t0, 3),
        "tests": tests, "static_findings": scan,
        "findings_count": len(scan) + tests.get("failures", 0) + tests.get("errors", 0),
        "status": "nominal" if (tests.get("ok") and not scan) else "issues",
    }
    _emit(log_path, record)
    # heartbeat file
    hb = Path(log_path).with_suffix(".heartbeat")
    hb.write_text(json.dumps({"agent": 11, "ts": time.time(),
                              "cycle": cycle_id, "status": record["status"]}))
    return record


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--cycle", type=float, default=30.0,
                    help="seconds between hunt cycles")
    ap.add_argument("--log", default="logs/bug_hunter.jsonl")
    ap.add_argument("--max-cycles", type=int, default=0,
                    help="stop after N cycles (0 = forever)")
    args = ap.parse_args()
    n = 0
    while True:
        n += 1
        try:
            rec = cycle(args.log, n)
            print(f"[BugHunter #{n}] status={rec['status']} "
                  f"findings={rec['findings_count']} tests={rec['tests'].get('ran',0)}",
                  flush=True)
        except Exception as e:
            _emit(args.log, {"agent": 11, "cycle": n, "ts": time.time(),
                             "status": "crash", "error": repr(e)})
            print(f"[BugHunter #{n}] CRASH: {e}", flush=True)
        if args.max_cycles and n >= args.max_cycles:
            break
        time.sleep(args.cycle)


if __name__ == "__main__":
    main()
