package net.mcsm.extras.client;

import java.util.Random;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoidAging;

/**
 * BUILD #475 -- THE HALLUCINATIONS.
 *
 * <p>This is the owed half of the D.8 promise that has only ever existed as two
 * switches: {@code Reality Glitches + Hallucinations} and {@code Hallucination
 * Intensity} were on the settings screen and read by nothing. The thing itself is
 * described by the user as part of what MASSG does to a world -- "reality warping,
 * hallucinations, corruption, screen glitches" -- and it belongs to the void as well:
 * the aging stages are the player being changed by that place, and this is what being
 * changed looks like from the inside.
 *
 * <p>WHAT IT DOES, in four kinds, none of which touch the world:
 *
 * <ul>
 *   <li><b>TEAR</b> -- the frame comes apart in horizontal bands, each a strip of the
 *       phase's own colour with a one-pixel chroma edge above and below it. Nothing is
 *       sampled from the framebuffer: the bands are painted, which is why they work on
 *       any renderer, with any pack, at any framerate.</li>
 *   <li><b>FIGURE</b> -- a silhouette at the edge of vision: near-black, too tall,
 *       standing a little too still, with two violet lenses and a faint aura. It is
 *       painted, never spawned: a hallucination that exists in the world would be a
 *       lie the game could interact with.</li>
 *   <li><b>FALSE SKY</b> -- the colour of a sky that is not there, washing over the
 *       screen with two flashes that never strike anything.</li>
 *   <li><b>WHISPER</b> -- one line of the lore, low on the screen, flickering.</li>
 * </ul>
 *
 * <p>WHEN. Only when the world is actually wrong, on the same feeds everything else
 * uses: MASSG present ({@link McsmMassgSky#active()}), the player's own void aging
 * ({@link McsmVoidAging} stages, through {@link McsmVoidAgingClient}), or a storm past
 * its rose phase ({@link McsmStormPhase#phase()}). Intensity scales with
 * {@link McsmExtrasConfig#hallucinationIntensity} and stops at zero when reality
 * glitches are switched off.
 *
 * <p>Survival only, and never while the game's own HUD is hidden: a player in creative
 * is looking at the world, not living in it, and this is the world doing something to
 * the player. Draw-only through the one proven per-frame extractor, bounded work,
 * wrapped -- an overlay that throws is a frame that does not draw.
 */
public final class McsmHallucinations {

    /** Nothing is drawn at or below this. */
    private static final float FLOOR = 0.02F;

    /** The four kinds. */
    private static final int TEAR = 0;
    private static final int FIGURE = 1;
    private static final int FALSE_SKY = 2;
    private static final int WHISPER = 3;
    private static final int KINDS = 4;

    /** The violet the lenses and the chroma edges wear (McsmIdentity's decayed glow). */
    private static final int VIOLET = 0xFF8A5CFF;

    /** A TEAR's span, in milliseconds, at intensity 0 and at intensity 1. */
    private static final long TEAR_MS_MIN = 420L;
    private static final long TEAR_MS_MAX = 900L;

    /** Between one episode and the next: how long the world behaves again. */
    private static final long GAP_MS_MIN = 9000L;
    private static final long GAP_MS_MAX = 30000L;

    /** The lore, whispered. Short lines, because a whisper is not a paragraph. */
    private static final String[] WHISPERS = {
        "\u00a75the count does not stop",
        "\u00a75you were not here a moment ago",
        "\u00a75it is already inside the sky",
        "\u00a75the door opened back",
        "\u00a75look up. do not look up.",
        "\u00a75MASSG",
        "\u00a75it remembers the shape of you",
        "\u00a75the floor is only very patient",
    };

    private static long nextAt = 0L;
    private static long startAt = 0L;
    private static long endAt = 0L;
    private static int kind = -1;
    private static long seed = 0L;
    private static float lastIntensity = 0.0F;

    private McsmHallucinations() {
    }

    /**
     * How wrong the world is right now, 0..1: the strongest live source, scaled by the
     * player's own setting. Any of the three feeds alone can carry it.
     */
    public static float intensity() {
        try {
            if (!McsmExtrasConfig.realityGlitches) {
                return 0.0F;
            }
            double setting = Mth.clamp(McsmExtrasConfig.hallucinationIntensity, 0.0D, 1.0D);
            if (setting <= 0.0D) {
                return 0.0F;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null) {
                return 0.0F;
            }
            // survival only: this is the world doing something to the player
            if (mc.player.isCreative() || mc.player.isSpectator()) {
                return 0.0F;
            }
            float source = 0.0F;
            // the creature: nothing is normal while it is up there
            if (McsmMassgSky.active()) {
                source = 1.0F;
            }
            // and the void's own aging of YOU -- the state that already exists
            try {
                long age = (long) McsmVoidAgingClient.ageOf(mc.player.getId());
                source = Math.max(source,
                        McsmVoidAging.stageOf(age) / (float) (McsmVoidAging.STAGES.length - 1) * 0.9F);
            } catch (Throwable ignored) {
                // the aging feed is optional here
            }
            // and a storm past its rose phase, which is when the sky stops behaving
            float phase = McsmStormPhase.phase();
            if (phase > McsmStormPhase.SHELL_ROSE) {
                source = Math.max(source, Math.min(1.0F,
                        (phase - McsmStormPhase.SHELL_ROSE) / 2.0F) * 0.7F);
            }
            return (float) (source * setting);
        } catch (Throwable ignored) {
            return 0.0F;
        }
    }

