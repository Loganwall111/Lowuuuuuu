#!/usr/bin/env python3
"""Facilities, cities, and the crater — building the v1.5 'places you long for hates'

Structures shipped (Java worldgen):
  summon_crater   — black-glass crater with tilted pillars and the corrupted heart
  epa_facility    — EPA: teal terminal hall + sealed vault framing
  tazo_town       — cozy yet overpopulated teal towers, plus tazos
  boom_town       — corrupted-melancholy living compound of broken slabs + citizens
  limitless_spaces — the store that goes forever (far enough)
  event_horizon   — nothing escapes, not even the structure's own idea of exiting

NBT writer: minimal valid Structure Template ("structure" tag root).
"""

from __future__ import annotations

import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "java-mod" / "src" / "main" / "resources" / "data" / "devouring_storms"

# ---------------------------------------------------------------------------- mini-NBT (big-endian)

def nbt_compound(fields: dict) -> bytes:
    out = bytearray()
    for name, (tag, value) in fields.items():
        nb = name.encode()
        out.append(tag)
        out += struct.pack(">H", len(nb)) + nb
        out += pack_tag(tag, value)
    out.append(0)
    return bytes(out)

def pack_tag(tag: int, value) -> bytes:
    if tag == 1:
        return struct.pack(">b", value)
    if tag == 3:
        return struct.pack(">i", value)
    if tag == 5:
        return struct.pack(">f", value)
    if tag == 6:
        return struct.pack(">d", value)
    if tag == 7:
        return struct.pack(">h", value)
    if tag == 8:
        nb = value.encode()
        return struct.pack(">H", len(nb)) + nb
    if tag == 9:                                       # list
        if not value:                                  # empty list -> type 0
            return struct.pack(">bi", 0, 0)
        subtype = value[0][0]
        out = struct.pack(">bi", subtype, len(value))
        b = bytearray(out)
        for _, v in value:
            b += pack_tag(subtype, v)
        return bytes(b)
    if tag == 10:
        return nbt_compound(value)
    if tag == 11:                                      # int array
        out = struct.pack(">i", len(value))
        return struct.pack(">i", len(value)) + b"".join(struct.pack(">i", v) for v in value)
    if tag == 12:                                      # long array
        return struct.pack(">i", len(value)) + b"".join(struct.pack(">q", v) for v in value)
    raise ValueError(f"unhandled nbt tag {tag}")

def structure_nbt(size: tuple[int, int, int], blocks: list[dict], entities: list[dict]) -> bytes:
    palette: list[dict] = []
    palette_idx: dict[str, int] = {}

    def palette_id(block: str, props: dict | None = None) -> int:
        key = block + ("|" + ",".join(f"{k}={v}" for k, v in sorted((props or {}).items())) if props else "")
        if key not in palette_idx:
            entry: dict = {"Name": (8, block)}
            if props:
                entry["Properties"] = (10, {k: (8, str(v)) for k, v in props.items()})
            palette.append(entry)
            palette_idx[key] = len(palette) - 1
        return palette_idx[key]

    bl = []
    for b in blocks:
        entry: dict = {"state": (3, palette_id(b["block"], b.get("props"))),
                       "pos": (9, [(3, b["x"]), (3, b["y"]), (3, b["z"])])}
        if "nbt" in b:
            entry["nbt"] = (10, b["nbt"])
        bl.append((10, entry))

    el = []
    for e in entities:
        nbt: dict = {"id": (8, e["id"])}
        for k, v in (e.get("fields") or {}).items():
            nbt[k] = (8, v) if isinstance(v, str) else (3, v)
        el.append((10, {"pos": (9, [(6, e["x"]), (6, e["y"]), (6, e["z"])]),
                        "blockPos": (9, [(3, int(e["x"])), (3, int(e["y"])), (3, int(e["z"]))]),
                        "nbt": (10, nbt)}))

    return nbt_compound({
        "size": (9, [(3, size[0]), (3, size[1]), (3, size[2])]),
        "palette": (9, [(10, p) for p in palette]),
        "blocks": (9, bl),
        "entities": (9, el),
    })

# ---------------------------------------------------------------------------- geometry helpers

B = lambda block, x, y, z, **kw: dict(block=block, x=x, y=y, z=z, **{k: v for k, v in kw.items()})

