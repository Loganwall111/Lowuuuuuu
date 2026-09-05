#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Devouring Storms -- model-builder recovery (Track B step 2b).
#
# Six model classes were excluded from the whole-mod source compile because
# Vineflower OOM'd INSIDE their createBodyLayer() builders (multi-thousand
# call chains, 300+ locals) at the 6 GB whole-tree heap:
#   WitherStormDevourer, WitherStormP4, HugeAssBackModel, HunchbackGrowth,
#   SeveredWitherStorm, WitherStormTentacles5
#
# This pipeline gives each class its own decompiler run at 12 GB (single
# class = tiny input, huge headroom), falls back to CFR when Vineflower
# still dies, splices the recovered createBodyLayer() back into the existing
# src-recon file (method-level, imports merged), lifts that class's compile
# exclusion in ci/build-source.sh, and pushes. The push re-triggers the
# whole-mod compile, which is the proof the splice is real.
#
# Report: ci/reports/model-recovery-latest.txt (travels back via git).
# ---------------------------------------------------------------------------
set -uo pipefail

DL=/tmp/mr-dl
WORK=/tmp/mr-work
mkdir -p "$DL" "$WORK" out ci/reports

fetch() {
  local url="$1" out="$DL/$2"
  [ -s "$out" ] || curl -fsSL --retry 3 --retry-delay 3 -o "$out" "$url" || {
    echo "::error title=recovery::download failed: $url"; return 1; }
  echo "[recovery] $2 $(stat -c%s "$out") B"
}

fetch "https://github.com/Vineflower/vineflower/releases/download/1.11.1/vineflower-1.11.1.jar" vineflower.jar \
  || fetch "https://repo1.maven.org/maven2/org/vineflower/vineflower/1.11.1/vineflower-1.11.1.jar" vineflower.jar || exit 1
fetch "https://repo1.maven.org/maven2/org/benf/cfr/0.152/cfr-0.152.jar" cfr.jar || exit 1

BASE_NAME="dabywitherstormmod-1.9.100-26.2-beta-mcsm.jar"
BASE_SHA="6adcf07e1ad810703c12cb25d7d135aca7b8f66f7d12c273ad3f00b5abdb6599"
fetch "https://github.com/Loganwall111/Lowuuuuuu/releases/download/mcsm-1.9.100/${BASE_NAME}" base.jar || exit 1
ACTUAL="$(sha256sum "$DL/base.jar" | cut -d' ' -f1)"
if [ "$ACTUAL" != "$BASE_SHA" ]; then
  echo "::error title=recovery::base jar sha mismatch: $ACTUAL"
  exit 1
fi

( cd "$WORK" && unzip -o -q "$DL/base.jar" 'net/dabicco/witherstormmod/entity/model/*.class' )

CLASSES="WitherStormDevourer WitherStormP4 HugeAssBackModel HunchbackGrowth SeveredWitherStorm WitherStormTentacles5"
REPORT=/tmp/mr-report.txt
: > "$REPORT"

for C in $CLASSES; do
  CLS="$WORK/net/dabicco/witherstormmod/entity/model/$C.class"
  if [ ! -s "$CLS" ]; then
    echo "$C: MISSING from base jar" | tee -a "$REPORT"
    continue
  fi
  OUT="$WORK/vf-$C"
  mkdir -p "$OUT"
  echo "[recovery] vineflower 12g on $C ..."
  java -Xmx12g -XX:-UseGCOverheadLimit -jar "$DL/vineflower.jar" "$CLS" "$OUT" > "$OUT/log.txt" 2>&1
  F="$(find "$OUT" -name '*.java' | head -1)"
  TOOL=vineflower
  if [ -z "$F" ] || grep -q "Couldn't be decompiled" "$F" 2>/dev/null; then
    echo "[recovery] vineflower failed on $C -- trying CFR"
    OUT="$WORK/cfr-$C"
    mkdir -p "$OUT"
    java -Xmx12g -jar "$DL/cfr.jar" "$CLS" --outputdir "$OUT" > "$OUT/log.txt" 2>&1
    F="$(find "$OUT" -name "$C.java" | head -1)"
    TOOL=cfr
    if [ -z "$F" ] || grep -q "Cannot decompile\|Exception" "$F" 2>/dev/null; then
      # CFR sometimes still emits the method with an exception comment
      if [ -n "$F" ] && grep -q "createBodyLayer" "$F"; then
        echo "[recovery] CFR output for $C kept despite warnings"
      else
        echo "$C: BOTH DECOMPILERS FAILED" | tee -a "$REPORT"
        continue
      fi
    fi
  fi
  if ! grep -q "createBodyLayer" "$F"; then
    echo "$C: recovered file lacks createBodyLayer" | tee -a "$REPORT"
    continue
  fi
  echo "$C: recovered via $TOOL -> $F" | tee -a "$REPORT"
  echo "$F" > "$WORK/splice-$C.path"
