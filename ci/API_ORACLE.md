# The API oracle: what the frozen jars actually declare

Recorded from the compile-time oracle the build dumps into
`ci-out/run-<N>/vanilla-api.txt` (`javap -p` for the vanilla and base-mod
classes, plus `javap -v` constant-pool probes for the render-type call sites).
Nothing here is a guess: each line is quoted from that dump, with the run it came
from. When a signature below moves, the injection that names it is what breaks --
and `ci/check_phase_uniform.py` reads this file, so the build says so.

## Run 519 -- the members the storm's render path is welded to

    # net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer
      private final net.dabicco.witherstormmod.entity.model.HugeAssBackModel hugeAssBackModel;
      private boolean previewShadowPass;
      private void submitGrowth5(WitherStormRenderState, PoseStack, SubmitNodeCollector);
      protected int getModelTint(WitherStormRenderState);
      public void submit(WitherStormRenderState, PoseStack, SubmitNodeCollector, CameraRenderState);

    # com.mojang.blaze3d.vertex.PoseStack
      public void scale(float, float, float);
      public void pushPose();
      public void popPose();

## What is welded to what

| Mixin | Target | Why it must exist |
| --- | --- | --- |
| `McsmHugeBackAttachMixin` | `WitherStormRenderer.submitGrowth5` (HEAD + RETURN) | the phase-5.5 upper back is submitted here; the mixin publishes the body's centre so the 1.72x enlargement can be taken about it (#423) and releases it afterwards |
| `McsmHugeBackPoseMixin` | `PoseStack.scale(float,float,float)` RETURN | that enlargement is the one that flings the back 34 blocks off the chassis when taken about the model origin (`ci/measure_hugeback.py` proves the arithmetic) |
| `McsmStormBodyNeutralTintMixin` | `WitherStormRenderer.getModelTint` (+ the `previewShadowPass` field) | the body's vertex multiplier, i.e. the colour the whole creature wears (#438) |
| `McsmBodyEyesRenderTypeMixin` | `WitherStormRenderer.submit` render-type calls | the teeth/eye pixels draw on the unlit eyes material, which is the only native glow (#422) |
| `McsmHeadEyesRenderTypeMixin` | `WitherStormHeadRenderer.submit` render-type calls | same, for the heads |

## Still open

The render-type call sites those two eyes mixins redirect
(`FoglessRenderTypes.eyes`, `GlowRenderTypes.emitterMark`, `GlowRenderTypes.bloomSource`,
`StormSkins.phase6Emissive`) are NOT in the dump yet: run 519 stopped before them
because one javap in that section aborted the group (fixed in #440 -- each probe
is now allowed to fail and says so). The next run's dump carries them, and this
file gets their quoted signatures then.
