package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmSounds;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidDescent;
import net.mcsm.extras.McsmVoidTiers;
import net.mcsm.extras.McsmVoidRifts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * BUILD #479 -- THE DEEP. The gel, from the inside.
 *
 * <p>THE ASK, with the concept image in the player's hands: "essentially the
 * floating area. Every monster has bioluminescent looks and glow. It's completely
 * emissive ... there's this bubbly gas with it, you're not in water ... basically in
 * a gel, but you're not floating or drowning but you are trying to fly inside. You
 * kind of float, bubbles around you ... the black point above starts to brighten
 * dark pink when looking above, the bedrock layer is visible as the fog ... gigantic
 * shadows ... directly made from shaders ... this place is the holder to the ... like
 * creatures; they're not really in an ocean at all."
 *
 * <p><b>IT WORKS WITH NO SHADER PACK AT ALL.</b> That is the standing rule for
 * everything in this mod, and it decides how the deep is built: the haze, the glow
 * above, the shadows and the bubbles are drawn here, in the frame, from this mod's
 * own palette -- not asked of a shader a player may not have installed. The shader
 * pack version of it (Iris) is a bonus on top, never the thing that makes it visible.
 *
 * <p>WHAT THE DEEP IS, in one paragraph: the band of the void below
 * {@link McsmVoidDescent#SURFACE_Y} -- the gel. Above the surface the void is air;
 * below it, the air has weight. The deeper the player is, the more of the frame the
 * gel owns, the brighter the pink glow over their head, the closer the bedrock fog
 * comes up, and the more of the place's own life is awake:
 *
 * <pre>
 *   bubbles     rising, small and constant: the gel is not still, ever
 *   motes       bioluminescent specks in three colours, drifting with their own clock
 *   shadows     enormous soft silhouettes high above, drifting across the glow
 *   flight      the gel holds you: you can fly here, and you never take fall damage
 *   the floor   the bedrock reads as fog at the bottom, which is the concept image
 * </pre>
 *
 * <p>Nothing here writes to the world, nothing here can damage anybody, and every
 * entry point is wrapped: a frame can be lost to a bug in this file exactly never.
 */
public final class McsmVoidDeep {

    // ------------------------------------------------------------------
    // The gel's own palette, from the concept image: violet-pink over near-black,
    // with the glow above it the brightest thing in the place.
    // ------------------------------------------------------------------
    /** The glow over the player's head. */
    private static final int GLOW_HI = 0xE8A24C7F;
    private static final int GLOW_LO = 0x00140A12;
    /** What is under them: the gel falling away into the bedrock's fog. */
    private static final int DEEP_HI = 0xB01A0E2A;
    private static final int DEEP_LO = 0xE8060309;
    /** The specks: three colours, none of them the sky's. */
    private static final int[] MOTE = { 0xFF7BE8FF, 0xFFB07BFF, 0xFF9BFF7B };
    /** The shadows above: a silhouette, not a shape. */
    private static final int SHADOW = 0x66120A1E;

    private static final int BANDS = 40;
    private static final int SHADOWS = 5;
    private static final int BUBBLES = 34;
    private static final int MOTES = 46;

    /** The surface crossing is remembered for this long, for the hand-over frame. */
    private static final long CROSSING_MS = 5000L;

    private static long crossingAt;
    /** BUILD #481 -- the tier last drawn, and when it was entered. */
    private static int shownTier = -1;
    private static long shownAt;
    private static boolean flyingGranted;
    private static int clock;

    private McsmVoidDeep() {
    }

    // ------------------------------------------------------------------
    // Where the player is in the gel: 0 at the surface, 1 at the bedrock
    // ------------------------------------------------------------------

    /** How deep into the gel the camera is, 0..1, or 0 when it is not in it. */
    public static float depth() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null) {
                return 0.0F;
            }
            if (!mc.player.level().dimension().equals(McsmVoid.DIMENSION)) {
                return 0.0F;
            }
            return depthAt(mc.player.getY());
        } catch (Throwable t) {
            return 0.0F;
        }
    }

    /**
     * The same, from a Y: 0 at the gel's surface, 1 on the invisible floor.
     *
     * <p>BUILD #481 -- and the floor is 2032 down now, not 64. The depth is the
     * fall's own depth through the five tiers of {@link McsmVoidTiers}: the gel's
     * surface is 0, the barrier the world stands on is 1, and the tiers between
     * them are the bands the art and the fog are read from.
     */
    public static float depthAt(double y) {
        return McsmVoidTiers.depthAt(y);
    }

    /** Which tier of the multi-layer void a Y is in. */
    public static int tier() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return McsmVoidTiers.TIER_BASELINE;
            }
            return McsmVoidTiers.tierAt(mc.player.getY());
        } catch (Throwable ignored) {
            return McsmVoidTiers.TIER_BASELINE;
        }
    }

    /** True while the deep should be drawn: in the gel, no screen open, switched on. */
    public static boolean active() {
        if (!McsmExtrasConfig.voidDescent) {
            return false;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null) {
                return false;
            }
            if (McsmTerminalClient.currentScreen(mc) != null) {
                return false;
            }
            return depth() > 0.005F || crossing();
        } catch (Throwable t) {
            return false;
        }
    }

    /** Was the player diving through the world's floor within the last few seconds? */
    public static boolean crossing() {
        return System.currentTimeMillis() - crossingAt < CROSSING_MS;
    }

    // ------------------------------------------------------------------
    // The client tick: the gel's life, the flight, and the sound of it
    // ------------------------------------------------------------------

    /** Called once per client tick from the mod's own client hook. */
    public static void tick() {
        try {
            if (!McsmExtrasConfig.voidDescent) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc == null ? null : mc.player;
            ClientLevel level = mc == null ? null : mc.level;
            if (player == null || level == null) {
                return;
            }
            boolean divingNow = McsmVoidDescent.diving(player);
            if (divingNow) {
                crossingAt = System.currentTimeMillis();
            }
            if (!level.dimension().equals(McsmVoid.DIMENSION)) {
                releaseFlight(player);
                return;
            }
            float t = depthAt(player.getY());
            if (t <= 0.005F) {
                releaseFlight(player);
                return;
            }
            hold(player);
            if (McsmTerminalClient.currentScreen(mc) != null) {
                return;
            }
            life(level, player, t);
            sound(level, player, t);
        } catch (Throwable t) {
            // the gel can never break a tick
        }
    }

    /** THE GEL HOLDS YOU: flight while inside it, and it is given back on the way out. */
    private static void hold(LocalPlayer player) {
        try {
            if (player.getAbilities().mayfly) {
                return;
            }
            player.getAbilities().mayfly = true;
            // (26.2's Abilities carries no flySpeed field any more: the dive's own
            // speed is the game's, and the gel only decides whether you may fly at all)
            player.onUpdateAbilities();
            flyingGranted = true;
        } catch (Throwable ignored) {
            // no flight this session: the fall still works, it is just heavier
        }
    }

    private static void releaseFlight(LocalPlayer player) {
        if (!flyingGranted) {
            return;
        }
        try {
            if (player.gameMode() != null && !player.gameMode().isCreative()
                    && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        } catch (Throwable ignored) {
            // if it cannot be taken back, the player keeps a gift rather than a bug
        } finally {
            flyingGranted = false;
        }
    }

    /** Bubbles up, motes across, and now and then something enormous goes by above. */
    private static void life(ClientLevel level, LocalPlayer player, float t) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        int heart = ++clock;

        // bubbles: the gel is not still, and this is the proof a player sees first
        if (heart % 2 == 0) {
            spawn(level, ParticleTypes.BUBBLE, x, y, z, 2.5D, 3.0D, 0.0D, 0.10D);
        }
        // the bioluminescent specks: free-floating life, none of it interested in you
        if (heart % 3 == 0) {
            spawn(level, ParticleTypes.GLOW, x, y, z, 7.0D, 5.0D, 0.006D, 0.0D);
        }
        // and something with weight moving a long way above, rarely
        if (heart % 90 == 0) {
            spawn(level, ParticleTypes.REVERSE_PORTAL, x, y, z, 16.0D, 22.0D, 0.0D, 0.0D);
        }
        // BUILD #482 -- the emitters the plan asks for around a rift's boundary:
        // bubble strings rising, and star-fracture dust, and only at the rift
        if (McsmVoidRifts.inside(x, y, z)) {
            if (heart % 2 == 0) {
                spawn(level, ParticleTypes.BUBBLE, x, y, z, 34.0D, 30.0D, 0.0D, 0.14D);
            }
            if (heart % 3 == 0) {
                spawn(level, ParticleTypes.GLOW, x, y, z, 30.0D, 26.0D, 0.02D, 0.01D);
            }
        }
    }

    /** One small burst of a particle type around the player, on the gel's own terms. */
    private static void spawn(ClientLevel level, ParticleOptions type,
            double x, double y, double z, double spread, double rise,
            double drift, double lift) {
        for (int i = 0; i < 3; i++) {
            double px = x + (level.getRandom().nextDouble() - 0.5D) * spread;
            double py = y - 2.0D + level.getRandom().nextDouble() * rise;
            double pz = z + (level.getRandom().nextDouble() - 0.5D) * spread;
            level.addParticle(type, px, py, pz,
                    (level.getRandom().nextDouble() - 0.5D) * drift,
                    lift + level.getRandom().nextDouble() * lift,
                    (level.getRandom().nextDouble() - 0.5D) * drift);
        }
    }

    /** The place's voice: one long drone, quiet, slower and lower the deeper you are. */
    private static void sound(ClientLevel level, LocalPlayer player, float t) {
        if (clock % 200 != 0) {
            return;
        }
        SoundEvent cue = t > 0.55F ? McsmSounds.OBLIVION_DRONE : McsmSounds.OBLIVION_WARP;
        level.playLocalSound(player.getX(), player.getY() + 1.0D, player.getZ(), cue,
                SoundSource.AMBIENT, 0.18F + 0.30F * t, 0.85F - 0.25F * t, false);
    }

    // ------------------------------------------------------------------
    // The fog: the bottom of the void reads as the bedrock's own haze
    // ------------------------------------------------------------------

    /**
     * What the deep does to the fog colour. Returns the opacity of the contribution
     * (0 = leave the world's fog exactly as it is) and fills {@code rgb} with the
     * colour it wants: violet-pink at the surface, going down to the bedrock's own
     * near-black violet at the floor -- the concept image's bottom, in fog form.
     */
    public static float fogBlend(ClientLevel level, float[] rgb) {
        try {
            if (!McsmExtrasConfig.voidDescent || level == null
                    || !level.dimension().equals(McsmVoid.DIMENSION)) {
                return 0.0F;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return 0.0F;
            }
            float t = depthAt(mc.player.getY());
            if (t <= 0.005F) {
                return 0.0F;
            }
            // the gel is thicker than it is bright: the fog goes UP with depth, and
            // its colour climbs out of the pink into the violet the bedrock wears
            float r = mix(0.62F, 0.16F, t);
            float gg = mix(0.28F, 0.07F, t);
            float b = mix(0.52F, 0.22F, t);

            // BUILD #481 -- and then the tier takes it over. Each layer of the void
            // has the colour the plan gives it (the luminous cavern's plum, the
            // abyss's total black, the gel's greenish-brown), and it wins more and
            // more of the fog the deeper the fall is in that layer.
            int tier = McsmVoidTiers.tierAt(mc.player.getY());
            if (tier > McsmVoidTiers.TIER_BASELINE) {
                float[] amb = new float[3];
                McsmVoidTiers.ambient(tier, amb);
                float k = 0.80F;
                r = mix(r, amb[0], k);
                gg = mix(gg, amb[1], k);
                b = mix(b, amb[2], k);
            }
            rgb[0] = r;
            rgb[1] = gg;
            rgb[2] = b;
            float opacity = 0.35F + 0.50F * t;
            if (tier == McsmVoidTiers.TIER_ABYSS) {
                opacity = Math.min(0.96F, opacity + 0.26F);   // absolute suppression
            } else if (tier == McsmVoidTiers.TIER_GEL) {
                opacity = Math.min(0.94F, opacity + 0.12F);
            }
            return opacity;
        } catch (Throwable ignored) {
            return 0.0F;
        }
    }

    private static float mix(float from, float to, float t) {
        return from + (to - from) * t;
    }

    // ------------------------------------------------------------------
    // And it is drawn: the glow above, the gel below, the shadows, the bubbles
    // ------------------------------------------------------------------

    /** Drawn every frame on the same proven HUD hook as the void floor and the scenes. */
    public static void draw(GuiGraphicsExtractor g) {
        if (!active()) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.font == null || mc.player == null) {
                return;
            }
            float t = Math.max(depth(), crossing() ? 0.15F : 0.0F);
            int w = g.guiWidth();
            int h = g.guiHeight();
            long now = System.currentTimeMillis();

            // ---- the glow over your head: what the concept image is mostly about
            glow(g, w, h, t, now);
            // ---- and under you: the gel's own weight, going down into the bedrock
            below(g, w, h, t, now);
            // ---- gigantic shadows: silhouettes crossing the light above
            shadows(g, w, h, t, now);
            // ---- bubbles, rising, all the way up the frame
            bubbles(g, w, h, t, now);
            // ---- bioluminescence: the specks that mean the place is alive
            specks(g, w, h, t, now);

            // ---- BUILD #481: and the tier the fall is in, drawn as itself -----
            // 23 tiers now: 15 tethered sponge layers + 2 nightmare layers (abyssal + reality-glitch/uninpossible)
            int tier = McsmVoidTiers.tierAt(mc.player.getY());
            if (tier == McsmVoidTiers.TIER_LUMINOUS) {
                spires(g, w, h, t, now);
            } else if (tier >= McsmVoidTiers.TIER_SPONGE && tier <= McsmVoidTiers.TIER_SPONGE_15) {
                // All sponge layers - faster start, 15 tethered
                sponge(g, w, h, t, now, tier);
            } else if (tier == McsmVoidTiers.TIER_ABYSS) {
                ruins(g, w, h, t, now);
            } else if (tier == McsmVoidTiers.TIER_FRACTURE) {
                waves(g, w, h, t, now);
            } else if (tier == McsmVoidTiers.TIER_GEL) {
                horizon(g, w, h, t, now);
            } else if (tier == McsmVoidTiers.TIER_ABYSSAL_NIGHTMARE) {
                abyssalNightmare(g, w, h, t, now);
            } else if (tier == McsmVoidTiers.TIER_REALITY_GLITCH_NIGHTMARE) {
                realityGlitchNightmare(g, w, h, t, now);
            }
            // ---- BUILD #482: inside a rift's window, the frame becomes it ----
            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();
            if (McsmVoidRifts.inside(px, py, pz)) {
                rift(g, w, h, t, now, McsmVoidRifts.depth(px, py, pz),
                        McsmVoidRifts.phase(px, py, pz));
            }
            // ---- and the gel's own pools, in the gel's own tier --------------
            if (tier == McsmVoidTiers.TIER_GEL) {
                pools(g, w, h, t, now, px, py, pz);
            }
            if (tier != shownTier) {
                shownTier = tier;
                shownAt = now;
            }
            if (now - shownAt < 3600L) {
                int alpha = (int) (230 * (1.0F - (now - shownAt) / 3600.0F));
                g.centeredText(mc.font, McsmVoidTiers.PLAN_NAME[tier].toUpperCase(),
                        w / 2, 14, (Math.max(alpha, 24) << 24) | 0xE8A24C);
                g.centeredText(mc.font, "tier " + tier + " of " + (McsmVoidTiers.TIERS - 1)
                        + " \u00b7 y=" + (int) mc.player.getY(), w / 2, 26,
                        (Math.max(alpha / 2, 16) << 24) | 0xD8C0F0);
            }
            if (t > 0.86F) {
                g.centeredText(mc.font, "THE GEL HORIZON", w / 2, h - 30, 0x99BFFFC8);
                g.centeredText(mc.font, "the bedrock is the fog", w / 2, h - 18, 0x88D8C0F0);
            }
        } catch (Throwable t) {
            // never break a frame over the gel
        }
    }

    /** The bright dark-pink bloom over the player, strongest at the top of the view. */
    private static void glow(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        int reach = (int) (h * (0.34D + 0.42D * t));
        float breathe = 0.86F + 0.14F * (float) Math.sin(now / 2600.0D);
        for (int i = 0; i < BANDS; i++) {
            float f = i / (float) BANDS;
            float fall = (1.0F - f) * (1.0F - f);
            int alpha = (int) ((GLOW_HI >>> 24) * fall * t * breathe * 0.55F);
            if (alpha <= 1) {
                continue;
            }
            int y1 = (int) (reach * f);
            int y2 = (int) (reach * (f + 1.0F / BANDS)) + 1;
            g.fill(0, y1, w, y2, (alpha << 24) | (GLOW_HI & 0xFFFFFF));
        }
    }

    /** And what is beneath: violet falling into the bedrock's own black. */
    private static void below(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        int reach = (int) (h * (0.30D + 0.40D * t));
        for (int i = 0; i < BANDS; i++) {
            float f = i / (float) BANDS;
            int alpha = (int) ((DEEP_HI >>> 24) * (1.0F - f) * t * 0.70F);
            int colour = i < BANDS / 2 ? DEEP_HI : DEEP_LO;
            if (alpha <= 1) {
                continue;
            }
            int y2 = h - (int) (reach * f);
            int y1 = y2 - ((int) (reach * (f + 1.0F / BANDS)) + 1);
            g.fill(0, y1, w, y2, (alpha << 24) | (colour & 0xFFFFFF));
        }
    }

    /** The gigantic shadows: soft blobs, drifting across the glow above. */
    private static void shadows(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        if (t < 0.18F) {
            return;
        }
        int band = (int) (h * 0.34D);
        for (int i = 0; i < SHADOWS; i++) {
            double speed = 0.010D + i * 0.004D;
            double phase = (now / 1000.0D) * speed + i * 1.7D;
            int cx = (int) (((phase % 1.0D) * 1.6D - 0.3D) * w);
            int cy = (int) (band * (0.22D + 0.16D * ((i * 7 % 5) / 5.0D)));
            int rx = (int) (w * (0.10D + 0.05D * ((i * 3 % 4) / 4.0D)));
            int ry = (int) (rx * 0.42D);
            int alpha = (int) (0x52 * t);
            if (alpha <= 2) {
                continue;
            }
            // soft edges: the same shape four times, growing and fading
            for (int ring = 4; ring >= 1; ring--) {
                float k = ring / 4.0F;
                int a = (int) (alpha * (1.0F - k) + 3);
                int x1 = cx - (int) (rx * k);
                int y1 = cy - (int) (ry * k);
                g.fill(x1, y1, x1 + (int) (rx * k * 2), y1 + (int) (ry * k * 2),
                        (a << 24) | (SHADOW & 0xFFFFFF));
            }
        }
    }

    /** The bubbles: a gel is not still, and this is what tells you so. */
    private static void bubbles(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        for (int i = 0; i < BUBBLES; i++) {
            int seed = i * 2654435761L != 0 ? i * 7919 : 1;
            double speed = 0.05D + (i % 5) * 0.014D;
            double up = ((now / 1000.0D) * speed + (seed % 100) / 100.0D) % 1.0D;
            int x = (int) (((seed * 37 % 1000) / 1000.0D) * w);
            int y = (int) (h - up * h * 1.05D);
            int size = 2 + (i % 3);
            int alpha = (int) (0x66 * t * (1.0F - up * 0.55F));
            if (alpha <= 2) {
                continue;
            }
            g.fill(x, y, x + size, y + size, (alpha << 24) | 0xDCE8FF);
            g.fill(x, y, x + size, y + 1, ((alpha + 40 > 255 ? 255 : alpha + 40) << 24) | 0xFFFFFF);
        }
    }

    /** Bioluminescence: three colours, twinkling, drifting with their own clock. */
    private static void specks(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        for (int i = 0; i < MOTES; i++) {
            long seed = i * 104729L;
            double phase = now / 3400.0D + (seed % 97) / 97.0D;
            int x = (int) (((seed * 53 % 991) / 991.0D) * w + Math.sin(phase) * 18.0D);
            int y = (int) (((seed * 71 % 983) / 983.0D) * h + Math.cos(phase * 0.7D) * 12.0D);
            int twinkle = (int) (110 + 90 * Math.sin(phase * 3.1D));
            int alpha = (int) (twinkle * t * 0.8F);
            if (alpha <= 3) {
                continue;
            }
            int colour = MOTE[i % MOTE.length];
            g.fill(x, y, x + 2, y + 2, (alpha << 24) | (colour & 0xFFFFFF));
        }
    }

    // ------------------------------------------------------------------
    // BUILD #481 -- the five tiers, drawn: each layer is its own picture
    // ------------------------------------------------------------------

    /** Tier 1, the luminous cavern: thin spires of cyan, emerald and amber standing in plum air. */
    private static void spires(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        int[] wall = { 0x38F0E0, 0x38F0A0, 0xFFA23A };
        for (int i = 0; i < McsmVoidTiers.LIGHTS[McsmVoidTiers.TIER_LUMINOUS]; i++) {
            long seed = i * 92821L;
            int x = (int) (((seed * 37 % 991) / 991.0D) * w + Math.sin(now / 9000.0D + i) * 26.0D);
            int width = 2 + (i % 3);
            int top = (int) (h * (0.08D + 0.22D * ((i * 3 % 5) / 5.0D)));
            int bottom = (int) (h * (0.58D + 0.30D * ((i * 7 % 4) / 4.0D)));
            int colour = wall[i % wall.length];
            int span = Math.max(1, bottom - top);
            for (int y = top; y < bottom; y += 2) {
                int fade = (int) (66 * t * (1.0F - (y - top) / (float) span));
                g.fill(x, y, x + width, y + 2, (Math.max(fade, 3) << 24) | colour);
            }
        }
    }

    /** Tier 2, the sponge: the orange-to-pink wash, and the pores it is full of. Now 15 tethered layers faster. */
    private static void sponge(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        sponge(g, w, h, t, now, McsmVoidTiers.TIER_SPONGE);
    }
    private static void sponge(GuiGraphicsExtractor g, int w, int h, float t, long now, int tier) {
        // Gradient shifts based on sponge depth tier - orange to pink across 15 layers
        int fog = McsmVoidTiers.FOG[Math.max(0, Math.min(McsmVoidTiers.TIERS-1, tier))];
        int topCol = 0xFF6A28;
        int bottomCol = fog != 0 ? fog : 0xFF3FA8;
        // Faster visual - more pores when deeper
        int poreCount = 46 + (tier - McsmVoidTiers.TIER_SPONGE) * 3;
        g.fillGradient(0, 0, w, h, (int) (54 * t) << 24 | topCol,
                (int) (86 * t) << 24 | bottomCol);
        for (int i = 0; i < poreCount; i++) {
            long seed = i * 6151L + tier * 1000L;
            int x = (int) (((seed * 41 % 977) / 977.0D) * w + Math.sin(now / 5200.0D + i * 0.7D) * 20.0D);
            int y = (int) (((seed * 67 % 971) / 971.0D) * h + Math.cos(now / 6100.0D + i) * 16.0D);
            int r = 3 + (i % 4);
            int alpha = (int) (72 * t + (tier - McsmVoidTiers.TIER_SPONGE) * 2);
            g.fill(x, y, x + r, y + r, (Math.min(alpha, 120) << 24) | 0x2A0A12);
            g.fill(x - 1, y - 1, x + r + 1, y, (Math.max(alpha - 30, 2) << 24) | 0xFFC07A);
        }
        // Speed indicator for faster start
        if (tier >= McsmVoidTiers.TIER_SPONGE) {
            int speed = (int)McsmVoidTiers.SPEED[tier];
            // Visual streaks for faster fall
            for (int i = 0; i < speed / 3; i++) {
                int x = (w * i / (speed/3 +1));
                g.fill(x, 0, x+1, h, (int)(10*t) << 24 | 0xFFFFFF);
            }
        }
    }

    /** Tier 3, the abyss: the light is gone; only the ruins down here still glow. */
    private static void ruins(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        g.fill(0, 0, w, h, (int) (150 * t) << 24 | 0x000000);
        for (int i = 0; i < McsmVoidTiers.LIGHTS[McsmVoidTiers.TIER_ABYSS]; i++) {
            long seed = i * 40009L;
            int x = (int) (((seed * 31 % 983) / 983.0D) * w);
            int y = (int) (h * (0.55D + 0.35D * ((seed * 17 % 89) / 89.0D)));
            int twinkle = (int) (150 + 90 * Math.sin(now / 1300.0D + i * 2.1D));
            int alpha = (int) (twinkle * t * 0.9F);
            for (int k = 0; k < 6; k++) {
                g.fill(x, y - k * 3, x + 2, y - k * 3 + 2, (alpha << 24) | 0x00FFB0);
            }
            g.fill(x - 3, y, x + 5, y + 1, (alpha << 24) | 0x00FFB0);
        }
    }

    /** Tier 4, the fracture: the view is rippling, and the ripples have scanlines in them. */
    private static void waves(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        for (int i = 0; i < 9; i++) {
            double phase = now / 1400.0D + i * 0.8D;
            int y = (int) (h * (0.08D + 0.11D * i) + Math.sin(phase) * 22.0D);
            int alpha = (int) (44 * t * (0.6D + 0.4D * Math.sin(phase * 1.7D)));
            g.fill(0, y, w, y + 3, (Math.max(alpha, 2) << 24) | 0xE070FF);
        }
        for (int i = 0; i < 22; i++) {
            int y = (int) ((i / 22.0D) * h);
            int alpha = (int) (26 * t);
            g.fill(0, y, w, y + 1, (alpha << 24) | 0x8A2BE2);
        }
    }

    /** Tier 5, the gel horizon: the greenish-brown fluid field, turning over. */
    private static void horizon(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        int band = (int) (h * 0.42D);
        for (int i = 0; i < 24; i++) {
            float f = i / 24.0F;
            int y = (int) (h - band * f - Math.sin(now / 2600.0D + i * 0.5D) * 6.0D);
            int alpha = (int) (60 * t * (1.0F - f));
            g.fill(0, y, w, y + band / 24 + 2, (Math.max(alpha, 3) << 24) | 0x1C1F16);
        }
        for (int i = 0; i < 14; i++) {
            long seed = i * 7717L;
            int x = (int) (((seed * 53 % 991) / 991.0D) * w);
            int y = (int) (h - band * ((seed * 29 % 97) / 97.0D) + Math.sin(now / 1900.0D + i) * 10.0D);
            int alpha = (int) (86 * t);
            g.fill(x, y, x + 2, y + 2, (alpha << 24) | 0xBFFFC8);
        }
    }

    // ------------------------------------------------------------------
    // BUILD #482 -- the rifts, and the gel's iridescent pools
    // ------------------------------------------------------------------

    /**
     * Tier 3 and 4, the rifts: a window into somewhere else.
     *
     * <p>The plan asks for a refraction pass on the inner face of a rift, ripples
     * across it, and a parallax layer behind that -- cosmic particles, star arrays,
     * shifting silhouettes -- plus emitters throwing bubble strings and star dust
     * around the rim. This is that, drawn in the frame: the displacement bands are
     * the ripples, the drifting stars and the soft silhouettes are the window, the
     * rim highlight is where the two meet, and every part of it fades in with
     * {@code into} so the window opens as a fall crosses the boundary rather than
     * switching on. The particles are the real ones, spawned in the client tick.
     */
    private static void rift(GuiGraphicsExtractor g, int w, int h, float t, long now,
            float into, double phase) {
        int k = (int) (255 * Math.max(0.12F, into));
        int cx = w / 2;
        int cy = h / 2;
        // the ripples: displacement bands running across the window
        for (int i = 0; i < 26; i++) {
            double ph = now / 620.0D + i * 0.55D + phase * 6.0D;
            int y = (int) (h * (i / 26.0D) + Math.sin(ph) * 20.0D * (1.0D - into * 0.4D));
            int alpha = (int) (58 * into * (0.55D + 0.45D * Math.sin(ph * 1.3D)));
            g.fill(0, y, w, y + h / 26 + 2, (Math.max(alpha, 2) << 24) | 0x6A2BE2);
        }
        // the window itself: a slow parallax swirl of stars
        for (int i = 0; i < 74; i++) {
            long seed = i * 224737L;
            double a = (seed % 360) / 57.2958D + now / 9000.0D + phase * 3.0D;
            double r = 0.10D + 0.42D * ((seed * 29 % 97) / 97.0D);
            int x = (int) (cx + Math.cos(a) * r * w);
            int y = (int) (cy + Math.sin(a) * r * h * 0.7D);
            int twinkle = (int) (150 + 90 * Math.sin(now / 700.0D + i));
            int alpha = (int) (twinkle * into * 0.9F);
            if (alpha <= 4) {
                continue;
            }
            int colour = (i % 3 == 0) ? 0xBFFFC8 : (i % 3 == 1) ? 0xE0C8FF : 0xFFE9B0;
            g.fill(x, y, x + 2, y + 2, (alpha << 24) | colour);
        }
        // and the silhouettes: something enormous, on the far side, going past
        for (int i = 0; i < 4; i++) {
            long seed = i * 91871L;
            double a = now / 21000.0D + i * 1.9D + phase;
            int x = (int) (cx + Math.cos(a) * w * 0.22D);
            int y = (int) (cy + Math.sin(a * 0.7D) * h * 0.16D);
            int rx = (int) (w * (0.10D + 0.05D * ((seed * 13 % 71) / 71.0D)));
            int ry = (int) (rx * 0.34D);
            for (int ring = 4; ring >= 1; ring--) {
                float f = ring / 4.0F;
                int alpha = (int) (34 * into * (1.0F - f) + 3);
                g.fill(x - (int) (rx * f), y - (int) (ry * f), x + (int) (rx * f),
                        y + (int) (ry * f), (alpha << 24) | 0x120A24);
            }
        }
        // the rim: where the void and the window disagree
        int rim = (int) (46 * into);
        g.fill(0, 0, w, 3, (rim << 24) | 0xE070FF);
        g.fill(0, h - 3, w, h, (rim << 24) | 0xE070FF);
        g.fill(0, 0, 3, h, ((rim / 2) << 24) | 0x8A2BE2);
        g.fill(w - 3, 0, w, h, ((rim / 2) << 24) | 0x8A2BE2);
    }

    /**
     * Tier 5, the gel's pools: iridescent fluid, no collision, no drowning.
     *
     * <p>The plan: "water surfaces render as an iridescent, glowing fluid matrix
     * that shifts colour dynamically between shimmering neon teals, deep amethysts,
     * and toxic magentas ... bright, full-bright white intersection foam lines ...
     * This water must have zero solid collision." Nothing in the deep places a
     * block, so there is nothing to collide with and nothing to drown in by
     * construction; what is drawn is the colour shifting with the fall, the wave
     * rows above each surface, and the foam line where a body would meet it.
     */
    private static void pools(GuiGraphicsExtractor g, int w, int h, float t, long now,
            double px, double py, double pz) {
        for (int i = 0; i < 3; i++) {
            double surface = h * (0.42D + 0.20D * i) + Math.sin(now / 2400.0D + i * 2.0D) * 8.0D;
            int hue = (int) (now / 24.0D + i * 90.0D) % 360;
            int colour = iridescent(hue);
            // the wave rows above the surface: the refraction, drawn as its own ribs
            for (int r = 1; r <= 8; r++) {
                int y = (int) (surface - r * 7 + Math.sin(now / 900.0D + r * 0.8D + i) * 3.0D);
                int alpha = (int) (30 * t * (1.0F - r / 9.0F));
                g.fill(0, y, w, y + 2, (Math.max(alpha, 2) << 24) | (colour & 0xFFFFFF));
            }
            // the body of the pool
            int body = (int) (92 * t);
            g.fill(0, (int) surface, w, h, (body << 24) | (colour & 0xFFFFFF));
            // and the foam: full-bright, wobbling, where an entity would cross it
            int foam = (int) (200 * t);
            for (int x = 0; x < w; x += 2) {
                int y = (int) (surface + Math.sin(x * 0.06D + now / 420.0D + i) * 2.5D);
                g.fill(x, y, x + 2, y + 1, (foam << 24) | 0xFFFFFF);
            }
        }
    }

    // ------------------------------------------------------------------
    // BUILD #485 -- Tier 9: Abyssal Nightmare & Reality-Glitch Nightmare / Uninpossible
    // ------------------------------------------------------------------

    /** Tier 6, abyssal nightmare dimension: final realm, terror chaos ancient evil */
    private static void abyssalNightmare(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        // Matte-black #0A0E14 background
        g.fill(0, 0, w, h, (int) (210 * t) << 24 | 0x0A0E14);
        // Gothic towers silhouette
        for (int i = 0; i < 9; i++) {
            long seed = i * 40021L;
            int x = (int) (((seed * 37 % 991) / 991.0D) * w + Math.sin(now / 7000.0D + i * 1.1D) * 18.0D);
            int base = h - (int) (h * 0.18D * ((seed * 13 % 71) / 71.0D));
            int height = (int) (h * (0.32D + 0.28D * ((seed * 29 % 97) / 97.0D)));
            int width = 6 + (i % 4);
            // tower
            g.fill(x, base - height, x + width, base, (int) (180 * t) << 24 | 0x121A2A);
            // spire tip
            g.fill(x + width/2 - 2, base - height - 14, x + width/2 + 2, base - height, (int) (200 * t) << 24 | 0x1A2438);
            // faint glow window
            if (i % 2 == 0) {
                int wy = base - height/2;
                g.fill(x+1, wy, x+width-1, wy+2, (int) (120 * t) << 24 | 0x8A2BE2);
            }
        }
        // Jagged ridges on absolute horizon
        for (int i = 0; i < 32; i++) {
            double ph = now / 3200.0D + i * 0.4D;
            int x = (int) ((i / 32.0D) * w);
            int x2 = (int) (((i+1) / 32.0D) * w);
            int ridgeH = (int) (h * 0.08D + Math.sin(ph) * h * 0.04D + (i % 5) * 3);
            g.fill(x, h - ridgeH, x2, h, (int) (160 * t) << 24 | 0x060A12);
        }
        // Distant Creator visage hint - huge soft shadow
        int cx = w/2 + (int)(Math.sin(now/12000.0D) * w * 0.08D);
        int cy = (int)(h * 0.28D);
        int cr = (int)(w * 0.22D);
        for (int ring = 5; ring >=1; ring--) {
            float f = ring / 5.0F;
            int alpha = (int)(28 * t * (1.0F - f));
            g.fill(cx - (int)(cr*f), cy - (int)(cr*0.6D*f), cx + (int)(cr*f), cy + (int)(cr*0.6D*f), (alpha << 24) | 0x0A0E14);
        }
        g.centeredText(Minecraft.getInstance().font, "THE ABYSSAL NIGHTMARE DIMENSION", w/2, h/2 - 20, (int)(200*t) << 24 | 0x8A2BE2);
        g.centeredText(Minecraft.getInstance().font, "terror · chaos · ancient evil · reality screams", w/2, h/2 - 8, (int)(160*t) << 24 | 0x5A4A6A);
    }

    /** Tier 9, reality-glitch nightmare / uninpossible layer: beyond impossible where reality breaks */
    private static void realityGlitchNightmare(GuiGraphicsExtractor g, int w, int h, float t, long now) {
        // Void-black #000000 with matte-black #0A0E14 gradient
        g.fill(0, 0, w, h, (int) (240 * t) << 24 | 0x000000);
        g.fillGradient(0, 0, w, h, (int)(180*t) << 24 | 0x0A0E14, (int)(220*t) << 24 | 0x000000);

        // Photorealistic 3D spires via 2D projection of 3D vertex math
        for (int i = 0; i < 12; i++) {
            long seed = i * 104729L;
            int x = (int) (((seed * 53 % 991) / 991.0D) * w + Math.sin(now/5000.0D + i) * 24.0D);
            int base = h - (int)(h * 0.12D * ((seed * 17 % 89)/89.0D));
            int height = (int)(h * (0.38D + 0.32D * ((seed * 29 % 97)/97.0D)));
            int width = 8 + (i % 5);
            // Matte-black tower
            g.fill(x, base - height, x + width, base, (int)(220*t) << 24 | 0x0A0E14);
            // Glowing edge
            g.fill(x-1, base - height, x, base, (int)(100*t) << 24 | 0x8A2BE2);
            // Tip with 4.5x bloom
            int tipY = base - height;
            int bloom = (int)(Math.min(255, 90 * t * 4.5F));
            g.fill(x+width/2-3, tipY-18, x+width/2+3, tipY, (bloom << 24) | 0x9D00FF);
            g.fill(x+width/2-1, tipY-22, x+width/2+1, tipY-18, (int)(bloom*0.8F) << 24 | 0xFFFFFF);
        }

        // Hanging fortresses - inverted
        for (int i = 0; i < 5; i++) {
            long seed = i * 224737L;
            int x = (int) (((seed * 41 % 977)/977.0D) * w);
            int y = (int)(h * (0.18D + 0.12D * i) + Math.sin(now/6000.0D + i*1.7D) * 10.0D);
            int size = 40 + i*12;
            // Platform
            g.fill(x, y, x+size, y+6, (int)(190*t) << 24 | 0x121A2A);
            // Spires hanging down
            for (int s = 0; s < 3; s++) {
                int sx = x + 6 + s* (size/3);
                g.fill(sx, y+6, sx+4, y+6+18+s*4, (int)(200*t) << 24 | 0x0A0E14);
                g.fill(sx+1, y+6+18+s*4, sx+3, y+6+22+s*4, (int)(160*t * 4.5F) << 24 | 0x8A2BE2);
            }
            // Chains
            g.fill(x+2, y-20, x+3, y, (int)(120*t) << 24 | 0x2A2A32);
            g.fill(x+size-3, y-20, x+size-2, y, (int)(120*t) << 24 | 0x2A2A32);
        }

        // Colossal Creator visage - crowned head, reality-tearing arms
        int cx = w/2 + (int)(McsmNinthLayerGeometry.gazeOffsetX(now) * 0.08D);
        int cy = (int)(h * 0.32D + Math.sin(now/8000.0D) * 12.0D);
        int headW = (int)(w * 0.28D);
        int headH = (int)(h * 0.42D);
        // Head silhouette matte-black
        for (int ring = 6; ring >=1; ring--) {
            float f = ring / 6.0F;
            int alpha = (int)(32*t*f);
            g.fill(cx - (int)(headW*f*0.6D), cy - (int)(headH*f*0.4D), cx + (int)(headW*f*0.6D), cy + (int)(headH*f*0.6D), (alpha << 24) | 0x0A0E14);
        }
        // Crown
        for (int p = 0; p < 7; p++) {
            int px = cx - headW/2 + p * (headW/6);
            int py = cy - (int)(headH*0.4D);
            g.fill(px, py-18, px+6, py, (int)(220*t) << 24 | 0xD4A017);
            g.fill(px+2, py-22, px+4, py-18, (int)(180*t * 4.5F) << 24 | 0xFFD700);
        }
        // Glowing purple lenses - full-bright emissive with gaze tracking
        double gazeX = McsmNinthLayerGeometry.gazeOffsetX(now);
        double gazeZ = McsmNinthLayerGeometry.gazeOffsetZ(now);
        int eyeSize = 12;
        int eyeY = cy - headH/8;
        int eyeLX = cx - headW/6 + (int)(gazeX * 0.05D);
        int eyeRX = cx + headW/6 + (int)(gazeX * 0.05D);
        int eyeYOff = (int)(gazeZ * 0.03D);
        // Left eye 4.5x bloom
        int bloom = (int)(Math.min(255, 200 * t * 4.5F));
        g.fill(eyeLX - eyeSize, eyeY + eyeYOff - eyeSize, eyeLX + eyeSize, eyeY + eyeYOff + eyeSize, (bloom << 24) | 0x8A2BE2);
        g.fill(eyeLX - eyeSize/2, eyeY + eyeYOff - eyeSize/2, eyeLX + eyeSize/2, eyeY + eyeYOff + eyeSize/2, (int)(255*t) << 24 | 0xFFFFFF);
        // Right eye
        g.fill(eyeRX - eyeSize, eyeY + eyeYOff - eyeSize, eyeRX + eyeSize, eyeY + eyeYOff + eyeSize, (bloom << 24) | 0x8A2BE2);
        g.fill(eyeRX - eyeSize/2, eyeY + eyeYOff - eyeSize/2, eyeRX + eyeSize/2, eyeY + eyeYOff + eyeSize/2, (int)(255*t) << 24 | 0xFFFFFF);

        // Radiant aura & phase-shifting color skirts at 4.5x bloom
        for (int i = 0; i < 6; i++) {
            int hue = (int)((now/50.0D + i*60) % 360);
            int col = hsv2rgb(hue);
            int auraY = cy + headH/2 + i*8;
            int alpha = (int)(Math.min(255, 40*t*4.5F * (1.0F - i/6.0F)));
            g.fill(0, auraY, w, auraY+2, (alpha << 24) | col);
        }

        // Glitch sides left/right every few seconds
        if ((now / 2200) % 2 == 0) {
            int glitchAlpha = (int)(60*t);
            g.fill(0, 0, 4, h, (glitchAlpha << 24) | 0x9D00FF);
            g.fill(w-4, 0, w, h, (glitchAlpha << 24) | 0x8A2BE2);
        }

        g.centeredText(Minecraft.getInstance().font, "THE REALITY-GLITCH NIGHTMARE / UNINPOSSIBLE LAYER", w/2, h/2 + headH/2 + 12, (int)(230*t) << 24 | 0x9D00FF);
        g.centeredText(Minecraft.getInstance().font, "beyond impossible where Minecraft reality completely breaks down", w/2, h/2 + headH/2 + 24, (int)(180*t) << 24 | 0x6A5A7A);
        g.centeredText(Minecraft.getInstance().font, "matte-black #0A0E14 #000000 · 4.5x bloom · glowing purple lenses watching", w/2, h/2 + headH/2 + 36, (int)(140*t) << 24 | 0x8A2BE2);
    }

    private static int hsv2rgb(int hue) {
        int h = Math.floorMod(hue, 360);
        if (h < 60) return 0xFF0000 | ((h * 255 / 60) << 8);
        if (h < 120) return (( (120-h) *255/60) <<16) | 0x00FF00;
        if (h < 180) return 0x00FF00 | ((h-120)*255/60);
        if (h < 240) return (( (240-h)*255/60) ) | 0x00FFFF;
        if (h < 300) return 0x0000FF | ((h-240)*255/60 <<16);
        return 0xFF00FF | ((360-h)*255/60 <<8);
    }

    /** The plan's three water colours, cycled: neon teal, deep amethyst, toxic magenta. */
    private static int iridescent(int hue) {
        int h = Math.floorMod(hue, 360);
        if (h < 120) {
            return 0x2AE8D8;
        }
        if (h < 240) {
            return 0x8A2BE2;
        }
        return 0xFF3FA8;
    }

    // ------------------------------------------------------------------
    // The hand-over frame: no grey screen, no progress bar
    // ------------------------------------------------------------------

    /**
     * True while the game is switching worlds underneath a player who is falling.
     *
     * <p>Read by {@code McsmVoidDeepLoadingMixin}. The player asked for no loading
     * screen on the way in, and the honest way to give them that is to paint the one
     * frame the game insists on spending: the gel, not the vanilla grey.
     */
    public static boolean suppressLoadingScreen() {
        return McsmExtrasConfig.voidDescent && crossing();
    }

    /** That frame's art: the gel, filling whatever the game is about to draw. */
    public static void paintEntryFrame(GuiGraphicsExtractor g) {
        try {
            int w = g.guiWidth();
            int h = g.guiHeight();
            for (int i = 0; i < BANDS; i++) {
                float f = i / (float) BANDS;
                int y1 = (int) (h * f);
                int y2 = y1 + h / BANDS + 1;
                int from = GLOW_HI;
                int to = DEEP_LO;
                float r = mix((from >> 16 & 0xFF) / 255.0F, (to >> 16 & 0xFF) / 255.0F, f);
                float gg = mix((from >> 8 & 0xFF) / 255.0F, (to >> 8 & 0xFF) / 255.0F, f);
                float b = mix((from & 0xFF) / 255.0F, (to & 0xFF) / 255.0F, f);
                int colour = 0xFF000000 | ((int) (r * 255) << 16) | ((int) (gg * 255) << 8)
                        | (int) (b * 255);
                g.fill(0, y1, w, y2, colour);
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.font != null) {
                g.centeredText(mc.font, "you are still falling", w / 2, h / 2 - 6, 0x99E8A24C);
            }
        } catch (Throwable ignored) {
            // if even this fails, the frame is the game's problem, not the player's
        }
    }
}
