#!/usr/bin/env python3
"""Build the Ultimate MCSM datapack: a written guidebook granted on first join,
plus quality-of-life functions and the MCSM structure catalogue.
"""
import json, os, shutil

DP = "/home/user/UltimateWitherStorm/datapack"
NS = f"{DP}/data/ultimatews"
FN = f"{NS}/functions"
for d in (FN, f"{NS}/advancements", f"{DP}/data/minecraft/tags/functions"):
    os.makedirs(d, exist_ok=True)


def esc(s):
    return s.replace("\\", "\\\\").replace('"', '\\"')


# --------------------------------------------------------------- the guidebook
def page(*lines):
    """Build one book page as a JSON text component string."""
    parts = []
    for ln in lines:
        if isinstance(ln, str):
            parts.append({"text": ln})
        else:
            parts.append(ln)
    return json.dumps(parts, separators=(",", ":"))


H = lambda t, c="dark_purple": {"text": t + "\n", "color": c, "bold": True}
B = lambda t, c="black": {"text": t, "color": c}
G = lambda t: {"text": t, "color": "dark_gray", "italic": True}

PAGES = [
    page(H("THE WITHER STORM"), B("A guide to surviving the "),
         B("Ultimate MCSM", "dark_purple"), B(" Wither Storm.\n\n"),
         G("It began as a Wither. It will not end as one.\n\n"),
         B("Turn the page to begin.", "gray")),

    page(H("1. THE PHASES"),
         B("The storm grows as it eats.\n\n"),
         B("\u25b8 Phase 1-2 ", "dark_purple"), B("Wither with a command block core.\n"),
         B("\u25b8 Phase 3-4 ", "dark_purple"), B("Tentacles emerge. A "),
         B("white halo", "white"), B(" hugs its sides.\n"),
         B("\u25b8 Phase 5 ", "dark_purple"), B("A "), B("black blur", "dark_gray"),
         B(" bruised with "), B("purple", "dark_purple"), B(" surrounds it.\n")),

    page(H("1. THE PHASES", "dark_purple"),
         B("\u25b8 Phase 5.1 ", "dark_purple"), B("A "), B("blue aura", "aqua"),
         B(" ignites.\n"),
         B("\u25b8 Phase 5.5 ", "dark_purple"), B("A "), B("purple aura", "light_purple"),
         B(" wraps AROUND the blue one and keeps growing.\n"),
         B("\u25b8 Phase 6-7 ", "dark_purple"), B("It splits. The sky belongs to it now.\n\n"),
         G("Growth is uncapped by default.")),

    page(H("2. THE TEETH"),
         B("Its teeth burn "), B("turquoise", "aqua"), B(".\n\n"),
         B("That glow is not decoration \u2014 it is the command block core showing "
           "through. When the teeth flare, it has locked onto you.\n\n"),
         G("Do not let it see you eat.")),

    page(H("3. TENTACLES"),
         B("Tentacles can:\n\n"),
         B("\u2022 Grab", "dark_red"), B(" you out of the air\n"),
         B("\u2022 Throw", "dark_red"), B(" you across the world\n"),
         B("\u2022 Slam", "dark_red"), B(" into terrain\n"),
         B("\u2022 Rip apart", "dark_red"), B(" houses and structures\n\n"),
         B("Struggle (sneak repeatedly) to break a grab.", "gray")),

    page(H("4. WHAT TO BUILD"),
         B("\u25b8 Amulet ", "dark_purple"), B("\u2014 links to the storm, lets you track it.\n"),
         B("\u25b8 Formidibomb ", "dark_purple"), B("\u2014 the only thing that truly hurts it.\n"),
         B("\u25b8 Super TNT ", "dark_purple"), B("\u2014 for everything else.\n"),
         B("\u25b8 Withered Beacon ", "dark_purple"), B("\u2014 hold ground against the tainted.\n")),

    page(H("5. THE TAINTED"),
         B("Everything it touches turns.\n\n"),
         B("Tainted stone, sand, logs, glass, mushrooms and flesh will spread "
           "through the world.\n\n"),
         B("Sickened mobs glow faintly. They were animals once.\n")),

    page(H("6. CONFIG"),
         B("This pack ships "), B("hundreds", "dark_purple"),
         B(" of toggles across 10 categories:\n\n"),
         B("Halos \u00b7 Teeth \u00b7 Sky \u00b7 Shaders \u00b7 Tentacles \u00b7 Destruction \u00b7 Growth \u00b7 "
           "World \u00b7 Audio \u00b7 Performance\n\n"),
         B("Default preset: "), B("MAX SPECTACLE", "light_purple")),

    page(H("7. SURVIVAL"),
         B("\u2022 It is drawn to noise and light.\n"),
         B("\u2022 Water slows the pull, not the storm.\n"),
         B("\u2022 Boating too long makes it lose interest.\n"),
         B("\u2022 It never truly stops.\n\n"),
         G("Good luck. You will need it.")),
]

