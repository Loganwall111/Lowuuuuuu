#!/usr/bin/env bash
# ============================================================================
# ci/build.sh — the MCSM jar build, runnable locally AND in GitHub Actions.
#
# Provenance: delivery/HANDOFF.md §8 (the original recipe needed a local JDK 25
# + Mojang/Maven network; the sandbox had neither, so this script moves the
# compile step to CI runners, which do).
#
# Steps: fetch deps -> javac the mcsm-extras sources --release 25 -> overlay
# core shaders + jar-overrides + fresh classes onto the newest delivery jar ->
# bump fabric.mod.json version -> zip -> sha256. Output in ./out/.
#
# Usage:  bash ci/build.sh            # version from ./VERSION
#         bash ci/build.sh 1.9.98     # explicit
# ============================================================================
set -euo pipefail

VER="${1:-$(cat VERSION | tr -d '[:space:]')}"
JAR_ID="${VER}-26.2-beta-mcsm"
echo "[build] MCSM ${JAR_ID}"

BASE="$(ls -1v delivery/dabywitherstormmod-*-26.2-beta-mcsm.jar | tail -1)"
echo "[build] base jar: ${BASE}"

DL=/tmp/mcsm-dl
mkdir -p "$DL"
fetch() { # url -> file
  local out="$DL/$(basename "$2")"
  if [ ! -s "$out" ]; then
    curl -fsSL --retry 3 --retry-delay 3 -o "$out" "$1"
  fi
  echo "[deps] $(basename "$out") $(stat -c%s "$out") B"
}
fetch "https://piston-data.mojang.com/v1/objects/2dc72797acbc1b63fc16a11c4ac393605f453754/client.jar" client.jar
fetch "https://repo1.maven.org/maven2/net/fabricmc/sponge-mixin/0.15.4+mixin.0.8.7/sponge-mixin-0.15.4+mixin.0.8.7.jar" mixin.jar
fetch "https://repo1.maven.org/maven2/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar" jspecify.jar
fetch "https://repo1.maven.org/maven2/it/unimi/dsi/fastutil/8.5.15/fastutil-8.5.15.jar" fastutil.jar
fetch "https://libraries.minecraft.net/com/mojang/datafixerupper/8.0.16/datafixerupper-8.0.16.jar" dfu.jar
fetch "https://libraries.minecraft.net/org/joml/joml/1.10.8/joml-1.10.8.jar" joml.jar

echo "[glsl] shader gate (glslang via shimcheck)"
chmod +x glslcheck/bin/glslang || true
python3 glslcheck/shimcheck.py mcsm-core-shaders \
  jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh \
  jar-overrides/assets/dabywitherstormmod/shaders/post/storm_sun_glow.fsh

echo "[javac] mcsm-extras"
rm -rf /tmp/mcsm-build
mkdir -p /tmp/mcsm-build
CP="$DL/client.jar:$BASE:$DL/mixin.jar:$DL/jspecify.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar"
javac -nowarn --release 25 -proc:none -cp "$CP" -d /tmp/mcsm-build \
  $(find mcsm-extras/java -name '*.java')
echo "[javac] OK: $(find /tmp/mcsm-build -name '*.class' | wc -l) classes"

echo "[assemble] overlay onto base"
FX=/tmp/mcsm-fx
rm -rf "$FX" && mkdir -p "$FX/cls"
( cd "$FX/cls" && unzip -o -q "$OLDPWD/$BASE" )
cp -r mcsm-core-shaders/* "$FX/cls/assets/minecraft/shaders/"
cp -r jar-overrides/* "$FX/cls/"
cp -r /tmp/mcsm-build/* "$FX/cls/"
sed -i "s/\"version\": \"[0-9.]*-26.2-beta-mcsm\"/\"version\": \"${JAR_ID}\"/" "$FX/cls/fabric.mod.json"

mkdir -p out
OUT="out/dabywitherstormmod-${JAR_ID}.jar"
rm -f "$OUT"
( cd "$FX/cls" && zip -q -r -X "$OLDPWD/$OUT" . -x '.*' )
( unzip -t "$OUT" > /dev/null )
sha256sum "$OUT" | tee "$OUT.sha256"
echo "[done] $OUT ($(stat -c%s "$OUT") B)"
