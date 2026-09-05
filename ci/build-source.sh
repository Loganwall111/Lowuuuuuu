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
#   Six model classes whose createBodyLayer() builders OOM'd Vineflower
#   (multi-thousand-call chains, 300+ locals): WitherStormDevourer,
#   WitherStormP4, HugeAssBackModel, HunchbackGrowth, SeveredWitherStorm,
#   WitherStormTentacles5. Until a high-heap single-class recovery pass
#   lands, those SIX classes keep coming from the base jar at assembly time
#   (compile-time fallback below).
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
curl -fsSL "$VURL" -o "$DL/version.json" || { echo "::error title=source-build::could not fetch MC version json"; exit 1; }
CLIENT_URL="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["downloads"]["client"]["url"])' "$DL/version.json" || true)"
fetch "$CLIENT_URL" client.jar || exit 1
# javac hard-fails on JSpecify type annotations in the raw vanilla class files
# (FriendlyByteBuf.readNullable); strip those attributes into a compile-only copy.
python3 ci/strip_typeann.py "$DL/client.jar" "$DL/client-stripped.jar" || {
  echo "::error title=source-build::client jar type-annotation stripping failed"; exit 1; }
fetch "https://repo1.maven.org/maven2/net/fabricmc/sponge-mixin/0.15.4+mixin.0.8.7/sponge-mixin-0.15.4+mixin.0.8.7.jar" mixin.jar || exit 1

# Every vanilla runtime library (netty, guava, log4j, authlib, ...) straight
# from the version manifest -- kills the whole "missing transitive lib" class
# of compile errors in one move.
mkdir -p "$DL/libs"
python3 - "$DL/version.json" "$DL/lib-list.txt" <<'LIBPY'
import json, os, sys
v = json.load(open(sys.argv[1]))
out = []
for lib in v.get("libraries", []):
    art = (lib.get("downloads") or {}).get("artifact") or {}
    url = art.get("url")
    if not url:
        continue
    name = art.get("path") or os.path.basename(url)
    if name.startswith("net/minecraft/client"):
        continue
    out.append(url + "\t" + os.path.basename(name))
open(sys.argv[2], "w").write("\n".join(out))
print(f"[deps] vanilla libraries in manifest: {len(out)}")
LIBPY
while IFS=$'\t' read -r url name; do
  [ -s "$DL/libs/$name" ] || curl -fsSL --retry 2 --retry-delay 2 -o "$DL/libs/$name" "$url" \
    || echo "::warning title=source-build::library download failed: $name"
done < "$DL/lib-list.txt"
LIBS_CP="$(find "$DL/libs" -name '*.jar' | tr '\n' ':')"
fetch "https://libraries.minecraft.net/it/unimi/dsi/fastutil/8.5.18/fastutil-8.5.18.jar" fastutil.jar || exit 1
fetch "https://libraries.minecraft.net/com/mojang/datafixerupper/10.0.21/datafixerupper-10.0.21.jar" dfu.jar || exit 1
fetch "https://libraries.minecraft.net/org/joml/joml/1.10.8/joml-1.10.8.jar" joml.jar || exit 1
fetch "https://libraries.minecraft.net/com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar" brigadier.jar || exit 1
fetch "https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.19.3/fabric-loader-0.19.3.jar" fabric-loader.jar || exit 1
fetch "https://libraries.minecraft.net/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar" gson.jar || exit 1
fetch "https://libraries.minecraft.net/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar" slf4j.jar || exit 1

