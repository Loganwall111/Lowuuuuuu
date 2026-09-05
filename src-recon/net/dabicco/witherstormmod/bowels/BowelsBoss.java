package net.dabicco.witherstormmod.bowels;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.dabicco.witherstormmod.BowelsEndRoom;
import net.dabicco.witherstormmod.BowelsFlip;
import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.dabicco.witherstormmod.entity.withered.WitheredMobs;
import net.dabicco.witherstormmod.network.CaveRumblePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BowelsBoss {
   public static final int GUARDS = 6;
   private static final int WALL_LIMBS = 7;
   private static final int SPAWN_EVERY = 60;
   private static final int WAVE_SIZE = 10;
   private static final double WAVE_CLEARED = 0.9;
   private static final double WAVE_RING = 15.0;
   private static final double WAVE_SPREAD = 3.5;
   private static final EntityType<?>[] WAVE_KINDS = new EntityType[]{
      EntityTypes.ZOMBIE,
      EntityTypes.ZOMBIE,
      EntityTypes.SKELETON,
      EntityTypes.SKELETON,
      EntityTypes.WITHER_SKELETON,
      EntityTypes.CREEPER,
      EntityTypes.SPIDER,
      EntityTypes.HUSK
   };
   private static long rollSettlesAt;
   private static final int ROLL_SETTLE = 98;
   private static final int MAWS = 2;
   public static final int STANCE_ASLEEP = 0;
   public static final int STANCE_LASH = 1;
   private static final String BEATEN = "dabywsmod_bowels_heart.beaten";
   private static final double GUARD_OUT = 2.58;
   private static final float GUARD_SCALE = 1.925F;
   private static final double GUARD_SPACING = 1.9;
   private static final int RISE_AT = 81;

   private BowelsBoss() {
   }

   private static AABB arena() {
      double r = 26.0;
      return new AABB(177.0 - r, 56.0, 0.0 - r, 177.0 + r, 100.0, 0.0 + r);
   }

   private static AABB holeField() {
      double r = 48.0;
      return new AABB(177.0 - r, 52.0, 0.0 - r, 177.0 + r, 100.0, 0.0 + r);
   }

   public static void tick(ServerLevel level) {
      if (!level.players().isEmpty()) {
         Vec3 dais = BowelsEndRoom.daisTop();
         boolean anyoneClose = false;

         for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(dais.x, dais.y, dais.z) < 9216.0) {
               anyoneClose = true;
               break;
            }
         }

         if (anyoneClose && level.isLoaded(BlockPos.containing(dais))) {
            List<net.dabicco.witherstormmod.bowels.BowelsHeartEntity> hearts = level.getEntitiesOfClass(
               net.dabicco.witherstormmod.bowels.BowelsHeartEntity.class, arena()
            );
            if (hearts.isEmpty()) {
               if (!isBeaten(level)) {
                  spawnHeart(level);
               }
            } else {
               for (int i = 1; i < hearts.size(); i++) {
                  hearts.get(i).discard();
               }

               net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart = hearts.get(0);
               if (heart.isFighting()) {
                  if (heart.getFightTicks() >= 81) {
                     beginRise(level, heart);
                  }

                  if (heart.getWaveOwed() > 0 && roomSettled(level, heart)) {
                     spawnWaveMob(level, heart);
                  }

                  openTheMaws(level, heart);
                  if (heart.getFightTicks() >= 36 && heart.getFightTicks() >= heart.getWallSpawnAt()) {
                     heart.setWallSpawnAt(heart.getFightTicks() + 60);
                     long live = level.getEntitiesOfClass(net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, holeField())
                        .stream()
                        .filter(t -> !t.isGuard())
                        .count();
                     if (live < 7L) {
                        spawnWallLimb(level);
                     }
                  }
               } else {
                  Vec3 want = BowelsEndRoom.daisTop();
                  if (heart.position().distanceToSqr(want) > 1.0E-4) {
                     heart.setPos(want.x, want.y, want.z);

                     for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limb : level.getEntitiesOfClass(
                        net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, arena(), net.dabicco.witherstormmod.bowels.BowelsTentacleEntity::isGuard
                     )) {
                        limb.discard();
                     }
                  }

                  topUpGuards(level, heart);
               }
            }
         }
      }
   }

   private static boolean roomSettled(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      if (!heart.hasRisen()) {
         return false;
      } else {
         return level.getGameTime() < rollSettlesAt
            ? false
            : level.getEntitiesOfClass(net.dabicco.witherstormmod.bowels.BowelsPedestalEntity.class, arena()).isEmpty();
      }
   }

   public static void onRoomRolled(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      rollSettlesAt = level.getGameTime() + 98L;
   }

   private static void spawnWaveMob(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      Vec3 middle = BowelsEndRoom.daisTop();
      int nth = heart.getWaveSpawned();
      double angle = nth * 2.39996 + level.getRandom().nextDouble() * 0.4;
      double at = 15.0 + (level.getRandom().nextDouble() - 0.5) * 3.5;
      double x = middle.x + Math.cos(angle) * at;
      double z = middle.z + Math.sin(angle) * at;
      EntityType<?> kind = WAVE_KINDS[level.getRandom().nextInt(WAVE_KINDS.length)];
      if (kind.create(level, EntitySpawnReason.EVENT) instanceof Mob monster) {
         monster.setPos(x, BowelsEndRoom.standAt(level.getSeed(), x, z), z);
         monster.setYRot((float)Math.toDegrees(angle) + 180.0F);
         monster.finalizeSpawn(level, level.getCurrentDifficultyAt(monster.blockPosition()), EntitySpawnReason.EVENT, (SpawnGroupData)null);
         monster.setPersistenceRequired();
         level.addFreshEntity(monster);
         if (WitheredMobs.turn(level, monster)) {
            heart.oneMoreOut();
         } else {
            monster.discard();
         }

         level.playSound((Entity)null, x, middle.y, z, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5F, 1.5F);
      }
   }

   private static void openTheMaws(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      if (BowelsFlip.flipped() && !BowelsFlip.rolling(level.getGameTime()) && waveCleared(level, heart)) {
         List<net.dabicco.witherstormmod.bowels.BowelsMawEntity> up = level.getEntitiesOfClass(net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, arena());
         if (up.size() < 2) {
            boolean[] taken = new boolean[2];

            for (net.dabicco.witherstormmod.bowels.BowelsMawEntity maw : up) {
               int side = maw.getSide();
               if (side >= 0 && side < 2) {
                  taken[side] = true;
               }
            }

            for (int side = 0; side < 2; side++) {
               if (!taken[side]) {
                  net.dabicco.witherstormmod.bowels.BowelsMawEntity mawx = (net.dabicco.witherstormmod.bowels.BowelsMawEntity)net.dabicco.witherstormmod.bowels.ModBowelsEntities.MAW
                     .create(level, EntitySpawnReason.EVENT);
                  if (mawx != null) {
                     mawx.placeAt(side);
                     level.addFreshEntity(mawx);
                     level.playSound((Entity)null, mawx.getX(), mawx.getY(), mawx.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0F, 0.55F);
                     return;
                  }
               }
            }
         }
      }
   }

   public static int guardStance(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      return heart.isFighting() ? 1 : 0;
   }

   public static void onHitSurvived(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      heart.queueWave(10);

      for (ServerPlayer player : level.players()) {
         ServerPlayNetworking.send(player, new CaveRumblePayload(50, 0.7F));
      }
   }

   public static double waveAlive(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      if (heart.getWaveSpawned() <= 0) {
         return 0.0;
      } else {
         long alive = level.getEntitiesOfClass(LivingEntity.class, arena(), e -> e.isAlive() && WitheredMobs.isWithered(e)).size();
         return (double)alive / heart.getWaveSpawned();
      }
   }

   public static boolean waveCleared(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      return heart.getWaveOwed() > 0 ? false : waveAlive(level, heart) <= 0.09999999999999998;
   }

   private static boolean isBeaten(ServerLevel level) {
      MinecraftServer server = level.getServer();
      return server != null && Files.exists(server.getWorldPath(LevelResource.ROOT).resolve("dabywsmod_bowels_heart.beaten"));
   }

   private static void markBeaten(ServerLevel level) {
      MinecraftServer server = level.getServer();
      if (server != null) {
         try {
            Files.writeString(server.getWorldPath(LevelResource.ROOT).resolve("dabywsmod_bowels_heart.beaten"), "1");
         } catch (IOException var3) {
            DabyWitherStormMod.LOGGER.warn("[bowels] couldn't record the fight as won", var3);
         }
      }
   }

   private static void spawnHeart(ServerLevel level) {
      Vec3 top = BowelsEndRoom.daisTop();
      net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart = (net.dabicco.witherstormmod.bowels.BowelsHeartEntity)net.dabicco.witherstormmod.bowels.ModBowelsEntities.HEART
         .create(level, EntitySpawnReason.TRIGGERED);
      if (heart != null) {
         heart.setPos(top.x, top.y, top.z);
         level.addFreshEntity(heart);
         DabyWitherStormMod.LOGGER.info("[bowels] the heart is on its pedestal at {}", top);
         topUpGuards(level, heart);
      }
   }

   private static void topUpGuards(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      List<net.dabicco.witherstormmod.bowels.BowelsTentacleEntity> present = level.getEntitiesOfClass(
         net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, heart.getBoundingBox().inflate(16.0), t -> t.getMode() == 0
      );
      boolean stale = present.size() < 6;
      if (!stale) {
         Set<Integer> seen = new HashSet<>();

         for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity g : present) {
            if (!seen.add(g.getOrder())) {
               stale = true;
               break;
            }

            if (Math.abs(g.getScale() - 1.925F) > 0.01F) {
               stale = true;
               break;
            }
         }
      }

      if (stale) {
         for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity old : present) {
            old.discard();
         }

         for (int side = 0; side < 2; side++) {
            for (int i = 0; i < 3; i++) {
               double sign = side == 0 ? 1.0 : -1.0;
               double alongApproach = (i - 1) * 1.9 + (side == 0 ? 0.0 : 0.95) - 0.475;
               net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limb = (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity)net.dabicco.witherstormmod.bowels.ModBowelsEntities.TENTACLE
                  .create(level, EntitySpawnReason.TRIGGERED);
               if (limb != null) {
                  limb.setPos(heart.getX() + alongApproach, BowelsEndRoom.guardMountY(), heart.getZ() + sign * 2.58);
                  limb.setMountYaw(sign > 0.0 ? 180.0F : 0.0F);
                  limb.setOrder(i * 2 + side);
                  limb.setOnEnd(i == 0 || i == 2);
                  limb.setPhase(new float[]{0.0F, 2.31F, 0.83F, 3.77F, 1.62F, 4.94F}[i * 2 + side]);
                  limb.setScale(1.925F);
                  limb.setCurl(1.0F);
                  limb.setMode(0);
                  limb.setBones(net.dabicco.witherstormmod.bowels.BowelsTentacleShape.BONES);
                  limb.setGuard(true);
                  level.addFreshEntity(limb);
               }
            }
         }
      }
   }

   private static void spawnWallLimb(ServerLevel level) {
      long seed = level.getSeed();
      ServerPlayer near = null;
      double best = Double.MAX_VALUE;
      Vec3 dais = BowelsEndRoom.daisTop();

      for (ServerPlayer player : level.players()) {
         if (!player.isSpectator() && !player.isCreative()) {
            double d = player.distanceToSqr(dais.x, dais.y, dais.z);
            if (d < best) {
               best = d;
               near = player;
            }
         }
      }

      boolean[] taken = new boolean[12];

      for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity other : level.getEntitiesOfClass(
         net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, holeField(), t -> !t.isGuard()
      )) {
         int idx = other.getHoleIndex();
         if (idx >= 0 && idx < taken.length) {
            taken[idx] = true;
         }
      }

      int hole = -1;
      double bestDist = Double.MAX_VALUE;

      for (int i = 0; i < 12; i++) {
         if (!taken[i]) {
            if (near == null) {
               if (hole < 0 || level.getRandom().nextInt(3) == 0) {
                  hole = i;
               }
            } else {
               Vec3 at = BowelsEndRoom.holeMouth(seed, i);
               double d = near.distanceToSqr(at.x, at.y, at.z);
               if (d < bestDist) {
                  bestDist = d;
                  hole = i;
               }
            }
         }
      }

      if (hole >= 0) {
         Vec3 mouth = BowelsEndRoom.holeMouth(seed, hole);
         net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limb = (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity)net.dabicco.witherstormmod.bowels.ModBowelsEntities.TENTACLE
            .create(level, EntitySpawnReason.TRIGGERED);
         if (limb != null) {
            limb.setMountYaw(BowelsEndRoom.holeYaw(hole));
            limb.setPhase(level.getRandom().nextFloat() * 6.28F);
            limb.setCurl(0.0F);
            limb.setBones(net.dabicco.witherstormmod.bowels.BowelsTentacleShape.BONES);
            limb.setScale(1.9F + level.getRandom().nextFloat() * 0.5F);
            limb.setHole(hole, mouth, BowelsEndRoom.holeForward(hole));
            limb.setMode(8);
            level.addFreshEntity(limb);
         }
      }
   }

   public static void onFightBegan(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      rollSettlesAt = 0L;
      List<net.dabicco.witherstormmod.bowels.BowelsTentacleEntity> guards = new ArrayList<>();

      for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limb : level.getEntitiesOfClass(
         net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, arena()
      )) {
         if (limb.getMode() == 0) {
            limb.setMode(1);
            if (limb.isGuard()) {
               guards.add(limb);
            }
         }
      }

      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity chosen = null;
      double best = Double.MAX_VALUE;

      for (ServerPlayer player : level.players()) {
         if (!player.isSpectator() && !player.isCreative() && BowelsEndRoom.onPedestal(player.getX(), player.getY(), player.getZ())) {
            for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limbx : guards) {
               double d = limbx.distanceToSqr(player);
               if (d < best) {
                  best = d;
                  chosen = limbx;
               }
            }
         }
      }

      if (chosen != null) {
         chosen.setWhack(true);
      }
   }

   public static void onPedestalCleared(ServerLevel level) {
      for (net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart : level.getEntitiesOfClass(
         net.dabicco.witherstormmod.bowels.BowelsHeartEntity.class, arena()
      )) {
         if (heart.isFighting()) {
            beginRise(level, heart);
         }
      }
   }

   private static void beginRise(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      if (!heart.hasRisen()) {
         heart.setRisen();
         net.dabicco.witherstormmod.bowels.BowelsPedestalEntity pedestal = (net.dabicco.witherstormmod.bowels.BowelsPedestalEntity)net.dabicco.witherstormmod.bowels.ModBowelsEntities.PEDESTAL
            .create(level, EntitySpawnReason.TRIGGERED);
         if (pedestal != null) {
            BlockPos origin = BowelsEndRoom.pedestalOrigin();
            pedestal.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
            pedestal.setSeed(level.getSeed());
            level.addFreshEntity(pedestal);
            level.playSound((Entity)null, origin.getX(), origin.getY(), origin.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 3.0F, 0.35F);

            for (ServerPlayer player : level.players()) {
               ServerPlayNetworking.send(player, new CaveRumblePayload(110, 1.0F));
            }
         }
      }
   }

   public static void finish(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart) {
      for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limb : level.getEntitiesOfClass(
         net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, holeField()
      )) {
         limb.setMode(9);
      }

      for (net.dabicco.witherstormmod.bowels.BowelsMawEntity maw : level.getEntitiesOfClass(net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, arena())) {
         maw.blind();
      }

      level.playSound((Entity)null, heart.getX(), heart.getY(), heart.getZ(), SoundEvents.WARDEN_DEATH, SoundSource.HOSTILE, 3.0F, 0.5F);
      level.sendParticles(ParticleTypes.SONIC_BOOM, heart.getX(), heart.getY() + 0.5, heart.getZ(), 1, 0.0, 0.0, 0.0, 0.0);

      for (ServerPlayer player : level.players()) {
         ServerPlayNetworking.send(player, new CaveRumblePayload(90, 1.0F));
      }

      net.dabicco.witherstormmod.bowels.BowelsFinale.begin(level, heart.getSlayer());
      markBeaten(level);
      heart.discard();
   }
}
