package net.dabicco.witherstormmod.client.gui;

import java.util.Arrays;
import java.util.List;
import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.SeveredWitherStormEntity;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.dabicco.witherstormmod.entity.state.SeveredWitherStormRenderState;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
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
   private static final net.dabicco.witherstormmod.client.PreviewScene SCENE = new net.dabicco.witherstormmod.client.PreviewScene();
   public static final float SPIN_PER_SECOND = 9.0F;
   public static final float DEFAULT_PITCH = 18.0F;
   public static final float MIN_PITCH = -25.0F;
   public static final float MAX_PITCH = 80.0F;
   public static final float MIN_ZOOM = 0.15F;
   public static final float MAX_ZOOM = 12.0F;
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
   private static final float DEPTH_LIMIT_PIXELS = 1400.0F;

   private StormModelPreview() {
   }

   private static void scene(StormModelPreview.View v, float sunAzimuth, int beamMask, int hazeColour, boolean castShadow, int backdrop) {
      SCENE.field = v.field();
      SCENE.groundY = v.groundY();
      SCENE.haze = hazeColour;
      SCENE.tile = v.tile();
      SCENE.pitch = v.pitch();
      SCENE.castShadow = castShadow;
      SCENE.beams = beamMask;
      SCENE.backdropIndex = backdrop;
      float el = 0.9075712F;
      float az = sunAzimuth * (float) (Math.PI / 180.0);
      SCENE.sunX = Mth.cos(el) * Mth.cos(az);
      SCENE.sunY = Mth.sin(el);
      SCENE.sunZ = Mth.cos(el) * Mth.sin(az);
   }

   public static double phaseValue(int phase, int subphase) {
      return Mth.clamp(phase, 0, 6) + Mth.clamp(subphase, 0, 4) * 0.2;
   }

   private static float frame(float[] table, float last, int phase, int subphase) {
      int lo = Mth.clamp(phase, 0, 7);
      if (lo == 7) {
         return table[lo];
      } else {
         float hi = lo >= 6 ? last : table[lo + 1];
         return Mth.lerp(Mth.clamp(subphase, 0, 4) * 0.2F, table[lo], hi);
      }
   }

   public static float autoSpinDegrees() {
      return (float)(Util.getMillis() % 360000L) / 1000.0F * 9.0F % 360.0F;
   }

   public static StormModelPreview.View view(int width, int height, int phase, int subphase, float pitch, float zoom, float panX, float panY) {
      int p = Mth.clamp(phase, 0, 7);
      float tall = frame(PHASE_HEIGHT, 34.0F, p, subphase);
      float wide = frame(PHASE_WIDTH, 30.0F, p, subphase);
      float drop = frame(PHASE_DROP, 0.22F, p, subphase);
      pitch = Mth.clamp(pitch, -25.0F, 80.0F);
      zoom = Mth.clamp(zoom, 0.15F, 12.0F);
      float scale = Math.min(height / tall, width / wide) * 0.86F * zoom;
      float groundY = -(drop + 0.12F) * tall;
      float field = Math.min(3.4F * tall, 1400.0F / scale);
      return new StormModelPreview.View(scale, -panX * tall, (0.5F - drop + panY) * tall, pitch, groundY, field, tall, tall / 9.0F);
   }

   public static float[] project(StormModelPreview.View v, int x0, int y0, int x1, int y1, double px, double py, double pz) {
      float cos = Mth.cos(v.pitch() * (float) (Math.PI / 180.0));
      float sin = Mth.sin(v.pitch() * (float) (Math.PI / 180.0));
      double rx = -px;
      double ry = -(py * cos - pz * sin);
      return new float[]{(x0 + x1) * 0.5F + v.scale() * (float)(rx + v.tx()), (y0 + y1) * 0.5F + v.scale() * (float)(ry + v.ty())};
   }

   public static double[] groundPoint(StormModelPreview.View v, int x0, int y0, int x1, int y1, double mouseX, double mouseY) {
      float cos = Mth.cos(v.pitch() * (float) (Math.PI / 180.0));
      float sin = Mth.sin(v.pitch() * (float) (Math.PI / 180.0));
      if (Math.abs(sin) < 0.03F) {
         return null;
      } else {
         double sx = (mouseX - (x0 + x1) * 0.5) / v.scale() - v.tx();
         double sy = (mouseY - (y0 + y1) * 0.5) / v.scale() - v.ty();
         double wx = -sx;
         double wz = (sy + v.groundY() * cos) / sin;
         return !(Math.abs(wx) > v.field()) && !(Math.abs(wz) > v.field()) ? new double[]{wx, wz} : null;
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
         boolean early = value < 4.0;
         off = early ? new Vec3(0.0, 3.05, 0.14) : WitherStormEntity.headOffset(index, value >= 6.0);
      }

      double rad = Math.toRadians(yaw);
      double c = Math.cos(rad);
      double s = Math.sin(rad);
      return new Vec3(off.x * c - off.z * s, off.y, off.x * s + off.z * c);
   }

   public static int headCount(int phase, int subphase) {
      if (phase == 7) {
         return 3;
      } else {
         double value = phaseValue(phase, subphase);
         if (value < 2.0) {
            return 0;
         } else {
            return value < 4.0 ? 1 : 3;
         }
      }
   }

   public static void render(
      GuiGraphicsExtractor g,
      int x0,
      int y0,
      int x1,
      int y1,
      StormModelPreview.View v,
      int phase,
      int subphase,
      float yaw,
      float sunAzimuth,
      int beamMask,
      int hazeColour,
      boolean castShadow
   ) {
      render(g, x0, y0, x1, y1, v, phase, subphase, yaw, sunAzimuth, beamMask, hazeColour, castShadow, 0);
   }

   public static void render(
      GuiGraphicsExtractor g,
      int x0,
      int y0,
      int x1,
      int y1,
      StormModelPreview.View v,
      int phase,
      int subphase,
      float yaw,
      float sunAzimuth,
      int beamMask,
      int hazeColour,
      boolean castShadow,
      int backdrop
   ) {
      if (x1 - x0 > 8 && y1 - y0 > 8) {
         scene(v, sunAzimuth, beamMask, hazeColour, castShadow, backdrop);
         EntityRenderState subject = phase == 7 ? fillSevered(yaw) : fillState(Mth.clamp(phase, 0, 6), subphase, yaw);
         Quaternionf tilt = new Quaternionf().rotateX(v.pitch() * (float) (Math.PI / 180.0));
         Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(tilt);
         g.entity(subject, v.scale(), new Vector3f(v.tx(), v.ty(), 0.0F), rotation, new Quaternionf(tilt).conjugate(), x0, y0, x1, y1);
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
      st.x = st.y = st.z = 0.0;
      st.boundingBoxWidth = 1.0F;
      st.boundingBoxHeight = 1.0F;
      st.eyeHeight = 1.0F;
      st.distanceToCameraSq = 1.0;
   }

   private static EntityRenderState fillState(int phase, int subphase, float yaw) {
      STATE.entityType = ModEntityTypes.WITHER_STORM;
      STATE.preview = SCENE;
      double value = phaseValue(phase, subphase);
      STATE.phase = value;
      STATE.phase4 = value >= 4.0;
      STATE.devourer = value >= 6.0;
      STATE.hatch = 1.0F;
      STATE.changeover = 0.0F;
      STATE.collapseTicks = -1.0F;
      STATE.snatchActive = false;
      STATE.slopePitch = 0.0F;
      STATE.slopeRoll = 0.0F;
      Arrays.fill(STATE.groundBias, 0.0F);
      STATE.spawnElapsedTicks = Float.MAX_VALUE;
      STATE.playingSpawnAnimation = false;
      STATE.phase5ElapsedTicks = value >= 5.0 ? 6000.0F : -1.0F;
      STATE.phase58ElapsedTicks = value >= 5.8 && value < 6.0 ? 6000.0F : -1.0F;
      STATE.frontTentacleElapsedTicks = value >= 3.0 && value < 4.0 ? 6000.0F : -1.0F;
      STATE.miniHeadElapsedTicks = value >= 2.0 ? 6000.0F : -1.0F;
      float now = clock();
      STATE.ageInTicks = STATE.idleTimeTicks = now;
      STATE.bodyRot = yaw;
      STATE.yRot = 0.0F;
      STATE.xRot = 0.0F;
      STATE.bodyRoll = 0.0F;
      STATE.headXRot[0] = STATE.headXRot[1] = 0.0F;
      STATE.headYRot[0] = STATE.headYRot[1] = 0.0F;
      double drift = 0.16;
      STATE.velX = Mth.sin(now * 0.013F) * 0.16;
      STATE.velZ = Mth.cos(now * 0.009F) * 0.16;
      STATE.velY = Mth.sin(now * 0.021F) * 0.16 * 0.35;
      STATE.nightFactor = 0.0F;
      blank(STATE);
      STATE.worldX = STATE.worldY = STATE.worldZ = 0.0;
      STATE.stormId = -1000 - phase * 8 - subphase;
      return STATE;
   }

   public record View(float scale, float tx, float ty, float pitch, float groundY, float field, float tall, float tile) {
   }
}
