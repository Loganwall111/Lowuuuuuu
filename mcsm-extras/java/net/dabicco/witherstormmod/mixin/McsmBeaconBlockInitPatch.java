package net.dabicco.witherstormmod.mixin;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmStormFx;
import net.mcsm.extras.McsmStormBeaconBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers the MCSM storm beacon block inside the mod's own block
 *  bootstrap, so it lands in their registry window with zero extra mods. */
@Mixin(net.dabicco.witherstormmod.ModBlocks.class)
public class McsmBeaconBlockInitPatch {

    @Inject(method = {"initialize"}, at = @At("TAIL"))
    private static void mcsm$registerBeacon(CallbackInfo ci) {
        try {
            McsmStormBeaconBlock.mcsm$register();
        } catch (Throwable t) {
            System.err.println("[MCSM] storm beacon registration skipped: " + t);
        }
    }
}
