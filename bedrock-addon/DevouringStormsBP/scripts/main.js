// DEVOURING STORMS — Bedrock behaviour engine.
// Stable @minecraft/server 2.0.0 APIs only — no experiments required.
// The storm's brain on Bedrock: growth, phases, devouring, devolution, play-dead rebirth,
// the Mainframe ritual, rift travel, the reincarnation cycle, and the ambient stalkers.

import { world, system, EntityDamageCause, EquipmentSlot, ItemStack } from "@minecraft/server";
import { BUILDS } from "./builds_data.js";

// ----------------------------------------------------------------------------- state

const PHASES = ["sleeping", "signal", "hunger", "devourer", "sunderer", "bowels", "genesis"];
const PHASE_EVENT = {
  sleeping: "ds:sleeping", signal: "ds:signal", hunger: "ds:hunger",
  devourer: "ds:devourer", sunderer: "ds:sunderer", bowels: "ds:bowels", genesis: "ds:genesis",
};
const PHASE_NAME = {
  sleeping: "M A S S G — dormant", signal: "M A S S G — THE SIGNAL", hunger: "M A S S G — HUNGER",
  devourer: "M A S S G — THE DEVOURER", sunderer: "M A S S G — THE SUNDERER",
  bowels: "M A S S G — THE BOWELS", genesis: "M A S S G — GENESIS",
  husk: "M A S S G — HUSK OF THE STORM",
};
// The fog ladder — the air itself changes colour with the storm.
// signal: blue-teal · hunger/devourer: bruised dark blue · sunderer: deep violet ·
// bowels: dark purple & pink · genesis: near-black with a red rim.
const FOG_BY_PHASE = {
  signal: "ds:fog_signal_teal",
  hunger: "ds:fog_devourer_blue",
  devourer: "ds:fog_devourer_blue",
  sunderer: "ds:fog_sunderer_violet",
  bowels: "ds:fog_bowels_pink",
  genesis: "ds:fog_genesis_black",
};
const PHASE_FOG_TAG = "ds_phasefog";

const AUDIO_LOGS = {
  1: [
    "§8[§7E.P.A.§8] §7FIELD LOG 001 — THE BELL",
    "§8§o> They told us the bell would ring seven times. It only ever rang once.",
    "§8§o> The seventh one isn't a sound. It's a door.",
    "§8§o> If you're hearing this, the quarantine still holds. Stay out of the sky.",
  ],
  2: [
    "§8[§7E.P.A.§8] §7FIELD LOG 002 — THE PLAGUE",
    "§8§o> It started in the soil. We lost the north field by Tuesday.",
    "§8§o> The corruption doesn't rot things. It re-writes them.",
    "§8§o> The livestock came back wrong. Don't let it finish with you.",
  ],
  3: [
    "§8[§7E.P.A.§8] §7FIELD LOG 003 — THE WATCHER",
    "§8§o> We are not the first expedition. We found their banners. Hundreds of them.",
    "§8§o> Something stands at the edge of camp at night. It does not blink. Don't blink either.",
    "§8§o> It drops things. Lures, maybe. The knife it hoards could end all of this.",
  ],
};

// THE MULTIVERSE — pockets on the ring: the breach, the Fray, Echo Fields.
const POCKETS = [
  { key: "center", at: { x: 0, y: 74, z: 0 }, name: "§5the quarantine heart" },
  { key: "fray", at: { x: 1000, y: 74, z: 0 }, name: "§dTHE FRAY§8 — where the stitching goes sideways" },
  { key: "echo", at: { x: -1000, y: 74, z: 0 }, name: "§bECHO FIELDS§8 — the quietest place left" },
];

const PREACHER_LINES = [
  "§8[§5Preacher§8] §7The storm is a mouth. The town is a prayer. Mouths do not finish prayers.",
  "§8[§5Preacher§8] §7We lit ninety-four banners so the ones who came before could find their way home.",
  "§8[§5Preacher§8] §7Do not thank the rifts for the silence. Silence is how it listens.",
  "§8[§5Preacher§8] §7The blueprints were corrupted, child. Nothing built on them owes us mercy.",
  "§8[§5Preacher§8] §7Seven schedules. Seven seals. The vault remembers the combination: M.A.S.S.G.O.O.S.",
  "§8[§5Preacher§8] §7If you meet the Watcher, do not wave. It keeps what it catches you throwing.",
];

const TOWNSFOLK_LINES = [
  "§8[§7Endertonian§8] §8§o...the rot came in through the well, but nobody says that near the Preacher.",
  "§8[§7Endertonian§8] §8§o...ninety-four banners. We counted. The storm only ever took the ones that moved.",
  "§8[§7Endertonian§8] §8§o...the Relay Hall terminal never woke. Probably for the best.",
  "§8[§7Endertonian§8] §8§o...Tazo used to run the market. He still remembers the prices.",
  "§8[§7Endertonian§8] §8§o...the vault in the hall takes seven slips. Seven. We buried four attempts.",
];

const BOWELS_FOG_TEASE = "§d§othe horizon goes purple-pink. something is splitting open.§r";
const HUSK_FALL_BROADCAST = "§8§lTHE STORM FALLS OUT OF THE SKY.§r §5§oIt drags itself across the dirt — the command block still holds it together.§r";
const HUSK_HINT = "§8Only the Storm Killer can rend the heart now. The Watcher hoards them.§8";
const PHASE_MUSIC = {
  signal: "ds.music.signal", hunger: "ds.music.hunger", devourer: "ds.music.devourer",
  sunderer: "ds.music.sunderer", bowels: "ds.music.sunderer", genesis: "ds.music.genesis",
};
const BOWELS_BROADCAST = "§5§lTHE STORM SPLITS OPEN. §d§oTHE BOWELS ARE EXPOSED.";
const CRITICAL_MUSIC = "ds.music.critical";
const DEVOLVE_THRESHOLDS = [0.75, 0.5, 0.25];
const WAKE_BROADCAST = "§5§lM A S S G   I S   W A K I N G   U P.";

const MAINFRAME_TRANSMISSION = [
  "§8[§dMAINFRAME§8] §7> INITIALIZING BREACH PROTOCOL...",
  "§8[§dMAINFRAME§8] §7> THE MAINFRAME HAS BEEN BREACHED.",
  "§8[§dMAINFRAME§8] §7> REALITY QUARANTINE STATUS: §cCOMPROMISED",
  "§8[§dMAINFRAME§8] §7> WITHER STORM BLUEPRINTS.......... §cCORRUPTED",
  "§8[§dMAINFRAME§8] §7> ANOMALY DESIGNATION: §5MASSG",
  "§8[§dMAINFRAME§8] §7> §oMASSIVE ABOMINATION SUNDERING STORM GENESIS",
  "§8[§dMAINFRAME§8] §cWARNING: §7The system was never stable.",
  "§8[§dMAINFRAME§8] §7Something is waiting beyond the portal.",
  "§8[§dMAINFRAME§8] §7THE PORTAL IS OPEN.",
  "§8[§dMAINFRAME§8] §8<transmission ends — next decrypted broadcast: §02027§8>",
];

const TAZO_LINES = [
  "§b<Tazo>§r You finally came. I woke up. It was a stormy night.",
  "§b<Tazo>§r You must destroy the storm before it's too late.",
  "§b<Tazo>§r This place... it took everything. The decay took over.",
  "§b<Tazo>§r Remember the storms. That's all I ask.",
  "§b<Tazo>§r There's always been something lurking down there. Not just the decay.",
  "§b<Tazo>§r A battle is coming. The end is near. Stay close to me.",
  "§b<Tazo>§r If you can hear me, that's great. If you see HER... she isn't real.",
  "§b<Tazo>§r We have been changed forever. Both of us.",
];

const VAULT_PAYLOAD = [
  "§8[§dVAULT§8] §7> SEVEN SCHEDULES ACCEPTED.",
  "§8[§dVAULT§8] §7> PASSWORD: §5§oM.A.S.S.G.O.O.S",
  "§8[§dVAULT§8] §7> ARG ARCHIVE UNSEALED.",
  "§8[§dVAULT§8] §7> payload 1/2 — the storm was never the anomaly. §oyou were.§r",
  "§8[§dVAULT§8] §7> payload 2/2 — coordinates: §0██°██'N ██°██'W§r §8— declassifies §02027§8.",
  "§8[§dVAULT§8] §8<the vault hums. something on the other end logged your face.>",
];

const VAULT_REWARD = [
  ["ds:classified_payload", 1], ["ds:commanded_star", 1], ["ds:music_disc_changed", 1],
  ["minecraft:echo_shard", 6], ["minecraft:diamond", 5],
];

const ANNA_LINES = [
  "§8<§7Anna§8>§o Anna isn't real.",
  "§8<§7Anna§8>§o This world is an illusion.",
  "§8<§7Anna§8>§o I'm sorry I didn't come to see you a long time ago.",
  "§8<§7Anna§8>§o I died 2 years after. But don't worry.",
  "§8<§7Anna§8>§o Rest, my boy. Dream of the ones that came before.",
  "§8<§7Anna§8>§o We were sleepwalking into the flames. We were almost okay.",
  "§8<§7Anna§8>§o We were waiting for the ships to carry us home.",
];

const END_PLATFORM = { x: 0, y: 74, z: 0 };
const musicIdsByPhase = [...Object.values(PHASE_MUSIC), CRITICAL_MUSIC, "ds.ambient.decayed_loop"];

const portalDwell = new Map();      // uuid -> ticks stood in a rift
const portalCooldown = new Map();   // uuid -> world time until portal works again
const pendingCycle = new Set();     // player uuids that died in the Decayed Realm
const lastMusic = new Map();        // player uuid -> last music event id
const watcherFogOn = new Set();     // player uuids currently inside the watcher paranoia fog

// ----------------------------------------------------------------------------- helpers

const dims = () => ["minecraft:overworld", "minecraft:nether", "minecraft:the_end"].map(w => world.getDimension(w));
const endDim = () => world.getDimension("minecraft:the_end");
const owDim = () => world.getDimension("minecraft:overworld");

function prop(entity, key, fallback) {
  const v = entity.getDynamicProperty(key);
  return v === undefined ? fallback : v;
}
function setProp(entity, key, value) {
  entity.setDynamicProperty(key, value);
}
function phaseOf(storm) {
  return prop(storm, "ds_phase", "sleeping");
}
function isPlayingDead(storm) {
  return phaseOf(storm) === "play_dead";
}
function isCritical(storm) {
  return prop(storm, "ds_critical", false) === true;
}

function setPhase(storm, phase, opts = {}) {
  if (!storm.isValid) return;
  setProp(storm, "ds_phase", phase);
  storm.triggerEvent(PHASE_EVENT[phase] ?? "ds:sleeping");
  if (opts.announce !== false) {
    broadcastNear(storm, 320, `§5§l${PHASE_NAME[phase]}`);
    storm.dimension.playSound("ds.massg.roar", storm.location, { volume: 4.0, pitch: 0.7 });
  }
  if (phase === "signal") {
    broadcastNear(storm, 320, WAKE_BROADCAST);
    storm.dimension.playSound("ds.massg.awakening", storm.location, { volume: 4.0, pitch: 0.9 });
  }
  if (phase === "bowels") {
    // phase 5.5 — the storm splits open: the horizon flushes pink, then the full
    // rupture cinematic plays out (split, purple pour, rise, shockwave)
    broadcastNear(storm, 480, BOWELS_FOG_TEASE);
    broadcastNear(storm, 320, BOWELS_BROADCAST);
    storm.dimension.playSound("ds.massg.devolve_sting", storm.location, { volume: 4.0, pitch: 0.55 });
    storm.dimension.playSound("ds.massg.rebirth", storm.location, { volume: 3.5, pitch: 0.65 });
    runBowelsCinematic(storm);
  }
  swapPhaseFog(storm, phase);
  playStormMusic(storm, phase);
  if (cfgOn("title_cards")) titleCardForPhase(storm, phase);
}

/** The storm paints the air in its own colour as it climbs the ladder. */
function swapPhaseFog(storm, phase) {
  const fog = FOG_BY_PHASE[phase];
  for (const p of playersNear(storm, 320)) {
    safeCmd(p, `fog @s pop ${PHASE_FOG_TAG}`);
    if (fog) safeCmd(p, `fog @s push "${fog}" ${PHASE_FOG_TAG}`);
  }
}

