#!/usr/bin/env bash
# ============================================================================
# ci/install-workflow.sh — install the MCSM jar build workflow into
# .github/workflows/ so GitHub automatically builds on arena-branch pushes.
#
# WHY: the arena "build bot" token has no `workflows` permission, so it cannot
# push .github/workflows/ files itself — this script is meant to be run by the
# repo owner with their own credentials. Idempotent: safe to re-run.
#
# Usage:  bash ci/install-workflow.sh      (from the repo root, after fetching)
# ============================================================================
set -euo pipefail

SRC="ci/workflows/build-mcsm.yml"
DST=".github/workflows/build-mcsm.yml"

if [ ! -f "$SRC" ]; then
  echo "error: $SRC not found — run me from the repository root."
  exit 1
fi

mkdir -p "$(dirname "$DST")"
cp "$SRC" "$DST"
git add "$DST"

if git diff --cached --quiet; then
  echo "[install] $DST already installed and identical — nothing to do."
  exit 0
fi

git commit -m "ci: install MCSM jar build workflow (auto-build on arena-branch pushes)"
if git push; then
  echo "[install] pushed. A 'Build MCSM Wither Storm jar' run starts now:"
  echo "          https://github.com/Loganwall111/Lowuuuuuu/actions"
else
  echo "[install] push failed — check your Git credentials (a token with the"
  echo "          'workflows' permission is required to update this file)."
  exit 1
fi
