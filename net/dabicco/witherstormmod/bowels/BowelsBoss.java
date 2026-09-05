package net.dabicco.witherstormmod.bowels;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
   private static final double WAVE_RING = (double)15.0F;
   private static final double WAVE_SPREAD = (double)3.5F;
   private static final EntityType<?>[] WAVE_KINDS;
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
      double r = (double)26.0F;
      return new AABB((double)177.0F - r, (double)56.0F, (double)0.0F - r, (double)177.0F + r, (double)100.0F, (double)0.0F + r);
   }

   private static AABB holeField() {
      double r = (double)48.0F;
      return new AABB((double)177.0F - r, (double)52.0F, (double)0.0F - r, (double)177.0F + r, (double)100.0F, (double)0.0F + r);
   }

   public static void tick(ServerLevel level) {
      if (!level.players().isEmpty()) {
         Vec3 dais = BowelsEndRoom.daisTop();
         boolean anyoneClose = false;

         for(ServerPlayer player : level.players()) {
            if (player.distanceToSqr(dais.x, dais.y, dais.z) < (double)9216.0F) {
               anyoneClose = true;
               break;
            }
         }

         if (anyoneClose) {
            if (level.isLoaded(BlockPos.containing(dais))) {
               List<BowelsHeartEntity> hearts = level.getEntitiesOfClass(BowelsHeartEntity.class, arena());
               if (hearts.isEmpty()) {
                  if (!isBeaten(level)) {
                     spawnHeart(level);
                  }
               } else {
                  for(int i = 1; i < hearts.size(); ++i) {
                     ((BowelsHeartEntity)hearts.get(i)).discard();
                  }

                  BowelsHeartEntity heart = (BowelsHeartEntity)hearts.get(0);
                  if (heart.isFighting()) {
                     if (heart.getFightTicks() >= 81) {
                        beginRise(level, heart);
                     }

                     if (heart.getWaveOwed() > 0 && roomSettled(level, heart)) {
                        spawnWaveMob(level, heart);
                     }

                     openTheMaws(level, heart);
                     if (heart.getFightTicks() >= 36) {
                        if (heart.getFightTicks() >= heart.getWallSpawnAt()) {
                           heart.setWallSpawnAt(heart.getFightTicks() + 60);
                           long live = level.getEntitiesOfClass(BowelsTentacleEntity.class, holeField()).stream().filter((t) -> !t.isGuard()).count();
                           if (live < 7L) {
                              spawnWallLimb(level);
                           }
                        }
                     }
                  } else {
                     Vec3 want = BowelsEndRoom.daisTop();
                     if (heart.position().distanceToSqr(want) > 1.0E-4) {
                        heart.setPos(want.x, want.y, want.z);

                        for(BowelsTentacleEntity limb : level.getEntitiesOfClass(BowelsTentacleEntity.class, arena(), BowelsTentacleEntity::isGuard)) {
                           limb.discard();
                        }
                     }

                     topUpGuards(level, heart);
                  }
               }
            }
         }
      }
   }

   private static boolean roomSettled(ServerLevel level, BowelsHeartEntity heart) {
      if (!heart.hasRisen()) {
         return false;
      } else {
         return level.getGameTime() < rollSettlesAt ? false : level.getEntitiesOfClass(BowelsPedestalEntity.class, arena()).isEmpty();
      }
   }

   public static void onRoomRolled(ServerLevel level, BowelsHeartEntity heart) {
      rollSettlesAt = level.getGameTime() + 98L;
   }

   private static void spawnWaveMob(ServerLevel level, BowelsHeartEntity heart) {
      Vec3 middle = BowelsEndRoom.daisTop();
      int nth = heart.getWaveSpawned();
      double angle = (double)nth * 2.39996 + level.getRandom().nextDouble() * 0.4;
      double at = (double)15.0F + (level.getRandom().nextDouble() - (double)0.5F) * (double)3.5F;
      double x = middle.x + Math.cos(angle) * at;
      double z = middle.z + Math.sin(angle) * at;
      EntityType<?> kind = WAVE_KINDS[level.getRandom().nextInt(WAVE_KINDS.length)];
      Entity mob = kind.create(level, EntitySpawnReason.EVENT);
      if (mob instanceof Mob monster) {
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

   private static void openTheMaws(ServerLevel level, BowelsHeartEntity heart) {
      if (BowelsFlip.flipped()) {
         if (!BowelsFlip.rolling(level.getGameTime())) {
            if (waveCleared(level, heart)) {
               List<BowelsMawEntity> up = level.getEntitiesOfClass(BowelsMawEntity.class, arena());
               if (up.size() < 2) {
                  boolean[] taken = new boolean[2];

                  for(BowelsMawEntity maw : up) {
                     int side = maw.getSide();
                     if (side >= 0 && side < 2) {
                        taken[side] = true;
                     }
                  }

                  for(int side = 0; side < 2; ++side) {
                     if (!taken[side]) {
                        BowelsMawEntity maw = (BowelsMawEntity)ModBowelsEntities.MAW.create(level, EntitySpawnReason.EVENT);
                        if (maw != null) {
                           maw.placeAt(side);
                           level.addFreshEntity(maw);
                           level.playSound((Entity)null, maw.getX(), maw.getY(), maw.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0F, 0.55F);
                           return;
                        }
                     }
                  }

               }
            }
         }
      }
   }

   public static int guardStance(ServerLevel level, BowelsHeartEntity heart) {
      return heart.isFighting() ? 1 : 0;
   }

   public static void onHitSurvived(ServerLevel level, BowelsHeartEntity heart) {
      heart.queueWave(10);

      for(ServerPlayer player : level.players()) {
         ServerPlayNetworking.send(player, new CaveRumblePayload(50, 0.7F));
      }

   }

   public static double waveAlive(ServerLevel level, BowelsHeartEntity heart) {
      if (heart.getWaveSpawned() <= 0) {
         return (double)0.0F;
      } else {
         long alive = (long)level.getEntitiesOfClass(LivingEntity.class, arena(), (e) -> e.isAlive() && WitheredMobs.isWithered(e)).size();
         return (double)alive / (double)heart.getWaveSpawned();
      }
   }

   public static boolean waveCleared(ServerLevel level, BowelsHeartEntity heart) {
      if (heart.getWaveOwed() > 0) {
         return false;
      } else {
         return waveAlive(level, heart) <= 0.09999999999999998;
      }
   }

   private static boolean isBeaten(ServerLevel level) {
      MinecraftServer server = level.getServer();
      return server != null && Files.exists(server.getWorldPath(LevelResource.ROOT).resolve("dabywsmod_bowels_heart.beaten"), new LinkOption[0]);
   }

   private static void markBeaten(ServerLevel level) {
      MinecraftServer server = level.getServer();
      if (server != null) {
         try {
            Files.writeString(server.getWorldPath(LevelResource.ROOT).resolve("dabywsmod_bowels_heart.beaten"), "1");
         } catch (IOException e) {
            DabyWitherStormMod.LOGGER.warn("[bowels] couldn't record the fight as won", e);
         }

      }
   }

   private static void spawnHeart(ServerLevel level) {
      Vec3 top = BowelsEndRoom.daisTop();
      BowelsHeartEntity heart = (BowelsHeartEntity)ModBowelsEntities.HEART.create(level, EntitySpawnReason.TRIGGERED);
      if (heart != null) {
         heart.setPos(top.x, top.y, top.z);
         level.addFreshEntity(heart);
         DabyWitherStormMod.LOGGER.info("[bowels] the heart is on its pedestal at {}", top);
         topUpGuards(level, heart);
      }
   }

   private static void topUpGuards(ServerLevel level, BowelsHeartEntity heart) {
      List<BowelsTentacleEntity> present = level.getEntitiesOfClass(BowelsTentacleEntity.class, heart.getBoundingBox().inflate((double)16.0F), (t) -> t.getMode() == 0);
      boolean stale = present.size() < 6;
      if (!stale) {
         Set<Integer> seen = new HashSet();

         for(BowelsTentacleEntity g : present) {
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
         for(BowelsTentacleEntity old : present) {
            old.discard();
         }

         for(int side = 0; side < 2; ++side) {
            for(int i = 0; i < 3; ++i) {
               double sign = side == 0 ? (double)1.0F : (double)-1.0F;
               double alongApproach = (double)(i - 1) * 1.9 + (side == 0 ? (double)0.0F : 0.95) - 0.475;
               BowelsTentacleEntity limb = (BowelsTentacleEntity)ModBowelsEntities.TENTACLE.create(level, EntitySpawnReason.TRIGGERED);
               if (limb != null) {
                  limb.setPos(heart.getX() + alongApproach, BowelsEndRoom.guardMountY(), heart.getZ() + sign * 2.58);
                  limb.setMountYaw(sign > (double)0.0F ? 180.0F : 0.0F);
                  limb.setOrder(i * 2 + side);
                  limb.setOnEnd(i == 0 || i == 2);
                  limb.setPhase((new float[]{0.0F, 2.31F, 0.83F, 3.77F, 1.62F, 4.94F})[i * 2 + side]);
                  limb.setScale(1.925F);
                  limb.setCurl(1.0F);
                  limb.setMode(0);
                  limb.setBones(BowelsTentacleShape.BONES);
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

      for(ServerPlayer player : level.players()) {
         if (!player.isSpectator() && !player.isCreative()) {
            double d = player.distanceToSqr(dais.x, dais.y, dais.z);
            if (d < best) {
               best = d;
               near = player;
            }
         }
      }

      boolean[] taken = new boolean[12];

      for(BowelsTentacleEntity other : level.getEntitiesOfClass(BowelsTentacleEntity.class, holeField(), (t) -> !t.isGuard())) {
         int idx = other.getHoleIndex();
         if (idx >= 0 && idx < taken.length) {
            taken[idx] = true;
         }
      }

      int hole = -1;
      double bestDist = Double.MAX_VALUE;

      for(int i = 0; i < 12; ++i) {
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
         BowelsTentacleEntity limb = (BowelsTentacleEntity)ModBowelsEntities.TENTACLE.create(level, EntitySpawnReason.TRIGGERED);
         if (limb != null) {
            limb.setMountYaw(BowelsEndRoom.holeYaw(hole));
            limb.setPhase(level.getRandom().nextFloat() * 6.28F);
            limb.setCurl(0.0F);
            limb.setBones(BowelsTentacleShape.BONES);
            limb.setScale(1.9F + level.getRandom().nextFloat() * 0.5F);
            limb.setHole(hole, mouth, BowelsEndRoom.holeForward(hole));
            limb.setMode(8);
            level.addFreshEntity(limb);
         }
      }
   }

   public static void onFightBegan(ServerLevel level, BowelsHeartEntity heart) {
      rollSettlesAt = 0L;
      List<BowelsTentacleEntity> guards = new ArrayList();

      for(BowelsTentacleEntity limb : level.getEntitiesOfClass(BowelsTentacleEntity.class, arena())) {
         if (limb.getMode() == 0) {
            limb.setMode(1);
            if (limb.isGuard()) {
               guards.add(limb);
            }
         }
      }

      BowelsTentacleEntity chosen = null;
      double best = Double.MAX_VALUE;

      for(ServerPlayer player : level.players()) {
         if (!player.isSpectator() && !player.isCreative() && BowelsEndRoom.onPedestal(player.getX(), player.getY(), player.getZ())) {
            for(BowelsTentacleEntity limb : guards) {
               double d = limb.distanceToSqr(player);
               if (d < best) {
                  best = d;
                  chosen = limb;
               }
            }
         }
      }

      if (chosen != null) {
         chosen.setWhack(true);
      }

   }

   public static void onPedestalCleared(ServerLevel level) {
      for(BowelsHeartEntity heart : level.getEntitiesOfClass(BowelsHeartEntity.class, arena())) {
         if (heart.isFighting()) {
            beginRise(level, heart);
         }
      }

   }

   private static void beginRise(ServerLevel level, BowelsHeartEntity heart) {
      if (!heart.hasRisen()) {
         heart.setRisen();
         BowelsPedestalEntity pedestal = (BowelsPedestalEntity)ModBowelsEntities.PEDESTAL.create(level, EntitySpawnReason.TRIGGERED);
         if (pedestal != null) {
            BlockPos origin = BowelsEndRoom.pedestalOrigin();
            pedestal.setPos((double)origin.getX() + (double)0.5F, (double)origin.getY(), (double)origin.getZ() + (double)0.5F);
            pedestal.setSeed(level.getSeed());
            level.addFreshEntity(pedestal);
            level.playSound((Entity)null, (double)origin.getX(), (double)origin.getY(), (double)origin.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 3.0F, 0.35F);

            for(ServerPlayer player : level.players()) {
               ServerPlayNetworking.send(player, new CaveRumblePayload(110, 1.0F));
            }

         }
      }
   }

   public static void finish(ServerLevel level, BowelsHeartEntity heart) {
      for(BowelsTentacleEntity limb : level.getEntitiesOfClass(BowelsTentacleEntity.class, holeField())) {
         limb.setMode(9);
      }

      for(BowelsMawEntity maw : level.getEntitiesOfClass(BowelsMawEntity.class, arena())) {
         maw.blind();
      }

      level.playSound((Entity)null, heart.getX(), heart.getY(), heart.getZ(), SoundEvents.WARDEN_DEATH, SoundSource.HOSTILE, 3.0F, 0.5F);
      level.sendParticles(ParticleTypes.SONIC_BOOM, heart.getX(), heart.getY() + (double)0.5F, heart.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);

      for(ServerPlayer player : level.players()) {
         ServerPlayNetworking.send(player, new CaveRumblePayload(90, 1.0F));
      }

      BowelsFinale.begin(level, heart.getSlayer());
      markBeaten(level);
      heart.discard();
   }

   static {
      WAVE_KINDS = new EntityType[]{EntityTypes.ZOMBIE, EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.SKELETON, EntityTypes.WITHER_SKELETON, EntityTypes.CREEPER, EntityTypes.SPIDER, EntityTypes.HUSK};
   }
}
