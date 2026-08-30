package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.mixin.GameRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

/**
 * StormAtmosphere — true full-screen cinematic post-processing for the storm.
 *
 * Loads {@code post_effect/storm_atmosphere.json} (a modern post-chain that
 * runs {@code shaders/post/storm_atmosphere.fsh}) and applies it every frame a
 * storm is present. The pass re-grades the finished frame with a smooth
 * purple-to-dark-magenta atmospheric fog that hugs the horizon behind the
 * Wither Storm — a post-effect, never a physical material block or sphere
 * wrapped around the model.
 */
public final class StormAtmosphere {
   private static final Identifier CHAIN = Identifier.fromNamespaceAndPath("dabywitherstormmod", "post_effect/storm_atmosphere");
   private static boolean failed;
   private static String lastStatus = "";

   private StormAtmosphere() {
   }

   public static void process() {
      if (failed || !DabyWSClientConfig.stormAtmosphere || ShaderPackCompat.active()) {
         return;
      }
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null || ClientDistantStormManager.all().isEmpty()) {
         return;
      }
      try {
         PostChain chain = mc.getShaderManager().getPostChain(CHAIN, LevelTargetBundle.MAIN_TARGETS);
         if (chain == null) {
            fail("chain failed to load");
            return;
         }
         chain.process(mc.gameRenderer.mainRenderTarget(), ((GameRendererAccessor)mc.gameRenderer).dabyws$resourcePool());
         status("running storm_atmosphere post chain");
      } catch (Exception e) {
         fail("error: " + e);
      }
   }

   private static void fail(String why) {
      failed = true;
      System.out.println("[dabywitherstormmod] storm atmosphere DISABLED (" + why + ")");
   }

   private static void status(String s) {
      if (!s.equals(lastStatus)) {
         lastStatus = s;
         System.out.println("[dabywitherstormmod] storm atmosphere: " + s);
      }
   }
}
