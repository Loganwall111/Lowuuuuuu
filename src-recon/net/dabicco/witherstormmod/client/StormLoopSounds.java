package net.dabicco.witherstormmod.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.WitherStormHeadEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class StormLoopSounds {
   private static final Map<Integer, net.dabicco.witherstormmod.client.BeamGroundLoopSound> BEAMS = new HashMap<>();
   private static final Map<Integer, net.dabicco.witherstormmod.client.StormTornadoSound> TORNADOES = new HashMap<>();

   private StormLoopSounds() {
   }

   public static void tick(Minecraft mc) {
      if (mc.level == null || mc.player == null) {
         clear();
      } else if (!mc.isPaused()) {
         Iterator<Entry<Integer, net.dabicco.witherstormmod.client.BeamGroundLoopSound>> bit = BEAMS.entrySet().iterator();

         while (bit.hasNext()) {
            if (bit.next().getValue().isStopped()) {
               bit.remove();
            }
         }

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof WitherStormHeadEntity head
               && head.isAlive()
               && head.isBeamActive()
               && !BEAMS.containsKey(head.getId())
               && !(mc.player.position().distanceTo(head.getBeamEndExact()) > 42.0)) {
               net.dabicco.witherstormmod.client.BeamGroundLoopSound snd = new net.dabicco.witherstormmod.client.BeamGroundLoopSound(head);
               BEAMS.put(head.getId(), snd);
               mc.getSoundManager().play(snd);
            }
         }

         Iterator<Entry<Integer, net.dabicco.witherstormmod.client.StormTornadoSound>> tit = TORNADOES.entrySet().iterator();

         while (tit.hasNext()) {
            if (tit.next().getValue().isStopped()) {
               tit.remove();
            }
         }

         if (DabyWSClientConfig.stormAmbience) {
            for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
               if (!d.collapsed && !(d.phase < 4.0F) && !(d.phase >= 5.8F) && !TORNADOES.containsKey(d.entityId)) {
                  double dx = d.dispX - mc.player.getX();
                  double dz = d.dispZ - mc.player.getZ();
                  if (!(dx * dx + dz * dz > 67600.0)) {
                     net.dabicco.witherstormmod.client.StormTornadoSound snd = new net.dabicco.witherstormmod.client.StormTornadoSound(
                        d.entityId, d.dispX, mc.player.getY(), d.dispZ
                     );
                     TORNADOES.put(d.entityId, snd);
                     mc.getSoundManager().play(snd);
                  }
               }
            }
         }
      }
   }

   public static void clear() {
      BEAMS.clear();
      TORNADOES.clear();
   }
}
