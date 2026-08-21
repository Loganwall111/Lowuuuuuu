package net.dabicco.witherstormmod.client;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.AnimationState;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Clean animation helper for the Wither Storm models.
 *
 * Bakes {@link AnimationDefinition}s (from the {@code entity.animation} package) into
 * {@link KeyframeAnimation}s bound to a model root, and drives them through the modern
 * MC animation API ({@link AnimationState} + {@code KeyframeAnimation.apply}). This
 * mirrors the 26.2 entity-animation pattern documented by Fabric.
 */
public final class StormAnimation {
   private final AnimationState idleState = new AnimationState();
   private final AnimationState spawnState = new AnimationState();
   private final AnimationState roarState = new AnimationState();

   private final KeyframeAnimation idle;
   private final KeyframeAnimation spawn;
   private final KeyframeAnimation roar;

   public StormAnimation(ModelPart root, AnimationDefinition idle, AnimationDefinition spawn, AnimationDefinition roar) {
      this.idle = idle != null ? idle.bake(root) : null;
      this.spawn = spawn != null ? spawn.bake(root) : null;
      this.roar = roar != null ? roar.bake(root) : null;
   }

   /** Advance animation states once per rendered frame. */
   public void tick(float ageInTicks, boolean spawnPlaying, float spawnProgress, boolean roaring) {
      int age = (int) ageInTicks;
      this.idleState.startIfStopped(age);
      this.idleState.update(age, 1.0F);

      if (spawnPlaying) {
         this.spawnState.startIfStopped(age);
      } else if (spawnProgress >= 1.0F) {
         this.spawnState.stop();
      }
      this.spawnState.update(age, 1.0F);

      if (roaring) {
         this.roarState.startIfStopped(age);
      } else {
         this.roarState.stop();
      }
      this.roarState.update(age, 1.0F);
   }

   /** Apply all active animations to the model root (call after resetPose). */
   public void apply(float ageInTicks) {
      if (this.idle != null) {
         this.idle.apply(this.idleState, ageInTicks);
      }
      if (this.spawn != null) {
         this.spawn.apply(this.spawnState, ageInTicks);
      }
      if (this.roar != null) {
         this.roar.apply(this.roarState, ageInTicks);
      }
   }
}
