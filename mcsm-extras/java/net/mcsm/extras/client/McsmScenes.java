package net.mcsm.extras.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.mcsm.extras.McsmAdams;
import net.mcsm.extras.McsmCities;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmMazes;
import net.mcsm.extras.McsmReality;
import net.mcsm.extras.McsmSceneTable;
import net.mcsm.extras.McsmServerRooms;
import net.mcsm.extras.entity.McsmBeast;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

/**
 * BUILD #447 -- CUTSCENES BEYOND THE BOOT SEQUENCE.
 *
 * <p>The boot sequence is the one the game already had: the logo scene, the burst,
 * the cracks. {"The wishlist"} asked for cutscenes PAST it -- moments in the world
 * that get their own frame -- and that is what this is: eight of them, each set off
 * by something the player actually does, each with its own title card, its own lines
 * and its own treatment.
 *
 * <p>HOW A SCENE IS SHOWN, given what this build may and may not assume:
 *
 * <ul>
 *   <li><b>BARS, TITLE, LINES.</b> Letterbox bars slide in and out, the title card
 *       fades up, and the lines type themselves on at the bottom. Drawn on the same
 *       proven per-frame HUD hook as the boot sequence and the ending, in
 *       {@code McsmHudAttachMixin}, so there is no new render path and no new API.</li>
 *   <li><b>THE CAMERA TURNS THE PLAYER, REFLECTIVELY.</b> There is no camera mixin
 *       here on purpose: the two setters this needs ({@code setYRot}, {@code setXRot},
 *       with {@code setYHeadRot} for the head) are called through reflection, so a
 *       renamed method degrades to "the bars play and the camera does not move"
 *       instead of failing a build on the runner. The player's own angles are saved
 *       before the scene and put back after it, including the interpolation fields,
 *       so a scene never leaves somebody facing the wrong way.</li>
 *   <li><b>ANY KEY SKIPS.</b> The keyboard is polled with {@link McsmKeyboard}, the
 *       same reader the terminal and the book use.</li>
 *   <li><b>ONCE.</b> A scene plays once per session, the first time its trigger
 *       fires, and never while a screen is open (so it cannot fight the config).</li>
 * </ul>
 *
 * <p>Triggers are facts the client can read on its own: the deterministic grids of
 * the districts, mazes and server rooms, the two dimensions that are places, the
 * storm's own phase, and an entity scan for the two things big enough to deserve a
 * frame of their own.
 */
public final class McsmScenes {

    private static final Set<String> PLAYED = ConcurrentHashMap.newKeySet();

    private static int current = -1;
    private static long startMs;
    private static float[] restore;
    private static float aimFrom;
    private static float aimTo;

    private McsmScenes() {
    }

    // ---------------------------------------------------------------------
    // Clock
    // ---------------------------------------------------------------------

    /** Called once per client tick, from the terminal's own proven registration. */
    public static void tick() {
        try {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc == null ? null : mc.player;
            if (player == null || mc.level == null) {
                if (current >= 0) {
                    finish(mc);
                }
                return;
            }
            if (current >= 0) {
                if (System.currentTimeMillis() - startMs > McsmSceneTable.all().get(current).ms()
                        || skipRequested()) {
                    finish(mc);
                }
                return;
            }
            // BUILD #449 -- Minecraft has no `screen` field in this API (run 528). The
            // proven read is the one the terminal already uses: mc.gui.screen().
            if (!McsmExtrasConfig.cutscenes || McsmTerminalClient.currentScreen(mc) != null) {
                return;
            }
            // one scene at a time, in catalogue order, each once
            for (int i = 0; i < McsmSceneTable.all().size(); i++) {
                McsmSceneTable.Scene scene = McsmSceneTable.all().get(i);
                if (PLAYED.contains(scene.id()) || !fires(scene.id(), player)) {
                    continue;
                }
                begin(mc, player, i);
                return;
            }
        } catch (Throwable t) {
            // a scene that cannot play is not a reason to break a tick
            current = -1;
        }
    }

