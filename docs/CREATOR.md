# DEVOURING STORMS — v1.5 "THE CREATOR" — Design & Tapthe Map

*"The Lord said it can warp reality itself."* — the series, on the crater.

This document indexes the v1.5 slate end-to-end. The raw machinery lives in-tree; this is
the atlas.

---

## THE CRATER VISION (VHS overlay)

Three layers, matching the pack's existing presentation philosophy:

1. **Java overlay** (`client/StormVisuals.java`, `renderVhs`) — screen-space tape when the
   trigger strength > 0:
   - recording a **playing VHS jukebox** within 10 blocks (blockstate scan, cached per 6 ticks),
   - being within 24 of a **corrupted command block** (the crater heart),
   - being within 52 of a **Creator** body,
   - having the **`overtaken`** status effect (what the Monstrosity applies).
2. **Iris shader** (`shaders/final.fsh`, `DS_VHS`) — during storm weather: dedicated
   tracking band crawl (8px smear), dropout lines, color bleed behind the band, letterbox
   hint, plus the *pre-existing lensing/maw* work intact. Off via `DS_VHS=0`.
3. **Bedrock** — `vhs_jukebox` interact plays tape records via script; fog console does
   the ambient weighting it already does in Decayed.

Java config: `vhs_overlay=true`. The **PLAY ▶ HH:MM:SS** counter runs vanilla game-time,
and the corner tag says which trigger owns your feed (`THE CRATER — EVENT LOG 0`,
`TAPE: REWRITTEN`, `FEED: CREATOR`).

## THE CREATOR (boss)

Java (`entity/CreatorEntity.java` + `CreatorHandEntity.java`):

| Stat | Value |
|---|---|
| Health | 900 |
| Scale | 8 (≈7.5 × 20 body) |
| Armor / KB resist | 12 / 1.0 |
| Speech | localized, honest-to-human sentences, one per ~2.5 min per viewer |
| Hand attack | spawn `creator_hand` +36 above target, telegraph 26t, strike 34 dmg in r≈7, retract 50t |

The hand is `noSave`: it can't persist; if the Creator unloads mid-swing the hand leaves.
The hand wears the Creator's skin folded: dark glove, lit knuckles.

Bedrock (`ds:creator`, `ds:creator_hand`): scripted handStrike (teleport chain + garlic-
grade knockback), chat lines every ~2.5 min to viewers in 220, 900 HP both pieces.

## THE MONSTROSITY & THE GLITCH LAWN

Java: `MonstrosityEntity` (120 HP) converts terrain blocks (`± r7-10` vertical+horizontal,
30/tick cap, skips air/glitch/obsidian/bedrock/corrupted_command_block) into `glitch_block`
and applies `Overtaken` on players inside 36 for 80-140 ticks. Bedrock scripts do the
lawn-speading via `monstrosityTick` + the occasional "LOOKS BACK" flash.

## THE FORGER & TENTACLE RAIN

Java: `ForgerEntity` (160 HP, floating) — every 180 ticks, picks a random player in 60
and interpolates 5-9 `sky_tentacle` spawns 22-30 above them (alive on the way down);
tentacle touches down, lingers 30 ticks, discards. Every 600-ish ticks it also dislodges
a **rift seam** column (`rift_portal` ×6) into the sky — these go where the Forger is,
which is "somewhere above you, praying downward."

## VARIANTS (colour denominations)

`MassgVariant` enum — CLASSIC, ROSE ("pink variant"), ABYSSAL, IVORY — synched through
entityData + NBT (`MassgVariant`), renderer tint multiplies the baked atlas. Severed
storms inherit (`summonMassgSevered`... already copies via `setVariant`). Tazos raffle
their own (7-way weight → mostly teal, sometimes rose/dusk/ivory) and persist through
`TazoVariant`. Bedrock ships `ds:massg_rose` with a genuinely different texture.

## PLANETS + THE ROCKET KEY

Four new datapack dimensions:

