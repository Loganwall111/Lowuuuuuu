package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;

import java.util.HashMap;
import java.util.Map;

import net.dabicco.witherstormmod.mixin.RenderPipelinesAccessor;
import net.dabicco.witherstormmod.mixin.RenderTypeInvoker;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * BUILD #416 -- the WHITE conic-ambient-light-column material.
 *
 * WHY THIS LIVES IN mcsm-extras AND NOT NEXT TO GlowRenderTypes.
 *
 * {@code net.dabicco.witherstormmod.client.GlowRenderTypes} is not compiled from
 * this repository: CI compiles ONLY {@code mcsm-extras/java} and assembles the rest
 * of the jar from the FROZEN base release asset
 * ({@code dabywitherstormmod-1.9.100-26.2-beta-mcsm.jar}, sha256 6adcf07e…), which
 * is pinned by hash in ci/build.sh. The reference copies of that class under
 * {@code net/} and {@code src-recon/} are documentation -- they are on no compile
 * classpath at all. Adding a method to one of them therefore does NOT add it to the
 * build, which is exactly how the first attempt at this halo failed to compile:
 * javac resolved GlowRenderTypes from the frozen jar and there was no glowWhite.
 *
 * So the white pipeline is built HERE, in a class that really is compiled and really
 * does overwrite/add into {@code net.dabicco.witherstormmod.client} at assembly time.
 *
 * WHAT IT IS. Same fragment shader as the ordinary glow pool ({@code core/storm_glow}),
 * same per-pixel gaussian falloff, same 4x emissive gain and the same ADDITIVE (ONE,
 * ONE) blend as {@link GlowRenderTypes#glow(Identifier)} -- but built with the
 * MCSM_GLOW_WHITE define, which pins the pool's hue to white. That is the whole point
 * of the separation the mandate asks for: the atmosphere the storm throws is WHITE at
 * every phase, while the aura around the teeth and eyes keeps carrying the phase colour
 * (bluish / white / bluish / pure blue / toxic green / blue) and is untouched by this.
 *
 * The pipeline is cached in a static exactly like the base's own pipelines, and the
 * per-texture RenderTypes are cached in a map, so this costs nothing per frame.
 */
public final class McsmWhiteGlow {

    private static final String NAMESPACE = "dabywitherstormmod";

    private static final Map<Identifier, RenderType> TYPES = new HashMap<>();
    private static RenderPipeline whitePipeline;

    private McsmWhiteGlow() {
    }

    /**
     * The white pool pipeline: an exact copy of the base glow pipeline's state with
     * MCSM_GLOW_WHITE added. Same entity-emissive snippet, so it binds the same Sampler0
     * + lightmap block the shader already expects -- no new uniform, no Vulkan
     * bind-group mismatch (the death-stack work and the fogless shader both depend on
     * that same rule).
     */
    private static RenderPipeline whitePipeline() {
        if (whitePipeline == null) {
            whitePipeline = RenderPipeline.builder(new RenderPipeline.Snippet[]{
                            RenderPipelinesAccessor.dabyws$entityEmissiveSnippet()})
                    .withLocation(id("pipeline/storm_glow_white"))
                    .withVertexShader(id("core/fogless_entity"))
                    .withFragmentShader(id("core/storm_glow"))
                    .withShaderDefine("MCSM_GLOW_WHITE")
                    .withShaderDefine("NO_OVERLAY")
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withColorTargetState(new ColorTargetState(new BlendFunction(
                            BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ZERO, BlendFactor.ONE)))
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .withCull(false)
                    .build();
        }
        return whitePipeline;
    }

    /** The white column's material: additive, per-pixel falloff, white-locked. */
    public static RenderType glowWhite(Identifier texture) {
        return TYPES.computeIfAbsent(texture, tex -> RenderTypeInvoker.dabyws$create(
                NAMESPACE + ":storm_glow_white:" + tex,
                RenderSetup.builder(whitePipeline()).withTexture("Sampler0", tex).createRenderSetup()));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }
}
