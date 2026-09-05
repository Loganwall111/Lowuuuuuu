package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.BowelsFrame;
import net.dabicco.witherstormmod.BowelsGravity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;

public final class BowelsView {
   private static final Quaternionf FRAME = new Quaternionf();
   private static final Quaternionf FROM = new Quaternionf();
   private static final Quaternionf TO = new Quaternionf();
   private static final Quaternionf SHAKE = new Quaternionf();
   private static final double SHAKE_SECONDS = (double)0.5F;
   private static final double TURN_SECONDS = (double)1.5F;
   private static final double ROLL_SHAKE_SECONDS = 0.9;
   private static final double ROLL_TURN_SECONDS = (double)4.0F;
   private static final float SHAKE_AMOUNT = 0.115F;
   private static Direction leaving;
   private static Direction arriving;
   private static long startedAt;

   private BowelsView() {
   }

   private static boolean isRoll() {
      return leaving != null && arriving != null && leaving.getOpposite() == arriving;
   }

   private static double shakeSeconds() {
      return isRoll() ? 0.9 : (double)0.5F;
   }

   private static double turnSeconds() {
      return isRoll() ? (double)4.0F : (double)1.5F;
   }

   public static Quaternionf frame(double x, double y, double z) {
      ClientLevel level = level();
      if (level == null) {
         return null;
      } else {
         Direction gravity = BowelsGravity.axisAt(x, y, z, level.getGameTime(), (Direction)null);
         return gravity == Direction.DOWN ? null : BowelsGravity.frame(gravity, FRAME);
      }
   }

   public static Quaternionf cameraFrame(Direction gravity) {
      if (level() != null && gravity != null) {
         long now = System.nanoTime();
         if (arriving == null) {
            arriving = gravity;
            leaving = gravity;
            startedAt = 0L;
         } else if (gravity != arriving) {
            leaving = arriving;
            arriving = gravity;
            startedAt = now;
         }

         if (arriving == Direction.DOWN && leaving == Direction.DOWN) {
            return null;
         } else {
            BowelsGravity.frame(arriving, TO);
            if (startedAt == 0L) {
               return TO;
            } else {
               double since = (double)(now - startedAt) / (double)1.0E9F;
               if (since >= shakeSeconds() + turnSeconds()) {
                  leaving = arriving;
                  return arriving == Direction.DOWN ? null : TO;
               } else {
                  BowelsGravity.frame(leaving, FROM);
                  float strength;
                  if (since < shakeSeconds()) {
                     float grow = (float)(since / shakeSeconds());
                     strength = 0.115F * grow * grow;
                  } else {
                     float t = (float)((since - shakeSeconds()) / turnSeconds());
                     strength = 0.115F * (1.0F - t * t * t);
                  }

                  float gust = 0.55F + 0.45F * Mth.sin((double)((float)(since * 3.7)));
                  float a = strength * gust;
                  float w1 = (float)(since * 9.1);
                  float w2 = (float)(since * 14.3);
                  float w3 = (float)(since * 5.3);
                  SHAKE.rotationX(Mth.sin((double)w1) * a + Mth.sin((double)(w2 * 0.61F)) * a * 0.45F).rotateZ(Mth.cos((double)(w1 * 0.83F)) * a + Mth.sin((double)w3) * a * 0.55F).rotateY(Mth.sin((double)(w3 * 0.47F)) * a * 0.7F);
                  if (since < shakeSeconds()) {
                     return FRAME.set(FROM).mul(SHAKE);
                  } else {
                     float t = (float)((since - shakeSeconds()) / turnSeconds());
                     float eased = t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
                     return FRAME.set(FROM).slerp(TO, eased).mul(SHAKE);
                  }
               }
            }
         }
      } else {
         leaving = null;
         arriving = null;
         return null;
      }
   }

   public static Direction leaving() {
      return leaving == arriving ? null : leaving;
   }

   public static boolean holding() {
      if (startedAt != 0L && arriving != null && leaving != arriving) {
         return (double)(System.nanoTime() - startedAt) / (double)1.0E9F < shakeSeconds();
      } else {
         return false;
      }
   }

   private static ClientLevel level() {
      ClientLevel level = Minecraft.getInstance().level;
      return level != null && BowelsGravity.isBowels(level) ? level : null;
   }

   public static Direction eyeAxis(Entity entity) {
      return BowelsFrame.boxAxis(entity);
   }
}
