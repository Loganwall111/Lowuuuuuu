package net.dabicco.witherstormmod;

import java.util.Objects;
import java.util.Set;
import net.dabicco.witherstormmod.block.WitheredDustBlock;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

class DabyWitherStormModClient$1 implements BlockTintSource {
   DabyWitherStormModClient$1(final DabyWitherStormModClient this$0) {
      Objects.requireNonNull(this$0);
      super();
   }

   public int color(BlockState state) {
      return WitheredDustBlock.tint((Integer)state.getValue(RedStoneWireBlock.POWER));
   }

   public Set<Property<?>> relevantProperties() {
      return Set.of(RedStoneWireBlock.POWER);
   }
}