/** The phase-5.5 rupture, staged out across four seconds. */
function runBowelsCinematic(storm) {
  const dim = storm.dimension;
  const base = { ...storm.location };
  const stage = (ticks, fn) => system.runTimeout(() => { try { fn(); } catch { /* storm left the stage */ } }, ticks);

  // SPLIT (0-30): the belly tears, segments shed down to the ground
  for (let t = 0; t <= 30; t += 5) {
    stage(t, () => {
      for (let i = 0; i < 6; i++) {
        dim.spawnParticle("ds:glitch", {
          x: base.x + (Math.random() - 0.5) * 5,
          y: base.y - 2 - Math.random() * 2,
          z: base.z + (Math.random() - 0.5) * 5,
        });
        dim.spawnParticle("ds:devour_pull", {
          x: base.x + (Math.random() - 0.5) * 4,
          y: base.y - 1,
          z: base.z + (Math.random() - 0.5) * 4,
        });
      }
      dim.playSound("ds.massg.roar", base, { volume: 1.6, pitch: 0.5 });
    });
  }
  // POUR (35-60): purple liquid streams out and pools in the air
  for (let t = 35; t <= 60; t += 5) {
    stage(t, () => {
      for (let i = 0; i < 10; i++) {
        dim.spawnParticle("ds:glitch", {
          x: base.x + (Math.random() - 0.5) * 2,
          y: base.y - 3 - (t - 35) * 0.14,
          z: base.z + (Math.random() - 0.5) * 2,
        });
      }
    });
  }
  // RISE (65-85): the storm reels the pour back up into itself
  for (let t = 65; t <= 85; t += 5) {
    stage(t, () => {
      for (let i = 0; i < 8; i++) {
        dim.spawnParticle("ds:glitch", {
          x: base.x + (Math.random() - 0.5) * 3,
          y: base.y - 6 + (t - 65) * 0.24,
          z: base.z + (Math.random() - 0.5) * 3,
        });
      }
    });
  }
  // SHOCKWAVE (90): one vast ring of everything it is made of
  stage(90, () => {
    for (let ring = 0; ring < 3; ring++) {
      const r = 4 + ring * 3.2;
      for (let i = 0; i < 28; i++) {
        const a = (i / 28) * Math.PI * 2;
        dim.spawnParticle("ds:sky_flash", {
          x: base.x + Math.cos(a) * r,
          y: base.y - 1 + ring * 0.4,
          z: base.z + Math.sin(a) * r,
        });
        dim.spawnParticle("ds:glitch", {
          x: base.x + Math.cos(a) * (r * 0.7),
          y: base.y - 2,
          z: base.z + Math.sin(a) * (r * 0.7),
        });
      }
    }
    dim.playSound("ds.massg.roar", base, { volume: 4.0, pitch: 0.45 });
    broadcastNear(storm, 480, "§d§lTHE BOWELS HAVE FORMED. §oAnd the purple pours on.§r");
  });
}

function setCritical(storm) {
  if (!storm.isValid || isCritical(storm)) return;
  setProp(storm, "ds_critical", true);
  storm.triggerEvent("ds:devolve");
  broadcastNear(storm, 320, "§4§oThe air itself is screaming.");
  playStormMusic(storm, phaseOf(storm));
}

function playStormMusic(storm, phase) {
  const wanted = isCritical(storm) ? CRITICAL_MUSIC : PHASE_MUSIC[phase];
  if (!wanted) return;
  for (const player of playersNear(storm, 256)) {
    if (lastMusic.get(player.id) === wanted) continue;
    for (const id of musicIdsByPhase) safeCmd(player, `stopsound @s "${id}"`);
    safeCmd(player, `playsound "${wanted}" @s ~ ~ ~ 1.0 1.0`);
    lastMusic.set(player.id, wanted);
  }
}

function playersNear(entity, radius) {
  return entity.dimension.getPlayers({ location: entity.location, maxDistance: radius });
}
function broadcastNear(entity, radius, text) {
  for (const p of playersNear(entity, radius)) p.sendMessage(text);
}
function safeCmd(entity, cmd) {
  try { entity.runCommand(cmd); } catch { /* storm-tolerant */ }
}
function toward(a, b) { // direction a -> b
  const dx = b.x - a.x, dy = b.y - a.y, dz = b.z - a.z;
  const l = Math.hypot(dx, dy, dz) || 1;
  return { x: dx / l, y: dy / l, z: dz / l };
}
function dist(a, b) { return Math.hypot(a.x - b.x, a.y - b.y, a.z - b.z); }

function healthOf(entity) {
  const h = entity.getComponent("health");
  return h ? { cur: h.currentValue, max: h.defaultValue ? h.effectiveMax ?? h.defaultValue : 700 } : null;
}

function hasItem(player, id) {
  const inv = player.getComponent("inventory")?.container;
  if (!inv) return false;
  for (let i = 0; i < inv.size; i++) {
    if (inv.getItem(i)?.typeId === id) return true;
  }
  return false;
}

function consumeHeldItem(player) {
  const eq = player.getComponent("equippable");
  if (!eq) return;
  const slot = eq.getEquipmentSlot(EquipmentSlot.Mainhand);
  const item = slot.getItem();
  if (!item) return;
  if (item.amount > 1) { item.amount -= 1; slot.setItem(item); } else { slot.setItem(undefined); }
}

// ----------------------------------------------------------------------------- mainboot

system.runInterval(stormDirectorTick, 20);
system.runInterval(fastTick, 4);
system.runInterval(watcherTick, 200);
system.runInterval(annaTick, 400);
system.runInterval(portalTick, 10);
system.runInterval(amuletTick, 80);
system.runInterval(skyFlashTick, 30);
system.runInterval(infectionTick, 100);
system.runInterval(decaySpreadTick, 160);
system.runInterval(preacherTick, 360);
system.runInterval(multiverseNpcTick, 340);
system.runInterval(tractorBeamTick, 8);
system.runInterval(quakeTick, 60);
system.runInterval(voidMawTick, 6);
system.runInterval(bellyTick, 15);

// ================================================== THE STORM
function stormDirectorTick() {
  for (const dim of dims()) {
    const storms = dim.getEntities({ families: ["ds_massg"] });
    for (const storm of storms) {
      if (!storm.isValid) continue;
      const phase = phaseOf(storm);
      if (phase === "husk") continue;   // it waits to be rent; it does not hunt

      // ---------- playing dead: the corrupted blueprints win, briefly ----------
      if (phase === "play_dead") {
        const reviveAt = prop(storm, "ds_revive_at", 0);
        if (world.getTime() >= reviveAt) {
          const pos = { x: storm.location.x, y: storm.location.y, z: storm.location.z };
          const genesis = dim.spawnEntity("ds:massg", pos);
          genesis.setDynamicProperty("ds_phase", "genesis");
          genesis.triggerEvent("ds:genesis");
          genesis.setDynamicProperty("ds_critical", true);
          const gh = genesis.getComponent("health");
          if (gh) gh.setCurrentValue(420);
          genesis.nameTag = PHASE_NAME.genesis;
          storm.triggerEvent("ds:true_dead");
          dim.playSound("ds.massg.rebirth", pos, { volume: 4.0, pitch: 0.7 });
          broadcastNear(genesis, 400, "§5§lTHE BLUEPRINTS WERE CORRUPTED. §r§5MASSG RISES AS GENESIS.");
          playStormMusic(genesis, "genesis");
        }
        continue;
      }

      // ---------- growth ----------
      let growth = prop(storm, "ds_growth", 0);
      if (phase === "sleeping") {
        const witnesses = playersNear(storm, 120);
        if (witnesses.length > 0) growth += 0.06; // it wakes when observed
      } else {
        growth += 0.02 + PHASES.indexOf(phase) * 0.008; // passive signal gain
      }
      if (growth >= 1 && phase !== "genesis") {
        const next = PHASES[Math.min(PHASES.indexOf(phase) + 1, PHASES.length - 1)];
        setProp(storm, "ds_growth", 0);
        setPhase(storm, next);
        storm.nameTag = PHASE_NAME[next];
      } else {
        setProp(storm, "ds_growth", growth);
      }

      // ---------- if you let it live, it grows. It does not stop. ----------
      if (phase === "genesis") {
        const overgrowth = prop(storm, "ds_overgrowth", 0);
        if (overgrowth > 0 && overgrowth % 40 === 0) {
          const h2 = healthOf(storm);
          if (h2) h2.setCurrentValue(h2.cur + 4);
          if (Math.random() < 0.3) broadcastNear(storm, 320, "§5§oTHE STORM STILL GROWS.");
        }
        setProp(storm, "ds_overgrowth", overgrowth + 1);
      }

      // ---------- passive regen ----------
      const h = healthOf(storm);
      if (h && phase !== "sleeping" && h.cur < h.max) h.setCurrentValue(Math.min(h.max, h.cur + 1.5));

      // ---------- devouring the world (block absorption) ----------
      const phaseIdx = PHASES.indexOf(phase);
      if (phaseIdx >= 2 && world.getTime() % 20 === 0) absorbBlocks(storm, dim);
    }
  }
}

function absorbBlocks(storm, dim) {
  const base = storm.location;
  for (let i = 0; i < 10; i++) {
    const p = {
      x: Math.floor(base.x + (Math.random() - 0.5) * 14),
      y: Math.floor(base.y - 6 + Math.random() * 12),
      z: Math.floor(base.z + (Math.random() - 0.5) * 14),
    };
    // never eat the arrival platform
    if (dim.id === "minecraft:the_end" && Math.abs(p.x) < 12 && Math.abs(p.z) < 12) continue;
    const block = dim.getBlock(p);
    if (!block || block.isAir) continue;
    const id = block.typeId;
    if (id === "minecraft:bedrock" || id.startsWith("ds:") || id.includes("portal")) continue;
    if (Math.random() > 0.25) continue;
    block.setTypeId(dim.id === "minecraft:the_end" ? "ds:decayed_stone" : "minecraft:air");
    setProp(storm, "ds_growth", prop(storm, "ds_growth", 0) + 0.008);
    dim.spawnParticle("ds:glitch", { x: p.x + 0.5, y: p.y + 0.5, z: p.z + 0.5 });
  }
}

// ================================================== fast loop: devour pulls, vortex, severed
const beams = new Map(); // storm id -> { victims: Entity[], until: worldtime }

function fastTick() {
  const now = world.getTime();
  for (const dim of dims()) {
    for (const storm of dim.getEntities({ families: ["ds_massg"] })) {
      if (!storm.isValid) continue;
      const phase = phaseOf(storm);
      if (phase === "play_dead") continue;
      const phaseIdx = PHASES.indexOf(phase);
      if (phaseIdx < 2) continue;

      // -------- genesis vortex + essence storm --------
      if (phase === "genesis" && now % 10 === 0) {
        for (const e of dim.getEntities({ location: storm.location, maxDistance: 26 })) {
          if (e === storm) continue;
          if (e.typeId === "ds:massg" || e.typeId === "ds:severed_storm") continue;
          const d = toward(e.location, storm.location);
          try { e.applyImpulse({ x: d.x * 0.06, y: d.y * 0.03, z: d.z * 0.06 }); } catch { /* ignore */ }
          if (dist(e.location, storm.location) < 8 && e.typeId !== "minecraft:item") {
            try { e.applyDamage(4, { cause: EntityDamageCause.magic, damagingEntity: storm }); } catch { /* gone */ }
          }
        }
      }

      // -------- sunder: tear fragments off --------
      if (phaseIdx >= 4 && now % 800 === 0) {
        const severed = dim.getEntities({ families: ["ds_severed"], location: storm.location, maxDistance: 96 });
        if (severed.length < 6) {
          const s = dim.spawnEntity("ds:severed_storm", {
            x: storm.location.x + (Math.random() - 0.5) * 12,
            y: storm.location.y + 2,
            z: storm.location.z + (Math.random() - 0.5) * 12,
          });
          s.nameTag = "Severed Storm";
        }
      }

      // -------- phase 5.5+: the exposed bowels bleed violet light --------
      if (phaseIdx >= 5 && now % 6 === 0) {
        const s = 2.2 + (phaseIdx - 5) * 0.4;
        for (let k = 0; k < 3; k++) {
          dim.spawnParticle("ds:glitch", {
            x: storm.location.x + (Math.random() - 0.5) * 2 * s,
            y: storm.location.y - 1.5 + Math.random() * 2,
            z: storm.location.z + (Math.random() - 0.5) * 2 * s,
          });
        }
        if (now % 120 === 0) dim.playSound("ds.massg.pull_loop", storm.location, { volume: 1.6, pitch: 0.5 });
      }

      // -------- tractor beam: reel them in --------
      let beam = beams.get(storm.id);
      if (beam && now < beam.until) {
        for (const v of beam.victims) {
          if (!v.isValid) continue;
          const mouth = { x: storm.location.x, y: storm.location.y + 3, z: storm.location.z };
          const d = toward(v.location, mouth);
          try { v.applyImpulse({ x: d.x * 0.14, y: d.y * 0.10, z: d.z * 0.14 }); } catch { /* ignore */ }
          if (now % 4 === 0) dim.spawnParticle("ds:devour_pull", mouth);
          if (dist(v.location, mouth) < 5) {
            if (v.typeId === "minecraft:player") {
              try {
                v.applyDamage(12, { cause: EntityDamageCause.entityAttack, damagingEntity: storm });
                v.addEffect("minecraft:wither", 120, { amplifier: 0 });
                v.applyImpulse({ x: -d.x * 1.6, y: 0.5, z: -d.z * 1.6 });
              } catch { /* gone */ }
            } else {
              try { v.applyDamage(80, { cause: EntityDamageCause.entityAttack, damagingEntity: storm }); } catch { /* eaten */ }
              setProp(storm, "ds_growth", prop(storm, "ds_growth", 0) + 0.04);
            }
            dim.playSound("ds.massg.devour", mouth, { volume: 3.0, pitch: 0.9 });
          }
        }
      } else if (now % 200 === 0 && phaseIdx >= 2) {
        // pick up to 3 victims
        const candidates = dim.getEntities({ location: storm.location, maxDistance: 44 })
          .filter(e => e.isValid && e !== storm
            && !["ds:massg", "ds:severed_storm", "ds:watcher", "ds:anna_apparition", "minecraft:item"].includes(e.typeId));
        if (candidates.length > 0) {
          const victims = candidates.sort(() => Math.random() - 0.5).slice(0, 3);
          beams.set(storm.id, { victims, until: now + 80 });
          dim.playSound("ds.massg.pull_loop", storm.location, { volume: 3.0, pitch: 1.0 });
        }
      }
    }
  }
}

