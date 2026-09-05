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
# The sandbox cannot read runner logs, only annotations -- so every failure
# path here dies through ::error:: with the actual message attached.
# ---------------------------------------------------------------------------
set -euo pipefail

die() { # die <stage> <detail-file-or-empty>
  local detail=""
  if [ -n "${2:-}" ] && [ -f "$2" ]; then
    detail=$(tail -c 1800 "$2" | tr '\n\r\t' '   ')
  fi
  echo "::error title=source-recon [$1]::${detail:-no detail captured}"
  exit 1
}

DL=/tmp/srcrecon
rm -rf "$DL" src-recon
mkdir -p "$DL/out"

BASE_VER="1.9.100"
BASE_NAME="dabywitherstormmod-${BASE_VER}-26.2-beta-mcsm.jar"
BASE_SHA="6adcf07e1ad810703c12cb25d7d135aca7b8f66f7d12c273ad3f00b5abdb6599"

echo "[recon] stage 1: fetch base jar"
curl -fsSL --retry 3 --retry-delay 3 -o "$DL/$BASE_NAME" \
  "https://github.com/Loganwall111/Lowuuuuuu/releases/download/mcsm-${BASE_VER}/${BASE_NAME}" \
  || die "base jar download failed"
echo "$BASE_SHA  $DL/$BASE_NAME" | sha256sum -c - || die "base jar hash mismatch"
(cd "$DL" && unzip -qo "$BASE_NAME" 'net/dabicco/*') || die "unzip failed"
N_CLASSES=$(find "$DL/net" -name '*.class' | wc -l)
echo "[recon] stage 1 ok: $N_CLASSES mod classes extracted"
[ "$N_CLASSES" -gt 300 ] || die "only $N_CLASSES classes extracted, expected 385"

echo "[recon] stage 2: fetch Vineflower"
VF="$DL/vineflower.jar"
VF_OK=0
for url in \
  "https://github.com/Vineflower/vineflower/releases/download/1.11.1/vineflower-1.11.1.jar" \
  "https://repo1.maven.org/maven2/org/vineflower/vineflower/1.11.1/vineflower-1.11.1.jar" \
  "https://github.com/Vineflower/vineflower/releases/download/1.10.1/vineflower-1.10.1.jar" \
  "https://repo1.maven.org/maven2/org/vineflower/vineflower/1.10.1/vineflower-1.10.1.jar"
do
  if curl -fsSL --retry 2 -o "$VF" "$url" && [ -s "$VF" ]; then
    echo "[recon] stage 2 ok: vineflower from $url ($(stat -c%s "$VF") B)"
    VF_OK=1
    break
  fi
  echo "[recon] fetch failed: $url"
done
[ "$VF_OK" -eq 1 ] || die "vineflower download failed from all mirrors"

echo "[recon] stage 3: decompile"
# Vineflower exits non-zero if ANY single class trips it, even when the other
# 380 decompiled fine (run 33988616162 did exactly that). The tree it leaves
# behind is the real success criterion, so the exit code is captured,
# reported, and tolerated.
VF_RC=0
java -Xmx3g -jar "$VF" "$DL/net" "$DL/out" > "$DL/vineflower.log" 2>&1 || VF_RC=$?
echo "[recon] vineflower exit code: $VF_RC (tolerated if the tree is complete)"

mkdir -p src-recon
cp -r "$DL/out/net" src-recon/ 2>/dev/null || die "no decompiled tree produced (rc=$VF_RC)" "$DL/vineflower.log"
N_JAVA=$(find src-recon -name '*.java' | wc -l)
ERR_LINES=$(grep -cE "ERROR" "$DL/vineflower.log" || true)
echo "[recon] stage 3: $N_JAVA java files recovered, $ERR_LINES ERROR log lines"
# 385 classes include inner classes/records which fold into their outer
# file, so a complete recon lands in the high 200s / low 300s of .java files.
[ "$N_JAVA" -gt 250 ] || die "only $N_JAVA java files recovered (rc=$VF_RC)" "$DL/vineflower.log"

{
  echo "base jar:       $BASE_NAME"
  echo "base sha256:    $BASE_SHA"
  echo "classes in:     $N_CLASSES"
  echo "java files out: $N_JAVA"
  echo "vineflower rc:  $VF_RC"
  echo "vineflower log: $(grep -cE 'ERROR' "$DL/vineflower.log" || true) ERROR lines"
  echo "date:           $(date -u +%Y-%m-%dT%H:%M:%SZ)"
} > src-recon/RECON_SUMMARY.txt
grep -E "ERROR" "$DL/vineflower.log" | head -40 >> src-recon/RECON_SUMMARY.txt || true

echo "::notice title=source-recon::recovered $N_JAVA java files from $N_CLASSES classes (vineflower rc=$VF_RC, $ERR_LINES errors in log)"
du -sh src-recon || true
