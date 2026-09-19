#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Devouring Storms deep scan -- run on a GitHub runner (which CAN download
# release assets and HAS a JDK), because the sandbox can do neither.
#
# Purpose: the original author's features that are broken or unfinished (the
# town build queue behind /mcsm build, the /mcsm command surface, preset
# mechanics) live in compiled classes we do not have sources for. To fix them
# with mixins we must first SEE them: this script downloads the pinned base
# jar, lists every mod class, extracts the strings that identify the feature
# classes, and disassembles the interesting ones with javap. Results are
# committed back to the repo under ci/api/scan/ by the calling workflow.
# ---------------------------------------------------------------------------
set -euo pipefail

DL=/tmp/deepscan
rm -rf "$DL" ci/api/scan
mkdir -p "$DL" ci/api/scan

BASE_VER="1.9.100"
BASE_NAME="dabywitherstormmod-${BASE_VER}-26.2-beta-mcsm.jar"
BASE_SHA="6adcf07e1ad810703c12cb25d7d135aca7b8f66f7d12c273ad3f00b5abdb6599"
curl -fsSL --retry 3 --retry-delay 3 \
  -o "$DL/$BASE_NAME" \
  "https://github.com/Loganwall111/Lowuuuuuu/releases/download/mcsm-${BASE_VER}/${BASE_NAME}"
echo "$BASE_SHA  $DL/$BASE_NAME" | sha256sum -c -

# Extract only the mod's own classes (vanilla/fabric are already indexed).
(cd "$DL" && unzip -qo "$BASE_NAME" 'net/dabicco/*' 'fabric.mod.json')

(cd "$DL" && find net/dabicco -name '*.class' | sort) > ci/api/scan/mod-classes.txt
echo "[scan] $(wc -l < ci/api/scan/mod-classes.txt) mod classes"

python3 ci/scanstrings.py "$DL" > ci/api/scan/strings.txt
tail -1 ci/api/scan/strings.txt

# javap signatures for every class whose NAME suggests feature machinery.
: > ci/api/scan/javap-signatures.txt
grep -Ei 'command|town|struct|build|village|story|schematic|place|queue|preset' \
     ci/api/scan/mod-classes.txt | grep -v '\$' | head -100 | while read -r c; do
  cn="${c%.class}"; cn="${cn//\//.}"
  echo "===== $cn =====" >> ci/api/scan/javap-signatures.txt
  javap -p -classpath "$DL" "$cn" >> ci/api/scan/javap-signatures.txt 2>/dev/null || true
done

# Full bytecode (-c) for the command class, anything town/build/queue-named,
# and any class whose strings matched the build-queue messages.
BYTECODE_SET=$(mktemp)
grep -Ei 'command|town|build|struct|queue|schematic|place' ci/api/scan/mod-classes.txt \
  | grep -v '\$' | head -20 > "$BYTECODE_SET" || true
awk '/^### /{cls=$2} /queued|location\(s\)|build over|no location/{print cls}' \
  ci/api/scan/strings.txt >> "$BYTECODE_SET" || true
# The town-queue machinery, inner classes included: McsmWorldgen owns the
# QUEUE/budget/tick() that /mcsm build feeds, and the first scan's head -14
# cut off exactly these two.
grep -E 'McsmWorldgen|StructureBuilder|McsmSchematic' ci/api/scan/mod-classes.txt \
  >> "$BYTECODE_SET" || true
sort -u "$BYTECODE_SET" | head -28 | while read -r c; do
  [ -n "$c" ] || continue
  cn="${c%.class}"; cn="${cn//\//.}"
  echo "===== $cn (bytecode) =====" >> ci/api/scan/javap-bytecode.txt
  javap -p -c -classpath "$DL" "$cn" >> ci/api/scan/javap-bytecode.txt 2>/dev/null || true
done
rm -f "$BYTECODE_SET"

# WHO CALLS THE QUEUE? McsmWorldgen.tick(ServerLevel) drains the build queue;
# if no class references McsmWorldgen at all outside the command, the queue is
# written-but-dead and /mcsm build can never produce a town. List every class
# whose constant pool mentions McsmWorldgen or StructureBuilder, then
# disassemble the mentions that are not the queue classes themselves.
grep -rl "McsmWorldgen\|StructureBuilder" "$DL/net" 2>/dev/null \
  | sed "s|^$DL/||" | sort > ci/api/scan/queue-referencers.txt || true
echo "[scan] classes referencing the queue machinery:"
cat ci/api/scan/queue-referencers.txt
: > ci/api/scan/queue-callers-bytecode.txt
grep -v -E 'McsmWorldgen|StructureBuilder' ci/api/scan/queue-referencers.txt \
  | head -10 | while read -r c; do
  cn="${c%.class}"; cn="${cn//\//.}"
  echo "===== $cn (references the queue) =====" >> ci/api/scan/queue-callers-bytecode.txt
  javap -p -c -classpath "$DL" "$cn" >> ci/api/scan/queue-callers-bytecode.txt 2>/dev/null || true
done

# ---------------------------------------------------------------------------
# Round 3 -- the town-queue diagnosis set:
#   * does the jar even SHIP the .schematic files /mcsm build loads?
#   * what are ANCHOR_X/ANCHOR_Z (where in the world do towns land)?
#   * how is McsmWorldgen.tick registered in onInitialize (which event, any
#     condition)?
# ---------------------------------------------------------------------------
unzip -l "$DL/$BASE_NAME" | grep -Ei 'schematic' > ci/api/scan/jar-schematics.txt \
  || echo "(NO schematic entries in the jar)" > ci/api/scan/jar-schematics.txt
unzip -l "$DL/$BASE_NAME" | awk 'NR>3 {print $4}' | grep -v '\.class$' | grep -v '^$' \
  | sort > ci/api/scan/jar-resources.txt || true
javap -p -constants -classpath "$DL" net.dabicco.witherstormmod.structures.McsmWorldgen \
  > ci/api/scan/worldgen-constants.txt 2>/dev/null || true
javap -p -c -classpath "$DL" net.dabicco.witherstormmod.DabyWitherStormMod \
  > ci/api/scan/mainmod-bytecode.txt 2>/dev/null || true
grep -n "McsmWorldgen\|ServerTick\|WorldEvents\|AFTER_TICK\|END_SERVER" ci/api/scan/mainmod-bytecode.txt \
  | head -30 > ci/api/scan/tick-registration.txt || true

wc -l ci/api/scan/*.txt
echo "[scan] done -- commit ci/api/scan back to the branch"
