package net.dabicco.devouringstorms.entity.model;

import net.dabicco.devouringstorms.entity.state.WitherStormRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class WitherStormModel extends EntityModel<WitherStormRenderState> {
   private final WitherCommandBlock commandBlock;
   private final WitherStormP4 stormP4;
   private boolean isPhase4;

   public WitherStormModel(ModelPart commandBlockPart, ModelPart stormP4Part) {
      super(commandBlockPart);
      this.commandBlock = new WitherCommandBlock(commandBlockPart);
      this.stormP4 = new WitherStormP4(stormP4Part);
   }

   public void setupAnim(WitherStormRenderState state) {
      this.isPhase4 = state.phase4;
      if (state.phase4) {
         this.stormP4.setupAnim(state);
      } else {
         this.commandBlock.setupAnim(state);
      }
   }
}
