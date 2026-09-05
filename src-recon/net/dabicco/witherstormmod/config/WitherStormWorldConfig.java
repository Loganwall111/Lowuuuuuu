package net.dabicco.witherstormmod.config;

import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.saveddata.SavedData;

public class WitherStormWorldConfig extends SavedData {
   public int severedScavenge = 1;
   public double severedScavengeInterval = 14.0;
   public double spiralStrength = 0.02;
   public double maxClusterSpeed = 0.35;
   public double phaseRequirementModifier = 1.0;
   public int clusterCooldown = 100;
   public int absorptionRadius = 8;
   public double pickupRangeModifier = 1.0;
   public int maxClusterRadius = 2;
   public static final net.dabicco.witherstormmod.config.WitherStormWorldConfig.ClusterStage[] CLUSTER_STAGES = new net.dabicco.witherstormmod.config.WitherStormWorldConfig.ClusterStage[]{
      new net.dabicco.witherstormmod.config.WitherStormWorldConfig.ClusterStage("maxClusterSizePhase0", 0.0, "phase 0", 0),
      new net.dabicco.witherstormmod.config.WitherStormWorldConfig.ClusterStage("maxClusterSizePhase1", 1.0, "phases 1-3", 1),
      new net.dabicco.witherstormmod.config.WitherStormWorldConfig.ClusterStage("maxClusterSizePhase4", 4.0, "phase 4", 2),
      new net.dabicco.witherstormmod.config.WitherStormWorldConfig.ClusterStage("maxClusterSizePhase5", 5.0, "phase 5", 3),
      new net.dabicco.witherstormmod.config.WitherStormWorldConfig.ClusterStage("maxClusterSizePhase58", 5.8, "phase 5.8+", 4)
   };
   public final int[] clusterStageMax = defaultClusterStageMax();
   public static final double BASE_PICKUP_RANGE = 48.0;
   public int beamClusterInterval = 70;
   public int beamGroundRadius = 3;
   public int beamShutoff = 1;
   public int headFireInterval = 100;
   public double headTargetRange = 96.0;
   public double roarRange = 260.0;
   public double beamSoundRange = 190.0;
   public int phase4Requirement = 2200;
   public int phase5Requirement = 9000;
   public double phase4TurnSpeed = 1.0;
   public double phase5TurnSpeed = 0.5;
   public double phase58TurnSpeed = 0.25;
   public double phase58DriftStrength = 1.0;
   public double stormSpeed = 0.1;
   public double stormStandoff = 50.0;
   public int phase4Altitude = 40;
   public double recoilStrength = 1.5;
   public int spawnFreezeSeconds = 60;
   public double chaseSpeed = 0.8;
   public int chaseIntervalMinutes = 90;
   public int distractionIntervalMinutes = 20;
   public int distractionDurationSeconds = 90;
   public int distractionRange = 160;
   public int caveRumble = 1;
   public int caveRumbleInterval = 75;
   public int caveRumbleDuration = 6;
   public double caveRumbleIntensity = 1.0;
   public int mobsFlee = 1;
   public int headForgiveSeconds = 25;
   public int mobPickup = 1;
   public int castThroughWater = 1;
   public int clustersTakeLiquids = 0;
   public int orbitStationaryTargets = 1;
   public int beamImpactLight = 11;
   public int tentacleAwareness = 1;
   public int witherSickness = 1;
   public int witheredMobs = 1;
   public int witheredMax = 24;
   public int witheredMaxCaves = 3;
   public int netherScale = 1;
   public int netherScaleInterval = 300;
   public int netherScaleRandom = 30;
   public int worldDarkening = 100;
   public int postFormidibombChase = 1;
   public int postFormidibombChaseSpeed = 140;
   public int fastGrowthToSixOne = 1;
   public int fastGrowthToSixOneSpeed = 220;
   public int endermanSiege = 1;
   public int endermanSiegeCount = 24;
   public int endermanSiegeSeconds = 95;
   public int endermanSiegeDistance = 90;
   public int endermanSiegeSlowdown = 50;
   public int endermanSiegeTentacleSpeed = 260;
   public int endermanSiegeBeamEats = 1;
   public int targetingMode = 0;
   public int instantGrowth = 0;
   public double instantGrowthRate = 4.0;
   public int infinitePhases = 0;
   public double phaseCeiling = 7.0;
   public int tentacleSlam = 1;
   public int tentacleSlamInterval = 260;
   public double tentacleSlamRadius = 12.0;
   public int structureRaid = 1;
   public int structureRaidInterval = 200;
   public double structureRaidRadius = 8.0;
   public int structureTearClusters = 3;
   public int townNpcPopulation = 10;
   public int deathBlast = 1;
   public double deathBlastRadius = 24.0;
   public int berserk = 1;
   public double berserkHealth = 0.3F;
   public int berserkSlamInterval = 40;
   public int buildingDestruction = 1;
   public double buildingTearRadius = 36.0;
   public int buildingTearInterval = 50;
   public int buildingClusterSize = 3;
   public int groundShakeOnSlam = 1;
   public double groundShakeRadius = 80.0;
   public int groundShockwaveParticles = 1;
   public int superCataclysmLightning = 1;
   public int lightningDischargeInterval = 80;
   public double lightningDamage = 15.0;
   public double tentacleSwoopSpeed = 1.5;
   public double tentacleThrowPower = 3.0;
   public double tentacleChompDamage = 100.0;
   public double tentacleSnatchRange = 48.0;
   public int tentacleTargetMobs = 1;
   public int tentacleEscapeHits = 5;
   public double debrisTornadoSpeed = 1.4;
   public double debrisDamageMultiplier = 1.0;
   public double bossHealthMultiplier = 1.0;
   public double bossAttackDamageMultiplier = 1.0;
   public double tractorBeamPullPower = 1.2;
   public double tractorBeamLiftSpeed = 0.8;
   public static final String[] TARGETING_LABELS = new String[]{"Ultimate", "Natural", "Nearest", "Group", "Structures"};
   public static final Map<String, net.dabicco.witherstormmod.config.WitherStormWorldConfig.Key> KEYS = new LinkedHashMap<>();
   public static final Codec<net.dabicco.witherstormmod.config.WitherStormWorldConfig> CODEC;

