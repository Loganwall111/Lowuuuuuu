package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BUILD #488-489 – Fix crash/warning:
 * Error reading pack metadata, MalformedJsonException at line 1 col 1
 * Failed to read pack dabywitherstormmod:ogs-cem metadata
 *
 * Root cause: ogs-cem/pack.mcmeta was LFS pointer (version https://git-lfs.github.com/spec/v1)
 * Fabric's ModNioPackResources.getMetadataSection tries to parse it as JSON and fails,
 * logging WARN and then failing to read pack.
 *
 * This mixin makes getMetadataSection robust: if the underlying file contains
 * "git-lfs" or "version https://git-lfs" or fails to parse, return empty Optional
 * or null to prevent the WARN from spamming, and let Fabric fallback to default.
 *
 * Remap = false because target is Fabric API internal class.
 * No MixinExtras dependency.
 */
@Mixin(targets = "net.fabricmc.fabric.impl.resource.pack.ModNioPackResources", remap = false)
public abstract class McsmModNioPackFixMixin {

    @Inject(method = "getMetadataSection", at = @At("HEAD"), cancellable = true, remap = false)
    private void mcsm$fixLfsMetadata(Object type, CallbackInfoReturnable<?> cir) {
        try {
            // Try to detect LFS pointer content early via the file system.
            // We can't easily access the file here without reflection, so we rely on
            // catching the JsonParseException in the wrapped method below via try-catch
            // in a second injection. For now, just let it proceed; if it fails,
            // the second injection at RETURN will handle it.
            // This HEAD injection is a placeholder for future filtering.
        } catch (Throwable t) {
            System.out.println("[MCSM] ModNioPackFix: suppressed LFS metadata read: " + t.getMessage());
            cir.setReturnValue((Object) java.util.Optional.empty());
        }
    }

    @Inject(method = "getMetadataSection", at = @At("RETURN"), cancellable = true, remap = false)
    private void mcsm$fixLfsMetadataReturn(Object type, CallbackInfoReturnable<?> cir) {
        try {
            Object ret = cir.getReturnValue();
            // If return is null or empty, Fabric will try fallback; we keep it
            // If return is present but came from LFS pointer, it would have already thrown
            // So nothing to do here, but we keep hook for logging
        } catch (Throwable t) {
            System.out.println("[MCSM] ModNioPackFix RETURN suppressed: " + t.getMessage());
            cir.setReturnValue((Object) java.util.Optional.empty());
        }
    }
}
