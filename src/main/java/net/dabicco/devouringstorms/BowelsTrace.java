package net.dabicco.devouringstorms;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class BowelsTrace {
   public static final int STEP_IN = 0;
   public static final int AFTER_IN = 1;
   public static final int TRAVEL = 2;
   public static final int MOVE_IN = 3;
   public static final int MOVE_OUT = 4;
   public static final int BEFORE_OUT = 5;
   public static final int STEP_OUT = 6;
   public static final String[] NAMES = new String[]{"aiStep^", "inFrame", "travel^", "move^", "move$", "outFrame", "aiStep$"};
   private static final Vec3[] SAMPLES;
   private static Vec3 lastTick;
   private static Vec3 lastInput;
   private static float lastSpeed;

   private BowelsTrace() {
   }

   public static void record(Entity entity, int slot, Vec3 velocity, boolean inFrame) {
      if (entity instanceof Player && entity.level().isClientSide()) {
         if (DevouringStormsClientConfig.bowelsFrameHud) {
            Direction gravity = BowelsFrame.boxAxis(entity);
            if (gravity != Direction.DOWN) {
               if (slot == 0) {
                  lastTick = SAMPLES[6];
               }

               SAMPLES[slot] = inFrame ? BowelsFrame.toWorld(gravity, velocity) : velocity;
            }
         }
      }
   }

   public static Vec3 sample(int slot) {
      return SAMPLES[slot];
   }

   public static void recordInput(Entity entity, Vec3 input, float speed) {
      if (entity instanceof Player && entity.level().isClientSide()) {
         if (DevouringStormsClientConfig.bowelsFrameHud) {
            if (BowelsFrame.boxAxis(entity) != Direction.DOWN) {
               lastInput = input;
               lastSpeed = speed;
            }
         }
      }
   }

   public static Vec3 input() {
      return lastInput;
   }

   public static float speed() {
      return lastSpeed;
   }

   public static Vec3 previousTick() {
      return lastTick;
   }

   static {
      SAMPLES = new Vec3[NAMES.length];
      lastTick = Vec3.ZERO;
      lastInput = Vec3.ZERO;
   }
}
