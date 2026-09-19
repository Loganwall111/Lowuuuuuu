package net.dabicco.witherstormmod;

import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BowelsPortal {
   public static final double COVER_PHASE = 6.8;
   public static final double COVER_DONE_PHASE = 6.9;
   private static final double MODEL_X = (double)26.0F;
   private static final double MODEL_Y = (double)-192.0F;
   private static final double MODEL_Z = (double)-32.0F;
   private static final double MODEL_HALF = (double)56.0F;
   private static final double BODY_SCALE = 1.1009174311926604;
   private static final double BODY_LIFT = 4.4990000000000006;
   private static final Vec3 LOCAL = fromModel((double)26.0F, (double)-192.0F, (double)-32.0F);
   private static final double HALF = 3.8532110091743115;
   private static final double VISUAL = 0.72;
   private static final double TOUCH_SLACK = (double)1.5F;

   private BowelsPortal() {
   }

   public static int platesFor(double phase) {
      if (phase < 6.8) {
         return 0;
      } else {
         double through = (phase - 6.8) / 0.10000000000000053;
         int plates = (int)Math.floor(through * (double)4.0F);
         return Math.max(0, Math.min(4, plates));
      }
   }

   public static boolean open(double phase) {
      return platesFor(phase) >= 4;
   }

   private static Vec3 fromModel(double mx, double my, double mz) {
      return new Vec3(mx / (double)16.0F * 1.1009174311926604, -(my / (double)16.0F + 4.4990000000000006) * 1.1009174311926604, -(mz / (double)16.0F) * 1.1009174311926604);
   }

   private static Vec3 turn(Vec3 local, float yawDegrees) {
      double yaw = Math.toRadians((double)yawDegrees);
      double c = Math.cos(yaw);
      double s = Math.sin(yaw);
      return new Vec3(local.x * c - local.z * s, local.y, local.x * s + local.z * c);
   }

   public static Vec3 sheetOffset(float yawDegrees) {
      return turn(LOCAL, yawDegrees);
   }

   public static Vec3[] corners(float yawDegrees) {
      double r = 2.7743119266055043;
      Vec3 centre = sheetOffset(yawDegrees);
      Vec3 right = turn(new Vec3((double)1.0F, (double)0.0F, (double)0.0F), yawDegrees);
      Vec3 up = new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
      return new Vec3[]{centre.add(right.scale(-r)).add(up.scale(-r)), centre.add(right.scale(r)).add(up.scale(-r)), centre.add(right.scale(r)).add(up.scale(r)), centre.add(right.scale(-r)).add(up.scale(r))};
   }

   public static Vec3 sheetAt(Entity storm) {
      return storm.position().add(sheetOffset(storm.getYRot()));
   }

   public static AABB mouth(Entity storm) {
      Vec3 at = sheetAt(storm);
      double r = 5.353211009174311;
      return new AABB(at.x - r, at.y - r, at.z - r, at.x + r, at.y + r, at.z + r);
   }

   public static void tick(ServerLevel level, Entity storm, int plates) {
      Vec3 at = sheetAt(storm);
      boolean open = plates >= 4;
      level.sendParticles(open ? ParticleTypes.PORTAL : ParticleTypes.WITCH, at.x, at.y, at.z, open ? 24 : 6, 1.9266055045871557, 1.9266055045871557, 1.9266055045871557, 0.05);
      if (open) {
         for(Entity touching : level.getEntities(storm, mouth(storm))) {
            if (touching instanceof ServerPlayer) {
               ServerPlayer player = (ServerPlayer)touching;
               send(player);
            } else if (touching instanceof ThrownEnderpearl) {
               ThrownEnderpearl pearl = (ThrownEnderpearl)touching;
               Entity var9 = pearl.getOwner();
               if (var9 instanceof ServerPlayer) {
                  ServerPlayer thrower = (ServerPlayer)var9;
                  send(thrower);
               }

               pearl.discard();
            }
         }

      }
   }

   public static void send(ServerPlayer player) {
      MinecraftServer server = player.level().getServer();
      if (server != null) {
         ServerLevel bowels = server.getLevel(BowelsGravity.BOWELS);
         if (bowels != null) {
            if (player.level() != bowels) {
               BowelsHallway.ensureBuilt(bowels);
               Vec3 to = BowelsEntry.arrival();
               player.teleportTo(bowels, to.x, to.y, to.z, Set.of(), player.getYRot(), 0.0F, false);
               player.setDeltaMovement(Vec3.ZERO);
               player.resetFallDistance();
               bowels.playSound((Entity)null, to.x, to.y, to.z, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.0F, 0.7F);
            }
         }
      }
   }
}
