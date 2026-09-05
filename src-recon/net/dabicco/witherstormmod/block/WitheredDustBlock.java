package net.dabicco.witherstormmod.block;

import net.dabicco.witherstormmod.BowelsGravity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.redstone.Orientation;

public class WitheredDustBlock extends RedStoneWireBlock {
   public WitheredDustBlock(Properties properties) {
      super(properties);
   }

   private static boolean saturated(BlockGetter level) {
      return level instanceof Level world && BowelsGravity.isBowels(world);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = super.getStateForPlacement(context);
      return saturated(context.getLevel()) ? (BlockState)state.setValue(POWER, 15) : state;
   }

   protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
      if (saturated(level)) {
         if ((Integer)state.getValue(POWER) != 15) {
            level.setBlock(pos, (BlockState)state.setValue(POWER, 15), 2);
         }
      } else {
         super.onPlace(state, level, pos, old, moving);
      }
   }

   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean moving) {
      if (saturated(level)) {
         if (!state.canSurvive(level, pos)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
         }
      } else {
         super.neighborChanged(state, level, pos, block, orientation, moving);
      }
   }

   protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      if (saturated(level)) {
         return 0;
      } else {
         BlockPos asker = pos.relative(direction.getOpposite());
         return level.getBlockState(asker).is(Blocks.REDSTONE_WIRE) ? 0 : super.getSignal(state, level, pos, direction);
      }
   }

   protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      if (saturated(level)) {
         return 0;
      } else {
         BlockPos asker = pos.relative(direction.getOpposite());
         return level.getBlockState(asker).is(Blocks.REDSTONE_WIRE) ? 0 : super.getDirectSignal(state, level, pos, direction);
      }
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if ((Integer)state.getValue(POWER) != 0 && !(random.nextFloat() > 0.2F)) {
         double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.7;
         double y = pos.getY() + 0.0625;
         double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.7;
         level.addParticle(new DustParticleOptions(ARGB.color(255, tint((Integer)state.getValue(POWER))), 1.0F), x, y, z, 0.0, 0.0, 0.0);
      }
   }

   public static int tint(int power) {
      float t = power / 15.0F;
      int r = (int)(95.0F + 160.0F * t);
      int g = (int)(18.0F + 62.0F * t);
      int b = (int)(72.0F + 128.0F * t);
      return r << 16 | g << 8 | b;
   }
}
