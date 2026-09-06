package net.mcsm.extras.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickTracker;
import net.minecraft.world.entity.Entity;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Devouring Storms: the Story Mode holographic terminal.
 *
 * A translucent panel pinned to the TOP-LEFT of the screen, stacking
 * DOWNWARD: build stamp, storm state, position, biome, world time - and
 * beneath it the hotbar mirrored as a vertical inventory column, the way
 * the Story Mode sidebar carries your items. While a Wither Storm is
 * alive in the world, cinematic letterbox bars close in top and bottom,
 * like the episode cutscenes.
 *
 * Deliberately mixin-free: everything here is stable Fabric/GUI API, so a
 * vanilla rendering rewrite can never turn this into a launch crash.
 */
public class McsmHudTerminal implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.register(this::paint);
    }

    private void paint(DrawContext ctx, RenderTickTracker ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        boolean storm = false;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof WitherStormEntity) {
                storm = true;
                break;
            }
        }
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        if (storm) {
            int bar = h / 12;
            ctx.fill(0, 0, w, bar, 0xFF000000);
            ctx.fill(0, h - bar, w, h, 0xFF000000);
        }

        String bio = mc.level.getBiome(mc.player.blockPosition())
                .unwrapKey().map(k -> k.location().getPath()).orElse("?");
        String[] lines = new String[] {
            "DEVOURING STORMS",
            "build " + McsmExtrasConfig.BUILD_VERSION,
            "storm: " + (storm ? "ACTIVE" : "dormant"),
            "xyz " + mc.player.blockPosition().getX()
                   + " " + mc.player.blockPosition().getY()
                   + " " + mc.player.blockPosition().getZ(),
            "biome " + bio,
            "time " + (mc.level.getDayTime() % 24000) / 1000 + "h",
        };
        int pw = 122;
        int ph = lines.length * 11 + 8;
        ctx.fill(2, 2, 2 + pw, 2 + ph, 0x88101018);
        ctx.fill(2, 2, 4, 2 + ph, 0xFF6A8FF7);
        for (int i = 0; i < lines.length; i++) {
            ctx.drawString(mc.font, lines[i], 8, 6 + i * 11,
                    i == 0 ? 0xFFBFD3FF : 0xFF9FB4D8);
        }

        int top = 2 + ph + 6;
        ctx.fill(2, top, 2 + 20, top + 9 * 18 + 4, 0x66101018);
        for (int i = 0; i < 9; i++) {
            ctx.renderItem(mc.player.getInventory().getItem(i), 4, top + 2 + i * 18);
        }
    }
}
