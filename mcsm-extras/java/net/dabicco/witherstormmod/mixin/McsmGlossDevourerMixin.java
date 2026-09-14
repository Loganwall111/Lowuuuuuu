package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.entity.model.WitherStormDevourer;
import net.mcsm.extras.client.McsmGlossSheen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #368: devourer body. Right after the base model is drawn, re-render
 * the same part as the native translucent gloss sheen.
 */
@Mixin(WitherStormDevourer.class)
public abstract class McsmGlossDevourerMixin {

    @Inject(method = "renderToBuffer", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$glossCoat(PoseStack poseStack, VertexConsumer consumer,
                                int light, int overlay, CallbackInfo ci) {
        // Build #385: flat gloss coat REMOVED (animated PBR glint replaces it).
        return;
    }
}
