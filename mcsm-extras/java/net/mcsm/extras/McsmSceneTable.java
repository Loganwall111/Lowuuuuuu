package net.mcsm.extras;

import java.util.ArrayList;
import java.util.List;

/**
 * BUILD #447 -- THE SCENE TABLE.
 *
 * <p>"Cutscenes beyond the boot sequence" off the wishlist. The boot sequence is
 * the one the game already had; these are the ones that happen IN the world, and
 * this class is their catalogue -- what they are called, what they say, how long
 * they run and what sets them off.
 *
 * <p>IT IS PLAIN COMMON CODE ON PURPOSE. The presentation lives in
 * {@code net.mcsm.extras.client.McsmScenes}, which cannot exist on a dedicated
 * server (it draws); the catalogue is here so that the server's {@code /ds scene}
 * command can list the same scenes the client plays, from the same strings, without
 * the server ever loading a client class.
 *
 * <p>Eight scenes, and every one of them is a moment the world already has: the
 * first district you walk into, the first mazes and racks, the rift, adams, the
 * thing that gets summoned, the thing that made all of it, and the phase where the
 * sky turns to amethyst.
 */
public final class McsmSceneTable {

    /**
     * One scene: an id, a title card, the lines it speaks, how long it runs, what
     * sets it off (in words, for the command and for the gate) and its flavour.
     */
    public record Scene(String id, String title, String[] lines, long ms, String trigger,
                        String flavour) {
    }

    private McsmSceneTable() {
    }

    private static final List<Scene> SCENES = List.of(
        new Scene("first_district", "THE DEAD CITIES WERE NOT EMPTY",
                new String[]{
                    "You are standing in one of the districts.",
                    "Nobody has lived here for a very long time,",
                    "and every window in the place is lit.",
                }, 5200L, "the first time a ruined district is within 96 blocks", "still"),

        new Scene("first_maze", "SOMETHING IS UNDER THE STREETS",
                new String[]{
                    "The districts have warehouses under them.",
                    "The hatch is on the surface, and it is marked.",
                    "The far corners are the reason to walk to them.",
                }, 5200L, "the first time a storage maze is within 96 blocks", "still"),

        new Scene("first_racks", "A MACHINE THAT STILL HAS POWER",
                new String[]{
                    "Three halls of rusted plate, and the racks blink.",
                    "Every three minutes the room drops to emergency",
                    "lighting, and something comes back up.",
                }, 5200L, "the first time a server room is within 96 blocks", "still"),

        new Scene("the_rift", "YOU ARE NOT IN THE SAME WORLD NOW",
                new String[]{
                    "This is the decayed reality. It is a place, not a",
                    "teleport: its own stone, its own cities, its own sky.",
                    "Sneak on the other side to come back.",
                }, 5600L, "the first time you stand in the decayed reality", "violet"),

        new Scene("adams_gate", "THE INFINITE DIMENSION OF ADAMS",
                new String[]{
                    "A grid of walkways with pillars between them, lit by",
                    "a spine of lamps, chamber after chamber.",
                    "It does not end, and two chambers in three hold something.",
                }, 5600L, "the first time you stand in adams", "teal"),

        new Scene("the_waking", "IT WAS ALWAYS A DOOR",
                new String[]{
                    "The rite called it up and the world answered.",
                    "Twenty-four blocks one way, forty the other.",
                    "Four thousand and ninety-six lives. It cannot be undone.",
                }, 7000L, "the first time the summoned thing is within 128 blocks", "shake"),

        new Scene("the_creator", "SOMETHING MADE ALL OF THIS",
                new String[]{
                    "Ninety-six blocks across. One hundred and fifty tall.",
                    "Forty thousand lives, which is more than the swarm",
                    "and less than the world.",
                }, 7000L, "the first time the creator is within 192 blocks", "shake"),

        new Scene("phase_six", "THE SKY TURNS TO AMETHYST",
                new String[]{
                    "The storm has reached the sixth band.",
                    "The teal is behind you, the violet is overhead,",
                    "and the horizon is still the brightest thing in the world.",
                }, 6200L, "the storm's own phase reaching 6.0", "violet"));

    public static List<Scene> all() {
        return SCENES;
    }

    public static Scene byId(String id) {
        if (id == null) {
            return null;
        }
        for (Scene scene : SCENES) {
            if (scene.id().equalsIgnoreCase(id)) {
                return scene;
            }
        }
        return null;
    }

    public static List<String> ids() {
        List<String> out = new ArrayList<>();
        for (Scene scene : SCENES) {
            out.add(scene.id());
        }
        return out;
    }

    /** "12 / 16 scenes, 5200 ms" -- the one-line summary the command prints. */
    public static String summary() {
        Scene longest = SCENES.get(0);
        for (Scene scene : SCENES) {
            if (scene.ms() > longest.ms()) {
                longest = scene;
            }
        }
        return SCENES.size() + " scenes, longest " + (longest.ms() / 1000L) + "s ("
                + longest.id() + ")";
    }
}