// ================================================== MCSM sky: far thunder in the cloud bank
// The Wither Storm's sky from Story Mode: bruised cloud banks and violet tears of
// far-off lightning while the storm feeds. (The Iris shader pack paints the Java sky.)
function skyFlashTick() {
  const now = world.getTime();
  for (const dim of dims()) {
    for (const storm of dim.getEntities({ families: ["ds_massg"] })) {
      if (!storm.isValid) continue;
      const idx = PHASES.indexOf(phaseOf(storm));
      if (idx < 3) continue;                       // devourer and beyond darkens the sky
      if (Math.random() > 0.22) continue;          // ~every 7 seconds
      for (const p of playersNear(storm, 320)) {
        p.spawnParticle("ds:sky_flash", {
          x: p.location.x + (Math.random() - 0.5) * 60,
          y: p.location.y + 24 + Math.random() * 12,
          z: p.location.z + (Math.random() - 0.5) * 60,
        });
        if (now % 90 === 0) p.playSound("ds.massg.roar", { volume: 0.5, pitch: 0.4 });
      }
    }
  }
}

// ================================================== THE INFECTION (mobs)
// "Then the plague came." Decay near the storm — or simply breathing the realm's
// air — converts the hostile living into Withered Symbionts.
function infectionTick() {
  for (const dim of dims()) {
    const inRealm = dim.id === "minecraft:the_end";
    const storms = dim.getEntities({ families: ["ds_massg"] })
      .filter(s => s.isValid && !isPlayingDead(s) && PHASES.indexOf(phaseOf(s)) >= 2);
    for (const player of dim.getPlayers()) {
      const mobs = dim.getEntities({ families: ["monster"], location: player.location, maxDistance: 40 });
      for (const mob of mobs) {
        if (!mob.isValid || mob.nameTag) continue;
        if (["ds:withered_symbiont", "ds:severed_storm", "ds:tazo", "ds:anna_apparition",
             "ds:watcher", "minecraft:ender_dragon", "minecraft:warden"].includes(mob.typeId)) continue;
        const nearStorm = storms.some(s => dist(s.location, mob.location) < 48);
        let exposed = inRealm || nearStorm;
        if (!exposed) {
          try { exposed = dim.getBlock(mob.location)?.typeId === "ds:decay_block"; } catch { exposed = false; }
        }
        if (!exposed) continue;
        if (Math.random() > (inRealm ? 0.08 : 0.04)) continue;
        try {
          const sym = dim.spawnEntity("ds:withered_symbiont", mob.location,
            { initialRotation: mob.getRotation().y });
          sym.nameTag = "Withered Symbiont";
          dim.spawnParticle("ds:glitch", mob.location);
          mob.remove();
          for (const p of playersNear(sym, 32)) {
            if (Math.random() < 0.15) p.sendMessage("§8§oThe corruption took something nearby.");
          }
        } catch { /* the rot keeps it this time */ }
      }
      // ---- THE TAKEN: villagers hold out the longest, then the decay wins ----
      for (const villager of dim.getEntities({ type: "minecraft:villager", location: player.location, maxDistance: 40 })) {
        if (!villager.isValid) continue;
        const nearStorm = storms.some(sv => dist(sv.location, villager.location) < 48);
        if (!inRealm && !nearStorm) continue;
        if (Math.random() > (inRealm ? 0.05 : 0.02)) continue;
        try {
          const taken = dim.spawnEntity("ds:the_taken", villager.location,
            { initialRotation: villager.getRotation().y });
          taken.nameTag = "§2The Taken";
          dim.spawnParticle("ds:glitch", villager.location);
          villager.remove();
          player.sendMessage("§2§oYou hear a door that will never open again.§r");
        } catch { /* so be it */ }
      }
    }
  }
}

// ================================================== THE INFECTION (terrain)
// The rot creeps: decay blocks slowly convert neighbouring terrain, one block per pulse.
const DECAY_CONVERT = new Map([
  ["minecraft:stone", "ds:decayed_stone"], ["minecraft:cobblestone", "ds:decayed_stone"],
  ["minecraft:deepslate", "ds:decayed_stone"], ["minecraft:end_stone", "ds:decayed_stone"],
  ["minecraft:dirt", "ds:decayed_soil"], ["minecraft:grass_block", "ds:decayed_soil"],
  ["minecraft:sand", "ds:decayed_soil"], ["minecraft:gravel", "ds:decayed_soil"],
]);
const DECAY_NEIGHBOURS = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]];

function decaySpreadTick() {
  for (const dim of dims()) {
    for (const player of dim.getPlayers()) {
      const px = Math.floor(player.location.x), py = Math.floor(player.location.y), pz = Math.floor(player.location.z);
      const src = {
        x: px + Math.floor(Math.random() * 11) - 5,
        y: Math.max(dim.heightRange.min, Math.min(dim.heightRange.max - 1, py + Math.floor(Math.random() * 7) - 3)),
        z: pz + Math.floor(Math.random() * 11) - 5,
      };
      let block;
      try { block = dim.getBlock(src); } catch { continue; }
      if (!block || block.typeId !== "ds:decay_block") continue;
      const off = DECAY_NEIGHBOURS[Math.floor(Math.random() * 6)];
      let target;
      try { target = dim.getBlock({ x: src.x + off[0], y: src.y + off[1], z: src.z + off[2] }); } catch { continue; }
      if (!target) continue;
      let replace = DECAY_CONVERT.get(target.typeId);
      if (!replace && (target.typeId.includes("_log") || target.typeId.includes("_wood"))) replace = "ds:rot_log";
      if (replace) {
        try { target.setTypeId(replace); } catch { /* protected ground */ }
      }
    }
  }
}

// ================================================== world.afterEvents

world.afterEvents.entityHealthChanged.subscribe((event) => {
  const entity = event.entity;
  if (!entity?.typeId || entity.typeId !== "ds:massg") return;
  const h = healthOf(entity);
  if (!h) return;
  const frac = h.cur / h.max;

  // devolution thresholds: it loses a threshold of itself, and answers
  let idx = prop(entity, "ds_devolve_index", 0);
  if (idx < DEVOLVE_THRESHOLDS.length && frac <= DEVOLVE_THRESHOLDS[idx]) {
    setProp(entity, "ds_devolve_index", idx + 1);
    setCritical(entity);
    const dim = entity.dimension;
    dim.playSound("ds.massg.devolve_sting", entity.location, { volume: 4.0, pitch: 0.8 });
    for (let i = 0; i < 2; i++) {
      dim.spawnEntity("ds:withered_symbiont", {
        x: entity.location.x + (Math.random() - 0.5) * 6,
        y: entity.location.y,
        z: entity.location.z + (Math.random() - 0.5) * 6,
      });
    }
    broadcastNear(entity, 320, `§c§l${PHASE_NAME[phaseOf(entity)] ?? "M A S S G"} §4IS DEVOLVING...`);
    const hp = healthOf(entity);
    if (hp) hp.setCurrentValue(Math.min(hp.max, hp.cur + hp.max * 0.08)); // it adapts
  }
  if (frac < 0.3) setCritical(entity);
});

world.afterEvents.entityDie.subscribe((event) => {
  const dead = event.deadEntity;

  // -- the reincarnation cycle --
  if (dead?.typeId === "minecraft:player" && dead.dimension.id === "minecraft:the_end") {
    pendingCycle.add(dead.id);
    return;
  }

  // -- the apparition leaves Schedule IV behind, sometimes --
  if (dead?.typeId === "ds:anna_apparition" && Math.random() < 0.5) {
    try { dead.dimension.spawnItem(new ItemStack("ds:schedule_4", 1), dead.location); } catch { /* not real anyway */ }
    return;
  }

  if (dead?.typeId !== "ds:massg") return;

  // -- MASSG death is a fork: formidibomb or nothing --
  const src = event.damageCause;
  const proj = src?.damagingProjectile;
  if (proj && proj.typeId === "ds:formidibomb") {
    // TRUE DEATH. Loot flows through the loot table; the sky exhales.
    broadcastAll("§a§lTHE STORM IS ENDED. §r§7...somewhere, a countdown keeps running.");
    dead.dimension.playSound("ds.massg.true_death", dead.location, { volume: 4.0, pitch: 0.6 });
    stopAllStormMusic();
    return;
  }

  // -- GENESIS falls wrong: not a formidibomb, not an ending — the husk walks the dirt --
  const phaseAtDeath = dead.getDynamicProperty("ds_phase");
  if (phaseAtDeath === "genesis") {
    const pos2 = { x: dead.location.x, y: dead.location.y, z: dead.location.z };
    const dim2 = dead.dimension;
    system.runTimeout(() => {
      const husk = dim2.spawnEntity("ds:massg", pos2);
      husk.setDynamicProperty("ds_phase", "husk");
      husk.setDynamicProperty("ds_core_hits", 0);
      husk.triggerEvent("ds:husk");
      husk.nameTag = PHASE_NAME.husk;
      const hh = husk.getComponent("health");
      if (hh) hh.setCurrentValue(400);
      dim2.playSound("ds.massg.play_dead", pos2, { volume: 4.0, pitch: 0.55 });
      broadcastAll(HUSK_FALL_BROADCAST);
      broadcastAll(HUSK_HINT);
      stopAllStormMusic();
    }, 4);
    return;
  }

  // THE BLUEPRINTS ARE CORRUPTED. It does not die. It plays dead.
  const pos = { x: dead.location.x, y: dead.location.y, z: dead.location.z };
  const dim = dead.dimension;
  system.runTimeout(() => {
    const shell = dim.spawnEntity("ds:massg", pos);
    shell.setDynamicProperty("ds_phase", "play_dead");
    shell.setDynamicProperty("ds_revive_at", world.getTime() + 600);
    shell.setDynamicProperty("ds_critical", true);
    shell.triggerEvent("ds:play_dead");
    shell.nameTag = "M A S S G — playing dead";
    const h = shell.getComponent("health");
    if (h) h.setCurrentValue(40);
    try { shell.addEffect("minecraft:resistance", 700, { amplifier: 5, showParticles: false }); } catch { /* tough already */ }
    dim.playSound("ds.massg.play_dead", pos, { volume: 4.0, pitch: 0.7 });
    broadcastAll("§8§oThe storm's light fades... §r§7§oit is only sleeping. §r§4§oUse a Formidibomb. END IT.");
    playStormMusic(shell, "genesis");
  }, 2);
});

world.afterEvents.playerSpawn.subscribe((event) => {
  const player = event.player;
  if (!pendingCycle.has(player.id)) return;
  pendingCycle.delete(player.id);
  system.runTimeout(() => {
    ensureDecayedPlatform(endDim());
    ensureEndertown(endDim());
    player.teleport({ x: END_PLATFORM.x + 0.5, y: END_PLATFORM.y + 1, z: END_PLATFORM.z + 3.5 }, { dimension: endDim() });
    safeCmd(player, `fog @s push "ds:fog_decayed_reality" mist`);
    player.onScreenDisplay.setTitle("§5§lT H E   C Y C L E   C O N T I N U E S.");
  }, 10);
});

function stopAllStormMusic() {
  for (const player of world.getAllPlayers()) {
    for (const id of musicIdsByPhase) safeCmd(player, `stopsound @s "${id}"`);
    lastMusic.delete(player.id);
  }
}

function broadcastAll(text) {
  world.sendMessage(text);
}

// ================= projectile hits: the Formidibomb and the storm's skulls
world.afterEvents.projectileHitEntity.subscribe((event) => {
  const proj = event.projectile;
  if (!proj || proj.typeId !== "ds:formidibomb") return;
  const hit = event.getEntityHit?.()?.entity;
  explodeAt(proj.dimension, proj.location, hit);
});
world.afterEvents.projectileHitBlock.subscribe((event) => {
  const proj = event.projectile;
  if (!proj || proj.typeId !== "ds:formidibomb") return;
  explodeAt(proj.dimension, proj.location, null);
});

function explodeAt(dim, pos, directHit) {
  try {
    dim.createExplosion(pos, 2.5, { breaksBlocks: false, causesFire: false });
  } catch {
    dim.spawnParticle("ds:glitch", pos);
    dim.playSound("ds.glitch", pos, { volume: 2.0 });
  }
  if (directHit && directHit.typeId === "ds:massg") {
    // the payload: the only thing that truly kills
    try { directHit.applyDamage(150, { cause: EntityDamageCause.entityExplosion, damagingEntity: undefined }); } catch { /* done */ }
  }
}

