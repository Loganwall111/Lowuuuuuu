#!/usr/bin/env bash
# =============================================================================
# restore-missing-classes.sh
#
# Recovers the "missing classes" from the ORIGINAL jar and drops them into the
# current source tree, rewriting them into the rebranded package namespace.
#
# WHY THIS WORKS: every missing class still exists as a .class inside
#   dabywitherstormmod-1.9.60-26.2-beta.zip
# which is preserved in the repo root as the historical upstream binary.
#
# RUN THIS ON A MACHINE WITH JAVA 25+ AND INTERNET (not the Arena sandbox).
#   bash tools/restore-missing-classes.sh
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")/.."

JAR="dabywitherstormmod-1.9.60-26.2-beta.zip"
OUT="build/decompiled"
VINEFLOWER_URL="https://repo1.maven.org/maven2/org/vineflower/vineflower/1.10.1/vineflower-1.10.1.jar"
VF="build/vineflower.jar"
DEST_ROOT="src/main/java/net/dabicco/devouringstorms"

rewrite_namespace() {
  local dir="$1"
  python - "$dir" <<'PY'
from pathlib import Path
import sys
root = Path(sys.argv[1])
for path in root.rglob('*.java'):
    text = path.read_text()
    text = text.replace('net.dabicco.witherstormmod', 'net.dabicco.devouringstorms')
    path.write_text(text)
PY
}

echo "==> 1. Checking prerequisites"
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is required. Install JDK 25+ and re-run." >&2
  exit 1
fi
if [ ! -f "$JAR" ]; then
  echo "ERROR: $JAR not found. Run: git checkout 4287fad -- $JAR" >&2
  exit 1
fi

echo "==> 2. Downloading Vineflower decompiler"
mkdir -p build
if [ ! -f "$VF" ]; then
  curl -L -o "$VF" "$VINEFLOWER_URL" || wget -O "$VF" "$VINEFLOWER_URL"
fi

echo "==> 3. Decompiling the jar"
rm -rf "$OUT"
mkdir -p "$OUT"
java -jar "$VF" --renamer=default -mixin=0 -log=ERROR "$JAR" "$OUT"

echo "==> 4. Copying missing .java files into the source tree"
SRC="$(cd "$OUT" && pwd)"
RESTORED=0
copy_pkg() {
  local rel="$1"
  local from="$SRC/net/dabicco/witherstormmod/$rel"
  if [ -d "$from" ]; then
    local to="$DEST_ROOT/$rel"
    mkdir -p "$to"
    local n
    n=$(find "$from" -name '*.java' | wc -l)
    if [ "$n" -gt 0 ]; then
      cp -r "$from"/. "$to"/
      rewrite_namespace "$to"
      RESTORED=$((RESTORED + n))
      echo "  restored $rel/  ($n files)"
    fi
  fi
}

copy_pkg "mixin"
copy_pkg "entity/model"
copy_pkg "entity/renderer"
copy_pkg "entity/state"
copy_pkg "entity/withered"
copy_pkg "item"
copy_pkg "menu"
copy_pkg "nether"
copy_pkg "network"

echo
echo "==> 5. Summary: restored $RESTORED .java files into $DEST_ROOT"
echo
echo "NOTE: decompiled source may still need small compile fixes against the clean"
echo "rewrite (renamed methods, deleted legacy helpers, etc.). Build next and fix"
echo "the remaining errors from there."
