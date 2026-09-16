package net.mcsm.extras.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.mcsm.extras.McsmFutureBook;
import net.mcsm.extras.McsmSounds;
import net.mcsm.extras.McsmTerminal;
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
        "cover", "book", "rift", "cities", "bestiary", "ladder", "creator", "blackhole",
        "tornado", "antenna"
    };
    private static final String[] BUTTONS = {"GUIDE", "RADIO", "REPORT", "NEXT", "CONFIG", "CLOSE"};

    private Mode mode;
    private String body = "";
    private String status = "";
    private boolean statusBad;
    private int ticks;
    private int shake;
    /** BUILD #448 -- a widget rebuild the input path asked for, done next frame. */
    private transient boolean pendingInit;
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
    /** BUILD #424 -- the login page's ENTER button is hot under the cursor. */
    private boolean enterHot;
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

    /**
     * THE CONSOLE'S REPORT, drawn from what the client already knows.
     *
     * It is deliberately the CLIENT's own reading of the world -- the dimension
     * it is standing in, where it is standing, what it is holding -- because the
     * server's copy of those numbers cannot reach a screen in this build (the
     * overlay compiles against the client jar and the Fabric rendering modules
     * only; see McsmTerminal.register, which explains why there is no channel).
     * The server still speaks: use the antenna and it answers in chat with the
     * numbers only it can compute (surface, storm band, the nearest hub).
     */
    private static String worldReport(boolean menu) {
        StringBuilder out = new StringBuilder();
        out.append("DEVOURING STORMS :: STORY TERMINAL\n");
        out.append("-----------------------------------------------------------\n");
        try {
            Minecraft mc = Minecraft.getInstance();
            if (menu || mc == null || mc.player == null || mc.level == null) {
                return McsmTerminal.localReport();
            }
            net.minecraft.core.BlockPos at = mc.player.blockPosition();
            out.append("position    : ").append(at.getX()).append(", ")
               .append(at.getY()).append(", ").append(at.getZ()).append('\n');
            net.minecraft.world.item.ItemStack held = mc.player.getMainHandItem();
            out.append("holding     : ").append(held.isEmpty()
                    ? "nothing" : held.getItem().getClass().getSimpleName()).append('\n');
            float storm = net.dabicco.witherstormmod.client.StormSkyDarken.factor();
            out.append("storm sky   : ").append(storm > 0.04F
                    ? "dark (the storm owns the sky here)" : "clear").append('\n');
        } catch (Throwable t) {
            return McsmTerminal.localReport();
        }
        out.append("-----------------------------------------------------------\n");
        out.append("The set answers in chat when you use it: the server prints the\n");
        out.append("dimension, the surface, the storm band and the nearest hub.\n");
        out.append("-----------------------------------------------------------\n");
        out.append("Guide pages : cover, rift, cities, bestiary, ladder, creator,\n");
        out.append("              blackhole, tornado, antenna\n");
        out.append("Radio       : ").append(McsmTerminal.stationNames().size())
           .append(" stations on this set.\n");
        out.append(McsmFutureBook.forecast()).append("\n");
        out.append("Future-book  : B opens it. Its last page is blank, and it is yours.\n");
        out.append("The MASSG    : released by the operator -- sneak-use the antenna.\n");
        out.append("               It cannot be killed, and it cannot be undone.\n");
        return out.toString();
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

    /**
     * BUILD #424 -- THE ENTER BUTTON. The login page's one action, drawn big
     * enough to be obvious and clickable even when the field has no focus. The
     * chip row is drawn on the login page too, so GUIDE / RADIO / CONFIG / CLOSE
     * are reachable before the code is entered (that is the "there's only one
     * config menu" report: from the login page there was no way to the panel).
     */
    private int[] enterRect() {
        int w = 132;
        int x = this.width / 2 - w / 2;
        int y = boxY + boxH + 14;
        return new int[]{x, y, w, 22};
    }

    /** One key press, dispatched. */
    private void onKey(int key) {
        if (key == McsmKeyboard.ESCAPE) {
            onClose();
            return;
        }
        if (key == McsmKeyboard.ENTER) {
            if (mode == Mode.LOGIN) {
                submitCode();
            } else if (mode == Mode.GUIDE) {
                guidePage = (guidePage + 1) % GUIDE_PAGES.length;
                body = McsmTerminal.guideText(GUIDE_PAGES[guidePage]);
            } else if (mode == Mode.CONSOLE) {
                body = worldReport(true);
            }
            return;
        }
        if (key == McsmKeyboard.BACKSPACE) {
            if (!typed.isEmpty()) {
                typed = typed.substring(0, typed.length() - 1);
                syncField();
            }
            return;
        }
        if (mode != Mode.LOGIN) {
            return;      // outside the login page this screen is buttons-only
        }
        if (key == McsmKeyboard.H) {
            status = McsmTerminal.hintText();
            statusBad = false;
            return;
        }
        char ch = McsmKeyboard.textOf(key);
        if (ch != 0 && typed.length() < 24) {
            typed = typed + ch;
            status = "";
            statusBad = false;
            syncField();
            mcsm$cue(McsmSounds.TERMINAL_KEY, 0.5F, 1.0F);
        }
    }

    /**
     * BUILD #425 -- the terminal's own key ticks, on the CLIENT side, through the
     * local player (the same call the HUD console already makes for its buttons).
     * The mod's sounds are registered in its own namespace by McsmSounds, so this
     * is the mod's voice, not the game's.
     */
    private void mcsm$cue(Object sound, float volume, float pitch) {
        try {
            net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && sound instanceof net.minecraft.sounds.SoundEvent event) {
                player.playSound(event, volume, pitch);
            }
        } catch (Throwable ignored) {
            // a silent terminal is better than a crashing one
        }
    }

    /**
     * BUILD #448 -- SHOW WHAT THREW.
     *
     * A crash the player cannot describe is a bug nobody can fix, and this build
     * cannot be run on the machine it is written on. So: nothing in this screen's
     * input path is allowed to end the game, and whatever would have done it is put
     * on the screen and in the log instead, in one line, with its class and message.
     */
    private void report(Throwable t, String where) {
        String line = where + ": " + t.getClass().getName()
                + (t.getMessage() == null ? "" : " -- " + t.getMessage());
        status = "\u00a7call right, that was a bug: " + line;
        statusBad = true;
        System.err.println("[ds] the terminal survived " + line);
        t.printStackTrace();
    }

    /** Keep the visible field and our own copy in step, whichever drove it. */
    private void syncField() {
        if (codeField != null) {
            try {
                codeField.setValue(typed);
            } catch (Throwable ignored) {
                // the field is only a display: our copy is authoritative
            }
        }
    }


    @Override
    protected void init() {
        rebuild();
    }

    /** init()'s body, on its own so the deferred path can call it too. */
    private void rebuild() {
        try {
            rebuildGuarded();
        } catch (Throwable t) {
            report(t, "rebuild");
        }
    }

    private void rebuildGuarded() {
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
        // BUILD #424 -- the keyboard is read here, per frame, straight off the
        // window (see McsmKeyboard). "The keys do not work or function" was
        // true: nothing in this screen ever looked at a key, so the only way in
        // was a button that did not exist on the login page either.
        // BUILD #448 -- THE CLICK THAT CRASHED THE GAME.
        //
        // The report: "when I try to click on the the enter password thing it just
        // crashes my game". This screen's input path is the render thread's, so
        // anything that throws in it takes the whole game down rather than printing
        // a line. Two things were wrong and both are fixed here:
        //
        //   * the loop below called straight into submitCode() -> accept() -> init(),
        //     and init() CLEARS AND REBUILDS THE WIDGET LIST in the middle of a frame
        //     the screen is being drawn in. That is a widget list mutated while it is
        //     being iterated, which is the classic way to crash a screen. The rebuild
        //     is DEFERRED now (pendingInit) and happens at the top of the next frame.
        //   * nothing caught a throwable, so any of the calls in the chain -- the
        //     sound cue, the report, the field sync -- could end the game. Every one
        //     of them is caught, and the throwable is SHOWN on the screen as well as
        //     logged, because a crash nobody can reproduce is a crash nobody can fix:
        //     the next report can be the line itself.
        if (pendingInit) {
            pendingInit = false;
            rebuild();
        }
        for (Integer key : McsmKeyboard.poll()) {
            onKey(key.intValue());
        }
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
        int[] enter = enterRect();
        this.enterHot = mouseX >= enter[0] && mouseX < enter[0] + enter[2]
                && mouseY >= enter[1] && mouseY < enter[1] + enter[3];
        switch (mode) {
            case LOGIN:
                drawLogin(g, px, maxX, y, t, b, yo, mouseX, mouseY);
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

        // Widgets are NOT drawn by the base call in this version -- the base
        // mod's own config screen draws each one itself (WitherStormConfigScreen
        // does exactly this). So the login field is drawn here, last, over the
        // panel.
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

    private void drawLogin(GuiGraphicsExtractor g, int x, int maxX, int y, int t, int b,
            int yo, int mouseX, int mouseY) {
        int cx = this.width / 2;
        g.centeredText(this.font, "THIS AREA IS RESTRICTED", cx, y, WARN);
        g.centeredText(this.font, "enter admin password to continue", cx, y + 13, TEXT_DIM);

        // the field itself is a real EditBox, framed here and drawn at the end
        if (codeField != null) {
            g.fill(boxX - 2, boxY - 2, boxX + boxW + 2, boxY + boxH + 2, EDGE_DIM);
            g.fill(boxX, boxY, boxX + boxW, boxY + boxH, PANEL);
        }

        // THE ENTER BUTTON -- clickable, always drawn, and the reason this page
        // stopped being a dead end. The keyboard types into the field; this is
        // the way in for anyone who would rather click.
        int[] e = enterRect();
        boolean eHot = this.enterHot;
        g.fill(e[0] - 1, e[1] - 1, e[0] + e[2] + 1, e[1] + e[3] + 1, EDGE);
        g.fill(e[0], e[1], e[0] + e[2], e[1] + e[3], eHot ? 0xFF1B6B8C : 0xFF0C2C3E);
        g.centeredText(this.font, "\u25B8 ENTER  (submit the code)", this.width / 2, e[1] + 7,
                eHot ? 0xFFFFFFFF : EDGE);
        // the chip row is live on this page too (GUIDE / RADIO / CONFIG / CLOSE)
        drawButtons(g, left() + 16, buttonY() + yo, mouseX, mouseY);

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
        g.text(this.font, "type with the keyboard, or click ENTER above. [H] hints. [ESC] leaves.",
                x, ty + 22, TEXT_DIM, false);
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
        // BUILD #424 -- the chips are live in every mode now, including the login
        // page. Before this, the only page with a password on it was the only
        // page with nothing clickable: GUIDE, RADIO, CONFIG (the settings panel)
        // and CLOSE were all unreachable until the code had been accepted.
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
        try {
            return mouseClickedGuarded(event, doubled);
        } catch (Throwable t) {
            report(t, "mouseClicked");
            return true;
        }
    }

    private boolean mouseClickedGuarded(MouseButtonEvent event, boolean doubled) {
        // the login page's ENTER button
        if (mode == Mode.LOGIN && event.button() == 0) {
            int[] e = enterRect();
            if (event.x() >= e[0] && event.x() < e[0] + e[2]
                    && event.y() >= e[1] && event.y() < e[1] + e[3]) {
                submitCode();
                return true;
            }
        }
        int hit = buttonAt(event.x(), event.y());
        if (hit >= 0 && event.button() == 0) {
            switch (hit) {
                case 0:
                    mode = Mode.GUIDE;
                    body = McsmTerminal.guideText(
                            GUIDE_PAGES[Math.max(0, Math.min(GUIDE_PAGES.length - 1, guidePage))]);
                    break;
                case 1:
                    mode = Mode.RADIO;
                    break;
                case 2:
                    mode = Mode.CONSOLE;
                    body = worldReport(local);
                    break;
                case 3:
                    guidePage = (guidePage + 1) % GUIDE_PAGES.length;
                    mode = Mode.GUIDE;
                    body = McsmTerminal.guideText(GUIDE_PAGES[guidePage]);
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
        try {
            acceptGuarded(action, a, b);
        } catch (Throwable t) {
            report(t, "accept");
        }
    }

    private void acceptGuarded(String action, String a, String b) {
        switch (action) {
            case "granted":
                mode = Mode.CONSOLE;
                status = "";
                if (b != null && !b.isEmpty()) {
                    body = b;
                }
                // DEFERRED, not immediate: see the note on the input loop. Calling
                // init() here rebuilt the widget list mid-frame.
                pendingInit = true;
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
                // deferred, for the same reason as "granted": a mode change that
                // rebuilds the widgets mid-frame is the crash this build just fixed
                pendingInit = true;
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

    /**
     * The code is checked HERE and now, because the screen is where the player
     * is. What that unlocks is the console's own content -- the report, the
     * guide, the radio -- all of which is client-side, and none of which changes
     * the world. The one thing in the world that the code gates is the MASSG, and
     * THAT is granted by the server ({@code McsmTerminal.verify}) when the player
     * has actually entered the code where it counts: the set in their hand.
     */
    /** The public submit, called by the ENTER button, the chip row and the keyboard. */
    public void submitCode() {
        try {
            submitCodeGuarded();
        } catch (Throwable t) {
            report(t, "submitCode");
        }
    }

    private void submitCodeGuarded() {
        if (McsmTerminal.CODE.equalsIgnoreCase(typedCode().trim())) {
            mcsm$cue(McsmSounds.TERMINAL_OPEN, 0.9F, 1.0F);
            accept("granted", "console", worldReport(local));
        } else {
            mcsm$cue(McsmSounds.TERMINAL_DENY, 0.9F, 1.0F);
            accept("denied", "login", "ACCESS DENIED\n\nThe code is in the world, in IVOR's hands.");
        }
    }

    public void askHint() {
        accept("line", "login", McsmTerminal.hintText());
    }

    public boolean isLogin() {
        return mode == Mode.LOGIN;
    }
}
