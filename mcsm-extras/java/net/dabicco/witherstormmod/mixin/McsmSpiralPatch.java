package net.dabicco.witherstormmod.mixin;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmStormFx;
import net.mcsm.extras.McsmStormBeaconBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCSM - counterclockwise destruction spiral.
 *
 * The cluster entity that carries torn blocks into the storm already owns
 * both spiral directions: beginTraveling() picks one and the motion math
 * mirrors the orbit angle (dneg) when spiralClockwise is false. The mod
 * defaults the toss clockwise; this pins every cluster to the CCW branch so
 * the consumption spiral always winds counterclockwise, Story Mode style.
 * Toggle: spiral_counter_clockwise in config/mcsm_storm_extras.properties.
 */
@Mixin(net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity.class)
public abstract class McsmSpiralPatch {

    @Shadow
    private boolean spiralClockwise;

    @Inject(method = {"beginTraveling"}, at = @At("TAIL"))
    private void mcsm$pinCounterClockwise(CallbackInfo ci) {
        McsmExtrasConfig.load();
        if (McsmExtrasConfig.spiralCounterClockwise && this.spiralClockwise) {
            this.spiralClockwise = false;
        }
    }
}
