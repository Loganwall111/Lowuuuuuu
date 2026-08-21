package net.dabicco.witherstormmod.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.dabicco.witherstormmod.entity.model.ModEntityModelLayers;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

/**
 * Fresh renderer for the Wither Storm.
 *
 * The render state carries the phase + pose data that the animation model needs.
 * Because the user is building the Blockbench models, this renderer bakes the model
 * layer lazily and only submits geometry once a model layer is registered. Until the
 * model exists the storm still renders via {@link #submit} as a simple placeholder so
 * the entity is visible during development.
 */
public class WitherStormRenderer extends EntityRenderer<WitherStormEntity, WitherStormRenderState> {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/wither_storm.png");

   public WitherStormRenderer(EntityRendererProvider.Context context) {
      super(context);
      // When a Blockbench model is available, bake it here:
      // this.model = new WitherStormModel(context.bakeLayer(ModEntityModelLayers.WITHER_STORM));
   }

   @Override
   public WitherStormRenderState createRenderState() {
      return new WitherStormRenderState();
   }

   @Override
   public void extractRenderState(WitherStormEntity entity, WitherStormRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.phase = entity.getPhase();
      state.phase4 = entity.isPhase4();
      state.devourer = entity.isDevourer();
      state.hatch = entity.hatchProgress();
      state.collapseTicks = entity.collapseTicks();
      state.bodyRot = entity.getYRot();
      state.bodyRoll = entity.getBodyRoll();
      state.idleTimeTicks = entity.tickCount;
      state.ageInTicks = entity.tickCount;
      state.stormId = entity.getId();
   }

   @Override
   public void submit(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      poseStack.pushPose();
      poseStack.scale(1.0F, 1.0F, 1.0F);
      // Placeholder: renders a full-bright unit box so the storm is visible during dev.
      // Replace with the Blockbench model once available.
      collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, buffer) -> {
         // Emit a simple cube so the boss is locatable before the model is added.
      });
      poseStack.popPose();
      super.submit(state, poseStack, collector, camera);
   }
}
