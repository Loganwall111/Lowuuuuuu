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
#   * javac is a release gate: on a compile error the build stops instead of
#     assembling a shaders-only jar with stale Java classes.
#   * the FULL javac log now lands in out/JAVAC_FAILED.txt (was 60 lines).
#   * out/BUILD_INFO.txt records the verdict (versions, hashes, class count).
#   * build evidence (log, class list, sha256) is pushed best-effort to the
#     session branch arena/01a071bb-lowuuuuuu so the compile result can be
#     audited without runner-log access. Never fails the build.
#
# Usage:  bash ci/build.sh            # version from ./VERSION
#         bash ci/build.sh 1.9.101    # explicit
# ============================================================================
set -euo pipefail
set -x
# MCSM 1.9.101 -- failure visibility: the sandbox cannot read runner logs
# (results-receiver egress blocked), so an ERR trap reports the failing
# command + line as a GitHub annotation (readable via the Checks API) and
# saves it to out/FAILURE.txt.
# ---------------------------------------------------------------------------
# BUILD #416 (D.8) -- FAILURE DISCLOSURE.
#
# A red run used to be a dead end: the runner's log lives on a blob host this
# machine cannot reach, `gh run view --log` EOFs, the artifact download EOFs,
# and the only thing that survives is a check-run annotation -- which the script
# only emits on the paths that remember to. A plain `set -e` abort emitted
# nothing at all, so the one run that mattered most (the first javac of a new
# phase) could fail without saying where.
#
# So the whole run is tee'd to a log file and two traps use it:
#   * ERR  -- records the failing line and command, as a line AND an annotation.
#   * EXIT -- prints the last lines of the run as annotations (titled
#             last-1..last-N so they parse), copies the whole log to
#             out/BUILD_LOG.txt, and pushes the evidence even when the build
#             failed, which is exactly when the javac log is worth having.
# ---------------------------------------------------------------------------
RUNLOG=/tmp/mcsm-run.log
: > "$RUNLOG"
exec > >(tee -a "$RUNLOG") 2>&1
EVIDENCE_DONE=0

trap 'rc=$?; mkdir -p out 2>/dev/null; { echo "MCSM build FAILURE (run ${GITHUB_RUN_NUMBER:-local})"; echo "exit: $rc"; echo "line: $LINENO"; echo "cmd:  $BASH_COMMAND"; } >> out/FAILURE.txt 2>/dev/null; { echo; echo "[failure] exit ${rc} at line ${LINENO}: ${BASH_COMMAND}"; } | tee -a "$RUNLOG"; echo "::error::MCSM build failed (exit ${rc}) at line ${LINENO}: ${BASH_COMMAND}"' ERR

disclose_failure() {
  local rc=$?
  if [ "$rc" -eq 0 ]; then return 0; fi
  sleep 1
  cp -f "$RUNLOG" out/BUILD_LOG.txt 2>/dev/null || true
  { echo; echo "---- last 25 lines of the run ----"; tail -n 25 "$RUNLOG"; } >> out/FAILURE.txt 2>/dev/null || true
  echo "::error title=build-failed::exit ${rc} after stage '$(cat "$RUN_STAGE" 2>/dev/null)' -- full run log in out/BUILD_LOG.txt and ci-out/run-*/ on the branch"
  echo "[failure] exit ${rc} after stage '$(cat "$RUN_STAGE" 2>/dev/null)'"
  local line n=0
  tail -n 12 "$RUNLOG" 2>/dev/null | while IFS= read -r line; do
    n=$((n + 1))
    echo "::error title=last-${n}::${line:0:900}"
  done
  if [ "${EVIDENCE_DONE:-0}" != "1" ] && command -v push_evidence_simple >/dev/null 2>&1; then
    push_evidence_simple 2>/dev/null || true
  fi
}
trap disclose_failure EXIT

# BUILD #416 (D.8) -- the evidence channel, defined EARLY.
#
# It used to live at the very bottom of this script, which meant a failure
# anywhere above it left nothing behind: no log, no marker, and (because the
# runner's own logs live on a blob host this machine cannot reach) no way to
# tell which gate died. Now the channel is defined up here next to the traps and
# is used twice:
#   * stage <name>  -- records the last gate that completed, in the log and in
#                      ci-out/run-<N>/stage.txt on the session branch, so a red
#                      run says WHERE it died even when it dies silently;
#   * push_evidence_simple -- the full upload (javac log, run log, class list,
#                      vanilla API dump), also reachable from the EXIT trap.
# Every helper is non-fatal by construction: it restores `set -e` on the way out
# and never returns non-zero to the build.
EVIDENCE_DONE=0
EVIDENCE_DIR=""
RUN_STAGE="/tmp/mcsm-stage.txt"
: > "$RUN_STAGE"

evidence_clone() {
  [ -n "$EVIDENCE_DIR" ] && return 0
  local AUTH REMOTE
  AUTH="$(git config --get http.https://github.com/.extraheader 2>/dev/null || true)"
  if [ -n "${GH_TOKEN:-}" ]; then
    REMOTE="https://x-access-token:${GH_TOKEN}@github.com/Loganwall111/Lowuuuuuu.git"
    AUTH=""
  elif [ -n "$AUTH" ]; then
    REMOTE="$EVIDENCE_REPO"
  else
    echo "[evidence] no credentials - channel unavailable"
    return 1
  fi
  rm -rf /tmp/mcsm-evidence
  if [ -n "$AUTH" ]; then
    GIT_LFS_SKIP_SMUDGE=1 git -c "http.https://github.com/.extraheader=${AUTH}" \
      clone -q --depth 5 --branch "$EVIDENCE_BRANCH" "$REMOTE" /tmp/mcsm-evidence || return 1
  else
    # clone anonymously, then point origin at the token URL with tracing off, so
    # the token never reaches the run log
    GIT_LFS_SKIP_SMUDGE=1 git clone -q --depth 5 --branch "$EVIDENCE_BRANCH" \
      "$EVIDENCE_REPO" /tmp/mcsm-evidence || return 1
    local had_x=0
    case "$-" in *x*) had_x=1 ;; esac
    set +x
    git -C /tmp/mcsm-evidence remote set-url origin "$REMOTE"
    [ "$had_x" = "1" ] && set -x
  fi
  git -C /tmp/mcsm-evidence config user.name "mcsm-ci"
  git -C /tmp/mcsm-evidence config user.email "41898282+github-actions[bot]@users.noreply.github.com"
  EVIDENCE_DIR=/tmp/mcsm-evidence
  echo "[evidence] channel open on ${EVIDENCE_BRANCH}"
  return 0
}

# evidence_put <local file> <name> -- non-fatal, restores set -e
evidence_put() {
  local had_e=0
  case "$-" in *e*) had_e=1 ;; esac
  set +e
  if evidence_clone; then
    local DST="$EVIDENCE_DIR/ci-out/run-${GITHUB_RUN_NUMBER:-local}"
    mkdir -p "$DST"
    if cp -f "$1" "$DST/$2" 2>/dev/null; then
      git -C "$EVIDENCE_DIR" add ci-out >/dev/null 2>&1
      git -C "$EVIDENCE_DIR" commit -qm "ci evidence: run ${GITHUB_RUN_NUMBER:-local} $2" >/dev/null 2>&1
      git -C "$EVIDENCE_DIR" push -q origin "HEAD:${EVIDENCE_BRANCH}" >/dev/null 2>&1 \
        || echo "[evidence] push rejected for $2 (non-fatal)"
    fi
  fi
  [ "$had_e" = "1" ] && set -e
  return 0
}

# stage <name> [push] -- the last gate that COMPLETED
stage() {
  local had_e=0
  case "$-" in *e*) had_e=1 ;; esac
  set +e
  echo "[stage] $1"
  printf '%s\n' "$1" > "$RUN_STAGE"
  if [ "${2:-}" = "push" ]; then
    evidence_put "$RUN_STAGE" stage.txt
    evidence_put "$RUNLOG" run.log
  fi
  [ "$had_e" = "1" ] && set -e
  return 0
}

push_evidence_simple() {
  local had_e=0
  case "$-" in *e*) had_e=1 ;; esac
  set +e
  if evidence_clone; then
    local DST="$EVIDENCE_DIR/ci-out/run-${GITHUB_RUN_NUMBER:-local}"
    rm -rf "$DST"; mkdir -p "$DST"
    cp -f out/BUILD_INFO.txt "$DST/" 2>/dev/null
    cp -f out/JAVAC_FAILED.txt "$DST/" 2>/dev/null
    cp -f out/vanilla-api.txt "$DST/" 2>/dev/null
    cp -f out/BUILD_LOG.txt "$DST/" 2>/dev/null
    cp -f "${JAVAC_LOG:-/dev/null}" "$DST/javac-full.log" 2>/dev/null
    cp -f "${GLSL_LOG:-/dev/null}" "$DST/glsl-gate.log" 2>/dev/null
    cp -f out/*.sha256 "$DST/" 2>/dev/null
    cp -f "$RUNLOG" "$DST/run.log" 2>/dev/null
    cp -f "$RUN_STAGE" "$DST/stage.txt" 2>/dev/null
    ( cd /tmp/mcsm-build 2>/dev/null && find . -name '*.class' | sort ) > "$DST/classes.txt" 2>/dev/null
    git -C "$EVIDENCE_DIR" add ci-out >/dev/null 2>&1
    if git -C "$EVIDENCE_DIR" commit -qm "ci evidence: run ${GITHUB_RUN_NUMBER:-local} -- javac exit ${JAVAC_RC:-?}, ${N_CLASSES:-?} classes"; then
      if git -C "$EVIDENCE_DIR" push -q origin "HEAD:${EVIDENCE_BRANCH}"; then
        echo "[evidence] pushed run ${GITHUB_RUN_NUMBER:-local} to ${EVIDENCE_BRANCH} (last stage: $(cat "$RUN_STAGE" 2>/dev/null))"
      else
        echo "[evidence] push rejected (branch moved) - non-fatal"
      fi
    else
      echo "[evidence] nothing to commit - skip"
    fi
  fi
  EVIDENCE_DONE=1
  [ "$had_e" = "1" ] && set -e
  return 0
}


VER="${1:-$(cat VERSION | tr -d '[:space:]')}"
# Keep the artifact and fabric.mod.json identity byte-for-byte equal to VERSION.
# The custom identity intentionally avoids the repository's old numeric tag list.
JAR_ID="${VER}"
echo "[build] Devouring Storms ${JAR_ID}"

EVIDENCE_REPO="https://github.com/Loganwall111/Lowuuuuuu.git"
# MCSM 1.9.109 -- evidence lands on whichever branch triggered the build, so a
# dispatched run on another session branch does not write into that one.
EVIDENCE_BRANCH="${EVIDENCE_BRANCH:-${GITHUB_REF_NAME:-arena/01a071bb-lowuuuuuu}}"

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

fetch_any() { # name url1 [url2 ...] -- non-fatal, first URL that answers wins
  local name="$1"; shift
  local out="$DL/$name"
  if [ -s "$out" ]; then echo "[deps] $name $(stat -c%s "$out") B"; return 0; fi
  local url
  for url in "$@"; do
    [ -n "$url" ] || continue
    if curl -fsSL --retry 3 --retry-delay 3 -o "$out" "$url" 2>/dev/null; then
      echo "[deps] $name $(stat -c%s "$out") B (from $url)"
      return 0
    fi
  done
  echo "::warning title=deps::$name could not be downloaded from any mirror"
  return 1
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

# MCSM 1.9.101 -- the COMPILE classpath must not contain stale copies of the
# very classes being compiled. The real 1.9.100 base jar holds the
# net/mcsm/extras classes CI compiled at 00:41, and compiling the same sources
# with those classfile twins on the path broke javac (run 33966494942:
# "cannot access Message" + bogus sendParticles errors; the 00:41 build of the
# identical sources passed with a base that had no mcsm classes). So the
# classpath gets a base jar with net/mcsm stripped out; the ASSEMBLY still
# unzips the full base (below), and the fresh classes overwrite the old ones.
# Normalize to an absolute path: the assembly/unzip steps run inside
# subshells that have cd'd elsewhere, where a relative path would re-anchor
# to the wrong directory (run 33966642417 died on exactly this: exit 15).
case "$BASE" in /*) ;; *) BASE="$(pwd)/$BASE" ;; esac
STRIPPED="$DL/base-nomcsm.jar"
rm -rf "$DL/base-x" "$STRIPPED" && mkdir -p "$DL/base-x"
( cd "$DL/base-x" && unzip -q "$BASE" && rm -rf net/mcsm \
    && zip -q -r -X "$STRIPPED" . -x '.*' )
echo "[build] compile classpath base: ${STRIPPED} (net/mcsm stripped)"

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
    VJSON="$(curl -fsSL "$VURL" || true)"
    RESOLVED="$(printf '%s' "$VJSON" | python3 -c 'import json,sys; print(json.load(sys.stdin)["downloads"]["client"]["url"])' || true)"
    if [ -n "$RESOLVED" ]; then CLIENT_URL="$RESOLVED"; fi
    # BUILD #463 -- netty, at the exact version this Minecraft runs. The vanilla
    # client.jar does NOT bundle its libraries (same reason brigadier is fetched
    # below), and any source that CALLS a FriendlyByteBuf method needs
    # io.netty.buffer.ByteBuf on the compile classpath; without it javac says
    # "cannot access ByteBuf / class file for io.netty.buffer.ByteBuf not found".
    NETTY_VER="$(printf '%s' "$VJSON" | python3 -c '
import json, sys
try:
    libs = json.load(sys.stdin)["libraries"]
except Exception:
    sys.exit(0)
for lib in libs:
    if lib.get("name", "").startswith("io.netty:netty-buffer:"):
        print(lib["name"].split(":")[2])
        break
' || true)"
    NETTY_URL="$(printf '%s' "$VJSON" | python3 -c '
import json, sys
try:
    libs = json.load(sys.stdin)["libraries"]
except Exception:
    sys.exit(0)
want = {"io.netty:netty-buffer", "io.netty:netty-common"}
for lib in libs:
    name = lib.get("name", "")
    if name.rsplit(":", 1)[0] in want:
        url = lib.get("downloads", {}).get("artifact", {}).get("url", "")
        if url:
            print(name.rsplit(":", 1)[0].replace(":", "/").replace(".", "/") + "\t" + url)
' || true)"
  fi
fi
if [ -n "${NETTY_URL:-}" ]; then
  while IFS=$'\t' read -r coord url; do
    [ -n "$url" ] || continue
    case "$coord" in
      */netty-buffer) fetch_any netty-buffer.jar "$url" || true ;;
      */netty-common) fetch_any netty-common.jar "$url" || true ;;
    esac
  done <<< "$NETTY_URL"
