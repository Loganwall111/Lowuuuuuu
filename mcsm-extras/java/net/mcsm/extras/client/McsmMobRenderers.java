package net.mcsm.extras.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.mcsm.extras.entity.McsmBeast;
import net.mcsm.extras.entity.McsmEntities;
import net.mcsm.extras.entity.McsmVoidLurker;
import net.mcsm.extras.entity.McsmDenizen;
import net.mcsm.extras.entity.McsmVoidwalker;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

/**
 * BUILD #456 -- the five bodies, and the four skins they wear.
 *
 * <p>The beasts existed as entities from BUILD #428 and had <b>no renderer at
 * all</b>: an entity type with nothing registered for it is not drawn, which is
 * how a mob can be in the world, walking at you, and invisible. This registers a
 * model layer and a renderer for every body the mod owns, and every one of them
 * names a texture that {@code ci/make_mcsm_textures.py} paints into the pack:
 *
 * <pre>
 *   mcsm:mas             the black warden, red lenses        textures/entity/massg.png
 *   mcsm:creator         the colossal, crowned               textures/entity/creator.png
 *   mcsm:whale_monster   the whale, all maw and tentacle     textures/entity/whale_monster.png
 *   mcsm:voidwalker      the thin walker, violet slits       textures/entity/voidwalker.png
 *   mcsm:void_lurker     the mini-boss, six lit tentacles    textures/entity/void_lurker.png
 * </pre>
 *
 * <p>The registration calls are the two this codebase already compiles elsewhere:
 * {@code EntityRendererRegistry.register} (as McsmStoryRendererMixin uses for the
 * Story Mode cast) and {@code ModelLayerRegistry.registerModelLayer} (the API the
 * base mod's own ModEntityModelLayers bakes its fifteen layers through).
 */
public final class McsmMobRenderers {

    private McsmMobRenderers() {
    }

