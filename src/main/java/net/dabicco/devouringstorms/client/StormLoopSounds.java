package net.dabicco.devouringstorms.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.WitherStormHeadEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class StormLoopSounds {
   private static final Map<Integer, BeamGroundLoopSound> BEAMS = new HashMap();
   private static final Map<Integer, StormTornadoSound> TORNADOES = new HashMap();

   private StormLoopSounds() {
   }

   public static void tick(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         if (!mc.isPaused()) {
            Iterator<Map.Entry<Integer, BeamGroundLoopSound>> bit = BEAMS.entrySet().iterator();

            while(bit.hasNext()) {
               if (((BeamGroundLoopSound)((Map.Entry)bit.next()).getValue()).isStopped()) {
                  bit.remove();
               }
            }

            for(Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof WitherStormHeadEntity) {
                  WitherStormHeadEntity head = (WitherStormHeadEntity)entity;
                  if (head.isAlive() && head.isBeamActive() && !BEAMS.containsKey(head.getId()) && !(mc.player.position().distanceTo(head.getBeamEndExact()) > (double)42.0F)) {
                     BeamGroundLoopSound snd = new BeamGroundLoopSound(head);
                     BEAMS.put(head.getId(), snd);
                     mc.getSoundManager().play(snd);
                  }
               }
            }

            Iterator<Map.Entry<Integer, StormTornadoSound>> tit = TORNADOES.entrySet().iterator();

            while(tit.hasNext()) {
               if (((StormTornadoSound)((Map.Entry)tit.next()).getValue()).isStopped()) {
                  tit.remove();
               }
            }

            if (DevouringStormsClientConfig.stormAmbience) {
               for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                  if (!d.collapsed && !(d.phase < 4.0F) && !(d.phase >= 5.8F) && !TORNADOES.containsKey(d.entityId)) {
                     double dx = d.dispX - mc.player.getX();
                     double dz = d.dispZ - mc.player.getZ();
                     if (!(dx * dx + dz * dz > (double)67600.0F)) {
                        StormTornadoSound snd = new StormTornadoSound(d.entityId, d.dispX, mc.player.getY(), d.dispZ);
                        TORNADOES.put(d.entityId, snd);
                        mc.getSoundManager().play(snd);
                     }
                  }
               }

            }
         }
      } else {
         clear();
      }
   }

   public static void clear() {
      BEAMS.clear();
      TORNADOES.clear();
   }
}