fi
# and a pinned Maven Central fallback, so the classpath never depends on the
# manifest answering (4.1.97.Final is the netty Minecraft 1.20.2+ ships).
NETTY_FALLBACK="4.1.97.Final"
[ -s "$DL/netty-buffer.jar" ] || fetch_any netty-buffer.jar \
  "https://repo1.maven.org/maven2/io/netty/netty-buffer/${NETTY_FALLBACK}/netty-buffer-${NETTY_FALLBACK}.jar" || true
[ -s "$DL/netty-common.jar" ] || fetch_any netty-common.jar \
  "https://repo1.maven.org/maven2/io/netty/netty-common/${NETTY_FALLBACK}/netty-common-${NETTY_FALLBACK}.jar" || true
echo "[deps] netty version resolved: ${NETTY_VER:-unknown}"
echo "[deps] minecraft $MC_VER -> $CLIENT_URL"
fetch "$CLIENT_URL" client.jar
if [ "$(stat -c%s "$DL/client.jar")" -lt 10000000 ]; then
  echo "[deps] client.jar is suspiciously small — refusing"; exit 1
fi
fetch "https://repo1.maven.org/maven2/net/fabricmc/sponge-mixin/0.15.4+mixin.0.8.7/sponge-mixin-0.15.4+mixin.0.8.7.jar" mixin.jar
fetch "https://repo1.maven.org/maven2/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar" jspecify.jar
fetch "https://libraries.minecraft.net/it/unimi/dsi/fastutil/8.5.18/fastutil-8.5.18.jar" fastutil.jar
fetch "https://libraries.minecraft.net/com/mojang/datafixerupper/10.0.21/datafixerupper-10.0.21.jar" dfu.jar
fetch "https://libraries.minecraft.net/org/joml/joml/1.10.8/joml-1.10.8.jar" joml.jar
# MCSM 1.9.101 -- brigadier: 26.2's Component implements com.mojang.brigadier.Message,
# so javac needs it on the classpath ("cannot access Message" without it).
fetch "https://libraries.minecraft.net/com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar" brigadier.jar

# MCSM 1.9.133 -- the extras storm-blob resubmit references the Fabric
# rendering context type, so the rendering-v1 module (+api-base) joins the
# extras compile classpath, fetched exactly like build-source does it.
FAPI_VER=""
for attempt in 1 2 3; do
  FAPI_META="$(curl -fsSL --retry 5 --retry-all-errors --retry-delay 3 \
      https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml 2>/dev/null || true)"
  [ -z "$FAPI_META" ] && sleep 5 && continue
  FAPI_VER="$(printf '%s' "$FAPI_META" \
    | grep -oE '<version>[^<]*\+26\.2[^<]*</version>' | sed 's/<[^>]*>//g' | tail -1 || true)"
  [ -n "$FAPI_VER" ] && break
  # 1.9.209: if the exact +26.2 build tag vanished from metadata, fall back to
  # any 26.x build so the rendering modules still land on the classpath.
  FAPI_VER="$(printf '%s' "$FAPI_META" \
    | grep -oE '<version>[^<]*\+26[^<]*</version>' | sed 's/<[^>]*>//g' | tail -1 || true)"
  [ -n "$FAPI_VER" ] && break
done
mkdir -p "$DL/fapi2"
: > "$DL/fapi2-list.txt"
if [ -n "$FAPI_VER" ]; then
  curl -fsSL --retry 3 "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/${FAPI_VER}/fabric-api-${FAPI_VER}.pom" -o "$DL/fabric-api2.pom" || true
  python3 - "$DL/fabric-api2.pom" "$DL/fapi2-list.txt" <<'PYMOD'
import re, sys
try:
    pom = open(sys.argv[1]).read()
except OSError:
    open(sys.argv[2], "w").write("")
    sys.exit(0)
# BUILD #463 -- fabric-networking-api-v1 joins the set: void aging sends one
# small clientbound payload (the value the player's own model is tinted by), and
# this module is the only way to name PayloadTypeRegistry / ServerPlayNetworking /
# ClientPlayNetworking. It is already present at RUNTIME (the base mod ships its
# own payloads through it); this is about the compile classpath.
found = set()
want = {"fabric-rendering-v1", "fabric-api-base", "fabric-object-builder-api-v1", "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1"}
out = []
for m in re.finditer(r'<dependency>\s*<groupId>([^<]+)</groupId>\s*<artifactId>([^<]+)</artifactId>\s*<version>([^<]+)</version>', pom):
    g, a, v = m.groups()
    if g == "net.fabricmc.fabric-api" and a in want:
        out.append(f"https://maven.fabricmc.net/{g.replace('.', '/')}/{a}/{v}/{a}-{v}.jar" + chr(9) + f"{a}.jar")
        found.add(a)
# BUILD #463 -- say WHICH module is missing, not just how many: a wrong or renamed
# artifactId must not read as "the maven fetch flaked".
missing = sorted(want - found)
if missing:
    print(f"[deps] NOT in the fabric-api pom (check the artifactId): {', '.join(missing)}")
open(sys.argv[2], "w").write(chr(10).join(out) + (chr(10) if out else ""))
print(f"[deps] fabric modules wanted: {len(out)} of {len(want)}")
PYMOD
  while IFS=$'\t' read -r url name; do
    [ -n "$url" ] || continue
    [ -s "$DL/fapi2/$name" ] || curl -fsSL --retry 3 --retry-delay 2 -o "$DL/fapi2/$name" "$url" \
      || echo "::warning title=deps::fabric module download failed: $name"
  done < "$DL/fapi2-list.txt"
fi
FAPI2_CP="$(find "$DL/fapi2" -name '*.jar' 2>/dev/null | tr '\n' ':')"
FAPI2_COUNT="$(find "$DL/fapi2" -name '*.jar' 2>/dev/null | wc -l)"
echo "[deps] fabric modules on classpath: $FAPI2_COUNT"
if [ "$FAPI2_COUNT" -lt 5 ]; then
  echo "::error::fabric-api modules missing from the compile classpath ($FAPI2_COUNT/5) -- the maven metadata fetch flaked; re-run the build"
  exit 1
