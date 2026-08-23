package net.dabicco.devouringstorms.bowels.client;

import java.util.HashMap;
import java.util.Map;
import net.dabicco.devouringstorms.entity.model.ModEntityModelLayers;
import net.dabicco.devouringstorms.entity.model.Tentacle;
import net.minecraft.client.model.geom.EntityModelSet;

public final class BowelsLimbModels {
   private static final int MAX_CACHED = 64;
   private static final Map<Integer, Tentacle> MODELS = new HashMap();

   private BowelsLimbModels() {
   }

   public static Tentacle forEntity(EntityModelSet modelSet, int entityId) {
      if (MODELS.size() > 64) {
         MODELS.clear();
      }

      return (Tentacle)MODELS.computeIfAbsent(entityId, (id) -> new Tentacle(modelSet.bakeLayer(ModEntityModelLayers.TENTACLE)));
   }

   public static void clear() {
      MODELS.clear();
   }
}