    /** Called once per HUD frame, after the world has been drawn. */
    public static void paint(GuiGraphicsExtractor g, DeltaTracker delta) {
        try {
            if (g == null) {
                return;
            }
            long now = System.currentTimeMillis();
            float it = intensity();
            lastIntensity = it;
            if (it <= FLOOR) {
                // the world is behaving: forget the episode entirely
                kind = -1;
                nextAt = 0L;
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.font == null) {
                return;
            }
            int w = g.guiWidth();
            int h = g.guiHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            if (kind < 0) {
                if (nextAt == 0L) {
                    nextAt = now + gap(now, it);
                } else if (now >= nextAt) {
                    begin(now, it);
                }
                return; // nothing on screen between episodes, by design
            }
            if (now >= endAt) {
                kind = -1;
                nextAt = now + gap(now, it);
                return;
            }
            float t = (now - startAt) / (float) Math.max(1L, endAt - startAt);
            switch (kind) {
                case TEAR:
                    tear(g, w, h, it, t, now);
                    break;
                case FIGURE:
                    figure(g, w, h, it, t, now);
                    break;
                case FALSE_SKY:
                    falseSky(g, w, h, it, t, now);
                    break;
                default:
                    whisper(g, mc, w, h, it, t, now);
                    break;
            }
        } catch (Throwable ignored) {
            // a hallucination may never cost a frame
        }
    }

    private static void begin(long now, float it) {
        seed = now * 0x9E3779B97F4A7C15L ^ (long) (it * 1000.0F);
        Random rng = new Random(seed);
        kind = rng.nextInt(KINDS);
        startAt = now;
        long span = TEAR_MS_MIN + (long) (rng.nextDouble() * (TEAR_MS_MAX - TEAR_MS_MIN) * (0.6D + it));
        endAt = now + span;
    }

    /** How long the world behaves after an episode: rare when faint, rarer still when calm. */
    private static long gap(long now, float it) {
        Random rng = new Random(now ^ 0x5DEECE66DL);
        long span = GAP_MS_MAX - (long) ((GAP_MS_MAX - GAP_MS_MIN) * Mth.clamp(it, 0.0F, 1.0F));
        return (long) (span * (0.55D + rng.nextDouble() * 0.9D));
    }

    /** The frame comes apart in bands. */
    private static void tear(GuiGraphicsExtractor g, int w, int h, float it, float t, long now) {
        Random rng = new Random(seed ^ 0x7F4A7C15L);
        int bands = 3 + (int) (it * 6.0F);
        float fade = (float) Math.sin(Math.PI * Mth.clamp(t, 0.0F, 1.0F));
        int a = (int) (216.0F * fade * (0.45F + 0.55F * it));
        if (a <= 3) {
            return;
        }
        float[] sky = McsmStormPhase.columnFor(Math.max(McsmStormPhase.phase(), McsmStormPhase.PHASE_MIN), 0.35F);
        int r = sky == null ? 42 : (int) (sky[0] * 255.0F);
        int gg = sky == null ? 24 : (int) (sky[1] * 255.0F);
        int b = sky == null ? 68 : (int) (sky[2] * 255.0F);
        int body = (a << 24) | (r << 16) | (gg << 8) | b;
        for (int i = 0; i < bands; i++) {
            int y = rng.nextInt(Math.max(1, h));
            int hgt = 2 + rng.nextInt(5);
            g.fill(0, y, w, Math.min(h, y + hgt), body);
            // the chroma split: one pixel of each channel, pulled apart
            int edge = (Math.min(150, a) << 24);
            g.fill(0, Math.max(0, y - 1), w, y, edge | 0x00FF3CA8);
            g.fill(0, Math.min(h, y + hgt), w, Math.min(h, y + hgt + 1), edge | 0x003CFFA0);
            // and the band's own slice of the frame, displaced: a strip of the phase
            // colour where the sky should not be
            int sx = rng.nextInt(Math.max(1, w));
            int sw = 24 + rng.nextInt(Math.max(1, w / 3));
            g.fill(sx, y, Math.min(w, sx + sw), Math.min(h, y + hgt + 2), edge | (VIOLET & 0x00FFFFFF));
        }
    }