// ================================================== THE WATCHER
function watcherTick() {
  const now = world.getTime();

  // ---- spawn attempts: behind you, in the dark ----
  for (const player of world.getAllPlayers()) {
    const dimId = player.dimension.id;
    const inDecayed = dimId === "minecraft:the_end";
    const night = dimId === "minecraft:overworld" && (world.getTimeOfDay() > 13000 && world.getTimeOfDay() < 23000);
    if (!(inDecayed || night)) continue;
    if (Math.random() > (inDecayed ? 0.5 : 0.3)) continue;

    const existing = player.dimension.getEntities({ families: ["ds_watcher"], location: player.location, maxDistance: 96 });
    if (existing.length > 0) continue;

    const view = player.getViewDirection();
    const behind = {
      x: player.location.x - view.x * (14 + Math.random() * 12) + (Math.random() - 0.5) * 8,
      y: player.location.y,
      z: player.location.z - view.z * (14 + Math.random() * 12) + (Math.random() - 0.5) * 8,
    };
    const watcher = player.dimension.spawnEntity("ds:watcher", behind);
    watcher.setDynamicProperty("ds_gaze", 0);
    player.dimension.playSound("ds.watcher.whisper", behind, { volume: 1.0, pitch: 0.8 });
  }

  // ---- gaze detection + heartbeat ----
  for (const dim of dims()) {
    for (const watcher of dim.getEntities({ families: ["ds_watcher"] })) {
      const near = playersNear(watcher, 80);
      if (near.length === 0) { watcher.setDynamicProperty("ds_gaze", 0); continue; }

      // heartbeat + paranoia fog for the close ones
      const PARANOIA_TAG = "watcherfog";
      if (now % 55 === 0) {
        for (const p of near) {
          if (dist(p.location, watcher.location) < 20) {
            dim.playSound("ds.watcher.heartbeat", watcher.location, { volume: 0.9, pitch: 0.85 });
          }
        }
      }
      for (const p of near) {
        const d2 = dist(p.location, watcher.location);
        if (d2 < 28) {
          if (!watcherFogOn.has(p.id)) {
            safeCmd(p, `fog @s push "ds:fog_watcher" ${PARANOIA_TAG}`);
            watcherFogOn.add(p.id);
          }
        } else if (watcherFogOn.has(p.id)) {
          safeCmd(p, `fog @s pop ${PARANOIA_TAG}`);
          watcherFogOn.delete(p.id);
        }
      }

      // gaze: every 200-tick scan, add to the closest looker's tally
      const looker = near
        .filter(p => {
          const view = p.getViewDirection();
          const dir = toward(p.getHeadLocation(), watcher.location);
          const dot = view.x * dir.x + view.y * dir.y + view.z * dir.z;
          if (dot < 0.9) return false;
          const cast = dim.getBlockFromRay(p.getHeadLocation(), dir, { maxDistance: dist(p.location, watcher.location) });
          return !cast?.block; // nothing between the looker and the gaze
        })
        .at(0);

      if (!looker) {
        watcher.setDynamicProperty("ds_gaze", Math.max(0, prop(watcher, "ds_gaze", 0) - 1));
        continue;
      }

      const gaze = prop(watcher, "ds_gaze", 0) + 1;
      watcher.setDynamicProperty("ds_gaze", gaze);
      if (gaze === 3) {
        looker.sendMessage("§8§oYou feel the weight of a silent gaze...");
        dim.playSound("ds.watcher.whisper", looker.location, { volume: 1.2, pitch: 0.8 });
      }
      if (gaze >= 12) {
        // it has been seen too well. It leaves its mark — and its eye.
        looker.addEffect("minecraft:darkness", 240, { amplifier: 0 });
        looker.addEffect("minecraft:slowness", 200, { amplifier: 1 });
        looker.sendMessage("§8§oYou should not have watched it back.");
        if (Math.random() < 0.5) {
          dim.spawnItem("ds:watcher_eye", watcher.location);
        }
        dim.playSound("ds.watcher.vanish", watcher.location, { volume: 1.5, pitch: 1.0 });
        dim.spawnParticle("ds:glitch", watcher.location);
        watcher.remove();
      }
    }
  }

  // ---- Tazo chatter ----
  if (world.getTime() % 1400 === 0) {
    for (const dim of dims()) {
      for (const tazo of dim.getEntities({ families: ["ds_tazo"] })) {
        const near = playersNear(tazo, 20);
        if (near.length === 0) continue;
        const line = TAZO_LINES[Math.floor(Math.random() * TAZO_LINES.length)];
        near[0].sendMessage(line);
      }
    }
  }
}

// ================================================== ANNA
function annaTick() {
  const end = endDim();
  const playersInEnd = end.getPlayers();
  if (playersInEnd.length === 0) return;

  // she appears where you are about to look
  if (Math.random() < 0.35) {
    const target = playersInEnd[Math.floor(Math.random() * playersInEnd.length)];
    const existing = end.getEntities({ families: ["ds_anna"], location: target.location, maxDistance: 130 });
    if (existing.length === 0) {
      const view = target.getViewDirection();
      const ahead = {
        x: target.location.x + view.x * (18 + Math.random() * 10),
        y: target.location.y + 0.5,
        z: target.location.z + view.z * (18 + Math.random() * 10),
      };
      const anna = end.spawnEntity("ds:anna_apparition", ahead);
      anna.setDynamicProperty("ds_gaze", 0);
      end.playSound("ds.anna.giggle", ahead, { volume: 0.7, pitch: 1.1 });
    }
  }

  // dissolve on close scrutiny
  for (const anna of end.getEntities({ families: ["ds_anna"] })) {
    const near = playersNear(anna, 28);
    if (near.length === 0) continue;
    const closest = near.sort((a, b) => dist(a.location, anna.location) - dist(b.location, anna.location))[0];

    const view = closest.getViewDirection();
    const dir = toward(closest.getHeadLocation(), anna.location);
    const dot = view.x * dir.x + view.y * dir.y + view.z * dir.z;
    const gazing = dot > 0.9 || dist(closest.location, anna.location) < 3.5;

    if (!gazing) {
      anna.setDynamicProperty("ds_gaze", Math.max(0, prop(anna, "ds_gaze", 0) - 1));
      continue;
    }
    const gaze = prop(anna, "ds_gaze", 0) + 1;
    anna.setDynamicProperty("ds_gaze", gaze);
    if (gaze >= 8) {
      const line = ANNA_LINES[Math.floor(Math.random() * ANNA_LINES.length)];
      closest.sendMessage(line);
      end.playSound("ds.glitch", anna.location, { volume: 2.0 });
      for (let i = 0; i < 8; i++) {
        end.spawnParticle("ds:glitch", {
          x: anna.location.x + (Math.random() - 0.5),
          y: anna.location.y + Math.random() * 1.8,
          z: anna.location.z + (Math.random() - 0.5),
        });
      }
      end.spawnItem("ds:memory_fragment", anna.location);
      anna.remove();
    }
  }
}

// ================================================== rift portal dwell / travel
function portalTick() {
  const now = world.getTime();
  for (const player of world.getAllPlayers()) {
    const atUntil = portalCooldown.get(player.id);
    if (atUntil !== undefined && now < atUntil) continue;

    const feet = player.dimension.getBlock(player.location);
    const head = player.dimension.getBlock({ x: player.location.x, y: player.location.y + 1, z: player.location.z });
    const onRift = feet?.typeId === "ds:rift_portal_block" || head?.typeId === "ds:rift_portal_block";
    const onTear = feet?.typeId === "ds:frayed_tear" || head?.typeId === "ds:frayed_tear";
    if (!onRift && !onTear) { portalDwell.delete(player.id); continue; }

    const dwell = (portalDwell.get(player.id) ?? 0) + 1;
    portalDwell.set(player.id, dwell);
    player.dimension.spawnParticle("ds:rift", player.location);
    if (dwell < 3) continue;
    portalDwell.delete(player.id);
    portalCooldown.set(player.id, now + 100);

    // ---- FRAYED TEAR: the multiverse ring (quarantine → THE FRAY → ECHO FIELDS → home) ----
    if (onTear && player.dimension.id === "minecraft:the_end") {
      const pIdx = POCKETS.findIndex((pk) =>
        Math.abs(player.location.x - pk.at.x) < 64 && Math.abs(player.location.z - pk.at.z) < 64);
      const nextPocket = POCKETS[(Math.max(pIdx, 0) + 1) % POCKETS.length];
      ensurePocket(endDim(), nextPocket);
      player.teleport({ x: nextPocket.at.x + 0.5, y: nextPocket.at.y + 1.0, z: nextPocket.at.z + 3.5 }, { dimension: endDim() });
      player.playSound("ds.rift.open", { volume: 1.4, pitch: 1.45 });
      player.sendMessage(`§d§oThe tear takes you... §r${nextPocket.name}`);
      continue;
    }

    if (player.dimension.id === "minecraft:the_end") {
      // back to the breach
      const raw = owDim().getDynamicProperty("ds_mainframe");
      if (typeof raw === "string") {
        const [px, py, pz] = JSON.parse(raw);
        player.teleport({ x: px + 0.5, y: py + 1, z: pz + 0.5 }, { dimension: owDim() });
      } else {
        const sp = world.getDefaultSpawnLocation() ?? { x: 0, y: 64, z: 0 };
        player.teleport({ x: sp.x + 0.5, y: sp.y, z: sp.z + 0.5 }, { dimension: owDim() });
      }
      player.playSound("ds.rift.open", { volume: 1.5, pitch: 1.2 });
    } else {
      // into the quarantine
      ensureDecayedPlatform(endDim());
      ensureEndertown(endDim());
      player.teleport({
        x: END_PLATFORM.x + 0.5, y: END_PLATFORM.y + 1.0, z: END_PLATFORM.z + 3.5,
      }, { dimension: endDim() });
      safeCmd(player, `fog @s push "ds:fog_decayed_reality" mist`);
      player.playSound("ds.rift.open", { volume: 1.5, pitch: 1.2 });
    }
  }
}

// ================================================== the Decayed Realm platform
function ensureDecayedPlatform(dim) {
  if (dim.getDynamicProperty("ds_platform_built") === true) return;
  dim.setDynamicProperty("ds_platform_built", true);

  const { x: ox, y: oy, z: oz } = END_PLATFORM;
  for (let dx = -5; dx <= 5; dx++) {
    for (let dz = -5; dz <= 5; dz++) {
      dim.getBlock({ x: ox + dx, y: oy - 1, z: oz + dz })?.setTypeId("ds:decayed_stone");
    }
  }
  for (const c of [-4, 4]) {
    for (let h = 0; h < 3; h++) {
      dim.getBlock({ x: ox + c, y: oy + h, z: oz + c })?.setTypeId("ds:rot_log");
      dim.getBlock({ x: ox - c, y: oy + h, z: oz - c })?.setTypeId("ds:rot_log");
    }
  }
  // the quiet terminal — the breach relay home
  dim.getBlock({ x: ox + 3, y: oy, z: oz + 3 })?.setTypeId("ds:terminal");
  // the return rift, north edge
  for (let dx = -1; dx <= 1; dx++) {
    dim.getBlock({ x: ox + dx, y: oy, z: oz - 4 })?.setTypeId("ds:rift_portal_block");
  }
  // Tazo was already there. Tazo has always been there.
  const tazo = dim.spawnEntity("ds:tazo", { x: ox - 2, y: oy + 1, z: oz });
  tazo.nameTag = "§bTazo";

  // ...and the first frayed tear out to the wider Multiverse, east of the pad
  dim.getBlock({ x: ox + 4, y: oy, z: oz - 4 })?.setTypeId("ds:frayed_tear");
}

// ================================================== the Preacher & the townsfolk
function preacherTick() {
  const dim = endDim();
  for (const town of dim.getEntities({ type: "ds:preacher" })) {
    const near = dim.getPlayers({ location: town.location, maxDistance: 16 });
    if (near.length === 0) continue;
    for (const p of near) p.sendMessage(PREACHER_LINES[Math.floor(Math.random() * PREACHER_LINES.length)]);
    for (const p of near) {
      if (dist(p.location, town.location) < 10) {
        try { p.addEffect("minecraft:regeneration", 120, { amplifier: 0 }); } catch { /* blessed enough */ }
      }
    }
  }
  for (const folk of dim.getEntities({ type: "ds:townsfolk" })) {
    if (Math.random() > 0.5) continue;
    const near = dim.getPlayers({ location: folk.location, maxDistance: 8 });
    if (near.length === 0) continue;
    for (const p of near) p.sendMessage(TOWNSFOLK_LINES[Math.floor(Math.random() * TOWNSFOLK_LINES.length)]);
  }
}

// ================================================== Endertown & the ruined realm (v1.1)
// Bedrock add-ons cannot register worldgen structures, so the realm's builds are
// stamped block-by-block from generated tables (tools/generate_structures.py).
// On Java these same builds generate as real structures from NBT templates —
// one source geometry, both editions.

const ENDERTOWN_SET = [
  { build: "endertown",      at: { x: 40,  y: 74, z: -8 }  },  // the banner-town
  { build: "watcher_shrine", at: { x: 20,  y: 74, z: 34 }  },  // vigil for Him
  { build: "rift_obelisk",   at: { x: 96,  y: 74, z: 44 }  },  // the scars pinned shut
  { build: "mainframe_ruin", at: { x: 112, y: 74, z: -24 } },  // where the breach began
  { build: "watcher_camp", at: { x: -140, y: 74, z: 60 } },    // it watches the quarantine
  { build: "rot_cathedral", at: { x: -170, y: 74, z: -60 } },  // where they kept praying
];

const TOWN_FIRST_LINES = [
  "§b<Tazo>§r Endertown. It still stands.",
  "§b<Tazo>§r We kept the banners lit so the ones who came before could find their way home.",
  "§8[§dMAINFRAME§8] §7> civic archive: §oENDERTOWN§r §7 // evacuation status: §5INCOMPLETE",
];

