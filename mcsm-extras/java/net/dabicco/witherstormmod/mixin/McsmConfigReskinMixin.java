package net.dabicco.witherstormmod.mixin;

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
 * Devouring Storms: WitherStormConfigScreen - the full Story-Mode console
 * revamp (Build #383, user order: "this whole place completely revamped,
 * a completely new look", buttons sideways like Story Mode, nothing
 * scrambled or covering anything, no preview button in the middle).
 *
 * Layout:
 *  - episodic header band: wordmark left, console tag right, hairline rule;
 *  - the three tabs become a centred rail of wide horizontal buttons;
 *  - one clean bottom bar: [Save to Server] [Reset Defaults] [Done] and the
 *    search field right-aligned - every widget on a single row, no overlap;
 *  - the base's hardcoded tab underline (drawn at the old tab position) is
 *    painted out and replaced by an accent underline under the active tab;
 *  - the giant 3D preview stays removed by default (McsmPreviewRemoveMixin);
 *    if the console toggle re-enables it, its controls keep their row.
 *
 * The old Build #371 silver pixel border frame is retained.
 */
@Mixin(WitherStormConfigScreen.class)
public abstract class McsmConfigReskinMixin {

    private static final int TAB_W = 150;
    private static final int TAB_H = 24;
    private static final int TAB_GAP = 12;
    private static final int TAB_Y = 26;
    private static final int BAR_H = 26;

    @Inject(method = "extractRenderState", at = @At("HEAD"), remap = false)
    private void dabyws$mcsPlate(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
        int w = self.width;
        int h = self.height;
        g.fillGradient(0, 0, w, h, 0xFF120A1E, 0xFF05030A);
        g.fillGradient(0, h * 3 / 4, w, h, 0x00000000, 0x443F255A);
        // side panels
        g.fillGradient(0, 0, 18, h, 0xAA0A0612, 0x22140622);
        g.fillGradient(w - 18, 0, w, h, 0x22140622, 0xAA0A0612);
        // bottom bar band (widgets draw on top of this later in the frame)
        g.fillGradient(0, h - 44, w, h, 0x0005030A, 0xCC0A0614);
        g.fill(0, h - 44, w, h - 43, 0xFF2A2A38);
    }

    /**
     * Build #383: reposition the chrome into the Story-Mode console layout.
     * Runs at init TAIL so every rebuild() (tab switch, picker close, search
     * edit) re-applies it. Picker overlays add none of these labels, so they
     * are untouched. Label matching uses the public AbstractWidget message
     * API (verified in ci/api/client.txt).
     */
    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void dabyws$storyLayout(CallbackInfo ci) {
        try {
            WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
            int w = self.width;
            int h = self.height;
            int total = TAB_W * 3 + TAB_GAP * 2;
            int tx = (w - total) / 2;
            int by = h - 36;
            int doneW = 220;
            int doneX = (w - doneW) / 2;
            for (Object child : self.children()) {
                if (!(child instanceof AbstractWidget aw)) {
                    continue;
                }
                String s = "";
                if (child instanceof AbstractButton ab && ab.getMessage() != null) {
                    s = ab.getMessage().getString();
                }
                if (s.equals("Server") || s.equals("Client") || s.equals("Experimental")) {
                    int i = s.equals("Server") ? 0 : s.equals("Client") ? 1 : 2;
                    aw.setX(tx + i * (TAB_W + TAB_GAP));
                    aw.setY(TAB_Y);
                    aw.setWidth(TAB_W);
                    aw.setHeight(TAB_H);
                } else if (s.equals("Done")) {
                    aw.setX(doneX);
                    aw.setY(by);
                    aw.setWidth(doneW);
                    aw.setHeight(BAR_H);
                } else if (s.equals("Reset Defaults")) {
                    aw.setX(doneX - 10 - 170);
                    aw.setY(by);
                    aw.setWidth(170);
                    aw.setHeight(BAR_H);
                } else if (s.equals("Save to Server")) {
                    aw.setX(doneX - 20 - 340);
                    aw.setY(by);
                    aw.setWidth(170);
                    aw.setHeight(BAR_H);
                } else if (s.startsWith("Model") || s.startsWith("Gigantic")) {
                    int i = s.startsWith("Model") ? 0 : 1;
                    aw.setX(doneX + doneW + 10 + i * 120);
                    aw.setY(by);
                    aw.setWidth(110);
                    aw.setHeight(BAR_H);
                } else if (child instanceof EditBox eb) {
                    eb.setX(w - 28 - 260);
                    eb.setY(by + 1);
                    eb.setWidth(260);
                    eb.setHeight(24);
                }
            }
        } catch (Throwable ignored) {
            // layout is cosmetic; never break the screen
        }
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
        g.fill(24, 57, w - 24, 58, 0xFF3A2A55);
        g.fill(24, 58, w - 24, 59, 0xFF14101F);
        // wordmark + console tag
        g.text(self.font, "\u00a7lSTORM CONFIGURATION", 28, 12, 0xFFEDE7F8, false);
        String tag = "EPISODE CONSOLE";
        g.text(self.font, tag, w - 28 - self.font.width(tag), 14, 0xFF7F6FA0, false);
        // accent underline beneath the active tab (active==false marks it)
        for (Object child : self.children()) {
            if (!(child instanceof AbstractButton ab) || ab.getMessage() == null) {
                continue;
            }
            String s = ab.getMessage().getString();
            if ((s.equals("Server") || s.equals("Client") || s.equals("Experimental")) && !ab.active) {
                g.fill(ab.getX(), 52, ab.getX() + ab.getWidth(), 54, 0xFFB9A6E8);
            }
        }

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
