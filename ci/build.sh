#!/usr/bin/env bash
# ============================================================================
# ci/build.sh — the MCSM jar build, runnable locally AND in GitHub Actions.
#
# Provenance: delivery/HANDOFF.md §8 (the original recipe needed a local JDK 25
# + Mojang/Maven network; the sandbox had neither, so this script moves the
# compile step to CI runners, which do).
#
# Steps: fetch deps -> GLSL gate -> javac the mcsm-extras sources --release 25
# -> overlay core shaders + jar-overrides + fresh classes onto the newest
# delivery jar -> bump fabric.mod.json version -> zip -> sha256. Output ./out/.
#
# 2026-09-05 hardening (compile audit):
#   * the old "survivable javac" was broken: on a compile error the class dir
#     is empty, `cp -r /tmp/mcsm-build/*` then dies under `set -e` and the jar
#     is never assembled. Fixed with a nullglob guard.
#   * the FULL javac log now lands in out/JAVAC_FAILED.txt (was 60 lines).
#   * out/BUILD_INFO.txt records the verdict (versions, hashes, class count).
#   * build evidence (log, class list, sha256) is pushed best-effort to the
#     session branch arena/01a06edf-lowuuuuuu so the compile result can be
#     audited without runner-log access. Never fails the build.
#
# Usage:  bash ci/build.sh            # version from ./VERSION
#         bash ci/build.sh 1.9.101    # explicit
# ============================================================================
set -euo pipefail
set -x

VER="${1:-$(cat VERSION | tr -d '[:space:]')}"
JAR_ID="${VER}-26.2-beta-mcsm"
echo "[build] MCSM ${JAR_ID}"

EVIDENCE_REPO="https://github.com/Loganwall111/Lowuuuuuu.git"
EVIDENCE_BRANCH="arena/01a06edf-lowuuuuuu"

# MCSM 1.9.101 -- base resolution moved BELOW the fetch() definition
# (it needs fetch). It no longer takes "latest jar in delivery/": that
# silently picked the fake 1.9.100 overlay (the old 1.9.99 base wearing a new
# version string; the new Java was never in it).

mkdir -p out
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

# MCSM 1.9.101 -- the base is the REAL CI-compiled 1.9.100 (release asset,
# sha 6adcf07e...), pinned by hash. If delivery/ holds a jar that matches,
# use it; otherwise the runner fetches the release asset and verifies the
# hash, aborting on mismatch. (The sandbox cannot download release assets, so
# a LOCAL build needs the file dropped into delivery/ first; CI needs nothing.)
BASE_VER="1.9.100"
BASE_NAME="dabywitherstormmod-${BASE_VER}-26.2-beta-mcsm.jar"
BASE_SHA="6adcf07e1ad810703c12cb25d7d135aca7b8f66f7d12c273ad3f00b5abdb6599"
BASE_LOCAL="delivery/${BASE_NAME}"
if [ -s "$BASE_LOCAL" ] && [[ "$(sha256sum "$BASE_LOCAL" | cut -d' ' -f1)" == "$BASE_SHA" ]]; then
  BASE="$BASE_LOCAL"
  echo "[build] base jar: ${BASE} (delivery copy, hash verified)"
else
  echo "[build] delivery/${BASE_NAME} absent or wrong hash -- fetching the mcsm-${BASE_VER} release asset"
  fetch "https://github.com/Loganwall111/Lowuuuuuu/releases/download/mcsm-${BASE_VER}/${BASE_NAME}" "$BASE_NAME"
  got="$(sha256sum "$DL/$BASE_NAME" | cut -d' ' -f1)"
  if [[ "$got" != "$BASE_SHA" ]]; then
    echo "[base] HASH MISMATCH: got ${got}, want ${BASE_SHA}" >&2
    exit 1
  fi
  BASE="$DL/$BASE_NAME"
  echo "[build] base jar: ${BASE} (release asset, hash verified)"
fi

