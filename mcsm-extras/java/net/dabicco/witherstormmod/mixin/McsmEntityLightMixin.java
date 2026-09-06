package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.mcsm.extras.McsmDynamicLights;

/**
 * Devouring Storms: coloured dynamic lights, phase 2a.
 *
 * A dropped luminous item now renders as if it carries its own light: the
 * packed light coordinates used for entity rendering get their block-light
 * nibble raised to the item's glow level (torch 14, glowstone 15, emerald
 * 9...). Target verified by the CI probe against the 26.2 client jar:
 * EntityRenderDispatcher#getPackedLightCoords(Entity, float).
 *
 * Packed layout is vanilla's (block light nibble at bits 4..7, sky at
 * 20..23); only the block nibble is raised and only upward, so vanilla
 * lighting elsewhere is untouched.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class McsmEntityLightMixin {

    @Inject(method = "getPackedLightCoords", at = @At("RETURN"))
    private void dabyws$glowingDropLightsItself(Entity entity, float partialTick,
            CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof ItemEntity)) {
            return;
        }
        ItemStack stack = ((ItemEntity) entity).getItem();
        int light = McsmDynamicLights.lightLevel(stack);
        if (light <= 0) {
            return;
        }
        int packed = cir.getReturnValue();
        int block = (packed >> 4) & 0xF;
        if (block < light) {
            packed = (packed & ~(0xF << 4)) | (light << 4);
            cir.setReturnValue(packed);
        }
    }
}
