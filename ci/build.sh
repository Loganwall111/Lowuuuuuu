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
want = {"fabric-rendering-v1", "fabric-api-base", "fabric-object-builder-api-v1", "fabric-lifecycle-events-v1"}
out = []
for m in re.finditer(r'<dependency>\s*<groupId>([^<]+)</groupId>\s*<artifactId>([^<]+)</artifactId>\s*<version>([^<]+)</version>', pom):
    g, a, v = m.groups()
    if g == "net.fabricmc.fabric-api" and a in want:
        out.append(f"https://maven.fabricmc.net/{g.replace('.', '/')}/{a}/{v}/{a}-{v}.jar" + chr(9) + f"{a}.jar")
open(sys.argv[2], "w").write(chr(10).join(out) + (chr(10) if out else ""))
print(f"[deps] rendering modules wanted: {len(out)}")
PYMOD
  while IFS=$'\t' read -r url name; do
    [ -n "$url" ] || continue
    [ -s "$DL/fapi2/$name" ] || curl -fsSL --retry 3 --retry-delay 2 -o "$DL/fapi2/$name" "$url" \
      || echo "::warning title=deps::fabric module download failed: $name"
  done < "$DL/fapi2-list.txt"
fi
FAPI2_CP="$(find "$DL/fapi2" -name '*.jar' 2>/dev/null | tr '\n' ':')"
FAPI2_COUNT="$(find "$DL/fapi2" -name '*.jar' 2>/dev/null | wc -l)"
echo "[deps] fabric rendering modules on classpath: $FAPI2_COUNT"
if [ "$FAPI2_COUNT" -lt 4 ]; then
  echo "::error::fabric-api rendering modules missing from the compile classpath ($FAPI2_COUNT/4) -- the maven metadata fetch flaked; re-run the build"
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
  unzip -Z1 "$DL/client.jar" 2>/dev/null | grep -E '^net/minecraft/(world/level|server/level|core/particles|client/particles|network/chat)/' \
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

echo "[phase] WitherStormPhase plumbing gate"
PHASE_OUT="$(python3 ci/check_phase_uniform.py 2>&1)" || {
  echo "$PHASE_OUT"
  echo "::error title=build::WitherStormPhase plumbing broken — the storm phase is not reaching the shaders"
  exit 1
}
PHASE_LINE="$(printf '%s\n' "$PHASE_OUT" | grep -F '[phase]' | tail -1)"
echo "$PHASE_LINE"
echo "::notice title=phase::$PHASE_LINE"

