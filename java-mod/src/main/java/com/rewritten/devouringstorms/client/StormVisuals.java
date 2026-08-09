package com.rewritten.devouringstorms.client;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.registry.ModStatusEffects;
import com.rewritten.devouringstorms.storm.MassgPhase;
import com.rewritten.devouringstorms.world.ModDimensions;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * All the screen-space horror:
 *  - storm corruption overlay (vignette, pulse, glitch strips) while a MASSG is loose,
 *  - the Decayed Reality's TWO RIFTS in the sky ("light flows, but not enough"),
 *  - the Watcher's mark dimming the edges of your vision.
 *
 * Renders through the vanilla HUD layer stack of the modern renderer.
 */
public final class StormVisuals {

    private static final ResourceLocation RIFT_TEXTURE = DevouringStorms.id("textures/environment/rift.png");

    /**
     * The fog ladder — the storm paints the air in its own progression:
     * SIGNAL: blue-teal sea-fog · HUNGER/DEVOURER: bruised dark blue · SUNDERER: deep violet ·
     * BOWELS: dark purple with a pink undertone · GENESIS: near-black with a red rim · HUSK: ash.
     */
    private static final java.util.Map<MassgPhase, int[]> PHASE_FOG = java.util.Map.of(
        MassgPhase.SIGNAL, new int[] { 20, 154, 146 },
        MassgPhase.HUNGER, new int[] { 22, 36, 74 },
        MassgPhase.DEVOURER, new int[] { 22, 36, 74 },
        MassgPhase.SUNDERER, new int[] { 51, 19, 92 },
        MassgPhase.BOWELS, new int[] { 85, 16, 75 },
        MassgPhase.GENESIS, new int[] { 90, 15, 15 },
        MassgPhase.HUSK, new int[] { 42, 32, 48 }
    );

    /**
     * CHANNEL TITLE CARDS — the channel's sign-off, printed over the world every time the
     * storm escalates. White, letterspaced, trembling; the REWRITTEN way.
     */
    private static final java.util.Map<Integer, String> PHASE_TITLES = java.util.Map.of(
        1, "PHASE 1 — THE SIGNAL",
        2, "PHASE 2 — THE HUNGER",
        3, "PHASE 3 — THE DEVOURER",
        4, "PHASE 4 — THE SUNDERER",
        5, "PHASE FIVE AND A HALF — THE BOWELS",
        6, "PHASE 6 — GENESIS",
        7, "THE STORM FALLS. FINISH IT."
    );
    private static int lastSeenPhase = -2;
    private static long cardStartTick = -9999;

    private StormVisuals() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;
        if (!minecraft.level.isClientSide()) return;

        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        float ticks = minecraft.player.tickCount + deltaTracker.getGameTimeDeltaPartialTick(false);

        boolean inDecayed = minecraft.level.dimension() == ModDimensions.DECAYED_LEVEL_KEY;
        float storm = StormClientState.stormActive ? StormClientState.intensity : 0.0f;
        float gazed = minecraft.player.hasEffect(ModStatusEffects.GAZED) ? 1.0f : 0.0f;

        float corruption = Mth.clamp(storm * 0.75f + (inDecayed ? 0.35f : 0.0f) + gazed * 0.5f, 0.0f, 1.0f);
        corruption *= com.rewritten.devouringstorms.util.DevouringConfig.getFloat("overlay_intensity", 1.0f);

        // ---- title-card bookkeeping runs even when the overlays are muted ----
        if (com.rewritten.devouringstorms.util.DevouringConfig.getBool("storm_title_cards", true)) {
            tickTitleCard(graphics, minecraft, w, h, ticks);
        }

        // ---- THE VISION: the Creator sees the world as dirty tape ----
        float vhs = vhsStrength(minecraft, ticks);
        if (vhs > 0.01f) renderVhs(graphics, minecraft, w, h, ticks, vhs);

        if (corruption <= 0.01f && !inDecayed && vhs <= 0.01f) return;

        // ---- base dim: the world's edges rotting away ----
        int dim = (int) (corruption * 90);
        if (dim > 0) {
            graphics.fillGradient(0, 0, w, h, (dim << 24) | 0x0A0012, 0x00000000);
            graphics.fillGradient(0, 0, 6, h, (dim << 24) | 0x1A0028, 0x00000000);
            graphics.fillGradient(w - 6, 0, w, h, 0x00000000, (dim << 24) | 0x1A0028);
        }

        // ---- critical pulse: the world flinches ----
        if (StormClientState.critical && storm > 0.0f) {
            float pulse = 0.5f + 0.5f * Mth.sin(ticks * 0.35f);
            int alpha = (int) (pulse * storm * 46);
            graphics.fill(0, 0, w, h, (alpha << 24) | 0x3C0010);
        }

