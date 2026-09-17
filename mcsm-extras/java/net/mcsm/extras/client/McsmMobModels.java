package net.mcsm.extras.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

/**
 * BUILD #456 -- OWN BODIES. The mod's monsters stop wearing borrowed skin.
 *
 * <p>THE REPORT, over and over: "the Massg the black figure warden with red eyes",
 * "voidwalkers (zombie-like)", "the void mini-boss (avoid) whose tentacles reach,
 * pull, swallow", "the gigantic the creator". Every one of those mobs existed as
 * an ENTITY and rendered as a re-kitted vanilla body -- a zombie with a black
 * skin -- which is the one thing a custom mob must never be.
 *
 * <p>This is the geometry for four of them, four meshes, four skeletons, each
 * animated in its own right:
 *
 * <pre>
 *   MassgModel       the black warden: hunched, two long horns swept back, a
 *                    heavy brow, and red eye lenses on their OWN part so the eyes
 *                    stay bright while the body stays black
 *   VoidwalkerModel  the thin walker: a hollow torso, a jaw that hangs, one arm
 *                    held up, two violet slits
 *   VoidLurkerModel  the one to avoid: a bulbous body, a maw, and SIX tentacles,
 *                    each with a lit tip that whips a beat behind it
 *   CreatorModel     the colossal: a crowned head, three spinning halos, and two
 *                    thin arms 100 blocks long coming down out of the sky
 * </pre>
 *
 * <p>HOW THE SIZE WORKS. A living model is positioned by the vanilla renderer at
 * 24 units (1.5 blocks) below the entity's own position, so a model's feet have to
 * land on y=24 or the mob sinks. Every skeleton here is written in player units
 * and then scaled by its own S -- and the root parts are shifted by exactly
 * {@code 24 - (bodyH + legH) * S}, so a 6x warden and an 8x Creator both stand on
 * the ground with their feet on it while their heads are 20 and 32 blocks up.
 *
 * <p>Every call is one the base mod's own HugeAssBackModel already compiles --
 * MeshDefinition, addOrReplaceChild, CubeListBuilder.texOffs().addBox(),
 * LayerDefinition.create() -- and every animation writes only xRot / yRot / zRot,
 * the three fields StoryCharacterRenderer already turns. Nothing is an API guess.
 */
public final class McsmMobModels {

    private McsmMobModels() {
    }

    /** The render state every one of them shares. */
    public static final class MobState extends HumanoidRenderState {
        public String kind = "mas";
        public float age;
    }

    /** The vanilla offset that puts a living model's feet on the ground, in units. */
    private static final float GROUND = 24.0F;

    // ------------------------------------------------------------------
    // Geometry helpers
    // ------------------------------------------------------------------

