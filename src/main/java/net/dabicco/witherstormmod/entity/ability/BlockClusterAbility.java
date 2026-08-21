package net.dabicco.witherstormmod.entity.ability;

import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.entity.ModEntityTypes;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

/**
 * Block-cluster absorption (phase 4+, the signature "the storm tears chunks out of the
 * ground" behaviour).
 *
 * Periodically rips a chunk of terrain out of the ground near the storm and launches it
 * as an orbiting {@link WitherStormClusterEntity}. Feeding the cluster back into the
 * storm converts the torn blocks into growth. This is the core of the video's
 * "flattens your world" behaviour alongside the tractor-beam/absorb goals.
 */
public class BlockClusterAbility implements StormAbility {
   private int cooldown;

   @Override
   public double phaseThreshold() {
      return 4.0;
   }

   @Override
   public void tick(WitherStormEntity storm, ServerLevel level) {
      if (--this.cooldown > 0) {
         return;
      }
      this.cooldown = Math.max(20, WitherStormConfigs.get(level).clusterCooldown);
      if (storm.isCollapsed()) {
         return;
      }
      BlockPos target = this.findSurfaceBlock(storm, level);
      if (target == null) {
         return;
      }
      WitherStormClusterEntity cluster = new WitherStormClusterEntity(ModEntityTypes.WITHER_STORM_CLUSTER, level);
      cluster.setOrigin(target);
      int radius = level.getRandom().nextInt(2);
      cluster.setRadius(radius);
      BlockPos spawnPos = WitherStormClusterEntity.adjustSpawnOrigin(target, radius);
      cluster.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
      cluster.absorbBlocks(target);
      cluster.setTargetStorm(storm);
      level.addFreshEntity(cluster);
      WitherStormClusterEntity.syncBlocksToTracking(cluster);
   }

   private BlockPos findSurfaceBlock(WitherStormEntity storm, ServerLevel level) {
      int range = WitherStormConfigs.get(level).pickupRange();
      for (int i = 0; i < 4; ++i) {
         int x = storm.getBlockX() + level.getRandom().nextInt(range * 2) - range;
         int z = storm.getBlockZ() + level.getRandom().nextInt(range * 2) - range;
         BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING, new BlockPos(x, 0, z)).below();
         BlockState state = level.getBlockState(surface);
         if (!state.isAir() && state.getFluidState().isEmpty()) {
            return surface;
         }
      }
      return null;
   }
}
