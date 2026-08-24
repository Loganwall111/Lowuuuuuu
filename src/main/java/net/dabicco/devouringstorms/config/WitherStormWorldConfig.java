package net.dabicco.devouringstorms.config;

import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.saveddata.SavedData;

public class WitherStormWorldConfig extends SavedData {
   public int severedScavenge = 1;
   public double severedScavengeInterval = (double)14.0F;
   public double spiralStrength = 0.02;
   public double maxClusterSpeed = 0.35;
   public double phaseRequirementModifier = (double)1.0F;
   public int clusterCooldown = 100;
   public int absorptionRadius = 8;
   public double pickupRangeModifier = (double)1.0F;
   public int maxClusterRadius = 2;
   public static final ClusterStage[] CLUSTER_STAGES = new ClusterStage[]{new ClusterStage("maxClusterSizePhase0", (double)0.0F, "phase 0", 0), new ClusterStage("maxClusterSizePhase1", (double)1.0F, "phases 1-3", 1), new ClusterStage("maxClusterSizePhase4", (double)4.0F, "phase 4", 2), new ClusterStage("maxClusterSizePhase5", (double)5.0F, "phase 5", 3), new ClusterStage("maxClusterSizePhase58", 5.8, "phase 5.8+", 4)};
   public final int[] clusterStageMax = defaultClusterStageMax();
   public static final double BASE_PICKUP_RANGE = (double)48.0F;
   public int beamClusterInterval = 70;
   public int beamGroundRadius = 3;
   public int beamShutoff = 1;
   public int headFireInterval = 100;
   public double headTargetRange = (double)96.0F;
   public double roarRange = (double)260.0F;
   public double beamSoundRange = (double)190.0F;
   public int phase4Requirement = 2200;
   public int phase5Requirement = 9000;
   public double phase4TurnSpeed = (double)1.0F;
   public double phase5TurnSpeed = (double)0.5F;
   public double phase58TurnSpeed = (double)0.25F;
   public double phase58DriftStrength = (double)1.0F;
   public double stormSpeed = 0.1;
   public double stormStandoff = (double)50.0F;
   public int phase4Altitude = 40;
   public double recoilStrength = (double)1.5F;
   public int spawnFreezeSeconds = 60;
   public double chaseSpeed = 0.8;
   public int chaseIntervalMinutes = 90;
   public int distractionIntervalMinutes = 20;
   public int distractionDurationSeconds = 90;
   public int distractionRange = 160;
   public int caveRumble = 1;
   public int caveRumbleInterval = 75;
   public int caveRumbleDuration = 6;
   public double caveRumbleIntensity = (double)1.0F;
   public int voidCorruption = 1;
   public int voidCorruptionInterval = 40;
   public int voidCorruptionRadius = 28;
   public int voidCorruptionBursts = 10;
   public int voidCorruptionGrass = 0;
   public int voidCorruptionTrees = 0;
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
   public double instantGrowthRate = (double)4.0F;
   public int infinitePhases = 0;
   public int infiniteGrowth = 0;
   public int infiniteGrowthLocksStage = 1;
   public int infiniteGrowthSecondsPerPhase = 120;
   public double infiniteGrowthSpeed = (double)1.0F;
   public double phaseCeiling = (double)0.0F;
   public int tentacleSlam = 1;
   public int tentacleSlamInterval = 260;
   public double tentacleSlamRadius = (double)12.0F;
   public int structureRaid = 1;
   public int structureRaidInterval = 200;
   public double structureRaidRadius = (double)8.0F;
   public int structureTearClusters = 3;
   public int townNpcPopulation = 10;
   public int townGuardPopulation = 1;
   public int townCatPopulation = 2;
   public int townPigPopulation = 1;
   public int deathBlast = 1;
   public double deathBlastRadius = (double)24.0F;
   public int experimentalBowelsDeath = 0;
   public int berserk = 1;
   public double berserkHealth = (double)0.3F;
   public int berserkSlamInterval = 40;
   public static final String[] TARGETING_LABELS = new String[]{"Ultimate", "Natural", "Nearest", "Group", "Structures"};
   public static final Map<String, Key> KEYS = new LinkedHashMap();
   public static final Codec<WitherStormWorldConfig> CODEC;

   private static int[] defaultClusterStageMax() {
      int[] out = new int[CLUSTER_STAGES.length];

      for(int i = 0; i < CLUSTER_STAGES.length; ++i) {
         out[i] = CLUSTER_STAGES[i].defaultMax();
      }

      return out;
   }

