package net.dabicco.witherstormmod.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.dabicco.witherstormmod.entity.withered.WitheredMobs;
import net.dabicco.witherstormmod.network.WitherSicknessPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;

public final class WitherSickness {
   private static final double RANGE_BASE = 72.0;
   private static final double RANGE_PER_PHASE = 18.0;
   private static final double RANGE_MAX = 184.0;
   public static final double WITHERED_FROM_PHASE = 5.0;
   public static final double WITHERED_FULL_PHASE = 5.8;
   private static final double PHASE4_RANGE = 34.0;
   private static final double WITHERED_TIGHT_RANGE = 72.0;
   private static final int SLOW_EXPOSURE_DIVISOR = 6;
   private static final int INCUBATION_TICKS = 1200;
   private static final int FULL_TICKS = 1600;
   private static final int NIGHT_TURN_TICKS = 200;
   private static final double CAVE_RANGE = 26.0;
   private static final double CAVE_RANGE_SQR = 676.0;
   private static final Map<UUID, Integer> EXPOSURE = new HashMap<>();
   private static final Identifier SICK_KNOCKBACK = Identifier.fromNamespaceAndPath("dabywitherstormmod", "sickened_knockback");
   private static final float NEARLY_TURNED = 0.55F;

   private static double rangeFor(net.dabicco.witherstormmod.entity.WitherStormEntity storm) {
      double p = Math.max(0.0, storm.getPhase());
      if (p >= 5.8) {
         return Math.min(184.0, 72.0 + p * 18.0);
      } else {
         return p >= 5.0 ? 72.0 : 34.0;
      }
   }

   private WitherSickness() {
   }

   public static void serverTick(ServerLevel level, net.dabicco.witherstormmod.entity.WitherStormEntity storm) {
      List<LivingEntity> nearby = level.getEntitiesOfClass(
         LivingEntity.class,
         storm.getBoundingBox().inflate(rangeFor(storm)),
         e -> e.isAlive() && !(e instanceof Player) && !(e instanceof net.dabicco.witherstormmod.entity.WitherStormEntity)
      );
      boolean night = level.isDarkOutside();

      for (LivingEntity mob : nearby) {
         if (!WitheredMobs.isWithered(mob)) {
            boolean underground = !level.canSeeSky(mob.blockPosition());
            if (!underground || !(mob.distanceToSqr(storm) > 676.0)) {
               double phaseNow = Math.max(0.0, storm.getPhase());
               int gain = phaseNow >= 5.8 ? 20 : Math.max(1, 3);
               int ticks = EXPOSURE.merge(mob.getUUID(), gain, Integer::sum);
               boolean canTurn = phaseNow >= 5.0 && WitheredMobs.canTurn(mob) && !storm.isDoomed(mob);
               float progress = night && !underground && canTurn ? Math.min(1.0F, ticks / 200.0F) : progressFor(ticks);
               if (!(progress <= 0.0F)) {
                  broadcast(mob, progress);
                  boolean converting = canTurn && WitheredMobs.hasRoom(level, mob);
                  boolean turned = progress >= 1.0F && converting && WitheredMobs.turn(level, mob);
                  if (!turned) {
                     markSickened(level, mob);
                     if (converting) {
                        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false));
                     } else if (progress > 0.35F) {
                        mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, progress > 0.8F ? 1 : 0));
                     }
                  }
               }
            }
         }
      }

      if (EXPOSURE.size() > 4096) {
         EXPOSURE.clear();
      }
   }

   public static void advance(LivingEntity mob, int ticks) {
      int total = EXPOSURE.merge(mob.getUUID(), ticks, Integer::sum);
      float progress = progressFor(total);
      if (progress > 0.0F) {
         broadcast(mob, progress);
      }
   }

   private static void markSickened(ServerLevel level, LivingEntity mob) {
      level.sendParticles(
         ParticleTypes.ASH,
         mob.getX(),
         mob.getY() + mob.getBbHeight() * 0.5,
         mob.getZ(),
         6,
         mob.getBbWidth() * 0.5,
         mob.getBbHeight() * 0.35,
         mob.getBbWidth() * 0.5,
         0.01
      );
      AttributeInstance knockback = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
      if (knockback != null) {
         knockback.addOrReplacePermanentModifier(new AttributeModifier(SICK_KNOCKBACK, 1.0, Operation.ADD_VALUE));
      }
   }

   public static boolean isAboutToTurn(LivingEntity mob, double stormPhase) {
      if (stormPhase < 5.0) {
         return false;
      } else if (!WitheredMobs.canTurn(mob)) {
         return false;
      } else {
         Integer ticks = EXPOSURE.get(mob.getUUID());
         if (ticks == null) {
            return false;
         } else {
            boolean fastPath = mob.level().isDarkOutside() && mob.level().canSeeSky(mob.blockPosition());
            float progress = fastPath ? Math.min(1.0F, ticks.intValue() / 200.0F) : progressFor(ticks);
            return progress >= 0.55F;
         }
      }
   }

   public static boolean isTurning(LivingEntity mob) {
      if (!WitheredMobs.canTurn(mob)) {
         return false;
      } else {
         Integer ticks = EXPOSURE.get(mob.getUUID());
         if (ticks != null && ticks > 0) {
            boolean fastPath = mob.level().isDarkOutside() && mob.level().canSeeSky(mob.blockPosition());
            return fastPath || ticks > 1200;
         } else {
            return false;
         }
      }
   }

   public static void keepWithered(LivingEntity mob) {
      EXPOSURE.put(mob.getUUID(), 2800);
      broadcast(mob, 1.0F, true);
   }

   private static float progressFor(int exposureTicks) {
      return exposureTicks <= 1200 ? 0.0F : Math.min(1.0F, (exposureTicks - 1200) / 1600.0F);
   }

   private static void broadcast(LivingEntity mob, float progress) {
      broadcast(mob, progress, false);
   }

   private static void broadcast(LivingEntity mob, float progress, boolean withered) {
      WitherSicknessPayload payload = new WitherSicknessPayload(mob.getId(), progress, withered);

      for (ServerPlayer player : PlayerLookup.tracking(mob)) {
         ServerPlayNetworking.send(player, payload);
      }
   }
}
