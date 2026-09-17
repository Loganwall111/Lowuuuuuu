package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Build #375 -- the DS reskin for EVERY screen the mod does not already
 * own (world select, new world, resource packs, pause, loading, and any
 * vanilla screen we have not individually reworked). The standing order:
 * "the rest of it, resource packs, create world, world select, loading
 * screen, make it all match the DS look."
 *
 * This hooks Screen.extractRenderState - public, non-final, proven
 * signature from the 26.2 client dump - at HEAD, so the DS backdrop goes
 * BEHIND whatever the screen draws itself. Screens with their own
 * overrides (TitleScreen, LevelLoadingScreen, PauseScreen, and our own
 * screens) never hit this method, so nothing owned is touched.
 */
@Mixin(Screen.class)
public abstract class McsmScreenReskinMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("HEAD"))
    private void dabyws$dsReskinBackdrop(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        // our own screens (console / texture painter / ...) bring their own
        if (self instanceof net.mcsm.extras.client.McsmExtrasScreen
                || self instanceof net.mcsm.extras.client.McsmTexturePainterScreen) {
            return;
        }
        // the title owns its own overhaul; the cinematic owns the frame
        if (self instanceof net.minecraft.client.gui.screens.TitleScreen) {
            return;
        }
        if (net.mcsm.extras.client.McsmCinematic.isSequenceActive()) {
            return;
        }
        int w = self.width;
        int h = self.height;
        if (w <= 0 || h <= 0) {
            return;
        }
        // BUILD #464 -- and it is the mod's own sky rather than a dark plate:
        // "fix the main menu be black" applies to every screen this reskin owns
        // (world select, create world, resource packs, and anything else the mod
        // has not reworked by hand). 0.88 keeps the widgets readable on top.
        net.mcsm.extras.client.McsmMenuSky.paint(g, w, h, 0.88F);
        g.fillGradient(0, h * 2 / 3, w, h, 0x00000000, 0x662A1A4A);
        // faint drifting motes so the backdrop is alive, not flat
        long t = System.currentTimeMillis();
        for (int i = 0; i < 14; i++) {
            long seed = (i + 1) * 0x9E3779B97F4A7CL;
            double speed = 3.0D + ((seed >>> 20) & 0xF) * 1.2D;
            double x = (double) ((int) (((seed >>> 40) / (double) (1L << 24)) * w + t * speed / 100.0D) % w);
            double y = ((seed >>> 16) & 0xFFFF) / 65535.0D * h;
            int s = 1 + (i % 2);
            int col = (i % 3 == 0) ? 0x26D9A441 : ((i % 3 == 1) ? 0x1E9FEFFF : 0x226A2AC8);
            g.fill((int) x, (int) y, (int) x + s, (int) y + s, col);
        }
        // silver frame, matching the title + in-game border (Image 3)
        if (McsmExtrasConfig.uiBorderLines) {
            g.fill(0, 0, w, 2, 0xFF8A8A9E);
            g.fill(0, h - 2, w, h, 0xFF8A8A9E);
            g.fill(0, 0, 2, h, 0xFF8A8A9E);
            g.fill(w - 2, 0, w, h, 0xFF8A8A9E);
        }
        // the DS corner mark, so a reskinned screen is unmistakable
        g.fill(6, 6, 18, 8, 0x88D9A441);
        g.fill(6, 6, 8, 18, 0x88D9A441);
    }
}
