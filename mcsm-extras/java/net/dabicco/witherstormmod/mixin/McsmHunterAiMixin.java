package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Build #374 -- ADVANCED WITHER AI ("hunts players" + tentacle grabs), the
 * user's standing request, built ONLY on proven public surface:
 *
 *   - lockRotationOn(Player)  : the storm's body turns to face its prey,
 *     every 6 ticks while a player is inside the 56-block hunt radius —
 *     the visible "it is watching you / coming for you" behaviour.
 *   - forceTentacleSlam()     : when the player closes inside 18 blocks,
 *     a tentacle slam is forced at the console's "Grab Interval" cadence.
 *
 * Gated by the existing "Enhanced Wither Storm AI" console toggle (server
 * side only; a client tick has no ServerPlayer list). The base AI already
 * chases (chase goal + standoffs from the private API dump); this layer
 * adds the relentless head-on hunt and the close-range grab without
 * touching any base-private state.
 */
@Mixin(WitherStormEntity.class)
public abstract class McsmHunterAiMixin {

    /** 56-block hunt radius (squared). */
    @Unique private static final double HUNT_RANGE_SQ = 56.0D * 56.0D;
    /** 18-block tentacle slam radius (squared). */
    @Unique private static final double SLAM_RANGE_SQ = 18.0D * 18.0D;

    /** Per-launch slam cadence guard (one active storm is the norm). */
    @Unique private static long dabyws$lastSlamTick = -999999L;

    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void dabyws$hunterMode(CallbackInfo ci) {
        try {
            Level level = this.level();
            if (level == null || level.isClientSide() || !(level instanceof ServerLevel)) {
                return;
            }
            McsmExtrasConfig.load();
            if (!McsmExtrasConfig.witherStormEnhancedAi) {
                return;
            }
            WitherStormEntity self = (WitherStormEntity) (Object) this;

            ServerPlayer prey = null;
            double best = Double.MAX_VALUE;
            // 26.2: players() lives on ServerLevel, not Level (dump-verified)
            for (ServerPlayer p : ((ServerLevel) level).players()) {
                double dx = p.getX() - self.getX();
                double dy = p.getY() - self.getY();
                double dz = p.getZ() - self.getZ();
                double d2 = dx * dx + dy * dy + dz * dz;
                if (d2 < best) {
                    best = d2;
                    prey = p;
                }
            }
            if (prey == null || best > HUNT_RANGE_SQ) {
                return;
            }

            // the body turns onto the prey every 6 ticks — it is hunting you
            if (self.tickCount % 6L == 0L) {
                self.lockRotationOn(prey);
            }

            // close range: a tentacle slam at the Grab Interval cadence
            if (best <= SLAM_RANGE_SQ) {
                double interval = Math.max(1.0D, McsmExtrasConfig.grabIntervalSeconds);
                long gt = level.getGameTime();
                if (gt - dabyws$lastSlamTick >= interval * 20.0D) {
                    dabyws$lastSlamTick = gt;
                    self.forceTentacleSlam();
                }
            }
        } catch (Throwable ignored) {
            // the hunt must never break an entity tick
        }
    }
}
