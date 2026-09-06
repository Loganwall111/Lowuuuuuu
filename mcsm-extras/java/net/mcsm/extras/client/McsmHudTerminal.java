package net.mcsm.extras.client;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import net.mcsm.extras.McsmExtrasConfig;

/**
 * Devouring Storms: the Story Mode HUD.
 *
 * The hotbar itself moves to the TOP-LEFT and gets bigger, MCSM-style:
 * nine large slots with real item icons, counts and durability
 * decorations, and a bright selection frame. The vanilla bottom hotbar
 * is covered by a cinematic dark bar (the clean engine-level cancellation
 * follows once the private Hud extract method is verified by the CI probe).
 * Beneath the big hotbar sits the holographic terminal panel (build stamp,
 * storm state, position, world time), and while the storm is active the
 * letterbox bars close in like the episode cutscenes.
 *
 * Drawn from the base mod's own HUD element (StormAtmosphereOverlay),
 * attached by McsmHudAttachMixin - all GuiGraphicsExtractor calls used
 * here (fill, text, item, itemDecorations, pose) are verified against the
 * 26.2 client jar by the CI GUI-surface probe.
 */
public final class McsmHudTerminal {

    private static final int SLOT = 26;
    private static final int SLOTS = 9;
    private static final float ICON_SCALE = 1.5F; // 16px icon -> 24px, fills the 26px slot

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

        // --- cover the vanilla bottom hotbar with a cinematic dark bar ------
        // (24px from the bottom hides the hotbar strip, keeps the XP bar and
        // hearts visible above it; goes away entirely in the letterbox above.)
        if (!active) {
            g.fill(0, h - 24, w, h, 0xF2080810);
            g.fill(0, h - 25, w, h - 24, 0xFF3F255A);
        }

        // --- MCSM hotbar: top-left, big, real icons -------------------------
        int px = 4;
        int py = 4;
        int pw = SLOTS * SLOT + 8;
        g.fill(px, py, px + pw, py + SLOT + 8, 0xB00D0D16);
        g.fill(px, py, px + pw, py + 1, 0xFF6A8FF7);
        g.fill(px, py + SLOT + 7, px + pw, py + SLOT + 8, 0xFF263165);

        int selected = player.getInventory().selected;
        Matrix3x2fStack pose = g.pose();
        for (int i = 0; i < SLOTS; i++) {
            int sx = px + 4 + i * SLOT;
            int sy = py + 4;
            g.fill(sx, sy, sx + SLOT, sy + SLOT, 0x66000000);
            if (i == selected) {
                g.fill(sx - 1, sy - 1, sx + SLOT + 1, sy, 0xFFFFFFFF);
                g.fill(sx - 1, sy + SLOT, sx + SLOT + 1, sy + SLOT + 1, 0xFFFFFFFF);
                g.fill(sx - 1, sy, sx, sy + SLOT, 0xFFFFFFFF);
                g.fill(sx + SLOT, sy, sx + SLOT + 1, sy + SLOT, 0xFFFFFFFF);
                g.fill(sx, sy, sx + SLOT, sy + SLOT, 0x337FA8FF);
            } else {
                g.fill(sx, sy, sx + SLOT, sy + 1, 0x44FFFFFF);
                g.fill(sx, sy, sx + 1, sy + SLOT, 0x44FFFFFF);
            }
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                // icon + count/durability, drawn 1.5x inside the big slot
                pose.pushMatrix();
                pose.translate(sx + 1, sy + 1);
                pose.scale(ICON_SCALE);
                g.item(stack, 0, 0);
                g.itemDecorations(mc.font, stack, 0, 0);
                pose.popMatrix();
            }
        }

        // --- holographic terminal panel beneath the hotbar ------------------
        String[] lines = new String[] {
            "\u00a79DEVOURING STORMS",
            "build " + McsmExtrasConfig.BUILD_VERSION,
            "storm: " + (active ? "\u00a7cACTIVE" : "\u00a77dormant"),
            "xyz " + player.blockPosition().getX()
                   + " " + player.blockPosition().getY()
                   + " " + player.blockPosition().getZ(),
            "time " + (mc.level.getOverworldClockTime() % 24000) / 1000 + "h",
        };
        int ty = py + SLOT + 12;
        int tw = 128;
        int th = lines.length * 11 + 8;
        g.fill(px, ty, px + tw, ty + th, 0x88101018);
        g.fill(px, ty, px + 2, ty + th, 0xFF6A8FF7);
        for (int i = 0; i < lines.length; i++) {
            g.text(mc.font, lines[i], px + 6, ty + 4 + i * 11,
                    i == 0 ? 0xFFBFD3FF : 0xFF9FB4D8, false);
        }
    }
}
