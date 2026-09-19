package net.mcsm.extras.client;

import net.mcsm.extras.entity.StoryCharacterEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * BUILD #466 -- THE CAST GET BODIES OF THEIR OWN.
 *
 * <p>THE MANDATE, twice over: "custom NPC models" and "custom NPCs + dialogue,
 * corrupted NPCs" -- with the note that the NPCs were "re-kitted vanilla mobs".
 * That note was literally true here: this renderer baked {@code ModelLayers.PLAYER}
 * (Minecraft's own player mesh, hat layer and all) and put a Story Mode skin on it.
 * A cast member was a player-shaped mannequin wearing a villager's smile.
 *
 * <p>WHAT THIS IS NOW. Our own baked skeleton, {@link #LAYER}: the player's own
 * proportions and rects for the seven body parts -- so the twenty skins this mod
 * already carries (src/main/resources/assets/dabywitherstormmod/textures/entity/
 * story, jesse through stampy) still map onto the body pixel for pixel and none of
 * the existing art is repainted or replaced -- and then five parts the player model
 * does not have:
 *
 * <pre>
 *   hood     over the head, from the hat-layer rect (32,0)
 *   collar   the shoulder wrap, from the body-layer rect (16,32)
 *   coat     the long coat, hanging past the hips, same fabric as the collar
 *   sleeves  both arms, from the arm-layer rects (40,32) and (48,32)
 *   wraps    both legs, from the leg-layer rects (0,32) and (0,48)
 *   badge    a chest tag, cut from the body layer
 * </pre>
 *
 * <p>EVERY PIXEL OF THE NEW GEOMETRY COMES FROM THE CHARACTER'S OWN SECOND LAYER.
 * That is the layer a player skin uses for its jacket and its hood -- so the cast
 * is not wearing OUR idea of their clothes, they are wearing theirs, and a skin
 * whose second layer is empty simply has no coat. Nothing invented, nothing
 * repainted, and no new textures to keep in sync with anything.
 *
 * <p>AND THE CORRUPTED HALF. The storm does to the cast what it does to the world:
 * {@link State#corruption} comes from the client's own phase tracker, and once the
 * storm is past the phase where it starts taking people, the body stops keeping its
 * shape -- the head jerks, the shoulders twist, the coat drags, the legs stiffen.
 * It is the same model, contaminated, which is what "corrupted NPCs" should be.
 *
 * <p>Animations stay in the two channels the dialogue engine already drives (TALK
 * and LAUGH) and in the three rotation fields this codebase already writes. The
 * coat is animated off the legs, so a walking character's coat moves without this
 * class touching a single field of the render state beyond the ones the old
 * renderer touched.
 */
public class StoryCharacterRenderer extends HumanoidMobRenderer<StoryCharacterEntity,
        StoryCharacterRenderer.State, StoryCharacterRenderer.Model> {

    /** Our own layer: this is what makes the cast our bodies instead of the player's. */
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("dabywitherstormmod", "story_character"), "main");

    /** The phase the storm starts taking people: below this, the cast is whole. */
    private static final float CORRUPT_FROM = 5.5F;
    private static final float CORRUPT_FULL = 7.5F;

    public static final class State extends HumanoidRenderState {
        public String character = "jesse";
        public boolean talking;
        public boolean laughing;
        public float actTime;
        /** 0 = whole, 1 = the storm has as much of them as it is going to get. */
        public float corruption;
    }

    /**
     * The layer: the player skeleton (same rects, same proportions, same skin
     * layout) plus the street clothes. Nothing here has geometry the player model
     * has and nothing about the player's body is written differently, which is what
     * keeps twenty existing skins looking the way their artist drew them.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        // BUILD #478 -- the returns are KEPT. This layer used to create these parts and
        // then look them up again by name (`root.getChild("body")`), and a lookup that
        // misses throws -- during the bake, which is inside the resource reload. Holding
        // the definition the call just returned cannot miss.
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48)
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        // ---- the hat layer: in a player skin this is the hood, the helmet, the
        // hair that hangs over the face. Here it is a hood, and it is theirs.
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.55F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        hat.addOrReplaceChild("hood_back", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0F, -1.5F, 2.0F, 8.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // the collar: sits on the shoulders, cut from the body layer
        body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(16, 32)
                .addBox(-4.6F, -1.6F, -2.6F, 9.2F, 3.0F, 5.2F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        // the coat: past the hips, so the silhouette is a person in a coat and not
        // a rectangle in trousers
        body.addOrReplaceChild("coat", CubeListBuilder.create().texOffs(16, 32)
                .addBox(-4.8F, 5.0F, -2.8F, 9.6F, 8.4F, 5.6F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        // the badge: something the cast was given before any of this
        body.addOrReplaceChild("badge", CubeListBuilder.create().texOffs(16, 32)
                .addBox(-1.5F, 1.6F, -3.05F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        rightArm.addOrReplaceChild("sleeve_r", CubeListBuilder.create()
                .texOffs(40, 32)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.26F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("sleeve_l", CubeListBuilder.create()
                .texOffs(48, 32)
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.26F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("wrap_r", CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.22F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("wrap_l", CubeListBuilder.create()
                .texOffs(0, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.22F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public static final class Model extends HumanoidModel<State> {

        private final ModelPart hood;
        private final ModelPart hoodBack;
        private final ModelPart collar;
        private final ModelPart coat;
        private final ModelPart badge;

        public Model(ModelPart root) {
            super(root);
            // BUILD #478 -- through the guarded lookup, like every part in the mob
            // bodies. A missing piece animates as its parent instead of throwing, and
            // this constructor runs inside the resource reload, where a throw is not
            // one broken NPC: it is every pack dropped and a black frame.
            this.hood = this.hat;
            this.hoodBack = McsmMobModels.part(this.hat, "hood_back");
            this.collar = McsmMobModels.part(this.body, "collar");
            this.coat = McsmMobModels.part(this.body, "coat");
            this.badge = McsmMobModels.part(this.body, "badge");
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            float t = s.actTime;

            // the coat moves because the legs do: nothing extra to read, and a
            // standing character's coat is still
            this.coat.xRot = this.rightLeg.xRot * 0.32F + this.leftLeg.xRot * 0.32F;
            this.coat.zRot = (this.rightLeg.zRot + this.leftLeg.zRot) * 0.25F;
            this.collar.xRot = -0.06F + this.body.xRot * 0.3F;
            this.hoodBack.xRot = 0.10F + this.head.xRot * 0.5F;

            if (s.laughing) {
                float bob = Mth.sin(t * 1.1F);
                this.head.xRot += 0.18F + bob * 0.16F;
                this.body.xRot += 0.05F + bob * 0.04F;
                this.rightArm.xRot = -0.9F + bob * 0.25F;
                this.leftArm.xRot = -0.9F - bob * 0.25F;
                this.rightArm.zRot = -0.35F;
                this.leftArm.zRot = 0.35F;
                this.rightArm.yRot = 0.0F;
                this.leftArm.yRot = 0.0F;
                this.coat.xRot -= 0.10F + bob * 0.06F;
                this.badge.zRot = bob * 0.2F;
            } else if (s.talking) {
                float nod = Mth.sin(t * 0.35F) * 0.10F + Mth.sin(t * 0.9F) * 0.05F;
                this.head.xRot += nod;
                this.head.zRot += Mth.sin(t * 0.22F) * 0.06F;
                // gesturing right arm: raised to chest height, waving forward/back
                float wave = Mth.sin(t * 0.6F);
                this.rightArm.xRot = -1.25F + wave * 0.35F;
                this.rightArm.yRot = -0.25F + wave * 0.15F;
                this.rightArm.zRot = -0.15F;
                this.leftArm.xRot *= 0.4F;
                this.leftArm.zRot = 0.08F + Mth.sin(t * 0.3F) * 0.04F;
                this.body.yRot += Mth.sin(t * 0.2F) * 0.04F;
            } else {
                // idle: a slow shift of weight, so a standing cast is not a statue
                this.body.zRot += Mth.sin(t * 0.03F) * 0.02F;
                this.badge.zRot = Mth.sin(t * 0.04F) * 0.05F;
            }

            this.hat.xRot = this.head.xRot;
            this.hat.yRot = this.head.yRot;
            this.hat.zRot = this.head.zRot;

            // ---- corrupted: the storm is in them. Past the phase where it starts
            // taking people the body stops holding its shape -- the same model,
            // contaminated, at a rate that grows with how far it has come.
            if (s.corruption > 0.01F) {
                float k = s.corruption;
                this.head.zRot += Mth.sin(t * 0.9F) * 0.24F * k;
                this.head.xRot += Mth.sin(t * 1.7F) * 0.11F * k + 0.12F * k;
                this.hat.zRot = this.head.zRot;
                this.hat.xRot = this.head.xRot;
                this.body.zRot += Mth.sin(t * 0.55F) * 0.13F * k;
                this.body.xRot += 0.10F * k;
                this.rightArm.zRot -= 0.42F * k;
                this.leftArm.zRot += 0.42F * k;
                this.rightArm.xRot += Mth.sin(t * 1.3F) * 0.16F * k;
                this.leftArm.xRot += Mth.sin(t * 1.1F + 1.4F) * 0.16F * k;
                this.coat.xRot -= 0.30F * k;
                this.collar.zRot = Mth.sin(t * 0.8F) * 0.22F * k;
                this.hoodBack.xRot -= 0.25F * k;
                this.badge.zRot += Mth.sin(t * 2.1F) * 0.35F * k;
                this.rightLeg.zRot += 0.12F * k;
                this.leftLeg.zRot -= 0.12F * k;
            }
        }
    }

    private static final Identifier FALLBACK = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/entity/story/jesse.png");

    public StoryCharacterRenderer(EntityRendererProvider.Context ctx) {
        // BUILD #478 -- THE LAST UNGUARDED BAKE. This constructor is called by
        // EntityRenderers.createEntityRenderers, which runs INSIDE the resource
        // reload: a bake or a construction that throws here does not cost the cast,
        // it drops every resource pack and the client comes up black -- the player's
        // own log, one class over from the whale. The cast now goes through the same
        // guarded factory as the mobs: this mod's skeleton when it is complete,
        // vanilla's own player skeleton otherwise, and never a throw.
        super(ctx, McsmMobModels.guarded("cast",
                McsmMobModels.humanoidRoot(ctx, LAYER, "cast"),
                Model::new, McsmMobModels.spareRoot(ctx)), 0.5F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StoryCharacterEntity e, State s, float partialTick) {
        super.extractRenderState(e, s, partialTick);
        s.character = e.getCharacter();
        s.talking = e.isTalking();
        s.laughing = e.isLaughing();
        s.actTime = e.tickCount + partialTick;
        // the client's own phase tracker: no world scan per character per frame
        float phase;
        try {
            phase = McsmStormPhase.phase();
        } catch (Throwable t) {
            phase = 0.0F;
        }
        s.corruption = Mth.clamp((phase - CORRUPT_FROM) / (CORRUPT_FULL - CORRUPT_FROM), 0.0F, 1.0F);
    }

    @Override
    public Identifier getTextureLocation(State s) {
        String c = s.character == null ? "jesse" : s.character;
        if (!c.matches("[a-z0-9_]+")) {
            return FALLBACK;
        }
        return Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/story/" + c + ".png");
    }
}
