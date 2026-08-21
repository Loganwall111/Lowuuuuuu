package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.ClientPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WitherStormPositionPacket(
      int entityId,
      double x,
      double y,
      double z,
      float yaw,
      float pitch,
      float roll,
      float phase,
      int phase5Ticks,
      int phase58Ticks,
      int activeHeads,
      HeadData[] heads,
      boolean collapsed,
      int collapseTicks,
      int siegeStage,
      int siegeProgress,
      SeveredData[] severed
) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<WitherStormPositionPacket> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "storm_position"));
   public static final StreamCodec<RegistryFriendlyByteBuf, WitherStormPositionPacket> CODEC = StreamCodec.of(WitherStormPositionPacket::write, WitherStormPositionPacket::read);

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(WitherStormPositionPacket payload, ClientPayloadContext context) {
      context.client().execute(() -> ClientDistantStormManager.update(payload));
   }

   private static void write(RegistryFriendlyByteBuf buf, WitherStormPositionPacket p) {
      buf.writeVarInt(p.entityId());
      buf.writeDouble(p.x());
      buf.writeDouble(p.y());
      buf.writeDouble(p.z());
      buf.writeFloat(p.yaw());
      buf.writeFloat(p.pitch());
      buf.writeFloat(p.roll());
      buf.writeFloat(p.phase());
      buf.writeVarInt(p.phase5Ticks());
      buf.writeVarInt(p.phase58Ticks());
      buf.writeVarInt(p.activeHeads());
      int headCount = p.heads() == null ? 0 : p.heads().length;
      buf.writeVarInt(headCount);
      for(int i = 0; i < headCount; ++i) {
         HeadData h = p.heads()[i];
         buf.writeFloat(h.localYaw());
         buf.writeFloat(h.pitch());
         buf.writeVarInt(h.fireElapsed());
         buf.writeBoolean(h.beamActive());
         buf.writeDouble(h.beamX());
         buf.writeDouble(h.beamY());
         buf.writeDouble(h.beamZ());
      }
      buf.writeBoolean(p.collapsed());
      buf.writeVarInt(p.collapseTicks());
      buf.writeVarInt(p.siegeStage());
      buf.writeVarInt(p.siegeProgress());
      int sevCount = p.severed() == null ? 0 : p.severed().length;
      buf.writeVarInt(sevCount);
      for(int i = 0; i < sevCount; ++i) {
         SeveredData s = p.severed()[i];
         buf.writeVarInt(s.entityId());
         buf.writeDouble(s.x());
         buf.writeDouble(s.y());
         buf.writeDouble(s.z());
         buf.writeFloat(s.yaw());
         buf.writeVarInt(s.side());
         buf.writeVarInt(s.activeHeadCount());
         writeFloats(buf, s.hYaw());
         writeFloats(buf, s.hPitch());
         writeInts(buf, s.hFire());
         writeBooleans(buf, s.hBeam());
         writeDoubles(buf, s.hx());
         writeDoubles(buf, s.hy());
         writeDoubles(buf, s.hz());
      }
   }

   private static WitherStormPositionPacket read(RegistryFriendlyByteBuf buf) {
      int entityId = buf.readVarInt();
      double x = buf.readDouble();
      double y = buf.readDouble();
      double z = buf.readDouble();
      float yaw = buf.readFloat();
      float pitch = buf.readFloat();
      float roll = buf.readFloat();
      float phase = buf.readFloat();
      int phase5Ticks = buf.readVarInt();
      int phase58Ticks = buf.readVarInt();
      int activeHeads = buf.readVarInt();
      int headCount = buf.readVarInt();
      HeadData[] heads = new HeadData[headCount];
      for(int i = 0; i < headCount; ++i) {
         heads[i] = new HeadData(buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble());
      }
      boolean collapsed = buf.readBoolean();
      int collapseTicks = buf.readVarInt();
      int siegeStage = buf.readVarInt();
      int siegeProgress = buf.readVarInt();
      int sevCount = buf.readVarInt();
      SeveredData[] severed = new SeveredData[sevCount];
      for(int i = 0; i < sevCount; ++i) {
         int sevId = buf.readVarInt();
         double sx = buf.readDouble();
         double sy = buf.readDouble();
         double sz = buf.readDouble();
         float syaw = buf.readFloat();
         int side = buf.readVarInt();
         int activeCount = buf.readVarInt();
         float[] hYaw = readFloats(buf);
         float[] hPitch = readFloats(buf);
         int[] hFire = readInts(buf);
         boolean[] hBeam = readBooleans(buf);
         double[] hx = readDoubles(buf);
         double[] hy = readDoubles(buf);
         double[] hz = readDoubles(buf);
         severed[i] = new SeveredData(sevId, sx, sy, sz, syaw, side, activeCount, hYaw, hPitch, hFire, hBeam, hx, hy, hz);
      }
      return new WitherStormPositionPacket(entityId, x, y, z, yaw, pitch, roll, phase, phase5Ticks, phase58Ticks, activeHeads, heads, collapsed, collapseTicks, siegeStage, siegeProgress, severed);
   }

   private static void writeFloats(RegistryFriendlyByteBuf buf, float[] arr) {
      buf.writeVarInt(arr == null ? 0 : arr.length);
      if (arr != null) {
         for(float v : arr) {
            buf.writeFloat(v);
         }
      }
   }

   private static void writeInts(RegistryFriendlyByteBuf buf, int[] arr) {
      buf.writeVarInt(arr == null ? 0 : arr.length);
      if (arr != null) {
         for(int v : arr) {
            buf.writeVarInt(v);
         }
      }
   }

   private static void writeBooleans(RegistryFriendlyByteBuf buf, boolean[] arr) {
      buf.writeVarInt(arr == null ? 0 : arr.length);
      if (arr != null) {
         for(boolean v : arr) {
            buf.writeBoolean(v);
         }
      }
   }

   private static void writeDoubles(RegistryFriendlyByteBuf buf, double[] arr) {
      buf.writeVarInt(arr == null ? 0 : arr.length);
      if (arr != null) {
         for(double v : arr) {
            buf.writeDouble(v);
         }
      }
   }

   private static float[] readFloats(RegistryFriendlyByteBuf buf) {
      int n = buf.readVarInt();
      float[] out = new float[n];
      for(int i = 0; i < n; ++i) {
         out[i] = buf.readFloat();
      }
      return out;
   }

   private static int[] readInts(RegistryFriendlyByteBuf buf) {
      int n = buf.readVarInt();
      int[] out = new int[n];
      for(int i = 0; i < n; ++i) {
         out[i] = buf.readVarInt();
      }
      return out;
   }

   private static boolean[] readBooleans(RegistryFriendlyByteBuf buf) {
      int n = buf.readVarInt();
      boolean[] out = new boolean[n];
      for(int i = 0; i < n; ++i) {
         out[i] = buf.readBoolean();
      }
      return out;
   }

   private static double[] readDoubles(RegistryFriendlyByteBuf buf) {
      int n = buf.readVarInt();
      double[] out = new double[n];
      for(int i = 0; i < n; ++i) {
         out[i] = buf.readDouble();
      }
      return out;
   }

   public static record HeadData(float localYaw, float pitch, int fireElapsed, boolean beamActive, double beamX, double beamY, double beamZ) {
      public static final HeadData EMPTY = new HeadData(0.0F, 0.0F, -1, false, 0.0, 0.0, 0.0);
   }

   public static record SeveredData(int entityId, double x, double y, double z, float yaw, int side, int activeHeadCount, float[] hYaw, float[] hPitch, int[] hFire, boolean[] hBeam, double[] hx, double[] hy, double[] hz) {
   }
}
