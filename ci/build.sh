#!/usr/bin/env bash
# Compile the requested MCSM sources, then overlay fresh classes and assets on
# the supplied base mod. Compilation failure MUST NOT produce a release JAR.
# Usage: bash ci/build.sh [1.9.101]  (JDK 25+, Python 3.9+, curl, unzip, zip)
set -euo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
VER="${1:-$(tr -d '[:space:]' < VERSION)}"
[[ "$VER" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "Invalid version: $VER" >&2; exit 1; }
JAR_ID="${VER}-26.2-beta-mcsm"
OUT="$ROOT/out/dabywitherstormmod-${JAR_ID}.jar"
mkdir -p "$ROOT/out"
# Prevent a failed repeat build from leaving an old artifact that looks fresh.
rm -f "$OUT" "$OUT.sha256" "$ROOT/out/BUILD_INFO.txt" "$ROOT/out/javac.log"

if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
for tool in javac python3 curl unzip zip; do
    command -v "$tool" >/dev/null || { echo "[build] Missing $tool (JDK 25+ is required)." >&2; exit 1; }
done
JAVAC_VERSION="$(javac -version 2>&1)"
JAVA_MAJOR="$(printf '%s\n' "$JAVAC_VERSION" | sed -n 's/^javac \([0-9]*\).*/\1/p')"
[[ -n "$JAVA_MAJOR" && "$JAVA_MAJOR" -ge 25 ]] || { echo "[build] JDK 25+ required; found $JAVAC_VERSION" >&2; exit 1; }

BASE="${MCSM_BASE_JAR:-$(python3 - <<'PY'
from pathlib import Path
import re
jars = []
for p in Path('delivery').glob('dabywitherstormmod-*-26.2-beta-mcsm.jar'):
    m = re.fullmatch(r'dabywitherstormmod-(\d+)\.(\d+)\.(\d+)-26\.2-beta-mcsm\.jar', p.name)
    if m:
        jars.append((tuple(map(int, m.groups())), str(p.resolve())))
if not jars:
    raise SystemExit('No base mod JAR found in delivery/')
print(max(jars)[1])
PY
)}"
BASE="$(python3 -c 'from pathlib import Path; import sys; print(Path(sys.argv[1]).resolve())' "$BASE")"
unzip -t "$BASE" >/dev/null
printf '[build] MCSM %s\n[build] Base: %s\n[build] %s\n' "$JAR_ID" "$BASE" "$JAVAC_VERSION"

DL="$ROOT/.cache/mcsm/deps"
python3 ci/download_deps.py "$DL"
CP="$(cat "$DL/classpath.txt"):$BASE"

python3 glslcheck/shimcheck.py mcsm-core-shaders \
    jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh \
    jar-overrides/assets/dabywitherstormmod/shaders/post/storm_sun_glow.fsh \
    | tee "$ROOT/out/glsl.log"

WORK="$(mktemp -d "$ROOT/.cache/mcsm/build.XXXXXXXX")"
trap 'rm -rf -- "$WORK"' EXIT
mkdir -p "$WORK/classes" "$WORK/jar"
mapfile -d '' SOURCES < <(find mcsm-extras/java -name '*.java' -print0 | sort -z)
[[ ${#SOURCES[@]} -gt 0 ]] || { echo '[javac] No Java sources found' >&2; exit 1; }
if javac -nowarn --release 25 -proc:none -cp "$CP" -d "$WORK/classes" \
    "${SOURCES[@]}" > "$ROOT/out/javac.log" 2>&1; then
    cat "$ROOT/out/javac.log"
else
    status=$?
    cat "$ROOT/out/javac.log" >&2
    if [[ "${GITHUB_ACTIONS:-}" == true ]]; then
        # Logs/artifact hosts may be unreachable from the agent. An annotation
        # exposes the real compiler errors through the GitHub Checks API too.
        python3 - <<'PY'
from pathlib import Path
log = Path('out/javac.log').read_text(errors='replace')[:50000]
log = log.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')
print('::error title=Java compilation failed::' + log)
PY
    fi
    echo '[javac] FAILED. No JAR assembled; old classes are NOT a fallback.' >&2
    exit "$status"
fi
N_CLASSES="$(find "$WORK/classes" -name '*.class' | wc -l | tr -d '[:space:]')"
[[ "$N_CLASSES" -gt 0 ]] || { echo '[javac] No classes emitted' >&2; exit 1; }
echo "[javac] OK: $N_CLASSES fresh classes from ${#SOURCES[@]} sources"

(cd "$WORK/jar" && unzip -q "$BASE")
mkdir -p "$WORK/jar/assets/minecraft/shaders"
cp -R mcsm-core-shaders/. "$WORK/jar/assets/minecraft/shaders/"
cp -R jar-overrides/. "$WORK/jar/"
cp -R "$WORK/classes/." "$WORK/jar/"
python3 ci/verify_build.py "$WORK/jar" "$WORK/classes" "$BASE" "$JAR_ID" "$JAVAC_VERSION"
(cd "$WORK/jar" && zip -q -r -X "$WORK/assembled.jar" . -x '.*')
unzip -t "$WORK/assembled.jar" >/dev/null
mv "$WORK/assembled.jar" "$OUT"
cp "$WORK/jar/META-INF/mcsm-build.json" "$ROOT/out/BUILD_INFO.txt"
python3 - "$OUT" <<'PY'
import hashlib
from pathlib import Path
import sys
p = Path(sys.argv[1])
sha = hashlib.sha256(p.read_bytes()).hexdigest()
p.with_suffix(p.suffix + '.sha256').write_text(f'{sha}  {p.name}\n')
print(f'[sha256] {sha}')
PY
printf '[done] %s\n' "$OUT"
if [[ "${GITHUB_ACTIONS:-}" == true ]]; then
    echo "::notice title=Full Java build verified::${JAR_ID}: ${N_CLASSES} fresh Java 25 classes; shaders and mixin registrations verified."
fi