    /** Something is standing at the edge of vision, and it does not move like a mob. */
    private static void figure(GuiGraphicsExtractor g, int w, int h, float it, float t, long now) {
        Random rng = new Random(seed ^ 0x2545F491L);
        boolean left = rng.nextBoolean();
        // it drifts a little, because it is not standing still either
        double sway = Math.sin(now * 0.0021D) * (6.0D + 10.0D * it);
        int cx = (int) ((left ? w * 0.14D : w * 0.86D) + sway);
        int bodyH = (int) (h * (0.52D + 0.14D * it));
        int top = h - bodyH;
        int halfW = (int) (8.0D + 10.0D * it);
        float fade = (float) Math.sin(Math.PI * Mth.clamp(t, 0.0F, 1.0F));
        int a = (int) (198.0F * fade);
        if (a <= 3) {
            return;
        }
        // the aura first: the air around it is wrong
        g.fill(cx - halfW * 3, top - 10, cx + halfW * 3, h, (Math.min(46, a / 4) << 24) | 0x002A0F4A);
        // the shape: too tall, and darker than anything that is really there
        g.fill(cx - halfW, top, cx + halfW, h, (a << 24) | 0x00050308);
        // the lenses: two, violet, and they do not blink
        int eyeY = top + bodyH / 9;
        int eyeR = Math.max(1, halfW / 3);
        g.fill(cx - eyeR - eyeR, eyeY, cx - eyeR + eyeR, eyeY + eyeR * 2, (a << 24) | (VIOLET & 0x00FFFFFF));
        g.fill(cx + eyeR - eyeR, eyeY, cx + eyeR + eyeR, eyeY + eyeR * 2, (a << 24) | (VIOLET & 0x00FFFFFF));
    }

    /** A sky that is not this sky, with lightning that never lands. */
    private static void falseSky(GuiGraphicsExtractor g, int w, int h, float it, float t, long now) {
        float fade = (float) Math.sin(Math.PI * Mth.clamp(t, 0.0F, 1.0F));
        float[] sky = McsmStormPhase.columnFor(Math.max(McsmStormPhase.phase(), McsmStormPhase.PHASE_MIN), 0.55F);
        int r = sky == null ? 90 : (int) (sky[0] * 255.0F);
        int gg = sky == null ? 40 : (int) (sky[1] * 255.0F);
        int b = sky == null ? 130 : (int) (sky[2] * 255.0F);
        int a = (int) (120.0F * fade * (0.4F + 0.6F * it));
        if (a > 3) {
            g.fillGradient(0, 0, w, h, (a << 24) | (r << 16) | (gg << 8) | b, 0x00000000);
        }
        // two strikes: short, bright, and nothing underneath them flinches
        Random rng = new Random(seed ^ 0x1B873593L);
        for (int i = 0; i < 2; i++) {
            long at = startAt + (long) (rng.nextDouble() * Math.max(1L, endAt - startAt));
            long since = now - at;
            if (since < 0L || since > 150L) {
                continue;
            }
            int flash = (int) (150.0F * (1.0F - since / 150.0F) * (0.5F + 0.5F * it));
            g.fill(0, 0, w, h, (flash << 24) | 0x00E9DFFF);
        }
    }

    /** One line of the lore, low on the screen, half-there. */
    private static void whisper(GuiGraphicsExtractor g, Minecraft mc, int w, int h, float it,
            float t, long now) {
        Random rng = new Random(seed ^ 0x27D4EB2FL);
        String line = WHISPERS[rng.nextInt(WHISPERS.length)];
        // flicker: it is only sometimes there, and never steadily
        long beat = now / 90L;
        if (((beat * 0x9E3779B97F4A7C15L) >>> 60) > (5L + (long) (it * 8.0F))) {
            return;
        }
        float fade = (float) Math.sin(Math.PI * Mth.clamp(t, 0.0F, 1.0F));
        int width = mc.font.width(line);
        int x = (w - width) / 2;
        int y = h - (int) (h * 0.22D);
        g.fill(x - 6, y - 3, x + width + 6, y + 11, ((int) (110 * fade) << 24) | 0x00060308);
        g.text(mc.font, line, x, y, ((int) (200 * fade) << 24) | 0x00CBA6FF, true);
    }

    /** For /ds: what the mod thinks the world is doing to the player right now. */
    public static String state() {
        return "hallucinations: intensity " + String.format("%.2f", lastIntensity)
                + (kind < 0 ? " (idle)" : " (kind " + kind + ")")
                + " \u00b7 reality glitches " + (McsmExtrasConfig.realityGlitches ? "ON" : "OFF")
                + ", setting " + McsmExtrasConfig.hallucinationIntensity;
    }
}
