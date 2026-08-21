package net.dabicco.witherstormmod.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.dabicco.witherstormmod.entity.state.SeveredWitherStormRenderState;
import net.dabicco.witherstormmod.mixin.ModelPartAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

public class SeveredWitherStorm extends EntityModel<SeveredWitherStormRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "witherstorm_severedleft"), "main"
   );
   private final ModelPart Severed;
   private final ModelPart Tentacle_7;
   private final ModelPart part_01_33;
   private final ModelPart part_01_34;
   private final ModelPart part_01_35;
   private final ModelPart part_01_36;
   private final ModelPart part_01_37;
   private final ModelPart part_01_57;
   private final ModelPart part_01_58;
   private final ModelPart part_01_59;
   private final ModelPart part_01_60;
   private final ModelPart Tentacle_8;
   private final ModelPart part_01_61;
   private final ModelPart part_01_62;
   private final ModelPart part_01_63;
   private final ModelPart part_01_64;
   private final ModelPart part_01_65;
   private final ModelPart part_01_66;
   private final ModelPart part_01_67;
   private final ModelPart part_01_68;
   private final ModelPart part_01_69;
   private final ModelPart Tentacle_9;
   private final ModelPart part_01_70;
   private final ModelPart part_01_71;
   private final ModelPart part_01_81;
   private final ModelPart part_01_82;
   private final ModelPart part_01_83;
   private final ModelPart part_01_84;
   private final ModelPart part_01_85;
   private final ModelPart part_01_86;
   private final ModelPart part_01_87;
   private final ModelPart Tentacle_10;
   private final ModelPart part_01_88;
   private final ModelPart part_01_89;
   private final ModelPart part_01_90;
   private final ModelPart part_01_91;
   private final ModelPart part_01_92;
   private final ModelPart part_01_93;
   private final ModelPart Tentacle_11;
   private final ModelPart part_01_94;
   private final ModelPart part_01_95;
   private final ModelPart part_01_96;
   private final ModelPart part_01_97;
   private final ModelPart part_01_98;
   private final ModelPart part_01_99;
   private final ModelPart part_01_100;
   private final ModelPart part_01_101;
   private final ModelPart part_01_102;
   private final ModelPart part_01_103;
   private final ModelPart part_01_104;
   private final ModelPart part_01_105;
   private final ModelPart part_01_106;
   private final ModelPart part_01_107;
   private final ModelPart part_01_108;
   private final ModelPart part_01_109;
   private final ModelPart Tentacle_12;
   private final ModelPart part_01_110;
   private final ModelPart part_01_111;
   private final ModelPart part_01_112;
   private final ModelPart part_01_113;
   private final ModelPart part_01_114;
   private final ModelPart part_01_115;
   private final ModelPart part_01_116;
   private final ModelPart part_01_117;
   private final ModelPart part_01_118;
   private final ModelPart part_01_119;
   private final ModelPart part_01_120;
   private final List<List<ModelPart>> chains = new ArrayList<>();

   public SeveredWitherStorm(ModelPart root) {
      super(root);
      this.Severed = root.getChild("Severed");
      this.Tentacle_7 = this.Severed.getChild("Tentacle_7");
      this.part_01_33 = this.Tentacle_7.getChild("part_01_33");
      this.part_01_34 = this.part_01_33.getChild("part_01_34");
      this.part_01_35 = this.part_01_34.getChild("part_01_35");
      this.part_01_36 = this.part_01_35.getChild("part_01_36");
      this.part_01_37 = this.part_01_36.getChild("part_01_37");
      this.part_01_57 = this.part_01_37.getChild("part_01_57");
      this.part_01_58 = this.part_01_57.getChild("part_01_58");
      this.part_01_59 = this.part_01_58.getChild("part_01_59");
      this.part_01_60 = this.part_01_59.getChild("part_01_60");
      this.Tentacle_8 = this.Severed.getChild("Tentacle_8");
      this.part_01_61 = this.Tentacle_8.getChild("part_01_61");
      this.part_01_62 = this.part_01_61.getChild("part_01_62");
      this.part_01_63 = this.part_01_62.getChild("part_01_63");
      this.part_01_64 = this.part_01_63.getChild("part_01_64");
      this.part_01_65 = this.part_01_64.getChild("part_01_65");
      this.part_01_66 = this.part_01_65.getChild("part_01_66");
      this.part_01_67 = this.part_01_66.getChild("part_01_67");
      this.part_01_68 = this.part_01_67.getChild("part_01_68");
      this.part_01_69 = this.part_01_68.getChild("part_01_69");
      this.Tentacle_9 = this.Severed.getChild("Tentacle_9");
      this.part_01_70 = this.Tentacle_9.getChild("part_01_70");
      this.part_01_71 = this.part_01_70.getChild("part_01_71");
      this.part_01_81 = this.part_01_71.getChild("part_01_81");
      this.part_01_82 = this.part_01_81.getChild("part_01_82");
      this.part_01_83 = this.part_01_82.getChild("part_01_83");
      this.part_01_84 = this.part_01_83.getChild("part_01_84");
      this.part_01_85 = this.part_01_84.getChild("part_01_85");
      this.part_01_86 = this.part_01_85.getChild("part_01_86");
      this.part_01_87 = this.part_01_86.getChild("part_01_87");
      this.Tentacle_10 = this.Severed.getChild("Tentacle_10");
      this.part_01_88 = this.Tentacle_10.getChild("part_01_88");
      this.part_01_89 = this.part_01_88.getChild("part_01_89");
      this.part_01_90 = this.part_01_89.getChild("part_01_90");
      this.part_01_91 = this.part_01_90.getChild("part_01_91");
      this.part_01_92 = this.part_01_91.getChild("part_01_92");
      this.part_01_93 = this.part_01_92.getChild("part_01_93");
      this.Tentacle_11 = this.Severed.getChild("Tentacle_11");
      this.part_01_94 = this.Tentacle_11.getChild("part_01_94");
      this.part_01_95 = this.part_01_94.getChild("part_01_95");
      this.part_01_96 = this.part_01_95.getChild("part_01_96");
      this.part_01_97 = this.part_01_96.getChild("part_01_97");
      this.part_01_98 = this.part_01_97.getChild("part_01_98");
      this.part_01_99 = this.part_01_98.getChild("part_01_99");
      this.part_01_100 = this.part_01_99.getChild("part_01_100");
      this.part_01_101 = this.part_01_100.getChild("part_01_101");
      this.part_01_102 = this.part_01_101.getChild("part_01_102");
      this.part_01_103 = this.part_01_102.getChild("part_01_103");
      this.part_01_104 = this.part_01_103.getChild("part_01_104");
      this.part_01_105 = this.part_01_104.getChild("part_01_105");
      this.part_01_106 = this.part_01_105.getChild("part_01_106");
      this.part_01_107 = this.part_01_106.getChild("part_01_107");
      this.part_01_108 = this.part_01_107.getChild("part_01_108");
      this.part_01_109 = this.part_01_108.getChild("part_01_109");
      this.Tentacle_12 = this.Severed.getChild("Tentacle_12");
      this.part_01_110 = this.Tentacle_12.getChild("part_01_110");
      this.part_01_111 = this.part_01_110.getChild("part_01_111");
      this.part_01_112 = this.part_01_111.getChild("part_01_112");
      this.part_01_113 = this.part_01_112.getChild("part_01_113");
      this.part_01_114 = this.part_01_113.getChild("part_01_114");
      this.part_01_115 = this.part_01_114.getChild("part_01_115");
      this.part_01_116 = this.part_01_115.getChild("part_01_116");
      this.part_01_117 = this.part_01_116.getChild("part_01_117");
      this.part_01_118 = this.part_01_117.getChild("part_01_118");
      this.part_01_119 = this.part_01_118.getChild("part_01_119");
      this.part_01_120 = this.part_01_119.getChild("part_01_120");
   }

   public static LayerDefinition createBodyLayer() {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      //
      // Bytecode:
      // 0000: new net/minecraft/client/model/geom/builders/MeshDefinition
      // 0003: dup
      // 0004: invokespecial net/minecraft/client/model/geom/builders/MeshDefinition.<init> ()V
      // 0007: astore 0
      // 0008: aload 0
      // 0009: invokevirtual net/minecraft/client/model/geom/builders/MeshDefinition.getRoot ()Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 000c: astore 1
      // 000d: aload 1
      // 000e: ldc "Severed"
      // 0010: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0013: bipush 0
      // 0014: sipush 128
      // 0017: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 001a: ldc_w -8.0
      // 001d: ldc_w -21.0
      // 0020: ldc_w -47.0
      // 0023: ldc_w 16.0
      // 0026: ldc_w 16.0
      // 0029: ldc_w 16.0
      // 002c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 002f: dup
      // 0030: fconst_0
      // 0031: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0034: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0037: bipush 0
      // 0038: sipush 128
      // 003b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 003e: ldc_w -8.0
      // 0041: ldc_w -5.0
      // 0044: ldc_w -63.0
      // 0047: ldc_w 16.0
      // 004a: ldc_w 16.0
      // 004d: ldc_w 16.0
      // 0050: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0053: dup
      // 0054: fconst_0
      // 0055: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0058: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 005b: bipush 0
      // 005c: sipush 128
      // 005f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0062: ldc_w -8.0
      // 0065: ldc_w 11.0
      // 0068: ldc_w -47.0
      // 006b: ldc_w 16.0
      // 006e: ldc_w 16.0
      // 0071: ldc_w 16.0
      // 0074: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0077: dup
      // 0078: fconst_0
      // 0079: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 007c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 007f: bipush 0
      // 0080: sipush 128
      // 0083: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0086: ldc_w -8.0
      // 0089: ldc_w 27.0
      // 008c: ldc_w -31.0
      // 008f: ldc_w 16.0
      // 0092: ldc_w 16.0
      // 0095: ldc_w 16.0
      // 0098: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 009b: dup
      // 009c: fconst_0
      // 009d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 00a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00a3: bipush 0
      // 00a4: sipush 128
      // 00a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00aa: ldc_w -8.0
      // 00ad: ldc_w 27.0
      // 00b0: ldc_w -15.0
      // 00b3: ldc_w 16.0
      // 00b6: ldc_w 16.0
      // 00b9: ldc_w 16.0
      // 00bc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 00bf: dup
      // 00c0: fconst_0
      // 00c1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 00c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00c7: bipush 0
      // 00c8: sipush 128
      // 00cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00ce: ldc_w -8.0
      // 00d1: ldc_w 27.0
      // 00d4: fconst_1
      // 00d5: ldc_w 16.0
      // 00d8: ldc_w 16.0
      // 00db: ldc_w 16.0
      // 00de: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 00e1: dup
      // 00e2: fconst_0
      // 00e3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 00e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00e9: bipush 0
      // 00ea: sipush 128
      // 00ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 00f0: ldc_w 8.0
      // 00f3: ldc_w 27.0
      // 00f6: fconst_1
      // 00f7: ldc_w 16.0
      // 00fa: ldc_w 16.0
      // 00fd: ldc_w 16.0
      // 0100: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0103: dup
      // 0104: fconst_0
      // 0105: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0108: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 010b: bipush 0
      // 010c: sipush 128
      // 010f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0112: ldc_w 8.0
      // 0115: ldc_w 27.0
      // 0118: ldc_w -15.0
      // 011b: ldc_w 16.0
      // 011e: ldc_w 16.0
      // 0121: ldc_w 16.0
      // 0124: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0127: dup
      // 0128: fconst_0
      // 0129: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 012c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 012f: bipush 0
      // 0130: sipush 128
      // 0133: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0136: ldc_w 8.0
      // 0139: ldc_w 27.0
      // 013c: ldc_w -31.0
      // 013f: ldc_w 16.0
      // 0142: ldc_w 16.0
      // 0145: ldc_w 16.0
      // 0148: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 014b: dup
      // 014c: fconst_0
      // 014d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0150: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0153: bipush 0
      // 0154: sipush 128
      // 0157: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 015a: ldc_w 8.0
      // 015d: ldc_w 27.0
      // 0160: ldc_w 17.0
      // 0163: ldc_w 16.0
      // 0166: ldc_w 16.0
      // 0169: ldc_w 16.0
      // 016c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 016f: dup
      // 0170: fconst_0
      // 0171: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0174: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0177: bipush 0
      // 0178: sipush 128
      // 017b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 017e: ldc_w 8.0
      // 0181: ldc_w 11.0
      // 0184: ldc_w 17.0
      // 0187: ldc_w 16.0
      // 018a: ldc_w 16.0
      // 018d: ldc_w 16.0
      // 0190: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0193: dup
      // 0194: fconst_0
      // 0195: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0198: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 019b: bipush 0
      // 019c: sipush 128
      // 019f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01a2: ldc_w 24.0
      // 01a5: ldc_w 27.0
      // 01a8: ldc_w 17.0
      // 01ab: ldc_w 16.0
      // 01ae: ldc_w 16.0
      // 01b1: ldc_w 16.0
      // 01b4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01b7: dup
      // 01b8: fconst_0
      // 01b9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 01bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01bf: bipush 0
      // 01c0: sipush 128
      // 01c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01c6: ldc_w 24.0
      // 01c9: ldc_w 11.0
      // 01cc: ldc_w 17.0
      // 01cf: ldc_w 16.0
      // 01d2: ldc_w 16.0
      // 01d5: ldc_w 16.0
      // 01d8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01db: dup
      // 01dc: fconst_0
      // 01dd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 01e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01e3: bipush 0
      // 01e4: sipush 128
      // 01e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 01ea: ldc_w 24.0
      // 01ed: ldc_w 11.0
      // 01f0: ldc_w 33.0
      // 01f3: ldc_w 16.0
      // 01f6: ldc_w 16.0
      // 01f9: ldc_w 16.0
      // 01fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 01ff: dup
      // 0200: fconst_0
      // 0201: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0204: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0207: bipush 0
      // 0208: sipush 128
      // 020b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 020e: ldc_w 8.0
      // 0211: ldc_w -5.0
      // 0214: ldc_w 33.0
      // 0217: ldc_w 16.0
      // 021a: ldc_w 16.0
      // 021d: ldc_w 16.0
      // 0220: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0223: dup
      // 0224: fconst_0
      // 0225: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0228: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 022b: bipush 0
      // 022c: sipush 128
      // 022f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0232: ldc_w 24.0
      // 0235: ldc_w -5.0
      // 0238: ldc_w 33.0
      // 023b: ldc_w 16.0
      // 023e: ldc_w 16.0
      // 0241: ldc_w 16.0
      // 0244: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0247: dup
      // 0248: fconst_0
      // 0249: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 024c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 024f: bipush 0
      // 0250: sipush 128
      // 0253: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0256: ldc_w 24.0
      // 0259: ldc_w 27.0
      // 025c: fconst_1
      // 025d: ldc_w 16.0
      // 0260: ldc_w 16.0
      // 0263: ldc_w 16.0
      // 0266: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0269: dup
      // 026a: fconst_0
      // 026b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 026e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0271: bipush 0
      // 0272: sipush 128
      // 0275: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0278: ldc_w 24.0
      // 027b: ldc_w 27.0
      // 027e: ldc_w -15.0
      // 0281: ldc_w 16.0
      // 0284: ldc_w 16.0
      // 0287: ldc_w 16.0
      // 028a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 028d: dup
      // 028e: fconst_0
      // 028f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0292: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0295: bipush 0
      // 0296: sipush 128
      // 0299: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 029c: ldc_w 24.0
      // 029f: ldc_w 27.0
      // 02a2: ldc_w -31.0
      // 02a5: ldc_w 16.0
      // 02a8: ldc_w 16.0
      // 02ab: ldc_w 16.0
      // 02ae: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02b1: dup
      // 02b2: fconst_0
      // 02b3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02b9: bipush 0
      // 02ba: sipush 128
      // 02bd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02c0: ldc_w 24.0
      // 02c3: ldc_w 27.0
      // 02c6: ldc_w -47.0
      // 02c9: ldc_w 16.0
      // 02cc: ldc_w 16.0
      // 02cf: ldc_w 16.0
      // 02d2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02d5: dup
      // 02d6: fconst_0
      // 02d7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02dd: bipush 0
      // 02de: sipush 128
      // 02e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 02e4: ldc_w 40.0
      // 02e7: ldc_w 27.0
      // 02ea: ldc_w -47.0
      // 02ed: ldc_w 16.0
      // 02f0: ldc_w 16.0
      // 02f3: ldc_w 16.0
      // 02f6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 02f9: dup
      // 02fa: fconst_0
      // 02fb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 02fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0301: bipush 0
      // 0302: sipush 128
      // 0305: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0308: ldc_w 40.0
      // 030b: ldc_w 27.0
      // 030e: ldc_w -31.0
      // 0311: ldc_w 16.0
      // 0314: ldc_w 16.0
      // 0317: ldc_w 16.0
      // 031a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 031d: dup
      // 031e: fconst_0
      // 031f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0322: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0325: bipush 0
      // 0326: sipush 128
      // 0329: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 032c: ldc_w 56.0
      // 032f: ldc_w 27.0
      // 0332: ldc_w -31.0
      // 0335: ldc_w 16.0
      // 0338: ldc_w 16.0
      // 033b: ldc_w 16.0
      // 033e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0341: dup
      // 0342: fconst_0
      // 0343: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0346: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0349: bipush 0
      // 034a: sipush 128
      // 034d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0350: ldc_w 56.0
      // 0353: ldc_w 27.0
      // 0356: ldc_w -15.0
      // 0359: ldc_w 16.0
      // 035c: ldc_w 16.0
      // 035f: ldc_w 16.0
      // 0362: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0365: dup
      // 0366: fconst_0
      // 0367: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 036a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 036d: bipush 0
      // 036e: sipush 128
      // 0371: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0374: ldc_w 72.0
      // 0377: ldc_w 27.0
      // 037a: fconst_1
      // 037b: ldc_w 16.0
      // 037e: ldc_w 16.0
      // 0381: ldc_w 16.0
      // 0384: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0387: dup
      // 0388: fconst_0
      // 0389: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 038c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 038f: bipush 0
      // 0390: sipush 128
      // 0393: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0396: ldc_w 56.0
      // 0399: ldc_w 27.0
      // 039c: fconst_1
      // 039d: ldc_w 16.0
      // 03a0: ldc_w 16.0
      // 03a3: ldc_w 16.0
      // 03a6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03a9: dup
      // 03aa: fconst_0
      // 03ab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03b1: bipush 0
      // 03b2: sipush 128
      // 03b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03b8: ldc_w 40.0
      // 03bb: ldc_w 27.0
      // 03be: ldc_w -15.0
      // 03c1: ldc_w 16.0
      // 03c4: ldc_w 16.0
      // 03c7: ldc_w 16.0
      // 03ca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03cd: dup
      // 03ce: fconst_0
      // 03cf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03d5: bipush 0
      // 03d6: sipush 128
      // 03d9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03dc: ldc_w 24.0
      // 03df: ldc_w 27.0
      // 03e2: ldc_w -63.0
      // 03e5: ldc_w 16.0
      // 03e8: ldc_w 16.0
      // 03eb: ldc_w 16.0
      // 03ee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 03f1: dup
      // 03f2: fconst_0
      // 03f3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 03f6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 03f9: bipush 0
      // 03fa: sipush 128
      // 03fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0400: ldc_w 40.0
      // 0403: ldc_w 27.0
      // 0406: ldc_w -63.0
      // 0409: ldc_w 16.0
      // 040c: ldc_w 16.0
      // 040f: ldc_w 16.0
      // 0412: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0415: dup
      // 0416: fconst_0
      // 0417: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 041a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 041d: bipush 0
      // 041e: sipush 128
      // 0421: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0424: ldc_w 40.0
      // 0427: ldc_w 43.0
      // 042a: ldc_w -63.0
      // 042d: ldc_w 16.0
      // 0430: ldc_w 16.0
      // 0433: ldc_w 16.0
      // 0436: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0439: dup
      // 043a: fconst_0
      // 043b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 043e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0441: bipush 0
      // 0442: sipush 128
      // 0445: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0448: ldc_w 56.0
      // 044b: ldc_w 43.0
      // 044e: ldc_w -63.0
      // 0451: ldc_w 16.0
      // 0454: ldc_w 16.0
      // 0457: ldc_w 16.0
      // 045a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 045d: dup
      // 045e: fconst_0
      // 045f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0462: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0465: bipush 0
      // 0466: sipush 128
      // 0469: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 046c: ldc_w 104.0
      // 046f: ldc_w 43.0
      // 0472: ldc_w -63.0
      // 0475: ldc_w 16.0
      // 0478: ldc_w 16.0
      // 047b: ldc_w 16.0
      // 047e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0481: dup
      // 0482: fconst_0
      // 0483: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0486: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0489: bipush 0
      // 048a: sipush 128
      // 048d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0490: ldc_w 120.0
      // 0493: ldc_w 43.0
      // 0496: ldc_w -63.0
      // 0499: ldc_w 16.0
      // 049c: ldc_w 16.0
      // 049f: ldc_w 16.0
      // 04a2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04a5: dup
      // 04a6: fconst_0
      // 04a7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04ad: bipush 0
      // 04ae: sipush 128
      // 04b1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04b4: ldc_w 136.0
      // 04b7: ldc_w 43.0
      // 04ba: ldc_w -63.0
      // 04bd: ldc_w 16.0
      // 04c0: ldc_w 16.0
      // 04c3: ldc_w 16.0
      // 04c6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04c9: dup
      // 04ca: fconst_0
      // 04cb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04d1: bipush 0
      // 04d2: sipush 128
      // 04d5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04d8: ldc_w 152.0
      // 04db: ldc_w 43.0
      // 04de: ldc_w -63.0
      // 04e1: ldc_w 16.0
      // 04e4: ldc_w 16.0
      // 04e7: ldc_w 16.0
      // 04ea: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 04ed: dup
      // 04ee: fconst_0
      // 04ef: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 04f2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04f5: bipush 0
      // 04f6: sipush 128
      // 04f9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 04fc: ldc_w 168.0
      // 04ff: ldc_w 43.0
      // 0502: ldc_w -63.0
      // 0505: ldc_w 16.0
      // 0508: ldc_w 16.0
      // 050b: ldc_w 16.0
      // 050e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0511: dup
      // 0512: fconst_0
      // 0513: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0516: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0519: bipush 0
      // 051a: sipush 128
      // 051d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0520: ldc_w 184.0
      // 0523: ldc_w 43.0
      // 0526: ldc_w -63.0
      // 0529: ldc_w 16.0
      // 052c: ldc_w 16.0
      // 052f: ldc_w 16.0
      // 0532: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0535: dup
      // 0536: fconst_0
      // 0537: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 053a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 053d: bipush 0
      // 053e: sipush 128
      // 0541: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0544: ldc_w 200.0
      // 0547: ldc_w 43.0
      // 054a: ldc_w -63.0
      // 054d: ldc_w 16.0
      // 0550: ldc_w 16.0
      // 0553: ldc_w 16.0
      // 0556: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0559: dup
      // 055a: fconst_0
      // 055b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 055e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0561: bipush 0
      // 0562: sipush 128
      // 0565: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0568: ldc_w 216.0
      // 056b: ldc_w 43.0
      // 056e: ldc_w -63.0
      // 0571: ldc_w 16.0
      // 0574: ldc_w 16.0
      // 0577: ldc_w 16.0
      // 057a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 057d: dup
      // 057e: fconst_0
      // 057f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0582: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0585: bipush 0
      // 0586: sipush 128
      // 0589: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 058c: ldc_w 232.0
      // 058f: ldc_w 43.0
      // 0592: ldc_w -63.0
      // 0595: ldc_w 16.0
      // 0598: ldc_w 16.0
      // 059b: ldc_w 16.0
      // 059e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 05a1: dup
      // 05a2: fconst_0
      // 05a3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05a6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05a9: bipush 0
      // 05aa: sipush 128
      // 05ad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05b0: ldc_w 216.0
      // 05b3: ldc_w 27.0
      // 05b6: ldc_w -63.0
      // 05b9: ldc_w 16.0
      // 05bc: ldc_w 16.0
      // 05bf: ldc_w 16.0
      // 05c2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 05c5: dup
      // 05c6: fconst_0
      // 05c7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05ca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05cd: bipush 0
      // 05ce: sipush 128
      // 05d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05d4: ldc_w 200.0
      // 05d7: ldc_w 27.0
      // 05da: ldc_w -63.0
      // 05dd: ldc_w 16.0
      // 05e0: ldc_w 16.0
      // 05e3: ldc_w 16.0
      // 05e6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 05e9: dup
      // 05ea: fconst_0
      // 05eb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 05ee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05f1: bipush 0
      // 05f2: sipush 128
      // 05f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 05f8: ldc_w 184.0
      // 05fb: ldc_w 27.0
      // 05fe: ldc_w -64.0
      // 0601: ldc_w 16.0
      // 0604: ldc_w 16.0
      // 0607: ldc_w 16.0
      // 060a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 060d: dup
      // 060e: fconst_0
      // 060f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0612: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0615: bipush 0
      // 0616: sipush 128
      // 0619: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 061c: ldc_w 168.0
      // 061f: ldc_w 27.0
      // 0622: ldc_w -64.0
      // 0625: ldc_w 16.0
      // 0628: ldc_w 16.0
      // 062b: ldc_w 16.0
      // 062e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0631: dup
      // 0632: fconst_0
      // 0633: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0636: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0639: bipush 0
      // 063a: sipush 128
      // 063d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0640: ldc_w 152.0
      // 0643: ldc_w 27.0
      // 0646: ldc_w -64.0
      // 0649: ldc_w 16.0
      // 064c: ldc_w 16.0
      // 064f: ldc_w 16.0
      // 0652: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0655: dup
      // 0656: fconst_0
      // 0657: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 065a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 065d: bipush 0
      // 065e: sipush 128
      // 0661: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0664: ldc_w 200.0
      // 0667: ldc_w 43.0
      // 066a: ldc_w -47.0
      // 066d: ldc_w 16.0
      // 0670: ldc_w 16.0
      // 0673: ldc_w 16.0
      // 0676: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0679: dup
      // 067a: fconst_0
      // 067b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 067e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0681: bipush 0
      // 0682: sipush 128
      // 0685: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0688: ldc_w 200.0
      // 068b: ldc_w 43.0
      // 068e: ldc_w -31.0
      // 0691: ldc_w 16.0
      // 0694: ldc_w 16.0
      // 0697: ldc_w 16.0
      // 069a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 069d: dup
      // 069e: fconst_0
      // 069f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 06a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06a5: bipush 0
      // 06a6: sipush 128
      // 06a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06ac: ldc_w 200.0
      // 06af: ldc_w 43.0
      // 06b2: ldc_w -15.0
      // 06b5: ldc_w 16.0
      // 06b8: ldc_w 16.0
      // 06bb: ldc_w 16.0
      // 06be: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 06c1: dup
      // 06c2: fconst_0
      // 06c3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 06c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06c9: bipush 0
      // 06ca: sipush 128
      // 06cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06d0: ldc_w 200.0
      // 06d3: ldc_w 43.0
      // 06d6: fconst_1
      // 06d7: ldc_w 16.0
      // 06da: ldc_w 16.0
      // 06dd: ldc_w 16.0
      // 06e0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 06e3: dup
      // 06e4: fconst_0
      // 06e5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 06e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06eb: bipush 0
      // 06ec: sipush 128
      // 06ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 06f2: ldc_w 216.0
      // 06f5: ldc_w 43.0
      // 06f8: fconst_1
      // 06f9: ldc_w 16.0
      // 06fc: ldc_w 16.0
      // 06ff: ldc_w 16.0
      // 0702: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0705: dup
      // 0706: fconst_0
      // 0707: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 070a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 070d: bipush 0
      // 070e: sipush 128
      // 0711: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0714: ldc_w 216.0
      // 0717: ldc_w 43.0
      // 071a: ldc_w 17.0
      // 071d: ldc_w 16.0
      // 0720: ldc_w 16.0
      // 0723: ldc_w 16.0
      // 0726: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0729: dup
      // 072a: fconst_0
      // 072b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 072e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0731: bipush 0
      // 0732: sipush 128
      // 0735: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0738: ldc_w 232.0
      // 073b: ldc_w 43.0
      // 073e: ldc_w 17.0
      // 0741: ldc_w 16.0
      // 0744: ldc_w 16.0
      // 0747: ldc_w 16.0
      // 074a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 074d: dup
      // 074e: fconst_0
      // 074f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0752: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0755: bipush 0
      // 0756: sipush 128
      // 0759: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 075c: ldc_w 248.0
      // 075f: ldc_w 43.0
      // 0762: ldc_w 17.0
      // 0765: ldc_w 16.0
      // 0768: ldc_w 16.0
      // 076b: ldc_w 16.0
      // 076e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0771: dup
      // 0772: fconst_0
      // 0773: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0776: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0779: bipush 0
      // 077a: sipush 128
      // 077d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0780: ldc_w 248.0
      // 0783: ldc_w 27.0
      // 0786: ldc_w 17.0
      // 0789: ldc_w 16.0
      // 078c: ldc_w 16.0
      // 078f: ldc_w 16.0
      // 0792: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0795: dup
      // 0796: fconst_0
      // 0797: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 079a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 079d: bipush 0
      // 079e: sipush 128
      // 07a1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07a4: ldc_w 248.0
      // 07a7: ldc_w 11.0
      // 07aa: ldc_w 17.0
      // 07ad: ldc_w 16.0
      // 07b0: ldc_w 16.0
      // 07b3: ldc_w 16.0
      // 07b6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 07b9: dup
      // 07ba: fconst_0
      // 07bb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 07be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07c1: bipush 0
      // 07c2: sipush 128
      // 07c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07c8: ldc_w 248.0
      // 07cb: ldc_w -5.0
      // 07ce: ldc_w 17.0
      // 07d1: ldc_w 16.0
      // 07d4: ldc_w 16.0
      // 07d7: ldc_w 16.0
      // 07da: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 07dd: dup
      // 07de: fconst_0
      // 07df: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 07e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07e5: bipush 0
      // 07e6: sipush 128
      // 07e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 07ec: ldc_w 248.0
      // 07ef: ldc_w -21.0
      // 07f2: ldc_w 17.0
      // 07f5: ldc_w 16.0
      // 07f8: ldc_w 16.0
      // 07fb: ldc_w 16.0
      // 07fe: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0801: dup
      // 0802: fconst_0
      // 0803: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0806: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0809: bipush 0
      // 080a: sipush 128
      // 080d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0810: ldc_w 216.0
      // 0813: ldc_w 11.0
      // 0816: ldc_w 17.0
      // 0819: ldc_w 16.0
      // 081c: ldc_w 16.0
      // 081f: ldc_w 16.0
      // 0822: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0825: dup
      // 0826: fconst_0
      // 0827: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 082a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 082d: bipush 0
      // 082e: sipush 128
      // 0831: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0834: ldc_w 248.0
      // 0837: ldc_w -21.0
      // 083a: ldc_w 33.0
      // 083d: ldc_w 16.0
      // 0840: ldc_w 16.0
      // 0843: ldc_w 16.0
      // 0846: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0849: dup
      // 084a: fconst_0
      // 084b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 084e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0851: bipush 0
      // 0852: sipush 128
      // 0855: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0858: ldc_w 248.0
      // 085b: ldc_w -5.0
      // 085e: ldc_w 33.0
      // 0861: ldc_w 16.0
      // 0864: ldc_w 16.0
      // 0867: ldc_w 16.0
      // 086a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 086d: dup
      // 086e: fconst_0
      // 086f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0872: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0875: bipush 0
      // 0876: sipush 128
      // 0879: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 087c: ldc_w 248.0
      // 087f: ldc_w 11.0
      // 0882: ldc_w 33.0
      // 0885: ldc_w 16.0
      // 0888: ldc_w 16.0
      // 088b: ldc_w 16.0
      // 088e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0891: dup
      // 0892: fconst_0
      // 0893: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0896: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0899: bipush 0
      // 089a: sipush 128
      // 089d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08a0: ldc_w 232.0
      // 08a3: ldc_w 11.0
      // 08a6: ldc_w 33.0
      // 08a9: ldc_w 16.0
      // 08ac: ldc_w 16.0
      // 08af: ldc_w 16.0
      // 08b2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 08b5: dup
      // 08b6: fconst_0
      // 08b7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 08ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08bd: bipush 47
      // 08bf: sipush 128
      // 08c2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08c8: ldc_w 232.0
      // 08cb: ldc_w -5.0
      // 08ce: ldc_w 49.0
      // 08d1: ldc_w 16.0
      // 08d4: ldc_w 16.0
      // 08d7: ldc_w 16.0
      // 08da: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 08dd: dup
      // 08de: fconst_0
      // 08df: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 08e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08e5: bipush 0
      // 08e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08e9: bipush 0
      // 08ea: sipush 128
      // 08ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 08f0: ldc_w 232.0
      // 08f3: ldc_w 27.0
      // 08f6: ldc_w 33.0
      // 08f9: ldc_w 16.0
      // 08fc: ldc_w 16.0
      // 08ff: ldc_w 16.0
      // 0902: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0905: dup
      // 0906: fconst_0
      // 0907: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 090a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 090d: bipush 0
      // 090e: sipush 128
      // 0911: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0914: ldc_w 216.0
      // 0917: ldc_w 27.0
      // 091a: ldc_w 33.0
      // 091d: ldc_w 16.0
      // 0920: ldc_w 16.0
      // 0923: ldc_w 16.0
      // 0926: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0929: dup
      // 092a: fconst_0
      // 092b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 092e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0931: bipush 0
      // 0932: sipush 128
      // 0935: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0938: ldc_w 232.0
      // 093b: ldc_w 43.0
      // 093e: ldc_w 17.0
      // 0941: ldc_w 16.0
      // 0944: ldc_w 16.0
      // 0947: ldc_w 16.0
      // 094a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 094d: dup
      // 094e: fconst_0
      // 094f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0952: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0955: bipush 0
      // 0956: sipush 128
      // 0959: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 095c: ldc_w 88.0
      // 095f: ldc_w 43.0
      // 0962: ldc_w -63.0
      // 0965: ldc_w 16.0
      // 0968: ldc_w 16.0
      // 096b: ldc_w 16.0
      // 096e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0971: dup
      // 0972: fconst_0
      // 0973: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0976: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0979: bipush 0
      // 097a: sipush 128
      // 097d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0980: ldc_w 56.0
      // 0983: ldc_w 43.0
      // 0986: ldc_w -47.0
      // 0989: ldc_w 16.0
      // 098c: ldc_w 16.0
      // 098f: ldc_w 16.0
      // 0992: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0995: dup
      // 0996: fconst_0
      // 0997: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 099a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 099d: bipush 0
      // 099e: sipush 128
      // 09a1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09a4: ldc_w 72.0
      // 09a7: ldc_w 43.0
      // 09aa: ldc_w -47.0
      // 09ad: ldc_w 16.0
      // 09b0: ldc_w 16.0
      // 09b3: ldc_w 16.0
      // 09b6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 09b9: dup
      // 09ba: fconst_0
      // 09bb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 09be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09c1: bipush 0
      // 09c2: sipush 128
      // 09c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09c8: ldc_w 72.0
      // 09cb: ldc_w 43.0
      // 09ce: ldc_w -63.0
      // 09d1: ldc_w 16.0
      // 09d4: ldc_w 16.0
      // 09d7: ldc_w 16.0
      // 09da: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 09dd: dup
      // 09de: fconst_0
      // 09df: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 09e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09e5: bipush 0
      // 09e6: sipush 128
      // 09e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 09ec: ldc_w 72.0
      // 09ef: ldc_w 43.0
      // 09f2: ldc_w -79.0
      // 09f5: ldc_w 16.0
      // 09f8: ldc_w 16.0
      // 09fb: ldc_w 16.0
      // 09fe: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a01: dup
      // 0a02: fconst_0
      // 0a03: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a06: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a09: bipush 0
      // 0a0a: sipush 128
      // 0a0d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a10: ldc_w 88.0
      // 0a13: ldc_w 43.0
      // 0a16: ldc_w -47.0
      // 0a19: ldc_w 16.0
      // 0a1c: ldc_w 16.0
      // 0a1f: ldc_w 16.0
      // 0a22: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a25: dup
      // 0a26: fconst_0
      // 0a27: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a2a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a2d: bipush 0
      // 0a2e: sipush 128
      // 0a31: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a34: ldc_w 104.0
      // 0a37: ldc_w 43.0
      // 0a3a: ldc_w -47.0
      // 0a3d: ldc_w 16.0
      // 0a40: ldc_w 16.0
      // 0a43: ldc_w 16.0
      // 0a46: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a49: dup
      // 0a4a: fconst_0
      // 0a4b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a4e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a51: bipush 0
      // 0a52: sipush 128
      // 0a55: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a58: ldc_w 72.0
      // 0a5b: ldc_w 43.0
      // 0a5e: ldc_w -31.0
      // 0a61: ldc_w 16.0
      // 0a64: ldc_w 16.0
      // 0a67: ldc_w 16.0
      // 0a6a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a6d: dup
      // 0a6e: fconst_0
      // 0a6f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a72: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a75: bipush 0
      // 0a76: sipush 128
      // 0a79: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a7c: ldc_w 88.0
      // 0a7f: ldc_w 43.0
      // 0a82: ldc_w -31.0
      // 0a85: ldc_w 16.0
      // 0a88: ldc_w 16.0
      // 0a8b: ldc_w 16.0
      // 0a8e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0a91: dup
      // 0a92: fconst_0
      // 0a93: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0a96: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0a99: bipush 0
      // 0a9a: sipush 128
      // 0a9d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0aa0: ldc_w 104.0
      // 0aa3: ldc_w 43.0
      // 0aa6: ldc_w -31.0
      // 0aa9: ldc_w 16.0
      // 0aac: ldc_w 16.0
      // 0aaf: ldc_w 16.0
      // 0ab2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ab5: dup
      // 0ab6: fconst_0
      // 0ab7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0aba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0abd: bipush 0
      // 0abe: sipush 128
      // 0ac1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ac4: ldc_w 72.0
      // 0ac7: ldc_w 43.0
      // 0aca: ldc_w -15.0
      // 0acd: ldc_w 16.0
      // 0ad0: ldc_w 16.0
      // 0ad3: ldc_w 16.0
      // 0ad6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ad9: dup
      // 0ada: fconst_0
      // 0adb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ade: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ae1: bipush 0
      // 0ae2: sipush 128
      // 0ae5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ae8: ldc_w 88.0
      // 0aeb: ldc_w 43.0
      // 0aee: ldc_w -15.0
      // 0af1: ldc_w 16.0
      // 0af4: ldc_w 16.0
      // 0af7: ldc_w 16.0
      // 0afa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0afd: dup
      // 0afe: fconst_0
      // 0aff: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b02: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b05: bipush 0
      // 0b06: sipush 128
      // 0b09: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b0c: ldc_w 104.0
      // 0b0f: ldc_w 43.0
      // 0b12: ldc_w -15.0
      // 0b15: ldc_w 16.0
      // 0b18: ldc_w 16.0
      // 0b1b: ldc_w 16.0
      // 0b1e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b21: dup
      // 0b22: fconst_0
      // 0b23: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b26: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b29: bipush 0
      // 0b2a: sipush 128
      // 0b2d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b30: ldc_w 88.0
      // 0b33: ldc_w 43.0
      // 0b36: fconst_1
      // 0b37: ldc_w 16.0
      // 0b3a: ldc_w 16.0
      // 0b3d: ldc_w 16.0
      // 0b40: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b43: dup
      // 0b44: fconst_0
      // 0b45: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b48: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b4b: bipush 0
      // 0b4c: sipush 128
      // 0b4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b52: ldc_w 120.0
      // 0b55: ldc_w 43.0
      // 0b58: ldc_w 17.0
      // 0b5b: ldc_w 16.0
      // 0b5e: ldc_w 16.0
      // 0b61: ldc_w 16.0
      // 0b64: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b67: dup
      // 0b68: fconst_0
      // 0b69: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b6c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b6f: bipush 0
      // 0b70: sipush 128
      // 0b73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b76: ldc_w 184.0
      // 0b79: ldc_w -5.0
      // 0b7c: ldc_w 33.0
      // 0b7f: ldc_w 16.0
      // 0b82: ldc_w 16.0
      // 0b85: ldc_w 16.0
      // 0b88: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0b8b: dup
      // 0b8c: fconst_0
      // 0b8d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0b90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b93: bipush 0
      // 0b94: sipush 128
      // 0b97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0b9a: ldc_w 168.0
      // 0b9d: ldc_w -5.0
      // 0ba0: ldc_w 33.0
      // 0ba3: ldc_w 16.0
      // 0ba6: ldc_w 16.0
      // 0ba9: ldc_w 16.0
      // 0bac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0baf: dup
      // 0bb0: fconst_0
      // 0bb1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bb4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bb7: bipush 0
      // 0bb8: sipush 128
      // 0bbb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bbe: ldc_w 184.0
      // 0bc1: ldc_w -21.0
      // 0bc4: ldc_w 33.0
      // 0bc7: ldc_w 16.0
      // 0bca: ldc_w 16.0
      // 0bcd: ldc_w 16.0
      // 0bd0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0bd3: dup
      // 0bd4: fconst_0
      // 0bd5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bd8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bdb: bipush 0
      // 0bdc: sipush 128
      // 0bdf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0be2: ldc_w 200.0
      // 0be5: ldc_w -5.0
      // 0be8: ldc_w 33.0
      // 0beb: ldc_w 16.0
      // 0bee: ldc_w 16.0
      // 0bf1: ldc_w 16.0
      // 0bf4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0bf7: dup
      // 0bf8: fconst_0
      // 0bf9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0bfc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0bff: bipush 0
      // 0c00: sipush 128
      // 0c03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c06: ldc_w 200.0
      // 0c09: ldc_w -5.0
      // 0c0c: ldc_w 49.0
      // 0c0f: ldc_w 16.0
      // 0c12: ldc_w 16.0
      // 0c15: ldc_w 16.0
      // 0c18: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c1b: dup
      // 0c1c: fconst_0
      // 0c1d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c23: bipush 0
      // 0c24: sipush 128
      // 0c27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c2a: ldc_w 200.0
      // 0c2d: ldc_w -21.0
      // 0c30: ldc_w 49.0
      // 0c33: ldc_w 16.0
      // 0c36: ldc_w 16.0
      // 0c39: ldc_w 16.0
      // 0c3c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c3f: dup
      // 0c40: fconst_0
      // 0c41: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c47: bipush 0
      // 0c48: sipush 128
      // 0c4b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c4e: ldc_w 200.0
      // 0c51: ldc_w -21.0
      // 0c54: ldc_w 33.0
      // 0c57: ldc_w 16.0
      // 0c5a: ldc_w 16.0
      // 0c5d: ldc_w 16.0
      // 0c60: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c63: dup
      // 0c64: fconst_0
      // 0c65: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c68: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c6b: bipush 0
      // 0c6c: sipush 352
      // 0c6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c72: ldc_w 200.0
      // 0c75: ldc_w -37.0
      // 0c78: ldc_w 17.0
      // 0c7b: ldc_w 16.0
      // 0c7e: ldc_w 16.0
      // 0c81: ldc_w 16.0
      // 0c84: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0c87: dup
      // 0c88: fconst_0
      // 0c89: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0c8c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c8f: bipush 0
      // 0c90: sipush 352
      // 0c93: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0c96: ldc_w 200.0
      // 0c99: ldc_w -37.0
      // 0c9c: ldc_w 33.0
      // 0c9f: ldc_w 16.0
      // 0ca2: ldc_w 16.0
      // 0ca5: ldc_w 16.0
      // 0ca8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0cab: dup
      // 0cac: fconst_0
      // 0cad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0cb0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cb3: bipush 0
      // 0cb4: sipush 352
      // 0cb7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cba: ldc_w 184.0
      // 0cbd: ldc_w -37.0
      // 0cc0: ldc_w 33.0
      // 0cc3: ldc_w 16.0
      // 0cc6: ldc_w 16.0
      // 0cc9: ldc_w 16.0
      // 0ccc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ccf: dup
      // 0cd0: fconst_0
      // 0cd1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0cd4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cd7: bipush 0
      // 0cd8: sipush 352
      // 0cdb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cde: ldc_w 168.0
      // 0ce1: ldc_w -37.0
      // 0ce4: ldc_w 33.0
      // 0ce7: ldc_w 16.0
      // 0cea: ldc_w 16.0
      // 0ced: ldc_w 16.0
      // 0cf0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0cf3: dup
      // 0cf4: fconst_0
      // 0cf5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0cf8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0cfb: bipush 0
      // 0cfc: sipush 128
      // 0cff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d02: ldc_w 136.0
      // 0d05: ldc_w 43.0
      // 0d08: fconst_1
      // 0d09: ldc_w 16.0
      // 0d0c: ldc_w 16.0
      // 0d0f: ldc_w 16.0
      // 0d12: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d15: dup
      // 0d16: fconst_0
      // 0d17: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d1a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d1d: bipush 0
      // 0d1e: sipush 128
      // 0d21: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d24: ldc_w 120.0
      // 0d27: ldc_w 43.0
      // 0d2a: fconst_1
      // 0d2b: ldc_w 16.0
      // 0d2e: ldc_w 16.0
      // 0d31: ldc_w 16.0
      // 0d34: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d37: dup
      // 0d38: fconst_0
      // 0d39: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d3f: bipush 0
      // 0d40: sipush 128
      // 0d43: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d46: ldc_w 120.0
      // 0d49: ldc_w 26.0
      // 0d4c: ldc_w -15.0
      // 0d4f: ldc_w 16.0
      // 0d52: ldc_w 16.0
      // 0d55: ldc_w 16.0
      // 0d58: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d5b: dup
      // 0d5c: fconst_0
      // 0d5d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d63: bipush 0
      // 0d64: sipush 128
      // 0d67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d6a: ldc_w 120.0
      // 0d6d: ldc_w 10.0
      // 0d70: ldc_w -15.0
      // 0d73: ldc_w 16.0
      // 0d76: ldc_w 16.0
      // 0d79: ldc_w 16.0
      // 0d7c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0d7f: dup
      // 0d80: fconst_0
      // 0d81: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0d84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d87: bipush 0
      // 0d88: sipush 128
      // 0d8b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0d8e: ldc_w 120.0
      // 0d91: ldc_w -6.0
      // 0d94: ldc_w -15.0
      // 0d97: ldc_w 16.0
      // 0d9a: ldc_w 16.0
      // 0d9d: ldc_w 16.0
      // 0da0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0da3: dup
      // 0da4: fconst_0
      // 0da5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0da8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dab: bipush 0
      // 0dac: sipush 128
      // 0daf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0db2: ldc_w 120.0
      // 0db5: ldc_w 43.0
      // 0db8: ldc_w -15.0
      // 0dbb: ldc_w 16.0
      // 0dbe: ldc_w 16.0
      // 0dc1: ldc_w 16.0
      // 0dc4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0dc7: dup
      // 0dc8: fconst_0
      // 0dc9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0dcc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dcf: bipush 0
      // 0dd0: sipush 128
      // 0dd3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dd6: ldc_w 120.0
      // 0dd9: ldc_w 43.0
      // 0ddc: ldc_w -31.0
      // 0ddf: ldc_w 16.0
      // 0de2: ldc_w 16.0
      // 0de5: ldc_w 16.0
      // 0de8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0deb: dup
      // 0dec: fconst_0
      // 0ded: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0df0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0df3: bipush 0
      // 0df4: sipush 128
      // 0df7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0dfa: ldc_w 136.0
      // 0dfd: ldc_w 43.0
      // 0e00: ldc_w -31.0
      // 0e03: ldc_w 16.0
      // 0e06: ldc_w 16.0
      // 0e09: ldc_w 16.0
      // 0e0c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e0f: dup
      // 0e10: fconst_0
      // 0e11: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e14: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e17: bipush 0
      // 0e18: sipush 128
      // 0e1b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e1e: ldc_w 152.0
      // 0e21: ldc_w 43.0
      // 0e24: ldc_w -31.0
      // 0e27: ldc_w 16.0
      // 0e2a: ldc_w 16.0
      // 0e2d: ldc_w 16.0
      // 0e30: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e33: dup
      // 0e34: fconst_0
      // 0e35: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e3b: bipush 0
      // 0e3c: sipush 128
      // 0e3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e42: ldc_w 168.0
      // 0e45: ldc_w 43.0
      // 0e48: ldc_w -31.0
      // 0e4b: ldc_w 16.0
      // 0e4e: ldc_w 16.0
      // 0e51: ldc_w 16.0
      // 0e54: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e57: dup
      // 0e58: fconst_0
      // 0e59: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e5c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e5f: bipush 0
      // 0e60: sipush 128
      // 0e63: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e66: ldc_w 184.0
      // 0e69: ldc_w 43.0
      // 0e6c: ldc_w -31.0
      // 0e6f: ldc_w 16.0
      // 0e72: ldc_w 16.0
      // 0e75: ldc_w 16.0
      // 0e78: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e7b: dup
      // 0e7c: fconst_0
      // 0e7d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0e80: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e83: bipush 0
      // 0e84: sipush 128
      // 0e87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0e8a: ldc_w 184.0
      // 0e8d: ldc_w 43.0
      // 0e90: ldc_w -47.0
      // 0e93: ldc_w 16.0
      // 0e96: ldc_w 16.0
      // 0e99: ldc_w 16.0
      // 0e9c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0e9f: dup
      // 0ea0: fconst_0
      // 0ea1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ea4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ea7: bipush 0
      // 0ea8: sipush 128
      // 0eab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0eae: ldc_w 168.0
      // 0eb1: ldc_w 43.0
      // 0eb4: ldc_w -47.0
      // 0eb7: ldc_w 16.0
      // 0eba: ldc_w 16.0
      // 0ebd: ldc_w 16.0
      // 0ec0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ec3: dup
      // 0ec4: fconst_0
      // 0ec5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0ec8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ecb: bipush 0
      // 0ecc: sipush 128
      // 0ecf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ed2: ldc_w 152.0
      // 0ed5: ldc_w 43.0
      // 0ed8: ldc_w -47.0
      // 0edb: ldc_w 16.0
      // 0ede: ldc_w 16.0
      // 0ee1: ldc_w 16.0
      // 0ee4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0ee7: dup
      // 0ee8: fconst_0
      // 0ee9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0eec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0eef: bipush 0
      // 0ef0: sipush 128
      // 0ef3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ef6: ldc_w 136.0
      // 0ef9: ldc_w 43.0
      // 0efc: ldc_w -47.0
      // 0eff: ldc_w 16.0
      // 0f02: ldc_w 16.0
      // 0f05: ldc_w 16.0
      // 0f08: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f0b: dup
      // 0f0c: fconst_0
      // 0f0d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f10: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f13: bipush 0
      // 0f14: sipush 128
      // 0f17: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f1a: ldc_w 120.0
      // 0f1d: ldc_w 43.0
      // 0f20: ldc_w -47.0
      // 0f23: ldc_w 16.0
      // 0f26: ldc_w 16.0
      // 0f29: ldc_w 16.0
      // 0f2c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f2f: dup
      // 0f30: fconst_0
      // 0f31: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f34: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f37: bipush 0
      // 0f38: sipush 128
      // 0f3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f3e: ldc_w 136.0
      // 0f41: ldc_w 43.0
      // 0f44: ldc_w -15.0
      // 0f47: ldc_w 16.0
      // 0f4a: ldc_w 16.0
      // 0f4d: ldc_w 16.0
      // 0f50: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f53: dup
      // 0f54: fconst_0
      // 0f55: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f58: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f5b: bipush 0
      // 0f5c: sipush 128
      // 0f5f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f62: ldc_w 136.0
      // 0f65: ldc_w 43.0
      // 0f68: ldc_w 17.0
      // 0f6b: ldc_w 16.0
      // 0f6e: ldc_w 16.0
      // 0f71: ldc_w 16.0
      // 0f74: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f77: dup
      // 0f78: fconst_0
      // 0f79: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0f7c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f7f: bipush 0
      // 0f80: sipush 128
      // 0f83: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0f86: ldc_w 168.0
      // 0f89: ldc_w 27.0
      // 0f8c: ldc_w 17.0
      // 0f8f: ldc_w 16.0
      // 0f92: ldc_w 16.0
      // 0f95: ldc_w 16.0
      // 0f98: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0f9b: dup
      // 0f9c: fconst_0
      // 0f9d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0fa0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fa3: bipush 0
      // 0fa4: sipush 128
      // 0fa7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0faa: ldc_w 168.0
      // 0fad: ldc_w 11.0
      // 0fb0: ldc_w 33.0
      // 0fb3: ldc_w 16.0
      // 0fb6: ldc_w 16.0
      // 0fb9: ldc_w 16.0
      // 0fbc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0fbf: dup
      // 0fc0: fconst_0
      // 0fc1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0fc4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fc7: bipush 0
      // 0fc8: sipush 128
      // 0fcb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0fce: ldc_w 152.0
      // 0fd1: ldc_w 11.0
      // 0fd4: ldc_w 33.0
      // 0fd7: ldc_w 16.0
      // 0fda: ldc_w 16.0
      // 0fdd: ldc_w 16.0
      // 0fe0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 0fe3: dup
      // 0fe4: fconst_0
      // 0fe5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 0fe8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0feb: bipush 0
      // 0fec: sipush 128
      // 0fef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 0ff2: ldc_w 136.0
      // 0ff5: ldc_w 11.0
      // 0ff8: ldc_w 33.0
      // 0ffb: ldc_w 16.0
      // 0ffe: ldc_w 16.0
      // 1001: ldc_w 16.0
      // 1004: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1007: dup
      // 1008: fconst_0
      // 1009: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 100c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 100f: bipush 0
      // 1010: sipush 128
      // 1013: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1016: ldc_w 120.0
      // 1019: ldc_w 11.0
      // 101c: ldc_w 33.0
      // 101f: ldc_w 16.0
      // 1022: ldc_w 16.0
      // 1025: ldc_w 16.0
      // 1028: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 102b: dup
      // 102c: fconst_0
      // 102d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1030: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1033: bipush 0
      // 1034: sipush 128
      // 1037: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 103a: ldc_w 152.0
      // 103d: ldc_w 27.0
      // 1040: ldc_w 33.0
      // 1043: ldc_w 16.0
      // 1046: ldc_w 16.0
      // 1049: ldc_w 16.0
      // 104c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 104f: dup
      // 1050: fconst_0
      // 1051: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1054: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1057: bipush 0
      // 1058: sipush 128
      // 105b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 105e: ldc_w 136.0
      // 1061: ldc_w 27.0
      // 1064: ldc_w 33.0
      // 1067: ldc_w 16.0
      // 106a: ldc_w 16.0
      // 106d: ldc_w 16.0
      // 1070: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1073: dup
      // 1074: fconst_0
      // 1075: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1078: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 107b: bipush 0
      // 107c: sipush 128
      // 107f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1082: ldc_w 120.0
      // 1085: ldc_w 27.0
      // 1088: ldc_w 33.0
      // 108b: ldc_w 16.0
      // 108e: ldc_w 16.0
      // 1091: ldc_w 16.0
      // 1094: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1097: dup
      // 1098: fconst_0
      // 1099: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 109c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 109f: bipush 0
      // 10a0: sipush 128
      // 10a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10a6: ldc_w 104.0
      // 10a9: ldc_w 27.0
      // 10ac: ldc_w 17.0
      // 10af: ldc_w 16.0
      // 10b2: ldc_w 16.0
      // 10b5: ldc_w 16.0
      // 10b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 10bb: dup
      // 10bc: fconst_0
      // 10bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 10c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10c3: bipush 0
      // 10c4: sipush 128
      // 10c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10ca: ldc_w 88.0
      // 10cd: ldc_w 27.0
      // 10d0: ldc_w 33.0
      // 10d3: ldc_w 16.0
      // 10d6: ldc_w 16.0
      // 10d9: ldc_w 16.0
      // 10dc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 10df: dup
      // 10e0: fconst_0
      // 10e1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 10e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10e7: bipush 0
      // 10e8: sipush 128
      // 10eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 10ee: ldc_w 104.0
      // 10f1: ldc_w 11.0
      // 10f4: ldc_w 33.0
      // 10f7: ldc_w 16.0
      // 10fa: ldc_w 16.0
      // 10fd: ldc_w 16.0
      // 1100: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1103: dup
      // 1104: fconst_0
      // 1105: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1108: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 110b: bipush 0
      // 110c: sipush 128
      // 110f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1112: ldc_w 104.0
      // 1115: ldc_w 11.0
      // 1118: ldc_w 81.0
      // 111b: ldc_w 16.0
      // 111e: ldc_w 16.0
      // 1121: ldc_w 16.0
      // 1124: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1127: dup
      // 1128: fconst_0
      // 1129: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 112c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 112f: bipush 0
      // 1130: sipush 128
      // 1133: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1136: ldc_w 120.0
      // 1139: ldc_w 11.0
      // 113c: ldc_w 81.0
      // 113f: ldc_w 16.0
      // 1142: ldc_w 16.0
      // 1145: ldc_w 16.0
      // 1148: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 114b: dup
      // 114c: fconst_0
      // 114d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1150: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1153: bipush 0
      // 1154: sipush 128
      // 1157: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 115a: ldc_w 88.0
      // 115d: ldc_w 11.0
      // 1160: ldc_w 33.0
      // 1163: ldc_w 16.0
      // 1166: ldc_w 16.0
      // 1169: ldc_w 16.0
      // 116c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 116f: dup
      // 1170: fconst_0
      // 1171: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1174: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1177: bipush 0
      // 1178: sipush 128
      // 117b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 117e: ldc_w 88.0
      // 1181: ldc_w -5.0
      // 1184: ldc_w 49.0
      // 1187: ldc_w 16.0
      // 118a: ldc_w 16.0
      // 118d: ldc_w 16.0
      // 1190: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1193: dup
      // 1194: fconst_0
      // 1195: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1198: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 119b: bipush 0
      // 119c: sipush 128
      // 119f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11a2: ldc_w 104.0
      // 11a5: ldc_w -5.0
      // 11a8: ldc_w 49.0
      // 11ab: ldc_w 16.0
      // 11ae: ldc_w 16.0
      // 11b1: ldc_w 16.0
      // 11b4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11b7: dup
      // 11b8: fconst_0
      // 11b9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 11bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11bf: bipush 0
      // 11c0: sipush 128
      // 11c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11c6: ldc_w 104.0
      // 11c9: ldc_w -5.0
      // 11cc: ldc_w 65.0
      // 11cf: ldc_w 16.0
      // 11d2: ldc_w 16.0
      // 11d5: ldc_w 16.0
      // 11d8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11db: dup
      // 11dc: fconst_0
      // 11dd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 11e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11e3: bipush 0
      // 11e4: sipush 128
      // 11e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 11ea: ldc_w 72.0
      // 11ed: ldc_w 11.0
      // 11f0: ldc_w 81.0
      // 11f3: ldc_w 16.0
      // 11f6: ldc_w 16.0
      // 11f9: ldc_w 16.0
      // 11fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 11ff: dup
      // 1200: fconst_0
      // 1201: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1204: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1207: bipush 0
      // 1208: sipush 128
      // 120b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 120e: ldc_w 40.0
      // 1211: ldc_w 11.0
      // 1214: ldc_w 81.0
      // 1217: ldc_w 16.0
      // 121a: ldc_w 16.0
      // 121d: ldc_w 16.0
      // 1220: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1223: dup
      // 1224: fconst_0
      // 1225: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1228: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 122b: bipush 0
      // 122c: sipush 128
      // 122f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1232: ldc_w 56.0
      // 1235: ldc_w 11.0
      // 1238: ldc_w 81.0
      // 123b: ldc_w 16.0
      // 123e: ldc_w 16.0
      // 1241: ldc_w 16.0
      // 1244: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1247: dup
      // 1248: fconst_0
      // 1249: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 124c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 124f: bipush 0
      // 1250: sipush 128
      // 1253: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1256: ldc_w 72.0
      // 1259: ldc_w 11.0
      // 125c: ldc_w 97.0
      // 125f: ldc_w 16.0
      // 1262: ldc_w 16.0
      // 1265: ldc_w 16.0
      // 1268: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 126b: dup
      // 126c: fconst_0
      // 126d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1270: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1273: bipush 0
      // 1274: sipush 128
      // 1277: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 127a: ldc_w 56.0
      // 127d: ldc_w 11.0
      // 1280: ldc_w 97.0
      // 1283: ldc_w 16.0
      // 1286: ldc_w 16.0
      // 1289: ldc_w 16.0
      // 128c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 128f: dup
      // 1290: fconst_0
      // 1291: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1294: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1297: bipush 0
      // 1298: sipush 128
      // 129b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 129e: ldc_w 40.0
      // 12a1: ldc_w 11.0
      // 12a4: ldc_w 97.0
      // 12a7: ldc_w 16.0
      // 12aa: ldc_w 16.0
      // 12ad: ldc_w 16.0
      // 12b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12b3: dup
      // 12b4: fconst_0
      // 12b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 12b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12bb: bipush 0
      // 12bc: sipush 128
      // 12bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12c2: ldc_w 8.0
      // 12c5: ldc_w 11.0
      // 12c8: ldc_w 97.0
      // 12cb: ldc_w 16.0
      // 12ce: ldc_w 16.0
      // 12d1: ldc_w 16.0
      // 12d4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12d7: dup
      // 12d8: fconst_0
      // 12d9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 12dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12df: bipush 0
      // 12e0: sipush 128
      // 12e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 12e6: ldc_w 8.0
      // 12e9: ldc_w 11.0
      // 12ec: ldc_w 81.0
      // 12ef: ldc_w 16.0
      // 12f2: ldc_w 16.0
      // 12f5: ldc_w 16.0
      // 12f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 12fb: dup
      // 12fc: fconst_0
      // 12fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1300: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1303: bipush 0
      // 1304: sipush 128
      // 1307: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 130a: ldc_w -8.0
      // 130d: ldc_w 11.0
      // 1310: ldc_w 81.0
      // 1313: ldc_w 16.0
      // 1316: ldc_w 16.0
      // 1319: ldc_w 16.0
      // 131c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 131f: dup
      // 1320: fconst_0
      // 1321: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1324: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1327: bipush 0
      // 1328: sipush 128
      // 132b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 132e: ldc_w -8.0
      // 1331: ldc_w 11.0
      // 1334: ldc_w 97.0
      // 1337: ldc_w 16.0
      // 133a: ldc_w 16.0
      // 133d: ldc_w 16.0
      // 1340: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1343: dup
      // 1344: fconst_0
      // 1345: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1348: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 134b: bipush 0
      // 134c: sipush 128
      // 134f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1352: ldc_w -8.0
      // 1355: ldc_w -5.0
      // 1358: ldc_w 97.0
      // 135b: ldc_w 16.0
      // 135e: ldc_w 16.0
      // 1361: ldc_w 16.0
      // 1364: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1367: dup
      // 1368: fconst_0
      // 1369: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 136c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 136f: bipush 0
      // 1370: sipush 128
      // 1373: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1376: ldc_w 24.0
      // 1379: ldc_w 11.0
      // 137c: ldc_w 97.0
      // 137f: ldc_w 16.0
      // 1382: ldc_w 16.0
      // 1385: ldc_w 16.0
      // 1388: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 138b: dup
      // 138c: fconst_0
      // 138d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1390: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1393: bipush 0
      // 1394: sipush 128
      // 1397: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 139a: ldc_w 56.0
      // 139d: ldc_w 11.0
      // 13a0: ldc_w 113.0
      // 13a3: ldc_w 16.0
      // 13a6: ldc_w 16.0
      // 13a9: ldc_w 16.0
      // 13ac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 13af: dup
      // 13b0: fconst_0
      // 13b1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 13b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13b7: bipush 0
      // 13b8: sipush 128
      // 13bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13be: ldc_w 40.0
      // 13c1: ldc_w 11.0
      // 13c4: ldc_w 113.0
      // 13c7: ldc_w 16.0
      // 13ca: ldc_w 16.0
      // 13cd: ldc_w 16.0
      // 13d0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 13d3: dup
      // 13d4: fconst_0
      // 13d5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 13d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13db: bipush 0
      // 13dc: sipush 128
      // 13df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13e2: ldc_w 56.0
      // 13e5: ldc_w -5.0
      // 13e8: ldc_w 113.0
      // 13eb: ldc_w 16.0
      // 13ee: ldc_w 16.0
      // 13f1: ldc_w 16.0
      // 13f4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 13f7: dup
      // 13f8: fconst_0
      // 13f9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 13fc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 13ff: bipush 0
      // 1400: sipush 128
      // 1403: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1406: ldc_w 40.0
      // 1409: ldc_w -5.0
      // 140c: ldc_w 113.0
      // 140f: ldc_w 16.0
      // 1412: ldc_w 16.0
      // 1415: ldc_w 16.0
      // 1418: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 141b: dup
      // 141c: fconst_0
      // 141d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1420: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1423: bipush 0
      // 1424: sipush 128
      // 1427: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 142a: ldc_w 72.0
      // 142d: ldc_w -5.0
      // 1430: ldc_w 97.0
      // 1433: ldc_w 16.0
      // 1436: ldc_w 16.0
      // 1439: ldc_w 16.0
      // 143c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 143f: dup
      // 1440: fconst_0
      // 1441: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1444: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1447: bipush 0
      // 1448: sipush 128
      // 144b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 144e: ldc_w 72.0
      // 1451: ldc_w -21.0
      // 1454: ldc_w 97.0
      // 1457: ldc_w 16.0
      // 145a: ldc_w 16.0
      // 145d: ldc_w 16.0
      // 1460: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1463: dup
      // 1464: fconst_0
      // 1465: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1468: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 146b: bipush 0
      // 146c: sipush 128
      // 146f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1472: ldc_w 56.0
      // 1475: ldc_w -21.0
      // 1478: ldc_w 97.0
      // 147b: ldc_w 16.0
      // 147e: ldc_w 16.0
      // 1481: ldc_w 16.0
      // 1484: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1487: dup
      // 1488: fconst_0
      // 1489: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 148c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 148f: bipush 0
      // 1490: sipush 128
      // 1493: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1496: ldc_w 40.0
      // 1499: ldc_w -21.0
      // 149c: ldc_w 97.0
      // 149f: ldc_w 16.0
      // 14a2: ldc_w 16.0
      // 14a5: ldc_w 16.0
      // 14a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14ab: dup
      // 14ac: fconst_0
      // 14ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 14b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14b3: bipush 0
      // 14b4: sipush 128
      // 14b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14ba: ldc_w 24.0
      // 14bd: ldc_w -21.0
      // 14c0: ldc_w 97.0
      // 14c3: ldc_w 16.0
      // 14c6: ldc_w 16.0
      // 14c9: ldc_w 16.0
      // 14cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14cf: dup
      // 14d0: fconst_0
      // 14d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 14d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14d7: bipush 0
      // 14d8: sipush 128
      // 14db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14de: ldc_w 24.0
      // 14e1: ldc_w -21.0
      // 14e4: ldc_w 113.0
      // 14e7: ldc_w 16.0
      // 14ea: ldc_w 16.0
      // 14ed: ldc_w 16.0
      // 14f0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 14f3: dup
      // 14f4: fconst_0
      // 14f5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 14f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 14fb: bipush 0
      // 14fc: sipush 128
      // 14ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1502: ldc_w 8.0
      // 1505: ldc_w -21.0
      // 1508: ldc_w 97.0
      // 150b: ldc_w 16.0
      // 150e: ldc_w 16.0
      // 1511: ldc_w 16.0
      // 1514: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1517: dup
      // 1518: fconst_0
      // 1519: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 151c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 151f: bipush 0
      // 1520: sipush 128
      // 1523: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1526: ldc_w 8.0
      // 1529: ldc_w -21.0
      // 152c: ldc_w 113.0
      // 152f: ldc_w 16.0
      // 1532: ldc_w 16.0
      // 1535: ldc_w 16.0
      // 1538: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 153b: dup
      // 153c: fconst_0
      // 153d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1540: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1543: bipush 0
      // 1544: sipush 128
      // 1547: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 154a: ldc_w 8.0
      // 154d: ldc_w -5.0
      // 1550: ldc_w 113.0
      // 1553: ldc_w 16.0
      // 1556: ldc_w 16.0
      // 1559: ldc_w 16.0
      // 155c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 155f: dup
      // 1560: fconst_0
      // 1561: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1564: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1567: bipush 0
      // 1568: sipush 128
      // 156b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 156e: ldc_w 8.0
      // 1571: ldc_w -5.0
      // 1574: ldc_w 81.0
      // 1577: ldc_w 16.0
      // 157a: ldc_w 16.0
      // 157d: ldc_w 16.0
      // 1580: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1583: dup
      // 1584: fconst_0
      // 1585: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1588: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 158b: bipush 0
      // 158c: sipush 128
      // 158f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1592: ldc_w 8.0
      // 1595: ldc_w -5.0
      // 1598: ldc_w 129.0
      // 159b: ldc_w 16.0
      // 159e: ldc_w 16.0
      // 15a1: ldc_w 16.0
      // 15a4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15a7: dup
      // 15a8: fconst_0
      // 15a9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15af: bipush 0
      // 15b0: sipush 128
      // 15b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15b6: ldc_w 24.0
      // 15b9: ldc_w -5.0
      // 15bc: ldc_w 129.0
      // 15bf: ldc_w 16.0
      // 15c2: ldc_w 16.0
      // 15c5: ldc_w 16.0
      // 15c8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15cb: dup
      // 15cc: fconst_0
      // 15cd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15d0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15d3: bipush 0
      // 15d4: sipush 128
      // 15d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15da: ldc_w 8.0
      // 15dd: ldc_w 11.0
      // 15e0: ldc_w 113.0
      // 15e3: ldc_w 16.0
      // 15e6: ldc_w 16.0
      // 15e9: ldc_w 16.0
      // 15ec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 15ef: dup
      // 15f0: fconst_0
      // 15f1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 15f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15f7: bipush 0
      // 15f8: sipush 128
      // 15fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 15fe: ldc_w 24.0
      // 1601: ldc_w 27.0
      // 1604: ldc_w 129.0
      // 1607: ldc_w 16.0
      // 160a: ldc_w 16.0
      // 160d: ldc_w 16.0
      // 1610: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1613: dup
      // 1614: fconst_0
      // 1615: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1618: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 161b: bipush 0
      // 161c: sipush 128
      // 161f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1622: ldc_w 40.0
      // 1625: ldc_w 27.0
      // 1628: ldc_w 129.0
      // 162b: ldc_w 16.0
      // 162e: ldc_w 16.0
      // 1631: ldc_w 16.0
      // 1634: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1637: dup
      // 1638: fconst_0
      // 1639: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 163c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 163f: bipush 0
      // 1640: sipush 128
      // 1643: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1646: ldc_w 40.0
      // 1649: ldc_w 11.0
      // 164c: ldc_w 129.0
      // 164f: ldc_w 16.0
      // 1652: ldc_w 16.0
      // 1655: ldc_w 16.0
      // 1658: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 165b: dup
      // 165c: fconst_0
      // 165d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1660: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1663: bipush 0
      // 1664: sipush 128
      // 1667: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 166a: ldc_w 24.0
      // 166d: ldc_w 27.0
      // 1670: ldc_w 113.0
      // 1673: ldc_w 16.0
      // 1676: ldc_w 16.0
      // 1679: ldc_w 16.0
      // 167c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 167f: dup
      // 1680: fconst_0
      // 1681: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1684: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1687: bipush 0
      // 1688: sipush 128
      // 168b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 168e: ldc_w 40.0
      // 1691: ldc_w 27.0
      // 1694: ldc_w 113.0
      // 1697: ldc_w 16.0
      // 169a: ldc_w 16.0
      // 169d: ldc_w 16.0
      // 16a0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16a3: dup
      // 16a4: fconst_0
      // 16a5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16a8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16ab: bipush 0
      // 16ac: sipush 128
      // 16af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16b2: ldc_w 56.0
      // 16b5: ldc_w 27.0
      // 16b8: ldc_w 113.0
      // 16bb: ldc_w 16.0
      // 16be: ldc_w 16.0
      // 16c1: ldc_w 16.0
      // 16c4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16c7: dup
      // 16c8: fconst_0
      // 16c9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16cf: bipush 0
      // 16d0: sipush 128
      // 16d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16d6: ldc_w 24.0
      // 16d9: ldc_w 11.0
      // 16dc: ldc_w 129.0
      // 16df: ldc_w 16.0
      // 16e2: ldc_w 16.0
      // 16e5: ldc_w 16.0
      // 16e8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 16eb: dup
      // 16ec: fconst_0
      // 16ed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 16f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16f3: bipush 0
      // 16f4: sipush 128
      // 16f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 16fa: ldc_w 8.0
      // 16fd: ldc_w 11.0
      // 1700: ldc_w 129.0
      // 1703: ldc_w 16.0
      // 1706: ldc_w 16.0
      // 1709: ldc_w 16.0
      // 170c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 170f: dup
      // 1710: fconst_0
      // 1711: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1714: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1717: bipush 0
      // 1718: sipush 128
      // 171b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 171e: ldc_w 88.0
      // 1721: ldc_w -5.0
      // 1724: ldc_w 81.0
      // 1727: ldc_w 16.0
      // 172a: ldc_w 16.0
      // 172d: ldc_w 16.0
      // 1730: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1733: dup
      // 1734: fconst_0
      // 1735: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1738: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 173b: bipush 0
      // 173c: sipush 128
      // 173f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1742: ldc_w 88.0
      // 1745: ldc_w -5.0
      // 1748: ldc_w 97.0
      // 174b: ldc_w 16.0
      // 174e: ldc_w 16.0
      // 1751: ldc_w 16.0
      // 1754: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1757: dup
      // 1758: fconst_0
      // 1759: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 175c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 175f: bipush 0
      // 1760: sipush 128
      // 1763: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1766: ldc_w 88.0
      // 1769: ldc_w -21.0
      // 176c: ldc_w 97.0
      // 176f: ldc_w 16.0
      // 1772: ldc_w 16.0
      // 1775: ldc_w 16.0
      // 1778: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 177b: dup
      // 177c: fconst_0
      // 177d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1780: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1783: bipush 0
      // 1784: sipush 128
      // 1787: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 178a: ldc_w 104.0
      // 178d: ldc_w -21.0
      // 1790: ldc_w 97.0
      // 1793: ldc_w 16.0
      // 1796: ldc_w 16.0
      // 1799: ldc_w 16.0
      // 179c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 179f: dup
      // 17a0: fconst_0
      // 17a1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 17a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17a7: bipush 0
      // 17a8: sipush 128
      // 17ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17ae: ldc_w 120.0
      // 17b1: ldc_w -21.0
      // 17b4: ldc_w 81.0
      // 17b7: ldc_w 16.0
      // 17ba: ldc_w 16.0
      // 17bd: ldc_w 16.0
      // 17c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 17c3: dup
      // 17c4: fconst_0
      // 17c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 17c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17cb: bipush 0
      // 17cc: sipush 128
      // 17cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17d2: ldc_w 104.0
      // 17d5: ldc_w -21.0
      // 17d8: ldc_w 81.0
      // 17db: ldc_w 16.0
      // 17de: ldc_w 16.0
      // 17e1: ldc_w 16.0
      // 17e4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 17e7: dup
      // 17e8: fconst_0
      // 17e9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 17ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17ef: bipush 0
      // 17f0: sipush 128
      // 17f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 17f6: ldc_w 104.0
      // 17f9: ldc_w -21.0
      // 17fc: ldc_w 65.0
      // 17ff: ldc_w 16.0
      // 1802: ldc_w 16.0
      // 1805: ldc_w 16.0
      // 1808: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 180b: dup
      // 180c: fconst_0
      // 180d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1810: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1813: bipush 0
      // 1814: sipush 128
      // 1817: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 181a: ldc_w 104.0
      // 181d: ldc_w -37.0
      // 1820: ldc_w 49.0
      // 1823: ldc_w 16.0
      // 1826: ldc_w 16.0
      // 1829: ldc_w 16.0
      // 182c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 182f: dup
      // 1830: fconst_0
      // 1831: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1834: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1837: bipush 0
      // 1838: sipush 128
      // 183b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 183e: ldc_w 104.0
      // 1841: ldc_w -53.0
      // 1844: ldc_w 49.0
      // 1847: ldc_w 16.0
      // 184a: ldc_w 16.0
      // 184d: ldc_w 16.0
      // 1850: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1853: dup
      // 1854: fconst_0
      // 1855: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1858: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 185b: bipush 0
      // 185c: sipush 128
      // 185f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1862: ldc_w 88.0
      // 1865: ldc_w -53.0
      // 1868: ldc_w 49.0
      // 186b: ldc_w 16.0
      // 186e: ldc_w 16.0
      // 1871: ldc_w 16.0
      // 1874: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1877: dup
      // 1878: fconst_0
      // 1879: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 187c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 187f: sipush 432
      // 1882: sipush 350
      // 1885: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1888: ldc_w 120.0
      // 188b: ldc_w -37.0
      // 188e: ldc_w 81.0
      // 1891: ldc_w 16.0
      // 1894: ldc_w 16.0
      // 1897: ldc_w 16.0
      // 189a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 189d: dup
      // 189e: fconst_0
      // 189f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18a5: bipush 0
      // 18a6: sipush 128
      // 18a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18ac: ldc_w 136.0
      // 18af: ldc_w -53.0
      // 18b2: ldc_w 97.0
      // 18b5: ldc_w 16.0
      // 18b8: ldc_w 16.0
      // 18bb: ldc_w 16.0
      // 18be: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 18c1: dup
      // 18c2: fconst_0
      // 18c3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18c9: bipush 0
      // 18ca: sipush 128
      // 18cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18d0: ldc_w 136.0
      // 18d3: ldc_w -53.0
      // 18d6: ldc_w 113.0
      // 18d9: ldc_w 16.0
      // 18dc: ldc_w 16.0
      // 18df: ldc_w 16.0
      // 18e2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 18e5: dup
      // 18e6: fconst_0
      // 18e7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 18ea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18ed: bipush 0
      // 18ee: sipush 128
      // 18f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 18f4: ldc_w 136.0
      // 18f7: ldc_w -69.0
      // 18fa: ldc_w 97.0
      // 18fd: ldc_w 16.0
      // 1900: ldc_w 16.0
      // 1903: ldc_w 16.0
      // 1906: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1909: dup
      // 190a: fconst_0
      // 190b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 190e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1911: bipush 0
      // 1912: sipush 128
      // 1915: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1918: ldc_w 168.0
      // 191b: ldc_w -53.0
      // 191e: ldc_w 81.0
      // 1921: ldc_w 16.0
      // 1924: ldc_w 16.0
      // 1927: ldc_w 16.0
      // 192a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 192d: dup
      // 192e: fconst_0
      // 192f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1932: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1935: bipush 0
      // 1936: sipush 128
      // 1939: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 193c: ldc_w 88.0
      // 193f: ldc_w -37.0
      // 1942: ldc_w 65.0
      // 1945: ldc_w 16.0
      // 1948: ldc_w 16.0
      // 194b: ldc_w 16.0
      // 194e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1951: dup
      // 1952: fconst_0
      // 1953: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1956: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1959: bipush 0
      // 195a: sipush 128
      // 195d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1960: ldc_w 88.0
      // 1963: ldc_w -37.0
      // 1966: ldc_w 81.0
      // 1969: ldc_w 16.0
      // 196c: ldc_w 16.0
      // 196f: ldc_w 16.0
      // 1972: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1975: dup
      // 1976: fconst_0
      // 1977: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 197a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 197d: bipush 0
      // 197e: sipush 128
      // 1981: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1984: ldc_w 72.0
      // 1987: ldc_w -37.0
      // 198a: ldc_w 81.0
      // 198d: ldc_w 16.0
      // 1990: ldc_w 16.0
      // 1993: ldc_w 16.0
      // 1996: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1999: dup
      // 199a: fconst_0
      // 199b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 199e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19a1: bipush 0
      // 19a2: sipush 128
      // 19a5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19a8: ldc_w -8.0
      // 19ab: ldc_w -37.0
      // 19ae: ldc_w 81.0
      // 19b1: ldc_w 16.0
      // 19b4: ldc_w 16.0
      // 19b7: ldc_w 16.0
      // 19ba: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 19bd: dup
      // 19be: fconst_0
      // 19bf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 19c2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19c5: bipush 0
      // 19c6: sipush 128
      // 19c9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19cc: ldc_w -8.0
      // 19cf: ldc_w -21.0
      // 19d2: ldc_w 81.0
      // 19d5: ldc_w 16.0
      // 19d8: ldc_w 16.0
      // 19db: ldc_w 16.0
      // 19de: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 19e1: dup
      // 19e2: fconst_0
      // 19e3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 19e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19e9: bipush 0
      // 19ea: sipush 128
      // 19ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 19f0: ldc_w 72.0
      // 19f3: ldc_w -69.0
      // 19f6: ldc_w 81.0
      // 19f9: ldc_w 16.0
      // 19fc: ldc_w 16.0
      // 19ff: ldc_w 16.0
      // 1a02: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a05: dup
      // 1a06: fconst_0
      // 1a07: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a0a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a0d: bipush 0
      // 1a0e: sipush 128
      // 1a11: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a14: ldc_w 120.0
      // 1a17: ldc_w -69.0
      // 1a1a: ldc_w 49.0
      // 1a1d: ldc_w 16.0
      // 1a20: ldc_w 16.0
      // 1a23: ldc_w 16.0
      // 1a26: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a29: dup
      // 1a2a: fconst_0
      // 1a2b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a2e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a31: bipush 0
      // 1a32: sipush 128
      // 1a35: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a38: ldc_w 152.0
      // 1a3b: ldc_w -69.0
      // 1a3e: ldc_w 65.0
      // 1a41: ldc_w 16.0
      // 1a44: ldc_w 16.0
      // 1a47: ldc_w 16.0
      // 1a4a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a4d: dup
      // 1a4e: fconst_0
      // 1a4f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a52: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a55: bipush 0
      // 1a56: sipush 128
      // 1a59: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a5c: ldc_w 152.0
      // 1a5f: ldc_w -85.0
      // 1a62: ldc_w 81.0
      // 1a65: ldc_w 16.0
      // 1a68: ldc_w 16.0
      // 1a6b: ldc_w 16.0
      // 1a6e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a71: dup
      // 1a72: fconst_0
      // 1a73: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a76: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a79: bipush 0
      // 1a7a: sipush 128
      // 1a7d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a80: ldc_w 136.0
      // 1a83: ldc_w -85.0
      // 1a86: ldc_w 65.0
      // 1a89: ldc_w 16.0
      // 1a8c: ldc_w 16.0
      // 1a8f: ldc_w 16.0
      // 1a92: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1a95: dup
      // 1a96: fconst_0
      // 1a97: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1a9a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1a9d: bipush 0
      // 1a9e: sipush 128
      // 1aa1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1aa4: ldc_w 104.0
      // 1aa7: ldc_w -85.0
      // 1aaa: ldc_w 65.0
      // 1aad: ldc_w 16.0
      // 1ab0: ldc_w 16.0
      // 1ab3: ldc_w 16.0
      // 1ab6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ab9: dup
      // 1aba: fconst_0
      // 1abb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1abe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ac1: bipush 0
      // 1ac2: sipush 128
      // 1ac5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ac8: ldc_w 104.0
      // 1acb: ldc_w -85.0
      // 1ace: ldc_w 81.0
      // 1ad1: ldc_w 16.0
      // 1ad4: ldc_w 16.0
      // 1ad7: ldc_w 16.0
      // 1ada: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1add: dup
      // 1ade: fconst_0
      // 1adf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ae2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ae5: bipush 0
      // 1ae6: sipush 128
      // 1ae9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1aec: ldc_w 104.0
      // 1aef: ldc_w -101.0
      // 1af2: ldc_w 97.0
      // 1af5: ldc_w 16.0
      // 1af8: ldc_w 16.0
      // 1afb: ldc_w 16.0
      // 1afe: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b01: dup
      // 1b02: fconst_0
      // 1b03: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b06: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b09: bipush 0
      // 1b0a: sipush 128
      // 1b0d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b10: ldc_w 120.0
      // 1b13: ldc_w -85.0
      // 1b16: ldc_w 33.0
      // 1b19: ldc_w 16.0
      // 1b1c: ldc_w 16.0
      // 1b1f: ldc_w 16.0
      // 1b22: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b25: dup
      // 1b26: fconst_0
      // 1b27: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b2a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b2d: bipush 0
      // 1b2e: sipush 128
      // 1b31: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b34: ldc_w 104.0
      // 1b37: ldc_w -85.0
      // 1b3a: ldc_w 49.0
      // 1b3d: ldc_w 16.0
      // 1b40: ldc_w 16.0
      // 1b43: ldc_w 16.0
      // 1b46: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b49: dup
      // 1b4a: fconst_0
      // 1b4b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b4e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b51: bipush 0
      // 1b52: sipush 128
      // 1b55: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b58: ldc_w 120.0
      // 1b5b: ldc_w -85.0
      // 1b5e: ldc_w 65.0
      // 1b61: ldc_w 16.0
      // 1b64: ldc_w 16.0
      // 1b67: ldc_w 16.0
      // 1b6a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b6d: dup
      // 1b6e: fconst_0
      // 1b6f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b72: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b75: bipush 0
      // 1b76: sipush 128
      // 1b79: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b7c: ldc_w 136.0
      // 1b7f: ldc_w -85.0
      // 1b82: ldc_w 49.0
      // 1b85: ldc_w 16.0
      // 1b88: ldc_w 16.0
      // 1b8b: ldc_w 16.0
      // 1b8e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1b91: dup
      // 1b92: fconst_0
      // 1b93: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1b96: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1b99: bipush 0
      // 1b9a: sipush 128
      // 1b9d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ba0: ldc_w 152.0
      // 1ba3: ldc_w -85.0
      // 1ba6: ldc_w 65.0
      // 1ba9: ldc_w 16.0
      // 1bac: ldc_w 16.0
      // 1baf: ldc_w 16.0
      // 1bb2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1bb5: dup
      // 1bb6: fconst_0
      // 1bb7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1bba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bbd: bipush 0
      // 1bbe: sipush 128
      // 1bc1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1bc4: ldc_w 88.0
      // 1bc7: ldc_w -69.0
      // 1bca: ldc_w 65.0
      // 1bcd: ldc_w 16.0
      // 1bd0: ldc_w 16.0
      // 1bd3: ldc_w 16.0
      // 1bd6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1bd9: dup
      // 1bda: fconst_0
      // 1bdb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1bde: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1be1: bipush 0
      // 1be2: sipush 128
      // 1be5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1be8: ldc_w 136.0
      // 1beb: ldc_w -21.0
      // 1bee: ldc_w 81.0
      // 1bf1: ldc_w 16.0
      // 1bf4: ldc_w 16.0
      // 1bf7: ldc_w 16.0
      // 1bfa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1bfd: dup
      // 1bfe: fconst_0
      // 1bff: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c02: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c05: bipush 0
      // 1c06: sipush 128
      // 1c09: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c0c: ldc_w 72.0
      // 1c0f: ldc_w -53.0
      // 1c12: ldc_w 65.0
      // 1c15: ldc_w 16.0
      // 1c18: ldc_w 16.0
      // 1c1b: ldc_w 16.0
      // 1c1e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c21: dup
      // 1c22: fconst_0
      // 1c23: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c26: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c29: bipush 0
      // 1c2a: sipush 128
      // 1c2d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c30: ldc_w 72.0
      // 1c33: ldc_w -85.0
      // 1c36: ldc_w 65.0
      // 1c39: ldc_w 16.0
      // 1c3c: ldc_w 16.0
      // 1c3f: ldc_w 16.0
      // 1c42: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c45: dup
      // 1c46: fconst_0
      // 1c47: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c4a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c4d: bipush 0
      // 1c4e: sipush 128
      // 1c51: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c54: ldc_w 56.0
      // 1c57: ldc_w -85.0
      // 1c5a: ldc_w 65.0
      // 1c5d: ldc_w 16.0
      // 1c60: ldc_w 16.0
      // 1c63: ldc_w 16.0
      // 1c66: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c69: dup
      // 1c6a: fconst_0
      // 1c6b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c6e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c71: bipush 0
      // 1c72: sipush 128
      // 1c75: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c78: ldc_w 56.0
      // 1c7b: ldc_w -85.0
      // 1c7e: ldc_w 49.0
      // 1c81: ldc_w 16.0
      // 1c84: ldc_w 16.0
      // 1c87: ldc_w 16.0
      // 1c8a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1c8d: dup
      // 1c8e: fconst_0
      // 1c8f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1c92: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c95: bipush 0
      // 1c96: sipush 352
      // 1c99: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1c9c: ldc_w 88.0
      // 1c9f: ldc_w -85.0
      // 1ca2: ldc_w 49.0
      // 1ca5: ldc_w 16.0
      // 1ca8: ldc_w 16.0
      // 1cab: ldc_w 16.0
      // 1cae: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1cb1: dup
      // 1cb2: fconst_0
      // 1cb3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1cb6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cb9: bipush 0
      // 1cba: sipush 352
      // 1cbd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cc0: ldc_w 72.0
      // 1cc3: ldc_w -85.0
      // 1cc6: ldc_w 49.0
      // 1cc9: ldc_w 16.0
      // 1ccc: ldc_w 16.0
      // 1ccf: ldc_w 16.0
      // 1cd2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1cd5: dup
      // 1cd6: fconst_0
      // 1cd7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1cda: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1cdd: bipush 0
      // 1cde: sipush 434
      // 1ce1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ce4: ldc_w 88.0
      // 1ce7: ldc_w -85.0
      // 1cea: ldc_w 33.0
      // 1ced: ldc_w 16.0
      // 1cf0: ldc_w 16.0
      // 1cf3: ldc_w 16.0
      // 1cf6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1cf9: dup
      // 1cfa: fconst_0
      // 1cfb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1cfe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d01: bipush 0
      // 1d02: sipush 434
      // 1d05: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d08: ldc_w 72.0
      // 1d0b: ldc_w -85.0
      // 1d0e: ldc_w 33.0
      // 1d11: ldc_w 16.0
      // 1d14: ldc_w 16.0
      // 1d17: ldc_w 16.0
      // 1d1a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d1d: dup
      // 1d1e: fconst_0
      // 1d1f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d22: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d25: bipush 0
      // 1d26: sipush 434
      // 1d29: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d2c: ldc_w 72.0
      // 1d2f: ldc_w -69.0
      // 1d32: ldc_w 33.0
      // 1d35: ldc_w 16.0
      // 1d38: ldc_w 16.0
      // 1d3b: ldc_w 16.0
      // 1d3e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d41: dup
      // 1d42: fconst_0
      // 1d43: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d46: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d49: bipush 0
      // 1d4a: sipush 434
      // 1d4d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d50: ldc_w 88.0
      // 1d53: ldc_w -69.0
      // 1d56: ldc_w 33.0
      // 1d59: ldc_w 16.0
      // 1d5c: ldc_w 16.0
      // 1d5f: ldc_w 16.0
      // 1d62: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d65: dup
      // 1d66: fconst_0
      // 1d67: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d6a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d6d: bipush 0
      // 1d6e: sipush 128
      // 1d71: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d74: ldc_w 40.0
      // 1d77: ldc_w -85.0
      // 1d7a: ldc_w 65.0
      // 1d7d: ldc_w 16.0
      // 1d80: ldc_w 16.0
      // 1d83: ldc_w 16.0
      // 1d86: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1d89: dup
      // 1d8a: fconst_0
      // 1d8b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1d8e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d91: bipush 0
      // 1d92: sipush 128
      // 1d95: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1d98: ldc_w 56.0
      // 1d9b: ldc_w -53.0
      // 1d9e: ldc_w 97.0
      // 1da1: ldc_w 16.0
      // 1da4: ldc_w 16.0
      // 1da7: ldc_w 16.0
      // 1daa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1dad: dup
      // 1dae: fconst_0
      // 1daf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1db2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1db5: bipush 0
      // 1db6: sipush 128
      // 1db9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dbc: ldc_w 24.0
      // 1dbf: ldc_w -53.0
      // 1dc2: ldc_w 97.0
      // 1dc5: ldc_w 16.0
      // 1dc8: ldc_w 16.0
      // 1dcb: ldc_w 16.0
      // 1dce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1dd1: dup
      // 1dd2: fconst_0
      // 1dd3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1dd6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dd9: bipush 0
      // 1dda: sipush 128
      // 1ddd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1de0: ldc_w 56.0
      // 1de3: ldc_w -53.0
      // 1de6: ldc_w 113.0
      // 1de9: ldc_w 16.0
      // 1dec: ldc_w 16.0
      // 1def: ldc_w 16.0
      // 1df2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1df5: dup
      // 1df6: fconst_0
      // 1df7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1dfa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1dfd: bipush 0
      // 1dfe: sipush 128
      // 1e01: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e04: ldc_w 56.0
      // 1e07: ldc_w -101.0
      // 1e0a: ldc_w 65.0
      // 1e0d: ldc_w 16.0
      // 1e10: ldc_w 16.0
      // 1e13: ldc_w 16.0
      // 1e16: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e19: dup
      // 1e1a: fconst_0
      // 1e1b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e1e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e21: bipush 0
      // 1e22: sipush 128
      // 1e25: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e28: ldc_w 40.0
      // 1e2b: ldc_w -101.0
      // 1e2e: ldc_w 81.0
      // 1e31: ldc_w 16.0
      // 1e34: ldc_w 16.0
      // 1e37: ldc_w 16.0
      // 1e3a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e3d: dup
      // 1e3e: fconst_0
      // 1e3f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e45: bipush 0
      // 1e46: sipush 128
      // 1e49: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e4c: ldc_w 120.0
      // 1e4f: ldc_w -5.0
      // 1e52: ldc_w 65.0
      // 1e55: ldc_w 16.0
      // 1e58: ldc_w 16.0
      // 1e5b: ldc_w 16.0
      // 1e5e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e61: dup
      // 1e62: fconst_0
      // 1e63: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e66: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e69: bipush 0
      // 1e6a: sipush 128
      // 1e6d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e70: ldc_w 136.0
      // 1e73: ldc_w -5.0
      // 1e76: ldc_w 65.0
      // 1e79: ldc_w 16.0
      // 1e7c: ldc_w 16.0
      // 1e7f: ldc_w 16.0
      // 1e82: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1e85: dup
      // 1e86: fconst_0
      // 1e87: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1e8a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e8d: bipush 0
      // 1e8e: sipush 128
      // 1e91: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1e94: ldc_w 152.0
      // 1e97: ldc_w -5.0
      // 1e9a: ldc_w 65.0
      // 1e9d: ldc_w 16.0
      // 1ea0: ldc_w 16.0
      // 1ea3: ldc_w 16.0
      // 1ea6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ea9: dup
      // 1eaa: fconst_0
      // 1eab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1eae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eb1: bipush 0
      // 1eb2: sipush 128
      // 1eb5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1eb8: ldc_w 152.0
      // 1ebb: ldc_w -21.0
      // 1ebe: ldc_w 65.0
      // 1ec1: ldc_w 16.0
      // 1ec4: ldc_w 16.0
      // 1ec7: ldc_w 16.0
      // 1eca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ecd: dup
      // 1ece: fconst_0
      // 1ecf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ed2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ed5: bipush 0
      // 1ed6: sipush 128
      // 1ed9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1edc: ldc_w 152.0
      // 1edf: ldc_w -37.0
      // 1ee2: ldc_w 65.0
      // 1ee5: ldc_w 16.0
      // 1ee8: ldc_w 16.0
      // 1eeb: ldc_w 16.0
      // 1eee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1ef1: dup
      // 1ef2: fconst_0
      // 1ef3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ef6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ef9: bipush 0
      // 1efa: sipush 128
      // 1efd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f00: ldc_w 152.0
      // 1f03: ldc_w -53.0
      // 1f06: ldc_w 49.0
      // 1f09: ldc_w 16.0
      // 1f0c: ldc_w 16.0
      // 1f0f: ldc_w 16.0
      // 1f12: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f15: dup
      // 1f16: fconst_0
      // 1f17: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f1a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f1d: bipush 0
      // 1f1e: sipush 128
      // 1f21: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f24: ldc_w 152.0
      // 1f27: ldc_w -53.0
      // 1f2a: ldc_w 33.0
      // 1f2d: ldc_w 16.0
      // 1f30: ldc_w 16.0
      // 1f33: ldc_w 16.0
      // 1f36: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f39: dup
      // 1f3a: fconst_0
      // 1f3b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f3e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f41: bipush 0
      // 1f42: sipush 128
      // 1f45: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f48: ldc_w 152.0
      // 1f4b: ldc_w -53.0
      // 1f4e: ldc_w 65.0
      // 1f51: ldc_w 16.0
      // 1f54: ldc_w 16.0
      // 1f57: ldc_w 16.0
      // 1f5a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f5d: dup
      // 1f5e: fconst_0
      // 1f5f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f62: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f65: bipush 0
      // 1f66: sipush 128
      // 1f69: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f6c: ldc_w 168.0
      // 1f6f: ldc_w -37.0
      // 1f72: ldc_w 65.0
      // 1f75: ldc_w 16.0
      // 1f78: ldc_w 16.0
      // 1f7b: ldc_w 16.0
      // 1f7e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1f81: dup
      // 1f82: fconst_0
      // 1f83: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1f86: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f89: bipush 0
      // 1f8a: sipush 128
      // 1f8d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1f90: ldc_w 168.0
      // 1f93: ldc_w -37.0
      // 1f96: ldc_w 49.0
      // 1f99: ldc_w 16.0
      // 1f9c: ldc_w 16.0
      // 1f9f: ldc_w 16.0
      // 1fa2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1fa5: dup
      // 1fa6: fconst_0
      // 1fa7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1faa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fad: bipush 0
      // 1fae: sipush 128
      // 1fb1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fb4: ldc_w 184.0
      // 1fb7: ldc_w -37.0
      // 1fba: ldc_w 49.0
      // 1fbd: ldc_w 16.0
      // 1fc0: ldc_w 16.0
      // 1fc3: ldc_w 16.0
      // 1fc6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1fc9: dup
      // 1fca: fconst_0
      // 1fcb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1fce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fd1: bipush 0
      // 1fd2: sipush 128
      // 1fd5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1fd8: ldc_w 152.0
      // 1fdb: ldc_w -5.0
      // 1fde: ldc_w 49.0
      // 1fe1: ldc_w 16.0
      // 1fe4: ldc_w 16.0
      // 1fe7: ldc_w 16.0
      // 1fea: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 1fed: dup
      // 1fee: fconst_0
      // 1fef: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 1ff2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ff5: bipush 0
      // 1ff6: sipush 128
      // 1ff9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 1ffc: ldc_w 136.0
      // 1fff: ldc_w -5.0
      // 2002: ldc_w 49.0
      // 2005: ldc_w 16.0
      // 2008: ldc_w 16.0
      // 200b: ldc_w 16.0
      // 200e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2011: dup
      // 2012: fconst_0
      // 2013: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2016: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2019: bipush 0
      // 201a: sipush 128
      // 201d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2020: ldc_w 120.0
      // 2023: ldc_w -5.0
      // 2026: ldc_w 49.0
      // 2029: ldc_w 16.0
      // 202c: ldc_w 16.0
      // 202f: ldc_w 16.0
      // 2032: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2035: dup
      // 2036: fconst_0
      // 2037: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 203a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 203d: bipush 0
      // 203e: sipush 128
      // 2041: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2044: ldc_w 72.0
      // 2047: ldc_w 11.0
      // 204a: ldc_w 33.0
      // 204d: ldc_w 16.0
      // 2050: ldc_w 16.0
      // 2053: ldc_w 16.0
      // 2056: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2059: dup
      // 205a: fconst_0
      // 205b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 205e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2061: bipush 0
      // 2062: sipush 128
      // 2065: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2068: ldc_w 56.0
      // 206b: ldc_w 11.0
      // 206e: ldc_w 17.0
      // 2071: ldc_w 16.0
      // 2074: ldc_w 16.0
      // 2077: ldc_w 16.0
      // 207a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 207d: dup
      // 207e: fconst_0
      // 207f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2082: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2085: bipush 0
      // 2086: sipush 128
      // 2089: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 208c: ldc_w 56.0
      // 208f: ldc_w -5.0
      // 2092: ldc_w 33.0
      // 2095: ldc_w 16.0
      // 2098: ldc_w 16.0
      // 209b: ldc_w 16.0
      // 209e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 20a1: dup
      // 20a2: fconst_0
      // 20a3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 20a6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20a9: bipush 0
      // 20aa: sipush 128
      // 20ad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20b0: ldc_w 72.0
      // 20b3: ldc_w -5.0
      // 20b6: ldc_w 33.0
      // 20b9: ldc_w 16.0
      // 20bc: ldc_w 16.0
      // 20bf: ldc_w 16.0
      // 20c2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 20c5: dup
      // 20c6: fconst_0
      // 20c7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 20ca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20cd: bipush 0
      // 20ce: sipush 128
      // 20d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20d4: ldc_w 40.0
      // 20d7: ldc_w -5.0
      // 20da: ldc_w 33.0
      // 20dd: ldc_w 16.0
      // 20e0: ldc_w 16.0
      // 20e3: ldc_w 16.0
      // 20e6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 20e9: dup
      // 20ea: fconst_0
      // 20eb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 20ee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20f1: bipush 0
      // 20f2: sipush 128
      // 20f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 20f8: ldc_w 40.0
      // 20fb: ldc_w -5.0
      // 20fe: ldc_w 17.0
      // 2101: ldc_w 16.0
      // 2104: ldc_w 16.0
      // 2107: ldc_w 16.0
      // 210a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 210d: dup
      // 210e: fconst_0
      // 210f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2112: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2115: bipush 0
      // 2116: sipush 128
      // 2119: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 211c: ldc_w 72.0
      // 211f: ldc_w 27.0
      // 2122: ldc_w 17.0
      // 2125: ldc_w 16.0
      // 2128: ldc_w 16.0
      // 212b: ldc_w 16.0
      // 212e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2131: dup
      // 2132: fconst_0
      // 2133: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2136: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2139: bipush 0
      // 213a: sipush 128
      // 213d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2140: ldc_w 88.0
      // 2143: ldc_w 27.0
      // 2146: ldc_w 17.0
      // 2149: ldc_w 16.0
      // 214c: ldc_w 16.0
      // 214f: ldc_w 16.0
      // 2152: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2155: dup
      // 2156: fconst_0
      // 2157: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 215a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 215d: bipush 0
      // 215e: sipush 128
      // 2161: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2164: ldc_w 40.0
      // 2167: ldc_w 11.0
      // 216a: fconst_1
      // 216b: ldc_w 16.0
      // 216e: ldc_w 16.0
      // 2171: ldc_w 16.0
      // 2174: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2177: dup
      // 2178: fconst_0
      // 2179: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 217c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 217f: bipush 0
      // 2180: sipush 128
      // 2183: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2186: ldc_w 152.0
      // 2189: ldc_w 43.0
      // 218c: ldc_w 17.0
      // 218f: ldc_w 16.0
      // 2192: ldc_w 16.0
      // 2195: ldc_w 16.0
      // 2198: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 219b: dup
      // 219c: fconst_0
      // 219d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 21a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21a3: bipush 0
      // 21a4: sipush 128
      // 21a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21aa: ldc_w 168.0
      // 21ad: ldc_w 43.0
      // 21b0: ldc_w 17.0
      // 21b3: ldc_w 16.0
      // 21b6: ldc_w 16.0
      // 21b9: ldc_w 16.0
      // 21bc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 21bf: dup
      // 21c0: fconst_0
      // 21c1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 21c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21c7: bipush 0
      // 21c8: sipush 128
      // 21cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21ce: ldc_w 184.0
      // 21d1: ldc_w 43.0
      // 21d4: ldc_w 17.0
      // 21d7: ldc_w 16.0
      // 21da: ldc_w 16.0
      // 21dd: ldc_w 16.0
      // 21e0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 21e3: dup
      // 21e4: fconst_0
      // 21e5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 21e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21eb: bipush 0
      // 21ec: sipush 128
      // 21ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 21f2: ldc_w 200.0
      // 21f5: ldc_w 43.0
      // 21f8: ldc_w 17.0
      // 21fb: ldc_w 16.0
      // 21fe: ldc_w 16.0
      // 2201: ldc_w 16.0
      // 2204: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2207: dup
      // 2208: fconst_0
      // 2209: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 220c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 220f: bipush 0
      // 2210: sipush 128
      // 2213: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2216: ldc_w 184.0
      // 2219: ldc_w 59.0
      // 221c: ldc_w 17.0
      // 221f: ldc_w 16.0
      // 2222: ldc_w 16.0
      // 2225: ldc_w 16.0
      // 2228: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 222b: dup
      // 222c: fconst_0
      // 222d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2230: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2233: bipush 0
      // 2234: sipush 128
      // 2237: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 223a: ldc_w 184.0
      // 223d: ldc_w 75.0
      // 2240: fconst_1
      // 2241: ldc_w 16.0
      // 2244: ldc_w 16.0
      // 2247: ldc_w 16.0
      // 224a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 224d: dup
      // 224e: fconst_0
      // 224f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2252: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2255: bipush 0
      // 2256: sipush 128
      // 2259: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 225c: ldc_w 184.0
      // 225f: ldc_w 59.0
      // 2262: fconst_1
      // 2263: ldc_w 16.0
      // 2266: ldc_w 16.0
      // 2269: ldc_w 16.0
      // 226c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 226f: dup
      // 2270: fconst_0
      // 2271: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2274: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2277: bipush 0
      // 2278: sipush 128
      // 227b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 227e: ldc_w 216.0
      // 2281: ldc_w 59.0
      // 2284: ldc_w -15.0
      // 2287: ldc_w 16.0
      // 228a: ldc_w 16.0
      // 228d: ldc_w 16.0
      // 2290: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2293: dup
      // 2294: fconst_0
      // 2295: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2298: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 229b: bipush 0
      // 229c: sipush 128
      // 229f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22a2: ldc_w 216.0
      // 22a5: ldc_w 59.0
      // 22a8: ldc_w -31.0
      // 22ab: ldc_w 16.0
      // 22ae: ldc_w 16.0
      // 22b1: ldc_w 16.0
      // 22b4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 22b7: dup
      // 22b8: fconst_0
      // 22b9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 22bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22bf: bipush 0
      // 22c0: sipush 128
      // 22c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22c6: ldc_w 216.0
      // 22c9: ldc_w 59.0
      // 22cc: ldc_w -47.0
      // 22cf: ldc_w 16.0
      // 22d2: ldc_w 16.0
      // 22d5: ldc_w 16.0
      // 22d8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 22db: dup
      // 22dc: fconst_0
      // 22dd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 22e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22e3: bipush 0
      // 22e4: sipush 128
      // 22e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 22ea: ldc_w 232.0
      // 22ed: ldc_w 59.0
      // 22f0: ldc_w -47.0
      // 22f3: ldc_w 16.0
      // 22f6: ldc_w 16.0
      // 22f9: ldc_w 16.0
      // 22fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 22ff: dup
      // 2300: fconst_0
      // 2301: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2304: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2307: bipush 0
      // 2308: sipush 128
      // 230b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 230e: ldc_w 248.0
      // 2311: ldc_w 75.0
      // 2314: ldc_w -47.0
      // 2317: ldc_w 16.0
      // 231a: ldc_w 16.0
      // 231d: ldc_w 16.0
      // 2320: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2323: dup
      // 2324: fconst_0
      // 2325: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2328: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 232b: bipush 0
      // 232c: sipush 128
      // 232f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2332: ldc_w 264.0
      // 2335: ldc_w 75.0
      // 2338: ldc_w -47.0
      // 233b: ldc_w 16.0
      // 233e: ldc_w 16.0
      // 2341: ldc_w 16.0
      // 2344: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2347: dup
      // 2348: fconst_0
      // 2349: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 234c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 234f: bipush 0
      // 2350: sipush 128
      // 2353: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2356: ldc_w 264.0
      // 2359: ldc_w 75.0
      // 235c: ldc_w -15.0
      // 235f: ldc_w 16.0
      // 2362: ldc_w 16.0
      // 2365: ldc_w 16.0
      // 2368: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 236b: dup
      // 236c: fconst_0
      // 236d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2370: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2373: bipush 0
      // 2374: sipush 128
      // 2377: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 237a: ldc_w 264.0
      // 237d: ldc_w 75.0
      // 2380: fconst_1
      // 2381: ldc_w 16.0
      // 2384: ldc_w 16.0
      // 2387: ldc_w 16.0
      // 238a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 238d: dup
      // 238e: fconst_0
      // 238f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2392: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2395: bipush 0
      // 2396: sipush 128
      // 2399: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 239c: ldc_w 264.0
      // 239f: ldc_w 59.0
      // 23a2: fconst_1
      // 23a3: ldc_w 16.0
      // 23a6: ldc_w 16.0
      // 23a9: ldc_w 16.0
      // 23ac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 23af: dup
      // 23b0: fconst_0
      // 23b1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 23b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23b7: bipush 0
      // 23b8: sipush 128
      // 23bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23be: ldc_w 264.0
      // 23c1: ldc_w 43.0
      // 23c4: fconst_1
      // 23c5: ldc_w 16.0
      // 23c8: ldc_w 16.0
      // 23cb: ldc_w 16.0
      // 23ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 23d1: dup
      // 23d2: fconst_0
      // 23d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 23d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23d9: bipush 0
      // 23da: sipush 128
      // 23dd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23e0: ldc_w 264.0
      // 23e3: ldc_w 27.0
      // 23e6: fconst_1
      // 23e7: ldc_w 16.0
      // 23ea: ldc_w 16.0
      // 23ed: ldc_w 16.0
      // 23f0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 23f3: dup
      // 23f4: fconst_0
      // 23f5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 23f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 23fb: bipush 0
      // 23fc: sipush 128
      // 23ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2402: ldc_w 264.0
      // 2405: ldc_w 11.0
      // 2408: fconst_1
      // 2409: ldc_w 16.0
      // 240c: ldc_w 16.0
      // 240f: ldc_w 16.0
      // 2412: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2415: dup
      // 2416: fconst_0
      // 2417: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 241a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 241d: bipush 47
      // 241f: sipush 128
      // 2422: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2425: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2428: ldc_w 264.0
      // 242b: ldc_w 75.0
      // 242e: ldc_w -63.0
      // 2431: ldc_w 16.0
      // 2434: ldc_w 16.0
      // 2437: ldc_w 16.0
      // 243a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 243d: dup
      // 243e: fconst_0
      // 243f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2442: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2445: bipush 0
      // 2446: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2449: bipush 0
      // 244a: sipush 128
      // 244d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2450: ldc_w 264.0
      // 2453: ldc_w 59.0
      // 2456: ldc_w -47.0
      // 2459: ldc_w 16.0
      // 245c: ldc_w 16.0
      // 245f: ldc_w 16.0
      // 2462: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2465: dup
      // 2466: fconst_0
      // 2467: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 246a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 246d: bipush 0
      // 246e: sipush 432
      // 2471: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2474: ldc_w 264.0
      // 2477: ldc_w 43.0
      // 247a: ldc_w -47.0
      // 247d: ldc_w 16.0
      // 2480: ldc_w 16.0
      // 2483: ldc_w 16.0
      // 2486: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2489: dup
      // 248a: fconst_0
      // 248b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 248e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2491: bipush 0
      // 2492: sipush 354
      // 2495: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2498: ldc_w 248.0
      // 249b: ldc_w 43.0
      // 249e: ldc_w -95.0
      // 24a1: ldc_w 16.0
      // 24a4: ldc_w 16.0
      // 24a7: ldc_w 16.0
      // 24aa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 24ad: dup
      // 24ae: fconst_0
      // 24af: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 24b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24b5: bipush 0
      // 24b6: sipush 354
      // 24b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24bc: ldc_w 248.0
      // 24bf: ldc_w 43.0
      // 24c2: ldc_w -79.0
      // 24c5: ldc_w 16.0
      // 24c8: ldc_w 16.0
      // 24cb: ldc_w 16.0
      // 24ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 24d1: dup
      // 24d2: fconst_0
      // 24d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 24d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24d9: bipush 0
      // 24da: sipush 354
      // 24dd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24e0: ldc_w 264.0
      // 24e3: ldc_w 43.0
      // 24e6: ldc_w -63.0
      // 24e9: ldc_w 16.0
      // 24ec: ldc_w 16.0
      // 24ef: ldc_w 16.0
      // 24f2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 24f5: dup
      // 24f6: fconst_0
      // 24f7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 24fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 24fd: sipush 436
      // 2500: sipush 432
      // 2503: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2506: ldc_w 248.0
      // 2509: ldc_w 27.0
      // 250c: ldc_w -79.0
      // 250f: ldc_w 16.0
      // 2512: ldc_w 16.0
      // 2515: ldc_w 16.0
      // 2518: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 251b: dup
      // 251c: fconst_0
      // 251d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2520: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2523: bipush 0
      // 2524: sipush 432
      // 2527: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 252a: ldc_w 248.0
      // 252d: ldc_w 27.0
      // 2530: ldc_w -63.0
      // 2533: ldc_w 16.0
      // 2536: ldc_w 16.0
      // 2539: ldc_w 16.0
      // 253c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 253f: dup
      // 2540: fconst_0
      // 2541: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2544: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2547: bipush 0
      // 2548: sipush 354
      // 254b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 254e: ldc_w 264.0
      // 2551: ldc_w 59.0
      // 2554: ldc_w -63.0
      // 2557: ldc_w 16.0
      // 255a: ldc_w 16.0
      // 255d: ldc_w 16.0
      // 2560: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2563: dup
      // 2564: fconst_0
      // 2565: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2568: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 256b: bipush 0
      // 256c: sipush 432
      // 256f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2572: ldc_w 248.0
      // 2575: ldc_w 27.0
      // 2578: ldc_w -47.0
      // 257b: ldc_w 16.0
      // 257e: ldc_w 16.0
      // 2581: ldc_w 16.0
      // 2584: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2587: dup
      // 2588: fconst_0
      // 2589: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 258c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 258f: bipush 0
      // 2590: sipush 432
      // 2593: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2596: ldc_w 232.0
      // 2599: ldc_w 11.0
      // 259c: ldc_w -63.0
      // 259f: ldc_w 16.0
      // 25a2: ldc_w 16.0
      // 25a5: ldc_w 16.0
      // 25a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 25ab: dup
      // 25ac: fconst_0
      // 25ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 25b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25b3: bipush 0
      // 25b4: sipush 432
      // 25b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25ba: ldc_w 232.0
      // 25bd: ldc_w -5.0
      // 25c0: ldc_w -63.0
      // 25c3: ldc_w 16.0
      // 25c6: ldc_w 16.0
      // 25c9: ldc_w 16.0
      // 25cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 25cf: dup
      // 25d0: fconst_0
      // 25d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 25d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25d7: bipush 0
      // 25d8: sipush 432
      // 25db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25de: ldc_w 216.0
      // 25e1: ldc_w -5.0
      // 25e4: ldc_w -47.0
      // 25e7: ldc_w 16.0
      // 25ea: ldc_w 16.0
      // 25ed: ldc_w 16.0
      // 25f0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 25f3: dup
      // 25f4: fconst_0
      // 25f5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 25f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 25fb: sipush 434
      // 25fe: sipush 432
      // 2601: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2604: ldc_w 184.0
      // 2607: ldc_w -21.0
      // 260a: ldc_w -47.0
      // 260d: ldc_w 16.0
      // 2610: ldc_w 16.0
      // 2613: ldc_w 16.0
      // 2616: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2619: dup
      // 261a: fconst_0
      // 261b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 261e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2621: bipush 0
      // 2622: sipush 432
      // 2625: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2628: ldc_w 200.0
      // 262b: ldc_w -5.0
      // 262e: ldc_w -47.0
      // 2631: ldc_w 16.0
      // 2634: ldc_w 16.0
      // 2637: ldc_w 16.0
      // 263a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 263d: dup
      // 263e: fconst_0
      // 263f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2642: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2645: bipush 0
      // 2646: sipush 432
      // 2649: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 264c: ldc_w 200.0
      // 264f: ldc_w -5.0
      // 2652: ldc_w -31.0
      // 2655: ldc_w 16.0
      // 2658: ldc_w 16.0
      // 265b: ldc_w 16.0
      // 265e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2661: dup
      // 2662: fconst_0
      // 2663: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2666: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2669: bipush 0
      // 266a: sipush 432
      // 266d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2670: ldc_w 184.0
      // 2673: ldc_w -5.0
      // 2676: ldc_w -31.0
      // 2679: ldc_w 16.0
      // 267c: ldc_w 16.0
      // 267f: ldc_w 16.0
      // 2682: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2685: dup
      // 2686: fconst_0
      // 2687: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 268a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 268d: bipush 0
      // 268e: sipush 432
      // 2691: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2694: ldc_w 168.0
      // 2697: ldc_w -5.0
      // 269a: ldc_w -63.0
      // 269d: ldc_w 16.0
      // 26a0: ldc_w 16.0
      // 26a3: ldc_w 16.0
      // 26a6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 26a9: dup
      // 26aa: fconst_0
      // 26ab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 26ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26b1: bipush 0
      // 26b2: sipush 432
      // 26b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26b8: ldc_w 152.0
      // 26bb: ldc_w -5.0
      // 26be: ldc_w -63.0
      // 26c1: ldc_w 16.0
      // 26c4: ldc_w 16.0
      // 26c7: ldc_w 16.0
      // 26ca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 26cd: dup
      // 26ce: fconst_0
      // 26cf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 26d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26d5: bipush 0
      // 26d6: sipush 432
      // 26d9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26dc: ldc_w 152.0
      // 26df: ldc_w -5.0
      // 26e2: ldc_w -47.0
      // 26e5: ldc_w 16.0
      // 26e8: ldc_w 16.0
      // 26eb: ldc_w 16.0
      // 26ee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 26f1: dup
      // 26f2: fconst_0
      // 26f3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 26f6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 26f9: bipush 0
      // 26fa: sipush 432
      // 26fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2700: ldc_w 152.0
      // 2703: ldc_w -5.0
      // 2706: ldc_w -31.0
      // 2709: ldc_w 16.0
      // 270c: ldc_w 16.0
      // 270f: ldc_w 16.0
      // 2712: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2715: dup
      // 2716: fconst_0
      // 2717: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 271a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 271d: bipush 0
      // 271e: sipush 432
      // 2721: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2724: ldc_w 168.0
      // 2727: ldc_w -5.0
      // 272a: ldc_w -79.0
      // 272d: ldc_w 16.0
      // 2730: ldc_w 16.0
      // 2733: ldc_w 16.0
      // 2736: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2739: dup
      // 273a: fconst_0
      // 273b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 273e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2741: bipush 0
      // 2742: sipush 350
      // 2745: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2748: ldc_w 136.0
      // 274b: ldc_w -21.0
      // 274e: ldc_w -95.0
      // 2751: ldc_w 16.0
      // 2754: ldc_w 16.0
      // 2757: ldc_w 16.0
      // 275a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 275d: dup
      // 275e: fconst_0
      // 275f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2762: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2765: bipush 0
      // 2766: sipush 350
      // 2769: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 276c: ldc_w 120.0
      // 276f: ldc_w -21.0
      // 2772: ldc_w -95.0
      // 2775: ldc_w 16.0
      // 2778: ldc_w 16.0
      // 277b: ldc_w 16.0
      // 277e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2781: dup
      // 2782: fconst_0
      // 2783: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2786: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2789: bipush 0
      // 278a: sipush 350
      // 278d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2790: ldc_w 168.0
      // 2793: ldc_w -5.0
      // 2796: ldc_w -95.0
      // 2799: ldc_w 16.0
      // 279c: ldc_w 16.0
      // 279f: ldc_w 16.0
      // 27a2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 27a5: dup
      // 27a6: fconst_0
      // 27a7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 27aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27ad: bipush 0
      // 27ae: sipush 350
      // 27b1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27b4: ldc_w 152.0
      // 27b7: ldc_w -21.0
      // 27ba: ldc_w -95.0
      // 27bd: ldc_w 16.0
      // 27c0: ldc_w 16.0
      // 27c3: ldc_w 16.0
      // 27c6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 27c9: dup
      // 27ca: fconst_0
      // 27cb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 27ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27d1: bipush 0
      // 27d2: sipush 432
      // 27d5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27d8: ldc_w 136.0
      // 27db: ldc_w -37.0
      // 27de: ldc_w -79.0
      // 27e1: ldc_w 16.0
      // 27e4: ldc_w 16.0
      // 27e7: ldc_w 16.0
      // 27ea: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 27ed: dup
      // 27ee: fconst_0
      // 27ef: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 27f2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27f5: bipush 0
      // 27f6: sipush 432
      // 27f9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 27fc: ldc_w 104.0
      // 27ff: ldc_w -37.0
      // 2802: ldc_w -63.0
      // 2805: ldc_w 16.0
      // 2808: ldc_w 16.0
      // 280b: ldc_w 16.0
      // 280e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2811: dup
      // 2812: fconst_0
      // 2813: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2816: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2819: bipush 0
      // 281a: sipush 432
      // 281d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2820: ldc_w 88.0
      // 2823: ldc_w -37.0
      // 2826: ldc_w -63.0
      // 2829: ldc_w 16.0
      // 282c: ldc_w 16.0
      // 282f: ldc_w 16.0
      // 2832: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2835: dup
      // 2836: fconst_0
      // 2837: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 283a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 283d: sipush 350
      // 2840: sipush 432
      // 2843: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2846: ldc_w 88.0
      // 2849: ldc_w -37.0
      // 284c: ldc_w -47.0
      // 284f: ldc_w 8.0
      // 2852: ldc_w 16.0
      // 2855: ldc_w 16.0
      // 2858: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 285b: dup
      // 285c: fconst_0
      // 285d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2860: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2863: bipush 0
      // 2864: sipush 432
      // 2867: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 286a: ldc_w 120.0
      // 286d: ldc_w -37.0
      // 2870: ldc_w -63.0
      // 2873: ldc_w 16.0
      // 2876: ldc_w 16.0
      // 2879: ldc_w 16.0
      // 287c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 287f: dup
      // 2880: fconst_0
      // 2881: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2884: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2887: bipush 26
      // 2889: sipush 350
      // 288c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 288f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2892: ldc_w 184.0
      // 2895: ldc_w -21.0
      // 2898: ldc_w -79.0
      // 289b: ldc_w 16.0
      // 289e: ldc_w 16.0
      // 28a1: ldc_w 16.0
      // 28a4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 28a7: dup
      // 28a8: fconst_0
      // 28a9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 28ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28af: bipush 0
      // 28b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28b3: sipush 350
      // 28b6: sipush 350
      // 28b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28bc: ldc_w 200.0
      // 28bf: ldc_w -21.0
      // 28c2: ldc_w -63.0
      // 28c5: ldc_w 16.0
      // 28c8: ldc_w 16.0
      // 28cb: ldc_w 16.0
      // 28ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 28d1: dup
      // 28d2: fconst_0
      // 28d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 28d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28d9: bipush 26
      // 28db: sipush 350
      // 28de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 28e4: ldc_w 200.0
      // 28e7: ldc_w -5.0
      // 28ea: ldc_w -63.0
      // 28ed: ldc_w 16.0
      // 28f0: ldc_w 16.0
      // 28f3: ldc_w 16.0
      // 28f6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 28f9: dup
      // 28fa: fconst_0
      // 28fb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 28fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2901: bipush 0
      // 2902: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2905: bipush 26
      // 2907: sipush 350
      // 290a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 290d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2910: ldc_w 184.0
      // 2913: ldc_w -5.0
      // 2916: ldc_w -79.0
      // 2919: ldc_w 16.0
      // 291c: ldc_w 16.0
      // 291f: ldc_w 16.0
      // 2922: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2925: dup
      // 2926: fconst_0
      // 2927: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 292a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 292d: bipush 0
      // 292e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2931: sipush 434
      // 2934: sipush 350
      // 2937: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 293a: ldc_w 184.0
      // 293d: ldc_w -37.0
      // 2940: ldc_w -63.0
      // 2943: ldc_w 16.0
      // 2946: ldc_w 16.0
      // 2949: ldc_w 16.0
      // 294c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 294f: dup
      // 2950: fconst_0
      // 2951: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2954: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2957: bipush 26
      // 2959: sipush 350
      // 295c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 295f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2962: ldc_w 184.0
      // 2965: ldc_w -21.0
      // 2968: ldc_w -63.0
      // 296b: ldc_w 16.0
      // 296e: ldc_w 16.0
      // 2971: ldc_w 16.0
      // 2974: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2977: dup
      // 2978: fconst_0
      // 2979: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 297c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 297f: bipush 0
      // 2980: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2983: bipush 0
      // 2984: sipush 350
      // 2987: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 298a: ldc_w 152.0
      // 298d: ldc_w -21.0
      // 2990: ldc_w -79.0
      // 2993: ldc_w 16.0
      // 2996: ldc_w 16.0
      // 2999: ldc_w 16.0
      // 299c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 299f: dup
      // 29a0: fconst_0
      // 29a1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 29a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29a7: bipush 0
      // 29a8: sipush 432
      // 29ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29ae: ldc_w 168.0
      // 29b1: ldc_w -5.0
      // 29b4: ldc_w -47.0
      // 29b7: ldc_w 16.0
      // 29ba: ldc_w 16.0
      // 29bd: ldc_w 16.0
      // 29c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 29c3: dup
      // 29c4: fconst_0
      // 29c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 29c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29cb: bipush 0
      // 29cc: sipush 432
      // 29cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29d2: ldc_w 168.0
      // 29d5: ldc_w -5.0
      // 29d8: ldc_w -31.0
      // 29db: ldc_w 16.0
      // 29de: ldc_w 16.0
      // 29e1: ldc_w 16.0
      // 29e4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 29e7: dup
      // 29e8: fconst_0
      // 29e9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 29ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29ef: bipush 0
      // 29f0: sipush 432
      // 29f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 29f6: ldc_w 216.0
      // 29f9: ldc_w -5.0
      // 29fc: ldc_w -31.0
      // 29ff: ldc_w 16.0
      // 2a02: ldc_w 16.0
      // 2a05: ldc_w 16.0
      // 2a08: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a0b: dup
      // 2a0c: fconst_0
      // 2a0d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a10: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a13: bipush 0
      // 2a14: sipush 432
      // 2a17: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a1a: ldc_w 232.0
      // 2a1d: ldc_w -5.0
      // 2a20: ldc_w -31.0
      // 2a23: ldc_w 16.0
      // 2a26: ldc_w 16.0
      // 2a29: ldc_w 16.0
      // 2a2c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a2f: dup
      // 2a30: fconst_0
      // 2a31: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a34: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a37: bipush 0
      // 2a38: sipush 432
      // 2a3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a3e: ldc_w 216.0
      // 2a41: ldc_w -5.0
      // 2a44: ldc_w -63.0
      // 2a47: ldc_w 16.0
      // 2a4a: ldc_w 16.0
      // 2a4d: ldc_w 16.0
      // 2a50: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a53: dup
      // 2a54: fconst_0
      // 2a55: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a58: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a5b: bipush 0
      // 2a5c: sipush 432
      // 2a5f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a62: ldc_w 232.0
      // 2a65: ldc_w 27.0
      // 2a68: ldc_w -63.0
      // 2a6b: ldc_w 16.0
      // 2a6e: ldc_w 16.0
      // 2a71: ldc_w 16.0
      // 2a74: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a77: dup
      // 2a78: fconst_0
      // 2a79: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2a7c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a7f: bipush 0
      // 2a80: sipush 128
      // 2a83: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2a86: ldc_w 248.0
      // 2a89: ldc_w 75.0
      // 2a8c: ldc_w -31.0
      // 2a8f: ldc_w 16.0
      // 2a92: ldc_w 16.0
      // 2a95: ldc_w 16.0
      // 2a98: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2a9b: dup
      // 2a9c: fconst_0
      // 2a9d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2aa0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2aa3: bipush 0
      // 2aa4: sipush 128
      // 2aa7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2aaa: ldc_w 264.0
      // 2aad: ldc_w 91.0
      // 2ab0: ldc_w -31.0
      // 2ab3: ldc_w 16.0
      // 2ab6: ldc_w 16.0
      // 2ab9: ldc_w 16.0
      // 2abc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2abf: dup
      // 2ac0: fconst_0
      // 2ac1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ac4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ac7: bipush 47
      // 2ac9: sipush 128
      // 2acc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2acf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ad2: ldc_w 264.0
      // 2ad5: ldc_w 91.0
      // 2ad8: ldc_w -47.0
      // 2adb: ldc_w 16.0
      // 2ade: ldc_w 16.0
      // 2ae1: ldc_w 16.0
      // 2ae4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ae7: dup
      // 2ae8: fconst_0
      // 2ae9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2aec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2aef: bipush 0
      // 2af0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2af3: bipush 0
      // 2af4: sipush 128
      // 2af7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2afa: ldc_w 280.0
      // 2afd: ldc_w 91.0
      // 2b00: ldc_w -47.0
      // 2b03: ldc_w 16.0
      // 2b06: ldc_w 16.0
      // 2b09: ldc_w 16.0
      // 2b0c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b0f: dup
      // 2b10: fconst_0
      // 2b11: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2b14: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b17: bipush 0
      // 2b18: sipush 432
      // 2b1b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b1e: ldc_w 280.0
      // 2b21: ldc_w 75.0
      // 2b24: ldc_w -47.0
      // 2b27: ldc_w 16.0
      // 2b2a: ldc_w 16.0
      // 2b2d: ldc_w 16.0
      // 2b30: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b33: dup
      // 2b34: fconst_0
      // 2b35: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2b38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b3b: bipush 0
      // 2b3c: sipush 128
      // 2b3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b42: ldc_w 280.0
      // 2b45: ldc_w 107.0
      // 2b48: ldc_w -47.0
      // 2b4b: ldc_w 16.0
      // 2b4e: ldc_w 16.0
      // 2b51: ldc_w 16.0
      // 2b54: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b57: dup
      // 2b58: fconst_0
      // 2b59: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2b5c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b5f: bipush 0
      // 2b60: sipush 128
      // 2b63: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b66: ldc_w 296.0
      // 2b69: ldc_w 107.0
      // 2b6c: ldc_w -47.0
      // 2b6f: ldc_w 16.0
      // 2b72: ldc_w 16.0
      // 2b75: ldc_w 16.0
      // 2b78: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b7b: dup
      // 2b7c: fconst_0
      // 2b7d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2b80: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b83: bipush 0
      // 2b84: sipush 128
      // 2b87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2b8a: ldc_w 296.0
      // 2b8d: ldc_w 91.0
      // 2b90: ldc_w -47.0
      // 2b93: ldc_w 16.0
      // 2b96: ldc_w 16.0
      // 2b99: ldc_w 16.0
      // 2b9c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2b9f: dup
      // 2ba0: fconst_0
      // 2ba1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ba4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ba7: bipush 0
      // 2ba8: sipush 128
      // 2bab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bae: ldc_w 296.0
      // 2bb1: ldc_w 75.0
      // 2bb4: ldc_w -47.0
      // 2bb7: ldc_w 16.0
      // 2bba: ldc_w 16.0
      // 2bbd: ldc_w 16.0
      // 2bc0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2bc3: dup
      // 2bc4: fconst_0
      // 2bc5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2bc8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bcb: bipush 47
      // 2bcd: sipush 128
      // 2bd0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bd3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bd6: ldc_w 296.0
      // 2bd9: ldc_w 59.0
      // 2bdc: ldc_w -47.0
      // 2bdf: ldc_w 16.0
      // 2be2: ldc_w 16.0
      // 2be5: ldc_w 16.0
      // 2be8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2beb: dup
      // 2bec: fconst_0
      // 2bed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2bf0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bf3: bipush 0
      // 2bf4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bf7: bipush 47
      // 2bf9: sipush 128
      // 2bfc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2bff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c02: ldc_w 296.0
      // 2c05: ldc_w 43.0
      // 2c08: ldc_w -47.0
      // 2c0b: ldc_w 16.0
      // 2c0e: ldc_w 16.0
      // 2c11: ldc_w 16.0
      // 2c14: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2c17: dup
      // 2c18: fconst_0
      // 2c19: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2c1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c1f: bipush 0
      // 2c20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c23: bipush 47
      // 2c25: sipush 128
      // 2c28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c2b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c2e: ldc_w 296.0
      // 2c31: ldc_w 27.0
      // 2c34: ldc_w -47.0
      // 2c37: ldc_w 16.0
      // 2c3a: ldc_w 16.0
      // 2c3d: ldc_w 16.0
      // 2c40: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2c43: dup
      // 2c44: fconst_0
      // 2c45: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2c48: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c4b: bipush 0
      // 2c4c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c4f: bipush 47
      // 2c51: sipush 128
      // 2c54: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c57: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c5a: ldc_w 296.0
      // 2c5d: ldc_w 11.0
      // 2c60: ldc_w -47.0
      // 2c63: ldc_w 16.0
      // 2c66: ldc_w 16.0
      // 2c69: ldc_w 16.0
      // 2c6c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2c6f: dup
      // 2c70: fconst_0
      // 2c71: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2c74: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c77: bipush 0
      // 2c78: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c7b: bipush 47
      // 2c7d: sipush 128
      // 2c80: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c83: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2c86: ldc_w 280.0
      // 2c89: ldc_w 11.0
      // 2c8c: ldc_w -47.0
      // 2c8f: ldc_w 16.0
      // 2c92: ldc_w 16.0
      // 2c95: ldc_w 16.0
      // 2c98: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2c9b: dup
      // 2c9c: fconst_0
      // 2c9d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ca0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ca3: bipush 0
      // 2ca4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ca7: bipush 47
      // 2ca9: sipush 128
      // 2cac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2caf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cb2: ldc_w 264.0
      // 2cb5: ldc_w -5.0
      // 2cb8: ldc_w -47.0
      // 2cbb: ldc_w 16.0
      // 2cbe: ldc_w 16.0
      // 2cc1: ldc_w 16.0
      // 2cc4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2cc7: dup
      // 2cc8: fconst_0
      // 2cc9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ccc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ccf: bipush 0
      // 2cd0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cd3: bipush 47
      // 2cd5: sipush 128
      // 2cd8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cdb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cde: ldc_w 280.0
      // 2ce1: ldc_w -5.0
      // 2ce4: ldc_w -47.0
      // 2ce7: ldc_w 16.0
      // 2cea: ldc_w 16.0
      // 2ced: ldc_w 16.0
      // 2cf0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2cf3: dup
      // 2cf4: fconst_0
      // 2cf5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2cf8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cfb: bipush 0
      // 2cfc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2cff: bipush 47
      // 2d01: sipush 352
      // 2d04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d0a: ldc_w 280.0
      // 2d0d: ldc_w -21.0
      // 2d10: ldc_w -47.0
      // 2d13: ldc_w 16.0
      // 2d16: ldc_w 16.0
      // 2d19: ldc_w 16.0
      // 2d1c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d1f: dup
      // 2d20: fconst_0
      // 2d21: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2d24: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d27: bipush 0
      // 2d28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d2b: bipush 47
      // 2d2d: sipush 352
      // 2d30: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d33: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d36: ldc_w 264.0
      // 2d39: ldc_w -21.0
      // 2d3c: ldc_w -47.0
      // 2d3f: ldc_w 16.0
      // 2d42: ldc_w 16.0
      // 2d45: ldc_w 16.0
      // 2d48: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d4b: dup
      // 2d4c: fconst_0
      // 2d4d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2d50: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d53: bipush 0
      // 2d54: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d57: sipush 430
      // 2d5a: sipush 352
      // 2d5d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d60: ldc_w 264.0
      // 2d63: ldc_w -37.0
      // 2d66: ldc_w -47.0
      // 2d69: ldc_w 16.0
      // 2d6c: ldc_w 16.0
      // 2d6f: ldc_w 16.0
      // 2d72: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d75: dup
      // 2d76: fconst_0
      // 2d77: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2d7a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d7d: sipush 430
      // 2d80: sipush 434
      // 2d83: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2d86: ldc_w 264.0
      // 2d89: ldc_w -53.0
      // 2d8c: ldc_w -47.0
      // 2d8f: ldc_w 16.0
      // 2d92: ldc_w 16.0
      // 2d95: ldc_w 16.0
      // 2d98: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2d9b: dup
      // 2d9c: fconst_0
      // 2d9d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2da0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2da3: sipush 430
      // 2da6: sipush 434
      // 2da9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2dac: ldc_w 264.0
      // 2daf: ldc_w -53.0
      // 2db2: ldc_w -31.0
      // 2db5: ldc_w 16.0
      // 2db8: ldc_w 16.0
      // 2dbb: ldc_w 16.0
      // 2dbe: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2dc1: dup
      // 2dc2: fconst_0
      // 2dc3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2dc6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2dc9: sipush 430
      // 2dcc: sipush 434
      // 2dcf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2dd2: ldc_w 264.0
      // 2dd5: ldc_w -53.0
      // 2dd8: ldc_w -15.0
      // 2ddb: ldc_w 16.0
      // 2dde: ldc_w 16.0
      // 2de1: ldc_w 16.0
      // 2de4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2de7: dup
      // 2de8: fconst_0
      // 2de9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2dec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2def: sipush 430
      // 2df2: sipush 434
      // 2df5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2df8: ldc_w 264.0
      // 2dfb: ldc_w -37.0
      // 2dfe: ldc_w -15.0
      // 2e01: ldc_w 16.0
      // 2e04: ldc_w 16.0
      // 2e07: ldc_w 16.0
      // 2e0a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e0d: dup
      // 2e0e: fconst_0
      // 2e0f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e12: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e15: bipush 0
      // 2e16: sipush 128
      // 2e19: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e1c: ldc_w 296.0
      // 2e1f: ldc_w 11.0
      // 2e22: ldc_w -31.0
      // 2e25: ldc_w 16.0
      // 2e28: ldc_w 16.0
      // 2e2b: ldc_w 16.0
      // 2e2e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e31: dup
      // 2e32: fconst_0
      // 2e33: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e36: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e39: bipush 0
      // 2e3a: sipush 128
      // 2e3d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e40: ldc_w 296.0
      // 2e43: ldc_w 27.0
      // 2e46: ldc_w -15.0
      // 2e49: ldc_w 16.0
      // 2e4c: ldc_w 16.0
      // 2e4f: ldc_w 16.0
      // 2e52: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e55: dup
      // 2e56: fconst_0
      // 2e57: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e5a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e5d: bipush 0
      // 2e5e: sipush 128
      // 2e61: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e64: ldc_w 296.0
      // 2e67: ldc_w 43.0
      // 2e6a: ldc_w -15.0
      // 2e6d: ldc_w 16.0
      // 2e70: ldc_w 16.0
      // 2e73: ldc_w 16.0
      // 2e76: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e79: dup
      // 2e7a: fconst_0
      // 2e7b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2e7e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e81: bipush 0
      // 2e82: sipush 128
      // 2e85: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2e88: ldc_w 296.0
      // 2e8b: ldc_w 43.0
      // 2e8e: fconst_1
      // 2e8f: ldc_w 16.0
      // 2e92: ldc_w 16.0
      // 2e95: ldc_w 16.0
      // 2e98: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2e9b: dup
      // 2e9c: fconst_0
      // 2e9d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ea0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ea3: bipush 0
      // 2ea4: sipush 128
      // 2ea7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2eaa: ldc_w 296.0
      // 2ead: ldc_w 59.0
      // 2eb0: fconst_1
      // 2eb1: ldc_w 16.0
      // 2eb4: ldc_w 16.0
      // 2eb7: ldc_w 16.0
      // 2eba: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2ebd: dup
      // 2ebe: fconst_0
      // 2ebf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ec2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ec5: bipush 0
      // 2ec6: sipush 128
      // 2ec9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ecc: ldc_w 296.0
      // 2ecf: ldc_w 75.0
      // 2ed2: fconst_1
      // 2ed3: ldc_w 16.0
      // 2ed6: ldc_w 16.0
      // 2ed9: ldc_w 16.0
      // 2edc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2edf: dup
      // 2ee0: fconst_0
      // 2ee1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2ee4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2ee7: bipush 0
      // 2ee8: sipush 128
      // 2eeb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2eee: ldc_w 296.0
      // 2ef1: ldc_w 27.0
      // 2ef4: ldc_w -31.0
      // 2ef7: ldc_w 16.0
      // 2efa: ldc_w 16.0
      // 2efd: ldc_w 16.0
      // 2f00: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f03: dup
      // 2f04: fconst_0
      // 2f05: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f08: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f0b: bipush 0
      // 2f0c: sipush 128
      // 2f0f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f12: ldc_w 296.0
      // 2f15: ldc_w 11.0
      // 2f18: ldc_w -15.0
      // 2f1b: ldc_w 16.0
      // 2f1e: ldc_w 16.0
      // 2f21: ldc_w 16.0
      // 2f24: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f27: dup
      // 2f28: fconst_0
      // 2f29: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f2c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f2f: bipush 0
      // 2f30: sipush 128
      // 2f33: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f36: ldc_w 296.0
      // 2f39: ldc_w -5.0
      // 2f3c: ldc_w -31.0
      // 2f3f: ldc_w 16.0
      // 2f42: ldc_w 16.0
      // 2f45: ldc_w 16.0
      // 2f48: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f4b: dup
      // 2f4c: fconst_0
      // 2f4d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f50: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f53: bipush 0
      // 2f54: sipush 128
      // 2f57: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f5a: ldc_w 296.0
      // 2f5d: ldc_w -5.0
      // 2f60: ldc_w -63.0
      // 2f63: ldc_w 16.0
      // 2f66: ldc_w 16.0
      // 2f69: ldc_w 16.0
      // 2f6c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f6f: dup
      // 2f70: fconst_0
      // 2f71: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f74: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f77: bipush 0
      // 2f78: sipush 128
      // 2f7b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f7e: ldc_w 312.0
      // 2f81: ldc_w -21.0
      // 2f84: ldc_w -31.0
      // 2f87: ldc_w 16.0
      // 2f8a: ldc_w 16.0
      // 2f8d: ldc_w 16.0
      // 2f90: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2f93: dup
      // 2f94: fconst_0
      // 2f95: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2f98: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2f9b: bipush 0
      // 2f9c: sipush 128
      // 2f9f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fa2: ldc_w 280.0
      // 2fa5: ldc_w -21.0
      // 2fa8: ldc_w -31.0
      // 2fab: ldc_w 16.0
      // 2fae: ldc_w 16.0
      // 2fb1: ldc_w 16.0
      // 2fb4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2fb7: dup
      // 2fb8: fconst_0
      // 2fb9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2fbc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fbf: bipush 0
      // 2fc0: sipush 128
      // 2fc3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fc6: ldc_w 280.0
      // 2fc9: ldc_w -21.0
      // 2fcc: ldc_w -15.0
      // 2fcf: ldc_w 16.0
      // 2fd2: ldc_w 16.0
      // 2fd5: ldc_w 16.0
      // 2fd8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2fdb: dup
      // 2fdc: fconst_0
      // 2fdd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 2fe0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fe3: bipush 0
      // 2fe4: sipush 128
      // 2fe7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 2fea: ldc_w 280.0
      // 2fed: ldc_w -5.0
      // 2ff0: ldc_w -15.0
      // 2ff3: ldc_w 16.0
      // 2ff6: ldc_w 16.0
      // 2ff9: ldc_w 16.0
      // 2ffc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 2fff: dup
      // 3000: fconst_0
      // 3001: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3004: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3007: bipush 0
      // 3008: sipush 128
      // 300b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 300e: ldc_w 280.0
      // 3011: ldc_w -37.0
      // 3014: ldc_w -31.0
      // 3017: ldc_w 16.0
      // 301a: ldc_w 16.0
      // 301d: ldc_w 16.0
      // 3020: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3023: dup
      // 3024: fconst_0
      // 3025: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3028: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 302b: bipush 0
      // 302c: sipush 128
      // 302f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3032: ldc_w 264.0
      // 3035: ldc_w -37.0
      // 3038: fconst_1
      // 3039: ldc_w 16.0
      // 303c: ldc_w 16.0
      // 303f: ldc_w 16.0
      // 3042: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3045: dup
      // 3046: fconst_0
      // 3047: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 304a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 304d: bipush 0
      // 304e: sipush 128
      // 3051: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3054: ldc_w 264.0
      // 3057: ldc_w -21.0
      // 305a: fconst_1
      // 305b: ldc_w 16.0
      // 305e: ldc_w 16.0
      // 3061: ldc_w 16.0
      // 3064: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3067: dup
      // 3068: fconst_0
      // 3069: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 306c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 306f: bipush 0
      // 3070: sipush 128
      // 3073: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3076: ldc_w 264.0
      // 3079: ldc_w -5.0
      // 307c: fconst_1
      // 307d: ldc_w 16.0
      // 3080: ldc_w 16.0
      // 3083: ldc_w 16.0
      // 3086: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3089: dup
      // 308a: fconst_0
      // 308b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 308e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3091: bipush 0
      // 3092: sipush 128
      // 3095: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3098: ldc_w 264.0
      // 309b: ldc_w -37.0
      // 309e: ldc_w 17.0
      // 30a1: ldc_w 16.0
      // 30a4: ldc_w 16.0
      // 30a7: ldc_w 16.0
      // 30aa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 30ad: dup
      // 30ae: fconst_0
      // 30af: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 30b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30b5: bipush 0
      // 30b6: sipush 128
      // 30b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30bc: ldc_w 280.0
      // 30bf: ldc_w 107.0
      // 30c2: ldc_w -31.0
      // 30c5: ldc_w 16.0
      // 30c8: ldc_w 16.0
      // 30cb: ldc_w 16.0
      // 30ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 30d1: dup
      // 30d2: fconst_0
      // 30d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 30d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30d9: bipush 0
      // 30da: sipush 128
      // 30dd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30e0: ldc_w 296.0
      // 30e3: ldc_w 123.0
      // 30e6: ldc_w -31.0
      // 30e9: ldc_w 16.0
      // 30ec: ldc_w 16.0
      // 30ef: ldc_w 16.0
      // 30f2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 30f5: dup
      // 30f6: fconst_0
      // 30f7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 30fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 30fd: bipush 0
      // 30fe: sipush 128
      // 3101: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3104: ldc_w 312.0
      // 3107: ldc_w 139.0
      // 310a: ldc_w -31.0
      // 310d: ldc_w 16.0
      // 3110: ldc_w 16.0
      // 3113: ldc_w 16.0
      // 3116: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3119: dup
      // 311a: fconst_0
      // 311b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 311e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3121: bipush 0
      // 3122: sipush 128
      // 3125: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3128: ldc_w 312.0
      // 312b: ldc_w 123.0
      // 312e: ldc_w -31.0
      // 3131: ldc_w 16.0
      // 3134: ldc_w 16.0
      // 3137: ldc_w 16.0
      // 313a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 313d: dup
      // 313e: fconst_0
      // 313f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3142: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3145: bipush 0
      // 3146: sipush 128
      // 3149: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 314c: ldc_w 312.0
      // 314f: ldc_w 107.0
      // 3152: ldc_w -31.0
      // 3155: ldc_w 16.0
      // 3158: ldc_w 16.0
      // 315b: ldc_w 16.0
      // 315e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3161: dup
      // 3162: fconst_0
      // 3163: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3166: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3169: bipush 0
      // 316a: sipush 128
      // 316d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3170: ldc_w 312.0
      // 3173: ldc_w 91.0
      // 3176: ldc_w -31.0
      // 3179: ldc_w 16.0
      // 317c: ldc_w 16.0
      // 317f: ldc_w 16.0
      // 3182: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3185: dup
      // 3186: fconst_0
      // 3187: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 318a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 318d: bipush 0
      // 318e: sipush 128
      // 3191: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3194: ldc_w 312.0
      // 3197: ldc_w 75.0
      // 319a: ldc_w -31.0
      // 319d: ldc_w 16.0
      // 31a0: ldc_w 16.0
      // 31a3: ldc_w 16.0
      // 31a6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 31a9: dup
      // 31aa: fconst_0
      // 31ab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 31ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31b1: bipush 0
      // 31b2: sipush 128
      // 31b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31b8: ldc_w 312.0
      // 31bb: ldc_w 59.0
      // 31be: ldc_w -31.0
      // 31c1: ldc_w 16.0
      // 31c4: ldc_w 16.0
      // 31c7: ldc_w 16.0
      // 31ca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 31cd: dup
      // 31ce: fconst_0
      // 31cf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 31d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31d5: bipush 0
      // 31d6: sipush 128
      // 31d9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31dc: ldc_w 312.0
      // 31df: ldc_w 43.0
      // 31e2: ldc_w -31.0
      // 31e5: ldc_w 16.0
      // 31e8: ldc_w 16.0
      // 31eb: ldc_w 16.0
      // 31ee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 31f1: dup
      // 31f2: fconst_0
      // 31f3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 31f6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 31f9: bipush 0
      // 31fa: sipush 128
      // 31fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3200: ldc_w 312.0
      // 3203: ldc_w 107.0
      // 3206: ldc_w -15.0
      // 3209: ldc_w 16.0
      // 320c: ldc_w 16.0
      // 320f: ldc_w 16.0
      // 3212: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3215: dup
      // 3216: fconst_0
      // 3217: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 321a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 321d: bipush 0
      // 321e: sipush 128
      // 3221: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3224: ldc_w 312.0
      // 3227: ldc_w 75.0
      // 322a: ldc_w -15.0
      // 322d: ldc_w 16.0
      // 3230: ldc_w 16.0
      // 3233: ldc_w 16.0
      // 3236: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3239: dup
      // 323a: fconst_0
      // 323b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 323e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3241: bipush 0
      // 3242: sipush 128
      // 3245: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3248: ldc_w 312.0
      // 324b: ldc_w 59.0
      // 324e: ldc_w -15.0
      // 3251: ldc_w 16.0
      // 3254: ldc_w 16.0
      // 3257: ldc_w 16.0
      // 325a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 325d: dup
      // 325e: fconst_0
      // 325f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3262: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3265: bipush 0
      // 3266: sipush 128
      // 3269: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 326c: ldc_w 312.0
      // 326f: ldc_w 91.0
      // 3272: ldc_w -15.0
      // 3275: ldc_w 16.0
      // 3278: ldc_w 16.0
      // 327b: ldc_w 16.0
      // 327e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3281: dup
      // 3282: fconst_0
      // 3283: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3286: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3289: bipush 0
      // 328a: sipush 128
      // 328d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3290: ldc_w 312.0
      // 3293: ldc_w 139.0
      // 3296: ldc_w -15.0
      // 3299: ldc_w 16.0
      // 329c: ldc_w 16.0
      // 329f: ldc_w 16.0
      // 32a2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 32a5: dup
      // 32a6: fconst_0
      // 32a7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 32aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32ad: bipush 0
      // 32ae: sipush 128
      // 32b1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32b4: ldc_w 312.0
      // 32b7: ldc_w 123.0
      // 32ba: fconst_1
      // 32bb: ldc_w 16.0
      // 32be: ldc_w 16.0
      // 32c1: ldc_w 16.0
      // 32c4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 32c7: dup
      // 32c8: fconst_0
      // 32c9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 32cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32cf: bipush 0
      // 32d0: sipush 128
      // 32d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32d6: ldc_w 296.0
      // 32d9: ldc_w 107.0
      // 32dc: fconst_1
      // 32dd: ldc_w 16.0
      // 32e0: ldc_w 16.0
      // 32e3: ldc_w 16.0
      // 32e6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 32e9: dup
      // 32ea: fconst_0
      // 32eb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 32ee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32f1: bipush 0
      // 32f2: sipush 128
      // 32f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 32f8: ldc_w 296.0
      // 32fb: ldc_w 91.0
      // 32fe: fconst_1
      // 32ff: ldc_w 16.0
      // 3302: ldc_w 16.0
      // 3305: ldc_w 16.0
      // 3308: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 330b: dup
      // 330c: fconst_0
      // 330d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3310: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3313: bipush 0
      // 3314: sipush 128
      // 3317: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 331a: ldc_w 312.0
      // 331d: ldc_w 107.0
      // 3320: fconst_1
      // 3321: ldc_w 16.0
      // 3324: ldc_w 16.0
      // 3327: ldc_w 16.0
      // 332a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 332d: dup
      // 332e: fconst_0
      // 332f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3332: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3335: bipush 0
      // 3336: sipush 128
      // 3339: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 333c: ldc_w 312.0
      // 333f: ldc_w 91.0
      // 3342: fconst_1
      // 3343: ldc_w 16.0
      // 3346: ldc_w 16.0
      // 3349: ldc_w 16.0
      // 334c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 334f: dup
      // 3350: fconst_0
      // 3351: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3354: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3357: bipush 0
      // 3358: sipush 128
      // 335b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 335e: ldc_w 296.0
      // 3361: ldc_w 123.0
      // 3364: ldc_w -15.0
      // 3367: ldc_w 16.0
      // 336a: ldc_w 16.0
      // 336d: ldc_w 16.0
      // 3370: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3373: dup
      // 3374: fconst_0
      // 3375: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3378: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 337b: bipush 0
      // 337c: sipush 128
      // 337f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3382: ldc_w 280.0
      // 3385: ldc_w 107.0
      // 3388: ldc_w -15.0
      // 338b: ldc_w 16.0
      // 338e: ldc_w 16.0
      // 3391: ldc_w 16.0
      // 3394: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3397: dup
      // 3398: fconst_0
      // 3399: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 339c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 339f: bipush 0
      // 33a0: sipush 128
      // 33a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33a6: ldc_w 280.0
      // 33a9: ldc_w 91.0
      // 33ac: ldc_w -15.0
      // 33af: ldc_w 16.0
      // 33b2: ldc_w 16.0
      // 33b5: ldc_w 16.0
      // 33b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 33bb: dup
      // 33bc: fconst_0
      // 33bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 33c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33c3: bipush 0
      // 33c4: sipush 128
      // 33c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33ca: ldc_w 280.0
      // 33cd: ldc_w 91.0
      // 33d0: fconst_1
      // 33d1: ldc_w 16.0
      // 33d4: ldc_w 16.0
      // 33d7: ldc_w 16.0
      // 33da: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 33dd: dup
      // 33de: fconst_0
      // 33df: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 33e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33e5: bipush 0
      // 33e6: sipush 128
      // 33e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 33ec: ldc_w 280.0
      // 33ef: ldc_w 75.0
      // 33f2: fconst_1
      // 33f3: ldc_w 16.0
      // 33f6: ldc_w 16.0
      // 33f9: ldc_w 16.0
      // 33fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 33ff: dup
      // 3400: fconst_0
      // 3401: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3404: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3407: bipush 0
      // 3408: sipush 128
      // 340b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 340e: ldc_w 280.0
      // 3411: ldc_w 59.0
      // 3414: fconst_1
      // 3415: ldc_w 16.0
      // 3418: ldc_w 16.0
      // 341b: ldc_w 16.0
      // 341e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3421: dup
      // 3422: fconst_0
      // 3423: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3426: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3429: bipush 0
      // 342a: sipush 128
      // 342d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3430: ldc_w 280.0
      // 3433: ldc_w 27.0
      // 3436: fconst_1
      // 3437: ldc_w 16.0
      // 343a: ldc_w 16.0
      // 343d: ldc_w 16.0
      // 3440: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3443: dup
      // 3444: fconst_0
      // 3445: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3448: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 344b: bipush 0
      // 344c: sipush 128
      // 344f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3452: ldc_w 280.0
      // 3455: ldc_w 11.0
      // 3458: fconst_1
      // 3459: ldc_w 16.0
      // 345c: ldc_w 16.0
      // 345f: ldc_w 16.0
      // 3462: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3465: dup
      // 3466: fconst_0
      // 3467: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 346a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 346d: bipush 0
      // 346e: sipush 128
      // 3471: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3474: ldc_w 280.0
      // 3477: ldc_w -5.0
      // 347a: ldc_w 17.0
      // 347d: ldc_w 16.0
      // 3480: ldc_w 16.0
      // 3483: ldc_w 16.0
      // 3486: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3489: dup
      // 348a: fconst_0
      // 348b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 348e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3491: bipush 0
      // 3492: sipush 128
      // 3495: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3498: ldc_w 280.0
      // 349b: ldc_w 43.0
      // 349e: fconst_1
      // 349f: ldc_w 16.0
      // 34a2: ldc_w 16.0
      // 34a5: ldc_w 16.0
      // 34a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 34ab: dup
      // 34ac: fconst_0
      // 34ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 34b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34b3: bipush 0
      // 34b4: sipush 128
      // 34b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34ba: ldc_w 232.0
      // 34bd: ldc_w 59.0
      // 34c0: ldc_w -31.0
      // 34c3: ldc_w 16.0
      // 34c6: ldc_w 16.0
      // 34c9: ldc_w 16.0
      // 34cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 34cf: dup
      // 34d0: fconst_0
      // 34d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 34d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34d7: bipush 0
      // 34d8: sipush 128
      // 34db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34de: ldc_w 248.0
      // 34e1: ldc_w 59.0
      // 34e4: fconst_1
      // 34e5: ldc_w 16.0
      // 34e8: ldc_w 16.0
      // 34eb: ldc_w 16.0
      // 34ee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 34f1: dup
      // 34f2: fconst_0
      // 34f3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 34f6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 34f9: bipush 0
      // 34fa: sipush 128
      // 34fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3500: ldc_w 248.0
      // 3503: ldc_w 59.0
      // 3506: ldc_w -15.0
      // 3509: ldc_w 16.0
      // 350c: ldc_w 16.0
      // 350f: ldc_w 16.0
      // 3512: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3515: dup
      // 3516: fconst_0
      // 3517: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 351a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 351d: bipush 0
      // 351e: sipush 128
      // 3521: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3524: ldc_w 232.0
      // 3527: ldc_w 59.0
      // 352a: fconst_1
      // 352b: ldc_w 16.0
      // 352e: ldc_w 16.0
      // 3531: ldc_w 16.0
      // 3534: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3537: dup
      // 3538: fconst_0
      // 3539: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 353c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 353f: bipush 0
      // 3540: sipush 128
      // 3543: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3546: ldc_w 232.0
      // 3549: ldc_w 59.0
      // 354c: ldc_w -15.0
      // 354f: ldc_w 16.0
      // 3552: ldc_w 16.0
      // 3555: ldc_w 16.0
      // 3558: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 355b: dup
      // 355c: fconst_0
      // 355d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3560: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3563: bipush 0
      // 3564: sipush 128
      // 3567: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 356a: ldc_w 232.0
      // 356d: ldc_w 59.0
      // 3570: ldc_w -63.0
      // 3573: ldc_w 16.0
      // 3576: ldc_w 16.0
      // 3579: ldc_w 16.0
      // 357c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 357f: dup
      // 3580: fconst_0
      // 3581: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3584: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3587: bipush 0
      // 3588: sipush 128
      // 358b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 358e: ldc_w 232.0
      // 3591: ldc_w 59.0
      // 3594: ldc_w -79.0
      // 3597: ldc_w 16.0
      // 359a: ldc_w 16.0
      // 359d: ldc_w 16.0
      // 35a0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 35a3: dup
      // 35a4: fconst_0
      // 35a5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 35a8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35ab: bipush 0
      // 35ac: sipush 128
      // 35af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35b2: ldc_w 232.0
      // 35b5: ldc_w 59.0
      // 35b8: ldc_w -95.0
      // 35bb: ldc_w 16.0
      // 35be: ldc_w 16.0
      // 35c1: ldc_w 16.0
      // 35c4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 35c7: dup
      // 35c8: fconst_0
      // 35c9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 35cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35cf: bipush 0
      // 35d0: sipush 128
      // 35d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35d6: ldc_w 248.0
      // 35d9: ldc_w 59.0
      // 35dc: ldc_w -63.0
      // 35df: ldc_w 16.0
      // 35e2: ldc_w 16.0
      // 35e5: ldc_w 16.0
      // 35e8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 35eb: dup
      // 35ec: fconst_0
      // 35ed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 35f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35f3: bipush 0
      // 35f4: sipush 128
      // 35f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 35fa: ldc_w 248.0
      // 35fd: ldc_w 59.0
      // 3600: ldc_w -79.0
      // 3603: ldc_w 16.0
      // 3606: ldc_w 16.0
      // 3609: ldc_w 16.0
      // 360c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 360f: dup
      // 3610: fconst_0
      // 3611: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3614: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3617: bipush 0
      // 3618: sipush 128
      // 361b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 361e: ldc_w 248.0
      // 3621: ldc_w 59.0
      // 3624: ldc_w -95.0
      // 3627: ldc_w 16.0
      // 362a: ldc_w 16.0
      // 362d: ldc_w 16.0
      // 3630: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3633: dup
      // 3634: fconst_0
      // 3635: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3638: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 363b: bipush 0
      // 363c: sipush 128
      // 363f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3642: ldc_w 232.0
      // 3645: ldc_w -85.0
      // 3648: ldc_w -79.0
      // 364b: ldc_w 16.0
      // 364e: ldc_w 16.0
      // 3651: ldc_w 16.0
      // 3654: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3657: dup
      // 3658: fconst_0
      // 3659: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 365c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 365f: bipush 0
      // 3660: sipush 128
      // 3663: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3666: ldc_w 280.0
      // 3669: ldc_w -69.0
      // 366c: fconst_1
      // 366d: ldc_w 16.0
      // 3670: ldc_w 16.0
      // 3673: ldc_w 16.0
      // 3676: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3679: dup
      // 367a: fconst_0
      // 367b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 367e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3681: bipush 0
      // 3682: sipush 128
      // 3685: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3688: ldc_w 232.0
      // 368b: ldc_w -85.0
      // 368e: fconst_1
      // 368f: ldc_w 16.0
      // 3692: ldc_w 16.0
      // 3695: ldc_w 16.0
      // 3698: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 369b: dup
      // 369c: fconst_0
      // 369d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 36a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36a3: bipush 0
      // 36a4: sipush 128
      // 36a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36aa: ldc_w 232.0
      // 36ad: ldc_w -53.0
      // 36b0: ldc_w 33.0
      // 36b3: ldc_w 16.0
      // 36b6: ldc_w 16.0
      // 36b9: ldc_w 16.0
      // 36bc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 36bf: dup
      // 36c0: fconst_0
      // 36c1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 36c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36c7: bipush 0
      // 36c8: sipush 128
      // 36cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36ce: ldc_w 232.0
      // 36d1: ldc_w -37.0
      // 36d4: ldc_w 49.0
      // 36d7: ldc_w 16.0
      // 36da: ldc_w 16.0
      // 36dd: ldc_w 16.0
      // 36e0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 36e3: dup
      // 36e4: fconst_0
      // 36e5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 36e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36eb: bipush 0
      // 36ec: sipush 128
      // 36ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 36f2: ldc_w 168.0
      // 36f5: ldc_w -21.0
      // 36f8: ldc_w 49.0
      // 36fb: ldc_w 16.0
      // 36fe: ldc_w 16.0
      // 3701: ldc_w 16.0
      // 3704: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3707: dup
      // 3708: fconst_0
      // 3709: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 370c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 370f: bipush 0
      // 3710: sipush 128
      // 3713: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3716: ldc_w 232.0
      // 3719: ldc_w -101.0
      // 371c: ldc_w -15.0
      // 371f: ldc_w 16.0
      // 3722: ldc_w 16.0
      // 3725: ldc_w 16.0
      // 3728: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 372b: dup
      // 372c: fconst_0
      // 372d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3730: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3733: bipush 0
      // 3734: sipush 128
      // 3737: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 373a: ldc_w 232.0
      // 373d: ldc_w -101.0
      // 3740: ldc_w -31.0
      // 3743: ldc_w 16.0
      // 3746: ldc_w 16.0
      // 3749: ldc_w 16.0
      // 374c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 374f: dup
      // 3750: fconst_0
      // 3751: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3754: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3757: bipush 0
      // 3758: sipush 128
      // 375b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 375e: ldc_w 216.0
      // 3761: ldc_w -101.0
      // 3764: ldc_w -31.0
      // 3767: ldc_w 16.0
      // 376a: ldc_w 16.0
      // 376d: ldc_w 16.0
      // 3770: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3773: dup
      // 3774: fconst_0
      // 3775: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3778: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 377b: bipush 0
      // 377c: sipush 128
      // 377f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3782: ldc_w 216.0
      // 3785: ldc_w -101.0
      // 3788: fconst_1
      // 3789: ldc_w 16.0
      // 378c: ldc_w 16.0
      // 378f: ldc_w 16.0
      // 3792: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3795: dup
      // 3796: fconst_0
      // 3797: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 379a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 379d: bipush 0
      // 379e: sipush 128
      // 37a1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37a4: ldc_w 216.0
      // 37a7: ldc_w -85.0
      // 37aa: ldc_w 17.0
      // 37ad: ldc_w 16.0
      // 37b0: ldc_w 16.0
      // 37b3: ldc_w 16.0
      // 37b6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 37b9: dup
      // 37ba: fconst_0
      // 37bb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 37be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37c1: bipush 0
      // 37c2: sipush 128
      // 37c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37c8: ldc_w 200.0
      // 37cb: ldc_w -85.0
      // 37ce: ldc_w 17.0
      // 37d1: ldc_w 16.0
      // 37d4: ldc_w 16.0
      // 37d7: ldc_w 16.0
      // 37da: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 37dd: dup
      // 37de: fconst_0
      // 37df: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 37e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37e5: bipush 0
      // 37e6: sipush 128
      // 37e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 37ec: ldc_w 200.0
      // 37ef: ldc_w -85.0
      // 37f2: fconst_1
      // 37f3: ldc_w 16.0
      // 37f6: ldc_w 16.0
      // 37f9: ldc_w 16.0
      // 37fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 37ff: dup
      // 3800: fconst_0
      // 3801: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3804: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3807: bipush 0
      // 3808: sipush 128
      // 380b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 380e: ldc_w 216.0
      // 3811: ldc_w -85.0
      // 3814: ldc_w 33.0
      // 3817: ldc_w 16.0
      // 381a: ldc_w 16.0
      // 381d: ldc_w 16.0
      // 3820: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3823: dup
      // 3824: fconst_0
      // 3825: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3828: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 382b: bipush 0
      // 382c: sipush 128
      // 382f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3832: ldc_w 216.0
      // 3835: ldc_w -85.0
      // 3838: ldc_w 49.0
      // 383b: ldc_w 16.0
      // 383e: ldc_w 16.0
      // 3841: ldc_w 16.0
      // 3844: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3847: dup
      // 3848: fconst_0
      // 3849: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 384c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 384f: bipush 0
      // 3850: sipush 128
      // 3853: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3856: ldc_w 216.0
      // 3859: ldc_w -69.0
      // 385c: ldc_w 33.0
      // 385f: ldc_w 16.0
      // 3862: ldc_w 16.0
      // 3865: ldc_w 16.0
      // 3868: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 386b: dup
      // 386c: fconst_0
      // 386d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3870: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3873: bipush 0
      // 3874: sipush 128
      // 3877: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 387a: ldc_w 184.0
      // 387d: ldc_w -69.0
      // 3880: ldc_w 33.0
      // 3883: ldc_w 16.0
      // 3886: ldc_w 16.0
      // 3889: ldc_w 16.0
      // 388c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 388f: dup
      // 3890: fconst_0
      // 3891: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3894: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3897: bipush 0
      // 3898: sipush 128
      // 389b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 389e: ldc_w 168.0
      // 38a1: ldc_w -69.0
      // 38a4: ldc_w 33.0
      // 38a7: ldc_w 16.0
      // 38aa: ldc_w 16.0
      // 38ad: ldc_w 16.0
      // 38b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 38b3: dup
      // 38b4: fconst_0
      // 38b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 38b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38bb: bipush 0
      // 38bc: sipush 128
      // 38bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38c2: ldc_w 200.0
      // 38c5: ldc_w -69.0
      // 38c8: ldc_w 33.0
      // 38cb: ldc_w 16.0
      // 38ce: ldc_w 16.0
      // 38d1: ldc_w 16.0
      // 38d4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 38d7: dup
      // 38d8: fconst_0
      // 38d9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 38dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38df: bipush 0
      // 38e0: sipush 128
      // 38e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 38e6: ldc_w 216.0
      // 38e9: ldc_w -53.0
      // 38ec: ldc_w 33.0
      // 38ef: ldc_w 16.0
      // 38f2: ldc_w 16.0
      // 38f5: ldc_w 16.0
      // 38f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 38fb: dup
      // 38fc: fconst_0
      // 38fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3900: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3903: bipush 0
      // 3904: sipush 128
      // 3907: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 390a: ldc_w 216.0
      // 390d: ldc_w -101.0
      // 3910: ldc_w -15.0
      // 3913: ldc_w 16.0
      // 3916: ldc_w 16.0
      // 3919: ldc_w 16.0
      // 391c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 391f: dup
      // 3920: fconst_0
      // 3921: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3924: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3927: bipush 0
      // 3928: sipush 128
      // 392b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 392e: ldc_w 152.0
      // 3931: ldc_w 59.0
      // 3934: ldc_w -15.0
      // 3937: ldc_w 16.0
      // 393a: ldc_w 16.0
      // 393d: ldc_w 16.0
      // 3940: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3943: dup
      // 3944: fconst_0
      // 3945: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3948: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 394b: bipush 0
      // 394c: sipush 128
      // 394f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3952: ldc_w 184.0
      // 3955: ldc_w 59.0
      // 3958: ldc_w -15.0
      // 395b: ldc_w 16.0
      // 395e: ldc_w 16.0
      // 3961: ldc_w 16.0
      // 3964: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3967: dup
      // 3968: fconst_0
      // 3969: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 396c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 396f: bipush 0
      // 3970: sipush 128
      // 3973: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3976: ldc_w 168.0
      // 3979: ldc_w 59.0
      // 397c: ldc_w -15.0
      // 397f: ldc_w 16.0
      // 3982: ldc_w 16.0
      // 3985: ldc_w 16.0
      // 3988: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 398b: dup
      // 398c: fconst_0
      // 398d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3990: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3993: bipush 0
      // 3994: sipush 128
      // 3997: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 399a: ldc_w 104.0
      // 399d: ldc_w 43.0
      // 39a0: fconst_1
      // 39a1: ldc_w 16.0
      // 39a4: ldc_w 16.0
      // 39a7: ldc_w 16.0
      // 39aa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 39ad: dup
      // 39ae: fconst_0
      // 39af: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 39b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39b5: bipush 0
      // 39b6: sipush 128
      // 39b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39bc: ldc_w 8.0
      // 39bf: ldc_w 27.0
      // 39c2: ldc_w -47.0
      // 39c5: ldc_w 16.0
      // 39c8: ldc_w 16.0
      // 39cb: ldc_w 16.0
      // 39ce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 39d1: dup
      // 39d2: fconst_0
      // 39d3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 39d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39d9: bipush 0
      // 39da: sipush 128
      // 39dd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39e0: ldc_w -8.0
      // 39e3: ldc_w -37.0
      // 39e6: ldc_w -47.0
      // 39e9: ldc_w 16.0
      // 39ec: ldc_w 16.0
      // 39ef: ldc_w 16.0
      // 39f2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 39f5: dup
      // 39f6: fconst_0
      // 39f7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 39fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 39fd: bipush 0
      // 39fe: sipush 128
      // 3a01: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a04: ldc_w -8.0
      // 3a07: ldc_w -53.0
      // 3a0a: ldc_w -31.0
      // 3a0d: ldc_w 16.0
      // 3a10: ldc_w 16.0
      // 3a13: ldc_w 16.0
      // 3a16: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3a19: dup
      // 3a1a: fconst_0
      // 3a1b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3a1e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a21: bipush 0
      // 3a22: sipush 352
      // 3a25: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a28: ldc_w -8.0
      // 3a2b: ldc_w -53.0
      // 3a2e: fconst_1
      // 3a2f: ldc_w 16.0
      // 3a32: ldc_w 16.0
      // 3a35: ldc_w 16.0
      // 3a38: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3a3b: dup
      // 3a3c: fconst_0
      // 3a3d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3a40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a43: bipush 0
      // 3a44: sipush 352
      // 3a47: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a4a: ldc_w 8.0
      // 3a4d: ldc_w -69.0
      // 3a50: ldc_w 49.0
      // 3a53: ldc_w 16.0
      // 3a56: ldc_w 16.0
      // 3a59: ldc_w 16.0
      // 3a5c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3a5f: dup
      // 3a60: fconst_0
      // 3a61: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3a64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a67: bipush 0
      // 3a68: sipush 352
      // 3a6b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a6e: ldc_w -8.0
      // 3a71: ldc_w -37.0
      // 3a74: ldc_w 65.0
      // 3a77: ldc_w 16.0
      // 3a7a: ldc_w 16.0
      // 3a7d: ldc_w 16.0
      // 3a80: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3a83: dup
      // 3a84: fconst_0
      // 3a85: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3a88: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a8b: bipush 0
      // 3a8c: sipush 352
      // 3a8f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3a92: ldc_w -8.0
      // 3a95: ldc_w -53.0
      // 3a98: ldc_w 81.0
      // 3a9b: ldc_w 16.0
      // 3a9e: ldc_w 16.0
      // 3aa1: ldc_w 16.0
      // 3aa4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3aa7: dup
      // 3aa8: fconst_0
      // 3aa9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3aac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3aaf: bipush 0
      // 3ab0: sipush 352
      // 3ab3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ab6: ldc_w 8.0
      // 3ab9: ldc_w -37.0
      // 3abc: ldc_w 81.0
      // 3abf: ldc_w 16.0
      // 3ac2: ldc_w 16.0
      // 3ac5: ldc_w 16.0
      // 3ac8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3acb: dup
      // 3acc: fconst_0
      // 3acd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3ad0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ad3: bipush 0
      // 3ad4: sipush 352
      // 3ad7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ada: ldc_w 24.0
      // 3add: ldc_w -37.0
      // 3ae0: ldc_w 81.0
      // 3ae3: ldc_w 16.0
      // 3ae6: ldc_w 16.0
      // 3ae9: ldc_w 16.0
      // 3aec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3aef: dup
      // 3af0: fconst_0
      // 3af1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3af4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3af7: bipush 0
      // 3af8: sipush 352
      // 3afb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3afe: ldc_w 40.0
      // 3b01: ldc_w -37.0
      // 3b04: ldc_w 81.0
      // 3b07: ldc_w 16.0
      // 3b0a: ldc_w 16.0
      // 3b0d: ldc_w 16.0
      // 3b10: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b13: dup
      // 3b14: fconst_0
      // 3b15: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3b18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b1b: bipush 0
      // 3b1c: sipush 352
      // 3b1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b22: ldc_w 56.0
      // 3b25: ldc_w -37.0
      // 3b28: ldc_w 81.0
      // 3b2b: ldc_w 16.0
      // 3b2e: ldc_w 16.0
      // 3b31: ldc_w 16.0
      // 3b34: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b37: dup
      // 3b38: fconst_0
      // 3b39: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3b3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b3f: bipush 0
      // 3b40: sipush 352
      // 3b43: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b46: ldc_w 24.0
      // 3b49: ldc_w -69.0
      // 3b4c: ldc_w 49.0
      // 3b4f: ldc_w 16.0
      // 3b52: ldc_w 16.0
      // 3b55: ldc_w 16.0
      // 3b58: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b5b: dup
      // 3b5c: fconst_0
      // 3b5d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3b60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b63: bipush 0
      // 3b64: sipush 352
      // 3b67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b6a: ldc_w 8.0
      // 3b6d: ldc_w -53.0
      // 3b70: fconst_1
      // 3b71: ldc_w 16.0
      // 3b74: ldc_w 16.0
      // 3b77: ldc_w 16.0
      // 3b7a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3b7d: dup
      // 3b7e: fconst_0
      // 3b7f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3b82: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b85: bipush 0
      // 3b86: sipush 128
      // 3b89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3b8c: ldc_w -8.0
      // 3b8f: ldc_w -53.0
      // 3b92: ldc_w 17.0
      // 3b95: ldc_w 16.0
      // 3b98: ldc_w 16.0
      // 3b9b: ldc_w 16.0
      // 3b9e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3ba1: dup
      // 3ba2: fconst_0
      // 3ba3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3ba6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ba9: bipush 0
      // 3baa: sipush 128
      // 3bad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bb0: ldc_w -8.0
      // 3bb3: ldc_w -53.0
      // 3bb6: ldc_w 33.0
      // 3bb9: ldc_w 16.0
      // 3bbc: ldc_w 16.0
      // 3bbf: ldc_w 16.0
      // 3bc2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3bc5: dup
      // 3bc6: fconst_0
      // 3bc7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3bca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bcd: bipush 0
      // 3bce: sipush 128
      // 3bd1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bd4: ldc_w -8.0
      // 3bd7: ldc_w -53.0
      // 3bda: ldc_w 49.0
      // 3bdd: ldc_w 16.0
      // 3be0: ldc_w 16.0
      // 3be3: ldc_w 16.0
      // 3be6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3be9: dup
      // 3bea: fconst_0
      // 3beb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3bee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bf1: bipush 0
      // 3bf2: sipush 352
      // 3bf5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3bf8: ldc_w 8.0
      // 3bfb: ldc_w -69.0
      // 3bfe: ldc_w 17.0
      // 3c01: ldc_w 16.0
      // 3c04: ldc_w 16.0
      // 3c07: ldc_w 16.0
      // 3c0a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c0d: dup
      // 3c0e: fconst_0
      // 3c0f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c12: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c15: bipush 0
      // 3c16: sipush 352
      // 3c19: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c1c: ldc_w 8.0
      // 3c1f: ldc_w -53.0
      // 3c22: ldc_w 33.0
      // 3c25: ldc_w 16.0
      // 3c28: ldc_w 16.0
      // 3c2b: ldc_w 16.0
      // 3c2e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c31: dup
      // 3c32: fconst_0
      // 3c33: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c36: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c39: bipush 47
      // 3c3b: bipush 0
      // 3c3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c42: ldc_w 24.0
      // 3c45: ldc_w -69.0
      // 3c48: ldc_w 65.0
      // 3c4b: ldc_w 16.0
      // 3c4e: ldc_w 16.0
      // 3c51: ldc_w 16.0
      // 3c54: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c57: dup
      // 3c58: fconst_0
      // 3c59: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c5c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c5f: bipush 0
      // 3c60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c63: bipush 0
      // 3c64: sipush 128
      // 3c67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c6a: ldc_w -8.0
      // 3c6d: ldc_w -69.0
      // 3c70: ldc_w 17.0
      // 3c73: ldc_w 16.0
      // 3c76: ldc_w 16.0
      // 3c79: ldc_w 16.0
      // 3c7c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3c7f: dup
      // 3c80: fconst_0
      // 3c81: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3c84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c87: bipush 0
      // 3c88: sipush 350
      // 3c8b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3c8e: ldc_w 40.0
      // 3c91: ldc_w -85.0
      // 3c94: ldc_w 17.0
      // 3c97: ldc_w 16.0
      // 3c9a: ldc_w 16.0
      // 3c9d: ldc_w 16.0
      // 3ca0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3ca3: dup
      // 3ca4: fconst_0
      // 3ca5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3ca8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cab: sipush 432
      // 3cae: sipush 350
      // 3cb1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cb4: ldc_w 24.0
      // 3cb7: ldc_w -85.0
      // 3cba: fconst_1
      // 3cbb: ldc_w 16.0
      // 3cbe: ldc_w 16.0
      // 3cc1: ldc_w 16.0
      // 3cc4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3cc7: dup
      // 3cc8: fconst_0
      // 3cc9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3ccc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ccf: sipush 432
      // 3cd2: sipush 350
      // 3cd5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cd8: ldc_w 8.0
      // 3cdb: ldc_w -85.0
      // 3cde: fconst_1
      // 3cdf: ldc_w 16.0
      // 3ce2: ldc_w 16.0
      // 3ce5: ldc_w 16.0
      // 3ce8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3ceb: dup
      // 3cec: fconst_0
      // 3ced: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3cf0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cf3: sipush 432
      // 3cf6: sipush 350
      // 3cf9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3cfc: ldc_w 24.0
      // 3cff: ldc_w -85.0
      // 3d02: ldc_w 17.0
      // 3d05: ldc_w 16.0
      // 3d08: ldc_w 16.0
      // 3d0b: ldc_w 16.0
      // 3d0e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d11: dup
      // 3d12: fconst_0
      // 3d13: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d16: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d19: sipush 432
      // 3d1c: sipush 350
      // 3d1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d22: ldc_w -8.0
      // 3d25: ldc_w -85.0
      // 3d28: ldc_w 17.0
      // 3d2b: ldc_w 16.0
      // 3d2e: ldc_w 16.0
      // 3d31: ldc_w 16.0
      // 3d34: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d37: dup
      // 3d38: fconst_0
      // 3d39: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d3f: sipush 432
      // 3d42: sipush 350
      // 3d45: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d48: ldc_w 24.0
      // 3d4b: ldc_w 27.0
      // 3d4e: ldc_w -95.0
      // 3d51: ldc_w 16.0
      // 3d54: ldc_w 16.0
      // 3d57: ldc_w 16.0
      // 3d5a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d5d: dup
      // 3d5e: fconst_0
      // 3d5f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d62: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d65: sipush 432
      // 3d68: sipush 350
      // 3d6b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d6e: ldc_w 152.0
      // 3d71: ldc_w 59.0
      // 3d74: fconst_1
      // 3d75: ldc_w 16.0
      // 3d78: ldc_w 16.0
      // 3d7b: ldc_w 16.0
      // 3d7e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3d81: dup
      // 3d82: fconst_0
      // 3d83: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3d86: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d89: sipush 432
      // 3d8c: sipush 350
      // 3d8f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3d92: ldc_w 152.0
      // 3d95: ldc_w 75.0
      // 3d98: fconst_1
      // 3d99: ldc_w 16.0
      // 3d9c: ldc_w 16.0
      // 3d9f: ldc_w 16.0
      // 3da2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3da5: dup
      // 3da6: fconst_0
      // 3da7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3daa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3dad: sipush 432
      // 3db0: sipush 350
      // 3db3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3db6: ldc_w 168.0
      // 3db9: ldc_w 75.0
      // 3dbc: fconst_1
      // 3dbd: ldc_w 16.0
      // 3dc0: ldc_w 16.0
      // 3dc3: ldc_w 16.0
      // 3dc6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3dc9: dup
      // 3dca: fconst_0
      // 3dcb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3dce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3dd1: sipush 432
      // 3dd4: sipush 350
      // 3dd7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3dda: ldc_w 152.0
      // 3ddd: ldc_w 59.0
      // 3de0: ldc_w 17.0
      // 3de3: ldc_w 16.0
      // 3de6: ldc_w 16.0
      // 3de9: ldc_w 16.0
      // 3dec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3def: dup
      // 3df0: fconst_0
      // 3df1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3df4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3df7: sipush 432
      // 3dfa: sipush 350
      // 3dfd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e00: ldc_w 168.0
      // 3e03: ldc_w 59.0
      // 3e06: ldc_w 17.0
      // 3e09: ldc_w 16.0
      // 3e0c: ldc_w 16.0
      // 3e0f: ldc_w 16.0
      // 3e12: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3e15: dup
      // 3e16: fconst_0
      // 3e17: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3e1a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e1d: bipush 32
      // 3e1f: sipush 350
      // 3e22: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e25: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e28: ldc_w 200.0
      // 3e2b: ldc_w 11.0
      // 3e2e: ldc_w 33.0
      // 3e31: ldc_w 16.0
      // 3e34: ldc_w 16.0
      // 3e37: ldc_w 16.0
      // 3e3a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3e3d: dup
      // 3e3e: fconst_0
      // 3e3f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3e42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e45: bipush 0
      // 3e46: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e49: sipush 432
      // 3e4c: sipush 350
      // 3e4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e52: ldc_w 184.0
      // 3e55: ldc_w 27.0
      // 3e58: ldc_w 17.0
      // 3e5b: ldc_w 16.0
      // 3e5e: ldc_w 16.0
      // 3e61: ldc_w 16.0
      // 3e64: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3e67: dup
      // 3e68: fconst_0
      // 3e69: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3e6c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e6f: sipush 432
      // 3e72: sipush 350
      // 3e75: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e78: ldc_w 184.0
      // 3e7b: ldc_w -53.0
      // 3e7e: ldc_w 17.0
      // 3e81: ldc_w 16.0
      // 3e84: ldc_w 16.0
      // 3e87: ldc_w 16.0
      // 3e8a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3e8d: dup
      // 3e8e: fconst_0
      // 3e8f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3e92: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e95: sipush 432
      // 3e98: sipush 350
      // 3e9b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3e9e: ldc_w 200.0
      // 3ea1: ldc_w -53.0
      // 3ea4: ldc_w 17.0
      // 3ea7: ldc_w 16.0
      // 3eaa: ldc_w 16.0
      // 3ead: ldc_w 16.0
      // 3eb0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3eb3: dup
      // 3eb4: fconst_0
      // 3eb5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3eb8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ebb: sipush 432
      // 3ebe: sipush 350
      // 3ec1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ec4: ldc_w 216.0
      // 3ec7: ldc_w -21.0
      // 3eca: ldc_w 17.0
      // 3ecd: ldc_w 16.0
      // 3ed0: ldc_w 16.0
      // 3ed3: ldc_w 16.0
      // 3ed6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3ed9: dup
      // 3eda: fconst_0
      // 3edb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3ede: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ee1: sipush 432
      // 3ee4: sipush 350
      // 3ee7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3eea: ldc_w 232.0
      // 3eed: ldc_w -21.0
      // 3ef0: ldc_w 17.0
      // 3ef3: ldc_w 16.0
      // 3ef6: ldc_w 16.0
      // 3ef9: ldc_w 16.0
      // 3efc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3eff: dup
      // 3f00: fconst_0
      // 3f01: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3f04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f07: sipush 432
      // 3f0a: sipush 350
      // 3f0d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f10: ldc_w 232.0
      // 3f13: ldc_w -5.0
      // 3f16: ldc_w 33.0
      // 3f19: ldc_w 16.0
      // 3f1c: ldc_w 16.0
      // 3f1f: ldc_w 16.0
      // 3f22: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3f25: dup
      // 3f26: fconst_0
      // 3f27: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3f2a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f2d: sipush 432
      // 3f30: sipush 350
      // 3f33: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f36: ldc_w 216.0
      // 3f39: ldc_w -5.0
      // 3f3c: ldc_w 17.0
      // 3f3f: ldc_w 16.0
      // 3f42: ldc_w 16.0
      // 3f45: ldc_w 16.0
      // 3f48: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3f4b: dup
      // 3f4c: fconst_0
      // 3f4d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3f50: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f53: sipush 432
      // 3f56: sipush 350
      // 3f59: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f5c: ldc_w 216.0
      // 3f5f: ldc_w -53.0
      // 3f62: ldc_w 17.0
      // 3f65: ldc_w 16.0
      // 3f68: ldc_w 16.0
      // 3f6b: ldc_w 16.0
      // 3f6e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3f71: dup
      // 3f72: fconst_0
      // 3f73: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3f76: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f79: sipush 432
      // 3f7c: bipush 0
      // 3f7d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f80: ldc_w 232.0
      // 3f83: ldc_w -69.0
      // 3f86: ldc_w 17.0
      // 3f89: ldc_w 16.0
      // 3f8c: ldc_w 16.0
      // 3f8f: ldc_w 16.0
      // 3f92: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3f95: dup
      // 3f96: fconst_0
      // 3f97: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3f9a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3f9d: sipush 432
      // 3fa0: sipush 350
      // 3fa3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3fa6: ldc_w 232.0
      // 3fa9: ldc_w -37.0
      // 3fac: ldc_w 17.0
      // 3faf: ldc_w 16.0
      // 3fb2: ldc_w 16.0
      // 3fb5: ldc_w 16.0
      // 3fb8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3fbb: dup
      // 3fbc: fconst_0
      // 3fbd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3fc0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3fc3: sipush 432
      // 3fc6: sipush 350
      // 3fc9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3fcc: ldc_w 232.0
      // 3fcf: ldc_w -37.0
      // 3fd2: ldc_w 33.0
      // 3fd5: ldc_w 16.0
      // 3fd8: ldc_w 16.0
      // 3fdb: ldc_w 16.0
      // 3fde: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 3fe1: dup
      // 3fe2: fconst_0
      // 3fe3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 3fe6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3fe9: sipush 432
      // 3fec: sipush 350
      // 3fef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 3ff2: ldc_w 216.0
      // 3ff5: ldc_w -37.0
      // 3ff8: fconst_2
      // 3ff9: ldc_w 16.0
      // 3ffc: ldc_w 16.0
      // 3fff: ldc_w 16.0
      // 4002: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4005: dup
      // 4006: fconst_0
      // 4007: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 400a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 400d: sipush 432
      // 4010: sipush 350
      // 4013: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4016: ldc_w 168.0
      // 4019: ldc_w -53.0
      // 401c: ldc_w 17.0
      // 401f: ldc_w 16.0
      // 4022: ldc_w 16.0
      // 4025: ldc_w 16.0
      // 4028: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 402b: dup
      // 402c: fconst_0
      // 402d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4030: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4033: sipush 432
      // 4036: sipush 350
      // 4039: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 403c: ldc_w 200.0
      // 403f: ldc_w 27.0
      // 4042: ldc_w 17.0
      // 4045: ldc_w 16.0
      // 4048: ldc_w 16.0
      // 404b: ldc_w 16.0
      // 404e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4051: dup
      // 4052: fconst_0
      // 4053: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4056: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4059: sipush 432
      // 405c: sipush 350
      // 405f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4062: ldc_w 184.0
      // 4065: ldc_w 11.0
      // 4068: ldc_w 33.0
      // 406b: ldc_w 16.0
      // 406e: ldc_w 16.0
      // 4071: ldc_w 16.0
      // 4074: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4077: dup
      // 4078: fconst_0
      // 4079: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 407c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 407f: sipush 432
      // 4082: sipush 350
      // 4085: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4088: ldc_w 24.0
      // 408b: ldc_w 11.0
      // 408e: ldc_w -79.0
      // 4091: ldc_w 16.0
      // 4094: ldc_w 16.0
      // 4097: ldc_w 16.0
      // 409a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 409d: dup
      // 409e: fconst_0
      // 409f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 40a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 40a5: sipush 432
      // 40a8: sipush 350
      // 40ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 40ae: ldc_w 8.0
      // 40b1: ldc_w 11.0
      // 40b4: ldc_w -79.0
      // 40b7: ldc_w 16.0
      // 40ba: ldc_w 16.0
      // 40bd: ldc_w 16.0
      // 40c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 40c3: dup
      // 40c4: fconst_0
      // 40c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 40c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 40cb: bipush 0
      // 40cc: sipush 350
      // 40cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 40d2: ldc_w 8.0
      // 40d5: ldc_w -5.0
      // 40d8: ldc_w -79.0
      // 40db: ldc_w 16.0
      // 40de: ldc_w 16.0
      // 40e1: ldc_w 16.0
      // 40e4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 40e7: dup
      // 40e8: fconst_0
      // 40e9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 40ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 40ef: bipush 0
      // 40f0: sipush 350
      // 40f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 40f6: ldc_w -8.0
      // 40f9: ldc_w 11.0
      // 40fc: ldc_w -63.0
      // 40ff: ldc_w 16.0
      // 4102: ldc_w 16.0
      // 4105: ldc_w 16.0
      // 4108: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 410b: dup
      // 410c: fconst_0
      // 410d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4110: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4113: bipush 0
      // 4114: sipush 350
      // 4117: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 411a: ldc_w -8.0
      // 411d: ldc_w 27.0
      // 4120: ldc_w -47.0
      // 4123: ldc_w 16.0
      // 4126: ldc_w 16.0
      // 4129: ldc_w 16.0
      // 412c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 412f: dup
      // 4130: fconst_0
      // 4131: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4134: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4137: sipush 432
      // 413a: sipush 350
      // 413d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4140: ldc_w 8.0
      // 4143: ldc_w 27.0
      // 4146: ldc_w -63.0
      // 4149: ldc_w 16.0
      // 414c: ldc_w 16.0
      // 414f: ldc_w 16.0
      // 4152: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4155: dup
      // 4156: fconst_0
      // 4157: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 415a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 415d: bipush 0
      // 415e: sipush 350
      // 4161: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4164: ldc_w 8.0
      // 4167: ldc_w -21.0
      // 416a: ldc_w -79.0
      // 416d: ldc_w 16.0
      // 4170: ldc_w 16.0
      // 4173: ldc_w 16.0
      // 4176: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4179: dup
      // 417a: fconst_0
      // 417b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 417e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4181: bipush 0
      // 4182: sipush 350
      // 4185: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4188: ldc_w 8.0
      // 418b: ldc_w -37.0
      // 418e: ldc_w -63.0
      // 4191: ldc_w 16.0
      // 4194: ldc_w 16.0
      // 4197: ldc_w 16.0
      // 419a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 419d: dup
      // 419e: fconst_0
      // 419f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 41a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 41a5: sipush 432
      // 41a8: sipush 350
      // 41ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 41ae: ldc_w 24.0
      // 41b1: ldc_w 11.0
      // 41b4: ldc_w -95.0
      // 41b7: ldc_w 16.0
      // 41ba: ldc_w 16.0
      // 41bd: ldc_w 16.0
      // 41c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 41c3: dup
      // 41c4: fconst_0
      // 41c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 41c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 41cb: sipush 432
      // 41ce: sipush 350
      // 41d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 41d4: ldc_w 24.0
      // 41d7: ldc_w -5.0
      // 41da: ldc_w -95.0
      // 41dd: ldc_w 16.0
      // 41e0: ldc_w 16.0
      // 41e3: ldc_w 16.0
      // 41e6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 41e9: dup
      // 41ea: fconst_0
      // 41eb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 41ee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 41f1: sipush 432
      // 41f4: sipush 350
      // 41f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 41fa: ldc_w 104.0
      // 41fd: ldc_w -133.0
      // 4200: ldc_w 49.0
      // 4203: ldc_w 16.0
      // 4206: ldc_w 16.0
      // 4209: ldc_w 16.0
      // 420c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 420f: dup
      // 4210: fconst_0
      // 4211: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4214: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4217: sipush 432
      // 421a: sipush 350
      // 421d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4220: ldc_w 88.0
      // 4223: ldc_w -133.0
      // 4226: ldc_w 49.0
      // 4229: ldc_w 16.0
      // 422c: ldc_w 16.0
      // 422f: ldc_w 16.0
      // 4232: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4235: dup
      // 4236: fconst_0
      // 4237: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 423a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 423d: sipush 432
      // 4240: sipush 350
      // 4243: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4246: ldc_w 88.0
      // 4249: ldc_w -117.0
      // 424c: ldc_w 49.0
      // 424f: ldc_w 16.0
      // 4252: ldc_w 16.0
      // 4255: ldc_w 16.0
      // 4258: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 425b: dup
      // 425c: fconst_0
      // 425d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4260: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4263: sipush 432
      // 4266: sipush 350
      // 4269: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 426c: ldc_w 104.0
      // 426f: ldc_w -117.0
      // 4272: ldc_w 49.0
      // 4275: ldc_w 16.0
      // 4278: ldc_w 16.0
      // 427b: ldc_w 16.0
      // 427e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4281: dup
      // 4282: fconst_0
      // 4283: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4286: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4289: sipush 432
      // 428c: sipush 350
      // 428f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4292: ldc_w 120.0
      // 4295: ldc_w -117.0
      // 4298: ldc_w 49.0
      // 429b: ldc_w 16.0
      // 429e: ldc_w 16.0
      // 42a1: ldc_w 16.0
      // 42a4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 42a7: dup
      // 42a8: fconst_0
      // 42a9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 42ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 42af: sipush 432
      // 42b2: sipush 350
      // 42b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 42b8: ldc_w 24.0
      // 42bb: ldc_w -85.0
      // 42be: ldc_w 33.0
      // 42c1: ldc_w 16.0
      // 42c4: ldc_w 16.0
      // 42c7: ldc_w 16.0
      // 42ca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 42cd: dup
      // 42ce: fconst_0
      // 42cf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 42d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 42d5: sipush 432
      // 42d8: sipush 350
      // 42db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 42de: ldc_w 8.0
      // 42e1: ldc_w -53.0
      // 42e4: ldc_w 65.0
      // 42e7: ldc_w 16.0
      // 42ea: ldc_w 16.0
      // 42ed: ldc_w 16.0
      // 42f0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 42f3: dup
      // 42f4: fconst_0
      // 42f5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 42f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 42fb: sipush 432
      // 42fe: sipush 350
      // 4301: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4304: ldc_w 24.0
      // 4307: ldc_w -53.0
      // 430a: ldc_w 65.0
      // 430d: ldc_w 16.0
      // 4310: ldc_w 16.0
      // 4313: ldc_w 16.0
      // 4316: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4319: dup
      // 431a: fconst_0
      // 431b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 431e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4321: sipush 432
      // 4324: sipush 350
      // 4327: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 432a: ldc_w 40.0
      // 432d: ldc_w -69.0
      // 4330: ldc_w 65.0
      // 4333: ldc_w 16.0
      // 4336: ldc_w 16.0
      // 4339: ldc_w 16.0
      // 433c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 433f: dup
      // 4340: fconst_0
      // 4341: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4344: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4347: sipush 432
      // 434a: sipush 350
      // 434d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4350: ldc_w 56.0
      // 4353: ldc_w -69.0
      // 4356: ldc_w 65.0
      // 4359: ldc_w 16.0
      // 435c: ldc_w 16.0
      // 435f: ldc_w 16.0
      // 4362: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4365: dup
      // 4366: fconst_0
      // 4367: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 436a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 436d: sipush 432
      // 4370: sipush 350
      // 4373: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4376: ldc_w 104.0
      // 4379: ldc_w -69.0
      // 437c: ldc_w 65.0
      // 437f: ldc_w 16.0
      // 4382: ldc_w 16.0
      // 4385: ldc_w 16.0
      // 4388: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 438b: dup
      // 438c: fconst_0
      // 438d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4390: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4393: sipush 432
      // 4396: sipush 350
      // 4399: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 439c: ldc_w 120.0
      // 439f: ldc_w -53.0
      // 43a2: ldc_w 65.0
      // 43a5: ldc_w 16.0
      // 43a8: ldc_w 16.0
      // 43ab: ldc_w 16.0
      // 43ae: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 43b1: dup
      // 43b2: fconst_0
      // 43b3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 43b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 43b9: sipush 432
      // 43bc: sipush 350
      // 43bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 43c2: ldc_w 120.0
      // 43c5: ldc_w -37.0
      // 43c8: ldc_w 65.0
      // 43cb: ldc_w 16.0
      // 43ce: ldc_w 16.0
      // 43d1: ldc_w 16.0
      // 43d4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 43d7: dup
      // 43d8: fconst_0
      // 43d9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 43dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 43df: sipush 432
      // 43e2: sipush 350
      // 43e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 43e8: ldc_w 136.0
      // 43eb: ldc_w -37.0
      // 43ee: ldc_w 65.0
      // 43f1: ldc_w 16.0
      // 43f4: ldc_w 16.0
      // 43f7: ldc_w 16.0
      // 43fa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 43fd: dup
      // 43fe: fconst_0
      // 43ff: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4402: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4405: sipush 432
      // 4408: sipush 350
      // 440b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 440e: ldc_w 120.0
      // 4411: ldc_w -69.0
      // 4414: ldc_w 65.0
      // 4417: ldc_w 16.0
      // 441a: ldc_w 16.0
      // 441d: ldc_w 16.0
      // 4420: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4423: dup
      // 4424: fconst_0
      // 4425: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4428: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 442b: sipush 432
      // 442e: sipush 350
      // 4431: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4434: ldc_w 136.0
      // 4437: ldc_w -69.0
      // 443a: ldc_w 65.0
      // 443d: ldc_w 16.0
      // 4440: ldc_w 16.0
      // 4443: ldc_w 16.0
      // 4446: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4449: dup
      // 444a: fconst_0
      // 444b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 444e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4451: bipush 30
      // 4453: sipush 350
      // 4456: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4459: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 445c: ldc_w 136.0
      // 445f: ldc_w -53.0
      // 4462: ldc_w 81.0
      // 4465: ldc_w 16.0
      // 4468: ldc_w 16.0
      // 446b: ldc_w 16.0
      // 446e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4471: dup
      // 4472: fconst_0
      // 4473: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4476: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4479: bipush 0
      // 447a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 447d: sipush 432
      // 4480: sipush 350
      // 4483: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4486: ldc_w 56.0
      // 4489: ldc_w -53.0
      // 448c: ldc_w 65.0
      // 448f: ldc_w 16.0
      // 4492: ldc_w 16.0
      // 4495: ldc_w 16.0
      // 4498: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 449b: dup
      // 449c: fconst_0
      // 449d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 44a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 44a3: sipush 432
      // 44a6: sipush 350
      // 44a9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 44ac: ldc_w 40.0
      // 44af: ldc_w -53.0
      // 44b2: ldc_w 65.0
      // 44b5: ldc_w 16.0
      // 44b8: ldc_w 16.0
      // 44bb: ldc_w 16.0
      // 44be: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 44c1: dup
      // 44c2: fconst_0
      // 44c3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 44c6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 44c9: bipush 0
      // 44ca: sipush 128
      // 44cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 44d0: ldc_w 40.0
      // 44d3: ldc_w -85.0
      // 44d6: fconst_1
      // 44d7: ldc_w 16.0
      // 44da: ldc_w 16.0
      // 44dd: ldc_w 16.0
      // 44e0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 44e3: dup
      // 44e4: fconst_0
      // 44e5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 44e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 44eb: bipush 0
      // 44ec: sipush 128
      // 44ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 44f2: ldc_w 40.0
      // 44f5: ldc_w -85.0
      // 44f8: ldc_w 33.0
      // 44fb: ldc_w 16.0
      // 44fe: ldc_w 16.0
      // 4501: ldc_w 16.0
      // 4504: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4507: dup
      // 4508: fconst_0
      // 4509: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 450c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 450f: bipush 0
      // 4510: sipush 128
      // 4513: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4516: ldc_w 40.0
      // 4519: ldc_w -69.0
      // 451c: ldc_w 33.0
      // 451f: ldc_w 16.0
      // 4522: ldc_w 16.0
      // 4525: ldc_w 16.0
      // 4528: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 452b: dup
      // 452c: fconst_0
      // 452d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4530: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4533: bipush 0
      // 4534: sipush 128
      // 4537: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 453a: ldc_w 56.0
      // 453d: ldc_w -69.0
      // 4540: ldc_w 33.0
      // 4543: ldc_w 16.0
      // 4546: ldc_w 16.0
      // 4549: ldc_w 16.0
      // 454c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 454f: dup
      // 4550: fconst_0
      // 4551: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4554: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4557: bipush 0
      // 4558: sipush 128
      // 455b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 455e: ldc_w 56.0
      // 4561: ldc_w -85.0
      // 4564: ldc_w 33.0
      // 4567: ldc_w 16.0
      // 456a: ldc_w 16.0
      // 456d: ldc_w 16.0
      // 4570: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4573: dup
      // 4574: fconst_0
      // 4575: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4578: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 457b: bipush 0
      // 457c: sipush 128
      // 457f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4582: ldc_w 72.0
      // 4585: ldc_w -101.0
      // 4588: ldc_w 33.0
      // 458b: ldc_w 16.0
      // 458e: ldc_w 16.0
      // 4591: ldc_w 16.0
      // 4594: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4597: dup
      // 4598: fconst_0
      // 4599: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 459c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 459f: bipush 0
      // 45a0: sipush 128
      // 45a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 45a6: ldc_w 88.0
      // 45a9: ldc_w -101.0
      // 45ac: ldc_w 33.0
      // 45af: ldc_w 16.0
      // 45b2: ldc_w 16.0
      // 45b5: ldc_w 0.1
      // 45b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 45bb: dup
      // 45bc: fconst_0
      // 45bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 45c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 45c3: bipush 0
      // 45c4: sipush 128
      // 45c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 45ca: ldc_w 40.0
      // 45cd: ldc_w -101.0
      // 45d0: ldc_w -15.0
      // 45d3: ldc_w 16.0
      // 45d6: ldc_w 16.0
      // 45d9: ldc_w 16.0
      // 45dc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 45df: dup
      // 45e0: fconst_0
      // 45e1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 45e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 45e7: bipush 0
      // 45e8: sipush 128
      // 45eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 45ee: ldc_w 40.0
      // 45f1: ldc_w -85.0
      // 45f4: ldc_w -15.0
      // 45f7: ldc_w 16.0
      // 45fa: ldc_w 16.0
      // 45fd: ldc_w 16.0
      // 4600: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4603: dup
      // 4604: fconst_0
      // 4605: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4608: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 460b: bipush 0
      // 460c: sipush 128
      // 460f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4612: ldc_w 40.0
      // 4615: ldc_w -85.0
      // 4618: ldc_w -31.0
      // 461b: ldc_w 16.0
      // 461e: ldc_w 16.0
      // 4621: ldc_w 16.0
      // 4624: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4627: dup
      // 4628: fconst_0
      // 4629: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 462c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 462f: bipush 0
      // 4630: sipush 350
      // 4633: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4636: ldc_w 104.0
      // 4639: ldc_w -101.0
      // 463c: ldc_w -31.0
      // 463f: ldc_w 16.0
      // 4642: ldc_w 16.0
      // 4645: ldc_w 16.0
      // 4648: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 464b: dup
      // 464c: fconst_0
      // 464d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4650: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4653: bipush 0
      // 4654: sipush 350
      // 4657: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 465a: ldc_w 88.0
      // 465d: ldc_w -101.2
      // 4660: ldc_w -31.0
      // 4663: ldc_w 16.0
      // 4666: ldc_w 0.2
      // 4669: ldc_w 16.0
      // 466c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 466f: dup
      // 4670: fconst_0
      // 4671: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4674: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4677: bipush 0
      // 4678: sipush 350
      // 467b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 467e: ldc_w 72.0
      // 4681: ldc_w -101.2
      // 4684: ldc_w -31.0
      // 4687: ldc_w 16.0
      // 468a: ldc_w 0.2
      // 468d: ldc_w 16.0
      // 4690: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4693: dup
      // 4694: fconst_0
      // 4695: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4698: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 469b: bipush 0
      // 469c: sipush 350
      // 469f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 46a2: ldc_w 88.0
      // 46a5: ldc_w -101.2
      // 46a8: ldc_w 17.0
      // 46ab: ldc_w 16.0
      // 46ae: ldc_w 0.2
      // 46b1: ldc_w 16.0
      // 46b4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 46b7: dup
      // 46b8: fconst_0
      // 46b9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 46bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 46bf: bipush 0
      // 46c0: sipush 350
      // 46c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 46c6: ldc_w 72.0
      // 46c9: ldc_w -101.2
      // 46cc: ldc_w 17.0
      // 46cf: ldc_w 16.0
      // 46d2: ldc_w 0.2
      // 46d5: ldc_w 16.0
      // 46d8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 46db: dup
      // 46dc: fconst_0
      // 46dd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 46e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 46e3: bipush 0
      // 46e4: sipush 128
      // 46e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 46ea: ldc_w 120.0
      // 46ed: ldc_w -101.0
      // 46f0: ldc_w -31.0
      // 46f3: ldc_w 16.0
      // 46f6: ldc_w 16.0
      // 46f9: ldc_w 16.0
      // 46fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 46ff: dup
      // 4700: fconst_0
      // 4701: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4704: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4707: bipush 0
      // 4708: sipush 128
      // 470b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 470e: ldc_w 40.0
      // 4711: ldc_w -101.0
      // 4714: ldc_w -31.0
      // 4717: ldc_w 16.0
      // 471a: ldc_w 16.0
      // 471d: ldc_w 16.0
      // 4720: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4723: dup
      // 4724: fconst_0
      // 4725: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4728: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 472b: bipush 0
      // 472c: sipush 128
      // 472f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4732: ldc_w 40.0
      // 4735: ldc_w -101.0
      // 4738: ldc_w -47.0
      // 473b: ldc_w 16.0
      // 473e: ldc_w 16.0
      // 4741: ldc_w 16.0
      // 4744: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4747: dup
      // 4748: fconst_0
      // 4749: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 474c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 474f: bipush 0
      // 4750: sipush 128
      // 4753: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4756: ldc_w 56.0
      // 4759: ldc_w -101.0
      // 475c: ldc_w -47.0
      // 475f: ldc_w 16.0
      // 4762: ldc_w 16.0
      // 4765: ldc_w 16.0
      // 4768: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 476b: dup
      // 476c: fconst_0
      // 476d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4770: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4773: bipush 0
      // 4774: sipush 128
      // 4777: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 477a: ldc_w 40.0
      // 477d: ldc_w -85.0
      // 4780: ldc_w -47.0
      // 4783: ldc_w 16.0
      // 4786: ldc_w 16.0
      // 4789: ldc_w 16.0
      // 478c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 478f: dup
      // 4790: fconst_0
      // 4791: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4794: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4797: bipush 0
      // 4798: sipush 128
      // 479b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 479e: ldc_w 24.0
      // 47a1: ldc_w -69.0
      // 47a4: ldc_w -15.0
      // 47a7: ldc_w 16.0
      // 47aa: ldc_w 16.0
      // 47ad: ldc_w 16.0
      // 47b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 47b3: dup
      // 47b4: fconst_0
      // 47b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 47b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 47bb: sipush 434
      // 47be: sipush 128
      // 47c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 47c4: ldc_w 24.0
      // 47c7: ldc_w -69.0
      // 47ca: fconst_1
      // 47cb: ldc_w 16.0
      // 47ce: ldc_w 16.0
      // 47d1: ldc_w 16.0
      // 47d4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 47d7: dup
      // 47d8: fconst_0
      // 47d9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 47dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 47df: bipush 0
      // 47e0: sipush 128
      // 47e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 47e6: ldc_w 24.0
      // 47e9: ldc_w -69.0
      // 47ec: ldc_w 17.0
      // 47ef: ldc_w 16.0
      // 47f2: ldc_w 16.0
      // 47f5: ldc_w 16.0
      // 47f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 47fb: dup
      // 47fc: fconst_0
      // 47fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4800: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4803: bipush 0
      // 4804: sipush 128
      // 4807: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 480a: ldc_w 24.0
      // 480d: ldc_w -69.0
      // 4810: ldc_w -31.0
      // 4813: ldc_w 16.0
      // 4816: ldc_w 16.0
      // 4819: ldc_w 16.0
      // 481c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 481f: dup
      // 4820: fconst_0
      // 4821: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4824: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4827: bipush 0
      // 4828: sipush 128
      // 482b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 482e: ldc_w 24.0
      // 4831: ldc_w -69.0
      // 4834: ldc_w -47.0
      // 4837: ldc_w 16.0
      // 483a: ldc_w 16.0
      // 483d: ldc_w 16.0
      // 4840: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4843: dup
      // 4844: fconst_0
      // 4845: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4848: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 484b: bipush 0
      // 484c: sipush 350
      // 484f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4852: ldc_w 40.0
      // 4855: ldc_w -53.0
      // 4858: ldc_w -47.0
      // 485b: ldc_w 16.0
      // 485e: ldc_w 16.0
      // 4861: ldc_w 16.0
      // 4864: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4867: dup
      // 4868: fconst_0
      // 4869: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 486c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 486f: bipush 0
      // 4870: sipush 350
      // 4873: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4876: ldc_w 56.0
      // 4879: ldc_w -53.0
      // 487c: ldc_w -47.0
      // 487f: ldc_w 16.0
      // 4882: ldc_w 16.0
      // 4885: ldc_w 16.0
      // 4888: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 488b: dup
      // 488c: fconst_0
      // 488d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4890: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4893: bipush 0
      // 4894: sipush 128
      // 4897: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 489a: ldc_w 56.0
      // 489d: ldc_w -101.0
      // 48a0: ldc_w -63.0
      // 48a3: ldc_w 16.0
      // 48a6: ldc_w 16.0
      // 48a9: ldc_w 16.0
      // 48ac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 48af: dup
      // 48b0: fconst_0
      // 48b1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 48b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 48b7: bipush 0
      // 48b8: sipush 350
      // 48bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 48be: ldc_w 56.0
      // 48c1: ldc_w -101.0
      // 48c4: ldc_w -31.0
      // 48c7: ldc_w 16.0
      // 48ca: ldc_w 16.0
      // 48cd: ldc_w 16.0
      // 48d0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 48d3: dup
      // 48d4: fconst_0
      // 48d5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 48d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 48db: bipush 0
      // 48dc: sipush 350
      // 48df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 48e2: ldc_w 56.0
      // 48e5: ldc_w -101.0
      // 48e8: fconst_1
      // 48e9: ldc_w 16.0
      // 48ec: ldc_w 16.0
      // 48ef: ldc_w 16.0
      // 48f2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 48f5: dup
      // 48f6: fconst_0
      // 48f7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 48fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 48fd: bipush 0
      // 48fe: sipush 350
      // 4901: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4904: ldc_w 41.0
      // 4907: ldc_w -101.0
      // 490a: ldc_w 33.0
      // 490d: ldc_w 16.0
      // 4910: ldc_w 16.0
      // 4913: ldc_w 16.0
      // 4916: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4919: dup
      // 491a: fconst_0
      // 491b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 491e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4921: bipush 0
      // 4922: sipush 350
      // 4925: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4928: ldc_w 57.0
      // 492b: ldc_w -101.0
      // 492e: ldc_w 33.0
      // 4931: ldc_w 16.0
      // 4934: ldc_w 16.0
      // 4937: ldc_w 16.0
      // 493a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 493d: dup
      // 493e: fconst_0
      // 493f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4942: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4945: bipush 0
      // 4946: sipush 350
      // 4949: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 494c: ldc_w 40.0
      // 494f: ldc_w -101.0
      // 4952: fconst_1
      // 4953: ldc_w 16.0
      // 4956: ldc_w 16.0
      // 4959: ldc_w 16.0
      // 495c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 495f: dup
      // 4960: fconst_0
      // 4961: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4964: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4967: sipush 432
      // 496a: sipush 128
      // 496d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4970: ldc_w 56.0
      // 4973: ldc_w -101.0
      // 4976: ldc_w 17.0
      // 4979: ldc_w 16.0
      // 497c: ldc_w 16.0
      // 497f: ldc_w 16.0
      // 4982: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4985: dup
      // 4986: fconst_0
      // 4987: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 498a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 498d: bipush 0
      // 498e: sipush 128
      // 4991: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4994: ldc_w 56.0
      // 4997: ldc_w -101.0
      // 499a: ldc_w -15.0
      // 499d: ldc_w 16.0
      // 49a0: ldc_w 16.0
      // 49a3: ldc_w 16.0
      // 49a6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 49a9: dup
      // 49aa: fconst_0
      // 49ab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 49ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 49b1: bipush 0
      // 49b2: sipush 128
      // 49b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 49b8: ldc_w 72.0
      // 49bb: ldc_w -117.0
      // 49be: ldc_w -47.0
      // 49c1: ldc_w 16.0
      // 49c4: ldc_w 16.0
      // 49c7: ldc_w 16.0
      // 49ca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 49cd: dup
      // 49ce: fconst_0
      // 49cf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 49d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 49d5: bipush 31
      // 49d7: sipush 192
      // 49da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 49dd: ldc_w 88.0
      // 49e0: ldc_w -133.0
      // 49e3: ldc_w -63.0
      // 49e6: ldc_w 16.0
      // 49e9: ldc_w 32.0
      // 49ec: ldc_w 16.0
      // 49ef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 49f2: dup
      // 49f3: fconst_0
      // 49f4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 49f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 49fa: bipush 31
      // 49fc: sipush 192
      // 49ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a02: ldc_w 88.0
      // 4a05: ldc_w -133.0
      // 4a08: ldc_w -47.0
      // 4a0b: ldc_w 16.0
      // 4a0e: ldc_w 16.0
      // 4a11: ldc_w 16.0
      // 4a14: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4a17: dup
      // 4a18: fconst_0
      // 4a19: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4a1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a1f: bipush 31
      // 4a21: sipush 192
      // 4a24: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a27: ldc_w 88.0
      // 4a2a: ldc_w -117.0
      // 4a2d: ldc_w -47.0
      // 4a30: ldc_w 16.0
      // 4a33: ldc_w 16.0
      // 4a36: ldc_w 16.0
      // 4a39: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4a3c: dup
      // 4a3d: fconst_0
      // 4a3e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4a41: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a44: bipush 0
      // 4a45: sipush 128
      // 4a48: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a4b: ldc_w 40.0
      // 4a4e: ldc_w -117.0
      // 4a51: ldc_w -15.0
      // 4a54: ldc_w 16.0
      // 4a57: ldc_w 16.0
      // 4a5a: ldc_w 16.0
      // 4a5d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4a60: dup
      // 4a61: fconst_0
      // 4a62: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4a65: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a68: bipush 0
      // 4a69: sipush 128
      // 4a6c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a6f: ldc_w 56.0
      // 4a72: ldc_w -117.0
      // 4a75: ldc_w -15.0
      // 4a78: ldc_w 16.0
      // 4a7b: ldc_w 16.0
      // 4a7e: ldc_w 16.0
      // 4a81: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4a84: dup
      // 4a85: fconst_0
      // 4a86: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4a89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a8c: bipush 0
      // 4a8d: sipush 128
      // 4a90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4a93: ldc_w 104.0
      // 4a96: ldc_w -117.0
      // 4a99: ldc_w -15.0
      // 4a9c: ldc_w 16.0
      // 4a9f: ldc_w 16.0
      // 4aa2: ldc_w 16.0
      // 4aa5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4aa8: dup
      // 4aa9: fconst_0
      // 4aaa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4aad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4ab0: bipush 0
      // 4ab1: sipush 128
      // 4ab4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4ab7: ldc_w 120.0
      // 4aba: ldc_w -117.0
      // 4abd: ldc_w -15.0
      // 4ac0: ldc_w 16.0
      // 4ac3: ldc_w 16.0
      // 4ac6: ldc_w 16.0
      // 4ac9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4acc: dup
      // 4acd: fconst_0
      // 4ace: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4ad1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4ad4: bipush 0
      // 4ad5: sipush 128
      // 4ad8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4adb: ldc_w 168.0
      // 4ade: ldc_w -117.0
      // 4ae1: ldc_w -15.0
      // 4ae4: ldc_w 16.0
      // 4ae7: ldc_w 16.0
      // 4aea: ldc_w 16.0
      // 4aed: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4af0: dup
      // 4af1: fconst_0
      // 4af2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4af5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4af8: bipush 47
      // 4afa: sipush 350
      // 4afd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b00: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b03: ldc_w 152.0
      // 4b06: ldc_w -117.0
      // 4b09: ldc_w -15.0
      // 4b0c: ldc_w 16.0
      // 4b0f: ldc_w 16.0
      // 4b12: ldc_w 16.0
      // 4b15: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4b18: dup
      // 4b19: fconst_0
      // 4b1a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4b1d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b20: bipush 0
      // 4b21: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b24: bipush 0
      // 4b25: sipush 128
      // 4b28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b2b: ldc_w 168.0
      // 4b2e: ldc_w -133.0
      // 4b31: ldc_w -15.0
      // 4b34: ldc_w 16.0
      // 4b37: ldc_w 16.0
      // 4b3a: ldc_w 16.0
      // 4b3d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4b40: dup
      // 4b41: fconst_0
      // 4b42: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4b45: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b48: bipush 0
      // 4b49: sipush 128
      // 4b4c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b4f: ldc_w 168.0
      // 4b52: ldc_w -117.0
      // 4b55: fconst_1
      // 4b56: ldc_w 16.0
      // 4b59: ldc_w 16.0
      // 4b5c: ldc_w 16.0
      // 4b5f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4b62: dup
      // 4b63: fconst_0
      // 4b64: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4b67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b6a: bipush 0
      // 4b6b: sipush 128
      // 4b6e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b71: ldc_w 184.0
      // 4b74: ldc_w -117.0
      // 4b77: fconst_1
      // 4b78: ldc_w 16.0
      // 4b7b: ldc_w 16.0
      // 4b7e: ldc_w 16.0
      // 4b81: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4b84: dup
      // 4b85: fconst_0
      // 4b86: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4b89: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b8c: bipush 0
      // 4b8d: sipush 128
      // 4b90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4b93: ldc_w 184.0
      // 4b96: ldc_w -101.0
      // 4b99: ldc_w -15.0
      // 4b9c: ldc_w 16.0
      // 4b9f: ldc_w 16.0
      // 4ba2: ldc_w 16.0
      // 4ba5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4ba8: dup
      // 4ba9: fconst_0
      // 4baa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4bad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4bb0: bipush 0
      // 4bb1: sipush 128
      // 4bb4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4bb7: ldc_w 200.0
      // 4bba: ldc_w -101.0
      // 4bbd: ldc_w -15.0
      // 4bc0: ldc_w 16.0
      // 4bc3: ldc_w 16.0
      // 4bc6: ldc_w 16.0
      // 4bc9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4bcc: dup
      // 4bcd: fconst_0
      // 4bce: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4bd1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4bd4: bipush 0
      // 4bd5: sipush 128
      // 4bd8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4bdb: ldc_w 200.0
      // 4bde: ldc_w -117.0
      // 4be1: ldc_w -15.0
      // 4be4: ldc_w 16.0
      // 4be7: ldc_w 16.0
      // 4bea: ldc_w 16.0
      // 4bed: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4bf0: dup
      // 4bf1: fconst_0
      // 4bf2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4bf5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4bf8: bipush 0
      // 4bf9: sipush 128
      // 4bfc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4bff: ldc_w 200.0
      // 4c02: ldc_w -133.0
      // 4c05: ldc_w -15.0
      // 4c08: ldc_w 16.0
      // 4c0b: ldc_w 16.0
      // 4c0e: ldc_w 16.0
      // 4c11: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4c14: dup
      // 4c15: fconst_0
      // 4c16: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4c19: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4c1c: bipush 0
      // 4c1d: sipush 128
      // 4c20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4c23: ldc_w 184.0
      // 4c26: ldc_w -101.0
      // 4c29: fconst_1
      // 4c2a: ldc_w 16.0
      // 4c2d: ldc_w 16.0
      // 4c30: ldc_w 16.0
      // 4c33: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4c36: dup
      // 4c37: fconst_0
      // 4c38: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4c3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4c3e: bipush 0
      // 4c3f: sipush 128
      // 4c42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4c45: ldc_w 184.0
      // 4c48: ldc_w -101.0
      // 4c4b: ldc_w 17.0
      // 4c4e: ldc_w 16.0
      // 4c51: ldc_w 16.0
      // 4c54: ldc_w 16.0
      // 4c57: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4c5a: dup
      // 4c5b: fconst_0
      // 4c5c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4c5f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4c62: bipush 0
      // 4c63: sipush 128
      // 4c66: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4c69: ldc_w 168.0
      // 4c6c: ldc_w -101.0
      // 4c6f: ldc_w 17.0
      // 4c72: ldc_w 16.0
      // 4c75: ldc_w 16.0
      // 4c78: ldc_w 16.0
      // 4c7b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4c7e: dup
      // 4c7f: fconst_0
      // 4c80: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4c83: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4c86: bipush 0
      // 4c87: sipush 128
      // 4c8a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4c8d: ldc_w 152.0
      // 4c90: ldc_w -101.0
      // 4c93: ldc_w 33.0
      // 4c96: ldc_w 16.0
      // 4c99: ldc_w 16.0
      // 4c9c: ldc_w 16.0
      // 4c9f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4ca2: dup
      // 4ca3: fconst_0
      // 4ca4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4ca7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4caa: bipush 0
      // 4cab: sipush 128
      // 4cae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4cb1: ldc_w 152.0
      // 4cb4: ldc_w -101.0
      // 4cb7: ldc_w 17.0
      // 4cba: ldc_w 16.0
      // 4cbd: ldc_w 16.0
      // 4cc0: ldc_w 16.0
      // 4cc3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4cc6: dup
      // 4cc7: fconst_0
      // 4cc8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4ccb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4cce: bipush 0
      // 4ccf: sipush 128
      // 4cd2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4cd5: ldc_w 136.0
      // 4cd8: ldc_w -101.0
      // 4cdb: ldc_w 17.0
      // 4cde: ldc_w 16.0
      // 4ce1: ldc_w 16.0
      // 4ce4: ldc_w 16.0
      // 4ce7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4cea: dup
      // 4ceb: fconst_0
      // 4cec: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4cef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4cf2: bipush 0
      // 4cf3: sipush 128
      // 4cf6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4cf9: ldc_w 136.0
      // 4cfc: ldc_w -101.0
      // 4cff: fconst_1
      // 4d00: ldc_w 16.0
      // 4d03: ldc_w 16.0
      // 4d06: ldc_w 16.0
      // 4d09: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4d0c: dup
      // 4d0d: fconst_0
      // 4d0e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4d11: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4d14: bipush 0
      // 4d15: sipush 128
      // 4d18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4d1b: ldc_w 152.0
      // 4d1e: ldc_w -101.0
      // 4d21: fconst_1
      // 4d22: ldc_w 16.0
      // 4d25: ldc_w 16.0
      // 4d28: ldc_w 16.0
      // 4d2b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4d2e: dup
      // 4d2f: fconst_0
      // 4d30: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4d33: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4d36: bipush 0
      // 4d37: sipush 128
      // 4d3a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4d3d: ldc_w 136.0
      // 4d40: ldc_w -101.0
      // 4d43: ldc_w 33.0
      // 4d46: ldc_w 16.0
      // 4d49: ldc_w 16.0
      // 4d4c: ldc_w 16.0
      // 4d4f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4d52: dup
      // 4d53: fconst_0
      // 4d54: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4d57: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4d5a: bipush 0
      // 4d5b: sipush 128
      // 4d5e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4d61: ldc_w 152.0
      // 4d64: ldc_w -101.0
      // 4d67: ldc_w 49.0
      // 4d6a: ldc_w 16.0
      // 4d6d: ldc_w 16.0
      // 4d70: ldc_w 16.0
      // 4d73: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4d76: dup
      // 4d77: fconst_0
      // 4d78: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4d7b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4d7e: bipush 0
      // 4d7f: sipush 128
      // 4d82: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4d85: ldc_w 168.0
      // 4d88: ldc_w -101.0
      // 4d8b: ldc_w 33.0
      // 4d8e: ldc_w 16.0
      // 4d91: ldc_w 16.0
      // 4d94: ldc_w 16.0
      // 4d97: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4d9a: dup
      // 4d9b: fconst_0
      // 4d9c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4d9f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4da2: bipush 0
      // 4da3: sipush 128
      // 4da6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4da9: ldc_w 168.0
      // 4dac: ldc_w -85.0
      // 4daf: ldc_w 49.0
      // 4db2: ldc_w 16.0
      // 4db5: ldc_w 16.0
      // 4db8: ldc_w 16.0
      // 4dbb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4dbe: dup
      // 4dbf: fconst_0
      // 4dc0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4dc3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4dc6: bipush 0
      // 4dc7: sipush 128
      // 4dca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4dcd: ldc_w 184.0
      // 4dd0: ldc_w -85.0
      // 4dd3: ldc_w 33.0
      // 4dd6: ldc_w 16.0
      // 4dd9: ldc_w 16.0
      // 4ddc: ldc_w 16.0
      // 4ddf: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4de2: dup
      // 4de3: fconst_0
      // 4de4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4de7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4dea: bipush 0
      // 4deb: sipush 128
      // 4dee: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4df1: ldc_w 168.0
      // 4df4: ldc_w -69.0
      // 4df7: ldc_w 49.0
      // 4dfa: ldc_w 16.0
      // 4dfd: ldc_w 16.0
      // 4e00: ldc_w 16.0
      // 4e03: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4e06: dup
      // 4e07: fconst_0
      // 4e08: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4e0b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e0e: bipush 0
      // 4e0f: sipush 128
      // 4e12: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e15: ldc_w 184.0
      // 4e18: ldc_w -69.0
      // 4e1b: ldc_w 49.0
      // 4e1e: ldc_w 16.0
      // 4e21: ldc_w 16.0
      // 4e24: ldc_w 16.0
      // 4e27: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4e2a: dup
      // 4e2b: fconst_0
      // 4e2c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4e2f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e32: bipush 0
      // 4e33: sipush 128
      // 4e36: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e39: ldc_w 120.0
      // 4e3c: ldc_w -117.0
      // 4e3f: fconst_1
      // 4e40: ldc_w 16.0
      // 4e43: ldc_w 16.0
      // 4e46: ldc_w 16.0
      // 4e49: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4e4c: dup
      // 4e4d: fconst_0
      // 4e4e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4e51: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e54: bipush 0
      // 4e55: sipush 128
      // 4e58: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e5b: ldc_w 88.0
      // 4e5e: ldc_w -117.0
      // 4e61: ldc_w -15.0
      // 4e64: ldc_w 16.0
      // 4e67: ldc_w 16.0
      // 4e6a: ldc_w 16.0
      // 4e6d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4e70: dup
      // 4e71: fconst_0
      // 4e72: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4e75: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e78: bipush 0
      // 4e79: sipush 350
      // 4e7c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e7f: ldc_w 72.0
      // 4e82: ldc_w -117.0
      // 4e85: ldc_w -15.0
      // 4e88: ldc_w 16.0
      // 4e8b: ldc_w 16.0
      // 4e8e: ldc_w 16.0
      // 4e91: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4e94: dup
      // 4e95: fconst_0
      // 4e96: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4e99: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4e9c: bipush 0
      // 4e9d: sipush 128
      // 4ea0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4ea3: ldc_w 56.0
      // 4ea6: ldc_w -133.0
      // 4ea9: ldc_w -15.0
      // 4eac: ldc_w 16.0
      // 4eaf: ldc_w 16.0
      // 4eb2: ldc_w 16.0
      // 4eb5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4eb8: dup
      // 4eb9: fconst_0
      // 4eba: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4ebd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4ec0: bipush 0
      // 4ec1: sipush 128
      // 4ec4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4ec7: ldc_w 104.0
      // 4eca: ldc_w -101.0
      // 4ecd: fconst_1
      // 4ece: ldc_w 16.0
      // 4ed1: ldc_w 16.0
      // 4ed4: ldc_w 16.0
      // 4ed7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4eda: dup
      // 4edb: fconst_0
      // 4edc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4edf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4ee2: bipush 0
      // 4ee3: sipush 128
      // 4ee6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4ee9: ldc_w 104.0
      // 4eec: ldc_w -101.0
      // 4eef: ldc_w -15.0
      // 4ef2: ldc_w 16.0
      // 4ef5: ldc_w 16.0
      // 4ef8: ldc_w 16.0
      // 4efb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4efe: dup
      // 4eff: fconst_0
      // 4f00: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4f03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f06: bipush 0
      // 4f07: sipush 128
      // 4f0a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f0d: ldc_w 72.0
      // 4f10: ldc_w -117.0
      // 4f13: ldc_w 33.0
      // 4f16: ldc_w 16.0
      // 4f19: ldc_w 16.0
      // 4f1c: ldc_w 16.0
      // 4f1f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4f22: dup
      // 4f23: fconst_0
      // 4f24: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4f27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f2a: bipush 0
      // 4f2b: sipush 350
      // 4f2e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f31: ldc_w 88.0
      // 4f34: ldc_w -117.0
      // 4f37: fconst_1
      // 4f38: ldc_w 16.0
      // 4f3b: ldc_w 16.0
      // 4f3e: ldc_w 16.0
      // 4f41: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4f44: dup
      // 4f45: fconst_0
      // 4f46: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4f49: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f4c: bipush 47
      // 4f4e: sipush 350
      // 4f51: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f54: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f57: ldc_w 72.0
      // 4f5a: ldc_w -117.0
      // 4f5d: fconst_1
      // 4f5e: ldc_w 16.0
      // 4f61: ldc_w 16.0
      // 4f64: ldc_w 16.0
      // 4f67: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4f6a: dup
      // 4f6b: fconst_0
      // 4f6c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4f6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f72: bipush 0
      // 4f73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f76: bipush 0
      // 4f77: sipush 128
      // 4f7a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f7d: ldc_w 104.0
      // 4f80: ldc_w -117.0
      // 4f83: fconst_1
      // 4f84: ldc_w 16.0
      // 4f87: ldc_w 16.0
      // 4f8a: ldc_w 16.0
      // 4f8d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4f90: dup
      // 4f91: fconst_0
      // 4f92: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4f95: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f98: bipush 0
      // 4f99: sipush 128
      // 4f9c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4f9f: ldc_w 104.0
      // 4fa2: ldc_w -133.0
      // 4fa5: fconst_1
      // 4fa6: ldc_w 16.0
      // 4fa9: ldc_w 16.0
      // 4fac: ldc_w 16.0
      // 4faf: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4fb2: dup
      // 4fb3: fconst_0
      // 4fb4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4fb7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4fba: bipush 0
      // 4fbb: sipush 128
      // 4fbe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4fc1: ldc_w 72.0
      // 4fc4: ldc_w -133.0
      // 4fc7: fconst_1
      // 4fc8: ldc_w 16.0
      // 4fcb: ldc_w 16.0
      // 4fce: ldc_w 16.0
      // 4fd1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4fd4: dup
      // 4fd5: fconst_0
      // 4fd6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4fd9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4fdc: bipush 0
      // 4fdd: sipush 128
      // 4fe0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 4fe3: ldc_w 72.0
      // 4fe6: ldc_w -133.0
      // 4fe9: ldc_w 33.0
      // 4fec: ldc_w 16.0
      // 4fef: ldc_w 16.0
      // 4ff2: ldc_w 16.0
      // 4ff5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 4ff8: dup
      // 4ff9: fconst_0
      // 4ffa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 4ffd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5000: bipush 0
      // 5001: sipush 128
      // 5004: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5007: ldc_w 88.0
      // 500a: ldc_w -133.0
      // 500d: ldc_w 33.0
      // 5010: ldc_w 16.0
      // 5013: ldc_w 16.0
      // 5016: ldc_w 16.0
      // 5019: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 501c: dup
      // 501d: fconst_0
      // 501e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5021: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5024: bipush 0
      // 5025: sipush 128
      // 5028: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 502b: ldc_w 88.0
      // 502e: ldc_w -117.0
      // 5031: ldc_w 33.0
      // 5034: ldc_w 16.0
      // 5037: ldc_w 16.0
      // 503a: ldc_w 16.0
      // 503d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5040: dup
      // 5041: fconst_0
      // 5042: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5045: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5048: bipush 47
      // 504a: sipush 128
      // 504d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5050: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5053: ldc_w 104.0
      // 5056: ldc_w -101.0
      // 5059: ldc_w 33.0
      // 505c: ldc_w 16.0
      // 505f: ldc_w 16.0
      // 5062: ldc_w 16.0
      // 5065: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5068: dup
      // 5069: fconst_0
      // 506a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 506d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5070: bipush 0
      // 5071: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5074: bipush 47
      // 5076: sipush 128
      // 5079: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 507c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 507f: ldc_w 104.0
      // 5082: ldc_w -101.0
      // 5085: ldc_w 49.0
      // 5088: ldc_w 16.0
      // 508b: ldc_w 16.0
      // 508e: ldc_w 16.0
      // 5091: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5094: dup
      // 5095: fconst_0
      // 5096: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5099: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 509c: bipush 0
      // 509d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 50a0: bipush 0
      // 50a1: sipush 128
      // 50a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 50a7: ldc_w 104.0
      // 50aa: ldc_w -117.0
      // 50ad: ldc_w 33.0
      // 50b0: ldc_w 16.0
      // 50b3: ldc_w 16.0
      // 50b6: ldc_w 16.0
      // 50b9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 50bc: dup
      // 50bd: fconst_0
      // 50be: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 50c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 50c4: bipush 0
      // 50c5: sipush 128
      // 50c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 50cb: ldc_w 120.0
      // 50ce: ldc_w -117.0
      // 50d1: ldc_w 33.0
      // 50d4: ldc_w 16.0
      // 50d7: ldc_w 16.0
      // 50da: ldc_w 16.0
      // 50dd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 50e0: dup
      // 50e1: fconst_0
      // 50e2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 50e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 50e8: bipush 0
      // 50e9: sipush 128
      // 50ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 50ef: ldc_w 72.0
      // 50f2: ldc_w -149.0
      // 50f5: ldc_w 49.0
      // 50f8: ldc_w 16.0
      // 50fb: ldc_w 16.0
      // 50fe: ldc_w 16.0
      // 5101: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5104: dup
      // 5105: fconst_0
      // 5106: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5109: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 510c: bipush 0
      // 510d: sipush 128
      // 5110: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5113: ldc_w 72.0
      // 5116: ldc_w -117.0
      // 5119: ldc_w 49.0
      // 511c: ldc_w 16.0
      // 511f: ldc_w 16.0
      // 5122: ldc_w 16.0
      // 5125: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5128: dup
      // 5129: fconst_0
      // 512a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 512d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5130: bipush 0
      // 5131: sipush 350
      // 5134: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5137: ldc_w 104.0
      // 513a: ldc_w -101.0
      // 513d: ldc_w 17.0
      // 5140: ldc_w 16.0
      // 5143: ldc_w 16.0
      // 5146: ldc_w 16.0
      // 5149: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 514c: dup
      // 514d: fconst_0
      // 514e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5151: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5154: bipush 0
      // 5155: sipush 128
      // 5158: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 515b: ldc_w 120.0
      // 515e: ldc_w -101.0
      // 5161: ldc_w 17.0
      // 5164: ldc_w 16.0
      // 5167: ldc_w 16.0
      // 516a: ldc_w 16.0
      // 516d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5170: dup
      // 5171: fconst_0
      // 5172: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5175: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5178: bipush 0
      // 5179: sipush 128
      // 517c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 517f: ldc_w 24.0
      // 5182: ldc_w -69.0
      // 5185: ldc_w 33.0
      // 5188: ldc_w 16.0
      // 518b: ldc_w 16.0
      // 518e: ldc_w 16.0
      // 5191: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5194: dup
      // 5195: fconst_0
      // 5196: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5199: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 519c: bipush 0
      // 519d: sipush 128
      // 51a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 51a3: ldc_w 40.0
      // 51a6: ldc_w -69.0
      // 51a9: ldc_w 49.0
      // 51ac: ldc_w 16.0
      // 51af: ldc_w 16.0
      // 51b2: ldc_w 16.0
      // 51b5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 51b8: dup
      // 51b9: fconst_0
      // 51ba: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 51bd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 51c0: bipush 0
      // 51c1: sipush 128
      // 51c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 51c7: ldc_w -8.0
      // 51ca: ldc_w -37.0
      // 51cd: ldc_w 17.0
      // 51d0: ldc_w 16.0
      // 51d3: ldc_w 16.0
      // 51d6: ldc_w 16.0
      // 51d9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 51dc: dup
      // 51dd: fconst_0
      // 51de: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 51e1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 51e4: bipush 0
      // 51e5: sipush 128
      // 51e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 51eb: ldc_w -24.0
      // 51ee: ldc_w -53.0
      // 51f1: ldc_w -15.0
      // 51f4: ldc_w 16.0
      // 51f7: ldc_w 16.0
      // 51fa: ldc_w 16.0
      // 51fd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5200: dup
      // 5201: fconst_0
      // 5202: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5205: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5208: bipush 0
      // 5209: sipush 128
      // 520c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 520f: ldc_w -8.0
      // 5212: ldc_w -69.0
      // 5215: ldc_w -15.0
      // 5218: ldc_w 16.0
      // 521b: ldc_w 16.0
      // 521e: ldc_w 16.0
      // 5221: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5224: dup
      // 5225: fconst_0
      // 5226: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5229: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 522c: bipush 0
      // 522d: sipush 128
      // 5230: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5233: ldc_w 8.0
      // 5236: ldc_w -69.0
      // 5239: ldc_w -15.0
      // 523c: ldc_w 16.0
      // 523f: ldc_w 16.0
      // 5242: ldc_w 16.0
      // 5245: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5248: dup
      // 5249: fconst_0
      // 524a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 524d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5250: bipush 0
      // 5251: sipush 128
      // 5254: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5257: ldc_w 8.0
      // 525a: ldc_w -85.0
      // 525d: ldc_w -15.0
      // 5260: ldc_w 16.0
      // 5263: ldc_w 16.0
      // 5266: ldc_w 16.0
      // 5269: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 526c: dup
      // 526d: fconst_0
      // 526e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5271: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5274: bipush 0
      // 5275: sipush 128
      // 5278: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 527b: ldc_w 8.0
      // 527e: ldc_w -69.0
      // 5281: ldc_w -31.0
      // 5284: ldc_w 16.0
      // 5287: ldc_w 16.0
      // 528a: ldc_w 16.0
      // 528d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5290: dup
      // 5291: fconst_0
      // 5292: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5295: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5298: bipush 0
      // 5299: sipush 128
      // 529c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 529f: ldc_w 8.0
      // 52a2: ldc_w -53.0
      // 52a5: ldc_w -47.0
      // 52a8: ldc_w 16.0
      // 52ab: ldc_w 16.0
      // 52ae: ldc_w 16.0
      // 52b1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 52b4: dup
      // 52b5: fconst_0
      // 52b6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 52b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 52bc: bipush 0
      // 52bd: sipush 128
      // 52c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 52c3: ldc_w 24.0
      // 52c6: ldc_w -53.0
      // 52c9: ldc_w -63.0
      // 52cc: ldc_w 16.0
      // 52cf: ldc_w 16.0
      // 52d2: ldc_w 16.0
      // 52d5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 52d8: dup
      // 52d9: fconst_0
      // 52da: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 52dd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 52e0: bipush 0
      // 52e1: sipush 128
      // 52e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 52e7: ldc_w 24.0
      // 52ea: ldc_w -85.0
      // 52ed: ldc_w -47.0
      // 52f0: ldc_w 16.0
      // 52f3: ldc_w 16.0
      // 52f6: ldc_w 16.0
      // 52f9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 52fc: dup
      // 52fd: fconst_0
      // 52fe: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5301: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5304: bipush 0
      // 5305: sipush 128
      // 5308: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 530b: ldc_w 24.0
      // 530e: ldc_w -85.0
      // 5311: ldc_w -31.0
      // 5314: ldc_w 16.0
      // 5317: ldc_w 16.0
      // 531a: ldc_w 16.0
      // 531d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5320: dup
      // 5321: fconst_0
      // 5322: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5325: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5328: bipush 0
      // 5329: sipush 128
      // 532c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 532f: ldc_w 24.0
      // 5332: ldc_w -85.0
      // 5335: ldc_w -15.0
      // 5338: ldc_w 16.0
      // 533b: ldc_w 16.0
      // 533e: ldc_w 16.0
      // 5341: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5344: dup
      // 5345: fconst_0
      // 5346: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5349: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 534c: bipush 0
      // 534d: sipush 128
      // 5350: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5353: ldc_w 40.0
      // 5356: ldc_w -53.0
      // 5359: ldc_w -63.0
      // 535c: ldc_w 16.0
      // 535f: ldc_w 16.0
      // 5362: ldc_w 16.0
      // 5365: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5368: dup
      // 5369: fconst_0
      // 536a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 536d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5370: bipush 0
      // 5371: sipush 432
      // 5374: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5377: ldc_w 40.0
      // 537a: ldc_w -53.0
      // 537d: ldc_w -31.0
      // 5380: ldc_w 16.0
      // 5383: ldc_w 16.0
      // 5386: ldc_w 16.0
      // 5389: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 538c: dup
      // 538d: fconst_0
      // 538e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5391: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5394: bipush 0
      // 5395: sipush 432
      // 5398: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 539b: ldc_w 56.0
      // 539e: ldc_w -53.0
      // 53a1: ldc_w 17.0
      // 53a4: ldc_w 16.0
      // 53a7: ldc_w 16.0
      // 53aa: ldc_w 16.0
      // 53ad: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 53b0: dup
      // 53b1: fconst_0
      // 53b2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 53b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 53b8: bipush 0
      // 53b9: sipush 432
      // 53bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 53bf: ldc_w 56.0
      // 53c2: ldc_w -53.0
      // 53c5: fconst_1
      // 53c6: ldc_w 16.0
      // 53c9: ldc_w 16.0
      // 53cc: ldc_w 16.0
      // 53cf: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 53d2: dup
      // 53d3: fconst_0
      // 53d4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 53d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 53da: bipush 0
      // 53db: sipush 432
      // 53de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 53e1: ldc_w 40.0
      // 53e4: ldc_w -37.0
      // 53e7: ldc_w 17.0
      // 53ea: ldc_w 16.0
      // 53ed: ldc_w 16.0
      // 53f0: ldc_w 16.0
      // 53f3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 53f6: dup
      // 53f7: fconst_0
      // 53f8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 53fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 53fe: bipush 0
      // 53ff: sipush 432
      // 5402: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5405: ldc_w 40.0
      // 5408: ldc_w -21.0
      // 540b: ldc_w 17.0
      // 540e: ldc_w 16.0
      // 5411: ldc_w 16.0
      // 5414: ldc_w 16.0
      // 5417: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 541a: dup
      // 541b: fconst_0
      // 541c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 541f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5422: bipush 0
      // 5423: sipush 432
      // 5426: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5429: ldc_w 40.0
      // 542c: ldc_w -21.0
      // 542f: fconst_1
      // 5430: ldc_w 16.0
      // 5433: ldc_w 16.0
      // 5436: ldc_w 16.0
      // 5439: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 543c: dup
      // 543d: fconst_0
      // 543e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5441: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5444: bipush 0
      // 5445: sipush 432
      // 5448: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 544b: ldc_w 40.0
      // 544e: ldc_w -37.0
      // 5451: ldc_w -15.0
      // 5454: ldc_w 16.0
      // 5457: ldc_w 16.0
      // 545a: ldc_w 16.0
      // 545d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5460: dup
      // 5461: fconst_0
      // 5462: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5465: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5468: bipush 0
      // 5469: sipush 432
      // 546c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 546f: ldc_w 56.0
      // 5472: ldc_w -53.0
      // 5475: ldc_w -15.0
      // 5478: ldc_w 16.0
      // 547b: ldc_w 16.0
      // 547e: ldc_w 16.0
      // 5481: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5484: dup
      // 5485: fconst_0
      // 5486: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5489: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 548c: bipush 0
      // 548d: sipush 432
      // 5490: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5493: ldc_w 56.0
      // 5496: ldc_w -53.0
      // 5499: ldc_w -31.0
      // 549c: ldc_w 16.0
      // 549f: ldc_w 16.0
      // 54a2: ldc_w 16.0
      // 54a5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 54a8: dup
      // 54a9: fconst_0
      // 54aa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 54ad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 54b0: sipush 348
      // 54b3: sipush 432
      // 54b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 54b9: ldc_w 56.0
      // 54bc: ldc_w -69.0
      // 54bf: ldc_w -31.0
      // 54c2: ldc_w 16.0
      // 54c5: ldc_w 16.0
      // 54c8: ldc_w 16.0
      // 54cb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 54ce: dup
      // 54cf: fconst_0
      // 54d0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 54d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 54d6: sipush 348
      // 54d9: sipush 432
      // 54dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 54df: ldc_w 56.0
      // 54e2: ldc_w -69.0
      // 54e5: ldc_w -15.0
      // 54e8: ldc_w 16.0
      // 54eb: ldc_w 16.0
      // 54ee: ldc_w 16.0
      // 54f1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 54f4: dup
      // 54f5: fconst_0
      // 54f6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 54f9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 54fc: bipush 0
      // 54fd: sipush 432
      // 5500: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5503: ldc_w 40.0
      // 5506: ldc_w -21.0
      // 5509: ldc_w -47.0
      // 550c: ldc_w 16.0
      // 550f: ldc_w 16.0
      // 5512: ldc_w 16.0
      // 5515: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5518: dup
      // 5519: fconst_0
      // 551a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 551d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5520: bipush 0
      // 5521: sipush 432
      // 5524: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5527: ldc_w 40.0
      // 552a: ldc_w -21.0
      // 552d: ldc_w -31.0
      // 5530: ldc_w 16.0
      // 5533: ldc_w 16.0
      // 5536: ldc_w 16.0
      // 5539: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 553c: dup
      // 553d: fconst_0
      // 553e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5541: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5544: bipush 0
      // 5545: sipush 432
      // 5548: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 554b: ldc_w 40.0
      // 554e: ldc_w -37.0
      // 5551: ldc_w -31.0
      // 5554: ldc_w 16.0
      // 5557: ldc_w 16.0
      // 555a: ldc_w 16.0
      // 555d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5560: dup
      // 5561: fconst_0
      // 5562: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5565: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5568: bipush 0
      // 5569: sipush 432
      // 556c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 556f: ldc_w 40.0
      // 5572: ldc_w -37.0
      // 5575: ldc_w -47.0
      // 5578: ldc_w 16.0
      // 557b: ldc_w 16.0
      // 557e: ldc_w 16.0
      // 5581: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5584: dup
      // 5585: fconst_0
      // 5586: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5589: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 558c: bipush 0
      // 558d: sipush 432
      // 5590: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5593: ldc_w 40.0
      // 5596: ldc_w -21.0
      // 5599: ldc_w -15.0
      // 559c: ldc_w 16.0
      // 559f: ldc_w 16.0
      // 55a2: ldc_w 16.0
      // 55a5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 55a8: dup
      // 55a9: fconst_0
      // 55aa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 55ad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 55b0: bipush 0
      // 55b1: sipush 432
      // 55b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 55b7: ldc_w 40.0
      // 55ba: ldc_w -37.0
      // 55bd: fconst_1
      // 55be: ldc_w 16.0
      // 55c1: ldc_w 16.0
      // 55c4: ldc_w 16.0
      // 55c7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 55ca: dup
      // 55cb: fconst_0
      // 55cc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 55cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 55d2: bipush 0
      // 55d3: sipush 432
      // 55d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 55d9: ldc_w 72.0
      // 55dc: ldc_w -53.0
      // 55df: ldc_w 33.0
      // 55e2: ldc_w 16.0
      // 55e5: ldc_w 16.0
      // 55e8: ldc_w 16.0
      // 55eb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 55ee: dup
      // 55ef: fconst_0
      // 55f0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 55f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 55f6: bipush 0
      // 55f7: sipush 432
      // 55fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 55fd: ldc_w 88.0
      // 5600: ldc_w -53.0
      // 5603: ldc_w 33.0
      // 5606: ldc_w 16.0
      // 5609: ldc_w 16.0
      // 560c: ldc_w 16.0
      // 560f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5612: dup
      // 5613: fconst_0
      // 5614: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5617: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 561a: bipush 0
      // 561b: sipush 432
      // 561e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5621: ldc_w 104.0
      // 5624: ldc_w -53.0
      // 5627: ldc_w 33.0
      // 562a: ldc_w 16.0
      // 562d: ldc_w 16.0
      // 5630: ldc_w 16.0
      // 5633: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5636: dup
      // 5637: fconst_0
      // 5638: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 563b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 563e: bipush 0
      // 563f: sipush 432
      // 5642: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5645: ldc_w 104.0
      // 5648: ldc_w -69.0
      // 564b: ldc_w 33.0
      // 564e: ldc_w 16.0
      // 5651: ldc_w 16.0
      // 5654: ldc_w 16.0
      // 5657: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 565a: dup
      // 565b: fconst_0
      // 565c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 565f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5662: bipush 0
      // 5663: sipush 432
      // 5666: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5669: ldc_w 104.0
      // 566c: ldc_w -85.0
      // 566f: ldc_w 33.0
      // 5672: ldc_w 16.0
      // 5675: ldc_w 16.0
      // 5678: ldc_w 16.0
      // 567b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 567e: dup
      // 567f: fconst_0
      // 5680: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5683: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5686: bipush 0
      // 5687: sipush 432
      // 568a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 568d: ldc_w 120.0
      // 5690: ldc_w -69.0
      // 5693: ldc_w 33.0
      // 5696: ldc_w 16.0
      // 5699: ldc_w 16.0
      // 569c: ldc_w 16.0
      // 569f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 56a2: dup
      // 56a3: fconst_0
      // 56a4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 56a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 56aa: sipush 348
      // 56ad: sipush 432
      // 56b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 56b3: ldc_w 120.0
      // 56b6: ldc_w -85.0
      // 56b9: ldc_w 17.0
      // 56bc: ldc_w 16.0
      // 56bf: ldc_w 16.0
      // 56c2: ldc_w 16.0
      // 56c5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 56c8: dup
      // 56c9: fconst_0
      // 56ca: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 56cd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 56d0: sipush 430
      // 56d3: sipush 432
      // 56d6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 56d9: ldc_w 120.0
      // 56dc: ldc_w -85.0
      // 56df: fconst_1
      // 56e0: ldc_w 16.0
      // 56e3: ldc_w 16.0
      // 56e6: ldc_w 16.0
      // 56e9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 56ec: dup
      // 56ed: fconst_0
      // 56ee: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 56f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 56f4: sipush 430
      // 56f7: sipush 432
      // 56fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 56fd: ldc_w 120.0
      // 5700: ldc_w -85.0
      // 5703: ldc_w -15.0
      // 5706: ldc_w 16.0
      // 5709: ldc_w 16.0
      // 570c: ldc_w 16.0
      // 570f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5712: dup
      // 5713: fconst_0
      // 5714: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5717: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 571a: sipush 512
      // 571d: sipush 432
      // 5720: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5723: ldc_w 120.0
      // 5726: ldc_w -85.0
      // 5729: ldc_w -31.0
      // 572c: ldc_w 16.0
      // 572f: ldc_w 16.0
      // 5732: ldc_w 16.0
      // 5735: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5738: dup
      // 5739: fconst_0
      // 573a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 573d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5740: bipush 0
      // 5741: sipush 432
      // 5744: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5747: ldc_w 40.0
      // 574a: ldc_w -53.0
      // 574d: fconst_1
      // 574e: ldc_w 16.0
      // 5751: ldc_w 16.0
      // 5754: ldc_w 16.0
      // 5757: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 575a: dup
      // 575b: fconst_0
      // 575c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 575f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5762: bipush 0
      // 5763: sipush 432
      // 5766: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5769: ldc_w 40.0
      // 576c: ldc_w -53.0
      // 576f: ldc_w 17.0
      // 5772: ldc_w 16.0
      // 5775: ldc_w 16.0
      // 5778: ldc_w 16.0
      // 577b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 577e: dup
      // 577f: fconst_0
      // 5780: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5783: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5786: bipush 0
      // 5787: sipush 432
      // 578a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 578d: ldc_w 40.0
      // 5790: ldc_w -53.0
      // 5793: ldc_w -15.0
      // 5796: ldc_w 16.0
      // 5799: ldc_w 16.0
      // 579c: ldc_w 16.0
      // 579f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 57a2: dup
      // 57a3: fconst_0
      // 57a4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 57a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 57aa: bipush 47
      // 57ac: sipush 128
      // 57af: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 57b2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 57b5: ldc_w 24.0
      // 57b8: ldc_w -37.0
      // 57bb: ldc_w -79.0
      // 57be: ldc_w 16.0
      // 57c1: ldc_w 16.0
      // 57c4: ldc_w 16.0
      // 57c7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 57ca: dup
      // 57cb: fconst_0
      // 57cc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 57cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 57d2: bipush 0
      // 57d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 57d6: bipush 0
      // 57d7: sipush 128
      // 57da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 57dd: ldc_w 40.0
      // 57e0: ldc_w -37.0
      // 57e3: ldc_w -79.0
      // 57e6: ldc_w 16.0
      // 57e9: ldc_w 16.0
      // 57ec: ldc_w 16.0
      // 57ef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 57f2: dup
      // 57f3: fconst_0
      // 57f4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 57f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 57fa: bipush 0
      // 57fb: sipush 128
      // 57fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5801: ldc_w 56.0
      // 5804: ldc_w -37.0
      // 5807: ldc_w -63.0
      // 580a: ldc_w 16.0
      // 580d: ldc_w 16.0
      // 5810: ldc_w 16.0
      // 5813: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5816: dup
      // 5817: fconst_0
      // 5818: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 581b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 581e: bipush 0
      // 581f: sipush 128
      // 5822: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5825: ldc_w 72.0
      // 5828: ldc_w -37.0
      // 582b: ldc_w -63.0
      // 582e: ldc_w 16.0
      // 5831: ldc_w 16.0
      // 5834: ldc_w 16.0
      // 5837: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 583a: dup
      // 583b: fconst_0
      // 583c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 583f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5842: bipush 47
      // 5844: sipush 128
      // 5847: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 584a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 584d: ldc_w 72.0
      // 5850: ldc_w -53.0
      // 5853: ldc_w -63.0
      // 5856: ldc_w 16.0
      // 5859: ldc_w 16.0
      // 585c: ldc_w 16.0
      // 585f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5862: dup
      // 5863: fconst_0
      // 5864: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5867: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 586a: bipush 0
      // 586b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 586e: bipush 47
      // 5870: sipush 128
      // 5873: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5876: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5879: ldc_w 72.0
      // 587c: ldc_w -53.0
      // 587f: ldc_w -79.0
      // 5882: ldc_w 16.0
      // 5885: ldc_w 16.0
      // 5888: ldc_w 16.0
      // 588b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 588e: dup
      // 588f: fconst_0
      // 5890: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5893: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5896: bipush 0
      // 5897: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 589a: bipush 47
      // 589c: sipush 128
      // 589f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58a5: ldc_w 72.0
      // 58a8: ldc_w -69.0
      // 58ab: ldc_w -63.0
      // 58ae: ldc_w 16.0
      // 58b1: ldc_w 16.0
      // 58b4: ldc_w 16.0
      // 58b7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 58ba: dup
      // 58bb: fconst_0
      // 58bc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 58bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58c2: bipush 0
      // 58c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58c6: bipush 47
      // 58c8: sipush 128
      // 58cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58ce: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58d1: ldc_w 72.0
      // 58d4: ldc_w -69.0
      // 58d7: ldc_w -79.0
      // 58da: ldc_w 16.0
      // 58dd: ldc_w 16.0
      // 58e0: ldc_w 16.0
      // 58e3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 58e6: dup
      // 58e7: fconst_0
      // 58e8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 58eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58ee: bipush 0
      // 58ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58f2: bipush 0
      // 58f3: sipush 128
      // 58f6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 58f9: ldc_w 56.0
      // 58fc: ldc_w -37.0
      // 58ff: ldc_w -79.0
      // 5902: ldc_w 16.0
      // 5905: ldc_w 16.0
      // 5908: ldc_w 16.0
      // 590b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 590e: dup
      // 590f: fconst_0
      // 5910: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5913: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5916: bipush 0
      // 5917: sipush 128
      // 591a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 591d: ldc_w 72.0
      // 5920: ldc_w -37.0
      // 5923: ldc_w -79.0
      // 5926: ldc_w 16.0
      // 5929: ldc_w 16.0
      // 592c: ldc_w 16.0
      // 592f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5932: dup
      // 5933: fconst_0
      // 5934: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5937: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 593a: bipush 0
      // 593b: sipush 128
      // 593e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5941: ldc_w 72.0
      // 5944: ldc_w -85.0
      // 5947: ldc_w -95.0
      // 594a: ldc_w 16.0
      // 594d: ldc_w 16.0
      // 5950: ldc_w 16.0
      // 5953: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5956: dup
      // 5957: fconst_0
      // 5958: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 595b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 595e: bipush 0
      // 595f: sipush 128
      // 5962: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5965: ldc_w 104.0
      // 5968: ldc_w -37.0
      // 596b: ldc_w -79.0
      // 596e: ldc_w 16.0
      // 5971: ldc_w 16.0
      // 5974: ldc_w 16.0
      // 5977: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 597a: dup
      // 597b: fconst_0
      // 597c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 597f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5982: bipush 0
      // 5983: sipush 128
      // 5986: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5989: ldc_w 88.0
      // 598c: ldc_w -37.0
      // 598f: ldc_w -79.0
      // 5992: ldc_w 16.0
      // 5995: ldc_w 16.0
      // 5998: ldc_w 16.0
      // 599b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 599e: dup
      // 599f: fconst_0
      // 59a0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 59a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59a6: bipush 47
      // 59a8: sipush 496
      // 59ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59b1: ldc_w 88.0
      // 59b4: ldc_w -37.0
      // 59b7: ldc_w 17.0
      // 59ba: ldc_w 16.0
      // 59bd: ldc_w 16.0
      // 59c0: ldc_w 16.0
      // 59c3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 59c6: dup
      // 59c7: fconst_0
      // 59c8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 59cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59ce: bipush 0
      // 59cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59d2: bipush 47
      // 59d4: sipush 496
      // 59d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59dd: ldc_w 104.0
      // 59e0: ldc_w -37.0
      // 59e3: ldc_w 17.0
      // 59e6: ldc_w 16.0
      // 59e9: ldc_w 16.0
      // 59ec: ldc_w 16.0
      // 59ef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 59f2: dup
      // 59f3: fconst_0
      // 59f4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 59f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59fa: bipush 0
      // 59fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 59fe: bipush 47
      // 5a00: sipush 496
      // 5a03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a06: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a09: ldc_w 120.0
      // 5a0c: ldc_w -53.0
      // 5a0f: ldc_w 17.0
      // 5a12: ldc_w 16.0
      // 5a15: ldc_w 16.0
      // 5a18: ldc_w 16.0
      // 5a1b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5a1e: dup
      // 5a1f: fconst_0
      // 5a20: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5a23: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a26: bipush 0
      // 5a27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a2a: bipush 47
      // 5a2c: sipush 496
      // 5a2f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a32: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a35: ldc_w 120.0
      // 5a38: ldc_w -53.0
      // 5a3b: fconst_1
      // 5a3c: ldc_w 16.0
      // 5a3f: ldc_w 16.0
      // 5a42: ldc_w 16.0
      // 5a45: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5a48: dup
      // 5a49: fconst_0
      // 5a4a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5a4d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a50: bipush 0
      // 5a51: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a54: bipush 47
      // 5a56: sipush 496
      // 5a59: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a5c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a5f: ldc_w 120.0
      // 5a62: ldc_w -53.0
      // 5a65: ldc_w -15.0
      // 5a68: ldc_w 16.0
      // 5a6b: ldc_w 16.0
      // 5a6e: ldc_w 16.0
      // 5a71: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5a74: dup
      // 5a75: fconst_0
      // 5a76: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5a79: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a7c: bipush 0
      // 5a7d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a80: sipush 496
      // 5a83: sipush 496
      // 5a86: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5a89: ldc_w 136.0
      // 5a8c: ldc_w -37.0
      // 5a8f: ldc_w -15.0
      // 5a92: ldc_w 16.0
      // 5a95: ldc_w 16.0
      // 5a98: ldc_w 16.0
      // 5a9b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5a9e: dup
      // 5a9f: fconst_0
      // 5aa0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5aa3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5aa6: sipush 496
      // 5aa9: sipush 496
      // 5aac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5aaf: ldc_w 152.0
      // 5ab2: ldc_w -37.0
      // 5ab5: ldc_w -15.0
      // 5ab8: ldc_w 16.0
      // 5abb: ldc_w 16.0
      // 5abe: ldc_w 16.0
      // 5ac1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5ac4: dup
      // 5ac5: fconst_0
      // 5ac6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5ac9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5acc: sipush 496
      // 5acf: sipush 496
      // 5ad2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5ad5: ldc_w 152.0
      // 5ad8: ldc_w -21.0
      // 5adb: ldc_w -15.0
      // 5ade: ldc_w 16.0
      // 5ae1: ldc_w 16.0
      // 5ae4: ldc_w 16.0
      // 5ae7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5aea: dup
      // 5aeb: fconst_0
      // 5aec: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5aef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5af2: sipush 496
      // 5af5: sipush 496
      // 5af8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5afb: ldc_w 168.0
      // 5afe: ldc_w -21.0
      // 5b01: ldc_w -15.0
      // 5b04: ldc_w 16.0
      // 5b07: ldc_w 16.0
      // 5b0a: ldc_w 16.0
      // 5b0d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5b10: dup
      // 5b11: fconst_0
      // 5b12: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5b15: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5b18: sipush 496
      // 5b1b: sipush 496
      // 5b1e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5b21: ldc_w 168.0
      // 5b24: ldc_w -37.0
      // 5b27: ldc_w -15.0
      // 5b2a: ldc_w 16.0
      // 5b2d: ldc_w 16.0
      // 5b30: ldc_w 16.0
      // 5b33: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5b36: dup
      // 5b37: fconst_0
      // 5b38: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5b3b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5b3e: sipush 496
      // 5b41: sipush 496
      // 5b44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5b47: ldc_w 168.0
      // 5b4a: ldc_w -53.0
      // 5b4d: ldc_w -15.0
      // 5b50: ldc_w 16.0
      // 5b53: ldc_w 16.0
      // 5b56: ldc_w 16.0
      // 5b59: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5b5c: dup
      // 5b5d: fconst_0
      // 5b5e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5b61: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5b64: sipush 496
      // 5b67: sipush 496
      // 5b6a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5b6d: ldc_w 184.0
      // 5b70: ldc_w -53.0
      // 5b73: ldc_w -15.0
      // 5b76: ldc_w 16.0
      // 5b79: ldc_w 16.0
      // 5b7c: ldc_w 16.0
      // 5b7f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5b82: dup
      // 5b83: fconst_0
      // 5b84: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5b87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5b8a: sipush 496
      // 5b8d: sipush 496
      // 5b90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5b93: ldc_w 184.0
      // 5b96: ldc_w -37.0
      // 5b99: ldc_w -15.0
      // 5b9c: ldc_w 16.0
      // 5b9f: ldc_w 16.0
      // 5ba2: ldc_w 16.0
      // 5ba5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5ba8: dup
      // 5ba9: fconst_0
      // 5baa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5bad: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5bb0: sipush 496
      // 5bb3: sipush 496
      // 5bb6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5bb9: ldc_w 184.0
      // 5bbc: ldc_w -21.0
      // 5bbf: ldc_w -15.0
      // 5bc2: ldc_w 16.0
      // 5bc5: ldc_w 16.0
      // 5bc8: ldc_w 16.0
      // 5bcb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5bce: dup
      // 5bcf: fconst_0
      // 5bd0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5bd3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5bd6: sipush 496
      // 5bd9: sipush 496
      // 5bdc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5bdf: ldc_w 200.0
      // 5be2: ldc_w -21.0
      // 5be5: ldc_w -15.0
      // 5be8: ldc_w 16.0
      // 5beb: ldc_w 16.0
      // 5bee: ldc_w 16.0
      // 5bf1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5bf4: dup
      // 5bf5: fconst_0
      // 5bf6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5bf9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5bfc: sipush 496
      // 5bff: sipush 496
      // 5c02: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c05: ldc_w 232.0
      // 5c08: ldc_w -37.0
      // 5c0b: ldc_w -15.0
      // 5c0e: ldc_w 16.0
      // 5c11: ldc_w 16.0
      // 5c14: ldc_w 16.0
      // 5c17: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5c1a: dup
      // 5c1b: fconst_0
      // 5c1c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5c1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c22: sipush 496
      // 5c25: sipush 496
      // 5c28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c2b: ldc_w 184.0
      // 5c2e: ldc_w -69.0
      // 5c31: ldc_w -15.0
      // 5c34: ldc_w 16.0
      // 5c37: ldc_w 16.0
      // 5c3a: ldc_w 16.0
      // 5c3d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5c40: dup
      // 5c41: fconst_0
      // 5c42: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5c45: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c48: sipush 432
      // 5c4b: sipush 350
      // 5c4e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c51: ldc_w 184.0
      // 5c54: ldc_w -101.0
      // 5c57: ldc_w -31.0
      // 5c5a: ldc_w 16.0
      // 5c5d: ldc_w 16.0
      // 5c60: ldc_w 16.0
      // 5c63: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5c66: dup
      // 5c67: fconst_0
      // 5c68: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5c6b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c6e: sipush 432
      // 5c71: sipush 350
      // 5c74: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c77: ldc_w 200.0
      // 5c7a: ldc_w -101.0
      // 5c7d: ldc_w -31.0
      // 5c80: ldc_w 16.0
      // 5c83: ldc_w 16.0
      // 5c86: ldc_w 16.0
      // 5c89: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5c8c: dup
      // 5c8d: fconst_0
      // 5c8e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5c91: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c94: bipush 40
      // 5c96: sipush 350
      // 5c99: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c9c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5c9f: ldc_w 200.0
      // 5ca2: ldc_w -101.0
      // 5ca5: ldc_w -47.0
      // 5ca8: ldc_w 16.0
      // 5cab: ldc_w 16.0
      // 5cae: ldc_w 16.0
      // 5cb1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5cb4: dup
      // 5cb5: fconst_0
      // 5cb6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5cb9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5cbc: bipush 0
      // 5cbd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5cc0: sipush 496
      // 5cc3: sipush 496
      // 5cc6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5cc9: ldc_w 200.0
      // 5ccc: ldc_w -53.0
      // 5ccf: ldc_w -15.0
      // 5cd2: ldc_w 16.0
      // 5cd5: ldc_w 16.0
      // 5cd8: ldc_w 16.0
      // 5cdb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5cde: dup
      // 5cdf: fconst_0
      // 5ce0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5ce3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5ce6: sipush 496
      // 5ce9: sipush 496
      // 5cec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5cef: ldc_w 200.0
      // 5cf2: ldc_w -37.0
      // 5cf5: ldc_w -15.0
      // 5cf8: ldc_w 16.0
      // 5cfb: ldc_w 16.0
      // 5cfe: ldc_w 16.0
      // 5d01: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5d04: dup
      // 5d05: fconst_0
      // 5d06: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5d09: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5d0c: sipush 496
      // 5d0f: sipush 496
      // 5d12: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5d15: ldc_w 216.0
      // 5d18: ldc_w -37.0
      // 5d1b: ldc_w -15.0
      // 5d1e: ldc_w 16.0
      // 5d21: ldc_w 16.0
      // 5d24: ldc_w 16.0
      // 5d27: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5d2a: dup
      // 5d2b: fconst_0
      // 5d2c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5d2f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5d32: sipush 496
      // 5d35: sipush 496
      // 5d38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5d3b: ldc_w 216.0
      // 5d3e: ldc_w -21.0
      // 5d41: ldc_w -15.0
      // 5d44: ldc_w 16.0
      // 5d47: ldc_w 16.0
      // 5d4a: ldc_w 16.0
      // 5d4d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5d50: dup
      // 5d51: fconst_0
      // 5d52: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5d55: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5d58: sipush 496
      // 5d5b: sipush 496
      // 5d5e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5d61: ldc_w 232.0
      // 5d64: ldc_w -21.0
      // 5d67: ldc_w -15.0
      // 5d6a: ldc_w 16.0
      // 5d6d: ldc_w 16.0
      // 5d70: ldc_w 16.0
      // 5d73: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5d76: dup
      // 5d77: fconst_0
      // 5d78: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5d7b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5d7e: sipush 496
      // 5d81: sipush 496
      // 5d84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5d87: ldc_w 200.0
      // 5d8a: ldc_w -69.0
      // 5d8d: ldc_w -15.0
      // 5d90: ldc_w 16.0
      // 5d93: ldc_w 16.0
      // 5d96: ldc_w 16.0
      // 5d99: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5d9c: dup
      // 5d9d: fconst_0
      // 5d9e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5da1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5da4: sipush 496
      // 5da7: sipush 496
      // 5daa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5dad: ldc_w 200.0
      // 5db0: ldc_w -85.0
      // 5db3: ldc_w -15.0
      // 5db6: ldc_w 16.0
      // 5db9: ldc_w 16.0
      // 5dbc: ldc_w 16.0
      // 5dbf: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5dc2: dup
      // 5dc3: fconst_0
      // 5dc4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5dc7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5dca: sipush 496
      // 5dcd: sipush 496
      // 5dd0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5dd3: ldc_w 216.0
      // 5dd6: ldc_w -85.0
      // 5dd9: ldc_w -15.0
      // 5ddc: ldc_w 16.0
      // 5ddf: ldc_w 16.0
      // 5de2: ldc_w 16.0
      // 5de5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5de8: dup
      // 5de9: fconst_0
      // 5dea: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5ded: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5df0: sipush 432
      // 5df3: bipush 0
      // 5df4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5df7: ldc_w 216.0
      // 5dfa: ldc_w -69.0
      // 5dfd: ldc_w -31.0
      // 5e00: ldc_w 16.0
      // 5e03: ldc_w 16.0
      // 5e06: ldc_w 16.0
      // 5e09: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5e0c: dup
      // 5e0d: fconst_0
      // 5e0e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5e11: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5e14: sipush 432
      // 5e17: bipush 0
      // 5e18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5e1b: ldc_w 248.0
      // 5e1e: ldc_w -69.0
      // 5e21: ldc_w -47.0
      // 5e24: ldc_w 16.0
      // 5e27: ldc_w 16.0
      // 5e2a: ldc_w 16.0
      // 5e2d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5e30: dup
      // 5e31: fconst_0
      // 5e32: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5e35: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5e38: sipush 432
      // 5e3b: bipush 0
      // 5e3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5e3f: ldc_w 232.0
      // 5e42: ldc_w -69.0
      // 5e45: ldc_w -63.0
      // 5e48: ldc_w 16.0
      // 5e4b: ldc_w 16.0
      // 5e4e: ldc_w 16.0
      // 5e51: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5e54: dup
      // 5e55: fconst_0
      // 5e56: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5e59: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5e5c: sipush 432
      // 5e5f: bipush 0
      // 5e60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5e63: ldc_w 232.0
      // 5e66: ldc_w -69.0
      // 5e69: ldc_w -47.0
      // 5e6c: ldc_w 16.0
      // 5e6f: ldc_w 16.0
      // 5e72: ldc_w 16.0
      // 5e75: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5e78: dup
      // 5e79: fconst_0
      // 5e7a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5e7d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5e80: sipush 432
      // 5e83: bipush 0
      // 5e84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5e87: ldc_w 248.0
      // 5e8a: ldc_w -69.0
      // 5e8d: ldc_w -31.0
      // 5e90: ldc_w 16.0
      // 5e93: ldc_w 16.0
      // 5e96: ldc_w 16.0
      // 5e99: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5e9c: dup
      // 5e9d: fconst_0
      // 5e9e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5ea1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5ea4: sipush 432
      // 5ea7: bipush 0
      // 5ea8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5eab: ldc_w 248.0
      // 5eae: ldc_w -69.0
      // 5eb1: fconst_1
      // 5eb2: ldc_w 16.0
      // 5eb5: ldc_w 16.0
      // 5eb8: ldc_w 16.0
      // 5ebb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5ebe: dup
      // 5ebf: fconst_0
      // 5ec0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5ec3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5ec6: sipush 432
      // 5ec9: bipush 0
      // 5eca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5ecd: ldc_w 248.0
      // 5ed0: ldc_w -37.0
      // 5ed3: fconst_1
      // 5ed4: ldc_w 16.0
      // 5ed7: ldc_w 16.0
      // 5eda: ldc_w 16.0
      // 5edd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5ee0: dup
      // 5ee1: fconst_0
      // 5ee2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5ee5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5ee8: sipush 432
      // 5eeb: bipush 0
      // 5eec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5eef: ldc_w 232.0
      // 5ef2: ldc_w -53.0
      // 5ef5: fconst_1
      // 5ef6: ldc_w 16.0
      // 5ef9: ldc_w 16.0
      // 5efc: ldc_w 16.0
      // 5eff: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5f02: dup
      // 5f03: fconst_0
      // 5f04: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5f07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f0a: sipush 432
      // 5f0d: bipush 0
      // 5f0e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f11: ldc_w 248.0
      // 5f14: ldc_w -53.0
      // 5f17: fconst_1
      // 5f18: ldc_w 16.0
      // 5f1b: ldc_w 16.0
      // 5f1e: ldc_w 16.0
      // 5f21: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5f24: dup
      // 5f25: fconst_0
      // 5f26: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5f29: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f2c: sipush 432
      // 5f2f: bipush 0
      // 5f30: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f33: ldc_w 248.0
      // 5f36: ldc_w -69.0
      // 5f39: ldc_w -15.0
      // 5f3c: ldc_w 16.0
      // 5f3f: ldc_w 16.0
      // 5f42: ldc_w 16.0
      // 5f45: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5f48: dup
      // 5f49: fconst_0
      // 5f4a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5f4d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f50: sipush 432
      // 5f53: bipush 0
      // 5f54: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f57: ldc_w 232.0
      // 5f5a: ldc_w -53.0
      // 5f5d: ldc_w -31.0
      // 5f60: ldc_w 16.0
      // 5f63: ldc_w 16.0
      // 5f66: ldc_w 16.0
      // 5f69: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5f6c: dup
      // 5f6d: fconst_0
      // 5f6e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5f71: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f74: sipush 432
      // 5f77: bipush 0
      // 5f78: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f7b: ldc_w 216.0
      // 5f7e: ldc_w -53.0
      // 5f81: ldc_w -31.0
      // 5f84: ldc_w 16.0
      // 5f87: ldc_w 16.0
      // 5f8a: ldc_w 16.0
      // 5f8d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5f90: dup
      // 5f91: fconst_0
      // 5f92: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5f95: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f98: sipush 432
      // 5f9b: bipush 0
      // 5f9c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5f9f: ldc_w 248.0
      // 5fa2: ldc_w -53.0
      // 5fa5: ldc_w -31.0
      // 5fa8: ldc_w 16.0
      // 5fab: ldc_w 16.0
      // 5fae: ldc_w 16.0
      // 5fb1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5fb4: dup
      // 5fb5: fconst_0
      // 5fb6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5fb9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5fbc: sipush 348
      // 5fbf: bipush 0
      // 5fc0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5fc3: ldc_w 248.0
      // 5fc6: ldc_w -37.0
      // 5fc9: ldc_w -31.0
      // 5fcc: ldc_w 16.0
      // 5fcf: ldc_w 16.0
      // 5fd2: ldc_w 16.0
      // 5fd5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5fd8: dup
      // 5fd9: fconst_0
      // 5fda: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 5fdd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5fe0: sipush 432
      // 5fe3: bipush 0
      // 5fe4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 5fe7: ldc_w 248.0
      // 5fea: ldc_w -21.0
      // 5fed: ldc_w -31.0
      // 5ff0: ldc_w 16.0
      // 5ff3: ldc_w 16.0
      // 5ff6: ldc_w 16.0
      // 5ff9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 5ffc: dup
      // 5ffd: fconst_0
      // 5ffe: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6001: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6004: sipush 432
      // 6007: bipush 0
      // 6008: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 600b: ldc_w 248.0
      // 600e: ldc_w -5.0
      // 6011: ldc_w -31.0
      // 6014: ldc_w 16.0
      // 6017: ldc_w 16.0
      // 601a: ldc_w 16.0
      // 601d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6020: dup
      // 6021: fconst_0
      // 6022: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6025: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6028: sipush 432
      // 602b: bipush 0
      // 602c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 602f: ldc_w 248.0
      // 6032: ldc_w 11.0
      // 6035: ldc_w -31.0
      // 6038: ldc_w 16.0
      // 603b: ldc_w 16.0
      // 603e: ldc_w 16.0
      // 6041: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6044: dup
      // 6045: fconst_0
      // 6046: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6049: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 604c: sipush 432
      // 604f: sipush 512
      // 6052: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6055: ldc_w 232.0
      // 6058: ldc_w -5.0
      // 605b: ldc_w -47.0
      // 605e: ldc_w 16.0
      // 6061: ldc_w 16.0
      // 6064: ldc_w 16.0
      // 6067: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 606a: dup
      // 606b: fconst_0
      // 606c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 606f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6072: sipush 432
      // 6075: bipush 0
      // 6076: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6079: ldc_w 232.0
      // 607c: ldc_w 11.0
      // 607f: ldc_w -47.0
      // 6082: ldc_w 16.0
      // 6085: ldc_w 16.0
      // 6088: ldc_w 16.0
      // 608b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 608e: dup
      // 608f: fconst_0
      // 6090: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6093: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6096: sipush 432
      // 6099: bipush 0
      // 609a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 609d: ldc_w 264.0
      // 60a0: ldc_w 27.0
      // 60a3: ldc_w -31.0
      // 60a6: ldc_w 16.0
      // 60a9: ldc_w 16.0
      // 60ac: ldc_w 16.0
      // 60af: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 60b2: dup
      // 60b3: fconst_0
      // 60b4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 60b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 60ba: sipush 432
      // 60bd: bipush 0
      // 60be: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 60c1: ldc_w 280.0
      // 60c4: ldc_w 27.0
      // 60c7: ldc_w -31.0
      // 60ca: ldc_w 16.0
      // 60cd: ldc_w 16.0
      // 60d0: ldc_w 16.0
      // 60d3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 60d6: dup
      // 60d7: fconst_0
      // 60d8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 60db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 60de: sipush 432
      // 60e1: bipush 0
      // 60e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 60e5: ldc_w 280.0
      // 60e8: ldc_w 43.0
      // 60eb: ldc_w -31.0
      // 60ee: ldc_w 16.0
      // 60f1: ldc_w 16.0
      // 60f4: ldc_w 16.0
      // 60f7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 60fa: dup
      // 60fb: fconst_0
      // 60fc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 60ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6102: sipush 432
      // 6105: bipush 0
      // 6106: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6109: ldc_w 280.0
      // 610c: ldc_w 59.0
      // 610f: ldc_w -31.0
      // 6112: ldc_w 16.0
      // 6115: ldc_w 16.0
      // 6118: ldc_w 16.0
      // 611b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 611e: dup
      // 611f: fconst_0
      // 6120: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6123: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6126: sipush 432
      // 6129: bipush 0
      // 612a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 612d: ldc_w 264.0
      // 6130: ldc_w 11.0
      // 6133: ldc_w -31.0
      // 6136: ldc_w 16.0
      // 6139: ldc_w 16.0
      // 613c: ldc_w 16.0
      // 613f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6142: dup
      // 6143: fconst_0
      // 6144: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6147: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 614a: sipush 496
      // 614d: sipush 496
      // 6150: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6153: ldc_w 168.0
      // 6156: ldc_w -85.0
      // 6159: ldc_w -15.0
      // 615c: ldc_w 16.0
      // 615f: ldc_w 16.0
      // 6162: ldc_w 16.0
      // 6165: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6168: dup
      // 6169: fconst_0
      // 616a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 616d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6170: sipush 432
      // 6173: sipush 496
      // 6176: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6179: ldc_w 184.0
      // 617c: ldc_w -85.0
      // 617f: ldc_w -31.0
      // 6182: ldc_w 16.0
      // 6185: ldc_w 16.0
      // 6188: ldc_w 16.0
      // 618b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 618e: dup
      // 618f: fconst_0
      // 6190: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6193: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6196: sipush 496
      // 6199: sipush 496
      // 619c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 619f: ldc_w 168.0
      // 61a2: ldc_w -101.0
      // 61a5: ldc_w -15.0
      // 61a8: ldc_w 16.0
      // 61ab: ldc_w 16.0
      // 61ae: ldc_w 16.0
      // 61b1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 61b4: dup
      // 61b5: fconst_0
      // 61b6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 61b9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 61bc: sipush 496
      // 61bf: sipush 432
      // 61c2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 61c5: ldc_w 168.0
      // 61c8: ldc_w -101.1
      // 61cb: ldc_w -31.0
      // 61ce: ldc_w 16.0
      // 61d1: ldc_w 0.1
      // 61d4: ldc_w 16.0
      // 61d7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 61da: dup
      // 61db: fconst_0
      // 61dc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 61df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 61e2: sipush 496
      // 61e5: sipush 430
      // 61e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 61eb: ldc_w 152.0
      // 61ee: ldc_w -101.0
      // 61f1: ldc_w -31.0
      // 61f4: ldc_w 16.0
      // 61f7: ldc_w 16.0
      // 61fa: ldc_w 16.0
      // 61fd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6200: dup
      // 6201: fconst_0
      // 6202: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6205: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6208: sipush 496
      // 620b: sipush 348
      // 620e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6211: ldc_w 136.0
      // 6214: ldc_w -101.0
      // 6217: ldc_w -31.0
      // 621a: ldc_w 16.0
      // 621d: ldc_w 16.0
      // 6220: ldc_w 16.0
      // 6223: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6226: dup
      // 6227: fconst_0
      // 6228: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 622b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 622e: sipush 496
      // 6231: sipush 348
      // 6234: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6237: ldc_w 136.0
      // 623a: ldc_w -101.0
      // 623d: ldc_w -15.0
      // 6240: ldc_w 16.0
      // 6243: ldc_w 16.0
      // 6246: ldc_w 16.0
      // 6249: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 624c: dup
      // 624d: fconst_0
      // 624e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6251: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6254: sipush 496
      // 6257: sipush 496
      // 625a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 625d: ldc_w 168.0
      // 6260: ldc_w -69.0
      // 6263: ldc_w -15.0
      // 6266: ldc_w 16.0
      // 6269: ldc_w 16.0
      // 626c: ldc_w 16.0
      // 626f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6272: dup
      // 6273: fconst_0
      // 6274: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6277: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 627a: sipush 496
      // 627d: sipush 496
      // 6280: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6283: ldc_w 136.0
      // 6286: ldc_w -53.0
      // 6289: ldc_w -15.0
      // 628c: ldc_w 16.0
      // 628f: ldc_w 16.0
      // 6292: ldc_w 16.0
      // 6295: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6298: dup
      // 6299: fconst_0
      // 629a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 629d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 62a0: sipush 496
      // 62a3: sipush 496
      // 62a6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 62a9: ldc_w 136.0
      // 62ac: ldc_w -69.0
      // 62af: fconst_1
      // 62b0: ldc_w 16.0
      // 62b3: ldc_w 16.0
      // 62b6: ldc_w 16.0
      // 62b9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 62bc: dup
      // 62bd: fconst_0
      // 62be: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 62c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 62c4: sipush 496
      // 62c7: sipush 496
      // 62ca: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 62cd: ldc_w 136.0
      // 62d0: ldc_w -69.0
      // 62d3: ldc_w 17.0
      // 62d6: ldc_w 16.0
      // 62d9: ldc_w 16.0
      // 62dc: ldc_w 16.0
      // 62df: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 62e2: dup
      // 62e3: fconst_0
      // 62e4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 62e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 62ea: sipush 496
      // 62ed: sipush 496
      // 62f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 62f3: ldc_w 136.0
      // 62f6: ldc_w -85.0
      // 62f9: fconst_1
      // 62fa: ldc_w 16.0
      // 62fd: ldc_w 16.0
      // 6300: ldc_w 16.0
      // 6303: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6306: dup
      // 6307: fconst_0
      // 6308: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 630b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 630e: sipush 496
      // 6311: sipush 496
      // 6314: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6317: ldc_w 152.0
      // 631a: ldc_w -53.0
      // 631d: ldc_w -15.0
      // 6320: ldc_w 16.0
      // 6323: ldc_w 16.0
      // 6326: ldc_w 16.0
      // 6329: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 632c: dup
      // 632d: fconst_0
      // 632e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6331: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6334: sipush 496
      // 6337: sipush 496
      // 633a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 633d: ldc_w 152.0
      // 6340: ldc_w -85.0
      // 6343: ldc_w -15.0
      // 6346: ldc_w 16.0
      // 6349: ldc_w 16.0
      // 634c: ldc_w 16.0
      // 634f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6352: dup
      // 6353: fconst_0
      // 6354: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6357: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 635a: sipush 496
      // 635d: sipush 496
      // 6360: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6363: ldc_w 152.0
      // 6366: ldc_w -69.0
      // 6369: ldc_w -15.0
      // 636c: ldc_w 16.0
      // 636f: ldc_w 16.0
      // 6372: ldc_w 16.0
      // 6375: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6378: dup
      // 6379: fconst_0
      // 637a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 637d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6380: sipush 496
      // 6383: sipush 496
      // 6386: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6389: ldc_w 120.0
      // 638c: ldc_w -53.0
      // 638f: ldc_w -31.0
      // 6392: ldc_w 16.0
      // 6395: ldc_w 16.0
      // 6398: ldc_w 16.0
      // 639b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 639e: dup
      // 639f: fconst_0
      // 63a0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 63a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 63a6: sipush 496
      // 63a9: sipush 496
      // 63ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 63af: ldc_w 120.0
      // 63b2: ldc_w -37.0
      // 63b5: ldc_w -31.0
      // 63b8: ldc_w 16.0
      // 63bb: ldc_w 16.0
      // 63be: ldc_w 16.0
      // 63c1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 63c4: dup
      // 63c5: fconst_0
      // 63c6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 63c9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 63cc: bipush 47
      // 63ce: sipush 496
      // 63d1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 63d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 63d7: ldc_w 88.0
      // 63da: ldc_w -21.0
      // 63dd: ldc_w 17.0
      // 63e0: ldc_w 16.0
      // 63e3: ldc_w 16.0
      // 63e6: ldc_w 16.0
      // 63e9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 63ec: dup
      // 63ed: fconst_0
      // 63ee: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 63f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 63f4: bipush 0
      // 63f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 63f8: bipush 47
      // 63fa: sipush 496
      // 63fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6400: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6403: ldc_w 72.0
      // 6406: ldc_w -5.0
      // 6409: fconst_1
      // 640a: ldc_w 16.0
      // 640d: ldc_w 16.0
      // 6410: ldc_w 16.0
      // 6413: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6416: dup
      // 6417: fconst_0
      // 6418: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 641b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 641e: bipush 0
      // 641f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6422: bipush 47
      // 6424: sipush 496
      // 6427: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 642a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 642d: ldc_w 72.0
      // 6430: ldc_w -5.0
      // 6433: ldc_w 17.0
      // 6436: ldc_w 16.0
      // 6439: ldc_w 16.0
      // 643c: ldc_w 16.0
      // 643f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6442: dup
      // 6443: fconst_0
      // 6444: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6447: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 644a: bipush 0
      // 644b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 644e: bipush 47
      // 6450: sipush 496
      // 6453: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6456: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6459: ldc_w 72.0
      // 645c: ldc_w -21.0
      // 645f: ldc_w 33.0
      // 6462: ldc_w 16.0
      // 6465: ldc_w 16.0
      // 6468: ldc_w 16.0
      // 646b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 646e: dup
      // 646f: fconst_0
      // 6470: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6473: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6476: bipush 0
      // 6477: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 647a: bipush 47
      // 647c: sipush 496
      // 647f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6482: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6485: ldc_w 56.0
      // 6488: ldc_w -21.0
      // 648b: ldc_w 33.0
      // 648e: ldc_w 16.0
      // 6491: ldc_w 16.0
      // 6494: ldc_w 16.0
      // 6497: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 649a: dup
      // 649b: fconst_0
      // 649c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 649f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64a2: bipush 0
      // 64a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64a6: bipush 47
      // 64a8: sipush 496
      // 64ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64ae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64b1: ldc_w 56.0
      // 64b4: ldc_w -37.0
      // 64b7: ldc_w 33.0
      // 64ba: ldc_w 16.0
      // 64bd: ldc_w 16.0
      // 64c0: ldc_w 16.0
      // 64c3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 64c6: dup
      // 64c7: fconst_0
      // 64c8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 64cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64ce: bipush 0
      // 64cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64d2: bipush 47
      // 64d4: sipush 496
      // 64d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64da: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64dd: ldc_w 72.0
      // 64e0: ldc_w -37.0
      // 64e3: ldc_w 33.0
      // 64e6: ldc_w 16.0
      // 64e9: ldc_w 16.0
      // 64ec: ldc_w 16.0
      // 64ef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 64f2: dup
      // 64f3: fconst_0
      // 64f4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 64f7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64fa: bipush 0
      // 64fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 64fe: bipush 47
      // 6500: sipush 496
      // 6503: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6506: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6509: ldc_w 56.0
      // 650c: ldc_w -5.0
      // 650f: ldc_w 17.0
      // 6512: ldc_w 16.0
      // 6515: ldc_w 16.0
      // 6518: ldc_w 16.0
      // 651b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 651e: dup
      // 651f: fconst_0
      // 6520: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6523: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6526: bipush 0
      // 6527: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 652a: bipush 47
      // 652c: sipush 432
      // 652f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6532: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6535: ldc_w 72.0
      // 6538: ldc_w -5.0
      // 653b: ldc_w -31.0
      // 653e: ldc_w 16.0
      // 6541: ldc_w 16.0
      // 6544: ldc_w 16.0
      // 6547: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 654a: dup
      // 654b: fconst_0
      // 654c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 654f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6552: bipush 0
      // 6553: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6556: bipush 47
      // 6558: sipush 432
      // 655b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 655e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6561: ldc_w 72.0
      // 6564: ldc_w -5.0
      // 6567: ldc_w -47.0
      // 656a: ldc_w 16.0
      // 656d: ldc_w 16.0
      // 6570: ldc_w 16.0
      // 6573: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6576: dup
      // 6577: fconst_0
      // 6578: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 657b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 657e: bipush 0
      // 657f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6582: bipush 47
      // 6584: sipush 432
      // 6587: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 658a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 658d: ldc_w 56.0
      // 6590: ldc_w -5.0
      // 6593: ldc_w -47.0
      // 6596: ldc_w 16.0
      // 6599: ldc_w 16.0
      // 659c: ldc_w 16.0
      // 659f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 65a2: dup
      // 65a3: fconst_0
      // 65a4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 65a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 65aa: bipush 0
      // 65ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 65ae: bipush 47
      // 65b0: sipush 432
      // 65b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 65b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 65b9: ldc_w 56.0
      // 65bc: ldc_w -5.0
      // 65bf: ldc_w -31.0
      // 65c2: ldc_w 16.0
      // 65c5: ldc_w 16.0
      // 65c8: ldc_w 16.0
      // 65cb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 65ce: dup
      // 65cf: fconst_0
      // 65d0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 65d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 65d6: bipush 0
      // 65d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 65da: bipush 47
      // 65dc: sipush 432
      // 65df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 65e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 65e5: ldc_w 56.0
      // 65e8: ldc_w -5.0
      // 65eb: ldc_w -15.0
      // 65ee: ldc_w 16.0
      // 65f1: ldc_w 16.0
      // 65f4: ldc_w 16.0
      // 65f7: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 65fa: dup
      // 65fb: fconst_0
      // 65fc: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 65ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6602: bipush 0
      // 6603: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6606: bipush 47
      // 6608: sipush 432
      // 660b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 660e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6611: ldc_w 72.0
      // 6614: ldc_w -5.0
      // 6617: ldc_w -15.0
      // 661a: ldc_w 16.0
      // 661d: ldc_w 16.0
      // 6620: ldc_w 16.0
      // 6623: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6626: dup
      // 6627: fconst_0
      // 6628: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 662b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 662e: bipush 0
      // 662f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6632: bipush 47
      // 6634: sipush 432
      // 6637: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 663a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 663d: ldc_w 56.0
      // 6640: ldc_w -5.0
      // 6643: fconst_1
      // 6644: ldc_w 16.0
      // 6647: ldc_w 16.0
      // 664a: ldc_w 16.0
      // 664d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6650: dup
      // 6651: fconst_0
      // 6652: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6655: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6658: bipush 0
      // 6659: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 665c: bipush 0
      // 665d: sipush 128
      // 6660: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6663: ldc_w 120.0
      // 6666: ldc_w -101.0
      // 6669: ldc_w -47.0
      // 666c: ldc_w 16.0
      // 666f: ldc_w 16.0
      // 6672: ldc_w 16.0
      // 6675: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6678: dup
      // 6679: fconst_0
      // 667a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 667d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6680: bipush 0
      // 6681: sipush 128
      // 6684: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6687: ldc_w 120.0
      // 668a: ldc_w -117.0
      // 668d: ldc_w -47.0
      // 6690: ldc_w 16.0
      // 6693: ldc_w 16.0
      // 6696: ldc_w 16.0
      // 6699: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 669c: dup
      // 669d: fconst_0
      // 669e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 66a1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 66a4: bipush 0
      // 66a5: sipush 128
      // 66a8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 66ab: ldc_w 136.0
      // 66ae: ldc_w -117.0
      // 66b1: ldc_w -47.0
      // 66b4: ldc_w 16.0
      // 66b7: ldc_w 16.0
      // 66ba: ldc_w 16.0
      // 66bd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 66c0: dup
      // 66c1: fconst_0
      // 66c2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 66c5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 66c8: bipush 0
      // 66c9: sipush 128
      // 66cc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 66cf: ldc_w 152.0
      // 66d2: ldc_w -133.0
      // 66d5: ldc_w -63.0
      // 66d8: ldc_w 16.0
      // 66db: ldc_w 16.0
      // 66de: ldc_w 16.0
      // 66e1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 66e4: dup
      // 66e5: fconst_0
      // 66e6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 66e9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 66ec: bipush 0
      // 66ed: sipush 128
      // 66f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 66f3: ldc_w 152.0
      // 66f6: ldc_w -117.0
      // 66f9: ldc_w -47.0
      // 66fc: ldc_w 16.0
      // 66ff: ldc_w 16.0
      // 6702: ldc_w 16.0
      // 6705: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6708: dup
      // 6709: fconst_0
      // 670a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 670d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6710: bipush 0
      // 6711: sipush 128
      // 6714: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6717: ldc_w 120.0
      // 671a: ldc_w -117.0
      // 671d: ldc_w -63.0
      // 6720: ldc_w 16.0
      // 6723: ldc_w 16.0
      // 6726: ldc_w 16.0
      // 6729: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 672c: dup
      // 672d: fconst_0
      // 672e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6731: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6734: bipush 0
      // 6735: sipush 128
      // 6738: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 673b: ldc_w 104.0
      // 673e: ldc_w -101.0
      // 6741: ldc_w -47.0
      // 6744: ldc_w 16.0
      // 6747: ldc_w 16.0
      // 674a: ldc_w 16.0
      // 674d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6750: dup
      // 6751: fconst_0
      // 6752: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6755: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6758: bipush 0
      // 6759: sipush 128
      // 675c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 675f: ldc_w 104.0
      // 6762: ldc_w -101.0
      // 6765: ldc_w -63.0
      // 6768: ldc_w 16.0
      // 676b: ldc_w 16.0
      // 676e: ldc_w 16.0
      // 6771: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6774: dup
      // 6775: fconst_0
      // 6776: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6779: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 677c: bipush 0
      // 677d: sipush 128
      // 6780: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6783: ldc_w 120.0
      // 6786: ldc_w -37.0
      // 6789: ldc_w -79.0
      // 678c: ldc_w 16.0
      // 678f: ldc_w 16.0
      // 6792: ldc_w 16.0
      // 6795: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6798: dup
      // 6799: fconst_0
      // 679a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 679d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 67a0: bipush 0
      // 67a1: sipush 432
      // 67a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 67a7: ldc_w 136.0
      // 67aa: ldc_w -21.0
      // 67ad: ldc_w -47.0
      // 67b0: ldc_w 16.0
      // 67b3: ldc_w 16.0
      // 67b6: ldc_w 16.0
      // 67b9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 67bc: dup
      // 67bd: fconst_0
      // 67be: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 67c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 67c4: bipush 0
      // 67c5: sipush 432
      // 67c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 67cb: ldc_w 120.0
      // 67ce: ldc_w -21.0
      // 67d1: ldc_w -47.0
      // 67d4: ldc_w 16.0
      // 67d7: ldc_w 16.0
      // 67da: ldc_w 16.0
      // 67dd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 67e0: dup
      // 67e1: fconst_0
      // 67e2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 67e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 67e8: bipush 0
      // 67e9: sipush 512
      // 67ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 67ef: ldc_w 104.0
      // 67f2: ldc_w -21.0
      // 67f5: ldc_w -47.0
      // 67f8: ldc_w 16.0
      // 67fb: ldc_w 16.0
      // 67fe: ldc_w 16.0
      // 6801: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6804: dup
      // 6805: fconst_0
      // 6806: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6809: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 680c: bipush 0
      // 680d: sipush 430
      // 6810: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6813: ldc_w 104.0
      // 6816: ldc_w -37.0
      // 6819: ldc_w -15.0
      // 681c: ldc_w 16.0
      // 681f: ldc_w 16.0
      // 6822: ldc_w 16.0
      // 6825: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6828: dup
      // 6829: fconst_0
      // 682a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 682d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6830: sipush 276
      // 6833: sipush 430
      // 6836: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6839: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 683c: ldc_w 88.0
      // 683f: ldc_w -37.0
      // 6842: fconst_1
      // 6843: ldc_w 16.0
      // 6846: ldc_w 16.0
      // 6849: ldc_w 16.0
      // 684c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 684f: dup
      // 6850: fconst_0
      // 6851: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6854: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6857: bipush 0
      // 6858: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 685b: bipush 0
      // 685c: sipush 430
      // 685f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6862: ldc_w 104.0
      // 6865: ldc_w -37.0
      // 6868: fconst_1
      // 6869: ldc_w 16.0
      // 686c: ldc_w 16.0
      // 686f: ldc_w 16.0
      // 6872: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6875: dup
      // 6876: fconst_0
      // 6877: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 687a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 687d: bipush 0
      // 687e: sipush 430
      // 6881: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6884: ldc_w 104.0
      // 6887: ldc_w -37.0
      // 688a: ldc_w -31.0
      // 688d: ldc_w 16.0
      // 6890: ldc_w 16.0
      // 6893: ldc_w 16.0
      // 6896: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6899: dup
      // 689a: fconst_0
      // 689b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 689e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68a1: sipush 286
      // 68a4: sipush 430
      // 68a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68aa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68ad: ldc_w 88.0
      // 68b0: ldc_w -37.0
      // 68b3: ldc_w -31.0
      // 68b6: ldc_w 16.0
      // 68b9: ldc_w 16.0
      // 68bc: ldc_w 16.0
      // 68bf: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 68c2: dup
      // 68c3: fconst_0
      // 68c4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 68c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68ca: bipush 0
      // 68cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68ce: sipush 286
      // 68d1: sipush 512
      // 68d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68da: ldc_w 56.0
      // 68dd: ldc_w -21.0
      // 68e0: ldc_w -63.0
      // 68e3: ldc_w 16.0
      // 68e6: ldc_w 16.0
      // 68e9: ldc_w 16.0
      // 68ec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 68ef: dup
      // 68f0: fconst_0
      // 68f1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 68f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68f7: bipush 0
      // 68f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 68fb: sipush 286
      // 68fe: sipush 512
      // 6901: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6904: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6907: ldc_w 72.0
      // 690a: ldc_w -21.0
      // 690d: ldc_w -63.0
      // 6910: ldc_w 16.0
      // 6913: ldc_w 16.0
      // 6916: ldc_w 16.0
      // 6919: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 691c: dup
      // 691d: fconst_0
      // 691e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6921: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6924: bipush 0
      // 6925: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6928: sipush 286
      // 692b: sipush 512
      // 692e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6931: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6934: ldc_w 88.0
      // 6937: ldc_w -21.0
      // 693a: ldc_w -47.0
      // 693d: ldc_w 16.0
      // 6940: ldc_w 16.0
      // 6943: ldc_w 16.0
      // 6946: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6949: dup
      // 694a: fconst_0
      // 694b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 694e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6951: bipush 0
      // 6952: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6955: sipush 286
      // 6958: sipush 430
      // 695b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 695e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6961: ldc_w 88.0
      // 6964: ldc_w -21.0
      // 6967: ldc_w -31.0
      // 696a: ldc_w 16.0
      // 696d: ldc_w 16.0
      // 6970: ldc_w 16.0
      // 6973: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6976: dup
      // 6977: fconst_0
      // 6978: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 697b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 697e: bipush 0
      // 697f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6982: sipush 286
      // 6985: sipush 430
      // 6988: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 698b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 698e: ldc_w 88.0
      // 6991: ldc_w -37.0
      // 6994: ldc_w -15.0
      // 6997: ldc_w 16.0
      // 699a: ldc_w 16.0
      // 699d: ldc_w 16.0
      // 69a0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 69a3: dup
      // 69a4: fconst_0
      // 69a5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 69a8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 69ab: bipush 0
      // 69ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 69af: sipush 286
      // 69b2: sipush 430
      // 69b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 69b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 69bb: ldc_w 88.0
      // 69be: ldc_w -21.0
      // 69c1: fconst_1
      // 69c2: ldc_w 16.0
      // 69c5: ldc_w 16.0
      // 69c8: ldc_w 16.0
      // 69cb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 69ce: dup
      // 69cf: fconst_0
      // 69d0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 69d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 69d6: bipush 0
      // 69d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 69da: sipush 286
      // 69dd: sipush 430
      // 69e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 69e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 69e6: ldc_w 88.0
      // 69e9: ldc_w -21.0
      // 69ec: ldc_w -15.0
      // 69ef: ldc_w 16.0
      // 69f2: ldc_w 16.0
      // 69f5: ldc_w 16.0
      // 69f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 69fb: dup
      // 69fc: fconst_0
      // 69fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6a00: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a03: bipush 0
      // 6a04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a07: bipush 0
      // 6a08: sipush 432
      // 6a0b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a0e: ldc_w 136.0
      // 6a11: ldc_w -21.0
      // 6a14: ldc_w -63.0
      // 6a17: ldc_w 16.0
      // 6a1a: ldc_w 16.0
      // 6a1d: ldc_w 16.0
      // 6a20: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6a23: dup
      // 6a24: fconst_0
      // 6a25: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6a28: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a2b: bipush 0
      // 6a2c: sipush 432
      // 6a2f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a32: ldc_w 136.0
      // 6a35: ldc_w -21.0
      // 6a38: ldc_w -31.0
      // 6a3b: ldc_w 16.0
      // 6a3e: ldc_w 16.0
      // 6a41: ldc_w 16.0
      // 6a44: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6a47: dup
      // 6a48: fconst_0
      // 6a49: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6a4c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a4f: bipush 0
      // 6a50: sipush 128
      // 6a53: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a56: ldc_w 120.0
      // 6a59: ldc_w -53.0
      // 6a5c: ldc_w -79.0
      // 6a5f: ldc_w 16.0
      // 6a62: ldc_w 16.0
      // 6a65: ldc_w 16.0
      // 6a68: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6a6b: dup
      // 6a6c: fconst_0
      // 6a6d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6a70: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a73: bipush 0
      // 6a74: sipush 128
      // 6a77: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a7a: ldc_w 120.0
      // 6a7d: ldc_w -69.0
      // 6a80: ldc_w -79.0
      // 6a83: ldc_w 16.0
      // 6a86: ldc_w 16.0
      // 6a89: ldc_w 16.0
      // 6a8c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6a8f: dup
      // 6a90: fconst_0
      // 6a91: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6a94: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a97: bipush 0
      // 6a98: sipush 128
      // 6a9b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6a9e: ldc_w 104.0
      // 6aa1: ldc_w -53.0
      // 6aa4: ldc_w -79.0
      // 6aa7: ldc_w 16.0
      // 6aaa: ldc_w 16.0
      // 6aad: ldc_w 16.0
      // 6ab0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6ab3: dup
      // 6ab4: fconst_0
      // 6ab5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6ab8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6abb: bipush 0
      // 6abc: sipush 128
      // 6abf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6ac2: ldc_w 24.0
      // 6ac5: ldc_w -21.0
      // 6ac8: ldc_w -79.0
      // 6acb: ldc_w 16.0
      // 6ace: ldc_w 16.0
      // 6ad1: ldc_w 16.0
      // 6ad4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6ad7: dup
      // 6ad8: fconst_0
      // 6ad9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6adc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6adf: bipush 0
      // 6ae0: sipush 128
      // 6ae3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6ae6: ldc_w 8.0
      // 6ae9: ldc_w -21.0
      // 6aec: ldc_w -63.0
      // 6aef: ldc_w 16.0
      // 6af2: ldc_w 16.0
      // 6af5: ldc_w 16.0
      // 6af8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6afb: dup
      // 6afc: fconst_0
      // 6afd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6b00: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b03: bipush 0
      // 6b04: sipush 128
      // 6b07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b0a: ldc_w 8.0
      // 6b0d: ldc_w -37.0
      // 6b10: ldc_w -95.0
      // 6b13: ldc_w 16.0
      // 6b16: ldc_w 16.0
      // 6b19: ldc_w 16.0
      // 6b1c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6b1f: dup
      // 6b20: fconst_0
      // 6b21: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6b24: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b27: bipush 0
      // 6b28: sipush 128
      // 6b2b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b2e: ldc_w 40.0
      // 6b31: ldc_w 27.0
      // 6b34: ldc_w -95.0
      // 6b37: ldc_w 16.0
      // 6b3a: ldc_w 16.0
      // 6b3d: ldc_w 16.0
      // 6b40: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6b43: dup
      // 6b44: fconst_0
      // 6b45: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6b48: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b4b: bipush 0
      // 6b4c: sipush 128
      // 6b4f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b52: ldc_w 40.0
      // 6b55: ldc_w 27.0
      // 6b58: ldc_w -111.0
      // 6b5b: ldc_w 16.0
      // 6b5e: ldc_w 16.0
      // 6b61: ldc_w 16.0
      // 6b64: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6b67: dup
      // 6b68: fconst_0
      // 6b69: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6b6c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b6f: bipush 0
      // 6b70: sipush 128
      // 6b73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b76: ldc_w 40.0
      // 6b79: ldc_w 11.0
      // 6b7c: ldc_w -111.0
      // 6b7f: ldc_w 16.0
      // 6b82: ldc_w 16.0
      // 6b85: ldc_w 16.0
      // 6b88: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6b8b: dup
      // 6b8c: fconst_0
      // 6b8d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6b90: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b93: bipush 0
      // 6b94: sipush 128
      // 6b97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6b9a: ldc_w 56.0
      // 6b9d: ldc_w 11.0
      // 6ba0: ldc_w -111.0
      // 6ba3: ldc_w 16.0
      // 6ba6: ldc_w 16.0
      // 6ba9: ldc_w 16.0
      // 6bac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6baf: dup
      // 6bb0: fconst_0
      // 6bb1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6bb4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6bb7: bipush 0
      // 6bb8: sipush 128
      // 6bbb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6bbe: ldc_w 56.0
      // 6bc1: ldc_w 27.0
      // 6bc4: ldc_w -111.0
      // 6bc7: ldc_w 16.0
      // 6bca: ldc_w 16.0
      // 6bcd: ldc_w 16.0
      // 6bd0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6bd3: dup
      // 6bd4: fconst_0
      // 6bd5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6bd8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6bdb: bipush 0
      // 6bdc: sipush 128
      // 6bdf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6be2: ldc_w 56.0
      // 6be5: ldc_w 43.0
      // 6be8: ldc_w -111.0
      // 6beb: ldc_w 16.0
      // 6bee: ldc_w 16.0
      // 6bf1: ldc_w 16.0
      // 6bf4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6bf7: dup
      // 6bf8: fconst_0
      // 6bf9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6bfc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6bff: bipush 0
      // 6c00: sipush 128
      // 6c03: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c06: ldc_w 72.0
      // 6c09: ldc_w 43.0
      // 6c0c: ldc_w -111.0
      // 6c0f: ldc_w 16.0
      // 6c12: ldc_w 16.0
      // 6c15: ldc_w 16.0
      // 6c18: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6c1b: dup
      // 6c1c: fconst_0
      // 6c1d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6c20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c23: bipush 0
      // 6c24: sipush 128
      // 6c27: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c2a: ldc_w 88.0
      // 6c2d: ldc_w 59.0
      // 6c30: ldc_w -127.0
      // 6c33: ldc_w 16.0
      // 6c36: ldc_w 16.0
      // 6c39: ldc_w 16.0
      // 6c3c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6c3f: dup
      // 6c40: fconst_0
      // 6c41: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6c44: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c47: bipush 0
      // 6c48: sipush 128
      // 6c4b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c4e: ldc_w 88.0
      // 6c51: ldc_w 75.0
      // 6c54: ldc_w -127.0
      // 6c57: ldc_w 16.0
      // 6c5a: ldc_w 16.0
      // 6c5d: ldc_w 16.0
      // 6c60: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6c63: dup
      // 6c64: fconst_0
      // 6c65: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6c68: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c6b: bipush 0
      // 6c6c: sipush 128
      // 6c6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c72: ldc_w 88.0
      // 6c75: ldc_w 75.0
      // 6c78: ldc_w -111.0
      // 6c7b: ldc_w 16.0
      // 6c7e: ldc_w 16.0
      // 6c81: ldc_w 16.0
      // 6c84: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6c87: dup
      // 6c88: fconst_0
      // 6c89: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6c8c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c8f: bipush 0
      // 6c90: sipush 128
      // 6c93: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6c96: ldc_w 88.0
      // 6c99: ldc_w 59.0
      // 6c9c: ldc_w -111.0
      // 6c9f: ldc_w 16.0
      // 6ca2: ldc_w 16.0
      // 6ca5: ldc_w 16.0
      // 6ca8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6cab: dup
      // 6cac: fconst_0
      // 6cad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6cb0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6cb3: bipush 0
      // 6cb4: sipush 128
      // 6cb7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6cba: ldc_w 104.0
      // 6cbd: ldc_w 59.0
      // 6cc0: ldc_w -127.0
      // 6cc3: ldc_w 16.0
      // 6cc6: ldc_w 16.0
      // 6cc9: ldc_w 16.0
      // 6ccc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6ccf: dup
      // 6cd0: fconst_0
      // 6cd1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6cd4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6cd7: bipush 0
      // 6cd8: sipush 128
      // 6cdb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6cde: ldc_w 104.0
      // 6ce1: ldc_w 75.0
      // 6ce4: ldc_w -127.0
      // 6ce7: ldc_w 16.0
      // 6cea: ldc_w 16.0
      // 6ced: ldc_w 16.0
      // 6cf0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6cf3: dup
      // 6cf4: fconst_0
      // 6cf5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6cf8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6cfb: bipush 0
      // 6cfc: sipush 128
      // 6cff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d02: ldc_w 104.0
      // 6d05: ldc_w 75.0
      // 6d08: ldc_w -111.0
      // 6d0b: ldc_w 16.0
      // 6d0e: ldc_w 16.0
      // 6d11: ldc_w 16.0
      // 6d14: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6d17: dup
      // 6d18: fconst_0
      // 6d19: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6d1c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d1f: bipush 0
      // 6d20: sipush 128
      // 6d23: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d26: ldc_w 104.0
      // 6d29: ldc_w 59.0
      // 6d2c: ldc_w -79.0
      // 6d2f: ldc_w 16.0
      // 6d32: ldc_w 16.0
      // 6d35: ldc_w 16.0
      // 6d38: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6d3b: dup
      // 6d3c: fconst_0
      // 6d3d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6d40: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d43: bipush 0
      // 6d44: sipush 128
      // 6d47: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d4a: ldc_w 120.0
      // 6d4d: ldc_w 59.0
      // 6d50: ldc_w -79.0
      // 6d53: ldc_w 16.0
      // 6d56: ldc_w 16.0
      // 6d59: ldc_w 16.0
      // 6d5c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6d5f: dup
      // 6d60: fconst_0
      // 6d61: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6d64: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d67: bipush 0
      // 6d68: sipush 128
      // 6d6b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d6e: ldc_w 88.0
      // 6d71: ldc_w 59.0
      // 6d74: ldc_w -79.0
      // 6d77: ldc_w 16.0
      // 6d7a: ldc_w 16.0
      // 6d7d: ldc_w 16.0
      // 6d80: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6d83: dup
      // 6d84: fconst_0
      // 6d85: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6d88: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d8b: bipush 0
      // 6d8c: sipush 128
      // 6d8f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6d92: ldc_w 72.0
      // 6d95: ldc_w 59.0
      // 6d98: ldc_w -95.0
      // 6d9b: ldc_w 16.0
      // 6d9e: ldc_w 16.0
      // 6da1: ldc_w 16.0
      // 6da4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6da7: dup
      // 6da8: fconst_0
      // 6da9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6dac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6daf: bipush 0
      // 6db0: sipush 128
      // 6db3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6db6: ldc_w 104.0
      // 6db9: ldc_w 75.0
      // 6dbc: ldc_w -95.0
      // 6dbf: ldc_w 16.0
      // 6dc2: ldc_w 16.0
      // 6dc5: ldc_w 16.0
      // 6dc8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6dcb: dup
      // 6dcc: fconst_0
      // 6dcd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6dd0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6dd3: bipush 0
      // 6dd4: sipush 128
      // 6dd7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6dda: ldc_w 88.0
      // 6ddd: ldc_w 75.0
      // 6de0: ldc_w -95.0
      // 6de3: ldc_w 16.0
      // 6de6: ldc_w 16.0
      // 6de9: ldc_w 16.0
      // 6dec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6def: dup
      // 6df0: fconst_0
      // 6df1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6df4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6df7: bipush 0
      // 6df8: sipush 128
      // 6dfb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6dfe: ldc_w 120.0
      // 6e01: ldc_w 59.0
      // 6e04: ldc_w -1.0
      // 6e07: ldc_w 16.0
      // 6e0a: ldc_w 16.0
      // 6e0d: ldc_w 16.0
      // 6e10: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6e13: dup
      // 6e14: fconst_0
      // 6e15: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6e18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6e1b: bipush 0
      // 6e1c: sipush 128
      // 6e1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6e22: ldc_w 88.0
      // 6e25: ldc_w 43.0
      // 6e28: ldc_w -127.0
      // 6e2b: ldc_w 16.0
      // 6e2e: ldc_w 16.0
      // 6e31: ldc_w 16.0
      // 6e34: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6e37: dup
      // 6e38: fconst_0
      // 6e39: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6e3c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6e3f: bipush 0
      // 6e40: sipush 128
      // 6e43: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6e46: ldc_w 104.0
      // 6e49: ldc_w 43.0
      // 6e4c: ldc_w -127.0
      // 6e4f: ldc_w 16.0
      // 6e52: ldc_w 16.0
      // 6e55: ldc_w 16.0
      // 6e58: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6e5b: dup
      // 6e5c: fconst_0
      // 6e5d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6e60: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6e63: bipush 0
      // 6e64: sipush 128
      // 6e67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6e6a: ldc_w 88.0
      // 6e6d: ldc_w 27.0
      // 6e70: ldc_w -127.0
      // 6e73: ldc_w 16.0
      // 6e76: ldc_w 16.0
      // 6e79: ldc_w 16.0
      // 6e7c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6e7f: dup
      // 6e80: fconst_0
      // 6e81: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6e84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6e87: bipush 0
      // 6e88: sipush 128
      // 6e8b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6e8e: ldc_w 104.0
      // 6e91: ldc_w 27.0
      // 6e94: ldc_w -127.0
      // 6e97: ldc_w 16.0
      // 6e9a: ldc_w 16.0
      // 6e9d: ldc_w 16.0
      // 6ea0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6ea3: dup
      // 6ea4: fconst_0
      // 6ea5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6ea8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6eab: bipush 0
      // 6eac: sipush 128
      // 6eaf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6eb2: ldc_w 120.0
      // 6eb5: ldc_w 27.0
      // 6eb8: ldc_w -127.0
      // 6ebb: ldc_w 16.0
      // 6ebe: ldc_w 16.0
      // 6ec1: ldc_w 16.0
      // 6ec4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6ec7: dup
      // 6ec8: fconst_0
      // 6ec9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6ecc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6ecf: bipush 0
      // 6ed0: sipush 128
      // 6ed3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6ed6: ldc_w 120.0
      // 6ed9: ldc_w 43.0
      // 6edc: ldc_w -127.0
      // 6edf: ldc_w 16.0
      // 6ee2: ldc_w 16.0
      // 6ee5: ldc_w 16.0
      // 6ee8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6eeb: dup
      // 6eec: fconst_0
      // 6eed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6ef0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6ef3: bipush 0
      // 6ef4: sipush 128
      // 6ef7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6efa: ldc_w 120.0
      // 6efd: ldc_w 59.0
      // 6f00: ldc_w -127.0
      // 6f03: ldc_w 16.0
      // 6f06: ldc_w 16.0
      // 6f09: ldc_w 16.0
      // 6f0c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6f0f: dup
      // 6f10: fconst_0
      // 6f11: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6f14: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6f17: bipush 0
      // 6f18: sipush 128
      // 6f1b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6f1e: ldc_w 120.0
      // 6f21: ldc_w 75.0
      // 6f24: ldc_w -127.0
      // 6f27: ldc_w 16.0
      // 6f2a: ldc_w 16.0
      // 6f2d: ldc_w 16.0
      // 6f30: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6f33: dup
      // 6f34: fconst_0
      // 6f35: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6f38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6f3b: bipush 0
      // 6f3c: sipush 128
      // 6f3f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6f42: ldc_w 120.0
      // 6f45: ldc_w 75.0
      // 6f48: ldc_w -111.0
      // 6f4b: ldc_w 16.0
      // 6f4e: ldc_w 16.0
      // 6f51: ldc_w 16.0
      // 6f54: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6f57: dup
      // 6f58: fconst_0
      // 6f59: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6f5c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6f5f: bipush 0
      // 6f60: sipush 128
      // 6f63: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6f66: ldc_w 136.0
      // 6f69: ldc_w 59.0
      // 6f6c: ldc_w -111.0
      // 6f6f: ldc_w 16.0
      // 6f72: ldc_w 16.0
      // 6f75: ldc_w 16.0
      // 6f78: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6f7b: dup
      // 6f7c: fconst_0
      // 6f7d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6f80: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6f83: bipush 0
      // 6f84: sipush 128
      // 6f87: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6f8a: ldc_w 136.0
      // 6f8d: ldc_w 43.0
      // 6f90: ldc_w -111.0
      // 6f93: ldc_w 16.0
      // 6f96: ldc_w 16.0
      // 6f99: ldc_w 16.0
      // 6f9c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6f9f: dup
      // 6fa0: fconst_0
      // 6fa1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6fa4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6fa7: bipush 0
      // 6fa8: sipush 128
      // 6fab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6fae: ldc_w 136.0
      // 6fb1: ldc_w 59.0
      // 6fb4: ldc_w -95.0
      // 6fb7: ldc_w 16.0
      // 6fba: ldc_w 16.0
      // 6fbd: ldc_w 16.0
      // 6fc0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6fc3: dup
      // 6fc4: fconst_0
      // 6fc5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6fc8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6fcb: bipush 0
      // 6fcc: sipush 128
      // 6fcf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6fd2: ldc_w 136.0
      // 6fd5: ldc_w 43.0
      // 6fd8: ldc_w -95.0
      // 6fdb: ldc_w 16.0
      // 6fde: ldc_w 16.0
      // 6fe1: ldc_w 16.0
      // 6fe4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 6fe7: dup
      // 6fe8: fconst_0
      // 6fe9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 6fec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6fef: bipush 0
      // 6ff0: sipush 128
      // 6ff3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 6ff6: ldc_w 136.0
      // 6ff9: ldc_w 43.0
      // 6ffc: ldc_w -79.0
      // 6fff: ldc_w 16.0
      // 7002: ldc_w 16.0
      // 7005: ldc_w 16.0
      // 7008: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 700b: dup
      // 700c: fconst_0
      // 700d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7010: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7013: bipush 0
      // 7014: sipush 128
      // 7017: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 701a: ldc_w 136.0
      // 701d: ldc_w 27.0
      // 7020: ldc_w -79.0
      // 7023: ldc_w 16.0
      // 7026: ldc_w 16.0
      // 7029: ldc_w 16.0
      // 702c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 702f: dup
      // 7030: fconst_0
      // 7031: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7034: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7037: bipush 0
      // 7038: sipush 128
      // 703b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 703e: ldc_w 120.0
      // 7041: ldc_w 75.0
      // 7044: ldc_w -95.0
      // 7047: ldc_w 16.0
      // 704a: ldc_w 16.0
      // 704d: ldc_w 16.0
      // 7050: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7053: dup
      // 7054: fconst_0
      // 7055: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7058: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 705b: bipush 0
      // 705c: sipush 128
      // 705f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7062: ldc_w 72.0
      // 7065: ldc_w 27.0
      // 7068: ldc_w -111.0
      // 706b: ldc_w 16.0
      // 706e: ldc_w 16.0
      // 7071: ldc_w 16.0
      // 7074: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7077: dup
      // 7078: fconst_0
      // 7079: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 707c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 707f: bipush 0
      // 7080: sipush 128
      // 7083: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7086: ldc_w 72.0
      // 7089: ldc_w 11.0
      // 708c: ldc_w -111.0
      // 708f: ldc_w 16.0
      // 7092: ldc_w 16.0
      // 7095: ldc_w 16.0
      // 7098: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 709b: dup
      // 709c: fconst_0
      // 709d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 70a0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 70a3: bipush 0
      // 70a4: sipush 128
      // 70a7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 70aa: ldc_w 88.0
      // 70ad: ldc_w 11.0
      // 70b0: ldc_w -111.0
      // 70b3: ldc_w 16.0
      // 70b6: ldc_w 16.0
      // 70b9: ldc_w 16.0
      // 70bc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 70bf: dup
      // 70c0: fconst_0
      // 70c1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 70c4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 70c7: bipush 0
      // 70c8: sipush 128
      // 70cb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 70ce: ldc_w 104.0
      // 70d1: ldc_w 11.0
      // 70d4: ldc_w -111.0
      // 70d7: ldc_w 16.0
      // 70da: ldc_w 16.0
      // 70dd: ldc_w 16.0
      // 70e0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 70e3: dup
      // 70e4: fconst_0
      // 70e5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 70e8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 70eb: bipush 0
      // 70ec: sipush 128
      // 70ef: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 70f2: ldc_w 120.0
      // 70f5: ldc_w 11.0
      // 70f8: ldc_w -111.0
      // 70fb: ldc_w 16.0
      // 70fe: ldc_w 16.0
      // 7101: ldc_w 16.0
      // 7104: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7107: dup
      // 7108: fconst_0
      // 7109: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 710c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 710f: bipush 0
      // 7110: sipush 128
      // 7113: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7116: ldc_w 136.0
      // 7119: ldc_w 11.0
      // 711c: ldc_w -111.0
      // 711f: ldc_w 16.0
      // 7122: ldc_w 16.0
      // 7125: ldc_w 16.0
      // 7128: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 712b: dup
      // 712c: fconst_0
      // 712d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7130: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7133: bipush 0
      // 7134: sipush 128
      // 7137: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 713a: ldc_w 136.0
      // 713d: ldc_w 27.0
      // 7140: ldc_w -111.0
      // 7143: ldc_w 16.0
      // 7146: ldc_w 16.0
      // 7149: ldc_w 16.0
      // 714c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 714f: dup
      // 7150: fconst_0
      // 7151: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7154: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7157: bipush 0
      // 7158: sipush 128
      // 715b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 715e: ldc_w 56.0
      // 7161: ldc_w 43.0
      // 7164: ldc_w -95.0
      // 7167: ldc_w 16.0
      // 716a: ldc_w 16.0
      // 716d: ldc_w 16.0
      // 7170: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7173: dup
      // 7174: fconst_0
      // 7175: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7178: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 717b: bipush 0
      // 717c: sipush 128
      // 717f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7182: ldc_w 56.0
      // 7185: ldc_w 43.0
      // 7188: ldc_w -79.0
      // 718b: ldc_w 16.0
      // 718e: ldc_w 16.0
      // 7191: ldc_w 16.0
      // 7194: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7197: dup
      // 7198: fconst_0
      // 7199: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 719c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 719f: bipush 0
      // 71a0: sipush 128
      // 71a3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 71a6: ldc_w 56.0
      // 71a9: ldc_w 27.0
      // 71ac: ldc_w -79.0
      // 71af: ldc_w 16.0
      // 71b2: ldc_w 16.0
      // 71b5: ldc_w 16.0
      // 71b8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 71bb: dup
      // 71bc: fconst_0
      // 71bd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 71c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 71c3: bipush 0
      // 71c4: sipush 128
      // 71c7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 71ca: ldc_w 56.0
      // 71cd: ldc_w -5.0
      // 71d0: ldc_w -111.0
      // 71d3: ldc_w 16.0
      // 71d6: ldc_w 16.0
      // 71d9: ldc_w 16.0
      // 71dc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 71df: dup
      // 71e0: fconst_0
      // 71e1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 71e4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 71e7: bipush 0
      // 71e8: sipush 128
      // 71eb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 71ee: ldc_w 72.0
      // 71f1: ldc_w -5.0
      // 71f4: ldc_w -111.0
      // 71f7: ldc_w 16.0
      // 71fa: ldc_w 16.0
      // 71fd: ldc_w 16.0
      // 7200: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7203: dup
      // 7204: fconst_0
      // 7205: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7208: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 720b: bipush 0
      // 720c: sipush 128
      // 720f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7212: ldc_w 88.0
      // 7215: ldc_w -5.0
      // 7218: ldc_w -111.0
      // 721b: ldc_w 16.0
      // 721e: ldc_w 16.0
      // 7221: ldc_w 16.0
      // 7224: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7227: dup
      // 7228: fconst_0
      // 7229: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 722c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 722f: bipush 0
      // 7230: sipush 128
      // 7233: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7236: ldc_w 104.0
      // 7239: ldc_w -5.0
      // 723c: ldc_w -111.0
      // 723f: ldc_w 16.0
      // 7242: ldc_w 16.0
      // 7245: ldc_w 16.0
      // 7248: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 724b: dup
      // 724c: fconst_0
      // 724d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7250: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7253: bipush 0
      // 7254: sipush 128
      // 7257: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 725a: ldc_w 40.0
      // 725d: ldc_w -5.0
      // 7260: ldc_w -95.0
      // 7263: ldc_w 16.0
      // 7266: ldc_w 16.0
      // 7269: ldc_w 16.0
      // 726c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 726f: dup
      // 7270: fconst_0
      // 7271: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7274: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7277: bipush 0
      // 7278: sipush 128
      // 727b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 727e: ldc_w 40.0
      // 7281: ldc_w -21.0
      // 7284: ldc_w -95.0
      // 7287: ldc_w 16.0
      // 728a: ldc_w 16.0
      // 728d: ldc_w 16.0
      // 7290: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7293: dup
      // 7294: fconst_0
      // 7295: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7298: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 729b: bipush 0
      // 729c: sipush 128
      // 729f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 72a2: ldc_w 56.0
      // 72a5: ldc_w -21.0
      // 72a8: ldc_w -95.0
      // 72ab: ldc_w 16.0
      // 72ae: ldc_w 16.0
      // 72b1: ldc_w 16.0
      // 72b4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 72b7: dup
      // 72b8: fconst_0
      // 72b9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 72bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 72bf: bipush 0
      // 72c0: sipush 128
      // 72c3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 72c6: ldc_w 72.0
      // 72c9: ldc_w -21.0
      // 72cc: ldc_w -95.0
      // 72cf: ldc_w 16.0
      // 72d2: ldc_w 16.0
      // 72d5: ldc_w 16.0
      // 72d8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 72db: dup
      // 72dc: fconst_0
      // 72dd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 72e0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 72e3: bipush 0
      // 72e4: sipush 128
      // 72e7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 72ea: ldc_w 88.0
      // 72ed: ldc_w -21.0
      // 72f0: ldc_w -95.0
      // 72f3: ldc_w 16.0
      // 72f6: ldc_w 16.0
      // 72f9: ldc_w 16.0
      // 72fc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 72ff: dup
      // 7300: fconst_0
      // 7301: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7304: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7307: bipush 0
      // 7308: sipush 128
      // 730b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 730e: ldc_w 104.0
      // 7311: ldc_w -21.0
      // 7314: ldc_w -95.0
      // 7317: ldc_w 16.0
      // 731a: ldc_w 16.0
      // 731d: ldc_w 16.0
      // 7320: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7323: dup
      // 7324: fconst_0
      // 7325: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7328: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 732b: bipush 0
      // 732c: sipush 128
      // 732f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7332: ldc_w 120.0
      // 7335: ldc_w -5.0
      // 7338: ldc_w -95.0
      // 733b: ldc_w 16.0
      // 733e: ldc_w 16.0
      // 7341: ldc_w 16.0
      // 7344: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7347: dup
      // 7348: fconst_0
      // 7349: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 734c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 734f: bipush 0
      // 7350: sipush 128
      // 7353: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7356: ldc_w 136.0
      // 7359: ldc_w -5.0
      // 735c: ldc_w -95.0
      // 735f: ldc_w 16.0
      // 7362: ldc_w 16.0
      // 7365: ldc_w 16.0
      // 7368: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 736b: dup
      // 736c: fconst_0
      // 736d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7370: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7373: bipush 0
      // 7374: sipush 128
      // 7377: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 737a: ldc_w 152.0
      // 737d: ldc_w -5.0
      // 7380: ldc_w -95.0
      // 7383: ldc_w 16.0
      // 7386: ldc_w 16.0
      // 7389: ldc_w 16.0
      // 738c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 738f: dup
      // 7390: fconst_0
      // 7391: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7394: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7397: bipush 0
      // 7398: sipush 128
      // 739b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 739e: ldc_w 152.0
      // 73a1: ldc_w 11.0
      // 73a4: ldc_w -95.0
      // 73a7: ldc_w 16.0
      // 73aa: ldc_w 16.0
      // 73ad: ldc_w 16.0
      // 73b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 73b3: dup
      // 73b4: fconst_0
      // 73b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 73b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 73bb: bipush 0
      // 73bc: sipush 128
      // 73bf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 73c2: ldc_w 168.0
      // 73c5: ldc_w 11.0
      // 73c8: ldc_w -95.0
      // 73cb: ldc_w 16.0
      // 73ce: ldc_w 16.0
      // 73d1: ldc_w 16.0
      // 73d4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 73d7: dup
      // 73d8: fconst_0
      // 73d9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 73dc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 73df: bipush 0
      // 73e0: sipush 128
      // 73e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 73e6: ldc_w 184.0
      // 73e9: ldc_w 27.0
      // 73ec: ldc_w -95.0
      // 73ef: ldc_w 16.0
      // 73f2: ldc_w 16.0
      // 73f5: ldc_w 16.0
      // 73f8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 73fb: dup
      // 73fc: fconst_0
      // 73fd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7400: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7403: bipush 0
      // 7404: sipush 128
      // 7407: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 740a: ldc_w 168.0
      // 740d: ldc_w 27.0
      // 7410: ldc_w -95.0
      // 7413: ldc_w 16.0
      // 7416: ldc_w 16.0
      // 7419: ldc_w 16.0
      // 741c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 741f: dup
      // 7420: fconst_0
      // 7421: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7424: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7427: bipush 0
      // 7428: sipush 128
      // 742b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 742e: ldc_w 152.0
      // 7431: ldc_w 27.0
      // 7434: ldc_w -95.0
      // 7437: ldc_w 16.0
      // 743a: ldc_w 16.0
      // 743d: ldc_w 16.0
      // 7440: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7443: dup
      // 7444: fconst_0
      // 7445: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7448: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 744b: bipush 0
      // 744c: sipush 128
      // 744f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7452: ldc_w 152.0
      // 7455: ldc_w 27.0
      // 7458: ldc_w -111.0
      // 745b: ldc_w 16.0
      // 745e: ldc_w 16.0
      // 7461: ldc_w 16.0
      // 7464: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7467: dup
      // 7468: fconst_0
      // 7469: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 746c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 746f: bipush 0
      // 7470: sipush 128
      // 7473: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7476: ldc_w 184.0
      // 7479: ldc_w 11.0
      // 747c: ldc_w -79.0
      // 747f: ldc_w 16.0
      // 7482: ldc_w 16.0
      // 7485: ldc_w 16.0
      // 7488: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 748b: dup
      // 748c: fconst_0
      // 748d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7490: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7493: bipush 0
      // 7494: sipush 128
      // 7497: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 749a: ldc_w 168.0
      // 749d: ldc_w 11.0
      // 74a0: ldc_w -79.0
      // 74a3: ldc_w 16.0
      // 74a6: ldc_w 16.0
      // 74a9: ldc_w 16.0
      // 74ac: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 74af: dup
      // 74b0: fconst_0
      // 74b1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 74b4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 74b7: bipush 0
      // 74b8: sipush 128
      // 74bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 74be: ldc_w 152.0
      // 74c1: ldc_w 11.0
      // 74c4: ldc_w -79.0
      // 74c7: ldc_w 16.0
      // 74ca: ldc_w 16.0
      // 74cd: ldc_w 16.0
      // 74d0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 74d3: dup
      // 74d4: fconst_0
      // 74d5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 74d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 74db: bipush 0
      // 74dc: sipush 128
      // 74df: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 74e2: ldc_w 200.0
      // 74e5: ldc_w 11.0
      // 74e8: ldc_w -79.0
      // 74eb: ldc_w 16.0
      // 74ee: ldc_w 16.0
      // 74f1: ldc_w 16.0
      // 74f4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 74f7: dup
      // 74f8: fconst_0
      // 74f9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 74fc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 74ff: bipush 0
      // 7500: sipush 128
      // 7503: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7506: ldc_w 216.0
      // 7509: ldc_w 11.0
      // 750c: ldc_w -79.0
      // 750f: ldc_w 16.0
      // 7512: ldc_w 16.0
      // 7515: ldc_w 16.0
      // 7518: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 751b: dup
      // 751c: fconst_0
      // 751d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7520: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7523: bipush 0
      // 7524: sipush 128
      // 7527: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 752a: ldc_w 232.0
      // 752d: ldc_w -69.0
      // 7530: ldc_w -31.0
      // 7533: ldc_w 16.0
      // 7536: ldc_w 16.0
      // 7539: ldc_w 16.0
      // 753c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 753f: dup
      // 7540: fconst_0
      // 7541: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7544: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7547: bipush 0
      // 7548: sipush 128
      // 754b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 754e: ldc_w 232.0
      // 7551: ldc_w -69.0
      // 7554: ldc_w -15.0
      // 7557: ldc_w 16.0
      // 755a: ldc_w 16.0
      // 755d: ldc_w 16.0
      // 7560: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7563: dup
      // 7564: fconst_0
      // 7565: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7568: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 756b: bipush 0
      // 756c: sipush 128
      // 756f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7572: ldc_w 184.0
      // 7575: ldc_w -53.0
      // 7578: ldc_w -63.0
      // 757b: ldc_w 16.0
      // 757e: ldc_w 16.0
      // 7581: ldc_w 16.0
      // 7584: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7587: dup
      // 7588: fconst_0
      // 7589: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 758c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 758f: bipush 0
      // 7590: sipush 128
      // 7593: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7596: ldc_w 200.0
      // 7599: ldc_w -37.0
      // 759c: ldc_w -95.0
      // 759f: ldc_w 16.0
      // 75a2: ldc_w 16.0
      // 75a5: ldc_w 16.0
      // 75a8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 75ab: dup
      // 75ac: fconst_0
      // 75ad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 75b0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 75b3: bipush 0
      // 75b4: sipush 128
      // 75b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 75ba: ldc_w 56.0
      // 75bd: ldc_w -37.0
      // 75c0: ldc_w -95.0
      // 75c3: ldc_w 16.0
      // 75c6: ldc_w 16.0
      // 75c9: ldc_w 16.0
      // 75cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 75cf: dup
      // 75d0: fconst_0
      // 75d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 75d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 75d7: bipush 0
      // 75d8: sipush 128
      // 75db: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 75de: ldc_w 72.0
      // 75e1: ldc_w -37.0
      // 75e4: ldc_w -95.0
      // 75e7: ldc_w 16.0
      // 75ea: ldc_w 16.0
      // 75ed: ldc_w 16.0
      // 75f0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 75f3: dup
      // 75f4: fconst_0
      // 75f5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 75f8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 75fb: bipush 0
      // 75fc: sipush 128
      // 75ff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7602: ldc_w 40.0
      // 7605: ldc_w 11.0
      // 7608: ldc_w -79.0
      // 760b: ldc_w 16.0
      // 760e: ldc_w 16.0
      // 7611: ldc_w 16.0
      // 7614: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7617: dup
      // 7618: fconst_0
      // 7619: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 761c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 761f: bipush 0
      // 7620: sipush 128
      // 7623: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7626: ldc_w -24.0
      // 7629: ldc_w -37.0
      // 762c: ldc_w -15.0
      // 762f: ldc_w 16.0
      // 7632: ldc_w 16.0
      // 7635: ldc_w 16.0
      // 7638: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 763b: dup
      // 763c: fconst_0
      // 763d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7640: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7643: bipush 0
      // 7644: sipush 128
      // 7647: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 764a: ldc_w -24.0
      // 764d: ldc_w -21.0
      // 7650: ldc_w -31.0
      // 7653: ldc_w 16.0
      // 7656: ldc_w 16.0
      // 7659: ldc_w 16.0
      // 765c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 765f: dup
      // 7660: fconst_0
      // 7661: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7664: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7667: bipush 0
      // 7668: sipush 128
      // 766b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 766e: ldc_w -24.0
      // 7671: ldc_w -5.0
      // 7674: ldc_w -31.0
      // 7677: ldc_w 16.0
      // 767a: ldc_w 16.0
      // 767d: ldc_w 16.0
      // 7680: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7683: dup
      // 7684: fconst_0
      // 7685: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7688: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 768b: bipush 0
      // 768c: sipush 128
      // 768f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7692: ldc_w -24.0
      // 7695: ldc_w -5.0
      // 7698: ldc_w -47.0
      // 769b: ldc_w 16.0
      // 769e: ldc_w 16.0
      // 76a1: ldc_w 16.0
      // 76a4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 76a7: dup
      // 76a8: fconst_0
      // 76a9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 76ac: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 76af: bipush 0
      // 76b0: sipush 128
      // 76b3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 76b6: ldc_w -8.0
      // 76b9: ldc_w -37.0
      // 76bc: ldc_w -31.0
      // 76bf: ldc_w 16.0
      // 76c2: ldc_w 16.0
      // 76c5: ldc_w 16.0
      // 76c8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 76cb: dup
      // 76cc: fconst_0
      // 76cd: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 76d0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 76d3: bipush 0
      // 76d4: sipush 128
      // 76d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 76da: ldc_w -24.0
      // 76dd: ldc_w 11.0
      // 76e0: ldc_w -31.0
      // 76e3: ldc_w 16.0
      // 76e6: ldc_w 16.0
      // 76e9: ldc_w 16.0
      // 76ec: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 76ef: dup
      // 76f0: fconst_0
      // 76f1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 76f4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 76f7: bipush 0
      // 76f8: sipush 128
      // 76fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 76fe: ldc_w -24.0
      // 7701: ldc_w 11.0
      // 7704: ldc_w -15.0
      // 7707: ldc_w 16.0
      // 770a: ldc_w 16.0
      // 770d: ldc_w 16.0
      // 7710: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7713: dup
      // 7714: fconst_0
      // 7715: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7718: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 771b: bipush 0
      // 771c: sipush 128
      // 771f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7722: ldc_w -24.0
      // 7725: ldc_w 11.0
      // 7728: fconst_1
      // 7729: ldc_w 16.0
      // 772c: ldc_w 16.0
      // 772f: ldc_w 16.0
      // 7732: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7735: dup
      // 7736: fconst_0
      // 7737: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 773a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 773d: bipush 0
      // 773e: sipush 128
      // 7741: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7744: ldc_w -24.0
      // 7747: ldc_w 11.0
      // 774a: ldc_w 17.0
      // 774d: ldc_w 16.0
      // 7750: ldc_w 16.0
      // 7753: ldc_w 16.0
      // 7756: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7759: dup
      // 775a: fconst_0
      // 775b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 775e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7761: bipush 0
      // 7762: sipush 128
      // 7765: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7768: ldc_w -24.0
      // 776b: ldc_w 11.0
      // 776e: ldc_w 33.0
      // 7771: ldc_w 16.0
      // 7774: ldc_w 16.0
      // 7777: ldc_w 16.0
      // 777a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 777d: dup
      // 777e: fconst_0
      // 777f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7782: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7785: bipush 0
      // 7786: sipush 128
      // 7789: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 778c: ldc_w -8.0
      // 778f: ldc_w 11.0
      // 7792: fconst_1
      // 7793: ldc_w 16.0
      // 7796: ldc_w 16.0
      // 7799: ldc_w 16.0
      // 779c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 779f: dup
      // 77a0: fconst_0
      // 77a1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 77a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 77a7: bipush 0
      // 77a8: sipush 128
      // 77ab: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 77ae: ldc_w -8.0
      // 77b1: ldc_w 11.0
      // 77b4: ldc_w 17.0
      // 77b7: ldc_w 16.0
      // 77ba: ldc_w 16.0
      // 77bd: ldc_w 16.0
      // 77c0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 77c3: dup
      // 77c4: fconst_0
      // 77c5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 77c8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 77cb: bipush 0
      // 77cc: sipush 128
      // 77cf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 77d2: ldc_w -8.0
      // 77d5: ldc_w 11.0
      // 77d8: ldc_w 33.0
      // 77db: ldc_w 16.0
      // 77de: ldc_w 16.0
      // 77e1: ldc_w 16.0
      // 77e4: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 77e7: dup
      // 77e8: fconst_0
      // 77e9: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 77ec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 77ef: bipush 0
      // 77f0: sipush 128
      // 77f3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 77f6: ldc_w -24.0
      // 77f9: ldc_w -5.0
      // 77fc: ldc_w -15.0
      // 77ff: ldc_w 16.0
      // 7802: ldc_w 16.0
      // 7805: ldc_w 16.0
      // 7808: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 780b: dup
      // 780c: fconst_0
      // 780d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7810: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7813: bipush 0
      // 7814: sipush 128
      // 7817: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 781a: ldc_w -24.0
      // 781d: ldc_w -5.0
      // 7820: fconst_1
      // 7821: ldc_w 16.0
      // 7824: ldc_w 16.0
      // 7827: ldc_w 16.0
      // 782a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 782d: dup
      // 782e: fconst_0
      // 782f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7832: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7835: bipush 0
      // 7836: sipush 128
      // 7839: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 783c: ldc_w -24.0
      // 783f: ldc_w -5.0
      // 7842: ldc_w 17.0
      // 7845: ldc_w 16.0
      // 7848: ldc_w 16.0
      // 784b: ldc_w 16.0
      // 784e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7851: dup
      // 7852: fconst_0
      // 7853: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7856: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7859: bipush 0
      // 785a: sipush 128
      // 785d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7860: ldc_w -24.0
      // 7863: ldc_w -5.0
      // 7866: ldc_w 49.0
      // 7869: ldc_w 16.0
      // 786c: ldc_w 16.0
      // 786f: ldc_w 16.0
      // 7872: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7875: dup
      // 7876: fconst_0
      // 7877: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 787a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 787d: bipush 0
      // 787e: sipush 128
      // 7881: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7884: ldc_w -8.0
      // 7887: ldc_w -5.0
      // 788a: ldc_w 49.0
      // 788d: ldc_w 16.0
      // 7890: ldc_w 16.0
      // 7893: ldc_w 16.0
      // 7896: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7899: dup
      // 789a: fconst_0
      // 789b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 789e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 78a1: bipush 0
      // 78a2: sipush 128
      // 78a5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 78a8: ldc_w -24.0
      // 78ab: ldc_w -5.0
      // 78ae: ldc_w 65.0
      // 78b1: ldc_w 16.0
      // 78b4: ldc_w 16.0
      // 78b7: ldc_w 16.0
      // 78ba: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 78bd: dup
      // 78be: fconst_0
      // 78bf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 78c2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 78c5: bipush 0
      // 78c6: sipush 128
      // 78c9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 78cc: ldc_w -24.0
      // 78cf: ldc_w -21.0
      // 78d2: ldc_w 65.0
      // 78d5: ldc_w 16.0
      // 78d8: ldc_w 16.0
      // 78db: ldc_w 16.0
      // 78de: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 78e1: dup
      // 78e2: fconst_0
      // 78e3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 78e6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 78e9: bipush 0
      // 78ea: sipush 128
      // 78ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 78f0: ldc_w -24.0
      // 78f3: ldc_w -21.0
      // 78f6: ldc_w 33.0
      // 78f9: ldc_w 16.0
      // 78fc: ldc_w 16.0
      // 78ff: ldc_w 16.0
      // 7902: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7905: dup
      // 7906: fconst_0
      // 7907: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 790a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 790d: bipush 0
      // 790e: sipush 128
      // 7911: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7914: ldc_w -24.0
      // 7917: ldc_w -21.0
      // 791a: ldc_w 17.0
      // 791d: ldc_w 16.0
      // 7920: ldc_w 16.0
      // 7923: ldc_w 16.0
      // 7926: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7929: dup
      // 792a: fconst_0
      // 792b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 792e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7931: bipush 0
      // 7932: sipush 128
      // 7935: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7938: ldc_w -24.0
      // 793b: ldc_w -37.0
      // 793e: ldc_w 17.0
      // 7941: ldc_w 16.0
      // 7944: ldc_w 16.0
      // 7947: ldc_w 16.0
      // 794a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 794d: dup
      // 794e: fconst_0
      // 794f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7952: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7955: bipush 0
      // 7956: sipush 128
      // 7959: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 795c: ldc_w -24.0
      // 795f: ldc_w -37.0
      // 7962: fconst_1
      // 7963: ldc_w 16.0
      // 7966: ldc_w 16.0
      // 7969: ldc_w 16.0
      // 796c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 796f: dup
      // 7970: fconst_0
      // 7971: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7974: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7977: bipush 0
      // 7978: sipush 128
      // 797b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 797e: ldc_w -24.0
      // 7981: ldc_w -21.0
      // 7984: fconst_1
      // 7985: ldc_w 16.0
      // 7988: ldc_w 16.0
      // 798b: ldc_w 16.0
      // 798e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7991: dup
      // 7992: fconst_0
      // 7993: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7996: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7999: bipush 0
      // 799a: sipush 128
      // 799d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 79a0: ldc_w -8.0
      // 79a3: ldc_w -21.0
      // 79a6: ldc_w 33.0
      // 79a9: ldc_w 16.0
      // 79ac: ldc_w 16.0
      // 79af: ldc_w 16.0
      // 79b2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 79b5: dup
      // 79b6: fconst_0
      // 79b7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 79ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 79bd: bipush 0
      // 79be: sipush 128
      // 79c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 79c4: ldc_w -24.0
      // 79c7: ldc_w -37.0
      // 79ca: ldc_w 49.0
      // 79cd: ldc_w 16.0
      // 79d0: ldc_w 16.0
      // 79d3: ldc_w 16.0
      // 79d6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 79d9: dup
      // 79da: fconst_0
      // 79db: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 79de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 79e1: bipush 0
      // 79e2: sipush 128
      // 79e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 79e8: ldc_w -8.0
      // 79eb: ldc_w -37.0
      // 79ee: ldc_w 49.0
      // 79f1: ldc_w 16.0
      // 79f4: ldc_w 16.0
      // 79f7: ldc_w 16.0
      // 79fa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 79fd: dup
      // 79fe: fconst_0
      // 79ff: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7a02: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a05: bipush 0
      // 7a06: sipush 128
      // 7a09: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a0c: ldc_w 8.0
      // 7a0f: ldc_w -21.0
      // 7a12: ldc_w 49.0
      // 7a15: ldc_w 16.0
      // 7a18: ldc_w 16.0
      // 7a1b: ldc_w 16.0
      // 7a1e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7a21: dup
      // 7a22: fconst_0
      // 7a23: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7a26: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a29: bipush 0
      // 7a2a: sipush 128
      // 7a2d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a30: ldc_w 8.0
      // 7a33: ldc_w -37.0
      // 7a36: ldc_w 33.0
      // 7a39: ldc_w 16.0
      // 7a3c: ldc_w 16.0
      // 7a3f: ldc_w 16.0
      // 7a42: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7a45: dup
      // 7a46: fconst_0
      // 7a47: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7a4a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a4d: bipush 0
      // 7a4e: sipush 128
      // 7a51: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a54: ldc_w -8.0
      // 7a57: ldc_w -21.0
      // 7a5a: ldc_w 49.0
      // 7a5d: ldc_w 16.0
      // 7a60: ldc_w 16.0
      // 7a63: ldc_w 16.0
      // 7a66: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7a69: dup
      // 7a6a: fconst_0
      // 7a6b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7a6e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a71: bipush 0
      // 7a72: sipush 128
      // 7a75: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a78: ldc_w 24.0
      // 7a7b: ldc_w -21.0
      // 7a7e: ldc_w 49.0
      // 7a81: ldc_w 16.0
      // 7a84: ldc_w 16.0
      // 7a87: ldc_w 16.0
      // 7a8a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7a8d: dup
      // 7a8e: fconst_0
      // 7a8f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7a92: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a95: bipush 0
      // 7a96: sipush 128
      // 7a99: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7a9c: ldc_w 40.0
      // 7a9f: ldc_w -21.0
      // 7aa2: ldc_w 49.0
      // 7aa5: ldc_w 16.0
      // 7aa8: ldc_w 16.0
      // 7aab: ldc_w 16.0
      // 7aae: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7ab1: dup
      // 7ab2: fconst_0
      // 7ab3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7ab6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ab9: bipush 0
      // 7aba: sipush 128
      // 7abd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ac0: ldc_w 56.0
      // 7ac3: ldc_w -21.0
      // 7ac6: ldc_w 49.0
      // 7ac9: ldc_w 16.0
      // 7acc: ldc_w 16.0
      // 7acf: ldc_w 16.0
      // 7ad2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7ad5: dup
      // 7ad6: fconst_0
      // 7ad7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7ada: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7add: bipush 0
      // 7ade: sipush 128
      // 7ae1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ae4: ldc_w 72.0
      // 7ae7: ldc_w -21.0
      // 7aea: ldc_w 49.0
      // 7aed: ldc_w 16.0
      // 7af0: ldc_w 16.0
      // 7af3: ldc_w 16.0
      // 7af6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7af9: dup
      // 7afa: fconst_0
      // 7afb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7afe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b01: bipush 0
      // 7b02: sipush 128
      // 7b05: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b08: ldc_w 88.0
      // 7b0b: ldc_w -21.0
      // 7b0e: ldc_w 65.0
      // 7b11: ldc_w 16.0
      // 7b14: ldc_w 16.0
      // 7b17: ldc_w 16.0
      // 7b1a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7b1d: dup
      // 7b1e: fconst_0
      // 7b1f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7b22: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b25: bipush 0
      // 7b26: sipush 128
      // 7b29: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b2c: ldc_w -8.0
      // 7b2f: ldc_w -5.0
      // 7b32: ldc_w 65.0
      // 7b35: ldc_w 16.0
      // 7b38: ldc_w 16.0
      // 7b3b: ldc_w 16.0
      // 7b3e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7b41: dup
      // 7b42: fconst_0
      // 7b43: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7b46: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b49: bipush 0
      // 7b4a: sipush 128
      // 7b4d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b50: ldc_w 8.0
      // 7b53: ldc_w -5.0
      // 7b56: ldc_w 65.0
      // 7b59: ldc_w 16.0
      // 7b5c: ldc_w 16.0
      // 7b5f: ldc_w 16.0
      // 7b62: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7b65: dup
      // 7b66: fconst_0
      // 7b67: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7b6a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b6d: bipush 0
      // 7b6e: sipush 128
      // 7b71: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b74: ldc_w 24.0
      // 7b77: ldc_w -5.0
      // 7b7a: ldc_w 65.0
      // 7b7d: ldc_w 16.0
      // 7b80: ldc_w 16.0
      // 7b83: ldc_w 16.0
      // 7b86: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7b89: dup
      // 7b8a: fconst_0
      // 7b8b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7b8e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b91: bipush 0
      // 7b92: sipush 128
      // 7b95: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7b98: ldc_w 40.0
      // 7b9b: ldc_w -5.0
      // 7b9e: ldc_w 65.0
      // 7ba1: ldc_w 16.0
      // 7ba4: ldc_w 16.0
      // 7ba7: ldc_w 16.0
      // 7baa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7bad: dup
      // 7bae: fconst_0
      // 7baf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7bb2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7bb5: bipush 0
      // 7bb6: sipush 128
      // 7bb9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7bbc: ldc_w 56.0
      // 7bbf: ldc_w -5.0
      // 7bc2: ldc_w 65.0
      // 7bc5: ldc_w 16.0
      // 7bc8: ldc_w 16.0
      // 7bcb: ldc_w 16.0
      // 7bce: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7bd1: dup
      // 7bd2: fconst_0
      // 7bd3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7bd6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7bd9: bipush 0
      // 7bda: sipush 128
      // 7bdd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7be0: ldc_w 72.0
      // 7be3: ldc_w -5.0
      // 7be6: ldc_w 65.0
      // 7be9: ldc_w 16.0
      // 7bec: ldc_w 16.0
      // 7bef: ldc_w 16.0
      // 7bf2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7bf5: dup
      // 7bf6: fconst_0
      // 7bf7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7bfa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7bfd: bipush 0
      // 7bfe: sipush 128
      // 7c01: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7c04: ldc_w 24.0
      // 7c07: ldc_w -5.0
      // 7c0a: ldc_w 81.0
      // 7c0d: ldc_w 16.0
      // 7c10: ldc_w 16.0
      // 7c13: ldc_w 16.0
      // 7c16: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7c19: dup
      // 7c1a: fconst_0
      // 7c1b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7c1e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7c21: bipush 0
      // 7c22: sipush 128
      // 7c25: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7c28: ldc_w -24.0
      // 7c2b: ldc_w -5.0
      // 7c2e: ldc_w 33.0
      // 7c31: ldc_w 16.0
      // 7c34: ldc_w 16.0
      // 7c37: ldc_w 16.0
      // 7c3a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7c3d: dup
      // 7c3e: fconst_0
      // 7c3f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7c42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7c45: bipush 0
      // 7c46: sipush 128
      // 7c49: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7c4c: ldc_w -24.0
      // 7c4f: ldc_w -21.0
      // 7c52: ldc_w -15.0
      // 7c55: ldc_w 16.0
      // 7c58: ldc_w 16.0
      // 7c5b: ldc_w 16.0
      // 7c5e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7c61: dup
      // 7c62: fconst_0
      // 7c63: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7c66: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7c69: ldc_w 37.0
      // 7c6c: ldc_w -231.0
      // 7c6f: ldc_w -87.0
      // 7c72: fconst_0
      // 7c73: ldc_w -1.5708
      // 7c76: fconst_0
      // 7c77: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7c7a: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7c7d: astore 2
      // 7c7e: aload 2
      // 7c7f: ldc "Tentacle_7"
      // 7c81: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7c84: bipush 0
      // 7c85: bipush 0
      // 7c86: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7c89: ldc_w -23.0856
      // 7c8c: ldc_w -9.2342
      // 7c8f: ldc_w -13.8513
      // 7c92: ldc_w 27.7027
      // 7c95: ldc_w 18.4684
      // 7c98: ldc_w 18.4684
      // 7c9b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7c9e: dup
      // 7c9f: fconst_0
      // 7ca0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7ca3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ca6: ldc_w 318.8023
      // 7ca9: ldc_w 150.0
      // 7cac: ldc_w -16.0
      // 7caf: ldc_w -1.5708
      // 7cb2: fconst_0
      // 7cb3: ldc_w -1.5708
      // 7cb6: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7cb9: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7cbc: astore 3
      // 7cbd: aload 3
      // 7cbe: ldc "part_01_33"
      // 7cc0: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7cc3: bipush 0
      // 7cc4: bipush 0
      // 7cc5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7cc8: ldc_w -23.0856
      // 7ccb: ldc_w -9.2342
      // 7cce: ldc_w -13.8513
      // 7cd1: ldc_w 27.7027
      // 7cd4: ldc_w 18.4684
      // 7cd7: ldc_w 18.4684
      // 7cda: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7cdd: dup
      // 7cde: fconst_0
      // 7cdf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7ce2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ce5: ldc_w -27.7027
      // 7ce8: fconst_0
      // 7ce9: fconst_0
      // 7cea: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7ced: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7cf0: astore 4
      // 7cf2: aload 4
      // 7cf4: ldc "part_01_34"
      // 7cf6: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7cf9: bipush 0
      // 7cfa: bipush 0
      // 7cfb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7cfe: ldc_w -23.0856
      // 7d01: ldc_w -9.2342
      // 7d04: ldc_w -13.8513
      // 7d07: ldc_w 31.0189
      // 7d0a: ldc_w 18.4684
      // 7d0d: ldc_w 18.4684
      // 7d10: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7d13: dup
      // 7d14: fconst_0
      // 7d15: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7d18: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7d1b: ldc_w -31.0189
      // 7d1e: fconst_0
      // 7d1f: fconst_0
      // 7d20: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7d23: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7d26: astore 5
      // 7d28: aload 5
      // 7d2a: ldc "part_01_35"
      // 7d2c: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7d2f: bipush 0
      // 7d30: bipush 0
      // 7d31: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7d34: ldc_w -23.0856
      // 7d37: ldc_w -9.2342
      // 7d3a: ldc_w -13.8513
      // 7d3d: ldc_w 31.0189
      // 7d40: ldc_w 18.4684
      // 7d43: ldc_w 18.4684
      // 7d46: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7d49: dup
      // 7d4a: fconst_0
      // 7d4b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7d4e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7d51: ldc_w -31.0189
      // 7d54: fconst_0
      // 7d55: fconst_0
      // 7d56: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7d59: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7d5c: astore 6
      // 7d5e: aload 6
      // 7d60: ldc "part_01_36"
      // 7d62: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7d65: bipush 0
      // 7d66: bipush 0
      // 7d67: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7d6a: ldc_w -23.0856
      // 7d6d: ldc_w -9.2342
      // 7d70: ldc_w -13.8513
      // 7d73: ldc_w 27.7027
      // 7d76: ldc_w 18.4684
      // 7d79: ldc_w 18.4684
      // 7d7c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7d7f: dup
      // 7d80: fconst_0
      // 7d81: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7d84: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7d87: ldc_w -27.7027
      // 7d8a: fconst_0
      // 7d8b: fconst_0
      // 7d8c: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7d8f: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7d92: astore 7
      // 7d94: aload 7
      // 7d96: ldc "part_01_37"
      // 7d98: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7d9b: bipush 0
      // 7d9c: bipush 0
      // 7d9d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7da0: ldc_w -23.0856
      // 7da3: ldc_w -9.2342
      // 7da6: ldc_w -4.6171
      // 7da9: ldc_w 31.0189
      // 7dac: ldc_w 18.4684
      // 7daf: ldc_w 9.2342
      // 7db2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7db5: dup
      // 7db6: fconst_0
      // 7db7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7dba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7dbd: bipush 0
      // 7dbe: bipush 0
      // 7dbf: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7dc2: ldc_w -23.0856
      // 7dc5: fconst_0
      // 7dc6: ldc_w -13.8513
      // 7dc9: ldc_w 31.0189
      // 7dcc: ldc_w 9.2342
      // 7dcf: ldc_w 9.2342
      // 7dd2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7dd5: dup
      // 7dd6: fconst_0
      // 7dd7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7dda: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ddd: ldc_w -31.0189
      // 7de0: fconst_0
      // 7de1: fconst_0
      // 7de2: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7de5: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7de8: astore 8
      // 7dea: aload 8
      // 7dec: ldc "part_01_57"
      // 7dee: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7df1: bipush 0
      // 7df2: bipush 0
      // 7df3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7df6: ldc_w -23.0856
      // 7df9: ldc_w -9.2342
      // 7dfc: ldc_w -4.6171
      // 7dff: ldc_w 27.7027
      // 7e02: ldc_w 18.4684
      // 7e05: ldc_w 9.2342
      // 7e08: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7e0b: dup
      // 7e0c: fconst_0
      // 7e0d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7e10: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7e13: bipush 0
      // 7e14: bipush 0
      // 7e15: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7e18: ldc_w -23.0856
      // 7e1b: fconst_0
      // 7e1c: ldc_w -13.8513
      // 7e1f: ldc_w 27.7027
      // 7e22: ldc_w 9.2342
      // 7e25: ldc_w 9.2342
      // 7e28: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7e2b: dup
      // 7e2c: fconst_0
      // 7e2d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7e30: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7e33: ldc_w -27.7027
      // 7e36: fconst_0
      // 7e37: fconst_0
      // 7e38: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7e3b: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7e3e: astore 9
      // 7e40: aload 9
      // 7e42: ldc "part_01_58"
      // 7e44: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7e47: bipush 0
      // 7e48: bipush 0
      // 7e49: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7e4c: ldc_w -26.4018
      // 7e4f: ldc_w -4.6171
      // 7e52: ldc_w -4.6171
      // 7e55: ldc_w 27.7027
      // 7e58: ldc_w 9.2342
      // 7e5b: ldc_w 9.2342
      // 7e5e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7e61: dup
      // 7e62: fconst_0
      // 7e63: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7e66: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7e69: ldc_w -24.3864
      // 7e6c: ldc_w 4.6171
      // 7e6f: fconst_0
      // 7e70: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7e73: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7e76: astore 10
      // 7e78: aload 10
      // 7e7a: ldc "part_01_59"
      // 7e7c: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7e7f: bipush 0
      // 7e80: bipush 0
      // 7e81: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7e84: ldc_w -26.4018
      // 7e87: ldc_w -4.6171
      // 7e8a: ldc_w -4.6171
      // 7e8d: ldc_w 27.7027
      // 7e90: ldc_w 9.2342
      // 7e93: ldc_w 9.2342
      // 7e96: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7e99: dup
      // 7e9a: fconst_0
      // 7e9b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7e9e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ea1: ldc_w -27.7027
      // 7ea4: fconst_0
      // 7ea5: fconst_0
      // 7ea6: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7ea9: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7eac: astore 11
      // 7eae: aload 11
      // 7eb0: ldc "part_01_60"
      // 7eb2: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7eb5: bipush 0
      // 7eb6: bipush 0
      // 7eb7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7eba: ldc_w -26.4018
      // 7ebd: ldc_w -4.6171
      // 7ec0: ldc_w -4.6171
      // 7ec3: ldc_w 27.7027
      // 7ec6: ldc_w 9.2342
      // 7ec9: ldc_w 9.2342
      // 7ecc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7ecf: dup
      // 7ed0: fconst_0
      // 7ed1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7ed4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ed7: ldc_w -27.7027
      // 7eda: fconst_0
      // 7edb: fconst_0
      // 7edc: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7edf: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7ee2: astore 12
      // 7ee4: aload 2
      // 7ee5: ldc "Tentacle_8"
      // 7ee7: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7eea: bipush 0
      // 7eeb: bipush 0
      // 7eec: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7eef: ldc_w -23.0856
      // 7ef2: ldc_w -9.2342
      // 7ef5: ldc_w -13.8513
      // 7ef8: ldc_w 27.7027
      // 7efb: ldc_w 18.4684
      // 7efe: ldc_w 18.4684
      // 7f01: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7f04: dup
      // 7f05: fconst_0
      // 7f06: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7f09: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f0c: bipush 0
      // 7f0d: bipush 0
      // 7f0e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f11: ldc_w -18.0856
      // 7f14: ldc_w -8.7658
      // 7f17: ldc_w -14.1
      // 7f1a: ldc_w 18.0
      // 7f1d: ldc_w 18.0
      // 7f20: ldc_w 0.4684
      // 7f23: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7f26: dup
      // 7f27: fconst_0
      // 7f28: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7f2b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f2e: bipush 0
      // 7f2f: sipush 428
      // 7f32: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f35: ldc_w -18.0856
      // 7f38: ldc_w -9.7658
      // 7f3b: ldc_w -14.1
      // 7f3e: ldc_w 18.0
      // 7f41: fconst_1
      // 7f42: ldc_w 18.4684
      // 7f45: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7f48: dup
      // 7f49: fconst_0
      // 7f4a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7f4d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f50: bipush 0
      // 7f51: bipush 0
      // 7f52: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f55: ldc_w -36.0856
      // 7f58: ldc_w -8.7658
      // 7f5b: ldc_w -14.1
      // 7f5e: ldc_w 18.0
      // 7f61: ldc_w 18.0
      // 7f64: ldc_w 0.4684
      // 7f67: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7f6a: dup
      // 7f6b: fconst_0
      // 7f6c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7f6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f72: ldc_w 251.8023
      // 7f75: ldc_w 55.0
      // 7f78: ldc_w -95.0
      // 7f7b: fconst_0
      // 7f7c: ldc_w -1.5708
      // 7f7f: ldc_w 1.5708
      // 7f82: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7f85: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7f88: astore 13
      // 7f8a: aload 13
      // 7f8c: ldc "part_01_61"
      // 7f8e: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f91: bipush 0
      // 7f92: bipush 0
      // 7f93: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7f96: ldc_w -23.0856
      // 7f99: ldc_w -9.2342
      // 7f9c: ldc_w -13.8513
      // 7f9f: ldc_w 27.7027
      // 7fa2: ldc_w 18.4684
      // 7fa5: ldc_w 18.4684
      // 7fa8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7fab: dup
      // 7fac: fconst_0
      // 7fad: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7fb0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7fb3: ldc_w -27.7027
      // 7fb6: fconst_0
      // 7fb7: fconst_0
      // 7fb8: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7fbb: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7fbe: astore 14
      // 7fc0: aload 14
      // 7fc2: ldc "part_01_62"
      // 7fc4: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7fc7: bipush 0
      // 7fc8: bipush 0
      // 7fc9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7fcc: ldc_w -23.0856
      // 7fcf: ldc_w -9.2342
      // 7fd2: ldc_w -13.8513
      // 7fd5: ldc_w 31.0189
      // 7fd8: ldc_w 18.4684
      // 7fdb: ldc_w 18.4684
      // 7fde: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 7fe1: dup
      // 7fe2: fconst_0
      // 7fe3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 7fe6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7fe9: ldc_w -31.0189
      // 7fec: fconst_0
      // 7fed: fconst_0
      // 7fee: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 7ff1: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 7ff4: astore 15
      // 7ff6: aload 15
      // 7ff8: ldc "part_01_63"
      // 7ffa: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 7ffd: bipush 0
      // 7ffe: bipush 0
      // 7fff: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8002: ldc_w -23.0856
      // 8005: ldc_w -9.2342
      // 8008: ldc_w -13.8513
      // 800b: ldc_w 31.0189
      // 800e: ldc_w 18.4684
      // 8011: ldc_w 18.4684
      // 8014: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8017: dup
      // 8018: fconst_0
      // 8019: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 801c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 801f: ldc_w -31.0189
      // 8022: fconst_0
      // 8023: fconst_0
      // 8024: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8027: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 802a: astore 16
      // 802c: aload 16
      // 802e: ldc "part_01_64"
      // 8030: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8033: bipush 0
      // 8034: bipush 0
      // 8035: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8038: ldc_w -23.0856
      // 803b: ldc_w -9.2342
      // 803e: ldc_w -13.8513
      // 8041: ldc_w 27.7027
      // 8044: ldc_w 18.4684
      // 8047: ldc_w 18.4684
      // 804a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 804d: dup
      // 804e: fconst_0
      // 804f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8052: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8055: ldc_w -27.7027
      // 8058: fconst_0
      // 8059: fconst_0
      // 805a: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 805d: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8060: astore 17
      // 8062: aload 17
      // 8064: ldc "part_01_65"
      // 8066: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8069: bipush 0
      // 806a: bipush 0
      // 806b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 806e: ldc_w -23.0856
      // 8071: ldc_w -9.2342
      // 8074: ldc_w -4.6171
      // 8077: ldc_w 31.0189
      // 807a: ldc_w 18.4684
      // 807d: ldc_w 9.2342
      // 8080: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8083: dup
      // 8084: fconst_0
      // 8085: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8088: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 808b: bipush 0
      // 808c: bipush 0
      // 808d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8090: ldc_w -23.0856
      // 8093: fconst_0
      // 8094: ldc_w -13.8513
      // 8097: ldc_w 31.0189
      // 809a: ldc_w 9.2342
      // 809d: ldc_w 9.2342
      // 80a0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 80a3: dup
      // 80a4: fconst_0
      // 80a5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 80a8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 80ab: ldc_w -31.0189
      // 80ae: fconst_0
      // 80af: fconst_0
      // 80b0: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 80b3: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 80b6: astore 18
      // 80b8: aload 18
      // 80ba: ldc "part_01_66"
      // 80bc: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 80bf: bipush 0
      // 80c0: bipush 0
      // 80c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 80c4: ldc_w -23.0856
      // 80c7: ldc_w -9.2342
      // 80ca: ldc_w -4.6171
      // 80cd: ldc_w 27.7027
      // 80d0: ldc_w 18.4684
      // 80d3: ldc_w 9.2342
      // 80d6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 80d9: dup
      // 80da: fconst_0
      // 80db: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 80de: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 80e1: bipush 0
      // 80e2: bipush 0
      // 80e3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 80e6: ldc_w -23.0856
      // 80e9: fconst_0
      // 80ea: ldc_w -13.8513
      // 80ed: ldc_w 27.7027
      // 80f0: ldc_w 9.2342
      // 80f3: ldc_w 9.2342
      // 80f6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 80f9: dup
      // 80fa: fconst_0
      // 80fb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 80fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8101: ldc_w -27.7027
      // 8104: fconst_0
      // 8105: fconst_0
      // 8106: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8109: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 810c: astore 19
      // 810e: aload 19
      // 8110: ldc "part_01_67"
      // 8112: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8115: bipush 0
      // 8116: bipush 0
      // 8117: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 811a: ldc_w -26.4018
      // 811d: ldc_w -4.6171
      // 8120: ldc_w -4.6171
      // 8123: ldc_w 27.7027
      // 8126: ldc_w 9.2342
      // 8129: ldc_w 9.2342
      // 812c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 812f: dup
      // 8130: fconst_0
      // 8131: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8134: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8137: ldc_w -24.3864
      // 813a: ldc_w 4.6171
      // 813d: fconst_0
      // 813e: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8141: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8144: astore 20
      // 8146: aload 20
      // 8148: ldc "part_01_68"
      // 814a: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 814d: bipush 0
      // 814e: bipush 0
      // 814f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8152: ldc_w -26.4018
      // 8155: ldc_w -4.6171
      // 8158: ldc_w -4.6171
      // 815b: ldc_w 27.7027
      // 815e: ldc_w 9.2342
      // 8161: ldc_w 9.2342
      // 8164: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8167: dup
      // 8168: fconst_0
      // 8169: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 816c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 816f: ldc_w -27.7027
      // 8172: fconst_0
      // 8173: fconst_0
      // 8174: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8177: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 817a: astore 21
      // 817c: aload 21
      // 817e: ldc "part_01_69"
      // 8180: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8183: bipush 0
      // 8184: bipush 0
      // 8185: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8188: ldc_w -26.4018
      // 818b: ldc_w -4.6171
      // 818e: ldc_w -4.6171
      // 8191: ldc_w 27.7027
      // 8194: ldc_w 9.2342
      // 8197: ldc_w 9.2342
      // 819a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 819d: dup
      // 819e: fconst_0
      // 819f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 81a2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 81a5: ldc_w -27.7027
      // 81a8: fconst_0
      // 81a9: fconst_0
      // 81aa: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 81ad: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 81b0: astore 22
      // 81b2: aload 2
      // 81b3: ldc "Tentacle_9"
      // 81b5: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 81b8: bipush 0
      // 81b9: bipush 0
      // 81ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 81bd: ldc_w -23.0856
      // 81c0: ldc_w -9.2342
      // 81c3: ldc_w -13.8513
      // 81c6: ldc_w 27.7027
      // 81c9: ldc_w 18.4684
      // 81cc: ldc_w 18.4684
      // 81cf: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 81d2: dup
      // 81d3: fconst_0
      // 81d4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 81d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 81da: ldc_w 184.8023
      // 81dd: ldc_w 84.0
      // 81e0: ldc_w 21.0
      // 81e3: ldc_w -1.5708
      // 81e6: fconst_0
      // 81e7: ldc_w -1.5708
      // 81ea: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 81ed: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 81f0: astore 23
      // 81f2: aload 23
      // 81f4: ldc "part_01_70"
      // 81f6: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 81f9: bipush 0
      // 81fa: bipush 0
      // 81fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 81fe: ldc_w -23.0856
      // 8201: ldc_w -9.2342
      // 8204: ldc_w -13.8513
      // 8207: ldc_w 27.7027
      // 820a: ldc_w 18.4684
      // 820d: ldc_w 18.4684
      // 8210: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8213: dup
      // 8214: fconst_0
      // 8215: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8218: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 821b: ldc_w -27.7027
      // 821e: fconst_0
      // 821f: fconst_0
      // 8220: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8223: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8226: astore 24
      // 8228: aload 24
      // 822a: ldc "part_01_71"
      // 822c: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 822f: bipush 0
      // 8230: bipush 0
      // 8231: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8234: ldc_w -23.0856
      // 8237: ldc_w -9.2342
      // 823a: ldc_w -13.8513
      // 823d: ldc_w 31.0189
      // 8240: ldc_w 18.4684
      // 8243: ldc_w 18.4684
      // 8246: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8249: dup
      // 824a: fconst_0
      // 824b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 824e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8251: ldc_w -31.0189
      // 8254: fconst_0
      // 8255: fconst_0
      // 8256: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8259: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 825c: astore 25
      // 825e: aload 25
      // 8260: ldc "part_01_81"
      // 8262: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8265: bipush 0
      // 8266: bipush 0
      // 8267: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 826a: ldc_w -23.0856
      // 826d: ldc_w -9.2342
      // 8270: ldc_w -13.8513
      // 8273: ldc_w 31.0189
      // 8276: ldc_w 18.4684
      // 8279: ldc_w 18.4684
      // 827c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 827f: dup
      // 8280: fconst_0
      // 8281: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8284: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8287: ldc_w -31.0189
      // 828a: fconst_0
      // 828b: fconst_0
      // 828c: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 828f: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8292: astore 26
      // 8294: aload 26
      // 8296: ldc "part_01_82"
      // 8298: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 829b: bipush 0
      // 829c: bipush 0
      // 829d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 82a0: ldc_w -23.0856
      // 82a3: ldc_w -9.2342
      // 82a6: ldc_w -13.8513
      // 82a9: ldc_w 27.7027
      // 82ac: ldc_w 18.4684
      // 82af: ldc_w 18.4684
      // 82b2: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 82b5: dup
      // 82b6: fconst_0
      // 82b7: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 82ba: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 82bd: ldc_w -27.7027
      // 82c0: fconst_0
      // 82c1: fconst_0
      // 82c2: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 82c5: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 82c8: astore 27
      // 82ca: aload 27
      // 82cc: ldc "part_01_83"
      // 82ce: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 82d1: bipush 0
      // 82d2: bipush 0
      // 82d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 82d6: ldc_w -23.0856
      // 82d9: ldc_w -9.2342
      // 82dc: ldc_w -4.6171
      // 82df: ldc_w 31.0189
      // 82e2: ldc_w 18.4684
      // 82e5: ldc_w 9.2342
      // 82e8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 82eb: dup
      // 82ec: fconst_0
      // 82ed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 82f0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 82f3: bipush 0
      // 82f4: bipush 0
      // 82f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 82f8: ldc_w -23.0856
      // 82fb: fconst_0
      // 82fc: ldc_w -13.8513
      // 82ff: ldc_w 31.0189
      // 8302: ldc_w 9.2342
      // 8305: ldc_w 9.2342
      // 8308: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 830b: dup
      // 830c: fconst_0
      // 830d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8310: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8313: ldc_w -31.0189
      // 8316: fconst_0
      // 8317: fconst_0
      // 8318: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 831b: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 831e: astore 28
      // 8320: aload 28
      // 8322: ldc "part_01_84"
      // 8324: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8327: bipush 0
      // 8328: bipush 0
      // 8329: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 832c: ldc_w -23.0856
      // 832f: ldc_w -9.2342
      // 8332: ldc_w -4.6171
      // 8335: ldc_w 27.7027
      // 8338: ldc_w 18.4684
      // 833b: ldc_w 9.2342
      // 833e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8341: dup
      // 8342: fconst_0
      // 8343: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8346: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8349: bipush 0
      // 834a: bipush 0
      // 834b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 834e: ldc_w -23.0856
      // 8351: fconst_0
      // 8352: ldc_w -13.8513
      // 8355: ldc_w 27.7027
      // 8358: ldc_w 9.2342
      // 835b: ldc_w 9.2342
      // 835e: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8361: dup
      // 8362: fconst_0
      // 8363: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8366: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8369: ldc_w -27.7027
      // 836c: fconst_0
      // 836d: fconst_0
      // 836e: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8371: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8374: astore 29
      // 8376: aload 29
      // 8378: ldc "part_01_85"
      // 837a: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 837d: bipush 0
      // 837e: bipush 0
      // 837f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8382: ldc_w -26.4018
      // 8385: ldc_w -4.6171
      // 8388: ldc_w -4.6171
      // 838b: ldc_w 27.7027
      // 838e: ldc_w 9.2342
      // 8391: ldc_w 9.2342
      // 8394: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8397: dup
      // 8398: fconst_0
      // 8399: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 839c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 839f: ldc_w -24.3864
      // 83a2: ldc_w 4.6171
      // 83a5: fconst_0
      // 83a6: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 83a9: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 83ac: astore 30
      // 83ae: aload 30
      // 83b0: ldc "part_01_86"
      // 83b2: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 83b5: bipush 0
      // 83b6: bipush 0
      // 83b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 83ba: ldc_w -26.4018
      // 83bd: ldc_w -4.6171
      // 83c0: ldc_w -4.6171
      // 83c3: ldc_w 27.7027
      // 83c6: ldc_w 9.2342
      // 83c9: ldc_w 9.2342
      // 83cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 83cf: dup
      // 83d0: fconst_0
      // 83d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 83d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 83d7: ldc_w -27.7027
      // 83da: fconst_0
      // 83db: fconst_0
      // 83dc: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 83df: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 83e2: astore 31
      // 83e4: aload 31
      // 83e6: ldc "part_01_87"
      // 83e8: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 83eb: bipush 0
      // 83ec: bipush 0
      // 83ed: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 83f0: ldc_w -26.4018
      // 83f3: ldc_w -4.6171
      // 83f6: ldc_w -4.6171
      // 83f9: ldc_w 27.7027
      // 83fc: ldc_w 9.2342
      // 83ff: ldc_w 9.2342
      // 8402: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8405: dup
      // 8406: fconst_0
      // 8407: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 840a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 840d: ldc_w -27.7027
      // 8410: fconst_0
      // 8411: fconst_0
      // 8412: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8415: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8418: astore 32
      // 841a: aload 2
      // 841b: ldc "Tentacle_10"
      // 841d: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8420: bipush 0
      // 8421: bipush 0
      // 8422: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8425: ldc_w -23.0856
      // 8428: ldc_w -9.2342
      // 842b: ldc_w -7.8513
      // 842e: ldc_w 31.0189
      // 8431: ldc_w 18.4684
      // 8434: ldc_w 18.4684
      // 8437: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 843a: dup
      // 843b: fconst_0
      // 843c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 843f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8442: ldc_w 94.0618
      // 8445: ldc_w -117.0
      // 8448: ldc_w 13.0
      // 844b: ldc_w 3.1416
      // 844e: fconst_0
      // 844f: ldc_w 1.5708
      // 8452: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8455: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8458: astore 33
      // 845a: aload 33
      // 845c: ldc "part_01_88"
      // 845e: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8461: bipush 0
      // 8462: bipush 0
      // 8463: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8466: ldc_w -23.0856
      // 8469: ldc_w -9.2342
      // 846c: ldc_w -13.8513
      // 846f: ldc_w 27.7027
      // 8472: ldc_w 18.4684
      // 8475: ldc_w 18.4684
      // 8478: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 847b: dup
      // 847c: fconst_0
      // 847d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8480: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8483: ldc_w -27.7027
      // 8486: fconst_0
      // 8487: ldc_w 6.0
      // 848a: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 848d: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8490: astore 34
      // 8492: aload 34
      // 8494: ldc "part_01_89"
      // 8496: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8499: bipush 0
      // 849a: bipush 0
      // 849b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 849e: ldc_w -23.0856
      // 84a1: ldc_w -9.2342
      // 84a4: ldc_w -4.6171
      // 84a7: ldc_w 31.0189
      // 84aa: ldc_w 18.4684
      // 84ad: ldc_w 9.2342
      // 84b0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 84b3: dup
      // 84b4: fconst_0
      // 84b5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 84b8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 84bb: bipush 0
      // 84bc: bipush 0
      // 84bd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 84c0: ldc_w -23.0856
      // 84c3: fconst_0
      // 84c4: ldc_w -13.8513
      // 84c7: ldc_w 31.0189
      // 84ca: ldc_w 9.2342
      // 84cd: ldc_w 9.2342
      // 84d0: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 84d3: dup
      // 84d4: fconst_0
      // 84d5: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 84d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 84db: ldc_w -31.0189
      // 84de: fconst_0
      // 84df: fconst_0
      // 84e0: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 84e3: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 84e6: astore 35
      // 84e8: aload 35
      // 84ea: ldc "part_01_90"
      // 84ec: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 84ef: bipush 0
      // 84f0: bipush 0
      // 84f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 84f4: ldc_w -23.0856
      // 84f7: ldc_w -9.2342
      // 84fa: ldc_w -4.6171
      // 84fd: ldc_w 27.7027
      // 8500: ldc_w 18.4684
      // 8503: ldc_w 9.2342
      // 8506: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8509: dup
      // 850a: fconst_0
      // 850b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 850e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8511: bipush 0
      // 8512: bipush 0
      // 8513: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8516: ldc_w -23.0856
      // 8519: fconst_0
      // 851a: ldc_w -13.8513
      // 851d: ldc_w 27.7027
      // 8520: ldc_w 9.2342
      // 8523: ldc_w 9.2342
      // 8526: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8529: dup
      // 852a: fconst_0
      // 852b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 852e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8531: ldc_w -27.7027
      // 8534: fconst_0
      // 8535: fconst_0
      // 8536: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8539: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 853c: astore 36
      // 853e: aload 36
      // 8540: ldc "part_01_91"
      // 8542: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8545: bipush 0
      // 8546: bipush 0
      // 8547: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 854a: ldc_w -26.4018
      // 854d: ldc_w -4.6171
      // 8550: ldc_w -4.6171
      // 8553: ldc_w 27.7027
      // 8556: ldc_w 9.2342
      // 8559: ldc_w 9.2342
      // 855c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 855f: dup
      // 8560: fconst_0
      // 8561: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8564: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8567: ldc_w -24.3864
      // 856a: ldc_w 4.6171
      // 856d: fconst_0
      // 856e: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8571: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8574: astore 37
      // 8576: aload 37
      // 8578: ldc "part_01_92"
      // 857a: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 857d: bipush 0
      // 857e: bipush 0
      // 857f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8582: ldc_w -26.4018
      // 8585: ldc_w -4.6171
      // 8588: ldc_w -4.6171
      // 858b: ldc_w 27.7027
      // 858e: ldc_w 9.2342
      // 8591: ldc_w 9.2342
      // 8594: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8597: dup
      // 8598: fconst_0
      // 8599: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 859c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 859f: ldc_w -27.7027
      // 85a2: fconst_0
      // 85a3: fconst_0
      // 85a4: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 85a7: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 85aa: astore 38
      // 85ac: aload 38
      // 85ae: ldc "part_01_93"
      // 85b0: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 85b3: bipush 0
      // 85b4: bipush 0
      // 85b5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 85b8: ldc_w -26.4018
      // 85bb: ldc_w -4.6171
      // 85be: ldc_w -4.6171
      // 85c1: ldc_w 27.7027
      // 85c4: ldc_w 9.2342
      // 85c7: ldc_w 9.2342
      // 85ca: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 85cd: dup
      // 85ce: fconst_0
      // 85cf: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 85d2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 85d5: ldc_w -27.7027
      // 85d8: fconst_0
      // 85d9: fconst_0
      // 85da: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 85dd: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 85e0: astore 39
      // 85e2: aload 2
      // 85e3: ldc "Tentacle_11"
      // 85e5: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 85e8: bipush 0
      // 85e9: bipush 0
      // 85ea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 85ed: ldc_w -4.6171
      // 85f0: ldc_w -9.2342
      // 85f3: ldc_w 13.8513
      // 85f6: ldc_w 9.2342
      // 85f9: ldc_w 9.2342
      // 85fc: ldc_w 9.2342
      // 85ff: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8602: dup
      // 8603: fconst_0
      // 8604: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8607: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 860a: bipush 0
      // 860b: bipush 0
      // 860c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 860f: ldc_w -60.0224
      // 8612: ldc_w -18.4684
      // 8615: ldc_w -13.8513
      // 8618: ldc_w 64.6395
      // 861b: ldc_w 27.7027
      // 861e: ldc_w 27.7027
      // 8621: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8624: dup
      // 8625: fconst_0
      // 8626: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8629: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 862c: bipush 0
      // 862d: bipush 0
      // 862e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8631: ldc_w -13.8513
      // 8634: ldc_w -9.2342
      // 8637: ldc_w 13.8513
      // 863a: ldc_w 9.2342
      // 863d: ldc_w 9.2342
      // 8640: ldc_w 9.2342
      // 8643: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8646: dup
      // 8647: fconst_0
      // 8648: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 864b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 864e: bipush 0
      // 864f: bipush 0
      // 8650: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8653: ldc_w -23.0856
      // 8656: ldc_w -9.2342
      // 8659: ldc_w 13.8513
      // 865c: ldc_w 9.2342
      // 865f: ldc_w 9.2342
      // 8662: ldc_w 9.2342
      // 8665: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8668: dup
      // 8669: fconst_0
      // 866a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 866d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8670: bipush 0
      // 8671: bipush 0
      // 8672: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8675: ldc_w -32.3198
      // 8678: ldc_w -9.2342
      // 867b: ldc_w 13.8513
      // 867e: ldc_w 9.2342
      // 8681: ldc_w 9.2342
      // 8684: ldc_w 9.2342
      // 8687: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 868a: dup
      // 868b: fconst_0
      // 868c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 868f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8692: bipush 0
      // 8693: bipush 0
      // 8694: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8697: ldc_w -41.554
      // 869a: ldc_w -9.2342
      // 869d: ldc_w 13.8513
      // 86a0: ldc_w 9.2342
      // 86a3: ldc_w 9.2342
      // 86a6: ldc_w 9.2342
      // 86a9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 86ac: dup
      // 86ad: fconst_0
      // 86ae: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 86b1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 86b4: bipush 0
      // 86b5: bipush 0
      // 86b6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 86b9: ldc_w -50.7882
      // 86bc: ldc_w -9.2342
      // 86bf: ldc_w 13.8513
      // 86c2: ldc_w 9.2342
      // 86c5: ldc_w 9.2342
      // 86c8: ldc_w 9.2342
      // 86cb: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 86ce: dup
      // 86cf: fconst_0
      // 86d0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 86d3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 86d6: bipush 0
      // 86d7: bipush 0
      // 86d8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 86db: ldc_w -60.0224
      // 86de: ldc_w -9.2342
      // 86e1: ldc_w 13.8513
      // 86e4: ldc_w 9.2342
      // 86e7: ldc_w 9.2342
      // 86ea: ldc_w 9.2342
      // 86ed: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 86f0: dup
      // 86f1: fconst_0
      // 86f2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 86f5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 86f8: bipush 0
      // 86f9: bipush 0
      // 86fa: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 86fd: ldc_w -27.0
      // 8700: ldc_w -19.0
      // 8703: ldc_w -15.0
      // 8706: ldc_w 16.0
      // 8709: ldc_w 16.0
      // 870c: ldc_w 16.0
      // 870f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8712: dup
      // 8713: fconst_0
      // 8714: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8717: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 871a: bipush 0
      // 871b: bipush 0
      // 871c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 871f: ldc_w -43.0
      // 8722: ldc_w -19.0
      // 8725: ldc_w -15.0
      // 8728: ldc_w 16.0
      // 872b: ldc_w 16.0
      // 872e: ldc_w 16.0
      // 8731: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8734: dup
      // 8735: fconst_0
      // 8736: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8739: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 873c: bipush 0
      // 873d: bipush 0
      // 873e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8741: ldc_w -43.0
      // 8744: ldc_w -3.0
      // 8747: ldc_w -15.0
      // 874a: ldc_w 16.0
      // 874d: ldc_w 16.0
      // 8750: ldc_w 16.0
      // 8753: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8756: dup
      // 8757: fconst_0
      // 8758: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 875b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 875e: bipush 0
      // 875f: bipush 0
      // 8760: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8763: ldc_w -27.0
      // 8766: ldc_w -3.0
      // 8769: ldc_w -15.0
      // 876c: ldc_w 16.0
      // 876f: ldc_w 16.0
      // 8772: ldc_w 16.0
      // 8775: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8778: dup
      // 8779: fconst_0
      // 877a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 877d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8780: bipush 0
      // 8781: bipush 0
      // 8782: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8785: ldc_w -11.0
      // 8788: ldc_w -19.0
      // 878b: ldc_w -15.0
      // 878e: ldc_w 16.0
      // 8791: ldc_w 16.0
      // 8794: ldc_w 16.0
      // 8797: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 879a: dup
      // 879b: fconst_0
      // 879c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 879f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 87a2: bipush 0
      // 87a3: bipush 0
      // 87a4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 87a7: ldc_w -11.0
      // 87aa: ldc_w -5.0
      // 87ad: ldc_w -15.0
      // 87b0: ldc_w 16.0
      // 87b3: ldc_w 16.0
      // 87b6: ldc_w 16.0
      // 87b9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 87bc: dup
      // 87bd: fconst_0
      // 87be: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 87c1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 87c4: ldc_w 131.0
      // 87c7: ldc_w 52.0
      // 87ca: ldc_w -113.0
      // 87cd: ldc_w -1.5708
      // 87d0: fconst_0
      // 87d1: ldc_w -1.5708
      // 87d4: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 87d7: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 87da: astore 40
      // 87dc: aload 40
      // 87de: ldc "part_01_94"
      // 87e0: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 87e3: bipush 0
      // 87e4: bipush 0
      // 87e5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 87e8: ldc_w -60.0224
      // 87eb: ldc_w -18.4684
      // 87ee: ldc_w -13.8513
      // 87f1: ldc_w 64.6395
      // 87f4: ldc_w 27.7027
      // 87f7: ldc_w 27.7027
      // 87fa: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 87fd: dup
      // 87fe: fconst_0
      // 87ff: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8802: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8805: ldc_w -64.6395
      // 8808: fconst_0
      // 8809: fconst_0
      // 880a: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 880d: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8810: astore 41
      // 8812: aload 41
      // 8814: ldc "part_01_95"
      // 8816: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8819: bipush 0
      // 881a: bipush 0
      // 881b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 881e: ldc_w -41.554
      // 8821: ldc_w -18.4684
      // 8824: ldc_w -13.8513
      // 8827: ldc_w 46.1711
      // 882a: ldc_w 27.7027
      // 882d: ldc_w 18.4684
      // 8830: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8833: dup
      // 8834: fconst_0
      // 8835: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8838: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 883b: bipush 0
      // 883c: bipush 0
      // 883d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8840: ldc_w -32.3198
      // 8843: ldc_w -9.2342
      // 8846: ldc_w 4.6171
      // 8849: ldc_w 36.9369
      // 884c: ldc_w 18.4684
      // 884f: ldc_w 9.2342
      // 8852: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8855: dup
      // 8856: fconst_0
      // 8857: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 885a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 885d: bipush 0
      // 885e: bipush 0
      // 885f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8862: ldc_w -41.554
      // 8865: ldc_w -9.2342
      // 8868: ldc_w 4.6171
      // 886b: ldc_w 9.2342
      // 886e: ldc_w 9.2342
      // 8871: ldc_w 9.2342
      // 8874: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8877: dup
      // 8878: fconst_0
      // 8879: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 887c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 887f: bipush 0
      // 8880: bipush 0
      // 8881: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8884: ldc_w -41.554
      // 8887: ldc_w -9.2342
      // 888a: ldc_w -23.0856
      // 888d: ldc_w 46.1711
      // 8890: ldc_w 18.4684
      // 8893: ldc_w 9.2342
      // 8896: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8899: dup
      // 889a: fconst_0
      // 889b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 889e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 88a1: ldc_w -64.6395
      // 88a4: fconst_0
      // 88a5: fconst_0
      // 88a6: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 88a9: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 88ac: astore 42
      // 88ae: aload 42
      // 88b0: ldc "part_01_96"
      // 88b2: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 88b5: bipush 0
      // 88b6: bipush 0
      // 88b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 88ba: ldc_w -32.3198
      // 88bd: ldc_w -18.4684
      // 88c0: ldc_w -4.6171
      // 88c3: ldc_w 36.9369
      // 88c6: ldc_w 27.7027
      // 88c9: ldc_w 9.2342
      // 88cc: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 88cf: dup
      // 88d0: fconst_0
      // 88d1: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 88d4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 88d7: bipush 0
      // 88d8: bipush 0
      // 88d9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 88dc: ldc_w -23.0855
      // 88df: ldc_w -9.2342
      // 88e2: ldc_w 4.6171
      // 88e5: ldc_w 27.7027
      // 88e8: ldc_w 9.2342
      // 88eb: ldc_w 9.2342
      // 88ee: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 88f1: dup
      // 88f2: fconst_0
      // 88f3: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 88f6: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 88f9: bipush 0
      // 88fa: bipush 0
      // 88fb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 88fe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8901: ldc_w -31.7167
      // 8904: ldc_w -9.2342
      // 8907: ldc_w -23.0856
      // 890a: ldc_w 36.9369
      // 890d: ldc_w 18.4684
      // 8910: ldc_w 18.4684
      // 8913: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8916: dup
      // 8917: fconst_0
      // 8918: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 891b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 891e: bipush 0
      // 891f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.mirror (Z)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8922: bipush 0
      // 8923: bipush 0
      // 8924: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8927: ldc_w -32.3198
      // 892a: ldc_w 9.2342
      // 892d: ldc_w -13.8513
      // 8930: ldc_w 9.2342
      // 8933: ldc_w 9.2342
      // 8936: ldc_w 18.4684
      // 8939: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 893c: dup
      // 893d: fconst_0
      // 893e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8941: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8944: ldc_w -46.1711
      // 8947: fconst_0
      // 8948: fconst_0
      // 8949: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 894c: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 894f: astore 43
      // 8951: aload 43
      // 8953: ldc "part_01_97"
      // 8955: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8958: bipush 0
      // 8959: bipush 0
      // 895a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 895d: ldc_w -32.3198
      // 8960: ldc_w -18.4684
      // 8963: ldc_w -4.6171
      // 8966: ldc_w 36.9369
      // 8969: ldc_w 27.7027
      // 896c: ldc_w 9.2342
      // 896f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8972: dup
      // 8973: fconst_0
      // 8974: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8977: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 897a: bipush 0
      // 897b: bipush 0
      // 897c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 897f: ldc_w -32.3198
      // 8982: ldc_w -9.2342
      // 8985: ldc_w 4.6171
      // 8988: ldc_w 46.1711
      // 898b: ldc_w 9.2342
      // 898e: ldc_w 9.2342
      // 8991: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8994: dup
      // 8995: fconst_0
      // 8996: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8999: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 899c: bipush 0
      // 899d: bipush 0
      // 899e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 89a1: ldc_w -32.3198
      // 89a4: ldc_w 9.2342
      // 89a7: ldc_w -13.8513
      // 89aa: ldc_w 36.9369
      // 89ad: ldc_w 9.2342
      // 89b0: ldc_w 18.4684
      // 89b3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 89b6: dup
      // 89b7: fconst_0
      // 89b8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 89bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 89be: bipush 0
      // 89bf: bipush 0
      // 89c0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 89c3: ldc_w -23.0856
      // 89c6: ldc_w -9.2342
      // 89c9: ldc_w -23.0856
      // 89cc: ldc_w 27.7027
      // 89cf: ldc_w 18.4684
      // 89d2: ldc_w 18.4684
      // 89d5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 89d8: dup
      // 89d9: fconst_0
      // 89da: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 89dd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 89e0: bipush 0
      // 89e1: bipush 0
      // 89e2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 89e5: ldc_w -32.3198
      // 89e8: fconst_0
      // 89e9: ldc_w -23.0856
      // 89ec: ldc_w 9.2342
      // 89ef: ldc_w 9.2342
      // 89f2: ldc_w 18.4684
      // 89f5: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 89f8: dup
      // 89f9: fconst_0
      // 89fa: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 89fd: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a00: bipush 0
      // 8a01: bipush 0
      // 8a02: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a05: ldc_w -32.3198
      // 8a08: ldc_w -9.2342
      // 8a0b: ldc_w -13.8513
      // 8a0e: ldc_w 9.2342
      // 8a11: ldc_w 9.2342
      // 8a14: ldc_w 9.2342
      // 8a17: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8a1a: dup
      // 8a1b: fconst_0
      // 8a1c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8a1f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a22: ldc_w -36.9369
      // 8a25: fconst_0
      // 8a26: fconst_0
      // 8a27: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8a2a: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8a2d: astore 44
      // 8a2f: aload 44
      // 8a31: ldc "part_01_98"
      // 8a33: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a36: bipush 0
      // 8a37: bipush 0
      // 8a38: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a3b: ldc_w -32.3198
      // 8a3e: ldc_w -18.4684
      // 8a41: ldc_w -4.6171
      // 8a44: ldc_w 36.9369
      // 8a47: ldc_w 27.7027
      // 8a4a: ldc_w 9.2342
      // 8a4d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8a50: dup
      // 8a51: fconst_0
      // 8a52: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8a55: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a58: bipush 0
      // 8a59: bipush 0
      // 8a5a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a5d: ldc_w -32.3198
      // 8a60: ldc_w -9.2342
      // 8a63: ldc_w 4.6171
      // 8a66: ldc_w 36.9369
      // 8a69: ldc_w 9.2342
      // 8a6c: ldc_w 9.2342
      // 8a6f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8a72: dup
      // 8a73: fconst_0
      // 8a74: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8a77: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a7a: bipush 0
      // 8a7b: bipush 0
      // 8a7c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a7f: ldc_w -32.3198
      // 8a82: fconst_0
      // 8a83: ldc_w 4.6171
      // 8a86: ldc_w 18.4684
      // 8a89: ldc_w 9.2342
      // 8a8c: ldc_w 9.2342
      // 8a8f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8a92: dup
      // 8a93: fconst_0
      // 8a94: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8a97: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a9a: bipush 0
      // 8a9b: bipush 0
      // 8a9c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8a9f: ldc_w -32.3198
      // 8aa2: ldc_w 9.2342
      // 8aa5: ldc_w -13.8513
      // 8aa8: ldc_w 36.9369
      // 8aab: ldc_w 9.2342
      // 8aae: ldc_w 18.4684
      // 8ab1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8ab4: dup
      // 8ab5: fconst_0
      // 8ab6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8ab9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8abc: bipush 0
      // 8abd: bipush 0
      // 8abe: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ac1: ldc_w -13.8513
      // 8ac4: fconst_0
      // 8ac5: ldc_w -23.0856
      // 8ac8: ldc_w 18.4684
      // 8acb: ldc_w 9.2342
      // 8ace: ldc_w 9.2342
      // 8ad1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8ad4: dup
      // 8ad5: fconst_0
      // 8ad6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8ad9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8adc: bipush 0
      // 8add: bipush 0
      // 8ade: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ae1: ldc_w -32.3198
      // 8ae4: ldc_w -9.2342
      // 8ae7: ldc_w -13.8513
      // 8aea: ldc_w 36.9369
      // 8aed: ldc_w 18.4684
      // 8af0: ldc_w 9.2342
      // 8af3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8af6: dup
      // 8af7: fconst_0
      // 8af8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8afb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8afe: ldc_w -36.9369
      // 8b01: fconst_0
      // 8b02: fconst_0
      // 8b03: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8b06: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8b09: astore 45
      // 8b0b: aload 45
      // 8b0d: ldc "part_01_99"
      // 8b0f: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b12: bipush 0
      // 8b13: bipush 0
      // 8b14: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b17: ldc_w -32.3198
      // 8b1a: ldc_w -18.4684
      // 8b1d: ldc_w -4.6171
      // 8b20: ldc_w 40.2531
      // 8b23: ldc_w 27.7027
      // 8b26: ldc_w 9.2342
      // 8b29: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8b2c: dup
      // 8b2d: fconst_0
      // 8b2e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8b31: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b34: bipush 0
      // 8b35: bipush 0
      // 8b36: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b39: ldc_w -32.3198
      // 8b3c: ldc_w -9.2342
      // 8b3f: ldc_w 4.6171
      // 8b42: ldc_w 36.9369
      // 8b45: ldc_w 9.2342
      // 8b48: ldc_w 9.2342
      // 8b4b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8b4e: dup
      // 8b4f: fconst_0
      // 8b50: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8b53: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b56: bipush 0
      // 8b57: bipush 0
      // 8b58: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b5b: ldc_w -32.3198
      // 8b5e: fconst_0
      // 8b5f: ldc_w 4.6171
      // 8b62: ldc_w 36.9369
      // 8b65: ldc_w 9.2342
      // 8b68: ldc_w 9.2342
      // 8b6b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8b6e: dup
      // 8b6f: fconst_0
      // 8b70: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8b73: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b76: bipush 0
      // 8b77: bipush 0
      // 8b78: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b7b: ldc_w -32.3198
      // 8b7e: ldc_w 9.2342
      // 8b81: ldc_w -4.6171
      // 8b84: ldc_w 36.9369
      // 8b87: ldc_w 9.2342
      // 8b8a: ldc_w 9.2342
      // 8b8d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8b90: dup
      // 8b91: fconst_0
      // 8b92: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8b95: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b98: bipush 0
      // 8b99: bipush 0
      // 8b9a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8b9d: ldc_w -4.6171
      // 8ba0: ldc_w 9.2342
      // 8ba3: ldc_w -13.8513
      // 8ba6: ldc_w 9.2342
      // 8ba9: ldc_w 9.2342
      // 8bac: ldc_w 9.2342
      // 8baf: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8bb2: dup
      // 8bb3: fconst_0
      // 8bb4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8bb7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8bba: bipush 0
      // 8bbb: bipush 0
      // 8bbc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8bbf: ldc_w -32.3198
      // 8bc2: ldc_w -9.2342
      // 8bc5: ldc_w -13.8513
      // 8bc8: ldc_w 36.9369
      // 8bcb: ldc_w 18.4684
      // 8bce: ldc_w 9.2342
      // 8bd1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8bd4: dup
      // 8bd5: fconst_0
      // 8bd6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8bd9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8bdc: ldc_w -36.9369
      // 8bdf: fconst_0
      // 8be0: fconst_0
      // 8be1: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8be4: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8be7: astore 46
      // 8be9: aload 46
      // 8beb: ldc "part_01_100"
      // 8bed: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8bf0: bipush 0
      // 8bf1: bipush 0
      // 8bf2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8bf5: ldc_w -23.0856
      // 8bf8: ldc_w -9.2342
      // 8bfb: ldc_w -13.8513
      // 8bfe: ldc_w 27.7027
      // 8c01: ldc_w 18.4684
      // 8c04: ldc_w 18.4684
      // 8c07: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8c0a: dup
      // 8c0b: fconst_0
      // 8c0c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8c0f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c12: bipush 0
      // 8c13: bipush 0
      // 8c14: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c17: ldc_w -23.0856
      // 8c1a: fconst_0
      // 8c1b: ldc_w 4.6171
      // 8c1e: ldc_w 27.7027
      // 8c21: ldc_w 9.2342
      // 8c24: ldc_w 9.2342
      // 8c27: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8c2a: dup
      // 8c2b: fconst_0
      // 8c2c: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8c2f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c32: ldc_w -36.9369
      // 8c35: fconst_0
      // 8c36: fconst_0
      // 8c37: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8c3a: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8c3d: astore 47
      // 8c3f: aload 47
      // 8c41: ldc "part_01_101"
      // 8c43: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c46: bipush 0
      // 8c47: bipush 0
      // 8c48: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c4b: ldc_w -23.0856
      // 8c4e: ldc_w -9.2342
      // 8c51: ldc_w -13.8513
      // 8c54: ldc_w 27.7027
      // 8c57: ldc_w 18.4684
      // 8c5a: ldc_w 18.4684
      // 8c5d: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8c60: dup
      // 8c61: fconst_0
      // 8c62: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8c65: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c68: ldc_w -27.7027
      // 8c6b: fconst_0
      // 8c6c: fconst_0
      // 8c6d: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8c70: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8c73: astore 48
      // 8c75: aload 48
      // 8c77: ldc "part_01_102"
      // 8c79: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c7c: bipush 0
      // 8c7d: bipush 0
      // 8c7e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c81: ldc_w -23.0855
      // 8c84: ldc_w -9.2342
      // 8c87: ldc_w -13.8513
      // 8c8a: ldc_w 31.0189
      // 8c8d: ldc_w 18.4684
      // 8c90: ldc_w 18.4684
      // 8c93: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8c96: dup
      // 8c97: fconst_0
      // 8c98: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8c9b: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8c9e: ldc_w -31.0189
      // 8ca1: fconst_0
      // 8ca2: fconst_0
      // 8ca3: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8ca6: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8ca9: astore 49
      // 8cab: aload 49
      // 8cad: ldc "part_01_103"
      // 8caf: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8cb2: bipush 0
      // 8cb3: bipush 0
      // 8cb4: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8cb7: ldc_w -23.0856
      // 8cba: ldc_w -9.2342
      // 8cbd: ldc_w -13.8513
      // 8cc0: ldc_w 31.0189
      // 8cc3: ldc_w 18.4684
      // 8cc6: ldc_w 18.4684
      // 8cc9: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8ccc: dup
      // 8ccd: fconst_0
      // 8cce: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8cd1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8cd4: ldc_w -31.0189
      // 8cd7: fconst_0
      // 8cd8: fconst_0
      // 8cd9: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8cdc: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8cdf: astore 50
      // 8ce1: aload 50
      // 8ce3: ldc "part_01_104"
      // 8ce5: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ce8: bipush 0
      // 8ce9: bipush 0
      // 8cea: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ced: ldc_w -23.0855
      // 8cf0: ldc_w -9.2342
      // 8cf3: ldc_w -13.8513
      // 8cf6: ldc_w 27.7027
      // 8cf9: ldc_w 18.4684
      // 8cfc: ldc_w 18.4684
      // 8cff: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8d02: dup
      // 8d03: fconst_0
      // 8d04: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8d07: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d0a: ldc_w -27.7027
      // 8d0d: fconst_0
      // 8d0e: fconst_0
      // 8d0f: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8d12: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8d15: astore 51
      // 8d17: aload 51
      // 8d19: ldc "part_01_105"
      // 8d1b: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d1e: bipush 0
      // 8d1f: bipush 0
      // 8d20: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d23: ldc_w -23.0856
      // 8d26: ldc_w -9.2342
      // 8d29: ldc_w -4.6171
      // 8d2c: ldc_w 31.0189
      // 8d2f: ldc_w 18.4684
      // 8d32: ldc_w 9.2342
      // 8d35: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8d38: dup
      // 8d39: fconst_0
      // 8d3a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8d3d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d40: bipush 0
      // 8d41: bipush 0
      // 8d42: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d45: ldc_w -23.0856
      // 8d48: fconst_0
      // 8d49: ldc_w -13.8513
      // 8d4c: ldc_w 31.0189
      // 8d4f: ldc_w 9.2342
      // 8d52: ldc_w 9.2342
      // 8d55: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8d58: dup
      // 8d59: fconst_0
      // 8d5a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8d5d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d60: ldc_w -31.0189
      // 8d63: fconst_0
      // 8d64: fconst_0
      // 8d65: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8d68: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8d6b: astore 52
      // 8d6d: aload 52
      // 8d6f: ldc "part_01_106"
      // 8d71: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d74: bipush 0
      // 8d75: bipush 0
      // 8d76: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d79: ldc_w -23.0856
      // 8d7c: ldc_w -9.2342
      // 8d7f: ldc_w -4.6171
      // 8d82: ldc_w 27.7027
      // 8d85: ldc_w 18.4684
      // 8d88: ldc_w 9.2342
      // 8d8b: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8d8e: dup
      // 8d8f: fconst_0
      // 8d90: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8d93: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d96: bipush 0
      // 8d97: bipush 0
      // 8d98: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8d9b: ldc_w -23.0856
      // 8d9e: fconst_0
      // 8d9f: ldc_w -13.8513
      // 8da2: ldc_w 27.7027
      // 8da5: ldc_w 9.2342
      // 8da8: ldc_w 9.2342
      // 8dab: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8dae: dup
      // 8daf: fconst_0
      // 8db0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8db3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8db6: ldc_w -27.7027
      // 8db9: fconst_0
      // 8dba: fconst_0
      // 8dbb: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8dbe: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8dc1: astore 53
      // 8dc3: aload 53
      // 8dc5: ldc "part_01_107"
      // 8dc7: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8dca: bipush 0
      // 8dcb: bipush 0
      // 8dcc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8dcf: ldc_w -26.4018
      // 8dd2: ldc_w -4.6171
      // 8dd5: ldc_w -4.6171
      // 8dd8: ldc_w 27.7027
      // 8ddb: ldc_w 9.2342
      // 8dde: ldc_w 9.2342
      // 8de1: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8de4: dup
      // 8de5: fconst_0
      // 8de6: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8de9: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8dec: ldc_w -24.3864
      // 8def: ldc_w 4.6171
      // 8df2: fconst_0
      // 8df3: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8df6: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8df9: astore 54
      // 8dfb: aload 54
      // 8dfd: ldc "part_01_108"
      // 8dff: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e02: bipush 0
      // 8e03: bipush 0
      // 8e04: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e07: ldc_w -26.4018
      // 8e0a: ldc_w -4.6171
      // 8e0d: ldc_w -4.6171
      // 8e10: ldc_w 27.7027
      // 8e13: ldc_w 9.2342
      // 8e16: ldc_w 9.2342
      // 8e19: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8e1c: dup
      // 8e1d: fconst_0
      // 8e1e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8e21: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e24: ldc_w -27.7027
      // 8e27: fconst_0
      // 8e28: fconst_0
      // 8e29: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8e2c: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8e2f: astore 55
      // 8e31: aload 55
      // 8e33: ldc "part_01_109"
      // 8e35: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e38: bipush 0
      // 8e39: bipush 0
      // 8e3a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e3d: ldc_w -26.4018
      // 8e40: ldc_w -4.6171
      // 8e43: ldc_w -4.6171
      // 8e46: ldc_w 27.7027
      // 8e49: ldc_w 9.2342
      // 8e4c: ldc_w 9.2342
      // 8e4f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8e52: dup
      // 8e53: fconst_0
      // 8e54: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8e57: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e5a: ldc_w -27.7027
      // 8e5d: fconst_0
      // 8e5e: fconst_0
      // 8e5f: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8e62: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8e65: astore 56
      // 8e67: aload 2
      // 8e68: ldc "Tentacle_12"
      // 8e6a: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e6d: bipush 0
      // 8e6e: bipush 0
      // 8e6f: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e72: ldc_w -32.3198
      // 8e75: ldc_w -18.4684
      // 8e78: ldc_w -4.6171
      // 8e7b: ldc_w 53.9369
      // 8e7e: ldc_w 27.7027
      // 8e81: ldc_w 9.2342
      // 8e84: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8e87: dup
      // 8e88: fconst_0
      // 8e89: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8e8c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e8f: bipush 0
      // 8e90: bipush 0
      // 8e91: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8e94: ldc_w -32.3198
      // 8e97: ldc_w -9.2342
      // 8e9a: ldc_w 4.6171
      // 8e9d: ldc_w 53.9369
      // 8ea0: ldc_w 9.2342
      // 8ea3: ldc_w 9.2342
      // 8ea6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8ea9: dup
      // 8eaa: fconst_0
      // 8eab: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8eae: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8eb1: bipush 0
      // 8eb2: bipush 0
      // 8eb3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8eb6: ldc_w -32.3198
      // 8eb9: fconst_0
      // 8eba: ldc_w 4.6171
      // 8ebd: ldc_w 18.4684
      // 8ec0: ldc_w 9.2342
      // 8ec3: ldc_w 9.2342
      // 8ec6: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8ec9: dup
      // 8eca: fconst_0
      // 8ecb: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8ece: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ed1: bipush 0
      // 8ed2: bipush 0
      // 8ed3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ed6: ldc_w -32.3198
      // 8ed9: ldc_w 9.2342
      // 8edc: ldc_w -13.8513
      // 8edf: ldc_w 53.9369
      // 8ee2: ldc_w 9.2342
      // 8ee5: ldc_w 18.4684
      // 8ee8: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8eeb: dup
      // 8eec: fconst_0
      // 8eed: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8ef0: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ef3: bipush 0
      // 8ef4: bipush 0
      // 8ef5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ef8: ldc_w -13.8513
      // 8efb: fconst_0
      // 8efc: ldc_w -23.0856
      // 8eff: ldc_w 34.4684
      // 8f02: ldc_w 9.2342
      // 8f05: ldc_w 9.2342
      // 8f08: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8f0b: dup
      // 8f0c: fconst_0
      // 8f0d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8f10: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f13: bipush 0
      // 8f14: bipush 0
      // 8f15: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f18: ldc_w -32.3198
      // 8f1b: ldc_w -9.2342
      // 8f1e: ldc_w -13.8513
      // 8f21: ldc_w 53.9369
      // 8f24: ldc_w 18.4684
      // 8f27: ldc_w 9.2342
      // 8f2a: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8f2d: dup
      // 8f2e: fconst_0
      // 8f2f: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8f32: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f35: ldc_w 29.676
      // 8f38: ldc_w 14.0
      // 8f3b: ldc_w 166.0
      // 8f3e: fconst_0
      // 8f3f: ldc_w 1.5708
      // 8f42: fconst_0
      // 8f43: invokestatic net/minecraft/client/model/geom/PartPose.offsetAndRotation (FFFFFF)Lnet/minecraft/client/model/geom/PartPose;
      // 8f46: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 8f49: astore 57
      // 8f4b: aload 57
      // 8f4d: ldc "part_01_110"
      // 8f4f: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f52: bipush 0
      // 8f53: bipush 0
      // 8f54: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f57: ldc_w -32.3198
      // 8f5a: ldc_w -9.2342
      // 8f5d: ldc_w 4.6171
      // 8f60: ldc_w 36.9369
      // 8f63: ldc_w 9.2342
      // 8f66: ldc_w 9.2342
      // 8f69: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8f6c: dup
      // 8f6d: fconst_0
      // 8f6e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8f71: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f74: bipush 0
      // 8f75: bipush 0
      // 8f76: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f79: ldc_w -32.3198
      // 8f7c: fconst_0
      // 8f7d: ldc_w 4.6171
      // 8f80: ldc_w 36.9369
      // 8f83: ldc_w 9.2342
      // 8f86: ldc_w 9.2342
      // 8f89: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8f8c: dup
      // 8f8d: fconst_0
      // 8f8e: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8f91: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f94: bipush 0
      // 8f95: bipush 0
      // 8f96: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8f99: ldc_w -32.3198
      // 8f9c: ldc_w 9.2342
      // 8f9f: ldc_w -4.6171
      // 8fa2: ldc_w 36.9369
      // 8fa5: ldc_w 9.2342
      // 8fa8: ldc_w 9.2342
      // 8fab: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8fae: dup
      // 8faf: fconst_0
      // 8fb0: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8fb3: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8fb6: bipush 0
      // 8fb7: bipush 0
      // 8fb8: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8fbb: ldc_w -4.6171
      // 8fbe: ldc_w 9.2342
      // 8fc1: ldc_w -13.8513
      // 8fc4: ldc_w 9.2342
      // 8fc7: ldc_w 9.2342
      // 8fca: ldc_w 9.2342
      // 8fcd: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8fd0: dup
      // 8fd1: fconst_0
      // 8fd2: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8fd5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8fd8: bipush 0
      // 8fd9: bipush 0
      // 8fda: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8fdd: ldc_w -32.3198
      // 8fe0: ldc_w -9.2342
      // 8fe3: ldc_w -13.8513
      // 8fe6: ldc_w 36.9369
      // 8fe9: ldc_w 18.4684
      // 8fec: ldc_w 18.2342
      // 8fef: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 8ff2: dup
      // 8ff3: fconst_0
      // 8ff4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 8ff7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 8ffa: ldc_w -36.9369
      // 8ffd: fconst_0
      // 8ffe: fconst_0
      // 8fff: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 9002: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 9005: astore 58
      // 9007: aload 58
      // 9009: ldc "part_01_111"
      // 900b: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 900e: bipush 0
      // 900f: bipush 0
      // 9010: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9013: ldc_w -23.0856
      // 9016: ldc_w -9.2342
      // 9019: ldc_w -13.8513
      // 901c: ldc_w 27.7027
      // 901f: ldc_w 18.4684
      // 9022: ldc_w 18.4684
      // 9025: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 9028: dup
      // 9029: fconst_0
      // 902a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 902d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9030: bipush 0
      // 9031: bipush 0
      // 9032: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9035: ldc_w -23.0856
      // 9038: fconst_0
      // 9039: ldc_w 4.6171
      // 903c: ldc_w 27.7027
      // 903f: ldc_w 9.2342
      // 9042: ldc_w 9.2342
      // 9045: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 9048: dup
      // 9049: fconst_0
      // 904a: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 904d: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9050: ldc_w -36.9369
      // 9053: fconst_0
      // 9054: fconst_0
      // 9055: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 9058: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 905b: astore 59
      // 905d: aload 59
      // 905f: ldc_w "part_01_112"
      // 9062: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9065: bipush 0
      // 9066: bipush 0
      // 9067: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 906a: ldc_w -23.0856
      // 906d: ldc_w -9.2342
      // 9070: ldc_w -13.8513
      // 9073: ldc_w 27.7027
      // 9076: ldc_w 18.4684
      // 9079: ldc_w 18.4684
      // 907c: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 907f: dup
      // 9080: fconst_0
      // 9081: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 9084: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9087: ldc_w -27.7027
      // 908a: fconst_0
      // 908b: fconst_0
      // 908c: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 908f: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 9092: astore 60
      // 9094: aload 60
      // 9096: ldc_w "part_01_113"
      // 9099: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 909c: bipush 0
      // 909d: bipush 0
      // 909e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 90a1: ldc_w -23.0855
      // 90a4: ldc_w -9.2342
      // 90a7: ldc_w -13.8513
      // 90aa: ldc_w 31.0189
      // 90ad: ldc_w 18.4684
      // 90b0: ldc_w 18.4684
      // 90b3: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 90b6: dup
      // 90b7: fconst_0
      // 90b8: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 90bb: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 90be: ldc_w -31.0189
      // 90c1: fconst_0
      // 90c2: fconst_0
      // 90c3: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 90c6: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 90c9: astore 61
      // 90cb: aload 61
      // 90cd: ldc_w "part_01_114"
      // 90d0: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 90d3: bipush 0
      // 90d4: bipush 0
      // 90d5: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 90d8: ldc_w -23.0856
      // 90db: ldc_w -9.2342
      // 90de: ldc_w -13.8513
      // 90e1: ldc_w 31.0189
      // 90e4: ldc_w 18.4684
      // 90e7: ldc_w 18.4684
      // 90ea: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 90ed: dup
      // 90ee: fconst_0
      // 90ef: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 90f2: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 90f5: ldc_w -31.0189
      // 90f8: fconst_0
      // 90f9: fconst_0
      // 90fa: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 90fd: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 9100: astore 62
      // 9102: aload 62
      // 9104: ldc_w "part_01_115"
      // 9107: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 910a: bipush 0
      // 910b: bipush 0
      // 910c: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 910f: ldc_w -23.0856
      // 9112: ldc_w -9.2342
      // 9115: ldc_w -13.8513
      // 9118: ldc_w 27.7027
      // 911b: ldc_w 18.4684
      // 911e: ldc_w 18.4684
      // 9121: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 9124: dup
      // 9125: fconst_0
      // 9126: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 9129: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 912c: ldc_w -27.7027
      // 912f: fconst_0
      // 9130: fconst_0
      // 9131: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 9134: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 9137: astore 63
      // 9139: aload 63
      // 913b: ldc_w "part_01_116"
      // 913e: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9141: bipush 0
      // 9142: bipush 0
      // 9143: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9146: ldc_w -23.0856
      // 9149: ldc_w -9.2342
      // 914c: ldc_w -4.6171
      // 914f: ldc_w 31.0189
      // 9152: ldc_w 18.4684
      // 9155: ldc_w 9.2342
      // 9158: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 915b: dup
      // 915c: fconst_0
      // 915d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 9160: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9163: bipush 0
      // 9164: bipush 0
      // 9165: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9168: ldc_w -23.0856
      // 916b: fconst_0
      // 916c: ldc_w -13.8513
      // 916f: ldc_w 31.0189
      // 9172: ldc_w 9.2342
      // 9175: ldc_w 9.2342
      // 9178: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 917b: dup
      // 917c: fconst_0
      // 917d: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 9180: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9183: ldc_w -31.0189
      // 9186: fconst_0
      // 9187: fconst_0
      // 9188: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 918b: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 918e: astore 64
      // 9190: aload 64
      // 9192: ldc_w "part_01_117"
      // 9195: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9198: bipush 0
      // 9199: bipush 0
      // 919a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 919d: ldc_w -23.0856
      // 91a0: ldc_w -9.2342
      // 91a3: ldc_w -4.6171
      // 91a6: ldc_w 27.7027
      // 91a9: ldc_w 18.4684
      // 91ac: ldc_w 9.2342
      // 91af: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 91b2: dup
      // 91b3: fconst_0
      // 91b4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 91b7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 91ba: bipush 0
      // 91bb: bipush 0
      // 91bc: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 91bf: ldc_w -23.0856
      // 91c2: fconst_0
      // 91c3: ldc_w -13.8513
      // 91c6: ldc_w 27.7027
      // 91c9: ldc_w 9.2342
      // 91cc: ldc_w 9.2342
      // 91cf: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 91d2: dup
      // 91d3: fconst_0
      // 91d4: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 91d7: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 91da: ldc_w -27.7027
      // 91dd: fconst_0
      // 91de: fconst_0
      // 91df: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 91e2: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 91e5: astore 65
      // 91e7: aload 65
      // 91e9: ldc_w "part_01_118"
      // 91ec: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 91ef: bipush 0
      // 91f0: bipush 0
      // 91f1: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 91f4: ldc_w -26.4018
      // 91f7: ldc_w -4.6171
      // 91fa: ldc_w -4.6171
      // 91fd: ldc_w 27.7027
      // 9200: ldc_w 9.2342
      // 9203: ldc_w 9.2342
      // 9206: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 9209: dup
      // 920a: fconst_0
      // 920b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 920e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9211: ldc_w -24.3864
      // 9214: ldc_w 4.6171
      // 9217: fconst_0
      // 9218: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 921b: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 921e: astore 66
      // 9220: aload 66
      // 9222: ldc_w "part_01_119"
      // 9225: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9228: bipush 0
      // 9229: bipush 0
      // 922a: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 922d: ldc_w -26.4018
      // 9230: ldc_w -4.6171
      // 9233: ldc_w -4.6171
      // 9236: ldc_w 27.7027
      // 9239: ldc_w 9.2342
      // 923c: ldc_w 9.2342
      // 923f: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 9242: dup
      // 9243: fconst_0
      // 9244: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 9247: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 924a: ldc_w -27.7027
      // 924d: fconst_0
      // 924e: fconst_0
      // 924f: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 9252: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 9255: astore 67
      // 9257: aload 67
      // 9259: ldc_w "part_01_120"
      // 925c: invokestatic net/minecraft/client/model/geom/builders/CubeListBuilder.create ()Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 925f: bipush 0
      // 9260: bipush 0
      // 9261: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.texOffs (II)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9264: ldc_w -26.4018
      // 9267: ldc_w -4.6171
      // 926a: ldc_w -4.6171
      // 926d: ldc_w 27.7027
      // 9270: ldc_w 9.2342
      // 9273: ldc_w 9.2342
      // 9276: new net/minecraft/client/model/geom/builders/CubeDeformation
      // 9279: dup
      // 927a: fconst_0
      // 927b: invokespecial net/minecraft/client/model/geom/builders/CubeDeformation.<init> (F)V
      // 927e: invokevirtual net/minecraft/client/model/geom/builders/CubeListBuilder.addBox (FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;
      // 9281: ldc_w -27.7027
      // 9284: fconst_0
      // 9285: fconst_0
      // 9286: invokestatic net/minecraft/client/model/geom/PartPose.offset (FFF)Lnet/minecraft/client/model/geom/PartPose;
      // 9289: invokevirtual net/minecraft/client/model/geom/builders/PartDefinition.addOrReplaceChild (Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;
      // 928c: astore 68
      // 928e: aload 0
      // 928f: sipush 512
      // 9292: sipush 512
      // 9295: invokestatic net/minecraft/client/model/geom/builders/LayerDefinition.create (Lnet/minecraft/client/model/geom/builders/MeshDefinition;II)Lnet/minecraft/client/model/geom/builders/LayerDefinition;
      // 9298: areturn
   }

   public ModelPart bodyRoot() {
      return this.Severed;
   }

   private void collectChains() {
      if (this.chains.isEmpty()) {
         for (ModelPart limb : children(this.Severed).values()) {
            List<ModelPart> run = new ArrayList<>();
            ModelPart cur = limb;

            while (true) {
               run.add(cur);
               Collection<ModelPart> kids = children(cur).values();
               if (kids.size() != 1) {
                  if (run.size() >= 2) {
                     this.chains.add(run);
                  }
                  break;
               }

               cur = kids.iterator().next();
            }
         }
      }
   }

   private static Map<String, ModelPart> children(ModelPart part) {
      return ((ModelPartAccessor)part).getChildren();
   }

   public void setupAnim(SeveredWitherStormRenderState state) {
      this.Severed.getAllParts().forEach(ModelPart::resetPose);
      this.collectChains();

      for (int c = 0; c < this.chains.size(); c++) {
         WitherStormTentacles5.smallIdle(this.chains.get(c), state.idleTimeTicks, c, 1.0F - state.droop);
         WitherStormTentacles5.limp(this.chains.get(c), state.droop, state.idleTimeTicks, c, state.groundBias[c % state.groundBias.length]);
      }
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.Severed.render(poseStack, buffer, packedLight, packedOverlay);
   }
}
