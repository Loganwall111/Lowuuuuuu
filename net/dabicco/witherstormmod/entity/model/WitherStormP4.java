package net.dabicco.witherstormmod.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.client.SnatchGrab;
import net.dabicco.witherstormmod.client.TentaclePhysics;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.animation.WitherStormP4Anim;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class WitherStormP4 extends EntityModel<WitherStormRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("modid", "dabicco_witherstormdestroyer"), "main"
   );
   private final ModelPart root;
   private final KeyframeAnimation spawnAnimation;
   private final KeyframeAnimation idleAnimation;
   private final ModelPart bone;
   private final ModelPart bone2;
   private final ModelPart bone3;
   private final ModelPart bone4;
   private final ModelPart bone63;
   private final ModelPart bone64;
   private final ModelPart bone122;
   private final ModelPart bone181;
   private final ModelPart bone240;
   private final ModelPart bone241;
   private final ModelPart bone242;
   private final ModelPart bone243;
   private final ModelPart bone244;
   private final ModelPart bone245;
   private final ModelPart bone246;
   private final ModelPart bone247;
   private final ModelPart bone248;
   private final ModelPart bone249;
   private final ModelPart bone250;
   private final ModelPart bone251;
   private final ModelPart bone252;
   private final ModelPart bone253;
   private final ModelPart bone254;
   private final ModelPart bone255;
   private final ModelPart bone256;
   private final ModelPart bone257;
   private final ModelPart bone258;
   private final ModelPart bone259;
   private final ModelPart bone260;
   private final ModelPart bone261;
   private final ModelPart bone262;
   private final ModelPart bone263;
   private final ModelPart bone264;
   private final ModelPart bone265;
   private final ModelPart bone266;
   private final ModelPart bone267;
   private final ModelPart bone268;
   private final ModelPart bone269;
   private final ModelPart bone270;
   private final ModelPart bone271;
   private final ModelPart bone272;
   private final ModelPart bone273;
   private final ModelPart bone274;
   private final ModelPart bone275;
   private final ModelPart bone276;
   private final ModelPart bone277;
   private final ModelPart bone278;
   private final ModelPart bone279;
   private final ModelPart bone280;
   private final ModelPart bone281;
   private final ModelPart bone282;
   private final ModelPart bone283;
   private final ModelPart bone284;
   private final ModelPart bone285;
   private final ModelPart bone286;
   private final ModelPart bone287;
   private final ModelPart bone288;
   private final ModelPart bone289;
   private final ModelPart bone290;
   private final ModelPart bone291;
   private final ModelPart bone292;
   private final ModelPart bone293;
   private final ModelPart bone294;
   private final ModelPart bone295;
   private final ModelPart bone296;
   private final ModelPart bone297;
   private final ModelPart bone298;
   private final ModelPart bone5;
   private final ModelPart bone6;
   private final ModelPart bone7;
   private final ModelPart bone8;
   private final ModelPart bone9;
   private final ModelPart bone10;
   private final ModelPart bone11;
   private final ModelPart bone12;
   private final ModelPart bone13;
   private final ModelPart bone14;
   private final ModelPart bone15;
   private final ModelPart bone16;
   private final ModelPart bone17;
   private final ModelPart bone18;
   private final ModelPart bone19;
   private final ModelPart bone20;
   private final ModelPart bone21;
   private final ModelPart bone22;
   private final ModelPart bone23;
   private final ModelPart bone24;
   private final ModelPart bone25;
   private final ModelPart bone26;
   private final ModelPart bone27;
   private final ModelPart bone28;
   private final ModelPart bone29;
   private final ModelPart bone30;
   private final ModelPart bone31;
   private final ModelPart bone32;
   private final ModelPart bone33;
   private final ModelPart bone34;
   private final ModelPart bone35;
   private final ModelPart bone36;
   private final ModelPart bone37;
   private final ModelPart bone38;
   private final ModelPart bone39;
   private final ModelPart bone40;
   private final ModelPart bone41;
   private final ModelPart bone42;
   private final ModelPart bone43;
   private final ModelPart bone44;
   private final ModelPart bone45;
   private final ModelPart bone46;
   private final ModelPart bone47;
   private final ModelPart bone48;
   private final ModelPart bone49;
   private final ModelPart bone50;
   private final ModelPart bone51;
   private final ModelPart bone52;
   private final ModelPart bone53;
   private final ModelPart bone54;
   private final ModelPart bone55;
   private final ModelPart bone56;
   private final ModelPart bone57;
   private final ModelPart bone58;
   private final ModelPart bone59;
   private final ModelPart bone60;
   private final ModelPart bone61;
   private final ModelPart bone62;
   private final ModelPart bone65;
   private final ModelPart bone66;
   private final ModelPart bone67;
   private final ModelPart bone68;
   private final ModelPart bone69;
   private final ModelPart bone70;
   private final ModelPart bone71;
   private final ModelPart bone72;
   private final ModelPart bone73;
   private final ModelPart bone74;
   private final ModelPart bone75;
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
   private final ModelPart bone91;
   private final ModelPart bone92;
   private final ModelPart bone93;
   private final ModelPart bone94;
   private final ModelPart bone95;
   private final ModelPart bone96;
   private final ModelPart bone97;
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
   private final ModelPart bone113;
   private final ModelPart bone114;
   private final ModelPart bone115;
   private final ModelPart bone116;
   private final ModelPart bone117;
   private final ModelPart bone118;
   private final ModelPart bone119;
   private final ModelPart bone120;
   private final ModelPart bone121;
   private final ModelPart bone123;
   private final ModelPart bone124;
   private final ModelPart bone125;
   private final ModelPart bone126;
   private final ModelPart bone127;
   private final ModelPart bone128;
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
   private final ModelPart bone145;
   private final ModelPart bone146;
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
   private final ModelPart bone176;
   private final ModelPart bone177;
   private final ModelPart bone178;
   private final ModelPart bone179;
   private final ModelPart bone180;
   private final ModelPart bone182;
   private final ModelPart bone183;
   private final ModelPart bone184;
   private final ModelPart bone185;
   private final ModelPart bone186;
   private final ModelPart bone187;
   private final ModelPart bone188;
   private final ModelPart bone189;
   private final ModelPart bone190;
   private final ModelPart bone191;
   private final ModelPart bone192;
   private final ModelPart bone193;
   private final ModelPart bone194;
   private final ModelPart bone195;
   private final ModelPart bone196;
   private final ModelPart bone197;
   private final ModelPart bone198;
   private final ModelPart bone199;
   private final ModelPart bone200;
   private final ModelPart bone201;
   private final ModelPart bone202;
   private final ModelPart bone203;
   private final ModelPart bone204;
   private final ModelPart bone205;
   private final ModelPart bone206;
   private final ModelPart bone207;
   private final ModelPart bone208;
   private final ModelPart bone209;
   private final ModelPart bone210;
   private final ModelPart bone211;
   private final ModelPart bone212;
   private final ModelPart bone213;
   private final ModelPart bone214;
   private final ModelPart bone215;
   private final ModelPart bone216;
   private final ModelPart bone217;
   private final ModelPart bone218;
   private final ModelPart bone219;
   private final ModelPart bone220;
   private final ModelPart bone221;
   private final ModelPart bone222;
   private final ModelPart bone223;
   private final ModelPart bone224;
   private final ModelPart bone225;
   private final ModelPart bone226;
   private final ModelPart bone227;
   private final ModelPart bone228;
   private final ModelPart bone229;
   private final ModelPart bone230;
   private final ModelPart bone231;
   private final ModelPart bone232;
   private final ModelPart bone233;
   private final ModelPart bone234;
   private final ModelPart bone235;
   private final ModelPart bone236;
   private final ModelPart bone237;
   private final ModelPart bone238;
   private final ModelPart bone239;
   private final ModelPart bone299;
   private final ModelPart bone300;
   private final ModelPart bone301;
   private final ModelPart DebrisRing;

   public WitherStormP4(ModelPart root) {
      super(root);
      this.root = root;
      this.spawnAnimation = WitherStormP4Anim.Spawn.bake(root);
      this.idleAnimation = WitherStormP4Anim.Idle.bake(root);
      this.bone = root.getChild("bone");
      this.bone2 = this.bone.getChild("bone2");
      this.bone3 = this.bone2.getChild("bone3");
      this.bone4 = this.bone.getChild("bone4");
      this.bone63 = this.bone.getChild("bone63");
      this.bone64 = this.bone63.getChild("bone64");
      this.bone122 = this.bone.getChild("bone122");
      this.bone181 = this.bone.getChild("bone181");
      this.bone240 = this.bone.getChild("bone240");
      this.bone241 = this.bone240.getChild("bone241");
      this.bone242 = this.bone241.getChild("bone242");
      this.bone243 = this.bone242.getChild("bone243");
      this.bone244 = this.bone243.getChild("bone244");
      this.bone245 = this.bone244.getChild("bone245");
      this.bone246 = this.bone245.getChild("bone246");
      this.bone247 = this.bone246.getChild("bone247");
      this.bone248 = this.bone247.getChild("bone248");
      this.bone249 = this.bone248.getChild("bone249");
      this.bone250 = this.bone249.getChild("bone250");
      this.bone251 = this.bone250.getChild("bone251");
      this.bone252 = this.bone251.getChild("bone252");
      this.bone253 = this.bone252.getChild("bone253");
      this.bone254 = this.bone253.getChild("bone254");
      this.bone255 = this.bone254.getChild("bone255");
      this.bone256 = this.bone255.getChild("bone256");
      this.bone257 = this.bone256.getChild("bone257");
      this.bone258 = this.bone257.getChild("bone258");
      this.bone259 = this.bone258.getChild("bone259");
      this.bone260 = this.bone259.getChild("bone260");
      this.bone261 = this.bone260.getChild("bone261");
      this.bone262 = this.bone261.getChild("bone262");
      this.bone263 = this.bone262.getChild("bone263");
      this.bone264 = this.bone263.getChild("bone264");
      this.bone265 = this.bone264.getChild("bone265");
      this.bone266 = this.bone265.getChild("bone266");
      this.bone267 = this.bone266.getChild("bone267");
      this.bone268 = this.bone267.getChild("bone268");
      this.bone269 = this.bone268.getChild("bone269");
      this.bone270 = this.bone269.getChild("bone270");
      this.bone271 = this.bone270.getChild("bone271");
      this.bone272 = this.bone271.getChild("bone272");
      this.bone273 = this.bone272.getChild("bone273");
      this.bone274 = this.bone273.getChild("bone274");
      this.bone275 = this.bone274.getChild("bone275");
      this.bone276 = this.bone275.getChild("bone276");
      this.bone277 = this.bone276.getChild("bone277");
      this.bone278 = this.bone277.getChild("bone278");
      this.bone279 = this.bone278.getChild("bone279");
      this.bone280 = this.bone279.getChild("bone280");
      this.bone281 = this.bone280.getChild("bone281");
      this.bone282 = this.bone281.getChild("bone282");
      this.bone283 = this.bone282.getChild("bone283");
      this.bone284 = this.bone283.getChild("bone284");
      this.bone285 = this.bone284.getChild("bone285");
      this.bone286 = this.bone285.getChild("bone286");
      this.bone287 = this.bone286.getChild("bone287");
      this.bone288 = this.bone287.getChild("bone288");
      this.bone289 = this.bone288.getChild("bone289");
      this.bone290 = this.bone289.getChild("bone290");
      this.bone291 = this.bone290.getChild("bone291");
      this.bone292 = this.bone291.getChild("bone292");
      this.bone293 = this.bone292.getChild("bone293");
      this.bone294 = this.bone293.getChild("bone294");
      this.bone295 = this.bone294.getChild("bone295");
      this.bone296 = this.bone295.getChild("bone296");
      this.bone297 = this.bone296.getChild("bone297");
      this.bone298 = this.bone297.getChild("bone298");
      this.bone5 = this.bone240.getChild("bone5");
      this.bone6 = this.bone5.getChild("bone6");
      this.bone7 = this.bone6.getChild("bone7");
      this.bone8 = this.bone7.getChild("bone8");
      this.bone9 = this.bone8.getChild("bone9");
      this.bone10 = this.bone9.getChild("bone10");
      this.bone11 = this.bone10.getChild("bone11");
      this.bone12 = this.bone11.getChild("bone12");
      this.bone13 = this.bone12.getChild("bone13");
      this.bone14 = this.bone13.getChild("bone14");
      this.bone15 = this.bone14.getChild("bone15");
      this.bone16 = this.bone15.getChild("bone16");
      this.bone17 = this.bone16.getChild("bone17");
      this.bone18 = this.bone17.getChild("bone18");
      this.bone19 = this.bone18.getChild("bone19");
      this.bone20 = this.bone19.getChild("bone20");
      this.bone21 = this.bone20.getChild("bone21");
      this.bone22 = this.bone21.getChild("bone22");
      this.bone23 = this.bone22.getChild("bone23");
      this.bone24 = this.bone23.getChild("bone24");
      this.bone25 = this.bone24.getChild("bone25");
      this.bone26 = this.bone25.getChild("bone26");
      this.bone27 = this.bone26.getChild("bone27");
      this.bone28 = this.bone27.getChild("bone28");
      this.bone29 = this.bone28.getChild("bone29");
      this.bone30 = this.bone29.getChild("bone30");
      this.bone31 = this.bone30.getChild("bone31");
      this.bone32 = this.bone31.getChild("bone32");
      this.bone33 = this.bone32.getChild("bone33");
      this.bone34 = this.bone33.getChild("bone34");
      this.bone35 = this.bone34.getChild("bone35");
      this.bone36 = this.bone35.getChild("bone36");
      this.bone37 = this.bone36.getChild("bone37");
      this.bone38 = this.bone37.getChild("bone38");
      this.bone39 = this.bone38.getChild("bone39");
      this.bone40 = this.bone39.getChild("bone40");
      this.bone41 = this.bone40.getChild("bone41");
      this.bone42 = this.bone41.getChild("bone42");
      this.bone43 = this.bone42.getChild("bone43");
      this.bone44 = this.bone43.getChild("bone44");
      this.bone45 = this.bone44.getChild("bone45");
      this.bone46 = this.bone45.getChild("bone46");
      this.bone47 = this.bone46.getChild("bone47");
      this.bone48 = this.bone47.getChild("bone48");
      this.bone49 = this.bone48.getChild("bone49");
      this.bone50 = this.bone49.getChild("bone50");
      this.bone51 = this.bone50.getChild("bone51");
      this.bone52 = this.bone51.getChild("bone52");
      this.bone53 = this.bone52.getChild("bone53");
      this.bone54 = this.bone53.getChild("bone54");
      this.bone55 = this.bone54.getChild("bone55");
      this.bone56 = this.bone55.getChild("bone56");
      this.bone57 = this.bone56.getChild("bone57");
      this.bone58 = this.bone57.getChild("bone58");
      this.bone59 = this.bone58.getChild("bone59");
      this.bone60 = this.bone59.getChild("bone60");
      this.bone61 = this.bone60.getChild("bone61");
      this.bone62 = this.bone61.getChild("bone62");
      this.bone65 = this.bone240.getChild("bone65");
      this.bone66 = this.bone65.getChild("bone66");
      this.bone67 = this.bone66.getChild("bone67");
      this.bone68 = this.bone67.getChild("bone68");
      this.bone69 = this.bone68.getChild("bone69");
      this.bone70 = this.bone69.getChild("bone70");
      this.bone71 = this.bone70.getChild("bone71");
      this.bone72 = this.bone71.getChild("bone72");
      this.bone73 = this.bone72.getChild("bone73");
      this.bone74 = this.bone73.getChild("bone74");
      this.bone75 = this.bone74.getChild("bone75");
      this.bone76 = this.bone75.getChild("bone76");
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
      this.bone91 = this.bone90.getChild("bone91");
      this.bone92 = this.bone91.getChild("bone92");
      this.bone93 = this.bone92.getChild("bone93");
      this.bone94 = this.bone93.getChild("bone94");
      this.bone95 = this.bone94.getChild("bone95");
      this.bone96 = this.bone95.getChild("bone96");
      this.bone97 = this.bone96.getChild("bone97");
      this.bone98 = this.bone97.getChild("bone98");
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
      this.bone113 = this.bone112.getChild("bone113");
      this.bone114 = this.bone113.getChild("bone114");
      this.bone115 = this.bone114.getChild("bone115");
      this.bone116 = this.bone115.getChild("bone116");
      this.bone117 = this.bone116.getChild("bone117");
      this.bone118 = this.bone117.getChild("bone118");
      this.bone119 = this.bone118.getChild("bone119");
      this.bone120 = this.bone119.getChild("bone120");
      this.bone121 = this.bone120.getChild("bone121");
      this.bone123 = this.bone121.getChild("bone123");
      this.bone124 = this.bone240.getChild("bone124");
      this.bone125 = this.bone124.getChild("bone125");
      this.bone126 = this.bone125.getChild("bone126");
      this.bone127 = this.bone126.getChild("bone127");
      this.bone128 = this.bone127.getChild("bone128");
      this.bone129 = this.bone128.getChild("bone129");
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
      this.bone144 = this.bone143.getChild("bone144");
      this.bone145 = this.bone144.getChild("bone145");
      this.bone146 = this.bone145.getChild("bone146");
      this.bone147 = this.bone146.getChild("bone147");
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
      this.bone161 = this.bone160.getChild("bone161");
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
      this.bone176 = this.bone175.getChild("bone176");
      this.bone177 = this.bone176.getChild("bone177");
      this.bone178 = this.bone177.getChild("bone178");
      this.bone179 = this.bone178.getChild("bone179");
      this.bone180 = this.bone179.getChild("bone180");
      this.bone182 = this.bone180.getChild("bone182");
      this.bone183 = this.bone240.getChild("bone183");
      this.bone184 = this.bone183.getChild("bone184");
      this.bone185 = this.bone184.getChild("bone185");
      this.bone186 = this.bone185.getChild("bone186");
      this.bone187 = this.bone186.getChild("bone187");
      this.bone188 = this.bone187.getChild("bone188");
      this.bone189 = this.bone188.getChild("bone189");
      this.bone190 = this.bone189.getChild("bone190");
      this.bone191 = this.bone190.getChild("bone191");
      this.bone192 = this.bone191.getChild("bone192");
      this.bone193 = this.bone192.getChild("bone193");
      this.bone194 = this.bone193.getChild("bone194");
      this.bone195 = this.bone194.getChild("bone195");
      this.bone196 = this.bone195.getChild("bone196");
      this.bone197 = this.bone196.getChild("bone197");
      this.bone198 = this.bone197.getChild("bone198");
      this.bone199 = this.bone198.getChild("bone199");
      this.bone200 = this.bone199.getChild("bone200");
      this.bone201 = this.bone200.getChild("bone201");
      this.bone202 = this.bone201.getChild("bone202");
      this.bone203 = this.bone202.getChild("bone203");
      this.bone204 = this.bone203.getChild("bone204");
      this.bone205 = this.bone204.getChild("bone205");
      this.bone206 = this.bone205.getChild("bone206");
      this.bone207 = this.bone206.getChild("bone207");
      this.bone208 = this.bone207.getChild("bone208");
      this.bone209 = this.bone208.getChild("bone209");
      this.bone210 = this.bone209.getChild("bone210");
      this.bone211 = this.bone210.getChild("bone211");
      this.bone212 = this.bone211.getChild("bone212");
      this.bone213 = this.bone212.getChild("bone213");
      this.bone214 = this.bone213.getChild("bone214");
      this.bone215 = this.bone214.getChild("bone215");
      this.bone216 = this.bone215.getChild("bone216");
      this.bone217 = this.bone216.getChild("bone217");
      this.bone218 = this.bone217.getChild("bone218");
      this.bone219 = this.bone218.getChild("bone219");
      this.bone220 = this.bone219.getChild("bone220");
      this.bone221 = this.bone220.getChild("bone221");
      this.bone222 = this.bone221.getChild("bone222");
      this.bone223 = this.bone222.getChild("bone223");
      this.bone224 = this.bone223.getChild("bone224");
      this.bone225 = this.bone224.getChild("bone225");
      this.bone226 = this.bone225.getChild("bone226");
      this.bone227 = this.bone226.getChild("bone227");
      this.bone228 = this.bone227.getChild("bone228");
      this.bone229 = this.bone228.getChild("bone229");
      this.bone230 = this.bone229.getChild("bone230");
      this.bone231 = this.bone230.getChild("bone231");
      this.bone232 = this.bone231.getChild("bone232");
      this.bone233 = this.bone232.getChild("bone233");
      this.bone234 = this.bone233.getChild("bone234");
      this.bone235 = this.bone234.getChild("bone235");
      this.bone236 = this.bone235.getChild("bone236");
      this.bone237 = this.bone236.getChild("bone237");
      this.bone238 = this.bone237.getChild("bone238");
      this.bone239 = this.bone238.getChild("bone239");
      this.bone299 = this.bone239.getChild("bone299");
      this.bone300 = this.bone.getChild("bone300");
      this.bone301 = this.bone.getChild("bone301");
      this.DebrisRing = root.getChild("DebrisRing");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition bone = root.addOrReplaceChild(
         "bone",
         CubeListBuilder.create()
            .texOffs(277, 329)
            .addBox(10.0F, -99.0F, 7.0F, 9.0F, 79.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-7.0F, -99.0F, 7.0F, 8.0F, 70.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(8.0F, -100.0F, -9.0F, 7.0F, 97.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-3.0F, -100.0F, -9.0F, 7.0F, 82.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-17.0F, -100.0F, 8.0F, 13.0F, 45.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(16.0F, -65.0F, -9.0F, 11.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(19.0F, -90.0F, 7.0F, 8.0F, 37.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(19.0F, -118.0F, 15.0F, 8.0F, 28.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(11.0F, -100.0F, 15.0F, 8.0F, 27.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-8.0F, -109.0F, 33.0F, 9.0F, 26.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-16.0F, -109.0F, 33.0F, 8.0F, 18.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(1.0F, -109.0F, 33.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(10.0F, -154.0F, 22.0F, 17.0F, 54.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-8.0F, -184.0F, 33.0F, 26.0F, 75.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(26.0F, -190.0F, 33.0F, 9.0F, 68.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(26.0F, -184.0F, 23.0F, 9.0F, 52.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(18.0F, -193.0F, 33.0F, 8.0F, 49.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-8.0F, -144.0F, 25.0F, 18.0F, 35.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-16.0F, -109.0F, 25.0F, 8.0F, 26.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-26.0F, -128.0F, 25.0F, 10.0F, 63.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-16.0F, -128.0F, 33.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-26.0F, -169.0F, 25.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-26.0F, -169.0F, 33.0F, 18.0F, 34.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-17.0F, -169.0F, 41.0F, 9.0F, 34.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-8.0F, -188.0F, 41.0F, 8.0F, 44.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-27.0F, -188.0F, 13.0F, 19.0F, 9.0F, 36.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(10.0F, -188.0F, 41.0F, 8.0F, 33.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(27.0F, -188.0F, 41.0F, 8.0F, 25.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(35.0F, -192.0F, 33.0F, 8.0F, 42.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -191.0F, 33.0F, 8.0F, 28.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(51.0F, -192.0F, 13.0F, 9.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -191.0F, 25.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(35.0F, -184.0F, 25.0F, 8.0F, 34.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(35.0F, -184.0F, 9.0F, 8.0F, 43.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(35.0F, -184.0F, 1.0F, 8.0F, 54.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(35.0F, -168.0F, -23.0F, 8.0F, 15.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(27.0F, -162.0F, -33.0F, 8.0F, 16.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(19.0F, -146.0F, -33.0F, 8.0F, 16.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(11.0F, -146.0F, -33.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(3.0F, -154.0F, -33.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -162.0F, -41.0F, 14.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-4.0F, -162.0F, -49.0F, 23.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -171.0F, -49.0F, 8.0F, 17.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -171.0F, -49.0F, 45.0F, 9.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(34.0F, -171.0F, -41.0F, 17.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -171.0F, -49.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(11.0F, -162.0F, -41.0F, 24.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(3.0F, -146.0F, -33.0F, 8.0F, 16.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -146.0F, -33.0F, 8.0F, 16.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -154.0F, -33.0F, 8.0F, 15.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -139.0F, -26.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -192.0F, 1.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -192.0F, 9.0F, 8.0F, 31.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -184.0F, -8.0F, 8.0F, 8.0F, 17.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(51.0F, -184.0F, -24.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(51.0F, -192.0F, -8.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(34.0F, -187.0F, -49.0F, 24.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -171.0F, -55.0F, 37.0F, 8.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-32.0F, -179.0F, -60.0F, 66.0F, 8.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -179.0F, -67.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -179.0F, -67.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -171.0F, -68.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -163.0F, -60.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -171.0F, -60.0F, 24.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -187.0F, -83.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -179.0F, -76.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-27.0F, -195.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -219.0F, -115.0F, 8.0F, 16.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -195.0F, -83.0F, 40.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -199.0F, -75.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-67.0F, -195.0F, -75.0F, 24.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-67.0F, -195.0F, -67.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-67.0F, -195.0F, -51.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-67.0F, -195.0F, -27.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -195.0F, -27.0F, 8.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -195.0F, -1.0F, 8.0F, 8.0F, 21.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-51.0F, -195.0F, -1.0F, 8.0F, 8.0F, 21.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -187.0F, -27.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -171.0F, -11.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -179.0F, -68.0F, 24.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -163.0F, -27.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -155.0F, -36.0F, 16.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-51.0F, -171.0F, -35.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -195.0F, -51.0F, 8.0F, 8.0F, 36.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-51.0F, -187.0F, -51.0F, 8.0F, 8.0F, 56.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -179.0F, -42.0F, 8.0F, 8.0F, 56.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-75.0F, -195.0F, -19.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-75.0F, -195.0F, -68.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-75.0F, -195.0F, -52.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-83.0F, -203.0F, -52.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-83.0F, -203.0F, -68.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-83.0F, -219.0F, -68.0F, 8.0F, 16.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-75.0F, -243.0F, -11.0F, 8.0F, 41.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-67.0F, -243.0F, -11.0F, 8.0F, 49.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -190.0F, -75.0F, 24.0F, 11.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -190.0F, -68.0F, 24.0F, 11.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -179.0F, -59.0F, 24.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -179.0F, -51.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -171.0F, -51.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-51.0F, -171.0F, -44.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-51.0F, -163.0F, -36.0F, 17.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -163.0F, -45.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -163.0F, -45.0F, 8.0F, 16.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-27.0F, -163.0F, -45.0F, 8.0F, 16.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -195.0F, -68.0F, 24.0F, 16.0F, 17.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -211.0F, -107.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -211.0F, -100.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -203.0F, -91.0F, 16.0F, 8.0F, 23.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -203.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -211.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -211.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -219.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -219.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -227.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-27.0F, -227.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -235.0F, -99.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-51.0F, -243.0F, -115.0F, 16.0F, 24.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-51.0F, -234.0F, -123.0F, 16.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -235.0F, -115.0F, 8.0F, 16.0F, 23.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-70.0F, -235.0F, -123.0F, 8.0F, 16.0F, 23.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-66.0F, -240.0F, -123.0F, 8.0F, 16.0F, 23.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -219.0F, -123.0F, 24.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -251.0F, -99.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -251.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-59.0F, -243.0F, -100.0F, 8.0F, 8.0F, 17.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-59.0F, -259.0F, -83.0F, 40.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -251.0F, -83.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -235.0F, -83.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(5.0F, -219.0F, -83.0F, 8.0F, 40.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(13.0F, -203.0F, -83.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(5.0F, -179.0F, -91.0F, 24.0F, 16.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(5.0F, -179.0F, -76.0F, 16.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(5.0F, -187.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(5.0F, -179.0F, -83.0F, 32.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(4.0F, -179.0F, -75.0F, 41.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(398, 229)
            .addBox(21.0F, -219.0F, -75.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(13.0F, -235.0F, -75.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(5.0F, -243.0F, -75.0F, 8.0F, 24.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(5.0F, -251.0F, -51.0F, 8.0F, 8.0F, 40.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(13.0F, -251.0F, -67.0F, 16.0F, 16.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(45.0F, -227.0F, -67.0F, 64.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(45.0F, -219.0F, -59.0F, 64.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(101.0F, -211.0F, -59.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(54.0F, -219.0F, -75.0F, 64.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(70.0F, -219.0F, -83.0F, 39.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(94.0F, -203.0F, -83.0F, 8.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(62.0F, -203.0F, -67.0F, 8.0F, 16.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(33.0F, -219.0F, -59.0F, 37.0F, 40.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(54.0F, -203.0F, -67.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(58.0F, -203.0F, -49.0F, 12.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(51.0F, -192.0F, -33.0F, 16.0F, 8.0F, 25.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(67.0F, -203.0F, -33.0F, 8.0F, 11.0F, 41.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(50.0F, -203.0F, 24.0F, 10.0F, 11.0F, 21.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(50.0F, -203.0F, -8.0F, 17.0F, 11.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(75.0F, -203.0F, -33.0F, 8.0F, 11.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(102.0F, -203.0F, -75.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(94.0F, -195.0F, -75.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(78.0F, -203.0F, -75.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(86.0F, -195.0F, -75.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(78.0F, -211.0F, -83.0F, 31.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(62.0F, -211.0F, -75.0F, 56.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(29.0F, -219.0F, -67.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(29.0F, -203.0F, -67.0F, 16.0F, 32.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(3.0F, -187.0F, -67.0F, 26.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(29.0F, -211.0F, -67.0F, 89.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(63.0F, -235.0F, -35.0F, 30.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(83.0F, -235.0F, 21.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(59.0F, -227.0F, 28.0F, 32.0F, 16.0F, 31.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(59.0F, -227.0F, 59.0F, 24.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(35.0F, -227.0F, 37.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(11.0F, -227.0F, 37.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -227.0F, 45.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -188.0F, 41.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(51.0F, -203.0F, 41.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(51.0F, -219.0F, 37.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(51.0F, -219.0F, 45.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(59.0F, -211.0F, 45.0F, 8.0F, 19.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(60.0F, -211.0F, 29.0F, 7.0F, 19.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(60.0F, -222.0F, 21.0F, 7.0F, 19.0F, 25.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(67.0F, -211.0F, -3.0F, 8.0F, 8.0F, 69.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(75.0F, -211.0F, -3.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(19.0F, -227.0F, 37.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-13.0F, -227.0F, 37.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(75.0F, -235.0F, 29.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(67.0F, -235.0F, 29.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(59.0F, -235.0F, 29.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(45.0F, -235.0F, -59.0F, 56.0F, 32.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(45.0F, -251.0F, -51.0F, 40.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(37.0F, -243.0F, -59.0F, 32.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(45.0F, -259.0F, -51.0F, 24.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(29.0F, -259.0F, -59.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(45.0F, -227.0F, -35.0F, 56.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(45.0F, -227.0F, -27.0F, 48.0F, 24.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(46.0F, -235.0F, -11.0F, 37.0F, 24.0F, 40.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(29.0F, -243.0F, -67.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(21.0F, -235.0F, -67.0F, 24.0F, 16.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -251.0F, -75.0F, 16.0F, 24.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(13.0F, -219.0F, -75.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(21.0F, -187.0F, -75.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-3.0F, -227.0F, -83.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-51.0F, -251.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -259.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -243.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-27.0F, -235.0F, -91.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-11.0F, -195.0F, -99.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -203.0F, -99.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -227.0F, -107.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-27.0F, -219.0F, -99.0F, 8.0F, 24.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-3.0F, -203.0F, -91.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-3.0F, -179.0F, -83.0F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-3.0F, -171.0F, -76.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-3.0F, -163.0F, -68.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-29.0F, -179.0F, -49.0F, 63.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -176.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(35.0F, -184.0F, -33.0F, 16.0F, 38.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -168.0F, -15.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(43.0F, -184.0F, 17.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-36.0F, -219.0F, 19.0F, 87.0F, 31.0F, 30.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(0.0F, -188.0F, 41.0F, 10.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-17.0F, -169.0F, 25.0F, 9.0F, 34.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-8.0F, -109.0F, 15.0F, 19.0F, 26.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(18.0F, -90.0F, -18.0F, 9.0F, 25.0F, 17.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(18.0F, -153.0F, -18.0F, 17.0F, 63.0F, 19.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(27.0F, -141.0F, 9.0F, 8.0F, 42.0F, 14.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(27.0F, -141.0F, 1.0F, 8.0F, 26.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(1.0F, -100.0F, -18.0F, 9.0F, 35.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-17.0F, -110.0F, -18.0F, 9.0F, 31.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-26.0F, -184.0F, 17.0F, 9.0F, 80.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-17.0F, -184.0F, 25.0F, 9.0F, 80.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-17.0F, -151.0F, 17.0F, 9.0F, 54.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-26.0F, -153.0F, 8.0F, 9.0F, 53.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -169.0F, 8.0F, 9.0F, 25.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -169.0F, 17.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -179.0F, -1.0F, 9.0F, 10.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-43.0F, -187.0F, -1.0F, 17.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-36.0F, -188.0F, 5.0F, 10.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -169.0F, -1.0F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-35.0F, -171.0F, -18.0F, 9.0F, 34.0F, 17.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-17.0F, -110.0F, -1.0F, 9.0F, 21.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-8.0F, -111.0F, -18.0F, 9.0F, 11.0F, 26.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-17.0F, -120.0F, -18.0F, 9.0F, 10.0F, 26.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-26.0F, -168.0F, -18.0F, 9.0F, 58.0F, 26.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-19.0F, -132.0F, -26.0F, 9.0F, 12.0F, 25.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-10.0F, -146.0F, -26.0F, 11.0F, 35.0F, 25.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(1.0F, -146.0F, -26.0F, 17.0F, 46.0F, 25.0F, new CubeDeformation(0.0F))
            .texOffs(277, 329)
            .addBox(-8.0F, -100.0F, -18.0F, 9.0F, 10.0F, 9.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -15.0F, 7.0F)
      );
      PartDefinition bone2 = bone.addOrReplaceChild(
         "bone2",
         CubeListBuilder.create()
            .texOffs(288, 164)
            .addBox(29.0F, -251.0F, -59.0F, 24.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(45.0F, -267.0F, -51.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(29.0F, -275.0F, -51.0F, 16.0F, 16.0F, 48.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(21.0F, -267.0F, -51.0F, 8.0F, 16.0F, 56.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(13.0F, -259.0F, -43.0F, 8.0F, 8.0F, 55.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-3.0F, -259.0F, -51.0F, 8.0F, 20.0F, 63.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-11.0F, -267.0F, -43.0F, 8.0F, 8.0F, 48.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-27.0F, -267.0F, -60.0F, 16.0F, 16.0F, 72.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-27.0F, -259.0F, -75.0F, 16.0F, 8.0F, 72.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-19.0F, -275.0F, -43.0F, 8.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-27.0F, -283.0F, -36.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-35.0F, -283.0F, -60.0F, 8.0F, 8.0F, 48.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-43.0F, -283.0F, -52.0F, 8.0F, 8.0F, 40.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -283.0F, -44.0F, 8.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -283.0F, -28.0F, 24.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -275.0F, -76.0F, 48.0F, 8.0F, 64.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -291.0F, -36.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-59.0F, -291.0F, -28.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -291.0F, -44.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -291.0F, -60.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -291.0F, -60.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -291.0F, -68.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -283.0F, -52.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-27.0F, -275.0F, -68.0F, 8.0F, 8.0F, 64.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -275.0F, -12.0F, 24.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -283.0F, -12.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -275.0F, -4.0F, 24.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -275.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -267.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -267.0F, -4.0F, 48.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -283.0F, -68.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -283.0F, -68.0F, 8.0F, 16.0F, 48.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-91.0F, -267.0F, -52.0F, 8.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-91.0F, -275.0F, -44.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-91.0F, -275.0F, -68.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -267.0F, -60.0F, 8.0F, 32.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -227.0F, -52.0F, 8.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -235.0F, -52.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-91.0F, -235.0F, -44.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-91.0F, -235.0F, -60.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-91.0F, -219.0F, -76.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(246, 277)
            .addBox(-91.0F, -243.0F, -84.0F, 8.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -235.0F, -84.0F, 16.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -236.0F, -84.0F, 32.0F, 41.0F, 73.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-91.0F, -243.0F, -68.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -243.0F, -76.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -243.0F, -84.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -219.0F, -92.0F, 24.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -227.0F, -100.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -219.0F, -108.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -235.0F, -92.0F, 16.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -243.0F, -76.0F, 8.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -267.0F, -68.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -259.0F, -76.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -219.0F, -28.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -243.0F, -20.0F, 8.0F, 16.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -259.0F, -20.0F, 8.0F, 16.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -259.0F, -12.0F, 24.0F, 16.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -267.0F, -36.0F, 8.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-91.0F, -259.0F, -52.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-83.0F, -275.0F, -20.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-43.0F, -267.0F, 4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-43.0F, -259.0F, -6.0F, 16.0F, 8.0F, 27.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-43.0F, -251.0F, -6.0F, 7.0F, 8.0F, 27.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -243.0F, -6.0F, 31.0F, 24.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-12.0F, -243.0F, 21.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-20.0F, -235.0F, 12.0F, 66.0F, 8.0F, 17.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-12.0F, -235.0F, 29.0F, 40.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-36.0F, -227.0F, 21.0F, 66.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-59.0F, -227.0F, 29.0F, 65.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-43.0F, -219.0F, 20.0F, 49.0F, 24.0F, 17.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(6.0F, -227.0F, 29.0F, 53.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(5.0F, -243.0F, 21.0F, 32.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-59.0F, -243.0F, 12.0F, 23.0F, 24.0F, 17.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-36.0F, -251.0F, 3.0F, 32.0F, 26.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(45.0F, -243.0F, -3.0F, 24.0F, 8.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-4.0F, -243.0F, 3.0F, 49.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(5.0F, -251.0F, 12.0F, 16.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -267.0F, 4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-59.0F, -251.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -251.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -251.0F, 4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -219.0F, 12.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-59.0F, -219.0F, -3.0F, 8.0F, 24.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-59.0F, -211.0F, 12.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -203.0F, 12.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -211.0F, 20.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-51.0F, -219.0F, 29.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -219.0F, 4.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-59.0F, -219.0F, 20.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-75.0F, -275.0F, -12.0F, 24.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -267.0F, -76.0F, 48.0F, 8.0F, 64.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-67.0F, -267.0F, -83.0F, 48.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-27.0F, -267.0F, -68.0F, 8.0F, 8.0F, 64.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(-11.0F, -259.0F, -60.0F, 8.0F, 17.0F, 72.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(37.0F, -283.0F, -43.0F, 8.0F, 8.0F, 40.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(45.0F, -283.0F, -43.0F, 8.0F, 16.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(46.0F, -288.0F, -43.0F, 13.0F, 16.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(75.0F, -288.0F, -32.0F, 13.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(53.0F, -283.0F, -43.0F, 6.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(61.0F, -267.0F, -43.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(53.0F, -275.0F, -35.0F, 16.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(45.0F, -275.0F, -35.0F, 8.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(45.0F, -267.0F, -35.0F, 8.0F, 24.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(53.0F, -267.0F, -35.0F, 8.0F, 24.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(45.0F, -251.0F, -3.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(69.0F, -243.0F, -35.0F, 8.0F, 8.0F, 40.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(77.0F, -243.0F, -19.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(69.0F, -251.0F, -35.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(61.0F, -267.0F, -35.0F, 24.0F, 16.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(38.0F, -259.0F, -3.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(29.0F, -267.0F, -3.0F, 9.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(61.0F, -259.0F, -35.0F, 8.0F, 16.0F, 32.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(69.0F, -275.0F, -35.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(72.0F, -283.0F, -35.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(66.0F, -283.0F, -19.0F, 16.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(69.0F, -267.0F, -43.0F, 8.0F, 16.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(77.0F, -275.0F, -43.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(29.0F, -283.0F, -11.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(21.0F, -275.0F, -3.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(5.0F, -259.0F, -11.0F, 8.0F, 8.0F, 23.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 1.0F, 0.0F)
      );
      PartDefinition bone3 = bone2.addOrReplaceChild(
         "bone3",
         CubeListBuilder.create()
            .texOffs(288, 164)
            .addBox(13.0F, -267.0F, 5.0F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(21.0F, -259.0F, 5.0F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(288, 164)
            .addBox(21.0F, -251.0F, 5.0F, 32.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition bone4 = bone.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(-26.0F, -111.0F, 23.0F));
      PartDefinition bone63 = bone.addOrReplaceChild(
         "bone63", CubeListBuilder.create(), PartPose.offsetAndRotation(-64.0F, -280.0F, -51.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition bone64 = bone63.addOrReplaceChild(
         "bone64",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-16.0F, -7.0F, -8.0F, 15.0F, 17.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, -7.0F, -8.0F, 11.0F, 4.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, 1.0F, -8.0F, 11.0F, 9.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-29.0F, -3.0F, -8.0F, 6.0F, 13.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-31.0F, -7.0F, -4.0F, 17.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, -7.0F, 10.0F, 7.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-16.0F, -11.0F, -8.0F, 18.0F, 21.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-13.0F, -7.0F, -12.0F, 15.0F, 17.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-20.0F, -11.0F, -5.0F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(3.0F, -11.0F, -1.0F, 0.0F, 0.0F, -1.5708F)
      );
      PartDefinition bone122 = bone.addOrReplaceChild(
         "bone122", CubeListBuilder.create(), PartPose.offsetAndRotation(14.0F, -256.0F, -38.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition bone181 = bone.addOrReplaceChild(
         "bone181", CubeListBuilder.create(), PartPose.offsetAndRotation(63.0F, -264.0F, -15.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition bone240 = bone.addOrReplaceChild(
         "bone240", CubeListBuilder.create(), PartPose.offsetAndRotation(72.0F, -211.0F, 49.0F, 0.0F, 0.0F, -3.1416F)
      );
      PartDefinition bone241 = bone240.addOrReplaceChild(
         "bone241",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-16.0F, -7.0F, -8.0F, 15.0F, 17.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, -7.0F, -8.0F, 11.0F, 4.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, 1.0F, -8.0F, 11.0F, 9.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-37.0F, -3.0F, -8.0F, 14.0F, 13.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-31.0F, -7.0F, -4.0F, 17.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, -7.0F, 10.0F, 7.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-16.0F, -11.0F, -8.0F, 18.0F, 21.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-13.0F, -7.0F, -12.0F, 15.0F, 17.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-20.0F, -11.0F, -5.0F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(32.0F, -74.0F, -42.0F, 0.0F, 0.0436F, 0.0F)
      );
      PartDefinition bone242 = bone241.addOrReplaceChild(
         "bone242",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-6.0F, -10.0F, -8.0F, 8.0F, 23.0F, 14.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-14.0F, -10.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-10.0F, -10.0F, -5.0F, 6.0F, 23.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-10.0F, -6.0F, -8.0F, 6.0F, 15.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-14.0F, -3.0F, -8.0F, 6.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-18.0F, -6.0F, 6.0F, 10.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-27.0F, -6.0F, 2.0F, 15.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-15.0F, -2.0F, -5.0F, 8.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-27.0F, -6.0F, -5.0F, 19.0F, 19.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-27.0F, 13.0F, -1.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-31.0F, -6.0F, -4.0F)
      );
      PartDefinition bone243 = bone242.addOrReplaceChild(
         "bone243",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -12.0F, -6.0F, 6.0F, 15.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 3.0F, -6.0F, 6.0F, 4.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, 7.0F, -6.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -8.0F, -6.0F, 6.0F, 15.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -12.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-27.0F, -1.0F, 1.0F)
      );
      PartDefinition bone244 = bone243.addOrReplaceChild(
         "bone244",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-8.0F, 4.0F, -5.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, -5.0F, 6.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -7.0F, -5.0F, 10.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -7.0F, 6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -7.0F, -9.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-8.0F, 3.0F, -1.0F)
      );
      PartDefinition bone245 = bone244.addOrReplaceChild(
         "bone245",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -2.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 5.0F, -2.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-8.0F, -1.0F, -3.0F)
      );
      PartDefinition bone246 = bone245.addOrReplaceChild(
         "bone246",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -1.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -6.0F, -1.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -3.0F, 7.0F, 6.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -6.0F, -5.0F, 10.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -2.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, 5.0F, -1.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone247 = bone246.addOrReplaceChild(
         "bone247",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, 0.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, 0.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -8.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-8.0F, 1.0F, -1.0F)
      );
      PartDefinition bone248 = bone247.addOrReplaceChild(
         "bone248",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, -4.0F, 6.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-12.0F, -8.0F, -8.0F, 6.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, 4.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-12.0F, -8.0F, -12.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -8.0F, -12.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 5.0F, 0.0F)
      );
      PartDefinition bone249 = bone248.addOrReplaceChild(
         "bone249",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-12.0F, -2.0F, 0.0F)
      );
      PartDefinition bone250 = bone249.addOrReplaceChild(
         "bone250",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -6.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 2.0F)
      );
      PartDefinition bone251 = bone250.addOrReplaceChild(
         "bone251",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -5.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone252 = bone251.addOrReplaceChild(
         "bone252",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 2.0F, -5.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -10.0F, -9.0F, 6.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 3.0F, 0.0F)
      );
      PartDefinition bone253 = bone252.addOrReplaceChild(
         "bone253",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -12.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 2.0F, 3.0F)
      );
      PartDefinition bone254 = bone253.addOrReplaceChild(
         "bone254",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 1.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 5.0F, -8.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -11.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone255 = bone254.addOrReplaceChild(
         "bone255",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -10.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone256 = bone255.addOrReplaceChild(
         "bone256",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -5.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -1.0F, -8.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -9.0F, -4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -1.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone257 = bone256.addOrReplaceChild(
         "bone257",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 0.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 7.0F, 0.0F)
      );
      PartDefinition bone258 = bone257.addOrReplaceChild(
         "bone258",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 2.0F, 4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -3.0F, 0.0F)
      );
      PartDefinition bone259 = bone258.addOrReplaceChild(
         "bone259",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-3.9253F, -5.1248F, -6.9982F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-3.9253F, 2.8752F, -2.9982F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-3.9253F, -1.1248F, 5.0018F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, -1.0F)
      );
      PartDefinition bone260 = bone259.addOrReplaceChild(
         "bone260",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -7.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 2.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 5.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-3.9253F, 0.8752F, 0.0018F)
      );
      PartDefinition bone261 = bone260.addOrReplaceChild(
         "bone261",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -6.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 6.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone262 = bone261.addOrReplaceChild(
         "bone262",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -6.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 2.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone263 = bone262.addOrReplaceChild(
         "bone263",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, -6.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -5.0F, 0.0F)
      );
      PartDefinition bone264 = bone263.addOrReplaceChild(
         "bone264",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 3.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -1.0F, -6.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -5.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -9.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone265 = bone264.addOrReplaceChild(
         "bone265",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone266 = bone265.addOrReplaceChild(
         "bone266",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone267 = bone266.addOrReplaceChild(
         "bone267",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -5.0F, -2.0F)
      );
      PartDefinition bone268 = bone267.addOrReplaceChild(
         "bone268",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 1.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone269 = bone268.addOrReplaceChild(
         "bone269",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 0.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone270 = bone269.addOrReplaceChild(
         "bone270",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 0.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone271 = bone270.addOrReplaceChild(
         "bone271",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 0.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 8.0F, 4.0F)
      );
      PartDefinition bone272 = bone271.addOrReplaceChild(
         "bone272",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone273 = bone272.addOrReplaceChild(
         "bone273",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone274 = bone273.addOrReplaceChild(
         "bone274",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone275 = bone274.addOrReplaceChild(
         "bone275",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone276 = bone275.addOrReplaceChild(
         "bone276",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone277 = bone276.addOrReplaceChild(
         "bone277",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone278 = bone277.addOrReplaceChild(
         "bone278",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 3.0F, 0.0F)
      );
      PartDefinition bone279 = bone278.addOrReplaceChild(
         "bone279",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 2.0F)
      );
      PartDefinition bone280 = bone279.addOrReplaceChild(
         "bone280",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone281 = bone280.addOrReplaceChild(
         "bone281",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone282 = bone281.addOrReplaceChild(
         "bone282",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone283 = bone282.addOrReplaceChild(
         "bone283",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone284 = bone283.addOrReplaceChild(
         "bone284",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone285 = bone284.addOrReplaceChild(
         "bone285",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -4.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone286 = bone285.addOrReplaceChild(
         "bone286",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -3.0F, -1.0F)
      );
      PartDefinition bone287 = bone286.addOrReplaceChild(
         "bone287",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone288 = bone287.addOrReplaceChild(
         "bone288",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone289 = bone288.addOrReplaceChild(
         "bone289",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone290 = bone289.addOrReplaceChild(
         "bone290",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone291 = bone290.addOrReplaceChild(
         "bone291",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone292 = bone291.addOrReplaceChild(
         "bone292",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone293 = bone292.addOrReplaceChild(
         "bone293",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone294 = bone293.addOrReplaceChild(
         "bone294",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 2.0F, 0.0F)
      );
      PartDefinition bone295 = bone294.addOrReplaceChild(
         "bone295",
         CubeListBuilder.create().texOffs(367, 421).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 1.0F, 0.0F)
      );
      PartDefinition bone296 = bone295.addOrReplaceChild(
         "bone296",
         CubeListBuilder.create().texOffs(367, 421).addBox(-4.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone297 = bone296.addOrReplaceChild(
         "bone297",
         CubeListBuilder.create().texOffs(367, 421).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone298 = bone297.addOrReplaceChild(
         "bone298",
         CubeListBuilder.create().texOffs(367, 421).addBox(-10.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone5 = bone240.addOrReplaceChild(
         "bone5",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(1.0F, -7.0F, -8.0F, 15.0F, 17.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -7.0F, -8.0F, 11.0F, 4.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, 1.0F, -8.0F, 11.0F, 9.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(23.0F, -3.0F, -8.0F, 14.0F, 13.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -7.0F, -4.0F, 17.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(18.0F, -7.0F, 10.0F, 7.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -11.0F, -8.0F, 18.0F, 21.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -12.0F, 15.0F, 17.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -11.0F, -5.0F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(101.0F, -74.0F, -42.0F, 0.0F, -0.0436F, 0.0F)
      );
      PartDefinition bone6 = bone5.addOrReplaceChild(
         "bone6",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -8.0F, 8.0F, 23.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -7.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(4.0F, -9.0F, -5.0F, 6.0F, 23.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(4.0F, -6.0F, -8.0F, 6.0F, 15.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -3.0F, -8.0F, 6.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -6.0F, 6.0F, 10.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(12.0F, -6.0F, 2.0F, 15.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(7.0F, -2.0F, -5.0F, 8.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -6.0F, -5.0F, 19.0F, 19.0F, 7.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(20.0F, 13.0F, -1.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(31.0F, -6.0F, -4.0F)
      );
      PartDefinition bone7 = bone6.addOrReplaceChild(
         "bone7",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -12.0F, -6.0F, 6.0F, 15.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 3.0F, -6.0F, 6.0F, 4.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 7.0F, -6.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -6.0F, 6.0F, 15.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -12.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(27.0F, -1.0F, 1.0F)
      );
      PartDefinition bone8 = bone7.addOrReplaceChild(
         "bone8",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 4.0F, -5.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -5.0F, 6.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -5.0F, 10.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -7.0F, 6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -7.0F, -9.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, 3.0F, -1.0F)
      );
      PartDefinition bone9 = bone8.addOrReplaceChild(
         "bone9",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -2.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -2.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, -1.0F, -3.0F)
      );
      PartDefinition bone10 = bone9.addOrReplaceChild(
         "bone10",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -1.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -6.0F, -1.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -3.0F, 7.0F, 6.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -5.0F, 10.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -2.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -1.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone11 = bone10.addOrReplaceChild(
         "bone11",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, 0.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, 0.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -8.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, 1.0F, -1.0F)
      );
      PartDefinition bone12 = bone11.addOrReplaceChild(
         "bone12",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -4.0F, 6.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(6.0F, -8.0F, -8.0F, 6.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 4.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(6.0F, -8.0F, -12.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -12.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 5.0F, 0.0F)
      );
      PartDefinition bone13 = bone12.addOrReplaceChild(
         "bone13",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(12.0F, -2.0F, 0.0F)
      );
      PartDefinition bone14 = bone13.addOrReplaceChild(
         "bone14",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -6.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 2.0F)
      );
      PartDefinition bone15 = bone14.addOrReplaceChild(
         "bone15",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -5.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone16 = bone15.addOrReplaceChild(
         "bone16",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, -5.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -10.0F, -9.0F, 6.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 3.0F, 0.0F)
      );
      PartDefinition bone17 = bone16.addOrReplaceChild(
         "bone17",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -12.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 2.0F, 3.0F)
      );
      PartDefinition bone18 = bone17.addOrReplaceChild(
         "bone18",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -8.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -11.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone19 = bone18.addOrReplaceChild(
         "bone19",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -10.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone20 = bone19.addOrReplaceChild(
         "bone20",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -5.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, -8.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -9.0F, -4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone21 = bone20.addOrReplaceChild(
         "bone21",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 7.0F, 0.0F)
      );
      PartDefinition bone22 = bone21.addOrReplaceChild(
         "bone22",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, 4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -3.0F, 0.0F)
      );
      PartDefinition bone23 = bone22.addOrReplaceChild(
         "bone23",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, -5.1248F, -6.9982F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, 2.8752F, -2.9982F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, -1.1248F, 5.0018F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, -1.0F)
      );
      PartDefinition bone24 = bone23.addOrReplaceChild(
         "bone24",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -7.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 5.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(3.9253F, 0.8752F, 0.0018F)
      );
      PartDefinition bone25 = bone24.addOrReplaceChild(
         "bone25",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 6.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone26 = bone25.addOrReplaceChild(
         "bone26",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone27 = bone26.addOrReplaceChild(
         "bone27",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -6.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -5.0F, 0.0F)
      );
      PartDefinition bone28 = bone27.addOrReplaceChild(
         "bone28",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 3.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, -6.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -5.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -9.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone29 = bone28.addOrReplaceChild(
         "bone29",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone30 = bone29.addOrReplaceChild(
         "bone30",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone31 = bone30.addOrReplaceChild(
         "bone31",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -5.0F, -2.0F)
      );
      PartDefinition bone32 = bone31.addOrReplaceChild(
         "bone32",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone33 = bone32.addOrReplaceChild(
         "bone33",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone34 = bone33.addOrReplaceChild(
         "bone34",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone35 = bone34.addOrReplaceChild(
         "bone35",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 8.0F, 4.0F)
      );
      PartDefinition bone36 = bone35.addOrReplaceChild(
         "bone36",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone37 = bone36.addOrReplaceChild(
         "bone37",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone38 = bone37.addOrReplaceChild(
         "bone38",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone39 = bone38.addOrReplaceChild(
         "bone39",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone40 = bone39.addOrReplaceChild(
         "bone40",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone41 = bone40.addOrReplaceChild(
         "bone41",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone42 = bone41.addOrReplaceChild(
         "bone42",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 3.0F, 0.0F)
      );
      PartDefinition bone43 = bone42.addOrReplaceChild(
         "bone43",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 2.0F)
      );
      PartDefinition bone44 = bone43.addOrReplaceChild(
         "bone44",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone45 = bone44.addOrReplaceChild(
         "bone45",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone46 = bone45.addOrReplaceChild(
         "bone46",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone47 = bone46.addOrReplaceChild(
         "bone47",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone48 = bone47.addOrReplaceChild(
         "bone48",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone49 = bone48.addOrReplaceChild(
         "bone49",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone50 = bone49.addOrReplaceChild(
         "bone50",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, -3.0F, -1.0F)
      );
      PartDefinition bone51 = bone50.addOrReplaceChild(
         "bone51",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone52 = bone51.addOrReplaceChild(
         "bone52",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone53 = bone52.addOrReplaceChild(
         "bone53",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone54 = bone53.addOrReplaceChild(
         "bone54",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone55 = bone54.addOrReplaceChild(
         "bone55",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone56 = bone55.addOrReplaceChild(
         "bone56",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone57 = bone56.addOrReplaceChild(
         "bone57",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone58 = bone57.addOrReplaceChild(
         "bone58",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 2.0F, 0.0F)
      );
      PartDefinition bone59 = bone58.addOrReplaceChild(
         "bone59",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, 1.0F, 0.0F)
      );
      PartDefinition bone60 = bone59.addOrReplaceChild(
         "bone60",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone61 = bone60.addOrReplaceChild(
         "bone61",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone62 = bone61.addOrReplaceChild(
         "bone62",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone65 = bone240.addOrReplaceChild(
         "bone65",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(1.0F, -7.0F, -8.0F, 15.0F, 17.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -7.0F, -8.0F, 11.0F, 4.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, 1.0F, -8.0F, 11.0F, 9.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(23.0F, -3.0F, -8.0F, 14.0F, 13.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -7.0F, -4.0F, 17.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(18.0F, -7.0F, 10.0F, 7.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -11.0F, -8.0F, 18.0F, 21.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -12.0F, 15.0F, 17.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -11.0F, -5.0F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(154.0F, 66.0F, -90.0F, 0.0F, -0.0436F, 0.0F)
      );
      PartDefinition bone66 = bone65.addOrReplaceChild(
         "bone66",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -10.0F, -8.0F, 8.0F, 23.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -10.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(4.0F, -10.0F, -5.0F, 6.0F, 23.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(4.0F, -6.0F, -8.0F, 6.0F, 15.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -3.0F, -8.0F, 6.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -6.0F, 6.0F, 10.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(12.0F, -6.0F, 2.0F, 15.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(7.0F, -2.0F, -5.0F, 8.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -6.0F, -5.0F, 19.0F, 19.0F, 7.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(20.0F, 13.0F, -1.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(31.0F, -6.0F, -4.0F)
      );
      PartDefinition bone67 = bone66.addOrReplaceChild(
         "bone67",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -12.0F, -6.0F, 6.0F, 15.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 3.0F, -6.0F, 6.0F, 4.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 7.0F, -6.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -6.0F, 6.0F, 15.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -12.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(27.0F, -1.0F, 1.0F)
      );
      PartDefinition bone68 = bone67.addOrReplaceChild(
         "bone68",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 4.0F, -5.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -5.0F, 6.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -5.0F, 10.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -7.0F, 6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -7.0F, -9.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, 3.0F, -1.0F)
      );
      PartDefinition bone69 = bone68.addOrReplaceChild(
         "bone69",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -2.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -2.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, -1.0F, -3.0F)
      );
      PartDefinition bone70 = bone69.addOrReplaceChild(
         "bone70",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -1.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -6.0F, -1.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -3.0F, 7.0F, 6.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -5.0F, 10.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -2.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -1.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone71 = bone70.addOrReplaceChild(
         "bone71",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, 0.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, 0.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -8.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, 1.0F, -1.0F)
      );
      PartDefinition bone72 = bone71.addOrReplaceChild(
         "bone72",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -4.0F, 6.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(6.0F, -8.0F, -8.0F, 6.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 4.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(6.0F, -8.0F, -12.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -12.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 5.0F, 0.0F)
      );
      PartDefinition bone73 = bone72.addOrReplaceChild(
         "bone73",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(12.0F, -2.0F, 0.0F)
      );
      PartDefinition bone74 = bone73.addOrReplaceChild(
         "bone74",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -6.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 2.0F)
      );
      PartDefinition bone75 = bone74.addOrReplaceChild(
         "bone75",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -5.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone76 = bone75.addOrReplaceChild(
         "bone76",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, -5.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -10.0F, -9.0F, 6.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 3.0F, 0.0F)
      );
      PartDefinition bone77 = bone76.addOrReplaceChild(
         "bone77",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -12.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 2.0F, 3.0F)
      );
      PartDefinition bone78 = bone77.addOrReplaceChild(
         "bone78",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -8.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -11.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone79 = bone78.addOrReplaceChild(
         "bone79",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -10.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone80 = bone79.addOrReplaceChild(
         "bone80",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -5.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, -8.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -9.0F, -4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone81 = bone80.addOrReplaceChild(
         "bone81",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 7.0F, 0.0F)
      );
      PartDefinition bone82 = bone81.addOrReplaceChild(
         "bone82",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, 4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -3.0F, 0.0F)
      );
      PartDefinition bone83 = bone82.addOrReplaceChild(
         "bone83",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, -5.1248F, -6.9982F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, 2.8752F, -2.9982F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, -1.1248F, 5.0018F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, -1.0F)
      );
      PartDefinition bone84 = bone83.addOrReplaceChild(
         "bone84",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -7.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 5.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(3.9253F, 0.8752F, 0.0018F)
      );
      PartDefinition bone85 = bone84.addOrReplaceChild(
         "bone85",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 6.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone86 = bone85.addOrReplaceChild(
         "bone86",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone87 = bone86.addOrReplaceChild(
         "bone87",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -6.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -5.0F, 0.0F)
      );
      PartDefinition bone88 = bone87.addOrReplaceChild(
         "bone88",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 3.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, -6.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -5.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -9.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone89 = bone88.addOrReplaceChild(
         "bone89",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone90 = bone89.addOrReplaceChild(
         "bone90",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone91 = bone90.addOrReplaceChild(
         "bone91",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -5.0F, -2.0F)
      );
      PartDefinition bone92 = bone91.addOrReplaceChild(
         "bone92",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone93 = bone92.addOrReplaceChild(
         "bone93",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone94 = bone93.addOrReplaceChild(
         "bone94",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone95 = bone94.addOrReplaceChild(
         "bone95",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 8.0F, 4.0F)
      );
      PartDefinition bone96 = bone95.addOrReplaceChild(
         "bone96",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone97 = bone96.addOrReplaceChild(
         "bone97",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone98 = bone97.addOrReplaceChild(
         "bone98",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone99 = bone98.addOrReplaceChild(
         "bone99",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone100 = bone99.addOrReplaceChild(
         "bone100",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone101 = bone100.addOrReplaceChild(
         "bone101",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone102 = bone101.addOrReplaceChild(
         "bone102",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 3.0F, 0.0F)
      );
      PartDefinition bone103 = bone102.addOrReplaceChild(
         "bone103",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 2.0F)
      );
      PartDefinition bone104 = bone103.addOrReplaceChild(
         "bone104",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone105 = bone104.addOrReplaceChild(
         "bone105",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone106 = bone105.addOrReplaceChild(
         "bone106",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone107 = bone106.addOrReplaceChild(
         "bone107",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone108 = bone107.addOrReplaceChild(
         "bone108",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone109 = bone108.addOrReplaceChild(
         "bone109",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone110 = bone109.addOrReplaceChild(
         "bone110",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, -3.0F, -1.0F)
      );
      PartDefinition bone111 = bone110.addOrReplaceChild(
         "bone111",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone112 = bone111.addOrReplaceChild(
         "bone112",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone113 = bone112.addOrReplaceChild(
         "bone113",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone114 = bone113.addOrReplaceChild(
         "bone114",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone115 = bone114.addOrReplaceChild(
         "bone115",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone116 = bone115.addOrReplaceChild(
         "bone116",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone117 = bone116.addOrReplaceChild(
         "bone117",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone118 = bone117.addOrReplaceChild(
         "bone118",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 2.0F, 0.0F)
      );
      PartDefinition bone119 = bone118.addOrReplaceChild(
         "bone119",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, 1.0F, 0.0F)
      );
      PartDefinition bone120 = bone119.addOrReplaceChild(
         "bone120",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone121 = bone120.addOrReplaceChild(
         "bone121",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone123 = bone121.addOrReplaceChild(
         "bone123",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone124 = bone240.addOrReplaceChild(
         "bone124",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(1.0F, -7.0F, -8.0F, 15.0F, 17.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -7.0F, -8.0F, 11.0F, 4.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, 1.0F, -8.0F, 11.0F, 9.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(23.0F, -3.0F, -8.0F, 14.0F, 13.0F, 18.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -7.0F, -4.0F, 17.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(18.0F, -7.0F, 10.0F, 7.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -11.0F, -8.0F, 18.0F, 21.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -12.0F, 15.0F, 17.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(14.0F, -11.0F, -5.0F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(39.0F, 55.0F, -65.0F, -0.0436F, 0.0F, 1.5708F)
      );
      PartDefinition bone125 = bone124.addOrReplaceChild(
         "bone125",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -10.0F, -8.0F, 8.0F, 23.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -10.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(4.0F, -10.0F, -5.0F, 6.0F, 23.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(4.0F, -6.0F, -8.0F, 6.0F, 15.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -3.0F, -8.0F, 6.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -6.0F, 6.0F, 10.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(12.0F, -6.0F, 2.0F, 15.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(7.0F, -2.0F, -5.0F, 8.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(8.0F, -6.0F, -5.0F, 19.0F, 19.0F, 7.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(20.0F, 13.0F, -1.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(31.0F, -6.0F, -4.0F)
      );
      PartDefinition bone126 = bone125.addOrReplaceChild(
         "bone126",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -12.0F, -6.0F, 6.0F, 15.0F, 15.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 3.0F, -6.0F, 6.0F, 4.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 7.0F, -6.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -6.0F, 6.0F, 15.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -12.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(27.0F, -1.0F, 1.0F)
      );
      PartDefinition bone127 = bone126.addOrReplaceChild(
         "bone127",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 4.0F, -5.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -5.0F, 6.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -5.0F, 10.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -7.0F, 6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -7.0F, -9.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, 3.0F, -1.0F)
      );
      PartDefinition bone128 = bone127.addOrReplaceChild(
         "bone128",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -2.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -2.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, -1.0F, -3.0F)
      );
      PartDefinition bone129 = bone128.addOrReplaceChild(
         "bone129",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -1.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -6.0F, -1.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -3.0F, 7.0F, 6.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -5.0F, 10.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -2.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -1.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone130 = bone129.addOrReplaceChild(
         "bone130",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, 0.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, 0.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -8.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(8.0F, 1.0F, -1.0F)
      );
      PartDefinition bone131 = bone130.addOrReplaceChild(
         "bone131",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -4.0F, 6.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(6.0F, -8.0F, -8.0F, 6.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 4.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(6.0F, -8.0F, -12.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, -12.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(2.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 5.0F, 0.0F)
      );
      PartDefinition bone132 = bone131.addOrReplaceChild(
         "bone132",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(12.0F, -2.0F, 0.0F)
      );
      PartDefinition bone133 = bone132.addOrReplaceChild(
         "bone133",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -6.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 2.0F)
      );
      PartDefinition bone134 = bone133.addOrReplaceChild(
         "bone134",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -5.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone135 = bone134.addOrReplaceChild(
         "bone135",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, -5.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -10.0F, -9.0F, 6.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 3.0F, 0.0F)
      );
      PartDefinition bone136 = bone135.addOrReplaceChild(
         "bone136",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -12.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 2.0F, 3.0F)
      );
      PartDefinition bone137 = bone136.addOrReplaceChild(
         "bone137",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 5.0F, -8.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -11.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone138 = bone137.addOrReplaceChild(
         "bone138",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -10.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone139 = bone138.addOrReplaceChild(
         "bone139",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -5.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, -8.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -9.0F, -4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone140 = bone139.addOrReplaceChild(
         "bone140",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 7.0F, 0.0F)
      );
      PartDefinition bone141 = bone140.addOrReplaceChild(
         "bone141",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, 4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -3.0F, 0.0F)
      );
      PartDefinition bone142 = bone141.addOrReplaceChild(
         "bone142",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, -5.1248F, -6.9982F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, 2.8752F, -2.9982F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0747F, -1.1248F, 5.0018F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, -1.0F)
      );
      PartDefinition bone143 = bone142.addOrReplaceChild(
         "bone143",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -7.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 5.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(3.9253F, 0.8752F, 0.0018F)
      );
      PartDefinition bone144 = bone143.addOrReplaceChild(
         "bone144",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 6.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone145 = bone144.addOrReplaceChild(
         "bone145",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -6.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 2.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone146 = bone145.addOrReplaceChild(
         "bone146",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 4.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -8.0F, -6.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -5.0F, 0.0F)
      );
      PartDefinition bone147 = bone146.addOrReplaceChild(
         "bone147",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 3.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -1.0F, -6.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -5.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -9.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone148 = bone147.addOrReplaceChild(
         "bone148",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone149 = bone148.addOrReplaceChild(
         "bone149",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone150 = bone149.addOrReplaceChild(
         "bone150",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -5.0F, -2.0F)
      );
      PartDefinition bone151 = bone150.addOrReplaceChild(
         "bone151",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 1.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone152 = bone151.addOrReplaceChild(
         "bone152",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone153 = bone152.addOrReplaceChild(
         "bone153",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone154 = bone153.addOrReplaceChild(
         "bone154",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 8.0F, 4.0F)
      );
      PartDefinition bone155 = bone154.addOrReplaceChild(
         "bone155",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone156 = bone155.addOrReplaceChild(
         "bone156",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -6.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone157 = bone156.addOrReplaceChild(
         "bone157",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone158 = bone157.addOrReplaceChild(
         "bone158",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone159 = bone158.addOrReplaceChild(
         "bone159",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone160 = bone159.addOrReplaceChild(
         "bone160",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone161 = bone160.addOrReplaceChild(
         "bone161",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 3.0F, 0.0F)
      );
      PartDefinition bone162 = bone161.addOrReplaceChild(
         "bone162",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 2.0F)
      );
      PartDefinition bone163 = bone162.addOrReplaceChild(
         "bone163",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone164 = bone163.addOrReplaceChild(
         "bone164",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone165 = bone164.addOrReplaceChild(
         "bone165",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone166 = bone165.addOrReplaceChild(
         "bone166",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone167 = bone166.addOrReplaceChild(
         "bone167",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone168 = bone167.addOrReplaceChild(
         "bone168",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -4.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone169 = bone168.addOrReplaceChild(
         "bone169",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, -3.0F, -1.0F)
      );
      PartDefinition bone170 = bone169.addOrReplaceChild(
         "bone170",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone171 = bone170.addOrReplaceChild(
         "bone171",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone172 = bone171.addOrReplaceChild(
         "bone172",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone173 = bone172.addOrReplaceChild(
         "bone173",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone174 = bone173.addOrReplaceChild(
         "bone174",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone175 = bone174.addOrReplaceChild(
         "bone175",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone176 = bone175.addOrReplaceChild(
         "bone176",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone177 = bone176.addOrReplaceChild(
         "bone177",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(367, 421)
            .mirror()
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(2.0F, 2.0F, 0.0F)
      );
      PartDefinition bone178 = bone177.addOrReplaceChild(
         "bone178",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, 1.0F, 0.0F)
      );
      PartDefinition bone179 = bone178.addOrReplaceChild(
         "bone179",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone180 = bone179.addOrReplaceChild(
         "bone180",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone182 = bone180.addOrReplaceChild(
         "bone182",
         CubeListBuilder.create().texOffs(367, 421).mirror().addBox(-2.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone183 = bone240.addOrReplaceChild(
         "bone183",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-16.0F, -7.0F, -8.0F, 15.0F, 17.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, -7.0F, -8.0F, 11.0F, 4.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, 1.0F, -8.0F, 11.0F, 9.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-37.0F, -3.0F, -8.0F, 14.0F, 13.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-31.0F, -7.0F, -4.0F, 17.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-25.0F, -7.0F, 10.0F, 7.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-16.0F, -11.0F, -8.0F, 18.0F, 21.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-13.0F, -7.0F, -12.0F, 15.0F, 17.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-20.0F, -11.0F, -5.0F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(84.0F, 45.0F, -65.0F, -0.0436F, 0.0F, -1.5708F)
      );
      PartDefinition bone184 = bone183.addOrReplaceChild(
         "bone184",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-6.0F, -10.0F, -8.0F, 8.0F, 23.0F, 14.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-14.0F, -10.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-10.0F, -10.0F, -5.0F, 6.0F, 23.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-10.0F, -6.0F, -8.0F, 6.0F, 15.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-14.0F, -3.0F, -8.0F, 6.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-18.0F, -6.0F, 6.0F, 10.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-27.0F, -6.0F, 2.0F, 15.0F, 19.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-15.0F, -2.0F, -5.0F, 8.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-27.0F, -6.0F, -5.0F, 19.0F, 19.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-27.0F, 13.0F, -1.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-31.0F, -6.0F, -4.0F)
      );
      PartDefinition bone185 = bone184.addOrReplaceChild(
         "bone185",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -12.0F, -6.0F, 6.0F, 15.0F, 15.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 3.0F, -6.0F, 6.0F, 4.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, 7.0F, -6.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -8.0F, -6.0F, 6.0F, 15.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -12.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-27.0F, -1.0F, 1.0F)
      );
      PartDefinition bone186 = bone185.addOrReplaceChild(
         "bone186",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-8.0F, 4.0F, -5.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, -5.0F, 6.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -7.0F, -5.0F, 10.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -7.0F, 6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -7.0F, -9.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-8.0F, 3.0F, -1.0F)
      );
      PartDefinition bone187 = bone186.addOrReplaceChild(
         "bone187",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -2.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -6.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 5.0F, -2.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-8.0F, -1.0F, -3.0F)
      );
      PartDefinition bone188 = bone187.addOrReplaceChild(
         "bone188",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -1.0F, 6.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -6.0F, -1.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -3.0F, 7.0F, 6.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -6.0F, -5.0F, 10.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -6.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -2.0F, -9.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, 5.0F, -1.0F, 10.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone189 = bone188.addOrReplaceChild(
         "bone189",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, 0.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, 0.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -8.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-8.0F, 1.0F, -1.0F)
      );
      PartDefinition bone190 = bone189.addOrReplaceChild(
         "bone190",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, -4.0F, 6.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-12.0F, -8.0F, -8.0F, 6.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -8.0F, -8.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, 4.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, 1.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-12.0F, -8.0F, -12.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -8.0F, -12.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-8.0F, -8.0F, 0.0F, 6.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 5.0F, 0.0F)
      );
      PartDefinition bone191 = bone190.addOrReplaceChild(
         "bone191",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-12.0F, -2.0F, 0.0F)
      );
      PartDefinition bone192 = bone191.addOrReplaceChild(
         "bone192",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -6.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 2.0F)
      );
      PartDefinition bone193 = bone192.addOrReplaceChild(
         "bone193",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -5.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone194 = bone193.addOrReplaceChild(
         "bone194",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -5.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 2.0F, -5.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -10.0F, -9.0F, 6.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 3.0F, 0.0F)
      );
      PartDefinition bone195 = bone194.addOrReplaceChild(
         "bone195",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -12.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 2.0F, 3.0F)
      );
      PartDefinition bone196 = bone195.addOrReplaceChild(
         "bone196",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 1.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 5.0F, -8.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -8.0F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -11.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, 4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone197 = bone196.addOrReplaceChild(
         "bone197",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -8.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -10.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone198 = bone197.addOrReplaceChild(
         "bone198",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -5.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -1.0F, -8.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -9.0F, -4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -1.0F, 4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone199 = bone198.addOrReplaceChild(
         "bone199",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 0.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 7.0F, 0.0F)
      );
      PartDefinition bone200 = bone199.addOrReplaceChild(
         "bone200",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 2.0F, 4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -3.0F, 0.0F)
      );
      PartDefinition bone201 = bone200.addOrReplaceChild(
         "bone201",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-3.9253F, -5.1248F, -6.9982F, 6.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-3.9253F, 2.8752F, -2.9982F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-3.9253F, -1.1248F, 5.0018F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, -1.0F)
      );
      PartDefinition bone202 = bone201.addOrReplaceChild(
         "bone202",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -7.0F, 6.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 2.0F, -3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 5.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-3.9253F, 0.8752F, 0.0018F)
      );
      PartDefinition bone203 = bone202.addOrReplaceChild(
         "bone203",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -6.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 6.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, -1.0F)
      );
      PartDefinition bone204 = bone203.addOrReplaceChild(
         "bone204",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -6.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 2.0F, 2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -2.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone205 = bone204.addOrReplaceChild(
         "bone205",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 4.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -8.0F, -6.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -5.0F, 0.0F)
      );
      PartDefinition bone206 = bone205.addOrReplaceChild(
         "bone206",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 3.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -1.0F, -6.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -5.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -9.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone207 = bone206.addOrReplaceChild(
         "bone207",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone208 = bone207.addOrReplaceChild(
         "bone208",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -6.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -7.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone209 = bone208.addOrReplaceChild(
         "bone209",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -5.0F, -2.0F)
      );
      PartDefinition bone210 = bone209.addOrReplaceChild(
         "bone210",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -3.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 1.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone211 = bone210.addOrReplaceChild(
         "bone211",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, 0.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone212 = bone211.addOrReplaceChild(
         "bone212",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 0.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone213 = bone212.addOrReplaceChild(
         "bone213",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, 0.0F, -8.0F, 6.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 8.0F, 4.0F)
      );
      PartDefinition bone214 = bone213.addOrReplaceChild(
         "bone214",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -2.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone215 = bone214.addOrReplaceChild(
         "bone215",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, -4.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -6.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 0.0F, 0.0F)
      );
      PartDefinition bone216 = bone215.addOrReplaceChild(
         "bone216",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, -4.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-4.0F, -4.0F, 0.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -2.0F, 0.0F)
      );
      PartDefinition bone217 = bone216.addOrReplaceChild(
         "bone217",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, 1.0F, 0.0F)
      );
      PartDefinition bone218 = bone217.addOrReplaceChild(
         "bone218",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone219 = bone218.addOrReplaceChild(
         "bone219",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone220 = bone219.addOrReplaceChild(
         "bone220",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 3.0F, 0.0F)
      );
      PartDefinition bone221 = bone220.addOrReplaceChild(
         "bone221",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 2.0F)
      );
      PartDefinition bone222 = bone221.addOrReplaceChild(
         "bone222",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone223 = bone222.addOrReplaceChild(
         "bone223",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone224 = bone223.addOrReplaceChild(
         "bone224",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone225 = bone224.addOrReplaceChild(
         "bone225",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone226 = bone225.addOrReplaceChild(
         "bone226",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone227 = bone226.addOrReplaceChild(
         "bone227",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -4.0F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone228 = bone227.addOrReplaceChild(
         "bone228",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, 1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -3.0F, -1.0F)
      );
      PartDefinition bone229 = bone228.addOrReplaceChild(
         "bone229",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, 1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone230 = bone229.addOrReplaceChild(
         "bone230",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, -1.0F)
      );
      PartDefinition bone231 = bone230.addOrReplaceChild(
         "bone231",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone232 = bone231.addOrReplaceChild(
         "bone232",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone233 = bone232.addOrReplaceChild(
         "bone233",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone234 = bone233.addOrReplaceChild(
         "bone234",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone235 = bone234.addOrReplaceChild(
         "bone235",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition bone236 = bone235.addOrReplaceChild(
         "bone236",
         CubeListBuilder.create()
            .texOffs(367, 421)
            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(367, 421)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 2.0F, 0.0F)
      );
      PartDefinition bone237 = bone236.addOrReplaceChild(
         "bone237",
         CubeListBuilder.create().texOffs(367, 421).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 1.0F, 0.0F)
      );
      PartDefinition bone238 = bone237.addOrReplaceChild(
         "bone238",
         CubeListBuilder.create().texOffs(367, 421).addBox(-4.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.0F, 1.0F)
      );
      PartDefinition bone239 = bone238.addOrReplaceChild(
         "bone239",
         CubeListBuilder.create().texOffs(367, 421).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-4.0F, -1.0F, 0.0F)
      );
      PartDefinition bone299 = bone239.addOrReplaceChild(
         "bone299",
         CubeListBuilder.create().texOffs(367, 421).addBox(-10.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -1.0F, 0.0F)
      );
      PartDefinition bone300 = bone.addOrReplaceChild("bone300", CubeListBuilder.create(), PartPose.offset(0.0F, -83.0F, -7.0F));
      PartDefinition bone301 = bone.addOrReplaceChild("bone301", CubeListBuilder.create(), PartPose.offset(28.0F, 39.0F, -7.0F));
      PartDefinition DebrisRing = root.addOrReplaceChild("DebrisRing", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      return LayerDefinition.create(mesh, 512, 512);
   }

   public void setupAnim(WitherStormRenderState state) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      if (state.playingSpawnAnimation) {
         this.spawnAnimation.apply((long)(state.spawnElapsedTicks * 50.0F), 1.0F);
      } else {
         this.idleAnimation.apply((long)(state.idleTimeTicks * 50.0F), 1.0F);
      }

      if (DabyWSClientConfig.tentaclePhysics) {
         TentaclePhysics.apply(this.root, state);
      }

      if (state.phase < 5.0) {
         SnatchGrab.apply(this.root, state);
      }

      if (state.phase >= 5.0 && state.phase5ElapsedTicks >= 0.0F) {
         float progress = Mth.clamp(state.phase5ElapsedTicks / 80.0F, 0.0F, 1.0F);
         TentaclePhysics.curlAndVanish(this.root, progress);
      }
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.root.render(poseStack, buffer, packedLight, packedOverlay);
   }
}
