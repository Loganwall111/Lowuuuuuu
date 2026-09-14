package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.client.FoglessRenderTypes;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.renderer.WitherStormHeadRenderer;
import net.dabicco.witherstormmod.entity.renderer.WitherStormRenderer;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Build #374: wire the Phase 1 command-block face into the emissive channel.
 *
 * The base renderer's face-glow pass (the turquoiseTeeth pass) only re-renders
 * the HUNCHBACK model, so the Phase 1 command-block head — the 8x8 head cube
 * with the wither face in the 64x96 WITHER_STORM layer — never got an emissive
 * pass at all: the eyes and teeth rendered flat and dark in the dark.
 *
 * At the TAIL of submitHunchback (which runs in the same pose frame as the
 * base face-glow pass and pops its own transforms before returning), re-render
 * the command-block model through the dedicated eyes RenderType with the
 * 64x96 face glow map, full-bright light and the live eye/teeth glow tint.
 *
 *  - phase < 2.0 only: that is exactly when the command-block head is visible
 *    (the base model hides the whole early body at phase >= 2);
 *    - the 64x96 maps (wither_storm_og_e.png / wither_storm_e.png) are the
 *    isolated eye+teeth silhouettes for this layer's UV space;
 *    - the same turquoiseTeeth toggle governs it, and the preview shadow pass
 *    is excluded exactly like the base's own face-glow pass.
 */
@Mixin(WitherStormRenderer.class)
public abstract class McsmFaceGlowMixin {

    /** Target-private fields the injected pass needs (merged at apply time). */
    @Shadow(remap = false) private boolean previewShadowPass;
    @Shadow(remap = false) private net.dabicco.witherstormmod.entity.model.WitherCommandBlock commandBlockModel;

    @Inject(method = "submitHunchback", at = @At("TAIL"), remap = false, require = 0)
    private void mcsm$commandBlockFaceGlow(WitherStormRenderState state, PoseStack poseStack,
                                           SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        if (state.phase >= 2.0D) {
            return;
        }
        if (previewShadowPass) {
            return;
        }
        if (!DabyWSClientConfig.turquoiseTeeth) {
            return;
        }
        try {
            // The command-block face plate (r1, texOffs 0,64) samples the
            // 160x160 body-sheet UV space — the legacy face map carries the
            // exact face pixels (UV 20,65-49,79) on a transparent field.
            Identifier glow = Identifier.fromNamespaceAndPath("dabywitherstormmod",
                    "textures/entity/wither_storm_legacy_e.png");
            submitNodeCollector.submitModel(
                    commandBlockModel,
                    state,
                    poseStack,
                    FoglessRenderTypes.eyes(glow),
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    WitherStormHeadRenderer.glowTint(),
                    null,
                    0,
                    null
            );
        } catch (Throwable ignored) {
            // Cosmetic pass only — never let the face glow take down the storm.
        }
    }
}
