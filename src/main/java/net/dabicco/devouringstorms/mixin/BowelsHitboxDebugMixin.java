package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.BowelsFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityHitboxDebugRenderer.class})
public abstract class BowelsHitboxDebugMixin {
   @Inject(
      method = {"showHitboxes(Lnet/minecraft/world/entity/Entity;FZ)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$honestHitbox(Entity entity, float partialTick, boolean server, CallbackInfo ci) {
      if (BowelsFrame.boxAxis(entity) != Direction.DOWN) {
         ci.cancel();
         Vec3 drift = entity.getPosition(partialTick).subtract(entity.position());
         Gizmos.cuboid(entity.getBoundingBox().move(drift), GizmoStyle.stroke(-16711936));
         Gizmos.point(entity.position().add(drift), -1, 0.06F);
         Vec3 eye = entity.getEyePosition().add(drift);
         Gizmos.point(eye, -65536, 0.08F);
         Gizmos.arrow(eye, eye.add(entity.getViewVector(partialTick).scale(2.0)), -16776961);
         Gizmos.arrow(eye, eye.add(entity.getDeltaMovement()), -16711681);
         Vec3 origin = entity.position().add(drift);
         Gizmos.arrow(origin, origin.add(BowelsFrame.down(BowelsFrame.boxAxis(entity))), -65281);
         Minecraft client = Minecraft.getInstance();
         if (entity == client.getCameraEntity() && client.gameRenderer != null) {
            Vec3 camera = client.gameRenderer.mainCamera().position();
            if (camera.distanceToSqr(eye) > 1.0E-6) {
               Gizmos.line(camera, eye, -256, 3.0F);
               Gizmos.point(camera, -256, 0.1F);
            }
         }
      }
   }
}
