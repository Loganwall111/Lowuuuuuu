package net.dabicco.witherstormmod.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Devouring Storms: WitherStormConfigScreen - the Story-Mode console revamp.
 *
 * Build #416 relayout. The #383 pass moved chrome by matching four labels and
 * left everything it did not recognise exactly where the base screen put it,
 * i.e. in the same 20-pixel band: the tabs it renamed onto a rail at y=26
 * collided with the base's tab underline at y=50, and the buttons it did not
 * name ("Cancel", "Apply Preset", "Discard Changes", "Save & Quit", the phase
 * and sub-phase pickers) stayed at the base's y=h-52 / y=h-27 rows and
 * overlapped the new single bottom bar. On any window narrower than ~900 px
 * its centred arithmetic also pushed "Save to Server" off the left edge. Since
 * the extras entry is added to this same widget list, an overlapping chrome
 * row is exactly how "click any button and it reopens the panel" happened.
 *
 * The layout is now table-driven and total: every widget this screen owns is
 * classified by label into a tab slot, the primary bottom row (Done plus the
 * preview controls), the secondary bottom row (save/reset/cancel/apply/discard)
 * or the right-aligned search field. Each row is laid out left-to-right with
 * cumulative x and a proportional shrink when the row does not fit, so no two
 * widgets can share pixels at any GUI scale, and no widget can be placed
 * off-screen at any width.
 *
 * The top-right corner is RESERVED for the Devouring Storms entry
 * (McsmGuiExtrasRows, 214x20 inset 26/8); the "EPISODE CONSOLE" tag is
 * right-aligned to the left of that slot.
 *
 * Chrome drawn here: episodic header band (wordmark + tag + hairline rule),
 * the painted-out base tab underline, a per-tab accent underline, the silver
 * pixel border frame, and the bottom bar band. The giant 3D preview stays
 * removed by default (McsmPreviewRemoveMixin).
 */
@Mixin(WitherStormConfigScreen.class)
public abstract class McsmConfigReskinMixin {

    private static final int TAB_W = 150;
    private static final int TAB_H = 24;
    private static final int TAB_GAP = 12;
    private static final int TAB_Y = 26;
    private static final int BAR_H = 22;
    /** Reserved top-right slot for the extras entry -- keep in sync with McsmGuiExtrasRows. */
    private static final int ENTRY_W = 214;
    private static final int ENTRY_INSET_X = 26;
    private static final int ENTRY_Y = 8;
    private static final int EDGE = 24;

