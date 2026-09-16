package net.dabicco.witherstormmod.mixin;

import java.util.Map;
import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.sounds.SoundManager;

import net.mcsm.extras.client.McsmButtonSounds;

/**
 * Build #416: Devouring Storms button audio, applied to <em>every</em>
 * control in the game rather than only the overhauled title screen.
 *
 * Up to now the custom sounds were wired by hand into
 * {@code McsmTitleOverhaulMixin} alone, so the config console, the extras
 * panel, pause menu, world lists and every vanilla screen still had either
 * the stock click or (after the title screen replaced its controls) silence.
 *
 * Two vanilla funnels are captured instead:
 * <ul>
 *   <li>{@code AbstractWidget.playDownSound(SoundManager)} -- the press
 *       funnel every button/slider uses, cancelled so the stock click and
 *       our chime never stack,</li>
 *   <li>{@code AbstractWidget.playButtonClickSound(SoundManager)} -- the
 *       static helper this Minecraft version uses for direct calls.</li>
 * </ul>
 * The hover tink is driven off {@code extractRenderState}, tracking hover
 * transitions per widget in a weak map so a hot control ticks once, not once
 * per frame.
 *
 * {@code require = 0} on purpose: if a future client moves either funnel, this
 * mixin degrades to "no custom sound" instead of refusing to launch.
 */
@Mixin({ AbstractWidget.class, AbstractSliderButton.class })
public abstract class McsmButtonSoundHookMixin {

    private static final Map<Object, Boolean> MCSM$HOVERED = new WeakHashMap<>();

    @Inject(method = "playDownSound", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcsm$customPressSound(SoundManager soundManager, CallbackInfo ci) {
        McsmButtonSounds.click();
        ci.cancel();
    }

    @Inject(method = "playButtonClickSound", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mcsm$customPressSoundStatic(SoundManager soundManager, CallbackInfo ci) {
        McsmButtonSounds.click();
        ci.cancel();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), require = 0)
    private void mcsm$hoverSound(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                 float partialTick, CallbackInfo ci) {
        try {
            AbstractWidget self = (AbstractWidget) (Object) this;
            boolean hovered = self.active && self.visible && self.isHovered();
            if (!hovered) {
                MCSM$HOVERED.remove(self);
                return;
            }
            if (!Boolean.TRUE.equals(MCSM$HOVERED.put(self, Boolean.TRUE))) {
                McsmButtonSounds.hover();
            }
        } catch (Throwable ignored) {
            // hover audio is cosmetic only
        }
    }
}
