# DEVOURING STORMS
### WITHERING REWRITE: AWAKENING — The official playable companion to the *Devouring Storms* series

> *"When the boundaries of the multiverse begin to collapse, a simple reflection becomes the doorway to something far worse."*

This repository contains the **Devouring Storms** game project — a full recreation of every concept
from the Devouring Storms YouTube series as playable Minecraft content:

| Deliverable | Target | Path |
|---|---|---|
| **Java Mod** (Fabric) | Minecraft Java **26.2** | [`java-mod/`](java-mod) |
| **Bedrock Add-On** (BP + RP + Scripts) | Minecraft Bedrock **26.40** | [`bedrock-addon/`](bedrock-addon) |
| **Built-in Shader Pack** (Iris / OptiFine) | Java Edition | [`shaders/DevouringStormsShaderPack/`](shaders/DevouringStormsShaderPack) |
| **Asset Generator** (textures + audio, procedural) | Python 3 | [`tools/generate_assets.py`](tools/generate_assets.py) |
| **Structure Generator** (Endertown & realm builds, both editions) | Python 3 | [`tools/generate_structures.py`](tools/generate_structures.py) |

---

## THE STORM

**MASSG** — *Massive Abomination Sundering Storm Genesis* — is the anomaly that was never supposed
to exist. The Wither Storm blueprints were corrupted, and deep beneath the broken code, it is waking up.

- Six-phase living storm boss: **SLEEPING → SIGNAL → HUNGER → DEVOURER → SUNDERER → GENESIS**
  — plus **phase 5.5: THE BOWELS**, where the storm splits open and its core burns violet
  (emissive glow pass on Java, emissive overlay layer on Bedrock).
