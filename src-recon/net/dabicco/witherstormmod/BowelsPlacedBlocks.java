package net.dabicco.witherstormmod.bowels;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import net.dabicco.witherstormmod.BowelsGravity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class BowelsPlacedBlocks {
   private static final int REMEMBERED = 4096;
   private static final Set<BlockPos> placed = new LinkedHashSet<>();

   private BowelsPlacedBlocks() {
   }

   public static void remember(Level level, BlockPos pos) {
      if (BowelsGravity.isBowels(level)) {
         placed.add(pos.immutable());
         if (placed.size() > 4096) {
            Iterator<BlockPos> oldest = placed.iterator();
            oldest.next();
            oldest.remove();
         }
      }
   }

   public static void forget(BlockPos pos) {
      placed.remove(pos);
   }

   public static BlockPos anyIn(ServerLevel level, net.dabicco.witherstormmod.bowels.BowelsMawEntity maw) {
      Iterator<BlockPos> it = placed.iterator();

      while (it.hasNext()) {
         BlockPos pos = it.next();
         if (level.isLoaded(pos)) {
            if (level.getBlockState(pos).isAir()) {
               it.remove();
            } else if (maw.inBeamAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
               return pos;
            }
         }
      }

      return null;
   }
}
