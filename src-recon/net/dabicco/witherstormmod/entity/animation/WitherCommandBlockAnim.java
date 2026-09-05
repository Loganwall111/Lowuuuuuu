package net.dabicco.witherstormmod.entity.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.animation.AnimationChannel.Interpolations;
import net.minecraft.client.animation.AnimationChannel.Targets;
import net.minecraft.client.animation.AnimationDefinition.Builder;

public class WitherCommandBlockAnim {
   public static final AnimationDefinition idle = Builder.withLength(2.5F)
      .looping()
      .addAnimation(
         "upperBodyPart2",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(1.25F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(2.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "upperBodyPart2",
         new AnimationChannel(
            Targets.POSITION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(2.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR)
            }
         )
      )
      .addAnimation(
         "upperBodyPart2",
         new AnimationChannel(
            Targets.SCALE,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR),
               new Keyframe(1.25F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.LINEAR),
               new Keyframe(2.5F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.CATMULLROM)
            }
         )
      )
      .build();
   public static final AnimationDefinition deathtest = Builder.withLength(1.8333F)
      .addAnimation(
         "upperBodyPart2",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "upperBodyPart2",
         new AnimationChannel(Targets.POSITION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "upperBodyPart2",
         new AnimationChannel(
            Targets.SCALE,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0, 1.0, 1.0), Interpolations.CATMULLROM),
               new Keyframe(1.8333F, KeyframeAnimations.scaleVec(-4.4F, 1.1F, 1.0), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "head1",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(1.75F, KeyframeAnimations.degreeVec(38.38F, 0.0F, 0.0F), Interpolations.LINEAR)
            }
         )
      )
      .addAnimation(
         "head2",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(1.75F, KeyframeAnimations.degreeVec(15.0F, -27.5F, 0.0F), Interpolations.LINEAR)
            }
         )
      )
      .addAnimation(
         "head3",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.LINEAR),
               new Keyframe(1.75F, KeyframeAnimations.degreeVec(17.5F, 27.5F, 0.0F), Interpolations.LINEAR)
            }
         )
      )
      .build();
}
