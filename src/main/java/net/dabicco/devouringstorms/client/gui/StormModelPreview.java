package net.dabicco.devouringstorms.client.gui;

import java.util.Arrays;
import java.util.List;
import net.dabicco.devouringstorms.client.PreviewScene;
import net.dabicco.devouringstorms.entity.ModEntityTypes;
import net.dabicco.devouringstorms.entity.SeveredWitherStormEntity;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.dabicco.devouringstorms.entity.state.SeveredWitherStormRenderState;
import net.dabicco.devouringstorms.entity.state.WitherStormRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class StormModelPreview {
   private static final WitherStormRenderState STATE = new WitherStormRenderState();
   private static final SeveredWitherStormRenderState SEVERED = new SeveredWitherStormRenderState();
   private static final PreviewScene SCENE = new PreviewScene();
   public static final float SPIN_PER_SECOND = 9.0F;
   public static final float DEFAULT_PITCH = 18.0F;
   public static final float MIN_PITCH = -25.0F;
   public static final float MAX_PITCH = 80.0F;
   public static final float MIN_ZOOM = 0.35F;
   public static final float MAX_ZOOM = 4.0F;
   public static final float SUN_ELEVATION = 52.0F;
   public static final String[] SUBPHASE_LABELS = new String[]{".0", ".2", ".4", ".6", ".8"};
   private static final float[] PHASE_HEIGHT = new float[]{10.0F, 10.0F, 11.0F, 12.0F, 48.0F, 64.0F, 68.0F, 34.0F};
   private static final float LAST_HEIGHT = 34.0F;
   private static final float[] PHASE_WIDTH = new float[]{5.0F, 5.0F, 6.0F, 7.0F, 34.0F, 50.0F, 58.0F, 30.0F};
   private static final float LAST_WIDTH = 30.0F;
   private static final float[] PHASE_DROP = new float[]{0.11F, 0.11F, 0.11F, 0.11F, 0.27F, 0.29F, 0.29F, 0.22F};
   private static final float LAST_DROP = 0.22F;
   public static final int SUBJECT_SEVERED = 7;
   private static final float GROUND_GAP = 0.12F;
   private static final float FIELD_REACH = 3.4F;
   private static final float DEPTH_LIMIT_PIXELS = 850.0F;

   private StormModelPreview() {
   }

   private static void scene(View v, float sunAzimuth, int beamMask, int hazeColour, boolean castShadow) {
      SCENE.field = v.field();
      SCENE.groundY = v.groundY();
      SCENE.haze = hazeColour;
      SCENE.tile = v.tile();
      SCENE.pitch = v.pitch();
      SCENE.castShadow = castShadow;
      SCENE.beams = beamMask;
      float el = 0.9075712F;
      float az = sunAzimuth * ((float)Math.PI / 180F);
      SCENE.sunX = Mth.cos((double)el) * Mth.cos((double)az);
      SCENE.sunY = Mth.sin((double)el);
      SCENE.sunZ = Mth.cos((double)el) * Mth.sin((double)az);
   }

   public static double phaseValue(int phase, int subphase) {
      return (double)Mth.clamp(phase, 0, 6) + (double)Mth.clamp(subphase, 0, 4) * 0.2;
   }

   private static float frame(float[] table, float last, int phase, int subphase) {
      int lo = Mth.clamp(phase, 0, 7);
      if (lo == 7) {
         return table[lo];
      } else {
         float hi = lo >= 6 ? last : table[lo + 1];
         return Mth.lerp((float)Mth.clamp(subphase, 0, 4) * 0.2F, table[lo], hi);
      }
   }

   public static float autoSpinDegrees() {
      return (float)(Util.getMillis() % 360000L) / 1000.0F * 9.0F % 360.0F;
   }

   public static View view(int width, int height, int phase, int subphase, float pitch, float zoom, float panX, float panY) {
      int p = Mth.clamp(phase, 0, 7);
      float tall = frame(PHASE_HEIGHT, 34.0F, p, subphase);
      float wide = frame(PHASE_WIDTH, 30.0F, p, subphase);
      float drop = frame(PHASE_DROP, 0.22F, p, subphase);
      pitch = Mth.clamp(pitch, -25.0F, 80.0F);
      zoom = Mth.clamp(zoom, 0.35F, 4.0F);
      float scale = Math.min((float)height / tall, (float)width / wide) * 0.86F * zoom;
      float groundY = -(drop + 0.12F) * tall;
      float field = Math.min(3.4F * tall, 850.0F / scale);
      return new View(scale, -panX * tall, (0.5F - drop + panY) * tall, pitch, groundY, field, tall, tall / 9.0F);
   }

   public static float[] project(View v, int x0, int y0, int x1, int y1, double px, double py, double pz) {
      float cos = Mth.cos((double)(v.pitch() * ((float)Math.PI / 180F)));
      float sin = Mth.sin((double)(v.pitch() * ((float)Math.PI / 180F)));
      double rx = -px;
      double ry = -(py * (double)cos - pz * (double)sin);
      return new float[]{(float)(x0 + x1) * 0.5F + v.scale() * (float)(rx + (double)v.tx()), (float)(y0 + y1) * 0.5F + v.scale() * (float)(ry + (double)v.ty())};
   }

   public static double[] groundPoint(View v, int x0, int y0, int x1, int y1, double mouseX, double mouseY) {
      float cos = Mth.cos((double)(v.pitch() * ((float)Math.PI / 180F)));
      float sin = Mth.sin((double)(v.pitch() * ((float)Math.PI / 180F)));
      if (Math.abs(sin) < 0.03F) {
         return null;
      } else {
         double sx = (mouseX - (double)(x0 + x1) * (double)0.5F) / (double)v.scale() - (double)v.tx();
         double sy = (mouseY - (double)(y0 + y1) * (double)0.5F) / (double)v.scale() - (double)v.ty();
         double wx = -sx;
         double wz = (sy + (double)(v.groundY() * cos)) / (double)sin;
         return !(Math.abs(wx) > (double)v.field()) && !(Math.abs(wz) > (double)v.field()) ? new double[]{wx, wz} : null;
      }
   }

   public static float yawToward(double wx, double wz) {
      return (float)Math.toDegrees(Math.atan2(-wx, wz));
   }

   public static Vec3 headPosition(int phase, int subphase, float yaw, int index) {
      Vec3 off;
      if (phase == 7) {
         off = SeveredWitherStormEntity.previewHeadOffsets(false)[Mth.clamp(index, 0, 2)];
      } else {
         double value = phaseValue(phase, subphase);
         boolean early = value < (double)4.0F;
         off = early ? new Vec3((double)0.0F, 3.05, 0.14) : WitherStormEntity.headOffset(index, value >= (double)6.0F);
      }

      double rad = Math.toRadians((double)yaw);
      double c = Math.cos(rad);
      double s = Math.sin(rad);
      return new Vec3(off.x * c - off.z * s, off.y, off.x * s + off.z * c);
   }

   public static int headCount(int phase, int subphase) {
      if (phase == 7) {
         return 3;
      } else {
         double value = phaseValue(phase, subphase);
         if (value < (double)2.0F) {
            return 0;
         } else {
            return value < (double)4.0F ? 1 : 3;
         }
      }
   }

   public static void render(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, View v, int phase, int subphase, float yaw, float sunAzimuth, int beamMask, int hazeColour, boolean castShadow) {
      if (x1 - x0 > 8 && y1 - y0 > 8) {
         scene(v, sunAzimuth, beamMask, hazeColour, castShadow);
         EntityRenderState subject = phase == 7 ? fillSevered(yaw) : fillState(Mth.clamp(phase, 0, 6), subphase, yaw);
         Quaternionf tilt = (new Quaternionf()).rotateX(v.pitch() * ((float)Math.PI / 180F));
         Quaternionf rotation = (new Quaternionf()).rotateZ((float)Math.PI).mul(tilt);
         g.entity(subject, v.scale(), new Vector3f(v.tx(), v.ty(), 0.0F), rotation, (new Quaternionf(tilt)).conjugate(), x0, y0, x1, y1);
      }
   }

   private static EntityRenderState fillSevered(float yaw) {
      SEVERED.entityType = ModEntityTypes.SEVERED_WITHER_STORM;
      SEVERED.preview = SCENE;
      SEVERED.phase = 6.0F;
      SEVERED.mirrored = false;
      SEVERED.side = 1;
      SEVERED.collapseTicks = -1.0F;
      SEVERED.droop = 0.0F;
      SEVERED.slopePitch = 0.0F;
      SEVERED.slopeRoll = 0.0F;
      Arrays.fill(SEVERED.groundBias, 0.0F);
      SEVERED.bodyRot = yaw;
      SEVERED.yRot = 0.0F;
      SEVERED.xRot = 0.0F;
      SEVERED.bodyRoll = 0.0F;
      SEVERED.idleTimeTicks = clock();
      SEVERED.ageInTicks = SEVERED.idleTimeTicks;
      SEVERED.stormId = -2000;
      SEVERED.bodyLight = 15728880;
      blank(SEVERED);
      return SEVERED;
   }

   private static float clock() {
      return (float)(Util.getMillis() % 100000L) / 50.0F;
   }

   private static void blank(EntityRenderState st) {
      st.lightCoords = 15728880;
      st.isInvisible = false;
      st.nameTag = null;
      st.scoreText = null;
      st.shadowRadius = 0.0F;
      st.shadowPieces.clear();
      st.leashStates = List.of();
      st.passengerOffset = Vec3.ZERO;
      st.nameTagAttachment = Vec3.ZERO;
      st.displayFireAnimation = false;
      st.outlineColor = 0;
      st.x = st.y = st.z = (double)0.0F;
      st.boundingBoxWidth = 1.0F;
      st.boundingBoxHeight = 1.0F;
      st.eyeHeight = 1.0F;
      st.distanceToCameraSq = (double)1.0F;
   }

   private static EntityRenderState fillState(int phase, int subphase, float yaw) {
      STATE.entityType = ModEntityTypes.WITHER_STORM;
      STATE.preview = SCENE;
      double value = phaseValue(phase, subphase);
      STATE.phase = value;
      STATE.phase4 = value >= (double)4.0F;
      STATE.devourer = value >= (double)6.0F;
      STATE.hatch = 1.0F;
      STATE.changeover = 0.0F;
      STATE.collapseTicks = -1.0F;
      STATE.snatchActive = false;
      STATE.slopePitch = 0.0F;
      STATE.slopeRoll = 0.0F;
      Arrays.fill(STATE.groundBias, 0.0F);
      STATE.spawnElapsedTicks = Float.MAX_VALUE;
      STATE.playingSpawnAnimation = false;
      STATE.phase5ElapsedTicks = value >= (double)5.0F ? 6000.0F : -1.0F;
      STATE.phase58ElapsedTicks = value >= 5.8 && value < (double)6.0F ? 6000.0F : -1.0F;
      STATE.frontTentacleElapsedTicks = value >= (double)3.0F && value < (double)4.0F ? 6000.0F : -1.0F;
      STATE.miniHeadElapsedTicks = value >= (double)2.0F ? 6000.0F : -1.0F;
      float now = clock();
      STATE.ageInTicks = STATE.idleTimeTicks = now;
      STATE.bodyRot = yaw;
      STATE.yRot = 0.0F;
      STATE.xRot = 0.0F;
      STATE.bodyRoll = 0.0F;
      STATE.headXRot[0] = STATE.headXRot[1] = 0.0F;
      STATE.headYRot[0] = STATE.headYRot[1] = 0.0F;
      double drift = 0.16;
      STATE.velX = (double)Mth.sin((double)(now * 0.013F)) * 0.16;
      STATE.velZ = (double)Mth.cos((double)(now * 0.009F)) * 0.16;
      STATE.velY = (double)Mth.sin((double)(now * 0.021F)) * 0.16 * 0.35;
      STATE.nightFactor = 0.0F;
      blank(STATE);
      STATE.worldX = STATE.worldY = STATE.worldZ = (double)0.0F;
      STATE.stormId = -1000 - phase * 8 - subphase;
      return STATE;
   }

   public static record View(float scale, float tx, float ty, float pitch, float groundY, float field, float tall, float tile) {
   }
}
