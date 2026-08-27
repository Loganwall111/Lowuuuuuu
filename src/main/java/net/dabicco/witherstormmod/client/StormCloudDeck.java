package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormCloudDeck — Cloud deck renderer.
 * Clouds are handled via authentic Minecraft: Story Mode shader clouds and resource packs.
 */
public final class StormCloudDeck {
   private static final ResourceLocation SLAB = ResourceLocation.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/mcsm_cloud.png");
   private static final double MAX_VIEW_DIST = 900.0;
   private static final int FULL_BRIGHT = 0xF000F0;

   private StormCloudDeck() {
   }

   public static void submit(LevelRenderContext ctx) {
      // Cloud deck disabled: clouds are handled authentically via MCSM shaders and resource packs
      int mode = (int)Math.round(DabyWSClientConfig.stormCloudDeck);
      if (mode <= 0) {
         return;
      }
   }

   private static float hash01(long id, int salt, int index) {
      long h = id * 6364136223846793005L + salt * 1442695040888963407L + index * 999999937L;
      h ^= h >>> 13;
      h *= 1274126177;
      h ^= h >>> 16;
      return (float)(h & 0xFFFF) / 65536.0F;
   }

   private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
