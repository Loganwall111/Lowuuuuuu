package net.mcsm.extras.client;

import java.util.ArrayList;
import java.util.List;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmFutureBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * BUILD #445 -- THE FUTURE-BOOK, ON SCREEN.
 *
 * <p>Two pages, a spine, a violet rule on each edge, and a chapter column down the
 * left with the reader's page at the bottom of it.
 *
 * <p>THE RULES THIS SCREEN KEEPS, all of them learned the hard way in this build:
 *
 * <ul>
 *   <li><b>THE KEYBOARD IS POLLED, NOT HOOKED.</b> This game version does not route
 *       key events to screens reliably (the terminal needed its own {@link McsmKeyboard}
 *       for exactly this reason), so every key here comes from {@link McsmKeyboard#poll()}
 *       once per frame, and typed letters come from {@link McsmKeyboard#textOf(int)}.
 *       There is no other input path, which is why the book works with the mouse
 *       alone if GLFW cannot be reached.</li>
 *   <li><b>NOTHING IS DRAWN OUTSIDE THE PLATE.</b> No edge tint, no darkening at
 *       the frame's borders: the plate is the whole screen's reason to exist and
 *       the backdrop behind it is one flat fill.</li>
 *   <li><b>THE PAGES COME FROM {@link McsmFutureBook}, AND THEY MOVE.</b> The screen
 *       reads them fresh every frame from the real clock, so a book left open at
 *       midnight changes its own dates.</li>
 * </ul>
 */
public final class McsmFutureBookScreen extends Screen {

    private static final int PLATE = 0xFF0D0A16;
    private static final int PLATE_EDGE = 0xFF2A1F45;
    private static final int SPINE = 0xFF6D3FD4;
    private static final int SPINE_DIM = 0xFF3A2760;
    private static final int TEXT = 0xFFDCCFF5;
    private static final int TEXT_DIM = 0xFF7C6CA0;
    private static final int VIOLET = 0xFF9A6BFF;
    private static final int GOLD = 0xFFE3C77A;
    private static final int MARKER = 0xFF1B1430;

    private static final int PLATE_W_MAX = 420;
    private static final int PLATE_H_MAX = 236;
    private static final int ROW = 11;

    private int selected;
    private int scroll;
    private boolean writing;
    private String typed = "";
    private String status = "";
    private int statusTicks;

    public McsmFutureBookScreen() {
        // The same constructor the terminal uses: this API's Screen does not
        // offer a no-argument one, and a missing super() here would be a javac
        // error rather than a screen.
        super(Component.literal("The Infinite Future-Book"));
        this.selected = McsmFutureBook.currentPage(McsmFutureBook.daysLeft());
    }

    private int plateW() {
        return Math.min(PLATE_W_MAX, this.width - 24);
    }

    private int plateH() {
        return Math.min(PLATE_H_MAX, this.height - 24);
    }

    private int midX() {
        return (this.width - plateW()) / 2 + plateW() / 2;
    }

    /** True while the reader is typing into the last page. */
    public boolean writing() {
        return writing;
    }

    /** The one public door, next to the terminal's own {@code show}. */
    public static void show() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreenAndShow(new McsmFutureBookScreen());
        }
    }

    // ---------------------------------------------------------------------
    // Frame
    // ---------------------------------------------------------------------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        List<McsmFutureBook.Page> pages = McsmFutureBook.pages();
        int last = pages.size();            // the reader's own page is one past the book

        // the book's own keyboard, polled like the terminal's
        for (Integer key : McsmKeyboard.poll()) {
            int k = key.intValue();
            if (k == McsmKeyboard.ESCAPE) {
                if (writing) {
                    writing = false;
                    typed = safe(McsmExtrasConfig.futureBookNote);
                } else {
                    onClose();
                }
            } else if (k == McsmKeyboard.ENTER) {
                if (selected == last) {
                    commit();
                } else {
                    selected = Math.min(last, selected + 1);
                }
            } else if (k == McsmKeyboard.BACKSPACE) {
                if (writing && !typed.isEmpty()) {
                    typed = typed.substring(0, typed.length() - 1);
                }
            } else if (k == McsmKeyboard.B) {
                // B belongs to opening the book, not to the page: leave the letter
                if (writing) {
                    typed = typed + "b";
                }
            } else if (k == McsmKeyboard.W) {
                if (!writing) {
                    selected = Math.max(0, selected - 1);
                } else {
                    typed = typed + "w";
                }
            } else if (k == McsmKeyboard.S) {
                if (!writing) {
                    selected = Math.min(last, selected + 1);
                } else {
                    typed = typed + "s";
                }
            } else if (k == McsmKeyboard.SPACE) {
                if (writing) {
                    typed = typed + " ";
                } else {
                    selected = Math.min(last, selected + 1);
                }
            } else if (writing) {
                char ch = McsmKeyboard.textOf(k);
                if (ch != 0 && typed.length() < 120) {
                    typed = typed + ch;
                }
            }
        }
        if (statusTicks > 0) {
            statusTicks--;
        }

        int plateW = Math.min(PLATE_W_MAX, this.width - 24);
        int plateH = Math.min(PLATE_H_MAX, this.height - 24);
        int x0 = (this.width - plateW) / 2;
        int y0 = (this.height - plateH) / 2;
        int x1 = x0 + plateW;
        int y1 = y0 + plateH;
        int mid = x0 + plateW / 2;
        int top = y0 + 26;
        int bottom = y1 - 26;

        // BUILD #464 -- flat backdrop, then the plate: this is the whole frame. The
        // backdrop was 0xF20A0812 (a black room around the book); it is the storm's
        // sky now, at half, so the book reads as the lit thing it is.
        McsmMenuSky.paint(g, this.width, this.height, 0.5F);
        g.fill(x0, y0, x1, y1, PLATE);
        g.fill(x0, y0, x1, y0 + 1, PLATE_EDGE);
        g.fill(x0, y1 - 1, x1, y1, PLATE_EDGE);
        g.fill(mid - 1, y0 + 6, mid + 1, y1 - 6, SPINE_DIM);
        g.fill(mid, y0 + 6, mid + 1, y1 - 6, SPINE);

        g.text(this.font, "\u2726 THE INFINITE FUTURE-BOOK", x0 + 10, y0 + 8, VIOLET, false);
        g.text(this.font, "D-" + McsmFutureBook.daysLeft() + " \u00b7 ends "
                + McsmFutureBook.endDate(), x1 - 10 - this.font.width("D-999 \u00b7 ends "
                + McsmFutureBook.endDate()), y0 + 8, TEXT_DIM, false);

        // ---- the chapter column --------------------------------------
        int rows = Math.max(1, (bottom - top) / ROW);
        if (selected < scroll) {
            scroll = selected;
        }
        if (selected >= scroll + rows) {
            scroll = selected - rows + 1;
        }
        scroll = Math.max(0, Math.min(scroll, Math.max(0, last + 1 - rows)));
        for (int i = 0; i < rows; i++) {
            int index = scroll + i;
            if (index > last) {
                break;
            }
            int y = top + i * ROW;
            boolean here = index == selected;
            if (here) {
                g.fill(x0 + 6, y - 2, mid - 6, y + ROW - 2, MARKER);
                g.fill(x0 + 6, y - 2, x0 + 8, y + ROW - 2, SPINE);
            }
            String title = index == last ? "THE LAST PAGE" : pages.get(index).title();
            String chapter = index == last ? "XXI" : pages.get(index).chapter();
            String label = chapter + "  " + trim(title, mid - x0 - 40);
            g.text(this.font, label, x0 + 11, y, here ? VIOLET : TEXT_DIM, false);
        }

        // ---- the right page ------------------------------------------
        int px = mid + 10;
        int maxX = x1 - 12;
        int py = top - 2;
        if (selected == last) {
            py = drawReaderPage(g, pages, px, py, maxX, bottom);
        } else {
            McsmFutureBook.Page page = pages.get(selected);
            g.text(this.font, "\u2500\u2500 CHAPTER " + page.chapter() + " \u2500\u2500", px, py, VIOLET,
                    false);
            py += 13;
            g.text(this.font, page.title(), px, py, TEXT, false);
            py += 12;
            String line = "D-" + page.day() + "  \u00b7  holds " + page.span()
                    + (page.span() == 1 ? " day" : " days") + "  \u00b7  " + McsmFutureBook.endDate();
            g.text(this.font, line, px, py, GOLD, false);
            py += 14;
            for (String body : page.lines()) {
                for (String wrapped : wrap(body, maxX - px)) {
                    if (py > bottom - 12) {
                        g.text(this.font, "... (the page goes on)", px, py, TEXT_DIM, false);
                        py += ROW;
                        break;
                    }
                    g.text(this.font, wrapped, px, py, TEXT, false);
                    py += ROW;
                }
            }
            if (py <= bottom - 12 && page.omen() != null && !page.omen().isEmpty()) {
                g.text(this.font, "\u00b7 " + page.omen(), px, bottom - 12, GOLD, false);
            }
        }

        // ---- the foot -------------------------------------------------
        String foot = writing
                ? "type your page \u00b7 ENTER keeps it \u00b7 ESC steps back"
                : "SPACE / W / S turn \u00b7 ENTER writes the last page \u00b7 ESC shuts the book";
        if (statusTicks > 0 && !status.isEmpty()) {
            foot = status;
        }
        g.text(this.font, foot, x0 + 10, y1 - 15, writing ? GOLD : TEXT_DIM, false);
    }

    /** The reader's own page: the book's last leaf, and the only writable one. */
    private int drawReaderPage(GuiGraphicsExtractor g, List<McsmFutureBook.Page> pages, int px,
                               int py, int maxX, int bottom) {
        g.text(this.font, "\u2500\u2500 CHAPTER XXI \u2500\u2500", px, py, VIOLET, false);
        py += 13;
        g.text(this.font, "THE LAST PAGE", px, py, TEXT, false);
        py += 12;
        g.text(this.font, "D-" + McsmFutureBook.daysLeft() + "  \u00b7  yours  \u00b7  kept in "
                + "this world's config", px, py, GOLD, false);
        py += 14;
        g.text(this.font, "Every other page in this book was written", px, py, TEXT, false);
        py += ROW;
        g.text(this.font, "before you got here. This one is not.", px, py, TEXT, false);
        py += ROW + 4;

        String note = safe(McsmExtrasConfig.futureBookNote);
        if (writing) {
            List<String> lines = wrap(typed, Math.max(40, maxX - px));
            int shown = Math.min(lines.size(), Math.max(1, (bottom - py - 14) / ROW));
            for (int i = 0; i < shown; i++) {
                boolean caret = i == shown - 1 && lines.size() <= shown;
                g.text(this.font, lines.get(i) + (caret ? "\u258C" : ""), px, py, TEXT, false);
                py += ROW;
            }
        } else if (!note.isEmpty()) {
            for (String line : wrap(note, Math.max(40, maxX - px))) {
                if (py > bottom - 14) {
                    break;
                }
                g.text(this.font, line, px, py, TEXT, false);
                py += ROW;
            }
            g.text(this.font, "\u00b7 written and dated, and it stays", px, bottom - 12, GOLD, false);
        } else {
            g.text(this.font, "(no page yet)", px, py, TEXT_DIM, false);
        }
        return py;
    }

    private void commit() {
        if (!writing) {
            writing = true;
            typed = safe(McsmExtrasConfig.futureBookNote);
            status = "the ink is yours \u00b7 ENTER keeps it";
            statusTicks = 80;
            return;
        }
        write();
    }

    /** The reader's page goes into the world's config, dated by nothing more than today. */
    private void write() {
        String clean = safe(typed).replace('\n', ' ').replace('\r', ' ').trim();
        McsmExtrasConfig.futureBookNote = clean;
        McsmExtrasConfig.futureBookNoteDay = McsmFutureBook.daysLeft();
        try {
            McsmExtrasConfig.save();
        } catch (Throwable t) {
            status = "the page could not be kept: " + t;
            statusTicks = 80;
            return;
        }
        writing = false;
        status = "written on D-" + McsmExtrasConfig.futureBookNoteDay + " \u00b7 it stays";
        statusTicks = 120;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubled);
        }
        // the reader's page is the whole lower half of the right page
        int mid = midX();
        int plateMidY = (this.height - plateH()) / 2 + plateH() / 2;
        if (event.x() > mid && event.y() > plateMidY) {
            if (selected != McsmFutureBook.pages().size()) {
                selected = McsmFutureBook.pages().size();
            } else {
                commit();
            }
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int direction = scrollY > 0 ? -1 : 1;
        selected = Math.max(0, Math.min(McsmFutureBook.pages().size(), selected + direction));
        return true;
    }

    @Override
    public void onClose() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreenAndShow(null);
        }
    }

    // ---------------------------------------------------------------------

    private String trim(String text, int maxWidth) {
        if (this.font == null || text == null) {
            return text == null ? "" : text;
        }
        if (this.font.width(text) <= maxWidth) {
            return text;
        }
        String cut = text;
        while (cut.length() > 2 && this.font.width(cut + "\u2026") > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "\u2026";
    }

    /** Word wrap, measured with the real font rather than guessed at. */
    private List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        String body = safe(text);
        if (body.isEmpty()) {
            out.add("");
            return out;
        }
        StringBuilder line = new StringBuilder();
        for (String word : body.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (line.length() == 0 || this.font.width(candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
            } else {
                out.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        out.add(line.toString());
        return out;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