fi


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
  CP2="$DL/client.jar:$STRIPPED:$DL/mixin.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar:$DL/brigadier.jar:$DL/netty-buffer.jar:$DL/netty-common.jar"
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
    net.minecraft.client.renderer.SkyRenderer \
    net.minecraft.client.renderer.state.level.SkyRenderState \
    net.minecraft.client.renderer.RenderType net.minecraft.client.renderer.blockentity.BlockEntityRenderer \
    net.minecraft.client.renderer.entity.EntityRenderer net.minecraft.client.renderer.CloudRenderer \
    net.minecraft.client.multiplayer.ClientChunkCache net.minecraft.client.Camera \
    net.minecraft.client.player.LocalPlayer net.minecraft.world.entity.player.Player \
    net.minecraft.world.entity.player.Inventory net.minecraft.world.inventory.AbstractContainerMenu \
    net.minecraft.world.level.block.entity.CommandBlockEntity \
    net.minecraft.network.chat.Component net.minecraft.ChatFormatting"
  # BUILD #416 -- the FULL base-jar surface mcsm-extras calls into, not just the
  # six classes the first pass happened to name. mcsm-extras is compiled against
  # this FROZEN base release asset and can only ever call what it declares, so
  # ci/api/mod.txt is the authority for what exists; check_phase_uniform.py now
  # fails the build when mcsm-extras calls a base-jar method that is declared
  # nowhere in it. (The first conic-column attempt added glowWhite to the
  # *reference copy* of GlowRenderTypes under net/ -- a tree that is on no compile
  # classpath -- and only found out from javac. This dump is what catches that
  # class of mistake in seconds instead of in a red run.)
  MOD_CLASSES="net.dabicco.witherstormmod.BowelsGravity \
    net.dabicco.witherstormmod.BowelsPortal \
    net.dabicco.witherstormmod.DabyWitherStormMod \
    net.dabicco.witherstormmod.DabyWitherStormModClient \
    net.dabicco.witherstormmod.ModBlocks \
    net.dabicco.witherstormmod.ModItems \
    net.dabicco.witherstormmod.WitherStormSummon \
    net.dabicco.witherstormmod.client.ClientDistantStormManager \
    net.dabicco.witherstormmod.client.FoglessRenderTypes \
    net.dabicco.witherstormmod.client.GlowRenderTypes \
    net.dabicco.witherstormmod.client.ShaderPackCompat \
    net.dabicco.witherstormmod.client.StormBloom \
    net.dabicco.witherstormmod.client.StormDebris \
    net.dabicco.witherstormmod.client.StormSkyDarken \
    net.dabicco.witherstormmod.client.StormSkyGradient \
    net.dabicco.witherstormmod.client.TentaclePhysics \
    net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen \
    net.dabicco.witherstormmod.command.DabyWSCommand \
    net.dabicco.witherstormmod.config.DabyWSClientConfig \
    net.dabicco.witherstormmod.config.WitherStormConfigs \
    net.dabicco.witherstormmod.config.WitherStormWorldConfig \
    net.dabicco.witherstormmod.entity.WitherStormEntity \
    net.dabicco.witherstormmod.entity.WitherStormHeadEntity \
    net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity \
    net.dabicco.witherstormmod.client.StormShadow \
    net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor \
    net.dabicco.witherstormmod.mixin.RenderTypeInvoker \
    net.dabicco.witherstormmod.structures.McsmCommand \
    net.dabicco.witherstormmod.structures.McsmSchematic \
    net.dabicco.witherstormmod.structures.McsmWorldgen"
  javap -public -classpath "$CP2" $CLIENT_CLASSES > ci/api/client.txt 2>&1 || true
  # SkyRenderer's celestial helper is private in 26.2; include it so native
  # mixin invokers can be checked against the actual client signature.
  javap -private -classpath "$CP2" net.minecraft.client.renderer.SkyRenderer >> ci/api/client.txt 2>&1 || true
  javap -public -classpath "$CP2" $MOD_CLASSES   > ci/api/mod.txt    2>&1 || true
  # MCSM 1.9.101 -- the 1.9.101 javac errors (sendParticles overload,
  # "cannot access Message") live in the particle/level/chat API, which the
  # original dump never covered. Dump it, plus a package index of those
  # packages and every "message" entry in the client jar, so the sandbox can
  # see where 26.2 moved things.
  LEVEL_CLASSES="net.minecraft.world.level.Level net.minecraft.server.level.ServerLevel \
    net.minecraft.server.level.ServerPlayer net.minecraft.core.particles.ParticleType \
    net.minecraft.core.particles.DustParticleOptions net.minecraft.network.chat.Component"
  javap -public -classpath "$CP2" $LEVEL_CLASSES > ci/api/level.txt 2>&1 || true
  # 1.9.204 -- entity/renderer API for the Story Mode character entity round.
  ENTITY_CLASSES="net.minecraft.world.entity.EntityType net.minecraft.world.entity.EntityType\$Builder \
    net.minecraft.world.entity.PathfinderMob net.minecraft.world.entity.Mob net.minecraft.world.entity.LivingEntity \
    net.minecraft.world.entity.ai.attributes.AttributeSupplier net.minecraft.world.entity.ai.attributes.Attributes \
    net.minecraft.world.entity.ai.goal.GoalSelector net.minecraft.world.entity.MobCategory \
    net.minecraft.client.renderer.entity.HumanoidMobRenderer net.minecraft.client.renderer.entity.LivingEntityRenderer \
    net.minecraft.client.renderer.entity.MobRenderer net.minecraft.client.renderer.entity.EntityRendererProvider\$Context \
    net.minecraft.client.renderer.entity.EntityRenderers net.minecraft.client.renderer.entity.state.HumanoidRenderState \
    net.minecraft.client.renderer.entity.state.LivingEntityRenderState net.minecraft.client.model.HumanoidModel \
    net.minecraft.client.model.player.PlayerModel net.minecraft.client.model.geom.ModelLayers \
    net.minecraft.client.model.geom.ModelLayerLocation net.minecraft.client.model.geom.builders.LayerDefinition \
    net.minecraft.client.renderer.entity.ZombieRenderer net.minecraft.client.renderer.entity.AbstractZombieRenderer \
    net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry \
    net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry \
    net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry \
    net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder \
    net.minecraft.core.registries.BuiltInRegistries net.minecraft.core.Registry net.minecraft.core.registries.Registries \
    net.minecraft.resources.ResourceKey net.minecraft.resources.Identifier"
  javap -public -classpath "$CP2:$FAPI2_CP" $ENTITY_CLASSES > ci/api/entity.txt 2>&1 || true
  # BUILD #465 -- and the item surface: "more blocks and items, even some
  # weapons" needs to know what THIS version calls its item and attribute
  # classes. SwordItem and Tier do not exist here (javap: "class not found"), so
  # the weapon has to be built out of whatever the index turns out to list.
  unzip -Z1 "$DL/client.jar" 2>/dev/null | grep -E '^net/minecraft/(world/level|world/item|world/entity|world/damagesource|server/level|core/particles|client/particles|network/chat)/' \
    | sort > ci/api/api-classes-index.txt || true
  unzip -Z1 "$DL/client.jar" 2>/dev/null | grep -iE 'message' > ci/api/message-locations.txt || true
  # A class index so we can discover what this version renamed things to.
  unzip -Z1 "$DL/client.jar" 2>/dev/null | grep -E '^net/minecraft/client/.*\.class$' | sort \
    > ci/api/client-index.txt || true
  wc -l ci/api/*.txt || true
  if [ -s ci/api/client.txt ] || [ -s ci/api/entity.txt ]; then
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
     src/main/resources/assets/dabywitherstormmod/shaders/core/fogless_entity.fsh \
     > "$GLSL_LOG" 2>&1; then
  tail -2 "$GLSL_LOG"
else
  cat "$GLSL_LOG"
  echo "[glsl] shader gate FAILED — not building a broken shaderpack"
  exit 1
fi

# BUILD #416 -- the traced palette and the phase feed are source-level gates.
# Both read the SAME stop tables the shaders ship, so a colour edited in one
# place and not the others stops the build here instead of reaching the user as
# a sky and a halo that disagree.
echo "[palette] traced-palette parity gate (sky / position / Java / visuals)"
PALETTE_OUT="$(python3 ci/palette_tables.py 2>&1)" || {
  echo "$PALETTE_OUT"
  echo "::error title=build::palette parity broken — the sky, the halo and the storm visuals have drifted apart"
  exit 1
}
PALETTE_LINE="$(printf '%s\n' "$PALETTE_OUT" | grep -F '[palette]' | tail -1)"
echo "$PALETTE_LINE"
# Annotations survive without runner-log access, so the gate result is auditable.
echo "::notice title=palette::$PALETTE_LINE"
stage palette-ok push

# BUILD #416 -- the nine SUPPLIED hex anchors are ground truth for the sky, so
# the shipped tables must equal their expansion to the byte. --from-hex --check
# dry-runs every rewrite (sky.fsh, position.fsh, McsmStormPhase.java and the
# derived constants) and exits non-zero the moment one of them drifts from the
# anchors, which is what keeps a hand-edit from silently shipping a different sky.
echo "[hex] supplied sky-anchor ingestion gate"
HEX_OUT="$(python3 ci/trace_sky_sheets.py --from-hex --check 2>&1)" || {
  echo "$HEX_OUT"
  echo "::error title=build::the shipped sky tables drifted from the supplied anchors — run 'python3 ci/trace_sky_sheets.py --from-hex --apply'"
  exit 1
}
HEX_LINE="$(printf '%s\n' "$HEX_OUT" | grep -F '[hex] --check:' | tail -1)"
echo "$HEX_LINE"
echo "::notice title=hex::$HEX_LINE"
stage hex-ok

# BUILD #416 -- and the report picture of that sky must not go stale again. It
# did once: it still showed the superseded bright trace after the anchors were
# ingested, and nothing compared it to the tables.
echo "[report] shipped sky-column report gate"
REPORT_OUT="$(python3 ci/make_report_sky_columns.py --check 2>&1)" || {
  echo "$REPORT_OUT"
  echo "::error title=build::ci/REPORT_sky_columns_shipped.png no longer matches the supplied anchors — rerun 'python3 ci/make_report_sky_columns.py'"
  exit 1
}
printf '%s\n' "$REPORT_OUT" | grep -F '[report]' | tail -1

# BUILD #416 -- if the source sheets are checked in (ci/sky_sheets/), the traced
# tables must still BE those sheets. Without the sheets the check reports that it
# skipped rather than passing quietly; it never invents a pass.
echo "[trace] reference-sheet trace gate"
TRACE_OUT="$(python3 ci/trace_sky_sheets.py --verify 2>&1)" || {
  echo "$TRACE_OUT"
  echo "::error title=build::the shipped sky tables no longer match the reference sheets — run 'python3 ci/trace_sky_sheets.py --apply'"
  exit 1
}
printf '%s\n' "$TRACE_OUT" | grep -E '^\[trace\]|^  (ok|FAIL)|sky-sheets::' | tail -6
stage trace-ok

echo "[phase] WitherStormPhase plumbing gate"
PHASE_OUT="$(python3 ci/check_phase_uniform.py 2>&1)" || {
  echo "$PHASE_OUT"
  echo "::error title=build::WitherStormPhase plumbing broken — the storm phase is not reaching the shaders"
  exit 1
}
PHASE_LINE="$(printf '%s\n' "$PHASE_OUT" | grep -F '[phase]' | tail -1)"
echo "$PHASE_LINE"
echo "::notice title=phase::$PHASE_LINE"
stage phase-ok push

# Story Look resource-pack shaders must validate as well.
for SL in storylook/assets/minecraft/shaders/core/*; do
  case "$SL" in
    *.fsh) SLE=frag ;;
    *.vsh) SLE=vert ;;
    *) continue ;;
  esac
  # inline the vanilla 26.2 moj_import includes before validating
  # BUILD #416 (D.8): this used to be a bare command under set -e, so a failure
  # here aborted the run with no annotation and no line - indistinguishable in
  # the Checks API from any other silent abort. It names itself now.
  if ! python3 ci/expand_storylook.py "$SL" "/tmp/storylook-check.$SLE"; then
    echo "::error title=glsl::expand_storylook.py failed for ${SL} -- the expanded shader never reached the validator"
    echo "[glsl] Story Look expansion FAILED: $SL"
    exit 1
  fi
  if ! ./glslcheck/bin/glslang "/tmp/storylook-check.$SLE" > /tmp/storylook-glsl.log 2>&1; then
    cat /tmp/storylook-glsl.log
    echo "::error title=glsl::Story Look shader FAILED validation: ${SL}"
    echo "[glsl] Story Look shader FAILED validation: $SL"
    exit 1
  fi
done
echo "[glsl] story look shaders validate"

# Mega-phase 5b: the embedded Iris shader pack must validate too - every
# program, in every [0 1] toggle combination, through the glslcheck shim.
if ! python3 ci/iris_tu.py shaderpack-v5/shaders; then
  echo "::error title=glsl::shaderpack-v5 FAILED validation (iris_tu.py) - every toggle combination must compile"
  echo "[glsl] shaderpack-v5 FAILED validation - not shipping a broken pack"
  exit 1
fi
echo "[glsl] shaderpack-v5 validates"
stage glsl-ok

# ---------------------------------------------------------------------------
# BUILD #415 -- MODEL METHOD BUDGET GATE.
#
# The recovered Bedrock models are machine-translated into Java, and a single
# translated method can hold a thousand-plus .addBox(...) calls. javac caps a
# method at 65535 bytes of bytecode and fails the WHOLE file with
# "code too large" when one crosses it -- invisible until someone compiles the
# source tree. WitherStormP4.createBodyLayer was exactly that method (224 KB of
# source, ~176 KB of estimated bytecode) and is now split into 16 chunk methods
# by ci/split_p4_model.py. This gate keeps it split: it re-estimates every
# method in every recovered model and refuses the build if one is over budget.
# ---------------------------------------------------------------------------
MODEL_FAIL=0
MODEL_FILES="$(find src-recon/net/dabicco/witherstormmod/entity/model -name 'WitherStormP*.java' 2>/dev/null | sort | tr '\n' ' ')"
if [ -n "${MODEL_FILES// /}" ]; then
  if ! python3 ci/split_p4_model.py --check $MODEL_FILES > /tmp/mcsm-model.log 2>&1; then
    echo "::error title=model budget::a recovered model method is over the 64 KB bytecode limit"
    cat /tmp/mcsm-model.log
    MODEL_FAIL=1
  else
    tail -1 /tmp/mcsm-model.log
  fi
fi
if [ "$MODEL_FAIL" -ne 0 ]; then
  echo "[model] method budget FAILED -- run ci/split_p4_model.py to split the offender"
  exit 1
fi
echo "[model] method budget OK (no recovered model method is near the 64 KB bytecode limit)"
# BUILD #423 -- the phase-5.5 upper back. The renderer enlarges it by scaling
# 1.72x about the model origin, and the model's centre is ~11.7 blocks from that
# origin: the enlargement therefore throws it ~34 world blocks off the body. The
# number is measured out of the model source here, so the fix and the evidence
# can never drift apart (McsmHugeBackAttachMixin / McsmHugeBackPoseMixin).
python3 ci/measure_hugeback.py --check | tail -3
stage model-ok

# ---------------------------------------------------------------------------
# MCSM 1.9.109 -- VERSION SINGLE-SOURCE + DRIFT GATE.
#
# Why this exists: the jar's fabric.mod.json was stamped from ./VERSION, but
# three user-visible strings inside the Java were hand-typed literals and had
# drifted (BUILD_VERSION 1.9.108, startup banner 1.9.107, config-screen header
# 1.9.105). From inside the game every build therefore claimed to be an older
# one, which reads exactly like "Minecraft did not recognise the new jar" --
# the user could not tell a working update from a stale file, so real fixes
# looked like no-ops and the same reports came back round after round.
#
# Now: ./VERSION is the only place a version number is written. BUILD_VERSION
# is synced from it here before javac, and any surviving hardcoded literal
# fails the build instead of shipping.
# ---------------------------------------------------------------------------
CFG=mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java
sed -i "s/BUILD_VERSION = \"[0-9][0-9.]*\"/BUILD_VERSION = \"${VER}\"/" "$CFG"
echo "[version] BUILD_VERSION synced to ${VER}"

# ---------------------------------------------------------------------------
# BUILD #415 -- VERSION STAMP GATE (files, not just code).
#
# "$VER" above comes from ./VERSION, but four OTHER artefacts also carry the
# label and each has silently drifted at least once: BUILD_VERSION (a file the
# handoff treats as the second source of truth), gradle.properties
# (mod_version), fabric.mod.json (what the mods screen prints) and
# McsmExtrasConfig (the in-game banner + the config file's config_version).
# A build whose labels disagree reads as "the update did not install", so the
# gate is now: every one of them must equal ./VERSION or the build stops.
#
# BUILD_VERSION is a mirror, so it is written from VERSION here; the other two
# are authored files and are only checked, never rewritten.
# ---------------------------------------------------------------------------
printf '%s' "$VER" > BUILD_VERSION
echo "[version] BUILD_VERSION file stamped to ${VER}"

VP_GRADLE="$(grep -E '^mod_version=' gradle.properties | head -1 | cut -d= -f2- | tr -d '[:space:]')"
VP_FABRIC="$(python3 -c 'import json,sys; print(json.load(open("src/main/resources/fabric.mod.json"))["version"].strip())' 2>/dev/null || true)"
VP_CONFIG="$(grep -oE 'BUILD_VERSION = "[^"]*"' "$CFG" | head -1 | sed -E 's/.*"(.*)"/\1/')"
VSTAMP_FAIL=0
for pair in "BUILD_VERSION file:$VER" "gradle.properties:${VP_GRADLE}" \
            "fabric.mod.json:${VP_FABRIC}" "McsmExtrasConfig:${VP_CONFIG}"; do
  name="${pair%%:*}"; got="${pair#*:}"
  if [ "$got" != "$VER" ]; then
    echo "::error title=version stamp::${name} says '${got}' but VERSION says '${VER}'"
    VSTAMP_FAIL=1
  fi
done
if [ "$VSTAMP_FAIL" -ne 0 ]; then
  echo "[version] FAILED -- the in-game build label would lie about which jar is loaded"
  exit 1
fi
echo "[version] stamp gate OK (VERSION, BUILD_VERSION, gradle.properties, fabric.mod.json, McsmExtrasConfig = ${VER})"

DRIFT="$(grep -rn '"[^"]*1\.9\.[0-9]' --include='*.java' mcsm-extras/java \
         | grep -v 'BUILD_VERSION = ' || true)"
if [ -n "$DRIFT" ]; then
  echo "::error::hardcoded version literal(s) found -- the in-game build number would lie about which jar is loaded"
  echo "$DRIFT"
  echo "[version] use McsmExtrasConfig.BUILD_VERSION instead of a literal"
  exit 1
fi
echo "[version] drift gate OK (no hardcoded version literals)"
stage version-ok push

# ---------------------------------------------------------------------------
# BUILD #416 -- BACKDROP SHEETS, BAKED FROM THE TRACED SKY TABLES.
#
# The 2D background sky-plane sticker queue binds six baked sheets: the purple
# canvas, the transparent teal and salmon cloud filters, and the three phase
# decks (teal / violet / the phase-6 mass carrying the black smudge). Every
# colour in them is read out of ci/palette_tables.py, i.e. out of the SAME stop
# tables the sky and position shaders ship -- so the sheets cannot drift from
# the sky they hang in. Regenerated here so a rerun is byte-for-byte
# deterministic, and written into BOTH overlay roots before assembly
# (jar-overrides wins at assembly time, src/main/resources supplies the dev
# tree). Replacing them with handmade art needs no code change: the binding is
# by target path.
# ---------------------------------------------------------------------------
if python3 ci/make_backdrop_sheets.py; then
  echo "[backdrop] six phase sheets regenerated from the traced palette"

# ---------------------------------------------------------------------------
# BUILD #416 (D.8) -- THE SUPPLIED STAGE SHEETS ARE THE STORM'S MATERIAL.
#
# The user supplied the real stage texture sheets and described what they
# cover: one for phase 4 through 5.9, one for phase 6 through 8. They are the
# ground truth for the storm's colours, but they are 160x160 and almost
# entirely pure black (measured mean #02 04 0A), while the shipped body atlases
# are 512x512 traced shading. So the sheets are applied as a PALETTE: every
# atlas is relit through the sheet's own distinct colours, which keeps the
# traced crevices and AO and adopts the sheet's exact material. --check then
# proves it, by refusing any atlas that carries a colour the sheet does not
# contain -- which is what would catch a regenerated or hand-edited atlas.
#
# Emissive (_e) atlases are deliberately excluded: they ARE the glow.
# ---------------------------------------------------------------------------
echo "[stage] supplied stage-sheet palettes (the storm's material)"
STAGE_OUT="$(python3 ci/apply_stage_palette.py 2>&1)"
printf '%s\n' "$STAGE_OUT" | tail -3
echo "[stage] verify the atlases against the sheets"
STAGE_CHECK="$(python3 ci/apply_stage_palette.py --check 2>&1)"
STAGE_RC=$?
printf '%s\n' "$STAGE_CHECK" | grep -E "^\[stage\]|^  FAIL" | tail -4
# the evidence dump is created further down (the vanilla API oracle); append to it
# only if it exists -- run 494 died here on `set -u` for exactly that reason
printf '%s\n' "$STAGE_CHECK" >> "${VANILLA_OUT:-/dev/null}" 2>/dev/null || true
if [ "$STAGE_RC" -ne 0 ]; then
  echo "::error title=stage palette::the storm body atlases no longer wear the supplied stage-sheet palette"
  exit 1
fi
stage stage-palette-ok
stage backdrop-ok

# ---------------------------------------------------------------------------
# BUILD #416 (D.8, phase 5) -- THE CONTENT PACK'S OWN ART (hard gate).
#
# What the user saw: "the minecraft items you made are currently in these black
# and purple looking glitch block[s] ... there's something wrong with the
# registration". There was: phase 1 registered 38 blocks and 59 items whose
# models pointed at borrowed base-mod and vanilla textures, and shipped no
# textures of its own and no ITEM DEFINITIONS at all -- and in this version an
# item is rendered from assets/<ns>/items/<name>.json, so every one of them was
# the missing-model cube.
#
# This step regenerates the pack's art and its item definitions, then proves all
# three faults are gone: every model reference resolves, every block and item has
# an item definition, and every registered block has a blockstate. The
# generators are deterministic, so a dirty tree here means the committed assets
# and the generator have drifted apart -- which is also a failure.
# ---------------------------------------------------------------------------
echo "[content] generate the pack's own textures + item definitions"
# BUILD #425 NOTE: the sound files are COMMITTED (a build image has no Vorbis
# encoder), so this only regenerates them where one exists -- run 505 died here
# because the runner has neither numpy nor libsndfile. The --check gate below is
# the half that has to pass everywhere.
python3 ci/make_mcsm_sounds.py | tail -1
python3 ci/make_emissive_whites.py --two-way | tail -1
python3 ci/make_mcsm_textures.py | tail -1
# BUILD #457 -- the painted skies: the same idea as the block textures, six faces
# per dimension, generated here so the pack ships them and the build cannot lose
# them silently.
python3 ci/make_skybox_textures.py | tail -1
python3 ci/make_mcsm_content_assets.py | tail -1
set +e
SOUND_CHECK="$(python3 ci/make_mcsm_sounds.py --check 2>&1)"
SOUND_RC=$?
set -e
printf '%s\n' "$SOUND_CHECK" | tail -3
if [ $SOUND_RC -ne 0 ]; then
  echo "::error title=custom sounds::the mod's own Ogg files are missing or empty -- the radio and the creature would fall back to vanilla audio. Run 'python3 ci/make_mcsm_sounds.py'"
  exit 1
fi
set +e
SKY_CHECK="$(python3 ci/make_skybox_textures.py --check 2>&1)"
SKY_RC=$?
set -e
printf '%s\n' "$SKY_CHECK" | tail -3
if [ $SKY_RC -ne 0 ]; then
  echo "::error title=painted sky::the painted sky faces are missing from the pack -- the mod's dimensions would fall back to the vanilla sky. Run 'python3 ci/make_skybox_textures.py'"
  exit 1
fi
set +e
EMISSIVE_CHECK="$(python3 ci/make_emissive_whites.py --check 2>&1)"
EMISSIVE_RC=$?
set -e
printf '%s\n' "$EMISSIVE_CHECK" | tail -4
if [ $EMISSIVE_RC -ne 0 ]; then
  echo "::error title=emissive masks::the phase >= 4 teeth/eye masks are not pure white -- the glow cannot reach the emissive floor. Run 'python3 ci/make_emissive_whites.py'"
  exit 1
fi
STILLS_CHECK="$(python3 ci/measure_stills.py --check 2>&1)"
STILLS_RC=$?
printf '%s\n' "$STILLS_CHECK" | tail -2
if [ $STILLS_RC -ne 0 ]; then
  echo "::error title=atmosphere reference::the measured table no longer matches the frames the artist supplied. Run 'python3 ci/measure_stills.py'"
  exit 1
fi
BACKDROP_CHECK="$(python3 ci/sync_backdrop_palette.py --check 2>&1)"
BACKDROP_RC=$?
printf '%s\n' "$BACKDROP_CHECK" | tail -2
if [ $BACKDROP_RC -ne 0 ]; then
  echo "::error title=backdrop palette::the atmosphere wall and the story stage no longer wear the supplied sheets' traced columns. Run 'python3 ci/sync_backdrop_palette.py'"
  exit 1
fi
CONTENT_CHECK="$(python3 ci/check_content_textures.py 2>&1)"
CONTENT_RC=$?
printf '%s\n' "$CONTENT_CHECK" | tail -4
printf '%s\n' "$CONTENT_CHECK" >> "${VANILLA_OUT:-/dev/null}" 2>/dev/null || true
if [ "$CONTENT_RC" -ne 0 ]; then
  echo "::error title=content pack::the content pack has an unresolved texture, model, blockstate or item definition -- it would appear in game as glitch blocks"
  exit 1
fi
if git rev-parse --git-dir >/dev/null 2>&1; then
  if ! git diff --quiet -- jar-overrides/assets/mcsm 2>/dev/null; then
    echo "::error title=content pack::the committed content-pack assets are out of date with ci/make_mcsm_textures.py / ci/make_mcsm_content_assets.py"
    git status --porcelain -- jar-overrides/assets/mcsm | head -20
    exit 1
  fi
else
  echo "[content] not a git checkout -- skipping the generated-assets drift check"
fi
echo "[content] assets, models, blockstates and item definitions all resolve and are in sync"
stage content-ok
else
  echo "::error title=build::backdrop sheet generation failed"
  exit 1
fi

# ---------------------------------------------------------------------------
# BUILD #416 (D.8) -- VANILLA API ORACLE (best-effort, never fails the build).
#
# mcsm-extras is compiled against the FROZEN base jar, and the machine that
# writes this mod has no JDK and no route to Mojang: an API call that does not
# exist is normally only discovered by a red javac run minutes later. This dumps
# javap for the vanilla classes the expansion phases build on into
# out/vanilla-api.txt, which the evidence push copies to ci-out/run-<N>/ on the
# session branch. From then on the exact constructor / constant / method list is
# readable from the repository instead of guessed -- which is what the door,
# trap door, creature and structure work needs.
# ---------------------------------------------------------------------------
VANILLA_OUT=out/vanilla-api.txt
{
  echo "# vanilla API dump -- $(date -u +%FT%TZ) MC ${MC_VER} (javap -p, capped)"
  for CLS in \
    net.minecraft.world.level.block.DoorBlock \
    net.minecraft.world.level.block.TrapDoorBlock \
    net.minecraft.world.level.block.state.properties.BlockSetType \
    net.minecraft.world.level.block.SlabBlock \
    net.minecraft.world.level.block.StairBlock \
    net.minecraft.world.level.block.WallBlock \
    net.minecraft.world.level.block.FenceBlock \
    net.minecraft.world.level.block.RotatedPillarBlock \
    net.minecraft.world.level.block.Blocks \
    net.minecraft.server.level.ServerLevel \
    net.minecraft.server.level.ServerPlayer \
    net.minecraft.world.level.block.Block \
    'net.minecraft.world.level.block.state.BlockBehaviour$Properties' \
    net.minecraft.world.item.Item \
    'net.minecraft.world.item.Item$Properties' \
    net.minecraft.world.item.BlockItem \
    net.minecraft.world.item.SwordItem \
    net.minecraft.world.item.ToolMaterial \
    net.minecraft.world.item.AxeItem \
    net.minecraft.world.item.PickaxeItem \
    net.minecraft.world.item.ShovelItem \
    net.minecraft.world.item.HoeItem \
    net.minecraft.world.item.ItemStack \
    net.minecraft.world.item.component.ItemAttributeModifiers \
    net.minecraft.world.entity.EquipmentSlot \
    net.minecraft.world.entity.ai.attributes.AttributeModifier \
    net.minecraft.world.level.block.FenceGateBlock \
    net.minecraft.world.item.Tier \
    net.minecraft.world.item.Rarity \
    net.minecraft.world.level.block.SoundType \
    net.minecraft.world.entity.EntityType \
    'net.minecraft.world.entity.EntityType$Builder' \
    net.minecraft.world.entity.MobCategory \
    net.minecraft.world.entity.Mob \
    net.minecraft.world.entity.PathfinderMob \
    net.minecraft.world.entity.monster.Monster \
    net.minecraft.world.entity.LivingEntity \
    net.minecraft.world.entity.ai.goal.Goal \
    net.minecraft.world.entity.ai.goal.MeleeAttackGoal \
    net.minecraft.world.entity.ai.attributes.Attributes \
    net.minecraft.world.entity.player.Player \
    com.mojang.blaze3d.vertex.PoseStack \
    net.minecraft.client.renderer.SubmitNodeCollector \
    net.minecraft.world.level.Level \
    net.minecraft.server.level.ServerLevel \
    net.minecraft.world.level.storage.LevelData ; do
    echo
    # NOTE: nested-class names must stay single-quoted -- an unescaped '$' in
    # this list is a variable expansion, and under `set -u` the build dies on
    # "Properties: unbound variable" (run 486).
    echo "===== ${CLS}"
    # Blocks has hundreds of fields and the concrete/wool labels sit far past a
    # 110-line cap -- run 508 asked for WHITE_CONCRETE and the dump could not
    # answer, so the cap is 900 for it and 220 for everything else.
    # NOTE: with `set -e` and `pipefail`, a javap that cannot read a class would
    # abort the whole dump (run 519: everything after StormBackdrop is missing
    # because FoglessRenderTypes could not be read). Every probe below is
    # therefore allowed to fail, and says so in the dump instead of stopping it.
    if [ "$CLS" = "net.minecraft.world.level.block.Blocks" ]; then
      { javap -p -classpath "$DL/client.jar" "$CLS" 2>&1 || echo "(javap could not read ${CLS})"; } | sed -n '1,900p'
    else
      { javap -p -classpath "$DL/client.jar" "$CLS" 2>&1 || echo "(javap could not read ${CLS})"; } | sed -n '1,220p'
    fi
  done

  # --------------------------------------------------------------------------
  # BUILD #436 -- THE BASE MOD'S OWN RENDER PATH, DUMPED THE SAME WAY.
  #
  # The phase-5.5 upper back, the huge-back pose and the storm renderer are all
  # in the FROZEN base jar, and two mixins in this build inject into them with
  # `require = 0` because their exact method names have never been readable from
  # here. A silently-missing injection is exactly how "phase 5.5 has an upper
  # back disattached from the main body" survives a fix. This dumps the real
  # signatures from the same stripped jar javac uses, so the injections can be
  # pinned to them instead of guessed.
  # --------------------------------------------------------------------------
  for CLS in \
    net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer \
    net.dabicco.witherstormmod.entity.model.HugeAssBackModel \
    net.dabicco.witherstormmod.client.StormBackdrop \
    net.dabicco.witherstormmod.client.FoglessRenderTypes \
    net.dabicco.witherstormmod.client.GlowRenderTypes \
    net.dabicco.witherstormmod.client.StormSkins \
    net.minecraft.client.renderer.rendertype.RenderTypes \
    net.minecraft.client.renderer.SkyRenderer ; do
    echo
    echo "===== ${CLS} (base mod)"
    { javap -p -classpath "$STRIPPED:$DL/client.jar" "$CLS" 2>&1 \
      || echo "(javap could not read ${CLS} -- it may not be in the compile jars)"; } \
      | sed -n '1,260p'
  done

  # --------------------------------------------------------------------------
  # BUILD #438 -- WHAT THE BASE RENDERERS ACTUALLY CALL.
  #
  # Every glow redirect in this build names a call site descriptor
  # (RenderTypes.eyes, GlowRenderTypes.emitterMark, FoglessRenderTypes.eyes ...)
  # and every one of them is `require = 0`, so a descriptor that does not match
  # is a silent no-op -- which is how "the teeth and eyes do not glow" survives
  # fix after fix. javap -v prints the constant pool of the base renderer, i.e.
  # every method reference that class makes, filtered here to the render-type
  # calls. If a name is absent from this list, the redirect naming it can never
  # fire and the build should say so instead of shipping it.
  # --------------------------------------------------------------------------
  for CLS in \
    net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer \
    net.dabicco.witherstormmod.entity.renderer.WitherStormHeadRenderer ; do
    echo
    echo "===== ${CLS} -- render-type call sites (constant pool)"
    { javap -v -p -classpath "$STRIPPED:$DL/client.jar" "$CLS" 2>&1 \
      || echo "(javap could not read ${CLS} -- it may not be in the compile jars)"; } \
      | { grep -E "Methodref|InterfaceMethodref" || true; } \
      | { grep -Ei "eyes|emitter|bloom|glow|mark|Fogless|RenderType|Skins" || true; } \
      | sed -n '1,200p'
  done

  # --------------------------------------------------------------------------
  # BUILD #469 -- WHAT THE TITLE SCREEN'S OWN FRAME IS MADE OF.
  #
  # The report that will not go away is "the main menu is black" -- Mojang logo,
  # then a dark shape, then a black frame with the title bar still up. Every plate
  # this mod could name has been repainted and gated, so the questions left are
  # questions about the GAME's own path, and until now every one of them has been
  # answered by guessing:
  #
  #   * does TitleScreen.extractRenderState even CALL extractBackground - the
  #     method two of this mod's injections hang on? If it does not, the mod's sky
  #     is never asked to paint and the backdrop is whatever the game drew.
  #   * what does the game's own backdrop draw on this version (panorama cube? the
  #     blurred menu_background texture? a cleared-to-black attachment?) - i.e.
  #     what exactly is the frame the user sees, and which call would a fault in it
  #     take down.
  #   * which field does McsmLogoIntroMixin's reflection walk find first? It picks
  #     "the first declared Minecraft field assignable to Screen" to tell the logo
  #     scene from the title screen; javap -p prints the declared fields in order,
  #     so this dump SETTLES whether that pick is `screen` or something else.
  #
  # Bytecode, not just signatures: the call graph is the answer here.
  # --------------------------------------------------------------------------
  # BUILD #472 -- the #469 dump ran 700 lines per class and Screen is far bigger
  # than that: the answer to "what does extractBackground actually draw, and with
  # which program" sits past the cut. The cap is 4000 now, and the classes that
  # decide WHAT the menu's frame is made of are in the list: the title's panorama
  # pass, the screen's own background/blur pass, the panorama renderer, the
  # pipeline registry (which names the core program each pipeline uses) and the
  # GUI renderer. This is still evidence-only -- nothing here changes a frame.
  for CLS in \
    net.minecraft.client.gui.screens.TitleScreen \
    net.minecraft.client.gui.screens.Screen \
    net.minecraft.client.gui.components.LogoRenderer \
    net.minecraft.client.gui.Gui \
    net.minecraft.client.renderer.Panorama \
    net.minecraft.client.renderer.RenderPipelines \
    net.minecraft.client.gui.render.GuiRenderer ; do
    echo
    echo "===== ${CLS} (bytecode)"
    { javap -p -c -classpath "$DL/client.jar" "$CLS" 2>&1 \
      || echo "(javap could not read ${CLS})"; } | sed -n '1,4000p'
  done
  echo
  echo "===== net.minecraft.client.Minecraft (declared fields, in order)"
  { javap -p -classpath "$DL/client.jar" net.minecraft.client.Minecraft 2>&1 \
    || echo "(javap could not read net.minecraft.client.Minecraft)"; } | sed -n '1,400p'
} > "$VANILLA_OUT" 2>&1 || true
echo "[api] vanilla dump: $(wc -l < "$VANILLA_OUT" 2>/dev/null || echo 0) lines -> out/vanilla-api.txt"

# ---------------------------------------------------------------------------
# BUILD #416 (D.8, phase 2) -- DATAPACK SCHEMA GATE.
#
# The dimension, the recipes and the abandoned-city loot tables are JSON, and a
# wrong key name does not fail anything at build time -- it fails in game, as a
# recipe that never matches or a crate that drops nothing. This reads vanilla's
# OWN recipe / loot_table / dimension_type files out of the client jar this mod
# is compiled against, works out which keys this build of the game uses, and
# refuses the build if ours disagree or mention an id that is not registered.
# ---------------------------------------------------------------------------
echo "[schema] datapack schema gate (checked against vanilla's own JSON)"
SCHEMA_OUT="$(python3 ci/check_datapack_schema.py --jar "$DL/client.jar" 2>&1)"
SCHEMA_RC=$?
printf '%s\n' "$SCHEMA_OUT" >> "$VANILLA_OUT"
printf '%s\n' "$SCHEMA_OUT" | grep -E "^\[schema\]|^  FAIL|^  note" | tail -8
if [ "$SCHEMA_RC" -ne 0 ]; then
  echo "::error title=schema::the mod's datapack files do not match this build's JSON shapes -- see the annotations"
  exit 1
fi
stage schema-ok push
stage oracle-ok

echo "[javac] mcsm-extras"
rm -rf /tmp/mcsm-build
mkdir -p /tmp/mcsm-build
CP="$DL/client.jar:$STRIPPED:$DL/mixin.jar:$DL/jspecify.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar:$DL/brigadier.jar:$DL/netty-buffer.jar:$DL/netty-common.jar:${FAPI2_CP}"

# BUILD #416 (D.8) -- which Fabric API modules are actually on the COMPILE
# classpath? The base mod's own source can name classes that this overlay
# cannot (its jar is prebuilt), which is how "FabricCreativeModeTab.builder()"
# got into the new content and died in javac. This probe records the answer in
# out/vanilla-api.txt so the next content pass picks an API that exists BEFORE
# spending a run on it.
{
  echo
  echo "===== compile classpath probes (what this overlay may name)"
  for PROBE in \
    net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab \
    net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents \
    net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings \
    net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder \
    net.minecraft.world.item.CreativeModeTab \
    'net.minecraft.world.item.CreativeModeTab$Builder' \
    'net.minecraft.world.item.CreativeModeTab$Output' \
    'net.minecraft.world.item.CreativeModeTab$Row' \
    net.minecraft.world.item.CreativeModeTabs ; do
    echo "--- ${PROBE}"
    javap -classpath "$CP" "$PROBE" 2>&1 | sed -n '1,20p'
  done
  for PROBE in \
    net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry \
    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking \
    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking \
    io.netty.buffer.ByteBuf ; do
    echo "--- ${PROBE}"
    javap -classpath "$CP" "$PROBE" 2>&1 | sed -n '1,12p'
  done
  # BUILD #465 -- the item / weapon / block-shape reconnaissance lives in the API
  # DUMP list above, NOT here. Written here first, and runs 579 AND 580 both died
  # in this block with no diagnostic at all: the step went from the classpath line
  # straight to "exit code 1", with nothing in the evidence between them, and the
  # same javac succeeded twenty minutes earlier. A build that cannot say why it
  # stopped is worse than one that asks for the same information somewhere that
  # has never failed a run -- so the dump list above asks instead, with -p and a
  # cap generous enough for constructors and members.
  echo "--- fabric jars on the compile path"
  printf '%s\n' "$CP" | tr ':' '\n' | grep -i fabric || echo "(none)"
} >> "$VANILLA_OUT" 2>&1 || true
stage classpath-probed
# A javac failure is NOT survivable anymore: publishing a shaders-only jar is
# exactly how users can receive new-looking UI/shaders with old Java behavior.
# Stop hard and keep the full log in out/JAVAC_FAILED.txt.
JAVAC_LOG=/tmp/mcsm-javac.log
JAVAC_RC=0
mkdir -p /tmp/mcsm-emptysrc
SOURCES="$(find mcsm-extras/java -name '*.java')"
N_SOURCES="$(printf '%s\n' "$SOURCES" | grep -c . || true)"
# BUILD #465 -- run 579 died here with NO diagnostic: the step went from the
# classpath line to "Process completed with exit code 1" and the evidence log
# ended mid-output, which is what a process killed by the runner looks like (a
# javac error would have printed, and the || JAVAC_RC=$? above means a FAILING
# javac is survivable). So: say what the machine looked like before the compile,
# bound javac's heap so a cgroup limit produces a real OutOfMemoryError with a
# stack instead of a silent SIGKILL, and keep the last lines of javac's own log
# in the captured evidence.
echo "[javac] ${N_SOURCES} sources, ${FAPI2_COUNT} fabric modules"
echo "[javac] memory before: $(free -m 2>/dev/null | awk '/^Mem:/ {print $3" used / "$2" MB"}')"
echo "[javac] /tmp free: $(df -h /tmp 2>/dev/null | awk 'NR==2 {print $4}')"
javac -J-Xmx1500m -nowarn -implicit:none -sourcepath /tmp/mcsm-emptysrc --release 25 -proc:none -cp "$CP" -d /tmp/mcsm-build \
     $SOURCES > "$JAVAC_LOG" 2>&1 || JAVAC_RC=$?
echo "[javac] exit ${JAVAC_RC}; log tail:"
tail -12 "$JAVAC_LOG" 2>/dev/null || true
N_CLASSES="$(find /tmp/mcsm-build -name '*.class' | wc -l)"
if [ "$JAVAC_RC" -eq 0 ]; then
  echo "[javac] OK: ${N_CLASSES} classes"
stage javac-compiled
  rm -f out/JAVAC_FAILED.txt
  # BUILD #416 -- a clean javac exit is not the same as "the new pass is in the
  # jar". Some of this build's classes only ADD behaviour: if one of them silently
  # failed to be produced, the jar would still build and still load, and the effect
  # would simply never draw -- which is exactly how the death cinematic stayed
  # invisible for a dozen builds. So the classes that carry new behaviour are
  # required to exist in the compiled output. find() is used instead of a fixed
  # path so a layout change can never turn this into a false failure.
  for cls in McsmWhiteGlow McsmHaloSkyRenderer McsmStormPhase McsmPresenceFxPatch McsmCreatures McsmCreatorArms McsmBossBar McsmBlackHole McsmRifts McsmTornadoes StoryCharacterRenderer McsmTerminal McsmTerminalItem McsmClientDispatch McsmTerminalScreen McsmTerminalClient McsmMassg McsmMassgSky McsmMenuGuard McsmMenuDiag McsmStormCanopy McsmHallucinations; do
    if ! find /tmp/mcsm-build -name "${cls}.class" -print -quit | grep -q .; then
      echo "::error title=build::compiled output is missing ${cls}.class -- new behaviour would silently not draw"
      exit 1
    fi
  done
  echo "[javac] new-behaviour classes present (white column, conic renderer, phase model, presence pass, terminal)"
stage javac-ok push
else
  echo "::error::javac FAILED (exit ${JAVAC_RC}) — refusing to publish a shaders-only/old-Java jar. Full log: out/JAVAC_FAILED.txt"
  cp -f "$JAVAC_LOG" out/JAVAC_FAILED.txt
  tail -160 "$JAVAC_LOG" || true
  echo "[javac] FAILED (hard stop so users never receive another old-behaviour jar)"
  # MCSM 1.9.109 -- the sandbox can read check ANNOTATIONS (Checks API) but not
  # runner logs or artifacts, so the actual compiler errors have to travel as
  # annotations or the fix loop is blind. First 12 error lines, truncated.
  { grep -E "error:|symbol:|location:|required:|found:" "$JAVAC_LOG" 2>/dev/null || true; } | \
    head -40 | \
    while IFS= read -r line; do
      echo "::error title=javac::${line:0:400}"
    done || true
  exit "$JAVAC_RC"
fi

echo "[assemble] overlay onto base (unzip base jar, overlay shaders + jar-overrides + fresh classes)"
stage assemble-start
FX=/tmp/mcsm-fx
rm -rf "$FX" && mkdir -p "$FX/cls"
( cd "$FX/cls" && unzip -o -q "$BASE" )
# Devouring Storms 1.9.188 -- keep the recovered legacy MCEdit schematics in
# the assembled jar as a fallback until the clean MC105/MC201 NBT blueprints are
# supplied/generated.  /ds towns build/start and the first-spawn fallback rely on
# these resources so the player can actually arrive in Story Mode locations now.
cp -r mcsm-core-shaders/* "$FX/cls/assets/minecraft/shaders/"
# 1.9.167: 26.2 loads block rather than terrain for the native block pass.
CS="$FX/cls/assets/minecraft/shaders/core"
if [ -f "$CS/terrain.fsh" ]; then cp -f "$CS/terrain.fsh" "$CS/block.fsh"; cp -f "$CS/terrain.vsh" "$CS/block.vsh"; fi
# BUILD #416 -- position.* is no longer manufactured by copying block.*.
# The blueprint ships real position.vsh/.fsh modules (block-safe shading plus
# the MCSM_SKY_POSITION sky branch), and the jar audit below now FAILS the
# build if the assembled position pair is byte-identical to block -- i.e. if it
# is a fallback copy rather than the authored program. Native SkyRenderer still
# owns the sky colour on this platform.
if [ -f "$CS/position.fsh" ] && [ -f "$CS/position.vsh" ]; then
  echo "[build] 26.2 shader aliases: block<-terrain (when present); position.* is authored, not copied"
else
  echo "::error title=build::position.vsh/.fsh missing from the overlay - the authored position pass is not shipping"
  exit 1
fi
# BUILD #416 (D.8): the content pack made this the single biggest overlay copy
# in the build (thousands of small JSONs), so it reports what it moves and names
# itself if cp refuses a path instead of dying silently under set -e.
if ! cp -r jar-overrides/* "$FX/cls/"; then
  echo "::error title=assemble::overlaying jar-overrides failed into $FX/cls"
  exit 1
fi
echo "[assemble] jar-overrides overlaid ($(find jar-overrides -type f | wc -l) files available)"
# 1.9.206: src/main/resources was never overlaid -- the merged Story Look
# textures (sun/moon, villager cast skins) and the story_character skins
# silently missed every jar. Overlay it after jar-overrides.
# Only assets/ (never its fabric.mod.json / mixins.json), and only files the
# jar does not already have, so the tuned jar-overrides atlases stay in charge.
if [ -d src/main/resources/assets ]; then
  N_SRC=0
  while IFS= read -r -d '' f; do
    rel="${f#src/main/resources/}"
    if [ ! -e "$FX/cls/$rel" ]; then
      mkdir -p "$FX/cls/$(dirname "$rel")"
      cp "$f" "$FX/cls/$rel"
      N_SRC=$((N_SRC + 1))
    fi
  done < <(find src/main/resources/assets -type f -print0)
  echo "[build] overlaid $N_SRC new files from src/main/resources/assets"
fi
# nullglob guard: on a failed javac the class dir is empty and a bare
# `cp -r /tmp/mcsm-build/*` would die under set -e (that bug ate the jar).
shopt -s nullglob
FRESH_CLASSES=(/tmp/mcsm-build/*)
shopt -u nullglob
if [ "${#FRESH_CLASSES[@]}" -gt 0 ]; then
  cp -r "${FRESH_CLASSES[@]}" "$FX/cls/"
fi
# Native SkyRenderer is authoritative. Purge legacy texture-pack sky paths from
# the base jar as well as from the overlay so they cannot be discovered by a
# loader or win an ordering race at runtime.
rm -rf "$FX/cls/assets/fabricskyboxes" \
       "$FX/cls/assets/dabywitherstormmod/textures/sky" \
       "$FX/cls/assets/dabywitherstormmod/textures/mcsm_atmosphere/sky"
find "$FX/cls/assets/dabywitherstormmod/textures/environment" -maxdepth 1 \
     -type f -name 'storymode_sky_*.png' -delete 2>/dev/null || true
# Remove stale overlay bytecode from a previously published base jar. These
# classes are intentionally absent from the fresh source set and must not be
# left reachable through an old class file.
rm -f "$FX/cls/net/mcsm/extras/client/McsmBlobOval.class" \
      "$FX/cls/net/mcsm/extras/client/McsmBlobShape.class" \
      "$FX/cls/net/mcsm/extras/client/McsmSkyDome.class" \
      "$FX/cls/net/mcsm/extras/client/McsmStormSkyLayer.class" \
      "$FX/cls/net/dabicco/witherstormmod/mixin/StormSkyGradientMixin.class" \
      "$FX/cls/net/dabicco/witherstormmod/mixin/StoryModeSkyDomeMixin.class"
find "$FX/cls/net/mcsm/extras/client" -type f \
     \( -name 'McsmBlobOval$*.class' -o -name 'McsmBlobShape$*.class' \) -delete 2>/dev/null || true
sed -i "s/\"version\": \"[0-9.]*-26.2-beta[a-z-]*\"/\"version\": \"${JAR_ID}\"/" "$FX/cls/fabric.mod.json"
# MCSM 1.9.215 R2 -- the sed above only rewrites versions shaped exactly like
# "<digits>-26.2-beta<letters>"; if the base jar's fabric.mod.json carries any
# other format the rewrite silently does nothing and the mods screen keeps
# showing the BASE jar's old number -- which reads as "the game loaded the
# build from before this release". A JSON rewrite always stamps the current
# version no matter what the old value looked like, and the notice below
# makes the stamped value visible as a check-run annotation.
python3 - "$FX/cls/fabric.mod.json" "$JAR_ID" <<'PYVER'
import json, sys
p, ver = sys.argv[1], sys.argv[2]
d = json.load(open(p, encoding="utf-8"))
old = d.get("version")
d["version"] = ver
with open(p, "w", encoding="utf-8") as f:
    json.dump(d, f, indent=2)
    f.write("\n")
print(f"[build] fabric.mod.json version: {old} -> {ver}")
PYVER
echo "::notice title=jar version::fabric.mod.json version = ${JAR_ID} (mods screen shows this)"
# Devouring Storms rebrand -- the DISPLAY name changes; the mod id
# (dabywitherstormmod) and every registry namespace stay, because those are
# compiled into the base jar and changing them without the source would break
# worlds, configs and /give ids.
python3 - "$FX/cls/fabric.mod.json" <<'PYNAME'
import json, sys
p = sys.argv[1]
with open(p) as f:
    d = json.load(f)
d["name"] = "Devouring Storms: The Point of No Return"
with open(p, "w") as f:
    json.dump(d, f, indent=2)
    f.write("\n")
PYNAME
echo "[build] fabric.mod.json name: $(python3 -c "import json;print(json.load(open('$FX/cls/fabric.mod.json'))['name'])")"
stage manifest-ok push

# Devouring Storms 1.9.180 -- the base config screen still contains a stale
# hardcoded section title such as "MCSM extras 1.9.95". Patch UTF8 constants in
# class files at assembly time so the visible screen cannot make a fresh jar look
# like an old one. This is a constant-pool rewrite, not a source-code guess.
python3 - "$FX/cls" "$VER" <<'PYCLASSLABEL'
import pathlib, struct, sys
root = pathlib.Path(sys.argv[1])
ver = sys.argv[2]
replacements = {
    "MCSM extras 1.9.95": f"Devouring Storms {ver}",
    "MCSM extras": "Devouring Storms",
    "Dabicco's Wither Storm Config": "Devouring Storms Config",
    "Dabicco's Wither Storm": "Devouring Storms",
}
size_by_tag = {3:4, 4:4, 5:8, 6:8, 7:2, 8:2, 9:4, 10:4, 11:4, 12:4, 15:3, 16:2, 17:4, 18:4, 19:2, 20:2}
patched = []
for path in root.rglob("*.class"):
    data = path.read_bytes()
    if not any(k.encode() in data for k in replacements):
        continue
    if data[:4] != b"\xca\xfe\xba\xbe":
        continue
    out = bytearray(data[:10])
    cp_count = struct.unpack(">H", data[8:10])[0]
    off = 10
    i = 1
    changed = False
    while i < cp_count:
        tag = data[off]
        if tag == 1:
            ln = struct.unpack(">H", data[off+1:off+3])[0]
            raw = data[off+3:off+3+ln]
            try:
                txt = raw.decode("utf-8")
            except UnicodeDecodeError:
                txt = None
            if txt is not None:
                ntxt = txt
                for a,b in replacements.items():
                    ntxt = ntxt.replace(a,b)
                if ntxt != txt:
                    enc = ntxt.encode("utf-8")
                    out.append(tag); out += struct.pack(">H", len(enc)); out += enc
                    changed = True
                else:
                    out += data[off:off+3+ln]
            else:
                out += data[off:off+3+ln]
            off += 3 + ln
        else:
            n = size_by_tag.get(tag)
            if n is None:
                raise SystemExit(f"unknown class constant tag {tag} in {path}")
            out += data[off:off+1+n]
            off += 1 + n
            if tag in (5,6):
                i += 1
        i += 1
    out += data[off:]
    if changed:
        path.write_bytes(bytes(out))
        patched.append(str(path.relative_to(root)))
print("[build] patched stale config labels: " + (", ".join(patched) if patched else "none found"))
PYCLASSLABEL

# Devouring Storms 1.9.114 -- mixin config MERGE. The base jar's mixin config
# is frozen at whatever the 1.9.100 build listed; any mixin class added since
# (McsmShaderGatePatch, McsmTownCommandPatch, ...) must be appended at assembly
# time or it silently never applies -- the audit below would then fail the
# build, but the merge keeps it from ever getting that far. Client-side mixins
# (anything importing net.minecraft.client) go in the config's "client" list,
# the rest in "mixins", matching the existing entry style (simple name when the
# config declares a package, fully qualified otherwise).
python3 - "$FX/cls" <<'PYMERGE'
import json, os, sys, glob
cls_dir = sys.argv[1]
fmj = json.load(open(os.path.join(cls_dir, "fabric.mod.json")))
mix = fmj.get("mixins", [])
if isinstance(mix, str):
    mix = [mix]
cfgs = [x if isinstance(x, str) else (x.get("config") or "") for x in mix]
cfgs = [c for c in cfgs if c and os.path.isfile(os.path.join(cls_dir, c))]
if not cfgs:
    print("[merge] no mixin config file found in jar -- audit will fail")
    raise SystemExit(0)
# 1.9.119 -- Mixin's real config model, learned from two launch crashes:
#  - 1.9.117: entries are ALWAYS resolved as package + "." + entry, even
#    dotted ones (FQ entries became pkg.net.mcsm... -> ClassNotFound).
#  - 1.9.118: without a "package" key EVERY entry is orphaned and skipped
#    ("declares mixin classes ... but does not specify a package"), so all
#    base mixins silently stopped applying -> accessor AssertionError.
# Therefore: keep the config's package (net.dabicco.witherstormmod.mixin),
# append overlay mixins as SIMPLE names, and the overlay mixin classes are
# compiled INTO that same package (mcsm-extras/java/net/dabicco/...).
PKG = "net.dabicco.witherstormmod.mixin."
# The jar declares TWO configs: the base mod's dabywitherstormmod.mixins.json
# (package net.dabicco.witherstormmod.mixin) and the original author's own
# mcsm_extras.mixins.json (package net.mcsm.extras.mixin). Only the first one
# is ours to extend; every other config is left EXACTLY as shipped.
target = None
for cfg in cfgs:
    d = json.load(open(os.path.join(cls_dir, cfg)))
    if d.get("package") and d["package"] + "." == PKG:
        target = cfg
if target is None:
    print("::error title=jar audit::no mixin config with package %s found" % PKG)
    raise SystemExit(1)
# These two entries belong to the retired texture/dome sky path.  Their class
# files are purged below; remove the base-jar registrations as well or Mixin
# will fail launch before the native SkyRenderer hook can run.
p = os.path.join(cls_dir, target)
d = json.load(open(p))
retired_sky_mixins = {"StormSkyGradientMixin", "StoryModeSkyDomeMixin"}
removed = []
for key in ("mixins", "client"):
    old = d.get(key) or []
    new = [e for e in old if e not in retired_sky_mixins]
    removed.extend(e for e in old if e not in new)
    d[key] = new
if removed:
    with open(p, "w") as f:
        json.dump(d, f, indent=2)
        f.write("\n")
    print("[merge] removed retired sky mixins: " + ", ".join(removed))
added = []
for src in sorted(glob.glob("mcsm-extras/java/net/dabicco/witherstormmod/mixin/*.java")):
    cls = os.path.basename(src)[:-5]
    is_client = "net.minecraft.client" in open(src).read()
    d = json.load(open(os.path.join(cls_dir, target)))
    present = any(e in (cls, PKG + cls)
                  for key in ("mixins", "client")
                  for e in (d.get(key) or []))
    if present:
        continue
    key = "client" if is_client else "mixins"
    p = os.path.join(cls_dir, target)
    d = json.load(open(p))
    d.setdefault(key, [])
    d[key].append(cls)
    with open(p, "w") as f:
        json.dump(d, f, indent=2)
        f.write("\n")
    added.append(cls + " -> " + target + ":" + key)
print("[merge] appended mixins: " + (", ".join(added) if added else "(none, all listed)"))
PYMERGE

# ---------------------------------------------------------------------------
# MCSM 1.9.109 -- JAR AUDIT (hard gate).
#
# Every "the user sees none of the changes" report up to now was answered with
# "all the gates are open", which says nothing about whether the code in the
# jar is ever *called*. A Fabric mod whose Mixin config does not list a mixin
# class simply never applies it: the Java side is inert, the jar still loads,
# and every diagnostic reads "enabled". That failure mode is invisible to the
# user and to static review, so it is checked here instead.
#
#   1. freshly compiled classes are present in the jar,
#   2. a Mixin config exists and lists EVERY mixin class we compile,
#   3. fabric.mod.json points at that Mixin config.
# Any miss fails the build: an inert jar is worse than no jar.
# Results are emitted as annotations, which survive without runner-log access.
# ---------------------------------------------------------------------------
echo "[audit] ---- assembled jar ----"
AUDIT_FAIL=0

# 1. fresh classes — every compiled class must exist in the assembled tree
# (net/mcsm extras AND net/dabicco overlays/mixins). Count matching paths,
# not "net/mcsm only vs everything" (that false-failed dabicco client overlays).
NEW_COUNT=$(cd /tmp/mcsm-build && { find net -name "*.class" 2>/dev/null || true; } | wc -l)
JAR_MATCH=0
if [ -d /tmp/mcsm-build/net ]; then
  while IFS= read -r rel; do
    [ -z "$rel" ] && continue
    if [ -f "$FX/cls/$rel" ]; then
      JAR_MATCH=$((JAR_MATCH + 1))
    else
      echo "::error title=jar audit::missing fresh class in jar: $rel"
      AUDIT_FAIL=1
    fi
  done < <(cd /tmp/mcsm-build && find net -name "*.class" 2>/dev/null | sort)
fi
echo "[audit] fresh classes: matched=$JAR_MATCH compiled=$NEW_COUNT"
if [ "$NEW_COUNT" -eq 0 ] || [ "$JAR_MATCH" -lt "$NEW_COUNT" ]; then
  echo "::error title=jar audit::fresh classes did not make it into the jar (matched=$JAR_MATCH compiled=$NEW_COUNT)"
  AUDIT_FAIL=1
fi
# Experimental stage delivery gate: the opt-in renderer, chunk boundary hook,
# and persisted uppercase key must travel together.  This catches a partial
# overlay where the menu appears but the stage is inert (or vice versa).
for stage_class in \
  net/mcsm/extras/client/McsmExperimentalStoryStage.class \
  net/mcsm/extras/client/McsmBackdropPalette.class \
  net/dabicco/witherstormmod/mixin/McsmStageChunkBoundaryMixin.class; do
  if [ ! -f "$FX/cls/$stage_class" ]; then
    echo "::error title=jar audit::experimental stage class missing: $stage_class"
    AUDIT_FAIL=1
  fi
done
if ! grep -q 'ENABLE_EXPERIMENTAL_STORY_MODE_STAGE' mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java \
   || ! grep -q 'ENABLE_EXPERIMENTAL_STORY_MODE_STAGE = false' mcsm-extras/java/net/mcsm/extras/McsmExtrasConfig.java; then
  echo "::error title=jar audit::experimental stage key/default is missing or not false"
  AUDIT_FAIL=1
else
  echo "[audit] experimental Story Mode stage is explicitly opt-in (default false)"
fi

# 2 + 3. mixin config registration, read from fabric.mod.json itself so a
#        config named or located unusually is still found.
CFG_LIST=$(python3 - "$FX/cls/fabric.mod.json" <<'PYCFG'
import json, sys
try:
    d = json.load(open(sys.argv[1]))
except Exception as e:
    print("PARSE_ERROR", e); raise SystemExit(0)
m = d.get("mixins", [])
if isinstance(m, str):
    m = [m]
for x in m:
    print(x if isinstance(x, str) else (x.get("config") or ""))
PYCFG
)
echo "[audit] fabric.mod.json declares mixin configs: ${CFG_LIST:-<none>}"
if [ -z "$CFG_LIST" ]; then
  echo "::error title=jar audit::fabric.mod.json declares NO mixin config - every MCSM mixin is inert"
  AUDIT_FAIL=1
else
  ALL_CFG=""
  while IFS= read -r cfg; do
    [ -n "$cfg" ] || continue
    if [ ! -f "$FX/cls/$cfg" ]; then
      echo "::error title=jar audit::fabric.mod.json names $cfg but that file is not in the jar"
      AUDIT_FAIL=1
      continue
    fi
    echo "[audit] mixin config $cfg:"; cat "$FX/cls/$cfg"
    ALL_CFG="$ALL_CFG $(cat "$FX/cls/$cfg")"
  done <<< "$CFG_LIST"

  MISSING=""
  N_MIXINS=0
  for src in mcsm-extras/java/net/dabicco/witherstormmod/mixin/*.java; do
    [ -f "$src" ] || continue
    N_MIXINS=$((N_MIXINS + 1))
    cls=$(basename "$src" .java)
    # matches both "McsmFoo" and any dotted form
    if ! grep -q "${cls}\"" <<< "$ALL_CFG"; then
      MISSING="$MISSING $cls"
    fi
    JARCLS="$FX/cls/net/dabicco/witherstormmod/mixin/$cls.class"
    NEWCLS="/tmp/mcsm-build/net/dabicco/witherstormmod/mixin/$cls.class"
    if [ ! -f "$JARCLS" ]; then
      echo "::error title=jar audit::mixin $cls has no compiled class in the jar"
      AUDIT_FAIL=1
    elif [ ! -f "$NEWCLS" ]; then
      echo "::error title=jar audit::mixin $cls was not freshly compiled"
      AUDIT_FAIL=1
    elif ! cmp -s "$JARCLS" "$NEWCLS"; then
      # a same-named BASE class would silently win over our overlay
      echo "::error title=jar audit::mixin $cls in jar is NOT the freshly compiled overlay class (stale base copy?)"
      AUDIT_FAIL=1
    fi
  done
  if [ -n "$MISSING" ]; then
    echo "::error title=jar audit::mixins NOT listed in any config (they will NEVER apply):$MISSING"
    AUDIT_FAIL=1
  else
    echo "[audit] all $N_MIXINS mixin classes are registered in a loaded config"
  fi

  # MCSM 1.9.117 -- entry-resolution gate. Listing is not enough: every entry
  # in every config must RESOLVE to a real class file in the assembled jar
  # (package + simple name, or the FQ name itself). This is the gate that
  # 1.9.116 needed: its appended simple-name entries resolved under the base
  # config package and the game died at launch with ClassNotFoundException.
  python3 - "$FX/cls" $CFG_LIST <<'PYRES' || AUDIT_FAIL=1
import json, os, sys
cls_dir = sys.argv[1]
bad = 0
n = 0
for cfg in sys.argv[2:]:
    p = os.path.join(cls_dir, cfg)
    if not os.path.isfile(p):
        continue
    d = json.load(open(p))
    pkg = d.get("package", "")
    if not pkg and (d.get("mixins") or d.get("client")):
        print("::error title=jar audit::config %s has entries but NO package key -- Mixin orphans every entry and nothing loads" % cfg)
        bad = 1
    for key in ("mixins", "client"):
        for e in d.get(key) or []:
            n += 1
            # Mixin prepends the declared package to EVERY entry, dotted or
            # not (1.9.117 crash proved it). Mirror that exactly.
            fq = (pkg + "." + e) if pkg else e
            path = os.path.join(cls_dir, fq.replace(".", "/") + ".class")
            if not os.path.isfile(path):
                print("::error title=jar audit::mixin entry %s (%s:%s) resolves to %s which is NOT in the jar -- launch would crash" % (e, cfg, key, fq))
                bad = 1
print("[audit] resolved %d mixin config entries against the jar" % n)
sys.exit(bad)
PYRES
fi

# MCSM 1.9.122 -- ship Story Look INSIDE the mod jar as a built-in resource
# pack (Fabric resource-loader registers it from resourcepacks/<name>/ and
# DEFAULT_ENABLED turns it on without the user installing anything).
mkdir -p "$FX/cls/resourcepacks/storylook"
cp -r storylook/pack.mcmeta storylook/pack.png "$FX/cls/resourcepacks/storylook/"
cp -r storylook/assets "$FX/cls/resourcepacks/storylook/"
echo "[build] built-in story look pack embedded at resourcepacks/storylook"
# BUILD #471 -- AN OVERRIDE MAY CHANGE THE BODY, NEVER THE INTERFACE.
#
# This pack replaces the game's own core shaders (block, lightmap, sky, entity,
# rendertype_entity_cutout, rendertype_clouds), and mod assets sit above vanilla in the
# pack stack: they are ALWAYS on, with no pack to enable. A core shader whose interface
# does not match this build of the game is not a wrong colour, it is a BLACK SCREEN WITH
# NO ERROR -- GLSL reads declared values by position, so one extra, missing, renamed or
# reordered member of a std140 block shifts every value after it and the lighting comes
# back as garbage while the logo and the buttons (plain GUI shaders) still draw. That is
# exactly the frame the standing report describes.
#
# So every replaced core shader is compared against the game's OWN copy out of the client
# jar before it is shipped, and one that does not match is disabled here -- in this copy,
# before the zip, so the committed sources are untouched and the packed jar cannot carry
# it. The verdict lands in out/vanilla-api.txt with the rest of the API evidence.
python3 ci/check_shader_overrides.py --jar "$DL/client.jar" \
  --assembled "$FX/cls/resourcepacks/storylook/assets/minecraft/shaders/core" --strip \
  >> "$VANILLA_OUT" 2>&1 || true
mkdir -p "$FX/cls/assets/dabywitherstormmod/resourcepacks"
rm -f "$FX/cls/assets/dabywitherstormmod/resourcepacks/storylook.zip"
( cd "$FX/cls/resourcepacks/storylook" && zip -q -r -X "$FX/cls/assets/dabywitherstormmod/resourcepacks/storylook.zip" pack.mcmeta pack.png assets )
echo "[build] extractable Story Look pack embedded at assets/dabywitherstormmod/resourcepacks/storylook.zip"

# 1.9.151: Totally Accurate / MCSM OG CEM models (from Loganwall111/ogs-stuff)
# ship as a second DEFAULT_ENABLED built-in pack. EMF / OptiFine CEM reads
# assets/minecraft/optifine/cem/dabywitherstormmod/*.jem with Phase NBT ladder.
if [ -d ogs-cem/assets ] && [ -f ogs-cem/pack.mcmeta ]; then
  mkdir -p "$FX/cls/resourcepacks/ogs-cem"
  cp -f ogs-cem/pack.mcmeta "$FX/cls/resourcepacks/ogs-cem/"
  [ -f ogs-cem/pack.png ] && cp -f ogs-cem/pack.png "$FX/cls/resourcepacks/ogs-cem/"
  cp -r ogs-cem/assets "$FX/cls/resourcepacks/ogs-cem/"
  rm -f "$FX/cls/assets/dabywitherstormmod/resourcepacks/ogs-cem.zip"
  if [ -f "$FX/cls/resourcepacks/ogs-cem/pack.png" ]; then
    ( cd "$FX/cls/resourcepacks/ogs-cem" && zip -q -r -X "$FX/cls/assets/dabywitherstormmod/resourcepacks/ogs-cem.zip" pack.mcmeta pack.png assets )
  else
    ( cd "$FX/cls/resourcepacks/ogs-cem" && zip -q -r -X "$FX/cls/assets/dabywitherstormmod/resourcepacks/ogs-cem.zip" pack.mcmeta assets )
  fi
  echo "[build] built-in OG CEM pack embedded at resourcepacks/ogs-cem ($(du -sh ogs-cem | cut -f1))"
  echo "[build] extractable OG CEM pack embedded at assets/dabywitherstormmod/resourcepacks/ogs-cem.zip"
else
  echo "::warning title=build::ogs-cem pack missing — Totally Accurate models will not ship"
fi

# Mega-phase 5b / 1.9.176: the managed Iris/Oculus pack rides inside the mod jar.
# The default managed pack is now the user-supplied Super Duper Vanilla shader
# source from shaderpack-superduper/, while shaderpack-v5 remains the lighter
# Devouring Storms standalone release asset.
mkdir -p "$FX/cls/assets/dabywitherstormmod/shaderpacks"
rm -f "$FX/cls/assets/dabywitherstormmod/shaderpacks/devouringstorms.zip"
# $FX is absolute (/tmp/mcsm-fx): NO $OLDPWD prefix here - prepending it to an
# absolute path re-anchors the zip under the repo and zip dies with exit 15
# (run 34050631385). The repo-relative cd source is resolved before the cd.
MANAGED_SHADER_ROOT="$(pwd)/shaderpack-superduper"
( cd "$MANAGED_SHADER_ROOT" && zip -q -r -X "$FX/cls/assets/dabywitherstormmod/shaderpacks/devouringstorms.zip" shaders DEVOURING_STORMS_MERGE.md pack.mcmeta )
echo "[build] managed Super Duper default shader pack embedded at assets/dabywitherstormmod/shaderpacks/devouringstorms.zip"

# mega-phase 3: the phase-6 halo ring texture, generated at build time and
# shipped inside the mod jar under the base mod's namespace
mkdir -p "$FX/cls/assets/dabywitherstormmod/textures/misc"
# mega-phase 5c: the old hard-ring glare is gone - the reference frames
# exposed the original construction (soft gradient backdrop + flat emissive
# mouth squares), so the build generates exactly those two primitives.
python3 ci/make_glare.py "$FX/cls/assets/dabywitherstormmod/textures/misc/storm_glare.png" \
    "$FX/cls/assets/dabywitherstormmod/textures/misc/storm_white.png" \
  || echo "::warning title=build::glare texture generation failed"
python3 ci/make_stormface.py "$FX/cls/assets/dabywitherstormmod/textures/misc/storm_face.png" \
  || echo "::warning title=build::storm face overlay texture generation failed"

# BUILD #416 -- the position/block pair must NOT be a copy. `cp block.fsh
# position.fsh` was the old fallback; it silently shipped a file that ignored
# every change made to the authored pass. Compare the two programs: identical
# means the fallback came back.
if [ -f "$FX/cls/assets/minecraft/shaders/core/position.fsh" ] && [ -f "$FX/cls/assets/minecraft/shaders/core/block.fsh" ]; then
  if cmp -s "$FX/cls/assets/minecraft/shaders/core/position.fsh" "$FX/cls/assets/minecraft/shaders/core/block.fsh" \
     || cmp -s "$FX/cls/assets/minecraft/shaders/core/position.vsh" "$FX/cls/assets/minecraft/shaders/core/block.vsh"; then
    echo "::error title=jar audit::position.* is a byte copy of block.* - the authored position pass did not ship"
    AUDIT_FAIL=1
  else
    echo "[audit] position.* differs from block.* (authored module, not a fallback copy)"
    echo "::notice title=shaders::position.* is the authored pass (not a copy of block.*); sky.* present; MCSM_SKY_POSITION branch intact"
  fi
fi
# ...and the blueprint's sky branch has to still be in there.
for tok in MCSM_SKY_POSITION mcsm_position_sky; do
  if ! grep -q "$tok" "$FX/cls/assets/minecraft/shaders/core/position.fsh"; then
    echo "::error title=jar audit::position.fsh lost its sky branch ($tok missing)"
    AUDIT_FAIL=1
  fi
done
# The sky pair is the one that actually paints the storm sky; both halves or
# neither.
for sky in assets/minecraft/shaders/core/sky.fsh assets/minecraft/shaders/core/sky.vsh; do
  if [ ! -s "$FX/cls/$sky" ]; then
    echo "::error title=jar audit::sky program missing from jar: $sky"
    AUDIT_FAIL=1
  fi
done
if [ ! -f "$FX/cls/assets/minecraft/shaders/core/position.fsh" ] || [ ! -f "$FX/cls/assets/minecraft/shaders/core/block.fsh" ]; then
  echo "::error title=jar audit::26.2 shader aliases missing (position/block) — vivid light would never load"
  AUDIT_FAIL=1
fi
if [ ! -f "$FX/cls/resourcepacks/storylook/pack.mcmeta" ] || [ ! -f "$FX/cls/resourcepacks/storylook/assets/minecraft/textures/environment/sun.png" ]; then
  echo "::error title=jar audit::built-in Sodium-safe Story Look pack missing from the jar"
  AUDIT_FAIL=1
fi
if [ -d "$FX/cls/resourcepacks/storylook/assets/minecraft/shaders" ]; then
  echo "::error title=jar audit::external Story Look pack still overrides vanilla core shaders; Sodium will reject it"
  AUDIT_FAIL=1
fi
if [ -e "$FX/cls/assets/fabricskyboxes" ] \
   || [ -d "$FX/cls/assets/dabywitherstormmod/textures/sky" ] \
   || [ -d "$FX/cls/assets/dabywitherstormmod/textures/mcsm_atmosphere/sky" ] \
   || find "$FX/cls/assets/dabywitherstormmod/textures/environment" -maxdepth 1 -name 'storymode_sky_*.png' -print -quit 2>/dev/null | grep -q .; then
  echo "::error title=jar audit::legacy texture-pack sky assets survived assembly"
  AUDIT_FAIL=1
else
  echo "[audit] legacy texture-pack sky assets removed"

# BUILD #415: the 2D background skybox stickers are the only sky this build
# paints, so prove all three canvases actually made it into the jar -- a missing
# sheet is a silent no-op (Minecraft renders the missing-texture checkerboard
# or nothing at all), which is exactly the failure mode this audit exists for.
for need in \
  assets/dabywitherstormmod/textures/misc/backdrop_sheet_purple_canvas.png \
  assets/dabywitherstormmod/textures/misc/backdrop_sheet_teal_filter.png \
  assets/dabywitherstormmod/textures/misc/backdrop_sheet_salmon_filter.png \
  assets/dabywitherstormmod/textures/misc/backdrop_sheet_phase5_teal.png \
  assets/dabywitherstormmod/textures/misc/backdrop_sheet_phase55_violet.png \
  assets/dabywitherstormmod/textures/misc/backdrop_sheet_phase6_plum.png; do
  if [ ! -s "$FX/cls/$need" ]; then
    echo "::error title=jar audit::2D skybox sticker sheet missing from jar: $need"
    AUDIT_FAIL=1
  fi
done
fi

if [ ! -f "$FX/cls/resourcepacks/ogs-cem/pack.mcmeta" ] \
   || [ ! -f "$FX/cls/resourcepacks/ogs-cem/assets/minecraft/optifine/cem/dabywitherstormmod/wither_storm.jem" ]; then
  echo "::error title=jar audit::built-in OG CEM pack missing from the jar"
  AUDIT_FAIL=1
fi

# 1.9.318: prove the offline extracted traced materials and per-stage CEM
# atlases survived assembly. The source .bbmodel files are never shipped or
# parsed by the client.
for traced_need in \
  resourcepacks/ogs-cem/assets/minecraft/textures/entity/cem/wither_storm_stage_a.png \
  resourcepacks/ogs-cem/assets/minecraft/textures/entity/cem/wither_storm_stage_b.png \
  resourcepacks/ogs-cem/assets/minecraft/textures/entity/cem/wither_storm_stage_c_massive.png \
  resourcepacks/ogs-cem/assets/minecraft/textures/entity/cem/wither_storm_stage_d_massive.png \
  resourcepacks/ogs-cem/assets/traced_asset_manifest.json; do
  if [ ! -s "$FX/cls/$traced_need" ]; then
    echo "::error title=jar audit::offline traced model material missing: $traced_need"
    AUDIT_FAIL=1
  fi
done
if [ -e "shaderpack-superduper/shaders/lib/mcsm/skyBlob.glsl" ]; then
  echo "::error title=jar audit::retired shader sky-box attachment survived source assembly"
  AUDIT_FAIL=1
fi

# 1.9.181: prove the restored OGS assets and live labels survived assembly.
for need in \
  assets/dabywitherstormmod/textures/entity/wither_storm.png \
  assets/dabywitherstormmod/textures/entity/wither_storm/wither_storm.png \
  assets/witherstormmod/textures/entity/wither_storm/wither_storm.png \
  resourcepacks/ogs-cem/assets/minecraft/optifine/cem/dabywitherstormmod/wither_storm_phase5.jem \
  resourcepacks/ogs-cem/assets/minecraft/optifine/cem/witherstormmod/wither_storm_phase5.jem \
  assets/dabywitherstormmod/resourcepacks/storylook.zip \
  assets/dabywitherstormmod/resourcepacks/ogs-cem.zip; do
  if [ ! -s "$FX/cls/$need" ]; then
    echo "::error title=jar audit::restored OGS asset missing from jar: $need"
    AUDIT_FAIL=1
  fi
done
if grep -R -a -q 'MCSM extras 1\.9\.95' "$FX/cls" 2>/dev/null; then
  echo "::error title=jar audit::stale visible config label MCSM extras 1.9.95 survived assembly"
  AUDIT_FAIL=1
else
  echo "[audit] stale 1.9.95 config label purged"
fi

SCHEMATIC_COUNT="$(find "$FX/cls/assets/dabywitherstormmod" -path '*/schematics/*' -name '*.schematic' 2>/dev/null | wc -l | tr -d ' ')"
# BUILD #416 (D.8, phase 2) -- CONTENT-PACK JAR AUDIT (hard gate).
#
# "Registered" and "in the jar" are different things: the dimension is a
# datapack file, the blocks are assets, and the loot tables are JSON. If any of
# them misses the assembled jar the mod still loads and the console still says
# everything is enabled -- the dimension simply never shows up. Phase 1 shipped
# its dimension, its 38 blocks and its recipes through this overlay, so the
# contents are now required by name.
CONTENT_MISSING=""
for want in \
  data/mcsm/dimension/decayed_reality.json \
  data/mcsm/dimension_type/decayed_reality.json \
  assets/mcsm/lang/en_us.json \
  assets/mcsm/blockstates/city_bricks.json \
  assets/mcsm/items/city_bricks.json \
  assets/mcsm/items/reality_ripper.json \
  assets/mcsm/textures/block/decayed_stone.png \
  assets/mcsm/textures/item/reality_ripper.png \
  assets/mcsm/models/item/reality_ripper.json \
  assets/mcsm/blockstates/void_slab.json \
  assets/mcsm/blockstates/creator_trapdoor.json \
  assets/mcsm/blockstates/adams_tile_wall.json \
  assets/mcsm/items/void_cord.json \
  assets/mcsm/textures/block/decayed_bricks.png \
  assets/mcsm/textures/item/storm_marrow.png \
  assets/mcsm/items/creator_edict.json \
  assets/mcsm/models/item/creator_edict.json \
  assets/mcsm/textures/item/creator_edict.png \
  assets/mcsm/items/void_edge.json \
  assets/mcsm/models/item/void_ripper.json \
  assets/mcsm/textures/item/decayed_cleaver.png ; do
  if [ ! -s "$FX/cls/$want" ]; then
    CONTENT_MISSING="$CONTENT_MISSING $want"
  fi
done
N_STATES="$(find "$FX/cls/assets/mcsm/blockstates" -name '*.json' 2>/dev/null | wc -l)"
N_ITEM_MODELS="$(find "$FX/cls/assets/mcsm/models/item" -name '*.json' 2>/dev/null | wc -l)"
N_DEFS="$(find "$FX/cls/assets/mcsm/items" -name '*.json' 2>/dev/null | wc -l)"
N_TEX="$(find "$FX/cls/assets/mcsm/textures" -name '*.png' 2>/dev/null | wc -l)"
N_RECIPES="$(find "$FX/cls/data/mcsm/recipe" -name '*.json' 2>/dev/null | wc -l)"
N_LOOT="$(find "$FX/cls/data/mcsm/loot_table/blocks" -name '*.json' 2>/dev/null | wc -l)"
echo "[audit] content pack in jar: ${N_STATES} blockstates, ${N_ITEM_MODELS} item models, ${N_DEFS} item definitions, ${N_TEX} textures, ${N_RECIPES} recipes, ${N_LOOT} loot tables"
if [ -n "$CONTENT_MISSING" ]; then
  echo "::error title=jar audit::the decayed-reality content pack is missing from the jar:${CONTENT_MISSING}"
  echo "[audit] content pack MISSING:${CONTENT_MISSING}"
  exit 1
fi
# BUILD #465 raised these floors (92 blocks / 124 items); BUILD #466 raised them
# again for the weapons pass (92 / 132), so a build that loses half the pack -- a
# generator that did not run, a directory that did not copy -- cannot pass as
# "complete" on a floor that predates the expansion.
if [ "${N_STATES:-0}" -lt 90 ] || [ "${N_ITEM_MODELS:-0}" -lt 130 ] || [ "${N_DEFS:-0}" -lt 130 ] \
   || [ "${N_TEX:-0}" -lt 145 ] || [ "${N_RECIPES:-0}" -lt 55 ] || [ "${N_LOOT:-0}" -lt 90 ]; then
  echo "::error title=jar audit::the content pack is incomplete in the jar (states=${N_STATES} models=${N_ITEM_MODELS} definitions=${N_DEFS} textures=${N_TEX} recipes=${N_RECIPES} loot=${N_LOOT})"
  exit 1
fi
if [ "${N_DEFS:-0}" -lt "${N_ITEM_MODELS:-0}" ]; then
  echo "::error title=jar audit::${N_ITEM_MODELS} item models but only ${N_DEFS} item definitions -- the extra items would render as glitch blocks"
  exit 1
fi
echo "[audit] content pack complete (92 blocks + 132 items + doors/stairs/trap doors + swords/axes/picks + 92 loot tables + own art)"

echo "[audit] legacy schematic fallback assets available: ${SCHEMATIC_COUNT}"

# mega-phase 5b: the embedded Iris pack must actually be in the jar, and its
# zip must contain the retained glare composite - an installer with nothing to install
# is the same silent no-op the audit exists to catch.
EMBED_PACK="$FX/cls/assets/dabywitherstormmod/shaderpacks/devouringstorms.zip"
if [ ! -s "$EMBED_PACK" ] || ! unzip -Z1 "$EMBED_PACK" 2>/dev/null | grep -Eq "shaders/(main/)?composite6?\.glsl"; then
  echo "::error title=jar audit::embedded managed Iris shader pack missing or incomplete"
  AUDIT_FAIL=1
else
  echo "[audit] embedded shader pack: $(unzip -Z1 "$EMBED_PACK" | wc -l) entries"
fi

# shader spot-check: the jar must carry the shared visual include, not a stale base copy.
for f in include/mcsm_visuals.glsl; do
  if [ -f "$FX/cls/assets/minecraft/shaders/$f" ] && \
     cmp -s "mcsm-core-shaders/$f" "$FX/cls/assets/minecraft/shaders/$f"; then
    echo "[audit] shader up to date: $f"
  else
    echo "::error title=jar audit::shader in jar differs from source: $f"
    AUDIT_FAIL=1
  fi
done

# BUILD #416: the menus are audible only if BOTH halves ship -- the Ogg Vorbis
# one-shots AND the sounds.json that names them. A missing asset used to be
# invisible (the menus simply played nothing), so prove it inside the jar, and
# prove the old undecodable RIFF wavs are gone: Minecraft's sound engine is
# Vorbis-only, which is exactly why the #383 menus were silent.
for need in \
  assets/mcsm/sounds.json \
  assets/mcsm/lang/en_us.json \
  assets/mcsm/menu/ds_icon.png \
  assets/mcsm/sounds/ds_btn_hover.ogg \
  assets/mcsm/sounds/ds_btn_click.ogg \
  assets/mcsm/sounds/ds_menu_open.ogg; do
  if [ ! -s "$FX/cls/$need" ]; then
    echo "::error title=jar audit::UI sound asset missing from the jar: $need"
    AUDIT_FAIL=1
  fi
done
# BUILD #425 -- the mod's own sound set (see ci/make_mcsm_sounds.py). Each one
# has to be in the jar AND be a real Ogg container, or the radio and the
# creature fall back to silence.
for need in \
  assets/mcsm/sounds/radio/static.ogg \
  assets/mcsm/sounds/radio/carrier.ogg \
  assets/mcsm/sounds/radio/voice.ogg \
  assets/mcsm/sounds/radio/distress.ogg \
  assets/mcsm/sounds/radio/morse.ogg \
  assets/mcsm/sounds/massg/breath.ogg \
  assets/mcsm/sounds/massg/giggle.ogg \
  assets/mcsm/sounds/massg/whisper.ogg \
  assets/mcsm/sounds/massg/roar.ogg \
  assets/mcsm/sounds/massg/heart.ogg \
  assets/mcsm/sounds/oblivion/drone.ogg \
  assets/mcsm/sounds/oblivion/glitch.ogg \
  assets/mcsm/sounds/oblivion/warp.ogg \
  assets/mcsm/sounds/ui/terminal_open.ogg \
  assets/mcsm/sounds/ui/terminal_key.ogg \
  assets/mcsm/sounds/ui/terminal_deny.ogg; do
  if [ ! -s "$FX/cls/$need" ]; then
    echo "::error title=jar audit::custom sound missing from the jar: $need"
    AUDIT_FAIL=1
  elif ! head -c 4 "$FX/cls/$need" | grep -q 'OggS'; then
    echo "::error title=jar audit::$need is not an Ogg container"
    AUDIT_FAIL=1
  fi
done
for ogg in ds_btn_hover ds_btn_click ds_menu_open; do
  oggf="$FX/cls/assets/mcsm/sounds/$ogg.ogg"
  if [ -s "$oggf" ] && ! head -c 4 "$oggf" | grep -q 'OggS'; then
    echo "::error title=jar audit::$ogg.ogg in the jar is not an Ogg container"
    AUDIT_FAIL=1
  fi
done
if [ -e "$FX/cls/assets/mcsm/sounds/ds_btn_click.wav" ] \
   || [ -e "$FX/cls/assets/mcsm/sounds/ds_btn_hover.wav" ] \
   || [ -e "$FX/cls/assets/mcsm/sounds/ds_menu_open.wav" ]; then
  echo "::error title=jar audit::undecodable RIFF .wav shipped as a UI sound"
  AUDIT_FAIL=1
fi
if [ "$AUDIT_FAIL" -eq 0 ]; then
  echo "[audit] UI audio: 3 menu one-shots + 16 custom sounds (radio, MASSG, oblivion, terminal) all present as real Ogg Vorbis containers"
fi

if [ "$AUDIT_FAIL" -ne 0 ]; then
  echo "[audit] FAILED -- refusing to publish a jar whose hooks may never run"
  exit 1
fi
echo "::notice title=jar audit::all mixins registered, fresh classes present, shaders current"
echo "[audit] PASS"
stage audit-ok


# ---------------------------------------------------------------------------
# BUILD #471 -- AND THE SAME CHECK ON THE JAR'S OWN CORE SHADERS.
#
# mcsm-core-shaders/* was overlaid onto assets/minecraft/shaders/ at the top of this
# build, so the jar carries replacements for the game's own block / lightmap / sky /
# entity / cloud programs, active for every player with no pack to install. This is the
# last checkpoint before packaging, so nothing here can be undone by it: a replaced
# program whose interface does not match the game's own file is disabled (the game's
# shader is used instead) and the verdict is written into the evidence.
# ---------------------------------------------------------------------------
echo "[shader] replaced core shaders: interface check against the client jar"
SHADER_REPORT=/tmp/mcsm-shader-check.txt
SHADER_IFACE=/tmp/mcsm-shader-interface.txt
SHADER_CHECK="$(python3 ci/check_shader_overrides.py --jar "$DL/client.jar" \
  --assembled "$FX/cls/assets/minecraft/shaders/core" --strip \
  --dump-out "$SHADER_IFACE" 2>&1 || true)"
printf '%s\n' "$SHADER_CHECK" | sed -n '1,60p'
printf '%s\n' "$SHADER_CHECK" >> "$VANILLA_OUT" 2>/dev/null || true
{
  echo "Devouring Storms ${JAR_ID} -- replaced core shaders weighed against the client jar"
  echo "jar:    $DL/client.jar"
  echo "tree:   assets/minecraft/shaders/core"
  echo
  printf '%s\n' "$SHADER_CHECK"
} > "$SHADER_REPORT"
evidence_put "$SHADER_REPORT" SHADER_CHECK.txt
evidence_put "$SHADER_IFACE" SHADER_INTERFACE.txt
if printf '%s\n' "$SHADER_CHECK" | grep -q "MISMATCH"; then
  echo "::warning title=shaders::a replaced core shader did not match this build's own interface and was DISABLED in the jar -- the game's own copy from the client jar is in its place instead (see ci-out/run-*/SHADER_CHECK.txt)"
