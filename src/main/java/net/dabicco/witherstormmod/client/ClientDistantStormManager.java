package net.dabicco.witherstormmod.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.dabicco.witherstormmod.network.WitherStormPositionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientDistantStormManager {
   private static final Map<Integer, StormData> STORMS = new HashMap();
   private static final long EXPIRE_MILLIS = 10000L;
   private static long lastCheckMillis = 0L;
   private static ResourceKey<Level> cachedDim;

   public static void update(WitherStormPositionPacket p) {
      StormData d = (StormData)STORMS.computeIfAbsent(p.entityId(), (id) -> {
         StormData nd = new StormData();
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

      for(int i = 0; i < 3; ++i) {
         WitherStormPositionPacket.HeadData h = p.heads()[i];
         d.headYaw[i] = h.localYaw();
         d.headPitch[i] = h.pitch();
         d.headFireStart[i] = h.fireElapsed() >= 0 ? gameTime - (long)h.fireElapsed() : -1L;
         d.beamActive[i] = h.beamActive();
         d.beamEnd[i] = h.beamActive() ? new Vec3(h.beamX(), h.beamY(), h.beamZ()) : null;
      }

      d.severed = p.severed();
      if (!d.severedInitialized && d.severed.length > 0) {
         for(WitherStormPositionPacket.SeveredData s : d.severed) {
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

         for(int i = 0; i < 3; ++i) {
            d.dispHeadYaw[i] = d.headYaw[i];
            d.dispHeadPitch[i] = d.headPitch[i];
         }

         d.initialized = true;
      }

      d.lastUpdateMillis = System.currentTimeMillis();
   }

   public static Collection<StormData> all() {
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
         for(StormData d : STORMS.values()) {
            d.lastUpdateMillis += delta;
         }
      }

      Iterator<StormData> it = STORMS.values().iterator();

      while(it.hasNext()) {
         if (now - ((StormData)it.next()).lastUpdateMillis > 10000L) {
            it.remove();
         }
      }

      return java.util.List.copyOf(STORMS.values());
   }

   public static void remove(int entityId) {
      STORMS.remove(entityId);
   }

   public static void clear() {
      STORMS.clear();
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
      public WitherStormPositionPacket.SeveredData[] severed = new WitherStormPositionPacket.SeveredData[0];
      public final double[] sevDispX = new double[2];
      public final double[] sevDispY = new double[2];
      public final double[] sevDispZ = new double[2];
      public final float[] sevDispYaw = new float[2];
      public boolean severedInitialized = false;
   }
}
