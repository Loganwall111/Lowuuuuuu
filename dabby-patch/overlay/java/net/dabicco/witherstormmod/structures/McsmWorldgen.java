package net.dabicco.witherstormmod.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * McsmWorldgen — where the Story Mode world actually gets built.
 *
 * The player asked for the whole MCSM world to exist in one place rather than
 * scattered randomly: Beacon Town, Sky City, the Order Temple, the Nether
 * train, the floating islands. The anchor is the seed's own landmark at
 * X=-640, Z=256, and everything is laid out around it — some close, some far,
 * deliberately not packed tight.
 *
 * Placement is queued and drained a few thousand blocks per tick. Some of these
 * builds are enormous (adv_creepyMansionFull is 489x257x1262 = 158 million
 * block positions) so stamping one in a single tick would hang the server for
 * minutes. Air is skipped, which removes ~83% of the work.
 */
public final class McsmWorldgen {

   /** The MCSM world anchor. */
   public static final int ANCHOR_X = -640;
   public static final int ANCHOR_Z = 256;

   private McsmWorldgen() {
   }

   /** One planned build: schematic path, world origin, and a display name. */
   public record Site(String path, int x, int y, int z, String label, boolean floating) {
   }

   /**
    * The Story Mode world layout.
    *
    * Offsets are relative to the anchor. Spread is deliberately wide — the
    * furthest sites are ~2.5k blocks out — so you travel between locations
    * instead of finding them stacked on top of each other. Sky builds are
    * lifted to y=276..308 -- above the vanilla cloud layer at y=192, so you
    * look DOWN on the clouds from them, as in the reference shots.
    */
   public static List<Site> layout() {
      List<Site> s = new ArrayList<>();

      /* ---- the heart of the world, at/near the anchor ---- */
      add(s, "MC201/adv_beaconTown.schematic",              0,    64,     0, "Beacon Town", false);
      add(s, "MC201/adv_beaconTownOutskirts.schematic",   360,    64,   140, "Beacon Town Outskirts", false);
      add(s, "MC201/adv_beaconTownMapShop.schematic",    -180,    64,   180, "Beacon Town Map Shop", false);
      add(s, "MC101/adv_townFair.schematic",             -520,    64,  -420, "EnderCon Town Fair", false);
      add(s, "MC101/adv_templeHub.schematic",             640,    64,  -560, "Order of the Stone Temple", false);
      add(s, "MC101/adv_templeHubInterior.schematic",     640,    38,  -560, "Temple Interior", false);

      /* ---- floating islands, high in the air ---- */
      add(s, "MC105/adv_skylandTown.schematic",          -980,   296,   720, "Sky City", true);
      add(s, "MC105/adv_skylandSpeakeasy.schematic",     -640,   284,  1080, "Sky Speakeasy", true);
      add(s, "MC105/adv_jungleFortress.schematic",       1180,   276,   540, "Jungle Fortress", true);
      add(s, "Additional/faceEast_adv_mushroomIsland_204B.schematic",
                                                          420,   308,  1240, "Mushroom Island", true);

      /* ---- the wilderness arc ---- */
      add(s, "MC101/adv_wilderness.schematic",           -260,    64,  -980, "The Wilderness", false);
      add(s, "MC101/adv_wildernessTreehouse.schematic",  -120,    64, -1240, "Wilderness Treehouse", false);
      add(s, "MC101/adv_wildernessTower.schematic",       180,    64, -1180, "Wilderness Tower", false);
      add(s, "MC101/adv_wildernessRavine.schematic",     -560,    48, -1320, "Wilderness Ravine", false);
      add(s, "MC101/adv_forestStage.schematic",          -820,    64,  -160, "Forest Stage", false);

      /* ---- the Order's places ---- */
      add(s, "MC102/adv_ellieCourtyard.schematic",       1420,    64,  -240, "Ellegaard's Courtyard", false);
      add(s, "MC102/adv_magnusCourtyardA.schematic",     1180,    64,   380, "Magnus's Courtyard", false);
      add(s, "MC102/adv_sorenCourtyard.schematic",       -1360,   64,  -640, "Soren's Courtyard", false);
      add(s, "MC102/adv_sorenInterior.schematic",        -1360,   40,  -640, "Soren's Interior", false);
      add(s, "MC103/adv_sorenGrinder.schematic",         -1760,   64,  -320, "Soren's Grinder", false);
      add(s, "MC103/adv_sorenPortalRoom.schematic",      -1760,   40,   120, "Soren's Portal Room", false);

      /* ---- the far reaches ---- */
      add(s, "MC104/adv_farlandsMazeExterior.schematic",  2100,   64,  1180, "Far Lands Maze", false);
      add(s, "MC104/adv_farlandsSwamp.schematic",         1740,   58,  1560, "Far Lands Swamp", false);
      add(s, "MC104/adv_farlandsCottageBasement.schematic", 2020,  44,  1520, "Far Lands Cottage", false);
      add(s, "MC106/adv_creepyMansion.schematic",        -2240,   64,  1420, "The Creepy Mansion", false);
      add(s, "MC107/adv_badlandsMaze.schematic",          2260,   64,  -1340, "Badlands Maze", false);
      add(s, "MC108/adv_snowyVillage.schematic",         -1980,   64,  -1720, "Snowy Village", false);

      /* ---- season two ---- */
      add(s, "MC201/adv_rivalTown.schematic",             1620,   64,  -1780, "Champion City", false);
      add(s, "MC201/adv_seaTemple.schematic",             -420,   34,  2180, "The Sea Temple", false);
      add(s, "MC202/adv_wonderlandExterior.schematic",    2480,   64,   420, "The Wonderland", false);
      add(s, "MC203/adv_prisonMaze.schematic",           -2620,   38,  -280, "The Prison Maze", false);
      add(s, "MC205/adv_beaconTownTwisted.schematic",     -160,   64,  2620, "Beacon Town (Twisted)", false);
      add(s, "MC205/adv_terminalControlCenter.schematic",  340,   64,  2980, "Terminal Control Center", false);

      /* ---- the Nether train ---- */
      add(s, "MC101/adv_theNetherTrain.schematic",        980,    64,  1880, "The Nether Train", false);
      add(s, "MC101/_NetherParts/adv_theNetherStation.schematic",
                                                          760,    64,  2180, "Nether Station", false);

      return s;
   }

