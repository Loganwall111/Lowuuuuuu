package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Build #374 -- removes the giant 3D storm preview from the base config
 * screen (user order: "remove the giant preview in the config menu").
 *
 * The preview is gated by two private members (proven from the CI private
 * API dump, ci/api/mod-private.txt):
 *   - previewVisible() : feeds BOTH the layout (rows panel width) and the
 *     draw/click paths, so returning false also lets the row panel expand
 *     to the full width the preview used to steal.
 *   - drawPreview(g)   : the actual 3D model pass (double safety).
 *
 * Default OFF for the preview (i.e. removed) with a "Giant 3D Preview"
 * console toggle to bring it back. Client-only; degrades silently if the
 * base ever renames the members.
 */
@Mixin(WitherStormConfigScreen.class)
public abstract class McsmPreviewRemoveMixin {

    @Inject(method = "previewVisible", at = @At("HEAD"), cancellable = true, remap = false)
    private void dabyws$noGiantPreview(CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance() != null && !McsmExtrasConfig.giantPreviewEnabled) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "drawPreview(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dabyws$skipPreviewDraw(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (Minecraft.getInstance() != null && !McsmExtrasConfig.giantPreviewEnabled) {
            ci.cancel();
        }
    }
}