# modmenu: newest release from the TerraformersMC maven (API surface is stable)
MODMENU_META="$(curl -fsSL https://maven.terraformersmc.com/releases/com/terraformersmc/modmenu/maven-metadata.xml)"
MODMENU_CANDIDATES="$(printf '%s' "$MODMENU_META" | grep -oE '<release>[^<]*</release>' | sed 's/<[^>]*>//g')
$(printf '%s' "$MODMENU_META" | grep -oE '<version>[^<]*</version>' | sed 's/<[^>]*>//g' | sort -Vr | head -4)"
MODMENU_OK=""
for MODMENU_VER in $MODMENU_CANDIDATES; do
  echo "[deps] trying modmenu $MODMENU_VER"
  if curl -fsSL --retry 2 -o "$DL/modmenu.jar" \
      "https://maven.terraformersmc.com/releases/com/terraformersmc/modmenu/${MODMENU_VER}/modmenu-${MODMENU_VER}.jar" \
     && unzip -l "$DL/modmenu.jar" | grep -q "com/terraformersmc/modmenu/api/ModMenuApi.class"; then
    MODMENU_OK="$MODMENU_VER"
    break
  fi
  echo "::warning title=source-build::modmenu $MODMENU_VER unusable (download or missing api package)"
done
if [ -z "$MODMENU_OK" ]; then
  echo "::error title=source-build::no usable modmenu jar found; ModMenuIntegration will not compile"
fi
echo "::notice title=source-build::modmenu in use: ${MODMENU_OK:-NONE}"

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

CP="$DL/client-stripped.jar:$LIBS_CP$DL/mixin.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar:$DL/brigadier.jar:$DL/fabric-loader.jar:$DL/fabric-api.jar:$DL/gson.jar:$DL/slf4j.jar:$DL/modmenu.jar:$FAPI_CP"

# Compile-time fallback, LAST on the classpath: the single class Vineflower
# could not recover (WitherStormDevourer.createBodyLayer -- OOM on a
# multi-thousand-call model builder). Every other symbol must resolve from
# the explicit source set, which javac prefers over classpath jars. When a
# CFR pass or a hand-rebuild recovers that method, drop this fallback.
fetch "https://github.com/Loganwall111/Lowuuuuuu/releases/download/mcsm-1.9.100/dabywitherstormmod-1.9.100-26.2-beta-mcsm.jar" base-fallback.jar || exit 1
CP="$CP:$DL/base-fallback.jar"

# --- source set: recovered mod + our overlay, minus the broken decompile ---
rm -f /tmp/ds-src.args
find src-recon -name '*.java' ! -name 'WitherStormDevourer.java' ! -name 'WitherStormP4.java' ! -name 'HugeAssBackModel.java' ! -name 'HunchbackGrowth.java' ! -name 'SeveredWitherStorm.java' ! -name 'WitherStormTentacles5.java' > /tmp/ds-src.args
find mcsm-extras/java -name '*.java' >> /tmp/ds-src.args
find ci/stubs -name '*.java' >> /tmp/ds-src.args
N_SRC=$(wc -l < /tmp/ds-src.args)
echo "[source] $N_SRC java files in the compile set"

rm -rf /tmp/ds-src-build && mkdir -p /tmp/ds-src-build
JAVAC_LOG=/tmp/ds-javac.log
# -J-Xss: the model builders are thousands-deep method-call chains that
# overflow javac's default attribution recursion stack.
# Self-widening loop: vanilla members the published mod bytecode calls
# directly (private/protected per the raw jar) are widened in a COMPILE-ONLY
# copy of the client jar, round by round, until javac converges.
WIDEN="$DL/widen.txt"; : > "$WIDEN"
CP_TAIL="${CP#*:}"
ROUND=0
RC=1
while [ "$ROUND" -lt 8 ]; do
  ROUND=$((ROUND + 1))
  python3 ci/widen_members.py "$DL/client-stripped.jar" "$DL/client-w.jar" "$WIDEN" > "$DL/widen-out.txt" 2>&1 || exit 1
  while IFS= read -r line; do
    echo "::notice title=widen-r$ROUND::${line:0:280}"
  done < <(head -10 "$DL/widen-out.txt")
  while IFS= read -r line; do
    echo "::notice title=widen-entry::${line:0:200}"
  done < "$WIDEN"
  rm -rf /tmp/ds-src-build && mkdir -p /tmp/ds-src-build
  javac -J-Xss512m -J-Xmx8g -nowarn --release 25 -proc:none -cp "$DL/client-w.jar:$CP_TAIL" -d /tmp/ds-src-build @/tmp/ds-src.args > "$JAVAC_LOG" 2>&1
  RC=$?
  [ "$RC" -eq 0 ] && break
  python3 - "$JAVAC_LOG" "$DL/acc-new.txt" <<'ACCPY'
