#!/usr/bin/env python3
"""
CLONE // Multi-Volume Agent Swarm — .rbxlx place-file generator.

Turns the Rojo-style `src/` tree (see default.project.json) into a single
`CLONE_MultiVolumeAgentSwarm.rbxlx` file that Roblox Studio can open directly
(File > Open, or double-click), with zero plugins.

Usage:  python3 tools/generate_rbxlx.py
"""
import json
import os
import sys
import xml.sax.saxutils as sax

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(REPO, "src")
OUT = os.path.join(REPO, "CLONE_MultiVolumeAgentSwarm.rbxlx")


def load_project():
    with open(os.path.join(REPO, "default.project.json"), "r", encoding="utf-8") as f:
        return json.load(f)


def node_class(filename):
    """Rojo-style class inference from a .lua file name."""
    if filename.endswith(".server.lua"):
        return "Script"
    if filename.endswith(".client.lua"):
        return "LocalScript"
    if filename.endswith(".lua"):
        return "ModuleScript"
    return None


def node_name(filename):
    """Instance name: strip the .server/.client/.lua suffixes."""
    for suffix in (".server.lua", ".client.lua", ".lua"):
        if filename.endswith(suffix):
            return filename[: -len(suffix)]
    return os.path.splitext(filename)[0]


def scan_dir(path):
    """Return child nodes (Folder / Script / LocalScript / ModuleScript) for a dir."""
    children = []
    for entry in sorted(os.listdir(path)):
        full = os.path.join(path, entry)
        if os.path.isdir(full):
            child = {"name": entry, "class": "Folder", "children": scan_dir(full)}
            children.append(child)
        else:
            cls = node_class(entry)
            if cls:
                with open(full, "r", encoding="utf-8") as f:
                    src = f.read()
                children.append({
                    "name": node_name(entry),
                    "class": cls,
                    "source": src,
                    "children": [],
                })
    return children


def build_node(key, val):
    """Recursively turn a default.project.json entry into a node dict."""
    if "$path" in val:
        path = os.path.normpath(os.path.join(REPO, val["$path"]))
        cls = val.get("$className", "Folder")
        return {"name": key, "class": cls, "children": scan_dir(path)}
    cls = val.get("$className", "Folder")
    node = {"name": key, "class": cls, "children": []}
    for child_key, child_val in val.items():
        if child_key.startswith("$"):
            continue
        node["children"].append(build_node(child_key, child_val))
    return node


def flatten(root, out):
    idx = 0
    def visit(node):
        nonlocal idx
        node["referent"] = idx
        idx += 1
        out.append(node)
        for c in node.get("children", []):
            visit(c)
    visit(root)
    return idx


def props_for(node):
    props = []
    props.append(f'<string name="Name">{sax.escape(node["name"])}</string>')
    cls = node["class"]
    if cls in ("Script", "LocalScript", "ModuleScript"):
        src = node.get("source", "")
        assert "]]>" not in src, f"source in {node['name']} contains ]]>, breaks CDATA"
        props.append(f'<ProtectedString name="Source"><![CDATA[{src}]]></ProtectedString>')
    if cls in ("Script", "LocalScript"):
        props.append('<bool name="Disabled">false</bool>')
    if cls == "DataModel":
        props.append('<string name="PlaceVersion">0</string>')
    return props


def emit(node, depth=1):
    ind = "\t" * depth
    ind2 = "\t" * (depth + 1)
    lines = [f'{ind}<Item class="{node["class"]}" referent="{node["referent"]}">']
    props = props_for(node)
    if props:
        lines.append(f"{ind2}<Properties>")
        for p in props:
            lines.append(f"{ind2}\t{p}")
        lines.append(f"{ind2}</Properties>")
    for c in node.get("children", []):
        lines.extend(emit(c, depth + 1))
    lines.append(f"{ind}</Item>")
    return lines


def main():
    project = load_project()
    tree = project["tree"]

    root = {"name": "Game", "class": "DataModel", "children": []}
    for svc_key, svc_val in tree.items():
        if svc_key.startswith("$"):
            continue
        root["children"].append(build_node(svc_key, svc_val))

    ordered = []
    total = flatten(root, ordered)

    out_lines = [
        '<?xml version="1.0"?>',
        '<roblox xmlns:xmime="http://www.w3.org/2005/05/xmlmime" '
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" '
        'xsi:noNamespaceSchemaLocation="http://www.roblox.com/roblox.xsd" version="4">',
        "\t<External>null</External>",
        "\t<External>nil</External>",
    ]
    out_lines.extend(emit(root))
    out_lines.append("</roblox>")

    with open(OUT, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(out_lines))

    print(f"Wrote {OUT} with {total} instances.")


if __name__ == "__main__":
    sys.exit(main())
