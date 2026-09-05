package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.Random;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormStarfield {
   private static final Identifier STAR = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/star.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final int BUDGET = 768;
   private static final double DOME_RADIUS = 512.0;
   private static final long SEED = 1337L;
   private static final float[] DX = new float[768];
   private static final float[] DY = new float[768];
   private static final float[] DZ = new float[768];
   private static final float[] SIZE = new float[768];
   private static final float[] BRIGHT = new float[768];
   private static final float[] SPEED = new float[768];
   private static final float[] PHASE = new float[768];
   private static final int[] KIND = new int[768];
   private static boolean seeded;

   private StormStarfield() {
   }

   private static void seed() {
      if (!seeded) {
         seeded = true;
         Random random = new Random(1337L);

         for (int i = 0; i < 768; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double u = random.nextDouble();
            double y = 0.04 + 0.96 * Math.pow(u, 0.65);
            double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
            DX[i] = (float)(Math.cos(theta) * r);
            DY[i] = (float)y;
            DZ[i] = (float)(Math.sin(theta) * r);
            SIZE[i] = 1.1F + random.nextFloat() * random.nextFloat() * 2.6F;
            BRIGHT[i] = 0.35F + random.nextFloat() * 0.65F;
            SPEED[i] = 0.6F + random.nextFloat() * 2.4F;
            PHASE[i] = random.nextFloat() * (float) (Math.PI * 2);
            KIND[i] = random.nextFloat() < 0.3F ? 1 : 0;
         }
      }
   }

   private static float visibility(Minecraft mc) {
      int mode = (int)Math.round(DabyWSClientConfig.stormStars);
      if (mode <= 0 || mc.level == null) {
         return 0.0F;
      } else if (mode >= 2) {
         return net.dabicco.witherstormmod.client.StormGlowRenderer.nightFactor(mc.level);
      } else {
         float darken = net.dabicco.witherstormmod.client.StormSkyDarken.factor();
         float night = 0.35F + 0.65F * net.dabicco.witherstormmod.client.StormGlowRenderer.nightFactor(mc.level);
         return Mth.clamp(darken * 1.25F * night, 0.0F, 1.0F);
      }
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      float vis = visibility(mc);
      if (!(vis <= 0.004F) && mc.level != null) {
         seed();
         Vec3 cam = ctx.levelState().cameraRenderState.pos;
         PoseStack poseStack = ctx.poseStack();
         SubmitNodeCollector collector = ctx.submitNodeCollector();
         float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         float t = gt * 0.05F * Math.max(0.0F, (float)DabyWSClientConfig.starTwinkleSpeed);
         float brightnessScale = (float)DabyWSClientConfig.starBrightness * vis;
         float density = (float)DabyWSClientConfig.starDensity;
         int count = Mth.clamp((int)(768.0F * density), 16, 768);
         float teal = net.dabicco.witherstormmod.client.StormPalettes.strength()
            * Mth.clamp((float)((net.dabicco.witherstormmod.client.StormSkyDarken.palettePhase() - 4.6) / 0.8), 0.0F, 1.0F)
            * 0.6F;
         collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.glow(STAR), (pose, consumer) -> {
            float[] col = new float[3];

            for (int i = 0; i < count; i++) {
               float tw = 0.5F + 0.5F * Mth.sin(t * SPEED[i] + PHASE[i]);
               tw *= tw;
               float alpha = BRIGHT[i] * brightnessScale * (0.25F + 0.75F * tw);
               if (!(alpha <= 0.004F)) {
                  int a = (int)(alpha * 255.0F);
                  if (a > 1) {
                     double px = cam.x + DX[i] * 512.0;
                     double py = cam.y + DY[i] * 512.0;
                     double pz = cam.z + DZ[i] * 512.0;
                     Vec3 view = new Vec3(DX[i], DY[i], DZ[i]).normalize();
                     Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
                     Vec3 right = view.cross(upHint).normalize().scale(SIZE[i]);
                     Vec3 up = right.cross(view).normalize().scale(SIZE[i]);
                     net.dabicco.witherstormmod.client.StormPalettes.starColor(KIND[i], teal, col);
                     int r = (int)(col[0] * 255.0F);
                     int g = (int)(col[1] * 255.0F);
                     int b = (int)(col[2] * 255.0F);
                     vertex(pose, consumer, px - right.x - up.x, py - right.y - up.y, pz - right.z - up.z, 0.0F, 0.0F, r, g, b, a);
                     vertex(pose, consumer, px + right.x - up.x, py + right.y - up.y, pz + right.z - up.z, 1.0F, 0.0F, r, g, b, a);
                     vertex(pose, consumer, px + right.x + up.x, py + right.y + up.y, pz + right.z + up.z, 1.0F, 1.0F, r, g, b, a);
                     vertex(pose, consumer, px - right.x + up.x, py - right.y + up.y, pz - right.z + up.z, 0.0F, 1.0F, r, g, b, a);
                  }
               }
            }
         });
      }
   }

   private static void vertex(Pose pose, VertexConsumer consumer, double x, double y, double z, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z)
         .setColor(r, g, b, a)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
