package net.dabicco.witherstormmod.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.client.FoglessRenderTypes;
import net.dabicco.witherstormmod.client.StormAnimation;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.dabicco.witherstormmod.entity.animation.WitherStormP4Anim;
import net.dabicco.witherstormmod.entity.model.HugeAssBackModel;
import net.dabicco.witherstormmod.entity.model.HunchbackGrowth;
import net.dabicco.witherstormmod.entity.model.ModEntityModelLayers;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

/**
 * Fresh renderer for the Wither Storm.
 *
 * Bakes the working box models (HunchbackGrowth + HugeAssBackModel) from the model
 * layer registry and renders them for the appropriate phase. The models carry their own
 * phase-driven {@code setupAnim} (growth/cover count etc.), and the {@link StormAnimation}
 * helper drives the existing {@code AnimationDefinition}s (spawn/idle) from the
 * {@code entity.animation} package on top.
 */
public class WitherStormRenderer extends EntityRenderer<WitherStormEntity, WitherStormRenderState> {
   private static final Identifier BODY_TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/wither_storm.png");
   private static final Identifier PHASE4_TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/phase_4_assets.png");

   private final HunchbackGrowth hunchbackModel;
   private final HugeAssBackModel backModel;
   private final StormAnimation bodyAnim;
   private final StormAnimation backAnim;

   public WitherStormRenderer(EntityRendererProvider.Context context) {
      super(context);
      // Bake working models from their registered layers.
      this.hunchbackModel = new HunchbackGrowth(context.bakeLayer(ModEntityModelLayers.HUNCHBACK_GROWTH));
      this.backModel = new HugeAssBackModel(context.bakeLayer(ModEntityModelLayers.HUGE_ASS_BACK));

      // The existing spawn/idle AnimationDefinitions target the Blockbench bone layout.
      this.bodyAnim = new StormAnimation(this.hunchbackModel.root(), WitherStormP4Anim.Idle, WitherStormP4Anim.Spawn, null);
      this.backAnim = new StormAnimation(this.backModel.root(), WitherStormP4Anim.Idle, WitherStormP4Anim.Spawn, null);
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
      state.playingSpawnAnimation = entity.isPlayingSpawnAnimation();
      state.spawnElapsedTicks = entity.getAnimationProgress();
      state.snatchActive = entity.getSnatchId() >= 0;
   }

   @Override
   public void submit(WitherStormRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      poseStack.pushPose();
      poseStack.translate(0.0, state.phase4 ? -12.0 : 0.0, 0.0);

      boolean spawnPlaying = state.playingSpawnAnimation;
      float spawnProgress = state.spawnElapsedTicks;
      float age = state.ageInTicks;

      // Phase-driven setupAnim (growth/cover counts) on the working models.
      this.hunchbackModel.setupAnim(state);
      this.backModel.setupAnim(state);

      // Drive the existing animation definitions.
      this.bodyAnim.tick(age, spawnPlaying, spawnProgress, false);
      this.backAnim.tick(age, spawnPlaying, spawnProgress, false);
      this.hunchbackModel.root().getAllParts().forEach(ModelPart::resetPose);
      this.backModel.root().getAllParts().forEach(ModelPart::resetPose);
      this.bodyAnim.apply(age);
      this.backAnim.apply(age);

      Identifier tex = state.phase4 ? PHASE4_TEXTURE : BODY_TEXTURE;
      collector.submitModel(this.hunchbackModel, state, poseStack, FoglessRenderTypes.bodyCutout(tex), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, (TextureAtlasSprite) null, 0, (ModelFeatureRenderer.CrumblingOverlay) null);
      collector.submitModel(this.backModel, state, poseStack, FoglessRenderTypes.bodyCutout(tex), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, (TextureAtlasSprite) null, 0, (ModelFeatureRenderer.CrumblingOverlay) null);

      poseStack.popPose();
      super.submit(state, poseStack, collector, camera);
   }
}
