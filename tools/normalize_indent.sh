#!/usr/bin/env bash
# Converts leading 4-space groups to tabs in every GDScript file (Godot style).
set -euo pipefail
cd "$(dirname "$0")/.."
find scripts shaders -name '*.gd' -print0 2>/dev/null | while IFS= read -r -d '' f; do
  unexpand --first-only -t 4 "$f" > "$f.tmp" && mv "$f.tmp" "$f"
done
