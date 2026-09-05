#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Devouring Storms -- WHOLE-MOD SOURCE COMPILE (Track B step 2).
#
# Compiles the ENTIRE mod from source for the first time:
#   src-recon/   = current-generation source recovered from the 1.9.100 jar
#   mcsm-extras/ = the Devouring Storms overlay
# with NO base jar on the classpath -- the source replaces it. When this
# reaches parity with the jar's class count, the published mod contains zero
# of the original author's compiled bytes and the namespace rename becomes
# ours to make (Devouring Storms 2.0.0).
#
# Known exclusions (documented in src-recon/RECON_SUMMARY.txt):
#   entity/model/WitherStormDevourer.java -- vineflower could not decompile
#   it; until a CFR pass or a hand fix lands, that ONE class keeps coming
#   from the base jar at assembly time.
#
# This script only reports (annotations + out/source-build-report.txt); it
# never publishes. The shipping pipeline (build.sh) is untouched.
# ---------------------------------------------------------------------------
set -uo pipefail

DL=/tmp/ds-src-dl
mkdir -p "$DL" out

fetch() {
  local url="$1" out="$DL/$2"
  [ -s "$out" ] || curl -fsSL --retry 3 --retry-delay 3 -o "$out" "$url" || {
    echo "::error title=source-build::download failed: $url"; return 1; }
  echo "[deps] $2 $(stat -c%s "$out") B"
}

# --- vanilla client (official mojmap names, same as the shipping build) ---
MANIFEST="$(curl -fsSL https://piston-meta.mojang.com/mc/game/version_manifest_v2.json || true)"
VURL="$(printf '%s' "$MANIFEST" | python3 -c 'import json,sys; m=json.load(sys.stdin); v=[x for x in m["versions"] if x["id"]=="26.2"]; print(v[0]["url"] if v else "")' || true)"
CLIENT_URL="$(curl -fsSL "$VURL" | python3 -c 'import json,sys; print(json.load(sys.stdin)["downloads"]["client"]["url"])' || true)"
fetch "$CLIENT_URL" client.jar || exit 1
fetch "https://repo1.maven.org/maven2/net/fabricmc/sponge-mixin/0.15.4+mixin.0.8.7/sponge-mixin-0.15.4+mixin.0.8.7.jar" mixin.jar || exit 1
fetch "https://repo1.maven.org/maven2/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar" jspecify.jar || exit 1
fetch "https://libraries.minecraft.net/it/unimi/dsi/fastutil/8.5.18/fastutil-8.5.18.jar" fastutil.jar || exit 1
fetch "https://libraries.minecraft.net/com/mojang/datafixerupper/10.0.21/datafixerupper-10.0.21.jar" dfu.jar || exit 1
fetch "https://libraries.minecraft.net/org/joml/joml/1.10.8/joml-1.10.8.jar" joml.jar || exit 1
fetch "https://libraries.minecraft.net/com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar" brigadier.jar || exit 1
fetch "https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.19.3/fabric-loader-0.19.3.jar" fabric-loader.jar || exit 1

# fabric-api: pick the newest build for MC 26.2 from Fabric's maven metadata
FAPI_VER="$(curl -fsSL https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml \
  | grep -oE '<version>[^<]*\+26\.2[^<]*</version>' | sed 's/<[^>]*>//g' | tail -1 || true)"
if [ -z "$FAPI_VER" ]; then
  echo "::error title=source-build::no fabric-api version for 26.2 found in maven metadata"
  exit 1
fi
echo "[deps] fabric-api resolved: $FAPI_VER"
fetch "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/${FAPI_VER}/fabric-api-${FAPI_VER}.jar" fabric-api.jar || exit 1

# The aggregate fabric-api jar is thin; the real classes live in per-module
# jars whose exact versions are listed in the aggregate POM. Pull them all.
curl -fsSL --retry 3 "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/${FAPI_VER}/fabric-api-${FAPI_VER}.pom" -o "$DL/fabric-api.pom" || {
  echo "::error title=source-build::could not fetch fabric-api POM"; exit 1; }
mkdir -p "$DL/fapi"
python3 - "$DL/fabric-api.pom" "$DL/fapi-list.txt" <<'PYEOF'
import re, sys
pom = open(sys.argv[1]).read()
out = []
for m in re.finditer(r'<dependency>\s*<groupId>([^<]+)</groupId>\s*<artifactId>([^<]+)</artifactId>\s*<version>([^<]+)</version>', pom):
    g, a, v = m.groups()
    if g != "net.fabricmc.fabric-api":
        continue
    out.append(f"https://maven.fabricmc.net/{g.replace('.', '/')}/{a}/{v}/{a}-{v}.jar\t{a}.jar")
open(sys.argv[2], "w").write("\n".join(out))
print(f"[deps] fabric-api modules in POM: {len(out)}")
PYEOF
while IFS=$'\t' read -r url name; do
  [ -s "$DL/fapi/$name" ] || curl -fsSL --retry 3 --retry-delay 2 -o "$DL/fapi/$name" "$url" \
    || echo "::warning title=source-build::module download failed: $name"
done < "$DL/fapi-list.txt"
FAPI_CP="$(find "$DL/fapi" -name '*.jar' | tr '\n' ':')"

CP="$DL/client.jar:$DL/mixin.jar:$DL/jspecify.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar:$DL/brigadier.jar:$DL/fabric-loader.jar:$DL/fabric-api.jar:$FAPI_CP"

# --- source set: recovered mod + our overlay, minus the broken decompile ---
rm -f /tmp/ds-src.args
find src-recon -name '*.java' ! -name 'WitherStormDevourer.java' > /tmp/ds-src.args
find mcsm-extras/java -name '*.java' >> /tmp/ds-src.args
N_SRC=$(wc -l < /tmp/ds-src.args)
echo "[source] $N_SRC java files in the compile set"

rm -rf /tmp/ds-src-build && mkdir -p /tmp/ds-src-build
JAVAC_LOG=/tmp/ds-javac.log
javac -nowarn --release 25 -proc:none -cp "$CP" -d /tmp/ds-src-build @/tmp/ds-src.args > "$JAVAC_LOG" 2>&1
RC=$?
N_CLS=$(find /tmp/ds-src-build -name '*.class' | wc -l)
{
  echo "source build report"
  echo "java files in:   $N_SRC"
  echo "classes out:     $N_CLS"
  echo "javac exit:      $RC"
  echo "jar reference:   385 mod classes (+ our overlay) in the 1.9.100 base"
  echo "--- first 40 error lines ---"
  grep -E "error:" "$JAVAC_LOG" | head -40 || true
} > out/source-build-report.txt

if [ "$RC" -eq 0 ]; then
  echo "::notice title=source-build::WHOLE MOD COMPILES FROM SOURCE: $N_CLS classes from $N_SRC files"
else
  N_ERR=$(grep -cE "error:" "$JAVAC_LOG" || true)
  echo "::error title=source-build::javac reported $N_ERR errors across $N_SRC files; first lines in annotations and out/source-build-report.txt"
  grep -E "error:" "$JAVAC_LOG" | head -12 | while IFS= read -r line; do
    echo "::error title=javac::${line:0:400}"
  done
  grep -oE '^[a-zA-Z0-9_./-]+\.java' "$JAVAC_LOG" | sort | uniq -c | sort -rn | head -15 | while read -r c f; do
    echo "::error title=errors-in::${c} ${f}"
  done
fi
exit 0   # report-only pipeline: never fail the workflow itself
