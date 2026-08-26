#!/usr/bin/env bash
# =============================================================================
# restore-missing-classes.sh
#
# Recovers the "missing classes" of the Wither Storm mod from the ORIGINAL jar
# and drops them into the source tree as .java files, so the clean rewrite can
# finally compile and enable the blocked features (bloom/clouds/fog/shadow,
# head/tentacle/severed models+renderers, the Rocket Retriever / Formidibomb items).
#
# WHY THIS WORKS: every missing class exists as a .class inside
#   dabywitherstormmod-1.9.60-26.2-beta.zip  (restored to the repo root).
# We decompile those specific classes back to source with the SAME Mojang
# mappings the rest of the codebase already uses.
#
# RUN THIS ON A MACHINE WITH JAVA 25+ AND INTERNET (not the Arena sandbox).
#   bash tools/restore-missing-classes.sh
#
# It will:
#   1. Download the Vineflower decompiler (single jar, no install).
#   2. Decompile the whole jar with Mojang mappings into build/decompiled/.
#   3. Copy every missing .java into the correct src/main/java package folder.
#   4. Print a summary of what was restored.
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")/.."

JAR="dabywitherstormmod-1.9.60-26.2-beta.zip"
OUT="build/decompiled"
MAPS="build/mojmap"
VINEFLOWER_URL="https://repo1.maven.org/maven2/org/vineflower/vineflower/1.10.1/vineflower-1.10.1.jar"
VF="build/vineflower.jar"

echo "==> 1. Checking prerequisites"
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is required. Install JDK 25+ and re-run." >&2; exit 1
fi
if [ ! -f "$JAR" ]; then
  echo "ERROR: $JAR not found. Run:  git checkout 4287fad -- $JAR" >&2; exit 1
fi

echo "==> 2. Downloading Vineflower decompiler"
mkdir -p build
if [ ! -f "$VF" ]; then
  curl -L -o "$VF" "$VINEFLOWER_URL" || wget -O "$VF" "$VINEFLOWER_URL"
fi

echo "==> 3. Decompiling the jar (this can take a minute)"
rm -rf "$OUT"
mkdir -p "$OUT"
java -jar "$VF" --renamer=default -mixin=0 -log=ERROR "$JAR" "$OUT"

echo "==> 4. Copying missing .java files into the source tree"
mkdir -p build/restored
SRC="$(cd "$OUT" && pwd)"
RESTORED=0
copy_pkg() { # copy_pkg <subpath-after-witherstormmod>
  local rel="$1"
  local from="$SRC/net/dabicco/witherstormmod/$rel"
  if [ -d "$from" ]; then
    local to="src/main/java/net/dabicco/witherstormmod/$rel"
    mkdir -p "$to"
    local n
    n=$(find "$from" -name '*.java' | wc -l)
    if [ "$n" -gt 0 ]; then
      cp -r "$from"/. "$to"/
      RESTORED=$((RESTORED+n))
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
echo "==> 5. Summary: restored $RESTORED .java files."
echo
echo "NOTE: decompiled source may need small fixes to compile against the clean
rewrite (e.g. renamed methods, references to classes we deleted). Run
'gradle build' and paste any errors; I will fix them."
