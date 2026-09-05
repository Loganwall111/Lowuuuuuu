package net.dabicco.witherstormmod.structures;

import net.dabicco.witherstormmod.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class StructureBuilder {
   private StructureBuilder() {
   }

   public static int build(ServerLevel server, BlockPos origin, String type) {
      String var3 = type.toLowerCase();
      switch (var3) {
         case "beacon":
            return beacon(server, origin);
         case "house":
            return house(server, origin);
         case "portal":
            return portalRuin(server, origin);
         case "town":
         case "endertown":
            return town(server, origin);
         case "church":
            return church(server, origin);
         case "temple":
         case "order_temple":
            return temple(server, origin);
         case "redstonia":
         case "soren_lab":
            return redstonia(server, origin);
         case "boomtown":
         case "boom_town":
            return boomtown(server, origin);
         case "endercon":
         case "contest_stage":
            return endercon(server, origin);
         case "bunker":
         case "shelter":
            return bunker(server, origin);
         case "shrine":
         case "crater_shrine":
            return shrine(server, origin);
         case "island":
         case "floating_island":
         case "sky_island":
            return floatingIsland(server, origin);
         case "sky_fortress":
         case "soren_fortress":
         case "mob_grinder":
            return skyFortress(server, origin);
         case "farlands":
         case "far_lands":
         case "monolith":
            return farLands(server, origin);
         case "ivor_lab":
         case "potion_lab":
         case "laboratory":
            return ivorLab(server, origin);
         case "storm_nest":
         case "vortex_hive":
         case "hive":
            return stormNest(server, origin);
         case "vault":
         case "treasure_vault":
         case "order_vault":
            return treasureVault(server, origin);
         default:
            return 0;
      }
   }

   private static void set(ServerLevel server, BlockPos pos, BlockState state) {
      server.setBlock(pos, state, 2);
   }

   private static int town(ServerLevel server, BlockPos o) {
      int n = well(server, o);
      int[][] a = new int[][]{{14, 0}, {-14, 0}, {0, 14}, {0, -14}, {10, 10}, {-10, -10}};

      for (int[] v : a) {
         n += house(server, o.offset(v[0], 0, v[1]));
      }

      return n + church(server, o.offset(18, 0, 10)) + beacon(server, o.offset(-18, 0, 12)) + portalRuin(server, o.offset(18, 0, -16));
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
      return placed + 2;
   }

   private static int house(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            if (Math.abs(x) != 3 && Math.abs(z) != 3) {
               set(server, origin.offset(x, 0, z), Blocks.COBBLESTONE.defaultBlockState());
               placed++;
            } else {
               for (int y = 0; y < 3; y++) {
                  set(server, origin.offset(x, y, z), Blocks.COBBLESTONE.defaultBlockState());
                  placed++;
               }
            }
         }
      }

      for (int x = -3; x <= 3; x++) {
         for (int zx = -3; zx <= 3; zx++) {
            set(server, origin.offset(x, 3, zx), Blocks.OAK_PLANKS.defaultBlockState());
            placed++;
         }
      }

      set(server, origin.offset(0, 1, 3), Blocks.AIR.defaultBlockState());
      set(server, origin.offset(0, 2, 3), Blocks.AIR.defaultBlockState());
      return placed + 2;
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
      return placed + 3;
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
            for (int zx = -w; zx <= w; zx++) {
               set(server, origin.offset(x, y, zx), Blocks.STONE_BRICKS.defaultBlockState());
               placed++;
            }
         }
      }

      set(server, origin.offset(0, 1, 4), Blocks.AIR.defaultBlockState());
      set(server, origin.offset(0, 2, 4), Blocks.AIR.defaultBlockState());
      return placed + 2;
   }

   private static int temple(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -7; x <= 7; x++) {
         for (int z = -7; z <= 7; z++) {
            set(server, origin.offset(x, 0, z), Blocks.QUARTZ_BLOCK.defaultBlockState());
            placed++;
         }
      }

      int[][] pillars = new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}, {-6, 0}, {6, 0}, {0, -6}, {0, 6}};

      for (int[] p : pillars) {
         for (int y = 1; y <= 8; y++) {
            set(server, origin.offset(p[0], y, p[1]), Blocks.QUARTZ_PILLAR.defaultBlockState());
            placed++;
         }
      }

      for (int x = -7; x <= 7; x++) {
         for (int z = -7; z <= 7; z++) {
            if (Math.abs(x) == 7 || Math.abs(z) == 7) {
               set(server, origin.offset(x, 9, z), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
               placed++;
            }
         }
      }

      for (int y = 10; y <= 13; y++) {
         int r = 14 - y;

         for (int x = -r; x <= r; x++) {
            for (int zx = -r; zx <= r; zx++) {
               set(server, origin.offset(x, y, zx), Blocks.SMOOTH_QUARTZ.defaultBlockState());
               placed++;
            }
         }
      }

      set(server, origin.offset(0, 1, 0), Blocks.BEACON.defaultBlockState());
      set(server, origin.offset(0, 2, 0), ((Block)Blocks.STAINED_GLASS.purple()).defaultBlockState());
      set(server, origin.offset(0, 14, 0), Blocks.SEA_LANTERN.defaultBlockState());
      return placed + 3;
   }

   private static int redstonia(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -6; x <= 6; x++) {
         for (int z = -6; z <= 6; z++) {
            set(server, origin.offset(x, 0, z), Blocks.IRON_BLOCK.defaultBlockState());
            placed++;
         }
      }

      for (int y = 1; y <= 6; y++) {
         for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
               if (Math.abs(x) == 6 || Math.abs(z) == 6) {
                  BlockState st = y % 2 == 0 ? Blocks.REDSTONE_LAMP.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
                  set(server, origin.offset(x, y, z), st);
                  placed++;
               }
            }
         }
      }

      set(server, origin.offset(0, 1, 0), ModBlocks.COMMAND_CORE_BLOCK.defaultBlockState());
      set(server, origin.offset(0, 2, 0), ((Block)Blocks.LIGHTNING_ROD.weathering().unaffected()).defaultBlockState());
      return placed + 2;
   }

   private static int boomtown(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -5; x <= 5; x++) {
         for (int z = -5; z <= 5; z++) {
            set(server, origin.offset(x, 0, z), Blocks.COBBLESTONE.defaultBlockState());
            placed++;
         }
      }

      for (int x = -4; x <= 4; x += 2) {
         for (int z = -4; z <= 4; z += 2) {
            set(server, origin.offset(x, 1, z), ModBlocks.SUPER_TNT.defaultBlockState());
            set(server, origin.offset(x, 2, z), ModBlocks.WITHERED_DUST.defaultBlockState());
            placed += 2;
         }
      }

      for (int y = 1; y <= 4; y++) {
         set(server, origin.offset(-5, y, -5), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
         set(server, origin.offset(5, y, -5), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
         set(server, origin.offset(-5, y, 5), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
         set(server, origin.offset(5, y, 5), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
         placed += 4;
      }

      return placed;
   }

   private static int endercon(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -8; x <= 8; x++) {
         for (int z = -8; z <= 8; z++) {
            set(server, origin.offset(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
            placed++;
         }
      }

      for (int step = 1; step <= 4; step++) {
         int inset = 9 - step;

         for (int x = -inset; x <= inset; x++) {
            set(server, origin.offset(x, step, inset), Blocks.OAK_PLANKS.defaultBlockState());
            set(server, origin.offset(x, step, -inset), Blocks.OAK_PLANKS.defaultBlockState());
            placed += 2;
         }
      }

      set(server, origin.offset(0, 1, 0), Blocks.GOLD_BLOCK.defaultBlockState());
      set(server, origin.offset(-2, 1, 0), Blocks.IRON_BLOCK.defaultBlockState());
      set(server, origin.offset(2, 1, 0), ((Block)Blocks.COPPER_BLOCK.weathering().unaffected()).defaultBlockState());
      set(server, origin.offset(0, 2, 0), Blocks.BEACON.defaultBlockState());
      return placed + 4;
   }

   private static int bunker(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int y = -4; y <= 0; y++) {
         for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
               if (y != -4 && y != 0 && Math.abs(x) != 4 && Math.abs(z) != 4) {
                  set(server, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
               } else {
                  set(server, origin.offset(x, y, z), ModBlocks.TAINTED_OBSIDIAN.defaultBlockState());
               }

               placed++;
            }
         }
      }

      set(server, origin.offset(-2, -3, -2), Blocks.CHEST.defaultBlockState());
      set(server, origin.offset(2, -3, -2), Blocks.BREWING_STAND.defaultBlockState());
      set(server, origin.offset(0, -3, 0), Blocks.LANTERN.defaultBlockState());
      set(server, origin.offset(0, -1, 0), Blocks.IRON_TRAPDOOR.defaultBlockState());
      return placed + 4;
   }

   private static int shrine(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            set(server, origin.offset(x, 0, z), ModBlocks.TAINTED_OBSIDIAN.defaultBlockState());
            placed++;
         }
      }

      for (int y = 1; y <= 4; y++) {
         set(server, origin.offset(-3, y, -3), ModBlocks.WITHERED_BONE_BLOCK.defaultBlockState());
         set(server, origin.offset(3, y, -3), ModBlocks.WITHERED_BONE_BLOCK.defaultBlockState());
         set(server, origin.offset(-3, y, 3), ModBlocks.WITHERED_BONE_BLOCK.defaultBlockState());
         set(server, origin.offset(3, y, 3), ModBlocks.WITHERED_BONE_BLOCK.defaultBlockState());
         placed += 4;
      }

      set(server, origin.offset(0, 1, 0), ModBlocks.COMMAND_CORE_BLOCK.defaultBlockState());
      set(server, origin.offset(0, 2, 0), ModBlocks.WITHER_STORM_EYE_BLOCK.defaultBlockState());
      return placed + 2;
   }

   private static int floatingIsland(ServerLevel server, BlockPos origin) {
      int placed = 0;
      BlockPos top = origin.above(18);
      int rad = 9;

      for (int y = 0; y >= -12; y--) {
         int layerRad = Math.max(1, rad + y * 2 / 3);

         for (int x = -layerRad; x <= layerRad; x++) {
            for (int z = -layerRad; z <= layerRad; z++) {
               if (x * x + z * z <= layerRad * layerRad) {
                  BlockState st;
                  if (y == 0) {
                     st = Blocks.GRASS_BLOCK.defaultBlockState();
                  } else if (y >= -3) {
                     st = Blocks.DIRT.defaultBlockState();
                  } else if (y >= -8) {
                     st = Blocks.STONE.defaultBlockState();
                  } else {
                     st = Blocks.DEEPSLATE.defaultBlockState();
                  }

                  set(server, top.offset(x, y, z), st);
                  placed++;
               }
            }
         }
      }

      for (int h = 1; h <= 4; h++) {
         set(server, top.offset(-4, h, -4), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
         set(server, top.offset(4, h, -4), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
         set(server, top.offset(-4, h, 4), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
         set(server, top.offset(4, h, 4), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
         placed += 4;
      }

      set(server, top.offset(0, 1, 0), Blocks.WATER.defaultBlockState());
      set(server, top.offset(0, 2, 0), Blocks.BEACON.defaultBlockState());
      return placed + 2;
   }

   private static int skyFortress(ServerLevel server, BlockPos origin) {
      int placed = 0;
      BlockPos base = origin.above(12);

      for (int x = -8; x <= 8; x++) {
         for (int z = -8; z <= 8; z++) {
            set(server, base.offset(x, 0, z), Blocks.SMOOTH_QUARTZ.defaultBlockState());
            set(server, base.offset(x, 10, z), ((Block)Blocks.WOOL.white()).defaultBlockState());
            placed += 2;
         }
      }

      for (int y = 1; y <= 9; y++) {
         for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
               if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                  BlockState st = y >= 4 && y <= 6 ? Blocks.TINTED_GLASS.defaultBlockState() : Blocks.QUARTZ_BLOCK.defaultBlockState();
                  set(server, base.offset(x, y, z), st);
                  placed++;
               }
            }
         }
      }

      for (int y = 1; y <= 9; y++) {
         set(server, base.offset(0, y, 0), Blocks.WATER.defaultBlockState());
         placed++;
      }

      return placed;
   }

   private static int farLands(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int y = 0; y <= 24; y++) {
         for (int x = -12; x <= 12; x++) {
            for (int z = -3; z <= 3; z++) {
               if ((x + y) % 3 != 0) {
                  BlockState st = y % 4 == 0 ? Blocks.BEDROCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
                  set(server, origin.offset(x, y, z), st);
                  placed++;
               }
            }
         }
      }

      return placed;
   }

   private static int ivorLab(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int y = -4; y <= 0; y++) {
         for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
               if (y != -4 && y != 0 && Math.abs(x) != 5 && Math.abs(z) != 5) {
                  set(server, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
               } else {
                  set(server, origin.offset(x, y, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
               }

               placed++;
            }
         }
      }

      set(server, origin.offset(-3, -3, -3), Blocks.BREWING_STAND.defaultBlockState());
      set(server, origin.offset(-2, -3, -3), Blocks.CAULDRON.defaultBlockState());
      set(server, origin.offset(0, -3, 0), ModBlocks.COMMAND_CORE_BLOCK.defaultBlockState());
      set(server, origin.offset(3, -3, 3), Blocks.BOOKSHELF.defaultBlockState());
      set(server, origin.offset(3, -2, 3), Blocks.BOOKSHELF.defaultBlockState());
      set(server, origin.offset(0, -1, 0), Blocks.SOUL_LANTERN.defaultBlockState());
      return placed + 6;
   }

   private static int stormNest(ServerLevel server, BlockPos origin) {
      int placed = 0;
      BlockPos center = origin.above(10);

      for (int r = 1; r <= 6; r++) {
         double angle = r * 1.1;
         int ox = (int)(Math.cos(angle) * r * 1.5);
         int oz = (int)(Math.sin(angle) * r * 1.5);
         set(server, center.offset(ox, r, oz), ModBlocks.WITHERED_BONE_BLOCK.defaultBlockState());
         set(server, center.offset(-ox, -r, -oz), ModBlocks.TAINTED_OBSIDIAN.defaultBlockState());
         placed += 2;
      }

      set(server, center, ModBlocks.WITHER_STORM_EYE_BLOCK.defaultBlockState());
      set(server, center.above(), ModBlocks.COMMAND_CORE_BLOCK.defaultBlockState());
      return placed + 2;
   }

   private static int treasureVault(ServerLevel server, BlockPos origin) {
      int placed = 0;

      for (int y = -3; y <= 3; y++) {
         for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
               if (Math.abs(y) != 3 && Math.abs(x) != 5 && Math.abs(z) != 5) {
                  set(server, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
               } else {
                  set(server, origin.offset(x, y, z), Blocks.NETHERITE_BLOCK.defaultBlockState());
               }

               placed++;
            }
         }
      }

      set(server, origin.offset(-2, -2, -2), Blocks.DIAMOND_BLOCK.defaultBlockState());
      set(server, origin.offset(2, -2, -2), Blocks.EMERALD_BLOCK.defaultBlockState());
      set(server, origin.offset(-2, -2, 2), Blocks.GOLD_BLOCK.defaultBlockState());
      set(server, origin.offset(2, -2, 2), ModBlocks.SUPER_COMMAND_BLOCK.defaultBlockState());
      set(server, origin.offset(0, -2, 0), Blocks.BEACON.defaultBlockState());
      return placed + 5;
   }
}