    /** True when this scene's own trigger is satisfied right now. */
    private static boolean fires(String id, LocalPlayer player) {
        double x = player.getX();
        double z = player.getZ();
        switch (id) {
            case "first_district": {
                // NOTE: McsmCities.nearestCity answers {dx, dz, distance} while the
                // maze and server-room probes answer {x, z, distanceSquared}. The
                // two shapes are different on purpose in those systems; this is the
                // one place both are read, so both are read correctly.
                int[] near = McsmCities.nearestCity((int) Math.floor(x), (int) Math.floor(z));
                return near != null && near[2] < 96;
            }
            case "first_maze": {
                int[] near = McsmMazes.nearestMaze((int) Math.floor(x), (int) Math.floor(z));
                return near != null && near[2] < 96 * 96;
            }
            case "first_racks": {
                int[] near = McsmServerRooms.nearestRoom((int) Math.floor(x), (int) Math.floor(z));
                return near != null && near[2] < 96 * 96;
            }
            case "the_rift":
                return player.level().dimension().equals(McsmReality.DECAYED_REALITY);
            case "adams_gate":
                return player.level().dimension().equals(McsmAdams.ADAMS);
            case "the_waking":
                return beastNear(player, McsmBeast.MAS, 128.0D);
            case "the_creator":
                return beastNear(player, McsmBeast.CREATOR, 192.0D);
            case "phase_six":
                return McsmStormPhase.active() && McsmStormPhase.phase() >= 6.0F;
            default:
                return false;
        }
    }

    /** The entity scan the HUD already proves: the classes and the bounding box. */
    private static boolean beastNear(LocalPlayer player, String kind, double range) {
        for (McsmBeast beast : player.level().getEntitiesOfClass(McsmBeast.class,
                player.getBoundingBox().inflate(range))) {
            if (kind.equals(beast.kind())) {
                return true;
            }
        }
        return false;
    }

    private static void begin(Minecraft mc, LocalPlayer player, int index) {
        McsmSceneTable.Scene scene = McsmSceneTable.all().get(index);
        current = index;
        startMs = System.currentTimeMillis();
        PLAYED.add(scene.id());
        aimFrom = player.getYRot();
        aimTo = aimFrom + 32.0F;
        restore = new float[]{player.getYRot(), player.getXRot()};
        if (scene.id().equals("the_rift") || scene.id().equals("adams_gate")) {
            aimTo = aimFrom + 18.0F;
        } else if (scene.id().equals("the_waking") || scene.id().equals("the_creator")) {
            aimTo = aimFrom + 46.0F;
        }
        System.out.println("[ds] scene: " + scene.id());
    }

    private static void finish(Minecraft mc) {
        current = -1;
        if (mc != null && mc.player != null && restore != null) {
            aim(mc.player, restore[0], restore[1]);
        }
        restore = null;
    }

    /** The catalogue's own one-liner, for the command and the panel. */
    public static String status() {
        if (current >= 0) {
            return "playing \"" + McsmSceneTable.all().get(current).title() + "\"";
        }
        return PLAYED.size() + " of " + McsmSceneTable.all().size() + " scenes played this session";
    }

    /** The first scene this session has not played, or null when all of them have. */
    public static String nextUnplayed() {
        for (McsmSceneTable.Scene scene : McsmSceneTable.all()) {
            if (!PLAYED.contains(scene.id())) {
                return scene.id();
            }
        }
        return null;
    }

    /** True once every scene in the catalogue has had its turn this session. */
    public static boolean allPlayed() {
        return nextUnplayed() == null;
    }