class Canvas:
    def __init__(self):
        self.blocks: list[dict] = []
        self.entities: list[dict] = []

    def fill(self, block: str, x0: int, y0: int, z0: int, x1=None, y1=None, z1=None, props=None):
        if x1 is None: x1, y1, z1 = x0, y0, z0
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    d = dict(block=block, x=x, y=y, z=z)
                    if props: d["props"] = props
                    self.blocks.append(d)

    def box(self, block: str, x0, y0, z0, x1, y1, z1, hollow=True):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    edge = x in (x0, x1) or y in (y0, y1) or z in (z0, z1)
                    if not hollow or edge:
                        self.blocks.append(dict(block=block, x=x, y=y, z=z))

    def solv_disk(self):
        # dedupe: later assignment wins (last paint overwrites its own voxel physically)
        self.blocks = list({(b["x"], b["y"], b["z"]): b for b in self.blocks}.values())

DS = "devouring_storms"

# ---------------------------------------------------------------------------- the builds

def build_summon_crater() -> Canvas:
    """The only place that answers the rocket key. Where the Lord said it can warp reality."""
    c = Canvas()
    # disc of obsidian ringed by glitch-block debris, r=22
    for x in range(-22, 23):
        for z in range(-22, 23):
            d = (x * 1.13) ** 2 + (z * 1.0) ** 2
            if d <= 484:
                if d < 16:
                    c.fill(DS + ":decay_block", x + 26, 2, z + 26)  # the wound itself
                elif d < 256:
                    c.fill(DS + ":decayed_stone", x + 26, 1, z + 26)
                    if (x * 7 + z * 13) % 19 == 0:
                        c.fill(DS + ":glitch_block", x + 26, 2, z + 26)
                else:
                    c.fill(DS + ":decayed_stone", x + 26, 1, z + 26)
    # tilted pillar fingers around the rim
    for ang, h in ((0, 9), (2, 6), (4, 8), (6, 5), (8, 10), (10, 7), (12, 8), (14, 6)):
        import math
        px, pz = 26 + int(20 * math.cos(ang * 0.448796)), 26 + int(20 * math.sin(ang * 0.448796))
        for i in range(h):
            c.fill(DS + ":mainframe_frame", px + i // 4, 2 + i, pz)
    # the heart: corrupted command block on a 4x4 mainframe plinth
    c.box(DS + ":mainframe_frame", 24, 2, 24, 28, 2, 28, hollow=False)
    c.fill(DS + ":corrupted_command_block", 26, 3, 26)
    c.entities.append(dict(id=DS + ":monstrosity", x=20.5, y=3, z=20.5))
    return c


def build_epa_facility() -> Canvas:
    """E.P.A. — Environmental Persistence Authority. Reliable, friendly, ergonomic,
    and entirely uninterested in what they found."""
    c = Canvas()
    c.solv_disk()
    # hall slab
    c.fill(DS + ":decayed_stone", 0, 0, 0, 30, 0, 22)
    c.box(DS + ":mainframe_frame", 0, 0, 0, 30, 7, 22, hollow=True)
    c.fill("minecraft:air", 0, 1, 0, 30, 6, 22)
    c.fill(DS + ":decayed_jukebox", 3, 1, 3)
    # TEAL terminal bank with whispering audio log shelves
    for x in range(5, 27, 4):
        c.fill(DS + ":mainframe_frame", x, 1, 2, x + 1, 3, 2)
        c.fill(DS + ":decayed_jukebox", x, 4, 2)
    # offices: decayed dust carpet made of nothing
    c.fill(DS + ":rot_log", 2, 1, 8, 28, 1, 21) if False else None
    # vault at the back, sealed, vaulting
    c.box(DS + ":sealed_vault", 24, 1, 14, 29, 6, 20, hollow=True)
    c.fill(DS + ":sealed_vault", 26, 3, 17)
    # broken roof slats letting the weather ask questions
    for x in range(2, 30, 3):
        c.fill(DS + ":rot_log", x, 7, 3, x, 7, 20) if False else None
    # staff: researchers at stations, whispering about quarantine feasibility
    c.entities += [dict(id=DS + ":researcher", x=6.5, y=1, z=4.5),
                   dict(id=DS + ":researcher", x=14.5, y=1, z=5.5),
                   dict(id=DS + ":researcher", x=21.5, y=1, z=6.5),
                   dict(id=DS + ":researcher", x=15.5, y=1, z=15.5)]
    return c


def build_tazo_town() -> Canvas:
    """Teal towers where every tazo knows its address. They all know yours too."""
    c = Canvas()
    c.fill(DS + ":decayed_stone", 0, 0, 0, 26, 0, 24)
    # four teal towers
    for ox, oz, h in ((3, 3, 9), (16, 4, 11), (6, 14, 8), (18, 15, 10)):
        c.box(DS + ":mainframe_frame", ox, 1, oz, ox + 5, h, oz + 5, hollow=True)
        c.fill(DS + ":decayed_jukebox", ox + 2, h + 1, oz + 2)
    # lantern orchards
    for x in range(2, 26, 6):
        c.fill(DS + ":mainframe_frame", x, 1, 12, x, 4, 12)
        c.fill(DS + ":decayed_jukebox", x, 5, 12)
    # tazos and their favorite people: tazos
    for i in range(6):
        x = 4 + (i * 4) % 18
        z = 5 + (i * 7) % 16
        c.entities.append(dict(id=DS + ":tazo", x=x + 0.5, y=1, z=z + 0.5,
                               fields={"TazoVariant": ["teal", "rose", "dusk", "teal", "ivory", "dusk"][i],
                                       "CustomName": f'"Tazo No. {i + 1}"'}))
    return c


def build_boom_town() -> Canvas:
    """Sixty-six kind-of-melancholy buildings owes to entropy with interest. The monolith
    stands. It is not decorative."""
    c = Canvas()
    import random
    random.seed(76)
    c.fill(DS + ":decayed_stone", 0, 0, 0, 34, 0, 30)
    # blocks of broken buildings
    for i in range(9):
        ox, oz = 2 + (i % 3) * 11 + random.randint(-1, 2), 2 + (i // 3) * 10 + random.randint(-1, 2)
        h = random.randint(4, 9)
        c.box(DS + ":rot_log" if random.random() < 0.4 else DS + ":mainframe_frame",
              ox, 1, oz, ox + 7, h, oz + 7, hollow=True)
        # glitch seams in at least one wall
        gx = ox + random.randint(1, 6)
        for gy in range(2, h):
            if random.random() < 0.5:
                c.fill(DS + ":glitch_block", gx, gy, oz)
        # void residue interior scorch
        c.fill(DS + ":glitch_block" if random.random() < 0.35 else DS + ":decay_block",
               ox + 3, 1, oz + 3)
    # central monolith: one corrupted command block where the deliberations were reached
    c.box(DS + ":mainframe_frame", 16, 1, 14, 18, 6, 16, hollow=False)
    c.fill(DS + ":corrupted_command_block", 17, 7, 15)
    # the seventy-six variants (we ship nine; the rest declined via spawn eggs)
    names = ["Gargler", "Pinter", "Decaying Sir", "Broadcaster No. 4", "The Mrs.",
             "Cart Regret", "Glitch Warden", "Statue (Applied)", "Floor Knocker"]
    for i, n in enumerate(names):
        x = 3 + (i * 4) % 28
        z = 4 + (i * 9) % 24
        c.entities.append(dict(id=DS + ":monstrosity_child" if False else DS + ":monstrosity",
                               x=x + 0.5, y=1, z=z + 0.5, fields={"CustomName": f'"{n}"'}))
    for i in range(4):
        c.entities.append(dict(id=DS + ":cart_shopper", x=5 + i * 7 + 0.5, y=1, z=8 + i * 5 + 0.5))
    return c


def build_limitless_spaces() -> Canvas:
    """The most endless warehouse in the Cosmic Abyss. Aisles repeat softly.
    Mind the carts. The carts do not mind you."""
    c = Canvas()
    import random
    random.seed(44)
    c.fill(DS + ":decayed_stone", 0, 0, 0, 30, 0, 26)
    c.box(DS + ":mainframe_frame", 0, 0, 0, 30, 5, 26, hollow=True)
    c.fill("minecraft:air", 0, 1, 0, 30, 4, 26)
    # shelves: towers of crate stacks, repeating at eerie intervals
    for x in range(4, 29, 6):
        for z in range(3, 25, 5):
            h = random.randint(2, 4)
            for y in range(1, h + 1):
                c.fill(DS + ":crate_block", x, y, z)
                c.fill(DS + ":crate_block", x + 1, y, z)
                if random.random() < 0.3:
                    c.fill(DS + ":glitch_block", x, h + 1, z)
    # dead register lane of crate tills
    for x in range(2, 30, 10):
        c.fill(DS + ":mainframe_frame", x, 1, 24, x + 2, 2, 24)
        c.fill(DS + ":vhs_jukebox", x + 1, 3, 24)
    # the aisle ladder that goes nowhere in particular yet
    c.fill(DS + ":mainframe_frame", 15, 1, 1, 15, 4, 1)
    # the staff: six cart shoppers and one monstruous floor manager
    for i in range(6):
        c.entities.append(dict(id=DS + ":cart_shopper", x=5 + (i * 4) % 24 + 0.5, y=1, z=6 + (i * 6) % 16 + 0.5))
    c.entities.append(dict(id=DS + ":monstrosity", x=15.5, y=1, z=13.5,
                           fields={"CustomName": '"Floor Manager"'}))
    return c


def build_event_horizon() -> Canvas:
    """The planet NEXUS holds the broadcast; the broadcast holds the hole.
    The obsidian ring is load-bearing to nobody."""
    c = Canvas()
    import math
    c.solv_disk()
    # great obsidian ring r=14
    r0, r1, cy = 12, 15, 20
    for x in range(-16, 17):
        for z in range(-16, 17):
            d = math.hypot(x, z)
            if r0 <= d <= r1:
                c.fill("minecraft:obsidian", x + 0, 1, z + 0)
    # markers: rift seams like spokes
    for ang in (0, 1.57, 3.14, 4.71):
        x, z = int(r1 * math.cos(ang)), int(r1 * math.sin(ang))
        c.fill(DS + ":rift_portal", x, 2, z)
    # the void maw presides, pulled even more confidently towards everything
    c.entities.append(dict(id=DS + ":void_maw", x=0.5, y=6, z=0.5))
    return c


# ---------------------------------------------------------------------------- worldgen json set

STRUCTS = {
    "summon_crater":  dict(build=build_summon_crater, salt=841137, spacing=48, separation=24,
                           biomes="#minecraft:is_overworld", y=24, terrain=True),
    "epa_facility":   dict(build=build_epa_facility, salt=841192, spacing=44, separation=22,
                           biomes="#minecraft:is_overworld", y=0, terrain=True),
    "tazo_town":      dict(build=build_tazo_town, salt=841203, spacing=46, separation=23,
                           biomes="#minecraft:is_overworld", y=0, terrain=False),
    "boom_town":      dict(build=build_boom_town, salt=841214, spacing=52, separation=26,
                           biomes=["devouring_storms:decayed_wastes"], y=1, terrain=False),
    "limitless_spaces": dict(build=build_limitless_spaces, salt=841225, spacing=34, separation=17,
                             biomes=["devouring_storms:decayed_wastes"], y=0, terrain=False),
    "event_horizon":  dict(build=build_event_horizon, salt=841236, spacing=80, separation=40,
                           biomes=["devouring_storms:decayed_wastes"], y=1, terrain=False),
}

def write_structure_files():
    for name, meta in STRUCTS.items():
        canvas = meta["build"]()
        canvas.solv_disk()
        mx = max((b["x"] for b in canvas.blocks), default=0)
        my = max((b["y"] for b in canvas.blocks), default=0)
        mz = max((b["z"] for b in canvas.blocks), default=0)
        bx = min((b["x"] for b in canvas.blocks), default=0)
        bz = min((b["z"] for b in canvas.blocks), default=0)
        # shift to non-negative
        if bx < 0 or bz < 0:
            for b in canvas.blocks:
                b["x"] -= bx; b["z"] -= bz
            for e in canvas.entities:
                e["x"] -= bx; e["z"] -= bz
            mx -= bx; mz -= bz
        nbt = structure_nbt((mx + 1, my + 1, mz + 1), canvas.blocks, canvas.entities)
        (DATA / "structure").mkdir(parents=True, exist_ok=True)
        (DATA / "structure" / f"{name}.nbt").write_bytes(nbt)

        biomes = meta["biomes"]
        biomes_val = biomes if isinstance(biomes, list) else biomes
        structure = {
            "type": "minecraft:jigsaw",
            "start_pool": f"devouring_storms:{name}",
            "size": 1,
            "biomes": biomes_val,
            "step": "surface_structures",
            "start_height": {"absolute": meta["y"]},
            "project_start_to_heightmap": "WORLD_SURFACE_WG" if meta["terrain"] else None,
            "use_expansion_hack": False,
            "max_distance_from_center": 90,
            "terrain_adaptation": "beard_thin",
            "spawn_overrides": {},
        }
        structure = {k: v for k, v in structure.items() if v is not None}
        pool = {
            "fallback": "minecraft:empty",
            "elements": [{
                "weight": 1,
                "element": {
                    "element_type": "minecraft:single_pool_element",
                    "projection": "rigid",
                    "location": f"devouring_storms:{name}",
                    "processors": "minecraft:empty",
                },
            }],
        }
        sset = {
            "structures": [{"structure": f"devouring_storms:{name}", "weight": 1}],
            "placement": {"type": "minecraft:random_spread",
                          "spacing": meta["spacing"], "separation": meta["separation"],
                          "salt": meta["salt"],
                          "spread_type": "linear"},
        }
        for sub, data in (("worldgen/structure", structure),
                          ("worldgen/template_pool", pool),
                          ("worldgen/structure_set", sset)):
            d = DATA / sub
            d.mkdir(parents=True, exist_ok=True)
            with open(d / f"{name}.json", "w") as f:
                json.dump(data, f, indent=2)
        print(f"  structure/{name}: {len(canvas.blocks)} blocks, {len(canvas.entities)} entities")


if __name__ == "__main__":
    write_structure_files()
