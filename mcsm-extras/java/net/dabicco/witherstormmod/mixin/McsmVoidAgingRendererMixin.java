package net.dabicco.witherstormmod.mixin;

import net.mcsm.extras.client.McsmVoidAgingClient;
import net.mcsm.extras.client.McsmVoidAgingState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BUILD #463 -- THE PLAYER MODEL BECOMES THE VOID. "the player model slowly
 * becomes the corrupted void".
 *
 * <p>This is the visible half of void aging, and it is the body rather than an
 * overlay: the two hooks are the ones this version's own wither-sickness layer
 * uses, so the injections are proven against the same client --
 *
 * <pre>
 *   extractRenderState(...)TAIL   copy the client's 0..1 value onto the state
 *   getModelTint(...)RETURN       pull that state's model colour toward the void
 * </pre>
 *
 * <p>{@code getModelTint} hands back the colour this renderer would have used, so
 * the void is a GRADE on the player's own skin rather than a second skin: red and
 * green are eaten first (the veil), blue holds and then climbs (the violet), and
 * a little of the void's own green creeps in only at the very end -- the same
 * two-colour ramp the dimension itself is lit with (see {@code McsmIdentity.VOID}).
 * Age 0 is a no-op, so an untouched player is exactly the player they were.
 */
@Mixin({LivingEntityRenderer.class})
public abstract class McsmVoidAgingRendererMixin {

    @Inject(
            method = {"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
                    + "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"},
            at = {@At("TAIL")},
            require = 0
    )
    private void mcsm$voidAge(LivingEntity entity, LivingEntityRenderState state,
                              float partialTick, CallbackInfo ci) {
        try {
            if (entity == null || !(state instanceof McsmVoidAgingState aged)) {
                return;
            }
            aged.mcsm$setVoidAge(McsmVoidAgingClient.ageOf(entity.getId()));
        } catch (Throwable ignored) {
            // a body that cannot be aged is a body that keeps its own colour
        }
    }

    @Inject(
            method = {"getModelTint(Lnet/minecraft/client/renderer/entity/state/"
                    + "LivingEntityRenderState;)I"},
            at = {@At("RETURN")},
            cancellable = true,
            require = 0
    )
    private void mcsm$voidTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        try {
            if (!(state instanceof McsmVoidAgingState aged)) {
                return;
            }
            float age = aged.mcsm$voidAge();
            if (age <= 0.0F) {
                return;
            }
            int argb = cir.getReturnValue();
            int a = (argb >>> 24) & 0xFF;
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            // the veil: most of the red and the green go first
            r = (int) (r * (1.0F - 0.74F * age));
            g = (int) (g * (1.0F - 0.66F * age));
            // the violet holds, and at the end it is the loudest thing left
            b = (int) Math.min(255.0F, b * (1.0F - 0.30F * age) + 46.0F * age * age);
            // and the void's own green only arrives at the last stage
            if (age > 0.75F) {
                float late = (age - 0.75F) / 0.25F;
                g = (int) Math.min(255.0F, g + 34.0F * late);
            }
            cir.setReturnValue((a << 24) | (Math.max(0, Math.min(255, r)) << 16)
                    | (Math.max(0, Math.min(255, g)) << 8) | Math.max(0, Math.min(255, b)));
        } catch (Throwable ignored) {
            // the vanilla tint stands
        }
    }
}