# Story Look resource-pack shaders must validate as well.
for SL in storylook/assets/minecraft/shaders/core/*; do
  case "$SL" in
    *.fsh) SLE=frag ;;
    *.vsh) SLE=vert ;;
    *) continue ;;
  esac
  # inline the vanilla 26.2 moj_import includes before validating
  python3 ci/expand_storylook.py "$SL" "/tmp/storylook-check.$SLE"
  if ! ./glslcheck/bin/glslang "/tmp/storylook-check.$SLE" > /tmp/storylook-glsl.log 2>&1; then
    cat /tmp/storylook-glsl.log
    echo "[glsl] Story Look shader FAILED validation: $SL"
    exit 1
  fi
done
echo "[glsl] story look shaders validate"

# Mega-phase 5b: the embedded Iris shader pack must validate too - every
# program, in every [0 1] toggle combination, through the glslcheck shim.
if ! python3 ci/iris_tu.py shaderpack-v5/shaders; then
  echo "[glsl] shaderpack-v5 FAILED validation - not shipping a broken pack"
  exit 1
fi
echo "[glsl] shaderpack-v5 validates"

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
    net.minecraft.world.level.block.Block \
    net.minecraft.world.level.block.state.BlockBehaviour$Properties \
    net.minecraft.world.item.Item \
    net.minecraft.world.item.Item$Properties \
    net.minecraft.world.item.BlockItem \
    net.minecraft.world.item.SwordItem \
    net.minecraft.world.item.Tier \
    net.minecraft.world.item.Rarity \
    net.minecraft.world.level.block.SoundType \
    net.minecraft.world.entity.EntityType \
    net.minecraft.world.entity.EntityType$Builder \
    net.minecraft.world.entity.MobCategory \
    net.minecraft.world.entity.Mob \
    net.minecraft.world.entity.PathfinderMob \
    net.minecraft.world.entity.monster.Monster \
    net.minecraft.world.entity.LivingEntity \
    net.minecraft.world.entity.ai.goal.Goal \
    net.minecraft.world.entity.ai.goal.MeleeAttackGoal \
    net.minecraft.world.entity.ai.attributes.Attributes \
    net.minecraft.world.entity.player.Player ; do
    echo
    echo "===== ${CLS}"
    javap -p -classpath "$DL/client.jar" "$CLS" 2>&1 | sed -n '1,110p'
  done
} > "$VANILLA_OUT" 2>&1 || true
echo "[api] vanilla dump: $(wc -l < "$VANILLA_OUT" 2>/dev/null || echo 0) lines -> out/vanilla-api.txt"

echo "[javac] mcsm-extras"
rm -rf /tmp/mcsm-build
mkdir -p /tmp/mcsm-build
CP="$DL/client.jar:$STRIPPED:$DL/mixin.jar:$DL/jspecify.jar:$DL/fastutil.jar:$DL/dfu.jar:$DL/joml.jar:$DL/brigadier.jar:${FAPI2_CP}"
# A javac failure is NOT survivable anymore: publishing a shaders-only jar is
# exactly how users can receive new-looking UI/shaders with old Java behavior.
# Stop hard and keep the full log in out/JAVAC_FAILED.txt.
JAVAC_LOG=/tmp/mcsm-javac.log
JAVAC_RC=0
mkdir -p /tmp/mcsm-emptysrc
javac -nowarn -implicit:none -sourcepath /tmp/mcsm-emptysrc --release 25 -proc:none -cp "$CP" -d /tmp/mcsm-build \
     $(find mcsm-extras/java -name '*.java') > "$JAVAC_LOG" 2>&1 || JAVAC_RC=$?
N_CLASSES="$(find /tmp/mcsm-build -name '*.class' | wc -l)"
if [ "$JAVAC_RC" -eq 0 ]; then
  echo "[javac] OK: ${N_CLASSES} classes"
  rm -f out/JAVAC_FAILED.txt
  # BUILD #416 -- a clean javac exit is not the same as "the new pass is in the
  # jar". Some of this build's classes only ADD behaviour: if one of them silently
  # failed to be produced, the jar would still build and still load, and the effect
  # would simply never draw -- which is exactly how the death cinematic stayed
  # invisible for a dozen builds. So the classes that carry new behaviour are
  # required to exist in the compiled output. find() is used instead of a fixed
  # path so a layout change can never turn this into a false failure.
  for cls in McsmWhiteGlow McsmHaloSkyRenderer McsmStormPhase McsmPresenceFxPatch; do
    if ! find /tmp/mcsm-build -name "${cls}.class" -print -quit | grep -q .; then
      echo "::error title=build::compiled output is missing ${cls}.class -- new behaviour would silently not draw"
      exit 1
    fi
  done
  echo "[javac] new-behaviour classes present (white column, conic renderer, phase model, presence pass)"
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

echo "[assemble] overlay onto base"
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
cp -r jar-overrides/* "$FX/cls/"
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
  echo "[audit] UI audio: 3 Ogg Vorbis one-shots + sounds.json + lang in the jar"
fi

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
push_evidence_simple() {
  local AUTH REMOTE
  AUTH="$(git config --get http.https://github.com/.extraheader 2>/dev/null || true)"
  # BUILD #416 (D.8) -- actions/checkout keeps its token out of the key this
  # used to read, so every evidence push since that change silently skipped and
  # the branch stayed empty of ci-out/. The workflow now passes GH_TOKEN in and
  # the runner clones/pushes with it; the old header path stays as a fallback.
  if [ -n "${GH_TOKEN:-}" ]; then
    REMOTE="https://x-access-token:${GH_TOKEN}@github.com/Loganwall111/Lowuuuuuu.git"
    AUTH=""
    echo "[evidence] using GH_TOKEN for the evidence push"
  elif [ -n "$AUTH" ]; then
    REMOTE="$EVIDENCE_REPO"
  else
    echo "[evidence] no credentials - skip"; return 0
  fi
  rm -rf /tmp/mcsm-evidence
  if [ -n "$AUTH" ]; then
    GIT_LFS_SKIP_SMUDGE=1 git -c "http.https://github.com/.extraheader=${AUTH}" \
      clone -q --depth 5 --branch "$EVIDENCE_BRANCH" "$REMOTE" /tmp/mcsm-evidence || {
      echo "[evidence] clone failed - skip"; return 0; }
  else
    GIT_LFS_SKIP_SMUDGE=1 git clone -q --depth 5 --branch "$EVIDENCE_BRANCH" "$REMOTE" /tmp/mcsm-evidence || {
      echo "[evidence] clone failed - skip"; return 0; }
  fi
  local DST="/tmp/mcsm-evidence/ci-out/run-${GITHUB_RUN_NUMBER:-local}"
  rm -rf "$DST"; mkdir -p "$DST"
  cp -f out/BUILD_INFO.txt "$DST/" 2>/dev/null || true
  cp -f out/JAVAC_FAILED.txt "$DST/" 2>/dev/null || true
  cp -f out/vanilla-api.txt "$DST/" 2>/dev/null || true
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
