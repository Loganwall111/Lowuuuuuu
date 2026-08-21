package net.dabicco.witherstormmod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.dabicco.witherstormmod.bowels.BowelsFinale;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;

public final class BowelsGravity {
   public static final ResourceKey<Level> BOWELS;
   private static final int SWING_TICKS = 900;
   private static final double PULL = 0.08;
   private static final double TERMINAL = 3.92;
   private static final Direction[] CYCLE;
   private static final float TURNOVER_DAMAGE = 3.0F;
   private static final float ROLL_DAMAGE = 1.0F;
   private static final int SPILL_LIFETIME = 300;
   private static final double SHAFT_CLEAR;
   private static final double SHAFT_SHOVE = 0.055;
   private static ServerLevel hallwayChecked;
   private static final Set<UUID> MANAGED;

   private BowelsGravity() {
   }

   public static boolean isBowels(Level level) {
      return level.dimension().equals(BOWELS);
   }

   public static Direction axisAt(double x, double y, double z, long gameTime, Direction previous) {
      Direction structured = BowelsRoute.axisAt(x, y, z, previous);
      if (structured != null) {
         return structured;
      } else {
         return previous != null ? previous : Direction.DOWN;
      }
   }

   public static Quaternionf frame(Direction pull, Quaternionf dest) {
      float QUARTER = ((float)Math.PI / 2F);
      Quaternionf var10000;
      switch (pull) {
         case DOWN -> var10000 = dest.identity();
         case UP -> var10000 = dest.rotationZ((float)Math.PI);
         case EAST -> var10000 = dest.rotationZ(((float)Math.PI / 2F));
         case WEST -> var10000 = dest.rotationZ((-(float)Math.PI / 2F));
         case NORTH -> var10000 = dest.rotationX(((float)Math.PI / 2F));
         case SOUTH -> var10000 = dest.rotationX((-(float)Math.PI / 2F));
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static void tick(ServerLevel level) {
      if (!isBowels(level)) {
         if (!MANAGED.isEmpty()) {
            for(ServerPlayer player : level.players()) {
               release(player);
            }
         }

      } else {
         if (hallwayChecked != level && !level.players().isEmpty()) {
            hallwayChecked = level;
            BowelsHallway.ensureBuilt(level);
         }

         long time = level.getGameTime();
         List<ServerPlayer> leaving = null;
         List<Entity> spent = null;

         for(Entity entity : level.getAllEntities()) {
            if (entity != null && !entity.isRemoved()) {
               if (entity instanceof ServerPlayer) {
                  ServerPlayer p = (ServerPlayer)entity;
                  if (BowelsEndRoom.atEjectPoint(p.getX(), p.getY(), p.getZ())) {
                     if (leaving == null) {
                        leaving = new ArrayList(1);
                     }

                     leaving.add(p);
                     continue;
                  }
               }

               BowelsFinale.pull(level, entity);
               BowelsEntry.centreDrop(level, entity);
               if (entity instanceof LivingEntity) {
                  release(entity);
                  clearOfTheShaft(entity);
                  if (entity instanceof ServerPlayer) {
                     ServerPlayer p = (ServerPlayer)entity;
                     landing(p);
                  }
               } else {
                  if (entity instanceof ItemEntity) {
                     ItemEntity item = (ItemEntity)entity;
                     if (item.getAge() >= 300 && item.getOwner() == null) {
                        if (spent == null) {
                           spent = new ArrayList(4);
                        }

                        spent.add(item);
                        continue;
                     }
                  }

                  push(entity, BowelsFrame.of(entity));
               }
            }
         }

         BowelsFinale.sweepStragglers(level);
         if (spent != null) {
            for(Entity e : spent) {
               e.discard();
            }
         }

         if (leaving != null) {
            for(ServerPlayer p : leaving) {
               BowelsEndRoom.eject(p);
            }
         }

      }
   }

   private static void clearOfTheShaft(Entity entity) {
      if (BowelsFlip.rolling(entity.level().getGameTime()) && !entity.onGround()) {
         if (!BowelsFinale.running(entity.level().getGameTime())) {
            if (BowelsEndRoom.holds(entity.getX(), entity.getY(), entity.getZ())) {
               double dx = entity.getX() - (double)177.0F;
               double dz = entity.getZ() - (double)0.0F;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (!(d >= SHAFT_CLEAR)) {
                  if (d < 0.001) {
                     dx = (double)1.0F;
                     dz = (double)0.0F;
                     d = (double)1.0F;
                  }

                  double strength = ((double)1.0F - d / SHAFT_CLEAR) * 0.055;
                  entity.setDeltaMovement(entity.getDeltaMovement().add(dx / d * strength, (double)0.0F, dz / d * strength));
                  entity.hurtMarked = true;
               }
            }
         }
      }
   }

   private static void landing(ServerPlayer player) {
      BowelsBody body = (BowelsBody)player;
      Direction now = BowelsFrame.boxAxis(player);
      Direction before = body.dabyws$lastPull();
      body.dabyws$setLastPull(now);
      if (before != null && before != now) {
         body.dabyws$setTurnoverFall(!player.onGround());
      } else if (body.dabyws$turnoverFall() && player.onGround()) {
         body.dabyws$setTurnoverFall(false);
         float cost = BowelsEndRoom.holds(player.getX(), player.getY(), player.getZ()) ? 1.0F : 3.0F;
         player.hurtServer(player.level(), player.damageSources().fall(), cost);
      }
   }

   private static void push(Entity entity, Direction gravity) {
      if (!entity.isNoGravity() || !MANAGED.contains(entity.getUUID())) {
         MANAGED.add(entity.getUUID());
         entity.setNoGravity(true);
      }
   }

   public static void release(Entity entity) {
      if (MANAGED.remove(entity.getUUID())) {
         entity.setNoGravity(false);
      }

   }

   public static boolean managed(Entity entity) {
      return MANAGED.contains(entity.getUUID());
   }

   static {
      BOWELS = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("dabywitherstormmod", "bowels"));
      CYCLE = new Direction[]{Direction.DOWN, Direction.EAST, Direction.UP, Direction.NORTH, Direction.DOWN, Direction.WEST, Direction.UP, Direction.SOUTH};
      SHAFT_CLEAR = BowelsEndRoom.holeClearance() + (double)1.5F;
      MANAGED = new HashSet();
   }
}
