package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.mcsm.extras.McsmBuiltinPack;
import net.mcsm.extras.McsmCities;
import net.mcsm.extras.McsmContent;
import net.mcsm.extras.McsmUiSounds;
import net.mcsm.extras.entity.McsmEntities;

/**
 * Registers the built-in Story Look resource pack during the mod's own
 * initialization (target and method name verified against the compiled
 * base mod), so the pack is known to the pack repository before its first
 * reload and comes up enabled by default.
 */
@Mixin(DabyWitherStormMod.class)
public abstract class McsmBuiltinPackMixin {

    @Inject(method = "onInitialize", at = @At("HEAD"), remap = false)
    private void dabyws$builtinPack(CallbackInfo ci) {
        McsmBuiltinPack.register();
        // 1.9.205 -- Story Mode character entity type + attributes.
        McsmEntities.register();
        // Build #416 -- UI sound events, registered here while the built-in
        // registries are still open (their lazy registration used to throw and
        // leave every menu silent).
        McsmUiSounds.initialize();
        // Build #416 (D.8) -- the Decayed Reality content pack: 35 blocks, 54
        // items, doors, trap doors, the new weapons and our own creative tab,
        // registered here because this is the only window in which the
        // built-in registries are still open (the same reason the base mod's
        // own ModBlocks/ModItems pass runs from onInitialize).
        McsmContent.register();
        McsmContent.registerTab();
        // Build #416 (D.8, phase 2) -- the abandoned cities of the decayed
        // reality. ServerTickEvents.END_LEVEL_TICK is the base mod's own hook
        // (DabyWitherStormMod registers McsmWorldgen.tick on it), so this needs
        // no mixin and no new API surface at all.
        McsmCities.register();
    }
}