const TOWN_LOOT = {
  endertown_house: [
    ["ds:storm_dust", 1, 3, 0.85], ["ds:decayed_flesh", 1, 2, 0.60],
    ["ds:tendril", 1, 2, 0.50], ["ds:decayed_bone", 1, 2, 0.45],
    ["minecraft:bread", 1, 3, 0.65], ["minecraft:ender_pearl", 1, 2, 0.30],
    ["ds:memory_fragment", 1, 1, 0.22], ["ds:storm_flesh", 1, 1, 0.25],
    ["ds:watcher_eye", 1, 1, 0.08], ["ds:audio_log_1", 1, 1, 0.12],
  ],
  endertown_plaza: [
    ["ds:storm_dust", 2, 4, 0.90], ["ds:memory_fragment", 1, 2, 0.55],
    ["minecraft:ender_pearl", 1, 3, 0.55], ["minecraft:amethyst_shard", 1, 3, 0.50],
    ["ds:watcher_eye", 1, 1, 0.30], ["minecraft:echo_shard", 1, 2, 0.25],
    ["ds:music_disc_ships", 1, 1, 0.15], ["ds:commanded_star", 1, 1, 0.06],
    ["ds:schedule_1", 1, 1, 0.65], ["ds:audio_log_3", 1, 1, 0.10],
  ],
  watcher_shrine: [
    ["ds:watcher_eye", 1, 1, 0.75], ["ds:memory_fragment", 1, 2, 0.60],
    ["minecraft:ender_pearl", 1, 3, 0.65], ["ds:storm_dust", 1, 3, 0.60],
    ["minecraft:amethyst_shard", 1, 2, 0.45], ["ds:schedule_3", 1, 1, 0.55],
  ],
  mainframe_ruin: [
    ["ds:corrupted_blueprints", 1, 1, 0.35], ["ds:storm_dust", 1, 3, 0.80],
    ["ds:decayed_flesh", 1, 2, 0.55], ["ds:decayed_bone", 1, 2, 0.55],
    ["ds:memory_fragment", 1, 1, 0.30], ["minecraft:redstone", 2, 5, 0.60],
    ["minecraft:iron_ingot", 1, 3, 0.50], ["ds:schedule_2", 1, 1, 0.50],
    ["ds:audio_log_2", 1, 1, 0.14],
  ],
  watcher_camp: [
    ["ds:watcher_eye", 1, 1, 0.75], ["ds:storm_dust", 1, 3, 0.60],
    ["ds:memory_fragment", 1, 2, 0.50], ["minecraft:ender_pearl", 1, 2, 0.60],
    ["minecraft:echo_shard", 1, 2, 0.35], ["ds:schedule_5", 1, 1, 0.55],
  ],
  rot_cathedral: [
    ["ds:storm_dust", 2, 4, 0.80], ["ds:memory_fragment", 1, 3, 0.60],
    ["ds:watcher_eye", 1, 1, 0.30], ["minecraft:amethyst_shard", 1, 3, 0.45],
    ["minecraft:rotten_flesh", 2, 5, 0.70], ["ds:commanded_star", 1, 1, 0.06],
    ["ds:schedule_6", 1, 1, 0.60],
  ],
  rot_cathedral_reliquary: [
    ["ds:memory_fragment", 1, 3, 0.75], ["ds:music_disc_ships", 1, 1, 0.20],
    ["ds:audio_log_1", 1, 1, 0.25], ["ds:seventh_trumpet", 1, 1, 0.12],
  ],
};

function chestLootFill(block, lootId) {
  if (!block || block.typeId !== "minecraft:chest") return;
  try {
    const inv = block.getComponent("minecraft:inventory")?.container;
    if (!inv) return;
    let slot = 0;
    for (const [id, min, max, chance] of TOWN_LOOT[lootId] ?? []) {
      if (slot >= inv.size) break;
      if (Math.random() > chance) continue;
      inv.setItem(slot++, new ItemStack(id, min + Math.floor(Math.random() * (max - min + 1))));
    }
  } catch { /* an empty chest still remembers */ }
}

function placeBuildJob(dim, buildName, origin, withLoot) {
  const spec = BUILDS[buildName];
  if (!spec) return;
  system.runJob((function* () {
    let placed = 0;
    for (const [bx, by, bz, idx] of spec.blocks) {
      try {
        const block = dim.getBlock({ x: origin.x + bx, y: origin.y + by, z: origin.z + bz });
        if (block) block.setTypeId(spec.ids[idx]);
      } catch { /* out of the world or unloaded — skip */ }
      if (++placed % 400 === 0) yield;   // spread the work across ticks
    }
    if (withLoot) {
      for (const [bx, by, bz, lootId] of spec.chests) {
        chestLootFill(dim.getBlock({ x: origin.x + bx, y: origin.y + by, z: origin.z + bz }), lootId);
      }
      for (const [bx, by, bz, typeId] of spec.entities ?? []) {
        // the town lives: spawn its people, but never twice in the same spot
        const spot = { x: origin.x + bx + 0.5, y: origin.y + by + 0.2, z: origin.z + bz + 0.5 };
        try {
          const existing = dim.getEntities({ type: typeId, location: spot, maxDistance: 12 });
          if (existing.length === 0) dim.spawnEntity(typeId, spot);
        } catch { /* the town is quiet anyway */ }
      }
    }
  })());
}

function ensureEndertown(dim) {
  if (dim.getDynamicProperty("ds_town_built") === true) return;
  dim.setDynamicProperty("ds_town_built", true);
  for (const entry of ENDERTOWN_SET) {
    const spec = BUILDS[entry.build];
    if (!spec) continue;
    placeBuildJob(dim, entry.build, { x: entry.at.x, y: entry.at.y - spec.groundY, z: entry.at.z }, true);
  }
  TOWN_FIRST_LINES.forEach((line, idx) => {
    system.runTimeout(() => {
      for (const p of dim.getPlayers()) p.sendMessage(line);
    }, 80 + idx * 70);
  });
  try { dim.playSound("ds.rift.open", { x: 40.5, y: 75, z: -7.5 }, { volume: 2.0, pitch: 0.8 }); } catch { /* quiet sky */ }
}

// ================================================== multiverse pockets
// Each frayed-tear ring stop is a small platform stacked in the realm's far corners.
function ensurePocket(dim, pocket) {
  const key = `ds_pocket_${pocket.key}`;
  if (dim.getDynamicProperty(key) === true) return;
  dim.setDynamicProperty(key, true);

  const { x: ox, y: oy, z: oz } = pocket.at;
  const accent = pocket.key === "fray" ? "ds:decayed_soil"
    : pocket.key === "echo" ? "minecraft:end_stone" : "ds:decayed_stone";
  for (let dx = -4; dx <= 4; dx++) {
    for (let dz = -4; dz <= 4; dz++) {
      dim.getBlock({ x: ox + dx, y: oy - 1, z: oz + dz })?.setTypeId(accent);
    }
  }
  for (const c of [-3, 3]) {
    for (let h = 0; h < 3; h++) {
      dim.getBlock({ x: ox + c, y: oy + h, z: oz + c })?.setTypeId("ds:rot_log");
      dim.getBlock({ x: ox - c, y: oy + h, z: oz - c })?.setTypeId("ds:rot_log");
    }
  }
  // the tear onward, north edge — the ring goes ever on
  dim.getBlock({ x: ox, y: oy, z: oz - 3 })?.setTypeId("ds:frayed_tear");
  // the trapped: "Anna, Travis, and Tonya are trapped in Decayed Reality."
  if (pocket.key === "fray") {
    try {
      const travis = dim.spawnEntity("ds:travis", { x: ox + 0.5, y: oy, z: oz + 2.5 });
      travis.nameTag = "§9Travis";
    } catch { /* the fray keeps him for now */ }
  } else if (pocket.key === "echo") {
    try {
      const tonya = dim.spawnEntity("ds:tonya", { x: ox + 0.5, y: oy, z: oz + 2.5 });
      tonya.nameTag = "§b§oTonya?";
    } catch { /* the echo keeps her for now */ }
  }
}

// ================================================== the Amulet of Decay
function amuletTick() {
  for (const player of world.getAllPlayers()) {
    if (!hasItem(player, "ds:amulet_of_decay")) continue;
    if (!player.getEffect("minecraft:wither")) continue;
    player.removeEffect("minecraft:wither");
    const h = player.getComponent("health");
    if (h) h.setCurrentValue(Math.min(h.effectiveMax ?? h.defaultValue, h.currentValue + 1));
    player.dimension.spawnParticle("ds:glitch", player.getHeadLocation());
  }
}

// ================================================== item rituals & consumables

// itemUseOn: blueprints on corrupted command block, rift key on terminal
world.afterEvents.itemUseOn.subscribe((event) => {
  const player = event.source;
  const block = event.block;
  const item = event.itemStack;
  if (!player || !block || !item) return;
  const dim = block.dimension;

  // ---- ENDERTOWN CORE: the town rises where it is remembered (Decayed Realm only) ----
  if (item.typeId === "ds:endertown_core") {
    if (dim.id !== "minecraft:the_end") {
      player.sendMessage("§8[§dMAINFRAME§8] §7The town can only rise inside the quarantine.");
      return;
    }
    const spec = BUILDS.endertown;
    if (!spec) return;
    const feet = block.location;
    const origin = { x: feet.x - 28, y: feet.y + 1 - spec.groundY, z: feet.z - 24 };
    consumeHeldItem(player);
    placeBuildJob(dim, "endertown", origin, true);
    dim.setDynamicProperty("ds_town_built", true);
    player.sendMessage("§d§oThe banners rise again. Endertown remembers.");
    player.playSound("ds.rift.open", { volume: 1.5, pitch: 0.8 });
    return;
  }

  // ---- THE SEALED VAULT: the seven schedules assemble (M.A.S.S.G.O.O.S) ----
  if (block.typeId === "ds:sealed_vault") {
    if (block.permutation.getState("ds:open") === true) {
      player.sendMessage("§8[§dVAULT§8] §7Archive open. The payload remains classified until §02027§7.");
      return;
    }
    const inv = player.getComponent("inventory")?.container;
    if (!inv) return;
    let owned = 0;
    for (let n = 1; n <= 7; n++) {
      for (let s = 0; s < inv.size; s++) {
        if (inv.getItem(s)?.typeId === `ds:schedule_${n}`) { owned++; break; }
      }
    }
    if (owned < 7) {
      player.sendMessage(`§8[§dVAULT§8] §7SEALED. §8schedules located: §5${owned}§8/§57`);
      player.sendMessage("§8[§dVAULT§8] §8there are seven. the town, the ruin, the shrine, the apparition, the companion, the storm, the severed.");
      player.playSound("ds.terminal.boot", { volume: 0.6, pitch: 0.4 });
      return;
    }
    // the seven assemble — consume one of each
    for (let n = 1; n <= 7; n++) {
      for (let s = 0; s < inv.size; s++) {
        const it = inv.getItem(s);
        if (it?.typeId === `ds:schedule_${n}`) {
          if (it.amount > 1) { it.amount -= 1; inv.setItem(s, it); } else { inv.setItem(s, undefined); }
          break;
        }
      }
    }
    block.setPermutation(block.permutation.withState("ds:open", true));
    dim.playSound("ds.terminal.boot", block.location, { volume: 1.6, pitch: 0.8 });
    dim.playSound("ds.rift.open", block.location, { volume: 1.2, pitch: 0.7 });
    VAULT_PAYLOAD.forEach((line, idx) => {
      system.runTimeout(() => {
        for (const p of dim.getPlayers({ location: block.location, maxDistance: 24 })) p.sendMessage(line);
      }, 20 + idx * 45);
    });
    for (const [id, count] of VAULT_REWARD) {
      try {
        const stack = new ItemStack(id, count);
        if (!inv.addItem(stack)) dim.spawnItem(stack, block.location);
      } catch { /* the floor accepts its offering */ }
    }
    player.onScreenDisplay.setTitle("§5§lM.A.S.S.G.O.O.S", {
      subtitle: "§8the archive unseals",
      fadeInDuration: 10, stayDuration: 70, fadeOutDuration: 20,
    });
    return;
  }

  // ---- MASSG IS WAKING UP ----
  if (block.typeId === "ds:corrupted_command_block" && item.typeId === "ds:corrupted_blueprints") {
    consumeHeldItem(player);
    block.setTypeId("ds:decay_block");
    const spawnPos = { x: block.location.x + 0.5, y: block.location.y + 14, z: block.location.z + 0.5 };
    const storm = dim.spawnEntity("ds:massg", spawnPos);
    storm.setDynamicProperty("ds_phase", "sleeping");
    storm.setDynamicProperty("ds_growth", 0);
    storm.triggerEvent("ds:sleeping");
    storm.nameTag = PHASE_NAME.sleeping;
    dim.playSound("ds.massg.awakening", spawnPos, { volume: 4.0, pitch: 0.8 });
    for (let i = 0; i < 3; i++) {
      try {
        dim.spawnEntity("minecraft:lightning_bolt", {
          x: block.location.x + (Math.random() - 0.5) * 16,
          y: block.location.y,
          z: block.location.z + (Math.random() - 0.5) * 16,
        });
      } catch { /* no permission for theatrical thunder */ }
    }
    broadcastNear(storm, 320, WAKE_BROADCAST);
    for (const p of playersNear(storm, 160)) safeCmd(p, `fog @s push "ds:fog_massg_storm" mist`);
    return;
  }

  // ---- THE MAINFRAME HAS BEEN BREACHED ----
  if (block.typeId === "ds:terminal" && item.typeId === "ds:rift_key") {
    const active = block.permutation.getState("ds:active") === true;
    if (active) {
      player.sendMessage("§8[§dMAINFRAME§8] §7The portal is open. Something on the other side is listening.");
      return;
    }
    if (!foundationComplete(block)) {
      player.sendMessage("§8[§dMAINFRAME§8] §7FOUNDATION INCOMPLETE. §oThe mainframe must stand upon a 5×5 frame.§r");
      return;
    }

    consumeHeldItem(player);
    block.setPermutation(block.permutation.withState("ds:active", true));
    dim.setDynamicProperty("ds_mainframe", JSON.stringify([block.location.x, block.location.y, block.location.z]));

    dim.playSound("ds.terminal.boot", block.location, { volume: 2.0 });
    dim.playSound("ds.rift.open", block.location, { volume: 2.0, pitch: 0.9 });

    // open the portal: 3×3 pad north of the terminal
    for (let dx = -1; dx <= 1; dx++) {
      for (let dz = 2; dz <= 4; dz++) {
        const pp = dim.getBlock({ x: block.location.x + dx, y: block.location.y, z: block.location.z - dz });
        if (pp?.isAir) pp.setTypeId("ds:rift_portal_block");
      }
    }

    // the classified transmission
    MAINFRAME_TRANSMISSION.forEach((line, idx) => {
      system.runTimeout(() => {
        for (const p of dim.getPlayers({ location: block.location, maxDistance: 48 })) {
          p.sendMessage(line);
          if (idx < MAINFRAME_TRANSMISSION.length - 1) {
            p.playSound("ds.terminal.transmission", { volume: 1.2 });
          }
        }
      }, 30 + idx * 50);
    });
    return;
  }

  // ---- eye on an active terminal: replay the tail of the transmission ----
  if (block.typeId === "ds:terminal" && item.typeId === "ds:watcher_eye") {
    for (const line of MAINFRAME_TRANSMISSION.slice(-3)) player.sendMessage(line);
    player.playSound("ds.terminal.transmission", { volume: 1.0 });
    return;
  }
});

