#!/usr/bin/env python3
"""
Emit the legacy (MC 1.8-era) numeric block ID -> modern block id table used by
McsmSchematic.java.

The MCSM schematics are MCEdit "Alpha" format: a byte array of numeric block
IDs plus a nibble array of data values. Minecraft dropped numeric IDs in 1.13,
so every one has to be mapped by hand.

Only the IDs that actually occur in the 181 schematics are emitted (223 of
them). Where a legacy block has no modern equivalent, the closest visual match
is used rather than air, so the builds stay readable.

Output: a Java switch body written to stdout.
"""

# id -> modern block, or (id, {data: block}) for data-dependent variants
M = {
    0: "air",
    1: "stone", 2: "grass_block", 3: "dirt", 4: "cobblestone", 5: "oak_planks",
    6: "oak_sapling", 7: "bedrock", 8: "water", 9: "water", 10: "lava",
    11: "lava", 12: "sand", 13: "gravel", 14: "gold_ore", 15: "iron_ore",
    16: "coal_ore", 17: "oak_log", 18: "oak_leaves", 19: "sponge",
    20: "glass", 21: "lapis_ore", 22: "lapis_block", 23: "dispenser",
    24: "sandstone", 25: "note_block", 26: "red_bed", 27: "powered_rail",
    28: "detector_rail", 29: "sticky_piston", 30: "cobweb", 31: "short_grass",
    32: "dead_bush", 33: "piston", 34: "piston_head", 35: "white_wool",
    36: "moving_piston", 37: "dandelion", 38: "poppy", 39: "brown_mushroom",
    40: "red_mushroom", 41: "gold_block", 42: "iron_block",
    43: "smooth_stone_slab", 44: "smooth_stone_slab", 45: "bricks",
    46: "tnt", 47: "bookshelf", 48: "mossy_cobblestone", 49: "obsidian",
    50: "torch", 51: "fire", 52: "spawner", 53: "oak_stairs", 54: "chest",
    55: "redstone_wire", 56: "diamond_ore", 57: "diamond_block",
    58: "crafting_table", 59: "wheat", 60: "farmland", 61: "furnace",
    62: "furnace", 63: "oak_sign", 64: "oak_door", 65: "ladder", 66: "rail",
    67: "cobblestone_stairs", 68: "oak_wall_sign", 69: "lever",
    70: "stone_pressure_plate", 71: "iron_door", 72: "oak_pressure_plate",
    73: "redstone_ore", 74: "redstone_ore", 75: "redstone_torch",
    76: "redstone_torch", 77: "stone_button", 78: "snow", 79: "ice",
    80: "snow_block", 81: "cactus", 82: "clay", 83: "sugar_cane",
    84: "jukebox", 85: "oak_fence", 86: "carved_pumpkin", 87: "netherrack",
    88: "soul_sand", 89: "glowstone", 90: "nether_portal",
    91: "jack_o_lantern", 92: "cake", 93: "repeater", 94: "repeater",
    95: "white_stained_glass", 96: "oak_trapdoor", 97: "infested_stone",
    98: "stone_bricks", 99: "brown_mushroom_block", 100: "red_mushroom_block",
    101: "iron_bars", 102: "glass_pane", 103: "melon", 104: "pumpkin_stem",
    105: "melon_stem", 106: "vine", 107: "oak_fence_gate",
    108: "brick_stairs", 109: "stone_brick_stairs", 110: "mycelium",
    111: "lily_pad", 112: "nether_bricks", 113: "nether_brick_fence",
    114: "nether_brick_stairs", 115: "nether_wart", 116: "enchanting_table",
    117: "brewing_stand", 118: "cauldron", 119: "end_portal",
    120: "end_portal_frame", 121: "end_stone", 122: "dragon_egg",
    123: "redstone_lamp", 124: "redstone_lamp", 125: "oak_slab",
    126: "oak_slab", 127: "cocoa", 128: "sandstone_stairs",
    129: "emerald_ore", 130: "ender_chest", 131: "tripwire_hook",
    132: "tripwire", 133: "emerald_block", 134: "spruce_stairs",
    135: "birch_stairs", 136: "jungle_stairs", 137: "command_block",
    138: "beacon", 139: "cobblestone_wall", 140: "flower_pot",
    141: "carrots", 142: "potatoes", 143: "oak_button", 144: "skeleton_skull",
    145: "anvil", 146: "trapped_chest", 147: "light_weighted_pressure_plate",
    148: "heavy_weighted_pressure_plate", 149: "comparator",
    150: "comparator", 151: "daylight_detector", 152: "redstone_block",
    153: "nether_quartz_ore", 154: "hopper", 155: "quartz_block",
    156: "quartz_stairs", 157: "activator_rail", 158: "dropper",
    159: "white_terracotta", 160: "white_stained_glass_pane",
    161: "acacia_leaves", 162: "acacia_log", 163: "acacia_stairs",
    164: "dark_oak_stairs", 165: "slime_block", 166: "barrier",
    167: "iron_trapdoor", 168: "prismarine", 169: "sea_lantern",
    170: "hay_block", 171: "white_carpet", 172: "terracotta",
    173: "coal_block", 174: "packed_ice", 175: "sunflower",
    176: "white_banner", 177: "white_wall_banner", 178: "daylight_detector",
    179: "red_sandstone", 180: "red_sandstone_stairs",
    181: "red_sandstone_slab", 182: "red_sandstone_slab",
    183: "spruce_fence_gate", 184: "birch_fence_gate",
    185: "jungle_fence_gate", 186: "dark_oak_fence_gate",
    187: "acacia_fence_gate", 188: "spruce_fence", 189: "birch_fence",
    190: "jungle_fence", 191: "dark_oak_fence", 192: "acacia_fence",
    193: "spruce_door", 194: "birch_door", 195: "jungle_door",
    196: "acacia_door", 197: "dark_oak_door", 198: "end_rod",
    199: "chorus_plant", 200: "chorus_flower", 201: "purpur_block",
    202: "purpur_pillar", 203: "purpur_stairs", 204: "purpur_slab",
    205: "purpur_slab", 206: "end_stone_bricks", 207: "beetroots",
    208: "dirt_path", 209: "end_gateway", 210: "repeating_command_block",
    211: "chain_command_block", 212: "frosted_ice", 213: "magma_block",
    214: "nether_wart_block", 215: "red_nether_bricks", 216: "bone_block",
    217: "structure_void", 218: "observer", 219: "white_shulker_box",
    231: "purple_shulker_box",
    236: "white_concrete", 237: "orange_concrete", 238: "magenta_concrete",
    240: "yellow_concrete", 241: "lime_concrete", 242: "pink_concrete",
    243: "gray_concrete", 245: "cyan_concrete", 246: "purple_concrete",
    248: "brown_concrete", 249: "green_concrete", 250: "red_concrete",
    251: "black_concrete", 252: "white_concrete_powder", 255: "structure_block",
}

