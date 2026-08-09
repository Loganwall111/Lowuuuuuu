#!/usr/bin/env python3
"""
DEVOURING STORMS — procedural structure generator (v1.1, Endertown expansion).

One source of truth for the Decayed Reality builds, emitted for BOTH editions:

  Java (Fabric mod):  gzipped structure NBT files placed by datapack worldgen
                      -> java-mod/src/main/resources/data/devouring_storms/structure/*.nbt
  Bedrock (add-on):   a script data module the behaviour engine stamps out block
                      by block (Bedrock add-ons cannot register worldgen structures)
                      -> bedrock-addon/DevouringStormsBP/scripts/builds_data.js
  Resource pack:      Endertown Core item icon
                      -> bedrock-addon/DevouringStormsRP/textures/items/endertown_core.png

Designed stateless on purpose: every block used has identical default block-states
on both editions (axis=y logs, sitting lanterns, y-axis chains ...). The ONLY
oriented block is the Java wall banner, which the Bedrock emitter swaps for a
wool "tapestry" block (block-entity NBT cannot be set through the stable script API).

Everything is original, deterministic, procedurally placed geometry. No assets or
layouts are copied from Decayed Reality V2 or any other project.
"""

import gzip
import os
import struct
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA_STRUCT_DIR = os.path.join(ROOT, "java-mod", "src", "main", "resources",
                               "data", "devouring_storms", "structure")
BEDROCK_SCRIPTS = os.path.join(ROOT, "bedrock-addon", "DevouringStormsBP", "scripts")
RP_ITEM_TEX = os.path.join(ROOT, "bedrock-addon", "DevouringStormsRP", "textures", "items")

DATA_VERSION = 4189  # ~MC 1.21.x; DataFixer happily upgrades older structure files

# ---------------------------------------------------------------- NBT writer (big-endian)

def _str(tag, name):  # named-tag header
    raw = name.encode("utf-8")
    return bytes([tag]) + struct.pack(">H", len(raw)) + raw

def st(v):  return (8, v)
def i32(v): return (3, int(v))
def dbl(v): return (6, float(v))
def lst(item_tag, payloads): return (9, (item_tag, payloads))
def comp(*pairs): return (10, list(pairs))

def _payload(buf, tag, payload):
    if tag == 3:   buf += struct.pack(">i", payload)
    elif tag == 6: buf += struct.pack(">d", payload)
    elif tag == 8:
        raw = payload.encode("utf-8")
        buf += struct.pack(">H", len(raw)) + raw
    elif tag == 9:
        item_tag, items = payload
        buf += bytes([item_tag]) + struct.pack(">i", len(items))
        for it in items:
            # lists store raw payloads (unnamed); unwrap (tag, payload) tuples
            buf = _payload(buf, item_tag, it[1] if isinstance(it, tuple) else it)
    elif tag == 10:
        for name, (t, p) in payload:
            buf += _str(t, name)
            buf = _payload(buf, t, p)
        buf += b"\x00"
    else:
        raise ValueError(f"unhandled tag {tag}")
    return buf

def write_nbt(root_compound_payload, path):
    """root_compound_payload: list of (name, (tag, payload)); gzip-compressed."""
    buf = _payload(b"", 10, root_compound_payload)
    data = _str(10, "") + buf
    with open(path, "wb") as fh:
        fh.write(gzip.compress(data, compresslevel=9, mtime=0))
    return len(data)

# ---------------------------------------------------------------- palette / mapping

MOD_JAVA = "devouring_storms"
# Java id -> Bedrock id where different.  "mod:X" shorthand expands per-edition.
BEDROCK_ID = {
    "minecraft:end_stone_bricks": "minecraft:end_bricks",
    "minecraft:end_stone_brick_wall": "minecraft:end_brick_wall",
}

def java_id(shorthand):
    return f"{MOD_JAVA}:{shorthand[4:]}" if shorthand.startswith("mod:") else shorthand

def bedrock_id(java):
    if java in BEDROCK_ID:
        return BEDROCK_ID[java]
    if java.startswith("mcr__banner_wool"):  # banner swap emitted already as wool
        return java.replace("mcr__banner_wool", "minecraft")
    if java.startswith(f"{MOD_JAVA}:"):
        name = java.split(":", 1)[1]
        if name == "rift_portal":
            return "ds:rift_portal_block"
        return f"ds:{name}"
    return java

# ---------------------------------------------------------------- builder