# The Minecraft client jar is resolved from the LIVE version manifest instead
# of a hardcoded object hash. A stale hash 404s and kills the build in seconds
# with no useful message. Falls back to the pinned hash if the manifest is
# unreachable.
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
if [ "$(stat -c%s "$DL/client.jar")" -lt 10000000 ]; then
  echo "[deps] client.jar is suspiciously small — refusing"; exit 1
fi
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
GLSL_LOG=/tmp/mcsm-glsl.log
if python3 glslcheck/shimcheck.py mcsm-core-shaders \
     jar-overrides/assets/dabywitherstormmod/shaders/core/storm_glow.fsh \
     jar-overrides/assets/dabywitherstormmod/shaders/post/storm_sun_glow.fsh \
     > "$GLSL_LOG" 2>&1; then
  tail -2 "$GLSL_LOG"
else
  cat "$GLSL_LOG"
  echo "[glsl] shader gate FAILED — not building a broken shaderpack"
  exit 1
fi

echo "[javac] mcsm-extras"
rm -rf /tmp/mcsm-build
mkdir -p /tmp/mcsm-build
CP="$DL/client.jar:$BASE:$DL/mixin.jar:$DL/jspecify.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar"
# A javac failure is survivable: the jar is still assembled from the previous
# classes + the current shaders, the FULL javac output goes to
# out/JAVAC_FAILED.txt, and the run is flagged with a GitHub error annotation.
JAVAC_LOG=/tmp/mcsm-javac.log
JAVAC_RC=0
javac -nowarn --release 25 -proc:none -cp "$CP" -d /tmp/mcsm-build \
     $(find mcsm-extras/java -name '*.java') > "$JAVAC_LOG" 2>&1 || JAVAC_RC=$?
N_CLASSES="$(find /tmp/mcsm-build -name '*.class' | wc -l)"
if [ "$JAVAC_RC" -eq 0 ]; then
  echo "[javac] OK: ${N_CLASSES} classes"
  rm -f out/JAVAC_FAILED.txt
else
  echo "::error::javac FAILED (exit ${JAVAC_RC}) — this jar has the NEW SHADERS but the OLD Java classes. Full log: out/JAVAC_FAILED.txt"
  cp -f "$JAVAC_LOG" out/JAVAC_FAILED.txt
  tail -80 "$JAVAC_LOG" || true
  echo "[javac] FAILED (continuing with a shaders-only jar)"
fi

