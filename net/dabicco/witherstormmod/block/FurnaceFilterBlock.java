package net.dabicco.witherstormmod.block;

import com.mojang.serialization.MapCodec;
import net.dabicco.witherstormmod.block.entity.FurnaceFilterBlockEntity;
import net.dabicco.witherstormmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FurnaceFilterBlock extends BaseEntityBlock {
   public static final MapCodec<FurnaceFilterBlock> CODEC = simpleCodec(FurnaceFilterBlock::new);
   private static final VoxelShape SHAPE = Block.box((double)1.0F, (double)0.0F, (double)1.0F, (double)15.0F, (double)4.0F, (double)15.0F);

   public FurnaceFilterBlock(BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(BlockStateProperties.LIT, false));
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.LIT});
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return level.getBlockState(pos.below()).getBlock() instanceof AbstractFurnaceBlock;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new FurnaceFilterBlockEntity(pos, state);
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.FURNACE_FILTER, FurnaceFilterBlockEntity::serverTick);
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!level.isClientSide()) {
         BlockEntity var7 = level.getBlockEntity(pos);
         if (var7 instanceof FurnaceFilterBlockEntity) {
            FurnaceFilterBlockEntity be = (FurnaceFilterBlockEntity)var7;
            player.openMenu(be);
         }
      }

      return InteractionResult.SUCCESS;
   }

   protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
      BlockEntity var6 = level.getBlockEntity(pos);
      if (var6 instanceof FurnaceFilterBlockEntity be) {
         if (!be.getOutput().isEmpty()) {
            Block.popResource(level, pos, be.getOutput());
         }
      }

      super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
   }

   protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction dir, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
      if (!state.canSurvive(level, pos)) {
         tickAccess.scheduleTick(pos, this, 1);
      }

      return super.updateShape(state, level, tickAccess, pos, dir, neighborPos, neighborState, random);
   }

   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!state.canSurvive(level, pos)) {
         dropResources(state, level, pos, level.getBlockEntity(pos));
         level.removeBlock(pos, false);
      }

   }
}
