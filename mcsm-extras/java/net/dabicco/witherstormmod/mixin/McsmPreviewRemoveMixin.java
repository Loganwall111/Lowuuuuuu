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

    // Build #378 — remove the leftover "Preview" wording from the settings.
    // The base screen still adds two preview-toggle buttons in the bottom
    // row, "Model: ON/OFF" and "Gigantic: ON/OFF", even though the 3D preview
    // they control is gated off above (so they do nothing by default). Hide
    // them while the preview is disabled; they reappear as the working
    // preview controls if the "Giant 3D Preview" console toggle is enabled.
    @Inject(method = "extractRenderState", at = @At("HEAD"), remap = false)
    private void dabyws$hidePreviewToggles(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        if (Minecraft.getInstance() == null || McsmExtrasConfig.giantPreviewEnabled) {
            return;
        }
        WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
        for (Object child : self.children()) {
            if (!(child instanceof net.minecraft.client.gui.components.AbstractButton)) {
                continue;
            }
            net.minecraft.client.gui.components.AbstractButton b =
                    (net.minecraft.client.gui.components.AbstractButton) child;
            net.minecraft.network.chat.Component msg = b.getMessage();
            if (msg == null) {
                continue;
            }
            String s = msg.getString();
            if (s.startsWith("Model") || s.startsWith("Gigantic")) {
                b.visible = false;
                b.active = false;
            }
        }
    }
}
