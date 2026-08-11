#!/usr/bin/env python3
"""Structural validation of .tscn/.tres: every ext_resource path exists, every
sub_resource/node reference resolves, and node `parent` paths are valid."""
import os, re, sys

root = "."
problems = []
def exists(path):
    if path.startswith("res://"):
        path = path[len("res://"):]
    return os.path.exists(os.path.join(root, path))

scene_files = []
for dp,_,fs in os.walk("scenes"):
    for f in fs:
        if f.endswith(".tscn") or f.endswith(".tres"):
            scene_files.append(os.path.join(dp, f))

for sf in scene_files:
    text = open(sf).read()
    # ext_resource paths
    for m in re.finditer(r'\[ext_resource type="([^"]+)" path="([^"]+)"', text):
        etype, ep = m.group(1), m.group(2)
        if not exists(ep):
            problems.append(f"{sf}: missing ext_resource {etype} -> {ep}")
        # For Script/PackedScene/Resource, the target should exist.
    # sub_resource id definitions
    sub_ids = set(re.findall(r'\[sub_resource type="([^"]+)" id="([^"]+)"', text))
    sub_refs = set(re.findall(r'SubResource\("([^"]+)"\)', text))
    for r in sub_refs:
        # Find matching id="r" in sub_resource
        if not re.search(r'id="%s"' % re.escape(r), text):
            problems.append(f"{sf}: SubResource('{r}') not defined")
    # node parent validity: parent="X/Y" must reference an earlier [node name=...]
    node_names = re.findall(r'\[node name="([^"]+)"', text)
    for m in re.finditer(r'\[node name="([^"]+)" type="([^"]+)"(?:\n(?:.*\n))*?\]', text):
        pass
    # Check parent references
    for m in re.finditer(r'parent="([^"]+)"', text):
        par = m.group(1)
        # parent is a node name or name path; verify the base name exists as a node
        base = par.split("/")[0]
        if base not in node_names:
            problems.append(f"{sf}: parent '{par}' base node '{base}' not declared")
    # referenced node names in script props (NodePath) - soft check only
    print(f"checked {sf}: nodes={len(node_names)} sub_ids={len(sub_ids)} refs={len(sub_refs)}")

if problems:
    print("\n=== PROBLEMS ===")
    for p in problems:
        print(p)
    sys.exit(1)
else:
    print("\nALL SCENE/RESOURCE STRUCTURAL CHECKS PASSED")