class Build:
    def __init__(self, name):
        self.name = name
        self.blocks = {}          # (x,y,z) -> (java_id, props_dict_or_None, nbt_or_None)
        self.chests = []          # (x,y,z, loot_key)  loot_key without "chests/"
        self.entities = []        # (x,y,z, java_entity_id) — placed with the template
        self.banner_count = 0

    def entity(self, x, y, z, entity_id):
        """Place an entity with the structure (Java templates carry them)."""
        self.entities.append((x, y, z, java_id(entity_id)))

    # ---- core setters
    def set(self, x, y, z, block, props=None, nbt=None):
        self.blocks[(x, y, z)] = (java_id(block), props, nbt)

    def rect(self, x0, y0, z0, x1, y1, z1, block, props=None):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, block, props)

    def ring(self, cx, y, cz, r, block, sy=None):
        """Floor ring-ish outline of radius r on the y plane."""
        for x in range(cx - r, cx + r + 1):
            for z in range(cz - r, cz + r + 1):
                d = (x - cx) ** 2 + (z - cz) ** 2
                if (r - 1) ** 2 < d <= r * r + 1:
                    self.set(x, y, z, block)
                    if sy:
                        self.set(x, y + sy, z, block)

    def disc(self, cx, y, cz, r, block):
        for x in range(cx - r, cx + r + 1):
            for z in range(cz - r, cz + r + 1):
                if (x - cx) ** 2 + (z - cz) ** 2 <= r * r:
                    self.set(x, y, z, block)

    # ---- dual-edition helpers
    def wall_banner(self, x, y, z, facing, colors):
        """Java: wall banner with patterns.  Bedrock: wool tapestry in the wall face."""
        base, pat_color = colors
        nbt = comp(
            ("id", st("minecraft:banner")),
            ("patterns", lst(10, [
                comp(("color", st(pat_color)), ("pattern", st("minecraft:rhombus"))),
                comp(("color", st(pat_color)), ("pattern", st("minecraft:border"))),
            ])),
        )
        self.set(x, y, z, f"minecraft:{base}_wall_banner",
                 props={"facing": facing}, nbt=nbt)
        self.banner_count += 1

    def stand_banner(self, x, y, z, base="purple"):
        nbt = comp(
            ("id", st("minecraft:banner")),
            ("patterns", lst(10, [
                comp(("color", st("black")), ("pattern", st("minecraft:circle"))),
                comp(("color", st("magenta")), ("pattern", st("minecraft:gradient"))),
            ])),
        )
        self.set(x, y, z, f"minecraft:{base}_banner", nbt=nbt)
        self.banner_count += 1

    def loot_chest(self, x, y, z, loot_key):
        nbt = comp(
            ("id", st("minecraft:chest")),
            ("LootTable", st(f"{MOD_JAVA}:chests/{loot_key}")),
        )
        self.set(x, y, z, "minecraft:chest", nbt=nbt)
        self.chests.append((x, y, z, loot_key))

    # ---- emitters
    def _normalize(self):
        xs = [p[0] for p in self.blocks] or [0]
        ys = [p[1] for p in self.blocks] or [0]
        zs = [p[2] for p in self.blocks] or [0]
        ox, oy, oz = min(xs), min(ys), min(zs)
        norm = {(x - ox, y - oy, z - oz): v for (x, y, z), v in self.blocks.items()}
        chests = [(x - ox, y - oy, z - oz, loot) for x, y, z, loot in self.chests]
        entities = [(x - ox, y - oy, z - oz, eid) for x, y, z, eid in self.entities]
        size = (max(xs) - ox + 1, max(ys) - oy + 1, max(zs) - oz + 1)
        return norm, chests, size, -oy, entities   # ground_y shift for Bedrock anchoring

    def emit_java(self):
        norm, _, size, _, entities = self._normalize()
        palette = [{"Name": st("minecraft:air")}]
        index = {("minecraft:air", ()): 0}
        block_tags = []
        for (x, y, z), (bid, props, nbt) in sorted(norm.items()):
            key = (bid, tuple(sorted((props or {}).items())))
            if key not in index:
                entry = [("Name", st(bid))]
                if props:
                    entry.append(("Properties", comp(*[(k, st(v)) for k, v in props.items()])))
                palette.append(dict(entry))
                index[key] = len(palette) - 1
            payload = [("pos", lst(3, [x, y, z])), ("state", i32(index[key]))]
            if nbt is not None:
                payload.append(("nbt", nbt))
            block_tags.append(comp(*payload))
        entity_tags = [
            comp(
                ("blockPos", lst(3, [x, y, z])),
                ("pos", lst(6, [dbl(x + 0.5), dbl(y), dbl(z + 0.5)])),
                ("nbt", comp(("id", st(eid)))),
            )
            for x, y, z, eid in entities
        ]
        root = [
            ("DataVersion", i32(DATA_VERSION)),
            ("size", lst(3, list(size))),
            ("palette", lst(10, [comp(*[(k, v) for k, v in p.items()]) for p in palette])),
            ("blocks", lst(10, block_tags)),
            ("entities", lst(10, entity_tags)),
        ]
        path = os.path.join(JAVA_STRUCT_DIR, f"{self.name}.nbt")
        raw = write_nbt(root, path)
        return path, len(self.blocks), raw

    def iter_bedrock(self):
        """yield (x, y, z, bedrock_block_id); banners become wool, nbt dropped."""
        for (x, y, z), (bid, _props, _nbt) in self.blocks.items():
            if "_wall_banner" in bid or (bid.endswith("_banner") and "_wall_" not in bid):
                yield (x, y, z, bid.replace("_wall_banner", "_wool").replace("_banner", "_wool"))
            else:
                yield (x, y, z, bedrock_id(bid))

# ---------------------------------------------------------------- shared pieces

def foundation(b, w, d, mat):
    b.rect(0, -1, 0, w - 1, -1, d - 1, mat)
    b.rect(0, -2, 0, w - 1, -2, d - 1, mat)
    for cx, cz in ((0, 0), (w - 3, 0), (0, d - 3), (w - 3, d - 3)):
        b.rect(cx, -2, cz, cx + 2, -2, cz + 2, "minecraft:air")  # chamfered corners

