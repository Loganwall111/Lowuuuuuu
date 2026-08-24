package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.dabicco.devouringstorms.ModSounds;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * One-shot pulse and shockwave VFX triggered by server state, distinct from the
 * permanent halo attached to the main storm renderer.
 */
public final class StormPulseFX {
   private static final int KIND_COMMAND = 0;
   private static final int KIND_SUMMON = 1;
   private static final int KIND_SPLIT = 2;
   private static final float BASE_LIFE_TICKS = 76.0F;
   private static final int SEGMENTS = 40;
   private static final List<Pulse> ACTIVE = new ArrayList();
   private static final float[] LAYER_SIZES = new float[]{0.9F, 1.25F, 1.7F};
   private static final float[] LAYER_ALPHAS = new float[]{0.58F, 0.28F, 0.12F};

   private StormPulseFX() {
   }

   public static void trigger(int entityId, double x, double y, double z, float phase, int kind) {
      if (kind == KIND_SUMMON && !DevouringStormsClientConfig.summonShockwave) {
         return;
      }

      ACTIVE.add(new Pulse(entityId, new Vec3(x, y, z), phase, kind, System.currentTimeMillis()));
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         double range = kind == KIND_SPLIT
            ? Math.max(26.0, DevouringStormsClientConfig.pulseHeartbeatRange * 1.35)
            : kind == KIND_SUMMON ? Math.max(24.0, DevouringStormsClientConfig.pulseHeartbeatRange * 1.2) : Math.max(16.0, DevouringStormsClientConfig.pulseHeartbeatRange);
         double dist = mc.player.position().distanceTo(new Vec3(x, y, z));
         float vol = (float)(1.0 - 0.82 * Mth.clamp(dist / range, 0.0, 1.0));
         if (vol > 0.03F) {
            float pulseVol = kind == KIND_SPLIT ? vol * 1.18F : kind == KIND_SUMMON ? vol * 1.15F : vol;
            mc.getSoundManager().play(new StormPulseSound(x, y, z, pulseVol));
            if (kind == KIND_SUMMON) {
               mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), ModSounds.STORM_THUMP_LARGE, SoundSource.HOSTILE, vol * 1.25F, 0.58F, false);
            } else if (kind == KIND_SPLIT) {
               mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), ModSounds.STORM_THUMP_LARGE, SoundSource.HOSTILE, vol * 1.35F, 0.56F, false);
            } else if (DevouringStormsClientConfig.pulseHeartbeat) {
               mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), ModSounds.STORM_THUMP_LARGE, SoundSource.HOSTILE, vol * (float)DevouringStormsClientConfig.pulseHeartbeatVolume, 0.62F, false);
            }
         }
      }
   }

   public static void clear() {
      ACTIVE.clear();
   }

   public static void render(LevelRenderContext ctx) {
      if (ACTIVE.isEmpty()) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return;
      }

      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack pose = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();
      long now = System.currentTimeMillis();
      Iterator<Pulse> it = ACTIVE.iterator();

      while (it.hasNext()) {
         Pulse pulse = it.next();
         boolean summon = pulse.kind() == KIND_SUMMON;
         boolean split = pulse.kind() == KIND_SPLIT;
         if ((!summon && !split && !DevouringStormsClientConfig.atmospherePulse) || (summon && !DevouringStormsClientConfig.summonShockwave)) {
            continue;
         }

         float lifeTicks = BASE_LIFE_TICKS * (summon ? 1.18F : split ? 0.72F : Mth.clamp((float)DevouringStormsClientConfig.pulsePeriod / 4.0F, 0.55F, 2.0F));
         float ticks = (float)(now - pulse.startMs()) / 50.0F;
         if (ticks > lifeTicks) {
            it.remove();
            continue;
         }

         Vec3 rel = pulse.pos().subtract(cam);
         double dist = rel.length();
         if (dist > 2500.0 || dist < 1.0E-4) {
            continue;
         }

         Vec3 view = rel.scale(1.0 / dist);
         float growIn = split ? Math.max(6.0F, lifeTicks * 0.16F) : Math.max(summon ? 10.0F : 14.0F, lifeTicks * (summon ? 0.18F : 0.24F));
         float fadeStart = split ? Math.max(growIn + 1.0F, lifeTicks * 0.18F) : Math.max(growIn + 2.0F, lifeTicks * (summon ? 0.30F : 0.22F));
         float grow = Mth.clamp(ticks / growIn, 0.0F, 1.0F);
         grow = grow * grow * (3.0F - 2.0F * grow);
         float fade = 1.0F - Mth.clamp((ticks - fadeStart) / Math.max(1.0F, lifeTicks - fadeStart), 0.0F, 1.0F);
         fade *= fade;
         float amount = (split ? Math.max(0.85F, (float)DevouringStormsClientConfig.pulseStrength) * 1.18F : summon ? (float)DevouringStormsClientConfig.summonShockwaveStrength : (float)DevouringStormsClientConfig.pulseStrength) * fade;
         if (amount <= 0.004F) {
            continue;
         }

         float[] col = split ? new float[]{0.72F, 0.30F, 1.0F} : StormPalettes.pulseColor(pulse.phase(), new float[3]);
         int[][] layerCols = new int[LAYER_SIZES.length][3];
         for (int i = 0; i < LAYER_SIZES.length; i++) {
            float mix = split ? (i == 0 ? 1.0F : i == 1 ? 0.88F : 0.72F) : 1.0F;
            layerCols[i][0] = (int)(Mth.clamp(col[0] * mix, 0.0F, 1.0F) * 255.0F);
            layerCols[i][1] = (int)(Mth.clamp(col[1] * mix, 0.0F, 1.0F) * 255.0F);
            layerCols[i][2] = (int)(Mth.clamp(col[2], 0.0F, 1.0F) * 255.0F);
         }

         double bodyR = split ? 18.0 + Math.max(0.0F, pulse.phase() - 6.0F) * 4.0F : (summon ? 32.0 : 24.0) + Math.max(0.0F, pulse.phase() - 5.0F) * (summon ? 12.0 : 10.0);
         double size = split ? Math.max(0.65, DevouringStormsClientConfig.pulseSize * 0.78) : summon ? DevouringStormsClientConfig.summonShockwaveSize : DevouringStormsClientConfig.pulseSize;
         double radius = bodyR * (split ? 0.72 + 1.08 * grow : summon ? 1.85 + 2.35 * grow : 1.4 + 1.7 * grow) * size;
         Vec3 centre = rel.add(0.0, bodyR * (split ? 0.08 : summon ? 0.12 : 0.15), 0.0);
         StormGlowRenderer.submitLight(pose, collector, centre, view, radius, LAYER_SIZES, LAYER_ALPHAS, layerCols, split ? amount * 1.24F : summon ? amount * 1.08F : amount);

         int alpha = (int)((split ? 220.0F : summon ? 210.0F : 180.0F) * fade);
         ring(pose, collector, centre, view, radius * (split ? 0.82 : 0.96), radius * (split ? 0.14 : summon ? 0.22 : 0.18), layerCols[0][0], layerCols[0][1], layerCols[0][2], alpha);
         ring(pose, collector, centre, view, radius * (split ? 1.04 : 1.18), radius * (split ? 0.08 : summon ? 0.12 : 0.10), 255, 255, 255, (int)((split ? 165.0F : summon ? 145.0F : 110.0F) * fade));
         if (split) {
            ring(pose, collector, centre, view, radius * 0.68, radius * 0.09, 255, 118, 34, (int)(132.0F * fade));
         }
      }
   }

   private static void ring(PoseStack pose, SubmitNodeCollector collector, Vec3 centre, Vec3 viewDir, double radius, double thickness, int r, int g, int b, int alpha) {
      if (alpha <= 2 || radius <= 0.01 || thickness <= 0.001) {
         return;
      }

      Vec3 up = Math.abs(viewDir.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = viewDir.cross(up).normalize();
      Vec3 upB = right.cross(viewDir).normalize();
      double rIn = Math.max(0.0, radius - thickness * 0.5);
      double rMid = radius;
      double rOut = radius + thickness * 0.5;
      Vec3[] in = new Vec3[SEGMENTS + 1];
      Vec3[] mid = new Vec3[SEGMENTS + 1];
      Vec3[] out = new Vec3[SEGMENTS + 1];

      for (int i = 0; i <= SEGMENTS; ++i) {
         double ang = Math.PI * 2.0 * (double)i / (double)SEGMENTS;
         double c = Math.cos(ang);
         double s = Math.sin(ang);
         in[i] = centre.add(right.scale(c * rIn)).add(upB.scale(s * rIn));
         mid[i] = centre.add(right.scale(c * rMid)).add(upB.scale(s * rMid));
         out[i] = centre.add(right.scale(c * rOut)).add(upB.scale(s * rOut));
      }

      collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (p, consumer) -> {
         for (int i = 0; i < SEGMENTS; ++i) {
            vertex(p, consumer, in[i], r, g, b, 0);
            vertex(p, consumer, in[i + 1], r, g, b, 0);
            vertex(p, consumer, mid[i + 1], r, g, b, alpha);
            vertex(p, consumer, mid[i], r, g, b, alpha);
            vertex(p, consumer, mid[i], r, g, b, alpha);
            vertex(p, consumer, mid[i + 1], r, g, b, alpha);
            vertex(p, consumer, out[i + 1], r, g, b, 0);
            vertex(p, consumer, out[i], r, g, b, 0);
         }
      });
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, int r, int g, int b, int alpha) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(r, g, b, alpha);
   }

   private record Pulse(int entityId, Vec3 pos, float phase, int kind, long startMs) {
   }

   private static final class StormPulseSound extends AbstractTickableSoundInstance {
      private int age;
      private final float baseVolume;

      private StormPulseSound(double x, double y, double z, float baseVolume) {
         super(ModSounds.CB_POWER, SoundSource.HOSTILE, RandomSource.create());
         this.x = x;
         this.y = y;
         this.z = z;
         this.baseVolume = baseVolume;
         this.volume = baseVolume;
         this.pitch = 0.84F;
         this.looping = false;
         this.delay = 0;
         this.attenuation = Attenuation.LINEAR;
         this.relative = false;
      }

      public void tick() {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level == null) {
            this.stop();
            return;
         }

         ++this.age;
         if (this.age > 28) {
            float fade = 1.0F - (float)(this.age - 28) / 26.0F;
            if (fade <= 0.0F) {
               this.volume = 0.0F;
               this.stop();
            } else {
               this.volume = this.baseVolume * fade * fade;
            }
         }
      }
   }
}