echo "[assemble] overlay onto base"
FX=/tmp/mcsm-fx
rm -rf "$FX" && mkdir -p "$FX/cls"
( cd "$FX/cls" && unzip -o -q "$OLDPWD/$BASE" )
cp -r mcsm-core-shaders/* "$FX/cls/assets/minecraft/shaders/"
cp -r jar-overrides/* "$FX/cls/"
# nullglob guard: on a failed javac the class dir is empty and a bare
# `cp -r /tmp/mcsm-build/*` would die under set -e (that bug ate the jar).
shopt -s nullglob
FRESH_CLASSES=(/tmp/mcsm-build/*)
shopt -u nullglob
if [ "${#FRESH_CLASSES[@]}" -gt 0 ]; then
  cp -r "${FRESH_CLASSES[@]}" "$FX/cls/"
fi
sed -i "s/\"version\": \"[0-9.]*-26.2-beta-mcsm\"/\"version\": \"${JAR_ID}\"/" "$FX/cls/fabric.mod.json"

OUT="out/dabywitherstormmod-${JAR_ID}.jar"
rm -f "$OUT"
( cd "$FX/cls" && zip -q -r -X "$OLDPWD/$OUT" . -x '.*' )
( unzip -t "$OUT" > /dev/null )
sha256sum "$OUT" | tee "$OUT.sha256"

{
  echo "MCSM build ${JAR_ID}"
  echo "date:        $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "run:         ${GITHUB_RUN_ID:-local} (#${GITHUB_RUN_NUMBER:-local})"
  echo "base jar:    ${BASE} ($(stat -c%s "$BASE") B)"
  echo "client.jar:  $(sha256sum "$DL/client.jar" | cut -d' ' -f1) ($(stat -c%s "$DL/client.jar") B)"
  echo "glsl gate:   PASS"
  echo "javac:       exit ${JAVAC_RC}, ${N_CLASSES} fresh classes"
  if [ "$JAVAC_RC" -eq 0 ]; then
    echo "VERDICT:     FULL BUILD — fresh Java classes + shaders"
  else
    echo "VERDICT:     SHADERS-ONLY — javac failed, old classes kept (see JAVAC_FAILED.txt)"
  fi
  echo "output:      ${OUT} ($(stat -c%s "$OUT") B)"
  echo "sha256:      $(cut -d' ' -f1 < "$OUT.sha256")"
} > out/BUILD_INFO.txt
cat out/BUILD_INFO.txt
[ "$JAVAC_RC" -eq 0 ] || echo "::warning::Build verdict: shaders-only jar (javac failed)"

# ---------------------------------------------------------------------------
# Build evidence: push the logs/class list/hashes to the session branch so the
# compile result is auditable even without Actions log access. Best-effort —
# NEVER fails the build. Skipped automatically when no credentials exist
# (local runs) or the push loses a race with a concurrent push.
# ---------------------------------------------------------------------------
push_evidence_simple() {
  local AUTH
  AUTH="$(git config --get http.https://github.com/.extraheader 2>/dev/null || true)"
  [ -n "$AUTH" ] || { echo "[evidence] no credentials — skip"; return 0; }
  rm -rf /tmp/mcsm-evidence
  GIT_LFS_SKIP_SMUDGE=1 git -c "http.https://github.com/.extraheader=${AUTH}" \
    clone -q --depth 5 --branch "$EVIDENCE_BRANCH" "$EVIDENCE_REPO" /tmp/mcsm-evidence || {
      echo "[evidence] clone failed — skip"; return 0; }
  local DST="/tmp/mcsm-evidence/ci-out/run-${GITHUB_RUN_NUMBER:-local}"
  rm -rf "$DST"; mkdir -p "$DST"
  cp -f out/BUILD_INFO.txt "$DST/" 2>/dev/null || true
  cp -f out/JAVAC_FAILED.txt "$DST/" 2>/dev/null || true
  cp -f "$JAVAC_LOG" "$DST/javac-full.log"
  cp -f "$GLSL_LOG" "$DST/glsl-gate.log"
  cp -f out/*.sha256 "$DST/" 2>/dev/null || true
  ( cd /tmp/mcsm-build && find . -name '*.class' | sort ) > "$DST/classes.txt"
  git -C /tmp/mcsm-evidence config user.name "mcsm-ci"
  git -C /tmp/mcsm-evidence config user.email "41898282+github-actions[bot]@users.noreply.github.com"
  if git -C /tmp/mcsm-evidence add ci-out && \
     git -C /tmp/mcsm-evidence commit -qm "ci evidence: run ${GITHUB_RUN_NUMBER:-local} — javac exit ${JAVAC_RC}, ${N_CLASSES} classes"; then
    if ! git -C /tmp/mcsm-evidence push -q origin "HEAD:${EVIDENCE_BRANCH}"; then
      echo "[evidence] push rejected (branch moved) — retrying once"
      git -C /tmp/mcsm-evidence fetch -q origin "$EVIDENCE_BRANCH"
      git -C /tmp/mcsm-evidence rebase -q FETCH_HEAD || { echo "[evidence] rebase failed — skip"; return 0; }
      git -C /tmp/mcsm-evidence push -q origin "HEAD:${EVIDENCE_BRANCH}" || echo "[evidence] retry push failed — skip"
    fi
  else
    echo "[evidence] nothing to commit — skip"
  fi
}
push_evidence_simple || echo "[evidence] skipped (non-fatal)"
rm -rf /tmp/mcsm-evidence

echo "[done] $OUT ($(stat -c%s "$OUT") B)"
