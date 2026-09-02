#!/usr/bin/env python3
"""Convert legacy MCEdit .schematic (numeric block IDs) -> 1.20.1 .nbt structures.

The Minecraft: Story Mode recreation ships 181 classic schematics using the
pre-1.13 numeric ID + data-value system. This maps them onto modern block
states and writes vanilla structure NBT so a datapack can place them in-world.
"""
import gzip, struct, os, sys, glob, json
from collections import Counter

SRC = "/home/user/dl/schem/Schematics"
OUT = "/home/user/UltimateWitherStorm/datapack/data/mcsm/structures"
os.makedirs(OUT, exist_ok=True)

# ----------------------------------------------------------------- NBT reader
class R:
    def __init__(s, b): s.b, s.i = b, 0
    def u1(s):
        v = s.b[s.i]; s.i += 1; return v
    def i2(s):
        v = struct.unpack_from(">h", s.b, s.i)[0]; s.i += 2; return v
    def i4(s):
        v = struct.unpack_from(">i", s.b, s.i)[0]; s.i += 4; return v
    def st(s):
        n = struct.unpack_from(">H", s.b, s.i)[0]; s.i += 2
        v = s.b[s.i:s.i + n].decode("utf8", "replace"); s.i += n; return v


def parse(r, t):
    if t == 1: return r.u1()
    if t == 2: return r.i2()
    if t == 3: return r.i4()
    if t == 4:
        v = struct.unpack_from(">q", r.b, r.i)[0]; r.i += 8; return v
    if t == 5:
        v = struct.unpack_from(">f", r.b, r.i)[0]; r.i += 4; return v
    if t == 6:
        v = struct.unpack_from(">d", r.b, r.i)[0]; r.i += 8; return v
    if t == 7:
        n = r.i4(); v = r.b[r.i:r.i + n]; r.i += n; return v
    if t == 8: return r.st()
    if t == 9:
        it = r.u1(); n = r.i4(); return [parse(r, it) for _ in range(n)]
    if t == 10:
        o = {}
        while True:
            tt = r.u1()
            if tt == 0: break
            o[r.st()] = parse(r, tt)
        return o
    if t == 11:
        n = r.i4(); v = struct.unpack_from(">%di" % n, r.b, r.i); r.i += 4 * n; return list(v)
    if t == 12:
        n = r.i4(); v = struct.unpack_from(">%dq" % n, r.b, r.i); r.i += 8 * n; return list(v)
    raise ValueError(f"tag {t}")


def read_schem(path):
    d = gzip.open(path, "rb").read()
    r = R(d)
    assert r.u1() == 10
    r.st()
    return parse(r, 10)


# ----------------------------------------------------------------- NBT writer
def w_str(s):
    b = s.encode("utf8"); return struct.pack(">H", len(b)) + b


def w_tag(name, tid, payload):
    return bytes([tid]) + w_str(name) + payload


def w_int(v): return struct.pack(">i", v)


def w_list(tid, items):
    return bytes([tid]) + struct.pack(">i", len(items)) + b"".join(items)


def w_compound(pairs):
    return b"".join(pairs) + b"\x00"


# ------------------------------------------------------- legacy -> modern map
WOOD = ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak"]
COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
          "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
STONEB = {0: "stone_bricks", 1: "mossy_stone_bricks", 2: "cracked_stone_bricks", 3: "chiseled_stone_bricks"}


def stair(kind, data):
    facing = ["east", "west", "south", "north"][data & 3]
    half = "top" if data & 4 else "bottom"
    return kind, {"facing": facing, "half": half}


def slab(kind, data):
    return kind, {"type": "top" if data & 8 else "bottom"}


def log(kind, data):
    ax = {0: "y", 4: "x", 8: "z", 12: "y"}[(data & 12)]
    return kind, {"axis": ax}


