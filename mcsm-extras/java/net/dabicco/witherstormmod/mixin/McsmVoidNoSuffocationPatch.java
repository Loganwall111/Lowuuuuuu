package net.dabicco.witherstormmod.mixin;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidDescent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BUILD #485 -- COMPLETELY DISABLE SUFFOCATING IN VOID ENTIRELY
 * User request: option to completely disable suffocating in void entirely.
 * True = no IN_WALL damage, no drowning, air always full, blocks cleared around player.
 * Implemented as mixin to avoid Fabric API entity event dependency (26.2 API changed).
 */
@Mixin(LivingEntity.class)
public abstract class McsmVoidNoSuffocationPatch {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void mcsm$noSuffocation(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (!McsmExtrasConfig.voidNoSuffocation) return;
            LivingEntity self = (LivingEntity)(Object)this;
            if (!(self instanceof ServerPlayer player)) return;

            // Check if in void dimension or in overworld deep void when merged
            boolean inVoid = player.level().dimension().equals(McsmVoid.DIMENSION);
            boolean inOverworldDeep = false;
            if (McsmExtrasConfig.voidMerged) {
                String dim = player.level().dimension().location().toString();
                if (dim.equals("minecraft:overworld") && player.getY() < player.level().getMinY() + 50) {
                    inOverworldDeep = true;
                }
                if (dim.equals("minecraft:overworld") && McsmVoidDescent.diving(player)) {
                    inOverworldDeep = true;
                }
            }
            if (!inVoid && !inOverworldDeep) return;

            // Check damage type via msgId to avoid DamageTypes class dependency
            String msgId = "";
            try {
                msgId = source.type().msgId();
            } catch (Throwable t) {
                try {
                    msgId = source.getMsgId();
                } catch (Throwable t2) {
                    msgId = source.toString();
                }
            }
            if (msgId == null) msgId = "";
            String lower = msgId.toLowerCase();

            // Disable suffocation, drowning, fall, out_of_world when noSuffoc enabled
            if (lower.contains("inwall") || lower.contains("in_wall") || lower.contains("suffocation") || lower.contains("inside")) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
            if (lower.contains("drown")) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
            if (lower.contains("fall")) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
            if (McsmExtrasConfig.voidMerged && (lower.contains("outofworld") || lower.contains("out_of_world") || lower.contains("void") || lower.contains("fell"))) {
                // For merged hundreds blocks fall, also disable out of world during diving
                if (McsmVoidDescent.diving(player) || McsmExtrasConfig.voidNoSuffocation) {
                    cir.setReturnValue(false);
                    cir.cancel();
                    return;
                }
            }
        } catch (Throwable ignored) {
            // never break damage path
        }
    }
}
