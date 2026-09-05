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
trap 'rc=$?; mkdir -p out 2>/dev/null; { echo "MCSM build FAILURE (run ${GITHUB_RUN_NUMBER:-local})"; echo "exit: $rc"; echo "line: $LINENO"; echo "cmd:  $BASH_COMMAND"; } > out/FAILURE.txt 2>/dev/null; cat out/FAILURE.txt 2>/dev/null; echo "::error title=MCSM build failed (exit $rc) line $LINENO::$BASH_COMMAND"' ERR

VER="${1:-$(cat VERSION | tr -d '[:space:]')}"
JAR_ID="${VER}-26.2-beta-ds"
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
fetch "https://libraries.minecraft.net/it/unimi/dsi/fastutil/8.5.18/fastutil-8.5.18.jar" fastutil.jar
fetch "https://libraries.minecraft.net/com/mojang/datafixerupper/10.0.21/datafixerupper-10.0.21.jar" dfu.jar
fetch "https://libraries.minecraft.net/org/joml/joml/1.10.8/joml-1.10.8.jar" joml.jar
# MCSM 1.9.101 -- brigadier: 26.2's Component implements com.mojang.brigadier.Message,
# so javac needs it on the classpath ("cannot access Message" without it).
fetch "https://libraries.minecraft.net/com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar" brigadier.jar

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
  CP2="$DL/client.jar:$STRIPPED:$DL/mixin.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar:$DL/brigadier.jar"
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
  # MCSM 1.9.101 -- the 1.9.101 javac errors (sendParticles overload,
  # "cannot access Message") live in the particle/level/chat API, which the
  # original dump never covered. Dump it, plus a package index of those
  # packages and every "message" entry in the client jar, so the sandbox can
  # see where 26.2 moved things.
  LEVEL_CLASSES="net.minecraft.world.level.Level net.minecraft.server.level.ServerLevel \
    net.minecraft.server.level.ServerPlayer net.minecraft.core.particles.ParticleType \
    net.minecraft.core.particles.DustParticleOptions net.minecraft.network.chat.Component"
  javap -public -classpath "$CP2" $LEVEL_CLASSES > ci/api/level.txt 2>&1 || true
  unzip -Z1 "$DL/client.jar" 2>/dev/null | grep -E '^net/minecraft/(world/level|server/level|core/particles|client/particles|network/chat)/' \
    | sort > ci/api/api-classes-index.txt || true
  unzip -Z1 "$DL/client.jar" 2>/dev/null | grep -iE 'message' > ci/api/message-locations.txt || true
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

