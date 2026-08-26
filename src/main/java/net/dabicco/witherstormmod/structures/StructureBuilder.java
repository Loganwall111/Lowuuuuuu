package net.dabicco.witherstormmod.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Procedural Story-Mode buildings, generated from code so no Blockbench or .nbt
 * structure files are required. These give the storm real backdrop towns to hunt
 * and level (combine with the "Structures" targeting mode + structure raids).
 */
public final class StructureBuilder {
   private StructureBuilder() {
   }

   /** Build one of: beacon | house | portal | church. Returns the number of blocks placed. */
   public static int build(ServerLevel server, BlockPos origin, String type) {
      switch (type) {
         case "beacon" -> {
            return beacon(server, origin);
         }
         case "house" -> {
            return house(server, origin);
         }
         case "portal" -> {
            return portalRuin(server, origin);
         }
         case "town", "endertown" -> { return town(server, origin); }
         case "church" -> {
            return church(server, origin);
         }
         default -> {
            return 0;
         }
      }
   }

   private static void set(ServerLevel server, BlockPos pos, BlockState state) {
      server.setBlock(pos, state, 2);
   }

   private static int town(ServerLevel server, BlockPos o) { int n=well(server,o); int[][] a={{14,0},{-14,0},{0,14},{0,-14},{10,10},{-10,-10}}; for(int[] v:a)n+=house(server,o.offset(v[0],0,v[1])); return n+church(server,o.offset(18,0,10))+beacon(server,o.offset(-18,0,12))+portalRuin(server,o.offset(18,0,-16)); }
   private static int well(ServerLevel s, BlockPos o) { int n=0; for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++) { if(Math.abs(x)==2||Math.abs(z)==2){set(s,o.offset(x,0,z),Blocks.COBBLESTONE.defaultBlockState());n++;} } set(s,o,Blocks.WATER.defaultBlockState()); return n+1; }

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
               BlockState st = (x == 0 && z == 0) ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.CUT_SANDSTONE.defaultBlockState();
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
