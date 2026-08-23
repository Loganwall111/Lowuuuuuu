package net.dabicco.devouringstorms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BowelsFrame {
   private static final Direction[] FRAME_HORIZONTAL;

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

   public static Vec3 toWorld(Direction gravity, Vec3 v) {
      Vec3 var10000;
      switch (gravity) {
         case DOWN -> var10000 = v;
         case UP -> var10000 = new Vec3(-v.x, -v.y, v.z);
         case EAST -> var10000 = new Vec3(-v.y, v.x, v.z);
         case WEST -> var10000 = new Vec3(v.y, -v.x, v.z);
         case NORTH -> var10000 = new Vec3(v.x, -v.z, v.y);
         case SOUTH -> var10000 = new Vec3(v.x, v.z, -v.y);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static Vec3 toFrame(Direction gravity, Vec3 v) {
      Vec3 var10000;
      switch (gravity) {
         case DOWN -> var10000 = v;
         case UP -> var10000 = new Vec3(-v.x, -v.y, v.z);
         case EAST -> var10000 = new Vec3(v.y, -v.x, v.z);
         case WEST -> var10000 = new Vec3(-v.y, v.x, v.z);
         case NORTH -> var10000 = new Vec3(v.x, v.z, -v.y);
         case SOUTH -> var10000 = new Vec3(v.x, -v.z, v.y);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static Vec3 down(Direction gravity) {
      return new Vec3((double)gravity.getStepX(), (double)gravity.getStepY(), (double)gravity.getStepZ());
   }

   public static AABB box(Direction gravity, Vec3 feet, double width, double height) {
      double half = width * (double)0.5F;
      double minX = feet.x - half;
      double maxX = feet.x + half;
      double minY = feet.y - half;
      double maxY = feet.y + half;
      double minZ = feet.z - half;
      double maxZ = feet.z + half;
      switch (gravity) {
         case DOWN:
            minY = feet.y;
            maxY = feet.y + height;
            break;
         case UP:
            minY = feet.y - height;
            maxY = feet.y;
            break;
         case EAST:
            minX = feet.x - height;
            maxX = feet.x;
            break;
         case WEST:
            minX = feet.x;
            maxX = feet.x + height;
            break;
         case NORTH:
            minZ = feet.z;
            maxZ = feet.z + height;
            break;
         case SOUTH:
            minZ = feet.z - height;
            maxZ = feet.z;
      }

      return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public static Vec3 eye(Direction gravity, Vec3 feet, double eyeHeight) {
      return feet.subtract(down(gravity).scale(eyeHeight));
   }

   public static AABB footprint(Direction gravity, AABB box) {
      double skin = 1.0E-6;
      AABB var10000;
      switch (gravity) {
         case DOWN -> var10000 = new AABB(box.minX, box.minY - skin, box.minZ, box.maxX, box.minY, box.maxZ);
         case UP -> var10000 = new AABB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY + skin, box.maxZ);
         case EAST -> var10000 = new AABB(box.maxX, box.minY, box.minZ, box.maxX + skin, box.maxY, box.maxZ);
         case WEST -> var10000 = new AABB(box.minX - skin, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
         case NORTH -> var10000 = new AABB(box.minX, box.minY, box.minZ - skin, box.maxX, box.maxY, box.minZ);
         case SOUTH -> var10000 = new AABB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ + skin);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static BlockState viewBlocker(Player player, Direction gravity) {
      Vec3 eye = player.getEyePosition();
      double across = (double)player.getBbWidth() * 0.8;
      BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();

      for(int corner = 0; corner < 8; ++corner) {
         Vec3 offset = toWorld(gravity, new Vec3(((double)((corner >> 0) % 2) - (double)0.5F) * across, ((double)((corner >> 1) % 2) - (double)0.5F) * 0.1, ((double)((corner >> 2) % 2) - (double)0.5F) * across));
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
      if (frame.y > (double)0.0F) {
         return movement;
      } else {
         double x = frame.x;
         double z = frame.z;

         for(double step = 0.05; x != (double)0.0F && wouldFall(player, gravity, x, (double)0.0F); x = trim(x, 0.05)) {
         }

         while(z != (double)0.0F && wouldFall(player, gravity, (double)0.0F, z)) {
            z = trim(z, 0.05);
         }

         while(x != (double)0.0F && z != (double)0.0F && wouldFall(player, gravity, x, z)) {
            x = trim(x, 0.05);
            z = trim(z, 0.05);
         }

         return toWorld(gravity, new Vec3(x, frame.y, z));
      }
   }

   private static double trim(double value, double step) {
      if (value < step && value >= -step) {
         return (double)0.0F;
      } else {
         return value > (double)0.0F ? value - step : value + step;
      }
   }

   private static boolean wouldFall(Player player, Direction gravity, double x, double z) {
      AABB box = player.getBoundingBox().move(toWorld(gravity, new Vec3(x, (double)0.0F, z)));
      Vec3 drop = down(gravity).scale(0.6);
      AABB under = footprint(gravity, box).expandTowards(drop.x, drop.y, drop.z);
      return player.level().noCollision(player, under);
   }

   public static void unstick(Entity entity, Direction gravity, double x, double z) {
      double reach = (double)entity.getBbWidth() * 0.35;
      Vec3 corner = toWorld(gravity, new Vec3(Math.signum(x - entity.getX()) * reach, (double)0.0F, Math.signum(z - entity.getZ()) * reach));
      Vec3 at = entity.position().add(corner);
      BlockPos pos = BlockPos.containing(at.x, at.y, at.z);
      if (suffocates(entity, gravity, pos)) {
         Direction escape = null;
         double best = Double.MAX_VALUE;

         for(Direction frame : FRAME_HORIZONTAL) {
            Direction world = Direction.getApproximateNearest(toWorld(gravity, new Vec3((double)frame.getStepX(), (double)frame.getStepY(), (double)frame.getStepZ())));
            double into = world.getAxis().choose(at.x - (double)pos.getX(), at.y - (double)pos.getY(), at.z - (double)pos.getZ());
            double distance = world.getAxisDirection() == AxisDirection.POSITIVE ? (double)1.0F - into : into;
            if (distance < best && !suffocates(entity, gravity, pos.relative(world))) {
               best = distance;
               escape = frame;
            }
         }

         if (escape != null) {
            Vec3 velocity = entity.getDeltaMovement();
            if (escape.getAxis() == Axis.X) {
               entity.setDeltaMovement(0.1 * (double)escape.getStepX(), velocity.y, velocity.z);
            } else {
               entity.setDeltaMovement(velocity.x, velocity.y, 0.1 * (double)escape.getStepZ());
            }

         }
      }
   }

   private static boolean suffocates(Entity entity, Direction gravity, BlockPos pos) {
      AABB body = entity.getBoundingBox();
      double x0 = (double)pos.getX();
      double y0 = (double)pos.getY();
      double z0 = (double)pos.getZ();
      double x1 = x0 + (double)1.0F;
      double y1 = y0 + (double)1.0F;
      double z1 = z0 + (double)1.0F;
      switch (gravity.getAxis()) {
         case X:
            x0 = body.minX;
            x1 = body.maxX;
            break;
         case Y:
            y0 = body.minY;
            y1 = body.maxY;
            break;
         case Z:
            z0 = body.minZ;
            z1 = body.maxZ;
      }

      return entity.level().collidesWithSuffocatingBlock(entity, (new AABB(x0, y0, z0, x1, y1, z1)).deflate(1.0E-7));
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
      if (gravity != null) {
         if (leave(entity)) {
            entity.setDeltaMovement(toWorld(gravity, entity.getDeltaMovement()));
         }

      }
   }

   public static double climb(Entity entity, Direction gravity, boolean blocked) {
      if (blocked && entity.onGround()) {
         double reach = 0.6;
         Vec3 ahead = toFrame(gravity, entity.getDeltaMovement());
         Vec3 flat = new Vec3(ahead.x, (double)0.0F, ahead.z);
         if (flat.lengthSqr() < 1.0E-8) {
            return (double)0.0F;
         } else {
            Vec3 probe = toWorld(gravity, flat.normalize().scale(0.1));
            AABB box = entity.getBoundingBox();
            if (entity.level().noCollision(box.move(probe))) {
               return (double)0.0F;
            } else {
               for(double lift = (double)0.0625F; lift <= 0.600001; lift += (double)0.0625F) {
                  Vec3 up = toWorld(gravity, new Vec3((double)0.0F, lift, (double)0.0F));
                  AABB raised = box.move(up);
                  if (entity.level().noCollision(raised) && entity.level().noCollision(raised.move(probe))) {
                     return lift;
                  }
               }

               return (double)0.0F;
            }
         }
      } else {
         return (double)0.0F;
      }
   }

   static {
      FRAME_HORIZONTAL = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
   }
}