def M(bid, data):
    """Return (block_name, properties_dict)."""
    P = {}
    if bid == 0: return "air", P
    if bid == 1: return ["stone", "granite", "polished_granite", "diorite",
                         "polished_diorite", "andesite", "polished_andesite"][min(data, 6)], P
    if bid == 2: return "grass_block", {"snowy": "false"}
    if bid == 3: return ["dirt", "coarse_dirt", "podzol"][min(data, 2)], P
    if bid == 4: return "cobblestone", P
    if bid == 5: return f"{WOOD[min(data,5)]}_planks", P
    if bid == 6: return f"{WOOD[min(data&7,5)]}_sapling", P
    if bid == 7: return "bedrock", P
    if bid in (8, 9): return "water", {"level": "0"}
    if bid in (10, 11): return "lava", {"level": "0"}
    if bid == 12: return ["sand", "red_sand"][min(data, 1)], P
    if bid == 13: return "gravel", P
    if bid == 14: return "gold_ore", P
    if bid == 15: return "iron_ore", P
    if bid == 16: return "coal_ore", P
    if bid == 17: return log(f"{WOOD[min(data&3,5)]}_log", data)
    if bid == 18: return f"{WOOD[min(data&3,5)]}_leaves", {"persistent": "true", "distance": "1"}
    if bid == 19: return "sponge", P
    if bid == 20: return "glass", P
    if bid == 21: return "lapis_ore", P
    if bid == 22: return "lapis_block", P
    if bid == 23: return "dispenser", {"facing": ["down", "up", "north", "south", "west", "east"][min(data & 7, 5)]}
    if bid == 24: return ["sandstone", "chiseled_sandstone", "cut_sandstone"][min(data, 2)], P
    if bid == 25: return "note_block", P
    if bid == 26: return "red_bed", P
    if bid == 27: return "powered_rail", P
    if bid == 28: return "detector_rail", P
    if bid == 30: return "cobweb", P
    if bid == 31: return ["dead_bush", "short_grass", "fern"][min(data, 2)], P
    if bid == 32: return "dead_bush", P
    if bid == 35: return f"{COLORS[min(data,15)]}_wool", P
    if bid == 37: return "dandelion", P
    if bid == 38: return ["poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip",
                          "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"][min(data, 8)], P
    if bid == 39: return "brown_mushroom", P
    if bid == 40: return "red_mushroom", P
    if bid == 41: return "gold_block", P
    if bid == 42: return "iron_block", P
    if bid == 43:
        return ["smooth_stone", "sandstone", "oak_planks", "cobblestone", "bricks",
                "stone_bricks", "nether_bricks", "quartz_block"][min(data & 7, 7)], P
    if bid == 44:
        k = ["smooth_stone_slab", "sandstone_slab", "oak_slab", "cobblestone_slab", "brick_slab",
             "stone_brick_slab", "nether_brick_slab", "quartz_slab"][min(data & 7, 7)]
        return slab(k, data)
    if bid == 45: return "bricks", P
    if bid == 46: return "tnt", P
    if bid == 47: return "bookshelf", P
    if bid == 48: return "mossy_cobblestone", P
    if bid == 49: return "obsidian", P
    if bid == 50:
        return ("wall_torch", {"facing": {1: "east", 2: "west", 3: "south", 4: "north"}[data & 7]}) \
            if (data & 7) in (1, 2, 3, 4) else ("torch", P)
    if bid == 51: return "fire", P
    if bid == 52: return "spawner", P
    if bid == 53: return stair("oak_stairs", data)
    if bid == 54: return "chest", {"facing": {2: "north", 3: "south", 4: "west", 5: "east"}.get(data, "north")}
    if bid == 55: return "redstone_wire", P
    if bid == 56: return "diamond_ore", P
    if bid == 57: return "diamond_block", P
    if bid == 58: return "crafting_table", P
    if bid == 59: return "wheat", {"age": str(min(data, 7))}
    if bid == 60: return "farmland", {"moisture": "0"}
    if bid == 61 or bid == 62:
        return "furnace", {"facing": {2: "north", 3: "south", 4: "west", 5: "east"}.get(data, "north"), "lit": "false"}
    if bid == 63: return "oak_sign", {"rotation": str(data & 15)}
    if bid == 64: return "oak_door", {"half": "upper" if data & 8 else "lower"}
    if bid == 65: return "ladder", {"facing": {2: "north", 3: "south", 4: "west", 5: "east"}.get(data, "north")}
    if bid == 66: return "rail", P
    if bid == 67: return stair("cobblestone_stairs", data)
    if bid == 68: return "oak_wall_sign", {"facing": {2: "north", 3: "south", 4: "west", 5: "east"}.get(data, "north")}
    if bid == 69: return "lever", P
    if bid == 70: return "stone_pressure_plate", P
    if bid == 71: return "iron_door", {"half": "upper" if data & 8 else "lower"}
    if bid == 72: return "oak_pressure_plate", P
    if bid == 73 or bid == 74: return "redstone_ore", P
    if bid == 75 or bid == 76:
        return ("redstone_wall_torch", {"facing": {1: "east", 2: "west", 3: "south", 4: "north"}.get(data & 7, "north")}) \
            if (data & 7) in (1, 2, 3, 4) else ("redstone_torch", P)
    if bid == 77: return "stone_button", P
    if bid == 78: return "snow", {"layers": str(min((data & 7) + 1, 8))}
    if bid == 79: return "ice", P
    if bid == 80: return "snow_block", P
    if bid == 81: return "cactus", P
    if bid == 82: return "clay", P
    if bid == 83: return "sugar_cane", P
    if bid == 84: return "jukebox", P
    if bid == 85: return "oak_fence", P
    if bid == 86: return "carved_pumpkin", {"facing": ["south", "west", "north", "east"][data & 3]}
    if bid == 87: return "netherrack", P
    if bid == 88: return "soul_sand", P
    if bid == 89: return "glowstone", P
    if bid == 90: return "nether_portal", {"axis": "x"}
    if bid == 91: return "jack_o_lantern", {"facing": ["south", "west", "north", "east"][data & 3]}
    if bid == 92: return "cake", P
    if bid in (93, 94): return "repeater", P
    if bid == 95: return f"{COLORS[min(data,15)]}_stained_glass", P
    if bid == 96: return "oak_trapdoor", {"half": "top" if data & 8 else "bottom"}
    if bid == 97: return "infested_stone", P
    if bid == 98: return STONEB.get(data & 3, "stone_bricks"), P
    if bid == 99: return "brown_mushroom_block", P
    if bid == 100: return "red_mushroom_block", P
    if bid == 101: return "iron_bars", P
    if bid == 102: return "glass_pane", P
    if bid == 103: return "melon", P
    if bid == 104: return "pumpkin_stem", P
    if bid == 105: return "melon_stem", P
    if bid == 106: return "vine", {"south": "true"}
    if bid == 107: return "oak_fence_gate", {"facing": ["south", "west", "north", "east"][data & 3]}
    if bid == 108: return stair("brick_stairs", data)
    if bid == 109: return stair("stone_brick_stairs", data)
    if bid == 110: return "mycelium", {"snowy": "false"}
    if bid == 111: return "lily_pad", P
    if bid == 112: return "nether_bricks", P
    if bid == 113: return "nether_brick_fence", P
    if bid == 114: return stair("nether_brick_stairs", data)
    if bid == 115: return "nether_wart", {"age": str(min(data, 3))}
    if bid == 116: return "enchanting_table", P
    if bid == 117: return "brewing_stand", P
    if bid == 118: return "cauldron", P
    if bid == 119: return "end_portal", P
    if bid == 120: return "end_portal_frame", {"facing": ["south", "west", "north", "east"][data & 3],
                                               "eye": "true" if data & 4 else "false"}
    if bid == 121: return "end_stone", P
    if bid == 122: return "dragon_egg", P
    if bid == 123: return "redstone_lamp", {"lit": "false"}
    if bid == 124: return "redstone_lamp", {"lit": "true"}
    if bid == 125: return f"{WOOD[min(data&7,5)]}_planks", P
    if bid == 126: return slab(f"{WOOD[min(data&7,5)]}_slab", data)
    if bid == 127: return "cocoa", P
    if bid == 128: return stair("sandstone_stairs", data)
    if bid == 129: return "emerald_ore", P
    if bid == 130: return "ender_chest", P
    if bid == 131: return "tripwire_hook", P
    if bid == 132: return "tripwire", P
    if bid == 133: return "emerald_block", P
    if bid == 134: return stair("spruce_stairs", data)
    if bid == 135: return stair("birch_stairs", data)
    if bid == 136: return stair("jungle_stairs", data)
    if bid == 137: return "command_block", P
    if bid == 138: return "beacon", P
    if bid == 139: return ["cobblestone_wall", "mossy_cobblestone_wall"][min(data, 1)], P
    if bid == 140: return "flower_pot", P
    if bid == 141: return "carrots", {"age": str(min(data, 7))}
    if bid == 142: return "potatoes", {"age": str(min(data, 7))}
    if bid == 143: return "oak_button", P
    if bid == 145: return "anvil", P
    if bid == 146: return "trapped_chest", {"facing": {2: "north", 3: "south", 4: "west", 5: "east"}.get(data, "north")}
    if bid == 147: return "light_weighted_pressure_plate", P
    if bid == 148: return "heavy_weighted_pressure_plate", P
    if bid in (149, 150): return "comparator", P
    if bid == 151 or bid == 178: return "daylight_detector", P
    if bid == 152: return "redstone_block", P
    if bid == 153: return "nether_quartz_ore", P
    if bid == 154: return "hopper", P
    if bid == 155:
        return ["quartz_block", "chiseled_quartz_block", "quartz_pillar"][min(data, 2)], P
    if bid == 156: return stair("quartz_stairs", data)
    if bid == 157: return "activator_rail", P
    if bid == 158: return "dropper", P
    if bid == 159: return f"{COLORS[min(data,15)]}_terracotta", P
    if bid == 160: return f"{COLORS[min(data,15)]}_stained_glass_pane", P
    if bid == 161: return f"{['acacia','dark_oak'][min(data&1,1)]}_leaves", {"persistent": "true"}
    if bid == 162: return log(f"{['acacia','dark_oak'][min(data&1,1)]}_log", data)
    if bid == 163: return stair("acacia_stairs", data)
    if bid == 164: return stair("dark_oak_stairs", data)
    if bid == 165: return "slime_block", P
    if bid == 166: return "barrier", P
    if bid == 167: return "iron_trapdoor", {"half": "top" if data & 8 else "bottom"}
    if bid == 168: return "prismarine", P
    if bid == 169: return "sea_lantern", P
    if bid == 170: return "hay_block", P
    if bid == 171: return f"{COLORS[min(data,15)]}_carpet", P
    if bid == 172: return "terracotta", P
    if bid == 173: return "coal_block", P
    if bid == 174: return "packed_ice", P
    if bid == 175: return ["sunflower", "lilac", "tall_grass", "large_fern", "rose_bush", "peony"][min(data & 7, 5)], \
                          {"half": "upper" if data & 8 else "lower"}
    if bid == 179: return "red_sandstone", P
    if bid == 180: return stair("red_sandstone_stairs", data)
    if bid == 181: return "red_sandstone", P
    if bid == 182: return slab("red_sandstone_slab", data)
    if bid in (183, 184, 185, 186, 187):
        k = {183: "spruce", 184: "birch", 185: "jungle", 186: "dark_oak", 187: "acacia"}[bid]
        return f"{k}_fence_gate", {"facing": ["south", "west", "north", "east"][data & 3]}
    if bid in (188, 189, 190, 191, 192):
        k = {188: "spruce", 189: "birch", 190: "jungle", 191: "dark_oak", 192: "acacia"}[bid]
        return f"{k}_fence", P
    if bid in (193, 194, 195, 196, 197):
        k = {193: "spruce", 194: "birch", 195: "jungle", 196: "acacia", 197: "dark_oak"}[bid]
        return f"{k}_door", {"half": "upper" if data & 8 else "lower"}
    if bid == 198: return "end_rod", P
    if bid == 199: return "chorus_plant", P
    if bid == 200: return "chorus_flower", P
    if bid == 201: return "purpur_block", P
    if bid == 202: return "purpur_pillar", P
    if bid == 203: return stair("purpur_stairs", data)
    if bid == 205: return slab("purpur_slab", data)
    if bid == 206: return "end_stone_bricks", P
    if bid == 208: return "dirt_path", P
    if bid == 210: return "repeating_command_block", P
    if bid == 211: return "chain_command_block", P
    if bid == 213: return "magma_block", P
    if bid == 214: return "nether_wart_block", P
    if bid == 215: return "red_nether_bricks", P
    if bid == 216: return "bone_block", P
    if bid == 218: return "observer", P
    if bid == 219: return "white_shulker_box", P
    if bid == 235: return f"{COLORS[min(data,15)]}_glazed_terracotta", P
    if bid == 251: return f"{COLORS[min(data,15)]}_concrete", P
    if bid == 252: return f"{COLORS[min(data,15)]}_concrete_powder", P
    if bid == 255: return "structure_block", P
    return "stone", P


