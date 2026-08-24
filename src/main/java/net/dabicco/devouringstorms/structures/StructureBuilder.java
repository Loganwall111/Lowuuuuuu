package net.dabicco.devouringstorms.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Procedural Story-Mode inspired buildings, generated from code so no Blockbench
 * or .nbt structure files are required. These give the storm real backdrop towns
 * to hunt and level, including an underground space beneath towns.
 */
public final class StructureBuilder {
   private StructureBuilder() {
   }

   /**
    * Build one of: beacon | house | portal | church | town | endertown |
    * undertown | under_town | watchtower | courtyard | street | market | farm.
    */
   public static int build(ServerLevel server, BlockPos origin, String type) {
      return switch (type) {
         case "beacon" -> beacon(server, origin);
         case "house" -> house(server, origin);
         case "portal" -> portalRuin(server, origin);
         case "church" -> church(server, origin);
         case "town", "endertown" -> town(server, origin);
         case "undertown", "under_town" -> underTown(server, origin);
         case "watchtower" -> watchtower(server, origin);
         case "courtyard" -> courtyard(server, origin);
         case "street" -> street(server, origin);
         case "market" -> market(server, origin);
         case "farm" -> farm(server, origin);
         default -> 0;
      };
   }

   private static void set(ServerLevel server, BlockPos pos, BlockState state) {
      server.setBlock(pos, state, 2);
   }

   private static int town(ServerLevel server, BlockPos o) {
      int n = 0;
      n += roadCross(server, o);
      n += well(server, o);
      int[][] homes = new int[][]{{14, 0}, {-14, 0}, {0, 14}, {0, -14}, {10, 10}, {-10, -10}, {10, -10}, {-10, 10}};

      for (int[] v : homes) {
         n += house(server, o.offset(v[0], 0, v[1]));
      }

      n += church(server, o.offset(18, 0, 10));
      n += beacon(server, o.offset(-18, 0, 12));
      n += portalRuin(server, o.offset(18, 0, -16));
      n += watchtower(server, o.offset(-22, 0, -20));
      n += courtyard(server, o.offset(-20, 0, 22));
      n += market(server, o.offset(24, 0, 2));
      n += farm(server, o.offset(-26, 0, 4));
      n += underTown(server, o);
      return n;
   }

   private static int roadCross(ServerLevel s, BlockPos o) {
      int n = 0;

      for (int i = -26; i <= 26; i++) {
         for (int w = -1; w <= 1; w++) {
            set(s, o.offset(i, 0, w), Blocks.DIRT_PATH.defaultBlockState());
            set(s, o.offset(w, 0, i), Blocks.DIRT_PATH.defaultBlockState());
            n += 2;
         }
      }

      return n;
   }

   private static int street(ServerLevel s, BlockPos o) {
      int n = 0;

      for (int x = -16; x <= 16; x++) {
         for (int z = -2; z <= 2; z++) {
            set(s, o.offset(x, 0, z), Math.abs(z) == 2 ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState());
            n++;
         }
      }

      n += watchtower(s, o.offset(-14, 0, 8));
      n += house(s, o.offset(10, 0, -10));
      return n;
   }

   private static int courtyard(ServerLevel s, BlockPos o) {
      int n = 0;

      for (int x = -5; x <= 5; x++) {
         for (int z = -5; z <= 5; z++) {
            boolean edge = Math.abs(x) == 5 || Math.abs(z) == 5;
            set(s, o.offset(x, 0, z), edge ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState());
            n++;
         }
      }

      for (int y = 1; y <= 3; y++) {
         set(s, o.offset(-5, y, -5), Blocks.STONE_BRICK_WALL.defaultBlockState());
         set(s, o.offset(5, y, -5), Blocks.STONE_BRICK_WALL.defaultBlockState());
         set(s, o.offset(-5, y, 5), Blocks.STONE_BRICK_WALL.defaultBlockState());
         set(s, o.offset(5, y, 5), Blocks.STONE_BRICK_WALL.defaultBlockState());
         n += 4;
      }

      set(s, o.offset(0, 1, 0), Blocks.SOUL_LANTERN.defaultBlockState());
      return n + 1;
   }

