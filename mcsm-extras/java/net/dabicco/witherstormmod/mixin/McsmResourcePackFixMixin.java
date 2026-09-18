package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

/**
 * BUILD #486 – Fix crash:
 * java.lang.NullPointerException: Cannot invoke "net.fabricmc.fabric.impl.resource.pack.FabricPack.fabric$isHidden()" because "profile" is null
 *   at net.fabricmc.fabric.impl.resource.client.DefaultResourcePackStorage.process
 *   at net.minecraft.client.Options.handler$cah000$fabric-resource-loader-v1$onLoad
 *
 * Root cause: overrides/resourcepacks contained LFS pointer files (zip + pack.mcmeta)
 * that Fabric tried to load as resource packs, failed to parse, and returned null profile.
 * Fabric 0.161.0+26.2 then NPEs when calling fabric$isHidden() on null.
 *
 * This mixin guards the call: if instance is null or throws NPE, treat as hidden/skip.
 * Also fixes the underlying LFS pointers in this repo (pack.mcmeta now valid JSON,
 * broken zip LFS pointers removed, folder with § renamed to DynamicSurroundings_R15).
 *
 * Remap = false because target is Fabric API internal class, not vanilla.
 */
@Mixin(targets = "net.fabricmc.fabric.impl.resource.client.DefaultResourcePackStorage", remap = false)
public abstract class McsmResourcePackFixMixin {

    @WrapOperation(method = "process", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/resource/pack/FabricPack;fabric$isHidden()Z"), remap = false)
    private boolean mcsm$guardIsHidden(net.fabricmc.fabric.impl.resource.pack.FabricPack instance, Operation<Boolean> original) {
        if (instance == null) {
            // Null profile = invalid pack (LFS pointer, broken zip) – treat as hidden to skip
            System.out.println("[MCSM] ResourcePackFix: null FabricPack profile skipped (broken pack.mcmeta or LFS pointer)");
            return true;
        }
        try {
            return original.call(instance);
        } catch (NullPointerException | RuntimeException e) {
            System.out.println("[MCSM] ResourcePackFix: exception in fabric$isHidden() – treating as hidden: " + e.getMessage());
            return true;
        }
    }

    // Additional guard: if process method itself receives null ResourcePackProfile, cancel early
    // We inject at HEAD of process(ResourcePackProfile) if exists – use generic Object to match
    @WrapOperation(method = "process", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/Pack;isHidden()Z"), remap = true)
    private boolean mcsm$guardVanillaHidden(net.minecraft.server.packs.repository.Pack instance, Operation<Boolean> original) {
        if (instance == null) {
            System.out.println("[MCSM] ResourcePackFix: null vanilla Pack skipped");
            return true;
        }
        try {
            return original.call(instance);
        } catch (NullPointerException | RuntimeException e) {
            System.out.println("[MCSM] ResourcePackFix: exception in Pack.isHidden() – treating as hidden: " + e.getMessage());
            return true;
        }
    }
}