    public static final ModelLayerLocation MASSG_LAYER =
            layer("massg");
    public static final ModelLayerLocation CREATOR_LAYER =
            layer("creator");
    public static final ModelLayerLocation VOIDWALKER_LAYER =
            layer("voidwalker");
    public static final ModelLayerLocation VOID_LURKER_LAYER =
            layer("void_lurker");
    public static final ModelLayerLocation DRIFTER_LAYER =
            layer("drifter");
    public static final ModelLayerLocation KEEPER_LAYER =
            layer("keeper");

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath("mcsm", name), "main");
    }

    private static Identifier skin(String name) {
        return Identifier.fromNamespaceAndPath("mcsm", "textures/entity/" + name + ".png");
    }

    /** Before anything can be baked: the four meshes. */
    public static void registerLayers() {
        try {
            ModelLayerRegistry.registerModelLayer(MASSG_LAYER, McsmMobModels.MassgModel::createBodyLayer);
            ModelLayerRegistry.registerModelLayer(CREATOR_LAYER, McsmMobModels.CreatorModel::createBodyLayer);
            ModelLayerRegistry.registerModelLayer(VOIDWALKER_LAYER, McsmMobModels.VoidwalkerModel::createBodyLayer);
            ModelLayerRegistry.registerModelLayer(VOID_LURKER_LAYER, McsmMobModels.VoidLurkerModel::createBodyLayer);
            ModelLayerRegistry.registerModelLayer(DRIFTER_LAYER, McsmMobModels.DrifterModel::createBodyLayer);
            ModelLayerRegistry.registerModelLayer(KEEPER_LAYER, McsmMobModels.KeeperModel::createBodyLayer);
        } catch (Throwable t) {
            System.err.println("[ds] the mob model layers could not be registered: " + t);
        }
    }

    /** Then the renderers, one per body the registry actually holds. */
    public static void registerRenderers() {
        try {
            if (McsmEntities.MAS != null) {
                EntityRendererRegistry.register(McsmEntities.MAS, MassgRenderer::new);
            }
            if (McsmEntities.CREATOR != null) {
                EntityRendererRegistry.register(McsmEntities.CREATOR, CreatorRenderer::new);
            }
            if (McsmEntities.WHALE_MONSTER != null) {
                // the whale wears the lurker's mesh: a bulbous body with a maw and
                // six tentacles is a whale monster already
                EntityRendererRegistry.register(McsmEntities.WHALE_MONSTER, WhaleRenderer::new);
            }
            if (McsmEntities.VOIDWALKER != null) {
                EntityRendererRegistry.register(McsmEntities.VOIDWALKER, VoidwalkerRenderer::new);
            }
            if (McsmEntities.VOID_LURKER != null) {
                EntityRendererRegistry.register(McsmEntities.VOID_LURKER, VoidLurkerRenderer::new);
            }
            if (McsmEntities.DRIFTER != null) {
                EntityRendererRegistry.register(McsmEntities.DRIFTER, DrifterRenderer::new);
            }
            if (McsmEntities.KEEPER != null) {
                EntityRendererRegistry.register(McsmEntities.KEEPER, KeeperRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the mob renderers could not be registered: " + t);
        }
    }

    // ------------------------------------------------------------------
    // MAS -- the black warden
    // ------------------------------------------------------------------
    public static final class MassgRenderer extends HumanoidMobRenderer<McsmBeast,
            McsmMobModels.MobState, McsmMobModels.MassgModel> {

        public MassgRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new McsmMobModels.MassgModel(ctx.bakeLayer(MASSG_LAYER)), 2.0F);
        }

        @Override
        public McsmMobModels.MobState createRenderState() {
            return new McsmMobModels.MobState();
        }

        @Override
        public void extractRenderState(McsmBeast e, McsmMobModels.MobState s, float partialTick) {
            super.extractRenderState(e, s, partialTick);
            s.kind = e.kind();
            s.age = e.tickCount + partialTick;
        }

        @Override
        public Identifier getTextureLocation(McsmMobModels.MobState s) {
            return skin("massg");
        }
    }

    // ------------------------------------------------------------------
    // THE CREATOR -- the colossal
    // ------------------------------------------------------------------
    public static final class CreatorRenderer extends HumanoidMobRenderer<McsmBeast,
            McsmMobModels.MobState, McsmMobModels.CreatorModel> {

        public CreatorRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new McsmMobModels.CreatorModel(ctx.bakeLayer(CREATOR_LAYER)), 6.0F);
        }

        @Override
        public McsmMobModels.MobState createRenderState() {
            return new McsmMobModels.MobState();
        }

        @Override
        public void extractRenderState(McsmBeast e, McsmMobModels.MobState s, float partialTick) {
            super.extractRenderState(e, s, partialTick);
            s.kind = e.kind();
            s.age = e.tickCount + partialTick;
        }

        @Override
        public Identifier getTextureLocation(McsmMobModels.MobState s) {
            return skin("creator");
        }
    }

    // ------------------------------------------------------------------
    // THE WHALE MONSTER
    // ------------------------------------------------------------------
    public static final class WhaleRenderer extends HumanoidMobRenderer<McsmBeast,
            McsmMobModels.MobState, McsmMobModels.VoidLurkerModel> {

        public WhaleRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new McsmMobModels.VoidLurkerModel(ctx.bakeLayer(VOID_LURKER_LAYER)), 2.5F);
        }

        @Override
        public McsmMobModels.MobState createRenderState() {
            return new McsmMobModels.MobState();
        }

        @Override
        public void extractRenderState(McsmBeast e, McsmMobModels.MobState s, float partialTick) {
            super.extractRenderState(e, s, partialTick);
            s.kind = e.kind();
            s.age = e.tickCount + partialTick;
        }

        @Override
        public Identifier getTextureLocation(McsmMobModels.MobState s) {
            return skin("whale_monster");
        }
    }

    // ------------------------------------------------------------------
    // THE VOIDWALKER
    // ------------------------------------------------------------------
    public static final class VoidwalkerRenderer extends HumanoidMobRenderer<McsmVoidwalker,
            McsmMobModels.MobState, McsmMobModels.VoidwalkerModel> {

        public VoidwalkerRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new McsmMobModels.VoidwalkerModel(ctx.bakeLayer(VOIDWALKER_LAYER)), 0.6F);
        }

        @Override
        public McsmMobModels.MobState createRenderState() {
            return new McsmMobModels.MobState();
        }

        @Override
        public void extractRenderState(McsmVoidwalker e, McsmMobModels.MobState s, float partialTick) {
            super.extractRenderState(e, s, partialTick);
            s.kind = "voidwalker";
            s.age = e.tickCount + partialTick;
        }

        @Override
        public Identifier getTextureLocation(McsmMobModels.MobState s) {
            return skin("voidwalker");
        }
    }

    // ------------------------------------------------------------------
    // THE VOID LURKER -- the mini-boss
    // ------------------------------------------------------------------
    public static final class VoidLurkerRenderer extends HumanoidMobRenderer<McsmVoidLurker,
            McsmMobModels.MobState, McsmMobModels.VoidLurkerModel> {

        public VoidLurkerRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new McsmMobModels.VoidLurkerModel(ctx.bakeLayer(VOID_LURKER_LAYER)), 1.6F);
        }

        @Override
        public McsmMobModels.MobState createRenderState() {
            return new McsmMobModels.MobState();
        }

        @Override
        public void extractRenderState(McsmVoidLurker e, McsmMobModels.MobState s, float partialTick) {
            super.extractRenderState(e, s, partialTick);
            s.kind = "void_lurker";
            s.age = e.tickCount + partialTick;
        }

        @Override
        public Identifier getTextureLocation(McsmMobModels.MobState s) {
            return skin("void_lurker");
        }
    }

    // ------------------------------------------------------------------
    // BUILD #460 -- THE DRIFTER AND THE KEEPER
    // ------------------------------------------------------------------
    public static final class DrifterRenderer extends HumanoidMobRenderer<McsmDenizen.McsmDrifter,
            McsmMobModels.MobState, McsmMobModels.DrifterModel> {

        public DrifterRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new McsmMobModels.DrifterModel(ctx.bakeLayer(DRIFTER_LAYER)), 0.5F);
        }

        @Override
        public McsmMobModels.MobState createRenderState() {
            return new McsmMobModels.MobState();
        }

        @Override
        public void extractRenderState(McsmDenizen.McsmDrifter e, McsmMobModels.MobState s, float partialTick) {
            super.extractRenderState(e, s, partialTick);
            s.kind = "drifter";
            s.age = e.tickCount + partialTick;
        }

        @Override
        public Identifier getTextureLocation(McsmMobModels.MobState s) {
            return skin("drifter");
        }
    }

    public static final class KeeperRenderer extends HumanoidMobRenderer<McsmDenizen.McsmKeeper,
            McsmMobModels.MobState, McsmMobModels.KeeperModel> {

        public KeeperRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new McsmMobModels.KeeperModel(ctx.bakeLayer(KEEPER_LAYER)), 0.7F);
        }

        @Override
        public McsmMobModels.MobState createRenderState() {
            return new McsmMobModels.MobState();
        }

        @Override
        public void extractRenderState(McsmDenizen.McsmKeeper e, McsmMobModels.MobState s, float partialTick) {
            super.extractRenderState(e, s, partialTick);
            s.kind = "keeper";
            s.age = e.tickCount + partialTick;
        }

        @Override
        public Identifier getTextureLocation(McsmMobModels.MobState s) {
            return skin("keeper");
        }
    }
}
