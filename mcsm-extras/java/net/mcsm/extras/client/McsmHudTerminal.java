package net.mcsm.extras.client;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import net.mcsm.extras.McsmExtrasConfig;

/**
 * Devouring Storms: the Story Mode HUD.
 *
 * The hotbar itself moves to the TOP-LEFT and gets bigger, MCSM-style:
 * nine large slots with real item icons, counts and durability
 * decorations, and a bright selection frame. The vanilla bottom hotbar
 * is cancelled at engine level (McsmHotbarHideMixin on Hud#extractItemHotbar,
 * verified by the CI GUI-surface probe).
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

    // --- MCSM episode card state (client-only, no extra mixin needed) --------
    private static ClientLevel lastLevel;
    private static long cardStart = -1L;
    private static final long CARD_MS = 6500L;

    // --- mega-phase 4: N-flash pulse state (phase 7+) ------------------------
    private static long pulseNextAt = 0L;
    private static long pulseStartAt = 0L;
    private static long lastThumpAt = 0L;
    private static int thumpStep = 0;

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

        // --- world-join detection: fire the MCSM episode title card ----------
        if (mc.level != lastLevel) {
            lastLevel = mc.level;
            cardStart = System.currentTimeMillis();
        }
        if (cardStart > 0L) {
            long t = System.currentTimeMillis() - cardStart;
            if (t > CARD_MS) {
                cardStart = -1L;
            } else {
                paintEpisodeCard(g, w, h, t);
                return; // the card owns the screen while it plays
            }
        }

        float storm = net.dabicco.witherstormmod.client.StormSkyDarken.factor();
        boolean active = storm > 0.04F;

        if (active) {
            int bar = Math.max(14, h / 12);
            g.fill(0, 0, w, bar, 0xFF000000);
            g.fill(0, h - bar, w, h, 0xFF000000);
        }

        // --- MCSM hotbar: top-left, big, real icons -------------------------
        int px = 4;
        int py = 4;
        int pw = SLOTS * SLOT + 8;
        g.fill(px, py, px + pw, py + SLOT + 8, 0xB00D0D16);
        g.fill(px, py, px + pw, py + 1, 0xFF6A8FF7);
        g.fill(px, py + SLOT + 7, px + pw, py + SLOT + 8, 0xFF263165);

        int selected = player.getInventory().getSelectedSlot();
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
        // --- mega-phase 4: the N-flash pulse ---------------------------------
        // Phase 7+: every 30-50 s a 20 s purple gradient slowly appears and
        // disappears OVER the storm, with a heartbeat double-thump, exactly
        // like the story's N-flash beats.
        long nowMs = System.currentTimeMillis();
        boolean late = false;
        try {
            for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d
                    : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
                if (d.phase >= 6.5F) {
                    late = true;
                    break;
                }
            }
        } catch (Throwable ignored) {
            // older base jar without the manager: no pulses, never a crash
        }
        if (late) {
            if (pulseStartAt > 0L) {
                long pt = nowMs - pulseStartAt;
                if (pt > 20000L) {
                    pulseStartAt = 0L;
                    pulseNextAt = nowMs + 30000L + (long) (Math.random() * 20000L);
                } else {
                    float env = (float) Math.sin(Math.PI * (pt / 20000.0D));
                    int aTop = (int) (env * 110.0F);
                    int aBot = aTop / 2;
                    g.fillGradient(0, 0, w, h,
                            (aTop << 24) | 0x6A2AC8, (aBot << 24) | 0x2A0A4A);
                    if (nowMs - lastThumpAt > (thumpStep == 0 ? 1500L : 260L)) {
                        thumpStep = (thumpStep + 1) % 2;
                        lastThumpAt = nowMs;
                        player.playSound(net.minecraft.sounds.SoundEvents.ANVIL_LAND, 0.42F, 0.42F);
                    }
                }
            } else if (nowMs >= pulseNextAt) {
                pulseStartAt = nowMs;
            }
        }

        int ty = py + SLOT + 12;
        int tw = 128;
        int th = lines.length * 11 + 8;
        g.fill(px, ty, px + tw, ty + th, 0x88101018);
        g.fill(px, ty, px + 2, ty + th, 0xFF6A8FF7);
        for (int i = 0; i < lines.length; i++) {
            g.text(mc.font, lines[i], px + 6, ty + 4 + i * 11,
                    i == 0 ? 0xFFBFD3FF : 0xFF9FB4D8, false);
        }

        // --- mega-phase 6b: portal glow + the warp entry sequence -----------
        paintPortal(mc, player, g, w, h);
        paintWarp(mc, player, g, w, h);
    }

    // --- mega-phase 6b state -------------------------------------------------
    private static float portalNear = 0.0F;
    private static float portalYaw = 0.0F;
    private static boolean warpSoundDone = false;
    private static boolean warpReleased = false;

    /**
     * Per-portal coloured bottom glow + coloured dust breathing at the
     * storm mouth (user order: "glowing lights at the bottom casting
     * different-coloured light per portal"). Teal while the plates are
     * still closing, magenta once the mouth is open, gold inside the
     * bowels hallway where the return mouth sits.
     */
    private static void paintPortal(Minecraft mc, LocalPlayer player,
            GuiGraphicsExtractor g, int w, int h) {
        portalNear = 0.0F;
        try {
            boolean inBowels = false;
            try {
                Class<?> bg = Class.forName("net.dabicco.witherstormmod.BowelsGravity");
                Object key = bg.getField("BOWELS").get(null);
                inBowels = player.level().dimension().equals(key);
            } catch (Throwable ignored) {
                // no bowels registry on this jar: overworld behaviour only
            }
            if (inBowels) {
                bottomGlow(g, w, h, 0xFFE666, 0.55F + 0.10F * (float) Math.sin(System.currentTimeMillis() * 0.004D));
                return;
            }
            net.dabicco.witherstormmod.entity.WitherStormEntity storm = null;
            double best = Double.MAX_VALUE;
            for (net.dabicco.witherstormmod.entity.WitherStormEntity e
                    : player.level().getEntitiesOfClass(
                            net.dabicco.witherstormmod.entity.WitherStormEntity.class,
                            player.getBoundingBox().inflate(48.0D))) {
                double d = e.distanceToSqr(player);
                if (d < best) {
                    best = d;
                    storm = e;
                }
            }
            if (storm == null) {
                return;
            }
            net.minecraft.world.phys.Vec3 mouth = mouthOf(storm);
            double d = mouth.distanceTo(player.position());
            if (d > 30.0D) {
                return;
            }
            float near = (float) (1.0D - d / 30.0D);
            portalNear = near;
            double dx = mouth.x - player.position().x;
            double dz = mouth.z - player.position().z;
            portalYaw = (float) Math.toDegrees(Math.atan2(-dx, -dz));
            boolean open = phaseOf(storm) >= 6.9D;
            int col = open ? 0xCC33FF : 0x33FFE6;
            bottomGlow(g, w, h, col, near);
            // coloured dust breathing up from the mouth bottom
            if (System.currentTimeMillis() % 100L < 50L) {
                net.minecraft.core.particles.DustParticleOptions dust =
                        new net.minecraft.core.particles.DustParticleOptions(col | 0xFF000000, 0.8F);
                for (int i = 0; i < 2; i++) {
                    player.level().addParticle(dust,
                            mouth.x + (Math.random() - 0.5) * 4.0D,
                            mouth.y - 2.0D + Math.random() * 1.2D,
                            mouth.z + (Math.random() - 0.5) * 4.0D,
                            0.0D, 0.06D, 0.0D);
                }
            }
        } catch (Throwable ignored) {
            // spectacle only - never crash the HUD
        }
    }

    private static void bottomGlow(GuiGraphicsExtractor g, int w, int h, int col, float near) {
        float s = Math.min(Math.max(near, 0.0F), 1.0F);
        int a1 = (int) (70.0F * s);
        int a2 = (int) (110.0F * s);
        int a3 = (int) (170.0F * s);
        g.fillGradient(0, h - (int) (h * 0.42 * s), w, h, (0 << 24) | col, (a1 << 24) | col);
        g.fillGradient(0, h - (int) (h * 0.24 * s), w, h, (0 << 24) | col, (a2 << 24) | col);
        g.fillGradient(0, h - (int) (h * 0.10 * s), w, h, (a2 << 24) | col, (a3 << 24) | col);
    }

    private static net.minecraft.world.phys.Vec3 mouthOf(
            net.dabicco.witherstormmod.entity.WitherStormEntity storm) {
        try {
            Class<?> bp = Class.forName("net.dabicco.witherstormmod.BowelsPortal");
            Object bb = bp.getMethod("mouth", net.minecraft.world.entity.Entity.class)
                    .invoke(null, storm);
            if (bb instanceof net.minecraft.world.phys.AABB b) {
                return b.getCenter();
            }
        } catch (Throwable ignored) {
            // fall through to the hardcoded mouth offset
        }
        // fromModel(26, -192, -32) at BODY_SCALE, turned by the storm yaw
        double lx = 1.7890D;
        double ly = 8.2580D;
        double lz = 2.2018D;
        double yaw = Math.toRadians(storm.getYRot());
        double c = Math.cos(yaw);
        double sn = Math.sin(yaw);
        return storm.position().add(new net.minecraft.world.phys.Vec3(
                lx * c - lz * sn, ly, lx * sn + lz * c));
    }

    private static double phaseOf(net.dabicco.witherstormmod.entity.WitherStormEntity storm) {
        try {
            Object o = storm.getClass().getMethod("getPhase").invoke(storm);
            if (o instanceof Number n) {
                return n.doubleValue();
            }
        } catch (Throwable ignored) {
            // unreadable phase: assume the mouth can open
        }
        return 7.0D;
    }

    /**
     * The warp entry sequence: converging letterbox, violet pull grade,
     * camera yaw dragged toward the mouth, white flash - then the server
     * thread runs the ORIGINAL bowels teleport once. No loading screen.
     */
    private static void paintWarp(Minecraft mc, LocalPlayer player,
            GuiGraphicsExtractor g, int w, int h) {
        java.util.UUID id = player.getUUID();
        if (!net.mcsm.extras.McsmWarp.warping(id)) {
            warpSoundDone = false;
            warpReleased = false;
            return;
        }
        float p = net.mcsm.extras.McsmWarp.progress(id);
        if (!warpSoundDone) {
            warpSoundDone = true;
            player.playSound(net.minecraft.sounds.SoundEvents.PORTAL_TRAVEL, 1.0F, 0.8F);
        }
        int e = (int) (h * 0.5D * p * p);
        g.fill(0, 0, w, e, 0xFF000000);
        g.fill(0, h - e, w, h, 0xFF000000);
        int a = (int) (130.0F * p);
        g.fillGradient(0, 0, w, h, (a << 24) | 0x2A0A4A, (a << 24) | 0x6A2AC8);
        if (portalNear > 0.02F) {
            float cur = player.getYRot();
            float diff = portalYaw - cur;
            while (diff > 180.0F) {
                diff -= 360.0F;
            }
            while (diff < -180.0F) {
                diff += 360.0F;
            }
            player.setYRot(cur + diff * 0.10F * p);
        }
        if (p > 0.85F) {
            int fa = (int) (255.0F * (p - 0.85F) / 0.15F);
            g.fill(0, 0, w, h, (fa << 24) | 0xFFFFFF);
        }
        if (p >= 1.0F && !warpReleased) {
            warpReleased = true;
            if (mc.getServer() != null) {
                mc.getServer().execute(() -> {
                    try {
                        net.minecraft.server.level.ServerPlayer sp =
                                mc.getServer().getPlayerList().getPlayer(id);
                        if (sp != null) {
                            net.mcsm.extras.McsmWarp.release(sp);
                        }
                    } catch (Throwable ignored) {
                        // never crash on the way through the portal
                    }
                });
            } else {
                net.mcsm.extras.McsmWarp.cancel(id);
            }
        }
    }

    /**
     * The MCSM episode title card: on every world join the screen closes to
     * a cinematic dark plate with heavy letterbox bars, "Episode One /
     * A NEW ORDER" fades in at center in big scaled type, the saga line
     * sits beneath, and a progress bar runs "Entering the story..." before
     * the whole card fades out into gameplay. Client-side only, drawn with
     * the verified extractor surface (fill, fillGradient, centeredText,
     * pose) - no new mixin, no unverified API.
     */
    private static void paintEpisodeCard(GuiGraphicsExtractor g, int w, int h, long t) {
        // fade in over 0.8s, out over the last 1.2s
        float fade = Math.min(1.0F, t / 800.0F)
                * Math.min(1.0F, (CARD_MS - t) / 1200.0F);
        if (fade <= 0.0F) {
            return;
        }
        int a = (int) (fade * 240.0F) << 24;

        // dark plate + heavy letterbox bars
        g.fill(0, 0, w, h, a | 0x050308);
        int bar = Math.max(24, h / 5);
        int barA = (int) (fade * 255.0F) << 24;
        g.fill(0, 0, w, bar, barA | 0x000000);
        g.fill(0, h - bar, w, h, barA | 0x000000);
        g.fillGradient(0, bar, w, bar + 2, a | 0x3F255A, a | 0x6A8FF7);
        g.fillGradient(0, h - bar - 2, w, h - bar, a | 0x6A8FF7, a | 0x3F255A);

        Minecraft mc = Minecraft.getInstance();
        Matrix3x2fStack pose = g.pose();
        int cx = w / 2;
        int cy = h / 2;

        // "Episode One" - big scaled type
        float s1 = Math.min(3.0F, w / 130.0F);
        pose.pushMatrix();
        pose.translate(cx, cy - 46);
        pose.scale(s1);
        g.centeredText(mc.font, "\u00a7f\u00a7lEpisode One", 0, 0, a | 0xEAF2FF);
        pose.popMatrix();

        // "A NEW ORDER"
        float s2 = Math.min(1.9F, w / 210.0F);
        pose.pushMatrix();
        pose.translate(cx, cy - 4);
        pose.scale(s2);
        g.centeredText(mc.font, "\u00a79\u00a7lA  N E W  O R D E R", 0, 0, a | 0xBFD3FF);
        pose.popMatrix();

        // saga line
        g.centeredText(mc.font,
                "\u00a78DEVOURING STORMS \u00a77\u00b7 \u00a78THE POINT OF NO RETURN",
                cx, cy + 34, a | 0x8FA3C8);

        // progress bar + entering text
        float prog = Math.min(1.0F, t / (float) CARD_MS);
        int bw = Math.min(260, w - 80);
        int bx = cx - bw / 2;
        int by = h - bar + 14;
        g.fill(bx, by, bx + bw, by + 3, (int) (fade * 90.0F) << 24 | 0x223355);
        g.fill(bx, by, bx + (int) (bw * prog), by + 3, a | 0x6A8FF7);
        g.centeredText(mc.font, "\u00a77Entering the story\u2026", cx, by + 10, a | 0x9FB4D8);
    }
}