def perimeter_wall(b, w, d):
    """2-high end-brick wall with posts, gates at the four midpoints."""
    gx0, gx1 = w // 2 - 2, w // 2 + 2      # gate span on x walls
    gz0, gz1 = d // 2 - 2, d // 2 + 2      # gate span on z walls
    for x in range(w):
        for z, axis in ((0, "x"), (d - 1, "x")):
            if gx0 <= x <= gx1:
                if x in (gx0, gx1):
                    b.set(x, 4, z, "minecraft:purpur_block")       # lintel caps
                continue
            if x % 4 == 0:
                b.rect(x, 1, z, x, 3, z, "minecraft:end_stone_bricks")
            else:
                b.rect(x, 1, z, x, 2, z, "minecraft:end_stone_brick_wall")
    for z in range(d):
        for x, axis in ((0, "z"), (w - 1, "z")):
            if gz0 <= z <= gz1:
                if z in (gz0, gz1):
                    b.set(x, 4, z, "minecraft:purpur_block")
                continue
            if z % 4 == 0:
                b.rect(x, 1, z, x, 3, z, "minecraft:end_stone_bricks")
            else:
                b.rect(x, 1, z, x, 2, z, "minecraft:end_stone_brick_wall")
    # lintels across the four gates + lanterns sitting on top
    b.rect(gx0, 4, 0, gx1, 4, 0, "minecraft:purpur_block")
    b.rect(gx0, 4, d - 1, gx1, 4, d - 1, "minecraft:purpur_block")
    b.rect(0, 4, gz0, 0, 4, gz1, "minecraft:purpur_block")
    b.rect(w - 1, 4, gz0, w - 1, 4, gz1, "minecraft:purpur_block")
    for lx, lz in ((gx0, 0), (gx1, 0), (gx0, d - 1), (gx1, d - 1)):
        b.set(lx, 5, lz, "minecraft:soul_lantern")
    for lx, lz in ((0, gz0), (0, gz1), (w - 1, gz0), (w - 1, gz1)):
        b.set(lx, 5, lz, "minecraft:soul_lantern")
    # obsidian corner pillars
    for cx, cz in ((0, 0), (w - 1, 0), (0, d - 1), (w - 1, d - 1)):
        b.rect(cx, 1, cz, cx, 5, cz, "minecraft:obsidian")
        b.set(cx, 6, cz, "minecraft:crying_obsidian")

def rot_tree(b, x, z, tall=4):
    for t in range(1, tall + 1):
        b.set(x, t, z, "mod:rot_log")
    top = tall + 1
    b.disc(x, top, z, 2, "mod:decay_block")
    b.disc(x, top + 1, z, 1, "mod:decay_block")
    b.set(x + 3, top - 1, z + 1, "mod:decay_block")
    b.set(x - 2, top - 2, z - 2, "mod:decay_block")
    for dx, dz in ((1, 0), (-1, 1), (0, -1)):
        b.set(x + dx, 0, z + dz, "mod:decayed_soil")

def lamp_post(b, x, z):
    b.rect(x, 1, z, x, 3, z, "minecraft:spruce_fence")
    b.set(x, 4, z, "minecraft:soul_lantern")

