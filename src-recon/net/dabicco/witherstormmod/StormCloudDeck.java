package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormCloudDeck {
   private static final Identifier SLAB = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/mcsm_cloud.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final double MAX_VIEW_DIST = 900.0;

   private StormCloudDeck() {
   }

   private static float hash01(int seed, int i, int slot) {
      int h = seed * 7919 + i * 104729 + slot * 130363;
      h ^= h >>> 13;
      h *= 1274126177;
      h ^= h >>> 16;
      return (h & 65535) / 65536.0F;
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      int mode = (int)Math.round(DabyWSClientConfig.stormCloudDeck);
      if (mode > 0 && mc.level != null && !net.dabicco.witherstormmod.client.ClientDistantStormManager.all().isEmpty()) {
         float coverage = (float)DabyWSClientConfig.stormCloudCoverage;
         if (!(coverage <= 0.05F)) {
            float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float nowSec = gt * 0.05F;
            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float baseAlpha = mode >= 2 ? 0.16F : 0.105F;
            float paletteMix = (float)DabyWSClientConfig.stormCloudPaletteMix;
            collector.submitCustomGeometry(
               poseStack,
               net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(SLAB),
               (pose, consumer) -> {
                  float[] col = new float[3];

                  for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
                     if (!(d.phase < 1.0F)) {
                        double spread = 130.0 + 90.0 * Math.min((double)d.phase, 6.0);
                        int slabs = Mth.clamp((int)(26.0F * coverage * (mode >= 2 ? 1.7F : 1.0F)), 4, 72);
                        net.dabicco.witherstormmod.client.StormPalettes.cloudColor(d.phase, col);
                        float cr = Mth.lerp(paletteMix, (float)DabyWSClientConfig.cloudColorR, col[0]);
                        float cg = Mth.lerp(paletteMix, (float)DabyWSClientConfig.cloudColorG, col[1]);
                        float cb = Mth.lerp(paletteMix, (float)DabyWSClientConfig.cloudColorB, col[2]);
                        float period = (float)Math.max(0.5, DabyWSClientConfig.pulsePeriod);
                        float breathe = 0.5F + 0.5F * Mth.sin((nowSec / period + d.entityId % 977 * 0.6183F) * (float) (Math.PI * 2));
                        float lum = 1.0F + 0.18F * breathe * (float)DabyWSClientConfig.pulseStrength;
                        int r = (int)(Mth.clamp(cr * lum, 0.0F, 1.0F) * 255.0F);
                        int g = (int)(Mth.clamp(cg * lum, 0.0F, 1.0F) * 255.0F);
                        int b = (int)(Mth.clamp(cb * lum, 0.0F, 1.0F) * 255.0F);

                        for (int i = 0; i < slabs; i++) {
                           double rad = (0.45 + 0.8 * hash01(d.entityId, i, 1)) * spread;
                           double ang0 = hash01(d.entityId, i, 2) * Math.PI * 2.0;
                           double drift = (0.004 + 0.012 * hash01(d.entityId, i, 3)) * (hash01(d.entityId, i, 4) < 0.5 ? -1.0 : 1.0);
                           double ang = ang0 + nowSec * drift;
                           double x = d.dispX + Math.cos(ang) * rad;
                           double z = d.dispZ + Math.sin(ang) * rad;
                           double alt = (hash01(d.entityId, i, 5) - 0.35) * (50.0 + 30.0 * Math.min((double)d.phase, 6.0))
                              + (float)DabyWSClientConfig.stormCloudAltitude;
                           double y = d.dispY - 20.0 + alt + Math.sin(nowSec * 0.05 * (0.5 + hash01(d.entityId, i, 6)) + hash01(d.entityId, i, 7) * 6.28) * 4.0;
                           double dist = cam.distanceTo(new Vec3(x, y, z));
                           if (!(dist > 900.0)) {
                              float distFade = dist < 60.0 ? (float)(dist / 60.0) : Mth.clamp(1.0F - (float)((dist - 650.0) / 250.0), 0.0F, 1.0F);
                              float alpha = baseAlpha * (0.55F + 0.45F * hash01(d.entityId, i, 8)) * distFade;
                              int a = (int)(alpha * 255.0F);
                              if (a > 2) {
                                 double halfLen = 22.0 + 60.0 * hash01(d.entityId, i, 9);
                                 double halfWid = 9.0 + 22.0 * hash01(d.entityId, i, 10);
                                 double rot = hash01(d.entityId, i, 11) * Math.PI * 2.0 + nowSec * drift * 1.7;
                                 double cosR = Math.cos(rot);
                                 double sinR = Math.sin(rot);
                                 double ux = cosR * halfLen;
                                 double uz = sinR * halfLen;
                                 double vx = -sinR * halfWid;
                                 double vz = cosR * halfWid;
                                 vertex(consumer, pose, x - ux - vx, y, z - uz - vz, 0.0F, 0.0F, r, g, b, a);
                                 vertex(consumer, pose, x + ux - vx, y, z + uz - vz, 1.0F, 0.0F, r, g, b, a);
                                 vertex(consumer, pose, x + ux + vx, y, z + uz + vz, 1.0F, 1.0F, r, g, b, a);
                                 vertex(consumer, pose, x - ux + vx, y, z - uz + vz, 0.0F, 1.0F, r, g, b, a);
                              }
                           }
                        }
                     }
                  }
               }
            );
         }
      }
   }

   private static void vertex(VertexConsumer consumer, Pose pose, double x, double y, double z, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z)
         .setColor(r, g, b, a)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
