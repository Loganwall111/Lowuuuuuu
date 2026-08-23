package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class GpuBufferPool {
   private static final int RING = 3;
   private static final Map<String, Slot> SLOTS = new HashMap();

   private GpuBufferPool() {
   }

   public static GpuBuffer write(String name, int usage, ByteBuffer data) {
      int needed = data.remaining();
      Slot slot = (Slot)SLOTS.computeIfAbsent(name, (k) -> new Slot());
      if (slot.capacity < needed) {
         for(int i = 0; i < 3; ++i) {
            if (slot.ring[i] != null) {
               slot.ring[i].close();
            }

            slot.ring[i] = null;
         }

         slot.capacity = Math.max(needed, needed * 3 / 2);
      }

      int index = slot.next;
      slot.next = (slot.next + 1) % 3;
      if (slot.ring[index] == null) {
         int capacity = slot.capacity;
         slot.ring[index] = RenderSystem.getDevice().createBuffer(() -> name, usage | 8, (long)capacity);
      }

      try {
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(slot.ring[index].slice(0L, (long)needed), data);
      } catch (IllegalStateException var8) {
         int capacity = slot.capacity;
         slot.ring[index] = RenderSystem.getDevice().createBuffer(() -> name, usage | 8, (long)capacity);
         data.rewind();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(slot.ring[index].slice(0L, (long)needed), data);
      }

      return slot.ring[index];
   }

   public static void close() {
      for(Slot slot : SLOTS.values()) {
         for(GpuBuffer buffer : slot.ring) {
            if (buffer != null) {
               buffer.close();
            }
         }
      }

      SLOTS.clear();
   }

   private static final class Slot {
      final GpuBuffer[] ring = new GpuBuffer[3];
      int capacity;
      int next;
   }
}
