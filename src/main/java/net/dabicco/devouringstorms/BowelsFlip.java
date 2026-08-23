package net.dabicco.devouringstorms;

public final class BowelsFlip {
   public static final int FLIP_AT = 3;
   public static final int ROLL_TICKS = 98;
   private static boolean flipped;
   private static long rolledAt = Long.MIN_VALUE;

   private BowelsFlip() {
   }

   public static void startRoll(long gameTime) {
      rolledAt = gameTime;
   }

   public static boolean rolling(long gameTime) {
      return gameTime >= rolledAt && gameTime < rolledAt + 98L;
   }

   public static void set(boolean value) {
      flipped = value;
   }

   public static boolean flipped() {
      return flipped;
   }
}
