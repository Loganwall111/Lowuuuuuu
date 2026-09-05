package net.dabicco.witherstormmod;

import net.dabicco.witherstormmod.BowelsFrame.1;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BowelsFrame {
   private static final Direction[] FRAME_HORIZONTAL = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

   private BowelsFrame() {
   }

   public static Direction of(Entity entity) {
      BowelsBody body = (BowelsBody)entity;
      if (!BowelsGravity.isBowels(entity.level())) {
         body.dabyws$setSettled((Direction)null);
         return Direction.DOWN;
      } else {
         Direction settled = BowelsGravity.axisAt(entity.getX(), entity.getY(), entity.getZ(), entity.level().getGameTime(), body.dabyws$settled());
         body.dabyws$setSettled(settled);
         return settled;
      }
   }

   public static boolean turned(Entity entity) {
      return of(entity) != Direction.DOWN;
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static Vec3 toWorld(Direction gravity, Vec3 v) {
      return switch (1.$SwitchMap$net$minecraft$core$Direction[gravity.ordinal()]) {
         case 1 -> v;
         case 2 -> new Vec3(-v.x, -v.y, v.z);
         case 3 -> new Vec3(-v.y, v.x, v.z);
         case 4 -> new Vec3(v.y, -v.x, v.z);
         case 5 -> new Vec3(v.x, -v.z, v.y);
         case 6 -> new Vec3(v.x, v.z, -v.y);
         default -> throw new MatchException((String)null, (Throwable)null);
      };
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static Vec3 toFrame(Direction gravity, Vec3 v) {
      return switch (1.$SwitchMap$net$minecraft$core$Direction[gravity.ordinal()]) {
         case 1 -> v;
         case 2 -> new Vec3(-v.x, -v.y, v.z);
         case 3 -> new Vec3(v.y, -v.x, v.z);
         case 4 -> new Vec3(-v.y, v.x, v.z);
         case 5 -> new Vec3(v.x, v.z, -v.y);
         case 6 -> new Vec3(v.x, -v.z, v.y);
         default -> throw new MatchException((String)null, (Throwable)null);
      };
   }

   public static Vec3 down(Direction gravity) {
      return new Vec3(gravity.getStepX(), gravity.getStepY(), gravity.getStepZ());
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static AABB box(Direction gravity, Vec3 feet, double width, double height) {
      double half = width * 0.5;
      double minX = feet.x - half;
      double maxX = feet.x + half;
      double minY = feet.y - half;
      double maxY = feet.y + half;
      double minZ = feet.z - half;
      double maxZ = feet.z + half;
      switch (1.$SwitchMap$net$minecraft$core$Direction[gravity.ordinal()]) {
         case 1:
            minY = feet.y;
            maxY = feet.y + height;
            break;
         case 2:
            minY = feet.y - height;
            maxY = feet.y;
            break;
         case 3:
            minX = feet.x - height;
            maxX = feet.x;
            break;
         case 4:
            minX = feet.x;
            maxX = feet.x + height;
            break;
         case 5:
            minZ = feet.z;
            maxZ = feet.z + height;
            break;
         case 6:
            minZ = feet.z - height;
            maxZ = feet.z;
      }

      return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public static Vec3 eye(Direction gravity, Vec3 feet, double eyeHeight) {
      return feet.subtract(down(gravity).scale(eyeHeight));
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static AABB footprint(Direction gravity, AABB box) {
      double skin = 1.0E-6;

      return switch (1.$SwitchMap$net$minecraft$core$Direction[gravity.ordinal()]) {
         case 1 -> new AABB(box.minX, box.minY - skin, box.minZ, box.maxX, box.minY, box.maxZ);
         case 2 -> new AABB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY + skin, box.maxZ);
         case 3 -> new AABB(box.maxX, box.minY, box.minZ, box.maxX + skin, box.maxY, box.maxZ);
         case 4 -> new AABB(box.minX - skin, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
         case 5 -> new AABB(box.minX, box.minY, box.minZ - skin, box.maxX, box.maxY, box.minZ);
         case 6 -> new AABB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ + skin);
         default -> throw new MatchException((String)null, (Throwable)null);
      };
   }

   public static BlockState viewBlocker(Player player, Direction gravity) {
      Vec3 eye = player.getEyePosition();
      double across = player.getBbWidth() * 0.8;
      MutableBlockPos at = new MutableBlockPos();

      for (int corner = 0; corner < 8; corner++) {
         Vec3 offset = toWorld(gravity, new Vec3(((corner >> 0) % 2 - 0.5) * across, ((corner >> 1) % 2 - 0.5) * 0.1, ((corner >> 2) % 2 - 0.5) * across));
         at.set(eye.x + offset.x, eye.y + offset.y, eye.z + offset.z);
         BlockState state = player.level().getBlockState(at);
         if (state.getRenderShape() != RenderShape.INVISIBLE && state.isViewBlocking(player.level(), at)) {
            return state;
         }
      }

      return null;
   }

   public static Vec3 backOffFromEdge(Player player, Direction gravity, Vec3 movement) {
      Vec3 frame = toFrame(gravity, movement);
      if (frame.y > 0.0) {
         return movement;
      } else {
         double x = frame.x;
         double z = frame.z;
         double step = 0.05;

         while (x != 0.0 && wouldFall(player, gravity, x, 0.0)) {
            x = trim(x, 0.05);
         }

         while (z != 0.0 && wouldFall(player, gravity, 0.0, z)) {
            z = trim(z, 0.05);
         }

         while (x != 0.0 && z != 0.0 && wouldFall(player, gravity, x, z)) {
            x = trim(x, 0.05);
            z = trim(z, 0.05);
         }

         return toWorld(gravity, new Vec3(x, frame.y, z));
      }
   }

   private static double trim(double value, double step) {
      if (value < step && value >= -step) {
         return 0.0;
      } else {
         return value > 0.0 ? value - step : value + step;
      }
   }

   private static boolean wouldFall(Player player, Direction gravity, double x, double z) {
      AABB box = player.getBoundingBox().move(toWorld(gravity, new Vec3(x, 0.0, z)));
      Vec3 drop = down(gravity).scale(0.6);
      AABB under = footprint(gravity, box).expandTowards(drop.x, drop.y, drop.z);
      return player.level().noCollision(player, under);
   }

   public static void unstick(Entity entity, Direction gravity, double x, double z) {
      double reach = entity.getBbWidth() * 0.35;
      Vec3 corner = toWorld(gravity, new Vec3(Math.signum(x - entity.getX()) * reach, 0.0, Math.signum(z - entity.getZ()) * reach));
      Vec3 at = entity.position().add(corner);
      BlockPos pos = BlockPos.containing(at.x, at.y, at.z);
      if (suffocates(entity, gravity, pos)) {
         Direction escape = null;
         double best = Double.MAX_VALUE;

         for (Direction frame : FRAME_HORIZONTAL) {
            Direction world = Direction.getApproximateNearest(toWorld(gravity, new Vec3(frame.getStepX(), frame.getStepY(), frame.getStepZ())));
            double into = world.getAxis().choose(at.x - pos.getX(), at.y - pos.getY(), at.z - pos.getZ());
            double distance = world.getAxisDirection() == AxisDirection.POSITIVE ? 1.0 - into : into;
            if (distance < best && !suffocates(entity, gravity, pos.relative(world))) {
               best = distance;
               escape = frame;
            }
         }

         if (escape != null) {
            Vec3 velocity = entity.getDeltaMovement();
            if (escape.getAxis() == Axis.X) {
               entity.setDeltaMovement(0.1 * escape.getStepX(), velocity.y, velocity.z);
            } else {
               entity.setDeltaMovement(velocity.x, velocity.y, 0.1 * escape.getStepZ());
            }
         }
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private static boolean suffocates(Entity entity, Direction gravity, BlockPos pos) {
      AABB body = entity.getBoundingBox();
      double x0 = pos.getX();
      double y0 = pos.getY();
      double z0 = pos.getZ();
      double x1 = x0 + 1.0;
      double y1 = y0 + 1.0;
      double z1 = z0 + 1.0;
      switch (1.$SwitchMap$net$minecraft$core$Direction$Axis[gravity.getAxis().ordinal()]) {
         case 1:
            x0 = body.minX;
            x1 = body.maxX;
            break;
         case 2:
            y0 = body.minY;
            y1 = body.maxY;
            break;
         case 3:
            z0 = body.minZ;
            z1 = body.maxZ;
      }

      return entity.level().collidesWithSuffocatingBlock(entity, new AABB(x0, y0, z0, x1, y1, z1).deflate(1.0E-7));
   }

   public static boolean enter(Entity entity, Direction gravity) {
      BowelsBody body = (BowelsBody)entity;
      int depth = body.dabyws$stepDepth();
      body.dabyws$setStepDepth(depth + 1);
      if (depth > 0) {
         return false;
      } else {
         body.dabyws$setStepAxis(gravity);
         return true;
      }
   }

   public static boolean leave(Entity entity) {
      BowelsBody body = (BowelsBody)entity;
      int depth = body.dabyws$stepDepth();
      if (depth <= 0) {
         return false;
      } else {
         body.dabyws$setStepDepth(depth - 1);
         if (depth - 1 > 0) {
            return false;
         } else {
            body.dabyws$setStepAxis((Direction)null);
            return true;
         }
      }
   }

   public static Direction active(Entity entity) {
      return ((BowelsBody)entity).dabyws$stepAxis();
   }

   public static Direction boxAxis(Entity entity) {
      Direction held = ((BowelsBody)entity).dabyws$stepAxis();
      return held != null ? held : of(entity);
   }

   public static void stepIn(Entity entity) {
      Direction gravity = active(entity);
      if (gravity == null) {
         gravity = of(entity);
         if (gravity == Direction.DOWN) {
            return;
         }
      }

      if (enter(entity, gravity)) {
         entity.setDeltaMovement(toFrame(gravity, entity.getDeltaMovement()));
      }
   }

   public static void stepOut(Entity entity) {
      Direction gravity = active(entity);
      if (gravity != null && leave(entity)) {
         entity.setDeltaMovement(toWorld(gravity, entity.getDeltaMovement()));
      }
   }

   public static double climb(Entity entity, Direction gravity, boolean blocked) {
      if (blocked && entity.onGround()) {
         double reach = 0.6;
         Vec3 ahead = toFrame(gravity, entity.getDeltaMovement());
         Vec3 flat = new Vec3(ahead.x, 0.0, ahead.z);
         if (flat.lengthSqr() < 1.0E-8) {
            return 0.0;
         } else {
            Vec3 probe = toWorld(gravity, flat.normalize().scale(0.1));
            AABB box = entity.getBoundingBox();
            if (entity.level().noCollision(box.move(probe))) {
               return 0.0;
            } else {
               for (double lift = 0.0625; lift <= 0.600001; lift += 0.0625) {
                  Vec3 up = toWorld(gravity, new Vec3(0.0, lift, 0.0));
                  AABB raised = box.move(up);
                  if (entity.level().noCollision(raised) && entity.level().noCollision(raised.move(probe))) {
                     return lift;
                  }
               }

               return 0.0;
            }
         }
      } else {
         return 0.0;
      }
   }
}
