package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.mixin.GameRendererAccessor;
import net.dabicco.devouringstorms.mixin.LevelRendererTargetsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormBloom {
   private static final Identifier[][] ENTITY_CHAINS = new Identifier[][]{{id("storm_bloom_entity_subtle_t0"), id("storm_bloom_entity_subtle_t1"), id("storm_bloom_entity_subtle_t2"), id("storm_bloom_entity_subtle_t3"), id("storm_bloom_entity_subtle_t4")}, {id("storm_bloom_entity_t0"), id("storm_bloom_entity_t1"), id("storm_bloom_entity_t2"), id("storm_bloom_entity_t3"), id("storm_bloom_entity_t4")}, {id("storm_bloom_entity_strong_t0"), id("storm_bloom_entity_strong_t1"), id("storm_bloom_entity_strong_t2"), id("storm_bloom_entity_strong_t3"), id("storm_bloom_entity_strong_t4")}};
   private static final float TIGHT_RADIUS_REF = 7.0F;
   private static final float WIDE_RADIUS_REF = 22.0F;
   private static final double REFERENCE_DISTANCE = (double)40.0F;
   private static final float[] EXPOSURE = new float[]{1.8F, 2.8F, 4.0F};
   private static final float[] TIGHT_WEIGHT = new float[]{1.0F, 1.0F, 1.0F};
   private static final float[] WIDE_WEIGHT = new float[]{0.55F, 0.7F, 0.9F};
   private static final double[] TIER_DISTANCES = new double[]{(double)30.0F, (double)60.0F, (double)110.0F, (double)200.0F};
   private static final Identifier[] SCREEN_CHAINS = new Identifier[]{id("storm_bloom_subtle_all"), id("storm_bloom_all"), id("storm_bloom_strong_all")};
   private static boolean failed = false;
   private static boolean drivingOutlineTarget = false;
   private static String lastStatus = "";
   private static boolean depthCaptureFailure;
   private static boolean warnedNoDepth;
   private static String lastSizeLine = "";

   private StormBloom() {
   }

   private static float apparentScale(Minecraft mc) {
      double best = Double.MAX_VALUE;
      Vec3 player = mc.player.position();

      for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         best = Math.min(best, player.distanceToSqr(d.x, d.y, d.z));
      }

      if (best == Double.MAX_VALUE) {
         return 1.0F;
      } else {
         double dist = Math.max(Math.sqrt(best), (double)1.0F);
         return (float)Mth.clamp((double)40.0F / dist, 0.12, 1.6);
      }
   }

   private static int distanceTier(Minecraft mc) {
      double best = Double.MAX_VALUE;
      Vec3 player = mc.player.position();

      for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         best = Math.min(best, player.distanceToSqr(d.x, d.y, d.z));
      }

      if (best == Double.MAX_VALUE) {
         return 0;
      } else {
         double dist = Math.sqrt(best);

         for(int i = 0; i < TIER_DISTANCES.length; ++i) {
            if (dist <= TIER_DISTANCES[i]) {
               return i;
            }
         }

         return TIER_DISTANCES.length;
      }
   }

   public static boolean wantsEntityTarget() {
      return !failed && !ShaderPackCompat.active() && DevouringStormsClientConfig.bloomMaskToStorm && Math.round(DevouringStormsClientConfig.bloomStrength) > 0L;
   }

   public static boolean suppressVanillaOutline() {
      return false;
   }

   public static void beginFrame() {
      StormSceneDepth.beginFrame();
      StormRenderStats.report();
      StormShadowMap.beginFrame();
      if (wantsEntityTarget()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            StormBloomTarget.beginFrame(mc);
         }
      }
   }

   public static void process() {
      if (!failed) {
         if (ShaderPackCompat.active()) {
            drivingOutlineTarget = false;
            status("off (a shader pack is active -- it does its own bloom)");
         } else {
            int level = (int)Math.round(DevouringStormsClientConfig.bloomStrength);
            if (level <= 0) {
               drivingOutlineTarget = false;
               status("off (Bloom is set to Off in Effects)");
            } else {
               Minecraft mc = Minecraft.getInstance();
               if (mc.level != null && mc.player != null) {
                  try {
                     if (DevouringStormsClientConfig.bloomMaskToStorm) {
                        if (!processHeadsOnly(mc, level)) {
                           drivingOutlineTarget = false;
                        }

                        return;
                     }

                     drivingOutlineTarget = false;
                     processWholeScreen(mc, level);
                  } catch (Exception e) {
                     failed = true;
                     drivingOutlineTarget = false;
                     System.out.println("[devouringstorms] storm bloom DISABLED after an error: " + String.valueOf(e));
                     e.printStackTrace();
                  }

               }
            }
         }
      }
   }

   public static RenderTarget sceneTarget(Minecraft mc) {
      return sceneDepthSource(mc);
   }

   private static RenderTarget sceneDepthSource(Minecraft mc) {
      try {
         LevelTargetBundle targets = ((LevelRendererTargetsAccessor)mc.levelRenderer).dabyws$targets();
         if (targets != null && targets.main != null) {
            RenderTarget t = (RenderTarget)targets.main.get();
            if (t != null && t.useDepth && t.getDepthTextureView() != null) {
               return t;
            }
         }
      } catch (Throwable var3) {
      }

      return StormBloomTarget.capturedScene();
   }

   public static void noteDepthCaptureFailure(Throwable t) {
      if (!depthCaptureFailure) {
         depthCaptureFailure = true;
         System.out.println("[devouringstorms] scene depth capture FAILED, bloom occlusion is off (glow will show through geometry): " + String.valueOf(t));
      }
   }

   private static boolean processHeadsOnly(Minecraft mc, int level) {
      RenderTarget heads = StormBloomTarget.target();
      if (heads == null) {
         status("NO BLOOM: the bloom buffer hasn't been created yet");
         return false;
      } else {
         RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
         if (mainTarget == null || heads.width == mainTarget.width && heads.height == mainTarget.height) {
            int debug = (int)Math.round(DevouringStormsClientConfig.bloomDebug);
            StormBloomDiag.tick();
            StormBloomDiag.setEnabled(debug != 0);
            int var10000 = heads.width;
            String sizeLine = var10000 + "x" + heads.height + " / main " + (mainTarget == null ? "null" : mainTarget.width + "x" + mainTarget.height);
            if (debug != 0 && !sizeLine.equals(lastSizeLine)) {
               lastSizeLine = sizeLine;
               System.out.println("[devouringstorms][diag] targets: bloom " + sizeLine);
            }

            StormBloomProbe.probe(heads);
            int lvl = Math.min(level - 1, 2);
            RenderTarget main = mc.gameRenderer.mainRenderTarget();
            float scale = apparentScale(mc) * ((float)main.height / 1080.0F);
            RenderTarget sceneDepth = sceneDepthSource(mc);
            if (sceneDepth == null && !warnedNoDepth) {
               warnedNoDepth = true;
               System.out.println("[devouringstorms] no scene depth available -- bloom occlusion is OFF, so the glow will show through geometry");
            }

            StormBloomHdr.run(heads, sceneDepth != null ? sceneDepth : main, sceneDepth, Math.max(1.0F, 7.0F * scale), Math.max(2.0F, 22.0F * scale), EXPOSURE[lvl], TIGHT_WEIGHT[lvl], WIDE_WEIGHT[lvl], debug);
            drivingOutlineTarget = true;
            status(debug == 0 ? "running HDR bloom (heads only, full-res)" : "DEBUG: showing bloom stage " + debug + " on its own");
            return true;
         } else {
            status("skipping one frame: bloom buffer is " + heads.width + "x" + heads.height + " but the screen is " + mainTarget.width + "x" + mainTarget.height);
            return false;
         }
      }
   }

   private static void processWholeScreen(Minecraft mc, int level) {
      Identifier chainId = SCREEN_CHAINS[Math.min(level - 1, SCREEN_CHAINS.length - 1)];
      PostChain chain = mc.getShaderManager().getPostChain(chainId, LevelTargetBundle.MAIN_TARGETS);
      if (chain == null) {
         status("chain " + String.valueOf(chainId) + " failed to load (see the errors above this line)");
      } else {
         status("running " + String.valueOf(chainId) + " (whole screen)");
         chain.process(mc.gameRenderer.mainRenderTarget(), ((GameRendererAccessor)mc.gameRenderer).dabyws$resourcePool());
      }
   }

   private static void status(String s) {
      if (!s.equals(lastStatus)) {
         lastStatus = s;
         System.out.println("[devouringstorms] storm bloom: " + s);
      }
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("devouringstorms", path);
   }
}
