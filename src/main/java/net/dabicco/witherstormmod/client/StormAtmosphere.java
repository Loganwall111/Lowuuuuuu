package net.dabicco.witherstormmod.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Clean registration for the Wither Storm's atmosphere + world-space VFX.
 *
 * Wires the self-contained, mixin-free renderers/effects into the Fabric client render
 * pipeline. The heavy bloom/glow pipeline (StormBloom / StormSceneDepth / StormShadow /
 * StormSunGlow) depends on the missing mixin accessors, so those are intentionally left
 * out until the mixins are restored. This class hooks:
 *
 *   - ClientDistantStormManager storm tracking (+ expiry pruning)
 *   - StormSkyDarken.update() each frame so the sky darkens near the storm
 *   - ClientSicknessManager / ClientWitheredManager pruning
 *   - DistantStormRenderer (distant storm model + debris)
 *   - FormidibombBlast (the giant bomb's purple blast sphere)
 *   - WitheredRenderer (withered-fragment / cast glows)
 */
public final class StormAtmosphere {
   private StormAtmosphere() {
   }

   public static void register() {
      // Per-tick state updates (sky darkening, distant-storm smoothing, cast/sickness pruning).
      ClientTickEvents.END_CLIENT_TICK.register(StormAtmosphere::tick);

      // World-space render submits.
      LevelRenderEvents.COLLECT_SUBMITS.register(DistantStormRenderer::render);
      LevelRenderEvents.COLLECT_SUBMITS.register(FormidibombBlast::render);
      LevelRenderEvents.COLLECT_SUBMITS.register(WitheredRenderer::render);
   }

   private static void tick(Minecraft client) {
      if (client.level != null) {
         ClientDistantStormManager.all(); // expire stale distant storms
         ClientSicknessManager.prune();
         ClientWitheredManager.prune();
         Vec3 cam = client.gameRenderer.getMainCamera().getPosition();
         float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         StormSkyDarken.update(cam, partial);
      }
   }
}
