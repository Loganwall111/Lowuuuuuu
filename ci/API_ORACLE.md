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

## Run 521 -- the render-type call sites the glow depends on

`javap -v` constant-pool probes of the two renderers that draw the teeth and the
eyes (filtered to Methodref/InterfaceMethodref entries naming eyes, emitter,
bloom, glow, mark, Fogless, RenderType or Skins):

    # WitherStormRenderer
      FoglessRenderTypes.eyes(Identifier) -> RenderType          # the redirect target
      FoglessRenderTypes.bodyCutout(Identifier)
      FoglessRenderTypes.bodyCutout(Identifier, boolean)
      FoglessRenderTypes.entityTranslucentEmissive(Identifier)
      FoglessRenderTypes.fogless()Z / reverseShading()Z
      StormSkins.teethGlow(double) -> Identifier
      StormSkins.phase4() / devourer() / legacy()
      RenderTypes.eyes(Identifier)                                # vanilla's unlit eyes material
      WitherStormRenderer.pieceType(Identifier[, boolean])

    # WitherStormHeadRenderer
      GlowRenderTypes.emitterMark(Identifier) -> RenderType      # a redirect target
      GlowRenderTypes.bloomSource(Identifier) -> RenderType      # a redirect target
      GlowRenderTypes.bloomOccluder(Identifier) / bloomEraseOccluded(Identifier)
      FoglessRenderTypes.bodyCutout(Identifier)
      StormSkins.phase4() -> Identifier
      (no RenderTypes.eyes call at all)

    # StormSkins (the base's own, from the same dump)
      legacy(), phase4(), devourer(), teethGlow(double), og()

## What this settled

* `McsmBodyEyesRenderTypeMixin` redirects `FoglessRenderTypes.eyes` -- PRESENT in
  the body renderer, so the body's teeth/eye pass is routed to vanilla's unlit
  eyes material as intended.
* `McsmHeadEyesRenderTypeMixin` redirects `GlowRenderTypes.emitterMark` and
  `GlowRenderTypes.bloomSource` -- both PRESENT in the head renderer.
* its third redirect, `RenderTypes.eyes` inside `submit`, was NOT present: the
  head renderer makes no such call, so that redirect could never fire and the
  body atlas was never kept out of the head's glow pass (built #442: the two live
  passes now wear `StormSkins.phase6Emissive()` directly, the same white-mask
  atlas the mini-head glow already uses).
* `StormSkins.phase6Emissive()`, `body(double)` and the rest are the OVERLAY's --
  the base has only `legacy/phase4/devourer/teethGlow/og`.
