package net.dabicco.devouringstorms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public final class BowelsEndRoom {
   public static final double ROOM_R = (double)21.0F;
   private static final double ROOM_RAGGED = 2.1;
   public static final double ROOM_X = (double)177.0F;
   public static final double ROOM_Z = (double)0.0F;
   public static final int FLOOR_Y = 60;
   public static final int CEIL_Y = 96;
   private static final double BOWL = (double)3.0F;
   private static final double FLOOR_ROUGH = 0.8;
   private static final double CEIL_BED = (double)3.0F;
   private static final long SALT_FLOOR = 3290067942L;
   private static final long SALT_ROOF = 802732293L;
   private static final long SALT_FEATURE = 2120458589L;
   private static final double FLAT_R = (double)14.0F;
   private static final double FLAT_FADE = (double)4.5F;
   private static final double APPROACH_HALF_WIDTH = (double)10.0F;
   private static final double SHAFT_R = (double)3.5F;
   private static final double SHAFT_RAGGED = 0.6;
   private static final int SHAFT_HEIGHT = 72;
   public static final int SHAFT_TOP = 168;
   public static final int EJECT_Y = 132;
   private static final int WALL = 3;
   private static final double INFLUENCE = 31.1;
   private static final long SALT_WALL = 1797902671L;
   private static final long SALT_SHAFT = 2742967426L;
   public static final double PLATTER_R = (double)4.5F;
   private static final double PLATTER_RAGGED = (double)0.5F;
   private static final double MID_R = 2.3;
   private static final double MID_RAGGED = 0.26;
   private static final double PLUS_ARM = (double)1.5F;
   private static final double PLUS_HALF = (double)0.5F;
   public static final int DAIS_H = 3;
   private static final double GUARD_BURY = 0.65;
   private static final int PEDESTAL_BASE = 1;
   public static final int PEDESTAL_RISE = 4;
   private static final double[] SAND_R = new double[]{(double)0.0F, (double)5.5F, 6.2, 6.9};
   private static final double SAND_RAGGED = (double)0.5F;
   private static final double SAND_BUMP = 1.15;
   private static final double SAND_BUMP_RARITY = 0.55;
   private static final int SAND_COURSES = 4;
   public static final int SAND_COURSE_COUNT = 4;
   private static final long SALT_DAIS = 1370539945L;
   private static final long SALT_SAND = 1014634980L;
   private static final long SALT_BUMP = 2637063351L;
   private static final long SALT_STEP = 1492323489L;
   public static final int HOLES = 12;
   private static final double HOLE_R = 1.35;
   private static final int HOLE_UP = 2;
   public static final double HOLE_BACK = (double)9.0F;
   private static final double HOLE_DEPTH = (double)10.0F;
   private static final double DOOR_R = 7.2;

   private BowelsEndRoom() {
   }

   private static double floorAt(long seed, double x, double z) {
      double dx = x - (double)177.0F;
      double dz = z - (double)0.0F;
      double r = Math.min(Math.sqrt(dx * dx + dz * dz) / (double)21.0F, (double)1.0F);
      double rough = (double)BowelsHallway.noise(seed ^ 3290067942L, x / (double)8.0F, z / (double)8.0F);
      double lift = r * r * r * r;
      double level = approachLevel(x, z);
      double middle = flatMiddle(x, z);
      double top = (double)60.0F + (double)3.0F * lift * level * middle + 0.8 * rough * (0.35 + 0.65 * lift) * level * middle;
      return Math.max(top, (double)60.0F);
   }

   private static double flatMiddle(double x, double z) {
      double dx = x - (double)177.0F;
      double dz = z - (double)0.0F;
      double r = Math.sqrt(dx * dx + dz * dz);
      return r <= (double)14.0F ? (double)0.0F : Mth.clamp((r - (double)14.0F) / (double)4.5F, (double)0.0F, (double)1.0F);
   }

   private static double approachLevel(double x, double z) {
      double across = Math.abs(z - (double)0.0F) / (double)10.0F;
      if (across >= (double)1.0F) {
         return (double)1.0F;
      } else {
         double along = Math.min(Math.abs(x - (double)177.0F) / (double)21.0F, (double)1.0F);
         if (along <= 0.45) {
            return (double)1.0F;
         } else {
            double reach = Mth.clamp((along - 0.45) / 0.55, (double)0.0F, (double)1.0F);
            double band = (double)1.0F - across * across;
            return (double)1.0F - reach * band;
         }
      }
   }

   private static double roofAt(long seed, double x, double z) {
      double dx = x - (double)177.0F;
      double dz = z - (double)0.0F;
      double r = Math.min(Math.sqrt(dx * dx + dz * dz) / (double)21.0F, (double)1.0F);
      double rough = (double)BowelsHallway.noise(seed ^ 802732293L, x / (double)8.0F, z / (double)8.0F);
      return (double)93.0F - (double)3.0F * r * r * (double)0.5F - 0.8 * rough;
   }

   static double outerFace(long seed, double y, double turns) {
      double h = Mth.clamp((y - (double)60.0F) / (double)36.0F, (double)0.0F, (double)1.0F);
      double profile = Math.sqrt(Math.max((double)0.0F, (double)1.0F - h * h * 0.88));
      return (double)21.0F * profile + 2.1 * (double)BowelsHallway.ringNoise(seed ^ 1797902671L, y / (double)9.0F, turns) + (double)3.0F;
   }

   static double shaftFace(long seed, double y, double turns) {
      return (double)3.5F + 0.6 * (double)BowelsHallway.ringNoise(seed ^ 2742967426L, y / (double)7.0F, turns) + (double)3.0F;
   }

   public static boolean holds(double x, double y, double z) {
      if (!(y < (double)52.0F) && !(y > (double)176.0F)) {
         double dx = x - (double)177.0F;
         double dz = z - (double)0.0F;
         return dx * dx + dz * dz < 967.21;
      } else {
         return false;
      }
   }

   public static double standAt(long seed, double x, double z) {
      return !BowelsFlip.flipped() ? floorAt(seed, x, z) + (double)1.5F : roofAt(seed, x, z) - (double)3.0F;
   }

   public static double holeClearance() {
      return 5.6;
   }

   public static Direction pull(double y) {
      return BowelsFlip.flipped() ? Direction.UP : Direction.DOWN;
   }

   public static boolean atEjectPoint(double x, double y, double z) {
      if (y < (double)132.0F) {
         return false;
      } else {
         double dx = x - (double)177.0F;
         double dz = z - (double)0.0F;
         double reach = 4.1;
         return dx * dx + dz * dz < reach * reach;
      }
   }

   public static void eject(ServerPlayer player) {
      MinecraftServer server = player.level().getServer();
      if (server != null) {
         ServerLevel overworld = server.overworld();
         if (overworld != null) {
            Entity storm = nearestStorm(overworld);
            double toX;
            double toZ;
            if (storm != null) {
               toX = storm.getX() + (double)64.0F;
               toZ = storm.getZ();
            } else {
               BlockPos spawn = overworld.getRespawnData().pos();
               toX = (double)spawn.getX() + (double)0.5F;
               toZ = (double)spawn.getZ() + (double)0.5F;
            }

            int bx = Mth.floor(toX);
            int bz = Mth.floor(toZ);
            overworld.getChunk(bx >> 4, bz >> 4);
            int surface = overworld.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
            float yaw = storm == null ? player.getYRot() : (float)(-Math.toDegrees(Math.atan2(storm.getX() - toX, storm.getZ() - toZ)));
            BowelsGravity.release(player);
            player.resetFallDistance();
            player.setDeltaMovement(Vec3.ZERO);
            player.teleportTo(overworld, toX, (double)surface, toZ, Set.of(), yaw, 0.0F, false);
            player.sendSystemMessage(Component.literal("The storm spits you out."));
         }
      }
   }

   private static Entity nearestStorm(ServerLevel overworld) {
      for(Entity entity : overworld.getAllEntities()) {
         if (entity instanceof WitherStormEntity) {
            return entity;
         }
      }

      return null;
   }

   public static int build(ServerLevel level, long seed) {
      return clearInterior(level, seed) + chamber(level, seed) + shaft(level, seed) + features(level, seed) + pedestal(level, seed) + tentacleHoles(level, seed) + doorway(level, seed, 151, Mth.floor((double)156.0F) + 10, false) + doorway(level, seed, Mth.floor((double)198.0F) - 8, Mth.ceil((double)198.0F) + 3 + 6, true);
   }

   private static int features(ServerLevel level, long seed) {
      RandomSource rng = RandomSource.create(seed ^ 2120458589L);
      int placed = 0;

      for(int i = 0; i < 9; ++i) {
         double angle = rng.nextDouble() * Math.PI * (double)2.0F;
         double at = (0.3 + rng.nextDouble() * 0.6) * (double)21.0F;
         double x = (double)177.0F + Math.cos(angle) * at;
         double z = (double)0.0F + Math.sin(angle) * at;
         double roof = roofAt(seed, x, z);
         double drop = (double)2.0F + rng.nextDouble() * (double)5.0F;

         for(double y = roof; y >= roof - drop; y -= 0.8) {
            double taper = (double)1.0F - (roof - y) / (drop + (double)1.0F);
            placed += BowelsHallway.blob(level, rng, x, y, z, 1.4 * taper + 0.4, ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState());
         }
      }

      return placed;
   }

   public static int platterTopY() {
      return 62;
   }

   public static double guardMountY() {
      return (double)platterTopY() - 0.65;
   }

   public static boolean onPedestal(double x, double y, double z) {
      if (!(y < (double)61.0F) && !(y > (double)71.0F)) {
         double dx = x - (double)177.0F;
         double dz = z - (double)0.0F;
         double reach = (double)5.5F;
         return dx * dx + dz * dz < reach * reach;
      } else {
         return false;
      }
   }

   public static Vec3 daisTop() {
      return new Vec3(Math.floor((double)177.0F) + (double)0.5F, (double)64.0F, Math.floor((double)0.0F) + (double)0.5F);
   }

   private static int pedestal(ServerLevel level, long seed) {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int reach = Mth.ceil((double)4.5F) + 2;
      int placed = 0;
      BlockPos origin = pedestalOrigin();
      int cx = origin.getX();
      int cz = origin.getZ();
      placed += writePedestal(level, seed, 0);

      for(int x = cx - reach; x <= cx + reach; ++x) {
         for(int z = cz - reach; z <= cz + reach; ++z) {
            for(int y = 64; y <= 72; ++y) {
               pos.set(x, y, z);
               if (!level.getBlockState(pos).isAir()) {
                  level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                  ++placed;
               }
            }
         }
      }

      return placed;
   }

   public static BlockPos pedestalOrigin() {
      return new BlockPos(Mth.floor((double)177.0F), 60, Mth.floor((double)0.0F));
   }

   public static void collectPedestal(long seed, List<BlockPos> offsets, List<BlockState> states) {
      int reach = Mth.ceil((double)5.0F) + 1;
      BlockPos origin = pedestalOrigin();

      for(int step = 0; step < 3; ++step) {
         for(int dxi = -reach; dxi <= reach; ++dxi) {
            for(int dzi = -reach; dzi <= reach; ++dzi) {
               double dx = (double)dxi;
               double dz = (double)dzi;
               if (inTier(seed, dx, dz, step)) {
                  offsets.add(new BlockPos(dxi, 1 + step, dzi));
                  states.add(daisBlock(origin.getX() + dxi, 61 + step, origin.getZ() + dzi, step));
               }
            }
         }
      }

   }

   public static int writePedestal(ServerLevel level, long seed, int lift) {
      List<BlockPos> offsets = new ArrayList();
      List<BlockState> states = new ArrayList();
      collectPedestal(seed, offsets, states);
      BlockPos origin = pedestalOrigin();
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < offsets.size(); ++i) {
         BlockPos off = (BlockPos)offsets.get(i);
         pos.set(origin.getX() + off.getX(), origin.getY() + off.getY() + lift, origin.getZ() + off.getZ());
         level.setBlock(pos, (BlockState)states.get(i), 2);
      }

      return offsets.size();
   }

   public static int clearPedestal(ServerLevel level, long seed, int lift) {
      List<BlockPos> offsets = new ArrayList();
      List<BlockState> states = new ArrayList();
      collectPedestal(seed, offsets, states);
      BlockPos origin = pedestalOrigin();
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

      for(BlockPos off : offsets) {
         pos.set(origin.getX() + off.getX(), origin.getY() + off.getY() + lift, origin.getZ() + off.getZ());
         level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
      }

      return offsets.size();
   }

   private static boolean inTier(long seed, double dx, double dz, int step) {
      double r = Math.sqrt(dx * dx + dz * dz);
      double turns = (Math.atan2(dz, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
      boolean var10000;
      switch (step) {
         case 0 -> var10000 = r <= (double)4.5F + (double)0.5F * (double)BowelsHallway.ringNoise(seed ^ 1370539945L, 1.7, turns);
         case 1 -> var10000 = r <= 2.3 + 0.26 * (double)BowelsHallway.ringNoise(seed ^ 1370539945L, 5.3, turns);
         default -> var10000 = Math.abs(dx) <= (double)0.5F && Math.abs(dz) <= (double)1.5F || Math.abs(dz) <= (double)0.5F && Math.abs(dx) <= (double)1.5F;
      }

      return var10000;
   }

   private static BlockState daisBlock(int x, int y, int z, int step) {
      int h = x * 668265263 ^ y * 374761393 ^ z * -1640531527;
      h ^= h >>> 15;
      h *= -2048144789;
      h ^= h >>> 13;
      int oneIn = step == 2 ? 6 : 11;
      return Math.floorMod(h, oneIn) == 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
   }

   public static int sandLayerY(int layer) {
      return 64 - layer;
   }

   private static double sandFaceAt(long seed, double turns, int layer) {
      if (layer == 0) {
         return (double)4.5F + (double)0.5F * (double)BowelsHallway.ringNoise(seed ^ 1370539945L, 1.7, turns);
      } else {
         double face = SAND_R[layer] + (double)0.5F * (double)BowelsHallway.ringNoise(seed ^ 1014634980L, 3.1 + (double)layer * 4.7, turns);
         if (layer > 0) {
            double lump = (double)BowelsHallway.ringNoise(seed ^ 2637063351L, (double)layer * 9.3, turns * (double)4.0F);
            if (lump > 0.55) {
               face += 1.15 * (lump - 0.55) / 0.44999999999999996;
            }
         }

         return face;
      }
   }

   private static boolean sandStair(long seed, double turns, int layer) {
      if (layer == 0) {
         return false;
      } else {
         return (double)BowelsHallway.ringNoise(seed ^ 1492323489L, (double)layer * 6.1, turns * (double)3.0F) > 0.42;
      }
   }

   public static void collectSand(long seed, List<BlockPos> offsets, List<BlockState> states) {
      int reach = Mth.ceil(SAND_R[3] + (double)0.5F + 1.15) + 1;
      BlockPos origin = pedestalOrigin();

      for(int layer = 0; layer < 4; ++layer) {
         for(int dxi = -reach; dxi <= reach; ++dxi) {
            for(int dzi = -reach; dzi <= reach; ++dzi) {
               double dx = (double)dxi;
               double dz = (double)dzi;
               double r = Math.sqrt(dx * dx + dz * dz);
               double turns = (Math.atan2(dz, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               double face = sandFaceAt(seed, turns, layer);
               if (!(r > face)) {
                  boolean rim = r > face - (double)1.0F;
                  BlockState state = rim && sandStair(seed, turns, layer) ? stepOut(dx, dz) : ModBlocks.WITHERED_SAND.defaultBlockState();
                  offsets.add(new BlockPos(dxi, sandLayerY(layer) - 60, dzi));
                  states.add(state);
               }
            }
         }
      }

   }

   public static int writeSand(ServerLevel level, long seed) {
      List<BlockPos> offsets = new ArrayList();
      List<BlockState> states = new ArrayList();
      collectSand(seed, offsets, states);
      BlockPos origin = pedestalOrigin();
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < offsets.size(); ++i) {
         BlockPos off = (BlockPos)offsets.get(i);
         pos.set(origin.getX() + off.getX(), origin.getY() + off.getY(), origin.getZ() + off.getZ());
         level.setBlock(pos, (BlockState)states.get(i), 2);
      }

      return offsets.size();
   }

   private static BlockState stepOut(double dx, double dz) {
      Direction inward = Math.abs(dx) >= Math.abs(dz) ? (dx >= (double)0.0F ? Direction.WEST : Direction.EAST) : (dz >= (double)0.0F ? Direction.NORTH : Direction.SOUTH);
      return (BlockState)ModBlocks.WITHERED_STAIRS.defaultBlockState().setValue(StairBlock.FACING, inward);
   }

   public static Vec3 holeForward(int index) {
      double angle = holeAngle(index);
      return new Vec3(-Math.cos(angle), (double)0.0F, -Math.sin(angle));
   }

   public static Vec3 holeMouth(long seed, int index) {
      double angle = holeAngle(index);
      double faceR = (double)20.0F;
      double fx = (double)177.0F + Math.cos(angle) * faceR;
      double fz = (double)0.0F + Math.sin(angle) * faceR;
      return new Vec3(fx, floorAt(seed, fx, fz) + (double)2.0F, fz);
   }

   public static double holeAngle(int index) {
      int perSide = Math.max(1, 6);
      int side = index / perSide;
      int within = index % perSide;
      double from = Math.toRadians((double)28.0F);
      double to = Math.toRadians((double)152.0F);
      double at = from + (to - from) * ((double)within + (double)0.5F) / (double)perSide;
      return side == 0 ? at : at + Math.PI;
   }

   public static float holeYaw(int index) {
      return (float)Math.toDegrees(holeAngle(index)) + 90.0F;
   }

   private static int tentacleHoles(ServerLevel level, long seed) {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int placed = 0;

      for(int i = 0; i < 12; ++i) {
         double angle = holeAngle(i);
         double ox = Math.cos(angle);
         double oz = Math.sin(angle);
         double cy = holeMouth(seed, i).y;

         for(double along = (double)18.0F; along <= (double)30.0F; along += (double)0.5F) {
            double bx = (double)177.0F + ox * along;
            double bz = (double)0.0F + oz * along;
            int reach = Mth.ceil(1.35) + 1;

            for(int dx = -reach; dx <= reach; ++dx) {
               for(int dy = -reach; dy <= reach; ++dy) {
                  for(int dz = -reach; dz <= reach; ++dz) {
                     int x = Mth.floor(bx) + dx;
                     int y = Mth.floor(cy) + dy;
                     int z = Mth.floor(bz) + dz;
                     double ddx = (double)x + (double)0.5F - bx;
                     double ddy = (double)y + (double)0.5F - cy;
                     double ddz = (double)z + (double)0.5F - bz;
                     if (!(ddx * ddx + ddy * ddy + ddz * ddz > 1.8225000000000002)) {
                        pos.set(x, y, z);
                        if (!level.getBlockState(pos).isAir()) {
                           level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                           ++placed;
                        }
                     }
                  }
               }
            }
         }
      }

      return placed;
   }

   private static int doorway(ServerLevel level, long seed, int fromX, int toX, boolean back) {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int reach = Mth.ceil(7.2) + 2;
      int placed = 0;

      for(int x = fromX; x <= toX; ++x) {
         double cy = back ? BowelsBackHall.axisY(seed, (double)x) : BowelsHallway.axisY(seed, (double)x);
         double cz = back ? BowelsBackHall.axisZ(seed, (double)x) : BowelsHallway.axisZ(seed, (double)x);
         double floor = back ? BowelsBackHall.floorTop(seed, (double)x, cz) : BowelsHallway.floorTop(seed, (double)x, cz);

         for(int y = Mth.floor(cy) - reach; y <= Mth.ceil(cy) + reach; ++y) {
            for(int z = Mth.floor(cz) - reach; z <= Mth.ceil(cz) + reach; ++z) {
               double dy = (double)y - cy;
               double dz = (double)z - cz;
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               double face = back ? BowelsBackHall.outerFace(seed, (double)x, turns) - (double)3.0F : BowelsHallway.outerFace(seed, (double)x, turns) - (double)3.0F;
               if (!(dy * dy + dz * dz > face * face) && !((double)y <= floor)) {
                  pos.set(x, y, z);
                  if (!level.getBlockState(pos).isAir()) {
                     level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                     ++placed;
                  }
               }
            }
         }
      }

      return placed;
   }

   private static int clearInterior(ServerLevel level, long seed) {
      int reach = Mth.ceil(23.1) + 2;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int placed = 0;
      int cx = Mth.floor((double)177.0F);
      int cz = Mth.floor((double)0.0F);

      for(int x = cx - reach; x <= cx + reach; ++x) {
         for(int z = cz - reach; z <= cz + reach; ++z) {
            double dx = (double)x - (double)177.0F;
            double dz = (double)z - (double)0.0F;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (!(r > 23.1)) {
               double turns = (Math.atan2(dz, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               int from = Mth.ceil(floorAt(seed, (double)x, (double)z)) + 1;
               int to = Mth.floor(roofAt(seed, (double)x, (double)z)) - 1;

               for(int y = from; y <= to; ++y) {
                  double h = Mth.clamp((double)(y - 60) / (double)36.0F, (double)0.0F, (double)1.0F);
                  double profile = Math.sqrt(Math.max((double)0.0F, (double)1.0F - h * h * 0.88));
                  double face = (double)21.0F * profile + 2.1 * (double)BowelsHallway.ringNoise(seed ^ 1797902671L, (double)y / (double)9.0F, turns);
                  if (!(r > face)) {
                     pos.set(x, y, z);
                     if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        ++placed;
                     }
                  }
               }
            }
         }
      }

      return placed;
   }

   private static int chamber(ServerLevel level, long seed) {
      int reach = Mth.ceil(26.1) + 2;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 1797902671L);
      int placed = 0;
      int cx = Mth.floor((double)177.0F);
      int cz = Mth.floor((double)0.0F);

      for(int x = cx - reach; x <= cx + reach; ++x) {
         for(int z = cz - reach; z <= cz + reach; ++z) {
            double dx = (double)x - (double)177.0F;
            double dz = (double)z - (double)0.0F;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (!(r > 26.1)) {
               double turns = (Math.atan2(dz, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;
               double ground = floorAt(seed, (double)x, (double)z);
               double roof = roofAt(seed, (double)x, (double)z);

               for(int y = 57; y <= 99; ++y) {
                  double h = Mth.clamp((double)(y - 60) / (double)36.0F, (double)0.0F, (double)1.0F);
                  double profile = Math.sqrt(Math.max((double)0.0F, (double)1.0F - h * h * 0.88));
                  double face = (double)21.0F * profile + 2.1 * (double)BowelsHallway.ringNoise(seed ^ 1797902671L, (double)y / (double)9.0F, turns);
                  double shaftFace = (double)3.5F + 0.6 * (double)BowelsHallway.ringNoise(seed ^ 2742967426L, (double)y / (double)7.0F, turns);
                  BlockState state;
                  if ((double)y <= ground) {
                     if (r > face + (double)3.0F) {
                        continue;
                     }

                     state = BowelsHallway.floorSkin(seed, rng, x, y, z);
                  } else if (y <= 96 && (double)y >= roof) {
                     if (r <= shaftFace) {
                        pos.set(x, y, z);
                        if (!level.getBlockState(pos).isAir()) {
                           level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                           ++placed;
                        }
                        continue;
                     }

                     if (r > face + (double)3.0F) {
                        continue;
                     }

                     state = BowelsHallway.floorSkin(seed, rng, x, y, z);
                  } else if (y <= 96) {
                     if (r <= face) {
                        if (x <= 163 || x >= 182) {
                           pos.set(x, y, z);
                           if (!level.getBlockState(pos).isAir()) {
                              level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                              ++placed;
                           }
                        }
                        continue;
                     }

                     if (r > face + (double)3.0F) {
                        continue;
                     }

                     state = BowelsHallway.wallSkin(seed, rng, x, y, z);
                  } else {
                     if (r <= shaftFace || r > face + (double)3.0F) {
                        continue;
                     }

                     state = BowelsHallway.wallSkin(seed, rng, x, y, z);
                  }

                  pos.set(x, y, z);
                  level.setBlock(pos, state, 2);
                  ++placed;
               }
            }
         }
      }

      return placed;
   }

   private static int shaft(ServerLevel level, long seed) {
      int reach = Mth.ceil(7.1) + 2;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 2742967426L);
      int placed = 0;
      int cx = Mth.floor((double)177.0F);
      int cz = Mth.floor((double)0.0F);

      for(int x = cx - reach; x <= cx + reach; ++x) {
         for(int z = cz - reach; z <= cz + reach; ++z) {
            double dx = (double)x - (double)177.0F;
            double dz = (double)z - (double)0.0F;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (!(r > 7.1)) {
               double turns = (Math.atan2(dz, dx) / (Math.PI * 2D) + (double)1.0F) % (double)1.0F;

               for(int y = 100; y <= 171; ++y) {
                  double face = (double)3.5F + 0.6 * (double)BowelsHallway.ringNoise(seed ^ 2742967426L, (double)y / (double)7.0F, turns);
                  boolean capping = y > 168;
                  if ((capping || !(r <= face)) && !(r > face + (double)3.0F)) {
                     pos.set(x, y, z);
                     level.setBlock(pos, BowelsHallway.wallSkin(seed, rng, x, y, z), 2);
                     ++placed;
                  }
               }
            }
         }
      }

      return placed;
   }
}