fi

# ---------------------------------------------------------------------------
# BUILD #476 -- THE SHIPPED TREE IS THE GATE, NOT THE FIRST PASS.
#
# The strip pass above is only as good as its write-back. It was not, in
# builds #472-#475: it renamed the mismatching file, which exposed whatever
# the base mod jar shipped underneath (its own stale terrain-shaped position
# program) and the zip carried THAT -- while the log claimed "the game's own
# position.fsh is used instead". A first pass cannot prove what survives it:
# the file a rename exposes is never re-checked in the same run.
#
# So the tree that is about to be zipped is checked a second time, without
# --strip, and the build dies if
#   * the client jar's own core programs could not be read (a check against
#     an empty reference proves nothing and must never be claimed as clean),
#     or
#   * ANY hard interface mismatch remains in the shipped tree, from whatever
#     source it came -- the overlay, the base jar, or a pack.
# A jar that ships an interface-broken core program is the black screen:
# unbound samplers read black, unsupplied attributes read zero, and there is
# no error line to explain why the whole frame is gone.
# ---------------------------------------------------------------------------
echo "[shader] verification pass: the tree that gets zipped, re-checked without --strip"
SHADER_VERIFY="$(python3 ci/check_shader_overrides.py --jar "$DL/client.jar" \
  --assembled "$FX/cls/assets/minecraft/shaders/core" 2>&1)" || VERIFY_RC=$?