   public int maxClusterRadiusFor(double phase) {
      int result = CLUSTER_STAGES[0].defaultMax();

      for(int i = 0; i < CLUSTER_STAGES.length; ++i) {
         if (phase >= CLUSTER_STAGES[i].minPhase()) {
            result = this.clusterStageMax[i];
         }
      }

      return result;
   }

   public int pickupRange() {
      return (int)Math.max((double)8.0F, (double)48.0F * this.pickupRangeModifier);
   }

   public Heightmap.Types groundHeightmap() {
      return this.castThroughWater != 0 ? Types.OCEAN_FLOOR : Types.MOTION_BLOCKING;
   }

   private static void key(String name, String description, double min, double max, boolean integer, ToDoubleFunction<WitherStormWorldConfig> get, ObjDoubleConsumer<WitherStormWorldConfig> set) {
      KEYS.put(name, new Key(name, description, min, max, integer, WitherStormWorldConfig.Widget.SLIDER, (String[])null, get, set));
   }

   private static void keyToggle(String name, String description, ToDoubleFunction<WitherStormWorldConfig> get, ObjDoubleConsumer<WitherStormWorldConfig> set) {
      KEYS.put(name, new Key(name, description, (double)0.0F, (double)1.0F, true, WitherStormWorldConfig.Widget.TOGGLE, (String[])null, get, set));
   }

   private static void keyCycle(String name, String description, String[] labels, ToDoubleFunction<WitherStormWorldConfig> get, ObjDoubleConsumer<WitherStormWorldConfig> set) {
      KEYS.put(name, new Key(name, description, (double)0.0F, (double)(labels.length - 1), true, WitherStormWorldConfig.Widget.CYCLE, labels, get, set));
   }

   public double[] toArray() {
      double[] out = new double[KEYS.size()];
      int i = 0;

      for(Key key : KEYS.values()) {
         out[i++] = key.get().applyAsDouble(this);
      }

      return out;
   }

