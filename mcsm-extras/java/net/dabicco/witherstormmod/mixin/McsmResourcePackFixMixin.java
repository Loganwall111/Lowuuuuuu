package net.dabicco.witherstormmod.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BUILD #486-487 – Fix crash:
 * java.lang.NullPointerException: Cannot invoke "net.fabricmc.fabric.impl.resource.pack.FabricPack.fabric$isHidden()" because "profile" is null
 *   at net.fabricmc.fabric.impl.resource.client.DefaultResourcePackStorage.process
 *   -> process(Ljava/util/Collection;)Ljava/util/List; is STATIC
 *
 * Root cause: overrides/resourcepacks contained LFS pointer files (zip + pack.mcmeta)
 * that Fabric tried to load as resource packs, failed to parse, and returned null profile.
 * Fabric 0.161.0+26.2 then NPEs when calling fabric$isHidden() on null inside process().
 *
 * This mixin guards the STATIC process method: if collection is null or contains null,
 * remove nulls and return filtered list, preventing NPE.
 * Also fixes underlying LFS pointers (pack.mcmeta now valid JSON, broken zip LFS pointers
 * removed, folder with § renamed to DynamicSurroundings_R15).
 *
 * Remap = false because target is Fabric API internal class, not vanilla.
 * No MixinExtras dependency – uses only standard Mixin to ensure CI compile works.
 */
@Mixin(targets = "net.fabricmc.fabric.impl.resource.client.DefaultResourcePackStorage", remap = false)
public abstract class McsmResourcePackFixMixin {

    @Inject(method = "process", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mcsm$fixNullProfile(Collection<?> packs, CallbackInfoReturnable<List<?>> cir) {
        if (packs == null) {
            System.out.println("[MCSM] ResourcePackFix: null collection – returning empty");
            cir.setReturnValue(List.of());
            return;
        }
        // If collection contains null (broken pack.mcmeta or LFS pointer), filter it out
        if (packs.contains(null)) {
            System.out.println("[MCSM] ResourcePackFix: null profile in collection – filtering");
            packs.removeIf(Objects::isNull);
        }
        // Also filter any profile where fabric$isHidden would NPE – we can't call it here safely,
        // but removing nulls already prevents the NPE at line 87
    }
}
