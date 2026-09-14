package net.dabicco.witherstormmod.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.dabicco.witherstormmod.network.WitherStormPositionPacket;
import net.dabicco.witherstormmod.network.WitherStormPositionPacket.HeadData;
import net.dabicco.witherstormmod.network.WitherStormPositionPacket.SeveredData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientDistantStormManager {
   private static final Map<Integer, net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData> STORMS = new HashMap<>();
   private static final long EXPIRE_MILLIS = 10000L;
   private static long lastCheckMillis = 0L;
   private static ResourceKey<Level> cachedDim;
   /**
    * One atmospheric owner captured at LevelRenderer.render HEAD. Every sky,
    * fog, cloud, backdrop, and HUD pass in that frame reads this immutable
    * selection instead of independently choosing a nearest storm.
    */
   private static StormData atmosphericOwner;
   private static Vec3 atmosphericOrigin = Vec3.ZERO;
   private static boolean atmosphereCaptured;

   public static void update(WitherStormPositionPacket p) {
      net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d = STORMS.computeIfAbsent(p.entityId(), id -> {
         net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData nd = new net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData();
         nd.entityId = id;
         return nd;
      });
      d.x = p.x();
      d.y = p.y();
      d.z = p.z();
      d.yaw = p.yaw();
      d.pitch = p.pitch();
      d.roll = p.roll();
      d.phase = p.phase();
      d.phase5Ticks = p.phase5Ticks();
      d.phase58Ticks = p.phase58Ticks();
      d.activeHeads = p.activeHeads();
      d.collapsed = p.collapsed();
      d.siegeStage = p.siegeStage();
      d.collapseTicks = p.collapseTicks();
      d.siegeProgress = p.siegeProgress();
      long gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;

      for (int i = 0; i < 3; i++) {
         HeadData h = p.heads()[i];
         d.headYaw[i] = h.localYaw();
         d.headPitch[i] = h.pitch();
         d.headFireStart[i] = h.fireElapsed() >= 0 ? gameTime - h.fireElapsed() : -1L;
         d.beamActive[i] = h.beamActive();
         d.beamEnd[i] = h.beamActive() ? new Vec3(h.beamX(), h.beamY(), h.beamZ()) : null;
      }

      d.severed = p.severed();
      if (!d.severedInitialized && d.severed.length > 0) {
         for (SeveredData s : d.severed) {
            int si = s.side() < 0 ? 0 : 1;
            d.sevDispX[si] = s.x();
            d.sevDispY[si] = s.y();
            d.sevDispZ[si] = s.z();
            d.sevDispYaw[si] = s.yaw();
         }

         d.severedInitialized = true;
      }

      if (!d.initialized) {
         d.dispX = d.x;
         d.dispY = d.y;
         d.dispZ = d.z;
         d.dispYaw = d.yaw;
         d.dispPitch = d.pitch;
         d.dispRoll = d.roll;

         for (int i = 0; i < 3; i++) {
            d.dispHeadYaw[i] = d.headYaw[i];
            d.dispHeadPitch[i] = d.headPitch[i];
         }

         d.initialized = true;
      }

      d.lastUpdateMillis = System.currentTimeMillis();
   }

   public static Collection<net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData> all() {
      Minecraft mc = Minecraft.getInstance();
      ResourceKey<Level> dim = mc.level == null ? null : mc.level.dimension();
      if (dim != cachedDim) {
         cachedDim = dim;
         STORMS.clear();
      }

      long now = System.currentTimeMillis();
      long delta = lastCheckMillis == 0L ? 0L : now - lastCheckMillis;
      lastCheckMillis = now;
      boolean frozen = Minecraft.getInstance().isPaused();
      if (delta > 0L && (frozen || delta > 1000L)) {
         for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : STORMS.values()) {
            d.lastUpdateMillis += delta;
         }
      }

      Iterator<net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData> it = STORMS.values().iterator();

      while (it.hasNext()) {
         if (now - it.next().lastUpdateMillis > 10000L) {
            it.remove();
         }
      }

      return STORMS.values();
   }

   /**
    * Phases 1 through 4 belong entirely to vanilla's overworld sky and clouds.
    * The storm owns the environment only once phase 5 has begun.
    */
   public static boolean customWeatherActive(float phase) {
      return phase >= 5.0F;
   }

   /**
    * Captures the one state that owns the atmospheric overlay for the current
    * render frame. This is called once from the LevelRenderer HEAD hook before
    * any sky/fog/cloud/backdrop pass runs. The origin is copied from the
    * authoritative StormData accessor at the same time, so later passes cannot
    * drift to a different storm or a camera-relative centre.
    */
   public static void captureAtmosphere(Vec3 camera) {
      StormData best = null;
      double bestDistance = Double.MAX_VALUE;
      for (StormData state : all()) {
         if (!customWeatherActive(state.phase)) {
            continue;
         }
         Vec3 origin = state.getStormOrigin();
         double distance = origin.distanceToSqr(camera);
         if (distance < bestDistance) {
            bestDistance = distance;
            best = state;
         }
      }
      atmosphericOwner = best;
      atmosphericOrigin = best == null ? Vec3.ZERO : best.getStormOrigin();
      atmosphereCaptured = true;
   }

   /**
    * Returns the owner captured for this frame. The lazy fallback preserves
    * compatibility for callers outside LevelRenderer while normal rendering
    * always uses the explicit per-frame capture above.
    */
   public static StormData nearestCustomWeather(Vec3 camera) {
      if (!atmosphereCaptured) {
         captureAtmosphere(camera);
      }
      return atmosphericOwner;
   }

   /** Authoritative origin paired with nearestCustomWeather(). */
   public static Vec3 atmosphericOrigin() {
      return atmosphericOrigin;
   }

   public static void remove(int entityId) {
      STORMS.remove(entityId);
      if (atmosphericOwner != null && atmosphericOwner.entityId == entityId) {
         atmosphericOwner = null;
         atmosphericOrigin = Vec3.ZERO;
      }
   }

   public static void clear() {
      STORMS.clear();
      atmosphericOwner = null;
      atmosphericOrigin = Vec3.ZERO;
      atmosphereCaptured = false;
   }

   public static final class StormData {
      public int entityId;
      public double x;
      public double y;
      public double z;
      public float yaw;
      public float pitch;
      public float roll;
      public float phase;
      public int activeHeads = 3;
      public boolean collapsed;
      public int siegeStage;
      public int collapseTicks;
      public int siegeProgress;
      public int phase5Ticks = -1;
      public int phase58Ticks = -1;
      public final float[] headYaw = new float[3];
      public final float[] headPitch = new float[3];
      public final long[] headFireStart = new long[]{-1L, -1L, -1L};
      public final boolean[] beamActive = new boolean[3];
      public final Vec3[] beamEnd = new Vec3[3];
      public double dispX;
      public double dispY;
      public double dispZ;
      public float dispYaw;
      public float dispPitch;
      public float dispRoll;
      public final float[] dispHeadYaw = new float[3];
      public final float[] dispHeadPitch = new float[3];
      public final Vec3[] dispBeamEnd = new Vec3[3];
      public boolean initialized = false;
      public long lastUpdateMillis;
      public SeveredData[] severed = new SeveredData[0];
      public final double[] sevDispX = new double[2];
      public final double[] sevDispY = new double[2];
      public final double[] sevDispZ = new double[2];
      public final float[] sevDispYaw = new float[2];
      public boolean severedInitialized = false;

      /**
       * Authoritative packet/entity origin.  Rendering interpolation belongs
       * to the model pass; atmospheric placement must not drift with a
       * camera-relative or independently smoothed billboard position.
       */
      public Vec3 getStormOrigin() {
         return new Vec3(this.x, this.y, this.z);
      }
   }
}
