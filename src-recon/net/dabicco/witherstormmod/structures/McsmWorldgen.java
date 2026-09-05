package net.dabicco.witherstormmod.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class McsmWorldgen {
   public static final int ANCHOR_X = -640;
   public static final int ANCHOR_Z = 256;
   private static final Deque<net.dabicco.witherstormmod.structures.McsmWorldgen.Job> QUEUE = new ArrayDeque<>();
   private static int budget = 24000;

   private McsmWorldgen() {
   }

   public static List<net.dabicco.witherstormmod.structures.McsmWorldgen.Site> layout() {
      List<net.dabicco.witherstormmod.structures.McsmWorldgen.Site> s = new ArrayList<>();
      add(s, "MC201/adv_beaconTown.schematic", 0, 64, 0, "Beacon Town", false);
      add(s, "MC201/adv_beaconTownOutskirts.schematic", 360, 64, 140, "Beacon Town Outskirts", false);
      add(s, "MC201/adv_beaconTownMapShop.schematic", -180, 64, 180, "Beacon Town Map Shop", false);
      add(s, "MC101/adv_townFair.schematic", -520, 64, -420, "EnderCon Town Fair", false);
      add(s, "MC101/adv_templeHub.schematic", 640, 64, -560, "Order of the Stone Temple", false);
      add(s, "MC101/adv_templeHubInterior.schematic", 640, 38, -560, "Temple Interior", false);
      add(s, "MC105/adv_skylandTown.schematic", -980, 296, 720, "Sky City", true);
      add(s, "MC105/adv_skylandSpeakeasy.schematic", -640, 284, 1080, "Sky Speakeasy", true);
      add(s, "MC105/adv_jungleFortress.schematic", 1180, 276, 540, "Jungle Fortress", true);
      add(s, "Additional/faceEast_adv_mushroomIsland_204B.schematic", 420, 308, 1240, "Mushroom Island", true);
      add(s, "MC101/adv_wilderness.schematic", -260, 64, -980, "The Wilderness", false);
      add(s, "MC101/adv_wildernessTreehouse.schematic", -120, 64, -1240, "Wilderness Treehouse", false);
      add(s, "MC101/adv_wildernessTower.schematic", 180, 64, -1180, "Wilderness Tower", false);
      add(s, "MC101/adv_wildernessRavine.schematic", -560, 48, -1320, "Wilderness Ravine", false);
      add(s, "MC101/adv_forestStage.schematic", -820, 64, -160, "Forest Stage", false);
      add(s, "MC102/adv_ellieCourtyard.schematic", 1420, 64, -240, "Ellegaard's Courtyard", false);
      add(s, "MC102/adv_magnusCourtyardA.schematic", 1180, 64, 380, "Magnus's Courtyard", false);
      add(s, "MC102/adv_sorenCourtyard.schematic", -1360, 64, -640, "Soren's Courtyard", false);
      add(s, "MC102/adv_sorenInterior.schematic", -1360, 40, -640, "Soren's Interior", false);
      add(s, "MC103/adv_sorenGrinder.schematic", -1760, 64, -320, "Soren's Grinder", false);
      add(s, "MC103/adv_sorenPortalRoom.schematic", -1760, 40, 120, "Soren's Portal Room", false);
      add(s, "MC104/adv_farlandsMazeExterior.schematic", 2100, 64, 1180, "Far Lands Maze", false);
      add(s, "MC104/adv_farlandsSwamp.schematic", 1740, 58, 1560, "Far Lands Swamp", false);
      add(s, "MC104/adv_farlandsCottageBasement.schematic", 2020, 44, 1520, "Far Lands Cottage", false);
      add(s, "MC106/adv_creepyMansion.schematic", -2240, 64, 1420, "The Creepy Mansion", false);
      add(s, "MC107/adv_badlandsMaze.schematic", 2260, 64, -1340, "Badlands Maze", false);
      add(s, "MC108/adv_snowyVillage.schematic", -1980, 64, -1720, "Snowy Village", false);
      add(s, "MC201/adv_rivalTown.schematic", 1620, 64, -1780, "Champion City", false);
      add(s, "MC201/adv_seaTemple.schematic", -420, 34, 2180, "The Sea Temple", false);
      add(s, "MC202/adv_wonderlandExterior.schematic", 2480, 64, 420, "The Wonderland", false);
      add(s, "MC203/adv_prisonMaze.schematic", -2620, 38, -280, "The Prison Maze", false);
      add(s, "MC205/adv_beaconTownTwisted.schematic", -160, 64, 2620, "Beacon Town (Twisted)", false);
      add(s, "MC205/adv_terminalControlCenter.schematic", 340, 64, 2980, "Terminal Control Center", false);
      add(s, "MC101/adv_theNetherTrain.schematic", 980, 64, 1880, "The Nether Train", false);
      add(s, "MC101/_NetherParts/adv_theNetherStation.schematic", 760, 64, 2180, "Nether Station", false);
      return s;
   }

   private static void add(List<net.dabicco.witherstormmod.structures.McsmWorldgen.Site> s, String path, int dx, int y, int dz, String label, boolean floating) {
      s.add(new net.dabicco.witherstormmod.structures.McsmWorldgen.Site(path, -640 + dx, y, 256 + dz, label, floating));
   }

   public static void setBudget(int n) {
      budget = Math.max(256, n);
   }

   public static int pending() {
      return QUEUE.size();
   }

   public static void clear() {
      QUEUE.clear();
   }

   public static void enqueue(net.dabicco.witherstormmod.structures.McsmSchematic sch, BlockPos origin, String label) {
      QUEUE.add(new net.dabicco.witherstormmod.structures.McsmWorldgen.Job(sch, origin, label));
   }

   public static int tick(ServerLevel level) {
      if (QUEUE.isEmpty()) {
         return 0;
      } else {
         int left = budget;

         while (left > 0 && !QUEUE.isEmpty()) {
            net.dabicco.witherstormmod.structures.McsmWorldgen.Job j = QUEUE.peek();
            left -= step(level, j, left);
            if (j.done()) {
               QUEUE.poll();
            }
         }

         return budget - left;
      }
   }

   private static int step(ServerLevel level, net.dabicco.witherstormmod.structures.McsmWorldgen.Job j, int allow) {
      int used = 0;
      net.dabicco.witherstormmod.structures.McsmSchematic s = j.sch;

      for (MutableBlockPos p = new MutableBlockPos(); j.y < s.height; j.y++) {
         while (j.z < s.length) {
            for (; j.x < s.width; j.x++) {
               int id = s.blockId(j.x, j.y, j.z);
               if (id != 0) {
                  BlockState st = state(id, s.blockData(j.x, j.y, j.z));
                  if (st != null) {
                     p.set(j.origin.getX() + j.x, j.origin.getY() + j.y, j.origin.getZ() + j.z);
                     if (level.isInWorldBounds(p)) {
                        level.setBlock(p, st, 2);
                        j.placed++;
                     }
                  }

                  if (++used >= allow) {
                     j.x++;
                     return used;
                  }
               }
            }

            j.x = 0;
            j.z++;
         }

         j.z = 0;
      }

      return used;
   }

   private static BlockState state(int id, int data) {
      String name = net.dabicco.witherstormmod.structures.LegacyBlocks.of(id, data);
      Identifier rid = Identifier.fromNamespaceAndPath("minecraft", name);
      Block b = (Block)BuiltInRegistries.BLOCK.getOptional(rid).orElse(null);
      return b != null && b != Blocks.AIR ? b.defaultBlockState() : null;
   }

   private static final class Job {
      final net.dabicco.witherstormmod.structures.McsmSchematic sch;
      final BlockPos origin;
      final String label;
      int x;
      int y;
      int z;
      int placed;

      Job(net.dabicco.witherstormmod.structures.McsmSchematic sch, BlockPos origin, String label) {
         this.sch = sch;
         this.origin = origin;
         this.label = label;
      }

      boolean done() {
         return this.y >= this.sch.height;
      }
   }

   public record Site(String path, int x, int y, int z, String label, boolean floating) {
   }
}
