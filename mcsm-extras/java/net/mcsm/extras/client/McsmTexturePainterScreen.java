package net.mcsm.extras.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.mcsm.extras.client.McsmClientChat;

/**
 * Build #374 -- the IN-GAME STORM TEXTURE PAINTER (user order: "an in-game
 * storm texture painter (so users can make their own textures)").
 *
 * A 16x16 canvas — the classic Wither Storm skin grid — with a 20-colour
 * palette, left-click/drag to paint, right-click/drag to erase (deep black),
 * and a Save that writes a real, working resource pack:
 *
 *   <game>/resourcepacks/DevouringStormsCustomStorm/
 *     pack.mcmeta                                        (pack_format 88)
 *     assets/dabywitherstormmod/textures/entity/wither_storm/wither_storm.png
 *                                                         (16x16 -> 160x160,
 *                                                          nearest-neighbour,
 *                                                          matching the sheet
 *                                                          the body UVs sample)
 *
 * Telling the user exactly what to do (one click in the pack screen) beats
 * trying to hot-swap a texture mid-session. All rendering is the proven
 * fill/text panel style of the Story Mode Console; the screen is fully
 * self-handled (no child widgets).
 */
public final class McsmTexturePainterScreen extends Screen {

    private static final int GRID = 16;
    private static final int CELL = 24;
    private static final int DEEP_BLACK = 0xFF050508;

    /** 20-colour palette: blacks, whites, the storm's emissive set, the rest. */
    private static final int[] PALETTE = {
            0xFF050508, 0xFF1A1A20, 0xFF4A4A55, 0xFFB8BDC9, 0xFFF4F6FA,
            0xFF5A0A0A, 0xFFB22222, 0xFFE8542E, 0xFFE8C07A, 0xFFF4EAD0,
            0xFF2E7A1E, 0xFF6FBF3A, 0xFF2E8A8A, 0xFF3ABFC4, 0xFF2E4A8A,
            0xFF4A6AD8, 0xFF5A2E8A, 0xFF8A3ABF, 0xFFBF3A8A, 0xFF7A4A1E
    };

    private final int[] pixels = new int[GRID * GRID];
    private final Screen parent;
    private int paletteIndex = 7;
    private int button = -1;          // -1 none, 0 paint, 1 erase (while dragging)
    private int lastCell = -1;

    private int canvasX() { return 18; }
    private int canvasY() { return 48; }
    private int paletteX() { return canvasX() + GRID * CELL + 44; }
    private int paletteY() { return 64; }
    private int btnX() { return 18; }
    private int btnY() { return Math.max(canvasY() + GRID * CELL + 18, this.height - 56); }

