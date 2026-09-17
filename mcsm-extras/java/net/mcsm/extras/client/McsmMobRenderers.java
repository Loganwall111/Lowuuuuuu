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

    /** Before anything can be baked: the bodies. */
    public static void registerLayers() {
        // BUILD #477 -- ONE AT A TIME. These shared a single try block, so the first
        // definition that threw took every layer after it with it -- silently, because
        // the catch only printed. A layer that is never registered is a mob that is a
        // missing model or, worse, a model the game cannot bake at all.
        layerMassg();
        layerCreator();
        layerVoidwalker();
        layerLurker();
        layerDrifter();
        layerKeeper();
        // BUILD #466 -- and the CAST's own body. The renderer for the Story Mode
        // characters used to bake ModelLayers.PLAYER: Minecraft's own player mesh with
        // a Story Mode skin on it. This is the layer that replaces it, and it has to be
        // registered before any renderer bakes it or the cast is a missing model rather
        // than a wrong one.
        try {
            ModelLayerRegistry.registerModelLayer(StoryCharacterRenderer.LAYER,
                    StoryCharacterRenderer::createBodyLayer);
        } catch (Throwable t) {
            System.err.println("[ds] the cast's body could not be registered: " + t);
        }
    }

    // BUILD #477 -- one registration per body, each in its own try block with the
    // EXACT call shape the whole file already used (`registerModelLayer(LAYER,
    // Model::createBodyLayer)`), so isolating them costs nothing at compile time.
    private static void layerMassg() {
        try {
            ModelLayerRegistry.registerModelLayer(MASSG_LAYER, McsmMobModels.MassgModel::createBodyLayer);
        } catch (Throwable t) {
            System.err.println("[ds] the Massg body could not be registered: " + t);
        }
    }

    private static void layerCreator() {
        try {
            ModelLayerRegistry.registerModelLayer(CREATOR_LAYER, McsmMobModels.CreatorModel::createBodyLayer);
        } catch (Throwable t) {
            System.err.println("[ds] the Creator body could not be registered: " + t);
        }
    }

    private static void layerVoidwalker() {
        try {
            ModelLayerRegistry.registerModelLayer(VOIDWALKER_LAYER, McsmMobModels.VoidwalkerModel::createBodyLayer);
        } catch (Throwable t) {
            System.err.println("[ds] the voidwalker body could not be registered: " + t);
        }
    }

    private static void layerLurker() {
        try {
            ModelLayerRegistry.registerModelLayer(VOID_LURKER_LAYER, McsmMobModels.VoidLurkerModel::createBodyLayer);
        } catch (Throwable t) {
            System.err.println("[ds] the lurker body could not be registered: " + t);
        }
    }

    private static void layerDrifter() {
        try {
            ModelLayerRegistry.registerModelLayer(DRIFTER_LAYER, McsmMobModels.DrifterModel::createBodyLayer);
        } catch (Throwable t) {
            System.err.println("[ds] the drifter body could not be registered: " + t);
        }
    }

    private static void layerKeeper() {
        try {
            ModelLayerRegistry.registerModelLayer(KEEPER_LAYER, McsmMobModels.KeeperModel::createBodyLayer);
        } catch (Throwable t) {
            System.err.println("[ds] the keeper body could not be registered: " + t);
        }
    }

    /** Then the renderers, one per body the registry actually holds. */
    public static void registerRenderers() {
        // BUILD #477 -- ONE AT A TIME, and each one caught BY NAME.
        //
        // EntityRenderers.createEntityRenderers runs inside the RESOURCE RELOAD. A
        // throw from a single mob's renderer does not cost that mob: it aborts the
        // reload for the whole game, Minecraft drops every resource pack, and the
        // client comes up with no models and a black frame. That is the player's own
        // report -- "my game just crashes or completely goes black" -- and this is
        // where it came from, so every one of them stands on its own now.
        try {
            if (McsmEntities.MAS != null) {
                EntityRendererRegistry.register(McsmEntities.MAS, MassgRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the Massg renderer could not be registered: " + t);
        }
        try {
            if (McsmEntities.CREATOR != null) {
                EntityRendererRegistry.register(McsmEntities.CREATOR, CreatorRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the Creator renderer could not be registered: " + t);
        }
        try {
            if (McsmEntities.WHALE_MONSTER != null) {
                // the whale wears the lurker's mesh: a bulbous body with a maw and
                // six tentacles is a whale monster already
                EntityRendererRegistry.register(McsmEntities.WHALE_MONSTER, WhaleRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the whale renderer could not be registered: " + t);
        }
        try {
            // BUILD #484 -- and the void's own whale: the same renderer and the same
            // body, because mcsm:void_whale IS a whale, under the id the plan gave it.
            if (McsmEntities.VOID_WHALE != null) {
                EntityRendererRegistry.register(McsmEntities.VOID_WHALE, WhaleRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the void whale renderer could not be registered: " + t);
        }
        try {
            if (McsmEntities.VOIDWALKER != null) {
                EntityRendererRegistry.register(McsmEntities.VOIDWALKER, VoidwalkerRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the voidwalker renderer could not be registered: " + t);
        }
        try {
            if (McsmEntities.VOID_LURKER != null) {
                EntityRendererRegistry.register(McsmEntities.VOID_LURKER, VoidLurkerRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the lurker renderer could not be registered: " + t);
        }
        try {
            if (McsmEntities.DRIFTER != null) {
                EntityRendererRegistry.register(McsmEntities.DRIFTER, DrifterRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the drifter renderer could not be registered: " + t);
        }
        try {
            if (McsmEntities.KEEPER != null) {
                EntityRendererRegistry.register(McsmEntities.KEEPER, KeeperRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the keeper renderer could not be registered: " + t);
        }
        try {
            // BUILD #483 -- the talker. A type with no renderer is a mob that is in
            // the world and invisible, which is the one outcome this file exists to
            // prevent.
            if (McsmEntities.VOID_DWELLER != null) {
                EntityRendererRegistry.register(McsmEntities.VOID_DWELLER, DwellerRenderer::new);
            }
        } catch (Throwable t) {
            System.err.println("[ds] the dweller renderer could not be registered: " + t);
        }
    }

    // ------------------------------------------------------------------
    // MAS -- the black warden
    // ------------------------------------------------------------------
    public static final class MassgRenderer extends HumanoidMobRenderer<McsmBeast,
            McsmMobModels.MobState, McsmMobModels.MassgModel> {

        public MassgRenderer(EntityRendererProvider.Context ctx) {
            // BUILD #477 -- through the guarded factory: a body that cannot be built
            // must never be able to abort the resource reload.
            super(ctx, McsmMobModels.massg(ctx), 2.0F);
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
            super(ctx, McsmMobModels.creator(ctx), 6.0F);
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
            super(ctx, McsmMobModels.lurker(ctx), 2.5F);
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
            super(ctx, McsmMobModels.voidwalker(ctx), 0.6F);
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
            super(ctx, McsmMobModels.lurker(ctx), 1.6F);
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
    // ------------------------------------------------------------------
    // THE VOID DWELLER (BUILD #483)
    // ------------------------------------------------------------------
    /**
     * The talker of the deep. It wears the lurker's own mesh -- a bulbous body and
     * six trailing limbs reads as something that lives in fluid -- in the void's
     * own colours, and its line is carried on the entity's synced data, so the
     * frame's typewriter needs nothing but the entity itself.
     */
    public static final class DwellerRenderer extends HumanoidMobRenderer<
            net.mcsm.extras.entity.VoidDwellerEntity,
            McsmMobModels.MobState, McsmMobModels.VoidLurkerModel> {

        public DwellerRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, McsmMobModels.lurker(ctx), 0.8F);
        }

        @Override
        public McsmMobModels.MobState createRenderState() {
            return new McsmMobModels.MobState();
        }

        @Override
        public void extractRenderState(net.mcsm.extras.entity.VoidDwellerEntity e,
                McsmMobModels.MobState s, float partialTick) {
            super.extractRenderState(e, s, partialTick);
            s.kind = "whale";
            s.age = e.tickCount + partialTick;
        }

        @Override
        public Identifier getTextureLocation(McsmMobModels.MobState s) {
            return skin("voidwalker");
        }
    }

    public static final class DrifterRenderer extends HumanoidMobRenderer<McsmDenizen.McsmDrifter,
            McsmMobModels.MobState, McsmMobModels.DrifterModel> {

        public DrifterRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, McsmMobModels.drifter(ctx), 0.5F);
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
            super(ctx, McsmMobModels.keeper(ctx), 0.7F);
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
