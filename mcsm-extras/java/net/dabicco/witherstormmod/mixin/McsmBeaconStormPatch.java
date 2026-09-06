package net.dabicco.witherstormmod.mixin;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmStormFx;
import net.mcsm.extras.McsmStormBeaconBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCSM - the storm beacon.
 *
 * Beacontown's beacon is the summon antenna: the moment a lit beacon's beam
 * sections come up (activation edge, not every tick), the beacon fires a
 * shockwave ring and hands the position to the mod's own summon path
 * (WitherStormSummon.trySpawn - the only summon route). If a full skull
 * formation is not present, an already-awake storm within 96 blocks is
 * provoked into a tentacle slam instead: every lit beacon out in the world
 * becomes a beacon-town style relay for the storm.
 */
@Mixin(BeaconBlockEntity.class)
public class McsmBeaconStormPatch {

    private static final Map<BlockPos, Boolean> MCSM$LAST = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Long> MCSM$CD = new ConcurrentHashMap<>();

    @Inject(method = {"tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;)V"},
            at = @At("TAIL"))
    private static void mcsm$shockwave(Level world, BlockPos pos, BlockState state, BeaconBlockEntity beacon, CallbackInfo ci) {
        McsmExtrasConfig.load();
        if (world.isClientSide() || !McsmExtrasConfig.enableBeaconStorm) return;
        boolean active = !beacon.getBeamSections().isEmpty();
        Boolean prev = MCSM$LAST.put(pos, active);
        if (!active || Boolean.TRUE.equals(prev)) return;
        long gt = world.getGameTime();
        long cool = (long) (McsmExtrasConfig.beaconCooldownSeconds * 20.0);
        Long last = MCSM$CD.get(pos);
        if (last != null && gt - last < cool) return;
        MCSM$CD.put(pos, gt);

        McsmStormFx.fire(world, pos);
    }
}
