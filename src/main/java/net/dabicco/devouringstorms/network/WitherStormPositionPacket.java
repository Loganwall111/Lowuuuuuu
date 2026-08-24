package net.dabicco.devouringstorms.network;

import net.dabicco.devouringstorms.client.ClientDistantStormManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
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
   float expansionPhase,
   int phase5Ticks,
   int phase58Ticks,
   int activeHeads,
   WitherStormPositionPacket.HeadData[] heads,
   boolean collapsed,
   int collapseTicks,
   int siegeStage,
   int siegeProgress,
   WitherStormPositionPacket.SeveredData[] severed
) implements CustomPacketPayload {
   public static final int HEAD_COUNT = 3;
   public static final Identifier ID = Identifier.fromNamespaceAndPath("devouringstorms", "storm_position");
   public static final Type<WitherStormPositionPacket> TYPE = new Type(ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, WitherStormPositionPacket> CODEC = StreamCodec.of(
      (buf, pkt) -> {
         buf.writeVarInt(pkt.entityId);
         buf.writeDouble(pkt.x);
         buf.writeDouble(pkt.y);
         buf.writeDouble(pkt.z);
         buf.writeFloat(pkt.yaw);
         buf.writeFloat(pkt.pitch);
         buf.writeFloat(pkt.roll);
         buf.writeFloat(pkt.phase);
         buf.writeFloat(pkt.expansionPhase);
         buf.writeInt(pkt.phase5Ticks);
         buf.writeInt(pkt.phase58Ticks);
         buf.writeByte(pkt.activeHeads);
         buf.writeBoolean(pkt.collapsed);
         buf.writeVarInt(pkt.collapseTicks);
         buf.writeVarInt(pkt.siegeStage);
         buf.writeVarInt(pkt.siegeProgress);

         for (int i = 0; i < 3; i++) {
            WitherStormPositionPacket.HeadData h = pkt.heads[i];
            buf.writeFloat(h.localYaw());
            buf.writeFloat(h.pitch());
            buf.writeVarInt(h.fireElapsed());
            buf.writeBoolean(h.beamActive());
            if (h.beamActive()) {
               buf.writeDouble(h.beamX());
               buf.writeDouble(h.beamY());
               buf.writeDouble(h.beamZ());
            }
         }

         buf.writeVarInt(pkt.severed.length);

         for (WitherStormPositionPacket.SeveredData s : pkt.severed) {
            buf.writeVarInt(s.entityId());
            buf.writeDouble(s.x());
            buf.writeDouble(s.y());
            buf.writeDouble(s.z());
            buf.writeFloat(s.yaw());
            buf.writeByte(s.side());
            buf.writeByte(s.heads());

            for (int ix = 0; ix < 3; ix++) {
               buf.writeFloat(s.headYaw()[ix]);
               buf.writeFloat(s.headPitch()[ix]);
               buf.writeVarInt(s.headFireElapsed()[ix] + 1);
               buf.writeBoolean(s.headBeamActive()[ix]);
               if (s.headBeamActive()[ix]) {
                  buf.writeDouble(s.headBeamX()[ix]);
                  buf.writeDouble(s.headBeamY()[ix]);
                  buf.writeDouble(s.headBeamZ()[ix]);
               }
            }
         }
      },
      buf -> {
         int entityId = buf.readVarInt();
         double x = buf.readDouble();
         double y = buf.readDouble();
         double z = buf.readDouble();
         float yaw = buf.readFloat();
         float pitch = buf.readFloat();
         float roll = buf.readFloat();
         float phase = buf.readFloat();
         float expansionPhase = buf.readFloat();
         int phase5Ticks = buf.readInt();
         int phase58Ticks = buf.readInt();
         int activeHeads = buf.readByte();
         boolean collapsed = buf.readBoolean();
         int collapseTicks = buf.readVarInt();
         int siegeStage = buf.readVarInt();
         int siegeProgress = buf.readVarInt();
         WitherStormPositionPacket.HeadData[] heads = new WitherStormPositionPacket.HeadData[3];

         for (int i = 0; i < 3; i++) {
            float localYaw = buf.readFloat();
            float headPitch = buf.readFloat();
            int fireElapsed = buf.readVarInt();
            boolean beamActive = buf.readBoolean();
            double bx = 0.0;
            double by = 0.0;
            double bz = 0.0;
            if (beamActive) {
               bx = buf.readDouble();
               by = buf.readDouble();
               bz = buf.readDouble();
            }

            heads[i] = new WitherStormPositionPacket.HeadData(localYaw, headPitch, fireElapsed, beamActive, bx, by, bz);
         }

         int severedCount = buf.readVarInt();
         WitherStormPositionPacket.SeveredData[] severed = new WitherStormPositionPacket.SeveredData[severedCount];

         for (int i = 0; i < severedCount; i++) {
            int sId = buf.readVarInt();
            double sx = buf.readDouble();
            double sy = buf.readDouble();
            double sz = buf.readDouble();
            float sYaw = buf.readFloat();
            int sSide = buf.readByte();
            int sHeads = buf.readByte();
            float[] sHeadYaw = new float[3];
            float[] sHeadPitch = new float[3];
            int[] sFire = new int[3];
            boolean[] sBeam = new boolean[3];
            double[] sBx = new double[3];
            double[] sBy = new double[3];
            double[] sBz = new double[3];

            for (int h = 0; h < 3; h++) {
               sHeadYaw[h] = buf.readFloat();
               sHeadPitch[h] = buf.readFloat();
               sFire[h] = buf.readVarInt() - 1;
               sBeam[h] = buf.readBoolean();
               if (sBeam[h]) {
                  sBx[h] = buf.readDouble();
                  sBy[h] = buf.readDouble();
                  sBz[h] = buf.readDouble();
               }
            }

            severed[i] = new WitherStormPositionPacket.SeveredData(sId, sx, sy, sz, sYaw, sSide, sHeads, sHeadYaw, sHeadPitch, sFire, sBeam, sBx, sBy, sBz);
         }

         return new WitherStormPositionPacket(
            entityId,
            x,
            y,
            z,
            yaw,
            pitch,
            roll,
            phase,
            expansionPhase,
            phase5Ticks,
            phase58Ticks,
            activeHeads,
            heads,
            collapsed,
            collapseTicks,
            siegeStage,
            siegeProgress,
            severed
         );
      }
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(WitherStormPositionPacket payload, Context context) {
      context.client().execute(() -> ClientDistantStormManager.update(payload));
   }

   public static record HeadData(float localYaw, float pitch, int fireElapsed, boolean beamActive, double beamX, double beamY, double beamZ) {
      public static final WitherStormPositionPacket.HeadData EMPTY = new WitherStormPositionPacket.HeadData(0.0F, 0.0F, -1, false, 0.0, 0.0, 0.0);
   }

   public static record SeveredData(
      int entityId,
      double x,
      double y,
      double z,
      float yaw,
      int side,
      int heads,
      float[] headYaw,
      float[] headPitch,
      int[] headFireElapsed,
      boolean[] headBeamActive,
      double[] headBeamX,
      double[] headBeamY,
      double[] headBeamZ
   ) {
   }
}