function foundationComplete(block) {
  const dim = block.dimension;
  const { x, y, z } = block.location;
  for (let dx = -2; dx <= 2; dx++) {
    for (let dz = -2; dz <= 2; dz++) {
      if (dx === 0 && dz === 0) continue;
      const f = dim.getBlock({ x: x + dx, y: y - 1, z: z + dz });
      if (f?.typeId !== "ds:mainframe_frame") return false;
    }
  }
  return true;
}

// itemUse (not on a block): the Watcher Eye reveals; E.P.A. audio logs play back
world.afterEvents.itemUse.subscribe((event) => {
  const player = event.source;
  const item = event.itemStack;
  if (!player || !item) return;

  // ---- E.P.A. FIELD TAPES ----
  if (item.typeId.startsWith("ds:audio_log_")) {
    const idx = parseInt(item.typeId.split("_").pop(), 10);
    for (const line of AUDIO_LOGS[idx] ?? []) player.sendMessage(line);
    player.playSound("ds.terminal.transmission", { volume: 0.5, pitch: 0.9 + idx * 0.1 });
    return;
  }

  if (item.typeId !== "ds:watcher_eye") return;

  const watchers = player.dimension.getEntities({ families: ["ds_watcher"], location: player.location, maxDistance: 96 });
  for (const w of watchers) {
    try { w.addEffect("minecraft:glowing", 160, { amplifier: 0 }); } catch { /* no glow available */ }
  }
  player.playSound("ds.watcher.whisper", { volume: 1.0, pitch: 0.6 });
});

// The Storm Killer: the only thing that can rend the husk's anchored command block.
// Three clean strikes through the stomach-hole, then the white fire takes the storm.
world.afterEvents.playerInteractWithEntity.subscribe((event) => {
  const storm = event.target;
  const player = event.player;
  const item = event.itemStack;
  if (!storm || !player || storm.typeId !== "ds:massg") return;
  const phase = prop(storm, "ds_phase", "sleeping");

  // ---- THE SEVENTH TRUMPET: it answers. one phase, right now, on purpose ----
  if (item?.typeId === "ds:seventh_trumpet") {
    if (phase === "husk" || phase === "play_dead") {
      player.sendMessage("§8You sound the trumpet. The husk does not answer. §oOnly the knife does.§r");
      return;
    }
    const inv = player.getComponent("inventory")?.container;
    const idx = PHASES.indexOf(phase);
    if (idx < 0 || idx >= PHASES.length - 1) return;
    if (inv) {
      const s = inv.find(item) ;
      if (s >= 0) inv.setItem(s, undefined);
    }
    const next = PHASES[idx + 1];
    setPhase(storm, next);
    storm.nameTag = PHASE_NAME[next];
    storm.dimension.playSound("ds.massg.awakening", storm.location, { volume: 4.0, pitch: 1.6 });
    for (const p of playersNear(storm, 320)) p.sendMessage("§5§lTHE SEVENTH TRUMPET SOUNDS. §r§d§oit answers.§r");
    return;
  }

  // ---- the rend finale: storm killer into the husk's open hole ----
  if (item?.typeId !== "ds:storm_killer") return;
  if (phase !== "husk") return;
  if (prop(storm, "ds_rend_ticks", 0) > 0) return;
  const holeOpen = (world.getTime() % 260) < 120;
  if (!holeOpen) {
    player.sendMessage("§8The stomach-hole is sealed shut. It opens in pulses — watch for the glow.");
    return;
  }
  const hits = prop(storm, "ds_core_hits", 0) + 1;
  setProp(storm, "ds_core_hits", hits);
  const dim = storm.dimension;
  dim.playSound("ds.massg.devolve_sting", storm.location, { volume: 3.0, pitch: 1.3 });
  for (let i = 0; i < 12; i++) dim.spawnParticle("ds:glitch", storm.location);
  dim.spawnParticle("ds:sky_flash", storm.location);
  if (hits < 3) {
    player.sendMessage(`§f§l${hits} §r§7— the Storm Killer bites the command block. §ostrike ${3 - hits} more while the hole is open§r`);
    return;
  }
  // REND: ripping rings of white and violet fire until it has nothing left
  setProp(storm, "ds_rend_ticks", 110);
  broadcastAll("§f§lTHE COMMAND BLOCK IS BREACHED. §r§d§oWhite fire rips out of the bowels.§r");
  dim.playSound("ds.massg.rebirth", storm.location, { volume: 4.0, pitch: 1.5 });
  rendFinale(storm);
});

function rendFinale(storm) {
  const dim = storm.dimension;
  const base = { ...storm.location };
  for (let t = 0; t <= 100; t += 10) {
    system.runTimeout(() => {
      try {
        const r = 1.0 + t * 0.22;
        for (let i = 0; i < 22; i++) {
          const a = (i / 22) * Math.PI * 2 + t * 0.2;
          dim.spawnParticle(t % 20 === 0 ? "ds:sky_flash" : "ds:glitch", {
            x: base.x + Math.cos(a) * r, y: base.y + 1, z: base.z + Math.sin(a) * r,
          });
        }
        dim.playSound("ds.massg.pull_loop", base, { volume: 1.8, pitch: 0.5 + t * 0.008 });
      } catch { /* storm left the stage */ }
    }, t);
  }
  system.runTimeout(() => {
    try {
      dim.spawnParticle("ds:sky_flash", base);
      dim.playSound("ds.massg.true_death", base, { volume: 4.0, pitch: 0.6 });
      for (const [id, n] of [["ds:storm_heart", 1], ["ds:commanded_star", 1], ["ds:withered_nether_star", 1],
                             ["ds:storm_dust", 12], ["ds:tendril", 3], ["ds:storm_flesh", 5]]) {
        try { dim.spawnItem(new ItemStack(id, n), base); } catch { /* dealt with by gravity */ }
      }
      broadcastAll("§f§lTHE STORM TEARS APART. §r§7White fire pours out of the bowels — it is over.");
      broadcastAll("§a§lTHE STORM IS ENDED. §r§7...somewhere, a countdown keeps running.");
      storm.remove();
    } catch { /* already gone, still over */ }
  }, 110);
}

// Schedule V — The Companion: Tazo kept one safe since the banners fell.
// Interact with Tazo while holding a Memory Fragment (the bond ritual) and he gives it up once.
world.afterEvents.playerInteractWithEntity.subscribe((event) => {
  const tazo = event.target;
  const player = event.player;
  const item = event.itemStack;
  if (!tazo || !player || tazo.typeId !== "ds:tazo") return;
  if (item?.typeId !== "ds:memory_fragment") return;
  if (tazo.getDynamicProperty("ds_gifted") === true) return;
  tazo.setDynamicProperty("ds_gifted", true);
  try { tazo.dimension.spawnItem(new ItemStack("ds:schedule_5", 1), tazo.location); } catch { /* he keeps it hidden */ }
  player.sendMessage("§b<Tazo>§r Hold onto this. §oA schedule. I kept it safe since the banners fell.§r");
  player.playSound("ds.terminal.transmission", { volume: 0.8, pitch: 1.2 });
});

// storm flesh: eating it marks you
world.afterEvents.itemCompleteUse.subscribe((event) => {
  const player = event.source;
  const item = event.itemStack;
  if (!player || !item) return;
  if (item.typeId !== "ds:storm_flesh" && item.typeId !== "ds:decayed_flesh") return;
  player.addEffect("minecraft:wither", 100, { amplifier: 0 });
  player.sendMessage("§5§oIt tastes like static. §r§8§oYou have been changed.");
});

/* ============================================================================
   v1.4 — MULTIVERSE SLATE
   tractor beams · earthquakes · the belly chamber · the Void Maw · the trapped
   title cards · the config console known as /scriptevent ds:cfg
   ========================================================================= */

// ------------------------------------------------------------------ config
// Server props persist across server restarts; toggle them with:
//   /scriptevent ds:cfg earthquakes 0        (off)
//   /scriptevent ds:cfg earthquakes 1        (on / default)
const DS_CFG_KEYS = [
  "earthquakes", "tractor_beams", "title_cards", "belly", "void_maw", "infection",
];
function cfgOn(key) {
  const v = world.getDynamicProperty(`ds_cfg_${key}`);
  return v === undefined ? true : v === true;
}
try {
  system.afterEvents.scriptEventReceived.subscribe((event) => {
    if (event.id !== "ds:cfg") return;
    const src = event.sourceEntity;
    const say = (m) => { if (src && src.isValid) { try { src.sendMessage(m); } catch { /* console */ } } };
    const parts = String(event.message ?? "").trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0 || parts[0] === "list") {
      say("§6§lDEVOURING STORMS — console§r  §8toggle with /scriptevent ds:cfg <key> <1|0>");
      for (const k of DS_CFG_KEYS) say(`  §7${k}: §f${cfgOn(k) ? "§aON" : "§cOFF"}`);
      return;
    }
    const [key, val] = parts;
    if (!DS_CFG_KEYS.includes(key)) { say(`§cunknown key: ${key} §7(see: list)`); return; }
    const on = !(val === "0" || String(val).toLowerCase() === "off");
    world.setDynamicProperty(`ds_cfg_${key}`, on);
    say(`§7${key} → ${on ? "§aON" : "§cOFF"}`);
  });
} catch { /* scriptEventReceived unavailable — console stays shut */ }

// ------------------------------------------------------------------ title cards
// The channel's sign-off: the episode cuts to black and the phase name trembles.
const PHASE_TITLECARDS = {
  signal:   ["P H A S E  1", "THE SIGNAL", "something in the sea is counting"],
  hunger:   ["P H A S E  2", "THE HUNGER", "it is eating more than blocks now"],
  devourer: ["P H A S E  3", "THE DEVOURER", "the mouths open all at once"],
  sunderer: ["P H A S E  4", "THE SUNDERER", "the ground has started shuddering"],
  bowels:   ["P H A S E  5 ½", "THE BOWELS", "it is open. it is stomach. it will remember you."],
  genesis:  ["P H A S E  6", "GENESIS", "THE FORMIDIBOMB ONLY MADE IT WORSE"],
  husk:     ["THE STORM FALLS", "FINISH IT", "the Watcher left you the Storm Killer"],
};
function titleCardForPhase(storm, phase) {
  const card = PHASE_TITLECARDS[phase];
  if (!card) return;
  let near = [];
  try { near = storm.dimension.getPlayers({ location: storm.location, maxDistance: 400 }); } catch { /* nobody */ }
  for (const player of near) {
    try {
      player.onScreenDisplay.setTitle(`§f§l${card[0]} — ${card[1]}`, {
        subtitle: `§d§o${card[2]}  §8§o// REWRITTEN`,
        fadeInDuration: 15, stayDuration: 80, fadeOutDuration: 20,
      });
      system.runTimeout(() => { try {
        player.onScreenDisplay.updateSubtitle(`§0${card[0]}`); /* ghosted echo of itself */
      } catch { /* fine */ } }, 110);
    } catch { /* the hud said no */ }
  }
}

