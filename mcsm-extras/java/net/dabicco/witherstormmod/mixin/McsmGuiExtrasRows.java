package net.dabicco.witherstormmod.mixin;

import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.client.McsmExtrasScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * MCSM - the Devouring Storms entry inside the mod's own config console.
 *
 * Build #416 restructure. The #383 generation is the reason the user reported
 * "the new settings pop up ... I click any button on there and it just reopens
 * the new config menu": it injected a {@code WitherStormConfigScreen$Row} whose
 * button lives in the SCROLLABLE rows list. Rows are laid out by
 * {@code repositionRows()} inside the content view, and the console's own
 * chrome (tabs at y=32, the Save/Reset/Model/Done bar at the bottom) is placed
 * by hardcoded coordinates in {@code init()}. Whenever the row list grew past
 * the view -- which the extras row itself did, being appended last -- that
 * button stayed live and clickable outside the view, i.e. invisible, sitting
 * on top of the bottom bar. Every click near the bottom of the console hit the
 * invisible extras button and reopened the panel. No layout pass can be sure
 * of a row.
 *
 * The entry is now a CHROME widget in a RESERVED slot: the top-right corner,
 * which the console reskin (McsmConfigReskinMixin) deliberately keeps clear
 * (it right-aligns its "EPISODE CONSOLE" tag to the left of this slot, and
 * nothing else is ever placed there). It is added through the console's own
 * {@code addChrome}/{@code addWidget} so it renders and receives clicks in the
 * normal widget pass, and re-asserted once per frame so a resize or a tab
 * rebuild cannot drift it.
 *
 * Anti-stacking: clicking it while a panel is already open does nothing, and
 * the panel itself never returns to this screen (see McsmExtrasScreen's
 * constructor). One console, one entry, no ping-pong.
 *
 * Fully silent on failure: a future refactor of their GUI costs us the entry
 * point (the Shift+C hotkey still opens the panel), never a crash.
 */
@Mixin(WitherStormConfigScreen.class)
public abstract class McsmGuiExtrasRows {

    /** Recognised by the reskin layout by this prefix -- keep the two in sync. */
    private static final String MCSM$ENTRY_PREFIX = "Devouring Storms Control Panel";
    /** Reserved top-right slot: width 214, height 20, inset 26/8 (see reskin). */
    private static final int MCSM$ENTRY_W = 214;
    private static final int MCSM$ENTRY_H = 20;
    private static final int MCSM$ENTRY_INSET_X = 26;
    private static final int MCSM$ENTRY_Y = 8;

    @Inject(method = {"init"}, at = @At("TAIL"))
    private void mcsm$extrasEntry(CallbackInfo ci) {
        try {
            McsmExtrasConfig.load();
            final Screen self = (Screen) (Object) this;
            if (mcsm$findEntry(self) != null) {
                return;
            }
            Button entry = Button.builder(
                    Component.literal(MCSM$ENTRY_PREFIX + " \u00a7d" + McsmExtrasConfig.BUILD_VERSION),
                    b -> mcsm$openPanel(self))
                    .bounds(0, 0, MCSM$ENTRY_W, MCSM$ENTRY_H).build();
            entry.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(
                    "Full Story Mode control panel: atmosphere, shaders, NPCs, storm VFX, world/story toggles.")));
            mcsm$addChrome(self, entry);
            mcsm$placeEntry(self, entry);
        } catch (Throwable t) {
            System.err.println("[MCSM] extras entry skipped: " + t);
        }
    }

    /**
     * Keep the entry in its reserved slot. Runs before the widget pass draws,
     * so a resize or a tab rebuild can never leave it mid-layout.
     */
    @Inject(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
        at = @At("HEAD")
    )
    private void mcsm$keepEntryPlaced(GuiGraphicsExtractor g, int mouseX, int mouseY,
                                      float partialTick, CallbackInfo ci) {
        try {
            Screen self = (Screen) (Object) this;
            AbstractWidget entry = mcsm$findEntry(self);
            if (entry != null) {
                mcsm$placeEntry(self, entry);
            }
        } catch (Throwable ignored) {
            // cosmetic only
        }
    }

    private static void mcsm$placeEntry(Screen self, AbstractWidget entry) {
        entry.setX(Math.max(8, self.width - MCSM$ENTRY_INSET_X - MCSM$ENTRY_W));
        entry.setY(MCSM$ENTRY_Y);
        entry.setWidth(MCSM$ENTRY_W);
        entry.setHeight(MCSM$ENTRY_H);
    }

    private static AbstractWidget mcsm$findEntry(Screen self) {
        for (Object child : self.children()) {
            if (child instanceof AbstractWidget aw && mcsm$isEntry(aw)) {
                return aw;
            }
        }
        return null;
    }

    private static boolean mcsm$isEntry(AbstractWidget aw) {
        if (!(aw instanceof Button)) {
            return false;
        }
        Component msg = ((Button) aw).getMessage();
        return msg != null && msg.getString().startsWith(MCSM$ENTRY_PREFIX);
    }

    /** The console's own addChrome(AbstractWidget) -- widget list + children. */
    private static void mcsm$addChrome(Screen self, AbstractWidget widget) throws Exception {
        Method addChrome = WitherStormConfigScreen.class
                .getDeclaredMethod("addChrome", AbstractWidget.class);
        addChrome.setAccessible(true);
        addChrome.invoke(self, widget);
    }

    private static void mcsm$openPanel(Screen self) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return;
            }
            // Never stack a second panel, and never re-open from inside one.
            if (mc.gui != null && mc.gui.screen() instanceof McsmExtrasScreen) {
                return;
            }
            mc.setScreenAndShow(new McsmExtrasScreen(self));
            System.err.println("[MCSM] extras panel opened from the config console");
        } catch (Throwable t) {
            System.err.println("[MCSM] extras panel open failed: " + t);
        }
    }
}
