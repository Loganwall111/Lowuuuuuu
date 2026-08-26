package net.dabicco.witherstormmod;

import net.minecraft.core.Direction;

public final class BowelsRoute {
   public static final int UPRIGHT_X = 80;
   public static final Direction WALL;
   private static final double SLACK = (double)2.0F;

   private BowelsRoute() {
   }

   public static Direction axisAt(double x, double y, double z, Direction previous) {
      if (BowelsEndRoom.holds(x, y, z)) {
         return BowelsEndRoom.pull(y);
      } else if (BowelsBackHall.holds(x, y, z)) {
         return Direction.DOWN;
      } else if (!BowelsEntry.holds(x, y, z) && !BowelsHallway.holds(x, y, z)) {
         return null;
      } else {
         double past = x - (double)80.0F;
         if (previous == WALL && past < (double)2.0F) {
            return WALL;
         } else if (previous == Direction.DOWN && past > (double)-2.0F) {
            return Direction.DOWN;
         } else {
            return past < (double)0.0F ? WALL : Direction.DOWN;
         }
      }
   }

   static {
      WALL = Direction.NORTH;
   }
}
