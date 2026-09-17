#!/usr/bin/env python3
"""Devouring Storms deep scan -- string extractor for compiled mod classes.

Walks an extracted class tree, pulls printable ASCII strings out of every
.class file, and prints the ones matching the keywords that identify the
features the original author left broken or unfinished (the town build queue,
the /mcsm command surface, teleport targets). Output is grouped by class so
the follow-up javap pass knows exactly which classes to disassemble.

Usage: python3 ci/scanstrings.py <extracted-dir>
"""
import os
import re
import sys

KEYWORDS = re.compile(
    r"queued|location\(s\)|build over|/mcsm|mcsm |town|schematic|structure|"
    r"teleport|no location|status|population|berserk|obliterate|devour",
    re.IGNORECASE,
)
STR = re.compile(rb"[\x20-\x7e]{6,200}")


def main(root: str) -> int:
    hits_by_class = {}
    for dirpath, _dirnames, filenames in os.walk(root):
        for fn in sorted(filenames):
            if not fn.endswith(".class"):
                continue
            path = os.path.join(dirpath, fn)
            rel = os.path.relpath(path, root)
            try:
                data = open(path, "rb").read()
            except OSError:
                continue
            seen = set()
            for m in STR.finditer(data):
                s = m.group().decode("ascii", "replace")
                if KEYWORDS.search(s) and s not in seen:
                    seen.add(s)
                    hits_by_class.setdefault(rel, []).append(s)
    for cls in sorted(hits_by_class):
        print(f"\n### {cls}")
        for s in hits_by_class[cls][:60]:
            print(f"    {s}")
    print(f"\n[scanstrings] {len(hits_by_class)} classes with keyword strings")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else "."))
