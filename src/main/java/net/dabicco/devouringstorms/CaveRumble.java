package net.dabicco.devouringstorms;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.dabicco.devouringstorms.config.WitherStormConfigs;
import net.dabicco.devouringstorms.config.WitherStormWorldConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.dabicco.devouringstorms.network.CaveRumblePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class CaveRumble {
   private static final double OVERHEAD_RADIUS = (double)90.0F;
   private static final int MIN_COVER = 6;
   private static final int CEILING_SEARCH = 24;
   private static final Map<ResourceKey<Level>, Long> nextRumble = new HashMap();
   private static final Map<ResourceKey<Level>, Long> rumbleUntil = new HashMap();
   private static final int RATTLE_RADIUS = 9;
   private static final int FLICKER_TICKS = 3;

   private CaveRumble() {
   }

   public static void tick(ServerLevel level) {
      WitherStormWorldConfig cfg = WitherStormConfigs.get(level);
      if (cfg.caveRumble == 0) {
         nextRumble.clear();
         rumbleUntil.clear();
      } else {
         long now = level.getGameTime();
         ResourceKey<Level> key = level.dimension();
         Long until = (Long)rumbleUntil.get(key);
         if (until != null) {
            if (now < until) {
               for(ServerPlayer player : level.players()) {
                  if (isUnderStorm(level, player)) {
                     shakeLoose(level, player, cfg);
                  }
               }

               return;
            }

            rumbleUntil.remove(key);
         }

         boolean anyone = false;

         for(ServerPlayer player : level.players()) {
            if (isUnderStorm(level, player)) {
               anyone = true;
               break;
            }
         }

         if (!anyone) {
            nextRumble.remove(key);
         } else {
            Long next = (Long)nextRumble.get(key);
            if (next == null) {
               nextRumble.put(key, now + interval(level.getRandom(), cfg));
            } else {
               if (now >= next) {
                  begin(level, cfg, now);
               }

            }
         }
      }
   }

   private static long interval(RandomSource random, WitherStormWorldConfig cfg) {
      int base = Math.max(5, cfg.caveRumbleInterval) * 20;
      return (long)(base + random.nextInt(Math.max(1, base / 2)));
   }

   private static void begin(ServerLevel level, WitherStormWorldConfig cfg, long now) {
      int duration = Math.max(20, cfg.caveRumbleDuration * 20);
      ResourceKey<Level> key = level.dimension();
      rumbleUntil.put(key, now + (long)duration);
      nextRumble.put(key, now + (long)duration + interval(level.getRandom(), cfg));
      float intensity = (float)Math.max((double)0.0F, cfg.caveRumbleIntensity);

      for(ServerPlayer player : level.players()) {
         if (isUnderStorm(level, player)) {
            ServerPlayNetworking.send(player, new CaveRumblePayload(duration, intensity));
            level.playSound((Entity)null, player.getX(), player.getY() + (double)12.0F, player.getZ(), SoundEvents.DEEPSLATE_BRICKS_BREAK, SoundSource.HOSTILE, 2.4F * Math.min(1.0F, intensity), 0.35F);
         }
      }

   }

   private static boolean isUnderStorm(ServerLevel level, ServerPlayer player) {
      int surface = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, player.getBlockX(), player.getBlockZ());
      if (surface - player.getBlockY() < 6) {
         return false;
      } else {
         for(WitherStormEntity storm : level.getEntitiesOfClass(WitherStormEntity.class, player.getBoundingBox().inflate((double)90.0F, (double)512.0F, (double)90.0F))) {
            if (storm.isAlive() && storm.isPhase4() && !(storm.getY() < player.getY() + (double)8.0F)) {
               double dx = storm.getX() - player.getX();
               double dz = storm.getZ() - player.getZ();
               if (dx * dx + dz * dz <= (double)8100.0F) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static void rattleFixtures(ServerLevel level, ServerPlayer player, WitherStormWorldConfig cfg) {
      RandomSource random = level.getRandom();
      double intensity = Math.max((double)0.0F, cfg.caveRumbleIntensity);
      if (!(intensity <= (double)0.0F)) {
         BlockPos centre = player.blockPosition();

         for(BlockPos pos : BlockPos.betweenClosed(centre.offset(-9, -9, -9), centre.offset(9, 9, 9))) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
               if (state.is(Blocks.DECORATED_POT)) {
                  if (!((double)random.nextFloat() > (double)0.35F * intensity)) {
                     level.destroyBlock(pos, true);
                     level.playSound((Entity)null, pos, SoundEvents.DECORATED_POT_SHATTER, SoundSource.BLOCKS, 0.9F, 0.9F + random.nextFloat() * 0.2F);
                  }
               } else {
                  String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
                  boolean bulb = path.endsWith("copper_bulb");
                  boolean lamp = state.is(Blocks.REDSTONE_LAMP);
                  if ((bulb || lamp) && !((double)random.nextFloat() > (double)0.5F * intensity)) {
                     BooleanProperty LIT = BlockStateProperties.LIT;
                     if (state.hasProperty(LIT)) {
                        boolean wasLit = (Boolean)state.getValue(LIT);
                        level.setBlock(pos, (BlockState)state.setValue(LIT, !wasLit), 2);
                        level.scheduleTick(pos, state.getBlock(), 3);
                     }
                  }
               }
            }
         }

      }
   }

   public static void restoreFlicker(ServerLevel level, BlockPos pos, BlockState state) {
      BooleanProperty LIT = BlockStateProperties.LIT;
      if (state.hasProperty(LIT)) {
         boolean shouldBeLit = level.hasNeighborSignal(pos);
         if ((Boolean)state.getValue(LIT) != shouldBeLit) {
            level.setBlock(pos, (BlockState)state.setValue(LIT, shouldBeLit), 2);
         }

      }
   }

   private static void shakeLoose(ServerLevel level, ServerPlayer player, WitherStormWorldConfig cfg) {
      rattleFixtures(level, player, cfg);
      RandomSource random = level.getRandom();
      double intensity = Math.max((double)0.0F, cfg.caveRumbleIntensity);
      int attempts = (int)Math.round((double)6.0F * intensity);

      for(int i = 0; i < attempts; ++i) {
         int ox = player.getBlockX() + random.nextInt(17) - 8;
         int oz = player.getBlockZ() + random.nextInt(17) - 8;
         BlockPos ceiling = findCeiling(level, ox, player.getBlockY(), oz);
         if (ceiling != null) {
            BlockState state = level.getBlockState(ceiling);
            if (!state.isAir()) {
               if (state.getBlock() instanceof PointedDripstoneBlock) {
                  if ((double)random.nextFloat() < (double)0.85F * intensity) {
                     level.scheduleTick(ceiling, state.getBlock(), 1);
                     level.playSound((Entity)null, ceiling, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 0.5F, 1.4F);
                  }
               } else if (!state.is(Blocks.GRAVEL) && !state.is(Blocks.SAND) && !state.is(Blocks.RED_SAND) && !state.is(Blocks.POINTED_DRIPSTONE)) {
                  if (!((double)random.nextFloat() > (double)0.35F * intensity) && !(state.getDestroySpeed(level, ceiling) < 0.0F)) {
                     FallingBlockEntity.fall(level, ceiling, state);
                     level.playSound((Entity)null, ceiling, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.9F, 0.6F + random.nextFloat() * 0.2F);
                  }
               } else {
                  level.scheduleTick(ceiling, state.getBlock(), 1);
               }
            }
         }
      }

   }

   private static BlockPos findCeiling(ServerLevel level, int x, int y, int z) {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y + 2, z);

      for(int i = 0; i < 24; ++i) {
         if (!level.getBlockState(pos).isAir()) {
            return pos.immutable();
         }

         pos.move(0, 1, 0);
      }

      return null;
   }

   public static void forget(UUID id) {
      nextRumble.remove(id);
      rumbleUntil.remove(id);
   }
}