done

# --- splice recovered createBodyLayer() into src-recon + lift exclusions ---
python3 - "$WORK" <<'SPLICE'
import os, re, sys

work = sys.argv[1]
repo = os.getcwd()
report = open("/tmp/mr-report.txt", "a")
SIG = re.compile(r'(?:public|private|protected)\s+static\s+LayerDefinition\s+createBodyLayer\s*\(\s*\)\s*\{')

def method_span(src, m):
    j = src.index("{", m.end() - 1)
    depth = 0
    for k in range(j, len(src)):
        if src[k] == "{":
            depth += 1
        elif src[k] == "}":
            depth -= 1
            if depth == 0:
                return m.start(), k + 1
    return None

spliced = []
for C in ["WitherStormDevourer", "WitherStormP4", "HugeAssBackModel",
          "HunchbackGrowth", "SeveredWitherStorm", "WitherStormTentacles5"]:
    pf = os.path.join(work, f"splice-{C}.path")
    if not os.path.isfile(pf):
        continue
    newp = open(pf).read().strip()
    oldp = os.path.join(repo, "src-recon/net/dabicco/witherstormmod/entity/model", C + ".java")
    new = open(newp).read()
    old = open(oldp).read()
    mn = SIG.search(new)
    mo = SIG.search(old)
    if not mn or not mo:
        report.write(f"{C}: splice skipped (signature not found new={bool(mn)} old={bool(mo)})\n")
        continue
    sn = method_span(new, mn)
    so = method_span(old, mo)
    if not sn or not so:
        report.write(f"{C}: splice skipped (unbalanced braces)\n")
        continue
    body = new[sn[0]:sn[1]]
    # merge missing imports
    head_end = old.index("public class") if "public class" in old else old.index("class ")
    added = []
    for imp in re.findall(r'^import ([\w.]+);$', new, re.M):
        simple = imp.rsplit(".", 1)[-1]
        if simple in body and ("import " + simple) not in old and (simple + ";") not in old[:head_end]:
            if re.search(r'\b' + re.escape(simple) + r'\b', old[:head_end]) is None:
                added.append("import " + imp + ";")
    out = old[:so[0]] + body + old[so[1]:]
    if added:
        cut = out.rindex("import ", 0, out.index("public class") if "public class" in out else out.index("class "))
        cut = out.index("\n", cut)
        out = out[:cut + 1] + "\n".join(added) + out[cut:]
    open(oldp, "w").write(out)
    spliced.append(C)
    report.write(f"{C}: createBodyLayer spliced from {os.path.basename(newp)} (+{len(added)} imports)\n")

# lift compile exclusions for spliced classes
bs = os.path.join(repo, "ci/build-source.sh")
s = open(bs).read()
for C in spliced:
    s = s.replace(f" ! -name '{C}.java'", "")
open(bs, "w").write(s)
report.write(f"spliced: {', '.join(spliced) or 'NONE'}\n")
report.close()
print("[recovery] spliced:", ", ".join(spliced) or "NONE")
SPLICE

cp /tmp/mr-report.txt ci/reports/model-recovery-latest.txt
cat /tmp/mr-report.txt | while IFS= read -r line; do
  echo "::notice title=recovery::${line:0:240}"
done

# --- push so the whole-mod compile re-runs as proof ---
if ! git diff --quiet -- src-recon ci/build-source.sh ci/reports 2>/dev/null; then
  git config user.name "ds-ci-bot"
  git config user.email "ci@devouringstorms.local"
  git add src-recon ci/build-source.sh ci/reports
  git commit -m "Source recon: recover ${SPLICE_COUNT:-model} createBodyLayer builders at 12g heap [no-ci]" || true
  git push "https://x-access-token:${GITHUB_TOKEN}@github.com/${GITHUB_REPOSITORY}.git" HEAD:${GITHUB_REF_NAME} \
    || echo "::warning title=recovery::push failed"
else
  echo "::notice title=recovery::nothing changed -- no splice landed"
fi
exit 0
