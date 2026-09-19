package net.mcsm.extras.client;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidTiers;

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

    private static final int SLOT = 20;
    private static final int SLOTS = 9;
    private static final float ICON_SCALE = 1.12F; // user feedback: smaller, closer-to-edge Story Mode rail

    // --- MCSM episode card state (client-only, no extra mixin needed) --------
    private static ClientLevel lastLevel;
    private static long cardStart = -1L;
    // BUILD #416 (D.8, phase 5): 6.5s of a full-screen title card on every
    // world join read as "a watermark sitting in the middle of the screen". The
    // card is now a real title card: in, hold, out, and the HUD comes back.
    private static final long CARD_MS = 3600L;

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

        // BUILD #416 (D.8, phase 6) -- THE SKY TERMINAL, drawn over everything
        // else this HUD does, because when it is up it is the only thing on
        // screen that matters.
        try {
            net.mcsm.extras.client.McsmMassgSky.paint(g, w, h);
        } catch (Throwable ignored) {
            // a glitch pass must never take a frame down
        }

        float storm = net.dabicco.witherstormmod.client.StormSkyDarken.factor();
        boolean active = storm > 0.04F;

        // --- Image 3 Silver Pixel Border Frame for Gameplay ---
        if (McsmExtrasConfig.uiBorderLines) {
            int borderCol = 0xFF8A8A9E; // Silver-gray border
            int innerCol = 0xFF2A2A38;
            // Border lines around screen
            g.fill(0, 0, w, 2, borderCol);
            g.fill(0, h - 2, w, h, borderCol);
            g.fill(0, 0, 2, h, borderCol);
            g.fill(w - 2, 0, w, h, borderCol);

            // Inset lines
            g.fill(4, 4, w - 4, 5, innerCol);
            g.fill(4, h - 5, w - 4, h - 4, innerCol);
            g.fill(4, 4, 5, h - 4, innerCol);
            g.fill(w - 5, 4, w - 4, h - 4, innerCol);

            // Image 3 L-Shape Pixel Corner Accents
            g.fill(2, 2, 10, 4, borderCol);
            g.fill(2, 2, 4, 10, borderCol);
            g.fill(w - 10, 2, w - 2, 4, borderCol);
            g.fill(w - 4, 2, w - 2, 10, borderCol);
            g.fill(2, h - 4, 10, h - 2, borderCol);
            g.fill(2, h - 10, 4, h - 2, borderCol);
            g.fill(w - 10, h - 4, w - 2, h - 2, borderCol);
            g.fill(w - 4, h - 10, w - 2, h - 2, borderCol);
        }

        if (active) {
            int bar = Math.max(14, h / 12);
            g.fill(0, 0, w, bar, 0xFF000000);
            g.fill(0, h - bar, w, h, 0xFF000000);
        }

        // --- MCSM story HUD: vertical inventory rail + top ability callouts ---
        Matrix3x2fStack pose = g.pose();
        int selected = player.getInventory().getSelectedSlot();
        int railW = SLOT + 8;
        int railH = SLOTS * (SLOT + 3) + 5;
        int px = 20;
        int py = Math.max(16, Math.min(h - railH - 16, h / 2 - railH / 2));

        // Left episode/action rail, matching the reference's stacked slots.
        g.fill(px - 3, py - 4, px + railW + 3, py + railH + 4, 0x33F2F6FF);
        g.fill(px - 3, py - 4, px - 1, py + railH + 4, 0xFFFFFFFF);
        g.fill(px + railW + 1, py - 4, px + railW + 3, py + railH + 4, 0xFFBFC9D8);
        g.fill(px - 3, py - 4, px + railW + 3, py - 2, 0xFFFFFFFF);
        g.fill(px - 3, py + railH + 2, px + railW + 3, py + railH + 4, 0xFFCAD3E2);
        for (int i = 0; i < SLOTS; i++) {
            int sx = px + 4;
            int sy = py + 3 + i * (SLOT + 3);
            int bg = (i == selected) ? 0xBEE8EEF8 : 0x82DCE4F0;
            g.fill(sx, sy, sx + SLOT, sy + SLOT, bg);
            g.fill(sx, sy, sx + SLOT, sy + 1, 0xAAFFFFFF);
            g.fill(sx, sy, sx + 1, sy + SLOT, 0xAAFFFFFF);
            g.fill(sx + SLOT - 1, sy, sx + SLOT, sy + SLOT, 0x33F2F6FF);
            g.fill(sx, sy + SLOT - 1, sx + SLOT, sy + SLOT, 0x33F2F6FF);
            if (i == selected) {
                // Cream-white Story Mode selection frame.
                g.fill(sx - 2, sy - 2, sx + SLOT + 2, sy, 0xFFFFF4C9);
                g.fill(sx - 2, sy + SLOT, sx + SLOT + 2, sy + SLOT + 2, 0xFFFFF4C9);
                g.fill(sx - 2, sy, sx, sy + SLOT, 0xFFFFF4C9);
                g.fill(sx + SLOT, sy, sx + SLOT + 2, sy + SLOT, 0xFFFFF4C9);
                g.fill(sx, sy, sx + SLOT, sy + SLOT, 0x448FD8FF);
            }
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                pose.pushMatrix();
                pose.translate(sx + 1, sy + 1);
                pose.scale(ICON_SCALE);
                g.item(stack, 0, 0);
                g.itemDecorations(mc.font, stack, 0, 0);
                pose.popMatrix();
            }
        }

        ItemStack held = player.getInventory().getItem(selected);
        if (!held.isEmpty()) {
            String label = itemName(held);
            int lx = px + railW + 28;
            int ly = py + 3 + selected * (SLOT + 3) + 7;
            int lw = Math.min(150, 10 + mc.font.width(label));
            g.fill(lx - 7, ly - 5, lx + lw, ly + 12, 0x6607070B);
            g.fill(lx - 7, ly - 5, lx - 4, ly + 12, 0xFFE9EEF8);
            g.text(mc.font, "\u00a7f" + label, lx, ly, 0xFFFFFFFF, true);
        }

        paintEffectCallouts(mc, player, g, w);
        paintStoryActionMeter(mc, player, g, w, h, active);

        // --- tiny diagnostics chip; the big debug panel is intentionally gone
        // so gameplay looks like the screenshot instead of a mod console.
        if (active) {
            String chip = "DS " + McsmExtrasConfig.BUILD_VERSION;
            g.fill(w - 76, 4, w - 4, 17, 0x55040610);
            g.fill(w - 76, 4, w - 74, 17, 0xFF6A8FF7);
            g.text(mc.font, chip, w - 70, 7, 0xFF9FB4D8, false);
        }

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

        // --- mega-phase 6b: portal glow + the warp entry sequence -----------
        paintPortal(mc, player, g, w, h);
        paintWarp(mc, player, g, w, h);

        // --- BUILD #489 V3 Endless Possibilities – HUD overload sync + meditation ---
        try {
            paintVoidOverload(mc, player, g, w, h);
        } catch (Throwable ignored) {
            // overload sync must never crash HUD
        }

        // --- Build #375: the Story Mode Console is discoverable in-game -----
        // A quiet persistent chip (bottom-left) pointing at the 300-tab
        // framework + texture painter: the user asked for these options to
        // be visible, not buried.
        try {
            net.minecraft.client.gui.Font font = mc.font;
            if (font != null) {
                String chip = "⚙ Shift+A — Story Mode Console";
                int cw = font.width(chip) + 10;
                int cy = h - 16;
                g.fill(4, cy - 3, 4 + cw, cy + 11, 0x55060409);
                g.fill(4, cy - 3, 5, cy + 11, 0x883A2A5A);
                g.text(font, "§7⚙ §8Shift+A — Story Mode Console", 9, cy, 0xB89FB8E8, false);
            }
        } catch (Throwable ignored) {
            // cosmetic only
        }
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

    private static String itemName(ItemStack stack) {
        try {
            Object c = stack.getClass().getMethod("getHoverName").invoke(stack);
            Object txt = c.getClass().getMethod("getString").invoke(c);
            if (txt instanceof String s && !s.isBlank()) {
                return s;
            }
        } catch (Throwable ignored) {
        }
        return "Item";
    }

    private static void paintEffectCallouts(Minecraft mc, LocalPlayer player,
            GuiGraphicsExtractor g, int w) {
        java.util.List<String> labels = new java.util.ArrayList<>();
        try {
            Object effects = player.getClass().getMethod("getActiveEffects").invoke(player);
            if (effects instanceof Iterable<?> it) {
                for (Object effect : it) {
                    String name = effectName(effect);
                    int amp = effectAmplifier(effect) + 1;
                    if (!name.isBlank()) {
                        labels.add("+" + amp + " " + name);
                    }
                    if (labels.size() >= 3) {
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {
            // some loader/API builds hide the collection: no status text then
        }
        if (labels.isEmpty()) {
            return;
        }
        int y = 13;
        int x = w / 2;
        for (int i = 0; i < labels.size(); i++) {
            String text = "\u00a7l" + labels.get(i);
            int yy = y + i * 12;
            int col = i == 0 ? 0xFFFF5656 : (i == 1 ? 0xFFFF8B8B : 0xFF65DBFF);
            g.centeredText(mc.font, text, x, yy, col);
        }
    }

    private static String effectName(Object effect) {
        try {
            Object desc = effect.getClass().getMethod("getDescriptionId").invoke(effect);
            String raw = String.valueOf(desc);
            raw = raw.substring(raw.lastIndexOf('.') + 1).replace('_', ' ');
            if (raw.isBlank()) {
                return "Effect";
            }
            String[] parts = raw.split(" ");
            StringBuilder out = new StringBuilder();
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                if (out.length() > 0) {
                    out.append(' ');
                }
                out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
            return out.toString();
        } catch (Throwable ignored) {
            return "Effect";
        }
    }

    private static int effectAmplifier(Object effect) {
        try {
            Object v = effect.getClass().getMethod("getAmplifier").invoke(effect);
            if (v instanceof Number n) {
                return n.intValue();
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private static void paintStoryActionMeter(Minecraft mc, LocalPlayer player,
            GuiGraphicsExtractor g, int w, int h, boolean active) {
        // Gold quick-action rails in the lower right, like Story Mode's
        // contextual prompts. They breathe subtly when the storm is active.
        int bars = 4;
        int bw = 46;
        int bh = 6;
        int gap = 3;
        int total = bars * bw + (bars - 1) * gap;
        int x = Math.max(86, w - total - 64);
        int y = h - Math.max(42, h / 12);
        float breath = active ? (0.85F + 0.15F * (float) Math.sin(System.currentTimeMillis() * 0.006D)) : 0.75F;
        int goldA = (int) (255.0F * breath);
        for (int i = 0; i < bars; i++) {
            int bx = x + i * (bw + gap);
            g.fill(bx, y, bx + bw, y + bh, 0x66332A00);
            g.fill(bx, y, bx + bw, y + 1, 0xFFFFF3AA);
            g.fill(bx, y + bh - 1, bx + bw, y + bh, 0xFF9C7614);
            g.fill(bx + 2, y + 2, bx + bw - 2, y + bh - 1, (goldA << 24) | 0xFFE55D);
        }
        int hookX = w - 38;
        int hookY = h - 32;
        int c = active ? 0xFF20D7F2 : 0xFF11889A;
        g.fill(hookX - 8, hookY + 11, hookX + 9, hookY + 14, 0x44000000);
        g.fill(hookX - 11, hookY + 7, hookX - 8, hookY + 12, c);
        g.fill(hookX + 8, hookY + 7, hookX + 11, hookY + 12, c);
        g.fill(hookX - 6, hookY + 12, hookX + 7, hookY + 15, c);
        g.text(mc.font, "↻", hookX - 6, hookY - 1, c, true);
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
            // 26.2: the client reaches its integrated server through
            // getSingleplayerServer() (verified in ci/api/client.txt);
            // Minecraft.getServer() no longer exists.
            var srv = mc.getSingleplayerServer();
            if (srv != null) {
                srv.execute(() -> {
                    try {
                        net.minecraft.server.level.ServerPlayer sp =
                                srv.getPlayerList().getPlayer(id);
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
     * BUILD #489 / 7000.0.25-M – V3 Endless Possibilities – HUD Overload Sync
     * Tie to McsmMeshSynthesizer live mesh synthesis data. When generator calculates
     * highly chaotic, sectioned-out geometry phase, force concentric orange-gold vortex
     * rings to ripple violently to reflect distortion. Blinding Gaze Synch: lock emissive
     * purple gaze loops to vertex noise frequency, flash white artifact across HUD margins.
     * Also meditation per layer and corrupted code scrawl on borders.
     */
    private static void paintVoidOverload(Minecraft mc, LocalPlayer player,
            GuiGraphicsExtractor g, int w, int h) {
        try {
            if (mc.level == null) return;
            if (!mc.level.dimension().equals(McsmVoid.DIMENSION)) return;
            double y = player.getY();
            if (y > McsmVoidTiers.GEL_FLOOR) return;

            double fallDist = player.fallDistance;
            double time = (mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

            float glitchFactor = McsmGlitchGenerator.computeGlitchFactor(fallDist, y, time);
            glitchFactor = net.minecraft.util.Mth.clamp(glitchFactor * 1.8F, 0.0F, 1.0F);

            float rippleFactor = McsmMeshSynthesizer.getVortexRippleFactor(glitchFactor, McsmMeshSynthesizer.getRecentAssets().size(), time);
            float noiseFreq = McsmMeshSynthesizer.getNoiseFrequencyForHud(glitchFactor, time);

            // Meditation per layer
            String meditation = McsmInfiniteVoidLayers.getCurrentMeditation();
            int medLayer = McsmInfiniteVoidLayers.getCurrentMeditationLayer();
            if (meditation == null || meditation.isEmpty()) {
                meditation = McsmMeshSynthesizer.getMeditationForLayer(medLayer);
            }
            String breathing = McsmMeshSynthesizer.getBreathingPhase(time, medLayer);
            float[] medColor = McsmMeshSynthesizer.getMeditationColor(medLayer, time);

            int medR = (int)(medColor[0] * 255);
            int medG = (int)(medColor[1] * 255);
            int medB = (int)(medColor[2] * 255);
            int medCol = 0xFF000000 | (medR << 16) | (medG << 8) | medB;

            // Orange-gold vortex rings – ripple violently when chaotic
            if (glitchFactor > 0.3F) {
                int cx = w / 2;
                int cy = h / 2;
                int rings = 6 + (int)(glitchFactor * 6); // 6-12 rings
                for (int i = 0; i < rings; i++) {
                    float baseRadius = 40.0F + i * 35.0F;
                    float radius = baseRadius * rippleFactor + (float)Math.sin(time * 0.003 + i * 0.7) * 10.0F * glitchFactor;
                    int alpha = (int)((80 - i * 8) * (0.3 + glitchFactor * 0.8) * medColor[3]);
                    alpha = Math.max(0, Math.min(255, alpha));
                    // Orange-gold: hue 35-50
                    float hue = 0.08F + i * 0.02F + (float)Math.sin(time * 0.0005 + i * 0.3) * 0.02F;
                    // Approximate orange-gold RGB from hue
                    int r = 255;
                    int gg = (int)(140 + hue * 100 + Math.sin(time * 0.001 + i) * 30);
                    int b = (int)(10 + i * 5);
                    gg = Math.max(0, Math.min(255, gg));
                    b = Math.max(0, Math.min(255, b));
                    int ringCol = (alpha << 24) | (r << 16) | (gg << 8) | b;

                    // Draw ring as 4 lines approximating circle via fill with thickness
                    int thickness = (int)(2 + glitchFactor * 3 + Math.sin(time * 0.01 + i) * glitchFactor * 2);
                    // Top and bottom arcs – simplified as rectangles that ripple
                    // Instead of true circle, draw horizontal bars that ripple to simulate vortex
                    int x0 = (int)(cx - radius);
                    int x1 = (int)(cx + radius);
                    int y0 = (int)(cy - radius);
                    int y1 = (int)(cy - radius + thickness);
                    if (y0 >= 0 && y1 < h) {
                        g.fill(x0, y0, x1, y1, ringCol);
                    }
                    y0 = (int)(cy + radius - thickness);
                    y1 = (int)(cy + radius);
                    if (y0 >= 0 && y1 < h) {
                        g.fill(x0, y0, x1, y1, ringCol);
                    }
                    // Left/right
                    x0 = (int)(cx - radius);
                    x1 = (int)(cx - radius + thickness);
                    y0 = (int)(cy - radius);
                    y1 = (int)(cy + radius);
                    if (x0 >= 0 && x1 < w) {
                        g.fill(x0, y0, x1, y1, ringCol);
                    }
                    x0 = (int)(cx + radius - thickness);
                    x1 = (int)(cx + radius);
                    if (x0 >= 0 && x1 < w) {
                        g.fill(x0, y0, x1, y1, ringCol);
                    }
                }
            }

            // Corrupted code scrawl on borders – when glitch high, show code-like text
            if (glitchFactor > 0.4F) {
                String[] codeSnippets = new String[]{
                    "fBm(x*0.05,y*0.05,z*0.05,time,4)*warpStrength",
                    "displaceVertexAdvanced(x,y,z,time,fall,glitch,playerPos,idx,seed)",
                    "generateOrganicCave(hollow, twist, fragment separation)",
                    "infiniteLandscape: 20+" + medLayer + " layers, meditation per layer",
                    "assetGenerator: " + McsmMeshSynthesizer.getRecentAssets().size() + " assets instantly",
                    "photorealisticF1: super duper landscape shader technique",
                    "endlessCheers: unlimited infinite universe cheers",
                    "V3 ENDLESS POSSIBILITIES – NO 20-MESH LIMIT – INFINITE",
                    "voidRudderAccel: " + String.format("%.2f", fallDist * 0.02) + " ripple=" + String.format("%.2f", rippleFactor),
                    "noiseFreq: " + String.format("%.2f", noiseFreq) + " glitch=" + String.format("%.2f", glitchFactor),
                    "meditation[" + medLayer + "]: " + meditation.substring(0, Math.min(40, meditation.length())),
                    "breathing: " + breathing,
                    "meshSynth: " + McsmMeshSynthesizer.state().substring(0, Math.min(60, McsmMeshSynthesizer.state().length()))
                };

                int borderAlpha = (int)(120 * glitchFactor);
                int codeCol = (borderAlpha << 24) | 0xFF8C00; // orange-gold code
                if (glitchFactor > 0.7F) {
                    codeCol = (borderAlpha << 24) | 0xFFD700; // gold when violent
                }

                // Left border scrawl
                int yOff = 20;
                for (int i = 0; i < codeSnippets.length && yOff < h - 20; i++) {
                    if (Math.random() < 0.3 + glitchFactor * 0.5) { // random flicker
                        String snippet = codeSnippets[(int)((time * 0.001 + i) % codeSnippets.length)];
                        // Truncate for width
                        if (mc.font.width(snippet) > w - 40) {
                            snippet = snippet.substring(0, Math.min(snippet.length(), 50)) + "...";
                        }
                        g.text(mc.font, "§6" + snippet, 6, yOff, codeCol, false);
                        yOff += 10 + (int)(Math.sin(time * 0.01 + i) * glitchFactor * 3);
                    }
                }

                // Right border scrawl – mirrored
                yOff = 30;
                for (int i = codeSnippets.length - 1; i >= 0 && yOff < h - 20; i--) {
                    if (Math.random() < 0.3 + glitchFactor * 0.5) {
                        String snippet = codeSnippets[(int)((time * 0.001 + i * 1.3) % codeSnippets.length)];
                        if (mc.font.width(snippet) > 120) {
                            snippet = snippet.substring(0, Math.min(snippet.length(), 30)) + "...";
                        }
                        int x = w - 6 - mc.font.width(snippet);
                        g.text(mc.font, "§e" + snippet, x, yOff, codeCol, false);
                        yOff += 11;
                    }
                }

                // Top/bottom corrupted bars
                if (glitchFactor > 0.6F) {
                    int flash = (int)(Math.sin(time * 0.02 + glitchFactor * 5) * 0.5 + 0.5) * 60 * (int)glitchFactor;
                    int flashCol = (flash << 24) | 0xFFFFFF;
                    g.fill(0, 0, w, 3, flashCol);
                    g.fill(0, h - 3, w, h, flashCol);
                }
            }

            // Meditation display – center bottom, glowing psychedelic
            if (meditation != null && !meditation.isEmpty()) {
                int cx = w / 2;
                int cy = h - 60;
                // Background
                g.fill(cx - 160, cy - 8, cx + 160, cy + 32, 0x66000000);
                g.fill(cx - 160, cy - 8, cx - 158, cy + 32, medCol);
                g.fill(cx + 158, cy - 8, cx + 160, cy + 32, medCol);

                // Truncate meditation to fit
                String medDisplay = meditation;
                if (mc.font.width(medDisplay) > 300) {
                    medDisplay = medDisplay.substring(0, 45) + "...";
                }
                g.centeredText(mc.font, "§f§l[Meditation Layer " + medLayer + "]", cx, cy - 6, medCol);
                g.centeredText(mc.font, "§7" + medDisplay, cx, cy + 6, 0xFFFFFFFF);
                g.centeredText(mc.font, "§e" + breathing, cx, cy + 18, 0xFFFFD700);
            }

            // F1 dual reality indicator
            boolean isF1 = McsmMeshSynthesizer.isF1Mode();
            if (isF1) {
                String f1Text = "§6§l[F1 PHOTOREALISTIC REALITY] Infinite Generator Mesh – Super Duper Photorealistic Landscape";
                g.fill(0, 20, w, 36, 0xAA000000);
                g.centeredText(mc.font, f1Text, w / 2, 24, 0xFFFF8C00);
            } else {
                if (y <= -60.0) {
                    String mainText = "§d§l[MAIN REALITY] Endless Universe – Unlimited Infinite Cheers – " + (36 + (int)(glitchFactor * 20)) + " layers visible – Infinite Possibilities";
                    g.fill(0, 20, w, 36, 0x66000000);
                    g.centeredText(mc.font, mainText, w / 2, 24, 0xFF9D4EDD);
                }
            }

            // Void Rudder acceleration indicator – ties to shader LERP
            if (fallDist > 10.0) {
                float accel = (float)(fallDist * 0.02);
                int barW = (int)(accel * 50);
                barW = Math.max(0, Math.min(200, barW));
                g.fill(w - 210, h - 30, w - 10, h - 20, 0x44000000);
                g.fill(w - 210, h - 30, w - 210 + barW, h - 20, 0xFFFF8C00);
                g.text(mc.font, "§6Void Rudder Accel: " + String.format("%.1f", accel), w - 200, h - 28, 0xFFFFFFFF, false);
            }

        } catch (Throwable ignored) {}
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

        // BUILD #416 (D.8, phase 5) -- THE MID-SCREEN WORDMARK IS GONE.
        //
        // "Devouring Storm" written across the middle of the screen is the
        // watermark the user asked to have removed -- the name is already in the
        // logo, so a third stamp of it in the centre of the picture is just
        // something standing on top of the shot. The episode card keeps its two
        // real title lines and nothing else; the identity line lives in the
        // main-menu wordmark, where it belongs.

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
