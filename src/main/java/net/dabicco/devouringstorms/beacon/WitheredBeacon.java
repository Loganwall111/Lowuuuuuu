package net.dabicco.devouringstorms.beacon;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface WitheredBeacon {
   boolean dabyws$isWithered();

   void dabyws$setWithered(boolean var1);

   boolean dabyws$isAffected();

   void dabyws$setAffected(boolean var1);

   /** Unique method exposed so the static tick inject can boost a withered beacon. */
   void dabyws$boost(Level level, BlockPos pos);
}