# Story Look resource-pack shaders must validate as well.
for SL in storylook/assets/minecraft/shaders/core/*; do
  case "$SL" in
    *.fsh) SLE=frag ;;
    *.vsh) SLE=vert ;;
    *) continue ;;
  esac
  cp "$SL" "/tmp/storylook-check.$SLE"
  if ! ./glslcheck/bin/glslang "/tmp/storylook-check.$SLE" > /tmp/storylook-glsl.log 2>&1; then
    cat /tmp/storylook-glsl.log
    echo "[glsl] Story Look shader FAILED validation: $SL"
    exit 1
  fi
done
echo "[glsl] story look shaders validate"

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

DRIFT="$(grep -rn '"[^"]*1\.9\.[0-9]' --include='*.java' mcsm-extras/java \
         | grep -v 'BUILD_VERSION = ' || true)"
if [ -n "$DRIFT" ]; then
  echo "::error::hardcoded version literal(s) found -- the in-game build number would lie about which jar is loaded"
  echo "$DRIFT"
  echo "[version] use McsmExtrasConfig.BUILD_VERSION instead of a literal"
  exit 1
fi
echo "[version] drift gate OK (no hardcoded version literals)"

echo "[javac] mcsm-extras"
rm -rf /tmp/mcsm-build
mkdir -p /tmp/mcsm-build
CP="$DL/client.jar:$STRIPPED:$DL/mixin.jar:$DL/jspecify.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar:$DL/brigadier.jar"
# A javac failure is NOT survivable anymore: publishing a shaders-only jar is
# exactly how users can receive new-looking UI/shaders with old Java behavior.
# Stop hard and keep the full log in out/JAVAC_FAILED.txt.
JAVAC_LOG=/tmp/mcsm-javac.log
JAVAC_RC=0
javac -nowarn --release 25 -proc:none -cp "$CP" -d /tmp/mcsm-build \
     $(find mcsm-extras/java -name '*.java') > "$JAVAC_LOG" 2>&1 || JAVAC_RC=$?
N_CLASSES="$(find /tmp/mcsm-build -name '*.class' | wc -l)"
if [ "$JAVAC_RC" -eq 0 ]; then
  echo "[javac] OK: ${N_CLASSES} classes"
  rm -f out/JAVAC_FAILED.txt
else
  echo "::error::javac FAILED (exit ${JAVAC_RC}) — refusing to publish a shaders-only/old-Java jar. Full log: out/JAVAC_FAILED.txt"
  cp -f "$JAVAC_LOG" out/JAVAC_FAILED.txt
  tail -160 "$JAVAC_LOG" || true
  echo "[javac] FAILED (hard stop so users never receive another old-behaviour jar)"
  # MCSM 1.9.109 -- the sandbox can read check ANNOTATIONS (Checks API) but not
  # runner logs or artifacts, so the actual compiler errors have to travel as
  # annotations or the fix loop is blind. First 12 error lines, truncated.
  { grep -E "error:|symbol:|location:|required:|found:" "$JAVAC_LOG" 2>/dev/null || true; } | \
    head -12 | \
    while IFS= read -r line; do
      echo "::error title=javac::${line:0:400}"
    done || true
  exit "$JAVAC_RC"
fi

echo "[assemble] overlay onto base"
FX=/tmp/mcsm-fx
rm -rf "$FX" && mkdir -p "$FX/cls"
( cd "$FX/cls" && unzip -o -q "$BASE" )
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
sed -i "s/\"version\": \"[0-9.]*-26.2-beta[a-z-]*\"/\"version\": \"${JAR_ID}\"/" "$FX/cls/fabric.mod.json"
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
FQ = "net.mcsm.extras.mixin."
added = []
for src in sorted(glob.glob("mcsm-extras/java/net/mcsm/extras/mixin/*.java")):
    cls = os.path.basename(src)[:-5]
    is_client = "net.minecraft.client" in open(src).read()
    present = False
    for cfg in cfgs:
        d = json.load(open(os.path.join(cls_dir, cfg)))
        for key in ("mixins", "client"):
            for e in d.get(key) or []:
                if e == cls or e == FQ + cls:
                    present = True
    if present:
        continue
    key = "client" if is_client else "mixins"
    for cfg in cfgs:
        p = os.path.join(cls_dir, cfg)
        d = json.load(open(p))
        entries = d.get("mixins") or []
        style_fq = any("." in e for e in entries + (d.get("client") or []))
        d.setdefault(key, [])
        d[key].append(FQ + cls if style_fq else cls)
        with open(p, "w") as f:
            json.dump(d, f, indent=2)
            f.write("\n")
        added.append(cls + " -> " + cfg + ":" + key)
        break
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

# 1. fresh classes
NEW_COUNT=$(cd /tmp/mcsm-build && { find net -name "*.class" 2>/dev/null || true; } | wc -l)
JAR_COUNT=$(cd "$FX/cls" && { find net/mcsm -name "*.class" 2>/dev/null || true; } | wc -l)
echo "[audit] mcsm classes: jar=$JAR_COUNT freshly-compiled=$NEW_COUNT"
if [ "$NEW_COUNT" -eq 0 ] || [ "$JAR_COUNT" -lt "$NEW_COUNT" ]; then
  echo "::error title=jar audit::fresh classes did not make it into the jar (jar=$JAR_COUNT compiled=$NEW_COUNT)"
  AUDIT_FAIL=1
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
  for src in mcsm-extras/java/net/mcsm/extras/mixin/*.java; do
    [ -f "$src" ] || continue
    N_MIXINS=$((N_MIXINS + 1))
    cls=$(basename "$src" .java)
    # matches both "McsmFoo" and "net.mcsm.extras.mixin.McsmFoo"
    if ! grep -q "${cls}\"" <<< "$ALL_CFG"; then
      MISSING="$MISSING $cls"
    fi
    if [ ! -f "$FX/cls/net/mcsm/extras/mixin/$cls.class" ]; then
      echo "::error title=jar audit::mixin $cls has no compiled class in the jar"
      AUDIT_FAIL=1
    fi
  done
  if [ -n "$MISSING" ]; then
    echo "::error title=jar audit::mixins NOT listed in any config (they will NEVER apply):$MISSING"
    AUDIT_FAIL=1
  else
    echo "[audit] all $N_MIXINS mixin classes are registered in a loaded config"
  fi
fi

# shader spot-check: the jar must carry THIS source, not the base's
for f in core/sky.fsh include/mcsm_visuals.glsl; do
  if [ -f "$FX/cls/assets/minecraft/shaders/$f" ] && \
     cmp -s "mcsm-core-shaders/$f" "$FX/cls/assets/minecraft/shaders/$f"; then
    echo "[audit] shader up to date: $f"
  else
    echo "::error title=jar audit::shader in jar differs from source: $f"
    AUDIT_FAIL=1
  fi
done

if [ "$AUDIT_FAIL" -ne 0 ]; then
  echo "[audit] FAILED -- refusing to publish a jar whose hooks may never run"
  exit 1
fi
echo "::notice title=jar audit::all mixins registered, fresh classes present, shaders current"
echo "[audit] PASS"

OUT="out/devouringstorms-${JAR_ID}.jar"
rm -f "$OUT"
( cd "$FX/cls" && zip -q -r -X "$OLDPWD/$OUT" . -x '.*' )
( unzip -t "$OUT" > /dev/null )
sha256sum "$OUT" | tee "$OUT.sha256"

{
  echo "Devouring Storms build ${JAR_ID}"
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
