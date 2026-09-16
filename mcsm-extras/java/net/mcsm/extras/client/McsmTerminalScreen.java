package net.mcsm.extras.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcsm.extras.McsmTerminal;
import net.mcsm.extras.net.McsmTerminalC2S;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * BUILD #416 (D.8, phase 5) -- THE HOLOGRAPHIC TERMINAL.
 *
 * The user's design: "when you use it a gigantic computer screen opens up on the
 * screen saying enter admin password to continue this area is restricted", the
 * code exists only in the world (IVOR's page, see {@code McsmTerminal}), and
 * behind the code is the console the story is told on.
 *
 * WHAT IT IS.
 *   * LOGIN   -- the restricted screen: scanline field, a real text field, and
 *                the hint the server chooses to give. The code is checked by the
 *                SERVER; a wrong one shakes the frame and prints the hint, the
 *                right one switches this screen to the console.
 *   * CONSOLE -- the live world report (band, position, cities, ladder, radio),
 *                drawn in the storm's own palette.
 *   * GUIDE   -- the field guide's pages.
 *   * RADIO   -- the stations the antenna has picked up.
 *
 * HOW IT IS BUILT, AND WHY IT LOOKS LIKE THIS. This Minecraft draws screens
 * through {@code Screen.extractRenderState(GuiGraphicsExtractor, ...)} -- the
 * proven idiom in this very repository -- so that is what is overridden here,
 * with {@code g.fill}, {@code g.fillGradient}, {@code g.text} and
 * {@code g.centeredText}. Text entry is a real {@code EditBox} (the same widget
 * the story console uses) so typing, backspace, selection and paste all behave
 * exactly as the player already expects from Minecraft, instead of being
 * hand-rolled key handling that would break on the first IME keyboard.
 *
 * No texture files: every pixel is drawn from code, so it cannot go missing.
 */
public final class McsmTerminalScreen extends Screen {

    private static final int BG_TOP = 0xFB05070E;
    private static final int BG_BOTTOM = 0xFB0A0F1E;
    private static final int PANEL = 0xD8070B14;
    private static final int EDGE = 0xFF39E0FF;
    private static final int EDGE_DIM = 0xFF12455A;
    private static final int VIOLET = 0xFF8A5CFF;
    private static final int TEXT = 0xFFBFE9FF;
    private static final int TEXT_DIM = 0xFF5F87A0;
    private static final int WARN = 0xFFFF6A6A;
    private static final int OK = 0xFF7CFFB0;

    private enum Mode { LOGIN, CONSOLE, GUIDE, RADIO }

    private static final String[] GUIDE_PAGES = {
        "cover", "rift", "cities", "bestiary", "ladder", "creator", "blackhole", "tornado", "antenna"
    };
    private static final String[] BUTTONS = {"GUIDE", "RADIO", "REPORT", "NEXT", "CONFIG", "CLOSE"};

    private Mode mode;
    private String body = "";
    private String status = "";
    private boolean statusBad;
    private int ticks;
    private int shake;
    private int openAnim;
    private int station;
    private int guidePage;
    private EditBox codeField;
    /** No connection (the main menu): the console runs on the local copy of the
     *  lore, and the CONFIG button is the way into the settings panel. */
    private final boolean local;
    private int boxX;
    private int boxY;
    private int boxW;
    private int boxH = 18;
    private String typed = "";
    private final List<String> radioLog = new ArrayList<>();

    public McsmTerminalScreen(String startMode, String initialBody) {
        super(Component.literal("Devouring Storms Terminal"));
        this.mode = modeOf(startMode);
        this.body = initialBody == null ? "" : initialBody;
        boolean offline = true;
        try {
            // "in a world" is the same test the rest of this overlay uses: a
            // local player exists. On the menu there is none, so the console
            // runs its local copy of the lore instead of asking a server.
            offline = Minecraft.getInstance().player == null;
        } catch (Throwable ignored) {
            offline = true;
        }
        this.local = offline;
        for (String station : McsmTerminal.stationNames()) {
            radioLog.add(station);
        }
    }

    private static Mode modeOf(String name) {
        if (name == null) {
            return Mode.LOGIN;
        }
        switch (name.toLowerCase(Locale.ROOT)) {
            case "console":
                return Mode.CONSOLE;
            case "guide":
                return Mode.GUIDE;
            case "radio":
                return Mode.RADIO;
            default:
                return Mode.LOGIN;
        }
    }

    /** The one place another class opens this screen. */
    public static void show(String mode, String body) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreenAndShow(new McsmTerminalScreen(mode, body));
        }
    }

    // ---------------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------------

    private int halfW() {
        return Math.min(300, Math.max(120, width / 2 - 20));
    }

    private int left() {
        return width / 2 - halfW();
    }

    private int right() {
        return width / 2 + halfW();
    }

    private int top() {
        return Math.max(14, height / 2 - 132);
    }

    private int bottom() {
        return Math.min(height - 14, height / 2 + 132);
    }

    private int buttonY() {
        return bottom() - 40;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.codeField = null;
        if (mode == Mode.LOGIN) {
            boxW = Math.min(240, halfW() * 2 - 40);
            boxX = width / 2 - boxW / 2;
            boxY = top() + 74;
            EditBox box = new EditBox(this.font, boxX, boxY, boxW, boxH,
                    Component.literal("admin password"));
            box.setMaxLength(24);
            box.setValue(typed);
            box.setHint(Component.literal("enter admin password"));
            box.setResponder(text -> this.typed = text);
            box.setCanLoseFocus(false);
            box.setFocused(true);
            this.codeField = box;
            this.addWidget(box);
        }
    }

    // ---------------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        ticks++;
        if (shake > 0) {
            shake--;
        }
        if (openAnim < 24) {
            openAnim++;
        }
        double ease = 1.0D - Math.pow(1.0D - openAnim / 24.0D, 3.0D);

        // the world behind the terminal goes dark and cold
        g.fill(0, 0, width, height, 0xC0040610);

        int wobble = shake > 0 ? (shake % 4) - 2 : 0;
        int yo = (int) Math.round((1.0D - ease) * 26.0D);
        int l = left() + wobble;
        int r = right() + wobble;
        int t = top() + yo;
        int b = bottom() + yo;

        // frame: glow, body, edges
        g.fillGradient(l - 4, t - 4, r + 4, b + 4, 0x4412A8C8, 0x2212A8C8);
        g.fillGradient(l, t, r, b, BG_TOP, BG_BOTTOM);
        g.fill(l, t, r, t + 2, EDGE);
        g.fill(l, b - 2, r, b, EDGE_DIM);
        g.fill(l, t, l + 2, b, EDGE_DIM);
        g.fill(r - 2, t, r, b, EDGE);

        // drifting scanlines + one bright sweep walking down the panel
        for (int y = t + 4; y < b - 4; y += 4) {
            int a = ((y + ticks) % 8 == 0) ? 0x22 : 0x0E;
            g.fill(l + 3, y, r - 3, y + 1, (a << 24) | 0x1E5A78);
        }
        int sweep = t + 6 + (int) ((ticks * 2) % Math.max(1, (b - t - 12)));
        g.fill(l + 3, sweep, r - 3, sweep + 1, 0x3339E0FF);

        int px = l + 16;
        int maxX = r - 16;
        int y = drawHeader(g, px, t + 10);
        switch (mode) {
            case LOGIN:
                drawLogin(g, px, maxX, y, t, b);
                break;
            case CONSOLE:
                drawLines(g, px, y, maxX, b - 48, "LIVE WORLD REPORT");
                drawButtons(g, px, buttonY() + yo, mouseX, mouseY);
                break;
            case GUIDE:
                drawLines(g, px, y, maxX, b - 48, "FIELD GUIDE :: "
                        + GUIDE_PAGES[Math.max(0, Math.min(GUIDE_PAGES.length - 1, guidePage))]
                                .toUpperCase(Locale.ROOT));
                drawButtons(g, px, buttonY() + yo, mouseX, mouseY);
                break;
            case RADIO:
                drawRadio(g, px, y, maxX, b - 48);
                drawButtons(g, px, buttonY() + yo, mouseX, mouseY);
                break;
            default:
                break;
        }
        g.text(this.font, "[ESC] close   [C] open anywhere   [H] ask IVOR   the code is in the world",
                px, b - 18, TEXT_DIM, false);

        // Widgets are NOT drawn by the base call in this version -- the mod's own
        // config screen draws each one itself (WitherStormConfigScreen does
        // exactly this). So the login field is drawn here, last, over the panel.
        if (codeField != null) {
            codeField.extractRenderState(g, mouseX, mouseY, partialTick);
        }
    }

    private int drawHeader(GuiGraphicsExtractor g, int x, int y) {
        g.text(this.font, "\u25C8 DEVOURING STORMS", x, y, EDGE, false);
        g.text(this.font, "STORY TERMINAL", x + 152, y, VIOLET, false);
        String state = mode == Mode.LOGIN ? "\u25CF RESTRICTED" : "\u25CF ACCESS GRANTED";
        g.text(this.font, state, right() - 16 - this.font.width(state), y,
                mode == Mode.LOGIN ? WARN : OK, false);
        g.fill(x, y + 13, right() - 14, y + 14, EDGE_DIM);
        return y + 26;
    }

    private void drawLogin(GuiGraphicsExtractor g, int x, int maxX, int y, int t, int b) {
        int cx = this.width / 2;
        g.centeredText(this.font, "THIS AREA IS RESTRICTED", cx, y, WARN);
        g.centeredText(this.font, "enter admin password to continue", cx, y + 13, TEXT_DIM);

        // the field itself is a real EditBox, framed here and drawn at the end
        if (codeField != null) {
            g.fill(boxX - 2, boxY - 2, boxX + boxW + 2, boxY + boxH + 2, EDGE_DIM);
            g.fill(boxX, boxY, boxX + boxW, boxY + boxH, PANEL);
        }

        int ty = y + 48;
        if (!status.isEmpty()) {
            for (String line : status.split("\n")) {
                g.text(this.font, line, x, ty, statusBad ? WARN : OK, false);
                ty += 11;
            }
        } else {
            g.text(this.font, "> waiting for operator input", x, ty, TEXT_DIM, false);
            ty += 11;
        }
        ty += 8;
        g.text(this.font, "The code is not on this screen. It is written down in the world,", x, ty, TEXT_DIM, false);
        g.text(this.font, "in IVOR's hands, in the secret room of an abandoned hospital.", x, ty + 11, TEXT_DIM, false);
        g.text(this.font, "[ENTER] submit     [H] ask the set for his line     [ESC] leave", x, ty + 22, TEXT_DIM, false);
    }

    private void drawLines(GuiGraphicsExtractor g, int x, int y, int maxX, int limit, String title) {
        g.text(this.font, "\u2500\u2500 " + title + " \u2500\u2500", x, y, VIOLET, false);
        y += 16;
        for (String line : body.split("\n")) {
            if (y > limit) {
                g.text(this.font, "  ... (more below)", x, y, TEXT_DIM, false);
                break;
            }
            if (line.startsWith("---")) {
                g.fill(x, y + 5, maxX, y + 6, EDGE_DIM);
            } else {
                g.text(this.font, line, x, y, line.startsWith("  ") ? EDGE : TEXT, false);
            }
            y += 11;
        }
    }

    private void drawRadio(GuiGraphicsExtractor g, int x, int y, int maxX, int limit) {
        g.text(this.font, "\u2500\u2500 SIGNAL LOG \u2500\u2500", x, y, VIOLET, false);
        y += 16;
        for (int i = 0; i < radioLog.size(); i++) {
            if (y > limit) {
                break;
            }
            String line = radioLog.get(i);
            g.text(this.font, (i == station ? "\u25B8 " : "  ") + line, x, y,
                    i == station ? EDGE : TEXT_DIM, false);
            y += 11;
        }
        g.text(this.font, "scroll to move between stations", x, y + 6, TEXT_DIM, false);
    }

    private void drawButtons(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
        int bw = 60;
        int bx = x;
        for (int i = 0; i < BUTTONS.length; i++) {
            if (bx + bw > right() - 12) {
                break;
            }
            boolean hot = mouseX >= bx && mouseX < bx + bw && mouseY >= y && mouseY < y + 16;
            g.fill(bx, y, bx + bw, y + 16, hot ? 0xFF123A52 : 0xCC0B1A26);
            g.fill(bx, y, bx + bw, y + 1, hot ? EDGE : EDGE_DIM);
            g.fill(bx, y + 15, bx + bw, y + 16, EDGE_DIM);
            g.text(this.font, BUTTONS[i], bx + 5, y + 4, hot ? EDGE : TEXT, false);
            bx += bw + 6;
        }
    }

    // ---------------------------------------------------------------------
    // Input
    // ---------------------------------------------------------------------

    private int buttonAt(double mouseX, double mouseY) {
        if (mode == Mode.LOGIN) {
            return -1;
        }
        int y = buttonY();
        int bx = left() + 16;
        for (int i = 0; i < BUTTONS.length; i++) {
            if (mouseX >= bx && mouseX < bx + 60 && mouseY >= y && mouseY < y + 16) {
                return i;
            }
            bx += 66;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        int hit = buttonAt(event.x(), event.y());
        if (hit >= 0 && event.button() == 0) {
            switch (hit) {
                case 0:
                    mode = Mode.GUIDE;
                    if (local) {
                        body = McsmTerminal.guideText(
                                GUIDE_PAGES[Math.max(0, Math.min(GUIDE_PAGES.length - 1, guidePage))]);
                    } else {
                        ClientPlayNetworking.send(new McsmTerminalC2S("guide",
                                GUIDE_PAGES[Math.max(0, Math.min(GUIDE_PAGES.length - 1, guidePage))], ""));
                    }
                    break;
                case 1:
                    mode = Mode.RADIO;
                    break;
                case 2:
                    mode = Mode.CONSOLE;
                    if (local) {
                        body = McsmTerminal.localReport();
                    } else {
                        ClientPlayNetworking.send(new McsmTerminalC2S("open", "", ""));
                    }
                    break;
                case 3:
                    guidePage = (guidePage + 1) % GUIDE_PAGES.length;
                    mode = Mode.GUIDE;
                    if (!local) {
                        ClientPlayNetworking.send(new McsmTerminalC2S("guide", GUIDE_PAGES[guidePage], ""));
                    } else {
                        body = McsmTerminal.guideText(GUIDE_PAGES[guidePage]);
                    }
                    break;
                case 4:
                    // The settings console, opened from inside the terminal: this
                    // is the "click the logo button -> the terminal -> the config"
                    // path, and it works from the menu as well as in game.
                    try {
                        Minecraft.getInstance().setScreenAndShow(new McsmExtrasScreen(this));
                    } catch (Throwable ignored) {
                    }
                    break;
                case 5:
                    onClose();
                    break;
                default:
                    break;
            }
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mode == Mode.RADIO && !radioLog.isEmpty()) {
            station = Math.floorMod(station + (scrollY > 0 ? -1 : 1), radioLog.size());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        try {
            ClientPlayNetworking.send(new McsmTerminalC2S("close", "", ""));
        } catch (Throwable ignored) {
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            // back to the game (the same call the story console's Done uses)
            mc.setScreenAndShow(null);
        }
    }

    // ---------------------------------------------------------------------
    // What the server sends back
    // ---------------------------------------------------------------------

    public void accept(String action, String a, String b) {
        switch (action) {
            case "granted":
                mode = Mode.CONSOLE;
                status = "";
                if (b != null && !b.isEmpty()) {
                    body = b;
                }
                this.init();
                break;
            case "denied":
                status = b == null ? "ACCESS DENIED" : b;
                statusBad = true;
                shake = 12;
                typed = "";
                if (codeField != null) {
                    codeField.setValue("");
                }
                break;
            case "line":
                status = b == null ? "" : b;
                statusBad = false;
                break;
            case "open":
                mode = modeOf(a);
                if (b != null && !b.isEmpty()) {
                    body = b;
                }
                this.init();
                break;
            case "radio":
                if (a != null) {
                    String entry = a + (b == null || b.isEmpty() ? "" : "\n     " + b);
                    if (!radioLog.contains(entry)) {
                        radioLog.add(entry);
                    }
                    station = radioLog.size() - 1;
                }
                break;
            case "close":
                break;
            default:
                break;
        }
    }

    /** The login field's current contents, for the submit path. */
    public String typedCode() {
        return typed;
    }

    public void submitCode() {
        String typed = typedCode();
        if (local) {
            // No server to ask (this is the menu, not a world): the code is
            // checked against the same constant the server uses. Nothing here
            // grants anything in a world -- that is still the server's call.
            if (McsmTerminal.CODE.equalsIgnoreCase(typed.trim())) {
                accept("granted", "console", McsmTerminal.localReport());
            } else {
                accept("denied", "login", "ACCESS DENIED\n\nThe code is in the world, in IVOR's hands.");
            }
            return;
        }
        ClientPlayNetworking.send(new McsmTerminalC2S("code", typed, ""));
    }

    public void askHint() {
        if (local) {
            accept("line", "login", McsmTerminal.hintText());
            return;
        }
        ClientPlayNetworking.send(new McsmTerminalC2S("hint", "", ""));
    }

    public boolean isLogin() {
        return mode == Mode.LOGIN;
    }
}
