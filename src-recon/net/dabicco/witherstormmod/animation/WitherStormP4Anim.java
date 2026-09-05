package net.dabicco.witherstormmod.entity.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.animation.AnimationChannel.Interpolations;
import net.minecraft.client.animation.AnimationChannel.Targets;
import net.minecraft.client.animation.AnimationDefinition.Builder;

public class WitherStormP4Anim {
   public static final AnimationDefinition Spawn = Builder.withLength(4.0F)
      .addAnimation(
         "bone",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone2",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone241",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(35.0078F, -26.1365F, 0.74F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(12.8455F, 11.8048F, 25.4888F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone242",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone243",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone244",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone247",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -55.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone248",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone249",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone252",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone253",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone257",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -37.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone258",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone260",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone261",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone263",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone265",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone268",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone271",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone272",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone274",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone275",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone276",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone277",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -35.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone278",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone280",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone281",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone284",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone286",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone287",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone288",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone289",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone291",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone292",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone294",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone296",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone297",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone298",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone5",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone6",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(20.0F, 30.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(17.2704F, 3.884F, -9.1078F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone7",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone8",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 32.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone9",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone10",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone11",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 32.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone12",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone14",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone15",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone17",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(-55.0F, -32.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone18",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone21",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 22.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone22",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone23",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone25",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone27",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone30",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(-111.6362F, -25.9897F, -34.4382F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone31",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone32",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone33",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone35",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone36",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone37",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone38",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone39",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone41",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone46",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 37.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone47",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone50",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone51",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone54",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone57",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone60",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone65",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 30.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(62.5F, -2.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone66",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone67",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 30.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(-39.6097F, -7.8807F, -15.3372F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone68",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone69",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone70",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-118.6271F, 15.4699F, -8.2833F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(-117.5F, 0.0F, 1.0E-4F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone72",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(11.9385F, -3.7318F, 17.1095F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(176.9806F, 6.0522F, 19.1795F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone73",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -7.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone75",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone76",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone77",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone78",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone80",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 22.5F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone83",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -67.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone84",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 22.5F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone85",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone87",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 12.5F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone88",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -27.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone90",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone91",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone94",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone95",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone96",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone101",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone103",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone104",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone105",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone108",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone110",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone116",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone117",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone121",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone124",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -87.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(-35.0F, -25.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone125",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -22.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone126",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone127",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -47.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone128",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone129",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 22.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone130",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone131",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone132",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone133",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone135",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone137",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone138",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone140",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone141",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone142",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -37.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone143",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone146",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone148",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -22.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone150",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone152",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone154",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone155",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone156",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone157",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone160",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone163",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone165",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -37.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone167",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone171",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone172",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone174",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone177",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone183",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 52.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone184",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -85.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone185",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone186",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone188",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone189",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone190",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone191",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone193",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone194",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone196",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone197",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(34.1354F, -16.7656F, -11.0653F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(35.6865F, -22.9193F, -15.6265F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone198",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone199",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(72.1563F, -12.1471F, 2.301F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone200",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone201",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(95.1733F, 26.4883F, 16.8086F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone202",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone203",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-69.4572F, -9.7083F, 30.2394F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(-69.2879F, -12.0481F, 29.3426F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone205",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone206",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone207",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(-7.3241F, 1.6189F, 12.3963F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone209",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone210",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone211",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone212",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone213",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone214",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone215",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone216",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone217",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone220",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone224",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone225",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 22.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone226",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone228",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone229",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone233",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone239",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .addAnimation(
         "bone299",
         new AnimationChannel(Targets.ROTATION, new Keyframe[]{new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)})
      )
      .build();
   public static final AnimationDefinition Idle = Builder.withLength(32.0F)
      .looping()
      .addAnimation(
         "bone",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone2",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone241",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.8455F, 11.8048F, 25.4888F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(73.0158F, 5.4152F, 0.8361F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(75.0508F, -30.0163F, 27.6196F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-22.1853F, -10.9209F, 29.769F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(12.8455F, 11.8048F, 25.4888F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone242",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-1.6215F, -9.1173F, 10.1293F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-1.518F, -3.3534F, 15.7137F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone243",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-0.9755F, -3.0907F, 17.5263F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone244",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-0.4989F, -2.8264F, 10.0123F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-30.2806F, 13.8789F, 5.6319F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone247",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone248",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(22.7957F, -28.0242F, -11.1701F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-82.9291F, -20.7587F, -8.2741F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-44.4848F, -29.0165F, 15.8214F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone249",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone252",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(60.0F, -5.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(12.0039F, 5.7246F, 1.0388F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone253",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-12.5F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone257",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -37.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -37.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone258",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 22.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone260",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone261",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone263",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone265",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone268",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone271",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -35.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(1.6123F, -7.2333F, -12.6021F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone272",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone274",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-7.0455F, -0.1575F, -2.5588F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone275",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone276",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone277",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -35.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(3.4512F, -9.3912F, -20.2835F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -35.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone278",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone280",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone281",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone284",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone286",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 35.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone287",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone288",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone289",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(77.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone291",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone292",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 27.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone294",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone296",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(133.9428F, 3.2663F, 31.4565F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone297",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone298",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone5",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(20.0F, 7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(66.8046F, 40.4064F, -15.0686F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(120.7734F, 12.7954F, 3.9555F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone6",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(17.2704F, 3.884F, -9.1078F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(17.2356F, 1.4965F, -9.85F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-0.2555F, 2.1155F, -9.6576F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(7.9268F, 1.5124F, 3.0739F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(17.2704F, 3.884F, -9.1078F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone7",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-0.6543F, 4.9571F, -7.5283F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.6543F, 4.9571F, 7.5283F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone8",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone9",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone10",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone11",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 5.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone12",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 30.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(9.849F, 28.4804F, 20.0053F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone15",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone17",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-55.0F, -32.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(100.9541F, -21.1412F, -18.5162F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(54.7467F, -13.2878F, 4.0986F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-15.7312F, -21.2642F, 12.4713F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(-55.0F, -32.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone18",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -12.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone21",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone22",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -37.5F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -5.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone23",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone25",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone27",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone30",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-111.6362F, -25.9897F, -34.4382F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(-119.5604F, -16.1421F, -12.7027F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(-111.6362F, -25.9897F, -34.4382F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone31",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone32",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone33",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone35",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone36",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 22.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(60.0F, -13.8F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone37",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone38",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone39",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone41",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(10.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone46",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 37.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 37.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone47",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone50",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone51",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone54",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone57",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone60",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone65",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(62.5F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(130.0F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(12.0F, KeyframeAnimations.degreeVec(140.5897F, 27.7445F, -19.9493F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(176.3939F, -24.1283F, 14.5923F), Interpolations.CATMULLROM),
               new Keyframe(24.2917F, KeyframeAnimations.degreeVec(-36.3212F, -2.6408F, 14.1548F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(62.5F, -2.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone66",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(12.0F, KeyframeAnimations.degreeVec(0.0F, 7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(0.0F, 25.39F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.2917F, KeyframeAnimations.degreeVec(0.0F, 34.26F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone67",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-39.6097F, -7.8807F, -15.3372F), Interpolations.CATMULLROM),
               new Keyframe(24.2917F, KeyframeAnimations.degreeVec(-140.0682F, -0.2302F, -8.8765F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(-39.6097F, -7.8807F, -15.3372F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone68",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(12.0F, KeyframeAnimations.degreeVec(48.1833F, -43.3307F, -13.2212F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(29.6808F, -10.4847F, 12.6319F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone69",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone70",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-117.5F, 0.0F, 1.0E-4F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(-117.5F, 0.0F, 1.0E-4F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone72",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(176.9806F, 6.0522F, 19.1795F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(175.5154F, 5.065F, 4.1407F), Interpolations.CATMULLROM),
               new Keyframe(12.0F, KeyframeAnimations.degreeVec(61.945F, 3.8324F, -9.7348F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(107.9262F, -29.8601F, 3.8363F), Interpolations.CATMULLROM),
               new Keyframe(24.2917F, KeyframeAnimations.degreeVec(310.3693F, -6.4481F, 7.1721F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(176.9806F, 6.0522F, 19.1795F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone73",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -7.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -7.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone75",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone76",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.5F), Interpolations.CATMULLROM),
               new Keyframe(24.2917F, KeyframeAnimations.degreeVec(11.5752F, 4.751F, -41.546F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone77",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone78",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(12.0F, KeyframeAnimations.degreeVec(42.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(57.5F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone80",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone83",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -67.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -67.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone84",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(-40.331F, -16.5888F, 62.5418F), Interpolations.CATMULLROM),
               new Keyframe(12.0F, KeyframeAnimations.degreeVec(-43.944F, -32.5538F, 73.895F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone85",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(18.2939F, 16.6657F, 5.4162F), Interpolations.CATMULLROM),
               new Keyframe(24.2917F, KeyframeAnimations.degreeVec(-19.6019F, 11.9127F, 3.814F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone87",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone88",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -27.5F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -27.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone90",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone91",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone94",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(-188.4424F, -27.2451F, 3.8872F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone95",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(12.0F, KeyframeAnimations.degreeVec(19.5684F, 26.128F, 8.8968F), Interpolations.CATMULLROM),
               new Keyframe(24.2917F, KeyframeAnimations.degreeVec(26.1289F, 46.9359F, 19.7161F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone96",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone101",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(0.0F, 7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone103",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone104",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone105",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(10.6275F, 19.6835F, 3.6165F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone108",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone110",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.2917F, KeyframeAnimations.degreeVec(0.0F, 35.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone116",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone117",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(17.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone121",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone124",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-35.0F, -25.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(24.8156F, 24.0179F, 19.4139F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(43.4278F, -3.7672F, 1.1722F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(66.6387F, -19.1838F, -1.7674F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(-35.0F, -25.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone125",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 7.96F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 14.79F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone126",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone127",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -3.15F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone128",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone129",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 22.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(50.0F, 22.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 22.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone130",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone131",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone132",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone133",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone135",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone137",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(-92.5F, 12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-92.5F, 12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone138",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 7.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone140",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone141",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(30.0F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone142",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone143",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-80.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone146",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone148",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -22.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(25.5351F, -36.4292F, -5.9086F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(12.4133F, -24.0338F, -15.1474F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -22.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone150",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(45.0F, 22.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(41.5107F, 9.7021F, -12.4576F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone152",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -40.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone154",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone155",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone156",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone157",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(37.3946F, 3.0414F, -3.9704F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(38.1302F, -10.834F, -14.686F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone160",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(3.904F, -17.0723F, -13.0863F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(4.6756F, -37.0179F, -14.7575F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone163",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone165",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -37.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-12.993F, -35.4917F, 21.6741F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(30.4526F, -22.0956F, -57.391F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -37.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone167",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(12.4475F, -0.866F, -33.2516F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone171",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone172",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, -5.0F, -7.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone174",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone177",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -65.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone183",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 52.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(-1.8803F, -6.5118F, -17.5066F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-1.7933F, 33.138F, 3.8447F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-33.8698F, -21.7434F, 7.6303F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 52.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone184",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(3.483F, -24.7717F, -8.2652F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(3.3607F, -19.7806F, -7.9428F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-1.26F, -32.2569F, -8.8025F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone185",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -7.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone186",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 17.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone188",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone189",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -17.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone190",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 2.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone191",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone193",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone194",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, -32.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, -27.5F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone196",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 12.5F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone197",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(35.6865F, -22.9193F, -15.6265F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(33.4065F, -12.6084F, -8.1926F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(33.1259F, -10.518F, -6.7929F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(35.6865F, -22.9193F, -15.6265F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone198",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone199",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(72.1563F, -12.1471F, 2.301F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(72.1563F, -12.1471F, 2.301F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone200",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone201",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(95.1733F, 26.4883F, 16.8086F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(105.1733F, 26.4883F, 16.8086F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(95.1733F, 26.4883F, 16.8086F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone202",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone203",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-69.2879F, -12.0481F, 29.3426F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-60.7983F, -18.6286F, 25.6091F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-62.1954F, -7.6331F, 31.7251F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(-69.2879F, -12.0481F, 29.3426F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone205",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone206",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone207",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.3241F, 1.6189F, 12.3963F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(-7.2471F, 1.936F, 14.8773F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(-7.4365F, 0.9762F, 7.4365F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-7.0523F, 2.5587F, 19.8422F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(-7.3241F, 1.6189F, 12.3963F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone209",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone210",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone211",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone212",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 27.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone213",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone214",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone215",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 35.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone216",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone217",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone220",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 27.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone224",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -22.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone225",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 22.5F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 35.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 52.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 22.5F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone226",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -20.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone228",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 37.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone229",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone233",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 17.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone239",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .addAnimation(
         "bone299",
         new AnimationChannel(
            Targets.ROTATION,
            new Keyframe[]{
               new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM),
               new Keyframe(24.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), Interpolations.CATMULLROM),
               new Keyframe(32.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), Interpolations.CATMULLROM)
            }
         )
      )
      .build();
}