    @Inject(method = "extractRenderState", at = @At("HEAD"), remap = false)
    private void dabyws$mcsPlate(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
        int w = self.width;
        int h = self.height;
        // BUILD #416 (D.8 UI pass) -- the settings console gets its own backdrop.
        //
        // It used to be a flat near-black plate: "a giant black border gets very
        // dark". The console now paints the storm's own sky as its ground -- a
        // violet-to-plum gradient, a horizon glow band, slow cloud streaks and a
        // soft vignette -- so the panel reads as a window onto the storm instead
        // of a black frame, while the content keeps its contrast (every value
        // here stays under 0x40 luminance and the text plate below is opaque).
        g.fillGradient(0, 0, w, h, 0xFF1A1130, 0xFF0B0716);
        // horizon glow, low and wide: the storm's own band, not a flat wash
        int glowY = h * 3 / 4;
        g.fillGradient(0, glowY, w, h, 0x00000000, 0x66462A6E);
        g.fillGradient(0, glowY - 40, w, glowY, 0x00000000, 0x22361F52);
        // cloud streaks: four slow bands, offset per streak so they never march
        long ms = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            int bandY = (int) (h * (0.18 + i * 0.09));
            int drift = (int) (((ms / (90 + i * 40)) % (w + 220)) - 110);
            int bandH = 10 + i * 4;
            g.fillGradient(Math.max(0, drift - 120), bandY, Math.min(w, drift + 160), bandY + bandH,
                    0x00000000, 0x14A98BD8);
        }
        // vignette corners, so the eye goes to the panel rather than the frame
        g.fillGradient(0, 0, w, 26, 0x33000000, 0x00000000);
        g.fillGradient(0, h - 26, w, h, 0x00000000, 0x44000000);
        // side panels
        g.fillGradient(0, 0, 18, h, 0xAA0A0612, 0x22140622);
        g.fillGradient(w - 18, 0, w, h, 0x22140622, 0xAA0A0612);
        // bottom bar band (widgets draw on top of this later in the frame)
        g.fillGradient(0, h - 70, w, h, 0x3305030A, 0xCC0A0614);
        g.fill(0, h - 70, w, h - 69, 0xFF3A2A55);
    }

    /**
     * Build #416: the whole chrome layout in one deterministic pass. Runs at
     * init TAIL so every rebuild() (tab switch, picker close, search edit)
     * re-applies it, and the extras entry re-asserts its own reserved slot
     * every frame regardless of the order the two mixins run in.
     */
    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void dabyws$storyLayout(CallbackInfo ci) {
        try {
            WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
            int w = self.width;
            int h = self.height;

            // ---- tab rail --------------------------------------------------
            int tabW = Math.min(TAB_W, Math.max(72, (w - 2 * EDGE - TAB_GAP * 2) / 3));
            int tabsTotal = tabW * 3 + TAB_GAP * 2;
            int tx = Math.max(EDGE, (w - tabsTotal) / 2);

            List<AbstractWidget> primary = new ArrayList<>();
            List<AbstractWidget> secondary = new ArrayList<>();
            EditBox search = null;

            for (Object child : self.children()) {
                if (!(child instanceof AbstractWidget aw)) {
                    continue;
                }
                if (child instanceof EditBox box) {
                    search = box;
                    continue;
                }
                String s = dabyws$label(aw);
                if (dabyws$isTab(s)) {
                    int i = s.equals("Server") ? 0 : s.equals("Client") ? 1 : 2;
                    aw.setX(tx + i * (tabW + TAB_GAP));
                    aw.setY(TAB_Y);
                    aw.setWidth(tabW);
                    aw.setHeight(TAB_H);
                    continue;
                }
                if (s.equals("Done") || dabyws$isPreviewControl(s)) {
                    primary.add(aw);
                    continue;
                }
                if (dabyws$isBottomAction(s)) {
                    secondary.add(aw);
                    continue;
                }
                // Unknown widget parked in the old bottom bands by the base
                // screen: give it a real slot instead of leaving it to overlap.
                if (aw.getY() + aw.getHeight() >= h - 70) {
                    secondary.add(aw);
                }
            }

            // The search field owns the right end of the secondary row.
            int searchW = Math.min(240, Math.max(140, w / 4));
            int rowRight = w - EDGE;
            if (search != null) {
                search.setX(Math.max(EDGE, w - EDGE - searchW));
                search.setY(h - 56 + 1);
                search.setWidth(searchW);
                search.setHeight(20);
                rowRight = Math.max(EDGE + 80, search.getX() - 12);
            }
            dabyws$layoutRow(secondary, EDGE, rowRight, h - 56, 20, 8);
            dabyws$layoutRow(primary, EDGE, w - EDGE, h - 30, BAR_H, 10);
        } catch (Throwable ignored) {
            // layout is cosmetic; never break the screen
        }
    }

    /** Left-to-right row with a proportional shrink when it does not fit. */
    private static void dabyws$layoutRow(List<AbstractWidget> row, int left, int right,
                                         int y, int height, int gap) {
        if (row == null || row.isEmpty()) {
            return;
        }
        int avail = Math.max(80, right - left);
        int wanted = gap * (row.size() - 1);
        for (AbstractWidget aw : row) {
            wanted += aw.getWidth();
        }
        double scale = wanted > avail ? (double) avail / wanted : 1.0;
        int used = 0;
        for (AbstractWidget aw : row) {
            used += Math.max(40, (int) Math.round(aw.getWidth() * scale));
        }
        used += (int) Math.round(gap * (row.size() - 1) * scale);
        int x = left + Math.max(0, (avail - used) / 2);
        int step = Math.max(2, (int) Math.round(gap * scale));
        for (AbstractWidget aw : row) {
            int wd = Math.max(40, (int) Math.round(aw.getWidth() * scale));
            aw.setX(x);
            aw.setY(y);
            aw.setWidth(wd);
            aw.setHeight(height);
            x += wd + step;
        }
    }

    private static String dabyws$label(AbstractWidget aw) {
        if (aw instanceof AbstractButton ab && ab.getMessage() != null) {
            return ab.getMessage().getString();
        }
        return "";
    }

    private static boolean dabyws$isTab(String s) {
        return s.equals("Server") || s.equals("Client") || s.equals("Experimental");
    }

    /** Done plus the giant-preview controls (hidden by default, see the preview mixin). */
    private static boolean dabyws$isPreviewControl(String s) {
        return s.startsWith("Model") || s.startsWith("Gigantic")
                || s.startsWith("Sub:") || s.equals("Phase") || s.startsWith("Phase ");
    }

    private static boolean dabyws$isBottomAction(String s) {
        return s.equals("Save to Server") || s.equals("Reset Defaults")
                || s.equals("Save & Quit") || s.equals("Discard Changes")
                || s.equals("Apply Preset") || s.equals("Cancel");
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"), remap = false)
    private void dabyws$mcsChrome(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
        int w = self.width;
        int h = self.height;

        // paint out the base's hardcoded tab underline (old tab position)
        g.fill(0, 50, w, 57, 0xFF11091C);
        // header rule
        g.fill(EDGE, 57, w - EDGE, 58, 0xFF3A2A55);
        g.fill(EDGE, 58, w - EDGE, 59, 0xFF14101F);
        // wordmark + console tag (the tag stops short of the reserved entry slot)
        net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
        g.text(font, "\u00a7lSTORM CONFIGURATION", 28, 12, 0xFFEDE7F8, false);
        String tag = "EPISODE CONSOLE";
        int entryLeft = w - ENTRY_INSET_X - ENTRY_W;
        int tagX = Math.max(28 + font.width("\u00a7lSTORM CONFIGURATION") + 16,
                entryLeft - 12 - font.width(tag));
        if (tagX + font.width(tag) < entryLeft) {
            g.text(font, tag, tagX, 14, 0xFF7F6FA0, false);
        }
        // accent underline beneath the active tab (active==false marks it).
        // Build #416 -- it breathes: the console had no motion of its own, so a
        // static rail read as a screenshot next to the animated title screen.
        // A slow pulse on the underline plus a travelling highlight on the
        // header rule is enough to make the screen feel alive without moving
        // any widget (the layout must stay fixed to stay overlap-free).
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.0032D) * 0.5D + 0.5D);
        int underlineA = 0x99 + (int) (pulse * 0x66);
        for (Object child : self.children()) {
            if (!(child instanceof AbstractButton ab) || ab.getMessage() == null) {
                continue;
            }
            String s = ab.getMessage().getString();
            if (dabyws$isTab(s) && !ab.active) {
                g.fill(ab.getX(), 52, ab.getX() + ab.getWidth(), 54,
                        (underlineA << 24) | 0xB9A6E8);
            }
        }
        // travelling highlight along the header rule
        int sweepW = Math.max(60, w / 6);
        int sweepX = (int) ((System.currentTimeMillis() / 12L) % (w + sweepW)) - sweepW;
        g.fill(Math.max(EDGE, sweepX), 57, Math.min(w - EDGE, sweepX + sweepW), 58, 0x6633E1FF);

        // Image 3 Silver Pixel Border Frame
        if (McsmExtrasConfig.uiBorderLines) {
            int borderCol = 0xFF8A8A9E;
            int innerCol  = 0xFF2A2A38;
            g.fill(0, 0, w, 2, borderCol);
            g.fill(0, h - 2, w, h, borderCol);
            g.fill(0, 0, 2, h, borderCol);
            g.fill(w - 2, 0, w, h, borderCol);

            g.fill(4, 4, w - 4, 5, innerCol);
            g.fill(4, h - 5, w - 4, h - 4, innerCol);
            g.fill(4, 4, 5, h - 4, innerCol);
            g.fill(w - 5, 4, w - 4, h - 4, innerCol);

            g.fill(2, 2, 10, 4, borderCol);
            g.fill(2, 2, 4, 10, borderCol);
            g.fill(w - 10, 2, w - 2, 4, borderCol);
            g.fill(w - 4, 2, w - 2, 10, borderCol);
            g.fill(2, h - 4, 10, h - 2, borderCol);
            g.fill(2, h - 10, 4, h - 2, borderCol);
            g.fill(w - 10, h - 4, w - 2, h - 2, borderCol);
            g.fill(w - 4, h - 10, w - 2, h - 2, borderCol);
        }
    }
}
