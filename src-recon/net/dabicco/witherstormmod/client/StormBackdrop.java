package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormBackdrop {
   private static final String NS = "dabywitherstormmod";
   private static final Identifier BLUE4 = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_phase4_blue.png");
   private static final Identifier BLACK = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_black.png");
   private static final Identifier TURQUOISE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_turquoise.png");
   private static final Identifier PURPLE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_purple.png");
   private static final Identifier PURPLE_PINK = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_purple_pink.png");
   private static final Identifier EMBER = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_ember.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final double SKY_DISTANCE = 220.0;

   private StormBackdrop() {
   }

   private static float ramp(float v, float lo, float hi) {
      if (hi <= lo) {
         return v >= hi ? 1.0F : 0.0F;
      } else {
         float t = Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F);
         return t * t * (3.0F - 2.0F * t);
      }
   }

   private static double bodyRadius(float phase) {
      if (phase < 4.0F) {
         return 4.0F + 1.5F * phase;
      } else if (phase < 5.0F) {
         return 10.0F + 8.0F * (phase - 4.0F);
      } else {
         return phase < 6.0F ? 18.0F + 22.0F * (phase - 5.0F) : 40.0F + 30.0F * (phase - 6.0F);
      }
   }

   public static void submit(LevelRenderContext ctx) {
      if (DabyWSClientConfig.stormBackdropQuad) {
         if (DabyWSClientConfig.stormBackdrop) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && !net.dabicco.witherstormmod.client.ClientDistantStormManager.all().isEmpty()) {
               float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
               float nowSec = gt * 0.05F;
               Vec3 cam = ctx.levelState().cameraRenderState.pos;
               PoseStack poseStack = ctx.poseStack();
               SubmitNodeCollector collector = ctx.submitNodeCollector();
               float master = (float)DabyWSClientConfig.stormBackdropStrength;
               if (!(master <= 0.004F)) {
                  for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
                     float phase = d.phase;
                     if (!(phase < 3.9F)) {
                        Vec3 centre = new Vec3(d.dispX, d.dispY, d.dispZ);
                        Vec3 toStorm = centre.subtract(cam);
                        double dist = toStorm.length();
                        if (!(dist < 1.0E-4)) {
                           Vec3 view = toStorm.scale(1.0 / dist);
                           float distFade = 1.0F - Mth.clamp((float)((dist - 1200.0) / 900.0), 0.0F, 1.0F);
                           if (!(distFade <= 0.004F)) {
                              double bodyR = bodyRadius(phase);
                              double skyDist = 220.0;
                              Vec3 at = cam.add(view.scale(skyDist));
                              double angular = Mth.clamp(bodyR / Math.max(dist, 1.0), 0.012, 0.85);
                              double baseR = skyDist * angular * 1.5 * (float)DabyWSClientConfig.stormBackdropSize;
                              if (DabyWSClientConfig.stormBackdropGrow && phase > 5.5F) {
                                 baseR *= 1.0F + (phase - 5.5F) * 0.26F;
                              }

                              float breathe = 1.0F + 0.03F * Mth.sin(nowSec * 0.045F * (float)DabyWSClientConfig.stormBackdropPulse);
                              baseR *= breathe;
                              float wBlue = ramp(phase, 3.95F, 4.2F) * (1.0F - ramp(phase, 4.6F, 5.0F));
                              float wTurq = ramp(phase, 4.45F, 4.9F) * (1.0F - ramp(phase, 6.0F, 6.35F));
                              float wPurp = ramp(phase, 6.0F, 6.35F);
                              float wPink = ramp(phase, 6.3F, 7.0F);
                              float wBlack = ramp(phase, 4.45F, 4.85F);
                              float a = master * distFade;
                              if (wPink > 0.004F && DabyWSClientConfig.stormBackdropPink) {
                                 quad(
                                    poseStack,
                                    collector,
                                    net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(PURPLE_PINK),
                                    at,
                                    view,
                                    baseR * 1.55,
                                    255,
                                    255,
                                    255,
                                    (int)(a * wPink * 245.0F)
                                 );
                              }

                              if (wPurp > 0.004F && DabyWSClientConfig.stormBackdropPurple) {
                                 quad(
                                    poseStack,
                                    collector,
                                    net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(PURPLE),
                                    at,
                                    view,
                                    baseR * 1.18,
                                    255,
                                    255,
                                    255,
                                    (int)(a * wPurp * 250.0F)
                                 );
                              }

                              if (wTurq > 0.004F && DabyWSClientConfig.stormBackdropTurquoise) {
                                 quad(
                                    poseStack,
                                    collector,
                                    net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(TURQUOISE),
                                    at,
                                    view,
                                    baseR * 1.1,
                                    255,
                                    255,
                                    255,
                                    (int)(a * wTurq * 250.0F)
                                 );
                              }

                              if (DabyWSClientConfig.stormBackdropEmber && phase >= 6.0F) {
                                 quad(
                                    poseStack,
                                    collector,
                                    net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(EMBER),
                                    at,
                                    view,
                                    baseR * 1.34,
                                    255,
                                    255,
                                    255,
                                    (int)(a * 90.0F * (float)DabyWSClientConfig.stormBackdropEmberStrength)
                                 );
                              }

                              if (wBlack > 0.004F && DabyWSClientConfig.stormBackdropBlack) {
                                 quad(
                                    poseStack,
                                    collector,
                                    net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(BLACK),
                                    at,
                                    view,
                                    baseR * 0.82,
                                    255,
                                    255,
                                    255,
                                    (int)(a * wBlack * 250.0F * (float)DabyWSClientConfig.stormBackdropBlackStrength)
                                 );
                              }

                              if (wBlue > 0.004F && DabyWSClientConfig.stormBackdropPhase4) {
                                 quad(
                                    poseStack,
                                    collector,
                                    net.dabicco.witherstormmod.client.GlowRenderTypes.glow(BLUE4),
                                    at,
                                    view,
                                    baseR * 0.95,
                                    255,
                                    255,
                                    255,
                                    (int)(a * wBlue * 235.0F * (float)DabyWSClientConfig.stormBackdropPhase4Strength)
                                 );
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void quad(
      PoseStack poseStack, SubmitNodeCollector collector, RenderType type, Vec3 at, Vec3 view, double radius, int r, int g, int b, int alpha
   ) {
      if (alpha > 2) {
         Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
         Vec3 right = view.cross(upHint).normalize();
         Vec3 up = right.cross(view).normalize();
         Vec3 rx = right.scale(radius * 1.15);
         Vec3 uy = up.scale(radius);
         int fa = Math.min(alpha, 255);
         collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            vertex(pose, consumer, at.subtract(rx).subtract(uy), 0.0F, 1.0F, r, g, b, fa);
            vertex(pose, consumer, at.add(rx).subtract(uy), 1.0F, 1.0F, r, g, b, fa);
            vertex(pose, consumer, at.add(rx).add(uy), 1.0F, 0.0F, r, g, b, fa);
            vertex(pose, consumer, at.subtract(rx).add(uy), 0.0F, 0.0F, r, g, b, fa);
         });
      }
   }

   private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z)
         .setColor(r, g, b, a)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
