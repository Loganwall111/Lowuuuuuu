package net.mcsm.extras.client;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BUILD #473 -- WHO IS PAINTING THE BLACK MENU, ANSWERED FROM INSIDE THE GAME.
 *
 * <p>The standing report is "it's still black ... the Mojang logo loads ... and then just
 * completely black", and every attempt to fix it from this side has been guesswork about a
 * screen this mod does not own. Read out of the 26.2 client's own bytecode (the build's
 * api dump, {@code Screen.extractRenderStateWithTooltipAndSubtitles}), the title's frame is:
 *
 * <pre>
 *   Screen.extractRenderStateWithTooltipAndSubtitles
 *     -&gt; extractBackground(...)          &lt;- the mod's injections land here
 *     -&gt; extractRenderState(...)         &lt;- TitleScreen's own, plus every widget
 * </pre>
 *
 * and {@code Screen.extractBackground} draws its backdrop from a TEXTURE --
 * {@code extractMenuBackground} -&gt; {@code extractMenuBackgroundTexture(MENU_BACKGROUND)}
 * = {@code minecraft:textures/gui/menu_background.png}. A resource pack that replaces that
 * one texture with a dark, blank or broken image turns the menu black while the logo and
 * the buttons (drawn from other textures) still come up. Nothing in this jar can override a
 * resource pack: mod resources sit BELOW the player's packs in the stack. So the only
 * honest thing the mod can do about it is SAY WHERE THE PICTURE CAME FROM.
 *
 * <p>The same is true of FancyMenu: when it owns the title screen, the menu is drawn from
 * {@code config/fancymenu/customization/title_screen_layout.txt} and the images beside it.
 * Those files are stored in Git LFS in this project's own repository, i.e. an instance that
 * was set up without {@code git lfs pull} has a 130-byte TEXT POINTER where the layout and
 * every image should be. FancyMenu then has no layout to draw and no images to load, which
 * is exactly "a failed thing with a very very very dark failed button on the side, and then
 * just completely black". This class reports that too, per file.
 *
 * <p>Every probe is wrapped: a diagnostic that throws is worse than no diagnostic. Nothing
 * here paints, allocates on a hot path, or touches a screen.
 */
public final class McsmMenuDiag {

    /** FancyMenu's main class, if the pack runs it. */
    private static final String[] FANCYMENU_CLASSES = {
        "de.keksuccino.fancymenu.FancyMenu",
        "de.keksuccino.fancymenu.v2.FancyMenu",
    };

    /** The layout FancyMenu draws the title screen from, and the images beside it. */
    private static final String FM_LAYOUT = "config/fancymenu/customization/title_screen_layout.txt";
    private static final String FM_ASSETS = "config/fancymenu/assets";

    /** The picture the game's own menu backdrop is made of, and the panorama behind it. */
    private static final String[] BACKDROPS = {
        "textures/gui/menu_background.png",
        "textures/gui/title/background/panorama_0.png",
    };

    private McsmMenuDiag() {
    }

    /** One report line per probe, ready for /ds menu. Never null, never throws. */
    public static List<String> report() {
        List<String> out = new ArrayList<String>();
        try {
            out.add(guardLine());
        } catch (Throwable ignored) {
            out.add("menu render: unreadable");
        }
        try {
            for (String line : fancyMenuLines()) {
                out.add(line);
            }
        } catch (Throwable ignored) {
            out.add("fancymenu: unreadable");
        }
        try {
            for (String name : BACKDROPS) {
                out.add(backdropLine(name));
            }
        } catch (Throwable ignored) {
            out.add("backdrops: unreadable");
        }
        return out;
    }

    private static String guardLine() {
        return McsmMenuGuard.state();
    }

