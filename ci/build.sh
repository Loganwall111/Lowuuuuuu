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
set -x   # MCSM 1.9.100: full trace so a red run names the exact command

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
    curl -fsSL --retry 3 --retry-delay 3 -o "$out" "$1" || {
      echo "[deps] FAILED to download $1"; return 1; }
  fi
  echo "[deps] $(basename "$out") $(stat -c%s "$out") B"
}

# MCSM 1.9.100 -- the Minecraft client jar is resolved from the LIVE version
# manifest instead of a hardcoded object hash. A stale hash 404s and kills the
# build in seconds with no useful message (exactly what run 33930633043 looked
# like). Falls back to the pinned hash if the manifest cannot be reached.
export MC_VER="${MC_VER:-26.2}"
CLIENT_URL="https://piston-data.mojang.com/v1/objects/2dc72797acbc1b63fc16a11c4ac393605f453754/client.jar"
MANIFEST="$(curl -fsSL https://piston-meta.mojang.com/mc/game/version_manifest_v2.json || true)"
if [ -n "$MANIFEST" ]; then
  VURL="$(printf '%s' "$MANIFEST" | python3 -c 'import json,sys; m=json.load(sys.stdin); v=[x for x in m["versions"] if x["id"]==__import__("os").environ.get("MC_VER","26.2")]; print(v[0]["url"] if v else "")' || true)"
  if [ -n "$VURL" ]; then
    RESOLVED="$(curl -fsSL "$VURL" | python3 -c 'import json,sys; print(json.load(sys.stdin)["downloads"]["client"]["url"])' || true)"
    if [ -n "$RESOLVED" ]; then CLIENT_URL="$RESOLVED"; fi
  fi
fi
echo "[deps] minecraft $MC_VER -> $CLIENT_URL"
fetch "$CLIENT_URL" client.jar
fetch "https://repo1.maven.org/maven2/net/fabricmc/sponge-mixin/0.15.4+mixin.0.8.7/sponge-mixin-0.15.4+mixin.0.8.7.jar" mixin.jar
fetch "https://repo1.maven.org/maven2/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar" jspecify.jar
fetch "https://repo1.maven.org/maven2/it/unimi/dsi/fastutil/8.5.15/fastutil-8.5.15.jar" fastutil.jar
fetch "https://libraries.minecraft.net/com/mojang/datafixerupper/8.0.16/datafixerupper-8.0.16.jar" dfu.jar
fetch "https://libraries.minecraft.net/org/joml/joml/1.10.8/joml-1.10.8.jar" joml.jar