    public McsmTexturePainterScreen(Screen parent) {
        super(Component.literal("Devouring Storms — Custom Storm Texture Painter"));
        this.parent = parent;
        Arrays.fill(pixels, DEEP_BLACK);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int w = this.width, h = this.height;
        // room
        g.fill(0, 0, w, h, 0xF807080C);
        g.fillGradient(0, 0, w, 80, 0x660D1016, 0x000D1016);

        Font font = Minecraft.getInstance().font;
        g.text(font, "Custom Storm Texture Painter", 18, 14, 0xFFE8C07A, true);
        g.text(font, "Left click / drag: paint   ·   Right click / drag: erase   ·   Pick a colour, paint, then Save.",
                18, 30, 0xFF8A90A0, true);

        // canvas
        int cx = canvasX(), cy = canvasY();
        g.fill(cx - 4, cy - 4, cx + GRID * CELL + 4, cy + GRID * CELL + 4, 0xFF12151C);
        g.fill(cx - 1, cy - 1, cx + GRID * CELL + 1, cy + GRID * CELL + 1, 0xFF2A3140);
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                int px = pixels[y * GRID + x];
                g.fill(cx + x * CELL, cy + y * CELL, cx + x * CELL + CELL, cy + y * CELL + CELL, px);
            }
        }
        // grid lines
        for (int i = 0; i <= GRID; i++) {
            g.fill(cx + i * CELL, cy, cx + i * CELL + 1, cy + GRID * CELL, 0x441A1E26);
            g.fill(cx, cy + i * CELL, cx + GRID * CELL, cy + i * CELL + 1, 0x441A1E26);
        }
        // hover cell
        int hc = cellAt(mouseX, mouseY);
        if (hc >= 0) {
            int hx = cx + (hc % GRID) * CELL, hy = cy + (hc / GRID) * CELL;
            g.fill(hx, hy, hx + CELL, hy + 1, 0xFFE8C07A);
            g.fill(hx, hy + CELL - 1, hx + CELL, hy + CELL, 0xFFE8C07A);
            g.fill(hx, hy, hx + 1, hy + CELL, 0xFFE8C07A);
            g.fill(hx + CELL - 1, hy, hx + CELL, hy + CELL, 0xFFE8C07A);
        }

        // palette
        int px = paletteX(), py = paletteY();
        g.text(font, "Palette", px, py - 12, 0xFFB8BDC9, true);
        for (int i = 0; i < PALETTE.length; i++) {
            int col = i % 5, row = i / 5;
            int sx = px + col * 34, sy = py + row * 34;
            g.fill(sx - 2, sy - 2, sx + 30, sy + 30, i == paletteIndex ? 0xFFE8C07A : 0xFF2A3140);
            g.fill(sx, sy, sx + 28, sy + 28, PALETTE[i]);
        }
        int curY = py + 4 * 34 + 16;
        g.text(font, "Current:", px, curY, 0xFF8A90A0, true);
        g.fill(px + 58, curY - 2, px + 88, curY + 18, PALETTE[paletteIndex]);

        // buttons: Save / Reset / Done
        drawButton(g, font, btnX(), btnY(), 150, "Save Custom Texture", mouseX, mouseY);
        drawButton(g, font, btnX() + 162, btnY(), 120, "Reset", mouseX, mouseY);
        drawButton(g, font, btnX() + 294, btnY(), 110, "Done", mouseX, mouseY);

        g.centeredText(font, "Saved packs land in  .minecraft/resourcepacks/DevouringStormsCustomStorm",
                w / 2, h - 14, 0xFF6B7280);
    }

    private void drawButton(GuiGraphicsExtractor g, Font font, int x, int y, int bw,
                            String label, int mouseX, int mouseY) {
        boolean hov = mouseX >= x && mouseX <= x + bw && mouseY >= y && mouseY <= y + 20;
        g.fill(x, y, x + bw, y + 20, hov ? 0xE0171B24 : 0xD812151C);
        g.fill(x, y, x + bw, y + 1, hov ? 0xFFD9A441 : 0xFF2A3140);
        g.fill(x, y + 19, x + bw, y + 20, hov ? 0xFFD9A441 : 0xFF2A3140);
        g.fill(x, y, x + 1, y + 20, hov ? 0xFFD9A441 : 0xFF2A3140);
        g.fill(x + bw - 1, y, x + bw, y + 20, hov ? 0xFFD9A441 : 0xFF2A3140);
        g.centeredText(font, label, x + bw / 2, y + 6, hov ? 0xFFE8C07A : 0xFFB8BDC9);
    }

    private boolean overButton(int mouseX, int mouseY, int x, int y, int bw) {
        return mouseX >= x && mouseX <= x + bw && mouseY >= y && mouseY <= y + 20;
    }

    private int cellAt(int mouseX, int mouseY) {
        int cx = canvasX(), cy = canvasY();
        if (mouseX < cx || mouseY < cy || mouseX >= cx + GRID * CELL || mouseY >= cy + GRID * CELL) {
            return -1;
        }
        int x = (mouseX - cx) / CELL, y = (mouseY - cy) / CELL;
        return y * GRID + x;
    }

    private int swatchAt(int mouseX, int mouseY) {
        int px = paletteX(), py = paletteY();
        if (mouseX < px || mouseY < py) {
            return -1;
        }
        int col = (mouseX - px) / 34, row = (mouseY - py) / 34;
        if (col < 0 || col >= 5 || row < 0 || row >= 4) {
            return -1;
        }
        int i = col + row * 5;
        return (mouseX - px - col * 34) <= 28 && (mouseY - py - row * 34) <= 28 ? i : -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        int mx = (int) event.x(), my = (int) event.y();
        int b = event.button();

        // buttons
        if (overButton(mx, my, btnX(), btnY(), 150)) {
            savePack();
            return true;
        }
        if (overButton(mx, my, btnX() + 162, btnY(), 120)) {
            Arrays.fill(pixels, DEEP_BLACK);
            return true;
        }
        if (overButton(mx, my, btnX() + 294, btnY(), 110)) {
            this.onClose();
            return true;
        }
        // palette
        int sw = swatchAt(mx, my);
        if (sw >= 0) {
            paletteIndex = sw;
            return true;
        }
        // canvas
        int c = cellAt(mx, my);
        if (c >= 0) {
            button = b;
            lastCell = c;
            paintCell(c);
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (button < 0) {
            return super.mouseDragged(event, dragX, dragY);
        }
        int c = cellAt((int) event.x(), (int) event.y());
        if (c >= 0 && c != lastCell) {
            lastCell = c;
            paintCell(c);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    private void paintCell(int c) {
        pixels[c] = (button == 1) ? DEEP_BLACK : PALETTE[paletteIndex];
    }

    /** Writes the 16x16 canvas as a working resource pack (160x160 sheet). */
    private void savePack() {
        try {
            // 16x16 -> 160x160 nearest-neighbour (the body UVs sample a 160px sheet)
            BufferedImage sheet = new BufferedImage(GRID * 10, GRID * 10, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < GRID; y++) {
                for (int x = 0; x < GRID; x++) {
                    int c = pixels[y * GRID + x];
                    for (int dy = 0; dy < 10; dy++) {
                        for (int dx = 0; dx < 10; dx++) {
                            sheet.setRGB(x * 10 + dx, y * 10 + dy, c);
                        }
                    }
                }
            }
            File gameDir = gameDir();
            if (gameDir == null) {
                chat("[ds] painter: could not find the game directory");
                return;
            }
            File packDir = new File(gameDir, "resourcepacks/DevouringStormsCustomStorm");
            File texDir = new File(packDir, "assets/dabywitherstormmod/textures/entity/wither_storm");
            if (!texDir.mkdirs() && !texDir.isDirectory()) {
                chat("[ds] painter: could not create the pack folder");
                return;
            }
            try (FileOutputStream out = new FileOutputStream(new File(texDir, "wither_storm.png"))) {
                ImageIO.write(sheet, "png", out);
            }
            try (FileOutputStream mc = new FileOutputStream(new File(packDir, "pack.mcmeta"))) {
                mc.write(("{\n  \"pack\": {\n    \"pack_format\": 88,\n    \"description\": \"Devouring Storms custom storm texture (painted in the Story Mode Console)\"\n  }\n}")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            chat("[ds] Custom storm texture SAVED — open Options → Resource Packs, enable "
                    + "“Devouring Storms Custom Storm”, then Done. It overrides the storm body sheet.");
            System.out.println("[ds] custom storm texture saved to " + texDir.getAbsolutePath());
        } catch (Throwable t) {
            chat("[ds] painter save failed: " + t);
        }
    }

    private static void chat(String msg) {
        try {
            McsmClientChat.say(msg);
        } catch (Throwable ignored) {
            System.out.println(msg);
        }
    }

    private static File gameDir() {
        try {
            Class<?> loaderCls = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = loaderCls.getMethod("getInstance").invoke(null);
            return new File(loaderCls.getMethod("getGameDir").invoke(loader).toString());
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public void onClose() {
        // same proven close/return pattern as the Story Mode Console
        Minecraft mc = Minecraft.getInstance();
        if (parent != null && mc != null) {
            mc.setScreenAndShow(parent);
        }
    }
}
