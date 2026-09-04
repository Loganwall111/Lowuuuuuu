package net.dabicco.witherstormmod;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public interface BowelsBody {
   Direction dabyws$settled();

   void dabyws$setSettled(Direction var1);

   Direction dabyws$stepAxis();

   void dabyws$setStepAxis(Direction var1);

   int dabyws$stepDepth();

   void dabyws$setStepDepth(int var1);

   Vec3 dabyws$moveFrom();

   void dabyws$setMoveFrom(Vec3 var1);

   Vec3 dabyws$moveWanted();

   void dabyws$setMoveWanted(Vec3 var1);

   Direction dabyws$lastPull();

   void dabyws$setLastPull(Direction var1);

   boolean dabyws$turnoverFall();

   void dabyws$setTurnoverFall(boolean var1);
}