# MCSM 1.9.100 -- close the loop: teach the sandbox the real API.
# This sandbox has no JDK and no route to Mojang/Maven, so every client-side
# class has been written blind against remembered signatures (the HUD move, the
# command wire and the inventory shift are still unwritten for exactly that
# reason). The runner has BOTH. So: javap the public API of everything we might
# target, write it into ci/api/, and push it back to the branch.
#   * actions/checkout persists credentials by default, so `git push` works.
#   * a push made with the GITHUB_TOKEN does NOT start another workflow run,
#     so this cannot recurse into itself.
#   * every step is best-effort: a failed dump must never fail the build.
if [ -n "${GITHUB_ACTIONS:-}" ]; then
  echo "[apidump] javap the real client + mod API"
  mkdir -p ci/api
  CP2="$DL/client.jar:$BASE:$DL/mixin.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar"
  CLIENT_CLASSES="net.minecraft.client.Minecraft net.minecraft.client.gui.Gui \
    net.minecraft.client.gui.GuiGraphics net.minecraft.client.gui.screens.Screen \
    net.minecraft.client.gui.screens.inventory.AbstractContainerScreen \
    net.minecraft.client.gui.screens.inventory.InventoryScreen \
    net.minecraft.client.gui.components.AbstractWidget \
    net.minecraft.client.gui.components.Button net.minecraft.client.gui.components.CycleButton \
    net.minecraft.client.gui.components.AbstractSliderButton \
    net.minecraft.client.gui.components.EditBox net.minecraft.client.gui.components.Tooltip \
    net.minecraft.client.gui.layouts.LinearLayout net.minecraft.client.DeltaTracker \
    net.minecraft.client.renderer.LevelRenderer net.minecraft.client.renderer.MultiBufferSource \
    net.minecraft.client.renderer.RenderType net.minecraft.client.renderer.blockentity.BlockEntityRenderer \
    net.minecraft.client.renderer.entity.EntityRenderer net.minecraft.client.Camera \
    net.minecraft.client.player.LocalPlayer net.minecraft.world.entity.player.Player \
    net.minecraft.world.entity.player.Inventory net.minecraft.world.inventory.AbstractContainerMenu \
    net.minecraft.world.level.block.entity.CommandBlockEntity \
    net.minecraft.network.chat.Component net.minecraft.ChatFormatting"
  MOD_CLASSES="net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen \
    net.dabicco.witherstormmod.client.ShaderPackCompat \
    net.dabicco.witherstormmod.client.FoglessRenderTypes \
    net.dabicco.witherstormmod.client.StormSkyGradient \
    net.dabicco.witherstormmod.entity.WitherStormEntity \
    net.dabicco.witherstormmod.command.DabyWSCommand"
  javap -public -classpath "$CP2" $CLIENT_CLASSES > ci/api/client.txt 2>&1 || true
  javap -public -classpath "$CP2" $MOD_CLASSES   > ci/api/mod.txt    2>&1 || true
  # A class index so we can discover what this version renamed things to.
  unzip -Z1 "$DL/client.jar" 2>/dev/null | grep -E '^net/minecraft/client/.*\.class$' | sort \
    > ci/api/client-index.txt || true
  wc -l ci/api/*.txt || true
  if [ -s ci/api/client.txt ]; then
    git add -f ci/api || true
    if ! git diff --cached --quiet -- ci/api; then
      git -c user.email="ci@mcsm.local" -c user.name="MCSM build" \
          commit -q -m "ci: api dump from the $MC_VER client (javap), so client-side code stops being written blind" || true
      git pull --rebase -q origin "${GITHUB_REF_NAME:-arena/01a06df7-lowuuuuuu}" || true
      git push origin "HEAD:${GITHUB_REF_NAME:-arena/01a06df7-lowuuuuuu}" || \
        echo "[apidump] push failed (token may be read-only); the dump stays on the runner"
    fi
  fi
fi

echo "[glsl] shader gate (glslang via shimcheck)"
chmod +x glslcheck/bin/glslang || true
python3 glslcheck/shimcheck.py mcsm-core-shaders \
  jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh \
  jar-overrides/assets/dabywitherstormmod/shaders/post/storm_sun_glow.fsh

echo "[javac] mcsm-extras"
rm -rf /tmp/mcsm-build
mkdir -p /tmp/mcsm-build
mkdir -p out
CP="$DL/client.jar:$BASE:$DL/mixin.jar:$DL/jspecify.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar"
# MCSM 1.9.100 -- a javac failure used to kill the job outright, so a compile
# error in ONE java file meant the user got no jar at all -- not even the
# shaders that were already finished. Now a failed compile is survivable: the
# jar is still assembled from the previous classes + the current shaders, the
# first 60 lines of javac output are written to out/JAVAC_FAILED.txt, and the
# run is flagged with a GitHub error annotation (red, but with an artifact).
JAVAC_LOG=/tmp/mcsm-javac.log
if javac -nowarn --release 25 -proc:none -cp "$CP" -d /tmp/mcsm-build \
     $(find mcsm-extras/java -name '*.java') > "$JAVAC_LOG" 2>&1; then
  echo "[javac] OK: $(find /tmp/mcsm-build -name '*.class' | wc -l) classes"
  rm -f out/JAVAC_FAILED.txt
else
  echo "::error::javac FAILED -- this jar has the NEW SHADERS but the OLD Java classes. See out/JAVAC_FAILED.txt"
  head -60 "$JAVAC_LOG" > out/JAVAC_FAILED.txt
  cat out/JAVAC_FAILED.txt
  echo "[javac] FAILED (continuing with a shaders-only jar)"
fi

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