    /** One part holding one box, at one UV, scaled, pivoted relative to its parent. */
    static PartDefinition box(PartDefinition parent, String name, int u, int v, float s,
            float px, float py, float pz, float x, float y, float z, float w, float h, float d) {
        return parent.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(u, v)
                        .addBox(x * s, y * s, z * s, w * s, h * s, d * s, new CubeDeformation(0.0F)),
                PartPose.offset(px * s, py * s, pz * s));
    }

    /** One part holding several boxes: the eyes, the halos, anything drawn twice. */
    static PartDefinition many(PartDefinition parent, String name, int u, int v, float s,
            float px, float py, float pz, float[]... boxes) {
        CubeListBuilder builder = CubeListBuilder.create();
        for (float[] b : boxes) {
            builder.texOffs(u, v).addBox(b[0] * s, b[1] * s, b[2] * s, b[3] * s, b[4] * s, b[5] * s,
                    new CubeDeformation(0.0F));
        }
        return parent.addOrReplaceChild(name, builder, PartPose.offset(px * s, py * s, pz * s));
    }

    /** An empty part, for the slots the humanoid skeleton expects to find. */
    static PartDefinition empty(PartDefinition parent, String name, float s,
            float px, float py, float pz) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create(),
                PartPose.offset(px * s, py * s, pz * s));
    }

    /**
     * BUILD #477 -- THE "hat" PART, WHICH IS THE ONE PART THAT MAY NOT VANISH.
     *
     * <p>{@code HumanoidModel}'s constructor reads {@code root.getChild("hat")} before a
     * single vertex is drawn, and {@code ModelPart.getChild} THROWS when it is missing
     * (26.2: {@code NoSuchElementException: Can't find part hat}). It used to be created
     * through {@link #empty} -- a part with an empty cube list -- and a part with no
     * cubes is exactly the kind of thing a baker is free to drop.
     *
     * <p>What that cost, from a player's own log:
     *
     * <pre>
     *   java.lang.IllegalArgumentException: Failed to create model for mcsm:whale_monster
     *   Caused by: java.util.NoSuchElementException: Can't find part hat
     *     at net.minecraft.client.model.HumanoidModel.&lt;init&gt;
     *     at net.mcsm.extras.client.McsmMobModels$VoidLurkerModel.&lt;init&gt;
     *     at ... EntityRenderers.createEntityRenderers
     *   [Render thread/INFO]: Caught error loading resourcepacks, removing all selected resourcepacks
     * </pre>
     *
     * <p>That is not one mob failing to draw. {@code createEntityRenderers} runs inside
     * the resource reload, so a throw there aborts the whole reload -- the game drops
     * every pack and comes up with no models, no fonts and a black frame. A missing hat
     * became "my game just crashes or completely goes black".
     *
     * <p>So the part carries a real (0.01-unit, sub-pixel, never visible) cube instead.
     * A part with geometry cannot be dropped by anything.
     */
    static PartDefinition hatPart(PartDefinition root, float s, float py) {
        return root.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0.0F, 0.0F, 0.0F, 0.01F, 0.01F, 0.01F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, py * s, 0.0F));
    }

    /**
     * A child part, or the parent when it is not there.
     *
     * <p>Every part this mod adds is read this way now. {@code getChild} throws on a
     * missing name, and a model constructor that throws takes the resource reload -- and
     * therefore the frame -- down with it (see {@link #hatPart}). A part that is not
     * there animates as its parent, which looks wrong for a frame and can never blacken
     * a screen.
     */
    static ModelPart part(ModelPart parent, String name) {
        if (parent == null) {
            return null;
        }
        try {
            return parent.getChild(name);
        } catch (Throwable ignored) {
            return parent;
        }
    }

    /** Is this root the seven parts {@code HumanoidModel} reads? */
    static boolean has(ModelPart root, String name) {
        if (root == null) {
            return false;
        }
        try {
            root.getChild(name);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    static boolean complete(ModelPart root) {
        for (String name : new String[] {"head", "hat", "body", "left_arm", "right_arm",
                "left_leg", "right_leg"}) {
            if (!has(root, name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * BUILD #477 -- THE ROOT IS NOT ALLOWED TO BE THE REASON A RELOAD DIES.
     *
     * <p>Order: this mod's own layer, then the same layer baked straight from its own
     * definition, then VANILLA'S PLAYER SKELETON. The last one is the guarantee -- it is
     * the mesh every vanilla humanoid in the game is built on, so it cannot be missing
     * the parts {@code HumanoidModel} requires. A mob with the player's skeleton for one
     * session is a wrong-looking mob; a throw here is a black screen for everyone.
     */
    public static ModelPart humanoidRoot(EntityRendererProvider.Context ctx,
            ModelLayerLocation layer, String tag) {
        ModelPart root = null;
        try {
            root = ctx.bakeLayer(layer);
            if (!complete(root)) {
                System.err.println("[ds] the " + tag + " model baked without the parts a "
                        + "humanoid body needs -- using vanilla's own skeleton instead");
                root = null;
            }
        } catch (Throwable t) {
            System.err.println("[ds] the " + tag + " model layer could not be baked: " + t);
            root = null;
        }
        if (root == null) {
            try {
                root = ctx.bakeLayer(ModelLayers.PLAYER);
            } catch (Throwable t) {
                // nothing left to fall back to: the game's own model set is the problem
                System.err.println("[ds] not even the player model could be baked: " + t);
            }
        }
        return root;
    }

    /**
     * The seven parts a HumanoidModel looks for, in player proportions scaled by
     * {@code s}, with the whole skeleton shifted so its feet land on the ground.
     *
     * <p>Layout, unscaled, +y downward: head from {@code -headH} to 0, body from 0
     * to {@code bodyH}, legs from {@code bodyH} to {@code bodyH + legH}. The shift
     * is {@code 24 - (bodyH + legH) * s}, so the feet always sit at 24.
     */
    static PartDefinition humanoid(MeshDefinition mesh, float s,
            float headW, float headH, float headD,
            float bodyW, float bodyH, float bodyD,
            float armW, float armH, float armD,
            float legW, float legH, float legD) {
        PartDefinition root = mesh.getRoot();
        float g = GROUND - (bodyH + legH) * s;
        float hw = headW / 2.0F;
        box(root, "head", 0, 0, s, 0.0F, g, 0.0F,
                -hw, -headH, -headD / 2.0F, headW, headH, headD);
        hatPart(root, s, g);
        float bw = bodyW / 2.0F;
        box(root, "body", 16, 16, s, 0.0F, g, 0.0F,
                -bw, 0.0F, -bodyD / 2.0F, bodyW, bodyH, bodyD);
        float aw = armW / 2.0F;
        box(root, "right_arm", 40, 16, s, -5.0F, g + 2.0F, 0.0F,
                -aw - 1.0F, -2.0F, -armD / 2.0F, armW, armH, armD);
        box(root, "left_arm", 40, 16, s, 5.0F, g + 2.0F, 0.0F,
                1.0F - aw, -2.0F, -armD / 2.0F, armW, armH, armD);
        float lw = legW / 2.0F;
        box(root, "right_leg", 0, 16, s, -1.9F, g + bodyH, 0.0F,
                -lw, 0.0F, -legD / 2.0F, legW, legH, legD);
        box(root, "left_leg", 0, 16, s, 1.9F, g + bodyH, 0.0F,
                lw - legW, 0.0F, -legD / 2.0F, legW, legH, legD);
        return root;
    }

    // ------------------------------------------------------------------
    // BUILD #477 -- THE GUARDED FACTORIES.
    //
    // Each one is the only way a renderer builds its body now: bake this mod's layer
    // through humanoidRoot() (which cannot hand back a skeleton without the parts
    // HumanoidModel reads), then construct the model, and if the CONSTRUCTION still
    // fails for any reason at all, report it and hand back the same model on vanilla's
    // player skeleton rather than letting the throw escape into the resource reload.
    // ------------------------------------------------------------------

    private static <M> M guarded(String tag, ModelPart root, ModelStage<M> stage, ModelPart spare) {
        if (root != null) {
            try {
                return stage.build(root);
            } catch (Throwable t) {
                System.err.println("[ds] the " + tag + " body could not be built on this "
                        + "mod's own skeleton (" + t + ") -- using vanilla's");
            }
        }
        if (spare != null) {
            try {
                return stage.build(spare);
            } catch (Throwable t) {
                System.err.println("[ds] the " + tag + " body could not be built at all: " + t);
            }
        }
        return null;
    }

    /** One model constructor, as a value. */
    public interface ModelStage<M> {
        M build(ModelPart root);
    }

    public static MassgModel massg(EntityRendererProvider.Context ctx) {
        return guarded("Massg", humanoidRoot(ctx, McsmMobRenderers.MASSG_LAYER, "Massg"),
                MassgModel::new, spareRoot(ctx));
    }

    public static CreatorModel creator(EntityRendererProvider.Context ctx) {
        return guarded("Creator", humanoidRoot(ctx, McsmMobRenderers.CREATOR_LAYER, "Creator"),
                CreatorModel::new, spareRoot(ctx));
    }

    public static VoidwalkerModel voidwalker(EntityRendererProvider.Context ctx) {
        return guarded("voidwalker",
                humanoidRoot(ctx, McsmMobRenderers.VOIDWALKER_LAYER, "voidwalker"),
                VoidwalkerModel::new, spareRoot(ctx));
    }

    public static VoidLurkerModel lurker(EntityRendererProvider.Context ctx) {
        return guarded("lurker",
                humanoidRoot(ctx, McsmMobRenderers.VOID_LURKER_LAYER, "lurker"),
                VoidLurkerModel::new, spareRoot(ctx));
    }

    public static DrifterModel drifter(EntityRendererProvider.Context ctx) {
        return guarded("drifter",
                humanoidRoot(ctx, McsmMobRenderers.DRIFTER_LAYER, "drifter"),
                DrifterModel::new, spareRoot(ctx));
    }

    public static KeeperModel keeper(EntityRendererProvider.Context ctx) {
        return guarded("keeper",
                humanoidRoot(ctx, McsmMobRenderers.KEEPER_LAYER, "keeper"),
                KeeperModel::new, spareRoot(ctx));
    }

    /** Vanilla's own humanoid skeleton: the last body any of them can fall back to. */
    private static ModelPart spareRoot(EntityRendererProvider.Context ctx) {
        try {
            return ctx.bakeLayer(ModelLayers.PLAYER);
        } catch (Throwable t) {
            System.err.println("[ds] no spare skeleton available: " + t);
            return null;
        }
    }

    // ==================================================================
    // MAS -- THE BLACK WARDEN. Six times a player: a long coat, two horns
    // swept back over the shoulders, a heavy brow, two red lenses.
    // ==================================================================
    public static final class MassgModel extends HumanoidModel<MobState> {

        public static final float S = 6.0F;

        private final ModelPart hornL;
        private final ModelPart hornR;
        private final ModelPart coat;
        private final ModelPart lenses;

        public MassgModel(ModelPart root) {
            super(root);
            ModelPart head = part(root, "head");
            ModelPart body = part(root, "body");
            this.hornL = part(head, "horn_l");
            this.hornR = part(head, "horn_r");
            this.lenses = part(head, "lenses");
            this.coat = part(body, "coat");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = humanoid(mesh, S, 10.0F, 10.0F, 10.0F, 16.0F, 22.0F, 8.0F,
                    6.0F, 22.0F, 6.0F, 8.0F, 24.0F, 8.0F);
            PartDefinition head = root.getChild("head");
            PartDefinition body = root.getChild("body");
            // two horns, swept back and out over the shoulders
            box(head, "horn_l", 64, 0, S, 2.0F, -7.0F, 2.0F, 0.0F, -4.0F, -1.0F, 3.0F, 3.0F, 16.0F);
            box(head, "horn_r", 64, 24, S, -5.0F, -7.0F, 2.0F, 0.0F, -4.0F, -1.0F, 3.0F, 3.0F, 16.0F);
            // the red: two lenses on their own part, so the body can stay black
            many(head, "lenses", 0, 384, S, 0.0F, -4.0F, -5.2F,
                    new float[] { -4.4F, -1.2F, 0.0F, 3.6F, 2.2F, 0.8F },
                    new float[] { 0.8F, -1.2F, 0.0F, 3.6F, 2.2F, 0.8F });
            // the coat: a wide skirt that reaches the ground and sways as it walks
            box(body, "coat", 80, 48, S, 0.0F, 18.0F, 0.0F, -11.0F, 0.0F, -6.0F, 22.0F, 12.0F, 12.0F);
            return LayerDefinition.create(mesh, 128, 128);
        }

        @Override
        public void setupAnim(MobState s) {
            super.setupAnim(s);
            float t = s.age;
            // it hunches: the head is always forward, the shoulders roll inward
            this.head.xRot += 0.16F + Mth.sin(t * 0.11F) * 0.05F;
            this.body.xRot += 0.10F + Mth.sin(t * 0.09F) * 0.03F;
            this.rightArm.xRot = this.rightArm.xRot * 0.7F + 0.30F;
            this.leftArm.xRot = this.leftArm.xRot * 0.7F + 0.30F;
            this.rightArm.zRot -= 0.14F;
            this.leftArm.zRot += 0.14F;
            // the horns follow the head, the coat follows the walk
            this.hornL.xRot = this.head.xRot * 0.4F + 0.22F;
            this.hornR.xRot = this.head.xRot * 0.4F + 0.22F;
            this.coat.zRot = Mth.sin(t * 0.07F) * 0.05F;
            this.coat.xRot = -this.body.xRot * 0.5F + Mth.sin(t * 0.13F) * 0.02F;
            // the lenses are set in the skull, so they only take a little of it
            this.lenses.xRot = this.head.xRot * 0.2F;
        }
    }

    // ==================================================================
    // THE VOIDWALKER. Player-scale, person-shaped, wrong.
    // ==================================================================
    public static final class VoidwalkerModel extends HumanoidModel<MobState> {

        public static final float S = 1.0F;

        private final ModelPart jaw;
        private final ModelPart tatter;
        private final ModelPart eyes;

        public VoidwalkerModel(ModelPart root) {
            super(root);
            this.jaw = part(part(root, "head"), "jaw");
            this.eyes = part(part(root, "head"), "eyes");
            this.tatter = part(part(root, "body"), "tatter");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = humanoid(mesh, S, 8.0F, 8.0F, 8.0F, 8.0F, 14.0F, 4.0F,
                    4.0F, 14.0F, 4.0F, 4.0F, 14.0F, 4.0F);
            PartDefinition head = root.getChild("head");
            PartDefinition body = root.getChild("body");
            // the jaw hangs open, a little under the skull
            box(head, "jaw", 32, 0, S, 0.0F, 7.0F, 1.0F, -3.0F, 0.0F, -3.0F, 6.0F, 3.0F, 6.0F);
            many(head, "eyes", 56, 40, S, 0.0F, -5.0F, -4.4F,
                    new float[] { -3.2F, -0.8F, 0.0F, 2.4F, 1.6F, 0.6F },
                    new float[] { 0.8F, -0.8F, 0.0F, 2.4F, 1.6F, 0.6F });
            // a strip of whatever it used to be wearing, still hanging off the hip
            box(body, "tatter", 32, 16, S, -3.0F, 4.0F, 0.0F, 0.0F, 0.0F, -2.6F, 3.0F, 12.0F, 1.0F);
            return LayerDefinition.create(mesh, 64, 64);
        }

        @Override
        public void setupAnim(MobState s) {
            super.setupAnim(s);
            float t = s.age;
            // one arm is up, one arm hangs: the walk that is not quite a walk
            this.rightArm.xRot = -1.9F + Mth.sin(t * 0.08F) * 0.10F;
            this.leftArm.xRot = this.leftArm.xRot * 0.4F + 0.55F;
            this.head.xRot += 0.28F;
            this.head.yRot += Mth.sin(t * 0.045F) * 0.10F;
            this.body.xRot += 0.10F;
            // the jaw works on its own clock, and the eyes never move with the head
            this.jaw.xRot = this.head.xRot * 0.5F + 0.22F + Math.abs(Mth.sin(t * 0.05F)) * 0.30F;
            this.tatter.xRot = -this.body.xRot * 0.6F + Mth.sin(t * 0.06F) * 0.08F;
            this.eyes.xRot = this.head.xRot * 0.3F;
        }
    }

    // ==================================================================
    // THE VOID LURKER. The one to avoid: a maw, and six tentacles that
    // reach -- each tip lit, each whipping one beat behind its arm.
    // ==================================================================
    public static final class VoidLurkerModel extends HumanoidModel<MobState> {

        public static final float S = 2.4F;
        public static final int TENTACLES = 6;

        private final ModelPart[] tentacles = new ModelPart[TENTACLES];
        private final ModelPart[] tips = new ModelPart[TENTACLES];
        private final ModelPart maw;

        public VoidLurkerModel(ModelPart root) {
            super(root);
            ModelPart body = part(root, "body");
            for (int i = 0; i < TENTACLES; i++) {
                this.tentacles[i] = part(body, "tentacle" + i);
                this.tips[i] = part(body, "tip" + i);
            }
            this.maw = part(part(root, "head"), "maw");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = humanoid(mesh, S, 12.0F, 12.0F, 12.0F, 16.0F, 16.0F, 12.0F,
                    5.0F, 14.0F, 5.0F, 6.0F, 12.0F, 6.0F);
            PartDefinition head = root.getChild("head");
            PartDefinition body = root.getChild("body");
            // the maw: a jaw that opens downward, under the face
            box(head, "maw", 160, 160, S, 0.0F, 8.0F, 1.0F, -5.0F, 0.0F, -5.0F, 10.0F, 6.0F, 8.0F);
            // six tentacles, three a side, each hanging from the body's underside
            // with a lit tip at the end of it
            for (int i = 0; i < TENTACLES; i++) {
                float side = (i % 2 == 0) ? -1.0F : 1.0F;
                float lane = (i / 2) - 1.0F;
                float px = side * 6.0F;
                float pz = lane * 5.0F;
                box(body, "tentacle" + i, 96, 40, S, px, 15.0F, pz,
                        -1.5F, 0.0F, -1.5F, 3.0F, 30.0F, 3.0F);
                box(body, "tip" + i, 96, 160, S, px, 15.0F, pz,
                        -1.5F, 30.0F, -1.5F, 3.0F, 4.0F, 3.0F);
            }
            return LayerDefinition.create(mesh, 256, 256);
        }

        @Override
        public void setupAnim(MobState s) {
            super.setupAnim(s);
            float t = s.age;
            this.head.xRot += 0.12F;
            this.body.xRot += 0.08F + Mth.sin(t * 0.07F) * 0.04F;
            // the arms are short and forward: the mouth does the work
            this.rightArm.xRot = -1.15F + Mth.sin(t * 0.09F) * 0.12F;
            this.leftArm.xRot = -1.15F - Mth.sin(t * 0.09F) * 0.12F;
            this.rightArm.zRot = -0.25F;
            this.leftArm.zRot = 0.25F;
            this.maw.xRot = 0.30F + Math.abs(Mth.sin(t * 0.05F)) * 0.45F;
            for (int i = 0; i < TENTACLES; i++) {
                float phase = t * 0.06F + i * 1.05F;
                float reach = Mth.sin(phase) * 0.55F;
                this.tentacles[i].xRot = -this.body.xRot + reach;
                this.tentacles[i].zRot = Mth.cos(phase * 0.8F) * 0.30F;
                this.tips[i].xRot = reach * 0.8F + Mth.sin(phase - 0.6F) * 0.35F;
                this.tips[i].zRot = Mth.cos(phase * 0.8F - 0.5F) * 0.35F;
            }
        }
    }

    // ==================================================================
    // THE CREATOR. Eight times a player: crowned, haloed, and standing on
    // the world with two arms that come down out of the sky.
    // ==================================================================
    public static final class CreatorModel extends HumanoidModel<MobState> {

        public static final float S = 8.0F;
        public static final int HALOS = 3;

        private final ModelPart[] halos = new ModelPart[HALOS];
        private final ModelPart skyL;
        private final ModelPart skyR;
        private final ModelPart eyes;

        public CreatorModel(ModelPart root) {
            super(root);
            ModelPart head = part(root, "head");
            for (int i = 0; i < HALOS; i++) {
                this.halos[i] = part(head, "halo" + i);
            }
            this.eyes = part(head, "eyes");
            this.skyL = part(part(root, "left_arm"), "sky_arm_l");
            this.skyR = part(part(root, "right_arm"), "sky_arm_r");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = humanoid(mesh, S, 12.0F, 12.0F, 12.0F, 20.0F, 26.0F, 10.0F,
                    6.0F, 30.0F, 6.0F, 9.0F, 28.0F, 9.0F);
            PartDefinition head = root.getChild("head");
            // the crown: flat rings, each wider than the last, each turning
            for (int i = 0; i < HALOS; i++) {
                float w = 10.0F + i * 4.0F;
                box(head, "halo" + i, 96, 512, S, 0.0F, -12.0F - i * 3.0F, 0.0F,
                        -w, 0.0F, -w / 2.0F, w * 2.0F, 1.0F, w);
            }
            // the eyes: white-hot, on their own part
            many(head, "eyes", 0, 512, S, 0.0F, -6.0F, -6.6F,
                    new float[] { -4.6F, -1.0F, 0.0F, 3.6F, 2.0F, 0.6F },
                    new float[] { 1.0F, -1.0F, 0.0F, 3.6F, 2.0F, 0.6F });
            // the arms that come out of the sky: thin, and two hundred units long
            box(root.getChild("left_arm"), "sky_arm_l", 600, 96, S, 3.0F, -2.0F, 0.0F,
                    -1.5F, -200.0F, -1.5F, 3.0F, 200.0F, 3.0F);
            box(root.getChild("right_arm"), "sky_arm_r", 600, 96, S, -3.0F, -2.0F, 0.0F,
                    -1.5F, -200.0F, -1.5F, 3.0F, 200.0F, 3.0F);
            return LayerDefinition.create(mesh, 512, 512);
        }

        @Override
        public void setupAnim(MobState s) {
            super.setupAnim(s);
            float t = s.age;
            // it barely moves, and that is the point: the halos do the moving
            this.head.xRot += 0.10F + Mth.sin(t * 0.03F) * 0.03F;
            this.body.xRot += 0.04F;
            this.rightArm.xRot = 0.25F + Mth.sin(t * 0.025F) * 0.08F;
            this.leftArm.xRot = 0.25F - Mth.sin(t * 0.025F) * 0.08F;
            for (int i = 0; i < HALOS; i++) {
                this.halos[i].yRot = t * (0.010F + i * 0.004F);
                this.halos[i].xRot = Mth.sin(t * 0.02F + i) * 0.10F;
            }
            // the sky arms sway the way something enormous sways: slowly, and a
            // little out of step with each other
            this.skyL.zRot = Mth.sin(t * 0.018F) * 0.18F + 0.06F;
            this.skyR.zRot = -Mth.sin(t * 0.018F + 0.7F) * 0.18F - 0.06F;
            this.skyL.xRot = this.head.xRot * 0.3F;
            this.skyR.xRot = this.head.xRot * 0.3F;
            this.eyes.xRot = this.head.xRot * 0.25F;
        }
    }

    // ==================================================================
    // BUILD #460 -- THE DRIFTER. The decayed reality's own: hooded, rags
    // to the knee, ash coming off it, ash where its face should be.
    // ==================================================================
    public static final class DrifterModel extends HumanoidModel<MobState> {

        public static final float S = 1.0F;

        private final ModelPart hood;
        private final ModelPart eyes;
        private final ModelPart tatterL;
        private final ModelPart tatterR;

        public DrifterModel(ModelPart root) {
            super(root);
            ModelPart head = part(root, "head");
            ModelPart body = part(root, "body");
            this.hood = part(head, "hood");
            this.eyes = part(head, "eyes");
            this.tatterL = part(body, "tatter_l");
            this.tatterR = part(body, "tatter_r");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = humanoid(mesh, S, 8.0F, 8.0F, 8.0F, 8.0F, 14.0F, 4.0F,
                    4.0F, 14.0F, 4.0F, 4.0F, 14.0F, 4.0F);
            PartDefinition head = root.getChild("head");
            PartDefinition body = root.getChild("body");
            // the hood: one flat slab over the skull, and it falls with the head
            box(head, "hood", 32, 0, S, 0.0F, -8.0F, 0.0F, -5.0F, -3.0F, -5.0F, 10.0F, 3.0F, 10.0F);
            // where the face is: two slits, the only violet on it, on their own part
            many(head, "eyes", 72, 0, S, 0.0F, -4.0F, -4.2F,
                    new float[] { -2.6F, -0.8F, 0.0F, 2.0F, 1.6F, 0.6F },
                    new float[] { 0.6F, -0.8F, 0.0F, 2.0F, 1.6F, 0.6F });
            // two strips of what it used to wear, hanging off the body
            box(body, "tatter_l", 56, 16, S, -2.4F, 13.0F, 0.0F, 0.0F, 0.0F, -2.4F, 3.0F, 12.0F, 1.0F);
            box(body, "tatter_r", 64, 16, S, 2.4F, 13.0F, 0.0F, -3.0F, 0.0F, -2.4F, 3.0F, 12.0F, 1.0F);
            return LayerDefinition.create(mesh, 128, 128);
        }

        @Override
        public void setupAnim(MobState s) {
            super.setupAnim(s);
            float t = s.age;
            // it lopes: low, head down, and the rags keep going after it stops
            this.head.xRot += 0.20F;
            this.body.xRot += 0.13F + Mth.sin(t * 0.10F) * 0.04F;
            this.rightArm.xRot = this.rightArm.xRot * 0.6F + 0.35F;
            this.leftArm.xRot = this.leftArm.xRot * 0.6F + 0.35F;
            this.rightArm.zRot -= 0.18F;
            this.leftArm.zRot += 0.18F;
            // and every so often the whole thing is somewhere else for a frame
            this.hood.xRot = this.head.xRot * 0.3F;
            this.tatterL.xRot = -this.body.xRot * 0.7F + Mth.sin(t * 0.22F) * 0.16F;
            this.tatterR.xRot = -this.body.xRot * 0.7F + Mth.sin(t * 0.22F + 1.3F) * 0.16F;
            this.eyes.xRot = this.head.xRot * 0.4F;
        }
    }

    // ==================================================================
    // BUILD #460 -- THE KEEPER. The infinite dimension's own: tall, brimmed,
    // a lantern in one hand, and it walks like it has all the time there is.
    // ==================================================================
    public static final class KeeperModel extends HumanoidModel<MobState> {

        public static final float S = 1.0F;

        private final ModelPart brim;
        private final ModelPart eyes;
        private final ModelPart lantern;
        private final ModelPart lamp;

        public KeeperModel(ModelPart root) {
            super(root);
            ModelPart head = part(root, "head");
            this.brim = part(head, "brim");
            this.eyes = part(head, "eyes");
            this.lantern = part(part(root, "right_arm"), "lantern");
            this.lamp = part(part(root, "right_arm"), "lamp");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = humanoid(mesh, S, 8.0F, 8.0F, 8.0F, 8.0F, 20.0F, 4.0F,
                    4.0F, 20.0F, 4.0F, 4.0F, 14.0F, 4.0F);
            PartDefinition head = root.getChild("head");
            PartDefinition arm = root.getChild("right_arm");
            // the brim: a flat ring around the skull, wider than the shoulders
            box(head, "brim", 32, 0, S, 0.0F, -7.0F, 0.0F, -7.0F, 0.0F, -7.0F, 14.0F, 1.0F, 14.0F);
            // its eyes are the same colour as everything it keeps
            many(head, "eyes", 128, 0, S, 0.0F, -4.0F, -4.2F,
                    new float[] { -2.6F, -0.8F, 0.0F, 2.0F, 1.6F, 0.6F },
                    new float[] { 0.6F, -0.8F, 0.0F, 2.0F, 1.6F, 0.6F });
            // the lantern: a box in the hand, and the light inside it on its own part
            box(arm, "lantern", 96, 0, S, 0.0F, 18.0F, 2.6F, -2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F);
            many(arm, "lamp", 112, 0, S, 0.0F, 18.0F, 2.6F,
                    new float[] { -1.5F, 1.0F, -1.5F, 3.0F, 3.0F, 3.0F });
            return LayerDefinition.create(mesh, 256, 256);
        }

        @Override
        public void setupAnim(MobState s) {
            super.setupAnim(s);
            float t = s.age;
            // no hurry anywhere in it: the walk is slow, the lantern swings
            this.head.xRot += 0.06F;
            this.rightArm.xRot = this.rightArm.xRot * 0.5F + 0.75F + Mth.sin(t * 0.03F) * 0.06F;
            this.leftArm.xRot = this.leftArm.xRot * 0.7F + 0.10F;
            this.brim.yRot = Mth.sin(t * 0.012F) * 0.22F;
            this.brim.xRot = this.head.xRot * 0.5F;
            this.eyes.xRot = this.head.xRot * 0.3F;
            // the lantern hangs from the hand: it takes the arm's swing a beat late
            this.lantern.xRot = -this.rightArm.xRot + Mth.sin(t * 0.03F - 0.5F) * 0.10F;
            this.lamp.xRot = this.lantern.xRot;
            this.lamp.zRot = Mth.sin(t * 0.05F) * 0.05F;
        }
    }
}