   public void applyArray(double[] v) {
      int i = 0;

      for(Key key : KEYS.values()) {
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
      key("spiralStrength", "How hard clusters spiral around the storm", (double)0.0F, 0.2, false, (c) -> c.spiralStrength, (c, v) -> c.spiralStrength = v);
      key("clusterSpeed", "Max travel speed of debris clusters", (double)0.0F, (double)2.0F, false, (c) -> c.maxClusterSpeed, (c, v) -> c.maxClusterSpeed = v);
      key("phaseRequirementModifier", "Scales how much the storm must eat to grow", 0.1, (double)5.0F, false, (c) -> c.phaseRequirementModifier, (c, v) -> c.phaseRequirementModifier = v);
      key("clusterCooldown", "Ticks between cluster spawns", (double)0.0F, (double)2000.0F, true, (c) -> (double)c.clusterCooldown, (c, v) -> c.clusterCooldown = (int)v);
      key("absorptionRadius", "Distance clusters shrink into the storm", (double)1.0F, (double)32.0F, true, (c) -> (double)c.absorptionRadius, (c, v) -> c.absorptionRadius = (int)v);
      key("pickupRangeModifier", "Multiplies pickup reach (48 blocks at 1.0)", (double)0.5F, (double)5.0F, false, (c) -> c.pickupRangeModifier, (c, v) -> c.pickupRangeModifier = v);

      for(int i = 0; i < CLUSTER_STAGES.length; ++i) {
         ClusterStage stage = CLUSTER_STAGES[i];
         final int idx = i;
         key(stage.key(), "Biggest random cluster radius in " + stage.label(), (double)0.0F, (double)8.0F, true, (c) -> (double)c.clusterStageMax[idx], (c, v) -> c.clusterStageMax[idx] = (int)v);
      }

      key("beamClusterInterval", "Ticks between beam-spawned clusters", (double)20.0F, (double)400.0F, true, (c) -> (double)c.beamClusterInterval, (c, v) -> c.beamClusterInterval = (int)v);
      key("beamGroundRadius", "Tractor beam ground circle radius", (double)1.0F, (double)8.0F, true, (c) -> (double)c.beamGroundRadius, (c, v) -> c.beamGroundRadius = (int)v);
      keyToggle("beamShutoff", "Heads may turn their beams off at will (mood flicker)", (c) -> (double)c.beamShutoff, (c, v) -> c.beamShutoff = (int)v);
      key("headFireInterval", "Base ticks between head shots", (double)20.0F, (double)600.0F, true, (c) -> (double)c.headFireInterval, (c, v) -> c.headFireInterval = (int)v);
      key("headTargetRange", "How far heads look for targets", (double)16.0F, (double)256.0F, false, (c) -> c.headTargetRange, (c, v) -> c.headTargetRange = v);
      key("phase4Requirement", "Growth needed per step in phase 4 (higher = phase 4 lasts far longer)", (double)200.0F, (double)30000.0F, true, (c) -> (double)c.phase4Requirement, (c, v) -> c.phase4Requirement = (int)v);
      key("phase5Requirement", "Growth needed per step in phase 5+ (higher = phase 5 lasts far longer)", (double)200.0F, (double)60000.0F, true, (c) -> (double)c.phase5Requirement, (c, v) -> c.phase5Requirement = (int)v);
      key("phase4TurnSpeed", "How fast the body turns in phase 4 (1.0 = normal)", 0.1, (double)3.0F, false, (c) -> c.phase4TurnSpeed, (c, v) -> c.phase4TurnSpeed = v);
      key("phase5TurnSpeed", "How fast the body turns in phase 5+ (1.0 = phase-4 speed)", 0.1, (double)3.0F, false, (c) -> c.phase5TurnSpeed, (c, v) -> c.phase5TurnSpeed = v);
      key("phase58TurnSpeed", "How fast the body turns in late phase 5 (5.8+)", 0.05, (double)3.0F, false, (c) -> c.phase58TurnSpeed, (c, v) -> c.phase58TurnSpeed = v);
      key("phase58DriftStrength", "How much the late phase-5 body wanders on its windy drift", (double)0.0F, (double)4.0F, false, (c) -> c.phase58DriftStrength, (c, v) -> c.phase58DriftStrength = v);
      key("stormSpeed", "Phase-4 fly speed", 0.02, (double)0.5F, false, (c) -> c.stormSpeed, (c, v) -> c.stormSpeed = v);
      key("stormStandoff", "Preferred distance from its target", (double)10.0F, (double)200.0F, false, (c) -> c.stormStandoff, (c, v) -> c.stormStandoff = v);
      key("cruiseAltitude", "Height above ground it tries to hold", (double)10.0F, (double)120.0F, true, (c) -> (double)c.phase4Altitude, (c, v) -> c.phase4Altitude = (int)v);
      key("recoilStrength", "Head-fire body jolt strength", (double)0.0F, (double)6.0F, false, (c) -> c.recoilStrength, (c, v) -> c.recoilStrength = v);
      key("spawnFreezeSeconds", "Seconds the storm sits frozen after spawning before it moves or eats", (double)0.0F, (double)120.0F, true, (c) -> (double)c.spawnFreezeSeconds, (c, v) -> c.spawnFreezeSeconds = (int)v);
      key("chaseSpeed", "Fly speed while actively chasing a player", 0.1, (double)3.0F, false, (c) -> c.chaseSpeed, (c, v) -> c.chaseSpeed = v);
      key("chaseInterval", "Minutes between automatic chases (phase 4+)", (double)5.0F, (double)720.0F, true, (c) -> (double)c.chaseIntervalMinutes, (c, v) -> c.chaseIntervalMinutes = (int)v);
      key("distractionInterval", "Minutes of chasing before it can get distracted", (double)1.0F, (double)240.0F, true, (c) -> (double)c.distractionIntervalMinutes, (c, v) -> c.distractionIntervalMinutes = (int)v);
      key("distractionDuration", "Seconds a distraction lasts", (double)10.0F, (double)600.0F, true, (c) -> (double)c.distractionDurationSeconds, (c, v) -> c.distractionDurationSeconds = (int)v);
      key("distractionRange", "How far away the random distraction point lands", (double)32.0F, (double)512.0F, true, (c) -> (double)c.distractionRange, (c, v) -> c.distractionRange = (int)v);
      keyCycle("targetingMode", "How the storm chooses where to go: Ultimate (fixed target), Natural (decides for itself), Nearest player, Group, or Structures (tours built structures and levels them)", TARGETING_LABELS, (c) -> (double)c.targetingMode, (c, v) -> c.targetingMode = (int)v);
      keyToggle("instantGrowth", "The storm grows from every scrap of block it pulls in, charging through the early phases almost instantly", (c) -> (double)c.instantGrowth, (c, v) -> c.instantGrowth = (int)v);
      key("instantGrowthRate", "Instant-growth multiplier applied to every bit of growth the storm eats (higher = it races through phases)", (double)1.0F, (double)100.0F, false, (c) -> c.instantGrowthRate, (c, v) -> c.instantGrowthRate = v);
      keyToggle("infinitePhases", "Experimental: remove the normal 6.99 limit so the storm can keep growing forever. Its art still compresses past the story stages, but the phase number, power and world threat no longer hard-stop.", (c) -> (double)c.infinitePhases, (c, v) -> c.infinitePhases = (int)v);
      keyToggle("infiniteGrowth", "Experimental: past phase 5 the storm also keeps inflating its whole body (heads included) on top of the outward back/cube mass, which now expands unbounded by default. Off by default.", (c) -> (double)c.infiniteGrowth, (c, v) -> c.infiniteGrowth = (int)v);
      keyToggle("infiniteGrowthLocksStage", "When Infinite Growth is on, keep phase 5 storms in phase 5 and phase 6 storms in phase 6 while their back growth, cube cloud and debris rings keep physically expanding. A Formidibomb can still force phase 5 into phase 6.", (c) -> (double)c.infiniteGrowthLocksStage, (c, v) -> c.infiniteGrowthLocksStage = (int)v);
      key("infiniteGrowthSecondsPerPhase", "When either infinite mode is on, how many seconds the storm should take to earn roughly one whole phase of passive growth on its own. With Infinite Growth, that same pace also drives the back and debris-cloud expansion.", (double)5.0F, (double)3600.0F, true, (c) -> (double)c.infiniteGrowthSecondsPerPhase, (c, v) -> c.infiniteGrowthSecondsPerPhase = (int)v);
      key("infiniteGrowthSpeed", "Extra multiplier for late phase-5 / phase-6 outward growth. 1.0 = normal, lower slows the back-mass expansion, higher makes it spread much faster without auto-promoting phase 5 into phase 6.", 0.1, (double)10.0F, false, (c) -> c.infiniteGrowthSpeed, (c, v) -> c.infiniteGrowthSpeed = v);
      key("phaseCeiling", "Optional soft cap for Infinite Phases / Infinite Growth. Set this to 0 for truly uncapped growth; any positive value becomes the virtual phase cap for both numeric and physical expansion.", (double)0.0F, (double)9999.0F, true, (c) -> (double)c.phaseCeiling, (c, v) -> c.phaseCeiling = v);
      keyToggle("tentacleSlam", "The storm hammers its tentacles into the ground, caving in a crater and flinging everything nearby", (c) -> (double)c.tentacleSlam, (c, v) -> c.tentacleSlam = (int)v);
      key("tentacleSlamInterval", "Ticks between tentacle slams", (double)60.0F, (double)1200.0F, true, (c) -> (double)c.tentacleSlamInterval, (c, v) -> c.tentacleSlamInterval = (int)v);
      key("tentacleSlamRadius", "Radius of a tentacle slam crater and its blast", (double)3.0F, (double)24.0F, true, (c) -> (double)c.tentacleSlamRadius, (c, v) -> c.tentacleSlamRadius = (int)v);
      keyToggle("structureRaid", "While the storm is touring built structures (Structures targeting), it actively levels them, caving chunks out of the target as it dwells", (c) -> (double)c.structureRaid, (c, v) -> c.structureRaid = (int)v);
      key("structureRaidInterval", "Ticks between structure-raid caved-ins", (double)60.0F, (double)600.0F, true, (c) -> (double)c.structureRaidInterval, (c, v) -> c.structureRaidInterval = (int)v);
      key("structureRaidRadius", "Radius of each structure-raid caved-in", (double)2.0F, (double)16.0F, true, (c) -> (double)c.structureRaidRadius, (c, v) -> c.structureRaidRadius = (int)v);
      key("structureTearClusters", "Chunks ripped out of a raided structure as flying clusters per raid - the building visibly breaks open and its pieces fly up to the storm", (double)0.0F, (double)8.0F, true, (c) -> (double)c.structureTearClusters, (c, v) -> c.structureTearClusters = (int)v);
      keyToggle("deathBlast", "When the storm is finally destroyed, it detonates in a cataclysmic blast, caving a huge crater and flinging everything around it (the story's final explosion)", (c) -> (double)c.deathBlast, (c, v) -> c.deathBlast = (int)v);
      keyToggle("experimentalBowelsDeath", "Experimental, off by default: if the storm is killed by the post-bowels finale, swap in a white-crack / purple-discharge death with a giant pixel shockwave and land-scouring blowout.", (c) -> (double)c.experimentalBowelsDeath, (c, v) -> c.experimentalBowelsDeath = (int)v);
      key("townNpcPopulation", "Named Story Mode villagers placed when building a town", 0, 20, true, c -> (double)c.townNpcPopulation, (c,v) -> c.townNpcPopulation=(int)v);
      key("townGuardPopulation", "How many iron golem town guards are placed with that town", 0, 6, true, c -> (double)c.townGuardPopulation, (c,v) -> c.townGuardPopulation=(int)v);
      key("townCatPopulation", "How many town cats appear around the roads and market", 0, 8, true, c -> (double)c.townCatPopulation, (c,v) -> c.townCatPopulation=(int)v);
      key("townPigPopulation", "How many story pigs, including Reuben-style mascots, are placed with the town", 0, 4, true, c -> (double)c.townPigPopulation, (c,v) -> c.townPigPopulation=(int)v);
      key("deathBlastRadius", "Radius of the storm's death blast crater and its damage", (double)4.0F, (double)64.0F, true, (c) -> (double)c.deathBlastRadius, (c, v) -> c.deathBlastRadius = (int)v);
      keyToggle("berserk", "When the Devourer is wounded below a health threshold it flies into a rage, slamming its tentacles far more often (the desperate final act)", (c) -> (double)c.berserk, (c, v) -> c.berserk = (int)v);
      key("berserkHealth", "Health fraction (0-1) below which the Devourer goes berserk", 0.1, (double)0.9F, false, (c) -> c.berserkHealth, (c, v) -> c.berserkHealth = v);
      key("berserkSlamInterval", "Tentacle slam interval in ticks once berserk", (double)10.0F, (double)120.0F, true, (c) -> (double)c.berserkSlamInterval, (c, v) -> c.berserkSlamInterval = (int)v);
      keyToggle("netherScale", "A giant tentacle swoops through the Nether at players hiding there (phase 5.1+)", (c) -> (double)c.netherScale, (c, v) -> c.netherScale = (int)v);
      key("netherScaleInterval", "Base seconds a player must linger in the Nether before a scaling can hit", (double)30.0F, (double)3600.0F, true, (c) -> (double)c.netherScaleInterval, (c, v) -> c.netherScaleInterval = (int)v);
      key("netherScaleRandom", "Extra random seconds added on top of the base interval", (double)0.0F, (double)600.0F, true, (c) -> (double)c.netherScaleRandom, (c, v) -> c.netherScaleRandom = (int)v);
      key("worldDarkening", "How far a storm may darken the world's lighting on this server, as a percent. Multiplies each player's own Darken World Lighting setting.", (double)0.0F, (double)100.0F, true, (c) -> (double)c.worldDarkening, (c, v) -> c.worldDarkening = (int)v);
      keyToggle("postFormidibombChase", "After a Formidibomb the storm gets up and comes straight for the nearest player instead of going back to what it was doing", (c) -> (double)c.postFormidibombChase, (c, v) -> c.postFormidibombChase = (int)v);
      key("postFormidibombChaseSpeed", "How fast that chase is, as a percent of the normal chase speed. Over 100 is faster than it normally hunts.", (double)25.0F, (double)300.0F, true, (c) -> (double)c.postFormidibombChaseSpeed, (c, v) -> c.postFormidibombChaseSpeed = (int)v);
      keyToggle("fastGrowthToSixOne", "Hurry the Devourer from phase 6 to 6.1, where its second head comes in, then return to normal growth", (c) -> (double)c.fastGrowthToSixOne, (c, v) -> c.fastGrowthToSixOne = (int)v);
      key("fastGrowthToSixOneSpeed", "How much faster, as a percent", (double)100.0F, (double)500.0F, true, (c) -> (double)c.fastGrowthToSixOneSpeed, (c, v) -> c.fastGrowthToSixOneSpeed = (int)v);
      keyToggle("endermanSiege", "At phase 6.1, once the Devourer has grown its second head, endermen gather in front of it and it turns on them", (c) -> (double)c.endermanSiege, (c, v) -> c.endermanSiege = (int)v);
      key("endermanSiegeCount", "How many gather", (double)0.0F, (double)80.0F, true, (c) -> (double)c.endermanSiegeCount, (c, v) -> c.endermanSiegeCount = (int)v);
      key("endermanSiegeSeconds", "How long the siege runs, in seconds", (double)10.0F, (double)600.0F, true, (c) -> (double)c.endermanSiegeSeconds, (c, v) -> c.endermanSiegeSeconds = (int)v);
      key("endermanSiegeDistance", "How far in front of the storm they appear, in blocks", (double)10.0F, (double)120.0F, true, (c) -> (double)c.endermanSiegeDistance, (c, v) -> c.endermanSiegeDistance = (int)v);
      key("endermanSiegeSlowdown", "What the storm's speed drops to while they are on it, as a percent", (double)10.0F, (double)100.0F, true, (c) -> (double)c.endermanSiegeSlowdown, (c, v) -> c.endermanSiegeSlowdown = (int)v);
      key("endermanSiegeTentacleSpeed", "How much faster its tentacles thrash during it, as a percent", (double)100.0F, (double)600.0F, true, (c) -> (double)c.endermanSiegeTentacleSpeed, (c, v) -> c.endermanSiegeTentacleSpeed = (int)v);
      keyToggle("endermanSiegeBeamEats", "An enderman caught in a tractor beam is simply gone", (c) -> (double)c.endermanSiegeBeamEats, (c, v) -> c.endermanSiegeBeamEats = (int)v);
      keyToggle("caveRumble", "Cave Rumble: while the storm is overhead and you are underground, the ceiling shakes, blocks drop out of it, and dripstone lets go", (c) -> (double)c.caveRumble, (c, v) -> c.caveRumble = (int)v);
      key("caveRumbleInterval", "Base seconds between cave rumbles (a random half again is added on top, so they never fall into a rhythm)", (double)10.0F, (double)900.0F, true, (c) -> (double)c.caveRumbleInterval, (c, v) -> c.caveRumbleInterval = (int)v);
      key("caveRumbleDuration", "How many seconds a cave rumble lasts", (double)1.0F, (double)60.0F, true, (c) -> (double)c.caveRumbleDuration, (c, v) -> c.caveRumbleDuration = (int)v);
      key("caveRumbleIntensity", "How violent a cave rumble is: the screen shake, how much of the ceiling comes down, and how loud it is", 0.1, (double)3.0F, false, (c) -> c.caveRumbleIntensity, (c, v) -> c.caveRumbleIntensity = v);
      keyToggle("voidCorruption", "Late-phase terrain corruption: the storm starts leaving withered flesh, stone, sand and wood scars across the world beneath it.", (c) -> (double)c.voidCorruption, (c, v) -> c.voidCorruption = (int)v);
      key("voidCorruptionInterval", "Ticks between terrain-corruption passes", (double)5.0F, (double)600.0F, true, (c) -> (double)c.voidCorruptionInterval, (c, v) -> c.voidCorruptionInterval = (int)v);
      key("voidCorruptionRadius", "How far from the storm those corruption passes can land, in blocks", (double)8.0F, (double)128.0F, true, (c) -> (double)c.voidCorruptionRadius, (c, v) -> c.voidCorruptionRadius = (int)v);
      key("voidCorruptionBursts", "How many random surface spots are corrupted each pass", (double)1.0F, (double)64.0F, true, (c) -> (double)c.voidCorruptionBursts, (c, v) -> c.voidCorruptionBursts = (int)v);
      keyToggle("voidCorruptionGrass", "Optional expansion, off by default: scar surviving grass and ground cover with withered dust, mushrooms and wider rotten surface patches so the corruption reads more like infected land.", (c) -> (double)c.voidCorruptionGrass, (c, v) -> c.voidCorruptionGrass = (int)v);
      keyToggle("voidCorruptionTrees", "Optional expansion, off by default: blacken nearby trees by stripping leaves and converting trunks / branches into withered logs around corruption strikes.", (c) -> (double)c.voidCorruptionTrees, (c, v) -> c.voidCorruptionTrees = (int)v);
      keyToggle("mobsFlee", "Nearby mobs panic and run away from the storm", (c) -> (double)c.mobsFlee, (c, v) -> c.mobsFlee = (int)v);
      key("headForgiveSeconds", "Breathing-room seconds after a head lets a player go", (double)0.0F, (double)600.0F, true, (c) -> (double)c.headForgiveSeconds, (c, v) -> c.headForgiveSeconds = (int)v);
      keyToggle("mobPickup", "Beams pull in and consume non-target mobs", (c) -> (double)c.mobPickup, (c, v) -> c.mobPickup = (int)v);
      keyToggle("castThroughWater", "Beams and clusters reach through water to the seabed", (c) -> (double)c.castThroughWater, (c, v) -> c.castThroughWater = (int)v);
      keyToggle("clustersTakeLiquids", "Clusters carry water and lava away too", (c) -> (double)c.clustersTakeLiquids, (c, v) -> c.clustersTakeLiquids = (int)v);
      keyToggle("severedScavenge", "Severed halves pull up small block clusters of their own. Never counts toward the storm's growth.", (c) -> (double)c.severedScavenge, (c, v) -> c.severedScavenge = (int)v);
      key("severedScavengeInterval", "Seconds between a severed half's scavenging attempts", (double)2.0F, (double)120.0F, false, (c) -> c.severedScavengeInterval, (c, v) -> c.severedScavengeInterval = v);
      keyToggle("orbitStationaryTargets", "Circle a target that stays in one place", (c) -> (double)c.orbitStationaryTargets, (c, v) -> c.orbitStationaryTargets = (int)v);
      key("roarRange", "How far the storm's roars carry, in blocks. Bites and snarls reach three quarters as far, big roars a quarter further.", (double)64.0F, (double)640.0F, false, (c) -> c.roarRange, (c, v) -> c.roarRange = v);
      key("beamSoundRange", "How far the tractor beam's switch-on and switch-off carry, in blocks", (double)32.0F, (double)480.0F, false, (c) -> c.beamSoundRange, (c, v) -> c.beamSoundRange = v);
      key("beamImpactLight", "How brightly a beam lights the ground it lands on (0 = off)", (double)0.0F, (double)15.0F, true, (c) -> (double)c.beamImpactLight, (c, v) -> c.beamImpactLight = (int)v);
      keyToggle("tentacleAwareness", "A body tentacle reaches for players caught in a beam", (c) -> (double)c.tentacleAwareness, (c, v) -> c.tentacleAwareness = (int)v);
      keyToggle("witherSickness", "Mobs near the storm too long get corrupted", (c) -> (double)c.witherSickness, (c, v) -> c.witherSickness = (int)v);
      keyToggle("witheredMobs", "Fully corrupted mobs turn Withered and gain manipulation powers", (c) -> (double)c.witheredMobs, (c, v) -> c.witheredMobs = (int)v);
      key("witheredMax", "How many Withered mobs may exist at once (higher = busier and heavier)", (double)1.0F, (double)64.0F, true, (c) -> (double)c.witheredMax, (c, v) -> c.witheredMax = (int)v);
      key("witheredMaxCaves", "Of those, how many may have turned underground (kept low on purpose)", (double)0.0F, (double)32.0F, true, (c) -> (double)c.witheredMaxCaves, (c, v) -> c.witheredMaxCaves = (int)v);
      CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).xmap((map) -> {
         WitherStormWorldConfig cfg = new WitherStormWorldConfig();
         map.forEach((name, value) -> {
            Key key = (Key)KEYS.get(name);
            if (key != null) {
               key.set().accept(cfg, key.clamp(value));
            }

         });
         return cfg;
      }, (cfg) -> {
         Map<String, Double> map = new LinkedHashMap();

         for(Key key : KEYS.values()) {
            map.put(key.name(), key.get().applyAsDouble(cfg));
         }

         return map;
      });
   }

   public static record ClusterStage(String key, double minPhase, String label, int defaultMax) {
   }

   public static enum Widget {
      SLIDER,
      TOGGLE,
      CYCLE;

      // $FF: synthetic method
      private static Widget[] $values() {
         return new Widget[]{SLIDER, TOGGLE, CYCLE};
      }
   }

   public static record Key(String name, String description, double min, double max, boolean integer, Widget widget, String[] cycleLabels, ToDoubleFunction<WitherStormWorldConfig> get, ObjDoubleConsumer<WitherStormWorldConfig> set) {
      public double clamp(double v) {
         double clamped = Math.max(this.min, Math.min(this.max, v));
         return this.integer ? (double)Math.round(clamped) : clamped;
      }
   }
}