import re, sys
log = open(sys.argv[1], encoding="utf-8", errors="replace").read()
pairs = set()
for m in re.finditer(r'([A-Za-z_$][\w$]*)\s+has\s+(?:private|protected)\s+access\s+in\s+([\w.$]+)', log):
    pairs.add((m.group(2), m.group(1)))
for m in re.finditer(r'(?:<[^>]*>)?([A-Za-z_$][\w$]*)\([^)]*\)\s+has\s+(?:private|protected)\s+access\s+in\s+([\w.$]+)', log):
    pairs.add((m.group(2), m.group(1)))
for m in re.finditer(r'([A-Za-z_$][\w$]*)\s+is\s+not\s+public\s+in\s+([\w.$]+)', log):
    pairs.add((m.group(2), m.group(1)))
open(sys.argv[2], "w").write("\n".join(f"{o}#{n}" for o, n in sorted(pairs)))
ACCPY
  BEFORE=$(wc -l < "$WIDEN")
  cat "$DL/acc-new.txt" >> "$WIDEN" 2>/dev/null || true
  sort -u "$WIDEN" -o "$WIDEN"
  AFTER=$(sort -u "$WIDEN" | grep -c . || true)
  echo "[widen] round $ROUND: javac rc=$RC, widen list $BEFORE -> $AFTER"
  [ "$AFTER" -le "$BEFORE" ] && break
done
echo "::notice title=source-build::compile rounds: $ROUND; widened vanilla members: $(grep -c . "$WIDEN" || true)"
N_CLS=$(find /tmp/ds-src-build -name '*.class' | wc -l)
{
  echo "source build report"
  echo "java files in:   $N_SRC"
  echo "classes out:     $N_CLS"
  echo "javac exit:      $RC"
  echo "jar reference:   385 mod classes (+ our overlay) in the 1.9.100 base"
  echo "--- widen list ---"
  cat "$WIDEN" 2>/dev/null || true
  echo "--- first 25 errors with detail ---"
  grep -A4 -E "error:" "$JAVAC_LOG" | head -120 || true
  echo "--- javac log head ---"
  head -5 "$JAVAC_LOG" || true
  echo "--- javac log tail ---"
  tail -20 "$JAVAC_LOG" || true
} > out/source-build-report.txt

if [ "$RC" -eq 0 ]; then
  echo "::notice title=source-build::WHOLE MOD COMPILES FROM SOURCE: $N_CLS classes from $N_SRC files"
else
  N_ERR=$(grep -cE "error:" "$JAVAC_LOG" || true)
  echo "::error title=source-build::javac reported $N_ERR errors across $N_SRC files; first lines in annotations and out/source-build-report.txt"
  grep -A4 -E "error:" "$JAVAC_LOG" | grep -E "error:|symbol:|location:|required:|found:" | head -12 | while IFS= read -r line; do
    echo "::error title=javac::${line:0:400}"
  done
  grep -E "error:" "$JAVAC_LOG" | sed -E 's/.*error: //; s/[0-9]+/N/g' | sort | uniq -c | sort -rn | head -10 | while IFS= read -r line; do
    echo "::error title=error-kinds::${line:0:300}"
  done
  grep -oE '^[a-zA-Z0-9_./-]+\.java' "$JAVAC_LOG" | sort | uniq -c | sort -rn | head -10 | while read -r c f; do
    echo "::error title=errors-in::${c} ${f}"
  done
  if [ "$N_ERR" -eq 0 ]; then
    echo "::error title=javac-nonzero-exit::javac exited $RC with no 'error:' lines; log tail follows"
    tail -12 "$JAVAC_LOG" | while IFS= read -r line; do
      echo "::error title=javac-tail::${line:0:400}"
    done
  fi
fi
exit 0   # report-only pipeline: never fail the workflow itself
