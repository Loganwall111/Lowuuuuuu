package net.dabicco.witherstormmod.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface StormHeadHost {
   Vec3 headOffsetFor(int var1);

   default float headScaleFor(int index) {
      return 6.0F;
   }

   default float headYawOffsetFor(int index) {
      return 0.0F;
   }

   default float headRollOffsetFor(int index) {
      return 0.0F;
   }

   default net.dabicco.witherstormmod.entity.WitherStormHeadEntity hostHead(ServerLevel server, int index) {
      return null;
   }

   default boolean headsDistressed() {
      return false;
   }

   default float headYawRangeFor(int index) {
      return 90.0F;
   }

   default float headLitFor(int index) {
      return 1.0F;
   }

   default boolean headBeamAllowed(int index) {
      return true;
   }

   default float beamScaleFor(int index) {
      return 1.0F;
   }

   default float headPitchRangeFor(int index) {
      return 60.0F;
   }

   boolean isDevourerForm();

   float getBodyRoll();

   default float attachYaw(float partialTick) {
      return 0.0F;
   }

   default float attachPitch(float partialTick) {
      return 0.0F;
   }

   default float attachRoll(float partialTick) {
      return 0.0F;
   }

   default double attachPivotY() {
      return 0.0;
   }

   default double attachDrop(float partialTick) {
      return 0.0;
   }
}