# Data-value variants worth honouring: these carry most of the visual identity
# (wool/terracotta/concrete colours, wood species, stone types).
COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
          "gray", "light_gray", "cyan", "purple", "blue", "brown", "green",
          "red", "black"]
WOODS = ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak"]

VARIANTS = {
    35: [f"{c}_wool" for c in COLORS],
    159: [f"{c}_terracotta" for c in COLORS],
    95: [f"{c}_stained_glass" for c in COLORS],
    160: [f"{c}_stained_glass_pane" for c in COLORS],
    171: [f"{c}_carpet" for c in COLORS],
    251: [f"{c}_concrete" for c in COLORS],
    252: [f"{c}_concrete_powder" for c in COLORS],
    176: [f"{c}_banner" for c in COLORS],
    5: [f"{w}_planks" for w in WOODS],
    17: ["oak_log", "spruce_log", "birch_log", "jungle_log"],
    162: ["acacia_log", "dark_oak_log"],
    18: ["oak_leaves", "spruce_leaves", "birch_leaves", "jungle_leaves"],
    161: ["acacia_leaves", "dark_oak_leaves"],
    1: ["stone", "granite", "polished_granite", "diorite", "polished_diorite",
        "andesite", "polished_andesite"],
    3: ["dirt", "coarse_dirt", "podzol"],
    12: ["sand", "red_sand"],
    24: ["sandstone", "chiseled_sandstone", "cut_sandstone"],
    179: ["red_sandstone", "chiseled_red_sandstone", "cut_red_sandstone"],
    98: ["stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks",
         "chiseled_stone_bricks"],
    155: ["quartz_block", "chiseled_quartz_block", "quartz_pillar"],
    168: ["prismarine", "prismarine_bricks", "dark_prismarine"],
    38: ["poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip",
         "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"],
    31: ["dead_bush", "short_grass", "fern"],
    139: ["cobblestone_wall", "mossy_cobblestone_wall"],
}

print("   // ---- plain ids ----")
for k in sorted(M):
    if k in VARIANTS:
        continue
    print(f'      case {k} -> "{M[k]}";')

print("\n   // ---- data-dependent ids ----")
for k in sorted(VARIANTS):
    opts = VARIANTS[k]
    arr = ", ".join(f'"{o}"' for o in opts)
    print(f"      // {k}: {len(opts)} variants")
    print(f"      case {k} -> pick(data, new String[]{{{arr}}});")
