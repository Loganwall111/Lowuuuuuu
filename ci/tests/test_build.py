"""Offline tests for download integrity and fail-closed JAR assembly (no JDK needed)."""

from contextlib import contextmanager
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import shutil
import struct
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

ROOT = Path(__file__).resolve().parents[2]


def load(name):
    spec = importlib.util.spec_from_file_location(name, ROOT / "ci" / (name + ".py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


deps = load("download_deps")
verify = load("verify_build")


@contextmanager
def working_directory(path):
    before = Path.cwd()
    os.chdir(path)
    try:
        yield
    finally:
        os.chdir(before)


class DownloadTests(unittest.TestCase):
    def test_verified_cache_avoids_network(self):
        with tempfile.TemporaryDirectory() as tmp:
            jar = Path(tmp) / "cache.jar"
            with zipfile.ZipFile(jar, "w") as z:
                z.writestr("example", "complete")
            digest = hashlib.sha1(jar.read_bytes()).hexdigest()
            with patch.object(deps.subprocess, "run") as run:
                self.assertEqual(jar, deps.download("https://example.invalid/a.jar", jar, digest))
                run.assert_not_called()

    def test_bad_checksum_never_becomes_cached_jar(self):
        with tempfile.TemporaryDirectory() as tmp:
            jar = Path(tmp) / "cache.jar"
            def fake_curl(args, **kwargs):
                Path(args[args.index("--output") + 1]).write_bytes(b"incomplete download")
            with patch.object(deps.subprocess, "run", side_effect=fake_curl):
                with self.assertRaisesRegex(ValueError, "SHA-1 mismatch"):
                    deps.download("https://example.invalid/a.jar", jar, "0" * 40)
            self.assertFalse(jar.exists())
            self.assertFalse(jar.with_name("cache.jar.part").exists())

    def test_html_is_not_accepted_as_a_jar(self):
        with tempfile.TemporaryDirectory() as tmp:
            jar = Path(tmp) / "cache.jar"
            def fake_curl(args, **kwargs):
                Path(args[args.index("--output") + 1]).write_text("<html>not a JAR</html>")
            with patch.object(deps.subprocess, "run", side_effect=fake_curl):
                with self.assertRaises(zipfile.BadZipFile):
                    deps.download("https://example.invalid/a.jar", jar)
            self.assertFalse(jar.exists())
            self.assertFalse(jar.with_name("cache.jar.part").exists())

    def test_failed_download_removes_partial_file(self):
        with tempfile.TemporaryDirectory() as tmp:
            jar = Path(tmp) / "cache.jar"
            def fake_curl(args, **kwargs):
                Path(args[args.index("--output") + 1]).write_bytes(b"partial")
                raise subprocess.CalledProcessError(22, args)
            with patch.object(deps.subprocess, "run", side_effect=fake_curl):
                with self.assertRaises(subprocess.CalledProcessError):
                    deps.download("https://example.invalid/a.jar", jar)
            self.assertFalse(jar.with_name("cache.jar.part").exists())


class AssemblyTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.cwd = working_directory(self.root)
        self.cwd.__enter__()
        self.addCleanup(self.cwd.__exit__, None, None, None)
        for name in ("out", "staged", "classes/net/mcsm", "staged/net/mcsm", "mcsm-extras/java/net/mcsm",
                     "mcsm-core-shaders", "jar-overrides"):
            Path(name).mkdir(parents=True, exist_ok=True)
        # Header fixture only: these tests verify the assembly gate, not javac.
        self.bytecode = b"\xca\xfe\xba\xbe" + struct.pack(">HH", 0, 69) + b"fixture"
        Path("classes/net/mcsm/Example.class").write_bytes(self.bytecode)
        Path("staged/net/mcsm/Example.class").write_bytes(self.bytecode)
        Path("mcsm-extras/java/net/mcsm/Example.java").write_text("package net.mcsm; class Example {}")
        Path("base.jar").write_bytes(b"base fixture")
        Path("staged/fabric.mod.json").write_text(json.dumps({"version": "old", "mixins": ["mixins.json"]}))
        Path("staged/mixins.json").write_text(json.dumps({"package": "net.mcsm", "client": ["Example"]}))
        self.git = patch.object(verify.subprocess, "check_output", side_effect=lambda args, **kw: "abc123\n" if "rev-parse" in args else "")
        self.git.start()
        self.addCleanup(self.git.stop)

    def check(self):
        return verify.verify("staged", "classes", "base.jar", "1.9.101-26.2-beta-mcsm", "javac 25")

    def test_fresh_classes_recorded_and_version_stamped(self):
        report = self.check()
        self.assertEqual("FULL BUILD", report["verdict"])
        self.assertEqual(1, report["fresh_class_count"])
        self.assertEqual("1.9.101-26.2-beta-mcsm", json.loads(Path("staged/fabric.mod.json").read_text())["version"])
        self.assertEqual(report, json.loads(Path("staged/META-INF/mcsm-build.json").read_text()))

    def test_stale_class_rejected(self):
        Path("staged/net/mcsm/Example.class").write_bytes(self.bytecode + b"old")
        with self.assertRaisesRegex(ValueError, "Stale class"):
            self.check()

    def test_missing_source_class_rejected(self):
        Path("mcsm-extras/java/net/mcsm/Missing.java").write_text("class Missing {}")
        with self.assertRaisesRegex(ValueError, "did not emit"):
            self.check()

    def test_wrong_java_version_rejected(self):
        Path("classes/net/mcsm/Example.class").write_bytes(b"\xca\xfe\xba\xbe" + struct.pack(">HH", 0, 65))
        with self.assertRaisesRegex(ValueError, "Not Java 25"):
            self.check()

    def test_missing_registered_mixin_rejected(self):
        Path("staged/mixins.json").write_text(json.dumps({"package": "net.mcsm", "mixins": ["Missing"]}))
        with self.assertRaisesRegex(ValueError, "Registered mixin is missing"):
            self.check()

    def test_stale_asset_rejected(self):
        Path("jar-overrides/example.txt").write_text("new")
        Path("staged/example.txt").write_text("old")
        with self.assertRaisesRegex(ValueError, "Asset overlay mismatch"):
            self.check()


class BuildFailureTests(unittest.TestCase):
    def test_compiler_failure_removes_old_output_and_never_packages(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            for name in ("ci", "delivery", "out", "bin", "mcsm-extras/java"):
                (root / name).mkdir(parents=True)
            shutil.copy(ROOT / "ci/build.sh", root / "ci/build.sh")
            (root / "VERSION").write_text("1.9.101\n")
            (root / "mcsm-extras/java/Example.java").write_text("class Example {}")
            with zipfile.ZipFile(root / "delivery/dabywitherstormmod-1.9.100-26.2-beta-mcsm.jar", "w") as z:
                z.writestr("fabric.mod.json", "{}")
            output = root / "out/dabywitherstormmod-1.9.101-26.2-beta-mcsm.jar"
            output.write_text("old output must not survive")
            output.with_suffix(".jar.sha256").write_text("old checksum")
            # Exercise the shell's failure path, not a real Java compilation.
            scripts = {
                "javac": '#!/bin/sh\nif [ "$1" = "-version" ]; then echo "javac 25"; exit 0; fi\necho "forced compile failure" >&2\nexit 7\n',
                "zip": '#!/bin/sh\ntouch PACKAGING_WAS_CALLED\nexit 0\n',
                "python3": f'''#!{sys.executable}
import os, pathlib, sys
if sys.argv[1] == "ci/download_deps.py":
    cache = pathlib.Path(sys.argv[2]); cache.mkdir(parents=True)
    (cache / "classpath.txt").write_text("compile-fixture.jar")
elif sys.argv[1] == "glslcheck/shimcheck.py":
    print("fixture shader gate")
else:
    os.execv({sys.executable!r}, [{sys.executable!r}] + sys.argv[1:])
''',
            }
            for name, text in scripts.items():
                exe = root / "bin" / name
                exe.write_text(text)
                exe.chmod(0o755)
            env = dict(os.environ, PATH=str(root / "bin") + os.pathsep + os.environ["PATH"])
            env.pop("JAVA_HOME", None)
            env.pop("MCSM_BASE_JAR", None)
            result = subprocess.run(["bash", str(root / "ci/build.sh")], cwd=root, env=env, text=True, capture_output=True)
            self.assertEqual(7, result.returncode, result.stdout + result.stderr)
            self.assertIn("old classes are NOT a fallback", result.stderr)
            self.assertFalse(output.exists())
            self.assertFalse(output.with_suffix(".jar.sha256").exists())
            self.assertFalse((root / "PACKAGING_WAS_CALLED").exists())


if __name__ == "__main__":
    unittest.main()