BOOK = ("written_book{title:\"Wither Storm Field Guide\",author:\"The Order of the Stone\","
        "generation:0,resolved:1b,display:{Name:'{\"text\":\"Wither Storm Field Guide\","
        "\"color\":\"light_purple\",\"italic\":false}'},pages:["
        + ",".join("'" + p.replace("\\", "\\\\").replace("'", "\\'") + "'" for p in PAGES)
        + "]}")

# ----------------------------------------------------------------- functions
open(f"{FN}/give_guidebook.mcfunction", "w").write(
    "# Grants the Wither Storm Field Guide\n"
    f"give @s minecraft:{BOOK} 1\n"
    'tellraw @s {"text":"\\u2726 You received the Wither Storm Field Guide.","color":"light_purple"}\n'
)

open(f"{FN}/first_join.mcfunction", "w").write(
    "# Runs once, the first time a player joins\n"
    "function ultimatews:give_guidebook\n"
    'title @s times 10 70 20\n'
    'title @s subtitle {"text":"It is already growing.","color":"gray"}\n'
    'title @s title {"text":"THE WITHER STORM","color":"dark_purple","bold":true}\n'
    "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 0.7 0.5\n"
    "advancement grant @s only ultimatews:root\n"
)

open(f"{FN}/load.mcfunction", "w").write(
    "scoreboard objectives add uws_joined dummy\n"
    'tellraw @a {"text":"[Ultimate MCSM Wither Storm] loaded \\u2014 MAX SPECTACLE","color":"dark_purple"}\n'
)

open(f"{FN}/tick.mcfunction", "w").write(
    "# give the guidebook exactly once per player\n"
    "execute as @a unless score @s uws_joined matches 1 run function ultimatews:first_join\n"
    "execute as @a unless score @s uws_joined matches 1 run scoreboard players set @s uws_joined 1\n"
)

json.dump({"values": ["ultimatews:load"]}, open(f"{DP}/data/minecraft/tags/functions/load.json", "w"), indent=1)
json.dump({"values": ["ultimatews:tick"]}, open(f"{DP}/data/minecraft/tags/functions/tick.json", "w"), indent=1)

json.dump({
    "display": {
        "icon": {"item": "minecraft:written_book"},
        "title": {"text": "The Wither Storm", "color": "dark_purple"},
        "description": {"text": "It begins."},
        "frame": "task", "show_toast": True, "announce_to_chat": False,
    },
    "criteria": {"tick": {"trigger": "minecraft:tick"}},
}, open(f"{NS}/advancements/root.json", "w"), indent=1)

json.dump({
    "pack": {
        "pack_format": 15,
        "description": "\u00a75Ultimate MCSM Wither Storm \u00a77\u2014 guidebook, structures, story tools",
    }
}, open(f"{DP}/pack.mcmeta", "w"), indent=2)

n = sum(len(f) for _, _, f in os.walk(DP))
print(f"datapack: {n} files, {len(PAGES)}-page guidebook")