- It **devours** mobs, players, and blocks — and it **grows**.
- It **devolves** as you hurt it; the music turns **critical**.
- It **infects**: decay near the storm (or breathing the realm's air) converts hostile mobs
  into **Withered Symbionts**, and the rot creeps through terrain block by block.
- When killed, it only *plays dead*. The blueprints are corrupted — it will be **reborn**, unless
  a **Formidibomb** ends it for good.
- The sky itself joins in: **MCSM-style boiling storm-cloud banks** and far violet thunder
  (Iris shader sky pass on Java, fog + sky-flash particles on Bedrock).
- **The full phase-5.5 rupture**: the horizon flushes purple-pink, the storm splits, segments
  rain out, purple liquid pours and is reeled back in, then a shockwave ring of everything
  it is made of. *(v1.3)*
- **The Husk** *(v1.3)* — strike Genesis down without the right tool and the storm falls out
  of the sky as a grounded zombie-form. The **command block inside keeps it intact** — nothing
  gets through. It only dies when something connected to it is destroyed.
- **The Storm Killer & the rend** *(v1.3)* — the Watcher hoards the blade that can end it.
  Take it into the husk's pulsing stomach-hole, strike the anchored command block three times,
  and watch the storm tear apart in rings of white-and-violet fire. Reward: the **Storm Heart**.
- **The fog ladder** *(v1.3)* — the air changes colour with it: blue-teal at the Signal,
  bruised dark blue as it feeds, deep violet, then dark purple & pink at the Bowels,
  and near-black at Genesis.
- **Infinite growth** *(v1.3)* — let Genesis live and it keeps growing. Deeply, seamlessly,
  until there is no world left above it.

## THE DECAYED REALITY

A quarantined world trapped in an endless cycle of destruction, rebirth, and corruption.
Enter through the **breached Mainframe**. Die there, and the cycle pulls you back — reincarnation.
Beneath its two rifts in the sky you will find **Tazo**, the **lurking Apparition** that calls itself
Anna, and the silent gaze of **THE WATCHER**.

## ENDERTOWN & THE RUINED REALM *(v1.1)*

Endertown still stands — a memorial banner-town in the heart of the quarantine. It generates as
real worldgen structures on Java and is raised block-by-block by the behaviour engine on Bedrock
(same Python geometry source for both, `tools/generate_structures.py`):

- **Endertown** — a full walled town: banner plaza with ~90 hand-patterned banners flanking a
  memorial spire, six block-houses, a two-storey tall house, the Relay Hall (its Terminal never
  woke), a climbable watchtower, market stalls, rot-trees — and loot caches worth the risk.
- **The Watcher Shrine** — a broken ring and an eye-spire. It was built for him. He came anyway.
- **The Mainframe Ruin** — where the breach began; a silent Corrupted Command Block, dead
  Terminals, and a chance at lost **Corrupted Blueprints**.
- **Rift Obelisks** — obsidian needles pinning the realm's rift-scars shut.
- **The Sealed Vault** *(v1.2)* — the ARG vault waits in Endertown's Relay Hall. Gather the
  **Seven Schedules** — plaza cache, Mainframe Ruin, Watcher Shrine, an apparition's gift,
  a companion's trust, the storm's corpse, the severed storms — and it accepts the password
  they assemble: **M.A.S.S.G.O.O.S**. The payload stays classified until 2027.
- **The Multiverse** *(v1.3)* — frayed tears in the fabric of reality (found at the realm
  platforms, or crafted) ride the ring: **Decayed Reality → The Fray → Echo Fields → home**.
- **Endertown lives** *(v1.3)* — **The Preacher** holds the banner plaza with sermons and a
  listener's blessing; **Endertonians** sweep the streets and tell you what the Preacher won't.
- **Lore objects** *(v1.3)* — three **E.P.A. audio logs** (the Bell, the Plague, the Watcher),
  and the **Seventh Trumpet**: the dormant ritual trigger that advances the storm one phase,
  on purpose. The husk does not answer.
- Bedrock bonus: the craftable **Endertown Core** re-raises the town after MASSG devours it.

## v1.5 — THE CREATOR

*"The Lord said it can warp reality itself."*

- **THE CRATER VISION (VHS overlay)** — look into the summoning crater, approach a playing
  **VHS Jukebox**, stray near the Monstrosity's broadcast radius (Java: *Overtaken* effect),
  or simply stand in front of the Creator — and your feed turns to **real dirty tape**:
  tracking bands tearing upward, hard scanlines, chroma ghosts, letterbox, and the cold
  white **PLAY ▶ counter** running its unforgivable numbers. Java overlay + Bedrock console
  fog pairing. Toggle: `vhs_overlay`.
- **THE CREATOR** — a cosmos-sized overseer (`/summon devouring_storms:creator`, Java egg,
  Bedrock egg). Robbins Egg-blue in his authority: black-silk robe sewn with stars the void
  misfiled; a face that doesn't scan as one; **two red eyes** that follow *you*, not the
  camera. He speaks actual human in a deep flat tone (chat, all viewers in 220 blocks,
  once per couple of minutes). Approach him and **THE HAND** falls out of the sky (Java:
  no-save 480 HP separate entity, telegraphed 26-tick descend, describable as "management
  inspects a table"), strikes for 34 wherever you were looking away from, retracts over
  another 50 ticks. Bedrock: scripted strike loop with particles + knockback.
- **THE MONSTROSITY** — moustached self-appointed caretaker of the glitch lawns. He
  converts the world to **glitch blocks** under his broadcast and applies **Overtaken**
  to visitors within 36 blocks (Java: 120 HP, +/- 7-10 radius converter, avoids the
  protected blocks; his spat static glows magenta). Boom Town voted for him quietly.
- **THE FORGER** — the foundry bell that never rings outward, only *downward*. Every
  nine seconds it spits **sky tentacles** 22-30 blocks above the nearest player and lets
  gravity deliver them. It also opens the occasional **rift seam** column — a 6-block
  fracture of rift portal into the Multiverse. Toggle: `forger`.
- **MASSG VARIANTS** — the storm's colour denominations ship as data: **classic / rose
  (pink) / abyssal / ivory**, applied by renderer-side grade tints over the shared atlas,
  survived by severed storms and tazos (tazos dock the same variant file). `/summon` with
  `{MassgVariant:"rose"}` or let tazos raffle their own lineups (7 rolls → teal, rose, dusk).
  Bedrock ships `ds:massg_rose` with a genuinely different skin. Toggle: n/a — it's art.
- **PLANETS + THE ROCKET KEY** — three pocket worlds reachable by the brass key
  (`rocket_key`, cycles HOME → AURTH (Stone) → VOLMAR (Iron) → NEXUS (Multiverse) → HOME):
  **cosmic_abyss** too — use a **broken record** anywhere to slip through the skip into
  the Abyss; the record cannot play you home. All four dims register as JSON datapack
  dimensions (flat cosmic abyss + two noise planets + the Nexus floor).
- **THE LIVING ECOSYSTEMS** — each planet tracks its own age counter (Stone → Bronze →
  Iron → Industrial → Digital → **Multiverse Age**), announcing era shifts to all viewers
  and sharpening its ambient particles per age (ash → smoke → glitch motes). Toggle: `planets`.
- **THREE MORE BLOCKS** — **glitch_block** (lit magenta converter hash, the Monstrosity's
  handwriting), **vhs_jukebox** (`PLAYING` lamp state that burns the Vision onto anyone
  looking at one while a tape runs), **crate_block** (shipping crates, aisle-grade timber,
  one sticker that meant something).
- **FOUR NEW RECORDS** — EA-suite for the VHS jukebox: *The Signal (tape rip)* (47s),
  *EAOIN, Sing* (58s), *Countdown* (66s), *Outside The Quarantine* (52s), procedurally
  synthesized in the generator with the rest of the soundtrack, comparator output 13.
- **SIX NEW STRUCTURES (Java worldgen)** — **Summon Crater** (obsidian ring, tilted frame
  fingers, corrupted command block heart, one monstrosity on watch), **E.P.A. Facility**
  (terminal bank + sealed vault wing, 4 researchers at their posts), **Tazo Town**
  (teal gunmetal towers, lantern orchards, 6 named tazos with variants), **Boom Town**
  (3×3 blocks of broken slabs with glitch seams + the central deliberation monolith;
  9 variant citizens apologizing to spawn eggs), **Limitless Spaces** (the store the Cosmic
  Abyss shelved across: crate aisles, VHS register lanes, six cart shoppers and a floor
  manager), **Event Horizon** (obsidian ring + rift seams + one Void Maw presiding over
  everything that will never leave the Nexus).
- **THE EARTH EATER** — planet-eating god (1500 HP, scale 6, flying). Radial bite: radius
  12. It signs travel documents without looking up.
- **E.P.A. RESEARCHERS + CART SHOPPERS** — passable-citizen NPCs: white coats, green badge,
  glasses catching the terminal light; shoppers push the faithful cart at 1.6 knockback.
- **SKY TENTACLES** — 16 HP descending stalks with four lit cups that discard on touchdown
  +30 ticks. Gorgeous. Dangerous the way furniture is dangerous — structurally.

## v1.4 — MULTIVERSE

- **Tractor beams** *(both editions)* — from the Devourer up, the storm's **three heads**
  pour out three filament beams that comb the ground, lift what they catch off its feet,
  and feed it toward the maw. MCSM made flesh. Toggle: `tractor_beams`.
- **Earthquakes** — under the Sunderer and up, the world buckles: camera shake, ash rings,
  a bruise under your feet. Toggle: `earthquakes`.
- **The belly of the storm** — fly into the **open bowels** and you're inside it: a chamber
  of stomach-wall stone, decay veins, and the **command block** beating on an obsidian dais.
  Strike it **three times with the Storm Killer** and the storm rips open from the inside.
  A frayed tear inside exhales you back out. Toggle: `belly` (Java: `stomach_interior`).
- **New beasts** — **Storm Mites** (frayspawn, travel in packs of 4), **The Taken**
  (the villagers who held out longest; slower than a symbiont, stronger, angrier — villagers
  now convert under prolonged decay), and the **Void Maw**: a black hole that got lost in
  the multiverse and liked the menu. It pulls, it eats, and every meal makes it a mouth
  with more mass. Toggle: `void_maw`.
- **The trapped** — **Travis** minds the tear in the Fray (trade him a Memory Fragment for
  the E.P.A. plague log he keeps for company). **Tonya?** hovers in the Echo Fields, answering
  slightly out of phase with everything. EAOIN sometimes answers a terminal first.
- **New structures** — the **Watcher Camp** (tents, a lens rig pointed at the quarantine,
  one patient watcher, Schedule V) and the **Rot Cathedral** (rot-log colonnades under a
  half-eaten roof, a Preacher mid-sermon, the Taken kneeling, Schedule VI, a reliquary with
  the Seventh Trumpet at 12%).
- **Title cards** — every phase change cuts to a channel-style title card:
  `P H A S E  5 ½ — THE BOWELS · it is open. it is stomach. it will remember you.`
  Toggle: `storm_title_cards` (Java config) / `title_cards` (Bedrock console).
- **Config everything** — Java: `config/devouring-storms.properties` (overlay intensity, fog
  ladder, title cards, watcher paranoia, bowels cinematic, debris rings, earthquakes,
  infection, infinite growth, void maw, stomach interior — hot-reloaded on edit).
  Bedrock: `/scriptevent ds:cfg <key> <1|0>` (try `/scriptevent ds:cfg list`).
- **Shader slate** — the Iris pack gains **gravitational lensing** (a precessing maw sinks
  into the frame during storm weather; Einstein-ring pullback, a bite of pure dark inside
  the photon sphere), a **maw drifting in the dome** where the sky should be, and **nine
  live sliders** in `shaders.properties` (`DS_LENSING`, `DS_CLOUD_CHURN`, `DS_CLOUD_COVER`,
  `DS_RIFT_GLOW`, `DS_SKY_DARKNESS`, `DS_MAW_SKY`, `DS_GRAIN`, `DS_VIGNETTE`, `DS_CHROMA`).

## Feature map

See [`docs/CONCEPTS.md`](docs/CONCEPTS.md) — every concept from the series, analysed and mapped to
its Java-mod and Bedrock add-on implementation.

## Quick start

**Java (26.2, Fabric):**
```bash
cd java-mod
./gradlew build        # produces build/libs/devouring-storms-<version>.jar
```
Requires [Fabric Loader 0.19.3+](https://fabricmc.net) and Fabric API 0.156.0+26.2.

**Bedrock (26.40):** zip `bedrock-addon/DevouringStormsBP` and `bedrock-addon/DevouringStormsRP`,
rename to `.mcaddon` contents (or pack as `.mcpack` files) and import. Enable **Beta APIs** is NOT
required — the behaviour pack uses the stable `@minecraft/server` 2.0.0 scripting API.

**Assets:** all textures, sounds and structures are generated procedurally (nothing copyrighted, nothing stolen):
```bash
python3 tools/generate_assets.py
python3 tools/generate_structures.py
```

Full instructions: [`docs/BUILDING.md`](docs/BUILDING.md) · [`docs/INSTALL.md`](docs/INSTALL.md)

## Credits & respect

Devouring Storms concept, series and characters © the REWRITTEN team (see `docs/CREDITS.md`).
This codebase is original work written for the project — **no assets from Decayed Reality V2 or any
other mod/add-on are copied or included.** Tazo and The Watcher appear here as original
game-ready interpretations; official private skins remain with their respective creators.

🟣 THE SYSTEM IS WATCHING.   🟢 THE PORTAL IS OPEN.   🔴 THE STORM IS COMING.