    /** Skill-free replay, for the command path and for testing. */
    public static boolean play(String id) {
        McsmSceneTable.Scene scene = McsmSceneTable.byId(id);
        if (scene == null) {
            return false;
        }
        for (int i = 0; i < McsmSceneTable.all().size(); i++) {
            if (McsmSceneTable.all().get(i) == scene) {
                Minecraft mc = Minecraft.getInstance();
                if (mc == null || mc.player == null) {
                    return false;
                }
                PLAYED.add(scene.id());
                begin(mc, mc.player, i);
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // The frame
    // ---------------------------------------------------------------------

    /** Drawn every frame, over the world, on the HUD's own hook. */
    public static void draw(GuiGraphicsExtractor g, DeltaTracker delta) {
        if (current < 0) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.font == null) {
                return;
            }
            McsmSceneTable.Scene scene = McsmSceneTable.all().get(current);
            long elapsed = System.currentTimeMillis() - startMs;
            if (elapsed < 0L || elapsed > scene.ms()) {
                return;
            }
            int w = g.guiWidth();
            int h = g.guiHeight();
            double t = (double) elapsed / (double) scene.ms();

            // the camera: a slow pan one way, which is put back when the scene ends
            if (mc.player != null) {
                float yaw = aimFrom + (aimTo - aimFrom) * (float) ease(t);
                float shake = scene.flavour().equals("shake")
                        ? (float) Math.sin(elapsed * 0.09D) * 0.35F : 0.0F;
                float pitch = mc.player.getXRot() + shake;
                aim(mc.player, yaw, pitch);
            }

            // the bars
            int bar = (int) (h * 0.12D);
            int shown = (int) (bar * (t < 0.15D ? t / 0.15D : (t > 0.85D ? (1.0D - t) / 0.15D : 1.0D)));
            if (shown > 0) {
                g.fill(0, 0, w, shown, 0xFF000000);
                g.fill(0, h - shown, w, h, 0xFF000000);
                g.fill(0, shown, w, shown + 1, 0xFF6D3FD4);
                g.fill(0, h - shown - 1, w, h - shown, 0xFF6D3FD4);
            }

            // the wash, per scene
            if (t > 0.1D && t < 0.9D) {
                int wash = washFor(scene.flavour());
                if (wash != 0) {
                    g.fill(0, shown, w, h - shown, wash);
                }
            }

            // the title card
            if (t > 0.08D) {
                int alpha = (int) (255 * Math.min(1.0D, (t - 0.08D) / 0.12D));
                g.centeredText(mc.font, scene.title(), w / 2, shown + 26,
                        0xFF000000 | (alpha << 16) | 0x6BFF);
            }

            // the lines, typed on
            int lines = scene.lines().length;
            int typed = (int) (lines * Math.clamp((t - 0.2D) / 0.5D, 0.0D, 1.0D));
            int y = h - shown - 14 - 24;
            for (int i = 0; i < typed && i < lines; i++) {
                int colour = i == lines - 1 ? 0xFFE3C77A : 0xFFDCCFF5;
                g.centeredText(mc.font, scene.lines()[i], w / 2,
                        y + i * 12, colour);
            }
            g.centeredText(mc.font, "space skips", w / 2, h - shown + 10,
                    0xFF7C6CA0);
        } catch (Throwable t) {
            // never break a frame over a scene
        }
    }

    private static int washFor(String flavour) {
        switch (flavour) {
            case "violet":
                return 0x1C8A5CFF;
            case "teal":
                return 0x1839E0FF;
            case "shake":
                return 0x12D86BFF;
            default:
                return 0;
        }
    }

    private static double ease(double t) {
        return t < 0.5D ? 2.0D * t * t : 1.0D - Math.pow(-2.0D * t + 2.0D, 2.0D) / 2.0D;
    }

    /**
     * Whether the player asked to skip, polled the way the terminal and the book
     * poll their keys.
     *
     * <p>DELIBERATELY NOT "ANY KEY". The first draft skipped on any key at all,
     * which is a trap: a scene fires when somebody WALKS somewhere, so the key
     * they were already tapping -- W -- is the one key they are most likely to tap
     * again in the second the scene starts, and it would skip the scene it just
     * triggered. Only the three keys that mean "I am done here" count.
     */
    public static boolean skipRequested() {
        for (Integer key : McsmKeyboard.poll()) {
            int k = key.intValue();
            if (k == McsmKeyboard.SPACE || k == McsmKeyboard.ENTER || k == McsmKeyboard.ESCAPE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Point somebody's head somewhere, reflectively.
     *
     * <p>Reflection is the point: {@code setYRot}/{@code setXRot} are the names every
     * version of this class has kept, but the build this machine cannot compile
     * against is the one that matters, and a missing setter must cost the camera pan
     * rather than the whole build. The interpolation fields are set alongside the
     * angles so the pan does not tear.
     */
    private static void aim(LocalPlayer player, float yaw, float pitch) {
        call(player, "setYRot", yaw);
        call(player, "setXRot", pitch);
        call(player, "setYHeadRot", yaw);
        field(player, "yRotO", yaw);
        field(player, "xRotO", pitch);
    }

    private static void call(Object target, String name, float value) {
        try {
            target.getClass().getMethod(name, float.class).invoke(target, Float.valueOf(value));
        } catch (Throwable ignored) {
            // a setter this jar does not have: the scene simply does not turn
        }
    }

    private static void field(Object target, String name, float value) {
        try {
            java.lang.reflect.Field f = target.getClass().getField(name);
            f.setFloat(target, value);
        } catch (Throwable ignored) {
            // likewise
        }
    }
}
