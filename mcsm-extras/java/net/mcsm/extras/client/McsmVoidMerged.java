package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoidTiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

/**
 * BUILD #485 -- MERGED VOID: dimension directly underneath overworld hundreds blocks down
 * User idea: instead of instant teleport from first void to second dimension, place second dimension
 * directly underneath overworld hundreds blocks down, skybox merges slowly to color of that area,
 * feels crazier as actual thing down there.
 * This draws fog overlay in overworld when falling below minY, slowly turning to first layer color.
 */
public final class McsmVoidMerged {

    private McsmVoidMerged() {}

    public static void draw(GuiGraphicsExtractor g) {
        try {
            if (!McsmExtrasConfig.voidMerged) return;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc == null ? null : mc.player;
            if (player == null) return;
            if (player.level() == null) return;
            // Only in overworld (not in void) - use ResourceKey check to avoid location() API change
            try {
                if (player.level().dimension().equals(net.mcsm.extras.McsmVoid.DIMENSION)) return;
                if (!player.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                    // Only show merged fog in overworld
                    //return;
                }
            } catch (Throwable ignored) {}

            double y = player.getY();
            int minY = player.level().getMinY(); // usually -64
            if (y > minY + 20) return;
            if (y > minY - 200) {
                // Falling hundreds blocks - skybox slowly turns color of first area
                float progress = (float)((minY + 20 - y) / (20 + 200));
                progress = Math.max(0, Math.min(1, progress));
                // First layer color: orange #FF5A1E -> pink #FF3FA8 gradient, use luminous cavern plum
                int r1 = 0xFF, g1 = 0x5A, b1 = 0x1E;
                int r2 = 0x2E, g2 = 0x0B, b2 = 0x36;
                int r = (int)(r1 + (r2 - r1) * progress);
                int gC = (int)(g1 + (g2 - g1) * progress);
                int b = (int)(b1 + (b2 - b1) * progress);
                int alpha = (int)(progress * 160);
                int w = g.guiWidth();
                int h = g.guiHeight();
                // Top fog
                g.fill(0, 0, w, (int)(h * 0.3f * progress), (alpha << 24) | (r << 16 | gC << 8 | b));
                // Bottom fog
                g.fill(0, h - (int)(h * 0.4f * progress), w, h, (alpha << 24) | (r << 16 | gC << 8 | b));
                // Center vignette - slowly turning
                int vignetteAlpha = (int)(progress * 100);
                g.fill(0, 0, w, h, (vignetteAlpha << 24) | (r << 16 | gC << 8 | b));
                if (progress > 0.5f) {
                    int textAlpha = (int)((progress - 0.5f) / 0.5f * 255);
                    g.centeredText(mc.font, "FALLING HUNDREDS OF BLOCKS - VOID DIRECTLY UNDERNEATH", w/2, 20, (textAlpha << 24) | 0xFFFFFF);
                    g.centeredText(mc.font, "skybox merging to first layer - " + (int)(progress*100) + "%", w/2, 35, (textAlpha << 24) | 0xFFAA55);
                }
            }
        } catch (Throwable ignored) {}
    }
}