// ------------------------------------------------------------------ the trapped
const TRAVIS_LINES = [
  "§9<Travis>§r I mind the tear. Anna said someone had to keep the roads open.",
  "§9<Travis>§r If you hear a bell under the wind — count the rings. It only ever rings six times.",
  "§9<Travis>§r EAOIN answered me once. It said: the next generation is listening.",
  "§9<Travis>§r Tonya followed Anna into somewhere I can't remember clearly. Tell her I still wait.",
  "§9<Travis>§§o(the ketamine hum of the frayed tear drowns whatever he says next)§r",
];
const TONYA_LINES = [
  "§b§o<Tonya?>§r the fields remember all four of us. even the one we made up.",
  "§b§o<Tonya?>§r am i loud? the wind here eats half of every sentence.",
  "§b§o<Tonya?>§r travis counts the bell. i stop at five. someone has to be wrong, that's how you know it's true.",
  "§b§o<Tonya?>§r anna isn't real. this world is an illusion. but the echo is warm, so i stay.",
];
function multiverseNpcTick() {
  const dim = endDim();
  for (const travis of dim.getEntities({ type: "ds:travis" })) {
    if (!travis.isValid) continue;
    const near = dim.getPlayers({ location: travis.location, maxDistance: 10 });
    if (near.length === 0) continue;
    for (const p of near) p.sendMessage(TRAVIS_LINES[Math.floor(Math.random() * TRAVIS_LINES.length)]);
  }
  for (const tonya of dim.getEntities({ type: "ds:tonya" })) {
    if (!tonya.isValid) continue;
    const near = dim.getPlayers({ location: tonya.location, maxDistance: 10 });
    if (near.length === 0) continue;
    for (const p of near) p.sendMessage(TONYA_LINES[Math.floor(Math.random() * TONYA_LINES.length)]);
  }
}
// Travis kept a spare plague log; he trades it for proof the others were real.
try {
  world.afterEvents.playerInteractWithEntity.subscribe((event) => {
    try {
      const target = event.target;
      if (!target || !target.isValid || target.typeId !== "ds:travis") return;
      if (target.getDynamicProperty("ds_tape_gifted") === true) return;
      const player = event.player;
      const stack = player.getComponent("equippable")?.getEquipment(EquipmentSlot.Mainhand);
      if (stack?.typeId !== "ds:memory_fragment") return;
      target.setDynamicProperty("ds_tape_gifted", true);
      target.dimension.spawnItem(new ItemStack("ds:audio_log_2", 1), target.location);
      player.sendMessage("§9<Travis>§r ...she was real. Here — the plague log. I kept a spare for company. Listen with the volume down.");
      target.dimension.playSound("random.levelup", target.location, { volume: 0.8, pitch: 1.0 });
    } catch { /* the fray keeps its secrets */ }
  });
} catch { /* interact event unavailable */ }

// ------------------------------------------------------------------ tractor beams
// Three heads, three beams; from the Devourer on, they comb the ground for prey.
function tractorBeamTick() {
  if (!cfgOn("tractor_beams")) return;
  for (const dim of dims()) {
    for (const storm of dim.getEntities({ families: ["ds_massg"] })) {
      if (!storm.isValid || isPlayingDead(storm)) continue;
      const idx = PHASES.indexOf(phaseOf(storm));
      if (idx < 3) continue; // devourer and up
      const range = 20 + idx * 3;
      const victims = dim.getEntities({ location: storm.location, maxDistance: range })
        .filter(e => e.isValid && e.typeId !== "ds:massg"
          && (e.typeId === "minecraft:player" || e.hasComponent?.("minecraft:health"))
          && e.typeId !== "ds:massg");
      for (const victim of victims) {
        if (dist(victim.location, storm.location) < 4) continue;
        try {
          const dx = storm.location.x - victim.location.x;
          const dy = (storm.location.y + 4) - victim.location.y;
          const dz = storm.location.z - victim.location.z;
          const len = Math.max(0.001, Math.hypot(dx, dy, dz));
          victim.applyImpulse({ x: dx / len * 0.11, y: 0.09 + dy / len * 0.05, z: dz / len * 0.11 });
          if (victim.typeId === "minecraft:player" && world.getTime() % 40 < 2) {
            victim.sendMessage("§5§oThe beam has you. The heads do not blink.§r");
          }
          const mid = {
            x: (storm.location.x + victim.location.x) / 2,
            y: (storm.location.y + victim.location.y) / 2 + 2,
            z: (storm.location.z + victim.location.z) / 2,
          };
          dim.spawnParticle("ds:glitch", mid);
        } catch { /* the beam released them */ }
      }
    }
  }
}

// ------------------------------------------------------------------ earthquakes
// Under the Sunderer and up: the world buckles. Camera shake, dust, and a bruise.
function quakeTick() {
  if (!cfgOn("earthquakes")) return;
  for (const dim of dims()) {
    for (const storm of dim.getEntities({ families: ["ds_massg"] })) {
      if (!storm.isValid || isPlayingDead(storm)) continue;
      const idx = PHASES.indexOf(phaseOf(storm));
      if (idx < 4) continue; // sunderer and up
      const next = prop(storm, "ds_next_quake", 0);
      if (world.getTime() < next) continue;
      setProp(storm, "ds_next_quake", world.getTime() + 700 + Math.floor(Math.random() * 900));
      const base = storm.location;
      dim.playSound("ds.massg.roar", base, { volume: 3.0, pitch: 0.32 });
      for (const player of dim.getPlayers({ location: base, maxDistance: 320 })) {
        try {
          player.runCommand("camerashake add @s 0.55 1.4 positional");
          player.applyImpulse({ x: (Math.random() - 0.5) * 0.2, y: 0.22, z: (Math.random() - 0.5) * 0.2 });
        } catch { /* braced */ }
      }
      for (let i = 0; i < 10; i++) {
        const a = (i / 10) * Math.PI * 2;
        try {
          dim.spawnParticle("minecraft:explosion_particle", { x: base.x + Math.cos(a) * 8, y: base.y - 1, z: base.z + Math.sin(a) * 8 });
        } catch { /* dust settles */ }
      }
      broadcastNear(storm, 340, "§8§oThe ground shudders — it is still feeding.");
    }
  }
}

// ------------------------------------------------------------------ the Void Maw
// A black hole that got lost in the multiverse and liked the menu. It pulls; it eats;
// every meal makes it a mouth with more mass.
function voidMawTick() {
  if (!cfgOn("void_maw")) return;
  for (const dim of dims()) {
    for (const maw of dim.getEntities({ type: "ds:void_maw" })) {
      if (!maw.isValid) continue;
      const grow = prop(maw, "ds_maw_mass", 0);
      const pullR = 18 + Math.min(14, grow * 2);
      // photon ring: light choosing a side
      if (world.getTime() % 6 < 3) {
        const a = world.getTime() * 0.21;
        try {
          dim.spawnParticle("minecraft:endrod", {
            x: maw.location.x + Math.cos(a) * 2.2, y: maw.location.y + 1.2, z: maw.location.z + Math.sin(a) * 2.2 });
        } catch { /* dim ring */ }
      }
      for (const victim of dim.getEntities({ location: maw.location, maxDistance: pullR })) {
        if (!victim.isValid || victim === maw || victim.typeId === "ds:void_maw") continue;
        const d = dist(victim.location, maw.location);
        if (d < 0.4) continue;
        const s = 0.05 * (1.0 - d / pullR) + 0.012;
        try {
          victim.applyImpulse({
            x: (maw.location.x - victim.location.x) / d * s * 8,
            y: (maw.location.y + 1 - victim.location.y) / d * s * 4,
            z: (maw.location.z - victim.location.z) / d * s * 8,
          });
        } catch { /* no hands */ }
        if (d < 2.2) {
          try { victim.applyDamage(8, { cause: EntityDamageCause.magic }); } catch { /* hardy */ }
          if (!victim.isValid || victim.getComponent?.("minecraft:health")?.currentValue <= 0) {
            setProp(maw, "ds_maw_mass", grow + 1);
            try { dim.playSound("ds.massg.devour", maw.location, { volume: 2.0, pitch: 0.3 }); } catch { /* fed quietly */ }
          }
        }
      }
    }
  }
}

// ------------------------------------------------------------------ THE BELLY
// Fly into the open bowels and you're inside the storm. Built high in the End-far
// (when the realm exists) or the Nether roof — the stomach finds space the storm owns.
const BELLY_AT = { x: 0, y: 250, z: 1000 };
function bellyLoc() { return BELLY_AT; }
function ensureBelly(dim) {
  if (world.getDynamicProperty("ds_belly_built") === true) return;
  world.setDynamicProperty("ds_belly_built", true);
  const { x: ox, y: oy, z: oz } = bellyLoc();
  // a chamber of stomach-wall stone with decay veins and one beating block
  for (let dx = -5; dx <= 5; dx++) {
    for (let dz = -5; dz <= 5; dz++) {
      dim.getBlock({ x: ox + dx, y: oy - 1, z: oz + dz })?.setTypeId("ds:decayed_stone");
      dim.getBlock({ x: ox + dx, y: oy + 5, z: oz + dz })?.setTypeId("ds:decayed_stone");
    }
  }
  for (let dy = 0; dy <= 4; dy++) {
    for (const [wx, wz] of [[-5, 0], [5, 0], [0, -5], [0, 5], [-4, -4], [4, -4], [-4, 4], [4, 4]]) {
      dim.getBlock({ x: ox + wx, y: oy + dy, z: oz + wz })?.setTypeId("ds:decayed_stone");
    }
  }
  // walls fill — every block between the pillars that isn't the doorway
  for (let dx = -5; dx <= 5; dx++) {
    for (let dz = -5; dz <= 5; dz++) {
      const edge = Math.max(Math.abs(dx), Math.abs(dz));
      if (edge !== 5) continue;
      for (let dy = 0; dy <= 4; dy++) {
        if (dz === -5 && dx >= -1 && dx <= 1 && dy <= 2) continue; // stomach doorway
        const vein = (dx * 13 + dz * 7 + dy * 5) % 6 === 0;
        dim.getBlock({ x: ox + dx, y: oy + dy, z: oz + dz })?.setTypeId(vein ? "ds:decay_block" : "ds:decayed_stone");
      }
    }
  }
  // the heart of the storm: the command block on an obsidian dais
  dim.getBlock({ x: ox, y: oy, z: oz + 2 })?.setTypeId("minecraft:obsidian");
  dim.getBlock({ x: ox, y: oy, z: oz })?.setTypeId("minecraft:obsidian");
  dim.getBlock({ x: ox, y: oy + 1, z: oz })?.setTypeId("ds:corrupted_command_block");
  dim.getBlock({ x: ox, y: oy, z: oz - 6 })?.setTypeId("ds:frayed_tear"); // a tear out, if you earned it
}
function inBelly(p) { return p.getDynamicProperty("ds_in_belly") === true; }
function bellyTick() {
  if (!cfgOn("belly")) return;
  for (const dim of dims()) {
    const realmOpen = dim.id === "minecraft:the_end" || dim.id === "minecraft:overworld";
    if (!realmOpen) continue;
    for (const storm of dim.getEntities({ families: ["ds_massg"] })) {
      if (!storm.isValid || isPlayingDead(storm)) continue;
      const phase = phaseOf(storm);
      if (phase !== "bowels" && phase !== "genesis") continue;
      for (const player of dim.getPlayers({ location: storm.location, maxDistance: 5 })) {
        if (player.isFlying === false && !player.isGliding) continue; // only the ones who fly in
        if (inBelly(player)) continue;
        try {
          ensureBelly(dim);
          const { x, y, z } = bellyLoc();
          setProp(player, "ds_in_belly", true);
          setProp(player, "ds_belly_hits", 0);
          setProp(player, "ds_belly_out_x", Math.floor(storm.location.x));
          setProp(player, "ds_belly_out_z", Math.floor(storm.location.z));
          player.teleport({ x: x + 0.5, y, z: z - 5.5 }, { dimension: dim });
          dim.playSound("ds.massg.devour", storm.location, { volume: 2.0, pitch: 0.5 });
          player.onScreenDisplay.setTitle("§5§lINSIDE THE STORM", {
            subtitle: "§d§ostrike the command block with the Storm Killer. everything else is clawing.",
            fadeInDuration: 10, stayDuration: 60, fadeOutDuration: 15,
          });
          player.sendMessage("§5§oIt is warm. It is rhythmic. It knows exactly what you came to do.");
        } catch { /* the swallow failed; lucky */ }
      }
    }
    // the way back: stand on the belly's tear and it exhales you at the storm
    for (const player of dim.getPlayers({ location: bellyLoc(), maxDistance: 10 })) {
      if (!inBelly(player)) continue;
      try {
        const feet = dim.getBlock({ x: Math.floor(player.location.x), y: Math.floor(player.location.y) - 1, z: Math.floor(player.location.z) });
        if (feet?.typeId === "ds:frayed_tear") {
          dopEject(player, dim, null);
        }
      } catch { /* still digesting */ }
    }
  }
}
function dopEject(player, dim, storm) {
  try {
    const ox = prop(player, "ds_belly_out_x", 0), oz = prop(player, "ds_belly_out_z", 0);
    setProp(player, "ds_in_belly", false);
    player.teleport({ x: ox + 0.5, y: 90, z: oz + 0.5 }, { dimension: dim });
    player.sendMessage("§5§oThe storm exhales you. It remembers the taste.");
    dim.playSound("ds.massg.play_dead", player.location, { volume: 1.2, pitch: 1.4 });
  } catch { /* exhale later */ }
}
// strike the command block with the Storm Killer: three bites and the storm rips open
try {
  world.afterEvents.entityHitBlock.subscribe((event) => {
    try {
      const actor = event.damagingEntity;
      if (!actor || !actor.isValid || actor.typeId !== "minecraft:player") return;
      if (!inBelly(actor)) return;
      const block = event.hitBlock;
      if (!block || block.typeId !== "ds:corrupted_command_block") return;
      const stack = actor.getComponent("equippable")?.getEquipment(EquipmentSlot.Mainhand);
      if (stack?.typeId !== "ds:storm_killer") {
        actor.sendMessage("§8The block refuses every blade but the one the Watcher hoards.");
        return;
      }
      const hits = prop(actor, "ds_belly_hits", 0) + 1;
      setProp(actor, "ds_belly_hits", hits);
      const dim = actor.dimension;
      dim.playSound("ds.massg.devolve_sting", block.location, { volume: 3.0, pitch: 1.3 });
      for (let i = 0; i < 12; i++) dim.spawnParticle("ds:glitch", block.location);
      dim.spawnParticle("ds:sky_flash", block.location);
      if (hits < 3) {
        actor.sendMessage(`§f§l${hits} §r§7— the Storm Killer bites the command block. §ostrike ${3 - hits} more§r`);
        return;
      }
      // the core breaks: its storm falls as a husk in its own dimension
      actor.sendMessage("§f§lTHE COMMAND BLOCK IS BREACHED. §r§d§oWhite fire rips out of the bowels.§r");
      broadcastAll("§f§lTHE COMMAND BLOCK IS BREACHED. §r§d§oWhite fire rips out of the bowels.§r");
      let storm = null;
      for (const cand of dim.getEntities({ families: ["ds_massg"] })) {
        const ph = phaseOf(cand);
        if (cand.isValid && (ph === "bowels" || ph === "genesis")) { storm = cand; break; }
      }
      if (storm) {
        const pos = { x: storm.location.x, y: storm.location.y, z: storm.location.z };
        storm.remove();
        system.runTimeout(() => {
          try {
            const husk = dim.spawnEntity("ds:massg", pos);
            husk.setDynamicProperty("ds_phase", "husk");
            husk.setDynamicProperty("ds_core_hits", 0);
            husk.triggerEvent("ds:husk");
            husk.nameTag = PHASE_NAME.husk;
            const hh = husk.getComponent("health");
            if (hh) hh.setCurrentValue(400);
            dim.playSound("ds.massg.play_dead", pos, { volume: 4.0, pitch: 0.55 });
            broadcastAll(HUSK_FALL_BROADCAST);
            broadcastAll(HUSK_HINT);
            stopAllStormMusic();
          } catch { /* the husk walks without ceremony */ }
        }, 4);
      }
      setProp(actor, "ds_belly_out_x", storm ? prop(actor, "ds_belly_out_x", 0) : 0);
      dopEject(actor, dim, storm);
    } catch { /* the block keeps its own count */ }
  });
} catch { /* entityHitBlock unavailable — the belly's gate stays shut */ }


