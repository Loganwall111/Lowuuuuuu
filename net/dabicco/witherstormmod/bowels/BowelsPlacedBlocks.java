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
   private static final Set<BlockPos> placed = new LinkedHashSet();

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

   public static BlockPos anyIn(ServerLevel level, BowelsMawEntity maw) {
      Iterator<BlockPos> it = placed.iterator();

      while(it.hasNext()) {
         BlockPos pos = (BlockPos)it.next();
         if (level.isLoaded(pos)) {
            if (level.getBlockState(pos).isAir()) {
               it.remove();
            } else if (maw.inBeamAt((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F)) {
               return pos;
            }
         }
      }

      return null;
   }
}