   private static int[] defaultClusterStageMax() {
      int[] out = new int[CLUSTER_STAGES.length];

      for (int i = 0; i < CLUSTER_STAGES.length; i++) {
         out[i] = CLUSTER_STAGES[i].defaultMax();
      }

      return out;
   }

   public int maxClusterRadiusFor(double phase) {
      int result = CLUSTER_STAGES[0].defaultMax();

      for (int i = 0; i < CLUSTER_STAGES.length; i++) {
         if (phase >= CLUSTER_STAGES[i].minPhase()) {
            result = this.clusterStageMax[i];
         }
      }

      return result;
   }

   public int pickupRange() {
      return (int)Math.max(8.0, 48.0 * this.pickupRangeModifier);
   }

   public Types groundHeightmap() {
      return this.castThroughWater != 0 ? Types.OCEAN_FLOOR : Types.MOTION_BLOCKING;
   }

   private static void key(
      String name,
      String description,
      double min,
      double max,
      boolean integer,
      ToDoubleFunction<net.dabicco.witherstormmod.config.WitherStormWorldConfig> get,
      ObjDoubleConsumer<net.dabicco.witherstormmod.config.WitherStormWorldConfig> set
   ) {
      KEYS.put(
         name,
         new net.dabicco.witherstormmod.config.WitherStormWorldConfig.Key(
            name, description, min, max, integer, net.dabicco.witherstormmod.config.WitherStormWorldConfig.Widget.SLIDER, (String[])null, get, set
         )
      );
   }

   private static void keyToggle(
      String name,
      String description,
      ToDoubleFunction<net.dabicco.witherstormmod.config.WitherStormWorldConfig> get,
      ObjDoubleConsumer<net.dabicco.witherstormmod.config.WitherStormWorldConfig> set
   ) {
      KEYS.put(
         name,
         new net.dabicco.witherstormmod.config.WitherStormWorldConfig.Key(
            name, description, 0.0, 1.0, true, net.dabicco.witherstormmod.config.WitherStormWorldConfig.Widget.TOGGLE, (String[])null, get, set
         )
      );
   }

   private static void keyCycle(
      String name,
      String description,
      String[] labels,
      ToDoubleFunction<net.dabicco.witherstormmod.config.WitherStormWorldConfig> get,
      ObjDoubleConsumer<net.dabicco.witherstormmod.config.WitherStormWorldConfig> set
   ) {
      KEYS.put(
         name,
         new net.dabicco.witherstormmod.config.WitherStormWorldConfig.Key(
            name, description, 0.0, labels.length - 1, true, net.dabicco.witherstormmod.config.WitherStormWorldConfig.Widget.CYCLE, labels, get, set
         )
      );
   }