def house(b, x0, z0, w, d, h):
    """Purpur block-house: brick base row, black-glass windows, ziggurat roof."""
    x1, z1 = x0 + w - 1, z0 + d - 1
    b.rect(x0, 0, z0, x1, 0, z1, "minecraft:spruce_planks")            # floor
    b.rect(x0 + 1, 1, z0 + 1, x1 - 1, h, z1 - 1, "minecraft:air")      # interior carve
    # walls: brick base row + purpur above; brick corners full height
    b.rect(x0, 1, z0, x1, 1, z1, "minecraft:end_stone_bricks")
    b.rect(x0, 2, z0, x1, h, z1, "minecraft:purpur_block")
    b.rect(x0 + 1, 1, z0 + 1, x1 - 1, h, z1 - 1, "minecraft:air")      # re-hollow
    for cx, cz in ((x0, z0), (x1, z0), (x0, z1), (x1, z1)):
        b.rect(cx, 1, cz, cx, h, cz, "minecraft:end_stone_bricks")
    # windows (black glass) on the two long faces
    mid_y = 3 if h >= 5 else 2
    for wx in range(x0 + 2, x1 - 1, 3):
        b.set(wx, mid_y, z0, "minecraft:black_stained_glass")
        b.set(wx, mid_y, z1, "minecraft:black_stained_glass")
    if h >= 6:  # upper storey windows
        for wx in range(x0 + 2, x1 - 1, 3):
            b.set(wx, h - 1, z0, "minecraft:black_stained_glass")
            b.set(wx, h - 1, z1, "minecraft:black_stained_glass")
    # door (south face): 1 wide, 2 high + mat outside
    dx = x0 + w // 2
    b.set(dx, 1, z1, "minecraft:air"); b.set(dx, 2, z1, "minecraft:air")
    b.set(dx, 0, z1 + 1, "minecraft:polished_blackstone")
    # second storey slab for tall houses (with a stair-well gap)
    if h >= 6:
        b.rect(x0 + 1, 4, z0 + 1, x1 - 1, 4, z1 - 1, "minecraft:spruce_planks")
        b.rect(dx - 1, 4, z0 + 1, dx, 4, z0 + 2, "minecraft:air")
    # ziggurat roof: overhang then recede
    rw, rd, ry = w + 2, d + 2, h + 1
    while rw >= 1 and rd >= 1:
        rx0, rz0 = x0 - 1 + (w + 2 - rw) // 2, z0 - 1 + (d + 2 - rd) // 2
        b.rect(rx0, ry, rz0, rx0 + rw - 1, ry, rz0 + rd - 1, "minecraft:purpur_block")
        rw -= 2; rd -= 2; ry += 1
    b.set(x0 + w // 2, ry, z0 + d // 2, "minecraft:crying_obsidian")   # finial
    # interior: chest, bedroll, lamp corner, creeping decay
    b.loot_chest(x0 + 1, 1, z0 + 1, "endertown_house")
    b.set(x1 - 1, 1, z0 + 1, "minecraft:purple_wool")
    b.set(x1 - 2, 1, z0 + 1, "minecraft:purple_wool")
    b.set(x1 - 1, 1, z1 - 1, "minecraft:spruce_fence")
    b.set(x1 - 1, 2, z1 - 1, "minecraft:lantern")
    b.set(x0 + 1, 1, z1 - 1, "mod:decay_block")
    return dx

# ---------------------------------------------------------------- ENDERTOWN

def build_endertown():
    b = Build("endertown")
    W, D = 56, 48
    PCX, PCZ = 28, 24                       # plaza centre

    foundation(b, W, D, "minecraft:end_stone_bricks")
    b.rect(0, 0, 0, W - 1, 0, D - 1, "minecraft:end_stone")          # ground
    # streets: N-S and E-W axes through the plaza
    b.rect(PCX - 2, 0, 0, PCX + 1, 0, D - 1, "minecraft:polished_blackstone")
    b.rect(0, 0, PCZ - 2, W - 1, 0, PCZ + 1, "minecraft:polished_blackstone")
    # plaza mosaic: end-brick disc, purple glass ring, blackstone rim
    b.disc(PCX, 0, PCZ, 10, "minecraft:end_stone_bricks")
    b.ring(PCX, 0, PCZ, 8, "minecraft:purple_stained_glass")
    b.ring(PCX, 0, PCZ, 10, "minecraft:polished_blackstone")

    perimeter_wall(b, W, D)

    # --- memorial spire (plaza centre): the grief of Endertown made stone
    b.rect(PCX - 2, 1, PCZ - 2, PCX + 2, 2, PCZ + 2, "minecraft:obsidian")
    b.rect(PCX - 1, 3, PCZ - 1, PCX + 1, 9, PCZ + 1, "minecraft:obsidian")
    b.set(PCX, 10, PCZ, "minecraft:crying_obsidian")
    for sx, sz in ((PCX - 1, PCZ), (PCX + 1, PCZ), (PCX, PCZ - 1), (PCX, PCZ + 1)):
        b.set(sx, 10, sz, "minecraft:purple_stained_glass")
    b.disc(PCX, 0, PCZ, 3, "minecraft:obsidian")                     # dais inlay
    b.loot_chest(PCX, 1, PCZ - 4, "endertown_plaza")                 # town heart cache
    b.set(PCX + 3, 1, PCZ, "mod:decayed_jukebox")                    # it still plays

    # standing banner ring around the spire (8 poles)
    for ang, (bx, bz) in enumerate(((36, 24), (20, 24), (28, 32), (28, 16),
                                    (34, 30), (22, 30), (34, 18), (22, 18))):
        b.rect(bx, 1, bz, bx, 3, bz, "minecraft:spruce_fence")
        b.stand_banner(bx, 4, bz, base="purple" if ang % 2 == 0 else "black")

    # --- the banner walls (north & south of plaza): Endertown's flags of memory
    # (banners hang on the plaza-facing side of each panel; attachment side = -facing)
    for panel_z, banner_z, facing in ((15, 16, "south"), (32, 31, "north")):
        b.rect(PCX - 10, 1, panel_z, PCX + 10, 4, panel_z, "minecraft:polished_blackstone")
        for i, bx in enumerate(range(PCX - 10, PCX + 11)):
            col = ("purple", "magenta") if i % 2 == 0 else ("black", "purple")
            b.wall_banner(bx, 2, banner_z, facing, col)
            b.wall_banner(bx, 3, banner_z, facing, col[::-1])
        b.set(PCX - 10, 5, panel_z, "minecraft:soul_lantern")
        b.set(PCX + 10, 5, panel_z, "minecraft:soul_lantern")

    # --- buildings
    house(b, 8, 8, 7, 7, 3)          # A — small home
    house(b, 8, 32, 7, 7, 3)         # B — small home
    house(b, 15, 3, 9, 9, 4)         # C — medium home
    house(b, 33, 3, 9, 9, 4)         # D — medium home
    house(b, 6, 20, 11, 11, 7)       # E — the tall house (two storeys)
    hall_door = house(b, 32, 38, 13, 7, 4)      # F — Relay Hall
    b.set(38, 1, 41, "mod:terminal")            # the town relay
    b.set(36, 1, 41, "mod:sealed_vault")        # the ARG vault, waiting for seven schedules
    b.loot_chest(33, 1, 43, "endertown_house")
    # spare banners flanking the hall's south face
    b.wall_banner(hall_door - 2, 2, 45, "south", ("purple", "magenta"))
    b.wall_banner(hall_door + 2, 2, 45, "south", ("purple", "magenta"))

    # --- watchtower (SE corner)
    tx, tz = 44, 34
    b.rect(tx, 0, tz, tx + 8, 0, tz + 8, "minecraft:end_stone_bricks")
    b.rect(tx, 1, tz, tx + 8, 14, tz + 8, "minecraft:end_stone_bricks")
    b.rect(tx + 1, 3, tz + 1, tx + 7, 13, tz + 7, "minecraft:air")   # hollow shaft
    for cx, cz in ((tx, tz), (tx + 8, tz), (tx, tz + 8), (tx + 8, tz + 8)):
        b.rect(cx, 1, cz, cx, 14, cz, "minecraft:obsidian")
    for band in (4, 8, 12):                                          # blackstone bands
        b.rect(tx, band, tz, tx + 8, band, tz, "minecraft:polished_blackstone")
        b.rect(tx, band, tz + 8, tx + 8, band, tz + 8, "minecraft:polished_blackstone")
        b.rect(tx, band, tz, tx, band, tz + 8, "minecraft:polished_blackstone")
        b.rect(tx + 8, band, tz, tx + 8, band, tz + 8, "minecraft:polished_blackstone")
    for wy in (6, 10):                                               # window slits
        b.rect(tx + 4, wy, tz, tx + 4, wy + 1, tz, "minecraft:black_stained_glass")
        b.rect(tx + 4, wy, tz + 8, tx + 4, wy + 1, tz + 8, "minecraft:black_stained_glass")
    b.set(tx + 4, 1, tz, "minecraft:air"); b.set(tx + 4, 2, tz, "minecraft:air")  # entry
    for ly, lcx, lcz in ((2, tx + 1, tz + 1), (4, tx + 5, tz + 1),
                         (6, tx + 1, tz + 5), (8, tx + 5, tz + 5),
                         (10, tx + 1, tz + 3), (12, tx + 5, tz + 3)):  # parkour ledges
        b.rect(lcx, ly, lcz, lcx + 1, ly, lcz + 1, "minecraft:spruce_planks")
    b.rect(tx + 4, 13, tz + 7, tx + 5, 13, tz + 7, "minecraft:spruce_planks")      # top step
    b.rect(tx - 1, 14, tz - 1, tx + 9, 14, tz + 9, "minecraft:end_stone_bricks")   # deck
    b.rect(tx + 1, 15, tz + 1, tx + 7, 15, tz + 7, "minecraft:air")
    for cx in range(tx - 1, tx + 10, 2):                                          # crenellation
        b.set(cx, 15, tz - 1, "minecraft:end_stone_brick_wall")
        b.set(cx, 15, tz + 9, "minecraft:end_stone_brick_wall")
    for cz in range(tz + 1, tz + 9, 2):
        b.set(tx - 1, 15, cz, "minecraft:end_stone_brick_wall")
        b.set(tx + 9, 15, cz, "minecraft:end_stone_brick_wall")
    b.rect(tx + 4, 1, tz + 4, tx + 4, 13, tz + 4, "mod:rot_log")     # heart-spine
    b.loot_chest(tx + 1, 15, tz + 1, "endertown_house")              # lookout cache
    for lx, lz in ((tx - 1, tz - 1), (tx + 9, tz - 1), (tx - 1, tz + 9), (tx + 9, tz + 9)):
        b.set(lx, 15, lz, "minecraft:soul_lantern")

    # --- market stalls along the west-east street
    for sx, sz, canopy in ((20, 17, "minecraft:purple_wool"), (33, 28, "minecraft:black_wool")):
        b.rect(sx, 1, sz, sx + 4, 1, sz, "minecraft:spruce_planks")  # counter
        for px, pz in ((sx, sz + 2), (sx + 4, sz + 2), (sx, sz - 1), (sx + 4, sz - 1)):
            b.rect(px, 1, pz, px, 2, pz, "minecraft:spruce_fence")
        b.rect(sx - 1, 3, sz - 1, sx + 5, 3, sz + 2, canopy)
        b.loot_chest(sx + 2, 1, sz + 1, "endertown_house")

    # --- rot trees, lamps, debris
    for tree_x, tree_z, tall in ((12, 17, 4), (11, 42, 5), (50, 26, 4), (40, 12, 6), (28, 5, 5)):
        rot_tree(b, tree_x, tree_z, tall)
    # --- its people: the town lives (Java templates place them; Bedrock spawns them)
    b.entity(24, 1, 21, "mod:preacher")          # the Preacher holds the plaza
    b.entity(13, 1, 17, "mod:townsfolk")         # sweeping near House A's door
    b.entity(40, 1, 46, "mod:townsfolk")         # loitering by the Relay Hall steps
    for lx, lz in ((40, 24), (16, 24), (28, 36), (28, 12), (35, 31), (21, 31), (35, 17), (21, 17)):
        lamp_post(b, lx, lz)
    for dx, dz, mat in ((3, 3, "mod:decay_block"), (51, 3, "minecraft:polished_blackstone"),
                        (4, 44, "mod:decay_block"), (45, 15, "mod:decayed_soil"),
                        (7, 26, "mod:decayed_soil"), (48, 6, "minecraft:obsidian"),
                        (24, 42, "mod:decayed_soil"), (36, 20, "mod:decay_block"),
                        (19, 24, "mod:decay_block"), (37, 24, "mod:decay_block"),
                        (44, 28, "minecraft:polished_blackstone"), (30, 8, "mod:decayed_soil"),
                        (22, 12, "mod:decay_block")):
        b.set(dx, 0, dz, mat)
    for sx, sz in ((4, 44), (52, 44), (51, 3)):
        b.set(sx, 1, sz, "minecraft:obsidian")                        # shard spikes
    return b

# ---------------------------------------------------------------- WATCHER SHRINE

def build_watcher_shrine():
    b = Build("watcher_shrine")
    W = D = 15
    foundation(b, W, D, "minecraft:end_stone_bricks")
    b.rect(0, 0, 0, W - 1, 0, D - 1, "minecraft:end_stone")
    # obsidian cross + purple ring
    b.rect(7, 0, 0, 7, 0, D - 1, "minecraft:obsidian")
    b.rect(0, 0, 7, W - 1, 0, 7, "minecraft:obsidian")
    b.ring(7, 0, 7, 5, "minecraft:purple_stained_glass")
    # the eye spire — where the Watcher is remembered, and watches back
    b.rect(6, 1, 6, 8, 4, 8, "minecraft:obsidian")
    b.set(7, 5, 7, "minecraft:crying_obsidian")
    for sx, sz in ((6, 7), (8, 7), (7, 6), (7, 8)):
        b.set(sx, 5, sz, "minecraft:purple_stained_glass")
    # hanging chains on the spire corners
    for cx, cz in ((5, 5), (9, 5), (5, 9), (9, 9)):
        b.rect(cx, 1, cz, cx, 3, cz, "minecraft:chain")
    # broken outer ring (end-brick stubs)
    pts = [(7, 1), (11, 2), (13, 7), (11, 12), (7, 13), (3, 12), (1, 7), (3, 2)]
    for idx, (px, pz) in enumerate(pts):
        top = 3 if idx % 3 == 0 else 2
        b.rect(px, 1, pz, px, top, pz, "minecraft:end_stone_brick_wall")
    # vigil lanterns
    for lx, lz in ((3, 3), (11, 3), (3, 11), (11, 11)):
        b.set(lx, 1, lz, "minecraft:end_stone_bricks")
        b.set(lx, 2, lz, "minecraft:soul_lantern")
    b.loot_chest(7, 1, 11, "watcher_shrine")
    for dx, dz in ((2, 6), (12, 8), (5, 12), (9, 2)):
        b.set(dx, 0, dz, "mod:decay_block")
    return b

# ---------------------------------------------------------------- MAINFRAME RUIN

def build_mainframe_ruin():
    b = Build("mainframe_ruin")
    W, D = 22, 18
    foundation(b, W, D, "mod:decayed_stone")
    b.rect(0, 0, 0, W - 1, 0, D - 1, "mod:decayed_stone")
    # decay bloom on the floor + collapse holes (deterministic)
    for x in range(W):
        for z in range(D):
            hsh = (x * 31 + z * 17) % 23
            if hsh in (0, 5):
                b.set(x, 0, z, "mod:decayed_soil")
            elif hsh == 9:
                b.set(x, 0, z, "mod:decay_block")
            elif hsh == 13 and 2 < x < W - 3 and 2 < z < D - 3:
                b.set(x, 0, z, "minecraft:air")
    # broken perimeter of mainframe frames (~80% kept), bulked corners
    for x in range(W):
        for z in (0, D - 1):
            for y in range(1, 5):
                if (x * 7 + y * 3 + z) % 5 != 0:
                    b.set(x, y, z, "mod:mainframe_frame")
    for z in range(D):
        for x in (0, W - 1):
            for y in range(1, 5):
                if (z * 7 + y * 3 + x) % 5 != 0:
                    b.set(x, y, z, "mod:mainframe_frame")
    for cx, cz in ((0, 0), (W - 2, 0), (0, D - 2), (W - 2, D - 2)):
        b.rect(cx, 1, cz, cx + 1, 5, cz + 1, "mod:mainframe_frame")
    # clinging ceiling fragments
    for fx, fz in ((3, 1), (W - 5, 1), (1, D - 4), (W - 4, D - 4)):
        b.rect(fx, 5, fz, fx + 2, 5, fz + 2, "mod:mainframe_frame")
    # the dead heart: pedestal, silent command block, two terminals that never woke
    b.rect(10, 1, 8, 12, 1, 10, "mod:mainframe_frame")
    b.set(11, 2, 9, "mod:corrupted_command_block")
    for px, pz in ((10, 8), (12, 8), (10, 10), (12, 10)):
        b.set(px, 2, pz, "mod:decay_block")
    b.set(11, 1, 4, "mod:terminal")
    b.set(11, 1, 13, "mod:terminal")
    # debris
    for rx, rz, hh in ((3, 3, 3), (18, 4, 2), (4, 14, 3), (17, 13, 2)):
        for t in range(1, hh + 1):
            b.set(rx, t, rz, "mod:rot_log")
    for ox, oz in ((7, 6), (15, 7), (8, 12), (14, 12), (6, 9), (16, 10)):
        b.set(ox, 1, oz, "minecraft:obsidian")
    b.loot_chest(2, 1, 2, "mainframe_ruin")
    b.loot_chest(19, 1, 15, "mainframe_ruin")
    return b

# ---------------------------------------------------------------- RIFT OBELISK

def build_rift_obelisk():
    b = Build("rift_obelisk")
    W = D = 9
    b.rect(1, -2, 1, W - 2, -2, D - 2, "minecraft:end_stone_bricks")
    b.rect(0, -1, 0, W - 1, -1, D - 1, "minecraft:end_stone_bricks")
    b.rect(0, 0, 0, W - 1, 0, D - 1, "minecraft:end_stone")
    # floor inlay cross
    b.rect(4, 0, 0, 4, 0, D - 1, "minecraft:purple_stained_glass")
    b.rect(0, 0, 4, W - 1, 0, 4, "minecraft:purple_stained_glass")
    # obsidian ring with a south entry gap
    for x in range(W):
        for z in (0, D - 1):
            if x != 4:
                b.set(x, 1, z, "minecraft:obsidian")
    for z in range(D):
        for x in (0, W - 1):
            b.set(x, 1, z, "minecraft:obsidian")
    # pedestal + needle with exposed crying core
    b.rect(3, 1, 3, 5, 1, 5, "minecraft:obsidian")
    for y in range(2, 12):
        b.set(4, y, 4, "minecraft:crying_obsidian" if y in (5, 9) else "minecraft:obsidian")
    b.set(4, 12, 4, "minecraft:crying_obsidian")
    for gx, gz in ((3, 2), (5, 2), (2, 3), (6, 3), (2, 5), (6, 5), (3, 6), (5, 6)):
        b.set(gx, 2, gz, "minecraft:purple_stained_glass")
        b.set(gx, 3, gz, "minecraft:purple_stained_glass")
    # chain pylons
    for cx, cz in ((2, 2), (6, 2), (2, 6), (6, 6)):
        b.rect(cx, 1, cz, cx, 2, cz, "minecraft:chain")
    # entry lanterns + the rift scar at the obelisk's foot (return-trip network)
    b.set(3, 1, 7, "minecraft:soul_lantern")
    b.set(5, 1, 7, "minecraft:soul_lantern")
    b.set(4, 1, 1, "mod:rift_portal")
    return b

# ---------------------------------------------------------------- Bedrock JS emission


# ---------------------------------------------------------------- WATCHER CAMP

def build_watcher_camp():
    """A Watcher-cult observation camp: tents, lens rig, and one very patient watcher.
    'It watches the quarantine. Someone claimed it first.'"""
    b = Build("watcher_camp")
    W, D = 18, 15
    foundation(b, W, D, "mod:decayed_soil")
    b.rect(0, 0, 0, W - 1, 0, D - 1, "mod:decayed_soil")
    # bloom: decay patches across the camp plat
    for x in range(W):
        for z in range(D):
            if (x * 13 + z * 29) % 19 == 3:
                b.set(x, 0, z, "mod:decay_block")
    # perimeter stones (low broken wall)
    for x in (2, 5, 9, 14, 16):
        b.rect(x, 1, 1, x, 2, 1, "mod:decayed_stone")
        b.rect(x, 1, D - 2, x, 2, D - 2, "mod:decayed_stone")
    for z in (3, 7, 11):
        b.rect(1, 1, z, 1, 2, z, "mod:decayed_stone")
        b.rect(W - 2, 1, z, W - 2, 2, z, "mod:decayed_stone")
    # three cult tents: wool walls, open fronts
    for i, (tx, tz) in enumerate(((4, 4), (12, 4), (8, 9))):
        wool = "minecraft:black_wool" if i == 1 else "minecraft:purple_wool"
        b.rect(tx, 1, tz, tx + 3, 1, tz + 2, wool)
        b.rect(tx, 2, tz, tx + 3, 2, tz, wool)          # sloped roof rows
        b.rect(tx, 2, tz + 2, tx + 3, 2, tz + 2, wool)
        b.rect(tx, 3, tz, tx + 3, 3, tz + 2, "minecraft:chain")  # ridge line, kits as chains
    # the lens rig: a tripod spire watching the quarantine line
    cx, cz = 8, 4
    for lx, lz in ((cx - 2, cz - 2), (cx + 2, cz - 2), (cx + 2, cz + 2), (cx - 2, cz + 2)):
        b.rect(lx, 1, lz, lx, 3, lz, "minecraft:chain")
    b.rect(cx, 1, cz, cx, 5, cz, "minecraft:obsidian")
    b.set(cx, 6, cz, "minecraft:crying_obsidian")
    b.set(cx, 6, cz - 1, "minecraft:purple_stained_glass")
    # vigil: soul lanterns at the gate, banners nobody counts anymore
    for lx, lz in ((3, 7), (15, 7)):
        b.set(lx, 1, lz, "mod:decayed_stone")
        b.set(lx, 2, lz, "minecraft:soul_lantern")
    b.stand_banner(1, 3, 1, base="black")
    b.stand_banner(W - 2, 3, D - 2, base="black")
    # the camp: one watcher on the rig, two taken pacing, a hoarded cache
    b.entity(cx, 6, cz, "mod:watcher")
    b.entity(12, 1, 10, "mod:the_taken")
    b.entity(4, 1, 11, "mod:the_taken")
    b.loot_chest(9, 1, 12, "watcher_camp")
    b.set(10, 1, 12, "minecraft:lantern")
    return b

# ---------------------------------------------------------------- ROT CATHEDRAL

def build_rot_cathedral():
    """The Rot Cathedral: where the taken used to be people, and the preacher still
    calls them by name. Nave of rot-log colonnades under a half-eaten roof."""
    b = Build("rot_cathedral")
    W, D = 21, 27
    foundation(b, W, D, "mod:decayed_stone")
    b.rect(0, 0, 0, W - 1, 0, D - 1, "mod:decayed_stone")
    mid = W // 2
    # nave carpet of decayed soil with decay blooms
    for x in range(2, W - 2):
        for z in range(2, D - 2):
            if (x * 7 + z * 11) % 17 == 4:
                b.set(x, 0, z, "mod:decay_block")
    # rot-log colonnades, every third one snapped mid-trunk
    for i, z in enumerate(range(4, D - 4, 4)):
        for x in (5, W - 6):
            full = (i % 3) != 1
            h = 6 if full else 3
            b.rect(x, 1, z, x, h, z, "mod:rot_log")
            if full:
                b.set(x, 7, z, "mod:decayed_stone")
    # broken roof: ribs across the nave, gaps where the sky got in
    for z in range(4, D - 4, 4):
        if z % 8 != 4:  # the missing ribs were eaten first
            for x in range(5, W - 5):
                y = 7 + abs(mid - x) // 4
                b.set(x, y, z, "mod:rot_log")
    # outer walls: low and crumbling
    for x in range(1, W - 1):
        h = 2 if x % 3 else 3
        b.rect(x, 1, 1, x, h, 1, "mod:decayed_stone")
    for z in range(1, D - 1):
        b.rect(1, 1, z, 1, 2, z, "mod:decayed_stone")
        b.rect(W - 2, 1, z, W - 2, 2, z, "mod:decayed_stone")
    # the altar: obsidian dais, decay mound, crying eye, and the preacher
    b.rect(mid - 1, 1, D - 6, mid + 1, 1, D - 4, "minecraft:obsidian")
    b.set(mid, 2, D - 5, "mod:decay_block")
    b.set(mid, 3, D - 5, "minecraft:crying_obsidian")
    b.disc(mid, 1, D - 5, 3, "mod:decayed_soil")
    # the pews, half of them gone
    for z in range(8, D - 8, 3):
        for x in (7, mid + 2):
            if (z + x) % 5 != 0:
                b.rect(x, 1, z, x + 2, 1, z, "mod:rot_log")
    # vigil banners on the columns that survived
    b.stand_banner(4, 1, D - 7, base="purple")
    b.stand_banner(W - 5, 1, D - 7, base="purple")
    # the congregation that stayed
    b.entity(mid, 1, D - 8, "mod:preacher")
    b.entity(6, 1, 8, "mod:the_taken")
    b.entity(W - 7, 1, 10, "mod:the_taken")
    b.entity(mid - 3, 1, 14, "mod:the_taken")
    b.entity(mid + 3, 1, 16, "mod:storm_mite")
    b.entity(mid + 1, 1, 6, "mod:storm_mite")
    # the tithe chest and the reliquary
    b.loot_chest(mid - 2, 2, D - 5, "rot_cathedral")
    b.loot_chest(mid + 2, 1, 9, "rot_cathedral_reliquary")
    for lx, lz in ((5, 6), (W - 6, 6), (mid, 2)):
        b.set(lx, 1, lz, "minecraft:soul_lantern")
    return b

BUILDS_ORDER = ("endertown", "watcher_shrine", "mainframe_ruin", "rift_obelisk",
                "watcher_camp", "rot_cathedral")

def emit_bedrock_js(builds):
    lines = [
        "// GENERATED by tools/generate_structures.py — do not hand-edit.",
        "// Block tables for the Decayed Reality builds (Endertown, the Watcher Shrine,",
        "// the Mainframe Ruin, the Rift Obelisk). Bedrock add-ons cannot register",
        "// worldgen structures, so the behaviour engine stamps these out instead.",
        "// Block states are all vanilla defaults by design (see generator notes).",
        "export const BUILDS = {",
    ]
    for name in BUILDS_ORDER:
        b = builds[name]
        norm, chests, size, ground_y, entities = b._normalize()
        ids = []
        id_index = {}
        rows = []
        for (x, y, z), (bid, props, nbt) in sorted(norm.items()):
            if "_wall_banner" in bid or bid.endswith("_banner"):
                bed = bid.replace("_wall_banner", "_wool").replace("_banner", "_wool")
            else:
                bed = bedrock_id(bid)
            if bed not in id_index:
                id_index[bed] = len(ids)
                ids.append(bed)
            rows.append(f"[{x},{y},{z},{id_index[bed]}]")
        chest_rows = ", ".join(f"[{x},{y},{z},\"{loot}\"]" for x, y, z, loot in chests)
        lines.append(f"  {name}: {{")
        lines.append(f"    size: [{size[0]},{size[1]},{size[2]}], groundY: {ground_y},")
        lines.append(f"    ids: {ids!r},".replace("'", '"'))
        # chunk rows for readability (60 blocks per line)
        lines.append("    blocks: [")
        for k in range(0, len(rows), 60):
            lines.append("      " + ",".join(rows[k:k + 60]) + ",")
        lines.append("    ],")
        lines.append(f"    chests: [{chest_rows}],")
        ent_rows = ", ".join(f"[{x},{y},{z},\"{bedrock_id(eid)}\"]" for x, y, z, eid in entities)
        lines.append(f"    entities: [{ent_rows}],")
        lines.append("  },")
    lines.append("};")
    lines.append("")
    path = os.path.join(BEDROCK_SCRIPTS, "builds_data.js")
    with open(path, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines))
    return path