def convert(path, out_path, max_volume=900_000):
    root = read_schem(path)
    W, H, L = root["Width"], root["Height"], root["Length"]
    if W * H * L > max_volume:
        return None, f"too big ({W}x{H}x{L})"
    if W > 48 or H > 48 or L > 48:
        return None, f"exceeds structure block limit ({W}x{H}x{L})"
    blocks, data = root["Blocks"], root["Data"]

    palette, pal_index, blk = [], {}, []
    for y in range(H):
        for z in range(L):
            for x in range(W):
                i = (y * L + z) * W + x
                bid = blocks[i]
                if bid == 0:
                    continue
                name, props = M(bid, data[i] & 15)
                key = (name, tuple(sorted(props.items())))
                if key not in pal_index:
                    pal_index[key] = len(palette)
                    palette.append((name, props))
                blk.append((x, y, z, pal_index[key]))
    if not blk:
        return None, "empty"

    pal_bytes = []
    for name, props in palette:
        pairs = [w_tag("Name", 8, w_str(f"minecraft:{name}"))]
        if props:
            pairs.append(w_tag("Properties", 10, w_compound(
                [w_tag(k, 8, w_str(str(v))) for k, v in props.items()])))
        pal_bytes.append(w_compound(pairs))

    blk_bytes = []
    for x, y, z, s in blk:
        blk_bytes.append(w_compound([
            w_tag("pos", 9, w_list(3, [w_int(x), w_int(y), w_int(z)])),
            w_tag("state", 3, w_int(s)),
        ]))

    root_c = w_compound([
        w_tag("DataVersion", 3, w_int(3465)),          # 1.20.1
        w_tag("size", 9, w_list(3, [w_int(W), w_int(H), w_int(L)])),
        w_tag("palette", 9, w_list(10, pal_bytes)),
        w_tag("blocks", 9, w_list(10, blk_bytes)),
        w_tag("entities", 9, w_list(10, [])),
    ])
    payload = w_tag("", 10, root_c)
    with gzip.open(out_path, "wb") as f:
        f.write(payload)
    return len(blk), f"{W}x{H}x{L}"


if __name__ == "__main__":
    files = sorted(glob.glob(f"{SRC}/**/*.schematic", recursive=True))
    ok, skipped = [], []
    for p in files:
        ep = os.path.basename(os.path.dirname(p)).lower()
        nm = os.path.splitext(os.path.basename(p))[0]
        safe = "".join(c if c.isalnum() else "_" for c in f"{ep}_{nm}").lower()
        try:
            n, info = convert(p, f"{OUT}/{safe}.nbt")
        except Exception as e:
            skipped.append((nm, f"error {e}")); continue
        if n:
            ok.append((safe, n, info))
        else:
            skipped.append((nm, info))
    print(f"converted {len(ok)} structures, skipped {len(skipped)}")
    for s, n, i in ok[:40]:
        print(f"  {s:<52} {n:>7} blocks  {i}")
    json.dump([s for s, _, _ in ok], open("/home/user/UltimateWitherStorm/tools/_structures.json", "w"), indent=1)