   private static int well(ServerLevel s, BlockPos o) {
      int n = 0;

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            if (Math.abs(x) == 2 || Math.abs(z) == 2) {
               set(s, o.offset(x, 0, z), Blocks.COBBLESTONE.defaultBlockState());
               n++;
            }
         }
      }

      set(s, o, Blocks.WATER.defaultBlockState());
      return n + 1;
   }

   private static int market(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -5; x <= 5; x++) {
         for (int z = -4; z <= 4; z++) {
            set(server, origin.offset(x, 0, z), Math.abs(x) == 5 || Math.abs(z) == 4 ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState());
            placed++;
         }
      }

      int[][] stalls = new int[][]{{-3, -1}, {0, 2}, {3, -1}};
      BlockState[] cloth = new BlockState[]{Blocks.RED_WOOL.defaultBlockState(), Blocks.YELLOW_WOOL.defaultBlockState(), Blocks.BLUE_WOOL.defaultBlockState()};

      for (int i = 0; i < stalls.length; i++) {
         int sx = stalls[i][0];
         int sz = stalls[i][1];

         set(server, origin.offset(sx - 1, 1, sz), Blocks.OAK_FENCE.defaultBlockState());
         set(server, origin.offset(sx + 1, 1, sz), Blocks.OAK_FENCE.defaultBlockState());
         set(server, origin.offset(sx - 1, 2, sz), cloth[i]);
         set(server, origin.offset(sx, 2, sz), cloth[i]);
         set(server, origin.offset(sx + 1, 2, sz), cloth[i]);
         set(server, origin.offset(sx, 1, sz), Blocks.BARREL.defaultBlockState());
         placed += 6;
      }

      set(server, origin.offset(-4, 1, 3), Blocks.CHEST.defaultBlockState());
      set(server, origin.offset(4, 1, 3), Blocks.CRAFTING_TABLE.defaultBlockState());
      return placed + 2;
   }

   private static int farm(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -5; x <= 5; x++) {
         for (int z = -4; z <= 4; z++) {
            BlockPos pos = origin.offset(x, 0, z);
            if (Math.abs(x) == 5 || Math.abs(z) == 4) {
               set(server, pos, Blocks.OAK_LOG.defaultBlockState());
            } else if (x == 0) {
               set(server, pos, Blocks.WATER.defaultBlockState());
            } else {
               set(server, pos, Blocks.FARMLAND.defaultBlockState());
               set(server, pos.above(), ((BlockState)Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7)));
               placed += 2;
               continue;
            }

            placed++;
         }
      }

      set(server, origin.offset(0, 1, -4), Blocks.OAK_FENCE.defaultBlockState());
      set(server, origin.offset(0, 1, 4), Blocks.OAK_FENCE.defaultBlockState());
      return placed + 2;
   }

   private static int underTown(ServerLevel server, BlockPos origin) {
      int placed = 0;
      BlockPos floor = origin.below(7);

      for (int y = 0; y <= 4; y++) {
         set(server, origin.below(y), y == 4 ? Blocks.IRON_BARS.defaultBlockState() : Blocks.AIR.defaultBlockState());
         placed++;
      }

      for (int x = -5; x <= 5; x++) {
         for (int z = -7; z <= 7; z++) {
            for (int y = 0; y <= 4; y++) {
               boolean shell = x == -5 || x == 5 || z == -7 || z == 7 || y == 0 || y == 4;
               BlockState state = shell ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState();
               set(server, floor.offset(x, y, z), state);
               placed++;
            }
         }
      }

      for (int z = -7; z <= 7; z++) {
         set(server, floor.offset(0, 0, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
         placed++;
      }

      for (int y = 1; y <= 3; y++) {
         set(server, floor.offset(-5, y, 0), Blocks.IRON_BARS.defaultBlockState());
         set(server, floor.offset(5, y, 0), Blocks.IRON_BARS.defaultBlockState());
         placed += 2;
      }

      set(server, floor.offset(-3, 1, -4), Blocks.CRAFTING_TABLE.defaultBlockState());
      set(server, floor.offset(3, 1, -4), Blocks.CHEST.defaultBlockState());
      set(server, floor.offset(-3, 1, 4), Blocks.BARREL.defaultBlockState());
      set(server, floor.offset(3, 1, 4), Blocks.FLETCHING_TABLE.defaultBlockState());
      set(server, floor.offset(0, 3, -5), Blocks.SOUL_LANTERN.defaultBlockState());
      set(server, floor.offset(0, 3, 5), Blocks.SOUL_LANTERN.defaultBlockState());
      placed += 6;
      return placed;
   }

   private static int beacon(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            set(server, origin.offset(x, 0, z), Blocks.SANDSTONE.defaultBlockState());
            placed++;
         }
      }

      for (int y = 1; y <= 8; y++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               BlockState st = x == 0 && z == 0 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.CUT_SANDSTONE.defaultBlockState();
               set(server, origin.offset(x, y, z), st);
               placed++;
            }
         }
      }

      set(server, origin.offset(0, 9, 0), Blocks.SEA_LANTERN.defaultBlockState());
      set(server, origin.offset(0, 10, 0), Blocks.GLASS.defaultBlockState());
      placed += 2;
      return placed;
   }

   private static int watchtower(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            set(server, origin.offset(x, 0, z), Blocks.COBBLESTONE.defaultBlockState());
            placed++;
         }
      }

      for (int y = 1; y <= 9; y++) {
         set(server, origin.offset(-2, y, -2), Blocks.OAK_LOG.defaultBlockState());
         set(server, origin.offset(2, y, -2), Blocks.OAK_LOG.defaultBlockState());
         set(server, origin.offset(-2, y, 2), Blocks.OAK_LOG.defaultBlockState());
         set(server, origin.offset(2, y, 2), Blocks.OAK_LOG.defaultBlockState());
         placed += 4;
      }

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            set(server, origin.offset(x, 10, z), Blocks.OAK_PLANKS.defaultBlockState());
            placed++;
         }
      }

      set(server, origin.offset(0, 11, 0), Blocks.SOUL_LANTERN.defaultBlockState());
      return placed + 1;
   }

   private static int house(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            if (Math.abs(x) == 3 || Math.abs(z) == 3) {
               for (int y = 0; y < 3; y++) {
                  set(server, origin.offset(x, y, z), Blocks.COBBLESTONE.defaultBlockState());
                  placed++;
               }
            } else {
               set(server, origin.offset(x, 0, z), Blocks.COBBLESTONE.defaultBlockState());
               placed++;
            }
         }
      }

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            set(server, origin.offset(x, 3, z), Blocks.OAK_PLANKS.defaultBlockState());
            placed++;
         }
      }

      set(server, origin.offset(0, 1, 3), Blocks.AIR.defaultBlockState());
      set(server, origin.offset(0, 2, 3), Blocks.AIR.defaultBlockState());
      placed += 2;
      return placed;
   }

   private static int portalRuin(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int y = 0; y < 5; y++) {
         set(server, origin.offset(-2, y, 0), Blocks.OBSIDIAN.defaultBlockState());
         set(server, origin.offset(2, y, 0), Blocks.OBSIDIAN.defaultBlockState());
         placed += 2;
      }

      for (int x = -2; x <= 2; x++) {
         set(server, origin.offset(x, 0, 0), Blocks.OBSIDIAN.defaultBlockState());
         set(server, origin.offset(x, 4, 0), Blocks.OBSIDIAN.defaultBlockState());
         placed += 2;
      }

      set(server, origin.offset(-1, 1, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState());
      set(server, origin.offset(1, 1, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState());
      set(server, origin.offset(0, 1, 0), Blocks.LAVA.defaultBlockState());
      placed += 3;
      return placed;
   }

   private static int church(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -4; x <= 4; x++) {
         for (int z = -4; z <= 4; z++) {
            set(server, origin.offset(x, 0, z), Blocks.STONE_BRICKS.defaultBlockState());
            placed++;
         }
      }

      for (int x = -4; x <= 4; x++) {
         for (int z = -4; z <= 4; z++) {
            if (Math.abs(x) == 4 || Math.abs(z) == 4) {
               for (int y = 1; y <= 3; y++) {
                  set(server, origin.offset(x, y, z), Blocks.STONE_BRICKS.defaultBlockState());
                  placed++;
               }
            }
         }
      }

      for (int y = 4; y <= 6; y++) {
         int w = 6 - y;

         for (int x = -w; x <= w; x++) {
            for (int z = -w; z <= w; z++) {
               set(server, origin.offset(x, y, z), Blocks.STONE_BRICKS.defaultBlockState());
               placed++;
            }
         }
      }

      set(server, origin.offset(0, 1, 4), Blocks.AIR.defaultBlockState());
      set(server, origin.offset(0, 2, 4), Blocks.AIR.defaultBlockState());
      placed += 2;
      return placed;
   }
}
