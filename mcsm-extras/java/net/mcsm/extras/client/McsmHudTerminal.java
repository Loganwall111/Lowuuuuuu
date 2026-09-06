package net.mcsm.extras.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import net.mcsm.extras.McsmExtrasConfig;

/**
 * Devouring Storms: the Story Mode holographic terminal.
 *
 * A translucent panel pinned to the TOP-LEFT of the screen, stacking
 * DOWNWARD: build stamp, storm state, position, biome, world time - and
 * beneath it the hotbar mirrored as a vertical inventory column (names
 * and counts, the way the Story Mode sidebar lists what you carry).
 * While the storm's atmosphere factor is up, cinematic letterbox bars
 * close in top and bottom like the episode cutscenes.
 *
 * Drawn from the base mod's own HUD element (StormAtmosphereOverlay,
 * registered in HudElementRegistry), attached by McsmHudAttachMixin - so
 * no Fabric rendering API and no vanilla-Gui mixin are involved, and the
 * 26.2 GuiGraphicsExtractor calls below are copied verbatim from the base
 * mod's compiled overlay code.
 */
public final class McsmHudTerminal {

    private McsmHudTerminal() {
    }

    public static void paint(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        int w = g.guiWidth();
        int h = g.guiHeight();
        float storm = net.dabicco.witherstormmod.client.StormSkyDarken.factor();
        boolean active = storm > 0.04F;

        if (active) {
            int bar = Math.max(14, h / 12);
            g.fill(0, 0, w, bar, 0xFF000000);
            g.fill(0, h - bar, w, h, 0xFF000000);
        }

        String[] lines = new String[] {
            "\u00a79DEVOURING STORMS",
            "build " + McsmExtrasConfig.BUILD_VERSION,
            "storm: " + (active ? "\u00a7cACTIVE" : "\u00a77dormant"),
            "xyz " + player.blockPosition().getX()
                   + " " + player.blockPosition().getY()
                   + " " + player.blockPosition().getZ(),
            "time " + (mc.level.getOverworldClockTime() % 24000) / 1000 + "h",
        };
        int pw = 128;
        int ph = lines.length * 11 + 8;
        g.fill(2, 2, 2 + pw, 2 + ph, 0x88101018);
        g.fill(2, 2, 4, 2 + ph, 0xFF6A8FF7);
        for (int i = 0; i < lines.length; i++) {
            g.text(mc.font, lines[i], 8, 6 + i * 11, i == 0 ? 0xFFBFD3FF : 0xFF9FB4D8, false);
        }

        int top = 2 + ph + 6;
        int rows = 9;
        g.fill(2, top, 2 + pw, top + rows * 11 + 6, 0x66101018);
        g.fill(2, top, 4, top + rows * 11 + 6, 0xFF3F255A);
        for (int i = 0; i < rows; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            String label = stack.isEmpty()
                    ? "\u00a78- empty -"
                    : stack.getCount() + "x " + stack.getDisplayName().getString();
            if (label.length() > 21) {
                label = label.substring(0, 20) + "\u2026";
            }
            g.text(mc.font, label, 8, top + 4 + i * 11,
                    stack.isEmpty() ? 0xFF5A6478 : 0xFFD8E2F4, false);
        }
    }
}