// =================================================================================
//   v1.5 — THE CREATOR, THE MONSTROSITY'S WORLD-GLITCH, THE FORGER'S RAIN
// =================================================================================

const CREATOR_LINES_V15 = [
  "§8§oYOU ARRIVE LATE.",
  "§8§oI DO NOT WATCH. I REMEMBER.",
  "§8§oTHE HAND IS ALREADY THERE.",
  "§8§oEVERY EXIT IS AN ENTRANCE I KEPT.",
  "§8§oGO ON. LOOK AT THE CRATER.",
];
const FORGER_LINES_V15 = [
  "§d§oIT FORGES THE SKIN OF STORMS.",
  "§d§oTHE BELLS OF THE FORGER RING DOWNWARD.",
  "§d§oYOUR CEILING WILL BE BORROWED.",
];
const BHS_SHOPPER_LINES_V15 = [
  "§b§oERRAND 7: ACQUIRE. THE CART KNOWS THE ROUTE.",
  "§b§oDO NOT BLOCK THE AISLE. THE AISLE IS ETERNAL.",
  "§b§oTHE LADDER GOES WHERE IT NEEDS TO. MIND THE CARTS.",
];

/** The Creator: speaks in real human, slow and deep; swings the Hand at the near. */
function creatorTick() {
  const now = world.getTime();
  for (const d of dims()) {
    let creators;
    try { creators = d.getEntities({ families: ["ds_creator"], type: "ds:creator" }); }
    catch { creators = []; }
    for (const c of creators) {
      if (now - prop(c, "ds_last_speak", 0) > 3000 + Math.floor(Math.random() * 1200)) {
        setProp(c, "ds_last_speak", now);
        const viewers = playersNear(c, 220);
        if (viewers.length) {
          const line = CREATOR_LINES_V15[Math.floor(Math.random() * CREATOR_LINES_V15.length)];
          for (const p of viewers) p.sendMessage(line);
          try { c.dimension.playSound("ds.record.signal_tape", c.location, { volume: 0.8, pitch: 0.5 }); } catch { /* silence is also his */ }
        }
      }
      if (now - prop(c, "ds_last_hand", 0) > 220) {
        const near = playersNear(c, 40);
        if (near.length) {
          setProp(c, "ds_last_hand", now);
          const target = near[Math.floor(Math.random() * near.length)];
          handStrike(c, target);
        }
      }
    }
  }
}

function handStrike(creator, target) {
  const dim = creator.dimension;
  const base = { ...target.location };
  let hand;
  try { hand = dim.spawnEntity("ds:creator_hand", { x: base.x, y: base.y + 36, z: base.z }); } catch { return; }
  broadcastAll("§8§lTHE HAND IS ALREADY THERE.§r");
  system.runTimeout(() => { try { hand.teleport({ x: base.x, y: base.y + 12, z: base.z }); } catch { /* phases through weather */ } }, 12);
  system.runTimeout(() => {
    try {
      hand.teleport({ x: base.x, y: base.y + 1.5, z: base.z });
      dim.playSound("ds.massg.devour", base, { volume: 4.0, pitch: 0.55 });
      for (let i = 0; i < 26; i++) dim.spawnParticle("ds:glitch", { x: base.x + (Math.random() - 0.5) * 8, y: base.y + 0.4, z: base.z + (Math.random() - 0.5) * 8 });
      dim.spawnParticle("ds:sky_flash", base);
      for (const p of playersNear({ dimension: dim, location: base }, 9)) {
        try { p.applyKnockback({ x: (Math.random() - 0.5), z: (Math.random() - 0.5) }, 2.2); } catch { /* caught in the hand */ }
        try { p.applyDamage(14, { cause: "entityAttack", damagingEntity: creator }); } catch { /* the hand forgives cheats / nothing */ }
      }
    } catch { /* already landed */ }
  }, 24);
  system.runTimeout(() => { try { hand.remove(); } catch { /* retracted politely */ } }, 70);
}

/** The Monstrosity: spreads the channel-static lawn. */
function monstrosityTick() {
  for (const d of dims()) {
    let mobs;
    try { mobs = d.getEntities({ families: ["ds_monstrosity"] }); } catch { mobs = []; }
    for (const m of mobs) {
      if (Math.random() < 0.5) continue;
      const here = m.location;
      const spot = {
        x: Math.floor(here.x + (Math.random() - 0.5) * 14),
        y: Math.floor(here.y - 1),
        z: Math.floor(here.z + (Math.random() - 0.5) * 14),
      };
      try {
        const block = d.getBlock(spot);
        if (!block) continue;
        if (!block.isAir && !block.typeId.startsWith("ds:glitch_block")) {
          block.setType("ds:glitch_block");
          d.spawnParticle("ds:glitch", { x: spot.x + 0.5, y: spot.y + 1, z: spot.z + 0.5 });
        }
      } catch { /* unloaded; the lawn waits */ }
      if (Math.random() < 0.05) for (const p of playersNear(m, 30)) {
        try { p.sendMessage("§d§k THE MONSTROSITY LOOKS BACK §r"); } catch { /* it read you there too */ }
      }
    }
  }
}

/** The Forger: bell-notes drop sky tentacles over the players underneath. */
function forgerTick() {
  for (const d of dims()) {
    let forgers;
    try { forgers = d.getEntities({ families: ["ds_forger"], type: "ds:forger" }); } catch { forgers = []; }
    for (const f of forgers) {
      if (world.getTime() - prop(f, "ds_last_rain", 0) < 180) continue;
      const targets = playersNear(f, 60);
      if (!targets.length) continue;
      setProp(f, "ds_last_rain", world.getTime());
      const t = targets[Math.floor(Math.random() * targets.length)];
      const pos = t.location;
      const count = 5 + Math.floor(Math.random() * 4);
      broadcastAll(FORGER_LINES_V15[Math.floor(Math.random() * FORGER_LINES_V15.length)]);
      try { d.playSound("ds.rift.open", f.location, { volume: 3.0, pitch: 0.7 }); } catch { /* bells are unmuted elsewhere */ }
      for (let i = 0; i < count; i++) {
        system.runTimeout(() => {
          try {
            const spot = {
              x: pos.x + (Math.random() - 0.5) * 16,
              y: pos.y + 22 + Math.random() * 8,
              z: pos.z + (Math.random() - 0.5) * 16,
            };
            const tent = d.spawnEntity("ds:sky_tentacle", spot);
            try { tent.applyImpulse({ x: 0, y: -0.9, z: 0 }); } catch { /* gravity signed off */ }
          } catch { /* one sank into it */ }
        }, i * 3);
      }
    }
  }
}

/** The Cart Shoppers: whisper aisle policy to the unfortunate. */
function shopperTick() {
  for (const d of dims()) {
    let shoppers;
    try { shoppers = d.getEntities({ families: ["ds_shopper"] }); } catch { shoppers = []; }
    for (const sh of shoppers) {
      if (Math.random() < 0.985) continue;
      const line = BHS_SHOPPER_LINES_V15[Math.floor(Math.random() * BHS_SHOPPER_LINES_V15.length)];
      for (const p of playersNear(sh, 16)) try { p.sendMessage(line); } catch { /* it overhead anyway */ }
    }
  }
}



const VHS_TRACKS = ["ds.record.signal_tape", "ds.record.eaoin", "ds.record.countdown", "ds.record.quarantine"];
const vhsPlaying = new Map(); // "x,y,z" -> world time until the tape ends

function registerV15Items() {
  // The VHS Jukebox: touching it drops the needle. The counter starts; you are always playing.
  world.afterEvents.playerInteractWithBlock.subscribe((event) => {
    const block = event.block;
    if (!block || block.typeId !== "ds:vhs_jukebox") return;
    const key = `${block.location.x},${block.location.y},${block.location.z}`;
    const now = world.getTime();
    if (now < (vhsPlaying.get(key) ?? 0)) {
      event.player.sendMessage("§f§o▶ PLAY §7— the tape has not finished telling you.§r");
      return;
    }
    const track = VHS_TRACKS[Math.floor(Math.random() * VHS_TRACKS.length)];
    vhsPlaying.set(key, now + 940);
    try { block.dimension.playSound(track, block.location, { volume: 2.2, pitch: 1.0 }); } catch { /* fidelity failure is feature-shaped */ }
    event.player.sendMessage("§f§l▶ PLAY§r §7— §o" + track.split(".").pop() + "§r");
  });

  world.afterEvents.itemUse.subscribe((event) => {
    const player = event.source;
    const item = event.itemStack;
    if (!player || !item) return;

    if (item.typeId === "ds:broken_record") {
      player.playSound("ds.record.signal_tape", { volume: 0.6, pitch: 0.3 });
      for (let i = 0; i < 14; i++) try { player.dimension.spawnParticle("ds:glitch", { x: player.location.x + (Math.random() - 0.5) * 6, y: player.location.y + 1 + Math.random() * 2, z: player.location.z + (Math.random() - 0.5) * 6 }); } catch { /* sparkles eluded paperwork */ }
      player.sendMessage("§8§o-The record skips. The skip repeats you. Inside the skip, something black unfolds.-§r");
      try { player.dimension.spawnEntity("ds:monstrosity", { x: player.location.x + 3, y: player.location.y + 1, z: player.location.z + 3 }); } catch { /* the abyss requested a rain check */ }
    }
    if (item.typeId === "ds:rocket_key") {
      player.sendMessage("§f§lTHE ROCKET ACCEPTS ITS NAME. §r§b§obuckle up — the sky goes the wrong way.§r");
      player.playSound("ds.rift.open", { volume: 2.5, pitch: 1.2 });
      try { player.applyKnockback({ x: 0, z: 0 }, 3.2); } catch { /* gravity respected the pronunciation */ }
      system.runTimeout(() => { try { player.applyKnockback({ x: 0, z: 0 }, 3.4); } catch { /* second stage, exit */ } }, 10);
      system.runTimeout(() => {
        try { player.teleport({ x: END_PLATFORM.x + 2, y: END_PLATFORM.y + 60, z: END_PLATFORM.z + 2 }, { dimension: endDim() }); } catch { /* one-way trips are still trips */ }
        player.sendMessage("§b§oThe Earth Eater signs your travel documents without looking up.§r");
      }, 24);
    }
  });
}

system.runTimeout(registerV15Items, 100);
system.runInterval(creatorTick, 20);
system.runInterval(monstrosityTick, 12);
system.runInterval(forgerTick, 12);
system.runInterval(shopperTick, 17);