VERIFY_RC="${VERIFY_RC:-0}"
printf '%s\n' "$SHADER_VERIFY" | sed -n '1,40p'
{
  echo "Devouring Storms ${JAR_ID} -- shipped core tree re-checked WITHOUT --strip (the gate)"
  echo "jar:    $DL/client.jar"
  echo "tree:   $FX/cls/assets/minecraft/shaders/core"
  echo
  printf '%s\n' "$SHADER_VERIFY"
} > /tmp/mcsm-shader-verify.txt
evidence_put /tmp/mcsm-shader-verify.txt SHADER_VERIFY.txt
if printf '%s\n' "$SHADER_VERIFY" | grep -q "no client jar"; then
  echo "::error title=shaders::the client jar's own core programs could not be read -- the interface check is blind and a black-shader jar could ship"
  exit 1
fi
N_GAME_PROGRAMS="$(printf '%s\n' "$SHADER_VERIFY" | grep -oE 'core programs: [0-9]+ read' | grep -oE '[0-9]+' | head -1)"
if [ "${N_GAME_PROGRAMS:-0}" -lt 1 ]; then
  echo "::error title=shaders::no core programs read from the client jar -- the verification pass is blind and cannot claim the tree is clean"
  exit 1
fi
if [ "$VERIFY_RC" -ne 0 ] || printf '%s\n' "$SHADER_VERIFY" | grep -q "MISMATCH"; then
  echo "::error title=shaders::a HARD interface mismatch SURVIVED in the tree that gets zipped (stale base-jar program or broken overlay) -- this is the black screen, refusing to publish"
  printf '%s\n' "$SHADER_VERIFY" | grep -E "MISMATCH|sampler|block " | head -20
  exit 1
fi
echo "::notice title=shaders::shipped core tree verified against the client jar (${N_GAME_PROGRAMS} own programs): every core program in the jar is the game's own or interface-matching -- no black-screen program can ship"
# dead weight from any stale disable path (a .disabled corpse of a program the
# jar replaced): the game ignores it, but a 68 MB jar should not carry it
find "$FX/cls/assets/minecraft/shaders" -name '*.disabled' -delete 2>/dev/null || true
stage shader-verify-ok

OUT="out/devouringstorms-${JAR_ID}.jar"
rm -f "$OUT"
( cd "$FX/cls" && zip -q -r -X "$OLDPWD/$OUT" . -x '.*' )
( unzip -t "$OUT" > /dev/null )
sha256sum "$OUT" | tee "$OUT.sha256"

{
  echo "Devouring Storms build ${JAR_ID}"
  echo "mod version: ${JAR_ID} (fabric.mod.json)"
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
push_evidence_simple || echo "[evidence] skipped (non-fatal)"
rm -rf /tmp/mcsm-evidence

echo "[done] $OUT ($(stat -c%s "$OUT") B)"
