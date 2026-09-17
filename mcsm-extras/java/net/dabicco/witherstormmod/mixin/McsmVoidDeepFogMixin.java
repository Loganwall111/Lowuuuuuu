package net.dabicco.witherstormmod.mixin;

import net.mcsm.extras.client.McsmVoidDeep;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BUILD #479 -- the bottom of the void reads as fog.
 *
 * <p>The player's own words: "the bedrock layer is visible as the fog". The fog the
 * game already computes is the vehicle for that: a depth-graded wash whose colour is
 * the gel's, from the pink at the surface to the bedrock's violet at the floor, mixed
 * in on top of whatever the dimension's own identity already asked for.
 *
 * <p>Same proven injection as {@code McsmNativeFogMixin} (the same method, the same
 * TAIL, the same blend shape), in its own class so the two never fight over one
 * callback: this one is the deep's, that one is the native sky's.
 */
@Mixin(FogRenderer.class)
public abstract class McsmVoidDeepFogMixin {

    @Inject(method = "computeFogColor", at = @At("TAIL"), require = 0)
    private void mcsm$deepFog(Camera camera, float partialTick, ClientLevel level,
            int renderDistance, float darkenAmount, Vector4f color, CallbackInfo ci) {
        try {
            if (color == null) {
                return;
            }
            float[] rgb = new float[3];
            float opacity = McsmVoidDeep.fogBlend(level, rgb);
            if (opacity <= 0.0F) {
                return;
            }
            color.x = color.x * (1.0F - opacity) + rgb[0] * opacity;
            color.y = color.y * (1.0F - opacity) + rgb[1] * opacity;
            color.z = color.z * (1.0F - opacity) + rgb[2] * opacity;
        } catch (Throwable ignored) {
            // the fog is never worth a frame
        }
    }
}
