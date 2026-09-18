package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BUILD #486 – Fix crash:
 * java.lang.NullPointerException: Cannot invoke "net.fabricmc.fabric.impl.resource.pack.FabricPack.fabric$isHidden()" because "profile" is null
 *   at net.fabricmc.fabric.impl.resource.client.DefaultResourcePackStorage.process
 *
 * Root cause: overrides/resourcepacks contained LFS pointer files (zip + pack.mcmeta)
 * that Fabric tried to load as resource packs, failed to parse, and returned null profile.
 * Fabric 0.161.0+26.2 then NPEs when calling fabric$isHidden() on null.
 *
 * This mixin guards the process method: if profile is null, cancel to skip broken pack.
 * Also fixes underlying LFS pointers (pack.mcmeta now valid JSON, broken zip LFS pointers
 * removed, folder with § renamed to DynamicSurroundings_R15).
 *
 * Remap = false because target is Fabric API internal class, not vanilla.
 * No MixinExtras dependency – uses only standard Mixin to ensure CI compile works.
 */
@Mixin(targets = "net.fabricmc.fabric.impl.resource.client.DefaultResourcePackStorage", remap = false)
public abstract class McsmResourcePackFixMixin {

    @Inject(method = "process", at = @At("HEAD"), cancellable = true, remap = false)
    private void mcsm$fixNullProfile(Object profile, CallbackInfo ci) {
        if (profile == null) {
            System.out.println("[MCSM] ResourcePackFix: null profile skipped (broken pack.mcmeta or LFS pointer)");
            ci.cancel();
        }
    }
}