    /**
     * FancyMenu, and whether the files it needs are really files. The LFS pointer check is
     * the point: a pointer is 130 bytes of text that reads
     * {@code version https://git-lfs.github.com/spec/v1}, and an instance full of them
     * cannot draw a customized title.
     */
    private static List<String> fancyMenuLines() {
        List<String> out = new ArrayList<String>();
        boolean present = false;
        for (String cls : FANCYMENU_CLASSES) {
            try {
                Class.forName(cls, false, McsmMenuDiag.class.getClassLoader());
                present = true;
                break;
            } catch (Throwable ignored) {
                // not this one
            }
        }
        if (!present) {
            out.add("fancymenu: not installed (the title is the game's own + this mod's)");
            return out;
        }
        File layout = new File(gameDir(), FM_LAYOUT);
        out.add("fancymenu: installed \u00b7 title layout " + describe(layout));
        File assets = new File(gameDir(), FM_ASSETS);
        int total = 0;
        int pointers = 0;
        try {
            File[] files = assets.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.isFile() || total >= 256) {
                        continue;
                    }
                    total++;
                    if (isLfsPointer(f)) {
                        pointers++;
                    }
                }
            }
        } catch (Throwable ignored) {
            // listed nothing
        }
        if (total == 0) {
            out.add("fancymenu assets: none in " + FM_ASSETS);
        } else if (pointers == total) {
            out.add("fancymenu assets: ALL " + total + " are Git LFS POINTERS, not images -- "
                    + "this instance was set up without `git lfs pull`, so every element "
                    + "FancyMenu draws is a failed placeholder (that dark box on the side)");
        } else if (pointers > 0) {
            out.add("fancymenu assets: " + pointers + " of " + total
                    + " are Git LFS pointers, not images");
        } else {
            out.add("fancymenu assets: " + total + " files, all real content");
        }
        return out;
    }

    /**
     * Which pack the menu backdrop comes from, how big it is, and how bright its pixels
     * are -- the mean luminance is what "completely black" means in numbers.
     */
    private static String backdropLine(String path) {
        try {
            Object mc = net.minecraft.client.Minecraft.getInstance();
            Object rm = mc == null ? null : mc.getClass().getMethod("getResourceManager").invoke(mc);
            if (rm == null) {
                return path + ": no resource manager yet";
            }
            Object id = net.minecraft.resources.Identifier.withDefaultNamespace(path);
            Optional<?> found = (Optional<?>) rm.getClass()
                    .getMethod("getResource", net.minecraft.resources.Identifier.class)
                    .invoke(rm, id);
            if (found == null || !found.isPresent()) {
                return path + ": MISSING -- the game will draw nothing where it should "
                        + "be (a black frame)";
            }
            Object res = found.get();
            String pack = "?";
            try {
                pack = String.valueOf(res.getClass().getMethod("sourcePackId").invoke(res));
            } catch (Throwable ignored) {
                // older/newer API: the size below still says a lot
            }
            long size = -1L;
            int mean = -1;
            try {
                InputStream in = (InputStream) res.getClass().getMethod("open").invoke(res);
                java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int n;
                while ((n = in.read(chunk)) > 0) {
                    buf.write(chunk, 0, n);
                }
                in.close();
                size = buf.size();
                byte[] all = buf.toByteArray();
                if (all.length > 0 && all[0] == 'v' && new String(all, 0, Math.min(60, all.length))
                        .contains("git-lfs.github.com")) {
                    return path + ": pack '" + pack + "' -- THIS IS A GIT LFS POINTER "
                            + "(a text file, not an image): nothing can draw it";
                }
                mean = meanLuminance(all);
            } catch (Throwable ignored) {
                // size/mean stay unknown
            }
            String verdict = "";
            if (mean >= 0) {
                verdict = mean <= 12 ? " \u00b7 MEAN LUMINANCE " + mean + "/255: this IS the "
                        + "black screen" : " \u00b7 mean luminance " + mean + "/255";
            }
            return path + ": pack '" + pack + "' \u00b7 " + size + " bytes" + verdict;
        } catch (Throwable t) {
            // the message is part of the answer: a wrong method name on this build's
            // ResourceManager should read as a wrong method name, not as "black".
            return path + ": unreadable (" + t.getClass().getSimpleName()
                    + (t.getMessage() == null ? "" : ": " + t.getMessage()) + ")";
        }
    }

    /**
     * Mean luminance of the image, via AWT -- the client JVM has java.desktop, and the
     * result is only ever printed. -1 when the image cannot be decoded (a pointer file,
     * a broken PNG, a format AWT does not know).
     */
    private static int meanLuminance(byte[] png) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(
                    new java.io.ByteArrayInputStream(png));
            if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
                return -1;
            }
            long sum = 0L;
            long count = 0L;
            for (int y = 0; y < img.getHeight(); y += 4) {
                for (int x = 0; x < img.getWidth(); x += 4) {
                    int rgb = img.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    sum += (r * 30 + g * 59 + b * 11) / 100;
                    count++;
                }
            }
            return count == 0L ? -1 : (int) (sum / count);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static String describe(File f) {
        try {
            if (!f.exists()) {
                return "MISSING (" + FM_LAYOUT + ")";
            }
            return (isLfsPointer(f) ? "A GIT LFS POINTER, not a layout" : f.length() + " bytes");
        } catch (Throwable ignored) {
            return "unreadable";
        }
    }

    /** A 130-byte text file that starts with the LFS spec URL is not content. */
    private static boolean isLfsPointer(File f) {
        try {
            if (f.length() > 512L) {
                return false;
            }
            InputStream in = new java.io.FileInputStream(f);
            try {
                byte[] head = new byte[64];
                int n = in.read(head);
                if (n <= 0) {
                    return false;
                }
                String text = new String(head, 0, n, "UTF-8");
                return text.contains("git-lfs.github.com/spec");
            } finally {
                in.close();
            }
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static File gameDir() {
        try {
            // typed, because this one is proven: Minecraft.gameDirectory is public and
            // McsmGate has read it the same way since the config work.
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                return mc.gameDirectory;
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return new File(".");
    }
}
