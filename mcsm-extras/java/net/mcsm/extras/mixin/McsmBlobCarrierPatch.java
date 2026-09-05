package net.mcsm.extras.mixin;

import net.dabicco.witherstormmod.client.StormSkyGradient;
import net.mcsm.extras.McsmDiag;
import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCSM 1.9.71 -- fixes the missing black glare blob.
 *
 * The blob never rendered because the storm direction could not survive the
 * trip to the shader. The mod's own McsmFogCarrierMixin packs yaw+pitch into
 * FogData.cloudEnd as:
 *
 *     cloudEnd = 1200 + (yaw+180)*2 + (pitch+90)*0.5
 *
 * That is NOT invertible. The pitch term spans [0,90) while the yaw term steps
 * by 2, so 45 distinct (yaw,pitch) pairs collide on the same value -- verified
 * by brute force over the whole angle domain. Our decoder therefore recovered
 * garbage: pitch pinned to -90 (straight down) in almost every case and yaw off
 * by up to 34 degrees, so mcsm_blob() drew the glare below the world.
 *
 * Fix: re-stamp cloudEnd at TAIL (after the mod's HEAD write) with a strictly
 * invertible integer packing:
 *
 *     cloudEnd = 3000 + yawIdx*181 + pitchIdx
 *     yawIdx   = round(yaw)   + 180   in [0,360]
 *     pitchIdx = round(pitch) +  90   in [0,180]
 *
 * pitchIdx < 181 guarantees uniqueness. Max value 68340, well inside float32's
 * exact-integer range (2^24), so nothing is lost in the uniform upload.
 * Verified exhaustively: 65341 angle pairs, zero round-trip mismatches.
 *
 * One degree of angular resolution is far finer than the blob's own angular
 * radius, so quantisation is invisible.
 *
 * The matching decoder lives in mcsm_boss_dir() in include/mcsm_visuals.glsl
 * and reads the 3000..68340 band. The legacy 1100..2150 band is still accepted
 * there so an unpatched carrier degrades instead of misplacing the blob.
 */
@Mixin(value = FogRenderer.class, priority = 1500)
public abstract class McsmBlobCarrierPatch {

    // FogRenderer.updateBuffer is OVERLOADED -- there is also a private
    // updateBuffer(ByteBuffer,int,Vector4f,F,F,F,F,F,F). A bare "updateBuffer"
    // is ambiguous, so the full descriptor is mandatory here. The mod's own
    // McsmFogCarrierMixin targets it the same way.
    // require = 1: if this ever stops matching we want a hard startup failure,
    // not a silently missing glare blob.
    @Inject(
        method = "updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V",
        at = @At("TAIL"),
        require = 1
    )
    private void mcsm$stampBlobCarrier(FogData data, CallbackInfo ci) {
        if (!StormSkyGradient.fogStampActive()) {
            return;
        }
        float p = StormSkyGradient.phase();
        if (p < 4.42F || p > 8.06F) {
            return;
        }
        float yaw = StormSkyGradient.yaw();
        float pitch = StormSkyGradient.pitch();

        // normalise yaw into [-180,180] before indexing
        yaw = yaw % 360.0F;
        if (yaw > 180.0F) {
            yaw -= 360.0F;
        }
        if (yaw < -180.0F) {
            yaw += 360.0F;
        }
        if (pitch > 90.0F) {
            pitch = 90.0F;
        }
        if (pitch < -90.0F) {
            pitch = -90.0F;
        }

        int yawIdx = Math.round(yaw) + 180;
        int pitchIdx = Math.round(pitch) + 90;
        if (yawIdx < 0) {
            yawIdx = 0;
        }
        if (yawIdx > 360) {
            yawIdx = 360;
        }
        if (pitchIdx < 0) {
            pitchIdx = 0;
        }
        if (pitchIdx > 180) {
            pitchIdx = 180;
        }

        // MCSM 1.9.98: WIDE carrier -- same invertible yaw/pitch payload,
        // multiplied by 16, with the user's glare-size index in the low nibble
        // (shader decodes sizeIdx 0..15 -> x0.50..x3.05). Max integer is
        // 68340*16+15 = 1093455 < 2^24, still exact in float32. The shader
        // accepts both encodings (wide band >= 47000), so an old jar-side
        // writer degrades gracefully to the 1.9.98 default size.
        McsmExtrasConfig.load();
        double size = McsmExtrasConfig.glareSize;
        int sizeIdx = (int) Math.round((size - 0.50) / 0.17);
        if (sizeIdx < 0) {
            sizeIdx = 0;
        }
        if (sizeIdx > 15) {
            sizeIdx = 15;
        }

        data.cloudEnd = (3000.0F + yawIdx * 181.0F + pitchIdx) * 16.0F + sizeIdx;
        McsmDiag.carrier(data.cloudEnd, yawIdx, pitchIdx);
    }
}
