package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.entity.model.WitherStormP4;
import net.mcsm.extras.client.McsmGlossSheen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #368: phase 4+ body. Right after the base model is drawn, re-render
 * the same part as the native translucent gloss sheen (the 4-quadrant cosmic
 * atlas underneath plus the scrolling storm_gloss coat on top).
 */
@Mixin(WitherStormP4.class)
public abstract class McsmGlossP4Mixin {

    @Inject(method = "renderToBuffer", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$glossCoat(PoseStack poseStack, VertexConsumer consumer,
                                int light, int overlay, CallbackInfo ci) {
        McsmGlossSheen.coatFor(this, poseStack, light, overlay);
    }
}
