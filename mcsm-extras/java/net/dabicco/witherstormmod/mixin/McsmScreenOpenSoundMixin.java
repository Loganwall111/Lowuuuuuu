package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import net.mcsm.extras.client.McsmButtonSounds;

/**
 * Build #416: one rising menu whoosh whenever a new screen unfolds.
 *
 * The old helper only played the open sound from the title screen, so the
 * config console, extras panel and every world/options screen opened in
 * silence. This hooks {@code Screen.init()} -- the one method every screen
 * runs to build its widgets -- and stays quiet when:
 * <ul>
 *   <li>the screen is the title screen (that one drives its own open
 *       sound),</li>
 *   <li>the same screen class re-inits within a moment of itself, i.e. a
 *       window resize or an internal relayout, not a real "menu opened".</li>
 * </ul>
 */
@Mixin(Screen.class)
public abstract class McsmScreenOpenSoundMixin {

    private static Class<?> MCSM$LAST_SCREEN;
    private static long MCSM$LAST_MS;

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void mcsm$menuOpenSound(CallbackInfo ci) {
        try {
            Screen self = (Screen) (Object) this;
            if (self instanceof TitleScreen) {
                return;
            }
            long now = System.currentTimeMillis();
            Class<?> cls = self.getClass();
            boolean sameScreenAgain = cls == MCSM$LAST_SCREEN && now - MCSM$LAST_MS < 700L;
            boolean tooSoon = now - MCSM$LAST_MS < 260L;
            MCSM$LAST_SCREEN = cls;
            MCSM$LAST_MS = now;
            if (sameScreenAgain || tooSoon) {
                return;
            }
            McsmButtonSounds.menuOpen();
        } catch (Throwable ignored) {
            // sound only
        }
    }
}
