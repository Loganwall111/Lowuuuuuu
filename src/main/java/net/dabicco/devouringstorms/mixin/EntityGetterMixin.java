package net.dabicco.devouringstorms.mixin;

import java.util.ArrayList;
import java.util.List;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.dabicco.devouringstorms.entity.cluster.WitherStormClusterEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityGetter.class})
public interface EntityGetterMixin {
   @Inject(
      method = {"getEntityCollisions"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$clusterCollisions(Entity entity, AABB box, CallbackInfoReturnable<List<VoxelShape>> cir) {
      if (entity instanceof Player) {
         List<VoxelShape> base = (List<VoxelShape>)cir.getReturnValue();
         List<VoxelShape> combined = null;

         for (WitherStormClusterEntity cluster : entity.level().getEntitiesOfClass(WitherStormClusterEntity.class, box.inflate(1.0))) {
            for (AABB blockBox : cluster.getCollisionBoxes()) {
               if (blockBox.intersects(box)) {
                  if (combined == null) {
                     combined = new ArrayList<>(base);
                  }

                  combined.add(Shapes.create(blockBox));
               }
            }
         }

         for (WitherStormEntity storm : entity.level().getEntitiesOfClass(WitherStormEntity.class, box.inflate(6.0))) {
            for (AABB blockBox : storm.getBackCollisionBoxes()) {
               if (blockBox.intersects(box)) {
                  if (combined == null) {
                     combined = new ArrayList<>(base);
                  }

                  combined.add(Shapes.create(blockBox));
               }
            }
         }

         if (combined != null) {
            cir.setReturnValue(combined);
         }
      }
   }
}