        // ---- glitch strips: reality tearing in horizontal bands ----
        float glitchChance = corruption * 0.16f + (StormClientState.critical ? 0.18f : 0.0f) + gazed * 0.10f;
        if (minecraft.level.random.nextFloat() < glitchChance) {
            int stripY = minecraft.level.random.nextInt(h);
            int stripH = 2 + minecraft.level.random.nextInt(8);
            int alpha = 30 + minecraft.level.random.nextInt(70);
            int color = minecraft.level.random.nextBoolean() ? 0xFF00FF : 0x00FFFF;
            graphics.fill(0, stripY, w, stripY + stripH, (alpha << 24) | color);
            // a second strip, misaligned on x — the tear isn't clean
            int xOff = minecraft.level.random.nextInt(40) - 20;
            graphics.fill(xOff, stripY + stripH, w + xOff, stripY + stripH + 1, (alpha << 24) | 0xFFFFFF);
        }

        // ---- the phase fog ladder: the air itself changes colour with the storm ----
        boolean fogOn = com.rewritten.devouringstorms.util.DevouringConfig.getBool("fog_ladder", true);
        if (storm > 0.0f && fogOn) {
            MassgPhase phase = StormClientState.currentPhase();
            if (phase != null) {
                int[] body = PHASE_FOG.getOrDefault(phase, PHASE_FOG.get(MassgPhase.SIGNAL));
                int a = (int) (storm * 34);
                int rgb = body[0] << 16 | body[1] << 8 | body[2];
                // horizon-weighted wash: thicker at the top of the screen, pooling at the edges
                graphics.fillGradient(0, 0, w, h / 2, (a << 24) | rgb, 0x00000000);
                graphics.fillGradient(0, 0, 20, h, (a << 24) | rgb, 0x00000000);
                graphics.fillGradient(w - 20, 0, w, h, 0x00000000, (a << 24) | rgb);
                if (phase == MassgPhase.BOWELS) {
                    // and the pink undertone that comes with the split
                    int pink = (int) (storm * (26.0 + 18.0f * Mth.sin(ticks * 0.2f)));
                    graphics.fillGradient(0, h - 14, w, h, (pink << 24) | 0xFF3FA8, 0x00000000);
                }
                if (phase == MassgPhase.HUSK) {
                    // the fell mood: grey ash where the sky used to be
                    graphics.fillGradient(0, 0, w, h / 3, ((int) (storm * 26) << 24) | 0x2A2030, 0x00000000);
                }
            }
        }

        // ---- the Watcher's paranoia: blink-masking + heartbeat vignette ----
        if (gazed > 0.0f
            && com.rewritten.devouringstorms.util.DevouringConfig.getBool("watcher_paranoia", true)) {
            // the world pulses at the edge of vision like a bad heartbeat
            float beat = Mth.abs(Mth.sin(ticks * 0.14f));
            graphics.fillGradient(0, 0, w, h, ((int) (beat * 70) << 24) | 0x060010, 0x00000000);
            // and sometimes it just... isn't there for a frame. don'tlookdon'tlook
            if (minecraft.level.random.nextInt(90) == 0) {
                graphics.fill(0, 0, w, h, 0xE0000000);
            }
            // cyan-magenta afterimage of the gaze itself
            if (minecraft.level.random.nextFloat() < 0.03f + 0.05f * beat) {
                int y = h / 3 + minecraft.level.random.nextInt(h / 3);
                graphics.fill(0, y, w, y + 2, 0x9000FFFF);
                graphics.fill(6, y + 2, w + 6, y + 4, 0x90FF00FF);
            }
        }