   private static void add(List<Site> s, String path, int dx, int y, int dz, String label, boolean floating) {
      s.add(new Site(path, ANCHOR_X + dx, y, ANCHOR_Z + dz, label, floating));
   }

   /* ------------------------------------------------------------------ */
   /* Chunked placement                                                   */
   /* ------------------------------------------------------------------ */

   /** A build in progress. */
   private static final class Job {
      final McsmSchematic sch;
      final BlockPos origin;
      final String label;
      int x;
      int y;
      int z;
      int placed;

      Job(McsmSchematic sch, BlockPos origin, String label) {
         this.sch = sch;
         this.origin = origin;
         this.label = label;
      }

      boolean done() {
         return this.y >= this.sch.height;
      }
   }

   private static final Deque<Job> QUEUE = new ArrayDeque<>();

   /** How many blocks to place per tick. Tuned to stay off the tick budget. */
   private static int budget = 24000;

   public static void setBudget(int n) {
      budget = Math.max(256, n);
   }

   public static int pending() {
      return QUEUE.size();
   }

   public static void clear() {
      QUEUE.clear();
   }

   /** Queue a schematic for placement. */
   public static void enqueue(McsmSchematic sch, BlockPos origin, String label) {
      QUEUE.add(new Job(sch, origin, label));
   }

   /**
    * Drain part of the queue. Call once per server tick.
    *
    * @return blocks placed this tick
    */
   public static int tick(ServerLevel level) {
      if (QUEUE.isEmpty()) {
         return 0;
      }
      int left = budget;
      while (left > 0 && !QUEUE.isEmpty()) {
         Job j = QUEUE.peek();
         left -= step(level, j, left);
         if (j.done()) {
            QUEUE.poll();
         }
      }
      return budget - left;
   }

   /** Place up to {@code allow} blocks of one job, resuming where it stopped. */
   private static int step(ServerLevel level, Job j, int allow) {
      int used = 0;
      McsmSchematic s = j.sch;
      BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();

      while (j.y < s.height) {
         while (j.z < s.length) {
            while (j.x < s.width) {
               int id = s.blockId(j.x, j.y, j.z);
               if (id != 0) { // skip air: ~83% of every build
                  BlockState st = state(id, s.blockData(j.x, j.y, j.z));
                  if (st != null) {
                     p.set(j.origin.getX() + j.x, j.origin.getY() + j.y, j.origin.getZ() + j.z);
                     if (level.isInWorldBounds(p)) {
                        // flag 2 = send to clients, skip neighbour updates:
                        // essential, or a 100M-block build triggers a
                        // cascade of block ticks and falls apart.
                        level.setBlock(p, st, 2);
                        j.placed++;
                     }
                  }
                  used++;
                  if (used >= allow) {
                     j.x++;
                     return used;
                  }
               }
               j.x++;
            }
            j.x = 0;
            j.z++;
         }
         j.z = 0;
         j.y++;
      }
      return used;
   }

   /** Resolve a legacy id+data to a modern BlockState, or null if unknown. */
   private static BlockState state(int id, int data) {
      String name = LegacyBlocks.of(id, data);
      Identifier rid = Identifier.fromNamespaceAndPath("minecraft", name);
      Block b = BuiltInRegistries.BLOCK.getOptional(rid).orElse(null);
      if (b == null || b == Blocks.AIR) {
         return null;
      }
      return b.defaultBlockState();
   }
}
