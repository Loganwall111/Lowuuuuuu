#!/usr/bin/env python3
"""Extract embedded Blockbench texture payloads from .bbmodel files.

This keeps the original texture *names* so any future Stage A-D model port can
reference the recovered PNGs directly from
`assets/devouringstorms/textures/entity/`.

Notes:
- only embedded `data:image/...;base64,...` sources are extracted;
- three known throwaway joke placeholders are skipped on purpose;
- if the same texture name appears with different bytes, the first file keeps the
  original name and later variants are written as `name__1.png`, `name__2.png`,
  etc. so nothing is silently lost.
"""

from __future__ import annotations

import argparse
import base64
import json
from pathlib import Path
from typing import Iterable

SKIP_NAMES = {
    "placeholder again.png",
    "it was at that moment superman realized his fate.png",
    "hold the elevator!.png",
}

WINDOWS_SAFE_RENAMES = {
    "1:1 flesh.png": "1_1_flesh.png",
    "this is 1:1 too.png": "this_is_1_1_too.png",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--geo-root",
        type=Path,
        default=Path("src/main/resources/assets/devouringstorms/geo"),
        help="Root folder that contains the source .bbmodel files.",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path("src/main/resources/assets/devouringstorms/textures/entity"),
        help="Destination for extracted PNG files.",
    )
    return parser.parse_args()


def iter_bbmodels(root: Path) -> Iterable[Path]:
    yield from sorted(root.rglob("*.bbmodel"))


def preferred_name(raw_name: str) -> str:
    name = Path(raw_name).name
    return WINDOWS_SAFE_RENAMES.get(name, name)


def write_unique_png(out_dir: Path, raw_name: str, payload: bytes) -> Path:
    original = preferred_name(raw_name)
    stem = Path(original).stem
    suffix = Path(original).suffix or ".png"
    candidate = out_dir / original

    if not candidate.exists():
        candidate.write_bytes(payload)
        return candidate

    if candidate.read_bytes() == payload:
        return candidate

    index = 1
    while True:
        duplicate = out_dir / f"{stem}__{index}{suffix}"
        if not duplicate.exists():
            duplicate.write_bytes(payload)
            return duplicate
        if duplicate.read_bytes() == payload:
            return duplicate
        index += 1


def extract_embedded_textures(geo_root: Path, out_dir: Path) -> tuple[int, int]:
    out_dir.mkdir(parents=True, exist_ok=True)
    written = 0
    models = 0

    for bbmodel in iter_bbmodels(geo_root):
        models += 1
        try:
            data = json.loads(bbmodel.read_text())
        except Exception as exc:  # pragma: no cover - tooling script
            print(f"[warn] failed to parse {bbmodel}: {exc}")
            continue

        for texture in data.get("textures", []):
            name = texture.get("name")
            source = texture.get("source", "")
            if not name or name in SKIP_NAMES:
                continue
            if not source.startswith("data:image/") or "," not in source:
                continue

            payload = base64.b64decode(source.split(",", 1)[1])
            write_unique_png(out_dir, name, payload)
            written += 1

    return models, written


def main() -> int:
    args = parse_args()
    models, refs = extract_embedded_textures(args.geo_root, args.output_dir)
    print(f"Scanned {models} .bbmodel files")
    print(f"Recovered {refs} embedded texture references into {args.output_dir}")
    return 0


if __name__ == "__main__":  # pragma: no cover - tooling script
    raise SystemExit(main())
