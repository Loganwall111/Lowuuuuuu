package net.dabicco.witherstormmod.mixin;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps native head teeth and eye geometry on the direct eyes material. */
@Mixin(net.dabicco.witherstormmod.entity.renderer.WitherStormHeadRenderer.class)
public abstract class McsmHeadEyesRenderTypeMixin {
    @Redirect(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/dabicco/witherstormmod/client/GlowRenderTypes;emitterMark(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"),
            remap = false,
            require = 0)
    private static RenderType mcsm$headEmitterEyes(Identifier texture) {
        return mcsm$emissive(texture);
    }

    @Redirect(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/dabicco/witherstormmod/client/GlowRenderTypes;bloomSource(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"),
            remap = false,
            require = 0)
    private static RenderType mcsm$headBloomEyes(Identifier texture) {
        return mcsm$emissive(texture);
    }

    /**
     * BUILD #442 -- THE DEDICATED EMISSIVE ATLAS, ON THE PASSES THAT EXIST.
     *
     * This third redirect used to target `RenderTypes.eyes(Identifier)` inside
     * `submit` -- and run 521's constant-pool probe proves the head renderer
     * never makes that call: its render-type references are GlowRenderTypes'
     * emitterMark / bloomSource / bloomOccluder / bloomEraseOccluded,
     * FoglessRenderTypes.bodyCutout and StormSkins.phase4, and no
     * RenderTypes.eyes at all. So that redirect could never fire, and the
     * intent behind it -- "keep the uploaded universal body texture out of
     * eye/teeth glow" -- never took effect on this build of the base.
     *
     * The two passes that DO exist are the two above, and they now feed the
     * dedicated 512x512 transparent emissive atlas instead of passing the
     * renderer's own texture through. That atlas is the one the white-mask gate
     * (ci/make_emissive_whites.py) holds to pure white from phase 4 up, and it
     * is the same atlas the mini-head glow already wears, so the head's teeth
     * and eyes come out full-bright white with no shader pack installed.
     */
    private static RenderType mcsm$emissive(Identifier incoming) {
        return RenderTypes.eyes(net.dabicco.witherstormmod.client.StormSkins.phase6Emissive());
    }
}
