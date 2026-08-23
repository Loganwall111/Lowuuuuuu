package net.dabicco.devouringstorms.entity.state;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public class DarkenedMovingBlockRenderState extends MovingBlockRenderState {
   public float brightnessScale = 1.0F;

   public int getBrightness(LightLayer layer, BlockPos pos) {
      return this.scale(super.getBrightness(layer, pos));
   }

   public int getRawBrightness(BlockPos pos, int skyDarken) {
      return this.scale(super.getRawBrightness(pos, skyDarken));
   }

   private int scale(int light) {
      return Math.round((float)light * this.brightnessScale);
   }
}