   public double[] toArray() {
      double[] out = new double[KEYS.size()];
      int i = 0;

      for (net.dabicco.witherstormmod.config.WitherStormWorldConfig.Key key : KEYS.values()) {
         out[i++] = key.get().applyAsDouble(this);
      }

      return out;
   }

   public void applyArray(double[] v) {
      int i = 0;

      for (net.dabicco.witherstormmod.config.WitherStormWorldConfig.Key key : KEYS.values()) {
         if (i >= v.length) {
            break;
         }

         key.set().accept(this, key.clamp(v[i++]));
      }
   }

   public void markChanged() {
      this.setDirty();
   }

   static {
      key("spiralStrength", "How hard clusters spiral around the storm", 0.0, 0.2, false, c -> c.spiralStrength, (c, v) -> c.spiralStrength = v);
      key("clusterSpeed", "Max travel speed of debris clusters", 0.0, 2.0, false, c -> c.maxClusterSpeed, (c, v) -> c.maxClusterSpeed = v);
      key(
         "phaseRequirementModifier",
         "Scales how much the storm must eat to grow",
         0.1,
         5.0,
         false,
         c -> c.phaseRequirementModifier,
         (c, v) -> c.phaseRequirementModifier = v
      );
      key("clusterCooldown", "Ticks between cluster spawns", 0.0, 2000.0, true, c -> c.clusterCooldown, (c, v) -> c.clusterCooldown = (int)v);
      key("absorptionRadius", "Distance clusters shrink into the storm", 1.0, 32.0, true, c -> c.absorptionRadius, (c, v) -> c.absorptionRadius = (int)v);
      key("pickupRangeModifier", "Multiplies pickup reach (48 blocks at 1.0)", 0.5, 5.0, false, c -> c.pickupRangeModifier, (c, v) -> c.pickupRangeModifier = v);

      for (int i = 0; i < CLUSTER_STAGES.length; i++) {
         net.dabicco.witherstormmod.config.WitherStormWorldConfig.ClusterStage stage = CLUSTER_STAGES[i];
         int idx = i;
         key(
            stage.key(),
            "Biggest random cluster radius in " + stage.label(),
            0.0,
            8.0,
            true,
            c -> c.clusterStageMax[idx],
            (c, v) -> c.clusterStageMax[idx] = (int)v
         );
      }

      key("beamClusterInterval", "Ticks between beam-spawned clusters", 20.0, 400.0, true, c -> c.beamClusterInterval, (c, v) -> c.beamClusterInterval = (int)v);
      key("beamGroundRadius", "Tractor beam ground circle radius", 1.0, 8.0, true, c -> c.beamGroundRadius, (c, v) -> c.beamGroundRadius = (int)v);
      keyToggle("beamShutoff", "Heads may turn their beams off at will (mood flicker)", c -> c.beamShutoff, (c, v) -> c.beamShutoff = (int)v);
      key("headFireInterval", "Base ticks between head shots", 20.0, 600.0, true, c -> c.headFireInterval, (c, v) -> c.headFireInterval = (int)v);
      key("headTargetRange", "How far heads look for targets", 16.0, 256.0, false, c -> c.headTargetRange, (c, v) -> c.headTargetRange = v);
      key(
         "phase4Requirement",
         "Growth needed per step in phase 4 (higher = phase 4 lasts far longer)",
         200.0,
         30000.0,
         true,
         c -> c.phase4Requirement,
         (c, v) -> c.phase4Requirement = (int)v
      );
      key(
         "phase5Requirement",
         "Growth needed per step in phase 5+ (higher = phase 5 lasts far longer)",
         200.0,
         60000.0,
         true,
         c -> c.phase5Requirement,
         (c, v) -> c.phase5Requirement = (int)v
      );
      key("phase4TurnSpeed", "How fast the body turns in phase 4 (1.0 = normal)", 0.1, 3.0, false, c -> c.phase4TurnSpeed, (c, v) -> c.phase4TurnSpeed = v);
      key(
         "phase5TurnSpeed",
         "How fast the body turns in phase 5+ (1.0 = phase-4 speed)",
         0.1,
         3.0,
         false,
         c -> c.phase5TurnSpeed,
         (c, v) -> c.phase5TurnSpeed = v
      );
      key("phase58TurnSpeed", "How fast the body turns in late phase 5 (5.8+)", 0.05, 3.0, false, c -> c.phase58TurnSpeed, (c, v) -> c.phase58TurnSpeed = v);
      key(
         "phase58DriftStrength",
         "How much the late phase-5 body wanders on its windy drift",
         0.0,
         4.0,
         false,
         c -> c.phase58DriftStrength,
         (c, v) -> c.phase58DriftStrength = v
      );
      key("stormSpeed", "Phase-4 fly speed", 0.02, 0.5, false, c -> c.stormSpeed, (c, v) -> c.stormSpeed = v);
      key("stormStandoff", "Preferred distance from its target", 10.0, 200.0, false, c -> c.stormStandoff, (c, v) -> c.stormStandoff = v);
      key("cruiseAltitude", "Height above ground it tries to hold", 10.0, 120.0, true, c -> c.phase4Altitude, (c, v) -> c.phase4Altitude = (int)v);
      key("recoilStrength", "Head-fire body jolt strength", 0.0, 6.0, false, c -> c.recoilStrength, (c, v) -> c.recoilStrength = v);
      key(
         "spawnFreezeSeconds",
         "Seconds the storm sits frozen after spawning before it moves or eats",
         0.0,
         120.0,
         true,
         c -> c.spawnFreezeSeconds,
         (c, v) -> c.spawnFreezeSeconds = (int)v
      );
      key("chaseSpeed", "Fly speed while actively chasing a player", 0.1, 3.0, false, c -> c.chaseSpeed, (c, v) -> c.chaseSpeed = v);
      key(
         "chaseInterval",
         "Minutes between automatic chases (phase 4+)",
         5.0,
         720.0,
         true,
         c -> c.chaseIntervalMinutes,
         (c, v) -> c.chaseIntervalMinutes = (int)v
      );
      key(
         "distractionInterval",
         "Minutes of chasing before it can get distracted",
         1.0,
         240.0,
         true,
         c -> c.distractionIntervalMinutes,
         (c, v) -> c.distractionIntervalMinutes = (int)v
      );
      key(
         "distractionDuration",
         "Seconds a distraction lasts",
         10.0,
         600.0,
         true,
         c -> c.distractionDurationSeconds,
         (c, v) -> c.distractionDurationSeconds = (int)v
      );
      key(
         "distractionRange",
         "How far away the random distraction point lands",
         32.0,
         512.0,
         true,
         c -> c.distractionRange,
         (c, v) -> c.distractionRange = (int)v
      );
      keyCycle(
         "targetingMode",
         "How the storm chooses where to go: Ultimate (fixed target), Natural (decides for itself), Nearest player, Group, or Structures (tours built structures and levels them)",
         TARGETING_LABELS,
         c -> c.targetingMode,
         (c, v) -> c.targetingMode = (int)v
      );
      keyToggle(
         "instantGrowth",
         "The storm grows from every scrap of block it pulls in, charging through the early phases almost instantly",
         c -> c.instantGrowth,
         (c, v) -> c.instantGrowth = (int)v
      );
      key(
         "instantGrowthRate",
         "Instant-growth multiplier applied to every bit of growth the storm eats (higher = it races through phases)",
         1.0,
         100.0,
         false,
         c -> c.instantGrowthRate,
         (c, v) -> c.instantGrowthRate = v
      );
      keyToggle(
         "infinitePhases",
         "Experimental: lift the phase ceiling so the storm keeps growing past 6.99 with no hard cap (its body may not have art past 6.1, but it keeps eating and getting stronger)",
         c -> c.infinitePhases,
         (c, v) -> c.infinitePhases = (int)v
      );
      key("phaseCeiling", "Highest phase the storm may reach when Infinite Phases is on", 6.0, 30.0, true, c -> c.phaseCeiling, (c, v) -> c.phaseCeiling = v);
      keyToggle(
         "tentacleSlam",
         "The storm hammers its tentacles into the ground, caving in a crater and flinging everything nearby",
         c -> c.tentacleSlam,
         (c, v) -> c.tentacleSlam = (int)v
      );
      key("tentacleSlamInterval", "Ticks between tentacle slams", 60.0, 1200.0, true, c -> c.tentacleSlamInterval, (c, v) -> c.tentacleSlamInterval = (int)v);
      key(
         "tentacleSlamRadius",
         "Radius of a tentacle slam crater and its blast",
         3.0,
         24.0,
         true,
         c -> c.tentacleSlamRadius,
         (c, v) -> c.tentacleSlamRadius = (int)v
      );
      keyToggle(
         "structureRaid",
         "While the storm is touring built structures (Structures targeting), it actively levels them, caving chunks out of the target as it dwells",
         c -> c.structureRaid,
         (c, v) -> c.structureRaid = (int)v
      );
      key(
         "structureRaidInterval",
         "Ticks between structure-raid caved-ins",
         60.0,
         600.0,
         true,
         c -> c.structureRaidInterval,
         (c, v) -> c.structureRaidInterval = (int)v
      );
      key(
         "structureRaidRadius", "Radius of each structure-raid caved-in", 2.0, 16.0, true, c -> c.structureRaidRadius, (c, v) -> c.structureRaidRadius = (int)v
      );
      key(
         "structureTearClusters",
         "Chunks ripped out of a raided structure as flying clusters per raid - the building visibly breaks open and its pieces fly up to the storm",
         0.0,
         8.0,
         true,
         c -> c.structureTearClusters,
         (c, v) -> c.structureTearClusters = (int)v
      );
      keyToggle(
         "deathBlast",
         "When the storm is finally destroyed, it detonates in a cataclysmic blast, caving a huge crater and flinging everything around it (the story's final explosion)",
         c -> c.deathBlast,
         (c, v) -> c.deathBlast = (int)v
      );
      key(
         "townNpcPopulation",
         "Named Story Mode villagers placed when building a town",
         0.0,
         20.0,
         true,
         c -> c.townNpcPopulation,
         (c, v) -> c.townNpcPopulation = (int)v
      );
      key(
         "deathBlastRadius",
         "Radius of the storm's death blast crater and its damage",
         4.0,
         64.0,
         true,
         c -> c.deathBlastRadius,
         (c, v) -> c.deathBlastRadius = (int)v
      );
      keyToggle(
         "berserk",
         "When the Devourer is wounded below a health threshold it flies into a rage, slamming its tentacles far more often (the desperate final act)",
         c -> c.berserk,
         (c, v) -> c.berserk = (int)v
      );
      key("berserkHealth", "Health fraction (0-1) below which the Devourer goes berserk", 0.1, 0.9F, false, c -> c.berserkHealth, (c, v) -> c.berserkHealth = v);
      key(
         "berserkSlamInterval",
         "Tentacle slam interval in ticks once berserk",
         10.0,
         120.0,
         true,
         c -> c.berserkSlamInterval,
         (c, v) -> c.berserkSlamInterval = (int)v
      );
      keyToggle(
         "netherScale", "A giant tentacle swoops through the Nether at players hiding there (phase 5.1+)", c -> c.netherScale, (c, v) -> c.netherScale = (int)v
      );
      key(
         "netherScaleInterval",
         "Base seconds a player must linger in the Nether before a scaling can hit",
         30.0,
         3600.0,
         true,
         c -> c.netherScaleInterval,
         (c, v) -> c.netherScaleInterval = (int)v
      );
      key(
         "netherScaleRandom",
         "Extra random seconds added on top of the base interval",
         0.0,
         600.0,
         true,
         c -> c.netherScaleRandom,
         (c, v) -> c.netherScaleRandom = (int)v
      );
      key(
         "worldDarkening",
         "How far a storm may darken the world's lighting on this server, as a percent. Multiplies each player's own Darken World Lighting setting.",
         0.0,
         100.0,
         true,
         c -> c.worldDarkening,
         (c, v) -> c.worldDarkening = (int)v
      );
      keyToggle(
         "postFormidibombChase",
         "After a Formidibomb the storm gets up and comes straight for the nearest player instead of going back to what it was doing",
         c -> c.postFormidibombChase,
         (c, v) -> c.postFormidibombChase = (int)v
      );
      key(
         "postFormidibombChaseSpeed",
         "How fast that chase is, as a percent of the normal chase speed. Over 100 is faster than it normally hunts.",
         25.0,
         300.0,
         true,
         c -> c.postFormidibombChaseSpeed,
         (c, v) -> c.postFormidibombChaseSpeed = (int)v
      );
      keyToggle(
         "fastGrowthToSixOne",
         "Hurry the Devourer from phase 6 to 6.1, where its second head comes in, then return to normal growth",
         c -> c.fastGrowthToSixOne,
         (c, v) -> c.fastGrowthToSixOne = (int)v
      );
      key(
         "fastGrowthToSixOneSpeed",
         "How much faster, as a percent",
         100.0,
         500.0,
         true,
         c -> c.fastGrowthToSixOneSpeed,
         (c, v) -> c.fastGrowthToSixOneSpeed = (int)v
      );
      keyToggle(
         "endermanSiege",
         "At phase 6.1, once the Devourer has grown its second head, endermen gather in front of it and it turns on them",
         c -> c.endermanSiege,
         (c, v) -> c.endermanSiege = (int)v
      );
      key("endermanSiegeCount", "How many gather", 0.0, 80.0, true, c -> c.endermanSiegeCount, (c, v) -> c.endermanSiegeCount = (int)v);
      key(
         "endermanSiegeSeconds",
         "How long the siege runs, in seconds",
         10.0,
         600.0,
         true,
         c -> c.endermanSiegeSeconds,
         (c, v) -> c.endermanSiegeSeconds = (int)v
      );
      key(
         "endermanSiegeDistance",
         "How far in front of the storm they appear, in blocks",
         10.0,
         120.0,
         true,
         c -> c.endermanSiegeDistance,
         (c, v) -> c.endermanSiegeDistance = (int)v
      );
      key(
         "endermanSiegeSlowdown",
         "What the storm's speed drops to while they are on it, as a percent",
         10.0,
         100.0,
         true,
         c -> c.endermanSiegeSlowdown,
         (c, v) -> c.endermanSiegeSlowdown = (int)v
      );
      key(
         "endermanSiegeTentacleSpeed",
         "How much faster its tentacles thrash during it, as a percent",
         100.0,
         600.0,
         true,
         c -> c.endermanSiegeTentacleSpeed,
         (c, v) -> c.endermanSiegeTentacleSpeed = (int)v
      );
      keyToggle(
         "endermanSiegeBeamEats",
         "An enderman caught in a tractor beam is simply gone",
         c -> c.endermanSiegeBeamEats,
         (c, v) -> c.endermanSiegeBeamEats = (int)v
      );
      keyToggle(
         "caveRumble",
         "Cave Rumble: while the storm is overhead and you are underground, the ceiling shakes, blocks drop out of it, and dripstone lets go",
         c -> c.caveRumble,
         (c, v) -> c.caveRumble = (int)v
      );
      key(
         "caveRumbleInterval",
         "Base seconds between cave rumbles (a random half again is added on top, so they never fall into a rhythm)",
         10.0,
         900.0,
         true,
         c -> c.caveRumbleInterval,
         (c, v) -> c.caveRumbleInterval = (int)v
      );
      key("caveRumbleDuration", "How many seconds a cave rumble lasts", 1.0, 60.0, true, c -> c.caveRumbleDuration, (c, v) -> c.caveRumbleDuration = (int)v);
      key(
         "caveRumbleIntensity",
         "How violent a cave rumble is: the screen shake, how much of the ceiling comes down, and how loud it is",
         0.1,
         3.0,
         false,
         c -> c.caveRumbleIntensity,
         (c, v) -> c.caveRumbleIntensity = v
      );
      keyToggle("mobsFlee", "Nearby mobs panic and run away from the storm", c -> c.mobsFlee, (c, v) -> c.mobsFlee = (int)v);
      key(
         "headForgiveSeconds",
         "Breathing-room seconds after a head lets a player go",
         0.0,
         600.0,
         true,
         c -> c.headForgiveSeconds,
         (c, v) -> c.headForgiveSeconds = (int)v
      );
      keyToggle("mobPickup", "Beams pull in and consume non-target mobs", c -> c.mobPickup, (c, v) -> c.mobPickup = (int)v);
      keyToggle("castThroughWater", "Beams and clusters reach through water to the seabed", c -> c.castThroughWater, (c, v) -> c.castThroughWater = (int)v);
      keyToggle("clustersTakeLiquids", "Clusters carry water and lava away too", c -> c.clustersTakeLiquids, (c, v) -> c.clustersTakeLiquids = (int)v);
      keyToggle(
         "severedScavenge",
         "Severed halves pull up small block clusters of their own. Never counts toward the storm's growth.",
         c -> c.severedScavenge,
         (c, v) -> c.severedScavenge = (int)v
      );
      key(
         "severedScavengeInterval",
         "Seconds between a severed half's scavenging attempts",
         2.0,
         120.0,
         false,
         c -> c.severedScavengeInterval,
         (c, v) -> c.severedScavengeInterval = v
      );
      keyToggle("orbitStationaryTargets", "Circle a target that stays in one place", c -> c.orbitStationaryTargets, (c, v) -> c.orbitStationaryTargets = (int)v);
      key(
         "roarRange",
         "How far the storm's roars carry, in blocks. Bites and snarls reach three quarters as far, big roars a quarter further.",
         64.0,
         640.0,
         false,
         c -> c.roarRange,
         (c, v) -> c.roarRange = v
      );
      key(
         "beamSoundRange",
         "How far the tractor beam's switch-on and switch-off carry, in blocks",
         32.0,
         480.0,
         false,
         c -> c.beamSoundRange,
         (c, v) -> c.beamSoundRange = v
      );
      key(
         "beamImpactLight",
         "How brightly a beam lights the ground it lands on (0 = off)",
         0.0,
         15.0,
         true,
         c -> c.beamImpactLight,
         (c, v) -> c.beamImpactLight = (int)v
      );
      keyToggle("tentacleAwareness", "A body tentacle reaches for players caught in a beam", c -> c.tentacleAwareness, (c, v) -> c.tentacleAwareness = (int)v);
      keyToggle(
         "buildingDestruction",
         "The storm actively tears houses, village structures, and roofs apart into floating debris clusters",
         c -> c.buildingDestruction,
         (c, v) -> c.buildingDestruction = (int)v
      );
      key(
         "buildingTearRadius",
         "Radius around the storm scanned for constructed buildings to tear apart",
         10.0,
         96.0,
         true,
         c -> c.buildingTearRadius,
         (c, v) -> c.buildingTearRadius = v
      );
      key(
         "buildingTearInterval",
         "Ticks between tearing chunks of buildings",
         10.0,
         300.0,
         true,
         c -> c.buildingTearInterval,
         (c, v) -> c.buildingTearInterval = (int)v
      );
      key(
         "buildingClusterSize",
         "Max block cluster radius from torn buildings",
         1.0,
         5.0,
         true,
         c -> c.buildingClusterSize,
         (c, v) -> c.buildingClusterSize = (int)v
      );
      keyToggle(
         "groundShakeOnSlam",
         "Tentacle slams cause an earthquake tremor and camera shake for nearby players",
         c -> c.groundShakeOnSlam,
         (c, v) -> c.groundShakeOnSlam = (int)v
      );
      key(
         "groundShakeRadius",
         "Radius of earthquake camera shake from tentacle slams",
         20.0,
         250.0,
         true,
         c -> c.groundShakeRadius,
         (c, v) -> c.groundShakeRadius = v
      );
      keyToggle(
         "groundShockwaveParticles",
         "Tentacle slams emit radial shockwaves, explosion dust rings, and dragon breath",
         c -> c.groundShockwaveParticles,
         (c, v) -> c.groundShockwaveParticles = (int)v
      );
      keyToggle(
         "superCataclysmLightning",
         "Cataclysm Phase (5.8+) discharges violent purple lightning strikes into terrain",
         c -> c.superCataclysmLightning,
         (c, v) -> c.superCataclysmLightning = (int)v
      );
      key(
         "lightningDischargeInterval",
         "Ticks between cataclysm purple lightning strikes",
         20.0,
         400.0,
         true,
         c -> c.lightningDischargeInterval,
         (c, v) -> c.lightningDischargeInterval = (int)v
      );
      key("lightningDamage", "Damage dealt by cataclysm purple lightning strikes", 1.0, 100.0, false, c -> c.lightningDamage, (c, v) -> c.lightningDamage = v);
      key("tentacleSwoopSpeed", "Velocity of swooping tentacle attacks", 0.5, 4.0, false, c -> c.tentacleSwoopSpeed, (c, v) -> c.tentacleSwoopSpeed = v);
      key(
         "tentacleThrowPower",
         "Catapult fling velocity when the tentacle throws a victim across the sky",
         1.0,
         6.0,
         false,
         c -> c.tentacleThrowPower,
         (c, v) -> c.tentacleThrowPower = v
      );
      key(
         "tentacleChompDamage",
         "Damage inflicted when a head chomps and eats a snatched victim",
         10.0,
         500.0,
         false,
         c -> c.tentacleChompDamage,
         (c, v) -> c.tentacleChompDamage = v
      );
      key(
         "tentacleSnatchRange",
         "Horizontal detection radius for tentacle swoops and grabs",
         10.0,
         80.0,
         true,
         c -> c.tentacleSnatchRange,
         (c, v) -> c.tentacleSnatchRange = v
      );
      keyToggle(
         "tentacleTargetMobs",
         "Tentacles swoop at and snatch nearby animals, golems, and monsters in addition to players",
         c -> c.tentacleTargetMobs,
         (c, v) -> c.tentacleTargetMobs = (int)v
      );
      key(
         "tentacleEscapeHits",
         "Hits with a weapon required to sever a tentacle's grip and escape",
         1.0,
         15.0,
         true,
         c -> c.tentacleEscapeHits,
         (c, v) -> c.tentacleEscapeHits = (int)v
      );
      key(
         "debrisTornadoSpeed",
         "Swirling speed of debris and block clusters orbiting the storm",
         0.1,
         4.0,
         false,
         c -> c.debrisTornadoSpeed,
         (c, v) -> c.debrisTornadoSpeed = v
      );
      key(
         "debrisDamageMultiplier",
         "Damage multiplier when flying block debris strikes players",
         0.0,
         5.0,
         false,
         c -> c.debrisDamageMultiplier,
         (c, v) -> c.debrisDamageMultiplier = v
      );
      key(
         "bossHealthMultiplier",
         "Overall health multiplier for the Wither Storm and Devourer",
         0.1,
         10.0,
         false,
         c -> c.bossHealthMultiplier,
         (c, v) -> c.bossHealthMultiplier = v
      );
      key(
         "bossAttackDamageMultiplier",
         "Damage multiplier for all storm attacks, skulls, beams, and slams",
         0.1,
         10.0,
         false,
         c -> c.bossAttackDamageMultiplier,
         (c, v) -> c.bossAttackDamageMultiplier = v
      );
      key(
         "tractorBeamPullPower",
         "Pull force of tractor beams on caught entities",
         0.1,
         3.0,
         false,
         c -> c.tractorBeamPullPower,
         (c, v) -> c.tractorBeamPullPower = v
      );
      key(
         "tractorBeamLiftSpeed",
         "Upward speed of tractor beams lifting entities to the maws",
         0.1,
         2.5,
         false,
         c -> c.tractorBeamLiftSpeed,
         (c, v) -> c.tractorBeamLiftSpeed = v
      );
      keyToggle("witherSickness", "Mobs near the storm too long get corrupted", c -> c.witherSickness, (c, v) -> c.witherSickness = (int)v);
      keyToggle("witheredMobs", "Fully corrupted mobs turn Withered and gain manipulation powers", c -> c.witheredMobs, (c, v) -> c.witheredMobs = (int)v);
      key(
         "witheredMax",
         "How many Withered mobs may exist at once (higher = busier and heavier)",
         1.0,
         64.0,
         true,
         c -> c.witheredMax,
         (c, v) -> c.witheredMax = (int)v
      );
      key(
         "witheredMaxCaves",
         "Of those, how many may have turned underground (kept low on purpose)",
         0.0,
         32.0,
         true,
         c -> c.witheredMaxCaves,
         (c, v) -> c.witheredMaxCaves = (int)v
      );
      CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).xmap(map -> {
         net.dabicco.witherstormmod.config.WitherStormWorldConfig cfg = new net.dabicco.witherstormmod.config.WitherStormWorldConfig();
         map.forEach((name, value) -> {
            net.dabicco.witherstormmod.config.WitherStormWorldConfig.Key key = KEYS.get(name);
            if (key != null) {
               key.set().accept(cfg, key.clamp(value));
            }
         });
         return cfg;
      }, cfg -> {
         Map<String, Double> map = new LinkedHashMap<>();

         for (net.dabicco.witherstormmod.config.WitherStormWorldConfig.Key key : KEYS.values()) {
            map.put(key.name(), key.get().applyAsDouble(cfg));
         }

         return map;
      });
   }

   public record ClusterStage(String key, double minPhase, String label, int defaultMax) {
   }

   public record Key(
      String name,
      String description,
      double min,
      double max,
      boolean integer,
      net.dabicco.witherstormmod.config.WitherStormWorldConfig.Widget widget,
      String[] cycleLabels,
      ToDoubleFunction<net.dabicco.witherstormmod.config.WitherStormWorldConfig> get,
      ObjDoubleConsumer<net.dabicco.witherstormmod.config.WitherStormWorldConfig> set
   ) {
      public double clamp(double v) {
         double clamped = Math.max(this.min, Math.min(this.max, v));
         return this.integer ? Math.round(clamped) : clamped;
      }
   }

   public static enum Widget {
      SLIDER,
      TOGGLE,
      CYCLE;

      private static net.dabicco.witherstormmod.config.WitherStormWorldConfig.Widget[] $values() {
         return new net.dabicco.witherstormmod.config.WitherStormWorldConfig.Widget[]{SLIDER, TOGGLE, CYCLE};
      }
   }
}
