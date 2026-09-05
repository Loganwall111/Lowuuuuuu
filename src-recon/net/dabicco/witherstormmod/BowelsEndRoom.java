package net.dabicco.witherstormmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
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
   public static final double ROOM_R = 21.0;
   private static final double ROOM_RAGGED = 2.1;
   public static final double ROOM_X = 177.0;
   public static final double ROOM_Z = 0.0;
   public static final int FLOOR_Y = 60;
   public static final int CEIL_Y = 96;
   private static final double BOWL = 3.0;
   private static final double FLOOR_ROUGH = 0.8;
   private static final double CEIL_BED = 3.0;
   private static final long SALT_FLOOR = 3290067942L;
   private static final long SALT_ROOF = 802732293L;
   private static final long SALT_FEATURE = 2120458589L;
   private static final double FLAT_R = 14.0;
   private static final double FLAT_FADE = 4.5;
   private static final double APPROACH_HALF_WIDTH = 10.0;
   private static final double SHAFT_R = 3.5;
   private static final double SHAFT_RAGGED = 0.6;
   private static final int SHAFT_HEIGHT = 72;
   public static final int SHAFT_TOP = 168;
   public static final int EJECT_Y = 132;
   private static final int WALL = 3;
   private static final double INFLUENCE = 31.1;
   private static final long SALT_WALL = 1797902671L;
   private static final long SALT_SHAFT = 2742967426L;
   public static final double PLATTER_R = 4.5;
   private static final double PLATTER_RAGGED = 0.5;
   private static final double MID_R = 2.3;
   private static final double MID_RAGGED = 0.26;
   private static final double PLUS_ARM = 1.5;
   private static final double PLUS_HALF = 0.5;
   public static final int DAIS_H = 3;
   private static final double GUARD_BURY = 0.65;
   private static final int PEDESTAL_BASE = 1;
   public static final int PEDESTAL_RISE = 4;
   private static final double[] SAND_R = new double[]{0.0, 5.5, 6.2, 6.9};
   private static final double SAND_RAGGED = 0.5;
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
   public static final double HOLE_BACK = 9.0;
   private static final double HOLE_DEPTH = 10.0;
   private static final double DOOR_R = 7.2;

   private BowelsEndRoom() {
   }

   private static double floorAt(long seed, double x, double z) {
      double dx = x - 177.0;
      double dz = z - 0.0;
      double r = Math.min(Math.sqrt(dx * dx + dz * dz) / 21.0, 1.0);
      double rough = BowelsHallway.noise(seed ^ 3290067942L, x / 8.0, z / 8.0);
      double lift = r * r * r * r;
      double level = approachLevel(x, z);
      double middle = flatMiddle(x, z);
      double top = 60.0 + 3.0 * lift * level * middle + 0.8 * rough * (0.35 + 0.65 * lift) * level * middle;
      return Math.max(top, 60.0);
   }

   private static double flatMiddle(double x, double z) {
      double dx = x - 177.0;
      double dz = z - 0.0;
      double r = Math.sqrt(dx * dx + dz * dz);
      return r <= 14.0 ? 0.0 : Mth.clamp((r - 14.0) / 4.5, 0.0, 1.0);
   }

   private static double approachLevel(double x, double z) {
      double across = Math.abs(z - 0.0) / 10.0;
      if (across >= 1.0) {
         return 1.0;
      } else {
         double along = Math.min(Math.abs(x - 177.0) / 21.0, 1.0);
         if (along <= 0.45) {
            return 1.0;
         } else {
            double reach = Mth.clamp((along - 0.45) / 0.55, 0.0, 1.0);
            double band = 1.0 - across * across;
            return 1.0 - reach * band;
         }
      }
   }

   private static double roofAt(long seed, double x, double z) {
      double dx = x - 177.0;
      double dz = z - 0.0;
      double r = Math.min(Math.sqrt(dx * dx + dz * dz) / 21.0, 1.0);
      double rough = BowelsHallway.noise(seed ^ 802732293L, x / 8.0, z / 8.0);
      return 93.0 - 3.0 * r * r * 0.5 - 0.8 * rough;
   }

   static double outerFace(long seed, double y, double turns) {
      double h = Mth.clamp((y - 60.0) / 36.0, 0.0, 1.0);
      double profile = Math.sqrt(Math.max(0.0, 1.0 - h * h * 0.88));
      return 21.0 * profile + 2.1 * BowelsHallway.ringNoise(seed ^ 1797902671L, y / 9.0, turns) + 3.0;
   }

   static double shaftFace(long seed, double y, double turns) {
      return 3.5 + 0.6 * BowelsHallway.ringNoise(seed ^ 2742967426L, y / 7.0, turns) + 3.0;
   }

   public static boolean holds(double x, double y, double z) {
      if (!(y < 52.0) && !(y > 176.0)) {
         double dx = x - 177.0;
         double dz = z - 0.0;
         return dx * dx + dz * dz < 967.21;
      } else {
         return false;
      }
   }

   public static double standAt(long seed, double x, double z) {
      return !BowelsFlip.flipped() ? floorAt(seed, x, z) + 1.5 : roofAt(seed, x, z) - 3.0;
   }

   public static double holeClearance() {
      return 5.6;
   }

   public static Direction pull(double y) {
      return BowelsFlip.flipped() ? Direction.UP : Direction.DOWN;
   }

   public static boolean atEjectPoint(double x, double y, double z) {
      if (y < 132.0) {
         return false;
      } else {
         double dx = x - 177.0;
         double dz = z - 0.0;
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
               toX = storm.getX() + 64.0;
               toZ = storm.getZ();
            } else {
               BlockPos spawn = overworld.getRespawnData().pos();
               toX = spawn.getX() + 0.5;
               toZ = spawn.getZ() + 0.5;
            }

            int bx = Mth.floor(toX);
            int bz = Mth.floor(toZ);
            overworld.getChunk(bx >> 4, bz >> 4);
            int surface = overworld.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
            float yaw = storm == null ? player.getYRot() : (float)(-Math.toDegrees(Math.atan2(storm.getX() - toX, storm.getZ() - toZ)));
            BowelsGravity.release(player);
            player.resetFallDistance();
            player.setDeltaMovement(Vec3.ZERO);
            player.teleportTo(overworld, toX, surface, toZ, Set.of(), yaw, 0.0F, false);
            player.sendSystemMessage(Component.literal("The storm spits you out."));
         }
      }
   }

   private static Entity nearestStorm(ServerLevel overworld) {
      for (Entity entity : overworld.getAllEntities()) {
         if (entity instanceof WitherStormEntity) {
            return entity;
         }
      }

      return null;
   }

   public static int build(ServerLevel level, long seed) {
      return clearInterior(level, seed)
         + chamber(level, seed)
         + shaft(level, seed)
         + features(level, seed)
         + pedestal(level, seed)
         + tentacleHoles(level, seed)
         + doorway(level, seed, 151, Mth.floor(156.0) + 10, false)
         + doorway(level, seed, Mth.floor(198.0) - 8, Mth.ceil(198.0) + 3 + 6, true);
   }

   private static int features(ServerLevel level, long seed) {
      RandomSource rng = RandomSource.create(seed ^ 2120458589L);
      int placed = 0;

      for (int i = 0; i < 9; i++) {
         double angle = rng.nextDouble() * Math.PI * 2.0;
         double at = (0.3 + rng.nextDouble() * 0.6) * 21.0;
         double x = 177.0 + Math.cos(angle) * at;
         double z = 0.0 + Math.sin(angle) * at;
         double roof = roofAt(seed, x, z);
         double drop = 2.0 + rng.nextDouble() * 5.0;

         for (double y = roof; y >= roof - drop; y -= 0.8) {
            double taper = 1.0 - (roof - y) / (drop + 1.0);
            placed += BowelsHallway.blob(level, rng, x, y, z, 1.4 * taper + 0.4, ModBlocks.WITHERED_FLESH_BLOCK.defaultBlockState());
         }
      }

      return placed;
   }

   public static int platterTopY() {
      return 62;
   }

   public static double guardMountY() {
      return platterTopY() - 0.65;
   }

   public static boolean onPedestal(double x, double y, double z) {
      if (!(y < 61.0) && !(y > 71.0)) {
         double dx = x - 177.0;
         double dz = z - 0.0;
         double reach = 5.5;
         return dx * dx + dz * dz < reach * reach;
      } else {
         return false;
      }
   }

   public static Vec3 daisTop() {
      return new Vec3(Math.floor(177.0) + 0.5, 64.0, Math.floor(0.0) + 0.5);
   }

   private static int pedestal(ServerLevel level, long seed) {
      MutableBlockPos pos = new MutableBlockPos();
      int reach = Mth.ceil(4.5) + 2;
      int placed = 0;
      BlockPos origin = pedestalOrigin();
      int cx = origin.getX();
      int cz = origin.getZ();
      placed += writePedestal(level, seed, 0);

      for (int x = cx - reach; x <= cx + reach; x++) {
         for (int z = cz - reach; z <= cz + reach; z++) {
            for (int y = 64; y <= 72; y++) {
               pos.set(x, y, z);
               if (!level.getBlockState(pos).isAir()) {
                  level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                  placed++;
               }
            }
         }
      }

      return placed;
   }

   public static BlockPos pedestalOrigin() {
      return new BlockPos(Mth.floor(177.0), 60, Mth.floor(0.0));
   }

   public static void collectPedestal(long seed, List<BlockPos> offsets, List<BlockState> states) {
      int reach = Mth.ceil(5.0) + 1;
      BlockPos origin = pedestalOrigin();

      for (int step = 0; step < 3; step++) {
         for (int dxi = -reach; dxi <= reach; dxi++) {
            for (int dzi = -reach; dzi <= reach; dzi++) {
               double dx = dxi;
               double dz = dzi;
               if (inTier(seed, dx, dz, step)) {
                  offsets.add(new BlockPos(dxi, 1 + step, dzi));
                  states.add(daisBlock(origin.getX() + dxi, 61 + step, origin.getZ() + dzi, step));
               }
            }
         }
      }
   }

   public static int writePedestal(ServerLevel level, long seed, int lift) {
      List<BlockPos> offsets = new ArrayList<>();
      List<BlockState> states = new ArrayList<>();
      collectPedestal(seed, offsets, states);
      BlockPos origin = pedestalOrigin();
      MutableBlockPos pos = new MutableBlockPos();

      for (int i = 0; i < offsets.size(); i++) {
         BlockPos off = offsets.get(i);
         pos.set(origin.getX() + off.getX(), origin.getY() + off.getY() + lift, origin.getZ() + off.getZ());
         level.setBlock(pos, states.get(i), 2);
      }

      return offsets.size();
   }

   public static int clearPedestal(ServerLevel level, long seed, int lift) {
      List<BlockPos> offsets = new ArrayList<>();
      List<BlockState> states = new ArrayList<>();
      collectPedestal(seed, offsets, states);
      BlockPos origin = pedestalOrigin();
      MutableBlockPos pos = new MutableBlockPos();

      for (BlockPos off : offsets) {
         pos.set(origin.getX() + off.getX(), origin.getY() + off.getY() + lift, origin.getZ() + off.getZ());
         level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
      }

      return offsets.size();
   }

   private static boolean inTier(long seed, double dx, double dz, int step) {
      double r = Math.sqrt(dx * dx + dz * dz);
      double turns = (Math.atan2(dz, dx) / (Math.PI * 2) + 1.0) % 1.0;

      return switch (step) {
         case 0 -> r <= 4.5 + 0.5 * BowelsHallway.ringNoise(seed ^ 1370539945L, 1.7, turns);
         case 1 -> r <= 2.3 + 0.26 * BowelsHallway.ringNoise(seed ^ 1370539945L, 5.3, turns);
         default -> Math.abs(dx) <= 0.5 && Math.abs(dz) <= 1.5 || Math.abs(dz) <= 0.5 && Math.abs(dx) <= 1.5;
      };
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
         return 4.5 + 0.5 * BowelsHallway.ringNoise(seed ^ 1370539945L, 1.7, turns);
      } else {
         double face = SAND_R[layer] + 0.5 * BowelsHallway.ringNoise(seed ^ 1014634980L, 3.1 + layer * 4.7, turns);
         if (layer > 0) {
            double lump = BowelsHallway.ringNoise(seed ^ 2637063351L, layer * 9.3, turns * 4.0);
            if (lump > 0.55) {
               face += 1.15 * (lump - 0.55) / 0.44999999999999996;
            }
         }

         return face;
      }
   }

   private static boolean sandStair(long seed, double turns, int layer) {
      return layer == 0 ? false : BowelsHallway.ringNoise(seed ^ 1492323489L, layer * 6.1, turns * 3.0) > 0.42;
   }

   public static void collectSand(long seed, List<BlockPos> offsets, List<BlockState> states) {
      int reach = Mth.ceil(SAND_R[3] + 0.5 + 1.15) + 1;
      BlockPos origin = pedestalOrigin();

      for (int layer = 0; layer < 4; layer++) {
         for (int dxi = -reach; dxi <= reach; dxi++) {
            for (int dzi = -reach; dzi <= reach; dzi++) {
               double dx = dxi;
               double dz = dzi;
               double r = Math.sqrt(dx * dx + dz * dz);
               double turns = (Math.atan2(dz, dx) / (Math.PI * 2) + 1.0) % 1.0;
               double face = sandFaceAt(seed, turns, layer);
               if (!(r > face)) {
                  boolean rim = r > face - 1.0;
                  BlockState state = rim && sandStair(seed, turns, layer) ? stepOut(dx, dz) : ModBlocks.WITHERED_SAND.defaultBlockState();
                  offsets.add(new BlockPos(dxi, sandLayerY(layer) - 60, dzi));
                  states.add(state);
               }
            }
         }
      }
   }

   public static int writeSand(ServerLevel level, long seed) {
      List<BlockPos> offsets = new ArrayList<>();
      List<BlockState> states = new ArrayList<>();
      collectSand(seed, offsets, states);
      BlockPos origin = pedestalOrigin();
      MutableBlockPos pos = new MutableBlockPos();

      for (int i = 0; i < offsets.size(); i++) {
         BlockPos off = offsets.get(i);
         pos.set(origin.getX() + off.getX(), origin.getY() + off.getY(), origin.getZ() + off.getZ());
         level.setBlock(pos, states.get(i), 2);
      }

      return offsets.size();
   }

   private static BlockState stepOut(double dx, double dz) {
      Direction inward = Math.abs(dx) >= Math.abs(dz) ? (dx >= 0.0 ? Direction.WEST : Direction.EAST) : (dz >= 0.0 ? Direction.NORTH : Direction.SOUTH);
      return (BlockState)ModBlocks.WITHERED_STAIRS.defaultBlockState().setValue(StairBlock.FACING, inward);
   }

   public static Vec3 holeForward(int index) {
      double angle = holeAngle(index);
      return new Vec3(-Math.cos(angle), 0.0, -Math.sin(angle));
   }

   public static Vec3 holeMouth(long seed, int index) {
      double angle = holeAngle(index);
      double faceR = 20.0;
      double fx = 177.0 + Math.cos(angle) * faceR;
      double fz = 0.0 + Math.sin(angle) * faceR;
      return new Vec3(fx, floorAt(seed, fx, fz) + 2.0, fz);
   }

   public static double holeAngle(int index) {
      int perSide = Math.max(1, 6);
      int side = index / perSide;
      int within = index % perSide;
      double from = Math.toRadians(28.0);
      double to = Math.toRadians(152.0);
      double at = from + (to - from) * (within + 0.5) / perSide;
      return side == 0 ? at : at + Math.PI;
   }

   public static float holeYaw(int index) {
      return (float)Math.toDegrees(holeAngle(index)) + 90.0F;
   }

   private static int tentacleHoles(ServerLevel level, long seed) {
      MutableBlockPos pos = new MutableBlockPos();
      int placed = 0;

      for (int i = 0; i < 12; i++) {
         double angle = holeAngle(i);
         double ox = Math.cos(angle);
         double oz = Math.sin(angle);
         double cy = holeMouth(seed, i).y;

         for (double along = 18.0; along <= 30.0; along += 0.5) {
            double bx = 177.0 + ox * along;
            double bz = 0.0 + oz * along;
            int reach = Mth.ceil(1.35) + 1;

            for (int dx = -reach; dx <= reach; dx++) {
               for (int dy = -reach; dy <= reach; dy++) {
                  for (int dz = -reach; dz <= reach; dz++) {
                     int x = Mth.floor(bx) + dx;
                     int y = Mth.floor(cy) + dy;
                     int z = Mth.floor(bz) + dz;
                     double ddx = x + 0.5 - bx;
                     double ddy = y + 0.5 - cy;
                     double ddz = z + 0.5 - bz;
                     if (!(ddx * ddx + ddy * ddy + ddz * ddz > 1.8225000000000002)) {
                        pos.set(x, y, z);
                        if (!level.getBlockState(pos).isAir()) {
                           level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                           placed++;
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
      MutableBlockPos pos = new MutableBlockPos();
      int reach = Mth.ceil(7.2) + 2;
      int placed = 0;

      for (int x = fromX; x <= toX; x++) {
         double cy = back ? BowelsBackHall.axisY(seed, x) : BowelsHallway.axisY(seed, x);
         double cz = back ? BowelsBackHall.axisZ(seed, x) : BowelsHallway.axisZ(seed, x);
         double floor = back ? BowelsBackHall.floorTop(seed, x, cz) : BowelsHallway.floorTop(seed, x, cz);

         for (int y = Mth.floor(cy) - reach; y <= Mth.ceil(cy) + reach; y++) {
            for (int z = Mth.floor(cz) - reach; z <= Mth.ceil(cz) + reach; z++) {
               double dy = y - cy;
               double dz = z - cz;
               double turns = (Math.atan2(dy, dz) / (Math.PI * 2) + 1.0) % 1.0;
               double face = back ? BowelsBackHall.outerFace(seed, x, turns) - 3.0 : BowelsHallway.outerFace(seed, x, turns) - 3.0;
               if (!(dy * dy + dz * dz > face * face) && !(y <= floor)) {
                  pos.set(x, y, z);
                  if (!level.getBlockState(pos).isAir()) {
                     level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                     placed++;
                  }
               }
            }
         }
      }

      return placed;
   }

   private static int clearInterior(ServerLevel level, long seed) {
      int reach = Mth.ceil(23.1) + 2;
      MutableBlockPos pos = new MutableBlockPos();
      int placed = 0;
      int cx = Mth.floor(177.0);
      int cz = Mth.floor(0.0);

      for (int x = cx - reach; x <= cx + reach; x++) {
         for (int z = cz - reach; z <= cz + reach; z++) {
            double dx = x - 177.0;
            double dz = z - 0.0;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (!(r > 23.1)) {
               double turns = (Math.atan2(dz, dx) / (Math.PI * 2) + 1.0) % 1.0;
               int from = Mth.ceil(floorAt(seed, x, z)) + 1;
               int to = Mth.floor(roofAt(seed, x, z)) - 1;

               for (int y = from; y <= to; y++) {
                  double h = Mth.clamp((y - 60) / 36.0, 0.0, 1.0);
                  double profile = Math.sqrt(Math.max(0.0, 1.0 - h * h * 0.88));
                  double face = 21.0 * profile + 2.1 * BowelsHallway.ringNoise(seed ^ 1797902671L, y / 9.0, turns);
                  if (!(r > face)) {
                     pos.set(x, y, z);
                     if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        placed++;
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
      MutableBlockPos pos = new MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 1797902671L);
      int placed = 0;
      int cx = Mth.floor(177.0);
      int cz = Mth.floor(0.0);

      for (int x = cx - reach; x <= cx + reach; x++) {
         for (int z = cz - reach; z <= cz + reach; z++) {
            double dx = x - 177.0;
            double dz = z - 0.0;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (!(r > 26.1)) {
               double turns = (Math.atan2(dz, dx) / (Math.PI * 2) + 1.0) % 1.0;
               double ground = floorAt(seed, x, z);
               double roof = roofAt(seed, x, z);

               for (int y = 57; y <= 99; y++) {
                  double h = Mth.clamp((y - 60) / 36.0, 0.0, 1.0);
                  double profile = Math.sqrt(Math.max(0.0, 1.0 - h * h * 0.88));
                  double face = 21.0 * profile + 2.1 * BowelsHallway.ringNoise(seed ^ 1797902671L, y / 9.0, turns);
                  double shaftFace = 3.5 + 0.6 * BowelsHallway.ringNoise(seed ^ 2742967426L, y / 7.0, turns);
                  BlockState state;
                  if (y <= ground) {
                     if (r > face + 3.0) {
                        continue;
                     }

                     state = BowelsHallway.floorSkin(seed, rng, x, y, z);
                  } else if (y <= 96 && y >= roof) {
                     if (r <= shaftFace) {
                        pos.set(x, y, z);
                        if (!level.getBlockState(pos).isAir()) {
                           level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                           placed++;
                        }
                        continue;
                     }

                     if (r > face + 3.0) {
                        continue;
                     }

                     state = BowelsHallway.floorSkin(seed, rng, x, y, z);
                  } else if (y <= 96) {
                     if (r <= face) {
                        if (x <= 163 || x >= 182) {
                           pos.set(x, y, z);
                           if (!level.getBlockState(pos).isAir()) {
                              level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                              placed++;
                           }
                        }
                        continue;
                     }

                     if (r > face + 3.0) {
                        continue;
                     }

                     state = BowelsHallway.wallSkin(seed, rng, x, y, z);
                  } else {
                     if (r <= shaftFace || r > face + 3.0) {
                        continue;
                     }

                     state = BowelsHallway.wallSkin(seed, rng, x, y, z);
                  }

                  pos.set(x, y, z);
                  level.setBlock(pos, state, 2);
                  placed++;
               }
            }
         }
      }

      return placed;
   }

   private static int shaft(ServerLevel level, long seed) {
      int reach = Mth.ceil(7.1) + 2;
      MutableBlockPos pos = new MutableBlockPos();
      RandomSource rng = RandomSource.create(seed ^ 2742967426L);
      int placed = 0;
      int cx = Mth.floor(177.0);
      int cz = Mth.floor(0.0);

      for (int x = cx - reach; x <= cx + reach; x++) {
         for (int z = cz - reach; z <= cz + reach; z++) {
            double dx = x - 177.0;
            double dz = z - 0.0;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (!(r > 7.1)) {
               double turns = (Math.atan2(dz, dx) / (Math.PI * 2) + 1.0) % 1.0;

               for (int y = 100; y <= 171; y++) {
                  double face = 3.5 + 0.6 * BowelsHallway.ringNoise(seed ^ 2742967426L, y / 7.0, turns);
                  boolean capping = y > 168;
                  if ((capping || !(r <= face)) && !(r > face + 3.0)) {
                     pos.set(x, y, z);
                     level.setBlock(pos, BowelsHallway.wallSkin(seed, rng, x, y, z), 2);
                     placed++;
                  }
               }
            }
         }
      }

      return placed;
   }
}
