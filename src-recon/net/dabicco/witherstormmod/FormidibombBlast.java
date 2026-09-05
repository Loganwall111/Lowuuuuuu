package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.dabicco.witherstormmod.ModSounds;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FormidibombBlast {
   private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/tractor_beam.png");
   private static final int SEGMENTS = 40;
   private static final float LIFE_TICKS = 90.0F;
   private static final double MAX_RADIUS = 150.0;
   private static final List<net.dabicco.witherstormmod.client.FormidibombBlast.Blast> ACTIVE = new ArrayList<>();

   private FormidibombBlast() {
   }

   public static void trigger(double x, double y, double z) {
      ACTIVE.add(new net.dabicco.witherstormmod.client.FormidibombBlast.Blast(new Vec3(x, y, z), System.currentTimeMillis()));
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         double dist = Math.sqrt(mc.player.distanceToSqr(x, y, z));
         if (dist > 260.0 && dist <= 2000.0) {
            float vol = (float)(1.0 - 0.55 * Mth.clamp((dist - 260.0) / 1740.0, 0.0, 1.0));
            Vec3 me = mc.player.position();
            Vec3 dir = new Vec3(x - me.x, y - me.y, z - me.z).normalize();
            Vec3 src = me.add(dir.scale(6.0));
            mc.level.playLocalSound(src.x, src.y, src.z, ModSounds.FORMIDIBOMB_DISTANT_EXPLOSION, SoundSource.BLOCKS, vol, 1.0F, false);
         }
      }
   }

   public static void clear() {
      ACTIVE.clear();
   }

   public static void render(LevelRenderContext ctx) {
      if (!ACTIVE.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack pose = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            long now = System.currentTimeMillis();
            Iterator<net.dabicco.witherstormmod.client.FormidibombBlast.Blast> it = ACTIVE.iterator();

            while (it.hasNext()) {
               net.dabicco.witherstormmod.client.FormidibombBlast.Blast b = it.next();
               float ticks = (float)(now - b.startMs) / 50.0F;
               if (ticks > 90.0F) {
                  it.remove();
               } else {
                  double dist = b.pos.distanceTo(cam);
                  if (!(dist > 2000.0)) {
                     float life = 1.0F - ticks / 90.0F;
                     float distFade = 1.0F - 0.35F * (float)Mth.clamp(dist / 2000.0, 0.0, 1.0);
                     Vec3 rel = b.pos.subtract(cam);
                     Vec3 view = rel.normalize();
                     float ballGrow = Mth.clamp(ticks / 10.0F, 0.0F, 1.0F);
                     ballGrow *= 2.0F - ballGrow;
                     float ballFade = Mth.clamp(1.0F - ticks / 67.5F, 0.0F, 1.0F);
                     ballFade *= ballFade;
                     float ba = ballFade * distFade;
                     double br = 82.5 * ballGrow;
                     if (ba > 0.01F) {
                        fan(pose, collector, rel, view, br * 1.55, 255, 120, 40, (int)(150.0F * ba));
                        fan(pose, collector, rel, view, br * 1.05, 255, 190, 90, (int)(215.0F * ba));
                        fan(pose, collector, rel, view, br * 0.62, 255, 245, 200, (int)(255.0F * ba));
                        fan(pose, collector, rel, view, br * 0.3, 255, 255, 255, (int)(255.0F * ba));
                     }

                     float wave = Mth.clamp(ticks / 72.0F, 0.0F, 1.0F);
                     float waveOut = 1.0F - (1.0F - wave) * (1.0F - wave);
                     double wr = 285.0 * waveOut;
                     float wa = (1.0F - wave) * (1.0F - wave) * distFade;
                     if (wa > 0.01F && wr > 1.0) {
                        double thick = 150.0 * (0.28 - 0.18 * waveOut);
                        ring(pose, collector, rel, view, wr, thick, 255, 235, 190, (int)(200.0F * wa));
                        ring(pose, collector, rel, view, wr * 0.93, thick * 0.6, 255, 255, 255, (int)(150.0F * wa));
                     }
                  }
               }
            }
         }
      }
   }

   private static void fan(PoseStack pose, SubmitNodeCollector collector, Vec3 centre, Vec3 viewDir, double radius, int r, int g, int b, int centreAlpha) {
      if (centreAlpha > 2 && !(radius <= 0.01)) {
         Vec3 up = Math.abs(viewDir.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
         Vec3 right = viewDir.cross(up).normalize();
         Vec3 upB = right.cross(viewDir).normalize();
         Vec3[] rim = new Vec3[41];

         for (int i = 0; i <= 40; i++) {
            double ang = (Math.PI * 2) * i / 40.0;
            rim[i] = centre.add(right.scale(Math.cos(ang) * radius)).add(upB.scale(Math.sin(ang) * radius));
         }

         collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (p, consumer) -> {
            for (int ix = 0; ix < 40; ix++) {
               vertex(p, consumer, centre, r, g, b, centreAlpha);
               vertex(p, consumer, centre, r, g, b, centreAlpha);
               vertex(p, consumer, rim[ix], r, g, b, 0);
               vertex(p, consumer, rim[ix + 1], r, g, b, 0);
            }
         });
      }
   }

   private static void ring(
      PoseStack pose, SubmitNodeCollector collector, Vec3 centre, Vec3 viewDir, double radius, double thickness, int r, int g, int b, int alpha
   ) {
      if (alpha > 2 && !(radius <= 0.01) && !(thickness <= 0.001)) {
         Vec3 up = Math.abs(viewDir.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
         Vec3 right = viewDir.cross(up).normalize();
         Vec3 upB = right.cross(viewDir).normalize();
         double rIn = Math.max(0.0, radius - thickness * 0.5);
         double rMid = radius;
         double rOut = radius + thickness * 0.5;
         Vec3[] in = new Vec3[41];
         Vec3[] mid = new Vec3[41];
         Vec3[] out = new Vec3[41];

         for (int i = 0; i <= 40; i++) {
            double ang = (Math.PI * 2) * i / 40.0;
            double c = Math.cos(ang);
            double s = Math.sin(ang);
            in[i] = centre.add(right.scale(c * rIn)).add(upB.scale(s * rIn));
            mid[i] = centre.add(right.scale(c * rMid)).add(upB.scale(s * rMid));
            out[i] = centre.add(right.scale(c * rOut)).add(upB.scale(s * rOut));
         }

         collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (p, consumer) -> {
            for (int ix = 0; ix < 40; ix++) {
               vertex(p, consumer, in[ix], r, g, b, 0);
               vertex(p, consumer, in[ix + 1], r, g, b, 0);
               vertex(p, consumer, mid[ix + 1], r, g, b, alpha);
               vertex(p, consumer, mid[ix], r, g, b, alpha);
               vertex(p, consumer, mid[ix], r, g, b, alpha);
               vertex(p, consumer, mid[ix + 1], r, g, b, alpha);
               vertex(p, consumer, out[ix + 1], r, g, b, 0);
               vertex(p, consumer, out[ix], r, g, b, 0);
            }
         });
      }
   }

   private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at, int r, int g, int b, int alpha) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(r, g, b, alpha);
   }

   private static final class Blast {
      final Vec3 pos;
      final long startMs;

      Blast(Vec3 pos, long startMs) {
         this.pos = pos;
         this.startMs = startMs;
      }
   }
}
