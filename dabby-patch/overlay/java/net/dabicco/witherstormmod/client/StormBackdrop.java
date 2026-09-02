package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormBackdrop — the gradient sky that hangs BEHIND the Wither Storm.
 *
 * Not a halo, not a ring. In MCSM the storm drags its own piece of sky around
 * with it: a huge soft gradient blob sitting *behind* the body, dark in the
 * middle and fading to nothing at the rim so it melts into the real skybox.
 * The ordinary sky keeps its own colour; only the patch behind the storm is
 * recoloured, and it follows the storm as it moves.
 *
 * Phase progression (matches the reference images):
 *
 *   < 3.9        nothing at all.
 *   4.0 -> 4.5   the blue phase-4 glow (the user's own reference art).
 *   4.5 -> 5.1   dark turquoise/green haze with a black blur in the centre.
 *   5.1 -> 5.5   swings to the purple sky.
 *   5.5 +        purple wraps outward into magenta/pink, and grows.
 *
 * The plane sits a fixed distance beyond the storm along the camera->storm
 * axis and always faces the camera, so it reads as sky rather than as a disc
 * bolted to the model. Submitted before the rest of the storm FX so the body,
 * the pulse and the glare all draw on top of it.
 */
public final class StormBackdrop {
   private static final String NS = "dabywitherstormmod";
   private static final Identifier BLUE4 = Identifier.fromNamespaceAndPath(NS, "textures/misc/backdrop_phase4_blue.png");
   private static final Identifier BLACK = Identifier.fromNamespaceAndPath(NS, "textures/misc/backdrop_black.png");
   private static final Identifier TURQUOISE = Identifier.fromNamespaceAndPath(NS, "textures/misc/backdrop_turquoise.png");
   private static final Identifier PURPLE = Identifier.fromNamespaceAndPath(NS, "textures/misc/backdrop_purple.png");
   private static final Identifier PURPLE_PINK = Identifier.fromNamespaceAndPath(NS, "textures/misc/backdrop_purple_pink.png");
   private static final Identifier EMBER = Identifier.fromNamespaceAndPath(NS, "textures/misc/backdrop_ember.png");

   private static final int FULL_BRIGHT = 15728880;

   private StormBackdrop() {
   }

   /** 0 below lo, 1 above hi, smooth in between. */
   private static float ramp(float v, float lo, float hi) {
      if (hi <= lo) {
         return v >= hi ? 1.0F : 0.0F;
      }
      float t = Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   /** Same body-size mapping StormPresenceFX uses, so we scale with the storm. */
   /** Fixed distance the backdrop is drawn at, so it behaves like sky. */
   private static final double SKY_DISTANCE = 220.0;

   private static double bodyRadius(float phase) {
      if (phase < 4.0F) {
         return (double)(4.0F + 1.5F * phase);
      } else if (phase < 5.0F) {
         return (double)(10.0F + 8.0F * (phase - 4.0F));
      } else {
         return phase < 6.0F ? (double)(18.0F + 22.0F * (phase - 5.0F)) : (double)(40.0F + 30.0F * (phase - 6.0F));
      }
   }

   public static void submit(LevelRenderContext ctx) {
      /* The dome (StormSkyDome) is the real dynamic sky now. This quad is kept
       * only as an optional extra and ships OFF: at any size it reads as a
       * curtain sliding past the camera, which is not what a sky does. */
      if (!DabyWSClientConfig.stormBackdropQuad) {
         return;
      }

      if (!DabyWSClientConfig.stormBackdrop) {
         return;
      }
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || ClientDistantStormManager.all().isEmpty()) {
         return;
      }

      float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float nowSec = gt * 0.05F;
      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();

      float master = (float)DabyWSClientConfig.stormBackdropStrength;
      if (master <= 0.004F) {
         return;
      }

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         float phase = d.phase;
         if (phase < 3.9F) {
            continue;
         }

         Vec3 centre = new Vec3(d.dispX, d.dispY, d.dispZ);
         Vec3 toStorm = centre.subtract(cam);
         double dist = toStorm.length();
         if (dist < 1.0E-4) {
            continue;
         }
         Vec3 view = toStorm.scale(1.0 / dist);

         float distFade = 1.0F - Mth.clamp((float)((dist - 1200.0) / 900.0), 0.0F, 1.0F);
         if (distFade <= 0.004F) {
            continue;
         }

         double bodyR = bodyRadius(phase);
         /* A real dynamic skybox, not a decal stuck behind the creature.
          *
          * The plane is anchored to the CAMERA at a fixed sky distance along the
          * direction of the storm, and its radius is a fraction of that same
          * distance. That gives it a constant angular size: it does not shrink or
          * parallax as the storm moves, exactly like the vanilla sky dome, while
          * still swinging around to stay behind the storm. Terrain still occludes
          * it because the pipeline depth-tests, so it sits behind the world. */
         double skyDist = SKY_DISTANCE;
         Vec3 at = cam.add(view.scale(skyDist));

         // angular size grows with the storm, but is expressed against sky distance
         /* Keep the backdrop a CONTAINED blob centred on the storm, not a wall.
          *
          * The old floor of 0.10 pinned it at ~29 degrees on screen no matter
          * how far away the storm was, which at distance made it 4-6x wider
          * than the creature: it filled half the view and slid past the camera
          * like a curtain. Tying it to the storm's true angular size (with a
          * small floor so it never vanishes) keeps it behind the body. */
         double angular = Mth.clamp(bodyR / Math.max(dist, 1.0), 0.012, 0.85);
         double baseR = skyDist * angular * 1.5 * (double)(float)DabyWSClientConfig.stormBackdropSize;
         if (DabyWSClientConfig.stormBackdropGrow && phase > 5.5F) {
            baseR *= (double)(1.0F + (phase - 5.5F) * 0.26F);
         }
         float breathe = 1.0F + 0.03F * Mth.sin(nowSec * 0.045F * (float)DabyWSClientConfig.stormBackdropPulse);
         baseR *= (double)breathe;

         /* ---- phase weights ---- */
         // the blue phase-4 glow, gone by the time turquoise is established
         float wBlue = ramp(phase, 3.95F, 4.20F) * (1.0F - ramp(phase, 4.60F, 5.00F));
         // turquoise/green owns 4.5 all the way through the end of phase 5
         float wTurq = ramp(phase, 4.45F, 4.90F) * (1.0F - ramp(phase, 6.00F, 6.35F));
         // purple only starts once phase 5 is over
         float wPurp = ramp(phase, 6.00F, 6.35F);
         // then it keeps getting pinker/violet as it grows
         float wPink = ramp(phase, 6.30F, 7.00F);
         // black core only once we are past the blue stage
         float wBlack = ramp(phase, 4.45F, 4.85F);

         float a = master * distFade;

         /* ---- back to front ---- */
         if (wPink > 0.004F && DabyWSClientConfig.stormBackdropPink) {
            quad(poseStack, collector, GlowRenderTypes.translucent(PURPLE_PINK), at, view,
                 baseR * 1.55, 255, 255, 255, (int)(a * wPink * 245.0F));
         }
         if (wPurp > 0.004F && DabyWSClientConfig.stormBackdropPurple) {
            quad(poseStack, collector, GlowRenderTypes.translucent(PURPLE), at, view,
                 baseR * 1.18, 255, 255, 255, (int)(a * wPurp * 250.0F));
         }
         if (wTurq > 0.004F && DabyWSClientConfig.stormBackdropTurquoise) {
            quad(poseStack, collector, GlowRenderTypes.translucent(TURQUOISE), at, view,
                 baseR * 1.10, 255, 255, 255, (int)(a * wTurq * 250.0F));
         }
         if (DabyWSClientConfig.stormBackdropEmber && phase >= 6.0F) {
            quad(poseStack, collector, GlowRenderTypes.translucent(EMBER), at, view,
                 baseR * 1.34, 255, 255, 255,
                 (int)(a * 90.0F * (float)DabyWSClientConfig.stormBackdropEmberStrength));
         }
         if (wBlack > 0.004F && DabyWSClientConfig.stormBackdropBlack) {
            quad(poseStack, collector, GlowRenderTypes.translucent(BLACK), at, view,
                 baseR * 0.82, 255, 255, 255,
                 (int)(a * wBlack * 250.0F * (float)DabyWSClientConfig.stormBackdropBlackStrength));
         }
         // Phase-4 blue sits on top: it is a glow, not a dark backdrop, so it
         // uses the additive glow type rather than plain translucency.
         if (wBlue > 0.004F && DabyWSClientConfig.stormBackdropPhase4) {
            quad(poseStack, collector, GlowRenderTypes.glow(BLUE4), at, view,
                 baseR * 0.95, 255, 255, 255,
                 (int)(a * wBlue * 235.0F * (float)DabyWSClientConfig.stormBackdropPhase4Strength));
         }
      }
   }

   /** Camera-facing textured quad, slightly wider than tall. */
   private static void quad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                            Vec3 at, Vec3 view, double radius, int r, int g, int b, int alpha) {
      if (alpha <= 2) {
         return;
      }
      Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = view.cross(upHint).normalize();
      Vec3 up = right.cross(view).normalize();
      Vec3 rx = right.scale(radius * 1.15);
      Vec3 uy = up.scale(radius);
      final int fa = Math.min(alpha, 255);
      collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
         vertex(pose, consumer, at.subtract(rx).subtract(uy), 0.0F, 1.0F, r, g, b, fa);
         vertex(pose, consumer, at.add(rx).subtract(uy), 1.0F, 1.0F, r, g, b, fa);
         vertex(pose, consumer, at.add(rx).add(uy), 1.0F, 0.0F, r, g, b, fa);
         vertex(pose, consumer, at.subtract(rx).add(uy), 0.0F, 0.0F, r, g, b, fa);
      });
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, float u, float v,
                              int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z)
              .setColor(r, g, b, a)
              .setUv(u, v)
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(FULL_BRIGHT)
              .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
