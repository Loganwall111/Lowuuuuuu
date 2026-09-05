package net.dabicco.witherstormmod;

import net.minecraft.core.Direction;

public final class BowelsRoute {
   public static final int UPRIGHT_X = 80;
   public static final Direction WALL = Direction.NORTH;
   private static final double SLACK = 2.0;

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
         double past = x - 80.0;
         if (previous == WALL && past < 2.0) {
            return WALL;
         } else if (previous == Direction.DOWN && past > -2.0) {
            return Direction.DOWN;
         } else {
            return past < 0.0 ? WALL : Direction.DOWN;
         }
      }
   }
}