# ---------------------------------------------------------------- Endertown Core icon

def emit_endertown_core_icon():
    """16x16 item icon: a tiny amethyst memorial spire ringed by banner bits."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    OBS = (18, 10, 28, 255); OBS_L = (34, 20, 52, 255)
    CRY = (150, 66, 196, 255); CRY_L = (203, 128, 236, 255)
    PUR = (110, 40, 172, 255); MAG = (208, 84, 224, 255); GLD = (235, 186, 98, 255)
    # ground strip
    for x in range(2, 14):
        px[x, 13] = OBS_L
    # spire
    for y in range(5, 13):
        for x in (7, 8):
            px[x, y] = OBS
    for y in range(3, 6):
        px[7, y] = OBS; px[8, y] = OBS
    px[7, 2] = CRY; px[8, 2] = CRY_L
    px[7, 6] = CRY; px[8, 6] = CRY_L
    # flanking banner poles
    for y in range(7, 13):
        px[4, y] = OBS_L; px[11, y] = OBS_L
    for y in range(7, 11):
        for x in (4, 5):
            px[x - 1, y] = PUR
        for x in (11, 12):
            px[x + 1 if x == 11 else x, y] = MAG if y % 2 == 0 else PUR
    px[3, 11] = MAG; px[13, 11] = MAG
    # glow dots
    px[6, 4] = GLD; px[9, 4] = GLD
    path = os.path.join(RP_ITEM_TEX, "endertown_core.png")
    img.save(path)
    return path

# ---------------------------------------------------------------- main

def main():
    os.makedirs(JAVA_STRUCT_DIR, exist_ok=True)
    builds = {
        "endertown": build_endertown(),
        "watcher_shrine": build_watcher_shrine(),
        "mainframe_ruin": build_mainframe_ruin(),
        "rift_obelisk": build_rift_obelisk(),
        "watcher_camp": build_watcher_camp(),
        "rot_cathedral": build_rot_cathedral(),
    }
    print("== Java structure NBT (gzipped) ==")
    for name in BUILDS_ORDER:
        b = builds[name]
        path, count, raw = b.emit_java()
        _, _, size, ground_y, _ = b._normalize()
        disk = os.path.getsize(path)
        print(f"  {name:16s} blocks={count:5d} size={size[0]}x{size[1]}x{size[2]} "
              f"ground_y={ground_y} banners={b.banner_count:3d} chests={len(b.chests)} "
              f"nbt_raw={raw/1024:.0f}KB gz={disk/1024:.0f}KB")
        # sanity: no duplicate placements (dict keys are unique positions)
        assert len(b.blocks) == len({p for p in b.blocks}), "duplicate positions"
    js = emit_bedrock_js(builds)
    print(f"== Bedrock script data ==\n  {js} ({os.path.getsize(js)/1024:.0f}KB)")
    icon = emit_endertown_core_icon()
    print(f"== Endertown Core icon ==\n  {icon}")

if __name__ == "__main__":
    main()
