package net.dabicco.witherstormmod.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.dabicco.witherstormmod.client.ClientConfigCache;
import net.dabicco.witherstormmod.client.SnatchGrab;
import net.dabicco.witherstormmod.client.TentacleRagdoll;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.animation.PhaseStormAnim;
import net.dabicco.witherstormmod.entity.BigTentacleShape;
import net.dabicco.witherstormmod.entity.CollapseAnim;
import net.dabicco.witherstormmod.entity.animation.WitherStormTentacles5Animation;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.dabicco.witherstormmod.mixin.ModelPartAccessor;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.AnimationDefinition.Builder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class WitherStormTentacles5 extends EntityModel<WitherStormRenderState> {
   private final ModelPart root;
   private final KeyframeAnimation[] smallSpawn;
   private final ModelPart[] smallRoots;
   private final List<ModelPart>[] smallChains;
   private final KeyframeAnimation largeIdle;
   private final ModelPart[] bigRoots;
   public boolean bigTentaclesOnly = false;
   public boolean singleBigTentacle = false;
   public boolean staticPose = false;
   public float staticPoseXRot = 0.0F;
   public float staticPoseZRot = 0.0F;
   public float staticCurl = 0.0F;
   private final List<ModelPart>[] bigChains;
   private final ModelPart upperBodyPart1;
   private final ModelPart bone67;
   private final ModelPart bone125;
   private final ModelPart tentacle6;
   private final ModelPart bone76;
   private final ModelPart bone77;
   private final ModelPart bone78;
   private final ModelPart bone79;
   private final ModelPart bone80;
   private final ModelPart bone81;
   private final ModelPart bone82;
   private final ModelPart bone83;
   private final ModelPart bone84;
   private final ModelPart bone85;
   private final ModelPart bone86;
   private final ModelPart bone87;
   private final ModelPart bone88;
   private final ModelPart bone89;
   private final ModelPart bone90;
   private final ModelPart tentacle7;
   private final ModelPart bone68;
   private final ModelPart bone69;
   private final ModelPart bone70;
   private final ModelPart bone71;
   private final ModelPart bone72;
   private final ModelPart bone73;
   private final ModelPart bone74;
   private final ModelPart bone75;
   private final ModelPart bone91;
   private final ModelPart bone92;
   private final ModelPart bone93;
   private final ModelPart bone94;
   private final ModelPart bone95;
   private final ModelPart bone96;
   private final ModelPart bone97;
   private final ModelPart tentacle8;
   private final ModelPart bone98;
   private final ModelPart bone99;
   private final ModelPart bone100;
   private final ModelPart bone101;
   private final ModelPart bone102;
   private final ModelPart bone103;
   private final ModelPart bone104;
   private final ModelPart bone105;
   private final ModelPart bone106;
   private final ModelPart bone107;
   private final ModelPart bone108;
   private final ModelPart bone109;
   private final ModelPart bone110;
   private final ModelPart bone111;
   private final ModelPart bone112;
   private final ModelPart tentacle9;
   private final ModelPart bone113;
   private final ModelPart bone114;
   private final ModelPart bone115;
   private final ModelPart bone116;
   private final ModelPart bone117;
   private final ModelPart bone118;
   private final ModelPart bone119;
   private final ModelPart bone120;
   private final ModelPart bone121;
   private final ModelPart bone122;
   private final ModelPart bone123;
   private final ModelPart bone124;
   private final ModelPart bone126;
   private final ModelPart bone127;
   private final ModelPart bone128;
   private final ModelPart tentacle10;
   private final ModelPart bone129;
   private final ModelPart bone130;
   private final ModelPart bone131;
   private final ModelPart bone132;
   private final ModelPart bone133;
   private final ModelPart bone134;
   private final ModelPart bone135;
   private final ModelPart bone136;
   private final ModelPart bone137;
   private final ModelPart bone138;
   private final ModelPart bone139;
   private final ModelPart bone140;
   private final ModelPart bone141;
   private final ModelPart bone142;
   private final ModelPart bone143;
   private final ModelPart bone144;
   private final ModelPart bone147;
   private final ModelPart bone148;
   private final ModelPart bone149;
   private final ModelPart bone150;
   private final ModelPart bone151;
   private final ModelPart bone152;
   private final ModelPart bone153;
   private final ModelPart bone154;
   private final ModelPart bone155;
   private final ModelPart bone156;
   private final ModelPart bone157;
   private final ModelPart bone158;
   private final ModelPart bone159;
   private final ModelPart bone160;
   private final ModelPart bone191;
   private final ModelPart bone176;
   private final ModelPart bone177;
   private final ModelPart bone178;
   private final ModelPart bone179;
   private final ModelPart bone180;
   private final ModelPart bone181;
   private final ModelPart bone182;
   private final ModelPart bone183;
   private final ModelPart bone184;
   private final ModelPart bone185;
   private final ModelPart bone186;
   private final ModelPart bone187;
   private final ModelPart bone188;
   private final ModelPart bone189;
   private final ModelPart bone190;
   private final ModelPart bone161;
   private final ModelPart bone162;
   private final ModelPart bone163;
   private final ModelPart bone164;
   private final ModelPart bone165;
   private final ModelPart bone166;
   private final ModelPart bone167;
   private final ModelPart bone168;
   private final ModelPart bone169;
   private final ModelPart bone170;
   private final ModelPart bone171;
   private final ModelPart bone172;
   private final ModelPart bone173;
   private final ModelPart bone174;
   private final ModelPart bone175;
   private static final float STAGGER_SEC = 1.6F;
   private static final float SPAWN_LEN_TICKS = 100.0F;
   private static final float BIG_POP_SEC = 0.6F;
   private static final double BIG_REVEAL_PHASE = 5.1;
   private static final float BIG_UNCURL_TICKS = 70.0F;
   private static final float BIG_STAGGER_TICKS = 18.0F;
   private static final float BIG_SIDE_CURL = 1.15F;
   public static final float PLACE_UP = 6.0F;
   public static final float PLACE_BACK = 1.25F;
   public static final float PLACE_SCALE = 5.0F;
   private static final float BIG_INWARD = 43.0F;
   private static final float BIG_UP = -1.0F;
   private static final float BIG_SCALE = 1.38F;
   private static final float SMALL_SCALE = 1.5F;
   private static final float SMALL_UP = 4.0F;
   private static final float HANG_AT = 0.1F;
   private static final float RECOVER_BY = 0.55F;
   private static final float HANG_DROP = 1.42F;
   private static final float TIP_LIFT = -0.62F;
   private static final float TIP_SIDE = 0.85F;
   private static final Map<Integer, Float> BIG_UNCURL_START = new HashMap<>();

   public WitherStormTentacles5(ModelPart root) {
      super(root);
      this.root = root;
      this.upperBodyPart1 = root.getChild("upperBodyPart1");
      this.bone67 = this.upperBodyPart1.getChild("bone67");
      this.bone125 = this.bone67.getChild("bone125");
      this.tentacle6 = this.bone125.getChild("tentacle6");
      this.bone76 = this.tentacle6.getChild("bone76");
      this.bone77 = this.bone76.getChild("bone77");
      this.bone78 = this.bone77.getChild("bone78");
      this.bone79 = this.bone78.getChild("bone79");
      this.bone80 = this.bone79.getChild("bone80");
      this.bone81 = this.bone80.getChild("bone81");
      this.bone82 = this.bone81.getChild("bone82");
      this.bone83 = this.bone82.getChild("bone83");
      this.bone84 = this.bone83.getChild("bone84");
      this.bone85 = this.bone84.getChild("bone85");
      this.bone86 = this.bone85.getChild("bone86");
      this.bone87 = this.bone86.getChild("bone87");
      this.bone88 = this.bone87.getChild("bone88");
      this.bone89 = this.bone88.getChild("bone89");
      this.bone90 = this.bone89.getChild("bone90");
      this.tentacle7 = this.bone125.getChild("tentacle7");
      this.bone68 = this.tentacle7.getChild("bone68");
      this.bone69 = this.bone68.getChild("bone69");
      this.bone70 = this.bone69.getChild("bone70");
      this.bone71 = this.bone70.getChild("bone71");
      this.bone72 = this.bone71.getChild("bone72");
      this.bone73 = this.bone72.getChild("bone73");
      this.bone74 = this.bone73.getChild("bone74");
      this.bone75 = this.bone74.getChild("bone75");
      this.bone91 = this.bone75.getChild("bone91");
      this.bone92 = this.bone91.getChild("bone92");
      this.bone93 = this.bone92.getChild("bone93");
      this.bone94 = this.bone93.getChild("bone94");
      this.bone95 = this.bone94.getChild("bone95");
      this.bone96 = this.bone95.getChild("bone96");
      this.bone97 = this.bone96.getChild("bone97");
      this.tentacle8 = this.bone125.getChild("tentacle8");
      this.bone98 = this.tentacle8.getChild("bone98");
      this.bone99 = this.bone98.getChild("bone99");
      this.bone100 = this.bone99.getChild("bone100");
      this.bone101 = this.bone100.getChild("bone101");
      this.bone102 = this.bone101.getChild("bone102");
      this.bone103 = this.bone102.getChild("bone103");
      this.bone104 = this.bone103.getChild("bone104");
      this.bone105 = this.bone104.getChild("bone105");
      this.bone106 = this.bone105.getChild("bone106");
      this.bone107 = this.bone106.getChild("bone107");
      this.bone108 = this.bone107.getChild("bone108");
      this.bone109 = this.bone108.getChild("bone109");
      this.bone110 = this.bone109.getChild("bone110");
      this.bone111 = this.bone110.getChild("bone111");
      this.bone112 = this.bone111.getChild("bone112");
      this.tentacle9 = this.bone125.getChild("tentacle9");
      this.bone113 = this.tentacle9.getChild("bone113");
      this.bone114 = this.bone113.getChild("bone114");
      this.bone115 = this.bone114.getChild("bone115");
      this.bone116 = this.bone115.getChild("bone116");
      this.bone117 = this.bone116.getChild("bone117");
      this.bone118 = this.bone117.getChild("bone118");
      this.bone119 = this.bone118.getChild("bone119");
      this.bone120 = this.bone119.getChild("bone120");
      this.bone121 = this.bone120.getChild("bone121");
      this.bone122 = this.bone121.getChild("bone122");
      this.bone123 = this.bone122.getChild("bone123");
      this.bone124 = this.bone123.getChild("bone124");
      this.bone126 = this.bone124.getChild("bone126");
      this.bone127 = this.bone126.getChild("bone127");
      this.bone128 = this.bone127.getChild("bone128");
      this.tentacle10 = this.bone125.getChild("tentacle10");
      this.bone129 = this.tentacle10.getChild("bone129");
      this.bone130 = this.bone129.getChild("bone130");
      this.bone131 = this.bone130.getChild("bone131");
      this.bone132 = this.bone131.getChild("bone132");
      this.bone133 = this.bone132.getChild("bone133");
      this.bone134 = this.bone133.getChild("bone134");
      this.bone135 = this.bone134.getChild("bone135");
      this.bone136 = this.bone135.getChild("bone136");
      this.bone137 = this.bone136.getChild("bone137");
      this.bone138 = this.bone137.getChild("bone138");
      this.bone139 = this.bone138.getChild("bone139");
      this.bone140 = this.bone139.getChild("bone140");
      this.bone141 = this.bone140.getChild("bone141");
      this.bone142 = this.bone141.getChild("bone142");
      this.bone143 = this.bone142.getChild("bone143");
      this.bone144 = this.bone125.getChild("bone144");
      this.bone147 = this.bone144.getChild("bone147");
      this.bone148 = this.bone147.getChild("bone148");
      this.bone149 = this.bone148.getChild("bone149");
      this.bone150 = this.bone149.getChild("bone150");
      this.bone151 = this.bone150.getChild("bone151");
      this.bone152 = this.bone151.getChild("bone152");
      this.bone153 = this.bone152.getChild("bone153");
      this.bone154 = this.bone153.getChild("bone154");
      this.bone155 = this.bone154.getChild("bone155");
      this.bone156 = this.bone155.getChild("bone156");
      this.bone157 = this.bone156.getChild("bone157");
      this.bone158 = this.bone157.getChild("bone158");
      this.bone159 = this.bone158.getChild("bone159");
      this.bone160 = this.bone159.getChild("bone160");
      this.bone191 = this.upperBodyPart1.getChild("bone191");
      this.bone176 = this.bone191.getChild("bone176");
      this.bone177 = this.bone176.getChild("bone177");
      this.bone178 = this.bone177.getChild("bone178");
      this.bone179 = this.bone178.getChild("bone179");
      this.bone180 = this.bone179.getChild("bone180");
      this.bone181 = this.bone180.getChild("bone181");
      this.bone182 = this.bone181.getChild("bone182");
      this.bone183 = this.bone182.getChild("bone183");
      this.bone184 = this.bone183.getChild("bone184");
      this.bone185 = this.bone184.getChild("bone185");
      this.bone186 = this.bone185.getChild("bone186");
      this.bone187 = this.bone186.getChild("bone187");
      this.bone188 = this.bone187.getChild("bone188");
      this.bone189 = this.bone188.getChild("bone189");
      this.bone190 = this.bone189.getChild("bone190");
      this.bone161 = this.bone191.getChild("bone161");
      this.bone162 = this.bone161.getChild("bone162");
      this.bone163 = this.bone162.getChild("bone163");
      this.bone164 = this.bone163.getChild("bone164");
      this.bone165 = this.bone164.getChild("bone165");
      this.bone166 = this.bone165.getChild("bone166");
      this.bone167 = this.bone166.getChild("bone167");
      this.bone168 = this.bone167.getChild("bone168");
      this.bone169 = this.bone168.getChild("bone169");
      this.bone170 = this.bone169.getChild("bone170");
      this.bone171 = this.bone170.getChild("bone171");
      this.bone172 = this.bone171.getChild("bone172");
      this.bone173 = this.bone172.getChild("bone173");
      this.bone174 = this.bone173.getChild("bone174");
      this.bone175 = this.bone174.getChild("bone175");
      Set<String> present = new HashSet<>();
      collectNames(root, present);
      this.largeIdle = filterToBones(WitherStormTentacles5Animation.large_idle, present).bake(root);
      this.smallRoots = new ModelPart[]{this.tentacle6, this.tentacle7, this.tentacle8, this.tentacle9, this.tentacle10, this.bone144};
      this.bigRoots = new ModelPart[]{this.bone176, this.bone161};
      List<ModelPart>[] bchains = new List[this.bigRoots.length];
      this.bigChains = bchains;

      for (int i = 0; i < this.bigRoots.length; i++) {
         List<ModelPart> parts = new ArrayList<>();
         collectParts(this.bigRoots[i], parts);
         this.bigChains[i] = parts;
      }

      List<ModelPart>[] chains = new List[this.smallRoots.length];
      this.smallChains = chains;
      this.smallSpawn = new KeyframeAnimation[this.smallRoots.length];

      for (int i = 0; i < this.smallRoots.length; i++) {
         Set<String> chainNames = new HashSet<>();
         List<ModelPart> chainParts = new ArrayList<>();
         collectNames(this.smallRoots[i], chainNames);
         collectParts(this.smallRoots[i], chainParts);
         this.smallChains[i] = chainParts;
         this.smallSpawn[i] = filterToBones(WitherStormTentacles5Animation.spawn, chainNames).bake(root);
      }
   }

   private static void collectNames(ModelPart part, Set<String> out) {
      Map<String, ModelPart> kids = ((ModelPartAccessor)(Object)part).getChildren();

      for (Entry<String, ModelPart> e : kids.entrySet()) {
         out.add(e.getKey());
         collectNames(e.getValue(), out);
      }
   }

   private static void collectParts(ModelPart part, List<ModelPart> out) {
      out.add(part);
      Map<String, ModelPart> kids = ((ModelPartAccessor)(Object)part).getChildren();

      for (ModelPart child : kids.values()) {
         collectParts(child, out);
      }
   }

   private static AnimationDefinition filterToBones(AnimationDefinition src, Set<String> keep) {
      Builder b = Builder.withLength(src.lengthInSeconds());
      if (src.looping()) {
         b.looping();
      }

      for (Entry<String, List<AnimationChannel>> e : src.boneAnimations().entrySet()) {
         if (keep.contains(e.getKey())) {
            for (AnimationChannel ch : e.getValue()) {
               b.addAnimation(e.getKey(), ch);
            }
         }
      }

      return b.build();
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshDefinition = new MeshDefinition();
      PartDefinition modelPartData = meshDefinition.getRoot();
      PartDefinition upperBodyPart1 = modelPartData.addOrReplaceChild("upperBodyPart1", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition bone67 = upperBodyPart1.addOrReplaceChild("bone67", CubeListBuilder.create(), PartPose.offset(0.0F, -9.0F, -4.0F));
      PartDefinition bone125 = bone67.addOrReplaceChild("bone125", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 4.0F));
      PartDefinition tentacle6 = bone125.addOrReplaceChild(
         "tentacle6",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.069F, -1.01F, -1.259F, 4.758F, 6.344F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.483F, -1.01F, 3.499F, 1.586F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(1.0F, -37.0F, -10.0F)
      );
      PartDefinition bone76 = tentacle6.addOrReplaceChild(
         "bone76",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.334F, -2.159F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.334F, 1.013F, 4.758F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.2F, 5.0F, 0.9F)
      );
      PartDefinition bone77 = bone76.addOrReplaceChild(
         "bone77",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.278F, -2.859F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, 0.313F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, -4.445F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, 0.7F)
      );
      PartDefinition bone78 = bone77.addOrReplaceChild(
         "bone78",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.222F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.222F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.683F, 0.222F, -2.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, -2.0F)
      );
      PartDefinition bone79 = bone78.addOrReplaceChild(
         "bone79",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, 0.152F, 1.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.569F, 0.152F, -0.273F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.983F, 0.152F, -3.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(2.189F, 0.152F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.0F)
      );
      PartDefinition bone80 = bone79.addOrReplaceChild(
         "bone80",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, 0.082F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, 0.082F, -2.445F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-3.569F, 0.082F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.983F, 0.082F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(1.189F, 0.082F, -0.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(1.0F, 8.0F, -1.0F)
      );
      PartDefinition bone81 = bone80.addOrReplaceChild(
         "bone81",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, 0.012F, -2.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, 0.012F, 1.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, 0.012F, -2.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.3F)
      );
      PartDefinition bone82 = bone81.addOrReplaceChild(
         "bone82",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, -0.058F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, -0.058F, 2.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, -0.058F, -1.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 8.0F, -1.0F)
      );
      PartDefinition bone83 = bone82.addOrReplaceChild(
         "bone83",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, -0.128F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, -0.128F, 2.013F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-0.3F, 8.0F, 0.0F)
      );
      PartDefinition bone84 = bone83.addOrReplaceChild(
         "bone84",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.198F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.0F, 8.0F, -0.3F)
      );
      PartDefinition bone85 = bone84.addOrReplaceChild(
         "bone85",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-0.983F, -0.268F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(-1.0F, 8.0F, 1.0F)
      );
      PartDefinition bone86 = bone85.addOrReplaceChild(
         "bone86",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.338F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone87 = bone86.addOrReplaceChild(
         "bone87",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.603F, -0.408F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, -0.408F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone88 = bone87.addOrReplaceChild(
         "bone88",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.097F, 0.522F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.683F, 0.522F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.7F, 7.0F, 0.0F)
      );
      PartDefinition bone89 = bone88.addOrReplaceChild(
         "bone89",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-0.697F, -0.548F, -1.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.6F, 9.0F, -0.3F)
      );
      PartDefinition bone90 = bone89.addOrReplaceChild(
         "bone90",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-1.697F, 0.382F, -0.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 7.0F, -1.0F)
      );
      PartDefinition tentacle7 = bone125.addOrReplaceChild(
         "tentacle7",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.069F, -1.01F, -1.259F, 4.758F, 6.344F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.483F, -1.01F, 3.499F, 1.586F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-5.0F, -43.0F, 12.0F)
      );
      PartDefinition bone68 = tentacle7.addOrReplaceChild(
         "bone68",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.334F, -2.159F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.334F, 1.013F, 4.758F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.2F, 5.0F, 0.9F)
      );
      PartDefinition bone69 = bone68.addOrReplaceChild(
         "bone69",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.278F, -2.859F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, 0.313F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, -4.445F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, 0.7F)
      );
      PartDefinition bone70 = bone69.addOrReplaceChild(
         "bone70",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.222F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.222F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.683F, 0.222F, -2.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, -2.0F)
      );
      PartDefinition bone71 = bone70.addOrReplaceChild(
         "bone71",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, 0.152F, 1.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.569F, 0.152F, -0.273F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.983F, 0.152F, -3.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(2.189F, 0.152F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.0F)
      );
      PartDefinition bone72 = bone71.addOrReplaceChild(
         "bone72",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, 0.082F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, 0.082F, -2.445F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-3.569F, 0.082F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.983F, 0.082F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(1.189F, 0.082F, -0.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(1.0F, 8.0F, -1.0F)
      );
      PartDefinition bone73 = bone72.addOrReplaceChild(
         "bone73",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, 0.012F, -2.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, 0.012F, 1.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, 0.012F, -2.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.3F)
      );
      PartDefinition bone74 = bone73.addOrReplaceChild(
         "bone74",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, -0.058F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, -0.058F, 2.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, -0.058F, -1.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 8.0F, -1.0F)
      );
      PartDefinition bone75 = bone74.addOrReplaceChild(
         "bone75",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, -0.128F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, -0.128F, 2.013F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-0.3F, 8.0F, 0.0F)
      );
      PartDefinition bone91 = bone75.addOrReplaceChild(
         "bone91",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.198F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.0F, 8.0F, -0.3F)
      );
      PartDefinition bone92 = bone91.addOrReplaceChild(
         "bone92",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-0.983F, -0.268F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(-1.0F, 8.0F, 1.0F)
      );
      PartDefinition bone93 = bone92.addOrReplaceChild(
         "bone93",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.338F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone94 = bone93.addOrReplaceChild(
         "bone94",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.603F, -0.408F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, -0.408F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone95 = bone94.addOrReplaceChild(
         "bone95",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.097F, 0.522F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.683F, 0.522F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.7F, 7.0F, 0.0F)
      );
      PartDefinition bone96 = bone95.addOrReplaceChild(
         "bone96",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-0.697F, -0.548F, -1.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.6F, 9.0F, -0.3F)
      );
      PartDefinition bone97 = bone96.addOrReplaceChild(
         "bone97",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-1.697F, 0.382F, -0.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 7.0F, -1.0F)
      );
      PartDefinition tentacle8 = bone125.addOrReplaceChild(
         "tentacle8",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.069F, -1.01F, -1.259F, 4.758F, 6.344F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.483F, -1.01F, 3.499F, 1.586F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(12.0F, -43.0F, 7.0F)
      );
      PartDefinition bone98 = tentacle8.addOrReplaceChild(
         "bone98",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.334F, -2.159F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.334F, 1.013F, 4.758F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.2F, 5.0F, 0.9F)
      );
      PartDefinition bone99 = bone98.addOrReplaceChild(
         "bone99",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.278F, -2.859F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, 0.313F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, -4.445F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, 0.7F)
      );
      PartDefinition bone100 = bone99.addOrReplaceChild(
         "bone100",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.222F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.222F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.683F, 0.222F, -2.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, -2.0F)
      );
      PartDefinition bone101 = bone100.addOrReplaceChild(
         "bone101",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, 0.152F, 1.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.569F, 0.152F, -0.273F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.983F, 0.152F, -3.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(2.189F, 0.152F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.0F)
      );
      PartDefinition bone102 = bone101.addOrReplaceChild(
         "bone102",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, 0.082F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, 0.082F, -2.445F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-3.569F, 0.082F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.983F, 0.082F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(1.189F, 0.082F, -0.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(1.0F, 8.0F, -1.0F)
      );
      PartDefinition bone103 = bone102.addOrReplaceChild(
         "bone103",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, 0.012F, -2.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, 0.012F, 1.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, 0.012F, -2.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.3F)
      );
      PartDefinition bone104 = bone103.addOrReplaceChild(
         "bone104",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, -0.058F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, -0.058F, 2.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, -0.058F, -1.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 8.0F, -1.0F)
      );
      PartDefinition bone105 = bone104.addOrReplaceChild(
         "bone105",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, -0.128F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, -0.128F, 2.013F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-0.3F, 8.0F, 0.0F)
      );
      PartDefinition bone106 = bone105.addOrReplaceChild(
         "bone106",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.198F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.0F, 8.0F, -0.3F)
      );
      PartDefinition bone107 = bone106.addOrReplaceChild(
         "bone107",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-0.983F, -0.268F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(-1.0F, 8.0F, 1.0F)
      );
      PartDefinition bone108 = bone107.addOrReplaceChild(
         "bone108",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.338F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone109 = bone108.addOrReplaceChild(
         "bone109",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.603F, -0.408F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, -0.408F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone110 = bone109.addOrReplaceChild(
         "bone110",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.097F, 0.522F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.683F, 0.522F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.7F, 7.0F, 0.0F)
      );
      PartDefinition bone111 = bone110.addOrReplaceChild(
         "bone111",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-0.697F, -0.548F, -1.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.6F, 9.0F, -0.3F)
      );
      PartDefinition bone112 = bone111.addOrReplaceChild(
         "bone112",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-1.697F, 0.382F, -0.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 7.0F, -1.0F)
      );
      PartDefinition tentacle9 = bone125.addOrReplaceChild(
         "tentacle9",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.069F, -1.01F, -1.259F, 4.758F, 6.344F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.483F, -1.01F, 3.499F, 1.586F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-11.0F, -43.0F, -6.0F)
      );
      PartDefinition bone113 = tentacle9.addOrReplaceChild(
         "bone113",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.334F, -2.159F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.334F, 1.013F, 4.758F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.2F, 5.0F, 0.9F)
      );
      PartDefinition bone114 = bone113.addOrReplaceChild(
         "bone114",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.278F, -2.859F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, 0.313F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, -4.445F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, 0.7F)
      );
      PartDefinition bone115 = bone114.addOrReplaceChild(
         "bone115",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.222F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.222F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.683F, 0.222F, -2.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, -2.0F)
      );
      PartDefinition bone116 = bone115.addOrReplaceChild(
         "bone116",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, 0.152F, 1.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.569F, 0.152F, -0.273F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.983F, 0.152F, -3.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(2.189F, 0.152F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.0F)
      );
      PartDefinition bone117 = bone116.addOrReplaceChild(
         "bone117",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, 0.082F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, 0.082F, -2.445F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-3.569F, 0.082F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.983F, 0.082F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(1.189F, 0.082F, -0.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(1.0F, 8.0F, -1.0F)
      );
      PartDefinition bone118 = bone117.addOrReplaceChild(
         "bone118",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, 0.012F, -2.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, 0.012F, 1.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, 0.012F, -2.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.3F)
      );
      PartDefinition bone119 = bone118.addOrReplaceChild(
         "bone119",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, -0.058F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, -0.058F, 2.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, -0.058F, -1.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 8.0F, -1.0F)
      );
      PartDefinition bone120 = bone119.addOrReplaceChild(
         "bone120",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, -0.128F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, -0.128F, 2.013F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-0.3F, 8.0F, 0.0F)
      );
      PartDefinition bone121 = bone120.addOrReplaceChild(
         "bone121",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.198F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.0F, 8.0F, -0.3F)
      );
      PartDefinition bone122 = bone121.addOrReplaceChild(
         "bone122",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-0.983F, -0.268F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(-1.0F, 8.0F, 1.0F)
      );
      PartDefinition bone123 = bone122.addOrReplaceChild(
         "bone123",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.338F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone124 = bone123.addOrReplaceChild(
         "bone124",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.603F, -0.408F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, -0.408F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone126 = bone124.addOrReplaceChild(
         "bone126",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.097F, 0.522F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.683F, 0.522F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.7F, 7.0F, 0.0F)
      );
      PartDefinition bone127 = bone126.addOrReplaceChild(
         "bone127",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-0.697F, -0.548F, -1.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.6F, 9.0F, -0.3F)
      );
      PartDefinition bone128 = bone127.addOrReplaceChild(
         "bone128",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-1.697F, 0.382F, -0.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 7.0F, -1.0F)
      );
      PartDefinition tentacle10 = bone125.addOrReplaceChild(
         "tentacle10",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.069F, -1.01F, -1.259F, 4.758F, 6.344F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.483F, -1.01F, 3.499F, 1.586F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-11.0F, -43.0F, -6.0F)
      );
      PartDefinition bone129 = tentacle10.addOrReplaceChild(
         "bone129",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.334F, -2.159F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.334F, 1.013F, 4.758F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.2F, 5.0F, 0.9F)
      );
      PartDefinition bone130 = bone129.addOrReplaceChild(
         "bone130",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.278F, -2.859F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, 0.313F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, -4.445F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, 0.7F)
      );
      PartDefinition bone131 = bone130.addOrReplaceChild(
         "bone131",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.222F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.222F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.683F, 0.222F, -2.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, -2.0F)
      );
      PartDefinition bone132 = bone131.addOrReplaceChild(
         "bone132",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, 0.152F, 1.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.569F, 0.152F, -0.273F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.983F, 0.152F, -3.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(2.189F, 0.152F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.0F)
      );
      PartDefinition bone133 = bone132.addOrReplaceChild(
         "bone133",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, 0.082F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, 0.082F, -2.445F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-3.569F, 0.082F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.983F, 0.082F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(1.189F, 0.082F, -0.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(1.0F, 8.0F, -1.0F)
      );
      PartDefinition bone134 = bone133.addOrReplaceChild(
         "bone134",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, 0.012F, -2.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, 0.012F, 1.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, 0.012F, -2.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.3F)
      );
      PartDefinition bone135 = bone134.addOrReplaceChild(
         "bone135",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, -0.058F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, -0.058F, 2.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, -0.058F, -1.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 8.0F, -1.0F)
      );
      PartDefinition bone136 = bone135.addOrReplaceChild(
         "bone136",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, -0.128F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, -0.128F, 2.013F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-0.3F, 8.0F, 0.0F)
      );
      PartDefinition bone137 = bone136.addOrReplaceChild(
         "bone137",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.198F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.0F, 8.0F, -0.3F)
      );
      PartDefinition bone138 = bone137.addOrReplaceChild(
         "bone138",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-0.983F, -0.268F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(-1.0F, 8.0F, 1.0F)
      );
      PartDefinition bone139 = bone138.addOrReplaceChild(
         "bone139",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.338F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone140 = bone139.addOrReplaceChild(
         "bone140",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.603F, -0.408F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, -0.408F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone141 = bone140.addOrReplaceChild(
         "bone141",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.097F, 0.522F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.683F, 0.522F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.7F, 7.0F, 0.0F)
      );
      PartDefinition bone142 = bone141.addOrReplaceChild(
         "bone142",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-0.697F, -0.548F, -1.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.6F, 9.0F, -0.3F)
      );
      PartDefinition bone143 = bone142.addOrReplaceChild(
         "bone143",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-1.697F, 0.382F, -0.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 7.0F, -1.0F)
      );
      PartDefinition bone144 = bone125.addOrReplaceChild(
         "bone144",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.334F, -2.159F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.334F, 1.013F, 4.758F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(16.2F, -38.0F, -10.1F)
      );
      PartDefinition bone147 = bone144.addOrReplaceChild(
         "bone147",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.269F, 0.278F, -2.859F, 4.758F, 6.344F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, 0.313F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.278F, -4.445F, 3.172F, 6.344F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, 0.7F)
      );
      PartDefinition bone148 = bone147.addOrReplaceChild(
         "bone148",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.683F, 0.222F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.269F, 0.222F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.683F, 0.222F, -2.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 6.4F, -2.0F)
      );
      PartDefinition bone149 = bone148.addOrReplaceChild(
         "bone149",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, 0.152F, 1.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.569F, 0.152F, -0.273F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-0.983F, 0.152F, -3.445F, 3.172F, 7.93F, 4.758F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(2.189F, 0.152F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.0F)
      );
      PartDefinition bone150 = bone149.addOrReplaceChild(
         "bone150",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, 0.082F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, 0.082F, -2.445F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-3.569F, 0.082F, 0.727F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.983F, 0.082F, 2.313F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(1.189F, 0.082F, -0.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(1.0F, 8.0F, -1.0F)
      );
      PartDefinition bone151 = bone150.addOrReplaceChild(
         "bone151",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, 0.012F, -2.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, 0.012F, 1.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, 0.012F, -2.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.3F, 8.0F, 1.3F)
      );
      PartDefinition bone152 = bone151.addOrReplaceChild(
         "bone152",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-2.283F, -0.058F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-2.283F, -0.058F, 2.013F, 3.172F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(0.889F, -0.058F, -1.159F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 8.0F, -1.0F)
      );
      PartDefinition bone153 = bone152.addOrReplaceChild(
         "bone153",
         CubeListBuilder.create()
            .texOffs(72, 0)
            .mirror()
            .addBox(-1.983F, -0.128F, -1.159F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.397F, -0.128F, 2.013F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-0.3F, 8.0F, 0.0F)
      );
      PartDefinition bone154 = bone153.addOrReplaceChild(
         "bone154",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.198F, -0.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.0F, 8.0F, -0.3F)
      );
      PartDefinition bone155 = bone154.addOrReplaceChild(
         "bone155",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-0.983F, -0.268F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(-1.0F, 8.0F, 1.0F)
      );
      PartDefinition bone156 = bone155.addOrReplaceChild(
         "bone156",
         CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.983F, -0.338F, -1.859F, 3.172F, 7.93F, 3.172F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone157 = bone156.addOrReplaceChild(
         "bone157",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(0.603F, -0.408F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.983F, -0.408F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-1.0F, 8.0F, 0.0F)
      );
      PartDefinition bone158 = bone157.addOrReplaceChild(
         "bone158",
         CubeListBuilder.create()
            .texOffs(118, 85)
            .mirror()
            .addBox(-0.097F, 0.522F, -1.859F, 1.586F, 7.93F, 3.172F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(118, 85)
            .mirror()
            .addBox(-1.683F, 0.522F, -1.859F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.7F, 7.0F, 0.0F)
      );
      PartDefinition bone159 = bone158.addOrReplaceChild(
         "bone159",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-0.697F, -0.548F, -1.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.6F, 9.0F, -0.3F)
      );
      PartDefinition bone160 = bone159.addOrReplaceChild(
         "bone160",
         CubeListBuilder.create().texOffs(118, 85).mirror().addBox(-1.697F, 0.382F, -0.559F, 1.586F, 7.93F, 1.586F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(1.0F, 7.0F, -1.0F)
      );
      PartDefinition bone191 = upperBodyPart1.addOrReplaceChild("bone191", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition bone176 = bone191.addOrReplaceChild(
         "bone176",
         CubeListBuilder.create()
            .texOffs(78, 60)
            .addBox(-19.86F, 1.2F, 6.1F, 22.08F, 2.76F, 1.38F, new CubeDeformation(0.0F))
            .texOffs(48, 1)
            .addBox(-19.86F, -5.7F, -10.46F, 22.08F, 9.66F, 16.56F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(37.0F, -56.0F, -8.0F, 0.0F, 3.1416F, 0.0F)
      );
      PartDefinition bone177 = bone176.addOrReplaceChild(
         "bone177",
         CubeListBuilder.create().texOffs(52, 18).addBox(-21.56F, -9.22F, -6.46F, 20.7F, 12.42F, 11.04F, new CubeDeformation(0.0F)),
         PartPose.offset(-19.0F, -2.0F, -4.0F)
      );
      PartDefinition bone178 = bone177.addOrReplaceChild(
         "bone178",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-22.02F, -5.6F, -4.7F, 23.46F, 11.04F, 8.28F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.02F, -8.36F, -1.94F, 23.46F, 16.56F, 5.52F, new CubeDeformation(0.0F)),
         PartPose.offset(-23.0F, -5.0F, 1.0F)
      );
      PartDefinition bone179 = bone178.addOrReplaceChild(
         "bone179",
         CubeListBuilder.create()
            .texOffs(34, 35)
            .addBox(-27.62F, -4.6F, -2.7F, 27.6F, 5.52F, 11.04F, new CubeDeformation(0.0F))
            .texOffs(34, 35)
            .addBox(-27.62F, -1.84F, 0.06F, 27.6F, 5.52F, 11.04F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-27.62F, -7.36F, 0.06F, 27.6F, 11.04F, 5.52F, new CubeDeformation(0.0F)),
         PartPose.offset(-22.0F, -1.0F, -2.0F)
      );
      PartDefinition bone180 = bone179.addOrReplaceChild(
         "bone180",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-25.84F, -2.84F, -3.18F, 26.22F, 8.28F, 8.28F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-25.84F, -0.08F, -5.94F, 26.22F, 5.52F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-28.0F, 1.0F, 6.0F)
      );
      PartDefinition bone181 = bone180.addOrReplaceChild(
         "bone181",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-22.92F, -3.84F, -3.94F, 22.08F, 8.28F, 8.28F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.92F, -6.6F, -1.18F, 22.08F, 11.04F, 5.52F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.92F, -6.6F, -6.7F, 22.08F, 8.28F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-25.0F, 1.0F, -2.0F)
      );
      PartDefinition bone182 = bone181.addOrReplaceChild(
         "bone182",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-23.38F, -0.08F, -7.7F, 23.46F, 2.76F, 11.04F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-23.38F, -2.84F, -2.18F, 23.46F, 2.76F, 2.76F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-23.38F, 2.68F, -4.94F, 23.46F, 2.76F, 8.28F, new CubeDeformation(0.0F)),
         PartPose.offset(-23.0F, -1.0F, 1.0F)
      );
      PartDefinition bone183 = bone182.addOrReplaceChild(
         "bone183",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-22.84F, -1.08F, -3.94F, 23.46F, 5.52F, 8.28F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.84F, -3.84F, -1.18F, 23.46F, 2.76F, 2.76F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.84F, 1.68F, -1.18F, 23.46F, 5.52F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-24.0F, 1.0F, -1.0F)
      );
      PartDefinition bone184 = bone183.addOrReplaceChild(
         "bone184",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-24.3F, -1.32F, -3.18F, 23.46F, 2.76F, 5.52F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-24.3F, -4.08F, -3.18F, 23.46F, 2.76F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-22.0F, 3.0F, 2.0F)
      );
      PartDefinition bone185 = bone184.addOrReplaceChild(
         "bone185",
         CubeListBuilder.create().texOffs(52, 18).addBox(-25.14F, -2.32F, -3.94F, 24.84F, 5.52F, 5.52F, new CubeDeformation(0.0F)),
         PartPose.offset(-24.0F, 1.0F, -2.0F)
      );
      PartDefinition bone186 = bone185.addOrReplaceChild(
         "bone186",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-14.7F, -3.32F, -3.56F, 16.56F, 4.14F, 4.14F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-14.7F, -1.94F, -3.56F, 16.56F, 4.14F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-27.0F, 1.0F, 1.0F)
      );
      PartDefinition bone187 = bone186.addOrReplaceChild(
         "bone187",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-16.26F, -2.32F, -3.56F, 16.56F, 1.38F, 4.14F, new CubeDeformation(0.0F))
            .texOffs(78, 60)
            .addBox(-16.26F, -0.94F, -3.56F, 16.56F, 1.38F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-15.0F, -1.0F, 0.0F)
      );
      PartDefinition bone188 = bone187.addOrReplaceChild(
         "bone188",
         CubeListBuilder.create()
            .texOffs(78, 60)
            .addBox(-14.44F, -3.94F, -3.56F, 15.18F, 1.38F, 2.76F, new CubeDeformation(0.0F))
            .texOffs(78, 60)
            .addBox(-14.44F, -2.56F, -2.18F, 15.18F, 1.38F, 1.38F, new CubeDeformation(0.0F)),
         PartPose.offset(-17.0F, 3.0F, 0.0F)
      );
      PartDefinition bone189 = bone188.addOrReplaceChild(
         "bone189",
         CubeListBuilder.create()
            .texOffs(78, 60)
            .addBox(-14.62F, -0.56F, -2.56F, 15.18F, 1.38F, 2.76F, new CubeDeformation(0.0F))
            .texOffs(78, 60)
            .addBox(-14.62F, -1.94F, -1.18F, 15.18F, 1.38F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-15.0F, -2.0F, -1.0F)
      );
      PartDefinition bone190 = bone189.addOrReplaceChild(
         "bone190",
         CubeListBuilder.create().texOffs(78, 60).addBox(-14.8F, -0.94F, -0.18F, 15.18F, 1.38F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-15.0F, -1.0F, -1.0F)
      );
      PartDefinition bone161 = bone191.addOrReplaceChild(
         "bone161",
         CubeListBuilder.create()
            .texOffs(78, 60)
            .addBox(-19.86F, 1.2F, 6.1F, 22.08F, 2.76F, 1.38F, new CubeDeformation(0.0F))
            .texOffs(48, 1)
            .addBox(-19.86F, -5.7F, -10.46F, 22.08F, 9.66F, 16.56F, new CubeDeformation(0.0F)),
         PartPose.offset(-32.0F, -56.0F, -8.0F)
      );
      PartDefinition bone162 = bone161.addOrReplaceChild(
         "bone162",
         CubeListBuilder.create().texOffs(52, 18).addBox(-21.56F, -9.22F, -6.46F, 20.7F, 12.42F, 11.04F, new CubeDeformation(0.0F)),
         PartPose.offset(-19.0F, -2.0F, -4.0F)
      );
      PartDefinition bone163 = bone162.addOrReplaceChild(
         "bone163",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-22.02F, -5.6F, -4.7F, 23.46F, 11.04F, 8.28F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.02F, -8.36F, -1.94F, 23.46F, 16.56F, 5.52F, new CubeDeformation(0.0F)),
         PartPose.offset(-23.0F, -5.0F, 1.0F)
      );
      PartDefinition bone164 = bone163.addOrReplaceChild(
         "bone164",
         CubeListBuilder.create()
            .texOffs(34, 35)
            .addBox(-27.62F, -4.6F, -2.7F, 27.6F, 5.52F, 11.04F, new CubeDeformation(0.0F))
            .texOffs(34, 35)
            .addBox(-27.62F, -1.84F, 0.06F, 27.6F, 5.52F, 11.04F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-27.62F, -7.36F, 0.06F, 27.6F, 11.04F, 5.52F, new CubeDeformation(0.0F)),
         PartPose.offset(-22.0F, -1.0F, -2.0F)
      );
      PartDefinition bone165 = bone164.addOrReplaceChild(
         "bone165",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-25.84F, -2.84F, -3.18F, 26.22F, 8.28F, 8.28F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-25.84F, -0.08F, -5.94F, 26.22F, 5.52F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-28.0F, 1.0F, 6.0F)
      );
      PartDefinition bone166 = bone165.addOrReplaceChild(
         "bone166",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-22.92F, -3.84F, -3.94F, 22.08F, 8.28F, 8.28F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.92F, -6.6F, -1.18F, 22.08F, 11.04F, 5.52F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.92F, -6.6F, -6.7F, 22.08F, 8.28F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-25.0F, 1.0F, -2.0F)
      );
      PartDefinition bone167 = bone166.addOrReplaceChild(
         "bone167",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-23.38F, -0.08F, -7.7F, 23.46F, 2.76F, 11.04F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-23.38F, -2.84F, -2.18F, 23.46F, 2.76F, 2.76F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-23.38F, 2.68F, -4.94F, 23.46F, 2.76F, 8.28F, new CubeDeformation(0.0F)),
         PartPose.offset(-23.0F, -1.0F, 1.0F)
      );
      PartDefinition bone168 = bone167.addOrReplaceChild(
         "bone168",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-22.84F, -1.08F, -3.94F, 23.46F, 5.52F, 8.28F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.84F, -3.84F, -1.18F, 23.46F, 2.76F, 2.76F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-22.84F, 1.68F, -1.18F, 23.46F, 5.52F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-24.0F, 1.0F, -1.0F)
      );
      PartDefinition bone169 = bone168.addOrReplaceChild(
         "bone169",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-24.3F, -1.32F, -3.18F, 23.46F, 2.76F, 5.52F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-24.3F, -4.08F, -3.18F, 23.46F, 2.76F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-22.0F, 3.0F, 2.0F)
      );
      PartDefinition bone170 = bone169.addOrReplaceChild(
         "bone170",
         CubeListBuilder.create().texOffs(52, 18).addBox(-25.14F, -2.32F, -3.94F, 24.84F, 5.52F, 5.52F, new CubeDeformation(0.0F)),
         PartPose.offset(-24.0F, 1.0F, -2.0F)
      );
      PartDefinition bone171 = bone170.addOrReplaceChild(
         "bone171",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-14.7F, -3.32F, -3.56F, 16.56F, 4.14F, 4.14F, new CubeDeformation(0.0F))
            .texOffs(52, 18)
            .addBox(-14.7F, -1.94F, -3.56F, 16.56F, 4.14F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-27.0F, 1.0F, 1.0F)
      );
      PartDefinition bone172 = bone171.addOrReplaceChild(
         "bone172",
         CubeListBuilder.create()
            .texOffs(52, 18)
            .addBox(-16.26F, -2.32F, -3.56F, 16.56F, 1.38F, 4.14F, new CubeDeformation(0.0F))
            .texOffs(78, 60)
            .addBox(-16.26F, -0.94F, -3.56F, 16.56F, 1.38F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-15.0F, -1.0F, 0.0F)
      );
      PartDefinition bone173 = bone172.addOrReplaceChild(
         "bone173",
         CubeListBuilder.create()
            .texOffs(78, 60)
            .addBox(-14.44F, -3.94F, -3.56F, 15.18F, 1.38F, 2.76F, new CubeDeformation(0.0F))
            .texOffs(78, 60)
            .addBox(-14.44F, -2.56F, -2.18F, 15.18F, 1.38F, 1.38F, new CubeDeformation(0.0F)),
         PartPose.offset(-17.0F, 3.0F, 0.0F)
      );
      PartDefinition bone174 = bone173.addOrReplaceChild(
         "bone174",
         CubeListBuilder.create()
            .texOffs(78, 60)
            .addBox(-14.62F, -0.56F, -2.56F, 15.18F, 1.38F, 2.76F, new CubeDeformation(0.0F))
            .texOffs(78, 60)
            .addBox(-14.62F, -1.94F, -1.18F, 15.18F, 1.38F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-15.0F, -2.0F, -1.0F)
      );
      PartDefinition bone175 = bone174.addOrReplaceChild(
         "bone175",
         CubeListBuilder.create().texOffs(78, 60).addBox(-14.8F, -0.94F, -0.18F, 15.18F, 1.38F, 2.76F, new CubeDeformation(0.0F)),
         PartPose.offset(-15.0F, -1.0F, -1.0F)
      );
      return LayerDefinition.create(meshDefinition, 512, 512);
   }

   public void setupAnim(WitherStormRenderState state) {
      PhaseStormAnim.setPhase((float)state.phase);
      float droop = CollapseAnim.droop(state.collapseTicks);
      float live = 1.0F - droop;
      this.root.getAllParts().forEach(ModelPart::resetPose);
      float elapsed = state.phase5ElapsedTicks < 0.0F ? 0.0F : state.phase5ElapsedTicks / 20.0F;
      float writhe = state.phase >= 5.8 ? (float)DabyWSClientConfig.lateGrowthWrithe : 1.0F;
      if (state.underSiege) {
         float p = Mth.clamp(state.siegeProgress, 0.0F, 1.0F);
         float full = (float)ClientConfigCache.cfg.endermanSiegeTentacleSpeed / 100.0F;
         writhe *= 1.0F + (full - 1.0F) * (0.38F + 0.62F * p);
      }

      float idleTime = state.idleTimeTicks * writhe;
      Integer[] order = new Integer[]{0, 1, 2, 3, 4, 5};
      Random rng = new Random((long)state.stormId * 2654435761L);

      for (int i = order.length - 1; i > 0; i--) {
         int j = rng.nextInt(i + 1);
         Integer t = order[i];
         order[i] = order[j];
         order[j] = t;
      }

      for (int slot = 0; slot < this.smallRoots.length; slot++) {
         int idx = order[slot];
         float revealAt = (float)slot * 1.6F;
         ModelPart rootPart = this.smallRoots[idx];
         if (!this.bigTentaclesOnly && !(elapsed < revealAt)) {
            rootPart.visible = true;
            rootPart.xScale = 1.5F;
            rootPart.yScale = 1.5F;
            rootPart.zScale = 1.5F;
            rootPart.y -= 4.0F;
            float sinceReveal = (elapsed - revealAt) * 20.0F;
            float fade = 12.0F;
            if (sinceReveal < 100.0F) {
               this.smallSpawn[idx].apply((long)(sinceReveal * 50.0F), 1.0F);
            } else if (sinceReveal < 100.0F + fade) {
               float p = (sinceReveal - 100.0F) / fade;
               this.smallSpawn[idx].apply(5000L, 1.0F - p);
               smallIdle(this.smallChains[idx], idleTime, idx, p * live);
            } else {
               smallIdle(this.smallChains[idx], idleTime, idx, live);
            }
         } else {
            rootPart.visible = false;
         }
      }

      if (state.preview != null && state.phase >= 5.1) {
         for (int b = 0; b < this.bigRoots.length; b++) {
            if (this.singleBigTentacle && b > 0) {
               this.bigRoots[b].visible = false;
            } else {
               this.bigRoots[b].visible = true;
               this.bigRoots[b].xScale = 1.0F;
               this.bigRoots[b].yScale = 1.0F;
               this.bigRoots[b].zScale = 1.0F;
               bigIdleMath(this.bigChains[b], idleTime, b, live);
            }
         }
      } else if (state.phase < 5.1) {
         BIG_UNCURL_START.remove(state.stormId);

         for (ModelPart big : this.bigRoots) {
            big.visible = false;
         }
      } else if (this.staticPose) {
         for (int bx = 0; bx < this.bigRoots.length; bx++) {
            if (this.singleBigTentacle && bx > 0) {
               this.bigRoots[bx].visible = false;
            } else {
               this.bigRoots[bx].visible = true;
               this.bigRoots[bx].xScale = 1.0F;
               this.bigRoots[bx].yScale = 1.0F;
               this.bigRoots[bx].zScale = 1.0F;
               this.bigRoots[bx].xRot = this.bigRoots[bx].xRot + this.staticPoseXRot;
               this.bigRoots[bx].zRot = this.bigRoots[bx].zRot + this.staticPoseZRot;
               if (this.staticCurl != 0.0F) {
                  List<ModelPart> chain = this.bigChains[bx];

                  for (int i = 1; i < chain.size(); i++) {
                     float w = (float)i / (float)chain.size();
                     ModelPart var10000 = chain.get(i);
                     var10000.zRot = var10000.zRot + this.staticCurl * w;
                  }
               }
            }
         }
      } else {
         float start = BIG_UNCURL_START.computeIfAbsent(state.stormId, k -> state.idleTimeTicks);
         float sinceReveal = state.idleTimeTicks - start;
         float u = Mth.clamp(sinceReveal / 70.0F, 0.0F, 1.0F);
         float e = u * u * (3.0F - 2.0F * u);

         for (int bxx = 0; bxx < this.bigRoots.length; bxx++) {
            if (this.singleBigTentacle && bxx > 0) {
               this.bigRoots[bxx].visible = false;
            } else {
               this.bigRoots[bxx].visible = true;
               this.bigRoots[bxx].x = this.bigRoots[bxx].x - Math.signum(this.bigRoots[bxx].x) * 43.0F;
               this.bigRoots[bxx].y -= -1.0F;
               float t = sinceReveal - (float)bxx * 18.0F;
               float scaleP = Mth.clamp(t / 12.0F, 0.02F, 1.0F);
               float s = Mth.clamp(scaleP * scaleP * (3.0F - 2.0F * scaleP), 0.02F, 1.0F);
               float bs = s * 1.38F;
               this.bigRoots[bxx].xScale = bs;
               this.bigRoots[bxx].yScale = bs;
               this.bigRoots[bxx].zScale = bs;
               sideUncurl(this.bigChains[bxx], (1.0F - e) * 1.15F);
            }
         }

         for (int bxxx = 0; bxxx < this.bigChains.length; bxxx++) {
            bigIdleMath(this.bigChains[bxxx], idleTime, bxxx, e * live);
            if (!this.ragdoll(this.bigChains[bxxx], state, 64 + bxxx, droop)) {
               limp(this.bigChains[bxxx], droop, idleTime, bxxx + 64, state.groundBias[(bxxx + 64) % state.groundBias.length], true);
            }
         }
      }

      for (int idx = 0; idx < this.smallChains.length; idx++) {
         if (!this.ragdoll(this.smallChains[idx], state, idx, droop)) {
            limp(this.smallChains[idx], droop, idleTime, idx, state.groundBias[idx % state.groundBias.length]);
         }
      }

      if (!this.staticPose) {
         SnatchGrab.reachUnderStorm(this.root, state);
      }
   }

   public static void bigIdleMath(List<ModelPart> chain, float timeTicks, int idx, float weight) {
      int n = chain.size();
      if (n != 0) {
         float speed = (float)DabyWSClientConfig.tentacleIdleSpeed * PhaseStormAnim.speed();
         float travel = (float)DabyWSClientConfig.tentacleWaveTravel * PhaseStormAnim.speed();
         float depth = (float)DabyWSClientConfig.bigTentacleCurlDepth * PhaseStormAnim.depth();
         float breath = (float)DabyWSClientConfig.bigTentacleHangBreath * PhaseStormAnim.breath();
         float t = timeTicks * 0.026F * speed + (float)idx * 2.3F;
         float side = 1.0F;
         float heave = (Mth.sin((double)(t * 0.61F + (float)idx * 1.7F)) * 0.1F + Mth.sin((double)(t * 0.27F + 2.4F)) * 0.05F) * breath;
         float lift = Mth.sin((double)(t * 0.43F + (float)idx * 2.9F)) * 0.17F * breath;
         float swing = Mth.sin((double)(t * 0.35F + (float)idx * 1.1F)) * 0.2F * breath;
         float prevDrop = 0.0F;
         float prevSide = 0.0F;

         for (int i = 0; i < n; i++) {
            ModelPart bone = chain.get(i);
            float along = n > 1 ? (float)i / (float)(n - 1) : 0.0F;
            float cumDrop = hangDrop(along, heave, lift);
            float cumSide = hangSide(along) * (side + swing);
            float phase = t - (float)i * 0.42F * travel;
            float waveDown = Mth.sin((double)phase);
            float waveSide = Mth.sin((double)(phase * 0.63F + 1.9F));
            float amp = (0.07F + 0.62F * along * along) * weight * depth;
            bone.zRot -= (cumDrop - prevDrop) * weight;
            bone.zRot += waveDown * amp;
            bone.yRot += (cumSide - prevSide) * weight + waveSide * amp * 0.8F;
            prevDrop = cumDrop;
            prevSide = cumSide;
         }
      }
   }

   private static float hangDrop(float along, float heave, float lift) {
      return BigTentacleShape.hangDrop(along, heave, lift);
   }

   private static float hangSide(float along) {
      float u = Mth.clamp((along - 0.080000006F) / 0.92F, 0.0F, 1.0F);
      return 0.85F * smoothstep(u) * (float)DabyWSClientConfig.bigTentacleSideSweep;
   }

   private static float smoothstep(float u) {
      float x = Mth.clamp(u, 0.0F, 1.0F);
      return x * x * (3.0F - 2.0F * x);
   }

   private static void sideUncurl(List<ModelPart> chain, float strength) {
      if (!(strength <= 1.0E-4F)) {
         for (int i = 0; i < chain.size(); i++) {
            float along = (float)i / (float)chain.size();
            chain.get(i).yRot += strength * (0.35F + 0.65F * along);
         }
      }
   }

   public static void smallIdle(List<ModelPart> chain, float timeTicks, int tentacleIdx, float weight) {
      float speed = (float)DabyWSClientConfig.tentacleIdleSpeed * PhaseStormAnim.speed();
      float travel = (float)DabyWSClientConfig.tentacleWaveTravel * PhaseStormAnim.speed();
      float depth = (float)DabyWSClientConfig.tentacleCurlDepth * PhaseStormAnim.depth();
      float cross = (float)DabyWSClientConfig.tentacleCrossAxis;
      float t = timeTicks * 0.022F * speed + (float)tentacleIdx * 2.3F;

      for (int i = 0; i < chain.size(); i++) {
         ModelPart bone = chain.get(i);
         float along = (float)i / (float)chain.size();
         float phase = t - (float)i * 0.28F * travel;
         float amp = (0.14F + 0.3F * along) * weight * depth;
         bone.xRot = bone.xRot + Mth.sin((double)phase) * amp;
         bone.zRot = bone.zRot + Mth.sin((double)(phase * 0.63F + 1.9F)) * amp * cross;
      }
   }

   private static Matrix4f toWorld(WitherStormRenderState state) {
      Matrix4f m = new Matrix4f();
      m.translate((float)state.x, (float)state.y, (float)state.z);
      m.rotate(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
      float topple = CollapseAnim.bodyPitch(state.collapseTicks);
      if (topple != 0.0F) {
         float flat = CollapseAnim.down(state.collapseTicks);
         float pivot = 17.0F;
         m.translate(0.0F, -18.5F * flat, 0.0F);
         m.translate(0.0F, pivot, 0.0F);
         m.rotate(Axis.XP.rotationDegrees(-topple));
         m.translate(0.0F, -pivot, 0.0F);
      }

      m.rotate(Axis.ZN.rotationDegrees(state.bodyRoll));
      m.translate(0.0F, 6.0F, 1.25F);
      m.scale(-5.0F, -5.0F, 5.0F);
      return m;
   }

   private boolean ragdoll(List<ModelPart> chain, WitherStormRenderState state, int chainIndex, float droop) {
      if (!(droop <= 0.001F) && !chain.isEmpty()) {
         Quaternionf ancestorQ = new Quaternionf();
         Matrix4f parent = TentacleRagdoll.parentTransform(this.root, chain.get(0), ancestorQ);
         if (parent == null) {
            return false;
         } else {
            Matrix4f full = toWorld(state).mul(parent);
            long key = (long)state.stormId << 8 | (long)(chainIndex & 0xFF);
            TentacleRagdoll.settle(chain, key, full, ancestorQ, droop);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void limp(List<ModelPart> chain, float amount, float timeTicks, int idx) {
      limp(chain, amount, timeTicks, idx, 0.0F);
   }

   public static void limp(List<ModelPart> chain, float amount, float timeTicks, int idx, float groundBias) {
      limp(chain, amount, timeTicks, idx, groundBias, false);
   }

   public static void limp(List<ModelPart> chain, float amount, float timeTicks, int idx, float groundBias, boolean lengthwiseX) {
      if (!(amount <= 0.001F)) {
         float lean = 0.24F + 0.1F * Mth.sin((double)((float)idx * 2.399963F));
         float reach = lean * (1.0F + groundBias * 0.55F);
         int n = chain.size();

         for (int i = 0; i < n; i++) {
            ModelPart bone = chain.get(i);
            float along = (float)i / (float)Math.max(1, n - 1);
            float weight = (float)Math.exp((double)(-along * 4.5F));
            float drop = (reach * weight + 0.012F) * amount;
            float settle = 0.055F * (1.0F - weight) * (1.0F + groundBias * 0.8F) * amount;
            drop += settle;
            float wobble = Mth.sin((double)((float)idx * 1.31F + along * 2.1F)) * 0.05F * amount;
            if (lengthwiseX) {
               bone.zRot -= drop;
               bone.yRot += wobble;
            } else {
               bone.xRot += drop;
               bone.zRot += wobble;
            }
         }
      }
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.upperBodyPart1.render(poseStack, buffer, packedLight, packedOverlay);
   }
}
