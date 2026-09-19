package net.dabicco.witherstormmod.mixin;

import net.mcsm.extras.client.McsmVoidAgingState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * BUILD #463 -- one float on the vanilla render state, and nothing else.
 *
 * <p>The same arrangement the base mod uses for its own wither-sickness tint
 * ({@code LivingEntityRenderStateMixin implements InfectionRenderState}): the
 * state is per entity, the renderer has it, and the tint can therefore be drawn
 * without looking anything up. The prefix is ours so the two never collide.
 */
@Mixin({LivingEntityRenderState.class})
public class McsmVoidAgingStateMixin implements McsmVoidAgingState {

    @Unique
    private float mcsm$voidAgingValue;

    public float mcsm$voidAge() {
        return this.mcsm$voidAgingValue;
    }

    public void mcsm$setVoidAge(float age) {
        this.mcsm$voidAgingValue = age;
    }
}
