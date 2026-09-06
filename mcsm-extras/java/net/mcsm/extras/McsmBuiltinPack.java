package net.mcsm.extras;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationPredicate;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

/**
 * Devouring Storms: the Story Look pack ships INSIDE the mod jar and turns
 * itself on. Fabric's resource-loader exposes built-in packs living at
 * resourcepacks/&lt;name&gt;/ in a mod jar; DEFAULT_ENABLED means the pack is
 * active the first time the game loads it, with no separate download and no
 * pack-screen step - and the user can still switch it off like any pack.
 */
public final class McsmBuiltinPack {

    private McsmBuiltinPack() {
    }

    public static void register() {
        FabricLoader.getInstance().getModContainer("dabywitherstormmod").ifPresent(mod ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                        ResourceLocation.fromNamespaceAndPath("dabywitherstormmod", "storylook"),
                        mod,
                        ResourcePackActivationPredicate.DEFAULT_ENABLED));
    }
}
