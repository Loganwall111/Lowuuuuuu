package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.entity.VoidDwellerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;

/**
 * BUILD #483 -- THE DWELLER'S OWN VOICE, IN THE FRAME.
 *
 * <p>THE BRIEF. "Right-clicking a Void NPC must open an overlay screen that outputs
 * show-inspired lore texts and glowing amethyst-purple chat logs using an animated
 * fade-in typewriting effect."
 *
 * <p>HOW IT IS DONE WITHOUT A PACKET. The dweller's line is already on its synced
 * data -- {@code SAY}, with how long it has left and how long it was given -- because
 * that is how the cast's talk gesture has always travelled. So the overlay needs
 * nothing added to the network: it finds the nearest dweller that is talking, reads
 * its line off the entity, and reveals it letter by letter as the line's own clock
 * runs out. {@code revealed = (1 - remaining / total) * length}, which means the text
 * finishes exactly when the voice does, and the voice is the dweller's own
 * (the mod's own Ogg cues, played where it stands).
 *
 * <p>The panel is amethyst-purple on near-black, it fades in with the first letters
 * and out with the last, and it only ever appears in the void -- this is the deep
 * talking to a player who is falling through it, not a chat window.
 */
public final class McsmVoidDwellerTalk {

    /** How close a dweller has to be for its line to be the frame's. */
    private static final double HEARING = 26.0D;

    private McsmVoidDwellerTalk() {
    }

    /** Drawn every frame on the same HUD hook as the deep and the void floor. */
    public static void draw(GuiGraphicsExtractor g) {
        if (!McsmExtrasConfig.voidDescent) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.font == null || mc.player == null) {
                return;
            }
            ClientLevel level = mc.level;
            LocalPlayer player = mc.player;
            if (level == null || !level.dimension().equals(McsmVoid.DIMENSION)) {
                return;
            }
            VoidDwellerEntity near = nearest(level, player);
            if (near == null) {
                return;
            }
            String line = near.saying();
            int left = near.sayTicks();
            int total = near.sayTotal();
            if (line == null || line.isEmpty() || left <= 0) {
                return;
            }
            // the typewriter: the line is revealed exactly in step with its clock
            float done = 1.0F - Math.max(0, left) / (float) total;
            int revealed = Math.max(1, (int) (line.length() * Math.max(0.0F, Math.min(1.0F, done))));
            String shown = line.substring(0, Math.min(line.length(), revealed));
            if (revealed < line.length() && (System.currentTimeMillis() / 120L) % 2L == 0L) {
                shown = shown + "_";
            }
            // the fade: in with the first letters, out with the last of the clock
            float fade = Math.min(1.0F, done * 6.0F);
            if (left < 12) {
                fade = Math.min(fade, left / 12.0F);
            }
            int a = (int) (200 * Math.max(0.0F, Math.min(1.0F, fade)));
            if (a <= 6) {
                return;
            }
            int w = g.guiWidth();
            int h = g.guiHeight();
            int panelH = 40;
            int y0 = h - panelH - 26;
            // the panel: near-black, with the deep's own violet at its edges
            g.fillGradient(0, y0, w, y0 + panelH, (a << 24) | 0x120A24, (a << 24) | 0x2A0E3A);
            g.fill(0, y0, w, y0 + 1, (Math.min(255, a + 40) << 24) | 0xE070FF);
            g.fill(0, y0 + panelH - 1, w, y0 + panelH,
                    (Math.min(255, a + 40) << 24) | 0x8A2BE2);
            // its name, in the void's own violet, and then the line in amethyst
            g.centeredText(mc.font, "VOID DWELLER", w / 2, y0 + 7, (a << 24) | 0xB07BFF);
            g.centeredText(mc.font, shown, w / 2, y0 + 22, (a << 24) | 0xE0C8FF);
        } catch (Throwable ignored) {
            // a line that cannot be drawn is not a reason to lose a frame
        }
    }

    /** The closest dweller that is talking, and only while it is talking. */
    private static VoidDwellerEntity nearest(ClientLevel level, LocalPlayer player) {
        AABB box = player.getBoundingBox().inflate(HEARING);
        VoidDwellerEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (VoidDwellerEntity e : level.getEntitiesOfClass(VoidDwellerEntity.class, box)) {
            if (e.sayTicks() <= 0) {
                continue;
            }
            double d = e.distanceToSqr(player);
            if (d < bestD) {
                bestD = d;
                best = e;
            }
        }
        return best;
    }
}
