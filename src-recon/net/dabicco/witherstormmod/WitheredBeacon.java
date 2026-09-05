package net.dabicco.witherstormmod.beacon;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface WitheredBeacon {
   boolean dabyws$isWithered();

   void dabyws$setWithered(boolean var1);

   boolean dabyws$isAffected();

   void dabyws$setAffected(boolean var1);

   void dabyws$boost(Level var1, BlockPos var2);
}
