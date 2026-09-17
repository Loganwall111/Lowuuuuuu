package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public final class WitheredRenderer {
   private static final Identifier BEAM_TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/tractor_beam.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final double VISIBLE_RANGE_SQR = (double)4096.0F;
   private static final int SEGMENTS = 16;

   private WitheredRenderer() {
   }

   public static void render(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         Vec3 cam = ctx.levelState().cameraRenderState.pos;
         PoseStack pose = ctx.poseStack();
         SubmitNodeCollector collector = ctx.submitNodeCollector();
         long now = System.currentTimeMillis();
         float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

         for(Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof ItemEntity) {
               ItemEntity item = (ItemEntity)entity;
               if (item.getItem().is(ModItems.WITHER_FRAGMENT) && !(item.distanceToSqr(cam.x, cam.y, cam.z) > (double)1600.0F)) {
                  Vec3 itemRel = (new Vec3(Mth.lerp((double)partial, item.xOld, item.getX()), Mth.lerp((double)partial, item.yOld, item.getY()) + 0.22, Mth.lerp((double)partial, item.zOld, item.getZ()))).subtract(cam);
                  submitFragmentGlow(pose, collector, itemRel, (float)(mc.level.getGameTime() % 100000L) + partial, item.getId());
               }
            } else if (entity instanceof LivingEntity) {
               LivingEntity mob = (LivingEntity)entity;
               ClientWitheredManager.Cast cast = ClientWitheredManager.get(mob.getId());
               if (cast != null && !(mob.distanceToSqr(cam.x, cam.y, cam.z) > (double)4096.0F)) {
                  Vec3 lerped = new Vec3(Mth.lerp((double)partial, mob.xOld, mob.getX()), Mth.lerp((double)partial, mob.yOld, mob.getY()), Mth.lerp((double)partial, mob.zOld, mob.getZ()));
                  Vec3 rel = lerped.subtract(cam);
                  if (cast.ability == 2) {
                     Vec3 victimRel = null;
                     if (cast.targetId >= 0) {
                        Entity victim = mc.level.getEntity(cast.targetId);
                        if (victim != null) {
                           victimRel = (new Vec3(Mth.lerp((double)partial, victim.xOld, victim.getX()), Mth.lerp((double)partial, victim.yOld, victim.getY()) + (double)victim.getBbHeight() * (double)0.5F, Mth.lerp((double)partial, victim.zOld, victim.getZ()))).subtract(cam);
                        }
                     }

                     submitOrb(pose, collector, rel, mob.getBbHeight(), (float)(mc.level.getGameTime() % 100000L) + partial, cast.abilityTicks(now) + partial, victimRel);
                  }

                  submitCommandBar(pose, collector, ctx, rel, mob.getBbHeight(), cast, now);
               }
            }
         }

      }
   }

   private static void submitCommandBar(PoseStack pose, SubmitNodeCollector collector, LevelRenderContext ctx, Vec3 rel, float height, ClientWitheredManager.Cast cast, long now) {
      int shown = cast.typed(now);
      String typed = cast.command.substring(0, shown);
      boolean caretOn = (now / 400L & 1L) == 0L;
      String caret = cast.stillTyping(now) ? "_" : (caretOn ? "_" : " ");
      MutableComponent line = Component.literal("> ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(typed).withStyle(ChatFormatting.GREEN)).append(Component.literal(caret).withStyle(ChatFormatting.WHITE));
      pose.pushPose();
      pose.translate(rel.x, rel.y, rel.z);
      collector.submitNameTag(pose, new Vec3((double)0.0F, (double)height + 1.05, (double)0.0F), 0, line, true, 15728880, ctx.levelState().cameraRenderState);
      pose.popPose();
   }

   private static void submitOrb(PoseStack pose, SubmitNodeCollector collector, Vec3 rel, float height, float timeTicks, float abilityTicks, Vec3 victimRel) {
      float riseT = Mth.clamp(abilityTicks / 18.0F, 0.0F, 1.0F);
      double eased = (double)1.0F - Math.pow((double)1.0F - (double)riseT, (double)3.0F);
      double from = (double)height * 0.55;
      double to = (double)height + 2.3;
      Vec3 orb = rel.add((double)0.0F, Mth.lerp(eased, from, to), (double)0.0F);
      float pulse = 0.8F + 0.2F * Mth.sin((double)(timeTicks * 0.4F));
      float emerge = 0.35F + 0.65F * riseT;
      Vec3 view = orb.normalize();
      submitFan(pose, collector, orb, view, 0.4 * (double)pulse * (double)emerge, 235, 170, 255, (int)(225.0F * emerge));
      submitFan(pose, collector, orb, view, 0.92 * (double)pulse * (double)emerge, 175, 70, 245, (int)(110.0F * emerge));
      double spin = (double)timeTicks * (double)0.75F;
      submitShardRing(pose, collector, orb, view, 0.95 * (double)emerge, spin, 6, 230, 150, 255, (int)(200.0F * emerge));
      submitShardRing(pose, collector, orb, view, 1.35 * (double)emerge, -spin * 0.6, 5, 170, 80, 245, (int)(140.0F * emerge));
      if (victimRel != null && !(riseT < 1.0F)) {
         Vec3 span = victimRel.subtract(orb);
         double len = span.length();
         if (!(len < (double)0.5F)) {
            Vec3 dir = span.scale((double)1.0F / len);
            Vec3 right = dir.cross(new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
            if (right.lengthSqr() < 1.0E-4) {
               right = new Vec3((double)1.0F, (double)0.0F, (double)0.0F);
            }

            right = right.normalize();
            Vec3 upB = right.cross(dir).normalize();
            Vec3[] top = new Vec3[4];
            Vec3[] bottom = new Vec3[4];
            int[][] signs = new int[][]{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};

            for(int i = 0; i < 4; ++i) {
               int a = signs[i][0];
               int b = signs[i][1];
               top[i] = orb.add(right.scale((double)a * 0.16)).add(upB.scale((double)b * 0.16));
               bottom[i] = victimRel.add(right.scale((double)a * (double)0.75F)).add(upB.scale((double)b * (double)0.75F));
            }

            int alpha = (int)(60.0F * pulse);
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(BEAM_TEXTURE), (p, consumer) -> {
               for(int i = 0; i < 4; ++i) {
                  int j = (i + 1) % 4;
                  emitQuad(p, consumer, top[i], top[j], bottom[j], bottom[i], alpha + 60, alpha);
                  emitQuad(p, consumer, top[j], top[i], bottom[i], bottom[j], alpha + 60, alpha);
               }

            });
         }
      }
   }

   private static void submitFragmentGlow(PoseStack pose, SubmitNodeCollector collector, Vec3 rel, float timeTicks, int seed) {
      float phase = timeTicks * 0.055F + (float)(seed % 32) * 0.63F;
      float breath = 0.5F + 0.5F * Mth.sin((double)phase);
      breath *= breath;
      Vec3 view = rel.normalize();
      int core = (int)(12.0F + 55.0F * breath);
      int bloom = (int)(5.0F + 26.0F * breath);
      submitFan(pose, collector, rel, view, 0.13 + 0.05 * (double)breath, 210, 150, 255, core);
      submitFan(pose, collector, rel, view, 0.28 + 0.11 * (double)breath, 150, 60, 235, bloom);
   }

   private static void submitShardRing(PoseStack pose, SubmitNodeCollector collector, Vec3 centre, Vec3 viewDir, double radius, double phase, int count, int r, int g, int b, int alpha) {
      if (alpha > 2 && !(radius <= 0.01)) {
         Vec3 up = Math.abs(viewDir.y) > 0.98 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
         Vec3 right = viewDir.cross(up).normalize();
         Vec3 upB = right.cross(viewDir).normalize();

         for(int i = 0; i < count; ++i) {
            double ang = phase + (Math.PI * 2D) * (double)i / (double)count;
            Vec3 at = centre.add(right.scale(Math.cos(ang) * radius)).add(upB.scale(Math.sin(ang) * radius));
            submitFan(pose, collector, at, viewDir, 0.13, r, g, b, alpha);
         }

      }
   }

   private static void submitFan(PoseStack pose, SubmitNodeCollector collector, Vec3 centre, Vec3 viewDir, double radius, int r, int g, int b, int centreAlpha) {
      Vec3 up = Math.abs(viewDir.y) > 0.98 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
      Vec3 right = viewDir.cross(up).normalize();
      Vec3 upB = right.cross(viewDir).normalize();
      Vec3[] rim = new Vec3[17];

      for(int i = 0; i <= 16; ++i) {
         double ang = (Math.PI * 2D) * (double)i / (double)16.0F;
         rim[i] = centre.add(right.scale(Math.cos(ang) * radius)).add(upB.scale(Math.sin(ang) * radius));
      }

      collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(BEAM_TEXTURE), (p, consumer) -> {
         for(int i = 0; i < 16; ++i) {
            vertex(p, consumer, centre, 0.5F, 0.5F, r, g, b, centreAlpha);
            vertex(p, consumer, centre, 0.5F, 0.5F, r, g, b, centreAlpha);
            vertex(p, consumer, rim[i], 0.0F, 1.0F, r, g, b, 0);
            vertex(p, consumer, rim[i + 1], 1.0F, 1.0F, r, g, b, 0);
         }

      });
   }

   private static void emitQuad(PoseStack.Pose pose, VertexConsumer consumer, Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3, int topAlpha, int alpha) {
      vertex(pose, consumer, v0, 0.0F, 0.0F, 200, 110, 255, topAlpha);
      vertex(pose, consumer, v1, 1.0F, 0.0F, 200, 110, 255, topAlpha);
      vertex(pose, consumer, v2, 1.0F, 1.0F, 200, 110, 255, alpha);
      vertex(pose, consumer, v3, 0.0F, 1.0F, 200, 110, 255, alpha);
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 p, float u, float v, int r, int g, int b, int alpha) {
      consumer.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(r, g, b, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