| Dim | File | Type | Generator | Lore |
|---|---|---|---|---|
| `cosmic_abyss` | `dimension/cosmic_abyss.json` | void-black dimensional fathoms | **flat void** | BHS-sits-here |
| `planet_aurth` | `dimension/planet_aurth.json` | planet type (daylight) | noise (decayed + rot_forest) | "the Stone Age had a morning" |
| `planet_volmar` | `dimension/planet_volmar.json` | planet type | noise (decayed) | iron and fire |
| `planet_nexus` | `dimension/planet_nexus.json` | planet type, skylight off | flat (Nexus floor) | the Multiverse Age |

`rocket_key`: `HOME → AURTH → VOLMAR → NEXUS → HOME` (ONE ring, cyclic, both landings
approximate above y=0).
`broken_record`: use it anywhere → Abyss at (0.5, 70, 0.5); **use it inside the Abyss →
"The needle finds the groove... it only plays arrival."** (No exit printed. Frayed tears
still work.)

## LIVING ECOSYSTEMS

`util/EcosystemTicker.java` — every 40 ticks each planet dimension:
- ages roll over each 54,000 game ticks (Stone → Bronze → Iron → Industrial → Digital →
  **Multiverse**), with a chat titlecard per era,
- ambience dials by age: ash (Stone-Bronze) → smoke (Iron-Industrial) → **glitch motes**
  (late game),
- session-scoped memory — age never teleports *backward* within a session.

## THE SIX STRUCTURES

Summarized in README; the geometry is authored at `tools/build_structures.py` (a minimal
big-endian NBT writer: palette + blocks + entities). All six register as `jigsaw`
structures with a single rigid pool + a `random_spread` set dedicated per structure:

| Structure | Salt | Spacing/Separation | Biomes |
|---|---|---|---|
| summon_crater | 841137 | 48/24 | #minecraft:is_overworld |
| epa_facility | 841192 | 44/22 | #minecraft:is_overworld |
| tazo_town | 841203 | 46/23 | #minecraft:is_overworld |
| boom_town | 841214 | 52/26 | devouring_storms:decayed_wastes |
| limitless_spaces | 841225 | 34/17 | devouring_storms:decayed_wastes |
| event_horizon | 841236 | 80/40 | devouring_storms:decayed_wastes |

Note: the three Decayed-dimension structures share the realm's `decayed_wastes` biome,
so they can also appear (rarely, very far apart) **in the Cosmic Abyss + Nexus** because
those dims register the same biome in their generator. This is intentional: Boom Town
in the abyss reads as the town's **faulty shelf copy**; Limitless Spaces in the abyss
is iys obvious home; the Event Horizon explicitly belongs to the Nexus.

## FOUR RECORDS

Java jukebox jsons: `data/devouring_storms/jukebox_song/{signal_tape,eaoin,countdown,quarantine}.json`.
Audio: procedural compositions in `tools/generate_assets.py` (`S["record/..."]`), B-minor-ish
signal-suite. All four also ship as `ds.record.*` Bedrock sound defs + VHS jukebox interact.

Toggle map additions: `vhs_overlay`, `creator`, `monstrosity_glitch`, `forger`, `planets`.

## Config additions (Java: config/devouring-storms.properties)

| Key | Default | What it does |
|---|---|---|
| vhs_overlay | true | full VHS screen overlay |
| creator | true | creator + hand strike |
| monstrosity_glitch | true | world-to-glitch conversion |
| forger | true | tentacle rain + rift seams |
| planets | true | ecosystem ages + planet ambient |

## Known build note

The sandbox cannot run `./gradlew build` (mapping host unreachable); v1.5 Java code is
hand-checked + balance-verified. Run the wrapper yourself: `cd java-mod && ./gradlew build`.
If a 26.2 mappings signature disagrees (e.g. entity `hurt(ServerLevel,…)` overloads),
the crashes will be type errors at aforementioned points; fix the signature, the
design is stable.
