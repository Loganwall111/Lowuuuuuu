package com.rewritten.devouringstorms.client.renderers;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.client.DsEntityRenderer;
import com.rewritten.devouringstorms.client.DsRenderState;
import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.storm.MassgPhase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * MASSG. A core of corrupted command-block matter, a ring of smaller skull-faces,
 * and tentacles that never stop reaching. Model authored in code: y+ is down (flip applied
 * by the base renderer), 16 texels per block.
 */
public class MassgRenderer extends DsEntityRenderer<MassgEntity, DsRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(DevouringStorms.id("massg"), "main");
    private static final ResourceLocation TEXTURE = DevouringStorms.id("textures/entity/massg.png");

    public MassgRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LAYER, TEXTURE, 2.0f);
    }

    @Override
    protected DsRenderState newRenderState() {
        return new DsRenderState();
    }

    // emissiveChild() takes no state — cache the current phase as each frame animates in.
    private MassgPhase renderedPhase = MassgPhase.SLEEPING;

    @Override
    protected String emissiveChild() {
        return renderedPhase.glows() ? "bowels" : null;
    }

    @Override
    protected void fillExtra(MassgEntity entity, DsRenderState state, float partialTick) {
        state.phase = entity.getPhase().id();
        state.growth = entity.getGrowth();
        state.critical = entity.isCritical();
        state.variant = entity.getVariant().ordinal();
    }

    /** The dripping, twitching silhouette: core + 3 heads + 6 tentacles. */
    public static net.minecraft.client.model.geom.builders.TexturedModelData createModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // core — 32x16x32 texels (2x1x2 blocks pre-scale)
        root.addOrReplaceChild("core",
            CubeListBuilder.create().texOffs(0, 0).addBox(-16f, 4f, -16f, 32f, 16f, 32f),
            PartPose.offset(0f, 0f, 0f));

        // maw — the devouring centre
        root.addOrReplaceChild("maw",
            CubeListBuilder.create().texOffs(0, 48).addBox(-8f, 20f, -8f, 16f, 10f, 16f),
            PartPose.offset(0f, -4f, 0f));

        // THE BOWELS — phase 5.5. Core tissue that splits out beneath the maw and burns
        // violet; hidden at birth, swelling through the Sunderer, exposed at the Bowels.
        // UV (64,80): a 14x9x14 cube spans 56x23 texels — the free quadrant of the 128 atlas.
        root.addOrReplaceChild("bowels",
            CubeListBuilder.create().texOffs(64, 80).addBox(-7f, 26f, -7f, 14f, 9f, 14f),
            PartPose.offset(0f, -4f, 0f));

        // three skull-faces: front, left, right
        root.addOrReplaceChild("head_front",
            CubeListBuilder.create().texOffs(64, 0).addBox(-5f, -5f, -5f, 10f, 10f, 10f),
            PartPose.offset(0f, -6f, -18f));
        root.addOrReplaceChild("head_left",
            CubeListBuilder.create().texOffs(64, 0).addBox(-5f, -5f, -5f, 10f, 10f, 10f),
            PartPose.offset(-16f, -6f, 4f));
        root.addOrReplaceChild("head_right",
            CubeListBuilder.create().texOffs(64, 0).addBox(-5f, -5f, -5f, 10f, 10f, 10f),
            PartPose.offset(16f, -6f, 4f));

        // THREE TRACTOR BEAMS pouring from the heads' mouths — translucent filament columns,
        // sampled from a pre-faded strip at UV (104,104) on the 128 atlas. Hidden below Devourer;
        // from the Devourer on, they comb the ground for anything warm.
        var beamCube = CubeListBuilder.create().texOffs(104, 104).addBox(-1f, 0f, -1f, 2f, 22f, 2f);
        root.addOrReplaceChild("beam_front", beamCube, PartPose.offset(0f, -1f, -18f));
        root.addOrReplaceChild("beam_left", beamCube, PartPose.offset(-16f, -1f, 4f));
        root.addOrReplaceChild("beam_right", beamCube, PartPose.offset(16f, -1f, 4f));

        // six reaching tentacles hanging below
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3.0;
            float x = (float) Math.cos(angle) * 14f;
            float z = (float) Math.sin(angle) * 14f;
            root.addOrReplaceChild("tentacle_" + i,
                CubeListBuilder.create().texOffs(0, 80).addBox(-1.5f, 0f, -1.5f, 3f, 22f, 3f),
                PartPose.offset(x, 18f, z));
        }
        return net.minecraft.client.model.geom.builders.TexturedModelData.createDefault(mesh,
            new net.minecraft.client.model.geom.builders.TextureDimensions(128, 128));
    }

    @Override
    protected void animate(DsRenderState state) {
        // The model parts are reached through the baked root in the base class; walk by name.
        float age = state.ageTicks;
        renderedPhase = MassgPhase.byId(state.phase);

        // the bowels: swelling through the Sunderer ("phase 5.5"), exposed at the Bowels
        withChild("bowels", part -> {
            float swell = switch (renderedPhase) {
                case SLEEPING, SIGNAL, HUNGER, DEVOURER -> 0.02f;   // sealed inside
                case SUNDERER -> 0.55f + 0.10f * (float) Math.sin(age * 0.10f);  // it strains
                case BOWELS, GENESIS -> 1.05f + 0.18f * (float) Math.sin(age * 0.25f); // exposed, vivid
                case HUSK -> 0.5f;   // slack, half-collapsed — the hole still opens
                default -> 0.02f;
            };
            part.xScale = swell;
            part.zScale = swell;
            part.yScale = Math.max(0.02f, swell * 0.9f);
        });

        withChild("core", part -> part.yRot = age * 0.02f);
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            withChild("tentacle_" + i, part -> {
                part.xRot = (float) Math.sin(age * 0.12f + idx * 1.3f) * 0.45f + 0.15f;
                part.zRot = (float) Math.cos(age * 0.09f + idx * 0.7f) * 0.4f;
            });
        }
        withChild("maw", part -> {
            float bite = (float) (Math.abs(Math.sin(age * 0.08f)) * 3.0);
            part.y = -4f + bite;
        });
        withChild("head_front", part -> part.yRot = (float) Math.sin(age * 0.05f) * 0.4f);
        withChild("head_left", part -> part.yRot = 0.6f + (float) Math.sin(age * 0.043f + 2.0f) * 0.3f);
        withChild("head_right", part -> part.yRot = -0.6f + (float) Math.cos(age * 0.037f) * 0.3f);

        // TRACTOR: from the Devourer on, the three beams comb the dark for prey.
        // They idle dormant (scale ~0) while the storm is small, bloom full-length when it
        // is hungry, and sweep slow cones the heads never stop tracing.
        boolean beamsOn = switch (renderedPhase) {
            case DEVOURER, SUNDERER, BOWELS, GENESIS -> true;
            default -> false;
        };
        float beamLen = beamsOn ? (0.45f + state.growth * 0.55f) : 0.001f;
        float sweepF = (float) Math.sin(age * 0.06f) * 0.55f;
        float sweepS = (float) Math.cos(age * 0.045f) * 0.45f;
        withChild("beam_front", part -> {
            part.yScale = beamLen;
            part.xRot = 0.35f + sweepF * 0.5f;
        });
        withChild("beam_left", part -> {
            part.yScale = beamLen * (0.85f + 0.15f * (float) Math.sin(age * 0.11f));
            part.xRot = 0.35f + sweepS * 0.5f;
            part.zRot = -0.25f;
        });
        withChild("beam_right", part -> {
            part.yScale = beamLen * (0.85f + 0.15f * (float) Math.cos(age * 0.09f));
            part.xRot = 0.35f - sweepF * 0.5f;
            part.zRot = 0.25f;
        });
    }

    @Override
    protected int tint(DsRenderState state) {
        int raw = tintBody(state);
        var v = com.rewritten.devouringstorms.entity.MassgVariant.byIdTurn(state.variant);
        if (v == com.rewritten.devouringstorms.entity.MassgVariant.CLASSIC) return raw;
        int a = (raw >>> 24) & 0xFF;
        int rr = (int) (((raw >> 16) & 0xFF) * v.r) & 0xFF;
        int gg = (int) (((raw >> 8) & 0xFF) * v.g) & 0xFF;
        int bb = (int) ((raw & 0xFF) * v.b) & 0xFF;
        return (a << 24) | (rr << 16) | (gg << 8) | bb;
    }

    protected int tintBody(DsRenderState state) {
        int alpha = 255;
        MassgPhase phase = MassgPhase.byId(state.phase);
        if (phase == MassgPhase.HUSK) {
            // the zombie-form: ashen, half-dead lavender — the glow left with the sky
            int g = (int) (140.0 + 30.0 * Math.sin(state.ageTicks * 0.05f));
            return (alpha << 24) | (190 & 0xFF) << 16 | (g & 0xFF) << 8 | 205;
        }
        if (phase == MassgPhase.BOWELS) {
            // the split-open storm: hot magenta flush over everything, breathing hard
            int pulse = (int) (150.0 + 105.0 * Math.sin(state.ageTicks * 0.35f));
            return (alpha << 24) | 0xFF << 16 | (pulse & 0xFF) << 8 | 0xFF;
        }
        if (state.critical) {
            // angry strobing pulse while devolving
            int pulse = (int) (200.0 + 55.0 * Math.sin(state.ageTicks * 0.6f));
            return (alpha << 24) | (pulse & 0xFF) << 16 | 0x80 << 8 | 0xFF;
        }
        if (phase.glows()) {
            // post-bowels bodies never quite stop smouldering
            int g = 170 + (int) (60.0 * Math.sin(state.ageTicks * 0.12f));
            return (alpha << 24) | 0xFF << 16 | (g & 0xFF) << 8 | 0xFF;
        }
        // phase-deep purple grade
        int g = 255 - phase.ordinal() * 24;
        int b = 255;
        return (alpha << 24) | 0xFF << 16 | (g & 0xFF) << 8 | b;
    }
}
