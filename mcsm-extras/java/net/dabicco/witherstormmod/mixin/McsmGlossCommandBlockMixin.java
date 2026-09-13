package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.entity.model.WitherCommandBlock;
import net.mcsm.extras.client.McsmGlossSheen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #368: command-block stage body (early phases). Right after the base
 * model is drawn, re-render the same part as the native translucent gloss
 * sheen so the blocky body reads as shiny wet obsidian.
 */
@Mixin(WitherCommandBlock.class)
public abstract class McsmGlossCommandBlockMixin {

    @Inject(method = "renderToBuffer", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$glossCoat(PoseStack poseStack, VertexConsumer consumer,
                                int light, int overlay, CallbackInfo ci) {
        McsmGlossSheen.coatFor(this, poseStack, light, overlay);
    }
}
