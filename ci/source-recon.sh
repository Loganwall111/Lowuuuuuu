#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Devouring Storms -- SOURCE RECON (Phase 0 of docs/WITHER_STORM_FEATURE_ROADMAP.md).
#
# The author's clean source in net/ is 1.9.60 generation: 171 files, and it
# predates the whole structures package (McsmWorldgen, McsmCommand,
# McsmSchematic, StructureBuilder, McsmGuidebook, LegacyBlocks) plus 40
# versions of fixes. The shipping jar is built on the hash-pinned 1.9.100
# base. To own the mod at source level we need source for EVERY class in
# that jar, so this script recovers it: Vineflower decompiles all 385
# net/dabicco classes out of the base jar and the result is committed back
# to the branch under src-recon/ by the calling workflow.
#
# src-recon/ is the CURRENT-generation source of record; net/ stays as the
# author's cleaner-named 1.9.60 reference for anything the decompiler mangled.
# ---------------------------------------------------------------------------
set -euo pipefail

DL=/tmp/srcrecon
rm -rf "$DL" src-recon
mkdir -p "$DL/out"

BASE_VER="1.9.100"
BASE_NAME="dabywitherstormmod-${BASE_VER}-26.2-beta-mcsm.jar"
BASE_SHA="6adcf07e1ad810703c12cb25d7d135aca7b8f66f7d12c273ad3f00b5abdb6599"
curl -fsSL --retry 3 --retry-delay 3 -o "$DL/$BASE_NAME" \
  "https://github.com/Loganwall111/Lowuuuuuu/releases/download/mcsm-${BASE_VER}/${BASE_NAME}"
echo "$BASE_SHA  $DL/$BASE_NAME" | sha256sum -c -

(cd "$DL" && unzip -qo "$BASE_NAME" 'net/dabicco/*')
N_CLASSES=$(find "$DL/net" -name '*.class' | wc -l)
echo "[recon] $N_CLASSES mod classes extracted from the base jar"

VF="$DL/vineflower.jar"
curl -fsSL --retry 2 -o "$VF" \
     "https://repo1.maven.org/maven2/org/vineflower/vineflower/1.11.1/vineflower-1.11.1.jar" \
  || curl -fsSL --retry 2 -o "$VF" \
     "https://github.com/Vineflower/vineflower/releases/download/1.11.1/vineflower-1.11.1.jar"

java -jar "$VF" "$DL/net" "$DL/out" > "$DL/vineflower.log" 2>&1 || {
  echo "[recon] vineflower exited non-zero; tail of log:"
  tail -40 "$DL/vineflower.log" || true
  exit 1
}

mkdir -p src-recon
cp -r "$DL/out/net" src-recon/
N_JAVA=$(find src-recon -name '*.java' | wc -l)
{
  echo "base jar:      $BASE_NAME"
  echo "base sha256:   $BASE_SHA"
  echo "classes in:    $N_CLASSES"
  echo "java files out: $N_JAVA"
  echo "vineflower:    1.11.1"
  echo "date:          $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "--- vineflower log lines matching ERROR (first 40) ---"
  grep -E "ERROR" "$DL/vineflower.log" | head -40 || echo "(none)"
} > src-recon/RECON_SUMMARY.txt

du -sh src-recon || true
echo "[recon] done: $N_JAVA java files recovered under src-recon/"
