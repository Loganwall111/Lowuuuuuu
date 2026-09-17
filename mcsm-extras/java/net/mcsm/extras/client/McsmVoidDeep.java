package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmSounds;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidDescent;
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

    /** The same, from a Y: 0 at the gel's surface, 1 on the invisible floor. */
    public static float depthAt(double y) {
        float span = McsmVoidDescent.SURFACE_Y - McsmVoid.FLOOR_Y;
        if (span <= 0.0F) {
            return 0.0F;
        }
        float t = (float) ((McsmVoidDescent.SURFACE_Y - y) / span);
        return Math.max(0.0F, Math.min(1.0F, t));
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
            player.getAbilities().flySpeed = Math.max(player.getAbilities().flySpeed, 0.05F);
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
            rgb[0] = mix(0.62F, 0.16F, t);
            rgb[1] = mix(0.28F, 0.07F, t);
            rgb[2] = mix(0.52F, 0.22F, t);
            return 0.35F + 0.50F * t;
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
            below(g, w, h, t);
            // ---- gigantic shadows: silhouettes crossing the light above
            shadows(g, w, h, t, now);
            // ---- bubbles, rising, all the way up the frame
            bubbles(g, w, h, t, now);
            // ---- bioluminescence: the specks that mean the place is alive
            specks(g, w, h, t, now);

            if (t > 0.62F) {
                g.centeredText(mc.font, "THE DEEP", w / 2, 14, 0x99E8A24C);
                g.centeredText(mc.font, "y=" + (int) mc.player.getY() + " \u00b7 "
                        + "the bedrock is the fog", w / 2, 26, 0x88D8C0F0);
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