        // ---- the two rifts in the Decayed Reality sky ----
        if (inDecayed && minecraft.player.getXRot() < -18.0f) {
            int size = Math.min(w, h) / 3;
            int yawShift = (int) (minecraft.player.getYRot() * 0.6f);
            int cx1 = w / 4 - yawShift % w;
            int cx2 = 3 * w / 4 - (yawShift * 2) % w;
            // The rift texture carries its own baked alpha; the RenderPipeline GUI blit
            // keeps this dependency-free. (If your mappings predate RenderPipeline blits,
            // swap in the legacy textureManager blit overload.)
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                RIFT_TEXTURE, cx1 - size / 2, h / 5, 0, 0, size, size, size, size);
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                RIFT_TEXTURE, cx2 - size / 4, h / 8, 0, 0, size / 2, size / 2, size / 2, size / 2);
        }
    }

    // ============================================================ THE VISION (VHS)
    // The crater, a playing VHS jukebox, the Monstrosity's broadcast, or looking at the
    // Creator — all of them turn your feed into tape: tracking noise, hard scanlines,
    // a chroma ghost, and that little counter you will never stop reading.
    private static final java.util.Map<String, Integer> vhsCache = new java.util.HashMap<>();
    private static String lastVhsSrc = "";

    private static float vhsStrength(Minecraft minecraft, float ticks) {
        if (!com.rewritten.devouringstorms.util.DevouringConfig.getBool("vhs_overlay", true)) return 0.0f;
        var player = minecraft.player;
        if (player == null || minecraft.level == null) return 0.0f;

        // 1) OVERTAKEN: the Monstrosity's broadcast owns your retinas
        boolean overtaken = player.hasEffect(com.rewritten.devouringstorms.registry.ModStatusEffects.OVERTAKEN);

        // 2) watching the Creator: the feed gives you the channel before it gives you the hand
        boolean nearCreator = !minecraft.level.getEntitiesOfClass(
            com.rewritten.devouringstorms.entity.CreatorEntity.class,
            player.getBoundingBox().inflate(52.0)).isEmpty();

        // 3) a playing VHS jukebox or the crater heart itself, scanned cheaply
        String src = "none";
        if (minecraft.player.tickCount % 6 == 0) {
            src = scanBlocks(minecraft, player);
            lastVhsSrc = src;
        } else {
            src = lastVhsSrc;
        }

        float strength = Math.max(overtaken ? 1.0f : 0.0f,
            Math.max(nearCreator ? 0.85f : 0.0f,
                "jukebox".equals(src) ? 0.75f : ("crater".equals(src) ? 0.8f : 0.0f)));
        return strength;
    }

    private static String scanBlocks(Minecraft minecraft, net.minecraft.world.entity.player.Player player) {
        int radius = 24;
        int jr = 10;
        var pos = player.blockPosition();
        boolean crater = false;
        for (int dx = -radius; dx <= radius && !crater; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                var s = minecraft.level.getBlockState(pos.offset(dx, 1, dz));
                if (s.is(com.rewritten.devouringstorms.registry.ModBlocks.CORRUPTED_COMMAND_BLOCK)) { crater = true; break; }
            }
        }
        boolean juke = false;
        outer:
        for (int dx = -jr; dx <= jr; dx++) {
            for (int dz = -jr; dz <= jr; dz++) {
                var s = minecraft.level.getBlockState(pos.offset(dx, 1, dz));
                if (s.is(com.rewritten.devouringstorms.registry.ModBlocks.VHS_JUKEBOX)
                    && s.getValue(com.rewritten.devouringstorms.block.VhsJukeboxBlock.PLAYING)) { juke = true; break outer; }
            }
        }
        return juke ? "jukebox" : (crater ? "crater" : "none");
    }

    private static void renderVhs(GuiGraphics graphics, Minecraft minecraft, int w, int h, float ticks, float strength) {
        var random = minecraft.level.random;

        // ---- letterbox: the feed gets cropped to 4:3 of the soul ----
        int bar = (int) (h * 0.09f * strength);
        graphics.fill(0, 0, w, bar, 0xFF000000);
        graphics.fill(0, h - bar, w, h, 0xFF000000);

        // ---- tracking noise: two tearing bands crawling upward ----
        float bandY = (ticks * 3.4f) % (h * 1.6f) - h * 0.3f;
        for (int i = 0; i < 2; i++) {
            int y = (int) (bandY + i * 48);
            if (y < 0 || y > h) continue;
            int a = (int) (55 * strength) & 0xFF;
            graphics.fill(0, y, w, y + 3, (a << 24) | 0xFFFFFF);
            int xOff = random.nextInt(26) - 13;
            graphics.fill(xOff, y + 3, w + xOff, y + 4, (a << 24) | 0xFFFFFF);
        }

        // ---- hard scanlines: the tape insists upon itself ----
        int scanAlpha = (int) (26 * strength) & 0xFF;
        for (int y = 0; y < h; y += 3) {
            graphics.fill(0, y, w, y + 1, (scanAlpha << 24) | 0x000000);
        }

        // ---- chroma ghost: red pushed left, cyan pushed right, alphanumeric justice ----
        int ca = (int) (120 * strength) & 0xFF;
        graphics.fill(0, bar, 4, h - bar, (ca << 24) | 0xFF2040);
        graphics.fill(w - 4, bar, w, h - bar, (ca << 24) | 0x20FFFF);
        if (random.nextInt(24) == 0) {
            int y = random.nextInt(h - bar * 2) + bar;
            graphics.fill(6, y, w - 8, y + 1, ((ca / 2) << 24) | 0xFF20FF);
        }

        // ---- the counter. PLAY. you are always playing ----
        long totalSec = minecraft.level.getGameTime() / 20;
        String stamp = String.format("PLAY \u25B6  %02d:%02d:%02d",
            (totalSec / 3600) % 24, (totalSec / 60) % 60, totalSec % 60);
        int sw = minecraft.font.width(stamp);
        graphics.fill(14, 14 + bar, 14 + sw + 8, 30 + bar, 0xB0000000);
        graphics.drawString(minecraft.font, stamp, 18, 18 + bar,
            ((int) (230 * strength) << 24) | 0xFFFFFF, true);

        String tag = switch (lastVhsSrc) {
            case "crater" -> "THE CRATER — EVENT LOG 0";
            case "jukebox" -> "TAPE: REWRITTEN";
            default -> "FEED: CREATOR";
        };
        int tw = minecraft.font.width(tag);
        graphics.fill(w - tw - 22, h - bar - 26, w - 12, h - bar - 12, 0xB0000000);
        graphics.drawString(minecraft.font, tag, w - tw - 18, h - bar - 22,
            ((int) (200 * strength) << 24) | 0xCFE0FF, false);

        // ---- tracking sparkles: the dust the Creator left on the lens ----
        for (int i = 0; i < (int) (14 * strength); i++) {
            int px = random.nextInt(w);
            int py = random.nextInt(h - bar * 2) + bar;
            graphics.fill(px, py, px + 2, py + 1, 0x66FFFFFF);
        }
    }

    /**
     * Watches the synced storm phase; when it changes, the world goes quiet for a card,
     * like the channel cutting to black before the episode starts. Dark band, letterspaced
     * title, and — for the two different kinds of ending — a white sub-line.
     */
    private static void tickTitleCard(GuiGraphics graphics, Minecraft minecraft, int w, int h, float ticks) {
        int seen = StormClientState.stormActive ? StormClientState.phase : -1;
        if (seen != lastSeenPhase) {
            if (seen >= 0 && PHASE_TITLES.containsKey(seen)) {
                cardStartTick = (long) Math.floor(ticks);
            }
            lastSeenPhase = seen;
        }
        long active = minecraft.level.getGameTime() - cardStartTick;
        if (active < 0 || active > 110) return;

        String title = PHASE_TITLES.getOrDefault(seen, "");
        if (title.isEmpty()) return;

        // fade in 15t · hold · fade out 20t
        float fade = active < 15 ? active / 15.0f
            : active > 90 ? 1.0f - (active - 90) / 20.0f
            : 1.0f;
        int a = (int) (fade * 210) & 0xFF;

        graphics.fill(0, h / 3 - 18, w, h / 3 + 26, (int) (fade * 160) << 24 | 0x05000A);
        // one jagged glitch line across the band — the episode markers the channel uses
        if (active == 16 || minecraft.level.random.nextInt(14) == 0) {
            int gy = h / 3 + 1 + minecraft.level.random.nextInt(12);
            graphics.fill(0, gy, w, gy + 1, 0x8800FFFF);
            graphics.fill(4, gy + 1, w + 4, gy + 2, 0x88FF00FF);
        }

        String spaced = title.replace("", " ").trim();
        float scale = seen == 5 ? 2.0f : 2.4f;
        int tw = minecraft.font.width(spaced);
        var pose = graphics.pose();
        try {
            pose.pushMatrix();
            // small tremble — the text is on camera too
            float jx = active < 30 ? minecraft.level.random.nextFloat() * 1.6f - 0.8f : 0.0f;
            pose.translate(w / 2.0f + jx, h / 3.0f);
            pose.scale(scale, scale);
            int col = (a << 24) | 0xF5F0FF;
            graphics.drawString(minecraft.font, spaced, (int) (-tw / 2.0f), 0, col, true);
        } finally {
            pose.popMatrix();
        }

        String sub = switch (seen) {
            case 5 -> "it is open. it is stomach. it will remember you.";
            case 6 -> "THE FORMIDIBOMB ONLY MADE IT WORSE";
            case 7 -> "the Watcher left you the Storm Killer. go up. go inside.";
            default -> "REWRITTEN // DEVOURING STORMS";
        };
        int sa = (int) (fade * 180) & 0xFF;
        int sw = minecraft.font.width(sub);
        graphics.drawString(minecraft.font, sub, w / 2 - sw / 2, h / 3 + 14, (sa << 24) | 0xBFA8D8, false);
    }
}
