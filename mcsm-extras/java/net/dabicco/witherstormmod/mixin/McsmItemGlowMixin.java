package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.mcsm.extras.McsmDynamicLights;

/**
 * Devouring Storms: coloured dynamic lights, phase 1.
 *
 * Dropped luminous items breathe coloured dust motes - a flint &amp; steel
 * drops struck-steel blue sparks, a torch burns orange, soul fire cyan,
 * glowstone gold. Target and every call below are verified against the
 * 26.2 client jar (ItemEntity#tick from the CI probe; addParticle /
 * DustParticleOptions(int, float) / isClientSide() / tickCount / getRandom
 * copied from the base mod's own compiled code).
 *
 * Phase 2 (real lightmap injection so the glow lights the ground) follows
 * once the LevelRenderer light surface comes back from the probe.
 */
@Mixin(ItemEntity.class)
public abstract class McsmItemGlowMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void dabyws$colouredGlow(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!self.level().isClientSide() || self.tickCount % 3 != 0) {
            return;
        }
        ItemStack stack = self.getItem();
        int color = McsmDynamicLights.glowColor(stack);
        if (color == 0) {
            return;
        }
        double x = self.getX();
        double y = self.getY() + 0.15D;
        double z = self.getZ();
        DustParticleOptions dust = new DustParticleOptions(color, 0.9F);
        for (int i = 0; i < 2; i++) {
            self.level().addParticle(dust,
                    x + (self.getRandom().nextDouble() - 0.5D) * 0.45D,
                    y + self.getRandom().nextDouble() * 0.35D,
                    z + (self.getRandom().nextDouble() - 0.5D) * 0.45D,
                    0.0D, 0.012D, 0.0D);
        }
    }
}
