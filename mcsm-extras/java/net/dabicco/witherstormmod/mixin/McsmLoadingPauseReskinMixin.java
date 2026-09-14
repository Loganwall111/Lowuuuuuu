package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.mcsm.extras.client.McsmCinematic;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Build #375 -- the DS reskin for the two screens the generic Screen
 * reskin cannot reach because they override extractRenderState
 * themselves (both public, same proven descriptor):
 *
 *   - LevelLoadingScreen : world loading - the DS backdrop + the storm
 *     silhouette replace the vanilla grey.
 *   - PauseScreen        : the in-game pause - same treatment.
 *
 * Both keep drawing their own functional widgets (progress bar, buttons)
 * on top; only the backdrop becomes Devouring Storms.
 */
@Mixin({LevelLoadingScreen.class, PauseScreen.class})
public abstract class McsmLoadingPauseReskinMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("HEAD"))
    private void dabyws$dsLoadingBackdrop(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        if (net.mcsm.extras.client.McsmCinematic.isSequenceActive()) {
            return;
        }
        net.minecraft.client.gui.screens.Screen self =
                (net.minecraft.client.gui.screens.Screen) (Object) this;
        int w = self.width;
        int h = self.height;
        if (w <= 0 || h <= 0) {
            return;
        }
        // the DS dark gradient
        g.fillGradient(0, 0, w, h, 0xFF08060D, 0xFF0C0714);
        g.fillGradient(0, h * 2 / 3, w, h, 0x00000000, 0x662A1A4A);

        // a slow-drifting debris field - the loading screen feels like the
        // storm is turning above you
        long t = System.currentTimeMillis();
        for (int i = 0; i < 22; i++) {
            long seed = (i + 1) * 0x9E3779B97F4A7CL;
            double speed = 4.0D + ((seed >>> 20) & 0xF) * 1.5D;
            double x = (double) ((int) (((seed >>> 40) / (double) (1L << 24)) * w + t * speed / 100.0D) % w);
            double y = ((seed >>> 16) & 0xFFFF) / 65535.0D * h;
            int s = 1 + (i % 3);
            int col = (i % 4 == 0) ? 0x33D9A441 : ((i % 4 == 1) ? 0x269FEFFF
                    : ((i % 4 == 2) ? 0x2E6A2AC8 : 0x2204030A));
            g.fill((int) x, (int) y, (int) x + s, (int) y + s, col);
        }

        // the little wither face, dead-centre, dim - "something is here"
        int fw = Math.min(46, w / 8);
        int fx = (w - fw) / 2;
        int fy = (int) (h * 0.30D);
        g.fill(fx, fy, fx + fw, fy + (int) (fw * 0.62D), 0xE60A0910);
        g.fill(fx + (int) (fw * 0.16D), fy + (int) (fw * 0.22D),
                fx + (int) (fw * 0.34D), fy + (int) (fw * 0.30D), 0xFF9FEFFF);
        g.fill(fx + (int) (fw * 0.62D), fy + (int) (fw * 0.22D),
                fx + (int) (fw * 0.80D), fy + (int) (fw * 0.30D), 0xFF9FEFFF);
        g.fill(fx + (int) (fw * 0.30D), fy + (int) (fw * 0.46D),
                fx + (int) (fw * 0.70D), fy + (int) (fw * 0.56D), 0xFFEAF6FF);

        // silver frame + the DS corner mark
        if (McsmExtrasConfig.uiBorderLines) {
            g.fill(0, 0, w, 2, 0xFF8A8A9E);
            g.fill(0, h - 2, w, h, 0xFF8A8A9E);
            g.fill(0, 0, 2, h, 0xFF8A8A9E);
            g.fill(w - 2, 0, w, h, 0xFF8A8A9E);
        }
        g.fill(6, 6, 18, 8, 0x88D9A441);
        g.fill(6, 6, 8, 18, 0x88D9A441);
    }
}
